/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein
 * is confidential and proprietary to MediaTek Inc. and/or its licensors.
 * Without the prior written permission of MediaTek inc. and/or its licensors,
 * any reproduction, modification, use or disclosure of MediaTek Software,
 * and information contained herein, in whole or in part, shall be strictly prohibited.
 *
 * MediaTek Inc. (C) 2010. All rights reserved.
 *
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER ON
 * AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL WARRANTIES,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NONINFRINGEMENT.
 * NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH RESPECT TO THE
 * SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY, INCORPORATED IN, OR
 * SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES TO LOOK ONLY TO SUCH
 * THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO. RECEIVER EXPRESSLY ACKNOWLEDGES
 * THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES
 * CONTAINED IN MEDIATEK SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK
 * SOFTWARE RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S ENTIRE AND
 * CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE RELEASED HEREUNDER WILL BE,
 * AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE MEDIATEK SOFTWARE AT ISSUE,
 * OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE CHARGE PAID BY RECEIVER TO
 * MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek Software")
 * have been modified by MediaTek Inc. All revisions are subject to any receiver's
 * applicable license agreements with MediaTek Inc.
 */

package com.android.providers.settings.fingerprint;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDiskIOException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;


import java.util.HashMap;

public class PrizeFpShareProvider extends ContentProvider {

    private static final String TAG = "PrizeFpShareProvider";

    public static final String DATABASE_NAME = "fp.db";

    public static final int DATABASE_VERSION = 2;

    // Column projection map
    private static HashMap<String, String> mLockAppProjectionMap = new HashMap<String, String>();
    private static HashMap<String, String> mFpFunctProjectionMap = new HashMap<String, String>();
    /*PRIZE-Add-M_Fingerprint-wangzhong-2016_6_28-start*/
    private static HashMap<String, String> mLockAppCipherProjectionMap = new HashMap<String, String>();
    private static HashMap<String, String> mOperationProjectionMap = new HashMap<String, String>();
    /*PRIZE-Add-M_Fingerprint-wangzhong-2016_6_28-end*/
    static {
    	mLockAppProjectionMap.put(PrizeAppLockMetaData._ID, PrizeAppLockMetaData._ID);
    	mLockAppProjectionMap.put(PrizeAppLockMetaData.PKG_NAME,PrizeAppLockMetaData.PKG_NAME);
    	mLockAppProjectionMap.put(PrizeAppLockMetaData.CLASS_NAME,PrizeAppLockMetaData.CLASS_NAME);
    	mLockAppProjectionMap.put(PrizeAppLockMetaData.LOCK_STATUS,PrizeAppLockMetaData.LOCK_STATUS);
    	mLockAppProjectionMap.put(PrizeAppLockMetaData.LOCK_STATUS_SETTINGS,PrizeAppLockMetaData.LOCK_STATUS_SETTINGS);/*PRIZE-Add-M_Fingerprint-wangzhong-2016_6_28*/

    	mFpFunctProjectionMap.put(PrizeFpFuntionMetaData._ID, PrizeFpFuntionMetaData._ID);
    	mFpFunctProjectionMap.put(PrizeFpFuntionMetaData.FUNCTION_NAME,PrizeFpFuntionMetaData.FUNCTION_NAME);
    	mFpFunctProjectionMap.put(PrizeFpFuntionMetaData.FUNCTION_STATUS,PrizeFpFuntionMetaData.FUNCTION_STATUS);

        /*PRIZE-Add-M_Fingerprint-wangzhong-2016_6_28-start*/
        mLockAppCipherProjectionMap.put(PrizeAppLockCipherMetaData._ID, PrizeAppLockCipherMetaData._ID);
        mLockAppCipherProjectionMap.put(PrizeAppLockCipherMetaData.CIPHER_1,PrizeAppLockCipherMetaData.CIPHER_1);
	 mLockAppCipherProjectionMap.put(PrizeAppLockCipherMetaData.CIPHER_TYPE,PrizeAppLockCipherMetaData.CIPHER_TYPE);
        mLockAppCipherProjectionMap.put(PrizeAppLockCipherMetaData.CIPHER_STATUS,PrizeAppLockCipherMetaData.CIPHER_STATUS);

        mOperationProjectionMap.put(PrizeFingerprintOperationMetaData._ID, PrizeFingerprintOperationMetaData._ID);
        mOperationProjectionMap.put(PrizeFingerprintOperationMetaData.OPERATION_NAME,PrizeFingerprintOperationMetaData.OPERATION_NAME);
        mOperationProjectionMap.put(PrizeFingerprintOperationMetaData.OPERATION_STATUS,PrizeFingerprintOperationMetaData.OPERATION_STATUS);
        mOperationProjectionMap.put(PrizeFingerprintOperationMetaData.OPERATION_EVENT_TYPE,PrizeFingerprintOperationMetaData.OPERATION_EVENT_TYPE);
        mOperationProjectionMap.put(PrizeFingerprintOperationMetaData.OPERATION_EVENT_STATUS,PrizeFingerprintOperationMetaData.OPERATION_EVENT_STATUS);
        /*PRIZE-Add-M_Fingerprint-wangzhong-2016_6_28-end*/
    }

    // Uri Matcher
    private static final UriMatcher URI_MATCHER;

    private static final int LOCK_APP_MULTIPLE_TASK_URI = 1;
    private static final int LOCK_APP_SINGLE_TASK_URI = 2;
    
    private static final int FP_FUNCTION_MULTIPLE_TASK_URI = 3;
    private static final int FP_FUNCTION_SINGLE_TASK_URI = 4;

    /*PRIZE-Add-M_Fingerprint-wangzhong-2016_6_28-start*/
    private static final int LOCK_APP_CIPHER_MULTIPLE_TASK_URI = 5;
    private static final int LOCK_APP_CIPHER_SINGLE_TASK_URI = 6;

    private static final int OPERATION_MULTIPLE_TASK_URI = 7;
    private static final int OPERATION_SINGLE_TASK_URI = 8;
    /*PRIZE-Add-M_Fingerprint-wangzhong-2016_6_28-end*/

    static {
        URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
        URI_MATCHER.addURI(PrizeAppLockMetaData.AUTHORITY, 
        		PrizeAppLockMetaData.TABLE_NAME,LOCK_APP_MULTIPLE_TASK_URI);
        URI_MATCHER.addURI(PrizeAppLockMetaData.AUTHORITY, 
        		PrizeAppLockMetaData.TABLE_NAME + "/#",LOCK_APP_SINGLE_TASK_URI);
        
        URI_MATCHER.addURI(PrizeFpFuntionMetaData.AUTHORITY, 
        		PrizeFpFuntionMetaData.TABLE_NAME,FP_FUNCTION_MULTIPLE_TASK_URI);
        URI_MATCHER.addURI(PrizeFpFuntionMetaData.AUTHORITY, 
        		PrizeFpFuntionMetaData.TABLE_NAME + "/#",FP_FUNCTION_SINGLE_TASK_URI);

        /*PRIZE-Add-M_Fingerprint-wangzhong-2016_6_28-start*/
        URI_MATCHER.addURI(PrizeAppLockCipherMetaData.AUTHORITY,
                PrizeAppLockCipherMetaData.TABLE_NAME,LOCK_APP_CIPHER_MULTIPLE_TASK_URI);
        URI_MATCHER.addURI(PrizeAppLockCipherMetaData.AUTHORITY,
                PrizeAppLockCipherMetaData.TABLE_NAME + "/#",LOCK_APP_CIPHER_SINGLE_TASK_URI);

        URI_MATCHER.addURI(PrizeFingerprintOperationMetaData.AUTHORITY,
                PrizeFingerprintOperationMetaData.TABLE_NAME,OPERATION_MULTIPLE_TASK_URI);
        URI_MATCHER.addURI(PrizeFingerprintOperationMetaData.AUTHORITY,
                PrizeFingerprintOperationMetaData.TABLE_NAME + "/#",OPERATION_SINGLE_TASK_URI);
        /*PRIZE-Add-M_Fingerprint-wangzhong-2016_6_28-end*/
    }

    private FpDatabaseHelper mDbHelper;

    @Override
    public boolean onCreate() {
        mDbHelper = FpDatabaseHelper.getInstance(getContext());
        return true;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        checkWritePermissions();

        SQLiteDatabase mDb = mDbHelper.getWritableDatabase();
        int count = 0;
        String rowId = null;
        switch (URI_MATCHER.match(uri)) {
        case LOCK_APP_MULTIPLE_TASK_URI:
            count = mDb.delete(PrizeAppLockMetaData.TABLE_NAME, selection,selectionArgs);
            break;
        case LOCK_APP_SINGLE_TASK_URI:
            rowId = uri.getPathSegments().get(1);
            count = mDb.delete(PrizeAppLockMetaData.TABLE_NAME,
            		PrizeAppLockMetaData._ID+ "="+ rowId+ (!TextUtils.isEmpty(selection) ? " AND ("
                                    + selection + ')' : ""), selectionArgs);
            break;
        case FP_FUNCTION_MULTIPLE_TASK_URI:
        	count = mDb.delete(PrizeFpFuntionMetaData.TABLE_NAME, selection,selectionArgs);
        	break;
        case FP_FUNCTION_SINGLE_TASK_URI:
        	rowId = uri.getPathSegments().get(1);
            count = mDb.delete(PrizeFpFuntionMetaData.TABLE_NAME,
            		PrizeFpFuntionMetaData._ID+ "="+ rowId+ (!TextUtils.isEmpty(selection) ? " AND ("
                                    + selection + ')' : ""), selectionArgs);
        	break;
        /*PRIZE-Add-M_Fingerprint-wangzhong-2016_6_28-start*/
        case LOCK_APP_CIPHER_MULTIPLE_TASK_URI:
            count = mDb.delete(PrizeAppLockCipherMetaData.TABLE_NAME, selection,selectionArgs);
            break;
        case LOCK_APP_CIPHER_SINGLE_TASK_URI:
            rowId = uri.getPathSegments().get(1);
            count = mDb.delete(PrizeAppLockCipherMetaData.TABLE_NAME,
                    PrizeAppLockCipherMetaData._ID+ "="+ rowId+ (!TextUtils.isEmpty(selection) ? " AND ("
                            + selection + ')' : ""), selectionArgs);
            break;

        case OPERATION_MULTIPLE_TASK_URI:
            count = mDb.delete(PrizeFingerprintOperationMetaData.TABLE_NAME, selection,selectionArgs);
            break;
        case OPERATION_SINGLE_TASK_URI:
            rowId = uri.getPathSegments().get(1);
            count = mDb.delete(PrizeFingerprintOperationMetaData.TABLE_NAME,
                    PrizeFingerprintOperationMetaData._ID+ "="+ rowId+ (!TextUtils.isEmpty(selection) ? " AND ("
                            + selection + ')' : ""), selectionArgs);
            break;
        /*PRIZE-Add-M_Fingerprint-wangzhong-2016_6_28-end*/
        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
        // getContext().getContentResolver().notifyChange(uri, null);
        sendNotify(uri);
        return count;
    }

    @Override
    public String getType(Uri uri) {
        switch (URI_MATCHER.match(uri)) {
        case LOCK_APP_MULTIPLE_TASK_URI:
            return PrizeAppLockMetaData.CONTENT_TYPE;
        case LOCK_APP_SINGLE_TASK_URI:
            return PrizeAppLockMetaData.CONTENT_ITEM_TYPE;
        case FP_FUNCTION_MULTIPLE_TASK_URI:
        	return PrizeFpFuntionMetaData.CONTENT_TYPE;
        case FP_FUNCTION_SINGLE_TASK_URI:
        	return PrizeFpFuntionMetaData.CONTENT_ITEM_TYPE;
        /*PRIZE-Add-M_Fingerprint-wangzhong-2016_6_28-start*/
        case LOCK_APP_CIPHER_MULTIPLE_TASK_URI:
            return PrizeAppLockCipherMetaData.CONTENT_TYPE;
        case LOCK_APP_CIPHER_SINGLE_TASK_URI:
            return PrizeAppLockCipherMetaData.CONTENT_ITEM_TYPE;

        case OPERATION_MULTIPLE_TASK_URI:
            return PrizeFingerprintOperationMetaData.CONTENT_TYPE;
        case OPERATION_SINGLE_TASK_URI:
            return PrizeFingerprintOperationMetaData.CONTENT_ITEM_TYPE;
        /*PRIZE-Add-M_Fingerprint-wangzhong-2016_6_28-end*/
        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    /**
     * Make sure the caller has permission to write this data.
     */
    private void checkWritePermissions() {
        if (getContext().checkCallingOrSelfPermission(
                android.Manifest.permission.WRITE_SECURE_SETTINGS)
                != PackageManager.PERMISSION_GRANTED) {
            throw new SecurityException(
                    String.format(
                            "Permission denial: writing to secure settings requires %1$s",
                            android.Manifest.permission.WRITE_SECURE_SETTINGS));
        }
    }

    @Override
    public Uri insert(Uri url, ContentValues initialValues) {
        switch (URI_MATCHER.match(url)) {
        case LOCK_APP_SINGLE_TASK_URI:
        case FP_FUNCTION_SINGLE_TASK_URI:
        /*PRIZE-Add-M_Fingerprint-wangzhong-2016_6_28-start*/
        case LOCK_APP_CIPHER_SINGLE_TASK_URI:

        case OPERATION_SINGLE_TASK_URI:
        /*PRIZE-Add-M_Fingerprint-wangzhong-2016_6_28-end*/
        	throw new IllegalArgumentException("Invalid URI: " + url);
        }

        checkWritePermissions();

        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        
        long rowId = 0;
        switch (URI_MATCHER.match(url)) {
        case LOCK_APP_MULTIPLE_TASK_URI:
        	rowId = db.insert(PrizeAppLockMetaData.TABLE_NAME, null,initialValues);
            if (rowId <= 0) {
                return null;
            }
            Log.v("@M_" + TAG, PrizeAppLockMetaData.TABLE_NAME + " <- " + initialValues);
            url = ContentUris.withAppendedId(PrizeAppLockMetaData.CONTENT_URI,rowId);
            getContext().getContentResolver().notifyChange(PrizeAppLockMetaData.CONTENT_URI, null);
        	break;
        case FP_FUNCTION_MULTIPLE_TASK_URI:
        	rowId = db.insert(PrizeFpFuntionMetaData.TABLE_NAME, null,initialValues);
            if (rowId <= 0) {
                return null;
            }
            Log.v("@M_" + TAG, PrizeFpFuntionMetaData.TABLE_NAME + " <- " + initialValues);
            url = ContentUris.withAppendedId(PrizeFpFuntionMetaData.CONTENT_URI,rowId);
            getContext().getContentResolver().notifyChange(PrizeFpFuntionMetaData.CONTENT_URI, null);
        	break;
        /*PRIZE-Add-M_Fingerprint-wangzhong-2016_6_28-start*/
        case LOCK_APP_CIPHER_MULTIPLE_TASK_URI:
            rowId = db.insert(PrizeAppLockCipherMetaData.TABLE_NAME, null,initialValues);
            if (rowId <= 0) {
                return null;
            }
            Log.v("@M_" + TAG, PrizeAppLockCipherMetaData.TABLE_NAME + " <- " + initialValues);
            url = ContentUris.withAppendedId(PrizeAppLockCipherMetaData.CONTENT_URI,rowId);
            getContext().getContentResolver().notifyChange(PrizeAppLockCipherMetaData.CONTENT_URI, null);
            break;

        case OPERATION_MULTIPLE_TASK_URI:
            rowId = db.insert(PrizeFingerprintOperationMetaData.TABLE_NAME, null,initialValues);
            if (rowId <= 0) {
                return null;
            }
            Log.v("@M_" + TAG, PrizeFingerprintOperationMetaData.TABLE_NAME + " <- " + initialValues);
            url = ContentUris.withAppendedId(PrizeFingerprintOperationMetaData.CONTENT_URI,rowId);
            getContext().getContentResolver().notifyChange(PrizeFingerprintOperationMetaData.CONTENT_URI, null);
            break;
        /*PRIZE-Add-M_Fingerprint-wangzhong-2016_6_28-end*/
        }

        sendNotify(url);
        return url;
    }

    /**
     * Send a notification when a particular content URI changes.
     * @param uri
     *            to send notifications for
     */
    private void sendNotify(Uri uri) {
        // Now send the notification through the content framework.
        String notify = uri.getQueryParameter("notify");
        if (notify == null || "true".equals(notify)) {
            getContext().getContentResolver().notifyChange(uri, null);
            Log.v("@M_" + TAG, "notifying: " + uri);
        } else {
            Log.v("@M_" + TAG, "notification suppressed: " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {
    	SQLiteDatabase mDb = mDbHelper.getReadableDatabase();
        try {
            SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
            switch (URI_MATCHER.match(uri)) {
            case LOCK_APP_MULTIPLE_TASK_URI:
            	qb.setTables(PrizeAppLockMetaData.TABLE_NAME);
                qb.setProjectionMap(mLockAppProjectionMap);
            	break;
            case LOCK_APP_SINGLE_TASK_URI:
            	qb.setTables(PrizeAppLockMetaData.TABLE_NAME);
                qb.setProjectionMap(mLockAppProjectionMap);
            	qb.appendWhere(PrizeAppLockMetaData._ID + "="+ uri.getPathSegments().get(1));
            	break;
            case FP_FUNCTION_MULTIPLE_TASK_URI:
            	qb.setTables(PrizeFpFuntionMetaData.TABLE_NAME);
                qb.setProjectionMap(mFpFunctProjectionMap);
            	break;
            case FP_FUNCTION_SINGLE_TASK_URI:
            	qb.setTables(PrizeFpFuntionMetaData.TABLE_NAME);
                qb.setProjectionMap(mFpFunctProjectionMap);
                qb.appendWhere(PrizeFpFuntionMetaData._ID + "="+ uri.getPathSegments().get(1));
            	break;
            /*PRIZE-Add-M_Fingerprint-wangzhong-2016_6_28-start*/
            case LOCK_APP_CIPHER_MULTIPLE_TASK_URI:
                qb.setTables(PrizeAppLockCipherMetaData.TABLE_NAME);
                qb.setProjectionMap(mLockAppCipherProjectionMap);
                break;
            case LOCK_APP_CIPHER_SINGLE_TASK_URI:
                qb.setTables(PrizeAppLockCipherMetaData.TABLE_NAME);
                qb.setProjectionMap(mLockAppCipherProjectionMap);
                qb.appendWhere(PrizeAppLockCipherMetaData._ID + "="+ uri.getPathSegments().get(1));
                break;

            case OPERATION_MULTIPLE_TASK_URI:
                qb.setTables(PrizeFingerprintOperationMetaData.TABLE_NAME);
                qb.setProjectionMap(mOperationProjectionMap);
                break;
            case OPERATION_SINGLE_TASK_URI:
                qb.setTables(PrizeFingerprintOperationMetaData.TABLE_NAME);
                qb.setProjectionMap(mOperationProjectionMap);
                qb.appendWhere(PrizeFingerprintOperationMetaData._ID + "="+ uri.getPathSegments().get(1));
                break;
            /*PRIZE-Add-M_Fingerprint-wangzhong-2016_6_28-end*/
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
            }
            
            Cursor c = qb.query(mDb, projection, selection, selectionArgs,null, null, null);
            if (c != null) {
                c.setNotificationUri(getContext().getContentResolver(), uri);
            }
            return c;
        } catch (SQLiteDiskIOException e) {
            Log.e("@M_" + TAG, e.toString());
            return null;
        }
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
            String[] selectionArgs) {

        checkWritePermissions();

        // update database
        SQLiteDatabase mDb = mDbHelper.getWritableDatabase();

        int count = 0;
        String rowId = null;
        switch (URI_MATCHER.match(uri)) {
        case LOCK_APP_MULTIPLE_TASK_URI:
            count = mDb.update(PrizeAppLockMetaData.TABLE_NAME, values,selection, selectionArgs);
            break;
        case LOCK_APP_SINGLE_TASK_URI:
            rowId = uri.getPathSegments().get(1);
            count = mDb.update(PrizeAppLockMetaData.TABLE_NAME,values,
            		PrizeAppLockMetaData._ID+ "="+ rowId+ (!TextUtils.isEmpty(selection) ? " AND ("
                                    + selection + ')' : ""), selectionArgs);
            break;
        case FP_FUNCTION_MULTIPLE_TASK_URI:
        	count = mDb.update(PrizeFpFuntionMetaData.TABLE_NAME, values,selection, selectionArgs);
        	break;
        case FP_FUNCTION_SINGLE_TASK_URI:
        	rowId = uri.getPathSegments().get(1);
            count = mDb.update(PrizeFpFuntionMetaData.TABLE_NAME,values,
            		PrizeFpFuntionMetaData._ID+ "="+ rowId+ (!TextUtils.isEmpty(selection) ? " AND ("
                                    + selection + ')' : ""), selectionArgs);
        	break;
        /*PRIZE-Add-M_Fingerprint-wangzhong-2016_6_28-start*/
        case LOCK_APP_CIPHER_MULTIPLE_TASK_URI:
            count = mDb.update(PrizeAppLockCipherMetaData.TABLE_NAME, values,selection, selectionArgs);
            break;
        case LOCK_APP_CIPHER_SINGLE_TASK_URI:
            rowId = uri.getPathSegments().get(1);
            count = mDb.update(PrizeAppLockCipherMetaData.TABLE_NAME,values,
                    PrizeAppLockCipherMetaData._ID+ "="+ rowId+ (!TextUtils.isEmpty(selection) ? " AND ("
                                    + selection + ')' : ""), selectionArgs);
            break;

        case OPERATION_MULTIPLE_TASK_URI:
            count = mDb.update(PrizeFingerprintOperationMetaData.TABLE_NAME, values,selection, selectionArgs);
            break;
        case OPERATION_SINGLE_TASK_URI:
            rowId = uri.getPathSegments().get(1);
            count = mDb.update(PrizeFingerprintOperationMetaData.TABLE_NAME,values,
                    PrizeFingerprintOperationMetaData._ID+ "="+ rowId+ (!TextUtils.isEmpty(selection) ? " AND ("
                                    + selection + ')' : ""), selectionArgs);
            break;
        /*PRIZE-Add-M_Fingerprint-wangzhong-2016_6_28-end*/
        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
        // getContext().getContentResolver().notifyChange(uri, null);
        sendNotify(uri);
        return count;
    }

    private static class FpDatabaseHelper extends SQLiteOpenHelper {

    	private static FpDatabaseHelper sSingleton;
    	
    	private FpDatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }
    	
    	public static synchronized FpDatabaseHelper getInstance(Context context) {
            if (sSingleton == null) {
                sSingleton = new FpDatabaseHelper(context);
            }
            return sSingleton;
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
        	bootstrapDB(db);
        }
        
        @Override
        public void onOpen(SQLiteDatabase db) {
            super.onOpen(db);

            Log.i(TAG, "Using schema version: " + db.getVersion());

//            if (!Build.VERSION.INCREMENTAL.equals(getBuildVersion(db))) {
//                Log.w(TAG, "Index needs to be rebuilt as build-version is not the same");
//                reconstruct(db);
//            } else {
//                Log.i(TAG, "Index is fine");
//            }
        }
        
        private void bootstrapDB(SQLiteDatabase db) {
        	db.execSQL("CREATE TABLE IF NOT EXISTS " + PrizeAppLockMetaData.TABLE_NAME 
            		+ " (" + PrizeAppLockMetaData._ID+ " INTEGER PRIMARY KEY,"
                    + PrizeAppLockMetaData.PKG_NAME + " TEXT,"
                    + PrizeAppLockMetaData.CLASS_NAME + " TEXT,"
                    + PrizeAppLockMetaData.LOCK_STATUS + " INTEGER DEFAULT(0),"
                    + PrizeAppLockMetaData.LOCK_STATUS_SETTINGS + " INTEGER DEFAULT(0));");/*PRIZE-Add-M_Fingerprint-wangzhong-2016_6_28*/

            db.execSQL("CREATE TABLE IF NOT EXISTS " + PrizeFpFuntionMetaData.TABLE_NAME 
            		+ " (" + PrizeFpFuntionMetaData._ID+ " INTEGER PRIMARY KEY,"
                    + PrizeFpFuntionMetaData.FUNCTION_NAME + " TEXT,"
                    + PrizeFpFuntionMetaData.FUNCTION_STATUS + " INTEGER DEFAULT(0));");

            /*PRIZE-Add-M_Fingerprint-wangzhong-2016_6_28-start*/
            db.execSQL("CREATE TABLE IF NOT EXISTS " + PrizeAppLockCipherMetaData.TABLE_NAME
                    + " (" + PrizeAppLockCipherMetaData._ID+ " INTEGER PRIMARY KEY,"
                    + PrizeAppLockCipherMetaData.CIPHER_1 + " TEXT,"
                    + PrizeAppLockCipherMetaData.CIPHER_TYPE + " INTEGER,"
                    + PrizeAppLockCipherMetaData.CIPHER_STATUS + " INTEGER DEFAULT(0));");

            db.execSQL("CREATE TABLE IF NOT EXISTS " + PrizeFingerprintOperationMetaData.TABLE_NAME
                    + " (" + PrizeFingerprintOperationMetaData._ID+ " INTEGER PRIMARY KEY,"
                    + PrizeFingerprintOperationMetaData.OPERATION_NAME + " TEXT,"
                    + PrizeFingerprintOperationMetaData.OPERATION_STATUS + " INTEGER DEFAULT(0),"
                    + PrizeFingerprintOperationMetaData.OPERATION_EVENT_TYPE + " TEXT,"
                    + PrizeFingerprintOperationMetaData.OPERATION_EVENT_STATUS + " INTEGER DEFAULT(0));");
            /*PRIZE-Add-M_Fingerprint-wangzhong-2016_6_28-end*/

            /*PRIZE-Change-M_Fingerprint-wangzhong-2016_8_11-start*/
            /*String insertOneFuncSql= "INSERT INTO " + PrizeFpFuntionMetaData.TABLE_NAME + " ('" 
            		+ PrizeFpFuntionMetaData.FUNCTION_NAME + "'," + PrizeFpFuntionMetaData.FUNCTION_STATUS +") "
            		+ "VALUES ('" + PrizeFpFuntionMetaData.FP_LOCK_SCREEN_FC + "'," + 0 + ")";*/
            /*String insertOneFuncSql= "INSERT INTO " + PrizeFpFuntionMetaData.TABLE_NAME + " ('" 
            		+ PrizeFpFuntionMetaData.FUNCTION_NAME + "'," + PrizeFpFuntionMetaData.FUNCTION_STATUS +") "
            		+ "VALUES ('" + PrizeFpFuntionMetaData.FP_LOCK_SCREEN_FC + "'," + 1 + ")";*/
            /*PRIZE-Change-M_Fingerprint-wangzhong-2016_8_11-end*/
            String insertTwoFuncSql= "INSERT INTO " + PrizeFpFuntionMetaData.TABLE_NAME + " ('" 
            		+ PrizeFpFuntionMetaData.FUNCTION_NAME + "'," + PrizeFpFuntionMetaData.FUNCTION_STATUS +") "
            		+ "VALUES ('" + PrizeFpFuntionMetaData.APP_LOCK_FC + "'," + PrizeFpFuntionMetaData.APP_LOCK_FUNCTION_CLOSE + ")";

            /*PRIZE-Add-M_Fingerprint-wangzhong-2016_6_28-start*/
            insertInitFingerprintOperationData(db, PrizeFingerprintOperationMetaData.LONGPRESS_INCALL, PrizeFingerprintOperationMetaData.EVENT_TYPE_LONGPRESS);
            insertInitFingerprintOperationData(db, PrizeFingerprintOperationMetaData.LONGPRESS_TAKE, PrizeFingerprintOperationMetaData.EVENT_TYPE_LONGPRESS);
            insertInitFingerprintOperationData(db, PrizeFingerprintOperationMetaData.LONGPRESS_CALLRECORD, PrizeFingerprintOperationMetaData.EVENT_TYPE_LONGPRESS);
            insertInitFingerprintOperationData(db, PrizeFingerprintOperationMetaData.LONGPRESS_SCREENCAPTURE, PrizeFingerprintOperationMetaData.EVENT_TYPE_LONGPRESS);
            insertInitFingerprintOperationData(db, PrizeFingerprintOperationMetaData.LONGPRESS_RETURNHOME, PrizeFingerprintOperationMetaData.EVENT_TYPE_LONGPRESS);
            insertInitFingerprintOperationData(db, PrizeFingerprintOperationMetaData.LONGPRESS_NOTICE, PrizeFingerprintOperationMetaData.EVENT_TYPE_LONGPRESS);

            insertInitFingerprintOperationData(db, PrizeFingerprintOperationMetaData.CLICK_BACK, PrizeFingerprintOperationMetaData.EVENT_TYPE_CLICK);
            insertInitFingerprintOperationData(db, PrizeFingerprintOperationMetaData.CLICK_SLIDELAUNCHER, PrizeFingerprintOperationMetaData.EVENT_TYPE_CLICK);
            insertInitFingerprintOperationData(db, PrizeFingerprintOperationMetaData.CLICK_MUSIC, PrizeFingerprintOperationMetaData.EVENT_TYPE_CLICK);
            insertInitFingerprintOperationData(db, PrizeFingerprintOperationMetaData.CLICK_VIDEO, PrizeFingerprintOperationMetaData.EVENT_TYPE_CLICK);
            /*PRIZE-Add-M_Fingerprint-wangzhong-2016_6_28-end*/
           // db.execSQL(insertOneFuncSql);
            db.execSQL(insertTwoFuncSql);
            Log.i(TAG, "Bootstrapped database");
        }

        /*PRIZE-Add-M_Fingerprint-wangzhong-2016_6_28-start*/
        private void insertInitFingerprintOperationData(SQLiteDatabase db, String nameValue, String type) {
            db.execSQL("INSERT INTO " + PrizeFingerprintOperationMetaData.TABLE_NAME
                    + " ("
                    + PrizeFingerprintOperationMetaData.OPERATION_NAME + ","
                    + PrizeFingerprintOperationMetaData.OPERATION_STATUS + ","
                    + PrizeFingerprintOperationMetaData.OPERATION_EVENT_TYPE + ","
                    + PrizeFingerprintOperationMetaData.OPERATION_EVENT_STATUS +") "
                    + "VALUES ('"
                    + nameValue + "',"
                    + 1 + ",'"
                    + type + "',"
                    + 0 + ")");
        }
        /*PRIZE-Add-M_Fingerprint-wangzhong-2016_6_28-end*/

        private void reconstruct(SQLiteDatabase db) {
            dropTables(db);
            bootstrapDB(db);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            if (oldVersion < newVersion) {
                Log.w(TAG, "Detected schema version '" +  oldVersion + "'. " +
                        "Index needs to be rebuilt for schema version '" + newVersion + "'.");
                reconstruct(db);
            }
        }

        @Override
        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Detected schema version '" +  oldVersion + "'. " +
                    "Index needs to be rebuilt for schema version '" + newVersion + "'.");
            reconstruct(db);
        }
        
        private void dropTables(SQLiteDatabase db) {
            db.execSQL("DROP TABLE IF EXISTS " + PrizeAppLockMetaData.TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + PrizeFpFuntionMetaData.TABLE_NAME);
            /*PRIZE-Add-M_Fingerprint-wangzhong-2016_6_28-start*/
            db.execSQL("DROP TABLE IF EXISTS " + PrizeAppLockCipherMetaData.TABLE_NAME);

            db.execSQL("DROP TABLE IF EXISTS " + PrizeFingerprintOperationMetaData.TABLE_NAME);
            /*PRIZE-Add-M_Fingerprint-wangzhong-2016_6_28-end*/
        }
    }
}
