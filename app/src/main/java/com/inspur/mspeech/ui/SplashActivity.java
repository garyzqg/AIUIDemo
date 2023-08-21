package com.inspur.mspeech.ui;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.inspur.mspeech.R;
import com.inspur.mspeech.utils.UIHelper;

import payfun.lib.basis.utils.ToastUtil;

public class SplashActivity extends AppCompatActivity {
    private static final String TAG = "SplashActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        UIHelper.hideBottomUIMenu(this);

        initView();
    }


    private void initView() {

    }


    public void clickItem(View view){
        ToastUtil.showLong(view.getTag().toString());
    }
}