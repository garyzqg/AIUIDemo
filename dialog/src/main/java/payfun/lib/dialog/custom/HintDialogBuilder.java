package payfun.lib.dialog.custom;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;

import payfun.lib.dialog.R;
import payfun.lib.dialog.base.TextInfo;

/**
 * @author : 时光
 * e-mail : qurongzhen@pay.media
 * date   : 2022/5/20 18:08
 * desc   : <p>提示对话框
 */
public class HintDialogBuilder extends UiWithBtnDialogBuilder<HintDialogBuilder> {


    /**
     * 标题
     */
    private String titleMsg;
    private TextInfo titleInfo;
    /**
     * 内容
     */
    private String contentMsg;
    private TextInfo contentInfo;
    /**
     * 细节
     */
    private String detailMsg;
    private TextInfo detailInfo;


    @Override
    public int getChildLayoutId() {
        return R.layout.dialog_tip;
    }

    @Override
    public void onInitChildView(DialogFragment dialog, View view, Bundle savedInstanceState) {
        TextView tvTipTitle = view.findViewById(R.id.tv_tip_title);
        TextView tvTipContent = view.findViewById(R.id.tv_tip_content);
        TextView tvTipDetail = view.findViewById(R.id.tv_tip_detail);

        useTextInfo(tvTipTitle, titleInfo);
        useTextInfo(tvTipContent, contentInfo);
        useTextInfo(tvTipDetail, detailInfo);

        useMsg(tvTipTitle, titleMsg);
        useMsg(tvTipContent, contentMsg);

        tvTipDetail.setVisibility(View.GONE);
        tvTipDetail.setText(detailMsg);

        tvTipContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tvTipDetail.getVisibility() == View.VISIBLE) {
                    tvTipDetail.setVisibility(View.GONE);
                } else {
                    if (!TextUtils.isEmpty(detailMsg)) {
                        tvTipDetail.setVisibility(View.VISIBLE);
                    }
                }
            }
        });
    }


    //region set方法


    public HintDialogBuilder setTitleMsg(String titleMsg) {
        this.titleMsg = titleMsg;
        return this;
    }

    public HintDialogBuilder setContentMsg(String contentMsg) {
        this.contentMsg = contentMsg;
        return this;
    }

    public HintDialogBuilder setDetailMsg(String detailMsg) {
        this.detailMsg = detailMsg;
        return this;
    }

    public HintDialogBuilder setTitleInfo(TextInfo titleInfo) {
        this.titleInfo = titleInfo;
        return this;
    }

    public HintDialogBuilder setContentInfo(TextInfo contentInfo) {
        this.contentInfo = contentInfo;
        return this;
    }

    public HintDialogBuilder setDetailInfo(TextInfo detailInfo) {
        this.detailInfo = detailInfo;
        return this;
    }

    //endregion set方法

    //region get方法

    public String getTitleMsg() {
        return titleMsg;
    }

    public String getContentMsg() {
        return contentMsg;
    }

    public String getDetailMsg() {
        return detailMsg;
    }

    public TextInfo getTitleInfo() {
        return titleInfo;
    }

    public TextInfo getContentInfo() {
        return contentInfo;
    }

    public TextInfo getDetailInfo() {
        return detailInfo;
    }


    //endregion get方法


}
