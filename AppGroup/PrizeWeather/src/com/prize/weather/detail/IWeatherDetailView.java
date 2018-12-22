package com.prize.weather.detail;

import com.prize.weather.framework.mvp.view.IView;

/**
 * 
 * @author wangzhong
 *
 */
public interface IWeatherDetailView extends IView {

	public boolean isLocation();
	public boolean isFirst();
	public boolean isSave();
	
	public int getID();
	public String getName();
	
}
