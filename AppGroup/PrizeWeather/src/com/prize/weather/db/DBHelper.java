
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
/**
 *****************************************
 * 版权所有©2015,深圳市铂睿智恒科技有限公司
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
*********************************************
 */

package com.prize.weather.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 **
 * 类描述：
 * @author 作者
 * @version 版本
 */
public class DBHelper extends SQLiteOpenHelper{
	private static final String DATABASE_NAME = "city.db";  
    private static final int DATABASE_VERSION = 1;  
	
	 
	 /**
	 * 方法描述：
	 * @param 参数名 说明
	 * @return 返回类型 说明
	 * @see 类名/完整类名/完整类名#方法名
	 */
	
	public DBHelper(Context context) {		
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		
	}


	/**
	 * 方法描述：
	 * @param 参数名 说明
	 * @return 返回类型 说明
	 * @see 类名/完整类名/完整类名#方法名
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {		
		
	}

	
	 /**
	 * 方法描述：
	 * @param 参数名 说明
	 * @return 返回类型 说明
	 * @see 类名/完整类名/完整类名#方法名
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {		
		
	}
	
}

