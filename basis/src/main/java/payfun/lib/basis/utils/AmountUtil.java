package payfun.lib.basis.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author : zhangqg
 * date   : 2021/5/21 18:15
 * desc   : <金钱转换工具类>
 */
public final class AmountUtil {

    /**
     * 金额为分的格式：是否是数字
     */
    public static final String CURRENCY_FEN_REGEX = "^[+]?(\\d+)$";
    private static final Pattern FEN_PATTERN = Pattern.compile(CURRENCY_FEN_REGEX);

    /**
     * 金额为元的格式：可以没有小数，小数最多不超过两位
     */
    public static final String CURRENCY_YUAN_REGEX = "^([1-9]\\d*(\\.\\d{1,2})?)|(0\\.([0][1-9]|[1-9]\\d?))$";
    private static final Pattern YUAN_PATTERN = Pattern.compile(CURRENCY_YUAN_REGEX);


    /**
     * 分转换为元
     * 如果想获取double类型的元单位的值，则对返回值进行Double.parseDouble(yuan);转换
     *
     * @param fen 以分为单位的数值字符串(不存在小数点及之后的数值,不合法的格式直接返回0.00)
     * @return 返回格式为 #0.00的金额，如：123.00
     */
    public static String fenToYuan(String fen) {
        if (!isFen(fen)) {
            return "0.00";
        }
        DecimalFormat df = new DecimalFormat("#0.00");
        return df.format(new BigDecimal(fen).divide(BigDecimal.valueOf(100)));
    }

    public static String fenToYuan(long fen) {
        return fenToYuan(String.valueOf(fen));
    }

    public static String fenToYuan(int fen) {
        return fenToYuan(String.valueOf(fen));
    }

    public static String fenToYuanWithDot(String fen) {
        if (!isFen(fen)) {
            return "0.00";
        }
        DecimalFormat df = new DecimalFormat("###,##0.00");
        return df.format(new BigDecimal(fen).divide(BigDecimal.valueOf(100)));
    }


    /**
     * 元转换为分
     *
     * @param yuan 以元为单位的数值字符串(如：123.05，且可以没有小数，小数最多不超过两位，超过两位则认为是非法格式，直接返回0)
     * @return 返回以分为单位的字符串，如：110表示1.1元
     */
    public static String yuanToFen(String yuan) {
        if (!isYuan(yuan)) {
            return "0";
        }
        return String.valueOf(new BigDecimal(yuan).multiply(BigDecimal.valueOf(100)).setScale(0, RoundingMode.DOWN));
    }

    public static String yuanToFen(double yuan) {
        return yuanToFen(String.valueOf(yuan));
    }

    public static int yuanToFenWithInt(String yuan) {
        if (!isYuan(yuan)) {
            return 0;
        }
        return new BigDecimal(yuan).multiply(BigDecimal.valueOf(100)).setScale(0, RoundingMode.DOWN).intValue();
    }

    public static int yuanToFenWithInt(double yuan) {
        return yuanToFenWithInt(String.valueOf(yuan));
    }

    public static long yuanToFenWithLong(String yuan) {
        if (!isYuan(yuan)) {
            return 0;
        }
        return new BigDecimal(yuan).multiply(BigDecimal.valueOf(100)).setScale(0, RoundingMode.DOWN).longValue();
    }

    public static long yuanToFenWithLong(double yuan) {
        return yuanToFenWithLong(String.valueOf(yuan));
    }


    /**
     * 是否是分为单位的值
     *
     * @param fen 字符串
     * @return 结果
     */
    public static boolean isFen(String fen) {
        if (!(fen == null || fen.length() == 0)) {
            return FEN_PATTERN.matcher(fen).matches();
        }
        return false;
    }

    /**
     * 是否是元为单位的值
     *
     * @param yuan 字符串
     * @return 结果
     */
    public static boolean isYuan(String yuan) {
        if (!(yuan == null || yuan.length() == 0)) {
            return YUAN_PATTERN.matcher(yuan).matches();
        }
        return false;
    }

    public static String getYuan(String str) {
        Matcher matcher = YUAN_PATTERN.matcher(str);
        String group = "";
        if (matcher.find()) {
            group = matcher.group(0);
        }
        return group;
    }


    /**
     * 是否是支付金额：即支付金额必须大于0
     *
     * @param obj 输入金额
     * @return 结果
     */
    public static boolean isPayMoney(Object obj) {
        if (obj != null) {
            try {
                return Double.parseDouble(obj.toString()) > 0;
            } catch (Exception e) {
            }
        }
        return false;
    }

}
