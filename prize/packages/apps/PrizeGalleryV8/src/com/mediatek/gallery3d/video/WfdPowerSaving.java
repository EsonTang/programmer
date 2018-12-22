package com.mediatek.gallery3d.video;

import android.content.Context;
import android.view.Window;
import android.provider.Settings;

import com.android.gallery3d.app.Log;

public class WfdPowerSaving extends PowerSaving {
    private static final String TAG = "Gallery2/VideoPlayer/WfdPowerSaving";
    private static final int EXTENSION_MODE_LIST_START = 10;
    private static final int EXTENSION_MODE_LIST_END = 12;

    public WfdPowerSaving(final Context context, final Window window) {
        super(context, window);
    }

    @Override
    protected int getPowerSavingMode() {
        int mode = POWER_SAVING_MODE_NONE;
        mode = Settings.Global.getInt(mContext.getContentResolver(),
                Settings.Global.WIFI_DISPLAY_POWER_SAVING_OPTION, 0);
        if ((mode >= EXTENSION_MODE_LIST_START)
                && (mode <= EXTENSION_MODE_LIST_END)) {
            mode = mode - EXTENSION_MODE_LIST_START;
        }
        Log.v(TAG, "getWfdPowerSavingMode(): " + mode);
        return mode;
    }

    @Override
    protected int getDelayTime() {
        int delayTime = Settings.Global.getInt(mContext.getContentResolver(),
                    Settings.Global.WIFI_DISPLAY_POWER_SAVING_DELAY, 0);
        Log.v(TAG, "getDelayTime(): " + delayTime);
        return delayTime * 1000;
    }

}
