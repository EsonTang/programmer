package com.prize.music.service;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.prize.music.IApolloService;
import com.prize.music.helpers.utils.MusicUtils;

public class ServiceBinder implements ServiceConnection {
	private final ServiceConnection mCallback;

	public ServiceBinder(ServiceConnection callback) {
		mCallback = callback;
	}

	@Override
	public void onServiceConnected(ComponentName className, IBinder service) {
		try {
			MusicUtils.mService = IApolloService.Stub.asInterface(service);
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
			MusicUtils.mService = null;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
}
