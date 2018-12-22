package com.prize.weather.framework.mvp.view;

import com.alibaba.fastjson.JSONException;

import android.content.Context;

/**
 * 
 * @author wangzhong
 *
 */
public interface IView extends IViewUpdateStatusListener {
	
	public Context getContext();
	
	public void handleException(Exception e);
	public void handleJsonExcetpion(JSONException e);
	
	public void updateView(Object o);

	public void showNodataView(boolean isNodata);
	
}
