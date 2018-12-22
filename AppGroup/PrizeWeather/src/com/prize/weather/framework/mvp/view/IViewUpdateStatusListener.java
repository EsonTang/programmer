package com.prize.weather.framework.mvp.view;

/**
 * 
 * @author wangzhong
 *
 */
public interface IViewUpdateStatusListener {

	/**
	 * For example: show the progressdialog.
	 */
	public void openUpdateStatus();
	
	public void closeUpdateStatus();
	
}
