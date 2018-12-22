package com.prize.app.util;

import android.app.Activity;
import android.content.SharedPreferences;

import com.prize.app.BaseApplication;

public class DataStoreUtils {
	public static final String FILE_NAME = "prize_base";
	//记录弹出消息推送的时间
	public static final String PUSH_TIME = "push_time";
	//记录弹出消息推送的应用
	public static final String PUSH_APP = "push_app";

	/*****************************/

	// WIFI设置 wifi环境下下载
	public static final String DOWNLOAD_WIFI_ONLY = "y";
	public static final String DOWNLOAD_WIFI_ONLY_ENABLE = "1";
	public static final String DOWNLOAD_WIFI_ONLY_UNABLE = "0";

	// check box on off
	public static final String CHECK_ON = "on";
	public static final String CHECK_OFF = "off";

	public static final String DEFAULT_VALUE = "";
	/** 软件更新提示设置 **/
	public static final String GAME_UPDATES_REMINDER = "update_reminder";
	/** 自动下载更新包 */
	public static final String AUTO_LOAD_UPDATE_PKG = "auto_load";
	/** 安装后删除安装包 */
	public static final String AUTO_DEL_PKG = "auto_dele";

	public static final String SWITCH_INSTALL_SILENT = "silent";
	public static final String SWITCH_SAVETRAFFIC = "saveTraffic";
	public static final String SWITCH_PUSH_NOTIFICATION = "push_notification";
	public static final String SWITCH_PUSH_GARBAGE_NOTIFICATION = "garbage_clean_notification";
	public static final String SWITCH_ADD_LUANCHER_FOLDER = "add_luancher_folder";
	/** 一键安装弹框 */
	public static final String VERSION_CODE = "version_code";
	/** 新版本迭代请求定位*/
	public static final String TIME_FOR_BD = "scanrate_for_bd";
	public static final String DOWNLOAD_NEWVERSION_OK= "download_newversion_ok";
	public static final String PUSHREQUESTFREQUENCY= "pushRequestFrequency";
	public static final String TRASHCLEARPUSHONOFF= "trashClearPushOnOff";
	public static final String TRASHCLEARPUSHFREQUENCY= "trashClearPushFrequency";
	public static final String TRASHCLEARSTORAGEOCUPPYSIZE= "trashClearStorageOcuppySize";
	public static final String TRASHCLEARGARBAGECLEANSIZE= "trashClearGarbageCleanSize";
	// 保存本地信息
	public static void saveLocalInfo(String name, String value) {
		SharedPreferences share = BaseApplication.curContext.getSharedPreferences(FILE_NAME, Activity.MODE_MULTI_PROCESS);

		if (share != null) {
			share.edit().putString(name, value).apply();
		}
	}

	// 读取本地信息
	public static String readLocalInfo(String name) {
		SharedPreferences share = BaseApplication.curContext.getSharedPreferences(FILE_NAME, Activity.MODE_MULTI_PROCESS);
		if (share != null) {
			return share.getString(name, DEFAULT_VALUE);
		}
		return DEFAULT_VALUE;
	}

//	/**
//	 * 从asserts 目录下读取图片文件
//	 *
//	 * @param context
//	 * @param imgFileName
//	 * @return
//	 */
//	public static BitmapDrawable readImgFromAssert(Context context,
//			String imgFileName) {
//		InputStream inputStream = null;
//		BitmapDrawable drawable = null;
//
//		if (null == imgFileName) {
//			return null;
//		}
//
//		try {
//			inputStream = context.getResources().getAssets().open(imgFileName);
//			drawable = new BitmapDrawable(inputStream);
//		} catch (Exception e) {
//			e.printStackTrace();
//		} finally {
//			if (null != inputStream) {
//				try {
//					inputStream.close();
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//			}
//
//		}
//		return drawable;
//	}

}
