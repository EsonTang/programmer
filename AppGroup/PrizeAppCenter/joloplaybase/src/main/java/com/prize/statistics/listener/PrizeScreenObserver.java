
package com.prize.statistics.listener;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.TextUtils;

/**
 * PrizeScreenObserver
 * 锁屏监听
 * Created by long
 */
public class PrizeScreenObserver extends BroadcastReceiver {
    /**
     * LOG Tag
     */
    private static final String LOG_TAG = PrizeScreenObserver.class.getSimpleName();
    /**
     * Context
     */
    private Context mContext;
    /**
     * Is Registered
     */
    private boolean isRegistered;

    /**
     * NetworkListener
     */
    private IScreenOffListener mListener;
    /**
     * Constructor
     *
     * @param aContext  Context
     * @param aListener INetworkListener
     */
    public PrizeScreenObserver(Context aContext, IScreenOffListener aListener) {
        mContext = aContext;
        isRegistered = false;
        this.mListener=aListener;
    }

    /**
     * start,  onResume call
     */
    public void start() {

        if (isRegistered) {

            return;
        }
        try {
            mContext.registerReceiver(this, new IntentFilter(Intent.ACTION_SCREEN_OFF));

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

    @Override
    public void onReceive(Context aContext, Intent aIntent) {
        if(aIntent!=null&& !TextUtils.isEmpty(aIntent.getAction())&&Intent.ACTION_SCREEN_OFF.equals(aIntent.getAction())){
            if (mListener != null) {
                mListener.onIScreenOff(aContext);
            }

        }

    }

    /**
     * IScreenOffListener
     */
    public interface IScreenOffListener {
        /**
         * @param aContext Context
         */
        void onIScreenOff(Context aContext);

    }
}
