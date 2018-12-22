/*******************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：
 *当前版本：
 *作	者：
 *完成日期：
 *修改记录：
 *修改日期：
 *版 本 号：
 *修 改 人：
 *修改内容：
...
 *修改记录：
 *修改日期：
 *版 本 号：
 *修 改 人：
 *修改内容：
 *********************************************/

package com.prize.app.database;

import android.provider.BaseColumns;

/**
 * 类描述：已下载任务表格信息
 * 
 * @author huanglinjun
 * @version 版本
 */
public class DownLoadedTable {
	/* download game info 表名 */
	public static final String TABLE_NAME_D0WNLOADED = "table_downLoaded";

	public static final String ID = BaseColumns._ID;
	public static final String GAME_APPID = "appId";
	public static final String GAME_PACKAGE = "packageName";
	public static final String GAME_NAME = "name";
	public static final String GAME_ICON_URL = "iconUrl"; // 游戏显示小图标
	public static final String GAME_VERSION_CODE = "versionCode";
	public static final String GAME_APK_SIZE = "apkSize";
	public static final String GAME_APK_URL = "downloadUrl";
	/**2.5 add 下载apk的versionName*/
	public static final String GAME_APK_VERSIONNAME = "versionName";
	/**2.5 add 下载apk的完成时间*/
	public static final String APK_DOWNLOADED_STAMP = "downloadTime";
	/**2.5 add 下载apk是更新还是安装*/
	public static final String APK_INSTALL_TYPE = "installType";
	/**2.9 add 下载apk所属界面*/
	public static final String APK_PAGEINFO = "pageInfo";

	public static final String SQL_DELETE_GAME_TABLE = PrizeDatabaseHelper.SQL_DELETE_TABLE
			+ TABLE_NAME_D0WNLOADED;

	public static final String SQL_CREATE_DOWNLOADED_TABLE = PrizeDatabaseHelper.SQL_CREATE_TABLE
			+ TABLE_NAME_D0WNLOADED
			+ "("
			+ ID
			+ " INTEGER primary key AUTOINCREMENT,"
			+ GAME_APPID
			+ " INTEGER,"
			+ GAME_PACKAGE
			+ " TEXT UNIQUE,"
			+ GAME_ICON_URL
			+ " TEXT,"
			+ GAME_NAME
			+ " TEXT,"
			+ GAME_VERSION_CODE
			+ " INTEGER,"
			+ GAME_APK_SIZE
			+ " INTEGER,"
			+ GAME_APK_URL
			+ " TEXT,"
			+ GAME_APK_VERSIONNAME
			+ " TEXT,"
			+ APK_DOWNLOADED_STAMP
			+ " TEXT,"
			+ APK_INSTALL_TYPE
			+ " TEXT,"
			+ APK_PAGEINFO + " TEXT" + ")";

	/** 已下载table字段名数组*/
	public static final String DOWNLOADED_COLUMNS[] = new String[] {
			DownLoadedTable.GAME_APPID, DownLoadedTable.GAME_PACKAGE,
			DownLoadedTable.GAME_NAME, DownLoadedTable.GAME_ICON_URL,
			DownLoadedTable.GAME_VERSION_CODE,
			DownLoadedTable.GAME_APK_SIZE, DownLoadedTable.GAME_APK_URL,DownLoadedTable.GAME_APK_VERSIONNAME,
			DownLoadedTable.APK_DOWNLOADED_STAMP,DownLoadedTable.APK_INSTALL_TYPE,DownLoadedTable.APK_PAGEINFO};
}
