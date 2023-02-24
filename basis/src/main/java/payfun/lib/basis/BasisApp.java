package payfun.lib.basis;

import android.app.Application;
import android.content.ComponentCallbacks2;
import android.content.Context;

import androidx.multidex.MultiDex;

import payfun.lib.basis.utils.InitUtil;

/**
 * @author : 时光
 * e-mail : qurongzhen@pay.media
 * date   : 2021/5/6 11:32
 * desc   : <BasisApp：可参照本Application进行仿写>
 */
public class BasisApp extends Application {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        InitUtil.init(this);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        if (level > ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN) {
            //后台进程
            //表示应用程序所有UI界面被隐藏了，即用户点击了Home键或者Back键导致引用的UI界面不可见。应释放一些资源。
        } else if (level > ComponentCallbacks2.TRIM_MEMORY_RUNNING_MODERATE) {
            //前台RUNNING进程
            //表示应用程序正常运行，并且不会被杀掉。但是目前手机的内存已经有点低了，系统可能会开始根据LRU缓存规则来去杀死进程了。
        } else if (level > ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW) {
            //前台RUNNING进程
            //表示应用程序正常运行，并且不会被杀掉。但是目前手机的内存已经非常低了，我们应该去释放掉一些不必要的资源以提升系统的性能，
            // 同时这也会直接影响到我们应用程序的性能。
        } else if (level > ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL) {
            //前台RUNNING进程
            //表示应用程序仍然正常运行，但是系统已经根据LRU缓存规则杀掉了大部分缓存的进程了。这个时候我们应当尽可能地去释放任何不必要的资源，
            // 不然的话系统可能会继续杀掉所有缓存中的进程，并且开始杀掉一些本来应当保持运行的进程，比如说后台运行的服务。
        } else if (level > ComponentCallbacks2.TRIM_MEMORY_BACKGROUND) {
            //后台进程
            //当应用程序是缓存的 : 表示手机目前内存已经很低了，系统准备开始根据LRU缓存来清理进程。这个时候我们的程序在LRU缓存列表的最近位置，是不太可能被清理掉的，
            // 但这时去释放掉一些比较容易恢复的资源能够让手机的内存变得比较充足，从而让我们的程序更长时间地保留在缓存当中，
            // 这样当用户返回我们的程序时会感觉非常顺畅，而不是经历了一次重新启动的过程。
        } else if (level > ComponentCallbacks2.TRIM_MEMORY_MODERATE) {
            //后台进程
            //当应用程序是缓存的 : 表示手机目前内存已经很低了，并且我们的程序处于LRU缓存列表的中间位置，如果手机内存还得不到进一步释放的话，那么我们的程序就有被系统杀掉的风险了。
        } else if (level > ComponentCallbacks2.TRIM_MEMORY_COMPLETE) {
            //后台进程
            //当应用程序是缓存的 : 表示手机目前内存已经很低了，并且我们的程序处于LRU缓存列表的最边缘位置，系统会最优先考虑杀掉我们的应用程序，
            // 在这个时候应当尽可能地把一切可以释放的东西都进行释放。相当于onLowMemory
        }

    }
}
