package payfun.lib.basis.utils;

/**
 * @author : zhangqg
 * date   : 2022/6/28 17:01
 * desc   : <p>格式化工具类
 */
public final class FormatUtil {

    /**
     * 脱敏手机号
     *
     * @param phone 手机号
     * @return 手机号：130****1234
     */
    public static String desensitizePhone(String phone) {
        if (phone == null) {
            return "";
        }
        if (phone.length() != 11) {
            return phone;
        }
        return phone.replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2");
    }


    public static String formatNonContent(String content) {
        return content == null ? "" : content;
    }


    public static String[] formatThrowable(Throwable e) {
        String[] s = new String[2];
        s[0] = e.toString();
        s[1] = "Caused by: " + getThrowable(e.getCause(), 1);
        return s;
    }


    private static String getThrowable(Throwable e, int count) {
        if (e != null && count < 5) {
            String s = e.toString();
            String throwable = getThrowable(e.getCause(), count++);
            return s + (throwable == null ? "" : ("\n" + throwable));
        }
        return null;
    }

    public static final int BYTE = 1;
    public static final int KB = 1024;
    public static final int MB = 1048576;
    public static final int GB = 1073741824;


    public static String formatByte2FitSize(final long byteSize) {
        return formatByte2FitSize(byteSize, 3);
    }

    public static String formatByte2FitSize(final long byteSize, int precision) {
        if (byteSize < KB) {
            return String.format("%." + precision + "fB", (double) byteSize);
        } else if (byteSize < MB) {
            return String.format("%." + precision + "fKB", (double) byteSize / KB);
        } else if (byteSize < GB) {
            return String.format("%." + precision + "fMB", (double) byteSize / MB);
        } else {
            return String.format("%." + precision + "fGB", (double) byteSize / GB);
        }
    }
}
