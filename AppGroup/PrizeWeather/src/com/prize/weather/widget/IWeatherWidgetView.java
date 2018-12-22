package com.prize.weather.widget;

import com.prize.weather.framework.mvp.view.IView;

/**
 * 
 * @author wangzhong
 *
 */
public interface IWeatherWidgetView extends IView {
	
	public boolean isSave();
	
	public String getName();
	public int getID();

}
