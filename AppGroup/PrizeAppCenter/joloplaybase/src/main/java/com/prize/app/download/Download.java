package com.prize.app.download;

import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.cundong.utils.PatchUtils;
import com.prize.app.BaseApplication;
import com.prize.app.excepiton.URLInvalidException;
import com.prize.app.util.ApkUtils;
import com.prize.app.util.FileUtils;
import com.prize.app.util.JLog;
import com.prize.app.util.SignUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.util.Random;

/**
 * 实际下载线程（implements Runnable）
 */
public class Download implements Runnable {
    private static final String TAG = "Download";
    /**
     * 下载缓冲区
     */
    private static final int BUFFER_LEN = 2048;
    /**
     * 更新数据库的频率
     */
    private static final int UPDATE_SQL_NUM = 100;

    private File downloadTmpFile; // 下载文件备份
    private String downloadFilePath;
    private String downloadUrl;
    private String downloadPkgName;
    private String gameCode;
    private DownloadClient mHttpClient;

    /**
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
    private String toApkMd5;
    private String pageInfo;

    public Download(String loadGameCode, String loadUrl, String pkgName,
                    int apkSize, int position, String toApkMd5, DownloadState listener,String pageInfo) {
        downloadUrl = loadUrl;
        gameCode = loadGameCode;
        backListener = listener;
        downloadPkgName = pkgName;
        downloadFileSize = apkSize;
        downloadState = DownloadState.STATE_DOWNLOAD_WAIT;
        mPosition = position;
        this.toApkMd5 = toApkMd5;
        this.pageInfo = pageInfo;
    }

    /**
     * 状态置成等待下载，并通知UI，下载准备中
     */
    void readyDownload(boolean isNewDownload) {
        notifyDownloadState(DownloadState.STATE_DOWNLOAD_WAIT, isNewDownload);
    }

    private void notifyDownloadState(int state, boolean isNewDownload) {
        backListener.onDownloadState(state, downloadPkgName, downloadResult,
                mPosition, isNewDownload);
        if (DownloadState.STATE_PATCHING == state) {
            String oldApkSource = ApkUtils.getSourceApkPath(BaseApplication.curContext, downloadPkgName);
            String new_apk_path = FileUtils.getGameAPKFilePath(gameCode);
            int patchResult = PatchUtils.patch(oldApkSource, new_apk_path, downloadFilePath);
            File file = new File(FileUtils.getPatchFilePath(gameCode));
            if (file != null && file.exists()) {
                file.delete();
            }
            if (patchResult == 0) {
                if (SignUtils.checkMd5(new_apk_path, toApkMd5)) {
                    state = DownloadState.STATE_PATCH_SUCESS;
                } else {
                    android.telecom.Log.i(TAG, "DownloadState.STATE_PATCH_FAILE,checkMd5 faile");
                    state = DownloadState.STATE_PATCH_FAILE;
                }
            } else {
                android.telecom.Log.i(TAG, "DownloadState.STATE_PATCH_FAILE");
                state = DownloadState.STATE_PATCH_FAILE;
            }
            backListener.onDownloadState(state, downloadPkgName, downloadResult,
                    mPosition, isNewDownload);
        }
        downloadState = state;
    }

    /**
     * 停止当前任务
     *
     * @param result ：停止当前任务的原因
     */
     void stopDownloadByResult(int result) {
        isRunning = false;
        // 只有是未知错误的时候，设置成错误下载，其他情况只是暂停。错误下载，会删除原数据重新下载
        if (DownloadState.ERROR_CODE_UNKOWN == result) {
            notifyDownloadState(DownloadState.STATE_DOWNLOAD_ERROR, false);
        } else {
            notifyDownloadState(DownloadState.STATE_DOWNLOAD_PAUSE, false);
        }

        //UserBroadcast有关，不知道干什么的
//        if (result != DownloadState.STATE_DOWNLOAD_PAUSE
//                && result != DownloadState.ERROR_CODE_SD_NOSAPCE
//                && result != DownloadState.STATE_DOWNLOAD_WAIT) {
//            Intent intent = new Intent("net_error");
//            intent.putExtra("downloadPkgName", downloadPkgName);
//            BaseApplication.curContext.sendBroadcast(intent);
//        }
        downloadResult = result;
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
            android.os.Process.setThreadPriority((int) Thread.currentThread()
                    .getId(), android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);

        } catch (IllegalArgumentException e) {
            e.printStackTrace();

        } catch (SecurityException e) {
            e.printStackTrace();

        }
        isRunning = true;
        notifyDownloadState(DownloadState.STATE_DOWNLOAD_START_LOADING, false);
        // 通知下载开始
        mHttpClient = new DownloadClient(downloadUrl, downloadPkgName);
        try {
            goToDownLoad();

        } catch (SocketTimeoutException e) {
            e.printStackTrace();
            try {
                goToDownLoad();
            } catch (IOException e1) {
                e1.printStackTrace();
                Log.i(TAG, "pkg ="
                        + downloadPkgName + "-SocketTimeoutException-IOException-e1=" + e1.getMessage());
                stopDownloadByResult(DownloadState.ERROR_CODE_TIME_OUT);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
            Log.i(TAG, "pkg ="
                    + downloadPkgName + "--MalformedURLException-e=" + e.getMessage());
            stopDownloadByResult(DownloadState.ERROR_CODE_HTTP);
        } catch (URLInvalidException e) {
            e.printStackTrace();
            Log.i(TAG, "pkg ="
                    + downloadPkgName + "--URLInvalidException-e=" + e.getMessage());
            stopDownloadByResult(DownloadState.ERROR_CODE_URL_ERROR);
        } catch (IOException e) {
            e.printStackTrace();
            JLog.i(TAG, "pkg ="
                    + downloadPkgName + "--IOException-e=" + e.getMessage());
            try {
                goToDownLoad();
            } catch (IOException e1) {
                e1.printStackTrace();
                Log.i(TAG, "pkg ="
                        + downloadPkgName + "-IOException-IOException-e1=" + e1.getMessage());
                stopDownloadByResult(DownloadState.ERROR_CODE_IO);
            }

        } catch (Exception e) {
            e.printStackTrace();
            Log.i(TAG, "pkg ="
                    + downloadPkgName + "--Exception-e=" + e.getMessage());
            stopDownloadByResult(DownloadState.ERROR_CODE_UNKOWN);
        } finally {
            if (mHttpClient != null) {
                mHttpClient.close();
                mHttpClient = null;
            }
        }
    }

    private void goToDownLoad() throws IOException {
        // 创建临时文件*.prize
        if (!TextUtils.isEmpty(toApkMd5)) {
            downloadFilePath = FileUtils.getPatchFilePath(gameCode);
            downloadTmpFile = new File(downloadFilePath); // 下载的临时文件
        } else {
            downloadFilePath = FileUtils.getDownloadTmpFilePath(gameCode);
            downloadTmpFile = new File(downloadFilePath); // 下载的临时文件

        }
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
    }

    /**
     * 检查SD是否有足够空间
     *
     * @param loadSize 需要下载的app大小
     * @return boolean
     */
    private boolean checkSDSpaceIsEnough(long loadSize) {
        return (FileUtils.getSDAvailaleSize() > (loadSize * 2));
//            return true;
//        }
//        return false;
    }

    /**
     * 断点续传
     *
     * @param serverPos 已下载的位置（断点下载）
     * @throws IOException IOException
     */
    private void download(final int serverPos) throws IOException {
        InputStream inputStream = null;
        RandomAccessFile randomAccessFile = null;

        try {
            inputStream = mHttpClient.getInputStream(serverPos,pageInfo);
            if (inputStream == null) {
                stopDownloadByResult(DownloadState.ERROR_CODE_HTTP);
                return;
            }

        } catch (Exception e) {
            Log.i(TAG, "download(final int serverPos)=" + e.getMessage());
            throw new URLInvalidException("下载地址异常: url:\"" + downloadUrl + "\"");
        }

        try {
            randomAccessFile = new RandomAccessFile(downloadTmpFile, "rw");
            int size = mHttpClient.getContentLength();//获取返回数据流大小
            if (size > 0) {
                downloadFileSize = size;
            }
            if (!checkSDSpaceIsEnough(downloadFileSize)) {
                // 空间不足
                stopDownloadByResult(DownloadState.ERROR_CODE_SD_NOSAPCE);
                showToast("内存已满，请释放部分内存后再重试！");
                DownloadTaskMgr.getInstance().pauseAllDownload();
                return;
            }

            if (size > 0) {
                downloadFileSize = downloadFileSize + serverPos; // 文件是实际大小
            }

            if (downloadPosition >= downloadFileSize) {
                if (!TextUtils.isEmpty(toApkMd5)) {
                    notifyDownloadState(DownloadState.STATE_PATCHING, false);
                } else {
                    notifyDownloadState(DownloadState.STATE_DOWNLOAD_SUCESS, false);
                }
                return;
            }

            byte[] buf = new byte[BUFFER_LEN];// 从服务端读取的byte流
            int len;// 从服务端读取的byte长度
            randomAccessFile.seek(serverPos);
            randomAccessFile.setLength(serverPos);

            int nowDownModNum = 0;
            int downloadSize = 0;
            long startTime = System.currentTimeMillis();
            while (isRunning && (-1 != (len = inputStream.read(buf)))) {
                // 2015-12-04
                if (!downloadTmpFile.exists()) {
                    notifyDownloadState(DownloadState.STATE_DOWNLOAD_PAUSE, false);
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

                if (nowDownModNum == UPDATE_SQL_NUM) {
                    nowDownModNum = 0;

                    long curTime = System.currentTimeMillis();
                    int usedTime = (int) ((curTime - startTime) / 1000);
                    if (usedTime == 0)
                        usedTime = 1;
                    int downloadSpeed = (downloadSize / usedTime) / 1024;
                    if (backListener != null) {
                        backListener.updateDownloadProgress(downloadPkgName,
                                downloadFileSize, downloadPosition,
                                downloadSpeed);
                    }
                }
                downloadSize += len;
                nowDownModNum++;
            }

			/*PRIZE-暂停后，马上点击继续下载，会读取上次的记录-longbaoxiu-2016-06-16-start*/

            if (backListener != null) {
                backListener.updateDownloadProgress(downloadPkgName,
                        downloadFileSize, downloadPosition, new Random().nextInt(30) + 50);
            }
            /*PRIZE-暂停后，马上点击继续下载，会读取上次的记录-longbaoxiu-2016-06-16-end*/

        } catch (IOException e) {
            throw e;
        } finally {
            try {
                if (randomAccessFile != null) {
                    randomAccessFile.close();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                try {
                    if (inputStream != null)
                        inputStream.close();
                } catch (Exception exp) {
                    exp.printStackTrace();
                }
            }
        }
        if (isRunning) {
            if (downloadPosition >= downloadFileSize) {
                JLog.i(TAG, "downloadPosition=" + downloadPosition
                        + "---downloadFileSize=" + downloadFileSize);
                // download success
                backListener.updateDownloadProgress(downloadPkgName,
                        downloadFileSize, downloadPosition, mPosition);
                if (!TextUtils.isEmpty(toApkMd5)) {
                    notifyDownloadState(DownloadState.STATE_PATCHING, false);
                } else {
                    notifyDownloadState(DownloadState.STATE_DOWNLOAD_SUCESS, false);

                }
            }
        }
    }

    private static Toast toast = null;

    /**
     * UI线程/非UI线程均可调用 显示 Toast
     */
    public static void showToast(final String str) {
        if (toast == null) {
            try {
                toast = Toast.makeText(BaseApplication.curContext, str,
                        Toast.LENGTH_SHORT);
                toast.show();
            } catch (Exception e) {
                BaseApplication.handler.post(new Runnable() {
                    @Override
                    public void run() {
                        showToast(str);
                    }
                });
            }
        } else {
            toast.cancel();
            toast = null;
            showToast(str);
        }
    }

}
