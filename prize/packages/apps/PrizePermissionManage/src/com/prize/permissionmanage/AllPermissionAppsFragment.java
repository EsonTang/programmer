/*
* Copyright (C) 2015 The Android Open Source Project
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

package com.prize.permissionmanage;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageItemInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.PermissionGroupInfo;
import android.content.pm.PermissionInfo;
import android.content.pm.PackageInfo;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;
import com.prize.permissionmanage.R;
import com.prize.permissionmanage.SettingsWithHeader;
import com.prize.permissionmanage.utils.Utils;

import java.util.Collections;
import java.util.Comparator;
import android.util.ArraySet;

/// M: CTA requirement - permission control @{
import android.Manifest;
import android.os.AsyncTask;
import android.preference.SwitchPreference;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.Switch;
import com.prize.permissionmanage.model.PermissionApps;
import com.prize.permissionmanage.model.PermissionApps.PermissionApp;
import com.prize.permissionmanage.model.PermissionApps.PmCache;
import com.prize.permissionmanage.model.PermissionGroup;
import com.prize.permissionmanage.model.PermissionGroups;
import com.prize.permissionmanage.model.AppPermissionGroup;
import com.prize.permissionmanage.model.Permission;
import com.prize.permissionmanage.model.PermissionState;
import com.mediatek.cta.CtaUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import android.os.Build;

///@}

/// M: [ALPS02866163] Refresh title bar text in multi-window mode
//import com.android.packageinstaller.permission.ui.ReviewPermissionsActivity;

public final class AllPermissionAppsFragment extends SettingsWithHeader implements Preference.OnPreferenceClickListener, PermissionGroups.PermissionsGroupsChangeCallback {

    private static final String LOG_TAG = "AllPermissionAppsFragment";

    private static final String KEY_OTHER = "other_perms";

    /// M: CTA requirement - permission control @{
    public static final String KEY_LAUNCHED_FROM = "launched_from";
    public static final int TYPE_FROM_NONE               = 0;
    public static final int TYPE_FROM_REVIEW_UI          = 1;
    public static final int TYPE_FROM_APP_ERROR_DIALOG   = 2;
    private boolean mAppSupportsRuntimePermissions = true;
	
	private ArraySet<String> mLauncherPkgs;

	private PermissionGroups mPermissionGroups;

    public static AllPermissionAppsFragment newInstance(String groupname,
            int launchedFrom, String permissionName) {
        AllPermissionAppsFragment instance = new AllPermissionAppsFragment();
        Bundle arguments = new Bundle();
        arguments.putString("groupname", groupname);
        /// M: CTA requirement - permission control @{
        arguments.putInt(KEY_LAUNCHED_FROM, launchedFrom);
        arguments.putString(Intent.EXTRA_PERMISSION_NAME, permissionName);
        ///@}
        instance.setArguments(arguments);
        return instance;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /// M: CTA requirement - permission control @{
        setHasOptionsMenu(!isLaunchedFromAppErrorDialog());
        final ActionBar ab = getActivity().getActionBar();
        if (ab != null) {
           // ab.setTitle(R.string.app_permissions);
            ab.setDisplayHomeAsUpEnabled(!isLaunchedFromAppErrorDialog());
        }
        //mScrolledDone = isLaunchedFromAppErrorDialog() ?
        //        TextUtils.isEmpty(getArguments().getString(Intent.EXTRA_PERMISSION_NAME)) :
        //            true;

		mLauncherPkgs = Utils.getLauncherPackages(getContext());
		mPermissionGroups = new PermissionGroups(getActivity(), getLoaderManager(), this);
		List<PermissionGroup> list = mPermissionGroups.getGroups();
		if(list.size() > 0){
			if(ab != null){
				ab.setTitle(list.get(0).getLabel());
			}
		}
		Log.d(LOG_TAG,"AllPermissionAppsFragment onCreate mLauncherPkgs.size() = " + mLauncherPkgs.size());
        ///@}
    }

    @Override
    public void onResume() {
        super.onResume();
        /// M: Refresh permission status @{
		mPermissionGroups.refresh();
        ///@}
        updateUi();
    }

	@Override
	public void onPermissionGroupsChanged() {
		Log.d(LOG_TAG,"AllPermissionAppsFragment onPermissionGroupsChanged mPermissionGroups.size() = " + mPermissionGroups.getGroups().size());
		updateUi();
	}

    private void updateUi() {
        
    	Log.d(LOG_TAG,"updateUi");
    	Context context = getActivity();
    	if (context == null) {
    		return;
    	}
		
        if (getPreferenceScreen() != null) {
            getPreferenceScreen().removeAll();
        }
        ArrayList<Preference> prefs = new ArrayList<>(); // Used for sorting.
        //String pkg = getArguments().getString(Intent.EXTRA_PACKAGE_NAME);
        PackageManager pm = getContext().getPackageManager();
		PmCache cache = new PmCache(getContext().getPackageManager());
		List<PermissionGroup> groups = mPermissionGroups.getGroups();
		String groupname = getArguments().getString("groupname");
		String permissionname = getArguments().getString(Intent.EXTRA_PERMISSION_NAME);
		Log.d(LOG_TAG,"AllPermissionAppsFragment groupname = " + groupname + " permissionname = " + permissionname + " groups.size = " + groups.size());
		PermissionGroup localgroup = null;
		for (PermissionGroup group : groups) {
			if(groupname.equals(group.getName())){
				localgroup = group;
				break;
			}
		}

        if(localgroup != null){
			boolean isSystemPermission = CtaUtils.isPlatformPermissionGroup(localgroup.getDeclaringPackage(), localgroup.getName());
			Log.d(LOG_TAG,"AllPermissionAppsFragment localgroup = " + localgroup.getName() + " isSystemPermission = " + isSystemPermission);
			
			if(isSystemPermission){
				new PermissionApps(getContext(), localgroup.getName(), new PermissionApps.Callback() {
					@Override
					public void onPermissionsLoaded(PermissionApps permissionApps) {
						if (getActivity() == null) {
							return;
						}
						setHeader(permissionApps.getIcon(), permissionApps.getLabel(), null);
						List<PermissionApp> mPermApps = permissionApps.getApps();
						for (PermissionApp app : mPermApps) {
							if (!Utils.shouldShowPermission(app)) {
								continue;
							}
							if (Utils.isSystem(app, mLauncherPkgs)) {
								// We default to not showing system apps, so hide them from count.
								continue;
							}

							List<Permission> mPermissions= app.getPermissionGroup().getPermissions();
							for (Permission permission : mPermissions) {
								//final AppPermissionGroup permGroup = app.getPermissionGroup();
								//final String[] filterPermissions = new String[] {permission.getName()};
							    PermissionInfo perm = null;
								if(permission.getName().equals(permissionname)){
        							try {
    									perm = pm.getPermissionInfo(permission.getName(), 0);
        							} catch (NameNotFoundException e) {
        								continue;
        							}
    								if ((perm.flags & PermissionInfo.FLAG_INSTALLED) == 0
    										|| (perm.flags & PermissionInfo.FLAG_REMOVED) != 0) {
    									continue;
    								}
									Myperference preference = new Myperference(getActivity(),true);
									try {
										PackageInfo info = pm.getPackageInfo(app.getPackageName(), PackageManager.GET_PERMISSIONS);
										preference.setIcon(info.applicationInfo.loadIcon(getActivity().getPackageManager()));
										preference.setTitle(info.applicationInfo.loadLabel(getActivity().getPackageManager()));
										mAppSupportsRuntimePermissions = info.applicationInfo
												.targetSdkVersion > Build.VERSION_CODES.LOLLIPOP_MR1;
									} catch (NameNotFoundException e) {
										getActivity().finish();
									}
									
									 preference.setKey(app.getKey());
									 //String permissionNumber = getString(R.string.prize_permission_count_name,count);
									 //preference.setSummary(permissionNumber);
									 //preference.setOnPreferenceClickListener(PrizeAppPermissionFragment.this);
									 
									 String summaryString = null;
									 if(mAppSupportsRuntimePermissions){
										 if(!permission.isUserFixed()){
											 summaryString = getString(R.string.prize_mode_ask);
										 }else{
											 if(permission.isGranted()){
												 summaryString = getString(R.string.prize_mode_grant);
											 }else{
												 summaryString = getString(R.string.prize_mode_block);
											 }
										 }
									 }else{
										 if (permission.hasAppOp()) {
											 if (permission.isAppOpAllowed()){
												 summaryString = getString(R.string.prize_mode_grant);
											 }else{
												 summaryString = getString(R.string.prize_mode_block);
											 }
										 }
									 }
									 preference.setSummary(summaryString);

									 preference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
										 @Override
										 public boolean onPreferenceClick(Preference preference) {
											 Intent intent = new Intent("android.intent.action.PRIZE_SET_PERMISSION_STYLE");
											 intent.putExtra("packagename", app.getPackageName());
											 intent.putExtra("permissioninfoname", permissionname);
											 intent.putExtra("groupname", groupname);
											 getActivity().startActivity(intent);
											 return true;
										 }
									 });
									 PreferenceScreen screen = getPreferenceScreen();
									 if (screen == null) {
										 screen = getPreferenceManager().createPreferenceScreen(getActivity());
										 setPreferenceScreen(screen);
									 }
									 screen.addPreference(preference);
									 break;
								}
							}
						}
					}
				}, cache).refresh(false);
			}
        }else{
			Log.d(LOG_TAG,"AllPermissionAppsFragment localgroup == null ");
        }
        /// M: CTA requirement - permission control @{
        //scrollToPermissionItem();
        ///@}
    }

    private Preference getImmutablePreference(PermissionInfo perm, PermissionGroupInfo group,
            PackageManager pm) {
        Preference pref = new Preference(getContext());
        Drawable icon = null;
        if (perm.icon != 0) {
            icon = perm.loadIcon(pm);
        } else if (group != null && group.icon != 0) {
            icon = group.loadIcon(pm);
        } else {
            icon = getContext().getDrawable(R.drawable.ic_perm_device_info);
        }
        pref.setIcon(Utils.applyTint(getContext(), icon, android.R.attr.colorControlNormal));
        pref.setTitle(perm.loadLabel(pm));
        /// M: CTA requirement - permission control @{
        if (CtaUtils.isCtaSupported()) {
            pref.setKey(perm.name);
        }
        ///@}
        final CharSequence desc = perm.loadDescription(pm);
        pref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                new AlertDialog.Builder(getContext())
                        .setMessage(desc)
                        .setPositiveButton(android.R.string.ok, null)
                        .show();
                return true;
            }
        });

        return pref;
    }

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		switch (item.getItemId()) {
			case android.R.id.home: {
				getActivity().finish();
				return true;
			}
		}
		return super.onOptionsItemSelected(item);
	}

    private boolean isLaunchedFromReviewUi() {
        return getArguments().getInt(AllAppPermissionsFragment.KEY_LAUNCHED_FROM) ==
                TYPE_FROM_REVIEW_UI;
    }

    private boolean isLaunchedFromAppErrorDialog() {
        return getArguments().getInt(KEY_LAUNCHED_FROM) ==
                TYPE_FROM_APP_ERROR_DIALOG;
    }

    @Override
    protected boolean shouldShowHeader() {
        return super.shouldShowHeader() && (CtaUtils.isCtaSupported() ?
                        !isLaunchedFromReviewUi() : true);
    }


    /// M: [ALPS02866163] Refresh title bar text in multi-window mode @{
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
		bindPermissionUi(getActivity(), getView());
    }
    ///@}

	private static void bindPermissionUi(Context context, View rootView) {
		if (context == null || rootView == null) {
			return;
		}
	}

    
    @Override
    public boolean onPreferenceClick(Preference preference) {

        return true;
    }

}
