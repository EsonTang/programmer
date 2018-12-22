/******************************************************
* All copyright©2016,shenzhen PRIZE technology co., LTD
*
* Content：system's macros defined entity class
* Version：1.0
* Author：wangxianzhen
* Date：2016-04-11
********************************************************/
package com.mediatek.common.prizeoption;

import android.os.SystemProperties;

public class PrizeOption {
	public static final String PRIZE_CUSTOMER_NAME = getString("ro.prize_customer");
    public static final boolean PRIZE_SLIDE_SCREENSHOT = true;
    public static final boolean PRIZE_OLD_LAUNCHER = getValue("ro.prize_old_launcher");
    public static final boolean PRIZE_TTS_SUPPORT = getValue("ro.prize_old_launcher");//OLD launcher & tts support
	public static final boolean PRIZE_AUTO_TEST = getValue("ro.prize_auto_test");
    public static final boolean PRIZE_CHANGED_WALLPAPER = getValue("ro.prize_changed_wallpaper");
    public static final boolean PRIZE_GAME_MODE = true;
    public static final boolean PRIZE_READING_MODE = true;
    public static final boolean PRIZE_FLOAT_WINDOW = getValue("ro.prize_float_window");
    public static final boolean PRIZE_BARRAGE_WINDOW = getValue("ro.prize_barrage_window");
    public static final boolean PRIZE_SHUTMENU = getValue("ro.prize_shut_menu");
    public static final boolean PRIZE_FLOAT_WINDOW_CONTROL = true;
    public static final boolean PRIZE_USB_SETTINGS = true;
    public static final boolean PRIZE_SALESSTATIS = getValue("sys.prize_salesstatis");
	public static final boolean PRIZE_NEW_FLOAT_WINDOW = getValue("ro.prize_new_float_window");
    public static final boolean PRIZE_SLEEP_GESTURE = false;
	public static final boolean PRIZE_FLASHLIGHT_SWITCHER = true;
	public static final boolean PRIZE_FLIP_SILENT = true;
	public static final boolean PRIZE_SMART_DIALING = false;
	public static final boolean PRIZE_SMART_ANSWER_CALL = false;
	public static final boolean PRIZE_POCKET_MODE = true;
	public static final boolean PRIZE_ANTIFAKE_TOUCH = true;
	public static final boolean PRIZE_NON_TOUCH_OPERATION = false;
		
    public static final boolean PRIZE_SYSTEMUI_BLUR_BG = true;
    public static final boolean PRIZE_SYSTEMUI_RECENTS = true;
    public static final boolean PRIZE_SYSTEMUI_HAOKAN_SCREENVIEW = getValue("ro.prize_haokan_screenview");
    public static final boolean PRIZE_SYSTEMUI_BATTERY_METER = true;
	public static final boolean PRIZE_SYSTEMUI_SCRIM_BG = true;
	public static final boolean PRIZE_FACEBEAUTY = getValue("ro.prize_facebeauty");
	public static final boolean PRIZE_CAMERA_FN_FACEBEAUTY = getValue("ro.prize_camera_fn_facebeauty");
	
	public static final boolean PRIZE_XIAOKU  = getValue("ro.prize_xiao_ku");
	public static final boolean PRIZE_HONGBAO_AUTO_HELPER  = getValue("persist.hongbao.auto.helper");
	
	public static final boolean PRIZE_APP_UPDATE = false;
	public static final boolean PRIZE_ADB_MANAGER = getValue("persist.prize.adb.manager");
	public static final boolean PRIZE_POWER_EXTEND_MODE = getValue("ro.prize_power_extend_mode");
    public static final boolean PRIZE_PURE_BACKGROUND   = getValue("ro.prize_pure_background");
	public static final boolean PRIZE_LOCK_LAUNCHER = true;
	public static final boolean PRIZE_FORBID_ERROR_DIALOG = getValue("ro.prize_forbid_error_dialog");
	public static final boolean PRIZE_INTERCEPT_INSTALL = true;//getValue("ro.prize.intercept.install");
	public static final boolean PRIZE_CHANGESIM_RESET_DIALOG = true;
       /*-prize-add by lihuangyuan,for insertsim reboot-2018-05-22-start*/
       public static final boolean PRIZE_INSERTSIM_REBOOT = getValue("ro.prize.insertsim.reboot");
       /*-prize-add by lihuangyuan,for insertsim reboot-2018-05-22-end*/
	public static final boolean PRIZE_RESET_VENDOR_APP = getValue("ro.prize.reset.vendor.app");
	public static final boolean PRIZE_CLOUD_LIST = getValue("ro.cloudlist");
	
	public static final boolean PRIZE_FINGERPRINT_MBACK = getValue("ro.prize_fingerprint_mback");
	public static final boolean PRIZE_SUPPORT_NAV_BAR_FOR_MBACK_DEVICE = isFeatureTurnedOn("ro.p_support_nav_bar_for_mback", PRIZE_FINGERPRINT_MBACK);
    public static final boolean PRIZE_FINGERPRINT_APPLOCK = getValue("ro.prize_fingerprint_applock");
    public static final boolean PRIZE_SPLIT_SCREEN = true;
	
    public static final boolean PRIZE_DYNAMICALLY_HIDE_NAVBAR = getValue("ro.support_hiding_navbar");
    public static final boolean PRIZE_REPOSITION_BACK_KEY = true;
    public static final boolean PRIZE_TREAT_RECENTS_AS_MENU = false;
    public static final boolean PRIZE_INTERCEPT_WAKEUP_ALARMS = true;
	public static final boolean PRIZE_STATUSBAR_INVERSE_COLOR = true;
    public static final boolean PRIZE_APP_MULTI_INSTANCES = isFeatureTurnedOn("ro.prize_app_multi_inst");
	public static final boolean PRIZE_REMOVE_PERSISTENT_PROP_FOR_THIRD_APP = true;
	public static final boolean PRIZE_HUYANMODE = true;
	public static final boolean PRIZE_MANAGE_PREBUILT_THIRD_APPS = true;
	public static final boolean PRIZE_LOW_MEMORY_OPTIMIZE = getValue("ro.prize_low_memory_optimize");
	public static final boolean PRIZE_SUPPORT_APP_ADJ_ADJUSTMENT = true;
	public static final boolean PRIZE_DISALLOW_INTER_LAUNCHING_APPS = true;
	public static final boolean PRIZE_CONTROL_APP_NETWORK_FOR_SLEEPING = isFeatureTurnedOn("ro.p_ctl_app_net_for_sleeping");
	public static final boolean PRIZE_NAVBAR_COLOR_CUST = isFeatureTurnedOn("ro.p_nav_bar_color_cust");
	
    public static final boolean PRIZE_SPLIT_SCREEN_ALLAPP = getIntBit("persist.prize_splite_screen", 0);
    public static final boolean PRIZE_SPLIT_SCREEN_DRAG = getIntBit("persist.prize_splite_screen", 1);
    public static final boolean PRIZE_SPLIT_SCREEN_SWAP = getIntBit("persist.prize_splite_screen", 2);
    public static final boolean PRIZE_SPLIT_SCREEN_DRAGVIEW = getIntBit("persist.prize_splite_screen", 3);
    public static final boolean PRIZE_SPLIT_SCREEN_RETURN = getIntBit("persist.prize_splite_screen", 4);
    public static final boolean PRIZE_SPLIT_SCREEN_HOME = getIntBit("persist.prize_splite_screen", 5);
    public static final boolean PRIZE_SPLIT_SCREEN_FOCUS = getIntBit("persist.prize_splite_screen", 6);
    public static final boolean PRIZE_SPLIT_SCREEN_BORDER = false;//getIntBit("persist.prize_splite_screen", 7);
    public static final boolean PRIZE_SPLIT_SCREEN_IME_NOTRESIZE = false;//getIntBit("persist.prize_splite_screen", 8);
    public static final boolean PRIZE_SPLIT_SCREEN_MENU = getIntBit("persist.prize_splite_screen", 9);
    public static final boolean PRIZE_SPLIT_SCREEN_BG_HINT_FOCUS = getIntBit("persist.prize_splite_screen", 10);
    public static final boolean PRIZE_SPLIT_SCREEN_NOT_CLR_RECENT = getIntBit("persist.prize_splite_screen", 11);
    public static final boolean PRIZE_SPLIT_SCREEN_HINT_NOT_SPLITE = getIntBit("persist.prize_splite_screen", 12);
    public static final boolean PRIZE_SPLIT_SCREEN_IME_QQMM_POP = false; //getIntBit("persist.prize_splite_screen", 13);
    public static final boolean PRIZE_SPLIT_SCREEN_LANDSCAPE_ONE_STATUBAR = getIntBit("persist.prize_splite_screen", 14);
    public static final boolean PRIZE_SPLIT_SCREEN_STATUSBAR_INVERSE_COLOR = getIntBit("persist.prize_splite_screen", 15);
    public static final boolean PRIZE_SPLIT_SCREEN_STATUBAR_USE_DOCKEDWIN = getIntBit("persist.prize_splite_screen", 16);
    public static final boolean PRIZE_SPLIT_SCREEN_NOT_HIDE_NAVBAR = getIntBit("persist.prize_splite_screen", 17);   
    public static final boolean PRIZE_SPLIT_SCREEN_MODIFY_CODE_LOGIC = getIntBit("persist.prize_splite_screen", 18);
    public static final boolean PRIZE_SPLIT_SCREEN_KEYBOARD_HOME = getIntBit("persist.prize_splite_screen", 19);
    public static final boolean PRIZE_SPLIT_SCREEN_LANDSCAPE_HOME_FAST = getIntBit("persist.prize_splite_screen", 20);
    public static final boolean PRIZE_SYSTEMUI_QQPOP_ICON = false;//getIntBit("persist.prize_splite_screen", 22); //will open
    public static final boolean PRIZE_SYSTEMUI_QQPOPICON_OPEN_SPLIT_SCREEN = false; //getIntBit("persist.prize_splite_screen", 23); //will open
    
    public static final boolean PRIZE_VIDEO_PLAY_IGNOR_QIHOO = true;
	/*prize-add-faceid-tangzeming-20171115-start*/
    public static final boolean PRIZE_FACE_ID  = getValue("ro.prize_faceid");
    public static final boolean PRIZE_FACE_ID_KOOBEE  = getValue("ro.prize_faceidkoobee");
    /*prize-add-faceid-tangzeming-20171115-end*/

    public static final boolean PRIZE_SUPPORT_DOZE_MODE_ENHANNCEMENT = true;

    public static final boolean PRIZE_SMART_CLEANER = getValue("ro.prize_smart_cleaner");
    /*prize-auto boot-add by wangxianzhen-2016-04-11-start*/
    public static final boolean PRIZE_FORBADE_THIRDAPP_AUTOBOOT = true;
    /*prize-auto boot-add by wangxianzhen-2016-04-11-end*/

    // Added for PrizeSysResMon.apk. Prize-linkh-20180308 @{    
    public static final boolean PRIZE_SUPPRORT_SYS_RES_MON = isFeatureTurnedOn("ro.prize_support_sys_res_mon");
    // @}

    // Prize Process Manager. Prize-linkh-20180316 @{    
    public static final boolean PRIZE_SUPPRORT_PROC_MGR = getIntBit("ro.prize_proc_mgr_flg", 0);
    // activity switch
    public static final boolean PRIZE_SUPPRORT_PROC_MGR_ACT = PRIZE_SUPPRORT_PROC_MGR && getIntBit("ro.prize_proc_mgr_flg", 1);
    // broadcast switch
    public static final boolean PRIZE_SUPPRORT_PROC_MGR_BR = PRIZE_SUPPRORT_PROC_MGR && getIntBit("ro.prize_proc_mgr_flg", 2);
    // service switch
    public static final boolean PRIZE_SUPPRORT_PROC_MGR_SR = PRIZE_SUPPRORT_PROC_MGR && getIntBit("ro.prize_proc_mgr_flg", 3);
    // provider switch
    public static final boolean PRIZE_SUPPRORT_PROC_MGR_PR = PRIZE_SUPPRORT_PROC_MGR && getIntBit("ro.prize_proc_mgr_flg", 4);
    // @}

    public static final boolean PRIZE_LOCKSCREEN_INVERSE = getValue("ro.prize_lockscreen_inverse");

    public static final boolean PRIZE_NOTCH_SCREEN = getValue("ro.prize_notch_screen");
    public static final boolean PRIZE_NOTCH_SCREEN2 = getValue("ro.prize_notch_screen2");//Compatible with all liuhai screen


    /*-prize-add by lihuangyuan,for notchscreen cut -2018-05-02-start*/
    public static final boolean PRIZE_NOTCH_SCREEN_CUT = getValue("ro.prize.notchcut");
    /*-prize-add by lihuangyuan,for notchscreen cut -2018-05-02-end*/
	
	//prize tangzhengrong 20180507 Swipe-up Gesture Navigation bar start
	public static final boolean PRIZE_SWIPE_UP_GESTURE_NAVIGATION = getValue("ro.prize_gesture_navigation");
	//prize tangzhengrong 20180507 Swipe-up Gesture Navigation bar end

    public static final boolean PRIZE_CMCC_SWITCH = getValue("ro.prize.cmcc_switch");//use for Differentiating channels and 4G+ software

    /*-prize-add by huangpengfei,for incomming call game modle -2018-05-28-start*/
    public static final boolean PRIZE_CALL_GAME_MODLE = getValue("ro.prize_call_game_modle");
    /*-prize-add by huangpengfei,for incomming call game modle -2018-05-28-end*/

    // Added by zenghui for Prize smart wifi switch Prize-linkh-20180530 start
    public static final boolean PRIZE_SMART_WIFI_SWITCH = getValue("ro.prize_smart_wifi_switch");
    // Added by zenghui for Prize smart wifi switch Prize-linkh-20180530 end

    /**
     *  fast unlock keyguard for FACEID Finger . prize-liyongli-2018/4/2
     */
    public static final boolean PRIZE_KEYGUARD_UNLOCK_FAST = true;
	
	/**
     *  Install boot wizard . prize-yueliu-2018/6/14
     */
    public static final boolean PRIZE_WELCOME_APP = true;
	
    //prize add by xiarui for vibrate in silent and ring @{
    public static final boolean PRIZE_VIBRATE_CONTROL = getValue("ro.prize_vibrate_control");
    //@}

    // prize add by xiarui for led blink when incoming call 2018-08-07 @{
    public static final boolean PRIZE_LED_BLINK = getValue("ro.prize_led_blink");
    // @}
    
	/* PRIZE IncallUI zhoushuanghua add for CooTek SDK <2018_06_21> start */
    public static final boolean PRIZE_COOTEK_SDK = getValue("ro.prize.cootek.location");
    /* PRIZE IncallUI zhoushuanghua add for CooTek SDK <2018_06_21> end */    
        
    /**
    * key value is 1, return true, else return false
    * @param  String key
    * @return boolean
    * @see PrizeOption
    */
    private static boolean getValue(String key) {
        return SystemProperties.get(key).equals("1");
    }

    private static String getString(String key) {
        return SystemProperties.get(key);
    }

    private static boolean isFeatureTurnedOn(String key, boolean defValue) {
        return SystemProperties.getBoolean(key, defValue);
    }

    private static boolean isFeatureTurnedOn(String key) {
        return isFeatureTurnedOn(key, false);
    }     
    
    private static int getInt(String strkey) {
        return SystemProperties.getInt( strkey, 0xFFFFFF);
    }
    private static boolean getIntBit(String strkey, int bitIdx) {
    	   int v = getInt(strkey);
    	   int bitv = 0x01<<bitIdx;
    	   if(  (v&bitv)==0 )
    	   	return false;
    	   	
        return true;
    }
    
}
