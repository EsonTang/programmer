package com.prize.app.net.datasource.base;

import java.util.ArrayList;

import com.prize.app.beans.HomeAdBean;
import com.prize.app.beans.Navbars;
import com.prize.app.beans.Navblocks;
import com.prize.app.net.AbstractNetData;

/**
 **
 * 首页广告
 * 
 * @author longbaoxiu
 * @version V1.0
 */
public class AppData extends AbstractNetData {

	public ArrayList<HomeAdBean> ads = new ArrayList<HomeAdBean>();
	public ArrayList<Navbars> navbars = new ArrayList<Navbars>();
	public ArrayList<Navblocks> navblocks = new ArrayList<Navblocks>();
	public ArrayList<String> words = new ArrayList<String>();

	// public String listCode;
	//
	// public String listname;

	public AppData() {
		ads = new ArrayList<HomeAdBean>();
	}

}
