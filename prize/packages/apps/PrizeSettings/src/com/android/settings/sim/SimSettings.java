/*
 * Copyright (C) 2014 The Android Open Source Project
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

package com.android.settings.sim;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.SystemProperties;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.provider.SearchIndexableResource;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.PreferenceCategory;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;
import android.telephony.PhoneNumberUtils;
import android.provider.Settings;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.telecom.PhoneAccount;

import android.text.TextUtils;
import android.util.Log;

import com.android.internal.logging.MetricsProto.MetricsEvent;
import com.android.internal.telephony.TelephonyProperties;
import com.android.settings.R;
import com.android.internal.telephony.TelephonyIntents;
import com.android.settings.RestrictedSettingsFragment;
import com.android.settings.Utils;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;

import com.mediatek.internal.telephony.ITelephonyEx;
import com.mediatek.settings.FeatureOption;
import com.mediatek.settings.UtilsExt;
import com.mediatek.settings.cdma.CdmaUtils;
import com.mediatek.settings.ext.ISettingsMiscExt;
import com.mediatek.settings.ext.ISimManagementExt;
import com.mediatek.settings.sim.RadioPowerController;
import com.mediatek.settings.sim.RadioPowerPreference;
import com.mediatek.settings.sim.SimHotSwapHandler;
import com.mediatek.settings.sim.TelephonyUtils;
import com.mediatek.settings.sim.SimHotSwapHandler.OnSimHotSwapListener;
import com.mediatek.telecom.TelecomManagerEx;

import java.util.ArrayList;
import java.util.List;

import com.android.settings.datausage.DataUsageSummary;
import android.database.ContentObserver;
import android.os.Handler;

/**prize start pyx  2016-06-24  for mobile switch*/
import android.support.v14.preference.SwitchPreference;
import android.widget.Toast;
import java.util.Iterator;
/**prize end pyx  2016-06-24  for mobile switch*/
public class SimSettings extends RestrictedSettingsFragment implements Indexable,  Preference.OnPreferenceChangeListener {/**prize add pyx  2016-06-24  for mobile switch*/
    private static final String TAG = "SimSettings";
    private static final boolean DBG = true;

    private static final String DISALLOW_CONFIG_SIM = "no_config_sim";
    private static final String SIM_CARD_CATEGORY = "sim_cards";
    private static final String KEY_CELLULAR_DATA = "sim_cellular_data";
    private static final String KEY_CALLS = "sim_calls";
    private static final String KEY_SMS = "sim_sms";
    public static final String EXTRA_SLOT_ID = "slot_id";
    /*add by liuweiquan for v7.0 20160712 start*/
    private static final String SIM_MANAGE_CATEGORY = "sim_manage_category";
    private PreferenceCategory mSimManageCategory;
    /*add by liuweiquan for v7.0 20160712 end*/
    /*prize start liuweiquan  2016-10-08*/
    private static final String DATA_USAGE_NETWORKS_KEY = "data_usage_menu_cellular_networks";
    private static final String DEFAULT_CARD_CATEGORY = "default_card_settings";
    private Preference mDataUsageNetworksPref = null;
    private PreferenceCategory mDefaultCardCategory;
    /*prize end liuweiquan  2016-10-08*/
	 private static final String KEY_SIM_ACTIVITIES = "sim_activities";
    /**
     * By UX design we use only one Subscription Information(SubInfo) record per SIM slot.
     * mAvalableSubInfos is the list of SubInfos we present to the user.
     * mSubInfoList is the list of all SubInfos.
     * mSelectableSubInfos is the list of SubInfos that a user can select for data, calls, and SMS.
     */
    private List<SubscriptionInfo> mAvailableSubInfos = null;
    private List<SubscriptionInfo> mSubInfoList = null;
    private List<SubscriptionInfo> mSelectableSubInfos = null;
    private PreferenceScreen mSimCards = null;
    private SubscriptionManager mSubscriptionManager;
    private int mNumSlots;
    private Context mContext;

	/**prize start pyx  2016-06-24  for mobile switch*/
    private SwitchPreference mMobileDataSwitch = null;
    /**prize end pyx  2016-06-24  for mobile switch*/
	private int mPhoneCount = TelephonyManager.getDefault().getPhoneCount();
    private int[] mCallState = new int[mPhoneCount];
    private PhoneStateListener[] mPhoneStateListener = new PhoneStateListener[mPhoneCount];
	/* prize add by lijimeng BUGID:24979Synchronous Mobile Data 2016-12-1 start*/
    private Boolean SimCardClicked = true;
	private int SimCardPosition;
	/* prize add by lijimeng BUGID:24979Synchronous Mobile Data 2016-12-1 end*/
    public SimSettings() {
        super(DISALLOW_CONFIG_SIM);
    }

    @Override
    protected int getMetricsCategory() {
        return MetricsEvent.SIM;
    }

    @Override
    public void onCreate(final Bundle bundle) {
        super.onCreate(bundle);
        mContext = getActivity();

        mSubscriptionManager = SubscriptionManager.from(getActivity());
        final TelephonyManager tm =
                (TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE);
        addPreferencesFromResource(R.xml.sim_settings);
        
        mNumSlots = tm.getSimCount();
        mSimCards = (PreferenceScreen)findPreference(SIM_CARD_CATEGORY);
        mAvailableSubInfos = new ArrayList<SubscriptionInfo>(mNumSlots);
        mSelectableSubInfos = new ArrayList<SubscriptionInfo>();
        SimSelectNotification.cancelNotification(getActivity());
      /**prize start pyx  2016-06-24  for mobile switch*/
        mMobileDataSwitch = (SwitchPreference)findPreference("mobile_data");
    	mMobileDataSwitch.setOnPreferenceChangeListener(this);
		/* prize-modify-by-lijimeng-for bugid 44921-start*/
		// if(mSelectableSubInfos.size() == 1) {
			// setDefaultConfig(mSelectableSubInfos.get(0).getSubscriptionId());
		// }
		List<SubscriptionInfo> list = mSubscriptionManager.getActiveSubscriptionInfoList();
		if(list != null && list.size() == 1) {
			setDefaultConfig(list.get(0).getSubscriptionId());
		}
		/* prize-modify-by-lijimeng-for bugid 44921-end*/
		/**prize end pyx  2016-06-24  for mobile switch*/	  
    	/*prize start liuweiquan  2016-10-08*/
    	updateMoblieNetworkState();
    	/*prize end liuweiquan  2016-10-08*/
        /// M: for [SIM Hot Swap], [SIM Radio On/Off] etc.
        initForSimStateChange();

        /// M: for Plug-in @{
        mSimManagementExt = UtilsExt.getSimManagmentExtPlugin(getActivity());
        mMiscExt = UtilsExt.getMiscPlugin(getActivity());
        /// @}
		 /// M: for radio switch control
        mRadioController = RadioPowerController.getInstance(getContext());
        /// @}

        /* Add by zhudaopeng at 2016-12-05 Start */
        getActivity().setTitle(
                mMiscExt.customizeSimDisplayString(getString(R.string.mobile_networks_settings_title),
                        SubscriptionManager.INVALID_SUBSCRIPTION_ID));
        /* Add by zhudaopeng at 2016-12-05 End */
    }
    
    /**prize start pyx  2016-06-24  for mobile switch*/
    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
	if(preference == mMobileDataSwitch) {
		boolean isChecked = Boolean.parseBoolean(newValue.toString());
        final TelephonyManager tm =
	            (TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE);
		int defaultSubId = SubscriptionManager.getDefaultDataSubscriptionId();
        if(defaultSubId == -1){
            final SubscriptionInfo sir = mSubscriptionManager
                    .getActiveSubscriptionInfoForSimSlotIndex(0);
            defaultSubId = sir.getSubscriptionId();
        }

		if(mSelectableSubInfos.size() == 1) { // only one present sim card
			//defaultSubId = mSelectableSubInfos.get(0).getSubscriptionId();
		}

		if(isChecked && !mSubscriptionManager.isValidSubscriptionId(defaultSubId)) {
			//Toast.makeText(getActivity(), R.string.select_cellular_data_first_msg,
					//Toast.LENGTH_SHORT).show();
			//return false;
		}
		if(defaultSubId != -1){
            tm.setDataEnabled(defaultSubId, isChecked);
        } else {
            tm.setDataEnabled(isChecked);
        }
	}
	return true;
    }
    /**prize end pyx  2016-06-24  for mobile switch*/
    
    private final SubscriptionManager.OnSubscriptionsChangedListener mOnSubscriptionsChangeListener
            = new SubscriptionManager.OnSubscriptionsChangedListener() {
        @Override
        public void onSubscriptionsChanged() {
            if (DBG) log("onSubscriptionsChanged:");
            updateSubscriptions();
        }
    };

	/**prize start pyx  2016-06-24  for mobile switch*/
    private void setDefaultConfig(int subId) {
		Log.d("TAG","****subId =="+subId);
    	if(!mSubscriptionManager.isValidSubscriptionId(subId)) {
    		return;
    	}

    	final TelecomManager telecomManager = TelecomManager.from(getActivity());
		final TelephonyManager telephonyManager = TelephonyManager.from(getActivity());

    	if (telecomManager.isInCall()) {
    		//Toast.makeText(getActivity(), R.string.default_data_switch_err_msg1,
    				//Toast.LENGTH_SHORT).show();
    	} else {
    		if(SubscriptionManager.getDefaultDataSubscriptionId() != subId) {
            		mSubscriptionManager.setDefaultDataSubId(subId);
    		}
    	}
		/* prize-modify-by-lijimeng-for bugid 44921-start*/
		if(mSubscriptionManager.getDefaultSmsSubscriptionId() != subId) {
			mSubscriptionManager.setDefaultSmsSubId(subId);
		}

        final Iterator<PhoneAccountHandle> phoneAccounts =
                    telecomManager.getCallCapablePhoneAccounts().listIterator();
    	PhoneAccountHandle phoneAccountHandle = null;
            // while (phoneAccounts.hasNext()) {
                // phoneAccountHandle = phoneAccounts.next();
                // final PhoneAccount phoneAccount = telecomManager.getPhoneAccount(phoneAccountHandle);
                // final String phoneAccountId = phoneAccountHandle.getId();

                // if (phoneAccount.hasCapabilities(PhoneAccount.CAPABILITY_SIM_SUBSCRIPTION)
                        // && TextUtils.isDigitsOnly(phoneAccountId)
                        // && Integer.parseInt(phoneAccountId) == subId){
                    // break;
                // }
            // }
		while (phoneAccounts.hasNext()) {
            phoneAccountHandle = phoneAccounts.next();
            final PhoneAccount phoneAccount = telecomManager.getPhoneAccount(phoneAccountHandle);
            if (subId == telephonyManager.getSubIdForPhoneAccount(phoneAccount)) {
                break;
            }
        }
		/* prize-modify-by-lijimeng-for bugid 44921-end*/
		Log.d("TAG","****phoneAccountHandle =="+phoneAccountHandle);
    	if((phoneAccountHandle != null) && 
    		(phoneAccountHandle != telecomManager.getUserSelectedOutgoingPhoneAccount())) {
    		telecomManager.setUserSelectedOutgoingPhoneAccount(phoneAccountHandle);
    	}
	} 
    	/**prize end pyx  2016-06-24  for mobile switch*/  
    private void updateSubscriptions() {
        mSubInfoList = mSubscriptionManager.getActiveSubscriptionInfoList();
        for (int i = 0; i < mNumSlots; ++i) {
            Preference pref = mSimCards.findPreference("sim" + i);
            if (pref instanceof SimPreference) {
                mSimCards.removePreference(pref);
            }
        }
        mAvailableSubInfos.clear();
        mSelectableSubInfos.clear();

        for (int i = 0; i < mNumSlots; ++i) {
            final SubscriptionInfo sir = mSubscriptionManager
                    .getActiveSubscriptionInfoForSimSlotIndex(i);
            SimPreference simPreference = new SimPreference(getPrefContext(), sir, i);
            simPreference.setOrder(i-mNumSlots);
            /// M: for [SIM Radio On/Off]
            if (sir != null) {
                int subId = sir.getSubscriptionId();
                simPreference.bindRadioPowerState(subId,
                        mRadioController.isRadioSwitchComplete(subId));

            } else {
                simPreference.bindRadioPowerState(SubscriptionManager.INVALID_SUBSCRIPTION_ID,
                        mRadioController.isRadioSwitchComplete(
                                SubscriptionManager.INVALID_SUBSCRIPTION_ID));
            }

            Log.d(TAG, "addPreference slot " + i);
            mSimCards.addPreference(simPreference);
            mAvailableSubInfos.add(sir);
            if (sir != null) {
                mSelectableSubInfos.add(sir);
            }
        }
        updateAllOptions();
    }

    private void updateAllOptions() {
        updateSimSlotValues();
        updateActivitesCategory();
    }

    private void updateSimSlotValues() {
        final int prefSize = mSimCards.getPreferenceCount();
        for (int i = 0; i < prefSize; ++i) {
            Preference pref = mSimCards.getPreference(i);
            if (pref instanceof SimPreference) {
                ((SimPreference)pref).update();
            }
        }
    }

    private void updateActivitesCategory() {
        updateCellularDataValues();
        updateCallValues();
        updateSmsValues();
    }
    
	/**prize start pyx  2016-06-24  for mobile switch*/
    private void updateMoblieDataSwitchState() {
        log("[updateMoblieDataSwitchState] mMobileDataSwitch ? " + mMobileDataSwitch);
        if (mMobileDataSwitch != null) {
	        final TelephonyManager tm =
	            (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
			boolean enable = tm.getDataEnabled();
			mMobileDataSwitch.setChecked(enable);
			/* prize add by lijimeng BUGID:24979Synchronous Mobile Data 2016-12-1 start*/
			if(SimCardPosition == SubscriptionManager.getDefaultDataSubscriptionId() && !SimCardClicked){
				mMobileDataSwitch.setChecked(false);
				tm.setDataEnabled(false);
			}
			/* prize add by lijimeng BUGID:24979Synchronous Mobile Data 2016-12-1 end*/
			mMobileDataSwitch.setEnabled(!mIsAirplaneModeOn && mSelectableSubInfos.size() >= 1);
        }
    }
	/**prize end pyx  2016-06-24  for mobile switch*/

    private void updateSmsValues() {
        final Preference simPref = findPreference(KEY_SMS);
        if (simPref != null) {
            SubscriptionInfo sir = mSubscriptionManager.getDefaultSmsSubscriptionInfo();
            simPref.setTitle(R.string.sms_messages_title);
            if (DBG) log("[updateSmsValues] mSubInfoList=" + mSubInfoList);

            /// M: for plug-in
            sir = mSimManagementExt.setDefaultSubId(getActivity(), sir, KEY_SMS);

            if (sir != null) {
                /* prize-modify-by-lijimeng-for bugid 56376-20180504-start*/
               // simPref.setSummary(sir.getDisplayName());
                simPref.setSummary("SIM "+ (sir.getSimSlotIndex()+1));
                /* prize-modify-by-lijimeng-for bugid 56376-20180504-end*/
                /// M: set enable state below to join more conditions
                // simPref.setEnabled(mSelectableSubInfos.size() > 1);
            } else if (sir == null) {
                /// M: for [Always Ask]
                // simPref.setSummary(R.string.sim_selection_required_pref);
                simPref.setSummary(R.string.sim_calls_ask_first_prefs_title);
                /// M: set enable state below to join more conditions
                // simPref.setEnabled(mSelectableSubInfos.size() >= 1);
                /// M: for plug-in
                mSimManagementExt.updateDefaultSmsSummary(simPref);
            }

            /* Modify by zhudaopeng 2016-11-24 Start */
            /* boolean enabled = sir == null ? mSelectableSubInfos.size() >= 1
                    : mSelectableSubInfos.size() > 1;*/
            /* Modify by zhudaopeng 2016-11-24 End */
            simPref.setEnabled(isDataPrefEnable());
            /// M: for plug-in
            mSimManagementExt.configSimPreferenceScreen(simPref, KEY_SMS,
                       mSelectableSubInfos.size());
            mSimManagementExt.setPrefSummary(simPref, KEY_SMS);
        }
    }

    private void updateCellularDataValues() {
        final Preference simPref = findPreference(KEY_CELLULAR_DATA);
        if (simPref != null) {
            SubscriptionInfo sir = mSubscriptionManager.getDefaultDataSubscriptionInfo();
            simPref.setTitle(R.string.cellular_data_title);
            if (DBG) log("[updateCellularDataValues] mSubInfoList=" + mSubInfoList);

            /// M: for plug-in
            sir = mSimManagementExt.setDefaultSubId(getActivity(), sir, KEY_CELLULAR_DATA);

            /// M: set enable state below to join more conditions @{
            /*
            boolean callStateIdle = isCallStateIdle();
            final boolean ecbMode = SystemProperties.getBoolean(
                    TelephonyProperties.PROPERTY_INECM_MODE, false);
            */
            /// @}

            if (sir != null) {
                /* prize-modify-by-lijimeng-for bugid 56376-20180504-start*/
                //simPref.setSummary(sir.getDisplayName());
                simPref.setSummary("SIM "+ (sir.getSimSlotIndex()+1));
                /* prize-modify-by-lijimeng-for bugid 56376-20180504-end*/
                // Enable data preference in msim mode and call state idle
             /// M: set enable state below to join more conditions
             // simPref.setEnabled((mSelectableSubInfos.size() > 1) && callStateIdle && !ecbMode);
            } else if (sir == null) {
                simPref.setSummary(R.string.sim_selection_required_pref);
                // Enable data preference in msim mode and call state idle
            /// M: set enable state below to join more conditions
            // simPref.setEnabled((mSelectableSubInfos.size() >= 1) && callStateIdle && !ecbMode);
            }
            /// M: check should enable data preference by multiple conditions @{
            
            /* Modify by zhudaopeng 2016-11-24 Start */
            /* boolean defaultState = sir == null ? mSelectableSubInfos.size() >= 1
                    : mSelectableSubInfos.size() > 1;
            simPref.setEnabled(shouldEnableSimPref(defaultState));*/
            simPref.setEnabled(isDataPrefEnable());
            /* Modify by zhudaopeng 2016-11-24 End */

            mSimManagementExt.configSimPreferenceScreen(simPref, KEY_CELLULAR_DATA, -1);
            /// @}
        }
		/**prize start pyx  2016-06-24  for mobile switch*/
        updateMoblieDataSwitchState();
		/**prize end pyx  2016-06-24  for mobile switch*/
        /*prize start liuweiquan  2016-10-08*/
        updateMoblieNetworkState();
        /*prize end liuweiquan  2016-10-08*/
    }

	/*prize start liuweiquan  2016-10-08*/
    private void updateMoblieNetworkState(){
    	PreferenceCategory mPreferenceCategoryActivities =
                (PreferenceCategory) findPreference(KEY_SIM_COMMON);
    	mDataUsageNetworksPref=findPreference(DATA_USAGE_NETWORKS_KEY);
    	final boolean isOwner = ActivityManager.getCurrentUser() == UserHandle.USER_OWNER;    	
    	if(DataUsageSummary.hasReadyMobileRadio(mContext)&&isOwner){
    		mDataUsageNetworksPref.setEnabled(true);
    	}else{
    		//mPreferenceCategoryActivities.removePreference(mDataUsageNetworksPref);
    		mDataUsageNetworksPref.setEnabled(false);
    	}    	
    }
    /*prize end liuweiquan  2016-10-08*/
    private boolean isDataPrefEnable() {
        final boolean ecbMode = SystemProperties.getBoolean(
                TelephonyProperties.PROPERTY_INECM_MODE, false);
        log("isEcbMode()... isEcbMode: " + ecbMode);
        return mSelectableSubInfos.size() >= 1
                && (!TelephonyUtils.isCapabilitySwitching())
                && (!mIsAirplaneModeOn)
                && !TelecomManager.from(mContext).isInCall()
                && !ecbMode;
    }

    private void updateCallValues() {
        final Preference simPref = findPreference(KEY_CALLS);
        if (simPref != null) {
            final TelecomManager telecomManager = TelecomManager.from(mContext);
            final TelephonyManager telephonyManager = TelephonyManager.from(mContext);
            PhoneAccountHandle phoneAccount =
                telecomManager.getUserSelectedOutgoingPhoneAccount();
            final List<PhoneAccountHandle> allPhoneAccounts =
                telecomManager.getCallCapablePhoneAccounts();

            phoneAccount = mSimManagementExt.setDefaultCallValue(phoneAccount);
            log("updateCallValues allPhoneAccounts size = " + allPhoneAccounts.size()
                    + " phoneAccount =" + phoneAccount);

            simPref.setTitle(R.string.calls_title);
            /// M: for ALPS02320747 @{
            // phoneaccount may got unregistered, need to check null here
            /*
            simPref.setSummary(phoneAccount == null
                    ? mContext.getResources().getString(R.string.sim_calls_ask_first_prefs_title)
                    : (String)telecomManager.getPhoneAccount(phoneAccount).getLabel());
             */
            PhoneAccount defaultAccount = phoneAccount == null ? null : telecomManager
                    .getPhoneAccount(phoneAccount);
            /* prize-modify-by-lijimeng-for bugid 56376-20180504-start*/
            /*simPref.setSummary(defaultAccount == null
                    ? mContext.getResources().getString(R.string.sim_calls_ask_first_prefs_title)
                    : (String)defaultAccount.getLabel()); */
            simPref.setSummary(defaultAccount == null
                    ? mContext.getResources().getString(R.string.sim_calls_ask_first_prefs_title)
                    : "SIM "+ (1+SubscriptionManager.getPhoneId(telephonyManager.getSubIdForPhoneAccount(defaultAccount))));
            /* prize-modify-by-lijimeng-for bugid 56376-20180504-end*/
            /// @}
            /* Modify by zhudaopeng 2016-11-24 Start */
            /* simPref.setEnabled(allPhoneAccounts.size() > 1); */
            simPref.setEnabled(isDataPrefEnable());
            /* Modify by zhudaopeng 2016-11-24 End */
            mSimManagementExt.configSimPreferenceScreen(simPref, KEY_CALLS,
                    allPhoneAccounts.size());

            /// M: For Op01 open market. @{
            if (SystemProperties.get("ro.cmcc_light_cust_support").equals("1")) {
                 if (DBG) {
                     log("Op01 open market:set call enable if size >= 1");
                   }
                    simPref.setEnabled(allPhoneAccounts.size() >= 1);
               }
            /// M: For Op01 open market. @}
            mSimManagementExt.setPrefSummary(simPref, KEY_CALLS);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mSubscriptionManager.addOnSubscriptionsChangedListener(mOnSubscriptionsChangeListener);
        updateSubscriptions();
        /// M: fix Google bug: only listen to default sub, listen to Phone state change instead @{
        /*
        final TelephonyManager tm =
                (TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE);
        if (mSelectableSubInfos.size() > 1) {
            Log.d(TAG, "Register for call state change");
            for (int i = 0; i < mPhoneCount; i++) {
                int subId = mSelectableSubInfos.get(i).getSubscriptionId();
                tm.listen(getPhoneStateListener(i, subId),
                        PhoneStateListener.LISTEN_CALL_STATE);
            }
        }
        */
        /// @}

        /// M: for [Tablet]
        removeItemsForTablet();

        /// M: for Plug-in @{
        customizeSimDisplay();
        mSimManagementExt.onResume(getActivity());
        /// @}
		if (mSubInfoList != null) {
            for (SubscriptionInfo subInfo : mSubInfoList) {
               getActivity().getContentResolver().registerContentObserver(Settings.Global.getUriFor(Settings.Global.MOBILE_DATA+subInfo.getSubscriptionId()),false, mSimSwitchObserver);
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mSubscriptionManager.removeOnSubscriptionsChangedListener(mOnSubscriptionsChangeListener);
        /// M: Google bug: only listen to default sub, listen to Phone state change instead @{
        /*
        final TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        for (int i = 0; i < mPhoneCount; i++) {
            if (mPhoneStateListener[i] != null) {
                tm.listen(mPhoneStateListener[i], PhoneStateListener.LISTEN_NONE);
                mPhoneStateListener[i] = null;
            }
        }
        */
        ///@}

        /// M: for Plug-in
        mSimManagementExt.onPause();
		getActivity().getContentResolver().unregisterContentObserver(mSimSwitchObserver);
    }

    private PhoneStateListener getPhoneStateListener(int phoneId, int subId) {
        // Disable Sim selection for Data when voice call is going on as changing the default data
        // sim causes a modem reset currently and call gets disconnected
        // ToDo : Add subtext on disabled preference to let user know that default data sim cannot
        // be changed while call is going on
        final int i = phoneId;
        mPhoneStateListener[phoneId]  = new PhoneStateListener(subId) {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                if (DBG) log("PhoneStateListener.onCallStateChanged: state=" + state);
                mCallState[i] = state;
                updateCellularDataValues();
            }
        };
        return mPhoneStateListener[phoneId];
    }

    @Override
    public boolean onPreferenceTreeClick(final Preference preference) {
        final Context context = mContext;
        Intent intent = new Intent(context, SimDialogActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        if (preference instanceof SimPreference) {
            Intent newIntent = new Intent(context, SimPreferenceDialog.class);
            newIntent.putExtra(EXTRA_SLOT_ID, ((SimPreference)preference).getSlotId());
            startActivity(newIntent);
        } else if (findPreference(KEY_CELLULAR_DATA) == preference) {
            intent.putExtra(SimDialogActivity.DIALOG_TYPE_KEY, SimDialogActivity.DATA_PICK);
            context.startActivity(intent);
        } else if (findPreference(KEY_CALLS) == preference) {
            intent.putExtra(SimDialogActivity.DIALOG_TYPE_KEY, SimDialogActivity.CALLS_PICK);
            context.startActivity(intent);
        } else if (findPreference(KEY_SMS) == preference) {
            intent.putExtra(SimDialogActivity.DIALOG_TYPE_KEY, SimDialogActivity.SMS_PICK);
            context.startActivity(intent);
		/*prize start liuweiquan  2016-10-08*/
        } else if(mDataUsageNetworksPref == preference){
        	final Intent i = new Intent(Intent.ACTION_MAIN);
        	//i.putExtra("Settings:SimSettings", true);
            i.setComponent(new ComponentName("com.android.phone",
                    "com.android.phone.MobileNetworkSettings"));
            startActivity(i);
        }
        return true;
    }
    /// M: for [SIM Radio On/Off]
    // private class SimPreference extends Preference {
    private class SimPreference extends RadioPowerPreference{
        private SubscriptionInfo mSubInfoRecord;
        private int mSlotId;
        Context mContext;

        public SimPreference(Context context, SubscriptionInfo subInfoRecord, int slotId) {
            super(context);

            mContext = context;
            mSubInfoRecord = subInfoRecord;
            mSlotId = slotId;
            setKey("sim" + mSlotId);
            update();
        }

        public void update() {
            final Resources res = mContext.getResources();

            setTitle(String.format(mContext.getResources()
                    .getString(R.string.sim_editor_title), (mSlotId + 1)));

            /// M: for Plug-in
            customizePreferenceTitle();

            if (mSubInfoRecord != null) {
                /// M: ALPS02871084, only get phone number once
                String phoneNum = getPhoneNumber(mSubInfoRecord);
                log("phoneNum = " + phoneNum);
                //if (TextUtils.isEmpty(getPhoneNumber(mSubInfoRecord))) {
                if (TextUtils.isEmpty(phoneNum)) {
                    setSummary(mSubInfoRecord.getDisplayName());
                } else {
                    //setSummary(mSubInfoRecord.getDisplayName() + " - " +
                    //        PhoneNumberUtils.createTtsSpannable(getPhoneNumber(mSubInfoRecord)));
                    setSummary(mSubInfoRecord.getDisplayName() + " - " +
                            PhoneNumberUtils.createTtsSpannable(phoneNum));
                    setEnabled(true);
                }
              //  setIcon(new BitmapDrawable(res, (mSubInfoRecord.createIconBitmap(mContext))));
                /// M: add for radio on/off @{
                int subId = mSubInfoRecord.getSubscriptionId();
                setRadioEnabled(!mIsAirplaneModeOn
                        && mRadioController.isRadioSwitchComplete(subId));
                if (mRadioController.isRadioSwitchComplete(subId)) {
                    setRadioOn(TelephonyUtils.isRadioOn(subId, getContext()));
                }

                /// @}
            } else {
                setSummary(R.string.sim_slot_empty);
                setFragment(null);
                setEnabled(false);
            }
        }

        private int getSlotId() {
            return mSlotId;
        }

        /**
         * only for plug-in, change "SIM" to "UIM/SIM".
         */
        private void customizePreferenceTitle() {
            int subId = SubscriptionManager.INVALID_SUBSCRIPTION_ID;
            if (mSubInfoRecord != null) {
                subId = mSubInfoRecord.getSubscriptionId();
            }
            setTitle(String.format(mMiscExt.customizeSimDisplayString(mContext.getResources()
                    .getString(R.string.sim_editor_title), subId), (mSlotId + 1)));
        }
    }

    // Returns the line1Number. Line1number should always be read from TelephonyManager since it can
    // be overridden for display purposes.
    private String getPhoneNumber(SubscriptionInfo info) {
        final TelephonyManager tm =
            (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        return tm.getLine1Number(info.getSubscriptionId());
    }

    private void log(String s) {
        Log.d(TAG, s);
    }

    /**
     * For search
     */
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {
                @Override
                public List<SearchIndexableResource> getXmlResourcesToIndex(Context context,
                        boolean enabled) {
                    ArrayList<SearchIndexableResource> result =
                            new ArrayList<SearchIndexableResource>();

                    if (Utils.showSimCardTile(context)) {
                        SearchIndexableResource sir = new SearchIndexableResource(context);
                        sir.xmlResId = R.xml.sim_settings;
                        result.add(sir);
                    }

                    return result;
                }
            };

    private boolean isCallStateIdle() {
        boolean callStateIdle = true;
        for (int i = 0; i < mCallState.length; i++) {
            if (TelephonyManager.CALL_STATE_IDLE != mCallState[i]) {
                callStateIdle = false;
            }
        }
        Log.d(TAG, "isCallStateIdle " + callStateIdle);
        return callStateIdle;
    }

     ///----------------------------------------MTK-----------------------------------------------

    private static final String KEY_SIM_COMMON = "sim_common";

    // / M: for Plug in @{
    private ISettingsMiscExt mMiscExt;
    private ISimManagementExt mSimManagementExt;
    // / @}

    private ITelephonyEx mTelephonyEx;
    private SimHotSwapHandler mSimHotSwapHandler;
    private boolean mIsAirplaneModeOn = false;

    private RadioPowerController mRadioController;
    // Receiver to handle different actions
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "mReceiver action = " + action);
            if (action.equals(Intent.ACTION_AIRPLANE_MODE_CHANGED)) {
                handleAirplaneModeChange(intent);
            } else if (action.equals(TelephonyIntents.ACTION_DEFAULT_DATA_SUBSCRIPTION_CHANGED)) {
                updateCellularDataValues();
            } else if (action.equals(TelecomManagerEx.ACTION_DEFAULT_ACCOUNT_CHANGED)
                    || action.equals(TelecomManagerEx.ACTION_PHONE_ACCOUNT_CHANGED)) {
                updateCallValues();
            } else if (action.equals(TelephonyIntents.ACTION_SET_RADIO_CAPABILITY_DONE)
                    || action.equals(TelephonyIntents.ACTION_SET_RADIO_CAPABILITY_FAILED)) {
                updateActivitesCategory();
            } else if (action.equals(TelephonyManager.ACTION_PHONE_STATE_CHANGED)) {
                updateActivitesCategory();
            // listen to radio state change
            } else if (action.equals(TelephonyIntents.ACTION_RADIO_STATE_CHANGED)) {
                int subId = intent.getIntExtra("subId", -1);
                if (mRadioController.isRadioSwitchComplete(subId)) {
                    handleRadioPowerSwitchComplete();
                }
            /* prize add by lijimeng BUGID:24979Synchronous Mobile Data 2016-12-1 start*/
            }else if(action.equals("SELECTED_SIM_CARD")){
		        SimCardClicked = intent.getBooleanExtra("SimSelected", true);
		        SimCardPosition = intent.getIntExtra("SimCardId", 1);
				updateMoblieDataSwitchState();
			}else if(action.equals("android.net.conn.CONNECTIVITY_CHANGE")){
				final TelephonyManager tm =(TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
				boolean enable = tm.getDataEnabled();
				if(!enable){
					if(mMobileDataSwitch.isChecked()){
					mMobileDataSwitch.setChecked(enable);
					mMobileDataSwitch.setEnabled(!mIsAirplaneModeOn && mSelectableSubInfos.size() >= 1);
					}
				}else{
					mMobileDataSwitch.setChecked(enable);
				}
			}
			/* prize add by lijimeng BUGID:24979Synchronous Mobile Data 2016-12-1 end*/
        }
    };

    /**
     * init for sim state change, including SIM hot swap, airplane mode, etc.
     */
    private void initForSimStateChange() {
        mTelephonyEx = ITelephonyEx.Stub.asInterface(
                ServiceManager.getService(Context.TELEPHONY_SERVICE_EX));
        /// M: for [SIM Hot Swap] @{
        mSimHotSwapHandler = new SimHotSwapHandler(getActivity().getApplicationContext());
        mSimHotSwapHandler.registerOnSimHotSwap(new OnSimHotSwapListener() {
            @Override
            public void onSimHotSwap() {
                if (getActivity() != null) {
                    log("onSimHotSwap, finish Activity~~");
                    getActivity().finish();
                }
            }
        });
        /// @}

        mIsAirplaneModeOn = TelephonyUtils.isAirplaneModeOn(getActivity().getApplicationContext());
        Log.d(TAG, "init()... airplane mode is: " + mIsAirplaneModeOn);

        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        intentFilter.addAction(TelephonyIntents.ACTION_DEFAULT_DATA_SUBSCRIPTION_CHANGED);
        // For PhoneAccount
        intentFilter.addAction(TelecomManagerEx.ACTION_DEFAULT_ACCOUNT_CHANGED);
        intentFilter.addAction(TelecomManagerEx.ACTION_PHONE_ACCOUNT_CHANGED);
        // For radio on/off
        intentFilter.addAction(TelephonyIntents.ACTION_SET_RADIO_CAPABILITY_DONE);
        intentFilter.addAction(TelephonyIntents.ACTION_SET_RADIO_CAPABILITY_FAILED);

		/* prize add by lijimeng BUGID:24979Synchronous Mobile Data 2016-12-1 start*/
		intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
		intentFilter.addAction("SELECTED_SIM_CARD");
		/* prize add by lijimeng BUGID:24979Synchronous Mobile Data 2016-12-1 end*/
        // listen to radio state
        intentFilter.addAction(TelephonyIntents.ACTION_RADIO_STATE_CHANGED);
        // listen to PHONE_STATE_CHANGE
        intentFilter.addAction(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
        getActivity().registerReceiver(mReceiver, intentFilter);
    }

    /**
     * update SIM values after radio switch
     */
    private void handleRadioPowerSwitchComplete() {
        if(isResumed()) {
            updateSimSlotValues();
        }
        // for plug-in
        mSimManagementExt.showChangeDataConnDialog(this, isResumed());
    }

    /**
     * When airplane mode is on, some parts need to be disabled for prevent some telephony issues
     * when airplane on.
     * Default data is not able to switch as may cause modem switch
     * SIM radio power switch need to disable, also this action need operate modem
     * @param airplaneOn airplane mode state true on, false off
     */
    private void handleAirplaneModeChange(Intent intent) {
        mIsAirplaneModeOn = intent.getBooleanExtra("state", false);
        Log.d(TAG, "airplane mode is = " + mIsAirplaneModeOn);
        updateSimSlotValues();
        updateActivitesCategory();
        removeItemsForTablet();
    }

    /**
     * remove unnecessary items for tablet
     */
    private void removeItemsForTablet() {
        // remove some item when in 4gds wifi-only
        if (FeatureOption.MTK_PRODUCT_IS_TABLET) {
            Preference sim_call_Pref = findPreference(KEY_CALLS);
            Preference sim_sms_Pref = findPreference(KEY_SMS);
            Preference sim_data_Pref = findPreference(KEY_CELLULAR_DATA);
            PreferenceCategory mPreferenceCategoryActivities =
                (PreferenceCategory) findPreference(KEY_SIM_COMMON);
            TelephonyManager tm = TelephonyManager.from(getActivity());
            if (!tm.isSmsCapable() && sim_sms_Pref != null) {
                mPreferenceCategoryActivities.removePreference(sim_sms_Pref);
            }
            if (!tm.isMultiSimEnabled() && sim_data_Pref != null && sim_sms_Pref != null) {
                mPreferenceCategoryActivities.removePreference(sim_data_Pref);
                mPreferenceCategoryActivities.removePreference(sim_sms_Pref);
            }
            if (!tm.isVoiceCapable() && sim_call_Pref != null) {
                mPreferenceCategoryActivities.removePreference(sim_call_Pref);
            }
        }
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy()");
        getActivity().unregisterReceiver(mReceiver);
        mSimHotSwapHandler.unregisterOnSimHotSwap();
        super.onDestroy();
    }

    /**
     * only for plug-in, change "SIM" to "UIM/SIM".
     */
    private void customizeSimDisplay() {
        if (mSimCards != null) {
            mSimCards.setTitle(mMiscExt.customizeSimDisplayString(
                    getString(R.string.sim_settings_title),
                    SubscriptionManager.INVALID_SUBSCRIPTION_ID));
        }
        /* Modify by zhudaopeng at 2016-12-05 Start */
        /* getActivity().setTitle(
                mMiscExt.customizeSimDisplayString(getString(R.string.sim_settings_title),
                        SubscriptionManager.INVALID_SUBSCRIPTION_ID));*/
        /* Modify by zhudaopeng at 2016-12-05 End */
    }


    private boolean shouldEnableSimPref(boolean defaultState) {
        String ecbMode = SystemProperties.get(TelephonyProperties.PROPERTY_INECM_MODE, "false");
        boolean isInEcbMode = false;
        if (ecbMode != null && ecbMode.contains("true")) {
            isInEcbMode = true;
        }
        boolean capSwitching = TelephonyUtils.isCapabilitySwitching();
        boolean inCall = TelecomManager.from(mContext).isInCall();

        log("defaultState :" + defaultState + ", capSwitching :"
                + capSwitching + ", airplaneModeOn :" + mIsAirplaneModeOn + ", inCall :"
                + inCall + ", ecbMode: " + ecbMode);
        return defaultState && !capSwitching && !mIsAirplaneModeOn && !inCall && !isInEcbMode;
    }

	private ContentObserver mSimSwitchObserver = new ContentObserver(new Handler()) {
       @Override
       public void onChange(boolean selfChange) {
		  if (mSubInfoList != null) {
			for (SubscriptionInfo subInfo : mSubInfoList) {
				if(subInfo.getSubscriptionId() == SubscriptionManager.getDefaultDataSubscriptionId()){
					final boolean enabled = Settings.Global.getInt(getActivity().getContentResolver(),Settings.Global.MOBILE_DATA+subInfo.getSubscriptionId(), 0) == 1;
					mMobileDataSwitch.setChecked(enabled);
					Log.d(TAG, "enabled: " + enabled+ "defaultDataSubId:" + subInfo.getSubscriptionId());
				}
            }
        }
       }
    };
}
