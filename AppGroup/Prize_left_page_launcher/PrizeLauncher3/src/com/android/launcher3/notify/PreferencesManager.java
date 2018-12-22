package com.android.launcher3.notify;

import java.io.File;

import com.android.launcher3.FileUtils;
import com.android.launcher3.Launcher;
import com.android.launcher3.LauncherAppState;
import com.android.launcher3.Utilities;
import com.android.launcher3.lq.DefaultConfig;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.provider.Settings;

public class PreferencesManager {

	private static final String SHARED_PREFERENCES_NAME = "com.szmttx.appstore.pioneer_preferences";

	public static final String KEY_GENERAL_CUSTOM_THEME = "custom_theme";
	public static final String KEY_GENERAL_CUSTOM_THEME_ENABLED = "custom_theme_enabled";
	public static final String KEY_GENERAL_THEME = "theme";
	public static final String KEY_DEFAULT_HOME_SCREEN = "default_home";
	public static final String KEY_GENERAL_INDEX = "index";
	public static final String KEY_DEFAULT_PAGE_STYLE = "key_default_page_style";
	public static final String KEY_CYCLE_INDEX = "set_cycle_slide";
	public static final String KEY_FOLDER_EFFECT = "folder_effect";
	public static final String KEY_GENERAL_ANIM_INDEX = "index_anim"; 
																		
	public static final String KEY_GENERAL_EFFECT_INDEX = "index_effect"; 
	public static final String KEY_LAYOUT_GRID_INDEX = "layout_grid_index"; 
	public static final String KEY_LAYOUT_GRID = "layout_grid"; 
																			
																			
	public static final String DEFAULT_THEME = "com.android.launcher3";
	public static final boolean DEFAULT_EXTEND_THEME = true;
	public static final String THEME_CHANGED_ACTION = "com.android.launcher.action.SET_CUSTOM_THEME";
	public static final int DEFAULT_EFFECT_INDEX = 0;
	public static final int DEFAULT_LAYOUT_GRID_INDEX = 0;
	public static final String DEFAULT_FOLDER_EFFECT_INDEX = "0";

	// add by zhouerlong
	private static String SP_NAME = "config";
	private static SharedPreferences sp;

	public static SharedPreferences getSharedPreferences(Context context) {
		return context.getSharedPreferences(SHARED_PREFERENCES_NAME,
				Context.MODE_PRIVATE);
	}

	public static String getCurrentTheme(Context context) {
		return getSharedPreferences(context).getString(KEY_GENERAL_THEME,
				DEFAULT_THEME);
	}

	public static void setCurrentTheme(Context context, String theme) {

		Editor e = getSharedPreferences(context).edit();
		e.putString(KEY_GENERAL_THEME, theme);
		e.commit();
	}
	
	
	/**保存默认首页配置
	 * @param context
	 * @param id
	 */
	public static void setDefaultHomeScreen(Context context,int id) {


		Editor e = getSharedPreferences(context).edit();
		e.putInt(KEY_DEFAULT_HOME_SCREEN, id);
		e.commit();
	}
	
	

	// add by zhouerlong
	/**读取默认首页配置
	 * @param context
	 * @return
	 */
	public static int getDefaultHomeScreen(Context context) {
        int d =Utilities.getDefaultHomepage();
		return getSharedPreferences(context).getInt(KEY_DEFAULT_HOME_SCREEN, d);
	}

	// add by zhouerlong
	public static int getCurrentThemeSelect(Context context) {
		return getSharedPreferences(context).getInt(KEY_GENERAL_INDEX, 0);
	}
	
	
	public static int getCurrentPageStyle(Context context) {
		return getSharedPreferences(context).getInt(KEY_DEFAULT_PAGE_STYLE, 0);
	}
	public static void setCurrentPageStyle(Context context,int style) {
		Editor e = getSharedPreferences(context).edit();
		e.putInt(KEY_DEFAULT_PAGE_STYLE, style);
		e.commit();
	}

	// add by zhouerlong
	public static int getCurrentAnimSelect(Context context) {
		return getSharedPreferences(context).getInt(KEY_GENERAL_ANIM_INDEX, 0);
	}// add by zel

	// add by zel

	public static boolean getKeyCycle(Context context) {
		return getSharedPreferences(context).getBoolean(KEY_CYCLE_INDEX, true);
	}

	public static void setCurrentCycle(Context context, boolean iscycle) {
		Editor e = getSharedPreferences(context).edit();
		e.putBoolean(KEY_CYCLE_INDEX, iscycle);
		e.commit();
	}
	
    //add by bxh
		public static int getCurrentLayoutGrid(Context context) {
			return getSharedPreferences(context).getInt(KEY_LAYOUT_GRID,
					DEFAULT_LAYOUT_GRID_INDEX);
		}
	
	// add by zel
	// add by zhouerlong
	public static int getCurrentEffectSelect(Context context) {

        int d= Utilities.getDefaultEffct();
		return getSharedPreferences(context).getInt(KEY_GENERAL_EFFECT_INDEX,
				d);
	}// add by zel

	// add by zel
	public static void setCurrentAnimation(Context context, int index) {
		Editor e = getSharedPreferences(context).edit();
		e.putInt(KEY_GENERAL_ANIM_INDEX, index);
		e.commit();

	}

	// add by zel
	public static void setCurrentEffect(Context context, int index) {
		Editor e = getSharedPreferences(context).edit();
		if(Launcher.isSupportGridChange){
			if(index >1){
				e.putInt(KEY_GENERAL_EFFECT_INDEX, index+2);
			}else if(index ==0){
				e.putInt(KEY_GENERAL_EFFECT_INDEX, index);
				e.putInt(KEY_LAYOUT_GRID, 20); //表示5x4布局
			}else{
				e.putInt(KEY_GENERAL_EFFECT_INDEX, index);
				e.putInt(KEY_LAYOUT_GRID, 16); //表示4x4布局
			}
		}else{
			e.putInt(KEY_GENERAL_EFFECT_INDEX, index);
		}
		e.commit();

	}

	// add by zhouerlong
	public static void saveInt(Context context, String key, int value) {
		if (sp == null)
			sp = context.getSharedPreferences(SP_NAME, 0);
		sp.edit().putInt(key, value).commit();
	}
	
	public static int getInt(Context context, String key, int defValue) {
		if (sp == null)
			sp = context.getSharedPreferences(SP_NAME, 0);
		return sp.getInt(key, defValue);
	}

}
