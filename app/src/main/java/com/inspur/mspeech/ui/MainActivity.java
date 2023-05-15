package com.inspur.mspeech.ui;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.allenliu.versionchecklib.v2.AllenVersionChecker;
import com.allenliu.versionchecklib.v2.builder.DownloadBuilder;
import com.allenliu.versionchecklib.v2.builder.UIData;
import com.allenliu.versionchecklib.v2.callback.ForceUpdateListener;
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
import com.iflytek.aiui.AIUIAgent;
import com.iflytek.aiui.AIUIConstant;
import com.iflytek.aiui.AIUIEvent;
import com.iflytek.aiui.AIUIListener;
import com.iflytek.aiui.AIUIMessage;
import com.iflytek.aiui.AIUISetting;
import com.inspur.mspeech.R;
import com.inspur.mspeech.adapter.MsgAdapter;
import com.inspur.mspeech.audio.AudioRecordOperator;
import com.inspur.mspeech.audio.AudioTrackOperator;
import com.inspur.mspeech.bean.Msg;
import com.inspur.mspeech.net.SpeechNet;
import com.inspur.mspeech.utils.Base64Utils;
import com.inspur.mspeech.utils.PermissionUtil;
import com.inspur.mspeech.utils.PrefersTool;
import com.inspur.mspeech.utils.SoftKeyBoardListener;
import com.inspur.mspeech.utils.UIHelper;
import com.inspur.mspeech.websocket.WebsocketOperator;
import com.inspur.mspeech.websocket.WebsocketVADOperator;
import com.inspur.mspeech.wekws.Spot;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import jaygoo.widget.wlv.WaveLineView;
import okhttp3.ResponseBody;
import payfun.lib.basis.Switch;
import payfun.lib.basis.utils.DeviceUtil;
import payfun.lib.basis.utils.LogUtil;
import payfun.lib.basis.utils.ToastUtil;
import payfun.lib.dialog.DialogUtil;
import payfun.lib.dialog.listener.OnDialogButtonClickListener;
import payfun.lib.net.rx.BaseObserver;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "MainActivity";
    private AudioRecordOperator mAudioRecordOperator;
    private RecyclerView mRvChat;
    private List<Msg> msgList = new ArrayList<>();
    private MsgAdapter mAdapter;
    private WaveLineView mWaveLineView;
    private AppCompatImageView mIvVoiceball;

    // AIUI
    private AIUIAgent mAIUIAgent = null;
    // AIUI工作状态
    private int mAIUIState = AIUIConstant.STATE_IDLE;

    public static final String WORK_DIR = "/sdcard/ivw";
    private String keywordPath = WORK_DIR + "/keyword.txt";
    private static final List<String> resource = Arrays.asList("kws.ort");
    //能力
    private final String ABILITY_IVW = "e867a88f2";
    private AiHandle aiHandle;
    private boolean mIsNewMsg = false;//定义变量 控制是否是新的一条消息 用于UI列表展示
    private boolean mIsPlayWord = false;//定义变量 播放什么唤醒词
//    private boolean mIsNewStreamAnswer= false;//定义变量 控制是否是新的一条流式答案 只针对流式答案返回处理
    private StringBuilder streamAnwser = new StringBuilder();

    private int mAiuiCount = 0;//AIUI初始化重试次数
    private String mIatMessage;//iat有效数据
    private boolean isFinalStringEmpty = false;//自研语音识别是否为空
    private HorizontalScrollView horizontalScrollViewMarquee;
    private LinearLayout ll;
    private AppCompatImageView iconInput;
    private EditText etInput;
    private LinearLayout inputLayout;
    private RelativeLayout speechLayout;
    private int chatType = 0; //0 语音交互 1输入交互
    private String inputMessage;
    private AppCompatImageView send;
    private TextView wakeupTip;
    private boolean startWakeup;//wekws只一次唤醒

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

//        InitUtil.init(this);


        //权限
        PermissionUtil.getPermission(this, () -> {
            //资源文件拷贝  语音唤醒
            copyAssetFolder(MainActivity.this, "ivw", String.format("%s/ivw", "/sdcard"));
            if (Switch.WAKE_UP_WEKWS){
                try {
                    assetsInit(this);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                Spot.init(getFilesDir().getPath());
            }
            //初始化SDK
            initSDK();
            //版本升級
            getVersionInfo();
        });

    }

    public void assetsInit(Context context) throws IOException {
        AssetManager assetMgr = context.getAssets();
        // Unzip all files in resource from assets to context.
        // Note: Uninstall the APP will remove the resource files in the context.
        for (String file : assetMgr.list("")) {
            if (resource.contains(file)) {
                File dst = new File(context.getFilesDir(), file);
                if (!dst.exists() || dst.length() == 0) {
                    Log.i(TAG, "Unzipping " + file + " to " + dst.getAbsolutePath());
                    InputStream is = assetMgr.open(file);
                    OutputStream os = new FileOutputStream(dst);
                    byte[] buffer = new byte[4 * 1024];
                    int read;
                    while ((read = is.read(buffer)) != -1) {
                        os.write(buffer, 0, read);
                    }
                    os.flush();
                }
            }
        }
    }

    private void getVersionInfo() {
        SpeechNet.getUpdatgeInfo(new BaseObserver<ResponseBody>() {
            @Override
            public void onNext(@NonNull ResponseBody baseResponse) {
                try {
                    String response = baseResponse.string();
                    JSONObject jsonObject = new JSONObject(response);
                    int code = jsonObject.optInt("code");
                    if (code == 200){
                        JSONArray data = jsonObject.optJSONArray("data");
                        if (data != null && data.length()>0){
                            JSONObject object = data.optJSONObject(0);
                            JSONObject info = object.optJSONObject("pag_info");
//                            String version = info.optString("pag_ver");
                            String versionName = info.optString("pag_name");//取这个值作为code
                            String downloadUrl = info.optString("tmp_access_addr");
                            String desc = info.optString("pag_desc");

                            int newVersionCode = Integer.parseInt(versionName);
                            int versionCode = DeviceUtil.getVersionCode(MainActivity.this);

                            if (newVersionCode>versionCode){
                                update(newVersionCode,downloadUrl,desc);
                            }

                        }
                    }

                } catch (IOException e) {
                    throw new RuntimeException(e);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void onError(@NonNull Throwable e) {

            }
        });
    }

    /**
     * 版本升级
     */
    private void update(int newVersionCode,String downloadUrl,String desc) {
        DownloadBuilder builder= AllenVersionChecker.getInstance()
                .downloadOnly(
                        UIData.create()
                                .setDownloadUrl(downloadUrl)
                                .setTitle("版本升级提示")
                                .setContent(desc.replace(" ","\n"))
                );
        //缓存策略：如果本地有安装包，首先判断与当前运行的程序的versionCode是否不一致，然后判断是否有传入最新的 versionCode，如果传入的versionCode大于本地的，重新从服务器下载，否则使用缓存
        builder.setNewestVersionCode(newVersionCode);//设置当前服务器最新的版本号，供库判断是否使用缓存
        builder.setForceRedownload(true); //默认false 设置为true表示如果本地有安装包缓存也会重新下载apk
        builder.setForceUpdateListener(new ForceUpdateListener() {//用户想要取消下载的时候回调 需要你自己关闭所有界面
            @Override
            public void onShouldForceUpdate() {
                ExitApp();
            }
        });
//        builder.setSilentDownload(true); //静默下载 默认false
        builder.setShowDownloadingDialog(true); //是否显示下载对话框 默认true
        builder.setShowNotification(true);// 是否显示通知栏  默认true
        builder.setRunOnForegroundService(true);//以前台service运行 推荐以前台服务运行更新，防止在后台时，服务被杀死 默认true
        builder.setShowDownloadFailDialog(true);//是否显示失败对话框 默认true
        builder.setDownloadAPKPath("/storage/emulated/0/inspurApk/");//自定义下载路径 默认：/storage/emulated/0/AllenVersionPath/
//        builder.setApkName(apkName);//自定义下载文件名 默认：getPackageName()

        //region 静默下载+直接安装（不会弹出升级对话框）
//        builder.setDirectDownload(true);
//        builder.setShowNotification(false);
//        builder.setShowDownloadingDialog(false);
//        builder.setShowDownloadFailDialog(false);
        //endregion 静默下载+直接安装（不会弹出升级对话框）

        builder.executeMission(this);


    }

    private void initView() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        TextView title = findViewById(R.id.title);
        title.setText("多模态认知交互V"+DeviceUtil.getVersionName(this));
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
                    AudioTrackOperator.getInstance().stop();
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
        mIvVoiceball.setOnClickListener(this);

        horizontalScrollViewMarquee = findViewById(R.id.horizontalScrollViewMarquee);
        ll = findViewById(R.id.ll);
        horizontalScrollViewMarquee.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        for (int i = 0; i < 3; i++) {
            LinearLayout hoLl = (LinearLayout) ((LinearLayout) horizontalScrollViewMarquee.getChildAt(0)).getChildAt(i);
            for (int j = 0; j < 5; j++) {
                TextView tvTip = (TextView) hoLl.getChildAt(j);
                tvTip.setOnClickListener(this);
            }
        }
        // 启动跑马灯效果
        startMarquee();

        inputLayout = findViewById(R.id.input_layout);
        speechLayout = findViewById(R.id.speech_layout);
        iconInput = findViewById(R.id.input_icon);
        iconInput.setOnClickListener(this);
        etInput = findViewById(R.id.et_input);
        etInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    inputSendMessage(etInput.getText().toString().trim());
                    etInput.setText("");
                    return true;
                }
                return false;
            }
        });

        SoftKeyBoardListener.setListener(this, new SoftKeyBoardListener.OnSoftKeyBoardChangeListener() {
            @Override
            public void keyBoardShow(int height) {

            }

            @Override
            public void keyBoardHide(int height) {
                // 键盘已经隐藏
                changeChatType(0);
            }
        });

        send = findViewById(R.id.send);
        send.setOnClickListener(this);
        wakeupTip = findViewById(R.id.wakeup_tip);
    }

    private void inputSendMessage(String message) {
        inputMessage = message;
        if (TextUtils.isEmpty(inputMessage)){
            showCenterToast("请输入文字后点击发送");
            return;
        }
        if (AudioTrackOperator.getInstance().getPlayState() == AudioTrack.PLAYSTATE_PLAYING || AudioTrackOperator.getInstance().isPlaying ){
            //如果当前正在播放 不允许再次发送
            showCenterToast("我正在思考中哦,请等待本次回复完成后再发送");
            return;
        }

        if (WebsocketOperator.getInstance().isOpen()){
            AudioTrackOperator.getInstance().isPlaying = true;
            WebsocketOperator.getInstance().sendMessage(inputMessage);
        }else {
            //输入框输入唤醒
            LogUtil.iTag(TAG, "input wakeup");
            WebsocketOperator.getInstance().connectWebSocket();
        }
        msgList.add(new Msg(inputMessage, Msg.TYPE_SEND));
        mAdapter.notifyItemInserted(msgList.size() - 1);
        mRvChat.scrollToPosition(msgList.size());
    }

    private void showCenterToast(String msg) {
        Toast toast = Toast.makeText(MainActivity.this,msg,Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER,0,0);
        toast.show();
    }


    private void startMarquee() {
        ViewTreeObserver vto = horizontalScrollViewMarquee.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // 获取跑马灯子 View 的数量
//                int childCount = ((LinearLayout) horizontalScrollViewMarquee.getChildAt(0)).getChildCount();
//
//                // 获取跑马灯子 View 的总宽度
//                int childWidth = 0;
//                for (int i = 0; i < childCount; i++) {
//                    childWidth += ((LinearLayout) horizontalScrollViewMarquee.getChildAt(0)).getChildAt(i).getWidth();
//                }

                // 计算跑马灯滚动的距离
                final int distance = ll.getWidth() - horizontalScrollViewMarquee.getWidth();

                // 创建一个 ValueAnimator 对象，用于执行跑马灯动画
                ValueAnimator animator = ValueAnimator.ofInt(0, distance);
                animator.setDuration(10000);
                animator.setInterpolator(new LinearInterpolator());
                animator.setRepeatCount(ValueAnimator.INFINITE);
                animator.setRepeatMode(ValueAnimator.REVERSE);
                animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        // 每次动画更新时，设置水平滚动
                        horizontalScrollViewMarquee.scrollTo((int) animation.getAnimatedValue(), 0);
                    }
                });

                // 启动动画
                animator.start();

                // 移除 OnGlobalLayoutListener
                horizontalScrollViewMarquee.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });

    }

    private void initSDK() {
        //初始化AudioTrack
        initAudioTrack();
        //初始化websocket
        initWebsocket(false);
        //初始化AudioRecord
        initAudioRecord();

        if (Switch.WAKE_UP_WEKWS){
            //wekws唤醒模型
            //开启AudioTrack录音
            if (mAudioRecordOperator != null){
                mAudioRecordOperator.startRecord();
            }
            Spot.reset();
            Spot.startSpot();

            wakeupTip.setText("请说\"Hi 小问\"或点击球形按钮唤醒我...");
        }else {
            //初始化AIKit唤醒库
            initIVW();
        }

        if (Switch.VAD_AIUI){
            // 初始化AIUI
            initAIUI();
        }else {
            //初始化websocket VAD
            initWebsocketVAD();
        }
    }

    /**
     * 初始化AIUI
     */
    private void initAIUI() {
        if (null == mAIUIAgent) {
            AIUISetting.setSystemInfo(AIUIConstant.KEY_SERIAL_NUM, "HS6103001A2106000028");
            mAIUIAgent = AIUIAgent.createAgent(this, getAIUIParams(), mAIUIListener);
        }

        if (null == mAIUIAgent) {
            LogUtil.iTag(TAG,"---------create_AIUI FAIL---------");
        } else {
            LogUtil.iTag(TAG,"---------create_AIUI SUCCESS---------");
//            controlRecord(AIUIConstant.CMD_START_RECORD);
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
//                    wakeUp();
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
                                            if (mIvVoiceball.getVisibility() == View.VISIBLE){
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

                                    AudioTrackOperator.getInstance().isPlaying = true;

                                    if (WebsocketOperator.getInstance().isOpen()) {
                                        mWaveLineView.setVisibility(View.INVISIBLE);
                                        mWaveLineView.setVolume(15);
                                        mWaveLineView.setMoveSpeed(290);
                                        mWaveLineView.stopAnim();
                                        mIvVoiceball.setVisibility(View.VISIBLE);
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
                    LogUtil.iTag(TAG,"AIUI 错误: " + event.arg1 + "\n" + event.info);
                    //失败后重试3次
                    if (mAiuiCount <3){
                        mAiuiCount++;
                        mAIUIAgent = null;
                        initAIUI();
                    }
                    break;
            }
        }
    };

    private void initAudioRecord() {
        mAudioRecordOperator = new AudioRecordOperator();
        mAudioRecordOperator.createAudioRecord(new AudioRecordOperator.RecordListener() {
            @Override
            public void data(byte[] data) {
                if (Switch.WAKE_UP_WEKWS){
                    // 1. add data to C++ interface
                    Spot.acceptWaveform(byteArray2ShortArray(data));

                    String result = Spot.getResult();
                    if (!TextUtils.isEmpty(result)){
                        double probability = Double.parseDouble(result);
                        if (probability > 0.5){
                            //唤醒后此方法多次调用
                            if (startWakeup){
                                LogUtil.iTag(TAG,"WEKWS wakeup " + probability);
                                WebsocketOperator.getInstance().connectWebSocket();
                                startWakeup = false;
                            }
                        }else {
                            if (!startWakeup){
                                startWakeup = true;
                            }
                        }
                    }
                }else {
                    //录音数据回调 流式写入IVW
                    AiInput.Builder dataBuilder = AiInput.builder();
                    dataBuilder.audio("wav", data);
                    AiHelper.getInst().write(dataBuilder.build(), aiHandle);
                }

                if (Switch.VAD_AIUI){
                    if(mAIUIState == AIUIConstant.STATE_WORKING && AudioTrackOperator.getInstance().getPlayState() != AudioTrack.PLAYSTATE_PLAYING && !AudioTrackOperator.getInstance().isPlaying){
                        String params = "data_type=audio,sample_rate=16000";
                        AIUIMessage msg = new AIUIMessage(AIUIConstant.CMD_WRITE, 0, 0, params, data);
                        mAIUIAgent.sendMessage(msg);
                    }
                }else {
                    //在收到VAD开始消息后 同步将采集到的数据发送给vad websocket
                    if (WebsocketVADOperator.getInstance().isOpen() && WebsocketVADOperator.getInstance().startSendMsg){
                        WebsocketVADOperator.getInstance().sendMessage(Base64Utils.base64EncodeToString(data),false);
                    }
                }

            }
        });
    }

    public static short[] byteArray2ShortArray(byte[] data) {
        short[] retVal = new short[data.length/2];
        for (int i = 0; i < retVal.length; i++)
            retVal[i] = (short) ((data[i * 2] & 0xff) | (data[i * 2 + 1] & 0xff) << 8);

        return retVal;
    }
    private void initAudioTrack() {
        AudioTrackOperator.getInstance().createStreamModeAudioTrack();
        AudioTrackOperator.getInstance().setStopListener(new AudioTrackOperator.IAudioTrackListener() {
            @Override
            public void onStop() {
                if (chatType == 1) return;
                if (WebsocketOperator.getInstance().isOpen()){
                    mIsPlayWord = false;
                    if (Switch.VAD_AIUI){
                        //AIUI交互设置成 continuous模式 不需要每次都唤醒
//                        AIUIMessage wakeupMsg = new AIUIMessage(AIUIConstant.CMD_WAKEUP, 0, 0, "", null);
//                        mAIUIAgent.sendMessage(wakeupMsg);
                        chatStateUi();
                    }else {
                        WebsocketVADOperator.getInstance().connectWebSocket();
                    }
                }
            }

            @Override
            public void onStopResource(boolean startVad) {
                //播放结束 vad ws发送start 收到{"status":"ok","type":"server_ready"}后开始语音检测识别
                if (!Switch.VAD_AIUI && startVad) {
                    WebsocketVADOperator.getInstance().sendMessage("start", true);
                }

                //展示水波纹
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mIvVoiceball.getVisibility() == View.VISIBLE && startVad) {
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

    private void initWebsocket(boolean reinit) {
        WebsocketOperator.getInstance().initWebSocket(reinit,new WebsocketOperator.IWebsocketListener() {
            @Override
            public void OnTtsData(byte[] audioData, boolean isFinish) {
                AudioTrackOperator.getInstance().write(audioData, isFinish);
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
            public void onNlpStreamData(String nlpStream, boolean isFinish) {
                //流式结果返回
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (isFinish){
                            streamAnwser.delete(0, streamAnwser.length());
                            return;
                        }
                        if (TextUtils.isEmpty(nlpStream)){
                            return;
                        }
                        if (TextUtils.isEmpty(streamAnwser)){
                            //新的数据
                            streamAnwser.append(nlpStream);
                            msgList.add(new Msg(streamAnwser.toString(), Msg.TYPE_RECEIVED));
                        }else {
                            streamAnwser.append(nlpStream);
                            msgList.set(msgList.size()-1,new Msg(streamAnwser.toString(), Msg.TYPE_RECEIVED));
                        }
                        mAdapter.notifyDataSetChanged();
                        mRvChat.scrollToPosition(msgList.size());
                    }
                });
            }

            @Override
            public void onOpen() {
                mIsPlayWord = true;
                isFinalStringEmpty = false;
                if (PrefersTool.getModelSwitch()){
                    streamAnwser.delete(0, streamAnwser.length());
                }

                //输入交互模式 不走语音识别相关逻辑
                if (chatType == 1){
                    if (!TextUtils.isEmpty(inputMessage)){
                        AudioTrackOperator.getInstance().isPlaying = true;
                        WebsocketOperator.getInstance().sendMessage(inputMessage);
                    }
                }else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            iconInput.setVisibility(View.GONE);
                            wakeupTip.setVisibility(View.GONE);
                        }
                    });

                    if (Switch.VAD_AIUI){//如果走AIUI语音识别渠道 此时需要唤醒AIUI
                        AIUIMessage wakeupMsg = new AIUIMessage(AIUIConstant.CMD_WAKEUP, 0, 0, "", null);
                        mAIUIAgent.sendMessage(wakeupMsg);
                        chatStateUi();
                    }else {//如果走自研语音唤醒 建联成功后再建联VAD websocket
                        WebsocketVADOperator.getInstance().connectWebSocket();
                    }
                }
            }

            @Override
            public void onError() {
                AudioTrackOperator.getInstance().writeSource(MainActivity.this, "audio/xiaozhong_box_disconnect.pcm",true);
            }

            @Override
            public void onClose(boolean isLogin) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        if (isLogin) {//token为空或失效 跳转登录
                            jumpToLogin();
                        }

                        if (chatType == 1) return;

                        //ws超时后 隐藏水波纹
                        if (mIvVoiceball.getVisibility() != View.VISIBLE) {
                            mWaveLineView.stopAnim();
                            mWaveLineView.setVisibility(View.INVISIBLE);
                            mIvVoiceball.setVisibility(View.VISIBLE);
                        }

                        if (Switch.VAD_AIUI) {
                            //AIUI休眠
                            AIUIMessage wakeupMsg = new AIUIMessage(AIUIConstant.CMD_RESET_WAKEUP, 0, 0, "", null);
                            mAIUIAgent.sendMessage(wakeupMsg);
                        } else {
                            //断联vad
                            WebsocketVADOperator.getInstance().close();
                        }
                        iconInput.setVisibility(View.VISIBLE);
                        wakeupTip.setVisibility(View.VISIBLE);
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
                handleFinalData(finalString);
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
            public void onParaData(String paraString) {
                //模型2回调 最终结果
                //最终识别结果 发给语音交互webscocket
                handleFinalData(paraString);
            }

            @Override
            public void onOpen() {
                mIsNewMsg = true;
                if (isFinalStringEmpty){//如果是因为本次识别结果为空导致的自动重连 不需要更新ui 直接发送start
                    WebsocketVADOperator.getInstance().sendMessage("start", true);
                }else {
                    chatStateUi();
                }
            }

            @Override
            public void onError() {
                //建联失败直接播报网络异常
                AudioTrackOperator.getInstance().writeSource(MainActivity.this, "audio/xiaozhong_box_disconnect.pcm",true);
            }

            @Override
            public void onClose() {
                //如果当前识别到的结果是空,则需要重新开启新一轮检测
                if (isFinalStringEmpty){
                    if (WebsocketOperator.getInstance().isOpen()){
                        WebsocketVADOperator.getInstance().connectWebSocket();
                    }
                }
            }
        });
    }

    private void handleFinalData(String paraString) {
        if(TextUtils.isEmpty(paraString)){
            //如果最终识别结果为空 自动重新唤醒 不更新ui
            isFinalStringEmpty = true;
            return;
        }else {
            isFinalStringEmpty = false;
        }
        WebsocketOperator.getInstance().sendMessage(paraString);
        if (WebsocketOperator.getInstance().isOpen()){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(mIsNewMsg){
                        msgList.add(new Msg(paraString,Msg.TYPE_SEND));
                    }else {
                        msgList.set(msgList.size()-1,new Msg(paraString,Msg.TYPE_SEND));
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

    /**
     * 对话状态ui更新
     */
    private void chatStateUi() {
        if (mIsPlayWord) {//语音唤醒
            AudioTrackOperator.getInstance().writeSource(MainActivity.this, "audio/" + PrefersTool.getVoiceName() + "_box_wakeUpReply.pcm",true);
        } else {//手动唤醒或自动唤醒
            AudioTrackOperator.getInstance().writeSource(MainActivity.this, "audio/ding.pcm",true);
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mIsPlayWord) {
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
                if (code == 18708){//授权量不足

                }
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
        Intent intent = new Intent();
        switch (item.getItemId()){
            case R.id.setting_menu_login:
                intent.setClass(MainActivity.this, LoginActivity.class);
                intentActivityResultLauncher.launch(intent);
                break;

            case R.id.setting_menu_about:
                intent.setClass(MainActivity.this, AboutActivity.class);
                startActivity(intent);
                break;

            case R.id.setting_menu_setting:
                intent.setClass(MainActivity.this, SettingActivity.class);
                intentActivityResultLauncher.launch(intent);
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
                intentActivityResultLauncher.launch(intent);
                return false;
            }
        },null);

    }

    public ActivityResultLauncher<Intent> intentActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),result -> {
        //设置界面回来重新init
        initWebsocket(true);
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

        if (mAudioRecordOperator != null){
            mAudioRecordOperator.startRecord();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mWaveLineView.onPause();

        //切页面或切后台 ws断联
        WebsocketOperator.getInstance().close();

        //audiotrack停止播放
        AudioTrackOperator.getInstance().stop();

        if (mAudioRecordOperator !=null){
            mAudioRecordOperator.stopRecord();
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mWaveLineView.release();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.iv_voiceball:
                //手动唤醒
                LogUtil.iTag(TAG, "click wakeup");
                //先后建联两个websocket 如果是自研语音识别 只建联一个
                WebsocketOperator.getInstance().connectWebSocket();
                break;
            case R.id.input_icon:
                changeChatType(1);
                etInput.requestFocus();
                DeviceUtil.showInputMethod(this,etInput);
                break;
            case R.id.send:
                inputSendMessage(etInput.getText().toString().trim());
                etInput.setText("");
                break;
            case R.id.tv_1:
            case R.id.tv_2:
            case R.id.tv_3:
            case R.id.tv_4:
            case R.id.tv_5:
            case R.id.tv_6:
            case R.id.tv_7:
            case R.id.tv_8:
            case R.id.tv_9:
            case R.id.tv_10:
            case R.id.tv_11:
            case R.id.tv_12:
            case R.id.tv_13:
            case R.id.tv_14:
            case R.id.tv_15:
                if (AudioTrackOperator.getInstance().getPlayState() != AudioTrack.PLAYSTATE_PLAYING && !AudioTrackOperator.getInstance().isPlaying){
                    changeChatType(1);
                    etInput.requestFocus();
                    DeviceUtil.showInputMethod(this,etInput);
                }
                String text = ((TextView) v).getText().toString();
                inputSendMessage(text);
                break;
            default:
                break;
        }
    }

    private void changeChatType(int type) {
        chatType = type;//输入模式
        if (type == 0) {//语音
            wakeupTip.setVisibility(View.VISIBLE);
            mAudioRecordOperator.startRecord();
            inputLayout.setVisibility(View.GONE);
            speechLayout.setVisibility(View.VISIBLE);
            if (mIvVoiceball.getVisibility() != View.VISIBLE) {
                mWaveLineView.stopAnim();
                mWaveLineView.setVisibility(View.INVISIBLE);
                mIvVoiceball.setVisibility(View.VISIBLE);
                iconInput.setVisibility(View.VISIBLE);
            }
        } else {//输入
            wakeupTip.setVisibility(View.GONE);
            mAudioRecordOperator.stopRecord();
            inputLayout.setVisibility(View.VISIBLE);
            speechLayout.setVisibility(View.GONE);
        }
    }


}