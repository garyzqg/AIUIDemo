package payfun.lib.basis.time;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;

import java.util.Calendar;

import payfun.lib.basis.utils.LogUtil;

/**
 * @author : 时光
 * e-mail : qurongzhen@pay.media
 * date   : 2021/11/15 11:28
 * desc   : <定时器工具类>
 */
public class AlarmUtil {
    public static final String ALARM_ACTION = "com.inspiry.alarm.clock";
    public static final String ALARM_START_MILLIS = "AlarmStartMillis";
    public static final String ALARM_INTERVAL_MILLIS = "AlarmIntervalMillis";
    public static final String ALARM_MSG = "AlarmMsg";

    /**
     * @param context          环境
     * @param firstStartMillis 闹钟的指定时间 当闹钟触发时间小于当前系统时间时，闹钟会立即触发，为了处理这种情况，添加了特殊处理
     * @param intent           定时触发时接收的Intent
     */
    public static void setAlarmTime(Context context, long firstStartMillis, long intervalMillis, Intent intent) {
        LogUtil.i("开始定时", "startMillis:" + firstStartMillis + "   intervalMillis:" + intervalMillis + " 当前时间:" + System.currentTimeMillis());
        if (firstStartMillis <= System.currentTimeMillis()) {
            long l = System.currentTimeMillis() - firstStartMillis;
            long count = (l / intervalMillis) + 1;
            firstStartMillis = firstStartMillis + count * intervalMillis;
            LogUtil.i("重置开始时间：" + firstStartMillis);
        }

        Context applicationContext = context.getApplicationContext();
        AlarmManager am = (AlarmManager) applicationContext.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(applicationContext, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        // 到了 指定时间后 后通过PendingIntent pi对象发送广播  版本适配
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // 6.0及以上
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, firstStartMillis, pendingIntent);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // 4.4及以上
            am.setExact(AlarmManager.RTC_WAKEUP, firstStartMillis, pendingIntent);
        } else {
            am.set(AlarmManager.RTC_WAKEUP, firstStartMillis, pendingIntent);
        }
    }


    /**
     * 设置闹钟 通过广播对定时结果进行监听，监听ALARM_ACTION = com.inspiry.alarm.clock;
     *
     * @param context          环境
     * @param firstStartMillis 闹钟第一次开始时间 毫秒
     * @param intervalMillis   闹钟周期的间隔时间 毫秒
     * @param tips             提示内容,通过intent中 ALARM_MSG 来获取提示
     */
    public static void setAlarmTime(Context context, long firstStartMillis, long intervalMillis, String tips) {
        Intent intent = new Intent(ALARM_ACTION);
        intent.putExtra(ALARM_INTERVAL_MILLIS, intervalMillis);
        intent.putExtra(ALARM_START_MILLIS, firstStartMillis);
        intent.putExtra(ALARM_MSG, tips);
        setAlarmTime(context, firstStartMillis, intervalMillis, intent);
    }


    public static AlarmBroadcastReceiver registerReceiver(Context context) {
        AlarmBroadcastReceiver alarmBroadcastReceiver = new AlarmBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(AlarmUtil.ALARM_ACTION);
        context.getApplicationContext().registerReceiver(alarmBroadcastReceiver, filter);
        return alarmBroadcastReceiver;
    }


    public static long makeStartMillis(int hour, int minute) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get
                (Calendar.DAY_OF_MONTH));
        //24小时制
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        return calendar.getTimeInMillis();
    }
}
