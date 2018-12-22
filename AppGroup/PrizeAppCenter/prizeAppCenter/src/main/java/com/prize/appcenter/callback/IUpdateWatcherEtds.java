package com.prize.appcenter.callback;

import com.prize.app.net.datasource.base.AppsItemBean;

import java.lang.ref.WeakReference;
import java.util.List;

public  class IUpdateWatcherEtds extends
		com.prize.app.download.IUpdateWatcher.Stub {
	
	private static IUpdateWatcherEtds instance =null;
	private static WeakReference<IUpdateWatcherEtds> instances;
	public static IUpdateWatcherEtds getInstance() {
		instance = new IUpdateWatcherEtds();
		instances=new WeakReference<IUpdateWatcherEtds>(instance);
		return instances.get();
	}
	public  void update(int number, List<String> imgs,
			List<AppsItemBean> listItem){
		if (mCallBack != null) {
			mCallBack.update(number, imgs, listItem);
		}
	};
	
	
	public interface IUpdateWatcherEtdsCallBack {
		void update(int number, List<String> imgs,
				List<AppsItemBean> listItem);
	}

	private IUpdateWatcherEtdsCallBack mCallBack;

	public void setmCallBack(IUpdateWatcherEtdsCallBack mCallBack) {
		this.mCallBack = mCallBack;
	}
}
