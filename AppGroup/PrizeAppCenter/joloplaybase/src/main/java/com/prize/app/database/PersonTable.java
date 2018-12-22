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
 * 类描述：个人信息
 * 
 * @author huanglinjun
 * @version 版本
 */
public class PersonTable {
	/* download game info 表名 */
	public static final String TABLE_NAME_PERSON = "table_person";

	public static final String ID = BaseColumns._ID;
	public static final String PERSON_USERID = "userId";
	public static final String PERSON_AVATAR = "avatar";
	public static final String PERSON_PHONE = "phone";
	public static final String PERSON_EMAIL = "email"; // 游戏显示小图标
	public static final String PERSON_REALNAME = "realName";
	public static final String PERSON_SEX = "sex";

	public static final String SQL_DELETE_PERSON_TABLE = PrizeDatabaseHelper.SQL_DELETE_TABLE
			+ TABLE_NAME_PERSON;

	public static final String SQL_CREATE_PERSON_TABLE = PrizeDatabaseHelper.SQL_CREATE_TABLE
			+ TABLE_NAME_PERSON
			+ "("
			+ ID
			+ " INTEGER primary key AUTOINCREMENT,"
			+ PERSON_USERID
			+ " TEXT,"
			+ PERSON_AVATAR
			+ " TEXT,"
			+ PERSON_PHONE
			+ " TEXT,"
			+ PERSON_EMAIL
			+ " TEXT,"
			+ PERSON_REALNAME
			+ " TEXT,"
			+ PERSON_SEX + " INTEGER" + ")";
	
	public static final String PERSON_COLUMNS[] = new String[] {
		PersonTable.PERSON_USERID, PersonTable.PERSON_AVATAR,
		PersonTable.PERSON_PHONE, PersonTable.PERSON_EMAIL,
		PersonTable.PERSON_REALNAME,
		PersonTable.PERSON_SEX};
}
