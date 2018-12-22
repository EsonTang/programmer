package com.prize.app.database.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.prize.app.database.CoverTable;
import com.prize.app.database.PrizeMainDBHelper;
import com.prize.app.net.datasource.base.CoverItemBean;
import com.prize.app.threads.SQLSingleThreadExcutor;
import com.prize.app.util.JLog;

import java.util.ArrayList;
import java.util.List;

/**
 * *
 * 操作封面数据库
 *
 * @author zhouerlong
 * @version V1.0
 */
public class CoverDAO {
    public static CoverItemBean query() {
        Cursor cursor = PrizeMainDBHelper.query(CoverTable.TABLE_NAME_GAME,
                null, null, null, null, null, null);
        CoverItemBean record = null;
        if (cursor == null)
            return null;
        try {
            while (cursor.moveToNext()) {
                record = new CoverItemBean();
                record.id = cursor.getInt(cursor
                        .getColumnIndex(CoverTable.COVERID));

                record.associateId = cursor.getString(cursor
                        .getColumnIndex(CoverTable.ASSOCIATEID));
                record.adType = cursor.getString(cursor
                        .getColumnIndex(CoverTable.ADTYPE));
                record.title = cursor.getString(cursor
                        .getColumnIndex(CoverTable.TITLE));
                record.imageUrl = cursor.getString(cursor
                        .getColumnIndex(CoverTable.IMAGEURL));
                record.url = cursor.getString(cursor
                        .getColumnIndex(CoverTable.URL));
                record.description = cursor.getString(cursor
                        .getColumnIndex(CoverTable.DESCRIPTION));
                record.createTime = cursor.getString(cursor
                        .getColumnIndex(CoverTable.CREATETIME));
                record.status = cursor.getInt(cursor
                        .getColumnIndex(CoverTable.STATUS));
                record.position = cursor.getInt(cursor
                        .getColumnIndex(CoverTable.POSITION));
                record.seconds = cursor.getInt(cursor
                        .getColumnIndex(CoverTable.SECONDS));
                record.startTime = cursor.getString(cursor
                        .getColumnIndex(CoverTable.STARTTIME));
                record.endTime = cursor.getString(cursor
                        .getColumnIndex(CoverTable.ENDTIME));
            }
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return record;
    }

    /**
     * 查询所有
     *
     * @return ArrayList<CoverItemBean>
     */
    public static ArrayList<CoverItemBean> queryAll() {

        ArrayList<CoverItemBean> list = new ArrayList<CoverItemBean>();
        Cursor cursor = PrizeMainDBHelper.query(CoverTable.TABLE_NAME_GAME,
                null, null, null, null, null, null);
        CoverItemBean record = null;
        try {
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    record = new CoverItemBean();
                    record.id = cursor.getInt(cursor
                            .getColumnIndex(CoverTable.COVERID));
                    record.associateId = cursor.getString(cursor
                            .getColumnIndex(CoverTable.ASSOCIATEID));
                    record.adType = cursor.getString(cursor
                            .getColumnIndex(CoverTable.ADTYPE));
                    record.title = cursor.getString(cursor
                            .getColumnIndex(CoverTable.TITLE));
                    record.imageUrl = cursor.getString(cursor
                            .getColumnIndex(CoverTable.IMAGEURL));
                    record.url = cursor.getString(cursor
                            .getColumnIndex(CoverTable.URL));
                    record.description = cursor.getString(cursor
                            .getColumnIndex(CoverTable.DESCRIPTION));
                    record.createTime = cursor.getString(cursor
                            .getColumnIndex(CoverTable.CREATETIME));
                    record.status = cursor.getInt(cursor
                            .getColumnIndex(CoverTable.STATUS));
                    record.position = cursor.getInt(cursor
                            .getColumnIndex(CoverTable.POSITION));
                    record.seconds = cursor.getInt(cursor
                            .getColumnIndex(CoverTable.SECONDS));
                    record.startTime = cursor.getString(cursor
                            .getColumnIndex(CoverTable.STARTTIME));
                    record.endTime = cursor.getString(cursor
                            .getColumnIndex(CoverTable.ENDTIME));
                    list.add(record);
                }
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public static boolean isupdate(ContentValues cv) {
        Cursor cursor = null;
        String coverId = cv.getAsString(CoverTable.COVERID);
        String columns[] = new String[]{CoverTable.COVERID,
                CoverTable.IMAGEURL};
        String where = CoverTable.COVERID + "=?";
        String[] whereClause = new String[]{coverId};
        try {
            cursor = PrizeMainDBHelper.query(CoverTable.TABLE_NAME_GAME,
                    columns, where, whereClause, null, null, null);
            if (JLog.isDebug && cursor != null) {
                JLog.i("0000", "cursor.getCount()=" + cursor.getCount());
            }
            if (cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    int id = cursor.getInt(0);
//					String imgUrl = cursor.getString(0);
                    JLog.i("0000", "isupdate--coverId=" + coverId + "----id=" + id);
                    if (String.valueOf(id).equals(coverId)) {
                        cursor.close();
                        return false;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (cursor != null) {
            cursor.close();
        }
        return true;

    }

    public static void updateCoverDb(final List<ContentValues> values) {
        SQLSingleThreadExcutor.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    for (ContentValues v : values) {
                        updateCoverTable(v, PrizeMainDBHelper.getDatabase());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 删除数据库过期的数据
     *
     * @param coverItemBean CoverItemBean
     */
    public static void deleOverdueData(final CoverItemBean coverItemBean) {
        SQLSingleThreadExcutor.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    deleOverdueData(coverItemBean, PrizeMainDBHelper.getDatabase());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /***
     * 插入或更新已安装应用表<br>
     * 只针对 CoverTable表操作
     *
     * @param cv  ContentValues
     * @return 0: fail, else: success
     */
    private static long updateCoverTable(ContentValues cv, SQLiteDatabase db) {
        Cursor cursor=null;
        try {

            String coverId = cv.getAsString(CoverTable.COVERID);
            String selection = CoverTable.COVERID + "=?";
            String selectionArgs[] = new String[]{coverId};
            cursor = db.query(CoverTable.TABLE_NAME_GAME, null, selection, selectionArgs, null, null, null);
            if (cursor == null || cursor.getCount() < 1) {
                if (cursor != null) {
                    cursor.close();
                }
                return db.insert(CoverTable.TABLE_NAME_GAME, null, cv);
            } else if (cursor.getCount() > 0) {
                return db.update(CoverTable.TABLE_NAME_GAME, cv, selection, selectionArgs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return 0;
    }

    /**
     * 删除过期数据
     * 只针对 CoverTable表操作
     *
     * @param coverItemBean CoverItemBean
     * @return 0: fail, else: success
     */
    private static long deleOverdueData(CoverItemBean coverItemBean, SQLiteDatabase db) {
        JLog.i("0000", "deleOverdueData--coverItemBean.id=" + coverItemBean.id);
        try {

            String coverId = coverItemBean.id + "";
            String selection = CoverTable.COVERID + "=?";
            String selectionArgs[] = new String[]{coverId};
            return db.delete(CoverTable.TABLE_NAME_GAME, selection, selectionArgs);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 开始事务
     */
    public void beginTransaction() {
        PrizeMainDBHelper.beginTransaction();
    }

//	/**
//	 * 结束事务
//	 */
//	public void endTransaction() {
//		PrizeMainDBHelper.endTransaction();
//	}
//
//	/**
//	 * 事务成功
//	 */
//	public void setTransactionSuccessful() {
//		PrizeMainDBHelper.setTransactionSuccessful();
//	}
}
