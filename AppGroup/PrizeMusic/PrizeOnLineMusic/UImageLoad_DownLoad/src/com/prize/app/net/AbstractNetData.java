package com.prize.app.net;

/**
 **
 * 响应网络数据（包含正确的响应码 ，响应码，响应消息，Session 超时）
 * 
 * @author longbaoxiu
 * @version V1.0
 */
public class AbstractNetData {
	/** 正确的响应码 **/
	public static final int RESPONSE_CODE_SUCCESS = 200;

	/** Session 超时 */
	public static final int RESPONSE_CODE_ERROR_SESSION_OUT_DATE = 90030000;

	/** 响应码 */
	public int code;
	/** 响应消息 */
	public String msg;

}
