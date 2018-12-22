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

package com.android.dialer.calllog.calllogcache;

import android.content.Context;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Pair;

import com.android.dialer.calllog.PhoneAccountUtils;
import com.android.dialer.util.PhoneNumberUtil;
import com.android.dialer.util.TelecomUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * This is the CallLogCache for versions of dialer Lollipop Mr1 and above with support for
 * multi-SIM devices.
 *
 * This class should not be initialized directly and instead be acquired from
 * {@link CallLogCache#getCallLogCache}.
 */
class CallLogCacheLollipopMr1 extends CallLogCache {
    // Maps from a phone-account/number pair to a boolean because multiple numbers could return true
    // for the voicemail number if those numbers are not pre-normalized.
    private final Map<Pair<PhoneAccountHandle, CharSequence>, Boolean> mVoicemailQueryCache =
            new HashMap<>();
    private final Map<PhoneAccountHandle, String> mPhoneAccountLabelCache = new HashMap<>();
    private final Map<PhoneAccountHandle, Integer> mPhoneAccountColorCache = new HashMap<>();
    private final Map<PhoneAccountHandle, Boolean> mPhoneAccountCallWithNoteCache = new HashMap<>();
    /// M: Cache subId with the special PhoneAccountHandle
    private final Map<PhoneAccountHandle, Integer> mSubIdCache = new HashMap<>();
    /// M: Cache voicemail numbers for each account, improve cache efficiency.
    private final Map<PhoneAccountHandle, String> mPhoneAccountVoiceMailNumber = new HashMap<>();

    /* package */ CallLogCacheLollipopMr1(Context context) {
        super(context);
    }

    @Override
    public void reset() {
        mVoicemailQueryCache.clear();
        mPhoneAccountLabelCache.clear();
        mPhoneAccountColorCache.clear();
        mPhoneAccountCallWithNoteCache.clear();
        /// M: Clear the cached subId
        mSubIdCache.clear();
        /// M: clear cache
        mPhoneAccountVoiceMailNumber.clear();

        super.reset();
    }

    @Override
    public boolean isVoicemailNumber(PhoneAccountHandle accountHandle, CharSequence number) {
        if (TextUtils.isEmpty(number)) {
            return false;
        }

        /// M: improve voice number lookup efficiency by caching all voice numbers, no need
        /// binder call telecom for every number. @{
        if (accountHandle == null) {
            return false;
        }
        String voiceNumber = null;
        if (mPhoneAccountVoiceMailNumber.containsKey(accountHandle)) {
            voiceNumber = mPhoneAccountVoiceMailNumber.get(accountHandle);
        } else {
            voiceNumber = TelecomUtil.getVoicemailNumber(mContext, accountHandle);
            mPhoneAccountVoiceMailNumber.put(accountHandle, voiceNumber);
        }
        if (number.equals(voiceNumber)){
            return true;
        }
        return PhoneNumberUtils.compare(number.toString(), voiceNumber);
        /// @}

//        Pair<PhoneAccountHandle, CharSequence> key = new Pair<>(accountHandle, number);
//        if (mVoicemailQueryCache.containsKey(key)) {
//            return mVoicemailQueryCache.get(key);
//        } else {
//            Boolean isVoicemail =
//                    PhoneNumberUtil.isVoicemailNumber(mContext, accountHandle, number.toString());
//            mVoicemailQueryCache.put(key, isVoicemail);
//            return isVoicemail;
//        }
    }

    @Override
    public String getAccountLabel(PhoneAccountHandle accountHandle) {
        if (mPhoneAccountLabelCache.containsKey(accountHandle)) {
            return mPhoneAccountLabelCache.get(accountHandle);
        } else {
            String label = PhoneAccountUtils.getAccountLabel(mContext, accountHandle);
            mPhoneAccountLabelCache.put(accountHandle, label);
            return label;
        }
    }

    @Override
    public int getAccountColor(PhoneAccountHandle accountHandle) {
        if (mPhoneAccountColorCache.containsKey(accountHandle)) {
            return mPhoneAccountColorCache.get(accountHandle);
        } else {
            Integer color = PhoneAccountUtils.getAccountColor(mContext, accountHandle);
            mPhoneAccountColorCache.put(accountHandle, color);
            return color;
        }
    }

    @Override
    public boolean doesAccountSupportCallSubject(PhoneAccountHandle accountHandle) {
        if (mPhoneAccountCallWithNoteCache.containsKey(accountHandle)) {
            return mPhoneAccountCallWithNoteCache.get(accountHandle);
        } else {
            Boolean supportsCallWithNote =
                    PhoneAccountUtils.getAccountSupportsCallSubject(mContext, accountHandle);
            mPhoneAccountCallWithNoteCache.put(accountHandle, supportsCallWithNote);
            return supportsCallWithNote;
        }
    }

    /// M: Get the subId by the special PhoneAccountHandle
    @Override
    public int getSubId(PhoneAccountHandle accountHandle) {
        if (!mSubIdCache.containsKey(accountHandle)) {
            TelecomManager telecomManager = (TelecomManager) mContext
                    .getSystemService(Context.TELECOM_SERVICE);
            int subId = TelephonyManager.getDefault().getSubIdForPhoneAccount(
                    telecomManager.getPhoneAccount(accountHandle));
            mSubIdCache.put(accountHandle, subId);
            return subId;
        }
        return mSubIdCache.get(accountHandle);
    }

}
