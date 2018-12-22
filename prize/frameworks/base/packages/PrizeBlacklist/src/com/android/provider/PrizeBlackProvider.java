package com.android.provider;

import java.util.HashMap;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.WhiteListColumns;
import android.text.TextUtils;



public class PrizeBlackProvider extends ContentProvider {

	private DbHelper dbHelper;
	private static final UriMatcher sUriMatcher;
	
//	private static final int MALWARE = 100;
//	private static final int MALWARE_ID = 102;
	
	private static final int PUREBACKGROND = 1;
	private static final int PUREBACKGROND_ID = 2;
	private static final int NOTIFICATION = 3;
	private static final int NOTIFICATION_ID = 4;
	private static final int FLOATWINDOW = 5;
	private static final int FLOATWINDOW_ID = 6;
	private static final int AUTOLAUNCH = 7;
	private static final int AUTOLAUNCH_ID = 8;
	private static final int NETFORBADE = 9;
	private static final int NETFORBADE_ID = 10;
	private static final int RELEATEWAKEUP = 11;
	private static final int RELEATEWAKEUP_ID = 12;
	private static final int SLEEPNETWHITE = 13;
	private static final int SLEEPNETWHITE_ID = 14;
	private static final int BLOCKACTIVITY = 15;
	private static final int BLOCKACTIVITY_ID = 16;
	private static final int MSGWHITE = 17;
	private static final int MSGWHITE_ID = 18;
	private static final int INSTALLWHITE = 19;
	private static final int INSTALLWHITE_ID = 20;
	private static final int DOZEWHITE = 21;
	private static final int DOZEWHITE_ID = 22;
       private static final int PROVIDERWAKEUP = 23;
	private static final int PROVIDERWAKEUP_ID = 24;
	
	private static HashMap<String, String> sProjection;
	
    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
//        sUriMatcher.addURI(PrizeBlacklist.AUTHORITY, "malware", MALWARE);
//        sUriMatcher.addURI(PrizeBlacklist.AUTHORITY, "malware/#", MALWARE_ID);
        
        sUriMatcher.addURI(WhiteListColumns.AUTHORITY, "purebackground", PUREBACKGROND);
        sUriMatcher.addURI(WhiteListColumns.AUTHORITY, "purebackground/#", PUREBACKGROND_ID);
        
        sUriMatcher.addURI(WhiteListColumns.AUTHORITY, "notification", NOTIFICATION);
        sUriMatcher.addURI(WhiteListColumns.AUTHORITY, "notification/#", NOTIFICATION_ID);
        
        sUriMatcher.addURI(WhiteListColumns.AUTHORITY, "floatwindow", FLOATWINDOW);
        sUriMatcher.addURI(WhiteListColumns.AUTHORITY, "floatwindow/#", FLOATWINDOW_ID);        
        
        sUriMatcher.addURI(WhiteListColumns.AUTHORITY, "autolaunch", AUTOLAUNCH);
        sUriMatcher.addURI(WhiteListColumns.AUTHORITY, "autolaunch/#", AUTOLAUNCH_ID);
        
        sUriMatcher.addURI(WhiteListColumns.AUTHORITY, "netforbade", NETFORBADE);
        sUriMatcher.addURI(WhiteListColumns.AUTHORITY, "netforbade/#", NETFORBADE_ID);
        
        sUriMatcher.addURI(WhiteListColumns.AUTHORITY, "releatewakeup", RELEATEWAKEUP);
        sUriMatcher.addURI(WhiteListColumns.AUTHORITY, "releatewakeup/#", RELEATEWAKEUP_ID);
        
        sUriMatcher.addURI(WhiteListColumns.AUTHORITY, "sleepnet", SLEEPNETWHITE);
        sUriMatcher.addURI(WhiteListColumns.AUTHORITY, "sleepnet/#", SLEEPNETWHITE_ID);
        
        sUriMatcher.addURI(WhiteListColumns.AUTHORITY, "blockactivity", BLOCKACTIVITY);
        sUriMatcher.addURI(WhiteListColumns.AUTHORITY, "blockactivity/#", BLOCKACTIVITY_ID);
        
        sUriMatcher.addURI(WhiteListColumns.AUTHORITY, "msgwhite", MSGWHITE);
        sUriMatcher.addURI(WhiteListColumns.AUTHORITY, "msgwhite/#", MSGWHITE_ID);
        
        sUriMatcher.addURI(WhiteListColumns.AUTHORITY, "installwhite", INSTALLWHITE);
        sUriMatcher.addURI(WhiteListColumns.AUTHORITY, "installwhite/#", INSTALLWHITE_ID);

	 sUriMatcher.addURI(WhiteListColumns.AUTHORITY, "dozewhite", DOZEWHITE);
        sUriMatcher.addURI(WhiteListColumns.AUTHORITY, "dozewhite/#", DOZEWHITE_ID);

        sUriMatcher.addURI(WhiteListColumns.AUTHORITY, "providerwakeup", PROVIDERWAKEUP);
        sUriMatcher.addURI(WhiteListColumns.AUTHORITY, "providerwakeup/#", PROVIDERWAKEUP_ID);
        
        sProjection = new HashMap<String, String>();    
        
        sProjection.put(WhiteListColumns.BaseColumns._ID, WhiteListColumns.BaseColumns._ID);
        sProjection.put(WhiteListColumns.BaseColumns.PKGNAME, WhiteListColumns.BaseColumns.PKGNAME);
        sProjection.put(WhiteListColumns.BaseColumns.ENABLE, WhiteListColumns.BaseColumns.ENABLE);
	 sProjection.put(WhiteListColumns.BaseColumns.ISSERVERCONFIG, WhiteListColumns.BaseColumns.ISSERVERCONFIG);
        sProjection.put(WhiteListColumns.RelateWakeup.CLASS, WhiteListColumns.RelateWakeup.CLASS);
        sProjection.put(WhiteListColumns.RelateWakeup.ACTION, WhiteListColumns.RelateWakeup.ACTION);
        sProjection.put(WhiteListColumns.RelateWakeup.CALLERPKG, WhiteListColumns.RelateWakeup.CALLERPKG);
    }
	
	@Override
	public boolean onCreate() {
		// TODO Auto-generated method stub
		dbHelper = new DbHelper(getContext());
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		// TODO Auto-generated method stub
		 SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
	        switch (sUriMatcher.match(uri)) {
	        /*case MALWARE:
	            qb.setTables(DbHelper.TABLE);
	            qb.setProjectionMap(sProjection);
	            break;
	        case MALWARE_ID:
	            qb.setTables(DbHelper.TABLE);
	            qb.setProjectionMap(sProjection);
	            qb.appendWhere(Malware._ID + "=" + uri.getPathSegments().get(1));
	            break;*/
	            
	        case PUREBACKGROND:
	        	qb.setTables(WhiteListColumns.Purebackground.TABLE);
	            qb.setProjectionMap(sProjection);
	        	break;
	        case PUREBACKGROND_ID:
	        	qb.setTables(WhiteListColumns.Purebackground.TABLE);
	            qb.setProjectionMap(sProjection);
	            qb.appendWhere(WhiteListColumns.BaseColumns._ID + "=" + uri.getPathSegments().get(1));
	        	break;
	        	
	        case NOTIFICATION:
	        	qb.setTables(WhiteListColumns.Notification.TABLE);
	            qb.setProjectionMap(sProjection);
	        	break;
	        case NOTIFICATION_ID:
	        	qb.setTables(WhiteListColumns.Notification.TABLE);
	            qb.setProjectionMap(sProjection);
	            qb.appendWhere(WhiteListColumns.BaseColumns._ID + "=" + uri.getPathSegments().get(1));
	        	break;
	        	
	        case FLOATWINDOW:
	        	qb.setTables(WhiteListColumns.FloatWindow.TABLE);
	            qb.setProjectionMap(sProjection);
	        	break;
	        case FLOATWINDOW_ID:
	        	qb.setTables(WhiteListColumns.FloatWindow.TABLE);
	            qb.setProjectionMap(sProjection);
	            qb.appendWhere(WhiteListColumns.BaseColumns._ID + "=" + uri.getPathSegments().get(1));
	        	break;
	        	
	        case AUTOLAUNCH:
	        	qb.setTables(WhiteListColumns.AutoLaunch.TABLE);
	            qb.setProjectionMap(sProjection);
	        	break;
	        case AUTOLAUNCH_ID:
	        	qb.setTables(WhiteListColumns.AutoLaunch.TABLE);
	            qb.setProjectionMap(sProjection);
	            qb.appendWhere(WhiteListColumns.BaseColumns._ID + "=" + uri.getPathSegments().get(1));
	        	break;
	        	
	        case NETFORBADE:
	        	qb.setTables(WhiteListColumns.NetForbade.TABLE);
	            qb.setProjectionMap(sProjection);
	        	break;
	        case NETFORBADE_ID:
	        	qb.setTables(WhiteListColumns.NetForbade.TABLE);
	            qb.setProjectionMap(sProjection);
	            qb.appendWhere(WhiteListColumns.BaseColumns._ID + "=" + uri.getPathSegments().get(1));
	        	break;
	        	
	        case RELEATEWAKEUP:
	        	qb.setTables(WhiteListColumns.RelateWakeup.TABLE);
	            qb.setProjectionMap(sProjection);
	        	break;
	        case RELEATEWAKEUP_ID:
	        	qb.setTables(WhiteListColumns.RelateWakeup.TABLE);
	            qb.setProjectionMap(sProjection);
	            qb.appendWhere(WhiteListColumns.BaseColumns._ID + "=" + uri.getPathSegments().get(1));
	        	break;
	        	
	        case SLEEPNETWHITE:
	        	qb.setTables(WhiteListColumns.SleepNetWhite.TABLE);
	            qb.setProjectionMap(sProjection);
	        	break;
	        case SLEEPNETWHITE_ID:
	        	qb.setTables(WhiteListColumns.SleepNetWhite.TABLE);
	            qb.setProjectionMap(sProjection);
	            qb.appendWhere(WhiteListColumns.BaseColumns._ID + "=" + uri.getPathSegments().get(1));
	        	break;
	        	
	        case BLOCKACTIVITY:
	        	qb.setTables(WhiteListColumns.BlockActivity.TABLE);
	            qb.setProjectionMap(sProjection);
	        	break;
	        case BLOCKACTIVITY_ID:
	        	qb.setTables(WhiteListColumns.BlockActivity.TABLE);
	            qb.setProjectionMap(sProjection);
	            qb.appendWhere(WhiteListColumns.BaseColumns._ID + "=" + uri.getPathSegments().get(1));
	        	break;
	        	
	        case MSGWHITE:
	        	qb.setTables(WhiteListColumns.MsgWhite.TABLE);
	            qb.setProjectionMap(sProjection);
	        	break;
	        case MSGWHITE_ID:
	        	qb.setTables(WhiteListColumns.MsgWhite.TABLE);
	            qb.setProjectionMap(sProjection);
	            qb.appendWhere(WhiteListColumns.BaseColumns._ID + "=" + uri.getPathSegments().get(1));
	        	break;
	        	
	        case INSTALLWHITE:
	        	qb.setTables(WhiteListColumns.InstallWhite.TABLE);
	            qb.setProjectionMap(sProjection);
	        	break;
	        case INSTALLWHITE_ID:
	        	qb.setTables(WhiteListColumns.InstallWhite.TABLE);
	            qb.setProjectionMap(sProjection);
	            qb.appendWhere(WhiteListColumns.BaseColumns._ID + "=" + uri.getPathSegments().get(1));
	        	break;

		case DOZEWHITE:
	        	qb.setTables(WhiteListColumns.DozeWhiteList.TABLE);
	            qb.setProjectionMap(sProjection);
	        	break;
	        case DOZEWHITE_ID:
	        	qb.setTables(WhiteListColumns.DozeWhiteList.TABLE);
	            qb.setProjectionMap(sProjection);
	            qb.appendWhere(WhiteListColumns.BaseColumns._ID + "=" + uri.getPathSegments().get(1));
	        	break;

                case PROVIDERWAKEUP:
	        	qb.setTables(WhiteListColumns.ProviderWakeup.TABLE);
	            qb.setProjectionMap(sProjection);
	        	break;
	        case PROVIDERWAKEUP_ID:
	        	qb.setTables(WhiteListColumns.ProviderWakeup.TABLE);
	            qb.setProjectionMap(sProjection);
	            qb.appendWhere(WhiteListColumns.BaseColumns._ID + "=" + uri.getPathSegments().get(1));
	        	break;
	        	
	        default:
	            throw new IllegalArgumentException("Err URI" + uri);
	        }
	        SQLiteDatabase db = dbHelper.getReadableDatabase();
	        Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, null);
	        c.setNotificationUri(getContext().getContentResolver(), uri);
	        return c;
	}

	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		// TODO Auto-generated method stub
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		long rowId = 0;
		Uri sUri = null;
		
		switch (sUriMatcher.match(uri)) {
        /*case MALWARE:        
        	rowId = db.insert(DbHelper.TABLE, Malware.PACKAGE, values);
        	sUri = ContentUris.withAppendedId(Malware.CONTENT_URI, rowId);
            break;*/
            
        case PUREBACKGROND:
        	rowId = db.insert(WhiteListColumns.Purebackground.TABLE, WhiteListColumns.BaseColumns.PKGNAME, values);
        	sUri = ContentUris.withAppendedId(WhiteListColumns.Purebackground.CONTENT_URI, rowId);
        	break;
        	
        case NOTIFICATION:
        	rowId = db.insert(WhiteListColumns.Notification.TABLE, WhiteListColumns.BaseColumns.PKGNAME, values);
        	sUri = ContentUris.withAppendedId(WhiteListColumns.Notification.CONTENT_URI, rowId);
        	break;
        	
        case FLOATWINDOW:
        	rowId = db.insert(WhiteListColumns.FloatWindow.TABLE, WhiteListColumns.BaseColumns.PKGNAME, values);
        	sUri = ContentUris.withAppendedId(WhiteListColumns.FloatWindow.CONTENT_URI, rowId);
        	break;
        	
        case AUTOLAUNCH:
        	rowId = db.insert(WhiteListColumns.AutoLaunch.TABLE, WhiteListColumns.BaseColumns.PKGNAME, values);
        	sUri = ContentUris.withAppendedId(WhiteListColumns.AutoLaunch.CONTENT_URI, rowId);
        	break;
        	
        case NETFORBADE:
        	rowId = db.insert(WhiteListColumns.NetForbade.TABLE, WhiteListColumns.BaseColumns.PKGNAME, values);
        	sUri = ContentUris.withAppendedId(WhiteListColumns.NetForbade.CONTENT_URI, rowId);
        	break;
        	
        case RELEATEWAKEUP:
        	rowId = db.insert(WhiteListColumns.RelateWakeup.TABLE, WhiteListColumns.BaseColumns.PKGNAME, values);
        	sUri = ContentUris.withAppendedId(WhiteListColumns.RelateWakeup.CONTENT_URI, rowId);
        	break;
        	
        case SLEEPNETWHITE:
        	rowId = db.insert(WhiteListColumns.SleepNetWhite.TABLE, WhiteListColumns.BaseColumns.PKGNAME, values);
        	sUri = ContentUris.withAppendedId(WhiteListColumns.SleepNetWhite.CONTENT_URI, rowId);
        	break;
        	
        case BLOCKACTIVITY:        	
        	rowId = db.insert(WhiteListColumns.BlockActivity.TABLE, WhiteListColumns.BaseColumns.PKGNAME, values);
        	sUri = ContentUris.withAppendedId(WhiteListColumns.BlockActivity.CONTENT_URI, rowId);
        	break;
        	
        case MSGWHITE:
        	rowId = db.insert(WhiteListColumns.MsgWhite.TABLE, WhiteListColumns.BaseColumns.PKGNAME, values);
        	sUri = ContentUris.withAppendedId(WhiteListColumns.MsgWhite.CONTENT_URI, rowId);
        	break;
        	
        case INSTALLWHITE:
        	rowId = db.insert(WhiteListColumns.InstallWhite.TABLE, WhiteListColumns.BaseColumns.PKGNAME, values);
        	sUri = ContentUris.withAppendedId(WhiteListColumns.InstallWhite.CONTENT_URI, rowId);
        	break;

	case DOZEWHITE:
        	rowId = db.insert(WhiteListColumns.DozeWhiteList.TABLE, WhiteListColumns.BaseColumns.PKGNAME, values);
        	sUri = ContentUris.withAppendedId(WhiteListColumns.DozeWhiteList.CONTENT_URI, rowId);
        	break;
       case PROVIDERWAKEUP:
        	rowId = db.insert(WhiteListColumns.ProviderWakeup.TABLE, WhiteListColumns.BaseColumns.PKGNAME, values);
        	sUri = ContentUris.withAppendedId(WhiteListColumns.ProviderWakeup.CONTENT_URI, rowId);
        	break;
        	
        default:
            throw new IllegalArgumentException("Err URI" + uri);
        }
		
        if (rowId > 0) {            
            getContext().getContentResolver().notifyChange(sUri, null);
            return sUri;
        }
		return null;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		SQLiteDatabase db = dbHelper.getWritableDatabase();
        int count;
        String sId;
        switch (sUriMatcher.match(uri)) {
        /*case MALWARE: 
            count = db.delete(DbHelper.TABLE, selection, selectionArgs);
            break;
        case MALWARE_ID: 
            sId = uri.getPathSegments().get(1);
            count = db.delete(DbHelper.TABLE, Malware._ID + "=" + sId
                    + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
            break;*/
            
        case PUREBACKGROND:        	
        	count = db.delete(WhiteListColumns.Purebackground.TABLE, selection, selectionArgs);
        	break;
        case PUREBACKGROND_ID:
        	sId = uri.getPathSegments().get(1);
            count = db.delete(WhiteListColumns.Purebackground.TABLE, WhiteListColumns.BaseColumns._ID + "=" + sId
                    + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
        	break;
        	
        case NOTIFICATION:
        	count = db.delete(WhiteListColumns.Notification.TABLE, selection, selectionArgs);
        	break;
        case NOTIFICATION_ID:
        	sId = uri.getPathSegments().get(1);
            count = db.delete(WhiteListColumns.Notification.TABLE, WhiteListColumns.BaseColumns._ID + "=" + sId
                    + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
        	break;
        	
        case FLOATWINDOW:
        	count = db.delete(WhiteListColumns.FloatWindow.TABLE, selection, selectionArgs);
        	break;
        case FLOATWINDOW_ID:
        	sId = uri.getPathSegments().get(1);
            count = db.delete(WhiteListColumns.FloatWindow.TABLE, WhiteListColumns.BaseColumns._ID + "=" + sId
                    + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
        	break;
        	
        case AUTOLAUNCH:
        	count = db.delete(WhiteListColumns.AutoLaunch.TABLE, selection, selectionArgs);
        	break;
        case AUTOLAUNCH_ID:
        	sId = uri.getPathSegments().get(1);
            count = db.delete(WhiteListColumns.AutoLaunch.TABLE, WhiteListColumns.BaseColumns._ID + "=" + sId
                    + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
        	break;
        	
        case NETFORBADE:
        	count = db.delete(WhiteListColumns.NetForbade.TABLE, selection, selectionArgs);
        	break;
        case NETFORBADE_ID:
        	sId = uri.getPathSegments().get(1);
            count = db.delete(WhiteListColumns.NetForbade.TABLE, WhiteListColumns.BaseColumns._ID + "=" + sId
                    + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
        	break;
        	
        case RELEATEWAKEUP:
        	count = db.delete(WhiteListColumns.RelateWakeup.TABLE, selection, selectionArgs);
        	break;
        case RELEATEWAKEUP_ID:
        	sId = uri.getPathSegments().get(1);
            count = db.delete(WhiteListColumns.RelateWakeup.TABLE, WhiteListColumns.BaseColumns._ID + "=" + sId
                    + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
        	break;
        	
        case SLEEPNETWHITE:
        	count = db.delete(WhiteListColumns.SleepNetWhite.TABLE, selection, selectionArgs);
        	break;
        case SLEEPNETWHITE_ID:
        	sId = uri.getPathSegments().get(1);
            count = db.delete(WhiteListColumns.SleepNetWhite.TABLE, WhiteListColumns.BaseColumns._ID + "=" + sId
                    + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
        	break;
        	
        case BLOCKACTIVITY:
        	count = db.delete(WhiteListColumns.BlockActivity.TABLE, selection, selectionArgs);
        	break;
        case BLOCKACTIVITY_ID:
        	sId = uri.getPathSegments().get(1);
            count = db.delete(WhiteListColumns.BlockActivity.TABLE, WhiteListColumns.BaseColumns._ID + "=" + sId
                    + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
        	break;
        	
        case MSGWHITE:
        	count = db.delete(WhiteListColumns.MsgWhite.TABLE, selection, selectionArgs);
        	break;
        case MSGWHITE_ID:
        	sId = uri.getPathSegments().get(1);
            count = db.delete(WhiteListColumns.MsgWhite.TABLE, WhiteListColumns.BaseColumns._ID + "=" + sId
                    + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
        	break;
        	
        case INSTALLWHITE:
        	count = db.delete(WhiteListColumns.InstallWhite.TABLE, selection, selectionArgs);
        	break;
        case INSTALLWHITE_ID:
        	sId = uri.getPathSegments().get(1);
            count = db.delete(WhiteListColumns.InstallWhite.TABLE, WhiteListColumns.BaseColumns._ID + "=" + sId
                    + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
        	break;

	case DOZEWHITE:
        	count = db.delete(WhiteListColumns.DozeWhiteList.TABLE, selection, selectionArgs);
        	break;
        case DOZEWHITE_ID:
        	sId = uri.getPathSegments().get(1);
            count = db.delete(WhiteListColumns.DozeWhiteList.TABLE, WhiteListColumns.BaseColumns._ID + "=" + sId
                    + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
        	break;

        case PROVIDERWAKEUP:
        	count = db.delete(WhiteListColumns.ProviderWakeup.TABLE, selection, selectionArgs);
        	break;
        case PROVIDERWAKEUP_ID:
        	sId = uri.getPathSegments().get(1);
            count = db.delete(WhiteListColumns.ProviderWakeup.TABLE, WhiteListColumns.BaseColumns._ID + "=" + sId
                    + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
        	break;
               
        default:
            throw new IllegalArgumentException("err URI" + uri);
        }
        if(count > 0)getContext().getContentResolver().notifyChange(uri, null);
        return count;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		// TODO Auto-generated method stub
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int count;
        String noteId;
        switch (sUriMatcher.match(uri)) {
        /*case MALWARE:
            count = db.update(DbHelper.TABLE, values, selection, selectionArgs);
            break;
        case MALWARE_ID:
        	noteId = uri.getPathSegments().get(1);
            count = db.update(DbHelper.TABLE, values, Malware._ID + "=" + noteId
                    + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
            break;*/
            
        case PUREBACKGROND:        	
        	count = db.update(WhiteListColumns.Purebackground.TABLE, values, selection, selectionArgs);
        	break;
        case PUREBACKGROND_ID:
        	noteId = uri.getPathSegments().get(1);
            count = db.update(WhiteListColumns.Purebackground.TABLE,values, WhiteListColumns.BaseColumns._ID + "=" + noteId
                    + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
        	break;
        	
        case NOTIFICATION:
        	count = db.update(WhiteListColumns.Notification.TABLE, values, selection, selectionArgs);
        	break;
        case NOTIFICATION_ID:
        	noteId = uri.getPathSegments().get(1);
        	count = db.update(WhiteListColumns.Notification.TABLE,values, WhiteListColumns.BaseColumns._ID + "=" + noteId
                    + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
        	break;
        	
        case FLOATWINDOW:
        	count = db.update(WhiteListColumns.FloatWindow.TABLE, values, selection, selectionArgs);
        	break;
        case FLOATWINDOW_ID:
        	noteId = uri.getPathSegments().get(1);
        	count = db.update(WhiteListColumns.FloatWindow.TABLE,values, WhiteListColumns.BaseColumns._ID + "=" + noteId
                    + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
        	break;
        	
        case AUTOLAUNCH:
        	count = db.update(WhiteListColumns.AutoLaunch.TABLE, values, selection, selectionArgs);
        	break;
        case AUTOLAUNCH_ID:
        	noteId = uri.getPathSegments().get(1);
        	count = db.update(WhiteListColumns.AutoLaunch.TABLE,values, WhiteListColumns.BaseColumns._ID + "=" + noteId
                    + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
        	break;
        	
        case NETFORBADE:
        	count = db.update(WhiteListColumns.NetForbade.TABLE, values, selection, selectionArgs);
        	break;
        case NETFORBADE_ID:
        	noteId = uri.getPathSegments().get(1);
        	count = db.update(WhiteListColumns.NetForbade.TABLE,values, WhiteListColumns.BaseColumns._ID + "=" + noteId
                    + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
        	break;
        	
        case RELEATEWAKEUP:
        	count = db.update(WhiteListColumns.RelateWakeup.TABLE, values, selection, selectionArgs);
        	break;
        case RELEATEWAKEUP_ID:
        	noteId = uri.getPathSegments().get(1);
        	count = db.update(WhiteListColumns.RelateWakeup.TABLE,values, WhiteListColumns.BaseColumns._ID + "=" + noteId
                    + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
        	break;
        	
        case SLEEPNETWHITE:
        	count = db.update(WhiteListColumns.SleepNetWhite.TABLE, values, selection, selectionArgs);
        	break;
        case SLEEPNETWHITE_ID:
        	noteId = uri.getPathSegments().get(1);
        	count = db.update(WhiteListColumns.SleepNetWhite.TABLE,values, WhiteListColumns.BaseColumns._ID + "=" + noteId
                    + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
        	break;
        	
        case BLOCKACTIVITY:
        	count = db.update(WhiteListColumns.BlockActivity.TABLE, values, selection, selectionArgs);
        	break;
        case BLOCKACTIVITY_ID:
        	noteId = uri.getPathSegments().get(1);
        	count = db.update(WhiteListColumns.BlockActivity.TABLE,values, WhiteListColumns.BaseColumns._ID + "=" + noteId
                    + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
        	break;
        	
        case MSGWHITE:
        	count = db.update(WhiteListColumns.MsgWhite.TABLE, values, selection, selectionArgs);
        	break;
        case MSGWHITE_ID:
        	noteId = uri.getPathSegments().get(1);
        	count = db.update(WhiteListColumns.MsgWhite.TABLE,values, WhiteListColumns.BaseColumns._ID + "=" + noteId
                    + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
        	break;
        	
        case INSTALLWHITE:
        	count = db.update(WhiteListColumns.InstallWhite.TABLE, values, selection, selectionArgs);
        	break;
        case INSTALLWHITE_ID:
        	noteId = uri.getPathSegments().get(1);
        	count = db.update(WhiteListColumns.InstallWhite.TABLE,values, WhiteListColumns.BaseColumns._ID + "=" + noteId
                    + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
            break;

	case DOZEWHITE:
        	count = db.update(WhiteListColumns.DozeWhiteList.TABLE, values, selection, selectionArgs);
        	break;
        case DOZEWHITE_ID:
        	noteId = uri.getPathSegments().get(1);
        	count = db.update(WhiteListColumns.DozeWhiteList.TABLE,values, WhiteListColumns.BaseColumns._ID + "=" + noteId
                    + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
            break;

        case PROVIDERWAKEUP:
        	count = db.update(WhiteListColumns.ProviderWakeup.TABLE, values, selection, selectionArgs);
        	break;
        case PROVIDERWAKEUP_ID:
        	noteId = uri.getPathSegments().get(1);
        	count = db.update(WhiteListColumns.ProviderWakeup.TABLE,values, WhiteListColumns.BaseColumns._ID + "=" + noteId
                    + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
            break;
			
        default:
            throw new IllegalArgumentException("Err URI" + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
	}

}
