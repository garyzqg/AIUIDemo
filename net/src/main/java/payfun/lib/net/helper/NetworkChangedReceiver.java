package payfun.lib.net.helper;

import static android.Manifest.permission.ACCESS_NETWORK_STATE;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;

import androidx.annotation.RequiresPermission;

import java.util.HashSet;
import java.util.Set;

import payfun.lib.basis.utils.InitUtil;
import payfun.lib.basis.utils.ThreadUtil;

/**
 * @author : 时光
 * e-mail : qurongzhen@pay.media
 * date   : 2022/8/10 17:27
 * desc   : <p>网络状态监听
 */
public class NetworkChangedReceiver extends BroadcastReceiver {
    private static class LazyHolder {
        private static final NetworkChangedReceiver INSTANCE = new NetworkChangedReceiver();
    }

    public synchronized static NetworkChangedReceiver getInstance() {
        return LazyHolder.INSTANCE;
    }

    private NetworkType mType;
    private Set<OnNetworkStatusChangedListener> mListeners = new HashSet<>();

    @RequiresPermission(ACCESS_NETWORK_STATE)
    void registerListener(final OnNetworkStatusChangedListener listener) {
        if (listener == null) {
            return;
        }
        ThreadUtil.runOnUiThread(new Runnable() {
            @Override
            @RequiresPermission(ACCESS_NETWORK_STATE)
            public void run() {
                int preSize = mListeners.size();
                mListeners.add(listener);
                if (preSize == 0 && mListeners.size() == 1) {
                    mType = NetHelper.getNetworkType();
                    IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
                    InitUtil.getAppContext().registerReceiver(NetworkChangedReceiver.getInstance(), intentFilter);
                }
            }
        });
    }

    boolean isRegistered(final OnNetworkStatusChangedListener listener) {
        if (listener == null) {
            return false;
        }
        return mListeners.contains(listener);
    }

    void unregisterListener(final OnNetworkStatusChangedListener listener) {
        if (listener == null) {
            return;
        }
        ThreadUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int preSize = mListeners.size();
                mListeners.remove(listener);
                if (preSize == 1 && mListeners.size() == 0) {
                    InitUtil.getAppContext().unregisterReceiver(NetworkChangedReceiver.getInstance());
                }
            }
        });
    }


    @Override
    public void onReceive(Context context, Intent intent) {
        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
            // debouncing
            ThreadUtil.runOnUiThreadDelayed(new Runnable() {
                @Override
                @RequiresPermission(ACCESS_NETWORK_STATE)
                public void run() {
                    NetworkType networkType = NetHelper.getNetworkType();
                    if (mType == networkType) {
                        return;
                    }
                    mType = networkType;
                    if (networkType == NetworkType.NETWORK_NO) {
                        for (OnNetworkStatusChangedListener listener : mListeners) {
                            listener.onNetDisconnected();
                        }
                    } else {
                        for (OnNetworkStatusChangedListener listener : mListeners) {
                            listener.onNetConnected(networkType);
                        }
                    }
                }
            }, 1000);
        }
    }
}
