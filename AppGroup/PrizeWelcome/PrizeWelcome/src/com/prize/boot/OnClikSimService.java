/*******************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：
 *当前版本：
 *作	者：
 *完成日期：
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

package com.prize.boot;

import com.android.internal.telephony.IccCardConstants;
import com.prize.boot.util.CTelephoneInfo;
import com.prize.boot.util.Utils;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.telephony.SubscriptionManager;
import android.util.Log;

public class OnClikSimService extends Service {
	private static final String TAG = "prize";
	private static final String ACTION_SIM_STATE_CHANGE = "android.intent.action.SIM_STATE_CHANGED";
	
	BroadcastReceiver mSIMStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null && action.equals(ACTION_SIM_STATE_CHANGE)) {
            	Log.v(Utils.TAG, "--ACTION_SIM_STATE_CHANGED--> sim : flag " );
                String newState = intent.getStringExtra(IccCardConstants.INTENT_KEY_ICC_STATE);
                if (!newState.equals(IccCardConstants.INTENT_VALUE_ICC_ABSENT)
                        && !newState.equals(IccCardConstants.INTENT_VALUE_ICC_NOT_READY)) {
                    // if we don't get one sim slot has sim card
                    // we set that slot was checked.
                	Log.v(Utils.TAG, "--ACTION_SIM_STATE_CHANGED--> sim : true " );
                	isSimExist();
                	stopSelf();
                } else if (newState.equals(IccCardConstants.INTENT_VALUE_ICC_ABSENT)) {
                    // if we don't get one sim slot has sim card
                    // we set that slot was checked.
                	Log.v(Utils.TAG, "--ACTION_SIM_STATE_CHANGED--> sim : false " );
                }
            }
        };
    };

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public void onCreate() {
		Log.v(Utils.TAG, "--OnClikSimService--> onCreate() " );
		registerReceiverAction();
		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.v(Utils.TAG, "--OnClikSimService--> onStartCommand() " );
		return super.onStartCommand(intent, flags, startId);
	}
	@Override
	public void onDestroy() {
		Log.v(Utils.TAG, "--OnClikSimService--> onDestroy() " );
		unRegisterReceiverAction();
		super.onDestroy();
	}

	private void registerReceiverAction() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(ACTION_SIM_STATE_CHANGE);
		registerReceiver(mSIMStateReceiver, filter);
	}
	
	private void unRegisterReceiverAction() {
		unregisterReceiver(mSIMStateReceiver);  
	}
	
	private boolean isSimExist() {
        CTelephoneInfo telephonyInfo = CTelephoneInfo.getInstance(this);
		telephonyInfo.setCTelephoneInfo();
		boolean sim1State = telephonyInfo.isSIM1Ready();
		boolean sim2State = telephonyInfo.isSIM2Ready();
		if(sim2State && !sim1State){
			Log.v(Utils.TAG, "--zwl--> selPosDefault: setDefaultDataSubId");
	    	final SubscriptionManager subscriptionManager = SubscriptionManager.from(this);
	        subscriptionManager.setDefaultDataSubId(1);
		}
		return (sim1State || sim2State);
    }
}
