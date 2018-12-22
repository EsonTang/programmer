/***
 * function:watermark
 * author: wanzhijuan
 * date:   2016-1-21
 */
package com.prize.sticker.db;

import android.database.Cursor;

public class CursorTool {
	public static String getCursorString(Cursor cursor, String column) {
    	final int index = cursor.getColumnIndex(column);
    	return cursor.getString(index);
    }
    
    public static long getCursorLong(Cursor cursor, String column) {
    	final int index = cursor.getColumnIndex(column);
    	return cursor.getLong(index);
    }
    
    public static int getCursorInt(Cursor cursor, String column) {
    	final int index = cursor.getColumnIndex(column);
    	return cursor.getInt(index);
    }
    
    public static float getCursorFloat(Cursor cursor, String column) {
    	final int index = cursor.getColumnIndex(column);
    	return cursor.getFloat(index);
    }
}
