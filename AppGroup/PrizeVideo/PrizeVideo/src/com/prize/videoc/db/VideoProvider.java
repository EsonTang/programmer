package com.prize.videoc.db;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import com.prize.videoc.bean.PVideo;

/**
 * Created by yiyi on 2015/6/9.
 */
public class VideoProvider {

	public static List<PVideo> getList(Context context) {
		if (context == null)
			return null;
		String orderBy = MediaStore.Video.Media.DATE_TAKEN + " DESC, "
				+ MediaStore.Video.Media._ID + " DESC ";
		Cursor cursor = context.getContentResolver().query(
				MediaStore.Video.Media.EXTERNAL_CONTENT_URI, null, null, null,
				orderBy);
		if (cursor == null)
			return null;

		List<PVideo> list = new ArrayList<PVideo>();
		while (cursor.moveToNext()) {
			int id = cursor.getInt(cursor
					.getColumnIndexOrThrow(MediaStore.Video.Media._ID));
			String title = cursor.getString(cursor
					.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE));
			String album = cursor.getString(cursor
					.getColumnIndexOrThrow(MediaStore.Video.Media.ALBUM));
			String artist = cursor.getString(cursor
					.getColumnIndexOrThrow(MediaStore.Video.Media.ARTIST));
			String displayName = cursor
					.getString(cursor
							.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME));
			String mimeType = cursor.getString(cursor
					.getColumnIndexOrThrow(MediaStore.Video.Media.MIME_TYPE));
			String path = cursor.getString(cursor
					.getColumnIndexOrThrow(MediaStore.Video.Media.DATA));
			long duration = cursor.getInt(cursor
					.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION));
			long size = cursor.getLong(cursor
					.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE));

			PVideo video = new PVideo(id, title, album, artist, displayName,
					mimeType, path, size, duration);

			list.add(video);
		}
		cursor.close();

		return list;
	}

	public static int delVideo(Context ctx, int id) {
		Uri uri = Uri.withAppendedPath(
				MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id + "");
		return ctx.getContentResolver().delete(uri, null, null);
	}
	
	public static PVideo findVideoByName(Context context,String name) {
		if (context == null||name==null)
			return null;
		String orderBy = MediaStore.Video.Media.DATE_TAKEN + " DESC, "
				+ MediaStore.Video.Media._ID + " DESC ";
		Cursor cursor = context.getContentResolver().query(
				MediaStore.Video.Media.EXTERNAL_CONTENT_URI, null, null, null,
				orderBy);
		if (cursor == null)
			return null;
	
		PVideo video = null;
		while (cursor.moveToNext()) {
			String displayName = cursor
					.getString(cursor
							.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME));
			if (name.equals(displayName)) {
				int id = cursor.getInt(cursor
						.getColumnIndexOrThrow(MediaStore.Video.Media._ID));
				String title = cursor.getString(cursor
						.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE));
				String album = cursor.getString(cursor
						.getColumnIndexOrThrow(MediaStore.Video.Media.ALBUM));
				String artist = cursor.getString(cursor
						.getColumnIndexOrThrow(MediaStore.Video.Media.ARTIST));
				String mimeType = cursor.getString(cursor
						.getColumnIndexOrThrow(MediaStore.Video.Media.MIME_TYPE));
				String path = cursor.getString(cursor
						.getColumnIndexOrThrow(MediaStore.Video.Media.DATA));
				long duration = cursor.getInt(cursor
						.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION));
				long size = cursor.getLong(cursor
						.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE));
				video = new PVideo(id, title, album, artist, displayName,
						mimeType, path, size, duration);
				break;
			}
		}
		cursor.close();
	
		return video;
	}

}
