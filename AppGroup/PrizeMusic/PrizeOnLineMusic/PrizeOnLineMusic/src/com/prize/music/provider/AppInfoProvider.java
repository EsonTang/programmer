/*******************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：
 *当前版本：
 *作	者：
 *完成日期：
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

package com.prize.music.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;

import com.prize.app.constants.Constants;
import com.prize.app.database.AccountTable;
import com.prize.app.database.DownLoadedTable;
import com.prize.app.database.PersonTable;
import com.prize.app.database.PrizeDatabaseHelper;

/**
 * 类描述：
 * 
 * @author 龙宝修
 * @version 版本
 */
public class AppInfoProvider extends ContentProvider {

	private static final String AUTHORITY = "com.prize.music.provider";
	public static final String TABLE_PERSON_PATH = Constants.TABLE_PERSON_PATH;
	public static final String TABLE_ACCOUNT_PATH = Constants.TABLE_ACCOUNT_PATH;
	private static final UriMatcher sUriMatcher;
	private static final int DOWNLOADED = 1;
	private static final int PERSON = 2;
	private static final int ACCOUNT = 3;
	private static final int EMAIL = 4;

	static {
		sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

		sUriMatcher.addURI(AUTHORITY, "table_person", PERSON);
		sUriMatcher.addURI(AUTHORITY, "table_person" + "/*", PERSON);

		sUriMatcher.addURI(AUTHORITY, "table_account", ACCOUNT);
		sUriMatcher.addURI(AUTHORITY, "table_account" + "/*", ACCOUNT);

		// sUriMatcher.addURI(AUTHORITY, "email", EMAIL);
	}

	@Override
	public boolean onCreate() {

		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		Cursor cursor = null;
		switch (sUriMatcher.match(uri)) {
		case DOWNLOADED:
			cursor = PrizeDatabaseHelper.query(
					DownLoadedTable.TABLE_NAME_D0WNLOADED,
					DownLoadedTable.DOWNLOADED_COLUMNS, null, null, null, null,
					null);
			return cursor;
		case PERSON:
			cursor = PrizeDatabaseHelper.query(PersonTable.TABLE_NAME_PERSON,
					PersonTable.PERSON_COLUMNS, null, null, null, null, null);
			return cursor;
		case ACCOUNT:
			cursor = PrizeDatabaseHelper.query(AccountTable.TABLE_NAME_ACCOUNT,
					AccountTable.ACCOUNT_COLUMNS, null, null, null, null, null);
			return cursor;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
	}

	@Override
	public String getType(Uri uri) {
		return "vnd.android.cursor.dir/nouse";
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		switch (sUriMatcher.match(uri)) {
		case DOWNLOADED:

			PrizeDatabaseHelper.replace(DownLoadedTable.TABLE_NAME_D0WNLOADED,
					values);
			// 数据变化的时候通知contentResolver
			getContext().getContentResolver().notifyChange(uri, null);
			return null;
		case PERSON:
			PrizeDatabaseHelper.delete(PersonTable.TABLE_NAME_PERSON, null,
					null);
			PrizeDatabaseHelper.insert(PersonTable.TABLE_NAME_PERSON, null,
					values);
			getContext().getContentResolver().notifyChange(uri, null);
			return null;
		case ACCOUNT:
			PrizeDatabaseHelper.delete(AccountTable.TABLE_NAME_ACCOUNT, null,
					null);
			PrizeDatabaseHelper.insert(AccountTable.TABLE_NAME_ACCOUNT, null,
					values);
			getContext().getContentResolver().notifyChange(uri, null);
			return null;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		switch (sUriMatcher.match(uri)) {
		case DOWNLOADED:
			PrizeDatabaseHelper.delete(DownLoadedTable.TABLE_NAME_D0WNLOADED,
					selection, selectionArgs);
			// 数据变化的时候通知contentResolver
			getContext().getContentResolver().notifyChange(uri, null);
			return 0;
		case PERSON:
			PrizeDatabaseHelper.delete(PersonTable.TABLE_NAME_PERSON,
					selection, selectionArgs);
			getContext().getContentResolver().notifyChange(uri, null);
			return 0;
		case ACCOUNT:
			PrizeDatabaseHelper.delete(AccountTable.TABLE_NAME_ACCOUNT,
					selection, selectionArgs);
			getContext().getContentResolver().notifyChange(uri, null);
			return 0;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		switch (sUriMatcher.match(uri)) {
		case DOWNLOADED:
			// PrizeDatabaseHelper.replace(DownLoadedTable.TABLE_NAME_GAME,
			// null, values);
			// 数据变化的时候通知contentResolver
			getContext().getContentResolver().notifyChange(uri, null);
			break;
		case EMAIL:
			PrizeDatabaseHelper.update(PersonTable.TABLE_NAME_PERSON, values,
					selection, selectionArgs);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		return 0;
	}

}
