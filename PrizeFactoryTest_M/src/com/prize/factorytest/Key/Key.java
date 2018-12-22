package com.prize.factorytest.Key;

import com.prize.factorytest.R;
import com.prize.factorytest.PrizeFactoryTestListActivity;
import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.os.SystemProperties;
	
public class Key extends Activity {

	private boolean bKeyHome = false;
	private boolean bKeyMenu = false;
	private boolean bKeyBack = false;
	private boolean bKeyVoUp = false;
	private boolean bKeyVoDn = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.key);
		if(SystemProperties.get("ro.support_hiding_navbar").equals("1")) {
			TextView mKeyMenu = (TextView) findViewById(R.id.menu);
			mKeyMenu.setVisibility(View.GONE);
		}
		confirmButton();

	}

	void confirmButton() {
		final Button buttonPass = (Button) findViewById(R.id.passButton);
		final Button buttonFail = (Button) findViewById(R.id.failButton);
		if(SystemProperties.get("ro.support_hiding_navbar").equals("1")) {
			if (bKeyVoUp && bKeyVoDn && bKeyBack && bKeyHome) {
				buttonPass.setEnabled(true);
			} else {
				buttonPass.setEnabled(false);
			}
		}else{
			if (bKeyVoUp && bKeyVoDn && bKeyBack && bKeyHome && bKeyMenu) {
				buttonPass.setEnabled(true);
			} else {
				buttonPass.setEnabled(false);
			}
		}
		
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
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		return true;
	}
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		TextView keyText = null;
		switch (keyCode) {
		case KeyEvent.KEYCODE_VOLUME_UP:
			keyText = (TextView) findViewById(R.id.volume_up);
			bKeyVoUp = true;
			break;
		case KeyEvent.KEYCODE_VOLUME_DOWN:
			keyText = (TextView) findViewById(R.id.volume_down);
			bKeyVoDn = true;
			break;
		case KeyEvent.KEYCODE_MENU:
			keyText = (TextView) findViewById(R.id.menu);
			bKeyMenu = true;
			break;
		case KeyEvent.KEYCODE_HOME:
			keyText = (TextView) findViewById(R.id.home);
			bKeyHome = true;
			break;
		case KeyEvent.KEYCODE_BACK:
			keyText = (TextView) findViewById(R.id.back);
			bKeyBack = true;
			break;
		}
		if (null != keyText) {
			keyText.setBackgroundResource(R.color.green);
			confirmButton();
		}
		return true;
	}

}
