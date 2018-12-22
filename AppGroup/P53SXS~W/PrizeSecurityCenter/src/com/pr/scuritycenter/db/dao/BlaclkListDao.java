package com.pr.scuritycenter.db.dao;

import java.util.ArrayList;
import java.util.List;

import tmsdk.common.module.aresengine.ContactEntity;
import tmsdk.common.module.aresengine.IContactDao;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.SystemClock;

import com.pr.scuritycenter.bean.BlackNumberInfo;
import com.pr.scuritycenter.db.BlackNumberDBHelper;

public class BlaclkListDao implements IContactDao<ContactEntity> {

	private static BlaclkListDao mBlaclkListDao;
	private BlackNumberDBHelper helper;
	private static Context context;

	public BlaclkListDao(Context context) {
		super();
		helper = new BlackNumberDBHelper(context);
	}

	public static BlaclkListDao getInstance() {
		if (null == mBlaclkListDao) {
			mBlaclkListDao = new BlaclkListDao(context);
		}
		return mBlaclkListDao;
	}

	/**
	 * 添加到黑名单数据库的方法
	 * 
	 * @return
	 */
	public boolean add(String number, String mode) {

		SQLiteDatabase db = helper.getWritableDatabase();

		ContentValues values = new ContentValues();

		values.put("number", number);

		values.put("mode", mode);

		long rowid = db.insert("blackinfo", null, values);

		if (rowid == -1) {
			return false;
		} else {
			return true;
		}

	}

	/**
	 * 根据电话号码删除黑名单的数据
	 * 
	 * @param number
	 *            电话号码
	 * @return
	 */
	public boolean delete(String number) {

		SQLiteDatabase db = helper.getWritableDatabase();

		int rowid = db.delete("blackinfo", "number = ?",
				new String[] { number });

		if (rowid == 0) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * 根据电话号码修改拦截的模式
	 * 
	 * @return
	 */
	public boolean changeNumberMode(String number, String newmode) {

		SQLiteDatabase db = helper.getWritableDatabase();

		ContentValues values = new ContentValues();

		values.put("mode", newmode);

		int rowid = db.update("blackinfo", values, "number = ?",
				new String[] { number });

		if (rowid == 0) {
			return false;
		} else {
			return true;
		}

	}

	/**
	 * 根据电话号码查询拦截的模式
	 * 
	 * @param number
	 * @return
	 */
	public String findNumberMode(String number) {
		String mode = "0";
		SQLiteDatabase db = helper.getReadableDatabase();
		// 返回一个游标
		Cursor cursor = db.query("blackinfo", new String[] { "mode" },
				"number = ?", new String[] { number }, null, null, null);
		// 判断当前的游标能否向后面移动
		if (cursor.moveToNext()) {
			mode = cursor.getString(0);
		}
		cursor.close();
		db.close();
		return mode;
	}

	/**
	 * 返回所有的黑名单
	 * 
	 * @return
	 */

	public List<BlackNumberInfo> findAll() {

		SQLiteDatabase db = helper.getReadableDatabase();

		Cursor cursor = db
				.query("blackinfo", new String[] { "number", "mode" }, null,
						null, null, null, null);
		// 创建一个集合用来存放黑名单
		List<BlackNumberInfo> lists = new ArrayList<BlackNumberInfo>();
		while (cursor.moveToNext()) {
			// 初始化黑名单的对象
			BlackNumberInfo info = new BlackNumberInfo();
			info.setNumber(cursor.getString(0));
			info.setMode(cursor.getString(1));
			lists.add(info);
		}
		SystemClock.sleep(2000);
		cursor.close();
		db.close();
		return lists;
	}

	/**
	 * 分页加载数据
	 * 
	 * @param pageSize
	 *            每一页展示的数据条目
	 * @param pageNumber
	 *            从哪一条数据开始
	 * @return 返回一个黑名单的集合数据
	 */
	public List<BlackNumberInfo> findPage(int pageSize, int pageNumber) {

		SQLiteDatabase db = helper.getReadableDatabase();

		Cursor cursor = db.rawQuery(
				"select number,mode from blackinfo limit ? offset ?",
				new String[] { String.valueOf(pageSize),
						String.valueOf(pageSize * pageNumber) });
		// 初始化黑名单的集合
		ArrayList<BlackNumberInfo> lists = new ArrayList<BlackNumberInfo>();

		while (cursor.moveToNext()) {
			BlackNumberInfo info = new BlackNumberInfo();
			info.setMode(cursor.getString(1));
			info.setNumber(cursor.getString(0));
			lists.add(info);
		}
		cursor.close();
		db.close();
		return lists;
	}

	/**
	 * 分批加载数据
	 * 
	 * @param startIndex
	 *            开始数据的条目
	 * @param pageCount
	 *            每页最多加载多少条数据
	 * @return 返回一个黑名单的集合数据
	 */
	public List<BlackNumberInfo> findPage2(int startIndex, int pageCount) {

		SQLiteDatabase db = helper.getReadableDatabase();

		Cursor cursor = db
				.rawQuery(
						"select number,mode from blackinfo order by _id desc  limit ? offset ?",
						new String[] { String.valueOf(pageCount),
								String.valueOf(startIndex) });
		// 初始化黑名单的集合
		ArrayList<BlackNumberInfo> lists = new ArrayList<BlackNumberInfo>();

		while (cursor.moveToNext()) {
			BlackNumberInfo info = new BlackNumberInfo();
			info.setMode(cursor.getString(1));
			info.setNumber(cursor.getString(0));
			lists.add(info);
		}
		cursor.close();
		db.close();
		return lists;
	}

	/**
	 * 返回所有数据的总数
	 * 
	 * @return
	 */
	public int getCountTotal() {
		String count = "";
		SQLiteDatabase db = helper.getReadableDatabase();
		Cursor cursor = db.rawQuery("select count(*) from blackinfo", null);
		if (cursor.moveToNext()) {
			count = cursor.getString(0);
		}

		cursor.close();
		return Integer.parseInt(count);
	}

	@Override
	public boolean contains(String phonenum, int flags) {
		// 不同的比较方式，可以达到不同的效果，比如可以定义前缀匹配等，都可以根据具体的业务需要实现
		// 这里以后8位来匹配
		//return BlackNumberDBHelper.contains(contactList, phonenum, callfrom);
		return false;
	}

}
