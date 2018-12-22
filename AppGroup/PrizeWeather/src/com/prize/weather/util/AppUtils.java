package com.prize.weather.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

/**
 * 
 * @author wangzhong
 *
 */
public class AppUtils {
	
	private AppUtils() {
		/* cannot be instantiated */
		throw new UnsupportedOperationException("cannot be instantiated");
	}

	/**
	 * 
	 */
	public static String getAppName(Context context) {
		try {
			PackageManager packageManager = context.getPackageManager();
			PackageInfo packageInfo = packageManager.getPackageInfo(
					context.getPackageName(), 0);
			int labelRes = packageInfo.applicationInfo.labelRes;
			return context.getResources().getString(labelRes);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Get the name of the application version information.
	 * 
	 * @param context
	 * @return The current application version of the name.
	 */
	public static String getVersionName(Context context) {
		try {
			PackageManager packageManager = context.getPackageManager();
			PackageInfo packageInfo = packageManager.getPackageInfo(
					context.getPackageName(), 0);
			return packageInfo.versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

}
