package com.prize.prizethemecenter.ui.utils;

import android.content.Context;
import android.os.Environment;
import android.os.StatFs;

import com.prize.app.util.MD5Util;
import com.prize.prizethemecenter.MainApplication;
import com.prize.prizethemecenter.bean.SingleThemeItemBean;
import com.prize.prizethemecenter.bean.table.LocalFontTable;
import com.prize.prizethemecenter.manage.DownloadTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * 
 * @author 写文件的工具类
 * 
 */
public class FileUtils
{
	/*** SD卡根目录 */
	public final static String CFG_PATH_SDCARD = Environment.getExternalStorageDirectory().getAbsolutePath();
	public static final String	ROOT_DIR		= "PrizeLiveStore";
	public static final String	FONT_DIR		= "LocalFontStore";
//	换成主题名称												+ UIUtils.getPackageName();
	public static final String	DOWNLOAD_DIR	= "download";
	public static final String	CACHE_DIR		= "cache";
	public static final String	ICON_DIR		= "icon";

	/** 判断SD卡是否挂载 */
	public static boolean isSDCardAvailable()
	{
		if (Environment.MEDIA_MOUNTED.equals(Environment
														.getExternalStorageState()))
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	/** 获取下载目录 */
	public static String getDownloadDir()
	{
		return getDir(DOWNLOAD_DIR);
	}

	/** 获取缓存目录 */
	public static String getCacheDir()
	{
		return getDir(CACHE_DIR);
	}

	/** 获取icon目录 */
	public static String getIconDir()
	{
		return getDir(ICON_DIR);
	}

	/** 获取应用目录，当SD卡存在时，获取SD卡上的目录，当SD卡不存在时，获取应用的cache目录 */
	public static String getDir(String name)
	{
		StringBuilder sb = new StringBuilder();
		if (isSDCardAvailable())
		{
			sb.append(getExternalStoragePath());
		}
		else
		{
			sb.append(getCachePath());
		}
		sb.append(name);
		sb.append(File.separator);
		String path = sb.toString();
		if (createDirs(path))
		{
			return path;
		}
		else
		{
			return null;
		}
	}

	/** 获取SD下的应用目录 */
	public static String getExternalStoragePath()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(Environment.getExternalStorageDirectory().getAbsolutePath());
		sb.append(File.separator);
		sb.append(ROOT_DIR);
		sb.append(File.separator);
		return sb.toString();
	}

	/** 获取SD下的字体目录 */
	public static String getExternalFontStoragePath()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(Environment.getExternalStorageDirectory().getAbsolutePath());
		sb.append(File.separator);
		sb.append(FONT_DIR);
		sb.append(File.separator);
		return sb.toString();
	}
	/** 获取应用的cache目录 */
	public static String getCachePath()
	{
		File f = MainApplication.curContext.getCacheDir();
		if (null == f)
		{
			return null;
		}
		else
		{
			return f.getAbsolutePath() + "/";
		}
	}

	/** 创建文件夹 */
	public static boolean createDirs(String dirPath)
	{
		File file = new File(dirPath);
		if (!file.exists() || !file.isDirectory()) { return file.mkdirs(); }
		return true;
	}

	/** 复制文件，可以选择是否删除源文件 */
	public static boolean copyFile(String srcPath, String destPath,
									boolean deleteSrc)
	{
		File srcFile = new File(srcPath);
		File destFile = new File(destPath);
		return copyFile(srcFile, destFile, deleteSrc);
	}

	/** 复制文件，可以选择是否删除源文件 */
	public static boolean copyFile(File srcFile, File destFile,
									boolean deleteSrc)
	{
		if (!srcFile.exists() || !srcFile.isFile()) { return false; }
		InputStream in = null;
		OutputStream out = null;
		try
		{
			in = new FileInputStream(srcFile);
			out = new FileOutputStream(destFile);
			byte[] buffer = new byte[1024];
			int i = -1;
			while ((i = in.read(buffer)) > 0)
			{
				out.write(buffer, 0, i);
				out.flush();
			}
			if (deleteSrc)
			{
				srcFile.delete();
			}
		}
		catch (Exception e)
		{
//			LogUtils.e(e);
			return false;
		}
		finally
		{
			IOUtils.close(out);
			IOUtils.close(in);
		}
		return true;
	}

	/** 判断文件是否可写 */
	public static boolean isWriteable(String path)
	{
		try
		{
			if (StringUtils.isEmpty(path)) { return false; }
			File f = new File(path);
			return f.exists() && f.canWrite();
		}
		catch (Exception e)
		{
//			LogUtils.e(e);
			return false;
		}
	}

	/** 修改文件的权限,例如"777"等 */
	public static void chmod(String path, String mode)
	{
		try
		{
			String command = "chmod " + mode + " " + path;
			Runtime runtime = Runtime.getRuntime();
			runtime.exec(command);
		}
		catch (Exception e)
		{
//			LogUtils.e(e);
		}
	}

	/**
	 * 把数据写入文件
	 * 
	 * @param is
	 *            数据流
	 * @param path
	 *            文件路径
	 * @param recreate
	 *            如果文件存在，是否需要删除重建
	 * @return 是否写入成功
	 */
	public static boolean writeFile(InputStream is, String path,
									boolean recreate)
	{
		boolean res = false;
		File f = new File(path);
		FileOutputStream fos = null;
		try
		{
			if (recreate && f.exists())
			{
				f.delete();
			}
			if (!f.exists() && null != is)
			{
				File parentFile = new File(f.getParent());
				parentFile.mkdirs();
				int count = -1;
				byte[] buffer = new byte[1024];
				fos = new FileOutputStream(f);
				while ((count = is.read(buffer)) != -1)
				{
					fos.write(buffer, 0, count);
				}
				res = true;
			}
		}
		catch (Exception e)
		{
//			LogUtils.e(e);
		}
		finally
		{
			IOUtils.close(fos);
			IOUtils.close(is);
		}
		return res;
	}

	/**
	 * 把字符串数据写入文件
	 * 
	 * @param content
	 *            需要写入的字符串
	 * @param path
	 *            文件路径名称
	 * @param append
	 *            是否以添加的模式写入
	 * @return 是否写入成功
	 */
	public static boolean writeFile(byte[] content, String path, boolean append)
	{
		boolean res = false;
		File f = new File(path);
		RandomAccessFile raf = null;
		try
		{
			if (f.exists())
			{
				if (!append)
				{
					f.delete();
					f.createNewFile();
				}
			}
			else
			{
				f.createNewFile();
			}
			if (f.canWrite())
			{
				raf = new RandomAccessFile(f, "rw");
				raf.seek(raf.length());
				raf.write(content);
				res = true;
			}
		}
		catch (Exception e)
		{
//			LogUtils.e(e);
		}
		finally
		{
			IOUtils.close(raf);
		}
		return res;
	}

	/**
	 * 把字符串数据写入文件
	 * 
	 * @param content
	 *            需要写入的字符串
	 * @param path
	 *            文件路径名称
	 * @param append
	 *            是否以添加的模式写入
	 * @return 是否写入成功
	 */
	public static boolean writeFile(String content, String path, boolean append)
	{
		return writeFile(content.getBytes(), path, append);
	}

	/**
	 * 把键值对写入文件
	 * 
	 * @param filePath
	 *            文件路径
	 * @param key
	 *            键
	 * @param value
	 *            值
	 * @param comment
	 *            该键值对的注释
	 */
	public static void writeProperties(String filePath, String key,
										String value, String comment)
	{
		if (StringUtils.isEmpty(key) || StringUtils.isEmpty(filePath)) { return; }
		FileInputStream fis = null;
		FileOutputStream fos = null;
		File f = new File(filePath);
		try
		{
			if (!f.exists() || !f.isFile())
			{
				f.createNewFile();
			}
			fis = new FileInputStream(f);
			Properties p = new Properties();
			p.load(fis);// 先读取文件，再把键值对追加到后面
			p.setProperty(key, value);
			fos = new FileOutputStream(f);
			p.store(fos, comment);
		}
		catch (Exception e)
		{
//			LogUtils.e(e);
		}
		finally
		{
			IOUtils.close(fis);
			IOUtils.close(fos);
		}
	}

	/** 根据值读取 */
	public static String readProperties(String filePath, String key,
										String defaultValue)
	{
		if (StringUtils.isEmpty(key) || StringUtils.isEmpty(filePath)) { return null; }
		String value = null;
		FileInputStream fis = null;
		File f = new File(filePath);
		try
		{
			if (!f.exists() || !f.isFile())
			{
				f.createNewFile();
			}
			fis = new FileInputStream(f);
			Properties p = new Properties();
			p.load(fis);
			value = p.getProperty(key, defaultValue);
		}
		catch (IOException e)
		{
//			LogUtils.e(e);
		}
		finally
		{
			IOUtils.close(fis);
		}
		return value;
	}

	/** 把字符串键值对的map写入文件 */
	public static void writeMap(String filePath, Map<String, String> map,
								boolean append, String comment)
	{
		if (map == null || map.size() == 0 || StringUtils.isEmpty(filePath)) { return; }
		FileInputStream fis = null;
		FileOutputStream fos = null;
		File f = new File(filePath);
		try
		{
			if (!f.exists() || !f.isFile())
			{
				f.createNewFile();
			}
			Properties p = new Properties();
			if (append)
			{
				fis = new FileInputStream(f);
				p.load(fis);// 先读取文件，再把键值对追加到后面
			}
			p.putAll(map);
			fos = new FileOutputStream(f);
			p.store(fos, comment);
		}
		catch (Exception e)
		{
//			LogUtils.e(e);
		}
		finally
		{
			IOUtils.close(fis);
			IOUtils.close(fos);
		}
	}

	/** 把字符串键值对的文件读入map */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static Map<String, String> readMap(String filePath,
												String defaultValue)
	{
		if (StringUtils.isEmpty(filePath)) { return null; }
		Map<String, String> map = null;
		FileInputStream fis = null;
		File f = new File(filePath);
		try
		{
			if (!f.exists() || !f.isFile())
			{
				f.createNewFile();
			}
			fis = new FileInputStream(f);
			Properties p = new Properties();
			p.load(fis);
			map = new HashMap<String, String>((Map) p);// 因为properties继承了map，所以直接通过p来构造一个map
		}
		catch (Exception e)
		{
//			LogUtils.e(e);
		}
		finally
		{
			IOUtils.close(fis);
		}
		return map;
	}

	/** 改名 */
	public static boolean copy(String src, String des, boolean delete)
	{
		File file = new File(src);
		if (!file.exists()) { return false; }
		File desFile = new File(des);
		FileInputStream in = null;
		FileOutputStream out = null;
		try
		{
			in = new FileInputStream(file);
			out = new FileOutputStream(desFile);
			byte[] buffer = new byte[1024];
			int count = -1;
			while ((count = in.read(buffer)) != -1)
			{
				out.write(buffer, 0, count);
				out.flush();
			}
		}
		catch (Exception e)
		{
//			LogUtils.e(e);
			return false;
		}
		finally
		{
			IOUtils.close(in);
			IOUtils.close(out);
		}
		if (delete)
		{
			file.delete();
		}
		return true;
	}

	/**
	 * 目标下 递归删除文件和文件夹
	 *
	 * @param file
	 *            要删除的根目录
	 */
	public static boolean recursionDeleteFile(File file) {
		if(!file.exists()){
			return false;
		}
		if (file.isFile()) {
			file.delete();
			return true;
		}
		if (file.isDirectory()) {
			File[] childFile = file.listFiles();
			if (childFile == null || childFile.length == 0) {
				 file.delete();
				return true;
			}
			for (File f : childFile) {
				recursionDeleteFile(f);
			}
			file.delete();
		}
		return true;
	}

	/**
	 * 删除指定路劲下的文件
	 */
	public static boolean DeleteFile(File file){
		if(!file.exists()) return false;
		if (file.isFile()) {
			file.delete();
			return true;
		}
		return true;
	}

	/**
	 * 获取下载文件的路径
	 * @param themeName
	 * @param type 1 主题 2 壁纸 3 字体
	 * @return
	 */
	public static  String getDownloadPath(String themeName,int type) {
		if(type==1){
			return new File(FileUtils.getDir("theme"), themeName+type+ ".zip").getAbsolutePath();
		}
		else if(type==2){
			return new File(FileUtils.getDir("wallpaper"), themeName +type+ ".zip").getAbsolutePath();
		}
		else if(type==3){
			return new File(FileUtils.getDir("font"), themeName +type+ ".ttf").getAbsolutePath();
		}
		else{
			return null;
		}
	}

	/**
	 * 获取下载文件的路径
	 * @param themeName
	 * @param type 1 主题 2 壁纸 3 字体
	 * @return
	 */
	public static  String getDownloadPathMine(String themeName,int type) {
		if(type==1){
			return new File(FileUtils.getDir("theme"), themeName + ".zip").getAbsolutePath();
		}
		else if(type==2){
			return new File(FileUtils.getDir("wallpaper"), themeName + ".zip").getAbsolutePath();
		}
		else if(type==3){
			return new File(FileUtils.getDir("font"), themeName + ".ttf").getAbsolutePath();
		}
		else{
			return null;
		}
	}

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

	/**
	 *
	 * 获取临时下载文件名称（包含后缀）
	 *
	 * @param ItemsBean
	 * @return String
	 */
	public static String getTempFileName(SingleThemeItemBean.ItemsBean ItemsBean) {
		String pattern = "%s";
		return String.format(pattern,ItemsBean.getId());
	}




	/**
	 *
	 * 获取临时下载文件名称（包含后缀）
	 *
	 * @param ItemsBean
	 * @return String
	 */
	public static String getTempFileName(String id) {
		String pattern = "%s";
		return String.format(pattern,id);
	}

	/**
	 * 获取临时下载文件的路径
	 * @param themeName
	 * @param type 1 主题 2 壁纸 3 字体
	 * @return
	 */
	public static  String getDownloadTempPath(String themeName,int type) {
		if(type==1){
			return new File(FileUtils.getDir("theme"), themeName + ".temp").getAbsolutePath();
		}
		else if(type==2){
			return new File(FileUtils.getDir("wallpaper"), themeName + ".temp").getAbsolutePath();
		}
		else if(type==3){
			return new File(FileUtils.getDir("font"), themeName + ".temp").getAbsolutePath();
		}
		else{
			return null;
		}
	}
	/**
	 *
	 * 根据名称删除临时文件
	 *
	 * @param songDetailInfo
	 * @return void
	 */
	public static void deleteTmpDownloadFile(SingleThemeItemBean.ItemsBean ItemsBean,int type) {
		DownloadTask.deleteTmpDownloadFile(getTempFileName(ItemsBean),type);
	}


	/**
	 *
	 * 根据名称删除临时文件
	 *
	 * @param songDetailInfo
	 * @return void
	 */
	public static void deleteTmpDownloadFile(String id,int type) {
		DownloadTask.deleteTmpDownloadFile(getTempFileName(id),type);
	}

	/**
	 * 判断文件是否已经下载
	 *
	 * @param ItemsBean
	 * @return boolean
	 */
	public static boolean isFileExists(SingleThemeItemBean.ItemsBean bean,int type) {
		String downloadFile = FileUtils.getDownloadPath(bean.getId(),type);
		return new File(downloadFile).exists();
	}

	/**
	 *
	 * 根据名称删除临时文件
	 *
	 * @param songDetailInfo
	 * @return void
	 */
	public static void deleteDownloadFile(String id,int type) {
		DownloadTask.deleteDownloadFile(getTempFileName(id),type);
	}


	/**
	 * 判断文件是否已经下载
	 *
	 * @param ItemsBean
	 * @return boolean
	 */
	public static boolean isFileExists(String id,int type) {
		String downloadFile = FileUtils.getDownloadPath(id,type);
		return new File(downloadFile).exists();
	}
}
