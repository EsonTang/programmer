/*******************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：文件操作类（复制，解压缩等）
 *当前版本：v1.0
 *作	者：
 *完成日期：
 *修改记录：
 *修改日期：
 *版 本 号：
 *修 改 人：
 *修改内容：
 *********************************************/
package com.freeme.operationManual.folder;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;

import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipFile;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

/**
 **
 * 文件操作类（复制，解压缩等）
 * 
 * @author longbaoxiu
 * @version V1.0
 */
public class Folder {
	// private static final int BUFFER_SIZE = 1048576;
	public static final String DEF_VALUE = "nomanual";
	public static final String KEY = "ZIP_FILE";
	/** 包名 * */
	public static String PACK_NAME = "";
	public static final String ROOT_CHINESE = "chinese";
	private String COMPRESS_POSTFIX = ".zip";
	private String mAssetZipFileName = null;
	private final Context mContext;
	/**
	 * 是否需要覆盖原有的assets下资源
	 */
	private final boolean isNeedRefresh = true;

	public Folder(Context paramContext) {
		this.mContext = paramContext;
		PACK_NAME = this.mContext.getPackageName();
	}

	// private boolean checkFileExit(String paramString) {
	// return new File(paramString).exists();
	// }

	/**
	 * 复制压缩文件到制定目录
	 * 
	 * @param paramString1
	 * @param paramString2
	 * @throws IOException
	 * @return void
	 * @see
	 */
	private void copyZip(String paramString1, String paramString2)
			throws IOException {
		if (!new File(paramString1 + paramString2).exists())
			new File(paramString1).mkdirs();
		FileOutputStream localFileOutputStream = new FileOutputStream(
				paramString1 + this.mAssetZipFileName);
		InputStream localInputStream = this.mContext.getAssets().open(
				paramString2);
		try {
			byte[] arrayOfByte = new byte[1048576];
			while (true) {
				int i = localInputStream.read(arrayOfByte, 0, 1048576);
				if (i == -1)
					break;
				localFileOutputStream.write(arrayOfByte, 0, i);
			}
		} finally {
			localInputStream.close();
			localFileOutputStream.close();
		}
		localInputStream.close();
		localFileOutputStream.close();
	}

	private void deleteCopiedZipFile(String paramString) {
		File localFile = new File(paramString);
		if (localFile.exists())
			localFile.delete();
	}

	/**
	 * 获取assets目录下的文件夹名称
	 * 
	 * @return String assets目录下的文件夹名称
	 * @see
	 */
	private String getAssetFileName() {
		AssetManager localAssetManager = this.mContext.getAssets();
		try {
			this.COMPRESS_POSTFIX = (Build.MODEL.trim() + this.COMPRESS_POSTFIX);
			String[] arrayOfString = localAssetManager.list("");
			for (int i = 0;; i++) {
				int j = arrayOfString.length;
				String str = null;
				
				
				
				if (i < j) {
					if (!arrayOfString[i].contains(this.COMPRESS_POSTFIX))
						continue;
					str = arrayOfString[i];
				}
				return str;
			}
		} catch (IOException localIOException) {
			localIOException.printStackTrace();
		}
		return null;
	}

	/**
	 * 获取.zip文件的名称（除去后缀.zip）
	 * 
	 * @return String （除去后缀.zip文件的名称）
	 */
	private String getFolderName() {
		String str1 = this.mAssetZipFileName;
		boolean bool = str1.contains(".zip");
		String str2 = null;
		if (bool)
			str2 = str1.split(".zip")[0];
		return str2;
	}

	/**
	 * 获取文件夹路径（/data/data/包名/fileName）
	 * 
	 * @return String 文件夹路径
	 * @see
	 */
	private String getFolderNamePath() {
		StringBuffer localStringBuffer = new StringBuffer();
		localStringBuffer.append(getPackagePath()).append(getFolderName())
				.append(File.separator);
		return localStringBuffer.toString();
	}

	/**
	 * 获取app所在路径
	 * 
	 * @return app所在路径
	 * @see
	 */
	public static String getPackagePath() {
		String str = Environment.getDataDirectory().getAbsolutePath();
		StringBuffer localStringBuffer = new StringBuffer();
		localStringBuffer.append(str).append("/data/").append(PACK_NAME)
				.append(File.separator);
		return localStringBuffer.toString();
	}

	// private String getSharedPreferences() {
	// return PreferenceManager.getDefaultSharedPreferences(this.mContext)
	// .getString("ZIP_FILE", "nomanual");
	// }

	/**
	 * 复制解压assets目录下的文件
	 * 
	 * @param paramString1
	 *            app所在路径
	 * @param paramString2
	 *            assets目录下的压缩文件名称
	 * @throws IOException
	 * @return boolean
	 */
	private boolean moveAndUnZip(String paramString1, String paramString2)
			throws IOException {
		copyZip(paramString1, paramString2);
		return unZip(paramString1, paramString1 + this.mAssetZipFileName);
	}

	private boolean putSharedPreferences(String paramString) {
		SharedPreferences.Editor localEditor = PreferenceManager
				.getDefaultSharedPreferences(this.mContext).edit();
		localEditor.putString("ZIP_FILE", paramString);
		return localEditor.commit();
	}

	/**
	 * 
	 * 解压assets目录下的压缩文件
	 * 
	 * @param paramString
	 *            assets目录下的压缩文件名称
	 * @return boolean
	 * @see
	 */
	private boolean unZip(String paramString) {
		try {
			boolean bool = moveAndUnZip(getPackagePath(), paramString);
			return bool;
		} catch (IOException localIOException) {
			localIOException.printStackTrace();
		}
		return false;
	}

	/**
	 * 复制解压assets目录下的文件
	 * 
	 * @param paramString1
	 *            app所在路径
	 * @param paramString2
	 *            app所在路径+ assets目录下的压缩文件名称
	 * @throws IOException
	 * @return boolean
	 */
	private boolean unZip(String paramString1, String paramString2)
			throws IOException {
		ZipFile localZipFile = new ZipFile(paramString2, "GBK");
		Enumeration localEnumeration = localZipFile.getEntries();
		while (localEnumeration.hasMoreElements()) {
			ZipEntry localZipEntry = (ZipEntry) localEnumeration.nextElement();
			String str1 = localZipEntry.getName();
			String str2 = paramString1 + "/" + str1;
			if (localZipEntry.isDirectory()) {
				File localFile1 = new File(str2);
				if (localFile1.exists())
					continue;
				localFile1.mkdirs();
				continue;
			}
			File localFile2 = new File(str2.substring(0, str2.lastIndexOf("/")));
			if (!localFile2.exists())
				localFile2.mkdirs();
			new File(paramString1 + "/" + str1);
			BufferedOutputStream localBufferedOutputStream = new BufferedOutputStream(
					new FileOutputStream(paramString1 + "/" + str1));
			BufferedInputStream localBufferedInputStream = new BufferedInputStream(
					localZipFile.getInputStream(localZipEntry));
			byte[] arrayOfByte = new byte[1024];
			for (int i = localBufferedInputStream.read(arrayOfByte); i != -1; i = localBufferedInputStream
					.read(arrayOfByte))
				localBufferedOutputStream.write(arrayOfByte, 0, i);
			localBufferedOutputStream.close();
		}
		localZipFile.close();
		putSharedPreferences(getFolderName());
		deleteCopiedZipFile(getPackagePath() + this.mAssetZipFileName);
		return true;
	}

	/**
	 * 获取文件夹路径（/data/data/包名/chinese/）
	 * 
	 * @return String
	 * @see
	 */
	public String getFilePath() {
		return getFolderNamePath() + "chinese" + File.separator;
	}

	/**
	 * 解压.zip文件
	 * 
	 * @return void
	 * @see
	 */
	public void unZip() {
		mAssetZipFileName = getAssetFileName();
		if (mAssetZipFileName == null) {
			return;
		}
		String folderPath = getPackagePath() + getFolderName();
		File file = new File(folderPath);
		if (isNeedRefresh) {
			if (file != null && file.exists()) {
				String versinRealse = getSystemVersionRealse();
				String currentRealse = android.os.Build.DISPLAY;
				if (versinRealse == null || !versinRealse.equals(currentRealse)) {
					delete(file);
					saveSystemVersionRealse(currentRealse);
				}
			}
		}

		if ((!file.exists()) && (!file.isDirectory())) {
			unZip(mAssetZipFileName);
		}

	}

	/**
	 * 获取系统版本（SharedPreferences保存）
	 * 
	 * @return
	 * @return String
	 * @see
	 */
	protected String getSystemVersionRealse() {
		return PreferenceManager.getDefaultSharedPreferences(this.mContext)
				.getString("VERSION_DISPLAY", null);

	}

	protected void saveSystemVersionRealse(String versionRealse) {
		// String versionRealse = android.os.Build.VERSION.RELEASE;
		SharedPreferences.Editor localEditor = PreferenceManager
				.getDefaultSharedPreferences(this.mContext).edit();
		localEditor.putString("VERSION_DISPLAY", versionRealse);
		localEditor.commit();
	}

	/**
	 * 递归删除文件夹
	 * 
	 * @param file
	 *            文件
	 * @return void
	 * @see
	 */
	public static void delete(File file) {
		if (file.isFile()) {
			file.delete();
			return;
		}

		if (file.isDirectory()) {
			File[] childFiles = file.listFiles();
			if (childFiles == null || childFiles.length == 0) {
				file.delete();
				return;
			}
			for (int i = 0; i < childFiles.length; i++) {
				delete(childFiles[i]);
			}
			file.delete();
		}
	}
}