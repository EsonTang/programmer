package com.prize.factorytest.WiFi;

import java.util.List;
import com.prize.factorytest.R;
import com.prize.factorytest.PrizeFactoryTestListActivity;
import android.app.Activity;
import android.net.wifi.ScanResult;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.prize.factorytest.FactoryTestApplication;
import java.util.Timer;
import java.util.TimerTask;
import android.util.Log;
import android.content.Intent;
import com.prize.factorytest.Service.WifiScanService;
import android.widget.Toast;
import android.view.KeyEvent;

public class WiFi extends Activity {
	private List<ScanResult> wifiScanResult;
	private TextView mTextView;
	private static String TAG = "PrizeFactoryTest/WiFi";
	private static Button buttonPass;
	private static Button buttonFail;
	FactoryTestApplication app;
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK){
			return true;
		}
		return false;
	}
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wifi);
		buttonPass = (Button) findViewById(R.id.passButton);
		buttonFail = (Button) findViewById(R.id.failButton);
		buttonPass.setEnabled(false);
		bindView();
		app = (FactoryTestApplication) getApplication();
		wifiScanResult=app.getWifiScanResult();
		if(!app.getIsWifiScanning()){		
			Log.e(TAG,"scan finish");
			if(wifiScanResult.size()>0){
				mHandler.sendEmptyMessage(0);
			}else{
				startService(new Intent(WiFi.this, WifiScanService.class));	
				startTimer();
			}
		}else{
			Log.e(TAG,"scannig");
			startTimer();
		}		
		confirmButton();
	}
	
	
	private Timer mTimer = null;  
	private TimerTask mTimerTask = null;
	private void startTimer(){          
		if (mTimer == null) {  
            mTimer = new Timer();  
        }   
        if (mTimerTask == null) {  
            mTimerTask = new TimerTask() {  
                @Override  
                public void run() {
					if(!app.getIsWifiScanning()){
						stopTimer();
						wifiScanResult=app.getWifiScanResult();	
						mHandler.sendEmptyMessage(0);				
					}		
					if(app.getWifiScanResult().size()>0){
						wifiScanResult=app.getWifiScanResult();	
						mHandler.sendEmptyMessage(0);			
					}
                }  
            };  
        } 
        if(mTimer != null && mTimerTask != null )  
            mTimer.schedule(mTimerTask,500,1000);    
    }
	private void stopTimer(){  
        
        if (mTimer != null) {  
            mTimer.cancel();  
            mTimer = null;  
        }  
  
        if (mTimerTask != null) {  
            mTimerTask.cancel();  
            mTimerTask = null;  
        }              
    }

	public void confirmButton() {
		buttonPass.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Log.e(TAG,"buttonPass onClick");
				setResultAndFinishActivity(RESULT_OK);
			}
		});
		buttonFail.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Log.e(TAG,"buttonFail onClick");
				setResultAndFinishActivity(RESULT_CANCELED);
			}
		});
	}
	
	void bindView() {
		mTextView = (TextView) findViewById(R.id.wifi_hint);
		mTextView.setText(getString(R.string.wifi_text));
	}
	static String wifiInfos = "";
	Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			String s = getString(R.string.wifi_text) + "\n\n" + "AP List:\n";
			wifiInfos = "";
			if (wifiScanResult != null && wifiScanResult.size() > 0) {
				buttonPass.setEnabled(true);
				for (int i = 0; i < wifiScanResult.size(); i++) {
					if(wifiScanResult.get(i).SSID.length()<1){
						continue;
					}
					s += " " + i + ": " + wifiScanResult.get(i).SSID + "   " + wifiScanResult.get(i).level +"dBm"+"\n\n";
					wifiInfos += " " + i + ": "
							+ wifiScanResult.get(i).toString() + "\n\n";
					mTextView.setText(s);
				}
			} else {
				Toast.makeText(getBaseContext(), getString(R.string.wifi_scan_null), Toast.LENGTH_SHORT).show();
				setResultAndFinishActivity(RESULT_CANCELED);
			}
		};
	};

	void setResultAndFinishActivity(int resultCode) {
		if (PrizeFactoryTestListActivity.toStartAutoTest == true) {
			PrizeFactoryTestListActivity.itempos++;
		}
		setResult(resultCode);
		finish();
	}
	@Override
	protected void onDestroy() {
		Log.e(TAG,"onDestroy");
		stopTimer();
		super.onDestroy();		
	}
}
