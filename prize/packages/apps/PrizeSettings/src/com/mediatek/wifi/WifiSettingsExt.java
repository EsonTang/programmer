package com.mediatek.wifi;

import android.content.ContentResolver;
import android.content.Context;
import android.net.NetworkInfo.DetailedState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;

import static android.net.wifi.WifiConfiguration.INVALID_NETWORK_ID;

import com.android.settingslib.wifi.AccessPoint;
import com.android.settings.SettingsPreferenceFragment;
import com.mediatek.settings.ext.IWifiSettingsExt;
import com.mediatek.settings.UtilsExt;

public class WifiSettingsExt {
    private static final String TAG = "WifiSettingsExt";
    private static final int MENU_ID_DISCONNECT = Menu.FIRST + 100;

    // add for plug in
    private IWifiSettingsExt mExt;
    private Context mActivity;

    public WifiSettingsExt(Context context) {
        mActivity = context;
    }

    public void onCreate() {
        // get plug in
        mExt = UtilsExt.getWifiSettingsPlugin(mActivity);
    }

    public void onActivityCreated(SettingsPreferenceFragment fragment, WifiManager wifiManager) {
        // register priority observer
        mExt.registerPriorityObserver(mActivity.getContentResolver());
        mExt.addCategories(fragment.getPreferenceScreen());
    }

    public void updatePriority() {
        // update priority after connnect AP
        Log.d(TAG, "mConnectListener or mSaveListener");
        mExt.updatePriority();
    }

    public void onResume() {
        // update priority when resume
        mExt.updatePriority();
    }

    /**
     * 1. add cmcc disconnect menu 2. WPS NFC feature
     *
     * @param menu
     * @param state
     * @param accessPoint
     */
    public void onCreateContextMenu(ContextMenu menu, DetailedState state
            , AccessPoint accessPoint) {
        // current connected AP, add a disconnect option to it
        mExt.updateContextMenu(menu, MENU_ID_DISCONNECT, state);
    }

    public boolean onContextItemSelected(MenuItem item, WifiConfiguration wifiConfig) {
        switch (item.getItemId()) {
        case MENU_ID_DISCONNECT:
            mExt.disconnect(wifiConfig);
            return true;
        default:
            break;
        }
        return false;
    }

    public void recordPriority(WifiConfiguration config) {
        // record priority of selected ap
        if (config != null) {
            // store the former priority value before user modification
            mExt.recordPriority(config.priority);
        } else {
            // the last added AP will have highest priority, mean all other
            // AP's priority will be adjusted, the same as adjust this new
            // added one's priority from lowest to highest
            mExt.recordPriority(-1);
        }
    }

    public void submit(WifiConfiguration config, AccessPoint accessPoint, DetailedState state) {
        Log.d(TAG, "submit, config = " + config);
        if (config == null) {
            /*
             * if (accessPoint != null && networkId != INVALID_NETWORK_ID &&
             * state != null) { Log.d(TAG, "submit, disconnect, networkId = " +
             * networkId); mExt.disconnect(networkId); }
             */
        } else if (config.networkId != INVALID_NETWORK_ID && accessPoint != null) {
            // save priority
            Log.d(TAG, "submit, setNewPriority");
            mExt.setNewPriority(config);
        } else {
            // update priority
            Log.d(TAG, "submit, updatePriorityAfterSubmit");
            mExt.updatePriorityAfterSubmit(config);
        }
        // set last connected config
        Log.d(TAG, "submit, setLastConnectedConfig");
        mExt.setLastConnectedConfig(config);
    }

    public void unregisterPriorityObserver(ContentResolver cr) {
        mExt.unregisterPriorityObserver(cr);
    }

    public void addPreference(PreferenceScreen screen, Preference preference, boolean isConfiged) {
        mExt.addPreference(screen, preference, isConfiged);
    }

    public void emptyCategory(PreferenceScreen screen) {
        mExt.emptyCategory(screen);
    }

    public void emptyScreen(PreferenceScreen screen) {
        mExt.emptyScreen(screen);
    }

    public void refreshCategory(PreferenceScreen screen) {
        mExt.refreshCategory(screen);
    }
}
