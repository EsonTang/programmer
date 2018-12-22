package com.android.lpserver;

import android.content.Context;
import android.content.SharedPreferences;

public class Config {

    public static final String ACTION_QIANGHONGBAO_SERVICE_DISCONNECT = "com.codeboy.qianghongbao.ACCESSBILITY_DISCONNECT";
    public static final String ACTION_QIANGHONGBAO_SERVICE_CONNECT = "com.codeboy.qianghongbao.ACCESSBILITY_CONNECT";

    public static final String ACTION_NOTIFY_LISTENER_SERVICE_DISCONNECT = "com.codeboy.qianghongbao.NOTIFY_LISTENER_DISCONNECT";
    public static final String ACTION_NOTIFY_LISTENER_SERVICE_CONNECT = "com.codeboy.qianghongbao.NOTIFY_LISTENER_CONNECT";

    public static final String PREFERENCE_NAME = "config";
    public static final String KEY_ENABLE_WECHAT = "KEY_ENABLE_WECHAT";
    public static final String KEY_WECHAT_AFTER_OPEN_HONGBAO = "KEY_WECHAT_AFTER_OPEN_HONGBAO";
    public static final String KEY_WECHAT_DELAY_TIME = "KEY_WECHAT_DELAY_TIME";
    public static final String KEY_WECHAT_AFTER_GET_HONGBAO = "KEY_WECHAT_AFTER_GET_HONGBAO";
    public static final String KEY_WECHAT_MODE = "KEY_WECHAT_MODE";
    public static final String WECHAT_RECORDOR = "WECHAT_RECORDOR";

    public static final String KEY_NOTIFICATION_SERVICE_ENABLE = "KEY_NOTIFICATION_SERVICE_ENABLE";

    public static final String KEY_NOTIFY_SOUND = "KEY_NOTIFY_SOUND";
    public static final String KEY_NOTIFY_VIBRATE = "KEY_NOTIFY_VIBRATE";
    public static final String KEY_NOTIFY_NIGHT_ENABLE = "KEY_NOTIFY_NIGHT_ENABLE";
    public static final String KEY_ENABLE_SINGLE = "KEY_ENABLE_SINGLE";

    private static final String KEY_AGREEMENT = "KEY_AGREEMENT";

    public static final int WX_AFTER_OPEN_HONGBAO = 0;//Open a red envelope
    public static final int WX_AFTER_OPEN_SEE = 1; //see the luck
    public static final int WX_AFTER_OPEN_NONE = 2; //Quietly watching

    public static final int WX_AFTER_GET_GOHOME = 0; //Return to the desktop

    public static final int WX_MODE_3 = 3;//Manual grab

    public static boolean OPEN_SINGLE_SWITCH = false;

    private static Config current;

    public static synchronized Config getConfig(Context context) {
        if(current == null) {
            current = new Config(context.getApplicationContext());
        }
        return current;
    }

    private static SharedPreferences preferences;
    private Context mContext;

    private Config(Context context) {
        mContext = context;
        preferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
    }

    /** Whether to start WeChat grab a red envelope*/
    public boolean isEnableWechat() {
        return preferences.getBoolean(KEY_ENABLE_WECHAT, true) && UmengConfig.isEnableWechat(mContext);
    }

    /** WeChat open the event after the red envelope*/
    public int getWechatAfterOpenHongBaoEvent() {
        int defaultValue = 0;
        String result =  preferences.getString(KEY_WECHAT_AFTER_OPEN_HONGBAO, String.valueOf(defaultValue));
        try {
            return Integer.parseInt(result);
        } catch (Exception e) {}
        return defaultValue;
    }

    /** WeChat grab red after the event.*/
    public int getWechatAfterGetHongBaoEvent() {
        SharedPreferences sp = mContext.getSharedPreferences(KEY_WECHAT_AFTER_GET_HONGBAO,Context.MODE_PRIVATE);
        int defaultValue = sp.getInt("after_get_hongbao",0);
        return defaultValue;
    }

    /** WeChat open red envelope delay time*/
    public int getWechatOpenDelayTime() {
        int defaultValue = 0;
        String result = preferences.getString(KEY_WECHAT_DELAY_TIME, String.valueOf(defaultValue));
        try {
            return Integer.parseInt(result);
        } catch (Exception e) {}
        return defaultValue;
    }

    /** Grab WeChat red envelope mode*/
    public int getWechatMode() {
        int defaultValue = 0;
        String result = preferences.getString(KEY_WECHAT_MODE, String.valueOf(defaultValue));
        try {
            return Integer.parseInt(result);
        } catch (Exception e) {}
        return defaultValue;
    }

    /** Whether to start the notification bar mode*/
    public boolean isEnableNotificationService() {
        return preferences.getBoolean(KEY_NOTIFICATION_SERVICE_ENABLE, false);
    }

    public void setNotificationServiceEnable(boolean enable) {
        preferences.edit().putBoolean(KEY_NOTIFICATION_SERVICE_ENABLE, enable).apply();
    }

    /** Whether to open the sound*/
    public boolean isNotifySound() {
        return preferences.getBoolean(KEY_NOTIFY_SOUND, true);
    }

    /**Whether to open the single chat red packet*/
    public static boolean isOpenSingleChat(){
        // prize-set the true as false-for-bugid-35017-by-zhaojian-2017.6.20
        return preferences.getBoolean(KEY_ENABLE_SINGLE,false);
    }

    /** Whether to open the vibration*/
    public boolean isNotifyVibrate() {
        return preferences.getBoolean(KEY_NOTIFY_VIBRATE, true);
    }

    /** Whether to open the night without interruption mode*/
    public boolean isNotifyNight() {
        return preferences.getBoolean(KEY_NOTIFY_NIGHT_ENABLE, false);
    }

    /** Free declaration*/
    public boolean isAgreement() {
        return preferences.getBoolean(KEY_AGREEMENT, false);
    }

    /** Set whether or not to agree*/
    public void setAgreement(boolean agreement) {
        preferences.edit().putBoolean(KEY_AGREEMENT, agreement).apply();
    }

    //prize-add-zhaojian for pcba:switch should be off by default -2017824 start
//    public void initHBSwith() {
//        SharedPreferences sf = mContext.getSharedPreferences("hb_helper_init_switch", 0);
//        boolean inited = sf.getBoolean("inited", false);
//        if (!inited) {
//            // read system prop
//            boolean switchState = SystemProperties.getBoolean("ro.hongbao.switch", true);
//            // write switch state
//            preferences.edit().putBoolean(KEY_ENABLE_WECHAT,switchState).commit();
//            sf.edit().putBoolean("inited", true).commit();
//        }
//    }
    //prize-add-zhaojian for pcba:switch should be off by default -2017824 end
}
