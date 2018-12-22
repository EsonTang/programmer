package com.prize.factorytest.FingerKeyCheck;


import com.prize.factorytest.R;
import com.prize.factorytest.PrizeFactoryTestListActivity;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.view.KeyEvent;
import android.os.SystemProperties;

public class FingerKeyCheck extends Activity {
	
	private boolean  keyFlay = false;
    
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
		setContentView(R.layout.fingerkey);
		TextView fingerKeyTV = (TextView) findViewById(R.id.fingerkey_check);
		
		
		keyFlay =SystemProperties.get("soter.teei.thh.ifaa").equals("success");
		
		if(keyFlay){
			fingerKeyTV.setText(String.format(getResources().getString(R.string.key_write_disc),getResources().getString(R.string.pass)));  
		}else{
			fingerKeyTV.setText(String.format(getResources().getString(R.string.key_write_disc),getResources().getString(R.string.fail))); 
		}
		 
		confirmButton();
	}

	public void confirmButton() {
		final Button buttonFail = (Button) findViewById(R.id.failButton);
		final Button buttonPass = (Button) findViewById(R.id.passButton);
		
		if(keyFlay){
			buttonPass.setEnabled(true);
		}else{
			buttonPass.setEnabled(false);
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
	protected void onResume() {

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