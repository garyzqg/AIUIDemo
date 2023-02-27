package com.inspur.mspeech.net;

import io.reactivex.rxjava3.core.Observable;
import okhttp3.ResponseBody;
import retrofit2.http.GET;

/**
 * @author : zhangqinggong
 * date    : 2023/2/24 17:24
 * desc    : 接口管理类
 */
public interface SpeechServer {

    //获取音色
    @GET("/bot/service/mmip/v2/tts/voice")
    Observable<ResponseBody> getVoiceName();
}
