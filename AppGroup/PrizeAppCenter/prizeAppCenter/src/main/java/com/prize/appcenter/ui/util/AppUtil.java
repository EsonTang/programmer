package com.prize.appcenter.ui.util;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.TrafficStats;

import com.prize.app.download.AppManagerCenter;
import com.prize.app.util.JLog;
import com.prize.appcenter.bean.AppInfo;
import com.prize.appcenter.bean.AppSyncInfo;
import com.prize.appcenter.bean.TrafficInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AppUtil {

	private static final String TAG = "AppUtil";

	/**
	 * 得到所有安装在设备上的程序
	 * 
	 * @param context Context
	 * @return List<AppInfo>
	 */
	public static List<AppInfo> getAllApp(Context context) {

		List<AppInfo> AppInfos = new ArrayList<AppInfo>();
		// 得到应用程序包管理器 ，通过它，可用得到应用程序的一些信息
		PackageManager pm = context.getPackageManager();

		// 得到所有安装的应用程序
		List<ApplicationInfo> appInfos = pm
				.getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES);

		for (ApplicationInfo appInfo : appInfos) {

			/*
			 * 1. 图标 2. 应用程序名称 3. 安装位置 4. APK大小 要先找到APK文件， 用户APK : data/ app
			 * 系统APK : system/app
			 */

			if ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {  
                //添加自己的代码即可  
				AppInfo info = new AppInfo();
				info.mPackageName = appInfo.packageName;
				info.mIcon = appInfo.loadIcon(pm);
				info.mLabel = (String) appInfo.loadLabel(pm);
				// 这里是文件的大小，所以用length
				info.mSize = new File(appInfo.sourceDir).length();
				AppInfos.add(info);
			}
		}

		return AppInfos;
	}

	/**
	 * 1.9同步助手：得到所有安装在设备上的程序,用于同步应用
	 * 
	 * @param context  Context
	 * @return List<AppInfo>
	 */
	public static List<AppSyncInfo> getAllSyncApp(Context context) {

		List<AppSyncInfo> AppSyncInfos = new ArrayList<AppSyncInfo>();
		// 得到应用程序包管理器 ，通过它，可用得到应用程序的一些信息
		PackageManager pm = context.getPackageManager();

		// 得到所有安装的应用程序
		List<ApplicationInfo> appInfos = pm
				.getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES);

		for (ApplicationInfo appInfo : appInfos) {

			if ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {

				AppSyncInfo info = new AppSyncInfo();

				info.packageName = appInfo.packageName;

				info.appName = (String) appInfo.loadLabel(pm);

				info.versionCode = AppManagerCenter.getAppVersionCode(appInfo.packageName);
//				info.versionCode = appInfo.versionCode;


				AppSyncInfos.add(info);
			}

		}
		JLog.i(TAG, "getAllSyncApp-AppSyncInfos.size()=" + AppSyncInfos.size());
		return AppSyncInfos;
	}

	/**
	 * 根据包名，获取到单个应用程序的信息
	 * 
	 * @param context
	 * @param packageName
	 * @return null , 表明获取失败，可以显示包名和默认的icon
	 */
	public static ApplicationInfo getAppInfoByPackageName(Context context,
			String packageName) {
		PackageManager pm = context.getPackageManager();
		try {
			return pm.getApplicationInfo(packageName,
					PackageManager.GET_UNINSTALLED_PACKAGES);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 预留 获取所有的应用程序的流量
	 * @Data 2016.5.20
	 */
	public static List<TrafficInfo> getAppTraffics(Context context) {

		List<TrafficInfo> infos = new ArrayList<TrafficInfo>();
		PackageManager pm = context.getPackageManager();

		// 得到所有安装的应用程序
		List<ApplicationInfo> appInfos = pm
				.getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES);

		for (ApplicationInfo appInfo : appInfos) {

			TrafficInfo tInfo = new TrafficInfo();
			// 获取接收的流量appInfo.
			tInfo.recSize = TrafficStats.getUidRxBytes(appInfo.uid);
			tInfo.sendSize = TrafficStats.getUidTxBytes(appInfo.uid);
			tInfo.icon = appInfo.loadIcon(pm);
			tInfo.label = (String) appInfo.loadLabel(pm);

			infos.add(tInfo);
		}

		return infos;
	}
}
