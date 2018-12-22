
/*******************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：文件管理
 *当前版本：V1.0
 *作	者：zhouerlong
 *完成日期：2015-8-5
 *********************************************/
package com.android.launcher3;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringBufferInputStream;
import java.io.UnsupportedEncodingException;

import android.content.Context;
import android.os.Environment;

public class FileUtils {
	static String FILENAME = "/default_workspace1.xml";

	/**
	 * 是否存在此目录
	 * @param context
	 * @param fileName
	 * @return
	 */
	public static boolean isexistsFile(Context context,String fileName) { 
		File file = new File(fileName);
			return file.exists();
	}
	/**
	 * 是否存在此目录
	 * @param context
	 * @param fileName
	 * @return
	 */
	public static boolean isexists(String fileName) { 
		File file = new File(fileName);
		return file.exists();
	}
	
	
	public static void dump(String str, File f) {
		FileWriter writer;
		str+="\n";
		try {

			writer = new FileWriter(f);
			writer.write(str);
			writer.flush();
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	/**加载wallpaper
	 * @param context
	 * @param fileName
	 * @return
	 */
	static public InputStream loadWallpaperXmlResource(Context context,
			String fileName) {

		File file = new File(Environment.getExternalStorageDirectory()
				+ fileName);
		byte[] data = null;
		FileInputStream fis = null;
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			if (!file.exists()) {
				return null;
			}
			try {
				fis = new FileInputStream(file);

				try {
					data = new byte[fis.available()];
					fis.read(data);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {

				try {
					fis.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return new ByteArrayInputStream(data);

	}
	/**加载xml文件
	 * @param fileName
	 * @return
	 */
	public static InputStream loadXmlFile(String fileName) {
		File file = new File(fileName);
		byte[] data = null;
		FileInputStream fis = null;
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			if (!file.exists()) {
				return null;
			}
			try {
				fis = new FileInputStream(file);

				try {
					data = new byte[fis.available()];
					fis.read(data);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {

				try {
					fis.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return new ByteArrayInputStream(data);

	}
	
	
	public static File getStorageFIle(String fileName) {
		return new File(Environment.getExternalStorageDirectory()
				+ fileName);
	}
	
	/***
	 * 拷贝文件
	 * @param in 输入
	 * @param out 输出
	 * @return 是否成功
	 */
	public static boolean copyFile(InputStream in, File outFile) {
		
		if (null == in || null == outFile)
			return false;
		if (outFile.exists()) {
			return true;
		}
		
		byte[] buffer = new byte[1024];
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(outFile);
			int len = -1;
			while((len = in.read(buffer)) != -1) {
				out.write(buffer, 0, len);
			}
			out.flush();
		}
		catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		finally {
			try {
				if (in != null)
					in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				if (out != null)
					out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return true;
	}
	
	//add by zel 将其保存到指定的目录 package Name class Name
	public static void saveFile(String str) {
		String filePath = null;
		boolean hasSDCard = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
		if (hasSDCard) {
			filePath = Environment.getExternalStorageDirectory().toString() + File.separator + "hello.txt";
		} else
			filePath = Environment.getDownloadCacheDirectory().toString() + File.separator + "hello.txt";
		
		try {
			File file = new File(filePath);
			if (!file.exists()) {
				File dir = new File(file.getParent());
				dir.mkdirs();
				file.createNewFile();
			}
			FileOutputStream outStream = new FileOutputStream(file);
			outStream.write(str.getBytes());
			outStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	//add by zhouerlong
	public static String getEnvironmentPath() {
		File  f = Environment.getExternalStorageDirectory();
		if (f.exists()) {
			return f.getPath();
		}
			return null;
	}
	public static String  FILE_CACHE="file/cache";
	public static  File getRawFile(String fileName,Context c) {
		File dir = c.getExternalFilesDir(FILE_CACHE);
		if (!dir.exists()) {
			dir.mkdir();
		}
		File file = new File(dir,fileName);
		return file;
		
	}
	public static String loadFileToString(File absoluteFile ) {
		byte[] data = null;
		FileInputStream fis = null;
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			if (!absoluteFile.exists()) {
				return null;
			}
			try {
				fis = new FileInputStream(absoluteFile);

				try {
					data = new byte[fis.available()];
					fis.read(data);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {

				try {
					fis.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		String str = null;
		try {
			str = new String(data, "GB2312");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return str;

	}
	//add by zhouerlong

}