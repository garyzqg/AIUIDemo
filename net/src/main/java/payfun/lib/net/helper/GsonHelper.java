package payfun.lib.net.helper;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.lang.reflect.Modifier;

/**
 * @author : zhangqg
 * date   : 2022/6/8 11:31
 * desc   : <Gson帮助类>
 */
public class GsonHelper {

    /**
     * 设置gson 时间解析格式可以避免不同的Local影响时间显示
     */
    public static final Gson GSON = new GsonBuilder()
            .excludeFieldsWithModifiers(Modifier.FINAL, Modifier.TRANSIENT, Modifier.STATIC)
            .setDateFormat("yyyy-MM-dd 'T' HH:mm:ss.SSS 'Z'")
            .serializeNulls()
            .create();
}
