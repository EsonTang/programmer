/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.systemui.statusbar.policy;

import android.content.Context;
import android.content.Intent;
import android.net.NetworkCapabilities;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;

import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.AsyncChannel;
import com.android.settingslib.wifi.WifiStatusTracker;
import com.android.systemui.statusbar.policy.NetworkController.IconState;
import com.android.systemui.statusbar.policy.NetworkController.SignalCallback;
import com.mediatek.systemui.ext.IMobileIconExt;
import com.mediatek.systemui.statusbar.util.FeatureOptions;
import com.mediatek.systemui.PluginManager;

import java.util.BitSet;
import com.android.systemui.R;

import java.util.Objects;
//add for statusbar inverse. prize-linkh-20150903
import com.mediatek.common.prizeoption.PrizeOption;
import com.android.systemui.statusbar.phone.PrizeStatusBarStyleListener;
import android.app.StatusBarManager;


public class WifiSignalController extends
        SignalController<WifiSignalController.WifiState, SignalController.IconGroup> implements PrizeStatusBarStyleListener{
    private static final String TAG = "WifiSignalController";

    private final WifiManager mWifiManager;
    private final AsyncChannel mWifiChannel;
    private final boolean mHasMobileData;

    private final WifiStatusTracker mWifiTracker;
    /// M: Add for plug in @ {
    private IMobileIconExt mMobileIconExt;
    // @ }
    public WifiSignalController(Context context, boolean hasMobileData,
            CallbackHandler callbackHandler, NetworkControllerImpl networkController) {
        super("WifiSignalController", context, NetworkCapabilities.TRANSPORT_WIFI,
                callbackHandler, networkController);
        mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        mWifiTracker = new WifiStatusTracker(mWifiManager);
        mHasMobileData = hasMobileData;
        Handler handler = new WifiHandler();
        mWifiChannel = new AsyncChannel();
        Messenger wifiMessenger = mWifiManager.getWifiServiceMessenger();
        if (wifiMessenger != null) {
            mWifiChannel.connect(context, handler, wifiMessenger);
        }
        // WiFi only has one state.
        mCurrentState.iconGroup = mLastState.iconGroup = new IconGroup(
                "Wi-Fi Icons",
                WifiIcons.WIFI_SIGNAL_STRENGTH,
                WifiIcons.QS_WIFI_SIGNAL_STRENGTH,
                AccessibilityContentDescriptions.WIFI_CONNECTION_STRENGTH,
                WifiIcons.WIFI_NO_NETWORK,
                WifiIcons.QS_WIFI_NO_NETWORK,
                WifiIcons.WIFI_NO_NETWORK,
                WifiIcons.QS_WIFI_NO_NETWORK,
                AccessibilityContentDescriptions.WIFI_NO_CONNECTION
                );
        /// M: Init plugin @ {
        mMobileIconExt = PluginManager.getMobileIconExt(context);
        /// @ }
		/*PRIZE-init mWifiActivity-liufan-2016-04-20-start*/
        mWifiActivity = WifiManager.DATA_ACTIVITY_NONE;
		/*PRIZE-init mWifiActivity-liufan-2016-04-20-end*/
    }

    @Override
    protected WifiState cleanState() {
        return new WifiState();
    }

	/*PRIZE-PrizeStatusBarStyleListener callback-liufan-2016-04-20-start*/
    private int mCurStatusBarStyle = StatusBarManager.STATUS_BAR_INVERSE_DEFALUT;
    @Override
    public void onStatusBarStyleChanged(int style) {
        Log.d(TAG, "onStatusBarStyleChanged(). curStyle=" + mCurStatusBarStyle + ", newStyle=" + style);
        if(mCurStatusBarStyle != style) {
            mCurStatusBarStyle = style;
			
        }
    }
	/*PRIZE-PrizeStatusBarStyleListener callback-liufan-2016-04-20-end*/
	
    @Override
    public void notifyListeners(SignalCallback callback) {
        // only show wifi in the cluster if connected or if wifi-only
        boolean wifiVisible = mCurrentState.enabled
                && (mCurrentState.connected || !mHasMobileData);
        String wifiDesc = wifiVisible ? mCurrentState.ssid : null;
        boolean ssidPresent = wifiVisible && mCurrentState.ssid != null;
        String contentDescription = getStringIfExists(getContentDescription());
        if (mCurrentState.inetCondition == 0) {
            contentDescription +=
                    ("," + mContext.getString(R.string.accessibility_quick_settings_no_internet));
        }
		/*PRIZE-add statusIconGray-liufan-2016-04-20-start*/
        IconState statusIcon = new IconState(wifiVisible, WifiIcons.WIFI_SIGNAL_STRENGTH_PRIZE[mCurrentState.level], contentDescription);
        IconState statusIconGray = new IconState(wifiVisible, WifiIcons.getWifiSignalStrengthIcon(1, mCurrentState.level), contentDescription);
        IconState qsIcon = new IconState(mCurrentState.connected, getQsCurrentIconId(),
                contentDescription);
        callback.setWifiIndicators(mCurrentState.enabled, statusIcon, statusIconGray, qsIcon, mWifiActivity,
                ssidPresent && mCurrentState.activityIn, ssidPresent && mCurrentState.activityOut,
                wifiDesc);
		/*PRIZE-add statusIconGray-liufan-2016-04-20-end*/
    }

    public void notifyListenersForInverse() {
        // only show wifi in the cluster if connected or if wifi-only
        boolean wifiVisible = mCurrentState.enabled
                && (mCurrentState.connected || !mHasMobileData);
        String wifiDesc = wifiVisible ? mCurrentState.ssid : null;
        boolean ssidPresent = wifiVisible && mCurrentState.ssid != null;
        String contentDescription = getStringIfExists(getContentDescription());
        if (mCurrentState.inetCondition == 0) {
            contentDescription +=
                    ("," + mContext.getString(R.string.accessibility_quick_settings_no_internet));
        }
		/*PRIZE-add statusIconGray-liufan-2016-04-20-start*/
        IconState statusIcon = new IconState(wifiVisible, WifiIcons.WIFI_SIGNAL_STRENGTH_PRIZE[mCurrentState.level], contentDescription);
        IconState statusIconGray = new IconState(wifiVisible, WifiIcons.getWifiSignalStrengthIcon(1, mCurrentState.level), contentDescription);
        IconState qsIcon = new IconState(mCurrentState.connected, getQsCurrentIconId(),
                contentDescription);
        mCallbackHandler.setWifiIndicatorsForInverse(mCurrentState.enabled, statusIcon, statusIconGray, qsIcon, mWifiActivity,
                ssidPresent && mCurrentState.activityIn, ssidPresent && mCurrentState.activityOut,
                wifiDesc);
		/*PRIZE-add statusIconGray-liufan-2016-04-20-end*/
    }

    /**
     * Extract wifi state directly from broadcasts about changes in wifi state.
     */
    public void handleBroadcast(Intent intent) {
        mWifiTracker.handleBroadcast(intent);
        mCurrentState.enabled = mWifiTracker.enabled;
        mCurrentState.connected = mWifiTracker.connected;
        mCurrentState.ssid = mWifiTracker.ssid;
        mCurrentState.rssi = mWifiTracker.rssi;
        mCurrentState.level = mWifiTracker.level;
		/*PRIZE-wifi-liufan-2016-04-20-start*/
        if(mCurrentState.level != mLastWifiLevel){ mWifiLevelChanged =true; }
        else { mWifiLevelChanged = false;}
        mLastWifiLevel = mCurrentState.level;
		/*PRIZE-wifi-liufan-2016-04-20-end*/
        notifyListenersIfNecessary();
    }

    /*PRIZE -wifi icons- liyao 20150615 start*/
    private int mWifiActivity;
    static boolean mWifiLevelChanged = false;
    static boolean mWifiActivityChanged = false ;
    static int mLastWifiLevel = -1;
    static int mLastWifiActivity = -1;
    /*PRIZE -wifi icons- liyao 20150615 end*/
    @VisibleForTesting
    void setActivity(int wifiActivity) {
        mCurrentState.activityIn = wifiActivity == WifiManager.DATA_ACTIVITY_INOUT
                || wifiActivity == WifiManager.DATA_ACTIVITY_IN;
        mCurrentState.activityOut = wifiActivity == WifiManager.DATA_ACTIVITY_INOUT
                || wifiActivity == WifiManager.DATA_ACTIVITY_OUT;
		/*PRIZE-wifi-liufan-2016-04-20-start*/
		mWifiActivity = wifiActivity;
        if(mWifiActivity != mLastWifiActivity){mWifiActivityChanged = true;}
        else {mWifiActivityChanged = false;}
        mLastWifiActivity = mWifiActivity;
		/*PRIZE-wifi-liufan-2016-04-20-end*/
        notifyListenersIfNecessary();
    }

    /**
     * Handler to receive the data activity on wifi.
     */
    private class WifiHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case AsyncChannel.CMD_CHANNEL_HALF_CONNECTED:
                    if (msg.arg1 == AsyncChannel.STATUS_SUCCESSFUL) {
                        mWifiChannel.sendMessage(Message.obtain(this,
                                AsyncChannel.CMD_CHANNEL_FULL_CONNECTION));
                    } else {
                        Log.e(mTag, "Failed to connect to wifi");
                    }
                    break;
                case WifiManager.DATA_ACTIVITY_NOTIFICATION:
                    setActivity(msg.arg1);
                    break;
                default:
                    // Ignore
                    break;
            }
        }
    }

    static class WifiState extends SignalController.State {
        String ssid;

        @Override
        public void copyFrom(State s) {
            super.copyFrom(s);
            WifiState state = (WifiState) s;
            ssid = state.ssid;
        }

        @Override
        protected void toString(StringBuilder builder) {
            super.toString(builder);
            builder.append(',').append("ssid=").append(ssid);
        }

        @Override
        public boolean equals(Object o) {
            return super.equals(o)
                    && Objects.equals(((WifiState) o).ssid, ssid);
        }
    }

    /// M: Disable inetCondition check as this condition is not sufficient in some cases.
    /// So always set it is in net with value 1. @ {
    @Override
    public void updateConnectivity(BitSet connectedTransports, BitSet validatedTransports) {
        // Override its parent methods, so keep check for migration
        mCurrentState.inetCondition = validatedTransports.get(mTransportType) ? 1 : 0;
        Log.d(TAG,"mCurrentState.inetCondition = " + mCurrentState.inetCondition);
        mCurrentState.inetCondition =
                mMobileIconExt.customizeWifiNetCondition(mCurrentState.inetCondition);
        notifyListenersIfNecessary();
    }

    /** Add for [WIFI StatusBar Active Icon].
     * Override to replace the icon if there is activity in / out.
     */
    @Override
    public int getCurrentIconId() {
        if (FeatureOptions.MTK_A1_SUPPORT) {
            return super.getCurrentIconId();
        }
        int iconId = super.getCurrentIconId();
        /// M: add "mCurrentState.connected" to avoid the probem that wifi icon showing when enable
        ///    wifi setting switch only
        /// M: for ALPS02828267
        if (mCurrentState.connected && (mCurrentState.activityIn || mCurrentState.activityOut)) {
            int type = getActiveType();
            if (type < WifiIcons.WIFI_SIGNAL_STRENGTH_INOUT[0].length) {
                iconId = WifiIcons.WIFI_SIGNAL_STRENGTH_INOUT[mCurrentState.level][type];
            }
        }
        return iconId;
    }

    /** Add for [WIFI StatusBar Active Icon].
     * Based on the activity type, to get relate icons.
     */
    private int getActiveType() {
        int type = WifiManager.DATA_ACTIVITY_NONE;
        if (mCurrentState.activityIn && mCurrentState.activityOut) {
            type = WifiManager.DATA_ACTIVITY_INOUT;
        } else if (mCurrentState.activityIn) {
            type = WifiManager.DATA_ACTIVITY_IN;
        } else if (mCurrentState.activityOut) {
            type = WifiManager.DATA_ACTIVITY_OUT;
        }
        return type;
    }
    /// @ }
}
