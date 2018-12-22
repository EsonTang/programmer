package com.prize.cloudlist;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Calendar;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.google.gson.Gson;
import com.prize.cloudlist.CloudUtils.UpgradeUpdateData;
import com.prize.cloudlist.upgrade.ClientInfo;
import com.prize.cloudlist.upgrade.XXTEAUtil;
import android.app.PackageInstallObserver;

public class UpgradeService
{
	public   String TAG = CloudUtils.TAG;
	public static final boolean IS_DEBUG = CloudUtils.IS_DEBUG;
	private Context mContext;

	private static String HTTP_HEAD = "http://";
	private static String DEFAULT_SERVER = "ad.szprize.cn";
	private static String APK_VERSION_PATH = "/api/package/upgrade/get";

	private UpgradeUpdateData mUpgradeUpdatedata;
	private CloudUtils mCloudUtil;
	private WorkWakelock mWakeLock;
	private Handler mWorkHandler;
	private String  mPackageName;

	public UpgradeService(Context context, WorkWakelock wakelock, Handler workhandler,CloudUtils util,String pkgname)
	{
		mContext = context;
		mWakeLock = wakelock;
		mWorkHandler = workhandler;
		mCloudUtil = util;
		mPackageName = pkgname;
		
		TAG = CloudUtils.TAG+"-"+pkgname;
		Log.d(TAG, "onCreate...");
	}	

	private String getCheckUpgradeDataFilePath()
	{
		return CloudUtils.getCloudListDir() + "/" + mPackageName;
	}
	private String getDownloadApkPath()
	{
		return Environment.getExternalStorageDirectory().getPath() + "/.cloudlist/"+mPackageName+".apk";
	}
	public static void setTestServer(String uri)
	{
		DEFAULT_SERVER = uri;
		Log.d("UpgradeService","setTestServer uri:"+uri);
	}
	
	public void loadData()
	{
		//read config file
		File file = new File("/mnt/sdcard/upgrade.conf");
		if (file.exists())
		{
			try
			{
				FileInputStream is = new FileInputStream(file);
				byte[] buf = new byte[(int) file.length()];
				is.read(buf);
				DEFAULT_SERVER = new String(buf);
				Log.i(TAG, "DEFAULT_SERVER:" + DEFAULT_SERVER);
				is.close();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		
		mUpgradeUpdatedata = mCloudUtil.readUpgradeUpdateDataFile(getCheckUpgradeDataFilePath());
		if(mUpgradeUpdatedata.svrVerNum == 0)
		{
			mUpgradeUpdatedata.svrVerNum = getPackageVersion(mPackageName);
			mCloudUtil.writeUpgradeUpdateDataFile(mUpgradeUpdatedata, getCheckUpgradeDataFilePath());
		}
		Log.d(TAG,"loadData versioncode:"+mUpgradeUpdatedata.svrVerNum);
	}
    public int getPackageVersion(String pkgname)
    {
    	PackageManager pkgmgr = mContext.getPackageManager();
		int installedver = 0;			
		try
		{
			PackageInfo pkginfo = pkgmgr.getPackageInfo(mPackageName, 0);
			installedver = pkginfo.versionCode;				
		}
		catch(Exception e)
		{				
		}
		return installedver;
    }
	////////////////////////////////////////////////////////////////////////////////////////////////////	

	public void startDownloadApkVersion()
	{
		if (IS_DEBUG)
			Log.i(TAG, "lastdownloadupgradelisttime:" + mUpgradeUpdatedata.checkversiontime.year + "-" + mUpgradeUpdatedata.checkversiontime.month + "-"
					+ mUpgradeUpdatedata.checkversiontime.dayofmonth + " " + mUpgradeUpdatedata.checkversiontime.hourofday + ":" + mUpgradeUpdatedata.checkversiontime.min
					+ ":" + mUpgradeUpdatedata.checkversiontime.second);

		//check download status & check install status
		if(UpgradeUpdateData.STATUS_DOWNLOAD.equals(mUpgradeUpdatedata.status)
			||UpgradeUpdateData.STATUS_INSTALL.equals(mUpgradeUpdatedata.status))
		{						
			//check installed version & svrversion
			int installedver = getPackageVersion(mPackageName);
			//go to download
			Log.d(TAG,"svrnum:"+mUpgradeUpdatedata.svrVerNum+",curver:"+installedver+",status:"+mUpgradeUpdatedata.status);
						
			//reset status & version
			mUpgradeUpdatedata.svrVerNum = installedver;
			mUpgradeUpdatedata.status = UpgradeUpdateData.STATUS_CHECKVER;
			mCloudUtil.writeUpgradeUpdateDataFile(mUpgradeUpdatedata, getCheckUpgradeDataFilePath());
		}
		
		
		
		Calendar c = Calendar.getInstance();
		int today = c.get(Calendar.DAY_OF_MONTH);
		if (today == mUpgradeUpdatedata.checkversiontime.dayofmonth)
		{
			if (IS_DEBUG) Log.i(TAG, "today:" + today + " already download upgrade list");
			return;
		}

		// 2.check network
		ConnectivityManager connectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netinfo = connectivityManager.getActiveNetworkInfo();
		if (netinfo == null || !netinfo.isConnected())
		{
			Log.e(TAG, "startDownloadApkVersion no network");
			return;
		}
		
		mWakeLock.addWakeLock();
		// 3.check apk version
		HttpClient httpclient = null;
		String uri = HTTP_HEAD + DEFAULT_SERVER + APK_VERSION_PATH;
		

		String jsonback = null;
		try
		{
			HttpParams httpParams = new BasicHttpParams();
			httpParams.setParameter("charset", HTTP.UTF_8);
			HttpConnectionParams.setConnectionTimeout(httpParams, 8 * 1000);
			HttpConnectionParams.setSoTimeout(httpParams, 8 * 1000);
			httpclient = new DefaultHttpClient(httpParams);

			//package & version
			uri += "?"+"package_name="+mPackageName
					+"&package_version="+mUpgradeUpdatedata.svrVerNum;
			
			HttpGet httpget = new HttpGet(uri);
			httpget.addHeader("charset", HTTP.UTF_8);	
			httpget.addHeader("accept","application/json");
			//httpget.addHeader("KOOBEE", "dido");	
			Log.i(TAG, "startDownloadApkVersion url:" + uri);
			
			//params
			ClientInfo clientinfo = ClientInfo.getInstance(mContext);
			String params = new Gson().toJson(clientinfo);
			Log.d(TAG,"http param:"+params);
			String headParams = XXTEAUtil.getParamsEncypt(params);
			if (IS_DEBUG)Log.d(TAG,"secret params:"+headParams);
			httpget.addHeader("params", headParams);			
			

			HttpResponse response;
			response = httpclient.execute(httpget);
			int code = response.getStatusLine().getStatusCode();
			Log.i(TAG, "startDownloadApkVersion code:" + code);
			if (code == 200)
			{
				jsonback = EntityUtils.toString(response.getEntity());
				if (IS_DEBUG)Log.i(TAG, "startDownloadApkVersion response:\n" + jsonback);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			Log.i(TAG, "startDownloadApkVersion download err:" + e.getMessage());
		}
		finally
		{
			if (httpclient != null)
			{
				httpclient.getConnectionManager().shutdown();
			}
		}
		if (jsonback != null && jsonback.length() > 0)
		{
			String download_url = "";
			int version = 0;
			try
			{
				//{"data":{"name":"","package_name":"com.prize.cloudlist","icon":"http://","version_name":"6.0",
				//"package_version":"6",
				//"release":"","download_url":"http:","apk_size":397387,"status":4,"ext":null}}
				JSONObject jsonret = new JSONObject(jsonback);
				JSONObject datajason = jsonret.getJSONObject("data");				

				String versionstr = datajason.optString("package_version");
				try
				{
					version = Integer.parseInt(versionstr);
				}
				catch(Exception e)
				{
					
				}
				download_url = datajason.optString("download_url");
				
				Log.i(TAG,"download_url:"+download_url);
				Log.i(TAG,"version:"+version);			
			}
			catch (Exception e)
			{
				e.printStackTrace();
				Log.i(TAG, "startDownloadApkVersion parse err:" + e.getMessage());
			}

			// 4. download apk			
			if (mUpgradeUpdatedata.svrVerNum < version && download_url != null && download_url.length() > 0)
			{				
				//DownloadData data = new DownloadData();
				mCurDownloaddata.url = download_url;
				mCurDownloaddata.apkVer = version;
				mCurDownloaddata.localpath = getDownloadApkPath();

				Message msg = mWorkHandler.obtainMessage();
				msg.what = WhiteListDownload.MSG_START_DOWNLOAD_APK;
				msg.obj = this;

				mWakeLock.addWakeLock();
				mWorkHandler.sendMessage(msg);
				
				mUpgradeUpdatedata.downloadurl = download_url;
				mUpgradeUpdatedata.localpath = mCurDownloaddata.localpath;
				mUpgradeUpdatedata.svrVerNum = mCurDownloaddata.apkVer;
				mUpgradeUpdatedata.status = UpgradeUpdateData.STATUS_DOWNLOAD;
			}

		}

		// 5.write download check time & status
		c = Calendar.getInstance();
		mUpgradeUpdatedata.checkversiontime.year = c.get(Calendar.YEAR);
		mUpgradeUpdatedata.checkversiontime.month = c.get(Calendar.MONTH) + 1;
		mUpgradeUpdatedata.checkversiontime.dayofmonth = c.get(Calendar.DAY_OF_MONTH);
		mUpgradeUpdatedata.checkversiontime.hourofday = c.get(Calendar.HOUR_OF_DAY);
		mUpgradeUpdatedata.checkversiontime.min = c.get(Calendar.MINUTE);
		mUpgradeUpdatedata.checkversiontime.second = c.get(Calendar.SECOND);
		mCloudUtil.writeUpgradeUpdateDataFile(mUpgradeUpdatedata, getCheckUpgradeDataFilePath());
	}

	public static class DownloadData
	{
		public String url;
		public int apkVer;
		public String localpath;
	}	
	public DownloadData mCurDownloaddata = new DownloadData();
	//try 3 times,when error
	public void startDownloadApk()
	{
		Log.d(TAG,"startDownloadApk url:"+mCurDownloaddata.url);
			
		//check alread download version == svrversion
		File localfile = new File(mCurDownloaddata.localpath);
		if(localfile.exists())
		{
			PackageManager packageManager = mContext.getPackageManager();  
			PackageInfo packageInfo = packageManager.getPackageArchiveInfo(mCurDownloaddata.localpath, PackageManager.GET_ACTIVITIES);
			//already download last time
			if(packageInfo != null && 
					packageInfo.versionCode == mUpgradeUpdatedata.svrVerNum
					&& mPackageName.equals(packageInfo.packageName))
			{
				Log.d(TAG,"last download file ver:"+packageInfo.versionCode+",localpath:"+mCurDownloaddata.localpath);
				//got to install
				Message msg = mWorkHandler.obtainMessage();
				msg.what = WhiteListDownload.MSG_START_INSTALL_APK;
				msg.obj = this;

				mWakeLock.addWakeLock();
				mWorkHandler.sendMessage(msg);
				
				//change status
				mUpgradeUpdatedata.downloadurl = mCurDownloaddata.url;
				mUpgradeUpdatedata.localpath = mCurDownloaddata.localpath;
				mUpgradeUpdatedata.svrVerNum = mCurDownloaddata.apkVer;
				mUpgradeUpdatedata.status = UpgradeUpdateData.STATUS_INSTALL;
				mCloudUtil.writeUpgradeUpdateDataFile(mUpgradeUpdatedata, getCheckUpgradeDataFilePath());
				return;
			}
		}
		
		
		//download apk
		InputStream is = null;
		FileOutputStream fileout = null;
		boolean isdownloadsuccess = false;
		try
		{
			HttpClient client = new DefaultHttpClient();  
	        HttpGet httpget = new HttpGet(mCurDownloaddata.url);  
	        HttpResponse response = client.execute(httpget);  
	
	        HttpEntity entity = response.getEntity();  
	        is = entity.getContent(); 
	         
	        File file = new File(mCurDownloaddata.localpath);  
	        if(!file.getParentFile().exists())
	        {
	        	file.getParentFile().mkdirs();
	        }
	        if(file.exists())
	        {
	        	file.delete();
	        }
	        
	        fileout = new FileOutputStream(file);  
	        /** 
	         * 根据实际运行效果 设置缓冲区大小 
	         */  
	        byte[] buffer=new byte[4096];  
	        int ch = 0;  
	        while ((ch = is.read(buffer)) != -1)
	        {  
	            fileout.write(buffer,0,ch);  
	        }  
	        is.close(); 
	        is = null;
	        fileout.flush();  
	        fileout.close();
	        fileout = null;
	        
	        isdownloadsuccess = true;
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				if(is != null)is.close();
				if(fileout != null)fileout.close();
			}
			catch(Exception e)
			{
				
			}
		}
		Log.d(TAG,"download apk success:"+isdownloadsuccess);
		//download success 
		if(isdownloadsuccess)
		{			
			//check the apk file is right
			PackageManager packageManager = mContext.getPackageManager();  
			PackageInfo packageInfo = packageManager.getPackageArchiveInfo(mCurDownloaddata.localpath, PackageManager.GET_ACTIVITIES);
			if(packageInfo == null || packageInfo.packageName == null)
			{
				Log.e(TAG,"parse apkfile error:"+mCurDownloaddata.localpath);				
			}
			else
			{
				Log.d(TAG,"parse apkfile version:"+packageInfo.versionCode);
				
				Message msg = mWorkHandler.obtainMessage();
				msg.what = WhiteListDownload.MSG_START_INSTALL_APK;
				msg.obj = this;

				mWakeLock.addWakeLock();
				mWorkHandler.sendMessage(msg);
				
				//change status
				mUpgradeUpdatedata.downloadurl = mCurDownloaddata.url;
				mUpgradeUpdatedata.localpath = mCurDownloaddata.localpath;
				mUpgradeUpdatedata.svrVerNum = mCurDownloaddata.apkVer;
				mUpgradeUpdatedata.status = UpgradeUpdateData.STATUS_INSTALL;
			}			
		}
		
		// 5.write download check time & status
		Calendar c = Calendar.getInstance();		
		mUpgradeUpdatedata.lastdownloadtime.year = c.get(Calendar.YEAR);
		mUpgradeUpdatedata.lastdownloadtime.month = c.get(Calendar.MONTH) + 1;
		mUpgradeUpdatedata.lastdownloadtime.dayofmonth = c.get(Calendar.DAY_OF_MONTH);
		mUpgradeUpdatedata.lastdownloadtime.hourofday = c.get(Calendar.HOUR_OF_DAY);
		mUpgradeUpdatedata.lastdownloadtime.min = c.get(Calendar.MINUTE);
		mUpgradeUpdatedata.lastdownloadtime.second = c.get(Calendar.SECOND);
		mCloudUtil.writeUpgradeUpdateDataFile(mUpgradeUpdatedata, getCheckUpgradeDataFilePath());
	}

	public class MyPackageInstallObserver extends PackageInstallObserver
	{
		public void onPackageInstalled(String basePackageName, int returnCode, String msg, Bundle extras) 
		{
			//returnCode:INSTALL_SUCCEEDED = 1
			Log.d(TAG,"onPackageInstalled package:"+basePackageName+",return:"+returnCode);
			mWakeLock.reduceWakeLock();
	    }
	}
	public void startInstallApk()
	{
		Log.d(TAG,"startInstall apk:"+mCurDownloaddata.localpath);
		File file = new File(mCurDownloaddata.localpath);
		if(!file.exists())
		{
			Log.e(TAG,"file :"+mCurDownloaddata.localpath+" is not exists!");
			return;
		}		
				
		
		//write install time
		Calendar c = Calendar.getInstance();
		mUpgradeUpdatedata.lastinstalltime.year = c.get(Calendar.YEAR);
		mUpgradeUpdatedata.lastinstalltime.month = c.get(Calendar.MONTH) + 1;
		mUpgradeUpdatedata.lastinstalltime.dayofmonth = c.get(Calendar.DAY_OF_MONTH);
		mUpgradeUpdatedata.lastinstalltime.hourofday = c.get(Calendar.HOUR_OF_DAY);
		mUpgradeUpdatedata.lastinstalltime.min = c.get(Calendar.MINUTE);
		mUpgradeUpdatedata.lastinstalltime.second = c.get(Calendar.SECOND);
		mCloudUtil.writeUpgradeUpdateDataFile(mUpgradeUpdatedata, getCheckUpgradeDataFilePath());		
		
		//install apk
		try
		{
			PackageManager pkgmgr = mContext.getPackageManager();
			Uri uri = Uri.fromFile(file);
			MyPackageInstallObserver mObserver = new MyPackageInstallObserver();
			
			//keep active,send broadcast to system,then system will send broadcast com.prize.keepactive.recv
			Intent intent = new Intent("com.prize.keepactive.send");
			mContext.sendBroadcast(intent);			
			
			
			mWakeLock.addWakeLock();
			pkgmgr.installPackage(uri,mObserver,PackageManager.INSTALL_REPLACE_EXISTING,mContext.getPackageName());			
		}
		catch(Exception e)
		{
			mWakeLock.reduceWakeLock();
			e.printStackTrace();
		}
		
		//do not change status tail to next check version time		
		
	}
}
