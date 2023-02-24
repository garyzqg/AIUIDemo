package com.inspur.mspeech.utils;

import android.util.Base64;

/**
 * @author : zhangqinggong
 * date    : 2023/1/13 17:25
 * desc    : Base64工具类
 */
public class Base64Utils {
    /**
     * 使用base64解密
     *
     * @param content 待解密内容
     * @return 解密后byte数组
     */
    public static byte[] base64EncodeToByte(String content) {
        return Base64.decode(content, Base64.DEFAULT);
    }

    /**
     * 使用base64解密
     *
     * @param content 待解密内容
     * @return 解密后字符串
     */
    public static String base64EncodeToString(String content) {
        byte[] decodeByte = Base64.decode(content, Base64.DEFAULT);
        return new String(decodeByte);
    }
}
