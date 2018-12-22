package com.goodix.util;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class Preferences{
    private static final String mName = "Preferences";
    private static final String PASSWORD = "password";
    
    public static int getPassword(Context context)
    {
        SharedPreferences mPfc = context.getSharedPreferences(mName,
                Activity.MODE_PRIVATE);
        return mPfc.getInt(PASSWORD, 1234);
    }
    
    public static void setPassword(Context context, int password)
    {
        SharedPreferences mPfc = context.getSharedPreferences(mName,
                Activity.MODE_PRIVATE);
        Editor editor = (Editor) mPfc.edit();
        editor.putInt(PASSWORD, password);
        editor.commit();
    }
    
    
    private static final String PASSWORD_STATUS = "password.status";
    
    public static boolean getPasswordIsBeUsed(Context context)
    {
        SharedPreferences mPfc = context.getSharedPreferences(mName,
                Activity.MODE_PRIVATE);
        return mPfc.getBoolean(PASSWORD_STATUS, true);
    }
    
    public static void setPasswordIsBeUsed(Context context, boolean bUsed)
    {
        SharedPreferences mPfc = context.getSharedPreferences(mName,
                Activity.MODE_PRIVATE);
        Editor editor = (Editor) mPfc.edit();
        editor.putBoolean(PASSWORD_STATUS, bUsed);
        editor.commit();
    }
    
    private static final String SERIALPORT = "serialport";
    private static final String BAUDRATE = "baudrate";
    
    public static String getSerialport(Context context)
    {
        SharedPreferences mPfc = context.getSharedPreferences(mName,
                Activity.MODE_PRIVATE);
        return mPfc.getString(SERIALPORT, null);
    }
    
    public static void setSerialport(Context context, String serialport)
    {
        SharedPreferences mPfc = context.getSharedPreferences(mName,
                Activity.MODE_PRIVATE);
        Editor editor = (Editor) mPfc.edit();
        editor.putString(SERIALPORT, serialport);
        editor.commit();
    }
    
    public static String getBaudrate(Context context)
    {
        SharedPreferences mPfc = context.getSharedPreferences(mName,
                Activity.MODE_PRIVATE);
        return mPfc.getString(BAUDRATE, null);
    }
    
    public static void setBaudrate(Context context, String baudrate)
    {
        SharedPreferences mPfc = context.getSharedPreferences(mName,
                Activity.MODE_PRIVATE);
        Editor editor = (Editor) mPfc.edit();
        editor.putString(BAUDRATE, baudrate);
        editor.commit();
    }
    
    private static final String ENABLE_DESCRIPTION = "enable.description";
    
    private static final String ENABLE_SHOWMESSAGE = "enable.showmessage";

    
    public static boolean getIsEnableDescription(Context context)
    {
        SharedPreferences mPfc = context.getSharedPreferences(mName,
                Activity.MODE_PRIVATE);
        /* Ĭ��ֵ����ʾ */
        return mPfc.getBoolean(ENABLE_DESCRIPTION, true);
    }
    
    public static void setIsEnableDescription(Context context, boolean bEnabled)
    {
        SharedPreferences mPfc = context.getSharedPreferences(mName,
                Activity.MODE_PRIVATE);
        Editor editor = (Editor) mPfc.edit();
        editor.putBoolean(ENABLE_DESCRIPTION, bEnabled);
        editor.commit();
    }
    
    public static boolean getIsEnableShowMessage(Context context)
    {
        SharedPreferences mPfc = context.getSharedPreferences(mName,
                Activity.MODE_PRIVATE);
        /* Ĭ��ֵ����ʾ */
        return mPfc.getBoolean(ENABLE_SHOWMESSAGE, false);
    }
    
    public static void setIsEnableShowMessage(Context context, boolean bEnabled)
    {
        SharedPreferences mPfc = context.getSharedPreferences(mName,
                Activity.MODE_PRIVATE);
        Editor editor = (Editor) mPfc.edit();
        editor.putBoolean(ENABLE_SHOWMESSAGE, bEnabled);
        editor.commit();
    }
    
    private static final String ENABLE_FP_LOCKSCREEN = "enable.fp.lockscreen";
    
    public static boolean getIsEnableFpUnlockscreen(Context context)
    {
        SharedPreferences mPfc = context.getSharedPreferences(mName,
                Activity.MODE_PRIVATE);
        return mPfc.getBoolean(ENABLE_FP_LOCKSCREEN, true);
    }
    
    public static void setIsEnableFpUnlockscreen(Context context,
            boolean bEnabled)
    {
        SharedPreferences mPfc = context.getSharedPreferences(mName,
                Activity.MODE_PRIVATE);
        Editor editor = (Editor) mPfc.edit();
        editor.putBoolean(ENABLE_FP_LOCKSCREEN, bEnabled);
        editor.commit();
    }
    
    private static final String UNLOCKSCREEN_IS_NEED_PASSWORD = "need.password";
    
    public static boolean getIsEnablePswUnlockscreen(Context context)
    {
        SharedPreferences mPfc = context.getSharedPreferences(mName,
                Activity.MODE_PRIVATE);
        return mPfc.getBoolean(UNLOCKSCREEN_IS_NEED_PASSWORD, false);
    }
    
    public static void setIsEnablePswUnlockscreen(Context context,
            boolean bEnabled)
    {
        SharedPreferences mPfc = context.getSharedPreferences(mName,
                Activity.MODE_PRIVATE);
        Editor editor = (Editor) mPfc.edit();
        editor.putBoolean(UNLOCKSCREEN_IS_NEED_PASSWORD, bEnabled);
        editor.commit();
    }
    
    private static final String SETTING_IS_NEED_PASSWORD = "setting.password";
    
    public static boolean getIsEnablePswSetting(Context context)
    {
        SharedPreferences mPfc = context.getSharedPreferences(mName,
                Activity.MODE_PRIVATE);
        return mPfc.getBoolean(SETTING_IS_NEED_PASSWORD, true);
    }
    
    public static void setIsEnablePswSetting(Context context, boolean bEnabled)
    {
        SharedPreferences mPfc = context.getSharedPreferences(mName,
                Activity.MODE_PRIVATE);
        Editor editor = (Editor) mPfc.edit();
        editor.putBoolean(SETTING_IS_NEED_PASSWORD, bEnabled);
        editor.commit();
    }
    
    private static final String SAVE_REGISTER_FILE = "save.register";
    
    public static boolean getSaveRegisterFile(Context context)
    {
        SharedPreferences mPfc = context.getSharedPreferences(mName,
                Activity.MODE_PRIVATE);
        return mPfc.getBoolean(SAVE_REGISTER_FILE, true);
    }
    
    public static void setSaveRegisterFile(Context context, boolean bEnabled)
    {
        SharedPreferences mPfc = context.getSharedPreferences(mName,
                Activity.MODE_PRIVATE);
        Editor editor = (Editor) mPfc.edit();
        editor.putBoolean(SAVE_REGISTER_FILE, bEnabled);
        editor.commit();
    }
    
    private static final String SAVE_CALIBRATION_FILE = "save.calibration";
    
    public static boolean getSaveCalibrationFile(Context context)
    {
        SharedPreferences mPfc = context.getSharedPreferences(mName,
                Activity.MODE_PRIVATE);
        return mPfc.getBoolean(SAVE_CALIBRATION_FILE, false);
    }
    
    public static void setSaveCalibrationFile(Context context, boolean bEnabled)
    {
        SharedPreferences mPfc = context.getSharedPreferences(mName,
                Activity.MODE_PRIVATE);
        Editor editor = (Editor) mPfc.edit();
        editor.putBoolean(SAVE_CALIBRATION_FILE, bEnabled);
        editor.commit();
    }
    
    private static final String MATCH_REGISTER_FILE = "match.register";
    
    public static boolean getSaveMatchFile(Context context)
    {
        SharedPreferences mPfc = context.getSharedPreferences(mName,
                Activity.MODE_PRIVATE);
        return mPfc.getBoolean(MATCH_REGISTER_FILE, true);
    }
    
    public static void setSaveMatchFile(Context context, boolean bEnabled)
    {
        SharedPreferences mPfc = context.getSharedPreferences(mName,
                Activity.MODE_PRIVATE);
        Editor editor = (Editor) mPfc.edit();
        editor.putBoolean(MATCH_REGISTER_FILE, bEnabled);
        editor.commit();
    }
    
    private static boolean bEnableEM = false;
    
    public static boolean getEnableEM()
    {
        return bEnableEM;
    }
    
    public static void setEnableEM(boolean bEnable)
    {
        bEnableEM = bEnable;
    }
}
