package com.inspur.mspeech.net;

import com.inspur.mspeech.bean.BaseResponse;

import java.util.Map;

import io.reactivex.rxjava3.core.Observable;
import retrofit2.http.GET;
import retrofit2.http.QueryMap;

/**
 * @author : zhangqinggong
 * date    : 2023/2/24 17:24
 * desc    : 接口管理类
 */
public interface UserServer {
    //获取问答集
    @GET("/bot/service/mmip/v2/statistics/user-available-count")
    Observable<BaseResponse<Integer>> getUser(@QueryMap Map<String,Object> body);

}
