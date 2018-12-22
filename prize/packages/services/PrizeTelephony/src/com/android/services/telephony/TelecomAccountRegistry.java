/*
* Copyright (C) 2014 MediaTek Inc.
* Modification based on code covered by the mentioned copyright
* and/or permission notice(s).
*/
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

package com.android.services.telephony;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.PersistableBundle;
import android.os.SystemProperties;
import android.provider.Settings;

import android.os.ServiceManager;

import android.os.UserHandle;
import android.os.UserManager;

import android.telecom.PhoneAccount;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;
import android.telephony.CarrierConfigManager;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.SubscriptionManager.OnSubscriptionsChangedListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.ArrayMap;

import com.android.ims.ImsConfig;
import com.android.ims.ImsException;
import com.android.ims.ImsManager;

import com.android.internal.telephony.IPhoneSubInfo;

import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.PhoneFactory;
import com.android.phone.PhoneGlobals;
import com.android.phone.PhoneUtils;
import com.android.phone.R;
import com.mediatek.telephony.TelephonyManagerEx;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import java.util.Map;

import java.util.Optional;

/**
 * Owns all data we have registered with Telecom including handling dynamic addition and
 * removal of SIMs and SIP accounts.
 */
final class TelecomAccountRegistry {
    private static final boolean DBG = false; /* STOP SHIP if true */

    // This icon is the one that is used when the Slot ID that we have for a particular SIM
    // is not supported, i.e. SubscriptionManager.INVALID_SLOT_ID or the 5th SIM in a phone.
    private final static int DEFAULT_SIM_ICON =  R.drawable.ic_multi_sim;
    private final static String GROUP_PREFIX = "group_";

    final class AccountEntry implements PstnPhoneCapabilitiesNotifier.Listener {
        private final Phone mPhone;
        private PhoneAccount mAccount;
        private final PstnIncomingCallNotifier mIncomingCallNotifier;
        private final PstnPhoneCapabilitiesNotifier mPhoneCapabilitiesNotifier;
        private boolean mIsEmergency;
        private boolean mIsDummy;
        private boolean mIsVideoCapable;
        private boolean mIsVideoPresenceSupported;
        private boolean mIsVideoPauseSupported;
        private boolean mIsMergeCallSupported;
        private boolean mIsVideoConferencingSupported;
        private boolean mIsMergeOfWifiCallsAllowedWhenVoWifiOff;

        AccountEntry(Phone phone, boolean isEmergency, boolean isDummy) {
            mPhone = phone;
            mIsEmergency = isEmergency;
            mIsDummy = isDummy;
            mAccount = registerPstnPhoneAccount(isEmergency, isDummy);
            Log.i(this, "Registered phoneAccount: %s with handle: %s",
                    mAccount, mAccount.getAccountHandle());
            mIncomingCallNotifier = new PstnIncomingCallNotifier((Phone) mPhone);
            mPhoneCapabilitiesNotifier = new PstnPhoneCapabilitiesNotifier((Phone) mPhone,
                    this);
        }

        void teardown() {
            mIncomingCallNotifier.teardown();
            mPhoneCapabilitiesNotifier.teardown();
        }

        /**
         * Registers the specified account with Telecom as a PhoneAccountHandle.
         * M: For ALPS01965388. Only create a PhoneAccount but not register it to Telecom.
         */
        private PhoneAccount registerPstnPhoneAccount(boolean isEmergency,
                                                    boolean isDummyAccount) {
            String dummyPrefix = isDummyAccount ? "Dummy " : "";

            // Build the Phone account handle.
            PhoneAccountHandle phoneAccountHandle =
                    PhoneUtils.makePstnPhoneAccountHandleWithPrefix(
                            mPhone, dummyPrefix, isEmergency);

            // Populate the phone account data.
            int subId = mPhone.getSubId();
            String subscriberId = mPhone.getSubscriberId();
            int color = PhoneAccount.NO_HIGHLIGHT_COLOR;
            int slotId = SubscriptionManager.INVALID_SIM_SLOT_INDEX;
            String line1Number = mTelephonyManager.getLine1Number(subId);
            if (line1Number == null) {
                line1Number = "";
            }
            String subNumber = mPhone.getLine1Number();
            if (subNumber == null) {
                subNumber = "";
            }

            String label;
            String description;
            Icon icon = null;

            // We can only get the real slotId from the SubInfoRecord, we can't calculate the
            // slotId from the subId or the phoneId in all instances.
            SubscriptionInfo record =
                    mSubscriptionManager.getActiveSubscriptionInfo(subId);

            if (isEmergency) {
                label = mContext.getResources().getString(R.string.sim_label_emergency_calls);
                description =
                    mContext.getResources().getString(R.string.sim_description_emergency_calls);

                Log.d(TelecomAccountRegistry.this, "[temp debug remove later] emergency");
            }
            /// M: for ALPS01774567, remove these code, make the account name always same. @{
            // Original code:
            // else if (mTelephonyManager.getPhoneCount() == 1) {
            // For single-SIM devices, we show the label and description as whatever the name of
            //     // the network is.
            //     description = label = mTelephonyManager.getNetworkOperatorName();
            // }
            /// @}
            else {
                // M: for ALPS01772299, don't change it if name is empty, init it as "".
                CharSequence subDisplayName = "";
                if (record != null) {
                    subDisplayName = record.getDisplayName();
                    slotId = record.getSimSlotIndex();
                    color = record.getIconTint();
                    icon = Icon.createWithBitmap(record.createIconBitmap(mContext));
                    // M: for ALPS01804842, set sub number as address
                    subNumber = record.getNumber();
                }
                Log.d(TelecomAccountRegistry.this, "[temp debug remove later] icon created");

                String slotIdString;
                if (SubscriptionManager.isValidSlotId(slotId)) {
                    slotIdString = Integer.toString(slotId);
                } else {
                    slotIdString = mContext.getResources().getString(R.string.unknown);
                }

                /// M: for ALPS01772299, don't change it if name is empty. @{
                // original code:
                // if (TextUtils.isEmpty(subDisplayName)) {
                //     // Either the sub record is not there or it has an empty display name.
                //     Log.w(this, "Could not get a display name for subid: %d", subId);
                //     subDisplayName = mContext.getResources().getString(
                //             R.string.sim_description_default, slotIdString);
                //     subDisplayName = "";
                // }
                /// @}

                Log.d(TelecomAccountRegistry.this, "[temp debug remove later] " +
                        "retrieve description+");
                // The label is user-visible so let's use the display name that the user may
                // have set in Settings->Sim cards.
                label = dummyPrefix + subDisplayName;
                description = dummyPrefix + mContext.getResources().getString(
                                R.string.sim_description_default, slotIdString);
                Log.d(TelecomAccountRegistry.this, "[temp debug remove later] " +
                        "retrieve description- : " + description);
            }
            if (subNumber == null) {
                subNumber = "";
            }

            // By default all SIM phone accounts can place emergency calls.
            int capabilities = PhoneAccount.CAPABILITY_SIM_SUBSCRIPTION |
                    PhoneAccount.CAPABILITY_CALL_PROVIDER |
                    PhoneAccount.CAPABILITY_MULTI_USER;

            if (mContext.getResources().getBoolean(R.bool.config_pstnCanPlaceEmergencyCalls)) {
                capabilities |= PhoneAccount.CAPABILITY_PLACE_EMERGENCY_CALLS;
            }
            // M: attach extended capabilities for the PhoneAccount
            // M: fix CR:ALPS03127035,dialog occur twice.
            if (!isEmergency) {
                capabilities |= getExtendedCapabilities();
            }

            mIsVideoCapable = mPhone.isVideoEnabled();

            if (!mIsPrimaryUser) {
                Log.i(this, "Disabling video calling for secondary user.");
                mIsVideoCapable = false;
            }

            /// M: manual open video call entry
            // if the property "manual.enable.video.call" value is 1,
            // then enable, other value disable.
            int val = SystemProperties.getInt("manual.enable.video.call", -1);
            if (mIsVideoCapable || val == 1) {
                capabilities |= PhoneAccount.CAPABILITY_VIDEO_CALLING;
            }

            mIsVideoPresenceSupported = isCarrierVideoPresenceSupported();
            if (mIsVideoCapable && mIsVideoPresenceSupported) {
                capabilities |= PhoneAccount.CAPABILITY_VIDEO_CALLING_RELIES_ON_PRESENCE;
            }

            if (mIsVideoCapable && isCarrierEmergencyVideoCallsAllowed()) {
                capabilities |= PhoneAccount.CAPABILITY_EMERGENCY_VIDEO_CALLING;
            }

            mIsVideoPauseSupported = isCarrierVideoPauseSupported();
            Bundle instantLetteringExtras = null;
            if (isCarrierInstantLetteringSupported()) {
                capabilities |= PhoneAccount.CAPABILITY_CALL_SUBJECT;
                instantLetteringExtras = getPhoneAccountExtras();
            }
            mIsMergeCallSupported = isCarrierMergeCallSupported();
            mIsVideoConferencingSupported = isCarrierVideoConferencingSupported();
            mIsMergeOfWifiCallsAllowedWhenVoWifiOff =
                    isCarrierMergeOfWifiCallsAllowedWhenVoWifiOff();

            if (isEmergency && mContext.getResources().getBoolean(
                    R.bool.config_emergency_account_emergency_calls_only)) {
                capabilities |= PhoneAccount.CAPABILITY_EMERGENCY_CALLS_ONLY;
            }

            if (icon == null) {
                // TODO: Switch to using Icon.createWithResource() once that supports tinting.
                Resources res = mContext.getResources();
                Drawable drawable = res.getDrawable(DEFAULT_SIM_ICON, null);
                drawable.setTint(res.getColor(R.color.default_sim_icon_tint_color, null));
                drawable.setTintMode(PorterDuff.Mode.SRC_ATOP);

                int width = drawable.getIntrinsicWidth();
                int height = drawable.getIntrinsicHeight();
                Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                drawable.draw(canvas);

                icon = Icon.createWithBitmap(bitmap);
            }

            // Check to see if the newly registered account should replace the old account.
            String groupId = "";
            String[] mergedImsis = mTelephonyManager.getMergedSubscriberIds();
            boolean isMergedSim = false;
            if (mergedImsis != null && subscriberId != null && !isEmergency) {
                for (String imsi : mergedImsis) {
                    if (imsi.equals(subscriberId)) {
                        isMergedSim = true;
                        break;
                    }
                }
            }
            if(isMergedSim) {
                groupId = GROUP_PREFIX + line1Number;
                Log.i(this, "Adding Merged Account with group: " + Log.pii(groupId));
            }

            PhoneAccount account = PhoneAccount.builder(phoneAccountHandle, label)
                     // M: for ALPS01804842, set sub number as address
                    .setAddress(Uri.fromParts(PhoneAccount.SCHEME_TEL, subNumber, null))
                    .setSubscriptionAddress(
                            Uri.fromParts(PhoneAccount.SCHEME_TEL, subNumber, null))
                    .setCapabilities(capabilities)
                    .setIcon(icon)
                    .setHighlightColor(color)
                    .setShortDescription(description)
                    .setSupportedUriSchemes(Arrays.asList(
                            PhoneAccount.SCHEME_TEL, PhoneAccount.SCHEME_VOICEMAIL))
                    .setExtras(putSortKey(instantLetteringExtras))
                    .setGroupId(groupId)
                    .build();

            /// M: For ALPS01965388.
            /** Original code: */
            // Register with Telecom and put into the account entry.
            // mTelecomManager.registerPhoneAccount(account);
            // move this operation to a thread
            //registerIfChanged(account);
            /// @}
            return account;
        }

        public PhoneAccountHandle getPhoneAccountHandle() {
            return mAccount != null ? mAccount.getAccountHandle() : null;
        }

        /**
         * Determines from carrier configuration whether pausing of IMS video calls is supported.
         *
         * @return {@code true} if pausing IMS video calls is supported.
         */
        private boolean isCarrierVideoPauseSupported() {
            // Check if IMS video pause is supported.
            PersistableBundle b =
                    PhoneGlobals.getInstance().getCarrierConfigForSubId(mPhone.getSubId());
            return b != null &&
                    b.getBoolean(CarrierConfigManager.KEY_SUPPORT_PAUSE_IMS_VIDEO_CALLS_BOOL);
        }

        /**
         * Determines from carrier configuration whether RCS presence indication for video calls is
         * supported.
         *
         * @return {@code true} if RCS presence indication for video calls is supported.
         */
        private boolean isCarrierVideoPresenceSupported() {
            PersistableBundle b =
                    PhoneGlobals.getInstance().getCarrierConfigForSubId(mPhone.getSubId());
            return b != null &&
                    b.getBoolean(CarrierConfigManager.KEY_USE_RCS_PRESENCE_BOOL);
        }

        /**
         * Determines from carrier config whether instant lettering is supported.
         *
         * @return {@code true} if instant lettering is supported, {@code false} otherwise.
         */
        private boolean isCarrierInstantLetteringSupported() {
            PersistableBundle b =
                    PhoneGlobals.getInstance().getCarrierConfigForSubId(mPhone.getSubId());
            return b != null &&
                    b.getBoolean(CarrierConfigManager.KEY_CARRIER_INSTANT_LETTERING_AVAILABLE_BOOL);
        }

        /**
         * Determines from carrier config whether merging calls is supported.
         *
         * @return {@code true} if merging calls is supported, {@code false} otherwise.
         */
        private boolean isCarrierMergeCallSupported() {
            PersistableBundle b =
                    PhoneGlobals.getInstance().getCarrierConfigForSubId(mPhone.getSubId());
            return b != null &&
                    b.getBoolean(CarrierConfigManager.KEY_SUPPORT_CONFERENCE_CALL_BOOL);
        }

        /**
         * Determines from carrier config whether emergency video calls are supported.
         *
         * @return {@code true} if emergency video calls are allowed, {@code false} otherwise.
         */
        private boolean isCarrierEmergencyVideoCallsAllowed() {
            PersistableBundle b =
                    PhoneGlobals.getInstance().getCarrierConfigForSubId(mPhone.getSubId());
            return b != null &&
                    b.getBoolean(CarrierConfigManager.KEY_ALLOW_EMERGENCY_VIDEO_CALLS_BOOL);
        }

        /**
         * Determines from carrier config whether video conferencing is supported.
         *
         * @return {@code true} if video conferencing is supported, {@code false} otherwise.
         */
        private boolean isCarrierVideoConferencingSupported() {
            PersistableBundle b =
                    PhoneGlobals.getInstance().getCarrierConfigForSubId(mPhone.getSubId());
            return b != null &&
                    b.getBoolean(CarrierConfigManager.KEY_SUPPORT_VIDEO_CONFERENCE_CALL_BOOL);
        }

        /**
         * Determines from carrier config whether merging of wifi calls is allowed when VoWIFI is
         * turned off.
         *
         * @return {@code true} merging of wifi calls when VoWIFI is disabled should be prevented,
         *      {@code false} otherwise.
         */
        private boolean isCarrierMergeOfWifiCallsAllowedWhenVoWifiOff() {
            PersistableBundle b =
                    PhoneGlobals.getInstance().getCarrierConfigForSubId(mPhone.getSubId());
            return b != null && b.getBoolean(
                    CarrierConfigManager.KEY_ALLOW_MERGE_WIFI_CALLS_WHEN_VOWIFI_OFF_BOOL);
        }

        /**
         * @return The {@link PhoneAccount} extras associated with the current subscription.
         */
        private Bundle getPhoneAccountExtras() {
            PersistableBundle b =
                    PhoneGlobals.getInstance().getCarrierConfigForSubId(mPhone.getSubId());

            int instantLetteringMaxLength = b.getInt(
                    CarrierConfigManager.KEY_CARRIER_INSTANT_LETTERING_LENGTH_LIMIT_INT);
            String instantLetteringEncoding = b.getString(
                    CarrierConfigManager.KEY_CARRIER_INSTANT_LETTERING_ENCODING_STRING);

            Bundle phoneAccountExtras = new Bundle();
            phoneAccountExtras.putInt(PhoneAccount.EXTRA_CALL_SUBJECT_MAX_LENGTH,
                    instantLetteringMaxLength);
            phoneAccountExtras.putString(PhoneAccount.EXTRA_CALL_SUBJECT_CHARACTER_ENCODING,
                    instantLetteringEncoding);
            return phoneAccountExtras;
        }

        /**
         * Receives callback from {@link PstnPhoneCapabilitiesNotifier} when the video capabilities
         * have changed.
         *
         * @param isVideoCapable {@code true} if video is capable.
         */
        @Override
        public void onVideoCapabilitiesChanged(boolean isVideoCapable) {
            mIsVideoCapable = isVideoCapable;
            synchronized (mAccountsLock) {
                if (!mAccounts.contains(this)) {
                    // Account has already been torn down, don't try to register it again.
                    // This handles the case where teardown has already happened, and we got a video
                    // update that lost the race for the mAccountsLock.  In such a scenario by the
                    // time we get here, the original phone account could have been torn down.
                    return;
                }
                mAccount = registerPstnPhoneAccount(mIsEmergency, mIsDummy);
                mHandler.obtainMessage(MSG_REGISTER_ONLY, mAccount).sendToTarget();
            }
        }

        /**
         * Indicates whether this account supports pausing video calls.
         * @return {@code true} if the account supports pausing video calls, {@code false}
         * otherwise.
         */
        public boolean isVideoPauseSupported() {
            return mIsVideoCapable && mIsVideoPauseSupported;
        }

        /**
         * M: get extended PhoneAccount capabilities currently.
         * @return the extended capability bit mask.
         */
        private int getExtendedCapabilities() {
            int extendedCapabilities = 0;
            if (mPhone.isWifiCallingEnabled()) {
                extendedCapabilities |= PhoneAccount.CAPABILITY_WIFI_CALLING;
            }
            boolean isImsReg = false;
            boolean isImsEnabled =
                    ImsManager.isEnhanced4gLteModeSettingEnabledByUser(mPhone.getContext(),
                    mPhone.getPhoneId());
            if (isImsEnabled) {
                try {
                    ImsManager imsManager = ImsManager.getInstance(mContext, mPhone.getPhoneId());
                    isImsReg = imsManager.getImsRegInfo();
                    ///M: we need also check if current network type is LTE or LTE+
                    // to add VoLTE calling capability, if not remove the volte capability. @{
                    int networkType = mTelephonyManager.getNetworkType(mPhone.getSubId());
                    if (isImsReg && (networkType == TelephonyManager.NETWORK_TYPE_LTE ||
                            networkType == TelephonyManager.NETWORK_TYPE_LTEA)) {
                        extendedCapabilities |= PhoneAccount.CAPABILITY_VOLTE_CALLING;
                        /// For Volte enhanced conference feature. @{
                        if (mPhone.isFeatureSupported(Phone.FeatureType.
                                                    VOLTE_ENHANCED_CONFERENCE)) {
                            extendedCapabilities |=
                                           PhoneAccount.CAPABILITY_VOLTE_CONFERENCE_ENHANCED;
                        }
                        /// @}
                    }
                    /// @}
                } catch (ImsException e) {
                    Log.v(this, "Get IMS register info fail.");
                }
            }

            /// Added for EVDO world phone.@{
            if (mPhone.getPhoneType() == PhoneConstants.PHONE_TYPE_CDMA) {
                extendedCapabilities |= PhoneAccount.CAPABILITY_CDMA_CALL_PROVIDER;
            }
            /// @}

            Log.v(TelecomAccountRegistry.this,
                    "PhoneId: %s, Ims enabled: %s, ImsReg: %s, extendedCapability: %d",
                    mPhone.getPhoneId(), isImsEnabled, isImsReg, extendedCapabilities);
            return extendedCapabilities;
        }

        /**
         * Indicates whether this account supports merging calls (i.e. conferencing).
         * @return {@code true} if the account supports merging calls, {@code false} otherwise.
         */
        public boolean isMergeCallSupported() {
            return mIsMergeCallSupported;
        }

        /**
         * Indicates whether this account supports video conferencing.
         * @return {@code true} if the account supports video conferencing, {@code false} otherwise.
         */
        public boolean isVideoConferencingSupported() {
            return mIsVideoConferencingSupported;
        }

        /**
         * Indicate whether this account allow merging of wifi calls when VoWIFI is off.
         * @return {@code true} if allowed, {@code false} otherwise.
         */
        public boolean isMergeOfWifiCallsAllowedWhenVoWifiOff() {
            return mIsMergeOfWifiCallsAllowedWhenVoWifiOff;
        }

        /// M: add subId to the Extras for easier access.
        /// add phoneId as new OrderKey about PhoneAccount for faster sorting.@{
        private Bundle putExtExtras(Bundle extra) {
            Bundle newExtra = extra;
            if (newExtra == null) {
                newExtra = new Bundle();
            }
            newExtra.putInt(PhoneAccount.EXTRA_PHONE_ACCOUNT_SORT_KEY, mPhone.getPhoneId());
            newExtra.putInt(PhoneAccount.EXTRA_EXT_SUBSCRIPTION_ID, mPhone.getSubId());
            return newExtra;
        }
        /// @}

        /// M: fix CR:ALPS02837867,selectAccount show order error about sim2,sim1.
        /// add phoneId as new OrderKey about PhoneAccount.@{
        private Bundle putSortKey(Bundle extra) {
            Bundle newExtra = extra;
            if (newExtra == null) {
                newExtra = new Bundle();
            }
            newExtra.putInt(PhoneAccount.EXTRA_PHONE_ACCOUNT_SORT_KEY, mPhone.getPhoneId());
            return newExtra;
        }
        /// @}
    }

    private OnSubscriptionsChangedListener mOnSubscriptionsChangedListener =
            new OnSubscriptionsChangedListener() {
        @Override
        public void onSubscriptionsChanged() {
            ///M: for some case(like IPO bootup), it will trigger subscription changed
            // by SST update SPN at which time the sim cards are not initialized.
            // in this case the subId would be dummy value and we would think the sim cards are
            // plug-out by mistake (actually its not initialized).

            // to avoid this case we need checking the ready status using below function.
            // when the sim card is plug out or sim card initialized
            // the "isReady" will return true. @{
//            if (!com.android.internal.telephony.SubscriptionController.getInstance().isReady()) {
//                return ;
//            }
            /// @}

            // Any time the SubscriptionInfo changes...rerun the setup
            tearDownAccounts();
            setupAccounts();

            /// M: Maybe need to update PhoneStateListener.
            updatePhoneStateListeners();
        }
    };

    private final BroadcastReceiver mUserSwitchedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(this, "User changed, re-registering phone accounts.");

            int userHandleId = intent.getIntExtra(Intent.EXTRA_USER_HANDLE, 0);
            UserHandle currentUserHandle = new UserHandle(userHandleId);
            mIsPrimaryUser = UserManager.get(mContext).getPrimaryUser().getUserHandle()
                    .equals(currentUserHandle);

            // Any time the user changes, re-register the accounts.
            tearDownAccounts();
            setupAccounts();
        }
    };

    /**
     * M: for multi-sub, all subs should be listen, not only the default one.
     * original code:
    private final PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
        @Override
        public void onServiceStateChanged(ServiceState serviceState) {
            int newState = serviceState.getState();
            if (newState == ServiceState.STATE_IN_SERVICE && mServiceState != newState) {
                tearDownAccounts();
                setupAccounts();
            }
            mServiceState = newState;
        }
    };
     */

    private static TelecomAccountRegistry sInstance;
    private final Context mContext;
    private final TelecomManager mTelecomManager;
    private final TelephonyManager mTelephonyManager;
    private final SubscriptionManager mSubscriptionManager;
    private List<AccountEntry> mAccounts = new LinkedList<AccountEntry>();
    private Object mAccountsLock = new Object();
    /// M: CC: Add Unknown Call Notification for no phone account to use @{
    private List<Phone> mNoAccountPhones = new LinkedList<Phone>();
    private List<PstnNoAccountUnknownCallNotifier> mNoAccountUnknownCallNotifiers =
            new LinkedList<PstnNoAccountUnknownCallNotifier>();
    /// @}

    private HandlerThread mHandlerThread = new HandlerThread("phone-account");
    private Handler mHandler;
    private final int MSG_REGISTER_FOR_ALL_ACCOUNT_ENTRIES = 1;
    private final int MSG_REGISTER_ONLY = 2;

    /**
     * M: should keep service state for all subs.
     * original code:
    private int mServiceState = ServiceState.STATE_POWER_OFF;
     */

    private final Map<Integer, Integer> mServiceStates = new ArrayMap<Integer, Integer>();

    private boolean mIsPrimaryUser = true;

    // TODO: Remove back-pointer from app singleton to Service, since this is not a preferred
    // pattern; redesign. This was added to fix a late release bug.
    private TelephonyConnectionService mTelephonyConnectionService;

    TelecomAccountRegistry(Context context) {
        mContext = context;
        mTelecomManager = TelecomManager.from(context);
        mTelephonyManager = TelephonyManager.from(context);
        mSubscriptionManager = SubscriptionManager.from(context);

        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                case MSG_REGISTER_FOR_ALL_ACCOUNT_ENTRIES:
                    List<PhoneAccount> phoneAccountsForAccountEntries = (List<PhoneAccount>)msg.obj;
                    for (PhoneAccount account : phoneAccountsForAccountEntries) {
                        Log.d(TelecomAccountRegistry.this, "Do register: " + account);
                    }
                    registerForAllAccountEntries(phoneAccountsForAccountEntries);
                    break;
                case MSG_REGISTER_ONLY:
                    PhoneAccount account = (PhoneAccount)msg.obj;
                    Log.d(TelecomAccountRegistry.this, "Do register with video: " + account);
                    registerIfChanged(account);
                    break;
                }
            }
        };
    }

    static synchronized final TelecomAccountRegistry getInstance(Context context) {
        if (sInstance == null && context != null) {
            sInstance = new TelecomAccountRegistry(context);
        }
        return sInstance;
    }

    void setTelephonyConnectionService(TelephonyConnectionService telephonyConnectionService) {
        this.mTelephonyConnectionService = telephonyConnectionService;
    }

    TelephonyConnectionService getTelephonyConnectionService() {
        return mTelephonyConnectionService;
    }

    /**
     * Determines if the {@link AccountEntry} associated with a {@link PhoneAccountHandle} supports
     * pausing video calls.
     *
     * @param handle The {@link PhoneAccountHandle}.
     * @return {@code True} if video pausing is supported.
     */
    boolean isVideoPauseSupported(PhoneAccountHandle handle) {
        synchronized (mAccountsLock) {
            for (AccountEntry entry : mAccounts) {
                if (entry.getPhoneAccountHandle().equals(handle)) {
                    return entry.isVideoPauseSupported();
                }
            }
        }
        return false;
    }

    /**
     * Determines if the {@link AccountEntry} associated with a {@link PhoneAccountHandle} supports
     * merging calls.
     *
     * @param handle The {@link PhoneAccountHandle}.
     * @return {@code True} if merging calls is supported.
     */
    boolean isMergeCallSupported(PhoneAccountHandle handle) {
        synchronized (mAccountsLock) {
            for (AccountEntry entry : mAccounts) {
                if (entry.getPhoneAccountHandle().equals(handle)) {
                    return entry.isMergeCallSupported();
                }
            }
        }
        return false;
    }

    /**
     * Determines if the {@link AccountEntry} associated with a {@link PhoneAccountHandle} supports
     * video conferencing.
     *
     * @param handle The {@link PhoneAccountHandle}.
     * @return {@code True} if video conferencing is supported.
     */
    boolean isVideoConferencingSupported(PhoneAccountHandle handle) {
        synchronized (mAccountsLock) {
            for (AccountEntry entry : mAccounts) {
                if (entry.getPhoneAccountHandle().equals(handle)) {
                    return entry.isVideoConferencingSupported();
                }
            }
        }
        return false;
    }

    /**
     * Determines if the {@link AccountEntry} associated with a {@link PhoneAccountHandle} allows
     * merging of wifi calls when VoWIFI is disabled.
     *
     * @param handle The {@link PhoneAccountHandle}.
     * @return {@code True} if merging of wifi calls is allowed when VoWIFI is disabled.
     */
    boolean isMergeOfWifiCallsAllowedWhenVoWifiOff(final PhoneAccountHandle handle) {
        synchronized (mAccountsLock) {
            Optional<AccountEntry> result = mAccounts.stream().filter(
                    entry -> entry.getPhoneAccountHandle().equals(handle)).findFirst();

            if (result.isPresent()) {
                return result.get().isMergeOfWifiCallsAllowedWhenVoWifiOff();
            } else {
                return false;
            }
        }
    }

    /**
     * @return Reference to the {@code TelecomAccountRegistry}'s subscription manager.
     */
    SubscriptionManager getSubscriptionManager() {
        return mSubscriptionManager;
    }

    /**
     * Returns the address (e.g. the phone number) associated with a subscription.
     *
     * @param handle The phone account handle to find the subscription address for.
     * @return The address.
     */
    Uri getAddress(PhoneAccountHandle handle) {
        synchronized (mAccountsLock) {
            for (AccountEntry entry : mAccounts) {
                if (entry.getPhoneAccountHandle().equals(handle)) {
                    return entry.mAccount.getAddress();
                }
            }
        }
        return null;
    }

    /**
     * Sets up all the phone accounts for SIMs on first boot.
     */
    void setupOnBoot() {
        // TODO: When this object "finishes" we should unregister by invoking
        // SubscriptionManager.getInstance(mContext).unregister(mOnSubscriptionsChangedListener);
        // This is not strictly necessary because it will be unregistered if the
        // notification fails but it is good form.

        // Register for SubscriptionInfo list changes which is guaranteed
        // to invoke onSubscriptionsChanged the first time.
        SubscriptionManager.from(mContext).addOnSubscriptionsChangedListener(
                mOnSubscriptionsChangedListener);

        // We also need to listen for changes to the service state (e.g. emergency -> in service)
        // because this could signal a removal or addition of a SIM in a single SIM phone.
        /**
         * M: for multi-sub, all subs has should be listen
         * Original code:
        mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_SERVICE_STATE);
         */
        listenPhoneState();
        registerReceiver();
        /** @} */

        // Listen for user switches.  When the user switches, we need to ensure that if the current
        // use is not the primary user we disable video calling.
        mContext.registerReceiver(mUserSwitchedReceiver,
                new IntentFilter(Intent.ACTION_USER_SWITCHED));
    }

    /**
     * Determines if the list of {@link AccountEntry}(s) contains an {@link AccountEntry} with a
     * specified {@link PhoneAccountHandle}.
     *
     * @param handle The {@link PhoneAccountHandle}.
     * @return {@code True} if an entry exists.
     */
    boolean hasAccountEntryForPhoneAccount(PhoneAccountHandle handle) {
        synchronized (mAccountsLock) {
            for (AccountEntry entry : mAccounts) {
                if (entry.getPhoneAccountHandle().equals(handle)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Un-registers any {@link PhoneAccount}s which are no longer present in the list
     * {@code AccountEntry}(s).
     */
    private void cleanupPhoneAccounts(List<PhoneAccount> phoneAccountsForAccountEntries) {
        ComponentName telephonyComponentName =
                new ComponentName(mContext, TelephonyConnectionService.class);
        // This config indicates whether the emergency account was flagged as emergency calls only
        // in which case we need to consider all phone accounts, not just the call capable ones.
        final boolean emergencyCallsOnlyEmergencyAccount = mContext.getResources().getBoolean(
                R.bool.config_emergency_account_emergency_calls_only);
        List<PhoneAccountHandle> accountHandles = emergencyCallsOnlyEmergencyAccount
                ? mTelecomManager.getAllPhoneAccountHandles()
                : mTelecomManager.getCallCapablePhoneAccounts(true /* includeDisabled */);

        for (PhoneAccountHandle handle : accountHandles) {
            if (telephonyComponentName.equals(handle.getComponentName()) &&
                    !isValidPhoneAccountForAccountEntries(handle,
                            phoneAccountsForAccountEntries)) {
                Log.i(this, "Unregistering phone account %s.", handle);
                mTelecomManager.unregisterPhoneAccount(handle);
            }
        }
    }

    private void setupAccounts() {
        // Go through SIM-based phones and register ourselves -- registering an existing account
        // will cause the existing entry to be replaced.
        Phone[] phones = PhoneFactory.getPhones();

        final boolean phoneAccountsEnabled = mContext.getResources().getBoolean(
                R.bool.config_pstn_phone_accounts_enabled);

        synchronized (mAccountsLock) {
            if (phoneAccountsEnabled) {
                for (Phone phone : phones) {
                    int subscriptionId = phone.getSubId();
                    Log.d(this, "Phone with subscription id %d", subscriptionId);
                    // setupAccounts can be called multiple times during service changes. Don't add
                    // an account if the Icc has not been set yet.
                    /// M: for ALPS03217604, get ICCID by subscriptionInfo to ensure right iccid @{
                    // Original code:
                    // if (subscriptionId >= 0 && phone.getFullIccSerialNumber() != null) {
                    // }
                    /// @}
                    if (subscriptionId >= 0 && PhoneUtils.getIccIdForPhone(phone) != null) {
                        mAccounts.add(new AccountEntry(phone, false /* emergency */,
                                false /* isDummy */));
                    /// M: CC: Add Unknown Call Notification for no phone account to use @{
                    } else {
                        mNoAccountPhones.add(phone);
                    }
                    ///@}
                }
            }

            /// M: FIXME: revert the "E" account design. observe the consequences.
            // If we did not list ANY accounts, we need to provide a "default" SIM account
            // for emergency numbers since no actual SIM is needed for dialing emergency
            // numbers but a phone account is.
            if (mAccounts.isEmpty()) {
                mAccounts.add(new AccountEntry(PhoneFactory.getDefaultPhone(), true /* emergency */,
                        false /* isDummy */));
                /// M: CC: Add Unknown Call Notification for no phone account to use
                mNoAccountPhones.remove(PhoneFactory.getDefaultPhone());
            }

            /// M: CC: Add Unknown Call Notification for no phone account to use @{
            for (Phone phone: mNoAccountPhones) {
                mNoAccountUnknownCallNotifiers.add(new PstnNoAccountUnknownCallNotifier(phone));
            }
            ///@}

            // Add a fake account entry.
            if (DBG && phones.length > 0 && "TRUE".equals(System.getProperty("dummy_sim"))) {
                mAccounts.add(new AccountEntry(phones[0], false /* emergency */,
                        true /* isDummy */));
            }

            List<PhoneAccount> phoneAccountsForAccountEntries = new LinkedList<PhoneAccount>();
            for (AccountEntry account : mAccounts) {
                PhoneAccount phoneAccount = account.mAccount;
                phoneAccountsForAccountEntries.add(phoneAccount);
            }
            mHandler.obtainMessage(MSG_REGISTER_FOR_ALL_ACCOUNT_ENTRIES,
                        phoneAccountsForAccountEntries).sendToTarget();
        }
    }

    private void registerForAllAccountEntries(List<PhoneAccount> phoneAccountsForAccountEntries) {
        for (PhoneAccount account : phoneAccountsForAccountEntries) {
            registerIfChanged(account);
        }
        cleanupPhoneAccounts(phoneAccountsForAccountEntries);

        // At some point, the phone account ID was switched from the subId to the iccId.
        // If there is a default account, check if this is the case, and upgrade the default account
        // from using the subId to iccId if so.
        PhoneAccountHandle defaultPhoneAccount =
                mTelecomManager.getUserSelectedOutgoingPhoneAccount();
        ComponentName telephonyComponentName =
                new ComponentName(mContext, TelephonyConnectionService.class);

        if (defaultPhoneAccount != null &&
                telephonyComponentName.equals(defaultPhoneAccount.getComponentName()) &&
                !isValidPhoneAccountForAccountEntries(defaultPhoneAccount,
                        phoneAccountsForAccountEntries)) {
            String phoneAccountId = defaultPhoneAccount.getId();
            if (!TextUtils.isEmpty(phoneAccountId) && TextUtils.isDigitsOnly(phoneAccountId)) {
                PhoneAccountHandle upgradedPhoneAccount =PhoneUtils
                        .makePstnPhoneAccountHandle(PhoneGlobals
                                .getPhone(Integer.parseInt(phoneAccountId)));

                if (isValidPhoneAccountForAccountEntries(upgradedPhoneAccount,
                        phoneAccountsForAccountEntries)) {
                    mTelecomManager.setUserSelectedOutgoingPhoneAccount(upgradedPhoneAccount);
                }
            }
        }
    }

    private void tearDownAccounts() {
        synchronized (mAccountsLock) {
            for (AccountEntry entry : mAccounts) {
                entry.teardown();
            }
            mAccounts.clear();
            /// M: CC: Add Unknown Call Notification for no phone account to use @{
            mNoAccountPhones.clear();
            for (PstnNoAccountUnknownCallNotifier entry : mNoAccountUnknownCallNotifiers) {
                entry.teardown();
            }
            mNoAccountUnknownCallNotifiers.clear();
            /// @}
        }
    }


    // ---------------------------------------mtk --------------------------------------//

    // For ALPS02077289. Used to save the network type for all subs.
    private final Map<Integer, Integer> mNetworkType = new ArrayMap<Integer, Integer>();
    // For ALPS02077289. For multi-sub, all subs should be listened.
    private Map<Integer, PhoneStateListener> mPhoneStateListeners
                           = new ArrayMap<Integer, PhoneStateListener>();

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean rebuildAccounts;
            String action = intent.getAction();
            // Check for extended broadcast to rebuild accounts
            rebuildAccounts = needRebuildAccounts(intent);
            Log.d(TelecomAccountRegistry.this, "[onReceive] " + action +
                    ", rebuildAccounts: " + rebuildAccounts);
            if (rebuildAccounts) {
                tearDownAccounts();
                setupAccounts();
            }
        }

        /**
         * Check the extended broadcast to determine whether to rebuild
         * the PhoneAccounts.
         * @param intent the intent.
         * @return true if rebuild needed
         */
        private boolean needRebuildAccounts(Intent intent) {
            boolean rebuildAccounts = false;
            String action = intent.getAction();
            if (ImsManager.ACTION_IMS_STATE_CHANGED.equals(action)) {
                rebuildAccounts = true;
            }
            return rebuildAccounts;
        }
    };

    /**
     * Register receiver to more broadcast, like IMS state changed.
     * @param intentFilter the target IntentFilter.
     */
    private void registerReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        // Receive extended broadcasts like IMS state changed
        intentFilter.addAction(ImsManager.ACTION_IMS_STATE_CHANGED);
        mContext.registerReceiver(mReceiver, intentFilter);
    }

    /**
     * For multi-sub, all subs should be listened.
     */
    private void listenPhoneState() {
        Phone[] phones = PhoneFactory.getPhones();
        for (Phone phone : phones) {
            int subscriptionId = phone.getSubId();
            if (subscriptionId >= 0 && !mPhoneStateListeners.containsKey(subscriptionId)) {
                if (!mServiceStates.containsKey(subscriptionId)) {
                    mServiceStates.put(subscriptionId, ServiceState.STATE_POWER_OFF);
                }
                /// For ALPS02077289. @{
                if (!mNetworkType.containsKey(subscriptionId)) {
                    mNetworkType.put(subscriptionId,
                            mTelephonyManager.getNetworkType(subscriptionId));
                }
                /// @}
                PhoneStateListener listener = new PhoneStateListener(subscriptionId) {
                    @Override
                    public void onServiceStateChanged(ServiceState serviceState) {
                        boolean rebuildAccounts = false;
                        int newState = serviceState.getState();
                        if (newState == ServiceState.STATE_IN_SERVICE
                                && mServiceStates.get(mSubId) != newState) {
                            Log.d(this, "[PhoneStateListener]ServiceState of sub %s changed "
                                    + "%s -> IN_SERVICE, reset PhoneAccount", mSubId,
                                    mServiceStates.get(mSubId));
                            rebuildAccounts = true;
                        }
                        mServiceStates.put(mSubId, newState);

                        /// For ALPS02077289. @{
                        // After SRVCC, the network maybe GSM or other, but IMS registered
                        // state won't change before current call disconnected.
                        // But we cannot make call over IMS, so needs to update
                        // related PhoneAccounts.
                        int newNetworkType = mTelephonyManager.getNetworkType(mSubId);
                        int oldNetworkType = mNetworkType.get(mSubId);
                        if (newNetworkType != oldNetworkType && !rebuildAccounts) {
                            rebuildAccounts = true;
                            Log.d(this, "Network type changed and need rebuild PhoneAccounts.");
                        }
                        mNetworkType.put(mSubId, newNetworkType);
                        Log.d(this, "mSubId = "+ mSubId +" service state = " + newState +
                                " new network type = " + newNetworkType +
                                " old network type = " +oldNetworkType);
                        if (rebuildAccounts) {
                            // TODO: each sub-account should be reset alone
                            tearDownAccounts();
                            setupAccounts();
                        }
                        /// @}
                    }
                };
                mTelephonyManager.listen(listener, PhoneStateListener.LISTEN_SERVICE_STATE);
                mPhoneStateListeners.put(subscriptionId, listener);
            }
        }
    }

    private void updatePhoneStateListeners() {
        // Unregister phone listeners for inactive subscriptions.
        Iterator<Integer> itr = mPhoneStateListeners.keySet().iterator();
        while (itr.hasNext()) {
            int subId = itr.next();
            SubscriptionInfo record = mSubscriptionManager.getActiveSubscriptionInfo(subId);
            if (record == null) {
                // Listening to LISTEN_NONE removes the listener.
                mTelephonyManager.listen(mPhoneStateListeners.get(subId),
                        PhoneStateListener.LISTEN_NONE);
                itr.remove();
            }
        }

        listenPhoneState();
    }

    /**
     * M: register PhoneAccount if it is a new or modified.
     * Skip registering if no change.
     * @param newAccount
     */
    private void registerIfChanged(PhoneAccount newAccount) {
        if ((newAccount == null) || (!isValidPhoneAccount(newAccount))) {
            Log.d(this, "[registerIfChanged], account is null or invalid account");
            return;
        }

        // Get old PhoneAccount if there has one.
        PhoneAccount oldAccount = mTelecomManager.getPhoneAccount(newAccount.getAccountHandle());
        // Check whether the account does change.
        /// M: FIXME: equals can't compare Icon, should enhance the equals method.
        if (!newAccount.equals(oldAccount)) {
            /// TODO: add log to see the capability diff.
            Log.d(this, "[registerIfChanged]Account changed.");
            mTelecomManager.registerPhoneAccount(newAccount);
        }
    }

    /**
     * M: FIXME: In seldom case, the Sub id would not be able to retrieve, in such case,
     * the id would be set to String "null" unexpected.
     * see PhoneUtils#makePstnPhoneAccountHandleWithPrefix()
     * We have no safe way to change this fact. We can only protect it here.
     * @param phoneAccount
     * @return
     */
    public boolean isValidPhoneAccount(PhoneAccount phoneAccount) {
        if (phoneAccount == null || phoneAccount.getAccountHandle() == null) {
            return false;
        }
        /// M: FIXME: as the title mentioned.
        if (TextUtils.equals(phoneAccount.getAccountHandle().getId(), "null")) {
            Log.w(this, "[isValidPhoneAccount]Invalid id null");
            return false;
        }
        return true;
    }

    private boolean isValidPhoneAccountForAccountEntries(PhoneAccountHandle handle,
            List<PhoneAccount> phoneAccountsForAccountEntries) {
        for (PhoneAccount account : phoneAccountsForAccountEntries) {
            if (account.getAccountHandle().equals(handle)) {
                return true;
            }
        }
        return false;
    }
}
