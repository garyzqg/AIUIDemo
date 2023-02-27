package com.inspur.mspeech.ui;

import android.os.Bundle;

import com.inspur.mspeech.R;
import com.inspur.mspeech.adapter.VoiceNameAdapter;
import com.inspur.mspeech.bean.VoiceBean;
import com.inspur.mspeech.net.SpeechNet;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import io.reactivex.rxjava3.annotations.NonNull;
import okhttp3.ResponseBody;
import payfun.lib.basis.utils.LogUtil;
import payfun.lib.net.rx.BaseObserver;

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
        SpeechNet.getVoiceName(new BaseObserver<ResponseBody>() {
            @Override
            public void onNext(@NonNull ResponseBody responseBody) {
                try {
                    String string = responseBody.string();
                    JSONObject dataJsonObject = new JSONObject(string);
                    JSONArray dataJsonArray = dataJsonObject.optJSONArray("data");

                    for (int i = 0; i < dataJsonArray.length(); i++) {
                        JSONObject data = dataJsonArray.optJSONObject(i);
                        String voiceId = data.optString("voiceId");
                        String voiceName = data.optString("voiceName");
                        String voiceAlias = data.optString("voiceAlias");
                        VoiceBean voiceBean = new VoiceBean(voiceId, voiceName, voiceAlias);
                        mVoiceBeanList.add(voiceBean);
                    }

                    mVoiceNameAdapter.notifyDataSetChanged();

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(@NonNull Throwable e) {
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