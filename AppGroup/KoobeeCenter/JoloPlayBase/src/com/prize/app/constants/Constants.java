package com.prize.app.constants;

import java.io.File;

import android.os.Environment;

public class Constants {

	// 现网地址
	 public static final String GIS_URL =
	 "http://appstore.szprize.cn/appstore";
	 
//	public static final String GIS_URL = "http://192.168.1.235:8080/appstore";

	// public static final String GIS_URL =
	// "http://192.168.1.187:8080/appstore";
	public static final String SYSTEM_UPGRADE_URL = GIS_URL
			+ "/ecard/upgrade"; // 系统版自升级url
	public static final String THIRD_UPGRADE_URL = GIS_URL
			+ "/appinfo/upgradeSpecial"; // 三方版自升级url

	
	public static final String GIS_URL_DEVICE = "http://ics.szprize.cn/ics";
	/****接口安全校验，先请求改接口来获取一个pid*****/
	public static final String PID_URL = GIS_URL_DEVICE+"/api/pid";
	/****此接口需要与pid接口联用*****/
	public static final String UUID_URL = GIS_URL_DEVICE+"/api/uuid";
	
	
	// 下载模块的常量
	public static final String QES_ACCEPT_CONTENT_TYPE = "application/octet-stream,application/vnd.android.package-archive";
	public static final String QES_UNACCEPT_CONTENT_TYPE = "text/html,text/plain";
	public static final String ANDROID_APP_SUFFIX = ".apk";
	public static final String PRIZE_TEM_FILE_SUFFIX = ".prize";
	public static final String PRIZEAPPCENTER = "ElectronicCard";
//	public static final String PPASSISTANT = "PPAssistant";
	
//	public static final int PUSH_FOR_TIME = 60 * 2;
	/****实际发布后****/
	public static final int PUSH_FOR_TIME = 60 * 60 * 4;
//	public static final int PUSH_FOR_TIME_ENTER = 60 * 60 * 1;
	public static final int PUSH_FOR_TIME_ENTER = 60;
	public static final int PAGE_SIZE = 20;
	public static String DOWNLOAD_FOLDER_NAME = "download";
	public static final String DOWNLOAD_FILE_NAME = PRIZEAPPCENTER
			+ ANDROID_APP_SUFFIX;
//	public static final String DOWNLOAD_APK_NAME = PPASSISTANT
//			+ ANDROID_APP_SUFFIX;
	public static final String APKFILEPATH = new StringBuilder(Environment
			.getExternalStorageDirectory().getAbsolutePath())
			.append(File.separator).append(Constants.DOWNLOAD_FOLDER_NAME)
			.append(File.separator).append(Constants.DOWNLOAD_FILE_NAME)
			.toString();
	
//	public static final String APKINSTALL = new StringBuilder(Environment
//			.getExternalStorageDirectory().getAbsolutePath())
//			.append(File.separator).append(Constants.DOWNLOAD_FOLDER_NAME)
//			.append(File.separator).append(Constants.DOWNLOAD_APK_NAME)
//			.toString();

	public static final String KEY_NAME_DOWNLOAD_ID = "downloadId";
	public static final String APP_MD5 = "appMD5";

	// ///数据统计event
	public static final String E_ENTER_APP_DETAIL = "E_ENTER_APP_DETAIL";
	// 统计参数命名
	public static final String EVT_ENTER_APP_DETAIL_ID = "ENTER_APP_DETAIL_ID";

	/** 首页轮播图 */
	public static final String E_HOMEPAGER_GALLERYFLOW_CLICK = "E_HOMEPAGER_GALLERYFLOW_CLICK";
	public static final String EVT_HOMEPAGER_GALLERYFLOW_CLICK_ID = "HOMEPAGER_GALLERYFLOW_CLICK_ID_";

	/** 装机必备 一键下载 */
	public static final String E_ONEKEY_DOWNLOAD_BTN = "E_ONEKEY_DOWNLOAD_BTN";
	public static final String EVT_ONEKEY_DOWNLOAD_BTN_ID = "ONEKEY_DOWNLOAD_BTN_ID";

	/** 热门专题 游戏游戏带我飞 */
	public static final String E_CLICK_APP_TOPIC = "E_CLICK_APP_TOPIC";
	public static final String EVT_CLICK_APP_TOPIC_ID = "CLICK_APP_TOPIC_ID_";

	/** 上线新品 */
	public static final String E_CLICK_APP_NEWPRODUCTOR = "E_CLICK_APP_NEWPRODUCTOR";
	public static final String EVT_CLICK_APP_NEWPRODUCTOR_ID = "CLICK_APP_NEWPRODUCTOR_ID";

	/** 软件榜单 */
	public static final String E_CLICK_RANKING_SOFT_RANK = "E_CLICK_RANKING_SOFT_RANK";
	public static final String EVT_CLICK_RANKING_SOFT_RANK_ID = "CLICK_RANKING_SOFT_RANK_ID";

	/** 游戏榜单 */
	public static final String E_CLICK_RANKING_GAME_RANK = "E_CLICK_RANKING_GAME_RANK";
	public static final String EVT_CLICK_RANKING_GAME_RANK_ID = "CLICK_RANKING_GAME_RANK_ID";

	/** 应用轮播图 */
	public static final String E_APPTYPE_GALLERYFLOW_CLICK = "E_APPTYPE_GALLERYFLOW_CLICK";
	public static final String EVT_APPTYPE_GALLERYFLOW_CLICK_ID = "APPTYPE_GALLERYFLOW_CLICK_ID_";

	/** 应用分类 */
	public static final String E_APPTYPE_CATEGORY_CLICK = "E_APPTYPE_CATEGORY_CLICK";
	public static final String EVT_APPTYPE_CATEGORY_CLICK_ID = "APPTYPE_CATEGORY_CLICK_ID";

	/** 应用排行榜 */
	public static final String E_APPTYPE_RANK_CLICK = "E_APPTYPE_RANK_CLICK";
	public static final String EVT_APPTYPE_RANK_CLICK_ID = "APPTYPE_RANK_CLICK_ID";

	/** 应用必备 */
	public static final String E_APPTYPE_REQUIRED_CLICK = "E_APPTYPE_REQUIRED_CLICK";
	public static final String EVT_APPTYPE_REQUIRED_CLICK_ID = "APPTYPE_REQUIRED_CLICK_ID";

	/** 应用专题 */
	public static final String E_APPTYPE_TOPIC_CLICK = "E_APPTYPE_TOPIC_CLICK";
	public static final String EVT_APPTYPE_TOPIC_CLICK_ID = "APPTYPE_TOPIC_CLICK_ID";

	/** 游戏轮播图 */
	public static final String E_GAMETYPE_GALLERYFLOW_CLICK = "E_GAMETYPE_GALLERYFLOW_CLICK";
	public static final String EVT_GAMETYPE_GALLERYFLOW_CLICK_ID = "GAMETYPE_GALLERYFLOW_CLICK_ID_";

	/** 游戏分类 */
	public static final String E_GAMETYPE_CATEGORY_CLICK = "E_GAMETYPE_CATEGORY_CLICK";
	public static final String EVT_GAMETYPE_CATEGORY_CLICK_ID = "GAMETYPE_CATEGORY_CLICK_ID";

	/** 游戏排行榜 */
	public static final String E_GAMETYPE_RANK_CLICK = "E_GAMETYPE_RANK_CLICK";
	public static final String EVT_GAMETYPE_RANK_CLICK_ID = "GAMETYPE_RANK_CLICK_ID";

	/** 游戏必玩 */
	public static final String E_GAMETYPE_REQUIRED_CLICK = "E_GAMETYPE_REQUIRED_CLICK";
	public static final String EVT_GAMETYPE_REQUIRED_CLICK_ID = "GAMETYPE_REQUIRED_CLICK_ID";

	/** 游戏网游 */
	public static final String E_GAMETYPE_NETGAME_CLICK = "E_GAMETYPE_NETGAME_CLICK";
	public static final String EVT_GAMETYPE_NETGAME_CLICK_ID = "GAMETYPE_NETGAME_CLICK_ID";

	/** 个人中心 */
	public static final String E_APP_PERSONALCENTER_CLICK = "E_APP_PERSONALCENTER_CLICK";
	public static final String EVT_APP_PERSONALCENTER_CLICK_ID = "APP_PERSONALCENTER_CLICK_ID";

	/** 应用更新 */
	public static final String E_APP_UPDATE_CLICK = "E_APP_UPDATE_CLICK";
	public static final String EVT_APP_UPDATE_CLICK_ID = "APP_UPDATE_CLICK_ID";

	/** 热门精选 */
	public static final String E_APP_HOT_CLICK = "E_APP_HOT_CLICK";
	public static final String EVT_APP_HOT_CLICK_ID = "APP_HOT_CLICK_ID";
	public static final String ACTION_UNREAD_CHANGED = "com.prizeappcenter.action.UNREAD_CHANGED";
	public static final String EXTRA_UNREAD_NUMBER = "com.prizeappcenter.intent.extra.UNREAD_NUMBER";
	
	/**首页卡片类型*/
	public static final String TOP_DOWNLOAD = "top_download";
	public static final String TOPIC_APPS = "topic_apps";
	public static final String ZUIMEI_APPS = "zuimei_apps";
	public static final String CARD_HAPPS = "card_happs";
	public static final String SINGLE_APP = "single_app";
	public static final String TOPIC_NO_BANNER = "topic_simple_apps";
	

	public static final String LAST_MODIFY = "last-modify";
	
	public static final String KEY_TID = "persist.sys.tid";
}
