/*******************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：从手机数据库中读取手机缩略图
 *当前版本：V1.0
 *作	者：卞新浩
 *完成日期：2015年4月6日
 *修改记录：
 *修改日期：
 *版 本 号：
 *修 改 人：
 *修改内容：
...
 *修改记录：
 *修改日期：
 *版 本 号：
 *修 改 人：
 *修改内容：
 *********************************************/

package com.pr.scuritycenter.db.dao;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.MediaStore.Images.Thumbnails;
import android.util.Log;

public class ThumbnailsDao {

	private static ArrayList<String> list;
	public static String TAG = "Thumbnails";

	public static List<String> smallImagePath(
			ContentResolver cr) {

		String[] projection = { Thumbnails._ID, Thumbnails.IMAGE_ID,
				Thumbnails.DATA };

		Cursor cursor = cr.query(Thumbnails.EXTERNAL_CONTENT_URI, projection,
				null, null, null);
		list = new ArrayList<String>();
		if (cursor.moveToFirst()) {
			String image_path;
			int dataColumn = cursor.getColumnIndex(Thumbnails.DATA);
			do {
				image_path = cursor.getString(dataColumn);
				Log.i(TAG, image_path + "---");
				
				list.add(image_path);

			} while (cursor.moveToNext());

		}
		return list;

	}

}
