/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.android.dialer.calllog;

import android.content.ComponentName;
import android.content.Context;
import android.support.annotation.Nullable;
import android.telecom.PhoneAccount;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;
import android.text.TextUtils;


import com.android.contacts.common.compat.CompatUtils;
import com.android.dialer.util.TelecomUtil;
import com.mediatek.dialer.ext.ExtensionManager;

import java.util.ArrayList;
import java.util.List;


/**
 * Methods to help extract {@code PhoneAccount} information from database and Telecomm sources.
 */
public class PhoneAccountUtils {
    /**
     * Return a list of phone accounts that are subscription/SIM accounts.
     */
    public static List<PhoneAccountHandle> getSubscriptionPhoneAccounts(Context context) {
        List<PhoneAccountHandle> subscriptionAccountHandles = new ArrayList<PhoneAccountHandle>();
        final List<PhoneAccountHandle> accountHandles =
                TelecomUtil.getCallCapablePhoneAccounts(context);
        for (PhoneAccountHandle accountHandle : accountHandles) {
            PhoneAccount account = TelecomUtil.getPhoneAccount(context, accountHandle);
            /// M: Judge whether the account is null
            if (account != null && account.hasCapabilities(
                    PhoneAccount.CAPABILITY_SIM_SUBSCRIPTION)) {
                subscriptionAccountHandles.add(accountHandle);
            }
        }
        return subscriptionAccountHandles;
    }

    /**
     * Compose PhoneAccount object from component name and account id.
     */
    @Nullable
    public static PhoneAccountHandle getAccount(@Nullable String componentString,
            @Nullable String accountId) {
        if (TextUtils.isEmpty(componentString) || TextUtils.isEmpty(accountId)) {
            return null;
        }
        final ComponentName componentName = ComponentName.unflattenFromString(componentString);
        if (componentName == null) {
            return null;
        }
        return new PhoneAccountHandle(componentName, accountId);
    }

    /**
     * Extract account label from PhoneAccount object.
     */
    @Nullable
    public static String getAccountLabel(Context context,
            @Nullable PhoneAccountHandle accountHandle) {
        PhoneAccount account = getAccountOrNull(context, accountHandle);
        if (account != null && account.getLabel() != null) {
            return account.getLabel().toString();
        }
        return null;
    }

    /**
     * Extract account color from PhoneAccount object.
     */
    public static int getAccountColor(Context context, @Nullable PhoneAccountHandle accountHandle) {
        final PhoneAccount account = TelecomUtil.getPhoneAccount(context, accountHandle);

        // For single-sim devices the PhoneAccount will be NO_HIGHLIGHT_COLOR by default, so it is
        // safe to always use the account highlight color.
        return account == null ? PhoneAccount.NO_HIGHLIGHT_COLOR : account.getHighlightColor();
    }

    /**
     * Determine whether a phone account supports call subjects.
     *
     * @return {@code true} if call subjects are supported, {@code false} otherwise.
     */
    public static boolean getAccountSupportsCallSubject(Context context,
            @Nullable PhoneAccountHandle accountHandle) {
        final PhoneAccount account = TelecomUtil.getPhoneAccount(context, accountHandle);

        return account == null ? false :
                account.hasCapabilities(PhoneAccount.CAPABILITY_CALL_SUBJECT);
    }

    /**
     * Retrieve the account metadata, but if the account does not exist or the device has only a
     * single registered and enabled account, return null.
     */
    @Nullable
    private static PhoneAccount getAccountOrNull(Context context,
            @Nullable PhoneAccountHandle accountHandle) {
        /// M: for ALPS02430078
        /// Plug-in OP09 do not return null in one account case @{
        if (TelecomUtil.getCallCapablePhoneAccounts(context).size() <= 1 &&
                ExtensionManager.getInstance().getCallLogExtension().shouldReturnAccountNull()) {
            return null;
        }
        /// @}
        return TelecomUtil.getPhoneAccount(context, accountHandle);
    }

     /** M: [Call Log Account Filter]
      * Retrieve the account by given id, return null if not find @{ */
     public static PhoneAccountHandle getPhoneAccountById(Context context, String id) {
         if (!TextUtils.isEmpty(id)) {
             final TelecomManager telecomManager = (TelecomManager) context
                     .getSystemService(Context.TELECOM_SERVICE);
             final List<PhoneAccountHandle> accounts = telecomManager.getCallCapablePhoneAccounts();
             for (PhoneAccountHandle account : accounts) {
                 if (id.equals(account.getId())) {
                     return account;
                 }
             }
         }
         return null;
     }

     public static PhoneAccount getPhoneAccount(Context context,
             PhoneAccountHandle acctHandle) {
         TelecomManager telecomManager = (TelecomManager) context
                 .getSystemService(Context.TELECOM_SERVICE);
         return telecomManager.getPhoneAccount(acctHandle);
     }

     public static String getAccountNumber(Context context,
             PhoneAccountHandle phoneAccount) {
         final PhoneAccount account = getPhoneAccount(context, phoneAccount);
         if (account == null) {
             return null;
         }
         return account.getAddress().getSchemeSpecificPart();
     }

     public static boolean hasMultipleCallCapableAccounts(Context context) {
         TelecomManager telecomManager = (TelecomManager) context
                 .getSystemService(Context.TELECOM_SERVICE);
         return telecomManager.getCallCapablePhoneAccounts().size() > 1;
     }
     /** @} */

    /**
     * M: [IMS Call] Return true if given account is subscription/SIM account.
     */
    public static boolean isSubScriptionAccount(Context context,
            PhoneAccountHandle accountHandle) {
        TelecomManager telecomManager =
                (TelecomManager) context.getSystemService(Context.TELECOM_SERVICE);
        final PhoneAccount account = telecomManager.getPhoneAccount(accountHandle);
        if (account != null && account.hasCapabilities(
                PhoneAccount.CAPABILITY_SIM_SUBSCRIPTION)) {
            return true;
        }
        return false;
    }

}
