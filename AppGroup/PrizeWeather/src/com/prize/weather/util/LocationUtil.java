package com.prize.weather.util;

import android.content.Context;
import android.util.Log;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;

public class LocationUtil{
	public LocationClient mLocationClient = null;
	public MyLocationListenner myListener;
	
	public String getCity(Context context){
		Log.d("hekeyi","getCity........");
		getLocation(context);
		return null;
	}

	 /**
	 * ����������
	 * @param ������ ˵��
	 * @return �������� ˵��
	 * @see ����/��������/��������#������
	 */
	private void getLocation(Context context) {
		mLocationClient = new LocationClient(context.getApplicationContext());
		myListener = new MyLocationListenner();
		mLocationClient.registerLocationListener(myListener);
	}
	
	public class MyLocationListenner implements BDLocationListener{
		
		 /**
		 * ����������
		 * @param ������ ˵��
		 * @return �������� ˵��
		 * @see ����/��������/��������#������
		 */
		@Override
		public void onReceiveLocation(BDLocation location) {			
			Log.d("hekeyi", "receivelocation location = "+location);
			if (location == null)
				return;
			Log.d("hekeyi", "city = "+location.getCity());
		}

		
		 /**
		 * ����������
		 * @param ������ ˵��
		 * @return �������� ˵��
		 * @see ����/��������/��������#������
		 */
		public void onReceivePoi(BDLocation arg0) {			
			Log.d("hekeyi","onReceivePoi.......");
			
		}
		
	}
}