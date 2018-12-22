package com.prize.runoldtest;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;


import com.prize.runoldtest.util.Const;
import com.prize.runoldtest.util.LogToFile;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemClock;
import android.view.KeyEvent;
import android.widget.TextView;

public class SlpRsmActivity extends Activity {
    private long sR_Time=0;//测试周期数
    private static final int REFRESH_TIME=2000;
    private boolean isSleep=true;
    private PowerManager pm;  
    private PowerManager.WakeLock wakeLock;  
    private static String TAG="SlpRsmActivity:";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slp_rsm);

        Intent intent=getIntent();
        String message=intent.getStringExtra(Const.EXTRA_MESSAGE);
        TextView textView=(TextView)findViewById(R.id.tvShow);
        textView.setText(message);
        if(message!=null){
        	sR_Time = Integer.parseInt(message)*2;
        }else{
        	sR_Time=10;
        }
      
    }

    TimerTask task = new TimerTask(){
        public void run(){
            try {
                ManualTestActivity.FlagVideo = true;
                Runtime.getRuntime().exec("input keyevent " + KeyEvent.KEYCODE_BACK);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    };
    Timer timer = new Timer();
    protected void onStart() {
        super.onStart();

       // timer.schedule(task, sR_Time*3000);
        handler.removeMessages(0x134);
        handler.sendEmptyMessage(0x134);
    }
    
    
    Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			if(sR_Time>=0){
				if(isSleep){//如果正在处于休眠，那么唤醒
					pm = (PowerManager) getSystemService(POWER_SERVICE);
			        wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, 
			        		"SimpleTimer");
					wakeLock.acquire();  
					LogToFile. writeToFile(LogToFile.VERBOSE, TAG, "screenOn");
			        android.util.Log.i("cxq", "screenOn");  
			        isSleep=false;
		    	}else{
		    		pm = (PowerManager) getSystemService(POWER_SERVICE);
		    		pm.goToSleep(SystemClock.uptimeMillis());  
		    		LogToFile. writeToFile(LogToFile.VERBOSE, TAG, "screenOff");
		            android.util.Log.i("cxq", "screenOff");  
		            isSleep=true;
		    	}
				sR_Time--;
				 handler.sendEmptyMessageDelayed(0x134,
	                     REFRESH_TIME);
			}else {
				pm = (PowerManager) getSystemService(POWER_SERVICE);
		        wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, 
		        		"SimpleTimer");
				wakeLock.acquire(); 
				LogToFile. writeToFile(LogToFile.VERBOSE, TAG, "finish");
				finish();
				 /*try {
					Runtime.getRuntime().exec("input keyevent " + KeyEvent.KEYCODE_BACK);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}*/
			}
			
		}
    	
    };
    
   
    
  
    
}
