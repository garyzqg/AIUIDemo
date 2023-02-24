package payfun.lib.basis.utils;

import java.io.Closeable;

/**
 * @author : 时光
 * e-mail : qurongzhen@pay.media
 * date   : 2021/5/6 13:57
 * desc   : <关流>
 */
public final class CloseUtil {
    /**
     * 关流
     * 关闭原则：
     * 1.先开后关；
     * 2.看依赖关系，如果流a依赖流b，应该先关闭流a，再关闭流b；
     * 3.完全可以只关闭处理流，不用关闭节点流。处理流关闭的时候，会调用其处理的节点流的关闭方法，如果将节点流关闭以后再关闭处理流，会抛出IO异常
     *
     * @param closeables The closeables.
     */
    public static void closeIO(final Closeable... closeables) {
        if (closeables == null) {
            return;
        }
        for (Closeable closeable : closeables) {
            if (closeable != null) {
                try {
                    closeable.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Close the io stream quietly.
     *
     * @param closeables The closeables.
     */
    public static void closeIOQuietly(final Closeable... closeables) {
        if (closeables == null) {
            return;
        }
        for (Closeable closeable : closeables) {
            if (closeable != null) {
                try {
                    closeable.close();
                } catch (Exception ignored) {
                }
            }
        }
    }
}
