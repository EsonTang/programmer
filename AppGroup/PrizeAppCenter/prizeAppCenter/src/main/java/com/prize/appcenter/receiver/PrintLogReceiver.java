package com.prize.appcenter.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.prize.app.util.JLog;

/***
 * 决定是否打印日志
 * 
 * @author fanjunchen
 * 
 */
public class PrintLogReceiver extends BroadcastReceiver {
	/** 开启日志打印功能 */
	private final String LOG_ENABLE = "prize_appcenter_log_on";
	/** 关闭日志打印功能 */
	private final String LOG_DISENABLE = "prize_appcenter_log_off";

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		String act = intent.getAction();
		if (LOG_ENABLE.equals(act)) {
			JLog.setDebug(true);
		} else if (LOG_DISENABLE.equals(act)) {
			JLog.setDebug(false);
		}
	}

}
