package com.prize.app.constants;

import android.os.Environment;

import java.io.File;

public class Constants {
   //测试地址
//	public static final String GIS_URL = "http://192.168.1.158:8084";

	public static final String GIS_URL = "http://newapi.szprize.cn";

    //	http://newapi.szprize.cn/ThemeStore/UpgradeInfo/check
    // 系统版自升级url
	public static final String SYSTEM_UPGRADE_URL = GIS_URL
			+ "/ThemeStore/UpgradeInfo/check";

	public static final String GIS_URL_DEVICE = "http://ics.szprize.cn/ics";
	/****接口安全校验，先请求改接口来获取一个pid*****/
	public static final String PID_URL = GIS_URL_DEVICE+"/api/pid";
	/****此接口需要与pid接口联用*****/
	public static final String UUID_URL = GIS_URL_DEVICE+"/api/uuid";

	public static final String ANDROID_APP_SUFFIX = ".apk";
//	public static final String PRIZE_TEM_FILE_SUFFIX = ".prize";
	public static final String PRIZETHEMECENTER = "prizeThemeCenter";
//	public static final int PUSH_FOR_TIME = 60 * 2;
	/****实际发布后****/
	public static final int PUSH_FOR_TIME = 60 * 60 * 4;
//	public static final int PUSH_FOR_TIME_ENTER = 60 * 60 * 1;
	public static final int PUSH_FOR_TIME_ENTER = 60;
	public static final int PAGE_SIZE = 20;
	public static String DOWNLOAD_FOLDER_NAME = "download";
	public static final String DOWNLOAD_FILE_NAME = PRIZETHEMECENTER
			+ ANDROID_APP_SUFFIX;

	public static final String APKFILEPATH = new StringBuilder(Environment
			.getExternalStorageDirectory().getAbsolutePath())
			.append(File.separator).append(Constants.DOWNLOAD_FOLDER_NAME)
			.append(File.separator).append(Constants.DOWNLOAD_FILE_NAME)
			.toString();


	public static final String APKPATH = new StringBuilder(Environment
			.getExternalStorageDirectory().getAbsolutePath())
			.append(File.separator).append(Constants.DOWNLOAD_FOLDER_NAME)
			.append(File.separator).append(Constants.PRIZETHEMECENTER)
			.toString();

	public static final String KEY_NAME_DOWNLOAD_ID = "downloadId";
	public static final String APP_MD5 = "appMD5";

	public static final String ACTION_UNREAD_CHANGED = "com.prizeappcenter.action.UNREAD_CHANGED";
	public static final String EXTRA_UNREAD_NUMBER = "com.prizeappcenter.intent.extra.UNREAD_NUMBER";

	public static final String LAST_MODIFY = "last-modify";
	
	public static final String KEY_TID = "persist.sys.tid";

	/**应用某主题广播带的参数 (包名)**/
	public static final String BRD_EXTRA = "pkg_name";
	/**参数类型， 暂定给锁屏用 1表示另一个参数为包名， 2表示另一个参数为文件路径； 若是壁纸或主题的话表示 0表示固定壁纸, 1表示滑动壁纸**/
	public static final String BRD_EXTRA_TYPE = "p_type";

	/**应用某锁屏广播**/
	public static final String BRD_ACTION_APPLY_LOCK = "lockscr_applied_notification";

	/**应用桌面壁纸广播**/
	public static final String BRD_ACTION_APPLY_LAUNCHER = "com.android.launcher3.action.PRELOAD_WORKSPACE";

	/**应用桌面壁纸广播**/
	public static final String BRD_ACTION_APPLY_SCROLL_LAUNCHER = "com.android.launcher3.action.SCROLL_WORKSPACE";

	/*本地主题数据库地址*/
	public static final String LOCAL_THEME_PATH = "content://com.android.launcher3.provider.theme/"
			+ "t_theme_table";

	/*本地壁纸数据库地址*/
	public static final String LOCAL_WALLPAGE_PATH = "content://com.android.launcher3.provider.theme/"
			+ "t_wallpaper_table";

	/*应用主题action*/
	public static final String RECEIVER_ACTION = "appley_theme_ztefs";
	public static final String QES_ACCEPT_CONTENT_TYPE = "application/octet-stream,application/vnd.android.package-archive";
	public static final String QES_UNACCEPT_CONTENT_TYPE = "text/html,text/plain,text/html;charset=GBK";

	public static final String SHIELDPACKAGES="shieldPackages";
}
