package com.prize.app.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.StatFs;
import android.provider.Settings;
import android.text.TextUtils;

import com.prize.app.BaseApplication;
import com.prize.app.constants.Constants;
import com.prize.app.download.DownloadHelper;
import com.prize.onlinemusibean.SongDetailInfo;

public class FileUtils {
	/*** SD卡根目录 */
	public final static String CFG_PATH_SDCARD = Environment
			.getExternalStorageDirectory().getAbsolutePath();
	/*** 应用中心的 SD卡根目录 */
	public final static String CFG_PATH_SDCARD_DIR = CFG_PATH_SDCARD
			+ File.separator + "prizeAppCenter";
	/*** SD卡上image目录 */
	public final static String CFG_PATH_SDCARD_PRIZE_IMAGE = CFG_PATH_SDCARD_DIR
			+ File.separator + "image";
	/*** SD卡上download目录 */
	public final static String CFG_PATH_SDCARD_DOWNLOAD_DIR = Constants.SONG_SAVE_PATH;
	/*** SD卡上database目录 */
	public final static String CFG_PATH_SDCARD_DATABASE_DIR = CFG_PATH_SDCARD_DIR
			+ File.separator + "database";

	private static String FIXED_PATH = CFG_PATH_SDCARD + File.separator
			+ ".android/data/";

	/** crash文件 */
	public final static String CFG_APP_CRASH_FILE = CFG_PATH_SDCARD_DIR
			+ File.separator + "appcrash.log";

	/**
	 * 创建app文件路径
	 * 
	 * @return void
	 */
	public static void initAppPath() {
		File appPath = new File(CFG_PATH_SDCARD_DIR);
		if (!appPath.exists()) {
			appPath.mkdir();
		}
	}

	/**
	 * 获取图片缓存目录
	 * 
	 * @return
	 */
	public static String getImageCachePath() {
		String cachePath;
		if (IsCanUseSdCard()) {
			File file = new File(CFG_PATH_SDCARD_PRIZE_IMAGE);
			if (!file.exists()) {
				file.mkdirs();
			}
			cachePath = file.getPath();
		} else {
			cachePath = BaseApplication.curContext.getCacheDir().getPath()
					+ File.separator + "image";
		}
		return cachePath;
	}

	/**
	 * 
	 * 返回以M为单位的
	 * 
	 * @param file
	 * @return
	 * @return String
	 * @see
	 */
	public static double getDirSize(File file) {
		// 判断文件是否存在
		if (file.exists()) {
			// 如果是目录则递归计算其内容的总大小
			if (file.isDirectory()) {
				File[] children = file.listFiles();
				double size = 0;
				for (File f : children)
					size += getDirSize(f);
				return size;
			} else {// 如果是文件则直接返回其大小,以“兆”为单位
				double size = (double) file.length() / 1024 / 1024;

				return size;
			}
		} else {
			return 0.0;
		}
	}

	// /**
	// * 获取统计缓存目录
	// */
	// public static String getStcCachePath() {
	// String cachePath;
	// if (IsCanUseSdCard()) {
	// File file = new File(CFG_PATH_SDCARD_JOLOPLAY_STC);
	// if (!file.exists()) {
	// file.mkdirs();
	// }
	// cachePath = file.getPath();
	// } else {
	// cachePath = BaseApplication.curContext.getCacheDir().getPath()
	// + File.separator + "statistics";
	// }
	// return cachePath;
	// }

	/**
	 * 将文件保存到Data目录
	 * 
	 * @param context
	 * @param inStream
	 * @param fileName
	 * @return
	 */
	public static boolean saveToData(InputStream inStream, String fileName) {
		FileOutputStream fos = null;
		try {
			fos = BaseApplication.curContext.openFileOutput(fileName,
					Context.MODE_PRIVATE);
			byte[] buffer = new byte[1024];
			int len = 0;
			while ((len = inStream.read(buffer)) != -1) {
				fos.write(buffer, 0, len);
			}
			return true;
		} catch (Exception e) {
		} finally {
			try {
				fos.close();
			} catch (Exception e) {
			}
		}
		return false;
	}

	/**
	 * 将obj 存储到data目录
	 * 
	 * @param obj
	 * @param fileName
	 */
	public static void saveObjToData(Object obj, String fileName) {
		ObjectOutputStream oos = null;
		try {
			FileOutputStream fis = BaseApplication.curContext.openFileOutput(
					fileName, Context.MODE_PRIVATE);
			oos = new ObjectOutputStream(fis);
			oos.writeObject(obj);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				oos.close();
			} catch (Exception e) {
			}
		}
	}

	/**
	 * data 目录下去输入流 <BR/>
	 * 调用者需要关闭流
	 * 
	 * @param fileName
	 * @return
	 */
	public static FileInputStream getFromData(String fileName) {
		FileInputStream is = null;
		try {
			is = BaseApplication.curContext.openFileInput(fileName);
		} catch (IOException e) {
		}
		return is;
	}

	/**
	 * 将Data目录下的图片取出
	 * 
	 * @param context
	 * @param fileName
	 * @return
	 */
	public static Bitmap getBitmapFromData(String fileName) {
		Bitmap bitmap = null;
		FileInputStream fis = null;
		try {
			fis = getFromData(fileName);
			bitmap = BitmapFactory.decodeStream(fis);
		} finally {
			try {
				fis.close();
			} catch (Exception e) {
			}
		}
		return bitmap;
	}

	/**
	 * 判断sd卡是否有用
	 */
	public static boolean IsCanUseSdCard() {
		try {
			return Environment.getExternalStorageState().equals(
					Environment.MEDIA_MOUNTED);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * 获取下载是临时文件
	 * 
	 * @param gameCode
	 * @return
	 */
	public static String getDownloadTmpFilePath(String gameCode) {
		return CFG_PATH_SDCARD_DOWNLOAD_DIR + File.separator + gameCode;
	}
	/**
	 * 
	 * 获取本地歌单收藏的本地图片路径
	 * @param gameCode
	 * @return String 
	 */
	public static String getCollectImgPath(String gameCode) {
		return Constants.SORT_COLLECT_LOGO_SAVE_PATH + File.separator + gameCode;
	}

	/**
	 * 获取本地已下载的歌曲mp3文件
	 * 
	 * @param 文件名称
	 * @return
	 */
	public static String getDownMusicFilePath(String gameCode) {
		return CFG_PATH_SDCARD_DOWNLOAD_DIR + File.separator + gameCode;
	}

	/**
	 * 
	 * 根据包名获取应用本省下载升级路径
	 * 
	 * @param packageName
	 * @return String
	 */
	public static String getAppAPKFilePath(String packageName) {
		return CFG_PATH_SDCARD_DOWNLOAD_DIR + File.separator + packageName
				+ Constants.ANDROID_APP_SUFFIX;
	}

	//
	// /**
	// *
	// * 根据包名获取应用本省下载升级路径
	// *
	// * @param packageName
	// * @return String
	// */
	// public static String getAppAPKFileTempPath(String packageName) {
	// return CFG_PATH_SDCARD_DOWNLOAD_DIR + File.separator + packageName
	// + Constants.PRIZE_TEM_FILE_SUFFIX;
	// }

	/**
	 * 获取SD卡有效空间
	 * 
	 * @return
	 */
	public static double getSDAvailaleSize() {
		StatFs stat = new StatFs(CFG_PATH_SDCARD);
		long blockSize = stat.getBlockSize();
		long availableBlocks = stat.getAvailableBlocks();
		return blockSize * availableBlocks;// 额外预留1M
	}

	// Bitmap保存为PNG
	public static boolean Bitmap2PNG(Bitmap bmp, String filepath) {
		if (bmp == null || filepath == null)
			return false;
		OutputStream stream = null;
		try {
			File file = new File(filepath);
			File dir = new File(file.getParent());
			if (!dir.exists())
				dir.mkdirs();
			if (file.exists())
				file.delete();

			stream = new FileOutputStream(filepath);
			if (bmp.compress(Bitmap.CompressFormat.PNG, 85, stream)) {
				stream.flush();
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			try {
				if (stream != null)
					stream.close();
			} catch (Exception e) {
			}
		}
		return false;
	}

	// 图片文件转Bitmap
	public static Bitmap PNGToBitmap(String path) {
		File file = new File(path);
		if (!file.exists()) {
			JLog.w("FileUtils", path + " is not exits");
			return null;
		}
		Bitmap bm = null;
		BitmapFactory.Options bfoOptions = new BitmapFactory.Options();
		bfoOptions.inDither = false;
		bfoOptions.inPurgeable = true;
		bfoOptions.inInputShareable = true;
		bfoOptions.inTempStorage = new byte[32 * 1024];

		FileInputStream fs = null;
		try {
			fs = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			JLog.w("FileUtils", path + " is not exits");
		}

		try {
			if (fs != null)
				bm = BitmapFactory.decodeFileDescriptor(fs.getFD(), null,
						bfoOptions);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (fs != null) {
				try {
					fs.close();
				} catch (IOException e2) {
					e2.printStackTrace();
				}
			}
		}

		return bm;
	}

	private static boolean hasClearedAPK = false;

	/**
	 * 清理已下载的APK 和 图片 清理规则：下载 > 10天的APK，和超过 30天的ICON 删除，每次运行只清理一次
	 */
	public static void clearSDKData() {
		if (hasClearedAPK) {
			return;
		} else {
			// 执行清除
			hasClearedAPK = true;
		}

		new Thread(new Runnable() {

			@Override
			public void run() {
				cleanDownloadFiles();
				cleanImgs();
			}
		}).start();
	}

	private static void cleanDownloadFiles() {
		JLog.info("cleanDownloadFiles");
		File loadDir = new File(CFG_PATH_SDCARD_DOWNLOAD_DIR);
		File[] files = loadDir.listFiles();
		if (files == null) {
		} else {
			int length = files.length;
			long lastModified = 0;
			long currentTime = System.currentTimeMillis();
			long DOWNLOAD_MAX_TIME = 1 * 24 * 60 * 60 * 1000;

			for (int i = 0; i < length; i++) {
				File delFile = files[i];
				if (delFile.isDirectory()) {
					// 按逻辑没有子目录，除非用户自己添加，故不要去删除用户的数据
				} else {
					lastModified = delFile.lastModified();
					if ((currentTime - lastModified) > DOWNLOAD_MAX_TIME) {
						JLog.info("cleanDownloadFiles="
								+ delFile.getAbsolutePath());
						delFile.delete();
					}
				}
			}
		}
	}

	private static void cleanImgs() {
		File imgDir = new File(CFG_PATH_SDCARD_PRIZE_IMAGE);
		File[] imgFiles = imgDir.listFiles();
		if (null == imgFiles) {

		} else {
			int length = imgFiles.length;
			long lastModified = 0;
			long currentTime = System.currentTimeMillis();
			long DOWNLOAD_MAX_TIME = 22 * 24 * 60 * 60 * 1000;// 22 天
																// ，32位机器上不会溢出，否则会变负数

			for (int i = 0; i < length; i++) {
				File delFile = imgFiles[i];
				if (delFile.isDirectory()) {
					// 按逻辑没有子目录，除非用户自己添加，故不要去删除用户的数据
				} else {
					lastModified = delFile.lastModified();
					if ((currentTime - lastModified) > DOWNLOAD_MAX_TIME) {
						JLog.info("cleanImgs" + delFile.getAbsolutePath());
						delFile.delete();
					}
				}
			}
		}
	}

	public static boolean clearImgsNow() {
		File imgDir = new File(CFG_PATH_SDCARD_PRIZE_IMAGE);
		File[] imgFiles = imgDir.listFiles();
		if (null == imgFiles) {
			return false;
		} else {
			int length = imgFiles.length;
			for (int i = 0; i < length; i++) {
				File delFile = imgFiles[i];
				if (delFile.isDirectory()) {
					// 按逻辑没有子目录，除非用户自己添加，故不要去删除用户的数据
				} else {
					delFile.delete();
				}
			}
			return true;
		}
	}

	/**
	 * 目标下 递归删除文件和文件夹
	 * 
	 * @param file
	 *            要删除的根目录
	 */
	public static boolean recursionDeleteFile(File file) {
		if (file.isFile()) {
			file.delete();
			return true;
		}
		if (file.isDirectory()) {
			File[] childFile = file.listFiles();
			if (childFile == null || childFile.length == 0) {
				// file.delete();
				return true;
			}
			for (File f : childFile) {
				recursionDeleteFile(f);
			}
		}
		return true;
	}

	/**
	 * 保存固定数据
	 * 
	 * @param key
	 * @param value
	 */
	public static void saveFixedInfo(final String key, final String value) {
		// 多处保存，速度会比较慢
		new Thread(new Runnable() {

			@Override
			public void run() {
				// 保存到系统数据
				Settings.System.putString(BaseApplication.curContext
						.getApplicationContext().getContentResolver(), key,
						value);
				// 保存到sdcard
				File file = new File(FIXED_PATH);
				if (!file.exists()) {
					file.mkdirs();
				} else if (file.isFile()) {
					file.delete();
					file.mkdirs();
				}
				FileOutputStream fos = null;
				try {
					String filePath = FIXED_PATH + File.separator + key;
					fos = new FileOutputStream(new File(filePath));
					fos.write(value.getBytes());
				} catch (Exception e) {
				} finally {
					try {
						if (fos != null)
							fos.close();
					} catch (Exception e) {
					}
				}
				// 保存到/data/data/
				DataStoreUtils.saveLocalInfo(key, value);
			}
		}).start();

	}

	/**
	 * 取出固定数据
	 * 
	 * @param key
	 * @return
	 */
	public static String getFixedInfo(String key) {
		// 从系统设置中取
		String value = Settings.System.getString(BaseApplication.curContext
				.getApplicationContext().getContentResolver(), key);
		// 从sdcard 取
		if (TextUtils.isEmpty(value)) {
			BufferedReader reader = null;
			try {
				String filePath = FIXED_PATH + File.separator + key;
				reader = new BufferedReader(new InputStreamReader(
						new FileInputStream(filePath)));
				value = reader.readLine();
			} catch (Exception e) {
			} finally {
				try {
					if (reader != null)
						reader.close();
				} catch (Exception e) {
				}
			}
			// 从/data/data/中取
			if (TextUtils.isEmpty(value)) {
				value = DataStoreUtils.readLocalInfo(key);
			}

			// 再次保存防止被删
			if (!TextUtils.isEmpty(value)) {
				saveFixedInfo(key, value);
			} else {
				value = null;
			}
		}

		return value;
	}

	/**
	 * 把简单的内容写入文本
	 * 
	 * @param content
	 * @param path
	 */
	public static void saveString(String content, String path) {
		if (null == content || null == path) {
			return;
		}
		FileWriter fw = null;
		try {
			fw = new FileWriter(path, true);
			fw.write(content);
			fw.flush();
		} catch (Exception e) {
		} finally {
			try {
				if (null != fw) {
					fw.close();
				}
			} catch (Exception e) {
			}
		}
	}

	public static String readString(String path) {
		if (null == path) {
			return null;
		}
		FileInputStream fileInS = null;
		InputStreamReader inRead = null;
		BufferedReader fr = null;
		String content = null;
		try {
			fileInS = new FileInputStream(new File(path));
			inRead = new InputStreamReader(fileInS);
			fr = new BufferedReader(inRead);
			String line = null;
			StringBuilder sBuilder = new StringBuilder();
			while ((line = fr.readLine()) != null) {
				sBuilder.append(line);
				sBuilder.append("\n");
			}
			content = sBuilder.toString();
		} catch (Exception e) {
		} finally {
			try {
				if (null != fileInS) {
					fileInS.close();
				}
				if (null != inRead) {
					inRead.close();
				}
				if (null != fr) {
					fr.close();
				}
			} catch (Exception e2) {
			}
		}
		return content;
	}

	/**
	 * added by fanjunchen 2015-11-19 获取下载完成的APK文件路径
	 * 
	 * @param gameCode
	 * @return
	 */
	public static String getDownloadAppFilePath(String gameCode) {
		return CFG_PATH_SDCARD_DOWNLOAD_DIR + File.separator + gameCode
				+ ".apk";
	}

	/**
	 * 安全删除文件.
	 * 
	 * @param file
	 * @return
	 */
	private void safeDeleteFile(File file) {
		if (file.isFile()) {
			deleteFileSafely(file);
			return;
		}
		if (file.isDirectory()) {
			File[] childFiles = file.listFiles();
			if (childFiles == null || childFiles.length == 0) {
				deleteFileSafely(file);
				return;
			}
			for (int i = 0; i < childFiles.length; i++) {
				safeDeleteFile(childFiles[i]);
			}
			deleteFileSafely(file);
		}
	}

	/**
	 * 安全删除文件.
	 * 
	 * @param file
	 * @return
	 */
	public static boolean deleteFileSafely(File file) {
		if (file != null) {
			String tmpPath = file.getParent() + File.separator
					+ System.currentTimeMillis();
			File tmp = new File(tmpPath);
			file.renameTo(tmp);
			return tmp.delete();
		}
		return false;
	}

	/**
	 * 将临时文件重命名
	 * 
	 * @param gameCode
	 * @return
	 * @throws IOException
	 */
	public static boolean renameTempMusic() throws IOException {
		File oldfile = new File(Constants.APKFILETEMPPATH);
		File newfile = new File(Constants.APKFILEPATH);
		if (newfile.exists()) {
			newfile.delete();
		}

		if (oldfile.exists()) {
			oldfile.renameTo(newfile);
			return true;
		} else {
			// 文件意外删除了
			return false;
		}
	}
}
