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

package com.android.systemui.statusbar.policy;

import android.content.Context;
import android.content.Intent;
import android.telephony.SubscriptionInfo;
import com.android.settingslib.net.DataUsageController;
import com.android.settingslib.wifi.AccessPoint;

import java.util.List;
/*PRIZE-add for liuhai screen-liufan-2018-04-09-start*/
import com.android.systemui.statusbar.phone.PhoneStatusBar;
import com.android.systemui.R;
/*PRIZE-add for liuhai screen-liufan-2018-04-09-end*/

public interface NetworkController {

    boolean hasMobileDataFeature();
    void addSignalCallback(SignalCallback cb);
    void removeSignalCallback(SignalCallback cb);
    void setWifiEnabled(boolean enabled);
    void onUserSwitched(int newUserId);
    AccessPointController getAccessPointController();
    DataUsageController getMobileDataController();
    DataSaverController getDataSaverController();

    boolean hasVoiceCallingFeature();

    void addEmergencyListener(EmergencyListener listener);
    void removeEmergencyListener(EmergencyListener listener);

    public interface SignalCallback {
        default void setWifiIndicators(boolean enabled, IconState statusIcon, IconState qsIcon,
                boolean activityIn, boolean activityOut, String description) {}
        /** M: Support [Network Type on Statusbar]
          * Add one more parameter networkIcon to signal view and show the network type beside
          * the signal.
          *  @ {*/
        default void setMobileDataIndicators(IconState statusIcon, IconState qsIcon, int statusType,
                int networkIcon, int volteType, int qsType, boolean activityIn, boolean activityOut,
                String typeContentDescription, String description, boolean isWide, int subId) {}
        /** @ } */
        default void setSubs(List<SubscriptionInfo> subs) {}
        default void setNoSims(boolean show) {}

        default void setEthernetIndicators(IconState icon) {}

        default void setIsAirplaneMode(IconState icon) {}

        default void setMobileDataEnabled(boolean enabled) {}


        
		/*PRIZE-add statusIconGray-liufan-2016-04-20-start*/
        default void setWifiIndicators(boolean enabled, IconState statusIcon, IconState statusIconGray, IconState qsIcon,
                int activityIcon, boolean activityIn, boolean activityOut, String description){}
        default void setMobileActivityId(int mobileActivityId, int subId){}
        default void setMobileDataIndicators(IconState statusIcon, IconState statusIconGray, IconState qsIcon, int statusType,
                int networkIcon, int volteType, int qsType, boolean activityIn, boolean activityOut,
                String typeContentDescription, String description, boolean isWide, int subId){}
        default void setVolteStatusIcon(final int iconId, final int grayIconId){}

        default void setWifiIndicatorsForInverse(boolean enabled, IconState statusIcon, IconState statusIconGray, IconState qsIcon,
                int activityIcon, boolean activityIn, boolean activityOut, String description){}
        default void setMobileDataIndicatorsForInverse(IconState statusIcon, IconState statusIconGray, IconState qsIcon, int statusType,
                int networkIcon, int volteType, int qsType, boolean activityIn, boolean activityOut,
                String typeContentDescription, String description, boolean isWide, int subId){}
		/*PRIZE-add statusIconGray-liufan-2016-04-20-end*/
        //add for statusbar inverse. prize-linkh-20150903
        default void onStatusBarStyleChanged(int style){}
        default void setIgnoreStatusBarStyleChanged(boolean ignored){}
        default boolean shouldIgnoreStatusBarStyleChanged(){ return true; }
        default int getStatusBarStyle(){ return 1; }
		//end...
    }

    public interface EmergencyListener {
        void setEmergencyCallsOnly(boolean emergencyOnly);
    }

    public static class IconState {
        public final boolean visible;
        public final int icon;
        public final String contentDescription;

        public IconState(boolean visible, int icon, String contentDescription) {
            this.visible = visible;
            /*PRIZE-update for liuhai screen-liufan-2018-04-09-start*/
            if(PhoneStatusBar.OPEN_LIUHAI_SCREEN2){
                this.icon = replaceIconForLiuHai2(icon);
            } else
            if(PhoneStatusBar.OPEN_LIUHAI_SCREEN){
                this.icon = replaceIconForLiuHai(icon);
            }else{
                this.icon = icon;
            }
            /*PRIZE-update for liuhai screen-liufan-2018-04-09-end*/
            this.contentDescription = contentDescription;
        }

        public IconState(boolean visible, int icon, int contentDescription,
                Context context) {
            this(visible, icon, context.getString(contentDescription));
        }

        /*PRIZE-add for liuhai screen-liufan-2018-06-25-start*/
        public static int replaceIconForLiuHai2(int icon){
            int result = icon;
            switch (icon) {
            //mobile data icon
            case R.drawable.stat_sys_signal_0_fully:
                result = R.drawable.liuhai2_stat_sys_signal_0_fully;
                break;
            case R.drawable.stat_sys_signal_1_fully:
                result = R.drawable.liuhai2_stat_sys_signal_1_fully;
                break;
            case R.drawable.stat_sys_signal_2_fully:
                result = R.drawable.liuhai2_stat_sys_signal_2_fully;
                break;
            case R.drawable.stat_sys_signal_3_fully:
                result = R.drawable.liuhai2_stat_sys_signal_3_fully;
                break;
            case R.drawable.stat_sys_signal_4_fully:
                result = R.drawable.liuhai2_stat_sys_signal_4_fully;
                break;

            case R.drawable.stat_sys_signal_0_fully_gray_prize:
                result = R.drawable.liuhai2_stat_sys_signal_0_fully_gray_prize;
                break;
            case R.drawable.stat_sys_signal_1_fully_gray_prize:
                result = R.drawable.liuhai2_stat_sys_signal_1_fully_gray_prize;
                break;
            case R.drawable.stat_sys_signal_2_fully_gray_prize:
                result = R.drawable.liuhai2_stat_sys_signal_2_fully_gray_prize;
                break;
            case R.drawable.stat_sys_signal_3_fully_gray_prize:
                result = R.drawable.liuhai2_stat_sys_signal_3_fully_gray_prize;
                break;
            case R.drawable.stat_sys_signal_4_fully_gray_prize:
                result = R.drawable.liuhai2_stat_sys_signal_4_fully_gray_prize;
                break;
            }
            return result;
        }
        /*PRIZE-add for liuhai screen-liufan-2018-06-25-end*/
    }

    /*PRIZE-add for liuhai screen-liufan-2018-04-09-start*/
    public static int replaceIconForLiuHai(int icon){
        int result = icon;
        switch (icon) {
        //mobile data icon
        case R.drawable.stat_sys_signal_0_fully:
            result = R.drawable.stat_sys_signal_0_liuhai_fully;
            break;
        case R.drawable.stat_sys_signal_1_fully:
            result = R.drawable.stat_sys_signal_1_liuhai_fully;
            break;
        case R.drawable.stat_sys_signal_2_fully:
            result = R.drawable.stat_sys_signal_2_liuhai_fully;
            break;
        case R.drawable.stat_sys_signal_3_fully:
            result = R.drawable.stat_sys_signal_3_liuhai_fully;
            break;
        case R.drawable.stat_sys_signal_4_fully:
            result = R.drawable.stat_sys_signal_4_liuhai_fully;
            break;

        case R.drawable.stat_sys_signal_0_fully_gray_prize:
            result = R.drawable.stat_sys_signal_0_liuhai_fully_gray_prize;
            break;
        case R.drawable.stat_sys_signal_1_fully_gray_prize:
            result = R.drawable.stat_sys_signal_1_liuhai_fully_gray_prize;
            break;
        case R.drawable.stat_sys_signal_2_fully_gray_prize:
            result = R.drawable.stat_sys_signal_2_liuhai_fully_gray_prize;
            break;
        case R.drawable.stat_sys_signal_3_fully_gray_prize:
            result = R.drawable.stat_sys_signal_3_liuhai_fully_gray_prize;
            break;
        case R.drawable.stat_sys_signal_4_fully_gray_prize:
            result = R.drawable.stat_sys_signal_4_liuhai_fully_gray_prize;
            break;

        //wifi icon
        case R.drawable.stat_sys_wifi_signal_0_prize:
            result = R.drawable.stat_sys_wifi_signal_liuhai_0_prize;
            break;
        case R.drawable.stat_sys_wifi_signal_1_prize:
            result = R.drawable.stat_sys_wifi_signal_liuhai_1_prize;
            break;
        case R.drawable.stat_sys_wifi_signal_2_prize:
            result = R.drawable.stat_sys_wifi_signal_liuhai_2_prize;
            break;
        case R.drawable.stat_sys_wifi_signal_3_prize:
            result = R.drawable.stat_sys_wifi_signal_liuhai_3_prize;
            break;
        case R.drawable.stat_sys_wifi_signal_4_prize:
            result = R.drawable.stat_sys_wifi_signal_liuhai_4_prize;
            break;

        case R.drawable.stat_sys_wifi_signal_0_gray_prize:
            result = R.drawable.stat_sys_wifi_signal_liuhai_0_gray_prize;
            break;
        case R.drawable.stat_sys_wifi_signal_1_gray_prize:
            result = R.drawable.stat_sys_wifi_signal_liuhai_1_gray_prize;
            break;
        case R.drawable.stat_sys_wifi_signal_2_gray_prize:
            result = R.drawable.stat_sys_wifi_signal_liuhai_2_gray_prize;
            break;
        case R.drawable.stat_sys_wifi_signal_3_gray_prize:
            result = R.drawable.stat_sys_wifi_signal_liuhai_3_gray_prize;
            break;
        case R.drawable.stat_sys_wifi_signal_4_gray_prize:
            result = R.drawable.stat_sys_wifi_signal_liuhai_4_gray_prize;
            break;
        }
        return result;
    }
    /*PRIZE-add for liuhai screen-liufan-2018-04-09-end*/

    /**
     * Tracks changes in access points.  Allows listening for changes, scanning for new APs,
     * and connecting to new ones.
     */
    public interface AccessPointController {
        void addAccessPointCallback(AccessPointCallback callback);
        void removeAccessPointCallback(AccessPointCallback callback);
        void scanForAccessPoints();
        int getIcon(AccessPoint ap);
        boolean connect(AccessPoint ap);
        boolean canConfigWifi();

        public interface AccessPointCallback {
            void onAccessPointsChanged(List<AccessPoint> accessPoints);
            void onSettingsActivityTriggered(Intent settingsIntent);
        }
    }
}