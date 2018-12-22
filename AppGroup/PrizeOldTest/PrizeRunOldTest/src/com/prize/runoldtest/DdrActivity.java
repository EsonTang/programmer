package com.prize.runoldtest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import com.prize.runoldtest.util.Const;
import com.prize.runoldtest.util.LogToFile;
import com.prize.runoldtest.util.LogUtil;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.widget.TextView;

public class DdrActivity extends Activity {
	private long ddr_circles;

	private static final String TAG = null;
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
		KeyguardManager mKeyguardManager= (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);  
        KeyguardManager.KeyguardLock mKeyguardLock = mKeyguardManager.newKeyguardLock("unLock");  
        mKeyguardLock.disableKeyguard(); 
        mPowerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);   
        mWakeLock = mPowerManager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "bright");   
        
		Intent intent = getIntent();
		app = (PrizeRunOldTestApplication) getApplication();
		ddr_circles = intent.getIntExtra(Const.EXTRA_MESSAGE, 0);
		
		if(ddr_circles == 0){
			ddr_circles = Integer.parseInt(app.getSharePref().getValue(Const.SHARE_PREF_DDR_TEST_CIRCLES));
			app.getSharePref().putValue(Const.SHARE_PREF_DDR_TEST, "0");
		}else{
			ddr_circles = 1;
			app.getSharePref().putValue(Const.SHARE_PREF_DDR_TEST_CIRCLES, String.valueOf(ddr_circles));
			app.getSharePref().putValue(Const.SHARE_PREF_DDR_TEST, "1");
			startReboot();
			return;
		}
		
		test_result = (TextView) findViewById(R.id.tvShow);
		test_result.setText(String.valueOf(ddr_circles));
		mDdrTestResult = "Start Date : " + getSystemDate()+"\n";
		mHandler.sendEmptyMessageDelayed(STRAT_DDR_TEST, 20000);
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
			String comand = "memtester " + formatFileSize(getAvailableMemory(DdrActivity.this)) + " " + ddr_circles;
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
				LogUtil.e("Finish Activity...");
				Intent intentAtaInfo = new Intent(DdrActivity.this, SingleTestActivity.class);
				intentAtaInfo.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intentAtaInfo);
				finish();
				break;
			case STRAT_DDR_TEST:
				LogUtil.e(" start ddr test ...");
				new Thread(mRunnable).start();
				break;
			default:
				super.handleMessage(msg);
				break;
			}
		}
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

	public void Memtester(String command) {
		Runtime r = Runtime.getRuntime();
		Process mProcess;
		BufferedReader mBufferedReader;
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
			e.printStackTrace();
		} catch (InterruptedException e) {
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
	protected void onStop() {
		super.onStop();
		mWakeLock.release();
	}
	
	private void startReboot(){
        Intent rebootIntent = new Intent(Intent.ACTION_REBOOT);
        rebootIntent.putExtra("nowait", 1);
        rebootIntent.putExtra("interval", 1);
        rebootIntent.putExtra("window", 0);
        sendBroadcast(rebootIntent);
        LogUtil.e("DDR Test reboot...");
    }

}
