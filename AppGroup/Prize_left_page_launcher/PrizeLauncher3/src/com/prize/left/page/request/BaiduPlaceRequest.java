package com.prize.left.page.request;

import org.xutils.http.RequestParams;
import org.xutils.http.annotation.HttpRequest;

/***
 * 美食团购数据项请求类
 * @author fanjunchen
 *
 */
@HttpRequest(host = "http://dt.szprize.cn",
        path = "baidu/suggestion.php")
public final class BaiduPlaceRequest extends RequestParams {
	/**关键字*/
	public String query;
	/**区域编号*/
	public String region;
	/**GPS位置*/
	public String location;
	
	public BaiduPlaceRequest() {
	}
	/**
	 * 设置查询串
	 * @param str
	 */
	public void setQ(String str) {
		query = str;//Uri.encode(str);
	}
}
