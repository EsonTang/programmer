package com.prize.left.page.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;

import com.android.launcher3.LauncherApplication;
import com.prize.cloud.util.Utils;
import com.prize.left.page.bean.table.AccountTable;
import com.prize.left.page.bean.table.PersonTable;
/**
 * 云账号需要用的provider
 * @author fanjunchen
 */
public class CloudProvider extends ContentProvider {

	private static final String AUTHORITY = Utils.AUTHORITY;
	
	private static final UriMatcher sUriMatcher;
	private static final int PERSON = 2;
	private static final int ACCOUNT = 3;
	private static final int EMAIL = 4;
	
	static {
		sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

		sUriMatcher.addURI(AUTHORITY, "table_person", PERSON);
		sUriMatcher.addURI(AUTHORITY, "table_person"+"/*", PERSON);
		
		sUriMatcher.addURI(AUTHORITY, "table_account", ACCOUNT);
		sUriMatcher.addURI(AUTHORITY, "table_account"+"/*", ACCOUNT);
		
		sUriMatcher.addURI(AUTHORITY, "email", EMAIL);
	}
		
	@Override
	public boolean onCreate() {
		
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		Cursor cursor = null;
		switch (sUriMatcher.match(uri)) {
		case PERSON:
			cursor = LauncherApplication.getDbManager().getDatabase().query(
					PersonTable.TABLE_NAME_PERSON, PersonTable.PERSON_COLUMNS, null,
					null, null, null, null);
			return cursor;
		case ACCOUNT:
			cursor = LauncherApplication.getDbManager().getDatabase().query(
					AccountTable.TABLE_NAME_ACCOUNT, AccountTable.ACCOUNT_COLUMNS, null,
					null, null, null, null);
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
		case PERSON:
			LauncherApplication.getDbManager().getDatabase().delete(PersonTable.TABLE_NAME_PERSON, null,null);
			LauncherApplication.getDbManager().getDatabase().insert(PersonTable.TABLE_NAME_PERSON,null, values);
			getContext().getContentResolver().notifyChange(uri, null);  
			return null;
		case ACCOUNT:
			LauncherApplication.getDbManager().getDatabase().delete(AccountTable.TABLE_NAME_ACCOUNT, null,null);
			LauncherApplication.getDbManager().getDatabase().insert(AccountTable.TABLE_NAME_ACCOUNT, null,values);
			getContext().getContentResolver().notifyChange(uri, null);  
			return null;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		switch (sUriMatcher.match(uri)) {
		case PERSON:
			LauncherApplication.getDbManager().getDatabase().delete(PersonTable.TABLE_NAME_PERSON, selection, selectionArgs);
			getContext().getContentResolver().notifyChange(uri, null);  
			return 0;
		case ACCOUNT:
			LauncherApplication.getDbManager().getDatabase().delete(AccountTable.TABLE_NAME_ACCOUNT, selection, selectionArgs);
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
		case EMAIL:
			LauncherApplication.getDbManager().getDatabase().update(PersonTable.TABLE_NAME_PERSON, values, selection, selectionArgs);
			break;	
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		return 0;
	}

}

