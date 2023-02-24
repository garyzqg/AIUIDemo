package payfun.lib.net.interceptor;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.text.DateFormat;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.GzipSink;
import okio.GzipSource;
import okio.Okio;

/**
 * @author : 时光
 * e-mail : qurongzhen@pay.media
 * date   : 2021/5/21 18:50
 * desc   : <Gzip压缩拦截器，可参考本类仿写拦截器>
 */
public class GzipResponseInterceptor implements Interceptor {
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request originalRequest = chain.request();
        Response response = chain.proceed(originalRequest);
        String date = response.header("date");
        try {
            DateFormat.getInstance().parse(date);
        } catch (Exception e) {
            e.printStackTrace();
        }

        ResponseBody responseBody = response.body();
        String respBody = null;
        if (responseBody != null) {
            BufferedSource source = responseBody.source();
            source.request(Long.MAX_VALUE);
            Buffer buffer = source.buffer();

            Charset charset = Charset.forName("UTF-8");
            MediaType contentType = responseBody.contentType();
            if (contentType != null) {
                try {
                    charset = contentType.charset(Charset.forName("UTF-8"));
                } catch (UnsupportedCharsetException e) {
                    e.printStackTrace();
                }
            }
            //接口返回的加密数据
            respBody = buffer.clone().readString(charset);
        }

//        try {
//
//            unGzip();
//            //这里具体参考自己项目的加密方式
//            byte[] bytes = EncryptUtils.decryptHexStringAES(respBody, "密钥", "AES/CBC/PKCS5Padding","偏移量".getBytes());
//            //解密后的数据
//            String decryptedData = new String(bytes);
//            //返回新创建的response
//            return response.newBuilder().body(ResponseBody.create(decryptedData,MediaType.get("text/plain"))).build();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        return response;
    }

    private RequestBody gzip(final RequestBody body) {

        return new RequestBody() {
            @Override
            public MediaType contentType() {
                return body.contentType();
            }

            @Override
            public long contentLength() {
                return -1; // 无法提前知道压缩后的数据大小
            }

            @Override
            public void writeTo(BufferedSink sink) throws IOException {
                BufferedSink gzipSink = Okio.buffer(new GzipSink(sink));
                body.writeTo(gzipSink);
                gzipSink.close();
            }
        };
    }


    private ResponseBody unGzip(final ResponseBody body) {
        return new ResponseBody() {
            @Nullable
            @Override
            public MediaType contentType() {
                return body.contentType();
            }

            @Override
            public long contentLength() {
                return -1;
            }

            @NotNull
            @Override
            public BufferedSource source() {
                GzipSource gzipSource = new GzipSource(body.source());
                BufferedSource buffer = Okio.buffer(gzipSource);
                try {
                    if (gzipSource != null) {
                        gzipSource.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return buffer;
            }
        };
    }
}
