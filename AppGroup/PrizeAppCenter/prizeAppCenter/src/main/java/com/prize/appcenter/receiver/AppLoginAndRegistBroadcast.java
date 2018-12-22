package com.prize.appcenter.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * 类描述：监听app登录或者注册的广播
 * 
 * @author huangchangguo
 * @version 1.9
 * 
 *          2016.7.19
 */
public class AppLoginAndRegistBroadcast extends BroadcastReceiver {


	GetReceiver getReceiver;

	@Override
	public void onReceive(Context context, Intent intent) {
		if (getReceiver != null) {
			getReceiver.getReceived(context, intent);

		}

	}

	public interface GetReceiver {

		void getReceived(Context context, Intent intent);

	}

	public void setGetReceiver(GetReceiver getReceiver) {
		this.getReceiver = getReceiver;
	}

}
