package com.prize.music.admanager;

import java.io.File;

import android.os.Environment;

/**
 * 启动页广告的配置信息
 * 
 * @author huangchangguo
 * 
 *         2017.5.22
 *
 */
public class Configs {
	/*** 请求地址 */
	public static final String NET_URL = "http://adapi.szprize.cn/Music/Ad/getAdData";
	//public static final String NET_URL = "http://101.200.187.142:8093/Music/Ad/getAdData";
	
	// 下载
	public static final String ANDROID_APP_SUFFIX = ".apk";
	public static final String PRIZE_TEM_FILE_SUFFIX = ".prize";
	public static final String PRIZE_MUSIC = "music";
	public static final String DOWNLOAD_FOLDER_NAME = "download";
	public static final String SP_TIME_KEY = "time_key";
	public static final String APP_TAG = "ad_events";
	public static final String UA = "user_agent";
	public static final String SPLASH_IP_SP_KEY = "ip_key";
	public static final String CHEACK_UPDATE_SP_KEY = "update_key";
	/*** apk存放的路径 */
	public static final String APK_FILE_PATH = new StringBuilder(
			Environment.getExternalStorageDirectory().getAbsolutePath()).append(File.separator)
					.append(DOWNLOAD_FOLDER_NAME).append(File.separator).append(PRIZE_MUSIC).toString();
	/*** 文件路径 */
	public static final String APK_FILETEMP_PATH = new StringBuilder(
			Environment.getExternalStorageDirectory().getAbsolutePath()).append(File.separator)
					.append(DOWNLOAD_FOLDER_NAME).append(File.separator).append(PRIZE_MUSIC).toString();
	
	/*********************** 自升级    2017.7.3 huangchangguo ************************************/
	/**检测升级的间隔，每天检测一次*/	
	public static final long UPDATE_PERIOD = 24*60*60;
}
