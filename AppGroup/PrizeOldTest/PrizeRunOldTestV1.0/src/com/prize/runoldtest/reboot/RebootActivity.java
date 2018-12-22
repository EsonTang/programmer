package com.prize.runoldtest.reboot;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import com.prize.runoldtest.PrizeRunOldTestApplication;
import com.prize.runoldtest.R;
import com.prize.runoldtest.RunInMainActivity;
import com.prize.runoldtest.R.id;
import com.prize.runoldtest.R.layout;
import com.prize.runoldtest.R.string;
import com.prize.runoldtest.ddr.DdrActivity;
import com.prize.runoldtest.sleeprsm.SlpRsmActivity;
import com.prize.runoldtest.util.Const;
import com.prize.runoldtest.util.DataUtil;
import com.prize.runoldtest.util.LogToFile;
import com.prize.runoldtest.util.LogUtil;
import com.prize.runoldtest.util.OldTestResult;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.view.WindowManager;
import android.widget.TextView;

public class RebootActivity extends Activity {
	private static final int MSG_START_FROM_REBOOT = 0;
	private static final int REBOOT_TIME = 8;

	private PrizeRunOldTestApplication app;
	private CountDownTimer mCountDownTimer;
	
	private int rebootTotalTime = 0;
	private int rebootCurTime = 0;
	 private  static final String Faile = "/sys/class/hw_info/hw_info_data/hw_info_resulf";
	 private  static final String Succese = "/sys/class/hw_info/hw_info_data/hw_info_read";
	 private TextView Failetext;
	 private TextView SuccessText;
	 private TextView ShowText;
	 private String TAG="RuninRebootTest";
	 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_reboot);
		Failetext=(TextView)findViewById(R.id.textfaile);
		SuccessText=(TextView)findViewById(R.id.textsuccess);
		ShowText=(TextView)findViewById(R.id.showtest);
		SuccessText.setMovementMethod(ScrollingMovementMethod.getInstance());
		app = (PrizeRunOldTestApplication) getApplication();
		DataUtil.addDestoryActivity(RebootActivity.this, "RebootActivity");
		//rebootTotalTime = getIntent().getIntExtra(Const.SHARE_PREF_REBOOT_TOTALTIMES, 0);
		rebootTotalTime = Integer.parseInt(app.getSharePref().getValue(Const.SHARE_PREF_REBOOT_TOTALTIMES));
		rebootCurTime = Integer.parseInt(app.getSharePref().getValue(Const.SHARE_PREF_REBOOT_CURTIMES));
		LogUtil.e("RebootActivity...intent = " + getIntent().hasExtra(Const.EXTRA_MESSAGE));
		initRebootState();
		LogToFile. writeToFile(LogToFile.VERBOSE, TAG, "RebootTest  begin.."+"\n");
		
	}
	

	private void initRebootState() {
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED  
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON  
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		if(getIntent().hasExtra(Const.EXTRA_MESSAGE)){
			LogToFile. writeToFile(LogToFile.VERBOSE, TAG, "hasExtraConst.EXTRA_MESSAGE"+"\n");
			rebootTotalTime = getIntent().getIntExtra(Const.EXTRA_MESSAGE, 0);
		}
			app.getSharePref().putValue(Const.SHARE_PREF_REBOOT_TOTALTIMES, String.valueOf(rebootTotalTime));
			app.getSharePref().putValue(Const.SHARE_PREF_REBOOT_SELECTE, "1");
		//	mHandler.sendEmptyMessage(MSG_START_FROM_REBOOT);
		
		if (getIntent().hasExtra("reboot") && getIntent().getExtras().getBoolean("reboot")) {
			LogToFile. writeToFile(LogToFile.VERBOSE, TAG, "hasExtra(reboot)"+"\n");
			ShowDeviceMessage();
		}
			mHandler.sendEmptyMessage(MSG_START_FROM_REBOOT);
	}

	Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_START_FROM_REBOOT:
				handleReboot();
				break;
			}
			super.handleMessage(msg);
		}
	};

	private void handleReboot() {
		LogToFile. writeToFile(LogToFile.VERBOSE, TAG, "onTick rebootTotalTime:"+rebootTotalTime+"rebootCurTime:"+rebootCurTime);
		LogUtil.e("oldtest rebooting...rebootTotalTime = "+rebootTotalTime+", rebootCurTime = "+rebootCurTime);
		if (rebootTotalTime > rebootCurTime) {
			mCountDownTimer = new CountDownTimer(REBOOT_TIME * 1000, 1000) {
				@Override
				public void onFinish() {
					LogToFile. writeToFile(LogToFile.VERBOSE, TAG, "goning one reboot.."+"\n");
					startReboot();
				}
				@Override
				public void onTick(long arg0) {
					LogToFile. writeToFile(LogToFile.VERBOSE, TAG, "onTick  bebooting.."+"\n");
					LogUtil.e("oldtest rebooting...");
				}
			}.start();
		} 
		/*KeyguardManager keyguardManager = (KeyguardManager)RebootActivity.this
                .getSystemService(KEYGUARD_SERVICE);
        KeyguardManager.KeyguardLock keyguardLock = keyguardManager.newKeyguardLock("unLock");
     
       keyguardLock.reenableKeyguard();
       keyguardLock.disableKeyguard();*/
       LogToFile. writeToFile(LogToFile.VERBOSE, TAG, " bebooting final finish.."+"\n");
	}
	
	
	private void ShowDeviceMessage(){//read device message 
		  BufferedReader bufReader = null;
		  try {
			bufReader = new BufferedReader(new FileReader(Faile));
		String	prop ="";
		String FailerAllInfo="";
		while( (prop=bufReader.readLine())!=null){
		//	LogToFile. writeToFile(LogToFile.VERBOSE, "MainActivity:", prop);
			
			FailerAllInfo=FailerAllInfo+"\n"+prop;
			LogToFile. writeToFile(LogToFile.VERBOSE, "MainActivity:", FailerAllInfo+"\n");
			}
		if(FailerAllInfo!=""){

			Failetext.setText(getResources().getString(R.string.failemessage)+FailerAllInfo+"\n");
			Failetext.setTextColor(Color.RED);
		}else{
			Failetext.setText("PASS"+"\n");
			Failetext.setTextColor(Color.GREEN);
		}
		
			String info="";
			String SuccessInfo="";
			 BufferedReader bufReader2 = null;
			 bufReader2 = new BufferedReader(new FileReader(Succese));
			 while( (info=bufReader2.readLine())!=null){
					LogToFile. writeToFile(LogToFile.VERBOSE, "MainActivity:", info+"\n");
					
					SuccessInfo=SuccessInfo+"\n"+info;
					}
			 SuccessText.setText(getResources().getString(R.string.successmessage)+"\n"+SuccessInfo);
		
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	private void startReboot(){
		app.getSharePref().putValue(Const.SHARE_PREF_REBOOT_CURTIMES, String.valueOf(rebootCurTime+1));
        if((rebootCurTime+1)==rebootTotalTime){
        	 LogToFile. writeToFile(LogToFile.VERBOSE, TAG, "the Lastone Reboot test atonce"+"\n");
        	app.getSharePref().putValue(Const.SHARE_PREF_REBOOT_SELECTE, "0");
        	app.getSharePref().putValue(Const.SHARE_PREF_REBOOT_CURTIMES, "0");
        	OldTestResult.RebootTestresult=true;
        }
        Intent rebootIntent = new Intent(Intent.ACTION_REBOOT);
        rebootIntent.putExtra("nowait", 1);
        rebootIntent.putExtra("interval", 1);
        rebootIntent.putExtra("window", 0);
        sendBroadcast(rebootIntent);
        LogToFile. writeToFile(LogToFile.VERBOSE, TAG, "rebootCurTime:"+rebootCurTime+"\n");
        LogUtil.e("oldtest start reboot...");
       
    }
	
	@Override
	protected void onDestroy() {
		if(mCountDownTimer!=null){
            mCountDownTimer.cancel();
        }
		
		super.onDestroy();
		
	}


	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		
	}
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
		rebootTotalTime=0;
		app.getSharePref().putValue(Const.SHARE_PREF_REBOOT_TOTALTIMES, String.valueOf(rebootTotalTime));
		if(mCountDownTimer!=null){
			mCountDownTimer.cancel();
		}
		app.getSharePref().putValue(Const.SHARE_PREF_REBOOT_SELECTE, "0");
		app.getSharePref().putValue(Const.SHARE_PREF_REBOOT_TOTALTIMES, "0");
		app.getSharePref().putValue(Const.SHARE_PREF_REBOOT_CURTIMES, "0");
		DataUtil.FlagRebootFinish=false;
		OldTestResult.RebootTestresult=false;
		DataUtil.finishBackPressActivity();
		DataUtil.resetFlag();
	}


}
