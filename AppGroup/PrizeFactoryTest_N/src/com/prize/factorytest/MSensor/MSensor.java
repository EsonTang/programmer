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
import android.widget.Toast;
import android.view.KeyEvent;
import android.widget.LinearLayout;
import android.widget.ImageView;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AccelerateInterpolator;
import android.os.Handler;
import android.util.Log;

public class MSensor extends Activity {

	private SensorManager mSensorManager = null;
	private Sensor mMSensor = null;
	private Sensor mGSensor = null;
	private OSensorListener mOSensorListener;
	private float[] valueTemp = new float[9];
	private float[] accelerometerValues = new float[3];
	private float[] magneticFieldValues = new float[3];
	private float mDirection;
	private float mTargetDirection;
	private LinearLayout mValueLayout;
	private LinearLayout mDirectionLayout;
	private CompassView mPointer = null;
	private boolean mStopDrawing = false;
	private static Button buttonPass;
	private final float MAX_ROATE_DEGREE = 1.0f;
	protected final Handler mHandler = new Handler();
	float[] values = new float[3];
	private AccelerateInterpolator mInterpolator;

	void getService() {
		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		if (mSensorManager == null) {
			fail(getString(R.string.service_get_fail));
		}

		mMSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
		mGSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		if (mMSensor == null || mGSensor == null) {
			fail(getString(R.string.sensor_get_fail));
		}

		mOSensorListener = new OSensorListener(this);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.e("liup","onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.msensor);
		
		getService();
		mInterpolator = new AccelerateInterpolator();
		
		mDirectionLayout = (LinearLayout) findViewById(R.id.layout_direction);
		mPointer = (CompassView) findViewById(R.id.compass_pointer);
		mValueLayout = (LinearLayout) findViewById(R.id.layout_value);	

		confirmButton();

	}

	protected Runnable mCompassViewUpdater = new Runnable() {
		@Override
		public void run() {
			if (mPointer != null && !mStopDrawing) {
				if (mDirection != mTargetDirection) {

					// calculate the short routine
					float to = mTargetDirection;
					if (to - mDirection > 180) {
						to -= 360;
					} else if (to - mDirection < -180) {
						to += 360;
					}

					// limit the max speed to MAX_ROTATE_DEGREE
					float distance = to - mDirection;
					if (Math.abs(distance) > MAX_ROATE_DEGREE) {
						distance = distance > 0 ? MAX_ROATE_DEGREE
								: (-1.0f * MAX_ROATE_DEGREE);
					}

					// need to slow down if the distance is short
					mDirection = normalizeDegree(mDirection
							+ ((to - mDirection) * mInterpolator
									.getInterpolation(Math.abs(distance) > MAX_ROATE_DEGREE ? 0.4f
											: 0.3f)));
					mPointer.updateDirection(mDirection);
					buttonPass.setEnabled(true);
				}

				updateDirection();

				mHandler.postDelayed(mCompassViewUpdater, 20);
			}
		}
	};
	
	private void updateDirection() {
		LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		mDirectionLayout.removeAllViews();
		mValueLayout.removeAllViews();

		ImageView east = null;
		ImageView west = null;
		ImageView south = null;
		ImageView north = null;
		float direction = normalizeDegree(mTargetDirection * -1.0f);
		
		if (direction > 22.5f && direction < 157.5f) {
			// east
			east = new ImageView(this);
			east.setImageResource(R.drawable.e_cn);
			east.setLayoutParams(lp);
			mDirectionLayout.addView(east);
		} else if (direction > 202.5f && direction < 337.5f) {
			// west
			west = new ImageView(this);
			west.setImageResource(R.drawable.w_cn);
			west.setLayoutParams(lp);
			mDirectionLayout.addView(west);
		}

		if (direction > 112.5f && direction < 247.5f) {
			// south
			south = new ImageView(this);
			south.setImageResource(R.drawable.s_cn);
			south.setLayoutParams(lp);
			mDirectionLayout.addView(south);
		} else if (direction < 67.5 || direction > 292.5f) {
			// north
			north = new ImageView(this);
			north.setImageResource(R.drawable.n_cn);
			north.setLayoutParams(lp);
			mDirectionLayout.addView(north);
		}
		
		int direction2 = (int) direction;
		boolean show = false;
		if (direction2 >= 100) {
			mValueLayout.addView(getNumberImage(direction2 / 100));
			direction2 %= 100;
			show = true;
		}
		if (direction2 >= 10 || show) {
			mValueLayout.addView(getNumberImage(direction2 / 10));
			direction2 %= 10;
		}
		
		mValueLayout.addView(getNumberImage(direction2));
		ImageView degreeImageView = new ImageView(this);
		degreeImageView.setImageResource(R.drawable.degree);
		degreeImageView.setLayoutParams(lp);
		mValueLayout.addView(degreeImageView);
	}
	
	private ImageView getNumberImage(int number) {
		ImageView image = new ImageView(this);
		LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		switch (number) {
		case 0:
			image.setImageResource(R.drawable.number_0);
			break;
		case 1:
			image.setImageResource(R.drawable.number_1);
			break;
		case 2:
			image.setImageResource(R.drawable.number_2);
			break;
		case 3:
			image.setImageResource(R.drawable.number_3);
			break;
		case 4:
			image.setImageResource(R.drawable.number_4);
			break;
		case 5:
			image.setImageResource(R.drawable.number_5);
			break;
		case 6:
			image.setImageResource(R.drawable.number_6);
			break;
		case 7:
			image.setImageResource(R.drawable.number_7);
			break;
		case 8:
			image.setImageResource(R.drawable.number_8);
			break;
		case 9:
			image.setImageResource(R.drawable.number_9);
			break;
		}
		image.setLayoutParams(lp);
		return image;
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

	public class OSensorListener implements SensorEventListener {
		public OSensorListener(Context context) {
			super();
		}

		public void onSensorChanged(SensorEvent event) {
			
			synchronized (this) {

				if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
					accelerometerValues = event.values;		
				}
				if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {				
					magneticFieldValues = event.values;
				}
				
				SensorManager.getRotationMatrix(valueTemp, null, accelerometerValues,
					magneticFieldValues);
				SensorManager.getOrientation(valueTemp, values);

				values[0] = (float) Math.toDegrees(values[0])* -1.0f;
				mTargetDirection = normalizeDegree(values[0]);
			}
		}

		public void onAccuracyChanged(Sensor arg0, int arg1) {

		}
	}
	
	private float normalizeDegree(float degree) {
		return (degree + 720) % 360;
	}
	
	@Override
	protected void onResume() {
		Log.e("liup","onResume");
		if (!mSensorManager.registerListener(mOSensorListener, mMSensor,
				SensorManager.SENSOR_DELAY_GAME)) {
			mSensorManager.registerListener(mOSensorListener, mMSensor,
					SensorManager.SENSOR_DELAY_GAME);
			fail(getString(R.string.sensor_register_fail));
		}
		if (!mSensorManager.registerListener(mOSensorListener, mGSensor,
				SensorManager.SENSOR_DELAY_GAME)) {
			mSensorManager.registerListener(mOSensorListener, mGSensor,
					SensorManager.SENSOR_DELAY_GAME);
			fail(getString(R.string.sensor_register_fail));
		}
		mStopDrawing = false;
		mHandler.postDelayed(mCompassViewUpdater, 60);
		super.onResume();
	}
	
	@Override
	public void onPause() {
		Log.e("liup","onPause");
		mStopDrawing = true;
		try {
			mSensorManager.unregisterListener(mOSensorListener, mMSensor);
			mSensorManager.unregisterListener(mOSensorListener, mGSensor);
		} catch (Exception e) {
		}
		super.onPause();
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK){
			return true;
		}
		return false;
	}
	
	void fail(Object msg) {
		toast(msg);
		setResult(RESULT_CANCELED);
		finish();
	}
	
	public void finish() {
		mPointer = null;
		mHandler.removeCallbacks(mCompassViewUpdater);
		super.finish();
	}
		
	public void toast(Object s) {
		if (s == null)
			return;
		Toast.makeText(this, s + "", Toast.LENGTH_SHORT).show();
	}
}
