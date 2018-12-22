package com.mediatek.gallery3d.video;

import android.content.Context;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.Settings;
import android.provider.Settings.System;
import android.view.Window;
import android.view.WindowManager;
import com.android.gallery3d.app.Log;
import com.mediatek.gallery3d.video.MtkVideoFeature;

public abstract class PowerSaving {
    private static final String TAG = "Gallery2/VideoPlayer/PowerSaving";
    protected Context mContext;
    private PowerManager mPowerManager;
    private Window mWindow;
    private Handler mHandler;
    private int mPowerSavingMode;
    private int mDelayedTime;
    private boolean mIsAutoBrithtness;
    private float mScreenBrightness;
    public static final int POWER_SAVING_MODE_OFF = 0;
    public static final int POWER_SAVING_MODE_DIM = 1;
    public static final int POWER_SAVING_MODE_NONE = 2;

    public PowerSaving(final Context context, final Window window) {
        Log.v(TAG, "PowerSaving construct");
        mContext = context;
        mPowerManager = (PowerManager) mContext
                .getSystemService(Context.POWER_SERVICE);
        mWindow = window;
        mHandler = new Handler();
        mPowerSavingMode = getPowerSavingMode();
        mDelayedTime = getDelayTime();
        initWindowsParameters();
    }

    public void refreshParameter() {
        Log.v(TAG, "refreshParameter()");
        initWindowsParameters();
    }

    private void initWindowsParameters() {
        Log.v(TAG, "initWindowsParameters()");
        mIsAutoBrithtness = isAutoBrightnessOn();
        WindowManager.LayoutParams lp = mWindow.getAttributes();
        mScreenBrightness = lp.screenBrightness;
    }

    public void startPowerSaving() {
        Log.v(TAG, "startPowerSaving()");
        mHandler.removeCallbacks(mPowerSavingRunnable);
        if (mIsAutoBrithtness) {
            // turn brightness to manual for avoid brightness auto change
            // affect power saving
            Settings.System.putInt(mContext.getContentResolver(),
                    System.SCREEN_BRIGHTNESS_MODE,
                    System.SCREEN_BRIGHTNESS_MODE_MANUAL);
        }
        mHandler.postDelayed(mPowerSavingRunnable, mDelayedTime);
    }

    public void endPowerSaving() {
        Log.v(TAG, "endPowerSaving");
        mHandler.removeCallbacks(mPowerSavingRunnable);
        if (mIsAutoBrithtness) {
            // When leave extension mode restore brightness mode
            Settings.System.putInt(mContext.getContentResolver(),
                    System.SCREEN_BRIGHTNESS_MODE,
                    System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
        }
        restoreBacklight();
    }

    private final Runnable mPowerSavingRunnable = new Runnable() {
        @Override
        public void run() {
            Log.v(TAG, "mPowerSavingRunnable run");
            adjustBacklight();
        }
    };

    protected abstract int getPowerSavingMode();
    protected abstract int getDelayTime();

    protected boolean isAutoBrightnessOn() {
        int BrightnessMode = Settings.System.getInt(
                mContext.getContentResolver(),
                System.SCREEN_BRIGHTNESS_MODE,
                System.SCREEN_BRIGHTNESS_MODE_MANUAL);
        boolean isAutoBrightness = false;
        if (BrightnessMode == System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
            isAutoBrightness = true;
        }
        Log.v(TAG, "isAutoBrightnessOn(): " + isAutoBrightness);
        return isAutoBrightness;
    }

    private void adjustBacklight() {
        Log.v(TAG, "adjustBacklight " + mPowerSavingMode);
        switch (mPowerSavingMode) {
        case PowerSaving.POWER_SAVING_MODE_OFF:
            mPowerManager.setBacklightOffForWfd(true);
            break;
        case PowerSaving.POWER_SAVING_MODE_DIM:
            WindowManager.LayoutParams lp = mWindow.getAttributes();
            mScreenBrightness = lp.screenBrightness;
            if (MtkVideoFeature.isSupperDimmingSupport()) {
                lp.screenBrightness = 10 / 255.0f;
            } else {
                lp.screenBrightness = 0 / 255.0f;
            }
            mWindow.setAttributes(lp);
            break;
        case PowerSaving.POWER_SAVING_MODE_NONE:
            break;
        default:
            break;
        }
    }

    private void restoreBacklight() {
        Log.v(TAG, "restoreBacklight " + mPowerSavingMode);
        switch (mPowerSavingMode) {
        case PowerSaving.POWER_SAVING_MODE_OFF:
            mPowerManager.setBacklightOffForWfd(false);
            break;
        case PowerSaving.POWER_SAVING_MODE_DIM:
            WindowManager.LayoutParams lp = mWindow.getAttributes();
            lp.screenBrightness = mScreenBrightness;
            mWindow.setAttributes(lp);
            break;
        case PowerSaving.POWER_SAVING_MODE_NONE:
            break;
        default:
            break;
        }
    }
}
