package com.android.dialer.service;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import com.cootek.smartdialer_oem_module.sdk.CooTekPhoneService;
import android.os.SystemProperties;
import com.mediatek.common.prizeoption.PrizeOption;

public class LocationService extends Service {
	private static final String TAG = "LocationService";
	public class LocationServiceImpl extends ILocationService.Stub {
		@Override
		public String getLocationInfo(String phoneNumber) throws RemoteException {
			return getCooTekLocation(phoneNumber);
		}
	}

	public String getCooTekLocation(String phoneNumber) {
        if(PrizeOption.PRIZE_COOTEK_SDK){
            if (CooTekPhoneService.isInitialized()) {
                //Foreign Phone Number
                if(phoneNumber != null && phoneNumber.startsWith("+")){
                    String foreignPhoneAttr = CooTekPhoneService.getInstance().getPhoneAttribute(phoneNumber);
                    if(foreignPhoneAttr != null){
						return foreignPhoneAttr;
					}
				}
				//Local Phone Number
				if(phoneNumber != null ){
					String localPhoneAttr = CooTekPhoneService.getInstance().getPhoneAttribute(phoneNumber);
					if(localPhoneAttr != null){
						return localPhoneAttr;
					}
				}
			}
		}
		return "";
    }
	
	@Override
	public void onCreate() {
		super.onCreate();
		Log.v(TAG, "onCreate called");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.v(TAG, "onDestory() called");
	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		Log.v(TAG, "onStart() called");
	}

	@Override
	public IBinder onBind(Intent intent) {
		Log.v(TAG, "onBind() called");
		return new LocationServiceImpl();
	}

}
