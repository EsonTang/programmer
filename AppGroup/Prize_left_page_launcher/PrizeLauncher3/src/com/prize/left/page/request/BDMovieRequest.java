package com.prize.left.page.request;

import org.xutils.http.annotation.HttpRequest;
import org.xutils.http.app.DefaultParamsBuilder;

/**
 * 电影请求类
 * 
 * @author Administrator
 * 
 */
@HttpRequest(// http://localhost.szprize.cn:9999
// path = "card/check",
path = "/zyp/api/datas", builder = DefaultParamsBuilder.class)
public final class BDMovieRequest extends BaseRequest {

//	public int pageIndex;
//
//	public int pageSize;

	public String cityId;

	public String uitype;

	public String dataCode;

	public BDMovieRequest() {
	};

}
