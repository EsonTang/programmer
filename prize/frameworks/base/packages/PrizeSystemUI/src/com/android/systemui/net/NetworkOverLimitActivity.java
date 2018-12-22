/*
 * Copyright (C) 2011 The Android Open Source Project
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

package com.android.systemui.net;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.INetworkPolicyManager;
import android.net.NetworkPolicy;
import android.net.NetworkTemplate;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import android.view.WindowManager;
import android.app.StatusBarManager;

import com.android.systemui.R;

import static android.net.NetworkPolicyManager.EXTRA_NETWORK_TEMPLATE;
import static android.net.NetworkTemplate.MATCH_MOBILE_3G_LOWER;
import static android.net.NetworkTemplate.MATCH_MOBILE_4G;
import static android.net.NetworkTemplate.MATCH_MOBILE_ALL;

/**
 * Notify user that a {@link NetworkTemplate} is over its
 * {@link NetworkPolicy#limitBytes}, giving them the choice of acknowledging or
 * "snoozing" the limit.
 */
public class NetworkOverLimitActivity extends Activity {
    private static final String TAG = "NetworkOverLimitActivity";

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        /*PRIZE-add for bugid:44874-liufan-2017-12-11-start*/
        getWindow().setDisableCustNavBarColor(true);
        /*PRIZE-add for bugid:44874-liufan-2017-12-11-end*/

        final NetworkTemplate template = getIntent().getParcelableExtra(EXTRA_NETWORK_TEMPLATE);
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getLimitedDialogTitleForTemplate(template));
        builder.setMessage(R.string.data_usage_disabled_dialog);

        builder.setPositiveButton(android.R.string.ok, null);
        builder.setNegativeButton(
                R.string.data_usage_disabled_dialog_enable, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        snoozePolicy(template);
                    }
                });

        final Dialog dialog = builder.create();
        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            public void onDismiss(DialogInterface dialog) {
                finish();
            }
        });
        /*PRIZE-add for bugid:45745-liufan-2017-12-18-start*/
        dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        dialog.getWindow().setDimAmount(0.5f);
        /*PRIZE-add for bugid:45745-liufan-2017-12-18-end*/

        dialog.show();
    }

    private void snoozePolicy(NetworkTemplate template) {
        final INetworkPolicyManager policyService = INetworkPolicyManager.Stub.asInterface(
                ServiceManager.getService(Context.NETWORK_POLICY_SERVICE));
        try {
            Log.i(TAG, "snoozeLimit pressed");
            policyService.snoozeLimit(template);
        } catch (RemoteException e) {
            Log.w(TAG, "problem snoozing network policy", e);
        }
    }

    private static int getLimitedDialogTitleForTemplate(NetworkTemplate template) {
        switch (template.getMatchRule()) {
            case MATCH_MOBILE_3G_LOWER:
                return R.string.data_usage_disabled_dialog_3g_title;
            case MATCH_MOBILE_4G:
                return R.string.data_usage_disabled_dialog_4g_title;
            case MATCH_MOBILE_ALL:
                return R.string.data_usage_disabled_dialog_mobile_title;
            default:
                return R.string.data_usage_disabled_dialog_title;
        }
    }
}
