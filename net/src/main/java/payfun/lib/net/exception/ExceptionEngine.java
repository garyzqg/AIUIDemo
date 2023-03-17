package payfun.lib.net.exception;

import com.google.gson.JsonIOException;
import com.google.gson.JsonParseException;
import com.google.gson.stream.MalformedJsonException;

import org.json.JSONException;

import java.io.InterruptedIOException;
import java.net.ConnectException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.text.ParseException;

import javax.net.ssl.SSLHandshakeException;

import retrofit2.HttpException;


/**
 * @author : zhangqg
 * date   : 2018/3/12 9:29
 * desc   : <网络封装框架下错误/异常处理工具，以后可以扩展
 * *
 * *             <无网络/>                       请检查网线；检查ip地址；检查服务器地址；重新加载；
 * *             <Http错误均视作网络错误/>       请检查网线；检查ip地址；检查服务器地址；重新加载；
 * *
 * *             <服务器返回：token失效/>        跳转到登录界面；重新加载；
 * *             <服务器返回：账号登录冲突/>     提示退出或跳转到登录界面或直接重新登录；
 * *             <服务器返回：无账号/>           提示账号错误,显示清空按钮；
 * *             <服务器返回：密码错误/>         提示密码错误，显示清空按钮；
 * *             <服务器返回：参数异常/>         提示参数错误；重新加载；
 * *             <服务器返回：空视图/>           空；重新加载；
 * *
 * *             <服务器返回：账号被禁/>         跳转到登录界面；重新加载；
 * *             <服务器返回：已经注册/>         跳转到登录界面；重新加载；
 * *             <服务器返回：重新登陆/>         跳转到登录界面；
 * *             <服务器返回：验证码错误/>       重新发送验证码；
 * *             <服务器返回：无权限/>           提示无权限；重新加载；
 * *
 * *             <网络连接错误：/>               请检查网线；检查ip地址；检查服务器地址；重新加载；
 * *             <网络超时：/>                   请检查网线；检查ip地址；检查服务器地址；重新加载；
 * *             <解析响应数据错误：/>           获取响应数据；重新加载；联系厂家；
 * *             <请求数据错误：/>               获取请求数据；重新加载；联系厂家；
 * *             <未知错误：/>                   显示未知错误信息；重新加载；联系厂家；
 * *
 * *      根据错误类型，进行处理的提示，然后更近一步，显示本次网络请求的所有信息，比如：请求头，请求接口，请求参数，和请求结果>
 */
public class ExceptionEngine {
    /**
     * 无网络
     */
    public static final String NO_NETWORK = "10000";
    /**
     * 网络错误HTTP
     */
    public static final String HTTP_ERROR = "11000";

    /**
     * 服务器返回：token失效
     */
    public static final String REMOTE_SERVER_TOKEN = "11001";
    /**
     * 服务器返回：账号被禁
     */
    public static final String REMOTE_SERVER_ACCOUNT_BANNED = "11002";
    /**
     * 服务器返回：已经注册
     */
    public static final String REMOTE_SERVER_REGISTERED = "11003";
    /**
     * 服务器返回：账号登录冲突
     */
    public static final String REMOTE_SERVER_ACCOUNT_CONFLICT = "11004";
    /**
     * 服务器返回：重新登陆
     */
    public static final String REMOTE_SERVER_RELOGIN = "11005";
    /**
     * 服务器返回：验证码错误
     */
    public static final String REMOTE_SERVER_AUTOCODE_ERROR = "11006";
    /**
     * 服务器返回：无该账号
     */
    public static final String REMOTE_SERVER_ACCOUNT_ERROR = "11007";
    /**
     * 服务器返回：密码错误
     */
    public static final String REMOTE_SERVER_PASSWORD_ERROR = "11008";
    /**
     * 服务器返回：无权限
     */
    public static final String REMOTE_SERVER_NOACCESS = "11009";
    /**
     * 服务器返回：参数异常
     */
    public static final String REMOTE_SERVER_PARAM_ERROR = "11010";
    /**
     * 服务器返回：空视图
     */
    public static final String REMOTE_SERVER_EMPTY = "11011";
    /**
     * 服务器返回：需要升级
     */
    public static final String REMOTE_SERVER_UPGRADE = "11012";

    /**
     * 网络连接错误
     */
    public static final String NET_CONN_ERROR = "12000";
    /**
     * 网络超时
     */
    public static final String TIME_OUT = "13000";
    /**
     * 请求数据错误
     */
    public static final String REQUEST_DATA_ERROR = "14000";
    /**
     * 解析响应数据错误
     */
    public static final String PARSER_RESPON_DATA_ERROR = "15000";
    /**
     * 响应数据不符合要求
     */
    public static final String RESPON_DATA_UNMATCH = "16000";
    /**
     * 未知错误
     */
    public static final String UNKNOWN = "19000";

    public static NetException handleException(Throwable e) {
        NetException ex;
        if (e instanceof ConnectException
                || e instanceof SSLHandshakeException
                || e instanceof SocketException
                || e instanceof UnknownHostException) {
            //连接网络错误
            ex = new NetException(NET_CONN_ERROR, e);
            ex.setErrorTitle("<网络连接错误>");
            ex.setTips(new String[]{"请检查网络"});
            return ex;
        } else if (e instanceof NullPointerException) {
            //请求数据错误
            ex = new NetException(REQUEST_DATA_ERROR, e);
            ex.setErrorTitle("<数据错误>");
            return ex;
        } else if (e instanceof HttpException) {
            //HTTP错误
            HttpException httpExc = (HttpException) e;
            ex = new NetException(httpExc.code()+"", e);
            ex.setErrorTitle("<网络错误 " + "HTTP(code：" + httpExc.code() + ")>");
            return ex;
        } else if (e instanceof SocketTimeoutException
                || e instanceof InterruptedIOException) {
            //网络超时
            ex = new NetException(TIME_OUT, e);
            ex.setErrorTitle("<网络超时>");
            ex.setTips(new String[]{"请检查网络"});
            return ex;
        } else if (e instanceof JsonParseException
                || e instanceof JsonIOException
                || e instanceof JSONException
                || e instanceof ParseException
                || e instanceof IllegalArgumentException
                || e instanceof MalformedJsonException) {
            //解析数据错误
            ex = new NetException(PARSER_RESPON_DATA_ERROR, e);
            ex.setErrorTitle("<数据解析异常>");
            return ex;
        } else if (e instanceof RemoteServerException) {
            //服务器返回的错误
            RemoteServerException serverExc = (RemoteServerException) e;
            ex = new NetException(serverExc.getErrorCode(), "<" + serverExc.getMessage() + ">");
            ex.setErrorTitle(serverExc.getMessage());
            return ex;
        } else if (e instanceof ResultException) {
            //服务器返回的错误
            ResultException serverExc = (ResultException) e;
            ex = new NetException(serverExc.getErrorCode(), "<" + serverExc.getMsg() + ">");
            ex.setErrorTitle(serverExc.getMessage());
            return ex;
        } else if (e instanceof NetException) {
            //自定义异常
            ex = (NetException) e;
            return ex;
        } else {  //未知错误
            ex = new NetException(UNKNOWN, e);
            ex.setErrorTitle("<未知错误>");
            return ex;
        }
    }


/*    public static void main(String[] args) {
        RemoteServerException rex = new RemoteServerException(1, "商户不存在");
        NetException netException = new NetException(rex.getErrorCode(),rex.getMessage());
        netException.setErrorData("<错误>");
        System.out.println(netException.getMessage());

    }*/


}
