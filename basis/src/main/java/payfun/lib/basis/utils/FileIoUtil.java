package payfun.lib.basis.utils;

import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

/**
 * @author : 时光
 * e-mail : qurongzhen@pay.media
 * date   : 2021/5/6 13:56
 * desc   : <流读写到文件>
 */
public final class FileIoUtil {

    private static int sBufferSize = 524288;

    private FileIoUtil() {
        throw new UnsupportedOperationException(" can't instantiate FileIoUtil");
    }

    ///////////////////////////////////////////////////////////////////////////
    //region writeFileFromIS without progress
    ///////////////////////////////////////////////////////////////////////////

    public static boolean writeFileFromIS(final String filePath, final InputStream is) {
        return writeFileFromIS(FileUtil.getFileByPath(filePath), is, false, null);
    }

    public static boolean writeFileFromIS(final String filePath,
                                          final InputStream is,
                                          final boolean append) {
        return writeFileFromIS(FileUtil.getFileByPath(filePath), is, append, null);
    }

    public static boolean writeFileFromIS(final File file, final InputStream is) {
        return writeFileFromIS(file, is, false, null);
    }

    public static boolean writeFileFromIS(final File file,
                                          final InputStream is,
                                          final boolean append) {
        return writeFileFromIS(file, is, append, null);
    }

    public static boolean writeFileFromIS(final File file,
                                          final InputStream is,
                                          final boolean append,
                                          final OnProgressUpdateListener listener) {
        if (is == null || !FileUtil.createOrExistsFile(file)) {
            LogUtil.e("FileIoUtil", "create file <" + file + "> failed.");
            return false;
        }
        OutputStream os = null;
        try {
            os = new BufferedOutputStream(new FileOutputStream(file, append), sBufferSize);
            if (listener == null) {
                byte[] data = new byte[sBufferSize];
                for (int len; (len = is.read(data)) != -1; ) {
                    os.write(data, 0, len);
                }
            } else {
                double totalSize = is.available();
                int curSize = 0;
                listener.onProgressUpdate(0);
                byte[] data = new byte[sBufferSize];
                for (int len; (len = is.read(data)) != -1; ) {
                    os.write(data, 0, len);
                    curSize += len;
                    listener.onProgressUpdate(curSize / totalSize);
                }
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            CloseUtil.closeIO(os, is);
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    //endregion
    ///////////////////////////////////////////////////////////////////////////


    ///////////////////////////////////////////////////////////////////////////
    //region writeFileFromString
    ///////////////////////////////////////////////////////////////////////////


    public static boolean writeFileFromString(final String filePath, final String content) {
        return writeFileFromString(FileUtil.getFileByPath(filePath), content, false);
    }

    public static boolean writeFileFromString(final String filePath,
                                              final String content,
                                              final boolean append) {
        return writeFileFromString(FileUtil.getFileByPath(filePath), content, append);
    }

    public static boolean writeFileFromString(final File file, final String content) {
        return writeFileFromString(file, content, false);
    }


    /**
     * Write file from string.
     *
     * @param file    The file.
     * @param content The string of content.
     * @param append  True to append, false otherwise.
     * @return {@code true}: success<br>{@code false}: fail
     */
    public static boolean writeFileFromString(final File file,
                                              final String content,
                                              final boolean append) {
        if (file == null || content == null) {
            return false;
        }
        if (!FileUtil.createOrExistsFile(file)) {
            Log.e("FileIoUtil", "create file <" + file + "> failed.");
            return false;
        }
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(file, append));
            bw.write(content);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            CloseUtil.closeIO(bw);
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    //endregion
    ///////////////////////////////////////////////////////////////////////////


    ///////////////////////////////////////////////////////////////////////////
    //region readFile2String
    ///////////////////////////////////////////////////////////////////////////

    public static String readFile2String(final String filePath) {
        return readFile2String(FileUtil.getFileByPath(filePath), null);
    }

    public static String readFile2String(final String filePath, final String charsetName) {
        return readFile2String(FileUtil.getFileByPath(filePath), charsetName);
    }

    public static String readFile2String(final File file) {
        return readFile2String(file, null);
    }


    /**
     * Return the string in file.
     *
     * @param file        The file.
     * @param charsetName The name of charset.
     * @return the string in file
     */
    public static String readFile2String(final File file, final String charsetName) {
        byte[] bytes = readFile2BytesByStream(file);
        if (bytes == null) {
            return null;
        }
        if (TextUtils.isEmpty(charsetName)) {
            return new String(bytes);
        } else {
            try {
                return new String(bytes, charsetName);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                return "";
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    //endregion
    ///////////////////////////////////////////////////////////////////////////


    ///////////////////////////////////////////////////////////////////////////
    //region readFile2BytesByStream
    ///////////////////////////////////////////////////////////////////////////

    public static byte[] readFile2BytesByStream(final String filePath) {
        return readFile2BytesByStream(FileUtil.getFileByPath(filePath), null);
    }

    public static byte[] readFile2BytesByStream(final File file) {
        return readFile2BytesByStream(file, null);
    }

    public static byte[] readFile2BytesByStream(final String filePath,
                                                final OnProgressUpdateListener listener) {
        return readFile2BytesByStream(FileUtil.getFileByPath(filePath), listener);
    }

    /**
     * Return the bytes in file by stream.
     *
     * @param file     The file.
     * @param listener The progress update listener.
     * @return the bytes in file
     */
    public static byte[] readFile2BytesByStream(final File file,
                                                final OnProgressUpdateListener listener) {
        if (!FileUtil.isFileExists(file)) {
            return null;
        }
        try {
            ByteArrayOutputStream os = null;
            InputStream is = new BufferedInputStream(new FileInputStream(file), sBufferSize);
            try {
                os = new ByteArrayOutputStream();
                byte[] b = new byte[sBufferSize];
                int len;
                if (listener == null) {
                    while ((len = is.read(b, 0, sBufferSize)) != -1) {
                        os.write(b, 0, len);
                    }
                } else {
                    double totalSize = is.available();
                    int curSize = 0;
                    listener.onProgressUpdate(0);
                    while ((len = is.read(b, 0, sBufferSize)) != -1) {
                        os.write(b, 0, len);
                        curSize += len;
                        listener.onProgressUpdate(curSize / totalSize);
                    }
                }
                return os.toByteArray();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            } finally {
                CloseUtil.closeIO(is, os);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }


    ///////////////////////////////////////////////////////////////////////////
    //endregion
    ///////////////////////////////////////////////////////////////////////////


    public static void setBufferSize(final int bufferSize) {
        sBufferSize = bufferSize;
    }

    public interface OnProgressUpdateListener {
        /**
         * 更新进度
         *
         * @param progress
         */
        void onProgressUpdate(double progress);
    }
}
