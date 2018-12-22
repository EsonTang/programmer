package com.prize.runoldtest.ddr;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.prize.runoldtest.PrizeRunOldTestApplication;
import com.prize.runoldtest.R;
import com.prize.runoldtest.RunAllTestActivity;
import com.prize.runoldtest.SingleTestActivity;
import com.prize.runoldtest.cpu.CpuTestActivity;
import com.prize.runoldtest.util.Const;
import com.prize.runoldtest.util.DataUtil;
import com.prize.runoldtest.util.LogToFile;
import com.prize.runoldtest.util.LogUtil;
import com.prize.runoldtest.util.OldTestResult;
import com.prize.runoldtest.video.VideoActivity;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences.Editor;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.view.WindowManager;
import android.widget.TextView;

public class DdrActivity extends Activity {
	private long ddr_circles;

	private static final String TAG = "DDRTest";
	private static final int REFRESH_VIEW = 1;
	private static final int FINISH_ACTIVITY = 2;
	private static final int STRAT_DDR_TEST = 3;
	private static DecimalFormat fileDecimalFormat = new DecimalFormat("#0.#");
	private TextView test_result = null;
	
	static String test_setText = "";
	static int Testing_times = 0;
	String MemtesterFileName = "/data/memtester_result.txt";
	String complexMemtester_1 = "memtester 3G 3";
	String mDdrTestResult = "";
	
	PowerManager mPowerManager = null;
	PowerManager.WakeLock mWakeLock = null;
	
	private PrizeRunOldTestApplication app;
	
	private MyHandler mHandler = new MyHandler();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ddr);
		/*KeyguardManager mKeyguardManager= (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);  
        KeyguardManager.KeyguardLock mKeyguardLock = mKeyguardManager.newKeyguardLock("unLock");  
        mKeyguardLock.disableKeyguard(); */
		 getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED  
	               | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON  
	               | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);  
        mPowerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);   
        mWakeLock = mPowerManager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "bright");   
        LogToFile. writeToFile(LogToFile.VERBOSE, TAG, "DDRTest begin.."+"\n");
		Intent intent = getIntent();
		app = (PrizeRunOldTestApplication) getApplication();
		
		if(getIntent().hasExtra(Const.EXTRA_MESSAGE)){
			ddr_circles = intent.getIntExtra(Const.EXTRA_MESSAGE, 0);
			app.getSharePref().putValue(Const.SHARE_PREF_DDR_TEST_CIRCLES, String.valueOf(ddr_circles));
		}else{
			ddr_circles = Integer.parseInt(app.getSharePref().getValue(Const.SHARE_PREF_DDR_TEST_CIRCLES));
		}
		
		DataUtil.addDestoryActivity(DdrActivity.this, "DdrActivity");
		
	    String str=	app.getSharePref().getValue(Const.SHARE_PREF_DDR_TEST).trim();
		if(str.equals("")||str.equals("0")){
			if(ddr_circles>=1){
				actReboot();
			}else{
				finish();
			}
			return;
		}else {
			ddr_circles = Integer.parseInt(app.getSharePref().getValue(Const.SHARE_PREF_DDR_TEST_CIRCLES));
			app.getSharePref().putValue(Const.SHARE_PREF_DDR_TEST, "0");
		}
		
		test_result = (TextView) findViewById(R.id.tvShow);
		test_result.setText(String.valueOf(ddr_circles+1));
		mDdrTestResult = "Start Date : " + getSystemDate()+"\n";
		mHandler.sendEmptyMessageDelayed(STRAT_DDR_TEST, 2000);
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		LogToFile. writeToFile(LogToFile.VERBOSE, TAG, "DDRTest onpause.."+"\n");
		 if(mWakeLock.isHeld()){
			 mWakeLock.release();
		 }
	}
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		mWakeLock.acquire();
	}

	private String getSystemDate() {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日    HH:mm:ss");
		Date curDate = new Date(System.currentTimeMillis());
		return formatter.format(curDate);
	}

	public Runnable mRunnable = new Runnable() {
		@Override
		public void run() {
			String comand = "memtester " + formatFileSize(getAvailableMemory(DdrActivity.this)) + " 1";
			LogUtil.e("comand = " + comand);
			Memtester(comand);
		}
	};
	
	private class MyHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case REFRESH_VIEW:
				test_result.setText(mDdrTestResult);
				break;
			case FINISH_ACTIVITY:
				LogUtil.e("Finish Activity...FINISH_ACTIVITY");
				 LogToFile. writeToFile(LogToFile.VERBOSE, TAG, "DDRTest finish.."+"\n");
				
				 int ddrtests=Integer.parseInt(app.getSharePref().getValue(Const.SHARE_PREF_DDR_TEST_CIRCLES));
				 if(ddrtests==0){
					 LogToFile. writeToFile(LogToFile.VERBOSE, TAG, "DDRTest finish.ddrtests=0"+"\n");
					 LogToFile. writeToFile(LogToFile.VERBOSE, TAG, "DDRTest finish.DataUtil.isManualTst:"+DataUtil.isManualTst+"\n");
					 DataUtil.resetFlag(); 
					 OldTestResult.DdrTestresult=true;
					
				        if( DataUtil.isTwlfTest){
				        	DataUtil.isTwlfTest=false;
				        	 LogToFile. writeToFile(LogToFile.VERBOSE, TAG, "DDRTest finish.istwlfTest=true"+"\n");
				        	SharedPreferences sharedPreferences =getSharedPreferences("twlftesttime", DdrActivity.MODE_PRIVATE); 
					  		int testtime=sharedPreferences.getInt("twlftesttimes", 0);
					        if(testtime>0){
					        	LogToFile. writeToFile(LogToFile.VERBOSE, TAG, "DDRTest finish.testtime>0"+"\n");
					        	DataUtil.FlagCpu=true;
					        	
					        	DdrActivity.this.finish(); 
					        }else{
					        	LogToFile. writeToFile(LogToFile.VERBOSE, TAG, "DDRTest finish.testtime<=0"+"\n");
					        	DataUtil.FlagtlfDdrFinalFinish=true;
					        	DataUtil.FlagDdr=false;
					        	showResultTip();
					        }
				        }else if(DataUtil.isSixTest){
				        	DataUtil.isSixTest=false;
				        	LogToFile. writeToFile(LogToFile.VERBOSE, TAG, "DDRTest finish.isSixTest is true"+"\n");
				        	SharedPreferences sharedPreferences =getSharedPreferences("sixtesttime", DdrActivity.MODE_PRIVATE); 
					  		int testtime=sharedPreferences.getInt("sixtesttimes", 0);
					        if(testtime>0){
					        	LogToFile. writeToFile(LogToFile.VERBOSE, TAG, "DDRTest finish.isSixTesttes ttime>0"+"\n");
					        	DataUtil.FlagCpu=true;
					        	
					        	DdrActivity.this.finish(); 
					        }else{
					        	LogToFile. writeToFile(LogToFile.VERBOSE, TAG, "DDRTest finish.isSixTesttes ttime<=0"+"\n");
					        	DataUtil.FlagsixDdrFinalFinish=true;
					        	DataUtil.FlagDdr=false;
					        	showResultTip();
					        }
				        }else if(DataUtil.isfourTest){
				        	DataUtil.isfourTest=false;
				        	LogToFile. writeToFile(LogToFile.VERBOSE, TAG, "DDRTest finish.isfourTest is true"+"\n");
				        	SharedPreferences sharedPreferences =getSharedPreferences("fourtesttime", DdrActivity.MODE_PRIVATE); 
					  		int testtime=sharedPreferences.getInt("fourtesttimes", 0);
					  	  if(testtime>0){
					        	LogToFile. writeToFile(LogToFile.VERBOSE, TAG, "DDRTest finish.isfourTesttes ttime>0"+"\n");
					        	DataUtil.FlagCpu=true;
					        	DdrActivity.this.finish(); 
					        }else{
					        	LogToFile. writeToFile(LogToFile.VERBOSE, TAG, "DDRTest finish.isfourTesttes ttime<=0"+"\n");
					        	DataUtil.FlagfourDdrFinalFinish=true;
					        	DataUtil.FlagDdr=false;
					        	showResultTip();
					        }
				        }
				        else if(DataUtil.isManualTst){
				        	SharedPreferences sharedPreferences =getSharedPreferences("manulddrtime", DdrActivity.MODE_PRIVATE); 
					  		int ddrtesttime=sharedPreferences.getInt("manulddrtimes", 0);
					  		 Editor editor = sharedPreferences.edit();
						  		editor.putInt("manulddrtimes", ddrtesttime-1);		
						  		editor.commit();
						  		LogToFile. writeToFile(LogToFile.VERBOSE, TAG, "DDRTest finish.isManualTst ddrtesttime-1:"+(ddrtesttime-1)+"\n");
					  		if(ddrtesttime-1>0){
					  			
					  			app.getSharePref().putValue(Const.SHARE_PREF_DDR_TEST, "1");
					  			startReboot();
					  		}else{
					  			DataUtil.ManualTestFinish=true;
					  			DataUtil.FlagDdr=false;
					  			showResultTip();
					  		}
				        	
				        } else{
				        	showResultTip();
				        }
				        
					
					 
				 }else if(ddrtests>0){
					 actReboot();
				 }
				
				break;
			case STRAT_DDR_TEST:
				LogUtil.e(" start ddr test ...");
				 LogToFile. writeToFile(LogToFile.VERBOSE, TAG, "DDRTest strat.."+"\n");
			//	 getActivity();
				new Thread(mRunnable).start();
				break;
			default:
				super.handleMessage(msg);
				break;
			}
		}
	} 
	
	/*private void getActivity(){
		ActivityManager am = (ActivityManager) DdrActivity.this.getSystemService(ACTIVITY_SERVICE);  
        List<ActivityManager.RunningTaskInfo> list = am.getRunningTasks(1);  
         String mstr="";
        for (ActivityManager.RunningTaskInfo taskInfo : list) {
        	 
        	 
        	 mstr=mstr+taskInfo.topActivity.getShortClassName();
        	 LogToFile. writeToFile(LogToFile.VERBOSE, TAG, "getActivity.mstr:"+mstr+"\n");
	}
	}*/
	AlertDialog dialog=null;
	private void showResultTip(){
		String str1=this.getResources().getString(R.string.testresult);
    	String message="DDRTest:PASS";
    	LogToFile. writeToFile(LogToFile.VERBOSE, TAG, "DDRTest finish.showResultTip"+"\n");
		String str3=this.getResources().getString(R.string.sure);
		 AlertDialog.Builder builder = new AlertDialog.Builder(this);  
    	 builder.setTitle(str1) ;
    	 builder.setMessage(message) ;
    	 builder.setPositiveButton(str3,  new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				// TODO Auto-generated method stub
				dialog.dismiss();
				DataUtil.FlagDdr=false;
				DdrActivity.this.finish();
			}
		} );
    	 dialog= builder.show();
	}
	
	
	private void actReboot(){
		 int ddrtests=Integer.parseInt(app.getSharePref().getValue(Const.SHARE_PREF_DDR_TEST_CIRCLES));
		app.getSharePref().putValue(Const.SHARE_PREF_DDR_TEST_CIRCLES, String.valueOf(ddrtests-1));
		app.getSharePref().putValue(Const.SHARE_PREF_DDR_TEST, "1");
		startReboot();
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		LogToFile. writeToFile(LogToFile.VERBOSE, TAG, "DDRTest onDestroy.."+"\n");
		super.onDestroy();
		
	}

	public static long getAvailableMemory(Context context) {
		ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
		am.getMemoryInfo(memoryInfo);
		return memoryInfo.availMem;
	}

	public static String formatFileSize(long size) {
		DecimalFormat df = fileDecimalFormat;
		LogUtil.e("memory before size = " + size);
		size = size - 5000;
		String fileSizeString = "0M";
		if (size < 1024 && size > 0) {
			fileSizeString = df.format(size) + "B";
		} else if (size < 1024 * 1024) {
			fileSizeString = df.format(size / 1024) + "K";
		} else {
			fileSizeString = df.format(size / (1024 * 1024)) + "M";
		}
		LogUtil.e("memory after size = " + fileSizeString);
		return fileSizeString;
	}
	Process mProcess;
	BufferedReader mBufferedReader;
	//int mmmr=3;
	public void Memtester(String command) {
		Runtime r = Runtime.getRuntime();
		
		Testing_times++;
		try {
			mProcess = r.exec(command);
			mBufferedReader = new BufferedReader(new InputStreamReader(mProcess.getInputStream()));
			String inline;
			while ((inline = mBufferedReader.readLine()) != null) {
				
					if (inline.length() > 1000) {
						inline = inline.substring(0, 1000);
					}
					mDdrTestResult += inline + "\n";
					mHandler.sendEmptyMessage(REFRESH_VIEW);
					LogUtil.e(inline);
					System.out.println(inline);
					 LogToFile. writeToFile(LogToFile.VERBOSE, TAG, "DDRTest is going"+"\n");
			
			
			}
			mBufferedReader.close();
			mProcess.waitFor();
			ReadTxtFile(MemtesterFileName);
			LogUtil.e("2222222222.");
			mDdrTestResult += "Finish Date : " + getSystemDate()+"\n";
			mHandler.sendEmptyMessage(REFRESH_VIEW);
			mHandler.sendEmptyMessage(FINISH_ACTIVITY);
			LogToFile.writeToFile(mDdrTestResult);
		} catch (IOException e) {
			LogToFile. writeToFile(LogToFile.VERBOSE, TAG, "DDRTest happen error.."+"\n");
			finish();
			e.printStackTrace();
		} catch (InterruptedException e) {
			LogToFile. writeToFile(LogToFile.VERBOSE, TAG, "DDRTest happen error.."+"\n");
			finish();
			e.printStackTrace();
		}
	}

	public String ReadTxtFile(String strFilePath) {
		String path = strFilePath;
		String content = "";
		File file = new File(path);
		if (file.isDirectory()) {
			LogUtil.e("The File doesn't not exist.");
		} else {
			try {
				InputStream instream = new FileInputStream(file);
				if (instream != null) {
					InputStreamReader inputreader = new InputStreamReader(instream);
					BufferedReader buffreader = new BufferedReader(inputreader);
					String line;
					while ((line = buffreader.readLine()) != null) {
						content += line + "\n";
						// if(inline.equals("SUCCESS")){
						if (line.indexOf("SUCCESS") != -1) {
							test_setText += Testing_times + " : " + "SUCCESS \n";
							test_result.setText(test_setText);
						} else {
							test_setText += Testing_times + " : " + "FAIL \n";
							test_result.setText(test_setText);
						}
					}
					instream.close();
				}
			} catch (java.io.FileNotFoundException e) {
				LogUtil.e("Exception The File doesn't not exist.");
			} catch (IOException e) {
				LogUtil.e(e.getMessage());
			}
		}
		return content;
	}
	
	
	 @Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		// timer.schedule(task, 90000);
	}
	  /*TimerTask task = new TimerTask(){
	        public void run(){
	        	try {
					mBufferedReader.close();
					mProcess.waitFor();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
	        	mHandler.removeMessages(REFRESH_VIEW);
				mHandler.removeMessages(FINISH_ACTIVITY);
	        	finishDdr();
	        	
	        }
	    };*/
	    
	//Timer timer = new Timer();
	/*private void finishDdr(){
		 LogToFile. writeToFile(LogToFile.VERBOSE, TAG, "DDRTest finish.ddrtests=0"+"\n");
		 
		 DataUtil.resetFlag(); 
		 OldTestResult.DdrTestresult=true;
		
	        if( DataUtil.isTwlfTest){
	        	DataUtil.isTwlfTest=false;
	        	 LogToFile. writeToFile(LogToFile.VERBOSE, TAG, "DDRTest finish.istwlfTest=true"+"\n");
	        	SharedPreferences sharedPreferences =getSharedPreferences("twlftesttime", DdrActivity.MODE_PRIVATE); 
		  		int testtime=sharedPreferences.getInt("twlftesttimes", 0);
		        if(testtime>0){
		        	LogToFile. writeToFile(LogToFile.VERBOSE, TAG, "DDRTest finish.testtime>0"+"\n");
		        	DataUtil.FlagCpu=true;
		        	
		        	DdrActivity.this.finish(); 
		        }else{
		        	LogToFile. writeToFile(LogToFile.VERBOSE, TAG, "DDRTest finish.testtime<=0"+"\n");
		        	DataUtil.FlagtlfDdrFinalFinish=true;
		        	DataUtil.FlagDdr=false;
		        	
		        	showResultTip();
		        }
	        }else if(DataUtil.isSixTest){
	        	DataUtil.isSixTest=false;
	        	LogToFile. writeToFile(LogToFile.VERBOSE, TAG, "DDRTest finish.isSixTest is true"+"\n");
	        	SharedPreferences sharedPreferences =getSharedPreferences("sixtesttime", DdrActivity.MODE_PRIVATE); 
		  		int testtime=sharedPreferences.getInt("sixtesttimes", 0);
		        if(testtime>0){
		        	LogToFile. writeToFile(LogToFile.VERBOSE, TAG, "DDRTest finish.isSixTesttes ttime>0"+"\n");
		        	DataUtil.FlagCpu=true;
		        	
		        	DdrActivity.this.finish(); 
		        }else{
		        	LogToFile. writeToFile(LogToFile.VERBOSE, TAG, "DDRTest finish.isSixTesttes ttime<=0"+"\n");
		        	DataUtil.FlagsixDdrFinalFinish=true;
		        	DataUtil.FlagDdr=false;
		        	showResultTip();
		        }
	        }else if(DataUtil.isManualTst){
	        	SharedPreferences sharedPreferences =getSharedPreferences("manualddrtesttime", DdrActivity.MODE_PRIVATE); 
		  		int ddrtesttime=sharedPreferences.getInt("manualddrtesttimes", 0);
	        	DataUtil.isManualTst=false;
	        	if(ddrtesttime>0){
	        		
	        	}else{
	        		
	        	}
	        	
	        }
	        else{
	        	showResultTip();
	        }
	}*/

	@Override
	protected void onStop() {
		super.onStop();
		 if(mWakeLock.isHeld()){
			 mWakeLock.release();
		 }
	}
	
	private void startReboot(){
		LogToFile. writeToFile(LogToFile.VERBOSE, TAG, "ddrtest~~Reboot.."+"\n");
        Intent rebootIntent = new Intent(Intent.ACTION_REBOOT);
        rebootIntent.putExtra("nowait", 1);
        rebootIntent.putExtra("interval", 1);
        rebootIntent.putExtra("window", 0);
        sendBroadcast(rebootIntent);
        LogUtil.e("DDR Test reboot...");
    }

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
		DataUtil.finishBackPressActivity();
		DataUtil.resetFlag();
		DataUtil.FlagtlfDdrFinalFinish=false;
		DataUtil.FlagsixDdrFinalFinish=false;
	}

}
