package com.inspur.mspeech.ui;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;

import com.inspur.mspeech.R;
import com.inspur.mspeech.bean.BaseResponse;
import com.inspur.mspeech.net.SpeechNet;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatTextView;
import io.reactivex.rxjava3.annotations.NonNull;
import payfun.lib.basis.utils.ToastUtil;
import payfun.lib.net.rx.BaseObserver;

public class QaAddActivity extends AppCompatActivity {

    private AppCompatTextView mTvCancel;
    private AppCompatTextView mTvConfirm;
    private AppCompatEditText mEtAddQuestion;
    private AppCompatEditText mEtAddAnswer;
    private String mQuestionText;
    private String mAnswerText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qa_add);

        initView();

    }

    private void initView() {
        mTvCancel = findViewById(R.id.tv_cancel);
        mTvConfirm = findViewById(R.id.tv_confirm);
        mEtAddQuestion = findViewById(R.id.et_add_question);
        mEtAddAnswer = findViewById(R.id.et_add_answer);

        mEtAddQuestion.requestFocus();
        mTvConfirm.setEnabled(false);

        mTvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        mTvConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SpeechNet.saveQa(mQuestionText, mAnswerText, new BaseObserver<BaseResponse>() {
                    @Override
                    public void onNext(@NonNull BaseResponse response) {
                        if (response.isSuccess()){
                            setResult(RESULT_OK);
                            finish();
                        }else {
                            ToastUtil.showLong("创建问答集失败");
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {

                    }
                });
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

    private void changeConfirmStatus() {
        if (!TextUtils.isEmpty(mQuestionText) && !TextUtils.isEmpty(mAnswerText)){
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