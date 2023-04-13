package com.inspur.mspeech.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;

import com.bumptech.glide.Glide;
import com.iflytek.aikit.core.AiHandle;
import com.iflytek.aikit.core.AiHelper;
import com.iflytek.aikit.core.AiInput;
import com.iflytek.aikit.core.AiRequest;
import com.iflytek.aikit.core.AiResponse;
import com.iflytek.aikit.core.AiResponseListener;
import com.iflytek.aikit.core.CoreListener;
import com.iflytek.aikit.core.ErrType;
import com.iflytek.aikit.core.JLibrary;
import com.inspur.mspeech.R;
import com.inspur.mspeech.adapter.MsgAdapter;
import com.inspur.mspeech.audio.AudioRecordOperator;
import com.inspur.mspeech.audio.AudioTrackOperator;
import com.inspur.mspeech.bean.Msg;
import com.inspur.mspeech.net.SpeechNet;
import com.inspur.mspeech.utils.Base64Utils;
import com.inspur.mspeech.utils.PermissionUtil;
import com.inspur.mspeech.utils.PrefersTool;
import com.inspur.mspeech.utils.UIHelper;
import com.inspur.mspeech.websocket.WebsocketOperator;
import com.inspur.mspeech.websocket.WebsocketVADOperator;

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
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import jaygoo.widget.wlv.WaveLineView;
import payfun.lib.basis.utils.InitUtil;
import payfun.lib.basis.utils.LogUtil;
import payfun.lib.dialog.DialogUtil;
import payfun.lib.dialog.listener.OnDialogButtonClickListener;

public class MainActivity extends AppCompatActivity{
    private static final String TAG = "MainActivity";
    private AudioTrackOperator mAudioTrackOperator;
    private AudioRecordOperator mAudioRecordOperator;
    private RecyclerView mRvChat;
    private List<Msg> msgList = new ArrayList<>();
    private MsgAdapter mAdapter;
    private WaveLineView mWaveLineView;
    private AppCompatImageView mIvVoiceball;

    public static final String WORK_DIR = "/sdcard/ivw";
    private String keywordPath = WORK_DIR + "/keyword.txt";
    //能力
    private final String ABILITY_IVW = "e867a88f2";
    private AiHandle aiHandle;
    private boolean mIsNewMsg = false;//定义变量 控制是否是新的一条消息 用于UI列表展示
    private boolean mIsPlayWord = false;//定义变量 播放什么唤醒词

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //设置无Title
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //设置应用全屏,必须写在setContentView方法前面
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main_new);
        UIHelper.hideBottomUIMenu(this);

        initView();

        InitUtil.init(this);

        //网络初始化
        SpeechNet.init();


        //权限
        PermissionUtil.getPermission(this, () -> {
            //资源文件拷贝  语音唤醒
            copyAssetFolder(MainActivity.this, "ivw", String.format("%s/ivw", "/sdcard"));
            //初始化SDK
            initSDK();
        });


    }

    private void initView() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        mWaveLineView = findViewById(R.id.waveLineView);
        mRvChat = findViewById(R.id.recyclerview_chat);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mAdapter = new MsgAdapter(msgList);

        mRvChat.setLayoutManager(layoutManager);
        mRvChat.setAdapter(mAdapter);

        mRvChat.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    //点击屏幕暂停播放
                    if(mAudioTrackOperator != null){
                        mAudioTrackOperator.shutdownExecutor();
                        mAudioTrackOperator.stop();
                        mAudioTrackOperator.flush();

                        mAudioTrackOperator.isPlaying = false;
                    }

                    WebsocketOperator.getInstance().close();
                }
                return false;
            }
        });
        mIvVoiceball = findViewById(R.id.iv_voiceball);
        Glide.with(this)
                .load(R.drawable.gif_voice_ball)
                .placeholder(R.drawable.voice_ball)
                .into(mIvVoiceball);
        mIvVoiceball.setOnClickListener(view -> {
            //手动唤醒
            LogUtil.iTag(TAG, "click wakeup");
            //先后建联两个websocket
            WebsocketOperator.getInstance().connectWebSocket();
        });

    }


    private void initSDK() {
        //初始化AudioTrack
        initAudioTrack();
        //初始化websocket
        initWebsocket(false);
        //初始化websocket VAD
        initWebsocketVAD();
        //初始化AudioRecord
        initAudioRecord();
        //初始化AIKit唤醒库
        initIVW();

    }

    private void initAudioRecord() {
        mAudioRecordOperator = new AudioRecordOperator();
        mAudioRecordOperator.createAudioRecord(new AudioRecordOperator.RecordListener() {
            @Override
            public void data(byte[] data) {
                //录音数据回调 流式写入IVW
                AiInput.Builder dataBuilder = AiInput.builder();
                dataBuilder.audio("wav", data);
                AiHelper.getInst().write(dataBuilder.build(), aiHandle);

                //在收到VAD开始消息后 同步将采集到的数据发送给vad websocket
                if (WebsocketVADOperator.getInstance().isOpen() && WebsocketVADOperator.getInstance().startSendMsg){
                    WebsocketVADOperator.getInstance().sendMessage(Base64Utils.base64EncodeToString(data),false);
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
                    //重新建联VAD websocket
                    if (WebsocketOperator.getInstance().isOpen()){
                        mIsPlayWord = false;
                        WebsocketVADOperator.getInstance().connectWebSocket();
                    }

                }

                @Override
                public void onStopResource(boolean startVad) {
                    //播放结束 vad ws发送start 收到{"status":"ok","type":"server_ready"}后开始语音检测识别
                    if (startVad){
                        WebsocketVADOperator.getInstance().sendMessage("start",true);
                    }

                    //展示水波纹
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (mIvVoiceball.getVisibility() == View.VISIBLE && startVad){
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

    private void initWebsocket(boolean reinit) {
        WebsocketOperator.getInstance().initWebSocket(reinit,new WebsocketOperator.IWebsocketListener() {
            @Override
            public void OnTtsData(byte[] audioData, boolean isFinish) {
                // TODO: 2023/1/30 每次都调用play CMD_STOP_RECORD?
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
                        mRvChat.scrollToPosition(msgList.size());
                    }
                });
            }

            @Override
            public void onOpen() {
                //建联成功后再建联VAD websocket
                WebsocketVADOperator.getInstance().connectWebSocket();

                mIsPlayWord = true;
            }

            @Override
            public void onError() {
                if (mAudioTrackOperator != null) {
                    mAudioTrackOperator.play();
                    mAudioTrackOperator.writeSource(MainActivity.this, "audio/xiaozhong_box_disconnect.pcm");
                }
            }

            @Override
            public void onClose(boolean isLogin) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //ws超时后 隐藏水波纹
                        if (mIvVoiceball.getVisibility() != View.VISIBLE) {
                            mWaveLineView.stopAnim();
                            mWaveLineView.setVisibility(View.INVISIBLE);
                            mIvVoiceball.setVisibility(View.VISIBLE);
                        }

                        //断联vad
                        WebsocketVADOperator.getInstance().close();

                        if (isLogin){//token为空或失效 跳转登录
                            jumpToLogin();
                        }
                    }
                });

            }

        });

    }

    private void initWebsocketVAD() {
        WebsocketVADOperator.getInstance().initWebSocket(new WebsocketVADOperator.IWebsocketListener() {

            @Override
            public void OnFinalData(String finalString) {
                //最终识别结果 发给语音交互webscocket
                if(TextUtils.isEmpty(finalString)){
                    return;
                }
                WebsocketOperator.getInstance().sendMessage(finalString);
                if (WebsocketOperator.getInstance().isOpen()){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(mIsNewMsg){
                                msgList.add(new Msg(finalString,Msg.TYPE_SEND));
                            }else {
                                msgList.set(msgList.size()-1,new Msg(finalString,Msg.TYPE_SEND));
                            }
                            mAdapter.notifyDataSetChanged();
                            mRvChat.scrollToPosition(msgList.size());

                            mWaveLineView.setVisibility(View.INVISIBLE);
                            mWaveLineView.setVolume(15);
                            mWaveLineView.setMoveSpeed(290);
                            mWaveLineView.stopAnim();
                            mIvVoiceball.setVisibility(View.VISIBLE);
                        }
                    });
                }
            }

            @Override
            public void OnVadData(String vadString) {
                //流式识别结果
                if (WebsocketOperator.getInstance().isOpen()){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(mIsNewMsg){
                                mIsNewMsg = false;
                                if (mIvVoiceball.getVisibility() == View.VISIBLE){
                                    mWaveLineView.setVisibility(View.VISIBLE);
                                    mIvVoiceball.setVisibility(View.GONE);
                                    mWaveLineView.startAnim();
                                }
                                mWaveLineView.setVolume(70);
                                mWaveLineView.setMoveSpeed(150);

                                msgList.add(new Msg(vadString,Msg.TYPE_SEND));
                                mAdapter.notifyDataSetChanged();
                                mRvChat.scrollToPosition(msgList.size());
                            }else{
                                msgList.set(msgList.size()-1,new Msg(vadString,Msg.TYPE_SEND));
                                mAdapter.notifyDataSetChanged();
                                mRvChat.scrollToPosition(msgList.size());
                            }
                        }
                    });
                }

            }

            @Override
            public void onOpen() {

                mIsNewMsg = true;

                if (mAudioTrackOperator != null) {
                    //播放本地音频文件 欢迎 需要先停止当前播放且释放队列内数据
                    if(mAudioTrackOperator != null){
                        mAudioTrackOperator.shutdownExecutor();
                        mAudioTrackOperator.stop();
                        mAudioTrackOperator.flush();

                        mAudioTrackOperator.isPlaying = false;
                    }

                    mAudioTrackOperator.play();

                    if (mIsPlayWord){//语音唤醒
                        mAudioTrackOperator.writeSource(MainActivity.this, "audio/"+PrefersTool.getVoiceName()+"_box_wakeUpReply.pcm");
                    }else {//手动唤醒或自动唤醒
                        mAudioTrackOperator.writeSource(MainActivity.this, "audio/ding.pcm");
                    }


                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (mIsPlayWord){
                                msgList.add(new Msg("我在呢", Msg.TYPE_RECEIVED));
                                mAdapter.notifyItemInserted(msgList.size() - 1);
//                                 mAdapter.notifyDataSetChanged();
                                mRvChat.scrollToPosition(msgList.size());
                            }
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
                //建联失败直接播报网络异常
                if (mAudioTrackOperator != null) {
                    mAudioTrackOperator.play();
                    mAudioTrackOperator.writeSource(MainActivity.this, "audio/xiaozhong_box_disconnect.pcm");
                }
            }

            @Override
            public void onClose() {

            }
        });
    }


    private void initIVW() {
        //授权参数
        final JLibrary.Params params = JLibrary.Params.builder()
                .appId(getResources().getString(R.string.appId))
                .apiKey(getResources().getString(R.string.apiKey))
                .apiSecret(getResources().getString(R.string.apiSecret))
                .workDir(WORK_DIR)
                .logOpen(false)
                .iLogOpen(false)
                .recordOpen(false)
                .build();
        //能力参数
        //SDK状态回调监听
        JLibrary.getInst().registerListener(coreListener);
        //能力输出回调监听
        JLibrary.getInst().registerListener(aiResponseListener);
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                JLibrary.getInst().initEntry(MainActivity.this, params);
//            }
//        }).start();
        JLibrary.getInst().initEntry(MainActivity.this, params);
    }


    private void initEngine() {
        int ret = AiHelper.getInst().engineInitNoParams(ABILITY_IVW);
        if (ret != 0) {
            LogUtil.iTag(TAG,"IVW 引擎初始化失败 " + ret);
        } else {
            LogUtil.iTag(TAG,"IVW 引擎初始化成功 " + ret);

            //加载数据
            loadData();
            //开始执行
            startIVW();
        }
    }

    private CoreListener coreListener = new CoreListener() {
        @Override
        public void onAuthStateChange(ErrType type, final int code) {
            if (code == 0) {
                LogUtil.iTag(TAG,"IVW 初始化成功");
                new Thread() {
                    public void run() {
                        try {
                            sleep(200);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        //引擎初始化
                        initEngine();
                    }
                }.start();
            } else {
                LogUtil.iTag(TAG,"IVW 初始化失败 " + code);
            }
        }
    };

    private AiResponseListener aiResponseListener = new AiResponseListener() {
        //能力执行结果返回
        @Override
        public void onResult(String ability, int handleID, List<AiResponse> outputData, Object usrContext) {
            LogUtil.iTag(TAG, "IVW onResult:handleID:" + handleID + ",size:" + outputData.size());
            if (null != outputData && outputData.size() > 0) {
                for (int i = 0; i < outputData.size(); i++) {
                    String key = outputData.get(i).getKey();

                    byte[] bytes = outputData.get(i).getValue();
                    if (bytes == null) {
                        continue;
                    }
                    String result = new String(bytes);
                    //ivw唤醒
                    if (key.equals("func_wake_up") || key.equals("func_pre_wakeup")) {
                        LogUtil.iTag(TAG, "IVW wakeup:" + result);
                        //先后建联两个websocket
                        WebsocketOperator.getInstance().connectWebSocket();
                    }
                }
            }
        }

        //事件回调
        @Override
        public void onEvent(String ability, int handleID, int event, List<AiResponse> eventData, Object usrContext) {
            // event: 0=未知；1=开始；2=结束；3=超时；4=进度；
            LogUtil.iTag(TAG, "IVW onEvent: " + event);
        }

        //错误通知，能力执行终止
        @Override
        public void onError(String ability, int handleID, int err, String msg, Object usrContext) {
            LogUtil.iTag(TAG, "IVW onError: " + err);
        }
    };

    private void loadData() {
        //加载数据 唤醒词
        AiInput.Builder customBuilder = AiInput.builder();
        /**
         * 可多次调用 传入多个文件
         * key 数据标识
         * value 数据内容
         * index 数据索引,用户可自定义设置
         */
        customBuilder.customText("key_word", keywordPath, 0);
        //数据加载
        int i = AiHelper.getInst().loadData(ABILITY_IVW, customBuilder.build());

        if (i != 0) {
            LogUtil.iTag(TAG,"IVW loadData 失败：" + i);
            return;
        } else {
            LogUtil.iTag(TAG,"IVW loadData 成功: " + i);
        }


        //指定要使用的个性化数据集合，未调用，则默认使用所有loadData加载的数据
        int[] indexs = {0};
        /**
         * indexs 个性化数据索引数组
         */
        //数据落盘
        AiHelper.getInst().specifyDataSet(ABILITY_IVW,"key_word",indexs);
    }

    private void startIVW() {
        AiInput.Builder paramBuilder = AiRequest.builder();
        paramBuilder.param("wdec_param_nCmThreshold", "0 0:800");//门限值 最小长度:0，最大长度:1024
        paramBuilder.param("gramLoad", true);//更新自定义唤醒词
        //启动会话，流式接口 开始计算能力 如果开启成功 就可以开启AudioTrack录音 流式写入数据
        aiHandle = AiHelper.getInst().start(ABILITY_IVW, paramBuilder.build(), null);
        if (aiHandle.isSuccess()) {
            LogUtil.iTag(TAG,"IVW start 成功：" + aiHandle.getCode());
            //开启AudioTrack录音
            if (mAudioRecordOperator != null){
                mAudioRecordOperator.startRecord();
            }
        } else {
            LogUtil.iTag(TAG,"IVW start 失败：" + aiHandle.getCode());
            return;
        }
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
            case R.id.setting_menu_login:
//                jumpToLogin();
                Intent intent3 = new Intent(MainActivity.this, LoginActivity.class);
                intentActivityResultLauncher3.launch(intent3);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void jumpToLogin() {
        DialogUtil.showTwoBtnDialog(MainActivity.this, "请先登录", new OnDialogButtonClickListener() {
            @Override
            public boolean onClick(DialogFragment baseDialog, View v) {
                PrefersTool.setAccesstoken("");
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                intentActivityResultLauncher3.launch(intent);
                return false;
            }
        },null);

    }

    public ActivityResultLauncher<Intent> intentActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),result -> {
        //设置音色页面回来 重新init ws
        initWebsocket(!TextUtils.equals(PrefersTool.getVoiceName(),WebsocketOperator.getInstance().voiceName));
    });


    public ActivityResultLauncher<Intent> intentActivityResultLauncher3 = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),result -> {

        if (result.getResultCode() == RESULT_OK){
            //新建websocket设置token
            initWebsocket(true);
        }
    });

    private void ExitApp() {
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
    }


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

        //切页面或切后台 ws断联
        WebsocketOperator.getInstance().close();

        //audiotrack停止播放
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