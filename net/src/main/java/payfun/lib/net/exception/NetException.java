package payfun.lib.net.exception;


/**
 * @author : zhangqg
 * date   : 2018/11/12 9:31
 * desc   : <整个HTTP请求过程中统一的错误接口格式>
 */
public class NetException extends Exception {
    /**
     * 错误码
     */
    private String errorCode;
    /**
     * 异常标题
     */
    private String errorTitle;

    /**
     * 对应错误的处理方法
     */
    private String[] tips;


    public NetException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public NetException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public NetException(String errorCode, Throwable cause) {
        super(cause);
        this.errorCode = errorCode;
    }

    public String getErrorTitle() {
        return errorTitle;
    }

    public void setErrorTitle(String errorData) {
        this.errorTitle = errorData;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String[] getTips() {
        return tips;
    }

    public void setTips(String[] tips) {
        this.tips = tips;
    }

}
