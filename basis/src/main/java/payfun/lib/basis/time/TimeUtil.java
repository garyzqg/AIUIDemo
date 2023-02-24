package payfun.lib.basis.time;

import android.os.SystemClock;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author : 时光
 * e-mail : qurongzhen@pay.media
 * date   : 2021/5/26 18:35
 * desc   : <时间工具类>
 */
public final class TimeUtil {


    /**
     * 获取当前时间，格式为：yyyy-MM-dd HH:mm:ss
     *
     * @return yyyy-MM-dd HH:mm:ss
     */
    public static String getCurFormatYMdHms() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return format.format(new Date());
    }

    /**
     * 获取当前时间，格式为：yyyyMMdd
     *
     * @return yyyy-MM-dd HH:mm:ss
     */
    public static String getCurFormatYMd() {
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
        return format.format(new Date());
    }

    /**
     * 将指定时间戳转换为格式：yyyy-MM-dd HH:mm:ss
     *
     * @param mills 指定时间戳
     * @return yyyy-MM-dd HH:mm:ss
     */
    public static String formatYMdHms(long mills) {
        return formatYMdHms(new Date(mills));
    }

    /**
     * 将指定时间戳转换为格式：MM-dd HH:mm
     *
     * @param mills 指定时间戳
     * @return MM-dd HH:mm
     */
    public static String formatMMddHHmm(long mills) {
        SimpleDateFormat format = new SimpleDateFormat("MM-dd HH:mm");
        return format.format(new Date(mills));
    }

    /**
     * 将指定时间戳转换为格式：HH:mm
     *
     * @param mills 指定时间戳
     * @return HH:mm
     */
    public static String formatHHmm(long mills) {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm");
        return format.format(new Date(mills));
    }


    /**
     * 将指定时间转换为格式：yyyy-MM-dd HH:mm:ss
     *
     * @param date 指定时间
     * @return yyyy-MM-dd HH:mm:ss
     */
    public static String formatYMdHms(Date date) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return format.format(date);
    }


    /**
     * 将格式为：yyyyMMddHHmmss的字符串转换为格式：yyyy-MM-dd HH:mm:ss
     *
     * @param yyyyMMddHHmmss yyyyMMddHHmmss格式字符串
     * @return yyyy-MM-dd HH:mm:ss
     */
    public static String formatYMdHms(String yyyyMMddHHmmss) {
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMddHHmmss");
        try {
            Date date = sdf1.parse(yyyyMMddHHmmss);
            return formatYMdHms(date);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return getCurFormatYMdHms();
    }


    /**
     * 获取系统开机时间
     * <p>注意：系统4.2以上才支持
     *
     * @return yyyy-MM-dd HH:mm:ss
     */
    public static String getSystemBootTime() {
        long time = System.currentTimeMillis() - SystemClock.elapsedRealtime();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date d1 = new Date(time);
        return format.format(d1);
    }
}
