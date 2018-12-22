package com.goodix.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class FpDbOperarionImpl {
	private static FpDbOperarionImpl instance;
	private Context mContext;
	private FpDBOpenHelper mHelper;
	private SQLiteDatabase mWriteDb;
	private SQLiteDatabase mReadDb;

	public static FpDbOperarionImpl getInstance(Context context){
		if(null == instance){
			instance = new FpDbOperarionImpl(context);
		}
		return instance;
	}

	private FpDbOperarionImpl(Context context) {
		mContext = context;
		setDaoObject();
	}

	private void setDaoObject(){
		if(null == mHelper){
			mHelper = FpDBOpenHelper.getInstance(mContext);
			mWriteDb = mHelper.getWritableDatabase();
			mReadDb = mHelper.getReadableDatabase();
		}
	}

	private void openWriteDb(){
		if(!mWriteDb.isOpen()){
			mWriteDb = mHelper.getWritableDatabase();
		}
	}

	private void openReadDb(){
		if(!mReadDb.isOpen()){
			mReadDb = mHelper.getReadableDatabase();
		}
	}

	/**
	 * 方法描述：添加数据至tableName表
	 * @param FpInfo
	 * @return int
	 * @see FpDbOperarionImpl#insert
	 */
	public int insert(String tableName,ContentValues values){
		synchronized(tableName){
			long flag = -1;
			setDaoObject();
			openWriteDb();
			flag = mWriteDb.insert(tableName, null, values);
			if(flag == -1l){
				return -1;
			}
			return 0;
		}
	}

	/**
	 * 方法描述：查询tableName的数据
	 * @param int
	 * @return Cursor
	 * @see FpDbOperarionImpl#queryForId
	 */
	public Cursor query(String tableName,String[] columns,String selection,String[] selectionArgs,String sortOrder){
		synchronized(tableName){
			setDaoObject();
			openReadDb();
			Cursor cursor = mReadDb.query(tableName, columns, selection, selectionArgs, null, null, sortOrder);
			return cursor;
		}
	}

	/**
	 * 方法描述：删除tableName的数据
	 * @param int
	 * @return int
	 * @see FpDbOperarionImpl#deleteById
	 */
	public int delete(String tableName,String selection,String[] selectionArgs){
		synchronized(tableName){
			int flag = -1;
			setDaoObject();
			openWriteDb();
			flag = mWriteDb.delete(tableName, selection, selectionArgs);
			return flag;
		}
	}

	/**
	 * 方法描述：更新某tableName数据
	 * @param FpInfo
	 * @return int
	 * @see FpDbOperarionImpl#updateById
	 */
	public int update(String tableName,String selection,String[] selectionArgs,ContentValues values){
		synchronized(tableName){
			int flag = -1;
			setDaoObject();
			openWriteDb();
			flag = mWriteDb.update(tableName, values, selection, selectionArgs);
			return flag;
		}
	}

	/**
	 * 方法描述：删除tableName表,并新建该表
	 * @param 
	 * @return void
	 * @see FpDbOperarionImpl#deleteAllData
	 */
	public void deleteAllData(String tableName){
		synchronized(tableName){
			setDaoObject();
			openWriteDb();
			mHelper.deleteTableByName(mWriteDb,tableName);
			mHelper.createTable(mWriteDb,tableName);
		}
	}
}
