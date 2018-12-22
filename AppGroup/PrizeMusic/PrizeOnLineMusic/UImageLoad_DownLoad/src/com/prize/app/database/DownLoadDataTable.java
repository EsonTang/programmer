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
 * 类描述：统计下载数据表格
 * 
 * @author huanglinjun
 * @version 版本
 */
public class DownLoadDataTable {
	/* download game info 表名 */
	public static final String TABLE_NAME_DOWNLOAD_DATA = "table_download_data";

	public static final String ID = BaseColumns._ID;
	public static final String DOWNLOADTYPE = "downloadType";
	public static final String PACKAGENAME = "packageName";
	public static final String  TIMEDALTA= "timeDelta";

	public static final String SQL_DELETE_DOWNLOADDATA_TABLE = PrizeDatabaseHelper.SQL_DELETE_TABLE
			+ TABLE_NAME_DOWNLOAD_DATA;

	public static final String SQL_CREATE_DOWNLOADDATA_TABLE = PrizeDatabaseHelper.SQL_CREATE_TABLE
			+ TABLE_NAME_DOWNLOAD_DATA
			+ "("
			+ ID
			+ " INTEGER primary key AUTOINCREMENT,"
			+ DOWNLOADTYPE
			+ " TEXT,"
			+ PACKAGENAME
			+ " TEXT,"
			+ TIMEDALTA
			+ " TEXT" + ")";
	
	/** 已下载table字段名数组*/
	public static final String ACCOUNT_COLUMNS[] = new String[] {
		DownLoadDataTable.DOWNLOADTYPE, DownLoadDataTable.PACKAGENAME, DownLoadDataTable.TIMEDALTA};
}
