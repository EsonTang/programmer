package com.prize.app.net.datasource.base;

import com.prize.app.beans.HomeAdBean;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * 搜索新增的推广返回bean（2.7add ）
*/
public class SearchPopBean implements Serializable{
	/**手机预装应用*/
	public String searchText;
	public String type;
	public String ids;
	public String updateTime;
	public ArrayList<AppsItemBean> apps = new ArrayList<AppsItemBean>();
	public HomeAdBean ads;

}
