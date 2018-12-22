package com.prize.autotest.mmi;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.prize.autotest.AutoConstant;
import com.prize.autotest.FactoryTestApplication;
import com.prize.autotest.R;
import android.app.Activity;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Log;
import android.view.Window;
import android.widget.TextView;
//import android.widget.Toast;

public class AutoWBGTestActivity extends Activity {

	FactoryTestApplication app;
	private List<ScanResult> wifiScanResult;
	private List<DeviceInfo> mDeviceList = new ArrayList<DeviceInfo>();
	LocationManager mLocationManager = null;

	final int OUT_TIME = 60 * 1000;
	boolean gpsResult = false;

	private String sBT;
	private String sWIFI;
	private String sGPS;

	private Location location;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.wbg);

		app = (FactoryTestApplication) getApplication();

		wifiScanResult = app.getWifiScanResult();
		mDeviceList = app.getBluetoothDeviceList();
		
		if(!app.getIsBluetoothScanning() || !app.getIsWifiScanning()){
			if(mDeviceList.size() > 0 && wifiScanResult.size()>0){
				mHandler.sendEmptyMessage(0);
			}else{
				startService(new Intent(this, BluetoothScanService.class));		
				startService(new Intent(this, WifiScanService.class));	
				startTimer();
			}
		}else{
			startTimer();
		}

		startGPS();
		AutoConstant.SendDataToService(AutoConstant.RESULT_SUCCUSS,
				AutoWBGTestActivity.this);
		
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
					if(!app.getIsWifiScanning() && !app.getIsBluetoothScanning()){
						stopTimer();
						wifiScanResult=app.getWifiScanResult();	
						mDeviceList = app.getBluetoothDeviceList();
						mHandler.sendEmptyMessage(0);				
					}		
					if(app.getWifiScanResult().size() > 0 && app.getBluetoothDeviceList().size()>0){
						wifiScanResult = app.getWifiScanResult();
						mDeviceList = app.getBluetoothDeviceList();
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

	CountDownTimer mCountDownTimer = new CountDownTimer(OUT_TIME, 3000) {

		@Override
		public void onTick(long arg0) {

		}

		@Override
		public void onFinish() {
			sGPS = "GPS num:\n" + satelliteList.size() + "GPS List:\n";
			if (gpsResult) {

				sGPS = "GPS num:\n" + satelliteList.size() + "GPS List:\n";
				for (int i = 0; i < satelliteList.size(); i++) {
					sGPS += getString(R.string.satellite) + i + ":" + "\n"
							+ getString(R.string.pr_noice)
							+ satelliteList.get(i).getPrn() + " "
							+ getString(R.string.sn_ratio)
							+ satelliteList.get(i).getSnr();
				}
				TextView mTextGPS = (TextView) findViewById(R.id.gps_list);
				mTextGPS.setText(sGPS);
				AutoConstant.writeFile("GPS : PASS" + "\n" + sGPS + "\n");
				AutoConstant.SendDataToService(AutoConstant.RESULT_SUCCUSS,
						AutoWBGTestActivity.this);

			} else {
				AutoConstant.writeFile("GPS : FAIL" + "\n");
				AutoConstant.SendDataToService(AutoConstant.RESULT_FAIL,
						AutoWBGTestActivity.this);
			}
			finish();
		}
	};

	void startGPS() {

		mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		Settings.Secure.setLocationProviderEnabled(getContentResolver(),
				LocationManager.GPS_PROVIDER, true);

		Settings.Secure.putIntForUser(getContentResolver(),
				Settings.Secure.LOCATION_MODE, 3, UserHandle.USER_CURRENT);

		if (!mLocationManager
				.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)) {
			/*Toast.makeText(this, getString(R.string.gps_enable_first) + "",
					Toast.LENGTH_SHORT).show();*/
			Intent gpsIntent = new Intent();
			gpsIntent.setClassName("com.android.settings",
					"com.android.settings.widget.SettingsAppWidgetProvider");
			gpsIntent.addCategory("android.intent.category.ALTERNATIVE");
			gpsIntent.setData(Uri.parse("custom:3"));
			try {
				PendingIntent.getBroadcast(this, 0, gpsIntent, 0).send();
			} catch (CanceledException e) {
				e.printStackTrace();
			}
		}

		Criteria criteria;
		criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		criteria.setAltitudeRequired(true);
		criteria.setBearingRequired(true);
		criteria.setCostAllowed(true);
		String provider = mLocationManager.getBestProvider(criteria, true);
		if (provider == null) {
			AutoConstant.SendDataToService(AutoConstant.RESULT_FAIL, this);
			return;
		}

		Log.e("liup", "GPS_EVENT_Listener");
		location = mLocationManager
				.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		mLocationManager.addGpsStatusListener(gpsStatusListener);
		mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
				1000, 1, mLocationListener);

	}

	private void setLocationView(Location location) {

		if (location != null) {
			double latitude = location.getLatitude();
			double longitude = location.getLongitude();
			double speed = location.getSpeed();
			double altitude = location.getAltitude();
			double bearing = location.getBearing();
			String value = getString(R.string.latitude) + latitude + '\n'
					+ getString(R.string.longitude) + longitude + '\n'
					+ getString(R.string.speed) + speed + "m/s" + '\n'
					+ getString(R.string.altitude) + altitude + "m" + '\n'
					+ getString(R.string.bearing) + bearing + '\n';
		}
	}

	LocationListener mLocationListener = new LocationListener() {

		public void onLocationChanged(Location location) {

			setLocationView(location);
		}

		public void onProviderDisabled(String provider) {

			setLocationView(null);
		}

		public void onProviderEnabled(String provider) {
			Location location = mLocationManager.getLastKnownLocation(provider);
			setLocationView(location);

		}

		public void onStatusChanged(String provider, int status, Bundle extras) {

		}
	};

	private GpsStatus mGpsStatus;
	private Iterable<GpsSatellite> mSatellites;
	List<GpsSatellite> satelliteList = new ArrayList<GpsSatellite>();
	GpsStatus.Listener gpsStatusListener = new GpsStatus.Listener() {

		public void onGpsStatusChanged(int arg0) {
			switch (arg0) {
			case GpsStatus.GPS_EVENT_STARTED:
				Log.e("liup", "GPS_EVENT_STARTED");
				mCountDownTimer.start();
				setProgressBarIndeterminateVisibility(true);
				break;
			case GpsStatus.GPS_EVENT_STOPPED:
				setProgressBarIndeterminateVisibility(false);
				break;
			case GpsStatus.GPS_EVENT_FIRST_FIX:
				break;
			case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
				mGpsStatus = mLocationManager.getGpsStatus(null);
				mSatellites = mGpsStatus.getSatellites();
				Iterator<GpsSatellite> it = mSatellites.iterator();
				int count = 0;
				satelliteList.clear();
				while (it.hasNext()) {
					GpsSatellite gpsS = (GpsSatellite) it.next();

					float getSnrNum = gpsS.getSnr();
					if (getSnrNum > 25) {
						satelliteList.add(count++, gpsS);
					}
				}
				if (count >= 3) {
					gpsResult = true;
				}
				break;
			default:
				break;
			}

		}

	};

	Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			sWIFI = "WIFI List:\n";
			Log.e("liup", "wifiScanResult.size() = " + wifiScanResult.size());
			Log.e("liup", "mDeviceList.size() = " + mDeviceList.size());
			if (wifiScanResult != null && wifiScanResult.size() > 0) {
				for (int i = 0; i < 10; i++) {// wifiScanResult.size()
					if (wifiScanResult.get(i).SSID.length() < 1) {
						continue;
					}
					sWIFI += " " + i + ": " + wifiScanResult.get(i).SSID
							+ "   " + wifiScanResult.get(i).level + "dBm"
							+ "\n";

				}
				TextView mTextWifi = (TextView) findViewById(R.id.wifi_list);
				mTextWifi.setText(sWIFI);

				AutoConstant.writeFile("WIFI : PASS" + "\n" + sWIFI + "\n");

				AutoConstant.SendDataToService(AutoConstant.RESULT_SUCCUSS,
						AutoWBGTestActivity.this);
			} else {
				AutoConstant.writeFile("WIFI : FAIL" + "\n");
				AutoConstant.SendDataToService(AutoConstant.RESULT_FAIL,
						AutoWBGTestActivity.this);
			}

			sBT = "BT List:\n";
			if (mDeviceList != null && mDeviceList.size() > 0) {

				for (int i = 0; i < mDeviceList.size(); i++) {
					sBT += getResources().getString(R.string.bt_name)
							+ mDeviceList.get(i).getName() + "\n"
							+ getResources().getString(R.string.bt_address)
							+ mDeviceList.get(i).getAddress() + "\n"
							+ getResources().getString(R.string.bt_rssi)
							+ mDeviceList.get(i).getRssi() + "dBm" + "\n";
				}
				TextView mTextBT = (TextView) findViewById(R.id.bt_list);
				mTextBT.setText(sBT);

				AutoConstant.writeFile("BT : PASS" + "\n" + sBT + "\n");
				AutoConstant.SendDataToService(AutoConstant.RESULT_SUCCUSS,
						AutoWBGTestActivity.this);
			} else {
				AutoConstant.writeFile("BT : FAIL" + "\n");
				AutoConstant.SendDataToService(AutoConstant.RESULT_FAIL,
						AutoWBGTestActivity.this);
			}
		};
	};

	@Override
	protected void onDestroy() {
		if (app.getIsBluetoothScanning()) {
			stopService(new Intent(this, BluetoothScanService.class));
		}
		if (app.getIsWifiScanning()) {
			stopService(new Intent(this, WifiScanService.class));
		}

		try {
			Settings.Secure.setLocationProviderEnabled(getContentResolver(),
					LocationManager.GPS_PROVIDER, false);
			mLocationManager.removeUpdates(mLocationListener);
			mLocationManager.removeGpsStatusListener(gpsStatusListener);
			setProgressBarIndeterminateVisibility(true);
		} catch (Exception e) {
		}
		stopTimer();
		if (mCountDownTimer != null) {
			mCountDownTimer.cancel();
		}

		super.onDestroy();
	}

}