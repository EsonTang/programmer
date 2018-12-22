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

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Switch;

import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.MetricsProto.MetricsEvent;
import com.android.settingslib.bluetooth.CachedBluetoothDevice;
import com.android.systemui.R;
import com.android.systemui.qs.QSDetailItems;
import com.android.systemui.qs.QSDetailItems.Item;
import com.android.systemui.qs.QSTile;
import com.android.systemui.statusbar.policy.BluetoothController;

import java.util.ArrayList;
import java.util.Collection;
import android.media.AudioManager;
/*prize add by xiarui 2018-03-26 for Bug#53698 start*/
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.util.Log;
/*prize add by xiarui 2018-03-26 for Bug#53698 end*/

/** Quick settings tile: Rings **/
public class PrizeRings extends QSTile<QSTile.BooleanState>  {
	private AudioManager mAudioManager;
    public PrizeRings(Host host) {
        super(host);
		mAudioManager = mContext.getSystemService(AudioManager.class);
        /*prize add by xiarui 2018-03-26 for Bug#53698 start*/
        final IntentFilter filter = new IntentFilter();
        filter.addAction(AudioManager.STREAM_MUTE_CHANGED_ACTION);
        mContext.registerReceiver(mStreamMuteChangeReceiver, filter);
        /*prize add by xiarui 2018-03-26 for Bug#53698 end*/
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
        final boolean isEnabled = (Boolean)mState.value;
		if(isEnabled){
			mAudioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
		}else{
			//mAudioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
			mAudioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
		}
		refreshState(!isEnabled);
    }

    @Override
    public CharSequence getTileLabel() {
		int ringMode = mAudioManager.getRingerMode();
		//if(AudioManager.RINGER_MODE_SILENT == ringMode){
		/*if(AudioManager.RINGER_MODE_VIBRATE == ringMode){
			return mContext.getString(R.string.quick_settings_no_prizerings);
		}else{
			return mContext.getString(R.string.quick_settings_prizerings);
		}*/
        return mContext.getString(R.string.quick_settings_no_prizerings);
        
    }

    @Override
    protected void handleUpdateState(BooleanState state, Object arg) {
		int ringMode = mAudioManager.getRingerMode();
        state.label = mContext.getString(R.string.quick_settings_no_prizerings);
		//if(AudioManager.RINGER_MODE_SILENT == ringMode){
		if(AudioManager.RINGER_MODE_VIBRATE == ringMode){
			state.value = true;
			state.icon = ResourceIcon.get(R.drawable.prize_silent_rings);
			state.contentDescription = state.label;
            state.colorId = 1;
		}else{
			state.value = false;
			//state.label = mContext.getString(R.string.quick_settings_prizerings);
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

    /*prize add by xiarui 2018-03-26 for Bug#53698 start*/

    @Override
    protected void handleDestroy() {
        super.handleDestroy();
        if (mStreamMuteChangeReceiver != null) {
            mContext.unregisterReceiver(mStreamMuteChangeReceiver);
        }
    }

    private BroadcastReceiver mStreamMuteChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action.equals(AudioManager.STREAM_MUTE_CHANGED_ACTION)) {
                final boolean muted = intent.getBooleanExtra(AudioManager.EXTRA_STREAM_VOLUME_MUTED, false);
                if (DEBUG) Log.d(TAG, "onReceive STREAM_MUTE_CHANGED_ACTION muted = " + muted);
                refreshState(!muted);
            }
        }
    };

    /*prize add by xiarui 2018-03-26 for Bug#53698 end*/
  
}
