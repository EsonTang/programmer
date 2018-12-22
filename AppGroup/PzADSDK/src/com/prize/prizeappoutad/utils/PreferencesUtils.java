package com.prize.prizeappoutad.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;

import com.prize.prizeappoutad.BaseApplication;
import com.prize.prizeappoutad.constants.Constants;

public class PreferencesUtils {
	public static String PREFERENCE_NAME = "przThirdAd";
	public static String PREFERENCEOUTAD_NAME = "przThirdOutAd";
	public static String ADDOWLOAD_ID_NAME = "przDownloadId";

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
				PREFERENCE_NAME, Context.MODE_MULTI_PROCESS);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString(key, value);
		return editor.commit();
	}

	/**
	 * 应用外广告put SP
	 * 
	 * @param context
	 * @param key
	 * @param value
	 * @return
	 */
	public static boolean putOutAdString(Context context, String key,
			String value) {
		SharedPreferences settings = context.getSharedPreferences(
				PREFERENCEOUTAD_NAME, Context.MODE_MULTI_PROCESS);
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
	 * 应用外广告get SP
	 * 
	 * @param context
	 * @param key
	 * @return
	 */
	public static String getOutAdString(Context context, String key) {
		return getOutAdString(context, key, null);
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
				PREFERENCE_NAME, Context.MODE_MULTI_PROCESS);
		return settings.getString(key, defaultValue);
	}

	/**
	 * 应用外广告get SP
	 * 
	 * @param context
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	public static String getOutAdString(Context context, String key,
			String defaultValue) {
		SharedPreferences settings = context.getSharedPreferences(
				PREFERENCEOUTAD_NAME, Context.MODE_MULTI_PROCESS);
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
				PREFERENCE_NAME, Context.MODE_MULTI_PROCESS);
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
				PREFERENCE_NAME, Context.MODE_MULTI_PROCESS);
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
				ADDOWLOAD_ID_NAME, Context.MODE_MULTI_PROCESS);
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
				ADDOWLOAD_ID_NAME, Context.MODE_MULTI_PROCESS);
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
				PREFERENCE_NAME, Context.MODE_MULTI_PROCESS);
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
				PREFERENCE_NAME, Context.MODE_MULTI_PROCESS);
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
				PREFERENCE_NAME, Context.MODE_MULTI_PROCESS);
		SharedPreferences.Editor editor = settings.edit();
		editor.putBoolean(key, value);
		return editor.commit();
	}

	/**
	 * clear preferences
	 * 
	 * @param context
	 * @return True if the new values were successfully written to persistent
	 *         storage.
	 */
	public static boolean clear(Context context) {
		SharedPreferences settings = context.getSharedPreferences(
				PREFERENCE_NAME, Context.MODE_MULTI_PROCESS);
		SharedPreferences.Editor editor = settings.edit();
		editor.clear();
		return editor.commit();
	}

	/**
	 * 清除SP
	 * 
	 * @param context
	 * @return
	 */
	public static boolean clearOutAd(Context context) {
		SharedPreferences settings = context.getSharedPreferences(
				PREFERENCEOUTAD_NAME, Context.MODE_MULTI_PROCESS);
		SharedPreferences.Editor editor = settings.edit();
		editor.clear();
		return editor.commit();
	}

	/**
	 * 清除下载id
	 * 
	 * @param context
	 * @return
	 */
	public static boolean clearDownloadId(Context context) {
		SharedPreferences settings = context.getSharedPreferences(
				ADDOWLOAD_ID_NAME, Context.MODE_MULTI_PROCESS);
		SharedPreferences.Editor editor = settings.edit();
		editor.clear();
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
				PREFERENCE_NAME, Context.MODE_MULTI_PROCESS);
		return settings.getBoolean(key, defaultValue);
	}

	/**
	 * 保存手机唯一标识id
	 * 
	 * @param context
	 */
	public static void saveKEY_TID(Context context, String uuid) {
		ContentResolver resolver = context.getContentResolver();
		Settings.System.putString(resolver, Constants.KEY_TID, uuid);
	}

	/**
	 * 获取手机唯一标识id
	 * 
	 * @param context
	 */
	public static String getKEY_TID(Context context) {
		ContentResolver resolver = BaseApplication.curContext
				.getContentResolver();
		return Settings.System.getString(resolver, Constants.KEY_TID);
	}
}
