package com.prize.appcenter.service;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.prize.app.download.IDownLoadService;
import com.prize.appcenter.ui.util.AIDLUtils;

public class ServiceBinder implements ServiceConnection {
	private final ServiceConnection mCallback;

	public ServiceBinder(ServiceConnection callback) {
		mCallback = callback;
	}

	@Override
	public void onServiceConnected(ComponentName className, IBinder service) {
		try {
			AIDLUtils.mService = IDownLoadService.Stub.asInterface(service);

			if (mCallback != null)
				mCallback.onServiceConnected(className, service);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	public void onServiceDisconnected(ComponentName className) {
		try {
			if (mCallback != null)
				mCallback.onServiceDisconnected(className);
			AIDLUtils.mService = null;
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
