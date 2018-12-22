/*
 * Copyright (c) 2010-2011, The prize Open Source Community (www.prize.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.prize.compass;

import java.util.Locale;

import android.app.Activity;
import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AccelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.prize.compass.R;
import android.view.Window;
import android.view.WindowManager;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.graphics.Color;

public class CompassActivity extends Activity {
	private static final int EXIT_TIME = 2000;
	private final float MAX_ROATE_DEGREE = 1.0f;
	private SensorManager mSensorManager;
	private Sensor mOrientationSensor;
	private Sensor aOrientationSensor;
	float[] accelerometerValues = new float[3];
	float[] magneticFieldValues = new float[3];
	float[] values = new float[3];
	float[] B = new float[9];
	private float mDirection;
	private float mTargetDirection;
	private AccelerateInterpolator mInterpolator;
	protected final Handler mHandler = new Handler();
	private boolean mStopDrawing;
	private boolean mChinease;
	private long firstExitTime = 0L;

	LocationApplication application;
	View mCompassView;
	CompassView mPointer;

	TextView mLatitudeTV;
	TextView mLongitudeTV;
	TextView mCity;
	TextView mProvince;
	LinearLayout mDirectionLayout;
	LinearLayout mAngleLayout;
	View mViewGuide;
	ImageView mGuideAnimation;
	boolean flag=false;//第一次启动应用，已经校准了的，不再提示振动和Toast
	boolean calibration_success=true;//第一次启动应用，检测到干扰后，避免重复调用校准界面
	
	protected Handler invisiableHandler = new Handler() {
		public void handleMessage(Message msg) {
			mViewGuide.setVisibility(View.GONE);
		}
	};

	public void onWindowFocusChanged(boolean hasFocus) {
		AnimationDrawable anim = (AnimationDrawable) mGuideAnimation
				.getDrawable();
		anim.start();
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
				}

				updateDirection();

				mHandler.postDelayed(mCompassViewUpdater, 20);
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().requestFeature(Window.FEATURE_NO_TITLE);  
		if(VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {  
			Window window = getWindow();  
			window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS  
					| WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);  
			window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN  
					| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION  
					| View.SYSTEM_UI_FLAG_LAYOUT_STABLE);  
			window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);  
			window.setStatusBarColor(Color.TRANSPARENT);  
			window.setNavigationBarColor(Color.TRANSPARENT);  
		} 
		setContentView(R.layout.main);
		initResources();
		initServices();
		application = LocationApplication.getInstance();
		if (application != null) {
			application.mTv = mLatitudeTV;
			application.mmProvince = mProvince;
			application.mmCity = mCity;

			if (application.mData != null)
				mLatitudeTV.setText(application.mData);
			if (application.str_city != null && application.str_city != null)
				mProvince.setText(application.str_province);
			mCity.setText(application.str_city);
			application.mLocationClient.start();
		}
		
	}

	@Override
	public void onBackPressed() {
		long curTime = System.currentTimeMillis();
		if (curTime - firstExitTime < EXIT_TIME) {

			CompassActivity.this.finish();
			//android.os.Process.killProcess(android.os.Process.myPid()); 
		} else {
			Toast.makeText(this, R.string.guide_exit, Toast.LENGTH_SHORT)
					.show();
			firstExitTime = curTime;
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		if (mOrientationSensor != null && aOrientationSensor != null) {
			mSensorManager.registerListener(mOrientationSensorEventListener,
					mOrientationSensor, SensorManager.SENSOR_DELAY_GAME);
			mSensorManager.registerListener(mOrientationSensorEventListener,
					aOrientationSensor, SensorManager.SENSOR_DELAY_GAME);
		} else {
			// Toast.makeText(this, R.string.cannot_get_sensor,
			// Toast.LENGTH_SHORT)
			// .show();
		}
		mStopDrawing = false;
		mHandler.postDelayed(mCompassViewUpdater, 60);
		application.mLocationClient.start();
		
	}

	@Override
	protected void onPause() {
		super.onPause();
		mStopDrawing = true;
		if (mOrientationSensor != null && aOrientationSensor != null) {
			mSensorManager.unregisterListener(mOrientationSensorEventListener);
		}
		application.mLocationClient.stop();
	}


	private void initResources() {
		mViewGuide = findViewById(R.id.view_guide);
		//mViewGuide.setVisibility(View.VISIBLE);
		//invisiableHandler.sendMessageDelayed(new Message(), 3000);
		mGuideAnimation = (ImageView) findViewById(R.id.guide_animation);
		mDirection = 0.0f;
		mTargetDirection = 0.0f;
		mInterpolator = new AccelerateInterpolator();
		mStopDrawing = true;
		mChinease = TextUtils.equals(Locale.getDefault().getLanguage(), "zh");

		mPointer = (CompassView) findViewById(R.id.compass_pointer);

		mCity = (TextView) findViewById(R.id.city);
		mProvince = (TextView) findViewById(R.id.province);
		mLatitudeTV = (TextView) findViewById(R.id.textview_location_latitude_degree);
		mDirectionLayout = (LinearLayout) findViewById(R.id.layout_direction);
		mAngleLayout = (LinearLayout) findViewById(R.id.layout_angle);
	}

	private void initServices() {
		// sensor manager
		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		// mOrientationSensor = mSensorManager.getSensorList(
		// Sensor.TYPE_ORIENTATION).get(0);
		aOrientationSensor = mSensorManager
				.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		mOrientationSensor = mSensorManager
				.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

	}

	private void updateDirection() {
		LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		mDirectionLayout.removeAllViews();
		mAngleLayout.removeAllViews();

		ImageView east = null;
		ImageView west = null;
		ImageView south = null;
		ImageView north = null;
		float direction = normalizeDegree(mTargetDirection * -1.0f);
		if (direction > 22.5f && direction < 157.5f) {
			// east
			east = new ImageView(this);
			east.setImageResource(mChinease ? R.drawable.e_cn : R.drawable.e);
			east.setLayoutParams(lp);
		} else if (direction > 202.5f && direction < 337.5f) {
			// west
			west = new ImageView(this);
			west.setImageResource(mChinease ? R.drawable.w_cn : R.drawable.w);
			west.setLayoutParams(lp);
		}

		if (direction > 112.5f && direction < 247.5f) {
			// south
			south = new ImageView(this);
			south.setImageResource(mChinease ? R.drawable.s_cn : R.drawable.s);
			south.setLayoutParams(lp);
		} else if (direction < 67.5 || direction > 292.5f) {
			// north
			north = new ImageView(this);
			north.setImageResource(mChinease ? R.drawable.n_cn : R.drawable.n);
			north.setLayoutParams(lp);
		}

		if (mChinease) {
			// east/west should be before north/south
			if (east != null) {
				mDirectionLayout.addView(east);
			}
			if (west != null) {
				mDirectionLayout.addView(west);
			}
			if (south != null) {
				mDirectionLayout.addView(south);
			}
			if (north != null) {
				mDirectionLayout.addView(north);
			}
		} else {
			// north/south should be before east/west
			if (south != null) {
				mDirectionLayout.addView(south);
			}
			if (north != null) {
				mDirectionLayout.addView(north);
			}
			if (east != null) {
				mDirectionLayout.addView(east);
			}
			if (west != null) {
				mDirectionLayout.addView(west);
			}
		}
		int direction2 = (int) direction;
		boolean show = false;
		if (direction2 >= 100) {
			mAngleLayout.addView(getNumberImage(direction2 / 100));
			direction2 %= 100;
			show = true;
		}
		if (direction2 >= 10 || show) {
			mAngleLayout.addView(getNumberImage(direction2 / 10));
			direction2 %= 10;
		}
		mAngleLayout.addView(getNumberImage(direction2));
		ImageView degreeImageView = new ImageView(this);
		degreeImageView.setImageResource(R.drawable.degree);
		degreeImageView.setLayoutParams(lp);
		mAngleLayout.addView(degreeImageView);
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

	
	private SensorEventListener mOrientationSensorEventListener = new SensorEventListener() {

		@Override
		public void onSensorChanged(SensorEvent event) {

			if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
				accelerometerValues = event.values;
			}
			if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
				magneticFieldValues = event.values;	
				
					if(event.accuracy<2&&calibration_success==true){//增加干扰检测灵敏度，第一次启动应用的时候，立马检测一次。
						Log.d("tyy", "检测到干扰，开始校准:"+"地磁场精度："+event.accuracy);
						mOrientationSensorEventListener.onAccuracyChanged(event.sensor, event.accuracy);//调用onAccuracyChanged
						
					}
			}

			SensorManager.getRotationMatrix(B, null, accelerometerValues,
					magneticFieldValues);
			SensorManager.getOrientation(B, values);

			values[0] = (float) Math.toDegrees(values[0])* -1.0f;
			mTargetDirection =normalizeDegree( values[0]);
		}

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
			
			if(sensor.getType()== Sensor.TYPE_MAGNETIC_FIELD){//地磁场
				Log.d("tyy", "3accuracy: "+accuracy);
				if(accuracy<2){//磁场干扰
					mViewGuide.setVisibility(View.VISIBLE);
					flag=true;
					calibration_success=false;
				}

				if(accuracy>=2){//校准成功
						mViewGuide.setVisibility(View.GONE);
						if(flag==true){//画“8”字校准后,校准成功给振动提示,第一次进入指南针，检测到已经校准，则不走该流程
							flag=false;
							calibration_success=true;
							Toast.makeText(CompassActivity.this, "校准成功", Toast.LENGTH_SHORT).show();
							Vibrator vibrator=(Vibrator)getSystemService(Service.VIBRATOR_SERVICE);
							vibrator.vibrate(new long[]{0,500},-1);
							Log.d("tyy", "校准成功");
						}
				}
			}
		}
	};


	private float normalizeDegree(float degree) {
		return (degree + 720) % 360;
	}

	@Override
	protected void onDestroy() {
		Log.d("tyy", "onDestroy");
		application =null;
		super.onDestroy();
	}

}
