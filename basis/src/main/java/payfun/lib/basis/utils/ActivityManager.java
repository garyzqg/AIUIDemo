package payfun.lib.basis.utils;

import android.app.Activity;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author : 时光
 * e-mail : qurongzhen@pay.media
 * date   : 2021/5/8 13:39
 * desc   : <activity管理器>
 */
public class ActivityManager {
    private static final String TAG = "ActivityManager";
    private static volatile ActivityManager instance = null;
    private static final LinkedList<Activity> mActivityList = new LinkedList<>();

    /**
     * 私有构造
     */
    private ActivityManager() {
    }

    /**
     * 单例实例
     *
     * @return ActivityManager
     */
    public static ActivityManager getInstance() {
        if (instance == null) {
            synchronized (ActivityManager.class) {
                if (instance == null) {
                    instance = new ActivityManager();
                }
            }
        }
        return instance;
    }

    List<Activity> getActivityList() {
        if (!mActivityList.isEmpty()) {
            return new LinkedList<>(mActivityList);
        }
        List<Activity> reflectActivities = getActivitiesByReflect();
        mActivityList.addAll(reflectActivities);
        return new LinkedList<>(mActivityList);
    }

    /**
     * @return the activities which topActivity is first position
     */
    private List<Activity> getActivitiesByReflect() {
        LinkedList<Activity> list = new LinkedList<>();
        Activity topActivity = null;
        try {
            Object activityThread = getActivityThread();
            Field mActivitiesField = activityThread.getClass().getDeclaredField("mActivities");
            mActivitiesField.setAccessible(true);
            Object mActivities = mActivitiesField.get(activityThread);
            if (!(mActivities instanceof Map)) {
                return list;
            }
            Map<Object, Object> binder_activityClientRecord_map = (Map<Object, Object>) mActivities;
            for (Object activityRecord : binder_activityClientRecord_map.values()) {
                Class activityClientRecordClass = activityRecord.getClass();
                Field activityField = activityClientRecordClass.getDeclaredField("activity");
                activityField.setAccessible(true);
                Activity activity = (Activity) activityField.get(activityRecord);
                if (topActivity == null) {
                    Field pausedField = activityClientRecordClass.getDeclaredField("paused");
                    pausedField.setAccessible(true);
                    if (!pausedField.getBoolean(activityRecord)) {
                        topActivity = activity;
                    } else {
                        list.add(activity);
                    }
                } else {
                    list.add(activity);
                }
            }
        } catch (Exception e) {
            Log.e("UtilsActivityLifecycle", "getActivitiesByReflect: " + e.getMessage());
        }
        if (topActivity != null) {
            list.addFirst(topActivity);
        }
        return list;
    }

    private Object getActivityThread() {
        Object activityThread = getActivityThreadInActivityThreadStaticField();
        if (activityThread != null) return activityThread;
        return getActivityThreadInActivityThreadStaticMethod();
    }

    private Object getActivityThreadInActivityThreadStaticField() {
        try {
            Class activityThreadClass = Class.forName("android.app.ActivityThread");
            Field sCurrentActivityThreadField = activityThreadClass.getDeclaredField("sCurrentActivityThread");
            sCurrentActivityThreadField.setAccessible(true);
            return sCurrentActivityThreadField.get(null);
        } catch (Exception e) {
            Log.e("UtilsActivityLifecycle", "getActivityThreadInActivityThreadStaticField: " + e.getMessage());
            return null;
        }
    }

    private Object getActivityThreadInActivityThreadStaticMethod() {
        try {
            Class activityThreadClass = Class.forName("android.app.ActivityThread");
            return activityThreadClass.getMethod("currentActivityThread").invoke(null);
        } catch (Exception e) {
            Log.e("UtilsActivityLifecycle", "getActivityThreadInActivityThreadStaticMethod: " + e.getMessage());
            return null;
        }
    }

    /**
     * 压入栈顶
     *
     * @param activity 界面
     */
    public synchronized void addActivity(Activity activity) {
        if (mActivityList.contains(activity)) {
            if (!mActivityList.getFirst().equals(activity)) {
                mActivityList.remove(activity);
                mActivityList.addFirst(activity);
            }
        } else {
            mActivityList.addFirst(activity);
        }
    }

    /**
     * 移除指定activity
     *
     * @param activity 界面
     */
    public synchronized void removeActivity(Activity activity) {
        mActivityList.remove(activity);
    }

    /**
     * 获取栈中activity的数量
     *
     * @return 栈中activity的数量，-1表示为null异常
     */
    public int getActivitySize() {
        return mActivityList.size();
    }

    /**
     * 获取当前Activity（堆栈中最后一个压入的）
     * <p>注意：该值在当前只添加移除的模式实际并不准确，需要再在onResume中进行处理
     *
     * @return 界面
     */
    @Nullable
    public Activity getTopActivity() {
        List<Activity> activityList = getActivityList();
        for (Activity activity : activityList) {
            if (!isActivityAlive(activity)) {
                continue;
            }
            return activity;
        }
        return null;
    }


    public void finishActivity(@NonNull final Activity activity) {
        finishActivity(activity, false);
    }

    /**
     * 结束任意一个activity
     *
     * @param activity   要finish的activity
     * @param isLoadAnim True表示对传出活动使用动画，否则为false。
     */
    public void finishActivity(@NonNull final Activity activity, final boolean isLoadAnim) {
        activity.finish();
        if (!isLoadAnim) {
            activity.overridePendingTransition(0, 0);
        }
    }

    public void finishActivity(@NonNull final Class<? extends Activity> clz) {
        finishActivity(clz, false);
    }

    public void finishActivity(@NonNull final Class<? extends Activity> clz,
                               final boolean isLoadAnim) {
        List<Activity> activityList = getActivityList();
        for (Activity activity : activityList) {
            if (activity.getClass().equals(clz)) {
                finishActivity(activity, isLoadAnim);
            }
        }
    }


    /**
     * 清除除了传入之外的其他activity
     *
     * @param activity 界面
     */
    public void finishAllButNotMe(Activity activity) {
        finishAllButNotMe(activity, false);
    }

    public void finishAllButNotMe(Activity activity, final boolean isLoadAnim) {
        List<Activity> activityList = getActivityList();
        for (Activity act : activityList) {
            if (act != activity) {
                finishActivity(act, isLoadAnim);
            }
        }
    }

    /**
     * 清楚除了指定类型的activity之外的activity
     *
     * @param classes 界面类型
     */
    public void finishAllButNotClass(Class<?> classes) {
        finishAllButNotClass(classes, false);
    }

    public void finishAllButNotClass(Class<?> classes, final boolean isLoadAnim) {
        List<Activity> activityList = getActivityList();
        for (Activity act : activityList) {
            if (!act.getClass().equals(classes)) {
                finishActivity(act, isLoadAnim);
            }
        }
    }

    /**
     * 结束所有Activity
     */
    public void finishAllActivities() {
        finishAllActivities(false);
    }

    /**
     * 结束所有Activity
     */
    public void finishAllActivities(final boolean isLoadAnim) {
        List<Activity> activityList = getActivityList();
        for (Activity act : activityList) {
            // sActivityList remove the index activity at onActivityDestroyed
            finishActivity(act, isLoadAnim);
        }
    }


    /**
     * 退出应用程序
     */
    public void exitApp() {
        try {
            finishAllActivities();
            mActivityList.clear();
        } catch (Exception e) {
            mActivityList.clear();
            LogUtil.e(TAG, "重启失败", e);
        } finally {
            //正常退出
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(0);
        }
    }


    /**
     * Return whether the activity is alive.
     *
     * @param activity The activity.
     * @return {@code true}: yes<br>{@code false}: no
     */
    public static boolean isActivityAlive(final Activity activity) {
        return activity != null && !activity.isFinishing()
                && (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1 || !activity.isDestroyed());
    }
}
