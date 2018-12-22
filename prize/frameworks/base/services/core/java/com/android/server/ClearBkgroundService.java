/************************************************************************
* Copyright (C) 2016, Shenzhen Prize co., LTD
* Name:     ClearBkgroundService.java
* Function: purebackground 
* Version:  2.0
* Author: lihuangyuan
* Data:     2017-11-09
************************************************************************/
package com.android.server;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.app.ActivityManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageDataObserver;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.PackageStats;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.util.ArraySet;
import android.util.Log;
import android.util.Slog;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.app.ActivityManager.MemoryInfo;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.telephony.TelephonyManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.text.format.Formatter;
import com.android.internal.telephony.TelephonyIntents;
import com.android.internal.app.PrizeResetOSActivity;
import com.prize.android.IClearBkgroundService;
import com.mediatek.common.prizeoption.PrizeOption;
/*PRIZE-add for reset vendor app-wangxianzhen-2017-03-20-start*/
import android.os.Bundle;
import android.provider.CallLog;
import android.annotation.SuppressLint;
import android.database.Cursor;
import android.util.ArrayMap;
import java.util.Calendar;
/*PRIZE-add for reset vendor app-wangxianzhen-2017-03-20-end*/
 
import android.os.HandlerThread;
import android.os.Looper;
import com.android.server.power.ShutdownThread;
/**prize-add default open adb -by-zhongweilin-start*/
import com.android.server.NvRAMAgent;
import android.os.ServiceManager;
/**prize-add default open adb -by-zhongweilin-end*/

public class ClearBkgroundService extends IClearBkgroundService.Stub {

    private final static String TAG = "PureBackground";
    private final static String TAG_ADB = "prizeadb";
    private final static boolean DEBUG = "eng".equals(Build.TYPE);
    private final static String ACTION_SCREEN_ON = "android.intent.action.SCREEN_ON";
    private final static String ACTION_SCREEN_OFF = "android.intent.action.SCREEN_OFF"; 


    
    private final static int MSG_SCREEN_ON = 0x01;
    private final static int MSG_SCREEN_OFF = 0x02;   
    private final static int MSG_RESET_OR_REBOOT = 0x07;     
    private final static int MSG_RESET_RESET_BOOT_DATA = 0x10;//reset some data when bootup
    private final static int MSG_SEND_KEEPALIVE_BROADCAST = 0x11;
    private final static int MSG_RESTORE_SUPERPOWER_DATA = 0x12;//restore superpower data


    
    private Context mContext = null;  
    private Calendar cal=null;
    private boolean mIsScreenOff = false;
    private long mScreenOffTime = 0;//the time screen off
	
    //for simchange post reset dialog
    public static final String PRIZE_SIM_ID_LIST = "prizesimid";
    private boolean bSimInsert = false;    

    /*PRIZE-add for reset vendor app-wangxianzhen-2017-03-20-start*/
    private ArrayMap<String, PrizeContactsInfo> mPrizeContactsInfo =  new ArrayMap<String, PrizeContactsInfo>();
    private final static int PRIZE_DIALER_TRACE_COUNT = 10;//dialer 10 count
    private final static int PRIZE_DIALER_TRACE_TIME = 10*60;//dialer 10 minutes    
    /*PRIZE-add for reset vendor app-wangxianzhen-2017-03-20-end*/    
    
	
    private BroadcastReceiver MyBroadcastReceiver = new BroadcastReceiver() 
    {
        @Override
        public void onReceive(Context context, Intent intent) 
        {
            String action = intent.getAction();
            Slog.v(TAG, "ClearBkgroundService onReceive action=" + action );

            if(action.equals(ACTION_SCREEN_ON))
            {            	  
                mHandler.sendEmptyMessageDelayed(MSG_SCREEN_ON, 100);
            }
	     else if(action.equals(ACTION_SCREEN_OFF))
	     { 
                mHandler.sendEmptyMessageDelayed(MSG_SCREEN_OFF, 100);
            }
	     else if(action.equals(TelephonyIntents.ACTION_SIM_STATE_CHANGED))
	     {
                TelephonyManager tm = (TelephonyManager) context.getSystemService(Service.TELEPHONY_SERVICE);
                if(tm == null) 
		  {
                    Log.i(TAG_ADB, "tm is null, return...");
                    return;
                }
                int state = tm.getSimState();
                if(TelephonyManager.SIM_STATE_READY == state)
		  {
                    Settings.System.putInt(mContext.getContentResolver(),"prize.adb.enable",1);//enable adb
                    /**prize-add default open adb -by-zhongweilin-start*/
                    if(PrizeOption.PRIZE_AUTO_TEST){
                        String audios = readProInfo(40);
                        String cameras = readProInfo(39);
                        String mmi = readProInfo(38);
                        Log.e(TAG, "readProInfo() aaaaa ret = " + audios + cameras + mmi);
                        if(audios == null || cameras == null || mmi == null || !audios.equals("P") || !cameras.equals("P") || !mmi.equals("P")){
                            Log.e(TAG, "readProInfo() aaaaa ret = " + "11111");
                            Settings.Global.putInt(context.getContentResolver(), Settings.Global.PACKAGE_VERIFIER_INCLUDE_ADB, 1);
                            Settings.Global.putInt(context.getContentResolver(), Settings.Global.ADB_ENABLED, 1);
                        }

                    }
                    /**prize-add default open adb -by-zhongweilin-end*/
                    
			/*PRIZE-add for changesimcard reset dialog-lihuangyuan-2017-06-15-start*/
			if(PrizeOption.PRIZE_CHANGESIM_RESET_DIALOG)
			{
				mHandler.sendEmptyMessageDelayed(MSG_RESET_OR_REBOOT, 100);
			}
			/*PRIZE-add for changesimcard reset dialog-lihuangyuan-2017-06-15-end*/
                }
            }	     
	     else if(action.equals("com.prize.sim.inserted"))
	     {
                Log.i(TAG_ADB, "ClearBkgroundService com.prize.sim.inserted");
                bSimInsert = true;
                if(!PrizeOption.PRIZE_CHANGESIM_RESET_DIALOG)
                {
                    Log.i(TAG_ADB, "send com.android.sim.inserted");
                    Intent intentsim = new Intent("com.android.sim.inserted");
                    mContext.sendBroadcast(intentsim);
                }
            }	     
	     /*PRIZE-add for reset vendor app-wangxianzhen-2017-03-20-start*/
	     else if(action.equals(Intent.ACTION_TIME_TICK))
	     {
	     	   long curtime = SystemClock.elapsedRealtime();
                Slog.i(TAG, "prize_vendor_app ACTION_TIME_TICK time=" + (curtime - mScreenOffTime));		  
                if(mIsScreenOff && curtime - mScreenOffTime >= 30*60*1000)
		  { //30 minutes
                    int prize_vendor_app_status = Settings.System.getInt(mContext.getContentResolver(),"prize_vendor_app_status",0);
                    Slog.i(TAG, "prize_vendor_app ACTION_TIME_TICK prize_vendor_app_status=" + prize_vendor_app_status);
                    if(0 == prize_vendor_app_status) 
		      {
                        cal=Calendar.getInstance();
                        if((cal != null) &&( 2 <= cal.get(Calendar.HOUR_OF_DAY))  &&  (cal.get(Calendar.HOUR_OF_DAY) <=5))
			   {
                            ReadContactsInfo();
                        }
                    }
                }            
            }
	     /*PRIZE-add for reset vendor app-wangxianzhen-2017-03-20-end*/
	     /*prize -add by lihuangyuan,for whitelist keep alive -2018-01-08-start*/
	     else if(action.equals("com.prize.keepactive.send"))
	     {
	         //send after 30 minutes
	         mHandler.sendEmptyMessageDelayed(MSG_SEND_KEEPALIVE_BROADCAST, 2*60*1000);
	     }
	     /*prize -add by lihuangyuan,for whitelist keep alive -2018-01-08-end*/
	     else
	     {
                Slog.i(TAG, "error");
            }
        }
    };
    /**prize-add default open adb -by-zhongweilin-start*/
    private String readProInfo(int index) {
        IBinder binder = ServiceManager.getService("NvRAMAgent");
        Log.e(TAG, "readProInfo() aaaaa binder = " + binder);
        NvRAMAgent agent = NvRAMAgent.Stub.asInterface(binder);
        Log.e(TAG, "readProInfo() aaaaa agent = " + agent);
        byte[] buff = null;
        try {
            buff = agent.readFileByName("/data/nvram/APCFG/APRDEB/PRODUCT_INFO");
        } catch (Exception e) {
            e.printStackTrace();
        }
        char c=(char)buff[index];
        String sn=new String(buff);
        return String.valueOf((char)buff[index]);
    }
    /**prize-add default open adb -by-zhongweilin-end*/
    
    private HandlerThread mWorkerThread = null;
    private WorkHandler mHandler = null;    
    public class WorkHandler extends Handler
    {
        public WorkHandler(Looper looper)
	 {
		super(looper);
	 }
        public void handleMessage(Message msg) 
        {
            switch (msg.what) {		  
                case MSG_SCREEN_ON: 
		  {
                    Slog.i(TAG, "MSG_SCREEN_ON");
			//stop kill every 15mins
		      mIsScreenOff = false;			
			mScreenOffTime = 0;
                    break;
                }
		  /*prize-add by lihuangyuan kill every 15mins ,2017-08-28-start*/
		  case MSG_SCREEN_OFF:
		  {			   
		  	mIsScreenOff = true;		
			mScreenOffTime = SystemClock.elapsedRealtime(); 			
		   }
		   break;                
		  /*PRIZE-add for changesimcard reset dialog-lihuangyuan-2017-06-15-start*/
                case MSG_RESET_OR_REBOOT: 
		  {
                    if(DEBUG) Slog.i(TAG, "MSG_RESET_OR_REBOOT");
                    showResetOrReboot(mContext);
			break;
                }
		  /*PRIZE-add for changesimcard reset dialog-lihuangyuan-2017-06-15-end*/
		  case MSG_RESET_RESET_BOOT_DATA:
		  	//lihuangyuan,set the reset dialog mode to false
		  	if(PrizeOption.PRIZE_CHANGESIM_RESET_DIALOG)
			{
				Settings.System.putInt(mContext.getContentResolver(),"prizeresetmode",0);
				Slog.i(TAG, "MSG_RESET_RESET_DIALOG_FLAG set to 0");
			}
			//lihuangyuan,set the powerextend mode to false
			{
			    String prop = SystemProperties.get("persist.sys.power_extend_mode");
			    if(prop != null && "true".equals(prop))
			    {
				SystemProperties.set("persist.sys.power_extend_mode", "false");
				//ShutdownThread.quitSuperSaverMode(mContext);
				mHandler.sendEmptyMessageDelayed(MSG_RESTORE_SUPERPOWER_DATA,5*1000);
			    }
			}
		  	break;	
		  case MSG_RESTORE_SUPERPOWER_DATA:
		  	ShutdownThread.quitSuperSaverMode(mContext);
		  	break;
			/*prize -add by lihuangyuan,for whitelist keep alive -2018-01-08-start*/
		  case MSG_SEND_KEEPALIVE_BROADCAST:
		  	{
			    //broadcast for restart the keep alive apps
			    Intent intent = new Intent("com.prize.keepactive.recv");
			    mContext.sendBroadcast(intent);
		  	}
		  	break;
			/*prize -add by lihuangyuan,for whitelist keep alive -2018-01-08-end*/
                default: 
		  {
                    if(DEBUG) Slog.i(TAG, "error msg " + msg.what);
			break;
                }
            }
        }
    }; 

    

    /*kill services*/
    public void amCmdForceStop(String pkgName){
        Slog.i(TAG, "amCmdForceStop " + pkgName);
        try {
            String[] cmdMode = new String[]{"/system/bin/sh","-c","am force-stop " + pkgName};
            //String[] cmdMode = new String[]{"/system/bin/am","force-stop " + pkgName};
            Runtime.getRuntime().exec(cmdMode);
        } catch (IOException e) {
            Slog.i(TAG, "amCmdForceStop e " + e);
            e.printStackTrace();
        }
    }

    ClearBkgroundService(Context context) {
        mContext = context;
        if(DEBUG) Slog.v(TAG, "onCreate()");
	 init();
    }
    public void systemReady()
    {
    	 
    }   
    public void init()
    {
    	 Slog.v(TAG, "init...");
	 /*prize-add by lihuangyuan kill every 15mins ,2017-08-28-start*/
	 mWorkerThread = new HandlerThread("ClearBkgroundService");
	 mWorkerThread.start();
	 mHandler = new WorkHandler(mWorkerThread.getLooper());
	 mHandler.sendEmptyMessage(MSG_RESET_RESET_BOOT_DATA);
	 /*prize-add by lihuangyuan kill every 15mins ,2017-08-28-end*/      

	 // Network connectivity receiver
        IntentFilter MyFilter = new IntentFilter();                
        MyFilter.addAction("com.prize.sim.inserted");
        MyFilter.addAction(TelephonyIntents.ACTION_SIM_STATE_CHANGED);
        /*PRIZE-add for reset vendor app-wangxianzhen-2017-03-20-start*/
        if(PrizeOption.PRIZE_RESET_VENDOR_APP) {
	     MyFilter.addAction(ACTION_SCREEN_ON);
            MyFilter.addAction(ACTION_SCREEN_OFF);
            MyFilter.addAction(Intent.ACTION_TIME_TICK);
        }
        /*PRIZE-add for reset vendor app-wangxianzhen-2017-03-20-end*/
	 /*prize -add by lihuangyuan,for whitelist keep alive -2018-01-08-start*/
	 MyFilter.addAction("com.prize.keepactive.send");
	 /*prize -add by lihuangyuan,for whitelist keep alive -2018-01-08-end*/
        mContext.registerReceiver(MyBroadcastReceiver, MyFilter);
    }

   /////////////////////////////////////////////////////////////////////////  
    

    /////////////////////////////////////////////////////////////////////////
    private void showResetOrReboot(Context context) {
        int neverRestFlag = Settings.System.getInt(mContext.getContentResolver(),"prize_never_reset",0);
        if(0 == neverRestFlag){
            ReadContactsInfo();
        }
        int phoneCount = TelephonyManager.getDefault().getPhoneCount();
        SubscriptionManager mSubscriptionManager = SubscriptionManager.from(context);
        if(mSubscriptionManager == null) {
            Log.i(TAG_ADB, "mSubscriptionManager is null, return...");
            return;
        }
        boolean mShowResetDialog = false;
        boolean isFirstSim = false;
        boolean bExistedBefore = false;
        int simid = 0;
        String simIDList = null;
        String newSimID = null;
        SubscriptionInfo IMSI = null;
        SubscriptionInfo IMSI1 = mSubscriptionManager.getActiveSubscriptionInfoForSimSlotIndex(0);
        SubscriptionInfo IMSI2 = mSubscriptionManager.getActiveSubscriptionInfoForSimSlotIndex(1);
        Log.i("prizeadb", "IMSI1=" + IMSI1);
        Log.i("prizeadb", "IMSI2=" + IMSI2);
        for(int i=0; i<phoneCount; i++) {
            simIDList = Settings.System.getString(context.getContentResolver(), PRIZE_SIM_ID_LIST);
            Log.i(TAG_ADB, "simIDList=" + simIDList + "     newSimID=" + newSimID);

            IMSI = mSubscriptionManager.getActiveSubscriptionInfoForSimSlotIndex(i);
            Log.i("prizeadb", "IMSI[" + i + "]=" + IMSI);
            if((null == IMSI) || (IMSI.equals(""))){
                Log.i(TAG_ADB, "IMSI is null, continue...");
                continue;
            }

            //Log.i("prizeadb", "IMSI[" + i + "]=" + IMSI.getIccId());
            if((simIDList == null) && (IMSI.getIccId() != null)) {
                isFirstSim = true;
                Settings.System.putString(context.getContentResolver(), PRIZE_SIM_ID_LIST, IMSI.getIccId() + ",");
                Log.i(TAG_ADB, "sim id list is null, add " + IMSI.getIccId() + " to db.");
            }
            if((simIDList != null) && (IMSI.getIccId() != null) && (!(simIDList.contains(IMSI.getIccId())))) {
                if(!isFirstSim) {
                    Log.i(TAG_ADB, "001 IMSI.getIccId=" + IMSI.getIccId() + "     newSimID=" + newSimID);
                    if(newSimID == null) {
                        Log.i(TAG_ADB, "newSimID is null, so newSimID=IMSI.getIccId+,");
                        newSimID = IMSI.getIccId() + ",";
                    } else {
                        if(!newSimID.contains(IMSI.getIccId())) {
                            Log.i(TAG_ADB, "newSimID += IMSI.getIccId()");
                            newSimID += IMSI.getIccId() + ",";
                        }
                    }
                    Log.i(TAG_ADB, "002 IMSI.getIccId=" + IMSI.getIccId() + "     newSimID=" + newSimID);
                    mShowResetDialog = true;
                }
                if(!mShowResetDialog){
                    Log.i(TAG_ADB, "sim id list is not null, add " + IMSI.getIccId() + " to db.");
                    Settings.System.putString(context.getContentResolver(), PRIZE_SIM_ID_LIST, simIDList + IMSI.getIccId() + ",");
                }
            }
            if((simIDList != null) && (IMSI.getIccId() != null) && (simIDList.contains(IMSI.getIccId()))) {
                bExistedBefore = true;
                simid = i;
            }
        }
        Log.i(TAG_ADB, "bExistedBefore=" + bExistedBefore + ", simid=" + simid);
        if(bExistedBefore) {
            mShowResetDialog = false;
            if((0 == simid) && (IMSI2 != null) && (IMSI2.getIccId() != null) && (!simIDList.contains(IMSI2.getIccId()))) {
                Log.i(TAG_ADB, "sim1 existed before, add sim2 " + IMSI2.getIccId() + " to db.");
                Settings.System.putString(context.getContentResolver(), PRIZE_SIM_ID_LIST, simIDList + IMSI2.getIccId() + ",");
            } else if((1 == simid) && (IMSI1 != null) && (IMSI1.getIccId() != null) && (!simIDList.contains(IMSI1.getIccId()))){
                Log.i(TAG_ADB, "sim2 existed before, add sim1 " + IMSI1.getIccId() + " to db.");
                Settings.System.putString(context.getContentResolver(), PRIZE_SIM_ID_LIST, simIDList + IMSI1.getIccId() + ",");
            }
        }
        int prizeresetmode = Settings.System.getInt(mContext.getContentResolver(),"prizeresetmode",0);
        neverRestFlag = Settings.System.getInt(mContext.getContentResolver(),"prize_never_reset",0);
        Log.i(TAG_ADB, "mShowResetDialog=" + mShowResetDialog + ", isFirstSim=" + isFirstSim + ", bSimInsert=" + bSimInsert + ", prizeresetmode=" + prizeresetmode
            + ", neverRestFlag=" + neverRestFlag);
        if(mShowResetDialog && (!bExistedBefore) && (0 == prizeresetmode) && (0 == neverRestFlag)) {
            Log.i(TAG_ADB, "send com.prize.reset.action");
            Settings.System.putInt(mContext.getContentResolver(),"prizeresetmode",1);
            Intent resetIntent = new Intent("com.prize.reset.action");
            resetIntent.putExtra("sim_id_list", simIDList);
            resetIntent.putExtra("new_sim_id", newSimID);
            resetIntent.putExtra("sim_insert", bSimInsert);
            resetIntent.setClassName("android",PrizeResetOSActivity.class.getName());
            resetIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            context.startActivityAsUser(resetIntent, UserHandle.CURRENT);
        } else if(/*(!mShowResetDialog) &&*/ bSimInsert)  {
            Log.i(TAG_ADB, "send com.android.sim.inserted");
            Intent intentsim = new Intent("com.android.sim.inserted");
            context.sendBroadcast(intentsim);
        }
        bSimInsert = false;
		if(bExistedBefore && prizeresetmode == 1)
		{
			Settings.System.putInt(mContext.getContentResolver(),"prizeresetmode",0);
		}
    }    
    /*PRIZE-add for reset vendor app-wangxianzhen-2017-03-20-start*/
    //@SuppressLint("SimpleDateFormat")
    private synchronized void ReadContactsInfo() {
        Cursor cs;
        cs=mContext.getContentResolver().query(CallLog.Calls.CONTENT_URI,
            new String[]{"_id",
                CallLog.Calls.NUMBER,
                CallLog.Calls.DURATION,
                CallLog.Calls.PHONE_ACCOUNT_ID,
                CallLog.Calls.PHONE_ACCOUNT_ADDRESS},
                null,null,CallLog.Calls.DEFAULT_SORT_ORDER);
        String callHistoryListStr="";
        if(cs!=null && cs.getCount()>0){
            for(cs.moveToFirst(); !cs.isAfterLast(); cs.moveToNext()){
                String strID = cs.getString(0);
                String callNumber=cs.getString(1);
                int callDuration=Integer.parseInt(cs.getString(2));
                String subid=cs.getString(3);
                String phoneNumber=cs.getString(4);
                String callInfo=strID + " " + callNumber + " " + callDuration + " " + subid + " " + phoneNumber +"\n";
                callHistoryListStr+=callInfo;

                if(mPrizeContactsInfo == null){
                    mPrizeContactsInfo =  new ArrayMap<String, PrizeContactsInfo>();
                }
                if(mPrizeContactsInfo != null){
                    if(!mPrizeContactsInfo.containsKey(subid)){
                        PrizeContactsInfo prizeContactsInfo = new PrizeContactsInfo(subid, 1, callDuration, phoneNumber, strID);
                        mPrizeContactsInfo.put(subid, prizeContactsInfo);
                    } else {
                        if(!mPrizeContactsInfo.get(subid).idList.contains(strID)){
                            mPrizeContactsInfo.get(subid).idList.add(strID);
                            mPrizeContactsInfo.get(subid).phoneDialerCount +=1;
                            mPrizeContactsInfo.get(subid).phoneDialerDuration +=callDuration;
                        }

                        if((mPrizeContactsInfo.get(subid).phoneDialerCount >= PRIZE_DIALER_TRACE_COUNT) ||
                            (mPrizeContactsInfo.get(subid).phoneDialerDuration >= PRIZE_DIALER_TRACE_TIME))
                        {
                                 Slog.i(TAG_ADB, "prize_vendor_app " + mPrizeContactsInfo.get(subid).toString());
					if(PrizeOption.PRIZE_CHANGESIM_RESET_DIALOG)
					{
						Settings.System.putInt(mContext.getContentResolver(),"prize_never_reset",1);
					}
					if(PrizeOption.PRIZE_RESET_VENDOR_APP)
					{
						Settings.System.putInt(mContext.getContentResolver(),"prize_vendor_app_status",1);
					}
                                  break;
                        }
                   }
               }
            }
        }
        Slog.i(TAG_ADB, "prize_vendor_app call history: " + callHistoryListStr);
        if(mPrizeContactsInfo != null){
            for(int i=0; i<mPrizeContactsInfo.size(); i++) {
                Log.i(TAG_ADB, mPrizeContactsInfo.valueAt(i).toString());
            }
        }

        if(cs != null){
            cs.close();
        }
    }

    final class PrizeContactsInfo{
        private final String phoneAccountID;
        private int phoneDialerCount;
        private int phoneDialerDuration;
        private final String phoneNumber;
        private List<String> idList = new ArrayList<String>();

        public PrizeContactsInfo(String phoneAccountID, int phoneDialerCount, int phoneDialerDuration, String phoneNumber, String idList) {
            this.phoneAccountID = phoneAccountID;
            this.phoneDialerCount = phoneDialerCount;
            this.phoneDialerDuration = phoneDialerDuration;
            this.phoneNumber = phoneNumber;
            this.idList.add(idList);
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("PrizeContactsInfo");
            sb.append(", phoneAccountID=" + phoneAccountID);
            sb.append(", phoneDialerCount=" + phoneDialerCount);
            sb.append(", phoneDialerDuration=" + phoneDialerDuration);
            sb.append(", phoneNumber=" + phoneNumber);
            sb.append(", idList=" + idList.toString());
            return sb.toString();
        }
    }
    /*PRIZE-add for reset vendor app-wangxianzhen-2017-03-20-end*/
}
