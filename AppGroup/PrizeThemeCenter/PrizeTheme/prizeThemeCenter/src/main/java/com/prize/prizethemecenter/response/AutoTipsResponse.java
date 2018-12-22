package com.prize.prizethemecenter.response;

import com.prize.prizethemecenter.bean.AutoTipsData;

/***
 *  自动提示返回结果
 * @author pengy
 *
 */
public final class AutoTipsResponse extends BaseResponse<AutoTipsData> {
	public AutoTipsResponse(){
        super();
        data = new AutoTipsData();
    }
}
