package com.inspur.mspeech.ui;

import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import com.inspur.mspeech.R;
import com.inspur.mspeech.utils.PrefersTool;

public class ModelSettingActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_model);
        AppCompatImageView back = findViewById(R.id.back);
        back.setOnClickListener(view -> {
            finish();
        });

        Switch modelSwitch = findViewById(R.id.model_switch);
        if (PrefersTool.getModelSwitch()){
            modelSwitch.setChecked(true);
        }
        modelSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                PrefersTool.setModelSwitch(isChecked);
            }
        });
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