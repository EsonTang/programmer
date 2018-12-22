package com.prize.left.page.response;
/***
 * 响应结果
 * @author fanjunchen
 *
 */
public abstract class BaseResponse<T> {
	/**状态码 0 成功, 1 超时, 2  失败*/
	public int code = 0;
	/**原因或提示*/
	public String msg;
	/**真实的数据实体*/
	public T data;
}
