package com.prize.left.page.request;

import org.xutils.http.annotation.HttpRequest;
import org.xutils.http.app.DefaultParamsBuilder;

/**
 * 团购请求类
 * @author liukun
 *
 */
@HttpRequest(/*host="",*/path="/zyp/api/datas",builder=DefaultParamsBuilder.class)
public class BDGroupRequest extends BaseRequest{
	
//	public int pageIndex;
//
//	public int pageSize;

	public String cityId;
	
	public String uitype;

	public String dataCode;

	public BDGroupRequest() {
	};
}
