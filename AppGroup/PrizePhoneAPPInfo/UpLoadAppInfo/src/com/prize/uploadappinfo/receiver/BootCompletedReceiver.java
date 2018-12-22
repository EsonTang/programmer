package com.prize.uploadappinfo.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.prize.uploadappinfo.constants.Constant;
import com.prize.uploadappinfo.utils.JLog;
import com.prize.uploadappinfo.utils.PollingUtils;

/**
 **
 * 开机启动广播
 * 
 * @author prize
 * @version V1.0
 */
public class BootCompletedReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		JLog.writeFileToSD("BootCompletedReceiver -onReceive---");
		if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
			/* 开机启动闹钟 推送服务 */
			PollingUtils.startPollingService(context,Constant.PUSH_FOR_TIME);

		}
	}

}
