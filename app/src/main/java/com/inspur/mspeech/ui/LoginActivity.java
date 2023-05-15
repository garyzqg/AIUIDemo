package com.inspur.mspeech.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.inspur.mspeech.R;
import com.inspur.mspeech.bean.BaseResponse;
import com.inspur.mspeech.bean.LoginBean;
import com.inspur.mspeech.bean.SceneBean;
import com.inspur.mspeech.net.SpeechNet;
import com.inspur.mspeech.utils.PrefersTool;

import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;
import io.reactivex.rxjava3.annotations.NonNull;
import payfun.lib.basis.utils.ToastUtil;
import payfun.lib.dialog.DialogUtil;
import payfun.lib.net.exception.ExceptionEngine;
import payfun.lib.net.exception.NetException;
import payfun.lib.net.rx.BaseObserver;

public class LoginActivity extends AppCompatActivity {

    private AppCompatEditText mEtUsername;
    private AppCompatEditText mEtPwd;
    private TextView login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initView();
    }

    private void initView() {

        mEtUsername = findViewById(R.id.et_username);
        if (TextUtils.isEmpty(PrefersTool.getAccesstoken())){//未登录状态
            String userNameSave = PrefersTool.getUserName();
            if (!TextUtils.isEmpty(userNameSave)){
                mEtUsername.setText(userNameSave);
            }
            mEtPwd = findViewById(R.id.et_pwd);
            login = findViewById(R.id.login);
            login.setOnClickListener( view -> {
                String userName = mEtUsername.getText().toString().trim();
                String pwd = mEtPwd.getText().toString().trim();
                if (TextUtils.isEmpty(userName)){
                    ToastUtil.showLong("请输入账号");
                    return;
                }
                if (TextUtils.isEmpty(pwd)){
                    ToastUtil.showLong("请输入密码");
                    return;
                }

                login(userName, pwd);
            });
        }else {//已登录状态
            LinearLayoutCompat llLogIn = findViewById(R.id.ll_log_in);
            LinearLayoutCompat llLoggedIn = findViewById(R.id.ll_logged_in);
            AppCompatTextView title = findViewById(R.id.title);
            AppCompatTextView tvUserName = findViewById(R.id.tv_userName);
            AppCompatTextView tvScene = findViewById(R.id.tv_scene);
            TextView logout = findViewById(R.id.logout);
            title.setText("已登录");
            llLogIn.setVisibility(View.GONE);
            llLoggedIn.setVisibility(View.VISIBLE);
            tvUserName.setText("账号: "+PrefersTool.getUserName());
            tvScene.setText("情景: "+PrefersTool.getsceneName());
            logout.setOnClickListener(view -> {
                PrefersTool.setAccesstoken("");
                setResult(RESULT_OK);
                finish();
            });
        }

        AppCompatImageView back = findViewById(R.id.back);
        back.setOnClickListener(view -> {
            finish();
        });

    }

    private void login(String userName, String pwd) {
        SpeechNet.login(userName, pwd, new BaseObserver<BaseResponse<LoginBean>>() {
            @Override
            public void onNext(@NonNull BaseResponse<LoginBean> response) {
                if (response.isSuccess()){
                    LoginBean data = response.getData();

                    String accessToken = data.getAccessToken();
                    if (!TextUtils.isEmpty(accessToken)){
                        PrefersTool.setAccesstoken(accessToken);
                        PrefersTool.setUserName(userName);
                        //获取情景id
                        getSceneId(accessToken,userName);
//                            finish();
                    }

                }else {
                    DialogUtil.showErrorDialog(LoginActivity.this,"登录失败",response.getMessage() +  " code:" + response.getCode());
                }
            }

            @Override
            public void onError(@NonNull Throwable e) {
                NetException netException = ExceptionEngine.handleException(e);
                DialogUtil.showErrorDialog(LoginActivity.this,"登录失败 ", netException.getErrorTitle());
            }
        });
    }

    private void getSceneId(String accessToken,String userName) {
        SpeechNet.getSceneId(new BaseObserver<BaseResponse<List<SceneBean>>>() {
            @Override
            public void onNext(@NonNull BaseResponse<List<SceneBean>> response) {
                if (response.isSuccess()){
                    List<SceneBean> data = response.getData();
                    // TODO: 2023/3/16 默认取第一个值
                    if (data!= null && data.size()>0){
                        SceneBean sceneBean = data.get(0);
                        String sceneId = sceneBean.getSceneId();
                        if (!TextUtils.isEmpty(sceneId)){
                            PrefersTool.setSceneId(sceneId);
                            PrefersTool.setSceneName(sceneBean.getSceneName());
                            ToastUtil.showLong("登录成功");

                            setResult(RESULT_OK);
                            finish();
                        }
                    }

                }else {
                    DialogUtil.showErrorDialog(LoginActivity.this,"获取情景失败 code = " + response.getCode(),response.getMessage());
                }
            }

            @Override
            public void onError(@NonNull Throwable e) {
                NetException netException = ExceptionEngine.handleException(e);
                DialogUtil.showErrorDialog(LoginActivity.this,"获取情景失败 ", netException.getErrorTitle());
            }
        });
    }
}