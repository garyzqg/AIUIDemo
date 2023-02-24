package payfun.lib.basis.utils;

import androidx.annotation.NonNull;

/**
 * @author : 时光
 * e-mail : qurongzhen@pay.media
 * date   : 2021/5/21 15:49
 * desc   : <p>崩溃收集工具类
 */
public final class CrashUtil {


    private static final Thread.UncaughtExceptionHandler DEFAULT_UNCAUGHT_EXCEPTION_HANDLER = Thread.getDefaultUncaughtExceptionHandler();


    public static void init(final OnCrashListener onCrashListener) {
        Thread.setDefaultUncaughtExceptionHandler(getUncaughtExceptionHandler(onCrashListener));
    }


    private static Thread.UncaughtExceptionHandler getUncaughtExceptionHandler(
            final OnCrashListener onCrashListener) {
        return new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(@NonNull final Thread t, @NonNull final Throwable e) {
                if (onCrashListener != null) {
                    onCrashListener.onCrash(e);
                }
                if (DEFAULT_UNCAUGHT_EXCEPTION_HANDLER != null) {
                    DEFAULT_UNCAUGHT_EXCEPTION_HANDLER.uncaughtException(t, e);
                }
            }
        };
    }


    public interface OnCrashListener {
        void onCrash(Throwable crashInfo);
    }
}
