package com.prize.autotest.mmi;

import com.prize.autotest.AutoConstant;
import com.prize.autotest.R;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public class AutoTPTestActivity extends Activity {

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
	
	private String cmdOrder = null;
	private BroadcastReceiver mBroadcast = null;
	public static boolean isTPResult = false;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		powerManager = (PowerManager) this
				.getSystemService(Context.POWER_SERVICE);
		wakeLock = this.powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK,
				"My Lock");
		
		mTpTest = new TPTestSurfaceView(this);
		setContentView(mTpTest);
		isTPResult = false;
		mBroadcast = new MyBroadcastReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(AutoConstant.ACTION_UI);
		registerReceiver(mBroadcast, filter);

		Intent intent = getIntent();
		cmdOrder = intent.getStringExtra("back");
		if (cmdOrder != null) {
			new Handler().post(new Runnable() {
				public void run() {
					runCmdOrder();
				}
			});
		}		
		
	}

	private class MyBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent != null) {
				cmdOrder = intent.getStringExtra("back");
				runCmdOrder();
			}
		}
	}
	private void runCmdOrder() {
		if (cmdOrder == null || cmdOrder.length() < 1) {
			return;
		}

		String temp = cmdOrder.substring(1);
		
		if (temp.startsWith(AutoConstant.CMD_MMI_TP_RESULT)) {
			if(isTPResult){
				AutoConstant.SendDataToService(AutoConstant.RESULT_SUCCUSS, this);
				AutoConstant.writeFile("TP : PASS" + "\n");
			}else{
				AutoConstant.SendDataToService(AutoConstant.RESULT_FAIL, this);
				AutoConstant.writeFile("TP : FAIL" + "\n");
			}
			finish();
		}
		AutoConstant.SendDataToService(AutoConstant.RESULT_SUCCUSS,
				this);

	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		wakeLock.acquire();
	}

	@Override
	protected void onPause() {
		wakeLock.release();
		super.onPause();
	}
	/*
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			if (mTpTest.mModeType == mTpTest.TP_TEST_FREE) {
				confirmButton();
			} else {
				if (mTpTest.mModeType == mTpTest.TP_TEST_LINEARITY1) {
					mTpTest.bLinearity1 = false;
				} else if (mTpTest.mModeType == mTpTest.TP_TEST_LINEARITY2) {
					mTpTest.bLinearity2 = false;
				} else if (mTpTest.mModeType == mTpTest.TP_TEST_ACCURACY) {
					mTpTest.bAccuracy = false;
				} else if (mTpTest.mModeType == mTpTest.TP_TEST_SENSITIVITY) {
					mTpTest.bSensitivity = false;
				} else if (mTpTest.mModeType == mTpTest.TP_TEST_ZOOM) {
					mTpTest.bZoom = false;
				} else if (mTpTest.mModeType == mTpTest.TP_TEST_LINEARITY_HORIZONTAL) {
					mTpTest.bLinearityHorizontal = false;
				} else if (mTpTest.mModeType == mTpTest.TP_TEST_LINEARITY_VERTICAL) {
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
		mLinearityHorizontal = (TextView) findViewById(R.id.linearityHorizontal);
		mLinearityVertical = (TextView) findViewById(R.id.linearityVertical);
		mFree = (TextView) findViewById(R.id.free);
		if (mTpTest.bLinearityHorizontal) {
			mLinearityHorizontal
					.setText(getString(R.string.touchpanel_edge_linearityHorizontal)
							+ getString(R.string.touchpanel_edge_pass));
		} else {
			mLinearityHorizontal
					.setText(getString(R.string.touchpanel_edge_linearityHorizontal)
							+ getString(R.string.touchpanel_edge_fail));
		}
		if (mTpTest.bLinearityVertical) {
			mLinearityVertical
					.setText(getString(R.string.touchpanel_edge_linearityVertical)
							+ getString(R.string.touchpanel_edge_pass));
		} else {
			mLinearityVertical
					.setText(getString(R.string.touchpanel_edge_linearityVertical)
							+ getString(R.string.touchpanel_edge_fail));
		}

		if (mTpTest.bLinearityHorizontal && mTpTest.bLinearityVertical) {
			AutoConstant.writeFile("TP : PASS" + "\n");
			AutoConstant.SendDataToService(AutoConstant.RESULT_SUCCUSS, this);
		} else {
			AutoConstant.writeFile("TP : FAIL" + "\n");
			AutoConstant.SendDataToService(AutoConstant.RESULT_FAIL, this);
		}

	}
*/
}
