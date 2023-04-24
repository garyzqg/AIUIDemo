package com.inspur.mspeech.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.inspur.mspeech.R;
import com.inspur.mspeech.adapter.VoiceNameAdapter;
import com.inspur.mspeech.audio.AudioTrackOperator;
import com.inspur.mspeech.bean.BaseResponse;
import com.inspur.mspeech.bean.VoiceBean;
import com.inspur.mspeech.net.SpeechNet;
import com.inspur.mspeech.utils.PrefersTool;

import java.util.ArrayList;
import java.util.List;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import io.reactivex.rxjava3.annotations.NonNull;
import payfun.lib.dialog.DialogUtil;
import payfun.lib.dialog.listener.OnDialogButtonClickListener;
import payfun.lib.net.exception.ExceptionEngine;
import payfun.lib.net.exception.NetException;
import payfun.lib.net.rx.BaseObserver;
/**
 * @author : zhangqinggong
 * date    : 2023/2/25 10:20
 * desc    : 音色设置页面
 */
public class VoiceNameSettingActivity extends AppCompatActivity {

    private RecyclerView mRvVoiceName;
    private List<VoiceBean> mVoiceBeanList = new ArrayList<>();
    private VoiceNameAdapter mVoiceNameAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voicename_setting);

        initView();
        getData();
    }

    private void getData() {
        SpeechNet.getVoiceName(new BaseObserver<BaseResponse<List<VoiceBean>>>() {
            @Override
            public void onNext(@NonNull BaseResponse<List<VoiceBean>> response) {
                if (response != null) {
                    if (response.isSuccess()) {
                        List<VoiceBean> voiceBeanList= response.getData();
//                        mVoiceBeanList.addAll(voiceBeanList);
                        // TODO: 2023/2/27 当前暂时只取前5个
                        for (int i = 0; i < (voiceBeanList.size()>5?5:voiceBeanList.size()); i++) {
                            mVoiceBeanList.add(voiceBeanList.get(i));
                        }
                        mVoiceNameAdapter.notifyDataSetChanged();
                    }else {
                        DialogUtil.showErrorDialog(VoiceNameSettingActivity.this,"获取可用音色失败 code = " + response.getCode(),response.getMessage());
                    }
                }


            }

            @Override
            public void onError(@NonNull Throwable e) {
                NetException netException = ExceptionEngine.handleException(e);
                if (TextUtils.equals(netException.getErrorCode(),"401")){//未登录或登录已过期
                    jumpToLogin();
                }else {
                    DialogUtil.showErrorDialog(VoiceNameSettingActivity.this,"获取可用音色失败",netException.getErrorTitle());
                }
            }

        });
    }

    private void jumpToLogin() {
        DialogUtil.showTwoBtnDialog(VoiceNameSettingActivity.this, "请先登录", (OnDialogButtonClickListener) (baseDialog, v) -> {
            PrefersTool.setAccesstoken("");
            Intent intent = new Intent(VoiceNameSettingActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            return false;
        }, (baseDialog, v) -> {
            finish();
            return false;
        });

    }
    public ActivityResultLauncher<Intent> intentActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == RESULT_OK){
            //登录成功
            getData();
        }
    });
    private void initView() {
        mRvVoiceName = findViewById(R.id.rv_voice_name);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mRvVoiceName.setLayoutManager(layoutManager);
        mVoiceNameAdapter = new VoiceNameAdapter(mVoiceBeanList);
        mVoiceNameAdapter.setOnItemClickListener(new VoiceNameAdapter.ItemClickListener() {
            @Override
            public void onClick(int position) {
                mVoiceNameAdapter.notifyDataSetChanged();
                AudioTrackOperator.getInstance().writeSource(VoiceNameSettingActivity.this, "audio/" + PrefersTool.getVoiceName() + "_box_wakeUpReply.pcm",false);

            }
        });
        mRvVoiceName.setAdapter(mVoiceNameAdapter);

        AppCompatImageView back = findViewById(R.id.back);
        back.setOnClickListener(view -> {
            finish();
        });
    }

}