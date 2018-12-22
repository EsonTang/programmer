package com.prize.weather.framework.mvp.model;

import com.alibaba.fastjson.JSONException;
import com.prize.weather.framework.http.HttpConnection;
import com.prize.weather.framework.http.IJsonParseExceptionHandler;
import com.prize.weather.framework.http.INetworkExcetpionHandler;
import com.prize.weather.util.NetworkUtils;

/**
 * 
 * @author wangzhong
 *
 */
public abstract class BaseModel {
	
	protected HttpConnection mHttpConnection = HttpConnection.obtainConnection();
	protected IJsonParseExceptionHandler mIJsonParseExceptionHandler;
	
	public BaseModel(INetworkExcetpionHandler mINetworkExcetpionHandler,
			IJsonParseExceptionHandler mIJsonParseExceptionHandler) {
		super();
		mHttpConnection.setINetworkExcetpionHandler(mINetworkExcetpionHandler);
		this.mIJsonParseExceptionHandler = mIJsonParseExceptionHandler;
	}
	
	public void abortRequest() {
		if (null != mHttpConnection) {
			mHttpConnection.abort();
		}
	}
	
	public void cancelRequest() {
		if (null != mHttpConnection) {
			abortRequest();
			mHttpConnection.resetConnection();
		}
	}

	public boolean canConnectToHttpServer() {
		if (NetworkUtils.isNetWorkActive()) {
			return true;
		}
		return false;
	}
	
	public void handleJsonParseException(JSONException e) {
		if (null != mIJsonParseExceptionHandler) {
			mIJsonParseExceptionHandler.handleParseException(e);
		}
	}
	
}
