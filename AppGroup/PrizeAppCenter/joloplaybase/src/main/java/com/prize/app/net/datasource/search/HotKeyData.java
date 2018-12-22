package com.prize.app.net.datasource.search;

import java.util.ArrayList;

import com.prize.app.beans.HotKeyBean;
import com.prize.app.net.AbstractNetData;

public class HotKeyData extends AbstractNetData {
	/**
	 * 列表
	 */
	public ArrayList<HotKeyBean> appWords = new ArrayList<HotKeyBean>();
	public ArrayList<HotKeyBean> gameWords = new ArrayList<HotKeyBean>();
}
