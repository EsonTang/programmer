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

package com.android.systemui.statusbar.phone;

import android.app.ActivityManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Process;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings;
import android.provider.Settings.Secure;
import android.service.quicksettings.Tile;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.android.systemui.R;
import com.android.systemui.qs.QSTile;
import com.android.systemui.qs.external.CustomTile;
import com.android.systemui.qs.external.TileLifecycleManager;
import com.android.systemui.qs.external.TileServices;
import com.android.systemui.qs.tiles.AirplaneModeTile;
import com.android.systemui.qs.tiles.BatteryTile;
import com.android.systemui.qs.tiles.BluetoothTile;
import com.android.systemui.qs.tiles.OneCleanUpTile;
import com.android.systemui.qs.tiles.PrizeRings;
import com.android.systemui.qs.tiles.CastTile;
import com.android.systemui.qs.tiles.CellularTile;
import com.android.systemui.qs.tiles.ColorInversionTile;
import com.android.systemui.qs.tiles.DataSaverTile;
import com.android.systemui.qs.tiles.DndTile;
import com.android.systemui.qs.tiles.FlashlightTile;
import com.android.systemui.qs.tiles.HotspotTile;
import com.android.systemui.qs.tiles.IntentTile;
import com.android.systemui.qs.tiles.LocationTile;
import com.android.systemui.qs.tiles.NightDisplayTile;
import com.android.systemui.qs.tiles.PrizeVibrateInSilentTile;
import com.android.systemui.qs.tiles.RotationLockTile;
import com.android.systemui.qs.tiles.UserTile;
import com.android.systemui.qs.tiles.WifiTile;
import com.android.systemui.qs.tiles.WorkModeTile;
import com.android.systemui.statusbar.policy.BatteryController;
import com.android.systemui.statusbar.policy.BluetoothController;
import com.android.systemui.statusbar.policy.CastController;
import com.android.systemui.statusbar.policy.NextAlarmController;
import com.android.systemui.statusbar.policy.FlashlightController;
import com.android.systemui.statusbar.policy.HotspotController;
import com.android.systemui.statusbar.policy.KeyguardMonitor;
import com.android.systemui.statusbar.policy.LocationController;
import com.android.systemui.statusbar.policy.NetworkController;
import com.android.systemui.statusbar.policy.RotationLockController;
import com.android.systemui.statusbar.policy.SecurityController;
import com.android.systemui.statusbar.policy.UserInfoController;
import com.android.systemui.statusbar.policy.UserSwitcherController;
import com.android.systemui.statusbar.policy.ZenModeController;
import com.android.systemui.tuner.TunerService;
import com.android.systemui.tuner.TunerService.Tunable;
import com.android.systemui.qs.tiles.PrizeBluLightTileDefined;

/// M: add plugin in quicksetting @{
import com.mediatek.common.prizeoption.PrizeOption;
import com.mediatek.systemui.ext.IQuickSettingsPlugin;
/// add plugin in quicksetting @}

/// M: Add extra tiles in quicksetting @{
import com.mediatek.systemui.PluginManager;
import com.mediatek.systemui.qs.tiles.HotKnotTile;
import com.mediatek.systemui.qs.tiles.ext.ApnSettingsTile;
import com.mediatek.systemui.qs.tiles.ext.DualSimSettingsTile;
import com.mediatek.systemui.qs.tiles.ext.MobileDataTile;
import com.mediatek.systemui.qs.tiles.ext.SimDataConnectionTile;
import com.mediatek.systemui.statusbar.policy.HotKnotController;
import com.mediatek.systemui.statusbar.util.SIMHelper;
// /@}

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
/*PRIZE-import package- liufan-2015-04-10-start*/
import com.android.systemui.qs.tiles.BaseTile;
import com.android.systemui.qs.QSTile;
import com.android.systemui.qs.QSTile.OnTileClickListener;
import com.android.systemui.qs.tiles.WifiTile;
import com.android.systemui.qs.tiles.BluetoothTile;
import com.android.systemui.qs.tiles.AirplaneModeTile;
import com.mediatek.systemui.qs.tiles.DataConnectionTileDefined;
import com.mediatek.systemui.qs.tiles.DataUsageTileDefined;
import com.android.systemui.qs.tiles.RotationLockTile;
import com.android.systemui.qs.tiles.FlashlightTile;
import com.android.systemui.qs.tiles.GPSTileDefined;
import com.android.systemui.qs.tiles.BrightnessTileDefined;
import com.android.systemui.qs.tiles.BatteryTile;
import com.android.systemui.qs.tiles.DormancyTileDefined;
import com.android.systemui.statusbar.policy.BatteryController;
import android.widget.TextView;
import com.android.systemui.BatteryMeterView;
/*PRIZE-added by pengcancan for flash --20161116-start*/
import com.android.systemui.qs.tiles.FlashlightTileDefined;
/*PRIZE-added by pengcancan for flash --20161116-end*/
import com.mediatek.systemui.PluginManager;
/*PRIZE-import package- liufan-2015-04-10-end*/
import com.android.systemui.qs.tiles.PrizeSpeedUpTile;

/** Platform implementation of the quick settings tile host **/
public class QSTileHost implements QSTile.Host, Tunable {
    private static final String TAG = "QSTileHost";
    private static final boolean DEBUG = Log.isLoggable(TAG, Log.DEBUG);

    public static final String TILES_SETTING = Secure.QS_TILES;

    private final Context mContext;
    private final PhoneStatusBar mStatusBar;
    private final LinkedHashMap<String, QSTile<?>> mTiles = new LinkedHashMap<>();
    protected final ArrayList<String> mTileSpecs = new ArrayList<>();
    private final BluetoothController mBluetooth;
    private final LocationController mLocation;
    private final RotationLockController mRotation;
    private final NetworkController mNetwork;
    private final ZenModeController mZen;
    private final HotspotController mHotspot;
    private final CastController mCast;
    private final Looper mLooper;
    private final FlashlightController mFlashlight;
    private final UserSwitcherController mUserSwitcherController;
    private final UserInfoController mUserInfoController;
    private final KeyguardMonitor mKeyguard;
    private final SecurityController mSecurity;
    private final BatteryController mBattery;
    private final StatusBarIconController mIconController;
    private final TileServices mServices;

    private final List<Callback> mCallbacks = new ArrayList<>();
    private final AutoTileManager mAutoTiles;
    private final ManagedProfileController mProfileController;
    private final NextAlarmController mNextAlarmController;
    private View mHeader;
    private int mCurrentUser;

    /// M: Add extra tiles in quicksetting @{
    // add HotKnot in quicksetting
    private final HotKnotController mHotKnot;
    // /@}

    public QSTileHost(Context context, PhoneStatusBar statusBar,
            BluetoothController bluetooth, LocationController location,
            RotationLockController rotation, NetworkController network,
            ZenModeController zen, HotspotController hotspot,
            CastController cast, FlashlightController flashlight,
            UserSwitcherController userSwitcher, UserInfoController userInfo,
            KeyguardMonitor keyguard, SecurityController security,
            BatteryController battery, StatusBarIconController iconController,
            NextAlarmController nextAlarmController,
            // M: Add extra tiles in quicksetting @{
            // add HotKnot in quicksetting
            HotKnotController hotknot) {
            // /@}
        mContext = context;
        mStatusBar = statusBar;
        mBluetooth = bluetooth;
        mLocation = location;
        mRotation = rotation;
        mNetwork = network;
        mZen = zen;
        mHotspot = hotspot;
        mCast = cast;
        mFlashlight = flashlight;
        mUserSwitcherController = userSwitcher;
        mUserInfoController = userInfo;
        mKeyguard = keyguard;
        mSecurity = security;
        mBattery = battery;
        mIconController = iconController;
        mNextAlarmController = nextAlarmController;
        mProfileController = new ManagedProfileController(this);

        /// M: Add extra tiles in quicksetting @{
        // add HotKnot in quicksetting
        mHotKnot = hotknot;
        // /@}

        final HandlerThread ht = new HandlerThread(QSTileHost.class.getSimpleName(),
                Process.THREAD_PRIORITY_BACKGROUND);
        ht.start();
        mLooper = ht.getLooper();

        mServices = new TileServices(this, mLooper);

        TunerService.get(mContext).addTunable(this, TILES_SETTING);
        // AutoTileManager can modify mTiles so make sure mTiles has already been initialized.
        mAutoTiles = new AutoTileManager(context, this);
    }
    /*PRIZE-add an another Construction method, receive OnTileClickListener obj- liufan-2015-04-10-start*/
    private static OnTileClickListener onTileClickListener;
    private BatteryController mBatteryController;
    private BatteryMeterView mBatteryMeterView;
    private TextView mBatteryLevel;
    
    public QSTileHost(Context context, PhoneStatusBar statusBar,
            BluetoothController bluetooth, LocationController location,
            RotationLockController rotation, NetworkController network,
            ZenModeController zen, HotspotController hotspot,
            CastController cast, FlashlightController flashlight,
            UserSwitcherController userSwitcher, UserInfoController userInfo,
			KeyguardMonitor keyguard, SecurityController security,
            BatteryController battery, StatusBarIconController iconController,
            NextAlarmController nextAlarmController,
            /// M: add HotKnot in quicksetting
            HotKnotController hotknot,

            /// liufan add BaseTile Listener
            OnTileClickListener onTileClickListener,
            BaseStatusBarHeader headerView,
            BatteryController mBatteryController
            ) {
        mContext = context;
        mStatusBar = statusBar;
        mBluetooth = bluetooth;
        mLocation = location;
        mRotation = rotation;
        mNetwork = network;
        mZen = zen;
        mHotspot = hotspot;
        mCast = cast;
        mFlashlight = flashlight;
        mUserSwitcherController = userSwitcher;
        mUserInfoController = userInfo;
        mKeyguard = keyguard;
        mSecurity = security;
        mBattery = battery;
        mIconController = iconController;
        mNextAlarmController = nextAlarmController;
        mProfileController = new ManagedProfileController(this);

        this.onTileClickListener = onTileClickListener;
        this.mBatteryController = mBatteryController;
        this.mBatteryLevel = (TextView) headerView.findViewById(R.id.battery_level);
        //this.mBatteryMeterView = (BatteryMeterView) headerView.findViewById(R.id.battery);
        //Log.e("liufan","mBatteryLevel---4->"+mBatteryLevel+", mBatteryMeterView---->"+mBatteryMeterView+", mBatteryController---->"+mBatteryController+", BluetoothController--->"+mBluetooth);
        /// M: add HotKnot in quicksetting
        mHotKnot = hotknot;


        final HandlerThread ht = new HandlerThread(QSTileHost.class.getSimpleName());
        ht.start();
        mLooper = ht.getLooper();

        mServices = new TileServices(this, mLooper);

        TunerService.get(mContext).addTunable(this, TILES_SETTING);
        // AutoTileManager can modify mTiles so make sure mTiles has already been initialized.
        mAutoTiles = new AutoTileManager(context, this);

    }
    
    /*PRIZE-add an another Construction method, receive OnTileClickListener obj- liufan-2015-04-10-end*/

    public NextAlarmController getNextAlarmController() {
        return mNextAlarmController;
    }

    public void setHeaderView(View view) {
        mHeader = view;
    }

    public PhoneStatusBar getPhoneStatusBar() {
        return mStatusBar;
    }

    public void destroy() {
        mAutoTiles.destroy();
        TunerService.get(mContext).removeTunable(this);
    }

    @Override
    public void addCallback(Callback callback) {
        mCallbacks.add(callback);
    }

    @Override
    public void removeCallback(Callback callback) {
        mCallbacks.remove(callback);
    }

    @Override
    public Collection<QSTile<?>> getTiles() {
        return mTiles.values();
    }

    @Override
    public void startActivityDismissingKeyguard(final Intent intent) {
        mStatusBar.postStartActivityDismissingKeyguard(intent, 0);
    }

    @Override
    public void startActivityDismissingKeyguard(PendingIntent intent) {
        mStatusBar.postStartActivityDismissingKeyguard(intent);
    }

    @Override
    public void startRunnableDismissingKeyguard(Runnable runnable) {
        mStatusBar.postQSRunnableDismissingKeyguard(runnable);
    }

    @Override
    public void warn(String message, Throwable t) {
        // already logged
    }

    public void animateToggleQSExpansion() {
        // TODO: Better path to animated panel expansion.
        mHeader.callOnClick();
    }

    @Override
    public void collapsePanels() {
        mStatusBar.postAnimateCollapsePanels();
    }

    @Override
    public void openPanels() {
        mStatusBar.postAnimateOpenPanels();
    }

    @Override
    public Looper getLooper() {
        return mLooper;
    }

    @Override
    public Context getContext() {
        return mContext;
    }

    @Override
    public BluetoothController getBluetoothController() {
        return mBluetooth;
    }

    @Override
    public LocationController getLocationController() {
        return mLocation;
    }

    @Override
    public RotationLockController getRotationLockController() {
        return mRotation;
    }

    @Override
    public NetworkController getNetworkController() {
        return mNetwork;
    }

    @Override
    public ZenModeController getZenModeController() {
        return mZen;
    }

    @Override
    public HotspotController getHotspotController() {
        return mHotspot;
    }

    @Override
    public CastController getCastController() {
        return mCast;
    }

    @Override
    public FlashlightController getFlashlightController() {
        return mFlashlight;
    }

    @Override
    public KeyguardMonitor getKeyguardMonitor() {
        return mKeyguard;
    }

    @Override
    public UserSwitcherController getUserSwitcherController() {
        return mUserSwitcherController;
    }

    @Override
    public UserInfoController getUserInfoController() {
        return mUserInfoController;
    }

    @Override
    public BatteryController getBatteryController() {
        return mBattery;
    }

    public SecurityController getSecurityController() {
        return mSecurity;
    }

    public TileServices getTileServices() {
        return mServices;
    }

    public StatusBarIconController getIconController() {
        return mIconController;
    }

    public ManagedProfileController getManagedProfileController() {
        return mProfileController;
    }

    /// M: Add extra tiles in quicksetting @{
    // add HotKnot in quicksetting
    @Override
    public HotKnotController getHotKnotController() {
        return mHotKnot;
    }

    @Override
    public void onTuningChanged(String key, String newValue) {
        if (!TILES_SETTING.equals(key)) {
            return;
        }
        if (DEBUG) Log.d(TAG, "Recreating tiles");
        if (newValue == null && UserManager.isDeviceInDemoMode(mContext)) {
            newValue = mContext.getResources().getString(R.string.quick_settings_tiles_retail_mode);
        }
        final List<String> tileSpecs = loadTileSpecs(mContext, newValue);
        int currentUser = ActivityManager.getCurrentUser();
        if (tileSpecs.equals(mTileSpecs) && currentUser == mCurrentUser) return;
        for (Map.Entry<String, QSTile<?>> tile : mTiles.entrySet()) {
            if (!tileSpecs.contains(tile.getKey())) {
                if (DEBUG) Log.d(TAG, "Destroying tile: " + tile.getKey());
                tile.getValue().destroy();
            }
        }
        final LinkedHashMap<String, QSTile<?>> newTiles = new LinkedHashMap<>();
        for (String tileSpec : tileSpecs) {
            QSTile<?> tile = mTiles.get(tileSpec);
            if (tile != null && (!(tile instanceof CustomTile)
                    || ((CustomTile) tile).getUser() == currentUser)) {
                if (DEBUG) Log.d(TAG, "Adding " + tile);
                tile.removeCallbacks();
                if (!(tile instanceof CustomTile) && mCurrentUser != currentUser) {
                    tile.userSwitch(currentUser);
                }
                newTiles.put(tileSpec, tile);
            } else {
                if (DEBUG) Log.d(TAG, "Creating tile: " + tileSpec);
                try {
                    tile = createTile(tileSpec);
                    if (tile != null && tile.isAvailable()) {
                        tile.setTileSpec(tileSpec);
                        newTiles.put(tileSpec, tile);
                    }
                } catch (Throwable t) {
                    Log.w(TAG, "Error creating tile for spec: " + tileSpec, t);
                }
            }
        }
        mCurrentUser = currentUser;
        mTileSpecs.clear();
        mTileSpecs.addAll(tileSpecs);
        mTiles.clear();
        mTiles.putAll(newTiles);
        for (int i = 0; i < mCallbacks.size(); i++) {
            mCallbacks.get(i).onTilesChanged();
        }
    }
    /*PRIZE-create specify tile by tileSpec- liufan-2015-04-10-start*/
    /**
    * Method Description：create the specify tile
    * @param tileSpec which tile eg:wifi,bt,dataconnection,airplane,audioprofile,location,cleanupkey,rotation,brightness,power,cell,dormancy,screenshot,lockscreen,more
    */
    private QSTile<?> createSpecifyTile(String tileSpec) {
        IQuickSettingsPlugin quickSettingsPlugin = PluginManager.getQuickSettingsPlugin(mContext);
        if (tileSpec.equals("wifi")) {
            //String label = mContext.getString(R.string.quick_settings_wifi_label);
            //return new BaseTile(this,label,R.drawable.ic_qs_wifi_on,R.drawable.ic_qs_wifi_off,onTileClickListener,tileSpec);
            return new WifiTile(this);
        } else if (tileSpec.equals("bt")){
            //String label = mContext.getString(R.string.quick_settings_bluetooth_label);
            //return new BaseTile(this,label,R.drawable.ic_qs_bt_on,R.drawable.ic_qs_bt_off,onTileClickListener,tileSpec);
            return new BluetoothTile(this);
        } else if (tileSpec.equals("dataconnection")) {
            //String label = mContext.getString(R.string.mobile);
            //return new BaseTile(this,label,R.drawable.ic_qs_dataconnection_on,R.drawable.ic_qs_dataconnection_off,onTileClickListener,tileSpec);
            return new DataConnectionTileDefined(this);
        } else if(tileSpec.equals("airplane") ){
            //String label = mContext.getString(R.string.quick_settings_airplane_mode_label);
            //return new BaseTile(this,label,R.drawable.ic_qs_airplane_on,R.drawable.ic_qs_airplane_off,onTileClickListener,tileSpec);
            return new AirplaneModeTile(this);
        } else if(tileSpec.equals("audioprofile") ){
            //String label = mContext.getString(R.string.audio_profile);
            return new BaseTile(this,R.string.audio_profile,R.drawable.ic_qs_audioprofile_normal_on,R.drawable.ic_qs_audioprofile_normal_off,onTileClickListener,tileSpec);
            //return new AudioProfileTileDefined(this);
        } else if(tileSpec.equals("cell") ){
            //String label = mContext.getString(R.string.quick_settings_cell_label);
            //return new BaseTile(this,label,R.drawable.ic_qs_cell_on,R.drawable.ic_qs_cell_off,onTileClickListener,tileSpec);
            //boolean displayDataUsage = quickSettingsPlugin.customizeDisplayDataUsage(false);
            //if (displayDataUsage) {
            return new DataUsageTileDefined(this);
            //} else {
            //    return new CellularTileDefined(this);
            //}
        } else if(tileSpec.equals("rotation") ){
            //String label = mContext.getString(R.string.quick_settings_rotation_unlocked_label);
            //return new BaseTile(this,label,R.drawable.ic_qs_rotation_on,R.drawable.ic_qs_rotation_off,onTileClickListener,tileSpec);
            return new RotationLockTile(this);
        } else if(tileSpec.equals("flashlight") ){
            //String label = mContext.getString(R.string.quick_settings_flashlight_label);
            //return new BaseTile(this,label,R.drawable.ic_qs_flashlight_on,R.drawable.ic_qs_flashlight_off,onTileClickListener,tileSpec);
            return new FlashlightTileDefined(this);/*PRIZE-added by pengcancan for flash --20161116-start*/
        } else if(tileSpec.equals("location") ){
            return new BaseTile(this,R.string.quick_settings_location_label,R.drawable.ic_qs_location_on,R.drawable.ic_qs_location_off,onTileClickListener,tileSpec);
        } else if(tileSpec.equals("gps") ){
            //String label = mContext.getString(R.string.quick_settings_gps_label);
            //return new BaseTile(this,label,R.drawable.ic_qs_gps_on,R.drawable.ic_qs_gps_off,onTileClickListener,tileSpec);
            return new GPSTileDefined(this);
        } else if(tileSpec.equals("cast") ){
            return new BaseTile(this,R.string.quick_settings_cast_title,R.drawable.ic_qs_cast_on,R.drawable.ic_qs_cast_off,onTileClickListener,tileSpec);
        } else if(tileSpec.equals("hotknot") ){
            return new BaseTile(this,R.string.quick_settings_hotspot_label,R.drawable.ic_qs_hotknot_on,R.drawable.ic_qs_hotknot_off,onTileClickListener,tileSpec);
        } else if(tileSpec.equals("screenshot") ){
            return new BaseTile(this,R.string.quick_settings_screenshot_label,R.drawable.ic_qs_screenshot_on,R.drawable.ic_qs_screenshot_on,onTileClickListener,tileSpec);
	/**shiyicheng-add-for-supershot-2015-11-03-start*/
	 } else if(tileSpec.equals("superscreenshot") ){
            return new BaseTile(this,R.string.quick_settings_superscreenshot_label,R.drawable.ic_qs_screenshot_on,R.drawable.ic_qs_screenshot_on,onTileClickListener,tileSpec);
	/**shiyicheng-add-for-supershot-2015-11-03-end*/

	 } else if(tileSpec.equals("lockscreen") ){
            return new BaseTile(this,R.string.quick_settings_lockscreen_label,R.drawable.ic_qs_lockscreen_on,R.drawable.ic_qs_lockscreen_on,onTileClickListener,tileSpec);
        } else if (tileSpec.equals("speedup")) {
            return new PrizeSpeedUpTile(this, onTileClickListener, tileSpec);
        } else if(tileSpec.equals("cleanupkey") ){
	        /*prize modify by xiarui for disable cleanupkey when in MultiWindowMode, 2017-11-23 start*/
	        return new OneCleanUpTile(this, onTileClickListener, tileSpec);
            //return new BaseTile(this,R.string.quick_settings_cleanupkey_label,R.drawable.ic_qs_cleanupkey_selector,R.drawable.ic_qs_cleanupkey_off,onTileClickListener,tileSpec);
            /*prize modify by xiarui for disable cleanupkey when in MultiWindowMode, 2017-11-23 end*/
        } else if(tileSpec.equals("brightness") ){
            return new BrightnessTileDefined(this,R.string.quick_settings_brightness_label,R.drawable.ic_qs_brightness_on,R.drawable.ic_qs_brightness_off,onTileClickListener,tileSpec);
        } else if(tileSpec.equals("power") ){
            String label = mContext.getString(R.string.quick_settings_power_label);
            //Log.e("liufan","mBatteryLevel---->"+mBatteryLevel+", mBatteryMeterView---->"+mBatteryMeterView+", mBatteryController---->"+mBatteryController+", BluetoothController--->"+mBluetooth);
            return new BatteryTile(this);
			//return new BatteryTile(this,label,R.drawable.ic_qs_power_on,R.drawable.ic_qs_power_off,onTileClickListener,tileSpec,this.mBatteryLevel,null,this.mBatteryController);
        /*PRIZE-Add for BluLight-zhudaopeng-2017-05-10-Start*/
        } else if(tileSpec.equals("blulight")){            
            return new PrizeBluLightTileDefined(this,R.string.quick_settings_blulight_mode,R.drawable.ic_qs_blulight_on,R.drawable.ic_qs_blulight_off, onTileClickListener,tileSpec);
        /*PRIZE-Add for BluLight-zhudaopeng-2017-05-10-End*/
        } else if(tileSpec.equals("dormancy") ){
            return new DormancyTileDefined(this,R.string.quick_settings_dormancy_label,R.drawable.ic_qs_dormancy_on,R.drawable.ic_qs_dormancy_off,onTileClickListener,tileSpec);
        } else if(tileSpec.equals("more") ){
            return new BaseTile(this,R.string.quick_settings_more_label,R.drawable.ic_qs_more_on,R.drawable.ic_qs_more_on,onTileClickListener,tileSpec);
        }else if(tileSpec.equals("prizerings") ){
            //prize modify by xiarui 2018-06-05 vibrate in silent @{
            if (PrizeOption.PRIZE_VIBRATE_CONTROL) {
                return new PrizeVibrateInSilentTile(this);
            } else {
                return new PrizeRings(this);
            }
            //@}
		}
        else return null;
    }
    /*PRIZE-create specify tile by tileSpec- liufan-2015-04-10-end*/

    @Override
    public void removeTile(String tileSpec) {
        ArrayList<String> specs = new ArrayList<>(mTileSpecs);
        specs.remove(tileSpec);
        Settings.Secure.putStringForUser(mContext.getContentResolver(), TILES_SETTING,
                TextUtils.join(",", specs), ActivityManager.getCurrentUser());
    }

    public void addTile(String spec) {
        final String setting = Settings.Secure.getStringForUser(mContext.getContentResolver(),
                TILES_SETTING, ActivityManager.getCurrentUser());
        final List<String> tileSpecs = loadTileSpecs(mContext, setting);
        if (tileSpecs.contains(spec)) {
            return;
        }
        tileSpecs.add(spec);
        Settings.Secure.putStringForUser(mContext.getContentResolver(), TILES_SETTING,
                TextUtils.join(",", tileSpecs), ActivityManager.getCurrentUser());
    }

    public void addTile(ComponentName tile) {
        List<String> newSpecs = new ArrayList<>(mTileSpecs);
        newSpecs.add(0, CustomTile.toSpec(tile));
        changeTiles(mTileSpecs, newSpecs);
    }

    public void removeTile(ComponentName tile) {
        List<String> newSpecs = new ArrayList<>(mTileSpecs);
        newSpecs.remove(CustomTile.toSpec(tile));
        changeTiles(mTileSpecs, newSpecs);
    }

    public void changeTiles(List<String> previousTiles, List<String> newTiles) {
        final int NP = previousTiles.size();
        final int NA = newTiles.size();
        for (int i = 0; i < NP; i++) {
            String tileSpec = previousTiles.get(i);
            if (!tileSpec.startsWith(CustomTile.PREFIX)) continue;
            if (!newTiles.contains(tileSpec)) {
                ComponentName component = CustomTile.getComponentFromSpec(tileSpec);
                Intent intent = new Intent().setComponent(component);
                TileLifecycleManager lifecycleManager = new TileLifecycleManager(new Handler(),
                        mContext, mServices, new Tile(), intent,
                        new UserHandle(ActivityManager.getCurrentUser()));
                lifecycleManager.onStopListening();
                lifecycleManager.onTileRemoved();
                TileLifecycleManager.setTileAdded(mContext, component, false);
                lifecycleManager.flushMessagesAndUnbind();
            }
        }
        if (DEBUG) Log.d(TAG, "saveCurrentTiles " + newTiles);
        Secure.putStringForUser(getContext().getContentResolver(), QSTileHost.TILES_SETTING,
                TextUtils.join(",", newTiles), ActivityManager.getCurrentUser());
    }

    public QSTile<?> createTile(String tileSpec) {
        QSTile<?> tile = createSpecifyTile(tileSpec);
        if(FeatureOption.PRIZE_QS_SORT && tile !=null){
            return tile;
        }
        IQuickSettingsPlugin quickSettingsPlugin = PluginManager
                .getQuickSettingsPlugin(mContext);
        if (tileSpec.equals("wifi")) return new WifiTile(this);
        else if (tileSpec.equals("bt")) return new BluetoothTile(this);
        else if (tileSpec.equals("cell")) return new CellularTile(this);
        else if (tileSpec.equals("dnd")) return new DndTile(this);
        else if (tileSpec.equals("inversion")) return new ColorInversionTile(this);
        else if (tileSpec.equals("airplane")) return new AirplaneModeTile(this);
        else if (tileSpec.equals("work")) return new WorkModeTile(this);
        else if (tileSpec.equals("rotation")) return new RotationLockTile(this);
        else if (tileSpec.equals("flashlight")) return new FlashlightTile(this);
        else if (tileSpec.equals("location")) return new LocationTile(this);
        else if (tileSpec.equals("cast")) return new CastTile(this);
        else if (tileSpec.equals("hotspot")) return new HotspotTile(this);
        else if (tileSpec.equals("user")) return new UserTile(this);
        else if (tileSpec.equals("battery")) return new BatteryTile(this);
        else if (tileSpec.equals("saver")) return new DataSaverTile(this);
        else if (tileSpec.equals("night")) return new NightDisplayTile(this);

        /// M: Add extra tiles in quicksetting @{
        else if (tileSpec.equals("hotknot") && SIMHelper.isMtkHotKnotSupport())
            return new HotKnotTile(this);
        // /@}
        /// M: add DataConnection in quicksetting @{
        else if (tileSpec.equals("dataconnection") && !SIMHelper.isWifiOnlyDevice())
            return new MobileDataTile(this);
        /// M: add DataConnection in quicksetting @}
        /// M: Customize the quick settings tiles for operator. @{
        else if (tileSpec.equals("simdataconnection") && !SIMHelper.isWifiOnlyDevice() &&
                quickSettingsPlugin.customizeAddQSTile(new SimDataConnectionTile(this)) != null) {
            return (SimDataConnectionTile) quickSettingsPlugin.customizeAddQSTile(
                    new SimDataConnectionTile(this));
        } else if (tileSpec.equals("dulsimsettings") && !SIMHelper.isWifiOnlyDevice() &&
                quickSettingsPlugin.customizeAddQSTile(new DualSimSettingsTile(this)) != null) {
            return (DualSimSettingsTile) quickSettingsPlugin.customizeAddQSTile(
                    new DualSimSettingsTile(this));
        } else if (tileSpec.equals("apnsettings") && !SIMHelper.isWifiOnlyDevice() &&
                quickSettingsPlugin.customizeAddQSTile(new ApnSettingsTile(this)) != null) {
            return (ApnSettingsTile) quickSettingsPlugin.customizeAddQSTile(
                    new ApnSettingsTile(this));
        } else if (quickSettingsPlugin.doOperatorSupportTile(tileSpec)) {
            // WifiCalling
            return (QSTile<?>) quickSettingsPlugin.createTile(this, tileSpec);
        }
        /// @}

        // Intent tiles.
        else if (tileSpec.startsWith(IntentTile.PREFIX)) return IntentTile.create(this,tileSpec);
        else if (tileSpec.startsWith(CustomTile.PREFIX)) return CustomTile.create(this,tileSpec);
        else {
            Log.w(TAG, "Bad tile spec: " + tileSpec);
            return null;
        }
    }

    protected List<String> loadTileSpecs(Context context, String tileList) {
        final Resources res = context.getResources();
        String defaultTileList = res.getString(R.string.quick_settings_tiles_default);
        /*PRIZE-use new config on the quick settings- liufan-2015-04-10-start*/
        //if(FeatureOption.PRIZE_QS_SORT){
        //    defaultTileList = res.getString(R.string.quick_settings_tiles_new_config);
        //}
        Log.d(TAG, "defaultTileList---->" + defaultTileList);
        /*PRIZE-use new config on the quick settings- liufan-2015-04-10-end*/
        /**PRIZE remove the effects of the quick settings layout by OP01liyao-2015-06-03 start*/
        /*
        /// M: Customize the quick settings tile order for operator. @{
        IQuickSettingsPlugin quickSettingsPlugin = PluginManager.getQuickSettingsPlugin(mContext);
        defaultTileList = quickSettingsPlugin.addOpTileSpecs(defaultTileList);
        // @}
        defaultTileList = quickSettingsPlugin.customizeQuickSettingsTileOrder(defaultTileList);
        /// M: Customize the quick settings tile order for operator. @}
        */
        /**PRIZE remove the effects of the quick settings layout by OP01 liyao-2015-06-03 end*/
        Log.d(TAG, "loadTileSpecs() default tile list: " + defaultTileList);

        if (tileList == null) {
            tileList = res.getString(R.string.quick_settings_tiles);
             Log.d(TAG, "Loaded tile specs from config: " + tileList);
        } else {
             Log.d(TAG, "Loaded tile specs from setting: " + tileList);
        }
			/*if(tileList.isEmpty()){
			 
        Settings.Secure.putStringForUser(mContext.getContentResolver(), TILES_SETTING,
                defaultTileList, ActivityManager.getCurrentUser());
			 }*/
        final ArrayList<String> tiles = new ArrayList<String>();
        boolean addedDefault = false;
        for (String tile : tileList.split(",")) {
            tile = tile.trim();
            if (tile.isEmpty()) continue;
            if (tile.equals("default")) {
                if (!addedDefault) {
                    tiles.addAll(Arrays.asList(defaultTileList.split(",")));
                    addedDefault = true;
                }
            } else {
                tiles.add(tile);
            }
        }
        return tiles;
    }
}
