/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.launcher3;

import android.app.SearchManager;
import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemProperties;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Xml;

import com.android.launcher3.LauncherSettings.Favorites;
import com.android.launcher3.config.ProviderConfig;
import com.android.launcher3.dao.FirstInstallTable;
import com.android.launcher3.lq.DefaultConfig;
import com.mediatek.common.featureoption.FeatureOption;
import com.mediatek.launcher3.ext.AllApps;
import com.mediatek.launcher3.ext.LauncherLog;
import com.prize.left.page.util.ClientInfo;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
//A by zhouerlong


import java.io.IOException;
import java.io.InputStream;
//A by zhouerlong
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.jar.Attributes.Name;

public class LauncherProvider extends ContentProvider {
    private static final String TAG = "Launcher3.LauncherProvider";
    private static final boolean LOGD = true;

    /// M: Add for smart book feature. Change DB is UI layout is changed.
    public static final int DATABASE_NUM = 3;
    private static final String[] DATABASE_NAME_ARRAY = {"launcher.db", "launcher2.db", "launcher3.db"};
    private static final String[] EMPTY_DATABASE_CREATED_ARRAY =
            {"EMPTY_DATABASE_CREATED",
            "EMPTY_DATABASE_CREATED2",
            "EMPTY_DATABASE_CREATED3"};
    private ArrayList<DatabaseHelper> mDatabaseHelpers;
    private int mDatabaseId;

    private static String DATABASE_NAME = "launcher.db";

    private static final int DATABASE_VERSION = 16;

    static final String OLD_AUTHORITY = "com.android.launcher2.settings";
    static final String AUTHORITY = ProviderConfig.AUTHORITY;

    public static final String TABLE_FAVORITES = "favorites";
    static final String TABLE_WORKSPACE_SCREENS = "workspaceScreens";
    static final String PARAMETER_NOTIFY = "notify";
    static final String UPGRADED_FROM_OLD_DATABASE =
            "UPGRADED_FROM_OLD_DATABASE";
    /// M: Remove final modifier for smart book feature.
    static String EMPTY_DATABASE_CREATED =
            "EMPTY_DATABASE_CREATED";
    static String EMPTY_DATABASE_CREATED_STK =
            "EMPTY_DATABASE_CREATED_STK";
    static final String DEFAULT_WORKSPACE_RESOURCE_ID =
            "DEFAULT_WORKSPACE_RESOURCE_ID";

    private static final String ACTION_APPWIDGET_DEFAULT_WORKSPACE_CONFIGURE =
            "com.android.launcher.action.APPWIDGET_DEFAULT_WORKSPACE_CONFIGURE";

    /**
     * {@link Uri} triggered at any registered {@link android.database.ContentObserver} when
     * {@link AppWidgetHost#deleteHost()} is called during database creation.
     * Use this to recall {@link AppWidgetHost#startListening()} if needed.
     */
    static final Uri CONTENT_APPWIDGET_RESET_URI =
            Uri.parse("content://" + AUTHORITY + "/appWidgetReset");

    public  static  DatabaseHelper mOpenHelper;
    /// M: Indicate whether the device is Tablet or not
    private static boolean sIsTablet = ("tablet".equals(
            SystemProperties.get("ro.build.characteristics")));

    private static boolean sJustLoadedFromOldDb;
    private static HashMap<String, String> renameApps =  new HashMap<>(); 

    @Override
    public boolean onCreate() {
        LauncherLog.d(TAG, "(LauncherProvider)onCreate");

        final Context context = getContext();

        /// M: Add for smart book feature. Create different DB for different UI layout.
        if (FeatureOption.MTK_SMARTBOOK_SUPPORT) {
            mDatabaseHelpers = new ArrayList<DatabaseHelper>();
            for (int i = 0; i < DATABASE_NUM; ++i) {
                DATABASE_NAME = DATABASE_NAME_ARRAY[i];
                EMPTY_DATABASE_CREATED = EMPTY_DATABASE_CREATED_ARRAY[i];
                mDatabaseHelpers.add(new DatabaseHelper(context));
            }
            setDatabaseId(context.getResources().getInteger(R.integer.database_id));
        } else {
        	if(mOpenHelper == null) {
                mOpenHelper = new DatabaseHelper(context);
        	}
        }

        LauncherAppState.setLauncherProvider(this);
        /// M: init apps cell count for op09.
//        LauncherExtPlugin.getInstance().getLoadDataExt(getContext()).initCellCount();
        return true;
    }

    @Override
    public String getType(Uri uri) {
        SqlArguments args = new SqlArguments(uri, null, null);
        if (TextUtils.isEmpty(args.where)) {
            return "vnd.android.cursor.dir/" + args.table;
        } else {
            return "vnd.android.cursor.item/" + args.table;
        }
    }

    //add by zhouerlong
    public static  void setFlagEmptyDbCreated(Context context) {
        String spKey = LauncherAppState.getSharedPreferencesKey();
        SharedPreferences sp = context.getSharedPreferences(spKey, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(EMPTY_DATABASE_CREATED, true);
        editor.putBoolean(UPGRADED_FROM_OLD_DATABASE, false);
        editor.commit();
    }
    
    //add by zhouerlong
    public static  void setFlagEmptyDbCreatedWitchSTK(Context context) {
        String spKey = LauncherAppState.getSharedPreferencesKey();
        SharedPreferences sp = context.getSharedPreferences(spKey, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(EMPTY_DATABASE_CREATED_STK, true);
        editor.commit();
    }
    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {
    	Cursor result=null;
        synchronized (LauncherProvider.class) {
        SqlArguments args = new SqlArguments(uri, selection, selectionArgs);
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(args.table);

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();  
        if(db.isReadOnly()) {
        	db.close();
        	db = mOpenHelper.getWritableDatabase();
        }
        db.beginTransaction();
        try {
            result   = qb.query(db, projection, args.where, args.args, null, null, sortOrder);
            db.setTransactionSuccessful();
            result.setNotificationUri(getContext().getContentResolver(), uri);
		} catch (Exception e) {
			// TODO: handle exception
		}finally {
			db.endTransaction();
			;
			
		}
        }
        return result;
    }
    
    
    

	private static void clearDB(SQLiteDatabase db) {

		db.execSQL(FirstInstallTable.DELETE_INSTALL_TABLE);
	}
    /**
	 * 查询操作可以多线程
	 *//*
	public static Cursor query(String table, String[] columns,
			String selection, String[] selectionArgs, String groupBy,
			String having, String orderBy) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		try {
			return db.query(table, columns, selection,
					selectionArgs, groupBy, having, orderBy);
		} catch (Exception e) {
			try {
				// 如果数据查询操作异常，将清除所有数据，重新创建数据库
//				clearDB(db);
			} catch (Exception e2) {
			}
			return null;
		}
	}*/
	
	
	/*public static void insert(final String table, final String nullColumnHack,
			final ContentValues values) {
		SQLSingleThreadExcutor.getInstance().execute(new Runnable() {
			@Override
			public void run() {
				try {

					long id = getDatabase().insert(table, nullColumnHack,
							values);
				} catch (Exception e) {
					return;
				}
			}
		});
	}*/
	
	/***
	 * 插入或更新已安装应用表<br>
	 * 只针对 InstalledAppTable表操作
	 * 
	 * @param cv
	 * @return 0: fail, else: success
	 *//*
	public static long updateTable(String table, ContentValues cv,String key) {

		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		String value = cv.getAsString(key);
		if (TextUtils.isEmpty(value))
			return 0;

		String[] args = new String[] { value };
		Cursor c = db.query(table, null, key + "=?", args, null, null, null);

		if (null == c || c.getCount() < 1) {
			if (c != null)
				c.close();
			return db.insert(table, null, cv);
		} else if (c.getCount() > 0) {
			c.close();
			return db.update(table, cv, key + "=?", args);
		}
		return 0;
	}*/
	/***
	 * 批量插入已安装应用的数据
	 * 
	 * @param datas
	 *            数据
	 * @return 0 : fail, -1: no data, 1:success
	 *//*
	public static int bulkInsert(List<ContentValues> datas,String table,String key) {
		if (null == datas || datas.size() < 1)
			return -1;
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		try {
			db.beginTransaction();
			int sz = datas.size();
			for (int i = 0; i < sz; i++) {
				ContentValues cv = datas.get(i);
				updateTable(table,cv, key);
			}
			db.setTransactionSuccessful();
			db.endTransaction();
			return 1;
		} catch (Exception e) {
			e.printStackTrace();
			db.endTransaction();
			return 0;
		}
	}*/

	/*public static void update(final String table, final ContentValues values,
			final String whereClause, final String[] whereArgs) {
		SQLSingleThreadExcutor.getInstance().execute(new Runnable() {
			@Override
			public void run() {
				try { // try - catch 原因:防止数据库前后版本不匹配,导致数据异常
					getDatabase().update(table, values, whereClause, whereArgs);
				} catch (Exception e) {
				}

			}
		});
	}*/
	
	/*public static void delete(final String table, final String whereClause,
			final String[] whereArgs) {
		SQLSingleThreadExcutor.getInstance().execute(new Runnable() {
			@Override
			public void run() {
				try { // try - catch 原因:防止数据库前后版本不匹配,导致数据异常
					getDatabase().delete(table, whereClause, whereArgs);
				} catch (Exception e) {
					return;
				}

			}
		});
	}*/
	
	
	
    
    
    
//	public static int bulkInsert(String table, ContentValues[] values) {
//
//		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
//		db.beginTransaction();
//		try {
//			int numValues = values.length;
//			for (int i = 0; i < numValues; i++) {
//				if (dbInsertAndCheck(mOpenHelper, db, table, null, values[i]) < 0) {
//					return 0;
//				}
//			}
//			db.setTransactionSuccessful();
//		} finally {
//			db.endTransaction();
//		}
//		return values.length;
//	}
	
	
	
	
//	protected static SQLiteDatabase getDatabase() {
//		// TODO Auto-generated method stub
//		return mOpenHelper.getWritableDatabase();
//	}

    private   long dbInsertAndCheck(DatabaseHelper helper, String table, String nullColumnHack, ContentValues values) {
        if (!values.containsKey(LauncherSettings.Favorites._ID)) {
            throw new RuntimeException("Error: attempting to add item without specifying an id");
        }
        long index=0;
            SQLiteDatabase db = helper.getWritableDatabase();  
            if(db.isReadOnly()) {
            	db.close();
            	db = helper.getWritableDatabase();
            }
        db.beginTransaction();
        try {
        	
        	index=db.insert(table, nullColumnHack, values);
            db.setTransactionSuccessful(); // 设置事务成功完成  
		} catch (Exception e) {
			// TODO: handle exception
		}finally {
			   db.endTransaction();  
               ; 
		}
        return index;
    }

    private static void deleteId(SQLiteDatabase db, long id) {
        Uri uri = LauncherSettings.Favorites.getContentUri(id, false);
        SqlArguments args = new SqlArguments(uri, null, null); 
        if(db.isReadOnly()) {
        	db.close();
        	db = mOpenHelper.getWritableDatabase();
        }
        try {
        	db.beginTransaction();
            db.delete(args.table, args.where, args.args);
            db.setTransactionSuccessful();
		} catch (Exception e) {
			
		}finally {
			db.endTransaction();
		}
    }

    @Override
    public Uri insert(Uri uri, ContentValues initialValues) {
        SqlArguments args = new SqlArguments(uri);
        synchronized (mOpenHelper) {
        addModifiedTime(initialValues);
        final long rowId = dbInsertAndCheck(mOpenHelper, args.table, null, initialValues);
        if (rowId <= 0) return null;

        uri = ContentUris.withAppendedId(uri, rowId);
        sendNotify(uri);

		
		}
        return uri;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        SqlArguments args = new SqlArguments(uri);

        synchronized (mOpenHelper) {
            SQLiteDatabase db = mOpenHelper.getWritableDatabase();  
            if(db.isReadOnly()) {
            	db.close();
            	db = mOpenHelper.getWritableDatabase();
            }
        db.beginTransaction();
        try {
            int numValues = values.length;
            for (int i = 0; i < numValues; i++) {
                addModifiedTime(values[i]);
                if (dbInsertAndCheck(mOpenHelper,  args.table, null, values[i]) < 0) {
                    return 0;
                }
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            ;
        }


        sendNotify(uri);
        }
        return values.length;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int count = 0;
        synchronized (mOpenHelper) {
        SqlArguments args = new SqlArguments(uri, selection, selectionArgs);


        SQLiteDatabase db = mOpenHelper.getWritableDatabase();  
        if(db.isReadOnly()) {
        	db.close();
        	db = mOpenHelper.getWritableDatabase();
        }
        db.beginTransaction();
        try {
        	
             count = db.delete(args.table, args.where, args.args);
             db.setTransactionSuccessful();
            if (count > 0) sendNotify(uri);
		} catch (Exception e) {
			// TODO: handle exception
		} finally {
			db.endTransaction();
			;
		}

        }
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int count = 0;
        synchronized (mOpenHelper) {
        SqlArguments args = new SqlArguments(uri, selection, selectionArgs);

        addModifiedTime(values);

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();  
        if(db.isReadOnly()) {
        	db.close();
        	db = mOpenHelper.getWritableDatabase();
        }
        db.beginTransaction();
        try {
	         count = db.update(args.table, values, args.where, args.args);
	         db.setTransactionSuccessful();
	        if (count > 0) sendNotify(uri);
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
            db.endTransaction(); 
			;
		}
        }

        return count;
    }

    private void sendNotify(Uri uri) {
        String notify = uri.getQueryParameter(PARAMETER_NOTIFY);
        if (notify == null || "true".equals(notify)) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // always notify the backup agent
//        LauncherBackupAgentHelper.dataChanged(getContext());
    }

    private void addModifiedTime(ContentValues values) {
        values.put(LauncherSettings.ChangeLogColumns.MODIFIED, System.currentTimeMillis());
    }

    public long generateNewItemId() {
        return mOpenHelper.generateNewItemId();
    }

    /// M: Add for edit and hide apps for op09.
//    public static SQLiteDatabase getSQLiteDatabase() {
//        return mOpenHelper.getWritableDatabase();
//    }

    public void updateMaxItemId(long id) {
        mOpenHelper.updateMaxItemId(id);
    }

    public long generateNewScreenId() {
        return mOpenHelper.generateNewScreenId();
    }

    // This is only required one time while loading the workspace during the
    // upgrade path, and should never be called from anywhere else.
    public void updateMaxScreenId(long maxScreenId) {
        mOpenHelper.updateMaxScreenId(maxScreenId);
    }

    /**
     * @param Should we load the old db for upgrade? first run only.
     */
    synchronized public boolean justLoadedOldDb() {
        String spKey = LauncherAppState.getSharedPreferencesKey();
        SharedPreferences sp = getContext().getSharedPreferences(spKey, Context.MODE_PRIVATE);

        boolean loadedOldDb = false || sJustLoadedFromOldDb;

        sJustLoadedFromOldDb = false;
        if (sp.getBoolean(UPGRADED_FROM_OLD_DATABASE, false)) {

            SharedPreferences.Editor editor = sp.edit();
            editor.putBoolean(UPGRADED_FROM_OLD_DATABASE, true);
            editor.commit();
            loadedOldDb = true;
        }
        return loadedOldDb;
    }
    
    synchronized public static void resetOldDb(Context c) {
        String spKey = LauncherAppState.getSharedPreferencesKey();
        SharedPreferences sp = c.getSharedPreferences(spKey, Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(UPGRADED_FROM_OLD_DATABASE, false);
        editor.commit();
    	
    }

    /**
     * @param workspaceResId that can be 0 to use default or non-zero for specific resource
     */
    synchronized public void loadDefaultFavoritesIfNecessary(int origWorkspaceResId) {
        String spKey = LauncherAppState.getSharedPreferencesKey();
        SharedPreferences sp = getContext().getSharedPreferences(spKey, Context.MODE_PRIVATE);
        if (sp.getBoolean(EMPTY_DATABASE_CREATED, false)) {
            int workspaceResId = origWorkspaceResId;//原始的workspaceid

            // Use default workspace resource if none provided
			if (workspaceResId == 0) {
				workspaceResId = sp.getInt(DEFAULT_WORKSPACE_RESOURCE_ID,
						R.xml.default_workspace1);
			}

            // Populate favorites table with initial favorites
            SharedPreferences.Editor editor = sp.edit();
            editor.remove(EMPTY_DATABASE_CREATED);
            if (origWorkspaceResId != 0) {
                editor.putInt(DEFAULT_WORKSPACE_RESOURCE_ID, origWorkspaceResId);
            }

            mOpenHelper.loadFavorites(mOpenHelper.getWritableDatabase(), workspaceResId);
            mOpenHelper.setFlagJustLoadedOldDb();
            editor.commit();
        }else {
//        	mOpenHelper.modifyFavoritesByConfigName(mOpenHelper.getWritableDatabase(), origWorkspaceResId);
        }
    }

    /**
     * M: Add for smart book feature. Set database ID.
     */
    public void setDatabaseId(int id) {
        LauncherLog.d(TAG, "setDatabaseId: id = " + id);

        mDatabaseId = id;
        DATABASE_NAME = DATABASE_NAME_ARRAY[id];
        EMPTY_DATABASE_CREATED = EMPTY_DATABASE_CREATED_ARRAY[id];
        mOpenHelper = mDatabaseHelpers.get(id);
    }

    /**
     * M: Add for smart book feature. Get database ID.
     */
    public int getDatabaseId() {
        return mDatabaseId;
    }

    private static interface ContentValuesCallback {
        public void onRow(ContentValues values);
    }

    class DatabaseHelper extends SQLiteOpenHelper {
        private static final String TAG_FAVORITES = "favorites";
        private static final String TAG_FAVORITE = "favorite";
        private static final String TAG_CLOCK = "clock";
        private static final String TAG_SEARCH = "search";
        private static final String TAG_APPWIDGET = "appwidget";
        private static final String TAG_SHORTCUT = "shortcut";
        private static final String TAG_FOLDER = "folder";
        private static final String TAG_EXTRA = "extra";
        private static final String TAG_INCLUDE = "include";

        private final Context mContext;
        private final AppWidgetHost mAppWidgetHost;
        private long mMaxItemId = -1;
        private long mMaxScreenId = -1;

        DatabaseHelper(Context context) {
//            super(context, DATABASE_NAME, null, ClientInfo.getInstance(context).appVersion);

            super(context, DATABASE_NAME, null, DATABASE_VERSION);
            if (LOGD) Log.d(TAG, "creating DatabaseHelper");
            mContext = context;
            mAppWidgetHost = new AppWidgetHost(context, Launcher.APPWIDGET_HOST_ID);

            // In the case where neither onCreate nor onUpgrade gets called, we read the maxId from
            // the DB here
            if (mMaxItemId == -1) {
                mMaxItemId = initializeMaxItemId(getWritableDatabase());
            }
            if (mMaxScreenId == -1) {
                mMaxScreenId = initializeMaxScreenId(getWritableDatabase());
            }
            
            /// M: Add for edit and hide apps for op09.
           /* LauncherExtPlugin.getInstance().getLoadDataExt(context)
                    .initializeMaxIdForAllAppsList(getWritableDatabase());*/
        }

        /**
         * Send notification that we've deleted the {@link AppWidgetHost},
         * probably as part of the initial database creation. The receiver may
         * want to re-call {@link AppWidgetHost#startListening()} to ensure
         * callbacks are correctly set.
         */
        private void sendAppWidgetResetNotify() {
            final ContentResolver resolver = mContext.getContentResolver();
            resolver.notifyChange(CONTENT_APPWIDGET_RESET_URI, null);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            if (LOGD) Log.d(TAG, "creating new launcher database");

            mMaxItemId = 1;
            mMaxScreenId = 0;

            db.execSQL("CREATE TABLE favorites (" +
                    "_id INTEGER PRIMARY KEY," +
                    "title TEXT," +
                    "title_id INTEGER,"+//add by zhouerlong+
                    "intent TEXT," +
                    "config_name TEXT," +
                    "container INTEGER," +
                    "screen INTEGER," +
                    "cellX INTEGER," +
                    "cellY INTEGER," +
                    "spanX INTEGER," +
                    "spanY INTEGER," +
                    "first_install INTEGER," +
                    "recommd_dis INTEGER," +
                    "app_hide INTEGER," +
                    "package_name TEXT," +
                    "componentname TEXT," +
                    "icon_uri TEXT," +
                    "down_state TEXT," +
                    "itemType INTEGER," +
                    "appWidgetId INTEGER NOT NULL DEFAULT -1," +
                    "isShortcut INTEGER," +
                    "iconType INTEGER," +
                    "down_progress INTEGER," +
                    "from_appstore INTEGER," +
                    "iconPackage TEXT," +
                    "iconResource TEXT," +
                    "icon BLOB," +
                    "uri TEXT," +
                    "displayMode INTEGER," +
                    "appWidgetProvider TEXT," +
                    "modified INTEGER NOT NULL DEFAULT 0" +
                    ");");
            db.execSQL(FirstInstallTable.FIRST_INSTALL_TABLE);
            
            addWorkspacesTable(db);
            addAppsTable(db);
            /// M: create all apps table, add for OP09.
//            LauncherExtPlugin.getInstance().getLoadDataExt(mContext)
//                    .createAllAppsTable(db);

            // Database was just created, so wipe any previous widgets
            if (mAppWidgetHost != null) {
                mAppWidgetHost.deleteHost();
                sendAppWidgetResetNotify();
            }

            // Try converting the old database
            ContentValuesCallback permuteScreensCb = new ContentValuesCallback() {
                public void onRow(ContentValues values) {
                    int container = values.getAsInteger(LauncherSettings.Favorites.CONTAINER);
                    if (container == Favorites.CONTAINER_DESKTOP) {
                        int screen = values.getAsInteger(LauncherSettings.Favorites.SCREEN);
                        screen = (int) upgradeLauncherDb_permuteScreens(screen);
                        values.put(LauncherSettings.Favorites.SCREEN, screen);
                    }
                }
            };
            Uri uri = Uri.parse("content://" + Settings.AUTHORITY +
                    "/old_favorites?notify=true");
            if (!convertDatabase(db, uri, permuteScreensCb, true)) {
                // Try and upgrade from the Launcher2 db
                uri = LauncherSettings.Favorites.OLD_CONTENT_URI;
                if (!convertDatabase(db, uri, permuteScreensCb, false)) {
                    // If we fail, then set a flag to load the default workspace
                    setFlagEmptyDbCreated();
                    LauncherProvider.setFlagEmptyDbCreatedWitchSTK(mContext);
                    return;
                }
            }
            // Right now, in non-default workspace cases, we want to run the final
            // upgrade code (ie. to fix workspace screen indices -> ids, etc.), so
            // set that flag too.
            setFlagJustLoadedOldDb();
        }

        private void addWorkspacesTable(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + TABLE_WORKSPACE_SCREENS + " (" +
                    LauncherSettings.WorkspaceScreens._ID + " INTEGER," +
                    LauncherSettings.WorkspaceScreens.SCREEN_RANK + " INTEGER," +
                    LauncherSettings.ChangeLogColumns.MODIFIED + " INTEGER NOT NULL DEFAULT 0" +
                    ");");
        }
        /*
            final int idIndex = c.getColumnIndexOrThrow(AllApps._ID);
                final int intentIndex = c.getColumnIndexOrThrow(AllApps.INTENT);
                final int titleIndex = c.getColumnIndexOrThrow(AllApps.TITLE);
                final int itemTypeIndex = c
                        .getColumnIndexOrThrow(AllApps.ITEM_TYPE);
                final int screenIndex = c.getColumnIndexOrThrow(AllApps.SCREEN);
                final int cellXIndex = c.getColumnIndexOrThrow(AllApps.CELLX);
                final int cellYIndex = c.getColumnIndexOrThrow(AllApps.CELLY);
                final int visibleIndex = c
                        .getColumnIndexOrThrow(AllApps.VISIBLE_FLAG);
         */
		private void addAppsTable(SQLiteDatabase db) {
			db.execSQL("CREATE TABLE " + AllApps.TABLE_ALLAPPS + " ("
					+ AllApps._ID + " INTEGER," 
					+ AllApps.INTENT + " TEXT,"
					+ AllApps.TITLE + " TEXT," 
					+ AllApps.ITEM_TYPE + " TEXT,"
					+ AllApps.PACKAGE_NAME + " TEXT," 
					+ AllApps.CLASS_NAME+ " TEXT,"
					+ AllApps.SCREEN + " TEXT," 
					+ AllApps.CELLX+ " TEXT," 
					+ AllApps.CELLY + " TEXT,"
					+ LauncherSettings.Favorites.SPANX + " TEXT,"
					+ LauncherSettings.Favorites.SPANY + " TEXT,"
					+ LauncherSettings.Favorites.CONTAINER + " INTEGER,"
					+ AllApps.VISIBLE_FLAG + " TEXT,"
					+LauncherSettings.ChangeLogColumns.MODIFIED +" INTEGER"
					+  ");");
		}

        private void setFlagJustLoadedOldDb() {
            String spKey = LauncherAppState.getSharedPreferencesKey();
            SharedPreferences sp = mContext.getSharedPreferences(spKey, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putBoolean(UPGRADED_FROM_OLD_DATABASE, true);
            editor.putBoolean(EMPTY_DATABASE_CREATED, false);
            editor.commit();
        }

        private void setFlagEmptyDbCreated() {
            String spKey = LauncherAppState.getSharedPreferencesKey();
            SharedPreferences sp = mContext.getSharedPreferences(spKey, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putBoolean(EMPTY_DATABASE_CREATED, true);
            editor.putBoolean(UPGRADED_FROM_OLD_DATABASE, false);
            editor.commit();
        }

        // We rearrange the screens from the old launcher
        // 12345 -> 34512
        private long upgradeLauncherDb_permuteScreens(long screen) {
            if (screen >= 2) {
                return screen - 2;
            } else {
                return screen + 3;
            }
        }

        private boolean convertDatabase(SQLiteDatabase db, Uri uri,
                                        ContentValuesCallback cb, boolean deleteRows) {
            if (LOGD) Log.d(TAG, "converting database from an older format, but not onUpgrade");
            boolean converted = false;

            final ContentResolver resolver = mContext.getContentResolver();
            Cursor cursor = null;

            try {
                cursor = resolver.query(uri, null, null, null, null);
            } catch (Exception e) {
                // Ignore
            }

            // We already have a favorites database in the old provider
            if (cursor != null) {
                try {
                     if (cursor.getCount() > 0) {
                        converted = copyFromCursor(db, cursor, cb) > 0;
                        if (converted && deleteRows) {
                            resolver.delete(uri, null, null);
                        }
                    }
                } finally {
                	try {
                		cursor.close();
        			} catch (Exception e) {
        				// TODO: handle exception
        			}
                }
            }

            if (converted) {
                // Convert widgets from this import into widgets
                if (LOGD) Log.d(TAG, "converted and now triggering widget upgrade");
                convertWidgets(db);

                // Update max item id
                mMaxItemId = initializeMaxItemId(db);
                if (LOGD) Log.d(TAG, "mMaxItemId: " + mMaxItemId);
            }

            return converted;
        }

        private int copyFromCursor(SQLiteDatabase db, Cursor c, ContentValuesCallback cb) {
            final int idIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites._ID);
            final int intentIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.INTENT);
            final int titleIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.TITLE);
            final int iconTypeIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.ICON_TYPE);
            final int iconIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.ICON);
            final int iconPackageIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.ICON_PACKAGE);
            final int iconResourceIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.ICON_RESOURCE);
            final int containerIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.CONTAINER);
            final int itemTypeIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.ITEM_TYPE);
            final int screenIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.SCREEN);
            final int cellXIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.CELLX);
            final int cellYIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.CELLY);
            final int uriIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.URI);
            final int displayModeIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.DISPLAY_MODE);

            ContentValues[] rows = new ContentValues[c.getCount()];
            int i = 0;
            while (c.moveToNext()) {
                ContentValues values = new ContentValues(c.getColumnCount());
                values.put(LauncherSettings.Favorites._ID, c.getLong(idIndex));
                values.put(LauncherSettings.Favorites.INTENT, c.getString(intentIndex));
                values.put(LauncherSettings.Favorites.TITLE, c.getString(titleIndex));
                values.put(LauncherSettings.Favorites.ICON_TYPE, c.getInt(iconTypeIndex));
                values.put(LauncherSettings.Favorites.ICON, c.getBlob(iconIndex));
                values.put(LauncherSettings.Favorites.ICON_PACKAGE, c.getString(iconPackageIndex));
                values.put(LauncherSettings.Favorites.ICON_RESOURCE, c.getString(iconResourceIndex));
                values.put(LauncherSettings.Favorites.CONTAINER, c.getInt(containerIndex));
                values.put(LauncherSettings.Favorites.ITEM_TYPE, c.getInt(itemTypeIndex));
                values.put(LauncherSettings.Favorites.APPWIDGET_ID, -1);
                values.put(LauncherSettings.Favorites.SCREEN, c.getInt(screenIndex));
                values.put(LauncherSettings.Favorites.CELLX, c.getInt(cellXIndex));
                values.put(LauncherSettings.Favorites.CELLY, c.getInt(cellYIndex));
                values.put(LauncherSettings.Favorites.URI, c.getString(uriIndex));
                values.put(LauncherSettings.Favorites.DISPLAY_MODE, c.getInt(displayModeIndex));
                if (cb != null) {
                    cb.onRow(values);
                }
                rows[i++] = values;
            }

            int total = 0;
            if (i > 0) {
                db.beginTransaction();
                try {
                    int numValues = rows.length;
                    for (i = 0; i < numValues; i++) {
                        if (dbInsertAndCheck(this,  TABLE_FAVORITES, null, rows[i]) < 0) {
                            return 0;
                        } else {
                            total++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
            }

            return total;
        }

        @Override
		public synchronized void close() {
			// TODO Auto-generated method stub
			super.close();
		}

		@Override
		public void onOpen(SQLiteDatabase db) {
			// TODO Auto-generated method stub
			super.onOpen(db);
		}

		@Override
		public SQLiteDatabase getWritableDatabase() {
			// TODO Auto-generated method stub
			return super.getWritableDatabase();
		}

		@Override
		public SQLiteDatabase getReadableDatabase() {
			// TODO Auto-generated method stub
			return super.getReadableDatabase();
		}

		@Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            if (LOGD) {
                LauncherLog.d(TAG, "onUpgrade triggered oldVersion = " + oldVersion + ", newVersion = " + newVersion);
            }
            LauncherLog.d(TAG, "升级开始");
            
        /*    // 删除原来的数据表  
            db.execSQL("DROP TABLE IF EXISTS " + "favorites"); 
            // 重新创建  
            onCreate(db);  */

            int version = oldVersion;
            
            if(newVersion==16) {

                db.beginTransaction();
                try {
					db.execSQL("alter table favorites add column recommd_dis integer default 0");
					db.setTransactionSuccessful();
				} catch (Exception e) {
					// TODO: handle exception
				}finally {
					db.endTransaction();
				}
            }
            
            
          /*  if (version < 3) {
                // upgrade 1,2 -> 3 added appWidgetId column
                db.beginTransaction();
                try {
                    // Insert new column for holding appWidgetIds
                    db.execSQL("ALTER TABLE favorites " +
                        "ADD COLUMN appWidgetId INTEGER NOT NULL DEFAULT -1;");
                    db.setTransactionSuccessful();
                    version = 3;
                } catch (SQLException ex) {
                    // Old version remains, which means we wipe old data
                    Log.e(TAG, ex.getMessage(), ex);
                } finally {
                    db.endTransaction();
                }

                // Convert existing widgets only if table upgrade was successful
                if (version == 3) {
                    convertWidgets(db);
                }
            }

            if (version < 4) {
                version = 4;
            }
            */


            // Where's version 5?
            // - Donut and sholes on 2.0 shipped with version 4 of launcher1.
            // - Passion shipped on 2.1 with version 6 of launcher3
            // - Sholes shipped on 2.1r1 (aka Mr. 3) with version 5 of launcher 1
            //   but version 5 on there was the updateContactsShortcuts change
            //   which was version 6 in launcher 2 (first shipped on passion 2.1r1).
            // The updateContactsShortcuts change is idempotent, so running it twice
            // is okay so we'll do that when upgrading the devices that shipped with it.
         /*   if (version < 6) {
                // We went from 3 to 5 screens. Move everything 1 to the right
                db.beginTransaction();
                try {
                    db.execSQL("UPDATE favorites SET screen=(screen + 1);");
                    db.setTransactionSuccessful();
                } catch (SQLException ex) {
                    // Old version remains, which means we wipe old data
                    Log.e(TAG, ex.getMessage(), ex);
                } finally {
                    db.endTransaction();
                }

               // We added the fast track.
                if (updateContactsShortcuts(db)) {
                    version = 6;
                }
            }

            if (version < 7) {
                // Version 7 gets rid of the special search widget.
                convertWidgets(db);
                version = 7;
            }

            if (version < 8) {
                // Version 8 (froyo) has the icons all normalized.  This should
                // already be the case in practice, but we now rely on it and don't
                // resample the images each time.
                normalizeIcons(db);
                version = 8;
            }

            if (version < 9) {
                // The max id is not yet set at this point (onUpgrade is triggered in the ctor
                // before it gets a change to get set, so we need to read it here when we use it)
                if (mMaxItemId == -1) {
                    mMaxItemId = initializeMaxItemId(db);
                }

                // Add default hotseat icons
                loadFavorites(db, R.xml.update_workspace);
                version = 9;
            }

            // We bumped the version three time during JB, once to update the launch flags, once to
            // update the override for the default launch animation and once to set the mimetype
            // to improve startup performance
            if (version < 12) {
                // Contact shortcuts need a different set of flags to be launched now
                // The updateContactsShortcuts change is idempotent, so we can keep using it like
                // back in the Donut days
                updateContactsShortcuts(db);
                /// M: [Tablet Only] Update the position of the items on
                ///    the HotSeat (Move every item 1 unit to the right)
                if (sIsTablet) {
                    db.beginTransaction();
                    try {
                        db.execSQL("UPDATE favorites SET screen=(screen+1),"
                            + " cellX=(cellX+1) WHERE container = -101;");
                        db.setTransactionSuccessful();
                    } catch (SQLException ex) {
                        // Old version remains, which means we wipe old data
                        LauncherLog.e(TAG, ex.getMessage(), ex);
                    } finally {
                        db.endTransaction();
                    }
                }

                version = 12;
            }

            if (version < 13) {
                // With the new shrink-wrapped and re-orderable workspaces, it makes sense
                // to persist workspace screens and their relative order.
                mMaxScreenId = 0;

                // This will never happen in the wild, but when we switch to using workspace
                // screen ids, redo the import from old launcher.
                sJustLoadedFromOldDb = true;

                addWorkspacesTable(db);
                version = 13;
            }

            if (version < 14) {
                db.beginTransaction();
                try {
                    // Insert new column for holding widget provider name
                    db.execSQL("ALTER TABLE favorites " +
                            "ADD COLUMN appWidgetProvider TEXT;");
                    db.setTransactionSuccessful();
                    version = 14;
                } catch (SQLException ex) {
                    // Old version remains, which means we wipe old data
                    Log.e(TAG, ex.getMessage(), ex);
                } finally {
                    db.endTransaction();
                }
            }


            if (version < 15) {
                db.beginTransaction();
                try {
                    // Insert new column for holding update timestamp
                    db.execSQL("ALTER TABLE favorites " +
                            "ADD COLUMN modified INTEGER NOT NULL DEFAULT 0;");
                    db.execSQL("ALTER TABLE workspaceScreens " +
                            "ADD COLUMN modified INTEGER NOT NULL DEFAULT 0;");
                    db.setTransactionSuccessful();
                    version = 15;
                } catch (SQLException ex) {
                    // Old version remains, which means we wipe old data
                    Log.e(TAG, ex.getMessage(), ex);
                } finally {
                    db.endTransaction();
                }
            }

            if (version != DATABASE_VERSION) {
                Log.w(TAG, "Destroying all old data.");
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_FAVORITES);
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_WORKSPACE_SCREENS);

                onCreate(db);
            }*/
        }

        private boolean updateContactsShortcuts(SQLiteDatabase db) {
            final String selectWhere = buildOrWhereString(Favorites.ITEM_TYPE,
                    new int[] { Favorites.ITEM_TYPE_SHORTCUT });

            Cursor c = null;
            final String actionQuickContact = "com.android.contacts.action.QUICK_CONTACT";
            db.beginTransaction();
            try {
                // Select and iterate through each matching widget
                c = db.query(TABLE_FAVORITES,
                        new String[] { Favorites._ID, Favorites.INTENT },
                        selectWhere, null, null, null, null);
                if (c == null) return false;

                if (LOGD) Log.d(TAG, "found upgrade cursor count=" + c.getCount());

                final int idIndex = c.getColumnIndex(Favorites._ID);
                final int intentIndex = c.getColumnIndex(Favorites.INTENT);

                while (c.moveToNext()) {
                    long favoriteId = c.getLong(idIndex);
                    final String intentUri = c.getString(intentIndex);
                    if (intentUri != null) {
                        try {
                            final Intent intent = Intent.parseUri(intentUri, 0);
                            android.util.Log.d("Home", intent.toString());
                            final Uri uri = intent.getData();
                            if (uri != null) {
                                final String data = uri.toString();
                                if ((Intent.ACTION_VIEW.equals(intent.getAction()) ||
                                        actionQuickContact.equals(intent.getAction())) &&
                                        (data.startsWith("content://contacts/people/") ||
                                        data.startsWith("content://com.android.contacts/" +
                                                "contacts/lookup/"))) {

                                    final Intent newIntent = new Intent(actionQuickContact);
                                    // When starting from the launcher, start in a new, cleared task
                                    // CLEAR_WHEN_TASK_RESET cannot reset the root of a task, so we
                                    // clear the whole thing preemptively here since
                                    // QuickContactActivity will finish itself when launching other
                                    // detail activities.
                                    newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                                            Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    newIntent.putExtra(
                                            Launcher.INTENT_EXTRA_IGNORE_LAUNCH_ANIMATION, true);
                                    newIntent.setData(uri);
                                    // Determine the type and also put that in the shortcut
                                    // (that can speed up launch a bit)
                                    newIntent.setDataAndType(uri, newIntent.resolveType(mContext));

                                    final ContentValues values = new ContentValues();
                                    values.put(LauncherSettings.Favorites.INTENT,
                                            newIntent.toUri(0));

                                    String updateWhere = Favorites._ID + "=" + favoriteId;
                                    db.update(TABLE_FAVORITES, values, updateWhere, null);
                                }
                            }
                        } catch (RuntimeException ex) {
                            Log.e(TAG, "Problem upgrading shortcut", ex);
                        } catch (URISyntaxException e) {
                            Log.e(TAG, "Problem upgrading shortcut", e);
                        }
                    }
                }

                db.setTransactionSuccessful();
            } catch (SQLException ex) {
                Log.w(TAG, "Problem while upgrading contacts", ex);
                return false;
            } finally {
                db.endTransaction();
                if (c != null) {
                    c.close();
                }
            }

            return true;
        }

        private void normalizeIcons(SQLiteDatabase db) {
            Log.d(TAG, "normalizing icons");

            db.beginTransaction();
            Cursor c = null;
            SQLiteStatement update = null;
            try {
                boolean logged = false;
                update = db.compileStatement("UPDATE favorites "
                        + "SET icon=? WHERE _id=?");

                c = db.rawQuery("SELECT _id, icon FROM favorites WHERE iconType=" +
                        Favorites.ICON_TYPE_BITMAP, null);

                final int idIndex = c.getColumnIndexOrThrow(Favorites._ID);
                final int iconIndex = c.getColumnIndexOrThrow(Favorites.ICON);

                while (c.moveToNext()) {
                    long id = c.getLong(idIndex);
                    byte[] data = c.getBlob(iconIndex);
                    try {
                        Bitmap bitmap = Utilities.resampleIconBitmap(
                                BitmapFactory.decodeByteArray(data, 0, data.length),
                                mContext);
                        if (bitmap != null) {
                            update.bindLong(1, id);
                            data = ItemInfo.flattenBitmap(bitmap);
                            if (data != null) {
                                update.bindBlob(2, data);
                                update.execute();
                            }
                            bitmap.recycle();
                        }
                    } catch (Exception e) {
                        if (!logged) {
                            Log.e(TAG, "Failed normalizing icon " + id, e);
                        } else {
                            Log.e(TAG, "Also failed normalizing icon " + id);
                        }
                        logged = true;
                    }
                }
                db.setTransactionSuccessful();
            } catch (SQLException ex) {
                Log.w(TAG, "Problem while allocating appWidgetIds for existing widgets", ex);
            } finally {
                db.endTransaction();
                if (update != null) {
                    update.close();
                }
                if (c != null) {
                    c.close();
                }
            }
        }

        // Generates a new ID to use for an object in your database. This method should be only
        // called from the main UI thread. As an exception, we do call it when we call the
        // constructor from the worker thread; however, this doesn't extend until after the
        // constructor is called, and we only pass a reference to LauncherProvider to LauncherApp
        // after that point
        public long generateNewItemId() {
            if (mMaxItemId < 0) {
                throw new RuntimeException("Error: max item id was not initialized");
            }
            mMaxItemId += 1;
            return mMaxItemId;
        }

        public void updateMaxItemId(long id) {
            mMaxItemId = id + 1;
        }

        private long initializeMaxItemId(SQLiteDatabase db) {
        	Cursor c=null;
            long id = -1;
			try {
            	if(db.isReadOnly()) {
            		db.close();
            		db  = mOpenHelper.getWritableDatabase();
            		
            	}
            	db.beginTransaction();
                 c = db.rawQuery("SELECT MAX(_id) FROM favorites", null);
                 db.setTransactionSuccessful();
                 // get the result
                 final int maxIdIndex = 0;
                 if (c != null && c.moveToNext()) {
                     id = c.getLong(maxIdIndex);
                 }

                 if (id == -1) {
                     throw new RuntimeException("Error: could not query max item id");
                 }
			} catch (Exception e) {
				
			}finally {

	            if (c != null) {
	                c.close();
	            }
	            db.endTransaction();
			}

            return id;
        }

        // Generates a new ID to use for an workspace screen in your database. This method
        // should be only called from the main UI thread. As an exception, we do call it when we
        // call the constructor from the worker thread; however, this doesn't extend until after the
        // constructor is called, and we only pass a reference to LauncherProvider to LauncherApp
        // after that point
        public long generateNewScreenId() {
            if (mMaxScreenId < 0) {
                throw new RuntimeException("Error: max screen id was not initialized");
            }
            mMaxScreenId += 1;
            return mMaxScreenId;
        }

        public void updateMaxScreenId(long maxScreenId) {
            mMaxScreenId = maxScreenId;
        }

        private long initializeMaxScreenId(SQLiteDatabase db) {
        	Cursor c=null;
            long id = -1;
        	try {
                 c = db.rawQuery("SELECT MAX(" + LauncherSettings.WorkspaceScreens._ID + ") FROM " + TABLE_WORKSPACE_SCREENS, null);

                // get the result
                final int maxIdIndex = 0;
                if (c != null && c.moveToNext()) {
                    id = c.getLong(maxIdIndex);
                }
                if (c != null) {
                    c.close();
                }

                if (id == -1) {
                    throw new RuntimeException("Error: could not query max screen id");
                }
			} catch (Exception e) {
				// TODO: handle exception
			}finally {
                if (c != null) {
                    c.close();
                }
			}

            return id;
        }

        /**
         * Upgrade existing clock and photo frame widgets into their new widget
         * equivalents.
         */
        private void convertWidgets(SQLiteDatabase db) {
            final AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(mContext);
            final int[] bindSources = new int[] {
                    Favorites.ITEM_TYPE_WIDGET_CLOCK,
                    Favorites.ITEM_TYPE_WIDGET_PHOTO_FRAME,
                    Favorites.ITEM_TYPE_WIDGET_SEARCH,
            };

            final String selectWhere = buildOrWhereString(Favorites.ITEM_TYPE, bindSources);

            Cursor c = null;

            db.beginTransaction();
            try {
                // Select and iterate through each matching widget
                c = db.query(TABLE_FAVORITES, new String[] { Favorites._ID, Favorites.ITEM_TYPE },
                        selectWhere, null, null, null, null);

                if (LOGD) Log.d(TAG, "found upgrade cursor count=" + c.getCount());

                final ContentValues values = new ContentValues();
                while (c != null && c.moveToNext()) {
                    long favoriteId = c.getLong(0);
                    int favoriteType = c.getInt(1);

                    // Allocate and update database with new appWidgetId
                    try {
                        int appWidgetId = mAppWidgetHost.allocateAppWidgetId();

                        if (LOGD) {
                            Log.d(TAG, "allocated appWidgetId=" + appWidgetId
                                    + " for favoriteId=" + favoriteId);
                        }
                        values.clear();
                        values.put(Favorites.ITEM_TYPE, Favorites.ITEM_TYPE_APPWIDGET);
                        values.put(Favorites.APPWIDGET_ID, appWidgetId);

                        // Original widgets might not have valid spans when upgrading
                        if (favoriteType == Favorites.ITEM_TYPE_WIDGET_SEARCH) {
                            values.put(LauncherSettings.Favorites.SPANX, 4);
                            values.put(LauncherSettings.Favorites.SPANY, 1);
                        } else {
                            values.put(LauncherSettings.Favorites.SPANX, 2);
                            values.put(LauncherSettings.Favorites.SPANY, 2);
                        }

                        String updateWhere = Favorites._ID + "=" + favoriteId;
                        db.update(TABLE_FAVORITES, values, updateWhere, null);

                        if (favoriteType == Favorites.ITEM_TYPE_WIDGET_CLOCK) {
                            // TODO: check return value
                            appWidgetManager.bindAppWidgetIdIfAllowed(appWidgetId,
                                    new ComponentName("com.android.alarmclock",
                                    "com.android.alarmclock.AnalogAppWidgetProvider"));
                        } else if (favoriteType == Favorites.ITEM_TYPE_WIDGET_PHOTO_FRAME) {
                            // TODO: check return value
                            appWidgetManager.bindAppWidgetIdIfAllowed(appWidgetId,
                                    new ComponentName("com.android.camera",
                                    "com.android.camera.PhotoAppWidgetProvider"));
                        } else if (favoriteType == Favorites.ITEM_TYPE_WIDGET_SEARCH) {
                            // TODO: check return value
                            appWidgetManager.bindAppWidgetIdIfAllowed(appWidgetId,
                                    getSearchWidgetProvider());
                        }
                    } catch (RuntimeException ex) {
                        Log.e(TAG, "Problem allocating appWidgetId", ex);
                    }
                }

                db.setTransactionSuccessful();
            } catch (SQLException ex) {
                Log.w(TAG, "Problem while allocating appWidgetIds for existing widgets", ex);
            } finally {
                db.endTransaction();
                if (c != null) {
                    c.close();
                }
            }

            // Update max item id
            mMaxItemId = initializeMaxItemId(db);
            if (LOGD) Log.d(TAG, "mMaxItemId: " + mMaxItemId);
        }

        private  final void beginDocument(XmlPullParser parser, String firstElementName)
                throws XmlPullParserException, IOException {
            int type;
            while ((type = parser.next()) != XmlPullParser.START_TAG
                    && type != XmlPullParser.END_DOCUMENT) {
                ;
            }

            if (type != XmlPullParser.START_TAG) {
                throw new XmlPullParserException("No start tag found");
            }

            if (!parser.getName().equals(firstElementName)) {
                throw new XmlPullParserException("Unexpected start tag: found " + parser.getName() +
                        ", expected " + firstElementName);
            }
        }

//A by zhouerlong
   public AttributesInfo  loadAttributesInfo(XmlPullParser parser) { 
	   		mPackageManager = mContext.getPackageManager();
            AttributesInfo info = new AttributesInfo();
           /* info.className = parser.getAttributeValue(null, AttributesInfo.CLASSNAME);
            info.packageName = parser.getAttributeValue(null, AttributesInfo.PACKAGENAME);*/
            String className = parser.getAttributeValue(null, AttributesInfo.CLASSNAME);
            String packageName = parser.getAttributeValue(null, AttributesInfo.PACKAGENAME);
            info.className = className;
        	info.packageName = packageName;
            if(null  != packageName && className !=null){
            	
            	String[] packageNames = packageName.split(";");
            	String[] classNames = className.split(";");
            	if(packageNames.length >1){
            		for (int i = 0; i < packageNames.length; i++) {
            			if(isValidPackage(mPackageManager,packageName.split(";")[i])){
            				info.className = classNames[i];
            				info.packageName = packageNames[i];
            				break;
            			}
            		}
            	}
            	
            }
            info.screen = parser.getAttributeValue(null, AttributesInfo.SCREEN);
            info.container = parser.getAttributeValue(null, AttributesInfo.CONTAINER);
            info.x = parser.getAttributeValue(null, AttributesInfo.X);
            info.y = parser.getAttributeValue(null, AttributesInfo.Y);
            info.spanX =parser.getAttributeValue(null,AttributesInfo.SPANX) != null?(Integer.valueOf(parser.getAttributeValue(null, AttributesInfo.SPANX))):1;
            info.spanY =parser.getAttributeValue(null,AttributesInfo.SPANY) != null?(Integer.valueOf(parser.getAttributeValue(null, AttributesInfo.SPANY))):1;

            
            String sIcon = parser.getAttributeValue(null, AttributesInfo.ICON);

            if (sIcon !=null) {
                sIcon = sIcon.substring(sIcon.lastIndexOf("/")+1);
            }
            info.icon = sIcon !=null?(this.mContext.getResources().getIdentifier(sIcon, "drawable", this.mContext.getPackageName())):-1;
            
            String stitle = parser.getAttributeValue(null, AttributesInfo.TITLE);
            String name = parser.getAttributeValue(null, AttributesInfo.NAME);
            
            if (stitle != null) {
                stitle = stitle.substring(stitle.lastIndexOf("/")+1);
            }
            info.title = stitle !=null?(this.mContext.getResources().getIdentifier(stitle, "string", this.mContext.getPackageName())):-1;
            if(name !=null) {
                String t ="CN;TW;US";
                String t1 ="CN;HK;US";
                String t2 ="CN;TW;GB";
                List<String> languages = Arrays.asList(t.split(";"));
                String []names = name.split(";");
                int i=languages.indexOf(Launcher.locale);
                if(i==-1) {
                	languages = Arrays.asList(t1.split(";"));
                	i=languages.indexOf(Launcher.locale);
                }
                if(i==-1) {
                	languages = Arrays.asList(t2.split(";"));
                	i=languages.indexOf(Launcher.locale);
                }
                if(i !=-1) {
                	info.name=	names[i];
//                	renameApps.put(info.packageName, info.name);
                }
            }
            
            info.uri = parser.getAttributeValue(null, AttributesInfo.URI);
        return info;
        	
        }
    	 String FILENAME = "/default_workspace1.xml";
		private PackageManager mPackageManager;
    	
    	/**
         * Returns true if the shortcuts already exists in the database.
         * we identify a shortcut by its title and intent.
         */
        private  int  findFavoritesId(Context context,Intent intent) {
            final ContentResolver cr = context.getContentResolver();
            boolean result = false;
            int id=-1;
            if (intent !=null){
            Cursor c = cr.query(LauncherSettings.Favorites.CONTENT_URI,
                new String[] { "_ID", "config_name"}, "intent=?",
                new String[] {intent.toUri(0) }, null);
            try {
                result = c.moveToFirst();
                if(result) {
                	id =c.getInt(c.getColumnIndexOrThrow(LauncherSettings.Favorites._ID));
                	String config =c.getString(c.getColumnIndexOrThrow(LauncherSettings.Favorites.CONFIG_NAME));
                }
                
            } finally {
            	try {
                    c.close();
				} catch (Exception e) {
					// TODO: handle exception
				}
            }
            }
            return id;
        }
    	private void modifyFavoritesByConfigName(SQLiteDatabase db, int workspaceResourceId) {
            if(LauncherLog.DEBUG) {
                LauncherLog.d(TAG, "loadFavorite begin: workspaceResourceId = " + workspaceResourceId);
            }

            Intent intent = new Intent(Intent.ACTION_MAIN, null);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            ContentValues values = new ContentValues();

            boolean isexistsFile = FileUtils.isexistsFile(mContext, DefaultConfig.default_workspace_path);
            if (LOGD) Log.v(TAG, String.format("Loading favorites from resid=0x%08x", workspaceResourceId));

            PackageManager packageManager = mContext.getPackageManager();
            int i = 0;
            try {
            	
//A by zhouerlong
        		XmlPullParserFactory factory = XmlPullParserFactory.newInstance();  //创建解析代工厂实例
        		XmlPullParser parser;
        		factory.setNamespaceAware(true);
        		if (isexistsFile) {
            		  parser = factory.newPullParser();//通过工程创建XmlPullParser
            		  InputStream input;
                           input = FileUtils.loadXmlFile(DefaultConfig.default_workspace_path);
                    if (input != null)
              		parser.setInput(input, "UTF-8");
        		}else {

                    parser = mContext.getResources().getXml(workspaceResourceId);	
        		}
                
                AttributeSet attrs = Xml.asAttributeSet(parser);
                beginDocument(parser, TAG_FAVORITES);

                final int depth = parser.getDepth();

                int type;
                while (((type = parser.next()) != XmlPullParser.END_TAG ||
                        parser.getDepth() > depth) && type != XmlPullParser.END_DOCUMENT) {

                    if (type != XmlPullParser.START_TAG) {
                        continue;
                    }
                    final String name = parser.getName();
//A by zhouerlong
                    AttributesInfo info = null;
                    if (isexistsFile) {
                    	info = this.loadAttributesInfo(parser);
                       
                    }
                    if (LauncherLog.DEBUG) {
                        LauncherLog.d(TAG, "loadFavorites: name = " + name);
                    }
                    
                    if(TAG_FAVORITE.equals(name)&& info.name!=null) {
                    	ContentValues v = new ContentValues();
                    	v.put(Favorites.CONFIG_NAME, info.name);

                        intent.setComponent(new ComponentName(info.packageName,info.className));
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                                Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                        	LauncherModel.updateItemInDatabaseHelper(mContext, v, findFavoritesId(mContext, intent), "LauncherProvider");
                        }
                    }
        } catch (XmlPullParserException e) {
            Log.w(TAG, "Got exception parsing favorites.", e);
        } catch (IOException e) {
            Log.w(TAG, "Got exception parsing favorites.", e);
        } catch (RuntimeException e) {
            Log.w(TAG, "Got exception parsing favorites.", e);
        }
    		
    	}
    	
        /**
         * Loads the default set of favorite packages from an xml file.
         *
         * @param db The database to write the values into
         * @param filterContainerId The specific container id of items to load
         */
        private int loadFavorites(SQLiteDatabase db, int workspaceResourceId) {
            if(LauncherLog.DEBUG) {
                LauncherLog.d(TAG, "loadFavorite begin: workspaceResourceId = " + workspaceResourceId);
            }

            Intent intent = new Intent(Intent.ACTION_MAIN, null);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            ContentValues values = new ContentValues();

            boolean isexistsFile = FileUtils.isexistsFile(mContext, DefaultConfig.default_workspace_path);
            if (LOGD) Log.v(TAG, String.format("Loading favorites from resid=0x%08x", workspaceResourceId));

            PackageManager packageManager = mContext.getPackageManager();
            int i = 0;
            try {
//A by zhouerlong
        		XmlPullParserFactory factory = XmlPullParserFactory.newInstance();  //创建解析代工厂实例
        		XmlPullParser parser;
        		factory.setNamespaceAware(true);
        		if (isexistsFile) {
            		  parser = factory.newPullParser();//通过工程创建XmlPullParser
            		  InputStream input;
                           input = FileUtils.loadXmlFile(DefaultConfig.default_workspace_path);
                    if (input != null)
              		parser.setInput(input, "UTF-8");
        		}else {

                    parser = mContext.getResources().getXml(workspaceResourceId);	
        		}
                
                AttributeSet attrs = Xml.asAttributeSet(parser);
                beginDocument(parser, TAG_FAVORITES);

                final int depth = parser.getDepth();

                int type;
                while (((type = parser.next()) != XmlPullParser.END_TAG ||
                        parser.getDepth() > depth) && type != XmlPullParser.END_DOCUMENT) {

                    if (type != XmlPullParser.START_TAG) {
                        continue;
                    }

                    boolean added = false;
                    final String name = parser.getName();
//A by zhouerlong
                    AttributesInfo info = null;
                    if (isexistsFile) {
                    	info = this.loadAttributesInfo(parser);
                       
                    }
                    if (LauncherLog.DEBUG) {
                        LauncherLog.d(TAG, "loadFavorites: name = " + name);
                    }

                    if (TAG_INCLUDE.equals(name)) {
                        final TypedArray a = mContext.obtainStyledAttributes(attrs, R.styleable.Include);

                        final int resId = a.getResourceId(R.styleable.Include_workspace, 0);

                        if (LOGD) Log.v(TAG, String.format(("%" + (2*(depth+1)) + "s<include workspace=%08x>"),
                                "", resId));

                        if (resId != 0 && resId != workspaceResourceId) {
                            // recursively load some more favorites, why not?
                            i += loadFavorites(db, resId);
                            added = false;
                            mMaxItemId = -1;
                        } else {
                            Log.w(TAG, String.format("Skipping <include workspace=0x%08x>", resId));
                        }

                        a.recycle();

                        if (LOGD) Log.v(TAG, String.format(("%" + (2*(depth+1)) + "s</include>"), ""));
                        continue;
                    }

                    // Assuming it's a <favorite> at this point
//A by zhouerlong
                    TypedArray a = null;
                    if (!isexistsFile ) {
                    	a = mContext.obtainStyledAttributes(attrs, R.styleable.Favorite);
                    }
                    

                    long container = LauncherSettings.Favorites.CONTAINER_DESKTOP;
                    if (isexistsFile) {

                        if (info.hasValue(info.container)) {
                        	container = Long.valueOf(info.container);
                        }	
                    }else {

                        
                          if (a.hasValue(R.styleable.Favorite_container)) {
                              container = Long.valueOf(a.getString(R.styleable.Favorite_container));
                          }
                    }
                    String screen;
                    String y;
                    String x;
                    if (isexistsFile) {

                         screen = info.screen;
                          x = info.x;
                          y = info.y;
                    }else {

                          screen = a.getString(R.styleable.Favorite_screen);
                           x = a.getString(R.styleable.Favorite_x);
                           y = a.getString(R.styleable.Favorite_y);
                    }

                    values.clear();
                    values.put(LauncherSettings.Favorites.CONTAINER, container);
                    values.put(LauncherSettings.Favorites.SCREEN, screen);
                    values.put(LauncherSettings.Favorites.CELLX, x);
                    values.put(LauncherSettings.Favorites.CELLY, y);
                    if (!isexistsFile) {
                        if (LOGD) {
                            final String title = a.getString(R.styleable.Favorite_title);
                            final String pkg = a.getString(R.styleable.Favorite_packageName);
                            final String something = title != null ? title : pkg;
                            Log.v(TAG, String.format(
                                    ("%" + (2*(depth+1)) + "s<%s%s c=%d s=%s x=%s y=%s>"),
                                    "", name,
                                    (something == null ? "" : (" \"" + something + "\"")),
                                    container, screen, x, y));
                        }
                    }

                    if (TAG_FAVORITE.equals(name)) {
                    	long id;
                    	if (isexistsFile) {
                        	 id = addAppShortcut(db,values,info,packageManager,intent);
                    	}else {

                              id = addAppShortcut(db, values, a, packageManager, intent);
                              
                    	}
                        added = id >= 0;
                    } else if (TAG_SEARCH.equals(name)) {
                        added = addSearchWidget(db, values);
                    } else if (TAG_CLOCK.equals(name)) {
                        added = addClockWidget(db, values);
                    } else if (TAG_APPWIDGET.equals(name)) {
                    	if (isexistsFile) {
                            added = addAppWidget(parser, attrs, type, db, values, info, packageManager);
                    	}else {
                            added = addAppWidget((XmlResourceParser)parser, attrs, type, db, values, a, packageManager);
                    	}
                    } else if (TAG_SHORTCUT.equals(name)) {
                    	long id;
                    	if (isexistsFile) {
                             id = addUriShortcut(db, values, info);
                    	}else {
                             id = addUriShortcut(db, values, a);
                    	}
                        added = id >= 0;
                    } else if (TAG_FOLDER.equals(name)) {
                        String title;
                        int titleResId;
                        if (isexistsFile&&info.name!=null) {
                        	String s = info.spanX+info.screen+info.spanY+info.x+info.y;//将文件夹记录下来
                              titleResId = Integer.valueOf(s);
                              if(info.name!=null) {
//                            	  titleResId=-1;
                              }
                              
                        }else {
                        	
                            titleResId =  info.title;//a.getResourceId(R.styleable.Favorite_title, -1);	
                        }
//                        if (titleResId != -1) {
//                            title = mContext.getResources().getString(titleResId);
//                        } else {
                            title = mContext.getResources().getString(R.string.folder_name);
                            

                            if (isexistsFile) {
                            	if(info.name!=null) {
                            		title = info.name;
                            	}
                            }
//                        }
                        
                        values.put(LauncherSettings.Favorites.TITLE, title);
                        values.put(LauncherSettings.Favorites.TITLE_ID, titleResId);
                        long folderId = addFolder(db, values);
                        added = folderId >= 0;

                        ArrayList<Long> folderItems = new ArrayList<Long>();

                        int folderDepth = parser.getDepth();
                        while ((type = parser.next()) != XmlPullParser.END_TAG ||
                                parser.getDepth() > folderDepth) {
                            if (type != XmlPullParser.START_TAG) {
                                continue;
                            }
                            final String folder_item_name = parser.getName();
                            TypedArray ar =null;
                            if (!isexistsFile) {

                                 ar = mContext.obtainStyledAttributes(attrs,
                                        R.styleable.Favorite);
                            }
                        	
                            if (isexistsFile) {
                            	info = this.loadAttributesInfo(parser);
                               
                            }
                            values.clear();
                            values.put(LauncherSettings.Favorites.CONTAINER, folderId);

                            if (!isexistsFile) {
                            if (LOGD) {
                                final String pkg = ar.getString(R.styleable.Favorite_packageName);
                                final String uri = ar.getString(R.styleable.Favorite_uri);
                                Log.v(TAG, String.format(("%" + (2*(folderDepth+1)) + "s<%s \"%s\">"), "",
                                        folder_item_name, uri != null ? uri : pkg));
                            } }
                            if (TAG_FAVORITE.equals(folder_item_name) && folderId >= 0) {
                            	long id=-1;
                            	if (isexistsFile) {

                                  id=  addAppShortcut(db, values, info, packageManager, intent);
                            	}else {

                                    id =
                                      addAppShortcut(db, values, ar, packageManager, intent);
                            	}
                                if (id >= 0) {
                                    folderItems.add(id);
                                }
                            } else if (TAG_SHORTCUT.equals(folder_item_name) && folderId >= 0) {
                            	long id;
                                if (isexistsFile) {
                                     id = addUriShortcut(db, values, info);	
                                }else {
                                    id = addUriShortcut(db, values, ar);
                                }
                                if (id >= 0) {
                                    folderItems.add(id);
                                }
                            } else {
                                throw new RuntimeException("Folders can " +
                                        "contain only shortcuts");
                            }
                            if (isexistsFile) {
                            	
                            }else {
                                ar.recycle();
                            }
                        }
                        // We can only have folders with >= 2 items, so we need to remove the
                        // folder and clean up if less than 2 items were included, or some
                        // failed to add, and less than 2 were actually added
                        if (folderItems.size() < 2 && folderId >= 0) {
                            // We just delete the folder and any items that made it
                            deleteId(db, folderId);
                            if (folderItems.size() > 0) {
                                deleteId(db, folderItems.get(0));
                            }
                            added = false;
                        }
                    }
                    if (added) i++;
                    if (isexistsFile) {
                    	
                    }else {
                        a.recycle();
                    }
                }
            } catch (XmlPullParserException e) {
                Log.w(TAG, "Got exception parsing favorites.", e);
            } catch (IOException e) {
                Log.w(TAG, "Got exception parsing favorites.", e);
            } catch (RuntimeException e) {
                Log.w(TAG, "Got exception parsing favorites.", e);
            }

            // Update the max item id after we have loaded the database
            if (mMaxItemId == -1) {
                mMaxItemId = initializeMaxItemId(db);
            }

            if(LauncherLog.DEBUG) {
                LauncherLog.d(TAG, "loadFavorites end: i = " + i);
            }

            return i;
        }

        private long addAppShortcut(SQLiteDatabase db, ContentValues values, TypedArray a,
                PackageManager packageManager, Intent intent) {
            long id = -1;
            ActivityInfo info;
            String packageName = a.getString(R.styleable.Favorite_packageName);
            String className = a.getString(R.styleable.Favorite_className);
            try {
                ComponentName cn;
                try {
                    cn = new ComponentName(packageName, className);
                    info = packageManager.getActivityInfo(cn, 0);
                } catch (PackageManager.NameNotFoundException nnfe) {
                    String[] packages = packageManager.currentToCanonicalPackageNames(
                        new String[] { packageName });
                    cn = new ComponentName(packages[0], className);
                    info = packageManager.getActivityInfo(cn, 0);
                }
                id = generateNewItemId();
                intent.setComponent(cn);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                values.put(Favorites.INTENT, intent.toUri(0));
                values.put(Favorites.TITLE, info.loadLabel(packageManager).toString());
                values.put(Favorites.ITEM_TYPE, Favorites.ITEM_TYPE_APPLICATION);
                values.put(Favorites.SPANX, 1);
                values.put(Favorites.SPANY, 1);
                values.put(Favorites.COMPONENTNAME, cn.toString());
                values.put(Favorites._ID, id);
                values.put(LauncherSettings.Favorites.PACKAGE_NAME, intent.getComponent().getPackageName());
                if (dbInsertAndCheck(this,  TABLE_FAVORITES, null, values) < 0) {
                    return -1;
                }
            } catch (PackageManager.NameNotFoundException e) {
                Log.w(TAG, "Unable to add favorite: " + packageName +
                        "/" + className, e);
            }
            return id;
        }

        private long addFolder(SQLiteDatabase db, ContentValues values) {
            values.put(Favorites.ITEM_TYPE, Favorites.ITEM_TYPE_FOLDER);
            values.put(Favorites.SPANX, 1);
            values.put(Favorites.SPANY, 1);
            long id = generateNewItemId();
            values.put(Favorites._ID, id);
            if (dbInsertAndCheck(this,  TABLE_FAVORITES, null, values) <= 0) {
                return -1;
            } else {
                return id;
            }
        }
        
        
        //add by zhouerlong begin 
        private long addAppShortcut(SQLiteDatabase db, ContentValues values, AttributesInfo a,
        PackageManager packageManager, Intent intent) {
    long id = -1;
    ActivityInfo info;
   /* String packageName = a.getString(R.styleable.Favorite_packageName);
    String className = a.getString(R.styleable.Favorite_className);*/
    

    String packageName = a.packageName;
    String className = a.className;
    try {
        ComponentName cn;
        try {
            cn = new ComponentName(packageName, className);
            info = packageManager.getActivityInfo(cn, 0);
        } catch (PackageManager.NameNotFoundException nnfe) {
            String[] packages = packageManager.currentToCanonicalPackageNames(
                new String[] { packageName });
            cn = new ComponentName(packages[0], className);
            info = packageManager.getActivityInfo(cn, 0);
        }
        id = generateNewItemId();
        intent.setComponent(cn);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        values.put(Favorites.INTENT, intent.toUri(0));
        if(a.name!=null) {
            values.put(Favorites.CONFIG_NAME, a.name);
        }
            values.put(Favorites.TITLE, info.loadLabel(packageManager).toString());
        values.put(Favorites.ITEM_TYPE, Favorites.ITEM_TYPE_APPLICATION);
        values.put(Favorites.SPANX, 1);
        values.put(Favorites.SPANY, 1);
        values.put(Favorites._ID, id);
        values.put(Favorites.COMPONENTNAME, cn.toString());
        values.put(Favorites.PACKAGE_NAME, intent.getComponent().getPackageName());
        if (dbInsertAndCheck(this,  TABLE_FAVORITES, null, values) < 0) {
            return -1;
        }
    } catch (PackageManager.NameNotFoundException e) {
        Log.w(TAG, "Unable to add favorite: " + packageName +
                "/" + className, e);
       // throw new RuntimeException(e);
    }
    return id;
}
        //add by zhouerlong end

        
        

        private ComponentName getSearchWidgetProvider() {
            SearchManager searchManager =
                    (SearchManager) mContext.getSystemService(Context.SEARCH_SERVICE);
            ComponentName searchComponent = searchManager.getGlobalSearchActivity();
            if (searchComponent == null) return null;
            return getProviderInPackage(searchComponent.getPackageName());
        }

        /**
         * Gets an appwidget provider from the given package. If the package contains more than
         * one appwidget provider, an arbitrary one is returned.
         */
        private ComponentName getProviderInPackage(String packageName) {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(mContext);
            List<AppWidgetProviderInfo> providers = appWidgetManager.getInstalledProviders();
            if (providers == null) return null;
            final int providerCount = providers.size();
            for (int i = 0; i < providerCount; i++) {
                ComponentName provider = providers.get(i).provider;
                if (provider != null && provider.getPackageName().equals(packageName)) {
                    return provider;
                }
            }
            return null;
        }

        private boolean addSearchWidget(SQLiteDatabase db, ContentValues values) {
            ComponentName cn = getSearchWidgetProvider();
            return addAppWidget(db, values, cn, 4, 1, null);
        }

        private boolean addClockWidget(SQLiteDatabase db, ContentValues values) {
            ComponentName cn = new ComponentName("com.android.alarmclock",
                    "com.android.alarmclock.AnalogAppWidgetProvider");
            return addAppWidget(db, values, cn, 2, 2, null);
        }

        private boolean addAppWidget(XmlResourceParser parser, AttributeSet attrs, int type,
                SQLiteDatabase db, ContentValues values, TypedArray a,
                PackageManager packageManager) throws XmlPullParserException, IOException {

            String packageName = a.getString(R.styleable.Favorite_packageName);
            String className = a.getString(R.styleable.Favorite_className);

            if (packageName == null || className == null) {
                return false;
            }

            boolean hasPackage = true;
            ComponentName cn = new ComponentName(packageName, className);
            try {
                packageManager.getReceiverInfo(cn, 0);
            } catch (Exception e) {
                String[] packages = packageManager.currentToCanonicalPackageNames(
                        new String[] { packageName });
                cn = new ComponentName(packages[0], className);
                try {
                    packageManager.getReceiverInfo(cn, 0);
                } catch (Exception e1) {
                    hasPackage = false;
                }
            }

            if (hasPackage) {
                int spanX = a.getInt(R.styleable.Favorite_spanX, 0);
                int spanY = a.getInt(R.styleable.Favorite_spanY, 0);

                // Read the extras
                Bundle extras = new Bundle();
                int widgetDepth = parser.getDepth();
                while ((type = parser.next()) != XmlPullParser.END_TAG ||
                        parser.getDepth() > widgetDepth) {
                    if (type != XmlPullParser.START_TAG) {
                        continue;
                    }

                    TypedArray ar = mContext.obtainStyledAttributes(attrs, R.styleable.Extra);
                    if (TAG_EXTRA.equals(parser.getName())) {
                        String key = ar.getString(R.styleable.Extra_key);
                        String value = ar.getString(R.styleable.Extra_value);
                        if (key != null && value != null) {
                            extras.putString(key, value);
                        } else {
                            throw new RuntimeException("Widget extras must have a key and value");
                        }
                    } else {
                        throw new RuntimeException("Widgets can contain only extras");
                    }
                    ar.recycle();
                }

                return addAppWidget(db, values, cn, spanX, spanY, extras);
            }

            return false;
        }
        
        
        
        //add by zhouerlong begin
        
        private boolean addAppWidget(XmlPullParser parser, AttributeSet attrs, int type,
                SQLiteDatabase db, ContentValues values, AttributesInfo info,
                PackageManager packageManager) throws XmlPullParserException, IOException {

          /*  String packageName = a.getString(R.styleable.Favorite_packageName);
            String className = a.getString(R.styleable.Favorite_className);*/
            String packageName = info.packageName;
            String className = info.className;

            if (packageName == null || className == null) {
                return false;
            }

            boolean hasPackage = true;
            ComponentName cn = new ComponentName(packageName, className);
            try {
                packageManager.getReceiverInfo(cn, 0);
            } catch (Exception e) {
                String[] packages = packageManager.currentToCanonicalPackageNames(
                        new String[] { packageName });
                cn = new ComponentName(packages[0], className);
                try {
                    packageManager.getReceiverInfo(cn, 0);
                } catch (Exception e1) {
                    hasPackage = false;
                }
            }

            if (hasPackage) {
                /*int spanX = a.getInt(R.styleable.Favorite_spanX, 0);
                int spanY = a.getInt(R.styleable.Favorite_spanY, 0);*/

                int spanX = info.spanX;
                int spanY = info.spanY;

                // Read the extras
                Bundle extras = new Bundle();
                int widgetDepth = parser.getDepth();
                while ((type = parser.next()) != XmlPullParser.END_TAG ||
                        parser.getDepth() > widgetDepth) {
                    if (type != XmlPullParser.START_TAG) {
                        continue;
                    }

                    TypedArray ar = mContext.obtainStyledAttributes(attrs, R.styleable.Extra);
                    if (TAG_EXTRA.equals(parser.getName())) {
                        String key = ar.getString(R.styleable.Extra_key);
                        String value = ar.getString(R.styleable.Extra_value);
                        if (key != null && value != null) {
                            extras.putString(key, value);
                        } else {
                            throw new RuntimeException("Widget extras must have a key and value");
                        }
                    } else {
                        throw new RuntimeException("Widgets can contain only extras");
                    }
                    ar.recycle();
                }

                return addAppWidget(db, values, cn, spanX, spanY, extras);
            }

            return false;
        }
        //add by zhouerlong end 
        private boolean addAppWidget(SQLiteDatabase db, ContentValues values, ComponentName cn,
                int spanX, int spanY, Bundle extras) {
            boolean allocatedAppWidgets = false;
            final AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(mContext);

            try {
                int appWidgetId = mAppWidgetHost.allocateAppWidgetId();

                values.put(Favorites.ITEM_TYPE, Favorites.ITEM_TYPE_APPWIDGET);
                values.put(Favorites.SPANX, spanX);
                values.put(Favorites.SPANY, spanY);
                values.put(Favorites.APPWIDGET_ID, appWidgetId);
                values.put(Favorites.APPWIDGET_PROVIDER, cn.flattenToString());
                values.put(Favorites._ID, generateNewItemId());
                dbInsertAndCheck(this, TABLE_FAVORITES, null, values);

                allocatedAppWidgets = true;

                // TODO: need to check return value
                appWidgetManager.bindAppWidgetIdIfAllowed(appWidgetId, cn);

                // Send a broadcast to configure the widget
                if (extras != null && !extras.isEmpty()) {
                    Intent intent = new Intent(ACTION_APPWIDGET_DEFAULT_WORKSPACE_CONFIGURE);
                    intent.setComponent(cn);
                    intent.putExtras(extras);
                    intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
                    mContext.sendBroadcast(intent);
                }
            } catch (RuntimeException ex) {
                Log.e(TAG, "Problem allocating appWidgetId", ex);
            }

            return allocatedAppWidgets;
        }

        private long addUriShortcut(SQLiteDatabase db, ContentValues values,
                TypedArray a) {
            Resources r = mContext.getResources();

            final int iconResId = a.getResourceId(R.styleable.Favorite_icon, 0);
            final int titleResId = a.getResourceId(R.styleable.Favorite_title, 0);

            Intent intent;
            String uri = null;
            try {
                uri = a.getString(R.styleable.Favorite_uri);
                intent = Intent.parseUri(uri, 0);
            } catch (URISyntaxException e) {
                Log.w(TAG, "Shortcut has malformed uri: " + uri);
                return -1; // Oh well
            }

            if (iconResId == 0 || titleResId == 0) {
                Log.w(TAG, "Shortcut is missing title or icon resource ID");
                return -1;
            }

            long id = generateNewItemId();
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            values.put(Favorites.INTENT, intent.toUri(0));
            values.put(Favorites.TITLE, r.getString(titleResId));
            values.put(Favorites.TITLE_ID, titleResId);
            values.put(Favorites.ITEM_TYPE, Favorites.ITEM_TYPE_SHORTCUT);
            values.put(Favorites.SPANX, 1);
            values.put(Favorites.SPANY, 1);
            values.put(Favorites.ICON_TYPE, Favorites.ICON_TYPE_RESOURCE);
            values.put(Favorites.ICON_PACKAGE, mContext.getPackageName());
            values.put(Favorites.ICON_RESOURCE, r.getResourceName(iconResId));
            values.put(Favorites._ID, id);

            if (dbInsertAndCheck(this, TABLE_FAVORITES, null, values) < 0) {
                return -1;
            }
            return id;
        }
    
    //add by zhouerlong begin
    private long addUriShortcut(SQLiteDatabase db, ContentValues values,
    		AttributesInfo a) {
        Resources r = mContext.getResources();

        /*final int iconResId = a.getResourceId(R.styleable.Favorite_icon, 0);
        final int titleResId = a.getResourceId(R.styleable.Favorite_title, 0);*/

        final int iconResId = a.icon;
        final int titleResId = a.title;
        
        

        Intent intent;
        String uri = null;
        try {
            /*uri = a.getString(R.styleable.Favorite_uri);*/
            uri = a.uri;
            intent = Intent.parseUri(uri, 0);
        } catch (URISyntaxException e) {
            Log.w(TAG, "Shortcut has malformed uri: " + uri);
            return -1; // Oh well
        }

        if (iconResId == 0 || titleResId == 0) {
            Log.w(TAG, "Shortcut is missing title or icon resource ID");
            return -1;
        }

        long id = generateNewItemId();
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        values.put(Favorites.INTENT, intent.toUri(0));
        values.put(Favorites.TITLE, r.getString(titleResId));
            values.put(Favorites.TITLE_ID, titleResId);
            if(intent.getComponent()!=null)
            values.put(Favorites.COMPONENTNAME, intent.getComponent().toString());
        values.put(Favorites.ITEM_TYPE, Favorites.ITEM_TYPE_SHORTCUT);
        values.put(Favorites.SPANX, 1);
        values.put(Favorites.SPANY, 1);
        values.put(Favorites.ICON_TYPE, Favorites.ICON_TYPE_RESOURCE);
        values.put(Favorites.ICON_PACKAGE, mContext.getPackageName());
        values.put(Favorites.ICON_RESOURCE, r.getResourceName(iconResId));
        values.put(Favorites._ID, id);

        if (dbInsertAndCheck(this,  TABLE_FAVORITES, null, values) < 0) {
            return -1;
        }
        return id;
    }
}
    //add by zhouerlong end

    /**
     * Build a query string that will match any row where the column matches
     * anything in the values list.
     */
    static String buildOrWhereString(String column, int[] values) {
        StringBuilder selectWhere = new StringBuilder();
        for (int i = values.length - 1; i >= 0; i--) {
            selectWhere.append(column).append("=").append(values[i]);
            if (i > 0) {
                selectWhere.append(" OR ");
            }
        }
        return selectWhere.toString();
    }

    static class SqlArguments {
        public final String table;
        public final String where;
        public final String[] args;

        SqlArguments(Uri url, String where, String[] args) {
            if (url.getPathSegments().size() == 1) {
                this.table = url.getPathSegments().get(0);
                this.where = where;
                this.args = args;
            } else if (url.getPathSegments().size() != 2) {
                throw new IllegalArgumentException("Invalid URI: " + url);
            } else if (!TextUtils.isEmpty(where)) {
                throw new UnsupportedOperationException("WHERE clause not supported: " + url);
            } else {
                this.table = url.getPathSegments().get(0);
                this.where = "_id=" + ContentUris.parseId(url);
                this.args = null;
            }
        }

        SqlArguments(Uri url) {
            if (url.getPathSegments().size() == 1) {
                table = url.getPathSegments().get(0);
                where = null;
                args = null;
            } else {
                throw new IllegalArgumentException("Invalid URI: " + url);
            }
        }
    }
    
    private static boolean isValidPackage(PackageManager pm, String  packagename) {
        try {
            // Skip if the application is disabled
            PackageInfo pi = pm.getPackageInfo(packagename, 0);
            if (!pi.applicationInfo.enabled) {
                return false;
            }

            // Check the activity
           
        } catch (NameNotFoundException e) {
            return false;
        }
		return true;
    }
    
    public static String getRenameAppName(String packageName){
    	if(renameApps!= null ){
    		Iterator iter = renameApps.entrySet().iterator();
    		while(iter.hasNext()){
    			Map.Entry entry = (Map.Entry) iter.next();
    			Object key = entry.getKey();
    			if(((String)key).equals(packageName)){
    				return (String) entry.getValue();
    			}
    		}
    	}
		return null;
    }
}
 