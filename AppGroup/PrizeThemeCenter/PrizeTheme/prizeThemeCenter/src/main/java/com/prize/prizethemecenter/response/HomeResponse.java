package com.prize.prizethemecenter.response;

import com.prize.prizethemecenter.bean.HomeThemeNetData;

/***
 * 首页返回数据
 * @author pengy
 *
 */
public final class HomeResponse extends BaseResponse<HomeThemeNetData> {
	public HomeResponse(){
        super();
        data = new HomeThemeNetData ();
    }
}
