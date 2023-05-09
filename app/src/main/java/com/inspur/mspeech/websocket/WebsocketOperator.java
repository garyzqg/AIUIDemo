package com.inspur.mspeech.websocket;

import android.text.TextUtils;
import android.util.Log;

import com.inspur.mspeech.bean.NlpBean;
import com.inspur.mspeech.bean.TtsBean;
import com.inspur.mspeech.net.NetConstants;
import com.inspur.mspeech.utils.Base64Utils;
import com.inspur.mspeech.utils.PrefersTool;

import org.java_websocket.enums.ReadyState;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.UUID;

import payfun.lib.basis.utils.LogUtil;
import payfun.lib.basis.utils.ToastUtil;
import payfun.lib.net.helper.GsonHelper;

/**
 * @author : zhangqinggong
 * date    : 2023/1/15 0:18
 * desc    : Websocket操作类
 */
public class WebsocketOperator {
   private static final String TAG = "WebsocketOperator";
   private static String sessionId;
   private JWebSocketClient mClient;
   public String voiceName;
   private static WebsocketOperator instance;
   private IWebsocketListener mIWebsocketListener;

   private WebsocketOperator() {
   }

   public static WebsocketOperator getInstance(){
      if (instance == null){
         instance = new WebsocketOperator();
         //启动时生成UUID作为sessionId 如果有登出操作需要重置
         sessionId = UUID.randomUUID().toString();
      }
      return instance;
   }

   /**
    * websocket初始化
    * @param reInit 是否重新初始化ws
    * @param iWebsocketListener 监听
    */
   public void initWebSocket(boolean reInit,IWebsocketListener iWebsocketListener) {
      if (reInit || mClient == null){
         mIWebsocketListener = iWebsocketListener;
         //ws://101.43.161.46:58091/ws？token=fengweisen&scene=xiaoguo_box&voiceName=xiaozhong&speed=50&ttsType=crcloud
         voiceName = PrefersTool.getVoiceName();
         boolean modelSwitch = PrefersTool.getModelSwitch();

         URI uri = URI.create(NetConstants.BASE_WS_URL_PROD+"/expressing/ws"+(modelSwitch?"/stream":"")+"?sceneId="+PrefersTool.getSceneId()+"&voiceName="+voiceName
                 +"&speed="+PrefersTool.getSpeed()+"&pitch="+PrefersTool.getTone()+"&ttsType=azure&sessionId="+sessionId);

//         URI uri = URI.create("ws://101.43.161.46:58091/ws？token=fengweisen&scene=xiaoguo_box&voiceName=xiaozhong&speed=50&ttsType=crcloud");
         //为了方便对接收到的消息进行处理，可以在这重写onMessage()方法
         LogUtil.iTag(TAG, "WebSocket init");
         mClient = new JWebSocketClient(uri) {
            @Override
            public void onOpen(ServerHandshake handshakedata) {
               LogUtil.iTag(TAG, "WebSocket onOpen");
               if (mIWebsocketListener != null){
                  mIWebsocketListener.onOpen();
               }
            }

            @Override
            public void onMessage(String message) {
//               LogUtil.iTag(TAG, "onMessage:" + message);
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
               LogUtil.iTag(TAG, "WebSocket onClose: code:" + code + " reason:" + reason + " remote:" + remote + "  " + (mIWebsocketListener != null));
               // TODO: 2023/1/13 断开连接后 是否控制不往aiui写数据 如何保证websocket的超时和AIUI的超时保持一致?
               if (mIWebsocketListener != null){
                  mIWebsocketListener.onClose(reason.contains("401"));
               }
            }

            @Override
            public void onError(Exception ex) {
               ex.printStackTrace();
               LogUtil.iTag(TAG, "WebSocket onError:" + ex.toString());
               if (mIWebsocketListener != null){
                  mIWebsocketListener.onError();
               }

            }

            @Override
            public void onMessage(ByteBuffer bytes) {
//               super.onMessage(bytes);
               //{"data":{"question":"你好","answer":"你也好","entities":[],"finish":true,"intent":"qa_general_intent"},"type":"nlp"}
               //{"type": "tts","data": {"is_finish": true,"audio": ""}}
               Charset charset = Charset.forName("utf-8");
               CharBuffer decode = charset.decode(bytes);
               String message = decode.toString();


               if (TextUtils.isEmpty(message)){
                  return;
               }

               try {
                  JSONObject jsonObject = new JSONObject(message);
                  String type = jsonObject.optString("type");
                  String data = jsonObject.optString("data");
                  LogUtil.iTag(TAG, "WebSocket onMessage -- type: " + type);
                  if (TextUtils.equals("nlp", type)) {
                     LogUtil.iTag(TAG, "WebSocket onMessage: " + message);
                     NlpBean nlpBean = GsonHelper.GSON.fromJson(data, NlpBean.class);
                     if (PrefersTool.getModelSwitch()){
                        String answer = nlpBean.getAnswer();
                        boolean finish = nlpBean.isFinish();
                        String text="";
                        if (!TextUtils.isEmpty(answer)){
                           JSONObject answerObject = new JSONObject(answer);
                           text = answerObject.optString("text");
                        }
                        if (mIWebsocketListener != null){
                           mIWebsocketListener.onNlpStreamData(text,finish);
                        }

                     }else {
                        String question = nlpBean.getQuestion();
                        String answer = nlpBean.getAnswer();
                        if (mIWebsocketListener != null){
                           mIWebsocketListener.OnNlpData(answer);
                        }
                     }

                  } else if (TextUtils.equals("tts", type)) {
                     TtsBean ttsBean = GsonHelper.GSON.fromJson(data, TtsBean.class);
                     boolean is_finish = ttsBean.isIs_finish();
                     String audio = ttsBean.getAudio();
                     if (TextUtils.isEmpty(audio)){
                        if (mIWebsocketListener != null){
                           mIWebsocketListener.OnTtsData(null,is_finish);
                        }
                     }else {
                        byte[] audioByte = Base64Utils.base64DecodeToByte(audio);
                        if (mIWebsocketListener != null){
                           mIWebsocketListener.OnTtsData(audioByte,is_finish);
                        }
                     }


                  }

               } catch (JSONException e) {
                  e.printStackTrace();
               }
            }
         };
         setToken();
         mClient.setConnectionLostTimeout(10 * 1000);
      }
   }

   private void setToken(){
      String accesstoken = PrefersTool.getAccesstoken();
      if (!TextUtils.isEmpty(accesstoken)){
         mClient.addHeader("Authorization", "Bearer " + accesstoken);
      }
   }


   /**
    * websocket连接
    */
   public void connectWebSocket() {
      //需要先断开已有连接

      if (mClient != null && mClient.isOpen()){
         mClient.close();
      }

      new Thread(new Runnable() {
         @Override
         public void run() {
            //连接时可以使用connect()方法或connectBlocking()方法，建议使用connectBlocking()方法，
            // connectBlocking多出一个等待操作，会先连接再发送。
            if (mClient != null) {
               if (!mClient.isOpen()) {
                  try {
                     if (mClient.getReadyState().equals(ReadyState.NOT_YET_CONNECTED)) {
                        mClient.connectBlocking();
                        LogUtil.iTag(TAG, "WebSocket connect");
                     } else if (mClient.getReadyState().equals(ReadyState.CLOSING) || mClient.getReadyState().equals(ReadyState.CLOSED)) {
                        mClient.reconnectBlocking();
                        LogUtil.iTag(TAG, "WebSocket reconnect");
                     }
                  } catch (Exception e) {
                     e.printStackTrace();
                     LogUtil.iTag(TAG, Log.getStackTraceString(e));
                     ToastUtil.showLong("服务调用异常");
//                     if (mIWebsocketListener != null){
//                        mIWebsocketListener.onError();
//                     }
                  }
               }
            }
         }
      }).start();

   }

   /**
    * 发消息
    *
    * @param message
    */
   public void sendMessage(String message) {
      if (mClient != null && mClient.isOpen() && mClient.getReadyState().equals(ReadyState.OPEN)) {
         LogUtil.iTag(TAG, "WebSocket sendMessage:" + message);
         try{
            mClient.send(message);
         }catch (Exception e){
            LogUtil.eTag(TAG,Log.getStackTraceString(e));
         }
      }
   }

   public boolean isOpen(){
      return mClient != null && mClient.isOpen();
   }
   public void close(){
      if (mClient != null && mClient.isOpen()){
         mClient.close();
      }
   }

   public interface IWebsocketListener{
      void OnTtsData(byte[] audioData,boolean isFinish);
      void OnNlpData(String nlpString);

      void onNlpStreamData(String nlpStream,boolean isFinish);
      void onOpen();
      void onError();
      void onClose(boolean isLogin);
   }
}
