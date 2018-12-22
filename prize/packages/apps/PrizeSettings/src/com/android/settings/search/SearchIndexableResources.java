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

package com.android.settings.search;

import android.provider.SearchIndexableResource;
import com.android.settings.DateTimeSettings;
import com.android.settings.DevelopmentSettings;
import com.android.settings.DeviceInfoSettings;
import com.android.settings.DisplaySettings;
import com.android.settings.LegalSettings;
import com.android.settings.PrivacySettings;
import com.android.settings.R;
import com.android.settings.ScreenPinningSettings;
import com.android.settings.SecuritySettings;
import com.android.settings.WallpaperTypeSettings;
import com.android.settings.WirelessSettings;
import com.android.settings.accessibility.AccessibilitySettings;
import com.android.settings.accounts.AccountSettings;
import com.android.settings.applications.AdvancedAppSettings;
import com.android.settings.applications.SpecialAccessSettings;
import com.android.settings.bluetooth.BluetoothSettings;
import com.android.settings.datausage.DataUsageMeteredSettings;
import com.android.settings.datausage.DataUsageSummary;
import com.android.settings.deviceinfo.StorageSettings;
import com.android.settings.display.ScreenZoomSettings;
import com.android.settings.fuelgauge.BatterySaverSettings;
import com.android.settings.fuelgauge.PowerUsageSummary;
import com.android.settings.inputmethod.InputMethodAndLanguageSettings;
import com.android.settings.location.LocationSettings;
import com.android.settings.location.ScanningSettings;
import com.android.settings.notification.ConfigureNotificationSettings;
import com.android.settings.notification.OtherSoundSettings;
import com.android.settings.notification.SoundSettings;
import com.android.settings.notification.ZenModePrioritySettings;
import com.android.settings.notification.ZenModeSettings;
import com.android.settings.notification.ZenModeVisualInterruptionSettings;
import com.android.settings.print.PrintSettingsFragment;
import com.android.settings.sim.SimSettings;
import com.android.settings.users.UserSettings;
import com.android.settings.wifi.AdvancedWifiSettings;
import com.android.settings.wifi.SavedAccessPointsWifiSettings;
import com.android.settings.wifi.WifiSettings;

import com.mediatek.audioprofile.SoundEnhancement;
import com.mediatek.nfc.NfcSettings;
import com.mediatek.search.PrizeFeedBack;
import com.mediatek.search.PrizeHBHelper;
import com.mediatek.search.PrizeSystemUpdate;
import com.mediatek.search.SearchExt;
import com.mediatek.settings.hotknot.HotKnotSettings;

import java.util.Collection;
import java.util.HashMap;
/// add new menu to search db lijimeng 20161220 start
import com.android.settings.PrizeApplicationManagementSettings;
import com.android.settings.PrizeOtherSettings;
import com.android.settings.TetherSettings;
import com.android.settings.notification.ZenModeAutomationSettings;
import com.android.settings.PrizeNotificationCentreActivity;
import com.android.settings.applications.ProcessStatsSummary;
import com.android.settings.PrizeManageAppInstances;
import com.android.settings.accounts.AccountSettings;
import com.android.settings.PrizeIntelligentSettings;
import com.android.settings.PrizeNoticeStatusBarSettings;
import com.android.settings.PrizeWallpaperLockscreenSettings;
/// add new menu to search db lijimeng 20161220 end
import com.android.settings.PrizeFingerprintOperationSettings;
import com.android.settings.PrizeOldLauncher;
import com.android.settings.PrizeFaceOperationSettings;
public final class SearchIndexableResources {

    public static int NO_DATA_RES_ID = 0;

    private static HashMap<String, SearchIndexableResource> sResMap =
            new HashMap<String, SearchIndexableResource>();

    static {
        sResMap.put(WifiSettings.class.getName(),
                new SearchIndexableResource(
                        Ranking.getRankForClassName(WifiSettings.class.getName()),
                        NO_DATA_RES_ID,
                        WifiSettings.class.getName(),
                        R.drawable.ic_settings_wireless_prize_v7));//R.drawable.ic_settings_wireless modify by ljm 20161220

        sResMap.put(AdvancedWifiSettings.class.getName(),
                new SearchIndexableResource(
                        Ranking.getRankForClassName(AdvancedWifiSettings.class.getName()),
                        R.xml.wifi_advanced_settings,
                        AdvancedWifiSettings.class.getName(),
                        R.drawable.ic_settings_wireless));

        sResMap.put(SavedAccessPointsWifiSettings.class.getName(),
                new SearchIndexableResource(
                        Ranking.getRankForClassName(SavedAccessPointsWifiSettings.class.getName()),
                        R.xml.wifi_display_saved_access_points,
                        SavedAccessPointsWifiSettings.class.getName(),
                        R.drawable.ic_settings_wireless));

        sResMap.put(BluetoothSettings.class.getName(),
                new SearchIndexableResource(
                        Ranking.getRankForClassName(BluetoothSettings.class.getName()),
                        NO_DATA_RES_ID,
                        BluetoothSettings.class.getName(),
                        R.drawable.ic_settings_bluetooth_prize_v8)); // R.drawable.ic_settings_bluetooth moidfy by ljm 20161220

        sResMap.put(SimSettings.class.getName(),
                new SearchIndexableResource(
                        Ranking.getRankForClassName(SimSettings.class.getName()),
                        R.xml.sim_settings, // NO_DATA_RES_ID moidfy by ljm 20161220
                        SimSettings.class.getName(),
                        R.drawable.ic_settings_network_prize)); // R.drawable.ic_sim_sd moidfy by ljm 20161220

//        sResMap.put(DataUsageSummary.class.getName(),
//                new SearchIndexableResource(
//                        Ranking.getRankForClassName(DataUsageSummary.class.getName()),
//                        NO_DATA_RES_ID,
//                        DataUsageSummary.class.getName(),
//                        R.drawable.ic_settings_data_usage_prize_v7)); // R.drawable.ic_settings_data_usage moidfy by ljm 20161220

        sResMap.put(DataUsageMeteredSettings.class.getName(),
                new SearchIndexableResource(
                        Ranking.getRankForClassName(DataUsageMeteredSettings.class.getName()),
                        NO_DATA_RES_ID,
                        DataUsageMeteredSettings.class.getName(),
                        R.drawable.ic_settings_data_usage_prize_v7)); // R.drawable.ic_settings_data_usage moidfy by ljm 20161220

        sResMap.put(WirelessSettings.class.getName(),
                new SearchIndexableResource(
                        Ranking.getRankForClassName(WirelessSettings.class.getName()),
                        NO_DATA_RES_ID,
                        WirelessSettings.class.getName(),
                        R.drawable.ic_settings_otherwireless_prize)); // R.drawable.ic_settings_more moidfy by ljm 20161220
		// already display in xml modify by ljm 20161220
        // sResMap.put(ScreenZoomSettings.class.getName(),
                // new SearchIndexableResource(
                        // Ranking.getRankForClassName(ScreenZoomSettings.class.getName()),
                        // NO_DATA_RES_ID,
                        // ScreenZoomSettings.class.getName(),
                        // R.drawable.ic_settings_display_prize));

        sResMap.put(DisplaySettings.class.getName(),
                new SearchIndexableResource(
                        Ranking.getRankForClassName(DisplaySettings.class.getName()),
                        NO_DATA_RES_ID,
                        DisplaySettings.class.getName(),
                        R.drawable.ic_settings_display_brightness_prize));

        // sResMap.put(WallpaperTypeSettings.class.getName(),
                // new SearchIndexableResource(
                        // Ranking.getRankForClassName(WallpaperTypeSettings.class.getName()),
                        // NO_DATA_RES_ID,
                        // WallpaperTypeSettings.class.getName(),
                        // R.drawable.ic_settings_display_prize));

        sResMap.put(ConfigureNotificationSettings.class.getName(),
                new SearchIndexableResource(
                        Ranking.getRankForClassName(ConfigureNotificationSettings.class.getName()),
                        R.xml.configure_notification_settings,
                        ConfigureNotificationSettings.class.getName(),
                        R.drawable.ic_settings_notifications));

        sResMap.put(SoundSettings.class.getName(),
                new SearchIndexableResource(
                        Ranking.getRankForClassName(SoundSettings.class.getName()),
                        NO_DATA_RES_ID,
                        SoundSettings.class.getName(),
                        R.drawable.ic_settings_sound_prize)); // All R.drawable.ic_settings_sound moidfy by ljm 20161220

        sResMap.put(OtherSoundSettings.class.getName(),
                new SearchIndexableResource(
                        Ranking.getRankForClassName(OtherSoundSettings.class.getName()),
                        NO_DATA_RES_ID,
                        OtherSoundSettings.class.getName(),
                        R.drawable.ic_settings_sound_prize));

        /// M: Add SoundEnhancement when AudioProfile support @{
        sResMap.put(SoundEnhancement.class.getName(),
                new SearchIndexableResource(
                        Ranking.getRankForClassName(SoundEnhancement.class.getName()),
                        NO_DATA_RES_ID,
                        SoundEnhancement.class.getName(),
                        R.drawable.ic_settings_sound_prize));
        /// @}

        sResMap.put(ZenModeSettings.class.getName(),
                new SearchIndexableResource(
                        Ranking.getRankForClassName(ZenModeSettings.class.getName()),
						NO_DATA_RES_ID, // R.xml.zen_mode_settings moidfy by ljm 20161220
                        ZenModeSettings.class.getName(),
                        R.drawable.ic_settings_zen_prize)); // All R.drawable.ic_settings_notifications moidfy by ljm 20161220

        sResMap.put(ZenModePrioritySettings.class.getName(),
                new SearchIndexableResource(
                        Ranking.getRankForClassName(ZenModePrioritySettings.class.getName()),
                        R.xml.zen_mode_priority_settings,
                        ZenModePrioritySettings.class.getName(),
                        R.drawable.ic_settings_zen_prize));

        sResMap.put(StorageSettings.class.getName(),
                new SearchIndexableResource(
                        Ranking.getRankForClassName(StorageSettings.class.getName()),
                        NO_DATA_RES_ID,
                        StorageSettings.class.getName(),
                        R.drawable.ic_settings_others)); //  R.drawable.ic_settings_storage moidfy by ljm 20161220

        sResMap.put(PowerUsageSummary.class.getName(),
                new SearchIndexableResource(
                        Ranking.getRankForClassName(PowerUsageSummary.class.getName()),
                        R.xml.power_usage_summary,
                        PowerUsageSummary.class.getName(),
                        R.drawable.ic_settings_battery_prize_v7)); // All R.drawable.ic_settings_battery moidfy by ljm 20161220

        /*sResMap.put(BatterySaverSettings.class.getName(),
                new SearchIndexableResource(
                        Ranking.getRankForClassName(BatterySaverSettings.class.getName()),
                        R.xml.battery_saver_settings,
                        BatterySaverSettings.class.getName(),
                        R.drawable.ic_settings_battery_prize_v7));*/

        sResMap.put(AdvancedAppSettings.class.getName(),
                new SearchIndexableResource(
                        Ranking.getRankForClassName(AdvancedAppSettings.class.getName()),
                        NO_DATA_RES_ID,
                        AdvancedAppSettings.class.getName(),
                        R.drawable.ic_settings_applications));

        sResMap.put(SpecialAccessSettings.class.getName(),
                new SearchIndexableResource(
                        Ranking.getRankForClassName(SpecialAccessSettings.class.getName()),
                        R.xml.special_access,
                        SpecialAccessSettings.class.getName(),
                        R.drawable.ic_settings_applications));

        sResMap.put(UserSettings.class.getName(),
                new SearchIndexableResource(
                        Ranking.getRankForClassName(UserSettings.class.getName()),
                        NO_DATA_RES_ID,
                        UserSettings.class.getName(),
                        R.drawable.ic_settings_multiuser));

        sResMap.put(LocationSettings.class.getName(),
                new SearchIndexableResource(
                        Ranking.getRankForClassName(LocationSettings.class.getName()),
                        R.xml.location_settings,
                        LocationSettings.class.getName(),
                        R.drawable.ic_settings_location));

        sResMap.put(ScanningSettings.class.getName(),
                new SearchIndexableResource(
                        Ranking.getRankForClassName(ScanningSettings.class.getName()),
                        R.xml.location_scanning,
                        ScanningSettings.class.getName(),
                        R.drawable.ic_settings_location));

        sResMap.put(SecuritySettings.class.getName(),
                new SearchIndexableResource(
                        Ranking.getRankForClassName(SecuritySettings.class.getName()),
                        NO_DATA_RES_ID,
                        SecuritySettings.class.getName(),
                        R.drawable.ic_settings_security_prize_v7)); // All  R.drawable.ic_settings_battery moidfy by ljm 20161220

        sResMap.put(ScreenPinningSettings.class.getName(),
                new SearchIndexableResource(
                        Ranking.getRankForClassName(ScreenPinningSettings.class.getName()),
                        NO_DATA_RES_ID,
                        ScreenPinningSettings.class.getName(),
                        R.drawable.ic_settings_security_prize_v7));

        sResMap.put(AccountSettings.class.getName(),
                new SearchIndexableResource(
                        Ranking.getRankForClassName(AccountSettings.class.getName()),
                        NO_DATA_RES_ID,
                        AccountSettings.class.getName(),
                        R.drawable.ic_settings_accounts));

        sResMap.put(InputMethodAndLanguageSettings.class.getName(),
                new SearchIndexableResource(
                        Ranking.getRankForClassName(InputMethodAndLanguageSettings.class.getName()),
                        NO_DATA_RES_ID,
                        InputMethodAndLanguageSettings.class.getName(),
                        R.drawable.ic_settings_others)); // R.drawable.ic_settings_language modify by lijimeng 20161220

        sResMap.put(PrivacySettings.class.getName(),
                new SearchIndexableResource(
                        Ranking.getRankForClassName(PrivacySettings.class.getName()),
                        NO_DATA_RES_ID,
                        PrivacySettings.class.getName(),
                        R.drawable.ic_settings_others)); // R.drawable.ic_settings_backup modify by lijimeng 20161220

        sResMap.put(DateTimeSettings.class.getName(),
                new SearchIndexableResource(
                        Ranking.getRankForClassName(DateTimeSettings.class.getName()),
                        R.xml.date_time_prefs,
                        DateTimeSettings.class.getName(),
                        R.drawable.ic_settings_others)); // R.drawable.ic_settings_date_time modify by lijimeng 20161220

        sResMap.put(AccessibilitySettings.class.getName(),
                new SearchIndexableResource(
                        Ranking.getRankForClassName(AccessibilitySettings.class.getName()),
                        NO_DATA_RES_ID,
                        AccessibilitySettings.class.getName(),
                        R.drawable.ic_settings_others)); // R.drawable.ic_settings_accessibility modify by lijimeng 20161220

        sResMap.put(PrintSettingsFragment.class.getName(),
                new SearchIndexableResource(
                        Ranking.getRankForClassName(PrintSettingsFragment.class.getName()),
                        NO_DATA_RES_ID,
                        PrintSettingsFragment.class.getName(),
                        R.drawable.ic_settings_others)); // R.drawable.ic_settings_print modify by lijimeng 20161220

        // sResMap.put(DevelopmentSettings.class.getName(),
                // new SearchIndexableResource(
                        // Ranking.getRankForClassName(DevelopmentSettings.class.getName()),
                        // NO_DATA_RES_ID,
                        // DevelopmentSettings.class.getName(),
                        // R.drawable.ic_settings_development_prize_v7)); // R.drawable.ic_settings_development modify by lijimeng 20161220

        sResMap.put(DeviceInfoSettings.class.getName(),
                new SearchIndexableResource(
                        Ranking.getRankForClassName(DeviceInfoSettings.class.getName()),
                        NO_DATA_RES_ID,
                        DeviceInfoSettings.class.getName(),
                        R.drawable.ic_settings_about_prize_v7)); // R.drawable.ic_settings_about modify by lijimeng 20161220

        // sResMap.put(LegalSettings.class.getName(),
                // new SearchIndexableResource(
                        // Ranking.getRankForClassName(LegalSettings.class.getName()),
                        // NO_DATA_RES_ID,
                        // LegalSettings.class.getName(),
                        // R.drawable.ic_settings_about_prize_v7)); // R.drawable.ic_settings_about modify by lijimeng 20161220

        sResMap.put(ZenModeVisualInterruptionSettings.class.getName(),
                new SearchIndexableResource(
                        Ranking.getRankForClassName(
                                ZenModeVisualInterruptionSettings.class.getName()),
                        R.xml.zen_mode_visual_interruptions_settings,
                        ZenModeVisualInterruptionSettings.class.getName(),
                        R.drawable.ic_settings_zen_prize)); //  R.drawable.ic_settings_notifications modify by lijimeng 20161220

        /// M: add for mtk feature(Settings is an entrance , has its separate apk,
        /// such as schedule power on/off) search function {@
        sResMap.put(SearchExt.class.getName(),
                new SearchIndexableResource(
                        Ranking.getRankForClassName(SearchExt.class.getName()),
                        NO_DATA_RES_ID,
                        SearchExt.class.getName(),
                        R.drawable.ic_settings_smart_accessibility_prize_v7)); //  R.mipmap.ic_launcher_settings modify by lijimeng 20161220
        /// @}
        /// M: add for HotKnot @{
        sResMap.put(HotKnotSettings.class.getName(),
                new SearchIndexableResource(
                        Ranking.getRankForClassName(HotKnotSettings.class.getName()),
                        NO_DATA_RES_ID,
                        HotKnotSettings.class.getName(),
                        R.drawable.ic_settings_hotknot));

        //// @}
        /// M: Add NFC setting when NFC addon support @{
        sResMap.put(NfcSettings.class.getName(),
                new SearchIndexableResource(
                        Ranking.getRankForClassName(NfcSettings.class.getName()),
                        NO_DATA_RES_ID,
                        NfcSettings.class.getName(),
                        R.drawable.ic_settings_wireless));
        /// @}

		/// add new menu to search db lijimeng 20161220 start
		sResMap.put(ZenModeAutomationSettings.class.getName(),
                new SearchIndexableResource(
                        Ranking.getRankForClassName(ZenModeAutomationSettings.class.getName()),
                        NO_DATA_RES_ID,
                        ZenModeAutomationSettings.class.getName(),
                        R.drawable.ic_settings_zen_prize));

		sResMap.put(PrizeNotificationCentreActivity.class.getName(),
                new SearchIndexableResource(
                        Ranking.getRankForClassName(PrizeNotificationCentreActivity.class.getName()),
                        NO_DATA_RES_ID,
                        PrizeNotificationCentreActivity.class.getName(),
                        R.drawable.ic_settings_notification_centre_settings_prize));

		sResMap.put(ProcessStatsSummary.class.getName(),
                new SearchIndexableResource(
                        Ranking.getRankForClassName(ProcessStatsSummary.class.getName()),
                        NO_DATA_RES_ID,
                        ProcessStatsSummary.class.getName(),
                        R.drawable.ic_settings_memory_prize));

		sResMap.put(PrizeManageAppInstances.class.getName(),
                new SearchIndexableResource(
                        Ranking.getRankForClassName(PrizeManageAppInstances.class.getName()),
                        NO_DATA_RES_ID,
                        PrizeManageAppInstances.class.getName(),
                        R.drawable.ic_settings_smart_accessibility_prize_v7));

		sResMap.put(AccountSettings.class.getName(),
                new SearchIndexableResource(
                        Ranking.getRankForClassName(AccountSettings.class.getName()),
                        NO_DATA_RES_ID,
                        AccountSettings.class.getName(),
                        R.drawable.ic_settings_accounts));

		sResMap.put(PrizeIntelligentSettings.class.getName(),
                new SearchIndexableResource(
                        Ranking.getRankForClassName(PrizeIntelligentSettings.class.getName()),
                        NO_DATA_RES_ID,
                        PrizeIntelligentSettings.class.getName(),
                        R.drawable.ic_settings_smart_accessibility_prize_v7));

		sResMap.put(PrizeNoticeStatusBarSettings.class.getName(),
                new SearchIndexableResource(
                        Ranking.getRankForClassName(PrizeNoticeStatusBarSettings.class.getName()),
                        NO_DATA_RES_ID,
                        PrizeNoticeStatusBarSettings.class.getName(),
                        R.drawable.ic_settings_notice_prize));

		sResMap.put(PrizeWallpaperLockscreenSettings.class.getName(),
                new SearchIndexableResource(
                        Ranking.getRankForClassName(PrizeWallpaperLockscreenSettings.class.getName()),
                        NO_DATA_RES_ID,
                        PrizeWallpaperLockscreenSettings.class.getName(),
                        R.drawable.ic_settings_wallpaper_prize));

		sResMap.put(PrizeApplicationManagementSettings.class.getName(),
                new SearchIndexableResource(
                        Ranking.getRankForClassName(PrizeApplicationManagementSettings.class.getName()),
                        R.xml.prize_application_management,
                        PrizeApplicationManagementSettings.class.getName(),
                        R.drawable.ic_settings_others));

		sResMap.put(PrizeOtherSettings.class.getName(),
                new SearchIndexableResource(
                        Ranking.getRankForClassName(PrizeOtherSettings.class.getName()),
                        NO_DATA_RES_ID,
                        PrizeOtherSettings.class.getName(),
                        R.drawable.ic_settings_others));

		sResMap.put(TetherSettings.class.getName(),
                new SearchIndexableResource(
                        Ranking.getRankForClassName(TetherSettings.class.getName()),
                        R.xml.tether_prefs,
                        TetherSettings.class.getName(),
                        R.drawable.ic_settings_otherwireless_prize));
        sResMap.put(PrizeFingerprintOperationSettings.class.getName(),
                new SearchIndexableResource(
                        Ranking.getRankForClassName(PrizeFingerprintOperationSettings.class.getName()),
                        NO_DATA_RES_ID,
                        PrizeFingerprintOperationSettings.class.getName(),
                        R.drawable.ic_settings_finger_prize_v8));
        sResMap.put(PrizeOldLauncher.class.getName(),
                new SearchIndexableResource(
                        Ranking.getRankForClassName(PrizeOldLauncher.class.getName()),
                        NO_DATA_RES_ID,
                        PrizeOldLauncher.class.getName(),
                        R.drawable.ic_settings_old_launcher_prize_v8));
        sResMap.put(PrizeHBHelper.class.getName(),
                new SearchIndexableResource(
                        Ranking.getRankForClassName(PrizeHBHelper.class.getName()),
                        NO_DATA_RES_ID,
                        PrizeHBHelper.class.getName(),
                        R.drawable.ic_settings_red_packet_helper_prize_v8));
        sResMap.put(PrizeSystemUpdate.class.getName(),
                new SearchIndexableResource(
                        Ranking.getRankForClassName(PrizeSystemUpdate.class.getName()),
                        NO_DATA_RES_ID,
                        PrizeSystemUpdate.class.getName(),
                        R.drawable.ic_settings_ota_prize_v8));
        sResMap.put(PrizeFaceOperationSettings.class.getName(),
                    new SearchIndexableResource(
                            Ranking.getRankForClassName(PrizeFaceOperationSettings.class.getName()),
                            R.xml.prize_face_operation_settings,
                            PrizeFaceOperationSettings.class.getName(),
                            R.drawable.ic_settings_face_prize_v7));
        sResMap.put(PrizeFeedBack.class.getName(),
                    new SearchIndexableResource(
                            Ranking.getRankForClassName(PrizeFeedBack.class.getName()),
                            NO_DATA_RES_ID,
                            PrizeFeedBack.class.getName(),
                            R.drawable.prize_feedback_v8));
		/// add new menu to search db lijimeng 20161220 end
    }

    private SearchIndexableResources() {
    }

    public static int size() {
        return sResMap.size();
    }

    public static SearchIndexableResource getResourceByName(String className) {
        return sResMap.get(className);
    }

    public static Collection<SearchIndexableResource> values() {
        return sResMap.values();
    }
}
