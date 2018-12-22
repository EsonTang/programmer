package com.prize.prizethemecenter.response;

import com.prize.prizethemecenter.bean.SearchResultData;

/***
 *  搜索返回结果
 * @author pengy
 *
 */
public final class SearchResultResponse extends BaseResponse<SearchResultData> {
	public SearchResultResponse(){
        super();
        data = new SearchResultData();
    }
}
