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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.DialogInterface.OnDismissListener;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.SystemProperties;
import android.telecom.PhoneAccount;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.android.settings.R;
import com.mediatek.internal.telephony.DefaultSmsSimSettings;
/// M: Add for CT 6M.
import com.mediatek.settings.FeatureOption;
import com.mediatek.settings.UtilsExt;
import com.mediatek.settings.cdma.CdmaUtils;
import com.mediatek.settings.cdma.OmhEventHandler;
import com.mediatek.settings.ext.IRCSSettings;
import com.mediatek.settings.ext.ISettingsMiscExt;
import com.mediatek.settings.ext.ISimManagementExt;
import com.mediatek.settings.sim.SimHotSwapHandler;
import com.mediatek.settings.sim.SimHotSwapHandler.OnSimHotSwapListener;
import com.mediatek.settings.sim.TelephonyUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SimDialogActivity extends Activity {
    private static String TAG = "SimDialogActivity";

    public static String PREFERRED_SIM = "preferred_sim";
    public static String DIALOG_TYPE_KEY = "dialog_type";
    public static final int INVALID_PICK = -1;
    public static final int DATA_PICK = 0;
    public static final int CALLS_PICK = 1;
    public static final int SMS_PICK = 2;
    public static final int PREFERRED_PICK = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Bundle extras = getIntent().getExtras();

        /// M: [SIM Hot Swap]
        setSimStateCheck();

        /// M: for plug-in @{
        mSimManagementExt = UtilsExt.getSimManagmentExtPlugin(getApplicationContext());
        mMiscExt = UtilsExt.getMiscPlugin(getApplicationContext());
        mRCSExt = UtilsExt.getRcsSettingsPlugin(getApplicationContext());
        /// @}

        final int dialogType = extras.getInt(DIALOG_TYPE_KEY, INVALID_PICK);

        switch (dialogType) {
            case DATA_PICK:
            case CALLS_PICK:
            case SMS_PICK:
                /// M: for AlPS02113443, avoid window leak @{
                // createDialog(this, dialogType).show();
                /// M: for ALPS02463456, activity state chaos, add log to check,
                // can be removed if not happen again @{
                if (isFinishing()) {
                    Log.e(TAG, "Activity Finishing!");
                }
                /// @}

                mDialog = createDialog(this, dialogType);
                mDialog.show();
                /// @}
                break;
            case PREFERRED_PICK:
                /// M: for ALPS02423087, hot plug timing issue, the sub list may already changed @{
                List<SubscriptionInfo> subs = SubscriptionManager.from(this)
                        .getActiveSubscriptionInfoList();
                if (subs == null || subs.size() != 1) {
                    Log.w(TAG, "Subscription count is not 1, skip preferred SIM dialog");
                    finish();
                    return;
                }
                /// @}
                displayPreferredDialog(extras.getInt(PREFERRED_SIM));
                break;
            default:
                throw new IllegalArgumentException("Invalid dialog type " + dialogType + " sent.");
        }
    }

    private void displayPreferredDialog(final int slotId) {
        final Resources res = getResources();
        final Context context = getApplicationContext();
        final SubscriptionInfo sir = SubscriptionManager.from(context)
                .getActiveSubscriptionInfoForSimSlotIndex(slotId);

        if (sir != null) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

            /// M: for Plug-in @{
            /*
            alertDialogBuilder.setTitle(R.string.sim_preferred_title);
            alertDialogBuilder.setMessage(res.getString(
                        R.string.sim_preferred_message, sir.getDisplayName()));
            */
            int subId = SubscriptionManager.getSubIdUsingPhoneId(slotId);
            String title = mMiscExt.customizeSimDisplayString(res
                    .getString(R.string.sim_preferred_title), subId);
            String message = mMiscExt.customizeSimDisplayString(res.getString(
                    R.string.sim_preferred_message, sir.getDisplayName()), subId);
            alertDialogBuilder.setTitle(title);
            alertDialogBuilder.setMessage(message);
            /// @}

            alertDialogBuilder.setPositiveButton(R.string.yes, new
                    DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    final int subId = sir.getSubscriptionId();
                    PhoneAccountHandle phoneAccountHandle =
                            subscriptionIdToPhoneAccountHandle(subId);
                    setDefaultDataSubId(context, subId);
                    setDefaultSmsSubId(context, subId);
                    setUserSelectedOutgoingPhoneAccount(phoneAccountHandle);
                    /// M: Add dismiss dialog before finish to void screen flash.
                    dismissSimDialog();
                    finish();
                }
            });
            alertDialogBuilder.setNegativeButton(R.string.no, new
                    DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog,int id) {
                    /// M: Add dismiss dialog before finish to void screen flash.
                    dismissSimDialog();
                    finish();
                }
            });

            /// M: when dialog dismissed, finish the activity as well @{
            alertDialogBuilder.setOnDismissListener(new OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    finish();
                }
            });
            /// @}

            /// M: for ALPS02422990 avoid window leak @{
            mDialog = alertDialogBuilder.create();
            mDialog.show();
            /// @}
        } else {
            finish();
        }
    }

    private void setDefaultDataSubId(final Context context, final int subId) {
        Log.d(TAG, "setDefaultDataSubId, sub = " + subId);
        final SubscriptionManager subscriptionManager = SubscriptionManager.from(context);
        /// M: for plug-in, need to call before setDefaultDataSubId
        mSimManagementExt.setDataState(subId);
        subscriptionManager.setDefaultDataSubId(subId);
        /// M: for plug-in, need to call after setDefaultDataSubId
        mSimManagementExt.setDataStateEnable(subId);
        /// M: for plug-in @{
        // Toast.makeText(context, R.string.data_switch_started, Toast.LENGTH_LONG).show();
        mDataSub = subId;
        /// @}
    }

    private static void setDefaultSmsSubId(final Context context, final int subId) {
        Log.d(TAG, "setDefaultSmsSubId, sub = " + subId);
        final SubscriptionManager subscriptionManager = SubscriptionManager.from(context);
        subscriptionManager.setDefaultSmsSubId(subId);
    }

    private void setUserSelectedOutgoingPhoneAccount(PhoneAccountHandle phoneAccount) {
        Log.d(TAG, "setUserSelectedOutgoingPhoneAccount phoneAccount = " + phoneAccount);
        final TelecomManager telecomManager = TelecomManager.from(this);
        telecomManager.setUserSelectedOutgoingPhoneAccount(phoneAccount);
    }

    private PhoneAccountHandle subscriptionIdToPhoneAccountHandle(final int subId) {
        final TelecomManager telecomManager = TelecomManager.from(this);
        final TelephonyManager telephonyManager = TelephonyManager.from(this);
        final Iterator<PhoneAccountHandle> phoneAccounts =
                telecomManager.getCallCapablePhoneAccounts().listIterator();

        while (phoneAccounts.hasNext()) {
            final PhoneAccountHandle phoneAccountHandle = phoneAccounts.next();
            final PhoneAccount phoneAccount = telecomManager.getPhoneAccount(phoneAccountHandle);
            if (subId == telephonyManager.getSubIdForPhoneAccount(phoneAccount)) {
                return phoneAccountHandle;
            }
        }

        return null;
    }

    public Dialog createDialog(final Context context, final int id) {
        final ArrayList<String> list = new ArrayList<String>();
        final SubscriptionManager subscriptionManager = SubscriptionManager.from(context);
        final List<SubscriptionInfo> subInfoList =
            subscriptionManager.getActiveSubscriptionInfoList();
        final int selectableSubInfoLength = subInfoList == null ? 0 : subInfoList.size();

        final DialogInterface.OnClickListener selectionListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int value) {
                        final SubscriptionInfo sir;
                        // set the network setting as current network
                        // if the current network option is set, then no further handling needed
                        // Return if handled by plugin
                        if (mSimManagementExt.setCurrentNetwork(id, value)) {
                            Log.d(TAG, "finish() ,onclick handled by setCurrentNetwork in Plugin");
                            finish();
                            return;
                        }

                        switch (id) {
                            case DATA_PICK:
								/*prize-add-by-lijimeng-for bugid 39089-20170920-start*/
								final TelephonyManager tm =(TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
								boolean enable = tm.getDataEnabled();
								/*prize-add-by-lijimeng-for bugid 39089-20170920-end*/
                                sir = subInfoList.get(value);
                                /// M: for plug-in
                                int targetSub = (sir == null ? null : sir.getSubscriptionId());
                                if (!mSimManagementExt.switchDefaultDataSub(context, targetSub)) {
                                    /// M: [C2K solution 2 enhancement] @{
                                    if (CdmaUtils.isCdmaCardCompetionForData(context)) {
                                        int defaultId = SubscriptionManager
                                        .getDefaultDataSubscriptionId();
                                        Log.d(TAG, "currnt default Id is: " + defaultId
                                                + " ,target Id: " + targetSub);

                                        if (defaultId != targetSub) {
                                            if (TelecomManager.from(context).isInCall()) {
                                                Toast.makeText(context,
                                                        R.string.default_data_switch_err_msg1,
                                                        Toast.LENGTH_SHORT).show();
                                            } else {
                                                setDefaultDataSubId(context, targetSub);
                                            }
                                        }
                                        /// @}
                                    /// M: for [C2K OMH Warning] @{
                                    } else if (CdmaUtils.isNonOmhSimInOmhDevice(targetSub)) {
                                        OmhEventHandler.getInstance(context).obtainMessage(
                                                OmhEventHandler.NEW_REQUEST,
                                                OmhEventHandler.TYPE_OMH_DATA_PICK, targetSub)
                                                .sendToTarget();
                                        /// @}
                                    } else {
                                        setDefaultDataSubId(context, targetSub);
										if(enable){
											tm.setDataEnabled(targetSub,enable);
										}
                                    }
                                }
                                break;
                            case CALLS_PICK:
                                final TelecomManager telecomManager =
                                        TelecomManager.from(context);
                                final List<PhoneAccountHandle> phoneAccountsList =
                                        telecomManager.getCallCapablePhoneAccounts();
                                Log.d(TAG, "phoneAccountsList = " + phoneAccountsList.toString());

                                /// M: for plug-in
                                value = mSimManagementExt.customizeValue(value);
                                /// M: add for CMCC OM project @{
                                if (SystemProperties.
                                        get("ro.cmcc_light_cust_support").equals("1")) {
                                    Log.d(TAG, "CMCC OM project value = " + value);
                                    TelephonyManager.MultiSimVariants mSimConfig = TelephonyManager
                                            .from(context).getMultiSimConfiguration();
                                    if ((value == 0) && (SubscriptionManager.from(context)
                                        .getActiveSubscriptionInfoCount() == 1) &&
                                        (mSimConfig == TelephonyManager.MultiSimVariants.DSDS ||
                                        mSimConfig == TelephonyManager.MultiSimVariants.DSDA)) {
                                            value = 1;
                                    }
                                }
                                /// @}

                                /// M: for ALPS02320816 @{
                                // phone account may changed in background
                                if (value > phoneAccountsList.size()) {
                                    Log.w(TAG, "phone account changed, do noting! value = " +
                                            value + ", phone account size = " +
                                            phoneAccountsList.size());
                                    break;
                                }
                                /// @}
								if(phoneAccountsList.size() == 1){
									setUserSelectedOutgoingPhoneAccount(phoneAccountsList.get(0));
								}else{
									setUserSelectedOutgoingPhoneAccount(value < 1 ?
                                        null : phoneAccountsList.get(value - 1));
								}
                                break;
                            case SMS_PICK:
                                /// M: for [SMS Always Ask]
                                // sir = subInfoList.get(value);
                                int subId = getPickSmsDefaultSub(subInfoList, value);
                                setDefaultSmsSubId(context, subId);

                                break;
                            default:
                                throw new IllegalArgumentException("Invalid dialog type "
                                        + id + " in SIM dialog.");
                        }
                        /// M: Add dismiss dialog before finish to void screen flash.
                        dismissSimDialog();
                        finish();
                    }
                };

        Dialog.OnKeyListener keyListener = new Dialog.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface arg0, int keyCode,
                    KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        finish();
                    }
                    return true;
                }
            };

        ArrayList<SubscriptionInfo> callsSubInfoList = new ArrayList<SubscriptionInfo>();
        /// M: for [SMS Always Ask] @{
        ArrayList<SubscriptionInfo> smsSubInfoList = new ArrayList<SubscriptionInfo>();
        /// @}
        if (id == CALLS_PICK) {
            final TelecomManager telecomManager = TelecomManager.from(context);
            final TelephonyManager telephonyManager = TelephonyManager.from(context);
            final Iterator<PhoneAccountHandle> phoneAccounts =
                    telecomManager.getCallCapablePhoneAccounts().listIterator();

            /// M: only for multiple accounts
            mSimManagementExt.updateList(list, callsSubInfoList,
                            telecomManager.getCallCapablePhoneAccounts().size());
            int accountSize = telecomManager.getCallCapablePhoneAccounts().size();
            Log.d(TAG, "phoneAccounts size = " + accountSize);
            if (accountSize > 1) {
                list.add(getResources().getString(R.string.sim_calls_ask_first_prefs_title));
                callsSubInfoList.add(null);
            }

            while (phoneAccounts.hasNext()) {
                final PhoneAccount phoneAccount =
                        telecomManager.getPhoneAccount(phoneAccounts.next());
                /// M: for ALPS02362894, seldom happened that phone account
                // unregistered in the background @{
                if (phoneAccount == null) {
                    Log.d(TAG, "phoneAccount is null");
                    continue;
                }
                /// @}
                list.add((String)phoneAccount.getLabel());
                int subId = telephonyManager.getSubIdForPhoneAccount(phoneAccount);
                Log.d(TAG, "phoneAccount label = " + phoneAccount.getLabel()
                        + ", subId = " + subId);
                if (subId != SubscriptionManager.INVALID_SUBSCRIPTION_ID) {
                    final SubscriptionInfo sir = SubscriptionManager.from(context)
                            .getActiveSubscriptionInfo(subId);
                    callsSubInfoList.add(sir);
                } else {
                    callsSubInfoList.add(null);
                }
            }
            Log.d(TAG, "callsSubInfoList = " + callsSubInfoList + ", list = " + list);
            /// M:for plug-in @{
            mSimManagementExt.customizeListArray(list);
            mSimManagementExt.customizeSubscriptionInfoArray(callsSubInfoList);
            /// @}
        /// M: for [SMS Always Ask] @{
        } else if (id == SMS_PICK) {
            setupSmsSubInfoList(list, subInfoList, selectableSubInfoLength, smsSubInfoList);
        /// @}
        } else {
            for (int i = 0; i < selectableSubInfoLength; ++i) {
                final SubscriptionInfo sir = subInfoList.get(i);
                CharSequence displayName = sir.getDisplayName();
                if (displayName == null) {
                    displayName = "";
                }
                list.add(displayName.toString());
            }
        }

        String[] arr = list.toArray(new String[0]);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        ListAdapter adapter = new SelectAccountListAdapter(
                /// M: for [SMS Always Ask] @{
                // id == CALLS_PICK ? callsSubInfoList : subInfoList,
                getAdapterData(id, subInfoList, callsSubInfoList, smsSubInfoList),
                /// @}
                builder.getContext(),
                R.layout.select_account_list_item,
                arr, id);

        switch (id) {
            case DATA_PICK:
                builder.setTitle(R.string.select_sim_for_data);
                break;
            case CALLS_PICK:
                builder.setTitle(R.string.select_sim_for_calls);
                break;
            case SMS_PICK:
                builder.setTitle(R.string.sim_card_select_title);
                break;
            default:
                throw new IllegalArgumentException("Invalid dialog type "
                        + id + " in SIM dialog.");
        }

        /// M: for plug-in
        changeDialogTitle(builder, id);

        Dialog dialog = builder.setAdapter(adapter, selectionListener).create();
        dialog.setOnKeyListener(keyListener);

        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                finish();
            }
        });

        return dialog;

    }

    private class SelectAccountListAdapter extends ArrayAdapter<String> {
        private Context mContext;
        private int mResId;
        private int mDialogId;
        private final float OPACITY = 0.54f;
        private List<SubscriptionInfo> mSubInfoList;

        public SelectAccountListAdapter(List<SubscriptionInfo> subInfoList,
                Context context, int resource, String[] arr, int dialogId) {
            super(context, resource, arr);
            mContext = context;
            mResId = resource;
            mDialogId = dialogId;
            mSubInfoList = subInfoList;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater)
                    mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView;
            final ViewHolder holder;

            if (convertView == null) {
                // Cache views for faster scrolling
                rowView = inflater.inflate(mResId, null);
                holder = new ViewHolder();
                holder.title = (TextView) rowView.findViewById(R.id.title);
                holder.summary = (TextView) rowView.findViewById(R.id.summary);
                holder.icon = (ImageView) rowView.findViewById(R.id.icon);
                rowView.setTag(holder);
            } else {
                rowView = convertView;
                holder = (ViewHolder) rowView.getTag();
            }

            final SubscriptionInfo sir = mSubInfoList.get(position);
            if (sir == null) {
                holder.title.setText(getItem(position));
                holder.summary.setText("");
				holder.summary.setVisibility(View.GONE);
                /// M: display icon for non-sub accounts @{
                if (mDialogId == CALLS_PICK) {
                    setPhoneAccountIcon(holder, position);
                } else {
                /// @}
                    holder.icon.setImageDrawable(getResources()
                            .getDrawable(R.drawable.ic_live_help));
                }
                /// @}
                /// M: for plug-in
                mSimManagementExt.setSmsAutoItemIcon(holder.icon, mDialogId, position);
                mSimManagementExt.setCurrNetworkIcon(holder.icon, mDialogId, position);
                holder.icon.setAlpha(OPACITY);
            } else {
                holder.title.setText(sir.getDisplayName());
				String number = sir.getNumber();
				if(number != null && number.trim().length() == 0){
					holder.summary.setVisibility(View.GONE);
				}else if(number == null){
					holder.summary.setVisibility(View.GONE);
				}else{
					holder.summary.setVisibility(View.VISIBLE);
				}
                holder.summary.setText(sir.getNumber());
                holder.icon.setImageBitmap(sir.createIconBitmap(mContext));
                /// M: when item numbers is over the screen, should set alpha 1.0f.
                holder.icon.setAlpha(1.0f);
            }
            return rowView;
        }

        private class ViewHolder {
            TextView title;
            TextView summary;
            ImageView icon;
        }

        private void setPhoneAccountIcon(ViewHolder holder, int location) {
            Log.d(TAG, "setSipAccountBitmap()... location: " + location);
            String askFirst = getResources().getString(R.string.sim_calls_ask_first_prefs_title);
            String lableString = getItem(location);
            final TelecomManager telecomManager = TelecomManager.from(mContext);
            List<PhoneAccountHandle> phoneAccountHandles=
                    telecomManager.getCallCapablePhoneAccounts();
            if (!askFirst.equals(lableString)) {
                if (phoneAccountHandles.size() > 1) {
                    location = location - 1;
                }
                PhoneAccount phoneAccount = null;
                if (location >= 0 && location < phoneAccountHandles.size()) {
                    phoneAccount =
                            telecomManager.getPhoneAccount(phoneAccountHandles.get(location));
                }
                Log.d(TAG, "setSipAccountBitmap()... position: " + location
                        + " account: "  + phoneAccount);
                if (phoneAccount != null) {
                    holder.icon.setImageDrawable(phoneAccount.getIcon().loadDrawable(mContext));
                }
            } else {
                holder.icon.setImageDrawable(getResources().getDrawable(R.drawable.ic_live_help));
            }
        }
    }

    ///-----------------------------------------MTK-----------------------------------------------

    /// M: for [SIM Hot Swap]
    private SimHotSwapHandler mSimHotSwapHandler;
    private ISimManagementExt mSimManagementExt;
    private ISettingsMiscExt mMiscExt;
    private Dialog mDialog;
    private IRCSSettings mRCSExt;
    ///M: ALPS02783578, add it for avoid screen flash
    private int mDataSub = SubscriptionManager.INVALID_SUBSCRIPTION_ID;

    // Receiver to handle different actions
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "onReceive, action = " + action);
            dismissSimDialog();
            finish();
        }
    };

    private void setSimStateCheck() {
        /// M: for [SIM Hot Swap] @{
        mSimHotSwapHandler = new SimHotSwapHandler(getApplicationContext());
        mSimHotSwapHandler.registerOnSimHotSwap(new OnSimHotSwapListener() {
            @Override
            public void onSimHotSwap() {
                Log.d(TAG, "onSimHotSwap, finish Activity");
                dismissSimDialog();
                finish();
            }
        });
        /// @}
        IntentFilter itentFilter = new IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        registerReceiver(mReceiver, itentFilter);
    }

    private void unsetSimStateCheck() {
        /// M: for [SIM Hot Swap]
        mSimHotSwapHandler.unregisterOnSimHotSwap();
        unregisterReceiver(mReceiver);
    }

    @Override
    protected void onPause() {
        /// M: for [C2K OMH Warning]
        OmhEventHandler.getInstance(this).sendEmptyMessage(OmhEventHandler.CLEAR_BUSY);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        unsetSimStateCheck();

        // M: for AlPS02113443, avoid window leak.
        dismissSimDialog();
        /// M: Show toast when destory to void screen flash.
        if (mDataSub != SubscriptionManager.INVALID_SUBSCRIPTION_ID) {
            Toast.makeText(
                    this,
                    mMiscExt.customizeSimDisplayString(getResources().getString(
                            R.string.data_switch_started), mDataSub), Toast.LENGTH_LONG).show();
        }
        super.onDestroy();
    }

    private int getPickSmsDefaultSub(final List<SubscriptionInfo> subInfoList,
            int value) {
        int subId = SubscriptionManager.INVALID_SUBSCRIPTION_ID;
        /// M: for plug-in
        value = mSimManagementExt.customizeValue(value);

        if (value < 1) {
            int length = subInfoList == null ? 0 : subInfoList.size();
            if (length == 1) {
                subId = subInfoList.get(value).getSubscriptionId();
            } else {
                subId = DefaultSmsSimSettings.ASK_USER_SUB_ID;
            }
        } else if (value >= 1 && value < subInfoList.size() + 1) {
            subId = subInfoList.get(value - 1).getSubscriptionId();
        } else {
            /// M: for plug-in
            subId = mSimManagementExt.getDefaultSmsSubIdForAuto();
        }
        subId = mRCSExt.getDefaultSmsClickContentExt(subInfoList, value, subId);
        Log.d(TAG, "getPickSmsDefaultSub, value: " + value + ", subId: " + subId);
        return subId;
    }

    private void setupSmsSubInfoList(final ArrayList<String> list,
            final List<SubscriptionInfo> subInfoList, final int selectableSubInfoLength,
            ArrayList<SubscriptionInfo> smsSubInfoList) {

            mSimManagementExt.updateList(list, smsSubInfoList,  selectableSubInfoLength);
        if ((selectableSubInfoLength > 1) && (mRCSExt.isNeedAskFirstItemForSms())) {
            list.add(getResources().getString(R.string.sim_calls_ask_first_prefs_title));
            smsSubInfoList.add(null);
        }
        for (int i = 0; i < selectableSubInfoLength; ++i) {
            final SubscriptionInfo sir = subInfoList.get(i);
            smsSubInfoList.add(sir);
            CharSequence displayName = sir.getDisplayName();
            if (displayName == null) {
                displayName = "";
            }
            list.add(displayName.toString());
        }
        /// M: for plug-in @{
        mSimManagementExt.customizeListArray(list);
        mSimManagementExt.customizeSubscriptionInfoArray(smsSubInfoList);
        mSimManagementExt.initAutoItemForSms(list, smsSubInfoList);
        /// @}
    }

    private List<SubscriptionInfo> getAdapterData(final int id,
            final List<SubscriptionInfo> subInfoList, ArrayList<SubscriptionInfo> callsSubInfoList,
            ArrayList<SubscriptionInfo> smsSubInfoList) {
        List<SubscriptionInfo> listForAdpter = null;
        switch (id) {
            case DATA_PICK:
                listForAdpter = subInfoList;
                break;
            case CALLS_PICK:
                listForAdpter = callsSubInfoList;
                break;
            case SMS_PICK:
                listForAdpter = smsSubInfoList;
                break;
            default:
                listForAdpter = null;
                throw new IllegalArgumentException("Invalid dialog type "
                        + id + " in SIM dialog.");
        }
        return listForAdpter;
    }

    /**
     * only for plug-in, change "SIM" to "SIM/UIM".
     *
     * @param builder the dialog builder need to modify.
     * @param id the dialog id.
     */
    private void changeDialogTitle(AlertDialog.Builder builder, int id) {
        switch (id) {
            case DATA_PICK:
                builder.setTitle(mMiscExt.customizeSimDisplayString(
                                    getResources().getString(R.string.select_sim_for_data),
                                    SubscriptionManager.INVALID_SUBSCRIPTION_ID));
                break;
            case CALLS_PICK:
                builder.setTitle(mMiscExt.customizeSimDisplayString(
                                    getResources().getString(R.string.select_sim_for_calls),
                                    SubscriptionManager.INVALID_SUBSCRIPTION_ID));
                break;
            case SMS_PICK:
                builder.setTitle(mMiscExt.customizeSimDisplayString(
                                    getResources().getString(R.string.sim_card_select_title),
                                    SubscriptionManager.INVALID_SUBSCRIPTION_ID));
                break;
            default:
                throw new IllegalArgumentException("Invalid dialog type "
                        + id + " in SIM dialog.");
        }
    }

    private void dismissSimDialog() {
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
            mDialog = null;
        }
    }
}
