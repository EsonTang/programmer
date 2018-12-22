package com.koobee.koobeecenter.db.outlets;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.koobee.koobeecenter.db.TableBase;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by yiyi on 2015/5/20.
 */
public class CustomerTable extends TableBase {

	public static class Info {
		public int id;
		public String province;
		public String area;
		public String company;
		public String address;
		public String tel;
		
		//public String consignee;
		//public String level;
		//public String head;
		//public String category;
	}

	// id
	public static final String COLUMN_ID = "_id";
	// ʡ
	public static final String COLUMN_PROVINCE = "province";
	// ����
	public static final String COLUMN_AREA = "area";
	// ����
	public static final String COLUMN_LEVEL = "level";
	// ��˾���
	public static final String COLUMN_COMPANY = "company";
	// ��˾��ַ
	public static final String COLUMN_ADDRESS = "address";
	// �ջ���
	public static final String COLUMN_CONSIGNEE = "consignee";
	// ��ϵ�绰
	public static final String COLUMN_TEL = "tel";
	// ������
	public static final String COLUMN_HEAD = "head";
	// ���
	public static final String COLUMN_CATEGORY = "category";

	private final static String TABLENAME = "CUSTOMER";

	private static Info getInfo(List<String> columns, Cursor cursor) {
		Info info = new Info();
		info.id = getValueInt(columns, cursor, COLUMN_ID);
		info.province = getValueString(columns, cursor, COLUMN_PROVINCE);
		info.area = getValueString(columns, cursor, COLUMN_AREA);
		info.company = getValueString(columns, cursor, COLUMN_COMPANY);
		info.address = getValueString(columns, cursor, COLUMN_ADDRESS);
		info.tel = getValueString(columns, cursor, COLUMN_TEL);
		
		//info.consignee = getValueString(columns, cursor, COLUMN_CONSIGNEE);
		//info.level = getValueString(columns, cursor, COLUMN_LEVEL);
		//info.head = getValueString(columns, cursor, COLUMN_HEAD);
		//info.category = getValueString(columns, cursor, COLUMN_CATEGORY);
		return info;
	}

	/**
	 * ��ȡ������ݣ�����idʡ�ݵ������
	 *
	 * @param db
	 * @return
	 */
	public static List<Info> queryCitys(SQLiteDatabase db) {
		List<String> columns = new ArrayList<String>();
		columns.add(COLUMN_ID);
		columns.add(COLUMN_PROVINCE);
		columns.add(COLUMN_AREA);
		Cursor cursor = null;
		try {
			cursor = db.query(TABLENAME, columns.toArray(new String[] {}),
					null, null, null, null, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
		List<Info> list = new LinkedList<Info>();
		if (cursor != null && cursor.getCount() > 0) {
			cursor.moveToFirst();
			Info info = getInfo(columns, cursor);
			list.add(info);
			while (cursor.moveToNext()) {
				info = getInfo(columns, cursor);
				list.add(info);
			}
		}
		if (cursor != null)
			cursor.close();
		return list;
	}

	public static List<Info> queryByArea(SQLiteDatabase db, String area) {
		List<String> columns = new LinkedList<String>();
		columns.add(COLUMN_COMPANY);
		columns.add(COLUMN_ADDRESS);
		//columns.add(COLUMN_CONSIGNEE);
		columns.add(COLUMN_TEL);
		//columns.add(COLUMN_HEAD);
		//columns.add(COLUMN_CATEGORY);
		Cursor cursor = queryInfo(db, COLUMN_AREA, area, columns, TABLENAME);
		List<Info> list = new LinkedList<Info>();
		if (cursor != null && cursor.getCount() > 0) {
			cursor.moveToFirst();
			Info info = getInfo(columns, cursor);
			list.add(info);
			while (cursor.moveToNext()) {
				info = getInfo(columns, cursor);
				list.add(info);
			}
		}
		if (cursor != null)
			cursor.close();
		return list;
	}

}
