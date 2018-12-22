package com.prize.uploadappinfo.bean;

import java.io.Serializable;

public class AppInfo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public String appName;
	public String packageName;
	public int versionCode;
	public String versionName;
	@Override
	public String toString() {
		return "AppInfo [appName=" + appName + ", packageName=" + packageName
				+ ", versionCode=" + versionCode + ", versionName="
				+ versionName + "]";
	}
	
	
}
