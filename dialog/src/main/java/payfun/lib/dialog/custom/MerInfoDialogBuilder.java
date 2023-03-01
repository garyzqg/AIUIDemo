package payfun.lib.dialog.custom;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;

import payfun.lib.dialog.R;
import payfun.lib.dialog.base.TextInfo;

/**
 * @author : 时光
 * e-mail : qurongzhen@pay.media
 * date   : 2022/5/20 18:08
 * desc   : <p>商户信息对话框
 */
public class MerInfoDialogBuilder extends UiWithBtnDialogBuilder<MerInfoDialogBuilder> {


    private String merName;
    private TextInfo merNameInfo;

    private String merId;
    private TextInfo merIdInfo;


    @Override
    public int getChildLayoutId() {
        return R.layout.dialog_mer_info;
    }

    @Override
    public void onInitChildView(DialogFragment dialog, View view, Bundle savedInstanceState) {
        TextView tvMerName = view.findViewById(R.id.tv_mer_name);
        TextView tvMerId = view.findViewById(R.id.tv_mer_id);

        useTextInfo(tvMerId, merNameInfo);
        useTextInfo(tvMerName, merIdInfo);

    }


    //region set方法


    public MerInfoDialogBuilder setMerName(String merName) {
        this.merName = merName;
        return this;
    }

    public MerInfoDialogBuilder setMerId(String merId) {
        this.merId = merId;
        return this;
    }

    public MerInfoDialogBuilder setMerNameInfo(TextInfo merNameInfo) {
        this.merNameInfo = merNameInfo;
        return this;
    }

    public MerInfoDialogBuilder setMerIdInfo(TextInfo merIdInfo) {
        this.merIdInfo = merIdInfo;
        return this;
    }

    //endregion set方法

    //region get方法

    public String getMerName() {
        return merName;
    }

    public String getMerId() {
        return merId;
    }

    public TextInfo getMerNameInfo() {
        return merNameInfo;
    }

    public TextInfo getMerIdInfo() {
        return merIdInfo;
    }


    //endregion get方法


}
