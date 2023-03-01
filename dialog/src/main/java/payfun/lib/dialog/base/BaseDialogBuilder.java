package payfun.lib.dialog.base;

import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;

import payfun.lib.dialog.R;
import payfun.lib.dialog.listener.DialogLifeCycleListener;
import payfun.lib.dialog.listener.OnAutoDismissListener;

/**
 * @author : zhangqg
 * date   : 2023/3/1 15:30
 * desc   : <构建对话框的参数>
 */
public abstract class BaseDialogBuilder<B extends BaseDialogBuilder> {
    public BaseDialogBuilder() {
        layoutId = initLayoutId();
    }

    /** 上下文对象 */
//    private final Context context;
    /**
     * Dialog 对象
     */
//    private DialogImpl dialog;

    /**
     * Dialog 布局
     */
    private int layoutId;

    /**
     * 主题样式
     */
    private int styleId = R.style.BaseDialogStyle;
    /**
     * 动画样式
     */
    private int animStyle = AnimAction.ANIM_DEFAULT;
    /**
     * 重心位置
     */
    private int gravity = Gravity.NO_GRAVITY;

    /**
     * 水平/垂直偏移
     */
    private int xOffset;
    private int yOffset;
    /**
     * 是否是全屏：为true时 width/height/xOffset/yOffset将不起作用
     */
    private boolean isFullScreen = false;
    /**
     * 是否隐藏底部导航栏
     */
    private boolean isHideNavBar = true;
    /**
     * 宽度和高度
     */
    private int width = 0;
    private int height = 0;

    /**
     * 背景遮盖层开关
     */
    private boolean backgroundDimEnabled = true;
    /**
     * 背景遮盖层透明度
     */
    private float backgroundDimAmount = 0.5f;


    /**
     * 是否能够被取消
     */
    private boolean cancelable = true;
    /**
     * 点击空白是否能够取消  前提是这个对话框可以被取消
     */
    private boolean canceledOnTouchOutside = true;


    /**
     * 对话框背景颜色，值0时为透明
     */
    private int backgroundColor = 0;
    /**
     * 是否开启模糊效果
     */
    private boolean isUseBlur = false;
    /**
     * 模糊透明度(0~255)
     */
    private int blurAlpha = 210;
    /**
     * 输入对话框，是否自动弹出输入键盘
     */
    private boolean autoShowInputKeyboard = false;


    /**
     * 是否自动隐藏对话框
     */
    private boolean isAutoDismiss;
    /**
     * 自动隐藏倒计时时间，单位：毫秒
     */
    private long autoDismissCountDownTime;
    /**
     * 自动隐藏倒计时时间间隔，单位：毫秒
     */
    private long autoDismissCountDownInterval;


    /**
     * 全局Dialog生命周期监听器
     */
    private DialogLifeCycleListener dialogLifeCycleListener;
    private DialogInterface.OnDismissListener onDismissListener;
    private DialogInterface.OnShowListener onShowListener;
    private DialogInterface.OnKeyListener onKeyListener;
    private OnAutoDismissListener onAutoDismissListener;


    /**
     * 初始化UI
     *
     * @return layoutId
     */
    protected abstract int initLayoutId();

    /**
     * 初始化view
     *
     * @param dialog             view
     * @param view               view
     * @param savedInstanceState 参数
     */
    protected abstract void onInitView(DialogFragment dialog, View view, Bundle savedInstanceState);


    //region set方法

    public B setLayoutId(int layoutId) {
        this.layoutId = layoutId;
        return (B) this;
    }

    public B setStyleId(int styleId) {
        this.styleId = styleId;
        return (B) this;
    }

    public B setAnimStyle(int animStyle) {
        this.animStyle = animStyle;
        return (B) this;
    }

    public B setGravity(int gravity) {
        this.gravity = gravity;
        return (B) this;
    }

    public B setXOffset(int xOffset) {
        this.xOffset = xOffset;
        return (B) this;
    }

    public B setYOffset(int yOffset) {
        this.yOffset = yOffset;
        return (B) this;
    }

    public B setFullScreen(boolean fullScreen) {
        isFullScreen = fullScreen;
        return (B) this;
    }

    public B setHideNavBar(boolean hideNavBar) {
        isHideNavBar = hideNavBar;
        return (B) this;
    }

    public B setWidth(int width) {
        this.width = width;
        return (B) this;
    }

    public B setHeight(int height) {
        this.height = height;
        return (B) this;
    }

    public B setBackgroundDimEnabled(boolean backgroundDimEnabled) {
        this.backgroundDimEnabled = backgroundDimEnabled;
        return (B) this;
    }

    public B setBackgroundDimAmount(float backgroundDimAmount) {
        this.backgroundDimAmount = backgroundDimAmount;
        return (B) this;
    }

    public B setCancelable(boolean cancelable) {
        this.cancelable = cancelable;
        return (B) this;
    }

    public B setCanceledOnTouchOutside(boolean canceledOnTouchOutside) {
        this.canceledOnTouchOutside = canceledOnTouchOutside;
        return (B) this;
    }

    public B setDialogLifeCycleListener(DialogLifeCycleListener dialogLifeCycleListener) {
        this.dialogLifeCycleListener = dialogLifeCycleListener;
        return (B) this;
    }

    public B setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
        return (B) this;
    }

    public B setUseBlur(boolean useBlur) {
        isUseBlur = useBlur;
        return (B) this;
    }

    public B setBlurAlpha(int blurAlpha) {
        this.blurAlpha = blurAlpha;
        return (B) this;
    }

    public B setAutoShowInputKeyboard(boolean autoShowInputKeyboard) {
        this.autoShowInputKeyboard = autoShowInputKeyboard;
        return (B) this;
    }

    public B setOnDismissListener(DialogInterface.OnDismissListener onDismissListener) {
        this.onDismissListener = onDismissListener;
        return (B) this;
    }

    public B setOnShowListener(DialogInterface.OnShowListener onShowListener) {
        this.onShowListener = onShowListener;
        return (B) this;
    }

    public B setOnKeyListener(DialogInterface.OnKeyListener onKeyListener) {
        this.onKeyListener = onKeyListener;
        return (B) this;
    }

    public B setOnAutoDismissListener(OnAutoDismissListener onAutoDismissListener) {
        this.onAutoDismissListener = onAutoDismissListener;
        return (B) this;
    }

    public B setAutoDismiss(boolean autoDismiss) {
        isAutoDismiss = autoDismiss;
        return (B) this;
    }

    public B setAutoDismissCountDownTime(long autoDismissCountDownTime) {
        this.autoDismissCountDownTime = autoDismissCountDownTime;
        return (B) this;
    }

    public B setAutoDismissCountDownInterval(long interval) {
        this.autoDismissCountDownInterval = interval;
        return (B) this;
    }

    //endregion set方法

    public static void build() {
    }

    //region get方法

    public int getLayoutId() {
        return layoutId;
    }

    public int getStyleId() {
        return styleId;
    }

    public int getAnimStyle() {
        return animStyle;
    }

    public int getGravity() {
        return gravity;
    }

    public int getXOffset() {
        return xOffset;
    }

    public int getYOffset() {
        return yOffset;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public boolean isBackgroundDimEnabled() {
        return backgroundDimEnabled;
    }

    public float getBackgroundDimAmount() {
        return backgroundDimAmount;
    }

    public boolean isCancelable() {
        return cancelable;
    }

    public boolean isCanceledOnTouchOutside() {
        return canceledOnTouchOutside;
    }

    public DialogLifeCycleListener getDialogLifeCycleListener() {
        return dialogLifeCycleListener;
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

    public boolean isUseBlur() {
        return isUseBlur;
    }

    public int getBlurAlpha() {
        return blurAlpha;
    }

    public boolean isFullScreen() {
        return isFullScreen;
    }

    public boolean isHideNavBar() {
        return isHideNavBar;
    }

    public boolean isAutoShowInputKeyboard() {
        return autoShowInputKeyboard;
    }

    public DialogInterface.OnDismissListener getOnDismissListener() {
        return onDismissListener;
    }

    public DialogInterface.OnShowListener getOnShowListener() {
        return onShowListener;
    }

    public DialogInterface.OnKeyListener getOnKeyListener() {
        return onKeyListener;
    }

    public OnAutoDismissListener getOnAutoDismissListener() {
        return onAutoDismissListener;
    }

    public boolean isAutoDismiss() {
        return isAutoDismiss;
    }

    public long getAutoDismissCountDownTime() {
        return autoDismissCountDownTime;
    }

    public long getAutoDismissCountDownInterval() {
        return autoDismissCountDownInterval;
    }

    //endregion get方法


    protected void useMsg(TextView textView, String msg) {
        if (textView == null) {
            return;
        }
        if (TextUtils.isEmpty(msg)) {
            textView.setVisibility(View.GONE);
        } else {
            textView.setText(msg);
            textView.setVisibility(View.VISIBLE);
        }
    }

    protected void useTextInfo(TextView textView, TextInfo textInfo) {
        if (textInfo == null) {
            return;
        }
        if (textView == null) {
            return;
        }
        if (textInfo.getFontSize() > 0) {
            textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, textInfo.getFontSize());
        }
        if (textInfo.getFontColor() != 1) {
            textView.setTextColor(textInfo.getFontColor());
        }
        if (textInfo.getGravity() != -1) {
            textView.setGravity(textInfo.getGravity());
        }
        Typeface font = Typeface.create(Typeface.SANS_SERIF, textInfo.isBold() ? Typeface.BOLD : Typeface.NORMAL);
        textView.setTypeface(font);
    }
}
