package com.android.purebackground.receiver;

import com.android.purebackground.util.PureBackgroundUtils;
import java.util.ArrayList;
import java.util.List;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.LauncherApps;
import android.content.pm.LauncherActivityInfo;
import android.content.pm.PackageManager;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Log;
/*-prize add by lihuangyuan,for whitelist -2017-05-06-start-*/
import android.os.WhiteListManager;
import android.provider.WhiteListColumns;
/*-prize add by lihuangyuan,for whitelist -2017-05-06-end-*/
public class BootCompletedReceiver extends BroadcastReceiver {

    private static final String TAG = "PureBackground";
    private PackageManager mPackageManager;
    private LauncherApps mLauncherApps;
    private List<String> mAppTempAllList = new ArrayList<String>();

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            Log.v(TAG, "BootCompletedReceiver android.intent.action.BOOT_COMPLETED");
            if(PureBackgroundUtils.isFirstOpenPureBackgroud(context)){
                queryFilterAppInfo(context);
                firstOpenPureBackgroud(context);
            } else {
                setFont(context);
            }
        }
    }

    public void queryFilterAppInfo(Context context) {

        Log.i(TAG,"BootCompletedReceiver queryFilterAppInfo");
        mPackageManager = context.getPackageManager();
        mAppTempAllList.clear();
        mLauncherApps = (LauncherApps) context.getSystemService(Context.LAUNCHER_APPS_SERVICE);
        final List<LauncherActivityInfo> lais = mLauncherApps.getActivityList(null, UserHandle.getCallingUserHandle());
	 /*-prize add by lihuangyuan,for whitelist -2017-05-06-start-*/
	 WhiteListManager whiteListMgr = (WhiteListManager)context.getSystemService(Context.WHITELIST_SERVICE);
	 String [] hideList = whiteListMgr.getPurebackgroundHideList();
	 /*-prize add by lihuangyuan,for whitelist -2017-05-06-end-*/
        for (LauncherActivityInfo lai : lais) {
            ApplicationInfo app = lai.getApplicationInfo();
            String packageName = app.loadLabel(mPackageManager).toString();
	     /*-prize modify by lihuangyuan,for whitelist -2017-05-06-start-*/
            /*if ((app != null) && (PureBackgroundUtils.hideAppIcon(app.packageName) &&
                (!PureBackgroundUtils.isThirdAppMarket(app.packageName)))) */
            if((app != null) && PureBackgroundUtils.isInList(app.packageName,hideList))
            /*-prize modify by lihuangyuan,for whitelist -2017-05-06-end-*/
           {
                Log.i(TAG, "BootCompletedReceiver hide packageName=" + packageName + ", app.packageName=" + app.packageName);
                continue;
            }
	      /*-prize modify by lihuangyuan,for whitelist -2017-05-06-start-*/
	     if(mAppTempAllList.contains(app.packageName))
	     	{
	     		continue;
	     	}
	     /*-prize modify by lihuangyuan,for whitelist -2017-05-06-end-*/
            Log.i(TAG, "BootCompletedReceiver packageName=" + packageName + ", app.packageName=" + app.packageName);
            mAppTempAllList.add(app.packageName);
        }

        List<ApplicationInfo> listAppcations = mPackageManager.getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES);
        for (ApplicationInfo app : listAppcations) {
            if ((app != null) && (app.packageName != null) && ((app.prizeFlags & ApplicationInfo.FLAG_IS_PREBUILT_THIRD_APPS) != 0)) {
                if(app.packageName.equals("com.ekesoo.font")) {
                    Log.i(TAG, "BootCompletedReceiver add app.packageName=" + app.packageName);
                    mAppTempAllList.add(app.packageName);
                }
            }
        }
        Log.i(TAG,"BootCompletedReceiver queryFilterAppInfo mAppTempAllList=" + mAppTempAllList);
    }

    public void firstOpenPureBackgroud(Context context){

        Log.i(TAG,"BootCompletedReceiver firstOpenPureBackgroud");
	 /*-prize add by lihuangyuan,for whitelist -2017-05-06-start-*/
	 WhiteListManager whiteListMgr = (WhiteListManager)context.getSystemService(Context.WHITELIST_SERVICE);
	 String [] defList = whiteListMgr.getPurebackgroundDefList();
	 
	 for (String mApp : mAppTempAllList) {
            if(PureBackgroundUtils.isInList(mApp,defList))
	     {
                Log.i(TAG,"BootCompletedReceiver firstOpenPureBackgroud Enable mApp " + mApp);
		   PureBackgroundUtils.insertToOrUpdateEnableDb(context,mApp,true);
            }
	     else 
            {
            	  Log.i(TAG,"BootCompletedReceiver firstOpenPureBackgroud Disable mApp " + mApp);
		  PureBackgroundUtils.insertToOrUpdateEnableDb(context,mApp,false);
            }
        }
	 Settings.System.putLong(context.getContentResolver(),PureBackgroundUtils.PURE_BG_STATUS_OPEN_VALUE, 1);
        Settings.System.putLong(context.getContentResolver(),PureBackgroundUtils.PURE_BG_STATUS_FIRST_VALUE, 1);        
	 /*-prize add by lihuangyuan,for whitelist -2017-05-06-end-*/
    }

    private void setFont(Context context) {
        int fontSettingsValue = Settings.System.getInt(context.getContentResolver(), "com.ekesoo.font.value", 0);
        Log.i(TAG,"BootCompletedReceiver setFont fontSettingsValue=" + fontSettingsValue);
        if(0 == fontSettingsValue) {
            PureBackgroundUtils.insertToOrUpdateEnableDb(context,"com.ekesoo.font",false);
            Settings.System.putInt(context.getContentResolver(),"com.ekesoo.font.value", 1);
        }
	 /*-prize add by lihuangyuan,for whitelist -2017-05-06-end-*/
    }
}
