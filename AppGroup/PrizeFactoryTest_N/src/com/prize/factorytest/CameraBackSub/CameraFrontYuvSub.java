package com.prize.factorytest.CameraBackSub;

import java.io.IOException;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import java.io.File;
import java.util.Timer;
import java.util.TimerTask;
import com.prize.factorytest.R;
import com.prize.factorytest.PrizeFactoryTestListActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.view.KeyEvent;
import android.os.Message;

import android.hardware.Camera;
import android.util.Log;

public class CameraFrontYuvSub extends Activity {
	private TextView alsps;
	Handler mhandle;
	Timer mTimer;
	public static Button buttonPass;
	private Camera mCamera = null;
	private Camera mCamera1 = null;

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK){
			return true;
		}
		return false;
	}
	
	private String catDevPath(String path) {
		String result = null;
		try {
			CMDExecute cmdexe = new CMDExecute();
			String[] args = { "/system/bin/cat", path };
			result = cmdexe.run(args, "system/bin/");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}

	

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.camera_back_sub_als);
		
		alsps = (TextView) findViewById(R.id.als_ps);
		try {
			//mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
			mCamera1 = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
			catDevPath("/sys/kernel/spc/spc_f_open");
		} catch (Exception exception) {
			finish();
			mCamera = null;
			mCamera1 = null;
			Log.e("CameraBackYuvSub","onCreate() exception="+String.valueOf(exception));
		}
		//alsps.setText(getString(R.string.camera_back_sub_tip));
		mhandle = new Handler() {
			public void handleMessage(Message msg) {
				if (msg.what == 0x123) {
					 alsps.setText(getString(R.string.camera_back_sub_tip) + " : " + catDevPath("/sys/kernel/spc/spc_f_value"));

					if (!catDevPath("/sys/kernel/spc/spc_f_value").contains("No")&&!catDevPath("/sys/kernel/spc/spc_f_value").contains("-1")) {
						buttonPass.setEnabled(true);
					}
				}
			}
		};

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

	protected void onResume() {
		super.onResume();
		if (mTimer == null) {
			mTimer = new Timer();
		}
		mTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				mhandle.sendEmptyMessage(0x123);
			}
		}, 0, 300);
	}

	public void onPause() {
		super.onPause();
		if (mTimer != null) {
			mTimer.cancel();
			mTimer = null;
		}
	}

	public void finish() {
		Log.e("tangan","1");
		if (mCamera1 != null) {
			mCamera1.release();
			Log.e("tangan","2");
		}
		if (mCamera != null  ) {
			mCamera.release();	
			Log.e("tangan","3");
		}
		try{
			catDevPath("/sys/kernel/spc/spc_f_close");
			}catch(Exception e){
				Log.e("CameraBackYuvSub","finish() exception="+String.valueOf(e));
		}
		super.finish();
	}
}
