package com.prize.factorytest.LSensor;

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

public class LSensor extends Activity {

	private SensorManager mSensorManager = null;
	private Sensor mLSensor = null;
	private LSensorListener mLSensorListener;
	private TextView mTextView;
	private final static int SENSOR_TYPE = Sensor.TYPE_LIGHT;
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
			mSensorManager.unregisterListener(mLSensorListener, mLSensor);
		} catch (Exception e) {
		}
		super.finish();
	}

	void getService() {

		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		if (mSensorManager == null) {
			fail(getString(R.string.service_get_fail));
		}

		mLSensor = mSensorManager.getDefaultSensor(SENSOR_TYPE);
		if (mLSensor == null) {
			fail(getString(R.string.sensor_get_fail));
		}

		mLSensorListener = new LSensorListener(this);
		if (!mSensorManager.registerListener(mLSensorListener, mLSensor,
				SENSOR_DELAY)) {
			mSensorManager.registerListener(mLSensorListener, mLSensor,
					SENSOR_DELAY);
			fail(getString(R.string.sensor_register_fail));
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.lsensor);
		mTextView = (TextView) findViewById(R.id.lsensor_result);
		getService();
		mTextView.setText(getString(R.string.lsensor_result) + "\n" + "10");
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

	void fail(Object msg) {
		setResult(RESULT_CANCELED);
		finish();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		if (mSensorManager == null || mLSensorListener == null
				|| mLSensor == null)
			return;
		mSensorManager.unregisterListener(mLSensorListener, mLSensor);
	}

	public class LSensorListener implements SensorEventListener {
		public LSensorListener(Context context) {
			super();
		}

		public void onSensorChanged(SensorEvent event) {

			synchronized (this) {
				if (event.sensor.getType() == SENSOR_TYPE) {

					if (event.values[0] != lastValues) {
						ccount++;
						lastValues = event.values[0];
						mTextView.setText(getString(R.string.lsensor_result)
								+ "\n" + event.values[0]);
					}
					if (ccount >= 3 && event.values[0] <= 200) {
						buttonPass.setEnabled(true);
					}
				}
			}
		}

		public void onAccuracyChanged(Sensor arg0, int arg1) {

		}
	}
}
