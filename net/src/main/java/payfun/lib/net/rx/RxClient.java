package payfun.lib.net.rx;

import android.text.TextUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import androidx.annotation.RawRes;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import payfun.lib.basis.utils.LogUtil;
import payfun.lib.net.api.DefaultApi;
import payfun.lib.net.helper.GsonHelper;
import payfun.lib.net.helper.HttpsHelper;
import payfun.lib.net.interceptor.HttpLogInterceptor2;
import retrofit2.CallAdapter;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * @author : zhangqg
 * date   : 2021/5/18 14:37
 * desc   : <对Retrofit，OkHttpClient，HttpLog等的配置组装类>
 */
public class RxClient {
    private final HttpLogInterceptor2 mLogging = new HttpLogInterceptor2(LogUtil::iTag);

    private Builder mBuilder;
    private Retrofit mRetrofit;

    private RxClient() {
    }

    private RxClient(Builder builder) {
        this.mBuilder = builder;
        mRetrofit = getRetrofit(mBuilder);
    }

    private RxClient(Retrofit retrofit) {
        mRetrofit = retrofit;
    }

    public DefaultApi getApi() {
        return getApi(DefaultApi.class);
    }

    public <T> T getApi(Class<T> sClass) {
        return getRetrofit().create(sClass);
    }

    public Retrofit getRetrofit() {
        return mRetrofit;
    }

    private Retrofit getRetrofit(Builder config) {
        Retrofit.Builder retrofitBuilder = new Retrofit.Builder();
        retrofitBuilder.baseUrl(config.baseUrl);
        if (config.converterFactories.isEmpty()) {
            retrofitBuilder.addConverterFactory(GsonConverterFactory.create(GsonHelper.GSON));
        } else {
            for (Converter.Factory converterFactory : config.converterFactories) {
                retrofitBuilder.addConverterFactory(converterFactory);
            }
        }
        if (config.adapterFactories.isEmpty()) {
            retrofitBuilder.addCallAdapterFactory(RxJava3CallAdapterFactory.create());
        } else {
            for (CallAdapter.Factory adapterFactory : config.adapterFactories) {
                retrofitBuilder.addCallAdapterFactory(adapterFactory);
            }
        }
        retrofitBuilder.client(getOkHttpClient(config));
        return retrofitBuilder.build();
    }

    private OkHttpClient getOkHttpClient(Builder config) {
        OkHttpClient.Builder okBuilder = new OkHttpClient.Builder();
        if (config.connectTimeout >= 0) {
            okBuilder.connectTimeout(config.connectTimeout, TimeUnit.SECONDS);
        }
        if (config.readTimeout >= 0) {
            okBuilder.readTimeout(config.readTimeout, TimeUnit.SECONDS);
        }
        if (config.writeTimeout >= 0) {
            okBuilder.writeTimeout(config.writeTimeout, TimeUnit.SECONDS);
        }

        if (config.isUseLog) {
            //如果不是在正式包，添加拦截 开启Log
            mLogging.setLevel(config.logLevel);
            mLogging.setRequestTag(config.logRequestTag);
            mLogging.setResponseTag(config.logResponseTag);
            okBuilder.addInterceptor(mLogging);
        }

        if (!config.interceptors.isEmpty()) {
            for (Interceptor interceptor : config.interceptors) {
                okBuilder.addInterceptor(interceptor);
            }
        }
        if (!config.netInterceptors.isEmpty()) {
            for (Interceptor interceptor : config.netInterceptors) {
                okBuilder.addNetworkInterceptor(interceptor);
            }
        }
        if (config.isUseHttps) {
            if (config.certificates == null || config.certificates.length == 0) {
                HttpsHelper.SSLParams sslParams = HttpsHelper.getSslSocketFactory();
                okBuilder.sslSocketFactory(sslParams.sSLSocketFactory, sslParams.trustManager);
            } else {
                okBuilder.sslSocketFactory(HttpsHelper.getSSLSocketFactory(config.certificates), HttpsHelper.UnSafeTrustManager);
            }
            okBuilder.hostnameVerifier(HttpsHelper.UnSafeHostnameVerifier);
        }
//        if (config.isUserUrlStrategy) {
//            okBuilder = RetrofitUrlManager.getInstance().with(okBuilder);
//        }
        return okBuilder.build();
    }


    public static final class Builder {
        //超时时间设置
        private int connectTimeout;
        private int readTimeout;
        private int writeTimeout;
        //是否开启默认log日志
        private boolean isUseLog;
        private String logRequestTag;
        private String logResponseTag;
        private HttpLogInterceptor2.Level logLevel = HttpLogInterceptor2.Level.ALL;
        //是否开启切换baseURL策略
        private boolean isUserUrlStrategy;
        //是否配置https
        private boolean isUseHttps;
        //证书
        private int[] certificates;

        private final List<Interceptor> interceptors = new ArrayList<>();
        private final List<Interceptor> netInterceptors = new ArrayList<>();

        //缓存路径
        private File cacheFile;
        //http缓存的大小限制
        private int cacheSize;
        //是否使用默认的通用缓存拦截器
        private boolean isUseCacheIntercept;
        //有无网络时的缓存配置
        private int cacheSecondWithNet = -1;//为0时，不使用缓存
        private int cacheSecondWithoutNet;

        //重试次数默认3次
        private int retryCount;
        //延迟重试
        private int retryDelay;
        //叠加延迟
        private int retryIncreaseDelay;

        //是否异常重连，重连延迟时间，重连次数；该配置放到observer中进行处理
        private boolean isUseRetryWhenError;
        private int secondRetryDelay = -1;
        private int maxRetryCount;

        //cookie持久化
//        private CookieJar cookieJar;

//        private Authenticator authenticator;
//        private Cache cache;
//        private DNS dns;

        //基础URL
        private String baseUrl;
        private final List<Converter.Factory> converterFactories = new ArrayList<>();
        private final List<CallAdapter.Factory> adapterFactories = new ArrayList<>();

        public Builder() {
        }

        //基础URL
        public RxClient.Builder baseUrl(String baseUrl) {
            if (!TextUtils.isEmpty(baseUrl) && !baseUrl.endsWith("/")) {
                baseUrl += "/";
            }
            this.baseUrl = baseUrl;
            return this;
        }

        //添加转换器
        public RxClient.Builder addConvertFactory(Converter.Factory factory) {
            this.converterFactories.add(factory);
            return this;
        }

        //添加适配器
        public RxClient.Builder addAdapterFactory(CallAdapter.Factory factory) {
            this.adapterFactories.add(factory);
            return this;
        }

        //isUserUrlStrategy：是否使用动态切换URL
        public RxClient.Builder isUserUrlStrategy(boolean isUserUrlStrategy) {
            this.isUserUrlStrategy = isUserUrlStrategy;
            return this;
        }

        //超时时间
        public RxClient.Builder connectTimeout(int seconds) {
            this.connectTimeout = seconds;
            return this;
        }

        public RxClient.Builder readTimeout(int seconds) {
            this.readTimeout = seconds;
            return this;
        }

        public RxClient.Builder writeTimeout(int seconds) {
            this.writeTimeout = seconds;
            return this;
        }

        //是否开启Log日志
        public RxClient.Builder isUseLog(boolean isUseLog) {
            this.isUseLog = isUseLog;
            return this;
        }

        //日志的请求tag，默认是Request
        public RxClient.Builder logRequestTag(String tag) {
            this.logRequestTag = tag;
            return this;
        }

        //日志的响应tag，默认是Response
        public RxClient.Builder logResponseTag(String tag) {
            this.logResponseTag = tag;
            return this;
        }

        //日志级别
        public RxClient.Builder logLevel(HttpLogInterceptor2.Level level) {
            this.logLevel = level;
            return this;
        }

        //配置https
        public RxClient.Builder isUseHttps(boolean isUseHttps) {
            this.isUseHttps = isUseHttps;
            return this;
        }

        //配置https证书
        public RxClient.Builder certificates(@RawRes int[] certificate) {
            this.certificates = certificate;
            return this;
        }

        //添加拦截器
        public RxClient.Builder addInterceptor(Interceptor interceptor) {
            this.interceptors.add(interceptor);
            return this;
        }

        //添加网络拦截器
        public RxClient.Builder addNetInterceptor(Interceptor interceptor) {
            this.netInterceptors.add(interceptor);
            return this;
        }

        public RxClient build() {
            return new RxClient(this);
        }

        /**
         * 使用该方法则不再使用以上的配置参数，完全使用传进来的retrofit
         *
         * @param retrofit 网络请求类
         * @return 组装类RxClient
         */
        public RxClient build(Retrofit retrofit) {
            return new RxClient(retrofit);
        }

    }
}
