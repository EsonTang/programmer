/*
* Copyright (C) 2014 MediaTek Inc.
* Modification based on code covered by the mentioned copyright
* and/or permission notice(s).
*/
/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.prize.setting;

import com.android.camera.Log;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.text.TextUtils;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
/**
 * 
 **
 * Use Baidu maps to get the location
 * @author wanzhijuan
 * @version V1.0
 */
public class BDLocationManager {
    private static final String TAG = "LocationManager";
    
    private Context mContext;
    private Listener mListener;
    private boolean mRecordLocation;
    
    //////////////////////////////
    private LocationClient mLocationClient;
    private PrizeLocationListener mBDLocationListener = new PrizeLocationListener();
    public static final String ADDR_KEY = "addr_key";
    public static final String ADDR_CITY_KEY = "addr_city_key";
    
    public interface Listener {
        public void showGpsOnScreenIndicator(boolean hasSignal);
        
        public void hideGpsOnScreenIndicator();
    }
    
    public BDLocationManager(Context context, Listener listener) {
        mContext = context;
        mListener = listener;
    }
    
    public Location getCurrentLocation() {
        Location location = null;
        if (mRecordLocation) {
            // go in best to worst order
            location = mBDLocationListener.current();
        }
        return location;
    }
    
    public void recordLocation(boolean recordLocation) {
        Log.d(TAG, "recordLocation(" + recordLocation + ") mRecordLocation=" + mRecordLocation);
        if (mRecordLocation != recordLocation) {
            mRecordLocation = recordLocation;
            if (recordLocation) {
                startReceivingLocationUpdates();
            } else {
                stopReceivingLocationUpdates();
            }
        }
    }
    
    private void startReceivingLocationUpdates() {
        if (mLocationClient == null) {
        	mLocationClient = new LocationClient(mContext);
        }
        if (mLocationClient != null) {
            try {
            	LocationClientOption option = new LocationClientOption();
        		option.setLocationMode(LocationMode.Battery_Saving);// 
        		option.setIsNeedAddress(true);//
        		option.setScanSpan(1000);
        		//option.setOpenGps(true); 
        		mLocationClient.setLocOption(option);	
        		mLocationClient.registerLocationListener(mBDLocationListener); // Register monitor function
        		mLocationClient.start();
            } catch (SecurityException ex) {
                Log.i(TAG, "fail to request location update, ignore", ex);
            } catch (IllegalArgumentException ex) {
                Log.d(TAG, "provider does not exist " + ex.getMessage());
            }
            Log.i(TAG, "startReceivingLocationUpdates");
        }
    }
    
    private void stopReceivingLocationUpdates() {
        if (mLocationClient != null) {
            try {
            	mLocationClient.unRegisterLocationListener(mBDLocationListener); 
            	mLocationClient.stop();
            } catch (Exception ex) {
                Log.i(TAG, "fail to remove location listners, ignore", ex);
            }
            Log.i(TAG, "stopReceivingLocationUpdates");
        }
        if (mListener != null)
            mListener.hideGpsOnScreenIndicator();
    }
    
    /**
	 * Location listener
	 */
	public class PrizeLocationListener implements BDLocationListener {
		
		private BDLocation mLastLocation;
		@Override
		public void onReceiveLocation(BDLocation location) {
			Log.d(TAG, "onReceiveLocation() location=" + location);
			if (location == null) {
				if (mListener != null && mRecordLocation) {
                    mListener.showGpsOnScreenIndicator(false);
                }
				return;
			}
			
			mLastLocation = location;
			Log.i(TAG, "onReceiveLocation, mLastLocation =" + mLastLocation.getAddrStr() + " time:" + System.currentTimeMillis()
					+ " lat=" + mLastLocation.getLatitude() + " long=" + mLastLocation.getLongitude() + " LocType=" + mLastLocation.getLocType()
					+ " =" + BDLocation.TypeGpsLocation + " city=" + mLastLocation.getCity() + " province=" + mLastLocation.getProvince() 
					+ " District=" + mLastLocation.getDistrict() + " getFloor()" + mLastLocation.getFloor() + " getStreet()=" + mLastLocation.getStreet()
					+ " getStreetNumber()=" + mLastLocation.getStreetNumber() + " addr=" + mLastLocation.getAddrStr());
			if (mListener != null && mRecordLocation) {
                mListener.showGpsOnScreenIndicator(true);
            }
		}

        private String getAddr() {
            String address = mLastLocation.getAddrStr();
            String addressLine = mLastLocation.getStreetNumber();
            if (!TextUtils.isEmpty(address) && !TextUtils.isEmpty(addressLine)) {
                int index = address.lastIndexOf(addressLine);
                if (index > 0) {
                    return address.substring(0, index);
                }
            }

            return address;
        }
		
		public Location current() {
			Location location = new Location(android.location.LocationManager.GPS_PROVIDER);
			try {
				location.setLatitude(mLastLocation!=null?mLastLocation.getLatitude():0.0f);
				location.setLongitude(mLastLocation!=null?mLastLocation.getLongitude():0.0f);
				Bundle addrBundle = new Bundle();
				addrBundle.putString(ADDR_KEY, mLastLocation!=null?getAddr():" ");
				addrBundle.putString(ADDR_CITY_KEY, mLastLocation!=null?mLastLocation.getCity():"");
				location.setExtras(addrBundle);
			} catch (Exception e) {
				e.printStackTrace();
			}
            return location;
        }
	};
	
	
	public void stop() {
		if (mLocationClient != null) {
			mLocationClient.stop();
		}
	}
}