package payfun.lib.dialog.base;

import static android.view.View.NO_ID;
import static android.view.WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
import static android.view.WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import payfun.lib.dialog.listener.OnAutoDismissListener;
import payfun.lib.dialog.widget.CountDownTimer2;

/**
 * @author : 时光
 * e-mail : qurongzhen@pay.media
 * date   : 2022/5/19 15:17
 * desc   : <对话框实现类>
 */
public class DialogImpl extends DialogFragment {
    private BaseDialogBuilder mDialogConfig;
    private View mContentView;
    private CountDownTimer2 mCountDown;

    public DialogImpl(BaseDialogBuilder dialogBuilder) {
        mDialogConfig = dialogBuilder;
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        int layoutId = mDialogConfig.getLayoutId();
        mContentView = inflater.inflate(layoutId, container, false);
        return mContentView;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        onInitWindow();
        mDialogConfig.onInitView(this, view, savedInstanceState);

    }


    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        DialogInterface.OnDismissListener onDismissListener = mDialogConfig.getOnDismissListener();
        if (onDismissListener != null) {
            onDismissListener.onDismiss(dialog);
        }
    }


    @Override
    public void onDetach() {
        super.onDetach();
        if (mCountDown != null) {
            mCountDown.cancel();
        }
    }

    protected void onInitWindow() {
        setStyle(DialogFragment.STYLE_NORMAL, mDialogConfig.getStyleId());
        Dialog dialog = getDialog();
        if (dialog == null) {
            return;
        }
        dialog.setOnDismissListener(mDialogConfig.getOnDismissListener());
        dialog.setOnShowListener(mDialogConfig.getOnShowListener());
        dialog.setOnKeyListener(mDialogConfig.getOnKeyListener());

        dialog.setCancelable(mDialogConfig.isCancelable());
        dialog.setCanceledOnTouchOutside(mDialogConfig.isCanceledOnTouchOutside());

        Window window = dialog.getWindow();
        if (window == null) {
            return;
        }

        WindowManager.LayoutParams params = window.getAttributes();

        if (mDialogConfig.isHideNavBar()) {
            //隐藏底部导航栏
            params.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE;
        }

        if (mDialogConfig.isFullScreen()) {
            window.addFlags(FLAG_TRANSLUCENT_STATUS);
            //window.setStatusBarColor();
            window.getDecorView().setPadding(0, 0, 0, 0);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                params.layoutInDisplayCutoutMode = LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
            }
            params.width = getRootWidth();
            params.height = getRootHeight();
        } else {
            if (mDialogConfig.getWidth() != 0) {
                params.width = mDialogConfig.getWidth();
            }
            if (mDialogConfig.getHeight() != 0) {
                params.height = mDialogConfig.getHeight();
            }
            if (mDialogConfig.getXOffset() != 0) {
                params.x = mDialogConfig.getXOffset();
            }
            if (mDialogConfig.getYOffset() != 0) {
                params.y = mDialogConfig.getYOffset();
            }
        }

        if (mDialogConfig.getGravity() != 0) {
            params.gravity = mDialogConfig.getGravity();
        }

        params.windowAnimations = mDialogConfig.getAnimStyle();
        window.setAttributes(params);
        if (mDialogConfig.isBackgroundDimEnabled()) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            window.setDimAmount(mDialogConfig.getBackgroundDimAmount());
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        }
        int backgroundColor = mDialogConfig.getBackgroundColor();
        window.setBackgroundDrawable(new ColorDrawable(backgroundColor));

        if (mDialogConfig.isAutoDismiss() && mDialogConfig.getAutoDismissCountDownTime() > 0) {
            long downTime = mDialogConfig.getAutoDismissCountDownTime();
            long downInterval = mDialogConfig.getAutoDismissCountDownInterval();
            if (mCountDown != null) {
                mCountDown.cancel();
            }
            mCountDown = new CountDownTimer2(downTime, downInterval) {
                @Override
                public void onTick(long millisUntilFinished) {

                }

                @Override
                public void onPause(long millisUntilFinished) {

                }

                @Override
                public void onFinish() {
                    mDialogConfig.setOnDismissListener(null);
                    OnAutoDismissListener onAutoDismissListener = mDialogConfig.getOnAutoDismissListener();
                    if (onAutoDismissListener != null) {
                        onAutoDismissListener.onAutoDismiss(DialogImpl.this, getContentView());
                    }
                    dismiss();
                }
            };
            mCountDown.start();
        }
    }


    public View getContentView() {
        return mContentView;
    }

    public final <T extends View> T findViews(@IdRes int id) {
        if (id == NO_ID || mContentView == null) {
            return null;
        }
        return mContentView.findViewById(id);
    }

    private int getRootWidth() {
        int displayWidth = 0;
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point point = new Point();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            display.getRealSize(point);
            displayWidth = point.x;
        } else {
            DisplayMetrics dm = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
            displayWidth = dm.widthPixels;
        }
        return displayWidth;
    }


    private int getRootHeight() {
        int displayHeight = 0;
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point point = new Point();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            display.getRealSize(point);
            displayHeight = point.y;
        } else {
            DisplayMetrics dm = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
            displayHeight = dm.heightPixels;
        }
        return displayHeight;
    }
}
