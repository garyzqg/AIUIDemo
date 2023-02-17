package com.example.aiuidemo.bean;

/**
 * @author : zhangqinggong
 * date    : 2023/1/13 15:27
 * desc    : TtsBean
 */
public class TtsBean {
    //{"type": "tts","data": {"is_finish": true,"audio": ""}}
    private boolean is_finish;

    private String audio;

    public boolean isIs_finish() {
        return is_finish;
    }

    public void setIs_finish(boolean is_finish) {
        this.is_finish = is_finish;
    }

    public String getAudio() {
        return audio;
    }

    public void setAudio(String audio) {
        this.audio = audio;
    }

    @Override
    public String toString() {
        return "TtsBean{" +
                "is_finish=" + is_finish +
                ", audio='" + audio + '\'' +
                '}';
    }
}
