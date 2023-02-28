package com.inspur.mspeech.ui;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;

import com.bumptech.glide.Glide;
import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;
import com.iflytek.aiui.AIUIAgent;
import com.iflytek.aiui.AIUIConstant;
import com.iflytek.aiui.AIUIEvent;
import com.iflytek.aiui.AIUIListener;
import com.iflytek.aiui.AIUIMessage;
import com.iflytek.aiui.AIUISetting;
import com.inspur.mspeech.R;
import com.inspur.mspeech.adapter.MsgAdapter;
import com.inspur.mspeech.audio.AudioTrackOperator;
import com.inspur.mspeech.bean.Msg;
import com.inspur.mspeech.net.SpeechNet;
import com.inspur.mspeech.utils.UIHelper;
import com.inspur.mspeech.websocket.WebsocketOperator;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import jaygoo.widget.wlv.WaveLineView;
import payfun.lib.basis.utils.InitUtil;
import payfun.lib.basis.utils.LogUtil;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "MainActivity";
    // AIUI
    private AIUIAgent mAIUIAgent = null;
    // AIUI工作状态
    private int mAIUIState = AIUIConstant.STATE_IDLE;

    private AudioTrackOperator mAudioTrackOperator;
    private String mIatMessage;//iat有效数据
    private RecyclerView mRvChat;
    private List<Msg> msgList = new ArrayList<>();
    private MsgAdapter mAdapter;
    private WaveLineView mWaveLineView;
    private AppCompatImageView mIvVoiceball;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //设置无Title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //设置应用全屏,必须写在setContentView方法前面
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main_new);
        UIHelper.hideBottomUIMenu(this);

        initView();

        InitUtil.init(this);

        SpeechNet.init();
        //权限
        getPermission();

    }

    private void initView() {
        mWaveLineView = findViewById(R.id.waveLineView);
        mRvChat = findViewById(R.id.recyclerview_chat);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mAdapter = new MsgAdapter(msgList);

        mRvChat.setLayoutManager(layoutManager);
        mRvChat.setAdapter(mAdapter);

        mIvVoiceball = findViewById(R.id.iv_voiceball);
        Glide.with(this)
                .load(R.drawable.gif_voice_ball)
                .placeholder(R.drawable.voice_ball)
                .into(mIvVoiceball);
        mIvVoiceball.setOnClickListener(view -> {
            if (mAIUIAgent != null){
                AIUIMessage wakeupMsg = new AIUIMessage(AIUIConstant.CMD_WAKEUP, 0, 0, "", null);
                mAIUIAgent.sendMessage(wakeupMsg);
            }
        });

    }

    //该方法用于创建显示Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_setting_action_bar,menu);
        return true;
    }
    //在选项菜单打开以后会调用这个方法，设置menu图标显示（icon）
    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        return super.onMenuOpened(featureId, menu);
    }
    //该方法对菜单的item进行监听
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.setting_menu_voice_name:
                Intent intent = new Intent(MainActivity.this, VoiceNameSettingActivity.class);
                intentActivityResultLauncher.launch(intent);
                break;
            case R.id.setting_menu_qa:
                Intent intent2 = new Intent(MainActivity.this, QaSettingActivity.class);
                startActivity(intent2);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public ActivityResultLauncher<Intent> intentActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),result -> {
        //设置音色页面回来 重新init ws
        initWebsocket();
    });

    @Override
    public void onClick(View view) {

    }

    private void initSDK() {
        // 初始化AIUI
        createAgent();
        //初始化AudioTrack
        initAudioTrack();
        //初始化websocket
        initWebsocket();

    }

    private void getPermission() {
        XXPermissions.with(MainActivity.this)
                // 申请多个权限
                .permission(Permission.ACCESS_COARSE_LOCATION, Permission.MANAGE_EXTERNAL_STORAGE, Permission.RECORD_AUDIO)
                // 申请单个权限
//                .permission(Permission.Group.CALENDAR)
                // 设置权限请求拦截器（局部设置）
                //.interceptor(new PermissionInterceptor())
                // 设置不触发错误检测机制（局部设置）
                //.unchecked()
                .request(new OnPermissionCallback() {
                    @Override
                    public void onGranted(List<String> permissions, boolean all) {
                        if (all) {
                            LogUtil.i("权限获取成功");
                            //资源文件拷贝  语音唤醒
                            copyAssetFolder(MainActivity.this, "ivw/vtn", String.format("%s/ivw/vtn", "/sdcard/AIUI"));
                            initSDK();
                        } else {
                            LogUtil.i("部分权限获取成功" + permissions.toString());

                            XXPermissions.startPermissionActivity(MainActivity.this, permissions);
                        }
                    }

                    @Override
                    public void onDenied(List<String> permissions, boolean never) {
                        if (never) {
                            LogUtil.e("拒绝权限,请手动授予");
                            // 如果是被永久拒绝就跳转到应用权限系统设置页面
                            XXPermissions.startPermissionActivity(MainActivity.this, permissions);
                        } else {
                            LogUtil.e("权限获取失败");
                        }
                    }
                });
    }


    private void initAudioTrack() {
        if (mAudioTrackOperator == null){
            mAudioTrackOperator = new AudioTrackOperator();
            mAudioTrackOperator.createStreamModeAudioTrack();
            mAudioTrackOperator.setStopListener(new AudioTrackOperator.IAudioTrackListener() {
                @Override
                public void onStop() {
                    //播放结束 重新开启录音
                    controlRecord(AIUIConstant.CMD_START_RECORD);
                    //展示水波纹 接收录音
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (mWaveLineView.getVisibility() != View.VISIBLE && WebsocketOperator.getInstance().isOpen()){
                                mWaveLineView.setVisibility(View.VISIBLE);
                                mIvVoiceball.setVisibility(View.GONE);
                                mWaveLineView.startAnim();
                                mWaveLineView.setMoveSpeed(290);
                                mWaveLineView.setVolume(15);
                            }
                        }
                    });

                }
            });
        }
    }

    private void initWebsocket() {
        WebsocketOperator.getInstance().initWebSocket(new WebsocketOperator.IWebsocketListener() {
            @Override
            public void OnTtsData(byte[] audioData, boolean isFinish) {
                // TODO: 2023/1/30 每次都调用play?
                if (mAudioTrackOperator != null) {
                    mAudioTrackOperator.play();
                    mAudioTrackOperator.write(audioData, isFinish);
                }
            }

            @Override
            public void OnNlpData(String nlpString) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        msgList.add(new Msg(nlpString, Msg.TYPE_RECEIVED));
//                            mAdapter.notifyItemInserted(msgList.size()-1);
                        mAdapter.notifyDataSetChanged();
                        mRvChat.scrollToPosition(msgList.size() - 1);
                    }
                });
            }

            @Override
            public void onOpen() {
                if (mAudioTrackOperator != null) {
                    mAudioTrackOperator.play();
                    mAudioTrackOperator.writeSource(MainActivity.this, "audio/xiaozhong_box_wakeUpReply.pcm");
                    //播放过程中AIUI不接收录音
                    controlRecord(AIUIConstant.CMD_STOP_RECORD);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            msgList.add(new Msg("我在呢", Msg.TYPE_RECEIVED));
                            mAdapter.notifyItemInserted(msgList.size() - 1);
//                    mAdapter.notifyDataSetChanged();
                            mRvChat.scrollToPosition(msgList.size() - 1);

                            //展示水波纹
                            mIvVoiceball.setVisibility(View.GONE);
                            mWaveLineView.setVisibility(View.VISIBLE);
                            mWaveLineView.startAnim();
                            mWaveLineView.setVolume(15);
                        }
                    });
                }


            }

            @Override
            public void onError() {
                if (mAudioTrackOperator != null) {
                    mAudioTrackOperator.play();
                    mAudioTrackOperator.writeSource(MainActivity.this, "audio/xiaozhong_box_disconnect.pcm");
                    //播放过程中AIUI不接收录音
                    controlRecord(AIUIConstant.CMD_STOP_RECORD);
                }
            }

            @Override
            public void onClose() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //ws超时后 隐藏水波纹
                        if (mWaveLineView.getVisibility() == View.VISIBLE) {
                            mWaveLineView.stopAnim();
                            mWaveLineView.setVisibility(View.INVISIBLE);
                            mIvVoiceball.setVisibility(View.VISIBLE);
                        }
                    }
                });

            }

        });

    }
    /**
     * 初始化AIUI
     */
    private void createAgent() {
        if (null == mAIUIAgent) {

            // TODO: 2022/12/5 设备鉴权唯一标识
            AIUISetting.setSystemInfo(AIUIConstant.KEY_SERIAL_NUM, "HS6103001A2106000028");

            mAIUIAgent = AIUIAgent.createAgent(this, getAIUIParams(), mAIUIListener);
        }

        if (null == mAIUIAgent) {
            LogUtil.iTag(TAG,"---------create_AIUI FAIL---------");
        } else {
            LogUtil.iTag(TAG,"---------create_AIUI SUCCESS---------");
            controlRecord(AIUIConstant.CMD_START_RECORD);
        }

    }

    private void controlRecord(int cmdConstant) {
        if (mAIUIAgent != null){
            // 打开AIUI内部录音机，开始录音
            String params = "sample_rate=16000,data_type=audio";
            AIUIMessage writeMsg = new AIUIMessage(cmdConstant, 0, 0, params, null );
            mAIUIAgent.sendMessage(writeMsg);
        }
    }

    private String getAIUIParams() {
        String params = "";
        AssetManager assetManager = getResources().getAssets();
        try {
            InputStream ins = assetManager.open( "cfg/aiui_phone.cfg" );
            byte[] buffer = new byte[ins.available()];

            ins.read(buffer);
            ins.close();

            params = new String(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return params;
    }

    AIUIListener mAIUIListener = new AIUIListener() {
        @Override
        public void onEvent(AIUIEvent event) {
            switch (event.eventType) {
                case AIUIConstant.EVENT_CONNECTED_TO_SERVER:
                    LogUtil.iTag(TAG,"AIUI -- 已连接服务器");
                    String uid = event.data.getString("uid");
                    break;

                case AIUIConstant.EVENT_SERVER_DISCONNECTED:
                    LogUtil.iTag(TAG,"AIUI -- 与服务器断开连接");
                    break;

                case AIUIConstant.EVENT_WAKEUP:
                    LogUtil.iTag(TAG,"AIUI -- WAKEUP 进入识别状态");
                    //websocket建联 若已连接状态需要先断开

                    WebsocketOperator.getInstance().connectWebSocket();


                    //播放本地音频文件 欢迎 需要先停止当前播放且释放队列内数据
                    if(mAudioTrackOperator != null){
                        mAudioTrackOperator.shutdownExecutor();
                        mAudioTrackOperator.stop();
                        mAudioTrackOperator.flush();

                        mAudioTrackOperator.isPlaying = false;
                    }

                    break;
                //结果事件（包含听写，语义，离线语法结果）
                case AIUIConstant.EVENT_RESULT://1
                    //结果解析事件
//                    LogUtil.iTag(TAG, "AIUI STATUS --- 结果INFO -- " + event.info);
//                    LogUtil.iTag(TAG, "AIUI STATUS --- 结果DATA -- " + event.data);
                    //听写结果(iat)
                    //语义结果(nlp)
                    //后处理服务结果(tpp)
                    //云端tts结果(tts)
                    //翻译结果(itrans)

                    /**
                     * event.info
                     * {
                     *     "data": [{
                     *         "params": {
                     *             "sub": "iat",
                     *         },
                     *         "content": [{
                     *             "dte": "utf8",
                     *             "dtf": "json",
                     *             "cnt_id": "0"
                     *         }]
                     *     }]
                     * }
                     */
                    try {
                        JSONObject info = new JSONObject(event.info);
                        JSONObject infoData = info.optJSONArray("data").optJSONObject(0);
                        String sub = infoData.optJSONObject("params").optString("sub");
                        JSONObject content = infoData.optJSONArray("content").optJSONObject(0);

                        if (content.has("cnt_id")) {
                            String cnt_id = content.optString("cnt_id");
                            String resultString = new String(event.data.getByteArray(cnt_id), "utf-8");
                            if ("iat".equals(sub) && resultString.length() > 2) {
//                                LogUtil.iTag(TAG, "AIUI EVENT_RESULT --- resultString -- " + resultString);
                                JSONObject result = new JSONObject(resultString);
                                JSONObject text = result.optJSONObject("text");
                                boolean ls = text.optBoolean("ls");//是否结束
                                int sn = text.optInt("sn");//第几句
                                JSONArray ws = text.optJSONArray("ws");
                                StringBuilder currentIatMessage = new StringBuilder();
                                for (int j = 0; j < ws.length(); j++) {
                                    JSONArray cw = ws.optJSONObject(j).optJSONArray("cw");
                                    String w = cw.optJSONObject(0).optString("w");
                                    if (!TextUtils.isEmpty(w)){
                                        currentIatMessage.append(w);
                                    }
                                }

                                LogUtil.iTag(TAG, "AIUI EVENT_RESULT --- iat -- current -- " + currentIatMessage);

                                if (currentIatMessage!= null && currentIatMessage.length()>0){
                                    mIatMessage = currentIatMessage.toString();

                                    if (WebsocketOperator.getInstance().isOpen()){

                                        if(sn == 1){
                                            //刚开始收到音频
                                            if (mWaveLineView.getVisibility() != View.VISIBLE){
                                                mWaveLineView.setVisibility(View.VISIBLE);
                                                mIvVoiceball.setVisibility(View.GONE);
                                                mWaveLineView.startAnim();
                                            }
                                            mWaveLineView.setVolume(70);
                                            mWaveLineView.setMoveSpeed(150);

                                            msgList.add(new Msg(mIatMessage,Msg.TYPE_SEND));
                                        }else{
                                            msgList.set(msgList.size()-1,new Msg(mIatMessage,Msg.TYPE_SEND));
                                        }

//                                    mAdapter.notifyItemInserted(msgList.size()-1);
                                        mAdapter.notifyDataSetChanged();
                                        mRvChat.scrollToPosition(msgList.size()-1);
                                    }

                                }

                                if (ls){
                                    LogUtil.iTag(TAG, "AIUI EVENT_RESULT --- iat -- final -- " + mIatMessage);

                                    WebsocketOperator.getInstance().sendMessage(mIatMessage);

                                    if (WebsocketOperator.getInstance().isOpen()){

                                        mWaveLineView.setVisibility(View.INVISIBLE);
                                        mWaveLineView.setVolume(15);
                                        mWaveLineView.setMoveSpeed(290);
                                        mWaveLineView.stopAnim();
                                        mIvVoiceball.setVisibility(View.VISIBLE);

                                        mAudioTrackOperator.isPlaying = true;
                                        //播放过程中AIUI不接收录音
                                        controlRecord(AIUIConstant.CMD_STOP_RECORD);
                                    }

                                }

                            }
                        }


                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                    break;
                case AIUIConstant.EVENT_SLEEP:
                    LogUtil.iTag(TAG, "AIUI -- 设备进入休眠");
                    break;

                case AIUIConstant.EVENT_START_RECORD:
                    LogUtil.iTag(TAG, "AIUI -- 已开始录音");
                    break;

                case AIUIConstant.EVENT_STOP_RECORD:
                    LogUtil.iTag(TAG, "AIUI -- 已停止录音");
                    break;
                // 状态事件
                case AIUIConstant.EVENT_STATE://3
                    mAIUIState = event.arg1;
                    if (AIUIConstant.STATE_IDLE == mAIUIState) {
                        // 闲置状态，AIUI未开启
                        LogUtil.iTag(TAG, "AIUI -- STATE_IDLE");
                    } else if (AIUIConstant.STATE_READY == mAIUIState) {
                        // AIUI已就绪，等待唤醒
                        LogUtil.iTag(TAG, "AIUI -- STATE_READY");
                    } else if (AIUIConstant.STATE_WORKING == mAIUIState) {
                        // AIUI工作中，可进行交互
                        LogUtil.iTag(TAG, "AIUI -- STATE_WORKING");
                    }
                    break;
                case AIUIConstant.EVENT_TTS: {
                    switch (event.arg1) {
                        case AIUIConstant.TTS_SPEAK_BEGIN:
                            LogUtil.iTag(TAG, "TTS --- 开始播放");
                            break;

                        case AIUIConstant.TTS_SPEAK_PROGRESS:
                            LogUtil.iTag(TAG, "TTS --- 播放进度为" + event.data.getInt("percent"));
                            break;

                        case AIUIConstant.TTS_SPEAK_PAUSED:
                            LogUtil.iTag(TAG, "TTS --- 暂停播放");
                            break;

                        case AIUIConstant.TTS_SPEAK_RESUMED:
                            LogUtil.iTag(TAG, "TTS --- 恢复播放");
                            break;

                        case AIUIConstant.TTS_SPEAK_COMPLETED:
                            LogUtil.iTag(TAG, "TTS --- 播放完成");
                            break;

                        default:
                            break;
                    }
                }
                    break;
                //错误事件
                case AIUIConstant.EVENT_ERROR://2
                    // TODO: 2023/2/3 错误码11217 偶现 需要重新初始化
                    LogUtil.iTag(TAG,"错误: " + event.arg1 + "\n" + event.info);
                    LogUtil.iTag(TAG,"---------error_aiui---------");
                    break;
            }
        }
    };

    public static boolean copyAssetFolder(Context context, String srcName, String dstName) {
        try {
            boolean result = true;
            String fileList[] = context.getAssets().list(srcName);
            if (fileList == null) return false;

            if (fileList.length == 0) {
                result = copyAssetFile(context, srcName, dstName);
            } else {
                File file = new File(dstName);
                result = file.mkdirs();
                for (String filename : fileList) {
                    result &= copyAssetFolder(context, srcName + File.separator + filename, dstName + File.separator + filename);
                }
            }
            return result;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean copyAssetFile(Context context, String srcName, String dstName) {
        try {
            InputStream in = context.getAssets().open(srcName);
            File outFile = new File(dstName);
            if (!outFile.getParentFile().exists()) {
                outFile.getParentFile().mkdirs();
            }
            OutputStream out = new FileOutputStream(outFile);
            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            out.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mWaveLineView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mWaveLineView.onPause();

        //切页面或切后台 ws断联 audiotrack停止播放
        WebsocketOperator.getInstance().close();

        if(mAudioTrackOperator != null){
            mAudioTrackOperator.shutdownExecutor();
            mAudioTrackOperator.stop();
            mAudioTrackOperator.flush();

            mAudioTrackOperator.isPlaying = false;
        }


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mWaveLineView.release();
    }
}