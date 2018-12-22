package com.prize.app.download;


import android.os.Parcelable;
import android.content.ContentValues;
import com.prize.app.net.datasource.base.AppsItemBean;
import com.prize.statistics.model.ExposureBean;
import com.prize.app.database.beans.HomeRecord;
import com.prize.app.download.IUpdateWatcher;
import com.prize.app.download.IServiceCallback;
import java.util.List;

interface IDownLoadService
{

    /**
	 * 下载开始
	 */
   void downLoadApp(in AppsItemBean itemBean,in boolean isBackground,in int optType,in int action);
   /**
	 * 下载暂停
	 */
   void pauseDownload(in AppsItemBean game, boolean isUserPressed);
   
   int getDownloadTaskSize();
   void cancelDownload(in AppsItemBean game);
   float getDownloadProgress(String pkgName);
   int getDownloadSpeed(String pkgName);
  
   oneway void unregistObserver(in IUpdateWatcher listener);
   oneway void registObserver(in IUpdateWatcher listener);
   
  
   oneway void registerCallback(in IServiceCallback listener);
   oneway void unregisterCallback(in IServiceCallback listener);
   
   
   int getGameAppState(String packageName, String gameCode,int versionCode);
   List<AppsItemBean> getDownloadAppList();
   List<AppsItemBean> getHasDownloadedAppList();
   String getDownloadedAppPageInfo(String packageName);

   boolean hasDownloadingApp();
   boolean hasInstallTask ();
   void removeTask(String packageName);
   void deleteSingle(String packageName);
   AppsItemBean getDownloadGameByPkgname(String pkgName);
   void installedGame(String packageName);
   String getisUpdate_install(String packageName);
   String getBackParam(String packageName);
   //判断是否已经存在手机安装的app
   boolean isInitIntalledAppOk();
   //获取已安装的app信息
   String getPackgeInfoStrFormDB();
   boolean inert2DB(String sysApp);
   void updateInstalledTable(in ContentValues cv);
    //删除已下载的数据表数据
   void deleteAllDownloadedData();
      //清空已完成的下载任务，如果已经清空了，则不做操作
   void clearAllLoadGametasks(in List<AppsItemBean> downloadDatas);
   //3.2 add 及时传输360曝光数据
   void uploadDataNow(in List<ExposureBean> downloadDatas);
     //3.2 add  及时传输360打点数据
   void upload360ClickDataNow(String backParams, String appName, String packageName);
   void deletePushData(String packageName);
   void registSelfPush();
   boolean hasPauseTaskMoreTwo();
}