/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein
 * is confidential and proprietary to MediaTek Inc. and/or its licensors.
 * Without the prior written permission of MediaTek inc. and/or its licensors,
 * any reproduction, modification, use or disclosure of MediaTek Software,
 * and information contained herein, in whole or in part, shall be strictly prohibited.
 *
 * MediaTek Inc. (C) 2010. All rights reserved.
 *
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER ON
 * AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL WARRANTIES,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NONINFRINGEMENT.
 * NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH RESPECT TO THE
 * SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY, INCORPORATED IN, OR
 * SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES TO LOOK ONLY TO SUCH
 * THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO. RECEIVER EXPRESSLY ACKNOWLEDGES
 * THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES
 * CONTAINED IN MEDIATEK SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK
 * SOFTWARE RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S ENTIRE AND
 * CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE RELEASED HEREUNDER WILL BE,
 * AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE MEDIATEK SOFTWARE AT ISSUE,
 * OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE CHARGE PAID BY RECEIVER TO
 * MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek Software")
 * have been modified by MediaTek Inc. All revisions are subject to any receiver's
 * applicable license agreements with MediaTek Inc.
 */
package com.mediatek.contacts;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Groups;
import android.text.TextUtils;
import android.util.SparseIntArray;
import android.widget.Toast;

import com.android.contacts.ContactSaveService;
import com.android.contacts.R;
import com.android.contacts.common.model.account.AccountWithDataSet;

import com.mediatek.contacts.util.Log;

import java.util.ArrayList;

public class ContactSaveServiceEx {
    private static final String TAG = "ContactSaveServiceEx";

    // :[Gemini+] all possible slot error can be safely put in this sparse int
    // array.
    private static SparseIntArray mSubIdError = new SparseIntArray();

    private static Handler mMainHandler = new Handler(Looper.getMainLooper());

    /**
     * Shows a toast on the UI thread.
     */
    private static void showToast(final int message) {
        mMainHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(ContactsApplicationEx.getContactsApplication(), message,
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    public static boolean checkGroupNameExist(ContactSaveService saveService, String groupName,
            String accountName, String accountType, boolean showTips) {
        boolean nameExists = false;

        if (TextUtils.isEmpty(groupName)) {
            if (showTips) {
                showToast(R.string.name_needed);
            }
            return false;
        }
        Cursor cursor = saveService.getContentResolver().query(
                Groups.CONTENT_SUMMARY_URI,
                new String[] { Groups._ID },
                Groups.TITLE + "=? AND " + Groups.ACCOUNT_NAME + " =? AND " + Groups.ACCOUNT_TYPE
                        + "=? AND " + Groups.DELETED + "=0",
                new String[] { groupName, accountName, accountType }, null);
        if (cursor != null) {
            if (cursor.getCount() > 0) {
                nameExists = true;
            }
            cursor.close();
        }
        // If group name exists, make a toast and return false.
        if (nameExists) {
            if (showTips) {
                showToast(R.string.group_name_exists);
            }
            return false;
        } else {
            return true;
        }
    }

    public static Intent addCheckGroupExistIntent(Intent serviceIntent,
            AccountWithDataSet account) {
        serviceIntent.putExtra(ContactSaveService.EXTRA_ACCOUNT_TYPE, account.type);
        serviceIntent.putExtra(ContactSaveService.EXTRA_ACCOUNT_NAME, account.name);
        serviceIntent.putExtra(ContactSaveService.EXTRA_DATA_SET, account.dataSet);
        return serviceIntent;
    }

    /**
     * fix ALPS00272729
     *
     * @param operations
     * @param resolver
     */
    public static void bufferOperations(ArrayList<ContentProviderOperation> operations,
            ContentResolver resolver) {
        try {
            Log.d(TAG, "[bufferOperatation] begin applyBatch ");
            resolver.applyBatch(ContactsContract.AUTHORITY, operations);
            Log.d(TAG, "[bufferOperatation] end applyBatch");
            operations.clear();
        } catch (RemoteException e) {
            Log.e(TAG, "[bufferOperatation]RemoteException:", e);
            showToast(R.string.contactSavedErrorToast);
        } catch (OperationApplicationException e) {
            Log.e(TAG, "[bufferOperatation]OperationApplicationException:", e);
            showToast(R.string.contactSavedErrorToast);
        }
    }
}
