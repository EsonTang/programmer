package com.prize.app.net.datasource.base;

import java.io.Serializable;
import java.util.ArrayList;

import com.prize.app.net.AbstractNetData;

/**
 **
 * 一键安装的返回数据item
 * 
 * @author huanglingjun
 * @version V1.0
 */
public class AppsKeyInstallingListData extends AbstractNetData implements
		Serializable {
	/** 用一句话描述这个变量表示什么 */
	private static final long serialVersionUID = 1L;
	public ArrayList<AppsItemBean> apps = new ArrayList<AppsItemBean>();


	public ArrayList<AppsItemBean> getApps() {
		return apps;
	}
}
