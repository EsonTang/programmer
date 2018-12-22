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
 * 类描述：push表格
 * 
 * @author huanglinjun
 * @version 版本
 */
public class PushTable {
	/* push info 表名 */
	public static final String TABLE_NAME_PUSH = "table_push";

	public static final String ID = BaseColumns._ID;
	public static final String APP_PACKAGERNAME = "packagename";
	public static final String APP_VERSION = "version";

	public static final String SQL_DELETE_PUSH_TABLE = PrizeDatabaseHelper.SQL_DELETE_TABLE
			+ TABLE_NAME_PUSH;

	public static final String SQL_CREATE_PUSH_TABLE = PrizeDatabaseHelper.SQL_CREATE_TABLE
			+ TABLE_NAME_PUSH
			+ "("
			+ ID
			+ " INTEGER primary key AUTOINCREMENT,"
			+ APP_PACKAGERNAME
			+ " TEXT,"
			+ APP_VERSION
			+ " INTEGER" + ")";
	
	/** 已下载table字段名数组*/
	public static final String PUSH_COLUMNS[] = new String[] {
		PushTable.APP_PACKAGERNAME, PushTable.APP_VERSION};
}
