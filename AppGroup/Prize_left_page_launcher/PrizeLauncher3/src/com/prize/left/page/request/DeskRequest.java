package com.prize.left.page.request;

import org.xutils.http.annotation.HttpRequest;
import org.xutils.http.app.DefaultParamsBuilder;

/***
 * 导航数据项请求类
 * @author fanjunchen
 *
 */
@HttpRequest( //host="http://launcher.szprize.cn",//localhost.szprize.cn:9999
        path = "/zyp/api/launcherIcons",
        builder = DefaultParamsBuilder.class)
public final class DeskRequest extends BaseRequest {
	
	public String channel = "koobee";
	
	public String accessTime;
	
	public DeskRequest() {
	}
}
