package com.prize.app.beans;

import com.prize.app.net.datasource.base.AppsItemBean;

/**
 * 关键字热搜
 * 
 * @author prize
 * 
 */
public class HotKeyBean {

	public String id;
	public String searchWord;
	public String hotValue;
	public String appType;
	public String ivalue;
	public String ikey;
	public InnerData data;

	public class InnerData {
		public AppsItemBean app;
	}
}
