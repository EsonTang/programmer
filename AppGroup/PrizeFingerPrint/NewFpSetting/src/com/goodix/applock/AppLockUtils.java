package com.goodix.applock;

import com.goodix.database.FpDbOperarionImpl;
import com.goodix.util.ConstantUtil;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

public class AppLockUtils {
	private static final String TAG = "AppLockUtils";

	public static final String  ID= "_id";
	public static final String  PKG_NAME= "pkgName";
	public static final String  CLASS_NAME= "className";
	public static final String  NEED_LOCK= "needLock";
	public static final String  ALREADY_UNLOCKER= "alreadyUnlocked";
	private static AppLockUtils sAppLockUtils;

	private static FpDbOperarionImpl mDao;


	private AppLockUtils(Context context) {
	}

	public static AppLockUtils getInstance(Context context) {
		if (sAppLockUtils == null) {
			sAppLockUtils = new AppLockUtils(context);
			mDao= FpDbOperarionImpl.getInstance(context);
		}
		return sAppLockUtils;
	}		

	public boolean isAppAlreadyUnlocked(String pkg) {
		boolean isUnLocked = false;
		Cursor cursor= mDao.query(ConstantUtil.APP_LOCK_TB_NAME, null, PKG_NAME + "=?"+" and "+ ALREADY_UNLOCKER + "=?", 
				new String[]{pkg,String.valueOf(1)}, null);
		while(null != cursor && cursor.moveToFirst()){
			isUnLocked = true;
			break;
		}
		if(null != cursor){
			cursor.close();
		}
		return isUnLocked;
	}

	public void setAppAlreadyLocked(String pkg) {
		ContentValues values = new ContentValues();
		values.put(ALREADY_UNLOCKER, 0);
		mDao.update(ConstantUtil.APP_LOCK_TB_NAME, PKG_NAME + "=?", new String[]{pkg},values);
	}

	public Cursor getAllLockedApp() {
		Cursor cursor= mDao.query(ConstantUtil.APP_LOCK_TB_NAME, null, NEED_LOCK + "=?", new String[]{String.valueOf(1)}, null);
		return cursor;
	}

	public void setAppAlreadyUnLocked(String pkg) {
		ContentValues values = new ContentValues();
		values.put(ALREADY_UNLOCKER, 1);
		mDao.update(ConstantUtil.APP_LOCK_TB_NAME, PKG_NAME + "=?", new String[]{pkg}, values);
	}	

	public boolean isAppNeedLock(String pkg) {
		String selection =  PKG_NAME + "=?" +" and " + NEED_LOCK + "=?";
		String[] selectionArgs = new String[]{pkg,String.valueOf(1)};
		Cursor cursor= mDao.query(ConstantUtil.APP_LOCK_TB_NAME, null, selection, selectionArgs, null);
		boolean isNeedLock = false;
		while(null != cursor && cursor.moveToFirst()){
			Log.d(TAG, "isAppNeedLock()  " + cursor.getInt(cursor.getColumnIndex(NEED_LOCK)));
			isNeedLock = true;
			break;
		}
		if(null != cursor){
			cursor.close();
		}
		return isNeedLock;
	}

}
