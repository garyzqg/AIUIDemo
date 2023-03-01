package payfun.lib.dialog;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import payfun.lib.dialog.base.BaseDialogBuilder;
import payfun.lib.dialog.base.DialogImpl;
import payfun.lib.dialog.custom.HintDialogBuilder;
import payfun.lib.dialog.custom.WaitDialogBuilder;
import payfun.lib.dialog.listener.OnDialogButtonClickListener;
import payfun.lib.dialog.widget.ProgressView;

/**
 * @author : zhangqg
 * date   : 2023/3/1 15:30
 * desc   : <功能简述>
 */
public class DialogUtil {

    /**
     * 删除确认弹框
     * @param activity
     * @param onBtnClickListener
     * @return
     */
    public static DialogImpl showDeleteDialog(FragmentActivity activity, String content,OnDialogButtonClickListener onBtnClickListener) {
        HintDialogBuilder hintDialogBuilder = new HintDialogBuilder();
        hintDialogBuilder.setContentMsg(content);
        hintDialogBuilder.setOnLeftBtnClickListener(onBtnClickListener);
        FragmentManager supportFragmentManager = activity.getSupportFragmentManager();
        DialogImpl dialog = new DialogImpl(hintDialogBuilder);
        dialog.showNow(supportFragmentManager, "delete");
        return dialog;
    }





    public static DialogImpl showDialog(FragmentActivity activity, BaseDialogBuilder dialogBuilder) {
        FragmentManager supportFragmentManager = activity.getSupportFragmentManager();
        DialogImpl dialog = new DialogImpl(dialogBuilder);
        dialog.showNow(supportFragmentManager, "test");
        return dialog;
    }


    public static DialogImpl showLoading(FragmentActivity activity, String content) {
        WaitDialogBuilder loading = new WaitDialogBuilder();
        loading.setDirection(LinearLayoutCompat.HORIZONTAL);
        loading.setWaitMsg(content);
        loading.setCanceledOnTouchOutside(false);
        return showDialog(activity, loading);
    }


    public static void showDialogTest(FragmentActivity activity) {
        FragmentManager supportFragmentManager = activity.getSupportFragmentManager();
        BaseDialogBuilder dialogBuilder = new BaseDialogBuilder() {

            @Override
            protected int initLayoutId() {
                return R.layout.dialog_waiting;
            }

            @Override
            protected void onInitView(DialogFragment dialog, View view, Bundle savedInstanceState) {
                ProgressView progressView = view.findViewById(R.id.progress);
            }
        };
        DialogImpl dialog = new DialogImpl(dialogBuilder);
        dialog.showNow(supportFragmentManager, "test");
    }
}
