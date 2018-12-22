package com.prize.left.page.test;

import org.xutils.x;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.prize.left.page.MainActivity;
import com.android.launcher3.R;

public class TestMainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.test_main);

		initView();
	}

	private void initView() {
		x.Ext.setDebug(true);

	}

	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
			case R.id.btn_main:
				Intent it = new Intent(this, MainActivity.class);
				startActivity(it);
				break;
		}
	}

}
