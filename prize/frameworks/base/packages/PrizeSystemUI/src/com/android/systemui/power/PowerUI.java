/*
 * Copyright (C) 2008 The Android Open Source Project
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

package com.android.systemui.power;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Log;
import android.util.Slog;

import com.android.systemui.SystemUI;
import com.android.systemui.statusbar.phone.PhoneStatusBar;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.Arrays;
/*PRIZE-PowerExtendMode-wangxianzhen-2015-05-30-start*/
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.os.PowerManager;
import com.mediatek.common.prizeoption.PrizeOption;
import com.android.systemui.R;
/*PRIZE-PowerExtendMode-wangxianzhen-2015-05-30-end*/

public class PowerUI extends SystemUI {
    static final String TAG = "PowerUI";
    static final boolean DEBUG = Log.isLoggable(TAG, Log.DEBUG);

    private final Handler mHandler = new Handler();
    private final Receiver mReceiver = new Receiver();

    private PowerManager mPowerManager;
    private WarningsUI mWarnings;
    private int mBatteryLevel = 100;
    private int mBatteryStatus = BatteryManager.BATTERY_STATUS_UNKNOWN;
    private int mPlugType = 0;
    private int mInvalidCharger = 0;

    private int mLowBatteryAlertCloseLevel;
    private final int[] mLowBatteryReminderLevels = new int[2];

    private long mScreenOffTime = -1;
	/*PRIZE-PowerExtendMode-wangxianzhen-2015-05-30-start*/
    private boolean mDeviceProvision = false;
    static final int WARN_BATTERY_LEVEL = 15;
    NotificationManager lNotificationManager;
    /*PRIZE-PowerExtendMode-wangxianzhen-2015-05-30-end*/
    public void start() {
        mPowerManager = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
        mScreenOffTime = mPowerManager.isScreenOn() ? -1 : SystemClock.elapsedRealtime();
        mWarnings = new PowerNotificationWarnings(mContext, getComponent(PhoneStatusBar.class));

        ContentObserver obs = new ContentObserver(mHandler) {
            @Override
            public void onChange(boolean selfChange) {
                updateBatteryWarningLevels();
            }
        };
        final ContentResolver resolver = mContext.getContentResolver();
        resolver.registerContentObserver(Settings.Global.getUriFor(
                Settings.Global.LOW_POWER_MODE_TRIGGER_LEVEL),
                false, obs, UserHandle.USER_ALL);
        updateBatteryWarningLevels();
        mReceiver.init();
		/*PRIZE-PowerExtendMode-wangxianzhen-2015-05-30-start*/
	  /*prize-add by lihuangyuan,for first boot can not show superpower dialog,2017-10-25-start*/
	  ContentObserver deviceProvisionObserver = new ContentObserver(mHandler) {
            @Override
            public void onChange(boolean selfChange) {
                mDeviceProvision = (0 != Settings.Global.getInt(mContext.getContentResolver(), Settings.Global.DEVICE_PROVISIONED, 0));
            }
        };        
        resolver.registerContentObserver(Settings.Global.getUriFor(
                Settings.Global.DEVICE_PROVISIONED),
                false, deviceProvisionObserver, UserHandle.USER_ALL);
	 /*prize-add by lihuangyuan,for first boot can not show superpower dialog,2017-10-25-end*/
        mDeviceProvision = (0 != Settings.Global.getInt(mContext.getContentResolver(), Settings.Global.DEVICE_PROVISIONED, 0));
        /*PRIZE-PowerExtendMode-wangxianzhen-2015-05-30-end*/
    }

    void updateBatteryWarningLevels() {
        int critLevel = mContext.getResources().getInteger(
                com.android.internal.R.integer.config_criticalBatteryWarningLevel);

        final ContentResolver resolver = mContext.getContentResolver();
        int defWarnLevel = mContext.getResources().getInteger(
                com.android.internal.R.integer.config_lowBatteryWarningLevel);
        int warnLevel = Settings.Global.getInt(resolver,
                Settings.Global.LOW_POWER_MODE_TRIGGER_LEVEL, defWarnLevel);
        if (warnLevel == 0) {
            warnLevel = defWarnLevel;
        }
        if (warnLevel < critLevel) {
            warnLevel = critLevel;
        }

        mLowBatteryReminderLevels[0] = warnLevel;
        mLowBatteryReminderLevels[1] = critLevel;
        mLowBatteryAlertCloseLevel = mLowBatteryReminderLevels[0]
                + mContext.getResources().getInteger(
                        com.android.internal.R.integer.config_lowBatteryCloseWarningBump);
    }

    /**
     * Buckets the battery level.
     *
     * The code in this function is a little weird because I couldn't comprehend
     * the bucket going up when the battery level was going down. --joeo
     *
     * 1 means that the battery is "ok"
     * 0 means that the battery is between "ok" and what we should warn about.
     * less than 0 means that the battery is low
     */
    private int findBatteryLevelBucket(int level) {
        if (level >= mLowBatteryAlertCloseLevel) {
            return 1;
        }
        if (level > mLowBatteryReminderLevels[0]) {
            return 0;
        }
        final int N = mLowBatteryReminderLevels.length;
        for (int i=N-1; i>=0; i--) {
            if (level <= mLowBatteryReminderLevels[i]) {
                return -1-i;
            }
        }
        throw new RuntimeException("not possible!");
    }

    private final class Receiver extends BroadcastReceiver {

        public void init() {
            // Register for Intent broadcasts for...
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_BATTERY_CHANGED);
            filter.addAction(Intent.ACTION_SCREEN_OFF);
            filter.addAction(Intent.ACTION_SCREEN_ON);
            filter.addAction(Intent.ACTION_USER_SWITCHED);
            filter.addAction(PowerManager.ACTION_POWER_SAVE_MODE_CHANGING);
            filter.addAction(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED);
			/*PRIZE-PowerExtendMode-wangxianzhen-2015-05-30-start*/
            filter.addAction("android.intent.action.ACTION_CLOSE_SUPERPOWER_NOTIFICATION");
            /*PRIZE-PowerExtendMode-wangxianzhen-2015-05-30-end*/
            mContext.registerReceiver(this, filter, null, mHandler);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_BATTERY_CHANGED)) {
                final int oldBatteryLevel = mBatteryLevel;
                mBatteryLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 100);
                final int oldBatteryStatus = mBatteryStatus;
                mBatteryStatus = intent.getIntExtra(BatteryManager.EXTRA_STATUS,
                        BatteryManager.BATTERY_STATUS_UNKNOWN);
                final int oldPlugType = mPlugType;
                mPlugType = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 1);
				/*PRIZE-PowerExtendMode-wangxianzhen-2015-05-30-start*/
                if (PrizeOption.PRIZE_POWER_EXTEND_MODE && (!PowerManager.isSuperSaverMode())){
                    lowBatteryToSuperSaverNotification(context, oldBatteryLevel);
                }
                /*PRIZE-PowerExtendMode-wangxianzhen-2015-05-30-end*/
                final int oldInvalidCharger = mInvalidCharger;
                mInvalidCharger = intent.getIntExtra(BatteryManager.EXTRA_INVALID_CHARGER, 0);

                final boolean plugged = mPlugType != 0;
                final boolean oldPlugged = oldPlugType != 0;

                int oldBucket = findBatteryLevelBucket(oldBatteryLevel);
                int bucket = findBatteryLevelBucket(mBatteryLevel);

                if (DEBUG) {
                    Slog.d(TAG, "buckets   ....." + mLowBatteryAlertCloseLevel
                            + " .. " + mLowBatteryReminderLevels[0]
                            + " .. " + mLowBatteryReminderLevels[1]);
                    Slog.d(TAG, "level          " + oldBatteryLevel + " --> " + mBatteryLevel);
                    Slog.d(TAG, "status         " + oldBatteryStatus + " --> " + mBatteryStatus);
                    Slog.d(TAG, "plugType       " + oldPlugType + " --> " + mPlugType);
                    Slog.d(TAG, "invalidCharger " + oldInvalidCharger + " --> " + mInvalidCharger);
                    Slog.d(TAG, "bucket         " + oldBucket + " --> " + bucket);
                    Slog.d(TAG, "plugged        " + oldPlugged + " --> " + plugged);
                }

                mWarnings.update(mBatteryLevel, bucket, mScreenOffTime);
                if (oldInvalidCharger == 0 && mInvalidCharger != 0) {
                    Slog.d(TAG, "showing invalid charger warning");
                    mWarnings.showInvalidChargerWarning();
                    return;
                } else if (oldInvalidCharger != 0 && mInvalidCharger == 0) {
                    mWarnings.dismissInvalidChargerWarning();
                } else if (mWarnings.isInvalidChargerWarningShowing()) {
                    // if invalid charger is showing, don't show low battery
                    return;
                }

                boolean isPowerSaver = mPowerManager.isPowerSaveMode();
                if (!plugged
                        && !isPowerSaver
                        && (bucket < oldBucket || oldPlugged)
                        && mBatteryStatus != BatteryManager.BATTERY_STATUS_UNKNOWN
                        && bucket < 0) {
                    // only play SFX when the dialog comes up or the bucket changes
                    final boolean playSound = bucket != oldBucket || oldPlugged;
                    mWarnings.showLowBatteryWarning(playSound);
                } else if (isPowerSaver || plugged || (bucket > oldBucket && bucket > 0)) {
                    mWarnings.dismissLowBatteryWarning();
                } else {
                    mWarnings.updateLowBatteryWarning();
                }
            } else if (Intent.ACTION_SCREEN_OFF.equals(action)) {
                mScreenOffTime = SystemClock.elapsedRealtime();
            } else if (Intent.ACTION_SCREEN_ON.equals(action)) {
                mScreenOffTime = -1;
            } else if (Intent.ACTION_USER_SWITCHED.equals(action)) {
                mWarnings.userSwitched();
			/*PRIZE-PowerExtendMode-wangxianzhen-2015-05-30-start*/
            } else if (action.equals("android.intent.action.ACTION_CLOSE_SUPERPOWER_NOTIFICATION")) {
				if (null != mSupersaverNotification){
                    lNotificationManager.cancel(SupersaverNotificationID);
                    mSupersaverNotification = null;
                }
            /*PRIZE-PowerExtendMode-wangxianzhen-2015-05-30-end*/
            } else {
                Slog.w(TAG, "unknown intent: " + intent);
            }
        }
    };

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.print("mLowBatteryAlertCloseLevel=");
        pw.println(mLowBatteryAlertCloseLevel);
        pw.print("mLowBatteryReminderLevels=");
        pw.println(Arrays.toString(mLowBatteryReminderLevels));
        pw.print("mBatteryLevel=");
        pw.println(Integer.toString(mBatteryLevel));
        pw.print("mBatteryStatus=");
        pw.println(Integer.toString(mBatteryStatus));
        pw.print("mPlugType=");
        pw.println(Integer.toString(mPlugType));
        pw.print("mInvalidCharger=");
        pw.println(Integer.toString(mInvalidCharger));
        pw.print("mScreenOffTime=");
        pw.print(mScreenOffTime);
        if (mScreenOffTime >= 0) {
            pw.print(" (");
            pw.print(SystemClock.elapsedRealtime() - mScreenOffTime);
            pw.print(" ago)");
        }
        pw.println();
        pw.print("soundTimeout=");
        pw.println(Settings.Global.getInt(mContext.getContentResolver(),
                Settings.Global.LOW_BATTERY_SOUND_TIMEOUT, 0));
        pw.print("bucket: ");
        pw.println(Integer.toString(findBatteryLevelBucket(mBatteryLevel)));
        mWarnings.dump(pw);
    }

    public interface WarningsUI {
        void update(int batteryLevel, int bucket, long screenOffTime);
        void dismissLowBatteryWarning();
        void showLowBatteryWarning(boolean playSound);
        void dismissInvalidChargerWarning();
        void showInvalidChargerWarning();
        void updateLowBatteryWarning();
        boolean isInvalidChargerWarningShowing();
        void dump(PrintWriter pw);
        void userSwitched();
    }
	
	/*PRIZE-PowerExtendMode-wangxianzhen-2015-05-30-start*/
    private static final int SupersaverNotificationID = 1;
    private static boolean mEnableSupersaverNotification = true;
    private static Notification mSupersaverNotification = null;
    private static final String ACTION_CLOSE_MODE_SWITCH_DIALOGS = "android.intent.action.CLOSE_MODE_SWITCH_DIALOGS";

    private void enableSupersaverNotification(boolean lEnable){
        mEnableSupersaverNotification = lEnable;
    }

    private boolean isEnableSupersaverNotification(){
        return mEnableSupersaverNotification;
    }

    /**
    * Whether show notification when battery is low, WARN_BATTERY_LEVEL% remaing.
    * @return
    */
    private boolean isShowSuperSaverNotification(int oldBatteryLevel){
        return (
                false==PowerManager.isSuperSaverMode()
                && true == mDeviceProvision
                && (
                    (mPlugType == 0 && oldBatteryLevel==mBatteryLevel && mBatteryLevel<=WARN_BATTERY_LEVEL)
                      || (oldBatteryLevel>mBatteryLevel && mBatteryLevel==WARN_BATTERY_LEVEL)
                    )
                );
    }
    /**
    * Notification when low battery.
    * @return
    */
    private void lowBatteryToSuperSaverNotification(final Context lContext, int lOldBatteryLevel){

        lNotificationManager = (NotificationManager)lContext.getSystemService(Context.NOTIFICATION_SERVICE);

        if (isEnableSupersaverNotification()){
            if (isShowSuperSaverNotification(lOldBatteryLevel)){

                Intent clickIntent = new Intent()
                    .setAction(Intent.ACTION_INTO_POWER_SAVER)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |Intent.FLAG_ACTIVITY_SINGLE_TOP|Intent.FLAG_ACTIVITY_CLEAR_TOP);

                PendingIntent lPendingIntent = PendingIntent.getActivity(
                    lContext
                    , 0 /* requestCode */
                    , clickIntent
                    , PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_ONE_SHOT
                );

                mSupersaverNotification = new Notification.Builder(lContext)
                    /*PRIZE-power notification icon- liufan-2016-07-14-start*/
                    .setSmallIcon(R.drawable.ic_power_low_prize)//com.prize.internal.R.drawable.ic_super_saver_on
                    /*PRIZE-power notification icon- liufan-2016-07-14-end*/
                    .setContentIntent(lPendingIntent)
                    .setContentTitle(mContext.getResources().getText(R.string.PRIZE_SUPER_SAVER_NOTIFICATION_TITLE))
                    .setContentText(mContext.getResources().getText(R.string.PRIZE_SUPER_SAVER_NOTIFICATION_CONTENT))
                    .setWhen(0)
                    .build();

                mSupersaverNotification.flags |= Notification.FLAG_AUTO_CANCEL;
                /*PRIZE-retain power notification icon- liufan-2016-07-14-start*/
                mSupersaverNotification.flags |= Notification.FLAG_KEEP_NOTIFICATION_ICON;
                /*PRIZE-retain power notification icon- liufan-2016-07-14-end*/
                lNotificationManager.notify(SupersaverNotificationID, mSupersaverNotification);
                enableSupersaverNotification(false);
                Slog.d(TAG, "PowerExtendMode lowBatteryToSuperSaverNotification()->notify()");
                ((PowerManager)lContext.getSystemService(Context.POWER_SERVICE)).switchSuperSaverMode(true);
            }
        }else{
            if (lOldBatteryLevel<mBatteryLevel && mBatteryLevel>WARN_BATTERY_LEVEL){
                if (null != mSupersaverNotification){
                    lNotificationManager.cancel(SupersaverNotificationID);
                    mSupersaverNotification = null;
                }
                enableSupersaverNotification(true);
                // Close mode switch dialog
                lContext.sendBroadcast(new Intent(ACTION_CLOSE_MODE_SWITCH_DIALOGS));
                Slog.d(TAG, "PowerExtendMode lowBatteryToSuperSaverNotification()->mBatteryLevel:"+mBatteryLevel);
                Slog.d(TAG, "PowerExtendMode lowBatteryToSuperSaverNotification()->oldBatteryLevel:"+lOldBatteryLevel);
            }
        }
    }
    /*PRIZE-PowerExtendMode-wangxianzhen-2015-05-30-end*/
}

