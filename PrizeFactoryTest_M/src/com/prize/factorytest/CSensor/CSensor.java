package com.prize.factorytest.CSensor;

import com.prize.factorytest.R;
import com.prize.factorytest.PrizeFactoryTestListActivity;
import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.view.KeyEvent;
import java.io.IOException;
import android.util.Log;

public class CSensor extends Activity {

	private SensorManager mSensorManager = null;
	private Sensor mCSensor = null;
	private CSensorListener mCSensorListener;
	TextView mTextView;
	private final static String INIT_VALUE = "";
	private static String value = INIT_VALUE;
	private final static int SENSOR_TYPE = Sensor.TYPE_STEP_COUNTER;
	private final static int SENSOR_DELAY = SensorManager.SENSOR_DELAY_FASTEST;
	private static Button buttonPass;
	private static float lastValues = 0;
	private int ccount = 0;

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK){
			return true;
		}
		return false;
	}
	
	@Override
	public void finish() {
		try {
			mSensorManager.unregisterListener(mCSensorListener, mCSensor);
		} catch (Exception e) {
		}
		super.finish();
	}

	void getService() {
		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		if (mSensorManager == null) {
			fail(getString(R.string.service_get_fail));
		}

		mCSensor = mSensorManager.getDefaultSensor(SENSOR_TYPE);
		if (mCSensor == null) {
			fail(getString(R.string.sensor_get_fail));
		}

		mCSensorListener = new CSensorListener(this);
		if (!mSensorManager.registerListener(mCSensorListener, mCSensor,
				SENSOR_DELAY)) {
			mSensorManager.registerListener(mCSensorListener, mCSensor,
					SENSOR_DELAY);
			fail(getString(R.string.sensor_register_fail));
		}
	}

	void updateView(Object s) {
		mTextView.setText(getString(R.string.csensor_name) + " : " + s);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.csensor);
		mTextView = (TextView) findViewById(R.id.csensor_result);
		getService();

		updateView(value);
		confirmButton();
	}
	@Override
	protected void onResume() {
		Log.e("liup","onResume");
		try {	
    		String[] cmdMode = new String[]{"/system/bin/sh","-c","echo" + " " + 0 + " > /proc/dummystep"};
			Runtime.getRuntime().exec(cmdMode);		
			Log.e("liup","onResume success");		
		} catch (IOException e) {
			e.printStackTrace();
		}
		super.onResume();
	}
	
	@Override
	public void onPause() {
		Log.e("liup","onPause");
		try {	
    		String[] cmdMode = new String[]{"/system/bin/sh","-c","echo" + " " + 1 + " > /proc/dummystep"};
			Runtime.getRuntime().exec(cmdMode);				
			Log.e("liup","onPause success");		
		} catch (IOException e) {
			e.printStackTrace();
		}
		super.onPause();
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

	void fail(Object msg) {
		setResult(RESULT_CANCELED);
		finish();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		if (mSensorManager == null || mCSensorListener == null
				|| mCSensor == null)
			return;
		mSensorManager.unregisterListener(mCSensorListener, mCSensor);
	}

	public class CSensorListener implements SensorEventListener {
		public CSensorListener(Context context) {
			super();
		}

		public void onSensorChanged(SensorEvent event) {
			
			synchronized (this) {
				if (event.sensor.getType() == SENSOR_TYPE) {
									
					if(event.values[0] != lastValues){
						ccount++;
						lastValues = event.values[0];
						mTextView.setText(getString(R.string.csensor_data)
								+ "\n" + event.values[0]);
					}
					if(ccount >= 3){
						buttonPass.setEnabled(true);
					}
				}
			}
		}

		public void onAccuracyChanged(Sensor arg0, int arg1) {

		}
	}
}
