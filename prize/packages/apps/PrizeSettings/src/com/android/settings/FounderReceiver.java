package com.android.settings;

import java.io.File;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.app.ActivityManagerNative;
import android.app.ActivityManager;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.view.inputmethod.InputMethodManager;
import java.util.List;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;

public class FounderReceiver extends BroadcastReceiver{

	private static final String TAG = FounderReceiver.class.getSimpleName();
	private static final String ACTION_FOUNDER = "com.founder.hifont.CHANGE_FONT";
	private static final String ACTION_INPUT_METHOD_CHANGED = "android.intent.action.INPUT_METHOD_CHANGED";
	private Context mContext;
    private static String mInputMethodString;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		mContext = context;
		if(ACTION_FOUNDER.equals(action)) {
			String filepath = intent.getStringExtra("filepath");
			try{
				File file = new File(filepath);
				if(!"system_fonts".equals(filepath) && !file.canRead()) {
					return;
				}
				if(mInputMethodString == null) {
					mInputMethodString = Settings.Secure.getString(context.getContentResolver(),  Settings.Secure.DEFAULT_INPUT_METHOD);
				}
			} catch (Exception e) {
				Log.w(TAG, "@@@---font file err:" + e.getMessage(), e);
				return;
			}
			
			Configuration config = new Configuration();
	        try {
	        	android.os.SystemProperties.set("persist.sys.founder.fontpath", filepath);
	        	config.updateFrom(ActivityManagerNative.getDefault().getConfiguration());
	        	float f1 = context.getResources().getConfiguration().fontScale;
				for (float f2 = f1 - 1.0E-4F;; f2 = f1 + 1.0E-4F) {
					config.fontScale = f2;
					break;
				}
				
				SharedPreferences prfs = context.getSharedPreferences("founder_fonts.xml", Context.MODE_PRIVATE);
		        SharedPreferences.Editor ed = prfs.edit();
		        long time = prfs.getLong("time", 0);
		        String imeId;
		        if(time == 0 || time < (System.currentTimeMillis() -10000)) {
		        	imeId = Settings.Secure.getStringForUser(
		            		context.getContentResolver(), 
		            		Settings.Secure.DEFAULT_INPUT_METHOD, 
							ActivityManagerNative.getDefault().getCurrentUser().id);
					ed.putString("imeId", imeId).commit();
				} else {
					imeId = prfs.getString("imeId", mInputMethodString);
		        }
	        
				Log.i(TAG, "@@@---default imeId:" + imeId);
		        ed.putLong("time", System.currentTimeMillis()).commit();
		        
	            String pkgName = imeId.substring(0, imeId.indexOf("/"));
	            String clsName = imeId.substring(imeId.indexOf("/")+1);
	            setInputMethod("com.android.inputmethod.latin/.LatinIME");
	            ActivityManagerNative.getDefault().updatePersistentConfiguration(config);
				
				forcestopThirdApps(mContext);
				Log.i(TAG,"com.tencent.mm");
				
	            
				android.os.Message msg = H.obtainMessage();
                Bundle b = new Bundle();
                b.putString("imeId", imeId);
				msg.obj = config;
                msg.setData(b);
                H.sendMessageDelayed(msg, 2000);
                
	        } catch (Exception e) {
	            Log.w(TAG, "@@@---onReceive---E:" + e.getMessage(), e);
	        }
		} else if(ACTION_INPUT_METHOD_CHANGED.equals(action)) {
			try{
					mInputMethodString = Settings.Secure.getString(context.getContentResolver(),  Settings.Secure.DEFAULT_INPUT_METHOD);
			} catch (Exception e) {
				Log.w(TAG, "@@@---font file err:" + e.getMessage(), e);
				return;
			}
		}
		
	}
	
	
	public void forcestopThirdApps(Context context) {
        List<PackageInfo> infos = context.getPackageManager().getInstalledPackages(0);
        int uid = android.os.UserHandle.myUserId();
        for (PackageInfo packageInfo : infos) {
            if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) <= 0) {
                String pkgName = packageInfo.applicationInfo.packageName;
                Log.d("Third", "@@@----pkgName:" + pkgName);
				try{
							ActivityManagerNative.getDefault().forceStopPackage(pkgName, uid);
	
				}catch(Exception e)
				{
					
				}
            }else{
				 String pkgName = packageInfo.applicationInfo.packageName;
				 
				 if((!pkgName.contains("com.android")||pkgName.equals("com.android.browser"))
					 &&!pkgName.contains("com.mediatek")
					&&!pkgName.equals("android")
					&&!pkgName.equals("com.google.android.webview")
					 &&!pkgName.contains("inputmethod")
					 &&(!pkgName.contains("com.prize"))){					 
					try{
					Log.d("Apps", "@@@----pkgName:" + pkgName);
					ActivityManagerNative.getDefault().forceStopPackage(pkgName, uid);
				}catch(Exception e)
				{
					
				} 
				}
			}
        }
    }
	
	public void setInputMethod(String imeId) { 
		try {
			Settings.Secure.putStringForUser(
					mContext.getContentResolver(), 
					Settings.Secure.DEFAULT_INPUT_METHOD, 
					imeId, 
					ActivityManagerNative.getDefault().getCurrentUser().id);
			
			android.view.inputmethod.InputMethodManager.getInstance().setInputMethod(null, imeId);
			Log.i(TAG, "@@@---setInputMethod:" + imeId);
		} catch (Exception e) {
            Log.w(TAG, "@@@---onReceive---E:" + e.getMessage(), e);
        }
		
	}
	
	android.os.Handler H = new android.os.Handler() {
		public void handleMessage(android.os.Message msg) {
            try{
                String imeId =   msg.getData().getString("imeId");
                setInputMethod(imeId);
                ActivityManagerNative.getDefault().updatePersistentConfiguration((Configuration) msg.obj);
            } catch (Exception e) {
                e.printStackTrace();
            }
		}
	};
}
