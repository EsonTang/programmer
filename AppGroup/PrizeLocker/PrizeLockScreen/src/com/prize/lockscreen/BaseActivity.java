package com.prize.lockscreen;

import com.prize.lockscreen.application.LockScreenApplication;
import com.prize.lockscreen.service.StarLockService;
import com.prize.lockscreen.utils.LogUtil;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
/***
 * 锁屏activity基类
 * @author fanjunchen
 *
 */
public abstract class BaseActivity extends Activity {

	private final String TAG = "BaseActivity_prize";
	
	private TimeChangedReceiver mTimeChangedReceiver;
	/**进入哪个应用  1 SMS， 2 PHONE， 3 CAMERA*/
	protected int mPos = 0;
	
	private final int TYPE_KEYGUARD = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR; //TYPE_KEYGUARD (0x7d4) // TYPE_SYSTEM_ERROR 2010(0x7da)
	@Override
    protected void onCreate(Bundle savedInstanceState) 
    {
		getWindow().setType(TYPE_KEYGUARD);
		// 做全屏显示
		super.onCreate(savedInstanceState);
    	requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
			Window window = getWindow();
			window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
					| WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
			window.getDecorView().setSystemUiVisibility(
					View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
							| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
							| View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
			window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
			window.setStatusBarColor(Color.TRANSPARENT);
			window.setNavigationBarColor(Color.TRANSPARENT);
		}
		
    }
	@Override
	public void setContentView(int layoutId) {
		super.setContentView(layoutId);
		
		startService(new Intent(this, StarLockService.class));
		init();
	}
	
	@Override
	public void setContentView(View view) {
		super.setContentView(view);
		
		startService(new Intent(this, StarLockService.class));
		init();
	}
	
	@Override
	public void setContentView(View v, LayoutParams params) {
		super.setContentView(v, params);
		
		startService(new Intent(this, StarLockService.class));
		init();
	}
	/***
	 * 注册时间广播，若要监听时间变化就调用此方法注册即可
	 */
	protected void registerTimeReceiver() {
		// 注册时间变化的广播
		mTimeChangedReceiver = new TimeChangedReceiver();
		IntentFilter filter = new IntentFilter();
	
		filter.addAction(Intent.ACTION_TIME_TICK);// 可同时监听日期，时间的变化。
		filter.addAction(Intent.ACTION_TIME_CHANGED);// 监听时间变化
		filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);// 监听时区变化：
		filter.addAction(Intent.ACTION_DATE_CHANGED);// 监听日期变化
		registerReceiver(mTimeChangedReceiver, filter);
	}
	/***
	 * 建议在这个方法中初始化控件, <br>不需要再显示调用，因在setContentView中已经调用了
	 */
	protected abstract void init();
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		
		if (event.getKeyCode() == KeyEvent.KEYCODE_HOME
				|| event.getKeyCode() == KeyEvent.KEYCODE_BACK
				|| event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_DOWN
				|| event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_UP) {
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		
		if (event.getKeyCode() == KeyEvent.KEYCODE_HOME
				|| event.getKeyCode() == KeyEvent.KEYCODE_BACK
				|| event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_DOWN
				|| event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_UP) {
			return true;
		}
		return super.onKeyUp(keyCode, event);
	}
	
	private class TimeChangedReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			LogUtil.d(TAG, "------>onReceive action:" + action);
			if (Intent.ACTION_TIME_TICK.equals(action)
					|| Intent.ACTION_TIME_CHANGED.equals(action)
					|| Intent.ACTION_DATE_CHANGED.equals(action)) {
				updateTime();
			} else if (Intent.ACTION_DATE_CHANGED.equals(action)) {
				// 时区变化
			} else {

			}
		}
	}
	/***
	 * 更新显示时间，即给时间控件赋值,若不需要则实现一个空方法即可
	 */
	protected abstract void updateTime();
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mTimeChangedReceiver != null)
			unregisterReceiver(mTimeChangedReceiver);
	}
	
	@Override
	public void finish() {
		LockScreenApplication.playSound(2);
		super.finish();
	}
	
	/***
	 * 解锁完成后进入到哪个APP
	 * @param pos
	 */
	protected void enterApp() {
		switch (mPos) {
		case 1:
			launchSms();
			break;
		case 2:
			launchDial();
			break;
		case 3:
			launchCamera();
			break;
		}
		mPos = 0;
	}
	
	//启动短信应用
    private void launchSms() {

		//mFocusView.setVisibility(View.GONE);
		Intent intent = new Intent();
		ComponentName comp = new ComponentName("com.android.mms",
				"com.android.mms.ui.ConversationList");
		intent.setComponent(comp);
		intent.setAction("android.intent.action.VIEW");
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
		startActivity(intent);
	}
    
    //启动拨号应用
    private void launchDial() {
		Intent intent = new Intent(Intent.ACTION_DIAL);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
		startActivity(intent);
	}
    
    //启动相机应用
    private void launchCamera() {
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		intent.addCategory(Intent.CATEGORY_DEFAULT);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
		startActivity(intent);
	}
}
