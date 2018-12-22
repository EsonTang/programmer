package com.android.dialer.prize.incallstatusbar;

import android.app.StatusBarManager;
import android.content.Context;
import android.content.Intent;
import android.telecom.TelecomManager;
import android.text.format.DateUtils;
import android.util.Log;

import com.android.dialer.R;
import com.android.incallui.CallTimer;
import com.android.incallui.InCallPresenter;

/**
 * Created by wangzhong on 2017/10/25.
 */
public class InCallStatusBarPresenter {

    public static final String TAG = "InCallStatusBarPresenter";
    private static final long CALL_TIME_UPDATE_INTERVAL_MS = 1000;

    private static InCallStatusBarPresenter inCallStatusBarPresenter;

    private StatusBarManager mStatusBarManager;
    private CallTimer mCallTimer;
    private boolean isInCalling = false;
    private long mCallStartTime;

    public static InCallStatusBarPresenter getInstance() {
        if (null == inCallStatusBarPresenter) {
            inCallStatusBarPresenter = new InCallStatusBarPresenter();
        }
        return inCallStatusBarPresenter;
    }

    private boolean isInCall(Context context) {
        TelecomManager tm = (TelecomManager) context.getSystemService(Context.TELECOM_SERVICE);
        if (null != tm && tm.isInCall()) {
            return true;
        }
        return false;
    }

    private StatusBarManager getStatusBarManager(Context context) {
        if (null == mStatusBarManager) {
            mStatusBarManager = (StatusBarManager) context.getSystemService(Context.STATUS_BAR_SERVICE);
        }
        return mStatusBarManager;
    }

    /**
     * Remove the StatusBar status temporarily.
     *
     * @param context
     */
    public void removeInCallStatusBar(Context context) {
        getStatusBarManager(context).clearStatusBarBackgroundColor();
    }

    /**
     * Clear the StatusBar status.
     *
     * @param context
     */
    public void clearInCallStatusBar(Context context) {
        removeInCallStatusBar(context);
        cancelCallTimer();
        isInCalling = false;
        mCallStartTime = 0;
    }

    /**
     * Show the incall StatusBar status.
     *
     * @param context
     */
    public void showInCallStatusBar(Context context) {
        if (isInCall(context)) {
            getStatusBarManager(context).setStatusBarBackgroundColor(context.getResources().getColor(R.color.prize_incallwindow_bg_color));
            getStatusBarManager(context).setStatusBarBackgroundClickEvent(InCallPresenter.getInstance().getInCallIntent(false, false));
            if (!isInCalling) {
                setInCallStatusBarText(context, context.getResources().getString(R.string.prize_incallwindow_outgoing));
            } else {
                updateInCallStatusBarTime(context);
            }
        }
    }

    private void setInCallStatusBarText(Context context, String tag) {
        getStatusBarManager(context).setStatusBarText(tag);
    }

    /**
     * Start counting.
     *
     * @param context
     * @param callStartTime
     */
    public void initCallTimer(Context context, long callStartTime) {
        isInCalling = true;
        mCallStartTime = callStartTime;
        if (mCallTimer == null) {
            mCallTimer = new CallTimer(new Runnable() {
                @Override
                public void run() {
                    updateInCallStatusBarTime(context);
                }
            });
        }
        mCallTimer.start(CALL_TIME_UPDATE_INTERVAL_MS);
    }

    private void updateInCallStatusBarTime(Context context) {
        if (isInCall(context)) {
            long duration = System.currentTimeMillis() - mCallStartTime;
            /* PRIZE InCallUI zhoushuanghua add for bug 59559 <2018_05_24> start */
            boolean hasSystemFeature = context.getPackageManager().hasSystemFeature("com.prize.notch.screen");
            String incallString = context.getResources().getString(R.string.prize_incallwindow_incall);
            if(hasSystemFeature){
                incallString = context.getResources().getString(R.string.prize_notch_incallwindow_incall);
            }
            setInCallStatusBarText(context, incallString
                    + DateUtils.formatElapsedTime(duration / 1000)); 
            /* PRIZE InCallUI zhoushuanghua add for bug 59559 <2018_05_24> end */
        }
    }

    private void cancelCallTimer() {
        if (null != mCallTimer) {
            mCallTimer.cancel();
            mCallTimer = null;
        }
    }
}
