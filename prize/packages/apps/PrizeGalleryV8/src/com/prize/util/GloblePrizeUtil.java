
package com.prize.util;

import java.io.File;
import java.lang.reflect.Method;

import com.android.gallery3d.app.Log;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.text.TextUtils;

public class GloblePrizeUtil {

	public static String PRIZE_NAVBAR_STATE = Settings.System.PRIZE_NAVBAR_STATE;
	
	public static boolean checkContianNavigationBar(Context context){
		boolean hasNavigationBar = false;
		Resources rs = context.getResources();
		int id = rs
				.getIdentifier("config_showNavigationBar", "bool", "android");
		if (id > 0) {
			hasNavigationBar = rs.getBoolean(id);
		}
	
		try {
			Class systemPropertiesClass = Class
					.forName("android.os.SystemProperties");
			Method m = systemPropertiesClass.getMethod("get", String.class);
			String navBarOverride = (String) m.invoke(systemPropertiesClass,
					"qemu.hw.mainkeys");
			if ("1".equals(navBarOverride)) {
				hasNavigationBar = false;
			} else if ("0".equals(navBarOverride)) {
				hasNavigationBar = true;
			}
		} catch (Exception e) {
			
		}
		return hasNavigationBar;
	}

	public static boolean checkDeviceHasNavigationBar(Context context) {
		boolean hasNavigationBar = false;
		Resources rs = context.getResources();
		int postion = -1;
		try {
			postion = Settings.System.getInt(context.getContentResolver(), PRIZE_NAVBAR_STATE);
		} catch (SettingNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		int id = rs
				.getIdentifier("config_showNavigationBar", "bool", "android");
		if (id > 0) {
			hasNavigationBar = rs.getBoolean(id);
		}
	
		try {
			Class systemPropertiesClass = Class
					.forName("android.os.SystemProperties");
			Method m = systemPropertiesClass.getMethod("get", String.class);
			String navBarOverride = (String) m.invoke(systemPropertiesClass,
					"qemu.hw.mainkeys");
			if ("1".equals(navBarOverride)) {
				hasNavigationBar = false;
			} else if ("0".equals(navBarOverride)) {
				hasNavigationBar = true;
			}
		} catch (Exception e) {
			
		}
		if(context.getResources().getConfiguration().orientation != Configuration.ORIENTATION_PORTRAIT){
			hasNavigationBar = false;
		}
		if(postion == 0){
			hasNavigationBar = false;
		}
		
		return hasNavigationBar;
	}
	//prize-add：verify whether system supports dynamic show navigation bar or not -pengcancan-20161114-start
	private static boolean checkSupportDynamicNav(Context context){
		boolean supportDynamicNav = false;
		try {
			Class systemPropertiesClass = Class
					.forName("android.os.SystemProperties");
			Method m = systemPropertiesClass.getMethod("get", String.class);
			String navBarOverride = (String) m.invoke(systemPropertiesClass,
					"ro.support_hiding_navbar");
			if ("0".equals(navBarOverride)) {
				supportDynamicNav = false;
			} else if ("1".equals(navBarOverride)) {
				supportDynamicNav = true;
			}
		} catch (Exception e) {
			
		}
		return supportDynamicNav;
	}
	//prize-add：verify whether system supports dynamic show navigation bar or not -pengcancan-20161114-end
	
	public static boolean isShowNavigationBar(Context context) {
		boolean show = false;
		if (checkSupportDynamicNav(context)) {
			Resources rs = context.getResources();
			int postion = 0;
			try {
				postion = Settings.System.getInt(context.getContentResolver(), PRIZE_NAVBAR_STATE);
			} catch (SettingNotFoundException e1) {
				e1.printStackTrace();
			}
			
			if (postion != 0) {
				show = true;
			}
		}else {
			if (checkContianNavigationBar(context)) {
				show = true;
			}
		}
		Log.i("pengcc", "[isShowNavigationBar] show : " + show);
		return show;
	}

	public static int getNavigationBarHeight(Context context) {
		int navigationBarHeight = 0;
		Resources rs = context.getResources();
		int id = rs.getIdentifier("navigation_bar_height", "dimen", "android");
		
		if (id > 0) {
			navigationBarHeight = rs.getDimensionPixelSize(id);
		}
		return navigationBarHeight;
	}
	
	public static int getStatusHeight(Context context) {
		 
	    int statusHeight = -1;
	    try {
	        Class<?> clazz = Class.forName("com.android.internal.R$dimen");
	        Object object = clazz.newInstance();
	        int height = Integer.parseInt(clazz.getField("status_bar_height")
	                .get(object).toString());
	        statusHeight = context.getResources().getDimensionPixelSize(height);
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    return statusHeight;
	}

	
	
	public static int getNavigationBarAllHeight(Context context) {
		int navigationBarHeight = 0;
		Resources rs = context.getResources();
		int id = rs.getIdentifier("navigation_bar_height", "dimen", "android");
		
		if (id > 0) {
			navigationBarHeight = rs.getDimensionPixelSize(id);
		}
		return navigationBarHeight;
	}
	
	//prize-public-bug:Video is still playing while the file has been deleted-pengcancan-20160225-start
		
		 /**M: @{
	     * get FilePathByURI
	     */
	    public static String getFilePath(final Context mContext, final String filePath) {
	        final ContentResolver cr = mContext.getContentResolver();
	        String filepath = null;
	        Log.i("pengcancan ","---->filePath:"+ filePath);
	        if (!TextUtils.isEmpty(filePath)) {
	                Cursor c = null;
	                try {
	                    c = cr.query(Uri.parse(filePath), null,
	                            null, null, null);
	                    if (c != null && c.moveToFirst()) {
	                        filepath = c.getString(1);
	                    }
	                } catch (SQLiteException e) {
	                	Log.i("pengcanccancan ","database operation error: " + e.getMessage());
	                } catch (Exception e) {
	                    Log.e("pengcancan", "getFilePath Exception", e);
	                } finally {
	                    if (c != null) {
	                        c.close();
	                    }
	                }
	        }
	        return filepath;
	    }



	    /**
	     *M: to check if the media file is removed from SD-card or not.
	     * @param ringtone
	     * @return
	     */
	    public static boolean isFileExisted(Context ctx, String fileURIPath) {
	        boolean result = false;
	        if (fileURIPath != null) {
	        	fileURIPath = Uri.decode(fileURIPath);
	        	String path = null;
	            if (fileURIPath.startsWith("content://")) {
	            	//Prize-public-bug:14387 video player exits while playing-20160412-pengcancan
					if (fileURIPath.contains("internal")||fileURIPath.startsWith("content://mms")) {
						return true;
					}
					path = getFilePath(ctx, fileURIPath);
				}else if (fileURIPath.startsWith("file://")) {
					path = fileURIPath.substring(7);
				}
	            if (!TextUtils.isEmpty(path)) {
	            	try {
	            		result = new File(path).exists();
	            	} catch (Exception e) {
	            		// TODO Auto-generated catch block
	            		e.printStackTrace();
	            	}
	            }
	            Log.i("pengcanccancan ",
	            		"isRingtoneExisted: " + result + " ,ringtone: " + fileURIPath + " ,Path: " + path);
	        }
	        return result;
	    }
		//prize-public-bug:Video is still playing while the file has been deleted-pengcancan-20160225end

}
