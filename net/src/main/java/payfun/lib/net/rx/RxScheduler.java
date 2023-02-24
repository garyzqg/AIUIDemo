package payfun.lib.net.rx;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.FlowableTransformer;
import io.reactivex.rxjava3.core.ObservableTransformer;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.schedulers.Schedulers;
import okhttp3.ResponseBody;
import payfun.lib.net.fun.BaseNetResultFun;


/**
 * @author : 时光
 * e-mail : qurongzhen@pay.media
 * date   : 2021/5/18 18:09
 * desc   : <p>Rx线程调度,可根据本类进行仿写
 */
public class RxScheduler {

    /**
     * 统一线程处理
     *
     * @param <T> 指定的泛型类型
     * @return ObservableTransformer
     */
    public static <T> ObservableTransformer<T, T> obsIo2Main() {
        return upstream -> upstream.subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                //防止1s内重复请求
//                .debounce(1, TimeUnit.SECONDS)
                ;
    }

    /**
     * http返回统一处理流程
     * 可参考该方法自定义处理流程
     * ResponseBody为入参，OUT为出参
     *
     * @param outClass 转换后的bean类
     * @param <OUT>    出参
     * @return Observable
     */
    public static <OUT> ObservableTransformer<ResponseBody, OUT> obsParseResp(Class<OUT> outClass) {
        return upstream -> {
            return upstream
                    .subscribeOn(Schedulers.io())
                    .unsubscribeOn(Schedulers.io())
                    .map(new BaseNetResultFun<>(outClass))
//                    .onErrorResumeNext(new NetErrorFun<>())
                    //observeOn使用的位置顺序决定了以上方法所在的线程
                    .observeOn(AndroidSchedulers.mainThread())
                    //防止1s内重复请求
//                    .debounce(1, TimeUnit.SECONDS)
                    //.(AutoDispose.<T>autoDisposable(AndroidLifecycleScopeProvider.from(this, Lifecycle.Event.ON_DESTROY)));
                    ;
        };
    }

    public static <OUT> ObservableTransformer<ResponseBody, OUT> obsParseResp(final Function<ResponseBody, OUT> netServerResultFun) {
        return upstream -> {
            return upstream
                    .subscribeOn(Schedulers.io())
                    .unsubscribeOn(Schedulers.io())
                    .map(netServerResultFun)
//                    .onErrorResumeNext(new NetErrorFun<>())
                    //observeOn使用的位置顺序决定了以上方法所在的线程
                    .observeOn(AndroidSchedulers.mainThread())
                    //防止1s内重复请求
//                    .debounce(1, TimeUnit.SECONDS)
                    //.(AutoDispose.<T>autoDisposable(AndroidLifecycleScopeProvider.from(this, Lifecycle.Event.ON_DESTROY)));
                    ;
        };
    }


    /**
     * 统一线程处理
     *
     * @param <T> 指定的泛型类型
     * @return FlowableTransformer
     */
    public static <T> FlowableTransformer<T, T> floIo2Main() {
        return upstream -> upstream.subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                //防止1s内重复请求
//                .debounce(1, TimeUnit.SECONDS)
                ;
    }


    /**
     * 通过反射,获得定义Class时声明的父类的范型参数的类型.
     * 如public BookManager extends GenericManager<Book>
     * Type[] genTypes = clazz.getGenericInterfaces();
     *
     * @param clazz clazz The class to introspect
     * @param index the Index of the generic declaration,start from 0.
     */
    public static Class getSuperClassGenericType(Class clazz, int index) throws IndexOutOfBoundsException {
        //返回表示此 Class 所表示的实体（类、接口、基本类型或 void）的直接超类的 Type。
        Type genType = clazz.getGenericSuperclass();
        if (!(genType instanceof ParameterizedType)) {
            return Object.class;
        }
        //返回表示此类型实际类型参数的 Type 对象的数组(),赋值给this.class
        Type[] params = ((ParameterizedType) genType).getActualTypeArguments();
        if (index >= params.length || index < 0) {
            return Object.class;
        }
        if (!(params[index] instanceof Class)) {
            return Object.class;
        }
        return (Class) params[index];
    }


}
