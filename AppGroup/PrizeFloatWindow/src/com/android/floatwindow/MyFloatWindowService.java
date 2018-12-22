package com.android.floatwindow;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;

public class MyFloatWindowService extends Service{
	
	//static final int MSG_GET_isWindowShowing = 1;
	static final int MSG_GET_startFloatWindowService = 2;
	static final int MSG_GET_stopFloatWindowService = 3;

	class ServiceHandler extends Handler{
		
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			/*case MSG_GET_isWindowShowing:
				boolean isWindowShow = FloatWindowController.isWindowShowing();
				break;*/
			case MSG_GET_startFloatWindowService:
				FloatWindowController.startFloatWindowService(MyFloatWindowService.this);
				Log.i("test", "startFloatWindowService");
				break;
			case MSG_GET_stopFloatWindowService:
				FloatWindowController.stopFloatWindowService(MyFloatWindowService.this);
				Log.i("test", "stopFloatWindowService");
				break;
			default:
				super.handleMessage(msg);
			}
		}
	}
	
	final Messenger mMessenger = new Messenger(new ServiceHandler());
	
	@Override
	public IBinder onBind(Intent intent) {
		Log.i("test", "onBind return !" + mMessenger.getBinder() + "");
		return mMessenger.getBinder();
	}
	
}
