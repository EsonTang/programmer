package com.prize.app.net.req;

import java.util.ArrayList;

import com.prize.app.beans.SearchKeyword;

/**
 * 全搜关键词列表响应
 * 
 * @author prize
 * @version 1.0 2013-2-7
 *
 */
public class GetSearchKeywordsResp extends BaseResp {
	private ArrayList<SearchKeyword> searchKeywordList;

	public ArrayList<SearchKeyword> getSearchKeywordList() {
		return searchKeywordList;
	}

	public void setSearchKeywordList(ArrayList<SearchKeyword> searchKeywordList) {
		this.searchKeywordList = searchKeywordList;
	}
}
