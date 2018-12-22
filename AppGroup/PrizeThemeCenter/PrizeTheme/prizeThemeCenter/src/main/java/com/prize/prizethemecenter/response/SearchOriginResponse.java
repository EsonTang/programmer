package com.prize.prizethemecenter.response;

import com.prize.prizethemecenter.bean.SearchOriginData;

/***
 *  自动提示返回结果
 * @author pengy
 *
 */
public final class SearchOriginResponse extends BaseResponse<SearchOriginData> {
	public SearchOriginResponse(){
        super();
        data = new SearchOriginData();
    }
}
