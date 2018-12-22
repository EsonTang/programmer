package com.android.server;

import java.util.ArrayList;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Handler;
import android.os.IWhiteListChangeListener;
import android.os.IWhiteListService;
import android.os.Message;
import android.os.RemoteException;
import android.os.WakeupItem;
import android.os.WhiteListManager;
import android.provider.Settings;
import android.provider.WhiteListColumns;
import android.util.Log;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.app.AppOpsManager;
import android.os.UserHandle;
import android.view.WindowManager;
import android.os.ServiceManager;
//import com.mediatek.common.mom.IMobileManager;
//import com.mediatek.common.mom.ReceiverRecord;
import android.app.INotificationManager;
import android.os.INetworkManagementService;
import android.os.HandlerThread;
import android.os.Looper;
import android.content.pm.PackageInfo;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import android.os.Environment;
import android.util.Xml;
import com.android.internal.util.XmlUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.IPackageManager;
import android.os.UserHandle;
import android.content.pm.ParceledListSlice;
import android.content.pm.ResolveInfo;
import android.os.Process;

public class WhiteListService extends IWhiteListService.Stub
{
	private static final String TAG = "whitelist";
	private Context mContext;
	public WhiteListService(Context context)
	{
		mContext = context;
		mWorkerThread = new HandlerThread("WhiteListService");
		mWorkerThread.start();
		mHandler = new WorkHandler(mWorkerThread.getLooper());
		mUid = UserHandle.myUserId();
	}
	public void systemReady()
	{
		Log.i(TAG,"systemReady....");
		IntentFilter filter = new IntentFilter();
		filter.addAction(ACTION_DATA_CHANGE);
		mContext.registerReceiver(mReceiver, filter);
		
		mHandler.sendMessageDelayed(mHandler.obtainMessage(DO_DATA_CHANGE, WhiteListManager.WHITELIST_TYPE_MSGWHITE,0),30*1000);
		mHandler.sendMessageDelayed(mHandler.obtainMessage(DO_DATA_CHANGE, WhiteListManager.WHITELIST_TYPE_INSTALLWHITE,0),30*1000);
		mHandler.sendMessageDelayed(mHandler.obtainMessage(DO_DATA_CHANGE, WhiteListManager.WHITELIST_TYPE_DOZEWHITE,0),30*1000);
              mHandler.sendMessageDelayed(mHandler.obtainMessage(DO_DATA_CHANGE, WhiteListManager.WHITELIST_TYPE_PROVIDERWAKEUP,0),30*1000);

		//do callback after systemready delay 30s
		mHandler.sendEmptyMessageDelayed(SYSTEM_READY_DOCALLBACK, 30*1000);
	}
	public static final String ACTION_DATA_CHANGE = "com.prize.whitelist.datachange";
	public static final String KEY_DATA_TYPE = "type";
	private BroadcastReceiver mReceiver = new BroadcastReceiver() 
	{		
		@Override
		public void onReceive(Context context, Intent intent) 
		{			
			if(ACTION_DATA_CHANGE.equals(intent.getAction()))
			{
				int type = intent.getIntExtra(KEY_DATA_TYPE, 0);
				Log.i(TAG,"action:"+ACTION_DATA_CHANGE+",type:"+type);
				mHandler.sendMessage(mHandler.obtainMessage(DO_DATA_CHANGE, type,0));
			}
		}
	};
	private void doDataChange(int type)
	{
		switch(type)
		{
		case WhiteListManager.WHITELIST_TYPE_PUREBKGROUND:
			doPurebackgroundChange();
			break;
		case WhiteListManager.WHITELIST_TYPE_NOTIFICATION:
			doNotificationChange();
			break;
		case WhiteListManager.WHITELIST_TYPE_FLOATWINDOW:
			doFloatWindowChange();
			break;
		case WhiteListManager.WHITELIST_TYPE_AUTOLAUNCH:
			doAutolaunchChange();
			break;
		case WhiteListManager.WHITELIST_TYPE_NETFORBADE:
			doNetForbadeChange();
			break;
		case WhiteListManager.WHITELIST_TYPE_RELEATEWAKEUP:
			doReleateWakeupChange();
			break;
		case WhiteListManager.WHITELIST_TYPE_SLEEPNET:
			doSleepNetChange();
			break;
		case WhiteListManager.WHITELIST_TYPE_BLOCKACTIVITY:
			doBlockActivityChange();
			break;
		case WhiteListManager.WHITELIST_TYPE_MSGWHITE:
			doMessageWhiteChange();
			break;
		case WhiteListManager.WHITELIST_TYPE_INSTALLWHITE:
			doInstallWhiteChange();
			break;	
		case WhiteListManager.WHITELIST_TYPE_DOZEWHITE:
			doDozeWhiteChange();
			break;

              case WhiteListManager.WHITELIST_TYPE_PROVIDERWAKEUP:
			doProviderWakeupChange();
			break;
		}
	}
	public class PkgRecord
	{
		public int    id;
		public String pkgname;
		public int    enable;
	}
	/**
	 * if pkg in hidelist and enable/disable,delete enable/disable
	 * if pkg is deflist and disable,update to enable
	 */
	private void doPurebackgroundChange()
	{
		Log.i(TAG,"doPurebackgroundChange....");
		synchronized (this)
		{
			//get data first time
			if(mPurebackgroundHideList == null)
			{
				mPurebackgroundHideList =  new ArrayList<String>();				
			}
			mPurebackgroundHideList.clear();
			ArrayList<String> hidelist = getPurebackgroundHideListInternal();
			mPurebackgroundHideList.addAll(hidelist);
			Log.i(TAG,"doPurebackgroundChange hidelist size:"+hidelist.size());

			if(mPurebackgroundDefList== null)
			{
				mPurebackgroundDefList =  new ArrayList<String>();				
			}
			mPurebackgroundDefList.clear();
			ArrayList<String> deflist = getPurebackgroundDefListInternal();
			mPurebackgroundDefList.addAll(deflist);
			Log.i(TAG,"doPurebackgroundChange deflist size:"+deflist.size());

			if(mPurebackgroundNotKillList== null)
			{
				mPurebackgroundNotKillList =  new ArrayList<String>();				
			}
			mPurebackgroundNotKillList.clear();
			ArrayList<String> notkilllist = getPurebackgroundNotKillListInternal();
			mPurebackgroundNotKillList.addAll(notkilllist);
			Log.i(TAG,"doPurebackgroundChange notkilllist size:"+notkilllist.size());
		}
		/*Cursor cursor = null;		
		ArrayList<PkgRecord > hidelist = new ArrayList<PkgRecord>();
		ArrayList<PkgRecord > defenablelist = new ArrayList<PkgRecord>();
		ArrayList<PkgRecord> enabledisablelist = new ArrayList<PkgRecord>();
		ArrayList<PkgRecord> notkilllist = new ArrayList<PkgRecord>();
		
		//hidelist
		try
		{
			cursor = mContext.getContentResolver().query(WhiteListColumns.Purebackground.CONTENT_URI, 
					new String[]{WhiteListColumns.BaseColumns._ID,WhiteListColumns.BaseColumns.PKGNAME,WhiteListColumns.BaseColumns.ENABLE},
					WhiteListColumns.BaseColumns.ENABLE+"=?",new String[]{""+WhiteListColumns.HIDE}, null);			
			if(cursor != null)
			{						
				while(cursor.moveToNext())
				{
					PkgRecord record = new PkgRecord();
					record.id = cursor.getInt(0);
					record.pkgname = cursor.getString(1);
					record.enable = cursor.getInt(2);
					hidelist.add(record);
				}
				
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			if(cursor != null)
			{
				cursor.close();
				cursor = null;
			}
		}
		
		//defenablelist
		try
		{
			cursor = mContext.getContentResolver().query(WhiteListColumns.Purebackground.CONTENT_URI, 
					new String[]{WhiteListColumns.BaseColumns._ID,WhiteListColumns.BaseColumns.PKGNAME,WhiteListColumns.BaseColumns.ENABLE},
					WhiteListColumns.BaseColumns.ENABLE+"=?",new String[]{""+WhiteListColumns.DEFENABLE}, null);			
			if(cursor != null)
			{						
				while(cursor.moveToNext())
				{
					PkgRecord record = new PkgRecord();
					record.id = cursor.getInt(0);
					record.pkgname = cursor.getString(1);
					record.enable = cursor.getInt(2);
					defenablelist.add(record);
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			if(cursor != null)
			{
				cursor.close();
				cursor = null;
			}
		}

		
		
		//enable & disable list
		try
		{
			cursor = mContext.getContentResolver().query(WhiteListColumns.Purebackground.CONTENT_URI, 
					new String[]{WhiteListColumns.BaseColumns._ID,WhiteListColumns.BaseColumns.PKGNAME,WhiteListColumns.BaseColumns.ENABLE},
					WhiteListColumns.BaseColumns.ENABLE+"<=?",new String[]{""+WhiteListColumns.ENABLE}, null);			
			if(cursor != null)
			{						
				while(cursor.moveToNext())
				{
					PkgRecord record = new PkgRecord();
					record.id = cursor.getInt(0);
					record.pkgname = cursor.getString(1);
					record.enable = cursor.getInt(2);
					enabledisablelist.add(record);
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			if(cursor != null)
			{
				cursor.close();
				cursor = null;
			}
		}
		
		//delete enabledisable list by hidelist
		for(int i=0;i<hidelist.size();i++)
		{
			PkgRecord record1 = hidelist.get(i);
			for(int j=0;j<enabledisablelist.size();j++)
			{
				PkgRecord record2 = enabledisablelist.get(j);
				if(record2.pkgname != null && record2.pkgname.equals(record1.pkgname))
				{
					record2.enable = WhiteListColumns.HIDE;
					try
					{
						int rows = mContext.getContentResolver().delete(WhiteListColumns.Purebackground.CONTENT_URI, 
								WhiteListColumns.BaseColumns._ID+"=? ",
								new String[]{""+record2.id});
						Log.i(TAG, "delte enabledisable rows:"+rows+",item:"+record2.pkgname+"/"+record2.enable);
					}
					catch(Exception e)
					{
						e.printStackTrace();
					}
				}
			}
		}
		
		//update enabledisable list by defenablelist
		for(int i=0;i<defenablelist.size();i++)
		{
			PkgRecord record1 = defenablelist.get(i);
			for(int j=0;j<enabledisablelist.size();j++)
			{
				PkgRecord record2 = enabledisablelist.get(j);
				if(record2.pkgname != null && record2.pkgname.equals(record1.pkgname)
					&& record2.enable == WhiteListColumns.DISABLE)
				{
					record2.enable = WhiteListColumns.ENABLE;
					try
					{
						ContentValues values = new ContentValues();
						values.put(WhiteListColumns.BaseColumns.ENABLE,WhiteListColumns.ENABLE);
						int ret = mContext.getContentResolver().update(WhiteListColumns.Purebackground.CONTENT_URI, 
								values, WhiteListColumns.BaseColumns._ID+"=?",new String[]{(""+record2.id)});						
						Log.i(TAG, "defenablelist update to enable ret:"+ret+",item:"+record2.pkgname+"/"+record2.enable);
					}
					catch(Exception e)
					{
						e.printStackTrace();
					}
				}
			}
		}
	       //not killlist is some disable app be killed later
		//update enabledisable list by notkilllist
		/*for(int i=0;i<notkilllist.size();i++)
		{
			PkgRecord record1 = notkilllist.get(i);
			for(int j=0;j<enabledisablelist.size();j++)
			{
				PkgRecord record2 = enabledisablelist.get(j);
				if(record2.pkgname != null && record2.pkgname.equals(record1.pkgname)
					&& record2.enable == WhiteListColumns.DISABLE)
				{
					record2.enable = WhiteListColumns.ENABLE;
					try
					{
						ContentValues values = new ContentValues();
						values.put(WhiteListColumns.BaseColumns.ENABLE,WhiteListColumns.ENABLE);
						int ret = mContext.getContentResolver().update(WhiteListColumns.Purebackground.CONTENT_URI, 
								values, WhiteListColumns.BaseColumns._ID+"=?",new String[]{(""+record2.id)});						
						Log.i(TAG, "notkilllist update to enable ret:"+ret+",item:"+record2.pkgname+"/"+record2.enable);
					}
					catch(Exception e)
					{
						e.printStackTrace();
					}
				}
			}
		}*/
		
	}
	
	private Backend mBackend = new Backend();
	public static class Backend {
		
		private INotificationManager sINM;
		public Backend()
		{
			sINM = INotificationManager.Stub.asInterface(ServiceManager.getService(Context.NOTIFICATION_SERVICE));
		}
		public boolean setNotificationsBanned(String pkg, int uid,boolean banned) 
		{
			try {
				sINM.setNotificationsEnabledForPackage(pkg, uid, !banned);
				return true;
			} catch (Exception e) {
				return false;
			}
		}

		public boolean getNotificationsBanned(String pkg, int uid) 
		{
			try {
				boolean enabled = sINM.areNotificationsEnabledForPackage(
						pkg, uid);
				return !enabled;
			} catch (Exception e) {
				return false;
			}
		}
		
	}
	/**
	 * if pkg in hidelist set notification true
	 * if pkg in deflist  set notification true
	 */
	private void doNotificationChange()
	{
		Log.i(TAG,"doNotificationChange....");
		synchronized (this)
		{
			//get data first time
			if(mNotificationHideList == null)
			{
				mNotificationHideList =  new ArrayList<String>();				
			}
			mNotificationHideList.clear();
			ArrayList<String> hidelist = getNotificationHideListInternal();
			mNotificationHideList.addAll(hidelist);
			Log.i(TAG,"doNotificationChange hidelist size:"+mNotificationHideList.size());

			if(mNotificationDefList == null)
			{
				mNotificationDefList =  new ArrayList<String>();				
			}
			mNotificationDefList.clear();
			ArrayList<String> deflist = getNotificationDefListInternal();
			mNotificationDefList.addAll(deflist);

			Log.i(TAG,"doNotificationChange deflist size:"+mNotificationDefList.size());
		}
		/*Cursor cursor = null;
		PackageManager packageManager = mContext.getPackageManager();
		
		String[] hidelist = null;		
		String[] deflist = null;
		try
		{
			hidelist = getNotificationHideList();
			deflist = getNotificationDefList();
		}
		catch(RemoteException e)
		{
			
		}
		if(hidelist != null)
		{
			for(int i=0;i<hidelist.length;i++)
			{
				String pkgname = hidelist[i];
				if(pkgname == null)continue;
				//set notification true
				try
				{
					ApplicationInfo appinfo = packageManager.getApplicationInfo(pkgname, PackageManager.GET_ACTIVITIES);
					if(appinfo  != null)
					{
						Log.i(TAG,"set notification banned false:"+pkgname);
						mBackend.setNotificationsBanned(pkgname, appinfo.uid, false);
					}
				}
				catch(Exception e)
				{						
				}					
			}
		}		
		if(deflist != null)
		{
			for(int i=0;i<deflist.length;i++)
			{
				String pkgname = deflist[i];
				if(pkgname == null)continue;
				//set notification true
				try
				{
					ApplicationInfo appinfo = packageManager.getApplicationInfo(pkgname, PackageManager.GET_ACTIVITIES);
					if(appinfo  != null)
					{
						Log.i(TAG,"set notification banned false:"+pkgname);
						mBackend.setNotificationsBanned(pkgname, appinfo.uid, false);
					}
				}
				catch(Exception e)
				{						
				}					
			}
		}*/
	}
	private boolean checkPemission(String packageName) 
	{		
		int mode = AppOpsManager.MODE_DEFAULT;
		PackageManager pm = mContext.getPackageManager();
		if(null != pm)
		{
			/*boolean permission = (PackageManager.PERMISSION_GRANTED == 
					pm.checkPermission("android.permission.SYSTEM_ALERT_WINDOW",packageName));
			AppOpsManager mAppOpsManager = (AppOpsManager) mContext.getSystemService(Context.APP_OPS_SERVICE);
			try 
			{
				int uid = pm.getPackageUid(packageName, UserHandle.myUserId());
				mode = mAppOpsManager.checkOp(AppOpsManager.OP_SYSTEM_ALERT_WINDOW, uid, packageName);
			}
			catch (NameNotFoundException e) {
			} 
			catch (Exception exp) {
			}
			return permission || mode == AppOpsManager.MODE_ALLOWED;*/
			try {
				PackageInfo info = pm.getPackageInfo(packageName,PackageManager.GET_PERMISSIONS);
				if(info != null && info.requestedPermissions != null)
				{
					for(int i =0;i<info.requestedPermissions.length;i++)
					{
						if("android.permission.SYSTEM_ALERT_WINDOW".equals(info.requestedPermissions[i]))
						{
							return true;
						}
					}
				}
			} catch (Exception exp) {
			}
		}
		return false;
	}
	/**
	 * if new enable ,set enable
	 */
	private void doFloatWindowChange()
	{
		Log.i(TAG,"doFloatWindowChange...");
		String[] enablelist = null;
		try
		{
			enablelist = getFloatDefList();
		}
		catch(RemoteException e){			
		}
		WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
		if(enablelist == null || wm == null)
		{
			Log.i(TAG,"enablelist or wm is null wm:"+wm);
			return;
		}
		
		for(int i=0; i<enablelist.length; i++)
		{
			String pkgname = enablelist[i];
			if(pkgname == null)continue;
			if(checkPemission(pkgname))
			{
				Log.i(TAG,"setfloatenable :"+pkgname);
				wm.setFloatEnable(pkgname,true);
			}
		}
	}
	
	public static boolean isInList(String pkgname,String []arylist)
	{
	   	if(arylist == null)return false;
		for(int i=0;i<arylist.length;i++)
		{
			if(pkgname.equals(arylist[i]))
			{
				return true;
			}
		}
		return false;
	}
	/////////////////////////////////////////////////////////////////////////////
	private static final String FILE_DIR = "data/com.mediatek.security";
       private static final String FILE_NAME = "bootreceiver.xml";
	private static final String PKG_TAG = "pkg";
	private int mUid;
	private ArrayList<String> loadAutoLaunchDenyListFromFile() 
	{
           Log.d(TAG, "loadAutoLaunchDenyListFromFile()");
	    File dataDir = Environment.getDataDirectory();
           File systemDir = new File(dataDir, FILE_DIR);
           File mFile = new File(systemDir, FILE_NAME);
	    
		
          ArrayList<String> results = new ArrayList<String>();   
          
          FileInputStream stream = null;
          boolean success = false;
          try 
         {
            stream = new FileInputStream(mFile);
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(stream, null);
            int type;
            while ((type = parser.next()) != XmlPullParser.START_TAG
                    && type != XmlPullParser.END_DOCUMENT) 
            {
                ;
            }

            if (type != XmlPullParser.START_TAG) {
		Log.e(TAG, "loadAutoLaunchDenyListFromFile can not find "+XmlPullParser.START_TAG);
                return results;
            }

            int outerDepth = parser.getDepth();
            while ((type = parser.next()) != XmlPullParser.END_DOCUMENT
                    && (type != XmlPullParser.END_TAG || parser.getDepth() > outerDepth)) 
            {
                if (type == XmlPullParser.END_TAG
                        || type == XmlPullParser.TEXT) 
                {
                    continue;
                }
                String tagName = parser.getName();
                if (tagName.equals(PKG_TAG)) 
	      {
                    String pkgName = parser.getAttributeValue(null, "n");
                    int userId = Integer.parseInt(parser.getAttributeValue(null, "u"));
                    boolean enabled = Boolean.parseBoolean(parser.getAttributeValue(null, "e"));
                    if (enabled == true) {
                        Log.e(TAG,"get autolaunch from file "+pkgName+" enabled:"+enabled);
                    }
                    Log.d(TAG, "Read package name: " + pkgName
                            + " enabled: " + enabled + " at User(" + userId
                            + ")");
                    if (mUid == userId) {
                        results.add(pkgName);
                    }
                } else {
                    Log.w(TAG, "Unknown element under <boot-receiver>: "
                            + parser.getName());
                    XmlUtils.skipCurrentTag(parser);
                }
            }
            success = true;
        } catch (IllegalStateException e) {
            Log.w(TAG, "Failed parsing " + e);
        } catch (NullPointerException e) {
            Log.w(TAG, "Failed parsing " + e);
        } catch (NumberFormatException e) {
            Log.w(TAG, "Failed parsing " + e);
        } catch (XmlPullParserException e) {
            Log.w(TAG, "Failed parsing " + e);
        } catch (FileNotFoundException e) {
            Log.i(TAG, "No existing " + mFile + "; starting empty");
        } catch (IOException e) {
            Log.w(TAG, "Failed parsing " + e);
        } catch (IndexOutOfBoundsException e) {
            Log.w(TAG, "Failed parsing " + e);
        } finally {
            try {
                if (stream != null) {
                    stream.close();
                }
            } catch (IOException e) {
                Log.e(TAG,"Fail to read receiver list");
            }
        }
        
        return results;
    }

    public void saveAutolaunchDenyToFile(ArrayList<String> denyList) 
    {
        Log.d(TAG, "saveAutolaunchDenyToFile");
	 File dataDir = Environment.getDataDirectory();
        File systemDir = new File(dataDir, FILE_DIR);
        File mFile = new File(systemDir, FILE_NAME);        
		
        if (mFile.exists() && (!mFile.getAbsoluteFile().delete())) 
	 {
            Log.e(TAG, "saveAutolaunchDenyToFile, deleteFile failed");
        }

        if (denyList.size() == 0) 
	 {
            Log.d(TAG, "saveAutolaunchDenyToFile, size = 0");
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
            for (String name : denyList) 
	     {
                Log.d(TAG, "saveToFile, tag = " + name);
                xml.startTag(null, PKG_TAG);
                xml.attribute(null, "n", name);
                xml.attribute(null, "u", String.valueOf(mUid));
                xml.attribute(null, "e", "false");
                xml.endTag(null, PKG_TAG);
            }
            xml.endTag(null, "bootlist");
            xml.endDocument();
        } 
	 catch (IOException | IllegalArgumentException | IllegalStateException e) 
        {
            Log.e(TAG, "saveAutolaunchDenyToFile, exception + " + e);
            e.printStackTrace();
        } 
	 finally 
	 {
              if (os != null) 
		{
                    try {
                        os.close();
                    } 
			catch (IOException e) {
                        e.printStackTrace();
                    }
                }
        }              
    }
	private ArrayList<String> getBootReceivers(Context context) 
	{
	        ArrayList<String> bootReceivers = new ArrayList<String>();
	        String[] policy = context.getResources().getStringArray(com.mediatek.internal.R.array.config_auto_boot_policy_intent_list);
	        Log.d(TAG, "getBootReceivers, policy:" + policy);
	        IPackageManager pm = IPackageManager.Stub.asInterface(ServiceManager.getService("package"));

	        for (int i = 0; i < policy.length; i++) 
		 {
	            Intent intent = new Intent(policy[i]);
	            try 
		     {
	                ParceledListSlice<ResolveInfo> parlist = pm.queryIntentReceivers(intent, 
						intent.resolveTypeIfNeeded(context.getContentResolver()),
	                                PackageManager.GET_META_DATA, mUid);
	                if (parlist != null) 
			  {
	                    List<ResolveInfo> receivers = parlist.getList();
	                    if (receivers != null) 
			      {
	                        for (int j = 0; j < receivers.size(); j++) 
				   {
	                            ResolveInfo info = receivers.get(j);
	                            String packageName = (info.activityInfo != null) ? info.activityInfo.packageName : null;
	                            ApplicationInfo appinfo = pm.getApplicationInfo(packageName, 0, mUid);
	                            if (isSystemApp(appinfo)) 
					{
	                                continue;
	                            }
	                            if (packageName != null  && !bootReceivers.contains(packageName)) 
					{
	                                Log.d(TAG, "getBootReceivers, packageName:"  + packageName);
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
	public static boolean isSystemApp(ApplicationInfo applicationInfo) 
       {
	        if (applicationInfo != null) 
		{
	            int appId = UserHandle.getAppId(applicationInfo.uid);
	            return ((applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) || (appId == Process.SYSTEM_UID);
	        }
		 else 
		 {	            
	            return false;
	        }
       }
	/**
	 * if enable,reset autolaunch true
	 */
	private void doAutolaunchChange()
	{
		Log.i(TAG,"doAutolaunchChange....");
		synchronized (this)
		{
			//get data first time
			if(mAutoLaunchDefList == null)
			{
				mAutoLaunchDefList =  new ArrayList<String>();				
			}
			mAutoLaunchDefList.clear();
			ArrayList<String> deflist = getAutoLaunchDefListInternal();
			mAutoLaunchDefList.addAll(deflist);

			Log.i(TAG,"doAutolaunchChange deflist size:"+mAutoLaunchDefList.size());
		}
		/*String[] deflist = null;		
		
		try
		{
			deflist = getAutoLaunchDefList();
		}
		catch(RemoteException e)
		{
			
		}
		ArrayList<String> bootrecvlist = getBootReceivers(mContext);
		if(deflist == null ||bootrecvlist == null)
		{
			Log.i(TAG,"doAutolaunchChange error");
			return;
		}
		ArrayList<String> autolaunchlist = new ArrayList<String>();
		ArrayList<String> denylist = loadAutoLaunchDenyListFromFile();
		
		for(int i=0;i<bootrecvlist.size();i++)
		{
			String pkg = bootrecvlist.get(i);
			if(isInList(pkg,deflist))//can auto launch
			{
				if(denylist.contains(pkg))
				{
					Log.i(TAG,"autolaunch remove from denylist:"+pkg);
					denylist.remove(pkg);
				}
			}
			else
			{
				if(!denylist.contains(pkg))
				{
					Log.i(TAG,"autolaunch add to denylist:"+pkg);
					denylist.add(pkg);
				}
			}
		}

		saveAutolaunchDenyToFile(denylist);	*/	
	}
	//////////////////////////////////////////////////////////////////////////////////
	/**
	*netType: wifi 1, 3g 0
	* true:forbidden network
	*/
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
	/**
	 * reset forbade pkg
	 */
	private void doNetForbadeChange()
	{
		Log.i(TAG,"doNetForbadeChange....");
		String [] forbadeList = null;
		try
		{
			forbadeList = getNetForbadeList();
		}
		catch(RemoteException e)
		{
			
		}
		PackageManager packageManager = mContext.getPackageManager();
		if(forbadeList != null)
		{
			for(int i=0;i<forbadeList.length;i++)
			{
				String packageName = forbadeList[i];
				if(packageName == null)continue;
				try
				{
					ApplicationInfo appinfo = packageManager.getApplicationInfo(packageName, PackageManager.GET_ACTIVITIES);
					 if(appinfo != null)
					 {
						 int userid = appinfo.uid;
						 setNetwork(userid,0);
						 setNetwork(userid,1);
						 Log.i(TAG,"setNetworkForbided "+forbadeList[i]+" userid:"+userid);
					 }
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
		}
	}
	/**
	 * call listener
	 */
	private void doReleateWakeupChange()
	{
		Log.i(TAG,"doReleateWakeupChange....");
		synchronized (mListenerList) 
		{
			for(int i=0;i<mListenerList.size();i++)
			{
				WhiteListListener listener = mListenerList.get(i);
				if(listener.type == WhiteListManager.WHITELIST_TYPE_RELEATEWAKEUP)
				{
					/*try
					{
						listener.listener.onWakeupListChange();
					}
					catch(RemoteException e)
					{
						e.printStackTrace();
					}*/
					doCallbackListener(listener);
				}
			}
		}		
	}
	/**
	 * call listener
	 */
	private void doSleepNetChange()
	{
		Log.i(TAG,"doSleepNetChange....");
		synchronized (mListenerList) 
		{
			for(int i=0;i<mListenerList.size();i++)
			{
				WhiteListListener listener = mListenerList.get(i);
				if(listener.type == WhiteListManager.WHITELIST_TYPE_SLEEPNET)
				{
					/*try
					{
						listener.listener.onSleepNetListChange();
					}
					catch(RemoteException e)
					{
						e.printStackTrace();
					}*/
					doCallbackListener(listener);
				}
			}
		}	
	}
	/**
	 * call listener
	 */
	private void doBlockActivityChange()
	{
		Log.i(TAG,"doBlockActivityChange....");
		synchronized (mListenerList) 
		{
			for(int i=0;i<mListenerList.size();i++)
			{
				WhiteListListener listener = mListenerList.get(i);
				if(listener.type == WhiteListManager.WHITELIST_TYPE_BLOCKACTIVITY)
				{
					/*try
					{
						listener.listener.onBlockActivityListChange();
					}
					catch(RemoteException e)
					{
						e.printStackTrace();
					}*/
					doCallbackListener(listener);
				}
			}
		}
	}
	/*
	*save to android.provider.Settings.System "key_default_sendmsg"
	*/
	private void doMessageWhiteChange()
	{
		Log.i(TAG,"doMessageWhiteChange....");
		String [] msgwhitelist = null;
		try
		{
			msgwhitelist = getMsgWhiteList();
		}
		catch(RemoteException e)
		{
			
		}
		if(msgwhitelist != null)
		{
			String str = "";
			for(int i=0;i<msgwhitelist.length;i++)
			{
				if(msgwhitelist[i] != null)
				{
					if(str.contains(msgwhitelist[i]))continue;
					if(i==0)
					{
						str += msgwhitelist[i];
					}
					else
					{
						str += ";"+msgwhitelist[i];
					}
				}
			}
			Log.i(TAG,"msgwhitelist:"+str);
			if(str.length() > 0)
			{
				Settings.System.putString(mContext.getContentResolver(),"key_default_sendmsg", str);
			}
		}
	}
	/*
	*save to android.provider.Settings.System "key_default_installed"
	*/
	private void doInstallWhiteChange()
	{
		Log.i(TAG,"doInstallWhiteChange....");
		String [] installList = null;
		try
		{
			installList = getInstallWhiteList();
		}
		catch(RemoteException e)
		{
			
		}
		if(installList != null)
		{
			String str = "";
			for(int i=0;i<installList.length;i++)
			{
				if(installList[i] != null)
				{
					if(str.contains(installList[i]))continue;
					if(i==0)
					{
						str += installList[i];
					}
					else
					{
						str += ";"+installList[i];
					}
				}
			}
			Log.i(TAG,"installList:"+str);
			if(str.length() > 0)
			{
				Settings.System.putString(mContext.getContentResolver(),"key_default_installed", str);
			}
		}
	}
	private void doDozeWhiteChange()
	{
		Log.i(TAG,"doDozeWhiteChange....");
		String [] dozeList = null;
		try
		{
			dozeList = getDozeWhiteList();
		}
		catch(RemoteException e)
		{
			
		}
		if(dozeList != null)
		{
			String str = "";
			for(int i=0;i<dozeList.length;i++)
			{
				if(dozeList[i] != null)
				{
					if(str.contains(dozeList[i]))continue;
					if(i==0)
					{
						str += dozeList[i];
					}
					else
					{
						str += ":"+dozeList[i];
					}
				}
			}
			Log.i(TAG,"dozeList:"+str);
			if(str.length() > 0)
			{
				Settings.System.putString(mContext.getContentResolver(),"prize_doze_white_list", str);
			}
		}
	}
       /**
	 * call listener
	 */
	private void doProviderWakeupChange()
	{
		Log.i(TAG,"doProviderWakeupChange....");
		synchronized (mListenerList) 
		{
			for(int i=0;i<mListenerList.size();i++)
			{
				WhiteListListener listener = mListenerList.get(i);
				if(listener.type == WhiteListManager.WHITELIST_TYPE_PROVIDERWAKEUP)
				{
					/*try
					{
						listener.listener.onWakeupListChange();
					}
					catch(RemoteException e)
					{
						e.printStackTrace();
					}*/
					doCallbackListener(listener);
				}
			}
		}		
	}
	public static final int DO_CALLBACK = 1;
	public static final int DO_DATA_CHANGE = 2;
	public static final int SYSTEM_READY_DOCALLBACK = 3;
	private HandlerThread mWorkerThread = null;
	private WorkHandler mHandler = null;
	public class WorkHandler extends Handler
	{
		public WorkHandler(Looper looper)
		{
			super(looper);
		}
		@Override
		public void handleMessage(Message msg) {
			switch(msg.what)
			{
			case DO_CALLBACK:
				{
					WhiteListListener listener = (WhiteListListener)msg.obj;
					doCallbackListener(listener);
				}
				break;
			case DO_DATA_CHANGE:
				{
					int type = msg.arg1;
					doDataChange(type);
				}
				break;
			case SYSTEM_READY_DOCALLBACK:
				doListenerCallback();
				break;
			}
		}
		
	};


	private ArrayList<String> mPurebackgroundHideList;
	private ArrayList<String> mPurebackgroundDefList;
	private ArrayList<String> mPurebackgroundNotKillList;
	private  ArrayList<String> getPurebackgroundHideListInternal()
	{
		Cursor cursor = null;
		ArrayList<String > hidelist = new ArrayList<String>();
		try
		{
			cursor = mContext.getContentResolver().query(WhiteListColumns.Purebackground.CONTENT_URI, 
					new String[]{WhiteListColumns.BaseColumns.PKGNAME},
					WhiteListColumns.BaseColumns.ENABLE+"=? ",
					new String[]{""+WhiteListColumns.HIDE}, null);			
			if(cursor != null)
			{						
				while(cursor.moveToNext())
				{
					String pkgname = cursor.getString(0);
					hidelist.add(pkgname);
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			if(cursor != null)
			{
				cursor.close();
				cursor = null;
			}
		}
		return hidelist;
	}
	@Override
	public String[] getPurebackgroundHideList() throws RemoteException {
		
		String [] ret = null;
		synchronized (this)
		{
			//get data first time
			if(mPurebackgroundHideList == null)
			{
				mPurebackgroundHideList =  new ArrayList<String>();
				ArrayList<String> hidelist = getPurebackgroundHideListInternal();
				mPurebackgroundHideList.addAll(hidelist);
			}
			
			if(mPurebackgroundHideList.size() > 0)
			{
				Log.i(TAG,"getPurebackgroundHideList count = "+mPurebackgroundHideList.size());
				ret = new String[mPurebackgroundHideList.size()];
				mPurebackgroundHideList.toArray(ret);
				return ret;
			}
		}
		return null;
	}

	@Override
	public boolean isPurebackgroundHide(String pkgname) throws RemoteException {
		synchronized (this)
		{
			//get data first time
			if(mPurebackgroundHideList == null)
			{
				mPurebackgroundHideList =  new ArrayList<String>();
				ArrayList<String> hidelist = getPurebackgroundHideListInternal();
				mPurebackgroundHideList.addAll(hidelist);
			}
			if(mPurebackgroundHideList.contains(pkgname))
			{
				return true;
			}
		}
		return false;
	}
	private ArrayList<String> getPurebackgroundDefListInternal()
	{
		Cursor cursor = null;
		String [] ret = null;
		ArrayList<String > deflist = new ArrayList<String>();
		try
		{
			cursor = mContext.getContentResolver().query(WhiteListColumns.Purebackground.CONTENT_URI, 
					new String[]{WhiteListColumns.BaseColumns.PKGNAME},
					WhiteListColumns.BaseColumns.ENABLE+"=? ",
					new String[]{""+WhiteListColumns.DEFENABLE}, null);

			if(cursor != null)
			{				
				while(cursor.moveToNext())
				{
					String pkgname = cursor.getString(0);					
					deflist.add(pkgname);
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			if(cursor != null)
			{
				cursor.close();
				cursor = null;
			}
		}
		return deflist;
	}
	@Override
	public String[] getPurebackgroundDefList() throws RemoteException {
		String [] ret = null;
		synchronized (this)
		{
			//get data first time
			if(mPurebackgroundDefList == null)
			{
				mPurebackgroundDefList =  new ArrayList<String>();
				ArrayList<String> deflist = getPurebackgroundDefListInternal();
				mPurebackgroundDefList.addAll(deflist);
			}
			if(mPurebackgroundDefList.size() > 0)
			{
				Log.i(TAG,"getPurebackgroundDefList count:"+mPurebackgroundDefList.size());
				ret = new String[mPurebackgroundDefList.size()];
				mPurebackgroundDefList.toArray(ret);
				return ret;
			}
		}
		return null;
	}

	@Override
	public boolean isPurebackgroundDef(String pkgname) throws RemoteException {
		synchronized (this)
		{
			//get data first time
			if(mPurebackgroundDefList == null)
			{
				mPurebackgroundDefList =  new ArrayList<String>();
				ArrayList<String> hidelist = getPurebackgroundDefListInternal();
				mPurebackgroundDefList.addAll(hidelist);
			}
			if(mPurebackgroundDefList.contains(pkgname))
			{
				return true;
			}
		}
		return false;
	}
	private  ArrayList<String> getPurebackgroundNotKillListInternal()
	{
		Cursor cursor = null;
		ArrayList<String > recordlist = new ArrayList<String>();
		try
		{
			cursor = mContext.getContentResolver().query(WhiteListColumns.Purebackground.CONTENT_URI, 
					new String[]{WhiteListColumns.BaseColumns.PKGNAME},
					WhiteListColumns.BaseColumns.ENABLE+"=? ",
					new String[]{""+WhiteListColumns.NOTKILL}, null);			
			if(cursor != null)
			{						
				while(cursor.moveToNext())
				{
					String pkgname = cursor.getString(0);
					recordlist.add(pkgname);
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			if(cursor != null)
			{
				cursor.close();
				cursor = null;
			}
		}
		return recordlist;
	}
	@Override
	public String[] getPurebackgroundNotKillList() throws RemoteException {
		String [] ret = null;
		synchronized (this)
		{
			//get data first time
			if(mPurebackgroundNotKillList == null)
			{
				mPurebackgroundNotKillList =  new ArrayList<String>();
				ArrayList<String> hidelist = getPurebackgroundNotKillListInternal();
				mPurebackgroundNotKillList.addAll(hidelist);
			}
			
			if(mPurebackgroundNotKillList.size() > 0)
			{
				Log.i(TAG,"getPurebackgroundNotKillList count:"+mPurebackgroundNotKillList.size());
				ret = new String[mPurebackgroundNotKillList.size()];
				mPurebackgroundNotKillList.toArray(ret);
				return ret;
			}
		}
		return null;
	}

	@Override
	public String[] getPurbackgroundEnableList() throws RemoteException {
		Cursor cursor = null;
		String [] ret = null;
		ArrayList<String > enablelist = new ArrayList<String>();
		try
		{
			cursor = mContext.getContentResolver().query(WhiteListColumns.Purebackground.CONTENT_URI, 
					new String[]{WhiteListColumns.BaseColumns.PKGNAME},
					WhiteListColumns.BaseColumns.ENABLE+"=?",new String[]{""+WhiteListColumns.ENABLE}, null);

			if(cursor != null)
			{				
				while(cursor.moveToNext())
				{
					String pkgname = cursor.getString(0);					
					enablelist.add(pkgname);
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			if(cursor != null)
			{
				cursor.close();
				cursor = null;
			}
		}
		
		if(enablelist.size() > 0)
		{
			Log.i(TAG,"getPurbackgroundEnableList count:"+enablelist.size());
			ret = new String[enablelist.size()];
			enablelist.toArray(ret);
			return ret;
		}
		return null;
	}

	@Override
	public String[] getPuregackgroundDisableList() throws RemoteException {
		Cursor cursor = null;
		String [] ret = null;
		ArrayList<String > disablelist = new ArrayList<String>();
		try
		{
			cursor = mContext.getContentResolver().query(WhiteListColumns.Purebackground.CONTENT_URI, 
					new String[]{WhiteListColumns.BaseColumns.PKGNAME},
					WhiteListColumns.BaseColumns.ENABLE+"=?",new String[]{""+WhiteListColumns.DISABLE}, null);
			
			if(cursor != null)
			{				
				while(cursor.moveToNext())
				{
					String pkgname = cursor.getString(0);					
					disablelist.add(pkgname);
				}			
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			if(cursor != null)
			{
				cursor.close();
				cursor = null;
			}
		}
		if(disablelist.size() > 0)
		{
			Log.i(TAG,"getPuregackgroundDisableList count:"+disablelist.size());
			ret = new String[disablelist.size()];
			disablelist.toArray(ret);
			return ret;
		}
		return null;
	}

	private ArrayList<String> mNotificationHideList;
	private ArrayList<String> mNotificationDefList;
	private ArrayList<String> getNotificationHideListInternal()
	{
		Cursor cursor = null;
		ArrayList<String > hidelist = new ArrayList<String>();
		try
		{
			cursor = mContext.getContentResolver().query(WhiteListColumns.Notification.CONTENT_URI, 
					new String[]{WhiteListColumns.BaseColumns.PKGNAME/*,WhiteListColumns.BaseColumns.ENABLE*/},
					WhiteListColumns.BaseColumns.ENABLE+"=?",new String[]{""+WhiteListColumns.HIDE}, null);
			if(cursor != null)
			{							
				while(cursor.moveToNext())
				{
					String pkgname = cursor.getString(0);
					hidelist.add(pkgname);
				}			
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			if(cursor != null)
			{
				cursor.close();
				cursor = null;
			}
		}
		return hidelist;
	}
	@Override
	public String[] getNotificationHideList() throws RemoteException {		
		String [] ret = null;
		synchronized (this)
		{
			//get data first time
			if(mNotificationHideList == null)
			{
				mNotificationHideList =  new ArrayList<String>();
				ArrayList<String> hidelist = getNotificationHideListInternal();
				mNotificationHideList.addAll(hidelist);
			}
			if(mNotificationHideList.size() > 0)
			{
				Log.i(TAG,"getNotificationHideList count = "+mNotificationHideList.size());
				ret = new String[mNotificationHideList.size()];
				mNotificationHideList.toArray(ret);
				return ret;
			}
		}
		return null;
	}

	@Override
	public boolean isNotificationHide(String pkgname) throws RemoteException {
		synchronized (this)
		{
			//get data first time
			if(mNotificationHideList == null)
			{
				mNotificationHideList =  new ArrayList<String>();
				ArrayList<String> hidelist = getNotificationHideListInternal();
				mNotificationHideList.addAll(hidelist);
			}
			if(mNotificationHideList.contains(pkgname))
			{
				return true;
			}
		}
		return false;
	}
	private ArrayList<String> getNotificationDefListInternal()
	{
		Cursor cursor = null;
		String [] ret = null;
		ArrayList<String > deflist = new ArrayList<String>();
		try
		{
			cursor = mContext.getContentResolver().query(WhiteListColumns.Notification.CONTENT_URI, 
					new String[]{WhiteListColumns.BaseColumns.PKGNAME},
					WhiteListColumns.BaseColumns.ENABLE+"=?",new String[]{""+WhiteListColumns.DEFENABLE}, null);
			if(cursor != null)
			{							
				while(cursor.moveToNext())
				{
					String pkgname = cursor.getString(0);
					deflist.add(pkgname);
				}				
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			if(cursor != null)
			{
				cursor.close();
				cursor = null;
			}
		}
		return deflist;
	}
	@Override
	public String[] getNotificationDefList() throws RemoteException {
		String [] ret = null;
		synchronized (this)
		{
			//get data first time
			if(mNotificationDefList == null)
			{
				mNotificationDefList =  new ArrayList<String>();
				ArrayList<String> deflist = getNotificationDefListInternal();
				mNotificationDefList.addAll(deflist);
			}
			if(mNotificationDefList.size() > 0)
			{
				Log.i(TAG,"getNotificationDefList count = "+mNotificationDefList.size());
				ret = new String[mNotificationDefList.size()];
				mNotificationDefList.toArray(ret);
				return ret;
			}
		}
		return null;
	}

	@Override
	public boolean isNotificationDef(String pkgname) throws RemoteException {
		synchronized (this)
		{
			//get data first time
			if(mNotificationDefList == null)
			{
				mNotificationDefList =  new ArrayList<String>();
				ArrayList<String> deflist = getNotificationDefListInternal();
				mNotificationDefList.addAll(deflist);
			}
			if(mNotificationDefList.contains(pkgname))
			{
				return true;
			}
		}
		return false;
	}

	@Override
	public String[] getFloatDefList() throws RemoteException {
		Cursor cursor = null;
		String [] ret = null;
		ArrayList<String > enablelist = new ArrayList<String>();
		try
		{
			cursor = mContext.getContentResolver().query(WhiteListColumns.FloatWindow.CONTENT_URI, 
					new String[]{WhiteListColumns.BaseColumns.PKGNAME},					
					WhiteListColumns.BaseColumns.ENABLE+"=?",new String[]{""+WhiteListColumns.ENABLE}, null);
			if(cursor != null)
			{							
				while(cursor.moveToNext())
				{
					String pkgname = cursor.getString(0);
					enablelist.add(pkgname);
				}				
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			if(cursor != null)
			{
				cursor.close();
				cursor = null;
			}
		}
		
		if(enablelist.size() > 0)
		{
			Log.i(TAG,"getFloatDefList count = "+enablelist.size());
			ret = new String[enablelist.size()];
			enablelist.toArray(ret);
			return ret;
		}
		return null;
	}

	@Override
	public boolean isFloatEnable(String pkgname) throws RemoteException {
		Cursor cursor = null;
		boolean ret = false;		
		try
		{
			cursor = mContext.getContentResolver().query(WhiteListColumns.FloatWindow.CONTENT_URI, 
					new String[]{WhiteListColumns.BaseColumns._ID},
					WhiteListColumns.BaseColumns.PKGNAME+"=? AND "+WhiteListColumns.BaseColumns.ENABLE+"=? ",
					new String[]{pkgname,""+WhiteListColumns.ENABLE}, null);
			if(cursor != null)
			{		
				if(cursor.getCount() > 0)
				{
					ret = true;
				}					
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}	
		finally
		{
			if(cursor != null)
			{
				cursor.close();
				cursor = null;
			}
		}
		return ret;
	}
	private ArrayList<String> mAutoLaunchDefList;
	private ArrayList<String> getAutoLaunchDefListInternal()
	{
		Cursor cursor = null;		
		ArrayList<String > deflist = new ArrayList<String>();
		try
		{
			cursor = mContext.getContentResolver().query(WhiteListColumns.AutoLaunch.CONTENT_URI, 
					new String[]{WhiteListColumns.BaseColumns.PKGNAME},
					WhiteListColumns.BaseColumns.ENABLE+"=?",new String[]{""+WhiteListColumns.DEFENABLE}, null);
			if(cursor != null)
			{							
				while(cursor.moveToNext())
				{
					String pkgname = cursor.getString(0);
					deflist.add(pkgname);
				}			
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			if(cursor != null)
			{
				cursor.close();
				cursor = null;
			}
		}
		return deflist;
	}
	@Override
	public String[] getAutoLaunchDefList() throws RemoteException {
		
		String [] ret = null;
		synchronized (this)
		{
			//get data first time
			if(mAutoLaunchDefList == null)
			{
				mAutoLaunchDefList =  new ArrayList<String>();
				ArrayList<String> deflist = getAutoLaunchDefListInternal();
				mAutoLaunchDefList.addAll(deflist);
			}
			if(mAutoLaunchDefList.size() > 0)
			{
				Log.i(TAG,"getAutoLaunchDefList count = "+mAutoLaunchDefList.size());
				ret = new String[mAutoLaunchDefList.size()];
				mAutoLaunchDefList.toArray(ret);
				return ret;
			}
		}
		return null;
	}

	@Override
	public boolean isAutoLaunchDef(String pkgname) throws RemoteException {
		synchronized (this)
		{
			//get data first time
			if(mAutoLaunchDefList == null)
			{
				mAutoLaunchDefList =  new ArrayList<String>();
				ArrayList<String> deflist = getAutoLaunchDefListInternal();
				mAutoLaunchDefList.addAll(deflist);
			}
			if(mAutoLaunchDefList.contains(pkgname))
			{
				return true;
			}
		}
		return false;
	}

	@Override
	public String[] getNetForbadeList() throws RemoteException {
		Cursor cursor = null;
		String [] ret = null;
		ArrayList<String > forbadelist = new ArrayList<String>();
		try
		{
			cursor = mContext.getContentResolver().query(WhiteListColumns.NetForbade.CONTENT_URI, 
					new String[]{WhiteListColumns.BaseColumns.PKGNAME/*,WhiteListColumns.BaseColumns.ENABLE*/},
					WhiteListColumns.BaseColumns.ENABLE+"=?",new String[]{""+WhiteListColumns.DISABLE}, null);
			if(cursor != null)
			{							
				while(cursor.moveToNext())
				{
					String pkgname = cursor.getString(0);
					forbadelist.add(pkgname);
				}				
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			if(cursor != null)
			{
				cursor.close();
				cursor = null;
			}
		}
		
		if(forbadelist.size() > 0)
		{
			Log.i(TAG,"getNetForbadeList count = "+forbadelist.size());
			ret = new String[forbadelist.size()];
			forbadelist.toArray(ret);
			return ret;
		}
		return null;
	}

	@Override
	public WakeupItem[] getWakeupList() throws RemoteException {
		ArrayList<WakeupItem> wakeuplist = new ArrayList<WakeupItem>();
		Cursor cursor = null;
		try
		{
			cursor = mContext.getContentResolver().query(WhiteListColumns.RelateWakeup.CONTENT_URI, 
					new String[]{WhiteListColumns.RelateWakeup.PKGNAME,WhiteListColumns.RelateWakeup.CLASS,WhiteListColumns.RelateWakeup.ACTION,WhiteListColumns.RelateWakeup.CALLERPKG,WhiteListColumns.BaseColumns.ENABLE},
					null,null,null);
					//WhiteListColumns.BaseColumns.ENABLE+"=?",new String[]{"0"}, null);
			
			if(cursor != null)
			{				
				while(cursor.moveToNext())
				{
					WakeupItem item = new WakeupItem();
					item.targetPkg = cursor.getString(0);
					item.classname = cursor.getString(1);
					item.action    = cursor.getString(2);
					item.callerpkg = cursor.getString(3);
					item.state     = cursor.getInt(4);	
					if(item.targetPkg !=null && item.targetPkg.equals("null"))item.targetPkg = null;
					if(item.classname !=null && item.classname.equals("null"))item.classname = null;
					if(item.action !=null && item.action.equals("null"))item.action = null;
					if(item.callerpkg !=null && item.callerpkg.equals("null"))item.callerpkg = null;
					
					wakeuplist.add(item);
					//Log.i(TAG,"getWakeupList :"+item.targetPkg+"/"+item.classname+"/"+item.action+"/"+item.callerpkg+"/"+item.state);
				}				
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			if(cursor != null)
			{
				cursor.close();
				cursor = null;
			}
		}
		
		if(wakeuplist.size() > 0)
		{
			Log.i(TAG,"getWakeupList count:"+wakeuplist.size());
			WakeupItem[] ret = new WakeupItem[wakeuplist.size()];
			wakeuplist.toArray(ret);
			return ret;
		}
		return null;
	}

	@Override
	public String[] getSleepNetList() throws RemoteException {
		Cursor cursor = null;
		String [] ret = null;
		ArrayList<String > sleepnetlist = new ArrayList<String>();
		try
		{
			cursor = mContext.getContentResolver().query(WhiteListColumns.SleepNetWhite.CONTENT_URI, 
					new String[]{WhiteListColumns.BaseColumns.PKGNAME},
					WhiteListColumns.BaseColumns.ENABLE+"=?",new String[]{""+WhiteListColumns.ENABLE}, null);
			if(cursor != null)
			{							
				while(cursor.moveToNext())
				{
					String pkgname = cursor.getString(0);
					sleepnetlist.add(pkgname);
				}				
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			if(cursor != null)
			{
				cursor.close();
				cursor = null;
			}
		}
		if(sleepnetlist.size() > 0)
		{
			Log.i(TAG,"getSleepNetList count = "+sleepnetlist.size());
			ret = new String[sleepnetlist.size()];
			sleepnetlist.toArray(ret);
			return ret;
		}
		return null;
	}

	@Override
	public String[] getBlockActivityList() throws RemoteException {
		ArrayList<String> blocklist = new ArrayList<String>();
		Cursor cursor = null;
		try
		{
			cursor = mContext.getContentResolver().query(WhiteListColumns.BlockActivity.CONTENT_URI, 
					new String[]{WhiteListColumns.BlockActivity.CLASS},
					null,null,null);
					//WhiteListColumns.BaseColumns.ENABLE+"=?",new String[]{"0"}, null);
			
			if(cursor != null)
			{				
				while(cursor.moveToNext())
				{
					String classnmae = cursor.getString(0);
					blocklist.add(classnmae);
				}				
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			if(cursor != null)
			{
				cursor.close();
				cursor = null;
			}
		}
		
		if(blocklist.size() > 0)
		{
			Log.i(TAG,"getBlockActivityList count:"+blocklist.size());
			String[] ret = new String[blocklist.size()];
			blocklist.toArray(ret);
			return ret;
		}
		return null;
	}

	@Override
	public String[] getMsgWhiteList() throws RemoteException {
		Cursor cursor = null;
		String [] ret = null;
		ArrayList<String > msgwhiteList = new ArrayList<String>();
		try
		{
			cursor = mContext.getContentResolver().query(WhiteListColumns.MsgWhite.CONTENT_URI, 
					new String[]{WhiteListColumns.BaseColumns.PKGNAME},
					WhiteListColumns.BaseColumns.ENABLE+"=?",new String[]{""+WhiteListColumns.ENABLE}, null);
			if(cursor != null)
			{							
				while(cursor.moveToNext())
				{
					String pkgname = cursor.getString(0);
					msgwhiteList.add(pkgname);
				}				
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			if(cursor != null)
			{
				cursor.close();
				cursor = null;
			}
		}
		if(msgwhiteList.size() > 0)
		{
			Log.i(TAG,"getMsgWhiteList count = "+msgwhiteList.size());
			ret = new String[msgwhiteList.size()];
			msgwhiteList.toArray(ret);
			return ret;
		}
		return null;
	}

	@Override
	public boolean isCanSendMsg(String pkgname) throws RemoteException {
		Cursor cursor = null;
		boolean ret = false;		
		try
		{
			cursor = mContext.getContentResolver().query(WhiteListColumns.MsgWhite.CONTENT_URI, 
					new String[]{WhiteListColumns.BaseColumns._ID},
					WhiteListColumns.BaseColumns.PKGNAME+"=? AND "+WhiteListColumns.BaseColumns.ENABLE+"=? ",
					new String[]{pkgname,""+WhiteListColumns.ENABLE}, null);
			if(cursor != null)
			{		
				if(cursor.getCount() > 0)
				{
					ret = true;
				}				
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}	
		finally
		{
			if(cursor != null)
			{
				cursor.close();
				cursor = null;
			}
		}
		return ret;
	}

	@Override
	public String[] getInstallWhiteList() throws RemoteException {
		Cursor cursor = null;
		String [] ret = null;
		ArrayList<String > installList = new ArrayList<String>();
		try
		{
			cursor = mContext.getContentResolver().query(WhiteListColumns.InstallWhite.CONTENT_URI, 
					new String[]{WhiteListColumns.BaseColumns.PKGNAME},
					WhiteListColumns.BaseColumns.ENABLE+"=?",new String[]{""+WhiteListColumns.ENABLE}, null);
			if(cursor != null)
			{							
				while(cursor.moveToNext())
				{
					String pkgname = cursor.getString(0);
					installList.add(pkgname);
				}				
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			if(cursor != null)
			{
				cursor.close();
				cursor = null;
			}
		}
		if(installList.size() > 0)
		{
			Log.i(TAG,"getInstallWhiteList count = "+installList.size());
			ret = new String[installList.size()];
			installList.toArray(ret);
			return ret;
		}
		return null;
	}
	@Override
	public String[] getDozeWhiteList() throws RemoteException {
		Cursor cursor = null;
		String [] ret = null;
		ArrayList<String > dozeList = new ArrayList<String>();
		try
		{
			cursor = mContext.getContentResolver().query(WhiteListColumns.DozeWhiteList.CONTENT_URI, 
					new String[]{WhiteListColumns.BaseColumns.PKGNAME},
					WhiteListColumns.BaseColumns.ENABLE+"=?",new String[]{""+WhiteListColumns.ENABLE}, null);
			if(cursor != null)
			{							
				while(cursor.moveToNext())
				{
					String pkgname = cursor.getString(0);
					dozeList.add(pkgname);
				}				
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			if(cursor != null)
			{
				cursor.close();
				cursor = null;
			}
		}
		if(dozeList.size() > 0)
		{
			Log.i(TAG,"getDozeWhiteList count = "+dozeList.size());
			ret = new String[dozeList.size()];
			dozeList.toArray(ret);
			return ret;
		}
		return null;
	}
       public WakeupItem[] getProviderWakeupWhiteList() /*throws RemoteException*/ {
		ArrayList<WakeupItem> wakeuplist = new ArrayList<WakeupItem>();
		Cursor cursor = null;
		try
		{
			cursor = mContext.getContentResolver().query(WhiteListColumns.ProviderWakeup.CONTENT_URI, 
					new String[]{WhiteListColumns.ProviderWakeup.PKGNAME,WhiteListColumns.ProviderWakeup.CLASS,WhiteListColumns.ProviderWakeup.CALLERPKG,WhiteListColumns.BaseColumns.ENABLE},
					null,null,null);
					//WhiteListColumns.BaseColumns.ENABLE+"=?",new String[]{"0"}, null);
			
			if(cursor != null)
			{				
				while(cursor.moveToNext())
				{
					WakeupItem item = new WakeupItem();
					item.targetPkg = cursor.getString(0);
					item.classname = cursor.getString(1);
					item.action    = null;
					item.callerpkg = cursor.getString(2);
					item.state     = cursor.getInt(3);	
					if(item.targetPkg !=null && item.targetPkg.equals("null"))item.targetPkg = null;
					if(item.classname !=null && item.classname.equals("null"))item.classname = null;
					if(item.action !=null && item.action.equals("null"))item.action = null;
					if(item.callerpkg !=null && item.callerpkg.equals("null"))item.callerpkg = null;
					
					wakeuplist.add(item);
					//Log.i(TAG,"getWakeupList :"+item.targetPkg+"/"+item.classname+"/"+item.action+"/"+item.callerpkg+"/"+item.state);
				}				
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			if(cursor != null)
			{
				cursor.close();
				cursor = null;
			}
		}
		
		if(wakeuplist.size() > 0)
		{
			Log.i(TAG,"getProviderWakeupWhiteList count:"+wakeuplist.size());
			WakeupItem[] ret = new WakeupItem[wakeuplist.size()];
			wakeuplist.toArray(ret);
			return ret;
		}
		return null;
	}
	private void doListenerCallback()
	{
		for(int i=0;i<mListenerList.size();i++)
		{
			WhiteListListener listener = mListenerList.get(i);
			doCallbackListener(listener);
		}
	}
	@Override
	public boolean isCanInstall(String pkgname) throws RemoteException {
		Cursor cursor = null;
		boolean ret = false;		
		try
		{
			cursor = mContext.getContentResolver().query(WhiteListColumns.InstallWhite.CONTENT_URI, 
					new String[]{WhiteListColumns.BaseColumns._ID},
					WhiteListColumns.BaseColumns.PKGNAME+"=? AND "+WhiteListColumns.BaseColumns.ENABLE+"=? ",
					new String[]{pkgname,""+WhiteListColumns.ENABLE}, null);
			if(cursor != null)
			{		
				if(cursor.getCount() > 0)
				{
					ret = true;
				}				
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}	
		finally
		{
			if(cursor != null)
			{
				cursor.close();
				cursor = null;
			}
		}
		return ret;
	}
	@Override
	public void registerChangeListener(IWhiteListChangeListener changelistener,int whitelistType)
			throws RemoteException {
		WhiteListListener listlistener = new WhiteListListener(whitelistType,changelistener);
		synchronized (this) 
		{
			for(int i=0;i<mListenerList.size();i++)
			{
				WhiteListListener listener = mListenerList.get(i);
				if(listener.listener == changelistener && whitelistType == listener.type)
				{
					Log.i(TAG,"registerChangeListener already register!");
					return;
				}
			}
			mListenerList.add(listlistener);
		}		
		//mHandler.sendMessage(mHandler.obtainMessage(DO_CALLBACK, listlistener));
	}
	@Override
	public void unregisterChangeListener(
			IWhiteListChangeListener changelistener) throws RemoteException {
		//listenerlist.remove(changelistener);	
		synchronized (this) {
			for(int i=0;i<mListenerList.size();i++)
			{
				WhiteListListener item = mListenerList.get(i);
				if(item.listener == changelistener)
				{
					mListenerList.remove(i);
					i --;
				}
			}
		}
	}
	private void doCallbackListener(WhiteListListener changelistener)
	{
		Log.i(TAG,"doCallbackListener type:"+changelistener.type);
		switch(changelistener.type)
		{
		case WhiteListManager.WHITELIST_TYPE_PUREBKGROUND:
			try
			{
				changelistener.listener.onPurebackgroundChange(true, true, true);
			}
			catch(Exception e)
			{
				
			}
			break;
		case WhiteListManager.WHITELIST_TYPE_NOTIFICATION:
			try
			{
				changelistener.listener.onNotificationChange(true, true);
			}
			catch(Exception e)
			{
				
			}
			break;
		case WhiteListManager.WHITELIST_TYPE_FLOATWINDOW:
			try
			{
				changelistener.listener.onFloatDefChange();
			}
			catch(Exception e)
			{
				
			}
			break;
		case WhiteListManager.WHITELIST_TYPE_AUTOLAUNCH:
			try
			{
				changelistener.listener.onAutoLaunchDefChange();
			}
			catch(Exception e)
			{
				
			}
			break;
		case WhiteListManager.WHITELIST_TYPE_NETFORBADE:
			try
			{
				changelistener.listener.onNetForbadeListChange();
			}
			catch(Exception e)
			{
				
			}
			break;
		case WhiteListManager.WHITELIST_TYPE_RELEATEWAKEUP:
			try
			{
				WakeupItem[] wakeuplist = getWakeupList();
				changelistener.listener.onWakeupListChange(wakeuplist);
			}
			catch(Exception e)
			{
				
			}
			break;
		case WhiteListManager.WHITELIST_TYPE_SLEEPNET:
			try
			{
				String[] sleepnetlist = getSleepNetList();
				changelistener.listener.onSleepNetListChange(sleepnetlist);
			}
			catch(Exception e)
			{
				
			}
			break;
		case WhiteListManager.WHITELIST_TYPE_BLOCKACTIVITY:
			try
			{
				String[] blocklist = getBlockActivityList();
				changelistener.listener.onBlockActivityListChange(blocklist);
			}
			catch(Exception e)
			{
				
			}
			break;
		case WhiteListManager.WHITELIST_TYPE_MSGWHITE:
			try
			{
				changelistener.listener.onMsgWhiteListChange();
			}
			catch(Exception e)
			{
				
			}
			break;
		case WhiteListManager.WHITELIST_TYPE_INSTALLWHITE:
			try
			{
				changelistener.listener.onInstallWhiteListChange();
			}
			catch(Exception e)
			{
				
			}
			break;

            case WhiteListManager.WHITELIST_TYPE_PROVIDERWAKEUP:
			try
			{
				WakeupItem[] wakeuplist = getProviderWakeupWhiteList();
				changelistener.listener.onProviderWakeupListChange(wakeuplist);
			}
			catch(Exception e)
			{
				
			}
			break;
		}
		
	}
	public class WhiteListListener
	{
		int type;
		IWhiteListChangeListener listener;
		public WhiteListListener(int _type,IWhiteListChangeListener _listener)
		{
			type = _type;
			listener = _listener;
		}
	}
	private ArrayList<WhiteListListener> mListenerList = new ArrayList<WhiteListListener>();
}
