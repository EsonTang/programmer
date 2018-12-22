package com.prize.prizethemecenter.request;

import org.xutils.http.RequestParams;

/***
 * 请求基类
 * @author zhouerlong
 *
 */
public class BaseRequest extends RequestParams {
	/**经度*/
	public String longitude;
	/**纬度*/
	public String latitude;
	/**语言*/
	public String language ;
	/**国家地区 code*/
	public String region ;
}
