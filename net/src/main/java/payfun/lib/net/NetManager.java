package payfun.lib.net;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

import payfun.lib.basis.utils.InitUtil;
import payfun.lib.net.api.DefaultApi;
import payfun.lib.net.rx.RxClient;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * @author : 时光
 * e-mail : qurongzhen@pay.media
 * date   : 2021/5/18 16:37
 * desc   : <网络统一管理器：可参考本类进行仿写>
 */
public final class NetManager {
    private volatile static NetManager sInstance;
    private static final Map<Class, Object> sApiMap = new HashMap<>();
    private static DefaultApi sDefault;

    private NetManager() {
    }

    public static NetManager getInstance() {
        if (sInstance == null) {
            synchronized (NetManager.class) {
                if (sInstance == null) {
                    sInstance = new NetManager();
                }
            }
        }
        return sInstance;
    }


    /**
     * 使用自定义接口，并进行配置
     * 调用前初始化一次即可
     *
     * @param zClass 自定义接口类
     * @param config 接口配置
     * @param <Z>    自定义接口泛型
     */
    public <Z> void initApi(@NonNull Class<Z> zClass, @NonNull NetManager.Config config) {
        if (sApiMap.containsKey(zClass)) {
            Object obj = sApiMap.get(zClass);
            if (obj == null) {
                sApiMap.put(zClass, config.config().build().getApi(zClass));
            }
        } else {
            sApiMap.put(zClass, config.config().build().getApi(zClass));
        }
    }

    public synchronized <Z> Z getApi(Class<Z> zClass) {
        if (zClass != null && sApiMap.containsKey(zClass)) {
            return (Z) sApiMap.get(zClass);
        } else {
            throw new NullPointerException((zClass == null ? "" : zClass.getSimpleName()) + "接口未初始化！");
        }
    }


    /**
     * 使用默认接口不用初始化，但是URL需要全路径
     *
     * @return 默认API
     */
    public synchronized DefaultApi getDefault() {
        if (sDefault == null) {
            RxClient build = Config.DEFAULT.config().build();
            sDefault = build.getApi();
        }
        return sDefault;
    }


    public interface Config {
        /**
         * 参数配置
         *
         * @return 构建类
         */
        @NonNull
        RxClient.Builder config();

        /**
         * 配置写法参考
         * 如果整个接口有统一的加解密，token等
         * 建议使用自定义拦截器通过addInterceptor()方法或者addNetInterceptor()来添加
         * 如果不进行初始化，使用默认时参数url需要传入全路径
         */
        NetManager.Config DEFAULT = () -> new RxClient.Builder()
                //.baseUrl("http://127.0.0.1/")
                .connectTimeout(10)
                .readTimeout(15)
                .writeTimeout(15)
                .addConvertFactory(GsonConverterFactory.create())
                .addAdapterFactory(RxJava3CallAdapterFactory.create())
                .isUseLog(InitUtil.isDebug());
    }
}
