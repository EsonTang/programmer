package com.mediatek.settings.ext;

import android.app.Activity;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.widget.ImageView;
/**
 *  the class for settings misc feature plugin.
 */
public interface ISettingsMiscExt {

    /**
    * Customize strings which contains 'SIM', replace 'SIM' by
    * 'UIM/SIM','UIM','card' etc.
    * @param simString : the strings which contains SIM
    * @param soltId : 1 , slot1 0, slot0 , -1 means always.
    * @internal
    */
    String customizeSimDisplayString(String simString, int slotId);

    /**
     * Add the operator customize settings in Settings->Location
     * @param pref: the root preferenceScreen
     * @param order: the customize settings preference order
     * @internal
     */
    void initCustomizedLocationSettings(PreferenceScreen root, int order);

    /**
     * Add the operator customize settings in Settings->Location
     * Update customize settings when location mode changed
     * @internal
     */
    void updateCustomizedLocationSettings();

    /**Customize the title of factory reset settings.
     * @param obj header or activity
     * @internal
     */
    void setFactoryResetTitle(Object obj);

    /**Customize the title of screen timeout preference.
     * @param pref the screen timeout preference
     * @internal
     */
    void setTimeoutPrefTitle(Preference pref);

    /**
     * Add customize item in settings.
     * @param targetDashboardCategory header list in settings,
     *  set to object so that settings.ext do not depend on settings
     * @param add whether add operator dashboard tile
     * @internal
     */
    void addCustomizedItem(Object targetDashboardCategory, Boolean add);

    /**
     * Customize add item drawable and location.
     * @param tile the new DashboardTile which create in CT will add intent.extra.
     * @param imageView for dashboardTile imageView set the drawable
     * @internal
     */
    void customizeDashboardTile(Object tile, ImageView imageView);

    /**
     * Returns if wifi-only mode is set.
     * @return boolean
     * @internal
     */
    boolean isWifiOnlyModeSet();

    /**
     * Customize the NetworkType String for 4G.
     * @param defaultString Host String
     * @param subId SubscriptionId
     * @return              Custom String
     * @internal
     */
    String getNetworktypeString(String defaultString, int subId);

    /**
     * Customize MacAddressString.
     * For OP09 A and C feature.
     * If wifi is virtual wifi return Unavailable;
     * When first time boot phone,
     * wifi do not open, will show virtual wifi.
     * The virtual wifi is "02:00:00:00:00:00".
     * but when your open wifi and close it.
     * The wifi is return actual wifi.
     * @param macAddressString : MacAddress String
     * @param unavailable : Unavailable
     * @return Unavailable if OP09 project, other return self.
     * @internal
     */
     String customizeMacAddressString(String macAddressString, String unavailable);

    /**
     * Give plugin a chance to update tiles list
     * @param activity call its setTileEnabled(component, enabled) method to update tile
     * @param isAdmin If current user is admin or not
     */
    void doUpdateTilesList(Activity activity, boolean isAdmin);
}
