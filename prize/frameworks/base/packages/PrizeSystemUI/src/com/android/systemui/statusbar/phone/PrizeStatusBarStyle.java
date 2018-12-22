/*
* created for status bar style. prize-linkh-20150901
*/
package com.android.systemui.statusbar.phone;

import android.util.IntArray;
import android.app.StatusBarManager;
import com.android.systemui.R;
import android.content.Context;
import android.util.Log;
import java.util.HashMap;
import android.content.res.Resources;
import com.android.systemui.statusbar.policy.LocationControllerImpl;
import com.mediatek.common.prizeoption.PrizeOption; //prize add by xiarui for vibrate control 2018-07-02

public final class PrizeStatusBarStyle {
    private static final String TAG = "StatusBarStyle";
    private Context mContext;
    private static PrizeStatusBarStyle mInstance;

    // 'More' icons in Notification icon Area.
    private static final int[] MORE_ICONS = new int[StatusBarManager.STATUS_BAR_INVERSE_TOTAL];
    private static final HashMap<String, PrizeStatusIconCategory> STATUS_ICONS_MAP = new HashMap<String, PrizeStatusIconCategory>();

    private PrizeStatusBarStyle() {}
    
    private void init(Context context) {
        mContext = context;

        // 'More' icons.
        MORE_ICONS[StatusBarManager.STATUS_BAR_INVERSE_WHITE] =  R.drawable.stat_notify_more;        
        MORE_ICONS[StatusBarManager.STATUS_BAR_INVERSE_GRAY] =  R.drawable.stat_notify_more_gray_prize;
        
        // 'Status' icons
        //context.getResources().getStringArray(com.android.internal.R.array.config_statusBarIcons);
        initStatusIcons();

        // 'Signal Cluster' icons


        // Other icons
        
    }

    //Note: some other status icons 
    private void initStatusIcons() {
        printLog("initStatusIcons().....");
        PrizeStatusIconCategory iconCategory = new PrizeStatusIconCategory(mContext);
        String slot = "tty";
        iconCategory.addIcons(R.drawable.stat_sys_tty_mode, StatusBarManager.STATUS_BAR_INVERSE_WHITE, R.drawable.stat_sys_tty_mode);        
        iconCategory.addIcons(R.drawable.stat_sys_tty_mode, StatusBarManager.STATUS_BAR_INVERSE_GRAY, R.drawable.stat_sys_tty_mode_gray_prize);
        /*PRIZE-add for liuhai screen-liufan-2018-06-25-start*/
        if(PhoneStatusBar.OPEN_LIUHAI_SCREEN2){
            iconCategory.addIcons(R.drawable.liuhai2_tty, StatusBarManager.STATUS_BAR_INVERSE_WHITE, R.drawable.liuhai2_tty);
            iconCategory.addIcons(R.drawable.liuhai2_tty, StatusBarManager.STATUS_BAR_INVERSE_GRAY, R.drawable.liuhai2_tty_gray_prize);
        }
        /*PRIZE-add for liuhai screen-liufan-2018-06-25-end*/
        STATUS_ICONS_MAP.put(slot, iconCategory);
        
        /*iconCategory = new PrizeStatusIconCategory(mContext);
        slot = "cdma_eri";
        iconCategory.addIcons(R.drawable.stat_sys_roaming_cdma_0, StatusBarManager.STATUS_BAR_INVERSE_WHITE, R.drawable.stat_sys_roaming_cdma_0);        
        iconCategory.addIcons(R.drawable.stat_sys_roaming_cdma_0, StatusBarManager.STATUS_BAR_INVERSE_GRAY, R.drawable.stat_sys_roaming_cdma_0_gray_prize);
        STATUS_ICONS_MAP.put(slot, iconCategory);*/

        iconCategory = new PrizeStatusIconCategory(mContext);
        slot = "bluetooth";
        iconCategory.addIcons(R.drawable.stat_sys_data_bluetooth, StatusBarManager.STATUS_BAR_INVERSE_WHITE, R.drawable.stat_sys_data_bluetooth);        
        iconCategory.addIcons(R.drawable.stat_sys_data_bluetooth, StatusBarManager.STATUS_BAR_INVERSE_GRAY, R.drawable.stat_sys_data_bluetooth_gray_prize);
        iconCategory.addIcons(R.drawable.stat_sys_data_bluetooth_connected, StatusBarManager.STATUS_BAR_INVERSE_WHITE, R.drawable.stat_sys_data_bluetooth_connected);        
        iconCategory.addIcons(R.drawable.stat_sys_data_bluetooth_connected, StatusBarManager.STATUS_BAR_INVERSE_GRAY, R.drawable.stat_sys_data_bluetooth_connected_gray_prize);
        /*PRIZE-add for liuhai screen-liufan-2018-04-09-start*/
        if(PhoneStatusBar.OPEN_LIUHAI_SCREEN && !PhoneStatusBar.OPEN_LIUHAI_SCREEN2){
            iconCategory.addIcons(R.drawable.liuhai_bluetooth, StatusBarManager.STATUS_BAR_INVERSE_WHITE, R.drawable.liuhai_bluetooth);
            iconCategory.addIcons(R.drawable.liuhai_bluetooth, StatusBarManager.STATUS_BAR_INVERSE_GRAY, R.drawable.liuhai_bluetooth_gray_prize);
            iconCategory.addIcons(R.drawable.liuhai_bluetooth_connected, StatusBarManager.STATUS_BAR_INVERSE_WHITE, R.drawable.liuhai_bluetooth_connected);
            iconCategory.addIcons(R.drawable.liuhai_bluetooth_connected, StatusBarManager.STATUS_BAR_INVERSE_GRAY, R.drawable.liuhai_bluetooth_connected_gray_prize);
        }
        /*PRIZE-add for liuhai screen-liufan-2018-04-09-end*/
		/*PRIZE-add for liuhai screen-liufan-2018-06-25-start*/
        if(PhoneStatusBar.OPEN_LIUHAI_SCREEN2){
            iconCategory.addIcons(R.drawable.liuhai2_bluetooth, StatusBarManager.STATUS_BAR_INVERSE_WHITE, R.drawable.liuhai2_bluetooth);
            iconCategory.addIcons(R.drawable.liuhai2_bluetooth, StatusBarManager.STATUS_BAR_INVERSE_GRAY, R.drawable.liuhai2_bluetooth_gray_prize);
            iconCategory.addIcons(R.drawable.liuhai2_bluetooth_connected, StatusBarManager.STATUS_BAR_INVERSE_WHITE, R.drawable.liuhai2_bluetooth_connected);
            iconCategory.addIcons(R.drawable.liuhai2_bluetooth_connected, StatusBarManager.STATUS_BAR_INVERSE_GRAY, R.drawable.liuhai2_bluetooth_connected_gray_prize);
        }
        /*PRIZE-add for liuhai screen-liufan-2018-06-25-end*/
        STATUS_ICONS_MAP.put(slot, iconCategory);

        iconCategory = new PrizeStatusIconCategory(mContext);
        slot = "alarm_clock";
        iconCategory.addIcons(R.drawable.stat_sys_alarm, StatusBarManager.STATUS_BAR_INVERSE_WHITE, R.drawable.stat_sys_alarm);        
        iconCategory.addIcons(R.drawable.stat_sys_alarm, StatusBarManager.STATUS_BAR_INVERSE_GRAY, R.drawable.stat_sys_alarm_gray_prize);
        /*PRIZE-add for liuhai screen-liufan-2018-04-09-start*/
        if(PhoneStatusBar.OPEN_LIUHAI_SCREEN && !PhoneStatusBar.OPEN_LIUHAI_SCREEN2){
            iconCategory.addIcons(R.drawable.liuhai_alarm_clock, StatusBarManager.STATUS_BAR_INVERSE_WHITE, R.drawable.liuhai_alarm_clock);
            iconCategory.addIcons(R.drawable.liuhai_alarm_clock, StatusBarManager.STATUS_BAR_INVERSE_GRAY, R.drawable.liuhai_alarm_clock_gray_prize);
        }
        /*PRIZE-add for liuhai screen-liufan-2018-04-09-end*/
		/*PRIZE-add for liuhai screen-liufan-2018-06-25-start*/
        if(PhoneStatusBar.OPEN_LIUHAI_SCREEN2){
            iconCategory.addIcons(R.drawable.liuhai2_alarm_clock, StatusBarManager.STATUS_BAR_INVERSE_WHITE, R.drawable.liuhai2_alarm_clock);
            iconCategory.addIcons(R.drawable.liuhai2_alarm_clock, StatusBarManager.STATUS_BAR_INVERSE_GRAY, R.drawable.liuhai2_alarm_clock_gray_prize);
        }
        /*PRIZE-add for liuhai screen-liufan-2018-06-25-end*/
        STATUS_ICONS_MAP.put(slot, iconCategory);

        iconCategory = new PrizeStatusIconCategory(mContext);
        slot = "data_saver";
        iconCategory.addIcons(R.drawable.stat_sys_data_saver, StatusBarManager.STATUS_BAR_INVERSE_WHITE, R.drawable.stat_sys_data_saver);        
        iconCategory.addIcons(R.drawable.stat_sys_data_saver, StatusBarManager.STATUS_BAR_INVERSE_GRAY, R.drawable.stat_sys_data_saver_gray_prize);
        /*PRIZE-add for liuhai screen-liufan-2018-06-25-start*/
        if(PhoneStatusBar.OPEN_LIUHAI_SCREEN2){
            iconCategory.addIcons(R.drawable.liuhai2_data_saver, StatusBarManager.STATUS_BAR_INVERSE_WHITE, R.drawable.liuhai2_data_saver);
            iconCategory.addIcons(R.drawable.liuhai2_data_saver, StatusBarManager.STATUS_BAR_INVERSE_GRAY, R.drawable.liuhai2_data_saver_gray_prize);
        }
        /*PRIZE-add for liuhai screen-liufan-2018-06-25-end*/
        STATUS_ICONS_MAP.put(slot, iconCategory);

        /*iconCategory = new PrizeStatusIconCategory(mContext);
        slot = "sync_active";
        iconCategory.addIcons(R.drawable.stat_sys_sync, StatusBarManager.STATUS_BAR_INVERSE_WHITE, R.drawable.stat_sys_sync);        
        iconCategory.addIcons(R.drawable.stat_sys_sync, StatusBarManager.STATUS_BAR_INVERSE_GRAY, R.drawable.stat_sys_sync_gray_prize);
        STATUS_ICONS_MAP.put(slot, iconCategory);*/

        iconCategory = new PrizeStatusIconCategory(mContext);
        slot = "zen";
        iconCategory.addIcons(R.drawable.stat_sys_zen_none, StatusBarManager.STATUS_BAR_INVERSE_WHITE, R.drawable.stat_sys_zen_none);        
        iconCategory.addIcons(R.drawable.stat_sys_zen_none, StatusBarManager.STATUS_BAR_INVERSE_GRAY, R.drawable.stat_sys_zen_none_gray_prize);
        iconCategory.addIcons(R.drawable.stat_sys_zen_important, StatusBarManager.STATUS_BAR_INVERSE_WHITE, R.drawable.stat_sys_zen_important);
        iconCategory.addIcons(R.drawable.stat_sys_zen_important, StatusBarManager.STATUS_BAR_INVERSE_GRAY, R.drawable.stat_sys_zen_important_gray_prize);
        iconCategory.addIcons(R.drawable.stat_sys_dnd_total_silence, StatusBarManager.STATUS_BAR_INVERSE_WHITE, R.drawable.stat_sys_dnd_total_silence);
        iconCategory.addIcons(R.drawable.stat_sys_dnd_total_silence, StatusBarManager.STATUS_BAR_INVERSE_GRAY, R.drawable.stat_sys_dnd_total_silence_gray_prize);
        /*PRIZE-add for liuhai screen-liufan-2018-04-09-start*/
        if(PhoneStatusBar.OPEN_LIUHAI_SCREEN && !PhoneStatusBar.OPEN_LIUHAI_SCREEN2){
            iconCategory.addIcons(R.drawable.liuhai_zen, StatusBarManager.STATUS_BAR_INVERSE_WHITE, R.drawable.liuhai_zen);
            iconCategory.addIcons(R.drawable.liuhai_zen, StatusBarManager.STATUS_BAR_INVERSE_GRAY, R.drawable.liuhai_zen_gray_prize);
        }
        /*PRIZE-add for liuhai screen-liufan-2018-04-09-end*/
        /*PRIZE-add for liuhai screen-liufan-2018-06-25-start*/
        if(PhoneStatusBar.OPEN_LIUHAI_SCREEN2){
            iconCategory.addIcons(R.drawable.liuhai2_zen, StatusBarManager.STATUS_BAR_INVERSE_WHITE, R.drawable.liuhai2_zen);
            iconCategory.addIcons(R.drawable.liuhai2_zen, StatusBarManager.STATUS_BAR_INVERSE_GRAY, R.drawable.liuhai2_zen_gray_prize);
            iconCategory.addIcons(R.drawable.liuhai2_zen_important, StatusBarManager.STATUS_BAR_INVERSE_WHITE, R.drawable.liuhai2_zen_important);
            iconCategory.addIcons(R.drawable.liuhai2_zen_important, StatusBarManager.STATUS_BAR_INVERSE_GRAY, R.drawable.liuhai2_zen_important_gray_prize);
        }
        /*PRIZE-add for liuhai screen-liufan-2018-06-25-end*/
        STATUS_ICONS_MAP.put(slot, iconCategory);

        iconCategory = new PrizeStatusIconCategory(mContext);
        slot = "volume";
        //prize modify by xiarui 2018-06-28 start @{
        if (PrizeOption.PRIZE_VIBRATE_CONTROL) {
            iconCategory.addIcons(R.drawable.stat_sys_ringer_silent, StatusBarManager.STATUS_BAR_INVERSE_WHITE, R.drawable.stat_sys_ringer_silent_prize2);
            iconCategory.addIcons(R.drawable.stat_sys_ringer_silent, StatusBarManager.STATUS_BAR_INVERSE_GRAY, R.drawable.stat_sys_ringer_silent_gray2);
            iconCategory.addIcons(R.drawable.stat_sys_ringer_vibrate, StatusBarManager.STATUS_BAR_INVERSE_WHITE, R.drawable.stat_sys_ringer_vibrate_prize2);
            iconCategory.addIcons(R.drawable.stat_sys_ringer_vibrate, StatusBarManager.STATUS_BAR_INVERSE_GRAY, R.drawable.stat_sys_ringer_vibrate_gray2);
        } else {
            iconCategory.addIcons(R.drawable.stat_sys_ringer_vibrate, StatusBarManager.STATUS_BAR_INVERSE_WHITE, R.drawable.stat_sys_ringer_vibrate);
            iconCategory.addIcons(R.drawable.stat_sys_ringer_vibrate, StatusBarManager.STATUS_BAR_INVERSE_GRAY, R.drawable.stat_sys_ringer_vibrate_gray_prize);
        }
        //@}
        /*PRIZE-add for liuhai screen-liufan-2018-04-09-start*/
        if(PhoneStatusBar.OPEN_LIUHAI_SCREEN && !PhoneStatusBar.OPEN_LIUHAI_SCREEN2){
            //prize add by xiarui 2018-06-28 start @{
            if (PrizeOption.PRIZE_VIBRATE_CONTROL) {
                iconCategory.addIcons(R.drawable.liuhai_volume, StatusBarManager.STATUS_BAR_INVERSE_WHITE, R.drawable.liuhai_volume2);
                iconCategory.addIcons(R.drawable.liuhai_volume, StatusBarManager.STATUS_BAR_INVERSE_GRAY, R.drawable.liuhai_volume_gray_prize2);
                iconCategory.addIcons(R.drawable.liuhai_volume_vibrator2, StatusBarManager.STATUS_BAR_INVERSE_WHITE, R.drawable.liuhai_volume_vibrator2);
                iconCategory.addIcons(R.drawable.liuhai_volume_vibrator2, StatusBarManager.STATUS_BAR_INVERSE_GRAY, R.drawable.liuhai_volume_vibrator_gray_prize2);
            } else {
                iconCategory.addIcons(R.drawable.liuhai_volume, StatusBarManager.STATUS_BAR_INVERSE_WHITE, R.drawable.liuhai_volume);
                iconCategory.addIcons(R.drawable.liuhai_volume, StatusBarManager.STATUS_BAR_INVERSE_GRAY, R.drawable.liuhai_volume_gray_prize);
            }
            //@}
        }
        /*PRIZE-add for liuhai screen-liufan-2018-04-09-end*/
		/*PRIZE-add for liuhai screen-liufan-2018-06-25-start*/
        if(PhoneStatusBar.OPEN_LIUHAI_SCREEN2){
            iconCategory.addIcons(R.drawable.liuhai2_volume, StatusBarManager.STATUS_BAR_INVERSE_WHITE, R.drawable.liuhai2_volume);
            iconCategory.addIcons(R.drawable.liuhai2_volume, StatusBarManager.STATUS_BAR_INVERSE_GRAY, R.drawable.liuhai2_volume_gray_prize);
        }
        /*PRIZE-add for liuhai screen-liufan-2018-06-25-end*/
        STATUS_ICONS_MAP.put(slot, iconCategory);

        iconCategory = new PrizeStatusIconCategory(mContext);
        slot = "cast";
        iconCategory.addIcons(R.drawable.stat_sys_cast, StatusBarManager.STATUS_BAR_INVERSE_WHITE, R.drawable.stat_sys_cast);        
        iconCategory.addIcons(R.drawable.stat_sys_cast, StatusBarManager.STATUS_BAR_INVERSE_GRAY, R.drawable.stat_sys_cast_gray_prize);
        /*PRIZE-add for liuhai screen-liufan-2018-06-25-start*/
        if(PhoneStatusBar.OPEN_LIUHAI_SCREEN2){
            iconCategory.addIcons(R.drawable.liuhai2_cast, StatusBarManager.STATUS_BAR_INVERSE_WHITE, R.drawable.liuhai2_cast);
            iconCategory.addIcons(R.drawable.liuhai2_cast, StatusBarManager.STATUS_BAR_INVERSE_GRAY, R.drawable.liuhai2_cast_gray_prize);
        }
        /*PRIZE-add for liuhai screen-liufan-2018-06-25-end*/
        STATUS_ICONS_MAP.put(slot, iconCategory);

        iconCategory = new PrizeStatusIconCategory(mContext);
        slot = "hotspot";
        iconCategory.addIcons(R.drawable.stat_sys_hotspot, StatusBarManager.STATUS_BAR_INVERSE_WHITE, R.drawable.stat_sys_hotspot);        
        iconCategory.addIcons(R.drawable.stat_sys_hotspot, StatusBarManager.STATUS_BAR_INVERSE_GRAY, R.drawable.stat_sys_hotspot_gray_prize);
        /*PRIZE-add for liuhai screen-liufan-2018-04-09-start*/
        if(PhoneStatusBar.OPEN_LIUHAI_SCREEN && !PhoneStatusBar.OPEN_LIUHAI_SCREEN2){
            iconCategory.addIcons(R.drawable.liuhai_hotspot, StatusBarManager.STATUS_BAR_INVERSE_WHITE, R.drawable.liuhai_hotspot);
            iconCategory.addIcons(R.drawable.liuhai_hotspot, StatusBarManager.STATUS_BAR_INVERSE_GRAY, R.drawable.liuhai_hotspot_gray_prize);
        }
        /*PRIZE-add for liuhai screen-liufan-2018-04-09-end*/
		/*PRIZE-add for liuhai screen-liufan-2018-06-25-start*/
        if(PhoneStatusBar.OPEN_LIUHAI_SCREEN2){
            iconCategory.addIcons(R.drawable.liuhai2_hotspot, StatusBarManager.STATUS_BAR_INVERSE_WHITE, R.drawable.liuhai2_hotspot);
            iconCategory.addIcons(R.drawable.liuhai2_hotspot, StatusBarManager.STATUS_BAR_INVERSE_GRAY, R.drawable.liuhai2_hotspot_gray_prize);
        }
        /*PRIZE-add for liuhai screen-liufan-2018-06-25-end*/
        STATUS_ICONS_MAP.put(slot, iconCategory);
        
        iconCategory = new PrizeStatusIconCategory(mContext);
        slot = "headset";
        iconCategory.addIcons(R.drawable.ic_headset_mic, StatusBarManager.STATUS_BAR_INVERSE_WHITE, R.drawable.ic_headset_mic);        
        iconCategory.addIcons(R.drawable.ic_headset_mic, StatusBarManager.STATUS_BAR_INVERSE_GRAY, R.drawable.ic_headset_mic_gray_prize);
        iconCategory.addIcons(R.drawable.ic_headset, StatusBarManager.STATUS_BAR_INVERSE_WHITE, R.drawable.ic_headset);        
        iconCategory.addIcons(R.drawable.ic_headset, StatusBarManager.STATUS_BAR_INVERSE_GRAY, R.drawable.ic_headset_gray_prize);
        /*PRIZE-add for liuhai screen-liufan-2018-06-25-start*/
        if(PhoneStatusBar.OPEN_LIUHAI_SCREEN2){
            iconCategory.addIcons(R.drawable.liuhai2_headset_with_mic, StatusBarManager.STATUS_BAR_INVERSE_WHITE, R.drawable.liuhai2_headset_with_mic);
            iconCategory.addIcons(R.drawable.liuhai2_headset_with_mic, StatusBarManager.STATUS_BAR_INVERSE_GRAY, R.drawable.liuhai2_headset_with_mic_gray_prize);
            iconCategory.addIcons(R.drawable.liuhai2_headset_without_mic, StatusBarManager.STATUS_BAR_INVERSE_WHITE, R.drawable.liuhai2_headset_without_mic);
            iconCategory.addIcons(R.drawable.liuhai2_headset_without_mic, StatusBarManager.STATUS_BAR_INVERSE_GRAY, R.drawable.liuhai2_headset_without_mic_gray_prize);
        }
        /*PRIZE-add for liuhai screen-liufan-2018-06-25-end*/
        STATUS_ICONS_MAP.put(slot, iconCategory);

        iconCategory = new PrizeStatusIconCategory(mContext);
        slot = "location";
        iconCategory.addIcons(LocationControllerImpl.LOCATION_STATUS_ICON_ID, StatusBarManager.STATUS_BAR_INVERSE_WHITE, LocationControllerImpl.LOCATION_STATUS_ICON_ID);        
        iconCategory.addIcons(LocationControllerImpl.LOCATION_STATUS_ICON_ID, StatusBarManager.STATUS_BAR_INVERSE_GRAY, R.drawable.stat_sys_location_gray_prize);
        /*PRIZE-add for liuhai screen-liufan-2018-04-09-start*/
        if(PhoneStatusBar.OPEN_LIUHAI_SCREEN && !PhoneStatusBar.OPEN_LIUHAI_SCREEN2){
            iconCategory.addIcons(R.drawable.liuhai_location, StatusBarManager.STATUS_BAR_INVERSE_WHITE, R.drawable.liuhai_location);
            iconCategory.addIcons(R.drawable.liuhai_location, StatusBarManager.STATUS_BAR_INVERSE_GRAY, R.drawable.liuhai_location_gray_prize);
        }
        /*PRIZE-add for liuhai screen-liufan-2018-04-09-end*/
		/*PRIZE-add for liuhai screen-liufan-2018-06-25-start*/
        if(PhoneStatusBar.OPEN_LIUHAI_SCREEN2){
            iconCategory.addIcons(R.drawable.liuhai2_location, StatusBarManager.STATUS_BAR_INVERSE_WHITE, R.drawable.liuhai2_location);
            iconCategory.addIcons(R.drawable.liuhai2_location, StatusBarManager.STATUS_BAR_INVERSE_GRAY, R.drawable.liuhai2_location_gray_prize);
        }
        /*PRIZE-add for liuhai screen-liufan-2018-06-25-end*/
        STATUS_ICONS_MAP.put(slot, iconCategory);
        iconCategory = new PrizeStatusIconCategory(mContext);

        slot = "speakerphone";
        iconCategory.addIcons(com.android.internal.R.drawable.stat_sys_speakerphone, StatusBarManager.STATUS_BAR_INVERSE_WHITE, R.drawable.stat_sys_speakerphone_prize);        
        iconCategory.addIcons(com.android.internal.R.drawable.stat_sys_speakerphone, StatusBarManager.STATUS_BAR_INVERSE_GRAY, R.drawable.stat_sys_speakerphone_gray_prize);
        /*PRIZE-add for liuhai screen-liufan-2018-06-25-start*/
        if(PhoneStatusBar.OPEN_LIUHAI_SCREEN2){
            iconCategory.addIcons(R.drawable.liuhai2_speakerphone, StatusBarManager.STATUS_BAR_INVERSE_WHITE, R.drawable.liuhai2_speakerphone);
            iconCategory.addIcons(R.drawable.liuhai2_speakerphone, StatusBarManager.STATUS_BAR_INVERSE_GRAY, R.drawable.liuhai2_speakerphone_gray_prize);
        }
        /*PRIZE-add for liuhai screen-liufan-2018-06-25-end*/
        STATUS_ICONS_MAP.put(slot, iconCategory);

        iconCategory = new PrizeStatusIconCategory(mContext);
        slot = "mute";
        iconCategory.addIcons(com.android.internal.R.drawable.stat_notify_call_mute, StatusBarManager.STATUS_BAR_INVERSE_WHITE, R.drawable.stat_notify_call_mute_prize);        
        iconCategory.addIcons(com.android.internal.R.drawable.stat_notify_call_mute, StatusBarManager.STATUS_BAR_INVERSE_GRAY, R.drawable.stat_notify_call_mute_gray_prize);
        /*PRIZE-add for liuhai screen-liufan-2018-06-25-start*/
        if(PhoneStatusBar.OPEN_LIUHAI_SCREEN2){
            iconCategory.addIcons(R.drawable.liuhai2_mute, StatusBarManager.STATUS_BAR_INVERSE_WHITE, R.drawable.liuhai2_mute);
            iconCategory.addIcons(R.drawable.liuhai2_mute, StatusBarManager.STATUS_BAR_INVERSE_GRAY, R.drawable.liuhai2_mute_gray_prize);
        }
        /*PRIZE-add for liuhai screen-liufan-2018-06-25-end*/
        STATUS_ICONS_MAP.put(slot, iconCategory);

        printLog("STATUS_ICONS_MAP.count = " + STATUS_ICONS_MAP.size());
    }

    private void printLog(String msg) {
        Log.d(TAG, msg);
    }
    public boolean isValidStatusBarStyle(int style) {
        boolean valid = true;
        if(style < 0 || style >= StatusBarManager.STATUS_BAR_INVERSE_TOTAL) {
            valid = false;
        }
        
        printLog("isValidStatusBarStyle(). style=" + style + ", valid=" + valid);
        return valid;
    }

    public static PrizeStatusBarStyle getInstance(Context context) {
        if(mInstance == null) {
            mInstance = new PrizeStatusBarStyle();
            mInstance.init(context);
        }

        return mInstance;
    }

    public int getMoreIcon(int style) {
        int icon = R.drawable.stat_notify_more;

        if(isValidStatusBarStyle(style)) {
            icon = MORE_ICONS[style];
        }

        return icon;

    }

    
    public int getColor(int style) {
        int color = StatusBarManager.STATUS_BAR_COLOR_DEFAULT;
        
        if(style == StatusBarManager.STATUS_BAR_INVERSE_WHITE) {
            color = StatusBarManager.STATUS_BAR_COLOR_WHITE;
        } else if(style == StatusBarManager.STATUS_BAR_INVERSE_GRAY) {
            color = StatusBarManager.STATUS_BAR_COLOR_GRAY;
        }

        return color;
    }

    public int getStatusIcon(int style, String slot) {
        return getStatusIcon(style, slot, 0);
    }
    
    public int getStatusIcon(int style, String slot, int defaultIconId) {
        printLog("getStatusIcon(). style=" + style + ", slot=" + slot + ", defalutIconId=" + defaultIconId);
        int icon = 0;

        if(isValidStatusBarStyle(style) && defaultIconId > 0) {
            PrizeStatusIconCategory category = STATUS_ICONS_MAP.get(slot);
            printLog("category=" + category);
            
            if(category != null) {
                icon = category.getIcon(defaultIconId, style);
            }
        }

        printLog("getStatusIcon(). icon=" + icon);
        return icon;
    }

    
}

