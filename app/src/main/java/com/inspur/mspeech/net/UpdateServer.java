package com.inspur.mspeech.net;

import io.reactivex.rxjava3.core.Observable;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.http.Body;
import retrofit2.http.POST;
/**
 * @author : zhangqinggong
 * date    : 2023/2/24 17:24
 * desc    : 接口管理类
 */
public interface UpdateServer {
    @POST("/prod-api/cloud/service/ota/v1/pag-query")
    Observable<ResponseBody> getUpdateVersion(@Body RequestBody body);


}
