package payfun.lib.basis.utils;

import android.content.ContentResolver;
import android.content.res.AssetFileDescriptor;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * @author : 时光
 * e-mail : qurongzhen@pay.media
 * date   : 2021/5/6 13:56
 * desc   : <功能简述>
 */
public final class FileUtil {
    private static final String LINE_SEP = System.getProperty("line.separator");

    private FileUtil() {
        throw new UnsupportedOperationException(" can't instantiate FileUtil");
    }


    public static File getFileByPath(final String filePath) {
        return TextUtils.isEmpty(filePath) ? null : new File(filePath);
    }


    public static boolean createOrExistsFile(final File file) {
        if (file == null) {
            return false;
        }
        if (file.exists()) {
            return file.isFile();
        }
        if (!createOrExistsDir(file.getParentFile())) {
            return false;
        }
        try {
            return file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean createOrExistsDir(final File file) {
        return file != null && (file.exists() ? file.isDirectory() : file.mkdirs());
    }


    public static boolean isFileExists(final File file) {
        if (file == null) {
            return false;
        }
        if (file.exists()) {
            return true;
        }
        return isFileExists(file.getAbsolutePath());
    }


    public static boolean isFileExists(final String filePath) {
        File file = getFileByPath(filePath);
        if (file == null) {
            return false;
        }
        if (file.exists()) {
            return true;
        }
        return isFileExistsApi29(filePath);
    }


    private static boolean isFileExistsApi29(String filePath) {
        if (Build.VERSION.SDK_INT >= 29) {
            try {
                Uri uri = Uri.parse(filePath);
                ContentResolver cr = InitUtil.getAppContext().getContentResolver();
                AssetFileDescriptor afd = cr.openAssetFileDescriptor(uri, "r");
                if (afd == null) {
                    return false;
                }
                try {
                    afd.close();
                } catch (IOException ignore) {
                }
            } catch (FileNotFoundException e) {
                return false;
            }
            return true;
        }
        return false;
    }

    public static boolean delete(final String filePath) {
        return delete(getFileByPath(filePath));
    }

    /**
     * 删除
     *
     * @param file The file.
     * @return {@code true}: success<br>{@code false}: fail
     */
    public static boolean delete(final File file) {
        if (file == null) {
            return false;
        }
        if (file.isDirectory()) {
            return deleteDir(file);
        }
        return deleteFile(file);
    }


    /**
     * 删除目录及下文件
     *
     * @param dir The directory.
     * @return {@code true}: success<br>{@code false}: fail
     */
    private static boolean deleteDir(final File dir) {
        if (dir == null) {
            return false;
        }
        // dir doesn't exist then return true
        if (!dir.exists()) {
            return true;
        }
        // dir isn't a directory then return false
        if (!dir.isDirectory()) {
            return false;
        }
        File[] files = dir.listFiles();
        if (files != null && files.length > 0) {
            for (File file : files) {
                if (file.isFile()) {
                    if (!file.delete()) {
                        return false;
                    }
                } else if (file.isDirectory()) {
                    if (!deleteDir(file)) {
                        return false;
                    }
                }
            }
        }
        return dir.delete();
    }


    /**
     * 删除文件.
     *
     * @param file The file.
     * @return {@code true}: success<br>{@code false}: fail
     */
    private static boolean deleteFile(final File file) {
        return file != null && (!file.exists() || file.isFile() && file.delete());
    }
}
