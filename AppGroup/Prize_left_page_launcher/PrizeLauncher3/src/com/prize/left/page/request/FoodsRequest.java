package com.prize.left.page.request;

import org.xutils.http.annotation.HttpRequest;
import org.xutils.http.app.DefaultParamsBuilder;

/***
 * 美食团购数据项请求类
 * @author fanjunchen
 *
 */
@HttpRequest(host = "http://www.baidu.com",
        path = "s",
        builder = DefaultParamsBuilder.class)
public final class FoodsRequest extends BaseRequest {
	
	public FoodsRequest() {
	}
}
