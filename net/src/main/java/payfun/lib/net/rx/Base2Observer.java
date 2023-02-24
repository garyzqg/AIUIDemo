package payfun.lib.net.rx;

import androidx.annotation.NonNull;

import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import payfun.lib.basis.utils.LogUtil;
import payfun.lib.net.exception.ExceptionEngine;
import payfun.lib.net.exception.NetException;
import payfun.lib.net.helper.NetHelper;


/**
 * @author : 时光
 * e-mail : qurongzhen@pay.media
 * date   : 2021/5/19 9:23
 * desc   : <p>Rx订阅回调封装
 */
public abstract class Base2Observer<T> implements Observer<T> {


    @Override
    public final void onSubscribe(@NonNull Disposable d) {
        boolean connected = NetHelper.isConnected();
        //网络是否连接
        LogUtil.i("Net -> Subscribe:" + System.currentTimeMillis() + "||" + connected + "||" + Thread.currentThread().getName());
        if (!connected) {
            //没有网络的时候，取消本次订阅，并调用onError，显示异常，此时不会走onNext，onComplete
            if (!d.isDisposed()) {
                d.dispose();
            }
            onError(new NetException(ExceptionEngine.NO_NETWORK, "<无网络>：请检查网络"));
            onComplete();
        }
    }

    @Override
    public final void onNext(@NonNull T t) {
        onSuccess(t);
    }


    @Override
    public final void onError(@NonNull Throwable e) {
        LogUtil.e(e);
        onFailed(e);
    }

    /**
     * 由于LifecycleProvider取消监听直接截断事件发送，但是必定回调onComplete()
     * 因此在这里判断请求是否被取消，如果到这里还未被取消，说明是LifecycleProvider导致的取消请求，回调onCancel逻辑
     * 备注：
     * 1.子类重写此方法时需要调用super
     * 2.多个请求复用一个监听者HttpObserver时，tag会被覆盖，取消回调会有误
     */
    @Override
    public void onComplete() {
        LogUtil.i("Net -> Complete");
    }


    /**
     * 成功
     *
     * @param t 成功回调结果
     */
    protected abstract void onSuccess(T t);

    /**
     * 失败
     *
     * @param ex 封装后的异常情况
     */
    protected abstract void onFailed(Throwable ex);

}
