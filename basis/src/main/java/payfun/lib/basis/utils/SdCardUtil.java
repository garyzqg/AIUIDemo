package payfun.lib.basis.utils;

import android.content.Context;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.text.format.Formatter;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * @author : 时光
 * e-mail : qurongzhen@pay.media
 * date   : 2021/5/6 16:55
 * desc   : <SD卡工具类>
 */
public final class SdCardUtil {

    /**
     * Return whether sdcard is enabled by environment.
     *
     * @return {@code true}: enabled<br>{@code false}: disabled
     */
    public static boolean isSdCardEnableByEnvironment() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }


    /**
     * Return the path of sdcard by environment.
     *
     * @return the path of sdcard by environment
     */
    public static String getSdCardPathByEnvironment() {
        if (isSdCardEnableByEnvironment()) {
            return Environment.getExternalStorageDirectory().getAbsolutePath();
        }
        return "";
    }

    /**
     * Return the information of sdcard.
     *
     * @return the information of sdcard
     */
    public static List<SdCardInfo> getSdCardInfo() {
        List<SdCardInfo> paths = new ArrayList<>();
        StorageManager sm = (StorageManager) InitUtil.getAppContext().getSystemService(Context.STORAGE_SERVICE);
        if (sm == null) {
            return paths;
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            List<StorageVolume> storageVolumes = sm.getStorageVolumes();
            try {
                //noinspection JavaReflectionMemberAccess
                Method getPathMethod = StorageVolume.class.getMethod("getPath");
                for (StorageVolume storageVolume : storageVolumes) {
                    boolean isRemovable = storageVolume.isRemovable();
                    String state = storageVolume.getState();
                    String path = (String) getPathMethod.invoke(storageVolume);
                    paths.add(new SdCardInfo(path, state, isRemovable));
                }
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        } else {
            try {
                Class<?> storageVolumeClazz = Class.forName("android.os.storage.StorageVolume");
                //noinspection JavaReflectionMemberAccess
                Method getPathMethod = storageVolumeClazz.getMethod("getPath");
                Method isRemovableMethod = storageVolumeClazz.getMethod("isRemovable");
                //noinspection JavaReflectionMemberAccess
                Method getVolumeStateMethod = StorageManager.class.getMethod("getVolumeState", String.class);
                //noinspection JavaReflectionMemberAccess
                Method getVolumeListMethod = StorageManager.class.getMethod("getVolumeList");
                Object result = getVolumeListMethod.invoke(sm);
                final int length = Array.getLength(result);
                for (int i = 0; i < length; i++) {
                    Object storageVolumeElement = Array.get(result, i);
                    String path = (String) getPathMethod.invoke(storageVolumeElement);
                    boolean isRemovable = (Boolean) isRemovableMethod.invoke(storageVolumeElement);
                    String state = (String) getVolumeStateMethod.invoke(sm, path);
                    paths.add(new SdCardInfo(path, state, isRemovable));
                }
            } catch (ClassNotFoundException | InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return paths;
    }


    public static class SdCardInfo {

        private String path;
        private String state;
        private boolean isRemovable;
        private long totalSize;
        private long availableSize;

        SdCardInfo(String path, String state, boolean isRemovable) {
            this.path = path;
            this.state = state;
            this.isRemovable = isRemovable;
//            this.totalSize = UtilsBridge.getFsTotalSize(path);
//            this.availableSize = UtilsBridge.getFsAvailableSize(path);
        }

        public String getPath() {
            return path;
        }

        public String getState() {
            return state;
        }

        public boolean isRemovable() {
            return isRemovable;
        }

        public long getTotalSize() {
            return totalSize;
        }

        public long getAvailableSize() {
            return availableSize;
        }

        @Override
        public String toString() {
            return "SdCardInfo {" +
                    "path = " + path +
                    ", state = " + state +
                    ", isRemovable = " + isRemovable +
                    ", totalSize = " + Formatter.formatFileSize(InitUtil.getAppContext(), totalSize) +
                    ", availableSize = " + Formatter.formatFileSize(InitUtil.getAppContext(), availableSize) +
                    '}';
        }
    }
}
