
 /*******************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：realize to find the path which Uninstall apps' residual info
 *当前版本：V 1.0
 *作	者：Bianxinhao
 *完成日期：2015-4-3
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

package com.pr.scuritycenter.db.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class CleanPathDao {

	private static final String path = "data/data/com.android.ScurityCenter/files/clearpath.db";
	
	
	
	public static String find(String Filepath){
		String apkname = null;
		SQLiteDatabase db = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READONLY);
		
		Cursor  cursor = db.rawQuery("select apkname from softdetail where filepath=?", new String[]{Filepath});
		
		if(cursor.moveToNext()){
			apkname = cursor.getString(0);
		}
		
		cursor.close();
		db.close();
		return apkname;
	}
	
	
	
}

