/*****************************************
*版权所有©2015,深圳市铂睿智恒科技有限公司
*
*内容摘要：LocationTile的复制类，修改ui图片
*当前版本：V1.0
*作  者：liufan
*完成日期：2015-4-14
*修改记录：
*修改日期：
*版 本 号：
*修 改 人：
*修改内容：
********************************************/

package com.android.systemui.qs.tiles;

import android.content.Intent;
import android.os.UserManager;

import android.provider.Settings;
import android.widget.Switch;

import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.MetricsProto.MetricsEvent;
import com.android.systemui.R;
import com.android.systemui.qs.QSTile;
import com.android.systemui.statusbar.policy.KeyguardMonitor;
import com.android.systemui.statusbar.policy.LocationController;
import com.android.systemui.statusbar.policy.LocationController.LocationSettingsChangeCallback;

import android.app.ActivityManager;
/** Quick settings tile: Location **/
public class GPSTileDefined extends QSTile<QSTile.BooleanState> {

    private final AnimationIcon mEnable =
            new AnimationIcon(R.drawable.ic_signal_location_enable_animation,
                    R.drawable.ic_signal_location_disable);
    private final AnimationIcon mDisable =
            new AnimationIcon(R.drawable.ic_signal_location_disable_animation,
                    R.drawable.ic_signal_location_enable);

    private final LocationController mController;
    private final KeyguardMonitor mKeyguard;
    private final Callback mCallback = new Callback();

    public GPSTileDefined(Host host) {
        super(host);
        mController = host.getLocationController();
        mKeyguard = host.getKeyguardMonitor();
    }

    @Override
    public BooleanState newTileState() {
        return new BooleanState();
    }

    @Override
    public void setListening(boolean listening) {
        if (listening) {
            mController.addSettingsChangedCallback(mCallback);
            mKeyguard.addCallback(mCallback);
        } else {
            mController.removeSettingsChangedCallback(mCallback);
            mKeyguard.removeCallback(mCallback);
        }
    }

    @Override
    public Intent getLongClickIntent() {
        return new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
    }

    @Override
    protected void handleClick() {
        if (mKeyguard.isSecure() && mKeyguard.isShowing()) {
            mHost.startRunnableDismissingKeyguard(new Runnable() {
                @Override
                public void run() {
                    final boolean wasEnabled = (Boolean) mState.value;
                    mHost.openPanels();
                    MetricsLogger.action(mContext, getMetricsCategory(), !wasEnabled);
                    mController.setLocationEnabled(!wasEnabled);
                }
            });
            return;
        }
        final boolean wasEnabled = (Boolean) mState.value;
        MetricsLogger.action(mContext, getMetricsCategory(), !wasEnabled);
        mController.setLocationEnabled(!wasEnabled);
    }

    @Override
    public CharSequence getTileLabel() {
        return mContext.getString(R.string.quick_settings_location_label);
    }

    @Override
    protected void handleUpdateState(BooleanState state, Object arg) {
        /**PRIZE-set the GPS icon dark when mode is Settings.Secure.LOCATION_MODE_BATTERY_SAVING-liufan-2016-03-22-start*/
        int mode = Settings.Secure.getIntForUser(mContext.getContentResolver(), Settings.Secure.LOCATION_MODE,
                Settings.Secure.LOCATION_MODE_OFF, ActivityManager.getCurrentUser());
        final boolean locationEnabled =  mController.isLocationEnabled() && mode != Settings.Secure.LOCATION_MODE_BATTERY_SAVING;
        /**PRIZE-set the GPS icon dark when mode is Settings.Secure.LOCATION_MODE_BATTERY_SAVING-liufan-2016-03-22-end*/

        // Work around for bug 15916487: don't show location tile on top of lock screen. After the
        // bug is fixed, this should be reverted to only hiding it on secure lock screens:
        // state.visible = !(mKeyguard.isSecure() && mKeyguard.isShowing());
        state.value = locationEnabled;
        checkIfRestrictionEnforcedByAdminOnly(state, UserManager.DISALLOW_SHARE_LOCATION);
        if (locationEnabled) {
            //state.iconId = R.drawable.ic_qs_gps_on;
            state.icon = ResourceIcon.get(R.drawable.ic_qs_gps_on);
            state.colorId = 1;
            state.label = mContext.getString(R.string.quick_settings_location_label);
            state.contentDescription = mContext.getString(
                    R.string.accessibility_quick_settings_location_on);
        } else {
            //state.iconId = R.drawable.ic_qs_gps_off;
            state.icon = ResourceIcon.get(R.drawable.ic_qs_gps_off);
            state.colorId = 0;
            state.label = mContext.getString(R.string.quick_settings_location_label);
            state.contentDescription = mContext.getString(
                    R.string.accessibility_quick_settings_location_off);
        }
        state.minimalAccessibilityClassName = state.expandedAccessibilityClassName
                = Switch.class.getName();
    }

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.QS_LOCATION;
    }

    @Override
    protected String composeChangeAnnouncement() {
        if (mState.value) {
            return mContext.getString(R.string.accessibility_quick_settings_location_changed_on);
        } else {
            return mContext.getString(R.string.accessibility_quick_settings_location_changed_off);
        }
    }

    private final class Callback implements LocationSettingsChangeCallback,
            KeyguardMonitor.Callback {
        @Override
        public void onLocationSettingsChanged(boolean enabled) {
            refreshState();
        }

        @Override
        public void onKeyguardChanged() {
            refreshState();
        }
    };
}
