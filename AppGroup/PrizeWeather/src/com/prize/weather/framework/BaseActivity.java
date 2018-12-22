package com.prize.weather.framework;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

/**
 * 
 * @author wangzhong
 *
 */
public abstract class BaseActivity extends FragmentActivity implements 
		IActivityOnCreateCallBack, IBDLocationFinishedListener {
	
	private static final String REFRESH_WIDGET_WEATHER = "com.prize.weather.REFRESH_WIDGET_WEATHER";
	
	protected LayoutInflater mLayoutInflater;
	protected Resources mResources;
	protected WindowManager mWindowManager;
	protected int mScreenWidth;
	protected int mScreenHeight;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		Thread.setDefaultUncaughtExceptionHandler(FrameApplication.getInstance());
		// location.
//		FrameApplication.getInstance().setLocationOption();
//		FrameApplication.getInstance().setmIBDLocationFinishedListener(this);

		initStatusBar();
		
		initPrimaryAttributes();
		
		initInfo();
		
		initView();
		
		initData();
		
		SysAppList sl = SysAppList.getInstance();
		sl.addActivity(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		Intent intent = new Intent(REFRESH_WIDGET_WEATHER);

		FrameApplication.getInstance().setLocationOption();
		FrameApplication.getInstance().setmIBDLocationFinishedListener(this);
		this.sendBroadcast(intent);	
		
	}

	private void initStatusBar() {
		Window window = getWindow();
		window.requestFeature(Window.FEATURE_NO_TITLE);
		if(VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
			window = getWindow();
			window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS 
					| WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
			window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
					//| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
					| View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
			window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
			window.setStatusBarColor(Color.TRANSPARENT);
			//window.setNavigationBarColor(Color.TRANSPARENT);
		}
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void initPrimaryAttributes() {
		mLayoutInflater = getLayoutInflater();
		mResources = getResources();
		mWindowManager = getWindowManager();
		Display display = mWindowManager.getDefaultDisplay();
		mScreenWidth = display.getWidth();
		mScreenHeight = display.getHeight();
	}

	@Override
	public void onBDLocationFinishedListener() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void initInfo() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void initView() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void initData() {
		// TODO Auto-generated method stub
		
	}
	
	
}
