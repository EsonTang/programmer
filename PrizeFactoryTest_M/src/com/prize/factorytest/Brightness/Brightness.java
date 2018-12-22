package com.prize.factorytest.Brightness;

import com.prize.factorytest.R;
import com.prize.factorytest.PrizeFactoryTestListActivity;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.view.KeyEvent;

public class Brightness extends Activity implements OnClickListener {
	private boolean mBrightnessDown;
	private boolean mBrightnessUp;
	private WindowManager.LayoutParams lp;
	private Button buttonPass;

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK){
			return true;
		}
		return false;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.brightness);
		lp = getWindow().getAttributes();
		Button display_lcd_on = (Button) findViewById(R.id.brightness_strong);
		display_lcd_on.setOnClickListener(this);
		Button display_lcd_off = (Button) findViewById(R.id.brightness_slow);
		display_lcd_off.setOnClickListener(this);
		confirmButton();

	}

	public void confirmButton() {
		buttonPass = (Button) findViewById(R.id.passButton);
		buttonPass.setEnabled(false);
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
	public void onClick(View arg0) {
		switch (arg0.getId()) {
		case R.id.brightness_strong:
			lp.screenBrightness = 1.0f;
			getWindow().setAttributes(lp);
			mBrightnessUp = true;
			if ((mBrightnessUp) && (mBrightnessDown)) {
				buttonPass.setEnabled(true);
			}
			break;
		case R.id.brightness_slow:
			lp = getWindow().getAttributes();
			lp.screenBrightness = 0.2f;
			getWindow().setAttributes(lp);
			mBrightnessDown = true;
			if ((mBrightnessUp) && (mBrightnessDown)) {
				buttonPass.setEnabled(true);
			}
			break;
		}
	}
}
