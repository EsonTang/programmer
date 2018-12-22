package com.pr.scuritycenter.base;

import android.app.Activity;
import android.os.Bundle;

import com.pr.scuritycenter.utils.StateBarUtils;

public abstract class BaseActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		//overridePendingTransition(0, 0);
		super.onCreate(savedInstanceState);
		StateBarUtils.initSateBar(this);
		setContentView();
		findViewById();
		controll();
	}

	protected abstract void setContentView();

	protected abstract void findViewById();

	protected abstract void controll();

	@Override
	protected void onPause() {
		overridePendingTransition(0, 0);
		super.onPause();
	}
}
