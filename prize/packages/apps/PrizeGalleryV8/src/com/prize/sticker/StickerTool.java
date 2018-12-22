/***
 * function:watermark
 * author: wanzhijuan
 * date:   2016-1-21
 */
package com.prize.sticker;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import android.util.LongSparseArray;

import com.android.gallery3d.filtershow.filters.FilterImageStickerRepresentation;
import com.android.gallery3d.filtershow.filters.FilterRepresentation;
import com.prize.sticker.db.CursorTool;
import com.prize.sticker.db.Provider;

public class StickerTool {
	
	protected static final String TAG = "StickerTool";
	public static final String TYPE_WATERMARK_HOT = "hot";
	public static final String TYPE_WATERMARK_INTEREST = "interest";
	public static final String TYPE_WATERMARK_FOOD = "food";
	public static final String TYPE_WATERMARK_TRAVEL = "travel";
	public static final String TYPE_WATERMARK_ORIGINALITY = "originality";
	public static final String TYPE_WATERMARK_MOOD = "mood";
	public static ArrayList<FilterRepresentation> buildStickerFilterRepresentations(List<WatermarkBean> watermarkBeans) {
        
        ArrayList <FilterRepresentation> stickerList = new ArrayList<FilterRepresentation>(watermarkBeans.size());
        FilterRepresentation rep;
        for (WatermarkBean watermarkBean : watermarkBeans) {
        	rep = new FilterImageStickerRepresentation(watermarkBean);
            stickerList.add(rep);
        }

//            filter.setSerializationName(serializationNames[i]);
//            filter.setName(context.getString(textId[i]));
//            filter.setTextId(textId[i]);
		return stickerList;
	}
	
	public static final int APP_CAMERA = 1;
	public static final int APP_GALLERY = 2;
	
	public static final int WORD_TYPE_EDIT = 0;
	public static final int WORD_TYPE_LOCATION = 2;
	public static final int WORD_TYPE_NONE = 3;
	public static final int WORD_TYPE_TIME = 1;
	public static final int WORD_TYPE_EDIT_MULTE = 8;
	
	
	public static void queryWatermarks(Context context) {
    	ContentResolver resolver = context.getContentResolver();
    	ArrayList<Long> historyWMIds = new ArrayList<Long>();
    	Cursor cursor = resolver.query(Provider.HistoryColumns.CONTENT_URI, null, Provider.HistoryColumns.WHERE_APP_TYPE, new String[] { Integer.toString(APP_GALLERY)}, Provider.HistoryColumns.DEFAULT_SORT_ORDER);
    	if (cursor != null) {
    		try {
                while (cursor.moveToNext()) {
                	final long historyWMId = CursorTool.getCursorLong(cursor, Provider.HistoryColumns.WATERMARK_ID);
                    historyWMIds.add(historyWMId);
                }
            } finally {
            	cursor.close();
            }
    	}
    	cursor = resolver.query(Provider.AlbumColumns.CONTENT_URI, null, Provider.AlbumColumns.WHERE_APP_ID, new String[] { Integer.toString(APP_CAMERA)}, null);
    	if (cursor == null) {
    		return;
    	}
    	Log.i(TAG, "queryWatermarks cursor=" + cursor);
    	try {
    		LongSparseArray<WatermarkBean> historySparseArray = new LongSparseArray<WatermarkBean>(historyWMIds.size());
            while (cursor.moveToNext()) {
            	final long albumId = CursorTool.getCursorLong(cursor, Provider.AlbumColumns._ID);
            	final String key = CursorTool.getCursorString(cursor, Provider.AlbumColumns.KEY);
                ArrayList<WatermarkBean> resources = StickerManager.getStickerManager().getWatermarkBeans(key);
                Cursor subCursor = resolver.query(Provider.WatermarkColumns.CONTENT_URI, null, Provider.WatermarkColumns.WHERE_ALBUM_ID, new String[] { Long.toString(albumId) }, null);
                if (subCursor.moveToFirst()) {
                	for (int i = 0, size = subCursor.getCount(); i < size; i++) {
                		try {
							WatermarkBean entity = getWatermarkResourceByCursor(key, resolver, subCursor, context);
							resources.add(entity);
							if (historyWMIds.contains(entity.getID())) {
								WatermarkBean history = (WatermarkBean) entity.clone();
								history.setType(TYPE_WATERMARK_HOT);
								historySparseArray.put(entity.getID(), history);
							}
						} catch (RemoteException e) {
							e.printStackTrace();
						}
                	}
                }
                Log.i(TAG, "queryWatermarks resources=" + resources);
            }
            for (long id : historyWMIds) { // order
            	StickerManager.getStickerManager().getWatermarkBeans(TYPE_WATERMARK_HOT).add(historySparseArray.get(id));
            }
        } finally {
        	cursor.close();
        }
    }
    
    public static WatermarkBean getWatermarkResourceByCursor(String type, ContentResolver resolver, Cursor cursor, Context context) throws RemoteException {
        // we expect the cursor is already at the row we need to read from
        final long watermarkId = CursorTool.getCursorLong(cursor, Provider.WordResColumns._ID);
        String thumbPath = CursorTool.getCursorString(cursor, Provider.WatermarkColumns.THUMB_PATH);
        boolean isSticker = CursorTool.getCursorInt(cursor, Provider.WatermarkColumns.IS_STICKER) == 1;
        boolean isColorChange = CursorTool.getCursorInt(cursor, Provider.WatermarkColumns.IS_COLOR_CHANGE) == 1;
        ArrayList<IWatermarkResource> resources = new ArrayList<IWatermarkResource>();
        int textColor = Color.BLACK;
        Cursor subCursor;
        subCursor = resolver.query(Provider.PhotoResColumns.CONTENT_URI, null,
        		Provider.PhotoResColumns.WHERE_WATERMARK_ID,
                new String[] { Long.toString(watermarkId) }  /* selectionArgs */,
                null /* sortOrder */);
        try {
            while (subCursor.moveToNext()) {
            	String photoResPath = CursorTool.getCursorString(subCursor, Provider.PhotoResColumns.RES_PATH);
            	float x = CursorTool.getCursorFloat(subCursor, Provider.PhotoResColumns.X);
            	float y = CursorTool.getCursorFloat(subCursor, Provider.PhotoResColumns.Y);
            	PhotoResource photoResource = new PhotoResource(x, y, getResId(photoResPath, context));
            	resources.add(photoResource);
            }
        } finally {
            subCursor.close();
        }

        subCursor = resolver.query(Provider.WordResColumns.CONTENT_URI, null,
        		Provider.WordResColumns.WHERE_WATERMARK_ID,
                new String[] { Long.toString(watermarkId) }  /* selectionArgs */,
                null /* sortOrder */);
        try {
            while (subCursor.moveToNext()) {
                String word = CursorTool.getCursorString(subCursor, Provider.WordResColumns.WORD);
                float x = CursorTool.getCursorFloat(subCursor, Provider.WordResColumns.X);
            	float y = CursorTool.getCursorFloat(subCursor, Provider.WordResColumns.Y);
            	int textSize = CursorTool.getCursorInt(subCursor, Provider.WordResColumns.TEXT_SIZE);
            	String textColorStr = CursorTool.getCursorString(subCursor, Provider.WordResColumns.TEXT_COLOR);
            	try {
            		textColor = Color.parseColor(textColorStr);
				} catch (Exception e) {
				}
            	boolean isV = CursorTool.getCursorInt(subCursor, Provider.WordResColumns.IS_TEXT_VERTICAL) == 1;
            	
            	int align = CursorTool.getCursorInt(subCursor, Provider.WordResColumns.ALIGN);
            	int limit = CursorTool.getCursorInt(subCursor, Provider.WordResColumns.LIMIT);
            	int wordType = CursorTool.getCursorInt(subCursor, Provider.WordResColumns.TYPE);
            	if (wordType == WORD_TYPE_LOCATION) {
            		AddressResource addressResource = new AddressResource(align, x, y, isV, textSize, word);
            		resources.add(addressResource);
            	} else {
            		if (wordType == WORD_TYPE_TIME) {
            			String format = CursorTool.getCursorString(subCursor, Provider.WordResColumns.TIME_FORMAT);
            			word = StickerTool.getDateFormat(format, word);
            			WordResource wordResource = new WordResource(align, x, y, isV, textSize, word, limit, false, true);
                		resources.add(wordResource);
            		} else {
            			boolean isSingle = (wordType != WORD_TYPE_EDIT_MULTE);
            			WordResource wordResource = new WordResource(align, x, y, isV, textSize, word, limit, true, isSingle);
                		resources.add(wordResource);
            		}
            	}
            }
        } finally {
            subCursor.close();
        }
        cursor.moveToNext();
        WatermarkBean watermarkBean = new WatermarkBean(textColor, isSticker, isColorChange, getResId(thumbPath, context), type, watermarkId, resources);
        return watermarkBean;
    }
    
    private static int getResId(String resName, Context context) {
    	return context.getResources().getIdentifier(resName, "drawable", context.getPackageName()); 
    }
    
    public static String getDateFormat(String format, String defaultTime) {
    	SimpleDateFormat formatter = new SimpleDateFormat(format);
    	Date curDate = new Date(System.currentTimeMillis());//获取当前时间
    	String str = formatter.format(curDate);
    	if (TextUtils.isEmpty(str)) {
    		return defaultTime;
    	}
    	return str;
    }
    
}
