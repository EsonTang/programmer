package com.mediatek.gallery3d.video;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaRouter;
import android.view.View;
import android.view.Display;

import com.android.gallery3d.app.Log;

public class MhlConnection extends RemoteConnection {
    private static final String TAG = "Gallery2/VideoPlayer/MhlConnection";
    private boolean mIsConnected;

    private static final int MHL_DISPLAY_STATE_CONNECTED = 1;
    private static final int MHL_DISPLAY_STATE_NOT_CONNECTED = 0;
    private static final int ROUTE_DISCONNECTED_DELAY = 1000;

    public MhlConnection(final Activity activity, final View rootView,
            final ConnectionEventListener eventListener) {
        super(activity,rootView,eventListener);
        Log.v(TAG, "MhlConnection construct");
        mIsConnected = false;
        entreExtensionIfneed();
    }

    @Override
    public void refreshConnection(boolean isConnected) {
        Log.v(TAG, "refreshConnection()");
        entreExtensionIfneed();
    }

    @Override
    public boolean isConnected() {
        Log.v(TAG, "isConnected(): " + mIsConnected);
        return mIsConnected;
    }

    @Override
    public boolean isInExtensionDisplay() {
        Log.v(TAG, "isExtension(): " + mIsConnected);
        return mIsConnected;
    }

    @Override
    protected void entreExtensionIfneed() {
        Log.v(TAG, "entreExtensionIfneed()");
        registerReceiver();
    }

    @Override
    public void doRelease() {
        Log.v(TAG, "doRelease()");
        unRegisterReceiver();
        dismissPresentation();
        mHandler.removeCallbacks(mSelectMediaRouteRunnable);
        mHandler.removeCallbacks(mUnselectMediaRouteRunnable);
    }

    private void registerReceiver() {
        Log.v(TAG, "registerMhlReceiver()");
        IntentFilter mhlFilter = new IntentFilter(Intent.ACTION_HDMI_PLUG);
        mContext.registerReceiver(mMhlReceiver, mhlFilter);
    }

    private void unRegisterReceiver() {
        Log.v(TAG, "unregisterMhlReceiver()");
        mContext.unregisterReceiver(mMhlReceiver);
    }

    private void leaveExtensionMode() {
        Log.v(TAG, "leaveMhlExtensionMode()");
        mHandler.removeCallbacks(mUnselectMediaRouteRunnable);
        //wait 500ms for releasing route connection
        mHandler.postDelayed(mUnselectMediaRouteRunnable,
                ROUTE_DISCONNECTED_DELAY);
    }

    private void enterExtensionMode() {
        Log.v(TAG, "enterMhlExtensionMode()");
        mHandler.removeCallbacks(mSelectMediaRouteRunnable);
        mHandler.post(mSelectMediaRouteRunnable);
    }

    private void connected() {
        Log.v(TAG, "connected()");
        mIsConnected = true;
        mOnEventListener.onEvent(ConnectionEventListener.EVENT_CONTINUE_PLAY);
        enterExtensionMode();
    }

    private void disConnected() {
        Log.v(TAG, "disConnected()");
        mOnEventListener.onEvent(ConnectionEventListener.EVENT_STAY_PAUSE);
        mOnEventListener.onEvent(ConnectionEventListener.EVENT_END_POWERSAVING);
        leaveExtensionMode();
        mIsConnected = false;
    }

    private BroadcastReceiver mMhlReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.v(TAG, "mMhlReceiver onReceive action: " + action);
            if (action != null && action.equals(Intent.ACTION_HDMI_PLUG)) {
                int state = intent.getIntExtra("state",
                        MHL_DISPLAY_STATE_NOT_CONNECTED);
                Log.v(TAG, "MHL state = " + state);
                if (state == MHL_DISPLAY_STATE_NOT_CONNECTED) {
                    disConnected();
                } else if (state == MHL_DISPLAY_STATE_CONNECTED) {
                    connected();
                }
            }
        }
    };
}
