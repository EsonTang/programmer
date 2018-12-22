package com.prize.permissionmanage;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.PermissionGroupInfo;
import android.content.pm.PermissionInfo;

import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceScreen;
import android.preference.PreferenceGroup;
import android.preference.PreferenceCategory;
import android.util.ArraySet;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.prize.permissionmanage.R;
import com.prize.permissionmanage.model.PermissionApps;
import com.prize.permissionmanage.model.PermissionApps.PermissionApp;
import com.prize.permissionmanage.model.PermissionApps.PmCache;
import com.prize.permissionmanage.model.PermissionGroup;
import com.prize.permissionmanage.model.PermissionGroups;
import com.prize.permissionmanage.model.AppPermissionGroup;
import com.prize.permissionmanage.model.Permission;
import com.prize.permissionmanage.PermissionsFrameFragment;
import com.prize.permissionmanage.utils.Utils;
import com.mediatek.cta.CtaUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by prize on 2018/2/5.
 */
	public class PrizePermissionFragment extends PermissionsFrameFragment
			implements PermissionGroups.PermissionsGroupsChangeCallback,
			Preference.OnPreferenceClickListener {
		private static final String LOG_TAG = "ManagePermissionsFragment";
	
		private static final String OS_PKG = "android";
	
		private static final String EXTRA_PREFS_KEY = "extra_prefs_key";
	
		private ArraySet<String> mLauncherPkgs;
	
		private PermissionGroups mPermissionGroups;
	
		private PreferenceScreen mExtraScreen;

		public static PrizePermissionFragment newInstance() {
			return new PrizePermissionFragment();
		}

		@Override
		public void onCreate(Bundle icicle) {
			super.onCreate(icicle);
			setLoading(true /* loading */, false /* animate */);
			setHasOptionsMenu(true);
			Log.d(LOG_TAG,"PrizePermissionFragment onCreate");
	//		  final ActionBar ab = getActivity().getActionBar();
	//		  if (ab != null) {
	//			  ab.setDisplayHomeAsUpEnabled(true);
	//		  }
			mLauncherPkgs = Utils.getLauncherPackages(getContext());
			mPermissionGroups = new PermissionGroups(getActivity(), getLoaderManager(), this);
			Log.d(LOG_TAG,"PrizePermissionFragment onCreate mLauncherPkgs.size() = " + mLauncherPkgs.size());
		}
	
		@Override
		public void onResume() {
			super.onResume();
			mPermissionGroups.refresh();
			updatePermissionsUi();
		}
	
		@Override
		public boolean onOptionsItemSelected(MenuItem item) {
			if (item.getItemId() == android.R.id.home) {
				getActivity().finish();
				return true;
			}
			return super.onOptionsItemSelected(item);
		}
	
		@Override
		public boolean onPreferenceClick(Preference preference) {
		/*
			String key = preference.getKey();
	
			PermissionGroup group = mPermissionGroups.getGroup(key);
			if (group == null) {
				return false;
			}
	
			Intent intent = new Intent("android.intent.action.SET_PERMISSION_APPS")
					.putExtra(Intent.EXTRA_PERMISSION_NAME, key);
			try {
				getActivity().startActivity(intent);
			} catch (ActivityNotFoundException e) {
				Log.w(LOG_TAG, "No app to handle " + intent);
			}
	    */
			return true;
		}
	
		@Override
		public void onPermissionGroupsChanged() {
			Log.d(LOG_TAG,"PrizePermissionFragment onPermissionGroupsChanged mPermissionGroups.size() = " + mPermissionGroups.getGroups().size());
			updatePermissionsUi();
		}
	
		@Override
		public void onViewCreated(View view, Bundle savedInstanceState) {
			super.onViewCreated(view, savedInstanceState);
			bindPermissionUi(getActivity(), getView());
		}
	
		private static void bindPermissionUi(Context context, View rootView) {
			if (context == null || rootView == null) {
				return;
			}
		}
	
		private void updatePermissionsUi() {
			Context context = getActivity();
			if (context == null) {
				return;
			}
	
			List<PermissionGroup> groups = mPermissionGroups.getGroups();
			PreferenceScreen screen = getPreferenceScreen();
			if (screen == null) {
				screen = getPreferenceManager().createPreferenceScreen(getActivity());
				setPreferenceScreen(screen);
			}
	
			// Use this to speed up getting the info for all of the PermissionApps below.
			// Create a new one for each refresh to make sure it has fresh data.
			PmCache cache = new PmCache(getContext().getPackageManager());
			PackageManager pm = getContext().getPackageManager();
			ArrayList<Preference> prefs = new ArrayList<>(); // Used for sorting.
			Map<String, Object> listsummary=new HashMap<String, Object>(); 
			//Map<String, Interger> listsummary = new HashMap<String, Interger>();
			for (PermissionGroup group : groups) {
				/// M: CTA requirement - permission control @{
				boolean isSystemPermission = CtaUtils.isPlatformPermissionGroup(
						group.getDeclaringPackage(), group.getName());
				/// @}

				if(isSystemPermission == false){
					continue;
				}
				
				PreferenceGroup prefGroup = findOrCreate(group, prefs);
				Preference preference = findPreference(group.getName());
				if (preference == null && mExtraScreen != null) {
					preference = mExtraScreen.findPreference(group.getName());
				}
				//final Preference finalPref = preference;
	
				new PermissionApps(getContext(), group.getName(), new PermissionApps.Callback() {
					@Override
					public void onPermissionsLoaded(PermissionApps permissionApps) {
						if (getActivity() == null) {
							return;
						}
						int granted = permissionApps.getGrantedCount(mLauncherPkgs);
						int total = permissionApps.getTotalCount(mLauncherPkgs);
						List<PermissionApp> mPermApps = permissionApps.getApps();
						PreferenceScreen localscreen = getPreferenceScreen();
						if (localscreen == null) {
							localscreen = getPreferenceManager().createPreferenceScreen(getActivity());
							setPreferenceScreen(localscreen);
						}
						for (PermissionApp app : mPermApps) {
							if (!Utils.shouldShowPermission(app)) {
								continue;
							}
							if (Utils.isSystem(app, mLauncherPkgs)) {
								// We default to not showing system apps, so hide them from count.
								continue;
							}
							List<Permission> mPermissions= app.getPermissionGroup().getPermissions();
							for (Permission permisson : mPermissions) {
								//final AppPermissionGroup permGroup = app.getPermissionGroup();
								//final String[] filterPermissions = new String[] {permisson.getName()};
							    PermissionInfo perm = null;
    							try {
									perm = pm.getPermissionInfo(permisson.getName(), 0);
    							} catch (NameNotFoundException e) {
    								continue;
    							}
								if ((perm.flags & PermissionInfo.FLAG_INSTALLED) == 0
										|| (perm.flags & PermissionInfo.FLAG_REMOVED) != 0) {
									continue;
								}

										
								Preference existingPref = localscreen.findPreference(perm.name);
								if(existingPref == null){
    								Myperference pref = new Myperference(getContext(),false);
    								//Drawable icon = null;
    								// pref.setIcon(Utils.applyTint(getContext(), icon, android.R.attr.colorControlNormal));
    								pref.setTitle(perm.loadLabel(pm));
    								pref.setKey(perm.name);
    								pref.setPersistent(false);
									prefGroup.addPreference(pref);
									int summary = 1;
									listsummary.put(perm.name, summary);
    								pref.setSummary(getString(R.string.prize_app_permission_count_name, summary));
									pref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
										@Override
										public boolean onPreferenceClick(Preference preference) {
											Intent intent = new Intent("android.intent.action.PRIZE_SET_PERMISSIONAPP_STYLE");
											intent.putExtra(Intent.EXTRA_PERMISSION_NAME, permisson.getName());
											intent.putExtra("groupname", group.getName());
											getActivity().startActivity(intent);
											return true;
										}
									});
								}else{
									int summary = 0;
                                    if (null != perm.name && listsummary.containsKey(perm.name)) {
                                        summary = (int)listsummary.get(perm.name);
                                    }
									summary++;
									listsummary.put(perm.name, summary);
    								existingPref.setSummary(getString(R.string.prize_app_permission_count_name, summary));
								}
							}
						}
	//					  finalPref.setSummary(getString(R.string.app_permissions_group_summary,
	//							  granted, total));
						//finalPref.setSummary(getString(R.string.prize_app_permission_count_name, total));
					}
				}, cache).refresh(false);
			}
	
			if (screen.getPreferenceCount() != 0) {
				setLoading(false /* loading */, true /* animate */);
			}
		}

		
		private PreferenceGroup findOrCreate(PermissionGroup group, 	ArrayList<Preference> prefs) {
			PreferenceGroup pref = (PreferenceGroup) findPreference(group.getName());
			if (pref == null) {
				pref = new PreferenceCategory(getContext());
				pref.setKey(group.getName());
				pref.setTitle(group.getLabel());
				prefs.add(pref);
				getPreferenceScreen().addPreference(pref);
			}
			pref.setSelectable(true);
			return pref;
		}
		public static class AdditionalPermissionsFragment extends PermissionsFrameFragment {
			@Override
			public void onCreate(Bundle icicle) {
				setLoading(true /* loading */, false /* animate */);
				super.onCreate(icicle);
				getActivity().setTitle(R.string.additional_permissions);
				setHasOptionsMenu(true);
	
				setPreferenceScreen(((PrizePermissionFragment) getTargetFragment()).mExtraScreen);
				setLoading(false /* loading */, true /* animate */);
			}
	
			@Override
			public void onDestroy() {
				getActivity().setTitle(R.string.app_permissions);
				super.onDestroy();
			}
	
			@Override
			public boolean onOptionsItemSelected(MenuItem item) {
				switch (item.getItemId()) {
					case android.R.id.home:
						getFragmentManager().popBackStack();
						return true;
				}
				return super.onOptionsItemSelected(item);
			}
	
			@Override
			public void onViewCreated(View view,  Bundle savedInstanceState) {
				super.onViewCreated(view, savedInstanceState);
				bindPermissionUi(getActivity(), getView());
			}
		}
	}

