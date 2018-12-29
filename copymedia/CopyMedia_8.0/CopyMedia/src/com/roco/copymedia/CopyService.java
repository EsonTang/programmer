package com.roco.copymedia;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.os.IBinder;

public class CopyService extends Service {	
	private CopyReceiver mCopyReceiver = new CopyReceiver();
	
	public static void debug(String msg){
		android.util.Log.d("xxczy","roco=>"+msg);
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
	
		debug("CopyService onCreate");
		mCopyReceiver.doCopyStuff(this);
	}
	
	
	@Override
	public void onStart(Intent intent, int startId) {
		// TODO Auto-generated method stub
		super.onStart(intent, startId);
	}
	
	@Override
	public void onDestroy() {
		debug("CopyService onDestroy");
		super.onDestroy();
	}

}
