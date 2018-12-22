package com.prize.left.page.response;

import java.util.ArrayList;

import com.prize.left.page.bean.HotTipsBean;

/***
 * 热门搜索响应类
 * @author pengy
 *
 */
public final class HotTipsResponse extends BaseResponse<HotTipsBean> {

	public HotTipsResponse() {
		super();
		data = new HotTipsBean();
		data.list = new ArrayList<>();
	}
}
