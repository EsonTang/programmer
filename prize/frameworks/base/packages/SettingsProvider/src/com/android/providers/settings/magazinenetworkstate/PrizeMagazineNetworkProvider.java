package com.android.providers.settings.magazinenetworkstate;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

public class PrizeMagazineNetworkProvider extends ContentProvider{
	public static final Uri URl = Uri.parse("content://com.android.settings.wallpaper.PrizeMagazineNetworkProvider");
	private SQLiteDatabase sqLiteDatabase;
	private int isTable = 0;

	@Override
	public int delete(Uri arg0, String arg1, String[] arg2) {
		return 0;
	}

	@Override
	public String getType(Uri arg0) {
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		sqLiteDatabase.insert("networkstate", "_id", values);
		getContext().getContentResolver().notifyChange(uri, null);
		return null;
	}

	@Override
	public boolean onCreate() {
		
		sqLiteDatabase = getContext().openOrCreateDatabase("magazinenetwork.db", Context.MODE_PRIVATE, null);
		Cursor cursor = sqLiteDatabase.rawQuery("select name from sqlite_master where type='table';", null);
		cursor.moveToFirst();
		while(cursor.moveToNext()){
			String name = cursor.getString(0);
			if(name.equals("networkstate")){
				isTable = 1;
			}
		}
		cursor.close();
		if(isTable == 0){
			sqLiteDatabase.execSQL("create table networkstate(_id INTEGER PRIMARY KEY AUTOINCREMENT,state INTEGER,time INTEGER,isnewphoto INTEGER)");
		}
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] arg1, String arg2, String[] arg3,
			String arg4) {
		Cursor cursor = sqLiteDatabase.query("networkstate",  null, null, null, null, null, null);
		return cursor;
	}

	@Override
	public int update(Uri uri, ContentValues values, String whereClause, String[] whereArgs) {
		Log.i("wallpaper", "SettingProvider PrizeMagazineProvider update!!!");
		int result = sqLiteDatabase.update("networkstate", values, "_id=?", new String[]{"1"});
		getContext().getContentResolver().notifyChange(uri, null);
		return result;
	}

}
