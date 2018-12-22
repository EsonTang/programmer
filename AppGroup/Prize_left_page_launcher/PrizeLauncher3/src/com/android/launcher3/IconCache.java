/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.launcher3;

import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.Log;

import com.android.gallery3d.util.LogUtils;
import com.android.launcher3.lq.FindDefaultResoures;
import com.android.prize.simple.model.IConstant;
import com.android.prize.simple.utils.SimplePrefUtils;
import com.lqsoft.LqServiceUpdater.LqService;
import com.lqsoft.lqtheme.LqShredPreferences;
import com.lqsoft.lqtheme.LqThemeParser;
import com.prize.left.page.model.DeskModel;

/**
 * Cache of application icons.  Icons can be made from any thread.
 */
public class IconCache {
    @SuppressWarnings("unused")
    private static final String TAG = "Launcher.IconCache";

    private static final int INITIAL_ICON_CACHE_CAPACITY = 50;

    private static class CacheEntry {
        public Bitmap icon;
        public String title;
    }

    private final Bitmap mDefaultIcon;
    private  Context mContext;
    private final PackageManager mPackageManager;
    
//    private Launcher mLauncher;
    public void setupLauncher(Context c) {
    	if(mContext!=null) {
    		mContext=null;
    	}
		this.mContext = LauncherApplication.getInstance().getApplicationContext();
	}

	private final  static HashMap<ComponentName, CacheEntry> mCache =
            new HashMap<ComponentName, CacheEntry>(INITIAL_ICON_CACHE_CAPACITY);
    public HashMap<ComponentName, CacheEntry> getmCache() {
		return mCache;
	}

	private int mIconDpi;

    public IconCache(Context context) {
        ActivityManager activityManager =
                (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

        mContext = context;
        mPackageManager = context.getPackageManager();
        mIconDpi = 480;//activityManager.getLauncherLargeIconDensity();

        // need to set mIconDpi before getting default icon
        mDefaultIcon = makeDefaultIcon();
    }
    
    private static IconCache mIconCache; 
    public static IconCache getInstace(Context c) {
    	if(mIconCache ==null) {
    		mIconCache = new IconCache(c);
    	}
    	return mIconCache;
    }

    public Drawable getFullResDefaultActivityIcon() {
        return getFullResIcon(Resources.getSystem(),
                android.R.mipmap.sym_def_app_icon);
    }

    public Drawable getFullResIcon(Resources resources, int iconId) {
        Drawable d;
        try {
            d = resources.getDrawableForDensity(iconId, mIconDpi);
        } catch (Resources.NotFoundException e) {
            d = null;
        }

        return (d != null) ? d : getFullResDefaultActivityIcon();
    }

    public Drawable getFullResIcon(String packageName, int iconId) {
        Resources resources;
        try {
            resources = mPackageManager.getResourcesForApplication(packageName);
        } catch (PackageManager.NameNotFoundException e) {
            resources = null;
        }
        if (resources != null) {
            if (iconId != 0) {
                return getFullResIcon(resources, iconId);
            }
        }
        return getFullResDefaultActivityIcon();
    }

    public Drawable getFullResIcon(ResolveInfo info) {
        return getFullResIcon(info.activityInfo);
    }

    public Drawable getFullResIcon(ActivityInfo info) {

        Resources resources;
        try {
            resources = mPackageManager.getResourcesForApplication(
                    info.applicationInfo);
        } catch (PackageManager.NameNotFoundException e) {
            resources = null;
        }
        if (resources != null) {
            int iconId = info.getIconResource();
            if (iconId != 0) {
                return getFullResIcon(resources, iconId);
            }
        }
        return getFullResDefaultActivityIcon();
    }

    private Bitmap makeDefaultIcon() {
        Drawable d = getFullResDefaultActivityIcon();
        Bitmap b = Bitmap.createBitmap(Math.max(d.getIntrinsicWidth(), 1),
                Math.max(d.getIntrinsicHeight(), 1),
                Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        d.setBounds(0, 0, b.getWidth(), b.getHeight());
        d.draw(c);
        c.setBitmap(null);
        return b;
    }

    /**
     * Remove any records for the supplied ComponentName.
     */
    public void remove(ComponentName componentName) {
        synchronized (mCache) {
            mCache.remove(componentName);
        }
    }

    /**
     * Empty out the cache.
     */
    public void flush() {
       synchronized (mCache) {
                Iterator<Entry<ComponentName, CacheEntry>> it = mCache.entrySet().iterator();
                while (it.hasNext()) {
                    final CacheEntry e = it.next().getValue();
                    if(e != null&&e.icon !=null && !e.icon.isRecycled()) {
//                        e.icon.recycle();
                    	e.icon=null;
                    }
                }
            }
        System.gc();
    	int size = (int) Runtime.getRuntime().totalMemory();
        mCache.clear();
        System.gc();
    	int newSize = (int) Runtime.getRuntime().totalMemory();
    	LogUtils.i("zhouerlong", "gc how :"+(newSize-size));
    }

    /**
     * Empty out the cache that aren't of the correct grid size
     */
    public void flushInvalidIcons(DeviceProfile grid) {
        synchronized (mCache) {
            Iterator<Entry<ComponentName, CacheEntry>> it = mCache.entrySet().iterator();
            while (it.hasNext()) {
                final CacheEntry e = it.next().getValue();
                if (e == null || e.icon == null) {
                	it.remove();
                	continue;
                }
                if (e.icon.getWidth() != grid.iconSizePx || e.icon.getHeight() != grid.iconSizePx) {
                    it.remove();
                }
            }
        }
    }

    /**
     * Fill in "application" with the icon and label for "info."
     */
    public void getTitleAndIcon(AppInfo application, ResolveInfo info,
            HashMap<Object, CharSequence> labelCache) {
        synchronized (mCache) {
            CacheEntry entry = cacheLocked(application.componentName, info, labelCache);

            application.title = entry.title;
            application.iconBitmap = entry.icon;
        }
    }

    public Bitmap getIcon(Intent intent) {
        synchronized (mCache) {
        	if(intent ==null) {
        		return mDefaultIcon;
        	}
            final ResolveInfo resolveInfo = mPackageManager.resolveActivity(intent, 0);
            ComponentName component = intent.getComponent();

            if (resolveInfo == null || component == null) {
                return mDefaultIcon;
            }
        	CacheEntry entry=null;
        	if(Launcher.isSupportClone&&intent.getAppInstanceIndex()==1&&LqShredPreferences.getLqThemePath().toString().contains(FindDefaultResoures.DEFALUT_THEME_PATH)) {
    			ComponentName comName = new ComponentName(intent.getComponent().getPackageName(),intent.getComponent().getClassName()+"s");

    			entry = cacheLocked(comName, resolveInfo, null);
    		}else {

    			entry = cacheLocked(component, resolveInfo, null);
    		}
            return entry.icon;
        }
    }

    public Bitmap getIcon(ComponentName component, ResolveInfo resolveInfo,
            HashMap<Object, CharSequence> labelCache) {
        synchronized (mCache) {
            if (resolveInfo == null || component == null) {
                return null;
            }

            CacheEntry entry = cacheLocked(component, resolveInfo, labelCache);

            System.gc();
            return entry.icon;
        }
    }

    public boolean isDefaultIcon(Bitmap icon) {
        return mDefaultIcon == icon;
    }

    private CacheEntry cacheLocked(ComponentName componentName, ResolveInfo info,
            HashMap<Object, CharSequence> labelCache) {
        CacheEntry entry = mCache.get(componentName);
        if (entry == null) {
            entry = new CacheEntry();
            if(!mCache.containsKey(componentName)) {
                mCache.put(componentName, entry);
            }

            ComponentName key = LauncherModel.getComponentNameFromResolveInfo(info);
            if (labelCache != null && labelCache.containsKey(key)) {
                entry.title = labelCache.get(key).toString();
            } else {
                entry.title = info.loadLabel(mPackageManager).toString();
                if (labelCache != null) {
                    labelCache.put(key, entry.title);
                }
            }
            if (entry.title == null) {
                entry.title = info.activityInfo.name;
            }

            
            if(LqShredPreferences.isLqtheme(mContext)){
            	Bitmap i=Utilities.createIconBitmap(
                        getFullResIcon(info), mContext);
          		 entry.icon = getThemeIcon(componentName, i, true, "",mContext);
                if(componentName.getClassName().equals("com.android.calendar.AllInOneActivity")) {
                	Bitmap calendar = getCalendarIcon();
                	if(calendar !=null) {
                  		 entry.icon = calendar;
                	}
                }	
                try {
					if (LauncherAppState.getInstance().getModel()
							.getCallBacks().get() instanceof Launcher) {
						Launcher l = (Launcher) LauncherAppState.getInstance()
								.getModel().getCallBacks().get();
						if (componentName.getClassName().equals(
								"com.tianqiyubao2345.activity.CoveryActivity") || componentName.getClassName().equals(
										"com.prize.weather.WeatherHomeActivity")) {
							l.dochangeWeatherIcon(componentName);
						}

					}
				} catch (Exception e) {
				}
                
              /*  if(componentName.getClassName().equals("com.tencent.mobileqq.activity.SplashActivity")) {

                	Bitmap calendar = getQQIcon(mContext, LqShredPreferences.getLqThemePath().toString(), "qq");
                	if(calendar !=null) {
                  		 entry.icon = calendar;
                	}
                }*/
            }
            if(entry.icon == null){
            	entry.icon = Utilities.createIconBitmap(
                        getFullResIcon(info), mContext);
            }
        }
        
        
        return entry;
    }
    
    public static  Bitmap getThemeIcon(ComponentName componentName,Bitmap icon,boolean isRecycled,String disp ,Context context) {

    	Bitmap bitmap=null;
    	if(context==null) {
    		return null;
    	}
    	if(!LqThemeParser.isInserticon(context, LqShredPreferences.getLqThemePath().toString(), componentName)) {

        	 bitmap = IconCache.getLqIcon(componentName, icon, true, "");
    		if(bitmap !=null) {
        		bitmap=ImageUtils.resizeIcon(bitmap, Utilities.sIconTextureHeight, Utilities.sIconTextureWidth);
    		}
    	}else {
       	 bitmap = LqThemeParser.getThemeIcon(context, LqShredPreferences.getLqThemePath().toString(), componentName);
    	}

        System.gc();
    	
    	return bitmap;
    }
    
    public static Bitmap getLqIcon(ComponentName componentName,Bitmap icon,boolean isRecycled,String disp ) {
    	 Bitmap b=null;
    	try {
    		b=LqService.getInstance().getIcon(componentName, icon, isRecycled, disp);
		} catch (Exception e) {
			e.printStackTrace();
		}
    	 if(b!=null&&componentName!=null) {
//    		 Log.i("zhouerlong", "你好  判断Lq 的getIcon 是否 已经 回收了图标:::b.isRecycled()"+b.isRecycled()+"包名：类名:"+componentName.toString());
    		 if(b.isRecycled()) {
    			 return null;
    		 }
    	 }
    	 
    	return b;
    }
    
    
    public static Bitmap getFolderIcon(String disp ) {
   	 Bitmap b=null;
   	try {
   		b=LqService.getInstance().getFolderIcon( disp);
		} catch (Exception e) {
			e.printStackTrace();
		}
   	 
   	return b;
   }
    
    
    
    

	//add by zhouerlong 0728 begin
    /**
     * 
     * 日历图片的处理
     * @return
     */
	public Bitmap getCalendarIcon() {
		

		DeskModel.getInstance(mContext).doGet();

		Calendar c = Calendar.getInstance(); // 秒
		int date = c.get(Calendar.DAY_OF_MONTH);
		int decade = date / 10;
		int unit = date % 10;
		Bitmap decadeIcon = LqThemeParser.getCalendarIcon(LauncherApplication.getInstance().getApplicationContext(), LqShredPreferences.getLqThemePath(), String.valueOf(decade));
		Bitmap unitIcon = LqThemeParser.getCalendarIcon(mContext, LqShredPreferences.getLqThemePath(), String.valueOf(unit));
		Bitmap calendarBg = LqThemeParser.getCalendarIcon(mContext, LqShredPreferences.getLqThemePath(), "calendar");
		Bitmap weekdayBg = LqThemeParser.getCalendarIcon(mContext, LqShredPreferences.getLqThemePath(), "weekday");

		int count=1;
		if(decade>0) {
			count=2;
		}
		if(decadeIcon!=null&&unitIcon!=null&&calendarBg!=null) {
			    Bitmap dest = ImageUtils
					.doodlesrc(decadeIcon, unitIcon, calendarBg,weekdayBg,mContext,count);
			 dest =ImageUtils.resizeIcon(dest, Utilities.sIconTextureHeight, Utilities.sIconTextureWidth);
			return dest;
			
		}
		return null;

	}
	
	
	public void respWeatherInfo(Bitmap dest,Launcher l) {
		if(mCache != null
				&& mCache.containsKey(l.mWeatherComponentName)) {
			CacheEntry entry = mCache.get(l.mWeatherComponentName);
			entry.icon = dest;
		}
		if(mCache != null
				&& mCache.containsKey(l.prizeWeatherComponentName)) {
			CacheEntry entry = mCache.get(l.prizeWeatherComponentName);
			entry.icon = dest;
		}
	}
	
	public Bitmap getWeatherIcon(Launcher l,int date,String iconName,String bgs) {
		int decade = date / 10;
		int unit = date % 10;
		Bitmap decadeIcon = LqThemeParser.getWeatherIcon(l, LqShredPreferences.getLqThemePath(), String.valueOf(decade),l.mWeather_path+"num/");
		Bitmap unitIcon = LqThemeParser.getWeatherIcon(l, LqShredPreferences.getLqThemePath(), String.valueOf(unit),l.mWeather_path+"num/");
		Bitmap icon = LqThemeParser.getWeatherIcon(l, LqShredPreferences.getLqThemePath(), iconName,l.mWeather_path);
		Bitmap degress = LqThemeParser.getWeatherIcon(l, LqShredPreferences.getLqThemePath(), bgs,l.mWeather_path+"num/");
		if (!(decadeIcon != null && unitIcon != null && icon != null&&degress!=null)) {
			return null;
		}
		Bitmap dest = ImageUtils
				.doodleWeather(decadeIcon, unitIcon, icon,degress);
		dest =ImageUtils.resizeIcon(dest, Utilities.sIconTextureHeight, Utilities.sIconTextureWidth);
		dest = ImageUtils.reDoodle(dest);
		return dest;
		
	}
	
	public void getWeatherIcon(Context c) {
		if (c instanceof Launcher) {
			Launcher l = (Launcher) c;
			l.reqWeatherInfo();
		}
	}
	
	
	public void reqWeatherInfo() {
		Intent it = new Intent();
		it.setAction(Launcher.REQ_WEATHER_BRD);
		String post = SimplePrefUtils.getString(mContext, IConstant.KEY_POSTAL);
		if (!TextUtils.isEmpty(post))
			post = "none";
		it.putExtra("postCode", post);// postCode为null

		mContext.sendBroadcast(it);
		it = null;
	}
	
	public static  Bitmap getQQIcon(Context applicationContext, String themePath,
			String iconName) {
		
		InputStream instr = null;
        Bitmap rettemp = null;
        try {
			instr = applicationContext.getAssets().open("theme/icon/"+iconName+".png");
			if (instr != null) {
				rettemp = BitmapFactory.decodeStream(instr);
//				ImageUtils.savePNG_After(rettemp, "bitmap.png");
				}
		} catch (IOException e) {
			e.printStackTrace();
		}
        return rettemp;
	}

    public HashMap<ComponentName,Bitmap> getAllIcons() {
        synchronized (mCache) {
            HashMap<ComponentName,Bitmap> set = new HashMap<ComponentName,Bitmap>();
            for (ComponentName cn : mCache.keySet()) {
                final CacheEntry e = mCache.get(cn);
                set.put(cn, e.icon);
            }
            return set;
        }
    }
	 
 // Added by lqsoft STSRT   *******************************************************
    public Bitmap getCustomIcon(ShortcutInfo item){
    	Bitmap icon= LauncherAppState.getInstance().getModel().getShortcutCustomIcon(item);
    	if(icon==null||icon.isRecycled()){
    		if(item.mIcon!=null){
    			icon=item.mIcon;
    		}else {
    			icon=mDefaultIcon;
			}
    	}

        //BEGIN change by fuyouhong for icon filter theme

        Bitmap b = null;
		if (LqShredPreferences.isLqtheme(mContext)) {
	         b = IconCache.getLqIcon(null, icon, false, "");
		}
        return b;
    }
    
    public Bitmap getResourcesIcon(String packageName,String resourceName){
        Bitmap bitmap = null;
        try {
            Resources resources = mPackageManager.getResourcesForApplication(packageName);
            if (resources != null) {
                final int id = resources.getIdentifier(resourceName, null, null);
                if (id > 0) {
                    bitmap = Utilities.createIconBitmap(getFullResIcon(resources, id), mContext);
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }
    
    public Bitmap getResourcesIcon(String packageName, String resourceName,boolean  processed) {
		Bitmap bp=getResourcesIcon(packageName, resourceName);
		if(processed){
			   Bitmap icon =null;
			if (LqShredPreferences.isLqtheme(mContext)) {
	             icon = IconCache.getLqIcon(null, bp, false, "");
			}
            return icon;
		}
		return bp;
	}
// Added by lqsoft  END    *******************************************************
    
}
