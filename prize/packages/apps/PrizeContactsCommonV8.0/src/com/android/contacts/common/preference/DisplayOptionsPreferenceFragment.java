/*
 * Copyright (C) 2010 The Android Open Source Project
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

package com.android.contacts.common.preference;

import android.app.ActionBar;
import android.content.Intent;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;


import com.android.contacts.common.R;
import com.android.contacts.common.util.ImplicitIntentsUtil;
import com.android.contacts.common.vcard.VCardCommonArguments;
import com.prize.contacts.common.util.PrizeBlockNumberHelper;
import com.android.contacts.common.model.AccountTypeManager;
import com.android.contacts.common.model.account.AccountWithDataSet;
import com.android.contacts.common.model.account.GoogleAccountType;
import com.android.contacts.commonbind.ObjectFactory;

import java.util.List;
/*prize-add for dido os8.0-hpf-2016-7-29-start*/
import android.view.LayoutInflater;
import android.view.ViewGroup;
/*prize-add for dido os8.0-hpf-2016-7-29-end*/

/**
 * This fragment shows the preferences for "display options"
 */
public class DisplayOptionsPreferenceFragment extends PreferenceFragment {
	/*prize-add-contacts-settings-item-click-event-huangliemin-2016-7-4-start*/
	private final String CONTACTS_MANAGER_KEY = "contacts_manager";
	private final String ACCOUNTS_KEY = "accounts";
	/*prize-add-contacts-settings-item-click-event-huangliemin-2016-7-4-end*/

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*prize-change-contacts-settings-huangliemin-2016-7-4-start*/
        // Load the preferences from an XML resource
        //addPreferencesFromResource(R.xml.preference_display_options);
        addPreferencesFromResource(R.xml.prize_preference_display_options);

        /*prize-remove-huangpengfei-2016-10-27-start*/
        //removeUnsupportedPreferences();
        /*prize-remove-huangpengfei-2016-10-27-end*/
        
        addExtraPreferences();
        
        /*prize-remove-huangpengfei-2016-10-27-start*/
//        final Preference aboutPreference = findPreference("about");
//        aboutPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
//            @Override
//            public boolean onPreferenceClick(Preference preference) {
//                ((ContactsPreferenceActivity) getActivity()).showAboutFragment();
//                return true;
//            }
//        });
        /*prize-remove-huangpengfei-2016-10-27-end*/
        
    /*prize-change-contacts-settings-huangliemin-2016-7-4-end*/
        /*prize-add for dido os8.0-hpf-2017-7-29-start*/
        ActionBar actionBar = getActivity().getActionBar();
        if(actionBar!=null) {
        	actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        	actionBar.setDisplayShowCustomEnabled(false);
        	actionBar.setDisplayHomeAsUpEnabled(true);
        	actionBar.setDisplayShowTitleEnabled(true);
        	actionBar.setDisplayUseLogoEnabled(false);
        	actionBar.setTitle(R.string.prize_contacts_setting_title);
        }
        /*prize-add for dido os8.0-hpf-2017-7-29-end*/
    }
	/*prize-add-wangyunhe-2016-8-8-start*/
    
    /*prize-add for dido os8.0-hpf-2017-7-29-start*/
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
    	
    	return inflater.inflate(R.layout.prize_preference_list_fragment, container, false);
    }
    /*prize-add for dido os8.0-hpf-2017-7-29-end*/
    

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
    	super.onActivityCreated(savedInstanceState);
    	getListView().setDivider(getActivity().getDrawable(R.drawable.prize_shape_list_divider));
    	getListView().setDividerHeight(1);
    }
	
	/*prize-add-wangyunhe-2016-8-8-end*/
    /*prize-add-contacts-settings-item-click-event-huangliemin-2016-7-4-start*/
    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
    		Preference preference) {
    	// TODO Auto-generated method stub
    	String key_str = preference.getKey();
    	Log.i("logtest", "onPreferenceTreeClick: "+key_str);
    	if(key_str.equals(CONTACTS_MANAGER_KEY)) {
    		/*PRIZE-change-yuandailin-2016-8-27-start*/
//    		final Intent intent = new Intent();
//    		intent.setAction("prize.action.manager_contacts");
//    		final Intent intent = new Intent(getActivity(), ContactImportExportActivity.class);
//    		intent.putExtra(VCardCommonArguments.ARG_CALLING_ACTIVITY,PeopleActivity.class.getName());
    		final Intent intent = new Intent();
    		intent.setAction("android.intent.action.contacts.list.IMPORTEXPORTCONTACTS");
    		intent.putExtra(VCardCommonArguments.ARG_CALLING_ACTIVITY,"PeopleActivity");
    		/*PRIZE-change-yuandailin-2016-8-27-end*/
    		startActivity(intent);
    	} else if(key_str.equals(ACCOUNTS_KEY)) {
    		final Intent intent = new Intent(Settings.ACTION_SYNC_SETTINGS);
    		intent.putExtra(Settings.EXTRA_AUTHORITIES, new String[]{
    			ContactsContract.AUTHORITY	
    		});
    		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
    		ImplicitIntentsUtil.startActivityInAppIfPossible(getActivity(), intent);
    	}
    	return super.onPreferenceTreeClick(preferenceScreen, preference);
    }
    /*prize-add-contacts-settings-item-click-event-huangliemin-2016-7-4-end*/


    private void removeUnsupportedPreferences() {
        // Disable sort order for CJK locales where it is not supported
        final Resources resources = getResources();
        if (!resources.getBoolean(R.bool.config_sort_order_user_changeable)) {
            getPreferenceScreen().removePreference(findPreference("sortOrder"));
        }

        // Disable display order for CJK locales as well
        if (!resources.getBoolean(R.bool.config_display_order_user_changeable)) {
            getPreferenceScreen().removePreference(findPreference("displayOrder"));
        }

        // Remove the "Default account" setting if there aren't any writable accounts
        final AccountTypeManager accountTypeManager = AccountTypeManager.getInstance(getContext());
        final List<AccountWithDataSet> accounts = accountTypeManager.getAccounts(
                /* contactWritableOnly */ true);
        if (accounts.isEmpty()) {
            getPreferenceScreen().removePreference(findPreference("accounts"));
        }
    }

    private void addExtraPreferences() {
        final PreferenceManager preferenceManager = ObjectFactory.getPreferenceManager(
                getContext());
        if (preferenceManager != null) {
            for (Preference preference : preferenceManager.getPreferences()) {
                getPreferenceScreen().addPreference(preference);
            }
        }
    }

    @Override
    public Context getContext() {
        return getActivity();
    }
}

