package com.prize.music.helpers.utils;

import java.io.File;
import java.io.RandomAccessFile;

import android.os.Environment;
import android.text.TextUtils;

/**
 * log工具类。发布上线 消除打印log、日志
 * 
 * @author Administrator
 *
 */
public class LogUtils {
	public static boolean isDebug = false;

	public static void v(String tag, String msg, Throwable t) {
		if (isDebug && !TextUtils.isEmpty(tag) && !TextUtils.isEmpty(msg))
			android.util.Log.v(tag, msg, t);
	}

	public static void d(String tag, String msg) {
		if (isDebug && !TextUtils.isEmpty(tag) && !TextUtils.isEmpty(msg))
			android.util.Log.d(tag, msg);
	}

	public static void d(String tag, String msg, Throwable t) {
		if (isDebug && !TextUtils.isEmpty(tag) && !TextUtils.isEmpty(msg))
			android.util.Log.d(tag, msg, t);
	}

	public static void i(String tag, String msg) {
		if (isDebug && !TextUtils.isEmpty(tag) && !TextUtils.isEmpty(msg))
			android.util.Log.i(tag, msg);
	}

	public static void i(String tag, String msg, Throwable t) {
		if (isDebug && !TextUtils.isEmpty(tag) && !TextUtils.isEmpty(msg))
			android.util.Log.i(tag, msg, t);
	}

	public static void w(String tag, String msg) {
		if (isDebug && !TextUtils.isEmpty(tag) && !TextUtils.isEmpty(msg))
			android.util.Log.w(tag, msg);
	}

	public static void w(String tag, String msg, Throwable t) {
		if (isDebug && !TextUtils.isEmpty(tag) && !TextUtils.isEmpty(msg))
			android.util.Log.w(tag, msg, t);
	}

	public static void e(String tag, String msg) {
		if (isDebug && !TextUtils.isEmpty(tag) && !TextUtils.isEmpty(msg))
			android.util.Log.e(tag, msg);
	}

	public static void e(String tag, String msg, Throwable t) {
		if (isDebug && !TextUtils.isEmpty(tag) && !TextUtils.isEmpty(msg))
			android.util.Log.e(tag, msg, t);
	}

	/**
	 * 写文件到sd卡上
	 * 
	 * @param content
	 */
	public static void writeFileToSD(String content) {
		// 使用RandomAccessFile 写文件 还是蛮好用的..推荐给大家使用...
		if (!isDebug)
			return;

		String sdStatus = Environment.getExternalStorageState();
		if (!sdStatus.equals(Environment.MEDIA_MOUNTED)) {
			return;
		}
		try {
			String pathName = "/mnt/sdcard/prizelog/";
			// JLog.i("11111",
			// Environment.getExternalStorageDirectory().getPath());
			String fileName = "prizelog.txt";
			File path = new File(pathName);
			File file = new File(pathName + fileName);
			if (!path.exists()) {
				path.mkdir();
			}
			if (!file.exists()) {
				file.createNewFile();
			}
			RandomAccessFile raf = new RandomAccessFile(file, "rw");
			raf.seek(file.length());
			raf.write(content.getBytes());
			raf.close();
		} catch (Exception e) {
		}
	}
}
