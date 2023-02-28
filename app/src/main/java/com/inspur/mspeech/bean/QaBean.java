package com.inspur.mspeech.bean;

/**
 * @author : zhangqinggong
 * date    : 2023/2/27 16:31
 * desc    :
 */
public class QaBean {
    private String qaId;
    private String questionId;
    private String questionTxt;
    private String answerId;
    private String answerTxt;

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

}
