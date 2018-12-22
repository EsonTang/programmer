package com.prize.left.page.util;

import java.io.File;

import org.xutils.common.util.LogUtil;

import android.app.AlarmManager;
import android.app.DownloadManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Utils;

import com.android.gallery3d.util.LogUtils;
import com.android.launcher3.Launcher;
import com.lqsoft.lqtheme.OLThemeNotification;
import com.prize.left.page.bean.AppInfoBean;
import com.prize.left.page.response.UpgradeResponse;

/**
 * @author Administrator
 *
 */
public class DownloadModel {

	private Context mContext;

	private DownloadChangeObserver downloadObserver;

	private DownloadManager downloadManager;
	
	

	private long downloadId = 0;

	private DownloadManagerPro downloadManagerPro;
	private long TIME=2*60 * 1000;//灭屏幕 两分钟 去检测安装更新

	private AppInfoBean mAppBean;

	private static DownloadModel mInstace = null;

	class DownloadChangeObserver extends ContentObserver {

		public DownloadChangeObserver(Handler handler) {
			super(handler);
		}

		@Override
		public void onChange(boolean selfChange) {
		/*	Log.i("zhouerlong", "selfChange");

			int[] bytesAndStatus = downloadManagerPro.getBytesAndStatus(downloadId);

			Log.i("zhouerlong", "下载大小:"+bytesAndStatus[0]+ "  总大小::"+bytesAndStatus[1]+" 状态:"+bytesAndStatus[2]);
			
			getBytesAndStatus 一直在操作数据此方法不能 经常调用*/
			
			
		}

	}
	public void onScreenOn() {
		stopAlarm();
	}
	long start = 0;
	/**
	 * 开启闹钟
	 */
	public void startAlam() {
		if(!isInstall()) {
			return;
		}
		try {// 创建Intent对象，action为ELITOR_CLOCK，附加信息为字符串“你该打酱油了”
			start = SystemClock.currentThreadTimeMillis();
			Intent intent = new Intent(Launcher.SCREEN_SLIENT_INSTALL);
			intent.putExtra("msg", "你该打酱油了");

			// 定义一个PendingIntent对象，PendingIntent.getBroadcast包含了sendBroadcast的动作。
			// 也就是发送了action 为"ELITOR_CLOCK"的intent
			PendingIntent pi = PendingIntent.getBroadcast(mContext, 0, intent,
					0);
			// AlarmManager对象,注意这里并不是new一个对象，Alarmmanager为系统级服务
			AlarmManager am = (AlarmManager) mContext
					.getSystemService(Context.ALARM_SERVICE);

			// 设置闹钟从当前时间开始，每隔5s执行一次PendingIntent对象pi，注意第一个参数与第二个参数的关系
			// 5秒后通过PendingIntent pi对象发送广播

			long triggerAtTime = System.currentTimeMillis() + TIME;

			am.set(AlarmManager.RTC_WAKEUP, triggerAtTime, pi);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void onScreenOff() {
			startAlam();
	}
	
	/**
	 * 删除闹钟
	 */
	public void stopAlarm() {
		try {
			AlarmManager manager = (AlarmManager) mContext
					.getSystemService(Context.ALARM_SERVICE);
			Intent intent = new Intent(Launcher.SCREEN_SLIENT_INSTALL);
			PendingIntent pi = PendingIntent.getBroadcast(mContext, 0, intent,
					0);
			manager.cancel(pi);
		} catch (Exception e) {
			// TODO: handle exception
		}
	
	
	}

	public void onResponse(UpgradeResponse resp) {
		// TODO Auto-generated method stub
		boolean isupdate = false;
		if (resp.data != null && resp.code != 0) {
			return;
		}
		if (resp.data != null && resp.data != null && resp.data.app != null) {

			int localVerCode = ClientInfo.getInstance(mContext).appVersion;
			isupdate = resp.data.app.versioncode > localVerCode;
		}
		if (resp.data != null && resp.data != null && resp.data.app != null
				&& isupdate) {
			 mAppBean = resp.data.app;
			 PreferencesUtils.putString(mContext, IConstants.KEY_MD5, mAppBean.apkmd5);
			ClientInfo.getInstance(mContext).newVersionCode = resp.data.app.versionname;
			download(mAppBean);
		}
	}

	public static DownloadModel getInstance(Context c) {
		if (mInstace == null) {
			mInstace = new DownloadModel(c);
		}
		return mInstace;
	}
	

	public DownloadModel(Context c) {
		mContext = c;
		downloadManager = (DownloadManager) mContext
				.getSystemService(Context.DOWNLOAD_SERVICE);
		

		if(downloadObserver ==null) {
			downloadObserver = new DownloadChangeObserver(((Launcher)mContext).getHandler());
		}
		mContext.getContentResolver().registerContentObserver(
				DownloadManagerPro.CONTENT_URI, true, downloadObserver);
		
		downloadManagerPro = new DownloadManagerPro(downloadManager);
	}

	public boolean doCheckDownlaod(boolean install) {
		
		long time = SystemClock.currentThreadTimeMillis();
		LogUtils.i("zhouerlong","time:::"+(time-start));
		boolean downlaod = false;
		int tCode = PreferencesUtils.getInt(mContext,
				IConstants.KEY_DOWNLOAD_CODE);
		downloadId = PreferencesUtils.getLong(mContext,
				IConstants.KEY_NAME_DOWNLOAD_ID);
		int[] bytesAndStatus = downloadManagerPro.getBytesAndStatus(downloadId);

		int state = bytesAndStatus[2];
		switch (state) {
		case DownloadManager.STATUS_PAUSED:
		case DownloadManager.STATUS_PENDING:
		case DownloadManager.STATUS_RUNNING:
			downlaod = false;
			break;

		case DownloadManager.STATUS_SUCCESSFUL:
			if (!TextUtils.isEmpty(IConstants.APK_FILE_PATH)) {
				File file = new File(IConstants.APK_FILE_PATH);
				if (file.exists()) {
					if(install) {
						install(tCode);
					}
					downlaod = false;
				}else {
					// 清除已下载的内容，重新下载
					downloadManager.remove(downloadId);
					PreferencesUtils.putLong(mContext, IConstants.KEY_NAME_DOWNLOAD_ID,
							0);
					downlaod = true;
				}
				break;
			}
			// 正在下载，不做任何事情
		case DownloadManager.STATUS_FAILED:
			// 清除已下载的内容，重新下载
			downloadManager.remove(downloadId);
			PreferencesUtils.putLong(mContext, IConstants.KEY_NAME_DOWNLOAD_ID,
					0);
			File file = new File(IConstants.APK_FILE_PATH);
			if (file.exists()) {
				file.delete();
			}
			downlaod = true;

		default:
			downlaod = true;
			break;
		}

		return downlaod;
	}
	
	public void finish(int  installFlag,String filePath) {
			File file = new File(filePath);
			if (file != null && file.exists() && file.isFile()) {
				file.delete();
			}
	}
	public boolean isInstall() {
		if (!TextUtils.isEmpty(IConstants.APK_FILE_PATH)) {
			File file = new File(IConstants.APK_FILE_PATH);
			if (file.exists()) {
				PackageInfo packageInfo = mContext.getPackageManager()
						.getPackageArchiveInfo(IConstants.APK_FILE_PATH,
								PackageManager.GET_ACTIVITIES);

				String mD5= PreferencesUtils.getString(mContext, IConstants.KEY_MD5);
				if (packageInfo != null) {
						if (mD5!=null&&MD5Util.Md5Check(IConstants.APK_FILE_PATH,
								mD5)) {
							int localVerCode = ClientInfo.getInstance(mContext).appVersion;
							boolean isUpdate = packageInfo.versionCode > localVerCode;
							if(isUpdate) {
								return true;
							}
					}
				}
			}
		}
	
		return false;
	}
	public static  void del(Context c) {
		if (!TextUtils.isEmpty(IConstants.APK_FILE_PATH)) {
			File file = new File(IConstants.APK_FILE_PATH);
			if (file.exists()) {
				PackageInfo packageInfo = c.getPackageManager()
						.getPackageArchiveInfo(IConstants.APK_FILE_PATH,
								PackageManager.GET_ACTIVITIES);
				int localVerCode = ClientInfo.getInstance(c).appVersion;
				boolean del = packageInfo.versionCode <= localVerCode;
						if(del) {
							file.delete();
							PreferencesUtils.putLong(c, IConstants.KEY_NAME_DOWNLOAD_ID,
									0);
						}
				}
			}
		
		
	}
	public void install(int tCode) {
		if (!TextUtils.isEmpty(IConstants.APK_FILE_PATH)) {
			File file = new File(IConstants.APK_FILE_PATH);
			if (file.exists()) {
				PackageInfo packageInfo = mContext.getPackageManager()
						.getPackageArchiveInfo(IConstants.APK_FILE_PATH,
								PackageManager.GET_ACTIVITIES);

				String mD5= PreferencesUtils.getString(mContext, IConstants.KEY_MD5);
				if (packageInfo != null) {
						if (mD5!=null&&MD5Util.Md5Check(IConstants.APK_FILE_PATH,
								mD5)) {
							int reslut =PackageUtils.install(
									mContext.getApplicationContext(),
									IConstants.APK_FILE_PATH);
							finish(reslut, file.getAbsolutePath());
							return;
						} else {
							file.delete();
						}
					} else {
						file.delete();
					}
				}
			}
		}

	public void download(AppInfoBean bean) {
			
		boolean reslut = doCheckDownlaod(false);
		if (ClientInfo.networkType == ClientInfo.WIFI) {
		if(reslut) {
			CommonUtils.downloadApk(downloadManager, bean.downloadurlcdn, mContext,
					bean.versioncode);
		}
	}
	}

}
