package com.prize.uploadappinfo.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.prize.uploadappinfo.bean.ClientInfo;
import com.prize.uploadappinfo.service.UploadService;
import com.prize.uploadappinfo.utils.PollingUtils;

public class NetStateReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		// 更新网络类型
		ClientInfo.getAPNType(context);
		if (ClientInfo.networkType !=ClientInfo.NONET) {
			PollingUtils.startService(context,
					UploadService.NET_CONNECTED);
		} else {
			// //////网络断开

		}

	}

}