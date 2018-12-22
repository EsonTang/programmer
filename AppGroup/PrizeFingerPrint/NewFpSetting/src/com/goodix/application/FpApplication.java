package com.goodix.application;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.goodix.aidl.IFingerprintManager;
import com.goodix.service.FingerprintManager;
import com.goodix.service.FingerprintManagerService;
import com.goodix.util.L;

public class FpApplication extends Application {

	private FingerprintManager mFingerprintManager;
	private static FpApplication mApplication;
	private ServiceConnectCallback callback = null;
	
	@Override
	public void onCreate() {
		super.onCreate();
		android.util.Log.d("FpApplication", "FpApplication onCreate()");
		mApplication = this;
//		CrashHandler crashHandler = CrashHandler.getInstance();
//		crashHandler.init(this);
//		initFpMangerService();
	}

	private void initFpMangerService() {
		if (mFingerprintManager == null) {
			Intent intent = new Intent(this, FingerprintManagerService.class);
			android.util.Log.d("ServiceStartReceiver", "bindService");
			bindService(intent, new FpManagerServiceConnection(),Context.BIND_AUTO_CREATE);
		}
	}

	private class FpManagerServiceConnection implements ServiceConnection {
		@Override
		public void onServiceConnected(ComponentName arg0, IBinder binder) {
			IFingerprintManager mService = IFingerprintManager.Stub.asInterface(binder);
			android.util.Log.d("ServiceStartReceiver", "Service Connect");
			mFingerprintManager = new FingerprintManager(mService);
			if (null != callback) {
				callback.serviceConnect();
			}
			mFingerprintManager.initMode();
			L.d("Service Connect success!");
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {

		}
	}
	
	public boolean isFpServiceManagerEmpty() {
		if (mFingerprintManager == null) {
			return true;
		}else{
			return false;
		}
	}

	public FingerprintManager getFpServiceManager() {
		if (mFingerprintManager == null) {
			L.d("mFingerprintManager is null!!!!!");
			android.util.Log.d("ServiceStartReceiver", "init  getFpServiceManager");
			initFpMangerService();
		}
		return mFingerprintManager;
	}

	public synchronized static FpApplication getInstance() {
		if(null == mApplication){
			mApplication = new FpApplication();
		}
		return mApplication;
	}
	
	public interface ServiceConnectCallback {
		void  serviceConnect();
	}
	
	public void setCallback(ServiceConnectCallback mCallback){
		callback = mCallback;
	}
}
