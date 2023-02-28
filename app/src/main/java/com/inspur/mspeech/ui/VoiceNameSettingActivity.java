package com.inspur.mspeech.ui;

import android.os.Bundle;

import com.inspur.mspeech.R;
import com.inspur.mspeech.adapter.VoiceNameAdapter;
import com.inspur.mspeech.bean.BaseResponse;
import com.inspur.mspeech.bean.VoiceBean;
import com.inspur.mspeech.net.SpeechNet;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import io.reactivex.rxjava3.annotations.NonNull;
import payfun.lib.basis.utils.LogUtil;
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
                    }
                }


            }

            @Override
            public void onError(@NonNull Throwable e) {
                e.printStackTrace();
                LogUtil.e(e);
            }

        });
    }

    private void initView() {
        mRvVoiceName = findViewById(R.id.rv_voice_name);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mRvVoiceName.setLayoutManager(layoutManager);
        mVoiceNameAdapter = new VoiceNameAdapter(mVoiceBeanList);
        mVoiceNameAdapter.setOnItemClickListener(new VoiceNameAdapter.ItemClickListener() {
            @Override
            public void onClick(int position) {
                mVoiceNameAdapter.notifyDataSetChanged();
            }
        });
        mRvVoiceName.setAdapter(mVoiceNameAdapter);
    }

}