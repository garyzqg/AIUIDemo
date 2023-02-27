package com.inspur.mspeech.net;

import okhttp3.ResponseBody;
import payfun.lib.net.NetManager;
import payfun.lib.net.rx.BaseObserver;
import payfun.lib.net.rx.RxClient;
import payfun.lib.net.rx.RxScheduler;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * @author : zhangqinggong
 * date    : 2023/2/24 17:24
 * desc    : 网络操作类
 */
public class SpeechNet {
    public static void init(){
        NetManager.getInstance().initApi(SpeechServer.class, () -> new RxClient.Builder()
                .baseUrl(NetConstants.BASE_URL_TEST)
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
    public static void getVoiceName(BaseObserver<ResponseBody> observer){
        NetManager.getInstance().getApi(SpeechServer.class)
                .getVoiceName()
                .compose(RxScheduler.obsIo2Main())
                .subscribe(observer);
    }
}
