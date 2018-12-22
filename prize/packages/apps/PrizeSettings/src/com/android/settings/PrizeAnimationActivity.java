package com.android.settings;

import android.os.Bundle;
import android.os.Message;
import android.app.Activity;
import android.content.Intent;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import java.util.ArrayList;
import java.util.List;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.content.ContentResolver;
import android.os.PowerManager;
import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.view.WindowManagerGlobal;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.util.Log;

public class PrizeAnimationActivity extends Activity {

	private AnimationDrawable animaition;
	private String string = null;
	private PrizeShowAnimationView mPrizeShowAnimationView;
	ArrayList<Integer> image;
	Context mContext;
	public static Intent intent;
	private static Uri queryUri = Uri.parse("content://com.prize.sleepgesture/sleepgesture"); 
	ContentResolver mContentResolver;  
	PowerManager mPowerManager;
	PowerManager.WakeLock mWakelock;
	boolean bCreate = false;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.activity_prize_animation);
		mPrizeShowAnimationView = (PrizeShowAnimationView) findViewById(R.id.empty);
		string = getIntent().getExtras().getString("gesture");
		mContentResolver = getContentResolver();
		mPowerManager = (PowerManager)getSystemService(Context.POWER_SERVICE);	
		mWakelock = mPowerManager.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP |PowerManager.FULL_WAKE_LOCK, "wakeUpScreen"); 
		
		IntentFilter screenOffFilter = new IntentFilter();
		screenOffFilter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(mScreenOffFilter, screenOffFilter);
		
		bCreate = true;
		
	}
	
	BroadcastReceiver mScreenOffFilter = new BroadcastReceiver() {
        @Override 
        public void onReceive(Context context, Intent intent) {
            PrizeAnimationActivity.this.finish();
        	android.os.Process.killProcess(android.os.Process.myPid());
        }
    };
	
	@Override
	protected void onResume() {
		Log.e("liup","onResume");
		if(bCreate){
			mWakelock.acquire(2000);
			mWakelock.release();
			try {
				WindowManagerGlobal.getWindowManagerService().dismissKeyguard();
			} catch (Exception e) {
				e.printStackTrace();
			}
			if(mPrizeShowAnimationView != null){
				selectAnimGesture();
				mPrizeShowAnimationView.startShowImage();
				new Thread(new MyThread()).start(); 
			}
			bCreate = false;
		}
		super.onResume(); 
	}
	
	
	@Override
	protected void onPause() {	
		Log.e("liup","onPause");
		super.onPause(); 
	}
	
	@Override
	public void finish(){
		Log.e("liup","finish");
		unregisterReceiver(mScreenOffFilter);
		super.finish();
	}
	
	@Override
	protected void onDestroy() {
		Log.e("liup","onDestroy");
		super.onDestroy();
	}
	private void selectAnimGesture(){
		TypedArray typedArray = getResources().obtainTypedArray(R.array.prize_sleep_c);;
		Log.e("liup","selectAnimGesture string = " + string.trim());
		if(string.trim().equals("0xcc")){//double
			typedArray = getResources().obtainTypedArray(R.array.prize_sleep_double);	
		}
		else if(string.trim().equals("0xaa")){//right
			typedArray = getResources().obtainTypedArray(R.array.prize_sleep_right);
		}
		else if(string.trim().equals("0xba")){//up
			typedArray = getResources().obtainTypedArray(R.array.prize_sleep_up);
		}
		else if(string.trim().equals("0xab")){//down
			typedArray = getResources().obtainTypedArray(R.array.prize_sleep_down);
		}
		else if(string.trim().equals("0xbb")){//left
			typedArray = getResources().obtainTypedArray(R.array.prize_sleep_left);
		}
		else if(string.trim().equals("0x6f")){//O
			typedArray = getResources().obtainTypedArray(R.array.prize_sleep_circle);
		}
		else if(string.trim().equals("0x76")){//v
			typedArray = getResources().obtainTypedArray(R.array.prize_sleep_v);
		}
		else if(string.trim().equals("0x77")){//w
			typedArray = getResources().obtainTypedArray(R.array.prize_sleep_w);
		}
		else if(string.trim().equals("0x7a")){//z
			typedArray = getResources().obtainTypedArray(R.array.prize_sleep_z);
		}
		else if(string.trim().equals("0x73")){//s
			typedArray = getResources().obtainTypedArray(R.array.prize_sleep_s);
		}
		else if(string.trim().equals("0x6d")){//m
			typedArray = getResources().obtainTypedArray(R.array.prize_sleep_m);
		}
		else if(string.trim().equals("0x65")){//e
			typedArray = getResources().obtainTypedArray(R.array.prize_sleep_e);
		}
		else if(string.trim().equals("0x63")){//c
			typedArray = getResources().obtainTypedArray(R.array.prize_sleep_c);
		}
		else if(string.trim().equals("0x5e")){//^
			typedArray = getResources().obtainTypedArray(R.array.prize_sleep_arror_up);
		}
		for( int index = 0; index < typedArray.length(); index++ ){
			mPrizeShowAnimationView.addImageId(typedArray.getResourceId( index, 0 ));
		}
	}

	
	private void selectAppGesture(){
		Cursor cursor = mContentResolver.query(queryUri, null,"item" + "=?",new String[] { string.trim() }, null);
		if (cursor != null && cursor.moveToFirst()) {
			String packagename = cursor.getString(cursor.getColumnIndex("packageName"));
			String classname = cursor.getString(cursor.getColumnIndex("classname"));
			if(classname.equals("com.android.camera.CameraLauncher")){
				classname = "com.android.camera.CameraActivity";
			}
			try {
				Intent intent = new Intent();
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				intent.setClassName(packagename, classname);
				startActivity(intent);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public class MyThread implements Runnable {  
	    @Override  
	    public void run() { 
	    	Log.e("liup","mPrizeShowAnimationView.isShowAnimation()1 = " + mPrizeShowAnimationView.isShowAnimation());
	        while (mPrizeShowAnimationView.isShowAnimation()) {  
	            try {  
	                Thread.sleep(10);
	            } catch (InterruptedException e) {  
	                e.printStackTrace();  
	            }  
	        }  
			Log.e("liup","mPrizeShowAnimationView.isShowAnimation()2 = " + mPrizeShowAnimationView.isShowAnimation());
			selectAppGesture();	    	
			PrizeAnimationActivity.this.finish();
        	android.os.Process.killProcess(android.os.Process.myPid());
	    }  
	}  
	
}
