package com.prize.left.page.request;

import org.xutils.http.annotation.HttpRequest;
import org.xutils.http.app.DefaultParamsBuilder;

/***
 * 热门搜索请求类
 * @author  pengy
 *
 */
@HttpRequest(
        path = "zyp/api/datas",
        builder = DefaultParamsBuilder.class/*可选参数, 控制参数构建过程, 定义参数签名, SSL证书等*/)
public final class HotTipsRequest extends BaseRequest {
	
	public String uitype;

	public String dataCode;
	
	public HotTipsRequest() {
	}
}
