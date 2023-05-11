package com.inspur.mspeech.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;

import com.inspur.mspeech.R;
import com.inspur.mspeech.adapter.VoiceNameAdapter;
import com.inspur.mspeech.adapter.VoiceStyleAdapter;
import com.inspur.mspeech.audio.AudioTrackOperator;
import com.inspur.mspeech.bean.BaseResponse;
import com.inspur.mspeech.bean.VoiceBean;
import com.inspur.mspeech.bean.VoiceStyleBean;
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
    private RecyclerView mRvVoiceStyle;
    private List<VoiceBean> mVoiceBeanList = new ArrayList<>();
    private List<VoiceStyleBean> mVoiceStyleList = new ArrayList<>();
    private VoiceNameAdapter mVoiceNameAdapter;
    private VoiceStyleAdapter mVoiceStyleAdapter;
    private ProgressBar progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voicename_setting);

        initView();
        getData();
    }

    private void getData() {
        progress.setVisibility(View.VISIBLE);
        SpeechNet.getVoiceName(new BaseObserver<BaseResponse<List<VoiceBean>>>() {
            @Override
            public void onNext(@NonNull BaseResponse<List<VoiceBean>> response) {
                progress.setVisibility(View.GONE);
                if (response != null) {
                    if (response.isSuccess()) {
                        List<VoiceBean> voiceBeanList= response.getData();
//                        mVoiceBeanList.addAll(voiceBeanList);
                        // TODO: 2023/2/27 当前暂时只取前5个
                        for (int i = 0; i < (voiceBeanList.size()>5?5:voiceBeanList.size()); i++) {
                            VoiceBean voiceBean = voiceBeanList.get(i);
                            mVoiceBeanList.add(voiceBean);

                            //取当前选择发言人的所有说话风格
                            if (voiceBean.getVoiceName().equals(PrefersTool.getVoiceName())){
                                mVoiceStyleList.addAll(voiceBean.getVoiceStyleList());
                                mVoiceStyleAdapter.notifyDataSetChanged();
                            }
                        }
                        mVoiceNameAdapter.notifyDataSetChanged();


                    }else {
                        DialogUtil.showErrorDialog(VoiceNameSettingActivity.this,"获取可用音色失败 code = " + response.getCode(),response.getMessage());
                    }
                }


            }

            @Override
            public void onError(@NonNull Throwable e) {
                progress.setVisibility(View.GONE);
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
            intentActivityResultLauncher.launch(intent);
            return false;
        }, (baseDialog, v) -> {
            finish();
            return false;
        });

    }
    public ActivityResultLauncher<Intent> intentActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        getData();
    });
    private void initView() {
        //发言人
        mRvVoiceName = findViewById(R.id.rv_voice_name);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mRvVoiceName.setLayoutManager(layoutManager);
        mVoiceNameAdapter = new VoiceNameAdapter(mVoiceBeanList);
        mVoiceNameAdapter.setOnItemClickListener(new VoiceNameAdapter.ItemClickListener() {
            @Override
            public void onClick(int position) {
                mVoiceNameAdapter.notifyDataSetChanged();
                AudioTrackOperator.getInstance().writeSource(VoiceNameSettingActivity.this, "audio/" + PrefersTool.getVoiceName() + "_box_wakeUpReply.pcm",false);

                //选择发言人后更新说话风格列表
                List<VoiceStyleBean> voiceStyleList = mVoiceBeanList.get(position).getVoiceStyleList();
                PrefersTool.setVoiceStyle("general");
                mVoiceStyleList.clear();
                mVoiceStyleList.addAll(voiceStyleList);
                mVoiceStyleAdapter.notifyDataSetChanged();
            }
        });
        mRvVoiceName.setAdapter(mVoiceNameAdapter);

        //说话风格
        mRvVoiceStyle = findViewById(R.id.rv_voice_style);
        LinearLayoutManager layoutManager2 = new LinearLayoutManager(this);
        mRvVoiceStyle.setLayoutManager(layoutManager2);
        mVoiceStyleAdapter = new VoiceStyleAdapter(mVoiceStyleList);
        mVoiceStyleAdapter.setOnItemClickListener(new VoiceStyleAdapter.ItemClickListener() {
            @Override
            public void onClick(int position) {
                mVoiceStyleAdapter.notifyDataSetChanged();
            }
        });
        mRvVoiceStyle.setAdapter(mVoiceStyleAdapter);


        AppCompatImageView back = findViewById(R.id.back);
        back.setOnClickListener(view -> {
            finish();
        });

        progress = findViewById(R.id.progress);
    }

}