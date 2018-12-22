/*
 * Copyright (C) 2012 The Android Open Source Project
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

package com.android.dialer.util;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;
import android.telecom.VideoProfile;

import com.android.contacts.common.CallUtil;
import com.android.contacts.common.util.Constants;
import com.android.phone.common.PhoneConstants;

/**
 * Utilities for creation of intents in Dialer, such as {@link Intent#ACTION_CALL}.
 */
public class IntentUtil {

    public static final String CALL_ACTION = Intent.ACTION_CALL;
    private static final String SMS_URI_PREFIX = "sms:";
    private static final int NO_PHONE_TYPE = -1;

    public static final String EXTRA_CALL_INITIATION_TYPE
            = "com.android.dialer.EXTRA_CALL_INITIATION_TYPE";

    public static class CallIntentBuilder {
        private Uri mUri;
        private int mCallInitiationType;
        private PhoneAccountHandle mPhoneAccountHandle;
        private boolean mIsVideoCall = false;

        public CallIntentBuilder(Uri uri) {
            mUri = uri;
        }

        public CallIntentBuilder(String number) {
            this(CallUtil.getCallUri(number));
        }

        public CallIntentBuilder setCallInitiationType(int initiationType) {
            mCallInitiationType = initiationType;
            return this;
        }

        public CallIntentBuilder setPhoneAccountHandle(PhoneAccountHandle accountHandle) {
            mPhoneAccountHandle = accountHandle;
            return this;
        }

        public CallIntentBuilder setIsVideoCall(boolean isVideoCall) {
            mIsVideoCall = isVideoCall;
            return this;
        }

        public Intent build() {
            return getCallIntent(
                    mUri,
                    mPhoneAccountHandle,
                    mIsVideoCall ? VideoProfile.STATE_BIDIRECTIONAL : VideoProfile.STATE_AUDIO_ONLY,
                    mCallInitiationType);
        }
    }

    /**
     * Create a call intent that can be used to place a call.
     *
     * @param uri Address to place the call to.
     * @param accountHandle {@link PhoneAccountHandle} to place the call with.
     * @param videoState Initial video state of the call.
     * @param callIntiationType The UI affordance the call was initiated by.
     * @return Call intent with provided extras and data.
     */
    public static Intent getCallIntent(
            Uri uri, PhoneAccountHandle accountHandle, int videoState, int callIntiationType) {
        final Intent intent = new Intent(CALL_ACTION, uri);
        intent.putExtra(TelecomManager.EXTRA_START_CALL_WITH_VIDEO_STATE, videoState);

        final Bundle b = new Bundle();
        b.putInt(EXTRA_CALL_INITIATION_TYPE, callIntiationType);
        intent.putExtra(TelecomManager.EXTRA_OUTGOING_CALL_EXTRAS, b);

        if (accountHandle != null) {
            intent.putExtra(TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE, accountHandle);
        }

        return intent;
    }

    public static Intent getSendSmsIntent(CharSequence phoneNumber) {
        return new Intent(Intent.ACTION_SENDTO, Uri.parse(SMS_URI_PREFIX + phoneNumber));
    }

    public static Intent getNewContactIntent() {
        return new Intent(Intent.ACTION_INSERT, ContactsContract.Contacts.CONTENT_URI);
    }

    public static Intent getNewContactIntent(CharSequence phoneNumber) {
        return getNewContactIntent(
                null /* name */,
                phoneNumber /* phoneNumber */,
                NO_PHONE_TYPE);
    }

    public static Intent getNewContactIntent(
            CharSequence name, CharSequence phoneNumber, int phoneNumberType) {
        Intent intent = getNewContactIntent();
        populateContactIntent(intent, name, phoneNumber, phoneNumberType);
        return intent;
    }

    public static Intent getAddToExistingContactIntent() {
        Intent intent = new Intent(Intent.ACTION_INSERT_OR_EDIT);
        intent.setType(ContactsContract.Contacts.CONTENT_ITEM_TYPE);
        return intent;
    }

    public static Intent getAddToExistingContactIntent(CharSequence phoneNumber) {
        return getAddToExistingContactIntent(
                null /* name */,
                phoneNumber /* phoneNumber */,
                NO_PHONE_TYPE);
    }

    public static Intent getAddToExistingContactIntent(
            CharSequence name, CharSequence phoneNumber, int phoneNumberType) {
        Intent intent = getAddToExistingContactIntent();
        populateContactIntent(intent, name, phoneNumber, phoneNumberType);
        return intent;
    }

    private static void populateContactIntent(
            Intent intent, CharSequence name, CharSequence phoneNumber, int phoneNumberType) {
        if (phoneNumber != null) {
            intent.putExtra(ContactsContract.Intents.Insert.PHONE, phoneNumber);
        }
        if (name != null) {
            intent.putExtra(ContactsContract.Intents.Insert.NAME, name);
        }
        if (phoneNumberType != NO_PHONE_TYPE) {
            intent.putExtra(ContactsContract.Intents.Insert.PHONE_TYPE, phoneNumberType);
        }
    }

    /**
     * M: According the type to get the CallIntent
     * @param uri the uri
     * @param initiationType
     * @param callType the call' type
     * @return the CallIntent
     */
    public static Intent getCallIntent(Uri uri, int initiationType, int callType) {
        final Intent intent = new CallIntentBuilder(uri).setCallInitiationType(initiationType)
                .setPhoneAccountHandle(null).build();
        if ((callType & Constants.DIAL_NUMBER_INTENT_IP) != 0) {
            intent.putExtra(Constants.EXTRA_IS_IP_DIAL, true);
        }

        if ((callType & Constants.DIAL_NUMBER_INTENT_VIDEO) != 0) {
            intent.putExtra(Constants.EXTRA_IS_VIDEO_CALL, true);
        }
        //M: [IMS Call] For IMS Call feature @{
        if ((callType & Constants.DIAL_NUMBER_INTENT_IMS) != 0) {
            intent.putExtra(Constants.EXTRA_IS_IMS_CALL, true);
        }
        ///@}
        return intent;
    }

    /**
     * M: Return Uri with an appropriate scheme, accepting both SIP and usual
     * phone call numbers.
     */
    public static Uri getCallUri(String number) {
        return CallUtil.getCallUri(number);
    }
}
