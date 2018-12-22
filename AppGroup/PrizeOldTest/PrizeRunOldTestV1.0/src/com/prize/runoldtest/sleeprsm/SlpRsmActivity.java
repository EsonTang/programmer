package com.prize.runoldtest.sleeprsm;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import com.prize.runoldtest.ManualTestActivity;
import com.prize.runoldtest.R;
import com.prize.runoldtest.R.id;
import com.prize.runoldtest.R.layout;
import com.prize.runoldtest.camera.CameraTestActivity;
import com.prize.runoldtest.reboot.RebootActivity;
import com.prize.runoldtest.util.Const;
import com.prize.runoldtest.util.DataUtil;
import com.prize.runoldtest.util.LogToFile;
import com.prize.runoldtest.util.OldTestResult;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemClock;
import android.view.KeyEvent;
import android.view.WindowManager;
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
        DataUtil.addDestoryActivity(SlpRsmActivity.this, "SlpRsmActivity");
        Intent intent=getIntent();
        int  message=intent.getIntExtra(Const.EXTRA_MESSAGE,0);
        TextView textView=(TextView)findViewById(R.id.tvShow);
        textView.setText(message+"");
        if(message>=0){
        	sR_Time = message*2;
        }else{
        	sR_Time=10;
        }
        LogToFile. writeToFile(LogToFile.VERBOSE, TAG, "SRTest begin..."+"\n");
    //  timer.schedule(task, sR_Time*3000);
    }
/*
    TimerTask task = new TimerTask(){
        public void run(){
            try {
            	DataUtil.FlagVideo = true;
            	LogToFile. writeToFile(LogToFile.VERBOSE, TAG, "SRTest finish..."+"\n");
               // Runtime.getRuntime().exec("input keyevent " + KeyEvent.KEYCODE_BACK);
            	finish();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    };
    Timer timer = new Timer();*/
    protected void onStart() {
        super.onStart();

       // timer.schedule(task, sR_Time*3000);
        handler.removeMessages(0x134);
        handler.sendEmptyMessage(0x134);
    }
    
    
    @Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		//  handler.removeMessages(0x134);
		  /*if(wakeLock.isHeld()){
				 wakeLock.release();
			 }*/
		  //SlpRsmActivity.this.finish();
	}
	Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			
			
			if(sR_Time>=0){
				if(isSleep){//如果正在处于休眠，那么唤醒
					pm = (PowerManager) getSystemService(POWER_SERVICE);
			        wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | 
			        		PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, 
			        		"SimpleTimer");
			        
			        wakeLock.acquire();  
			        wakeLock.release(); // 释放
			    	/*getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED  
			                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON  
			                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);*/
			     // 屏幕解锁
			       KeyguardManager keyguardManager = (KeyguardManager)SlpRsmActivity.this
			                .getSystemService(KEYGUARD_SERVICE);
			        KeyguardManager.KeyguardLock keyguardLock = keyguardManager.newKeyguardLock("unLock");
			        // 屏幕锁定
			       keyguardLock.reenableKeyguard();
			       keyguardLock.disableKeyguard(); 
			        
					LogToFile. writeToFile(LogToFile.VERBOSE, TAG, "screenOn"+"\n");
			        android.util.Log.i("cxq", "screenOn");  
			        isSleep=false;
		    	}else{
		    		pm = (PowerManager) getSystemService(POWER_SERVICE);
		    		pm.goToSleep(SystemClock.uptimeMillis());  
		    		LogToFile. writeToFile(LogToFile.VERBOSE, TAG, "screenOff"+"\n");
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
				LogToFile. writeToFile(LogToFile.VERBOSE, TAG, "handleMessage finish"+"\n");
				DataUtil.FlagVideo = true;
				OldTestResult.SrTestresult=true;
				Intent intent = new Intent();
	            //把返回数据存入Intent
	            intent.putExtra("result", "SRTest:PASS");
	            //设置返回数据
	            SlpRsmActivity.this.setResult(RESULT_OK, intent);
	            new Handler().postDelayed(new Runnable(){    
     			    public void run() {    
     			    	  SlpRsmActivity.this.finish();
     			    }    
     			 }, 2000);
	            
	          
				/*try {
					Runtime.getRuntime().exec("input keyevent " + KeyEvent.KEYCODE_BACK);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}*/
			}
			
		}
    	
    };

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		LogToFile. writeToFile(LogToFile.VERBOSE, TAG, "onDestroy "+"\n");
		DataUtil.FlagVideo = true;
		if(wakeLock.isHeld()){
			 wakeLock.release();
		 }
		  handler.removeMessages(0x134);
		
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
		/*Intent intent = new Intent();
        //把返回数据存入Intent
        intent.putExtra("result", "SrTest:FAIL");
        //设置返回数据
        SlpRsmActivity.this.setResult(RESULT_OK, intent);*/
        
       // SlpRsmActivity.this.finish();
		DataUtil.finishBackPressActivity();
	}
    
   
    
  
    
}
