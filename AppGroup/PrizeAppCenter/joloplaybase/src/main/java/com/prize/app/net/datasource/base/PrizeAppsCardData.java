package com.prize.app.net.datasource.base;

import com.prize.app.net.AbstractNetData;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 **
 * 应用列表的返回数据item
 * 
 * @author huanglingjun
 * @version V4.0
 */
public class PrizeAppsCardData extends AbstractNetData {
	public ArrayList<AppsItemBean> apps = new ArrayList<AppsItemBean>();
	public ArrayList<FocusBean> focus = new ArrayList<FocusBean>();
	public AppData summary;
	private int pageCount;
	private int pageIndex;
	private short pageSize;
	private int pageItemCount;

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


	public class FocusBean implements Serializable {
		public int id;
		public String title;
		/**类型 app,web,topic,rank,appsin(嵌入应用)，catsin（嵌入小分类），catgory（分类），matts（田字格）*/
		public String type;
		public String imageUrl;
		/**关联id*/
		public String cid;
		public String updateTime;
		public String value;
		public AppsItemBean app;
		public List<AppsItemBean> apps = new ArrayList<AppsItemBean>();
		public ArrayList<CatFocusBean> catFocusList = new ArrayList<CatFocusBean>();
		/**focus位置*/
		public int positon;
//		public ArrayList<FocusListBean> focusList = new ArrayList<FocusListBean>();

	}

	public class FocusListBean implements Serializable {
		public int id;
		public String title;
		/**类型 app,web,topic,rank,appsin(嵌入应用)，catsin（嵌入小分类），catgory（分类），matts（田字格）*/
		public String type;
		public String imageUrl;
		/**关联id*/
		public String cid;
		public String updateTime;
		public String value;

	}
	public class CatFocusBean implements Serializable {
		public String title;
		public String imageUrl;
		public String catName;
		public String cId;
		/**1:代表应用分类；2：游戏分类**/
		public int catTypeId;
		public String pCatName;
		public int cIdpos;
		public ArrayList<CategoryContent> tags;

	}


}
