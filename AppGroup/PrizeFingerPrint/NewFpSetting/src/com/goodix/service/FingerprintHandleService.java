package com.goodix.service;

import java.util.ArrayList;

import com.goodix.model.FpInfo;
import com.goodix.util.Fingerprint;

import android.app.Service;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.os.IBinder;

public class FingerprintHandleService extends Service{
	public static final int DATABASE_MAX_ITEM = 5;
	private Context mContent;

	@Override
	public void onCreate(){
		super.onCreate();
		mContent = getApplicationContext();
	}

	@Override
	public void onDestroy(){
		super.onDestroy();
	}


	@Override
	public boolean onUnbind(Intent intent){
		return super.onUnbind(intent);
	}

	public int getDatabaseSpace(){
		ContentResolver cr = this.getContentResolver();
        Cursor cursor = cr.query(FpDBContentProvider.CONTENT_URI, null, null,
                null, null);
        int count = cursor.getCount();
        if(null != cursor){
			cursor.close();
		}
        return DATABASE_MAX_ITEM - count;
	}

	@Override
	public IBinder onBind(Intent arg0){
		return new ServiceBinder();
	}

	public ArrayList<Fingerprint> query(){
		ArrayList<Fingerprint> list = new ArrayList<Fingerprint>();
        ContentResolver cr = this.getContentResolver();
        Cursor cursor;
        Fingerprint fp = null;
        
        String[] projection = null; //  return all finger
        String selection = null;
        String[] selectionArgs = null;
        String sortOrder = null;
        
        cursor = cr.query(FpDBContentProvider.CONTENT_URI, projection,
                selection, selectionArgs, sortOrder);
        int count = cursor.getCount();
        if (0 != count){
            if (null != cursor){
                cursor.moveToFirst();
                do{
                    fp = new Fingerprint(cursor.getInt(0), cursor.getString(1),
                            cursor.getString(2), cursor.getString(3));
                    list.add(fp);
                }while (cursor.moveToNext());
            }// very importance.
        }
        if(null != cursor){
			cursor.close();
		}
		return list;
	}

	public Fingerprint match(String path){
		return null;
	}

	public boolean match(String srcPath, String dstPath){
		return true;
	}

	public boolean insert(Fingerprint mFp){
		ContentResolver cr = this.getContentResolver();
        ContentValues fpItem = new ContentValues();
        fpItem.put(FpInfo.ID, mFp.getKey());
        fpItem.put(FpInfo.NAME, mFp.getName());
        fpItem.put(FpInfo.DESCRIPTION, mFp.getDescription());
        fpItem.put(FpInfo.URI, mFp.getUri());
        if (cr.insert(FpDBContentProvider.CONTENT_URI, fpItem) != null){
            // this.mDatabaseBCount++;
            return true;
        }
        return false;
	}

	public boolean update(Fingerprint mFp){
		ContentResolver cr = this.getContentResolver();
        String[] selectionArgs = null;
        String where = FpInfo.ID + "=" + mFp.getKey();
        ContentValues values = new ContentValues();
        values.put(FpInfo.ID, mFp.getKey());
        values.put(FpInfo.NAME, mFp.getName());
        values.put(FpInfo.DESCRIPTION, mFp.getDescription());
        values.put(FpInfo.URI, mFp.getUri());
        if (0 != cr.update(FpDBContentProvider.CONTENT_URI, values, where,
                selectionArgs))
            return true; // return update item > 0, make sure that database was updated
        return false;
	}

	public boolean delete(int mKey){
		ContentResolver cr = this.getContentResolver();
        String[] selectionArgs = null;
        String where = FpInfo.ID + "=" + mKey;
        if (0 != cr.delete(FpDBContentProvider.CONTENT_URI, where,
                selectionArgs)){
            return true;
        }
        return false;
	}

	public class ServiceBinder extends Binder{
		public FingerprintHandleService getService(){
			return FingerprintHandleService.this;
		}
	}
}
