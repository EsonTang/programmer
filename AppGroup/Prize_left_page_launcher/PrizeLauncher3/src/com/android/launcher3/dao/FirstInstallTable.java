package com.android.launcher3.dao;

import android.provider.BaseColumns;

/**
 ** 数据库 首页表的字段及其他信息
 * 
 * @author longbaoxiu
 * @version V1.0
 */
public class FirstInstallTable {
	/** 表名 */
	public static final String TABLE_NAME = "new_install_table";

	public static final String ID = BaseColumns._ID;
	
	public static final String PACKAGE_NAME = "package_name";
	public static final String CLASS_NAME = "class_name";
	public static final String FIRST_INSTALL = "first_install";
	public static final String APP_HIDE="app_hide";

	/*
	 * "id": 18, "associateId": 0, "adType": "web", "title": "启动广告", "imageUrl":
	 * "http://cdnimages.oss-cn-hangzhou.aliyuncs.com/appstore/ad/启动广告_1447069709471_关于.png"
	 * , "url": "http://wefire.qq.com/", "description": "阿斯蒂芬", "createTime":
	 * "2015-11-09", "status": 1, "position": 3
	 */
	/***
	 * SQL语句
	 */
	public static final String DELETE_TABLE = "drop table if exists ";
	public static final String CREATE_TABLE = "create table if not exists ";

	/** HOME Table */
	public static final String DELETE_INSTALL_TABLE = CREATE_TABLE
			+ TABLE_NAME;
	/***
	 * 创建首页数据表Sqlite语句
	 */
	public static final String FIRST_INSTALL_TABLE = CREATE_TABLE
			+ TABLE_NAME
			+ "("
			+ ID
			+ " INTEGER primary key AUTOINCREMENT,"
			+ PACKAGE_NAME
			+ " TEXT,"
			+ FIRST_INSTALL
			+ " INTEGER,"
			+ APP_HIDE
			+ " INTEGER,"
			+ CLASS_NAME
			+ " TEXT" + ")";

}