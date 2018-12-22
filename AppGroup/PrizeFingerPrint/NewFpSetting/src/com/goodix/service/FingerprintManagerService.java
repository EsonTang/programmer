package com.goodix.service;

import java.util.Vector;

import android.app.Instrumentation;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.RemoteException;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;

import com.goodix.aidl.IKeyguardService;
import com.goodix.aidl.IEnrollCallback;
import com.goodix.aidl.IFingerprintManager;
import com.goodix.aidl.IVerifyCallback;
import com.goodix.device.CommandType;
import com.goodix.device.FpDevice;
import com.goodix.device.MessageType;
import com.goodix.model.FpFunctionItem;
import com.goodix.util.FpFunctionSPUtil;
import com.goodix.util.L;
import com.goodix.aidl.IUpdateBaseCallback;

public class FingerprintManagerService extends Service {

	private static final String TAG = "FingerprintManagerService";

	private FpDevice device = FpDevice.open();

	private IBinder mPreToken;
	
    private IUpdateBaseCallback mUpdateBaseCb = null;
	private class Client {
		public static final int TYPE_ENROLL = 1;
		public static final int TYPE_VERIFY = 2;
		public IBinder token;
		public int type;
		public Object callback;

		public Client(IBinder token, int type, Object callback) {
			this.token = token;
			this.type = type;
			this.callback = callback;
		}
	}

	private enum ManagerStatus {
		MANAGER_INIT, 
		MANAGER_IDLE, 
		MANAGER_ENROLL, 
		MANAGER_VERIFY,
	}

	private ManagerStatus mManagerStatus = ManagerStatus.MANAGER_IDLE;

	private enum EventStatus {
		EVENT_IDLE, 
		EVENT_TOUCH, 
		EVENT_UNTOUCH_NO_RESULT, 
		EVENT_RESULT_NO_UNTOUCH,
		EVENT_COMPLETE,
	}

	private EventStatus mEventStatus = EventStatus.EVENT_IDLE;

	private Vector<Client> mClientList = new Vector<Client>();

	private Handler mDispathMessageHandler;

	private PowerManager mPmGer ;

	private IKeyguardService mKeyguardService;

	private TelephonyManager mTelephonyManager;

	private InCallPhoneStateListener mListener;

	private boolean mIsRinging = false;

	private Context mContext;

	@Override
	public IBinder onBind(Intent arg0) {
		return stub;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(TAG, "onCreate()");
		mContext = getApplicationContext();
		HandlerThread mDispatchMessageThread = new HandlerThread("dispatch");
		mDispatchMessageThread.start();
		mDispathMessageHandler = new DispatchMessageHandler(mDispatchMessageThread.getLooper());
		device.setDispathcMessageHandler(mDispathMessageHandler);
		registerScreenActionReceiver();

		mPmGer = (PowerManager) getSystemService(Context.POWER_SERVICE);

		if (mListener == null) {
			mListener = new InCallPhoneStateListener();
		}
		mTelephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
		mTelephonyManager.listen(mListener,PhoneStateListener.LISTEN_CALL_STATE);

		Intent intent = new Intent();
		intent.setClassName("com.android.systemui", "com.android.systemui.keyguard.KeyguardService");
		bindService(intent, mKeyguardConnection,Context.BIND_AUTO_CREATE);
	}

	@Override
	public void onDestroy() {
		unregisterReceiver(receiver);
		Log.d(TAG, "onDestroy()");
		super.onDestroy();
	}

	private void registerScreenActionReceiver() {
		final IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_SCREEN_OFF);
		filter.addAction(Intent.ACTION_SCREEN_ON);
		filter.addAction(Intent.ACTION_BOOT_COMPLETED);
		filter.setPriority(1000);
		registerReceiver(receiver, filter);
	}

    public void setUpdateBaseCallbackInternal(IUpdateBaseCallback callback) {
        mUpdateBaseCb = callback;
    }

	private final BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(final Context context, final Intent intent) {
			String strAction = intent.getAction();
            boolean bScreenOn = mPmGer.isScreenOn();
			L.d("FingerprintManagerService .onReceive .Intent.strAction == "+ intent.getAction());
			if ((strAction.equals(Intent.ACTION_SCREEN_ON) && bScreenOn)
					|| (strAction.equals(Intent.ACTION_SCREEN_OFF) && !bScreenOn)
					|| strAction.equals(Intent.ACTION_BOOT_COMPLETED)) {
				UpdateStatus();
			}
		}
	};
	
    public void updateBaseInternal() {
        Log.d(TAG, "send update command to device");
        device.cancelRecognize();
        device.SendCmd(CommandType.GOODIX_ENGTEST_CMD_UPDATE_BASE, null, null);
    }	

	public static void sendKeyCode(final int keyCode){  
		new Thread () {  
			public void run() {  
				try {  
					Instrumentation inst = new Instrumentation();  
					inst.sendKeyDownUpSync(keyCode);  
				} catch (Exception e) {  
					Log.e("Exception when sendPointerSync", e.toString());  
				}  
			}  
		}.start();  
	}

	public void UpdateStatus() {
		boolean bScreenOn = mPmGer.isScreenOn();
		Log.v(TAG, "FingerprintManagerService : UpdateStatus");
		L.d("FingerprintManagerService : UpdateStatus.mManagerStatus == "+ mManagerStatus);
		ManagerStatus nextStatus = ManagerStatus.MANAGER_VERIFY;
		IBinder nextToken = null;
		if (!mClientList.isEmpty()) {
			Client client = mClientList.lastElement();
			if (client.type == Client.TYPE_ENROLL) {
				Log.v(TAG, "nextStatus = ManagerStatus.MANAGER_ENROLL");
				L.d("nextStatus = ManagerStatus.MANAGER_ENROLL");
				nextStatus = ManagerStatus.MANAGER_ENROLL;
			} else if (client.type == Client.TYPE_VERIFY) {
				Log.v(TAG, "nextStatus = ManagerStatus.MANAGER_VERIFY");
				L.d("nextStatus = ManagerStatus.MANAGER_VERIFY");
				nextStatus = ManagerStatus.MANAGER_VERIFY;
			}
			nextToken = client.token;
		}

		/*
		if (nextStatus == mManagerStatus && nextToken == mPreToken) {
			if (bScreenOn == false && mManagerStatus == ManagerStatus.MANAGER_VERIFY) {
				L.d("UpdateStatus set FF mode");
				mManagerStatus = ManagerStatus.MANAGER_IDLE;
				device.cancelRecognize();
				device.setMode(CommandType.GOODIX_ENGTEST_CMD_SET_MODE_FF);
				mEventStatus = EventStatus.EVENT_IDLE;
			}
			return;
		}
		 */

		switch (mManagerStatus) {
		case MANAGER_IDLE:
			break;
		case MANAGER_ENROLL:
			Log.v(TAG, "device.cancelRegister()");
			mManagerStatus = ManagerStatus.MANAGER_IDLE;
			device.cancelRegister();
            if (nextStatus != ManagerStatus.MANAGER_ENROLL&& nextStatus != ManagerStatus.MANAGER_VERIFY) {
                L.d("set key mode");
                device.setMode(CommandType.GOODIX_ENGTEST_CMD_SET_MODE_KEY);
            }
			break;
		case MANAGER_VERIFY:
			Log.v(TAG, "device.cancelRecognize();");
			L.d("device.cancelRecognize()");
			mManagerStatus = ManagerStatus.MANAGER_IDLE;
			device.cancelRecognize();
			if (nextStatus != ManagerStatus.MANAGER_ENROLL&& nextStatus != ManagerStatus.MANAGER_VERIFY) {
				L.d("set key mode");
				device.setMode(CommandType.GOODIX_ENGTEST_CMD_SET_MODE_KEY);
			}
			break;
		default:
			break;
		}

		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		switch (nextStatus) {
		case MANAGER_IDLE:
			break;
		case MANAGER_ENROLL:
			Log.v(TAG, "device.register();");
			L.d( "device.register();");
            device.setMode(CommandType.GOODIX_ENGTEST_CMD_SET_MODE_IMG);
			device.getPermission("1234");
			device.register();
			break;

		case MANAGER_VERIFY:
			Log.v(TAG, "device.recognize();");
			//			if (bScreenOn == true) {
			L.d("set imag mode");
			device.setMode(CommandType.GOODIX_ENGTEST_CMD_SET_MODE_IMG);
			device.recognize();
			/*
			} else {

				L.d("set FF mode");
				mManagerStatus = ManagerStatus.MANAGER_IDLE;
				mPreToken = nextToken;
				device.setMode(CommandType.GOODIX_ENGTEST_CMD_SET_MODE_FF);

				return;
			}
			 */
			break;

		default:
			break;
		}
		mManagerStatus = nextStatus;
		mPreToken = nextToken;
		mEventStatus = EventStatus.EVENT_IDLE;
		L.d("FingerprintManagerService : UpdateStatus.end");
	}

	private boolean isTokenExist(IBinder token) {
		if (!mClientList.isEmpty()) {
			for (Client client : mClientList) {
				if (client.token == token) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean removeClient(IBinder token) {
		if (!mClientList.isEmpty()) {
			for (Client client : mClientList) {
				if (client.token == token) {
					mClientList.remove(client);
					return true;
				}
			}
		}
		return false;
	}

	private IFingerprintManager.Stub stub = new IFingerprintManager.Stub() {
		@Override
		public int verify(IBinder token, IVerifyCallback callback)
				throws RemoteException {
			Log.v(TAG, "FingerprintManagerService : verify");
			/**
			 * 1. check token , the token has existed in list. 2. add to list.
			 * 3. update status.
			 */
			if (!isTokenExist(token)) {
				Log.v(TAG, "FingerprintManagerService : add client to list");
				mClientList.add(new Client(token, Client.TYPE_VERIFY, callback));
				UpdateStatus();
			}
			return 0;
		}

		@Override
		public int cancelVerify(IBinder token) throws RemoteException {
			/*
			 * 1. check the client is last one or not. 2. delete client. 3.
			 * update status if the token is last one.
			 */
			Log.v(TAG, "FingerprintManagerService : cancelVerify");
			if (mClientList.lastElement().token == token) {
				mClientList.removeElement(mClientList.lastElement());
				UpdateStatus();
			} else {
				L.d("cancelVerify : removeClient");
				removeClient(token);
			}

			return 0;
		}

		@Override
		public int enroll(IBinder token, IEnrollCallback callback)
				throws RemoteException {
			/**
			 * 1. check token , the token has existed in list. 2. add to list.
			 * 3. update status.
			 */
			Log.v(TAG, "FingerprintManagerService : enEnroll");
			if (!isTokenExist(token)) {
				mClientList.add(new Client(token, Client.TYPE_ENROLL, callback));
				UpdateStatus();
			}
			return 0;
		}

		@Override
		public int resetEnroll(IBinder token) throws RemoteException {
			/*
			 * 1. check the client is last one or not. 2. delete client. 3.
			 * update status if the token is last one.
			 */

			Log.v(TAG, "FingerprintManagerService : resetEnroll");
			if (mClientList.size() > 0) {
				if (mClientList.lastElement().token == token) {
					/* reset device enroll */
					device.resetRegister();
				}
			}
			return 0;
		}

		@Override
		public int cancelEnroll(IBinder token) throws RemoteException {
			/*
			 * 1. check the client is last one or not. 2. delete client. 3.
			 * update status if the token is last one.
			 */

			Log.v(TAG, "FingerprintManagerService : cancelEnroll");
			if (mClientList.size() > 0) {
				if (mClientList.lastElement().token == token) {
					mClientList.removeElement(mClientList.lastElement());
					UpdateStatus();
				} else {
					removeClient(token);
				}
			}
			return 0;
		}

		@Override
		public int saveEnroll(IBinder token, int index) throws RemoteException {
			/*
			 * 1. Check is the last one token. 2. Check is complete register. 3.
			 * save register.
			 */
			Log.v(TAG, "FingerprintManagerService : saveEnroll");
			if (mClientList.size() > 0) {
				Client client = mClientList.lastElement();
				if (client.token == token) {
					device.saveRegister(index);
					UpdateStatus();
				}
			}
			return 0;
		}

		@Override
		public int query() throws RemoteException {
			Log.v(TAG, "FingerprintManagerService : query template.");
			return device.query();
		}

		public int delete(int i) throws RemoteException {
			Log.v(TAG, "FingerprintManagerService : delete template.");
			if (mManagerStatus == ManagerStatus.MANAGER_VERIFY) {
				mManagerStatus = ManagerStatus.MANAGER_IDLE;
				device.cancelRecognize();
			}
			if (mManagerStatus != ManagerStatus.MANAGER_ENROLL) {
				device.cancelRegister();
			}

			device.getPermission("1234");
			int result = device.delete(i);
			UpdateStatus();
			return result;
		}

		@Override
		public String getInfo() throws RemoteException {
			return device.getInfo();
		}

        @Override
        public void initMode() throws RemoteException {
            //updateBaseInternal();
            device.setMode(CommandType.GOODIX_ENGTEST_CMD_SET_MODE_KEY);
        }

        @Override
        public void setUpdateBaseCallback(IUpdateBaseCallback callback) {
            setUpdateBaseCallbackInternal(callback);
        }
	};


	public void sendMessageToClient(Message msg) {
		Log.d(TAG, "sendMessageToClient(Message msg)");
		if (mClientList.isEmpty()) {
			return;
		}
		for(int i =0; i < mClientList.size(); i++) {
			Log.d("maggie", "mClientList[" + i + "]=" + mClientList.get(i) + ", type = " + (mClientList.get(i).type == 1?"TYPE_ENROLL":"TYPE_VERIFY"));
		}
		Client client = mClientList.lastElement();
		try {
			if (client.type == Client.TYPE_VERIFY) {
				((IVerifyCallback) mClientList.lastElement().callback).handleMessage(msg.what, msg.arg1, msg.arg2,(byte[]) msg.obj);
//				unlock(msg.what, msg.arg1, msg.arg2);
			} else if (client.type == Client.TYPE_ENROLL) {
				((IEnrollCallback) mClientList.lastElement().callback).handleMessage(msg.what, msg.arg1, msg.arg2,(byte[]) msg.obj);
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	private final ServiceConnection mKeyguardConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mKeyguardService = IKeyguardService.Stub.asInterface(service);
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mKeyguardService = null;
		}
	};

	private void onIdentifyError(){
		PowerManager.WakeLock timeoutWakeLock = mPmGer.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, TAG);
		timeoutWakeLock.acquire(5000);
		Intent intent = new Intent();
		intent.setAction("com.prize.broadcast.unlock_failed");
		sendBroadcast(intent);
	}

	private void unlock(int msg, int arg1, int arg2){
		if (msg == MessageType.MSG_TYPE_RECONGNIZE_SUCCESS) {
			if (arg2 > 0) {
				boolean isScreenOn = mPmGer.isScreenOn();
				//				if(isScreenOn) {
				try {
					Intent intent = new Intent();
					intent.setAction("com.prize.broadcast.unlock_success");
					sendBroadcast(intent);
				} catch (Exception e) {
					Log.d(TAG," unLock RemoteException when keyguardDone");
				}
				//				}

			} else {
				onIdentifyError();	
			}
		} else if (msg == MessageType.MSG_TYPE_RECONGNIZE_FAILED) {
			onIdentifyError();
		} else if (msg == MessageType.MSG_TYPE_RECONGNIZE_NO_REGISTER_DATA) {
			onIdentifyError();
		}
	}

	private class DispatchMessageHandler extends Handler {
		public DispatchMessageHandler(Looper looper) {
			super(looper);
		}

		public void handleVerifyMessage(Message msg) {
			Log.v(TAG, "FingerprintManagerService : handleVerifyMessage");
			L.d("FingerprintManagerService : handleVerifyMessage MessageType = " + MessageType.getString(msg.what));
			if (mManagerStatus != ManagerStatus.MANAGER_VERIFY) {
				return;
			}
			switch (msg.what) {
            case 9:
                sendMessageToClient(msg);
                break;
			case MessageType.MSG_TYPE_COMMON_TOUCH:
				if (mEventStatus == EventStatus.EVENT_IDLE) {
					mEventStatus = EventStatus.EVENT_TOUCH;

					sendMessageToClient(msg);
				}
				break;
			case MessageType.MSG_TYPE_COMMON_UNTOUCH: {
				if (mEventStatus == EventStatus.EVENT_TOUCH
						|| mEventStatus == EventStatus.EVENT_IDLE) {
					mEventStatus = EventStatus.EVENT_UNTOUCH_NO_RESULT;
				} else if (mEventStatus == EventStatus.EVENT_RESULT_NO_UNTOUCH) {
					mEventStatus = EventStatus.EVENT_COMPLETE;
				}
				sendMessageToClient(msg);
			}
			break;

			case MessageType.MSG_TYPE_COMMON_NOTIFY_INFO: {
				sendMessageToClient(msg);
			}
			break;

			case MessageType.MSG_TYPE_RECONGNIZE_SUCCESS:
			case MessageType.MSG_TYPE_RECONGNIZE_TIMEOUT:
			case MessageType.MSG_TYPE_RECONGNIZE_FAILED:
			case MessageType.MSG_TYPE_RECONGNIZE_BAD_IMAGE:
			case MessageType.MSG_TYPE_RECONGNIZE_GET_DATA_FAILED:
			case MessageType.MSG_TYPE_RECONGNIZE_NO_REGISTER_DATA: {
				if (mEventStatus == EventStatus.EVENT_TOUCH
						|| mEventStatus == EventStatus.EVENT_IDLE) {
					mEventStatus = EventStatus.EVENT_RESULT_NO_UNTOUCH;
				} else if (mEventStatus == EventStatus.EVENT_UNTOUCH_NO_RESULT) {
					mEventStatus = EventStatus.EVENT_COMPLETE;
				}
				sendMessageToClient(msg);
				break;
			}
			default:
				break;
			}
			if (mEventStatus == EventStatus.EVENT_COMPLETE) {
				Log.v(TAG, "EVENT_COMPLETE : device.recognize()");
				mEventStatus = EventStatus.EVENT_IDLE;
				L.d("EVENT_COMPLETE : device.recognize()");
				device.recognize();

			}
		}

		private int percent = 0;

		public void handleEnrollMessage(Message msg) {
			Log.v(TAG, "FingerprintManagerService : handleEnrollMessage");
			Log.v(TAG, "MessageType = " + MessageType.getString(msg.what));
			L.d("FingerprintManagerService : handleEnrollMessage MessageType = " + MessageType.getString(msg.what));

			if (mManagerStatus != ManagerStatus.MANAGER_ENROLL) {
				return;
			}
			switch (msg.what) {
			case MessageType.MSG_TYPE_COMMON_TOUCH:
				if (mEventStatus == EventStatus.EVENT_IDLE) {
					mEventStatus = EventStatus.EVENT_TOUCH;
					sendMessageToClient(msg);

				}
				break;
			case MessageType.MSG_TYPE_COMMON_UNTOUCH: {
				if (mEventStatus == EventStatus.EVENT_TOUCH
						|| mEventStatus == EventStatus.EVENT_IDLE) {
					mEventStatus = EventStatus.EVENT_UNTOUCH_NO_RESULT;
				} else if (mEventStatus == EventStatus.EVENT_RESULT_NO_UNTOUCH) {
					mEventStatus = EventStatus.EVENT_COMPLETE;
				}
				sendMessageToClient(msg);
			}
			break;

			case MessageType.MSG_TYPE_COMMON_NOTIFY_INFO: 
			case MessageType.MSG_TYPE_REGISTER_DUPLICATE_REG:
				sendMessageToClient(msg);

				break;

			case MessageType.MSG_TYPE_REGISTER_PIECE:
			case MessageType.MSG_TYPE_REGISTER_NO_PIECE:
			case MessageType.MSG_TYPE_REGISTER_NO_EXTRAINFO:
			case MessageType.MSG_TYPE_REGISTER_LOW_COVER:
			case MessageType.MSG_TYPE_REGISTER_BAD_IMAGE:
			case MessageType.MSG_TYPE_REGISTER_GET_DATA_FAILED:
			case MessageType.MSG_TYPE_ERROR: {
				if (mEventStatus == EventStatus.EVENT_TOUCH
						|| mEventStatus == EventStatus.EVENT_IDLE) {
					mEventStatus = EventStatus.EVENT_RESULT_NO_UNTOUCH;
					L.d("mEventStatus = EventStatus.EVENT_RESULT_NO_UNTOUCH");

				} else if (mEventStatus == EventStatus.EVENT_UNTOUCH_NO_RESULT) {
					mEventStatus = EventStatus.EVENT_COMPLETE;
				}
				percent = msg.arg1;
				sendMessageToClient(msg);
			}
			break;
            case 9:
                sendMessageToClient(msg);
                break;
			default:
				break;
			}
			if (mEventStatus == EventStatus.EVENT_COMPLETE && percent < 100) {
				Log.v(TAG, "EVENT_COMPLETE : device.register()");
				L.d("EVENT_COMPLETE : device.register()");

				mEventStatus = EventStatus.EVENT_IDLE;

				device.register();
				percent = 0;
			}
		}

		public void handleMessage(Message msg) {
			Log.d(TAG, "handleMessage(Message msg)");
			Log.d(TAG, "msg.what = " + msg.what + ", msg:" + MessageType.getString(msg.what));
			if (msg.what == MessageType.MSG_TYPE_COMMON_TOUCH){
				FpFunctionSPUtil mSharePUtil = new FpFunctionSPUtil(mContext);
				boolean isFpPhotoUsefully = mSharePUtil.getFunctionStatus(FpFunctionItem.CAMERA);
				Log.d(TAG, "isFpPhotoUsefully = " + isFpPhotoUsefully);
				boolean isFpAnswerUsefully = mSharePUtil.getFunctionStatus(FpFunctionItem.ANSWER);
				Log.d(TAG, "isFpAnswerUsefully = " + isFpAnswerUsefully);
				if(isFpPhotoUsefully){
					sendKeyCode(KeyEvent.KEYCODE_CAMERA);
				}
				if(mIsRinging && isFpAnswerUsefully){
					sendKeyCode(KeyEvent.KEYCODE_CALL);
				}
			}
			Log.v(TAG, "FingerprintManagerService : handleMessage mManagerStatus == " + mManagerStatus);
			if (mManagerStatus == ManagerStatus.MANAGER_VERIFY) {
				handleVerifyMessage(msg);
			} else if (mManagerStatus == ManagerStatus.MANAGER_ENROLL) {
				handleEnrollMessage(msg);
			}
			if (msg.what == MessageType.MSG_TYPE_UPDATE_BASE_FINISHED) {
                //Log.d(TAG, "now send 9 msg to client");
                //sendMessageToClient(msg);
                try {
                    if (mUpdateBaseCb != null) {
                        mUpdateBaseCb.updated();
                    }
                } catch (RemoteException e) {
                    Log.e(TAG, "cannot call updatebase callback", e);
                }
            }
		}
	}

	private class InCallPhoneStateListener extends PhoneStateListener {
		@Override
		public void onCallStateChanged(int state, String incomingNumber) {
			if (state == TelephonyManager.CALL_STATE_RINGING) {
				mIsRinging = true;
			}else if(state == TelephonyManager.CALL_STATE_IDLE ||
					state == TelephonyManager.CALL_STATE_OFFHOOK){
				mIsRinging = false;
			}
		};
	}
}
