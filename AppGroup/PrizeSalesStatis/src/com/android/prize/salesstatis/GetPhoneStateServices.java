package com.android.prize.salesstatis;

import com.android.prize.salesstatis.util.IService;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

public class GetPhoneStateServices extends Service {

	private static final String TAG = "PrizeSalesStatis";
	
	private class MyBinder extends IService.Stub{ 
		 
		public String getImei() throws RemoteException{
    		Log.i(TAG, "[GetPhoneStateServices]--getImei()");
    		String curImei = "";
    		CTelephoneInfo telephonyInfo = CTelephoneInfo.getInstance(GetPhoneStateServices.this);
    		telephonyInfo.setPhoneInfo();
    		String imeiSIM1 = telephonyInfo.getImeiSIM1();
    		String imeiSIM2 = telephonyInfo.getImeiSIM2();
    		String meidSIM1 = telephonyInfo.getMeidSIM1();
    		String meidSIM2 = telephonyInfo.getMeidSIM2();
    		if (imeiSIM1 != null) {
    			curImei = imeiSIM1;
    			if (meidSIM2 != null) {
    				curImei = curImei + "," + meidSIM2;
    			} else if (imeiSIM2 != null) {
    				curImei = curImei + "," + imeiSIM2;
    			}
    		} else if (meidSIM1 != null) {
    			curImei = meidSIM1;
    			if (imeiSIM2 != null) {
    				curImei = curImei + "," + imeiSIM2;
    			} else if (meidSIM2 != null) {
    				curImei = curImei + "," + meidSIM2;
    			}
    		}
    		return curImei;
    	}
	     
	  } 

    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        Log.d(TAG, "ServiceServer onBind");
        return new MyBinder();
    }

    public void onCreate() {
        // TODO Auto-generated method stub
        Log.d(TAG, "ServiceServer onCreate");
        super.onCreate();
    }

    public void onDestroy() {
        // TODO Auto-generated method stub
        Log.d(TAG, "ServiceServer onDestroy");
        super.onDestroy();
    }

    public void onStart(Intent intent, int startId) {
        // TODO Auto-generated method stub
        Log.d(TAG, "ServiceServer onStart");
        super.onStart(intent, startId);
    }

    public boolean onUnbind(Intent intent) {
        // TODO Auto-generated method stub
        Log.d(TAG, "ServiceServer onUnbind");
        return super.onUnbind(intent);
    }
}
