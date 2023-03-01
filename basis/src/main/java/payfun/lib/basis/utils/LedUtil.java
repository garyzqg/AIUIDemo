package payfun.lib.basis.utils;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author : zhangqg
 * date   : 2021/12/20 10:52
 * desc   : <p>灯控控制类</p>
 */
public class LedUtil {

    public static final int LED_ON = 1;
    public static final int LED_OFF = 0;

    public static final int LED_LEFT = 0;
    public static final int LED_RIGHT = 1;


    @IntDef({LED_ON, LED_OFF})
    @Retention(RetentionPolicy.SOURCE)
    public @interface LedStatus {

    }

    @IntDef({LED_LEFT, LED_RIGHT})
    @Retention(RetentionPolicy.SOURCE)
    public @interface LedPosition {

    }


    /**
     * 控制单个灯
     *
     * @param context     环境变量
     * @param ledPosition 灯的位置 {@link #LED_LEFT} 左侧灯；{@link #LED_RIGHT} 右侧灯；
     * @param ledStatus   设置灯的状态：{@link #LED_ON}开灯；{@link #LED_OFF} 关灯；
     */
    public static void controlSingleLED(Context context, @LedPosition int ledPosition, @LedStatus int ledStatus) {
        Intent intent = new Intent("com.action.control.led");
        intent.putExtra("type", ledPosition);
        intent.putExtra("enable", ledStatus);
        context.sendBroadcast(intent);
    }

    /**
     * 控制所有灯
     *
     * @param context   环境变量
     * @param ledStatus 设置灯的状态：{@link #LED_ON}开灯；{@link #LED_OFF} 关灯；
     */
    public static void controlAllLED(Context context, @LedStatus int ledStatus) {
        controlSingleLED(context, LED_LEFT, ledStatus);
        controlSingleLED(context, LED_RIGHT, ledStatus);
    }


    /**
     * ROM>=1.1.0.19 新增：呼吸灯功能
     *
     * @param context     环境变量
     * @param ledPosition 灯的位置 {@link #LED_LEFT} 左侧灯；{@link #LED_RIGHT} 右侧灯；
     * @param ledStatus   设置灯的状态：{@link #LED_ON}开灯；{@link #LED_OFF} 关灯；
     * @param brightness  “0”表示关闭； “1”表示打开； “255”表示呼吸灯；
     */
    private static void controlSingleLED(Context context, @LedPosition int ledPosition, @LedStatus int ledStatus, String brightness) {
        Intent intent = new Intent("com.action.control.led");
        intent.putExtra("type", ledPosition);
        intent.putExtra("enable", ledStatus);
        intent.putExtra("brightness", brightness);
        context.sendBroadcast(intent);
    }


    /**
     * LedUtil.controlAllLED(getActivity(), LedUtil.LED_OFF, "0");  代表关闭
     * LedUtil.controlAllLED(getActivity(), LedUtil.LED_ON, "1");  代表LED灯常量
     * LedUtil.controlAllLED(getActivity(), LedUtil.LED_ON, "255");  代表LED灯开启呼吸灯模式
     *
     * @param context
     * @param ledStatus
     */
    public static void controlAllBreathLED(Context context, @LedStatus int ledStatus) {
        controlSingleLED(context, LED_LEFT, ledStatus, "255");
        controlSingleLED(context, LED_RIGHT, ledStatus, "255");
    }


}
