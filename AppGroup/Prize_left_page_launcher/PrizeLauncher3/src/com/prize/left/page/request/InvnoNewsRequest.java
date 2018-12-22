package com.prize.left.page.request;

import org.xutils.http.annotation.HttpRequest;
import org.xutils.http.app.DefaultParamsBuilder;

/***
 * 新闻请求类
 * @author fanjunchen
 *
 */
@HttpRequest(//http://localhost.szprize.cn:9999
        //path = "card/check",
		path="/zyp/api/datas",
        builder = DefaultParamsBuilder.class)
public final class InvnoNewsRequest extends BaseRequest {
	
	public String uitype;
	
	public String dataCode;
	
	public InvnoNewsRequest() {
	}
}
