package com.prize.weather.framework.mvp.presenter;

/**
 * 
 * @author wangzhong
 *
 * @param <R>
 */
public interface IUpdate<R> {
	
	public R doInBackground();
	
	public void updateView(R mBackgroundHandler);

}
