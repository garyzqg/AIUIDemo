package com.inspur.mspeech.utils;

import payfun.lib.basis.utils.SpUtil;

/**
 * @author : zhangqinggong
 * date    : 2023/2/27 9:41
 * desc    : sp管理类
 */
public class PrefersTool {
    private static final String VOICE_NAME = "voice_name";//音色
    private static final String VOICE_STYLE = "voice_style";//语气 说话风格
    private static final String AVAILABLE_COUNT = "available_count";//可使用次数
    private static final String USED_COUNT = "used_count";//已使用次数
    private static final String ACCESS_TOKEN = "accesstoken";
    private static final String USERNAME = "user_name";
    private static final String SCENE_ID = "scene_id";
    private static final String SCENE_NAME = "scene_name";
    private static final String MODEL_SWITCH = "model_switch";
    private static final String SPEED = "speed";
    private static final String TONE = "tone";
    private static final String LLM_TYPE = "llmType";

    public static void setSceneId(String sceneId) {
        SpUtil.getInstance().put(SCENE_ID, sceneId);
    }
    public static String getSceneId() {
        return SpUtil.getInstance().getString(SCENE_ID);
    }

    public static void setSceneName(String sceneName) {
        SpUtil.getInstance().put(SCENE_NAME, sceneName);
    }
    public static String getsceneName() {
        return SpUtil.getInstance().getString(SCENE_NAME);
    }
    public static void setUserName(String userName) {
        SpUtil.getInstance().put(USERNAME, userName);
    }
    public static String getUserName() {
        return SpUtil.getInstance().getString(USERNAME);
    }
    public static void setAccesstoken(String accesstoken) {
        SpUtil.getInstance().put(ACCESS_TOKEN, accesstoken);
    }
    public static String getAccesstoken() {
        return SpUtil.getInstance().getString(ACCESS_TOKEN);
    }

    public static void setVoiceName(String voiceName) {
        SpUtil.getInstance().put(VOICE_NAME, voiceName);
    }
    public static String getVoiceName() {
        return SpUtil.getInstance().getString(VOICE_NAME, "XiaoshuangNeural");
    }

    public static void setAvailableCount(int count) {
        SpUtil.getInstance().put(AVAILABLE_COUNT, count);
    }
    public static int getAvailableCount() {
        return SpUtil.getInstance().getInt(AVAILABLE_COUNT, 50);
    }


    public static void setUsedCount(int count) {
        SpUtil.getInstance().put(USED_COUNT, count);
    }
    public static int getUsedCount() {
        return SpUtil.getInstance().getInt(USED_COUNT, 0);
    }

    public static void setModelSwitch(boolean modelSwitch) {
        SpUtil.getInstance().put(MODEL_SWITCH, modelSwitch);
    }
    public static boolean getModelSwitch() {
        return SpUtil.getInstance().getBoolean(MODEL_SWITCH, true);
    }

    public static void setSpeed(int speed) {
        SpUtil.getInstance().put(SPEED, speed);
    }
    public static int getSpeed() {
        return SpUtil.getInstance().getInt(SPEED, 20);
    }

    public static void setTone(int tone) {
        SpUtil.getInstance().put(TONE, tone);
    }
    public static int getTone() {
        return SpUtil.getInstance().getInt(TONE, 0);
    }

    public static void setVoiceStyle(String voiceStyle) {
        SpUtil.getInstance().put(VOICE_STYLE, voiceStyle);
    }
    public static String getVoiceStyle() {
        return SpUtil.getInstance().getString(VOICE_STYLE, "general");
    }
    public static void setLlmType(String llmType) {
        SpUtil.getInstance().put(LLM_TYPE, llmType);
    }
    public static String getLlmType() {
        return SpUtil.getInstance().getString(LLM_TYPE, "default");
    }
}
