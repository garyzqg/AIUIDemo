package payfun.lib.basis.utils;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Formatter;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import androidx.annotation.IntDef;
import androidx.annotation.IntRange;
import androidx.annotation.RequiresApi;

/**
 * @author : zhangqg
 * date   : 2021/5/6 13:58
 * desc   : <日志>
 */
public final class LogUtil {
    public static final int V = Log.VERBOSE;
    public static final int D = Log.DEBUG;
    public static final int I = Log.INFO;
    public static final int W = Log.WARN;
    public static final int E = Log.ERROR;
    public static final int A = Log.ASSERT;


    @IntDef({V, D, I, W, E, A})
    @Retention(RetentionPolicy.SOURCE)
    private @interface TYPE {
    }

    private static final char[] T = new char[]{'V', 'D', 'I', 'W', 'E', 'A'};

    private static final int FILE = 0x10;
    private static final int JSON = 0x20;
    private static final int XML = 0x30;

    private static final String OPEN_BRACE = "{";
    private static final String OPEN_BRACKET = "[";
    private static final String TOP_CORNER = "┌";
    private static final String MIDDLE_CORNER = "├";
    private static final String LEFT_BORDER = "│ ";
    private static final String BOTTOM_CORNER = "└";
    private static final String SIDE_DIVIDER = "──────────";
    private static final String MIDDLE_DIVIDER = "┄┄┄┄┄┄┄┄┄┄";
    private static final String TOP_BORDER = TOP_CORNER + SIDE_DIVIDER + SIDE_DIVIDER;
    private static final String MIDDLE_BORDER = MIDDLE_CORNER + MIDDLE_DIVIDER + MIDDLE_DIVIDER;
    private static final String BOTTOM_BORDER = BOTTOM_CORNER + SIDE_DIVIDER + SIDE_DIVIDER;
    private static final int MAX_LEN = 4000;
    @SuppressLint({"SimpleDateFormat", "ConstantLocale"})
    private static final Format FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS ", Locale.getDefault());
    private static final String NOTHING = "log nothing";
    private static final String NULL = "null";
    private static final String ARGS = "args";
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");
    private static final Config CONFIG = new Config();

    private static final ExecutorService sExecutor = Executors.newSingleThreadExecutor();

    private LogUtil() {
        throw new UnsupportedOperationException("do not instantiation me" + "（LogUtil）...");
    }

    public static Config getConfig() {
        return CONFIG;
    }

    public static void v(final Object... contents) {
        log(V, CONFIG.getGlobalTag(), contents);
    }

    public static void vTag(final String tag, final Object... contents) {
        log(V, tag, contents);
    }

    public static void d(final Object... contents) {
        log(D, CONFIG.getGlobalTag(), contents);
    }

    public static void dTag(final String tag, final Object... contents) {
        log(D, tag, contents);
    }

    public static void i(final Object... contents) {
        log(I, CONFIG.getGlobalTag(), contents);
    }

    public static void iTag(final String tag, final Object... contents) {
        log(I, tag, contents);
    }

    public static void w(final Object... contents) {
        log(W, CONFIG.getGlobalTag(), contents);
    }

    public static void wTag(final String tag, final Object... contents) {
        log(W, tag, contents);
    }

    public static void e(final Object... contents) {
        log(E, CONFIG.getGlobalTag(), contents);
    }

    public static void eTag(final String tag, final Object... contents) {
        log(E, tag, contents);
    }

    public static void a(final Object... contents) {
        log(A, CONFIG.getGlobalTag(), contents);
    }

    public static void aTag(final String tag, final Object... contents) {
        log(A, tag, contents);
    }

    public static void file(final Object content) {
        log(FILE | D, CONFIG.getGlobalTag(), content);
    }

    public static void file(@TYPE final int type, final Object content) {
        log(FILE | type, CONFIG.getGlobalTag(), content);
    }

    public static void file(final String tag, final Object content) {
        log(FILE | D, tag, content);
    }

    public static void file(@TYPE final int type, final String tag, final Object content) {
        log(FILE | type, tag, content);
    }

    public static void json(final String content) {
        log(JSON | D, CONFIG.getGlobalTag(), content);
    }

    public static void json(@TYPE final int type, final String content) {
        log(JSON | type, CONFIG.getGlobalTag(), content);
    }

    public static void json(final String tag, final String content) {
        log(JSON | D, tag, content);
    }

    public static void json(@TYPE final int type, final String tag, final String content) {
        log(JSON | type, tag, content);
    }

    public static void xml(final String content) {
        log(XML | D, CONFIG.getGlobalTag(), content);
    }

    public static void xml(@TYPE final int type, final String content) {
        log(XML | type, CONFIG.getGlobalTag(), content);
    }

    public static void xml(final String tag, final String content) {
        log(XML | D, tag, content);
    }

    public static void xml(@TYPE final int type, final String tag, final String content) {
        log(XML | type, tag, content);
    }

    private static void log(final int type, final String tag, final Object... contents) {
        if (!CONFIG.mLogSwitch) {
            return;
        }
        final int typeLow = type & 0x0f, typeHigh = type & 0xf0;
        if (typeLow < CONFIG.mConsoleFilter && typeLow < CONFIG.mFileFilter) {
            return;
        }
        final TagHead tagHead = processTagAndHead(tag);
        final String body = processBody(typeHigh, contents);
        if (CONFIG.mLog2ConsoleSwitch && typeLow >= CONFIG.mConsoleFilter && typeHigh != FILE) {
            print2Console(typeLow, tagHead.tag, tagHead.consoleHead, body);
        }
        if ((CONFIG.mLog2FileSwitch || typeHigh == FILE) && typeLow >= CONFIG.mFileFilter) {
            sExecutor.execute(() -> print2File(typeLow, tagHead.tag, tagHead.fileHead + body));
        }
    }

    //region 组装头部

    private static TagHead processTagAndHead(String tag) {
        if (!CONFIG.mTagIsSpace && !CONFIG.mLogHeadSwitch) {
            tag = CONFIG.mGlobalTag;
        } else {
            final StackTraceElement[] stackTrace = new Throwable().getStackTrace();
            final int stackDeep = (3 + CONFIG.mHeadStackOffset) >= stackTrace.length ? 3 : (3 + CONFIG.mHeadStackOffset);
            StackTraceElement targetElement = stackTrace[stackDeep];
            final String fileName = getFileName(targetElement);
            if (CONFIG.mTagIsSpace && TextUtils.isEmpty(tag)) {
                // 混淆可能导致文件名被改变从而找不到"."
                int index = fileName.indexOf('.');
                tag = index == -1 ? fileName : fileName.substring(0, index);
            }
            if (CONFIG.mLogHeadSwitch) {
                String tName = Thread.currentThread().getName();
                final String head = new Formatter()
                        .format("%s, %s(%s:%d)",
                                tName,
                                targetElement.getMethodName(),
                                fileName,
                                targetElement.getLineNumber())
                        .toString();
                final String fileHead = " [" + head + "]: ";
                if (CONFIG.mHeadStackDeep <= 1) {
                    return new TagHead(tag, new String[]{head}, fileHead);
                } else {
                    final String[] consoleHead =
                            new String[Math.min(CONFIG.mHeadStackDeep, stackTrace.length - stackDeep)];
                    consoleHead[0] = head;
                    int spaceLen = tName.length() + 2;
                    String space = new Formatter().format("%" + spaceLen + "s", "").toString();
                    for (int i = 1, len = consoleHead.length; i < len; ++i) {
                        targetElement = stackTrace[i + stackDeep];
                        consoleHead[i] = new Formatter()
                                .format("%s%s(%s:%d)",
                                        space,
                                        targetElement.getMethodName(),
                                        getFileName(targetElement),
                                        targetElement.getLineNumber())
                                .toString();
                    }
                    return new TagHead(tag, consoleHead, fileHead);
                }
            }
        }
        return new TagHead(tag, null, ": ");
    }

    private static String getFileName(final StackTraceElement targetElement) {
        String fileName = targetElement.getFileName();
        if (fileName != null) {
            return fileName;
        }
        // 混淆可能会导致获取为空 加-keep attributes SourceFile,LineNumberTable
        String className = targetElement.getClassName();
        String[] classNameInfo = className.split("\\.");
        if (classNameInfo.length > 0) {
            className = classNameInfo[classNameInfo.length - 1];
        }
        int index = className.indexOf('$');
        if (index != -1) {
            className = className.substring(0, index);
        }
        return className + ".java";
    }

    //endregion 组装头部

    //region 组装身体及格式化输出内容的方法

    private static String processBody(final int type, final Object... contents) {
        String body = NULL;
        if (contents != null) {
            if (contents.length == 1) {
                body = formatObject(type, contents[0]);
            } else {
                StringBuilder sb = new StringBuilder();
                for (int i = 0, len = contents.length; i < len; ++i) {
                    Object content = contents[i];
                    sb.append(ARGS)
                            .append("[")
                            .append(i)
                            .append("]")
                            .append(" = ")
                            .append(formatObject(type, content))
                            .append(LINE_SEPARATOR);
                }
                body = sb.toString();
            }
        }
        return body.length() == 0 ? NOTHING : body;
    }

    private static String formatObject(int type, Object object) {
        if (object == null) {
            return NULL;
        }
        if (type == JSON) {
            return formatJson(object.toString());
        }
        if (type == XML) {
            return formatXml(object.toString());
        }
        return formatObject2String(object);
    }

    private static String formatObject2String(Object object) {
        if (object.getClass().isArray()) {
            return formatArray2String(object);
        }
        if (object instanceof Throwable) {
            return formatThrowable((Throwable) object);
        }
        if (object instanceof Bundle) {
            return formatBundle2String((Bundle) object);
        }
        if (object instanceof Intent) {
            return formatIntent2String((Intent) object);
        }
        return object.toString();
    }

    private static String formatArray2String(Object object) {
        if (object instanceof Object[]) {
            return Arrays.deepToString((Object[]) object);
        } else if (object instanceof boolean[]) {
            return Arrays.toString((boolean[]) object);
        } else if (object instanceof byte[]) {
            return Arrays.toString((byte[]) object);
        } else if (object instanceof char[]) {
            return Arrays.toString((char[]) object);
        } else if (object instanceof double[]) {
            return Arrays.toString((double[]) object);
        } else if (object instanceof float[]) {
            return Arrays.toString((float[]) object);
        } else if (object instanceof int[]) {
            return Arrays.toString((int[]) object);
        } else if (object instanceof long[]) {
            return Arrays.toString((long[]) object);
        } else if (object instanceof short[]) {
            return Arrays.toString((short[]) object);
        }
        throw new IllegalArgumentException("Array has incompatible type: " + object.getClass());
    }

    private static String formatThrowable(Throwable throwable) {
        if (CONFIG.mBodyStackDeep == 0) {
            return throwable.toString();
        }
        if (CONFIG.mBodyStackDeep == 1) {
            return formatStackTraceElement(throwable);
        }

        StringBuilder bodyBuilder = new StringBuilder(128);
        final List<Throwable> throwableList = new ArrayList<>();
        while (throwable != null && !throwableList.contains(throwable)) {
            throwableList.add(throwable);
            throwable = throwable.getCause();
        }
        for (Throwable childThrowable : throwableList) {
            if (CONFIG.mBodyStackDeep == 2) {
                bodyBuilder.append(childThrowable.toString()).append("\n");
            } else {
                bodyBuilder.append(formatStackTraceElement(childThrowable)).append("\n");
            }
        }
        return bodyBuilder.toString();
    }

    private static String formatStackTraceElement(Throwable throwable) {
        StringBuilder bodyBuilder = new StringBuilder("Cause by：");
        bodyBuilder.append(throwable.toString()).append("\n");
        StackTraceElement[] stackTrace = throwable.getStackTrace();
        for (StackTraceElement stackTraceElement : stackTrace) {
            bodyBuilder.append(stackTraceElement.toString()).append("\n");
        }
        return bodyBuilder.toString();
    }

    private static String formatIntent2String(Intent intent) {
        StringBuilder sb = new StringBuilder(128);
        sb.append("Intent { ");
        boolean first = true;
        String mAction = intent.getAction();
        if (mAction != null) {
            sb.append("act=").append(mAction);
            first = false;
        }
        Set<String> mCategories = intent.getCategories();
        if (mCategories != null) {
            if (!first) {
                sb.append(' ');
            }
            first = false;
            sb.append("cat=[");
            boolean firstCategory = true;
            for (String c : mCategories) {
                if (!firstCategory) {
                    sb.append(',');
                }
                sb.append(c);
                firstCategory = false;
            }
            sb.append("]");
        }
        Uri mData = intent.getData();
        if (mData != null) {
            if (!first) {
                sb.append(' ');
            }
            first = false;
            sb.append("dat=").append(mData);
        }
        String mType = intent.getType();
        if (mType != null) {
            if (!first) {
                sb.append(' ');
            }
            first = false;
            sb.append("typ=").append(mType);
        }
        int mFlags = intent.getFlags();
        if (mFlags != 0) {
            if (!first) {
                sb.append(' ');
            }
            first = false;
            sb.append("flg=0x").append(Integer.toHexString(mFlags));
        }
        String mPackage = intent.getPackage();
        if (mPackage != null) {
            if (!first) {
                sb.append(' ');
            }
            first = false;
            sb.append("pkg=").append(mPackage);
        }
        ComponentName mComponent = intent.getComponent();
        if (mComponent != null) {
            if (!first) {
                sb.append(' ');
            }
            first = false;
            sb.append("cmp=").append(mComponent.flattenToShortString());
        }
        Rect mSourceBounds = intent.getSourceBounds();
        if (mSourceBounds != null) {
            if (!first) {
                sb.append(' ');
            }
            first = false;
            sb.append("bnds=").append(mSourceBounds.toShortString());
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            ClipData mClipData = intent.getClipData();
            if (mClipData != null) {
                if (!first) {
                    sb.append(' ');
                }
                first = false;
                formatClipData2String(mClipData, sb);
            }
        }
        Bundle mExtras = intent.getExtras();
        if (mExtras != null) {
            if (!first) {
                sb.append(' ');
            }
            first = false;
            sb.append("extras={");
            sb.append(formatBundle2String(mExtras));
            sb.append('}');
        }
        if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
            Intent mSelector = intent.getSelector();
            if (mSelector != null) {
                if (!first) {
                    sb.append(' ');
                }
                first = false;
                sb.append("sel={");
                sb.append(mSelector == intent ? "(this Intent)" : formatIntent2String(mSelector));
                sb.append("}");
            }
        }
        sb.append(" }");
        return sb.toString();
    }

    private static String formatBundle2String(Bundle bundle) {
        Iterator<String> iterator = bundle.keySet().iterator();
        if (!iterator.hasNext()) {
            return "Bundle {}";
        }
        StringBuilder sb = new StringBuilder(128);
        sb.append("Bundle { ");
        for (; ; ) {
            String key = iterator.next();
            Object value = bundle.get(key);
            sb.append(key).append('=');
            if (value instanceof Bundle) {
                sb.append(value == bundle ? "(this Bundle)" : formatBundle2String((Bundle) value));
            } else {
                sb.append(formatObject2String(value));
            }
            if (!iterator.hasNext()) {
                return sb.append(" }").toString();
            }
            sb.append(',').append(' ');
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private static void formatClipData2String(ClipData clipData, StringBuilder sb) {
        ClipData.Item item = clipData.getItemAt(0);
        if (item == null) {
            sb.append("ClipData.Item {}");
            return;
        }
        sb.append("ClipData.Item { ");
        String mHtmlText = item.getHtmlText();
        if (mHtmlText != null) {
            sb.append("H:");
            sb.append(mHtmlText);
            sb.append("}");
            return;
        }
        CharSequence mText = item.getText();
        if (mText != null) {
            sb.append("T:");
            sb.append(mText);
            sb.append("}");
            return;
        }
        Uri uri = item.getUri();
        if (uri != null) {
            sb.append("U:").append(uri);
            sb.append("}");
            return;
        }
        Intent intent = item.getIntent();
        if (intent != null) {
            sb.append("I:");
            sb.append(formatIntent2String(intent));
            sb.append("}");
            return;
        }
        sb.append("NULL");
        sb.append("}");
    }

    private static String formatJson(String json) {
        try {
            if (json.startsWith(OPEN_BRACE)) {
                json = new JSONObject(json).toString(4);
            } else if (json.startsWith(OPEN_BRACKET)) {
                json = new JSONArray(json).toString(4);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }

    private static String formatXml(String xml) {
        try {
            Source xmlInput = new StreamSource(new StringReader(xml));
            StreamResult xmlOutput = new StreamResult(new StringWriter());
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            transformer.transform(xmlInput, xmlOutput);
            xml = xmlOutput.getWriter().toString().replaceFirst(">", ">" + LINE_SEPARATOR);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return xml;
    }

    //endregion 组装身体及格式化输出内容的方法

    //region 输出到控制台

    private static void print2Console(final int type,
                                      final String tag,
                                      final String[] head,
                                      final String msg) {
        printBorder(type, tag, true);
        printHead(type, tag, head);
        printMsg(type, tag, msg);
        printBorder(type, tag, false);
    }

    private static void printBorder(final int type, final String tag, boolean isTop) {
        if (CONFIG.mLogBorderSwitch) {
            input2Console(type, tag, isTop ? TOP_BORDER : BOTTOM_BORDER);
        }
    }

    private static void printHead(final int type, final String tag, final String[] head) {
        if (head != null) {
            for (String aHead : head) {
                input2Console(type, tag, CONFIG.mLogBorderSwitch ? LEFT_BORDER + aHead : aHead);
            }
            if (CONFIG.mLogBorderSwitch) {
                input2Console(type, tag, MIDDLE_BORDER);
            }
        }
    }

    private static void printMsg(final int type, final String tag, final String msg) {
        int len = msg.length();
        int countOfSub = len / MAX_LEN;
        if (countOfSub > 0) {
            int index = 0;
            for (int i = 0; i < countOfSub; i++) {
                printSubMsg(type, tag, msg.substring(index, index + MAX_LEN));
                index += MAX_LEN;
            }
            if (index != len) {
                printSubMsg(type, tag, msg.substring(index, len));
            }
        } else {
            printSubMsg(type, tag, msg);
        }
    }

    private static void printSubMsg(final int type, final String tag, final String msg) {
        if (!CONFIG.mLogBorderSwitch) {
            input2Console(type, tag, msg);
            return;
        }
        String[] lines = msg.split(LINE_SEPARATOR);
        for (String line : lines) {
            input2Console(type, tag, LEFT_BORDER + line);
        }
    }

    private static void input2Console(int type, String tag, String msg) {
        Log.println(type, tag, msg);
        if (CONFIG.mOnConsoleOutputListener != null) {
            CONFIG.mOnConsoleOutputListener.onConsoleOutput(type, tag, msg);
        }
    }

    //endregion 输出到控制台

    //region 输出到文件

    private static void print2File(final int type, final String tag, final String msg) {
        Date now = new Date();
        String format = FORMAT.format(now);
        String yMd = format.substring(0, 10);
        String time = format.substring(11);
        final String fullPath =
                (CONFIG.getDir() == null ? CONFIG.mDefaultDir : CONFIG.getDir()) +
                        CONFIG.mFilePrefix + "-" + yMd + CONFIG.mFileExtension;
        if (!createOrExistsFile(fullPath, yMd)) {
            Log.e(tag, "log to " + fullPath + " failed!");
            return;
        }
        final String content = time +
                T[type - V] +
                "/" +
                tag +
                msg +
                LINE_SEPARATOR +
                MIDDLE_DIVIDER +
                LINE_SEPARATOR;
        input2File(content, fullPath);
    }

    private static boolean createOrExistsFile(final String filePath, final String date) {
        File file = new File(filePath);
        if (file.exists()) {
            return file.isFile();
        }
        if (!createOrExistsDir(file.getParentFile())) {
            return false;
        }
        try {
            deleteDueLogs(filePath, date);
            boolean isCreate = file.createNewFile();
            if (isCreate) {
                printDeviceInfo(filePath);
            }
            return isCreate;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static boolean createOrExistsDir(final File file) {
        return file != null && (file.exists() ? file.isDirectory() : file.mkdirs());
    }

    /**
     * 删除过期的日志
     *
     * @param filePath 文件路径
     * @param date     当前日期
     */
    private static void deleteDueLogs(final String filePath, final String date) {
        if (CONFIG.mLogFileMaxSaveDays <= 0) {//不删除，长久保存日志
            return;
        }
        File file = new File(filePath);
        File parentFile = file.getParentFile();
        File[] files = parentFile.listFiles((dir, name) -> isMatchLogFileName(name));
        if (files == null || files.length <= 0) {
            return;
        }
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        try {
            long dueMillis = sdf.parse(date).getTime() - CONFIG.mLogFileMaxSaveDays * 86400000L;
            for (final File aFile : files) {
                String name = aFile.getName();
                //int l = name.length();
                String logDay = findDate(name);
                if (sdf.parse(logDay).getTime() <= dueMillis) {
                    sExecutor.execute(() -> {
                        boolean delete = aFile.delete();
                        if (!delete) {
                            Log.e("LogUtil", "delete " + aFile + " failed!");
                        }
                    });
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static boolean isMatchLogFileName(String name) {
        return name.matches("^" + CONFIG.mFilePrefix + "-[0-9]{4}-[0-9]{2}-[0-9]{2}.*$");
    }

    private static String findDate(String str) {
        Pattern pattern = Pattern.compile("[0-9]{4}-[0-9]{2}-[0-9]{2}");
        Matcher matcher = pattern.matcher(str);
        if (matcher.find()) {
            return matcher.group();
        }
        return "";
    }

    private static void printDeviceInfo(final String filePath) {
        CONFIG.mFileHead.addFirst("Date of Log", FORMAT.format(new Date()));
        input2File(CONFIG.mFileHead.toString(), filePath);
    }

    private static void input2File(final String input, final String filePath) {
        if (CONFIG.mFileWriter == null) {
            BufferedWriter bw = null;
            try {
                bw = new BufferedWriter(new FileWriter(filePath, true));
                bw.write(input);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (bw != null) {
                        bw.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            CONFIG.mFileWriter.write(filePath, input);
        }
        if (CONFIG.mOnFileOutputListener != null) {
            CONFIG.mOnFileOutputListener.onFileOutput(filePath, input);
        }
    }

    //endregion 输出到文件

    /**
     * 日志配置类
     */
    public static class Config {

        /**
         * log 默认存储目录
         */
        private String mDefaultDir;
        /**
         * log 存储目录
         */
        private String mDir;
        /**
         * log 文件前缀
         */
        private String mFilePrefix = "Log";
        /**
         * log 文件后缀
         */
        private String mFileExtension = ".txt";
        /**
         * log 总开关，默认开
         */
        private boolean mLogSwitch = true;
        /**
         * logcat 是否输出到控制台，默认开
         */
        private boolean mLog2ConsoleSwitch = true;
        /**
         * log 全局标签
         */
        private String mGlobalTag = "";
        /**
         * log 标签是否为空白
         */
        private boolean mTagIsSpace = true;
        /**
         * log 头部开关，默认开
         */
        private boolean mLogHeadSwitch = true;
        /**
         * log 写入文件开关，默认关
         */
        private boolean mLog2FileSwitch = false;
        /**
         * log 边框开关，默认开
         */
        private boolean mLogBorderSwitch = true;
        /**
         * log 控制台过滤器
         */
        private int mConsoleFilter = V;
        /**
         * log 文件过滤器
         */
        private int mFileFilter = V;
        /**
         * log 头栈深度
         */
        private int mHeadStackDeep = 1;
        /**
         * log 头栈深度偏移
         */
        private int mHeadStackOffset = 0;
        /**
         * log 身栈是否开启全部，默认1。0：表示只拿第一层的简短原因；1：表示只开启最外层栈；2：表示开启全部身栈,但是只有简短原因；3：表示开启全身栈。
         */
        private int mBodyStackDeep = 1;
        /**
         * sd卡中日志文件的最多保存天数，默认7天;-1时表示不删除
         */
        private int mLogFileMaxSaveDays = 7;
        /**
         * sd卡中单个日志文件大小最大是多少，默认5MB
         */
        private long mLogFileMaxSize = 5 * 1024 * 1024;
        /**
         * sd卡中所有日志文件大小最大是多少，默认100MB
         */
        private long mLogAllFileMaxSize = 100 * 1024 * 1024;

        private IFileWriter mFileWriter;
        private OnConsoleOutputListener mOnConsoleOutputListener;
        private OnFileOutputListener mOnFileOutputListener;
        private final FileHead mFileHead = new FileHead("Log");


        private Config() {
            if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                    && InitUtil.getAppContext().getExternalCacheDir() != null) {
                mDefaultDir = InitUtil.getAppContext().getExternalCacheDir() + System.getProperty("file.separator") + "log" + System.getProperty("file.separator");
            } else {
                mDefaultDir = InitUtil.getAppContext().getCacheDir() + System.getProperty("file.separator") + "log" + System.getProperty("file.separator");
            }
        }

        /**
         * 设置 log 总开关，包括输出到控制台和文件，默认开
         *
         * @param logSwitch true:开启;    false:关闭;
         * @return Config
         */
        public final Config setLogSwitch(final boolean logSwitch) {
            mLogSwitch = logSwitch;
            return this;
        }

        /**
         * 设置是否输出到控制台开关，默认开
         *
         * @param consoleSwitch true:开启;    false:关闭;
         * @return Config
         */
        public final Config setConsoleSwitch(final boolean consoleSwitch) {
            mLog2ConsoleSwitch = consoleSwitch;
            return this;
        }

        /**
         * 设置 log 全局标签，默认为空
         * 当全局标签不为空时，我们输出的 log 全部为该 tag，
         * 如果传入的 tag 为空那就显示类名，否则显示 tag
         *
         * @param tag 标签
         * @return Config
         */
        public final Config setGlobalTag(final String tag) {
            if (TextUtils.isEmpty(tag)) {
                mGlobalTag = "";
                mTagIsSpace = true;
            } else {
                mGlobalTag = tag;
                mTagIsSpace = false;
            }
            return this;
        }

        /**
         * 设置 log 头信息开关，默认为开
         *
         * @param logHeadSwitch true:开启;    false:关闭;
         * @return Config
         */
        public final Config setLogHeadSwitch(final boolean logHeadSwitch) {
            mLogHeadSwitch = logHeadSwitch;
            return this;
        }

        /**
         * 打印 log 时是否存到文件的开关，默认关；
         *
         * @param log2FileSwitch true:开启;    false:关闭;
         * @return Config
         */
        public final Config setLog2FileSwitch(final boolean log2FileSwitch) {
            mLog2FileSwitch = log2FileSwitch;
            return this;
        }

        /**
         * 当自定义路径为空时，写入应用的/cache/log/目录中
         *
         * @param dir 文件写入路径
         * @return Config
         */
        public final Config setDir(final String dir) {
            if (TextUtils.isEmpty(dir)) {
                mDir = null;
            } else {
                mDir = dir.endsWith(LINE_SEPARATOR) ? dir : dir + LINE_SEPARATOR;
            }
            return this;
        }

        public final Config setDir(final File dir) {
            mDir = dir == null ? null : dir.getAbsolutePath() + LINE_SEPARATOR;
            return this;
        }

        /**
         * 当文件前缀为空时，默认为"Log"，即写入文件为"Log-MM-dd.txt"
         *
         * @param filePrefix 文件前缀
         * @return Config
         */
        public final Config setFilePrefix(final String filePrefix) {
            if (TextUtils.isEmpty(filePrefix)) {
                mFilePrefix = "Log";
            } else {
                mFilePrefix = filePrefix;
            }
            return this;
        }

        public final Config setFileExtension(final String fileExtension) {
            if (TextUtils.isEmpty(fileExtension)) {
                mFileExtension = ".txt";
            } else {
                if (fileExtension.startsWith(".")) {
                    mFileExtension = fileExtension;
                } else {
                    mFileExtension = "." + fileExtension;
                }
            }
            return this;
        }

        /**
         * 输出日志是否带边框开关，默认开
         *
         * @param borderSwitch true:开启;    false:关闭;
         * @return Config
         */
        public final Config setBorderSwitch(final boolean borderSwitch) {
            mLogBorderSwitch = borderSwitch;
            return this;
        }

        /**
         * log 的控制台过滤器，和 logcat 过滤器同理，默认 Verbose
         *
         * @param consoleFilter 控制台日志过滤级别
         * @return Config
         */
        public final Config setConsoleFilter(@TYPE final int consoleFilter) {
            mConsoleFilter = consoleFilter;
            return this;
        }

        /**
         * log 文件过滤器，和 logcat 过滤器同理，默认 Verbose
         *
         * @param fileFilter 文件日志过滤级别
         * @return Config
         */
        public final Config setFileFilter(@TYPE final int fileFilter) {
            mFileFilter = fileFilter;
            return this;
        }

        /**
         * log 栈深度，默认为 1
         *
         * @param stackDeep 堆栈的深度
         * @return Config
         */
        public final Config setHeadStackDeep(@IntRange(from = 1) final int stackDeep) {
            mHeadStackDeep = stackDeep;
            return this;
        }

        public final Config setHeadStackOffset(@IntRange(from = 0) final int stackOffset) {
            mHeadStackOffset = stackOffset;
            return this;
        }

        /**
         * 堆栈的深度
         *
         * @param stackDeep 0：表示只拿第一层的简短原因；1：表示只开启最外层栈；2：表示开启全部身栈,但是只有简短原因；3：表示开启全身栈。
         * @return Config
         */
        public final Config setBodyStackDeep(@IntRange(from = 0, to = 3) final int stackDeep) {
            mBodyStackDeep = stackDeep;
            return this;
        }

        /**
         * sd卡中日志文件的最多保存天数，默认7天
         *
         * @param days 默认7天,小于等于0则表示无限期
         * @return Config
         */
        public final Config setLogFileMaxSaveDays(final int days) {
            mLogFileMaxSaveDays = days;
            return this;
        }

        /**
         * sd卡中单个日志文件大小最大是多少
         *
         * @param fileMaxSize 默认5MB
         * @return Config
         */
        public final Config setLogFileMaxSize(final long fileMaxSize) {
            mLogFileMaxSize = fileMaxSize;
            return this;
        }

        /**
         * sd卡中所有日志文件大小最大是多少
         *
         * @param allFileMaxSize 默认100MB
         * @return Config
         */
        public final Config setLogAllFileMaxSize(final long allFileMaxSize) {
            mLogAllFileMaxSize = allFileMaxSize;
            return this;
        }

        public final Config setFileWriter(final IFileWriter fileWriter) {
            mFileWriter = fileWriter;
            return this;
        }

        public final Config setOnConsoleOutputListener(final OnConsoleOutputListener listener) {
            mOnConsoleOutputListener = listener;
            return this;
        }

        public final Config setOnFileOutputListener(final OnFileOutputListener listener) {
            mOnFileOutputListener = listener;
            return this;
        }

        public final Config addFileExtraHead(final Map<String, String> fileExtraHead) {
            mFileHead.append(fileExtraHead);
            return this;
        }

        public final Config addFileExtraHead(final String key, final String value) {
            mFileHead.append(key, value);
            return this;
        }

        @Override
        public String toString() {
            return "Config{"
                    + LINE_SEPARATOR + "logSwitch: " + mLogSwitch
                    + LINE_SEPARATOR + "consoleSwitch: " + mLog2ConsoleSwitch
                    + LINE_SEPARATOR + "tag: " + ("".equals(getGlobalTag()) ? "null" : getGlobalTag())
                    + LINE_SEPARATOR + "headSwitch: " + mLogHeadSwitch
                    + LINE_SEPARATOR + "fileSwitch: " + mLog2FileSwitch
                    + LINE_SEPARATOR + "dir: " + getDir()
                    + LINE_SEPARATOR + "filePrefix: " + mFilePrefix
                    + LINE_SEPARATOR + "borderSwitch: " + mLogBorderSwitch
                    + LINE_SEPARATOR + "consoleFilter: " + mConsoleFilter
                    + LINE_SEPARATOR + "fileFilter: " + mFileFilter
                    + LINE_SEPARATOR + "HeadStackDeep: " + mHeadStackDeep
                    + LINE_SEPARATOR + "HeadStackOffset: " + mHeadStackOffset
                    + LINE_SEPARATOR + "BodyStackDeep: " + mBodyStackDeep
                    + LINE_SEPARATOR + "fileMaxSaveDays: " + mLogFileMaxSaveDays
                    + LINE_SEPARATOR + "fileWriter: " + mFileWriter
                    + LINE_SEPARATOR + "onConsoleOutputListener: " + mOnConsoleOutputListener
                    + LINE_SEPARATOR + "onFileOutputListener: " + mOnFileOutputListener
                    + LINE_SEPARATOR + "fileExtraHeader: " + mFileHead.getAppended() +
                    '}';
        }


        public final String getDir() {
            return (TextUtils.isEmpty(mDir) ? mDefaultDir : mDir);
        }

        public final String getGlobalTag() {
            if (TextUtils.isEmpty(mGlobalTag)) {
                return "";
            }
            return mGlobalTag;
        }

    }

    public interface IFileWriter {
        void write(String file, String content);
    }

    public interface OnConsoleOutputListener {
        void onConsoleOutput(@TYPE int type, String tag, String content);
    }

    public interface OnFileOutputListener {
        /**
         * 可在此处处理文件缓存策略:文件数量，文件单个大小，文件总大小，文件时间，新建文件名称
         *
         * @param filePath 写入的文件的路径
         * @param content  写入的内容
         */
        void onFileOutput(String filePath, String content);
    }

    private static class TagHead {
        String tag;
        String[] consoleHead;
        String fileHead;

        TagHead(String tag, String[] consoleHead, String fileHead) {
            this.tag = tag;
            this.consoleHead = consoleHead;
            this.fileHead = fileHead;
        }
    }

    static final class FileHead {

        private String mName;
        private LinkedHashMap<String, String> mFirst = new LinkedHashMap<>();
        private LinkedHashMap<String, String> mLast = new LinkedHashMap<>();

        FileHead(String name) {
            mName = name;
        }

        void addFirst(String key, String value) {
            append2Host(mFirst, key, value);
        }

        void append(Map<String, String> extra) {
            append2Host(mLast, extra);
        }

        void append(String key, String value) {
            append2Host(mLast, key, value);
        }

        private void append2Host(Map<String, String> host, Map<String, String> extra) {
            if (extra == null || extra.isEmpty()) {
                return;
            }
            for (Map.Entry<String, String> entry : extra.entrySet()) {
                append2Host(host, entry.getKey(), entry.getValue());
            }
        }

        private void append2Host(Map<String, String> host, String key, String value) {
            if (TextUtils.isEmpty(key) || TextUtils.isEmpty(value)) {
                return;
            }
            int delta = 19 - key.length(); // 19 is length of "Device Manufacturer"
            if (delta > 0) {
                key = key + "                   ".substring(0, delta);
            }
            host.put(key, value);
        }

        public String getAppended() {
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, String> entry : mLast.entrySet()) {
                sb.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
            }
            return sb.toString();
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            String border = "************* " + mName + " Head ****************\n";
            sb.append(border);
            for (Map.Entry<String, String> entry : mFirst.entrySet()) {
                sb.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
            }
            sb.append("Device Manufacturer: ").append(Build.MANUFACTURER).append("\n");
            sb.append("Device Model       : ").append(Build.MODEL).append("\n");
            sb.append("Android Version    : ").append(Build.VERSION.RELEASE).append("\n");
            sb.append("Android SDK        : ").append(Build.VERSION.SDK_INT).append("\n");

            String versionName = "";
            long versionCode = 0L;
            try {
                PackageInfo pi = InitUtil.getAppContext()
                        .getPackageManager()
                        .getPackageInfo(InitUtil.getAppContext().getPackageName(), 0);
                if (pi != null) {
                    versionName = pi.versionName;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        versionCode = pi.getLongVersionCode();
                    } else {
                        versionCode = pi.versionCode;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            sb.append("App VersionName    : ").append(versionName).append("\n");
            sb.append("App VersionCode    : ").append(versionCode).append("\n");

            sb.append(getAppended());
            return sb.append(border).append("\n").toString();
        }
    }
}
