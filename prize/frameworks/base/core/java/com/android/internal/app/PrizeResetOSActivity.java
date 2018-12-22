/*
sim insert, reset os.
author:wangxianzhen
date:2016-07-26
*/
package com.android.internal.app;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.Settings;
import android.app.Activity;
import android.app.StatusBarManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import com.prize.internal.R;
public class PrizeResetOSActivity extends Activity implements OnClickListener, OnKeyListener {

	private static final String TAG = "prizeadb";
	private static final int SHOW_REBOOT_DIALOG_TIME = 5;
	private static final int REBOOT_DIALOG_UPDATE_INTERVAL = 1000;
	private static final String PRIZE_SIM_ID_LIST = "prizesimid";
	private Handler mHandler = new Handler();
	private int mShowSeconds;
	private TextView tv_Reset;
	private Button btn_OK;
	private Button btn_Cancel;
	private Button btn_Reboot;
	String idList = null;
	String newID = null;
        boolean bSimInsert = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.sim_insert_dialog);
        Intent intent=getIntent();  
        Bundle bundle=intent.getExtras();  
        idList=bundle.getString("sim_id_list");
        newID = bundle.getString("new_sim_id");
        bSimInsert = bundle.getBoolean("sim_insert", true);
        Log.i(TAG, "onCreate idList=" + idList + " newID=" + newID + ", bSimInsert=" + bSimInsert);
        
        setActivityFullScreen();
        
        tv_Reset = (TextView)findViewById(R.id.reset_text);
        
        btn_Reboot = (Button)findViewById(R.id.reboot_button);
        btn_Reboot.setOnClickListener(this);
        btn_Reboot.setVisibility(View.GONE);
        btn_OK = (Button) findViewById(R.id.reset_ok_button);
        btn_OK.setOnClickListener(this);
        btn_Cancel = (Button) findViewById(R.id.reset_cancel_button);
        btn_Cancel.setOnClickListener(this);
    }
	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		switch (arg0.getId()) {
		case R.id.reset_ok_button:
			onClickOKButton();
			break;
		case R.id.reset_cancel_button:
			onClickCancelButton();
			break;
		case R.id.reboot_button:
			onClickRebootButton();
			break;
		default:
			break;
		}
	}

	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		Log.i(TAG, "PrizeResetOSActivity onPause");
		StatusBarManager statusBarManager = (StatusBarManager) getSystemService(Context.STATUS_BAR_SERVICE);
        statusBarManager.disable(StatusBarManager.DISABLE_NONE);
	}
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		Log.i(TAG, "PrizeResetOSActivity onResume");
        //setActivityFullScreen();
		StatusBarManager statusBarManager = (StatusBarManager) getSystemService(Context.STATUS_BAR_SERVICE);
        statusBarManager.disable(StatusBarManager.DISABLE_EXPAND);
	}
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		Log.i(TAG, "PrizeResetOSActivity onDestroy");
	}
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		Log.i(TAG, "PrizeResetOSActivity onStop");
	}

    private void setActivityFullScreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        	Window window = getWindow();
        	window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        	window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
        			View.SYSTEM_UI_FLAG_IMMERSIVE_GESTURE_ISOLATED | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
        			View.SYSTEM_UI_FLAG_IMMERSIVE | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN);
        	window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM |
        			WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        	window.setStatusBarColor(Color.TRANSPARENT);
        	window.setType(WindowManager.LayoutParams.TYPE_PHONE);
        }
    }
	private Runnable mUpdateDialogRunnable = new Runnable() {
		@Override
		public void run() {
			mShowSeconds--;
			if (mShowSeconds <= 0) {
				Log.i(TAG, "go to reboot");				
				mHandler.removeCallbacks(mUpdateDialogRunnable);
				// reboot phone
				reboot();
				return;
			} else {
				mHandler.postDelayed(mUpdateDialogRunnable, REBOOT_DIALOG_UPDATE_INTERVAL);
			}
			updateTimeInfo();
		}
	};
	
	protected void updateTimeInfo() {
		// TODO Auto-generated method stub
		String str = getResources().getString(R.string.reboot_alert);
		String text = String.format(str, ""+mShowSeconds);
		tv_Reset.setText(text);
	}
	
	private void onClickOKButton() {
		// TODO Auto-generated method stub
		Log.i(TAG, "onClickOKButton");
		Settings.System.putString(getContentResolver(), PRIZE_SIM_ID_LIST, idList + newID);
		Settings.System.putInt(getContentResolver(),"prizeresetmode",0);
		Intent intent = new Intent(Intent.ACTION_MASTER_CLEAR);
        intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
        intent.putExtra(Intent.EXTRA_REASON, "MasterClearConfirm");
        intent.putExtra(Intent.EXTRA_WIPE_EXTERNAL_STORAGE, false);
        Log.d(TAG, "send broadcast ACTION_MASTER_CLEAR");
        sendBroadcast(intent);
	}
	
	private void onClickCancelButton() {
		// TODO Auto-generated method stub
		Log.i(TAG, "onClickCancelButton bSimInsert=" + bSimInsert);
                Settings.System.putString(getContentResolver(), PRIZE_SIM_ID_LIST, idList + newID);
		/*-prize-modify by lihuangyuan,remove reboot dialog -2017-04-20-start-*/
            if(bSimInsert)
            {
                Log.i(TAG, "send com.android.sim.inserted");
                Intent intentsim = new Intent("com.android.sim.inserted");
                sendBroadcast(intentsim);
            }
            Settings.System.putInt(getContentResolver(),"prizeresetmode",0);
		PrizeResetOSActivity.this.finish();		
	    /*-prize-modify by lihuangyuan,remove reboot dialog -2017-04-20-end-*/
	}
	
	private void onClickRebootButton() {
		// TODO Auto-generated method stub
		Log.i(TAG, "onClickRebootButton");
		reboot();
		
	}
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		Log.i(TAG, "PrizeResetOSActivity onKeyDown: " + event.getKeyCode());
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_MENU) {
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_HOME) {
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	@Override
	public boolean onKey(View arg0, int arg1, KeyEvent arg2) {
		// TODO Auto-generated method stub
		Log.i(TAG, "onKey: " + arg2.getKeyCode());
		return false;
	}
	
	private void reboot() {
		PowerManager powermgr = (PowerManager) getSystemService(Context.POWER_SERVICE);
		try {
			powermgr.reboot("siminserted");
		} catch (Exception e) {
			Log.i(TAG, "reboot e  " + e.toString());
		}
	}
}
