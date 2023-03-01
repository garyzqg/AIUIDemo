package payfun.lib.net.api;

import java.util.List;
import java.util.Map;

import io.reactivex.rxjava3.core.Observable;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import payfun.lib.net.bean.BaseResp;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.PartMap;
import retrofit2.http.QueryMap;
import retrofit2.http.Streaming;
import retrofit2.http.Url;


/**
 * @author : zhangqg
 * date   : 2021/5/18 18:27
 * desc   : <默认通信请求接口类; 参考：https://github.com/square/retrofit >
 */
public interface DefaultApi {

    /**
     * 表单请求:POST
     *
     * @param url  url或者domain,无则传空串
     * @param maps Map形式的请求参数
     * @return 返回参数
     */
    @POST()
    @FormUrlEncoded
    Observable<ResponseBody> postForm(@Url String url, @FieldMap Map<String, Object> maps);

    /**
     * Object参数请求
     *
     * @param url    url或者domain,无则传空串
     * @param object 请求参数
     * @return 返回参数
     */
    @POST()
    Observable<ResponseBody> post(@Url String url, @Body Object object);

    /**
     * RequestBody参数请求:POST
     *
     * @param url  url或者domain,无则传空串
     * @param body RequestBody形式的请求参数,可自定义请求内容
     * @return 返回参数
     */
    @POST()
    Observable<ResponseBody> post(@Url String url, @Body RequestBody body);

    /**
     * Map参数请求:POST
     *
     * @param url  url或者domain,无则传空串
     * @param maps Map形式的请求参数,可自定义请求内容
     * @return 返回参数
     */
    @POST()
    Observable<ResponseBody> post(@Url String url, @Body Map<String, Object> maps);

    /**
     * Map参数请求:GET
     *
     * @param url  url或者domain,无则传空串
     * @param maps Map形式的请求参数
     * @return 返回参数
     */
    @GET()
    Observable<ResponseBody> get(@Url String url, @QueryMap Map<String, Object> maps);

    @Multipart
    @POST()
    Observable<ResponseBody> uploadFile(@Url String fileUrl, @Part("description") RequestBody description, @Part("files") MultipartBody.Part file);

    @Multipart
    @POST()
    Observable<ResponseBody> uploadFiles(@Url String url, @PartMap Map<String, RequestBody> maps);

    @Multipart
    @POST()
    Observable<ResponseBody> uploadFiles(@Url String url, @Part List<MultipartBody.Part> parts);

    /**
     * 下载文件
     *
     * @param fileUrl 文件地址
     * @return 流
     */
    @Streaming
    @GET
    Observable<ResponseBody> downloadFile(@Url String fileUrl);


    /**
     * Get请求
     *
     * @param url url
     * @return 结果
     */
    @GET()
    Call<BaseResp<String>> postCall(@Url String url);

    /**
     * 下载文件
     *
     * @param fileUrl 文件地址
     * @return 流
     */
    @Streaming
    @GET
    Call<ResponseBody> downloadFileCall(@Url String fileUrl);


}
