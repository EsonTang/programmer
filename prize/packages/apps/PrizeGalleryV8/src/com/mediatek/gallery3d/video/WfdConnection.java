package com.mediatek.gallery3d.video;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.display.DisplayManager;
import android.hardware.display.WifiDisplayStatus;
import android.provider.Settings;
import android.view.View;
import android.widget.Toast;

import com.android.gallery3d.app.Log;
import com.android.gallery3d.R;

public class WfdConnection extends RemoteConnection {
    private static final String TAG = "Gallery2/VideoPlayer/WfdConnection";
    private static final int ROUTE_DISCONNECTED_DELAY = 1000;
    private static final int EXTENSION_MODE_LIST_START = 10;
    private static final int EXTENSION_MODE_LIST_END = 12;
    private static final int NORMAL_MODE = 1;
    private static final int EXTENSION_MODE = 2;
    private boolean mIsConnected;
    private int mCurrentMode;

    public WfdConnection(final Activity activity, final View rootView,
            final ConnectionEventListener eventListener, boolean isConnected) {
        super(activity,rootView,eventListener);
        Log.v(TAG, "WfdConnection construct");
        mCurrentMode = getCurrentPowerSavingMode();
        initConnection(isConnected);
    }

    @Override
    public void refreshConnection(boolean isConnected) {
        Log.v(TAG, "refreshConnection() isConnected= " + isConnected);
        initConnection(isConnected);
    }

    private void initConnection(boolean isConnected) {
        Log.v(TAG, "initConnection()");
        mIsConnected = isConnected;
        entreExtensionIfneed();
        registerReceiver();
    }

    private int getCurrentPowerSavingMode() {
        int mCurrentMode = NORMAL_MODE;
        if (isExtensionFeatureOn()) {
            mCurrentMode = EXTENSION_MODE;
        }
        Log.v(TAG, "getCurrentPowerSavingMode()= " + mCurrentMode);
        return mCurrentMode;
    }

    @Override
    public boolean isConnected() {
        Log.v(TAG, "isConnected()= " + mIsConnected);
        return mIsConnected;
    }

    @Override
    public boolean isInExtensionDisplay() {
        boolean isExtensionDisplay = mIsConnected && mCurrentMode == EXTENSION_MODE;
        Log.v(TAG, "isInExtensionDisplay()= " + isExtensionDisplay);
        return isExtensionDisplay;
    }

    @Override
    protected void entreExtensionIfneed() {
        Log.v(TAG, "entreExtensionIfneed() mIsConnected= " + mIsConnected
                + " mCurrentMode= " + mCurrentMode);
        if (mIsConnected && mCurrentMode == EXTENSION_MODE) {
            mOnEventListener.onEvent(ConnectionEventListener.EVENT_CONTINUE_PLAY);
            enterExtensionMode();
        }
    }

    @Override
    public void doRelease() {
        Log.v(TAG, "doRelease()");
        unRegisterReceiver();
        if (isInExtensionDisplay()) {
            dismissPresentation();
            mHandler.removeCallbacks(mSelectMediaRouteRunnable);
            mHandler.removeCallbacks(mUnselectMediaRouteRunnable);
        }
    }

    private boolean isExtensionFeatureOn() {
        int mode = Settings.Global.getInt(mContext.getContentResolver(),
                Settings.Global.WIFI_DISPLAY_POWER_SAVING_OPTION, 0);
        Log.v(TAG, "getPowerSavingMode() mode = " + mode);
        if ((mode >= EXTENSION_MODE_LIST_START)
                && (mode <= EXTENSION_MODE_LIST_END)) {
            return true;
        }
        return false;
    }

    private void registerReceiver() {
        Log.v(TAG, "registerReceiver");
        IntentFilter filter = new IntentFilter(
                DisplayManager.ACTION_WIFI_DISPLAY_STATUS_CHANGED);
        mContext.registerReceiver(mWfdReceiver, filter);
    }

    private void unRegisterReceiver() {
        Log.v(TAG, "unRegisterReceiver");
        mContext.unregisterReceiver(mWfdReceiver);
    }

    private void leaveExtensionMode() {
        Log.v(TAG, "leaveExtensionMode()");
        mHandler.removeCallbacks(mUnselectMediaRouteRunnable);
        //wait 500ms for releasing route connection
        mHandler.postDelayed(mUnselectMediaRouteRunnable,
                ROUTE_DISCONNECTED_DELAY);
        mOnEventListener.onEvent(ConnectionEventListener.EVENT_FINISH_NOW);
    }

    private void enterExtensionMode() {
        Log.v(TAG, "enterExtensionMode()");
        mHandler.removeCallbacks(mSelectMediaRouteRunnable);
        mHandler.post(mSelectMediaRouteRunnable);
    }

    private void disConnect() {
        Log.v(TAG, "disConnect()");
        Toast.makeText(mContext.getApplicationContext(),
                mContext.getString(R.string.wfd_disconnected),
                Toast.LENGTH_LONG).show();
        mOnEventListener.onEvent(ConnectionEventListener.EVENT_END_POWERSAVING);
        if (isInExtensionDisplay()) {
            leaveExtensionMode();
        }
    }

    private BroadcastReceiver mWfdReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.v(TAG, "mWfdReceiver onReceive action: " + action);
            if (action != null
                    && action
                            .equals(DisplayManager.ACTION_WIFI_DISPLAY_STATUS_CHANGED)) {
                WifiDisplayStatus status = (WifiDisplayStatus) intent
                        .getParcelableExtra(DisplayManager.EXTRA_WIFI_DISPLAY_STATUS);
                if (status != null) {
                    int state = status.getActiveDisplayState();
                    Log.v(TAG, "mWfdReceiver onReceive wfd State: "
                                + state + " mIsConnected=" + mIsConnected);
                    if (state == WifiDisplayStatus.DISPLAY_STATE_NOT_CONNECTED && mIsConnected) {
                        disConnect();
                        mIsConnected = false;
                    } else if (state == WifiDisplayStatus.DISPLAY_STATE_CONNECTED) {
                        mIsConnected = true;
                        entreExtensionIfneed();
                    }
                }
            }
        }
    };
}
