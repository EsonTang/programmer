package com.android.settings.applications;

import java.util.ArrayList;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.app.INotificationManager;
import android.app.Notification;
import android.os.AsyncTask;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.service.notification.NotificationListenerService;
import android.util.ArrayMap;
import android.widget.Toast;
import android.provider.Settings;
import android.util.Log;
import com.mediatek.common.mom.IMobileManager;
import com.mediatek.common.mom.ReceiverRecord;
import com.mediatek.common.prizeoption.PrizeOption;
import android.net.Uri;
import android.database.Cursor;
import java.io.*;
import android.content.ContentResolver;
import android.content.res.Configuration;
import android.app.ActivityManagerNative;
import android.os.RemoteException;
import android.os.SystemProperties;

public class PrizeNotificationInitReceiver extends BroadcastReceiver {

	private static Backend mBackend = new Backend();
	private PackageManager packageManager;
	private List<PackageInfo> packageInfos;
	private static boolean bPrizeNotificationCustom = false;
	private IMobileManager mMoMService;
	private	List<ReceiverRecord> recordList = null;
	private Context mContext;
	private static ContentResolver mContentResolver;
	private static Uri queryUri;
	private final Configuration mCurConfig = new Configuration();
	private static boolean isSimpleLauncherShow=false;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		mContentResolver = context.getContentResolver();
		Log.e("liup","intent.getAction() = " + intent.getAction());
		
		if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
			
			packageManager = context.getPackageManager();
			
			if (mMoMService == null) {
				mMoMService = (IMobileManager) context.getSystemService(Context.MOBILE_SERVICE);
			}
			
			bPrizeNotificationCustom = Settings.System.getInt(mContentResolver,Settings.System.PRIZE_NOTIFICATION_CUSTOM,0) != 0;		
			if(bPrizeNotificationCustom){
				//bootcomplete save fontsize
				Settings.System.putInt(mContentResolver,Settings.System.PRIZE_SIMPLELAUNCHER_FONTSIZE_CUSTOM,readFontSizePreference());
				
				Settings.System.putInt(mContentResolver,Settings.System.PRIZE_NOTIFICATION_CUSTOM,0);
				initAllAppBackend();
				initAllAppBoot();
			}
			
			if(PrizeOption.PRIZE_SLEEP_GESTURE){
				init_sleep_gesture();
			}
			
			
		}
		if(intent.getAction().equals(Intent.ACTION_SCREEN_ON)){		
			if(bPrizeNotificationCustom) {
				init_sleep_gesture();
			}
		}
		
		if(intent.getAction().equals("com.cooee.SimpleLauncher2.show")){
			writeFontSizePreference(3);
			SystemProperties.set("persist.sys.simpleLuancher", "1");
		}
		if(intent.getAction().equals("com.cooee.SimpleLauncher2.hide")){
			writeFontSizePreference(Settings.System.getInt(mContentResolver,Settings.System.PRIZE_SIMPLELAUNCHER_FONTSIZE_CUSTOM,1));
			SystemProperties.set("persist.sys.simpleLuancher", "0");
		}
		
	}
	private int readFontSizePreference() {
		int value = 0;
        try {
            mCurConfig.updateFrom(ActivityManagerNative.getDefault().getConfiguration());
        } catch (RemoteException e) {
        }
		if(mCurConfig.fontScale == 0.9f){
			value = 0;
		}else if(mCurConfig.fontScale == 1.1f){
			value = 2;
		}else if(mCurConfig.fontScale == 1.15f){
			value = 3;
		}else{
			value = 1;
		}
		return value;
    }
	
	private void writeFontSizePreference(int fontValue) {
		if(fontValue == 0){
			mCurConfig.fontScale = 0.9f;
		}else if(fontValue == 2){
			mCurConfig.fontScale = 1.1f;
		}else if(fontValue == 3){
			mCurConfig.fontScale = 1.75f;
		}else{
			mCurConfig.fontScale = 1.0f;
		}
        try {
            ActivityManagerNative.getDefault().updatePersistentConfiguration(mCurConfig);
        } catch (RemoteException e) {
        }
    }
	
	private void init_sleep_gesture(){
		queryUri = Uri.parse("content://com.prize.sleepgesture/sleepgesture"); 
		int bGesture = 0;
		final boolean bSleepGesture = Settings.System.getInt(mContentResolver,Settings.System.PRIZE_SLEEP_GESTURE,0) == 1;
		Cursor cursor = mContentResolver.query(queryUri, null, null, null,null);
		if (cursor != null && cursor.moveToFirst()) {
			do {
				int onoff = cursor.getInt(cursor.getColumnIndex("onoff"));
				bGesture = bGesture + onoff;
			}while (cursor.moveToNext());
		}	
		if(cursor != null) {
			cursor.close();
		}
		if(bSleepGesture && bGesture > 0){
			try {	
	    		String[] cmdMode = new String[]{"/system/bin/sh","-c","echo" + " " + 1 + " > /proc/gt9xx_enable"};
				Runtime.getRuntime().exec(cmdMode);				
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}else{
			try {	
	    		String[] cmdMode = new String[]{"/system/bin/sh","-c","echo" + " " + 0 + " > /proc/gt9xx_enable"};
				Runtime.getRuntime().exec(cmdMode);				
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
	}
	
	public void initAllAppBoot() {
		try {
			recordList = mMoMService.getBootReceiverList();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(null == recordList){
			return;
		}
		for (ReceiverRecord info : recordList) {
			String packageName = info.packageName;
			if(packageName.equals("com.tencent.mm") || packageName.equals("com.tencen1.mm") || 
				packageName.equals("com.tencent.mobileqq") || packageName.equals("com.baidu.input") || packageName.contains("com.prize.appcenter.service")){
				mMoMService.setBootReceiverEnabledSetting(packageName, true);
			}else{	
				mMoMService.setBootReceiverEnabledSetting(packageName, false);
			}	
		}
	}
			
			
	public void initAllAppBackend() {
		packageInfos = packageManager
				.getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES);
		for (PackageInfo info : packageInfos) {
			ApplicationInfo appInfo = info.applicationInfo;
			
			String packageName = appInfo.packageName;
			int uid = appInfo.uid;
			
			if(!(packageName.equals("com.tencent.mm") || packageName.equals("com.tencen1.mm") || 
			packageName.equals("com.tencent.mobileqq") || packageName.contains("com.prize.appcenter.service"))){
				if (filterApp(appInfo)) {
					mBackend.setNotificationsBanned(packageName, uid, true);
				} else {
					if(packageName.equals("net.qihoo.launcher.widget.clockweather") || packageName.equals("com.qihoo360.mobilesafe")|| 
					packageName.equals("com.cooee.unilauncher") || packageName.equals("com.tencent.android.qqdownloader") || 
					packageName.equals("com.qiyi.video") || packageName.equals("com.baidu.browser.apps") || 
					packageName.equals("com.baidu.searchbox") || packageName.equals("com.tencent.mtt") ||
					packageName.equals("com.baidu.browser.apps_mr") || packageName.equals("com.joloplay.gamecenter.prize") ||
					packageName.equals("com.ifeng.news2") || packageName.equals("com.tencent.qqpimsecure") ||
					packageName.equals("com.sohu.inputmethod.sogou") || packageName.equals("com.qihoo.browser") ||
					packageName.equals("com.ss.android.article.news") || packageName.equals("com.ss.android.essay.joke") ||
					packageName.equals("com.yidian.dk") || packageName.equals("com.duomi.android") || packageName.equals("com.youku.phone") || 
					packageName.equals("com.qihoo.haosou") || packageName.equals("com.andreader.prein") || packageName.equals("com.tencent.portfolio") || 
					packageName.equals("com.android.browser") || packageName.equals("com.tianqiwhite") || packageName.equals("com.reach.weitoutiao") || 
					packageName.equals("com.UCMobile") || packageName.equals("com.sohu.newsclient")){
						mBackend.setNotificationsBanned(packageName, uid, true);
					}else{
						mBackend.setNotificationsBanned(packageName, uid, false);
					}
				}
			}
		}
	}

	public boolean filterApp(ApplicationInfo info) {
		if ((info.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) {
			return true;
		} else if ((info.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
			return true;
		}
		return false;
	}

	
	public static class Backend {
		static INotificationManager sINM = INotificationManager.Stub
				.asInterface(ServiceManager
						.getService(Context.NOTIFICATION_SERVICE));

		public boolean setNotificationsBanned(String pkg, int uid,
				boolean banned) {
			try {
				sINM.setNotificationsEnabledForPackage(pkg, uid, !banned);
				return true;
			} catch (Exception e) {
				return false;
			}
		}

		public boolean getNotificationsBanned(String pkg, int uid) {
			try {
				final boolean enabled = sINM.areNotificationsEnabledForPackage(
						pkg, uid);
				return !enabled;
			} catch (Exception e) {
				return false;
			}
		}

		public boolean getHighPriority(String pkg, int uid) {
			try {
				return sINM.getPackagePriority(pkg, uid) == Notification.PRIORITY_MAX;
			} catch (Exception e) {
				return false;
			}
		}

		public boolean setHighPriority(String pkg, int uid, boolean highPriority) {
			try {
				sINM.setPackagePriority(pkg, uid,
						highPriority ? Notification.PRIORITY_MAX
								: Notification.PRIORITY_DEFAULT);
				return true;
			} catch (Exception e) {
				return false;
			}
		}

		public boolean getSensitive(String pkg, int uid) {
			try {
				return sINM.getPackageVisibilityOverride(pkg, uid) == Notification.VISIBILITY_PRIVATE;
			} catch (Exception e) {
				return false;
			}
		}

		public boolean setSensitive(String pkg, int uid, boolean sensitive) {
			try {
				sINM.setPackageVisibilityOverride(
						pkg,
						uid,
						sensitive ? Notification.VISIBILITY_PRIVATE
								: NotificationListenerService.Ranking.VISIBILITY_NO_OVERRIDE);
				return true;
			} catch (Exception e) {
				return false;
			}
		}
	}
}
