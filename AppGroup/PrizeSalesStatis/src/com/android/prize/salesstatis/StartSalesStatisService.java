/*******************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：
 *当前版本：
 *作	者：
 *完成日期：
 *修改记录：
 *修改日期：
 *版 本 号：
 *修 改 人：
 *修改内容：
...
 *修改记录：
 *修改日期：
 *版 本 号：
 *修 改 人：
 *修改内容：
 *********************************************/

package com.android.prize.salesstatis;

import com.android.prize.salesstatis.util.PhoneStute;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.provider.Settings;
import android.util.Log;

public class StartSalesStatisService extends Service {
	private static final String TAG = "PrizeSalesStatis";
	private static final String ACTION_CONNECTIVITY_CHANGE = "android.net.conn.CONNECTIVITY_CHANGE";

	// 声明定位
	private LocationClient mLocationClient;
	private BDLocation location;
	private static boolean netState = false;
	private ClientInfo clientInfo;
	private BroadcastReceiver myReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo activeInfo = manager.getActiveNetworkInfo();
			int salesstatis = Settings.System.getInt(context.getContentResolver(),
					Settings.System.PRIZE_SALES_STATIS_NET, 0);
			if (salesstatis == 0) {
				stopSelf();
			}
			String position = clientInfo.getPosition();
			Log.v(TAG, "[StartSalesStatisService]ConnectivityManager----> position = " + position);
			if (position == null || position.equals("") || position.equals(ClientInfo.UNKNOWN)) {
				Log.v(TAG, "[StartSalesStatisService]ConnectivityManager---->location.isStarted() " + mLocationClient.isStarted());
				if (activeInfo != null) {
					netState = true;
					Log.v(TAG, "[StartSalesStatisService]ConnectivityManager---->activeInfo ");
					if (!mLocationClient.isStarted()) {
						mLocationClient.start();
					}else{
						mLocationClient.stop();
						mLocationClient.start();
					}
				} else {
					netState = false;
					Log.v(TAG, "[StartSalesStatisService]ConnectivityManager---->activeInfo is null; ");
					if (mLocationClient.isStarted()) {
						mLocationClient.stop();
					}
				}
			}
		}
	};

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		clientInfo = ClientInfo.getInstance(getApplicationContext());
		registerReceiverAction();
		initLocationClient(getApplicationContext());
		Log.v(TAG, "[StartSalesStatisService]---onCreate() ----");
		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		Log.v(TAG, "[StartSalesStatisService]---onStartCommand ----");
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		unRegisterReceiverAction();
		super.onDestroy();
	}

	private void registerReceiverAction() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(ACTION_CONNECTIVITY_CHANGE);
		registerReceiver(myReceiver, filter);
	}

	private void unRegisterReceiverAction() {
		unregisterReceiver(myReceiver);
	}

	public Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			String ms = (String) msg.obj;
			clientInfo.setPosition(ms);
			// 停止定位
			mLocationClient.stop();
			Log.v(TAG, "[StartSalesStatisService]----BDLocationListener over----Position = " + ms);
			sendPacelableBundle();
			stopSelf();
		};
	};
	
	public void sendPacelableBundle(){
        PhoneStute mPhoneStute = new PhoneStute();
        mPhoneStute.latitude = clientInfo.getLatitude();
        mPhoneStute.longitude = clientInfo.getLongitude();
        mPhoneStute.position = clientInfo.position;
        Intent mIntent = new Intent();
        mIntent.setClass(StartSalesStatisService.this, SalesStatisService.class);
        Bundle mBundle = new Bundle();
        mBundle.putParcelable("addr", mPhoneStute);// 传递Book对象
        mIntent.putExtras(mBundle);
        StartSalesStatisService.this.startService(mIntent);
    }

	/**
	 * 初始化定位
	 */
	private void initLocationClient(Context context) {
		Log.v(TAG, "[StartSalesStatisService]----BDLocationClient init----");
		mLocationClient = new LocationClient(context); // 声明LocationClient类
		mLocationClient.registerLocationListener(myListener); // 注册监听函数

		LocationClientOption option = new LocationClientOption();
		option.setLocationMode(LocationMode.Battery_Saving);// 设置定位模式
		option.setIsNeedAddress(true);// 返回的定位结果包含地址信息
		// option.setOpenGps(true); //开启gps,前提是手机硬件开启了gps
		mLocationClient.setLocOption(option);
	}

	/**
	 * 定位监听器
	 */
	public BDLocationListener myListener = new BDLocationListener() {
		@Override
		public void onReceiveLocation(BDLocation location) {
			if (location == null)
				return;
			// 拿到地址信息
			Log.v(TAG, "[StartSalesStatisService]----BDLocationClient over----");
			String addr = location.getAddrStr();
			if (addr != null) {
				clientInfo.setLatitude(location.getLatitude());
				clientInfo.setLongitude(location.getLongitude());
				Message message = Message.obtain();
				message.obj = addr;
				handler.sendMessage(message);
			}
		}
	};

}
