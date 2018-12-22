package com.prize.app.download;

interface IUIDownLoadListener {   

   void handleDownloadState(int state, int errorCode, String pkgName,
			int position);
	 void onUpdateProgress(String pkgName);


	 void onReady(String pkgName);

	 void onFinish(String pkgName);

	 void onStart(String pkgName);

	 void onStop(String pkgName);

    void onInstallSucess(String pkgName);
	void onPause(String pkgName);
	/**
	 * 下载模块初始完成
	 */
	void onInitDownloadMode();

	/**
	 * 下载出错，UI的处理
	 * 
	 * @param pkgName
	 */
     void onError(String pkgName);

	/**
	 * 处理错误的code, 可能因为网络超时、导致超时、没有SD卡、没有网络、网络设置不对等原因，实现类如果需要关心错误值,实现该类
	 * 
	 * @param pkgName
	 * @param errorCode
	 */
	void onErrorCode(String pkgName, int errorCode);

}  