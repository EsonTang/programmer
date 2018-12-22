package com.prize.left.page.request;

import org.xutils.http.annotation.HttpRequest;
import org.xutils.http.app.DefaultParamsBuilder;

/***
 * 常用(最近)联系人请求类
 * @author fanjunchen
 *
 */
@HttpRequest(host = "http://www.baidu.com",
        path = "s",
        builder = DefaultParamsBuilder.class/*可选参数, 控制参数构建过程, 定义参数签名, SSL证书等*/)
public final class AppRequest extends BaseRequest {
	/**搜索串*/
	public String queryStr;
	
	public AppRequest() {
	}
}
