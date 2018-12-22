package com.prize.app.net.datasource.base;
import com.prize.app.net.AbstractNetData;

import java.io.Serializable;
import java.util.ArrayList;

/**
 **
 * 一键安装的返回数据item
 * 
 * @author huanglingjun
 * @version V1.0
 */
public class AppsKeyInstallingPageListData extends AbstractNetData implements
		Serializable  {

	private static final long serialVersionUID = 909526138238620656L;
	public ArrayList<AppsKeyInstallingPageItemBean> onekeylist = new ArrayList<AppsKeyInstallingPageItemBean>();
	public ArrayList<AppsKeyInstallingPageItemBean> getPages() {
		return onekeylist;
	}


	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("onekeylist size = "+onekeylist.size());
		sb.append("\n");
		for (AppsKeyInstallingPageItemBean page : onekeylist) {
			sb.append(page.toString());
			sb.append("----------------------------------------------------------");
			sb.append("\n");
		}
		return sb.toString();
	}

}
