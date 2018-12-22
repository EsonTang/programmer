package com.example.longshotscreen.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferenceUtils
{
	private static SharedPreferences mPrefs = null;

	public static SharedPreferences getPrefs(Context context)
	{
		SharedPreferences sharedPreferences = null;
		try
		{
			if (mPrefs == null)
				mPrefs = context.getSharedPreferences("SuperShot", 0);
			sharedPreferences = mPrefs;
			return mPrefs;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return sharedPreferences;
	}

	public static String getString(Context context, String key, String defValue)
	{
		return getPrefs(context).getString(key, defValue);
	}

	public static boolean putString(Context context, String key, String value)
	{
		return getPrefs(context).edit().putString(key, value).commit();
	}
}
