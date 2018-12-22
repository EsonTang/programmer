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
 * limitations under the License.
 */

package com.android.systemui.qs.tiles;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.os.Handler;
import android.os.SystemProperties;
import android.provider.Settings;
import android.util.Log;

import com.android.internal.logging.MetricsProto.MetricsEvent;
import com.android.systemui.R;
import com.android.systemui.qs.QSTile;
import com.android.systemui.volume.Util;

/**
 * create by xiarui 2018-06-05 for vibrate in silent
 */
public class PrizeVibrateInSilentTile extends QSTile<QSTile.BooleanState> {

    private static final String TAG = "PrizeVibrateInSilentTile";

    private AudioManager mAudioManager;

    public PrizeVibrateInSilentTile(Host host) {
        super(host);
        mAudioManager = mContext.getSystemService(AudioManager.class);
        //register receiver
        final IntentFilter filter = new IntentFilter();
        filter.addAction(AudioManager.INTERNAL_RINGER_MODE_CHANGED_ACTION);
        mContext.registerReceiver(mRingerModeChangedReceiver, filter);
        //register observer
        final ContentResolver resolver = mContext.getContentResolver();
        resolver.registerContentObserver(Settings.System.getUriFor(Settings.System.PRIZE_VIBRATE_IN_SILENT), false, mVibrateInSilentObserver);
    }

    @Override
    public BooleanState newTileState() {
        return new BooleanState();
    }

    @Override
    public Intent getLongClickIntent() {
        return null;
    }

    @Override
    public void setListening(boolean listening) {

    }

    @Override
    protected void handleClick() {
        final boolean isEnabled = (Boolean) mState.value;
        if (isEnabled) {
            mAudioManager.setRingerModeInternal(AudioManager.RINGER_MODE_NORMAL);
        } else {
            boolean isVibrateInSilent = isVibrateInSilentOpen();//SystemProperties.getBoolean("debug.ringer.vibrator", false);
            Log.d("debug.ringer", "isVibrateInSilent = " + isVibrateInSilent);
            if (isVibrateInSilent) {
                mAudioManager.setRingerModeInternal(AudioManager.RINGER_MODE_VIBRATE);
            } else {
                mAudioManager.setRingerModeInternal(AudioManager.RINGER_MODE_SILENT);
            }
        }
    }

    @Override
    public CharSequence getTileLabel() {
        return mContext.getString(R.string.quick_settings_no_prizerings);

    }

    @Override
    protected void handleUpdateState(BooleanState state, Object arg) {
        int ringMode = mAudioManager.getRingerModeInternal();
        state.label = mContext.getString(R.string.quick_settings_no_prizerings);
        if (AudioManager.RINGER_MODE_VIBRATE == ringMode || AudioManager.RINGER_MODE_SILENT == ringMode) {
            state.value = true;
            state.icon = ResourceIcon.get(R.drawable.prize_silent_rings);
            state.contentDescription = state.label;
            state.colorId = 1;
        } else {
            state.value = false;
            state.icon = ResourceIcon.get(R.drawable.prize_normal_rings);
            state.contentDescription = state.label;
            state.colorId = 0;
        }
    }

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.QS_PANEL;
    }

    @Override
    protected String composeChangeAnnouncement() {
        return null;
    }

    @Override
    protected void handleDestroy() {
        super.handleDestroy();
        if (mRingerModeChangedReceiver != null) {
            mContext.unregisterReceiver(mRingerModeChangedReceiver);
        }
        if (mVibrateInSilentObserver != null) {
            final ContentResolver resolver = mContext.getContentResolver();
            resolver.unregisterContentObserver(mVibrateInSilentObserver);
        }
    }

    private BroadcastReceiver mRingerModeChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action.equals(AudioManager.INTERNAL_RINGER_MODE_CHANGED_ACTION)) {
                final int rm = intent.getIntExtra(AudioManager.EXTRA_RINGER_MODE, -1);
                Log.d(TAG, "onReceive RINGER_MODE_CHANGED_ACTION rm=" + Util.ringerModeToString(rm));
                refreshState();
            }
        }
    };

    private final ContentObserver mVibrateInSilentObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            int ringerMode = mAudioManager.getRingerModeInternal();
            boolean isVibrateInSilent = isVibrateInSilentOpen();
            Log.d(TAG,"mVibrateInSilentObserver - onChange ringerMode = " + Util.ringerModeToString(ringerMode) + " , isVibrateInSilent = " + isVibrateInSilent);
            if (ringerMode == AudioManager.RINGER_MODE_SILENT && isVibrateInSilentOpen()) {
                mAudioManager.setRingerModeInternal(AudioManager.RINGER_MODE_VIBRATE);
            } else if (ringerMode == AudioManager.RINGER_MODE_VIBRATE && !isVibrateInSilentOpen()) {
                mAudioManager.setRingerModeInternal(AudioManager.RINGER_MODE_SILENT);
            }
        }
    };

    private boolean isVibrateInSilentOpen() {
        return Settings.System.getInt(mContext.getContentResolver(), Settings.System.PRIZE_VIBRATE_IN_SILENT, 0) != 0;
    }
}
