package com.prize.left.page.request;

import org.xutils.http.annotation.HttpRequest;
import org.xutils.http.app.DefaultParamsBuilder;

/***
 * 百度热词请求类
 * @author fanjunchen
 *
 */
@HttpRequest(
        path = "zyp/api/datas",
        builder = DefaultParamsBuilder.class/*可选参数, 控制参数构建过程, 定义参数签名, SSL证书等*/)
public final class HotWordRequest extends BaseRequest {
	
	public String type;
	
	public String from ;
	
	public String dt ;
	/**类型, shishi, dianying, zongyi, tiyu*/
	public String c ;
	
	public String uitype;

	public String dataCode;
	
	public HotWordRequest() {
	}
}
