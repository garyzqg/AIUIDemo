package com.inspur.mspeech.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;

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
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import io.reactivex.rxjava3.annotations.NonNull;
import payfun.lib.basis.utils.LogUtil;
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
    private static final String TAG = "QaSettingActivity";
    private RecyclerView mRvQa;
    private List<QaBean> mQaBeanList = new ArrayList<>();
    private QaAdapter mQaAdapter;
    private LinearLayoutCompat mLlNoData;
    private final int SIZE = 50;
    private int currentPage = 1;
    private int totalSize;
    private ProgressBar progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qa_setting);

        initView();

        getData();

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
        addQa.setOnClickListener(view -> {
            //新增问答集
            Intent intent = new Intent(QaSettingActivity.this, QaAddActivity.class);
            intentActivityResultLauncher.launch(intent);
        });

        AppCompatImageView back = findViewById(R.id.back);
        back.setOnClickListener(view -> {
            finish();
        });

        mLlNoData = findViewById(R.id.ll_no_data);
        progress = findViewById(R.id.progress);

        mRvQa.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@androidx.annotation.NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                //滑动到底部且滑动停止时，canScrollVertically(1) 方法会返回 false，表示已经滑动到底部
                if (!recyclerView.canScrollVertically(1) && newState == RecyclerView.SCROLL_STATE_IDLE) {
                    // 滑动到底部，触发加载更多
                    LogUtil.iTag(TAG,"加载更多 page= " + currentPage);
                    if (currentPage * SIZE < totalSize){
                        currentPage++;
                        getData();
                    }

                }
            }
        });

    }

    public ActivityResultLauncher<Intent> intentActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),result -> {
        currentPage = 1;
        getData();
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

    private void getData() {
        progress.setVisibility(View.VISIBLE);
        SpeechNet.getQa(currentPage,SIZE,new BaseObserver<BaseResponse<List<QaBean>>>() {
            @Override
            public void onNext(@NonNull BaseResponse<List<QaBean>> response) {
                progress.setVisibility(View.GONE);
                if (response.isSuccess()){

                    List<QaBean> data = response.getData();
                    totalSize = response.getTotal();

                    if (currentPage == 1 && mQaBeanList.size()>0){
                        mQaBeanList.clear();
                    }
                    if(data.size()>0){
                        mRvQa.setVisibility(View.VISIBLE);
                        mLlNoData.setVisibility(View.GONE);
                        mQaBeanList.addAll(data);
                        mQaAdapter.notifyDataSetChanged();
                    }else if (currentPage == 1){
                        mRvQa.setVisibility(View.GONE);
                        mLlNoData.setVisibility(View.VISIBLE);
                    }

                }else {
                    DialogUtil.showErrorDialog(QaSettingActivity.this,"获取问答集失败 code = " + response.getCode(),response.getMessage());
                }
            }

            @Override
            public void onError(@NonNull Throwable e) {
                progress.setVisibility(View.GONE);
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