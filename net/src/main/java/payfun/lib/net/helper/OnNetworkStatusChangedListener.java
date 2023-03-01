package payfun.lib.net.helper;

/**
 * @author : zhangqg
 * date   : 2022/8/10 17:28
 * desc   : <p>网络状态监听回调</p>
 */
public interface OnNetworkStatusChangedListener {
    void onNetDisconnected();

    void onNetConnected(NetworkType networkType);
}
