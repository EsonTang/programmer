/*******************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：
 *当前版本：1.0.0
 *作	者：yiyi
 *完成日期：2015年7月23日
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
package com.prize.cloud.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.prize.cloud.service.AccountService;
import com.prize.cloud.util.CloudIntent;

/**
 * 监听类，主要功能为启动定时更新passport 注：已废除，不需要此功能了
 * 
 * @author yiyi
 * @version 1.0.0
 */
public class CloudReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
	

		if (action.equalsIgnoreCase("android.net.conn.CONNECTIVITY_CHANGE")) {

			ConnectivityManager manager = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo ni = manager.getActiveNetworkInfo();
			if (ni != null && ni.isConnected()) {

				Intent it = new Intent(context, AccountService.class);
				it.setAction(CloudIntent.ACTION_ACTIVATE_IN_BACKGROUND);
				context.startService(it);

				PollMgr.startPollingService(context, PollMgr.IntervalMillis,
						AccountService.class,
						CloudIntent.ACTION_ACTIVATE_IN_BACKGROUND);
			}
		} else if (action.equalsIgnoreCase(Intent.ACTION_BOOT_COMPLETED)) {
			PollMgr.startPollingService(context, PollMgr.IntervalMillis,
					AccountService.class,
					CloudIntent.ACTION_ACTIVATE_IN_BACKGROUND);
		}
	}

}
