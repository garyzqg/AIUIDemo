package com.inspur.mspeech.bean;

/**
 * @author : zhangqinggong
 * date    : 2023/2/27 15:44
 * desc    : base response bean
 */
public class BaseResponse<T> {
    private int code;
    private String message;
    private T data;


    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public boolean isSuccess() {
        return code == 200;
    }
}
