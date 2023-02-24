package payfun.lib.basis.time;


import android.app.AlarmManager;
import android.content.Context;
import android.provider.Settings;
import android.text.format.DateFormat;

import androidx.annotation.RequiresPermission;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import payfun.lib.basis.utils.InitUtil;


/**
 * @author : 时光
 * e-mail : qurongzhen@pay.media
 * date   : 2021/5/28 13:57
 * desc   : <设置系统时间>
 */
public class SysTimeUtil {
    public static int MS = 1000;


    /**
     * 设置系统日期
     *
     * @param year  年
     * @param month 月：0表示1月
     * @param day   天
     */
    @RequiresPermission(android.Manifest.permission.SET_TIME)
    public static void setSysDate(int year, int month, int day) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month);
        c.set(Calendar.DAY_OF_MONTH, day);

        long when = c.getTimeInMillis();

        if (when / MS < Integer.MAX_VALUE) {
            ((AlarmManager) InitUtil.getAppContext().getSystemService(Context.ALARM_SERVICE)).setTime(when);
        }
    }


    /**
     * 设置系统时间
     *
     * @param hour   小时
     * @param minute 分钟
     */
    @RequiresPermission(android.Manifest.permission.SET_TIME)
    public static void setSysTime(int hour, int minute) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, hour);
        c.set(Calendar.MINUTE, minute);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        long when = c.getTimeInMillis();

        if (when / MS < Integer.MAX_VALUE) {
            ((AlarmManager) InitUtil.getAppContext().getSystemService(Context.ALARM_SERVICE)).setTime(when);
        }
    }

    /**
     * 设置系统时间
     *
     * @param targetMillis 要修改到的指定时间（到毫秒）
     * @param errRange     与当前时间的误差，单位分钟，如果小于等于0，则直接修改；如果大于0则误差内不修改，误差外触发修改
     */
    @RequiresPermission(android.Manifest.permission.SET_TIME)
    public static void setSysTime(long targetMillis, int errRange) {
        if (targetMillis <= 0) {
            return;
        }
        if (errRange <= 0) {
            if (targetMillis / MS < Integer.MAX_VALUE) {
                ((AlarmManager) InitUtil.getAppContext().getSystemService(Context.ALARM_SERVICE)).setTime(targetMillis);
            }
        } else {
            long currentTime = System.currentTimeMillis();
            //误差大小 n 分钟
            int value = errRange * 60 * 1000;
            if (targetMillis / MS < Integer.MAX_VALUE && Math.abs(targetMillis - currentTime) > value) {
                ((AlarmManager) InitUtil.getAppContext().getSystemService(Context.ALARM_SERVICE)).setTime(targetMillis);
            }
        }
    }

    /**
     * 设置系统时区
     *
     * @param timeZone 时区
     */
    public static void setTimeZone(String timeZone) {
        final Calendar now = Calendar.getInstance();
        TimeZone tz = TimeZone.getTimeZone(timeZone);
        now.setTimeZone(tz);
    }

    /**
     * 获取系统当前的时区
     *
     * @return 时区
     */
    public static String getDefaultTimeZone() {
        return TimeZone.getDefault().getDisplayName();
    }


    /**
     * 设置系统的时间是否需要自动获取
     *
     * @param checked {@code true}: 自动获取 {@code false}: 否
     */
    public static void setAutoDateTime(int checked) {
        Settings.Global.putInt(InitUtil.getAppContext().getContentResolver(),
                Settings.Global.AUTO_TIME, checked);
    }


    /**
     * 判断系统的时间是否自动获取的
     *
     * @return {@code true}: 是自动获取 {@code false}: 否
     */
    public static boolean isDateTimeAuto() {
        try {
            return Settings.Global.getInt(InitUtil.getAppContext().getContentResolver(),
                    Settings.Global.AUTO_TIME) > 0;
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }


    /**
     * 设置系统的时区是否自动获取
     *
     * @param checked {@code true}: 自动获取 {@code false}: 否
     */
    public static void setAutoTimeZone(int checked) {
        Settings.Global.putInt(InitUtil.getAppContext().getContentResolver(),
                Settings.Global.AUTO_TIME_ZONE, checked);
    }


    /**
     * 判断系统的时区是否是自动获取的
     *
     * @return {@code true}: 是自动获取 {@code false}: 否
     */
    public static boolean isTimeZoneAuto() {
        try {
            return Settings.Global.getInt(InitUtil.getAppContext().getContentResolver(),
                    Settings.Global.AUTO_TIME_ZONE) > 0;
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 设置12小时制还是24小时制
     *
     * @param flag {@code true}: 24小时制 {@code false}: 12小时制
     */
    public static void set24Or12(boolean flag) {
        Settings.System.putString(InitUtil.getAppContext().getContentResolver(),
                Settings.System.TIME_12_24, flag ? "24" : "12");
    }


    /**
     * 是12小时制还是24小时制
     *
     * @return {@code true}: 24小时制 {@code false}: 12小时制
     */
    public static boolean is24Hour() {
        return DateFormat.is24HourFormat(InitUtil.getAppContext());
    }

    /**
     * 获取服务器时间
     *
     * @param date 如：Fri, 28 May 2021 09:38:51 GMT
     * @return 时间戳：13位
     */
    public static long getRemoteTime(String date) {
        long defValue = 0;
        try {
            Date parse = java.text.DateFormat.getInstance().parse(date);
            defValue = parse.getTime();
        } catch (Exception e) {
            defValue = -1;
            e.printStackTrace();
        }
        return defValue;
    }
}
