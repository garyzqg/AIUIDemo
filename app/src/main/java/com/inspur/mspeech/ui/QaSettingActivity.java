package com.inspur.mspeech.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.inspur.mspeech.R;
import com.inspur.mspeech.adapter.QaAdapter;
import com.inspur.mspeech.bean.BaseResponse;
import com.inspur.mspeech.bean.QaBean;
import com.inspur.mspeech.net.SpeechNet;

import java.util.ArrayList;
import java.util.List;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import io.reactivex.rxjava3.annotations.NonNull;
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
                }
            }

            @Override
            public void onError(@NonNull Throwable e) {

            }
        });
    }
}