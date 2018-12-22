package com.pr.scuritycenter.db;

import java.util.List;

import tmsdk.common.module.aresengine.ContactEntity;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class BlackNumberDBHelper extends SQLiteOpenHelper{

	public BlackNumberDBHelper(Context context) {
		super(context, "callsafe.db", null, 1);
	}
    /**
     * 创建blackinfo这个表
     * number 黑名单电话号码
     * mode    黑名单拦截的模式
     */
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("create table blackinfo (_id integer primary key autoincrement,number varchar(20),mode varchar(2)) ");
		
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		
	}
	public static boolean contains(List<ContactEntity> contactList, String phonenum, int callfrom) {
		for (ContactEntity entity : contactList) {
			String pattern = entity.phonenum;
			if (pattern.length()>8)
				pattern = pattern.substring(pattern.length()-8);
			if (phonenum.endsWith(pattern)) {
				return true;
			}
		}
		return false;
	}
	

}
