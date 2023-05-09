package com.inspur.mspeech.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.Switch;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;

import com.inspur.mspeech.R;
import com.inspur.mspeech.utils.PrefersTool;

import retrofit2.Call;

public class SettingActivity extends AppCompatActivity implements View.OnClickListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        findViewById(R.id.back).setOnClickListener(this);
        findViewById(R.id.rl_voicename_setting).setOnClickListener(this);
        findViewById(R.id.rl_qa_setting).setOnClickListener(this);

        Switch modelSwitch = findViewById(R.id.model_switch);
        if (!PrefersTool.getModelSwitch()){
            modelSwitch.setChecked(false);
        }
        modelSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                PrefersTool.setModelSwitch(isChecked);
            }
        });
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        switch (v.getId()){
            case R.id.back:
                finish();
                break;
            case R.id.rl_voicename_setting:
                intent.setClass(SettingActivity.this, VoiceNameSettingActivity.class);
                startActivity(intent);
                break;
            case R.id.rl_qa_setting:
                intent.setClass(SettingActivity.this, QaSettingActivity.class);
                startActivity(intent);
                break;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}