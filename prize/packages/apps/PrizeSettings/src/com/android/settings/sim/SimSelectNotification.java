/*
 * Copyright (C) 2014 The Android Open Source Project
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

package com.android.settings.sim;

import com.android.internal.telephony.IccCardConstants;
import com.android.internal.telephony.TelephonyIntents;
import com.android.settings.R;
import com.android.settings.Settings.SimSettingsActivity;
import com.mediatek.settings.UtilsExt;
import com.mediatek.settings.cdma.CdmaUtils;
import com.mediatek.settings.cdma.OmhEventHandler;
import com.mediatek.settings.ext.ISettingsMiscExt;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.SystemProperties;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.android.settings.Utils;

/// M: Add for CT 6M.
import com.mediatek.settings.FeatureOption;

import java.util.List;

public class SimSelectNotification extends BroadcastReceiver {
    private static final String TAG = "SimSelectNotification";
    private static final int NOTIFICATION_ID = 1;
    // Begin added  by minjiang.liao fix bug id 52910 on 2018/04/10
    private static boolean newSimDetected = false;
    // End added  by minjiang.liao fix bug id 52910 on 2018/04/10

    @Override
    public void onReceive(Context context, Intent intent) {
        /// M: add for auto sanity
        if (UtilsExt.shouldDisableForAutoSanity()) {
            return;
        }
        /// @}

        /// M: for [C2K 2 SIM Warning] @{
        // can not listen to SIM_STATE_CHANGED because it happened even when SIM switch
        // (switch default data), in which case should not show the C2K 2 SIM warning
        // SIM_STATE_CHANGED is not exact, maybe ICC is ready or absent,
        // but SubscriptionController is not ready.
        List<SubscriptionInfo> subs = SubscriptionManager.from(context)
                .getActiveSubscriptionInfoList();
        final int detectedType = intent.getIntExtra(
                SubscriptionManager.INTENT_KEY_DETECT_STATUS, 0);
        Log.d(TAG, "sub info update, type = " + detectedType + ", subs = " + subs);
        // Begin modified by  minjiang.liao for bug id 52910 on 2018/04/10
        if (detectedType != SubscriptionManager.EXTRA_VALUE_NOCHANGE) {
            Log.d(TAG, "newSimDetected set as true");
            newSimDetected = true;
            /*return;*/
        }
        // End modified by minjiang.liao for bug id 52910 on 2018/04/10
		
        // Begin added by minjiang.liao for bug id 52910/55400/56069 on 2018/04/10		
        /// @} 
        Log.d(TAG, "newsimdetected  : " + newSimDetected);
        if (!newSimDetected) {
            return;
        }
        if(subs != null) {
            int subSize = subs.size();
            for(int i = 0; i < subSize; i++) {
                CharSequence cDisplayName = subs.get(i).getDisplayName();
                Log.d(TAG, "display name is" + cDisplayName);
                if(cDisplayName != null) {
                    String displayName = cDisplayName.toString();
                    if (displayName.contains("CARD")) {
                        Log.d(TAG,"SIM display name is not ready yet");
                        return;
                    }
                }
            }
        }
        // End added by minjiang.liao for bug id 52910/55400/56069 on 2018/04/10

        final TelephonyManager telephonyManager = (TelephonyManager)
                context.getSystemService(Context.TELEPHONY_SERVICE);
        final SubscriptionManager subscriptionManager = SubscriptionManager.from(context);
        final int numSlots = telephonyManager.getSimCount();
        final boolean isInProvisioning = Settings.Global.getInt(context.getContentResolver(),
                Settings.Global.DEVICE_PROVISIONED, 0) == 0;

        // Do not create notifications on single SIM devices or when provisioning i.e. Setup Wizard.
        if (numSlots < 2 || isInProvisioning) {
            return;
        }

        // Cancel any previous notifications
        cancelNotification(context);

        // If sim state is not ABSENT or LOADED then ignore
        String simStatus = intent.getStringExtra(IccCardConstants.INTENT_KEY_ICC_STATE);
        if (!(IccCardConstants.INTENT_VALUE_ICC_ABSENT.equals(simStatus) ||
                IccCardConstants.INTENT_VALUE_ICC_LOADED.equals(simStatus))) {
            Log.d(TAG, "sim state is not Absent or Loaded");
            /// M: maybe ACTION_SUBINFO_RECORD_UPDATED is received, @{
            //  but ICC card don't loaded or absent
            /*
            return;
            */
            /// @}
        } else {
            Log.d(TAG, "simstatus = " + simStatus);
        }

        int state;
        for (int i = 0; i < numSlots; i++) {
            state = telephonyManager.getSimState(i);
            if (!(state == TelephonyManager.SIM_STATE_ABSENT
                    || state == TelephonyManager.SIM_STATE_READY
                    || state == TelephonyManager.SIM_STATE_UNKNOWN)) {
                Log.d(TAG, "All sims not in valid state yet");
                /// M: maybe ACTION_SUBINFO_RECORD_UPDATED is received, @{
                //  but ICC card don't loaded or absent
                /*
                return;
                 */
                /// @}
            }
        }

        List<SubscriptionInfo> sil = subscriptionManager.getActiveSubscriptionInfoList();
        if (sil == null || sil.size() < 1) {
            Log.d(TAG, "Subscription list is empty");
            return;
        }

        // Clear defaults for any subscriptions which no longer exist
        subscriptionManager.clearDefaultsForInactiveSubIds();

        boolean dataSelected = SubscriptionManager.isUsableSubIdValue(
                SubscriptionManager.getDefaultDataSubscriptionId());
        boolean smsSelected = SubscriptionManager.isUsableSubIdValue(
                SubscriptionManager.getDefaultSmsSubscriptionId());
        Log.d(TAG, "dataSelected = " + dataSelected + " smsSelected = " + dataSelected);

        // If data and sms defaults are selected, dont show notification (Calls default is optional)
        if (dataSelected && smsSelected
            /// M: For Op01 open market. @{
                && !SystemProperties.get("ro.cmcc_light_cust_support").equals("1")
            /// @}
            ) {
            Log.d(TAG, "Data & SMS default sims are selected. No notification");
            return;
        }
          
		// Begin deleted by liaominjian on 2018/04/26 to disable sim change notification
        // Create a notification to tell the user that some defaults are missing
        // createNotification(context);
		// End deleted by liaominjian on 2018/04/26 to disable sim change notification

        /// M: for plug-in and CT 6M
        if (!isSimDialogNeeded(context)) {
            Log.d(TAG, "sim dialog not needed, RETURN!");
            return;
        }
        if (sil.size() == 1) {
            /// M: For Op01 open market,show no notification. @{
                if (SystemProperties.get("ro.cmcc_light_cust_support").equals("1")) {
                    Log.d(TAG, "size == 1,show no notification");
                    return;
                }
            /// @}
            // Begin added by minjiang.liao for bug id 52910 on 2018/04/10
            newSimDetected = false;
            // End added by minjiang.liao for bug id 52910 on 2018/04/10

            // If there is only one subscription, ask if user wants to use if for everything
            Intent newIntent = new Intent(context, SimDialogActivity.class);
            newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            newIntent.putExtra(SimDialogActivity.DIALOG_TYPE_KEY, SimDialogActivity.PREFERRED_PICK);
            newIntent.putExtra(SimDialogActivity.PREFERRED_SIM, sil.get(0).getSimSlotIndex());
            context.startActivity(newIntent);
            /// M: for [C2K OMH Warning]
            OmhEventHandler.getInstance(context).sendEmptyMessage(OmhEventHandler.SET_BUSY);
        } else if (!dataSelected ||
            /// M: Op01 open market request. @{
                SystemProperties.get("ro.cmcc_light_cust_support").equals("1")) {
            /// @}
            // If there are multiple, ensure they pick default data
            // Begin added by minjiang.liao for bug id 52910 on 2018/04/10
            newSimDetected = false;
            // End added by minjiang.liao for bug id 52910 on 2018/04/10
            Intent newIntent = new Intent(context, SimDialogActivity.class);
            newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            newIntent.putExtra(SimDialogActivity.DIALOG_TYPE_KEY, SimDialogActivity.DATA_PICK);
            context.startActivity(newIntent);
            /// M: for [C2K OMH Warning]
            OmhEventHandler.getInstance(context).sendEmptyMessage(OmhEventHandler.SET_BUSY);
        }
    }

    private void createNotification(Context context){
        final Resources resources = context.getResources();
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_sim_card_alert_white_48dp)
                .setColor(context.getColor(R.color.sim_noitification))
                .setContentTitle(resources.getString(R.string.sim_notification_title))
                .setContentText(resources.getString(R.string.sim_notification_summary));

        /// M: for plug-in
        customizeSimDisplay(context, builder);

        Intent resultIntent = new Intent(context, SimSettingsActivity.class);
        resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(context, 0, resultIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);
        builder.setContentIntent(resultPendingIntent);
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    public static void cancelNotification(Context context) {
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIFICATION_ID);
    }

    ///---------------------------------------MTK-------------------------------------------------

    /**
     * only for plug-in, change "SIM" to "UIM/SIM".
     * @param context the context.
     * @param builder the notification builder.
     */
    private void customizeSimDisplay(
                    Context context,
                    NotificationCompat.Builder builder) {
        Resources resources = context.getResources();
        String title = resources.getString(R.string.sim_notification_title);
        String text = resources.getString(R.string.sim_notification_summary);

        ISettingsMiscExt miscExt = UtilsExt.getMiscPlugin(context);
        title = miscExt.customizeSimDisplayString(
                            title,
                            SubscriptionManager.INVALID_SUBSCRIPTION_ID);
        text = miscExt.customizeSimDisplayString(
                            text,
                            SubscriptionManager.INVALID_SUBSCRIPTION_ID);
        builder.setContentTitle(title);
        builder.setContentText(text);
    }

    /**
     *  add for plug-in and CT 6M.
     *  whether allowed show data pick dailog and preferred sim dialog.
     */
    private boolean isSimDialogNeeded(Context context) {
        return UtilsExt.getSimManagmentExtPlugin(context).isSimDialogNeeded();
    }
}
