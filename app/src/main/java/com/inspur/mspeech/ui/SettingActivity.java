package com.inspur.mspeech.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.inspur.mspeech.R;
import com.inspur.mspeech.utils.PrefersTool;

public class SettingActivity extends AppCompatActivity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {

    private TextView speedNum;
    private TextView toneNum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        findViewById(R.id.back).setOnClickListener(this);
        findViewById(R.id.rl_voicename_setting).setOnClickListener(this);
        findViewById(R.id.rl_qa_setting).setOnClickListener(this);
        //语速设置
        SeekBar speedAjust = findViewById(R.id.speed_adjust);
        speedAjust.setOnSeekBarChangeListener(this);
        int speed = PrefersTool.getSpeed();
        speedAjust.setProgress(speed);
        speedNum = findViewById(R.id.tv_speed_num);
        speedNum.setText(speed+"");
        //音调设置
        SeekBar toneAjust = findViewById(R.id.tone_adjust);
        toneAjust.setOnSeekBarChangeListener(this);
        int tone = PrefersTool.getTone();
        toneAjust.setProgress(tone);
        toneNum = findViewById(R.id.tv_tone_num);
        toneNum.setText(tone+"");

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

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        int progress = seekBar.getProgress();
        if (seekBar.getId() == R.id.speed_adjust){//语速设置
            PrefersTool.setSpeed(progress);
            speedNum.setText(progress+"");
        }else if (seekBar.getId() == R.id.tone_adjust){//音调设置
            PrefersTool.setTone(progress);
            toneNum.setText(progress+"");
        }
    }
}