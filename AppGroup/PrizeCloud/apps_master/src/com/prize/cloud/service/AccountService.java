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
package com.prize.cloud.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.lidroid.xutils.util.LogUtils;
import com.prize.cloud.task.LoginTask;
import com.prize.cloud.task.TaskCallback;
import com.prize.cloud.util.CloudIntent;

/**
 * 用于自动激活
 * @author yiyi
 * @version 1.0.0
 */
public class AccountService extends Service {

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent != null) {
			String action = intent.getAction();
			LogUtils.i("onStartCommand: " + action);
			if (action.equals(CloudIntent.ACTION_ACTIVATE_IN_BACKGROUND)) {
				autoActivate();
			}
		}
		return START_NOT_STICKY;
	}

	private void autoActivate() {

		new LoginTask(getApplicationContext(), new TaskCallback<String>() {

			@Override
			public void onTaskSuccess(String data) {
				LogUtils.i("自动刷新passport:" + data);

			}

			@Override
			public void onTaskError(int errorCode, String msg) {
				LogUtils.i("自动刷新passport失败 :" + msg);

			}
		}).doExecute();
		stopSelf();
	}

}
