package com.prize.app.download;

import android.os.RemoteException;

import java.lang.ref.WeakReference;

public class IUIDownLoadListenerImp extends IServiceCallback.Stub {
	private static WeakReference<IUIDownLoadListenerImp> instances;
	public static IUIDownLoadListenerImp getInstance() {
		instances=new WeakReference<IUIDownLoadListenerImp>(new IUIDownLoadListenerImp());
		return instances.get();
	}

	@Override
	public void handleDownloadState(int state, int errorCode, String pkgName,
			int position,boolean isNewDownload) throws RemoteException {
		onRefreshUI(pkgName, state);
		if (mCallBack != null) {
			mCallBack.callBack(pkgName, state,isNewDownload);
		}

	}

	/**
	 * 刷UI
	 * 
	 * @param pkgName 包名
	 * @param state app的下载状态
	 */
	public void onRefreshUI(String pkgName, int state) {

	}

	public interface IUIDownLoadCallBack {
		void callBack(String pkgName, int state,boolean isNewDownload);
	}

	private IUIDownLoadCallBack mCallBack;

	public void setmCallBack(IUIDownLoadCallBack mCallBack) {
		this.mCallBack = mCallBack;
	}

}
