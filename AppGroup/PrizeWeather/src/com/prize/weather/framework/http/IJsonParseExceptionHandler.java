package com.prize.weather.framework.http;

import com.alibaba.fastjson.JSONException;

/**
 * 
 * @author wangzhong
 *
 */
public interface IJsonParseExceptionHandler {
	
	public void handleParseException(JSONException e);
	
}
