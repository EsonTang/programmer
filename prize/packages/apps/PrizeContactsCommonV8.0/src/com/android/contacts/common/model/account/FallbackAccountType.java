/*
* Copyright (C) 2014 MediaTek Inc.
* Modification based on code covered by the mentioned copyright
* and/or permission notice(s).
*/
/*
 * Copyright (C) 2009 The Android Open Source Project
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

package com.android.contacts.common.model.account;

import android.content.Context;
import android.net.sip.SipManager;
import android.telephony.TelephonyManager;
//import android.util.Log;

import com.android.contacts.common.R;
import com.android.contacts.common.model.dataitem.DataKind;
import com.android.contacts.common.testing.NeededForTesting;

import com.mediatek.contacts.ContactsSystemProperties;
import com.mediatek.contacts.util.Log;

public class FallbackAccountType extends BaseAccountType {
    private static final String TAG = "FallbackAccountType";

    private FallbackAccountType(Context context, String resPackageName) {
        this.accountType = null;
        this.dataSet = null;
        this.titleRes = R.string.account_phone;
        this.iconRes = R.mipmap.ic_contacts_launcher;
        Log.d(TAG, "[FallbackAccountType]new,iconRes = " + iconRes);
        // Note those are only set for unit tests.
        this.resourcePackageName = resPackageName;
        this.syncAdapterPackageName = resPackageName;

        TelephonyManager telephonyManager = new TelephonyManager(context);
        try {
            addDataKindStructuredName(context);
            addDataKindDisplayName(context);
            addDataKindPhoneticName(context);
            addDataKindNickname(context);
            addDataKindPhone(context);
            addDataKindEmail(context);
            addDataKindStructuredPostal(context);
            addDataKindIm(context);
            addDataKindOrganization(context);
            addDataKindPhoto(context);
            addDataKindNote(context);
            addDataKindWebsite(context);
            /// M: ALPS02758318.profile whether show sip adress field depend on isVoiceCapable @{
            ///addDataKindSipAddress(context);
            boolean isVoiceCapable = telephonyManager.isVoiceCapable();
            boolean isVoipSupported = SipManager.isVoipSupported(context);
            Log.i(TAG, "[FallbackAccountType]isVoiceCapable = " + isVoiceCapable
                    + ",isVoipSupported = " + isVoipSupported);
            if (isVoiceCapable && isVoipSupported) {
                addDataKindSipAddress(context);
            }
            /// @}

            addDataKindGroupMembership(context);
            // The following lines are provided and maintained by Mediatek Inc.
            /// M: VOLTE IMS Call feature.
            /*prize - remove IMS Call -hpf-2017-12-20-start*/
            /*if (ContactsSystemProperties.MTK_VOLTE_SUPPORT
                    && ContactsSystemProperties.MTK_IMS_SUPPORT) {
                addDataKindImsCall(context);
            }*/
            /*prize - remove IMS Call -hpf-2017-12-20-end*/
            // The previous lines are provided and maintained by Mediatek Inc.

            mIsInitialized = true;
        } catch (DefinitionException e) {
            Log.e(TAG, "Problem building account type", e);
        }
    }

    public FallbackAccountType(Context context) {
        this(context, null);
    }

    /**
     * Used to compare with an {@link ExternalAccountType} built from a test contacts.xml.
     * In order to build {@link DataKind}s with the same resource package name,
     * {@code resPackageName} is injectable.
     */
    @NeededForTesting
    static AccountType createWithPackageNameForTest(Context context, String resPackageName) {
        return new FallbackAccountType(context, resPackageName);
    }

    @Override
    public boolean areContactsWritable() {
        return true;
    }
}
