package com.prize.left.page.model;
/***
 * 业务操作接口
 * @author fanjunchen
 *
 */
public interface IModel<T> extends IResponse<T> {
	/**发送Get请求*/
	void doGet();
	/**发送post请求*/
	void doPost();
	/**取消请求*/
	void cancel();
}
