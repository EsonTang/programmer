package com.prize.lockscreen;

import java.util.Date;
import com.prize.lockscreen.interfaces.IUnLockListener;
import com.prize.lockscreen.receiver.HomeWatcherReceiver;
import com.prize.lockscreen.service.LockScreenNotificationListenerService;
import com.prize.lockscreen.utils.TimeUtil;
import com.prize.lockscreen.widget.PullDoorView;
import com.prize.prizelockscreen.R;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.MotionEvent;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.TextView;
/***
 * 上滑解锁 锁屏实现主activity
 * @author june
 *
 */
public class LockMainActivity extends BaseActivity {

	private PullDoorView mPullDoorView;
	private int LOCK_SCREEN_TYPE = 1;// [1:向上滑动解锁方式，2：气泡解锁方式]
	private TextView tvHint;
	private static final String ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners";
	private static final String ACTION_NOTIFICATION_LISTENER_SETTINGS = "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS";
	public static final String ACTION_NLS_UPDATE = "com.prize.notificationlistener.NLSUPDATE";
	private HomeWatcherReceiver mHomeKeyReceiver;
	private String mDateFormat;
	private TextView mDateView;
	private TextView mTimeView;
	
	//public static final int FLAG_HOMEKEY_DISPATCHED = 0x80000000;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (LOCK_SCREEN_TYPE == 1) {
			setContentView(R.layout.slid_up_unlock_lay);
			Animation ani = new AlphaAnimation(0f, 1f);
			ani.setDuration(1500);
			ani.setRepeatMode(Animation.REVERSE);
			ani.setRepeatCount(Animation.INFINITE);
			tvHint = (TextView) findViewById(R.id.tv_hint);
			tvHint.startAnimation(ani);
			mPullDoorView = (PullDoorView) findViewById(R.id.pulldoor_layout);
			mPullDoorView.setUnlockListener(new IUnLockListener() {
				@Override
				public void onUnlockFinish() {
					finish();
				}

				@Override
				public boolean checkPwd(int pos) {
					// TODO Auto-generated method stub
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
		if (!isAccessEnabled()) {
			showConfirmDialog();
		}
		getActiveNotice(this);
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

	private boolean isAccessEnabled() {
		String pkgName = getPackageName();
		final String flat = Settings.Secure.getString(getContentResolver(),
				ENABLED_NOTIFICATION_LISTENERS);
		if (!TextUtils.isEmpty(flat)) {
			final String[] names = flat.split(":");
			for (int i = 0; i < names.length; i++) {
				final ComponentName cn = ComponentName
						.unflattenFromString(names[i]);
				if (cn != null) {
					if (TextUtils.equals(pkgName, cn.getPackageName())) {
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * 
	 */
	private void openNotificationAccess() {
		startActivity(new Intent(ACTION_NOTIFICATION_LISTENER_SETTINGS));
	}

	/**
	 * 获取通知权限的广播
	 */
	private void showConfirmDialog() {
		new AlertDialog.Builder(this)
				.setMessage("Please enable NotificationMonitor access")
				.setTitle("Notification Access")
				.setIconAttribute(android.R.attr.alertDialogIcon)
				.setCancelable(true)
				.setPositiveButton(android.R.string.ok,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								openNotificationAccess();
							}
						})
				.setNegativeButton(android.R.string.cancel,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								// do nothing
							}
						}).create().show();
	}

	/**
	 * 
	 * @param context
	 */
	private void getActiveNotice(Context context){
		Intent intent = new Intent();
		intent.setAction(LockScreenNotificationListenerService.ACTION_NLS_CONTROL);
		intent.putExtra(Constant.COMMAND, Constant.GET_ACTIVE_NOTICE);
		context.sendBroadcast(intent);
	}

}
