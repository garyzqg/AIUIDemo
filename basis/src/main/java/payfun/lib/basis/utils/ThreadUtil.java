package payfun.lib.basis.utils;

import android.os.Looper;
import android.util.Log;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author : 时光
 * e-mail : qurongzhen@pay.media
 * date   : 2022/6/21 16:29
 * desc   : <p>线程工具类
 */
public final class ThreadUtil {
    private static ThreadPoolExecutor sCPUThreadPoolExecutor;
    private static ThreadPoolExecutor sIOThreadPoolExecutor;

    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    // We want at least 2 threads and at most 4 threads in the core pool,
    // preferring to have 1 less than the CPU count to avoid saturating
    // the CPU with background work

    public static final int CORE_POOL_SIZE = Math.max(2, Math.min(CPU_COUNT - 1, 5));
    private static final int MAXIMUM_POOL_SIZE = CORE_POOL_SIZE;
    private static final int KEEP_ALIVE_SECONDS = 5;
    private static final BlockingQueue<Runnable> sPoolWorkQueue = new LinkedBlockingQueue<>();
    private static final DefaultThreadFactory sThreadFactory = new DefaultThreadFactory();
    private static final RejectedExecutionHandler sHandler = new RejectedExecutionHandler() {// 一般不会到这里
        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            Executors.newCachedThreadPool().execute(r);
        }
    };


    private static class SingletonHolder {
        private static final ThreadUtil INSTANCE = new ThreadUtil();
    }

    private ThreadUtil() {
    }

    public static ThreadUtil getInstance() {
        return SingletonHolder.INSTANCE;
    }


    /**
     * 获取CPU线程池
     *
     * @return
     */
    public ThreadPoolExecutor defaultCPUExecutor() {
        return sCPUThreadPoolExecutor;
    }

    /**
     * 获取IO线程池
     *
     * @return
     */
    public ThreadPoolExecutor defaultIOExecutor() {
        return sIOThreadPoolExecutor;
    }


    public static void runOnUiThread(final Runnable runnable) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            runnable.run();
        } else {
            InitUtil.getMainHandler().post(runnable);
        }
    }

    public static void runOnUiThreadDelayed(final Runnable runnable, long delayMillis) {
        InitUtil.getMainHandler().postDelayed(runnable, delayMillis);
    }


    /**
     * 获取新的单线程池
     *
     * @return
     */
    public static ThreadPoolExecutor newSingleExecutor() {
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(1, 1,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(), new DefaultThreadFactory());
        return threadPoolExecutor;
    }

    /**
     * 获取新的CPU线程池
     *
     * @return
     */
    public static ThreadPoolExecutor newCPUExecutor() {
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
                CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE_SECONDS, TimeUnit.SECONDS,
                sPoolWorkQueue, sThreadFactory, sHandler);
        threadPoolExecutor.allowCoreThreadTimeOut(true);
        return threadPoolExecutor;
    }

    /**
     * 获取新的IO线程池
     *
     * @return
     */
    public static ThreadPoolExecutor newIOExecutor() {
        return new ThreadPoolExecutor(0, Integer.MAX_VALUE,
                60L, TimeUnit.SECONDS,
                new SynchronousQueue<Runnable>(),
                new DefaultThreadFactory(), sHandler);
    }


    /**
     * The default thread factory.
     */
    private static class DefaultThreadFactory implements ThreadFactory {
        private static final AtomicInteger poolNumber = new AtomicInteger(1);
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        DefaultThreadFactory() {
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() :
                    Thread.currentThread().getThreadGroup();
            namePrefix = "TaskPool-" +
                    poolNumber.getAndIncrement() +
                    "-Thread-";
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r,
                    namePrefix + threadNumber.getAndIncrement(),
                    0);
            if (t.isDaemon()) {
                t.setDaemon(false);
            }
            if (t.getPriority() != Thread.NORM_PRIORITY) {
                t.setPriority(Thread.NORM_PRIORITY);
            }
            return t;
        }
    }

    static {
        sCPUThreadPoolExecutor = newCPUExecutor();

        sIOThreadPoolExecutor = newIOExecutor();

//        Executors.newScheduledThreadPool();
    }


    public abstract static class Task<T> implements Runnable {

        private static final int NEW = 0;
        private static final int RUNNING = 1;
        private static final int EXCEPTIONAL = 2;
        private static final int COMPLETING = 3;
        private static final int CANCELLED = 4;
        private static final int INTERRUPTED = 5;
        private static final int TIMEOUT = 6;

        private final AtomicInteger state = new AtomicInteger(NEW);

        private volatile boolean isSchedule;
        private volatile Thread runner;

        private Timer mTimer;
        private long mTimeoutMillis;
        private OnTimeoutListener mTimeoutListener;

        private Executor deliver;

        public abstract T doInBackground() throws Throwable;

        public abstract void onSuccess(T result);

        public abstract void onCancel();

        public abstract void onFail(Throwable t);

        @Override
        public void run() {
            if (isSchedule) {
                if (runner == null) {
                    if (!state.compareAndSet(NEW, RUNNING)) {
                        return;
                    }
                    runner = Thread.currentThread();
                    if (mTimeoutListener != null) {
                        Log.w("ThreadUtil", "Scheduled task doesn't support timeout.");
                    }
                } else {
                    if (state.get() != RUNNING) {
                        return;
                    }
                }
            } else {
                if (!state.compareAndSet(NEW, RUNNING)) {
                    return;
                }
                runner = Thread.currentThread();
                if (mTimeoutListener != null) {
                    mTimer = new Timer();
                    mTimer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            if (!isDone() && mTimeoutListener != null) {
                                timeout();
                                mTimeoutListener.onTimeout();
                                onDone();
                            }
                        }
                    }, mTimeoutMillis);
                }
            }
            try {
                final T result = doInBackground();
                if (isSchedule) {
                    if (state.get() != RUNNING) {
                        return;
                    }
                    getDeliver().execute(new Runnable() {
                        @Override
                        public void run() {
                            onSuccess(result);
                        }
                    });
                } else {
                    if (!state.compareAndSet(RUNNING, COMPLETING)) {
                        return;
                    }
                    getDeliver().execute(new Runnable() {
                        @Override
                        public void run() {
                            onSuccess(result);
                            onDone();
                        }
                    });
                }
            } catch (InterruptedException ignore) {
                state.compareAndSet(CANCELLED, INTERRUPTED);
            } catch (final Throwable throwable) {
                if (!state.compareAndSet(RUNNING, EXCEPTIONAL)) {
                    return;
                }
                getDeliver().execute(new Runnable() {
                    @Override
                    public void run() {
                        onFail(throwable);
                        onDone();
                    }
                });
            }
        }

        public void cancel() {
            cancel(true);
        }

        public void cancel(boolean mayInterruptIfRunning) {
            synchronized (state) {
                if (state.get() > RUNNING) {
                    return;
                }
                state.set(CANCELLED);
            }
            if (mayInterruptIfRunning) {
                if (runner != null) {
                    runner.interrupt();
                }
            }

            getDeliver().execute(new Runnable() {
                @Override
                public void run() {
                    onCancel();
                    onDone();
                }
            });
        }

        private void timeout() {
            synchronized (state) {
                if (state.get() > RUNNING) {
                    return;
                }
                state.set(TIMEOUT);
            }
            if (runner != null) {
                runner.interrupt();
            }
        }


        public boolean isCanceled() {
            return state.get() >= CANCELLED;
        }

        public boolean isDone() {
            return state.get() > RUNNING;
        }

        public Task<T> setDeliver(Executor deliver) {
            this.deliver = deliver;
            return this;
        }

        /**
         * Scheduled task doesn't support timeout.
         */
        public Task<T> setTimeout(final long timeoutMillis, final OnTimeoutListener listener) {
            mTimeoutMillis = timeoutMillis;
            mTimeoutListener = listener;
            return this;
        }

        private void setSchedule(boolean isSchedule) {
            this.isSchedule = isSchedule;
        }

        private Executor getDeliver() {
            if (deliver == null) {
                return getGlobalDeliver();
            }
            return deliver;
        }

        @CallSuper
        protected void onDone() {
            TASK_POOL_MAP.remove(this);
            if (mTimer != null) {
                mTimer.cancel();
                mTimer = null;
                mTimeoutListener = null;
            }
        }

        public interface OnTimeoutListener {
            void onTimeout();
        }
    }

    private static Executor sDeliver;
    private static final Map<Task, ExecutorService> TASK_POOL_MAP = new ConcurrentHashMap<>();

    private static Executor getGlobalDeliver() {
        if (sDeliver == null) {
            sDeliver = new Executor() {
                @Override
                public void execute(@NonNull Runnable command) {
                    runOnUiThread(command);
                }
            };
        }
        return sDeliver;
    }


    public abstract static class SimpleTask<T> extends Task<T> {

        @Override
        public void onCancel() {
            Log.e("ThreadUtils", "onCancel: " + Thread.currentThread());
        }

        @Override
        public void onFail(Throwable t) {
            Log.e("ThreadUtils", "onFail: ", t);
        }

    }

    public abstract static class ConsumerTask<Result> extends SimpleTask<Result> {

        private Consumer<Result> mConsumer;

        public ConsumerTask(final Consumer<Result> consumer) {
            mConsumer = consumer;
        }

        @Override
        public void onSuccess(Result result) {
            if (mConsumer != null) {
                mConsumer.accept(result);
            }
        }
    }


    public interface Consumer<T> {
        void accept(T t);
    }

    public interface Supplier<T> {
        T get();
    }

    public interface Func1<Ret, Par> {
        Ret call(Par param);
    }
}
