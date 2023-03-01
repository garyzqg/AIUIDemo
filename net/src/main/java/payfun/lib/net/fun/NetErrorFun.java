package payfun.lib.net.fun;


import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.functions.Function;
import payfun.lib.net.exception.ExceptionEngine;

/**
 * @author : zhangqg
 * date   : 2019/11/20 9:32
 * desc   : <网络请求结果处理函数>
 */
public class NetErrorFun<T> implements Function<Throwable, Observable<T>> {

    @Override
    public Observable<T> apply(Throwable throwable) throws Throwable {
        return Observable.error(ExceptionEngine.handleException(throwable));
    }

}
