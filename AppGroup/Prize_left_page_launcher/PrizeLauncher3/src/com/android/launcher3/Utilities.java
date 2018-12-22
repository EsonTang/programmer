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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.app.admin.DeviceAdminInfo;
import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PaintDrawable;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Environment;
import android.os.UserHandle;
import android.provider.Settings;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.launcher3.config.WallpaperSetService;
import com.android.launcher3.lq.DefaultConfig;
import com.android.launcher3.lq.FindDefaultResoures;
import com.android.launcher3.view.PrizeScrollLayout;
import com.lqsoft.lqtheme.LqShredPreferences;
import com.lqsoft.lqtheme.LqThemeParser;
import com.mediatek.launcher3.ext.LauncherLog;
//xieweiwei_20150205_add_begin
//xieweiwei_20150205_add_end

/**
 * Various utilities shared amongst the Launcher's classes.
 */
public final class Utilities {
    private static final String TAG = "Launcher.Utilities";

    public static int sIconWidth = -1;//add by zhouerlong
    private static int sIconHeight = -1;
    public static int sIconTextureWidth = -1;
    public static int sIconTextureHeight = -1;
    public static int duration = 300;
    public static int revert_duration = 400;

    private static final Paint sBlurPaint = new Paint();
    private static final Paint sGlowColorPressedPaint = new Paint();
    private static final Paint sGlowColorFocusedPaint = new Paint();
    private static final Paint sDisabledPaint = new Paint();
    private static final Rect sOldBounds = new Rect();
    private static final Canvas sCanvas = new Canvas();
    private static final float sRatio=1.0f;

    static {
        sCanvas.setDrawFilter(new PaintFlagsDrawFilter(Paint.DITHER_FLAG,
                Paint.FILTER_BITMAP_FLAG));
    }
    
    public static int getRandom(int start, int end){
    	Random rdm = new Random();
    	return rdm.nextInt(end-start+1) + start;
    }    
    
    static int sColors[] = { 0xffff0000, 0xff00ff00, 0xff0000ff };
    static int sColorIndex = 0;

    /**
     * Returns a FastBitmapDrawable with the icon, accurately sized.
     */
    static public Drawable createIconDrawable(Bitmap icon) {//m by zhouerlong
        FastBitmapDrawable d = new FastBitmapDrawable(icon);
        d.setFilterBitmap(true);
        resizeIconDrawable(d);
        return d;
    }
    
    public static boolean checkApkExist(Context context, String packageName) { 
        boolean hasInstalled = false;  
        try {
            PackageManager pm = context.getPackageManager();  
            List<PackageInfo> list = pm  
                    .getInstalledPackages(PackageManager.PERMISSION_GRANTED);  
            for (PackageInfo p : list) {  
                if (packageName != null && packageName.equals(p.packageName)) {  
                    hasInstalled = true;  
                    break;  
                }  
            }  
		} catch (Exception e) {
			// TODO: handle exception
		}
        return hasInstalled;  
        }
	static public int getDuration() {

		int d = 600;
		String duration = LauncherAppState.getInstance()
				.getLauncehrApplication().getDefault_config()
				.get(DefaultConfig.start_activity_duration);
		if (duration != null) {
			try {
				d = Integer.valueOf(duration);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return d;
	}
	
	
	static public void  generatedefaultXml(Workspace w) {
		
		

		File packageFile = new File(getEnvironmentPathApps() + File.separator
				+ "default_workspace_koobee.xml");
		String defaultBegin = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\r\n\r\n   <favorites xmlns:launcher=\"http://schemas.android.com/apk/res-auto/com.android.launcher3\" >\r\n\r\n";
		StringBuffer defaultContent = new StringBuffer();
		defaultContent.append(defaultBegin);
		List<ShortcutAndWidgetContainer > shs = w.getAllShortcutAndWidgetContainersTest();
		for(int p=0;p<shs.size(); p++) { 
			ShortcutAndWidgetContainer s = shs.get(p);
			if(s.getParent().getParent() instanceof Hotseat) {
				String hotset ="<!-- Hotseat ************************************************ 的排版-->\r\n\r\n"; 
				defaultContent.append(hotset);
				
				for(int i=0;i<s.getChildCount();i++) {
					View view = s.getChildAt(i);
					ShortcutInfo tag = (ShortcutInfo) view.getTag();
					String commant = "\r\n\t<!-- "+tag.title+"-->\r\n";
					String favorite = "\t<favorite\r\n";
					String cls = "\t\tlauncher:className=\""+tag.getIntent().getComponent().getClassName()+"\"\r\n";
					String pkg = "\t\tlauncher:packageName=\""+tag.getIntent().getComponent().getPackageName()+"\"\r\n";
					String container = "\t\tlauncher:container=\""+tag.container+"\"\r\n";
					String screen = "\t\tlauncher:screen=\""+tag.screenId+"\"\r\n";
					String x = "\t\tlauncher:x=\""+tag.cellX+"\"\r\n";
					String y = "\t\tlauncher:y=\""+tag.cellY+"\"";
					String fend= "\t/>\r\n";
					defaultContent.append(commant);
					defaultContent.append(favorite);
					defaultContent.append(cls);
					defaultContent.append(pkg);
					defaultContent.append(container);
					defaultContent.append(screen);
					defaultContent.append(x);
					defaultContent.append(y);	
					defaultContent.append(fend);
				}
				

				String hotsetend ="<!--************************************************ Hotseat  的排版 结束-->\r\n\r\n"; 
				defaultContent.append(hotsetend);
			}else {
				String pageBegin ="<!-- ************************************************workspace  第"+p+"页的排版 开始-->\r\n\r\n"; 
				defaultContent.append(pageBegin);
			
			for(int i=0;i<s.getChildCount();i++) {
				View child = s.getChildAt(i);
				ItemInfo info = (ItemInfo) child.getTag();
				
				if(info instanceof  ShortcutInfo) {
					try {
						ShortcutInfo tag = (ShortcutInfo) info;
						String commant = "\r\n\t<!-- "+tag.title+"-->\r\n";
						String favorite = "\t<favorite\r\n";
						String cls = "\t\tlauncher:className=\""+tag.getIntent().getComponent().getClassName()+"\"\r\n";
						String pkg = "\t\tlauncher:packageName=\""+tag.getIntent().getComponent().getPackageName()+"\"\r\n";
						String container = "\t\tlauncher:container=\""+tag.container+"\"\r\n";
						String screen = "\t\tlauncher:screen=\""+tag.screenId+"\"\r\n";
						String x = "\t\tlauncher:x=\""+tag.cellX+"\"\r\n";
						String y = "\t\tlauncher:y=\""+tag.cellY+"\"";
						String fend= "\t/>\r\n";
						defaultContent.append(commant);
						defaultContent.append(favorite);
						defaultContent.append(cls);
						defaultContent.append(pkg);
//						defaultContent.append(container);
						defaultContent.append(screen);
						defaultContent.append(x);
						defaultContent.append(y);	
						defaultContent.append(fend);
					} catch (Exception e) {
						// TODO: handle exception
					}	
				}
				/* <appwidget
				 launcher:className="com.tianqiyubao2345.widget.WeatherWidget2"
				 launcher:packageName="com.tianqiwhite"
				         launcher:screen="0"
				         launcher:spanX="4"
				         launcher:spanY="1"
				         launcher:x="0"
				         launcher:y="0" />*/
				
				if(info instanceof  LauncherAppWidgetInfo) {
					try {
						LauncherAppWidgetInfo tag = (LauncherAppWidgetInfo) info;
						String commant = "\r\n\r\n\t<!-- 小部件-->\r\n";
						String appwidget = "\t<appwidget\r\n";
						String cls = "\t\tlauncher:className=\""+tag.providerName.getClassName()+"\"\r\n";
						String pkg = "\t\tlauncher:packageName=\""+tag.providerName.getPackageName()+"\"\r\n";
						String screen = "\t\tlauncher:screen=\""+tag.screenId+"\"\r\n";
						String spanX = "\t\tlauncher:spanX=\""+tag.spanX+"\"\r\n";
						String spanY = "\t\tlauncher:spanY=\""+tag.spanY+"\"\r\n";
						String x = "\t\tlauncher:x=\""+tag.cellX+"\"\r\n";
						String y = "\t\tlauncher:y=\""+tag.cellY+"\"\r\n";
						String fend= "\t/>\r\n";
						defaultContent.append(commant);
						defaultContent.append(appwidget);
						defaultContent.append(cls);
						defaultContent.append(pkg);
//						defaultContent.append(container);
						defaultContent.append(screen);
						defaultContent.append(x);
						defaultContent.append(y);	
						defaultContent.append(spanX);
						defaultContent.append(spanY);	
						defaultContent.append(fend);
					} catch (Exception e) {
						// TODO: handle exception
					}	
				}
				
				
				/* <!-- 文件夹 -->
				    <folder
				        launcher:screen="0"
						launcher:name="工具箱;工具箱;Tools"
				        launcher:x="3"
				        launcher:y="2" >*/
				if(info instanceof FolderInfo) {

					FolderInfo f = (FolderInfo) info;
					try {
						
						String commant = "\r\n\t<!-- "+f.title+"-->\r\n";
						String folder = "\t<folder\r\n";
						String screen = "\t\tlauncher:screen=\""+f.screenId+"\"\r\n";
						String name = "\t\tlauncher:name=\""+f.title+";"+f.title+";"+f.title+"\"\r\n";
						String x = "\t\tlauncher:x=\""+f.cellX+"\"\r\n";
						String y = "\t\tlauncher:y=\""+f.cellY+"\"";
						String fend= "\t>\r\n";
						

						defaultContent.append(commant);
						defaultContent.append(folder);
						defaultContent.append(screen);
						defaultContent.append(name);
						defaultContent.append(x);
						defaultContent.append(y);
						defaultContent.append(fend);
					} catch (Exception e) {
						// TODO: handle exception
					}

					List<ShortcutInfo> contents = f.getContents();
					for(ShortcutInfo fs:contents) {
						
						try {
							ShortcutInfo tag = (ShortcutInfo) fs;
							String commants = "\t\r\n\t<!-- "+tag.title+"-->\r\n";
							String favorites = "\t\t<favorite\r\n";
							String clss = "\t\t\tlauncher:className=\""+tag.getIntent().getComponent().getClassName()+"\"\r\n";
							String pkgs = "\t\t\tlauncher:packageName=\""+tag.getIntent().getComponent().getPackageName()+"\"\r\n";
//							String containers = "launcher:container=\""+tag.container+"\"\r\n";
							String screens = "\t\t\tlauncher:screen=\""+tag.screenId+"\"\r\n";
							String xs = "\t\t\tlauncher:x=\""+tag.cellX+"\"\r\n";
							String ys = "\t\t\tlauncher:y=\""+tag.cellY+"\"";
							String fends= "\t/>\r\n";
							defaultContent.append(commants);
							defaultContent.append(favorites);
							defaultContent.append(clss);
							defaultContent.append(pkgs);
//							defaultContent.append(containers);
							defaultContent.append(screens);
							defaultContent.append(xs);
							defaultContent.append(ys);	
							defaultContent.append(fends);	
						} catch (Exception e) {
							// TODO: handle exception
						}
					}
					
					String folderEnd = "\t</folder>\r\n";
					defaultContent.append(folderEnd);
				}
				
				
				

			}
			String pageEnd ="<!-- workspace  ************************************************第"+p+"页的排版 结束-->\r\n\r\n"; 
			defaultContent.append(pageEnd);

			}
			
			
		}
		
		String OkEnd = "</favorites>";
		defaultContent.append(OkEnd);

		FileUtils.dump(defaultContent.toString(), packageFile);
		Toast.makeText(w.getContext(), "生成文件OK", Toast.LENGTH_LONG).show();
		
		
		
	}
	
	
	public static String getEnvironmentPathApps() {
		File f = Environment.getExternalStorageDirectory();

		File ficon = new File(f.getPath() + File.separator + "apps");
		if (!ficon.exists()) {
			ficon.mkdir();
		}
		File app = new File(ficon + "/app");

		if (!app.exists()) {
			app.mkdir();
		}
		return app.getPath();
	}
	

	static public int getRevertDuration() {

		int d = 600;
		String duration = LauncherAppState.getInstance()
				.getLauncehrApplication().getDefault_config()
				.get(DefaultConfig.revert_duration);
		if (duration != null) {
			try {
				d = Integer.valueOf(duration);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return d;
	}
	
	
	static public int getOpenFolderDuration() {

		int d = 600;
		String duration = LauncherAppState.getInstance()
				.getLauncehrApplication().getDefault_config()
				.get(DefaultConfig.open_folder_duration);
		if (duration != null) {
			try {
				d = Integer.valueOf(duration);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return d;
	}
	
	static public int getDragDuration() {

		int d = 600;
		String duration = LauncherAppState.getInstance()
				.getLauncehrApplication().getDefault_config()
				.get(DefaultConfig.drag_duration);
		if (duration != null) {
			try {
				d = Integer.valueOf(duration);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return d;
	}
	
	static public int getDropDuration() {

		int d = 600;
		String duration = LauncherAppState.getInstance()
				.getLauncehrApplication().getDefault_config()
				.get(DefaultConfig.drop_duration);
		if (duration != null) {
			try {
				d = Integer.valueOf(duration);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return d;
	}
	

	
	static public int getenterEditDuration() {

		int d = 600;
		String duration = LauncherAppState.getInstance()
				.getLauncehrApplication().getDefault_config()
				.get(DefaultConfig.enter_edit_duration);
		if (duration != null) {
			try {
				d = Integer.valueOf(duration);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return d;
	}
	
	static public int getShowListEditDuration() {

		int d = 600;
		String duration = LauncherAppState.getInstance()
				.getLauncehrApplication().getDefault_config()
				.get(DefaultConfig.show_edit_list_duration);
		if (duration != null) {
			try {
				d = Integer.valueOf(duration);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return d;
	}
    
    
	static public int getUnlockDuration() {

		int d = 400;
		String duration = LauncherAppState.getInstance()
				.getLauncehrApplication().getDefault_config()
				.get(DefaultConfig.unlock_duration);
		if (duration != null) {
			try {
				d = Integer.valueOf(duration);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return d;
	}
	
	static public int getUninstallDuration() {

		int d = 400;
		String duration = LauncherAppState.getInstance()
				.getLauncehrApplication().getDefault_config()
				.get(DefaultConfig.uninstall_duration);
		if (duration != null) {
			try {
				d = Integer.valueOf(duration);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return d;
	}
	
	public static int random(int max) {
		Random random = new Random();
		int randomInt = random.nextInt(max);
		return randomInt;
	}

	static public int getDefaultEffct() {
		String effct = LauncherAppState.getInstance().getLauncehrApplication()
				.getDefault_config().get(DefaultConfig.default_effect_id);
		int d = 0;
		try {
			d = (effct != null) ? Integer.valueOf(effct) : 0;
		} catch (Exception e) {
		}
		return d;
	}
	
	static public String getdefaultThemeId() {
		String id = LauncherAppState.getInstance().getLauncehrApplication()
				.getDefault_config().get(DefaultConfig.default_bulit_in_theme_name);
		return id;
	}
	
	static public String getChannel() {
		String channel = LauncherAppState.getInstance().getLauncehrApplication()
				.getDefault_config().get(DefaultConfig.channel);
		return channel;
	}
	
	static public List<String> getFilterApps() {

		String apps = LauncherAppState.getInstance().getLauncehrApplication()
				.getDefault_config().get(DefaultConfig.filter_apps);
		List<String> mFilterApps = apps != null ? Arrays
				.asList(apps.split(";")) : null;
		return mFilterApps;
	}
	
	
	static public List<String> getFilterWidgets() {

		String apps = LauncherAppState.getInstance().getLauncehrApplication()
				.getDefault_config().get(DefaultConfig.filter_widgets);
		List<String> mFilterApps = apps != null ? Arrays
				.asList(apps.split(";")) : null;
		return mFilterApps;
	}

	static public int getDefaultHomepage() {
		int d = 0;
		String defaultPage = LauncherAppState.getInstance()
				.getLauncehrApplication().getDefault_config()
				.get(DefaultConfig.default_home_page);
		try {
			d = defaultPage != null ? Integer.valueOf(defaultPage) : 0;
		} catch (Exception e) {
		}
		return d;
	}
	
	static public int getDeskTick() {
		int d = -1;
		String defaultPage = LauncherAppState.getInstance()
				.getLauncehrApplication().getDefault_config()
				.get(DefaultConfig.desk_tick);
		try {
			d = defaultPage != null ? Integer.valueOf(defaultPage) : -1;
		} catch (Exception e) {
		}
		return d;
	}

	static public int getDefaultIconSize(int defaultIcon) {
    	int icon_size=defaultIcon;
        String icon_pix =LauncherAppState.getInstance().getLauncehrApplication().getDefault_config().get(DefaultConfig.app_icon_size);
         icon_size =icon_pix!=null?Integer.valueOf(icon_pix):defaultIcon;
         return icon_size;
    }

	static String getDefaultpageCount() {
		return LauncherAppState.getInstance().getLauncehrApplication()
				.getDefault_config().get(DefaultConfig.default_page_count);
	}

	static int getInstallPosPage() {
		String install_pos_page = LauncherAppState.getInstance()
				.getLauncehrApplication().getDefault_config()
				.get(DefaultConfig.install_pos_page);
		int page = 1;
		try {
			page = install_pos_page != null ? Integer.valueOf(install_pos_page)
					: 1;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return page;
	}

	static boolean supportEasyLauncher() {
		String easy = LauncherAppState.getInstance().getLauncehrApplication()
				.getDefault_config().get(DefaultConfig.easy_launcher);
		boolean easy_item = Boolean.valueOf(easy);
		return easy_item;
	}
	
	static boolean defaultXml() {
		String easy = LauncherAppState.getInstance().getLauncehrApplication()
				.getDefault_config().get(DefaultConfig.default_xml);
		boolean easy_item = Boolean.valueOf(easy);
		return easy_item;
	}
	
	static boolean getStartActivityModel() {
		String model = LauncherAppState.getInstance().getLauncehrApplication()
				.getDefault_config().get(DefaultConfig.start_model);
		boolean start = Boolean.valueOf(model);
		return start;
	}

	
	/** 
	    * 计算两个日期型的时间相差多少时间 
	    * @param startDate  开始日期 
	    * @param endDate    结束日期 
	      * @return 
	    */  
	   static public  boolean twoDateDistance(String start,String end,Date curData){  

	    	try {
	    		
	    		  SimpleDateFormat   sf   =   new   SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		    	Date startDate = sf.parse(start);//publishtime为"E MMMM dd hh:mm:ss z yyyy"这种格式，
		    	Date endDate = sf.parse(end);//publishtime为"E MMMM dd hh:mm:ss z yyyy"这种格式，
//		    	new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		        if(startDate == null ||endDate == null){  
		            return false;  
		        }  
		        long low = endDate.getTime() - curData.getTime();  
		        long pow = curData.getTime() - startDate.getTime();  
		        return low>0&&pow>0;
			} catch (Exception e) {
				return false;
			}
	        
	        
	        
	   /*     if (timeLong<60*1000)  
	            return timeLong/1000 + "秒前";  
	        else if (timeLong<60*60*1000){  
	            timeLong = timeLong/1000 /60;  
	            return timeLong + "分钟前";  
	        }  
	        else if (timeLong<60*60*24*1000){  
	            timeLong = timeLong/60/60/1000;  
	            return timeLong+"小时前";  
	        }  
	        else if (timeLong<60*60*24*1000*7){  
	            timeLong = timeLong/1000/ 60 / 60 / 24;  
	            return timeLong + "天前";  
	        }  
	        else if (timeLong<60*60*24*1000*7*4){  
	            timeLong = timeLong/1000/ 60 / 60 / 24/7;  
	            return timeLong + "周前";  
	        }  
	        else {  
	            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");  
	            sdf.setTimeZone(TimeZone.getTimeZone("GMT+08:00"));  
	            return sdf.format(startDate);  
	        }  */
	        
	        
	}  
	

	static boolean supportTestTheme() {
		String test = LauncherAppState.getInstance().getLauncehrApplication()
				.getDefault_config().get(DefaultConfig.test);
		boolean easy_item = Boolean.valueOf(test);
		return easy_item;
	}
	
	static public boolean isNumeric(String str){    
		  
		         Pattern pattern = Pattern.compile("[0-9]*");    
		   
		         Matcher isNum = pattern.matcher(str);   
		   
		         if( !isNum.matches() ){   
		             return false;    
		        }    
		        return true;    
		  
		     }   
	

	static boolean supportleftScreen() {
		String left = LauncherAppState.getInstance().getLauncehrApplication()
				.getDefault_config().get(DefaultConfig.left_screen);
		boolean easy_item = Boolean.valueOf(left);
		return easy_item;
	}
	
	static boolean supportLongClickEnterEdit() {
		String left = LauncherAppState.getInstance().getLauncehrApplication()
				.getDefault_config().get(DefaultConfig.long_click_enter_edit);
		boolean easy_item = Boolean.valueOf(left);
		return easy_item;
	}
	
	
	
	public static boolean supportUnlockAnim() {
		String unlock = LauncherAppState.getInstance().getLauncehrApplication()
				.getDefault_config().get(DefaultConfig.isUnlock);
		boolean isUnlock = Boolean.valueOf(unlock);
		return isUnlock;
	}

	static String getDefaultTextColor() {
		return LauncherAppState.getInstance().getLauncehrApplication()
				.getDefault_config().get(DefaultConfig.text_color);
	}

	static boolean isSupportClone() {
		String isClone = LauncherAppState.getInstance()
				.getLauncehrApplication().getDefault_config()
				.get(DefaultConfig.isSupportClone);
		boolean isSupportClone = isClone != null ? Boolean.valueOf(isClone)
				: true;
		return isSupportClone;
	}

	static boolean isKoobee() {

		String koobee = LauncherAppState.getInstance().getLauncehrApplication()
				.getDefault_config().get(DefaultConfig.is_koobee);
		boolean isKoobee = Boolean.valueOf(koobee);
		return isKoobee;
	}
	
	
	static public boolean isLocalTheme() {
		return LqShredPreferences.getLqThemePath().contains(FindDefaultResoures.DEFALUT_THEME_PATH);
	}
	
	static boolean isSupportUpgrade() {

		String isUpgrade = LauncherAppState.getInstance().getLauncehrApplication()
				.getDefault_config().get(DefaultConfig.isSupportUpgrade);
		boolean isSupportUpgrade = isUpgrade != null ? Boolean.valueOf(isUpgrade)
				: true;
		return isSupportUpgrade;
	}
    /**
     * Resizes an icon drawable to the correct icon size.
     */
    static public void resizeIconDrawable(Drawable icon) {
            icon.setBounds(0, 0, sIconTextureWidth, sIconTextureHeight);
    }

    /**
     * Returns a bitmap suitable for the all apps view. Used to convert pre-ICS
     * icon bitmaps that are stored in the database (which were 74x74 pixels at hdpi size)
     * to the proper size (48dp)
     */
    static public Bitmap createIconBitmap(Bitmap icon, Context context) {//m by zhouerlong
        int textureWidth = sIconTextureWidth;
        int textureHeight = sIconTextureHeight;
        int sourceWidth = icon.getWidth();
        int sourceHeight = icon.getHeight();
        if (sourceWidth > textureWidth && sourceHeight > textureHeight) {
            // Icon is bigger than it should be; clip it (solves the GB->ICS migration case)
            return Bitmap.createBitmap(icon,
                    (sourceWidth - textureWidth) / 2,
                    (sourceHeight - textureHeight) / 2,
                    textureWidth, textureHeight);
        } else if (sourceWidth == textureWidth && sourceHeight == textureHeight) {
            // Icon is the right size, no need to change it
            return icon;
        } else {
            // Icon is too small, render to a larger bitmap
            final Resources resources = context.getResources();
            return createIconBitmap(new BitmapDrawable(resources, icon), context);
        }
    }

    /**
     * Returns a bitmap suitable for the all apps view.
     */
    public static Bitmap createIconBitmap(Drawable icon, Context context) {
        synchronized (sCanvas) { // we share the statics :-(
            if (sIconWidth == -1) {
                initStatics(context);
            }
            int width = sIconWidth;
            int height = sIconHeight;

            if (icon instanceof PaintDrawable) {
                PaintDrawable painter = (PaintDrawable) icon;
                painter.setIntrinsicWidth(width);
                painter.setIntrinsicHeight(height);
            } else if (icon instanceof BitmapDrawable) {
                // Ensure the bitmap has a density.
                BitmapDrawable bitmapDrawable = (BitmapDrawable) icon;
                Bitmap bitmap = bitmapDrawable.getBitmap();
                if (bitmap.getDensity() == Bitmap.DENSITY_NONE) {
                    bitmapDrawable.setTargetDensity(context.getResources().getDisplayMetrics());
                }
            }
            int sourceWidth = icon.getIntrinsicWidth();
            int sourceHeight = icon.getIntrinsicHeight();
            if (sourceWidth > 0 && sourceHeight > 0) {
                // Scale the icon proportionally to the icon dimensions
                final float ratio = (float) sourceWidth / sourceHeight;
                if (sourceWidth > sourceHeight) {
                    height = (int) (width / ratio);
                } else if (sourceHeight > sourceWidth) {
                    width = (int) (height * ratio);
                }
            }

            // no intrinsic size --> use default size
            int textureWidth = sIconTextureWidth;
            int textureHeight = sIconTextureHeight;

            final Bitmap bitmap = Bitmap.createBitmap(textureWidth, textureHeight,
                    Bitmap.Config.ARGB_8888);
            final Canvas canvas = sCanvas;
            canvas.setBitmap(bitmap);

            final int left = (textureWidth-width) / 2;
            final int top = (textureHeight-height) / 2;

            @SuppressWarnings("all") // suppress dead code warning
            final boolean debug = false;
            if (debug) {
                // draw a big box for the icon for debugging
                canvas.drawColor(sColors[sColorIndex]);
                if (++sColorIndex >= sColors.length) sColorIndex = 0;
                Paint debugPaint = new Paint();
                debugPaint.setColor(0xffcccc00);
                canvas.drawRect(left, top, left+width, top+height, debugPaint);
            }

            sOldBounds.set(icon.getBounds());
            icon.setBounds(left, top, left+width, top+height);
            try {

                icon.draw(canvas);
			} catch (Exception e) {
				e.printStackTrace();
			}
            icon.setBounds(sOldBounds);
            canvas.setBitmap(null);

            return bitmap;
        }
    }
    
    
			//A by zel
    /**
     * Returns a bitmap suitable for the all apps view.
     */
    public static Bitmap createIconBitmap(Drawable icon, Context context,int pad) {
        synchronized (sCanvas) { // we share the statics :-(
            if (sIconWidth == -1) {
                initStatics(context);
            }

            int width = sIconWidth-pad;
            int height = sIconHeight-pad;

            if (icon instanceof PaintDrawable) {
                PaintDrawable painter = (PaintDrawable) icon;
                painter.setIntrinsicWidth(width);
                painter.setIntrinsicHeight(height);
            } else if (icon instanceof BitmapDrawable) {
                // Ensure the bitmap has a density.
                BitmapDrawable bitmapDrawable = (BitmapDrawable) icon;
                Bitmap bitmap = bitmapDrawable.getBitmap();
                if (bitmap.getDensity() == Bitmap.DENSITY_NONE) {
                    bitmapDrawable.setTargetDensity(context.getResources().getDisplayMetrics());
                }
            }
            int sourceWidth = icon.getIntrinsicWidth();
            int sourceHeight = icon.getIntrinsicHeight();
            if (sourceWidth > 0 && sourceHeight > 0) {
                // Scale the icon proportionally to the icon dimensions
                final float ratio = (float) sourceWidth / sourceHeight;
                if (sourceWidth > sourceHeight) {
                    height = (int) (width / ratio);
                } else if (sourceHeight > sourceWidth) {
                    width = (int) (height * ratio);
                }
            }

            // no intrinsic size --> use default size
            int textureWidth = sIconTextureWidth;
            int textureHeight = sIconTextureHeight;

            final Bitmap bitmap = Bitmap.createBitmap(textureWidth, textureHeight,
                    Bitmap.Config.ARGB_8888);
            final Canvas canvas = sCanvas;
            canvas.setBitmap(bitmap);

            final int left = (textureWidth-width) / 2;
            final int top = (textureHeight-height) / 2;

            @SuppressWarnings("all") // suppress dead code warning
            final boolean debug = false;
            if (debug) {
                // draw a big box for the icon for debugging
                canvas.drawColor(sColors[sColorIndex]);
                if (++sColorIndex >= sColors.length) sColorIndex = 0;
                Paint debugPaint = new Paint();
                debugPaint.setColor(0xffcccc00);
                canvas.drawRect(left, top, left+width, top+height, debugPaint);
            }

            sOldBounds.set(icon.getBounds());
            icon.setBounds(left, top, left+width, top+height);
            icon.draw(canvas);
            icon.setBounds(sOldBounds);
            canvas.setBitmap(null);

            return bitmap;
        }
    }

			//A by zel
    /**
     * Returns a Bitmap representing the thumbnail of the specified Bitmap.
     *
     * @param bitmap The bitmap to get a thumbnail of.
     * @param context The application's context.
     *
     * @return A thumbnail for the specified bitmap or the bitmap itself if the
     *         thumbnail could not be created.
     */
    static Bitmap resampleIconBitmap(Bitmap bitmap, Context context) {
        synchronized (sCanvas) { // we share the statics :-(
            if (sIconWidth == -1) {
                initStatics(context);
            }

            if (bitmap.getWidth() == sIconWidth && bitmap.getHeight() == sIconHeight) {
                return bitmap;
            } else {
                final Resources resources = context.getResources();
                return createIconBitmap(new BitmapDrawable(resources, bitmap), context);
            }
        }
    }

    /**
     * Given a coordinate relative to the descendant, find the coordinate in a parent view's
     * coordinates.
     *
     * @param descendant The descendant to which the passed coordinate is relative.
     * @param root The root view to make the coordinates relative to.
     * @param coord The coordinate that we want mapped.
     * @param includeRootScroll Whether or not to account for the scroll of the descendant:
     *          sometimes this is relevant as in a child's coordinates within the descendant.
     * @return The factor by which this descendant is scaled relative to this DragLayer. Caution
     *         this scale factor is assumed to be equal in X and Y, and so if at any point this
     *         assumption fails, we will need to return a pair of scale factors.
     */
    public static float getDescendantCoordRelativeToParent(View descendant, View root,
                                                           int[] coord, boolean includeRootScroll) {
        ArrayList<View> ancestorChain = new ArrayList<View>();

        float[] pt = {coord[0], coord[1]};

        View v = descendant;
        while(v != root && v != null) {
            ancestorChain.add(v);
            v = (View) v.getParent();
        }
        ancestorChain.add(root);

        float scale = 1.0f;
        int count = ancestorChain.size();
        for (int i = 0; i < count; i++) {
            View v0 = ancestorChain.get(i);
            // For TextViews, scroll has a meaning which relates to the text position
            // which is very strange... ignore the scroll.
            if (v0 != descendant || includeRootScroll) {
                pt[0] -= v0.getScrollX();
                pt[1] -= v0.getScrollY();
            }
            v0.getMatrix().mapPoints(pt);
			if (v0.getParent() instanceof PrizeScrollLayout||v0 instanceof ViewPager) {

				pt[0] += 0;
			} else {
				pt[0] += v0.getLeft();
			}
            pt[1] += v0.getTop();
            scale *= v0.getScaleX();
        }

        coord[0] = (int) Math.round(pt[0]);
        coord[1] = (int) Math.round(pt[1]);
        return scale;
    }

    /**
     * Inverse of {@link #getDescendantCoordRelativeToSelf(View, int[])}.
     */
    public static float mapCoordInSelfToDescendent(View descendant, View root,
                                                   int[] coord) {
        ArrayList<View> ancestorChain = new ArrayList<View>();

        float[] pt = {coord[0], coord[1]};

        View v = descendant;
        while(v != root) {
            ancestorChain.add(v);
            v = (View) v.getParent();
        }
        ancestorChain.add(root);

        float scale = 1.0f;
        Matrix inverse = new Matrix();
        int count = ancestorChain.size();
        for (int i = count - 1; i >= 0; i--) {
            View ancestor = ancestorChain.get(i);
            View next = i > 0 ? ancestorChain.get(i-1) : null;

            pt[0] += ancestor.getScrollX();
            pt[1] += ancestor.getScrollY();

            if (next != null) {
                pt[0] -= next.getLeft();
                pt[1] -= next.getTop();
                next.getMatrix().invert(inverse);
                inverse.mapPoints(pt);
                scale *= next.getScaleX();
            }
        }

        coord[0] = (int) Math.round(pt[0]);
        coord[1] = (int) Math.round(pt[1]);
        return scale;
    }

    /// M: Change to public for smart book feature.
    public static void initStatics(Context context) {
        final Resources resources = context.getResources();
        final DisplayMetrics metrics = resources.getDisplayMetrics();
        final float density = metrics.density;

        sIconWidth = sIconHeight = (int) resources.getDimension(R.dimen.app_icon_size);
        sIconTextureWidth = sIconTextureHeight = sIconWidth;

        sBlurPaint.setMaskFilter(new BlurMaskFilter(5 * density, BlurMaskFilter.Blur.NORMAL));
        sGlowColorPressedPaint.setColor(0xffffc300);
        sGlowColorFocusedPaint.setColor(0xffff8e00);

        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0.2f);
        sDisabledPaint.setColorFilter(new ColorMatrixColorFilter(cm));
        sDisabledPaint.setAlpha(0x88);
    }

    public static void setIconSize(int widthPx) {
        sIconWidth = sIconHeight = widthPx;
        sIconTextureWidth = sIconTextureHeight = widthPx;
    }

    public static void scaleRect(Rect r, float scale) {
        if (scale != 1.0f) {
            r.left = (int) (r.left * scale + 0.5f);
            r.top = (int) (r.top * scale + 0.5f);
            r.right = (int) (r.right * scale + 0.5f);
            r.bottom = (int) (r.bottom * scale + 0.5f);
        }
    }

    public static void scaleRectAboutCenter(Rect r, float scale) {
        int cx = r.centerX();
        int cy = r.centerY();
        r.offset(-cx, -cy);
        Utilities.scaleRect(r, scale);
        r.offset(cx, cy);
    }

    public static void startActivityForResultSafely(
            Activity activity, Intent intent, int requestCode) {
        try {
            activity.startActivityForResult(intent, requestCode);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(activity, R.string.activity_not_found, Toast.LENGTH_SHORT).show();
        } catch (SecurityException e) {
            Toast.makeText(activity, R.string.activity_not_found, Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Launcher does not have the permission to launch " + intent +
                    ". Make sure to create a MAIN intent-filter for the corresponding activity " +
                    "or use the exported attribute for this activity.", e);
        }
    }

    /**
     * M: Check whether the given component name is enabled.
     *
     * @param context
     * @param cmpName
     * @return true if the component is in default or enable state, and the application is also in default or enable state,
     *         false if in disable or disable user state.
     */
    static boolean isComponentEnabled(final Context context, final ComponentName cmpName) {
        final String pkgName = cmpName.getPackageName();
        final PackageManager pm = context.getPackageManager();
        // Check whether the package has been uninstalled or the component already removed.
        ActivityInfo aInfo = null;
        try {
            aInfo = pm.getActivityInfo(cmpName, 0);
        } catch (NameNotFoundException e) {
            LauncherLog.w(TAG, "isComponentEnabled NameNotFoundException: pkgName = " + pkgName);
        }

        if (aInfo == null) {
            LauncherLog.d(TAG, "isComponentEnabled return false because component " + cmpName + " has been uninstalled!");
            return false;
        }

        final int pkgEnableState = pm.getApplicationEnabledSetting(pkgName);
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "isComponentEnabled: cmpName = " + cmpName + ",pkgEnableState = " + pkgEnableState);
        }
        if (pkgEnableState == PackageManager.COMPONENT_ENABLED_STATE_DEFAULT
                || pkgEnableState == PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {
            final int cmpEnableState = pm.getComponentEnabledSetting(cmpName);
            if (LauncherLog.DEBUG) {
                LauncherLog.d(TAG, "isComponentEnabled: cmpEnableState = " + cmpEnableState);
            }
            if (cmpEnableState == PackageManager.COMPONENT_ENABLED_STATE_DEFAULT
                    || cmpEnableState == PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {
                return true;
            }
        }

        return false;
    }

    //xieweiwei_20150205_add_begin
    //the system property flag of setting and getting lockscreen wallpaper, values: [0, 1]
    //if they are 0, we use "wallpaper" as the name of the operation file,
    //if they are 1, we use "lockscreen_wallpaper" as the name of the operation file
    public static final String KEY_IS_SET_LOCKSCREEN_WALLPAPER = "isSetLockscreenWallpaper";
    public static final int VALUE_LOCKSCREEN_WALLPAPER_NO = 0;
    public static final int VALUE_LOCKSCREEN_WALLPAPER_YES = 1;
    public static void setFlagOfSettingLockscreenWallpaper(ContentResolver contentResolver, boolean flag) {
        if (flag) {
            Settings.System.putInt(contentResolver, KEY_IS_SET_LOCKSCREEN_WALLPAPER, VALUE_LOCKSCREEN_WALLPAPER_YES);
        } else {
            Settings.System.putInt(contentResolver, KEY_IS_SET_LOCKSCREEN_WALLPAPER, VALUE_LOCKSCREEN_WALLPAPER_NO);
        }
    }
    public static boolean isLockscreenWallpaperIntent(Intent intent) {
        if (intent != null && intent.getExtras() != null) {
            return intent.getExtras().getBoolean(KEY_IS_SET_LOCKSCREEN_WALLPAPER, false);
        }
        return false;
    }
    //xieweiwei_20150205_add_end
    
    /**
	 * ÊÇ·ñº¬ÓÐÐéÄâ¼ü
	 * 
	 * @param context
	 * @return
	 */
	public static boolean checkDeviceHasNavigationBar(Context context) {
		if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
			boolean hasNavigationBar = false;
			Resources rs = context.getResources();
			int id = rs.getIdentifier("config_showNavigationBar", "bool",
					"android");
			if (id > 0) {
				hasNavigationBar = rs.getBoolean(id);
			}
			try {
				Class systemPropertiesclass = Class
						.forName("android.os.SystemProperties");
				Method m = systemPropertiesclass.getMethod("get", String.class);
				String navBarOverride = (String) m.invoke(
						systemPropertiesclass, "qemu.hw.mainkeys");
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
	 * »ñÈ¡ÐéÄâ°´¼üµÄ¸ß¶È
	 * 
	 * @param context
	 * @return
	 */
	public static int getNavigationBarHeight(Context context) {

		int navigationBarHeight = 0;
		if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
			Resources rs = context.getResources();
			int id = rs.getIdentifier("navigation_bar_height", "dimen",
					"android");
			if (id > 0 && checkDeviceHasNavigationBar(context)) {
				navigationBarHeight = rs.getDimensionPixelSize(id);
			}
			return navigationBarHeight;
		}
		return 0;
	}

	public static  boolean hasDigit(String content) {

    	boolean flag = false;

    	Pattern p = Pattern.compile(".*\\d+.*");

    	Matcher m = p.matcher(content);

    	if (m.matches())

    	flag = true;

    	return flag;

    	}
	
	
	//截取数字  
	public static  String getNumbers(String content) {  
	    Pattern pattern = Pattern.compile("\\d+");  
	    Matcher matcher = pattern.matcher(content);  
	    String num = null;
	    while (matcher.find()) {  
	        num= matcher.group(0);  
	    }  
	    return num;  
	}  
	
	public static  String getFirstNumbers(String content) {  
	    Pattern pattern = Pattern.compile("\\d+");  
	    Matcher matcher = pattern.matcher(content);  
	    String num = null;
	    while (matcher.find()) {  
	        return matcher.group(0);  
	    }  
	    return null;  
	}
	
	public static  InputStream getSavedWallpaper(String path) {
		FileInputStream fis = null;
		File imgFile = new File(path);
		if (imgFile.exists()) {
			try {
				fis = new FileInputStream(path);
			} catch (Exception e) {
			}
		} else {
		}
		return fis;
	}
	
	static public void setWallpaper(final Intent intent,final Context c) {
		try {
			new Thread(new Runnable() {

				@Override
				public void run() {
					String path = intent.getStringExtra("wall_path");
					boolean  istheme = intent.getBooleanExtra("istheme", false);
					InputStream is=null;
					if(istheme) {
						is = LqThemeParser.getWallpaper(c, path);
					}else {
						 is = getSavedWallpaper(path);
					}
					if (is != null) {
						android.app.WallpaperManager wallpaperManager = android.app.WallpaperManager
								.getInstance(c);
						try {
							wallpaperManager.setStream(is);
						} catch (IOException e) {
							e.printStackTrace();
						} finally {
							if (is != null) {
								try {
									is.close();
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
						}

					}
				}
			}).start();
		} catch (Exception e) {
		}
	}
	static public void startSetWallpaperSevice(String path,Context c,boolean istheme,Intent intent) {
		if(intent==null) {
			 intent = new Intent(c, WallpaperSetService.class);
		}
		intent.putExtra("wall_path", path);
		intent.putExtra("istheme", istheme);
		/** 进入Activity开始服务 */
		c.startService(intent);
	}
	
	static public void setBlueWallpaper(View v,final Launcher l) {

		v.postDelayed(new Runnable() {
			
			@Override
			public void run() {

				BlueTaskWall b = new BlueTaskWall(l, l.getWallpaperBg());
				b.execute();
			}
		}, 500);
	}
	
	//判断设备管理器是否激活
	static public List getActiveAppList(Context context) {
		List<ResolveInfo>  avail  = context.getPackageManager().queryBroadcastReceivers(
                new Intent(DeviceAdminReceiver.ACTION_DEVICE_ADMIN_ENABLED),
                PackageManager.GET_META_DATA);
		
		ArrayList list = new ArrayList<>();
		if(avail != null){
			for(ResolveInfo Info:avail){
				try {
					DeviceAdminInfo adminInfo = new DeviceAdminInfo(context, Info);
					if(isActiveAdmin(adminInfo,context)){
						list.add(Info.activityInfo.packageName);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return list;
		}
		return null;
		
	}
	 private static DevicePolicyManager mDPM;
	 private static boolean isActiveAdmin(DeviceAdminInfo item,Context context) {
		    mDPM = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
	        return mDPM.isAdminActiveAsUser(item.getComponent(), getUserId(item));
	    }
	private static int getUserId(DeviceAdminInfo adminInfo) {
        return UserHandle.getUserId(adminInfo.getActivityInfo().applicationInfo.uid);
    }
	
	
	public static  String toFormatLanguage(String name,String defaultName) {
    	String result=defaultName;
        String t ="CN;TW;US";
        String t1 ="CN;HK;US";
        String t2 ="CN;TW;GB";
        List<String> languages = Arrays.asList(t.split(";"));
        String []names = name.split(";");
        int i=languages.indexOf(Launcher.locale);
        if(i==-1) {
        	languages = Arrays.asList(t1.split(";"));
        	i=languages.indexOf(Launcher.locale);
        }
        if(i==-1) {
        	languages = Arrays.asList(t2.split(";"));
        	i=languages.indexOf(Launcher.locale);
        }
        if(i !=-1) {
        	result=	names[i];
        }
        
        return result;
        
        
    
    }
	
	/**
	 * 重复点击
	 */
	
	 private static long lastClickTime;
	    public synchronized static boolean isFastClick(long tweentime) {
	        long time = System.currentTimeMillis();   
	        if ( time - lastClickTime < tweentime) {   
	            return true;   
	        }   
	        lastClickTime = time;   
	        return false;   
	    }
}
