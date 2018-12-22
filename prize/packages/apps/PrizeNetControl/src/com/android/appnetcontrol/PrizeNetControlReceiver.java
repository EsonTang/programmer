package com.android.appnetcontrol;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.RemoteException;
import android.os.INetworkManagementService;
import android.os.ServiceManager;
import java.util.Map;    
import java.util.Map.Entry;
import android.util.Log;
/*-prize add by lihuangyuan,for whitelist -2017-05-06-start-*/
import android.os.WhiteListManager;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
/*-prize add by lihuangyuan,for whitelist -2017-05-06-end-*/

import android.os.Bundle;
public class PrizeNetControlReceiver extends BroadcastReceiver{
	
	public static final String WIFI_PREFS_NAME = "WifiPrefsFile";
	public static final String G_PREFS_NAME = "GPrefsFile";
	Context mContext;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		mContext = context;
		// TODO Auto-generated method stub
		if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
			Log.d("PrizeNetControl", "BOOT_COMPLETED receive");
			readSpData();
			/*--prize-add by lihuangyuan,for skip  com.gangyun.beautysnap --2017-03-22-start-*/
			setAppsNetworkForbided(context);
			/*--prize-add by lihuangyuan,for skip  com.gangyun.beautysnap --2017-03-22-end-*/
		}
		if (intent.getAction().equals("com.mediatek.security.action.DATA_UPDATE")) {
			Log.d("PrizeNetControl", "com.mediatek.security.action.DATA_UPDATE");
			updateData(context,intent);
		}
	}
	/*--prize-add by lihuangyuan,for skip  com.gangyun.beautysnap --2017-03-22-start-*/
	private void setAppsNetworkForbided(Context context)
	{		
		PackageManager pkgmgr = context.getPackageManager();		
		
		/*-prize add by lihuangyuan,for whitelist -2017-05-06-start-*/
		WhiteListManager whiteListMgr = (WhiteListManager)context.getSystemService(Context.WHITELIST_SERVICE);
		 String [] forbadeList = whiteListMgr.getNetForbadeList();			
		if(forbadeList != null)
		{
			for(int i=0;i<forbadeList.length;i++)
			{
				try
				{
					ApplicationInfo appinfo = pkgmgr.getApplicationInfo(forbadeList[i], PackageManager.GET_ACTIVITIES);
					 if(appinfo != null)
					 {
						 int userid = appinfo.uid;
						 setNetwork(userid,0);
						 setNetwork(userid,1);
						 Log.i("PrizeNetControl","setAppsNetworkForbided "+forbadeList[i]+" userid:"+userid);
					 }
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
		}
		 /*-prize add by lihuangyuan,for whitelist -2017-05-06-end-*/
		
	}	
	/*--prize-add by lihuangyuan,for skip  com.gangyun.beautysnap --2017-03-22-end-*/
	
	private void readSpData() {
		SharedPreferences wSharePref = mContext.getSharedPreferences(WIFI_PREFS_NAME, 0);
		SharedPreferences gSharePref = mContext.getSharedPreferences(G_PREFS_NAME, 0);
		Map<String, ?> wMap = wSharePref.getAll();
		Map<String, ?> gMap = gSharePref.getAll();
		if (wMap != null || wMap.size() > 0) {
			for (Map.Entry<String, ?> wEntry : wMap.entrySet()) {
				if (!wEntry.getKey().equals("wifi_all") &&
						!Boolean.valueOf(wEntry.getValue().toString())) {
					setNetwork(Integer.parseInt(wEntry.getKey()), 1);
				}
			}
		}
		if (gMap != null || gMap.size() > 0) {
			for (Map.Entry<String, ?> gEntry : gMap.entrySet()) {
				if (!gEntry.getKey().equals("g_all") &&
						!Boolean.valueOf(gEntry.getValue().toString())) {
					setNetwork(Integer.parseInt(gEntry.getKey()), 0);
				}
			}
		}
	}
	
	/**
	*netType: wifi 1, 3g 0
	* true:forbidden network
	*/
	private void setNetwork(int uid, int netType) {
		try {
			INetworkManagementService netMgr = 
				INetworkManagementService.Stub.asInterface(ServiceManager.getService(Context.NETWORKMANAGEMENT_SERVICE));
			netMgr.setFirewallUidChainRule(uid, netType, true);
        } catch (RemoteException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void updateData(Context context,Intent intent) {
		SharedPreferences wSharePref = mContext.getSharedPreferences(WIFI_PREFS_NAME, 0);
		SharedPreferences gSharePref = mContext.getSharedPreferences(G_PREFS_NAME, 0);
		SharedPreferences.Editor wEditor = wSharePref.edit();
		SharedPreferences.Editor gEditor = gSharePref.edit();
		Bundle bundle = intent.getExtras();
		if(bundle != null){
			int uid = bundle.getInt("uid",-1);
			boolean enable = bundle.getBoolean("cellulardata_control_state",false);
			Log.d("PrizeNetControl", "uid == "+uid+"   ******enable ==  "+enable);
			if(uid != -1){
				gEditor.putBoolean(uid + "", enable);
				gEditor.commit();
			}
		}
	}
}