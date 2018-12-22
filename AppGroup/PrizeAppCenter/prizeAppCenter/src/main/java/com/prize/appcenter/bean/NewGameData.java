package com.prize.appcenter.bean;

import com.prize.app.net.datasource.base.AppsItemBean;

import java.io.Serializable;
import java.util.ArrayList;

/***
 * WebView页面请求某个应用的实体
 * 
 * @author fanjunchen
 * 
 */
public class NewGameData implements Serializable {
	private static final long serialVersionUID = 7705947885509430734L;
	public int pageCount;
	public int pageIndex;
	public short pageSize;
	public int pageItemCount;

	public ArrayList<AppsItemBean> apps = new ArrayList<>();
}
