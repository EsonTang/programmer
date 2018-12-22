package com.prize.app.net;

/**
 * 网络数据监听
 * 
 * @author prize
 * 
 */
public interface NetSourceListener<T> {
	/** 成功 */
	public static final int WHAT_SUCCESS = 100;
	/** 失败 */
	public static final int WHAT_NETERR = 110;

	/**
	 * 在类{@link #AbstractNetSource} 中调用（listener.sendMessage(NetSourceListener.
	 * WHAT_SUCCESS, data）（data属于T extends {@link #AbstractNetData}类型） <br/>
	 * 网络层由此向上发送消息<br/>
	 * {@link #WHAT_SUCCESS} 成功<br/>
	 * {@link #WHAT_NETERR} 失败 <br/>
	 * {@link #WHAT_NOT_LOGIN} Session 过期
	 * 
	 * 
	 * @param t
	 */
	void sendMessage(int what, T data);
	

	public String getPersonUserId();
}
