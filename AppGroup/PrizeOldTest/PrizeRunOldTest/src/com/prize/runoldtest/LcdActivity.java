package com.prize.runoldtest;

import java.util.Timer;
import java.util.TimerTask;

import com.prize.runoldtest.util.Const;
import com.prize.runoldtest.util.DataUtil;
import com.prize.runoldtest.util.LogUtil;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.util.Log;
import android.view.WindowManager;
import android.widget.FrameLayout;

public class LcdActivity extends Activity {
    private long LcdTime;
    FrameLayout frame = null;
    private PowerManager.WakeLock wakeLock = null;
    private static final int REFRESH_TIME = 2000;
    /** 存储的文件名 */  
  //  public static final String DATABASE = "Database";
    Handler handler = new Handler() {
        int i = 0;
        public void handleMessage(Message msg) {
        	//SharedPreferences sharepreference = LcdActivity.this.getSharedPreferences(DATABASE,  
	          //      Activity.MODE_PRIVATE);
	      //  String str=sharepreference.getString("testenable", "true").toString();
	     //  if(str.equals("true")){
	        	 if (msg.what == 0x123) {
	                 ChangeImage(i);
	                 i++;
	                 if(i == 5)
	                     i = 0;
	             }
	       handler.sendEmptyMessageDelayed(0x123,
	                     REFRESH_TIME);
	       
	      // }
	        /*else {
	        	while (handler.hasMessages(0x123)) {
	        		handler.removeMessages(0x123);
                }
	        	finish();
	        }*/
           
            
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtil.e("this is a SB ----");
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_lcd);
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK |PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, "My TAG");
        wakeLock.acquire();
        DataUtil.addDestoryActivity(LcdActivity.this, "LcdActivity");
        Intent intent=getIntent();
        LcdTime = intent.getIntExtra(Const.EXTRA_MESSAGE,0);
        timer.schedule(task, LcdTime*1000);
        frame = (FrameLayout)findViewById(R.id.myFrame);
    }

    void ChangeImage(int i)
    {
        Drawable a = getResources().getDrawable(R.drawable.m1);
        Drawable b = getResources().getDrawable(R.drawable.m2);
        Drawable c = getResources().getDrawable(R.drawable.m3);
        Drawable d = getResources().getDrawable(R.drawable.m4);
        Drawable e = getResources().getDrawable(R.drawable.m5);
        switch(i)
        {
            case 0:
                frame.setForeground(a);
                break;
            case 1:
                frame.setForeground(b);
                break;
            case 2:
                frame.setForeground(c);
                break;
            case 3:
                frame.setForeground(d);
                break;
            case 4:
                frame.setForeground(e);
                break;
        }
    }

    TimerTask task = new TimerTask(){
        public void run(){
        	LogUtil.e("KEYCODE_BACKFlag3D");
            ManualTestActivity.Flag3D = true;
            LcdActivity.this.finish();
        }
    };
    Timer timer = new Timer();
    protected void onStart() {
        super.onStart();
        handler.removeMessages(0x123);
        handler.sendEmptyMessage(0x123);
    }

    public void onPause() {
        super.onPause();
        handler.removeMessages(0x123);
        wakeLock.release();
        LogUtil.e("onPauseLcdActivity");
        timer.cancel();
    }

}
