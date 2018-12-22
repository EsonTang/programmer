package com.prize.factorytest.YCD;

import com.prize.factorytest.R;
import com.prize.factorytest.PrizeFactoryTestListActivity;

import java.io.IOException;
import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.widget.Button;
import android.view.KeyEvent;

public class YCD extends Activity {

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK){
			return true;
		}
		return false;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ycd);
		try {	
    		String[] cmdMode = new String[]{"/system/bin/sh","-c","echo" + " " + 1 + " > /proc/led_yc_mode"};
			Runtime.getRuntime().exec(cmdMode);				
		} catch (IOException e) {
			e.printStackTrace();
		}
		confirmButton();
	}

	public void confirmButton() {
		final Button buttonPass = (Button) findViewById(R.id.passButton);
		final Button buttonFail = (Button) findViewById(R.id.failButton);
		buttonPass.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (PrizeFactoryTestListActivity.toStartAutoTest == true) {
					PrizeFactoryTestListActivity.itempos++;
				}
				setResult(RESULT_OK);
				finish();
			}
		});
		buttonFail.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (PrizeFactoryTestListActivity.toStartAutoTest == true) {
					PrizeFactoryTestListActivity.itempos++;
				}
				setResult(RESULT_CANCELED);
				finish();
			}

		});
	}

	@Override
	protected void onDestroy() {
		try {	
    		String[] cmdMode = new String[]{"/system/bin/sh","-c","echo" + " " + 0 + " > /proc/led_yc_mode"};
			Runtime.getRuntime().exec(cmdMode);				
		} catch (IOException e) {
			e.printStackTrace();
		}

		super.onDestroy();
	}

}
