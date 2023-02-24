package payfun.lib.basis.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowManager;

import androidx.core.content.FileProvider;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

/**
 * @author : 时光
 * e-mail : qurongzhen@pay.media
 * date   : 2022/5/24 13:36
 * desc   : <安卓系统型工具类>
 */
public class AppUtil {

    /**
     * 是否是主进程
     *
     * @param application 环境
     * @return true=主进程；false=非主进程
     */
    public static boolean isMainProcess(Application application) {
        try {
            int pid = android.os.Process.myPid();
            String process = getProcessName(application, pid);
            return process.equalsIgnoreCase(application.getApplicationInfo().processName);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 根据pid获取应用进程名称
     *
     * @param context 环境
     * @param pid     pid
     * @return 应用进程名称
     */
    public static String getProcessName(Context context, int pid) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader("/proc/" + pid + "/cmdline"));
            String processName = reader.readLine();
            if (!TextUtils.isEmpty(processName)) {
                processName = processName.trim();
            }
            return processName;
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
        return "";
    }

    /**
     * 隐藏顶部状态栏
     *
     * @param activity 界面
     */
    public static void hideTopStatusBar(Activity activity) {
        if (activity == null) {
            return;
        }
        Window window = activity.getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.getInsetsController().hide(WindowInsets.Type.statusBars());
        } else {
            window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }

    /**
     * 隐藏底部虚拟导航栏
     *
     * @param activity 界面
     */
    public static void hideBottomNavigationBar(Activity activity) {
        if (activity == null) {
            return;
        }
        Window window = activity.getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.getInsetsController().hide(WindowInsets.Type.navigationBars());
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WindowManager.LayoutParams params = window.getAttributes();
            params.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE;
            window.setAttributes(params);
        } else {
            View decorView = activity.getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.GONE);
        }
    }


    /**
     * Install the app.
     * <p>Target APIs greater than 25 must hold
     * {@code <uses-permission android:name="android.permission.REQUEST_INSTALL_PACKAGES" />}</p>
     *
     * @param filePath The path of file.
     */
    public static void installApp(final String filePath) {
        installApp(FileUtil.getFileByPath(filePath));
    }

    /**
     * Install the app.
     * <p>Target APIs greater than 25 must hold
     * {@code <uses-permission android:name="android.permission.REQUEST_INSTALL_PACKAGES" />}</p>
     *
     * @param file The file.
     */
    public static void installApp(final File file) {
        if (!FileUtil.isFileExists(file)) {
            return;
        }

        Uri uri;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            uri = Uri.fromFile(file);
        } else {
            String authority = InitUtil.getPackageName() + ".pm.provider";
            uri = FileProvider.getUriForFile(InitUtil.getAppContext(), authority, file);
        }
        if (uri == null) {
            return;
        }
        Intent installAppIntent = new Intent(Intent.ACTION_VIEW);
        String type = "application/vnd.android.package-archive";
        installAppIntent.setDataAndType(uri, type);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            installAppIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
        installAppIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        InitUtil.getAppContext().startActivity(installAppIntent);
    }

    /**
     * Install the app.
     * <p>Target APIs greater than 25 must hold
     * {@code <uses-permission android:name="android.permission.REQUEST_INSTALL_PACKAGES" />}</p>
     *
     * @param uri The uri.
     */
    public static void installApp(final Uri uri) {
        if (uri == null) {
            return;
        }
        Intent installAppIntent = new Intent(Intent.ACTION_VIEW);
        String type = "application/vnd.android.package-archive";
        installAppIntent.setDataAndType(uri, type);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            installAppIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
        installAppIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        InitUtil.getAppContext().startActivity(installAppIntent);
    }

    /**
     * Uninstall the app.
     * <p>Target APIs greater than 25 must hold
     * Must hold {@code <uses-permission android:name="android.permission.REQUEST_DELETE_PACKAGES" />}</p>
     *
     * @param packageName The name of the package.
     */
    public static void uninstallApp(final String packageName) {
        if (TextUtils.isEmpty(packageName)) {
            return;
        }
        Intent intent = new Intent(Intent.ACTION_DELETE);
        intent.setData(Uri.parse("package:" + packageName));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        InitUtil.getAppContext().startActivity(intent);
    }


    /**
     * Return whether application is running.
     *
     * @param pkgName The name of the package.
     * @return {@code true}: yes<br>{@code false}: no
     */
    public static boolean isAppRunning(final String pkgName) {
        if (TextUtils.isEmpty(pkgName)) {
            return false;
        }
        android.app.ActivityManager am = (android.app.ActivityManager) InitUtil.getAppContext().getSystemService(Context.ACTIVITY_SERVICE);
        if (am != null) {
            List<android.app.ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(Integer.MAX_VALUE);
            if (taskInfo != null && taskInfo.size() > 0) {
                for (android.app.ActivityManager.RunningTaskInfo aInfo : taskInfo) {
                    if (aInfo.baseActivity != null) {
                        if (pkgName.equals(aInfo.baseActivity.getPackageName())) {
                            return true;
                        }
                    }
                }
            }
            List<android.app.ActivityManager.RunningServiceInfo> serviceInfo = am.getRunningServices(Integer.MAX_VALUE);
            if (serviceInfo != null && serviceInfo.size() > 0) {
                for (ActivityManager.RunningServiceInfo aInfo : serviceInfo) {
                    if (pkgName.equals(aInfo.service.getPackageName())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
