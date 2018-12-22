package com.prize.prizethemecenter.manage;

/**
 * 下载状态抽象类
 * Created by Administrator on 2016/12/22.
 */
public abstract class DownloadState {
    /** 下载状态, 注意:不要轻易修改值,数据库有记录!!!! */
    public static final int STATE_DOWNLOAD_WAIT = 0;
    // 注意：STATE_DOWNLOAD_SUCESS, STATE_DOWNLOAD_ERROR,
    // STATE_DOWNLOAD_LOADING，值不允许改变，和老版本兼容的状态值，数据库中有记录
    /** 取消下载 */
    public static final int STATE_DOWNLOAD_CANCEL =1;
    /** 下载过程发生了错误 */
    public static final int STATE_DOWNLOAD_ERROR = 2;
    /** 下载中 */
    public static final int STATE_DOWNLOAD_START_LOADING = 3;
    /** 通知下载开始 */
    public static final int STATE_DOWNLOAD_PAUSE = 4;
    // 注意：STATE_DOWNLOAD_SUCESS, STATE_DOWNLOAD_ERROR,
    // STATE_DOWNLOAD_LOADING，值不允许改变，和老版本兼容的状态值，数据库中有记录
    /** 通知进度 */
    public static final int STATE_DOWNLOAD_UPDATE_PROGRESS = 5;
    /** 下载成功 */
    public static final int STATE_DOWNLOAD_SUCESS = 6;
    /** 下载，并安装成功 */
    public static final int STATE_DOWNLOAD_INSTALLED = 7;
    /** 下载，需要更新 */
    public static final int STATE_DOWNLOAD_UPDATE = 8;

    /** 下载模块初始化完成 **/
    public static final int STATE_DOWNLOAD_MODE_INIT = 1000;
    public static final int STATE_DOWNLOAD_REFRESH = STATE_DOWNLOAD_MODE_INIT + 1;

    /** 下载错误值 */
    public static final int ERROR_NONE = 0;
    /** 无网络 **/
    public static final int ERROR_CODE_NO_NET = 0x2000;
    /** 无SD卡 **/
    public static final int ERROR_CODE_NO_SDCARD = ERROR_CODE_NO_NET + 1;
    /** 数据库操作失败 **/
    public static final int ERROR_CODE_DATABASE = ERROR_CODE_NO_SDCARD + 1;
    /** SD卡读写失败 */
    public static final int ERROR_CODE_IO = ERROR_CODE_DATABASE + 1;
    /** SD卡 无可用空间 */
    public static final int ERROR_CODE_SD_NOSAPCE = ERROR_CODE_IO + 1;
    /** 网络连接异常 */
    public static final int ERROR_CODE_HTTP = ERROR_CODE_SD_NOSAPCE + 1;
    /** 网络连接异常 */
    public static final int ERROR_CODE_TIME_OUT = ERROR_CODE_HTTP + 1;
    /** 下载连接超时 */
    public static final int ERROR_CODE_URL_ERROR = ERROR_CODE_TIME_OUT + 1;
    /** 仅WIFI网络下线，但无WIFI */
    public static final int ERROR_CODE_NOT_WIFI = ERROR_CODE_URL_ERROR + 1;
    /** 其他未知错误 */
    public static final int ERROR_CODE_UNKOWN = ERROR_CODE_NOT_WIFI + 1;

    /**
     * 下载状态的返回值，处理下载状态，task状态的变化
     *
     * @param state
     *            ： 状态值
     * @param pkgname
     *            ： 下载的包名
     * @param errorCode
     *            : 错误状态，中止的时候
     */

    public abstract void onDownloadState(int state, String  theme_id,int errorCode);
    /**
     * 进度更新
     *
     * @param pkgname
     * @param downloadFileSize
     * @param downloadPosition
     * @param downloadSpeed
     *            以Kb单位返回
     */
    public abstract void updateDownloadProgress(int theme_id,
                                                int downloadFileSize, int downloadPosition, int downloadSpeed);
}
