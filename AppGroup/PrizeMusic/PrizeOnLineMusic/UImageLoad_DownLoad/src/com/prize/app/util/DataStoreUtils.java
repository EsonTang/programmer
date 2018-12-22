package com.prize.app.util;

import java.io.InputStream;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.BitmapDrawable;

import com.prize.app.BaseApplication;

public class DataStoreUtils {
	public static final String FILE_NAME = "prize_music";
	/**边听变下载*/
	public static final String SWITCH_LISTEN_DOWNLOAD = "listen_download";
	/**WiFi自动下载*/
	public static final String SWITCH_WIFI_DOWNLOAD = "wifi_download";
	/**弹框提示*/
	public static final String SWITCH_HINT_SHOW = "hint_show";
	/**定时停止*/
	public static final String SWITCH_TIME_STOP = "time_stop";
	

	// check box on off
	public static final String CHECK_ON = "on";
	public static final String CHECK_OFF = "off";

	public static final String DEFAULT_VALUE = "";
	public static final String SWITCH_SAVETRAFFIC = "switch_savetraffic";
	
	// 保存本地信息
	public static void saveLocalInfo(String name, String value) {
		SharedPreferences share = BaseApplication.curContext
				.getSharedPreferences(FILE_NAME, Activity.MODE_WORLD_WRITEABLE);

		if (share != null) {
			share.edit().putString(name, value).apply();
		}
	}

	// 读取本地信息
	public static String readLocalInfo(String name) {
		
		Context c =null;
		try {
			c  = BaseApplication.curContext.createPackageContext(BaseApplication.curContext.getPackageName(), Context.CONTEXT_IGNORE_SECURITY);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
	
		SharedPreferences share = c.getSharedPreferences(
		    		  FILE_NAME, Context.MODE_WORLD_READABLE + Context.MODE_WORLD_WRITEABLE
		                      + Context.MODE_MULTI_PROCESS);
		if(share!=null){
			return share.getString(name, DEFAULT_VALUE);
		}
	    
		return DEFAULT_VALUE;
	}

	/**
	 * 从asserts 目录下读取图片文件
	 * 
	 * @param context
	 * @param fileName
	 * @return
	 */
	public static BitmapDrawable readImgFromAssert(Context context,
			String imgFileName) {
		InputStream inputStream = null;
		BitmapDrawable drawable = null;

		if (null == imgFileName) {
			return null;
		}

		try {
			inputStream = context.getResources().getAssets().open(imgFileName);
			drawable = new BitmapDrawable(inputStream);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (null != inputStream) {
				try {
					inputStream.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		}
		return drawable;
	}

}
