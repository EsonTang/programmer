package com.prize.app.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.prize.app.BaseApplication;
import com.prize.app.threads.SQLSingleThreadExcutor;
import com.prize.app.util.CommonUtils;
import com.prize.app.util.DataStoreUtils;
import com.prize.app.util.JLog;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


/**
 * 数据库操作类（主要是下载进程使用？）
 *
 * @author longbaoxiu
 * @version V1.0
 */
public class PrizeDatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "PrizeDatabaseHelper";
    private static final String DB_NAME = "DownloadManager.db";// 数据库名称
    /**
     * 2.0版本升级3; 2.5版本升4（modify by longbaoxiu）：2.7版本升5;2.9版本升级到6
     **/
    private static final int DB_VERSION = 6;// 数据库版本
    private static SQLiteDatabase database;
    private static PrizeDatabaseHelper dbHelper;


    /***
     * SQL语句
     */
    public static final String SQL_DELETE_TABLE = "drop table if exists ";
    public static final String SQL_CREATE_TABLE = "create table if not exists ";

    /**
     * SQLiteOpenHelper
     */
    public PrizeDatabaseHelper() {
        super(BaseApplication.curContext, DB_NAME, null, DB_VERSION);
    }

    public static synchronized void initPrizeSQLiteDatabase() {
        if (null == dbHelper) {
            try {
                dbHelper = new PrizeDatabaseHelper();
                database = dbHelper.getWritableDatabase();
                Log.i(TAG, "initPrizeSQLiteDatabase-dbHelper=" + dbHelper + "---database=" + database);
            } catch (Exception e) {
                e.printStackTrace();
                Log.i(TAG, "initPrizeSQLiteDatabase-" + e.getMessage());
            }
        }
        try {
            if (null == database) {
                dbHelper.close();
                dbHelper = null;
                dbHelper = new PrizeDatabaseHelper();
                database = dbHelper.getWritableDatabase();
                Log.i(TAG, "initPrizeSQLiteDatabase-if(database==null)--dbHelper=" + dbHelper + "database=" + database);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.i(TAG, "initPrizeSQLiteDatabase-if (database == null)-" + e.getMessage());
            BaseApplication.curContext.deleteDatabase(DB_NAME);

        }
    }

//    public static void releasePrizeDataBase() {
//        if (null != database) {
//            database.close();
//            database = null;
//        }
//
//        if (null != dbHelper) {
//            dbHelper.close();
//            dbHelper = null;
//        }
//    }

    public static SQLiteDatabase getDatabase() {
        if (null == database) {
            initPrizeSQLiteDatabase();
        }
        return database;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.i(TAG, "onCreate");
        db.execSQL(DownloadGameTable.SQL_CREATE_GAME_TABLE);
        db.execSQL(DownLoadedTable.SQL_CREATE_DOWNLOADED_TABLE);
        db.execSQL(PersonTable.SQL_CREATE_PERSON_TABLE);
        db.execSQL(AccountTable.SQL_CREATE_ACCOUNT_TABLE);
        db.execSQL(BackGroundDownloadTable.SQL_CREATE_APP_TABLE);
        db.execSQL(InstalledAppTable.SQL_CREATE);
        //2.5版本增加：同一版本的app只提醒一次 longbaoxiu 2017/02/22
        db.execSQL(PushTable.SQL_CREATE_PUSH_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        JLog.i(TAG, "onUpgrade--oldVersion=" + oldVersion + "--newVersion=" + newVersion);
        // 使用for实现跨版本升级数据库
        for (int i = oldVersion; i <= newVersion; i++) {
            switch (i) {
                case 6:
                    changeTable(db, DownLoadedTable.TABLE_NAME_D0WNLOADED, DownLoadedTable.SQL_CREATE_DOWNLOADED_TABLE, DownLoadedTable.APK_PAGEINFO);
                    break;
                case 5:
                    changeTable(db, DownloadGameTable.TABLE_NAME_GAME, DownloadGameTable.SQL_CREATE_GAME_TABLE, DownloadGameTable.APP_PAGEINFO);
                    changeTable(db, DownloadGameTable.TABLE_NAME_GAME, DownloadGameTable.SQL_CREATE_GAME_TABLE, DownloadGameTable.APP_OTHERINFO);
                    break;
                case 4:
                    changeTable(db, DownLoadedTable.TABLE_NAME_D0WNLOADED, DownLoadedTable.SQL_CREATE_DOWNLOADED_TABLE, DownLoadedTable.GAME_APK_VERSIONNAME);
                    changeTable(db, DownLoadedTable.TABLE_NAME_D0WNLOADED, DownLoadedTable.SQL_CREATE_DOWNLOADED_TABLE, DownLoadedTable.APK_DOWNLOADED_STAMP);
                    changeTable(db, DownLoadedTable.TABLE_NAME_D0WNLOADED, DownLoadedTable.SQL_CREATE_DOWNLOADED_TABLE, DownLoadedTable.APK_INSTALL_TYPE);
                    break;
                case 3:
                    changeTable(db, DownloadGameTable.TABLE_NAME_GAME, DownloadGameTable.SQL_CREATE_GAME_TABLE, DownloadGameTable.GAME_TOAPKMD5);
                    changeTable(db, BackGroundDownloadTable.TABLE_NAME_APP, BackGroundDownloadTable.SQL_CREATE_APP_TABLE, BackGroundDownloadTable.GAME_TOAPKMD5);
                    break;
                case 2:
                    clearDB(db);
                    break;

                default:
                    break;
            }
        }
    }


    /**
     * 2.0新增 增加表字段
     *
     * @param db               SQLiteDatabase
     * @param tableName        表名
     * @param sql_create_table 创建表的sql语句
     * @param columName        增加的表字段名称
     */
    private static void changeTable(SQLiteDatabase db, String tableName, String sql_create_table, String columName) {

        //2.5版本修正：把写死的字段当做参数  处理  longbnaoxiu 2017/02/22
        String sql1 = "ALTER TABLE " + tableName + " ADD COLUMN " + columName + " VARCHAR";
        if (JLog.isDebug) {
            JLog.i(TAG, "changeTable-tableName=" + tableName + "--sql_create_table=" + sql_create_table + "----db=" + db);
        }
        try {
            db.execSQL(sql_create_table);
            db.execSQL(sql1);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.i(TAG, "onDowngrade-oldVersion=" + oldVersion + "---newVersion=" + newVersion);
        Locale locale = Locale.getDefault();
        if (Locale.CHINA.getCountry().equals(locale.getCountry())) {
            Toast.makeText(BaseApplication.curContext,
                    "你进行了降级操作,为确保您的正常使用,请升级市场新版本.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(
                    BaseApplication.curContext,
                    "You degraded operation, to ensure that you properly, please upgrade to the new version.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 清空数据表 并重新创建
     *
     * @param db SQLiteDatabase
     */
    private static void clearDB(SQLiteDatabase db) {
        // 数据库降级，清空数据库，确保能启动

//		db.execSQL(DownloadGameTable.SQL_DELETE_GAME_TABLE);
//		db.execSQL(DownloadGameTable.SQL_CREATE_GAME_TABLE);
//
//		db.execSQL(MygamesTable.SQL_DELETE_MY_GAMES_TABLE);
//		db.execSQL(MygamesTable.SQL_CREATE_MY_GAMES_TABLE);

//        db.execSQL(HomeTable.SQL_DELETE_HOME_TABLE);
//        db.execSQL(HomeTable.SQL_CREATE_HOME_TABLE);

//		db.execSQL(DownLoadedTable.SQL_DELETE_GAME_TABLE);
//		db.execSQL(DownLoadedTable.SQL_CREATE_DOWNLOADED_TABLE);
//
//		db.execSQL(SearchHistory.SQL_DELETE_TABLE);
//		db.execSQL(SearchHistory.SQL_CREATE_GAME_TABLE);
//
//		db.execSQL(InstalledAppTable.SQL_DELETE);
//		db.execSQL(InstalledAppTable.SQL_CREATE);
//
//		db.execSQL(PersonTable.SQL_DELETE_PERSON_TABLE);
//		db.execSQL(PersonTable.SQL_CREATE_PERSON_TABLE);
//
//		db.execSQL(AccountTable.SQL_DELETE_ACCOUNT_TABLE);
//		db.execSQL(AccountTable.SQL_CREATE_ACCOUNT_TABLE);
//
//		db.execSQL(CoverTable.SQL_DELETE_COVER_TABLE);
//		db.execSQL(CoverTable.SQL_CREATE_COVER_TABLE);
//
//		db.execSQL(DownLoadDataTable.SQL_DELETE_DOWNLOADDATA_TABLE);
//		db.execSQL(DownLoadDataTable.SQL_CREATE_DOWNLOADDATA_TABLE);
//
//		db.execSQL(PushTable.SQL_DELETE_PUSH_TABLE);
//		db.execSQL(PushTable.SQL_CREATE_PUSH_TABLE);


    }

    /**
     * 清空数据表 并重新创建
     *
     * @param db        SQLiteDatabase
     * @param tableName 表名称
     */
    private static void clearDBTable(SQLiteDatabase db, String tableName) {
        // 数据库降级，清空数据库，确保能启动
        if (JLog.isDebug) {
            JLog.i(TAG, "clearDBTable-tableName=" + tableName);
        }
        switch (tableName) {
            case DownloadGameTable.TABLE_NAME_GAME:
                db.execSQL(DownloadGameTable.SQL_DELETE_GAME_TABLE);
                db.execSQL(DownloadGameTable.SQL_CREATE_GAME_TABLE);
                break;

//            case MygamesTable.TABLE_NAME_MYGAMES:
//                db.execSQL(MygamesTable.SQL_DELETE_MY_GAMES_TABLE);
//                db.execSQL(MygamesTable.SQL_CREATE_MY_GAMES_TABLE);
//                break;
//            case HomeTable.TABLE_NAME_GAME:
//                db.execSQL(HomeTable.SQL_DELETE_HOME_TABLE);
//                db.execSQL(HomeTable.SQL_CREATE_HOME_TABLE);
//                break;
            case DownLoadedTable.TABLE_NAME_D0WNLOADED:
                db.execSQL(DownLoadedTable.SQL_DELETE_GAME_TABLE);
                db.execSQL(DownLoadedTable.SQL_CREATE_DOWNLOADED_TABLE);

                break;
            case InstalledAppTable.TABLE_NAME:
                db.execSQL(InstalledAppTable.SQL_DELETE);
                db.execSQL(InstalledAppTable.SQL_CREATE);
                break;
            case SearchHistory.TABLE_NAME:
                db.execSQL(SearchHistory.SQL_DELETE_TABLE);
                db.execSQL(SearchHistory.SQL_CREATE_GAME_TABLE);
                break;
            case PushTable.TABLE_NAME_PUSH:
                db.execSQL(PushTable.SQL_DELETE_PUSH_TABLE);
                db.execSQL(PushTable.SQL_CREATE_PUSH_TABLE);
                break;

//            case DownLoadDataTable.TABLE_NAME_DOWNLOAD_DATA:
//                db.execSQL(DownLoadDataTable.SQL_DELETE_DOWNLOADDATA_TABLE);
//                db.execSQL(DownLoadDataTable.SQL_CREATE_DOWNLOADDATA_TABLE);
//                break;
            case AccountTable.TABLE_NAME_ACCOUNT:
                db.execSQL(AccountTable.SQL_DELETE_ACCOUNT_TABLE);
                db.execSQL(AccountTable.SQL_CREATE_ACCOUNT_TABLE);
                break;
            case PersonTable.TABLE_NAME_PERSON:
                db.execSQL(PersonTable.SQL_DELETE_PERSON_TABLE);
                db.execSQL(PersonTable.SQL_CREATE_PERSON_TABLE);
                break;
//            case CoverTable.TABLE_NAME_GAME:
//                db.execSQL(CoverTable.SQL_DELETE_COVER_TABLE);
//                db.execSQL(CoverTable.SQL_CREATE_COVER_TABLE);
//                break;
        }

    }

    /**
     * 执行SQL语句
     *
     * @param sql sql语句
     */
    public static void executeSQL(final String sql, final Object[] bindArgs) {
        SQLSingleThreadExcutor.getInstance().execute(new Runnable() {

            @Override
            public void run() {
                try { // try - catch 原因:防止数据库前后版本不匹配,导致数据异常
                    if (null != bindArgs) {
                        getDatabase().execSQL(sql, bindArgs);
                    } else {
                        getDatabase().execSQL(sql);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static void update(final String table, final ContentValues values,
                              final String whereClause, final String[] whereArgs) {
        SQLSingleThreadExcutor.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                try { // try - catch 原因:防止数据库前后版本不匹配,导致数据异常
                    getDatabase().update(table, values, whereClause, whereArgs);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
    }

    public static void insert(final String table, final String nullColumnHack,
                              final ContentValues values) {
        SQLSingleThreadExcutor.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                try { // try - catch 原因:防止数据库前后版本不匹配,导致数据异常

                    getDatabase().insertWithOnConflict(table, nullColumnHack, values, SQLiteDatabase.CONFLICT_REPLACE);
//                    long id = getDatabase().replace(table, nullColumnHack, values);
//                    if (id == -1) {
//                        Log.i(TAG, "insert Exception =-1");
//                        if (TextUtils.isEmpty(DataStoreUtils.readLocalInfo("prize_remote_crash"))) {
//                            DataStoreUtils.saveLocalInfo("prize_remote_crash", "1");
//                        } else {
//                            clearDBTable(getDatabase(), table);
//                            DataStoreUtils.saveLocalInfo("prize_remote_crash", "");
//                        }
//                        CommonUtils.killProcessByPId(BaseApplication.curContext, false);
//                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error inserting " + values, e);
                    if (!TextUtils.isEmpty(e.toString()) && e.toString().contains("SQLiteReadOnlyDatabaseException")) {
                        if (TextUtils.isEmpty(DataStoreUtils.readLocalInfo("prize_remote_crash"))) {
                            DataStoreUtils.saveLocalInfo("prize_remote_crash", "1");
                        } else {
                            clearDBTable(getDatabase(), table);
                            DataStoreUtils.saveLocalInfo("prize_remote_crash", "");
                        }
                        CommonUtils.killProcessByPId(BaseApplication.curContext, false);
                    }
                }
            }
        });
    }

    public static void delete(final String table, final String whereClause,
                              final String[] whereArgs) {
        SQLSingleThreadExcutor.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                try { // try - catch 原因:防止数据库前后版本不匹配,导致数据异常
                    getDatabase().delete(table, whereClause, whereArgs);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
    }

//    public static boolean deleteGame(final String table,
//                                     final String whereClause, final String[] whereArgs) {
//        int a = getDatabase().delete(table, whereClause, whereArgs);
//        return a > 0;
//    }

    /**
     * 方法描述：删除列表里面的数据返回状态
     */
    public static int deleteCollection(final String table,
                                       final String whereClause, final String[] whereArgs) {
        int state = 0;
        try {
            state = getDatabase().delete(table, whereClause, whereArgs);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return state;
    }

    /**
     * 方法描述：替换列表数据
     */
    public static void replaceAll(final String table,
                                  final ArrayList<ContentValues> values) {
        SQLSingleThreadExcutor.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                beginTransaction();
                try {
                    for (ContentValues contentValues : values) {
                        getDatabase().replace(table, null, contentValues);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                setTransactionSuccessful();
                endTransaction();
            }
        });
    }

    /**
     * 方法描述：替换列表数据
     */
    public static void replace(final String table,
                               final ContentValues contentValues) {
        SQLSingleThreadExcutor.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    getDatabase().replace(table, null, contentValues);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 方法描述：清空列表中的所有数据
     */
    public static void deleteAll(final String table) {
        SQLSingleThreadExcutor.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                try { // try - catch 原因:防止数据库前后版本不匹配,导致数据异常
                    String sql_delete_all = "TRUNCATE TABLE " + table;
                    getDatabase().execSQL(sql_delete_all);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 方法描述：清空列表中的所有数据
     */
    public static void deleteAllData(final String table) {
        SQLSingleThreadExcutor.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                try { // try - catch 原因:防止数据库前后版本不匹配,导致数据异常
                    String sql_delete_all = "DELETE FROM " + table + ";";
                    getDatabase().execSQL(sql_delete_all);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 查询操作可以多线程
     */
    public static Cursor query(String table, String[] columns,
                               String selection, String[] selectionArgs, String groupBy,
                               String having, String orderBy) {

        try {

            return getDatabase().query(table, columns, selection,
                    selectionArgs, groupBy, having, orderBy);
        } catch (Exception e) {
            try {
                // 如果数据查询操作异常，将清除所有数据，重新创建数据库
//				clearDB(database);
                Log.i(TAG, "query=" + e.getMessage());
                Log.i(TAG, "query Exception =-1");
//                if (TextUtils.isEmpty(DataStoreUtils.readLocalInfo("prize_remote_crash"))) {
//                    DataStoreUtils.saveLocalInfo("prize_remote_crash", "1");
//                } else {
                clearDBTable(getDatabase(), table);
                DataStoreUtils.saveLocalInfo("prize_remote_crash", "");
//                }
//                CommonUtils.killProcessByPId(BaseApplication.curContext, false);
            } catch (Exception e2) {
                e2.printStackTrace();
            }
            return null;
        }
    }

    public static void beginTransaction() {
        try {
            getDatabase().beginTransaction();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void endTransaction() {
        try {
            getDatabase().endTransaction();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void setTransactionSuccessful() {
        try {
            getDatabase().setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

//    public static Cursor rawQuery(String sql, String[] selectionArgs) {
//        return getDatabase().rawQuery(sql, selectionArgs);
//    }

    /**
     * 插入或更新已安装应用表<br>
     * 只针对 InstalledAppTable表操作
     *
     * @param cv ContentValues
     * @return 0: fail, else: success
     */
    public static long updateInstalledTable(ContentValues cv) {
        SQLiteDatabase db = getDatabase();
        return updateInstalledTable(cv, db);
    }

    /***
     * 插入或更新已安装应用表<br>
     * 只针对 InstalledAppTable表操作
     *
     * @param cv  ContentValues
     * @return 0: fail, else: success
     */
    private static long updateInstalledTable(ContentValues cv, SQLiteDatabase db) {
        if (db == null)
            return 0;
        String pkg = cv.getAsString(InstalledAppTable.PKG_NAME);
        if (TextUtils.isEmpty(pkg))
            return 0;

        String[] args = new String[]{pkg};
        try {
            Cursor c = db.query(InstalledAppTable.TABLE_NAME, null,
                    InstalledAppTable.PKG_NAME + "=?", args, null, null, null);

            if (null == c || c.getCount() < 1) {
                if (c != null)
                    c.close();
                return db.insert(InstalledAppTable.TABLE_NAME, null, cv);
            } else if (c.getCount() > 0) {
                c.close();
                return db.update(InstalledAppTable.TABLE_NAME, cv,
                        InstalledAppTable.PKG_NAME + "=?", args);
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return 0;
    }

    /***
     * 批量插入已安装应用的数据
     *
     * @param datas 数据
     * @return 0 : fail, -1: no data, 1:success
     */
    public static int batchInsert(List<ContentValues> datas) {
        if (null == datas || datas.size() < 1)
            return -1;
        SQLiteDatabase db = getDatabase();
        if (db == null) {
            return -1;
        }
        try {
            db.beginTransaction();
            int sz = datas.size();
            for (int i = 0; i < sz; i++) {
                ContentValues cv = datas.get(i);
                updateInstalledTable(cv, db);
            }
            if (db != null) {
                db.setTransactionSuccessful();
                db.endTransaction();
            }
            return 1;
        } catch (Exception e) {
            e.printStackTrace();
            if (db != null) {
                db.endTransaction();
            }
            return 0;
        }
    }


//    protected static String[] getColumnNames(SQLiteDatabase db, String tableName) {
//
//        String[] columnNames = null;
//        Cursor c = null;
//
//        try {
//            c = db.rawQuery("PRAGMA table_info(" + tableName + ")", null);
//            if (null != c) {
//                int columnIndex = c.getColumnIndex("name");
//                if (-1 == columnIndex) {
//                    return null;
//                }
//
//                int index = 0;
//                columnNames = new String[c.getCount()];
//                for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
//                    columnNames[index] = c.getString(columnIndex);
//                    JLog.i(TAG, "columnNames[" + index + "]=" + columnNames[index]);
//                    index++;
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            if (c != null) {
//                c.close();
//            }
//        }
//
//        return columnNames;
//    }
}
