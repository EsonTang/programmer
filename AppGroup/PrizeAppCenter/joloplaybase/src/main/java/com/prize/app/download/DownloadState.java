package com.prize.app.download;

/**
 * 
 **
 * 下载状态抽象类
 * 
 * @author prize
 * @version V1.0
 */
public abstract class DownloadState {
	/** 下载状态, 注意:不要轻易修改值,数据库有记录!!!! */
	public static final int STATE_DOWNLOAD_WAIT = 0;
	// 注意：STATE_DOWNLOAD_SUCESS, STATE_DOWNLOAD_ERROR,
	// STATE_DOWNLOAD_LOADING，值不允许改变，和老版本兼容的状态值，数据库中有记录
	/** 下载成功 */
	public static final int STATE_DOWNLOAD_SUCESS = 1;
	/** 下载过程发生了错误 */
	public static final int STATE_DOWNLOAD_ERROR = 2;
	/** 下载中 */
	public static final int STATE_DOWNLOAD_START_LOADING = 3;
	/** 暂停 */
	public static final int STATE_DOWNLOAD_PAUSE = 4;
	// 注意：STATE_DOWNLOAD_SUCESS, STATE_DOWNLOAD_ERROR,
	// STATE_DOWNLOAD_LOADING，值不允许改变，和老版本兼容的状态值，数据库中有记录
	/** 通知进度 */
	public static final int STATE_DOWNLOAD_UPDATE_PROGRESS = 5;
	/** 下载，并安装成功 */
	public static final int STATE_DOWNLOAD_INSTALLED = 6;
	/** 取消下载 */
	public static final int STATE_DOWNLOAD_CANCEL = 7;
	/** 合成失败 */
	 static final int STATE_PATCH_FAILE =8;
	/** 合成中...*/
	 static final int STATE_PATCHING =9;
	/** 合成成功.*/
	 static final int STATE_PATCH_SUCESS =10;

	/**下载模块初始化完成 **/
	 static final int STATE_DOWNLOAD_MODE_INIT = 1000;
	/** 静默安装开始以及静默安装结束*/
	public static final int STATE_DOWNLOAD_REFRESH = STATE_DOWNLOAD_MODE_INIT + 1;

	/** 下载错误值 */
	 static final int ERROR_NONE = 0;
	/** 无网络 **/
	 static final int ERROR_CODE_NO_NET = 0x2000;
	/** 无SD卡 **/
	 static final int ERROR_CODE_NO_SDCARD = ERROR_CODE_NO_NET + 1;
	/** 数据库操作失败 **/
	 private static final int ERROR_CODE_DATABASE = ERROR_CODE_NO_SDCARD + 1;
	/** SD卡读写失败 */
	 static final int ERROR_CODE_IO = ERROR_CODE_DATABASE + 1;
	/** SD卡 无可用空间 */
	 static final int ERROR_CODE_SD_NOSAPCE = ERROR_CODE_IO + 1;
	/** 网络连接异常 */
	 static final int ERROR_CODE_HTTP = ERROR_CODE_SD_NOSAPCE + 1;
	/** 网络连接异常 */
	 static final int ERROR_CODE_TIME_OUT = ERROR_CODE_HTTP + 1;
	/** 下载连接超时 */
	 static final int ERROR_CODE_URL_ERROR = ERROR_CODE_TIME_OUT + 1;
	/** 仅WIFI网络下线，但无WIFI */
	 private static final int ERROR_CODE_NOT_WIFI = ERROR_CODE_URL_ERROR + 1;
	/** 其他未知错误 */
	 static final int ERROR_CODE_UNKOWN = ERROR_CODE_NOT_WIFI + 1;

	/**
	 * 下载状态的返回值，处理下载状态，task状态的变化
	 * 
	 * @param state
	 *            ： 状态值
	 * @param pkgname
	 *            ： 下载的包名
	 * @param errorCode
	 *            : 错误状态，中止的时候
	 *            ： 下载的包名
	 * @param isNewDownload
	 *            : 是否是新的下载任务
	 */

	public abstract void onDownloadState(int state, String pkgname,
			int errorCode,int position,boolean isNewDownload);

	/**
	 * 进度更新
	 * 
	 * @param pkgname 包名
	 * @param downloadFileSize 文件大小
	 * @param downloadPosition 已下载的位置
	 * @param downloadSpeed 下载速度
	 */
	public abstract void updateDownloadProgress(String pkgname,
			int downloadFileSize, int downloadPosition,int downloadSpeed);
	
}
