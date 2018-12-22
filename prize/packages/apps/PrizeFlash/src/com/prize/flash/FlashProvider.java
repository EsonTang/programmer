
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

package com.prize.flash;

import java.util.HashMap;
import android.os.SystemProperties;
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
import android.util.Log;

public class FlashProvider extends ContentProvider {  
  
    private static final int FLASH = 1;  
    private static final int OTHER = 2;  
    private static final UriMatcher sUriMatcher;  
    public static final String AUTHORITY = "com.android.flash";  
    public static final Uri CONTENT_URI = Uri.parse("content://"+ AUTHORITY +"/systemflashs"); 

    @Override  
    public boolean onCreate() {  
    	Log.d("xucm","provide oncreate");
        return true;  
    }  
  
    @Override  
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,  
            String sortOrder) { 
    	return null;
       }  
  
    @Override  
    public String getType(Uri uri) {  
        switch (sUriMatcher.match(uri)) {  
        case FLASH:  
            return "0";  
        case OTHER:  
            return "1";  
        default:  
            throw new IllegalArgumentException("Unknown URI " + uri);  
        }  
    }  
  
    @Override  
    public Uri insert(Uri uri, ContentValues initialValues) {  
       return uri;
   }  
  
    @Override  
    public int delete(Uri uri, String where, String[] whereArgs) { 
        return 0;  
   }  
  
    @Override  
    public int update(Uri uri, ContentValues values, String where, String[] whereArgs) { 
    	SystemProperties.set("persist.sys.prizeflash", values.getAsString("flashstatus"));
        Log.d("xucm", "android presystempropty:"+SystemProperties.getInt("persist.sys.prizeflash",0));
        getContext().getContentResolver().notifyChange(uri, null);  
        return 0;
     }  
  
    static {  
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);  
        // 这个地方的persons要和PersonColumns.CONTENT_URI中最后面的一个Segment一致  
        sUriMatcher.addURI(AUTHORITY, "systemflashs", FLASH);  
        sUriMatcher.addURI(AUTHORITY, "systemflashs/#", OTHER);  
  
    }  
}  
