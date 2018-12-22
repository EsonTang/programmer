package com.prize.prizeappoutad.utils;

import java.io.InputStream;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.BitmapDrawable;

public class DataStoreUtils {
	public static final String FILE_NAME = "prize_base";
	/** 通用 true */
	public static final String SP_TRUE = "true";
	/** 通用 false */
	public static final String SP_FALSE = "false";

	/** 封面页更新时间 */
	public static final String FRONT_COVER_UPDATE_TIME = "a";
	/** 游戏列表文件 版本 */
	public static final String GAME_PKG_FILE_VER = "b";
	/** 游戏列表文件 下载地址 */
	public static final String GAME_PKG_FILE_URL = "c";
	/** 收集本机app开关 状态 */
	public static final String SWITCH_APP_UPLOAD_STATE = "d";
	public static final String SWITCH_APP_UPLOAD_STATE_ENABLE = "1";
	public static final String SWITCH_APP_UPLOAD_STATE_UNABLE = "";
	/** 收集本机app开关 时间间隔 */
	public static final String SWITCH_APP_UPLOAD_INTERVAL = "e";
	public static final String UPLOAD_LAST_TIME = "upload_last_time";
	public static final String UPLOAD_INFO = "upload_info";
	// 退出是否继续下载设置
	public static final String CONTINUE_DOWNLOAD_EXIT = "f";
	public static final String CONTINUE_DOWNLOAD_ENABLE = "1";// 退出后继续下载,不杀进程
	public static final String CONTINUE_DOWNLOAD_UNABLE = "0"; // 退出后不继续下载,杀进程
	public static final String CONTINUE_DOWNLOAD_UNKNOW = ""; // 默认退出的时候，不继续下载
	// HTC 微博 微信 BBS
	public static final String HTC_WEIBO = "htc_weibo";
	public static final String HTC_WEIXIN = "htc_weixin";
	public static final String HTC_BBS = "htc_bbs";
	// 记录弹出消息推送的时间
	public static final String PUSH_TIME = "push_time";
	// 记录弹出消息推送的应用
	public static final String PUSH_APP = "push_app";

	// 做留存用
	public static final String FLAG_STAY = "w";
	/********* 更新信息 ***************/
	// 是否需要更新
	public static final String UPDATE_STATE = "x";
	public static final String UPDATE_STATE_ENABLE = "1";
	//
	public static final String UPDATE_APKDOWNLOADURL = "x1";
	public static final String UPDATE_APKNEWVER = "x2";
	public static final String UPDATE_APKNEWVERINT = "x3";
	public static final String UPDATE_UPGRADEPOLICY = "x4";
	public static final String UPDATE_UPGRADETOOLTIP = "x5";

	/*****************************/

	// WIFI设置 wifi环境下下载
	public static final String DOWNLOAD_WIFI_ONLY = "y";
	public static final String DOWNLOAD_WIFI_ONLY_ENABLE = "1";
	public static final String DOWNLOAD_WIFI_ONLY_UNABLE = "0";

	// check box on off
	public static final String CHECK_ON = "on";
	public static final String CHECK_OFF = "off";

	public static final String DEFAULT_VALUE = "";
	// 平台安装记录
	public static final String JOLY_PF = "z";
	public static final String JOLY_PF_UNINSTALLED = "";
	public static final String JOLY_PF_INSTALLED = "1";
	/** 平台启动的最后时间 */
	public static final String LAST_ACTIVATE_CLIENT_TIME = "last_activate_t";
	/** 是否提醒用户, on , off */
	public static final String REMIND_USER_ENABLE = "remind";
	/** 多长时间提醒, 单位：ms */
	public static final String REMIND_INTERVAL_MS = "remind_ms";
	/** 替换游戏对话框是否不再显示 */
	public static final String REPLACE_GAME_ALERT_NOT_SHOW = "not_show_replace";
	/** 软件更新提示设置 **/
	public static final String GAME_UPDATES_REMINDER = "update_reminder";
	/** 自动下载更新包 */
	public static final String AUTO_LOAD_UPDATE_PKG = "auto_load";
	/** 安装后删除安装包 */
	public static final String AUTO_DEL_PKG = "auto_dele";
	/** notify news switch */
	public static final String NOTIFY_NEWS_ENABLE = "notify_news";
	/** homepage 山寨更新区是否显示 */
	public static final String HOME_HIDE_PIRATE_UPDATE = "home_show_p_u";

	public static final String USER_LOG_STATE = "log";
	/** 用户注销的记录 */
	public static final String LOGOFF_USER = "logoff";

	/** WIFI下提示“一键安装界面的开关和时间间隔” **/
	public static final String NESSARY_ENABLE = "nessary_enable";
	public static final String NESSARY_INTERVAL = "nessary_interval";
	public static final String LAST_GET_NESSARY_TIME = "last_get_nessary_time";

	public static final String SWITCH_INSTALL_SILENT = "silent";
	public static final String SWITCH_SAVETRAFFIC = "saveTraffic";
	public static final String SWITCH_PUSH_NOTIFICATION = "push_notification";
	public static final String SWITCH_ADD_LUANCHER_FOLDER = "add_luancher_folder";
	public static final String UPDATA_NEED = "updata_need";
	/** 记住我的选择 */
	public static final String REMENBER_CHOICE = "remenber_choice";
	/** 保存当前服务器应用版本 */
	public static final String NEW_VERSION = "new_version";
	/** 一键安装弹框 */
	public static final String VERSION_CODE = "version_code";
	public static final String REQUEST_ALLAPP = "request_allapp";
	public static final String UUID = "uuid";
	public static final String DOWNLOAD_NEWVERSION_OK = "download_newversion_ok";
	public static final String ADMIN = "admin";
	public static final String PUSHREQUESTFREQUENCY = "pushRequestFrequency";

	// 保存本地信息
	public static void saveLocalInfo(String name, String value) {
		// SharedPreferences share = BaseApplication.curContext
		// .getSharedPreferences(FILE_NAME, Activity.MODE_MULTI_PROCESS);
		//
		// if (share != null) {
		// share.edit().putString(name, value).apply();
		// }
		// TODO
	}

	// 读取本地信息
	public static String readLocalInfo(String name) {
		// SharedPreferences share = BaseApplication.curContext
		// .getSharedPreferences(FILE_NAME, Activity.MODE_MULTI_PROCESS);
		SharedPreferences share = null;
		if (share != null) {
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
