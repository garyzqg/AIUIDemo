package com.inspur.mspeech.net;

import com.inspur.mspeech.bean.BaseResponse;
import com.inspur.mspeech.bean.VoiceBean;

import java.util.List;

import io.reactivex.rxjava3.core.Observable;
import retrofit2.http.GET;

/**
 * @author : zhangqinggong
 * date    : 2023/2/24 17:24
 * desc    : 接口管理类
 */
public interface SpeechServer {
    //获取音色
    @GET("/bot/service/mmip/v2/tts/voice")
    Observable<BaseResponse<List<VoiceBean>>> getVoiceName();

}
