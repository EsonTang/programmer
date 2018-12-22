package com.prize.prizethemecenter.manage;

/**
 * Created by Administrator on 2016/12/26.
 */
public abstract class UIDownLoadListener {
    public void handleDownloadState(int state, int errorCode, int theme_Id) {
        switch (state) {
            case DownloadState.STATE_DOWNLOAD_START_LOADING:
                onStart(theme_Id);
                break;
            case DownloadState.STATE_DOWNLOAD_PAUSE:
                onPause(theme_Id);
                break;
            case DownloadState.STATE_DOWNLOAD_ERROR:
                onError(theme_Id);
                onErrorCode(theme_Id, errorCode);
                break;
            case DownloadState.STATE_DOWNLOAD_SUCESS:
                onFinish(theme_Id);
                break;
            case DownloadState.STATE_DOWNLOAD_WAIT:
                onReady(theme_Id);
                break;
            case DownloadState.STATE_DOWNLOAD_INSTALLED:
                onInstallSucess(theme_Id);
                break;
            case DownloadState.STATE_DOWNLOAD_UPDATE_PROGRESS:
                onUpdateProgress(theme_Id);
                break;
            case DownloadState.STATE_DOWNLOAD_MODE_INIT:
                // 下载模块初始化完成
                onInitDownloadMode();
                return;
            case DownloadState.STATE_DOWNLOAD_CANCEL:
                onStop(theme_Id);
                break;
        }

        onRefreshUI(theme_Id);
    }

    protected void onInstallSucess(int theme_id){

    }

    protected void onUpdateProgress(int theme_Id) {

    }

    protected void onReady(int theme_Id) {

    }

    /**
     * 下载完成
     *
     * @param song_Id
     * @return void
     * @see
     */
    protected void onFinish(int theme_Id) {
    }

    protected void onStart(int theme_Id) {
    }

    protected void onStop(int theme_Id) {
    }

    protected void onPause(int theme_Id) {
    }

    /**
     * 下载模块初始完成
     */
    protected void onInitDownloadMode() {

    }

    /**
     * 下载出错，UI的处理
     *
     * @param song_Id
     */
    protected void onError(int theme_Id) {

    }

    /**
     * 处理错误的code, 可能因为网络超时、导致超时、没有SD卡、没有网络、网络设置不对等原因，实现类如果需要关心错误值,实现该类
     *
     * @param song_Id
     * @param errorCode
     */
    protected void onErrorCode(int theme_Id, int errorCode) {
    }

    /**
     * 刷UI
     *
     * @param song_Id
     */
    public abstract void onRefreshUI(int theme_Id);

}
