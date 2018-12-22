package com.prize.lockscreen;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.TextView;

import com.prize.lockscreen.service.StatusViewManager;
import com.prize.lockscreen.utils.TimeUtil;
import com.prize.lockscreen.widget.StarLockView;
import com.prize.prizelockscreen.R;

/***
 * 圆圈滑动解锁
 */
public class BootActivity extends BaseActivity {
	private static final boolean DBG = true;
	private static final String TAG = "MainActivity";
	public static final int MSG_LAUNCH_HOME = 0;
	public static final int MSG_LAUNCH_DIAL = 1;
	public static final int MSG_LAUNCH_SMS = 2;
	public static final int MSG_LAUNCH_CAMERA = 3;

	private StarLockView mLockView;
	public static StatusViewManager mStatusViewManager;

	private String mDateFormat;
	private TextView mDateView;
	private TextView mTimeView;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (DBG)
			Log.d(TAG, "onCreate()");

		setContentView(R.layout.circle_unlock_lay);
	}

	protected void init() {
		initViews();
		mStatusViewManager = new StatusViewManager(this,
				this.getApplicationContext());
		mLockView.setMainHandler(mHandler);

		registerTimeReceiver();
	}

	// 接收来自StarLockView发送的消息，处理解锁、启动相关应用的功能
	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch (msg.what) {
			case MSG_LAUNCH_HOME:
				finish();
				break;
			case MSG_LAUNCH_SMS:
				launchSms();
				finish();
				break;
			case MSG_LAUNCH_DIAL:
				launchDial();
				finish();
				break;
			case MSG_LAUNCH_CAMERA:
				launchCamera();
				finish();
				break;
			}
		}

	};

	// 启动短信应用
	private void launchSms() {

		// mFocusView.setVisibility(View.GONE);
		Intent intent = new Intent();
		ComponentName comp = new ComponentName("com.android.mms",
				"com.android.mms.ui.ConversationList");
		intent.setComponent(comp);
		intent.setAction("android.intent.action.VIEW");
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
		startActivity(intent);
	}

	// 启动拨号应用
	private void launchDial() {
		Intent intent = new Intent(Intent.ACTION_DIAL);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
		startActivity(intent);
	}

	// 启动相机应用
	private void launchCamera() {
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		intent.addCategory(Intent.CATEGORY_DEFAULT);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
		startActivity(intent);
	}

	// 使home物理键失效
	@Override
	public void onAttachedToWindow() {
		// TODO Auto-generated method stub
		super.onAttachedToWindow();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if (DBG)
			Log.d(TAG, "onDestroy()");
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if (DBG)
			Log.d(TAG, "onResume()");
	}

	@Override
	public void onDetachedFromWindow() {
		// TODO Auto-generated method stub
		super.onDetachedFromWindow();
		if (DBG)
			Log.d(TAG, "onDetachedFromWindow()");
	}

	public void initViews() {
		// TODO Auto-generated method stub
		mLockView = (StarLockView) findViewById(R.id.four_group);
		
		mDateFormat = getString(R.string.month_day_year_week);
		mDateView = (TextView) findViewById(R.id.text_date);
		mTimeView = (TextView) findViewById(R.id.text_time);
		mDateView.setText(DateFormat.format(mDateFormat, TimeUtil.getTime()));
		mTimeView.setText(TimeUtil.getCurrentTime());
	}

	protected void updateTime() {
		mDateView.setText(DateFormat.format(mDateFormat, TimeUtil.getTime()));
		mTimeView.setText(TimeUtil.getCurrentTime());
	}

}