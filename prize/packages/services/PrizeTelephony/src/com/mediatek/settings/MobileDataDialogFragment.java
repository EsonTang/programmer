/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein
 * is confidential and proprietary to MediaTek Inc. and/or its licensors.
 * Without the prior written permission of MediaTek inc. and/or its licensors,
 * any reproduction, modification, use or disclosure of MediaTek Software,
 * and information contained herein, in whole or in part, shall be strictly prohibited.
 */
/* MediaTek Inc. (C) 2017. All rights reserved.
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

package com.mediatek.settings;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.android.phone.R;
import com.android.phone.SubscriptionInfoHelper;

/**
 * A dialog fragment that notify the user if they are sure they want to temporarily use data.
 */
public class MobileDataDialogFragment extends DialogFragment implements OnClickListener {

    private static final String LOG_TAG = "MobileDataDialogFragment";
    private static final String DIALOG_NAME = "DataTrafficDialog";
    private static final String ARG_INTENT = "intent";
    private static final String ARG_MESSAGE = "message";

    /**
     * Show a tip dialog, notify the user if they are sure they want to temporarily use data.
     * @param intent the intent used by startActivity
     * @param message the information to show
     * @param fragmentManager the fragment manager
     */
    public static void show(Intent intent, String message, FragmentManager fragmentManager) {
        MobileDataDialogFragment dialog = new MobileDataDialogFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_INTENT, intent);
        args.putString(ARG_MESSAGE, message);
        dialog.setArguments(args);
        dialog.setCancelable(false);
        dialog.show(fragmentManager, DIALOG_NAME);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String message = getArguments().getString(ARG_MESSAGE);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(message)
                .setIconAttribute(android.R.attr.alertDialogIcon)
                .setPositiveButton(R.string.alert_dialog_ok, this)
                .setNegativeButton(android.R.string.cancel, this);
        builder.setCancelable(false);
        AlertDialog alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        return alertDialog;
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (which == dialog.BUTTON_POSITIVE) {
            Intent intent = getArguments().getParcelable(ARG_INTENT);
            if (intent != null) {
                int subId = intent.getIntExtra(SubscriptionInfoHelper.SUB_ID_EXTRA,
                        SubscriptionInfoHelper.NO_SUB_ID);
                Log.d(LOG_TAG, "subId = " + subId);
                getActivity().startActivity(intent);
            }
        }
    }
}
