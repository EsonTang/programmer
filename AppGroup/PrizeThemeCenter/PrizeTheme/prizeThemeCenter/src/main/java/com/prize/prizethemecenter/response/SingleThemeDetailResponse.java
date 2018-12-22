package com.prize.prizethemecenter.response;

import com.prize.prizethemecenter.bean.SingleThemeItemBean;

/**
 * Created by Administrator on 2016/9/12.
 */
public class SingleThemeDetailResponse extends BaseResponse<SingleThemeItemBean> {

	public SingleThemeDetailResponse() {
		super();
		data = new SingleThemeItemBean();

	}
}
