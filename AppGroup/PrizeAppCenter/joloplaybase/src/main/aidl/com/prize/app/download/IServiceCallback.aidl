package com.prize.app.download;

interface IServiceCallback {    

 void handleDownloadState(int state,int errorCode,String pkgName,int position,boolean isNewDownload);
			
	

}  