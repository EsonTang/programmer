package com.prize.weather.framework.http;

import java.io.IOException;

/**
 * 
 * @author wangzhong
 * 
 */
public class NetworkException extends IOException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3427942697625059905L;

	public enum ExceptionType {
		NetworkNotActivie,
		NetworkTooSlow;
	}
	
	private ExceptionType exceptionType;
	
	public NetworkException(String detailMessage, ExceptionType type) {
		super(detailMessage);
		exceptionType = type;
	}

	public ExceptionType getExceptionType() {
		return exceptionType;
	}
	
}
