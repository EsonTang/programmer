package com.prize.app.download;

import android.os.RemoteException;


public abstract class IUIDownLoadListenerImp extends IUIDownLoadListener.Stub {

	@Override
	public void handleDownloadState(int state, int errorCode, String pkgName,
			int position) throws RemoteException {
//		switch (state) {
//		case DownloadState.STATE_DOWNLOAD_START_LOADING:
//			onStart(pkgName);
//			break;
//		case DownloadState.STATE_DOWNLOAD_PAUSE:
//			onPause(pkgName);
//			break;
//		case DownloadState.STATE_DOWNLOAD_ERROR:
//			onError(pkgName);
//			onErrorCode(pkgName, errorCode);
//			break;
//		case DownloadState.STATE_DOWNLOAD_SUCESS:
//			onFinish(pkgName);
//			break;
//		case DownloadState.STATE_DOWNLOAD_WAIT:
//			onReady(pkgName);
//			break;
//		case DownloadState.STATE_DOWNLOAD_INSTALLED:
//			onInstallSucess(pkgName);
//			break;
//		case DownloadState.STATE_DOWNLOAD_UPDATE_PROGRESS:
//			onUpdateProgress(pkgName);
//			break;
//		case DownloadState.STATE_DOWNLOAD_MODE_INIT:
//			// 下载模块初始化完成
//			onInitDownloadMode();
//			return;
//		case DownloadState.STATE_DOWNLOAD_CANCEL:
//			onStop(pkgName);
//			break;
//		}

		onRefreshUI(pkgName, position);

	}

	@Override
	public void onUpdateProgress(String pkgName) throws RemoteException {

	}

	@Override
	public void onReady(String pkgName) throws RemoteException {

	}

	@Override
	public void onFinish(String pkgName) throws RemoteException {

	}

	@Override
	public void onStart(String pkgName) throws RemoteException {

	}

	@Override
	public void onStop(String pkgName) throws RemoteException {
		// TODO Auto-generated method stub

	}

	@Override
	public void onInstallSucess(String pkgName) throws RemoteException {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPause(String pkgName) throws RemoteException {
		// TODO Auto-generated method stub

	}

	@Override
	public void onInitDownloadMode() throws RemoteException {
		// TODO Auto-generated method stub

	}

	@Override
	public void onError(String pkgName) throws RemoteException {
		// TODO Auto-generated method stub

	}

	@Override
	public void onErrorCode(String pkgName, int errorCode)
			throws RemoteException {
		// TODO Auto-generated method stub

	}
	

	/**
	 * 刷UI
	 * 
	 * @param pkgName
	 */
	public abstract void onRefreshUI(String pkgName, int position);

}
