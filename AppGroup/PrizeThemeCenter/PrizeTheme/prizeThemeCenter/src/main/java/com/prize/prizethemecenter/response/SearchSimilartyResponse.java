package com.prize.prizethemecenter.response;

import com.prize.prizethemecenter.bean.SearchSimilartyData;

/***
 *  自动提示返回结果
 * @author pengy
 *
 */
public final class SearchSimilartyResponse extends BaseResponse<SearchSimilartyData> {
	public SearchSimilartyResponse(){
        super();
        data = new SearchSimilartyData();
    }
}
