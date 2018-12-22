package com.android.systemui;

import java.lang.reflect.Method;

import com.android.systemui.R;


import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

public class SimCheckService extends Service {
	private static final String TAG = "SimCheckService";

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();

		Log.i(TAG,"onCreate++++++++");
		// ////////////////////////////////////////////////////////////////////		
		IntentFilter filter = new IntentFilter();		
		filter.addAction(ACTION_SIM_INSERTED);
		registerReceiver(mSimReceiver, filter);		
		
		mContext =  this;		
		mWindowManager = (WindowManager) mContext
                .getSystemService(Context.WINDOW_SERVICE);		
		PowerManager powermgr = (PowerManager) getSystemService(Context.POWER_SERVICE);
		mFullScreenWakelock = powermgr.newWakeLock(PowerManager.FULL_WAKE_LOCK
				| PowerManager.ACQUIRE_CAUSES_WAKEUP, TAG);
		// //////////////////////////////////////////////////////////////////////		
	}

	public static final String ACTION_SHOW_REBOOT_DIALOG = "com.android.action.SHOW_REBOOT_DIALOG";
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		if(intent != null)
		{
			String action = intent.getAction();
			Log.i(TAG,"onStartCommand action:"+action);
			if(ACTION_SHOW_REBOOT_DIALOG.equals(action))
			{
					
				mHandler.post(new Runnable() {
	                @Override
	                public void run() {
	                	showPopupWindow(SimCheckService.this);
	                }
	            });
			}
		}
		return super.onStartCommand(intent, flags, startId);		
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		unregisterReceiver(mSimReceiver);
	}	
	
	///////////////////////////////////////////////////////////////////////////////////
	private final static String ACTION_SIM_INSERTED = "com.android.sim.inserted";
	private SimStateReceive mSimReceiver = new SimStateReceive();

	public class SimStateReceive extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			// System.out.println("sim state changed");
			String action = intent.getAction();
			if (action.equals(ACTION_SIM_INSERTED)) {
				Log.i(TAG,"ACTION_SIM_INSERTED++++++++++");
				showPopupWindow(SimCheckService.this);
			}
		}

	}
	
	/////////////////////////////////////////////////////////////////////////////////////////
	private int mShowSeconds;
	public static final int SHOW_REBOOT_DIALOG_TIME = 8;
	public static final int REBOOT_DIALOG_UPDATE_INTERVAL = 1000;
	
	private PowerManager.WakeLock mFullScreenWakelock = null;
	private TextView mSimDialogText = null;
	private View mSimDialogView = null;
	private boolean mIsShowPopup = false;
	private WindowManager.LayoutParams mPreviewLayoutParams;
	private WindowManager mWindowManager;
	private Context mContext;

	private  void setUpView(final Context context) {

		Log.i(TAG, "setUp view");

		mSimDialogView = LayoutInflater.from(context).inflate(
				R.layout.siminsert_alertdialog, null);
		mSimDialogText = (TextView)mSimDialogView.findViewById(R.id.siminsert_textview);
		String str = context.getResources().getString(R.string.str_siminsertalert);
		String text = String.format(str, ""+mShowSeconds);
		mSimDialogText.setText(text);
		
		Button positiveBtn = (Button) mSimDialogView.findViewById(R.id.siminsert_button);
		positiveBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.i(TAG, "ok on click");
				mHandler.removeCallbacks(mUpdateDialogRunnable);								
				hidePopupWindow();					
				reboot();
			}
		});		

	}
	public  void hidePopupWindow() {
		Log.i(TAG, "hide " + mSimDialogView);
		if (mIsShowPopup && null != mSimDialogView) {
			Log.i(TAG, "hidePopupWindow");
			mWindowManager.removeView(mSimDialogView);
			mIsShowPopup = false;
			mSimDialogView = null;
		}
		if (mFullScreenWakelock.isHeld()) {
			mFullScreenWakelock.release();
		}
	}
	public void showPopupWindow(Context context) {
		if (mIsShowPopup) {
			Log.i(TAG, "return cause already shown");
			return;
		}

		if (!mFullScreenWakelock.isHeld()) {
			Log.i(TAG, "sim insert full screen");
			mFullScreenWakelock.acquire();
		}
		
		mShowSeconds = SHOW_REBOOT_DIALOG_TIME;
		mIsShowPopup = true;
		Log.i(TAG, "showPopupWindow");

		// 获取应用的Context
		mContext = context.getApplicationContext();
		// 获取WindowManager
		mWindowManager = (WindowManager) mContext
				.getSystemService(Context.WINDOW_SERVICE);

		setUpView(context);

		mPreviewLayoutParams = new WindowManager.LayoutParams();
		// 类型
		//mPreviewLayoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
		//mPreviewLayoutParams.type = WindowManager.LayoutParams.TYPE_KEYGUARD_DIALOG;
		mPreviewLayoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;

		// WindowManager.LayoutParams.TYPE_SYSTEM_ALERT

		// 设置flag

		int flags = WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM
		   | WindowManager.LayoutParams.FLAG_FULLSCREEN
                 | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                 |WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                 |WindowManager.LayoutParams.FLAG_LAYOUT_IN_OVERSCAN
                 |WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                 |WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR;
                
		// 如果设置了WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE，弹出的View收不到Back键的事件
		mPreviewLayoutParams.flags = flags;
              mPreviewLayoutParams.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                                                           | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
		// 不设置这个弹出框的透明遮罩显示为黑色
		mPreviewLayoutParams.format = PixelFormat.TRANSLUCENT;
		// FLAG_NOT_TOUCH_MODAL不阻塞事件传递到后面的窗口
		// 设置 FLAG_NOT_FOCUSABLE 悬浮窗口较小时，后面的应用图标由不可长按变为可长按
		// 不设置这个flag的话，home页的划屏会有问题

		mPreviewLayoutParams.width = LayoutParams.MATCH_PARENT;
		mPreviewLayoutParams.height = LayoutParams.MATCH_PARENT;

		mPreviewLayoutParams.gravity = Gravity.CENTER;
              mPreviewLayoutParams.setTitle("simreboot");
		mWindowManager.addView(mSimDialogView, mPreviewLayoutParams);
		Log.i(TAG, "add view");
		
		updatePopupWindow();
		mHandler.postDelayed(mUpdateDialogRunnable,
				REBOOT_DIALOG_UPDATE_INTERVAL);
	}
	private void updatePopupWindow()
	{
		String str = getResources().getString(R.string.str_siminsertalert);
		String text = String.format(str, "" + mShowSeconds);
		mSimDialogText.setText(text);
		//mSimDialogText.setText(R.string.str_siminsertalert);
		mWindowManager.updateViewLayout(mSimDialogView, mPreviewLayoutParams);
	}
	
	private Runnable mUpdateDialogRunnable = new Runnable() {
		@Override
		public void run() {
			mShowSeconds--;
			if (mShowSeconds <= 0) {
				Log.i(TAG, "go to reboot+++++++++++");				
				hidePopupWindow();
				mSimDialogView = null;
				mSimDialogText = null;

				mHandler.removeCallbacks(mUpdateDialogRunnable);
				// reboot phone
				reboot();
				return;
			} else {
				mHandler.postDelayed(mUpdateDialogRunnable,
						REBOOT_DIALOG_UPDATE_INTERVAL);
			}
			updatePopupWindow();
		}
	};

	private Handler mHandler = new Handler();

	private void reboot() {
		PowerManager powermgr = (PowerManager) getSystemService(Context.POWER_SERVICE);
		try
		{
			powermgr.reboot("siminserted");
		}
		catch(Exception e)
		{
			
		}

	}
}
