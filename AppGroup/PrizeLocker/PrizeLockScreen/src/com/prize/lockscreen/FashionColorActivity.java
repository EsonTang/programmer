package com.prize.lockscreen;

import java.util.Date;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.MotionEvent;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.TextView;

import com.prize.lockscreen.interfaces.IUnLockListener;
import com.prize.lockscreen.receiver.HomeWatcherReceiver;
import com.prize.lockscreen.utils.TimeUtil;
import com.prize.lockscreen.widget.ColorBubbleView;
import com.prize.prizelockscreen.R;
/***
 * 流行色锁屏 实现主activity
 * @author fanjunchen
 *
 */
public class FashionColorActivity extends BaseActivity {

	private ColorBubbleView mBubbleView;
	private int LOCK_SCREEN_TYPE = 1;// [1:向上滑动解锁方式，2：气泡解锁方式]
	private TextView tvHint;
	public static final String ACTION_NLS_UPDATE = "com.prize.notificationlistener.NLSUPDATE";
	private HomeWatcherReceiver mHomeKeyReceiver;
	private String mDateFormat;
	private TextView mDateView;
	private TextView mTimeView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);

		if (LOCK_SCREEN_TYPE == 1) {
			setContentView(R.layout.fashion_color_lay);
			Animation ani = new AlphaAnimation(0f, 1f);
			ani.setDuration(1500);
			ani.setRepeatMode(Animation.REVERSE);
			ani.setRepeatCount(Animation.INFINITE);
			tvHint = (TextView) findViewById(R.id.tv_hint);
			tvHint.startAnimation(ani);
			mBubbleView = (ColorBubbleView) findViewById(R.id.pulldoor_layout);
			mBubbleView.setUnlockListener(new IUnLockListener() {
				@Override
				public void onUnlockFinish() {
					finish();
				}

				@Override
				public boolean checkPwd(int pos) {
					return false;
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
			});
		}
	}
	
	protected void init() {
		
		// 注册时间变化的广播
		registerTimeReceiver();
		
		mDateFormat = getString(R.string.month_day_year_week);
		mDateView = (TextView) findViewById(R.id.text_date);
		mTimeView = (TextView) findViewById(R.id.text_time);
		mDateView.setText(DateFormat.format(mDateFormat, new Date()));
		mTimeView.setText(TimeUtil.getCurrentTime());
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

}
