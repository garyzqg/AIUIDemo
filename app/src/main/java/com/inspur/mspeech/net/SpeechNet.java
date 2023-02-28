package com.inspur.mspeech.net;

import com.inspur.mspeech.bean.BaseResponse;
import com.inspur.mspeech.bean.QaBean;
import com.inspur.mspeech.bean.VoiceBean;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.RequestBody;
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
                .baseUrl(NetConstants.BASE_URL_VOICENAME_PROD)
                .connectTimeout(10)
                .readTimeout(15)
                .writeTimeout(15)
                .addConvertFactory(GsonConverterFactory.create())
                .addAdapterFactory(RxJava3CallAdapterFactory.create())
                .isUseLog(true)
        );

        NetManager.getInstance().initApi(QaServer.class, () -> new RxClient.Builder()
                .baseUrl(NetConstants.BASE_QA_URL_PROD)
                .connectTimeout(10)
                .readTimeout(15)
                .writeTimeout(15)
                .addConvertFactory(GsonConverterFactory.create())
                .addAdapterFactory(RxJava3CallAdapterFactory.create())
                .isUseLog(true)
        );
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
        para.put("sceneId", "1628207224099196929");
        para.put("size", 50);
        para.put("userId", "123456789");
        para.put("userAccount", "speechtest");
        NetManager.getInstance().getApi(QaServer.class)
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
        para.put("sceneId", "1628207224099196929");
        para.put("userAccount", "speechtest");
        para.put("userId", "123456789");
        String s = GsonHelper.GSON.toJson(para);
        RequestBody body = RequestBody.create(s, MediaType.parse("application/json; charset=utf-8"));
        NetManager.getInstance().getApi(QaServer.class)
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
        para.put("sceneId", "1628207224099196929");
        para.put("userAccount", "speechtest");
        para.put("userId", "123456789");
        NetManager.getInstance().getApi(QaServer.class)
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
        para.put("sceneId", "1628207224099196929");
        para.put("userAccount", "speechtest");
        para.put("userId", "123456789");
        String s = GsonHelper.GSON.toJson(para);
        RequestBody body = RequestBody.create(s, MediaType.parse("application/json; charset=utf-8"));
        NetManager.getInstance().getApi(QaServer.class)
                .saveQuestion(body)
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
        para.put("sceneId", "1628207224099196929");
        para.put("userAccount", "speechtest");
        para.put("userId", "123456789");
        String s = GsonHelper.GSON.toJson(para);
        RequestBody body = RequestBody.create(s, MediaType.parse("application/json; charset=utf-8"));
        NetManager.getInstance().getApi(QaServer.class)
                .saveAnswer(body)
                .compose(RxScheduler.obsIo2Main())
                .subscribe(observer);
    }
}
