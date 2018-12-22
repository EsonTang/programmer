package com.prize.weather.framework.http;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.json.JSONException;

import android.util.Log;

/**
 * 
 * @author wangzhong
 *
 */
public class HttpConnection {
	
	private final static String TAG = "HttpConnection";
	
	private HttpConnectionConfig mConfig = new HttpConnectionConfig();
	private HttpBus mHttpBus = HttpBus.getInstance();
//	private String mRequestData;
	private String mResultData;
	
	private INetworkExcetpionHandler mINetworkExcetpionHandler;
	
	private HttpConnection() {
		
	}
	
	public static HttpConnection obtainConnection() {
		return new HttpConnection();
	}

	public void setFullURL(String fullURL) {
		mConfig.setFullURL(fullURL);
	}
	
	public void setMethod(String method) {
		mConfig.setMethod(method);
	}

	
	public void setINetworkExcetpionHandler(
			INetworkExcetpionHandler mINetworkExcetpionHandler) {
		this.mINetworkExcetpionHandler = mINetworkExcetpionHandler;
		mHttpBus.setNetWorkExcetpionHandler(mINetworkExcetpionHandler);
	}
	
	public void resetConnection() {
		mConfig.setMethod(null);
		
	}

	public void abort() {
		mHttpBus.abort();
	}

	
	
	///////////////////////////////////////////////////////////////////////////////////
	// GET
	///////////////////////////////////////////////////////////////////////////////////
	public String doGet() {
		try {
			prepareGet();
//			mResultData = mHttpBus.strGet(mConfig.getFullURL(), mRequestData);
			mResultData = mHttpBus.strGet(mConfig.getFullURL());
			Log.d(TAG, "mResultData : " + mResultData);
		} catch (UnsupportedEncodingException e) {
			mINetworkExcetpionHandler.handleNetworkException(e, "不支持所设定的字符编码格式");
			return null;
		} catch (IOException e) {
			mINetworkExcetpionHandler.handleNetworkException(e, "发生IO异常");
			return null;
		} catch (JSONException e) {
			mINetworkExcetpionHandler.handleNetworkException(e, "发生JSON异常");
			return null;
		}
		return mResultData;
	}

	private void prepareGet() throws UnsupportedEncodingException,
			IOException, JSONException {
//		mRequestData = new String(mRequestData.getBytes(mConfig.getGetEncoding()), mConfig.getGetEncoding());
	}
	
}
