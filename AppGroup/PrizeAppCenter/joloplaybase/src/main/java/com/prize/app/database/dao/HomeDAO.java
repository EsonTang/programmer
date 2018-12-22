package com.prize.app.database.dao;

import android.content.ContentValues;
import android.database.Cursor;

import com.prize.app.database.HomeTable;
import com.prize.app.database.PrizeMainDBHelper;
import com.prize.app.database.beans.HomeRecord;

/**
 * *
 * 操作首页相关数据库的DAO
 *
 * @author longbaoxiu
 * @version V1.0
 */
public class HomeDAO {

    private static final String COLLUMS[] = new String[]{HomeTable.HOME_CONTENT_TYPE, HomeTable.JSON};
//    private static final String COLLUMS[] = new String[]{HomeTable.HOME_CODE,
//            HomeTable.HOME_CONTENT_TYPE, HomeTable.HOME_DESC,
//            HomeTable.HOME_IMG, HomeTable.HOME_TYPE, HomeTable.HOME_NAME,
//            HomeTable.HOME_PACKAGE, HomeTable.HOME_SIZE,
//            HomeTable.HOME_APKSIZEFORMAT, HomeTable.HOME_COUNT,
//            HomeTable.HOME_GAMEDOWNLOADURL, HomeTable.HOME_GAMEVERSIONCODE,
//            HomeTable.HOME_KEY, HomeTable.HOME_RATE, HomeTable.HOME_GIFTCOUNT,
//            HomeTable.HOME_APKMD5, HomeTable.JSON};


    /**
     * 取列表
     *
     * @return String
     */
    public String getJsonList() {
        return getRecordsJson(HomeRecord.CONTENT_TYPE_LIST);
    }



    /**
     * 根据类型取内容
     *
     * @param type 类型
     * @return String
     */
    private String getRecordsJson(int type) {
        String json = "";
        String selection = HomeTable.HOME_CONTENT_TYPE + "=?";
        String[] selectionArgs = new String[]{"" + type};
        Cursor cursor = PrizeMainDBHelper.query(HomeTable.TABLE_NAME_GAME,
                COLLUMS, selection, selectionArgs, null, null, null);
        try {
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    json = cursor.getString(cursor.getColumnIndex(HomeTable.JSON));
                }
                cursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return json;
    }

    /**
     * 添加
     *
     * @param record  HomeRecord
     */
    public void insert(HomeRecord record) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(HomeTable.HOME_CONTENT_TYPE, record.content_type);
        contentValues.put(HomeTable.JSON, record.json);
        PrizeMainDBHelper.insert(HomeTable.TABLE_NAME_GAME, null,
                contentValues);

    }


    /**
     * 删除所有通知
     */
    public void deleteAllGame() {
        PrizeMainDBHelper.delete(HomeTable.TABLE_NAME_GAME,
                HomeTable.HOME_CONTENT_TYPE + "=?",
                new String[]{HomeRecord.CONTENT_TYPE_LIST + ""});
    }


    /**
     * 开始事务
     */
    public void beginTransaction() {
        PrizeMainDBHelper.beginTransaction();
    }

    /**
     * 结束事务
     */
    public void endTransaction() {
        PrizeMainDBHelper.endTransaction();
    }

    /**
     * 事务成功
     */
    public void setTransactionSuccessful() {
        PrizeMainDBHelper.setTransactionSuccessful();
    }
}
