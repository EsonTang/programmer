package com.prize.weather.framework.http;

import java.util.Locale;

import com.prize.weather.framework.Constants;

/**
 * 
 * @author wangzhong
 *
 */
public class HttpConnectionConfig {

	private static String mHost;
	private static Integer mPort;
	private static String mPath;
	
	/**
	 * http://host:port/path
	 */
	private String mFullURL;
	private String mMethod;
	
	private String mPostEncoding, mGetEncoding, mResultEncoding;
	
	private boolean isEncryptData = true;
	private boolean isCompressData = false;
	
	public static String getHost() {
		return mHost;
	}
	public static void setHost(String mHost) {
		HttpConnectionConfig.mHost = mHost;
	}
	public static Integer getPort() {
		return mPort;
	}
	public static void setPort(Integer mPort) {
		HttpConnectionConfig.mPort = mPort;
	}
	public static String getPath() {
		return mPath;
	}
	public static void setPath(String mPath) {
		HttpConnectionConfig.mPath = mPath;
	}
	
	
	public String getFullURL() {
		return mFullURL;
	}
	public void setFullURL(String mFullURL) {
		this.mFullURL = mFullURL;
	}
	public String getMethod() {
		return mMethod;
	}
	public void setMethod(String mMethod) {
		this.mMethod = mMethod;
	}
	
	
	public String getPostEncoding() {
		return null == mPostEncoding ? Constants.DEFAULT_ENCODING : mPostEncoding;
	}
	public void setPostEncoding(String mPostEncoding) {
		this.mPostEncoding = formatEncoding(mPostEncoding);
	}
	public String getGetEncoding() {
		return null == mGetEncoding ? Constants.DEFAULT_ENCODING : mGetEncoding;
	}
	public void setGetEncoding(String mGetEncoding) {
		this.mGetEncoding = formatEncoding(mGetEncoding);
	}
	public String getResultEncoding() {
		return null == mResultEncoding ? Constants.DEFAULT_ENCODING : mResultEncoding;
	}
	public void setResultEncoding(String mResultEncoding) {
		this.mResultEncoding = formatEncoding(mResultEncoding);
	}
	
	
	public boolean isEncryptData() {
		return isEncryptData;
	}
	public void setEncryptData(boolean isEncryptData) {
		this.isEncryptData = isEncryptData;
	}
	public boolean isCompressData() {
		return isCompressData;
	}
	public void setCompressData(boolean isCompressData) {
		this.isCompressData = isCompressData;
	}
	
	
	private String formatEncoding(String encoding) {
		String repEncoding = encoding.toLowerCase(Locale.getDefault()).
				replace("[-_]", "");
		if (repEncoding.equals("utf8"))
			return Constants.UTF_8;
		if (repEncoding.equals("utf16"))
			return Constants.UTF_16;
		if (repEncoding.equals("usascii"))
			return Constants.US_ASCII;
		if (repEncoding.equals("ascii"))
			return Constants.ASCII;
		if (repEncoding.equals("iso88591"))
			return Constants.ISO_8859_1;
		if (repEncoding.equals("gbk"))
			return Constants.GBK;
		if (repEncoding.equals("gb2312"))
			return Constants.GB2312;
		return null;
	}
	
}
