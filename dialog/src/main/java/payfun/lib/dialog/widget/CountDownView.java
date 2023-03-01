package payfun.lib.dialog.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import payfun.lib.dialog.R;

/**
 * @author : zhangqg
 * date   : 2022/6/2 15:07
 * desc   : <功能简述>
 */

/**
 * @author : zhangqg
 * date   : 2023/3/1 15:30
 * desc   : <提示对话框>
 */
public class CountDownView extends LinearLayout {

    private TextView tvCountDown;
    private CountDownListener listener;
    private CountDownFinishListener finishListener;
    private CountDownTimer2 downTimer;
    private int msgColor;
    private String defMsg;
    private String prefixMsg;
    private String suffixMsg;
    private long totalTime;
    private long intervalTime;
    private float msgTextSize;

    public CountDownView(Context context) {
        this(context, null);
    }

    public CountDownView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CountDownView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.CountDownView);
        msgTextSize = array.getDimension(R.styleable.CountDownView_msgTextSize, 28);
        msgColor = array.getColor(R.styleable.CountDownView_msgColor, Color.parseColor("#ff999999"));
        defMsg = array.getString(R.styleable.CountDownView_defMsg);
        prefixMsg = array.getString(R.styleable.CountDownView_prefixMsg);
        suffixMsg = array.getString(R.styleable.CountDownView_suffixMsg);
        totalTime = (long) array.getFloat(R.styleable.CountDownView_totalTime, 60000);
        intervalTime = (long) array.getFloat(R.styleable.CountDownView_intervalTime, 1000);
        array.recycle();
        initView(context, attrs);
    }

    private void initView(Context context, AttributeSet attrs) {
        tvCountDown = new TextView(context);
        tvCountDown.setTextColor(msgColor);
        tvCountDown.setTextSize(TypedValue.COMPLEX_UNIT_SP, msgTextSize);
        LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tvCountDown.setLayoutParams(layoutParams);
        if (!TextUtils.isEmpty(defMsg)) {
            setTvDefMsg(defMsg);
        } else {
            setTvCountDown(String.valueOf(totalTime / 1000));
        }
        addView(tvCountDown);
    }

    public void setTvCountDown(String content) {
        if (tvCountDown != null) {
            String format = String.format("%s%s%s", empty(prefixMsg), content, empty(suffixMsg));
            tvCountDown.setText(format);
        }
    }

    public void setTvDefMsg(String content) {
        if (tvCountDown != null) {
            tvCountDown.setText(content);
        }
    }

    public CountDownView setCountDownFormat(String prefix, String suffix) {
        this.prefixMsg = prefix;
        this.suffixMsg = suffix;
        if (!TextUtils.isEmpty(defMsg)) {
            setTvDefMsg(defMsg);
        } else {
            setTvCountDown(String.valueOf(totalTime / 1000));
        }
        return this;
    }

    /**
     * @param totalTime    倒计时时间:单位毫秒
     * @param intervalTime 倒计时间隔时间：单位毫秒
     * @return VIEW
     */
    public CountDownView setCountDownValue(long totalTime, long intervalTime) {
        this.totalTime = totalTime;
        this.intervalTime = intervalTime;
        return this;
    }

    public TextView getTvCountDown() {
        return tvCountDown;
    }

    public CountDownView setCountDownListener(CountDownListener listener) {
        this.listener = listener;
        return this;
    }

    public CountDownView setCountDownFinishListener(CountDownFinishListener listener) {
        this.finishListener = listener;
        return this;
    }

    /**
     * 很重要啊，组件内部处理释放资源，外部不需要操心重置倒计时以及资源释放
     */
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopCountDown();
    }


    /**
     * 开始倒计时
     */
    public void startCountDown() {
        stopCountDown();
        downTimer = new CountDownTimer2(totalTime, intervalTime) {
            @Override
            public void onTick(long millisUntilFinished) {
                long seconds = Math.round((double) millisUntilFinished / 1000);
                setTvCountDown(String.valueOf(seconds));
                if (listener != null) {
                    listener.onTick(seconds);
                }
            }

            @Override
            public void onPause(long millisUntilFinished) {
                long seconds = Math.round((double) millisUntilFinished / 1000);
                setTvCountDown(String.valueOf(seconds));
                if (listener != null) {
                    listener.onPause(seconds);
                }
            }

            @Override
            public void onFinish() {
                stopCountDown();
                if (!TextUtils.isEmpty(defMsg)) {
                    setTvDefMsg(defMsg);
                } else {
                    setTvCountDown("0");
                }
                if (listener != null) {
                    listener.onFinish();
                }
                if (finishListener != null) {
                    finishListener.onFinish();
                }
            }
        };
        downTimer.start();
    }

    public void pauseCountDown() {
        if (downTimer != null) {
            downTimer.pause();
        }
    }

    public void resumeCountDown() {
        if (downTimer != null) {
            downTimer.resume();
        }
    }

    public void stopCountDown() {
        if (downTimer != null) {
            downTimer.cancel();
            downTimer = null;
        }
    }


    private String empty(String content) {
        return TextUtils.isEmpty(content) ? "" : content;
    }


    public interface CountDownFinishListener {
        void onFinish();
    }

    public interface CountDownListener {
        void onTick(long l);

        void onPause(long l);

        void onFinish();
    }
}
