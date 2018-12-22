package com.android.prize.salesstatis;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
public class ClickSimStateService extends Service {
	private static final String TAG = "PrizeSalesStatis";
	private static final String ACTION_SIM_STATE_CHANGE = "android.intent.action.SIM_STATE_CHANGED";

	private static int MAX_SIM_STATE_TIME = 250;
	private static boolean simStateSales = false;
	private static int simInterpositionTimer = 0;
	private static int simTimerCount = 0;
	
	// 信息实体类
	private ClientInfo clientInfo;

	private BroadcastReceiver myTimeChangeReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(Intent.ACTION_TIME_TICK)) {
				Log.v(TAG, "[ClickSimStateService]-time change--> simInterpositionTimer = " +simInterpositionTimer + " , MAX_SIM_STATE_TIME = "+MAX_SIM_STATE_TIME);
				Log.v(TAG, "[ClickSimStateService]-time change--> simStateSales = " +simStateSales);
				if (simInterpositionTimer > MAX_SIM_STATE_TIME) {
					startSalesStatisService();
					stopSelf();
				}
				if (simStateSales) {
					simInterpositionTimer++;
				}
			}
		}
	};

	private BroadcastReceiver myReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(ACTION_SIM_STATE_CHANGE)) {
				Log.v(TAG, "[ClickSimStateService]--->simTimerCount = " + simTimerCount
						+ ",simInterpositionTimer = " + simInterpositionTimer + ",simStateSales = " + simStateSales + "-----> before");
				TelephonyManager tm = (TelephonyManager) context.getSystemService(Service.TELEPHONY_SERVICE);
				int state = tm.getSimState();
				switch (state) {
					case TelephonyManager.SIM_STATE_UNKNOWN:
					case TelephonyManager.SIM_STATE_ABSENT:
						simStateSales = false;
						simTimerCount++;
						if (simTimerCount > 3) {
							simTimerCount = 0;
							simInterpositionTimer = 0;
						}
						break;
					case TelephonyManager.SIM_STATE_READY:
					case TelephonyManager.SIM_STATE_PIN_REQUIRED:
					case TelephonyManager.SIM_STATE_PUK_REQUIRED:
					case TelephonyManager.SIM_STATE_NETWORK_LOCKED:
						simStateSales = true;
						break;
					default:
						break;
				}
				Log.v(TAG, "[ClickSimStateService]-->simTimerCount = " + simTimerCount
						+ ",simInterpositionTimer = " + simInterpositionTimer + ",simStateSales = " + simStateSales + "-----> after");
			}
        }
	};

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
        getTimeMin();
		registerReceiverAction();
		Log.v(TAG, "[ClickSimStateService]---onStartCommand()----");
		return super.onStartCommand(intent, flags, startId);
	}
    /**prize-add-by-zhongweilin-20151023_salesstatic_test-start*/
    public static final String AUTOHORITY = "com.prize.salesstatic";
    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.prize.salesstatic";
    public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.prize.salesstatic";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTOHORITY + "/prizesalesstatictables");
    public static final String MIN = "time_min";
    private void getTimeMin(){
        ContentResolver  contentResolver = this.getContentResolver(); 
        try{
            Cursor cursor = contentResolver.query(CONTENT_URI, new String[] {MIN}, null, null, null);
            if(cursor!=null){
                while (cursor.moveToNext()) {
                    int ret = Integer.valueOf(cursor.getString(cursor.getColumnIndex(MIN)));
                    if(ret > 0){
                        MAX_SIM_STATE_TIME = ret;
                    }
                    Log.v(TAG, "[ClickSimStateService]=getSalesSatic Test Data :" + MAX_SIM_STATE_TIME);
                }
                cursor.close();  //查找后关闭游标
            }
        }catch(Exception e) {
            Log.v(TAG, "[ClickSimStateService]==cursor==Exception :" + e.toString());
        }              
    }
    /**prize-add-by-zhongweilin-20151023_salesstatic_test-end*/

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		unRegisterReceiverAction();
		Log.v(TAG, "[ClickSimStateService]---onDestroy()---");
		super.onDestroy();
	}

	private void registerReceiverAction() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(ACTION_SIM_STATE_CHANGE);
		registerReceiver(myReceiver, filter);
		registerReceiver(myTimeChangeReceiver, new IntentFilter(Intent.ACTION_TIME_TICK));
	}

	private void unRegisterReceiverAction() {
		unregisterReceiver(myReceiver);
		unregisterReceiver(myTimeChangeReceiver);
	}

	private void startSalesStatisService() {
		int salesstatis = Settings.System.getInt(getContentResolver(), Settings.System.PRIZE_SALES_STATIS_NET, 0);
		Log.v(TAG, "[ClickSimStateService]**Network**salesstatis = " + salesstatis);
		if (salesstatis == 1) {
			Intent mServiceIntent = new Intent(this, StartSalesStatisService.class);
			this.startService(mServiceIntent);
		}
		int salesstatismms = Settings.System.getInt(getContentResolver(), Settings.System.PRIZE_SALES_STATIS_MMS, 0);
		Log.v(TAG, "[ClickSimStateService]**MMS**salesstatismms = " + salesstatismms);
		if (salesstatismms == 1) {
			Intent mAutoSmsServiceIntent = new Intent(this, AutoSendSmsService.class);
			this.startService(mAutoSmsServiceIntent);
		}
	}
}
