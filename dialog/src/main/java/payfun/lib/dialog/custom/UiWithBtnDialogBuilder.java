package payfun.lib.dialog.custom;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import androidx.fragment.app.DialogFragment;

import payfun.lib.dialog.R;
import payfun.lib.dialog.base.BaseDialogBuilder;
import payfun.lib.dialog.listener.OnDialogButtonClickListener;

/**
 * @author : zhangqg
 * date   : 2023/3/1 15:30
 * desc   : <底部带左右两个按钮的可填充对话框>
 */
public abstract class UiWithBtnDialogBuilder<B extends UiWithBtnDialogBuilder> extends BaseDialogBuilder<B> {

    /**
     * 左侧按钮文案
     */
    private String leftMsg;
    /**
     * 右侧按钮文案
     */
    private String rightMsg;

    private OnDialogButtonClickListener onLeftBtnClickListener;
    private OnDialogButtonClickListener onRightBtnClickListener;


    @Override
    protected int initLayoutId() {
        return R.layout.dialog_ui_with_btn;
    }

    @Override
    protected void onInitView(DialogFragment dialog, View view, Bundle savedInstanceState) {
        FrameLayout flDialogContentView = view.findViewById(R.id.fl_dialog_content);
        Button btnDialogLeft = view.findViewById(R.id.btn_dialog_left);
        Button btnDialogRight = view.findViewById(R.id.btn_dialog_right);

        onDoBeforeViewPerform(dialog, view);

        btnDialogLeft.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                boolean isIntercept = false;
                if (onLeftBtnClickListener != null) {
                    isIntercept = onLeftBtnClickListener.onClick(dialog, btnDialogLeft);
                }
                if (!isIntercept && dialog != null && dialog.isResumed()) {
                    dialog.dismiss();
                }
            }
        });

        btnDialogRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isIntercept = false;
                if (onRightBtnClickListener != null) {
                    isIntercept = onRightBtnClickListener.onClick(dialog, btnDialogRight);
                }
                if (!isIntercept && dialog != null && dialog.isResumed()) {
                    dialog.dismiss();
                }
            }
        });
        if (TextUtils.isEmpty(leftMsg) && TextUtils.isEmpty(rightMsg)) {
            btnDialogLeft.setVisibility(View.VISIBLE);
            btnDialogRight.setVisibility(View.VISIBLE);
        } else {
            useMsg(btnDialogLeft, leftMsg);
            useMsg(btnDialogRight, rightMsg);
        }

        int childLayoutId = getChildLayoutId();
        View childView = LayoutInflater.from(flDialogContentView.getContext()).inflate(childLayoutId, flDialogContentView, true);
        onInitChildView(dialog, childView, savedInstanceState);


    }

    /**
     * 在View初始化后提前做一些操作
     */
    public void onDoBeforeViewPerform(DialogFragment dialog, View view) {
    }

    /**
     * 获取子布局id
     *
     * @return id
     */
    public abstract int getChildLayoutId();

    /**
     * 父布局初始化完成，子布局初始化
     *
     * @param dialog             界面
     * @param view               界面
     * @param savedInstanceState 状态
     */
    public abstract void onInitChildView(DialogFragment dialog, View view, Bundle savedInstanceState);


    //region set方法

    public B setLeftMsg(String leftMsg) {
        this.leftMsg = leftMsg;
        return (B) this;
    }

    public B setRightMsg(String rightMsg) {
        this.rightMsg = rightMsg;
        return (B) this;
    }


    public B setOnLeftBtnClickListener(OnDialogButtonClickListener onBtnClickListener) {
        this.onLeftBtnClickListener = onBtnClickListener;
        return (B) this;
    }

    public B setOnRightBtnClickListener(OnDialogButtonClickListener onBtnClickListener) {
        this.onRightBtnClickListener = onBtnClickListener;
        return (B) this;
    }

    //endregion set方法

    //region get方法

    public String getLeftMsg() {
        return leftMsg;
    }

    public String getRightMsg() {
        return rightMsg;
    }

    public OnDialogButtonClickListener getOnLeftBtnClickListener() {
        return onLeftBtnClickListener;
    }

    public OnDialogButtonClickListener getOnRightBtnClickListener() {
        return onRightBtnClickListener;
    }


    //endregion get方法

}
