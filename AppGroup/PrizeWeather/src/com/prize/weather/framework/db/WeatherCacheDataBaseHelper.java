package com.prize.weather.framework.db;

import java.sql.SQLException;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.prize.weather.framework.model.WeatherDBCache;

/**
 * 
 * @author wangzhong
 *
 */
public class WeatherCacheDataBaseHelper extends OrmLiteSqliteOpenHelper {

	private final static int DB_VERSION = 1;
	private final static String DB_NAME = "weather_data.db";
	private static WeatherCacheDataBaseHelper mWeatherCacheDataBaseHelper;
	
	private WeatherCacheDataBaseHelper(Context context, String databaseName,
			CursorFactory factory, int databaseVersion) {
		super(context, databaseName, factory, databaseVersion);
	}

	public static WeatherCacheDataBaseHelper getInstance(Context context) {
		if (null == mWeatherCacheDataBaseHelper) {
			mWeatherCacheDataBaseHelper = new WeatherCacheDataBaseHelper(context, DB_NAME, null, DB_VERSION);
		}
		return mWeatherCacheDataBaseHelper;
	}
	
	@Override
	public void onCreate(SQLiteDatabase arg0, ConnectionSource arg1) {
		try {
			TableUtils.createTableIfNotExists(connectionSource, WeatherDBCache.class);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase arg0, ConnectionSource arg1, int arg2,
			int arg3) {
		
	}

}
