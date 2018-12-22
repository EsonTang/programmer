package com.prize.weather.framework;

/**
 * onCreate()
 * @author wangzhong
 */
public interface IActivityOnCreateCallBack {

	/**
	 * Initialize the basic attributes of the page.
	 */
	public void initPrimaryAttributes();

	/**
	 * Initialize the initial information of the page.<br>
	 * (Contains the <b>Intent</b> of the message and <b>SharedPreferences</b> information, etc)
	 */
	public void initInfo();
	
	/**
	 * Create a view.<br>
	 * (Note: in this method the first line of the adding the <b>setContentView(R.layout.xxx);</b>)
	 */
	public void initView();
	
	/**
	 * Initialize the data needs to be loaded.
	 * 
	 */
	public void initData();

}
