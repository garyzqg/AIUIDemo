package payfun.lib.basis.utils;

import static android.Manifest.permission.ACCESS_WIFI_STATE;
import static android.Manifest.permission.CHANGE_WIFI_STATE;
import static android.Manifest.permission.INTERNET;
import static android.Manifest.permission.READ_PHONE_STATE;
import static android.content.Context.SENSOR_SERVICE;
import static android.content.Context.WIFI_SERVICE;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.TrafficStats;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresPermission;
import androidx.core.app.ActivityCompat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author : 时光
 * date   : 2022/8/9 17:34
 * desc   : <p>设备参数获取工具类
 */
public class DeviceUtil {


    /**
     * Return whether device is rooted.
     *
     * @return {@code true}: yes<br>{@code false}: no
     */
    public static boolean isDeviceRooted() {
        String su = "su";
        String[] locations = {"/system/bin/", "/system/xbin/", "/sbin/", "/system/sd/xbin/",
                "/system/bin/failsafe/", "/data/local/xbin/", "/data/local/bin/", "/data/local/",
                "/system/sbin/", "/usr/bin/", "/vendor/bin/"};
        for (String location : locations) {
            if (new File(location + su).exists()) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取最近升级时间
     *
     * @return 时间戳
     */
    public static long getLastInstallTime() {
        try {
            PackageManager packageManager = InitUtil.getAppContext().getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(InitUtil.getPackageName(), 0);
            //应用装时间
            return packageInfo.lastUpdateTime;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * 获取首次安装时间
     *
     * @return 时间戳
     */
    public static long getFirstInstallTime() {
        try {
            PackageManager packageManager = InitUtil.getAppContext().getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(InitUtil.getPackageName(), 0);
            //应用装时间
            return packageInfo.firstInstallTime;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -2;
    }


    /**
     * 获取蓝牙是否开启
     *
     * @return 1:表示开启；2：表示未开启；
     */
    @RequiresPermission(Manifest.permission.BLUETOOTH)
    public static int getBlueState() {
        return BluetoothAdapter.getDefaultAdapter().isEnabled() ? 1 : 2;
    }


    //region 获取系统参数

    private final static String UNKNOWN = "unknown";

    /**
     * 获取手机型号/品牌
     *
     * @return 手机型号/品牌
     */
    public static String getSystemModel() {
        try {
            String model = Build.MODEL;
            if (!TextUtils.isEmpty(model)) {
                return model;
            }
        } catch (Throwable ignore) {/**/}
        return UNKNOWN;
    }

    /**
     * 获取主板
     *
     * @return 主板
     */
    public static String getSystemBoard() {
        try {
            String broad = Build.BOARD;
            if (!TextUtils.isEmpty(broad)) {
                return broad;
            }
        } catch (Throwable ignore) {/**/}
        return UNKNOWN;
    }

    /**
     * 获取设备品牌
     *
     * @return 品牌
     */
    public static String getBrand() {
        try {
            String brand = Build.BRAND;
            if (!TextUtils.isEmpty(brand)) {
                return brand;
            }
        } catch (Throwable ignore) {/**/}
        return UNKNOWN;
    }


    /**
     * 获取设备序列号.（如：WTK7N16923005607）, 个别设备无法获取
     *
     * @return the serial of device
     */
    @SuppressLint("HardwareIds")
    @RequiresPermission(READ_PHONE_STATE)
    public static String getSerial() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                Class<?> c = Class.forName("android.os.SystemProperties");
                Method get = c.getMethod("get", String.class);
                String serial = (String) get.invoke(c, "ro.serialno");
                if (!TextUtils.isEmpty(serial)) {
                    return serial;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                return Build.getSerial();
            } catch (SecurityException e) {
                e.printStackTrace();
                return "";
            }
        }
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? Build.getSerial() : Build.SERIAL;
    }

    /**
     * 获取当前手机ROM版本号
     *
     * @return ROM版本号
     */
    public static String getRomVersion() {
        return ShellUtil.execCmd("getprop ro.inspos.version").successMsg;
    }

    /**
     * 获取当前手机系统版本号
     *
     * @return 系统版本号
     */
    public static String getSystemVersion() {
        return Build.VERSION.RELEASE;
    }

    /**
     * 获取当前使用语音
     *
     * @return 语言
     */
    public static String getSystemLanguage() {
        return Locale.getDefault().getLanguage();
    }

    //endregion 获取系统参数


    //region 获取设备各种编号

    /**
     * 返回唯一设备编号.
     * <p>If the version of SDK is greater than 28, it will return an empty string.</p>
     * <p>Must hold {@code <uses-permission android:name="android.permission.READ_PHONE_STATE" />}</p>
     *
     * @return the unique device id
     */
    @SuppressLint("HardwareIds")
    @RequiresPermission(READ_PHONE_STATE)
    public static String getDeviceId() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return "";
        }
        TelephonyManager tm = getTelephonyManager();
        @SuppressLint("MissingPermission") String deviceId = tm.getDeviceId();
        if (!TextUtils.isEmpty(deviceId)) {
            return deviceId;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            @SuppressLint("MissingPermission") String imei = tm.getImei();
            if (!TextUtils.isEmpty(imei)) {
                return imei;
            }
            @SuppressLint("MissingPermission") String meid = tm.getMeid();
            return TextUtils.isEmpty(meid) ? "" : meid;
        }
        return "";
    }


    /**
     * 获取IMEI：唯一的设备ID： GSM手机的 IMEI 和 CDMA手机的 MEID；
     * International Mobile Equipment Identity，国际移动设备身份码的缩写。是由15位数字组成的“电子串号”，
     * 它与每台手机一一对应，每个IMEI在世界上都是唯一的。
     * <p>If the version of SDK is greater than 28, it will return an empty string.</p>
     * <p>Must hold {@code <uses-permission android:name="android.permission.READ_PHONE_STATE" />}</p>
     *
     * @return the IMEI
     */
    @RequiresPermission(READ_PHONE_STATE)
    public static String getIMEI() {
        return getImeiOrMeid(true);
    }


    /**
     * Return the MEID.
     * <p>If the version of SDK is greater than 28, it will return an empty string.</p>
     * <p>Must hold {@code <uses-permission android:name="android.permission.READ_PHONE_STATE" />}</p>
     *
     * @return the MEID
     */
    @RequiresPermission(READ_PHONE_STATE)
    public static String getMEID() {
        return getImeiOrMeid(false);
    }

    @SuppressLint({"HardwareIds", "MissingPermission"})
    @RequiresPermission(READ_PHONE_STATE)
    public static String getImeiOrMeid(boolean isImei) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return "";
        }
        TelephonyManager tm = getTelephonyManager();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (isImei) {
                return getMinOne(tm.getImei(0), tm.getImei(1));
            } else {
                return getMinOne(tm.getMeid(0), tm.getMeid(1));
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            String ids = getSystemPropertyByReflect(isImei ? "ril.gsm.imei" : "ril.cdma.meid");
            if (!TextUtils.isEmpty(ids)) {
                String[] idArr = ids.split(",");
                if (idArr.length == 2) {
                    return getMinOne(idArr[0], idArr[1]);
                } else {
                    return idArr[0];
                }
            }

            @SuppressLint("MissingPermission") String id0 = tm.getDeviceId();
            String id1 = "";
            try {
                Method method = tm.getClass().getMethod("getDeviceId", int.class);
                id1 = (String) method.invoke(tm,
                        isImei ? TelephonyManager.PHONE_TYPE_GSM
                                : TelephonyManager.PHONE_TYPE_CDMA);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
            if (isImei) {
                if (id0 != null && id0.length() < 15) {
                    id0 = "";
                }
                if (id1 != null && id1.length() < 15) {
                    id1 = "";
                }
            } else {
                if (id0 != null && id0.length() == 14) {
                    id0 = "";
                }
                if (id1 != null && id1.length() == 14) {
                    id1 = "";
                }
            }
            return getMinOne(id0, id1);
        } else {
            @SuppressLint("MissingPermission") String deviceId = tm.getDeviceId();
            if (isImei) {
                if (deviceId != null && deviceId.length() >= 15) {
                    return deviceId;
                }
            } else {
                if (deviceId != null && deviceId.length() == 14) {
                    return deviceId;
                }
            }
        }
        return "";
    }

    private static TelephonyManager getTelephonyManager() {
        return (TelephonyManager) InitUtil.getAppContext().getSystemService(Context.TELEPHONY_SERVICE);
    }

    private static String getMinOne(String s0, String s1) {
        boolean empty0 = TextUtils.isEmpty(s0);
        boolean empty1 = TextUtils.isEmpty(s1);
        if (empty0 && empty1) {
            return "";
        }
        if (!empty0 && !empty1) {
            if (s0.compareTo(s1) <= 0) {
                return s0;
            } else {
                return s1;
            }
        }
        if (!empty0) {
            return s0;
        }
        return s1;
    }

    private static String getSystemPropertyByReflect(String key) {
        try {
            @SuppressLint("PrivateApi")
            Class<?> clz = Class.forName("android.os.SystemProperties");
            Method getMethod = clz.getMethod("get", String.class, String.class);
            return (String) getMethod.invoke(clz, key, "");
        } catch (Exception e) {/**/}
        return "";
    }


    /**
     * 返回SIM卡运营商的名字
     * 服务商名称： 例如：中国移动、联通 SIM卡的状态必须是 SIM_STATE_READY(使用getSimState()判断).
     * * 中国移动：46000 46002 // 中国联通：46001 CHN-UNICOM // 中国电信：46003 //
     *
     * @return the sim operator name
     */
    public static String getSimOperatorName() {
        TelephonyManager tm = getTelephonyManager();
        return tm.getSimOperatorName();
    }


    /**
     * Return the IMSI.
     * 唯一的用户ID： 例如：IMSI(国际移动用户识别码) for a GSM phone.
     * <p>Must hold {@code <uses-permission android:name="android.permission.READ_PHONE_STATE" />}</p>
     *
     * @return the IMSI
     */
    @SuppressLint("HardwareIds")
    @RequiresPermission(READ_PHONE_STATE)
    public static String getSimIMSI() {
        try {
            @SuppressLint("MissingPermission") String subscriberId = getTelephonyManager().getSubscriberId();
            return subscriberId == null ? "" : subscriberId;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }


    /**
     * 返回 ICCID.
     * SIM卡的串号： 例如： ICCID是卡的标识，IMSI是用户的标识
     * <p>
     * ICCID：Integrate circuit card identity 集成电路卡识别码（固化在SIM卡中）
     * ICCID为IC卡的唯一识别号码，共有20位数字组成，其编码格式为：XXXXXX 0MFSS YYGXX XXXXX。分别介绍如下：
     * 前六位运营商代码：中国移动的为：898600；898602 ，中国联通的为：898601、898606
     * 、898609，中国电信898603，手机SIM卡及卡托上印有iccid号码 需要权限：READ_PHONE_STA
     * <p>Must hold {@code <uses-permission android:name="android.permission.READ_PHONE_STATE" />}</p>
     *
     * @return the ICCID
     */
    @SuppressLint("HardwareIds")
    @RequiresPermission(READ_PHONE_STATE)
    public static String getSimICCID() {
        try {
            @SuppressLint("MissingPermission") String simSerialNumber = getTelephonyManager().getSimSerialNumber();
            return simSerialNumber == null ? "" : simSerialNumber;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }


    /**
     * Return whether sim card state is ready.
     *
     * @return {@code true}: yes<br>{@code false}: no
     */
    public static boolean isSimCardReady() {
        TelephonyManager tm = getTelephonyManager();
        return tm.getSimState() == TelephonyManager.SIM_STATE_READY;
    }

    /**
     * Return the sim operator using mnc.
     *
     * @return the sim operator
     */
    public static String getSimOperatorByMnc() {
        TelephonyManager tm = getTelephonyManager();
        String operator = tm.getSimOperator();
        if (operator == null) {
            return "";
        }
        switch (operator) {
            case "46000":
            case "46002":
            case "46007":
            case "46020":
                return "中国移动";
            case "46001":
            case "46006":
            case "46009":
                return "中国联通";
            case "46003":
            case "46005":
            case "46011":
                return "中国电信";
            default:
                return operator;
        }
    }

    //endregion 获取设备各种编号


    //region 获取设备唯一编号

    private static final String KEY_UDID = "KEY_UDID";
    private volatile static String udid;

    /**
     * Return the unique device id.
     * <pre>{1}{UUID(macAddress)}</pre>
     * <pre>{2}{UUID(androidId )}</pre>
     * <pre>{9}{UUID(random    )}</pre>
     *
     * @return the unique device id
     */
    public static String getUniqueDeviceId() {
        return getUniqueDeviceId("", true);
    }

    /**
     * Return the unique device id.
     * <pre>android 10 deprecated {prefix}{1}{UUID(macAddress)}</pre>
     * <pre>{prefix}{2}{UUID(androidId )}</pre>
     * <pre>{prefix}{9}{UUID(random    )}</pre>
     *
     * @param prefix The prefix of the unique device id.
     * @return the unique device id
     */
    public static String getUniqueDeviceId(String prefix) {
        return getUniqueDeviceId(prefix, true);
    }

    /**
     * Return the unique device id.
     * <pre>{1}{UUID(macAddress)}</pre>
     * <pre>{2}{UUID(androidId )}</pre>
     * <pre>{9}{UUID(random    )}</pre>
     *
     * @param useCache True to use cache, false otherwise.
     * @return the unique device id
     */
    public static String getUniqueDeviceId(boolean useCache) {
        return getUniqueDeviceId("", useCache);
    }

    /**
     * Return the unique device id.
     * <pre>android 10 deprecated {prefix}{1}{UUID(macAddress)}</pre>
     * <pre>{prefix}{2}{UUID(androidId )}</pre>
     * <pre>{prefix}{9}{UUID(random    )}</pre>
     *
     * @param prefix   The prefix of the unique device id.
     * @param useCache True to use cache, false otherwise.
     * @return the unique device id
     */
    public static String getUniqueDeviceId(String prefix, boolean useCache) {
        if (!useCache) {
            return getUniqueDeviceIdReal(prefix);
        }
        if (udid == null) {
            synchronized (DeviceUtil.class) {
                if (udid == null) {
                    final String id = SpUtil.getInstance().getString(KEY_UDID, null);
                    if (id != null) {
                        udid = id;
                        return udid;
                    }
                    return getUniqueDeviceIdReal(prefix);
                }
            }
        }
        return udid;
    }

    private static String getUniqueDeviceIdReal(String prefix) {
        try {
            final String androidId = getAndroidID();
            if (!TextUtils.isEmpty(androidId)) {
                return saveUdid(prefix + 2, androidId);
            }

        } catch (Exception ignore) {/**/}
        return saveUdid(prefix + 9, "");
    }

    /**
     * Return the android id of device.
     *
     * @return the android id of device
     */
    @SuppressLint("HardwareIds")
    public static String getAndroidID() {
        String id = Settings.Secure.getString(
                InitUtil.getAppContext().getContentResolver(),
                Settings.Secure.ANDROID_ID
        );
        if ("9774d56d682e549c".equals(id)) {
            return "";
        }
        return id == null ? "" : id;
    }

    private static String saveUdid(String prefix, String id) {
        udid = getUdid(prefix, id);
        SpUtil.getInstance().put(KEY_UDID, udid);
        return udid;
    }

    private static String getUdid(String prefix, String id) {
        if (id.equals("")) {
            return prefix + UUID.randomUUID().toString().replace("-", "");
        }
        return prefix + UUID.nameUUIDFromBytes(id.getBytes()).toString().replace("-", "");
    }


    @RequiresPermission(allOf = {ACCESS_WIFI_STATE, INTERNET, CHANGE_WIFI_STATE})
    public static boolean isSameDevice(final String uniqueDeviceId) {
        // {prefix}{type}{32id}
        if (TextUtils.isEmpty(uniqueDeviceId) && uniqueDeviceId.length() < 33) {
            return false;
        }
        if (uniqueDeviceId.equals(udid)) {
            return true;
        }
        final String cachedId = SpUtil.getInstance().getString(KEY_UDID, null);
        if (uniqueDeviceId.equals(cachedId)) {
            return true;
        }
        int st = uniqueDeviceId.length() - 33;
        String type = uniqueDeviceId.substring(st, st + 1);
        if (type.startsWith("1")) {
            String macAddress = getMacAddress();
            if (macAddress.equals("")) {
                return false;
            }
            return uniqueDeviceId.substring(st + 1).equals(getUdid("", macAddress));
        } else if (type.startsWith("2")) {
            final String androidId = getAndroidID();
            if (TextUtils.isEmpty(androidId)) {
                return false;
            }
            return uniqueDeviceId.substring(st + 1).equals(getUdid("", androidId));
        }
        return false;
    }

    //endregion 获取设备唯一编号


    //region 获取各种内存CPU大小

    /**
     * 获取安卓当前可用运行内存大小
     *
     * @return 可用内存大小
     */
    public static double getAvailMemory() {
        android.app.ActivityManager am = (android.app.ActivityManager) InitUtil.getAppContext().getSystemService(Context.ACTIVITY_SERVICE);
        android.app.ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(mi);
        // mi.availMem; 当前系统的可用内存
        return format(mi.availMem / 1024.0 / 1024.0 / 1024.0);
    }


    /**
     * 获取android总运行内存大小
     */
    public static double getTotalMemory() {
        // 系统内存信息文件
        String str1 = "/proc/meminfo";
        String str2;
        String[] arrayOfString;
        long initial_memory = 0;
        try {
            FileReader localFileReader = new FileReader(str1);
            BufferedReader localBufferedReader = new BufferedReader(localFileReader, 8192);
            // 读取meminfo第一行，系统总内存大小
            str2 = localBufferedReader.readLine();
            arrayOfString = str2.split("\\s+");

            // 获得系统总内存，单位是KB
            int i = Integer.valueOf(arrayOfString[1]).intValue();
            //int值乘以1024转换为long类型
            initial_memory = new Long((long) i * 1024);
            localBufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return format(initial_memory / 1024.0 / 1024.0 / 1024.0);
    }


    /**
     * 获取手机内部可用空间大小
     *
     * @return 大小，字节为单位
     */
    public static double getAvailableInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        //获取可用区块数量
        long availableBlocks = stat.getAvailableBlocks();
        return format((availableBlocks * blockSize) / 1024.0 / 1024.0 / 1024.0);
    }


    /**
     * 获取手机内部空间总大小
     *
     * @return 大小，字节为单位
     */
    public static double getTotalInternalMemorySize() {
        //获取内部存储根目录
        File path = Environment.getDataDirectory();
        //系统的空间描述类
        StatFs stat = new StatFs(path.getPath());
        //每个区块占字节数
        long blockSize = stat.getBlockSize();
        //区块总数
        long totalBlocks = stat.getBlockCount();
        return format((totalBlocks * blockSize) / 1024.0 / 1024.0 / 1024.0);
    }

    /**
     * 获取CPU使用率
     *
     * @return 使用率
     */
    public static double getCpuRate() {
        // 系统CPU信息文件
        String path = "/proc/stat";
        long totalJiffies[] = new long[2];
        long totalIdle[] = new long[2];
        //设置这个参数，这要是防止两次读取文件获知的CPU数量不同，导致不能计算。这里统一以第一次的CPU数量为基准
        int firstCPUNum = 0;
        FileReader fileReader = null;
        BufferedReader bufferedReader = null;
        Pattern pattern = Pattern.compile(" [0-9]+");
        for (int i = 0; i < 2; i++) {
            totalJiffies[i] = 0;
            totalIdle[i] = 0;
            try {
                fileReader = new FileReader(path);
                bufferedReader = new BufferedReader(fileReader, 8192);
                int currentCPUNum = 0;
                String str;
                while ((str = bufferedReader.readLine()) != null && (i == 0 || currentCPUNum < firstCPUNum)) {
                    if (str.toLowerCase().startsWith("cpu")) {
                        currentCPUNum++;
                        int index = 0;
                        Matcher matcher = pattern.matcher(str);
                        while (matcher.find()) {
                            try {
                                long tempJiffies = Long.parseLong(matcher.group(0).trim());
                                totalJiffies[i] += tempJiffies;
                                if (index == 3) {
                                    //空闲时间为该行第4条栏目
                                    totalIdle[i] += tempJiffies;
                                }
                                index++;
                            } catch (NumberFormatException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    if (i == 0) {
                        firstCPUNum = currentCPUNum;
                        try {
                            //暂停50毫秒，等待系统更新信息。
                            Thread.sleep(50);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        double rate = -1;
        if (totalJiffies[0] > 0 && totalJiffies[1] > 0 && totalJiffies[0] != totalJiffies[1]) {
            rate = 1.0 * ((totalJiffies[1] - totalIdle[1]) - (totalJiffies[0] - totalIdle[0])) / (totalJiffies[1] - totalJiffies[0]);
        }

        return format(rate);
    }

    /**
     * 保留小数点后三位数字
     *
     * @param num 数字
     * @return
     */
    private static double format(double num) {
        BigDecimal bg = new BigDecimal(num);
        double f1 = bg.setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue();
        return f1;
    }

    //endregion 获取各种内存CPU大小


    //region 获取mac地址

    /**
     * 获取 MAC address.
     * <p>Must hold {@code <uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />},
     * {@code <uses-permission android:name="android.permission.INTERNET" />},
     * {@code <uses-permission android:name="android.permission.CHANGE_WIFI_STATE" />}</p>
     *
     * @return the MAC address
     */
    @RequiresPermission(allOf = {ACCESS_WIFI_STATE, CHANGE_WIFI_STATE})
    public static String getMacAddress() {
        String macAddress = getMacAddress((String[]) null);
        if (!TextUtils.isEmpty(macAddress) || getWifiEnabled()) {
            return macAddress;
        }
        setWifiEnabled(true);
        setWifiEnabled(false);
        return getMacAddress((String[]) null);
    }


    /**
     * 获取 MAC address.
     * <p>Must hold {@code <uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />},
     * {@code <uses-permission android:name="android.permission.INTERNET" />}</p>
     *
     * @return the MAC address
     */
    @RequiresPermission(allOf = {ACCESS_WIFI_STATE})
    public static String getMacAddress(final String... excepts) {
        String macAddress = getMacAddressByNetworkInterface();
        if (isAddressNotInExcepts(macAddress, excepts)) {
            return macAddress;
        }
        macAddress = getMacAddressByInetAddress();
        if (isAddressNotInExcepts(macAddress, excepts)) {
            return macAddress;
        }
        macAddress = getMacAddressByWifiInfo();
        if (isAddressNotInExcepts(macAddress, excepts)) {
            return macAddress;
        }
        macAddress = getMacAddressByFile();
        if (isAddressNotInExcepts(macAddress, excepts)) {
            return macAddress;
        }
        return "";
    }

    private static boolean getWifiEnabled() {
        @SuppressLint("WifiManagerLeak")
        WifiManager manager = (WifiManager) InitUtil.getAppContext().getSystemService(WIFI_SERVICE);
        if (manager == null) {
            return false;
        }
        return manager.isWifiEnabled();
    }

    /**
     * Enable or disable wifi.
     * <p>Must hold {@code <uses-permission android:name="android.permission.CHANGE_WIFI_STATE" />}</p>
     *
     * @param enabled True to enabled, false otherwise.
     */
    @RequiresPermission(CHANGE_WIFI_STATE)
    private static void setWifiEnabled(final boolean enabled) {
        @SuppressLint("WifiManagerLeak")
        WifiManager manager = (WifiManager) InitUtil.getAppContext().getSystemService(WIFI_SERVICE);
        if (manager == null) {
            return;
        }
        if (enabled == manager.isWifiEnabled()) {
            return;
        }
        manager.setWifiEnabled(enabled);
    }

    private static String getMacAddressByNetworkInterface() {
        try {
            Enumeration<NetworkInterface> nis = NetworkInterface.getNetworkInterfaces();
            while (nis.hasMoreElements()) {
                NetworkInterface ni = nis.nextElement();
                if (ni == null || !ni.getName().equalsIgnoreCase("wlan0")) {
                    continue;
                }
                byte[] macBytes = ni.getHardwareAddress();
                if (macBytes != null && macBytes.length > 0) {
                    StringBuilder sb = new StringBuilder();
                    for (byte b : macBytes) {
                        sb.append(String.format("%02x:", b));
                    }
                    return sb.substring(0, sb.length() - 1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "02:00:00:00:00:00";
    }

    private static String getMacAddressByInetAddress() {
        try {
            InetAddress inetAddress = getInetAddress();
            if (inetAddress != null) {
                NetworkInterface ni = NetworkInterface.getByInetAddress(inetAddress);
                if (ni != null) {
                    byte[] macBytes = ni.getHardwareAddress();
                    if (macBytes != null && macBytes.length > 0) {
                        StringBuilder sb = new StringBuilder();
                        for (byte b : macBytes) {
                            sb.append(String.format("%02x:", b));
                        }
                        return sb.substring(0, sb.length() - 1);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "02:00:00:00:00:00";
    }

    private static InetAddress getInetAddress() {
        try {
            Enumeration<NetworkInterface> nis = NetworkInterface.getNetworkInterfaces();
            while (nis.hasMoreElements()) {
                NetworkInterface ni = nis.nextElement();
                // To prevent phone of xiaomi return "10.0.2.15"
                if (!ni.isUp()) {
                    continue;
                }
                Enumeration<InetAddress> addresses = ni.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress inetAddress = addresses.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        String hostAddress = inetAddress.getHostAddress();
                        if (hostAddress.indexOf(':') < 0) {
                            return inetAddress;
                        }
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String getMacAddressByFile() {
        ShellUtil.CmdResult result = ShellUtil.execCmd("getprop wifi.interface", false);
        if (result.result == 0) {
            String name = result.successMsg;
            if (name != null) {
                result = ShellUtil.execCmd("cat /sys/class/net/" + name + "/address", false);
                if (result.result == 0) {
                    String address = result.successMsg;
                    if (address != null && address.length() > 0) {
                        return address;
                    }
                }
            }
        }
        return "02:00:00:00:00:00";
    }

    private static boolean isAddressNotInExcepts(final String address, final String... excepts) {
        if (TextUtils.isEmpty(address)) {
            return false;
        }
        if ("02:00:00:00:00:00".equals(address) || "  :  :  :  :  :  ".equals(address)
                || "00:00:00:00:00:00".equals(address)) {
            return false;
        }
        if (!checkMacAddressFormat(address)) {
            return false;
        }
        if (excepts == null || excepts.length == 0) {
            return true;
        }
        for (String filter : excepts) {
            if (filter != null && filter.equals(address)) {
                return false;
            }
        }
        return true;
    }

    private static boolean checkMacAddressFormat(String address) {
        if (null != address) {
            address = address.replace(":", "").trim();
            String format = "(((?!0{10})([0-9A-Fa-f]{12})))";
            return address.matches(format);
        }
        return false;
    }

    @RequiresPermission(ACCESS_WIFI_STATE)
    private static String getMacAddressByWifiInfo() {
        try {
            final WifiManager wifi = (WifiManager) InitUtil.getAppContext()
                    .getApplicationContext().getSystemService(WIFI_SERVICE);
            if (wifi != null) {
                final WifiInfo info = wifi.getConnectionInfo();
                if (info != null) {
                    @SuppressLint("HardwareIds")
                    String macAddress = info.getMacAddress();
                    if (!TextUtils.isEmpty(macAddress)) {
                        return macAddress;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "02:00:00:00:00:00";
    }

    //endregion 获取mac地址


    //region 获取定位

    /**
     * 获取最新定位
     *
     * @return 定位
     */
    @Nullable
    public static Location getLastLocation() {
        LocationManager locationManager = (LocationManager) InitUtil.getAppContext().getSystemService(Context.LOCATION_SERVICE);
        List<String> providers = locationManager.getProviders(true);
        Location bestLocation = null;
        for (String provider : providers) {
            if (ActivityCompat.checkSelfPermission(InitUtil.getAppContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(InitUtil.getAppContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                return null;
            }
            Location l = locationManager.getLastKnownLocation(provider);
            if (l == null) {
                continue;
            }
            if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                // Found best last known location: %s", l);
                bestLocation = l;
            }
        }
        return bestLocation;
    }

    /**
     * 注册位置监听器
     *
     * @param locationListener
     */
    @SuppressLint("MissingPermission")
    public static void registerLocationListener(final LocationListener locationListener) {
        LocationManager locationManager = (LocationManager) InitUtil.getAppContext().getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);// 高精度
        criteria.setAltitudeRequired(false);// 不要求海拔
        criteria.setBearingRequired(false);// 不要求方位
        criteria.setCostAllowed(true);// 允许有花费
        criteria.setPowerRequirement(Criteria.POWER_LOW);// 低功耗
        //从可用的位置提供器中，匹配以上标准的最佳提供器
        String provider = locationManager.getBestProvider(criteria, true);

        // 监听位置变化，2秒一次，距离10米以上
        locationManager.requestLocationUpdates(provider, 200000, 10, locationListener);
    }


    //endregion 获取定位

    /**
     * 获取流量
     *
     * @return 总上传;总下载;流量上传;流量下载;应用上传;应用下载;且只能表示从设备启动到监听的时间，如何设备重启，基本全变为0；
     */
    public static String getTraffic() {
        int uid = -1;
        try {
            PackageManager pm = InitUtil.getAppContext().getPackageManager();
            ApplicationInfo ai = pm.getApplicationInfo(InitUtil.getPackageName(), PackageManager.GET_META_DATA);
            uid = ai.uid;
        } catch (Exception e) {
            e.printStackTrace();
        }
        long txBytes = -1;
        long rxBytes = -1;
        if (uid > 0) {
            //根据UID 获取流量数据。每个应用有个 UID ，可以通过这个 UID 知道该应用一共产生了多少流量。
            // Rx 接收，Tx 发送，bytes 字节数。
            txBytes = TrafficStats.getUidTxBytes(uid);
            rxBytes = TrafficStats.getUidRxBytes(uid);
            //方法返回值 -1 代表的是应用程序没有产生流量 或者操作系统不支持流量统计
        }

        //获取通过移动数据发送和接收的流量。
        // Rx 是 接收，Tx 是发送，bytes 是字节数。统计从设备启动开始。
        long mobileTxBytes = TrafficStats.getMobileTxBytes();
        long mobileRxBytes = TrafficStats.getMobileRxBytes();

        //获取通过所有网络接口传输的流量。统计数据由网络层统计，所以包括 TCP 和 UDP 包。
        // Rx 接收，Tx 发送，bytes 字节数，packets 数据包数量。也是从设备启动开始。
        long totalTxBytes = TrafficStats.getTotalTxBytes();
        long totalRxBytes = TrafficStats.getTotalRxBytes();

        String totalTx = FormatUtil.formatByte2FitSize(totalTxBytes);
        String totalRx = FormatUtil.formatByte2FitSize(totalRxBytes);
        String mobileTx = FormatUtil.formatByte2FitSize(mobileTxBytes);
        String mobileRx = FormatUtil.formatByte2FitSize(mobileRxBytes);
        String tx = FormatUtil.formatByte2FitSize(txBytes);
        String rx = FormatUtil.formatByte2FitSize(rxBytes);

        return totalTx + ";" + totalRx + ";"
                + mobileTx + ";" + mobileRx + ";"
                + tx + ";" + rx;
    }


    /**
     * 注册传感器监听器
     *
     * @param sensorEventListener
     */
    public static void registerSensorListener(final SensorEventListener sensorEventListener) {
        SensorManager sm = (SensorManager) InitUtil.getAppContext().getSystemService(SENSOR_SERVICE);
        sm.registerListener(sensorEventListener, sm.getDefaultSensor(Sensor.TYPE_ALL), SensorManager.SENSOR_DELAY_NORMAL);
    }
}
