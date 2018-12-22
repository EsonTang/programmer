package com.android.dialer.prize.tmsdkcallmark;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * Created by wangzhong on 2017/5/5.
 */

public class CallMarkCacheDao {
	//prize add by lijimeng TMSDK_Call_Mark 20180906-start
    public interface PrizeUpdateData {
        void prizeUpdataData(String number, String type);
    }
	//prize add by lijimeng TMSDK_Call_Mark 20180906-end
    public static final String TAG = "TMSDK_Call_Mark";

    private Context mContext;
    private CallMarkCacheSQLiteOpenHelper mCallMarkCacheSQLiteOpenHelper;
    private SQLiteDatabase mSQLiteDatabase;
	//prize add by lijimeng TMSDK_Call_Mark 20180906-start
    private static CallMarkCacheDao mCallMarkCacheDao;
	private static PrizeUpdateData mPrizeUpdateData;
	//prize add by lijimeng TMSDK_Call_Mark 20180906-end
    private void showLog(String log) {
        Log.d(TAG, ":::::::::: DAO ::::: " + log);
    }

    public CallMarkCacheDao(Context context) {
        this.mContext = context;
        initCallMarkCacheDao();
    }
	//prize add by lijimeng TMSDK_Call_Mark 20180906-start
    public static CallMarkCacheDao getInstance(Context context, PrizeUpdateData prizeUpdateData) {
        if (mCallMarkCacheDao == null) {
            mCallMarkCacheDao = new CallMarkCacheDao(context);
        }
        if (mPrizeUpdateData == null && prizeUpdateData != null) {
            mPrizeUpdateData = prizeUpdateData;
        }
        return mCallMarkCacheDao;
    }
	//prize add by lijimeng TMSDK_Call_Mark 20180906-end
    private void initCallMarkCacheDao() {
        if (null == mCallMarkCacheSQLiteOpenHelper) {
            mCallMarkCacheSQLiteOpenHelper = new CallMarkCacheSQLiteOpenHelper(mContext);
        }
        if (null == mSQLiteDatabase) {
            mSQLiteDatabase = mCallMarkCacheSQLiteOpenHelper.getReadableDatabase();
        }
    }
	//prize modify by lijimeng TMSDK_Call_Mark 20180906-start
    public Cursor query() {
        initCallMarkCacheDao();
        Cursor cursor = mSQLiteDatabase.query(CallMarkCacheSQLiteOpenHelper.TABLE_NAME,
                new String[]{CallMarkCacheSQLiteOpenHelper.TABLE_COLUMN_ID, CallMarkCacheSQLiteOpenHelper.TABLE_COLUMN_PHONE_NUMBER,
                        CallMarkCacheSQLiteOpenHelper.TABLE_COLUMN_TYPE}, null, null, null, null, null);
        return cursor;
    }
	//prize modify by lijimeng TMSDK_Call_Mark 20180906-end
	//prize modify by lijimeng TMSDK_Call_Mark 20180906-start
    public Cursor query(String phoneNumber) {
        if (null == phoneNumber) {
            return null;
        }
        initCallMarkCacheDao();
        Cursor cursor = mSQLiteDatabase.query(CallMarkCacheSQLiteOpenHelper.TABLE_NAME,
                new String[]{CallMarkCacheSQLiteOpenHelper.TABLE_COLUMN_PHONE_NUMBER, CallMarkCacheSQLiteOpenHelper.TABLE_COLUMN_TYPE},
                CallMarkCacheSQLiteOpenHelper.TABLE_COLUMN_PHONE_NUMBER + "=?", new String[]{phoneNumber},
                null, null, null);
        return cursor;
    }
	//prize modify by lijimeng TMSDK_Call_Mark 20180906-end
	//prize modify by lijimeng TMSDK_Call_Mark 20180906-start
    public void update(String phoneNumber, String type) {
        if (null == phoneNumber) {
            return;
        }
        initCallMarkCacheDao();
        Cursor cursor = query(phoneNumber);
        if (null == cursor || cursor.getCount() < 1) {
            showLog("insert!  phoneNumber : " + phoneNumber + ",  type : " + type);
            insert(phoneNumber, type);
        } else {
            showLog("update!  phoneNumber : " + phoneNumber + ",  type : " + type);
            cursor.moveToFirst();
            if (type.equals(cursor.getString(cursor.getColumnIndex(CallMarkCacheSQLiteOpenHelper.TABLE_COLUMN_TYPE)))) {
                showLog("update type no change return");
                return;
            }
            ContentValues values = new ContentValues();
            values.put(CallMarkCacheSQLiteOpenHelper.TABLE_COLUMN_PHONE_NUMBER, phoneNumber);
            values.put(CallMarkCacheSQLiteOpenHelper.TABLE_COLUMN_TYPE, type);
            mSQLiteDatabase.update(CallMarkCacheSQLiteOpenHelper.TABLE_NAME, values,
                    CallMarkCacheSQLiteOpenHelper.TABLE_COLUMN_PHONE_NUMBER + "=?", new String[]{phoneNumber});
        }
        cursor.close();
        if (mPrizeUpdateData != null) {
            mPrizeUpdateData.prizeUpdataData(phoneNumber, type);
        }
    }
	//prize modify by lijimeng TMSDK_Call_Mark 20180906-end
    private long insert(String phoneNumber, String type) {
        if (null == phoneNumber) {
            return -1;
        }
        initCallMarkCacheDao();
        ContentValues values = new ContentValues();
        values.put(CallMarkCacheSQLiteOpenHelper.TABLE_COLUMN_PHONE_NUMBER, phoneNumber);
        values.put(CallMarkCacheSQLiteOpenHelper.TABLE_COLUMN_TYPE, type);
        return mSQLiteDatabase.insert(CallMarkCacheSQLiteOpenHelper.TABLE_NAME, null, values);
    }
	//prize add by lijimeng TMSDK_Call_Mark 20180906-start
    public void delete(int id) {
        initCallMarkCacheDao();
        showLog("delete!  start id : " + id);
        mSQLiteDatabase.delete(CallMarkCacheSQLiteOpenHelper.TABLE_NAME,
                CallMarkCacheSQLiteOpenHelper.TABLE_COLUMN_ID + "< ?", new String[]{String.valueOf(id + 50)});
    }
	//prize add by lijimeng TMSDK_Call_Mark 20180906-end
    public void close() {
        if (null != mSQLiteDatabase) {
            mSQLiteDatabase.close();
        }
        if (null != mCallMarkCacheSQLiteOpenHelper) {
            mCallMarkCacheSQLiteOpenHelper.close();
        }
		//prize add by lijimeng TMSDK_Call_Mark 20180906-start
        if (mCallMarkCacheDao != null) {
            mCallMarkCacheDao = null;
        }
        if (mPrizeUpdateData != null) {
            mPrizeUpdateData = null;
        }
		//prize add by lijimeng TMSDK_Call_Mark 20180906-end
    }
}
