package com.inspur.mspeech.sse;

import com.inspur.mspeech.net.NetConstants;
import com.inspur.mspeech.utils.PrefersTool;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import okhttp3.sse.EventSources;
import payfun.lib.basis.utils.LogUtil;
import payfun.lib.basis.utils.ToastUtil;

/**
 * @author : zhangqinggong
 * date    : 2023/8/8 14:55
 * desc    : sse管理类
 */
public class SseOperator {
    private static final String TAG = "SseOperator";
    private static SseOperator mInstance;
    private EventSourceListener eventSourceListener;
    private SSEConnectSucccesListener mSseConnectSucccesListener;
    private static long timeStamp;
    private SseOperator() {
    }

    public static synchronized SseOperator getInstance() {
        if (mInstance == null) {
            mInstance = new SseOperator();
            timeStamp = System.currentTimeMillis();
        }
        return mInstance;
    }

    public void connect(String content) {
        String url = NetConstants.BASE_URL_TEST+"/bot/service/mmip/v2/llm/get/answer"+
                "?sceneId=7894561230&sessionId="+timeStamp+"&queryContent=" + content;
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(1, TimeUnit.MINUTES)
                .readTimeout(1, TimeUnit.MINUTES)
                .build();
        Request request = new Request
                .Builder()
                .header("Authorization", "Bearer " + PrefersTool.getAccesstoken())
                .url(url)
                .build();

        EventSource.Factory factory = EventSources.createFactory(okHttpClient);

        eventSourceListener = new EventSourceListener() {
            /**
             * {@inheritDoc}
             */
            @Override
            public void onOpen(final EventSource eventSource, final Response response) {
                LogUtil.iTag(TAG, "sse已连接");

            }

            /**
             * {@inheritDoc}
             */
            @Override
            public void onEvent(final EventSource eventSource, final String id, final String type, final String data) {
                LogUtil.iTag(TAG, "sse数据" + data);
                if (mSseConnectSucccesListener != null) {
                    mSseConnectSucccesListener.getData(data);
                }
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public void onClosed(final EventSource eventSource) {
                //连接失败尝试重新连接
//                if (request != null && eventSourceListener != null) {
//                    LogUtil.eTag(TAG, "sse断联，正在尝试重新连接...");
//                    factory.newEventSource(request, eventSourceListener);
//                } else {
//                    LogUtil.eTag(TAG, "sse断联，请稍后重试");
//                    ToastUtil.showShort("sse断联，请稍后重试");
//                }
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public void onFailure(final EventSource eventSource, final Throwable t, final Response response) {
//                LogUtil.e("使用事件源时出现异常","连接服务端时出现异常-----" + response.message());
                //连接失败尝试重新连接
                if (request != null && eventSourceListener != null) {
                    LogUtil.eTag(TAG, "sse连接失败，正在尝试重新连接...");
                    factory.newEventSource(request, eventSourceListener);
                } else {
                    LogUtil.eTag(TAG, "sse连接失败，请稍后重试");
                    ToastUtil.showShort("sse连接失败，请稍后重试");
                }
            }
        };
        //创建事件
        factory.newEventSource(request, eventSourceListener);
        //由于springboot test异步的，加下面代码卡住同步
//        CountDownLatch countDownLatch = new CountDownLatch(1);
//        try {
//            countDownLatch.await();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
    }

    //建立监听回调
    public void getSSEMessage(SSEConnectSucccesListener sseConnectSucccesListener) {
        mSseConnectSucccesListener = sseConnectSucccesListener;
    }

    public interface SSEConnectSucccesListener {
        void getData(String data);
    }
}
