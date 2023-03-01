package payfun.lib.dialog.widget;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;

/**
 * @author : zhangqg
 * date   : 2023/3/1 15:30
 * desc   : <倒计时>
 * * 1. 处理5.0以下版本不能取消问题；
 * * 2. 新增暂停，继续功能和暂停回调；
 * * 3. 未处理本类倒计时误差问题： 在你自己的 onTick() 里 修改一下秒数的计算，改为四舍五入取整
 * *      seconds = Math.round((double) millisecond / 1000);
 * * 4. 未处理内存泄漏问题；
 */
public abstract class CountDownTimer2 {
    /**
     * Millis since epoch when alarm should stop.
     */
    private final long mMillisInFuture;

    /**
     * The interval in millis that the user receives callbacks
     */
    private final long mCountdownInterval;

    private long mStopTimeInFuture;

    /**
     * boolean representing if the timer was cancelled
     */
    private boolean mCancelled = false;

    /**
     * @param millisInFuture    The number of millis in the future from the call to {@link #start()} until the countdown is done and {@link #onFinish()} is called.
     * @param countDownInterval The interval along the way to receive {@link #onTick(long)} callbacks.
     */
    public CountDownTimer2(long millisInFuture, long countDownInterval) {
        mMillisInFuture = millisInFuture;
        mCountdownInterval = countDownInterval;
    }

    /**
     * Cancel the countdown.
     */
    public synchronized final void cancel() {
        mCancelled = true;
        mLeftTime = 0;
        mHandler.removeMessages(MSG);
    }

    /**
     * Start the countdown.
     */
    public synchronized final CountDownTimer2 start() {
        cancel();
        mCancelled = false;
        if (mMillisInFuture <= 0) {
            onFinish();
            return this;
        }
        mStopTimeInFuture = SystemClock.elapsedRealtime() + mMillisInFuture;
        mHandler.sendMessage(mHandler.obtainMessage(MSG));
        return this;
    }

    /**
     * 暂停时还剩下的时间==0表示不是暂停状态
     */
    private long mLeftTime;

    /**
     * pause the countdown.
     */
    public synchronized final CountDownTimer2 pause() {
        if (mCancelled) {
            return this;
        }
        mCancelled = true;
        mHandler.removeMessages(MSG);
        mLeftTime = mStopTimeInFuture - SystemClock.elapsedRealtime();
        onPause(mLeftTime);
        return this;
    }

    /**
     * resume the countdown.
     */
    public synchronized final CountDownTimer2 resume() {
        if (mCancelled) {
            mCancelled = false;
            if (mMillisInFuture <= 0 || mLeftTime <= 0) {
                onFinish();
                return this;
            }
            mStopTimeInFuture = SystemClock.elapsedRealtime() + mLeftTime;
            mHandler.sendMessage(mHandler.obtainMessage(MSG));
        }
        return this;
    }

    /**
     * Callback fired on regular interval.
     *
     * @param millisUntilFinished The amount of time until finished.
     */
    public abstract void onTick(long millisUntilFinished);

    public abstract void onPause(long millisUntilFinished);

    /**
     * Callback fired when the time is up.
     */
    public abstract void onFinish();

    private static final int MSG = 1;

    // handles counting down
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler(Looper.getMainLooper()) {

        @Override
        public void handleMessage(Message msg) {

            synchronized (CountDownTimer2.this) {
                if (mCancelled) {
                    return;
                }

                final long millisLeft = mStopTimeInFuture - SystemClock.elapsedRealtime();

                if (millisLeft <= 0) {
                    onFinish();
                } else {
                    long lastTickStart = SystemClock.elapsedRealtime();
                    onTick(millisLeft);

                    // take into account user's onTick taking time to execute
                    long lastTickDuration = SystemClock.elapsedRealtime() - lastTickStart;
                    long delay;

                    if (millisLeft < mCountdownInterval) {
                        // just delay until done
                        delay = millisLeft - lastTickDuration;

                        // special case: user's onTick took more than interval to
                        // complete, trigger onFinish without delay
                        if (delay < 0) {
                            delay = 0;
                        }
                    } else {
                        delay = mCountdownInterval - lastTickDuration;

                        // special case: user's onTick took more than interval to
                        // complete, skip to next interval
                        while (delay < 0) {
                            delay += mCountdownInterval;
                        }
                    }

                    sendMessageDelayed(obtainMessage(MSG), delay);
                }
            }
        }
    };
}
