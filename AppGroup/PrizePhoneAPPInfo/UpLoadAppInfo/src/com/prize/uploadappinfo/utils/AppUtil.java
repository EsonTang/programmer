package com.prize.uploadappinfo.utils;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

import com.prize.uploadappinfo.bean.AppInfo;

public class AppUtil {

	private static final String TAG = "AppUtil";

	/**
	 * 得到所有安装在设备上的程序
	 * 
	 * @param context
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

			if ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
				// 添加自己的代码即可
				AppInfo info = new AppInfo();
				info.packageName = appInfo.packageName;
				info.appName = (String) appInfo.loadLabel(pm);
				info.versionCode = appInfo.versionCode;
				try {
					info.versionName = pm.getPackageInfo(appInfo.packageName, 0).versionName;
				} catch (NameNotFoundException e) {
					e.printStackTrace();
				}

				AppInfos.add(info);
			}

		}

		return AppInfos;
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

}
