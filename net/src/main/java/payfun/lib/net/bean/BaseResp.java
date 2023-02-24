package payfun.lib.net.bean;

/**
 * @author : 时光
 * e-mail : qurongzhen@pay.media
 * date   : 2022/5/10 15:50
 * desc   : <基础响应参数>
 */
public class BaseResp<T> {
    private int code;
    private String message;
    private T data;

    public BaseResp() {
    }

    public BaseResp(int code, T resp) {
        this.code = code;
        this.data = resp;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public boolean isSuccess() {
        return code == 200;
    }

    public String getCodeMsg() {
        return getMessage() + "(code：" + getCode() + ")";
    }
}
