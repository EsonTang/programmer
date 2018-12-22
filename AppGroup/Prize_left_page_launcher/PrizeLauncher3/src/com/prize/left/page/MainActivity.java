package com.prize.left.page;


import android.app.Activity;
import android.os.Bundle;

import com.android.launcher3.R;
import com.prize.left.page.ui.LeftFrameLayout;

public class MainActivity extends Activity {

	LeftFrameLayout frame;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.left_page_lay);
		
		initView();
	}
	
	private void initView() {
		frame = (LeftFrameLayout)findViewById(R.id.left_frame);
		frame.setActivity(this);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		frame.onDestroy();
	}
}
