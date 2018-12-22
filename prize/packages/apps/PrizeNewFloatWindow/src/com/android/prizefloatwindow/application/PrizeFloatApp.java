package com.android.prizefloatwindow.application;

import java.lang.ref.WeakReference;
import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;


public class PrizeFloatApp extends Application {
    private static PrizeFloatApp instance;
    public static boolean DEBUG = false;
    public  MyHandler mHandler;
    private static Context context;
    public static PrizeFloatApp getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        context = getApplicationContext();
    }
    public static Context getContext() {
		return context;
	}
    public  Handler getHandler() {
		if(mHandler == null){
			mHandler = new MyHandler(context,Looper.getMainLooper());
		}
		return mHandler;
	}
    public static class MyHandler extends Handler {
		private WeakReference<Context> reference;

		
		public MyHandler(Context context,Looper looper)
		{
			super(looper);
			reference = new WeakReference<>(context);
		}
		@Override
		public void handleMessage(Message msg) {
			Context context = (Context) reference.get();
			if (context != null) {
				switch (msg.what) {
				/*case InstallThread.UNINSTALL_SUCCESS:
					String uninstall_succ_packagename = (String) msg.obj;
					TXLog.d(TAG, "--MyHandler----handleMessage----uninstall_succ_packagename=="+uninstall_succ_packagename);
					ToastUtil.showToast(context,uninstall_succ_packagename+ context.getResources().getString(R.string.uninstall_success));
					break;*/
				default:
					break;
				}
			}
		}
	}
}
