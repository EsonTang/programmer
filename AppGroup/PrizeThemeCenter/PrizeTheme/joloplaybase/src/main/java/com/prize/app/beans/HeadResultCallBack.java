package com.prize.app.beans;

import java.util.Map;

/**
 * head信息回调接口
 * @author prize
 */
public interface HeadResultCallBack {
	void onResponseHeaders(Map<String, String> headers);
}
