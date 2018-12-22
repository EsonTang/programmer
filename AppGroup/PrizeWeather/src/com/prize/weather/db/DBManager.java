
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

import java.util.ArrayList;
import java.util.List;

import com.prize.weather.city.CityEntity;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 **
 * 类描述：
 * @author 作者
 * @version 版本
 */
public class DBManager {
	public static boolean isUser = false;
//	private DBHelper helper;
//	private SQLiteDatabase db;

	AssetsDatabaseManager mg;
	SQLiteDatabase db;
	 /**
	 * 方法描述：
	 * @param 参数名 说明
	 * @return 返回类型 说明
	 * @see 类名/完整类名/完整类名#方法名
	 */
	
	public DBManager(Context context) {
//		helper = new DBHelper(context);
//		db = helper.getWritableDatabase();
		executCity(context);
	}

	private void executCity(Context context) {
		// 初始化，只需要调用一次  
		AssetsDatabaseManager.initManager(context.getApplicationContext());  
		// 获取管理对象，因为数据库需要通过管理对象才能够获取  
		mg = AssetsDatabaseManager.getManager();  
		// 通过管理对象获取数据库  
		db = mg.getDatabase("city.db");  
		if(db == null){
			isUser = true;
		}else{
			isUser = false;
		}
	}
	
	/*public List<CityEntity> query(){
		ArrayList<CityEntity> citys = new ArrayList<CityEntity>();
		Cursor c = queryTheCursor();
		while(c.moveToNext()){
			CityEntity city = new CityEntity();
			city.id = c.getInt(c.getColumnIndex("ID"));
			city.pid = c.getInt(c.getColumnIndex("PID"));
			city.cCode = c.getInt(c.getColumnIndex("c_code"));
			city.dCode = c.getInt(c.getColumnIndex("d_code"));
			city.type = c.getInt(c.getColumnIndex("type"));
			city.name = c.getString(c.getColumnIndex("name"));
			city.isEnable = c.getInt(c.getColumnIndex("isenable"));
			city.isHot = c.getInt(c.getColumnIndex("ishot"));
			citys.add(city);
		}
		c.close();
		return citys;
		
	}*/
	
	public List<CityEntity> queryAllCitys(){
		ArrayList<CityEntity> citys= new ArrayList<CityEntity>();
		if(null != db){
			Cursor c = db.rawQuery("SELECT * FROM province where c_code!=0", null);
			citys.clear();
			citys = (ArrayList<CityEntity>) parseEntityData(c);
			c.close();			
		}
		return citys;
	}
	
	public List<CityEntity> queryHotCity(){
		ArrayList<CityEntity> citys= new ArrayList<CityEntity>();
		if(null != db){
			Cursor c = db.rawQuery("SELECT * FROM province where ishot=1", null);
			citys.clear();
			citys = (ArrayList<CityEntity>) parseEntityData(c);
			c.close();			
		}
		return citys;
	}
	
	public List<CityEntity> queryProvinces(){
		ArrayList<CityEntity> citys= new ArrayList<CityEntity>();
		if(null != db){
			Cursor c = db.rawQuery("SELECT * FROM province where type=1 or type=0", null);
			citys.clear();
			citys = (ArrayList<CityEntity>) parseEntityData(c);
			c.close();			
		}
		return citys;
	}
	
	public List<CityEntity> queryCity(int provDcode){
		Log.d("hekeyi","provDcode = "+provDcode);
		ArrayList<CityEntity> citys= new ArrayList<CityEntity>();
		if(null != db){
			Cursor c = db.rawQuery("SELECT * FROM province where PID="+provDcode, null);
			citys.clear();
			citys = (ArrayList<CityEntity>) parseEntityData(c);
			c.close();			
		}
		return citys;
	}
	
	private List<CityEntity> parseEntityData(Cursor c) {		
		ArrayList<CityEntity> citys= new ArrayList<CityEntity>();
		while(c.moveToNext()){
			CityEntity city = new CityEntity();
			city.id = c.getInt(c.getColumnIndex("ID"));
			city.pid = c.getInt(c.getColumnIndex("PID"));
			city.cCode = c.getInt(c.getColumnIndex("c_code"));
			city.dCode = c.getInt(c.getColumnIndex("d_code"));
			city.type = c.getInt(c.getColumnIndex("type"));
			city.name = c.getString(c.getColumnIndex("name"));
			city.isEnable = c.getInt(c.getColumnIndex("isenable"));
			city.isHot = c.getInt(c.getColumnIndex("ishot"));
			citys.add(city);
		}
		c.close();
		return citys;
	}

	/** 
     * query all persons, return cursor 
     * @return  Cursor 
     */  
    public Cursor queryTheCursor() {  
        Cursor c = db.rawQuery("SELECT * FROM province where ishot=1", null);  
        return c;  
    }  
    
    public void closeDB(){
    	db.close();
    }
    
}
