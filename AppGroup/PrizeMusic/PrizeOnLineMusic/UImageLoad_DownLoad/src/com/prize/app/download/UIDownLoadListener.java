package com.prize.app.download;

/**
 * 同UI交互
 *
 * @author prize
 * 
 */
public abstract class UIDownLoadListener {
	public void handleDownloadState(int state, int errorCode, int song_Id) {
		switch (state) {
		case DownloadState.STATE_DOWNLOAD_START_LOADING:
			onStart(song_Id);
			break;
		case DownloadState.STATE_DOWNLOAD_PAUSE:
			onPause(song_Id);
			break;
		case DownloadState.STATE_DOWNLOAD_ERROR:
			onError(song_Id);
			onErrorCode(song_Id, errorCode);
			break;
		case DownloadState.STATE_DOWNLOAD_SUCESS:
			onFinish(song_Id);
			break;
		case DownloadState.STATE_DOWNLOAD_WAIT:
			onReady(song_Id);
			break;
		// case DownloadState.STATE_DOWNLOAD_INSTALLED:
		// onInstallSucess(song_Id);
		// break;
		case DownloadState.STATE_DOWNLOAD_UPDATE_PROGRESS:
			onUpdateProgress(song_Id);
			break;
		case DownloadState.STATE_DOWNLOAD_MODE_INIT:
			// 下载模块初始化完成
			onInitDownloadMode();
			return;
		case DownloadState.STATE_DOWNLOAD_CANCEL:
			onStop(song_Id);
			break;
		}

		onRefreshUI(song_Id);
	}

	protected void onUpdateProgress(int song_Id) {

	}

	protected void onReady(int song_Id) {

	}

	/**
	 * 下载完成
	 * 
	 * @param song_Id
	 * @return void
	 * @see
	 */
	protected void onFinish(int song_Id) {
	}

	protected void onStart(int song_Id) {
	}

	protected void onStop(int song_Id) {
	}

	protected void onPause(int song_Id) {
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
	protected void onError(int song_Id) {

	}

	/**
	 * 处理错误的code, 可能因为网络超时、导致超时、没有SD卡、没有网络、网络设置不对等原因，实现类如果需要关心错误值,实现该类
	 * 
	 * @param song_Id
	 * @param errorCode
	 */
	protected void onErrorCode(int song_Id, int errorCode) {
	}

	/**
	 * 刷UI
	 * 
	 * @param song_Id
	 */
	public abstract void onRefreshUI(int song_Id);

}
