package com.prize.left.page.request;

import org.xutils.http.annotation.HttpRequest;
import org.xutils.http.app.DefaultParamsBuilder;

/***
 * 与后台同步卡片请求类
 * @author fanjunchen
 *
 */
@HttpRequest(/*host = "http://localhost.szprize.cn:9999/"*/
        path = "/zyp/api/modules",
        builder = DefaultParamsBuilder.class/*可选参数, 控制参数构建过程, 定义参数签名, SSL证书等*/)
public final class SyncCardRequest extends BaseRequest {
	public String accessTime;
}
