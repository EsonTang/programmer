package com.prize.prizethemecenter.manage;


import com.prize.app.util.JLog;
import com.prize.prizethemecenter.R;
import com.prize.prizethemecenter.bean.SingleThemeItemBean;
import com.prize.prizethemecenter.exception.URLInvalidException;
import com.prize.prizethemecenter.ui.utils.FileUtils;
import com.prize.prizethemecenter.ui.utils.ToastUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.util.Random;

/**
 * Created by Administrator on 2016/12/22.
 */
public class Download implements Runnable {
    /**
     * 下载缓冲区
     */
    // private static final int BUFFER_LEN = 1024;
    private static final int BUFFER_LEN = 2048;
    /**
     * 更新数据库的频率
     */
    private static final int UPDATE_SQL_NUM = 100;
    private static final String TAG = "Download";

    private File downloadTmpFile; // 下载文件备份
    private String downloadFilePath;
    private String downloadUrl;
    private int themeID;
    private DownloadClient mHttpClient;
    /***
     * 当前下载的位置
     */
    private int downloadPosition;

    /**
     * 游戏的APK大小
     */
    private int downloadFileSize;

    private boolean isRunning = false;

    private int downloadResult = 0;

    private DownloadState backListener;
    /**
     * 下载状态
     */
    private int downloadState;

    private int mPosition;

    private int type;

    public Download(SingleThemeItemBean.ItemsBean bean, DownloadState listener,int type) {
        downloadUrl = bean.getDownload_url();
        backListener = listener;
        themeID = Integer.parseInt(bean.getId());
        downloadState = DownloadState.STATE_DOWNLOAD_WAIT;
        this.type = type;

    }

    /**
     * 状态置成等待下载，并通知UI，下载准备中
     */
    public void readyDownload() {
        notifyDownloadState(DownloadState.STATE_DOWNLOAD_WAIT);
    }

    public long getDownloadPosition() {
        return downloadPosition;
    }


    /**
     * 停止当前任务
     *
     * @param result ：停止当前任务的原因
     */
    public void stopDownloadByResult(int result) {
        isRunning = false;
        // 只有是未知错误的时候，设置成错误下载，其他情况只是暂停。错误下载，会删除原数据重新下载
        if (DownloadState.ERROR_CODE_UNKOWN == result) {
            notifyDownloadState(DownloadState.STATE_DOWNLOAD_ERROR);
            ToastUtils.showToast(R.string.down_failed);
        } else {
            notifyDownloadState(DownloadState.STATE_DOWNLOAD_PAUSE);
//            ToastUtils.showToast(R.string.down_pause);
        }
        downloadResult = result;
    }

    private void notifyDownloadState(int state) {
        backListener.onDownloadState(state, themeID+""+type, downloadResult);
        downloadState = state;
    }

    @Override
    public void run() {
        if (DownloadState.STATE_DOWNLOAD_WAIT != downloadState) {
            return;
        }
        if (isRunning) {
            return;
        }
        try {
            android.os.Process.setThreadPriority((int) Thread.currentThread().getId(), android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
        } catch (IllegalArgumentException e) {
        } catch (SecurityException e) {
        }
        isRunning = true;
        notifyDownloadState(DownloadState.STATE_DOWNLOAD_START_LOADING);
        JLog.i(TAG, ", 开始下载 Download 包名 =" + themeID);
        // 通知下载开始
        mHttpClient = new DownloadClient(downloadUrl, themeID+""+type);
        try {
            downloadFilePath = FileUtils.getDownloadTempPath(themeID + "", type);
            downloadTmpFile = new File(downloadFilePath); // 下载的临时文件
            if (downloadTmpFile.exists()) {
                // 如果文件已经存在,断点续传
                downloadPosition = (int) downloadTmpFile.length();
            } else {
                File parentFile = downloadTmpFile.getParentFile();
                if (null != parentFile && !parentFile.exists()) {
                    parentFile.mkdirs();
                }
                downloadTmpFile.createNewFile();
            }
            // 开始下载
            download(downloadPosition);
        } catch (SocketTimeoutException e) {
            e.printStackTrace();
            stopDownloadByResult(DownloadState.ERROR_CODE_TIME_OUT);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            stopDownloadByResult(DownloadState.ERROR_CODE_HTTP);
        } catch (URLInvalidException e) {
            e.printStackTrace();
            stopDownloadByResult(DownloadState.ERROR_CODE_URL_ERROR);
        } catch (IOException e) {
            e.printStackTrace();
            stopDownloadByResult(DownloadState.ERROR_CODE_IO);
        } catch (Exception e) {
            e.printStackTrace();
            stopDownloadByResult(DownloadState.ERROR_CODE_UNKOWN);
        } finally {
            if (mHttpClient != null) {
                mHttpClient.close();
                mHttpClient = null;
            }
        }


    }

    /**
     * 断点续传
     *
     * @param serverPos
     * @throws SocketTimeoutException
     * @throws MalformedURLException
     * @throws IOException
     */
    private void download(final int serverPos) {
        InputStream inputStream = null;
        RandomAccessFile randomAccessFile = null;

        try {
            inputStream = mHttpClient.getInputStream(serverPos);
            if (inputStream == null) {
                stopDownloadByResult(DownloadState.ERROR_CODE_HTTP);
                return;
            }

        } catch (Exception e) {
//            throw new URLInvalidException("下载地址异常: url:\"" + downloadUrl + "\"");
        }
        try {
            randomAccessFile = new RandomAccessFile(downloadTmpFile, "rw");
            int size = mHttpClient.getContentLength();
            if (size > 0) {
                downloadFileSize = size;
            }
            if (!checkSDSpaceIsEnough(downloadFileSize)) {
                // 空间不足
                stopDownloadByResult(DownloadState.ERROR_CODE_SD_NOSAPCE);
                ToastUtils.showToast("内存已满，请释放部分内存后再重试！");
                DownloadTaskMgr mgr = DownloadTaskMgr.getInstance();
                mgr.pauseAllDownload();
                return;
            }
            if (size > 0) {
                downloadFileSize += serverPos;
            }

            if (downloadPosition >= downloadFileSize && downloadFileSize>512) {
                notifyDownloadState(DownloadState.STATE_DOWNLOAD_SUCESS);
                return;
            }

            byte[] buf = new byte[BUFFER_LEN];
            int len;
            randomAccessFile.seek(serverPos);
            randomAccessFile.setLength(serverPos);

            int nowDownModNum = 0;
            JLog.e(TAG, "开始读取");
            long startTime = System.currentTimeMillis();
            int downloadSize = 0;
            int tempDownloadSize = 0;

            while (isRunning && (-1 != (len = inputStream.read(buf)))) {
                if (!downloadTmpFile.exists()) {
                    notifyDownloadState(DownloadState.STATE_DOWNLOAD_PAUSE);
                    if (randomAccessFile != null) {
                        randomAccessFile.close();
                    }
                    if (inputStream != null) {
                        inputStream.close();
                    }
                    return;
                }
                randomAccessFile.write(buf, 0, len);
                downloadPosition += len;
                downloadSize += len;
                if (nowDownModNum == UPDATE_SQL_NUM) {
                    nowDownModNum = 0;
                    long curTime = System.currentTimeMillis();
                    int usedTime = (int) ((curTime - startTime) / 1000);

                    if (usedTime == 0)
                        usedTime = 1;
                    int downloadSpeed = (downloadSize / usedTime) / 1024;

                    if (backListener != null) {
                        if(tempDownloadSize == downloadSize){
                            backListener.updateDownloadProgress(Integer.parseInt(themeID + "" + type), downloadFileSize, downloadPosition, 0);
                        }else{
                            backListener.updateDownloadProgress(Integer.parseInt(themeID + "" + type), downloadFileSize, downloadPosition, downloadSpeed);
                        }
                    }
                }
                nowDownModNum++;
                downloadSize+=len;
                tempDownloadSize = downloadSize;
            }
            if(backListener != null){
                backListener.updateDownloadProgress(Integer.parseInt(themeID + "" + type),downloadFileSize, downloadPosition, new Random().nextInt(30)+50);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (randomAccessFile != null) {
                    randomAccessFile.close();
                }
            } catch (Exception ex) {
            } finally {
                try {
                    if (inputStream != null)
                        inputStream.close();
                } catch (Exception exp) {
                }
            }
        }

        if (isRunning) {
            if (downloadPosition >= downloadFileSize) {
                JLog.i(TAG, "downloadPosition=" + downloadPosition + ":::downloadFileSize=" + downloadFileSize);
                // download success
                backListener.updateDownloadProgress(Integer.parseInt(themeID + "" + type), downloadFileSize,
                        downloadPosition, 0);
                notifyDownloadState(DownloadState.STATE_DOWNLOAD_SUCESS);
                JLog.e(TAG, "下载完成通知");
            }
        }

    }


    /**
     * 检查SD是否有足够空间
     *
     * @param loadSize
     * @return
     */
    private boolean checkSDSpaceIsEnough(long loadSize) {
        if (FileUtils.getSDAvailaleSize() > (loadSize * 2)) {
            return true;
        }
        return false;
    }
}
