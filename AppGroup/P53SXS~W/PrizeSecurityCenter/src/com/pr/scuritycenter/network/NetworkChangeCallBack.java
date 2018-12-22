package com.pr.scuritycenter.network;

import tmsdk.bg.module.network.INetworkChangeCallBack;
import tmsdk.common.module.network.NetworkInfoEntity;
import android.widget.TextView;

public final class NetworkChangeCallBack implements INetworkChangeCallBack {
	private String mName;
	private TextView mShower;
	
	public NetworkChangeCallBack(String name, TextView shower) {
		mName = name;
		mShower = shower;
	}
	
	//当到达月结日时回调
	@Override
	public void onClosingDateReached() {
		
	}
	
	// 当Day发生变化时回调
	@Override
	public void onDayChanged() {
		
	}
	
	//当流量有发生变化时 
	@Override
	public void onNormalChanged(final NetworkInfoEntity arg0) {
		mShower.post(new Runnable() {

			@Override
			public void run() {
				// 刷新
				synchronized (NetworkChangeCallBack.class) {
					String content = String.format("%s: dayused %d retail %d", mName, arg0.mUsedForDay, arg0.mRetialForMonth) + "\n";
					if (mShower.getLineCount() <= 20) {
						content = mShower.getText().toString() + content;
					}
					mShower.setText(content);
				}
			}
		});
	}
}
