
package com.prize.music.admanager.statistics.listener;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;

/**
 * PrizeNetworkObserver
 * 网络变化监听
 * Created by long
 */
public class NetworkObserver extends BroadcastReceiver {
    /**
     * LOG Tag
     */
    private static final String LOG_TAG = NetworkObserver.class.getSimpleName();
    /**
     * Context
     */
    private Context mContext;
    /**
     * NetworkListener
     */
    private INetworkListener mListener;
    /**
     * IsNetworkAvailable
     */
    private boolean mIsNetworkAvailable;
    /**
     * Is Registered
     */
    private boolean isRegistered;

    private static NetworkInfo.State currentWifi = NetworkInfo.State.UNKNOWN;
    private static NetworkInfo.State currentMobile = NetworkInfo.State.UNKNOWN;

    /**
     * Constructor
     *
     * @param aContext  Context
     * @param aListener INetworkListener
     */
    public NetworkObserver(Context aContext, INetworkListener aListener) {
        mContext = aContext;
        mListener = aListener;
        mIsNetworkAvailable = false;
        isRegistered = false;
    }

    /**
     * start,  onResume call
     */
    public void start() {

        if (isRegistered) {

            return;
        }
        try {
            mContext.registerReceiver(this, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

            isRegistered = true;
        } catch (Exception e) {
            isRegistered = false;
        }
    }

    /**
     * stop， onPause call
     */
    public void stop() {

        if (!isRegistered) {

            return;
        }
        try {
            mContext.unregisterReceiver(this);
            isRegistered = false;
        } catch (Exception e) {
            isRegistered = true;
        }
    }

    /**
     * Indicates whether network connectivity is possible.
     *
     * @param aContext Context
     * @return true if the network is available, false otherwise
     */
    public boolean isNetworkAvailable(Context aContext) {
        ConnectivityManager cm = (ConnectivityManager) aContext
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        return (info != null && info.isAvailable());
    }

    @Override
    public void onReceive(Context aContext, Intent aIntent) {

        if (TextUtils.equals(aIntent.getAction(), ConnectivityManager.CONNECTIVITY_ACTION)) {
            boolean isAvailable = isNetworkAvailable(aContext);
            ConnectivityManager manager = (ConnectivityManager) aContext
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo info = manager
                    .getNetworkInfo(ConnectivityManager.TYPE_WIFI);

            NetworkInfo.State wifi = (info != null) ? info.getState() : NetworkInfo.State.DISCONNECTED;
            info = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            NetworkInfo.State mobile = (info != null) ? info.getState() : NetworkInfo.State.DISCONNECTED;
            if (isAvailable) {
                if (!mIsNetworkAvailable && mListener != null) {
                    // 判断网络状态
                    if ((currentWifi == wifi) && (currentMobile == mobile))
                        // 两者都没变化
                        return;
                    mListener.onNetworkConnected(aContext);
                }
            } else {
                if (mListener != null) {
                    mListener.onNetworkUnConnected(aContext);
                }
            }
            mIsNetworkAvailable = isAvailable;

            currentWifi = wifi;
            currentMobile = mobile;

        }
    }

    /**
     * INetworkListener
     */
    public interface INetworkListener {
        /**
         * Connected(
         *
         * @param aContext Context
         */
        void onNetworkConnected(Context aContext);


        /**
         * UnConnected
         *
         * @param aContext
         */
        void onNetworkUnConnected(Context aContext);
    }
}
