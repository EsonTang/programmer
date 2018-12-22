package com.pr.scuritycenter.framework;

import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.pr.scuritycenter.R;

/**
 * 
 * @author wangzhong
 *
 */
public abstract class BaseActivity extends FragmentActivity implements 
		IActivityOnCreateCallBack, OnClickListener {

	//private static final String REFRESH_WIDGET_WEATHER = "com.prize.weather.REFRESH_WIDGET_WEATHER";
	
	protected LayoutInflater mLayoutInflater;
	protected Resources mResources;
	protected WindowManager mWindowManager;
	protected int mScreenWidth;
	protected int mScreenHeight;
	
	protected ImageView bt_topbar_back;
	protected TextView tv_topbar_title;
	protected ImageView bt_topbar_assist;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//Thread.setDefaultUncaughtExceptionHandler(FrameApplication.getInstance());

		initStatusBar();
		
		initPrimaryAttributes();
		
		initInfo();
		
		initView();
		
		initTopbar();
		
		initData();
	}

	private void initStatusBar() {
		Window window = getWindow();
		window.requestFeature(Window.FEATURE_NO_TITLE);
		if(VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
			window = getWindow();
			window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS 
					| WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
			window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
					| View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
			window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
			window.setStatusBarColor(Color.TRANSPARENT);
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

	protected void initTopbar() {
		bt_topbar_back = (ImageView) findViewById(R.id.bt_topbar_back);
		tv_topbar_title = (TextView) findViewById(R.id.tv_topbar_title);
		bt_topbar_assist = (ImageView) findViewById(R.id.bt_topbar_assist);
		
		if (bt_topbar_back != null) {
			bt_topbar_back.setOnClickListener(this);
		}
		if (bt_topbar_assist != null) {
			bt_topbar_assist.setOnClickListener(this);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.bt_topbar_back:
			actionBack();
			break;
		case R.id.bt_topbar_assist:
			actionAssist();
			break;

		default:
			break;
		}
	}

	protected void actionBack() {
		onBackPressed();
	}

	protected void actionAssist() {
		
	}

	/**
	 * Set the title.
	 * @param title
	 */
	protected void setTitle(String title) {
		if (tv_topbar_title != null) {
			tv_topbar_title.setText(title);
		}
	}

	/**
	 * Set the top bar right button bg.
	 * @param resid
	 */
	protected void setAssistBG(int resid) {
		if (bt_topbar_assist != null) {
			bt_topbar_assist.setBackgroundResource(resid);
		}
	}

}
