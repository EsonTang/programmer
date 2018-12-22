package com.pr.scuritycenter.bean;

import android.graphics.drawable.Drawable;

public class AppInfo {

	/**
	 * 图标
	 */
	private Drawable mDrawable = null;
	/**
	 * 应用程序的名字
	 */
	private String appName = null;
	/**
	 * 应用程序的包名
	 */
	private String packageName = null;
	/**
	 * 应用程序的大小
	 */
	private long apkSize;

	/**
	 * 应用程序的路径
	 */
	private String apkPath = null;
	/**
	 * 安装到哪里
	 */
	private boolean isInRom = false;
	/**
	 * 判断是系统app还是用户app
	 */
	private boolean isUserApp = false;

	public Drawable getmDrawable() {
		return mDrawable;
	}

	public void setmDrawable(Drawable mDrawable) {
		this.mDrawable = mDrawable;
	}

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public long getApkSize() {
		return apkSize;
	}

	public void setApkSize(long apkSize) {
		this.apkSize = apkSize;
	}

	public String getApkPath() {
		return apkPath;
	}

	public void setApkPath(String apkPath) {
		this.apkPath = apkPath;
	}

	public boolean isInRom() {
		return isInRom;
	}

	public void setInRom(boolean isInRom) {
		this.isInRom = isInRom;
	}

	public boolean isUserApp() {
		return isUserApp;
	}

	public void setUserApp(boolean isUserApp) {
		this.isUserApp = isUserApp;
	}

	@Override
	public String toString() {
		return "AppInfo [appName=" + appName + ", packageName=" + packageName
				+ ", apkSize=" + apkSize + ", apkPath=" + apkPath
				+ ", isInRom=" + isInRom + ", isUserApp=" + isUserApp + "]";
	}

}
