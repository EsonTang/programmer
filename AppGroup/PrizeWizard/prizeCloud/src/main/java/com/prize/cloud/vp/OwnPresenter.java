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
package com.prize.cloud.vp;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.telephony.SmsManager;
import android.telephony.SubscriptionManager;
import android.widget.Toast;

import com.prize.cloud.R;
import com.prize.cloud.activity.RegSuccessActivity;
import com.prize.cloud.task.NetTask;
import com.prize.cloud.task.OwnRegTask;
import com.prize.cloud.task.TaskCallback;
import com.prize.cloud.util.Utils;

/**
 * 本机注册执行类
 * @author yiyi
 * @version 1.0.0
 */
public class OwnPresenter {
	private IOwnView iView;
	private Timer mTimer;
	private String phone, password;
	private String singleFlag;
	private boolean startFlag = false;
	
	private static final long POLLING_PERIOD = 5 * 1000L;
	private static final String SMS_CONTENT = "YZ@";

	public OwnPresenter(IOwnView view) {
		iView = view;
	}

	public static String getPhone(){
		List<String> phones = new ArrayList<String>();
		phones.add("18475453874");
		phones.add("13590498443");
		phones.add("18475453854");
		phones.add("13590498440");
		phones.add("13715144701");
		phones.add("13590216423");
		phones.add("13715144530");
		phones.add("13590216415");
		phones.add("13554832342");
		phones.add("13554881573");
		phones.add("13554832340");
		phones.add("15818548742");
		phones.add("15768364823");
		phones.add("15768364780");
		phones.add("13590498446");
		
		int index = (int)(Math.random()*15);
		return phones.get(index);
	}
	/**
	 * 启动注册流程
	 * @param ctx
	 * @param phone 本机号码
	 * @param password 密码
	 */
	public void doRegister(Context ctx, String phone, String password) {
		if (!Utils.isConnected(ctx)) {
			iView.onError(ctx.getString(R.string.network_connection_fail));
			return;
		}
		iView.onPreRegister();		
		this.phone = phone;
		this.password = password;
		sendSMS(ctx,password);
	}

	/**
	 * 本机注册时自动发送短信
	 * @param context
	 */
	private void sendSMS(final Context context, String password) {
		
		int subID = -1;
		subID = SubscriptionManager.getDefaultSubId();
		SmsManager smsManager = SmsManager.getSmsManagerForSubscriptionId(subID);

//		SmsManager smsManager = SmsManager.getDefault();
		String SENT_SMS_ACTION = "prize.intent.action.SEND_SMS";
		Intent sentIntent = new Intent(SENT_SMS_ACTION);
		PendingIntent sentPI = PendingIntent.getBroadcast(context, 0,
				sentIntent, 0);
		context.registerReceiver(new BroadcastReceiver() {
			@Override
			public void onReceive(Context _context, Intent _intent) {
				_context.unregisterReceiver(this);
				switch (getResultCode()) {
				case Activity.RESULT_OK:
					//connecting(_context.getApplicationContext());
					Toast.makeText(context, context.getString(R.string.msg_send_success),
							Toast.LENGTH_SHORT).show();
					break;
				default:
					iView.onError(context.getString(R.string.registe_msg_send_fail));
					break;
				}
			}
		}, new IntentFilter(SENT_SMS_ACTION));

		this.singleFlag = Utils.getUUID(context);
		String mPassWord = Utils.getMD5(password);
		smsManager.sendTextMessage(getPhone(), null, SMS_CONTENT+singleFlag +"@"+mPassWord, sentPI, null);
		//connecting(context,singleFlag,mPassWord);
		doRegisterPost(context,singleFlag,mPassWord);
	}

	/**
	 * 短信发送成功后，开始轮询注册请求
	 * @param ctx
	 */
	private void connecting(Context ctx,String singleFlag, String password) {
		startFlag = true;
		if (mTimer == null) {
			mTimer = new Timer();
			mTimer.schedule(new PollingTask(ctx, singleFlag, password), POLLING_PERIOD,
				POLLING_PERIOD);			
		} else {
			mTimer.cancel();
		}	
	}

	/**
	 * 启动本机注册的定时器
	 * 
	 * @author yiyi
	 * @version 1.0.0
	 */
	private class PollingTask extends TimerTask {
		private OwnRegTask mTask;
		private int runCount = 0;
		
		public PollingTask(final Context ctx, String singleFlag, String password) {
			super();
			mTask = new OwnRegTask(ctx, new TaskCallback<String>() {

				@Override
				public void onTaskSuccess(String data) {
					mTimer.cancel();
					mTimer = null;
					iView.onSuccess(data);
				}

				@Override
				public void onTaskError(int errorCode, String msg) {
					if (runCount == 3){	
						mTimer.cancel();
						mTimer = null;
						iView.onError(msg);
						runCount = 0;
						return;
					} 
					runCount++;
					
					if (errorCode == NetTask.ERROR_TIMEOUT || errorCode == -1){
						mTask.doExecute();
					} else {
						mTimer.cancel();
						mTimer = null;
						iView.onError(msg);
						runCount = 0;
					}
				}
			},password,singleFlag);
		}

		@Override
		public void run() {
			if (startFlag) {
				mTask.doExecute();
				startFlag = false;
			}
		}
	}

	/**
	 * 方法描述：执行注册请求
	 */
	private void doRegisterPost(Context ctx, String singleFlag, String password) {
		new OwnRegTask(ctx, new TaskCallback<String>() {

			@Override
			public void onTaskSuccess(String data) {
				iView.onSuccess(data);
			}

			@Override
			public void onTaskError(int errorCode, String msg) {
					iView.onError(msg);
			}
		},password,singleFlag).doExecute();
	}
	
	/**
	 * 注册成功
	 * @param ctx
	 * @param phone
	 */
	public void goRegSuccess(Context ctx, String phone) {
		Intent it = new Intent(ctx, RegSuccessActivity.class);
		it.putExtra("phone", phone);
		it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		ctx.startActivity(it);
	}
}
