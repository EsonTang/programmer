package com.prize.left.page.request;

import org.xutils.http.annotation.HttpRequest;
import org.xutils.http.app.DefaultParamsBuilder;

/***
 * 
 * @author zhouerlong
 *
 */
@HttpRequest( //host="http://launcher.szprize.cn",//localhost.szprize.cn:9999
		
		//http://192.168.1.187:8080/appstore/launcher/apps
//		host = "http://192.168.1.148:8080",
		host = "http://appstore.szprize.cn",
        path = "/appstore/launcher/apps",
        builder = DefaultParamsBuilder.class)
public final class FolderRequest extends BaseRequest {
	
	public String channel ;
	
	public String accessTime;
	
	public String packageNames;
	
	public FolderRequest() {
	}
}
