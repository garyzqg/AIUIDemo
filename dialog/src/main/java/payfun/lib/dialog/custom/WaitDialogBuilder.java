package payfun.lib.dialog.custom;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import payfun.lib.dialog.R;
import payfun.lib.dialog.base.BaseDialogBuilder;
import payfun.lib.dialog.widget.ProgressView;

/**
 * @author : 时光
 * e-mail : qurongzhen@pay.media
 * date   : 2022/5/20 14:30
 * desc   : <等待进度条对话框，可设置提示语和主题>
 */
public class WaitDialogBuilder extends BaseDialogBuilder<WaitDialogBuilder> {
    private ThemeAction theme = ThemeAction.LIGHT;
    /**
     * 等待对话框中的布局方向，目前只有（LinearLayoutCompat.HORIZONTAL=从左到右）和（LinearLayoutCompat.VERTICAL=从上到下）两种
     */
    private int direction = LinearLayout.VERTICAL;

    /**
     * 等待的提示文案
     */
    private String waitMsg;
    private TextView tvWaitMsg;

    @Override
    protected int initLayoutId() {
        return R.layout.dialog_waiting;
    }

    @Override
    protected void onInitView(DialogFragment dialog, View view, Bundle savedInstanceState) {
        CardView cardView = (CardView) view;
        LinearLayout boxBody = view.findViewById(R.id.box_body);
        ProgressView progressView = view.findViewById(R.id.progress);
        tvWaitMsg = view.findViewById(R.id.tv_wait_message);
        int cardBackgroundColor;
        int progressBarColor;
        int msgColor;
        if (theme == ThemeAction.DARK) {
            cardBackgroundColor = ContextCompat.getColor(cardView.getContext(), R.color.cardview_dark_background);
            progressBarColor = ContextCompat.getColor(cardView.getContext(), android.R.color.white);
            msgColor = progressBarColor;
        } else {
            cardBackgroundColor = ContextCompat.getColor(cardView.getContext(), R.color.cardview_light_background);
            progressBarColor = ContextCompat.getColor(cardView.getContext(), android.R.color.darker_gray);
            msgColor = ContextCompat.getColor(cardView.getContext(), android.R.color.black);
        }


        cardView.setCardBackgroundColor(cardBackgroundColor);
        progressView.setBarColor(progressBarColor);
        tvWaitMsg.setTextColor(msgColor);
        boxBody.setOrientation(direction);
        setWaitMsg(waitMsg);
    }

    //region set方法

    public WaitDialogBuilder setDirection(@LinearLayoutCompat.OrientationMode int orientation) {
        this.direction = orientation;
        return this;
    }

    public WaitDialogBuilder setTheme(ThemeAction theme) {
        this.theme = theme;
        return this;
    }

    public WaitDialogBuilder setWaitMsg(String msg) {
        this.waitMsg = msg;
        if (tvWaitMsg != null) {
            if (TextUtils.isEmpty(waitMsg)) {
                tvWaitMsg.setText("");
                tvWaitMsg.setVisibility(View.GONE);
            } else {
                tvWaitMsg.setText(waitMsg);
                tvWaitMsg.setVisibility(View.VISIBLE);
            }
        }
        return this;
    }


    //endregion set方法

    //region get方法

    public int getDirection() {
        return direction;
    }

    public String getWaitMsg() {
        return waitMsg;
    }

    public ThemeAction getTheme() {
        return theme;
    }

    //endregion get方法
}
