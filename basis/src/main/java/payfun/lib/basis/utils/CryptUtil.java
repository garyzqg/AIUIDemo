package payfun.lib.basis.utils;

import android.util.Base64;

import androidx.annotation.Nullable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.security.MessageDigest;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/**
 * @author : 时光
 * e-mail : qurongzhen@pay.media
 * date   : 2022/6/29 18:25
 * desc   : <p>加解密工具类
 */
public final class CryptUtil {


    public static final String GZIP_ENCODE_UTF_8 = "UTF-8";
    public static final String GZIP_ENCODE_ISO_8859_1 = "ISO-8859-1";

    /**
     * md5 加密
     *
     * @param input 需加密内容
     * @return 加密后内容
     */
    public static String md5(String input) {
        if (input == null || input.length() == 0) {
            return "";
        }
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] bytes = md5.digest(input.getBytes());
            StringBuilder result = new StringBuilder();
            for (byte b : bytes) {
                String temp = Integer.toHexString(b & 0xff);
                if (temp.length() == 1) {
                    temp = "0" + temp;
                }
                result.append(temp);
            }
            return result.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }


    /**
     * AES 解密
     *
     * @param content     需要解密的字符串
     * @param encodeRules AES Key String,must be 128,192,256bits
     * @return decrypted string, if exception happens return null
     */
    public static String aesDecode(String content, String encodeRules) {
        try {
            byte[] rawKey = encodeRules.getBytes();
            SecretKeySpec skeySpec = new SecretKeySpec(rawKey, "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec);
            byte[] decrypted = cipher.doFinal(Base64.decode(content, Base64.NO_WRAP));
            return new String(decrypted, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * AES 加密
     *
     * @param content     需要加密的字符串
     * @param encodeRules AES Key String,must be 128,192,256bits
     * @return encrypted base64 string, if exception happens return null
     */
    public static String aesEncode(String content, String encodeRules) {
        try {
            byte[] rawKey = encodeRules.getBytes();
            SecretKeySpec keySpec = new SecretKeySpec(rawKey, "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            byte[] encrypted = cipher.doFinal(content.getBytes());
            return Base64.encodeToString(encrypted, Base64.NO_WRAP);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


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


    /**
     * 压缩
     *
     * @param str      待压缩的内容
     * @param encoding 编码格式
     * @return byte数组
     */
    @Nullable
    public static byte[] compress(String str, String encoding) {
        if (str == null || str.length() == 0) {
            return null;
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        GZIPOutputStream gzip;
        try {
            gzip = new GZIPOutputStream(out);
            gzip.write(str.getBytes(encoding));
            gzip.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return out.toByteArray();
    }


    /**
     * 压缩
     *
     * @param str 待压缩的内容 默认使用utf-8
     * @return byte数组
     */
    @Nullable
    public static byte[] compress(String str) {
        return compress(str, GZIP_ENCODE_UTF_8);
    }

    /**
     * 解压
     *
     * @param bytes 待解压的数组
     * @return byte数组
     */
    @Nullable
    public static byte[] uncompress(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        try {
            GZIPInputStream unGzip = new GZIPInputStream(in);
            byte[] buffer = new byte[256];
            int n;
            while ((n = unGzip.read(buffer)) >= 0) {
                out.write(buffer, 0, n);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return out.toByteArray();
    }


    /**
     * 解压
     *
     * @param bytes    待解压的数组
     * @param encoding 编码格式
     * @return 解压内容
     */
    @Nullable
    public static String uncompressToString(byte[] bytes, String encoding) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        try {
            GZIPInputStream unGzip = new GZIPInputStream(in);
            byte[] buffer = new byte[256];
            int n;
            while ((n = unGzip.read(buffer)) >= 0) {
                out.write(buffer, 0, n);
            }
            return out.toString(encoding);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 解压
     *
     * @param bytes 待解压的数组 默认utf-8
     * @return 解压内容
     */
    @Nullable
    public static String uncompressToString(byte[] bytes) {
        return uncompressToString(bytes, GZIP_ENCODE_UTF_8);
    }


//    public static void main(String[] args) throws IOException {
//        String s = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
//        System.out.println("字符串长度：" + s.length());
//        System.out.println("压缩后：：" + compress(s).length);
//        System.out.println("解压后：" + uncompress(compress(s)).length);
//        System.out.println("解压字符串后：：" + uncompressToString(compress(s)).length());
//    }
}
