package com.prize.prizenavigation.utils;

import android.os.Environment;

import java.io.File;

/**
 * 升级常量类
 */
public class Constants {
   //测试地址
//	public static final String GIS_URL = "http://testnewapi.szprize.cn";

	public static final String GIS_URL = "http://newapi.szprize.cn";

    //	http://newapi.szprize.cn/ThemeStore/UpgradeInfo/check
    // 系统版自升级url
	public static final String SYSTEM_UPGRADE_URL = GIS_URL
			+ "/Prompt/Prompt/upgradeInfo";

	public static final String GIS_URL_DEVICE = "http://ics.szprize.cn/ics";
	/****接口安全校验，先请求改接口来获取一个pid*****/
	public static final String PID_URL = GIS_URL_DEVICE+"/api/pid";
	/****此接口需要与pid接口联用*****/
	public static final String UUID_URL = GIS_URL_DEVICE+"/api/uuid";

	public static final String ANDROID_APP_SUFFIX = ".apk";
//	public static final String PRIZE_TEM_FILE_SUFFIX = ".prize";
	public static final String PRIZENAVIGATION = "prizeNavigation";
//	public static final int PUSH_FOR_TIME = 60 * 2;
	/****实际发布后****/
	public static final int PUSH_FOR_TIME = 60 * 60 * 4;
//	public static final int PUSH_FOR_TIME_ENTER = 60 * 60 * 1;
	public static final int PUSH_FOR_TIME_ENTER = 60;
	public static final int PAGE_SIZE = 20;
	public static String DOWNLOAD_FOLDER_NAME = "download";
	public static final String DOWNLOAD_FILE_NAME = PRIZENAVIGATION
			+ ANDROID_APP_SUFFIX;

	public static final String APKFILEPATH = new StringBuilder(Environment
			.getExternalStorageDirectory().getAbsolutePath())
			.append(File.separator).append(Constants.DOWNLOAD_FOLDER_NAME)
			.append(File.separator).append(Constants.DOWNLOAD_FILE_NAME)
			.toString();


	public static final String APKPATH = new StringBuilder(Environment
			.getExternalStorageDirectory().getAbsolutePath())
			.append(File.separator).append(Constants.DOWNLOAD_FOLDER_NAME)
			.append(File.separator).append(Constants.PRIZENAVIGATION)
			.toString();

	public static final String KEY_NAME_DOWNLOAD_ID = "downloadId";
	public static final String APP_MD5 = "appMD5";


	public static final String LAST_MODIFY = "last-modify";
	
	public static final String KEY_TID = "persist.sys.tid";


}
