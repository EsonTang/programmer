package com.prize.app.net.datasource.base;

import com.prize.app.beans.SearchStatusResBean;
import com.prize.app.beans.TopicItemBean;
import com.prize.app.net.AbstractNetData;

import java.util.ArrayList;

/**
 **
 * 应用列表的返回数据item
 * 
 * @author longbaoxiu
 * @version V1.0
 */
public class PrizeAppsTypeData extends AbstractNetData {
	public ArrayList<AppsItemBean> apps = new ArrayList<AppsItemBean>();
	public TopicItemBean topic;
	public AppData summary;
	public SearchStatusResBean status;
	/**搜索推广 2.7 add**/
	public SearchPopBean pops;
	private int pageCount;
	private int pageIndex;
	private short pageSize;
	private int pageItemCount;
	private String update;  //榜单更新时间

	public ArrayList<AppsItemBean> getApps() {
		return apps;
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
	
	public String getUpdate() {
		return update;
	}

}
