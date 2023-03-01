package payfun.lib.net.exception;


/**
 * @author : zhangqg
 * date   : 2021/5/19 9:32
 * desc   : <结果异常>
 */
public class ResultException extends RuntimeException {

    private String msg;
    /**
     * 错误码
     */
    private String errorCode;

    public ResultException(String errorCode, String msg) {
        super();
        this.msg = msg;
        this.errorCode = errorCode;
    }


    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }
}
