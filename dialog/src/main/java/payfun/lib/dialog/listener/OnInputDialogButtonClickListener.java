package payfun.lib.dialog.listener;

import android.view.View;

import androidx.fragment.app.DialogFragment;


public interface OnInputDialogButtonClickListener {

    boolean onClick(DialogFragment baseDialog, View v, String inputStr);
}
