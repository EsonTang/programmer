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
 * limitations under the License
 */

package com.android.phone.settings;

import com.android.phone.R;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.UserHandle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.telecom.PhoneAccount;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;
import android.text.TextUtils;
import android.util.AttributeSet;

import com.mediatek.phone.ext.ExtensionManager;

import java.util.List;
import java.util.Objects;

public class AccountSelectionPreference extends ListPreference implements
        Preference.OnPreferenceChangeListener {

    public interface AccountSelectionListener {
        boolean onAccountSelected(AccountSelectionPreference pref, PhoneAccountHandle account);
        void onAccountSelectionDialogShow(AccountSelectionPreference pref);
        void onAccountChanged(AccountSelectionPreference pref);
    }
    private static final String TAG = "settings/AccountSelectionPreference";
    private final Context mContext;
    private AccountSelectionListener mListener;
    private PhoneAccountHandle[] mAccounts;
    private String[] mEntryValues;
    private CharSequence[] mEntries;
    private boolean mShowSelectionInSummary = true;

    public AccountSelectionPreference(Context context) {
        this(context, null);
    }

    public AccountSelectionPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        setOnPreferenceChangeListener(this);
    }

    public void setListener(AccountSelectionListener listener) {
        mListener = listener;
    }

    public void setShowSelectionInSummary(boolean value) {
        mShowSelectionInSummary = value;
    }

    public void setModel(
            TelecomManager telecomManager,
            List<PhoneAccountHandle> accountsList,
            PhoneAccountHandle currentSelection,
            CharSequence nullSelectionString) {

        mAccounts = accountsList.toArray(new PhoneAccountHandle[accountsList.size()]);
        mEntryValues = new String[mAccounts.length + 1];
        mEntries = new CharSequence[mAccounts.length + 1];

        PackageManager pm = mContext.getPackageManager();

        int selectedIndex = mAccounts.length;  // Points to nullSelectionString by default

        /// M: For OP09 reset the selectedIndex to the first one @{
        selectedIndex = ExtensionManager.getPhoneMiscExt().getSelectedIndex(selectedIndex);
        /// }@
        int i = 0;
        for ( ; i < mAccounts.length; i++) {
            PhoneAccount account = telecomManager.getPhoneAccount(mAccounts[i]);
            CharSequence label = account.getLabel();
            if (label != null) {
                label = pm.getUserBadgedLabel(label, mAccounts[i].getUserHandle());
            }
            boolean isSimAccount =
                    account.hasCapabilities(PhoneAccount.CAPABILITY_SIM_SUBSCRIPTION);
            mEntries[i] = (TextUtils.isEmpty(label) && isSimAccount)
                    ? mContext.getString(R.string.phone_accounts_default_account_label)
                    : String.valueOf(label);
            mEntryValues[i] = Integer.toString(i);
            if (Objects.equals(currentSelection, mAccounts[i])) {
                selectedIndex = i;
            }
        }
        mEntryValues[i] = Integer.toString(i);
        mEntries[i] = nullSelectionString;

        /// M: remove the "Ask first" item from the call with selection list.
        mEntryValues = ExtensionManager.getPhoneMiscExt()
                 .removeAskFirstFromSelectionListIndex(mEntryValues);
        mEntries = ExtensionManager.getPhoneMiscExt()
                 .removeAskFirstFromSelectionListValue(mEntries);

        // add "current network" item to the selection list
        mEntryValues = ExtensionManager.getPhoneMiscExt()
                 .addCurrentNetworkToSelectionListIndex(mEntryValues);
        mEntries = ExtensionManager.getPhoneMiscExt()
                 .addCurrentNetworkToSelectionListValue(mEntries);
        // change highlighted index if the current network is selected
        selectedIndex = ExtensionManager.getPhoneMiscExt()
                    .getCurrentNetworkIndex(selectedIndex);


        setEntryValues(mEntryValues);
        setEntries(mEntries);
        setValueIndex(selectedIndex);
        if (mShowSelectionInSummary) {
            setSummary(mEntries[selectedIndex]);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {

        final int index = Integer.parseInt((String) newValue);

        // No further handling needed if current network is being selected by the user
        // only update the summary to be shown on the calling accounts screen
        if (ExtensionManager.getPhoneMiscExt().onPreferenceChange(index)) {
            if (mShowSelectionInSummary) {
                setSummary(mEntries[index]);
            }
            return true;
        }
        if (mListener != null) {
            //int index = Integer.parseInt((String) newValue);
            PhoneAccountHandle account = index < mAccounts.length ? mAccounts[index] : null;
            if (mListener.onAccountSelected(this, account)) {
                if (mShowSelectionInSummary) {
                    setSummary(mEntries[index]);
                }
                if (index != findIndexOfValue(getValue())) {
                    setValueIndex(index);
                    mListener.onAccountChanged(this);
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Modifies the dialog to change the default "Cancel" button to "Choose Accounts", which
     * triggers the {@link PhoneAccountSelectionPreferenceActivity} to be shown.
     *
     * @param builder The {@code AlertDialog.Builder}.
     */
    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        // Notify the listener that the dialog is about to be built.  This is important so that the
        // list of enabled accounts can be updated prior to showing the dialog.
        mListener.onAccountSelectionDialogShow(this);

        super.onPrepareDialogBuilder(builder);
    }
}
