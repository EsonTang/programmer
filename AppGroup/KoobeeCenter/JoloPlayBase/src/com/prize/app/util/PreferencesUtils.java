/*******************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：
 *当前版本：
 *作	者：
 *完成日期：
 *修改记录：
 *修改日期：
 *版 本 号：
 *修 改 人：
 *修改内容：
...
 *修改记录：
 *修改日期：
 *版 本 号：
 *修 改 人：
 *修改内容：
 *********************************************/

package com.prize.app.util;

import com.prize.app.BaseApplication;
import com.prize.app.constants.Constants;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;

public class PreferencesUtils {
	public static String PREFERENCE_NAME = "PrizeAppCommon";

	private PreferencesUtils() {
		throw new AssertionError();
	}

	/**
	 * put string preferences
	 * 
	 * @param context
	 * @param key
	 *            The name of the preference to modify
	 * @param value
	 *            The new value for the preference
	 * @return True if the new values were successfully written to persistent
	 *         storage.
	 */
	public static boolean putString(Context context, String key, String value) {
		SharedPreferences settings = context.getSharedPreferences(
				PREFERENCE_NAME, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString(key, value);
		return editor.commit();
	}

	/**
	 * get string preferences
	 * 
	 * @param context
	 * @param key
	 *            The name of the preference to retrieve
	 * @return The preference value if it exists, or null. Throws
	 *         ClassCastException if there is a preference with this name that
	 *         is not a string
	 * @see #getString(Context, String, String)
	 */
	public static String getString(Context context, String key) {
		return getString(context, key, null);
	}

	/**
	 * get string preferences
	 * 
	 * @param context
	 * @param key
	 *            The name of the preference to retrieve
	 * @param defaultValue
	 *            Value to return if this preference does not exist
	 * @return The preference value if it exists, or defValue. Throws
	 *         ClassCastException if there is a preference with this name that
	 *         is not a string
	 */
	public static String getString(Context context, String key,
			String defaultValue) {
		SharedPreferences settings = context.getSharedPreferences(
				PREFERENCE_NAME, Context.MODE_PRIVATE);
		return settings.getString(key, defaultValue);
	}

	/**
	 * put int preferences
	 * 
	 * @param context
	 * @param key
	 *            The name of the preference to modify
	 * @param value
	 *            The new value for the preference
	 * @return True if the new values were successfully written to persistent
	 *         storage.
	 */
	public static boolean putInt(Context context, String key, int value) {
		SharedPreferences settings = context.getSharedPreferences(
				PREFERENCE_NAME, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
		editor.putInt(key, value);
		return editor.commit();
	}

	/**
	 * get int preferences
	 * 
	 * @param context
	 * @param key
	 *            The name of the preference to retrieve
	 * @return The preference value if it exists, or -1. Throws
	 *         ClassCastException if there is a preference with this name that
	 *         is not a int
	 * @see #getInt(Context, String, int)
	 */
	public static int getInt(Context context, String key) {
		return getInt(context, key, -1);
	}

	/**
	 * get int preferences
	 * 
	 * @param context
	 * @param key
	 *            The name of the preference to retrieve
	 * @param defaultValue
	 *            Value to return if this preference does not exist
	 * @return The preference value if it exists, or defValue. Throws
	 *         ClassCastException if there is a preference with this name that
	 *         is not a int
	 */
	public static int getInt(Context context, String key, int defaultValue) {
		SharedPreferences settings = context.getSharedPreferences(
				PREFERENCE_NAME, Context.MODE_PRIVATE);
		return settings.getInt(key, defaultValue);
	}

	/**
	 * put long preferences
	 * 
	 * @param context
	 * @param key
	 *            The name of the preference to modify
	 * @param value
	 *            The new value for the preference
	 * @return True if the new values were successfully written to persistent
	 *         storage.
	 */
	public static boolean putLong(Context context, String key, long value) {
		SharedPreferences settings = context.getSharedPreferences(
				PREFERENCE_NAME, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
		editor.putLong(key, value);
		return editor.commit();
	}

	/**
	 * get long preferences
	 * 
	 * @param context
	 * @param key
	 *            The name of the preference to retrieve
	 * @return The preference value if it exists, or -1. Throws
	 *         ClassCastException if there is a preference with this name that
	 *         is not a long
	 * @see #getLong(Context, String, long)
	 */
	public static long getLong(Context context, String key) {
		return getLong(context, key, -1);
	}

	/**
	 * get long preferences
	 * 
	 * @param context
	 * @param key
	 *            The name of the preference to retrieve
	 * @param defaultValue
	 *            Value to return if this preference does not exist
	 * @return The preference value if it exists, or defValue. Throws
	 *         ClassCastException if there is a preference with this name that
	 *         is not a long
	 */
	public static long getLong(Context context, String key, long defaultValue) {
		SharedPreferences settings = context.getSharedPreferences(
				PREFERENCE_NAME, Context.MODE_PRIVATE);
		return settings.getLong(key, defaultValue);
	}

	/**
	 * put float preferences
	 * 
	 * @param context
	 * @param key
	 *            The name of the preference to modify
	 * @param value
	 *            The new value for the preference
	 * @return True if the new values were successfully written to persistent
	 *         storage.
	 */
	public static boolean putFloat(Context context, String key, float value) {
		SharedPreferences settings = context.getSharedPreferences(
				PREFERENCE_NAME, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
		editor.putFloat(key, value);
		return editor.commit();
	}

	/**
	 * get float preferences
	 * 
	 * @param context
	 * @param key
	 *            The name of the preference to retrieve
	 * @return The preference value if it exists, or -1. Throws
	 *         ClassCastException if there is a preference with this name that
	 *         is not a float
	 * @see #getFloat(Context, String, float)
	 */
	public static float getFloat(Context context, String key) {
		return getFloat(context, key, -1);
	}

	/**
	 * get float preferences
	 * 
	 * @param context
	 * @param key
	 *            The name of the preference to retrieve
	 * @param defaultValue
	 *            Value to return if this preference does not exist
	 * @return The preference value if it exists, or defValue. Throws
	 *         ClassCastException if there is a preference with this name that
	 *         is not a float
	 */
	public static float getFloat(Context context, String key, float defaultValue) {
		SharedPreferences settings = context.getSharedPreferences(
				PREFERENCE_NAME, Context.MODE_PRIVATE);
		return settings.getFloat(key, defaultValue);
	}

	/**
	 * put boolean preferences
	 * 
	 * @param context
	 * @param key
	 *            The name of the preference to modify
	 * @param value
	 *            The new value for the preference
	 * @return True if the new values were successfully written to persistent
	 *         storage.
	 */
	public static boolean putBoolean(Context context, String key, boolean value) {
		SharedPreferences settings = context.getSharedPreferences(
				PREFERENCE_NAME, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
		editor.putBoolean(key, value);
		return editor.commit();
	}

	/**
	 * get boolean preferences, default is false
	 * 
	 * @param context
	 * @param key
	 *            The name of the preference to retrieve
	 * @return The preference value if it exists, or false. Throws
	 *         ClassCastException if there is a preference with this name that
	 *         is not a boolean
	 * @see #getBoolean(Context, String, boolean)
	 */
	public static boolean getBoolean(Context context, String key) {
		return getBoolean(context, key, false);
	}

	/**
	 * get boolean preferences
	 * 
	 * @param context
	 * @param key
	 *            The name of the preference to retrieve
	 * @param defaultValue
	 *            Value to return if this preference does not exist
	 * @return The preference value if it exists, or defValue. Throws
	 *         ClassCastException if there is a preference with this name that
	 *         is not a boolean
	 */
	public static boolean getBoolean(Context context, String key,
			boolean defaultValue) {
		SharedPreferences settings = context.getSharedPreferences(
				PREFERENCE_NAME, Context.MODE_PRIVATE);
		return settings.getBoolean(key, defaultValue);
	}
	
	/**
	 * 保存手机唯一标识id
	 * 
	 * @param context
	 */
	public static void saveKEY_TID(String uuid) {
		ContentResolver resolver = BaseApplication.curContext.getContentResolver();
		Settings.System.putString(resolver, Constants.KEY_TID, uuid);
	}
	/**
	 * 获取手机唯一标识id
	 * 
	 * @param context
	 */
	public static String getKEY_TID() {
		ContentResolver resolver = BaseApplication.curContext.getContentResolver();
		return Settings.System.getString(resolver, Constants.KEY_TID);
	}
}
