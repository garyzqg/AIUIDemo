package com.inspur.mspeech.utils;

import payfun.lib.basis.utils.SpUtil;

/**
 * @author : zhangqinggong
 * date    : 2023/2/27 9:41
 * desc    : sp管理类
 */
public class PrefersTool {
    private static final String VOICE_NAME = "voice_name";//音色
    private static final String AVAILABLE_COUNT = "available_count";//可使用次数
    private static final String USED_COUNT = "used_count";//已使用次数

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
}
