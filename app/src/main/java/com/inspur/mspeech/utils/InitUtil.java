package com.inspur.mspeech.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.text.TextUtils;

/**
 * @author : 时光
 * e-mail : qurongzhen@pay.media
 * date   : 2021/5/6 11:30
 * desc   : <工具类初始化 InitUtil>
 */
public final class InitUtil {
    @SuppressLint("StaticFieldLeak")
    private static Context sContext;
    private static Handler sHandler;
    private static String SN;
    private static String packageName;
    private static String versionName;
    private static boolean isDebug = false;

    private InitUtil() {
        throw new UnsupportedOperationException("u can't instantiate InitUtil");
    }

    /**
     * 初始化工具类
     *
     * @param context 上下文
     */
    public static synchronized void init(Context context) {
        if (InitUtil.sContext == null) {
            InitUtil.sContext = context.getApplicationContext();
        }
    }

    /**
     * 获取ApplicationContext
     *
     * @return ApplicationContext
     */
    public static synchronized Context getAppContext() {
        if (sContext != null) {
            return sContext;
        }
        throw new NullPointerException("null " + "(InitUtil)sContext");
    }

    /**
     * 获取全局主进程Handler
     *
     * @return
     */
    public static synchronized Handler getMainHandler() {
        if (sHandler == null) {
            sHandler = new Handler(getAppContext().getMainLooper());
        }
        return sHandler;
    }

//    public static synchronized String getSN() {
//        if (TextUtils.isEmpty(SN)) {
//            ApiManager apiManager = new ApiManager(InitUtil.getAppContext());
//            SN = apiManager.getSN();
//        }
//        return SN;
//    }

    public static synchronized String getPackageName() {
        if (TextUtils.isEmpty(packageName)) {
            packageName = sContext.getPackageName();
        }
        return packageName;
    }

    public static synchronized String getVersionName() {
        if (TextUtils.isEmpty(versionName)) {
            try {
                versionName = sContext.getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return versionName;
    }

    public static String getAppName() {
        try {
            PackageManager packageManager = sContext.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(
                    sContext.getPackageName(), 0);
            int labelRes = packageInfo.applicationInfo.labelRes;
            return sContext.getResources().getString(labelRes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }


    public static String getString(int resId) {
        return getAppContext().getString(resId);
    }

    public static String getMeta(String key) {
        String value = "";
        try {
            ApplicationInfo info = getAppContext().getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
            value = info.metaData.getString(key);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }


    public static void setDebug(boolean isDebug) {
        InitUtil.isDebug = isDebug;
    }


    public static boolean isDebug() {
        return isDebug;
    }

    /**
     * 默认Log打印配置，可不调用该方法自行配置
     */
    public static void initDefaultLog(boolean isLog) {
        LogUtil.Config config = LogUtil.getConfig()
                .setLogSwitch(isLog)
                .setConsoleSwitch(isLog)
                .setGlobalTag(null)
                .setLogHeadSwitch(isLog)
                .setLog2FileSwitch(isLog)
                .setDir("")
                .setFilePrefix("")
                .setBorderSwitch(isLog)
                .setConsoleFilter(LogUtil.D)
                .setFileFilter(LogUtil.D)
                .setFileExtension(".txt")
                .setLogFileMaxSaveDays(-1)
                .setFileWriter(null)
                .setOnConsoleOutputListener(null)
                .setOnFileOutputListener(null)
                .addFileExtraHead("key", "value")
                .setBodyStackDeep(1)
                .setHeadStackDeep(1)
                .setHeadStackOffset(0);
        LogUtil.i("config:" + config.toString());
    }
}
