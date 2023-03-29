package com.inspur.mspeech.websocket;

import android.text.TextUtils;

import com.google.gson.reflect.TypeToken;
import com.inspur.mspeech.bean.VadBean;
import com.inspur.mspeech.bean.VadMsgBean;

import org.java_websocket.enums.ReadyState;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.List;

import payfun.lib.basis.utils.LogUtil;
import payfun.lib.net.helper.GsonHelper;

/**
 * @author : zhangqinggong
 * date    : 2023/3/27 9:39
 * desc    : Websocket操作类 - VAD
 */
public class WebsocketVADOperator {
   private static final String TAG = "WebsocketVADOperator";
   private JWebSocketClient mClient;
   private static WebsocketVADOperator instance;
   private IWebsocketListener mIWebsocketListener;
   public boolean startSendMsg = false;

   private WebsocketVADOperator() {
   }

   public static WebsocketVADOperator getInstance(){
      if (instance == null){
         instance = new WebsocketVADOperator();
      }
      return instance;
   }

   /**
    * websocket初始化
    * @param iWebsocketListener 监听
    */
   public void initWebSocket(IWebsocketListener iWebsocketListener) {
      if (mClient == null){
         mIWebsocketListener = iWebsocketListener;
         URI uri = URI.create("ws://10.180.151.125:10089");
         //为了方便对接收到的消息进行处理，可以在这重写onMessage()方法
         LogUtil.iTag(TAG, "VAD WebSocket init");
         mClient = new JWebSocketClient(uri) {
            @Override
            public void onOpen(ServerHandshake handshakedata) {
               LogUtil.iTag(TAG, "VAD WebSocket onOpen");
               if (iWebsocketListener != null){
                  iWebsocketListener.onOpen();
               }
            }

            @Override
            public void onMessage(String message) {
               LogUtil.iTag(TAG, "VAD WebSocket onMessage:" + message);
               if (TextUtils.equals("{\"status\":\"ok\",\"type\":\"server_ready\"}",message)){
                  //{"status":"ok","type":"server_ready"} 开始发送采集的音频数据
                  startSendMsg = true;
               }else if (TextUtils.equals("{\"vad\":\"end\"}",message)){
                  //{"vad":"end"} 收到后发送一个end 不做其他任何处理
                  sendMessage("end",true);
                  startSendMsg = false;
               } else if (TextUtils.equals("{\"status\":\"ok\",\"type\":\"speech_end\"}",message)){
                  //{"status":"ok","type":"speech_end"} 收到后断开连接
                  close();
               } else {
                  try{
                     //识别数据 流式识别
                     //{"status":"ok","type":"partial_result","nbest":"[{\"sentence\":\"在接收数据\"}]"}
                     //{"status":"ok","type":"final_result","nbest":"[{\"sentence\":\"在接收复这\",\"word_pieces\":[{\"word\":\"在\",\"start\":100,\"end\":200},{\"word\":\"接\",\"start\":340,\"end\":440},{\"word\":\"收\",\"start\":660,\"end\":760},{\"word\":\"复\",\"start\":900,\"end\":1000},{\"word\":\"这\",\"start\":1180,\"end\":1280}]}]"}
                     VadBean vadBean = GsonHelper.GSON.fromJson(message, VadBean.class);
                     String type = vadBean.getType();
                     if (TextUtils.equals("partial_result",type)){
                        //流式识别结果
                        if (iWebsocketListener != null){
                           String nbest = vadBean.getNbest();
                           List<VadMsgBean> datas = GsonHelper.GSON.fromJson(nbest, new TypeToken<List<VadMsgBean>>() {}.getType());
                           String sentence = datas.get(0).getSentence();
                           iWebsocketListener.OnVadData(sentence);
                        }
                     }else if (TextUtils.equals("final_result",type)){
                        //最终识别结果
                        if (iWebsocketListener != null){
                           String nbest = vadBean.getNbest();
                           List<VadMsgBean> datas = GsonHelper.GSON.fromJson(nbest, new TypeToken<List<VadMsgBean>>() {}.getType());
                           String sentence = datas.get(0).getSentence();
                           iWebsocketListener.OnFinalData(sentence);
                        }
                     }
                  }catch (Exception e){//解析异常捕获
                     e.printStackTrace();
                  }

               }
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
               LogUtil.iTag(TAG, "VAD WebSocket onClose: code:" + code + " reason:" + reason + " remote:" + remote);
               if (iWebsocketListener != null){
                  iWebsocketListener.onClose();
               }

               startSendMsg = false;
            }

            @Override
            public void onError(Exception ex) {
               LogUtil.iTag(TAG, "VAD WebSocket onError:" + ex.toString());
               if (iWebsocketListener != null){
                  iWebsocketListener.onError();
               }
            }

         };

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
                        LogUtil.iTag(TAG, "VAD WebSocket connect");
                     } else if (mClient.getReadyState().equals(ReadyState.CLOSING) || mClient.getReadyState().equals(ReadyState.CLOSED)) {
                        mClient.reconnectBlocking();
                        LogUtil.iTag(TAG, "VAD WebSocket reconnect");
                     }
                  } catch (Exception e) {
                     e.printStackTrace();
                     if (mIWebsocketListener != null){
                        mIWebsocketListener.onError();
                     }
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
   public void sendMessage(String message,boolean log) {
      if (mClient != null && mClient.isOpen()) {
         if (log){
            LogUtil.iTag(TAG, "VAD WebSocket sendMessage:" + message);
         }

         mClient.send(message);
      } else {
         // TODO: 2023/1/13 此时如果是唤醒后超时没有交互,是否不做任何播报?
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
      void OnFinalData(String finalString);
      void OnVadData(String vadString);
      void onOpen();
      void onError();
      void onClose();
   }
}
