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
 * limitations under the License
 */

package com.android.contacts.common.preference;

import android.app.AlertDialog;
import android.content.Context;
import android.preference.ListPreference;
import android.util.AttributeSet;

import com.android.contacts.common.model.AccountTypeManager;
import com.android.contacts.common.model.account.AccountType;
import com.android.contacts.common.model.account.AccountTypeWithDataSet;
import com.android.contacts.common.model.account.AccountWithDataSet;
import com.mediatek.contacts.util.AccountTypeUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DefaultAccountPreference extends ListPreference {
    private ContactsPreferences mPreferences;
    private Map<String, AccountWithDataSet> mAccountMap;

    public DefaultAccountPreference(Context context) {
        super(context);
        prepare();
    }

    public DefaultAccountPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        prepare();
    }

    private void prepare() {
        mPreferences = new ContactsPreferences(getContext());
        mAccountMap = new HashMap<>();
        final AccountTypeManager accountTypeManager = AccountTypeManager.getInstance(getContext());
        List<AccountWithDataSet> accounts = accountTypeManager.getAccounts(true);
        /// M: Modify for meeting the Contacts sim indicator style. @{
        final ArrayList<String> accountNamesToShow = new ArrayList<String>();
        final ArrayList<String> accountNamesArray = new ArrayList<String>();
        for (AccountWithDataSet account : accounts) {
            final AccountType accountType = accountTypeManager.getAccountType(
                    account.type, account.dataSet);
            if (accountType.isIccCardAccount()) {
                String simName = AccountTypeUtils.getDisplayAccountName(getContext(), account.name);
                accountNamesToShow.add(simName);
            } else {
                accountNamesToShow.add(account.name);
            }
            accountNamesArray.add(account.name);
            mAccountMap.put(account.name, account);
        }
        setEntries((String[])accountNamesToShow.toArray(new String[0]));
        setEntryValues((String[])accountNamesArray.toArray(new String[0]));
        /// @}
        final Set<String> accountNames = mAccountMap.keySet();
        final String defaultAccount = String.valueOf(mPreferences.getDefaultAccount());
        if (accounts.size() == 1) {
            setValue(accounts.get(0).name);
        } else if (accountNames.contains(defaultAccount)) {
            setValue(defaultAccount);
        } else {
            setValue(null);
        }
    }

    @Override
    protected boolean shouldPersist() {
        return false;   // This preference takes care of its own storage
    }

    @Override
    public CharSequence getSummary() {
        String defaultAccount = mPreferences.getDefaultAccount();
        /// M: Modify for meeting the Contacts sim indicator style. @{
        final Set<String> accountNames = mAccountMap.keySet();
        if (accountNames.contains(defaultAccount)) {
            String accountString = AccountTypeUtils.getDisplayAccountName(
                    getContext(), defaultAccount);
            CharSequence account = accountString;
            return account;
        } else {
            return null;
        }
        /// @}
    }

    @Override
    protected boolean persistString(String value) {
        if (value == null && mPreferences.getDefaultAccount() == null) {
            return true;
        }
        if (value == null || mPreferences.getDefaultAccount() == null
                || !value.equals(mPreferences.getDefaultAccount())) {
            mPreferences.setDefaultAccount(mAccountMap.get(value));
            notifyChanged();
        }
        return true;
    }

    @Override
    // UX recommendation is not to show cancel button on such lists.
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        super.onPrepareDialogBuilder(builder);
        builder.setNegativeButton(null, null);
    }
}
