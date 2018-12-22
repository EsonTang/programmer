package com.prize.appcenter.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.text.TextUtils;

import com.prize.app.constants.Constants;
import com.prize.app.util.DataStoreUtils;
import com.prize.appcenter.service.PrizeAppCenterService;
import com.prize.appcenter.ui.util.PollingUtils;

/**
 ** 
 * 开机启动广播
 * 
 * @author longbaoxiu
 * @version V1.0
 */
public class BootCompletedReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {

		if (intent!=null&& !TextUtils.isEmpty(intent.getAction())&&intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
			DataStoreUtils.saveLocalInfo(com.prize.app.constants.Constants.PHONE_BOOOT_TIME,System.currentTimeMillis()+ "");
			/* 开机启动闹钟 推送服务 */
			PollingUtils.startPollingService(context, Constants.PUSH_FOR_TIME,
					PrizeAppCenterService.class,
					PrizeAppCenterService.MSG_ACTION);
			/* 开机启动闹钟 垃圾扫描服务 */
			PollingUtils.startPollingService(context, Constants.PUSH_FOR_TIME,
					PrizeAppCenterService.class,
					PrizeAppCenterService.TRASH_SCAN_ACTION);
			if (Build.VERSION.SDK_INT < 23) {//大于等于23的系统版本的已经增加了控制最多显示6条；而小于23的 因为含有就签名，以防万一，还是保留
				PollingUtils.setFixedTime(context);
			}

		}
	}

}
