/*******************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：开机启动服务
 *当前版本：
 *作	者：zhongweilin
 *完成日期：20150408
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

package com.prize.smart;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import android.os.SystemProperties;
import com.mediatek.common.prizeoption.PrizeOption;

public class BootCompletedReceiver extends BroadcastReceiver {
	private static final String TAG = "prize";
	private static final String ACTION = "android.intent.action.BOOT_COMPLETED";
	
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		Log.v(TAG, "***PrizeGene**BootCompleted******");
		if (intent.getAction().equals(ACTION)) {
			if (PrizeOption.PRIZE_FLIP_SILENT) {
				Log.v(TAG, "*****start TurnOverToPauseService******");
				Intent turnOverServiceIntent = new Intent(context, TurnOverToPauseService.class);
				context.startService(turnOverServiceIntent);
			}
			if (PrizeOption.PRIZE_POCKET_MODE) {
				Log.v(TAG, "*****start PocketModeService******");
				Intent pocketServiceIntent = new Intent(context, PocketModeService.class);
				context.startService(pocketServiceIntent);
			}
			if (PrizeOption.PRIZE_ANTIFAKE_TOUCH) {
				Log.v(TAG, "*****start AntiFakeTouchService******");
				Intent mAntiFakeTouchServiceIntent = new Intent(context, AntiFakeTouchService.class);
				context.startService(mAntiFakeTouchServiceIntent);
			}
			if (PrizeOption.PRIZE_SMART_ANSWER_CALL) {
				Log.v(TAG, "*****start SmartAnswerCallService******");
				Intent mSmartAnswerCallServiceIntent = new Intent(context, SmartAnswerCallService.class);
				context.startService(mSmartAnswerCallServiceIntent);
			}
			
			if (PrizeOption.PRIZE_NON_TOUCH_OPERATION ) {
				Log.v(TAG, "*****start DistanceOperationService******");
				Intent mStent = new Intent(context, DistanceOperationService.class);
				context.startService(mStent);
			}
		}
	}
}
