package payfun.lib.basis.utils;

import java.nio.charset.Charset;

/**
 * @author : zhangqg
 * date   : 2021/11/1 16:18
 * desc   : <判断工具类>
 */
public final class JudgeUtil {

    /**
     * 判断奇偶数
     *
     * @return true ：偶数；false ：奇数；
     */
    public static boolean isEvenNum(int num) {
        if ((num & 1) == 1) {
            //奇数
            return false;
        } else {
            //偶数
            return true;
        }
    }


    /**
     * 字符串包含多少个字节
     *
     * @param string  输入内容
     * @param charset 编码方式
     * @return 字符所占的字节个数
     */
    public static int getByteCount(String string, Charset charset) {
        if (string == null || string.length() == 0) {
            return 0;
        }
        byte[] bytes = string.getBytes(charset);
        return bytes.length;
    }


    public static final String PHONE_REGEX = "^1[0-9]{10}$";

    /**
     * 是否是手机号
     *
     * @param phone 需校验的手机号
     * @return true：是；false：否
     */
    public static boolean isPhone(String phone) {
        if (phone == null || phone.length() == 0) {
            return false;
        }
        return phone.matches(PHONE_REGEX);
    }


}
