package com.prize.prizenavigation.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.BitmapDrawable;

import com.prize.prizenavigation.NavigationApplication;

import java.io.InputStream;

public class DataStoreUtils {
	public static final String FILE_NAME = "prize_base";
	/** 通用 true */
	public static final String SP_TRUE = "true";
	/** 通用 false */
	public static final String SP_FALSE = "false";
	public static final String CHECK_ON = "on";
	public static final String CHECK_OFF = "off";
	public static final String DEFAULT_VALUE = "on";

	public static final String SWITCH_PUSH_NOTIFICATION = "push_notification";
	public static final String SWITCH_SAVETRAFFIC = "switch_savetraffic";
	//记录弹出消息推送的时间
	public static final String PUSH_TIME = "push_time";

	// WIFI设置 wifi环境下下载
	public static final String DOWNLOAD_WIFI_ONLY = "y";
	public static final String DOWNLOAD_WIFI_ONLY_ENABLE = "1";
	public static final String DOWNLOAD_WIFI_ONLY_UNABLE = "0";
	/** 自动下载更新包 */
	public static final String AUTO_LOAD_UPDATE_PKG = "auto_load";
	public static final String PUSHREQUESTFREQUENCY= "pushRequestFrequency";

	public static final String AUTO_CHANGE_THEME = "auto_change_theme";
	public static final String TRAFFIC_DOWNLOAD = "traffic_download";
	public static final String RECEIVE_NOTIFICATION = "receive_notification";

	public static final String DOWNLOAD_NEWVERSION_OK= "download_newversion_ok";
	public static String FONT_DETAIL_KEY= "font_detail_key";//是否推送键值
	public static String FONT_DETAIL_ID= "font_detail_id";
	public static String FONT_DETAIL_URL= "font_detail_url";
	// 保存本地信息
	public static void saveLocalInfo(String name, String value) {
		SharedPreferences share = NavigationApplication.getContext()
				.getSharedPreferences(FILE_NAME, Activity.MODE_MULTI_PROCESS);

		if (share != null) {
			share.edit().putString(name, value).apply();
		}
	}

	// 读取本地信息
	public static String readLocalInfo(String name) {
		SharedPreferences share = NavigationApplication.getContext()
				.getSharedPreferences(FILE_NAME, Activity.MODE_MULTI_PROCESS);
		if (share != null) {
			return share.getString(name, DEFAULT_VALUE);
		}
		return DEFAULT_VALUE;
	}

	/**
	 * 清除本地信息
	 * @param name
	 * @param value
	 */
	public static void removeLocalInfo(String name) {
		SharedPreferences share = NavigationApplication.getContext()
				.getSharedPreferences(FILE_NAME, Activity.MODE_MULTI_PROCESS);
		if (share != null) {
			SharedPreferences.Editor edit = share.edit();
			edit.remove(name);
			edit.apply();
		}
	}

	/**
	 * 保存本地boolean
	 * @param name
	 * @param value
	 */
	public static void saveShareInfo(String name, boolean value) {
		SharedPreferences share = NavigationApplication.getContext()
				.getSharedPreferences(FILE_NAME, Activity.MODE_MULTI_PROCESS);

		if (share != null) {
			share.edit().putBoolean(name, value).apply();
		}
	}

	/**
	 * 读取本地boolean
	 * @param name
	 *
	 */
	public static boolean readShareInfo(String name) {
		SharedPreferences share = NavigationApplication.getContext()
				.getSharedPreferences(FILE_NAME, Activity.MODE_MULTI_PROCESS);
		if (share != null) {
			return share.getBoolean(name, false);
		}
		return false;
	}

	/**
	 * 清除本地boolean
	 * @param name
	 *
	 */
	public static void removeShareInfo(String name) {
		SharedPreferences share = NavigationApplication.getContext()
				.getSharedPreferences(FILE_NAME, Activity.MODE_MULTI_PROCESS);
		if (share != null) {
			SharedPreferences.Editor edit = share.edit();
			edit.remove(name);
			edit.apply();
		}
	}
	/**
	 * 从asserts 目录下读取图片文件
	 * 
	 * @param context
	 * @param imgFileName
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
