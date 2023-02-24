package payfun.lib.net.fun;

import io.reactivex.rxjava3.functions.Function;
import okhttp3.ResponseBody;
import payfun.lib.net.bean.DownloadEntity;
import payfun.lib.net.convert.ConvertHelper;
import payfun.lib.net.exception.NetException;

/**
 * @author : 时光
 * e-mail : qurongzhen@pay.media
 * date   : 2019/11/20 9:38
 * desc   : <服务器返回结果转换类：是对返回参数是ResponseBody的处理>
 * desc   : <可参考本类写法，自行仿写需要的中间转换层，如统一的结果码处理，统一解密等>
 */
public class DownloadNetResultFun implements Function<ResponseBody, DownloadEntity> {

    @Override
    public DownloadEntity apply(ResponseBody responseBody) throws Exception {
        DownloadEntity downloadEntity = ConvertHelper.convertStreamFile(responseBody, new DownloadEntity("", ""));
        if (downloadEntity.isSuccess()) {
            return downloadEntity;
        } else {
            throw new NetException("" + downloadEntity.getCode(), downloadEntity.getMsg());
        }
    }

}
