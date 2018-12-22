package com.prize.cloudlist;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Calendar;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.prize.cloudlist.CloudUtils.CloudUpdateData;
import com.prize.cloudlist.CloudUtils.ProviderWakeupItem;
import com.prize.cloudlist.CloudUtils.ReleateWakeupItem;

public class WhiteListDownload
{
	private static String DEFAULT_SERVER = "newapi.szprize.cn";
	private static String HTTP_HEAD = "http://";
	private static String VERSION_PATH = "/Os/Os/getVersion";
	private static String WHITELIST_DATA_PATH = "/Os/Os/getWhiteNames?os_type=";

	public static final int OS_TYPE_MSG = 1;
	public static final int OS_TYPE_INSTALL = 2;
	public static final int OS_TYPE_PUREBKGROUND = 3;
	public static final int OS_TYPE_NOTIFICATION = 4;
	public static final int OS_TYPE_FLOATWINDOW = 5;
	public static final int OS_TYPE_AUTOLAUNCH = 6;
	public static final int OS_TYPE_NETFORBADE = 7;
	public static final int OS_TYPE_SLEEPNET = 8;
	public static final int OS_TYPE_WAKEUP = 9;
	public static final int OS_TYPE_BLOCKACTIVITY = 10;
	public static final int OS_TYPE_DOZEWHITE = 11;
	public static final int OS_TYPE_SCREENOFFKILL = 12;
	public static final int OS_TYPE_SMARTKILL = 13;
	public static final int OS_TYPE_PROVIDER_WAKEUP = 14;

	// run in the handlethread
	public void init(Context context, WorkWakelock wakelock)
	{
		mContext = context;
		mHandler = new WorkHandler(Looper.myLooper());
		mWakeLock = wakelock;

		mUpdater = new WhitsListUpdateData();
		mUpdater.init(mContext);

		// init upgradeservice
		mUpgradeList.clear();
		UpgradeService cloudlistUpgrade = new UpgradeService(mContext, mWakeLock, mHandler, mCloudUtil, "com.prize.cloudlist");
		mUpgradeList.add(cloudlistUpgrade);

		// UpgradeService securityUpgrade = new
		// UpgradeService(this,mWakeLock,mHandler,mCloudUtil,"com.prize.prizesecurity");
		// mUpgradeList.add(securityUpgrade);

		loadData();
	}

	public void setTestServer(String uri)
	{
		DEFAULT_SERVER = uri;
		Log.d(TAG, "start test url:" + DEFAULT_SERVER);
	}

	public void startTest()
	{
		if(mHandler == null)return;
		mCloudUpdatedata.lastdownloadwhitelisttime.dayofmonth = 0;
		mCloudUpdatedata.autolaunchver = "";
		mCloudUpdatedata.blockactivityver = "";
		mCloudUpdatedata.dozewhitelistversion = "";
		mCloudUpdatedata.floatwindowver = "";
		mCloudUpdatedata.installwhitelistversion = "";
		mCloudUpdatedata.msgwhitelistversion = "";
		mCloudUpdatedata.netforbadever = "";
		mCloudUpdatedata.notificationver = "";
		mCloudUpdatedata.purebkgroundver = "";
		mCloudUpdatedata.sleepnetver = "";
		mCloudUpdatedata.wakeupver = "";
		mCloudUpdatedata.screenoffkillversion = "";
		mCloudUpdatedata.smartkillversion = "";
		mCloudUpdatedata.providerwakeupversion = "";
		// for whitelist		
		//mHandler.sendEmptyMessage(MSG_START_DOWNLOAD_WHITELIST_VERSION);
		doStartCheck();
	}
	public void setUpgradeTestServer(String uri)
	{
		UpgradeService.setTestServer(uri);
	}
	public void startUpgradeTest()
	{
		if(mHandler == null)return;
		
		for (int i = 0; i < mUpgradeList.size(); i++)
		{			

			Message msg = mHandler.obtainMessage();
			msg.obj = mUpgradeList.get(i);
			msg.what = MSG_START_DOWNLOAD_UPGRADE_VERSION;
			mHandler.sendMessage(msg);
		}
	}

	private void loadData()
	{
		File file = new File("/mnt/sdcard/cloudlist.conf");
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
		mCloudUpdatedata = mCloudUtil.readCloudUpdateDataFile(CloudUtils.getCheckRootDataFilePath(mContext));
		
		for (int i = 0; i < mUpgradeList.size(); i++)
		{
			mUpgradeList.get(i).loadData();
		}
	}

	public static final String TAG = CloudUtils.TAG;
	public static boolean IS_DEBUG = true;

	private Context mContext;
	private WorkHandler mHandler = null;

	private CloudUpdateData mCurWhiteListVersion = new CloudUpdateData();
	private CloudUpdateData mCloudUpdatedata;
	private WhitsListUpdateData mUpdater;

	private ArrayList<UpgradeService> mUpgradeList = new ArrayList<UpgradeService>();

	private CloudUtils mCloudUtil = new CloudUtils();
	private WorkWakelock mWakeLock = null;

	public static final int MSG_START_DOWNLOAD_WHITELIST_VERSION = 1;
	public static final int MSG_START_DOWNLOAD_UPGRADE_VERSION = 2;

	public static final int MSG_START_DOWNLOAD_WHITELIST_SMS = 101;
	public static final int MSG_START_DOWNLOAD_WHITELIST_INSTALL = 102;
	public static final int MSG_START_DOWNLOAD_PUREBKGROUND = 103;
	public static final int MSG_START_DOWNLOAD_NOTIFICATION = 104;
	public static final int MSG_START_DOWNLOAD_FLOATWINDOW = 105;
	public static final int MSG_START_DOWNLOAD_AUTOLAUNCH = 106;
	public static final int MSG_START_DOWNLOAD_NETFORBADE = 107;
	public static final int MSG_START_DOWNLOAD_RELEATEWAKEUP = 108;
	public static final int MSG_START_DOWNLOAD_SLEEPNET = 109;
	public static final int MSG_START_DOWNLOAD_BLOCKACTIVITY = 110;
	public static final int MSG_START_DOWNLOAD_DOZEWHITE = 111;
	public static final int MSG_START_DOWNLOAD_SCREENOFFKILL = 112;
	public static final int MSG_START_DOWNLOAD_SMARTKILL = 113;
	public static final int MSG_START_DOWNLOAD_PROVIDERWAKEUP = 114;
	

	public static final int MSG_START_DOWNLOAD_APK = 201;
	public static final int MSG_START_INSTALL_APK = 202;

	public class WorkHandler extends Handler
	{
		public WorkHandler(Looper looper)
		{
			super(looper);
		}

		@Override
		public void handleMessage(Message msg)
		{
			switch (msg.what)
			{
			case MSG_START_DOWNLOAD_WHITELIST_VERSION:
				startDownloadWhiteVersion();
				mWakeLock.reduceWakeLock();
				break;

			case MSG_START_DOWNLOAD_WHITELIST_SMS:
				startDownloadWhiteListSMS();
				mWakeLock.reduceWakeLock();
				break;
			case MSG_START_DOWNLOAD_WHITELIST_INSTALL:
				startDownloadWhiteListInstall();
				mWakeLock.reduceWakeLock();
				break;

			case MSG_START_DOWNLOAD_PUREBKGROUND:
				startDownloadPurebkground();
				mWakeLock.reduceWakeLock();
				break;
			case MSG_START_DOWNLOAD_NOTIFICATION:
				startDownloadNotification();
				mWakeLock.reduceWakeLock();
				break;
			case MSG_START_DOWNLOAD_FLOATWINDOW:
				startDownloadFloatWindow();
				mWakeLock.reduceWakeLock();
				break;
			case MSG_START_DOWNLOAD_AUTOLAUNCH:
				startDownloadAutoLaunch();
				mWakeLock.reduceWakeLock();
				break;
			case MSG_START_DOWNLOAD_NETFORBADE:
				startDownloadNetForbade();
				mWakeLock.reduceWakeLock();
				break;
			case MSG_START_DOWNLOAD_RELEATEWAKEUP:
				startDownloadReleateWakeup();
				mWakeLock.reduceWakeLock();
				break;
			case MSG_START_DOWNLOAD_SLEEPNET:
				startDownloadSleepNet();
				mWakeLock.reduceWakeLock();
				break;
			case MSG_START_DOWNLOAD_BLOCKACTIVITY:
				startDownloadBlockActivity();
				mWakeLock.reduceWakeLock();
				break;
			case MSG_START_DOWNLOAD_DOZEWHITE:
				startDownloadDozeWhitelist();
				mWakeLock.reduceWakeLock();
				break;
			case MSG_START_DOWNLOAD_SCREENOFFKILL:
				startDownloadScreenOffKillWhite();
				mWakeLock.reduceWakeLock();
				break;
			case MSG_START_DOWNLOAD_SMARTKILL:
				startDownloadSmartKillWhite();
				mWakeLock.reduceWakeLock();
				break;
			case MSG_START_DOWNLOAD_PROVIDERWAKEUP:
				startDownloadProviderWakeupWhite();
				mWakeLock.reduceWakeLock();
				break;
				
				
			case MSG_START_DOWNLOAD_UPGRADE_VERSION:
				{
					UpgradeService mUpgradeService = (UpgradeService) msg.obj;
					mUpgradeService.startDownloadApkVersion();
					mWakeLock.reduceWakeLock();
				}
				break;
			case MSG_START_DOWNLOAD_APK:
				{
					UpgradeService mUpgradeService = (UpgradeService) msg.obj;
					mUpgradeService.startDownloadApk();
					mWakeLock.reduceWakeLock();
				}
				break;
			case MSG_START_INSTALL_APK:
				{
					UpgradeService mUpgradeService = (UpgradeService) msg.obj;
					mUpgradeService.startInstallApk();
					mWakeLock.reduceWakeLock();
				}
				break;
			}
		}
	}

	// ////////////////////////////////////////////////////////////////////
	public String getDataFromServer(String uri, String funcTag)
	{
		String jsonback = null;
		HttpClient httpclient = null;
		try
		{
			HttpParams httpParams = new BasicHttpParams();
			httpParams.setParameter("charset", HTTP.UTF_8);
			HttpConnectionParams.setConnectionTimeout(httpParams, 8 * 1000);
			HttpConnectionParams.setSoTimeout(httpParams, 8 * 1000);
			httpclient = new DefaultHttpClient(httpParams);

			HttpGet httpget = new HttpGet(uri);
			httpget.addHeader("charset", HTTP.UTF_8);
			httpget.addHeader("KOOBEE", "dido");
			String imei = CloudUtils.getImei();
			if(imei != null)
			{
				Log.d(TAG,"imei:"+imei);
				httpget.addHeader("imei", imei);
			}

			HttpResponse response;
			response = httpclient.execute(httpget);
			int code = response.getStatusLine().getStatusCode();
			Log.i(TAG, funcTag + " code:" + code);
			if (code == 200)
			{
				jsonback = EntityUtils.toString(response.getEntity());
				Log.i(TAG, funcTag + ":\n" + jsonback);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			Log.i(TAG, funcTag + " download err:" + e.getMessage());
		}
		finally
		{
			if (httpclient != null)
			{
				httpclient.getConnectionManager().shutdown();
			}
		}
		return jsonback;
	}

	public boolean isNetWorkOk(Context context)
	{
		ConnectivityManager connectivityManager = (ConnectivityManager) (context.getSystemService(Context.CONNECTIVITY_SERVICE));
		NetworkInfo netinfo = connectivityManager.getActiveNetworkInfo();
		if (netinfo == null || !netinfo.isConnected()) { return false; }
		return true;
	}

	// ////////////////////////////////////////////////////////////////////////////
	public void doStartCheck()
	{
		if (mHandler == null) return;
		
		mHandler.sendEmptyMessage(MSG_START_DOWNLOAD_WHITELIST_VERSION);
		for (int i = 0; i < mUpgradeList.size(); i++)
		{
			Message msg = mHandler.obtainMessage();
			msg.obj = mUpgradeList.get(i);
			msg.what = MSG_START_DOWNLOAD_UPGRADE_VERSION;
			mHandler.sendMessage(msg);
		}
	}

	// /////////////////////////////////////////////////////////////////////////////
	private void startDownloadWhiteVersion()
	{
		// 1.check whether check today
		if (IS_DEBUG)
			Log.i(TAG, "lastdownloadwhitelisttime:" + mCloudUpdatedata.lastdownloadwhitelisttime.year + "-" + mCloudUpdatedata.lastdownloadwhitelisttime.month + "-"
					+ mCloudUpdatedata.lastdownloadwhitelisttime.dayofmonth + " " + mCloudUpdatedata.lastdownloadwhitelisttime.hourofday + ":" + mCloudUpdatedata.lastdownloadwhitelisttime.min + ":"
					+ mCloudUpdatedata.lastdownloadwhitelisttime.second);
		Calendar c = Calendar.getInstance();
		int today = c.get(Calendar.DAY_OF_MONTH);
		if (today == mCloudUpdatedata.lastdownloadwhitelisttime.dayofmonth)
		{
			if (IS_DEBUG) Log.i(TAG, "today:" + today + " already download white list");
			return;
		}

		// 2.check network
		if (!isNetWorkOk(mContext))
		{
			Log.e(TAG, "startDownloadWhiteVersion no network");
			return;
		}

		mWakeLock.addWakeLock();
		/*
		 * if(mBatteryLevel < 20) {
		 * Log.e(TAG,"startDownloadWhiteVersion mBatteryLevel:"+mBatteryLevel);
		 * return; }
		 */

		// 3.start download white version
		String uri = HTTP_HEAD + DEFAULT_SERVER + VERSION_PATH;
		Log.i(TAG, "startDownloadWhiteVersion VERSION_PATH:" +VERSION_PATH);
		Log.i(TAG, "startDownloadWhiteVersion url:" + uri);
		String jsonback = getDataFromServer(uri, "startDownloadWhiteVersion");

		// 4.parse jsondata
		if (jsonback != null && jsonback.length() > 0)
		{
			try
			{
				// {"code":"0","msg":"ok","version":[{"system":"os_sms_ver","value":"39"},
				// {"system":"os_install_ver","value":"7"},
				// {"system":"os_pure_ver","value":"2"},
				// {"system":"os_notice_ver","value":"2"},
				// {"system":"os_window_ver","value":"2"},
				// {"system":"os_launcher_ver","value":"2"},
				// {"system":"os_network_ver","value":"2"},
				// {"system":"os_sleepnet_ver","value":"2"},
				// {"system":"os_associate_ver","value":"3"},
				// {"system":"os_intercept_ver","value":"5"},
				// {"system":"os_doze_ver","value":"0"},
				// {"system":"os_clear_ver","value":"2"},
				// {"system":"os_protect_ver","value":"0"},
				// {"system":"os_provider_ver","value":"1"}]}
				JSONObject jsonret = new JSONObject(jsonback);
				JSONArray version = jsonret.getJSONArray("version");
				CloudUpdateData downloadVersion = new CloudUpdateData();

				String code = jsonret.optString("code");
				if (code != null && code.equals("0"))
				{
					for (int i = 0; i < version.length(); i++)
					{
						JSONObject veritem = version.getJSONObject(i);
						String type = veritem.optString("system");
						if ("os_sms_ver".equals(type))
						{
							downloadVersion.msgwhitelistversion = veritem.optString("value");
						}
						else if ("os_install_ver".equals(type))
						{
							downloadVersion.installwhitelistversion = veritem.optString("value");
						}
						else if ("os_pure_ver".equals(type))
						{
							downloadVersion.purebkgroundver = veritem.optString("value");
						}
						else if ("os_notice_ver".equals(type))
						{
							downloadVersion.notificationver = veritem.optString("value");
						}
						else if ("os_window_ver".equals(type))
						{
							downloadVersion.floatwindowver = veritem.optString("value");
						}
						else if ("os_launcher_ver".equals(type))
						{
							downloadVersion.autolaunchver = veritem.optString("value");
						}
						else if ("os_network_ver".equals(type))
						{
							downloadVersion.netforbadever = veritem.optString("value");
						}
						else if ("os_sleepnet_ver".equals(type))
						{
							downloadVersion.sleepnetver = veritem.optString("value");
						}
						else if ("os_associate_ver".equals(type))
						{
							downloadVersion.wakeupver = veritem.optString("value");
						}
						else if ("os_intercept_ver".equals(type))
						{
							downloadVersion.blockactivityver = veritem.optString("value");
						}
						else if ("os_doze_ver".equals(type))
						{
							downloadVersion.dozewhitelistversion = veritem.optString("value");
						}
						else if("os_clear_ver".equals(type))
						{
							downloadVersion.screenoffkillversion = veritem.optString("value");
						}
						else if("os_protect_ver".equals(type))
						{
							downloadVersion.smartkillversion = veritem.optString("value");
						}
						else if("os_provider_ver".equals(type))
						{
							downloadVersion.providerwakeupversion = veritem.optString("value");
						}
					}
					Log.i(TAG, "download version sms:" + downloadVersion.msgwhitelistversion + ",cur:" + mCloudUpdatedata.msgwhitelistversion);
					Log.i(TAG, "download version install:" + downloadVersion.installwhitelistversion + ",cur:" + mCloudUpdatedata.installwhitelistversion);
					Log.i(TAG, "download version purebkground:" + downloadVersion.purebkgroundver + ",cur:" + mCloudUpdatedata.purebkgroundver);
					Log.i(TAG, "download version notification:" + downloadVersion.notificationver + ",cur:" + mCloudUpdatedata.notificationver);
					Log.i(TAG, "download version floatwindow:" + downloadVersion.floatwindowver + ",cur:" + mCloudUpdatedata.floatwindowver);
					Log.i(TAG, "download version autolaunch:" + downloadVersion.autolaunchver + ",cur:" + mCloudUpdatedata.autolaunchver);
					Log.i(TAG, "download version netforbade:" + downloadVersion.netforbadever + ",cur:" + mCloudUpdatedata.netforbadever);
					Log.i(TAG, "download version wakeup:" + downloadVersion.wakeupver + ",cur:" + mCloudUpdatedata.wakeupver);
					Log.i(TAG, "download version sleepnet:" + downloadVersion.sleepnetver + ",cur:" + mCloudUpdatedata.sleepnetver);
					Log.i(TAG, "download version blockactivity:" + downloadVersion.blockactivityver + ",cur:" + mCloudUpdatedata.blockactivityver);
					Log.i(TAG, "download version dozewhite:" + downloadVersion.dozewhitelistversion + ",cur:" + mCloudUpdatedata.dozewhitelistversion);
					Log.i(TAG, "download version screenofkillwhite:" + downloadVersion.screenoffkillversion + ",cur:" + mCloudUpdatedata.screenoffkillversion);
					Log.i(TAG, "download version smartkillwhite:" + downloadVersion.smartkillversion + ",cur:" + mCloudUpdatedata.smartkillversion);
					Log.i(TAG, "download version providerwakeup:" + downloadVersion.providerwakeupversion + ",cur:" + mCloudUpdatedata.providerwakeupversion);
					boolean isUpdated = false;
					mCurWhiteListVersion = downloadVersion;
					if (mCloudUpdatedata.msgwhitelistversion == null || !mCloudUpdatedata.msgwhitelistversion.equals(downloadVersion.msgwhitelistversion))
					{
						// if version change start download whitelist
						mWakeLock.addWakeLock();
						mHandler.sendEmptyMessage(MSG_START_DOWNLOAD_WHITELIST_SMS);
						isUpdated = true;
					}
					if (mCloudUpdatedata.installwhitelistversion == null || !mCloudUpdatedata.installwhitelistversion.equals(downloadVersion.installwhitelistversion))
					{
						// if version change start download whitelist
						mWakeLock.addWakeLock();
						mHandler.sendEmptyMessage(MSG_START_DOWNLOAD_WHITELIST_INSTALL);
						isUpdated = true;
					}
					if (mCloudUpdatedata.purebkgroundver == null || !mCloudUpdatedata.purebkgroundver.equals(downloadVersion.purebkgroundver))
					{
						// if version change start download whitelist
						mWakeLock.addWakeLock();
						mHandler.sendEmptyMessage(MSG_START_DOWNLOAD_PUREBKGROUND);
						isUpdated = true;
					}
					if (mCloudUpdatedata.notificationver == null || !mCloudUpdatedata.notificationver.equals(downloadVersion.notificationver))
					{
						// if version change start download whitelist
						mWakeLock.addWakeLock();
						mHandler.sendEmptyMessage(MSG_START_DOWNLOAD_NOTIFICATION);
						isUpdated = true;
					}
					if (mCloudUpdatedata.floatwindowver == null || !mCloudUpdatedata.floatwindowver.equals(downloadVersion.floatwindowver))
					{
						// if version change start download whitelist
						mWakeLock.addWakeLock();
						mHandler.sendEmptyMessage(MSG_START_DOWNLOAD_FLOATWINDOW);
						isUpdated = true;
					}
					if (mCloudUpdatedata.autolaunchver == null || !mCloudUpdatedata.autolaunchver.equals(downloadVersion.autolaunchver))
					{
						// if version change start download whitelist
						mWakeLock.addWakeLock();
						mHandler.sendEmptyMessage(MSG_START_DOWNLOAD_AUTOLAUNCH);
						isUpdated = true;
					}
					if (mCloudUpdatedata.netforbadever == null || !mCloudUpdatedata.netforbadever.equals(downloadVersion.netforbadever))
					{
						// if version change start download whitelist
						mWakeLock.addWakeLock();
						mHandler.sendEmptyMessage(MSG_START_DOWNLOAD_NETFORBADE);
						isUpdated = true;
					}
					if (mCloudUpdatedata.wakeupver == null || !mCloudUpdatedata.wakeupver.equals(downloadVersion.wakeupver))
					{
						// if version change start download whitelist
						mWakeLock.addWakeLock();
						mHandler.sendEmptyMessage(MSG_START_DOWNLOAD_RELEATEWAKEUP);
						isUpdated = true;
					}
					if (mCloudUpdatedata.sleepnetver == null || !mCloudUpdatedata.sleepnetver.equals(downloadVersion.sleepnetver))
					{
						// if version change start download whitelist
						mWakeLock.addWakeLock();
						mHandler.sendEmptyMessage(MSG_START_DOWNLOAD_SLEEPNET);
						isUpdated = true;
					}
					if (mCloudUpdatedata.blockactivityver == null || !mCloudUpdatedata.blockactivityver.equals(downloadVersion.blockactivityver))
					{
						// if version change start download whitelist
						mWakeLock.addWakeLock();
						mHandler.sendEmptyMessage(MSG_START_DOWNLOAD_BLOCKACTIVITY);
						isUpdated = true;
					}
					if (mCloudUpdatedata.dozewhitelistversion == null || !mCloudUpdatedata.dozewhitelistversion.equals(downloadVersion.dozewhitelistversion))
					{
						// if version change start download whitelist
						mWakeLock.addWakeLock();
						mHandler.sendEmptyMessage(MSG_START_DOWNLOAD_DOZEWHITE);
						isUpdated = true;
					}
					if (mCloudUpdatedata.screenoffkillversion == null || !mCloudUpdatedata.screenoffkillversion.equals(downloadVersion.screenoffkillversion))
					{
						// if version change start download whitelist
						mWakeLock.addWakeLock();
						mHandler.sendEmptyMessage(MSG_START_DOWNLOAD_SCREENOFFKILL);
						isUpdated = true;
					}
					if (mCloudUpdatedata.smartkillversion == null || !mCloudUpdatedata.smartkillversion.equals(downloadVersion.smartkillversion))
					{
						// if version change start download whitelist
						mWakeLock.addWakeLock();
						mHandler.sendEmptyMessage(MSG_START_DOWNLOAD_SMARTKILL);
						isUpdated = true;
					}
					if (mCloudUpdatedata.providerwakeupversion == null || !mCloudUpdatedata.providerwakeupversion.equals(downloadVersion.providerwakeupversion))
					{
						// if version change start download whitelist
						mWakeLock.addWakeLock();
						mHandler.sendEmptyMessage(MSG_START_DOWNLOAD_PROVIDERWAKEUP);
						isUpdated = true;
					}
					if (isUpdated)
					{
						// mHandler.sendEmptyMessage(MSG_UPDATE_DATA);
					}
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
				Log.i(TAG, "startDownloadWhiteVersion parse err:" + e.getMessage());
			}

		}

		// 5.write download check time
		c = Calendar.getInstance();
		mCloudUpdatedata.lastdownloadwhitelisttime.year = c.get(Calendar.YEAR);
		mCloudUpdatedata.lastdownloadwhitelisttime.month = c.get(Calendar.MONTH) + 1;
		mCloudUpdatedata.lastdownloadwhitelisttime.dayofmonth = c.get(Calendar.DAY_OF_MONTH);
		mCloudUpdatedata.lastdownloadwhitelisttime.hourofday = c.get(Calendar.HOUR_OF_DAY);
		mCloudUpdatedata.lastdownloadwhitelisttime.min = c.get(Calendar.MINUTE);
		mCloudUpdatedata.lastdownloadwhitelisttime.second = c.get(Calendar.SECOND);
		mCloudUtil.writeCloudUpdateDataFile(mCloudUpdatedata, CloudUtils.getCheckRootDataFilePath(mContext));
	}

	private void startDownloadWhiteListSMS()
	{
		Log.i(TAG, "startDownloadWhiteListSMS......");

		// 1.check network
		if (!isNetWorkOk(mContext))
		{
			Log.e(TAG, "startDownloadWhiteListSMS no network");
			return;
		}

		// 2. download whitelist
		String uri = HTTP_HEAD + DEFAULT_SERVER + WHITELIST_DATA_PATH + OS_TYPE_MSG;
		Log.i(TAG, "startDownloadWhiteListSMS url:" + uri);
		boolean issuccess = false;
		String jsonback = getDataFromServer(uri, "startDownloadWhiteListSMS");

		// 3.parse json data
		ArrayList<String> whitelistdata = new ArrayList<String>();
		// parse white list data
		// {"code":"0","msg":"ok","total":"3","list":[{"os_type":"1","package_name":"com.sohu.inputmethod.sogouss","status":"0"},{"os_type":"1","package_name":"com.sohu.inputmethod.sogoutest","status":"0"},{"os_type":"1","package_name":"com.tencent.portfolio.test","status":"0"}]}
		if (jsonback != null)
		{
			try
			{
				JSONObject jsonret = new JSONObject(jsonback);
				JSONArray whitelist = jsonret.getJSONArray("list");
				for (int i = 0; i < whitelist.length(); i++)
				{
					JSONObject item = whitelist.getJSONObject(i);
					String pkgname = item.optString("package_name");
					if (pkgname == null || pkgname.length() <= 0) continue;

					whitelistdata.add(pkgname);
					Log.i(TAG, "pkgname:" + pkgname);
				}
				issuccess = true;
			}
			catch (Exception e)
			{
				e.printStackTrace();
				Log.i(TAG, "startDownloadWhiteListSMS parse err:" + e.getMessage());
			}
		}
		// 4.save white list to db & updatedata
		if(issuccess)
		{
			mUpdater.updateWhiteListSmsData(whitelistdata);
		}

		// 4.save white list version
		if (issuccess)
		{
			mCloudUpdatedata.msgwhitelistversion = mCurWhiteListVersion.msgwhitelistversion;
			mCloudUtil.writeCloudUpdateDataFile(mCloudUpdatedata, CloudUtils.getCheckRootDataFilePath(mContext));
		}
	}

	private void startDownloadWhiteListInstall()
	{
		Log.i(TAG, "startDownloadWhiteListInstall......");

		// 1.check network
		if (!isNetWorkOk(mContext))
		{
			Log.e(TAG, "startDownloadWhiteListInstall no network");
			return;
		}

		// 2. download whitelist
		String uri = HTTP_HEAD + DEFAULT_SERVER + WHITELIST_DATA_PATH + OS_TYPE_INSTALL;
		Log.i(TAG, "startDownloadWhiteListInstall url:" + uri);
		String jsonback = getDataFromServer(uri, "startDownloadWhiteListInstall");
		boolean issuccess = false;

		// 3.parse jason
		ArrayList<String> whitelistdata = new ArrayList<String>();
		// parse white list data
		// :{"code":"0","msg":"ok","total":"3","list":[{"name":"\u817e\u8baf\u624b\u673a\u7ba1\u5bb6","package_name":"com.sohu.inputmethod.sogouss"},{"name":"360\u5f71\u89c6\u5927\u5168","package_name":"com.sohu.inputmethod.sogoutest"}]}"
		if (jsonback != null)
		{
			try
			{
				JSONObject jsonret = new JSONObject(jsonback);
				JSONArray whitelist = jsonret.getJSONArray("list");
				for (int i = 0; i < whitelist.length(); i++)
				{
					JSONObject item = whitelist.getJSONObject(i);
					String pkgname = item.optString("package_name");
					if (pkgname == null || pkgname.length() <= 0) continue;

					whitelistdata.add(pkgname);
					Log.i(TAG, "pkgname:" + pkgname);
				}
				issuccess = true;
			}
			catch (Exception e)
			{
				e.printStackTrace();
				Log.i(TAG, "startDownloadWhiteListInstall parse err:" + e.getMessage());
			}
		}
		// 3.save white list to db & update data
		if(issuccess)
		{
			mUpdater.updateWhiteListInstallData(whitelistdata);
		}

		// 4.save white list version
		if (issuccess)
		{
			mCloudUpdatedata.installwhitelistversion = mCurWhiteListVersion.installwhitelistversion;
			mCloudUtil.writeCloudUpdateDataFile(mCloudUpdatedata, CloudUtils.getCheckRootDataFilePath(mContext));
		}
	}

	private void startDownloadPurebkground()
	{
		Log.i(TAG, "startDownloadPurebkground......");

		// 1.check network
		if (!isNetWorkOk(mContext))
		{
			Log.e(TAG, "startDownloadPurebkground no network");
			return;
		}

		// 2. download whitelist
		String uri = HTTP_HEAD + DEFAULT_SERVER + WHITELIST_DATA_PATH + OS_TYPE_PUREBKGROUND;
		Log.i(TAG, "startDownloadPurebkground url:" + uri);
		String jsonback = getDataFromServer(uri, "startDownloadPurebkground");
		boolean issuccess = false;

		// 3.parse jason
		ArrayList<String> hidelist = new ArrayList<String>();
		ArrayList<String> defenablelist = new ArrayList<String>();
		ArrayList<String> notkilllist = new ArrayList<String>();
		// parse white list data
		// {"code":"0","msg":"ok","total":"3","list":[{"os_type":"1","package_name":"com.sohu.inputmethod.sogouss","status":"0"},{"os_type":"1","package_name":"com.sohu.inputmethod.sogoutest","status":"0"},{"os_type":"1","package_name":"com.tencent.portfolio.test","status":"0"}]}
		if (jsonback != null)
		{
			try
			{
				JSONObject jsonret = new JSONObject(jsonback);
				JSONArray whitelist = jsonret.getJSONArray("list");
				for (int i = 0; i < whitelist.length(); i++)
				{
					JSONObject item = whitelist.getJSONObject(i);
					String pkgname = item.optString("package_name");
					if (pkgname == null || pkgname.length() <= 0) continue;

					int status = item.getInt("status");
					if (status == 2)
					{
						hidelist.add(pkgname);
					}
					else if (status == 3)
					{
						defenablelist.add(pkgname);
					}
					else if (status == 4)
					{
						notkilllist.add(pkgname);
					}

					Log.i(TAG, "pkgname:" + pkgname + ",status:" + status);
				}
				issuccess = true;
			}
			catch (Exception e)
			{
				e.printStackTrace();
				Log.i(TAG, "startDownloadPurebkground parse err:" + e.getMessage());
			}
		}

		// 4.save white list to db & update data
		if(issuccess)
		{
			mUpdater.updateWhiteListPureBkgroundData(hidelist, defenablelist, notkilllist);
		}

		// 5.save white list version
		if (issuccess)
		{
			mCloudUpdatedata.purebkgroundver = mCurWhiteListVersion.purebkgroundver;
			mCloudUtil.writeCloudUpdateDataFile(mCloudUpdatedata, CloudUtils.getCheckRootDataFilePath(mContext));
		}
	}

	private void startDownloadNotification()
	{
		Log.i(TAG, "startDownloadNotification......");

		// 1.check network
		if (!isNetWorkOk(mContext))
		{
			Log.e(TAG, "startDownloadNotification no network");
			return;
		}

		// 2. download whitelist
		String uri = HTTP_HEAD + DEFAULT_SERVER + WHITELIST_DATA_PATH + OS_TYPE_NOTIFICATION;
		Log.i(TAG, "startDownloadNotification url:" + uri);
		String jsonback = getDataFromServer(uri, "startDownloadNotification");
		boolean issuccess = false;

		// 3.parse jason
		ArrayList<String> hidelist = new ArrayList<String>();
		ArrayList<String> defenablelist = new ArrayList<String>();
		// parse white list data
		// {"code":"0","msg":"ok","total":"3","list":[{"os_type":"1","package_name":"com.sohu.inputmethod.sogouss","status":"0"},{"os_type":"1","package_name":"com.sohu.inputmethod.sogoutest","status":"0"},{"os_type":"1","package_name":"com.tencent.portfolio.test","status":"0"}]}
		if (jsonback != null)
		{
			try
			{
				JSONObject jsonret = new JSONObject(jsonback);
				JSONArray whitelist = jsonret.getJSONArray("list");
				for (int i = 0; i < whitelist.length(); i++)
				{
					JSONObject item = whitelist.getJSONObject(i);
					String pkgname = item.optString("package_name");
					if (pkgname == null || pkgname.length() <= 0) continue;

					int status = item.optInt("status");
					if (status == 2)
					{
						hidelist.add(pkgname);
					}
					else if (status == 3)
					{
						defenablelist.add(pkgname);
					}
					Log.i(TAG, "pkgname:" + pkgname);
				}
				issuccess = true;
			}
			catch (Exception e)
			{
				e.printStackTrace();
				Log.i(TAG, "startDownloadNotification parse err:" + e.getMessage());
			}
		}

		// 4.save white list to db & update data
		if(issuccess)
		{
			mUpdater.updateWhiteListNotificationData(hidelist, defenablelist);
		}

		// 5.save white list version
		if (issuccess)
		{
			mCloudUpdatedata.notificationver = mCurWhiteListVersion.notificationver;
			mCloudUtil.writeCloudUpdateDataFile(mCloudUpdatedata, CloudUtils.getCheckRootDataFilePath(mContext));
		}
	}

	private void startDownloadFloatWindow()
	{
		Log.i(TAG, "startDownloadFloatWindow......");

		// 1.check network
		if (!isNetWorkOk(mContext))
		{
			Log.e(TAG, "startDownloadFloatWindow no network");
			return;
		}

		// 2. download whitelist
		String uri = HTTP_HEAD + DEFAULT_SERVER + WHITELIST_DATA_PATH + OS_TYPE_FLOATWINDOW;
		Log.i(TAG, "startDownloadFloatWindow url:" + uri);
		String jsonback = getDataFromServer(uri, "startDownloadFloatWindow");
		boolean issuccess = false;

		// 3.parse jason
		ArrayList<String> enablelist = new ArrayList<String>();
		// parse white list data
		// {"code":"0","msg":"ok","total":"3","list":[{"os_type":"1","package_name":"com.sohu.inputmethod.sogouss","status":"0"},{"os_type":"1","package_name":"com.sohu.inputmethod.sogoutest","status":"0"},{"os_type":"1","package_name":"com.tencent.portfolio.test","status":"0"}]}
		if (jsonback != null)
		{
			try
			{
				JSONObject jsonret = new JSONObject(jsonback);
				JSONArray whitelist = jsonret.getJSONArray("list");
				for (int i = 0; i < whitelist.length(); i++)
				{
					JSONObject item = whitelist.getJSONObject(i);
					String pkgname = item.optString("package_name");
					if (pkgname == null || pkgname.length() <= 0) continue;

					enablelist.add(pkgname);
					Log.i(TAG, "pkgname:" + pkgname);
				}
				issuccess = true;
			}
			catch (Exception e)
			{
				e.printStackTrace();
				Log.i(TAG, "startDownloadFloatWindow parse err:" + e.getMessage());
			}
		}
		// 3.save white list to db & update data
		if(issuccess)
		{
			mUpdater.updateWhiteListFloatwindowData(enablelist);
		}

		// 4.save white list version
		if (issuccess)
		{
			mCloudUpdatedata.floatwindowver = mCurWhiteListVersion.floatwindowver;
			mCloudUtil.writeCloudUpdateDataFile(mCloudUpdatedata, CloudUtils.getCheckRootDataFilePath(mContext));
		}
	}

	private void startDownloadAutoLaunch()
	{
		Log.i(TAG, "startDownloadAutoLaunch......");

		// 1.check network
		if (!isNetWorkOk(mContext))
		{
			Log.e(TAG, "startDownloadAutoLaunch no network");
			return;
		}

		// 2. download whitelist
		String uri = HTTP_HEAD + DEFAULT_SERVER + WHITELIST_DATA_PATH + OS_TYPE_AUTOLAUNCH;
		Log.i(TAG, "startDownloadAutoLaunch url:" + uri);
		String jsonback = getDataFromServer(uri, "startDownloadAutoLaunch");
		boolean issuccess = false;

		// 3.parse jason
		ArrayList<String> defenablelist = new ArrayList<String>();
		// parse white list data
		// {"code":"0","msg":"ok","total":"3","list":[{"os_type":"1","package_name":"com.sohu.inputmethod.sogouss","status":"0"},{"os_type":"1","package_name":"com.sohu.inputmethod.sogoutest","status":"0"},{"os_type":"1","package_name":"com.tencent.portfolio.test","status":"0"}]}
		if (jsonback != null)
		{
			try
			{
				JSONObject jsonret = new JSONObject(jsonback);
				JSONArray whitelist = jsonret.getJSONArray("list");
				for (int i = 0; i < whitelist.length(); i++)
				{
					JSONObject item = whitelist.getJSONObject(i);
					String pkgname = item.optString("package_name");
					if (pkgname == null || pkgname.length() <= 0) continue;

					defenablelist.add(pkgname);
					Log.i(TAG, "pkgname:" + pkgname);
				}
				issuccess = true;
			}
			catch (Exception e)
			{
				e.printStackTrace();
				Log.i(TAG, "startDownloadAutoLaunch parse err:" + e.getMessage());
			}
		}
		// 4.save white list
		if(issuccess)
		{
			mUpdater.updateWhiteListAutoLaunchData(defenablelist);
		}
		// 5.save white list version
		if (issuccess)
		{
			mCloudUpdatedata.autolaunchver = mCurWhiteListVersion.autolaunchver;
			mCloudUtil.writeCloudUpdateDataFile(mCloudUpdatedata, CloudUtils.getCheckRootDataFilePath(mContext));
		}
	}

	private void startDownloadNetForbade()
	{
		Log.i(TAG, "startDownloadNetForbade......");

		// 1.check network
		if (!isNetWorkOk(mContext))
		{
			Log.e(TAG, "startDownloadNetForbade no network");
			return;
		}

		// 2. download whitelist
		String uri = HTTP_HEAD + DEFAULT_SERVER + WHITELIST_DATA_PATH + OS_TYPE_NETFORBADE;
		Log.i(TAG, "startDownloadNetForbade url:" + uri);
		String jsonback = getDataFromServer(uri, "startDownloadNetForbade");
		boolean issuccess = false;

		// 3.parse jason
		ArrayList<String> disablelist = new ArrayList<String>();
		// parse white list data
		// {"code":"0","msg":"ok","total":"3","list":[{"os_type":"1","package_name":"com.sohu.inputmethod.sogouss","status":"0"},{"os_type":"1","package_name":"com.sohu.inputmethod.sogoutest","status":"0"},{"os_type":"1","package_name":"com.tencent.portfolio.test","status":"0"}]}
		if (jsonback != null)
		{
			try
			{
				JSONObject jsonret = new JSONObject(jsonback);
				JSONArray whitelist = jsonret.getJSONArray("list");
				for (int i = 0; i < whitelist.length(); i++)
				{
					JSONObject item = whitelist.getJSONObject(i);
					String pkgname = item.optString("package_name");
					if (pkgname == null || pkgname.length() <= 0) continue;

					disablelist.add(pkgname);
					Log.i(TAG, "pkgname:" + pkgname);
				}
				issuccess = true;
			}
			catch (Exception e)
			{
				e.printStackTrace();
				Log.i(TAG, "startDownloadNetForbade parse err:" + e.getMessage());
			}
		}
		// 4.save white list
		if(issuccess)
		{
			mUpdater.updateWhiteListNetForbadeData(disablelist);
		}

		// 5.save white list version
		if (issuccess)
		{
			mCloudUpdatedata.netforbadever = mCurWhiteListVersion.netforbadever;
			mCloudUtil.writeCloudUpdateDataFile(mCloudUpdatedata, CloudUtils.getCheckRootDataFilePath(mContext));
		}
	}

	private void startDownloadReleateWakeup()
	{
		Log.i(TAG, "startDownloadReleateWakeup......");

		// 1.check network
		if (!isNetWorkOk(mContext))
		{
			Log.e(TAG, "startDownloadReleateWakeup no network");
			return;
		}

		// 2. download whitelist
		String uri = HTTP_HEAD + DEFAULT_SERVER + WHITELIST_DATA_PATH + OS_TYPE_WAKEUP;
		Log.i(TAG, "startDownloadReleateWakeup url:" + uri);
		String jsonback = getDataFromServer(uri, "startDownloadReleateWakeup");
		boolean issuccess = false;

		// 3.parse jason
		ArrayList<ReleateWakeupItem> whitelistdata = new ArrayList<ReleateWakeupItem>();
		// parse white list data
		// {"code":"0","msg":"ok","total":"3","list":[{"package_name":"test","action":"testaction","class_name":"999","caller_pkg":"999"},
		// {"package_name":"qqq222","action":"qqqff22","class_name":"111","caller_pkg":"qqqf"},
		// {"package_name":"oooo1","action":"ooooo2","class_name":"oooo3","caller_pkg":"oooo43"}]}

		if (jsonback != null)
		{
			try
			{
				JSONObject jsonret = new JSONObject(jsonback);
				JSONArray whitelist = jsonret.getJSONArray("list");
				for (int i = 0; i < whitelist.length(); i++)
				{
					JSONObject item = whitelist.getJSONObject(i);
					String pkgname = item.optString("package_name");
					String action = item.optString("action");
					String classname = item.optString("class_name");
					String callerpkg = item.optString("caller_pkg");
					if ((pkgname == null || pkgname.length() <= 0) && (action == null || action.length() <= 0) && (classname == null || classname.length() <= 0))
					{
						continue;
					}
					int state = item.optInt("status");
					ReleateWakeupItem wakeupitem = new ReleateWakeupItem(pkgname, classname, action, callerpkg, state);
					whitelistdata.add(wakeupitem);

					Log.i(TAG, "pkgname:" + pkgname + ",action:" + action + ",classname:" + classname + ",callerpkg:" + callerpkg + ",state:" + state);
				}
				issuccess = true;
			}
			catch (Exception e)
			{
				e.printStackTrace();
				Log.i(TAG, "startDownloadReleateWakeup parse err:" + e.getMessage());
			}
		}
		// 4.save white list
		if(issuccess)
		{
			mUpdater.updateWhiteListReleateWakeupData(whitelistdata);
		}

		// 5.save white list version
		if (issuccess)
		{
			mCloudUpdatedata.wakeupver = mCurWhiteListVersion.wakeupver;
			mCloudUtil.writeCloudUpdateDataFile(mCloudUpdatedata, CloudUtils.getCheckRootDataFilePath(mContext));
		}
	}

	private void startDownloadSleepNet()
	{
		Log.i(TAG, "startDownloadSleepNet......");

		// 1.check network
		if (!isNetWorkOk(mContext))
		{
			Log.e(TAG, "startDownloadSleepNet no network");
			return;
		}

		// 2. download whitelist
		String uri = HTTP_HEAD + DEFAULT_SERVER + WHITELIST_DATA_PATH + OS_TYPE_SLEEPNET;
		Log.i(TAG, "startDownloadSleepNet url:" + uri);
		String jsonback = getDataFromServer(uri, "startDownloadSleepNet");
		boolean issuccess = false;

		// 3.parse json
		ArrayList<String> whitelistdata = new ArrayList<String>();
		// parse white list data
		// {"code":"0","msg":"ok","total":"3","list":[{"os_type":"1","package_name":"com.sohu.inputmethod.sogouss","status":"0"},{"os_type":"1","package_name":"com.sohu.inputmethod.sogoutest","status":"0"},{"os_type":"1","package_name":"com.tencent.portfolio.test","status":"0"}]}
		if (jsonback != null)
		{
			try
			{
				JSONObject jsonret = new JSONObject(jsonback);
				JSONArray whitelist = jsonret.getJSONArray("list");
				for (int i = 0; i < whitelist.length(); i++)
				{
					JSONObject item = whitelist.getJSONObject(i);
					String pkgname = item.optString("package_name");
					if (pkgname == null || pkgname.length() <= 0) continue;

					whitelistdata.add(pkgname);
					Log.i(TAG, "pkgname:" + pkgname);
				}
				issuccess = true;
			}
			catch (Exception e)
			{
				e.printStackTrace();
				Log.i(TAG, "startDownloadSleepNet parse err:" + e.getMessage());
			}
		}
		// 4.save white list
		if(issuccess)
		{
			mUpdater.updateWhiteListSleepNetData(whitelistdata);
		}

		// 5.save white list version
		if (issuccess)
		{
			mCloudUpdatedata.sleepnetver = mCurWhiteListVersion.sleepnetver;
			mCloudUtil.writeCloudUpdateDataFile(mCloudUpdatedata, CloudUtils.getCheckRootDataFilePath(mContext));
		}
	}

	private void startDownloadBlockActivity()
	{
		Log.i(TAG, "startDownloadBlockActivity......");

		// 1.check network
		if (!isNetWorkOk(mContext))
		{
			Log.e(TAG, "startDownloadBlockActivity no network");
			return;
		}

		// 2. download whitelist
		String uri = HTTP_HEAD + DEFAULT_SERVER + WHITELIST_DATA_PATH + OS_TYPE_BLOCKACTIVITY;
		Log.i(TAG, "startDownloadBlockActivity url:" + uri);
		String jsonback = getDataFromServer(uri, "startDownloadBlockActivity");
		boolean issuccess = false;

		// 3.parse jason
		ArrayList<String> whitelistdata = new ArrayList<String>();
		// parse white list data
		// {"code":"0","msg":"ok","total":"3","list":[{"class_name":"888"},{"class_name":"888"},{"class_name":"999"}]}
		if (jsonback != null)
		{
			try
			{
				JSONObject jsonret = new JSONObject(jsonback);
				JSONArray whitelist = jsonret.getJSONArray("list");
				for (int i = 0; i < whitelist.length(); i++)
				{
					JSONObject item = whitelist.getJSONObject(i);
					String classname = item.optString("class_name");
					whitelistdata.add(classname);
					Log.i(TAG, "classname:" + classname);
				}
				issuccess = true;
			}
			catch (Exception e)
			{
				e.printStackTrace();
				Log.i(TAG, "startDownloadBlockActivity parse err:" + e.getMessage());
			}
		}
		// 4.save white list
		if(issuccess)
		{
			mUpdater.updateWhiteListBlockActivity(whitelistdata);
		}

		// 4.save white list version
		if (issuccess)
		{
			mCloudUpdatedata.blockactivityver = mCurWhiteListVersion.blockactivityver;
			mCloudUtil.writeCloudUpdateDataFile(mCloudUpdatedata, CloudUtils.getCheckRootDataFilePath(mContext));
		}
	}

	private void startDownloadDozeWhitelist()
	{
		Log.i(TAG, "startDownloadDozeWhitelist......");

		// 1.check network
		if (!isNetWorkOk(mContext))
		{
			Log.e(TAG, "startDownloadDozeWhitelist no network");
			return;
		}

		// 2. download whitelist
		String uri = HTTP_HEAD + DEFAULT_SERVER + WHITELIST_DATA_PATH + OS_TYPE_DOZEWHITE;
		Log.i(TAG, "startDownloadDozeWhitelist url:" + uri);
		String jsonback = getDataFromServer(uri, "startDownloadDozeWhitelist");
		boolean issuccess = false;

		// 3.parse jason
		ArrayList<String> defenablelist = new ArrayList<String>();
		// parse white list data
		// {"code":"0","msg":"ok","total":"3","list":[{"os_type":"1","package_name":"com.sohu.inputmethod.sogouss","status":"0"},{"os_type":"1","package_name":"com.sohu.inputmethod.sogoutest","status":"0"},{"os_type":"1","package_name":"com.tencent.portfolio.test","status":"0"}]}
		if (jsonback != null)
		{
			try
			{
				JSONObject jsonret = new JSONObject(jsonback);
				JSONArray whitelist = jsonret.getJSONArray("list");
				for (int i = 0; i < whitelist.length(); i++)
				{
					JSONObject item = whitelist.getJSONObject(i);
					String pkgname = item.optString("package_name");
					if (pkgname == null || pkgname.length() <= 0) continue;

					defenablelist.add(pkgname);
					Log.i(TAG, "pkgname:" + pkgname);
				}
				issuccess = true;
			}
			catch (Exception e)
			{
				e.printStackTrace();
				Log.i(TAG, "startDownloadAutoLaunch parse err:" + e.getMessage());
			}
		}
		// 4.save white list
		if(issuccess)
		{
			mUpdater.updateWhiteListDozeWhiteData(defenablelist);
		}

		// 5.save white list version
		if (issuccess)
		{
			mCloudUpdatedata.dozewhitelistversion = mCurWhiteListVersion.dozewhitelistversion;
			mCloudUtil.writeCloudUpdateDataFile(mCloudUpdatedata, CloudUtils.getCheckRootDataFilePath(mContext));
		}
	}
	private void startDownloadScreenOffKillWhite()
	{
		Log.i(TAG, "startDownloadScreenOffKillWhite......");

		// 1.check network
		if (!isNetWorkOk(mContext))
		{
			Log.e(TAG, "startDownloadScreenOffKillWhite no network");
			return;
		}

		// 2. download whitelist
		String uri = HTTP_HEAD + DEFAULT_SERVER + WHITELIST_DATA_PATH + OS_TYPE_SCREENOFFKILL;
		Log.i(TAG, "startDownloadScreenOffKillWhite url:" + uri);
		String jsonback = getDataFromServer(uri, "startDownloadScreenOffKillWhite");
		boolean issuccess = false;

		// 3.parse jason
		ArrayList<String> defenablelist = new ArrayList<String>();
		// parse white list data
		//{"code":"0","msg":"ok","total":"18",
		//"list":[
		//{"type":"CalledKillFilterList","package_name":"com.adups.fota.sysoper"},
		//{"type":"CalledKillFilterList","package_name":"com.prize.cloudlist"},
		
		//{"type":"DozeModeFilterList","package_name":"com.mediatek.mtklogger"},
		//{"type":"DozeModeFilterList","package_name":"com.android.dialer"},
		
		//{"type":"ForceStopFilterList","package_name":"com.tencent.mobileqq"},
		//{"type":"ForceStopFilterList","package_name":"com.tencent.mm"},
		
		//{"type":"MorningKillFilterList","package_name":"com.prize.tts"},
		//{"type":"MorningKillFilterList","package_name":"com.prize.rootcheck"},
		
		//{"type":"PackageFilterList","package_name":"com.android.phone123"},
		//{"type":"PackageFilterList","package_name":"com.adups.fota"},
		
		//{"type":"ProtectProcessList","package_name":"com.adups.fota"},
		//{"type":"ProtectProcessList","package_name":"com.prize.cloudlist123"},
		
		//{"type":"ProtectServiceInfo","package_name":"com.prize.weather#com.prize.weather:widgetservice#com.prize.weather:locationservice"},
		//{"type":"ProtectServiceInfo","package_name":"com.android.deskclock#com.android.deskclock"},
		
		//{"type":"ScreenOffKillFilterList","package_name":"com.tencent.mm"},
		//{"type":"ScreenOffKillFilterList","package_name":"com.tencent.mobileqq"},		
		//{"type":"ScreenOffKillFilterList","package_name":"com.android.deskclock123"},
		//{"type":"ScreenOffKillFilterList","package_name":"com.prize.sysresmon123"}]}
		ArrayList<String> allkillwhitelist = new ArrayList<String>();
		ArrayList<String> mornkillwhitelist = new ArrayList<String>();
		ArrayList<String> screenoffkillwhitelist = new ArrayList<String>();		
		ArrayList<String> calledkilwhitellist = new ArrayList<String>();			
		ArrayList<String> protectprocesswhitelist = new ArrayList<String>();
		ArrayList<String> protectservicewhitelist = new ArrayList<String>();
		ArrayList<String> forcestopwhitelist = new ArrayList<String>();	
		ArrayList<String> dozemodewhitelist = new ArrayList<String>();
		ArrayList<String> musicwhitelist = new ArrayList<String>();
		ArrayList<String> mapwhitelist = new ArrayList<String>();
		if (jsonback != null)
		{
			try
			{
				JSONObject jsonret = new JSONObject(jsonback);
				JSONArray whitelist = jsonret.getJSONArray("list");
				for (int i = 0; i < whitelist.length(); i++)
				{
					JSONObject item = whitelist.getJSONObject(i);
					String pkgname = item.optString("package_name");
					String type = item.optString("type");
					if (pkgname == null || pkgname.length() <= 0 || type == null || type.length()<=0) continue;
					
					if (type.equals("CalledKillFilterList"))
					{
						calledkilwhitellist.add(pkgname);
					}
					else if (type.equals("DozeModeFilterList"))
					{
						dozemodewhitelist.add(pkgname);
					}
					else if (type.equals("ForceStopFilterList"))
					{
						forcestopwhitelist.add(pkgname);
					}
					else if (type.equals("MorningKillFilterList"))
					{
						mornkillwhitelist.add(pkgname);
					}
					else if (type.equals("PackageFilterList"))
					{
						allkillwhitelist.add(pkgname);
					}
					else if (type.equals("ProtectProcessList"))
					{
						protectprocesswhitelist.add(pkgname);
					}
					else if (type.equals("ProtectServiceInfo"))
					{
						protectservicewhitelist.add(pkgname);
					}
					else if (type.equals("ScreenOffKillFilterList"))
					{
						screenoffkillwhitelist.add(pkgname);
					}
					else if (type.equals("MusicFilterList"))
					{
						musicwhitelist.add(pkgname);
					}
					else if (type.equals("MapFilterList"))
					{
						mapwhitelist.add(pkgname);
					}

					Log.i(TAG, "pkgname:" + pkgname + ",type:" + type);
				}
				issuccess = true;
			}
			catch (Exception e)
			{
				e.printStackTrace();
				Log.i(TAG, "startDownloadScreenOffKillWhite parse err:" + e.getMessage());
			}
		}
		// 4.save white list
		if(issuccess)
		{
			issuccess = mUpdater.updateScreenOffKillWhiteData(allkillwhitelist, mornkillwhitelist, screenoffkillwhitelist, 
				calledkilwhitellist, protectprocesswhitelist, protectservicewhitelist, 
				forcestopwhitelist, dozemodewhitelist,musicwhitelist,mapwhitelist);
		}

		// 5.save white list version
		if (issuccess)
		{
			mCloudUpdatedata.screenoffkillversion = mCurWhiteListVersion.screenoffkillversion;
			mCloudUtil.writeCloudUpdateDataFile(mCloudUpdatedata, CloudUtils.getCheckRootDataFilePath(mContext));
		}
	}
	private void startDownloadSmartKillWhite()
	{
		Log.i(TAG, "startDownloadSmartKillWhite......");

		// 1.check network
		if (!isNetWorkOk(mContext))
		{
			Log.e(TAG, "startDownloadSmartKillWhite no network");
			return;
		}

		// 2. download whitelist
		String uri = HTTP_HEAD + DEFAULT_SERVER + WHITELIST_DATA_PATH + OS_TYPE_SMARTKILL;
		Log.i(TAG, "startDownloadSmartKillWhite url:" + uri);
		String jsonback = getDataFromServer(uri, "startDownloadSmartKillWhite");
		boolean issuccess = false;

		// 3.parse jason
		ArrayList<String> protectprocesslist = new ArrayList<String>();
		ArrayList<String> protectpackagelist = new ArrayList<String>();
		// parse white list data
		// {"code":"0","msg":"ok","total":"4","list":[{"type":"ProtectPackage","package_name":"com.prize.cloudlist"},{"type":"ProtectPackage","package_name":"com.android.bluetooth"},{"type":"ProtectProcess","package_name":"com.prize.rootcheck:remote"},{"type":"ProtectProcess","package_name":"22222222222"}]}
		if (jsonback != null)
		{
			try
			{
				JSONObject jsonret = new JSONObject(jsonback);
				JSONArray whitelist = jsonret.getJSONArray("list");
				for (int i = 0; i < whitelist.length(); i++)
				{
					JSONObject item = whitelist.getJSONObject(i);
					String pkgname = item.optString("package_name");
					String type = item.optString("type");
					if (pkgname == null || pkgname.length() <= 0 || type == null || type.length()<=0) continue;

					if (type.equals("ProtectPackage"))
					{
						protectpackagelist.add(pkgname);
					}
					else if (type.equals("ProtectProcess"))
					{
						protectprocesslist.add(pkgname);
					}
					
					Log.i(TAG, "pkgname:" + pkgname+",type:"+type);
				}
				issuccess = true;
			}
			catch (Exception e)
			{
				e.printStackTrace();
				Log.i(TAG, "startDownloadSmartKillWhite parse err:" + e.getMessage());
			}
		}
		// 4.save white list
		if(issuccess)
		{
			issuccess = mUpdater.updateSmartKillWhiteData(protectprocesslist, protectpackagelist);
		}

		// 5.save white list version
		if (issuccess)
		{
			mCloudUpdatedata.smartkillversion = mCurWhiteListVersion.smartkillversion;
			mCloudUtil.writeCloudUpdateDataFile(mCloudUpdatedata, CloudUtils.getCheckRootDataFilePath(mContext));
		}
	}
	private void startDownloadProviderWakeupWhite()
	{
		Log.i(TAG, "startDownloadProviderWakeupWhite......");

		// 1.check network
		if (!isNetWorkOk(mContext))
		{
			Log.e(TAG, "startDownloadProviderWakeupWhite no network");
			return;
		}

		// 2. download whitelist
		String uri = HTTP_HEAD + DEFAULT_SERVER + WHITELIST_DATA_PATH + OS_TYPE_PROVIDER_WAKEUP;
		Log.i(TAG, "startDownloadProviderWakeupWhite url:" + uri);
		String jsonback = getDataFromServer(uri, "startDownloadProviderWakeupWhite");
		boolean issuccess = false;

		// 3.parse jason
		ArrayList<ProviderWakeupItem> providerWakeList = new ArrayList<ProviderWakeupItem>();
		// parse white list data
		// {"code":"0","msg":"ok","total":"4","list":
		//[{"package_name":"1111111111","class_name":"1222222222","caller_pkg":"33333333333333","status":"2"},
		//{"package_name":"2222222","class_name":"3333333","caller_pkg":"444444444444","status":"0"},
		//{"package_name":"11111","class_name":"22222","caller_pkg":"33333333","status":"1"},
		//{"package_name":"43","class_name":"5","caller_pkg":"6","status":"1"}]}
		// State that indicates start request(activity/broadcast/service/provider) can be granted.
        //public static final int STATE_GRANTED = 0;
        // State that indicates start request(activity/broadcast/service/provider) should be denied.
        //public static final int STATE_DENIED = 1; 
        // State that indicates if the host process has already been running, then start request can 
        // be granted. If not, start request will be denied.
        //public static final int STATE_DENIED_IF_NOT_RUNNING = 2;
		
		if (jsonback != null)
		{
			try
			{
				JSONObject jsonret = new JSONObject(jsonback);
				JSONArray whitelist = jsonret.getJSONArray("list");
				for (int i = 0; i < whitelist.length(); i++)
				{
					JSONObject item = whitelist.getJSONObject(i);
					String pkgname = item.optString("package_name");
					String classname = item.optString("class_name");
					String callpkg = item.optString("caller_pkg");
					int state = item.optInt("status");
					
					if ((pkgname == null || pkgname.length() <= 0) && (callpkg == null || callpkg.length() <= 0) && (classname == null || classname.length() <= 0))
					{
						continue;
					}					
					
					ProviderWakeupItem wakeupitem = new ProviderWakeupItem(pkgname, classname, callpkg, state);
					providerWakeList.add(wakeupitem);
					
					Log.i(TAG, "wakeupitem:" + wakeupitem);
				}
				issuccess = true;
			}
			catch (Exception e)
			{
				e.printStackTrace();
				Log.i(TAG, "startDownloadProviderWakeupWhite parse err:" + e.getMessage());
			}
		}
		// 4.save white list
		if(issuccess)
		{
			mUpdater.updateWhiteListProviderWakeupData(providerWakeList);
		}

		// 5.save white list version
		if (issuccess)
		{
			mCloudUpdatedata.providerwakeupversion = mCurWhiteListVersion.providerwakeupversion;
			mCloudUtil.writeCloudUpdateDataFile(mCloudUpdatedata, CloudUtils.getCheckRootDataFilePath(mContext));
		}
	}
	// /////////////////////////////////////////////////////////////
}
