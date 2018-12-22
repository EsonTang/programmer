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

package com.mediatek.simprocessor;

import java.util.HashMap;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.UserHandle;
import android.os.UserManager;

import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.TelephonyIntents;

import com.mediatek.simprocessor.SimProcessorService;
import com.mediatek.simprocessor.SimServiceUtils;
import com.mediatek.simprocessor.Log;

import android.os.SystemProperties;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;

public class BootCmpReceiver extends BroadcastReceiver {
    private static final String TAG = "BootCmpReceiver";
    private static final int ERROR_SUB_ID = -1000;
    public static final String NEED_REFRESH_SIM_CONTACTS = "need_refresh_sim_contacts";
    public static final String ACTION_REFRESH_SIM_CONTACT =
            "com.android.contacts.REFRESH_SIM_CONTACT";
    public static final boolean MTK_OWNER_SIM_SUPPORT =
            isPropertyEnabled("ro.mtk_owner_sim_support");

    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        Log.i(TAG, "[onReceive], action is " + action);

        // add for multi-user ALPS01964765, whether the current user is running.
        // if not , will do nothing.
        UserManager userManager = (UserManager) context.getSystemService(Context.USER_SERVICE);
        boolean isRunning = userManager.isUserRunning(new UserHandle(UserHandle.myUserId()));
        Log.d(TAG, "[onReceive], the current user is: " + UserHandle.myUserId()
                + " isRunning: " + isRunning);
        if (!isRunning) {
            return;
        }

        /**
         * M: Bug Fix for CR ALPS01328816: when other owner, do not show sms
         * when share contact @{
         */
        if (action.equals("android.intent.action.USER_SWITCHED_FOR_MULTIUSER_APP")
                && MTK_OWNER_SIM_SUPPORT) {
            if (UserHandle.myUserId() == UserHandle.USER_OWNER) {
                context.getPackageManager().setComponentEnabledSetting(
                        new ComponentName("com.android.contacts",
                                "com.mediatek.contacts.ShareContactViaSMSActivity"),
                        PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                        PackageManager.DONT_KILL_APP);
            } else {
                context.getPackageManager().setComponentEnabledSetting(
                        new ComponentName("com.android.contacts",
                                "com.mediatek.contacts.ShareContactViaSMSActivity"),
                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                        PackageManager.DONT_KILL_APP);
            }
            System.exit(0);
        }
        /** @} */

        /// M: Not support SIM Contacts in guest mode.
        if (UserHandle.myUserId() != UserHandle.USER_OWNER) {
            Log.i(TAG, "[onReceive], The current user isn't owner !");
            return;
        }

        /// Change for ALPS02377518, should prevent accessing SubInfo if has no
        // basic permissions.
        SharedPreferences perferences = context.getSharedPreferences(context.getPackageName(),
                Context.MODE_PRIVATE);
        if (SimProcessorUtils.hasBasicPermissions(context)) {
            if (TelephonyIntents.ACTION_PHB_STATE_CHANGED.equals(action)) {
                processPhoneBookChanged(context, intent);
            } else if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
                if (!isPhbReady()) {
                    processBootComplete(context);
                } else {
                    processDupSimContacts(context);
                }
            } else if (ACTION_REFRESH_SIM_CONTACT.equals(action)) {
                /// Add for ALPS02383518, when BootCmpReceiver received
                // PHB_CHANGED intent but has no READ_PHONE permission,
                //  marked NEED_REFRESH_SIM_CONTACTS as true. So refresh
                // all SIM contacts after open all permission and back to
                // contacts, this action is sent from PeopleActivity$onCreate. @{
                boolean needRefreshSIMContacts = perferences.getBoolean(
                        NEED_REFRESH_SIM_CONTACTS, false);
                if (needRefreshSIMContacts) {
                    resfreshAllSimContacts(context);
                    perferences.edit().putBoolean(NEED_REFRESH_SIM_CONTACTS, false).apply();
                } else if (!SimProcessorService.isSimProcessorRunning()) {
                    Log.i(TAG, "[onReceive], No need refresh and service is not running!");
                }
                /// @}
            }
        } else if (TelephonyIntents.ACTION_PHB_STATE_CHANGED.equals(action)
                || Intent.ACTION_BOOT_COMPLETED.equals(action)) {
            Log.e(TAG, "Contact has no basic permissions");
            perferences.edit().putBoolean(NEED_REFRESH_SIM_CONTACTS, true).apply();
            return;
        }
        /// @}
    }

    private boolean isPhbReady() {
        final int simCount = TelephonyManager.getDefault().getSimCount();
        Log.i(TAG, "isPhbReady simCount: " + simCount);
        for (int slotId = 0; slotId < simCount; slotId++) {
            int[] subId = SubscriptionManager.getSubId(slotId);
            if (subId != null && subId.length > 0 && SimCardUtils.isPhoneBookReady(subId[0])) {
                Log.i(TAG, "isPhbReady ready! ");
                return true;
            }
        }
        return false;
    }

    private void startSimService(Context context, int subId, int workType) {
        Intent intent = null;
        intent = new Intent(context, SimProcessorService.class);
        intent.putExtra(SimServiceUtils.SERVICE_SUBSCRIPTION_KEY, subId);
        intent.putExtra(SimServiceUtils.SERVICE_WORK_TYPE, workType);
        Log.d(TAG, "[startSimService]subId:" + subId + "|workType:" + workType);
        context.startService(intent);
    }

    private void processPhoneBookChanged(Context context, Intent intent) {
        Log.d(TAG, "processPhoneBookChanged");
        boolean phbReady = intent.getBooleanExtra("ready", false);
        int subId = intent.getIntExtra(PhoneConstants.SUBSCRIPTION_KEY, ERROR_SUB_ID);
        Log.d(TAG, "[processPhoneBookChanged]phbReady:" + phbReady + "|subId:" + subId);
        if (phbReady && subId > 0) {
            startSimService(context, subId, SimServiceUtils.SERVICE_WORK_IMPORT);
        } else if (subId > 0 && !phbReady) {
            startSimService(context, subId, SimServiceUtils.SERVICE_WORK_REMOVE);
        }
    }

    /**
     * fix for [PHB Status Refatoring] ALPS01003520
     * when boot complete,remove the contacts if the card of a slot had been removed
     */
    private void processBootComplete(Context context) {
        Log.d(TAG, "processBootComplete");
        startSimService(context, SimServiceUtils.SERVICE_FORCE_REMOVE_SUB_ID,
            SimServiceUtils.SERVICE_WORK_REMOVE);
    }

    private void processDupSimContacts(Context context) {
        Log.d(TAG, "processDupSimContacts");
        startSimService(context, SimServiceUtils.SERVICE_REMOVE_DUP_SUB_ID,
            SimServiceUtils.SERVICE_WORK_REMOVE);
    }

    public void resfreshAllSimContacts(Context context) {
        Log.i(TAG, "resfreshSimContacts");
        startSimService(context, SimServiceUtils.SERVICE_FORCE_REMOVE_SUB_ID,
                SimServiceUtils.SERVICE_WORK_REMOVE);
        List<SubscriptionInfo> subscriptionInfoList = SubscriptionManager.from(
                context).getActiveSubscriptionInfoList();
        if (subscriptionInfoList == null || subscriptionInfoList.size() == 0) {
            Log.i(TAG, "resfreshSimContacts has no sim.");
            return;
        }
        for (SubscriptionInfo subscriptionInfo : subscriptionInfoList) {
            Log.i(TAG, "resfreshSimContacts start get sub " + subscriptionInfo.getSubscriptionId());
            startSimService(context, subscriptionInfo.getSubscriptionId(),
                    SimServiceUtils.SERVICE_WORK_IMPORT);
        }
    }

    private static boolean isPropertyEnabled(String propertyString) {
        return "1".equals(SystemProperties.get(propertyString));
    }

}
