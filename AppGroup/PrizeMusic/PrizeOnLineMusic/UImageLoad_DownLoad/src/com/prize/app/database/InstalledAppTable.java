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
 * 类描述：存放系统中已经安装的应用表
 * @author fanjunchen
 */
public class InstalledAppTable {
	/** 已经安装的应用 */
	public static final String TABLE_NAME = "t_intalled_app";

	public static final String ID = BaseColumns._ID;
	/**包名**/
	public static final String PKG_NAME = "pkgName";
	/**版本号**/
	public static final String VERSION_CODE = "verCode";
	/**扩展字段**/
	public static final String ATTR1 = "attr1";
	/**扩展字段**/
	public static final String ATTR2 = "attr2";
	/**扩展字段**/
	public static final String ATTR3 = "attr3";

	public static final String SQL_DELETE = PrizeDatabaseHelper.SQL_DELETE_TABLE
			+ TABLE_NAME;
	
	public static final String SQL_CREATE = PrizeDatabaseHelper.SQL_CREATE_TABLE
			+ TABLE_NAME
			+ "("
			+ ID + " INTEGER primary key AUTOINCREMENT,"
			+ PKG_NAME + " TEXT,"
			+ VERSION_CODE + " INTEGER,"
			+ ATTR1	+ " TEXT,"
			+ ATTR2	+ " TEXT,"
			+ ATTR3	+ " TEXT)";
	
	public static final String DOWNLOADED_COLUMNS[] = new String[] {
			InstalledAppTable.PKG_NAME, InstalledAppTable.VERSION_CODE,
			InstalledAppTable.ATTR1, InstalledAppTable.ATTR2, InstalledAppTable.ATTR3 };
}
