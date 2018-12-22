package com.prize.factorytest.FingerPrint;

import com.prize.factorytest.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.os.SystemProperties;
import com.prize.factorytest.PrizeFactoryTestListActivity;
import android.util.Log;
import android.view.KeyEvent;

public class FingerPrint extends Activity {
	public static Button buttonPass;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fingerprint);
		startFingerPrint();
		confirmButton();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK){
			return true;
		}
		return false;
	}
	
	void startFingerPrint() {
		/*Prize-modify by liyu-for fingerprint factory test-20160621-start*/
		if (SystemProperties.get("ro.prize_fingerprint").equals("1")){
			try{
				startActivity(new Intent(FingerPrint.this, FingerPrintActivity.class));
			}catch(Exception e){
				
			}
		}
		else if(SystemProperties.get("ro.prize_fingerprint").equals("2")){
			try{
				Intent intent = new Intent();
				intent.setClassName("com.android.settings", 
					"com.android.settings.fingerprint.FingerprintEnrollEnrolling");
				startActivity(intent);
			}catch(Exception e){
				
			}
		}
		/*Prize-modify by liyu-for fingerprint factory test-20160621-end*/
		return;
	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == 1) {
			 buttonPass.setEnabled(true);
		}
	}
	
	public void confirmButton() {
		buttonPass = (Button) findViewById(R.id.passButton);
		final Button buttonFail = (Button) findViewById(R.id.failButton);
		buttonPass.setEnabled(false);
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
	protected void onResume() {
		if(SystemProperties.get("persist.sys.prize_fp_enable").equals("1")){
			buttonPass.setEnabled(true);
			SystemProperties.set("persist.sys.prize_fp_enable", "0");
		}
		super.onResume();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	public void finish() {
		super.finish();
	}

}
