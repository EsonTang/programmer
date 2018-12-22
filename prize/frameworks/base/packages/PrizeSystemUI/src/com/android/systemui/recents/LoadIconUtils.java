package com.android.systemui.recents;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;


import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.UserHandle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Xml;
import java.util.ArrayList;
import android.database.Cursor;
import android.net.Uri;
import android.view.Display;
import android.view.WindowManager;
import com.android.systemui.statusbar.phone.NotificationPanelView;

public class LoadIconUtils {

    private static final String TAG = "systemui.recents.LoadIconUtils";
    public static final String THEME_EXE_ACTION = "appley_theme_ztefs";
    public static final String THEME_EXE_PATH_KEY = "themePath";
    private static final boolean over_icon_news = true;
	private static final String DEFALUT_THEME_PATH = "/system/media/config/";
    public static String path = "/system/media/config/theme/default/default.jar";//launcher theme path

    private static List<String> sPkgsName = new ArrayList<String>();
	private static List<String> sClassName = new ArrayList<String>();

	private static List<String> sIconsName = new ArrayList<String>();
    
    public static void registerLauncherThemeReceiver(Context context, BroadcastReceiver mReceiver){
        IntentFilter filter = new IntentFilter();
        filter.addAction(THEME_EXE_ACTION);
        context.registerReceiverAsUser(mReceiver,UserHandle.ALL,filter,null,null);
    }
    
    public static void unRegisterLauncherThemeReceiver(Context context, BroadcastReceiver mReceiver){
        context.unregisterReceiver(mReceiver);
    }
    
    public static Drawable requestIcon(Context context, ComponentName comp, int appInstanceIndex){
        ComponentName comName = null;
        if(appInstanceIndex == 1){
            comName = new ComponentName(comp.getPackageName(),comp.getClassName()+"s");
        }
        InputStream is = getIcon(context, path, comName != null ? comName : comp);
        if(is != null){
            Bitmap bitmap = BitmapFactory.decodeStream(is);
            if(bitmap != null){
                return new BitmapDrawable(context.getResources(),bitmap);
            }
        }
        return null;
    }

    public static boolean copyFile(InputStream in, File outFile) {
        if (null == in || null == outFile)
            return false;
        if (outFile.exists()) {
            outFile.delete();
        }
        byte[] buffer = new byte[1024];
        FileOutputStream out = null;
        try {
            if(!outFile.exists()){
                outFile.getParentFile().mkdirs();
                outFile.createNewFile();
            }
            out = new FileOutputStream(outFile);
            int len = -1;
            while((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
            out.flush();
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        finally {
            try {
                if (in != null)
                    in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (out != null)
                    out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }
    
    public static  InputStream getlock(Context applicationContext, String themePath) {
        if(themePath == null) return null;
        Resources mResources = getResourse(applicationContext,themePath);
        InputStream instr = null;
        Bitmap rettemp = null;
        try {
            if(mResources!=null)
            instr = mResources.getAssets().open("theme/lock/livewallpaper"+".zip");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return instr;
    }
    
    
    public static InputStream getIconByZip(String themeFilePath, String name,Context context) {
		InputStream t = null;
		BufferedInputStream bis = null;
		ZipFile mZipFile = null;
		try {

			if (mZipFile == null) {
				File file = new File(themeFilePath);
				mZipFile = new ZipFile(themeFilePath);
			}
			String iconDir = "theme/" + getFolder(context) + "/icon/";
			ZipEntry e =mZipFile.getEntry(iconDir+name);
			if(e==null) {
				return null;
			}
			t = mZipFile.getInputStream(e);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return t;
	}
    
    public static int getDpi(Context context){
		int dpi = 0;
		WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		Display display = windowManager.getDefaultDisplay();
		DisplayMetrics displayMetrics = new DisplayMetrics();
		@SuppressWarnings("rawtypes")
		Class c;
		try {
			c = Class.forName("android.view.Display");
			@SuppressWarnings("unchecked")
			Method method = c.getMethod("getRealMetrics",DisplayMetrics.class);
			method.invoke(display, displayMetrics);
			dpi=displayMetrics.heightPixels;
		}catch(Exception e){
			e.printStackTrace();
		}
		return dpi;
	}
    public static String getFolder(Context context) {
    	
    	DisplayMetrics dm = context.getResources().getDisplayMetrics();
		int width = dm.widthPixels;
		int height = dm.heightPixels;
		int viewHeight = getDpi(context);
		int screenWidth;
		int screenHeight;
		if (width > height ) {
			screenWidth = height;
			screenHeight = width;
			if(width<viewHeight) {
				screenHeight = viewHeight;
			}
		} else {
			screenWidth = width;
			screenHeight = height;
			if(height < viewHeight)
				screenHeight = viewHeight;
		}

        Log.d("dpi-now","screenWidth--------->"+screenWidth);
        Log.d("dpi-now","screenHeight--------->"+screenHeight);
		String dpi = "xhdpi";
				if (screenWidth <= 540 && screenHeight <= 960) { // hdpi
					dpi = "hdpi";
				} else if (screenWidth > 540 && screenWidth <= 720 && screenHeight > 960
						&& screenHeight <= 1280) { // xhdpi
					dpi = "xhdpi";
				} else if (screenWidth > 720 && screenWidth <= 1080 && screenHeight > 1280
						&& screenHeight <= 1920) { // xxxhdpi
					dpi = "xxhdpi";
				}else{
					dpi = "dpi";
				}
				
				if(width > 540 && width < 720 && height>=1280){
					dpi = "dpi";
				}
        Log.d("dpi-now","dpi--------->"+dpi);
		return dpi;
	}
    
    public static InputStream getlockByZip(String themeFilePath) {
		// long costTime = System.currentTimeMillis();
		InputStream t = null;
		BufferedInputStream bis = null;
		ZipFile mZipFile = null;
		try {
			if (mZipFile == null) {
				File file = new File(themeFilePath);
				mZipFile = new ZipFile(themeFilePath);
			}
			ZipEntry e =mZipFile.getEntry("theme/lock/lock"+".rar");
			if(e==null) {
				return null;
			}
			t = mZipFile.getInputStream(e);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return t;
	}
    
    public static Bitmap getWallpaper(Context applicationContext, String themePath) {
   	 if(themePath == null) return null;
        Resources mResources = getResourse(applicationContext,themePath);
        InputStream instr = null;
        Bitmap rettemp = null;
        try {
            if(mResources!=null){
           	 instr = mResources.getAssets().open("theme/wallpaper/default_wallpaper.jpg");
            }
            rettemp = BitmapFactory.decodeStream(instr);
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        if (rettemp == null) {
        	InputStream t = null;
    		ZipFile mZipFile = null;
    		try {
    			if (mZipFile == null) {
    				File file = new File(themePath);
    				mZipFile = new ZipFile(themePath);
    			}
    			String wallpaperDir = "theme/" + getFolder(applicationContext) + "/wallpaper/default_wallpaper.jpg";
    			ZipEntry e =mZipFile.getEntry(wallpaperDir);
    			if(e==null) {
    				return null;
    			}
    			t = mZipFile.getInputStream(e);
    			rettemp = BitmapFactory.decodeStream(t);
    		} catch (Exception e) {
    			e.printStackTrace();
    		}
        }
        return rettemp;
   }

    
    public static String getCurrrentThemePath(Context c) {
		String path = getThemePath(c);
		/*Cursor cursor = c.getContentResolver().query(
				Uri.parse("content://com.nqmobile.live.base.dataprovider" + "/"
						+ "theme_local"), null, "isSelected" + "=?",
				new String[] { "1" }, null);
		if (cursor != null && cursor.moveToNext()) {
			path = cursor.getString(cursor.getColumnIndex("themePath"));

		}*/
		return path.isEmpty() ? null : path;
	}
    
	public static void init(Context context, String str) {
		String p = getCurrrentThemePath(context);
		if(p != null){
        Log.d(TAG,"getCurrrentThemePath--->"+p);
			path = p;
		}
		findOverIcons(str);
	}
    
	public static LinkedHashMap<String, String> findOverIcons(String path) {
		String configPath = path;
		File configFile = new File(configPath);
		if (!configFile.exists()) {
			return null;
		}
		InputStream is = null;
		LinkedHashMap<String, String> configs = new LinkedHashMap<>();
		List<String> arrays = null;
		try {
			is = new FileInputStream(configFile);
			XmlPullParser xpp = Xml.newPullParser();
			xpp.setInput(is, "UTF-8");
			int eventType = xpp.getEventType();
			while (eventType != XmlPullParser.END_DOCUMENT) {
				switch (eventType) {
				case XmlPullParser.START_DOCUMENT:
					break;
				case XmlPullParser.START_TAG:
					if (xpp.getName().equals("string-array")) {
						if (xpp.getAttributeValue(0).equals(
								"overlay_icon_package")) {
							arrays = sPkgsName;
						} else if (xpp.getAttributeValue(0).equals(
								"overlay_icon_class")) {
							arrays = sClassName;
						} else if (xpp.getAttributeValue(0).equals(
								"overlay_icon_image")) {
							arrays = sIconsName;
						}

					} else if (xpp.getName().equals("item")) {
						xpp.next();
					    //add by zhouerlong  20170205


						if(over_icon_news) {
						try {

							String item = xpp.getText();
							String[] items = item.split(";");
							
							sPkgsName.add(items[0]);
							if(over_icon_news) {
							sClassName.add(items[0]+";"+items[1]);
							}else {
								sClassName.add(items[1]);
							
							}
							sIconsName.add(items[2]);
						} catch (Exception e) {
							// TODO: handle exception
						}
						}else {
							arrays.add(xpp.getText());
						}
					    //add by zhouerlong  20170205
					}
					break;
				case XmlPullParser.END_TAG:
					break;
				}
				eventType = xpp.next();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return configs;
	}
    

    private static InputStream getIcon(Context applicationContext,
            String themePath, ComponentName comp) {
    	String kb="/system/media/config/default_config/overlay_icon_koobee.xml";
    	String ks ="/system/media/config/default_config/overlay_icon_koosai.xml";
    	if(sPkgsName.isEmpty()) {
    		String r = MySystemPropertiesPrize.isKoobee() ? kb : ks;
    		Log.d(TAG,"overlay_icon file ------------------>"+r);
    		init(applicationContext, r);
    	}

		//if (!themePath.contains(DEFALUT_THEME_PATH)) {
		//	return null;
		//}
		SharedPreferences sp = applicationContext.getSharedPreferences(
				"CalendarIcon", Context.MODE_PRIVATE);
		String lastThemePath = sp.getString("last", "");

		Resources mResources = getResourse(applicationContext, themePath);
        //Log.d(TAG,"pkg------------------>"+comp.getPackageName().toLowerCase());
        //Log.d(TAG,"cls------------------>"+comp.getClassName().toLowerCase());
		String iconName = null;

		String coms = null;
		String pkg = ";" + comp.getPackageName().toLowerCase() + ";";
		String cls = ";" + comp.getClassName().toLowerCase() + ";";

	    //rm by zhouerlong  20170205
      /*  if (sClassName.contains(cls)) {
            for(int i = 0;i<sClassName.size();i++){
                String p = sClassName.get(i);
                if(p.equals(cls)){
                    if (sClassName.get(i).equals(cls)) {
                        iconName = sIconsName.get(i);
                        if(iconName != null){
                            break;
                        }
                    }
                }
            }
            //int i = mPkgsList.indexOf(pkg);
            //if (mClss[i].equals(cls)) {
            //    iconName = mIconnames[i];
            //}
        }*/
	    //rm by zhouerlong  20170205
		
		
	    //----------add by zhouerlong  20170205-------------------------

		
		if(over_icon_news) {
			 pkg = comp.getPackageName().toLowerCase();
			 cls = comp.getClassName().toLowerCase();
			  coms = pkg+";"+cls;
		}else {
			 pkg = ";" + comp.getPackageName().toLowerCase() + ";";
			 cls = ";" + comp.getClassName().toLowerCase() + ";";
		}
		
	
		if(over_icon_news) {

		if (sClassName.contains(coms)) {
			int i = sClassName.indexOf(coms);
				iconName = sIconsName.get(i);
		}
		}
		else {
		if (sClassName.contains(cls)) {
			int i = sClassName.indexOf(cls);
			if (sClassName.get(i).equals(cls)) {
				iconName = sIconsName.get(i);
			}
		}
		}
		InputStream instr = null;
        //Log.d(TAG,"iconName------>"+iconName);
		if (iconName != null) {
			try {
				if(themePath.contains(".jar")) {
					if (mResources != null)
						instr = mResources.getAssets().open(
								"theme/icon/" + iconName + ".png");
				}else {
					instr =getIconByZip(themePath, iconName+".png",applicationContext);
				}
			} catch (IOException e) {
				Log.d(TAG,"FileNotFound: theme/icon/" + iconName + ".png");
			}
		}
		return instr;

	}
	public static void saveThemePath(Context context, String str){
        SharedPreferences sp = context.getSharedPreferences("database",
            Context.MODE_PRIVATE);
        sp.edit().putString("database_path", str).commit();
	}
	
	public static String getThemePath(Context context){
		SharedPreferences sp = context.getSharedPreferences(
				"database", Context.MODE_PRIVATE);
		String lastThemePath = sp.getString("database_path", "");
		return lastThemePath;
	}

    private static Resources getResourse(Context context, String themePath) {
        Resources s = null;
        try {
            AssetManager asm = AssetManager.class.newInstance();
            AssetManager.class.getMethod("addAssetPath", String.class).invoke(
                    asm, themePath);
            Resources res = context.getResources();
            s = new Resources(asm, res.getDisplayMetrics(),
                    res.getConfiguration());
            SharedPreferences sp = context.getSharedPreferences("CalendarIcon",
                    Context.MODE_PRIVATE);
            sp.edit().putString("last", themePath).commit();
        } catch (IllegalAccessException e) {
            Log.d(TAG,"cur themePath IllegalAccessException--->"+themePath);
        } catch (IllegalArgumentException e) {
            Log.d(TAG,"cur themePath IllegalArgumentException--->"+themePath);
        } catch (InvocationTargetException e) {
            Log.d(TAG,"cur themePath InvocationTargetException--->"+themePath);
        } catch (NoSuchMethodException e) {
            Log.d(TAG,"cur themePath NoSuchMethodException--->"+themePath);
        } catch (InstantiationException e) {
            Log.d(TAG,"cur themePath InstantiationException--->"+themePath);
        }
        return s;
    }
}
