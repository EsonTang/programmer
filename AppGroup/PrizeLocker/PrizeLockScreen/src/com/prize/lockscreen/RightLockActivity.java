package com.prize.lockscreen;

import java.util.Date;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.prize.lockscreen.interfaces.IUnLockListener;
import com.prize.lockscreen.receiver.HomeWatcherReceiver;
import com.prize.lockscreen.utils.LogUtil;
import com.prize.lockscreen.utils.TimeUtil;
import com.prize.lockscreen.widget.PasswordTextView;
import com.prize.lockscreen.widget.PullRightDoorView;
import com.prize.prizelockscreen.R;
/***
 * 右滑解锁 实现主activity
 * @author june
 *
 */
public class RightLockActivity extends BaseActivity {

	private PullRightDoorView mPullRightView;
	private int LOCK_SCREEN_TYPE = 1;
	private View maskView;
	private HomeWatcherReceiver mHomeKeyReceiver;
	private String mDateFormat;
	private TextView mDateView;
	private TextView mTimeView;
	
	private View mPinView;
	
	private PasswordTextView mPassTxt;
	
	private boolean havePassword = true;
	
	//public static final int FLAG_HOMEKEY_DISPATCHED = 0x80000000;
	
	private IUnLockListener mUnlockListener = new IUnLockListener() {
		@Override
		public void onUnlockFinish() {
			enterApp();
			finish();
		}

		@Override
		public boolean checkPwd(int pos) {
			mPos = pos;
			// 查询是否有密码, 若有则执行如下操作
			if (havePassword) {
				mPinView.setVisibility(View.VISIBLE);
				mPullRightView.setVisibility(View.GONE);
			}
			return havePassword;
		}
		@Override
		public void trueUnlock() {
			
		}
		@Override
		public boolean hasPwd() {
			return false;
		}
		@Override
		public String getPwd() {
			return null;
		}

		@Override
		public void emergencyUnlock() {
			// TODO Auto-generated method stub
			
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (LOCK_SCREEN_TYPE == 1) {
			//setContentView(R.layout.right_lock_lay);
			setContentView(R.layout.right_all_lock);
			mPinView = findViewById(R.id.pin_lay);
			mPullRightView = (PullRightDoorView) findViewById(R.id.pulldoor_layout);
			mPullRightView.setUnlockListener(mUnlockListener);
			ViewParent pv = mPullRightView.getParent();
			if (havePassword && pv instanceof FrameLayout) {
				BitmapDrawable d = new BitmapDrawable(getResources(), mPullRightView.getBgBitmap());
				((FrameLayout)pv).setBackground(d);
			}
		}
	}
	
	protected void init() {
		
		registerTimeReceiver();
		
		mDateFormat = getString(R.string.month_day_year_week);
		mDateView = (TextView) findViewById(R.id.text_date);
		mTimeView = (TextView) findViewById(R.id.text_time);
		mDateView.setText(DateFormat.format(mDateFormat, new Date()));
		mTimeView.setText(TimeUtil.getCurrentTime());
		
		mPassTxt = (PasswordTextView) findViewById(R.id.simPinEntry);
	}

	protected void updateTime() {
		mDateView.setText(DateFormat.format(mDateFormat, new Date()));
		mTimeView.setText(TimeUtil.getCurrentTime());
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
		mUnlockListener = null;
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
	
	public void onClick(View v) {
		switch (v.getId()) {
		//case R.id.key_enter:
		case 0:
			//校验密码的正确性
			String pass = mPassTxt.getText();
			LogUtil.i("aaa", "====" + pass);
			if ("1234".equals(pass)) {
				if (mPinView != null)
					mPinView.setVisibility(View.GONE);
				enterApp();
				finish();
			}
			else {
				mPassTxt.reset(true);
			}
			break;
		case R.id.delete_button:
			if (mPassTxt != null)
				mPassTxt.deleteLastChar();
			break;
		case R.id.txt_emc:
			Intent it = new Intent("com.android.phone.EmergencyDialer.DIAL");
			it.addCategory(Intent.CATEGORY_DEFAULT);
			it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
			startActivity(it);
			it = null;
			resetViewShow();
			break;
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		return super.onKeyDown(keyCode, event);
	}
	
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		
		if (event.getKeyCode() == KeyEvent.KEYCODE_HOME
				|| event.getKeyCode() == KeyEvent.KEYCODE_BACK
				|| event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_DOWN
				|| event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_UP) {
			if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
				resetViewShow();
			}
			return true;
		}
		return super.onKeyUp(keyCode, event);
	}
	/***
	 * 重新设置视图
	 */
	private void resetViewShow() {
		if (mPinView != null && View.VISIBLE == mPinView.getVisibility()) {
			mPinView.setVisibility(View.GONE);
			mPassTxt.reset(false);
			mPullRightView.scrollTo(0, 0);
			mPullRightView.setVisibility(View.VISIBLE);
			
//			int x = (int)mPullRightView.getLeft();
//			ObjectAnimator.ofFloat(mPullRightView, "translationX", x, 0).setDuration(300).start();
		} 
		else if (mPullRightView != null && (View.GONE == mPullRightView.getVisibility()
				|| View.INVISIBLE == mPullRightView.getVisibility())) {
			mPinView.setVisibility(View.GONE);
			mPassTxt.reset(false);
			mPullRightView.scrollTo(0, 0);
			mPullRightView.setVisibility(View.VISIBLE);
		}
		else {
			if (mPullRightView != null)
				mPullRightView.invalidate();
			if (mPinView != null)
				mPinView.invalidate();
		}
	}
}
