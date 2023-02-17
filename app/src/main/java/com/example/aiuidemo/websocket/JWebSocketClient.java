package com.example.aiuidemo.websocket;

import com.example.aiuidemo.utils.LogUtil;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.nio.ByteBuffer;

/**
 * @author : zhangqinggong
 * date    : 2023/1/13 10:22
 * desc    : WebSocketClient
 */
public class JWebSocketClient extends WebSocketClient {

   private static final String TAG = "JWebSocketClient";
   public JWebSocketClient(URI serverUri) {
      super(serverUri);
   }

   @Override
   public void onOpen(ServerHandshake handshakedata) {
      LogUtil.iTag(TAG,"onOpen");
   }

   @Override
   public void onMessage(String message) {
      LogUtil.iTag(TAG,"onMessage:" + message);
   }

   @Override
   public void onClose(int code, String reason, boolean remote) {
      LogUtil.iTag(TAG,"onClose: code:" + code+ " reason:" + reason+ " remote:" + remote);
   }

   @Override
   public void onError(Exception ex) {
      LogUtil.iTag(TAG,"onError:" + ex.toString());
   }

   @Override
   public void onMessage(ByteBuffer bytes) {
      super.onMessage(bytes);
   }

}
