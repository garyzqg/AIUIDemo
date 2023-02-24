package payfun.lib.net.fun;

import android.util.Base64;

import io.reactivex.rxjava3.functions.Function;
import okhttp3.ResponseBody;
import payfun.lib.basis.utils.CryptUtil;
import payfun.lib.basis.utils.LogUtil;
import payfun.lib.net.helper.GsonHelper;

/**
 * @author : 时光
 * e-mail : qurongzhen@pay.media
 * date   : 2019/11/20 9:38
 * desc   : <服务器返回结果转换类：是对返回参数是ResponseBody的处理>
 * desc   : <可参考本类写法，自行仿写需要的中间转换层，如统一的结果码处理，统一解密等>
 */
public class BaseNetResultFun<OUT> implements Function<ResponseBody, OUT> {

    private Class<OUT> outClass;

    private BaseNetResultFun() {
    }

    public BaseNetResultFun(Class<OUT> tClass) {
        outClass = tClass;
    }


    @Override
    public OUT apply(ResponseBody responseBody) throws Exception {
        String result = responseBody.string();
        LogUtil.e(result + "\n" + Thread.currentThread().getName());

        byte[] decodeByte = Base64.decode(result, Base64.DEFAULT);
        //GZIP解压
        result = CryptUtil.uncompressToString(decodeByte);
        LogUtil.e(result);

        return GsonHelper.GSON.fromJson(result, outClass);
    }

}
