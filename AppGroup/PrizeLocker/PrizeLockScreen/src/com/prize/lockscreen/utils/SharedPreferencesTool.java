package com.prize.lockscreen.utils;


import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.provider.Settings;
/***
 * SharedPreference辅助类
 * @author fanjunchen
 *
 */
public class SharedPreferencesTool {
	
	private static final String LOCKSCREEN_SHARE_NAME = "lockscreen_share";
	/**锁屏是否开启*/
	private static final String LOCKSCREEN_ENABLE = "lockscreen_enable";
	/**0表示无密码，1表示数字密码，2表示图案密码, 3表示混合密码*/
	private static final String LOCKSCREEN_STYLE = "lockscreen_style";
	/**无(关闭)锁屏密码*/
	public static final int LOCK_STYLE_NO_PASSWORD = 0;
	/**数字密码*/
	public static final int LOCK_STYLE_NUMBER_PASSWORD = 1;
	/**图案密码*/
	public static final int LOCK_STYLE_PATTERN_PASSWORD = 2;
	/**复杂密码*/
	public static final int LOCK_STYLE_COMPLEX_PASSWORD = 3;
	/**图案密码对应用key*/
	private static final String LOCKSCREEN_PATTERN_PWD = "lockscreen_pattern_pwd";
	/**数字密码对应的key*/
	private static final String LOCKSCREEN_NUMBER_PWD = "lockscreen_number_pwd";
	/**锁屏样式存放key*/
	public static final String KEYGUARD_TYPE = "keyguard_type";
	
	/**锁屏状态存放key 1:表示已经锁上, 0 表示没有*/
	public static final String PRIZE_LOCK_STATE = "prize_lock_state";
	
	private static SharedPreferences mSharedPreferences;

	private static SharedPreferences obtainSharedPreferences(Context context) {
		if (mSharedPreferences == null) {
			mSharedPreferences = context.getSharedPreferences(
					LOCKSCREEN_SHARE_NAME, Context.MODE_PRIVATE);
		}
		return mSharedPreferences;
	}

	/**
	 * 获取锁屏是否启用
	 * @param context
	 * @return 默认为开启
	 */
	public static boolean isLockScreenEnable(Context context) {
		SharedPreferences sp = obtainSharedPreferences(context);
		return sp.getBoolean(LOCKSCREEN_ENABLE, true);
	}

	/**
	 * 设置锁屏启用或禁用
	 * 
	 * @param context
	 * @param enable
	 */
	public static void setLockScreenEnable(Context context, boolean enable) {
		SharedPreferences sp = obtainSharedPreferences(context);
		Editor editor = sp.edit();
		editor.putBoolean(LOCKSCREEN_ENABLE, enable);
		editor.commit();
	}

	/**
	 * 获取锁屏密码方式
	 * @param context
	 * @return
	 */
	public static int getLockPwdType(Context context) {
		SharedPreferences sp = obtainSharedPreferences(context);
		return sp.getInt(LOCKSCREEN_STYLE, 0);
	}

	/**
	 * 设置锁屏密码方式
	 * 
	 * @param context
	 * @param style
	 */
	public static void setLockPwdType(Context context, int style) {
		SharedPreferences sp = obtainSharedPreferences(context);
		Editor editor = sp.edit();
		editor.putInt(LOCKSCREEN_STYLE, style);
		editor.commit();
	}

	/**
	 * 获取图案密码
	 * 
	 * @param context
	 * @return
	 */
	public static String getPatternPassword(Context context) {
		SharedPreferences sp = obtainSharedPreferences(context);
		return sp.getString(LOCKSCREEN_PATTERN_PWD, "");
	}

	/**
	 * 设置图案密码
	 * 
	 * @param context
	 * @param password
	 */
	public static void setPatternPassword(Context context, String password) {
		SharedPreferences sp = obtainSharedPreferences(context);
		Editor editor = sp.edit();
		editor.putString(LOCKSCREEN_PATTERN_PWD, password);
		editor.commit();
	}

	/**
	 * 获取数字密码
	 * 
	 * @param context
	 * @return
	 */
	public static String getNumberPassword(Context context) {
		SharedPreferences sp = obtainSharedPreferences(context);
		return sp.getString(LOCKSCREEN_NUMBER_PWD, "");
	}

	/**
	 * 设置数字密码
	 * 
	 * @param context
	 * @param password
	 */
	public static void setNumberPassword(Context context, String password) {
		SharedPreferences sp = obtainSharedPreferences(context);
		Editor editor = sp.edit();
		editor.putString(LOCKSCREEN_NUMBER_PWD, password);
		editor.commit();
	}
	/***
	 * 获取锁屏样式
	 * @param ctx
	 * @return
	 */
	public static int getLockStyle(Context ctx) {
		try {
			return Settings.System.getInt(ctx.getContentResolver(), KEYGUARD_TYPE);
		} catch (Exception e) {
			return 1;
		}
	}
	
	/***
	 * 设置锁屏样式
	 * @param ctx
	 * @return
	 */
	public static void setLockStyle(Context ctx, int type) {
		Settings.System.putInt(ctx.getContentResolver(), KEYGUARD_TYPE, type);
	}
	
	/***
	 * 设置锁屏状态
	 * @param ctx
	 * @param isLock boolean true:locked, false:unlock
	 */
	public static void setLockStatus(Context ctx, boolean isLock) {
		if (isLock)
			Settings.System.putInt(ctx.getContentResolver(), PRIZE_LOCK_STATE, 1);
		else
			Settings.System.putInt(ctx.getContentResolver(), PRIZE_LOCK_STATE, 0);
	}
}
