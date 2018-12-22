package com.goodix.service;

import com.goodix.database.FpDbOperarionImpl;
import com.goodix.util.ConstantUtil;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.content.UriMatcher;

public class FpDBContentProvider extends ContentProvider{
    public static final Uri CONTENT_URI = Uri.parse(
    		"content://com.goodix.service.fpdbcontentprovider/elements");
    private static final int ALLROWS = 1;
    private static final int SINGLEROW = 2;
    private static final UriMatcher uriMatcher;
    private FpDbOperarionImpl mDao;
    
    static{
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI("com.goodix.service.fpdbcontentprovider",
                "elements", ALLROWS);
        uriMatcher.addURI("com.goodix.service.fpdbcontentprovider",
                "elements/#", SINGLEROW);
    }

    @Override
	public boolean onCreate(){
    	mDao = FpDbOperarionImpl.getInstance(getContext());
		return true;
	}
    
    @Override
	public int delete(Uri uri, String selection, String[] selectionArgs){
		switch (uriMatcher.match(uri)){
		case SINGLEROW:
			/*
			 * String rowID = uri.getPathSegments().get(1);
			 * 
			 * selection = FpDBOpenHelper.KEY + "=" + rowID +
			 * (!TextUtils.isEmpty(selection)?"AND (" + selection + ')':"");
			 */
			break;

		default:
			break;
		}

		if (selection == null){
			selection = "1"; // while(1) delete all items
		}
		
		int deleteCount = mDao.delete(ConstantUtil.FP_INFO_TB_NAME,selection,selectionArgs);
		super.getContext().getContentResolver().notifyChange(uri, null);
		return deleteCount;
	}

	@Override
	public String getType(Uri uri){
		switch (uriMatcher.match(uri)){
		case ALLROWS:
			return "vnd.android.cursor.dir/vnd.goodix.elemental";

		case SINGLEROW:
			return "vnd.android.cursor.item/vnd.goodix.elemental";

		default:
			throw new IllegalArgumentException("Unsupported URI : " + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values){
		long id = mDao.insert(ConstantUtil.FP_INFO_TB_NAME,values);

		if (id > -1){
			Uri insertedID = ContentUris.withAppendedId(CONTENT_URI, id);
			super.getContext().getContentResolver()
			.notifyChange(insertedID, null);
			return insertedID;
		}else{
			return null;
		}
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder){

		switch (uriMatcher.match(uri)){
		case ALLROWS:

			break;

		case SINGLEROW:
			// String rowID = uri.getPathSegments().get(1);
			// queryBuilder.appendWhere(FpDBOpenHelper.KEY + " = " + rowID);
			break;

		default:
			break;

		}

		Cursor cursor = mDao.query(ConstantUtil.FP_INFO_TB_NAME,projection,selection,selectionArgs,sortOrder);
		return cursor;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs){
		switch (uriMatcher.match(uri)){
		case SINGLEROW:
			// String rowID = uri.getPathSegments().get(1);

			// selection = FpDBOpenHelper.KEY + "=" + rowID +
			// (!TextUtils.isEmpty(selection)?"AND (" + selection + ')':"");
			break;

		default:
			break;
		}

		int updateCount = mDao.update(ConstantUtil.FP_INFO_TB_NAME,selection,selectionArgs,values);
		super.getContext().getContentResolver().notifyChange(uri, null);
		return updateCount;
	}
}
