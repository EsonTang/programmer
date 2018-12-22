package com.prize.qihoo.cleandroid.sdk;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.prize.qihoo.cleandroid.sdk.plugins.SharedPreferencesImpl;


/**
 * 跨进程调用时，需要实现，否则可能出现数据丢失的问题
 */
public class SharedPrefUtils {

    /** 破损包过滤路径 */
    public final static String KEY_CLEAR_APKPATH_FILTER = "clear_apkpath_filter";

    /** 系统缓存勾选包名列表 */
    public final static String KEY_CLEAR_APPCACHE_SELECT = "clear_appcache_select";

    /** 记录历史清理记录 */
    public static final String SYSCLEAR_TRASH_HISTORY = "o_c_t_h";

    public static String getString(Context context, String key, String defValue) {
        SharedPreferencesImpl sharedPreferencesImpl = new SharedPreferencesImpl(context);
        SharedPreferences  sharedPreferences = sharedPreferencesImpl.getDefaultSharedPreferences();
        return sharedPreferences.getString(key, defValue);
    }

    public static void setString(Context context, String key, String value) {

        SharedPreferencesImpl sharedPreferencesImpl = new SharedPreferencesImpl(context);
        SharedPreferences  sharedPreferences = sharedPreferencesImpl.getDefaultSharedPreferences();
        Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public static long getLong(Context context, String key, long defValue) {
        SharedPreferencesImpl sharedPreferencesImpl = new SharedPreferencesImpl(context);
        SharedPreferences  sharedPreferences = sharedPreferencesImpl.getDefaultSharedPreferences();
        return sharedPreferences.getLong(key, defValue);
    }

    public static void setLong(Context context, String key, long value) {
        SharedPreferencesImpl sharedPreferencesImpl = new SharedPreferencesImpl(context);
        SharedPreferences  sharedPreferences = sharedPreferencesImpl.getDefaultSharedPreferences();
        Editor editor = sharedPreferences.edit();
        editor.putLong(key, value);
        editor.commit();
    }

}
