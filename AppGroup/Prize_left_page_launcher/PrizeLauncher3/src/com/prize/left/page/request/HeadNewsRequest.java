package com.prize.left.page.request;

import org.xutils.http.annotation.HttpRequest;
import org.xutils.http.app.DefaultParamsBuilder;

/***
 * 头条新闻请求类
 * @author fanjunchen
 *
 */
@HttpRequest(host = "http://www.baidu.com",
        path = "s",
        builder = DefaultParamsBuilder.class/*可选参数, 控制参数构建过程, 定义参数签名, SSL证书等*/)
public final class HeadNewsRequest extends BaseRequest {
	
	public HeadNewsRequest() {
	}
}
