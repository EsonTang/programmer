package com.prize.factorytest.PSensor;

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
import android.widget.Toast;
import android.view.KeyEvent;

public class PSensor extends Activity {

	private SensorManager mSensorManager = null;
	private Sensor mPSensor = null;
	private PSensorListener mPSensorListener;
	TextView mTextView;
	Button cancelButton;
	private final static String INIT_VALUE = "";
	private static String value = INIT_VALUE;
	private final static int SENSOR_TYPE = Sensor.TYPE_PROXIMITY;
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
			mSensorManager.unregisterListener(mPSensorListener, mPSensor);
		} catch (Exception e) {
		}
		super.finish();
	}

	void getService() {

		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		if (mSensorManager == null) {
			fail(getString(R.string.service_get_fail));
		}

		mPSensor = mSensorManager.getDefaultSensor(SENSOR_TYPE);
		if (mPSensor == null) {
			fail(getString(R.string.sensor_get_fail));
		}

		mPSensorListener = new PSensorListener(this);
		if (!mSensorManager.registerListener(mPSensorListener, mPSensor,
				SENSOR_DELAY)) {
			mSensorManager.registerListener(mPSensorListener, mPSensor,
					SENSOR_DELAY);
			fail(getString(R.string.sensor_register_fail));
		}
	}

	void updateView(Object s) {

		mTextView.setText(R.string.psensor_result + " : " + s);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.psensor);
		mTextView = (TextView) findViewById(R.id.psensor_result);
		getService();
		updateView(value);
		confirmButton();
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

	void fail(Object msg) {
		toast(msg);
		setResult(RESULT_CANCELED);
		finish();
	}
	@Override
	protected void onDestroy() {

		super.onDestroy();

		if (mSensorManager == null || mPSensorListener == null
				|| mPSensor == null)
			return;
		mSensorManager.unregisterListener(mPSensorListener, mPSensor);
	}

	public class PSensorListener implements SensorEventListener {

		public PSensorListener(Context context) {
			super();
		}

		public void onSensorChanged(SensorEvent event) {
			synchronized (this) {
				TextView psensor = (TextView) findViewById(R.id.psensor_result);
				if (event.sensor.getType() == SENSOR_TYPE) {
					psensor.setText(getString(R.string.psensor_result)
							+ event.values[0]);
					if (event.values[0] != lastValues) {
						ccount++;
						lastValues = event.values[0];
					}
					if (ccount >= 2) {
						buttonPass.setEnabled(true);
					}
				}
			}
		}

		public void onAccuracyChanged(Sensor arg0, int arg1) {
		}
	}

	public void toast(Object s) {
		if (s == null)
			return;
		Toast.makeText(this, s + "", Toast.LENGTH_SHORT).show();
	}
}
