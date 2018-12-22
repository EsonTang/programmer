package com.prize.app.database.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.text.TextUtils;

import com.prize.app.database.DownloadGameTable;
import com.prize.app.database.PrizeDatabaseHelper;
import com.prize.app.download.DownloadState;
import com.prize.app.download.DownloadTask;
import com.prize.app.net.datasource.base.AppPatch;
import com.prize.app.net.datasource.base.AppsItemBean;
import com.prize.app.util.JLog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * 前台下载DAO
 */
public class GameDAO {
    /**
     * 游戏数据库表名
     */
    private static String mColunmGameItemName[] = new String[]{
            DownloadGameTable.GAME_CODE, DownloadGameTable.GAME_PACKAGE,
            DownloadGameTable.GAME_CLASS, DownloadGameTable.GAME_NAME,
            DownloadGameTable.GAME_ICON_URL,
            DownloadGameTable.GAME_VERSION_CODE,
            DownloadGameTable.GAME_APK_SIZE, DownloadGameTable.GAME_APK_URL,
            DownloadGameTable.GAME_DOWNLOAD_STATE,
            DownloadGameTable.GAME_LOADED_SIZE, DownloadGameTable.GAME_TYPE,
            DownloadGameTable.GAME_LOAD_FLAG, DownloadGameTable.GAME_TOAPKMD5, DownloadGameTable.APP_PAGEINFO, DownloadGameTable.APP_OTHERINFO};

    // 数据库的每项对应的列
    private static final int COLUMN_GAME_ID = 0;
    private static final int COLUMN_GAME_PACKAGE = COLUMN_GAME_ID + 1;
    private static final int COLUMN_GAME_CLASS = COLUMN_GAME_PACKAGE + 1;
    private static final int COLUMN_GAME_NAME = COLUMN_GAME_CLASS + 1;
    private static final int COLUMN_GAME_ICONURL = COLUMN_GAME_NAME + 1;
    private static final int COLUMN_GAME_VERSIONCODE = COLUMN_GAME_ICONURL + 1;
    private static final int COLUMN_GAME_APKSIZE = COLUMN_GAME_VERSIONCODE + 1;
    private static final int COLUMN_GAME_DOWNURL = COLUMN_GAME_APKSIZE + 1;
    private static final int COLUMN_GAME_DOWNSTATE = COLUMN_GAME_DOWNURL + 1;
    private static final int COLUMN_GAME_DOWNLOAD_POSITION = COLUMN_GAME_DOWNSTATE + 1;
    private static final int COLUMN_GAME_GAME_TYPE = COLUMN_GAME_DOWNLOAD_POSITION + 1;
    private static final int COLUMN_GAME_LOAD_FLAG = COLUMN_GAME_GAME_TYPE + 1;
    private static final int COLUMN_GAME_GAME_TOAPKMD5 = COLUMN_GAME_LOAD_FLAG + 1;
    private static final int COLUMN_APP_PAGEINFO = COLUMN_GAME_GAME_TOAPKMD5 + 1;
    private static final int COLUMN_APP_OTHERINFO = COLUMN_APP_PAGEINFO + 1;

    private static GameDAO instance;

    private GameDAO() {
        JLog.i("GameDAO", "执行GameDAO构造函数");
    }

    public static GameDAO getInstance() {
        if (instance == null) {
            synchronized (GameDAO.class) {
                if (instance == null) {
                    instance = new GameDAO();
                }
            }
        }
        return instance;
    }

    /**
     * 创建游戏下载任务
     */
    public boolean insertGame(DownloadTask task) {
        if (null == task) {
            return false;
        }
        AppsItemBean gameBean = task.loadGame;

        ContentValues contentValues = new ContentValues();
        contentValues.put(DownloadGameTable.GAME_CODE, gameBean.id);
        contentValues.put(DownloadGameTable.GAME_PACKAGE, gameBean.packageName);
        contentValues.put(DownloadGameTable.GAME_CLASS, task.isUpdate_install);
        contentValues.put(DownloadGameTable.GAME_NAME, gameBean.name);
        if (gameBean != null && !TextUtils.isEmpty(gameBean.largeIcon)) {
            contentValues.put(DownloadGameTable.GAME_ICON_URL, gameBean.largeIcon);
        } else {
            contentValues.put(DownloadGameTable.GAME_ICON_URL, gameBean.iconUrl);
        }
        if (gameBean != null && gameBean.appPatch != null && !TextUtils.isEmpty(gameBean.appPatch.toApkMd5)) {
            contentValues.put(DownloadGameTable.GAME_TOAPKMD5, gameBean.appPatch.toApkMd5);
            contentValues.put(DownloadGameTable.GAME_APK_SIZE, gameBean.appPatch.patchSize);
            contentValues.put(DownloadGameTable.GAME_APK_URL, gameBean.appPatch.patchUrl);

        } else {
            contentValues.put(DownloadGameTable.GAME_TOAPKMD5, "");
            contentValues.put(DownloadGameTable.GAME_APK_SIZE, gameBean.apkSize);
            contentValues.put(DownloadGameTable.GAME_APK_URL, gameBean.downloadUrl);
        }
        contentValues.put(DownloadGameTable.GAME_VERSION_CODE, gameBean.versionCode);
        contentValues.put(DownloadGameTable.GAME_DOWNLOAD_STATE, task.gameDownloadState);
        contentValues.put(DownloadGameTable.GAME_LOADED_SIZE, task.gameDownloadPostion);
        //2.5 新增 用来存储versionName longbaoxiu
        contentValues.put(DownloadGameTable.GAME_TYPE, gameBean.versionName);
        contentValues.put(DownloadGameTable.GAME_LOAD_FLAG, task.loadFlag);
        //2.7 新增 用来存储s所属页面位置信息 longbaoxiu
        if (!TextUtils.isEmpty(gameBean.pageInfo)) {
            contentValues.put(DownloadGameTable.APP_PAGEINFO, gameBean.pageInfo);
        }
        //2.7 新增  longbaoxiu
        if (!TextUtils.isEmpty(gameBean.backParams)) {
            contentValues.put(DownloadGameTable.APP_OTHERINFO, gameBean.backParams);
        }
        PrizeDatabaseHelper.insert(DownloadGameTable.TABLE_NAME_GAME, null,
                contentValues);

        return true;
    }

    /**
     * 只更新修改游戏信息
     */
    public void updateGame(DownloadTask task) {
        AppsItemBean gameBean = task.loadGame;
        ContentValues contentValues = new ContentValues();
        contentValues.put(DownloadGameTable.GAME_CODE, gameBean.id);
        contentValues.put(DownloadGameTable.GAME_PACKAGE, gameBean.packageName);
        contentValues.put(DownloadGameTable.GAME_NAME, gameBean.name);
        if (gameBean != null && !TextUtils.isEmpty(gameBean.largeIcon)) {
            contentValues.put(DownloadGameTable.GAME_ICON_URL,
                    gameBean.largeIcon);
        } else {
            contentValues
                    .put(DownloadGameTable.GAME_ICON_URL, gameBean.iconUrl);
        }
        contentValues.put(DownloadGameTable.GAME_VERSION_CODE,
                gameBean.versionCode);
        contentValues.put(DownloadGameTable.GAME_APK_SIZE, gameBean.apkSize);
        contentValues.put(DownloadGameTable.GAME_APK_URL, gameBean.downloadUrl);
        // contentValues.put(DownloadGameTable.GAME_TYPE, gameBean.gameType);
        contentValues.put(DownloadGameTable.GAME_LOAD_FLAG, task.loadFlag);
        contentValues.put(DownloadGameTable.GAME_CLASS, task.isUpdate_install);

        String where = DownloadGameTable.GAME_PACKAGE + "=?";
        String[] args = new String[]{gameBean.packageName};

        PrizeDatabaseHelper.update(DownloadGameTable.TABLE_NAME_GAME,
                contentValues, where, args);
    }

    /**
     * 只更新app所在页面信息
     */
    public void updatePageInfo(String packageName, String pageInfo) {
        if (TextUtils.isEmpty(packageName) || TextUtils.isEmpty(pageInfo)) return;
        ContentValues contentValues = new ContentValues();
        contentValues.put(DownloadGameTable.APP_PAGEINFO, pageInfo);
        String where = DownloadGameTable.GAME_PACKAGE + "=?";
        String[] args = new String[]{packageName};
        PrizeDatabaseHelper.update(DownloadGameTable.TABLE_NAME_GAME,
                contentValues, where, args);
    }

    /**
     * 根据pkgname删除一个游戏数据
     */
    public void deleteData(String pkgName) {
        PrizeDatabaseHelper
                .delete(DownloadGameTable.TABLE_NAME_GAME,
                        DownloadGameTable.GAME_PACKAGE + "=?",
                        new String[]{pkgName});
    }


    /**
     * 修改下载状态，只需记录
     * STATE_DOWNLOAD_SUCESS，STATE_DOWNLOAD_ERROR，STATE_DOWNLOAD_PAUSE
     * ，恢复时，用于区别下载状态
     *
     * @param pkgName 包名
     * @param state   下载状态
     */
    public void updateState(String pkgName, int state) {
        if ((DownloadState.STATE_DOWNLOAD_SUCESS == state)
                || (DownloadState.STATE_DOWNLOAD_ERROR == state)
                || (DownloadState.STATE_DOWNLOAD_PAUSE == state)) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(DownloadGameTable.GAME_DOWNLOAD_STATE, state);
            PrizeDatabaseHelper.update(DownloadGameTable.TABLE_NAME_GAME,
                    contentValues, DownloadGameTable.GAME_PACKAGE + "=?",
                    new String[]{pkgName});
        }
    }

    /**
     * 获取下载任务 下载中及待下载的的app
     *
     * @return ArrayList<AppsItemBean>
     */
    public ArrayList<AppsItemBean> getDownloadAppList() {
        ArrayList<AppsItemBean> list = new ArrayList<AppsItemBean>();
        /*
         * String sql =
		 * "select * from table_game where download_state=0 or download_state=4;"
		 * ; Cursor cursor = PrizeDatabaseHelper.rawQuery(sql, null);
		 */
        Cursor cursor = PrizeDatabaseHelper.query(
                DownloadGameTable.TABLE_NAME_GAME, mColunmGameItemName, null,
                null, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                if (cursor.getLong(COLUMN_GAME_APKSIZE) != cursor
                        .getLong(COLUMN_GAME_DOWNLOAD_POSITION)) {
                    AppsItemBean loadGame = new AppsItemBean();
                    loadGame.id = cursor.getString(COLUMN_GAME_ID);
                    loadGame.packageName = cursor
                            .getString(COLUMN_GAME_PACKAGE);
                    loadGame.name = cursor.getString(COLUMN_GAME_NAME);
                    loadGame.iconUrl = cursor.getString(COLUMN_GAME_ICONURL);
                    loadGame.versionCode = cursor.getInt(COLUMN_GAME_VERSIONCODE);
                    loadGame.apkSize = cursor.getLong(COLUMN_GAME_APKSIZE) + "";
                    loadGame.downloadUrl = cursor.getString(COLUMN_GAME_DOWNURL);
                    loadGame.versionName = cursor.getString(COLUMN_GAME_GAME_TYPE);
                    loadGame.pageInfo = cursor.getString(COLUMN_APP_PAGEINFO);
                    loadGame.backParams = cursor.getString(COLUMN_APP_OTHERINFO);
                    list.add(loadGame);
                }
            }
            if (JLog.isDebug) {
                JLog.i("GameDAO", "list.size()=" + list.size());
            }
            Collections.reverse(list);
            cursor.close();
        }
        return list;
    }

//    /**
//     * 获取暂停的任务个数是否大于等于2
//     *
//     * @return boolean 大于等于2返回true
//     */
//    public boolean hasPauseTaskMoreTwo() {
//        Cursor cursor = PrizeDatabaseHelper.query(
//                DownloadGameTable.TABLE_NAME_GAME, mColunmGameItemName, DownloadGameTable.GAME_DOWNLOAD_STATE+ "=?",
//                new String[]{String.valueOf(DownloadState.STATE_DOWNLOAD_PAUSE)}, null, null, null);
//        if (JLog.isDebug) {
//            JLog.i("GameDAO","hasPauseTaskMoreTwo-cursor.getCount()="+cursor.getCount());
//        }
//        return cursor!=null&&cursor.getCount()>=2;
//    }

    /**
     * 更新下载进度
     *
     * @param pkgName 包名
     * @param pos     总大小
     */
    public void updateDownloadSize(String pkgName, long totalSize, long pos) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DownloadGameTable.GAME_APK_SIZE, totalSize);
        contentValues.put(DownloadGameTable.GAME_LOADED_SIZE, pos);
        PrizeDatabaseHelper.update(DownloadGameTable.TABLE_NAME_GAME,
                contentValues, DownloadGameTable.GAME_PACKAGE + "=?",
                new String[]{pkgName});
    }

    public void updateGameFlag(DownloadTask loadGameTask) {
        if (null == loadGameTask) {
            return;
        }
        ContentValues contentValues = new ContentValues();
        contentValues.put(DownloadGameTable.GAME_LOAD_FLAG,
                loadGameTask.loadFlag);
        String pkgName = loadGameTask.loadGame.packageName;

        PrizeDatabaseHelper.update(DownloadGameTable.TABLE_NAME_GAME,
                contentValues, DownloadGameTable.GAME_PACKAGE + "=?",
                new String[]{pkgName});
    }

    /**
     * 获取所有的下载任务
     *
     * @return HashMap
     */
    public HashMap<String, DownloadTask> getAllDownloadExeTask() {
        HashMap<String, DownloadTask> dataMap = new HashMap<String, DownloadTask>();
        Cursor cursor = PrizeDatabaseHelper.query(
                DownloadGameTable.TABLE_NAME_GAME, mColunmGameItemName, null,
                null, null, null, null);
        DownloadTask task;
        if (cursor != null) {
            while (cursor.moveToNext()) {
                task = sqlToDownloadTask(cursor);
                dataMap.put(task.loadGame.packageName, task);
            }
            cursor.close();
        }

        return dataMap;
    }

    /**
     * 转换为下载任务
     *
     * @param cursor Cursor
     * @return DownloadTask
     */
    private DownloadTask sqlToDownloadTask(Cursor cursor) {
        DownloadTask data = new DownloadTask();
        AppsItemBean loadGame = data.loadGame;
        loadGame.id = cursor.getString(COLUMN_GAME_ID);
        loadGame.packageName = cursor.getString(COLUMN_GAME_PACKAGE);
        loadGame.name = cursor.getString(COLUMN_GAME_NAME);
        loadGame.iconUrl = cursor.getString(COLUMN_GAME_ICONURL);
        loadGame.versionCode = cursor.getInt(COLUMN_GAME_VERSIONCODE);
        loadGame.apkSize = cursor.getLong(COLUMN_GAME_APKSIZE) + "";
        loadGame.downloadUrl = cursor.getString(COLUMN_GAME_DOWNURL);
        loadGame.versionName = cursor.getString(COLUMN_GAME_GAME_TYPE);
        loadGame.installType = cursor.getString(COLUMN_GAME_CLASS);
        loadGame.downloadUrl = cursor.getString(COLUMN_GAME_DOWNURL);
        data.gameDownloadState = cursor.getInt(COLUMN_GAME_DOWNSTATE);
        String toApkmd5 = cursor.getString(COLUMN_GAME_GAME_TOAPKMD5);
        AppPatch appPatch = null;
        if (!TextUtils.isEmpty(toApkmd5)) {
            appPatch = new AppPatch();
            appPatch.toApkMd5 = toApkmd5;
            appPatch.patchUrl = loadGame.downloadUrl;
            appPatch.patchSize = Long.parseLong(loadGame.apkSize);
        }
        if ((DownloadState.STATE_DOWNLOAD_WAIT == data.gameDownloadState)
                || DownloadState.STATE_DOWNLOAD_START_LOADING == data.gameDownloadState) {
            // 如果是wait or
            // loading的状态，修改成暂停。因为APP已经被退出了，另外，兼容之前的版本，之前记录loading的状态，需要转换回来
            data.gameDownloadState = DownloadState.STATE_DOWNLOAD_PAUSE;
        }
        int loadsize = (int) cursor.getLong(COLUMN_GAME_DOWNLOAD_POSITION);
        data.setDownloadSize(Integer.parseInt(loadGame.apkSize), loadsize);

        data.loadFlag = cursor.getInt(COLUMN_GAME_LOAD_FLAG);
        data.isUpdate_install = cursor.getString(COLUMN_GAME_CLASS);
        return data;
    }


    /***
     * 更新应用的URL * @param url * @param pkg
     */
    public final void updateDownUrl(final String url, final String pkg) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DownloadGameTable.GAME_APK_URL, url);
        PrizeDatabaseHelper.update(DownloadGameTable.TABLE_NAME_GAME,
                contentValues, DownloadGameTable.GAME_PACKAGE + "=?",
                new String[]{pkg});
    }

    /***
     * @param pkg  包名
     * @return String  url
     */
    public final String getAppDownUrl(String pkg) {
        String result = null;
        Cursor cursor = PrizeDatabaseHelper.query(
                DownloadGameTable.TABLE_NAME_GAME,
                new String[]{DownloadGameTable.GAME_APK_URL},
                DownloadGameTable.GAME_PACKAGE + "=?", new String[]{pkg},
                null, null, null);
        if (cursor != null) {
            if (cursor.moveToNext()) {
                result = cursor.getString(0);
            }
            cursor.close();
        }
        return result;
    }

    /***
     * @param pkg  包名
     * @return String  返回app的页面信息
     */
    public final String getAppPageInf(String pkg) {
        String result = null;
        Cursor cursor = PrizeDatabaseHelper.query(
                DownloadGameTable.TABLE_NAME_GAME,
                new String[]{DownloadGameTable.APP_PAGEINFO},
                DownloadGameTable.GAME_PACKAGE + "=?", new String[]{pkg},
                null, null, null);
        if (cursor != null) {
            if (cursor.moveToNext()) {
                result = cursor.getString(0);
            }
            cursor.close();
        }
        return result;
    }

    /***
     * @param pkg 包名
     * @return String  360打点数据
     */
    public final String getAppBackParams(String pkg) {
        String result = null;
        Cursor cursor = PrizeDatabaseHelper.query(
                DownloadGameTable.TABLE_NAME_GAME,
                new String[]{DownloadGameTable.APP_OTHERINFO},
                DownloadGameTable.GAME_PACKAGE + "=?", new String[]{pkg},
                null, null, null);
        if (cursor != null) {
            if (cursor.moveToNext()) {
                result = cursor.getString(0);
            }
            cursor.close();
        }
        return result;
    }

}