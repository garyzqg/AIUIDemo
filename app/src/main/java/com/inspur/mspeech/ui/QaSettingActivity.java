package com.inspur.mspeech.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import com.inspur.mspeech.R;
import com.inspur.mspeech.adapter.QaAdapter;
import com.inspur.mspeech.bean.BaseResponse;
import com.inspur.mspeech.bean.QaBean;
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
 * date    : 2023/2/27 14:28
 * desc    : 问答设置页面
 */
public class QaSettingActivity extends AppCompatActivity {
    private RecyclerView mRvQa;
    private List<QaBean> mQaBeanList = new ArrayList<>();
    private QaAdapter mQaAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qa_setting);

        initView();

        initData();

    }

    private void initView() {
        mRvQa = findViewById(R.id.rv_qa);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mRvQa.setLayoutManager(layoutManager);
        mQaAdapter = new QaAdapter(mQaBeanList);
        mRvQa.setAdapter(mQaAdapter);

        mQaAdapter.setOnItemClickListener(new QaAdapter.ItemClickListener() {
            @Override
            public void onClick(int position) {
                //修改问答
                Intent intent = new Intent(QaSettingActivity.this, QaEditActivity.class);
                intent.putExtra("qaDetail",mQaBeanList.get(position));
                intentActivityResultLauncher.launch(intent);
            }
        });
        AppCompatImageView addQa = findViewById(R.id.iv_add_qa);
        addQa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //新增问答集
                Intent intent = new Intent(QaSettingActivity.this, QaAddActivity.class);
                intentActivityResultLauncher.launch(intent);
            }
        });
    }

    public ActivityResultLauncher<Intent> intentActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),result -> {
        //更新数据
        if (result.getResultCode() == RESULT_OK){
            initData();
        }
    });

    private void jumpToLogin() {
        DialogUtil.showTwoBtnDialog(QaSettingActivity.this, "请先登录", (OnDialogButtonClickListener) (baseDialog, v) -> {
            PrefersTool.setAccesstoken("");
            Intent intent = new Intent(QaSettingActivity.this, LoginActivity.class);
            intentActivityResultLauncher.launch(intent);
            return false;
        }, (baseDialog, v) -> {
            finish();
            return false;
        });

    }

    private void initData() {
        SpeechNet.getQa(new BaseObserver<BaseResponse<List<QaBean>>>() {
            @Override
            public void onNext(@NonNull BaseResponse<List<QaBean>> response) {
                if (response.isSuccess()){
                    if (mQaBeanList.size()>0){
                        mQaBeanList.clear();
                    }
                    mQaBeanList.addAll(response.getData());
                    mQaAdapter.notifyDataSetChanged();
                }else {
                    DialogUtil.showErrorDialog(QaSettingActivity.this,"获取问答集失败 code = " + response.getCode(),response.getMessage());
                }
            }

            @Override
            public void onError(@NonNull Throwable e) {
                NetException netException = ExceptionEngine.handleException(e);
                if (TextUtils.equals(netException.getErrorCode(),"401")){//未登录或登录已过期
                    jumpToLogin();
                }else {
                    DialogUtil.showErrorDialog(QaSettingActivity.this,"获取问答集失败",netException.getErrorTitle());
                }
            }
        });
    }
}