package com.inspur.mspeech.net;

import com.inspur.mspeech.bean.BaseResponse;
import com.inspur.mspeech.bean.QaBean;

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
 * desc    : 问答设置接口管理类
 */
public interface QaServer {
    //获取问答集
    @GET("/bot/service/mmip/v2/qa")
    Observable<BaseResponse<List<QaBean>>> getQa(@QueryMap Map<String,Object> body);

    //存储问答集
    @POST("/bot/service/mmip/v2/qa")
    Observable<BaseResponse> saveQa(@Body RequestBody body);

    //删除问答集
    @DELETE("/bot/service/mmip/v2/qa")
    Observable<BaseResponse> deleteQa(@QueryMap Map<String,Object> body);


    //新增一个问法
    @POST("/bot/service/mmip/v2/qa/question")
    Observable<BaseResponse> saveQuestion(@Body RequestBody body);

    //删除一个问法
    @DELETE("/bot/service/mmip/v2/qa/question")
    Observable<BaseResponse> deleteQuestion(@QueryMap Map<String,Object> body);

    //新增一个答案
    @POST("/bot/service/mmip/v2/qa/answer")
    Observable<BaseResponse> saveAnswer(@Body RequestBody body);

    //删除一个答案
    @DELETE("/bot/service/mmip/v2/qa/answer")
    Observable<BaseResponse> deleteAnswer(@QueryMap Map<String,Object> body);
}
