package com.android.prizefloatwindow.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Log;

import com.android.prizefloatwindow.ArcTipViewController;
import com.android.prizefloatwindow.application.PrizeFloatApp;

public class ScreenShotUtils {
	private static  ScreenshotRunnable mScreenshotRunnable;
	private static ScreenShotUtils instance;
	final  Object mScreenshotLock = new Object();
	ServiceConnection mScreenshotConnection = null;
	private static final String SYSUI_PACKAGE = "com.android.prizefloatwindow";
    private static final String SYSUI_SCREENSHOT_SERVICE ="com.android.prizefloatwindow.screenshot.TakeScreenshotService";
    private static final String SYSUI_SCREENSHOT_ERROR_RECEIVER ="com.android.prizefloatwindow.screenshot.ScreenshotServiceErrorReceiver";
	
    public static ScreenShotUtils getInstance() {  
    	if(instance == null){
    		instance = new ScreenShotUtils(PrizeFloatApp.getInstance().getApplicationContext());
    	}
        return instance;
    }
    private ScreenShotUtils(Context application) {
    	if(mScreenshotRunnable == null){
			mScreenshotRunnable = new ScreenshotRunnable();
		}
    }
    
	private  class ScreenshotRunnable implements Runnable {

        @Override
        public void run() {
            takeScreenshot(PrizeFloatApp.getContext());
        }
    }
 
	public  void takeScreen(){
		if(mScreenshotRunnable == null){
			mScreenshotRunnable = new ScreenshotRunnable();
		}
		PrizeFloatApp.getInstance().getHandler().post(mScreenshotRunnable);
	}
	
	
	
	private  void takeScreenshot(final Context mContext) {
		Log.d("snail_screenshot", "----------------takeScreenshot--------start------");
        synchronized (mScreenshotLock) {
        	Log.d("snail_screenshot", "----------------takeScreenshot--------start--mScreenshotConnection != null----boolean=="+String.valueOf(mScreenshotConnection != null));
            if (mScreenshotConnection != null) {
                return;
            }
            int mNewOriginalPrizeFloatWindow = Settings.System.getInt(mContext.getContentResolver(),Settings.System.PRIZE_NEW_FLOAT_WINDOW,0);
            Log.d("snail", "-------PhoneWindowManager.java---hideFloatForScreenshot-----mNewOriginalPrizeFloatWindow=="+mNewOriginalPrizeFloatWindow);
            if(1 == mNewOriginalPrizeFloatWindow){
                Intent iNewFloatWindow = new Intent("android.intent.action.PRIZEHIDETEMP");
                iNewFloatWindow.setPackage("com.android.prizefloatwindow");
                mContext.sendBroadcast(iNewFloatWindow);
            }
            final ComponentName serviceComponent = new ComponentName(SYSUI_PACKAGE,SYSUI_SCREENSHOT_SERVICE);
            final Intent serviceIntent = new Intent();
            serviceIntent.setPackage(SYSUI_PACKAGE);
            serviceIntent.setComponent(serviceComponent);
            ServiceConnection conn = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    synchronized (mScreenshotLock) {
                    	Log.d("snail_screenshot", "----------------takeScreenshot----onServiceConnected------boolean=="+String.valueOf(mScreenshotConnection != this));
                        if (mScreenshotConnection != this) {
                            return;
                        }
                        Messenger messenger = new Messenger(service);
                        Message msg = Message.obtain(null, 0);
                        final ServiceConnection myConn = this;
                        Handler h = new Handler(PrizeFloatApp.getInstance().getHandler().getLooper()) {
                            @Override
                            public void handleMessage(Message msg) {
                                synchronized (mScreenshotLock) {
                                    if (mScreenshotConnection == myConn) {
                                        mContext.unbindService(mScreenshotConnection);
                                        mScreenshotConnection = null;
                                        PrizeFloatApp.getInstance().getHandler().removeCallbacks(mScreenshotTimeout);
                                    }
                                }
                            }
                        };
                        msg.replyTo = new Messenger(h);
                        msg.arg1  = 0;
                        msg.arg2 = 1;
                        try {
                            messenger.send(msg);
                        } catch (RemoteException e) {
                        }
                    }
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    synchronized (mScreenshotLock) {
                    	Log.d("snail_screenshot", "----------------takeScreenshot----onServiceDisconnected------boolean=="+String.valueOf(mScreenshotConnection != this));
                        if (mScreenshotConnection != null) {
                            mContext.unbindService(mScreenshotConnection);
                            mScreenshotConnection = null;
                            PrizeFloatApp.getInstance().getHandler().removeCallbacks(mScreenshotTimeout);
                        }
                    }
                }
            };
            if (mContext.bindService(serviceIntent, conn, Context.BIND_AUTO_CREATE)) {
                mScreenshotConnection = conn;
                PrizeFloatApp.getInstance().getHandler().postDelayed(mScreenshotTimeout, 10000);
            }
        }
    }
	final  Runnable mScreenshotTimeout = new Runnable() {
        @Override public void run() {
            synchronized (mScreenshotLock) {
                if (mScreenshotConnection != null) {
                	PrizeFloatApp.getContext().unbindService(mScreenshotConnection); 
                    mScreenshotConnection = null;
                    notifyScreenshotError();
                }
            }
        }
    };
    private void notifyScreenshotError() {
        // If the service process is killed, then ask it to clean up after itself
        final ComponentName errorComponent = new ComponentName(SYSUI_PACKAGE,SYSUI_SCREENSHOT_ERROR_RECEIVER);
        Intent errorIntent = new Intent(Intent.ACTION_USER_PRESENT);
        errorIntent.setComponent(errorComponent);
        errorIntent.addFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY_BEFORE_BOOT |Intent.FLAG_RECEIVER_FOREGROUND);
        PrizeFloatApp.getContext().sendBroadcastAsUser(errorIntent, UserHandle.CURRENT);
    }
}
