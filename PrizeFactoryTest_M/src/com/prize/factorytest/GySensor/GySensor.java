package com.prize.factorytest.GySensor;

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
import android.widget.ImageView;

public class GySensor extends Activity {

	private SensorManager mSensorManager = null;
	private Sensor mMSensor = null;
	private GySensorListener mGySensorListener;
	TextView mTextView;
	Button cancelButton;
	private final static String INIT_VALUE = "";
	private static String value = INIT_VALUE;
	private final static int SENSOR_TYPE = Sensor.TYPE_GYROSCOPE;
	private final static int SENSOR_DELAY = SensorManager.SENSOR_DELAY_FASTEST;

	private static Button buttonPass;

	@Override
	public void finish() {
		try {
			mSensorManager.unregisterListener(mGySensorListener, mMSensor);
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

		mGySensorListener = new GySensorListener(this);
		if (!mSensorManager.registerListener(mGySensorListener, mMSensor,
				SENSOR_DELAY)) {
			mSensorManager.registerListener(mGySensorListener, mMSensor,
					SENSOR_DELAY);
			fail(getString(R.string.sensor_register_fail));
		}
	}

	void updateView(Object s) {

		mTextView.setText(getString(R.string.gysensor_name) + " : " + s);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		//getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_GESTURE_ISOLATED);
		setContentView(R.layout.gysensor);
		mTextView = (TextView) findViewById(R.id.gysensor_result);
		getService();

		updateView(value);
		showDialog();

	}

	public void showDialog() {
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

	void pass() {

		setResult(RESULT_OK);
		finish();
	}

	@Override
	protected void onDestroy() {

		super.onDestroy();

		if (mSensorManager == null || mGySensorListener == null
				|| mMSensor == null)
			return;
		mSensorManager.unregisterListener(mGySensorListener, mMSensor);
	}
	
	int ccount = 0;	
	public class GySensorListener implements SensorEventListener {
		public GySensorListener(Context context) {
			super();
		}

		public void onSensorChanged(SensorEvent event) {
			
			synchronized (this) {
				TextView msensor = (TextView) findViewById(R.id.gysensor_result);
				ImageView gysensor = (ImageView) findViewById(R.id.gysensor_image);
				if (event.sensor.getType() == SENSOR_TYPE) {
					msensor.setText(getString(R.string.gysensor_data) + "\n"
							+ "x: " + event.values[0]
							+ "y: " + event.values[1]
							+ "z: " + event.values[2]);
					if(event.values[0] > 3){
						gysensor.setBackgroundResource(R.drawable.gsensor_up);
					}else if(event.values[0] < -3){
						gysensor.setBackgroundResource(R.drawable.gsensor_down);
					}	
					if(event.values[1] > 3){
						gysensor.setBackgroundResource(R.drawable.gsensor_right);
					}else if(event.values[1] < -3){
						gysensor.setBackgroundResource(R.drawable.gsensor_left);
					}		
					if(event.values[2] > 3){
						gysensor.setBackgroundResource(R.drawable.gsensor_left);
					}else if(event.values[2] < -3){
						gysensor.setBackgroundResource(R.drawable.gsensor_right);
					}	
					buttonPass.setEnabled(true);
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
