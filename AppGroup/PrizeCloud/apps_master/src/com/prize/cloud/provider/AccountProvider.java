/*******************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：
 *当前版本：1.0.0
 *作	者：yiyi
 *完成日期：2015年7月23日
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
package com.prize.cloud.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.util.Log;

import com.lidroid.xutils.DbUtils;
import com.lidroid.xutils.exception.DbException;
import com.lidroid.xutils.util.LogUtils;
import com.prize.cloud.app.CloudApp;
import com.prize.cloud.bean.CloudAccount;
import com.prize.cloud.bean.Person;
import com.prize.cloud.db.DbManager;

/**
 * 云账号的数据提供类，提供两类数据：账号和个人信息
 * @author yiyi
 * @version 1.0.0
 */
public class AccountProvider extends ContentProvider {

	public static final String AUTHORITY = "com.prize.provider.cloud";

	private static final int PERSON = 1;
	private static final int ACCOUNT = 2;
	private static final int EMAIL = 3;

	private static final UriMatcher sUriMatcher;

	private DbUtils dbUtils;

	@Override
	public boolean onCreate() {
		dbUtils = DbManager.getInstance().getDb();
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		dbUtils = DbManager.getInstance().getDb();
		if (dbUtils == null) {    //数据共享时，其他应用查询该数据库时，会出现这种情况，要先初始化db否则getDb()为null
			DbManager.getInstance().createDb(CloudApp.getInstance());
			dbUtils = DbManager.getInstance().getDb();
		}
		LogUtils.i("Query uri :" + uri);
		switch (sUriMatcher.match(uri)) {
		case PERSON:
			Person person = null;
			try {
				person = dbUtils.findFirst(Person.class);
			} catch (DbException e1) {
				e1.printStackTrace();
			}
			if (person == null)
				return null;
			{
				String[] columns = new String[] { "userId", "avatar", "phone",
						"email", "realName", "sex" };
				MatrixCursor stringCursor = new MatrixCursor(columns);
				String row[] = new String[6];
				row[0] = person.getUserId();
				row[1] = person.getAvatar();
				row[2] = person.getPhone();
				row[3] = person.getEmail();
				row[4] = person.getRealName();
				row[5] = person.getSex() + "";
				stringCursor.addRow(row);
				return stringCursor;
			}
		case ACCOUNT:
			CloudAccount account = null;
			try {
				account = dbUtils.findFirst(CloudAccount.class);
			} catch (DbException e) {
				e.printStackTrace();
			}
			if (account == null) {
				return null;
			}				
			{
				String[] columns = new String[] { "loginName", "password",
						"passport" };
				MatrixCursor stringCursor = new MatrixCursor(columns);
				String row[] = new String[3];
				row[0] = account.getLoginName();
				row[1] = account.getPassword();
				row[2] = account.getPassport();
				stringCursor.addRow(row);
				return stringCursor;
			}
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
	}

	@Override
	public String getType(Uri uri) {
		return "vnd.android.cursor.item/nouse";
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		if (dbUtils == null)
			dbUtils = DbManager.getInstance().getDb();
		LogUtils.i("Insert uri :" + uri);
		switch (sUriMatcher.match(uri)) {
		case PERSON:
			Person person = valuestoPerson(values);
			try {
				dbUtils.deleteAll(Person.class);
				dbUtils.save(person);
			} catch (DbException e1) {
				e1.printStackTrace();
			}
			break;
		case ACCOUNT:
			CloudAccount account = valuesToAccount(values);
			try {
				dbUtils.deleteAll(CloudAccount.class);
				dbUtils.save(account);
			} catch (DbException e) {
				e.printStackTrace();
			}

			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		return null;
	}

	private final static Person valuestoPerson(ContentValues values) {
		Person person = new Person();
		try {
			person.setUserId(values.getAsString("userId"));
			person.setPhone(values.getAsString("phone"));
			person.setAvatar(values.getAsString("avatar"));
			person.setEmail(values.getAsString("email"));
			person.setRealName(values.getAsString("realName"));
			person.setSex(values.getAsInteger("sex"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return person;
	}

	private final static CloudAccount valuesToAccount(ContentValues values) {
		CloudAccount account = new CloudAccount();
		account.setLoginName(values.getAsString("loginName"));
		account.setPassword(values.getAsString("password"));
		account.setPassport(values.getAsString("passport"));
		return account;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		if (dbUtils == null)
			dbUtils = DbManager.getInstance().getDb();
		switch (sUriMatcher.match(uri)) {
		case PERSON:
			try {
				dbUtils.deleteAll(Person.class);
			} catch (DbException e) {
				return -1;
			}
			break;
		case ACCOUNT:
			try {
				dbUtils.deleteAll(CloudAccount.class);
			} catch (DbException e) {
				return -1;
			}
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		return 0;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		if (dbUtils == null)
			dbUtils = DbManager.getInstance().getDb();
		switch (sUriMatcher.match(uri)) {
		case EMAIL:
			Person person = null;
			try {
				person = dbUtils.findFirst(Person.class);
			} catch (DbException e) {
				e.printStackTrace();
			}
			if (person == null)
				return -1;
			person.setEmail(values.getAsString("email"));
			try {
				dbUtils.update(person, "email");
			} catch (DbException e) {
				e.printStackTrace();
			}
			break;

		default:
			break;
		}
		return 0;
	}

	static {
		sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

		sUriMatcher.addURI(AUTHORITY, "person", PERSON);
		sUriMatcher.addURI(AUTHORITY, "person" + "/*", PERSON);

		sUriMatcher.addURI(AUTHORITY, "account", ACCOUNT);
		sUriMatcher.addURI(AUTHORITY, "account" + "/*", ACCOUNT);

		sUriMatcher.addURI(AUTHORITY, "email", EMAIL);

	}

}
