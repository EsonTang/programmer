package com.prize.left.page.util;

import org.xutils.common.util.LogUtil;
import org.xutils.http.RequestParams;

import com.android.launcher3.LauncherApplication;
import com.baidu.location.Address;
import com.prize.left.page.bean.table.PersonTable;
import com.prize.left.page.safe.XXTEAUtil;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.text.TextUtils;

public class PreferencesUtils {
	public static String PREFERENCE_NAME = "LeftPageCommon";

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
	 * 获取手机唯一标识id
	 * 
	 * @param context
	 */
	public static String getKEY_TID() {
		ContentResolver resolver = LauncherApplication.getInstance().getApplicationContext().getContentResolver();
		return Settings.System.getString(resolver, IConstants.KEY_TID);
	}
	
	/**
	 * 添加请求头信息
	 */
	public static void addHeaderParam(RequestParams reqParam,Context context){
		
		ClientInfo	clientInfo = ClientInfo.getInstance(context);		
		PersonTable p = LauncherApplication.getInstance().getLoginPerson();
		if(p!=null) {
			clientInfo.setUserId(p.userId);
		}
		Address address=LauncherApplication.getInstance().getAddress();
		if (address!=null) {
			clientInfo.setAddress(address);
					
			clientInfo.setClientStartTime(System.currentTimeMillis());
			clientInfo.setNetStatus(ClientInfo.networkType);
			clientInfo.tid = PreferencesUtils.getKEY_TID();
		}
		String headParams = CommonUtils.toGson(clientInfo);
		headParams = XXTEAUtil.getParamsEncypt(headParams);
		if (!TextUtils.isEmpty(headParams)) {
			reqParam.addHeader("params", headParams);
		}
	}
}
