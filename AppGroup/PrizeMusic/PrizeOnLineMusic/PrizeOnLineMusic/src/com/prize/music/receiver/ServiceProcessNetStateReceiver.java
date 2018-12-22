package com.prize.music.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.prize.app.beans.PlayNetInfo;

public class ServiceProcessNetStateReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		// 更新网络类型
		PlayNetInfo.getAPNType(context);

	}

}