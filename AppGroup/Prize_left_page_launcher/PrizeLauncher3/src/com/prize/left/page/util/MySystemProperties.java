package com.prize.left.page.util;

import java.lang.reflect.Method;

public class MySystemProperties {
	
	private static final String TAG = "MySystemProperties";
	/**koobee的手机*/
	public final static String KOOBEE = "dido";
	/**coosea的手机*/
	public final static String COOSEA = "bingo";
	// String SystemProperties.get(String key){}
	public static String get(String key) {
		init();
		
		String value = null;
		
		try {
			value = (String) mGetMethod.invoke(mClassType, key);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return value;
	}
	
	//int SystemProperties.get(String key, int def){}
	public static int getInt(String key, int def) {
		init();
		
		int value = def;
		try {
			Integer v = (Integer) mGetIntMethod.invoke(mClassType, key, def);
			value = v.intValue();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return value;
	}
	
	public static int getSysVersion() {
		return getInt("ro.build.version.sdk", -1);
	}
	/**
	 * 判断是否为koobee的手机
	 * @return
	 */
	public static boolean isKoobee() {
		String str = getSdkVersion();
		if (str != null && str.contains(KOOBEE))
			return true;
		return false;
	}
	/***
	 * 获取系统版本
	 * @return
	 */
	public static String getSdkVersion() {
		return get("ro.product.system.version");
	}
	
	//-------------------------------------------------------------------
	private static Class<?> mClassType = null;
	private static Method mGetMethod = null;
	private static Method mGetIntMethod = null;
	private static void init() {
		try {
			if (mClassType == null) {
				mClassType = Class.forName("android.os.SystemProperties");
				
				mGetMethod = mClassType.getDeclaredMethod("get", String.class);
				mGetIntMethod = mClassType.getDeclaredMethod("getInt", String.class, int.class);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
