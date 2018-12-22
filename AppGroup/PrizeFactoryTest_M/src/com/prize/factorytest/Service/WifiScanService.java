package com.prize.factorytest.Service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import java.util.List;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.CountDownTimer;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.content.BroadcastReceiver;
import com.prize.factorytest.FactoryTestApplication;
public class WifiScanService extends Service {
    private static final String TAG = "WifiScanService";
	FactoryTestApplication app;
	Context mContext;
	/*wifi*/
	private WifiLock mWifiLock;
	private WifiManager mWifiManager;
	private List<ScanResult> wifiScanResult;
	final int SCAN_INTERVAL = 3000;
	final int OUT_TIME = 30000;
	IntentFilter mFilter = new IntentFilter();
	private boolean scanResultAvailable = false;
	private boolean wifiScanFinish = false;
	/*wifi*/
	
    public class ScanServiceBinder extends Binder {
        public WifiScanService getService() {
            return WifiScanService.this;
        }
    }
    private ScanServiceBinder mBinder = new ScanServiceBinder();
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
		super.onCreate();
		mContext=this;
		app = (FactoryTestApplication) getApplication();
		startWifiScan();		
    }

	@Override
	public void onDestroy() {
		Log.e(TAG,"TaskWifiScan onDestroy");
		unregisterReceiver(mReceiver);		
		try {
			mCountDownTimer.cancel();
			if (true == mWifiLock.isHeld()){
				enableWifi(false);
				mWifiLock.release();
			}
		} catch (Exception e) {
		}
		super.onDestroy();		
	}
	private void startWifiScan(){
		app.setIsWifiScanning(true);
		mWifiManager = (WifiManager) mContext
				.getSystemService(Context.WIFI_SERVICE);
		/** Keep Wi-Fi awake */
		mWifiLock = mWifiManager.createWifiLock(
				WifiManager.WIFI_MODE_SCAN_ONLY, "WiFi");
		if (false == mWifiLock.isHeld())
			mWifiLock.acquire();

		if(mWifiManager.getWifiState()==WifiManager.WIFI_STATE_DISABLED){
			Log.e(TAG,"TaskWifiScan enableWifi");
			enableWifi(true);
		}
		mFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
		mFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
		registerReceiver(mReceiver, mFilter);		
		mCountDownTimer.start();
	}
	public boolean getWifiScanState(){
		return wifiScanFinish;
	}
	
	private void enableWifi(boolean enable) {
		if (mWifiManager != null)
			mWifiManager.setWifiEnabled(enable);
	}
	
	CountDownTimer mCountDownTimer = new CountDownTimer(OUT_TIME, SCAN_INTERVAL) {
		@Override
		public void onFinish() {	
			wifiScanResult = mWifiManager.getScanResults();
			app.setIsWifiScanning(false);
			scanResultAvailable = true;
			wifiScanFinish = true;
			mHandler.sendEmptyMessage(0);
			WifiScanService.this.stopSelf();
		}
		@Override
		public void onTick(long arg0) {
			// At least conduct startScan() 3 times to ensure wifi's scan
			if (mWifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED) {
				Log.e(TAG,"TaskWifiScan startScan");
				mWifiManager.startScan();
			}
		}
	};
	
	Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {			
			if (wifiScanResult != null && wifiScanResult.size() > 0) {
				app.setWifiScanResult(wifiScanResult);
				Log.e(TAG,"TaskWifiScan size ="+wifiScanResult.size());
			} else {
				
			}
		};
	};	
	private BroadcastReceiver mReceiver = new BroadcastReceiver() {
		public void onReceive(Context c, Intent intent) {
			if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(intent
					.getAction())) {
				Log.e(TAG,"TaskWifiScan mReceiver ="+mWifiManager.getScanResults().size());
				if (!scanResultAvailable) {
					wifiScanResult = mWifiManager.getScanResults();
					//scanResultAvailable = true;
					mHandler.sendEmptyMessage(0);
				}
			}
		}
	};	
}
