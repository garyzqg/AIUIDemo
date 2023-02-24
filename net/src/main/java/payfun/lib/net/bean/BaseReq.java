package payfun.lib.net.bean;

/**
 * @author : 时光
 * e-mail : qurongzhen@pay.media
 * date   : 2022/5/10 15:49
 * desc   : <基础请求参数>
 */
public class BaseReq<T> {

    private int action;
    private T req;


    public int getAction() {
        return action;
    }

    public void setAction(int action) {
        this.action = action;
    }

    public T getReq() {
        return req;
    }

    public void setReq(T req) {
        this.req = req;
    }
}
