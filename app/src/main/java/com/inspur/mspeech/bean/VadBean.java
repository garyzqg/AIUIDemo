package com.inspur.mspeech.bean;

/**
 * @author : zhangqinggong
 * date    : 2023/3/27 15:21
 * desc    : vad bean
 */
public class VadBean {
    /**
     * {"status":"ok","type":"partial_result","nbest":"[{\"sentence\":\"在接收术据\"}]"}
     */

    private String status;
    private String type;
    private String nbest;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getNbest() {
        return nbest;
    }

    public void setNbest(String nbest) {
        this.nbest = nbest;
    }


}
