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
 *********************************************/

package com.prize.app.database;

/**
 **
 * 搜索记录表
 * 
 * @author longbaoxiu
 * @version V1.0
 */
public class SearchHistory {
	public static final String TABLE_NAME = "table_history";
	/** 搜索 关键字 */
	public static final String HISTORY_TITLE = "title";
	public static final String ID = "_id";
	/** 插入时间 */
	public static final String TIMESTAMP = "timestamp";
	/** table_history */
	public static final String SQL_DELETE_TABLE = PrizeDatabaseHelper.SQL_DELETE_TABLE
			+ TABLE_NAME;

	public static final String SQL_CREATE_GAME_TABLE = PrizeDatabaseHelper.SQL_CREATE_TABLE
			+ SearchHistory.TABLE_NAME
			+ " ( "
			+ SearchHistory.ID
			+ " INTEGER PRIMARY KEY AUTOINCREMENT,"
			+ SearchHistory.HISTORY_TITLE
			+ " VARCHAR(50), "
			+ SearchHistory.TIMESTAMP + " LONG); ";

}
