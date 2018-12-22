/***
 * function:watermark
 * author: wanzhijuan
 * date:   2016-1-21
 */
package com.prize.sticker.db;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

public class WatermarkContentProvider extends ContentProvider {

    private static final int ALBUMS = 1;  
    private static final int ALBUMS_ID = 2;  
      
    private static final int WATERMARKS = 3;  
    private static final int WATERMARKS_ID = 4;  
    
    private static final int PHOTO_RESOURCES = 5;  
    private static final int PHOTO_RESOURCES_ID = 6;  
    
    private static final int WORD_RESOURCES = 7;  
    private static final int WORD_RESOURCES_ID = 8;  
    
    private static final int HISTORYS = 9;  
    private static final int HISTORYS_ID = 10;  
    
    private static final UriMatcher sUriMatcher;  
  
 // database helper use to get database instance
    private DatabaseHelper mOpenHelper; 
    
    static {  
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);  
        sUriMatcher.addURI(Provider.AUTHORITY, "watermark_albums", ALBUMS);  
        sUriMatcher.addURI(Provider.AUTHORITY, "watermark_albums/#", ALBUMS_ID); 
        
        sUriMatcher.addURI(Provider.AUTHORITY, "watermark_datas", WATERMARKS);  
        sUriMatcher.addURI(Provider.AUTHORITY, "watermark_datas/#", WATERMARKS_ID); 
        
        sUriMatcher.addURI(Provider.AUTHORITY, "image_resource_datas", PHOTO_RESOURCES);  
        sUriMatcher.addURI(Provider.AUTHORITY, "image_resource_datas/#", PHOTO_RESOURCES_ID); 
        
        sUriMatcher.addURI(Provider.AUTHORITY, "text_resource_datas", WORD_RESOURCES);  
        sUriMatcher.addURI(Provider.AUTHORITY, "text_resource_datas/#", WORD_RESOURCES_ID); 
        
        sUriMatcher.addURI(Provider.AUTHORITY, "watermark_historys", HISTORYS);  
        sUriMatcher.addURI(Provider.AUTHORITY, "watermark_historys/#", HISTORYS_ID); 
        
    }
    
	@Override
	public boolean onCreate() {
		mOpenHelper = new DatabaseHelper(getContext());  
		return true;
	}
	

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();  
        String orderBy;  
        switch (sUriMatcher.match(uri)) { 
        case ALBUMS:  
        case ALBUMS_ID:  
            qb.setTables(Provider.AlbumColumns.TABLE_NAME);  
            // If no sort order is specified use the default  
            if (TextUtils.isEmpty(sortOrder)) {  
                orderBy = Provider.AlbumColumns.DEFAULT_SORT_ORDER;  
            } else {  
                orderBy = sortOrder;  
            }  
            break;  
            
        case WATERMARKS:  
        case WATERMARKS_ID:  
            qb.setTables(Provider.WatermarkColumns.TABLE_NAME);  
            // If no sort order is specified use the default  
            if (TextUtils.isEmpty(sortOrder)) {  
                orderBy = Provider.WatermarkColumns.DEFAULT_SORT_ORDER;  
            } else {  
                orderBy = sortOrder;  
            }  
            break;  
            
        case PHOTO_RESOURCES:  
        case PHOTO_RESOURCES_ID:  
            qb.setTables(Provider.PhotoResColumns.TABLE_NAME);  
            // If no sort order is specified use the default  
            if (TextUtils.isEmpty(sortOrder)) {  
                orderBy = Provider.PhotoResColumns.DEFAULT_SORT_ORDER;  
            } else {  
                orderBy = sortOrder;  
            }  
            break;  
            
        case WORD_RESOURCES:  
        case WORD_RESOURCES_ID:  
            qb.setTables(Provider.WordResColumns.TABLE_NAME);  
            // If no sort order is specified use the default  
            if (TextUtils.isEmpty(sortOrder)) {  
                orderBy = Provider.WordResColumns.DEFAULT_SORT_ORDER;  
            } else {  
                orderBy = sortOrder;  
            }  
            break;  
            
        case HISTORYS:  
        case HISTORYS_ID:  
            qb.setTables(Provider.HistoryColumns.TABLE_NAME);  
            // If no sort order is specified use the default  
            if (TextUtils.isEmpty(sortOrder)) {  
                orderBy = Provider.HistoryColumns.DEFAULT_SORT_ORDER;  
            } else {  
                orderBy = sortOrder;  
            }  
            break;  
        default:  
            throw new IllegalArgumentException("Unknown URI " + uri);  
        }  
  
        switch (sUriMatcher.match(uri)) {  
        case ALBUMS:  
        case WATERMARKS:  
        case PHOTO_RESOURCES:
        case WORD_RESOURCES:
        case HISTORYS:
            break;  
  
        case ALBUMS_ID:  
            qb.appendWhere(Provider.AlbumColumns._ID + "=" + uri.getPathSegments().get(1));  
            break;  
              
        case WATERMARKS_ID:  
            qb.appendWhere(Provider.WatermarkColumns._ID + "=" + uri.getPathSegments().get(1));  
            break; 
            
        case PHOTO_RESOURCES_ID:  
            qb.appendWhere(Provider.PhotoResColumns._ID + "=" + uri.getPathSegments().get(1));  
            break; 
            
        case WORD_RESOURCES_ID:  
            qb.appendWhere(Provider.WordResColumns._ID + "=" + uri.getPathSegments().get(1));  
            break; 
            
        case HISTORYS_ID:  
            qb.appendWhere(Provider.HistoryColumns._ID + "=" + uri.getPathSegments().get(1));  
            break; 
  
        default:  
            throw new IllegalArgumentException("Unknown URI " + uri);  
        }  
  
        // Get the database and run the query  
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();  
        Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, orderBy);  
  
        // Tell the cursor what uri to watch, so it knows when its source data changes  
        c.setNotificationUri(getContext().getContentResolver(), uri);  
        return c;  
	}

	@Override
	public String getType(Uri uri) {
		switch (sUriMatcher.match(uri)) { 
        case ALBUMS:  
        case WATERMARKS:
        case PHOTO_RESOURCES:
        case WORD_RESOURCES:
        case HISTORYS:
            return Provider.CONTENT_TYPE;  
        case ALBUMS_ID:  
        case WATERMARKS_ID:  
        case PHOTO_RESOURCES_ID:
        case WORD_RESOURCES_ID:
        case HISTORYS_ID:
            return Provider.CONTENT_ITEM_TYPE;  
        default:  
            throw new IllegalArgumentException("Unknown URI " + uri);  
        }  
	}

	@Override
	public Uri insert(Uri uri, ContentValues initialValues) {
		ContentValues values;  
        if (initialValues != null) {  
            values = new ContentValues(initialValues);  
        } else {  
            values = new ContentValues();  
        }  
          
        String tableName = "";  
        String nullColumn = "";  
        switch (sUriMatcher.match(uri)) { 
        case ALBUMS:  
            tableName = Provider.AlbumColumns.TABLE_NAME;  
            nullColumn = Provider.AlbumColumns.APP_TYPE;  
            // Make sure that the fields are all set  
            break;  
        case WATERMARKS:  
            tableName = Provider.WatermarkColumns.TABLE_NAME;  
            nullColumn = Provider.WatermarkColumns.ALBUM_ID;  
            // Make sure that the fields are all set  
            break;  
        case PHOTO_RESOURCES:  
            tableName = Provider.PhotoResColumns.TABLE_NAME;  
            nullColumn = Provider.PhotoResColumns.WATERMARK_ID;  
            // Make sure that the fields are all set  
            break;  
        case WORD_RESOURCES:  
            tableName = Provider.WordResColumns.TABLE_NAME;  
            nullColumn = Provider.WordResColumns.WATERMARK_ID;  
            // Make sure that the fields are all set  
            break;  
        case HISTORYS:  
            tableName = Provider.HistoryColumns.TABLE_NAME;  
            nullColumn = Provider.HistoryColumns.WATERMARK_ID;  
            // Make sure that the fields are all set  
            break;  
        default:  
            // Validate the requested uri  
            throw new IllegalArgumentException("Unknown URI " + uri);  
                  
        }  
  
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();  
        long rowId = db.insert(tableName, nullColumn, values);  
        if (rowId > 0) {  
            Uri noteUri = ContentUris.withAppendedId(uri, rowId);  
            getContext().getContentResolver().notifyChange(noteUri, null);  
            return noteUri;  
        }  
  
        throw new SQLException("Failed to insert row into " + uri); 
	}

	@Override
	public int delete(Uri uri, String where, String[] whereArgs) {
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();  
        int count;  
        switch (sUriMatcher.match(uri)) { 
        
        case ALBUMS:  
            count = db.delete(Provider.AlbumColumns.TABLE_NAME, where, whereArgs);  
            break;  
  
        case ALBUMS_ID:  
            String albumId = uri.getPathSegments().get(1);  
            count = db.delete(Provider.AlbumColumns.TABLE_NAME, Provider.AlbumColumns._ID + "=" + albumId  
                    + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);  
            break;  
              
        case WATERMARKS:  
            count = db.delete(Provider.WatermarkColumns.TABLE_NAME, where, whereArgs);  
            break;  
              
        case WATERMARKS_ID:  
            String watermarkId = uri.getPathSegments().get(1);  
            count = db.delete(Provider.WatermarkColumns.TABLE_NAME, Provider.WatermarkColumns._ID + "=" + watermarkId  
                    + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);  
            break;  
            
        case PHOTO_RESOURCES:  
            count = db.delete(Provider.PhotoResColumns.TABLE_NAME, where, whereArgs);  
            break;  
              
        case PHOTO_RESOURCES_ID:  
            String photoResId = uri.getPathSegments().get(1);  
            count = db.delete(Provider.PhotoResColumns.TABLE_NAME, Provider.PhotoResColumns._ID + "=" + photoResId  
                    + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);  
            break;  
            
        case WORD_RESOURCES:  
            count = db.delete(Provider.WordResColumns.TABLE_NAME, where, whereArgs);  
            break;  
              
        case WORD_RESOURCES_ID:  
            String wordResId = uri.getPathSegments().get(1);  
            count = db.delete(Provider.WordResColumns.TABLE_NAME, Provider.WordResColumns._ID + "=" + wordResId  
                    + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);  
            break;  
            
        case HISTORYS:  
            count = db.delete(Provider.HistoryColumns.TABLE_NAME, where, whereArgs);  
            break;  
              
        case HISTORYS_ID:  
            String historyId = uri.getPathSegments().get(1);  
            count = db.delete(Provider.HistoryColumns.TABLE_NAME, Provider.HistoryColumns._ID + "=" + historyId  
                    + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);  
            break;  
  
        default:  
            throw new IllegalArgumentException("Unknown URI " + uri);  
        }  
  
        getContext().getContentResolver().notifyChange(uri, null);  
        return count;  
	}

	@Override
	public int update(Uri uri, ContentValues values, String where,
			String[] whereArgs) {
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();  
        int count;  
        switch (sUriMatcher.match(uri)) { 
        case ALBUMS:  
            count = db.update(Provider.AlbumColumns.TABLE_NAME, values, where, whereArgs);  
            break;  
  
        case ALBUMS_ID:  
            String albumId = uri.getPathSegments().get(1);  
            count = db.update(Provider.AlbumColumns.TABLE_NAME, values, Provider.AlbumColumns._ID + "=" + albumId  
                    + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);  
            break;  
        case WATERMARKS:  
            count = db.update(Provider.WatermarkColumns.TABLE_NAME, values, where, whereArgs);  
            break;  
              
        case WATERMARKS_ID:  
            String watermarkId = uri.getPathSegments().get(1);  
            count = db.update(Provider.WatermarkColumns.TABLE_NAME, values, Provider.WatermarkColumns._ID + "=" + watermarkId  
                    + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);  
            break;  
            
        case PHOTO_RESOURCES:  
            count = db.update(Provider.PhotoResColumns.TABLE_NAME, values, where, whereArgs);  
            break;  
  
        case PHOTO_RESOURCES_ID:  
            String photoResId = uri.getPathSegments().get(1);  
            count = db.update(Provider.PhotoResColumns.TABLE_NAME, values, Provider.PhotoResColumns._ID + "=" + photoResId  
                    + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);  
            break;  
        case WORD_RESOURCES:  
            count = db.update(Provider.WordResColumns.TABLE_NAME, values, where, whereArgs);  
            break;  
              
        case WORD_RESOURCES_ID:  
            String wordResId = uri.getPathSegments().get(1);  
            count = db.update(Provider.WordResColumns.TABLE_NAME, values, Provider.WordResColumns._ID + "=" + wordResId  
                    + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);  
            break; 
            
        case HISTORYS:  
            count = db.update(Provider.HistoryColumns.TABLE_NAME, values, where, whereArgs);  
            break;  
              
        case HISTORYS_ID:  
            String historyId = uri.getPathSegments().get(1);  
            count = db.update(Provider.HistoryColumns.TABLE_NAME, values, Provider.HistoryColumns._ID + "=" + historyId  
                    + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);  
            break; 
  
        default:  
            throw new IllegalArgumentException("Unknown URI " + uri);  
        }  
  
        getContext().getContentResolver().notifyChange(uri, null);  
        return count;  
	}

}
