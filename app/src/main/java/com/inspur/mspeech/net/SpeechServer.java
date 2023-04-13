package com.inspur.mspeech.net;

import com.inspur.mspeech.bean.BaseResponse;
import com.inspur.mspeech.bean.LoginBean;
import com.inspur.mspeech.bean.QaBean;
import com.inspur.mspeech.bean.SceneBean;
import com.inspur.mspeech.bean.VoiceBean;

import java.util.List;
import java.util.Map;

import io.reactivex.rxjava3.core.Observable;
import okhttp3.RequestBody;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.QueryMap;

/**
 * @author : zhangqinggong
 * date    : 2023/2/24 17:24
 * desc    : 接口管理类
 */
public interface SpeechServer {
    //获取音色
    @GET("/bot/service/mmip/expressing/v2/tts/voice")
    Observable<BaseResponse<List<VoiceBean>>> getVoiceName();

    //获取可用次数
    @GET("/bot/service/mmip/statistics/v2/statistics/user-available-count")
    Observable<BaseResponse<Integer>> getUser();

    //获取情景id
    @GET("/bot/service/mmip/statistics/v2/statistics/scene")
    Observable<BaseResponse<List<SceneBean>>> getSceneId();
    //获取问答集
    @GET("/bot/service/mmip/understanding/v2/qa")
    Observable<BaseResponse<List<QaBean>>> getQa(@QueryMap Map<String,Object> body);

    //存储问答集
    @POST("/bot/service/mmip/understanding/v2/qa")
    Observable<BaseResponse> saveQa(@Body RequestBody body);

    //删除问答集
    @DELETE("/bot/service/mmip/understanding/v2/qa")
    Observable<BaseResponse> deleteQa(@QueryMap Map<String,Object> body);


    //新增一个问法
    @POST("/bot/service/mmip/understanding/v2/qa/question")
    Observable<BaseResponse> saveQuestion(@Body RequestBody body);

    //删除一个问法
    @DELETE("/bot/service/mmip/understanding/v2/qa/question")
    Observable<BaseResponse> deleteQuestion(@QueryMap Map<String,Object> body);

    //新增一个答案
    @POST("/bot/service/mmip/understanding/v2/qa/answer")
    Observable<BaseResponse> saveAnswer(@Body RequestBody body);

    //删除一个答案
    @DELETE("/bot/service/mmip/understanding/v2/qa/answer")
    Observable<BaseResponse> deleteAnswer(@QueryMap Map<String,Object> body);

    //登录
    @POST("/bot/service/mmip/auth/v2/login")
    Observable<BaseResponse<LoginBean>> login(@Body RequestBody body);
}
