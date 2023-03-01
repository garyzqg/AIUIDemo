package payfun.lib.net.helper;

/**
 * @author : zhangqg
 * date   : 2022/8/11 16:20
 * desc   : <p>网络状态
 */
public @interface NetState {
    int NO_NULL = 0;
    int NET_SUCCESS = 1;
    int NET_ERROR = 2;
    int IOT_SUCCESS = 3;
    int IOT_ERROR = 4;
}
