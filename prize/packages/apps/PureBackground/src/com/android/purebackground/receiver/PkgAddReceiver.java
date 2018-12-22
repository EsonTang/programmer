package com.android.purebackground.receiver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.android.purebackground.util.PureBackgroundUtils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;
/*-prize add by lihuangyuan,for whitelist -2017-05-06-start-*/
import android.os.WhiteListManager;
import android.provider.WhiteListColumns;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.INetworkManagementService;
import android.os.ServiceManager;
/*-prize add by lihuangyuan,for whitelist -2017-05-06-end-*/
public class PkgAddReceiver extends BroadcastReceiver {

    private static final String TAG = "PureBackground";
    private static final boolean DEBUG = true;

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction().equals(Intent.ACTION_PACKAGE_ADDED)) {
            if(DEBUG) Log.i(TAG, "PkgAddReceiver intent " + intent);
            int mSwitchValues = 0;
            try {
                mSwitchValues = (int)Settings.System.getLong(context.getContentResolver(), PureBackgroundUtils.PURE_BG_STATUS_OPEN_VALUE);
            } catch (SettingNotFoundException e) {
                // TODO Auto-generated catch block
                Log.i(TAG, "PkgAddReceiver SettingNotFoundException e" + e);
                e.printStackTrace();
            }
            Log.i(TAG, "PkgAddReceiver mSwitchValues=" + mSwitchValues);
            //if(1 == mSwitchValues){
                String packageName = intent.getDataString().replace("package:", "");
                Log.i(TAG, "PkgAddReceiver packageName= " + packageName);
		  /*-prize add by lihuangyuan,for whitelist -2017-05-06-start-*/
                WhiteListManager whiteListMgr = (WhiteListManager)context.getSystemService(Context.WHITELIST_SERVICE);
		   String [] hideList = whiteListMgr.getPurebackgroundHideList();
		   boolean isHide = false;
		   if(PureBackgroundUtils.isInList(packageName, hideList))
		   {
		   	isHide = true;
		   }
		   Log.i(TAG, "PkgAddReceiver isHide: " +isHide);
		   if(!isHide)
		   {
		   	String [] defList = whiteListMgr.getPurebackgroundDefList();
		   	boolean isDefEnable = false;
			if(PureBackgroundUtils.isInList(packageName, defList))
			{
				isDefEnable = true;
			}
			Log.i(TAG, "PkgAddReceiver isDefEnable: " + isDefEnable);
			if(isDefEnable)
			{
				PureBackgroundUtils.insertToEnableDb(context,packageName,true);
			}
			else
			{				
				PureBackgroundUtils.insertToEnableDb(context,packageName,false);
			}
		   }
		   /*-prize add by lihuangyuan,for net control forbade apk access network -2017-05-23-start-*/
		   String [] forbadeList = whiteListMgr.getNetForbadeList();
		   if(PureBackgroundUtils.isInList(packageName, forbadeList))
		   {
		   	PackageManager packageManager = context.getPackageManager();
			try
			{
				ApplicationInfo appinfo = packageManager.getApplicationInfo(packageName, PackageManager.GET_ACTIVITIES);
				 if(appinfo != null)
				 {
					 int userid = appinfo.uid;
					 setNetwork(userid,0);
					 setNetwork(userid,1);
					 Log.i(TAG,"setNetworkForbided "+packageName+" userid:"+userid);
				 }
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		   }		   
		   /*-prize add by lihuangyuan,for net control forbade apk access network -2017-05-23-end-*/
            //}
              /*-prize add by lihuangyuan,for whitelist -2017-05-06-end-*/
        }
    }
    /*-prize add by lihuangyuan,for net control forbade apk access network -2017-05-23-start-*/
    private void setNetwork(int uid, int netType) 
	{
		try 
		{
			INetworkManagementService netMgr = INetworkManagementService.Stub.asInterface(ServiceManager.getService(Context.NETWORKMANAGEMENT_SERVICE));
			netMgr.setFirewallUidChainRule(uid, netType, true);
	    	}
		catch (Exception e) {
			//e.printStackTrace();
		}
	}
	/*-prize add by lihuangyuan,for net control forbade apk access network -2017-05-23-end-*/
}
