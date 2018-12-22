package com.prize.appcenter.ui.datamgr;

/**
 **
 * 用于数据回调接口类，一般都是在界面实现该接口 直接回调数据
 * 
 * @author longbaoxiu
 * @version V1.0
 */
public interface DataManagerCallBack {
	/**
	 * 用于DataManager 回调
	 * 
	 * @param what
	 *            返回标记
	 * @param arg1
	 * @param arg2
	 * @param obj
	 *            返回的数据
	 */
	void onBack(int what, int arg1, int arg2, Object obj);
}
