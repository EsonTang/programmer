package com.android.settings;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.provider.Settings;
import android.util.Log;

import com.mediatek.pq.PictureQuality;
/*prize-add for huyanmode-lihuangyuan-2017-07-04-start*/
import com.mediatek.common.prizeoption.PrizeOption;
/*prize-add for huyanmode-lihuangyuan-2017-07-04-end*/
/**
 * Created by Administrator on 2017/5/3.
 */

public class PrizeBluLightTimeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        Log.d("BluLight","onReceive() action = "+action);
        final ContentResolver mResolver = context.getContentResolver();
        int mTimeType = 0;
        if(/*PrizeBluLightUtil.BOOT_COMPLETED.equals(action)*/
			PrizeBluLightUtil.HUYAN_BOOT_COMPLETED.equals(action)){
            Log.d("BluLight","onReceive() Boot_Completed");
            int mBluLightStatus = Settings.System.getInt(mResolver,
                    Settings.System.PRIZE_BLULIGHT_MODE_STATE, 0);
            int mBluLightTimeStatus = Settings.System.getInt(mResolver,
                    Settings.System.PRIZE_BLULIGHT_TIME_STATE, 0);
            if(mBluLightStatus == 1 || mBluLightTimeStatus == 1){
                mTimeType = PrizeBluLightUtil.BOOT_COMPLETED_TYPE;
            }
        } else {
            if(PrizeBluLightUtil.BLULIGHT_TIME_OPEN_ACTION.equals(action)){
                mTimeType = PrizeBluLightUtil.TIME_START_TYPE;
                Log.d("BluLight","onReceive() StartReceiver");
            } else if(PrizeBluLightUtil.BLULIGHT_TIME_CLOSE_ACTION.equals(action)){
                mTimeType = PrizeBluLightUtil.TIME_END_TYPE;
                Log.d("BluLight","onReceive() OverReceiver");
            }

        }
	  if(PrizeOption.PRIZE_HUYANMODE)
	 {
        	Intent mIntent = new Intent(context,PrizeBluLightService.class);
        	mIntent.putExtra(PrizeBluLightUtil.TIME_TPYE_KEY,mTimeType);
        	context.startService(mIntent);
	 }
    }
}
