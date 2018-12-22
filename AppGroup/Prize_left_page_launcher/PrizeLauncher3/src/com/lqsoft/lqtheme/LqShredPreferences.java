package com.lqsoft.lqtheme;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class LqShredPreferences {
	// ===========================================================
	// Constants
	// ===========================================================
	private static Context mContext;
	private static SharedPreferences sp = null;
	private static boolean isLqtheme = true;
	public static String DEFAULT_THME = "system/media/config/theme/jinshu/jinshu.jar";//默认的主题 .jar
	public static String DEFAULT_PATH = "system/media/config/";//默认配置路径
	// ===========================================================
	// Fields
	// ===========================================================

	// ===========================================================
	// Constructors
	// ===========================================================
	
	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	// ===========================================================
	// Methods
	// ===========================================================
	public static void init(Context context) {
		mContext = context;
		sp = mContext.getSharedPreferences(OLThemeNotification.KEY_LQTHEME, Context.MODE_PRIVATE);
		isLqtheme = sp.getBoolean(OLThemeNotification.KEY_IS_LQTHEME, true);
	}
	public static boolean isLqtheme(Context context){
		if(sp == null){
			sp = context.getSharedPreferences(OLThemeNotification.KEY_LQTHEME, Context.MODE_PRIVATE);
			isLqtheme = sp.getBoolean(OLThemeNotification.KEY_IS_LQTHEME, true);
		}
		return isLqtheme;
	}
	
	public static String getLqThemePath(){
		if(sp == null){
			sp = mContext.getSharedPreferences(OLThemeNotification.KEY_LQTHEME, Context.MODE_PRIVATE);
		}
		return sp.getString(OLThemeNotification.KEY_LQTHEME_PATH, DEFAULT_THME);
	}
	
	public static void setLqtheme(boolean islqTheme,String themePath){
		if(sp == null){
			sp = mContext.getSharedPreferences(OLThemeNotification.KEY_LQTHEME, Context.MODE_PRIVATE);
		}
		Editor editor = sp.edit();
		editor.putBoolean(OLThemeNotification.KEY_IS_LQTHEME, islqTheme);
		editor.putString(OLThemeNotification.KEY_LQTHEME_PATH, themePath);
		editor.commit();
		isLqtheme = true;
	}
	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}
