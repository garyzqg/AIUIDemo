package payfun.lib.basis.time;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import payfun.lib.basis.utils.LogUtil;

/**
 * @author : 时光
 * e-mail : qurongzhen@pay.media
 * date   : 2021/11/15 15:09
 * desc   : <闹钟广播监听 : 需要在清单文件中添加如下广播注册，可自定义该广播进行监听>
 * <p>
 * <receiver android:name="payfun.lib.basis.time.AlarmBroadcastReceiver">
 * <intent-filter>
 * <action android:name="com.inspiry.alarm.clock"/>
 * </intent-filter>
 * </receiver>
 * </p>
 */
public class AlarmBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (TextUtils.equals(AlarmUtil.ALARM_ACTION, intent.getAction())) {
            long intervalMillis = intent.getLongExtra(AlarmUtil.ALARM_INTERVAL_MILLIS, 0);
            long startMillis = intent.getLongExtra(AlarmUtil.ALARM_START_MILLIS, 0);
            LogUtil.i("触发定时", "startMillis:" + startMillis + "   intervalMillis:" + intervalMillis);
            if (intervalMillis != 0) {
                long newStartMillis = startMillis + intervalMillis;
                intent.putExtra(AlarmUtil.ALARM_START_MILLIS, newStartMillis);
                AlarmUtil.setAlarmTime(context, newStartMillis, intervalMillis, intent);
            }
        }
    }
}
