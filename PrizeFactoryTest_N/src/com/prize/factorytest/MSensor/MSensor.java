package com.prize.factorytest.MSensor;

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

public class MSensor extends Activity {

	private SensorManager mSensorManager = null;
	private Sensor mMSensor = null;
	private MSensorListener mMSensorListener;
	TextView mTextView;
	Button cancelButton;
	private final static String INIT_VALUE = "";
	private static String value = INIT_VALUE;
	private final static int SENSOR_TYPE = Sensor.TYPE_MAGNETIC_FIELD;
	private final static int SENSOR_DELAY = SensorManager.SENSOR_DELAY_FASTEST;
	private static Button buttonPass;
	static float lastValues = 0;
	int ccount = 0;	

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
			mSensorManager.unregisterListener(mMSensorListener, mMSensor);
		} catch (Exception e) {
		}
		super.finish();
	}

	void getService() {
		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		if (mSensorManager == null) {
			fail(getString(R.string.service_get_fail));
		}

		mMSensor = mSensorManager.getDefaultSensor(SENSOR_TYPE);
		if (mMSensor == null) {
			fail(getString(R.string.sensor_get_fail));
		}

		mMSensorListener = new MSensorListener(this);
		if (!mSensorManager.registerListener(mMSensorListener, mMSensor,
				SENSOR_DELAY)) {
			mSensorManager.registerListener(mMSensorListener, mMSensor,
					SENSOR_DELAY);
			fail(getString(R.string.sensor_register_fail));
		}
	}

	void updateView(Object s) {
		mTextView.setText(getString(R.string.msensor_name) + " : " + s);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.msensor);
		mTextView = (TextView) findViewById(R.id.msensor_result);
		getService();

		updateView(value);
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
		toast(msg);
		setResult(RESULT_CANCELED);
		finish();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		if (mSensorManager == null || mMSensorListener == null
				|| mMSensor == null)
			return;
		mSensorManager.unregisterListener(mMSensorListener, mMSensor);
	}

	public class MSensorListener implements SensorEventListener {
		public MSensorListener(Context context) {
			super();
		}

		public void onSensorChanged(SensorEvent event) {
			
			synchronized (this) {
				TextView msensor = (TextView) findViewById(R.id.msensor_result);

				if (event.sensor.getType() == SENSOR_TYPE) {
									
					if(event.values[0] != lastValues){
						ccount++;
						lastValues = event.values[0];
						msensor.setText(getString(R.string.msensor_data) + "\n"
								+ "x: " + event.values[0]
								+ "y: " + event.values[1]
								+ "z: " + event.values[2]);
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
	public void toast(Object s) {

		if (s == null)
			return;
		Toast.makeText(this, s + "", Toast.LENGTH_SHORT).show();
	}
}
