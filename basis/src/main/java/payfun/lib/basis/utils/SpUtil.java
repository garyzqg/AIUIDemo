package payfun.lib.basis.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.collection.SimpleArrayMap;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * @author : zhangqg
 * date   : 2021/5/6 13:55
 * desc   : <本地持久化SP>
 */
public final class SpUtil {

    private static SimpleArrayMap<String, SpUtil> SP_UTILS_MAP = new SimpleArrayMap<>();
    private SharedPreferences sShared;

    /**
     * 获取 SP 实例
     *
     * @return {@link SpUtil}
     */
    public static SpUtil getInstance() {
        return getInstance("");
    }

    /**
     * 获取 SP 实例
     *
     * @param spName sp 名
     * @return {@link SpUtil}
     */
    public static synchronized SpUtil getInstance(String spName) {
        if (TextUtils.isEmpty(spName)) {
            spName = "spSaver";
        }
        SpUtil spUtils = SP_UTILS_MAP.get(spName);
        if (spUtils == null) {
            spUtils = new SpUtil(spName);
            SP_UTILS_MAP.put(spName, spUtils);
        }
        return spUtils;
    }


    /**
     * @param spName key值
     */
    private SpUtil(final String spName) {
        sShared = InitUtil.getAppContext().getSharedPreferences(spName, Context.MODE_PRIVATE);
    }


    /**
     * SP 中写入 String
     *
     * @param key   键
     * @param value 值
     */
    public void put(@NonNull final String key, @NonNull final String value) {
        put(key, value, false);
    }

    /**
     * SP 中写入 String
     *
     * @param key      键
     * @param value    值
     * @param isCommit {@code true}: {@link SharedPreferences.Editor#commit()}<br>
     *                 {@code false}: {@link SharedPreferences.Editor#apply()}
     */
    public void put(@NonNull final String key,
                    @NonNull final String value,
                    final boolean isCommit) {
        if (isCommit) {
            sShared.edit().putString(key, value).commit();
        } else {
            sShared.edit().putString(key, value).apply();
        }
    }

    /**
     * SP 中读取 String
     *
     * @param key 键
     * @return 存在返回对应值，不存在返回默认值{@code ""}
     */
    public String getString(@NonNull final String key) {
        return getString(key, "");
    }

    /**
     * SP 中读取 String
     *
     * @param key          键
     * @param defaultValue 默认值
     * @return 存在返回对应值，不存在返回默认值{@code defaultValue}
     */
    public String getString(@NonNull final String key, @NonNull final String defaultValue) {
        return sShared.getString(key, defaultValue);
    }

    /**
     * SP 中写入 int
     *
     * @param key   键
     * @param value 值
     */
    public void put(@NonNull final String key, final int value) {
        put(key, value, false);
    }

    /**
     * SP 中写入 int
     *
     * @param key      键
     * @param value    值
     * @param isCommit {@code true}: {@link SharedPreferences.Editor#commit()}<br>
     *                 {@code false}: {@link SharedPreferences.Editor#apply()}
     */
    public void put(@NonNull final String key, final int value, final boolean isCommit) {
        if (isCommit) {
            sShared.edit().putInt(key, value).commit();
        } else {
            sShared.edit().putInt(key, value).apply();
        }
    }

    /**
     * SP 中读取 int
     *
     * @param key 键
     * @return 存在返回对应值，不存在返回默认值-1
     */
    public int getInt(@NonNull final String key) {
        return getInt(key, -1);
    }

    /**
     * SP 中读取 int
     *
     * @param key          键
     * @param defaultValue 默认值
     * @return 存在返回对应值，不存在返回默认值{@code defaultValue}
     */
    public int getInt(@NonNull final String key, final int defaultValue) {
        return sShared.getInt(key, defaultValue);
    }

    /**
     * SP 中写入 long
     *
     * @param key   键
     * @param value 值
     */
    public void put(@NonNull final String key, final long value) {
        put(key, value, false);
    }

    /**
     * SP 中写入 long
     *
     * @param key      键
     * @param value    值
     * @param isCommit {@code true}: {@link SharedPreferences.Editor#commit()}<br>
     *                 {@code false}: {@link SharedPreferences.Editor#apply()}
     */
    public void put(@NonNull final String key, final long value, final boolean isCommit) {
        if (isCommit) {
            sShared.edit().putLong(key, value).commit();
        } else {
            sShared.edit().putLong(key, value).apply();
        }
    }

    /**
     * SP 中读取 long
     *
     * @param key 键
     * @return 存在返回对应值，不存在返回默认值-1
     */
    public long getLong(@NonNull final String key) {
        return getLong(key, -1L);
    }

    /**
     * SP 中读取 long
     *
     * @param key          键
     * @param defaultValue 默认值
     * @return 存在返回对应值，不存在返回默认值{@code defaultValue}
     */
    public long getLong(@NonNull final String key, final long defaultValue) {
        return sShared.getLong(key, defaultValue);
    }

    /**
     * SP 中写入 float
     *
     * @param key   键
     * @param value 值
     */
    public void put(@NonNull final String key, final float value) {
        put(key, value, false);
    }

    /**
     * SP 中写入 float
     *
     * @param key      键
     * @param value    值
     * @param isCommit {@code true}: {@link SharedPreferences.Editor#commit()}<br>
     *                 {@code false}: {@link SharedPreferences.Editor#apply()}
     */
    public void put(@NonNull final String key, final float value, final boolean isCommit) {
        if (isCommit) {
            sShared.edit().putFloat(key, value).commit();
        } else {
            sShared.edit().putFloat(key, value).apply();
        }
    }

    /**
     * SP 中读取 float
     *
     * @param key 键
     * @return 存在返回对应值，不存在返回默认值-1
     */
    public float getFloat(@NonNull final String key) {
        return getFloat(key, -1f);
    }

    /**
     * SP 中读取 float
     *
     * @param key          键
     * @param defaultValue 默认值
     * @return 存在返回对应值，不存在返回默认值{@code defaultValue}
     */
    public float getFloat(@NonNull final String key, final float defaultValue) {
        return sShared.getFloat(key, defaultValue);
    }

    /**
     * SP 中写入 boolean
     *
     * @param key   键
     * @param value 值
     */
    public void put(@NonNull final String key, final boolean value) {
        put(key, value, false);
    }

    /**
     * SP 中写入 boolean
     *
     * @param key      键
     * @param value    值
     * @param isCommit {@code true}: {@link SharedPreferences.Editor#commit()}<br>
     *                 {@code false}: {@link SharedPreferences.Editor#apply()}
     */
    public void put(@NonNull final String key, final boolean value, final boolean isCommit) {
        if (isCommit) {
            sShared.edit().putBoolean(key, value).commit();
        } else {
            sShared.edit().putBoolean(key, value).apply();
        }
    }

    /**
     * SP 中读取 boolean
     *
     * @param key 键
     * @return 存在返回对应值，不存在返回默认值{@code false}
     */
    public boolean getBoolean(@NonNull final String key) {
        return getBoolean(key, false);
    }

    /**
     * SP 中读取 boolean
     *
     * @param key          键
     * @param defaultValue 默认值
     * @return 存在返回对应值，不存在返回默认值{@code defaultValue}
     */
    public boolean getBoolean(@NonNull final String key, final boolean defaultValue) {
        return sShared.getBoolean(key, defaultValue);
    }

    /**
     * SP 中写入 String 集合
     *
     * @param key    键
     * @param values 值
     */
    public void put(@NonNull final String key, @NonNull final Set<String> values) {
        put(key, values, false);
    }

    /**
     * SP 中写入 String 集合
     *
     * @param key      键
     * @param values   值
     * @param isCommit {@code true}: {@link SharedPreferences.Editor#commit()}<br>
     *                 {@code false}: {@link SharedPreferences.Editor#apply()}
     */
    public void put(@NonNull final String key,
                    @NonNull final Set<String> values,
                    final boolean isCommit) {
        if (isCommit) {
            sShared.edit().putStringSet(key, values).commit();
        } else {
            sShared.edit().putStringSet(key, values).apply();
        }
    }

    /**
     * SP 中读取 StringSet
     *
     * @param key 键
     * @return 存在返回对应值，不存在返回默认值{@code Collections.<String>emptySet()}
     */
    public Set<String> getStringSet(@NonNull final String key) {
        return getStringSet(key, Collections.<String>emptySet());
    }

    /**
     * SP 中读取 StringSet
     *
     * @param key          键
     * @param defaultValue 默认值
     * @return 存在返回对应值，不存在返回默认值{@code defaultValue}
     */
    public Set<String> getStringSet(@NonNull final String key,
                                    @NonNull final Set<String> defaultValue) {
        return sShared.getStringSet(key, defaultValue);
    }

    /**
     * SP 中获取所有键值对
     *
     * @return Map 对象
     */
    public Map<String, ?> getAll() {
        return sShared.getAll();
    }

    /**
     * SP 中是否存在该 key
     *
     * @param key 键
     * @return {@code true}: 存在<br>{@code false}: 不存在
     */
    public boolean contains(@NonNull final String key) {
        return sShared.contains(key);
    }

    /**
     * SP 中移除该 key
     *
     * @param key 键
     */
    public void remove(@NonNull final String key) {
        remove(key, false);
    }

    /**
     * SP 中移除该 key
     *
     * @param key      键
     * @param isCommit {@code true}: {@link SharedPreferences.Editor#commit()}<br>
     *                 {@code false}: {@link SharedPreferences.Editor#apply()}
     */
    public void remove(@NonNull final String key, final boolean isCommit) {
        if (isCommit) {
            sShared.edit().remove(key).commit();
        } else {
            sShared.edit().remove(key).apply();
        }
    }

    /**
     * SP 中清除所有数据
     */
    public void clear() {
        clear(false);
    }

    /**
     * SP 中清除所有数据
     *
     * @param isCommit {@code true}: {@link SharedPreferences.Editor#commit()}<br>
     *                 {@code false}: {@link SharedPreferences.Editor#apply()}
     */
    public void clear(final boolean isCommit) {
        if (isCommit) {
            sShared.edit().clear().commit();
        } else {
            sShared.edit().clear().apply();
        }
    }
}
