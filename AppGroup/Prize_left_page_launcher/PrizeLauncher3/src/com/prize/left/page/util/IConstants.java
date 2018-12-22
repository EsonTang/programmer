package com.prize.left.page.util;

import java.io.File;

import android.os.Environment;

/***
 * 常量类
 * @author fanjunchen
 *
 */
public interface IConstants {
	/**自己服务器的host URL*/
	public static final String BASE_URL = "";
	
	public static final String BD_BASE = "http://m.baidu.com/s?from=1012322k";
	/**调用系统下载所产生的下载ID*/
	public static final String KEY_NAME_DOWNLOAD_ID = "sys_down_id";
	/**要下载APK的版本号*/
	public static final String KEY_DOWNLOAD_CODE = "verCode";
	/**更新时间key*/
	public static final String KEY_REFRESH_TIME = "refreshTime";

	/**更新时间key*/
	public static final String KEY_REFRESH_DESK_TIME = "refreshDeskTime";
	/**更新时间key*/
	public static final String KEY_REFRESH_FOLDER_TIME = "refreshDeskTime";
	/**更新时间key*/
	public static final String KEY_REFRESH_UP_TIME = "refreshUpTime";
	/**同步卡片访问时间key*/
	public static final String KEY_SYNC_ACCESS_TIME = "syncAccessTime";
	/**同步卡片访问间隔时间key*/
	public static final String KEY_BT_SYNC_ACCESS_TIME = "syncBTAccessTime";
	/**访问网络导航时间key*/
	public static final String KEY_NET_NAVI_ACCESS_TIME = "accessTime";
	/**访问网络push时间key*/
	public static final String KEY_NET_PUSH_ACCESS_TIME = "pushAccessTime";
	/**访问网络push时间key*/
	public static final String KEY_NET_ICON_ACCESS_TIME = "iconAccessTime";
	/**建议卡片是否可见key*/
	public static final String KEY_SUGG_ISVISIBLE = "suggestionVisible";
	
	/**导航卡片uitype*/
	public static final String NAVI_CARD_UITYPE = "navigation";
	/**常用卡片uitype*/
	public static final String RECENT_USE_CARD_UITYPE = "common";
	/**电影卡片uitype*/
	public static final String BDMOVIE_CARD_UITYPE = "vertical";
	/**团购卡片uitype*/
	public static final String BD_GROUP_CARD_UITYPE = "horizontal_goods";
	/**热搜卡片uitype*/
	public static final String BD_HOT_WD_CARD_UITYPE = "hotsearch";
	
	/**热搜搜索卡片uitype*/
	public static final String BD_HOT_TIPS_CARD_UITYPE = "hotword";
	/**新闻卡片uitype*/
	public static final String INVNO_NEWS_CARD_UITYPE = "horizontal_message";
	
	public static final String APK_FILE_NAME = "toggleWithLeft.apk";
	/**检查更新升级时间*/
	public static final String KEY_UPGRADE_CHECK_TIME = "checkUpgradeTime";
	/**延迟检查更新升级时间*/
	public static final String KEY_DELAY_CHECK_TIME = "delayUpgradeTime";
	/**升级广播*/
	public static final String LAUNCHER_UPDATE = "com.android.launcher.update";
	/**升级广播*/
	public static final String KEY_MD5 = "md5";
	
	public static final String KEY_TID = "persist.sys.tid";
	
	public static final String APK_FILE_PATH = new StringBuilder(Environment
			.getExternalStorageDirectory().getAbsolutePath())
			.append(File.separator).append(Environment.DIRECTORY_DOWNLOADS)
			.append(File.separator).append(APK_FILE_NAME)
			.toString();
}
