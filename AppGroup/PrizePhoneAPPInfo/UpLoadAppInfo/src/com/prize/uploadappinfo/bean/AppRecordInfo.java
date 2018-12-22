package com.prize.uploadappinfo.bean;

import java.io.Serializable;

public class AppRecordInfo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public String appName;
	public String packageName;
	/** 卸载或安装类型或者*/
	public String type; 
	/** 安装或则卸载时间*/
	public long opTime;
	/** 卸载时或者安装时所在的位置*/
	public String address;
	@Override
	public String toString() {
		return "AppRecordInfo [appName=" + appName + ", packageName="
				+ packageName + ", type=" + type + ", opTime=" + opTime
				+ ", address=" + address + "]";
	}
	
	
}
