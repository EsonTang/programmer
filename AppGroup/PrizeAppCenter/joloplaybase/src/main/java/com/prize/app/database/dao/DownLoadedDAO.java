package com.prize.app.database.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.text.TextUtils;

import com.prize.app.database.DownLoadedTable;
import com.prize.app.database.PrizeDatabaseHelper;
import com.prize.app.net.datasource.base.AppsItemBean;
import com.prize.app.util.JLog;

import java.util.ArrayList;
import java.util.Collections;

/**
 * 类描述：已下载列表操作管理工具
 *
 * @author huanglingjun
 * @version 版本
 */
public class DownLoadedDAO {
//	/** 字段集合 */
//	public static String mColunmGameItemName[] = new String[] {
//			DownLoadedTable.GAME_APPID, DownLoadedTable.GAME_PACKAGE,
//			DownLoadedTable.GAME_NAME, DownLoadedTable.GAME_ICON_URL,
//			DownLoadedTable.GAME_VERSION_CODE, DownLoadedTable.GAME_APK_SIZE,
//			DownLoadedTable.GAME_APK_URL,
//			DownLoadedTable.GAME_APK_VERSIONNAME,
//			DownLoadedTable.APK_DOWNLOADED_STAMP,
//			DownLoadedTable.APK_INSTALL_TYPE };

    private static DownLoadedDAO instance;

    public static DownLoadedDAO getInstance() {
        if (null == instance) {
            instance = new DownLoadedDAO();
        }
        return instance;
    }

//	/** 插入列表 */
//	public boolean insertApp(AppsItemBean appBean) {
//		ContentValues contentValues = new ContentValues();
//		contentValues.put(DownLoadedTable.GAME_APPID, appBean.id);
//		contentValues.put(DownLoadedTable.GAME_PACKAGE, appBean.packageName);
//		contentValues.put(DownLoadedTable.GAME_NAME, appBean.name);
//		if (appBean != null && !TextUtils.isEmpty(appBean.largeIcon)) {
//			contentValues.put(DownLoadedTable.GAME_ICON_URL, appBean.largeIcon);
//
//		} else {
//
//			contentValues.put(DownLoadedTable.GAME_ICON_URL, appBean.iconUrl);
//		}
//		contentValues.put(DownLoadedTable.GAME_VERSION_CODE,
//				appBean.versionCode);
//		contentValues.put(DownLoadedTable.GAME_APK_SIZE, appBean.apkSize);
//		contentValues.put(DownLoadedTable.GAME_APK_URL, appBean.downloadUrl);
//		PrizeDatabaseHelper.insert(DownLoadedTable.TABLE_NAME_D0WNLOADED, null,
//				contentValues);
//
//		return true;
//	}

//	public ArrayList<ContentValues> getAllValues(
//			ArrayList<AppsItemBean> collections) {
//		ArrayList<ContentValues> values = new ArrayList<ContentValues>();
//		for (int i = 0; i < collections.size(); i++) {
//			AppsItemBean itemBean = collections.get(i);
//			values.add(getContentValues(itemBean));
//		}
//		return values;
//	}

    public ContentValues getContentValues(AppsItemBean appBean) {
        if (JLog.isDebug) {
            JLog.i("DownLoadedDAO", "--getContentValues--" + appBean+"--appBean.pageInfo="+appBean.pageInfo);
        }
        ContentValues contentValues = new ContentValues();
        contentValues.put(DownLoadedTable.GAME_APPID, appBean.id);
        contentValues.put(DownLoadedTable.GAME_PACKAGE, appBean.packageName);
        contentValues.put(DownLoadedTable.GAME_NAME, appBean.name);

        if (appBean != null && !TextUtils.isEmpty(appBean.largeIcon)) {
            contentValues.put(DownLoadedTable.GAME_ICON_URL, appBean.largeIcon);
        } else {
            contentValues.put(DownLoadedTable.GAME_ICON_URL, appBean.iconUrl);

        }
        contentValues.put(DownLoadedTable.GAME_VERSION_CODE, appBean.versionCode);
        contentValues.put(DownLoadedTable.GAME_APK_SIZE, appBean.apkSize);
        contentValues.put(DownLoadedTable.GAME_APK_URL, appBean.downloadUrl);
        contentValues.put(DownLoadedTable.GAME_APK_VERSIONNAME, appBean.versionName);
        contentValues.put(DownLoadedTable.APK_DOWNLOADED_STAMP, System.currentTimeMillis() + "");
        contentValues.put(DownLoadedTable.APK_INSTALL_TYPE, appBean.installType);
        contentValues.put(DownLoadedTable.APK_PAGEINFO, appBean.pageInfo);
        return contentValues;
    }


    /**
     * 根据pkgname删除一个数据
     */
    public int deleteSingle(String pkgName) {
        if (TextUtils.isEmpty(pkgName)) return 0;
        return PrizeDatabaseHelper.deleteCollection(
                DownLoadedTable.TABLE_NAME_D0WNLOADED,
                DownLoadedTable.GAME_PACKAGE + "=?", new String[]{pkgName});
    }

    /**
     * 方法描述：清空列表
     */
    public void deleteAllDownloadedData() {
        PrizeDatabaseHelper.deleteAllData(DownLoadedTable.TABLE_NAME_D0WNLOADED);
    }

    /**
     * 方法描述：获取下载成功的apk
     *
     * @return ArrayList<AppsItemBean>
     */
    public ArrayList<AppsItemBean> getHasDownloadedAppList() {
        ArrayList<AppsItemBean> list = new ArrayList<AppsItemBean>();
        Cursor cursor = PrizeDatabaseHelper.query(
                DownLoadedTable.TABLE_NAME_D0WNLOADED, DownLoadedTable.DOWNLOADED_COLUMNS,
                null, null, null, null, null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                AppsItemBean loadGame = new AppsItemBean();
                loadGame.id = cursor.getString(cursor
                        .getColumnIndex(DownLoadedTable.GAME_APPID));
                loadGame.packageName = cursor.getString(cursor
                        .getColumnIndex(DownLoadedTable.GAME_PACKAGE));
                loadGame.name = cursor.getString(cursor
                        .getColumnIndex(DownLoadedTable.GAME_NAME));
                loadGame.iconUrl = cursor.getString(cursor
                        .getColumnIndex(DownLoadedTable.GAME_ICON_URL));
                loadGame.versionCode = cursor.getInt(cursor
                        .getColumnIndex(DownLoadedTable.GAME_VERSION_CODE));
                loadGame.apkSize = cursor.getLong(cursor
                        .getColumnIndex(DownLoadedTable.GAME_APK_SIZE)) + "";
                loadGame.downloadUrl = cursor.getString(cursor
                        .getColumnIndex(DownLoadedTable.GAME_APK_URL));
                loadGame.versionName = cursor.getString(cursor
                        .getColumnIndex(DownLoadedTable.GAME_APK_VERSIONNAME));
                loadGame.dowloadedStamp = cursor.getString(cursor
                        .getColumnIndex(DownLoadedTable.APK_DOWNLOADED_STAMP));
                loadGame.installType = cursor.getString(cursor
                        .getColumnIndex(DownLoadedTable.APK_INSTALL_TYPE));
                list.add(loadGame);

            }

            cursor.close();
        }
        if (JLog.isDebug) {
            JLog.i("DownLoadedDAO", "list=" + list);
            if (list != null) {
                JLog.i("DownLoadedDAO", "list.size()=" + list.size());
            }
        }
        Collections.reverse(list);
        return list;
    }

    /**
     * 方法描述：获取下载成功的apk
     *
     * @return ArrayList<AppsItemBean>
     */
    public String getDownloadedAppPageInfo(String packageName) {
        Cursor cursor = PrizeDatabaseHelper.query(
                DownLoadedTable.TABLE_NAME_D0WNLOADED, new String[]{DownLoadedTable.APK_PAGEINFO},
                DownLoadedTable.GAME_PACKAGE + "=?", new String[]{packageName}, null, null, null);

        if (cursor != null&&cursor.getCount()>0) {
            cursor.moveToFirst();
            return cursor.getString(cursor.getColumnIndex(DownLoadedTable.APK_PAGEINFO));
        }
        return null;

    }
}