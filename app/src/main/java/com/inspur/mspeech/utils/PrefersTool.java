package com.inspur.mspeech.utils;

import payfun.lib.basis.utils.SpUtil;

/**
 * @author : zhangqinggong
 * date    : 2023/2/27 9:41
 * desc    : sp管理类
 */
public class PrefersTool {
    private static final String VOICE_NAME = "voice_name";//音色

    public static void setVoiceName(String voiceName) {
        SpUtil.getInstance().put(VOICE_NAME, voiceName);
    }
    public static String getVoiceName() {
        return SpUtil.getInstance().getString(VOICE_NAME, "XiaohanNeural");
    }
}
