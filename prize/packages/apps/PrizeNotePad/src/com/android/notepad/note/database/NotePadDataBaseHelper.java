
/*******************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：ORMLite框架操作数据库
 *当前版本：V1.0
 *作	者：朱道鹏
 *完成日期：2015-04-17
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

package com.android.notepad.note.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.android.notepad.note.model.NoteEvent;

/**
 **
 * 类描述：数据库操作辅助类
 * @author 朱道鹏
 * @version V1.0
 */
public class NotePadDataBaseHelper extends SQLiteOpenHelper{
	private final static int DATABASE_VERSION = 1;  
	private static final String DB_NAME = "note_pads.db";
	private static NotePadDataBaseHelper mNotePadDataBaseHelper; 
	
	private NotePadDataBaseHelper(Context context) {
		super(context, DB_NAME, null, DATABASE_VERSION);

	}

	 /**
	 * 方法描述：单例模式获得数据库辅助对象
	 * @param Context
	 * @return NotePadDataBaseHelper
	 * @see NotePadDataBaseHelper#getInstance
	 */
	public static NotePadDataBaseHelper getInstance(Context context) {  
		if (mNotePadDataBaseHelper == null) {  
			mNotePadDataBaseHelper = new NotePadDataBaseHelper(context);  
		}  
		return mNotePadDataBaseHelper; 
	} 

	@Override
	public void onCreate(SQLiteDatabase db) {
		String noteSql = "CREATE TABLE IF NOT EXISTS " + NoteEvent.TABLE_NAME + " (" 
				+ NoteEvent.ID+ " INTEGER PRIMARY KEY AUTOINCREMENT," + NoteEvent.CONTENTS+ " TEXT," 
				+ NoteEvent.CREATED_DATE + " LONG DEFAULT(0)," + NoteEvent.BG_COLOR + " INTEGER DEFAULT(0),"
				+ NoteEvent.FONT_SIZE + " INTEGER DEFAULT(0)," 
				+ NoteEvent.UUID + " TEXT)";
		db.execSQL(noteSql);
	}


	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS" + NoteEvent.TABLE_NAME);
	}

}

