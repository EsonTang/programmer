package com.goodix.util;

import com.goodix.database.FpDbOperarionImpl;
import com.goodix.model.AppLockEvent;

import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

public class DataUtil{
    private FpDbOperarionImpl mDao;

	public DataUtil(Context context) {
    	mDao = FpDbOperarionImpl.getInstance(context);
    }

	public boolean getAppLockState(ComponentName componentName) {
    	String currentPkgName = componentName.getPackageName();
    	String selection = AppLockEvent.PKG_NAME+"=?"+" and "+AppLockEvent.NEED_LOCK+"=?";
		String[] selectionArgs = new String[]{currentPkgName,String.valueOf(1)}; 
		Cursor cursor = mDao.query(ConstantUtil.APP_LOCK_TB_NAME,null,selection,selectionArgs,null);
		boolean isLock = false;
		while(null != cursor && cursor.moveToFirst()){
    			isLock =  true;
    			break;
    	}
		if(null != cursor){
			cursor.close();
		}
        return isLock;
    }

    public void storeLockApp(ComponentName componentName) {
    	String currentPkgName = componentName.getPackageName();
        ContentValues values = new ContentValues();
        values.put(AppLockEvent.PKG_NAME, componentName.getPackageName());
        values.put(AppLockEvent.CLASS_NAME, componentName.getClassName());
        values.put(AppLockEvent.NEED_LOCK, 1);
        String selection = AppLockEvent.PKG_NAME+"=?";
        String[] selectionArgs = new String[]{currentPkgName}; 
        mDao.update(ConstantUtil.APP_LOCK_TB_NAME, selection, selectionArgs, values);
    }
    
    public void removeLockApp(ComponentName componentName) {
    	String currentPkgName = componentName.getPackageName();
        ContentValues values = new ContentValues();
        values.put(AppLockEvent.PKG_NAME, componentName.getPackageName());
        values.put(AppLockEvent.CLASS_NAME, componentName.getClassName());
        values.put(AppLockEvent.NEED_LOCK, 0);
        String selection = AppLockEvent.PKG_NAME+"=?";
        String[] selectionArgs = new String[]{currentPkgName}; 
        mDao.update(ConstantUtil.APP_LOCK_TB_NAME, selection, selectionArgs, values);
    }
    
    public void setAppAlreadyUnLocked(String pkgName){
    	String selection = AppLockEvent.PKG_NAME+"=?";
		String[] selectionArgs = new String[]{pkgName}; 
		Cursor cursor = mDao.query(ConstantUtil.APP_LOCK_TB_NAME,null,selection,selectionArgs,null);
    	while(null != cursor && cursor.moveToFirst()){
    		ContentValues values = new ContentValues();
    		values.put(AppLockEvent.ALREADY_UNLOCKER, 1);
    		mDao.update(ConstantUtil.APP_LOCK_TB_NAME,selection,selectionArgs,values);
    		break;
    	}
    	if(null != cursor){
			cursor.close();
		}
    }
    
    public void setAppAlreadyLocked(String pkgName){
    	String selection = AppLockEvent.PKG_NAME+"=?";
		String[] selectionArgs = new String[]{pkgName}; 
		Cursor cursor = mDao.query(ConstantUtil.APP_LOCK_TB_NAME,null,selection,selectionArgs,null);
    	while(null != cursor && cursor.moveToFirst()){
    		ContentValues values = new ContentValues();
    		values.put(AppLockEvent.ALREADY_UNLOCKER, 0);
    		mDao.update(ConstantUtil.APP_LOCK_TB_NAME,selection,selectionArgs,values);
    		break;
    	}
    	if(null != cursor){
			cursor.close();
		}
    }
}
