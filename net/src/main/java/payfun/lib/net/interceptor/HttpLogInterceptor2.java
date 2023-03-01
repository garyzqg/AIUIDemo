package payfun.lib.net.interceptor;
/*
 * Copyright (C) 2015 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import android.text.TextUtils;

import java.io.EOFException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import okhttp3.Connection;
import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.internal.http.HttpHeaders;
import okhttp3.internal.platform.Platform;
import okio.Buffer;
import okio.BufferedSource;
import okio.GzipSource;


/**
 * @author : zhangqg
 * date   : 2021/5/18 14:39
 * desc   : <日志拦截器：该日志拦截器适配 okhttp3:4.9.1>
 */
public final class HttpLogInterceptor2 implements Interceptor {
    private static final Charset UTF8 = StandardCharsets.UTF_8;

    public enum Level {
        /**
         * No logs.
         */
        NONE,
        /**
         * Logs request and response lines.
         *
         * <p>Example:
         * <pre>{@code
         * --> POST /greeting http/1.1 (3-byte body)
         *
         * <-- 200 OK (22ms, 6-byte body)
         * }</pre>
         */
        BASIC,
        /**
         * Logs request and response lines and their respective headers.
         *
         * <p>Example:
         * <pre>{@code
         * --> POST /greeting http/1.1
         * Host: example.com
         * Content-Type: plain/text
         * Content-Length: 3
         * --> END POST
         *
         * <-- 200 OK (22ms)
         * Content-Type: plain/text
         * Content-Length: 6
         * <-- END HTTP
         * }</pre>
         */
        HEADERS,
        /**
         * Logs request and response lines and their respective headers and bodies (if present).
         *
         * <p>Example:
         * <pre>{@code
         * --> POST /greeting http/1.1
         * Host: example.com
         *
         * Hi?
         * --> END POST
         *
         * <-- 200 OK (22ms)
         *
         * Hello!
         * <-- END HTTP
         * }</pre>
         */
        BODY,
        /**
         * Logs request and response lines and their respective headers and bodies (if present).
         *
         * <p>Example:
         * <pre>{@code
         * --> POST /greeting http/1.1
         * Host: example.com
         * Content-Type: plain/text
         * Content-Length: 3
         *
         * Hi?
         * --> END POST
         *
         * <-- 200 OK (22ms)
         * Content-Type: plain/text
         * Content-Length: 6
         *
         * Hello!
         * <-- END HTTP
         * }</pre>
         */
        ALL
    }

    public interface Logger {
        /**
         * 日志回调
         *
         * @param tag     tag
         * @param message 日志内容
         */
        void log(String tag, String message);

        /**
         * A {@link HttpLogInterceptor2.Logger} defaults output appropriate for the current platform.
         */
        HttpLogInterceptor2.Logger DEFAULT = (tag, message) -> Platform.get().log(message, Platform.INFO, null);

    }

    public HttpLogInterceptor2() {
        this(HttpLogInterceptor2.Logger.DEFAULT);
    }

    public HttpLogInterceptor2(HttpLogInterceptor2.Logger logger) {
        this.logger = logger;
    }

    private final HttpLogInterceptor2.Logger logger;

    private volatile HttpLogInterceptor2.Level level = HttpLogInterceptor2.Level.NONE;
    private volatile String REQUEST = "HTTP:Request";
    private volatile String RESPONSE = "HTTP:Response";

    /**
     * Change the level at which this interceptor logs.
     */
    public HttpLogInterceptor2 setLevel(HttpLogInterceptor2.Level level) {
        if (level == null) {
            throw new NullPointerException("level == null. Use Level.NONE instead.");
        }
        this.level = level;
        return this;
    }

    public void setRequestTag(String requestTag) {
        if (!TextUtils.isEmpty(requestTag)) {
            this.REQUEST = requestTag;
        }
    }

    public void setResponseTag(String responseTag) {
        if (!TextUtils.isEmpty(responseTag)) {
            this.RESPONSE = responseTag;
        }
    }

    public HttpLogInterceptor2.Level getLevel() {
        return level;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        HttpLogInterceptor2.Level level = this.level;

        Request request = chain.request();
        if (level == HttpLogInterceptor2.Level.NONE) {
            return chain.proceed(request);
        }

        boolean logBody = level == HttpLogInterceptor2.Level.BODY || level == Level.ALL;
        boolean logHeaders = level == Level.ALL || level == HttpLogInterceptor2.Level.HEADERS;

        RequestBody requestBody = request.body();
        boolean hasRequestBody = requestBody != null;
        Connection connection = chain.connection();

        String requestBasicMessage = "--> "
                + request.method()
                + ' ' + request.url()
                + (connection != null ? " " + connection.protocol() : "");
        if (!logHeaders && hasRequestBody) {
            requestBasicMessage += " (" + requestBody.contentLength() + "-byte body)";
        }
        String requestHeaderMessage = "";
        String requestBodyMessage = "";
        String requestEndMessage = "";

        if (logHeaders) {
            if (hasRequestBody) {
                // Request body headers are only present when installed as a network interceptor. Force
                // them to be included (when available) so there values are known.
                if (requestBody.contentType() != null) {
                    requestHeaderMessage += "Content-Type: " + requestBody.contentType() + "\n";
                }
                if (requestBody.contentLength() != -1) {
                    requestHeaderMessage += "Content-Length: " + requestBody.contentLength() + "\n";
                }
            }

            Headers headers = request.headers();
            for (int i = 0, count = headers.size(); i < count; i++) {
                String name = headers.name(i);
                // Skip headers from the request body as they are explicitly logged above.
                if (!"Content-Type".equalsIgnoreCase(name) && !"Content-Length".equalsIgnoreCase(name)) {
                    requestHeaderMessage += name + ": " + headers.value(i) + "\n";
                }
            }
        }

        if (!logBody || !hasRequestBody) {
            requestEndMessage += "--> END " + request.method();
        } else if (bodyEncoded(request.headers())) {
            requestEndMessage += "--> END " + request.method() + " (encoded body omitted)";
        } else if (requestBody.isDuplex()) {
            requestEndMessage += "--> END " + request.method() + " (duplex request body omitted)";
        } else if (requestBody.isOneShot()) {
            requestEndMessage += "--> END " + request.method() + " (one-shot body omitted)";
        } else {
            Buffer buffer = new Buffer();
            requestBody.writeTo(buffer);

            Charset charset = UTF8;
            MediaType contentType = requestBody.contentType();
            if (contentType != null) {
                charset = contentType.charset(UTF8);
            }

            if (isPlaintext(buffer)) {
                requestBodyMessage += buffer.readString(charset) + "\n";
                requestEndMessage += "--> END " + request.method()
                        + " (" + requestBody.contentLength() + "-byte body)";
            } else {
                requestEndMessage += "--> END " + request.method() + " (binary "
                        + requestBody.contentLength() + "-byte body omitted)";
            }
        }
        logger.log(REQUEST, requestBasicMessage + "\n" + (logHeaders ? requestHeaderMessage : "")
                + (logBody ? "\n" + requestBodyMessage : "") + requestEndMessage);

        long startNs = System.nanoTime();
        Response response;
        try {
            response = chain.proceed(request);
        } catch (Exception e) {
            logger.log(RESPONSE, "<-- HTTP FAILED: " + e);
            throw e;
        }
        long tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs);

        ResponseBody responseBody = response.body();
        long contentLength = responseBody == null ? 0 : responseBody.contentLength();
        String bodySize = contentLength != -1 ? contentLength + "-byte" : "unknown-length";

        String responseBasicMessage = "<-- "
                + response.code()
                + (response.message().isEmpty() ? "" : ' ' + response.message())
                + ' ' + response.request().url()
                + " (" + tookMs + "ms" + (!logHeaders ? ", " + bodySize + " body" : "") + ')';

        String responseHeaderMessage = "";
        String responseBodyMessage = "";
        String responseEndMessage = "";

        if (logHeaders) {
            Headers headers = response.headers();
            for (int i = 0, count = headers.size(); i < count; i++) {
                responseHeaderMessage += headers.name(i) + ": " + headers.value(i) + "\n";
            }

            if (!logBody || !HttpHeaders.hasBody(response)) {
                responseEndMessage += "<-- END HTTP";
            } else if (bodyEncoded(response.headers())) {
                responseEndMessage += "<-- END HTTP (encoded body omitted)";
            } else {
                BufferedSource source = responseBody.source();
                // Buffer the entire body.
                source.request(Long.MAX_VALUE);
                Buffer buffer = source.buffer();

                Long gzippedLength = null;
                if ("gzip".equalsIgnoreCase(headers.get("Content-Encoding"))) {
                    gzippedLength = buffer.size();
                    GzipSource gzippedResponseBody = null;
                    try {
                        gzippedResponseBody = new GzipSource(buffer.clone());
                        buffer = new Buffer();
                        buffer.writeAll(gzippedResponseBody);
                    } catch (Exception e) {
                        logger.log(RESPONSE, "<-- HTTP Gzip FAILED: " + e);
                    } finally {
                        try {
                            if (gzippedResponseBody != null) {
                                gzippedResponseBody.close();
                            }
                        } catch (Exception e) {
                            //ignored here
                        }
                    }
                }

                Charset charset = UTF8;
                MediaType contentType = responseBody.contentType();
                if (contentType != null) {
                    charset = contentType.charset(UTF8);
                }

                if (!isPlaintext(buffer)) {
                    responseEndMessage += "<-- END HTTP (binary " + buffer.size() + "-byte body omitted)";
                } else {
                    if (contentLength != 0) {
                        responseBodyMessage += buffer.clone().readString(charset) + "\n";
                    }

                    if (gzippedLength != null) {
                        responseEndMessage += "<-- END HTTP (" + buffer.size() + "-byte,"
                                + gzippedLength + "-gzipped-byte body)";
                    } else {
                        responseEndMessage += "<-- END HTTP (" + buffer.size() + "-byte body)";
                    }
                }
            }
        }
        logger.log(RESPONSE, responseBasicMessage + "\n" + (logHeaders ? responseHeaderMessage : "")
                + (logBody ? "\n" + responseBodyMessage : "") + responseEndMessage);
        return response;
    }

    /**
     * Returns true if the body in question probably contains human readable text. Uses a small sample
     * of code points to detect unicode control characters commonly used in binary file signatures.
     */
    static boolean isPlaintext(Buffer buffer) {
        try {
            Buffer prefix = new Buffer();
            long byteCount = buffer.size() < 64 ? buffer.size() : 64;
            buffer.copyTo(prefix, 0, byteCount);
            for (int i = 0; i < 16; i++) {
                if (prefix.exhausted()) {
                    break;
                }
                int codePoint = prefix.readUtf8CodePoint();
                if (Character.isISOControl(codePoint) && !Character.isWhitespace(codePoint)) {
                    return false;
                }
            }
            return true;
        } catch (EOFException e) {
            // Truncated UTF-8 sequence.
            return false;
        }
    }

    private boolean bodyEncoded(Headers headers) {
        String contentEncoding = headers.get("Content-Encoding");
        return contentEncoding != null && !"identity".equalsIgnoreCase(contentEncoding) && !"gzip".equalsIgnoreCase(contentEncoding);
    }
}

