package payfun.lib.basis.utils;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.media.AudioManager;
import android.provider.Settings;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresPermission;

/**
 * @author : 时光
 * e-mail : qurongzhen@pay.media
 * date   : 2022/6/8 14:51
 * desc   : <部分系统功能设置工具类>
 */
public final class SysSettingUtil {

    /**
     * 获取屏幕的亮度
     */
    public static int getScreenBrightness(Context context) {
        int brightness = 0;
        try {
            brightness = Settings.System.getInt(context.getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return brightness;
    }


    /**
     * 设置窗口亮度
     *
     * @param window     窗口
     * @param brightness 亮度值
     */
    public static void setWindowBrightness(@NonNull final Window window,
                                           @IntRange(from = 0, to = 255) int brightness) {
        //不让屏幕全暗
        if (brightness <= 1) {
            brightness = 1;
        }
        WindowManager.LayoutParams lp = window.getAttributes();
        //0到1,调整亮度暗到全亮
        lp.screenBrightness = brightness / 255f;
        window.setAttributes(lp);
    }

    /**
     * 设置当前Activity显示时的亮度
     * <p>需添加权限 {@code <uses-permission android:name="android.permission.WRITE_SETTINGS" />}</p>
     * 屏幕亮度最大数值一般为255，各款手机有所不同
     * screenBrightness 的取值范围在[0,1]之间
     */
    @RequiresPermission(Manifest.permission.WRITE_SETTINGS)
    public static boolean setScreenBrightness(Context context, int brightness) {
        //不让屏幕全暗
        if (brightness <= 1) {
            brightness = 1;
        }
        ContentResolver resolver = context.getContentResolver();
        boolean b = Settings.System.putInt(resolver, Settings.System.SCREEN_BRIGHTNESS, brightness);
        resolver.notifyChange(Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS), null);
        return b;
    }


    public static void setSysMusicVolume(int volume) {
        setVolumeCheck(AudioManager.STREAM_MUSIC, volume);
    }

    public static int getSysMusicVolume(Context context) {
        AudioManager systemService = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        return systemService.getStreamVolume(AudioManager.STREAM_MUSIC);
    }

    public static void setSysRingVolume(int volume) {
        setVolumeCheck(AudioManager.STREAM_RING, volume);
    }

    public static int getSysRingVolume(Context context) {
        AudioManager systemService = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        return systemService.getStreamVolume(AudioManager.STREAM_RING);
    }

    private static void setVolumeCheck(int streamType, int progress) {
        AudioManager mAudioManager = (AudioManager) InitUtil.getAppContext().getSystemService(Context.AUDIO_SERVICE);
        try {
            mAudioManager.setStreamVolume(streamType, progress, 0);
        } catch (Exception e) {
            Toast.makeText(InitUtil.getAppContext(), "请关闭勿扰模式后调节音量", Toast.LENGTH_LONG).show();
        }
    }


}


