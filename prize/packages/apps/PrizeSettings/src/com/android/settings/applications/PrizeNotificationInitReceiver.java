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
import com.mediatek.common.prizeoption.PrizeOption;
import android.net.Uri;
import android.database.Cursor;
import java.io.*;
import android.content.ContentResolver;
/**prize-add-game-reading-mode-liuweiquan-20160123-start*/
import android.app.NotificationManager;
import android.app.Notification;
import android.app.PendingIntent;
import com.android.settings.accessibility.AccessibilitySettings;
import com.android.settings.R;
/**prize-add-game-reading-mode-liuweiquan-20160123-end*/
/*Prize-set vlife network add by dengyu-20160913-start*/
import android.os.INetworkManagementService;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.RemoteException;
/*Prize-set vlife network add by dengyu-20160913-end*/

/*-prize add by lihuangyuan,for whitelist -2017-05-06-start-*/
import android.os.WhiteListManager;
import android.provider.WhiteListColumns;
/*-prize add by lihuangyuan,for whitelist -2017-05-06-end-*/
//prize liup 20161124 autoboot control start
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import android.os.Environment;
import org.xmlpull.v1.XmlSerializer;
import android.util.Xml;
import android.os.UserHandle;
import android.content.pm.IPackageManager;
import android.content.pm.ParceledListSlice;
import android.content.pm.ResolveInfo;
import android.os.Process;
//prize liup 20161124 autoboot control start

//add liup 20171016 recovery cali save data start
import java.io.File;
import java.io.IOException; 
import java.io.FileInputStream;
import java.io.InputStreamReader;  
import java.io.FileOutputStream;
//add liup 20171016 recovery cali save data end
/*-prize-add by lihuangyuan-for draw overlay other app permission-2017-12-15-start*/
import android.app.AppOpsManager;
/*-prize-add by lihuangyuan-for draw overlay other app permission-2017-12-15-end*/
public class PrizeNotificationInitReceiver extends BroadcastReceiver {

	private static Backend mBackend = new Backend();
	private PackageManager packageManager;
	private List<PackageInfo> packageInfos;
	private static boolean bPrizeNotificationCustom = false;
	private Context mContext;
	private static ContentResolver mContentResolver;
	private static Uri queryUri;
	
	//prize liup 20161124 autoboot control start
	private File mFile;
    private static final String FILE_DIR = "data/com.mediatek.security";
    private static final String FILE_NAME = "bootreceiver.xml";
    private static final String PKG_TAG = "pkg";
	private static final String TAG = "whitelist";
	//prize liup 20161124 autoboot control start
		private static final String filePath = "/data/prize_backup/";
	private static final String fileName = "prize_factory_gsensor";

	@Override
	public void onReceive(Context context, Intent intent) {
		mContext = context;
		if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
			mContentResolver = context.getContentResolver();
			packageManager = context.getPackageManager();
			
			bPrizeNotificationCustom = Settings.System.getInt(mContentResolver,Settings.System.PRIZE_NOTIFICATION_CUSTOM,0) != 0;		
			if(bPrizeNotificationCustom){
				initAllAppBackend();
				//prize liup 20161124 autoboot control start
				initAllAppBoot();
				//prize liup 20161124 autoboot control start
				Settings.System.putInt(mContentResolver,Settings.System.PRIZE_NOTIFICATION_CUSTOM,0);
			}
			
			if(PrizeOption.PRIZE_SLEEP_GESTURE){
				Log.e("liup","ACTION_BOOT_COMPLETED");
				init_sleep_gesture();
			}
			
			initGsesorCali();//add liup 20171016 recovery cali save data
			/**prize-add-game-reading-mode-liuweiquan-20160123-start*/
			handlePrizeGameMode();
			handlePrizeReadingMode();
			/**prize-add-game-reading-mode-liuweiquan-20160123-end*/			
		}
		if(intent.getAction().equals(Intent.ACTION_SCREEN_ON)){
			Log.e("liup","ACTION_SCREEN_ON");
			if(bPrizeNotificationCustom) {
				init_sleep_gesture();
			}
		}
		/**prize-add-game-reading-mode-liuweiquan-20160415-start*/
		if(intent.getAction().equals("accessibility.prize.onoff")){
			handlePrizeGameMode();
			handlePrizeReadingMode();
		}
		/**prize-add-game-reading-mode-liuweiquan-20160415-end*/
	}
	//add liup 20171016 recovery cali save data start
	private void initGsesorCali() {
		String fileData = null;
		File file = new File(filePath);
		if (!file.exists()) {
			return;
		}
		Log.e("liup","initGsesorCali");	
		try {
			FileInputStream mFileInputStream = new FileInputStream(filePath + fileName);
			InputStreamReader mInputStreamReader = new InputStreamReader(mFileInputStream, "UTF-8");
			char[] input = new char[mFileInputStream.available()];
			mInputStreamReader.read(input);
			mInputStreamReader.close();
			mFileInputStream.close();
			fileData = new String(input);
		} catch (Exception e) {
		}
		Log.e("liup","fileData = " + fileData);	
		try {	
			if(fileData != null){
				String[] cmdMode = new String[]{"/system/bin/sh","-c","echo" + " " + fileData + " > /proc/gsensor_cali"};
				Runtime.getRuntime().exec(cmdMode);		
			}			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	//add liup 20171016 recovery cali save data end
	
	/**prize-add-game-reading-mode-liuweiquan-20160415-start*/
	private void handlePrizeGameMode() {
		final boolean bPrizeGameMode = Settings.System.getInt(mContext.getContentResolver(),Settings.System.PRIZE_GAME_MODE,0) == 1;
		if(bPrizeGameMode){
			addIconToStatusbar(R.drawable.game_mode,R.string.prize_game_mode_title,R.string.prize_game_mode_notification_summary,AccessibilitySettings.GameModeNotificationID);
		}else{
			deleteIconToStatusbar(AccessibilitySettings.GameModeNotificationID);
		}
	}
	private void handlePrizeReadingMode() {
		final boolean bPrizeReadingMode = Settings.System.getInt(mContext.getContentResolver(),Settings.System.PRIZE_READING_MODE,0) == 1;
		if(bPrizeReadingMode){
			addIconToStatusbar(R.drawable.reading_mode,R.string.prize_reading_mode_title,R.string.prize_reading_mode_notification_summary,AccessibilitySettings.ReadingModeNotificationID);
		}else{
			deleteIconToStatusbar(AccessibilitySettings.ReadingModeNotificationID);
		}
	}
	private void deleteIconToStatusbar(int notificationID){ 
		NotificationManager notifyManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
		notifyManager.cancel(notificationID);
	}
	/**prize-add-game-reading-mode-liuweiquan-20160415-end*/
	/**prize-add-game-reading-mode-liuweiquan-20160123-start*/
	private void addIconToStatusbar(int iconID,int titleID,int summaryID,int notificationID){ 
		NotificationManager notificationManager =(NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
		String titleStr = mContext.getString(titleID);
		String summaryStr = mContext.getString(summaryID);
		Notification notification = new Notification();
		notification.icon = iconID;
		notification.when = 0;
		notification.flags |= Notification.FLAG_ONGOING_EVENT;
		notification.flags |= Notification.FLAG_NO_CLEAR;
		notification.flags |= Notification.FLAG_KEEP_NOTIFICATION_ICON;
			
		notification.tickerText = titleStr;
		notification.defaults = 0; // please be quiet
		notification.sound = null;
		notification.vibrate = null;
		notification.priority = Notification.PRIORITY_DEFAULT;
		//Intent intent = new Intent(mContext,com.android.settings.Settings.class); 
		Intent intent = new Intent();
		intent.setAction("android.settings.ACCESSIBILITY_SETTINGS");
		PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, intent, 0);
		notification.setLatestEventInfo(mContext, titleStr, summaryStr, pendingIntent);     
		notificationManager.notify(notificationID, notification);
	}
  /**prize-add-game-reading-mode-liuweiquan-20160123-end*/ 
	
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
	/*-prize add by lihuangyuan,for whitelist -2017-05-06-start-*/
	public static boolean isInList(String pkgname,String []arylist)
	{
	   	if(arylist == null)return false;
		for(int i=0;i<arylist.length;i++)
		{
			if(pkgname.contains(arylist[i]))
			{
				return true;
			}
		}
		return false;
	}
	/*-prize add by lihuangyuan,for whitelist -2017-05-06-end-*/
	//prize liup 20161124 autoboot control start
	public void initAllAppBoot() {
		File dataDir = Environment.getDataDirectory();
        File systemDir = new File(dataDir, FILE_DIR);
        mFile = new File(systemDir, FILE_NAME);
		
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (mFile) {
                List<String> denyList = new ArrayList<String>();
		  List<String> mAutoBootList = getBootReceivers(mContext);
		 /*-prize add by lihuangyuan,for whitelist -2017-05-06-start-*/
		  //autolaunch
		WhiteListManager whiteListMgr = (WhiteListManager)mContext.getSystemService(Context.WHITELIST_SERVICE);
 		String [] defList = whiteListMgr.getAutoLaunchDefList();
		/*-prize add by lihuangyuan,for whitelist -2017-05-06-end-*/
                for (String packagename : mAutoBootList) {					
			  /*-prize add by lihuangyuan,for whitelist -2017-05-06-start-*/
			  if(!isInList(packagename,defList))
			 {
				denyList.add(packagename);
				Log.i(TAG,"addpackage "+packagename+" in autodef list");
			 }
			 /*-prize add by lihuangyuan,for whitelist -2017-05-06-end-*/
                }

                if (mFile.exists() && (!mFile.getAbsoluteFile().delete())) {
                    Log.e("liup", "saveToFile, deleteFile failed");
                }

                if (denyList.size() == 0) {
                    Log.d(TAG, "saveToFile, size = 0");
		      /*-prize add by lihuangyuan,for whitelist -2017-11-08-start-*/
		     Settings.System.putString(mContext.getContentResolver(), "denyAutoAppList","");
		     /*-prize add by lihuangyuan,for whitelist -2017-11-08-end-*/
                    return;
                }

                FileOutputStream os = null;
                try {
                    mFile.getAbsoluteFile().createNewFile();
                    os = new FileOutputStream(mFile);
                    XmlSerializer xml = Xml.newSerializer();
                    xml.setOutput(os, "UTF-8");
                    xml.startDocument("UTF-8", true);
                    xml.startTag(null, "bootlist");
                    for (String name : denyList) {
                        Log.d(TAG, "saveToFile, tag = " + name);
                        xml.startTag(null, PKG_TAG);
                        xml.attribute(null, "n", name);
                        xml.attribute(null, "u", String.valueOf(UserHandle.myUserId()));
                        xml.attribute(null, "e", "false");
                        xml.endTag(null, PKG_TAG);
                    }
                    xml.endTag(null, "bootlist");
                    xml.endDocument();
                } catch (IOException | IllegalArgumentException | IllegalStateException e) {
                    Log.e("liup", "saveToFile, exception + " + e);
                    e.printStackTrace();
                } finally {
                    if (os != null) {
                        try {
                            os.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
			  /*-prize add by lihuangyuan,for whitelist -2017-11-08-start-*/
			  //write to settings
			  String denyAutoAppList ="";
			  for (String name : denyList) 
			  {
			      denyAutoAppList += name + ",";
			  }
			  Settings.System.putString(mContext.getContentResolver(), "denyAutoAppList",denyAutoAppList);
			  /*-prize add by lihuangyuan,for whitelist -2017-11-08-end-*/
                }//end synchronized
            }//end run
        });
        t.start();
    }
	
	private List<String> getBootReceivers(Context context) {
        List<String> bootReceivers = new ArrayList<String>();
        String[] policy = context
                .getResources()
                .getStringArray(
                        com.mediatek.internal.R.array.config_auto_boot_policy_intent_list);
        Log.d(TAG, "getBootReceivers, policy:" + policy);
        IPackageManager pm = IPackageManager.Stub.asInterface(ServiceManager
                .getService("package"));

        for (int i = 0; i < policy.length; i++) {
            Intent intent = new Intent(policy[i]);
            try {
                ParceledListSlice<ResolveInfo> parlist = pm
                        .queryIntentReceivers(intent, intent
                                .resolveTypeIfNeeded(context
                                        .getContentResolver()),
                                PackageManager.GET_META_DATA, UserHandle.myUserId());
                if (parlist != null) {
                    List<ResolveInfo> receivers = parlist.getList();
                    if (receivers != null) {
                        for (int j = 0; j < receivers.size(); j++) {
                            ResolveInfo info = receivers.get(j);
                            String packageName =
                              (info.activityInfo != null) ? info.activityInfo.packageName : null;
                            ApplicationInfo appinfo = pm.getApplicationInfo(
                                    packageName, 0, UserHandle.myUserId());
                            if (isSystemApp(appinfo)) {
                                continue;
                            }
                            if (packageName != null
                                    && !bootReceivers.contains(packageName)) {

                                Log.d(TAG, "getBootReceivers, packageName:"
                                        + packageName);
                                bootReceivers.add(packageName);
                            }
                        }
                    }
                }
            } catch (RemoteException e) {
                // Should never happend
                continue;
            }
        }
        return bootReceivers;
    }
	
	public static boolean isSystemApp(ApplicationInfo applicationInfo) {
        if (applicationInfo != null) {
            int appId = UserHandle.getAppId(applicationInfo.uid);
            return ((applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) ||
                    (appId == Process.SYSTEM_UID);
        } else {
            Log.d(TAG, "isSystemApp() return false with null packageName");
            return false;
        }
    }
	//prize liup 20161124 autoboot control start
			
	public void initAllAppBackend() {
		packageInfos = packageManager
				.getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES);
		/*-prize add by lihuangyuan,for whitelist -2017-05-06-start-*/
		WhiteListManager whiteListMgr = (WhiteListManager)mContext.getSystemService(Context.WHITELIST_SERVICE);
		 String [] defList = whiteListMgr.getNotificationDefList();
		 String [] hideList = whiteListMgr.getNotificationHideList();
		 /*-prize add by lihuangyuan,for whitelist -2017-05-06-end-*/
		 /*-prize-add by lihuangyuan-for draw overlay other app permission-2017-12-15-start*/
		AppOpsManager mAppOpsManager = (AppOpsManager) mContext.getSystemService(Context.APP_OPS_SERVICE);
		String [] floatdeflist = whiteListMgr.getFloatDefList();
		/*-prize-add by lihuangyuan-for draw overlay other app permission-2017-12-15-end*/
		for (PackageInfo info : packageInfos) {
			ApplicationInfo appInfo = info.applicationInfo;
			
			String packageName = appInfo.packageName;
			int uid = appInfo.uid;
			/*-prize add by lihuangyuan,for whitelist -2017-05-06-start-*/
			if(isInList(packageName,hideList)||packageName.equals("android"))
			{
				//do nothing
				Log.i(TAG,"initAllAppBackend pkg:"+packageName+" is in hide list ");
			}
			else
			{							
				if(isInList(packageName,defList)||packageName.equals("com.prize"))
				{
					Log.i(TAG,"initAllAppBackend pkg:"+packageName+" is in deflist set Banned false");
					mBackend.setNotificationsBanned(packageName, uid, false);
				}
				else
				{
					Log.i(TAG,"initAllAppBackend pkg:"+packageName+" is not in deflist set Banned true");
					mBackend.setNotificationsBanned(packageName, uid, true);
				}
			}
			/*-prize add by lihuangyuan,for whitelist -2017-05-06-end-*/
			/*-prize-add by lihuangyuan-for draw overlay other app permission-2017-12-15-start*/
			if(isInList(packageName,floatdeflist)||packageName.equals("android")||appInfo.isSystemApp())
			{
			    mAppOpsManager.setMode(AppOpsManager.OP_SYSTEM_ALERT_WINDOW,
                				appInfo.uid, packageName, AppOpsManager.MODE_ALLOWED);
			}
			else
			{
			    mAppOpsManager.setMode(AppOpsManager.OP_SYSTEM_ALERT_WINDOW,
                				appInfo.uid, packageName, AppOpsManager.MODE_ERRORED);
			}			
			/*-prize-add by lihuangyuan-for draw overlay other app permission-2017-12-15-end*/
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
        static INotificationManager sINM = INotificationManager.Stub.asInterface(
                ServiceManager.getService(Context.NOTIFICATION_SERVICE));

        public boolean setNotificationsBanned(String pkg, int uid, boolean banned) {
            try {
                sINM.setNotificationsEnabledForPackage(pkg, uid, !banned);
                return true;
            } catch (Exception e) {
               return false;
            }
        }

        public boolean getNotificationsBanned(String pkg, int uid) {
            try {
                final boolean enabled = sINM.areNotificationsEnabledForPackage(pkg, uid);
                return !enabled;
            } catch (Exception e) {
                return false;
            }
        }

        public boolean getHighPriority(String pkg, int uid) {
            try {
                return sINM.getPriority(pkg, uid) == Notification.PRIORITY_MAX;
            } catch (Exception e) {
                return false;
            }
        }

        public boolean setHighPriority(String pkg, int uid, boolean highPriority) {
            try {
                sINM.setPriority(pkg, uid,
                        highPriority ? Notification.PRIORITY_MAX : Notification.PRIORITY_DEFAULT);
                return true;
            } catch (Exception e) {
                return false;
            }
        }

        public boolean getSensitive(String pkg, int uid) {
            try {
                return sINM.getVisibilityOverride(pkg, uid) == Notification.VISIBILITY_PRIVATE;
            } catch (Exception e) {
                return false;
            }
        }

        public boolean setSensitive(String pkg, int uid, boolean sensitive) {
            try {
                sINM.setVisibilityOverride(pkg, uid,
                        sensitive ? Notification.VISIBILITY_PRIVATE
                                : NotificationListenerService.Ranking.VISIBILITY_NO_OVERRIDE);
                return true;
            } catch (Exception e) {
                return false;
            }
        }
    }
}
