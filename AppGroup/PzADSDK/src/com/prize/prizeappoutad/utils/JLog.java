package com.prize.prizeappoutad.utils;

import java.io.File;
import java.io.RandomAccessFile;

import android.os.Environment;
import android.util.Log;

import com.prize.prizeappoutad.constants.Constants;

/**
 * 统一的打印日志入口，便于后面维护
 * 
 */
public class JLog {
	// 日志开关，默认打开
	public static boolean isDebug = Constants.JLog;

	public static void d(String tag, String msg) {
		if (isDebug)
			Log.d(tag, msg);
	}

	public static void i(String tag, String msg) {
		if (isDebug)
			Log.i(tag, msg);
	}

	public static void w(String tag, String msg) {
		if (isDebug)
			Log.w(tag, msg);
	}

	public static void e(String tag, String msg) {
		if (isDebug)
			Log.e(tag, msg);
	}

	// /***
	// * vipLog产品发布后，也打开的LOG，慎用！<br>
	// * 用于非常重要的LOG输出
	// *
	// * @param msg
	// */
	// public static void vipLog(String msg) {
	// Log.d("prizeApp", msg);
	// }

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

	public static void writeInstallFileToSD(String content) {
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
			String fileName = "installlog.txt";
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

	public static void writeDataFileToSD(String content) {
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
			String fileName = "datallog.txt";
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

	public static void writeNetDataFileToSD(String content) {
		// 使用RandomAccessFile 写文件 还是蛮好用的..推荐给大家使用...
		if (!isDebug)
			return;

		String sdStatus = Environment.getExternalStorageState();
		if (!sdStatus.equals(Environment.MEDIA_MOUNTED)) {
			return;
		}
		try {
			String pathName = FileUtils.LOG_URL;
			// JLog.i("11111",
			// Environment.getExternalStorageDirectory().getPath());
			String fileName = "netlog.txt";
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

	// @prize added by fanjunchen 20151217
	public static void setDebug(boolean isOpen) {
		isDebug = isOpen;
	}
	// @prize end
}
