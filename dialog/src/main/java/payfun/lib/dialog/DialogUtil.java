package payfun.lib.dialog;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import payfun.lib.dialog.base.BaseDialogBuilder;
import payfun.lib.dialog.base.DialogImpl;
import payfun.lib.dialog.custom.WaitDialogBuilder;
import payfun.lib.dialog.widget.ProgressView;

/**
 * @author : zhangqg
 * date   : 2023/3/1 15:30
 * desc   : <功能简述>
 */
public class DialogUtil {


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
