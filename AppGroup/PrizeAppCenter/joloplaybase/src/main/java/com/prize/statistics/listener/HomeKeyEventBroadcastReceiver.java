package com.prize.statistics.listener;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.prize.app.util.JLog;

/**
 * Home键监听
 * @创建者 longbaoxiu
 * @创建者 2017/7/5.15:42
 * @描述
 */

public class HomeKeyEventBroadcastReceiver extends BroadcastReceiver {
    private static final String SYSTEM_EVENT_REASON = "reason";
    private static final String SYSTEM_HOME_KEY = "homekey";
    private static final String SYSTEM_RECENT_APPS = "recentapps";
    /**
     * Context
     */
    private Context mContext;
    private HomeKeyListener listener;
    /**
     * Is Registered
     */
    private boolean isRegistered;

    public HomeKeyEventBroadcastReceiver(Context aContext, HomeKeyListener l) {
        listener = l;
        mContext = aContext;
        listener = l;
        isRegistered = false;
    }

    /**
     * This method is called when the BroadcastReceiver is receiving an Intent
     * broadcast.  During this time you can use the other methods on
     * BroadcastReceiver to view/modify the current result values.  This method
     * is always called within the main thread of its process, unless you
     * explicitly asked for it to be scheduled on a different thread using
     * thread you should
     * never perform long-running operations in it (there is a timeout of
     * 10 seconds that the system allows before considering the receiver to
     * be blocked and a candidate to be killed). You cannot launch a popup dialog
     * in your implementation of onReceive().
     * <p>
     * <p><b>If this BroadcastReceiver was launched through a &lt;receiver&gt; tag,
     * then the object is no longer alive after returning from this
     * function.</b>  This means you should not perform any operations that
     * return a result to you asynchronously -- in particular, for interacting
     * with services, you should use
     * instead of
     * If you wish
     * to interact with a service that is already running, you can use
     * {@link #peekService}.
     * <p>
     * <p>The Intent filters used in {@link Context#registerReceiver}
     * and in application manifests are <em>not</em> guaranteed to be exclusive. They
     * are hints to the operating system about how to find suitable recipients. It is
     * possible for senders to force delivery to specific recipients, bypassing filter
     * resolution.  For this reason, {@link #onReceive(Context, Intent) onReceive()}
     * implementations should respond only to known actions, ignoring any unexpected
     * Intents that they may receive.
     *
     * @param context The Context in which the receiver is running.
     * @param intent  The Intent being received.
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        if (null == intent) {
            return;
        }
        String action = intent.getAction();
        if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
            String reason = intent.getStringExtra(SYSTEM_EVENT_REASON);
            JLog.i("PrizeStaticsManagerImpl", "reason=" + reason);
            if (null != reason) {
                if (reason.equals(SYSTEM_HOME_KEY)) {
                    //Home key short pressed.
                    if (null != listener) {
                        listener.onHomeKeyShortPressed();
                    }
                } else if (reason.equals(SYSTEM_RECENT_APPS)) {
                    //Home key long pressed.
                    if (null != listener) {
                        listener.onHomeKeyLongPressed();
                    }
                }
            }
        }
    }

    /**
     * start,  onResume call
     */
    public void start() {

        if (isRegistered) {

            return;
        }
        try {
            mContext.registerReceiver(this, new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
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

    public interface HomeKeyListener {
         void onHomeKeyShortPressed();

        void onHomeKeyLongPressed();
    }
}
