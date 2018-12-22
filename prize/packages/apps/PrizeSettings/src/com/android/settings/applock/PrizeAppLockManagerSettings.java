/*
 * Copyright (C) 2006 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.applock;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.android.settings.R;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Comparator;

import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceScreen;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;

import com.android.internal.logging.MetricsProto.MetricsEvent;

import android.content.pm.LauncherApps;
import android.content.pm.LauncherActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.UserHandle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuInflater;
import android.graphics.drawable.Drawable;
import android.database.Cursor;
import android.net.Uri;
import android.content.pm.PackageManager;
import android.content.pm.PackageInfo;
import com.android.settings.SettingsPreferenceFragment;
import android.widget.TextView;
/**
 * Activity to pick an application that will be used to display installation information and
 * options to uninstall/delete user data for system applications. This activity
 * can be launched through Settings or via the ACTION_MANAGE_PACKAGE_STORAGE
 * intent.
 */
public class PrizeAppLockManagerSettings extends SettingsPreferenceFragment 
	implements OnPreferenceChangeListener
{

    static final String TAG = "PrizeAppLock";
    static final boolean DEBUG = Log.isLoggable(TAG, Log.DEBUG);
    static final String RECOMMAND_KEY = "recommand_category";
    static final String ENCRYPTION_KEY = "can_encryption_app";

    private static String[] sRecommandAppArray = new String[]{
		"com.android.mms",		
		"com.android.dialer",
		//"com.android.contacts",
		"com.android.gallery3d",
		"com.tencent.mm",
		"com.tencent.mobileqq",
		"com.qiyi.video",
		"com.sina.weibo",
		"com.meitu.meipaimv",
    };
    private static String[] sCannotLockAppArray = new String[]{
		"com.android.music",
		"com.iflytek.inputmethod",
		"com.sohu.inputmethod.sogou",
		"com.nqmobile.live.base",
		"com.android.launcher3",
		"com.pr.scuritycenter",
		"com.android.calculator2",
		"com.android.fmradio",
		"com.android.providers.downloads.ui",
		"com.assistant.icontrol",
		"com.prize.lockscreen",
		"com.prize.compass",
		"com.android.stk",
		"com.android.utk",
		"com.baidu.map.location",
		"com.android.settings",
		"com.mediatek.camera",
		"com.prize.factorytest",
		"com.android.documentsui",
		"com.prize.flash",	
		"com.baidu.duer.phone",
		"com.android.contacts",
    };
    private PreferenceCategory mRecommandCategory;
    private PreferenceCategory mCanEncryptionCategory;
    private PackageManager mPackageManager;
    private LauncherApps mLauncherApps;
    private CustomComparator mCustomComparator;

    protected int getMetricsCategory() {
        return MetricsEvent.DISPLAY;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.prize_applock_settings);
	 mPackageManager = getActivity().getPackageManager();
	 mLauncherApps = (LauncherApps) getActivity().getSystemService(Context.LAUNCHER_APPS_SERVICE);
	 mRecommandCategory = (PreferenceCategory)findPreference(RECOMMAND_KEY);
	 mCanEncryptionCategory = (PreferenceCategory)findPreference(ENCRYPTION_KEY);
	 mCustomComparator = new CustomComparator();
	 
	 
    }
    
    private View mAppLockOpenParent;
    private TextView mAppLockOpenBtn;
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
    	 super.onViewCreated(view,savedInstanceState);
		 
        mAppLockOpenParent = view.findViewById(R.id.ll_prize_applock_open);
	 mAppLockOpenBtn = (TextView)view.findViewById(R.id.bt_prize_applock_open);	 
	 if(mAppLockOpenBtn != null)
	 {	 	
	 	mAppLockOpenBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startSettingPassword();
                }
            });
	 }
	 //startLoadData();
    }
    public void startSettingPassword()
    {
    	  Intent intent = new Intent();
                    intent.setClassName("com.android.settings",
                            PrizeAppLockPasswordSettingsActivity.class.getName());
         startActivity(intent);
	  //getActivity().finish();
    }
    @Override
    public boolean onPreferenceChange(Preference preference, Object value) 
    {
    	  if(preference == null) return true;
    	  String key = preference.getKey();
	  if(key == null)return true;
	  
	  boolean isCheck = (Boolean) value;
	  AppItem selitem=null;
	  Log.d(TAG,"onPreferenceChange key:"+key+",value:"+value);
	  synchronized (mInstallApps) 
	  {	  	
	  	for(int i=0;i<mInstallApps.size();i++)
	  	{
	  		AppItem item = mInstallApps.get(i);
			if(item.pkgname.equals(key))
			{
				selitem = item;
				item.isLock = isCheck?PrizeAppLockMetaData.LOCK_STATUS_LOCK:PrizeAppLockMetaData.LOCK_STATUS_UNLOCK;
				break;
			}
	  	}
	  }
	  //update
	  if(selitem != null)
	  {
	  	updateAppLock(selitem.pkgname, "",selitem.isLock);
	  }	  

	  return true;
    }
    public void initPreference()
    {
    	 Log.d(TAG,"initPreference...");
    	 /* prize-add-by-lijimeng-for bugid 54178-20180330-start*/
    	 if(getActivity() == null){
			 Log.d(TAG,"initPreference...null");
    	 	return;
		 }
		 /* prize-add-by-lijimeng-for bugid 54178-20180330-end*/
    	 mRecommandCategory.removeAll();
	 mCanEncryptionCategory.removeAll();
    	 for(int i=0;i<mInstallApps.size();i++)
    	 {
    	 	AppItem item = mInstallApps.get(i);
		SwitchPreference switchpref = new SwitchPreference(getActivity());
		switchpref.setKey(item.pkgname);
		switchpref.setLayoutResource(R.layout.prize_applock_preference_material);
		switchpref.setIcon(item.appIcon);
		switchpref.setTitle(item.appname);
		switchpref.setChecked(item.isLock == PrizeAppLockMetaData.LOCK_STATUS_LOCK);
		switchpref.setOnPreferenceChangeListener(this);
		
		if(item.isRecommand)
		{
			Log.d(TAG,"initPreference mRecommandCategory add "+item.appname);
			mRecommandCategory.addPreference(switchpref);
		}
		else
		{
			Log.d(TAG,"initPreference mCanEncryptionCategory add "+item.appname);
			mCanEncryptionCategory.addPreference(switchpref);
		}
    	 }
	 mHandler.sendEmptyMessageDelayed(MSG_REFERSH_UI2, 100);
    }

    private boolean isInitFinished = false;
    public static final int MSG_REFERSH_UI = 1;
    public static final int MSG_REFERSH_UI2 = 2;
    Handler mHandler = new Handler() 
    {
        public void handleMessage(Message msg) 
	{
            switch (msg.what) 
	     {
            case MSG_REFERSH_UI:
		    synchronized (mInstallApps) 
		    {
		    	initPreference();
		    }
		    isInitFinished = true;
		    break;
	     case MSG_REFERSH_UI2:
		 	setLoading(false,true);
		 	break;
            }
       }
    };
    public void startLoadData()
    {
       setLoading(true, false);
    	new Thread(new Runnable() {
			@Override
			public void run() {
				isInitFinished = false;
				synchronized (mInstallApps) 
				{
					getInstallApps();
				}

				mHandler.sendEmptyMessageDelayed(MSG_REFERSH_UI, 100);
			}
		}).start();
    }


	
    @Override
    public void onResume() {
        super.onResume();     

         startLoadData();
        boolean ishaspassword = hasAppLockCipher();
	 if(mAppLockOpenParent != null)
	 {
	 	if(!ishaspassword)
	 	{
			mAppLockOpenParent.setVisibility(View.VISIBLE);
	 	}
		else 
		{
			mAppLockOpenParent.setVisibility(View.GONE);			
		}
	 }
	 if(mMenu != null)
	 {
	     if(ishaspassword)
	     {
	         mMenu.setGroupVisible(Menu.NONE, true);
	     }
	     else
	     {
	         mMenu.setGroupVisible(Menu.NONE, false);
	     }
	 }
    }   

     /////////////////////////////////////////////////////////////////////////
    public class AppItem
    {
    	 String pkgname;
	 String appname;
	 Drawable appIcon;
	 int isLock;// 1:lock 0:unlock
	 boolean isRecommand;
	 long firstInstallTime;
    }
    private ArrayList<AppItem> mInstallApps = new ArrayList<AppItem>();
    private boolean isRecommandApp(String pkgname)
    {
    	for(int i=0;i<sRecommandAppArray.length;i++)
    	{
    		if(sRecommandAppArray[i].equals(pkgname))
    		{
    			return true;
    		}
    	}
	return false;
    }
    private boolean isHideApp(String pkgname)
    {
    	for(int i=0;i<sCannotLockAppArray.length;i++)
    	{
    		if(sCannotLockAppArray[i].equals(pkgname))
    		{
    			return true;
    		}
    	}
	return false;
    }
    private void getInstallApps()
    {
            mInstallApps.clear();
    	     PackageManager pkgmgr = getActivity().getPackageManager();
    	     ArrayList<String> lockApps = getLockAppList();
    	     List<LauncherActivityInfo> lais= mLauncherApps.getActivityList(null,UserHandle.getCallingUserHandle());
            for (LauncherActivityInfo lai : lais) 
	    {
                ApplicationInfo app = lai.getApplicationInfo();
                String appName = app.loadLabel(mPackageManager).toString();
		  if(app == null)continue;
                if(isHideApp(app.packageName))
		  {
                    Log.i(TAG, "hide appname="  + appName + ", app.packageName=" + app.packageName);
                    continue;
                }
		  PackageInfo pkginfo = null;
		  try
		  {
		  	pkginfo = pkgmgr.getPackageInfo(app.packageName,0);
		  }
		  catch(Exception ex){}
		  if(pkginfo == null)continue;
		  
                AppItem item = new AppItem();
		  item.pkgname = app.packageName;
		  item.appIcon = app.loadIcon(mPackageManager);
		  item.appname = appName;		                  
		  item.firstInstallTime = pkginfo.firstInstallTime;
                item.isLock = PrizeAppLockMetaData.LOCK_STATUS_UNLOCK;
		  if(lockApps.contains(item.pkgname))
		  {
		  	item.isLock = PrizeAppLockMetaData.LOCK_STATUS_LOCK;
		  }
		  if(isRecommandApp(item.pkgname))
		  {
		  	item.isRecommand = true;
		  }
		  if("com.android.dialer".equals(item.pkgname))
		  {
		         List<LauncherActivityInfo> aislist= mLauncherApps.getActivityList("com.android.contacts",UserHandle.getCallingUserHandle());
			  if(aislist.size() > 0)
			  {
			      item.appname += "("+aislist.get(0).getApplicationInfo().loadLabel(mPackageManager).toString()+")";
			  }		      
		  }
		  Log.i(TAG, "add appname="  + item.appname + ", packageName=" + item.pkgname+",islock:"+item.isLock+",isrecommand:"+item.isRecommand);		  
		  
		  mInstallApps.add(item);		  	  		  
           }
	    Collections.sort(mInstallApps, mCustomComparator);
    }    
  	public class CustomComparator implements Comparator<Object>
	{
		@Override
		public int compare(Object obj1, Object obj2) 
	      {
			if(obj1 instanceof AppItem && obj2 instanceof AppItem){
				AppItem item1 = (AppItem)obj1;
				AppItem item2 = (AppItem)obj2;
				int compareName = item1.appname.compareTo(item2.appname); 
				return compareName;
				/*if(item1.firstInstallTime > item2.firstInstallTime)
				{
					return -1;
				}
				else if(item1.firstInstallTime < item2.firstInstallTime)
				{
					return 1;
				}
				else
				{
					return 0;
				}*/
			}
			return -1;
		}

	}
      /////////////////////////////////////////////////////////////////////////////
      private ArrayList<String> getLockAppList()
	{
		ArrayList<String> tmplist = new ArrayList<String>();
		Cursor dataCursor = null;	
		if(getActivity() == null)return tmplist;
		
		try 
		{						
			dataCursor = getActivity().getContentResolver().query(PrizeAppLockMetaData.CONTENT_URI, new String[]{PrizeAppLockMetaData.PKG_NAME}, PrizeAppLockMetaData.LOCK_STATUS+"=?", new String[]{""+PrizeAppLockMetaData.LOCK_STATUS_LOCK},null);
			if(dataCursor != null && dataCursor.getCount() > 0 )
			{
				while(dataCursor.moveToNext())
				{
					String pkgname = dataCursor.getString(0);
					tmplist.add(pkgname);
				}
			}
			if(dataCursor != null)
			{
				dataCursor.close();
				dataCursor = null;
			}			
			
		} catch (Exception e) {
			
		} finally {
			if (dataCursor != null) {
				dataCursor.close();
				dataCursor = null;
			}			
		}
		return tmplist;		
	}
	 public boolean hasAppLockCipher() 
	 {
	        String whereApplockCipher = PrizeAppLockCipherMetaData.CIPHER_STATUS + " ="+PrizeAppLockCipherMetaData.CHIPHER_STATUS_VALID;//effective.
	        ContentResolver resolverr = getContentResolver();
	        boolean hasApplockCipher = false;
	        if (null != resolverr) 
		 {
	            Cursor cursorApplockCipher = resolverr.query(PrizeAppLockCipherMetaData.CONTENT_URI, null, whereApplockCipher, null, null);
	            if (null != cursorApplockCipher && cursorApplockCipher.getCount() > 0) 
		     {
	            		hasApplockCipher = true;
	            } 
		     else 
		     {
	            		hasApplockCipher = false;
	            }
	            if(cursorApplockCipher != null)
		     {
	            		cursorApplockCipher.close();
	            }
	        }
	        return hasApplockCipher;
    }
    private void updateAppLock(String pkgName,String className,int lockstatus)
    {
    		String where = PrizeAppLockMetaData.PKG_NAME + " =?";
        	String[] selectionArgs = new String[]{pkgName};
        	ContentResolver resolver = getContentResolver();
		if(resolver == null)return;

		Log.d(TAG,"updateAppLock pkgName:"+pkgName+",lockstatus:"+lockstatus);
		Cursor mCursor = null;
		try 
		{
	              mCursor = resolver.query(PrizeAppLockMetaData.CONTENT_URI, null, where, selectionArgs,null);
	    		if(null != mCursor && mCursor.getCount() > 0)
			{
	    			//update
	            		ContentValues values = new ContentValues();
	            		values.put(PrizeAppLockMetaData.LOCK_STATUS, lockstatus);
				resolver.update(PrizeAppLockMetaData.CONTENT_URI, values, where, selectionArgs);
	    		}
			else
			{
				//insert 
	    			ContentValues values = new ContentValues();
	            		values.put(PrizeAppLockMetaData.PKG_NAME, pkgName);
	            		values.put(PrizeAppLockMetaData.CLASS_NAME, className);
				values.put(PrizeAppLockMetaData.LOCK_STATUS, lockstatus);
	            		resolver.insert(PrizeAppLockMetaData.CONTENT_URI, values);
	    		}
		} catch (Exception e) {
			
		} finally {
			if (mCursor != null) {
				mCursor.close();
				mCursor = null;
			}			
		} 	

		if("com.android.dialer".equals(pkgName))
		{
		    updateAppLock("com.android.contacts","",lockstatus);
		}
    		
    	}

    //////////////////////////////////////////////////////////////////////////
    private Menu mMenu;
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) 
    {
	    //super.onCreateOptionsMenu(menu, inflater);
	    menu.add(Menu.NONE,  Menu.FIRST, 0, R.string.prize_applock_manager_ciphersettings);
	    Log.d(TAG,"onCreateOptionsMenu add item");
    }
    @Override
    public void onPrepareOptionsMenu(Menu menu) 
    {
        super.onPrepareOptionsMenu(menu);
	 mMenu = menu;
        if (hasAppLockCipher()) {
	     Log.d(TAG,"onPrepareOptionsMenu has password");
            menu.setGroupVisible(Menu.NONE, true);
        } else {
            Log.d(TAG,"onPrepareOptionsMenu not have password");
            menu.setGroupVisible(Menu.NONE, false);
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) 
    {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) 
	 {
            case Menu.FIRST :
                startSettingPassword();
                break;            
        }
        return true;
    } 
    //////////////////////////////////////////////////////////////////////////
}
