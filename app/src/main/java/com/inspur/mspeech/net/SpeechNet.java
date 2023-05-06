package com.inspur.mspeech.net;

import android.text.TextUtils;

import com.inspur.mspeech.bean.BaseResponse;
import com.inspur.mspeech.bean.LoginBean;
import com.inspur.mspeech.bean.QaBean;
import com.inspur.mspeech.bean.SceneBean;
import com.inspur.mspeech.bean.VoiceBean;
import com.inspur.mspeech.utils.Base64Utils;
import com.inspur.mspeech.utils.PrefersTool;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import payfun.lib.net.NetManager;
import payfun.lib.net.helper.GsonHelper;
import payfun.lib.net.rx.BaseObserver;
import payfun.lib.net.rx.RxClient;
import payfun.lib.net.rx.RxScheduler;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * @author : zhangqinggong
 * date    : 2023/2/24 17:24
 * desc    : 音色获取网络操作类
 */
public class SpeechNet {
    public static void init(){
        NetManager.getInstance().initApi(SpeechServer.class, () -> new RxClient.Builder()
                .baseUrl(NetConstants.BASE_URL_PROD)
                .connectTimeout(10)
                .readTimeout(15)
                .writeTimeout(15)
                .addInterceptor(new HeaderInterceptor())
                .addConvertFactory(GsonConverterFactory.create())
                .addAdapterFactory(RxJava3CallAdapterFactory.create())
                .isUseLog(true)
        );

        NetManager.getInstance().initApi(UpdateServer.class, () -> new RxClient.Builder()
                .baseUrl(NetConstants.BASE_URL_UPDATE)
                .connectTimeout(10)
                .readTimeout(15)
                .writeTimeout(15)
                .addConvertFactory(GsonConverterFactory.create())
                .addAdapterFactory(RxJava3CallAdapterFactory.create())
                .isUseLog(true)
        );
    }

    public static class HeaderInterceptor implements Interceptor {
        @Override
        public Response intercept(Chain chain) throws IOException {
            String token = PrefersTool.getAccesstoken();
            Request originalRequest = chain.request();
            if (TextUtils.isEmpty(token)) {
                return chain.proceed(originalRequest);
            } else {
                Request updateRequest = originalRequest.newBuilder()
                        .header("Authorization", "Bearer " + token)
                        .build();
                return chain.proceed(updateRequest);
            }
        }
    }

    /**
     * 获取音色列表
     * @param observer
     */
    public static void getVoiceName(BaseObserver<BaseResponse<List<VoiceBean>>> observer){
        NetManager.getInstance().getApi(SpeechServer.class)
                .getVoiceName()
                .compose(RxScheduler.obsIo2Main())
                .subscribe(observer);
    }

    /**
     * 获取问答集
     * @param observer
     */
    public static void getQa(BaseObserver<BaseResponse<List<QaBean>>> observer){
        Map<String, Object> para = new HashMap<>();
        // TODO: 2023/2/27 参数待实现
        para.put("page", 1);
        para.put("sceneId", PrefersTool.getSceneId());
        para.put("size", 50);
        NetManager.getInstance().getApi(SpeechServer.class)
                .getQa(para)
                .compose(RxScheduler.obsIo2Main())
                .subscribe(observer);
    }

    /**
     * 新增问答集
     * @param observer
     */
    public static void saveQa(String questionTxt,String answerTxt,BaseObserver<BaseResponse> observer){
        Map<String, Object> para = new HashMap<>();
        para.put("questionTxt", questionTxt);
        para.put("answerTxt", answerTxt);
        para.put("answerType", "txt");
        para.put("sceneId", PrefersTool.getSceneId());
        String s = GsonHelper.GSON.toJson(para);
        RequestBody body = RequestBody.create(s, MediaType.parse("application/json; charset=utf-8"));
        NetManager.getInstance().getApi(SpeechServer.class)
                .saveQa(body)
                .compose(RxScheduler.obsIo2Main())
                .subscribe(observer);
    }

    /**
     * 删除问答集
     * @param observer
     */
    public static void deleteQa(String qaId,BaseObserver<BaseResponse> observer){
        Map<String, Object> para = new HashMap<>();
        para.put("qaId", qaId);
        para.put("sceneId", PrefersTool.getSceneId());
        NetManager.getInstance().getApi(SpeechServer.class)
                .deleteQa(para)
                .compose(RxScheduler.obsIo2Main())
                .subscribe(observer);
    }

    /**
     * 问题 - 新增
     * @param observer
     */
    public static void saveQuestion(String qaId,String questionTxt,BaseObserver<BaseResponse> observer){
        Map<String, Object> para = new HashMap<>();
        para.put("qaId", qaId);
        para.put("questionTxt", questionTxt);
        para.put("standard", "0");
        para.put("sceneId", PrefersTool.getSceneId());
        String s = GsonHelper.GSON.toJson(para);
        RequestBody body = RequestBody.create(s, MediaType.parse("application/json; charset=utf-8"));
        NetManager.getInstance().getApi(SpeechServer.class)
                .saveQuestion(body)
                .compose(RxScheduler.obsIo2Main())
                .subscribe(observer);
    }

    /**
     * 问题 - 删除
     * @param observer
     */
    public static void deleteQuestion(String qaId,String questionId,BaseObserver<BaseResponse> observer){
        Map<String, Object> para = new HashMap<>();
        para.put("qaId", qaId);
        para.put("questionId", questionId);
        para.put("sceneId", PrefersTool.getSceneId());
        NetManager.getInstance().getApi(SpeechServer.class)
                .deleteQuestion(para)
                .compose(RxScheduler.obsIo2Main())
                .subscribe(observer);
    }

    /**
     * 答案 - 新增
     * @param observer
     */
    public static void saveAnswer(String qaId,String answerTxt,BaseObserver<BaseResponse> observer){
        Map<String, Object> para = new HashMap<>();
        para.put("qaId", qaId);
        para.put("answerTxt", answerTxt);
        para.put("answerType", "txt");
        para.put("standard", "0");
        para.put("sceneId", PrefersTool.getSceneId());
        String s = GsonHelper.GSON.toJson(para);
        RequestBody body = RequestBody.create(s, MediaType.parse("application/json; charset=utf-8"));
        NetManager.getInstance().getApi(SpeechServer.class)
                .saveAnswer(body)
                .compose(RxScheduler.obsIo2Main())
                .subscribe(observer);
    }

    /**
     * 答案 - 删除
     * @param observer
     */
    public static void deleteAnswer(String qaId,String answerId,BaseObserver<BaseResponse> observer){
        Map<String, Object> para = new HashMap<>();
        para.put("qaId", qaId);
        para.put("answerId", answerId);
        para.put("sceneId", PrefersTool.getSceneId());
        NetManager.getInstance().getApi(SpeechServer.class)
                .deleteAnswer(para)
                .compose(RxScheduler.obsIo2Main())
                .subscribe(observer);
    }

    /**
     * 次数限制
     * @param observer
     */
    public static void userCount(BaseObserver<BaseResponse<Integer>> observer){

        NetManager.getInstance().getApi(SpeechServer.class)
                .getUser()
                .compose(RxScheduler.obsIo2Main())
                .subscribe(observer);
    }

    /**
     * 登录
     * @param observer
     */
    public static void login(String userName,String pwd,BaseObserver<BaseResponse<LoginBean>> observer){
        Map<String, Object> para = new HashMap<>();
        para.put("password", Base64Utils.base64EncodeToString(pwd));
        para.put("userName", userName);
        String s = GsonHelper.GSON.toJson(para);
        RequestBody body = RequestBody.create(s, MediaType.parse("application/json; charset=utf-8"));
        NetManager.getInstance().getApi(SpeechServer.class)
                .login(body)
                .compose(RxScheduler.obsIo2Main())
                .subscribe(observer);
    }
    /**
     * 获取情景id
     * @param observer
     */
    public static void getSceneId(BaseObserver<BaseResponse<List<SceneBean>>> observer){
        NetManager.getInstance().getApi(SpeechServer.class)
                .getSceneId()
                .compose(RxScheduler.obsIo2Main())
                .subscribe(observer);
    }

    /**
     * 登录
     * @param observer
     */
    public static void getUpdatgeInfo(BaseObserver<ResponseBody> observer){
        Map<String, Object> para = new HashMap<>();
        para.put("agent_os", "Android");
        para.put("agent_model", "M");
        String s = GsonHelper.GSON.toJson(para);
        RequestBody body = RequestBody.create(s, MediaType.parse("application/json; charset=utf-8"));
        NetManager.getInstance().getApi(UpdateServer.class)
                .getUpdateVersion(body)
                .compose(RxScheduler.obsIo2Main())
                .subscribe(observer);
    }

}
