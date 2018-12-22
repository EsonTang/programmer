package com.prize.prizethemecenter.bean;

import java.io.Serializable;

/**
 * Created by Administrator on 2016/10/10.
 */
public class DownloadInfo implements Serializable {

    public String  title;                                               //下载的文件名
    public String	downloadUrl;										// 下载的网络地址
    public String	savePath;											// 本地存储地址
    public int		currentState;                                        // 当前状态
    public String	themeID;										   // 主题的ID
    public long		curentProgress;									    // 当前进度
    public long		totaleSize;										   // 应用总大小
    public Runnable	downloadTask;										// 下载的线程
    public String downloadSpeed;                                           //下载的速度
    public int type;                                                    //下载的类型
    public String thumbnail;                                             //缩略图
	public boolean isSelected;                                            //正在使用
    public boolean isChecked;                                           //是否box被选中
    public String md5;                                                  //下载主题或字体的MD5值
    public String wallType;                                            //下载壁纸的type

    public DownloadInfo() {
    }

    public String getWallType() {
        return wallType;
    }

    public void setWallType(String pWallType) {
        wallType = pWallType;
    }
    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }



    public boolean getChecked() {
        return isChecked;
    }

    public void setChecked(boolean pChecked) {
        isChecked = pChecked;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String pTitle) {
        title = pTitle;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String pDownloadUrl) {
        downloadUrl = pDownloadUrl;
    }

    public String getSavePath() {
        return savePath;
    }

    public void setSavePath(String pSavePath) {
        savePath = pSavePath;
    }

    public int getCurrentState() {
        return currentState;
    }

    public void setCurrentState(int pCurrentState) {
        currentState = pCurrentState;
    }

    public String getThemeID() {
        return themeID;
    }

    public void setThemeID(String pThemeID) {
        themeID = pThemeID;
    }

    public long getCurentProgress() {
        return curentProgress;
    }

    public void setCurentProgress(long pCurentProgress) {
        curentProgress = pCurentProgress;
    }

    public long getTotaleSize() {
        return totaleSize;
    }

    public void setTotaleSize(long pTotaleSize) {
        totaleSize = pTotaleSize;
    }

    public Runnable getDownloadTask() {
        return downloadTask;
    }

    public void setDownloadTask(Runnable pDownloadTask) {
        downloadTask = pDownloadTask;
    }

    public String getDownloadSpeed() {
        return downloadSpeed;
    }

    public void setDownloadSpeed(String pDownloadSpeed) {
        downloadSpeed = pDownloadSpeed;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String pThumbnail) {
        thumbnail = pThumbnail;
    }

    public boolean isSelected() {
        return isSelected;
    }
}
