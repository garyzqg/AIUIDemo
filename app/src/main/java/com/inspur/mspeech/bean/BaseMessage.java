package com.inspur.mspeech.bean;

/**
 * @author : zhangqinggong
 * date    : 2023/1/13 15:30
 * desc    :
 */
public class BaseMessage<T> {
    private T data;

    private String type;

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "BaseMessage{" +
                "data=" + data +
                ", type='" + type + '\'' +
                '}';
    }
}
