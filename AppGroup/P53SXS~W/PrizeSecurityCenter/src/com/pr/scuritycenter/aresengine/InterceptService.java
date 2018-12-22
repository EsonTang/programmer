package com.pr.scuritycenter.aresengine;

import java.util.ArrayList;
import tmsdk.bg.creator.ManagerCreatorB;
import tmsdk.bg.module.aresengine.AresEngineManager;
import tmsdk.bg.module.aresengine.DataFilter;
import tmsdk.bg.module.aresengine.DataHandler;
import tmsdk.bg.module.aresengine.DataInterceptor;
import tmsdk.bg.module.aresengine.DataInterceptorBuilder;
import tmsdk.bg.module.aresengine.IncomingCallFilter;
import tmsdk.bg.module.aresengine.IncomingSmsFilter;
import tmsdk.bg.module.aresengine.IntelliSmsChecker;
import tmsdk.common.module.aresengine.CallLogEntity;
import tmsdk.common.module.aresengine.FilterConfig;
import tmsdk.common.module.aresengine.IntelliSmsCheckResult;
import tmsdk.common.module.aresengine.SmsEntity;
import tmsdk.common.module.aresengine.TelephonyEntity;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.pr.scuritycenter.aresengine.InterceptActivity.RefreshDataUIBroadcastReceiver;
import com.pr.scuritycenter.utils.CalendarUtils;

/**
 * 
 * @author wangzhong
 *
 */
public class InterceptService extends Service {

//	private BlackNumberDao mBlackNumberDao;
//	private TelephonyManager mTelephonyManager;
//	private IncomingCallPhoneStateListener mIncomingCallPhoneStateListener;
	
	
	/**
	 * Incoming call intercept form data.
	 */
	private InterceptIncomingCallDao mInterceptIncomingCallDao;
	
	private AresEngineManager mAresEnginManager;
	
	private DataHandlerCallback mIncomingcall;
	private DataHandlerCallback mSyscalllog;
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		init();
	}

	private void init() {
		// Incoming call intercept form data.
		mInterceptIncomingCallDao = new InterceptIncomingCallDao(this);
		
		// AIDL.
//		mBlackNumberDao = new BlackNumberDao(this);
//		mTelephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
//		mIncomingCallPhoneStateListener = new IncomingCallPhoneStateListener(this, mBlackNumberDao);
//		mTelephonyManager.listen(mIncomingCallPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
		
		initTencent();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// AIDL.
//		mTelephonyManager.listen(mIncomingCallPhoneStateListener, PhoneStateListener.LISTEN_NONE);
//		mIncomingCallPhoneStateListener = null;
		
		unregisterCallback(DataInterceptorBuilder.TYPE_INCOMING_CALL, mIncomingcall);
		unregisterCallback(DataInterceptorBuilder.TYPE_SYSTEM_CALL, mSyscalllog);
	}

	public void initTencent() {
		// 设置配置因子
		mAresEnginManager = ManagerCreatorB.getManager(AresEngineManager.class);
		mAresEnginManager.setAresEngineFactor(new InterceptAresEngineFactor(getApplicationContext()));

		// //////////////////////////来去电拦截使用示例//////////////////////////////////////////////
		DataInterceptorBuilder<?> builder = null;
		// 每一种类型的拦截器只能调用addIntercepter方法添加一次，之后添加的不会生效并且会导致程序异常。
		// 实际使用过程中推荐在Application启动时添加拦截器， 这里捕获异常只是为了方便demo的可测试性
		try {
			// 添加来电拦截器
			builder = DataInterceptorBuilder.createInComingCallInterceptorBuilder();
			mAresEnginManager.addInterceptor(builder);

			// 添加通话记录拦截器
			builder = DataInterceptorBuilder.createSystemCallLogInterceptorBuilder();
			mAresEnginManager.addInterceptor(builder);

			// 配置拦截模式，如只“白名单和系统联系人通过”
			DataInterceptor<?> intercepter = mAresEnginManager.findInterceptor(DataInterceptorBuilder.TYPE_INCOMING_CALL);
			DataFilter<?> filter = intercepter.dataFilter();
			FilterConfig config = new FilterConfig();
//			config.set(IncomingCallFilter.PRIVATE_CALL, FilterConfig.STATE_DISABLE);
//			config.set(IncomingCallFilter.BLACK_LIST, FilterConfig.STATE_DISABLE);
//			config.set(IncomingCallFilter.WHITE_LIST, FilterConfig.STATE_ACCEPTABLE);
//			config.set(IncomingCallFilter.SYS_CONTACT, FilterConfig.STATE_ACCEPTABLE);
			// ...
//			config.set(IncomingCallFilter.BLACK_LIST, FilterConfig.STATE_ENABLE);
			config.set(IncomingCallFilter.PRIVATE_CALL, FilterConfig.STATE_DISABLE);
			config.set(IncomingCallFilter.WHITE_LIST, FilterConfig.STATE_DISABLE);
			config.set(IncomingCallFilter.BLACK_LIST, FilterConfig.STATE_REJECTABLE);
			config.set(IncomingCallFilter.SYS_CONTACT, FilterConfig.STATE_ACCEPTABLE);
			filter.setConfig(config);

		} catch (RuntimeException e) {
			e.printStackTrace();
		}
		
		// 回调来电功能对象
		mIncomingcall = new DataHandlerCallback("来电");
		registerCallback(DataInterceptorBuilder.TYPE_INCOMING_CALL, mIncomingcall);
		
		// 回调通话记录功能对象
//		mSyscalllog = new DataHandlerCallback("通话记录");
//		registerCallback(DataInterceptorBuilder.TYPE_SYSTEM_CALL, mSyscalllog);

		/**
		 * 下面是短信智能拦截使用示例
		 */
		SmsEntity sms = new SmsEntity();
		sms.name = "姓名";
		sms.phonenum = "123";
		sms.body = "短信测试";

		// 智能拦截调用接口
		IntelliSmsChecker smschecker = mAresEnginManager.getIntelligentSmsChecker();
		IntelliSmsCheckResult checkresult = smschecker.check(sms);
		Log.i("demo", "智能拦截结果: " + checkresult.contentType() + " , " + checkresult.suggestion);
		
		SmsCheckTest.testSms(mAresEnginManager);
	}

	// 添加回调需要使用的对象
	private void registerCallback(String type, DataHandler.DataHandlerCallback callback) {
		mAresEnginManager.findInterceptor(type).dataHandler().addCallback(callback);
	}
	
	// 注销被回调的功能对象
	private void unregisterCallback(String type, DataHandler.DataHandlerCallback callback) {
		mAresEnginManager.findInterceptor(type).dataHandler().removeCallback(callback);
	}

	private ArrayList<String> mLines = new ArrayList<String>();
	
	private class DataHandlerCallback implements DataHandler.DataHandlerCallback {

		private String mTag;

		public DataHandlerCallback(String tag) {
			mTag = tag;
		}

		@Override
		public void onCallback(TelephonyEntity data, int filter, int state, Object... datas) {
			synchronized (mLines) {
				if (mLines.size() >= 20) {
					mLines.remove(0);
				}
				
				if (data instanceof CallLogEntity) {
					Log.v("JOHN", "data.phonenum : " + data.phonenum);
					String phonenum = data.phonenum;
					String time = CalendarUtils.getCurrentTime();
					final InterceptIncomingCallBean interceptIncomingCallBean = new InterceptIncomingCallBean();
					interceptIncomingCallBean.setNumber(phonenum);
					interceptIncomingCallBean.setTime(time);
					mInterceptIncomingCallDao.add("", phonenum, time);
					
					myHandler.sendEmptyMessage(MSG_NOTIFYDATASETCHANGED);
				} else {
					String body = (data instanceof SmsEntity) ? (data.phonenum + " " + ((SmsEntity) data).body) : data.phonenum;
					String line = String.format("%s: %s %s", mTag, body, msgFilterState(data instanceof SmsEntity, filter, state, datas));
					mLines.add(line);

					Log.i("demo", line);
					final StringBuffer content = new StringBuffer();
					for (String l : mLines) {
						content.append(l).append("\n");
					}

					// Do sms thing.
					/*mContextShower.post(new Runnable() {

						@Override
						public void run() {
							mContextShower.setText(content.toString());
						}
					});*/
				}
			}
		}
	}

	private static String msgFilterState(boolean isSms, int filter, int state, Object[] args) {
		// 判断是否为短信拦截
		if (isSms) {
			final String[] FILTER_NAME = { "private", "white list", "black list", "system contacts", "recent call log", "keywords", "intelli engine", "stranger sms" };
			int indexOfFilterName = Integer.numberOfTrailingZeros(filter);
			
			//判断拦截内容
			switch (filter) {
			case IncomingSmsFilter.REMOVE_PRIVATE_SMS:
				//判断拦截器是否启动
				if (state == FilterConfig.STATE_ENABLE)
					return "MOVED to private";
				break;

			case IncomingSmsFilter.WHITE_LIST:
			case IncomingSmsFilter.BLACK_LIST:
			case IncomingSmsFilter.SYS_CONTACT:
			case IncomingSmsFilter.LAST_CALLS:
			case IncomingSmsFilter.KEY_WORKDS:
			case IncomingSmsFilter.STRANGER_SMS:
				
				//判断权限是否为可通过
				if (state == FilterConfig.STATE_ACCEPTABLE)
					return "PASSED by " + FILTER_NAME[indexOfFilterName];
				else if (state == FilterConfig.STATE_REJECTABLE)
					return "BLOCKED by " + FILTER_NAME[indexOfFilterName];
				break;

			case IncomingSmsFilter.INTELLIGENT_CHECKING:
				//判断拦截器是否启动
				if (state == FilterConfig.STATE_ENABLE) {
					IntelliSmsCheckResult checkResult = (IntelliSmsCheckResult) (args[0]);
					
					// 判断处理建议，suggestion 取值范围｛-1，1，2，3，4｝
					switch (checkResult.suggestion) {
					case IntelliSmsCheckResult.SUGGESTION_PASS:
						return "PASSED by " + FILTER_NAME[indexOfFilterName];

					case IntelliSmsCheckResult.SUGGESTION_INTERCEPT:
					case IntelliSmsCheckResult.SUGGESTION_DOUBT:
						return "BLOCKED by " + FILTER_NAME[indexOfFilterName];

					default:
						return "WTF? by " + FILTER_NAME[indexOfFilterName];
					}
				}
				break;
			}
		} else {
			final String[] FILTER_NAME = { "private", "white list", "black list", "system contacts", "last call", "stranger call" };
			int indexOfFilterName = Integer.numberOfTrailingZeros(filter);
			
			//判断是否有可通过权限
			if (state == FilterConfig.STATE_ACCEPTABLE)
				return "PASSED by " + FILTER_NAME[indexOfFilterName];
			else if (state == FilterConfig.STATE_REJECTABLE)
				return "BLOCKED by " + FILTER_NAME[indexOfFilterName];
		}
		return "PASSED filter=" + filter + "state=" + state;
	}

	private static final int MSG_NOTIFYDATASETCHANGED = 11;

	@SuppressLint("HandlerLeak")
	private Handler myHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_NOTIFYDATASETCHANGED:
				// SEND THE RECIVER...
				Intent iRefreshDataUI = new Intent(RefreshDataUIBroadcastReceiver.ACTION_INTERCEPT_REFRESH);
				sendBroadcast(iRefreshDataUI);
				break;
			}
		}
		
	};

}
