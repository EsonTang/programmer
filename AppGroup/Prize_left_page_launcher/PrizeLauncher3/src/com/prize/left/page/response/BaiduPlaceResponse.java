package com.prize.left.page.response;

import java.util.List;

import com.prize.left.page.bean.AddrBean;

/***
 * 导航card数据响应类
 * @author fanjunchen
 *
 */
public final class BaiduPlaceResponse {
	/**状态 0 正确*/
	public int status = 0;
	/**返回信息/原因等*/
	public String message;
	/**
	 * 地址组
	 */
	public List<AddrBean> result;
}
