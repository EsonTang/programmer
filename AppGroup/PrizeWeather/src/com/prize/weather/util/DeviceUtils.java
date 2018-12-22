package com.prize.weather.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import android.content.Context;
import android.content.res.Resources;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;

/**
 * 
 * @author wangzhong
 *
 */
public class DeviceUtils {
	
	/**
	 * Whether they contain virtual navigation bar.
	 * @param context
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static boolean checkDeviceHasNavigationBar(Context context) {
		if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
			boolean hasNavigationBar = false;
			Resources rs = context.getResources();
			int id = rs.getIdentifier("config_showNavigationBar", "bool", "android");
			if (id > 0) {
				hasNavigationBar = rs.getBoolean(id);
			}
			try {
				Class systemPropertiesclass = Class.forName("android.os.SystemProperties");
				Method m = systemPropertiesclass.getMethod("get", String.class);
				String navBarOverride = (String) m.invoke(systemPropertiesclass, "qemu.hw.mainkeys");
				if ("1".equals(navBarOverride)) {
					hasNavigationBar = false;
				} else if ("0".equals(navBarOverride)) {
					hasNavigationBar = true;
				}
			} catch (Exception e) {
				return hasNavigationBar;
			}
			return hasNavigationBar;
		}
		return false;
	}

	/**
	 * The height of the virtual navigation bar.
	 * @param context
	 * @return
	 */
	public static int getNavigationBarHeight(Context context) {
		int navigationBarHeight = 0;
		if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
			Resources rs = context.getResources();
			int id = rs.getIdentifier("navigation_bar_height", "dimen", "android");
			if (id > 0 && checkDeviceHasNavigationBar(context)) {
				navigationBarHeight = rs.getDimensionPixelSize(id);
			}
			return navigationBarHeight;
		}
		return 0;
	}

	/**
	 * Get the height of the status bar.
	 * @return Returns the status bar height pixel values.
	 */
	public static int getStatusBarHeight(Context context) {
		int statusBarHeight = 0;
		if (statusBarHeight == 0) {
			try {
				Class<?> c = Class.forName("com.android.internal.R$dimen");
				Object o = c.newInstance();
				Field f = c.getField("status_bar_height");
				int x = (Integer) f.get(o);
				statusBarHeight = context.getResources().getDimensionPixelSize(x);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return statusBarHeight;
	}
	
	/**
	 * Get the height of the navigation bar.
	 * @return Returns the status bar height pixel values.
	 */
	public static int getNavigationBarHeight() {		
		/*if (SystemProperties.get("qemu.hw.mainkeys").equals("0")) {
			if (mNavigationBarHeight == 0) {
				try {
					Class<?> c = Class.forName("com.android.internal.R$dimen");
					Object o = c.newInstance();
					Field f = c.getField("navigation_bar_height");
					int x = (Integer) f.get(o);
					mNavigationBarHeight = getResources().getDimensionPixelSize(x);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return mNavigationBarHeight;
		} else {
			return 0;
		}*/
		return 0;
	}

}
