package com.android.systemui.qs.tiles;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.android.internal.logging.MetricsProto.MetricsEvent;
import com.android.internal.telephony.IccCardConstants;
import com.android.internal.telephony.TelephonyIntents;
import com.android.systemui.R;
import com.android.systemui.qs.QSTile;
import com.android.systemui.recents.LogUtils;
import com.android.systemui.recents.Recents;
import com.android.systemui.recents.events.activity.MultiWindowStateChangedEvent;
import com.android.systemui.recents.misc.SystemServicesProxy;
import com.android.systemui.statusbar.StatusBarState;
import com.android.systemui.statusbar.phone.QSTileHost;

/**
 * Created by prize-xiarui on 2017/11/23.
 * {@link OneCleanUpTile} disable when System in MultiWindowMode {@link com.android.systemui.recents.RecentsActivity#onMultiWindowModeChanged(boolean)}
 */

public class OneCleanUpTile extends QSTile<QSTile.BooleanState> {

    private String configName;

    public OneCleanUpTile(Host host, OnTileClickListener onTileClickListener, String configName) {
        super(host);
        this.onTileClickListener = onTileClickListener;
        this.configName = configName;
    }

    @Override
    public BooleanState newTileState() {
        return new BooleanState();
    }

    @Override
    protected void handleClick() {
        if(onTileClickListener != null && mState.value) {
            onTileClickListener.onTileClick(mState.value, configName);
            LogUtils.d("OneCleanUpTile","OneCleanUpTile--->" + configName);
        }
    }

    @Override
    protected void handleUpdateState(BooleanState state, Object arg) {
        //if (arg instanceof Boolean) {
        //    state.value = (Boolean) arg;
        //}
        SystemServicesProxy ssp = Recents.getSystemServices();
        int barState = ((QSTileHost)mHost).getPhoneStatusBar().getBarState(); //for Bug#43987
        if (ssp.hasDockedTask() || barState == StatusBarState.KEYGUARD || barState == StatusBarState.SHADE_LOCKED) {
            state.value = false;
        } else {
            state.value = true;
        }
        mState.value = state.value;
        LogUtils.d("OneCleanUpTile", "handleUpdateState  state.value=" + state.value);
        state.icon = ResourceIcon.get(mState.value ? R.drawable.ic_qs_cleanupkey_selector : R.drawable.ic_qs_cleanupkey_off);
        state.colorId = mState.value ? 1 : 0;
        state.label = getTileLabel();
        state.contentDescription = state.label;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.QS_PANEL;
    }

    @Override
    public Intent getLongClickIntent() {
        return null;
    }

    @Override
    protected void setListening(boolean listening) {

    }

    @Override
    public CharSequence getTileLabel() {
        return mContext.getString(R.string.quick_settings_cleanupkey_label);
    }

}
