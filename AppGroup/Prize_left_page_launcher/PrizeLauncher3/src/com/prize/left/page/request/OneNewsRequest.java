package com.prize.left.page.request;

import org.xutils.http.annotation.HttpRequest;
import org.xutils.http.app.DefaultParamsBuilder;

/***
 * 一点资讯新闻请求类
 * @author fanjunchen
 *
 */
@HttpRequest(host = "http://o.go2yd.com",
        path = "oapi/kusai/channel",
        builder = DefaultParamsBuilder.class/*可选参数, 控制参数构建过程, 定义参数签名, SSL证书等*/)
public final class OneNewsRequest extends BaseRequest {
	
	public String channel_id = "0";
	
	public int count = 3;
	
	public int offset = 1;
	
	public OneNewsRequest() {
	}
	
	public void nextPage() {
		offset = offset + count;
	}
	
	public void reset() {
		offset = 1;
		count = 3;
	}
}
