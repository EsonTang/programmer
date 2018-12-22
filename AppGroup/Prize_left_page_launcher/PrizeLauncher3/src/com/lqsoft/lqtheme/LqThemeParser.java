package com.lqsoft.lqtheme;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.android.launcher3.ImageUtils;
import com.android.launcher3.Launcher;
import com.android.launcher3.R;
import com.android.launcher3.SQLSingleThreadExcutor;
import com.android.launcher3.Utilities;
import com.android.launcher3.lq.DefaultConfig;
import com.android.launcher3.lq.FindDefaultResoures;



public class LqThemeParser {
    private static List<String> mPkgsName;
	// ===========================================================
    // Constants
    // ===========================================================

	private static List<String> mClassName;
//	public static List<String> mIconsOverName = new ArrayList<>();

	private static List<String> mIconsName;
	public static Resources mResources;

    // ===========================================================
    // Fields
    // ===========================================================

/*//    private static Resources mResources;
	private static String[] mPkgs;
	private static String[] mClss;
	private static String[] mIconnames;
	private static List<String> mPkgsList;*/
	private ThemeParserBaseAdapter parserBaseAdapter;
    // ===========================================================
    // Constructors
    // ===========================================================

    // ===========================================================
    // Getter & Setter
    // ===========================================================

    public void setAdapter(ThemeParserBaseAdapter adapter){
        parserBaseAdapter = adapter;
    }
    
    public int getThemeType(String themeFilePath) {
        if(parserBaseAdapter == null){
            return 0;
        }
        return parserBaseAdapter.getThemeType(themeFilePath);
    }
    
    public String getApplyThemeFilePath(Context context,String themeFilePath) {
        if(parserBaseAdapter == null){
            return null;
        }
        return parserBaseAdapter.getApplyThemeFilePath(context,themeFilePath);
    }
    
    // ===========================================================
    // Methods for/from SuperClass/Interfaces
    // ===========================================================

    // ===========================================================
    // Methods
    // ===========================================================

    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================
    public static  Bitmap getCalendarIcon(Context applicationContext, String themePath,
			String iconName) {
		SharedPreferences sp =applicationContext.getSharedPreferences("CalendarIcon", Context.MODE_PRIVATE);
		String lastThemePath = sp.getString("last","");
		/*if(mResources == null || !themePath.equals(lastThemePath)){
			initResourse(applicationContext,themePath);
		}*/
		if(mResources==null) {
			 mResources= getResourse(applicationContext,themePath);
		}
		InputStream instr = null;
        Bitmap rettemp = null;
        try {
        	if(mResources!=null)
			instr = mResources.getAssets().open("theme/icon/dynamicicon/calendar/"+iconName+".png");
			if (instr != null) {
				rettemp = BitmapFactory.decodeStream(instr);
				instr.close();

//				mResources.getPreloadedDrawables().clear();
//				mResources=null;
				}
		} catch (IOException e) {
			e.printStackTrace();
		}
        return rettemp;
	}
    
	public static void applyWallpaper(final Context applicationContext,
			final String themePath) {
		Utilities.startSetWallpaperSevice(themePath, applicationContext,true,((Launcher)applicationContext).getWallService());
	}
    
    public static  InputStream getWallpaper(Context applicationContext, String themePath) {
		SharedPreferences sp =applicationContext.getSharedPreferences("CalendarIcon", Context.MODE_PRIVATE);
		String lastThemePath = sp.getString("last","");
		if(mResources==null) {
			 mResources= getResourse(applicationContext,themePath);
		}
		InputStream instr = null;
        try {
        	if(mResources!=null)
			instr = mResources.getAssets().open("theme/wallpaper/default_wallpaper"+".jpg");
//			mResources.getPreloadedDrawables().clear();
//			mResources=null;
		} catch (IOException e) {
			e.printStackTrace();
		}
        return instr;
	}
    
    
    
    
    
    public static  Bitmap getDeskIcon(Context applicationContext, String themePath,
			String iconName) {
		SharedPreferences sp =applicationContext.getSharedPreferences("CalendarIcon", Context.MODE_PRIVATE);
		String lastThemePath = sp.getString("last","");
		/*if(mResources == null || !themePath.equals(lastThemePath)){
			initResourse(applicationContext,themePath);
		}*/

		if(mResources==null) {
			 mResources= getResourse(applicationContext,themePath);
		}
		InputStream instr = null;
        Bitmap rettemp = null;
        try {
        	if(mResources!=null)
			instr = mResources.getAssets().open("theme/icon/dynamicicon/deskclock/"+iconName+".png");
			if (instr != null) {
				rettemp = BitmapFactory.decodeStream(instr);
				instr.close();
				}
		} catch (IOException e) {
			e.printStackTrace();
		}
        return rettemp;
	}
    
    
    public static  Bitmap getWeatherIcon(Context applicationContext, String themePath,
			String iconName,String path) {
		SharedPreferences sp =applicationContext.getSharedPreferences("CalendarIcon", Context.MODE_PRIVATE);
		String lastThemePath = sp.getString("last","");
	/*	if(mResources == null || !themePath.equals(lastThemePath)){
			initResourse(applicationContext,themePath);
		}*/

		if(mResources==null) {
			 mResources= getResourse(applicationContext,themePath);
		}
		InputStream instr = null;
        Bitmap rettemp = null;
        try {
        	if(mResources!=null)
			instr = mResources.getAssets().open(path+iconName+".png");
			if (instr != null) {
				rettemp = BitmapFactory.decodeStream(instr);
				instr.close();
				}
		} catch (IOException e) {
			e.printStackTrace();
		}
        return rettemp;
	}
    
    
    public static  Bitmap getQQIcon(Context applicationContext, String themePath,
			String iconName) {
		SharedPreferences sp =applicationContext.getSharedPreferences("CalendarIcon", Context.MODE_PRIVATE);
		String lastThemePath = sp.getString("last","");
		/*if(mResources == null || !themePath.equals(lastThemePath)){
			initResourse(applicationContext,themePath);
		}*/

		if(mResources==null) {
			 mResources= getResourse(applicationContext,themePath);
		}
		InputStream instr = null;
        Bitmap rettemp = null;
        try {
        	if(mResources!=null)
			instr = mResources.getAssets().open("theme/icon/"+iconName+".png");
			if (instr != null) {
				rettemp = BitmapFactory.decodeStream(instr);
				instr.close();
				}
		} catch (IOException e) {
			e.printStackTrace();
		}
        return rettemp;
	}
    
    
    public static  Bitmap getMaskIcon(Context applicationContext) {
		SharedPreferences sp =applicationContext.getSharedPreferences("CalendarIcon", Context.MODE_PRIVATE);
		String lastThemePath = sp.getString("last","");
		/*if(mResources == null || !themePath.equals(lastThemePath)){
			initResourse(applicationContext,themePath);
		}*/

		 String themePath=LqShredPreferences.getLqThemePath();
		if(mResources==null) {
			 mResources= getResourse(applicationContext,themePath);
		}
		InputStream instr = null;
        Bitmap rettemp = null;
        try {
        	if(mResources!=null)
			instr = mResources.getAssets().open("theme/filter/mask.png");
			if (instr != null) {
				rettemp = BitmapFactory.decodeStream(instr);
				instr.close();
				}
		} catch (IOException e) {
			e.printStackTrace();
		}
        return rettemp;
	}
    public static void   init() {

		if (mPkgsName == null) {
//			mPkgs=DefaultConfig.parserXmlByPull(DefaultConfig.default_overlay_icon_path);
		}
    }
    
    
    public static Bitmap getThemeIcon(Context applicationContext, String themePath,
 ComponentName comp) {

		/*if (!themePath.contains(FindDefaultResoures.DEFALUT_THEME_PATH)) {
			return null;
		}*/
		SharedPreferences sp = applicationContext.getSharedPreferences(
				"CalendarIcon", Context.MODE_PRIVATE);
		String lastThemePath = sp.getString("last", "");
		mPkgsName = DefaultConfig.sOverIconpkgs;
		mClassName = DefaultConfig.sOverIconclss;
		mIconsName = DefaultConfig.sOverIcons;
		/*
		 * if (mResources == null || !themePath.equals(lastThemePath)) {
		 * initResourse(applicationContext, themePath); }
		 */

		if(mResources==null) {
			 mResources= getResourse(applicationContext,themePath);
		}
		String iconName = null;
		String pkg;
		String cls;
		if(Launcher.over_icon_news) {
			 pkg = comp.getPackageName();
			 cls = comp.getClassName();
		}else {
			 pkg = ";" + comp.getPackageName().toLowerCase() + ";";
			 cls = ";" + comp.getClassName().toLowerCase() + ";";
		}
		if (mClassName.contains(cls)) {
			int i = mClassName.indexOf(cls);
			if (mClassName.get(i).equals(cls)) {
				iconName = mIconsName.get(i);
			}
		}
		InputStream instr = null;
		Bitmap rettemp = null;
		if (iconName != null) {
			try {
				if (mResources != null)
					instr = mResources.getAssets().open(
							"theme/icon/" + iconName + ".png");
				if (instr != null) {

					rettemp = BitmapFactory.decodeStream(instr);
					if(rettemp.getWidth()!=Utilities.sIconTextureHeight) {
						rettemp = ImageUtils.resize(rettemp, Utilities.sIconTextureHeight, Utilities.sIconTextureHeight);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return rettemp;

	}
    
    public static  boolean isInserticon(Context applicationContext, String themePath,
 ComponentName comp) {

		/*if (!themePath.contains(FindDefaultResoures.DEFALUT_THEME_PATH)) {
			return false;
		}*/
		SharedPreferences sp = applicationContext.getSharedPreferences(
				"CalendarIcon", Context.MODE_PRIVATE);
		String lastThemePath = sp.getString("last", "");

		 mPkgsName = DefaultConfig.sOverIconpkgs;
		 mClassName = DefaultConfig.sOverIconclss;
		 mIconsName  = DefaultConfig.sOverIcons;

		 
		 if(comp==null) {
			 return false;
		 }
			if(mResources==null) {
				 mResources= getResourse(applicationContext,themePath);
			}
		String iconName = null;
		String pkg;
		String cls;
		
		if(Launcher.over_icon_news) {
			 pkg = comp.getPackageName();
			 cls = comp.getClassName();
		}else {
			 pkg = ";" + comp.getPackageName().toLowerCase() + ";";
			 cls = ";" + comp.getClassName().toLowerCase() + ";";
		}
		if (mClassName.contains(cls)) {
			int i = mClassName.indexOf(cls);
			if (mClassName.get(i).equals(cls)) {
				iconName = mIconsName.get(i);
				/*if(!mIconsOverName.contains(iconName)) {
					mIconsOverName.add(iconName);
				}else {
					if(!DefaultConfig.findDefault(DefaultConfig.default_workspace_path, comp)) {
						return false;
					}
				}*/
			}
		}
		InputStream instr = null;
		if (iconName != null) {
			try {
				if (mResources != null)
					instr = mResources.getAssets().open(
							"theme/icon/" + iconName + ".png");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return instr != null;
	}
    
    public static boolean isDefaultLoacalTHeme() {
    	return LqShredPreferences.getLqThemePath().contains("default")&&LqShredPreferences.getLqThemePath().contains(FindDefaultResoures.DEFALUT_THEME_PATH);
    }
    
    
    /*public static  void initResourse(Context context,String themePath) {
		 
        try {
       	 AssetManager asm = AssetManager.class.newInstance();
			AssetManager.class.getMethod("addAssetPath", String.class).invoke(asm, themePath);
			Resources res = context.getResources();
			mResources = new Resources(asm, res.getDisplayMetrics(), res.getConfiguration());
			SharedPreferences sp =context.getSharedPreferences("CalendarIcon", Context.MODE_PRIVATE);
			sp.edit().putString("last", themePath).commit();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		}
	}*/
    
    
    public static  Resources getResourse(Context context,String themePath) {
    	Resources s=null;
        try {
       	 AssetManager asm = AssetManager.class.newInstance();
			AssetManager.class.getMethod("addAssetPath", String.class).invoke(asm, themePath);
			Resources res = context.getResources();
			 s= new Resources(asm, res.getDisplayMetrics(), res.getConfiguration());
			SharedPreferences sp =context.getSharedPreferences("CalendarIcon", Context.MODE_PRIVATE);
			sp.edit().putString("last", themePath).commit();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		}
        return s;
	}

}
