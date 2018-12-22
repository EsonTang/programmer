package com.prize.appcenter.activity;

import com.prize.appcenter.ui.actionBar.ActionBarNoTabActivity;

/**
 * 
 * @author prize
 * 
 */
public abstract class UpdateActivity extends ActionBarNoTabActivity {

	@Override
	protected void onCreate(android.os.Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	};

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onBack(int what, int arg1, int arg2, Object obj) {
	}

}
