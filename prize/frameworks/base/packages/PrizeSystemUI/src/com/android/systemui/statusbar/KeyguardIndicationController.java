/*
 * Copyright (C) 2014 The Android Open Source Project
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
 * limitations under the License
 */

package com.android.systemui.statusbar;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Color;
import android.hardware.fingerprint.FingerprintManager;
import android.os.BatteryManager;
import android.os.BatteryStats;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.os.UserManager;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;

import com.android.internal.app.IBatteryStats;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.KeyguardUpdateMonitorCallback;
import com.android.systemui.R;
import com.android.systemui.statusbar.phone.KeyguardIndicationTextView;
import com.android.systemui.statusbar.phone.LockIcon;
import com.android.systemui.statusbar.phone.StatusBarKeyguardViewManager;

/*PRIZE-import package- yuhao-2016-08-25-start*/
import com.android.systemui.statusbar.phone.FeatureOption;
/*PRIZE-import package- yuhao-2016-08-25-end*/
/*PRIZE-import package vlife- liufan-2016-09-06-start*/
import com.android.systemui.statusbar.phone.NotificationPanelView;
/*PRIZE-import package vlife- liufan-2016-09-06-end*/
/*PRIZE-add for haokan-liufan-2017-10-25-start*/
import com.android.systemui.statusbar.phone.PhoneStatusBar;
import com.mediatek.common.prizeoption.PrizeOption;
/*PRIZE-add for haokan-liufan-2017-10-25-end*/

/**
 * Controls the indications and error messages shown on the Keyguard
 */
public class KeyguardIndicationController {

    private static final String TAG = "KeyguardIndication";
    private static final boolean DEBUG_CHARGING_SPEED = false;

    private static final int MSG_HIDE_TRANSIENT = 1;
    private static final int MSG_CLEAR_FP_MSG = 2;
    private static final long TRANSIENT_FP_ERROR_TIMEOUT = 1300;

    private final Context mContext;
    private final KeyguardIndicationTextView mTextView;
    private final UserManager mUserManager;
    private final IBatteryStats mBatteryInfo;

    private final int mSlowThreshold;
    private final int mFastThreshold;
    private final LockIcon mLockIcon;
    private StatusBarKeyguardViewManager mStatusBarKeyguardViewManager;

    private String mRestingIndication;
    private String mTransientIndication;
    private int mTransientTextColor;
    private boolean mVisible;

    private boolean mPowerPluggedIn;
    private boolean mPowerCharged;
    private int mChargingSpeed;
    private int mChargingWattage;
    private String mMessageToShowOnScreenOn;

    public KeyguardIndicationController(Context context, KeyguardIndicationTextView textView,
                                        LockIcon lockIcon) {
        mContext = context;
        mTextView = textView;
        mLockIcon = lockIcon;

        Resources res = context.getResources();
        mSlowThreshold = res.getInteger(R.integer.config_chargingSlowlyThreshold);
        mFastThreshold = res.getInteger(R.integer.config_chargingFastThreshold);

        mUserManager = context.getSystemService(UserManager.class);
        mBatteryInfo = IBatteryStats.Stub.asInterface(
                ServiceManager.getService(BatteryStats.SERVICE_NAME));

        KeyguardUpdateMonitor.getInstance(context).registerCallback(mUpdateMonitor);
        context.registerReceiverAsUser(mTickReceiver, UserHandle.SYSTEM,
                new IntentFilter(Intent.ACTION_TIME_TICK), null, null);
    }

    public void setVisible(boolean visible) {
        /*PRIZE-add for haokan-liufan-2017-10-25-start*/
        PhoneStatusBar bar = mStatusBarKeyguardViewManager != null
            ? mStatusBarKeyguardViewManager.getStatusBar() : null;
        if(bar != null && bar.isUseHaoKan()){
            return ;
        }
        /*PRIZE-add for haokan-liufan-2017-10-25-end*/
        mVisible = visible;
        mTextView.setVisibility(visible ? View.VISIBLE : View.GONE);
        if (visible) {
            hideTransientIndication();
            updateIndication();
        }
    }

    /*PRIZE-add for bugid:51190-liufan-2018-02-27-start*/
    public String getShowIndication(){
        if(mTextView!=null){
            return mTextView.getText().toString();
        }
        return null;
    }
    /*PRIZE-add for bugid:51190-liufan-2018-02-27-end*/

    /**
     * Sets the indication that is shown if nothing else is showing.
     */
    public void setRestingIndication(String restingIndication) {
        mRestingIndication = restingIndication;
        updateIndication();
    }

    /**
     * Hides transient indication in {@param delayMs}.
     */
    public void hideTransientIndicationDelayed(long delayMs) {
        mHandler.sendMessageDelayed(
                mHandler.obtainMessage(MSG_HIDE_TRANSIENT), delayMs);
    }

    /**
     * Shows {@param transientIndication} until it is hidden by {@link #hideTransientIndication}.
     */
	//Modify for background_reflect - luyingying - 2017-09-09
    public void showTransientIndication(int transientIndication, int color) {
        showTransientIndication(mContext.getResources().getString(transientIndication), color);
    }
    //Modify end
    /**
     * Shows {@param transientIndication} until it is hidden by {@link #hideTransientIndication}.
     */
    public void showTransientIndication(String transientIndication) {
        showTransientIndication(transientIndication, Color.WHITE);
    }

    /**
     * Shows {@param transientIndication} until it is hidden by {@link #hideTransientIndication}.
     */
    public void showTransientIndication(String transientIndication, int color) {
        mTransientIndication = transientIndication;
        /*PRIZE-Add for backgroud_reflect-luyingying-2017-09-09-Start*/
        if(PrizeOption.PRIZE_LOCKSCREEN_INVERSE){
            if(textColor == 0){
                mTransientTextColor = PhoneStatusBar.LOCK_DARK_COLOR;
            }else{
                mTransientTextColor = PhoneStatusBar.LOCK_LIGHT_COLOR;
            }
        } else {
            mTransientTextColor = textColor;
        }
        /*PRIZE-Add for backgroud_reflect-luyingying-2017-09-09-end*/
        mHandler.removeMessages(MSG_HIDE_TRANSIENT);
        updateIndication();
    }

    /**
     * Hides transient indication.
     */
    public void hideTransientIndication() {
        if (mTransientIndication != null) {
            mTransientIndication = null;
            mHandler.removeMessages(MSG_HIDE_TRANSIENT);
            updateIndication();
        }
    }

    private void updateIndication() {
        /*PRIZE-add for haokan-liufan-2018-2-24-start*/
        PhoneStatusBar bar = mStatusBarKeyguardViewManager != null
            ? mStatusBarKeyguardViewManager.getStatusBar() : null;
        if(bar != null && bar.isUseHaoKan()){
            return ;
        }
        /*PRIZE-add for haokan-liufan-2018-2-24-end*/
        if (mVisible) {
            mTextView.switchIndication(computeIndication());
            mTextView.setTextColor(computeColor());
        }
    }

    private int computeColor() {
        /*PRIZE-finger unlock error color-liufan-2016-09-18-start*/
        /*if (!TextUtils.isEmpty(mTransientIndication)) {
            return mTransientTextColor;
        }
        return Color.WHITE;*/
        //Modify for background_reflect - luyingying - 2017-09-09
        if(PrizeOption.PRIZE_LOCKSCREEN_INVERSE){
            if(textColor == 0){
                mTransientTextColor = PhoneStatusBar.LOCK_DARK_COLOR;
            }else{
                mTransientTextColor = PhoneStatusBar.LOCK_LIGHT_COLOR;
            }
            return mTransientTextColor;
        } else {
            return 0x80ffffff;
        }
        /*PRIZE-finger unlock error color-liufan-2016-09-18-end*/
    }

    private String computeIndication() {
        /*PRIZE-add for haokan-liufan-2017-10-25-start*/
        PhoneStatusBar bar = mStatusBarKeyguardViewManager != null
            ? mStatusBarKeyguardViewManager.getStatusBar() : null;
        if(bar != null && bar.isUseHaoKan()){
            return "";
        }
        /*PRIZE-add for haokan-liufan-2017-10-25-end*/

        if (!TextUtils.isEmpty(mTransientIndication)) {
            return mTransientIndication;
        }
        if (mPowerPluggedIn) {
            String indication = computePowerIndication();
            if (DEBUG_CHARGING_SPEED) {
                indication += ",  " + (mChargingWattage / 1000) + " mW";
            }
            return indication;
        }
        return mRestingIndication;
    }

    private String computePowerIndication() {
        if (mPowerCharged) {
            return mContext.getResources().getString(R.string.keyguard_charged);
        }

        // Try fetching charging time from battery stats.
        long chargingTimeRemaining = 0;
        try {
            chargingTimeRemaining = mBatteryInfo.computeChargeTimeRemaining();

        } catch (RemoteException e) {
            Log.e(TAG, "Error calling IBatteryStats: ", e);
        }
        final boolean hasChargingTime = chargingTimeRemaining > 0;

        int chargingId;
        switch (mChargingSpeed) {
            case KeyguardUpdateMonitor.BatteryStatus.CHARGING_FAST:
                chargingId = hasChargingTime
                        ? R.string.keyguard_indication_charging_time_fast
                        : R.string.keyguard_plugged_in_charging_fast;
                break;
            case KeyguardUpdateMonitor.BatteryStatus.CHARGING_SLOWLY:
                chargingId = hasChargingTime
                        ? R.string.keyguard_indication_charging_time_slowly
                        : R.string.keyguard_plugged_in_charging_slowly;
                break;
            default:
                chargingId = hasChargingTime
                        ? R.string.keyguard_indication_charging_time
                        : R.string.keyguard_plugged_in;
                break;
        }

        if (hasChargingTime) {
            String chargingTimeFormatted = Formatter.formatShortElapsedTimeRoundingUpToMinutes(
                    mContext, chargingTimeRemaining);
            return mContext.getResources().getString(chargingId, chargingTimeFormatted);
        } else {
            return mContext.getResources().getString(chargingId);
        }
    }

    KeyguardUpdateMonitorCallback mUpdateMonitor = new KeyguardUpdateMonitorCallback() {
        public int mLastSuccessiveErrorMessage = -1;

        @Override
        public void onRefreshBatteryInfo(KeyguardUpdateMonitor.BatteryStatus status) {
            boolean isChargingOrFull = status.status == BatteryManager.BATTERY_STATUS_CHARGING
                    || status.status == BatteryManager.BATTERY_STATUS_FULL;
            mPowerPluggedIn = status.isPluggedIn() && isChargingOrFull;
            mPowerCharged = status.isCharged();
            mChargingWattage = status.maxChargingWattage;
            mChargingSpeed = status.getChargingSpeed(mSlowThreshold, mFastThreshold);
            updateIndication();
        }

        @Override
        public void onFingerprintHelp(int msgId, String helpString) {
            KeyguardUpdateMonitor updateMonitor = KeyguardUpdateMonitor.getInstance(mContext);
            if (!updateMonitor.isUnlockingWithFingerprintAllowed()) {
                return;
            }
            /*PRIZE-finger unlock error color-liufan-2016-09-09-start*/
/*-prize-add by lihuangyuan,for fingerprint quick unlock-2018-03-05-start*/
            int errorColor = mContext.getResources().getColor(R.color.prize_finger_system_warning_color, null);
/*-prize-add by lihuangyuan,for fingerprint quick unlock-2018-03-05-end*/
            /*PRIZE-finger unlock error color-liufan-2016-09-09-end*/
            if (mStatusBarKeyguardViewManager.isBouncerShowing()) {
                mStatusBarKeyguardViewManager.showBouncerMessage(helpString, errorColor);
            } else if (updateMonitor.isDeviceInteractive()) {
                mLockIcon.setTransientFpError(true);
				//Modify for backgroud_reflect-luyingying-2017-09-09-Start*/
                showTransientIndication(helpString, textColor);
				//Modify end
                mHandler.removeMessages(MSG_CLEAR_FP_MSG);
                mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_CLEAR_FP_MSG),
                        TRANSIENT_FP_ERROR_TIMEOUT);
            }
            // Help messages indicate that there was actually a try since the last error, so those
            // are not two successive error messages anymore.
            mLastSuccessiveErrorMessage = -1;
        }

        @Override
        public void onFingerprintError(int msgId, String errString) {
            KeyguardUpdateMonitor updateMonitor = KeyguardUpdateMonitor.getInstance(mContext);
            if (!updateMonitor.isUnlockingWithFingerprintAllowed()
                    || msgId == FingerprintManager.FINGERPRINT_ERROR_CANCELED) {
                return;
            }
            /*PRIZE-finger unlock error color-liufan-2016-09-09-start*/
/*-prize-add by lihuangyuan,for fingerprint quick unlock-2018-03-05-start*/
            int errorColor = mContext.getResources().getColor(R.color.prize_finger_system_warning_color, null);
/*-prize-add by lihuangyuan,for fingerprint quick unlock-2018-03-05-end*/
            /*PRIZE-finger unlock error color-liufan-2016-09-09-end*/
            if (mStatusBarKeyguardViewManager.isBouncerShowing()) {
                // When swiping up right after receiving a fingerprint error, the bouncer calls
                // authenticate leading to the same message being shown again on the bouncer.
                // We want to avoid this, as it may confuse the user when the message is too
                // generic.
                /*if (mLastSuccessiveErrorMessage != msgId)*/ {//removed by lihuangyuan,for fingerprint quick unlock-2018-03-05
			Log.d(TAG,"onFingerprintError showBouncerMessage");
                    mStatusBarKeyguardViewManager.showBouncerMessage(errString, errorColor);
                }
            } else if (updateMonitor.isDeviceInteractive()) {
				    //Modify for backgroud_reflect-luyingying-2017-09-09-Start*/
                    showTransientIndication(errString, textColor);
					//Modify end
                // We want to keep this message around in case the screen was off
                mHandler.removeMessages(MSG_HIDE_TRANSIENT);
                hideTransientIndicationDelayed(5000);
            } else {
                mMessageToShowOnScreenOn = errString;
            }
            mLastSuccessiveErrorMessage = msgId;
        }

        @Override
        public void onScreenTurnedOn() {
            if (mMessageToShowOnScreenOn != null) {
                /*PRIZE-finger unlock error color-liufan-2016-09-09-start*/
                //int errorColor = mContext.getResources().getColor(R.color.prize_finger_system_warning_color,
                //        null);
                /*PRIZE-finger unlock error color-liufan-2016-09-09-end*/
				//Modify for backgroud_reflect-luyingying-2017-09-09-Start*/
                showTransientIndication(mMessageToShowOnScreenOn, textColor);
				//Modify end
                // We want to keep this message around in case the screen was off
                mHandler.removeMessages(MSG_HIDE_TRANSIENT);
                hideTransientIndicationDelayed(5000);
                mMessageToShowOnScreenOn = null;
            }
        }

        @Override
        public void onFingerprintRunningStateChanged(boolean running) {
            if (running) {
                mMessageToShowOnScreenOn = null;
            }
        }

        @Override
        public void onFingerprintAuthenticated(int userId) {
            super.onFingerprintAuthenticated(userId);
            mLastSuccessiveErrorMessage = -1;
        }

        @Override
        public void onFingerprintAuthFailed() {
            super.onFingerprintAuthFailed();
            mLastSuccessiveErrorMessage = -1;
        }

        @Override
        public void onUserUnlocked() {
            if (mVisible) {
                updateIndication();
            }
        }
    };

    BroadcastReceiver mTickReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (mVisible) {
                updateIndication();
            }
        }
    };


    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MSG_HIDE_TRANSIENT && mTransientIndication != null) {
                mTransientIndication = null;
                updateIndication();
            } else if (msg.what == MSG_CLEAR_FP_MSG) {
                mLockIcon.setTransientFpError(false);
                hideTransientIndication();
            }
        }
    };

    public void setStatusBarKeyguardViewManager(
            StatusBarKeyguardViewManager statusBarKeyguardViewManager) {
        mStatusBarKeyguardViewManager = statusBarKeyguardViewManager;
    }
	/*PRIZE-Add for backgroud_reflect-luyingying-2017-09-09-Start*/
    public static int textColor = 0;
    public void setTextColor(int color){
		textColor = color;
    }
    /*PRIZE-Add for backgroud_reflect-luyingying-2017-09-09-end*/
}
