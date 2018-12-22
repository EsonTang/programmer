package com.prize.videoc.db;

import java.io.File;

import com.lidroid.xutils.DbUtils;
import com.lidroid.xutils.exception.DbException;
import com.prize.videoc.bean.PVideo;

public class RecordDao {

	public static void saveLastPlay(DbUtils db, PVideo video) {
		video.setTimeStamp(System.currentTimeMillis());
		db.configAllowTransaction(true);
		try {
			db.deleteAll(PVideo.class);
			db.save(video);
		} catch (DbException e) {
			e.printStackTrace();
		} finally {
			db.configAllowTransaction(false);
		}
	}

	public static PVideo findLastPlay(DbUtils db) {
		try {
			return db.findFirst(PVideo.class);
		} catch (DbException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void delAll(DbUtils db) {
		try {
			db.deleteAll(PVideo.class);
		} catch (DbException e) {
			e.printStackTrace();
		}
	}
	
	public static boolean verifyFileExistence(PVideo video) {
		String filePath=video.getPath();
		File file=new File(filePath);
		return file.exists();
	}
}
