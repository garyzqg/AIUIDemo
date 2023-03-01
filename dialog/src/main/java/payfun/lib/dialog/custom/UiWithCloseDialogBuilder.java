package payfun.lib.dialog.custom;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.fragment.app.DialogFragment;

import payfun.lib.dialog.R;
import payfun.lib.dialog.base.BaseDialogBuilder;
import payfun.lib.dialog.listener.OnDialogButtonClickListener;

/**
 * @author : zhangqg
 * date   : 2023/3/1 15:30
 * desc   : <右上角带有关闭按钮的对话框>
 */
public abstract class UiWithCloseDialogBuilder<B extends UiWithCloseDialogBuilder> extends BaseDialogBuilder<B> {


    private OnDialogButtonClickListener onCloseBtnClickListener;


    @Override
    protected int initLayoutId() {
        return R.layout.dialog_ui_with_close;
    }

    @Override
    protected void onInitView(DialogFragment dialog, View view, Bundle savedInstanceState) {
        FrameLayout flDialogContentView = view.findViewById(R.id.fl_dialog_content);
        ImageView ivDialogClose = view.findViewById(R.id.iv_dialog_close);
        onDoBeforeViewPerform(dialog, view);

        ivDialogClose.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                boolean isIntercept = false;
                if (onCloseBtnClickListener != null) {
                    isIntercept = onCloseBtnClickListener.onClick(dialog, ivDialogClose);
                }
                if (!isIntercept && dialog != null && dialog.isResumed()) {
                    dialog.dismiss();
                }
            }
        });

        int childLayoutId = getChildLayoutId();
        View childView = LayoutInflater.from(flDialogContentView.getContext()).inflate(childLayoutId, flDialogContentView, true);
        onInitChildView(dialog, childView, savedInstanceState);
    }

    /**
     * 在View初始化后提前做一些操作
     */
    public void onDoBeforeViewPerform(DialogFragment dialog, View view) {
    }

    public abstract int getChildLayoutId();

    public abstract void onInitChildView(DialogFragment dialog, View view, Bundle savedInstanceState);


    //region set方法

    public B setOnCloseBtnClickListener(OnDialogButtonClickListener onBtnClickListener) {
        this.onCloseBtnClickListener = onBtnClickListener;
        return (B) this;
    }


    //endregion set方法

    //region get方法


    //endregion get方法

}
