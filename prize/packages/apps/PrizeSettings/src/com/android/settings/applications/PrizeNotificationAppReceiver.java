package com.android.settings.applications;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
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
import android.content.SharedPreferences;
/*-prize add by lihuangyuan,for whitelist -2017-05-06-start-*/
import android.os.WhiteListManager;
import android.provider.WhiteListColumns;
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
import android.os.RemoteException;
/*-prize add by lihuangyuan,for whitelist -2017-05-06-end-*/
/*-prize-add by lihuangyuan-for draw overlay other app permission-2017-12-15-start*/
import android.app.AppOpsManager;
/*-prize-add by lihuangyuan-for draw overlay other app permission-2017-12-15-end*/

public class PrizeNotificationAppReceiver extends BroadcastReceiver {

	private static final String TAG = "whitelist";
	private static Backend mBackend = new Backend();
	private PackageManager packageManager;
	private static boolean bBackend = false;
	private static boolean bMoMService = false;
	private	static int uid = 0;
	private	static String pkgName = null;
	/*-prize add by lihuangyuan,for whitelist -2017-05-06-start-*/
	private Context mContext ;
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
	@Override
	public void onReceive(Context context, Intent intent) {
		packageManager = context.getPackageManager();
		//install app->ACTION_PACKAGE_ADDED;update app->ACTION_PACKAGE_ADDED + ACTION_PACKAGE_REPLACED
		if (intent.getAction().equals(Intent.ACTION_PACKAGE_ADDED)) {
			try {
				pkgName = intent.getData().getEncodedSchemeSpecificPart();
				ApplicationInfo ai = packageManager.getApplicationInfo(pkgName, PackageManager.GET_ACTIVITIES);				
				
				if(("com.android.launcher3").equals(pkgName)){
					startApk("com.android.launcher3","com.android.launcher3.Launcher");
					return;
				}
				/*-prize add by lihuangyuan,for whitelist -2017-05-06-start-*/
				if(pkgName == null)return;
				mContext = context;
				WhiteListManager whiteListMgr = (WhiteListManager)context.getSystemService(Context.WHITELIST_SERVICE);
				/*-prize-add by lihuangyuan-for draw overlay other app permission-2017-12-15-start*/
				boolean isfloatenable = whiteListMgr.isFloatEnable(pkgName);
				if(isfloatenable)
				{
					Log.i(TAG,"float enable :"+isfloatenable+",package:"+pkgName);
					AppOpsManager mAppOpsManager = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
					mAppOpsManager.setMode(AppOpsManager.OP_SYSTEM_ALERT_WINDOW,
                				ai.uid, pkgName, AppOpsManager.MODE_ALLOWED);
				}
				/*-prize-add by lihuangyuan-for draw overlay other app permission-2017-12-15-end*/
				
				//notification
		 		String [] defList = whiteListMgr.getNotificationDefList();
				String [] hideList = whiteListMgr.getNotificationHideList();
				if(isInList(pkgName,hideList))
				{
					Log.i(TAG,"addpackage "+pkgName+" in notification hide list ignore");
				}
				else
				{					
					if(isInList(pkgName,defList))
					{						
						mBackend.setNotificationsBanned(pkgName, ai.uid, false);						
						Log.i(TAG,"addpackage "+pkgName+" in notification def list set Banned false");
					}
					else
					{	
						mBackend.setNotificationsBanned(pkgName, ai.uid, true); 
						Log.i(TAG,"addpackage "+pkgName+" not in notification def list set Banned true");
					}
				}

				//autolaunch
				mUid = UserHandle.myUserId();
                           final String [] autolaunchdefList = whiteListMgr.getAutoLaunchDefList();
				new Thread(new Runnable() {
			            @Override
			            public void run() {
				                synchronized (mContext)
				                {
							ArrayList<String> denylist = loadAutoLaunchDenyListFromFile();
							ArrayList<String>bootrecvlist = getBootReceivers(mContext);
							if(bootrecvlist.contains(pkgName))//have boot receiver
							{
								if(!isInList(pkgName,autolaunchdefList))//deny auto launch
								{
								    Log.i(TAG,"addpackage "+pkgName+" to denylist");
								    denylist.add(pkgName);
								    saveAutolaunchDenyToFile(denylist);
								}
								else
								{
									if(denylist.contains(pkgName))
									{
										Log.i(TAG,"remove "+pkgName+" from denylist");
										denylist.remove(pkgName);
										saveAutolaunchDenyToFile(denylist);
									}
								}
							}							
				                }
			            	}
				}).start();
				/*-prize add by lihuangyuan,for whitelist -2017-05-06-end-*/

				/*prize-removed by lihuangyuan,for do not modify notification backend 2017-11-07-start*/
				/*uid = ai.uid;
				bBackend = mBackend.getNotificationsBanned(pkgName,uid);				
				SharedPreferences sp = context.getSharedPreferences("appcontrol",
						Context.MODE_PRIVATE);
				SharedPreferences.Editor editor = sp.edit();
				editor.putBoolean("backend", bBackend);
				editor.putString("pkgname", pkgName);
				editor.putInt("uid", uid);
				editor.commit();
				mBackend.setNotificationsBanned(pkgName, uid, true); */
				/*prize-removed by lihuangyuan,for do not modify notification backend 2017-11-07-end*/
				
			} catch (Exception e) {
				return;
			}
		}
		if(intent.getAction().equals(Intent.ACTION_PACKAGE_REPLACED)){//update app,do not modify notification backend

			/*prize-removed by lihuangyuan,for do not modify notification backend 2017-11-07-start*/
			/*SharedPreferences sp = context.getSharedPreferences("appcontrol",
						Context.MODE_PRIVATE);
			bBackend = sp.getBoolean("backend", false);
			pkgName = sp.getString("pkgname", "");
			uid = sp.getInt("uid", 0);*/
			
			/*if(pkgName.equals("com.prize.appcenter")){
				bBackend = true;
			}*/						
			/*Log.i(TAG,"replacepackage "+pkgName+",bBackend:"+bBackend);
			mBackend.setNotificationsBanned(pkgName, uid, bBackend);*/ 
			/*prize-removed by lihuangyuan,for do not modify notification backend 2017-11-07-end*/
		}

		
	}
       /*-prize add by lihuangyuan,for whitelist -2017-05-06-start-*/
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
	  /*-prize add by lihuangyuan,for whitelist -2017-11-08-start-*/
	  //write to settings
	  String denyAutoAppList ="";
	  for (String name : denyList) 
	  {
	      denyAutoAppList += name + ",";
	  }
	  Settings.System.putString(mContext.getContentResolver(), "denyAutoAppList",denyAutoAppList);
	  /*-prize add by lihuangyuan,for whitelist -2017-11-08-end-*/
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
    /*-prize add by lihuangyuan,for whitelist -2017-05-06-end-*/
    public static boolean startApk(String packageName, String activityName) {  
        boolean isSuccess = false;  
          
        String cmd = "am start -n " + packageName + "/" + activityName + " \n";  
        try {  
            java.lang.Process process = Runtime.getRuntime().exec(cmd);  
              
//            isSuccess = waitForProcess(process);  
        } catch (IOException e) {   
            e.printStackTrace();  
        }   
        return isSuccess;  
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
