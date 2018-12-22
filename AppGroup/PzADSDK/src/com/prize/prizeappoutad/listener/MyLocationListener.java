package com.prize.prizeappoutad.listener;

import java.util.Properties;

import android.content.Context;

import com.baidu.location.Address;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.prize.prizeappoutad.bean.ClientInfo;
import com.prize.prizeappoutad.constants.Constants;
import com.prize.prizeappoutad.utils.JLog;
import com.prize.prizeappoutad.utils.MTAUtils;
import com.tencent.stat.StatService;

/**
 * 百度定位监听
 * 
 * @author haungchangguo 2016.10.26
 */
public class MyLocationListener implements BDLocationListener {
	private ClientInfo mClientInfo;
	private Context mContext;
	private LocationClient mLocationClient;
	private GetAdressSuccess mAdressSuccess;

	public MyLocationListener(Context context, LocationClient locationClient,
			GetAdressSuccess getAdressSuccess) {
		this.mLocationClient = locationClient;
		this.mContext = context;
		this.mAdressSuccess = getAdressSuccess;
	}

	@Override
	public void onReceiveLocation(BDLocation location) {
		// 设置位置信息
		Address address = null;
		MTAUtils.BDLocation(mContext);
		mClientInfo = ClientInfo.getInstance(mContext);
		try {
			address = location.getAddress();
			mClientInfo.setAddress(address);
		} catch (Exception e) {
			JLog.i("huang-MyLocationListener.getAddress.Exception: ",
					e.toString());
		}
		JLog.i("huang-MyLocationListener.getAddress: ", address.address);
		mLocationClient.stop();
		mAdressSuccess.getAdressSuccess();
	}

	public interface GetAdressSuccess {
		void getAdressSuccess();
	}

}
