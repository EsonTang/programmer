package com.koobee.koobeecenter.base;

import android.app.Activity;
import android.os.Bundle;

public abstract class BaseActivity extends Activity {
	// /**
	// * 屏幕的宽度和高度
	// */
	// protected int mScreenWidth;
	// protected int mScreenHeight;

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		/**
		 * 获取屏幕宽度和高度
		 */
		// DisplayMetrics metric = new DisplayMetrics();
		// getWindowManager().getDefaultDisplay().getMetrics(metric);
		// mScreenWidth = metric.widthPixels;
		// mScreenHeight = metric.heightPixels;
		super.onCreate(savedInstanceState);

	}

	protected abstract void init();

	protected abstract void findViewById();

	protected abstract void setListener();

}
