package com.prize.app.net.datasource.search;

import java.util.ArrayList;

import com.prize.app.beans.HotKeyBean;
import com.prize.app.net.AbstractNetData;

public class HotAppData extends AbstractNetData {
	/**
	 * 列表
	 */

	public int pageCount;
	public int pageIndex;
	public int pageSize;
	public int pageItemCount;
	public ArrayList<HotKeyBean> hotwords = new ArrayList<HotKeyBean>();
}
