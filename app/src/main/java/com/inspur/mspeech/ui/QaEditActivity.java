package com.inspur.mspeech.ui;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;

import com.inspur.mspeech.R;
import com.inspur.mspeech.adapter.QaEditAdapter;
import com.inspur.mspeech.bean.BaseResponse;
import com.inspur.mspeech.bean.QaBean;
import com.inspur.mspeech.net.SpeechNet;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import io.reactivex.rxjava3.annotations.NonNull;
import payfun.lib.basis.utils.ToastUtil;
import payfun.lib.dialog.DialogUtil;
import payfun.lib.dialog.listener.OnDialogButtonClickListener;
import payfun.lib.net.rx.BaseObserver;

public class QaEditActivity extends AppCompatActivity {

    private AppCompatTextView mTvCancel;
    private AppCompatTextView mTvConfirm;
    private AppCompatEditText mEtAddQuestion;
    private AppCompatEditText mEtAddAnswer;
    private Button mBtnDeleteQa;
    private String mQuestionText;
    private String mAnswerText;

    private RecyclerView mRvQuestion;
    private RecyclerView mRvAnswer;
    private List<QaBean> mQuestionList = new ArrayList<>();
    private List<QaBean> mAnswerList = new ArrayList<>();
    private QaEditAdapter mQaQuestionAdapter;
    private QaEditAdapter mQaAnswerAdapter;
    private String mQaId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qa_edit);

        initView();

        getData();

    }

    private void getData() {
        QaBean qaBean = getIntent().getParcelableExtra("qaDetail");
        mQuestionList.addAll(qaBean.getQuestionDataList());
        mAnswerList.addAll(qaBean.getAnswerDataList());
        mQaId = qaBean.getQaId();
        mQaQuestionAdapter.notifyDataSetChanged();
        mQaAnswerAdapter.notifyDataSetChanged();
    }

    private void initView() {
        mTvCancel = findViewById(R.id.tv_cancel);
        mTvConfirm = findViewById(R.id.tv_confirm);
        mEtAddQuestion = findViewById(R.id.et_add_question);
        mEtAddAnswer = findViewById(R.id.et_add_answer);
        mBtnDeleteQa = findViewById(R.id.btn_delete_qa);

        mRvQuestion = findViewById(R.id.rv_question);
        mRvAnswer = findViewById(R.id.rv_answer);
        mQaQuestionAdapter = new QaEditAdapter(mQuestionList);
        mQaAnswerAdapter = new QaEditAdapter(mAnswerList);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        LinearLayoutManager layoutManager2 = new LinearLayoutManager(this);
        mRvQuestion.setLayoutManager(layoutManager);
        mRvAnswer.setLayoutManager(layoutManager2);

        mRvQuestion.setAdapter(mQaQuestionAdapter);
        mRvAnswer.setAdapter(mQaAnswerAdapter);

        mTvConfirm.setEnabled(false);

        mQaQuestionAdapter.setOnItemClickListener(position -> {
            //弹框提示是否删除
            DialogUtil.showDeleteDialog(this, "是否确认删除？",new OnDialogButtonClickListener() {
                @Override
                public boolean onClick(DialogFragment baseDialog, View v) {
                    //确定删除
                    QaBean qaBean = mQuestionList.get(position);
                    SpeechNet.deleteQuestion(qaBean.getQaId(), qaBean.getQuestionId(), new BaseObserver<BaseResponse>() {
                        @Override
                        public void onNext(@NonNull BaseResponse response) {
                            if (response.isSuccess()) {
                                setResult(RESULT_OK);
                                finish();
                            }else {
                                // TODO: 2023/3/1 弹窗
                            }
                        }

                        @Override
                        public void onError(@NonNull Throwable e) {
                                // TODO: 2023/3/1 弹窗
                        }
                    });
                    return false;
                }
            });

        });

        mQaAnswerAdapter.setOnItemClickListener(position -> {
            //弹框提示是否删除
            DialogUtil.showDeleteDialog(this, "是否确认删除？",new OnDialogButtonClickListener() {
                @Override
                public boolean onClick(DialogFragment baseDialog, View v) {
                    //确定删除
                    QaBean qaBean = mAnswerList.get(position);
                    SpeechNet.deleteAnswer(qaBean.getQaId(), qaBean.getAnswerId(), new BaseObserver<BaseResponse>() {
                        @Override
                        public void onNext(@NonNull BaseResponse response) {
                            if (response.isSuccess()){
                                setResult(RESULT_OK);
                                finish();
                            }else {

                            }
                        }

                        @Override
                        public void onError(@NonNull Throwable e) {

                        }
                    });
                    return false;
                }
            });

        });

        mBtnDeleteQa.setOnClickListener(view -> {
            //弹框提示是否删除
            DialogUtil.showDeleteDialog(this, "是否确认删除此问答集？", new OnDialogButtonClickListener() {
                @Override
                public boolean onClick(DialogFragment baseDialog, View v) {
                    //确定删除
                    SpeechNet.deleteQa(mQaId, new BaseObserver<BaseResponse>() {
                        @Override
                        public void onNext(@NonNull BaseResponse response) {
                            if (response.isSuccess()) {
                                setResult(RESULT_OK);
                                finish();
                            } else {
                                ToastUtil.showLong("删除问答集失败");
                            }
                        }

                        @Override
                        public void onError(@NonNull Throwable e) {
                            ToastUtil.showLong("删除问答集失败");
                        }
                    });
                    return false;
                }
            });
        });
        mTvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        mTvConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //问法和回复需要单独调接口 且一次只能添加一条
                if (!TextUtils.isEmpty(mQuestionText)){
                    SpeechNet.saveQuestion(mQaId, mQuestionText, new BaseObserver<BaseResponse>() {
                        @Override
                        public void onNext(@NonNull BaseResponse response) {
                            if (response.isSuccess()){
                                saveAnswer();
                            }else {
                                ToastUtil.showLong("新增问题失败");
                            }
                        }

                        @Override
                        public void onError(@NonNull Throwable e) {
                            ToastUtil.showLong("新增问题失败");
                        }
                    });
                }else {
                    saveAnswer();
                }
            }
        });

        mEtAddQuestion.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                mQuestionText = editable.toString().trim();
                changeConfirmStatus();
            }
        });


        mEtAddAnswer.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                mAnswerText = editable.toString().trim();
                changeConfirmStatus();
            }
        });
    }

    private void saveAnswer() {
        if (!TextUtils.isEmpty(mAnswerText)){
            SpeechNet.saveAnswer(mQaId, mAnswerText, new BaseObserver<BaseResponse>() {
                @Override
                public void onNext(@NonNull BaseResponse response) {
                    if (response.isSuccess()){
                        setResult(RESULT_OK);
                        finish();
                    }else {
                        ToastUtil.showLong("新增答案失败");
                    }
                }

                @Override
                public void onError(@NonNull Throwable e) {
                    ToastUtil.showLong("新增答案失败");
                }
            });
        }else {
            setResult(RESULT_OK);
            finish();
        }
    }

    private void changeConfirmStatus() {
        if (!TextUtils.isEmpty(mQuestionText) || !TextUtils.isEmpty(mAnswerText)){
            if (!mTvConfirm.isEnabled()){
                mTvConfirm.setEnabled(true);
                mTvConfirm.setTextColor(getResources().getColor(R.color.color_msg_bg_2));
            }
        }else {
            if (mTvConfirm.isEnabled()){
                mTvConfirm.setEnabled(false);
                mTvConfirm.setTextColor(getResources().getColor(R.color.color_gray_text));
            }
        }
    }
}