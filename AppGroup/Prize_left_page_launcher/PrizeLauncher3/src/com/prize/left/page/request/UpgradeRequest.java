package com.prize.left.page.request;

import org.xutils.http.annotation.HttpRequest;
import org.xutils.http.app.DefaultParamsBuilder;

/***
 * 应用升级请求类
 * @author fanjunchen
 *
 */
@HttpRequest( host="http://192.168.1.158:8083",
        path = "/zyp/api/getNewVersion",
        builder = DefaultParamsBuilder.class)
public final class UpgradeRequest extends BaseRequest {
	
	/**版本号 */
	public int versionCode = 0;
	/**需要升级的包名及版本号, 以'#'相连, ';'分号格开, eg: a.b.c#3;b.a.c#2*/
	public String packages;
	
	public UpgradeRequest() {
	}
}
