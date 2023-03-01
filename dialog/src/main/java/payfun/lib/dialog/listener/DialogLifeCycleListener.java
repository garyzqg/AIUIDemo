package payfun.lib.dialog.listener;


import androidx.fragment.app.DialogFragment;

public interface DialogLifeCycleListener {

    void onCreate(DialogFragment dialog);

    void onShow(DialogFragment dialog);

    void onDismiss(DialogFragment dialog);

}
