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

package com.android.prize;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import com.android.prize.salesstatis.AutoSendSmsService;
import com.android.prize.salesstatis.ClickSimStateService;
import com.android.prize.salesstatis.StartSalesStatisService;

public class BootCompletedReceiver extends BroadcastReceiver {
	private static final String TAG = "PrizeSalesStatis";
	private final static String PROP_FIRST = "persist.sys.konkagene_is_first";
	private static final String ACTION = "android.intent.action.BOOT_COMPLETED";
	private static final String	PROJECT_VERSION = "project_version";
	
	@Override
	public void onReceive(Context context, Intent intent) {
		SharedPreferences mySharedPreferences= context.getSharedPreferences("test", Activity.MODE_PRIVATE); 
		SharedPreferences.Editor editor = mySharedPreferences.edit();
		// TODO Auto-generated method stub
		Log.v(TAG, "*****BootCompleted******");
		if (intent.getAction().equals(ACTION)) {
			Log.v(TAG, "*****start SalesStatisService******");
			int salesstatis = Settings.System.getInt(context.getContentResolver(), Settings.System.PRIZE_SALES_STATIS_NET, 0);
			int salesstatismms = Settings.System.getInt(context.getContentResolver(), Settings.System.PRIZE_SALES_STATIS_MMS, 0);
			String projectversionpre = mySharedPreferences.getString(PROJECT_VERSION, "");
			String projectversionprecur = Build.DISPLAY;
			Log.v(TAG, "**SalesStatis--->projectversionpre = "+projectversionpre+", projectversionprecur = "+projectversionprecur);
			if(!projectversionpre.equals(projectversionprecur)){
				salesstatis = 1;
				Settings.System.putInt(context.getContentResolver(), Settings.System.PRIZE_SALES_STATIS_NET, 1);
				editor.putString(PROJECT_VERSION, projectversionprecur);
				editor.commit(); 
			}
			
            Log.v(TAG, "**SalesStatisService--->salesstatismms = "+salesstatismms+", salesstatis = "+salesstatis);
			if(salesstatis == 1 || salesstatismms == 1){
				Intent mStent = new Intent(context, ClickSimStateService.class);
				context.startService(mStent);
			}
		}
	}
}
