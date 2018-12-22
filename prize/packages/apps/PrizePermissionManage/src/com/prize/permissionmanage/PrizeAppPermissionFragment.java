package com.prize.permissionmanage;

import android.app.Fragment;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.prize.permissionmanage.model.AppPermissionGroup;
import com.prize.permissionmanage.model.AppPermissions;

import java.util.ArrayList;
import java.util.List;
import com.prize.permissionmanage.R;
import android.content.Context;
/**
 * Created by prize on 2018/2/5.
 */
	public class PrizeAppPermissionFragment  extends PermissionsFrameFragment implements Preference.OnPreferenceClickListener{
	
		private List<PackageInfo> mAppList;
		private List<Integer> mAppPermissionCount = new ArrayList<>();
		private List<AppPermissions> mAppPermissionList = new ArrayList<>();
		private PackageInfo mPackageInfo;
		private  AppPermissions mAppPermissions;
		private int mPermissionCount;
		private Context mContext;
		private AsyncTask<Void, Void, List<AppPermissions>> asyncTask;
		@TargetApi(Build.VERSION_CODES.HONEYCOMB)
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			mContext = getActivity();
			mAppList = mContext.getPackageManager().getInstalledPackages(0);
			addPreferencesFromResource(R.layout.prize_app_permission);
			setLoading(true,false);
	
		    asyncTask =	new AsyncTask<Void, Void, List<AppPermissions>>() {
			   @Override
			   protected List<AppPermissions> doInBackground(Void... params) {
				   mAppPermissionCount.clear();
				   mAppPermissionList.clear();
				   for (int i = 0; i < mAppList.size(); i++) {
					   mPermissionCount = 0;
					   PackageInfo packageInfo = mAppList.get(i);
					   if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
						   continue;
					   }
					   loadPackageInfo(packageInfo);
					   //  String requestedPermission[] = mPackageInfo.requestedPermissions;
	
					   mAppPermissions = new AppPermissions(mContext, mPackageInfo, null, true, new Runnable() {
						   @Override
						   public void run() {
							   getActivity().finish();
						   }
					   });
					   if (mAppPermissions != null && mAppPermissions.getPermissionGroups().size() > 0) {
						   mAppPermissionList.add(mAppPermissions);
						   List<AppPermissionGroup> appPermissionGroups = mAppPermissions.getPermissionGroups();
						   for (int j = 0; j < appPermissionGroups.size(); j++) {
							   AppPermissionGroup appPermissionGroup = appPermissionGroups.get(j);
							   mPermissionCount += appPermissionGroup.getPermissions().size();
						   }
						   mAppPermissionCount.add(mPermissionCount);
					   }
				   }
	
				   return mAppPermissionList;
			   }
	
			   @Override
			   protected void onPostExecute(List<AppPermissions> appPermissionses) {
				   super.onPostExecute(appPermissionses);
				   for (int i = 0; i < appPermissionses.size(); i++) {
						PackageInfo packageInfo = appPermissionses.get(i).getPackageInfo();
						int count = mAppPermissionCount.get(i);
				        if(mContext != null){
							 Myperference preference = new Myperference(mContext,true);
							 preference.setIcon(packageInfo.applicationInfo.loadIcon(mContext.getPackageManager()));
							 preference.setTitle(packageInfo.applicationInfo.loadLabel(mContext.getPackageManager()));
							 preference.setKey(packageInfo.packageName);
							String permissionNumber = getString(R.string.prize_permission_count_name,count);
							 preference.setSummary(permissionNumber);
							 preference.setOnPreferenceClickListener(PrizeAppPermissionFragment.this);
							 PreferenceScreen screen = getPreferenceScreen();
							 if (screen == null) {
								 screen = getPreferenceManager().createPreferenceScreen(mContext);
								 setPreferenceScreen(screen);
							 }
							 screen.addPreference(preference);
				        }
					}
					setLoading(false,true);
				   }
			   
	
	
		   };
			asyncTask.execute();
	
	
		}
	
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			return super.onCreateView(inflater, container, savedInstanceState);
		}
	
		@Override
		public boolean onPreferenceClick(Preference preference) {
			Intent intent = new Intent("android.intent.action.PRIZE_SET_PERMISSION");
			intent.putExtra(Intent.EXTRA_PACKAGE_NAME,preference.getKey());
			mContext.startActivity(intent);
			return true;
		}
	
		private void loadPackageInfo(PackageInfo packageInfo) {
			try {
				mPackageInfo = mContext.getPackageManager().getPackageInfo(
						packageInfo.packageName, PackageManager.GET_PERMISSIONS);
			} catch (PackageManager.NameNotFoundException e) {
				Log.d("lijimeng","mPackageInfo.packageName == "+mPackageInfo.packageName);
			}
		}
		
		@Override
		public void onDestroy() {
			super.onDestroy();
			if(asyncTask != null){
				 asyncTask.cancel(true);
			}
		}
	}

