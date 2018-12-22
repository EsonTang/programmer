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
 * 类描述：账号表格信息
 * 
 * @author huanglinjun
 * @version 版本
 */
public class AccountTable {
	/* download game info 表名 */
	public static final String TABLE_NAME_ACCOUNT = "table_account";

	public static final String ID = BaseColumns._ID;
	public static final String ACCOUNT_LOGINNAME = "loginName";
	public static final String ACCOUNT_PASSWORD = "password";
	public static final String ACCOUNT_PASSPORT = "passport";

	public static final String SQL_DELETE_ACCOUNT_TABLE = PrizeDatabaseHelper.SQL_DELETE_TABLE
			+ TABLE_NAME_ACCOUNT;

	public static final String SQL_CREATE_ACCOUNT_TABLE = PrizeDatabaseHelper.SQL_CREATE_TABLE
			+ TABLE_NAME_ACCOUNT
			+ "("
			+ ID
			+ " INTEGER primary key AUTOINCREMENT,"
			+ ACCOUNT_LOGINNAME
			+ " TEXT,"
			+ ACCOUNT_PASSWORD
			+ " TEXT,"
			+ ACCOUNT_PASSPORT
			+ " TEXT" + ")";
	
	/** 已下载table字段名数组*/
	public static final String ACCOUNT_COLUMNS[] = new String[] {
		AccountTable.ACCOUNT_LOGINNAME, AccountTable.ACCOUNT_PASSWORD,
		AccountTable.ACCOUNT_PASSPORT};
}
