/*******************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：操作搜索记录数据库
 *当前版本：
 *作	者：
 *完成日期：
 *修改记录：
 *修改日期：
 *版 本 号：
 *修 改 人：
 *修改内容：
 *********************************************/
package com.prize.app.database.dao;

import android.content.ContentValues;
import android.database.Cursor;

import com.prize.app.database.InstalledAppTable;
import com.prize.app.database.PrizeMainDBHelper;
import com.prize.app.database.SearchHistory;
import com.prize.app.util.CommonUtils;

import java.util.ArrayList;

/**
 * *
 * 操作搜索记录数据库
 *
 * @author longbaoxiu
 * @version V1.0
 */
public class SearchHistoryDao {
    private static final String COLLUMS[] = new String[]{
            SearchHistory.HISTORY_TITLE, SearchHistory.TIMESTAMP};

    /**
     * 获取搜索记录
     *
     * @return ArrayList<String>
     */
    public static ArrayList<String> getSearchHistoryList() {
        ArrayList<String> gameList = new ArrayList<String>();
        Cursor cursor = PrizeMainDBHelper.query(SearchHistory.TABLE_NAME,
                COLLUMS, null, null, null, null, SearchHistory.TIMESTAMP
                        + " desc");
        if (cursor == null) {
            return gameList;
        }

        try {
            while (cursor.moveToNext()) {
                String title = cursor.getString(cursor
                        .getColumnIndex(SearchHistory.HISTORY_TITLE));
                gameList.add(title);
            }
            cursor.close();
        } catch (Exception e) {
            cursor.close();
        }
        return gameList;
    }

    /**
     * 获取搜索记录和输入的字符串匹配
     *
     * @return ArrayList<String>
     */
    public static ArrayList<String> getSearchHistoryMatchList(String enterKey) {
        ArrayList<String> gameList = new ArrayList<String>();
        Cursor cursor = PrizeMainDBHelper.query(SearchHistory.TABLE_NAME,
                COLLUMS, null, null, null, null, SearchHistory.TIMESTAMP
                        + " desc");
        if (cursor == null) {
            return gameList;
        }

        try {
            while (cursor.moveToNext()) {
                String title = cursor.getString(cursor
                        .getColumnIndex(SearchHistory.HISTORY_TITLE));
                title = CommonUtils.getMaxLenStr(title);
                if (title.contains(enterKey)) {
                    gameList.add(title);
                }
            }
            cursor.close();
        } catch (Exception e) {
            cursor.close();
        }
        return gameList;
    }

    /**
     * 添加搜索记录
     *
     * @param title       搜索关键字
     * @param currentTime 当前时间
     */
    public static void insert(String title, long currentTime) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(SearchHistory.HISTORY_TITLE, title);
        contentValues.put(SearchHistory.TIMESTAMP, currentTime);
        delRepeat(title);
        PrizeMainDBHelper.insert(SearchHistory.TABLE_NAME, null,
                contentValues);

    }

    /**
     * 先查询，后删除
     *
     * @param title 搜索关键字
     */
    private static void delRepeat(String title) {
        Cursor cursor = null;
        try {
            cursor = PrizeMainDBHelper.query(SearchHistory.TABLE_NAME,
                    null, SearchHistory.HISTORY_TITLE + "=?", new String[]{title}, null, null, null);
            if (cursor == null || cursor.getCount() <= 0) {
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        PrizeMainDBHelper.delete(SearchHistory.TABLE_NAME,
                SearchHistory.HISTORY_TITLE + "=?", new String[]{title});
    }

    /**
     * 清空表数据
     */
    public static void cleardata() {
        StringBuilder order = new StringBuilder("delete from ");
        order.append(SearchHistory.TABLE_NAME);
        PrizeMainDBHelper.executeSQL(order.toString(), null);
    }
}
