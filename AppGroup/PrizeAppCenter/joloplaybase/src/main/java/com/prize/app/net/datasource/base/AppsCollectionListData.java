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
public class AppsCollectionListData extends AbstractNetData implements
		Serializable {
	public ArrayList<AppsItemBean> collections = new ArrayList<AppsItemBean>();

	private int pageCount;
	private int pageIndex;
	private short pageSize;
	private int pageItemCount;

	public ArrayList<AppsItemBean> getApps() {
		return collections;
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

	public static class AppsCollectionItemBean implements Serializable {

		/** 用一句话描述这个变量表示什么 */
		private static final long serialVersionUID = 1L;

		public int appTypeId;
		public String name;
		public String packageName;
		public String categoryName;
		public float rating;
		public String versionName;
		public String iconUrl;
		public String largeIcon;
		public long apkSize;
		public String apkSizeFormat;
		public int boxLabel;
		public int downloadTimes;
		public String downloadTimesFormat;
		public String downloadUrl;
		public int versionCode;
		public String updateInfo;
		public String updateTime;
		public String id;
		public String apkMd5;

		@Override
		public String toString() {
			return "AppsCollectionItemBean [appTypeId=" + appTypeId + ", name="
					+ name + ", packageName=" + packageName + ", categoryName="
					+ categoryName + ", rating=" + rating + ", versionName="
					+ versionName + ", iconUrl=" + iconUrl + ", apkSize="
					+ apkSize + ", apkSizeFormat=" + apkSizeFormat
					+ ", boxLabel=" + boxLabel + ", downloadTimes="
					+ downloadTimes + ", downloadTimesFormat="
					+ downloadTimesFormat + ", downloadUrl=" + downloadUrl
					+ ", versionCode=" + versionCode + ", updateInfo="
					+ updateInfo + ", updateTime=" + updateTime + ", id=" + id
					+ "]";
		}
	}

}
