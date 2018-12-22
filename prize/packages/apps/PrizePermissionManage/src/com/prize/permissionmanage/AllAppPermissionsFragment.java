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
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceGroup;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.mediatek.cta.CtaUtils;
import com.prize.permissionmanage.R;
import com.prize.permissionmanage.model.AppPermissionGroup;
import com.prize.permissionmanage.model.AppPermissions;
import com.prize.permissionmanage.model.Permission;
import com.prize.permissionmanage.model.PermissionGroup;
import com.prize.permissionmanage.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import android.os.Build;

/// M: CTA requirement - permission control @{
///@}

/// M: [ALPS02866163] Refresh title bar text in multi-window mode
//import com.android.packageinstaller.permission.ui.ReviewPermissionsActivity;

public final class AllAppPermissionsFragment extends SettingsWithHeader implements Preference.OnPreferenceClickListener {

    private static final String LOG_TAG = "AllAppPermissionsFragment";

    private static final String KEY_OTHER = "other_perms";

    private boolean mAppSupportsRuntimePermissions = true;

    public static AllAppPermissionsFragment newInstance(String packageName,
            int launchedFrom, String permissionName) {
        AllAppPermissionsFragment instance = new AllAppPermissionsFragment();
        Bundle arguments = new Bundle();
        arguments.putString(Intent.EXTRA_PACKAGE_NAME, packageName);
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
            //ab.setTitle(R.string.app_permissions);
            ab.setDisplayHomeAsUpEnabled(!isLaunchedFromAppErrorDialog());
        }
        mScrolledDone = isLaunchedFromAppErrorDialog() ?
                TextUtils.isEmpty(getArguments().getString(Intent.EXTRA_PERMISSION_NAME)) :
                    true;

        if (CtaUtils.isCtaSupported()) {
            PackageInfo pkgInfo = null;
            String pkg = getArguments().getString(Intent.EXTRA_PACKAGE_NAME);
            try {
                pkgInfo = getActivity().getPackageManager().getPackageInfo(pkg,
                        PackageManager.GET_PERMISSIONS);
            } catch (NameNotFoundException e) {
                getActivity().finish();
            }
			if(pkgInfo != null){
				mAppSupportsRuntimePermissions = pkgInfo.applicationInfo
						.targetSdkVersion > Build.VERSION_CODES.LOLLIPOP_MR1;
			}
            mAppPermissions = new AppPermissions(getActivity(), pkgInfo, null, false,
                    new Runnable() {
                @Override
                public void run() {
                    getActivity().finish();
                }
            });
        }
            if(ab != null){
                ab.setTitle(mAppPermissions.getAppLabel());
            }

        ///@}
    }

    @Override
    public void onResume() {
        super.onResume();
        /// M: Refresh permission status @{
        if (CtaUtils.isCtaSupported()) {
            mAppPermissions.refresh();
        }
        ///@}
        updateUi();
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

    private void updateUi() {
        if (getPreferenceScreen() != null) {
            getPreferenceScreen().removeAll();
        }
        addPreferencesFromResource(R.layout.prize_all_permission);
        PreferenceGroup otherGroup = (PreferenceGroup) findPreference(KEY_OTHER);
        ArrayList<Preference> prefs = new ArrayList<>(); // Used for sorting.
        prefs.add(otherGroup);
        String pkg = getArguments().getString(Intent.EXTRA_PACKAGE_NAME);
        otherGroup.removeAll();
        PackageManager pm = getContext().getPackageManager();
        getPreferenceScreen().removePreference(otherGroup);
        try {
            PackageInfo info = pm.getPackageInfo(pkg, PackageManager.GET_PERMISSIONS);

            ApplicationInfo appInfo = info.applicationInfo;
            final Drawable icon = appInfo.loadIcon(pm);
            final CharSequence label = appInfo.loadLabel(pm);
            Intent infoIntent = null;
            if (!getActivity().getIntent().getBooleanExtra(
                    "hideInfoButton", false)) {
                infoIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        .setData(Uri.fromParts("package", pkg, null));
            }
            setHeader(icon, label, infoIntent);

            if (info.requestedPermissions != null) {
                for (int i = 0; i < info.requestedPermissions.length; i++) {
                    PermissionInfo perm;
                    try {
                        perm = pm.getPermissionInfo(info.requestedPermissions[i], 0);
                    } catch (NameNotFoundException e) {
                        Log.e(LOG_TAG,
                                "Can't get permission info for " + info.requestedPermissions[i], e);
                        continue;
                    }

                    if ((perm.flags & PermissionInfo.FLAG_INSTALLED) == 0
                            || (perm.flags & PermissionInfo.FLAG_REMOVED) != 0) {
                        continue;
                    }

                    if (perm.protectionLevel == PermissionInfo.PROTECTION_DANGEROUS) {
                        PermissionGroupInfo group = getGroup(perm.group, pm);
                        PreferenceGroup pref =
                                findOrCreate(group != null ? group : perm, pm, prefs);
                        pref.addPreference(getPreference(perm, group, pm));
                    } else if (perm.protectionLevel == PermissionInfo.PROTECTION_NORMAL) {
                        PermissionGroupInfo group = getGroup(perm.group, pm);
                        otherGroup.addPreference(getPreference(perm, group, pm));
                    }
                }
            }
        } catch (NameNotFoundException e) {
            Log.e(LOG_TAG, "Problem getting package info for " + pkg, e);
        }
        // Sort an ArrayList of the groups and then set the order from the sorting.
        Collections.sort(prefs, new Comparator<Preference>() {
            @Override
            public int compare(Preference lhs, Preference rhs) {
                String lKey = lhs.getKey();
                String rKey = rhs.getKey();
                if (lKey.equals(KEY_OTHER)) {
                    return 1;
                } else if (rKey.equals(KEY_OTHER)) {
                    return -1;
                } else if (Utils.isModernPermissionGroup(lKey)
                        != Utils.isModernPermissionGroup(rKey)) {
                    return Utils.isModernPermissionGroup(lKey) ? -1 : 1;
                }
                return lhs.getTitle().toString().compareTo(rhs.getTitle().toString());
            }
        });
        for (int i = 0; i < prefs.size(); i++) {
            prefs.get(i).setOrder(i);
        }
        /// M: CTA requirement - permission control @{
        scrollToPermissionItem();
        ///@}
    }

    private PermissionGroupInfo getGroup(String group, PackageManager pm) {
        try {
            return pm.getPermissionGroupInfo(group, 0);
        } catch (NameNotFoundException e) {
            return null;
        }
    }

    private PreferenceGroup findOrCreate(PackageItemInfo group, PackageManager pm,
            ArrayList<Preference> prefs) {
        PreferenceGroup pref = (PreferenceGroup) findPreference(group.name);
        if (pref == null) {
            pref = new PreferenceCategory(getContext());
            pref.setKey(group.name);
            pref.setTitle(group.loadLabel(pm));
            prefs.add(pref);
            getPreferenceScreen().addPreference(pref);
        }
        pref.setSelectable(true);
        return pref;
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

    /// M: CTA requirement - permission control @{
    public static final String KEY_LAUNCHED_FROM = "launched_from";
    public static final int TYPE_FROM_NONE               = 0;
    public static final int TYPE_FROM_REVIEW_UI          = 1;
    public static final int TYPE_FROM_APP_ERROR_DIALOG   = 2;
    private AppPermissions mAppPermissions;
    private boolean mScrolledDone;

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

    private Preference getPreference(PermissionInfo perm, PermissionGroupInfo group,
            PackageManager pm) {
        if (isMutableGranularPermission(perm, group)) {
            return getMutablePreference(perm, group, pm);
        } else {
            return getImmutablePreference(perm, group, pm);
        }
    }

    private boolean isMutableGranularPermission(final PermissionInfo perm,
            final PermissionGroupInfo group) {
        if (CtaUtils.isCtaSupported()) {
            if (group != null &&
                    perm.protectionLevel == PermissionInfo.PROTECTION_DANGEROUS) {
                return true;
            }
        }
        return false;
    }

    private Preference getMutablePreference(PermissionInfo perm, PermissionGroupInfo group,
            PackageManager pm) {
        final AppPermissionGroup permGroup = mAppPermissions.getPermissionGroup(group.name);
        final String[] filterPermissions = new String[] {perm.name};

//        SwitchPreference pref = new SwitchPreference(getContext()) {
//            @Override
//            protected void onBindView(View view) {
//                super.onBindView(view);
//                final Switch switchWidget = (Switch) view.findViewById(
//                        com.android.internal.R.id.switch_widget);
//                if (switchWidget == null) {
//                    return;
//                }
//                switchWidget.setClickable(true);
//                if (permGroup.isSystemFixed()) {
//                    switchWidget.setEnabled(false);
//                } else {
//                    switchWidget.setEnabled(true);
//                }
//                switchWidget.setOnCheckedChangeListener(new OnCheckedChangeListener() {
//                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                        /// M: Pre-grant permissions in review UI @{
//                        if (isLaunchedFromReviewUi()) {
//                            ((ReviewPermissionsActivity) getActivity()).setPermStateCache(
//                                    permGroup.getPermission(perm.name),
//                                    isChecked ?
//                                            PermissionState.STATE_ALLOWED :
//                                            PermissionState.STATE_DENIED);
//                        } else {
//                            if (isChecked) {
//                                permGroup.grantRuntimePermissions(false, filterPermissions);
//                            } else {
//                                permGroup.revokeRuntimePermissions(false, filterPermissions);
//                            }
//                        }
//                        ///@}
//                        setChecked(isChecked);
//                    }
//                });
//            }
//            @Override
//            protected void onClick() {
//                // do nothing
//            }
//        };
        Myperference pref = new Myperference(getContext(),false);
        Drawable icon = null;
        final CharSequence desc = perm.loadDescription(pm);
        if (perm.icon != 0) {
            icon = perm.loadIcon(pm);
        } else if (group != null && group.icon != 0) {
            icon = group.loadIcon(pm);
        } else {
            icon = getContext().getDrawable(R.drawable.ic_perm_device_info);
        }
       // pref.setIcon(Utils.applyTint(getContext(), icon, android.R.attr.colorControlNormal));
        pref.setTitle(perm.loadLabel(pm));
        /// M: Pre-grant permissions in review UI @{
//        if (isLaunchedFromReviewUi()) {
//            pref.setChecked(((ReviewPermissionsActivity) getActivity())
//                    .isPermGrantedByCache(permGroup.getPermission(perm.name)));
//        } else {
//            pref.setChecked(permGroup.areRuntimePermissionsGranted(filterPermissions));
//        }
        ///@}
        pref.setKey(perm.name);
        pref.setPersistent(false);
		Permission permission = permGroup.getPermission(perm.name);
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
        pref.setSummary(summaryString);
        //pref.setOnPreferenceClickListener(this);
        pref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
				Intent intent = new Intent("android.intent.action.PRIZE_SET_PERMISSION_STYLE");
				intent.putExtra("packagename", getArguments().getString(Intent.EXTRA_PACKAGE_NAME));
				intent.putExtra("permissioninfoname", perm.name);
				intent.putExtra("groupname", group.name);
				getActivity().startActivity(intent);
				return true;
            }
        });

        return pref;
    }

    private void scrollToPermissionItem() {
        if (!CtaUtils.isCtaSupported()) {
            return;
        }
        if (mScrolledDone) {
            return;
        }
        mScrolledDone = true;
        final String perm = getArguments().getString(Intent.EXTRA_PERMISSION_NAME);
        if (TextUtils.isEmpty(perm)) {
            return;
        }
        PermissionInfo permInfo;
        try {
            permInfo = getContext().getPackageManager().getPermissionInfo(perm, 0);
        } catch (NameNotFoundException e) {
            Log.e(LOG_TAG, "Can't get permission info for " + perm, e);
            return;
        }
        final String permGroup = permInfo.group;
        if (TextUtils.isEmpty(permGroup)) {
            return;
        }
        new AsyncTask<Void, Void, Integer>() {
            @Override
            protected Integer doInBackground(Void... args) {
                int pos = 0;
                PreferenceGroup desiredGroup =
                        (PreferenceGroup) getPreferenceScreen().findPreference(permGroup);
                for (int i = 0; i < getPreferenceScreen().getPreferenceCount(); i++) {
                    PreferenceGroup group = (PreferenceGroup)
                            getPreferenceScreen().getPreference(i);
                    if (group.getOrder() < desiredGroup.getOrder()) {
                        pos += group.getPreferenceCount() + 1;
                    }
                }
                pos += desiredGroup.findPreference(perm).getOrder() + 1;
                return pos;
            }

            @Override
            protected void onPostExecute(Integer result) {
                if(result == null){
					return;
                }
                if (result.intValue() <= 0) {
                    return;
                }
                ListView lv = (ListView) getActivity().findViewById(android.R.id.list);
				if(lv != null){
					lv.setSelection(result.intValue());
				}
            }
        }.execute();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        if (CtaUtils.isCtaSupported()) {
            //View moreButton = getActivity().findViewById(R.id.more_button);
            //if (moreButton != null) {
            //    moreButton.setVisibility(View.GONE);
            //}
        }
        return super.onCreateView(inflater, container, savedInstanceState);
    }
    ///@}

    /// M: [ALPS02866163] Refresh title bar text in multi-window mode @{
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //if (isLaunchedFromReviewUi() &&
            //    getActivity() instanceof ReviewPermissionsActivity) {
            //((ReviewPermissionsActivity) getActivity()).bindUi(mAppPermissions);
        //}
    }
    ///@}
    
    @Override
    public boolean onPreferenceClick(Preference preference) {

        return true;
    }

}
