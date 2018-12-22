package com.prize.prizethemecenter.bean;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * 首页推荐返回数据
 * @author pengy
 */
public class HomeThemeNetData implements Serializable{
	
	private static final long serialVersionUID = 1L;

	public HomeThemeData hot;
	public ArrayList<HomeAdBean> banner = new ArrayList<HomeAdBean>();
	public SearchTipsBean searchbox;
	
	
	public class HomeThemeData implements Serializable{
		
		private static final long serialVersionUID = 5642941431364188153L;

		public ArrayList<ThemeItemBean> item = new ArrayList<ThemeItemBean>();
		
		public int pageCount;
		public int pageIndex;
		public short pageSize;
		public int pageItemCount;
	}
	@Override
	public String toString() {
		return "HomeThemeNetData [hot=" + hot + ", banner=" + banner + "]";
	}
}
