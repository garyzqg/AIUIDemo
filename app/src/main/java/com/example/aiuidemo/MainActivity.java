package com.example.aiuidemo;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.iflytek.aiui.AIUIAgent;
import com.iflytek.aiui.AIUIConstant;
import com.iflytek.aiui.AIUIEvent;
import com.iflytek.aiui.AIUIListener;
import com.iflytek.aiui.AIUIMessage;
import com.iflytek.aiui.AIUISetting;

import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "MainActivity";
    // AIUI
    private AIUIAgent mAIUIAgent = null;
    // AIUI工作状态
    private int mAIUIState = AIUIConstant.STATE_IDLE;

    private AudioTrackOperator mAudioTrackOperator;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.init_sdk).setOnClickListener(this);
        findViewById(R.id.btnRec).setOnClickListener(this);
        findViewById(R.id.btnStop).setOnClickListener(this);
        findViewById(R.id.btnSave).setOnClickListener(this);
        findViewById(R.id.btnTTS).setOnClickListener(this);
        findViewById(R.id.AudioStart).setOnClickListener(this);
        findViewById(R.id.AudioStop).setOnClickListener(this);
        findViewById(R.id.AudioPause).setOnClickListener(this);
        findViewById(R.id.getState).setOnClickListener(this);
        findViewById(R.id.writeTest).setOnClickListener(this);
        findViewById(R.id.silent_update).setOnClickListener(this);

        // TODO: 2022/12/5 动态权限申请

        //资源文件拷贝  语音唤醒
        // TODO: 2022/12/6 如果要更新文件 需要写删除逻辑
        copyFilesFromAssets(this,"ivw/vtn", "/sdcard/AIUI/ivw/vtn");

        InitUtil.init(this);

//        Intent autostartSettingIntent = getAutostartSettingIntent(this);
//        startActivity(autostartSettingIntent);



        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> localApps = getPackageManager().queryIntentActivities(mainIntent, 0);

        Iterator iterator = localApps.iterator();
        while (iterator.hasNext()) {
            ResolveInfo resolveInfo = (ResolveInfo) iterator.next();
            String packageName = resolveInfo.activityInfo.packageName;
            if (packageName.equals(getApplication().getPackageName())) {
                iterator.remove();
            }
        }

    }

    /**
     * 获取自启动管理页面的Intent
     *
     * @param context context
     * @return 返回自启动管理页面的Intent
     */
    public static Intent getAutostartSettingIntent(Context context) {
        ComponentName componentName = null;
        String brand = Build.MANUFACTURER;
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        switch (brand.toLowerCase()) {
            case "samsung"://三星
                componentName = new ComponentName("com.samsung.android.sm", "com.samsung.android.sm.app.dashboard.SmartManagerDashBoardActivity");
                break;
            case "huawei"://华为
                Log.e("自启动管理 >>>>", "getAutostartSettingIntent: 华为");
                componentName = new ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.appcontrol.activity.StartupAppControlActivity");
//            componentName = new ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity");//目前看是通用的
                break;
            case "xiaomi"://小米
//                componentName = new ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity");
                componentName = new ComponentName("com.android.settings","com.android.settings.BackgroundApplicationsManager");
                break;
            case "vivo"://VIVO
//            componentName = new ComponentName("com.iqoo.secure", "com.iqoo.secure.safaguard.PurviewTabActivity");
                componentName = new ComponentName("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity");
                break;
            case "oppo"://OPPO
//            componentName = new ComponentName("com.oppo.safe", "com.oppo.safe.permission.startup.StartupAppListActivity");
                componentName = new ComponentName("com.coloros.oppoguardelf", "com.coloros.powermanager.fuelgaue.PowerUsageModelActivity");
                break;
            case "yulong":
            case "360"://360
                componentName = new ComponentName("com.yulong.android.coolsafe", "com.yulong.android.coolsafe.ui.activity.autorun.AutoRunListActivity");
                break;
            case "meizu"://魅族
                componentName = new ComponentName("com.meizu.safe", "com.meizu.safe.permission.SmartBGActivity");
                break;
            case "oneplus"://一加
                componentName = new ComponentName("com.oneplus.security", "com.oneplus.security.chainlaunch.view.ChainLaunchAppListActivity");
                break;
            case "letv"://乐视
                intent.setAction("com.letv.android.permissionautoboot");
            default://其他
                intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
                intent.setData(Uri.fromParts("package", context.getPackageName(), null));
                break;
        }
        intent.setComponent(componentName);
        return intent;
    }


    /**
     * 复制assets中的文件到指定目录
     *
     * @param context     上下文
     * @param assetsPath  assets资源路径
     * @param storagePath 目标文件夹的路径
     */
    public static void copyFilesFromAssets(Context context, String assetsPath, String storagePath) {
        String temp = "";

        if (TextUtils.isEmpty(storagePath)) {
            return;
        } else if (storagePath.endsWith(File.separator)) {
            storagePath = storagePath.substring(0, storagePath.length() - 1);
        }

        if (TextUtils.isEmpty(assetsPath) || assetsPath.equals(File.separator)) {
            assetsPath = "";
        } else if (assetsPath.endsWith(File.separator)) {
            assetsPath = assetsPath.substring(0, assetsPath.length() - 1);
        }

        AssetManager assetManager = context.getAssets();
        try {
            File file = new File(storagePath);
            if (!file.exists()) {//如果文件夹不存在，则创建新的文件夹
                file.mkdirs();
            }

            // 获取assets目录下的所有文件及目录名
            String[] fileNames = assetManager.list(assetsPath);
            if (fileNames.length > 0) {//如果是目录 apk
                for (String fileName : fileNames) {
                    if (!TextUtils.isEmpty(assetsPath)) {
                        temp = assetsPath + File.separator + fileName;//补全assets资源路径
                    }

                    String[] childFileNames = assetManager.list(temp);
                    if (!TextUtils.isEmpty(temp) && childFileNames.length > 0) {//判断是文件还是文件夹：如果是文件夹
                        copyFilesFromAssets(context, temp, storagePath + File.separator + fileName);
                    } else {//如果是文件
                        InputStream inputStream = assetManager.open(temp);
                        readInputStream(storagePath + File.separator + fileName, inputStream);
                    }
                }
            } else {//如果是文件 doc_test.txt或者apk/app_test.apk
                InputStream inputStream = assetManager.open(assetsPath);
                if (assetsPath.contains(File.separator)) {//apk/app_test.apk
                    assetsPath = assetsPath.substring(assetsPath.lastIndexOf(File.separator), assetsPath.length());
                }
                readInputStream(storagePath + File.separator + assetsPath, inputStream);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 读取输入流中的数据写入输出流
     *
     * @param storagePath 目标文件路径
     * @param inputStream 输入流
     */
    public static void readInputStream(String storagePath, InputStream inputStream) {
        File file = new File(storagePath);
        try {
            if (!file.exists()) {
                // 1.建立通道对象
                FileOutputStream fos = new FileOutputStream(file);
                // 2.定义存储空间
                byte[] buffer = new byte[inputStream.available()];
                // 3.开始读文件
                int lenght = 0;
                while ((lenght = inputStream.read(buffer)) != -1) {// 循环从输入流读取buffer字节
                    // 将Buffer中的数据写到outputStream对象中
                    fos.write(buffer, 0, lenght);
                }
                fos.flush();// 刷新缓冲区
                // 4.关闭流
                fos.close();
                inputStream.close();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }


    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.init_sdk:
                initSDK();
                break;
            case R.id.btnRec:
//                startReord();
                // 先发送唤醒消息，改变AIUI内部状态，只有唤醒状态才能接收语音输入
                if( AIUIConstant.STATE_WORKING != mAIUIState ){
                    AIUIMessage wakeupMsg = new AIUIMessage(AIUIConstant.CMD_WAKEUP, 0, 0, "", null);
                    mAIUIAgent.sendMessage(wakeupMsg);
                }

                // 打开AIUI内部录音机，开始录音
                String params = "sample_rate=16000,data_type=audio";
                AIUIMessage writeMsg = new AIUIMessage( AIUIConstant.CMD_START_RECORD, 0, 0, params, null );
                mAIUIAgent.sendMessage(writeMsg);
                break;
            case R.id.btnStop:
//                stopRecord();
                break;
            case R.id.btnSave:
//                saveAudio();
                break;
            case R.id.writeTest:
//                writeAudioTest();
                break;
            case R.id.btnTTS:

                break;
            case R.id.AudioStart:
                mAudioTrackOperator.play();
                break;
            case R.id.AudioStop:
                mAudioTrackOperator.stop();
                mAudioTrackOperator.release();
                break;
            case R.id.AudioPause:
                mAudioTrackOperator.pause();
//                mAudioTrackOperator.flush();
                break;

            case R.id.getState:
                int playState = mAudioTrackOperator.getPlayState();
                int state = mAudioTrackOperator.getState();
                LogUtil.iTag(TAG, "AIUI getState -- playState:" + playState + " state:" + state);
                break;
            case R.id.silent_update:
                SilentUpdateOperator.installSilently("/data/local/tmp/app-release.apk");
//                SilentUpdateOperator.installSilently("/sdcard/Download/app-release.apk");
//                SilentUpdateOperator.execCommand(new String[]{"pm install -r " + "/sdcard/Download/app-release.apk"},false,true);
                break;
            default:
                break;
        }
    }

    private void initSDK() {
        // 初始化AIUI
        createAgent();
        // 初始化CAE
//        initCaeEngine();
        // 初始化alsa录音
//        initAlsa();

        initAudioTrack();

    }


    private void initAudioTrack() {
        if (mAudioTrackOperator == null){
            mAudioTrackOperator = new AudioTrackOperator();
            mAudioTrackOperator.createStreamModeAudioTrack();
        }
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
            //strTip = "AIUI初始化失败!";
        } else {
            //strTip = "AIUI初始化成功!";
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
            Log.i(TAG, "onEvent: " + event.eventType + "  " + event.arg1);
            switch (event.eventType) {
                //唤醒事件
                case AIUIConstant.EVENT_WAKEUP://4
                    Log.i(TAG, "AIUI STATUS --- 唤醒");
                    String info = event.info;
                    Log.i(TAG, "on EVENT_WAKEUP: " + info);
                    if(info != null &&  !info.isEmpty()){
                        try {
                            JSONObject jsInfo = new JSONObject(info);
                            String ivwResult = jsInfo.getString("ivw_result");
                            JSONObject ivwInfo = new JSONObject(ivwResult);
                            String keyword = ivwInfo.getString("keyword");
                            Log.i(TAG, "本次唤醒为:"+ keyword);
                        }catch (Exception e){

                        }
                    }
                    break;
                //结果事件（包含听写，语义，离线语法结果）
                case AIUIConstant.EVENT_RESULT://1
                    //结果解析事件
                    LogUtil.iTag(TAG, "AIUI STATUS --- 结果INFO -- " + event.info);
                    LogUtil.iTag(TAG, "AIUI STATUS --- 结果DATA -- " + event.data);

                    try {
                        JSONObject bizParamJson = new JSONObject(event.info);
                        JSONObject data = bizParamJson.getJSONArray("data").getJSONObject(0);
                        JSONObject params = data.getJSONObject("params");
                        JSONObject content = data.getJSONArray("content").getJSONObject(0);

//                        if (content.has("cnt_id")) {
//                            String cnt_id = content.getString("cnt_id");
//                            JSONObject cntJson = new JSONObject(new String(event.data.getByteArray(cnt_id), "utf-8"));
//                            Log.i(TAG, "AIUI STATUS --- 结果DATA -- " + new String(event.data.getByteArray(cnt_id)));
//                            String sub = params.optString("sub");
//
//                            //听写结果(iat)
//                            //语义结果(nlp)
//                            //后处理服务结果(tpp)
//                            //云端tts结果(tts)
//                            //翻译结果(itrans)
//
//                            if ("nlp".equals(sub)) {
//                                // 解析得到语义结果
//                                JSONObject resultStr = cntJson.optJSONObject("intent");
//
//                            }
//                        }


                        String sub = params.optString("sub");
                        if ("tts".equals(sub)) {
                            if (content.has("cnt_id")) {
                                String sid = event.data.getString("sid");
                                String cnt_id = content.getString("cnt_id");
                                byte[] audio = event.data.getByteArray(cnt_id); //合成音频数据



                                /**
                                 *
                                 * 音频块位置状态信息，取值：
                                 * - 0（合成音频开始块）
                                 * - 1（合成音频中间块，可出现多次）
                                 * - 2（合成音频结束块)
                                 * - 3（合成音频独立块,在短合成文本时出现）
                                 *
                                 * 举例说明：
                                 * 一个正常语音合成可能对应的块顺序如下：
                                 *   0 1 1 1 ... 2
                                 * 一个短的语音合成可能对应的块顺序如下:
                                 *   3
                                 **/
                                int dts = content.getInt("dts");

                                mAudioTrackOperator.write(audio,dts);
                                int frameId = content.getInt("frame_id");// 音频段id，取值：1,2,3,...

                                int percent = event.data.getInt("percent"); //合成进度

                                boolean isCancel = "1".equals(content.getString("cancel"));  //合成过程中是否被取消
                            }
                        }
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                    break;
                //休眠事件
                case AIUIConstant.EVENT_SLEEP://5
                    Log.i(TAG, "AIUI STATUS --- 休眠");
                    break;
                case AIUIConstant.EVENT_START_RECORD://11
                    Log.i( TAG,  "AIUI STATUS --- 开始录音");
                    //开始录音
                    break;
                case AIUIConstant.EVENT_STOP_RECORD://12
                    Log.i( TAG,  "AIUI STATUS --- 停止录音");
                    // 停止录音
                    break;
                // 状态事件
                case AIUIConstant.EVENT_STATE: {//3
                    mAIUIState = event.arg1;
                    if (AIUIConstant.STATE_IDLE == mAIUIState) {//1
                        // 闲置状态，AIUI未开启
                        Log.i(TAG, "AIUI STATUS --- 状态 -- 闲置状态，AIUI未开启");
                    } else if (AIUIConstant.STATE_READY == mAIUIState) {//2
                        // AIUI已就绪，等待唤醒
                        Log.i(TAG, "AIUI STATUS --- 状态 -- AIUI已就绪，等待唤醒");
                    } else if (AIUIConstant.STATE_WORKING == mAIUIState) {//3
                        // AIUI工作中，可进行交互
                        Log.i(TAG, "AIUI STATUS --- 状态 -- AIUI工作中，可进行交互");
                    }
                } break;
                case AIUIConstant.EVENT_TTS: {
                    switch (event.arg1) {
                        case AIUIConstant.TTS_SPEAK_BEGIN:
                            Log.i(TAG, "TTS --- 开始播放");
                            break;

                        case AIUIConstant.TTS_SPEAK_PROGRESS:
                            Log.i(TAG, "TTS --- 播放进度为" + event.data.getInt("percent"));
//                            showTip("缓冲进度为" + mTtsBufferProgress +
//                                    ", 播放进度为" + event.data.getInt("percent"));     // 播放进度
                            break;

                        case AIUIConstant.TTS_SPEAK_PAUSED:
//                            showTip("暂停播放");
                            Log.i(TAG, "TTS --- 暂停播放");
                            break;

                        case AIUIConstant.TTS_SPEAK_RESUMED:
//                            showTip("恢复播放");
                            Log.i(TAG, "TTS --- 恢复播放");
                            break;

                        case AIUIConstant.TTS_SPEAK_COMPLETED:
//                            showTip("播放完成");
                            Log.i(TAG, "TTS --- 播放完成");
                            break;

                        default:
                            break;
                    }
                }
                    break;
                //错误事件
                case AIUIConstant.EVENT_ERROR://2
                    Log.i(TAG, "AIUI STATUS --- 错误" + event.arg1);
                    break;
            }
        }
    };

}