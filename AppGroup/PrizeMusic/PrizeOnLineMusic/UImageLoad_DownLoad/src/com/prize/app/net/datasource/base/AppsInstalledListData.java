package com.prize.app.net.datasource.base;

import java.io.Serializable;
import java.util.ArrayList;

import com.prize.app.net.AbstractNetData;

/**
 **
 * 应用列表的返回数据item
 * 
 * @author huanglingjun
 * @version V1.0
 */
public class AppsInstalledListData extends AbstractNetData implements
		Serializable {
	/** 用一句话描述这个变量表示什么 */
	private static final long serialVersionUID = 1L;
	public ArrayList<AppsInstallItemBean> records = new ArrayList<AppsInstallItemBean>();

	private int pageCount;
	private int pageIndex;
	private short pageSize;
	private int pageItemCount;

	public ArrayList<AppsInstallItemBean> getApps() {
		return records;
	}

	public int getPageCount() {
		return pageCount;
	}

	public int getPageIndex() {
		return pageIndex;
	}

	public short getPageSize() {
		return pageSize;
	}

	public int getPageItemCount() {
		return pageItemCount;
	}

	public class AppsInstallItemBean implements Serializable {
		public int id;
		public int accountId;
		public String createTime;
		public AppsItemBean app;
		private static final long serialVersionUID = 1L;

	}
}
