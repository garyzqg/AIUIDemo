package payfun.lib.net.exception;


/**
 * @author : 时光
 * e-mail : qurongzhen@pay.media
 * date   : 2018/11/13 9:32
 * desc   : <和后端沟通后自定义的服务器信息异常类>
 */
public class RemoteServerException extends RuntimeException {
    /**
     * 错误码
     */
    private String errorCode;
    /**
     * 对应错误的处理方法
     */
    private String[] tips;

    public RemoteServerException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }


    public RemoteServerException(String errorCode, Throwable cause) {
        super(cause);
        this.errorCode = errorCode;
    }

    public RemoteServerException(String errorCode, String errorMsg, Throwable cause) {
        super(cause);
        this.errorCode = errorCode;
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
