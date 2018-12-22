/*
* Copyright (C) 2014 MediaTek Inc.
* Modification based on code covered by the mentioned copyright
* and/or permission notice(s).
*/
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

package com.android.incallui;

import com.google.common.base.Preconditions;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.telecom.Call.Details;
import android.telecom.DisconnectCause;
import android.telecom.PhoneAccount;
import android.telecom.PhoneAccountHandle;
import android.telecom.StatusHints;
import android.telecom.TelecomManager;
import android.telecom.VideoProfile;
import android.telephony.CarrierConfigManager;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import android.widget.ListAdapter;

import com.android.contacts.common.ContactsUtils;
import com.android.contacts.common.compat.telecom.TelecomManagerCompat;
import com.android.contacts.common.preference.ContactsPreferences;
import com.android.contacts.common.testing.NeededForTesting;
import com.android.contacts.common.util.ContactDisplayUtils;
import com.android.dialer.R;
import com.android.incallui.Call.State;
import com.android.incallui.ContactInfoCache.ContactCacheEntry;
import com.android.incallui.ContactInfoCache.ContactInfoCacheCallback;
import com.android.incallui.InCallPresenter.InCallDetailsListener;
import com.android.incallui.InCallPresenter.InCallEventListener;
import com.android.incallui.InCallPresenter.InCallState;
import com.android.incallui.InCallPresenter.InCallStateListener;
import com.android.incallui.InCallPresenter.IncomingCallListener;
import com.android.incalluibind.ObjectFactory;
/// M: For volte @{
import com.android.incallui.ContactInfoCache.ContactInfoUpdatedListener;
/// @}
/// M: Add for recording. @{
import com.android.incallui.InCallPresenter.PhoneRecorderListener;
/// @}
import java.lang.ref.WeakReference;

import static com.android.contacts.common.compat.CallSdkCompat.Details.PROPERTY_ENTERPRISE_CALL;

/// M: add for performance trace, plug in, volte and feature options. @{
import com.mediatek.incallui.InCallTrace;
import com.mediatek.incallui.InCallUtils;
import com.mediatek.incallui.ext.ExtensionManager;
import com.mediatek.incallui.volte.InCallUIVolteUtils;
import com.mediatek.incallui.wrapper.FeatureOptionWrapper;
/// @}

/**
 * Presenter for the Call Card Fragment.
 * <p>
 * This class listens for changes to InCallState and passes it along to the fragment.
 */
public class CallCardPresenter extends Presenter<CallCardPresenter.CallCardUi>
        implements InCallStateListener, IncomingCallListener, InCallDetailsListener,
        InCallEventListener, CallList.CallUpdateListener, DistanceHelper.Listener
        , PhoneRecorderListener {

    public interface EmergencyCallListener {
        public void onCallUpdated(BaseFragment fragment, boolean isEmergency);
    }

    private static final String TAG = CallCardPresenter.class.getSimpleName();
    private static final long CALL_TIME_UPDATE_INTERVAL_MS = 1000;

    private final EmergencyCallListener mEmergencyCallListener =
            ObjectFactory.newEmergencyCallListener();
    private DistanceHelper mDistanceHelper;
    /*Prize-add huangliemin-2016-5-25*/
    // Use to mark which contact info and view needs to be updated.
    private enum CallEnum {
        PRIMARY,
        SECONDARY,
        NULL
    }
    /*Prize-add huangliemin-2016-5-25*/

    private Call mPrimary;
    private Call mSecondary;
    private ContactCacheEntry mPrimaryContactInfo;
    private ContactCacheEntry mSecondaryContactInfo;
    private CallTimer mCallTimer;
    private Context mContext;
    @Nullable private ContactsPreferences mContactsPreferences;
    private boolean mSpinnerShowing = false;
    private boolean mHasShownToast = false;
    private InCallContactInteractions mInCallContactInteractions;
    private boolean mIsFullscreen = false;
    private boolean mHasSecondCall;//PRIZE-add-yuandailin-2016-3-14
	
	/*PRIZE-feture add hotline--ganxiayong-2018-05-15-start*/
	public static Context myContext;
	public static String[] visitHotline = new String[] { "0769 8860 8291",
			"076988608291", "0769 8860 8198", "076988608198", "0769 8860 8292",
			"076988608292", "0769 8860 8696", "076988608696", "0769 8860 8611",
			"076988608611"};
	public static final String HOTLINE_PREFIX = "0769";
	/*PRIZE-feture add hotline--ganxiayong-2018-05-15-end*/

    public static class ContactLookupCallback implements ContactInfoCacheCallback {
        private final WeakReference<CallCardPresenter> mCallCardPresenter;
        private final boolean mIsPrimary;

        public ContactLookupCallback(CallCardPresenter callCardPresenter, boolean isPrimary) {
            mCallCardPresenter = new WeakReference<CallCardPresenter>(callCardPresenter);
            mIsPrimary = isPrimary;
        }

        @Override
        public void onContactInfoComplete(String callId, ContactCacheEntry entry) {
            CallCardPresenter presenter = mCallCardPresenter.get();
            if (presenter != null) {
            	Log.d(TAG,"[ContactLookupCallback   onContactInfoComplete]   mIsPrimary = "+mIsPrimary );
                presenter.onContactInfoComplete(callId, entry, mIsPrimary);
                /// M: Add for RCS plugin. @{
                ExtensionManager.getRCSeCallCardExt().onImageLoaded(callId, entry);
                /// @}
            }
        }

        @Override
        public void onImageLoadComplete(String callId, ContactCacheEntry entry) {
            CallCardPresenter presenter = mCallCardPresenter.get();
            if (presenter != null) {
                presenter.onImageLoadComplete(callId, entry);
                /// M: Add for RCS plugin. @{
                ExtensionManager.getRCSeCallCardExt().onImageLoaded(callId, entry);
                /// @}
           }
        }

        @Override
        public void onContactInteractionsInfoComplete(String callId, ContactCacheEntry entry) {
            CallCardPresenter presenter = mCallCardPresenter.get();
            if (presenter != null) {
                presenter.onContactInteractionsInfoComplete(callId, entry);
            }
        }
    }

    public CallCardPresenter() {
        // create the call timer
        mCallTimer = new CallTimer(new Runnable() {
            @Override
            public void run() {
                updateCallTime();
            }
        });
    }

    public void init(Context context, Call call) {
    	Log.d(TAG, "[init]");
        mContext = Preconditions.checkNotNull(context);
		myContext = mContext;	//PRIZE-feture add hotline--ganxiayong-2018-05-15

        /// M: For volte @{
        // Here we will use "mContext", so need add here, instead of "onUiReady()"
        ContactInfoCache.getInstance(mContext)
                .addContactInfoUpdatedListener(mContactInfoUpdatedListener);
        /// @}
        mDistanceHelper = ObjectFactory.newDistanceHelper(mContext, this);
        mContactsPreferences = ContactsPreferencesFactory.newContactsPreferences(mContext);

        // Call may be null if disconnect happened already.
        if (call != null) {
            mPrimary = call;
            if (shouldShowNoteSentToast(mPrimary)) {
                final CallCardUi ui = getUi();
                if (ui != null) {
                    ui.showNoteSentToast();
                }
            }
            CallList.getInstance().addCallUpdateListener(call.getId(), this);

            // start processing lookups right away.
            if (!call.isConferenceCall()) {
                startContactInfoSearch(call, true, call.getState() == Call.State.INCOMING);
            } else {
                updateContactEntry(null, true);
            }
        }

        onStateChange(null, InCallPresenter.getInstance().getInCallState(), CallList.getInstance());
    }

    @Override
    public void onUiReady(CallCardUi ui) {
        super.onUiReady(ui);
        Log.d(TAG,"[onUiReady]   mPrimaryContactInfo = "+mPrimaryContactInfo);
        if (mContactsPreferences != null) {
            mContactsPreferences.refreshValue(ContactsPreferences.DISPLAY_ORDER_KEY);
        }

        // Contact search may have completed before ui is ready.
        if (mPrimaryContactInfo != null) {
            /*PRIZE-change-yuandailin-2016-3-14-start*/
            //updatePrimaryDisplayInfo();
            updatePrimaryDisplayInfo(false);
            /*PRIZE-change-yuandailin-2016-3-14-end*/
        }

        // Register for call state changes last
        InCallPresenter.getInstance().addListener(this);
        InCallPresenter.getInstance().addIncomingCallListener(this);
        InCallPresenter.getInstance().addDetailsListener(this);
        InCallPresenter.getInstance().addInCallEventListener(this);
        /// M: [Phone Recording] Add for recording. @{
        InCallPresenter.getInstance().addPhoneRecorderListener(this);
        updateVoiceCallRecordState();
        /// @}
    }

    @Override
    public void onUiUnready(CallCardUi ui) {
        super.onUiUnready(ui);

        // stop getting call state changes
        InCallPresenter.getInstance().removeListener(this);
        InCallPresenter.getInstance().removeIncomingCallListener(this);
        InCallPresenter.getInstance().removeDetailsListener(this);
        InCallPresenter.getInstance().removeInCallEventListener(this);
        if (mPrimary != null) {
            CallList.getInstance().removeCallUpdateListener(mPrimary.getId(), this);
        }

        if (mDistanceHelper != null) {
            mDistanceHelper.cleanUp();
        }

        /// M: ALPS01828853. @{
        // should remove listener when ui unready.
        InCallPresenter.getInstance().removePhoneRecorderListener(this);
        /// @}

        /// M: For volte @{
        ContactInfoCache.getInstance(mContext)
                .removeContactInfoUpdatedListener(mContactInfoUpdatedListener);
        /// @}

        mPrimary = null;
        mPrimaryContactInfo = null;
        mSecondaryContactInfo = null;
    }

    @Override
    public void onIncomingCall(InCallState oldState, InCallState newState, Call call) {
        // same logic should happen as with onStateChange()
        onStateChange(oldState, newState, CallList.getInstance());
    }

    @Override
    public void onStateChange(InCallState oldState, InCallState newState, CallList callList) {
        Log.d(this, "onStateChange()   newState = " + newState+"   oldState = " + oldState);
        final CallCardUi ui = getUi();
        if (ui == null) {
            return;
        }
        ui.updateCallStatus(newState);//PRIZE-add for video call-haungpengfei-2017-3-6
        
        Call primary = null;
        Call secondary = null;

        if (newState == InCallState.INCOMING) {
            primary = callList.getIncomingCall();
            /// M: [1A1H2W] get the second incoming call
            secondary = callList.getSecondaryIncomingCall();
        } else if (newState == InCallState.PENDING_OUTGOING || newState == InCallState.OUTGOING) {
            primary = callList.getOutgoingCall();
            if (primary == null) {
                primary = callList.getPendingOutgoingCall();
            }

            // getCallToDisplay doesn't go through outgoing or incoming calls. It will return the
            // highest priority call to display as the secondary call.
            secondary = getCallToDisplay(callList, null, true);
        } else if (newState == InCallState.INCALL) {
            primary = getCallToDisplay(callList, null, false);
            secondary = getCallToDisplay(callList, primary, true);
        }

        /*PRIZE-Delete-Optimize_Dialer-wangzhong-2018_3_5-start*/
        /*if (mInCallContactInteractions != null &&
                (oldState == InCallState.INCOMING || newState == InCallState.INCOMING)) {
            ui.showContactContext(newState != InCallState.INCOMING);
        }*/
        /*PRIZE-Delete-Optimize_Dialer-wangzhong-2018_3_5-end*/

        Log.d(this, "Primary call: " + primary);
        Log.d(this, "Secondary call: " + secondary);

        final boolean primaryChanged = !(Call.areSame(mPrimary, primary) &&
                Call.areSameNumber(mPrimary, primary));
        final boolean secondaryChanged = !(Call.areSame(mSecondary, secondary) &&
                Call.areSameNumber(mSecondary, secondary));

        mSecondary = secondary;
        Call previousPrimary = mPrimary;
        mPrimary = primary;

        if (primaryChanged && shouldShowNoteSentToast(primary)) {
            ui.showNoteSentToast();
        }

        // Refresh primary call information if either:
        // 1. Primary call changed.
        // 2. The call's ability to manage conference has changed.
        // 3. The call subject should be shown or hidden.
        boolean b1 = shouldRefreshPrimaryInfo(primaryChanged, ui, shouldShowCallSubject(mPrimary));
        boolean b2 = needUpdatePrimaryForVolte(oldState, newState, mPrimary);
        Log.d(TAG, "[onStateChange----]   b1 = "+b1+"   b2 = "+b2);
        if (shouldRefreshPrimaryInfo(primaryChanged, ui, shouldShowCallSubject(mPrimary))
                /// M: [VoLTE Conference] volte conference incoming call @{
                || needUpdatePrimaryForVolte(oldState, newState, mPrimary)
                /// @}
                ) {
            // primary call has changed
            if (previousPrimary != null) {
                //clear progess spinner (if any) related to previous primary call
                maybeShowProgressSpinner(previousPrimary.getState(),
                        Call.SessionModificationState.NO_REQUEST);
                CallList.getInstance().removeCallUpdateListener(previousPrimary.getId(), this);
            }
            CallList.getInstance().addCallUpdateListener(mPrimary.getId(), this);

            mPrimaryContactInfo = ContactInfoCache.buildCacheEntryFromCall(mContext, mPrimary,
                    mPrimary.getState() == Call.State.INCOMING);
            /*PRIZE-remove-yuandailin-2016-6-3-start*/
            Log.d(TAG, "[onStateChange----]   mSecondary = "+mSecondary+"   secondaryChanged = "+secondaryChanged);
            if (mSecondary == null && !secondaryChanged) {
                mHasSecondCall = false;
                updatePrimaryDisplayInfo(false);
            } else {
                mHasSecondCall = true;
                updatePrimaryDisplayInfo(true);
            }
            updatePrimaryDisplayInfo();
            /*PRIZE-remove-yuandailin-2016-6-3-end*/
            maybeStartSearch(mPrimary, true);
            maybeClearSessionModificationState(mPrimary);
        }

        if (previousPrimary != null && mPrimary == null) {
            //clear progess spinner (if any) related to previous primary call
            maybeShowProgressSpinner(previousPrimary.getState(),
                    Call.SessionModificationState.NO_REQUEST);
            CallList.getInstance().removeCallUpdateListener(previousPrimary.getId(), this);
        }
        Log.d(TAG, "[onStateChange]   mSecondary = "+mSecondary+"   secondaryChanged = "+secondaryChanged);
        if (mSecondary == null) {
            // Secondary call may have ended.  Update the ui.
            mSecondaryContactInfo = null;
            updateSecondaryDisplayInfo();
            /*Prize add-huangliemin-2016-5-23*/
            mHasSecondCall = false;
            updatePrimaryDisplayInfo(mHasSecondCall);//PRIZE-add-yuandailin-2016-6-3	
            /*Prize add-huangliemin-2016-5-23*/
        } else if (secondaryChanged) {
            /*Prize add-huangliemin-2016-5-23*/
            mHasSecondCall = true;
            /*Prize add-huangliemin-2016-5-23*/
            // secondary call has changed
            mSecondaryContactInfo = ContactInfoCache.buildCacheEntryFromCall(mContext, mSecondary,
                    /// M: add for judge incoming call sate. @{
                    /*
                     * Google code:
                    mSecondary.getState() == Call.State.INCOMING);
                     */
                    Call.State.isIncoming(mSecondary.getState()));
                    /// @}
            updateSecondaryDisplayInfo();
            maybeStartSearch(mSecondary, false);
            maybeClearSessionModificationState(mSecondary);
        }

        // Start/stop timers.
        if (isPrimaryCallActive()) {
            Log.d(this, "Starting the calltime timer");
            mCallTimer.start(CALL_TIME_UPDATE_INTERVAL_MS);
        } else {
            Log.d(this, "Canceling the calltime timer");
            mCallTimer.cancel();
            /*PRIZE-change-yuandailin-2016-3-14-start*/
            //ui.setPrimaryCallElapsedTime(false, 0);
            ui.setPrimaryCallElapsedTime(false, 0, mHasSecondCall);
            /*PRIZE-change-yuandailin-2016-3-14-end*/
        }

        // Set the call state
        int callState = Call.State.IDLE;
        if (mPrimary != null) {
            callState = mPrimary.getState();
            updatePrimaryCallState();
        } else {
            getUi().setCallState(
                    callState,
                    VideoProfile.STATE_AUDIO_ONLY,
                    Call.SessionModificationState.NO_REQUEST,
                    new DisconnectCause(DisconnectCause.UNKNOWN),
                    null,
                    null,
                    null,
                    false /* isWifi */,
                    false /* isConference */,
                    false /* isWorkCall */);
            getUi().showHdAudioIndicator(false);
        }

        maybeShowManageConferenceCallButton();

        // Hide the end call button instantly if we're receiving an incoming call.
        getUi().setEndCallButtonEnabled(shouldShowEndCallButton(mPrimary, callState),
                callState != Call.State.INCOMING /* animate */);
        maybeSendAccessibilityEvent(oldState, newState, primaryChanged);

        /// M: for ALPS01774241
        // update record icon when state change
        updateVoiceCallRecordState();

        /// M: for ALPS01945830, update primarycall and callbutton background color. @{
        //ui.updateColors();//PRIZE-remove-yuandailin-2016-3-9
        /// @}
        /*PRIZE-add for video call-yuandailin-2016-7-6-start*/
        if (mPrimary != null && CallUtils.isVideoCall(mPrimary) && newState == InCallState.INCALL) {
            ui.updatePrimaryCallInfo(true);
        }
        /*PRIZE-add for video call-yuandailin-2016-7-6-end*/

    }

    @Override
    public void onDetailsChanged(Call call, Details details) {
        updatePrimaryCallState();

        /*PRIZE-Delete-Optimize_Dialer-wangzhong-2018_3_5-start*/
        /// M: FIXME:  refresh in this situation is correct?
        // [Video call]should hide or display call card photo with video state@{
        /*if (call != null && mPrimary != null && Call.areSame(call,mPrimary)) {
            displayPhotoWithVideoUi(mPrimary);
        }*/
        /// @}
        /*PRIZE-Delete-Optimize_Dialer-wangzhong-2018_3_5-end*/

        if (call.can(Details.CAPABILITY_MANAGE_CONFERENCE) !=
                details.can(Details.CAPABILITY_MANAGE_CONFERENCE)) {
            maybeShowManageConferenceCallButton();
        }
    }

    @Override
    public void onCallChanged(Call call) {
        // No-op; specific call updates handled elsewhere.
    }

    /**
     * Handles a change to the session modification state for a call.  Triggers showing the progress
     * spinner, as well as updating the call state label.
     *
     * @param sessionModificationState The new session modification state.
     */
    @Override
    public void onSessionModificationStateChange(int sessionModificationState) {
        Log.d(this, "onSessionModificationStateChange : sessionModificationState = " +
                sessionModificationState);

        if (mPrimary == null) {
            return;
        }
        maybeShowProgressSpinner(mPrimary.getState(), sessionModificationState);
        getUi().setEndCallButtonEnabled(sessionModificationState !=
                        Call.SessionModificationState.RECEIVED_UPGRADE_TO_VIDEO_REQUEST,
                true /* shouldAnimate */);
        updatePrimaryCallState();
        /// M: Update secondary call display if necessary @{
        if (mSecondary != null) {
            updateSecondaryDisplayInfo();
        }
        /// @}

        /*PRIZE-Add-Optimize_Dialer-wangzhong-2018_3_5-start*/
        updateVoiceCallRecordState(true);
        /*PRIZE-Add-Optimize_Dialer-wangzhong-2018_3_5-end*/
    }

    /**
     * Handles a change to the last forwarding number by refreshing the primary call info.
     */
    @Override
    public void onLastForwardedNumberChange() {
        Log.v(this, "onLastForwardedNumberChange");

        if (mPrimary == null) {
            return;
        }
        updatePrimaryDisplayInfo();
    }

    /**
     * Handles a change to the child number by refreshing the primary call info.
     */
    @Override
    public void onChildNumberChange() {
        Log.v(this, "onChildNumberChange");

        if (mPrimary == null) {
            return;
        }
        updatePrimaryDisplayInfo();
    }

    private boolean shouldRefreshPrimaryInfo(boolean primaryChanged, CallCardUi ui,
            boolean shouldShowCallSubject) {
        if (mPrimary == null) {
            return false;
        }
        /*PRIZE-Change-Optimize_Dialer-wangzhong-2018_3_5-start*/
        /*return primaryChanged ||
                ui.isManageConferenceVisible() != shouldShowManageConference() ||
                ui.isCallSubjectVisible() != shouldShowCallSubject;*/
        return primaryChanged ||
                ui.isManageConferenceVisible() != shouldShowManageConference() ||
                false != shouldShowCallSubject;
        /*PRIZE-Change-Optimize_Dialer-wangzhong-2018_3_5-end*/
    }

    private String getSubscriptionNumber() {
        // If it's an emergency call, and they're not populating the callback number,
        // then try to fall back to the phone sub info (to hopefully get the SIM's
        // number directly from the telephony layer).
        PhoneAccountHandle accountHandle = mPrimary.getAccountHandle();
        if (accountHandle != null) {
            TelecomManager mgr = InCallPresenter.getInstance().getTelecomManager();
            PhoneAccount account = TelecomManagerCompat.getPhoneAccount(mgr, accountHandle);
            if (account != null) {
                return getNumberFromHandle(account.getSubscriptionAddress());
            }
        }
        return null;
    }

    private void updatePrimaryCallState() {
        /// M: add for performance trace.
        InCallTrace.begin("callcard_updateprimarystate");
        if (getUi() != null && mPrimary != null) {
            boolean isWorkCall = mPrimary.hasProperty(PROPERTY_ENTERPRISE_CALL)
                    || (mPrimaryContactInfo == null ? false
                            : mPrimaryContactInfo.userType == ContactsUtils.USER_TYPE_WORK);
            getUi().setCallState(
                    mPrimary.getState(),
                    mPrimary.getVideoState(),
                    mPrimary.getSessionModificationState(),
                    mPrimary.getDisconnectCause(),
                    getConnectionLabel(),
                    getCallStateIcon(),
                    getGatewayNumber(),
                    mPrimary.hasProperty(Details.PROPERTY_WIFI),
                    mPrimary.isConferenceCall(),
                    isWorkCall);

            /*PRIZE-Delete-Optimize_Dialer-wangzhong-2018_3_5-start*/
            /// M: update primary call's photo display.
            /*displayPhotoWithVideoUi(mPrimary);*/
            /*PRIZE-Delete-Optimize_Dialer-wangzhong-2018_3_5-end*/
            maybeShowHdAudioIcon();
            /**
             * M: [ALPS02292879] Don't show the ECC call back number in order to unionize
             * behaviors @{
             */
            if (FeatureOptionWrapper.supportsEccCallbackNumber()) {
                setCallbackNumber();
            }
            /** @} */

            //add for Plug-in. @{
            ExtensionManager.getCallCardExt().onStateChange(mPrimary.getTelecomCall());
            ExtensionManager.getRCSeCallCardExt().onStateChange(mPrimary.getTelecomCall());
            //add for Plug-in. @}
        }
        /// M: add for performance trace.
        InCallTrace.end("callcard_updateprimarystate");
    }

    /**
     * Show the HD icon if the call is active and has {@link Details#PROPERTY_HIGH_DEF_AUDIO},
     * except if the call has a last forwarded number (we will show that icon instead).
     */
    private void maybeShowHdAudioIcon() {
        boolean showHdAudioIndicator =
                isPrimaryCallActive() && mPrimary.hasProperty(Details.PROPERTY_HIGH_DEF_AUDIO) &&
                TextUtils.isEmpty(mPrimary.getLastForwardedNumber());
        getUi().showHdAudioIndicator(showHdAudioIndicator);
    }

    /**
     * Only show the conference call button if we can manage the conference.
     */
    private void maybeShowManageConferenceCallButton() {
        getUi().showManageConferenceCallButton(shouldShowManageConference());
    }

    /**
     * Determines if a pending session modification exists for the current call.  If so, the
     * progress spinner is shown, and the call state is updated.
     *
     * @param callState The call state.
     * @param sessionModificationState The session modification state.
     */
    private void maybeShowProgressSpinner(int callState, int sessionModificationState) {
        final boolean show = sessionModificationState ==
                Call.SessionModificationState.WAITING_FOR_UPGRADE_RESPONSE
                && callState == Call.State.ACTIVE;
        if (show != mSpinnerShowing) {
            getUi().setProgressSpinnerVisible(show);
            mSpinnerShowing = show;
        }
    }

    /**
     * Determines if the manage conference button should be visible, based on the current primary
     * call.
     *
     * @return {@code True} if the manage conference button should be visible.
     */
    private boolean shouldShowManageConference() {
        if (mPrimary == null) {
            return false;
        }

        return mPrimary.can(android.telecom.Call.Details.CAPABILITY_MANAGE_CONFERENCE)
                && !mIsFullscreen;
    }

    private void setCallbackNumber() {
        String callbackNumber = null;

        // Show the emergency callback number if either:
        // 1. This is an emergency call.
        // 2. The phone is in Emergency Callback Mode, which means we should show the callback
        //    number.
        boolean showCallbackNumber = mPrimary.hasProperty(Details.PROPERTY_EMERGENCY_CALLBACK_MODE);

        if (mPrimary.isEmergencyCall() || showCallbackNumber) {
            callbackNumber = getSubscriptionNumber();
        } else {
            StatusHints statusHints = mPrimary.getTelecomCall().getDetails().getStatusHints();
            if (statusHints != null) {
                Bundle extras = statusHints.getExtras();
                if (extras != null) {
                    callbackNumber = extras.getString(TelecomManager.EXTRA_CALL_BACK_NUMBER);
                }
            }
        }

        final String simNumber = TelecomManagerCompat.getLine1Number(
                InCallPresenter.getInstance().getTelecomManager(),
                InCallPresenter.getInstance().getTelephonyManager(),
                mPrimary.getAccountHandle());
        if (!showCallbackNumber && PhoneNumberUtils.compare(callbackNumber, simNumber)) {
            Log.d(this, "Numbers are the same (and callback number is not being forced to show);" +
                    " not showing the callback number");
            callbackNumber = null;
        }

        getUi().setCallbackNumber(callbackNumber, mPrimary.isEmergencyCall() || showCallbackNumber);
    }

    public void updateCallTime() {
        final CallCardUi ui = getUi();

        if (ui == null) {
            mCallTimer.cancel();
        } else if (!isPrimaryCallActive()) {
            /*PRIZE-change-yuandailin-2016-3-14-start*/
            //ui.setPrimaryCallElapsedTime(false, 0);
            ui.setPrimaryCallElapsedTime(false, 0, mHasSecondCall);
            mCallTimer.cancel();
        } else {
            final long callStart = mPrimary.getConnectTimeMillis();
            final long duration = System.currentTimeMillis() - callStart;
            //ui.setPrimaryCallElapsedTime(true, duration);
            ui.setPrimaryCallElapsedTime(true, duration, mHasSecondCall);
            /*PRIZE-change-yuandailin-2016-3-14-end*/
        }
    }

    public void onCallStateButtonTouched() {
        Intent broadcastIntent = ObjectFactory.getCallStateButtonBroadcastIntent(mContext);
        if (broadcastIntent != null) {
            Log.d(this, "Sending call state button broadcast: ", broadcastIntent);
            mContext.sendBroadcast(broadcastIntent, Manifest.permission.READ_PHONE_STATE);
        }
    }

    //M: [VideoCall] in MTK solution, when click photo, do nothing @{
    /**
     * Handles click on the contact photo by toggling fullscreen mode if the current call is a video
     * call.
     */
    /* public void onContactPhotoClick() {
        // M: [VideoCall]fixed for ALPS02304060,when only active state
        // can change fullscreen mode @{
        if (getUi() == null) {
            return;
        }
        if (mPrimary != null && mPrimary.isVideoCall(mContext) &&
                mPrimary.getState() == Call.State.ACTIVE &&
                !getUi().isVideoDisplayViewVisible() &&
                !mPrimary.isHeld()) {
            InCallPresenter.getInstance().toggleFullscreenMode();
        }
        /// @}
    } */
    /// @}

    private void maybeStartSearch(Call call, boolean isPrimary) {
    	Log.d(TAG,"[maybeStartSearch]    isPrimary = "+isPrimary);
        // no need to start search for conference calls which show generic info.
        /**
         * M: [VoLTE conference] incoming call still need to search.
         * google original code:
         if (call != null && !call.isConferenceCall()) {
         * @{
         */
        if ((call != null && !call.isConferenceCall()) || isIncomingVolteConference(call)) {
        /** @} */
            startContactInfoSearch(call, isPrimary, call.getState() == Call.State.INCOMING);
        }
    }

    private void maybeClearSessionModificationState(Call call) {
        if (call.getSessionModificationState() !=
                Call.SessionModificationState.RECEIVED_UPGRADE_TO_VIDEO_REQUEST) {
            call.setSessionModificationState(Call.SessionModificationState.NO_REQUEST);
        }
    }

    /**
     * Starts a query for more contact data for the save primary and secondary calls.
     */
    private void startContactInfoSearch(final Call call, final boolean isPrimary,
            boolean isIncoming) {
    	Log.d(TAG,"[startContactInfoSearch]    isPrimary = "+isPrimary);
        final ContactInfoCache cache = ContactInfoCache.getInstance(mContext);

        cache.findInfo(call, isIncoming, new ContactLookupCallback(this, isPrimary));
    }

    private void onContactInfoComplete(String callId, ContactCacheEntry entry, boolean isPrimary) {
    	Log.d(TAG,"[onContactInfoComplete]   isPrimary = "+isPrimary);
    	/*prize-remove-haungpengfei-2016-12-27-start*/
//        final boolean entryMatchesExistingCall =
//                (isPrimary && mPrimary != null && TextUtils.equals(callId,  mPrimary.getId())) ||
//                (!isPrimary && mSecondary != null && TextUtils.equals(callId, mSecondary.getId()));
//        if (entryMatchesExistingCall) {
//            updateContactEntry(entry, isPrimary);
//        } else {
//            Log.w(this, "Dropping stale contact lookup info for " + callId);
//        }
        /*prize-remove-haungpengfei-2016-12-27-end*/
    	/*prize-add-haungpengfei-2016-12-27-start*/
		CallEnum newType = reCalculateContactInfoType(callId);
		if (newType != CallEnum.NULL) {
			updateContactEntry(entry, newType, false);
			
			/* prize-add-haungpengfei-2016-12-27-end */
			final Call call = CallList.getInstance().getCallById(callId);
			if (call != null) {
				call.getLogState().contactLookupResult = entry.contactLookupResult;
			}
			if (entry.contactUri != null) {
				CallerInfoUtils.sendViewNotification(mContext, entry.contactUri);
			}
		}
    }

    private void onImageLoadComplete(String callId, ContactCacheEntry entry) {
        if (getUi() == null) {
            return;
        }

        if (entry.photo != null) {
            if (mPrimary != null && callId.equals(mPrimary.getId())) {
                boolean showContactPhoto = !VideoCallPresenter.showIncomingVideo(
                        mPrimary.getVideoState(), mPrimary.getState());
                getUi().setPrimaryImage(entry.photo, showContactPhoto);
            }
        }
    }

    private void onContactInteractionsInfoComplete(String callId, ContactCacheEntry entry) {
        if (getUi() == null) {
            return;
        }

        if (mPrimary != null && callId.equals(mPrimary.getId())) {
            mPrimaryContactInfo.locationAddress = entry.locationAddress;
            updateContactInteractions();
        }
    }

    @Override
    public void onLocationReady() {
        // This will only update the contacts interactions data if the location returns after
        // the contact information is found.
        updateContactInteractions();
    }

    private void updateContactInteractions() {
        if (mPrimary != null && mPrimaryContactInfo != null
                && (mPrimaryContactInfo.locationAddress != null
                        || mPrimaryContactInfo.openingHours != null)) {
            // TODO: This is hardcoded to "isBusiness" because functionality to differentiate
            // between business and personal has not yet been added.

            if (setInCallContactInteractionsType(true /* isBusiness */)) {
                /*PRIZE-Delete-Optimize_Dialer-wangzhong-2018_3_5-start*/
                /*getUi().setContactContextTitle(
                        mInCallContactInteractions.getBusinessListHeaderView());*/
                /*PRIZE-Delete-Optimize_Dialer-wangzhong-2018_3_5-end*/
            }

            mInCallContactInteractions.setBusinessInfo(
                    mPrimaryContactInfo.locationAddress,
                    mDistanceHelper.calculateDistance(mPrimaryContactInfo.locationAddress),
                    mPrimaryContactInfo.openingHours);
        /*PRIZE-Delete-Optimize_Dialer-wangzhong-2018_3_5-start*/
            /*getUi().setContactContextContent(mInCallContactInteractions.getListAdapter());
            getUi().showContactContext(mPrimary.getState() != State.INCOMING);
        } else {
            getUi().showContactContext(false);*/
        /*PRIZE-Delete-Optimize_Dialer-wangzhong-2018_3_5-end*/
        }
    }

    /**
     * Update the contact interactions type so that the correct UI is shown.
     *
     * @param isBusiness {@code true} if the interaction is a business interaction, {@code false} if
     * it is a personal contact.
     *
     * @return {@code true} if this is a new type of contact interaction (business or personal).
     * {@code false} if it hasn't changed.
     */
    private boolean setInCallContactInteractionsType(boolean isBusiness) {
        if (mInCallContactInteractions == null) {
            mInCallContactInteractions =
                    new InCallContactInteractions(mContext, isBusiness);
            return true;
        }

        return mInCallContactInteractions.switchContactType(isBusiness);
    }

    private void updateContactEntry(ContactCacheEntry entry, boolean isPrimary) {
    	Log.d(TAG, "[updateContactEntry]   isPrimary = "+isPrimary);
        if (isPrimary) {
            mPrimaryContactInfo = entry;
            /*PRIZE-change-yuandailin-2016-4-14-start*/
            //updatePrimaryDisplayInfo();
            updatePrimaryDisplayInfo(false);
            /*PRIZE-change-yuandailin-2016-4-14-end*/
        } else {
            mSecondaryContactInfo = entry;
            updateSecondaryDisplayInfo();
        }
    }

    /*Prize-add huangliemin-2016-5-25*/
    /**
     * Update the contact entry and view with specified view type.
     *
     * @param entry
     * @param type         Includes the following three types: PRIMARY/SECONDARY/THIRD.
     * @param isConference
     */
    private void updateContactEntry(ContactCacheEntry entry, CallEnum type, boolean isConference) {
        switch (type) {
            case PRIMARY:
            	Log.d(TAG,"[updateContactEntry]   PRIMARY");
                mPrimaryContactInfo = entry;
                updatePrimaryDisplayInfo(mHasSecondCall);
                break;
            case SECONDARY:
            	Log.d(TAG,"[updateContactEntry]   SECONDARY");
                mSecondaryContactInfo = entry;
                mHasSecondCall = true;
                updatePrimaryDisplayInfo(true);
                updateSecondaryDisplayInfo();
                break;
            default:
                break;
        }
    }
    /*Prize-add huangliemin-2016-5-25*/

    /**
     * Get the highest priority call to display.
     * Goes through the calls and chooses which to return based on priority of which type of call
     * to display to the user. Callers can use the "ignore" feature to get the second best call
     * by passing a previously found primary call as ignore.
     *
     * @param ignore A call to ignore if found.
     */
    private Call getCallToDisplay(CallList callList, Call ignore, boolean skipDisconnected) {
        // Active calls come second.  An active call always gets precedent.
        Call retval = callList.getActiveCall();
        if (retval != null && retval != ignore) {
            return retval;
        }

        // Sometimes there is intemediate state that two calls are in active even one is about
        // to be on hold.
        retval = callList.getSecondActiveCall();
        if (retval != null && retval != ignore) {
            return retval;
        }

        // Disconnected calls get primary position if there are no active calls
        // to let user know quickly what call has disconnected. Disconnected
        // calls are very short lived.
        if (!skipDisconnected) {
            retval = callList.getDisconnectingCall();
            if (retval != null && retval != ignore) {
                return retval;
            }

            /// M: ALPS02217975 previously disconnected call screen for cdma is shown again@{
            retval = getDisconnectedCdmaConfCall(callList);
            if (retval != null && retval != ignore) {
                return retval;
            }
            /// @}

            retval = callList.getDisconnectedCall();
            if (retval != null && retval != ignore) {
                return retval;
            }
        }

        // Then we go to background call (calls on hold)
        retval = callList.getBackgroundCall();
        if (retval != null && retval != ignore) {
            return retval;
        }

        // Lastly, we go to a second background call.
        retval = callList.getSecondBackgroundCall();

        return retval;
    }

    private void updatePrimaryDisplayInfo() {
        final CallCardUi ui = getUi();
        if (ui == null) {
            // TODO: May also occur if search result comes back after ui is destroyed. Look into
            // removing that case completely.
            Log.d(TAG, "updatePrimaryDisplayInfo called but ui is null!");
            return;
        }

        InCallTrace.begin("callcard_updateprimaryinfo");
        if (mPrimary == null) {
            // Clear the primary display info.
            ui.setPrimary(null, null, false, null, null, false, false, false);
            return;
        }

        // Hide the contact photo if we are in a video call and the incoming video surface is
        // showing.
        boolean showContactPhoto = !VideoCallPresenter
                .showIncomingVideo(mPrimary.getVideoState(), mPrimary.getState());

        // Call placed through a work phone account.
        boolean hasWorkCallProperty = mPrimary.hasProperty(PROPERTY_ENTERPRISE_CALL);

        if (mPrimary.isConferenceCall()) {
            Log.d(TAG, "Update primary display info for conference call.");

            /// M: [VoLTE conference]show caller info for incoming volte conference @{
            if (isIncomingVolteConference(mPrimary)) {
                setPrimaryForIncomingVolteConference(showContactPhoto, hasWorkCallProperty);
            } else if ((!mPrimary.can(android.telecom.Call.Details.CAPABILITY_MANAGE_CONFERENCE)) &&
                    (showConfHostNumberToParticipant(mContext))) {
                showHostNumberToParticipantInConf(showContactPhoto, hasWorkCallProperty);
            } else {
            /// @}
            ui.setPrimary(
                    null /* number */,
                    getConferenceString(mPrimary),
                    false /* nameIsNumber */,
                    null /* label */,
                    getConferencePhoto(mPrimary),
                    false /* isSipCall */,
                    showContactPhoto,
                    hasWorkCallProperty);
            }
        } else if (mPrimaryContactInfo != null) {
            Log.d(TAG, "Update primary display info for " + mPrimaryContactInfo);

            String name = getNameForCall(mPrimaryContactInfo);
            String number;

            boolean isChildNumberShown = !TextUtils.isEmpty(mPrimary.getChildNumber());
            boolean isForwardedNumberShown = !TextUtils.isEmpty(mPrimary.getLastForwardedNumber());
            boolean isCallSubjectShown = shouldShowCallSubject(mPrimary);

            /*PRIZE-Delete-Optimize_Dialer-wangzhong-2018_3_5-start*/
            /*if (isCallSubjectShown) {
                ui.setCallSubject(mPrimary.getCallSubject());
            } else {
                ui.setCallSubject(null);
            }*/
            /*PRIZE-Delete-Optimize_Dialer-wangzhong-2018_3_5-end*/

            if (isCallSubjectShown) {
                number = null;
            } else if (isChildNumberShown) {
                number = mContext.getString(R.string.child_number, mPrimary.getChildNumber());
            } else if (isForwardedNumberShown) {
                // Use last forwarded number instead of second line, if present.
                number = mPrimary.getLastForwardedNumber();
            } else {
                number = getNumberForCall(mPrimaryContactInfo);
            }

            /*PRIZE-Delete-Optimize_Dialer-wangzhong-2018_3_5-start*/
            /*ui.showForwardIndicator(isForwardedNumberShown);*/
            /*PRIZE-Delete-Optimize_Dialer-wangzhong-2018_3_5-end*/
            maybeShowHdAudioIcon();

            boolean nameIsNumber = name != null && name.equals(mPrimaryContactInfo.number);
            // Call with caller that is a work contact.
            boolean isWorkContact = (mPrimaryContactInfo.userType == ContactsUtils.USER_TYPE_WORK);
            ui.setPrimary(
                    number,
                    name,
                    nameIsNumber,
                    isChildNumberShown || isCallSubjectShown ? null : mPrimaryContactInfo.label,
                    mPrimaryContactInfo.photo,
                    mPrimaryContactInfo.isSipCall,
                    showContactPhoto,
                    hasWorkCallProperty || isWorkContact);

            updateContactInteractions();
        } else {
            // Clear the primary display info.
            ui.setPrimary(null, null, false, null, null, false, false, false);
        }

        if (mEmergencyCallListener != null) {
            boolean isEmergencyCall = mPrimary.isEmergencyCall();
            mEmergencyCallListener.onCallUpdated((BaseFragment) ui, isEmergencyCall);
        }

        /// M: Add for plugin. @{
        if (mPrimary != null) {
            ExtensionManager.getCallCardExt().updatePrimaryDisplayInfo(mPrimary.getTelecomCall());
        }
        /// @}
        InCallTrace.end("callcard_updateprimaryinfo");
    }

    private void updatePrimaryDisplayInfo(boolean hasSecond) {
        final CallCardUi ui = getUi();
        if (ui == null) {
            // TODO: May also occur if search result comes back after ui is destroyed. Look into
            // removing that case completely.
            Log.d(TAG, "updatePrimaryDisplayInfo called but ui is null!");
            return;
        }

        InCallTrace.begin("callcard_updateprimaryinfo");
        if (mPrimary == null) {
            // Clear the primary display info.
            /*PRIZE-change-yuandailin-2016-3-14-start*/
            //ui.setPrimary(null, null, false, null, null, false);
            ui.setPrimary(null, null, false, null, null, null, false);
            /*PRIZE-change-yuandailin-2016-3-14-end*/
            return;
        }
        /*PRIZE-add-fix bug[30020]-huangpengfei-2017-3-10-start*/
        final boolean showSwap = mPrimary.can(
                android.telecom.Call.Details.CAPABILITY_SWAP_CONFERENCE);
        /*PRIZE-add-fix bug[30020]-huangpengfei-2017-3-10-end*/

        if (mPrimary.isConferenceCall()) {
            Log.d(TAG, "Update primary display info for conference call.");

            /// M: [VoLTE conference]show caller info for incoming volte conference @{
            if (isIncomingVolteConference(mPrimary)) {
                //setPrimaryForIncomingVolteConference();
            } else {
                /// @}
                ui.setPrimary(
                        null /* number */,
                        getConferenceString(mPrimary),
                        false /* nameIsNumber */,
                        null /* label */,
                        null /* location*/,//PRIZE-add-yuandailin-2016-3-14
                        getConferencePhoto(mPrimary),
                        false /* isSipCall */);
            }

            /*PRIZE-add-fix bug[30020]-huangpengfei-2017-3-10-start*/
            if (!hasSecond) {
                if (showSwap) {
                    ui.setPrizeMultiPartyCallInfo(true,null,mContext.getString(R.string.prize_multi_party_call),false,null,null);
                } else {
                    ui.setPrizeMultiPartyCallInfo(false,null,null,false,null,null);
                }
            } else {
                /*PRIZE-add-fix bug[29283]-huangpengfei-2017-3-13-start*/
                ui.setPrimarySmall(false,null,null,false,null,null);
                /*PRIZE-add-fix bug[29283]-huangpengfei-2017-3-13-end*/
            }
            /*PRIZE-add-fix bug[30020]-huangpengfei-2017-3-10-end*/
        } else if (mPrimaryContactInfo != null) {
            Log.d(TAG, "Update primary display info for " + mPrimaryContactInfo);

            String name = getNameForCall(mPrimaryContactInfo);
            String number = getNumberForCall(mPrimaryContactInfo);
            boolean nameIsNumber = name != null && name.equals(mPrimaryContactInfo.number);
            /*PRIZE-change-yuandailin-2016-3-14-start*/
            String location = getLocationForCall(mPrimaryContactInfo);
            if (hasSecond) {
                ui.setPrimarySmall(true, number, name, nameIsNumber, location, mPrimaryContactInfo.label);
                ui.setPrimarySmallEnabled(true);
            } else {
                ui.setPrimarySmall(false, number, name, nameIsNumber, location, mPrimaryContactInfo.label);/*PRIZE-add-yuandailin-2016-5-30-start*/
                ui.setPrimary(
                        number,
                        name,
                        nameIsNumber,
                        mPrimaryContactInfo.label,
                        location,
                        mPrimaryContactInfo.photo,
                        mPrimaryContactInfo.isSipCall);
                /*PRIZE-add-fix bug[30020]-huangpengfei-2017-3-10-start*/
                ui.setPrizeMultiPartyCallInfo(false,null,null,false,null,null);
                /*PRIZE-add-fix bug[30020]-huangpengfei-2017-3-10-end*/
            }
            /*PRIZE-change-yuandailin-2016-3-14-end*/
        } else {
            // Clear the primary display info.
            ui.setPrimary(null, null, false, null, null, null, false);//PRIZE-change-yuandailin-2016-3-14
        }

        if (mEmergencyCallListener != null) {
            boolean isEmergencyCall = PhoneNumberUtils.isEmergencyNumber(
                    getNumberFromHandle(mPrimary.getHandle()));
            mEmergencyCallListener.onCallUpdated((BaseFragment) ui, isEmergencyCall);
        }

        /// M: Add for plugin. @{
        if (mPrimary != null) {
            ExtensionManager.getCallCardExt().updatePrimaryDisplayInfo(mPrimary.getTelecomCall());
        }
        /// @}
        InCallTrace.end("callcard_updateprimaryinfo");
    }

    private void updateSecondaryDisplayInfo() {
        final CallCardUi ui = getUi();
        if (ui == null) {
            return;
        }

        if (mSecondary == null) {
            // Clear the secondary display info.
            /*PRIZE-change-yuandailin-2016-3-14-start*/
            //ui.setSecondary(false, null, false, null, null, false /* isConference */
            //        , false /* isVideoCall */);
            ui.setSecondary(false, null, null, false, null, null, null, false /* isConference */,
                    false /* isVideoCall */);
            return;
        }

        /// M: add for OP09 plug in @{
        ExtensionManager.getCallCardExt().setPhoneAccountForSecondCall(
                getAccountForCall(mSecondary));
        /// add for OP09 plug in @}
        /// M: Can't swipe calls when primary call's in intermediate video session state @{
        /*PRIZE-Change-DialerV8-wangzhong-2017_7_19-start*/
        /*final boolean canSwipeCall =
                mPrimary.getSessionModificationState()
                != Call.SessionModificationState.WAITING_FOR_UPGRADE_RESPONSE
            && mPrimary.getSessionModificationState()
                != Call.SessionModificationState.RECEIVED_UPGRADE_TO_VIDEO_REQUEST
            && mPrimary.getSessionModificationState()
                != Call.SessionModificationState.WAITING_FOR_DOWNGRADE_RESPONSE
            && mPrimary.getSessionModificationState()
                != Call.SessionModificationState.WAITING_FOR_PAUSE_VIDEO_RESPONSE
            && mPrimary.getSessionModificationState()
                != Call.SessionModificationState.RECEIVED_UPGRADE_TO_VIDEO_REQUEST_ONE_WAY;*/
        boolean prizeCanSwipeCall = false;
        if (null != mPrimary) {
            prizeCanSwipeCall =
                    mPrimary.getSessionModificationState()
                    != Call.SessionModificationState.WAITING_FOR_UPGRADE_RESPONSE
                && mPrimary.getSessionModificationState()
                    != Call.SessionModificationState.RECEIVED_UPGRADE_TO_VIDEO_REQUEST
                && mPrimary.getSessionModificationState()
                    != Call.SessionModificationState.WAITING_FOR_DOWNGRADE_RESPONSE
                && mPrimary.getSessionModificationState()
                    != Call.SessionModificationState.WAITING_FOR_PAUSE_VIDEO_RESPONSE
                && mPrimary.getSessionModificationState()
                    != Call.SessionModificationState.RECEIVED_UPGRADE_TO_VIDEO_REQUEST_ONE_WAY;
        }
        final boolean canSwipeCall = prizeCanSwipeCall;
        /*PRIZE-Change-DialerV8-wangzhong-2017_7_19-end*/
        /// @}
        if (mSecondary.isConferenceCall()) {
            ui.setSecondary(
                    true /* show */,
                    getConferenceString(mSecondary),
                    false /* nameIsNumber */,
                    null /* label */,
                    getCallProviderLabel(mSecondary),
                    true /* isConference */,
                    mSecondary.isVideoCall(mContext),
                    mIsFullscreen);
            /// M: sometimes we can't swipe call @{
            ui.setSecondaryEnabled(canSwipeCall);
            /// @}
        } else if (mSecondaryContactInfo != null) {
            Log.d(TAG, "updateSecondaryDisplayInfo() " + mSecondaryContactInfo);
            String name = getNameForCall(mSecondaryContactInfo);
            boolean nameIsNumber = name != null && name.equals(mSecondaryContactInfo.number);
            /*PRIZE-add-yuandailin-2016-3-14-start*/
            String number = getNumberForCall(mSecondaryContactInfo);
            String location = getLocationForCall(mSecondaryContactInfo);
            ui.setSecondary(
                    true /* show */,
                    name,
                    number,
                    nameIsNumber,
                    mSecondaryContactInfo.label,
                    location,
                    getCallProviderLabel(mSecondary),
                    false /* isConference */,
                    mSecondary.isVideoCall(mContext));
            /*PRIZE-add-yuandailin-2016-3-14-end*/
            /// Fix ALPS01768230. @{
            ui.setSecondaryEnabled(canSwipeCall);
        } else {
            ui.setSecondaryEnabled(false);
            /// @}

            // Clear the secondary display info.
            /*PRIZE-change-yuandailin-2016-3-14-start*/
            //ui.setSecondary(false, null, false, null, null, false /* isConference */,
            //        false /* isVideoCall */);
            ui.setSecondary(false, null, null, false, null, null, null, false /* isConference */,
                    false /* isVideoCall */);
            /*PRIZE-change-yuandailin-2016-3-14-end*/
        }
    }

    /**
     * Gets the phone account to display for a call.
     */
    private PhoneAccount getAccountForCall(Call call) {
        PhoneAccountHandle accountHandle = call.getAccountHandle();
        if (accountHandle == null) {
            return null;
        }
        /*PRIZE-add-yuandailin-2015-9-8-start*/
        int subId = call.getSubId();
        if (subId != -1 && getUi() != null) {
            /*PRIZE-Change-PrizeInCallUI_N-wangzhong-2016_10_24-start*/
            /*getUi().setSimIcon(subId);*/
        	if(mPrimaryContactInfo == null){
        		getUi().setSimIcon(subId, "", false);
        	}else{
        		String name = getNameForCall(mPrimaryContactInfo);
                boolean nameIsNumber = name != null && name.equals(mPrimaryContactInfo.number);
                getUi().setSimIcon(subId, name, nameIsNumber);
        	}
            /*PRIZE-Change-PrizeInCallUI_N-wangzhong-2016_10_24-end*/
        }
        /*PRIZE-add-yuandailin-2015-9-8-end*/
        return TelecomManagerCompat.getPhoneAccount(
                InCallPresenter.getInstance().getTelecomManager(),
                accountHandle);
    }

    /**
     * Returns the gateway number for any existing outgoing call.
     */
    private String getGatewayNumber() {
        if (hasOutgoingGatewayCall()) {
            return getNumberFromHandle(mPrimary.getGatewayInfo().getGatewayAddress());
        }
        return null;
    }

    /**
     * Return the string label to represent the call provider
     */
    private String getCallProviderLabel(Call call) {
        PhoneAccount account = getAccountForCall(call);
        TelecomManager mgr = InCallPresenter.getInstance().getTelecomManager();
        if (account != null && !TextUtils.isEmpty(account.getLabel())
                && TelecomManagerCompat.getCallCapablePhoneAccounts(mgr).size() > 1) {
            return account.getLabel().toString();
        }
        return null;
    }

    /**
     * Returns the label (line of text above the number/name) for any given call.
     * For example, "calling via [Account/Google Voice]" for outgoing calls.
     */
    private String getConnectionLabel() {
        // M: add for OP09 plug in. @{
        String label = ExtensionManager.getCallCardExt().getCallProviderLabel(mContext,
                getAccountForCall(mPrimary));
        if (label != null) {
            return label;
        }
        // @}
        StatusHints statusHints = mPrimary.getTelecomCall().getDetails().getStatusHints();
        if (statusHints != null && !TextUtils.isEmpty(statusHints.getLabel())) {
            return statusHints.getLabel().toString();
        }

        if (hasOutgoingGatewayCall() && getUi() != null) {
            // Return the label for the gateway app on outgoing calls.
            final PackageManager pm = mContext.getPackageManager();
            try {
                ApplicationInfo info = pm.getApplicationInfo(
                        mPrimary.getGatewayInfo().getGatewayProviderPackageName(), 0);
                return pm.getApplicationLabel(info).toString();
            } catch (PackageManager.NameNotFoundException e) {
                Log.e(this, "Gateway Application Not Found.", e);
                return null;
            }
        }
        return getCallProviderLabel(mPrimary);
    }

    private Drawable getCallStateIcon() {
        /// M: add for OP09 Plug in.@{
        Drawable iconEx = ExtensionManager.getCallCardExt().getCallProviderIcon(mContext,
                getAccountForCall(mPrimary));
        if (iconEx != null) {
            return iconEx;
        }
        /// @}
        // Return connection icon if one exists.
        StatusHints statusHints = mPrimary.getTelecomCall().getDetails().getStatusHints();
        if (statusHints != null && statusHints.getIcon() != null) {
            Drawable icon = statusHints.getIcon().loadDrawable(mContext);
            if (icon != null) {
                return icon;
            }
        }

        return null;
    }

    private boolean hasOutgoingGatewayCall() {
        // We only display the gateway information while STATE_DIALING so return false for any other
        // call state.
        // TODO: mPrimary can be null because this is called from updatePrimaryDisplayInfo which
        // is also called after a contact search completes (call is not present yet).  Split the
        // UI update so it can receive independent updates.
        if (mPrimary == null) {
            return false;
        }
        return Call.State.isDialing(mPrimary.getState()) && mPrimary.getGatewayInfo() != null &&
                !mPrimary.getGatewayInfo().isEmpty();
    }

    /*PRIZE-add the dialing text-yuandailin-2016-8-19-start*/
    public int getPrimaryCallState() {
        if (mPrimary != null) {
            return mPrimary.getState();
        }
        return -1;
    }
    /*PRIZE-add the dialing text-yuandailin-2016-8-19-end*/

	/*PRIZE-feture add hotline--zhoushuanghua-2018-06-28-start*/
	public static boolean isContain(String number) {
		if(number != null) {
			for (int i = 0; i < visitHotline.length; i++) {
				String hotline = visitHotline[i];
				if (number.replaceAll(" ", "").equals(hotline)) {
					return true;
				}
			}
		}
		return false;
	}
    /*PRIZE-feture add hotline--zhoushuanghua-2018-06-28-end*/
	
    /**
     * Gets the name to display for the call.
     */
    @NeededForTesting
    String getNameForCall(ContactCacheEntry contactInfo) {
        String preferredName = ContactDisplayUtils.getPreferredDisplayName(
                contactInfo.namePrimary,
                contactInfo.nameAlternative,
                mContactsPreferences);
		/*PRIZE-feture add hotline--ganxiayong-2018-05-15-start*/
		String number = contactInfo.number;
		if (number != null && number.startsWith(HOTLINE_PREFIX) && isContain(number)) {
			return myContext.getResources().getString(R.string.visit_hotline);
        }
		/*PRIZE-feture add hotline--ganxiayong-2018-05-15-end*/
        if (TextUtils.isEmpty(preferredName)) {
            return contactInfo.number;
        }
        return preferredName;
    }

    /**
     * Gets the number to display for a call.
     */
    @NeededForTesting
    String getNumberForCall(ContactCacheEntry contactInfo) {
        // If the name is empty, we use the number for the name...so don't show a second
        // number in the number field
        String preferredName = ContactDisplayUtils.getPreferredDisplayName(
                    contactInfo.namePrimary,
                    contactInfo.nameAlternative,
                    mContactsPreferences);
        if (TextUtils.isEmpty(preferredName)) {
            return contactInfo.location;
        }
        return contactInfo.number;
    }

    /*PRIZE-add-yuandailin-2016-3-14-start*/
    private static String getLocationForCall(ContactCacheEntry contactInfo) {
        return contactInfo.location;
    }
    /*PRIZE-add-yuandailin-2016-3-14-end*/

    public void secondaryInfoClicked() {
        if (mSecondary == null) {
            Log.w(this, "Secondary info clicked but no secondary call.");
            return;
        }
        /// M: [1A1H2W] if two incoming exist, switch them @{
        if (InCallUtils.isTwoIncomingCalls()) {
            CallList.getInstance().switchIncomingCalls();
            return;
        }
        /// @}

        Log.i(this, "Swapping call to foreground: " + mSecondary);
        /// M: [log optimize]
        Log.op(mSecondary, Log.CcOpAction.UNHOLD, "secondary call banner clicked.");
        TelecomAdapter.getInstance().unholdCall(mSecondary.getId());
    }

    public void endCallClicked() {
        if (mPrimary == null) {
            return;
        }

        Log.i(this, "Disconnecting call: " + mPrimary);
        final String callId = mPrimary.getId();
        mPrimary.setState(Call.State.DISCONNECTING);
        CallList.getInstance().onUpdate(mPrimary);
        /// M: [log optimize]
        Log.op(mPrimary, Log.CcOpAction.DISCONNECT, "end call clicked");
        TelecomAdapter.getInstance().disconnectCall(callId);
   }

    private String getNumberFromHandle(Uri handle) {
        return handle == null ? "" : handle.getSchemeSpecificPart();
    }

    /**
     * Handles a change to the fullscreen mode of the in-call UI.
     *
     * @param isFullscreenMode {@code True} if the in-call UI is entering full screen mode.
     */
    @Override
    public void onFullscreenModeChanged(boolean isFullscreenMode) {
        mIsFullscreen = isFullscreenMode;
        final CallCardUi ui = getUi();
        if (ui == null) {
            return;
        }
        ui.setCallCardVisible(!isFullscreenMode);
        ui.setSecondaryInfoVisible(!isFullscreenMode);
        maybeShowManageConferenceCallButton();
    }

    @Override
    public void onSecondaryCallerInfoVisibilityChanged(boolean isVisible, int height) {
        // No-op - the Call Card is the origin of this event.
    }

    private boolean isPrimaryCallActive() {
        return mPrimary != null && mPrimary.getState() == Call.State.ACTIVE;
    }

    private String getConferenceString(Call call) {
        boolean isGenericConference = call.hasProperty(Details.PROPERTY_GENERIC_CONFERENCE);
        Log.v(this, "getConferenceString: " + isGenericConference);

        final int resId = isGenericConference
                ? R.string.card_title_in_call : R.string.card_title_conf_call;
        return mContext.getResources().getString(resId);
    }

    private Drawable getConferencePhoto(Call call) {
        boolean isGenericConference = call.hasProperty(Details.PROPERTY_GENERIC_CONFERENCE);
        Log.v(this, "getConferencePhoto: " + isGenericConference);

        /*PRIZE-change-yuandailin-2015-7-31-start*/
        final int resId = isGenericConference
                ? R.drawable.prize_incallui_little_photo : R.drawable.img_conference;//PRIZE-change-yuandailin-2016-6-2
        /*PRIZE-change-yuandailin-2015-7-31-end*/
        Drawable photo = mContext.getResources().getDrawable(resId);
        photo.setAutoMirrored(true);
        return photo;
    }

    private boolean shouldShowEndCallButton(Call primary, int callState) {
        if (primary == null) {
            return false;
        }
        if ((!Call.State.isConnectingOrConnected(callState)
                && callState != Call.State.DISCONNECTING) || callState == Call.State.INCOMING) {
            return false;
        }
        if (mPrimary.getSessionModificationState()
                == Call.SessionModificationState.RECEIVED_UPGRADE_TO_VIDEO_REQUEST) {
            return false;
        }
        return true;
    }

    private void maybeSendAccessibilityEvent(InCallState oldState, InCallState newState,
                                             boolean primaryChanged) {
        if (mContext == null) {
            return;
        }
        final AccessibilityManager am = (AccessibilityManager) mContext.getSystemService(
                Context.ACCESSIBILITY_SERVICE);
        if (!am.isEnabled()) {
            return;
        }
        // Announce the current call if it's new incoming/outgoing call or primary call is changed
        // due to switching calls between two ongoing calls (one is on hold).
        if ((oldState != InCallState.OUTGOING && newState == InCallState.OUTGOING)
                || (oldState != InCallState.INCOMING && newState == InCallState.INCOMING)
                || primaryChanged) {
            if (getUi() != null) {
                getUi().sendAccessibilityAnnouncement();
            }
        }
    }

    /**
     * Determines whether the call subject should be visible on the UI.  For the call subject to be
     * visible, the call has to be in an incoming or waiting state, and the subject must not be
     * empty.
     *
     * @param call The call.
     * @return {@code true} if the subject should be shown, {@code false} otherwise.
     */
    private boolean shouldShowCallSubject(Call call) {
        if (call == null) {
            return false;
        }

        boolean isIncomingOrWaiting = mPrimary.getState() == Call.State.INCOMING ||
                mPrimary.getState() == Call.State.CALL_WAITING;
        return isIncomingOrWaiting && !TextUtils.isEmpty(call.getCallSubject()) &&
                call.getNumberPresentation() == TelecomManager.PRESENTATION_ALLOWED &&
                call.isCallSubjectSupported();
    }

    /**
     * Determines whether the "note sent" toast should be shown.  It should be shown for a new
     * outgoing call with a subject.
     *
     * @param call The call
     * @return {@code true} if the toast should be shown, {@code false} otherwise.
     */
    private boolean shouldShowNoteSentToast(Call call) {
        return call != null && hasCallSubject(call) && (call.getState() == Call.State.DIALING
                || call.getState() == Call.State.CONNECTING);
    }

    private static boolean hasCallSubject(Call call) {
        return !TextUtils.isEmpty(call.getTelecomCall().getDetails().getIntentExtras()
                .getString(TelecomManager.EXTRA_CALL_SUBJECT));
    }

    public interface CallCardUi extends Ui {
        void setVisible(boolean on);
        /*PRIZE-Delete-Optimize_Dialer-wangzhong-2018_3_5-start*/
        /*void setContactContextTitle(View listHeaderView);
        void setContactContextContent(ListAdapter listAdapter);
        void showContactContext(boolean show);*/
        /*PRIZE-Delete-Optimize_Dialer-wangzhong-2018_3_5-end*/
        void setCallCardVisible(boolean visible);
        void setPrimary(String number, String name, boolean nameIsNumber, String label,
                Drawable photo, boolean isSipCall, boolean isContactPhotoShown, boolean isWorkCall);

        /*PRIZE-add-yuandailin-2016-3-14-start*/
        void setPrimarySmall(boolean show, String number, String name, boolean nameIsNumber, String location,
                String label);
        void setPrimary(String number, String name, boolean nameIsNumber, String location, String label,
                Drawable photo, boolean isSipCall);
        void setSecondary(boolean show, String name, String number, boolean nameIsNumber, String label,
                String location, String providerLabel, boolean isConference, boolean isIncoming);
        /*PRIZE-Change-PrizeInCallUI_N-wangzhong-2016_10_24-start*/
        /*void setSimIcon(int subId);*/
        void setSimIcon(int subId, String name, boolean nameIsNumber);
        /*PRIZE-Change-PrizeInCallUI_N-wangzhong-2016_10_24-end*/
        void setPrimarySmallEnabled(boolean enable);
        void setPrimaryCallElapsedTime(boolean show, long duration, boolean hasSecond);
        /*PRIZE-add-yuandailin-2016-3-14-end*/

        void setSecondary(boolean show, String name, boolean nameIsNumber, String label,
                String providerLabel, boolean isConference, boolean isVideoCall,
                boolean isFullscreen);
        void setSecondaryInfoVisible(boolean visible);
        void setCallState(int state, int videoState, int sessionModificationState,
                DisconnectCause disconnectCause, String connectionLabel,
                Drawable connectionIcon, String gatewayNumber, boolean isWifi,
                boolean isConference, boolean isWorkCall);
        void setPrimaryCallElapsedTime(boolean show, long duration);
        void setPrimaryName(String name, boolean nameIsNumber);
        void setPrimaryImage(Drawable image, boolean isVisible);
        /*PRIZE-Delete-Optimize_Dialer-wangzhong-2018_3_5-start*/
        /*void setPrimaryPhoneNumber(String phoneNumber);
        void setPrimaryLabel(String label);*/
        /*PRIZE-Delete-Optimize_Dialer-wangzhong-2018_3_5-end*/
        void setEndCallButtonEnabled(boolean enabled, boolean animate);
        void setCallbackNumber(String number, boolean isEmergencyCalls);
        /*PRIZE-Delete-Optimize_Dialer-wangzhong-2018_3_5-start*/
        /*void setCallSubject(String callSubject);*/
        /*PRIZE-Delete-Optimize_Dialer-wangzhong-2018_3_5-end*/
        void setProgressSpinnerVisible(boolean visible);
        void showHdAudioIndicator(boolean visible);
        /*PRIZE-Delete-Optimize_Dialer-wangzhong-2018_3_5-start*/
        /*void showForwardIndicator(boolean visible);*/
        /*PRIZE-Delete-Optimize_Dialer-wangzhong-2018_3_5-end*/
        void showManageConferenceCallButton(boolean visible);
        boolean isManageConferenceVisible();
        /*PRIZE-Delete-Optimize_Dialer-wangzhong-2018_3_5-start*/
        /*boolean isCallSubjectVisible();*/
        /*PRIZE-Delete-Optimize_Dialer-wangzhong-2018_3_5-end*/
        void animateForNewOutgoingCall();
        void sendAccessibilityAnnouncement();
        void showNoteSentToast();
        /// M: @{
        // Add for recording
        void updateVoiceRecordIcon(boolean show);
        // ALPS01759672.
        void setSecondaryEnabled(boolean enable);
        /// @}
        /*PRIZE-Delete-Optimize_Dialer-wangzhong-2018_3_5-start*/
        /// M: for ALPS01945830, update primarycall and callbutton background color.@{
        /*void updateColors();*/
        /// @}
        /*PRIZE-Delete-Optimize_Dialer-wangzhong-2018_3_5-end*/
        /// M: got videodisplay surface visibility @{
        boolean isVideoDisplayViewVisible();
        /// @}

        /*PRIZE-Delete-Optimize_Dialer-wangzhong-2018_3_5-start*/
        /// M: used for hiding or display photo. @{
        /*void setPhotoVisible(boolean visible);*/
        /// @}
        /*PRIZE-Delete-Optimize_Dialer-wangzhong-2018_3_5-end*/

        void updatePrimaryCallInfo(boolean isHide);//PRIZE-add for video call-yuandailin-2016-7-6
        void updateCallStatus(InCallState newState);//PRIZE-add for video call-haungpengfei-2017-3-6

        /*PRIZE-Add-Optimize_Dialer-wangzhong-2018_3_5-start*/
        boolean isShowingVoiceRecordIcon();
        /*PRIZE-Add-Optimize_Dialer-wangzhong-2018_3_5-end*/

        /*PRIZE-add-fix bug[30020]-huangpengfei-2017-3-10-start*/
        void setPrizeMultiPartyCallInfo(boolean show, String number, String name, boolean nameIsNumber,String location,
            String label);
        /*PRIZE-add-fix bug[30020]-huangpengfei-2017-3-10-end*/
        /*PRIZE-add-fix bug[29283]-huangpengfei-2017-3-13-start*/
        void showSIMNoSupportDialog();
        /*PRIZE-add-fix bug[29283]-huangpengfei-2017-3-13-end*/
    }

    /// M: For volte @{
    /**
     * listner onContactInfoUpdated(),
     * will be notified when ContactInfoCache finish re-query, triggered by some
     * call's number change.
     */
    private final ContactInfoUpdatedListener mContactInfoUpdatedListener
            = new ContactInfoUpdatedListener() {
        public void onContactInfoUpdated(String callId) {
            handleContactInfoUpdated(callId);
        }
    };

    /**
     * M: trigger UI update for call when the call changes to call waiting state.
     * @param call
     */
    private void handleIsCallWaitingChanged(Call call) {
        Log.d(this, "handleIsCallWaitingChanged()... call = " + call);
        // only trigger refresh UI when the primary call changed to be call waiting,
        // when mPrimaryContactInfo == null, skip; when mPrimaryContactInfo becomes non-null,
        // will trigger it.
        if (call != null && mPrimary != null && call.getId() == mPrimary.getId()) {
            if (mPrimaryContactInfo != null) {
                Log.d(this, "handleIsCallWaitingChanged()... trigger UI refresh.");
                // TODO: maybe we can re-use google default follow,
                // onDetailsChanged() in in this class will trigger below function also.
                updatePrimaryCallState();
            }
        }
    }

    /**
     * M: ask for new ContactInfo to update UI when re-query complete by ContactInfoCache.
     */
    private void handleContactInfoUpdated(String callId) {
        Log.d(this, "handleContactInfoUpdated()... callId = " + callId);
        Call call = null;
        boolean isPrimary = false;
        if (mPrimary != null && mPrimary.getId() == callId) {
            isPrimary = true;
            call = mPrimary;
        } else if (mSecondary != null && mSecondary.getId() == callId) {
            isPrimary = false;
            call = mSecondary;
        }
        if (call != null) {
            startContactInfoSearch(call, isPrimary, call.getState() == Call.State.INCOMING);
        }
    }
    /// @}

    /// M: For second call color @{
    public int getSecondCallColor() {
        return InCallPresenter.getInstance().getPrimaryColorFromCall(mSecondary);
    }
    /// @}

    /// M: add for VOLTE feature. @{
    /**
     * M: check whether the incoming call is conference.
     * @param call
     * @return true if it is Volte conference call.
     */
    private boolean isIncomingVolteConference(Call call) {
        return InCallUIVolteUtils.isIncomingVolteConferenceCall(call);
    }

    /**
     * M: to set primary call for volte conference.
     */
    private void setPrimaryForIncomingVolteConference(boolean showContactPhoto,
                                                      boolean hasWorkCallProperty) {
        if (mPrimaryContactInfo == null) {
            Log.d(this, "[setPrimaryForIncomingVolteConference]no contact info");
            getUi().setPrimary(null, null, false, null, null, false, false, false);
            return;
        }
        String name = getNameForCall(mPrimaryContactInfo);
        String number = getNumberForCall(mPrimaryContactInfo);
        boolean nameIsNumber = name != null && name.equals(mPrimaryContactInfo.number);
        getUi().setPrimary(
                number,
                name,
                nameIsNumber,
                mPrimaryContactInfo.label,
                getConferencePhoto(mPrimary),
                false/*isSip*/,
                showContactPhoto,
                hasWorkCallProperty);
    }

    /**
     * M: need to update primary for volte case.
     * @param oldState
     * @param newState
     * @param call
     * @return
     */
    private boolean needUpdatePrimaryForVolte(
            InCallState oldState, InCallState newState, Call call) {
        return call != null &&
                call.isConferenceCall() &&
                oldState == InCallState.INCOMING &&
                newState != InCallState.INCOMING;
    }
    /// @}

    /// M: [Voice Record] add for phone recording. @{
    /**
     * update record state.
     */
    @Override
    public void onUpdateRecordState(int state, int customValue) {
        if (FeatureOptionWrapper.isSupportPhoneVoiceRecording()) {
            updateVoiceCallRecordState();
        }
    }

    /*PRIZE-Change-Optimize_Dialer-wangzhong-2018_3_5-start*/
    /**
     * update voice call record state
     */
    /*private void updateVoiceCallRecordState() {
        final CallCardUi ui = getUi();
        if (ui == null) {
            return;
        }

        Log.d(this, "[updateVoiceCallRecordState]...");

        Call ringCall = null;
        int ringCallState = -1;
        ringCall = CallList.getInstance().getIncomingCall();
        if (null != ringCall) {
            ringCallState = ringCall.getState();
        }
        if ((InCallPresenter.getInstance().isRecording()) && (ringCallState != Call.State.INCOMING)
                && (ringCallState != Call.State.CALL_WAITING)) {
            ui.updateVoiceRecordIcon(true);
        } else if ((!InCallPresenter.getInstance().isRecording())
                || (ringCallState == Call.State.INCOMING)
                || (ringCallState == Call.State.CALL_WAITING)) {
            ui.updateVoiceRecordIcon(false);
        }
    }*/
    private void updateVoiceCallRecordState() {
        updateVoiceCallRecordState(false);
    }

    private void updateVoiceCallRecordState(boolean isTakeEffect) {
        final CallCardUi ui = getUi();
        if (ui == null) {
            return;
        }
        Call ringCall = null;
        int ringCallState = -1;
        ringCall = CallList.getInstance().getIncomingCall();
        if (null != ringCall) {
            ringCallState = ringCall.getState();
            Log.d(this, "[Prize] [updateVoiceCallRecordState] getIncomingCall");
        } else {
            ringCall = CallList.getInstance().getVideoUpgradeRequestCall();
            if (null != ringCall) {
                ringCallState = ringCall.getState();
                Log.d(this, "[Prize] [updateVoiceCallRecordState] getVideoUpgradeRequestCall");
            }
        }
        boolean isRecording = InCallPresenter.getInstance().isRecording();
        Log.d(this, "[Prize] [updateVoiceCallRecordState] isRecording : " + isRecording
                + ", ringCallState : " + ringCallState + ", ui.isShowingVoiceRecordIcon() : "
                + ui.isShowingVoiceRecordIcon() + ", isTakeEffect : " + isTakeEffect);
        if (isRecording && (ringCallState != Call.State.INCOMING)
                && (ringCallState != Call.State.CALL_WAITING)
                && (ui.isShowingVoiceRecordIcon() || isTakeEffect)) {
            ui.updateVoiceRecordIcon(true);
        } else if ((!isRecording) || (ringCallState == Call.State.INCOMING)
                || (ringCallState == Call.State.CALL_WAITING)
                || !ui.isShowingVoiceRecordIcon()) {
            ui.updateVoiceRecordIcon(false);
        }
    }
    /*PRIZE-Change-Optimize_Dialer-wangzhong-2018_3_5-end*/
    /// @}

    /// M: ALPS02217975 previously disconnected call screen for cdma is shown again@{
    /*
     * get cdma conference call from callist.
     * @param callList
     */
    private Call getDisconnectedCdmaConfCall(CallList callList) {
        Call cdmaConfCall = callList.getCdmaConfCall();
        if (cdmaConfCall != null  && cdmaConfCall.getState() == Call.State.DISCONNECTED) {
            return cdmaConfCall;
        }
        return null;
    }
    /// @}

    public long getCountdown() {
        return InCallPresenter.getInstance().getAutoDeclineCountdown();
    }

    /*PRIZE-Delete-Optimize_Dialer-wangzhong-2018_3_5-start*/
     /*
     * M : hide call card photo according with videoUi .
     * @param call is primary call @{
     */
     /*public void displayPhotoWithVideoUi(Call call) {
         final CallCardUi ui = getUi();

         if (ui == null) {
             Log.e(this, "[hidePhotoWithVideoUi] CallCardUi is null return");
             return;
         }

         if (call == null) {
             Log.e(this, "[hidePhotoWithVideoUi] call is null return");
             ui.setPhotoVisible(true);
             return;
         }

         int videoState = call.getVideoState();
         int callState = call.getState();
         boolean isPaused = VideoProfile.isPaused(videoState);
         boolean isCallActive = callState == Call.State.ACTIVE;
         // when in held state ,can't show video, should show photo instead
         boolean isHeld = call.isHeld();
         boolean isShowVideoDisplay = !isPaused && isCallActive && !isHeld;

         Log.d(this, "[hidePhotoWithVideoUi] videoState is->" + videoState
                + " callState is-->" + callState + " isShowVideoDisplay -->"
                + isShowVideoDisplay + " held-->" + isHeld
                + " isShowVideoDisplay -->");
         if (VideoProfile.isBidirectional(videoState)) {
             ui.setPhotoVisible(!isShowVideoDisplay);
         } else if (VideoProfile.isTransmissionEnabled(videoState)) {
             ui.setPhotoVisible(true);
         } else if (VideoProfile.isReceptionEnabled(videoState)) {
             ui.setPhotoVisible(!isShowVideoDisplay);
         } else {
             ui.setPhotoVisible(true);
         }
     }*/
    /// @}
    /*PRIZE-Delete-Optimize_Dialer-wangzhong-2018_3_5-end*/

    ///M: for bug ALPS02695153, when rotate screen , we should store progress show status.@{
    public void setSpinnerShowing(boolean shown) {
        mSpinnerShowing = shown;
    }
    ///@}

    /**
     * M: to show host number to participant.
     */
    private void showHostNumberToParticipantInConf(boolean showContactPhoto,
                                                      boolean hasWorkCallProperty) {
        Log.d(this, "showHostNumberToParticipantInConf");
        if (mPrimaryContactInfo == null) {
            Log.d(this, "showHostNumberToParticipantInConf: no contact info");
            getUi().setPrimary(null, null, false, null, null, false, false, false);
            return;
        }
        String name = getNameForCall(mPrimaryContactInfo);
        String number = getNumberForCall(mPrimaryContactInfo);
        boolean nameIsNumber = name != null && name.equals(mPrimaryContactInfo.number);
        Log.d(this, "name= " + name + " number= " + number + " nameIsNumber= " + nameIsNumber);
        if (name.equals("Unknown") && number == null) {
            Log.d(this, "name unknown, number null, return");
            return;
        }
        getUi().setPrimary(
                number,
                name,
                nameIsNumber,
                mPrimaryContactInfo.label,
                mPrimaryContactInfo.photo,
                false/*mPrimaryContactInfo.isSipCall*/,
                showContactPhoto,
                hasWorkCallProperty);
    }

    /// M: Customize for specific operator and location @{
    private boolean showConfHostNumberToParticipant(Context context) {
        Log.d(this, "showConfHostNumberToParticipant");
        boolean showHostNumber = false;
        PersistableBundle b = null;

        //int phoneId = RadioCapabilitySwitchUtil.getMainCapabilityPhoneId();
        //int subId = SubscriptionManager.getSubIdUsingPhoneId(phoneId);

        TelecomManager telecomManager =
                (TelecomManager) context.getSystemService(Context.TELECOM_SERVICE);
        TelephonyManager telephonyManager = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        int subId = telephonyManager.getSubIdForPhoneAccount(
                telecomManager.getPhoneAccount(mPrimary.getAccountHandle()));

        CarrierConfigManager configMgr = (CarrierConfigManager) context
                .getSystemService(Context.CARRIER_CONFIG_SERVICE);
        if (configMgr != null) {
            b = configMgr.getConfigForSubId(subId);
            if (b != null) {
                showHostNumber = b.getBoolean(CarrierConfigManager
                        .KEY_SHOW_CONF_HOST_NUMBER_TO_PARTICIPANT);
            }
        }
        Log.d(this, "showHostNumber: %s" + showHostNumber);
        return showHostNumber;
    }
    /// @}

    /*Prize-add huangliemin-2016-5-25*/
    /**
     * M: Maybe after querying complete, primary call or secondary call has be changed.
     * So, check the finished querying callId is what kind of call.
     * @param callId
     * @return
     */
    private CallEnum reCalculateContactInfoType(String callId) {
        CallEnum callEnum;
        if (mPrimary != null && mPrimary.getId() == callId) {
            callEnum = CallEnum.PRIMARY;
        } else if (mSecondary != null && mSecondary.getId() == callId) {
            callEnum = CallEnum.SECONDARY;
        } else {
            callEnum = CallEnum.NULL;
        }
        Log.d(this, "reCalculateContactInfoType... callId = " + callId + "; callType = " + callEnum);
        return callEnum;
    }
    /*Prize-add huangliemin-2016-5-25*/

    /*prize-add-fix bug[30020]-huangpengfei-2017-3-10-start*/
    public void swapClicked() {
        if (mPrimary == null) {
            return;
        }

        Log.i(this, "Swapping the call: " + mPrimary);
        /// M: [log optimize]
        Log.op(mPrimary, Log.CcOpAction.SWAP, "swap key clicked.");
        TelecomAdapter.getInstance().swap(mPrimary.getId());
    }
    /*prize-add-fix bug[30020]-huangpengfei-2017-3-10-end*/
}
