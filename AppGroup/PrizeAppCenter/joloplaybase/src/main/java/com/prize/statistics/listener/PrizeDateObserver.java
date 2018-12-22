
package com.prize.statistics.listener;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.TextUtils;

import com.prize.app.util.JLog;


/**PrizeDateObserver
 *日期变化监听
 */
public class PrizeDateObserver extends BroadcastReceiver {
    /**
     * LOG Tag
     */
    private static final String LOG_TAG = PrizeDateObserver.class.getSimpleName();
    /**
     * Context
     */
    private Context mContext;
    /**
     * IDatekListener
     */
    private IDatekListener mListener;
    /**
     * Is Registered
     */
    private boolean isRegistered;


    /**
     * Constructor
     *
     * @param aContext  Context
     * @param aListener INetworkListener
     */
    public PrizeDateObserver(Context aContext, IDatekListener aListener) {
        mContext = aContext;
        mListener = aListener;
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
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_TIME_CHANGED);
//            filter.addAction(Intent.ACTION_DATE_CHANGED);
            mContext.registerReceiver(this, filter);
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
        JLog.i("PRIZE2016", "PrizeDateObserver-onReceive-aIntent.getAction()="+aIntent.getAction());
        if (TextUtils.equals(aIntent.getAction(), Intent.ACTION_TIME_CHANGED)) {
                if (mListener != null) {
                    mListener.onDateChange(aContext);
                }
            }
        }

    /**
     * INetworkListener
     */
    public interface IDatekListener {
        /**
         * Connected(
         *
         * @param aContext Context
         */
        void onDateChange(Context aContext);

    }
}
