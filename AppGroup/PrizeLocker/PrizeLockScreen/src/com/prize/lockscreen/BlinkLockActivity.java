package com.prize.lockscreen;

import java.util.Date;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.TextView;

import com.prize.lockscreen.interfaces.IEnterApp;
import com.prize.lockscreen.interfaces.IUnLockListener;
import com.prize.lockscreen.receiver.HomeWatcherReceiver;
import com.prize.lockscreen.utils.TimeUtil;
import com.prize.lockscreen.widget.BlinkRelativeView;
import com.prize.lockscreen.widget.PullRightDoorView;
import com.prize.prizelockscreen.R;
/***
 * 眨眼解锁 实现主activity
 * @author june
 *
 */
public class BlinkLockActivity extends BaseActivity implements IEnterApp {

	private PullRightDoorView mPullRightView;
	private int LOCK_SCREEN_TYPE = 1;
	private View maskView;
	private HomeWatcherReceiver mHomeKeyReceiver;
	private String mDateFormat;
	private TextView mDateView;
	private TextView mTimeView;
	
	private View mPhoneView, mCameraView, mHomeView;
	
	private View mPhoneViewTop, mCameraViewTop, mHomeViewTop;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (LOCK_SCREEN_TYPE == 1) {
			 setContentView(R.layout.blink_relative_lay);
//			setContentView(R.layout.pin_code_lay);
			/*mPullRightView = (PullRightDoorView) findViewById(R.id.pulldoor_layout);
			mPullRightView.setUnlockListener(new IUnLockListener() {
				@Override
				public void onUnlockFinish() {
					finish();
				}

				@Override
				public boolean checkPwd() {
					return false;
				}
			});*/
		}
	}
	
	protected void init() {
		
		registerTimeReceiver();
		BlinkRelativeView blinkView = (BlinkRelativeView)findViewById(R.id.content_view);
		blinkView.setEnterApp(this);
		blinkView.setUnlockListener(new IUnLockListener() {
			
			@Override
			public void onUnlockFinish() {
				finish();
			}
			
			@Override
			public boolean checkPwd(int which) {
				return false;
			}
			
			@Override
			public String getPwd() {
				return "";
			}

			@Override
			public void trueUnlock() {
				
			}

			@Override
			public boolean hasPwd() {
				return false;
			}

			@Override
			public void emergencyUnlock() {
				// TODO Auto-generated method stub
				
			}
			
		});
		
		mDateFormat = getString(R.string.month_day_year_week);
		mDateView = (TextView) findViewById(R.id.text_date);
		mTimeView = (TextView) findViewById(R.id.text_time);
		mDateView.setText(DateFormat.format(mDateFormat, new Date()));
		mTimeView.setText(TimeUtil.getCurrentTime());
		
		mPhoneView = findViewById(R.id.img_phone);
		mCameraView = findViewById(R.id.img_camera);
		mHomeView = findViewById(R.id.img_home);
		
		mPhoneViewTop = findViewById(R.id.img_phone_top);
		mCameraViewTop = findViewById(R.id.img_camera_top);
		mHomeViewTop = findViewById(R.id.img_home_top);
	}

	protected void updateTime() {
		mDateView.setText(DateFormat.format(mDateFormat, new Date()));
		mTimeView.setText(TimeUtil.getCurrentTime());
	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.img_home:
			
			int homeTop = mHomeView.getTop();
			int homeBottom = mHomeViewTop.getBottom();
			int trianHeight = 372; //392
			int scrHeight = 1280;
			
			int upY = (scrHeight -  trianHeight) / 2 - homeTop;
			
			int down = (scrHeight +  trianHeight) / 2 - homeBottom;
			AnimatorSet mAnimateSet = new AnimatorSet();
			//设置动画
			
//			mAnimateSet.playSequentially(ObjectAnimator.ofFloat(mHomeView, "translationY", 0f, 100, 0), ObjectAnimator.ofFloat(mCameraView, "translationY", 0f, 100, 0),
//					ObjectAnimator.ofFloat(mPhoneView, "translationY", 0f, 100, 0));
			
//			mAnimateSet.playTogether(ObjectAnimator.ofFloat(mHomeView, "translationY", 0f, upY),
//					ObjectAnimator.ofFloat(mHomeViewTop, "translationY", 0f, down));
//			//设置动画时间
//			mAnimateSet.setDuration(1000);
//			
//			mAnimateSet.start();
			break;
		/*case R.id.key_enter:
			finish();
			break;*/
		}
	}
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
	}

	@Override
	protected void onStart() {
		super.onStart();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		registerHomeKeyReceiver(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		unregisterHomeKeyReceiver(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onAttachedToWindow() {
		super.onAttachedToWindow();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return super.onTouchEvent(event);
	}

	private void registerHomeKeyReceiver(Context context) {
		mHomeKeyReceiver = new HomeWatcherReceiver();
		IntentFilter homeFilter = new IntentFilter(
				Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
		context.registerReceiver(mHomeKeyReceiver, homeFilter);
	}

	private void unregisterHomeKeyReceiver(Context context) {
		context.unregisterReceiver(mHomeKeyReceiver);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		finish();
		return false;
	}
	
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		return false;
	}

	@Override
	public void enterApp(int which) {
		// TODO Auto-generated method stub
		finish();
	}
}
