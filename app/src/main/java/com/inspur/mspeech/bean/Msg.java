package com.inspur.mspeech.bean;

/**
 * @author : zhangqinggong
 * date    : 2023/2/6 16:53
 * desc    : 消息实体
 */
public class Msg {
    public static final int TYPE_RECEIVED = 0;//收到的消息
    public static final int TYPE_SEND = 1;//发出的消息
    private String content;
    private int type;

    public Msg(String content, int type) {
        this.content = content;
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public int getType() {
        return type;
    }
}
