package com.prize.compass;

import android.app.Application;
import android.widget.TextView;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.prize.compass.R;

public class LocationApplication extends Application {
	private static LocationApplication instance;
	public LocationClient mLocationClient = null;
	public String mData;
	public String address;
	public String str_city;
	public String str_province;
	public MyLocationListenner myListener = new MyLocationListenner();
	public TextView mTv;
	public TextView mmCity;
	public TextView mmProvince;

	public static LocationApplication getInstance() {
		return instance;
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		instance = this;
		mLocationClient = new LocationClient(getApplicationContext());
		mLocationClient.registerLocationListener(myListener);
		setLocationOption();
		super.onCreate();
	}


	public void setLocationOption() {
		LocationClientOption option = new LocationClientOption();
		option.setProdName("Compass");
		option.setOpenGps(true); 
		option.setCoorType("bd09ll"); 
		option.setAddrType("all"); 
		option.setScanSpan(5000); 
		option.setPriority(LocationClientOption.GpsFirst);
		mLocationClient.setLocOption(option);
	}


	public class MyLocationListenner implements BDLocationListener {

		@Override
		public void onReceiveLocation(BDLocation location) {
			// TODO Auto-generated method stub
			if (location == null)
				return;
			StringBuffer sb = new StringBuffer(256);
			double latitude = location.getLatitude();
			double longitude = location.getLongitude();
			String latitudeStr;
			String longitudeStr;
			// sb.append(location.getTime());
            if(getFlagLocationString(latitude).equals("000") && getFlagLocationString(longitude).equals("000")){
            	
            }else{
			if (latitude >= 0.0f) {
				latitudeStr = getString(R.string.direction_north,
						getLocationString(latitude));
			} else {
				latitudeStr = getString(R.string.direction_south,
						getLocationString(-1.0 * latitude));
			}

			if (longitude >= 0.0f) {
				longitudeStr = getString(R.string.direction_east,
						getLocationString(longitude));
			} else {
				longitudeStr = getString(R.string.direction_west,
						getLocationString(-1.0 * longitude));
			}
			sb.append(latitudeStr);
			sb.append(" ");
			sb.append(longitudeStr);
            }
			mData = sb.toString();
			if (mTv != null)
				mTv.setText(mData);
			if (location.getLocType() == BDLocation.TypeNetWorkLocation) {
				str_province = location.getProvince();
				str_city = location.getCity();
			} else {
				str_province = location.getProvince();
				str_city = location.getCity();
			}
			if (mmProvince != null)
				mmProvince.setText(str_province);
			if (mmCity != null)
				mmCity.setText(str_city);
		}

		private String getLocationString(double input) {
			int du = (int) input;
			int fen = (((int) ((input - du) * 3600))) / 60;
			int miao = (((int) ((input - du) * 3600))) % 60;
			return String.valueOf(du) + "° " + String.valueOf(fen) + " ′ "
					+ String.valueOf(miao) + " ″ ";
		}
		private String getFlagLocationString(double input) {
			int du = (int) input;
			int fen = (((int) ((input - du) * 3600))) / 60;
			int miao = (((int) ((input - du) * 3600))) % 60;
			return String.valueOf(du) + String.valueOf(fen) + String.valueOf(miao);
		}


	}
}
