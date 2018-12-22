/***
 * function:watermark
 * author: wanzhijuan
 * date:   2016-1-21
 */
package com.prize.sticker.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

	// database name
	public static final String DATABASE_NAME = "p_watermark.db";
    // database version
    private static final int DATABASE_VERSION = 1;
	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);  
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE  IF NOT EXISTS " + Provider.AlbumColumns.TABLE_NAME + " ("  
                + Provider.AlbumColumns._ID + " INTEGER PRIMARY KEY,"  
                + Provider.AlbumColumns.APP_TYPE + " INTEGER,"  
                + Provider.AlbumColumns.NAME + " VARCHAR(12),"  
                + Provider.AlbumColumns.KEY + " TEXT"  
                + ");");  
          
        db.execSQL("CREATE TABLE IF NOT EXISTS " + Provider.WatermarkColumns.TABLE_NAME + " ("  
                + Provider.WatermarkColumns._ID + " INTEGER PRIMARY KEY,"  
                + Provider.WatermarkColumns.KEY_ID + " INTEGER,"  
                + Provider.WatermarkColumns.NAME + " VARCHAR(12),"  
                + Provider.WatermarkColumns.URL + " TEXT,"  
                + Provider.WatermarkColumns.ALBUM_ID + " INTEGER,"  
                + Provider.WatermarkColumns.THUMB_URL + " TEXT,"  
                + Provider.WatermarkColumns.THUMB_PATH + " TEXT,"  
                + Provider.WatermarkColumns.IS_STICKER + " BOOLEAN,"  
                + Provider.WatermarkColumns.WIDTH + " INTEGER,"  
                + Provider.WatermarkColumns.HEIGHT + " INTEGER,"  
                + Provider.WatermarkColumns.IS_COLOR_CHANGE + " BOOLEANm"  
                + Provider.WatermarkColumns.X_SCREEN + " INTEGER,"  
                + Provider.WatermarkColumns.Y_SCREEN + " INTEGER"
                + ");");  
        
        db.execSQL("CREATE TABLE IF NOT EXISTS " + Provider.PhotoResColumns.TABLE_NAME + " ("  
                + Provider.PhotoResColumns._ID + " INTEGER PRIMARY KEY,"  
                + Provider.PhotoResColumns.RESOURCE_ID + " INTEGER,"  
                + Provider.PhotoResColumns.WATERMARK_ID + " INTEGER,"  
                + Provider.PhotoResColumns.RES_PATH + " TEXT,"  
                + Provider.PhotoResColumns.X + " FLOAT,"  
                + Provider.PhotoResColumns.Y + " FLOAT,"  
                + Provider.PhotoResColumns.ABSOLUTE + " BOOLEAN,"  
                + Provider.PhotoResColumns.RELATIVE_ID + " INTEGER,"  
                + Provider.PhotoResColumns.URL + " TEXT"  
                + ");");  
        
        db.execSQL("CREATE TABLE IF NOT EXISTS " + Provider.WordResColumns.TABLE_NAME + " ("  
                + Provider.WordResColumns._ID + " INTEGER PRIMARY KEY,"  
                + Provider.WordResColumns.RESOURCE_ID + " INTEGER,"  
                + Provider.WordResColumns.WATERMARK_ID + " INTEGER,"  
                + Provider.WordResColumns.WORD + " TEXT,"  
                + Provider.WordResColumns.TEXT_SIZE + " INTEGER,"  
                + Provider.WordResColumns.IS_TEXT_VERTICAL + " BOOLEAN,"  
                + Provider.WordResColumns.TEXT_COLOR + " TEXT,"  
                + Provider.WordResColumns.TYPE + " INTEGER,"  
                + Provider.WordResColumns.X + " FLOAT,"  
                + Provider.WordResColumns.Y + " FLOAT,"  
                + Provider.WordResColumns.ALIGN + " INTEGER,"  
                + Provider.WordResColumns.LIMIT + " INTEGER,"  
                + Provider.WordResColumns.TIME_FORMAT + " TEXT,"  
                + Provider.WordResColumns.FONT + " TEXT"  
                + ");");  
        
        db.execSQL("CREATE TABLE IF NOT EXISTS " + Provider.HistoryColumns.TABLE_NAME + " ("  
                + Provider.HistoryColumns._ID + " INTEGER PRIMARY KEY,"  
                + Provider.HistoryColumns.APP_TYPE + " INTEGER,"  
                + Provider.HistoryColumns.WATERMARK_ID + " INTEGER,"  
                + Provider.HistoryColumns.MODIFY_TIME + " DATETIME"  
                + ");");  
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + Provider.AlbumColumns.TABLE_NAME);  
        db.execSQL("DROP TABLE IF EXISTS " + Provider.WatermarkColumns.TABLE_NAME);  
        db.execSQL("DROP TABLE IF EXISTS " + Provider.PhotoResColumns.TABLE_NAME);  
        db.execSQL("DROP TABLE IF EXISTS " + Provider.WordResColumns.TABLE_NAME);  
        db.execSQL("DROP TABLE IF EXISTS " + Provider.HistoryColumns.TABLE_NAME);  
        onCreate(db); 
	}

}
