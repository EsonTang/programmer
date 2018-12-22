package com.prize.app.util;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.provider.MediaStore;
import android.util.Log;

import com.prize.app.BaseApplication;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


/***
 * 校验下载的APK文件用的
 * 
 * @author fanjunchen
 *
 */
public class MD5Util {
	/**
	 * 默认的密码字符串组合，用来将字节转换成 16 进制表示的字符,apache校验下载的文件的正确性用的就是默认的这个组合
	 */
	protected static char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6',
			'7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
	protected static MessageDigest messagedigest = null;
	static {
		try {
			messagedigest = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}

	public static String getFileMD5String(File file) throws IOException {
		InputStream fis;
		fis = new FileInputStream(file);
		byte[] buffer = new byte[1024];
		int numRead = 0;
		while ((numRead = fis.read(buffer)) > 0) {
			messagedigest.update(buffer, 0, numRead);
		}
		fis.close();
		return bufferToHex(messagedigest.digest());
	}

	private static String bufferToHex(byte bytes[]) {
		return bufferToHex(bytes, 0, bytes.length);
	}

	private static String bufferToHex(byte bytes[], int m, int n) {
		StringBuffer stringbuffer = new StringBuffer(2 * n);
		int k = m + n;
		for (int l = m; l < k; l++) {
			appendHexPair(bytes[l], stringbuffer);
		}
		return stringbuffer.toString();
	}

	private static void appendHexPair(byte bt, StringBuffer stringbuffer) {
		char c0 = hexDigits[(bt & 0xf0) >> 4];// 取字节中高 4 位的数字转换
		// 为逻辑右移，将符号位一起右移,此处未发现两种符号有何不同
		char c1 = hexDigits[bt & 0xf];// 取字节中低 4 位的数字转换
		stringbuffer.append(c0);
		stringbuffer.append(c1);
	}

	/***
	 * 包名校验下载是否正确（网络劫持）
	 * 
	 * @param filePath
	 *            apk文件路径
	 * @param packageName 包名
	 * @return boolean
	 */
	public static boolean  isDownComplete(String filePath, String packageName) {
		PackageManager pm = BaseApplication.curContext.getApplicationContext().getPackageManager();
		PackageInfo pathPackageInfo = pm.getPackageArchiveInfo(filePath,
				PackageManager.GET_ACTIVITIES);
		if (pathPackageInfo != null) {
				if (!packageName.equals(pathPackageInfo.packageName)) {
					Log.i("MD5Util","下载包名不一致,需要下载包名："+packageName+"---实际下载："+pathPackageInfo.packageName);
					File file = new File(filePath);
					if (file.exists()) {
						file.delete();
					}
					BaseApplication.curContext.getApplicationContext().getContentResolver().delete(
							MediaStore.Files.getContentUri("external"),
							"_DATA=?",
							new String[]{filePath});
					return false;

				}


		}
		return true;
	}

	public static boolean Md5Check(String filePath, String md5) {
		if (null == md5 || "".equals(md5)) {
			return true;
		}
		File f = new File(filePath);
		String nMd5 = null;
		try {
			nMd5 = MD5Util.getFileMD5String(f);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return md5.equals(nMd5);
	}
}