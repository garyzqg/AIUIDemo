package com.inspur.mspeech.bean;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

/**
 * @author : zhangqinggong
 * date    : 2023/2/27 16:31
 * desc    :
 */
public class QaBean implements Parcelable {
    private String qaId;
    private String questionId;
    private String questionTxt;
    private String answerId;
    private String answerTxt;
    private int standard;
    private List<QaBean> questionDataList;
    private List<QaBean> answerDataList;

    protected QaBean(Parcel in) {
        qaId = in.readString();
        questionId = in.readString();
        questionTxt = in.readString();
        answerId = in.readString();
        answerTxt = in.readString();
        standard = in.readInt();
        questionDataList = in.createTypedArrayList(QaBean.CREATOR);
        answerDataList = in.createTypedArrayList(QaBean.CREATOR);
    }

    public static final Creator<QaBean> CREATOR = new Creator<QaBean>() {
        @Override
        public QaBean createFromParcel(Parcel in) {
            return new QaBean(in);
        }

        @Override
        public QaBean[] newArray(int size) {
            return new QaBean[size];
        }
    };

    public int getStandard() {
        return standard;
    }

    public void setStandard(int standard) {
        this.standard = standard;
    }

    public List<QaBean> getQuestionDataList() {
        return questionDataList;
    }

    public void setQuestionDataList(List<QaBean> questionDataList) {
        this.questionDataList = questionDataList;
    }

    public List<QaBean> getAnswerDataList() {
        return answerDataList;
    }

    public void setAnswerDataList(List<QaBean> answerDataList) {
        this.answerDataList = answerDataList;
    }

    public String getQaId() {
        return qaId;
    }

    public void setQaId(String qaId) {
        this.qaId = qaId;
    }

    public String getQuestionId() {
        return questionId;
    }

    public void setQuestionId(String questionId) {
        this.questionId = questionId;
    }

    public String getQuestionTxt() {
        return questionTxt;
    }

    public void setQuestionTxt(String questionTxt) {
        this.questionTxt = questionTxt;
    }

    public String getAnswerId() {
        return answerId;
    }

    public void setAnswerId(String answerId) {
        this.answerId = answerId;
    }

    public String getAnswerTxt() {
        return answerTxt;
    }

    public void setAnswerTxt(String answerTxt) {
        this.answerTxt = answerTxt;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(qaId);
        parcel.writeString(questionId);
        parcel.writeString(questionTxt);
        parcel.writeString(answerId);
        parcel.writeString(answerTxt);
        parcel.writeInt(standard);
        parcel.writeTypedList(questionDataList);
        parcel.writeTypedList(answerDataList);
    }
}
