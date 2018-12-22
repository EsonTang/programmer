package com.prize.weather.framework.http;

/**
 * 
 * @author wangzhong
 * 
 */
public interface INetworkExcetpionHandler {
	
	public void handleNetworkException(Exception e, String warningMessage);
	
}
