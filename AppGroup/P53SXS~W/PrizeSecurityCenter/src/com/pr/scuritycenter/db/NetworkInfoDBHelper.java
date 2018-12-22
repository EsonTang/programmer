package com.pr.scuritycenter.db;

import tmsdk.common.module.network.NetworkInfoEntity;
import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

public class NetworkInfoDBHelper extends OrmLiteSqliteOpenHelper {

	private static final String TABLE_NAME = "network_info.db";
	/**
	 * networkInfoDao ，每张表对于一个
	 */
	private Dao<NetworkInfoEntity, Integer> networkInfoDao;

	public NetworkInfoDBHelper(Context context) {
		super(context, TABLE_NAME, null, 2);
	}

	@Override
	public void onCreate(SQLiteDatabase database,
			ConnectionSource connectionSource) {
		try {
			TableUtils.createTable(connectionSource, NetworkInfoEntity.class);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase database,
			ConnectionSource connectionSource, int oldVersion, int newVersion) {
		try {
			TableUtils.dropTable(connectionSource, NetworkInfoEntity.class,
					true);
			onCreate(database, connectionSource);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static NetworkInfoDBHelper instance;

	/**
	 * 单例获取该Helper
	 * 
	 * @param context
	 * @return
	 */
	public static synchronized NetworkInfoDBHelper getHelper(Context context) {
		if (instance == null) {
			synchronized (NetworkInfoDBHelper.class) {
				if (instance == null)
					instance = new NetworkInfoDBHelper(context);
			}
		}

		return instance;
	}

	/**
	 * 获得networkInfoDao
	 * 
	 * @return
	 * @throws SQLException
	 * @throws java.sql.SQLException
	 */
	public Dao<NetworkInfoEntity, Integer> getNetworkInfoDao()
			throws java.sql.SQLException {
		if (networkInfoDao == null) {
			networkInfoDao = getDao(NetworkInfoEntity.class);
		}
		return networkInfoDao;
	}

	/**
	 * 释放资源
	 */
	@Override
	public void close() {
		super.close();
		networkInfoDao = null;
	}
}
