package com.inspur.mspeech.bean;

/**
 * @author : zhangqinggong
 * date    : 2023/3/27 15:21
 * desc    : vad消息 bean
 */
public class VadMsgBean {
    /**
     * {"status":"ok","type":"partial_result","nbest":"[{\"sentence\":\"在接收术据\"}]"}
     * {"status":"ok","type":"final_result","nbest":"[{\"sentence\":\"在接收复这\",\"word_pieces\":[{\"word\":\"在\",\"start\":100,\"end\":200},{\"word\":\"接\",\"start\":340,\"end\":440},{\"word\":\"收\",\"start\":660,\"end\":760},{\"word\":\"复\",\"start\":900,\"end\":1000},{\"word\":\"这\",\"start\":1180,\"end\":1280}]}]"}
     */
    private String sentence;

    public String getSentence() {
        return sentence;
    }

    public void setSentence(String sentence) {
        this.sentence = sentence;
    }
}
