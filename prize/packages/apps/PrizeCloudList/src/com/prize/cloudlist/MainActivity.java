package com.prize.cloudlist;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.Toast;
import android.location.LocationManager;
import java.io.File;
import java.io.FileInputStream;


public class MainActivity extends Activity
{
	public static final String TAG = "MainActivity";
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		Button btn = (Button)findViewById(R.id.testbutton);
		btn.setOnClickListener(new OnClickListener()
		{			
			@Override
			public void onClick(View v)
			{
				/*Intent intent = new Intent();
				intent.setComponent(new ComponentName("com.prize.cloudlist","com.prize.cloudlist.CloudListService"));
				startService(intent);*/
				mHandler.postDelayed(mTestRun, 10*1000);
			}
		});
	}
	private Handler mHandler = new Handler();
	private Runnable mTestRun = new Runnable()
	{
		
		@Override
		public void run()
		{
			//WindowManager wmgr = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
			//boolean isfullscreen = wmgr.isFullScreenMode();			
			//Toast.makeText(MainActivity.this,"Current screenfullmode is"+isfullscreen,Toast.LENGTH_LONG).show();
			
			/*LocationManager lmgr= (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                    boolean isBaiduUseLocation = lmgr.isAppUseLocation("com.baidu.BaiduMap");
                    Toast.makeText(MainActivity.this,"Current baidu use location is"+isBaiduUseLocation,Toast.LENGTH_LONG).show();*/
                     /*File file = new File("/sys/power/wake_lock");
			try
			{
				FileInputStream is = new FileInputStream(file);
				byte[] buffer = new byte[1024];
				is.read(buffer);
				String str = new String(buffer);
				Log.d("lhy","wake_lock:"+str);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}*/
			
			mHandler.postDelayed(mTestRun, 10*1000);
		}
	};
	private void hidesoftinput()
	{
		InputMethodManager inputmgr = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		//inputmgr.prizeHideSoftInput(InputMethodManager.HIDE_NOT_ALWAYS, null);            
	}
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		Log.d(TAG,"onkeyDown "+keyCode);
		if(keyCode == 82)
		{
			hidesoftinput();
		}
		return super.onKeyDown(keyCode, event);
	}
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event)
	{
		Log.d(TAG,"onKeyUp "+keyCode);
		
		return super.onKeyDown(keyCode, event);
	}
	

}
