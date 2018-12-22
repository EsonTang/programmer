/*
* Copyright (C) 2011-2014 MediaTek Inc.
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
package com.mediatek.services.telephony;

import android.os.SystemProperties;
import android.telecom.ConnectionRequest;
import android.telecom.PhoneAccountHandle;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.android.internal.telephony.SubscriptionController;

import com.android.phone.PhoneUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * The emergency call retry handler.
 * Selected the proper Phone for setting up the ecc call.
 */
public class EmergencyRetryHandler {
    private static final String TAG = "ECCRetryHandler";
    private static final boolean DBG = true;

    private static final boolean MTK_CT_VOLTE_SUPPORT
            = "1".equals(SystemProperties.get("persist.mtk_ct_volte_support", "0"));

    private static final int MAX_NUM_RETRIES =
            TelephonyManager.getDefault().getPhoneCount() > 1 ?
            (TelephonyManager.getDefault().getPhoneCount() - 1) : (MTK_CT_VOLTE_SUPPORT ? 1 : 0);

    private ConnectionRequest mRequest = null;
    private int mNumRetriesSoFar = 0;
    private List<PhoneAccountHandle> mAttemptRecords;
    private Iterator<PhoneAccountHandle> mAttemptRecordIterator;
    private String mCallId = null;

    /**
     * Init the EmergencyRetryHandler.
     * @param request ConnectionRequest
     * @param initPhoneId PhoneId of the initial ECC
     */
    public EmergencyRetryHandler(ConnectionRequest request, int initPhoneId) {
        mRequest = request;
        mNumRetriesSoFar = 0;
        mAttemptRecords = new ArrayList<PhoneAccountHandle>();

        PhoneAccountHandle phoneAccountHandle;
        int num = 0;

        while (num <  MAX_NUM_RETRIES) {
            // 1. Add other phone rather than initPhone sequentially
            for (int i = 0; i < TelephonyManager.getDefault().getSimCount(); i++) {
                int[] subIds = SubscriptionController.getInstance().getSubIdUsingSlotId(i);
                if (subIds == null || subIds.length == 0)
                    continue;

                int phoneId = SubscriptionController.getInstance().getPhoneId(subIds[0]);
                if (initPhoneId != phoneId) {
                    // If No SIM is inserted, the corresponding IccId will be null,
                    // take phoneId as PhoneAccountHandle::mId which is IccId originally
                    phoneAccountHandle = PhoneUtils.makePstnPhoneAccountHandle(
                            Integer.toString(phoneId));
                    mAttemptRecords.add(phoneAccountHandle);
                    num ++;
                    log("Add #" + num + " to ECC Retry list: " + phoneAccountHandle);
                }
            }

            // 2. Add initPhone at last
            phoneAccountHandle = PhoneUtils.makePstnPhoneAccountHandle(
                    Integer.toString(initPhoneId));
            mAttemptRecords.add(phoneAccountHandle);
            num ++;
            log("Add #" + num + " to ECC Retry list: " + phoneAccountHandle);
        }

        mAttemptRecordIterator = mAttemptRecords.iterator();
    }

    public void setCallId(String id) {
        log("setCallId = " + id);
        mCallId = id;
    }

    public String getCallId() {
        log("getCallId = " + mCallId);
        return mCallId;
    }

    public boolean isTimeout() {
        log("mNumRetriesSoFar = " + mNumRetriesSoFar);
        return (mNumRetriesSoFar >= MAX_NUM_RETRIES);
    }

    public ConnectionRequest getRequest() {
        log("mRequest = " + mRequest);
        return mRequest;
    }

    public PhoneAccountHandle getNextAccountHandle() {
        if (mAttemptRecordIterator.hasNext()) {
            mNumRetriesSoFar ++;
            log("getNextAccountHandle has Next");
            return mAttemptRecordIterator.next();
        }
        log("getNextAccountHandle is null");
        return null;
    }

    private void log(String s) {
        Log.d(TAG, s);
    }
}
