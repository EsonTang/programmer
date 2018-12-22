/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.android.settings.datausage;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.NetworkPolicyManager;
import android.net.NetworkTemplate;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.Settings.Global;
import android.support.v7.preference.PreferenceViewHolder;
import android.telecom.TelecomManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Checkable;
import android.widget.Toast;

import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.MetricsProto.MetricsEvent;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.TelephonyIntents;
import com.android.settings.CustomDialogPreference;
import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settings.sim.SimDialogActivity;
import com.mediatek.settings.UtilsExt;
import com.mediatek.settings.cdma.CdmaUtils;
import com.mediatek.settings.ext.IDataUsageSummaryExt;
import com.mediatek.settings.sim.TelephonyUtils;

import java.util.List;

public class CellDataPreference extends CustomDialogPreference implements TemplatePreference {

    private static final String TAG = "CellDataPreference";

    public int mSubId = SubscriptionManager.INVALID_SUBSCRIPTION_ID;
    public boolean mChecked;
    public boolean mMultiSimDialog;
    private TelephonyManager mTelephonyManager;
    private SubscriptionManager mSubscriptionManager;

    public CellDataPreference(Context context, AttributeSet attrs) {
        super(context, attrs, android.R.attr.switchPreferenceStyle);
    }

    @Override
    protected void onRestoreInstanceState(Parcelable s) {
        CellDataState state = (CellDataState) s;
        super.onRestoreInstanceState(state.getSuperState());
        mTelephonyManager = TelephonyManager.from(getContext());
        mChecked = state.mChecked;
        mMultiSimDialog = state.mMultiSimDialog;
        if (mSubId == SubscriptionManager.INVALID_SUBSCRIPTION_ID) {
            mSubId = state.mSubId;
            setKey(getKey() + mSubId);
        }
        notifyChanged();
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        CellDataState state = new CellDataState(super.onSaveInstanceState());
        state.mChecked = mChecked;
        state.mMultiSimDialog = mMultiSimDialog;
        state.mSubId = mSubId;
        return state;
    }

    @Override
    public void onAttached() {
        log("onAttached...");
        super.onAttached();

        /// M: for plug-in @{
        mDataUsageSummaryExt = UtilsExt.getDataUsageSummaryPlugin(getContext()
                .getApplicationContext());

        mListener.setListener(true, mSubId, getContext());
        /// M: check airplane mode
        mIsAirplaneModeOn = TelephonyUtils.isAirplaneModeOn(getContext());

        /// M: observe more event @{
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        /// M: [C2K solution2 enhancement] @{
        intentFilter.addAction(TelephonyIntents.ACTION_SET_RADIO_CAPABILITY_DONE);
        intentFilter.addAction(TelephonyIntents.ACTION_SET_RADIO_CAPABILITY_FAILED);
        intentFilter.addAction(TelephonyIntents.ACTION_DEFAULT_DATA_SUBSCRIPTION_CHANGED);
        /// @}

        /// M: for cmcc plugin, when is cmcc + other card,
        /// cmcc card data enable, other disable data
        mDataUsageSummaryExt.customReceiver(intentFilter);
        getContext().registerReceiver(mReceiver, intentFilter);
        /// @}

        /// M: check enabled condition
        updateScreenEnableState();

        /// M: for plug-in OP07 @{
        if (mDataUsageSummaryExt != null) {
            mDataUsageSummaryExt.setPreferenceSummary(this);
        }
    }

    @Override
    public void onDetached() {
        log("onDetached...");
        mListener.setListener(false, mSubId, getContext());
        super.onDetached();
        /// M:
        getContext().unregisterReceiver(mReceiver);
        mAlertForCdmaCompetition = false;
    }

    @Override
    public void setTemplate(NetworkTemplate template, int subId, NetworkServices services) {
        if (subId == SubscriptionManager.INVALID_SUBSCRIPTION_ID) {
            throw new IllegalArgumentException("CellDataPreference needs a SubscriptionInfo");
        }
        mSubscriptionManager = SubscriptionManager.from(getContext());
        mTelephonyManager = TelephonyManager.from(getContext());
        if (mSubId == SubscriptionManager.INVALID_SUBSCRIPTION_ID) {
            mSubId = subId;
            setKey(getKey() + subId);
        }
        updateChecked();
    }

    private void updateChecked() {
        setChecked(mTelephonyManager.getDataEnabled(mSubId));
    }

    @Override
    protected void performClick(View view) {
        log("performClick, checked = " + mChecked);
        MetricsLogger.action(getContext(), MetricsEvent.ACTION_CELL_DATA_TOGGLE, !mChecked);
        final SubscriptionInfo currentSir = mSubscriptionManager.getActiveSubscriptionInfo(
                mSubId);
        final SubscriptionInfo nextSir = mSubscriptionManager.getDefaultDataSubscriptionInfo();
        if (mChecked) {
            // If the device is single SIM or is enabling data on the active data SIM then forgo
            // the pop-up.
            if (!Utils.showSimCardTile(getContext()) ||
                    (nextSir != null && currentSir != null &&
                            currentSir.getSubscriptionId() == nextSir.getSubscriptionId())) {
                setMobileDataEnabled(false);
                if (nextSir != null && currentSir != null &&
                        currentSir.getSubscriptionId() == nextSir.getSubscriptionId()) {
                    disableDataForOtherSubscriptions(mSubId);
                }
                return;
            }

            // disabling data; show confirmation dialog which eventually
            // calls setMobileDataEnabled() once user confirms.
            mMultiSimDialog = false;
            super.performClick(view);
        } else {
            // If we are showing the Sim Card tile then we are a Multi-Sim device.
            /// M: Don't show dialog when enable default data sim and null sim.
            if (Utils.showSimCardTile(getContext()) && currentSir != null && (nextSir != null &&
                    currentSir.getSubscriptionId() != nextSir.getSubscriptionId()
                    || nextSir == null)) {
                mMultiSimDialog = true;
                if (nextSir != null && currentSir != null &&
                        currentSir.getSubscriptionId() == nextSir.getSubscriptionId()) {
                    setMobileDataEnabled(true);
                    disableDataForOtherSubscriptions(mSubId);
                    return;
                }
                super.performClick(view);
            } else {
                setMobileDataEnabled(true);
            }
        }
    }

    private void setMobileDataEnabled(boolean enabled) {
        if (DataUsageSummary.LOGD) log("setMobileDataEnabled(" + enabled + ","
                + mSubId + ")");
        mTelephonyManager.setDataEnabled(mSubId, enabled);
        setChecked(enabled);
    }

    private void setChecked(boolean checked) {
        log("setChecked " + checked);
        if (mChecked == checked) return;
        mChecked = checked;
        notifyChanged();
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        View switchView = holder.findViewById(android.R.id.switch_widget);
        switchView.setClickable(false);
        ((Checkable) switchView).setChecked(mChecked);

        /// M: for plug-in to customize onClickListener @{
        mDataUsageSummaryExt.onBindViewHolder(getContext(), holder.itemView,
                new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performClick(v);
            }
        });
        /// @}
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder,
            DialogInterface.OnClickListener listener) {
        if (mMultiSimDialog) {
            showMultiSimDialog(builder, listener);
        } else {
            /// M: for plug-in
            if (mDataUsageSummaryExt.onDisablingData(mSubId)) {
                showDisableDialog(builder, listener);
            }
        }
    }

    private void showDisableDialog(AlertDialog.Builder builder,
            DialogInterface.OnClickListener listener) {
        builder.setTitle(null)
                .setMessage(R.string.data_usage_disable_mobile)
                .setPositiveButton(android.R.string.ok, listener)
                .setNegativeButton(android.R.string.cancel, null);
    }

    private void showMultiSimDialog(AlertDialog.Builder builder,
            DialogInterface.OnClickListener listener) {
        final SubscriptionInfo currentSir = mSubscriptionManager.getActiveSubscriptionInfo(mSubId);
        final SubscriptionInfo nextSir = mSubscriptionManager.getDefaultDataSubscriptionInfo();

        final String previousName = (nextSir == null)
            ? getContext().getResources().getString(R.string.sim_selection_required_pref)
            : nextSir.getDisplayName().toString();

        builder.setTitle(R.string.sim_change_data_title);
        builder.setMessage(getContext().getString(R.string.sim_change_data_message,
                String.valueOf(currentSir != null ? currentSir.getDisplayName() : null),
                previousName));

        builder.setPositiveButton(R.string.okay, listener);
        builder.setNegativeButton(R.string.cancel, null);
    }

    private void disableDataForOtherSubscriptions(int subId) {
        List<SubscriptionInfo> subInfoList = mSubscriptionManager.getActiveSubscriptionInfoList();
        if (subInfoList != null) {
            for (SubscriptionInfo subInfo : subInfoList) {
                if (subInfo.getSubscriptionId() != subId) {
                    mTelephonyManager.setDataEnabled(subInfo.getSubscriptionId(), false);
                }
            }
        }
    }

    @Override
    protected void onClick(DialogInterface dialog, int which) {
        if (which != DialogInterface.BUTTON_POSITIVE) {
            return;
        }
        log("onClick, mMultiSimDialog = " + mMultiSimDialog);
        if (mMultiSimDialog) {
            /// M: if in call, do not switch data @{
            if (TelecomManager.from(getContext()).isInCall()) {
                Toast.makeText(getContext(), R.string.default_data_switch_err_msg1,
                        Toast.LENGTH_SHORT).show();
                log("in Call, RETURN!");
                return;
            }
            /// @}

            mSubscriptionManager.setDefaultDataSubId(mSubId);
            setMobileDataEnabled(true);
            disableDataForOtherSubscriptions(mSubId);
        } else {
            // TODO: extend to modify policy enabled flag.
            setMobileDataEnabled(false);
        }
    }

    private final DataStateListener mListener = new DataStateListener() {
        @Override
        public void onChange(boolean selfChange) {
            log("data state changed");
            updateChecked();
        }
    };

    public abstract static class DataStateListener extends ContentObserver {
        public DataStateListener() {
            super(new Handler(Looper.getMainLooper()));
        }

        public void setListener(boolean listening, int subId, Context context) {
            if (listening) {
                Uri uri = Global.getUriFor(Global.MOBILE_DATA);
                if (TelephonyManager.getDefault().getSimCount() != 1) {
                    uri = Global.getUriFor(Global.MOBILE_DATA + subId);
                }
                context.getContentResolver().registerContentObserver(uri, false, this);
            } else {
                context.getContentResolver().unregisterContentObserver(this);
            }
        }
    }

    public static class CellDataState extends BaseSavedState {
        public int mSubId;
        public boolean mChecked;
        public boolean mMultiSimDialog;

        public CellDataState(Parcelable base) {
            super(base);
        }

        public CellDataState(Parcel source) {
            super(source);
            mChecked = source.readByte() != 0;
            mMultiSimDialog = source.readByte() != 0;
            mSubId = source.readInt();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeByte((byte) (mChecked ? 1 : 0));
            dest.writeByte((byte) (mMultiSimDialog ? 1 : 0));
            dest.writeInt(mSubId);
        }

        public static final Creator<CellDataState> CREATOR = new Creator<CellDataState>() {
            @Override
            public CellDataState createFromParcel(Parcel source) {
                return new CellDataState(source);
            }

            @Override
            public CellDataState[] newArray(int size) {
                return new CellDataState[size];
            }
        };
    }


    ///------------------------------------MTK------------------------------------------------
    private boolean mIsAirplaneModeOn;
    private View mDataEnabled;
    private IDataUsageSummaryExt mDataUsageSummaryExt;

    /// [C2K solution 2 enhancement] @{
    // add for c2k solution 2. mark data enable click event.
    private boolean mAlertForCdmaCompetition = false;

    // add a BroadcastReceiver to update status when the phone states changed
    // for the MSIM phone.
   private BroadcastReceiver mReceiver = new BroadcastReceiver() {

       @Override
       public void onReceive(Context context, Intent intent) {

           String action = intent.getAction();
           log("onReceive broadcast , action =  " + action);

           if (action.equals(Intent.ACTION_AIRPLANE_MODE_CHANGED)) {
              mIsAirplaneModeOn = intent.getBooleanExtra("state", false);
              updateScreenEnableState();
           } else if (action.equals(
                   TelephonyIntents.ACTION_DEFAULT_DATA_SUBSCRIPTION_CHANGED)) {
               onCdmaCompetitionHandled(intent);
               updateScreenEnableState();
            } else if (action.equals(TelephonyIntents.ACTION_SET_RADIO_CAPABILITY_DONE)
                    || action.equals(TelephonyIntents.ACTION_SET_RADIO_CAPABILITY_FAILED)
                    || mDataUsageSummaryExt.customDualReceiver(action)) {
               updateScreenEnableState();
           }
       }
   };

    private void onCdmaCompetitionHandled(Intent intent) {
        int defaultDataSubId = intent.getIntExtra(PhoneConstants.SUBSCRIPTION_KEY,
                SubscriptionManager.INVALID_SUBSCRIPTION_ID);
        log("defaultDataSubId: " + defaultDataSubId + " mAlertForCdmaCompetition: "
                + mAlertForCdmaCompetition);
        if (mAlertForCdmaCompetition && (defaultDataSubId == mSubId)) {
            setMobileDataEnabled(true);
            disableDataForOtherSubscriptions(mSubId);
            mAlertForCdmaCompetition = false;
        }
    }

   private void updateScreenEnableState() {
        boolean isCapabilitySwitching = TelephonyUtils.isCapabilitySwitching();
        log("updateScreenEnableState, mIsAirplaneModeOn = " + mIsAirplaneModeOn
                + ", isCapabilitySwitching = " + isCapabilitySwitching);
        setEnabled(!mIsAirplaneModeOn && !isCapabilitySwitching
                && mDataUsageSummaryExt.isAllowDataEnable(mSubId));
   }

    private void log(String msg) {
        Log.d(TAG + "[" + mSubId + "]", msg);
    }
}
