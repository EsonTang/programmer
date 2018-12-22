package com.pr.scuritycenter.network;

import tmsdk.bg.creator.ManagerCreatorB;
import tmsdk.bg.module.network.INetworkChangeCallBack;
import tmsdk.bg.module.network.NetworkManager;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.pr.scuritycenter.R;
import com.pr.scuritycenter.db.dao.NetworkInfoDao;

public class NetworkActivity extends Activity {
	private Button 	 mStartStopButton;
	private TextView mContentShower;
	private NetworkManager mNetworkManager;
	private INetworkChangeCallBack mCallbackForMobile;
	private INetworkChangeCallBack mCallbackForWIFI;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.network_activity);
		//启动&停止按钮
		mStartStopButton = (Button) findViewById(R.id.star_stop_btn);
		mContentShower = (TextView) findViewById(R.id.content_shower);
	
		mNetworkManager = ManagerCreatorB.getManager(NetworkManager.class);
		//INTERVAL_FOR_REALTIME = 2;最短时间的刷新，适配一些极端情况
		mNetworkManager.setInterval(NetworkManager.INTERVAL_FOR_REALTIME);
		mStartStopButton.setText(mNetworkManager.isEnable() ? "停止" : "启动");
		//添加默认的Mobile监控器和WIFI监控器
		mNetworkManager.addDefaultMobileMonitor("mobile", NetworkInfoDao.getInstance("mobile"));
		mNetworkManager.addDefaultWifiMonitor("WIFI", NetworkInfoDao.getInstance("WIFI"));
		
		mCallbackForMobile = new NetworkChangeCallBack("mobile", mContentShower);
		mCallbackForWIFI = new NetworkChangeCallBack("WIFI", mContentShower);
		//寻找流量监控器,添加毁掉
		mNetworkManager.findMonitor("mobile").addCallback(mCallbackForMobile);
		mNetworkManager.findMonitor("WIFI").addCallback(mCallbackForWIFI);
		
		
		
		mStartStopButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//监控服务是否开启
				boolean enable = mNetworkManager.isEnable();
				//设置监控服务开关状态
				mNetworkManager.setEnable(!enable);
				mStartStopButton.setText(!enable ? "停止" : "启动");
			}
		});
	}
	
	@Override
	protected void onDestroy() {
		//寻找流量监控器,删除毁掉
		mNetworkManager.findMonitor("mobile").removeCallback(mCallbackForMobile);
		mNetworkManager.findMonitor("WIFI").removeCallback(mCallbackForWIFI);
		super.onDestroy();
	}
}
