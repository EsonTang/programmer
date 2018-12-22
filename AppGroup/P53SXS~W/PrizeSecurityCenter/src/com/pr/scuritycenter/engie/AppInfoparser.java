package com.pr.scuritycenter.engie;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;

import com.pr.scuritycenter.bean.AppInfo;

public class AppInfoparser {
	public static List<AppInfo> getAppInfo(Context context) {
		// 获取到安装包的管理者
		PackageManager pm = context.getPackageManager();
		// 获取到安装包的所有基本程序的信息
		List<PackageInfo> infos = pm.getInstalledPackages(0);

		List<AppInfo> lists = new ArrayList<AppInfo>();

		for (PackageInfo info : infos) {
			AppInfo appInfo = new AppInfo();
			// 获取到app的包名
			String packageName = info.packageName;
			int uid = info.applicationInfo.uid;
			System.out.println(packageName + "----" + uid);
			appInfo.setPackageName(packageName);
			// 获取到icon图标
			Drawable icon = info.applicationInfo.loadIcon(pm);

			appInfo.setmDrawable(icon);
			// 获取到应用程序的名字
			String appname = (String) info.applicationInfo.loadLabel(pm);

			appInfo.setAppName(appname);

			// 获取到apk的路径
			String apkPath = info.applicationInfo.sourceDir;

			appInfo.setApkPath(apkPath);

			File file = new File(apkPath);
			// 获取到apk的大小
			long apkSize = file.length();

			appInfo.setApkSize(apkSize);

			int flags = info.applicationInfo.flags;
			// 判断当前是应用app还是系统app
			if ((flags & info.applicationInfo.FLAG_SYSTEM) != 0) {
				// true 表示是系统应用
				appInfo.setUserApp(false);
			} else {
				// false 表示是用户应用
				appInfo.setUserApp(true);
			}
			// 判断当前是app是否是安装到哪里
			if ((flags & info.applicationInfo.FLAG_EXTERNAL_STORAGE) != 0) {
				// ture 表示安装到外部存储 sd卡
				appInfo.setInRom(false);
			} else {
				// false 表示安装到机身内存。rom
				appInfo.setInRom(true);
			}

			lists.add(appInfo);

		}

		return lists;
	}
}
