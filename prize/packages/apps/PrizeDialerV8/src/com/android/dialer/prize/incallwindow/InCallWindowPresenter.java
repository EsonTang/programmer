package com.android.dialer.prize.incallwindow;

import android.app.StatusBarManager;
import android.content.Context;
import android.content.Intent;
import android.telecom.TelecomManager;
import android.util.Log;

import com.android.dialer.R;
import com.android.incallui.CallTimer;
import com.android.incallui.InCallPresenter;

/**
 * Created by wangzhong on 2017/8/19.
 */
public class InCallWindowPresenter {

    public static final String TAG = "InCallWindowPresenter";

    private static final long CALL_TIME_UPDATE_INTERVAL_MS = 1000;

    private static InCallWindowPresenter inCallWindowPresenter;

    private boolean isInCalling = false;
    private CallTimer mCallTimer;
    private long mCallStartTime;
    private StatusBarManager mStatusBarManager;

    private InCallWindowPresenter() {

    }

    public boolean isInCalling() {
        return isInCalling;
    }

    public void setInCalling(boolean inCalling) {
        isInCalling = inCalling;
    }

    private long getCallStartTime() {
        return mCallStartTime;
    }

    public void setCallStartTime(long callStartTime) {
        mCallStartTime = callStartTime;
    }

    private StatusBarManager getStatusBarManager(Context context) {
        if (null == mStatusBarManager) {
            mStatusBarManager = (StatusBarManager) context.getSystemService(Context.STATUS_BAR_SERVICE);
        }
        return mStatusBarManager;
    }

    public static InCallWindowPresenter getInstance() {
        if (null == inCallWindowPresenter) {
            inCallWindowPresenter = new InCallWindowPresenter();
        }
        return inCallWindowPresenter;
    }

    private boolean isInCall(Context context) {
        TelecomManager tm = (TelecomManager) context.getSystemService(Context.TELECOM_SERVICE);
        if (null != tm && tm.isInCall()) {
            return true;
        }
        return false;
    }

    public void showInCallWindow(Context context) {
        if (isInCall(context)) {
            InCallWindowManager.createNotificationWindow(context);
            if (isInCalling()) {
                updateInCallWindowTime(context);
            }
            //setStatusBarBackground(context);
        }
    }

    public void removeInCallWindow(Context context) {
        clearStatusBarBackground(context);
        /*if (isInCall(context) && InCallWindowManager.isInCallWindowVisible()) {*/
        if (InCallWindowManager.isInCallWindowVisible()) {
            InCallWindowManager.removeNotificationWindow(context);
        }
    }

    /**
     * Clear all status.
     * @param context
     */
    public void deleteInCallWindow(Context context) {
        setInCalling(false);
        removeInCallWindow(context);
        cancelCallTimer();
    }

    /**
     * Call Timer.
     */
    public void initCallTimer(Context context, long callStartTime) {
        Log.d(TAG, "InCallWindow  initCallTimer()");
        setCallStartTime(callStartTime);
        setInCalling(true);
        if (mCallTimer == null) {
            //mContext = context;
            mCallTimer = new CallTimer(new Runnable() {
                @Override
                public void run() {
                    updateInCallWindowTime(context);
                }
            });
        }
        mCallTimer.start(CALL_TIME_UPDATE_INTERVAL_MS);
    }

    public void cancelCallTimer() {
        if (null != mCallTimer) {
            mCallTimer.cancel();
        }
    }

    /**
     * update the call time.
     * @param context
     */
    private void updateInCallWindowTime(Context context) {
        if (isInCall(context)) {
            long duration = System.currentTimeMillis() - getCallStartTime();
            Log.d(TAG, "InCallWindow  updateInCallWindowTime()");
            InCallWindowManager.updateInCallWindow(context, duration);
        }
    }

    /**
     * Set the StatusBar background.
     * @param context
     */
    public void setStatusBarBackground(Context context) {
        getStatusBarManager(context).setStatusBarBackgroundColor(context.getResources().getColor(R.color.prize_incallwindow_bg_color));
        getStatusBarManager(context).setStatusBarBackgroundClickEvent(InCallPresenter.getInstance().getInCallIntent(false, false));
    }

    /**
     * Clear the StatusBar background.
     * @param context
     */
    private void clearStatusBarBackground(Context context) {
        getStatusBarManager(context).clearStatusBarBackgroundColor();
    }

}
