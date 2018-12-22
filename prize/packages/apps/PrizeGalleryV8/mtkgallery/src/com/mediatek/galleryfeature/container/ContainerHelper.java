package com.mediatek.galleryfeature.container;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Images.ImageColumns;

import com.mediatek.galleryframework.base.ExtItem;
import com.mediatek.galleryframework.base.MediaData;
import com.mediatek.galleryframework.util.MtkLog;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;

public class ContainerHelper {
    private static final String TAG = "MtkGallery2/ContainerHelper";
    private static final int QUERY_COUNT_LIMIT = 100;

    private static final int CONTAINER_PLAY_MAX_COUNT = 10;

    public static ArrayList<MediaData> getPlayData(Context context,
            MediaData data) {
        ArrayList<MediaData> mds = data.relateData;
        int lastRepeatCount = 0;
        int maxCount = CONTAINER_PLAY_MAX_COUNT;

        if (mds == null && data.subType == MediaData.SubType.CONSHOT) {
            mds = getConShotDatas(context, data.groupID, data.bucketId);
            lastRepeatCount = 0;
        }
        if (mds != null) {
            return getPlayData(mds, maxCount, lastRepeatCount);
        }
        return null;
    }

    private static ArrayList<MediaData> getPlayData(ArrayList<MediaData> datas,
            int maxCount, int lastRepeatCount) {
        ArrayList<MediaData> md = new ArrayList<MediaData>();
        int num;
        int playCount;
        float space;
        MediaData tmpData = null;

        if (datas == null || datas.size() == 0 || maxCount == 0) {
            MtkLog.w(TAG, "<getPlayDatas> return null");
            return null;
        }
        num = datas.size();
        if (num <= maxCount) {
            space = 1;
            playCount = num;
        } else {
            space = (float) (num - 1) / (maxCount - 1);
            playCount = maxCount;
        }
        for (int i = 0; i < playCount; i++) {
            int index = (int) (i * space);
            // MtkLog.w(TAG, "<getPlayDatas> add index:"+index);
            tmpData = datas.get(index);
            md.add(tmpData);
        }
        for (int i = 0; i < lastRepeatCount; i++) {
            md.add(tmpData);
        }

        return md;
    }

    public static ArrayList<MediaData> getConShotDatas(Context context,
            long groupId, int bucketId) {
        Cursor cursor = getConShotsCursor(context, groupId, bucketId);
        if (cursor == null) {
            return null;
        }
        ArrayList<MediaData> list = MediaData.parseImageMediaDatas(cursor, true);
        return list;
    }

    public static void deleteConshotDatas(Context context, long groupId, int bucketId) {
        String whereClause = Images.Media.GROUP_ID + " = ?" + " AND " + Images.Media.TITLE
                + " LIKE 'IMG%CS'" + " AND " + ImageColumns.BUCKET_ID + "= ?";
        String[] whereClauseArgs = new String[] { String.valueOf(groupId),
                String.valueOf(bucketId) };

        Uri baseUri = Images.Media.EXTERNAL_CONTENT_URI;
        Uri uri = baseUri.buildUpon().appendQueryParameter("limit", 0 + "," + QUERY_COUNT_LIMIT)
                .build();
        ContentResolver resolver = context.getContentResolver();
        resolver.delete(baseUri, whereClause, whereClauseArgs);
    }

    public static Cursor getConShotsCursor(Context context,
            long groupId, int bucketId) {
        ContentResolver resolver = context.getContentResolver();
        String whereClause = Images.Media.GROUP_ID + " = ?" + " AND "
                + Images.Media.TITLE + " LIKE 'IMG%CS'" + " AND "
                + ImageColumns.BUCKET_ID + "= ?";
        String[] whereClauseArgs = new String[] { String.valueOf(groupId),
                String.valueOf(bucketId) };

        String orderClause = ImageColumns.GROUP_INDEX + " ASC";
        Uri baseUri = Images.Media.EXTERNAL_CONTENT_URI;
        Uri uri = baseUri.buildUpon().appendQueryParameter("limit",
                0 + "," + QUERY_COUNT_LIMIT).build();
        Cursor curosr = MediaData.queryImage(context, uri,
                whereClause, whereClauseArgs, orderClause);
        return curosr;
    }

    private static class FileComparator implements Comparator<File> {
        @Override
        public int compare(File file1, File file2) {
            return file1.getName().compareTo(file2.getName());
        }
    }

    static ArrayList<ExtItem> getExtItem(ArrayList<MediaData> datas) {
        ArrayList<ExtItem> items = new ArrayList<ExtItem>();
        for (MediaData data : datas) {
            ExtItem item = new ExtItem(data);
            items.add(item);
        }
        return items;
    }

    public static void setBestShotMark(Context context, int bestShotMark,
            long id) {
        Uri baseUri = Images.Media.EXTERNAL_CONTENT_URI;
        ContentValues cv = new ContentValues(1);
        cv.put(Images.Media.IS_BEST_SHOT, bestShotMark);
        int result = context.getContentResolver().update(baseUri, cv, "_id=?",
                new String[] { String.valueOf(id) });
        MtkLog.i(TAG, "<setIsBestShot> update isBestShot value of id[" + id
                + "] result = " + result);
    }
}