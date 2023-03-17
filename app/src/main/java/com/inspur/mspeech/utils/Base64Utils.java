package com.inspur.mspeech.utils;

import android.util.Base64;

import java.io.UnsupportedEncodingException;

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
    public static byte[] base64DecodeToByte(String content) {
        return Base64.decode(content, Base64.DEFAULT);
    }

    /**
     * 使用base64解密
     *
     * @param content 待解密内容
     * @return 解密后字符串
     */
    public static String base64DecodeToString(String content) {
        byte[] decodeByte = Base64.decode(content, Base64.DEFAULT);
        return new String(decodeByte);
    }
    /**
     * 使用base64加密
     *
     * @param content 待加密内容
     * @return 加密后字符串
     */
    public static String base64EncodeToString(String content) {
        String s = "";
        try {
            s = Base64.encodeToString(content.getBytes("utf-8"),Base64.NO_WRAP);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return s;
    }

}
