package com.prize.factorytest.TouchPanelEdge;

import com.prize.factorytest.R;
import com.prize.factorytest.PrizeFactoryTestListActivity;
import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.content.Context;
public class TouchPanelEdge extends Activity {

	TPTestSurfaceView mTpTest;
	TextView mLinearity;
	TextView mAccuracy;
	TextView mSensitivity;
	TextView mZoom;
	TextView mLinearityHorizontal;
	TextView mLinearityVertical;
	TextView mFree;

	PowerManager powerManager = null;
	WakeLock wakeLock = null;
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		powerManager = (PowerManager) this
				.getSystemService(Context.POWER_SERVICE);
		wakeLock = this.powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK,
				"My Lock");
	}

	@Override
	public void onResume() {
		super.onResume();
		mTpTest = new TPTestSurfaceView(this);
		setContentView(mTpTest);
		wakeLock.acquire();
	}
	
	@Override
	protected void onPause() {
		wakeLock.release();
		super.onPause();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			if (mTpTest.mModeType == mTpTest.TP_TEST_FREE) {
				confirmButton();
			} else {
				if(mTpTest.mModeType == mTpTest.TP_TEST_LINEARITY1){
					mTpTest.bLinearity1 = false;
				}else if(mTpTest.mModeType == mTpTest.TP_TEST_LINEARITY2){
					mTpTest.bLinearity2 = false;
				}else if(mTpTest.mModeType == mTpTest.TP_TEST_ACCURACY){
					mTpTest.bAccuracy = false;
				}else if(mTpTest.mModeType == mTpTest.TP_TEST_SENSITIVITY){
					mTpTest.bSensitivity = false;
				}else if(mTpTest.mModeType == mTpTest.TP_TEST_ZOOM){
					mTpTest.bZoom = false;
				}else if(mTpTest.mModeType == mTpTest.TP_TEST_LINEARITY_HORIZONTAL){
					mTpTest.bLinearityHorizontal = false;
				}else if(mTpTest.mModeType == mTpTest.TP_TEST_LINEARITY_VERTICAL){
					mTpTest.bLinearityVertical = false;
				}
				mTpTest.toNext();
			}
			break;
		}
		return true;
	}

	public void confirmButton() {

		setContentView(R.layout.touchpanel_confirm);
		mLinearityHorizontal = (TextView)findViewById(R.id.linearityHorizontal);
		mLinearityVertical = (TextView)findViewById(R.id.linearityVertical);
		mFree = (TextView)findViewById(R.id.free);
		if(mTpTest.bLinearityHorizontal){
			mLinearityHorizontal.setText(getString(R.string.touchpanel_edge_linearityHorizontal) + getString(R.string.touchpanel_edge_pass));
		}else{
			mLinearityHorizontal.setText(getString(R.string.touchpanel_edge_linearityHorizontal) + getString(R.string.touchpanel_edge_fail));
		}
		if(mTpTest.bLinearityVertical){
			mLinearityVertical.setText(getString(R.string.touchpanel_edge_linearityVertical) + getString(R.string.touchpanel_edge_pass));
		}else{
			mLinearityVertical.setText(getString(R.string.touchpanel_edge_linearityVertical) + getString(R.string.touchpanel_edge_fail));
		}
		
		final Button buttonPass = (Button) findViewById(R.id.passButton);
		final Button buttonFail = (Button) findViewById(R.id.failButton);
		
		if (mTpTest.bLinearityHorizontal && mTpTest.bLinearityVertical) {//mTpTest.bLinearity1 && mTpTest.bLinearity2 && mTpTest.bAccuracy && mTpTest.bSensitivity && mTpTest.bZoom && 
			buttonPass.setEnabled(true);
		} else {
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

}
