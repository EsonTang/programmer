package com.prize.left.page.model;

public interface IResponse<T> {
	/**
	 * 请求响应方法
	 * @param data 响应对象
	 */
	void onResponse(T resp);
}
