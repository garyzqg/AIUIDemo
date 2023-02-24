package payfun.lib.basis.thread;

import android.os.Handler;
import android.os.Looper;
import android.os.Process;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * @author : 时光
 * e-mail : qurongzhen@pay.media
 * date   : 2021/9/24 16:06
 * desc   : <带Handler的线程>
 */
public class HandlerTask extends Thread {
    int mPriority;
    int mTid = -1;
    Looper mLooper;
    @Nullable
    private Handler mHandler;

    public HandlerTask(String name) {
        super(name);
        mPriority = Process.THREAD_PRIORITY_DEFAULT;
    }

    public HandlerTask(String name, int priority) {
        super(name);
        mPriority = priority;
    }


    protected void onLooperPrepared() {
    }

    @Override
    public void run() {
        mTid = Process.myTid();
        Looper.prepare();
        synchronized (this) {
            mLooper = Looper.myLooper();
            notifyAll();
        }
        Process.setThreadPriority(mPriority);
        onLooperPrepared();
        Looper.loop();
        mTid = -1;
    }


    public Looper getLooper() {
        if (!isAlive() && mLooper == null) {
            return null;
        }
        synchronized (this) {
            while (isAlive() && mLooper == null) {
                try {
                    wait();
                } catch (InterruptedException e) {
                }
            }
        }
        return mLooper;
    }


    @NonNull
    public Handler getThreadHandler() {
        if (mHandler == null) {
            mHandler = new Handler(getLooper());
        }
        return mHandler;
    }


    public boolean quit() {
        Looper looper = getLooper();
        if (looper != null) {
            looper.quit();
            return true;
        }
        return false;
    }


    public boolean quitSafely() {
        Looper looper = getLooper();
        if (looper != null) {
            looper.quitSafely();
            return true;
        }
        return false;
    }


    public int getThreadId() {
        return mTid;
    }
}
