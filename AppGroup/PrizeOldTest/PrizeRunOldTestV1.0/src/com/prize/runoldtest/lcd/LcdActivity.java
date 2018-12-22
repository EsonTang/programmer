package com.prize.runoldtest.lcd;

import java.util.Timer;
import java.util.TimerTask;

import com.prize.runoldtest.ManualTestActivity;
import com.prize.runoldtest.R;
import com.prize.runoldtest.R.drawable;
import com.prize.runoldtest.R.id;
import com.prize.runoldtest.R.layout;
import com.prize.runoldtest.cpu.CpuTestActivity;
import com.prize.runoldtest.util.Const;
import com.prize.runoldtest.util.DataUtil;
import com.prize.runoldtest.util.LogToFile;
import com.prize.runoldtest.util.LogUtil;
import com.prize.runoldtest.util.OldTestResult;

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
    public String TAG = "RunInLCDTest";
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
	        		 LogToFile. writeToFile(LogToFile.VERBOSE, TAG, "LCDTest :handleMessage:change image.."+"\n");
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
      //  LogUtil.e("this is a SB ----");
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED  
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON  
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_lcd);
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK |PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, "My TAG");
        wakeLock.acquire();
        LogToFile. writeToFile(LogToFile.VERBOSE, TAG, "LCDTest :lcdTest begin..."+"\n");
        DataUtil.addDestoryActivity(LcdActivity.this, "LcdActivity");
        Intent intent=getIntent();
        LcdTime = intent.getIntExtra(Const.EXTRA_MESSAGE,0);
        LcdTime=LcdTime*60*1000;
        timer.schedule(task, LcdTime);
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
        	DataUtil.Flag3D = true;
        	LogToFile. writeToFile(LogToFile.VERBOSE, TAG, "LCDTest :lcdTest finish..."+"\n");
        	OldTestResult.LcdTestresult=true;
        	
        	Intent intent = new Intent();
            //把返回数据存入Intent
            intent.putExtra("result", "LCDTest:PASS");
            //设置返回数据
            LcdActivity.this.setResult(RESULT_OK, intent);
            
        	
            LcdActivity.this.finish();
        }
    };
    Timer timer = new Timer();
    protected void onStart() {
        super.onStart();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED  
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON  
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        handler.removeMessages(0x123);
        handler.sendEmptyMessage(0x123);
    }

    public void onPause() {
        super.onPause();
        handler.removeMessages(0x123);
		 timer.cancel();
		 if(wakeLock.isHeld()){
			 wakeLock.release();
		 }
		 finish();
        LogUtil.e("onPauseLcdActivity");
       
    }

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
		
		/*Intent intent = new Intent();
        //把返回数据存入Intent
        intent.putExtra("result", "LCDTest:FAIL");
        //设置返回数据
        LcdActivity.this.setResult(RESULT_OK, intent);*/
        
		LogToFile. writeToFile(LogToFile.VERBOSE, TAG, "LCDTest :lcdTest onBackPressed..."+"\n");
       
        DataUtil.finishBackPressActivity();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		LogToFile. writeToFile(LogToFile.VERBOSE, TAG, "LCDTest :lcdTest onDestroy..."+"\n");
		handler.removeMessages(0x123);
		 timer.cancel();
		 if(wakeLock.isHeld()){
			 wakeLock.release();
		 }
		
	}

}
