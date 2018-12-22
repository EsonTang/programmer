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
 * limitations under the License.
 */

package com.android.server.telecom;

import android.app.ActivityManager;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.UserInfo;
import android.content.Intent;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Process;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.SystemVibrator;
import android.os.PowerManager;
import android.os.Trace;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.CallLog.Calls;
import android.provider.Settings;
import android.telecom.CallAudioState;
import android.telecom.Conference;
import android.telecom.Connection;
import android.telecom.DisconnectCause;
import android.telecom.GatewayInfo;
import android.telecom.ParcelableConference;
import android.telecom.ParcelableConnection;
import android.telecom.PhoneAccount;
import android.telecom.PhoneAccountHandle;
import android.telecom.VideoProfile;
import android.telecom.TelecomManager;
import android.telephony.PhoneNumberUtils;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.telephony.AsyncEmergencyContactNotifier;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.TelephonyProperties;
import com.android.internal.util.IndentingPrintWriter;
import com.android.server.telecom.Call;
import com.android.server.telecom.TelecomServiceImpl.DefaultDialerManagerAdapter;
import com.android.server.telecom.callfiltering.AsyncBlockCheckFilter;
import com.android.server.telecom.callfiltering.BlockCheckerAdapter;
import com.android.server.telecom.callfiltering.CallFilterResultCallback;
import com.android.server.telecom.callfiltering.CallFilteringResult;
import com.android.server.telecom.callfiltering.CallScreeningServiceFilter;
import com.android.server.telecom.callfiltering.DirectToVoicemailCallFilter;
import com.android.server.telecom.callfiltering.IncomingCallFilter;
import com.android.server.telecom.components.ErrorDialogActivity;

import java.util.ArrayList;
import java.util.Collection;

import com.mediatek.telecom.TelecomManagerEx;
import com.mediatek.telecom.TelecomUtils;
///M: add for OP09 plug-in
import com.mediatek.telecom.ext.ExtensionManager;
import com.mediatek.telecom.recording.PhoneRecorderHandler;
import com.mediatek.telecom.volte.TelecomVolteUtils;
import com.mediatek.telephony.TelephonyManagerEx;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import android.widget.Toast;

// prize add by xiarui, PRIZE_LED_BLINK, for flash light control when incoming call 2018-08-03 start
import com.mediatek.common.prizeoption.PrizeOption;
// prize add by xiarui, PRIZE_LED_BLINK, for flash light control when incoming call 2018-08-03 end

/**
 * Singleton.
 *
 * NOTE: by design most APIs are package private, use the relevant adapter/s to allow
 * access from other packages specifically refraining from passing the CallsManager instance
 * beyond the com.android.server.telecom package boundary.
 */
@VisibleForTesting
public class CallsManager extends Call.ListenerBase
        implements VideoProviderProxy.Listener, CallFilterResultCallback {

    // TODO: Consider renaming this CallsManagerPlugin.
    @VisibleForTesting
    public interface CallsManagerListener {
        void onCallAdded(Call call);
        void onCallRemoved(Call call);
        void onCallStateChanged(Call call, int oldState, int newState);
        void onConnectionServiceChanged(
                Call call,
                ConnectionServiceWrapper oldService,
                ConnectionServiceWrapper newService);
        void onIncomingCallAnswered(Call call);
        void onIncomingCallRejected(Call call, boolean rejectWithMessage, String textMessage);
        void onCallAudioStateChanged(CallAudioState oldAudioState, CallAudioState newAudioState);
        void onRingbackRequested(Call call, boolean ringback);
        void onIsConferencedChanged(Call call);
        void onIsVoipAudioModeChanged(Call call);
        void onVideoStateChanged(Call call, int previousVideoState, int newVideoState);
        void onCanAddCallChanged(boolean canAddCall);
        void onSessionModifyRequestReceived(Call call, VideoProfile videoProfile);
        void onHoldToneRequested(Call call);
        /* M: CC part start */
        void onConnectionLost(Call call);
        void onCdmaCallAccepted(Call call);
        /* M: CC part end */
        /**
         * M: The all incoming calls will be sorted according to user's action,
         * since there are more than 1 incoming call exist user may touch to switch
         * any incoming call to the primary screen, the sequence of the incoming call
         * will be changed.
         */
        void onInComingCallListChanged(List<Call> newList);
        ///M: CC: For 3G VT only.
        void onVtStatusInfoChanged(Call call, int status);
        void onExternalCallChanged(Call call, boolean isExternalCall);
    }

    private static final String TAG = "CallsManager";

    private static final int MAXIMUM_LIVE_CALLS = 1;
    private static final int MAXIMUM_HOLD_CALLS = 1;
    /**
     * M: [CTA] MTK supports 2 ringing calls at a time.
     * Requirement comes from CTA DSDA cases(6.2.2.3.3), and coding as a common feature.
     * CTS verify passed. @{
    private static final int MAXIMUM_RINGING_CALLS = 1;
     */
    private static final int MAXIMUM_RINGING_CALLS = 2;
    /** @} */
    private static final int MAXIMUM_DIALING_CALLS = 2;   
    private static final int MAXIMUM_OUTGOING_CALLS = 1;
    private static final int MAXIMUM_TOP_LEVEL_CALLS = 2;

    private static final int[] OUTGOING_CALL_STATES =
            {CallState.CONNECTING, CallState.SELECT_PHONE_ACCOUNT, CallState.DIALING,
                    CallState.PULLING};

    private static final int[] LIVE_CALL_STATES =
            {CallState.CONNECTING, CallState.SELECT_PHONE_ACCOUNT, CallState.DIALING,
                    CallState.PULLING, CallState.ACTIVE};

    public static final String TELECOM_CALL_ID_PREFIX = "TC@";

    // Maps call technologies in PhoneConstants to those in Analytics.
    private static final Map<Integer, Integer> sAnalyticsTechnologyMap;
    static {
        sAnalyticsTechnologyMap = new HashMap<>(5);
        sAnalyticsTechnologyMap.put(PhoneConstants.PHONE_TYPE_CDMA, Analytics.CDMA_PHONE);
        sAnalyticsTechnologyMap.put(PhoneConstants.PHONE_TYPE_GSM, Analytics.GSM_PHONE);
        sAnalyticsTechnologyMap.put(PhoneConstants.PHONE_TYPE_IMS, Analytics.IMS_PHONE);
        sAnalyticsTechnologyMap.put(PhoneConstants.PHONE_TYPE_SIP, Analytics.SIP_PHONE);
        sAnalyticsTechnologyMap.put(PhoneConstants.PHONE_TYPE_THIRD_PARTY,
                Analytics.THIRD_PARTY_PHONE);
    }

    /**
     * The main call repository. Keeps an instance of all live calls. New incoming and outgoing
     * calls are added to the map and removed when the calls move to the disconnected state.
     *
     * ConcurrentHashMap constructor params: 8 is initial table size, 0.9f is
     * load factor before resizing, 1 means we only expect a single thread to
     * access the map so make only a single shard
     */
    private final Set<Call> mCalls = Collections.newSetFromMap(
            new ConcurrentHashMap<Call, Boolean>(8, 0.9f, 1));

    /**
     * The current telecom call ID.  Used when creating new instances of {@link Call}.  Should
     * only be accessed using the {@link #getNextCallId()} method which synchronizes on the
     * {@link #mLock} sync root.
     */
    private int mCallId = 0;

    /**
     * Stores the current foreground user.
     */
    private UserHandle mCurrentUserHandle = UserHandle.of(ActivityManager.getCurrentUser());

    private final ConnectionServiceRepository mConnectionServiceRepository;
    private final DtmfLocalTonePlayer mDtmfLocalTonePlayer;
    private final InCallController mInCallController;
    private final CallAudioManager mCallAudioManager;
    // prize add by xiarui, PRIZE_LED_BLINK, for flash light control when incoming call 2018-08-03 start
    private PrizeCallFlashLightManager mCallFlashLightManager;
    // prize add by xiarui, PRIZE_LED_BLINK, for flash light control when incoming call 2018-08-03 end
    private RespondViaSmsManager mRespondViaSmsManager;
    private final Ringer mRinger;
    private final InCallWakeLockController mInCallWakeLockController;
    // For this set initial table size to 16 because we add 13 listeners in
    // the CallsManager constructor.
    private final Set<CallsManagerListener> mListeners = Collections.newSetFromMap(
            new ConcurrentHashMap<CallsManagerListener, Boolean>(16, 0.9f, 1));
    private final HeadsetMediaButton mHeadsetMediaButton;
    private final WiredHeadsetManager mWiredHeadsetManager;
    private final BluetoothManager mBluetoothManager;
    private final DockManager mDockManager;
    private final TtyManager mTtyManager;
    private final ProximitySensorManager mProximitySensorManager;
    private final PhoneStateBroadcaster mPhoneStateBroadcaster;
    private final CallLogManager mCallLogManager;
    private final Context mContext;
    private final TelecomSystem.SyncRoot mLock;
    private final ContactsAsyncHelper mContactsAsyncHelper;
    private final CallerInfoAsyncQueryFactory mCallerInfoAsyncQueryFactory;
    private final PhoneAccountRegistrar mPhoneAccountRegistrar;
    private final MissedCallNotifier mMissedCallNotifier;
    private final CallerInfoLookupHelper mCallerInfoLookupHelper;
    private final DefaultDialerManagerAdapter mDefaultDialerManagerAdapter;
    private final Timeouts.Adapter mTimeoutsAdapter;
    private final PhoneNumberUtilsAdapter mPhoneNumberUtilsAdapter;
    private final NotificationManager mNotificationManager;
    private final Set<Call> mLocallyDisconnectingCalls = new HashSet<>();
    private final Set<Call> mPendingCallsToDisconnect = new HashSet<>();
    /* Handler tied to thread in which CallManager was initialized. */
    private final Handler mHandler = new Handler(Looper.getMainLooper());

    private boolean mCanAddCall = true;
    /// M: Added for update screen wake state.@{
    private TelecomUtils mTelecomUtils;
    /// @}

    private TelephonyManager.MultiSimVariants mRadioSimVariants = null;

    private Runnable mStopTone;

    /* added for CDMA India optr*/
    private static final String ACTION_ESN_MO_CALL =
                     "com.android.server.telecom.ESN_OUTGOING_CALL_PLACED";

    /// M: Add to keep toast information. @{
    String mToastInformation;
    /// @}

    /// M: MSMA call control @{
    private final Map<Call, PendingCallAction> mPendingCallActions = new HashMap<>();
    private final List<Call> mSortedInComingCallList = new ArrayList<Call>();
    /// @}

    /// M: [EccDisconnectAll] Add for Ecc disconnect all calls @{
    private Call mPendingEccCall;
    /// @}

    /**
     * Initializes the required Telecom components.
     */
    CallsManager(
            Context context,
            TelecomSystem.SyncRoot lock,
            ContactsAsyncHelper contactsAsyncHelper,
            CallerInfoAsyncQueryFactory callerInfoAsyncQueryFactory,
            MissedCallNotifier missedCallNotifier,
            PhoneAccountRegistrar phoneAccountRegistrar,
            HeadsetMediaButtonFactory headsetMediaButtonFactory,
            ProximitySensorManagerFactory proximitySensorManagerFactory,
            InCallWakeLockControllerFactory inCallWakeLockControllerFactory,
            CallAudioManager.AudioServiceFactory audioServiceFactory,
            BluetoothManager bluetoothManager,
            WiredHeadsetManager wiredHeadsetManager,
            SystemStateProvider systemStateProvider,
            DefaultDialerManagerAdapter defaultDialerAdapter,
            Timeouts.Adapter timeoutsAdapter,
            AsyncRingtonePlayer asyncRingtonePlayer,
            PhoneNumberUtilsAdapter phoneNumberUtilsAdapter,
            InterruptionFilterProxy interruptionFilterProxy) {
        mContext = context;
        mLock = lock;
        mPhoneNumberUtilsAdapter = phoneNumberUtilsAdapter;
        mContactsAsyncHelper = contactsAsyncHelper;
        mCallerInfoAsyncQueryFactory = callerInfoAsyncQueryFactory;
        mPhoneAccountRegistrar = phoneAccountRegistrar;
        mMissedCallNotifier = missedCallNotifier;
        StatusBarNotifier statusBarNotifier = new StatusBarNotifier(context, this);
        mWiredHeadsetManager = wiredHeadsetManager;
        mBluetoothManager = bluetoothManager;
        mDefaultDialerManagerAdapter = defaultDialerAdapter;
        mDockManager = new DockManager(context);
        mTimeoutsAdapter = timeoutsAdapter;
        mCallerInfoLookupHelper = new CallerInfoLookupHelper(context, mCallerInfoAsyncQueryFactory,
                mContactsAsyncHelper, mLock);

        mDtmfLocalTonePlayer = new DtmfLocalTonePlayer();
        mNotificationManager = (NotificationManager) context.getSystemService(
                Context.NOTIFICATION_SERVICE);
        CallAudioRouteStateMachine callAudioRouteStateMachine = new CallAudioRouteStateMachine(
                context,
                this,
                bluetoothManager,
                wiredHeadsetManager,
                statusBarNotifier,
                audioServiceFactory,
                interruptionFilterProxy,
                CallAudioRouteStateMachine.doesDeviceSupportEarpieceRoute()
        );
        callAudioRouteStateMachine.initialize();

        CallAudioRoutePeripheralAdapter callAudioRoutePeripheralAdapter =
                new CallAudioRoutePeripheralAdapter(
                        callAudioRouteStateMachine,
                        bluetoothManager,
                        wiredHeadsetManager,
                        mDockManager);

        InCallTonePlayer.Factory playerFactory = new InCallTonePlayer.Factory(
                callAudioRoutePeripheralAdapter, lock);

        SystemSettingsUtil systemSettingsUtil = new SystemSettingsUtil();
        RingtoneFactory ringtoneFactory = new RingtoneFactory(this, context);
        SystemVibrator systemVibrator = new SystemVibrator(context);
        mInCallController = new InCallController(
                context, mLock, this, systemStateProvider, defaultDialerAdapter, mTimeoutsAdapter);
        mRinger = new Ringer(playerFactory, context, systemSettingsUtil, asyncRingtonePlayer,
                ringtoneFactory, systemVibrator, mInCallController);

        // prize add by xiarui, PRIZE_LED_BLINK, for flash light control when incoming call 2018-08-03 start
        //mCallAudioManager = new CallAudioManager(callAudioRouteStateMachine,
        //        this,new CallAudioModeStateMachine((AudioManager)
        //                mContext.getSystemService(Context.AUDIO_SERVICE)),
        //        playerFactory, mRinger, new RingbackPlayer(playerFactory), mDtmfLocalTonePlayer);
        CallAudioModeStateMachine callAudioModeStateMachine = new CallAudioModeStateMachine((AudioManager)
                mContext.getSystemService(Context.AUDIO_SERVICE));

        mCallAudioManager = new CallAudioManager(callAudioRouteStateMachine,
                this, callAudioModeStateMachine,
                playerFactory, mRinger, new RingbackPlayer(playerFactory), mDtmfLocalTonePlayer);

        if (PrizeOption.PRIZE_LED_BLINK) {
            mCallFlashLightManager = new PrizeCallFlashLightManager(context, callAudioModeStateMachine);
        }
        // prize add by xiarui, PRIZE_LED_BLINK, for flash light control when incoming call 2018-08-03 end

        mHeadsetMediaButton = headsetMediaButtonFactory.create(context, this, mLock);
        mTtyManager = new TtyManager(context, mWiredHeadsetManager);
        mProximitySensorManager = proximitySensorManagerFactory.create(context, this);
        mPhoneStateBroadcaster = new PhoneStateBroadcaster(this);
        mCallLogManager = new CallLogManager(context, phoneAccountRegistrar, mMissedCallNotifier);
        mConnectionServiceRepository =
                new ConnectionServiceRepository(mPhoneAccountRegistrar, mContext, mLock, this);
        mInCallWakeLockController = inCallWakeLockControllerFactory.create(context, this);

        mListeners.add(mInCallWakeLockController);
        mListeners.add(statusBarNotifier);
        mListeners.add(mCallLogManager);
        mListeners.add(mPhoneStateBroadcaster);
        mListeners.add(mInCallController);
        mListeners.add(mCallAudioManager);
        mListeners.add(missedCallNotifier);
        mListeners.add(mHeadsetMediaButton);
        mListeners.add(mProximitySensorManager);
        // prize add by xiarui, PRIZE_LED_BLINK, for flash light control when incoming call 2018-08-03 start
        if (PrizeOption.PRIZE_LED_BLINK) {
            mListeners.add(mCallFlashLightManager);
        }
        // prize add by xiarui, PRIZE_LED_BLINK, for flash light control when incoming call 2018-08-03 end
        ///M: add listener for phone recording
        mListeners.add(PhoneRecorderHandler.getInstance());

        // There is no USER_SWITCHED broadcast for user 0, handle it here explicitly.
        final UserManager userManager = UserManager.get(mContext);
        // Don't load missed call if it is run in split user model.
        if (userManager.isPrimaryUser()) {
            onUserSwitch(Process.myUserHandle());
        }
    }

    public void setRespondViaSmsManager(RespondViaSmsManager respondViaSmsManager) {
        if (mRespondViaSmsManager != null) {
            mListeners.remove(mRespondViaSmsManager);
        }
        mRespondViaSmsManager = respondViaSmsManager;
        mListeners.add(respondViaSmsManager);
    }

    public RespondViaSmsManager getRespondViaSmsManager() {
        return mRespondViaSmsManager;
    }

    public CallerInfoLookupHelper getCallerInfoLookupHelper() {
        return mCallerInfoLookupHelper;
    }

    @Override
    public void onSuccessfulOutgoingCall(Call call, int callState) {
        Log.v(this, "onSuccessfulOutgoingCall, %s", call);

        setCallState(call, callState, "successful outgoing call");
        if (!mCalls.contains(call)) {
            // Call was not added previously in startOutgoingCall due to it being a potential MMI
            // code, so add it now.
            addCall(call);
        }

        // The call's ConnectionService has been updated.
        for (CallsManagerListener listener : mListeners) {
            listener.onConnectionServiceChanged(call, null, call.getConnectionService());
        }

        // ALPS01781841, do not mark Ecc call as dialing state this time point
        // Ecc call is marked as dialing state only when FWK call state event(MSG_SET_DIALING) post
        // to ConnectionServiceWrapper.Adapter
        if (!call.isEmergencyCall()) {
            markCallAsDialing(call);
        }
    }

    @Override
    public void onFailedOutgoingCall(Call call, DisconnectCause disconnectCause) {
        Log.v(this, "onFailedOutgoingCall, call: %s", call);

        markCallAsRemoved(call);
    }

    @Override
    public void onSuccessfulIncomingCall(Call incomingCall) {
        Log.d(this, "onSuccessfulIncomingCall");
        if (incomingCall.hasProperty(Connection.PROPERTY_EMERGENCY_CALLBACK_MODE)) {
            Log.i(this, "Skipping call filtering due to ECBM");
            onCallFilteringComplete(incomingCall, new CallFilteringResult(true, false, true, true));
            return;
        }

        List<IncomingCallFilter.CallFilter> filters = new ArrayList<>();
        filters.add(new DirectToVoicemailCallFilter(mCallerInfoLookupHelper));
        filters.add(new AsyncBlockCheckFilter(mContext, new BlockCheckerAdapter()));
        filters.add(new CallScreeningServiceFilter(mContext, this, mPhoneAccountRegistrar,
                mDefaultDialerManagerAdapter,
                new ParcelableCallUtils.Converter(), mLock));
        new IncomingCallFilter(mContext, this, incomingCall, mLock,
                mTimeoutsAdapter, filters).performFiltering();
    }

    @Override
    public void onCallFilteringComplete(Call incomingCall, CallFilteringResult result) {
        // Only set the incoming call as ringing if it isn't already disconnected. It is possible
        // that the connection service disconnected the call before it was even added to Telecom, in
        // which case it makes no sense to set it back to a ringing state.
        if (incomingCall.getState() != CallState.DISCONNECTED &&
                incomingCall.getState() != CallState.DISCONNECTING) {
            setCallState(incomingCall, CallState.RINGING,
                    result.shouldAllowCall ? "successful incoming call" : "blocking call");
        } else {
            Log.i(this, "onCallFilteringCompleted: call already disconnected.");
            return;
        }

        if (result.shouldAllowCall) {
            if (hasMaximumRingingCalls()) {
                Log.i(this, "onCallFilteringCompleted: Call rejected! Exceeds maximum number of " +
                        "ringing calls.");
                rejectCallAndLog(incomingCall);
            /**
             * M: [CTA] Don't reject incoming call while dialing.
             * Requirements comes from CTA cases(6.2.2.3.5) that should allow incoming call while
             * dialing for DSDA phone. We add a flow to guarantee there no conflict for co-exist
             * incoming call and dialing call. (If user accept incoming call, the dialing one would
             * be disconnected)
            } else if (hasMaximumDialingCalls()) {
                Log.i(this, "onCallFilteringCompleted: Call rejected! Exceeds maximum number of " +
                        "dialing calls.");
                rejectCallAndLog(incomingCall);
             */
            // M: fix Cr:ALPS03263825,when exist ECC,incoming call arrive,will not reject
            // incoming call.
            // } else if (hasEmergencyCall()) {
            //    Log.i(this, "onCallScreeningCompleted: Call rejected! has emergency call");
            //     rejectCallAndLog(incomingCall);
            } else if (shouldBlockFor3GVT(incomingCall.isVideoCall())) {
                Log.i(this, "onCallScreeningCompleted: Call rejected! Should block for 3G VT");
                rejectCallAndLog(incomingCall);
            } else {
                ///M: ALPS02817676 @{
                // there has timing issue, MT call has disconnected before filtering complete
                // finally an orphan call will always in Telecom
                // so do not add call if MT call has disconnected
                if (incomingCall.getState() == CallState.DISCONNECTED) {
                    Log.d(this, "incoming call has disconncted, do not add it");
                    return;
                }
                /// @}
                addCall(incomingCall);
            }
        } else {
            if (result.shouldReject) {
                Log.i(this, "onCallFilteringCompleted: blocked call, rejecting.");
                incomingCall.reject(false, null);
            }
            if (result.shouldAddToCallLog) {
                Log.i(this, "onCallScreeningCompleted: blocked call, adding to call log.");
                if (result.shouldShowNotification) {
                    Log.w(this, "onCallScreeningCompleted: blocked call, showing notification.");
                }
                mCallLogManager.logCall(incomingCall, Calls.MISSED_TYPE,
                        result.shouldShowNotification);
            } else if (result.shouldShowNotification) {
                Log.i(this, "onCallScreeningCompleted: blocked call, showing notification.");
                mMissedCallNotifier.showMissedCallNotification(incomingCall);
            }
        }
    }

    @Override
    public void onFailedIncomingCall(Call call) {
        setCallState(call, CallState.DISCONNECTED, "failed incoming call");
        call.removeListener(this);
    }

    @Override
    public void onSuccessfulUnknownCall(Call call, int callState) {
        setCallState(call, callState, "successful unknown call");
        Log.d(this, "onSuccessfulUnknownCall for call %s", call);
        addCall(call);
    }

    @Override
    public void onFailedUnknownCall(Call call) {
        Log.d(this, "onFailedUnknownCall for call %s", call);
        setCallState(call, CallState.DISCONNECTED, "failed unknown call");
        call.removeListener(this);
    }

    @Override
    public void onRingbackRequested(Call call, boolean ringback) {
        for (CallsManagerListener listener : mListeners) {
            listener.onRingbackRequested(call, ringback);
        }
    }

    @Override
    public void onPostDialWait(Call call, String remaining) {
        mInCallController.onPostDialWait(call, remaining);
    }

    @Override
    public void onPostDialChar(final Call call, char nextChar) {
        if (PhoneNumberUtils.is12Key(nextChar)) {
            // Play tone if it is one of the dialpad digits, canceling out the previously queued
            // up stopTone runnable since playing a new tone automatically stops the previous tone.
            if (mStopTone != null) {
                mHandler.removeCallbacks(mStopTone.getRunnableToCancel());
                ///M: to avoid deadlock, using method which allowing logs' synchronized error
                mStopTone.cancelAllowingSynchronizedError();
            }

            mDtmfLocalTonePlayer.playTone(call, nextChar);

            mStopTone = new Runnable("CM.oPDC", mLock) {
                @Override
                public void loggedRun() {
                    // Set a timeout to stop the tone in case there isn't another tone to
                    // follow.
                    mDtmfLocalTonePlayer.stopTone(call);
                }
            };
            mHandler.postDelayed(mStopTone.prepare(),
                    Timeouts.getDelayBetweenDtmfTonesMillis(mContext.getContentResolver()));
        } else if (nextChar == 0 || nextChar == TelecomManager.DTMF_CHARACTER_WAIT ||
                nextChar == TelecomManager.DTMF_CHARACTER_PAUSE) {
            // Stop the tone if a tone is playing, removing any other stopTone callbacks since
            // the previous tone is being stopped anyway.
            if (mStopTone != null) {
                mHandler.removeCallbacks(mStopTone.getRunnableToCancel());
                mStopTone.cancel();
            }
            mDtmfLocalTonePlayer.stopTone(call);
        } else {
            Log.w(this, "onPostDialChar: invalid value %d", nextChar);
        }
    }

    @Override
    public void onParentChanged(Call call) {
        // parent-child relationship affects which call should be foreground, so do an update.
        updateCanAddCall();
        for (CallsManagerListener listener : mListeners) {
            listener.onIsConferencedChanged(call);
        }
    }

    @Override
    public void onChildrenChanged(Call call) {
        // parent-child relationship affects which call should be foreground, so do an update.
        updateCanAddCall();
        for (CallsManagerListener listener : mListeners) {
            listener.onIsConferencedChanged(call);
        }
    }

    @Override
    public void onIsVoipAudioModeChanged(Call call) {
        for (CallsManagerListener listener : mListeners) {
            listener.onIsVoipAudioModeChanged(call);
        }
    }

    @Override
    public void onVideoStateChanged(Call call, int previousVideoState, int newVideoState) {
        for (CallsManagerListener listener : mListeners) {
            listener.onVideoStateChanged(call, previousVideoState, newVideoState);
        }
    }

    @Override
    public boolean onCanceledViaNewOutgoingCallBroadcast(final Call call) {
        mPendingCallsToDisconnect.add(call);
        mHandler.postDelayed(new Runnable("CM.oCVNOCB", mLock) {
            @Override
            public void loggedRun() {
                if (mPendingCallsToDisconnect.remove(call)) {
                    Log.i(this, "Delayed disconnection of call: %s", call);
                  /// M: [log optimize]
                    Log.disconnectEvent(call,
                            "CallsManager.onCanceledViaNewOutgoingCallBroadcast");
                    call.disconnect();
                }
            }
        }.prepare(), Timeouts.getNewOutgoingCallCancelMillis(mContext.getContentResolver()));

        return true;
    }

    /**
     * Handles changes to the {@link Connection.VideoProvider} for a call.  Adds the
     * {@link CallsManager} as a listener for the {@link VideoProviderProxy} which is created
     * in {@link Call#setVideoProvider(IVideoProvider)}.  This allows the {@link CallsManager} to
     * respond to callbacks from the {@link VideoProviderProxy}.
     *
     * @param call The call.
     */
    @Override
    public void onVideoCallProviderChanged(Call call) {
        VideoProviderProxy videoProviderProxy = call.getVideoProviderProxy();

        if (videoProviderProxy == null) {
            return;
        }

        videoProviderProxy.addListener(this);
    }

    /**
     * Handles session modification requests received via the {@link TelecomVideoCallCallback} for
     * a call.  Notifies listeners of the {@link CallsManager.CallsManagerListener} of the session
     * modification request.
     *
     * @param call The call.
     * @param videoProfile The {@link VideoProfile}.
     */
    @Override
    public void onSessionModifyRequestReceived(Call call, VideoProfile videoProfile) {
        int videoState = videoProfile != null ? videoProfile.getVideoState() :
                VideoProfile.STATE_AUDIO_ONLY;
        Log.v(TAG, "onSessionModifyRequestReceived : videoProfile = " + VideoProfile
                .videoStateToString(videoState));

        for (CallsManagerListener listener : mListeners) {
            listener.onSessionModifyRequestReceived(call, videoProfile);
        }
    }

    public Collection<Call> getCalls() {
        return Collections.unmodifiableCollection(mCalls);
    }

    /**
     * Play or stop a call hold tone for a call.  Triggered via
     * {@link Connection#sendConnectionEvent(String)} when the
     * {@link Connection#EVENT_ON_HOLD_TONE_START} event or
     * {@link Connection#EVENT_ON_HOLD_TONE_STOP} event is passed through to the
     *
     * @param call The call which requested the hold tone.
     */
    @Override
    public void onHoldToneRequested(Call call) {
        for (CallsManagerListener listener : mListeners) {
            listener.onHoldToneRequested(call);
        }
    }

    @VisibleForTesting
    public Call getForegroundCall() {
        if (mCallAudioManager == null) {
            // Happens when getForegroundCall is called before full initialization.
            return null;
        }
        return mCallAudioManager.getForegroundCall();
    }

    public UserHandle getCurrentUserHandle() {
        return mCurrentUserHandle;
    }

    public CallAudioManager getCallAudioManager() {
        return mCallAudioManager;
    }

    InCallController getInCallController() {
        return mInCallController;
    }

    @VisibleForTesting
    public boolean hasEmergencyCall() {
        for (Call call : mCalls) {
            if (call.isEmergencyCall()) {
                return true;
            }
        }
        return false;
    }

    /**
     * use to check if current mCalls contains a emergency call,
     * need to exclude the current outgoing cemergency call.
     * @param onGoingcall
     * @return
     */
    private boolean hasOtherEmergencyCall(Call onGoingcall) {
        for (Call call : mCalls) {
            if (Objects.equals(onGoingcall, call)) {
                continue;
            }
            if (call.isEmergencyCall()) {
                return true;
            }
        }
        return false;
    }

    boolean hasOnlyDisconnectedCalls() {
        for (Call call : mCalls) {
            if (!call.isDisconnected()) {
                return false;
            }
        }
        return true;
    }

    boolean hasVideoCall() {
        for (Call call : mCalls) {
            if (VideoProfile.isVideo(call.getVideoState())) {
                return true;
            }
        }
        return false;
    }

    @VisibleForTesting
    public CallAudioState getAudioState() {
        return mCallAudioManager.getCallAudioState();
    }

    boolean isTtySupported() {
        return mTtyManager.isTtySupported();
    }

    int getCurrentTtyMode() {
        return mTtyManager.getCurrentTtyMode();
    }

    @VisibleForTesting
    public void addListener(CallsManagerListener listener) {
        mListeners.add(listener);
    }

    void removeListener(CallsManagerListener listener) {
        mListeners.remove(listener);
    }

    /**
     * Starts the process to attach the call to a connection service.
     *
     * @param phoneAccountHandle The phone account which contains the component name of the
     *        connection service to use for this call.
     * @param extras The optional extras Bundle passed with the intent used for the incoming call.
     */
    void processIncomingCallIntent(PhoneAccountHandle phoneAccountHandle, Bundle extras) {
        Log.d(this, "processIncomingCallIntent");
        Uri handle = extras.getParcelable(TelecomManager.EXTRA_INCOMING_CALL_ADDRESS);
        if (handle == null) {
            // Required for backwards compatibility
            handle = extras.getParcelable(TelephonyManager.EXTRA_INCOMING_NUMBER);
        }
        Call call = new Call(
                getNextCallId(),
                mContext,
                this,
                mLock,
                mConnectionServiceRepository,
                mContactsAsyncHelper,
                mCallerInfoAsyncQueryFactory,
                mPhoneNumberUtilsAdapter,
                handle,
                null /* gatewayInfo */,
                null /* connectionManagerPhoneAccount */,
                phoneAccountHandle,
                Call.CALL_DIRECTION_INCOMING /* callDirection */,
                false /* forceAttachToExistingConnection */,
                false /* isConference */
        );

        call.initAnalytics();
        if (getForegroundCall() != null) {
            getForegroundCall().getAnalytics().setCallIsInterrupted(true);
            call.getAnalytics().setCallIsAdditional(true);
        }

        setIntentExtrasAndStartTime(call, extras);
        /// M: For VoLTE @{
        if (TelecomVolteUtils.isConferenceInvite(extras)) {
            call.setIsIncomingFromConfServer(true);
        }
        /// @}

        // TODO: Move this to be a part of addCall()
        call.addListener(this);
        call.startCreateConnection(mPhoneAccountRegistrar);
    }

    void addNewUnknownCall(PhoneAccountHandle phoneAccountHandle, Bundle extras) {
        Uri handle = extras.getParcelable(TelecomManager.EXTRA_UNKNOWN_CALL_HANDLE);
        Log.d(this, "addNewUnknownCall with handle: %s", Log.pii(handle));
        Call call = new Call(
                getNextCallId(),
                mContext,
                this,
                mLock,
                mConnectionServiceRepository,
                mContactsAsyncHelper,
                mCallerInfoAsyncQueryFactory,
                mPhoneNumberUtilsAdapter,
                handle,
                null /* gatewayInfo */,
                null /* connectionManagerPhoneAccount */,
                phoneAccountHandle,
                Call.CALL_DIRECTION_UNKNOWN /* callDirection */,
                // Use onCreateIncomingConnection in TelephonyConnectionService, so that we attach
                // to the existing connection instead of trying to create a new one.
                true /* forceAttachToExistingConnection */,
                false /* isConference */
        );
        call.initAnalytics();

        setIntentExtrasAndStartTime(call, extras);
        call.addListener(this);
        call.startCreateConnection(mPhoneAccountRegistrar);
    }

    private boolean areHandlesEqual(Uri handle1, Uri handle2) {
        if (handle1 == null || handle2 == null) {
            return handle1 == handle2;
        }

        if (!TextUtils.equals(handle1.getScheme(), handle2.getScheme())) {
            return false;
        }

        final String number1 = PhoneNumberUtils.normalizeNumber(handle1.getSchemeSpecificPart());
        final String number2 = PhoneNumberUtils.normalizeNumber(handle2.getSchemeSpecificPart());
        return TextUtils.equals(number1, number2);
    }

    private Call reuseOutgoingCall(Uri handle) {
        // Check to see if we can reuse any of the calls that are waiting to disconnect.
        // See {@link Call#abort} and {@link #onCanceledViaNewOutgoingCall} for more information.
        Call reusedCall = null;
        for (Iterator<Call> callIter = mPendingCallsToDisconnect.iterator(); callIter.hasNext();) {
            Call pendingCall = callIter.next();
            if (reusedCall == null && areHandlesEqual(pendingCall.getHandle(), handle)) {
                callIter.remove();
                Log.i(this, "Reusing disconnected call %s", pendingCall);
                reusedCall = pendingCall;
            } else {
                Log.d(this, "Not reusing disconnected call %s", pendingCall);
                /// M: [log optimize]
                Log.disconnectEvent(pendingCall, "CallsManager.reuseOutgoingCall: " +
                        "disconnect pendingCall");
                /// M: Since AOSP will disconnect the pending call here,
                /// we should remove it from the pending call list to prevent to reuse
                /// the call in future. Otherwise, if the pending call has been disconnected
                /// before, this pendingCall.disconnect will cause the value of
                /// mIsLocallyDisconnecting abnormal because there won't be state changes when
                /// Call.setState is called and mIsLocallyDisconnecting cannot be reset
                /// to false. If this call object is reused in futher, the state of the call
                /// cannnot be updated right to IncallService because getParcelableState of
                /// ParcelableCallUtils will convert all states exception disconnected to
                /// disconnecting if mIsLocallyDisconnecting is true. @{
                callIter.remove();
                /// @}
                pendingCall.disconnect();
            }
        }

        ///M: the reused call isn't an accepted call before dialing out.
        if (reusedCall != null) {
            reusedCall.setIsAcceptedCdmaMoCall(false);
        }

        return reusedCall;
    }

    /**
     * Kicks off the first steps to creating an outgoing call so that InCallUI can launch.
     *
     * @param handle Handle to connect the call with.
     * @param phoneAccountHandle The phone account which contains the component name of the
     *        connection service to use for this call.
     * @param extras The optional extras Bundle passed with the intent used for the incoming call.
     * @param initiatingUser {@link UserHandle} of user that place the outgoing call.
     */
    Call startOutgoingCall(Uri handle, PhoneAccountHandle phoneAccountHandle, Bundle extras,
            UserHandle initiatingUser) {
        /// M: OP07 Plugin @{
        if(ExtensionManager.getCallMgrExt().blockOutgoingCall(handle,
                    phoneAccountHandle, extras)){
            Log.w(this, "[startOutgoingCall] blockOutgoingCall as it is emergency num on roaming");
            return null;
        }
        boolean isReusedCall = true;
        Call call = reuseOutgoingCall(handle);

        // Create a call with original handle. The handle may be changed when the call is attached
        // to a connection service, but in most cases will remain the same.
        if (call == null) {
            call = new Call(getNextCallId(), mContext,
                    this,
                    mLock,
                    mConnectionServiceRepository,
                    mContactsAsyncHelper,
                    mCallerInfoAsyncQueryFactory,
                    mPhoneNumberUtilsAdapter,
                    handle,
                    null /* gatewayInfo */,
                    null /* connectionManagerPhoneAccount */,
                    null /* phoneAccountHandle */,
                    Call.CALL_DIRECTION_OUTGOING /* callDirection */,
                    false /* forceAttachToExistingConnection */,
                    false /* isConference */
            );
            call.initAnalytics();

            call.setInitiatingUser(initiatingUser);

            isReusedCall = false;
        }

        // Set the video state on the call early so that when it is added to the InCall UI the UI
        // knows to configure itself as a video call immediately.
        if (extras != null) {
            int videoState = extras.getInt(TelecomManager.EXTRA_START_CALL_WITH_VIDEO_STATE,
                    VideoProfile.STATE_AUDIO_ONLY);

            // If this is an emergency video call, we need to check if the phone account supports
            // emergency video calling.
            // Also, ensure we don't try to place an outgoing call with video if video is not
            // supported.
            if (VideoProfile.isVideo(videoState)) {
                PhoneAccount account =
                        mPhoneAccountRegistrar.getPhoneAccount(phoneAccountHandle, initiatingUser);

                if (call.isEmergencyCall() && account != null &&
                        !account.hasCapabilities(PhoneAccount.CAPABILITY_EMERGENCY_VIDEO_CALLING)) {
                    // Phone account doesn't support emergency video calling, so fallback to
                    // audio-only now to prevent the InCall UI from setting up video surfaces
                    // needlessly.
                    Log.i(this, "startOutgoingCall - emergency video calls not supported; " +
                            "falling back to audio-only");
                    videoState = VideoProfile.STATE_AUDIO_ONLY;
                } else if (account != null &&
                        !account.hasCapabilities(PhoneAccount.CAPABILITY_VIDEO_CALLING)) {
                    // Phone account doesn't support video calling, so fallback to audio-only.
                    Log.i(this, "startOutgoingCall - video calls not supported; fallback to " +
                            "audio-only.");
                    videoState = VideoProfile.STATE_AUDIO_ONLY;
                }
            }

            call.setVideoState(videoState);
        }

        List<PhoneAccountHandle> accounts = constructPossiblePhoneAccounts(handle, initiatingUser);
        Log.v(this, "startOutgoingCall found accounts = " + accounts);

        /// M: For InCallMMI, use use foreground call's account. @{
        if (mCallAudioManager.getPossiblyHeldForegroundCall() != null
                && isPotentialInCallMMICode(handle)) {
            Call ongoingCall = mCallAudioManager.getPossiblyHeldForegroundCall();
            // If there is an ongoing call, use the same phone account to place this new call.
            // If the ongoing call is a conference call, we fetch the phone account from the
            // child calls because we don't have targetPhoneAccount set on Conference calls.
            // TODO: Set targetPhoneAccount for all conference calls (b/23035408).
            if (ongoingCall.getTargetPhoneAccount() == null &&
                    !ongoingCall.getChildCalls().isEmpty()) {
                ongoingCall = ongoingCall.getChildCalls().get(0);
            }
            if (ongoingCall.getTargetPhoneAccount() != null) {
                phoneAccountHandle = ongoingCall.getTargetPhoneAccount();
            }
        }
        /// @}

        // Only dial with the requested phoneAccount if it is still valid. Otherwise treat this call
        // as if a phoneAccount was not specified (does the default behavior instead).
        // Note: We will not attempt to dial with a requested phoneAccount if it is disabled.
        if (phoneAccountHandle != null) {
            if (!accounts.contains(phoneAccountHandle)) {
                /**
                 * M: [ALPS02821524]Per google design, if a user-specified account not in the list
                 * of current available accounts, just ignore the specified one and choose another
                 * in the following startOutgoingCall() steps.
                 * We don't think it wisdom to make the choice for user.
                 * In some cases, like ALPS02821524, the call will be accidentally made via a
                 * unexpected SIM.
                 * So we decide to abort the call if the specified account wasn't valid at the
                 * moment.
                 * [ALPS02834214]We treat emergency call different For OP09. The ECC will
                 * go through the previous AOSP way to drop the specified account.
                 * Because OP09 has a feature to show multiple Dial icon in Dialpad, user can
                 * choose any of them to dial calls including Emergency calls.
                 * We shouldn't abort the call here if it was an ECC.
                 * @{
                 */
                if (!call.isEmergencyCall()) {
                    showToastInfomation(mContext.getResources()
                            .getString(R.string.outgoing_call_failed));
                    if (mCalls.contains(call)) {
                        // This call can already exist if it is a reused call,
                        // See {@link #reuseOutgoingCall}.
                        /// M: [log optimize]
                        Log.disconnectEvent(call, "CallsManager.startOutgoingCall: " +
                                "Specified PhoneAccount not valid: " + phoneAccountHandle);
                        call.disconnect();
                    }
                    return null;
                }
                /** @} */
                phoneAccountHandle = null;
            }
        }

        if (phoneAccountHandle == null && accounts.size() > 0) {
            // No preset account, check if default exists that supports the URI scheme for the
            // handle and verify it can be used.
            if(accounts.size() > 1) {
                PhoneAccountHandle defaultPhoneAccountHandle =
                        mPhoneAccountRegistrar.getOutgoingPhoneAccountForScheme(handle.getScheme(),
                                initiatingUser);
                if (defaultPhoneAccountHandle != null &&
                        accounts.contains(defaultPhoneAccountHandle)) {
                    phoneAccountHandle = defaultPhoneAccountHandle;
                }
            } else {
                // Use the only PhoneAccount that is available
                phoneAccountHandle = accounts.get(0);
            }
            /// M: For OP09 plug-in, ignoring default phone account @{
            if (!ExtensionManager.getPhoneAccountExt().shouldRemoveDefaultPhoneAccount(accounts)) {
                Log.i(this, "MO - OP09 case: account changed: %s => null", phoneAccountHandle);
                phoneAccountHandle = null;
            }
            /// @}
        }

        /// M: For Ip dial @{
        if (TelecomUtils.isIpDialRequest(mContext, call, extras)) {
            call.setIsIpCall(true);
            accounts = mPhoneAccountRegistrar.getSimPhoneAccountsOfCurrentUser();
            phoneAccountHandle = TelecomUtils.getPhoneAccountForIpDial(accounts,
                    phoneAccountHandle);
            Log.i(this, "MO - Ip dial case: account / accounts changed to be : %s / %s",
                    phoneAccountHandle, accounts);
        }
        /// @}

        /*PRIZE-Delete-DialerV8-wangzhong-2017_7_19-start*/
        /// M: Added for suggesting phone account feature. @{
        // When suggested PhoneAccountHandle is not same with defaultAccountHandle, need let user
        // to pick. @{
        /*if (TelecomUtils.shouldShowAccountSuggestion(extras, accounts, phoneAccountHandle)) {
            Log.i(this, "MO - Sugguest case: account changed: %s => null", phoneAccountHandle);
            phoneAccountHandle = null;
        }*/
        /// @}
        /*PRIZE-Delete-DialerV8-wangzhong-2017_7_19-end*/

        /// M: For VoLTE @{
        // Case of no VoLTE phoneAccount has been handled in processOutgoingCall().
        // For now, only one account supports VoLTE.
        boolean isImsCallRequest = (extras != null
                && extras.getBoolean(TelecomVolteUtils.EXTRA_VOLTE_IMS_CALL, false));
        boolean isConferenceDialRequest = TelecomVolteUtils.isConferenceDialRequest(extras);
        if (isImsCallRequest || isConferenceDialRequest) {
            accounts = mPhoneAccountRegistrar.getVolteCallCapablePhoneAccounts();
            phoneAccountHandle = TelecomVolteUtils.getPhoneAccountForVoLTE(accounts,
                    phoneAccountHandle);
            Log.i(this, "MO - VoLTE case: account / accounts changed to be : %s / %s",
                    phoneAccountHandle, accounts);
            if (isConferenceDialRequest) {
                call.setIsConferenceDial(true);
                call.setConferenceParticipants(TelecomVolteUtils.getConferenceDialNumbers(extras));
            }
        }
        /// @}

        call.setTargetPhoneAccount(phoneAccountHandle);

        boolean isPotentialInCallMMICode = isPotentialInCallMMICode(handle);

        // Do not support any more live calls.  Our options are to move a call to hold, disconnect
        // a call, or cancel this call altogether. If a call is being reused, then it has already
        // passed the makeRoomForOutgoingCall check once and will fail the second time due to the
        // call transitioning into the CONNECTING state.
        if (!isReusedCall && !makeRoomForOutgoingCall(call, call.isEmergencyCall())) {
            // just cancel at this point.
            Log.d(this, "No remaining room for outgoing call: %s", call);
            showToastInfomation(mContext.getResources()
                    .getString(R.string.outgoing_call_failed));
            if (mCalls.contains(call)) {
                // This call can already exist if it is a reused call,
                // See {@link #reuseOutgoingCall}.
                call.disconnect();
            }
            return null;
        }

        boolean needsAccountSelection = phoneAccountHandle == null && accounts.size() > 1 &&
                !call.isEmergencyCall();

        Log.i(this, "MO - dump: needSelect = %s; phoneAccountHandle = %s; accounts.size() = %s.",
                needsAccountSelection, phoneAccountHandle, accounts.size());

        /// M: For Ip dial @{
        // If we do have a sim phoneAccountHandle here, check Ip-prefix first.
        if (!TelecomUtils.handleIpPrefixForCall(mContext, call)) {
            /// M: [log optimize]
            Log.disconnectEvent(call, "CallsManager.startOutgoingCall: failed to add IP prefix");
            disconnectCall(call);
            return null;
        };
        /// @}

        if (needsAccountSelection) {
            // This is the state where the user is expected to select an account
            call.setState(CallState.SELECT_PHONE_ACCOUNT, "needs account selection");
            // Create our own instance to modify (since extras may be Bundle.EMPTY)
            extras = new Bundle(extras);
            extras.putParcelableList(android.telecom.Call.AVAILABLE_PHONE_ACCOUNTS, accounts);
        } else {
            call.setState(
                    CallState.CONNECTING,
                    phoneAccountHandle == null ? "no-handle" : phoneAccountHandle.toString());
        }

        setIntentExtrasAndStartTime(call, extras);

        // Do not add the call if it is a potential MMI code.
        // M:fix CR:ALPS02853331,telephony show toast and incallui show dialog in mmi.
        if ((isPotentialMMICode(handle) || isPotentialInCallMMICode)
                && TelecomUtils.isSupportMMICode(call.getTargetPhoneAccount())
                && !needsAccountSelection) {
            call.addListener(this);
            /// M: If no account for MMI Code, show a dialog with "No SIM or SIM error" message. @{
            if (phoneAccountHandle == null) {
                Log.d(this, "MO - MMI with no sim: show error dialog and return");
                TelecomUtils.showErrorDialog(mContext, R.string.callFailed_simError);
                disconnectCall(call);
                return null;
            }
            /// @}
        } else if (!mCalls.contains(call)) {
            // We check if mCalls already contains the call because we could potentially be reusing
            // a call which was previously added (See {@link #reuseOutgoingCall}).
            addCall(call);
        }

        return call;
    }

    ///M: [EccDisconnectAll] can dial Ecc when no call exists.
    private boolean isOkForECC(Call ecc) {
        return mCalls.size() == 0 || (mCalls.size() == 1 && mCalls.contains(ecc)) ? true : false;
    }
    /// @}

    /**
     * Attempts to issue/connect the specified call.
     *
     * @param handle Handle to connect the call with.
     * @param gatewayInfo Optional gateway information that can be used to route the call to the
     *        actual dialed handle via a gateway provider. May be null.
     * @param speakerphoneOn Whether or not to turn the speakerphone on once the call connects.
     * @param videoState The desired video state for the outgoing call.
     */
    @VisibleForTesting
    public void placeOutgoingCall(Call call, Uri handle, GatewayInfo gatewayInfo,
            boolean speakerphoneOn, int videoState) {
        if (call == null) {
            // don't do anything if the call no longer exists
            Log.i(this, "Canceling unknown call.");
            return;
        }
        /// M: For CDMA India optr @{
        broadcastCallPlacedIntent(call);
        /// @}

        final Uri uriHandle = (gatewayInfo == null) ? handle : gatewayInfo.getGatewayAddress();

        if (gatewayInfo == null) {
            Log.d(this, "Creating a new outgoing call with handle: %s", Log.piiHandle(uriHandle));
        } else {
            Log.d(this, "Creating a new outgoing call with gateway handle: %s, original handle: %s",
                    Log.pii(uriHandle), Log.pii(handle));
        }

        call.setHandle(uriHandle);
        call.setGatewayInfo(gatewayInfo);

        final boolean useSpeakerWhenDocked = mContext.getResources().getBoolean(
                R.bool.use_speaker_when_docked);

        final boolean useSpeakerForDock = isSpeakerphoneEnabledForDock();
        final boolean useSpeakerForVideoCall = isSpeakerphoneAutoEnabledForVideoCalls(videoState);

        // Auto-enable speakerphone if the originating intent specified to do so, if the call
        // is a video call, of if using speaker when docked
        call.setStartWithSpeakerphoneOn(speakerphoneOn || useSpeakerForVideoCall
                || (useSpeakerWhenDocked && useSpeakerForDock));
        call.setVideoState(videoState);

        if (speakerphoneOn) {
            Log.i(this, "%s Starting with speakerphone as requested", call);
        } else if (useSpeakerWhenDocked && useSpeakerForDock) {
            Log.i(this, "%s Starting with speakerphone because car is docked.", call);
        } else if (useSpeakerForVideoCall) {
            Log.i(this, "%s Starting with speakerphone because its a video call.", call);
        } else {
            Log.d(this, "%s Starting with speakerphone because car is docked.", call);
        }

        if (neededForceSpeakerOn()) {
            call.setStartWithSpeakerphoneOn(true);
        } else if (VideoProfile.isVideo(videoState) &&
            !mWiredHeadsetManager.isPluggedIn() &&
            !mBluetoothManager.isBluetoothAvailable() &&
            isSpeakerEnabledForVideoCalls()) {
            Log.i(this, "Starting with speakerphone because of video call");
            call.setStartWithSpeakerphoneOn(true);
        } else {
            call.setStartWithSpeakerphoneOn(speakerphoneOn || mDockManager.isDocked());
        }

        if (call.isEmergencyCall()) {
            new AsyncEmergencyContactNotifier(mContext).execute();

            ///M: [EccDisconnectAll] Add for Ecc disconnect all calls @{
            if (ExtensionManager.getCallMgrExt().shouldDisconnectCallsWhenEcc()) {
                if (!isOkForECC(call)) {
                    Log.i(this, "placeOutgoingCall now is not ok for ECC, waiting ......");
                    mCalls.remove(call); //remove the Ecc itself before disconnect all
                    mPendingEccCall = call;
                    disconnectAllCalls();
                    if (mPendingEccCall != null) {
                        mCalls.add(mPendingEccCall);
                    }
                    return; //do not place Ecc call when it's not okey for Ecc
                }
            }
            ///@}
        }

        /// M: ALPS02035599 Since NewOutgoingCallIntentBroadcaster and the SELECT_PHONE_ACCOUNT @{
        // sequence run in parallel, this call may be already disconnected in the  select phone
        // account sequence.
        if (call.getState() == CallState.DISCONNECTED) {
            return;
        }
        /// @}

        final boolean requireCallCapableAccountByHandle = mContext.getResources().getBoolean(
                com.android.internal.R.bool.config_requireCallCapableAccountForHandle);

        if (call.getTargetPhoneAccount() != null || call.isEmergencyCall()) {
            // If the account has been set, proceed to place the outgoing call.
            // Otherwise the connection will be initiated when the account is set by the user.
            call.startCreateConnection(mPhoneAccountRegistrar);
        } else if (mPhoneAccountRegistrar.getCallCapablePhoneAccounts(
                requireCallCapableAccountByHandle ? call.getHandle().getScheme() : null, false,
                call.getInitiatingUser()).isEmpty()) {
            // If there are no call capable accounts, disconnect the call.
            markCallAsDisconnected(call, new DisconnectCause(DisconnectCause.CANCELED,
                    "No registered PhoneAccounts"));
            markCallAsRemoved(call);
        }
    }

    /**
     * Attempts to start a conference call for the specified call.
     *
     * @param call The call to conference.
     * @param otherCall The other call to conference with.
     */
    @VisibleForTesting
    public void conference(Call call, Call otherCall) {
        call.conferenceWith(otherCall);
    }

    /**
     * Instructs Telecom to answer the specified call. Intended to be invoked by the in-call
     * app through {@link InCallAdapter} after Telecom notifies it of an incoming call followed by
     * the user opting to answer said call.
     *
     * @param call The call to answer.
     * @param videoState The video state in which to answer the call.
     */
    @VisibleForTesting
    public void answerCall(Call call, int videoState) {
        if (!mCalls.contains(call)) {
            Log.d(this, "Request to answer a non-existent call %s", call);
        } else {
            Call foregroundCall = getForegroundCall();
            // If the foreground call is not the ringing call and it is currently isActive() or
            // STATE_DIALING, put it on hold before answering the call.
            if (foregroundCall != null && foregroundCall != call &&
                    (foregroundCall.isActive() ||
                     foregroundCall.getState() == CallState.DIALING ||
                     foregroundCall.getState() == CallState.PULLING)) {
                if (0 == (foregroundCall.getConnectionCapabilities()
                        & Connection.CAPABILITY_HOLD)
                        /// M: For OP09 Dsda requirement @{
                        || has1A1WInAnotherPhoneAccount(call)) {
                        /// @}
                    // This call does not support hold.  If it is from a different connection
                    // service, then disconnect it, otherwise allow the connection service to
                    // figure out the right states.
                    /**
                     * M: [DSDA] In MTK's DSDA solution, the Telecom is responsible to disconnect
                     * Active call in some cases.
                     * e.g:
                     * 1. G(1) Active + C(1) Hold + G(2) Waiting + C(2) Waiting
                     *    => answer C(2)
                     *    => G(1) DISCONNECT + C(1) Active(conf) + G(2) Waiting + C(2) Active(conf)
                     * 2. G(1) Hold + C(1) Active + G(2) Waiting + C(2) Waiting
                     *    => answer G(2)
                     *    => G(1) Hold + C(1) DISCONNECT + G(2) Active + C(2) DISCONNECT
                     * Because, both G and C has the same ConnectionService, we can't check them
                     * here.
                     * google code:
                    if (foregroundCall.getConnectionService() != call.getConnectionService()) {
                     * @{
                     */
                    if (!isInSamePhoneAccount(foregroundCall, call)) {
                        /// M: [log optimize]
                        Log.disconnectEvent(foregroundCall, "CallsManager.answerCall: " +
                                "disconnect foreground call because cannot hold it");
                    /** @} */
                        foregroundCall.disconnect();
                    }
                } else {
                    Call heldCall = getHeldCall();
                    if (heldCall != null) {
                        Log.v(this, "Disconnecting held call %s before holding active call.",
                                heldCall);
                        /// M: [log optimize]
                        Log.disconnectEvent(heldCall, "CallsManager.answerCall: " +
                                "disconnect held call for holding the foreground one");
                        heldCall.disconnect();
                        /// M: MSMA call control. @{
                        // Add answer waiting call as a pending call action. Some modem cann't
                        // handle disconnect and answer at the same time. So here need to wait
                        // disconnect complete.
                        addPendingCallAction(heldCall, call,
                                PendingCallAction.PENDING_ACTION_ANSWER, videoState);
                        Log.v(this, "Holding active call %s before answering incoming call %s.",
                                foregroundCall, call);
                        foregroundCall.hold();
                        return;
                        /// @}
                    }

                    Log.v(this, "Holding active/dialing call %s before answering incoming call %s.",
                            foregroundCall, call);
                    foregroundCall.hold();
                }
                // TODO: Wait until we get confirmation of the active call being
                // on-hold before answering the new call.
                // TODO: Import logic from CallManager.acceptCall()
            }

            for (CallsManagerListener listener : mListeners) {
                listener.onIncomingCallAnswered(call);
            }

            // We do not update the UI until we get confirmation of the answer() through
            // {@link #markCallAsActive}.
            call.answer(videoState);
            if (isSpeakerphoneAutoEnabledForVideoCalls(videoState)) {
                call.setStartWithSpeakerphoneOn(true);
            }
        }
    }

    /**
     * Determines if the speakerphone should be automatically enabled for the call.  Speakerphone
     * should be enabled if the call is a video call and bluetooth or the wired headset are not in
     * use.
     *
     * @param videoState The video state of the call.
     * @return {@code true} if the speakerphone should be enabled.
     */
    public boolean isSpeakerphoneAutoEnabledForVideoCalls(int videoState) {
        return VideoProfile.isVideo(videoState) &&
            !mWiredHeadsetManager.isPluggedIn() &&
            !mBluetoothManager.isBluetoothAvailable() &&
            isSpeakerEnabledForVideoCalls();
    }

    /**
     * Determines if the speakerphone should be enabled for when docked.  Speakerphone
     * should be enabled if the device is docked and bluetooth or the wired headset are
     * not in use.
     *
     * @return {@code true} if the speakerphone should be enabled for the dock.
     */
    private boolean isSpeakerphoneEnabledForDock() {
        return mDockManager.isDocked() &&
            !mWiredHeadsetManager.isPluggedIn() &&
            !mBluetoothManager.isBluetoothAvailable();
    }

    /**
     * Determines if the speakerphone should be automatically enabled for video calls.
     *
     * @return {@code true} if the speakerphone should automatically be enabled.
     */
    private static boolean isSpeakerEnabledForVideoCalls() {
        return (SystemProperties.getInt(TelephonyProperties.PROPERTY_VIDEOCALL_AUDIO_OUTPUT,
                PhoneConstants.AUDIO_OUTPUT_DEFAULT) ==
                PhoneConstants.AUDIO_OUTPUT_ENABLE_SPEAKER);
    }

    /**
     * Instructs Telecom to reject the specified call. Intended to be invoked by the in-call
     * app through {@link InCallAdapter} after Telecom notifies it of an incoming call followed by
     * the user opting to reject said call.
     */
    @VisibleForTesting
    public void rejectCall(Call call, boolean rejectWithMessage, String textMessage) {
        if (!mCalls.contains(call)) {
            Log.d(this, "Request to reject a non-existent call %s", call);
        } else {
            for (CallsManagerListener listener : mListeners) {
                listener.onIncomingCallRejected(call, rejectWithMessage, textMessage);
            }
            call.reject(rejectWithMessage, textMessage);
        }
    }

    /**
     * Instructs Telecom to play the specified DTMF tone within the specified call.
     *
     * @param digit The DTMF digit to play.
     */
    @VisibleForTesting
    public void playDtmfTone(Call call, char digit) {
        if (!mCalls.contains(call)) {
            Log.d(this, "Request to play DTMF in a non-existent call %s", call);
        } else {
            call.playDtmfTone(digit);
            mDtmfLocalTonePlayer.playTone(call, digit);
        }
    }

    /**
     * Instructs Telecom to stop the currently playing DTMF tone, if any.
     */
    @VisibleForTesting
    public void stopDtmfTone(Call call) {
        if (!mCalls.contains(call)) {
            Log.d(this, "Request to stop DTMF in a non-existent call %s", call);
        } else {
            call.stopDtmfTone();
            mDtmfLocalTonePlayer.stopTone(call);
        }
    }

    /**
     * Instructs Telecom to continue (or not) the current post-dial DTMF string, if any.
     */
    void postDialContinue(Call call, boolean proceed) {
        //ALPS01833456 call maybe null
        if (call != null) {
            if (!mCalls.contains(call)) {
                Log.d(this, "Request to continue post-dial string in a non-existent call %s", call);
            } else {
                call.postDialContinue(proceed);
            }
        }
    }

    /**
     * Instructs Telecom to disconnect the specified call. Intended to be invoked by the
     * in-call app through {@link InCallAdapter} for an ongoing call. This is usually triggered by
     * the user hitting the end-call button.
     */
    @VisibleForTesting
    public void disconnectCall(Call call) {
        Log.v(this, "disconnectCall %s", call);

        if (!mCalls.contains(call)) {
            Log.d(this, "Unknown call (%s) asked to disconnect", call);
        } else {
            mLocallyDisconnectingCalls.add(call);
            /// M: CC start. @{
            // All childCalls whithin a conference call should not be updated as foreground
            for (Call childCall : call.getChildCalls()) {
                mLocallyDisconnectingCalls.add(childCall);
            }
            /// @}
            call.disconnect();
        }
    }

    /**
     * Instructs Telecom to disconnect all calls.
     */
    void disconnectAllCalls() {
        Log.v(this, "disconnectAllCalls");

        for (Call call : mCalls) {
            /// M: only disconnect top level calls. @{
            if (call.getParentCall() != null) {
                continue;
            }
            /// @}
            /// M: [log optimize]
            Log.disconnectEvent(call, "CallsManager.disconnectAllCalls");
            disconnectCall(call);
        }
    }

    /**
     * Instructs Telecom to put the specified call on hold. Intended to be invoked by the
     * in-call app through {@link InCallAdapter} for an ongoing call. This is usually triggered by
     * the user hitting the hold button during an active call.
     */
    @VisibleForTesting
    public void holdCall(Call call) {
        if (!mCalls.contains(call)) {
            Log.d(this, "Unknown call (%s) asked to be put on hold", call);
        } else {
            Log.d(this, "Putting call on hold: (%s)", call);
            call.hold();
        }
        /// M: When have active call and hold call in different account, hold operation will
        // swap the two call.
        Call heldCall = getHeldCall();
        if (heldCall != null &&
                !Objects.equals(call.getTargetPhoneAccount(), heldCall.getTargetPhoneAccount())) {
            heldCall.unhold();
        }
        /// @}
    }

    /**
     * Instructs Telecom to release the specified call from hold. Intended to be invoked by
     * the in-call app through {@link InCallAdapter} for an ongoing call. This is usually triggered
     * by the user hitting the hold button during a held call.
     */
    @VisibleForTesting
    public void unholdCall(Call call) {
        if (!mCalls.contains(call)) {
            Log.d(this, "Unknown call (%s) asked to be removed from hold", call);
        } else {
            boolean otherCallHeld = false;
            Log.d(this, "unholding call: (%s)", call);
            /* Google Original code
            for (Call c : mCalls) {
                // Only attempt to hold parent calls and not the individual children.
                if (c != null && c.isAlive() && c != call && c.getParentCall() == null) {
                    otherCallHeld = true;
                    Log.event(c, Log.Events.SWAP);
                    c.hold();
                }
            } */
            /// M: If foreground call doesn't support hold, should not do unhold. @{
            Call foregroundCall = getForegroundCall();
            if (foregroundCall != null && foregroundCall != call) {
                if (foregroundCall.can(Connection.CAPABILITY_HOLD)) {
                    foregroundCall.hold();
                } else {
                    TelecomUtils.showToast(R.string.incall_error_supp_service_switch);
                    return;
                }
            }

            /// @}

            if (otherCallHeld) {
                Log.event(call, Log.Events.SWAP);
            }

            call.unhold();
        }
    }

    @Override
    public void onExtrasChanged(Call c, int source, Bundle extras) {
        if (source != Call.SOURCE_CONNECTION_SERVICE) {
            return;
        }
        handleCallTechnologyChange(c);
        handleChildAddressChange(c);
        updateCanAddCall();
    }

    // Construct the list of possible PhoneAccounts that the outgoing call can use based on the
    // active calls in CallsManager. If any of the active calls are on a SIM based PhoneAccount,
    // then include only that SIM based PhoneAccount and any non-SIM PhoneAccounts, such as SIP.
    private List<PhoneAccountHandle> constructPossiblePhoneAccounts(Uri handle, UserHandle user) {
        if (handle == null) {
            return Collections.emptyList();
        }
        List<PhoneAccountHandle> allAccounts =
                mPhoneAccountRegistrar.getCallCapablePhoneAccounts(handle.getScheme(), false, user);
        ///M: Google DSDA static judgement is not suitable for MTK
        // because MTK DSDA can dynamic change into DSDS when two sim cards are GSM
        // or C+G combination, but C is in roaming state
        // MTK add one API to judge DSDA mode and its value will change dynamically @{
        // google original code:
        /*
        // First check the Radio SIM Technology
        if(mRadioSimVariants == null) {
            TelephonyManager tm = (TelephonyManager) mContext.getSystemService(
                    Context.TELEPHONY_SERVICE);
            // Cache Sim Variants
            mRadioSimVariants = tm.getMultiSimConfiguration();
        }
        // Only one SIM PhoneAccount can be active at one time for DSDS. Only that SIM PhoneAccount
        // Should be available if a call is already active on the SIM account.
        if(mRadioSimVariants != TelephonyManager.MultiSimVariants.DSDA) {
        */
        if(!TelecomUtils.isInDsdaMode()) {
        /// @}
            List<PhoneAccountHandle> simAccounts =
                    mPhoneAccountRegistrar.getSimPhoneAccountsOfCurrentUser();
            PhoneAccountHandle ongoingCallAccount = null;
            for (Call c : mCalls) {
                if (!c.isDisconnected() && !c.isNew() && simAccounts.contains(
                        c.getTargetPhoneAccount())) {
                    ongoingCallAccount = c.getTargetPhoneAccount();
                    break;
                }
            }
            if (ongoingCallAccount != null) {
                // Remove all SIM accounts that are not the active SIM from the list.
                Log.d(this, "[constructPossiblePhoneAccounts]Not DSDA, remove all SIM accounts" +
                        " except the ongoing call account: " + ongoingCallAccount);
                simAccounts.remove(ongoingCallAccount);
                allAccounts.removeAll(simAccounts);
            }
        }
        return allAccounts;
    }

    /**
     * Informs listeners (notably {@link CallAudioManager} of a change to the call's external
     * property.
     * .
     * @param call The call whose external property changed.
     * @param isExternalCall {@code True} if the call is now external, {@code false} otherwise.
     */
    @Override
    public void onExternalCallChanged(Call call, boolean isExternalCall) {
        Log.v(this, "onConnectionPropertiesChanged: %b", isExternalCall);
        for (CallsManagerListener listener : mListeners) {
            listener.onExternalCallChanged(call, isExternalCall);
        }
    }

    private void handleCallTechnologyChange(Call call) {
        if (call.getExtras() != null
                && call.getExtras().containsKey(TelecomManager.EXTRA_CALL_TECHNOLOGY_TYPE)) {

            Integer analyticsCallTechnology = sAnalyticsTechnologyMap.get(
                    call.getExtras().getInt(TelecomManager.EXTRA_CALL_TECHNOLOGY_TYPE));
            if (analyticsCallTechnology == null) {
                analyticsCallTechnology = Analytics.THIRD_PARTY_PHONE;
            }
            call.getAnalytics().addCallTechnology(analyticsCallTechnology);
        }
    }

    public void handleChildAddressChange(Call call) {
        if (call.getExtras() != null
                && call.getExtras().containsKey(Connection.EXTRA_CHILD_ADDRESS)) {

            String viaNumber = call.getExtras().getString(Connection.EXTRA_CHILD_ADDRESS);
            call.setViaNumber(viaNumber);
        }
    }

    /** Called by the in-call UI to change the mute state. */
    void mute(boolean shouldMute) {
        mCallAudioManager.mute(shouldMute);
    }

    /**
      * Called by the in-call UI to change the audio route, for example to change from earpiece to
      * speaker phone.
      */
    void setAudioRoute(int route) {
        mCallAudioManager.setAudioRoute(route);
    }

    /** Called by the in-call UI to turn the proximity sensor on. */
    void turnOnProximitySensor() {
        mProximitySensorManager.turnOn();
    }

    /**
     * Called by the in-call UI to turn the proximity sensor off.
     * @param screenOnImmediately If true, the screen will be turned on immediately. Otherwise,
     *        the screen will be kept off until the proximity sensor goes negative.
     */
    void turnOffProximitySensor(boolean screenOnImmediately) {
        mProximitySensorManager.turnOff(screenOnImmediately);
    }

    void phoneAccountSelected(Call call, PhoneAccountHandle account, boolean setDefault) {
        if (setDefault) {
            mPhoneAccountRegistrar
                        .setUserSelectedOutgoingPhoneAccount(account, call.getInitiatingUser());
        }
        if (!mCalls.contains(call) && !isPotentialMMICode(call.getHandle())
                && !isPotentialInCallMMICode(call.getHandle())) {
            Log.d(this, "Attempted to add account to unknown call %s", call);
        } else {
            call.setTargetPhoneAccount(account);
            if (call.isConferenceDial()) {
                call.startCreateConnection(TelecomSystem.getInstance().getPhoneAccountRegistrar());
                Log.d(this, "conference dial after account selected");
                return;
            }

            /// M: Conference dial will not send broadcast, skip conference dial check.
            if (!call.isConferenceDial() && !call.isNewOutgoingCallIntentBroadcastDone()) {
                return;
            }

            /// M: For Ip dial @{
            if (!TelecomUtils.handleIpPrefixForCall(mContext, call)) {
                /// M: [log optimize]
                Log.disconnectEvent(call,
                        "CallsManager.phoneAccountSelected: failed to deal with IP prefix");
                disconnectCall(call);
                return;
            };
            /// @}

            // Note: emergency calls never go through account selection dialog so they never
            // arrive here.
            if (makeRoomForOutgoingCall(call, false /* isEmergencyCall */)) {
                call.startCreateConnection(mPhoneAccountRegistrar);
            } else {
                showToastInfomation(mContext.getResources()
                        .getString(R.string.outgoing_call_failed));
                /// M: [log optimize]
                Log.disconnectEvent(call,
                        "CallsManager.phoneAccountSelected: failed to make room");
                call.disconnect();
            }
            /// M: For CDMA India optr @{
            broadcastCallPlacedIntent(call);
            /// @}
        }
    }

    /// M: For CDMA India optr @{
    private void broadcastCallPlacedIntent(Call call) {
        if (SystemProperties.get("persist.sys.esn_track_switch").equals("1")) {
            Log.d(this, "broadcastCallPlacedIntent Entry");
            int subId = call.getSubId();
            if (subId != SubscriptionManager.INVALID_SUBSCRIPTION_ID) {
                Log.d(this, "phoneAccountSelected cdma subIdInt= " + subId);
                mContext.sendBroadcast(new Intent(ACTION_ESN_MO_CALL)
                        .putExtra(PhoneConstants.SUBSCRIPTION_KEY, subId));
            }
        }
    }
    /// @}

    /** Called when the audio state changes. */
    @VisibleForTesting
    public void onCallAudioStateChanged(CallAudioState oldAudioState, CallAudioState
            newAudioState) {
        Log.v(this, "onAudioStateChanged, audioState: %s -> %s", oldAudioState, newAudioState);
        for (CallsManagerListener listener : mListeners) {
            listener.onCallAudioStateChanged(oldAudioState, newAudioState);
        }
    }

    void markCallAsRinging(Call call) {
        setCallState(call, CallState.RINGING, "ringing set explicitly");
    }

    void markCallAsDialing(Call call) {
        setCallState(call, CallState.DIALING, "dialing set explicitly");
        maybeMoveToSpeakerPhone(call);
    }

    void markCallAsPulling(Call call) {
        setCallState(call, CallState.PULLING, "pulling set explicitly");
        maybeMoveToSpeakerPhone(call);
    }

    void markCallAsActive(Call call) {
        setCallState(call, CallState.ACTIVE, "active set explicitly");
        maybeMoveToSpeakerPhone(call);
    }

    void markCallAsOnHold(Call call) {
        setCallState(call, CallState.ON_HOLD, "on-hold set explicitly");
    }

    /**
     * Marks the specified call as STATE_DISCONNECTED and notifies the in-call app. If this was the
     * last live call, then also disconnect from the in-call controller.
     *
     * @param disconnectCause The disconnect cause, see {@link android.telecom.DisconnectCause}.
     */
    void markCallAsDisconnected(Call call, DisconnectCause disconnectCause) {

        /// M: for volte @{
        // TODO: if disconnectCause is IMS_EMERGENCY_REREG, redial it and do not notify disconnect.
        // placeOutgoingCall(call, handle, gatewayInfo, speakerphoneOn, videoState);
        /// @}

        call.setDisconnectCause(disconnectCause);
        setCallState(call, CallState.DISCONNECTED, "disconnected set explicitly");
    }

    /**
     * Removes an existing disconnected call, and notifies the in-call app.
     */
    void markCallAsRemoved(Call call) {
        removeCall(call);
        Call foregroundCall = mCallAudioManager.getPossiblyHeldForegroundCall();
        if (mLocallyDisconnectingCalls.contains(call)
            || ExtensionManager.getCallMgrExt().shouldResumeHoldCall() ) {

            mLocallyDisconnectingCalls.remove(call);

            if (foregroundCall != null && foregroundCall.getState() == CallState.ON_HOLD
                    //// M: ALPS01765683 Disconnect a member in conference (in HOLD status),
                    // the conference should still in hold status. @{
                    && !(call.getChildCalls().size() > 0
                    && call.getChildCalls().contains(foregroundCall))) {
                    //// @}
                foregroundCall.unhold();
            }
        } else if (foregroundCall != null &&
                /*PRIZE-Delete-DialerV8-wangzhong-2017_7_19-start*/
                /*!foregroundCall.can(Connection.CAPABILITY_SUPPORT_HOLD)  &&*/
                /*PRIZE-Delete-DialerV8-wangzhong-2017_7_19-end*/
                foregroundCall.getState() == CallState.ON_HOLD) {

            // The new foreground call is on hold, however the carrier does not display the hold
            // button in the UI.  Therefore, we need to auto unhold the held call since the user has
            // no means of unholding it themselves.
            Log.i(this, "Auto-unholding held foreground call (call doesn't support hold)");
            foregroundCall.unhold();
        }

        ///M: [EccDisconnectAll] place the pending Ecc if exists when all calls disconnected @{
        Log.d(this, "markCallAsRemoved mCalls size = " + mCalls.size());
        if (mPendingEccCall != null && isOkForECC(mPendingEccCall)) {
            Log.i(this, "markCallAsRemoved, dial pending ECC:" + mPendingEccCall);
            if (!mCalls.contains(mPendingEccCall)) {
                mCalls.add(mPendingEccCall);
            }
            mPendingEccCall.startCreateConnection(mPhoneAccountRegistrar);
            mPendingEccCall = null;
        }
        ///@}

        ///M: Make sure the destroyed call be removed from pending call list. @{
        // A pending call which has been destroyed should not be reused in future.
        // The aim to reuse a call object is to make InCallService not to know
        // the call is disconnected by telecom locally. If the call has already
        // been destroy, it means the IncallService already knows that the call
        // was disconnected. If the call is reused in future, the Incallservice
        // will see the state is changed from disconnected to other state, it is
        // abnormal.
        mPendingCallsToDisconnect.remove(call);
        ///@}
    }

    /**
     * Cleans up any calls currently associated with the specified connection service when the
     * service binder disconnects unexpectedly.
     *
     * @param service The connection service that disconnected.
     */
    void handleConnectionServiceDeath(ConnectionServiceWrapper service) {
        if (service != null) {
            for (Call call : mCalls) {
                if (call.getConnectionService() == service) {
                    if (call.getState() != CallState.DISCONNECTED) {
                        markCallAsDisconnected(call, new DisconnectCause(DisconnectCause.ERROR));
                    }
                    markCallAsRemoved(call);
                }
            }
        }
    }

    /**
     * Determines if the {@link CallsManager} has any non-external calls.
     *
     * @return {@code True} if there are any non-external calls, {@code false} otherwise.
     */
    boolean hasAnyCalls() {
        if (mCalls.isEmpty()) {
            return false;
        }

        for (Call call : mCalls) {
            if (!call.isExternalCall()) {
                return true;
            }
        }
        return false;
    }

    boolean hasActiveOrHoldingCall() {
        return getFirstCallWithState(CallState.ACTIVE, CallState.ON_HOLD) != null;
    }

    boolean hasRingingCall() {
        return getFirstCallWithState(CallState.RINGING) != null ||
               ///M: checking "NEW" incoming call to avoid timing issue.
               getFirstCallWithState(CallState.NEW) != null &&
               getFirstCallWithState(CallState.NEW).isIncoming();
    }

    boolean onMediaButton(int type) {
        if (hasAnyCalls()) {
            if (HeadsetMediaButton.SHORT_PRESS == type) {
                Call ringingCall = getFirstCallWithState(CallState.RINGING);
                if (ringingCall == null) {
                    mCallAudioManager.toggleMute();
                    return true;
                } else {
                    /// M: AOSP code:
                    /// ringingCall.answer(VideoProfile.STATE_AUDIO_ONLY);
                    // M: fix CR:ALPS03122393,can not answer 3GVT call by Headset.
                    // (3GVT can not downgrade to voice call).
                    if (ringingCall.is3GVideoCall()) {
                        disconnectCall(ringingCall);
                    } else {
                        answerCall(ringingCall, VideoProfile.STATE_AUDIO_ONLY);
                    }
                    return true;
                }
            } else if (HeadsetMediaButton.LONG_PRESS == type) {
                Log.d(this, "handleHeadsetHook: longpress -> hangup");
                Call callToHangup = getFirstCallWithState(
                        CallState.RINGING, CallState.DIALING, CallState.PULLING, CallState.ACTIVE,
                        CallState.ON_HOLD);
                if (callToHangup != null) {
                    /// M: ALPS01790323. disconnect call through CallsManager
                    /*
                     * original code
                     * callToHangup.disconnect();
                     */
                    /// M: [log optimize]
                    Log.disconnectEvent(callToHangup,
                            "CallsManager.onMediaButton: headset long pressed");
                    disconnectCall(callToHangup);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns true if telecom supports adding another top-level call.
     */
    @VisibleForTesting
    public boolean canAddCall() {
        boolean isDeviceProvisioned = Settings.Global.getInt(mContext.getContentResolver(),
                Settings.Global.DEVICE_PROVISIONED, 0) != 0;
        if (!isDeviceProvisioned) {
            Log.d(TAG, "Device not provisioned, canAddCall is false.");
            return false;
        }

        /// M: MSMA call control. Need to support more than 2 top level calls. @{
        if (hasEmergencyCall() || hasRingingCall()) {
            return false;
        }

        if (getFirstCallWithState(OUTGOING_CALL_STATES) != null || getDialingCdmaCall() != null) {
            return false;
        }

        int count = 0;
        for (Call call : mCalls) {
            if (call.isEmergencyCall()) {
                // We never support add call if one of the calls is an emergency call.
                return false;
            } else if (call.isExternalCall()) {
                // External calls don't count.
                continue;
            } else if (call.getParentCall() == null) {
                count++;
            }
            Bundle extras = call.getExtras();
            if (extras != null) {
                if (extras.getBoolean(Connection.EXTRA_DISABLE_ADD_CALL, false)) {
                    return false;
                }
            }

            // We do not check states for canAddCall. We treat disconnected calls the same
            // and wait until they are removed instead. If we didn't count disconnected calls,
            // we could put InCallServices into a state where they are showing two calls but
            // also support add-call. Technically it's right, but overall looks better (UI-wise)
            // and acts better if we wait until the call is removed.
            if (count >= MAXIMUM_TOP_LEVEL_CALLS) {
                return false;
            }
        }

        return true;
    }

    @VisibleForTesting
    public Call getRingingCall() {
        return getFirstCallWithState(CallState.RINGING);
    }

    @VisibleForTesting
    public Call getActiveCall() {
        return getFirstCallWithState(CallState.ACTIVE);
    }

    Call getDialingCall() {
        return getFirstCallWithState(CallState.DIALING);
    }

    @VisibleForTesting
    public Call getHeldCall() {
        return getFirstCallWithState(CallState.ON_HOLD);
    }

    @VisibleForTesting
    public int getNumHeldCalls() {
        int count = 0;
        for (Call call : mCalls) {
            if (call.getParentCall() == null && call.getState() == CallState.ON_HOLD) {
                count++;
            }
        }
        return count;
    }

    @VisibleForTesting
    public Call getOutgoingCall() {
        return getFirstCallWithState(OUTGOING_CALL_STATES);
    }

    @VisibleForTesting
    public Call getFirstCallWithState(int... states) {
        return getFirstCallWithState(null, states);
    }

    @VisibleForTesting
    public PhoneNumberUtilsAdapter getPhoneNumberUtilsAdapter() {
        return mPhoneNumberUtilsAdapter;
    }

    /**
     * Returns the first call that it finds with the given states. The states are treated as having
     * priority order so that any call with the first state will be returned before any call with
     * states listed later in the parameter list.
     *
     * @param callToSkip Call that this method should skip while searching
     */
    Call getFirstCallWithState(Call callToSkip, int... states) {
        for (int currentState : states) {
            // check the foreground first
            Call foregroundCall = getForegroundCall();
            if (foregroundCall != null && foregroundCall.getState() == currentState) {
                return foregroundCall;
            }

            for (Call call : mCalls) {
                if (Objects.equals(callToSkip, call)) {
                    continue;
                }

                // Only operate on top-level calls
                if (call.getParentCall() != null) {
                    continue;
                }

                if (call.isExternalCall()) {
                    continue;
                }

                if (currentState == call.getState()) {
                    return call;
                }
            }
        }
        return null;
    }

    Call createConferenceCall(
            String callId,
            PhoneAccountHandle phoneAccount,
            ParcelableConference parcelableConference) {

        ///M: If the parceled conference doesn't specify any time, then set the connect time
        // to System.currentTimeMillis(), because connection has already been created.
        // Original comments below: @{
        // If the parceled conference specifies a connect time, use it; otherwise default to 0,
        // which is the default value for new Calls.
        long connectTime =
                parcelableConference.getConnectTimeMillis() ==
                        Conference.CONNECT_TIME_NOT_SPECIFIED ? System.currentTimeMillis() :
                        parcelableConference.getConnectTimeMillis();
        ///@}

        Call call = new Call(
                callId,
                mContext,
                this,
                mLock,
                mConnectionServiceRepository,
                mContactsAsyncHelper,
                mCallerInfoAsyncQueryFactory,
                mPhoneNumberUtilsAdapter,
                null /* handle */,
                null /* gatewayInfo */,
                null /* connectionManagerPhoneAccount */,
                phoneAccount,
                Call.CALL_DIRECTION_UNDEFINED /* callDirection */,
                false /* forceAttachToExistingConnection */,
                true /* isConference */,
                connectTime);

        setCallState(call, Call.getStateFromConnectionState(parcelableConference.getState()),
                "new conference call");
        call.setConnectionCapabilities(parcelableConference.getConnectionCapabilities());
        call.setConnectionProperties(parcelableConference.getConnectionProperties());
        call.setVideoState(parcelableConference.getVideoState());
        call.setVideoProvider(parcelableConference.getVideoProvider());
        call.setStatusHints(parcelableConference.getStatusHints());
        call.putExtras(Call.SOURCE_CONNECTION_SERVICE, parcelableConference.getExtras());
        // In case this Conference was added via a ConnectionManager, keep track of the original
        // Connection ID as created by the originating ConnectionService.
        Bundle extras = parcelableConference.getExtras();
        if (extras != null && extras.containsKey(Connection.EXTRA_ORIGINAL_CONNECTION_ID)) {
            call.setOriginalConnectionId(extras.getString(Connection.EXTRA_ORIGINAL_CONNECTION_ID));
        }

        // TODO: Move this to be a part of addCall()
        call.addListener(this);
        addCall(call);
        return call;
    }

    /**
     * @return the call state currently tracked by {@link PhoneStateBroadcaster}
     */
    int getCallState() {
        return mPhoneStateBroadcaster.getCallState();
    }

    /**
     * Retrieves the {@link PhoneAccountRegistrar}.
     *
     * @return The {@link PhoneAccountRegistrar}.
     */
    PhoneAccountRegistrar getPhoneAccountRegistrar() {
        return mPhoneAccountRegistrar;
    }

    /**
     * Retrieves the {@link MissedCallNotifier}
     * @return The {@link MissedCallNotifier}.
     */
    MissedCallNotifier getMissedCallNotifier() {
        return mMissedCallNotifier;
    }

    /**
     * Reject an incoming call and manually add it to the Call Log.
     * @param incomingCall Incoming call that has been rejected
     */
    private void rejectCallAndLog(Call incomingCall) {
        if (incomingCall.getConnectionService() != null) {
            // Only reject the call if it has not already been destroyed.  If a call ends while
            // incoming call filtering is taking place, it is possible that the call has already
            // been destroyed, and as such it will be impossible to send the reject to the
            // associated ConnectionService.
            incomingCall.reject(false, null);
        } else {
            Log.i(this, "rejectCallAndLog - call already destroyed.");
        }

        // Since the call was not added to the list of calls, we have to call the missed
        // call notifier and the call logger manually.
        // Do we need missed call notification for direct to Voicemail calls?
        mCallLogManager.logCall(incomingCall, Calls.MISSED_TYPE,
                true /*showNotificationForMissedCall*/);
    }

    /**
     * Adds the specified call to the main list of live calls.
     *
     * @param call The call to add.
     */
    private void addCall(Call call) {
        Trace.beginSection("addCall");
        Log.v(this, "addCall(%s)", call);
        call.addListener(this);
        mCalls.add(call);

        // Specifies the time telecom finished routing the call. This is used by the dialer for
        // analytics.
        Bundle extras = call.getIntentExtras();
        extras.putLong(TelecomManager.EXTRA_CALL_TELECOM_ROUTING_END_TIME_MILLIS,
                SystemClock.elapsedRealtime());

        updateCanAddCall();
        // onCallAdded for calls which immediately take the foreground (like the first call).
        for (CallsManagerListener listener : mListeners) {
            if (Log.SYSTRACE_DEBUG) {
                Trace.beginSection(listener.getClass().toString() + " addCall");
            }
            listener.onCallAdded(call);
            if (Log.SYSTRACE_DEBUG) {
                Trace.endSection();
            }
        }
        Trace.endSection();
    }

    private void removeCall(Call call) {
        Trace.beginSection("removeCall");
        Log.v(this, "removeCall(%s)", call);

        call.setParentCall(null);  // need to clean up parent relationship before destroying.
        call.removeListener(this);
        call.clearConnectionService();

        boolean shouldNotify = false;
        if (mCalls.contains(call)) {
            mCalls.remove(call);
            shouldNotify = true;
        }

        call.destroy();

        // Only broadcast changes for calls that are being tracked.
        if (shouldNotify) {
            updateCanAddCall();
            for (CallsManagerListener listener : mListeners) {
                if (Log.SYSTRACE_DEBUG) {
                    Trace.beginSection(listener.getClass().toString() + " onCallRemoved");
                }
                listener.onCallRemoved(call);
                if (Log.SYSTRACE_DEBUG) {
                    Trace.endSection();
                }
            }
        }
        Trace.endSection();
    }

    /**
     * Sets the specified state on the specified call.
     *
     * @param call The call.
     * @param newState The new state of the call.
     */
    private void setCallState(Call call, int newState, String tag) {
        if (call == null) {
            return;
        }
        int oldState = call.getState();
        Log.d(this, "setCallState %s -> %s, call: %s", CallState.toString(oldState),
                CallState.toString(newState), call);
        if (newState != oldState) {
            // Unfortunately, in the telephony world the radio is king. So if the call notifies
            // us that the call is in a particular state, we allow it even if it doesn't make
            // sense (e.g., STATE_ACTIVE -> STATE_RINGING).
            // TODO: Consider putting a stop to the above and turning CallState
            // into a well-defined state machine.
            // TODO: Define expected state transitions here, and log when an
            // unexpected transition occurs.
            call.setState(newState, tag);
            maybeShowErrorDialogOnDisconnect(call);

            Trace.beginSection("onCallStateChanged");
            // Only broadcast state change for calls that are being tracked.
            // M: Add log for upgrade debug.
            if (TelecomUtils.getUpgradeLoggingEnabled()) {
                Log.i(this, "setCallState mCalls.contains(call) = " + mCalls.contains(call));
            }
            if (mCalls.contains(call)) {
                updateCanAddCall();
                // M: Add log for upgrade debug.
                if (TelecomUtils.getUpgradeLoggingEnabled()) {
                    Log.i(this, "setCallState before mListeners size = " + mListeners.size());
                }
                for (CallsManagerListener listener : mListeners) {
                    if (Log.SYSTRACE_DEBUG) {
                        Trace.beginSection(listener.getClass().toString() + " onCallStateChanged");
                    }
                    // M: Add log for upgrade debug.
                    if (TelecomUtils.getUpgradeLoggingEnabled()) {
                        Log.i(this, "setCallState listener = " + listener);
                    }
                    listener.onCallStateChanged(call, oldState, newState);
                    if (Log.SYSTRACE_DEBUG) {
                        Trace.endSection();
                    }
                }
                // M: Add log for upgrade debug.
                if (TelecomUtils.getUpgradeLoggingEnabled()) {
                    Log.i(this, "setCallState after mListeners size = " + mListeners.size());
                }
            }
            /// M: MSMA call control, first call action finished. @{
            handleActionProcessComplete(call);
            /// @}
            Trace.endSection();
        }

        ///M: For ECC retry @{
        // need to set audio mode as normal when ecc call state unchanged case here.
        if (call.isEmergencyCall()
                && (oldState == CallState.CONNECTING && newState == CallState.CONNECTING)
                && (getForegroundCall() != null && getForegroundCall().isEmergencyCall())) {
            mCallAudioManager.resetAudioMode();
        }
        /// @}
    }


    private void updateCanAddCall() {
        boolean newCanAddCall = canAddCall();
        if (newCanAddCall != mCanAddCall) {
            mCanAddCall = newCanAddCall;
            for (CallsManagerListener listener : mListeners) {
                if (Log.SYSTRACE_DEBUG) {
                    Trace.beginSection(listener.getClass().toString() + " updateCanAddCall");
                }
                listener.onCanAddCallChanged(mCanAddCall);
                if (Log.SYSTRACE_DEBUG) {
                    Trace.endSection();
                }
            }
        }
    }

    private boolean isPotentialMMICode(Uri handle) {
        /** M:android default code @{
        return (handle != null && handle.getSchemeSpecificPart() != null
                && handle.getSchemeSpecificPart().contains("#"));
        @ } */
        String number = handle != null ? handle.getSchemeSpecificPart() : null;
        if (TextUtils.isEmpty(number)) {
            return false;
        }

        ///M: add for test case TC31.9.1.1 @{
        if (number.trim().equals("7") ||
                number.trim().equals("36")) {
              int currentMode = SystemProperties.getInt("gsm.gcf.testmode", 0);
              if (currentMode == 2) {// mode 2 stands for FTA Mode.
                 return true;
              }
        }
        /// @}

        ///M: MMI doesn't contain "@"
        return (number.contains("#") && !number.contains("@"));
    }

    /**
     * Determines if a dialed number is potentially an In-Call MMI code.  In-Call MMI codes are
     * MMI codes which can be dialed when one or more calls are in progress.
     * <P>
     * Checks for numbers formatted similar to the MMI codes defined in:
     * {@link com.android.internal.telephony.Phone#handleInCallMmiCommands(String)}
     *
     * @param handle The URI to call.
     * @return {@code True} if the URI represents a number which could be an in-call MMI code.
     */
    private boolean isPotentialInCallMMICode(Uri handle) {
        if (handle != null && handle.getSchemeSpecificPart() != null &&
                handle.getScheme() != null &&
                handle.getScheme().equals(PhoneAccount.SCHEME_TEL)) {

            String dialedNumber = handle.getSchemeSpecificPart();
            return (dialedNumber.equals("0") ||
                    (dialedNumber.startsWith("1") && dialedNumber.length() <= 2) ||
                    (dialedNumber.startsWith("2") && dialedNumber.length() <= 2) ||
                    dialedNumber.equals("3") ||
                    dialedNumber.equals("4") ||
                    dialedNumber.equals("5"));
        }
        return false;
    }

    private int getNumCallsWithState(int... states) {
        int count = 0;
        for (int state : states) {
            for (Call call : mCalls) {

                if (call.getParentCall() == null && call.getState() == state &&
                        !call.isExternalCall()) {

                    count++;
                }
            }
        }
        return count;
    }

    private boolean hasMaximumLiveCalls() {
        return MAXIMUM_LIVE_CALLS <= getNumCallsWithState(LIVE_CALL_STATES);
    }

    private boolean hasMaximumHoldingCalls() {
        return MAXIMUM_HOLD_CALLS <= getNumCallsWithState(CallState.ON_HOLD);
    }

    private boolean hasMaximumRingingCalls() {
        return MAXIMUM_RINGING_CALLS <= getNumCallsWithState(CallState.RINGING);
    }

    private boolean hasMaximumOutgoingCalls() {
        return MAXIMUM_OUTGOING_CALLS <= getNumCallsWithState(OUTGOING_CALL_STATES);
    }

    private boolean hasMaximumDialingCalls() {
        return MAXIMUM_DIALING_CALLS <= getNumCallsWithState(CallState.DIALING, CallState.PULLING);
    }

    private boolean makeRoomForOutgoingCall(Call call, boolean isEmergency) {
        /// M: Add some outgoing call control rule @{
        if (hasOtherEmergencyCall(call)) {
            return false;
        }
        /// M: [EccDisconnectAll] Add for Ecc disconnect all calls
        if (isEmergency) {
            return true;
        }
        if (preventCallFromOtherSimBasedAccountForDsds(call)) {
            return false;
        }
        if (isPotentialInCallMMICode(call.getHandle())
                && TelecomUtils.isSupportMMICode(call.getTargetPhoneAccount())) {
            return true;
        }
        /// M: handle CDMA potential MMI call. @{
        // If the account doesn't support MMI, such as c2k, MMI will be consider as normal call,
        // so need to do more check if could place a new normal call.
        if (isPotentialMMICode(call.getHandle())
                && TelecomUtils.isSupportMMICode(
                call.getTargetPhoneAccount())) {
            if (getRingingCall() != null)
                return false;
            else {
                ///M:@{
                // If there is an orphaned call in the {@link CallState#SELECT_PHONE_ACCOUNT}
                // state, just disconnect it since the user has explicitly started a new call.
                Call outCall = getFirstCallWithState(call, OUTGOING_CALL_STATES);
                if (outCall != null && outCall.getState() == CallState.SELECT_PHONE_ACCOUNT) {
                    Log.d(this, "disconnect call to USSD call room");
                    /// M: [log optimize]
                    Log.disconnectEvent(outCall, "CallsManager.makeRoomForOutgoingCall:" +
                                    " disconnect SELECT_PHONE_ACCOUNT call for MMI");
                    outCall.disconnect();
                }
                /// @}
                return true;
            }
        }
        /// @}
        if (hasRingingCall() && !isEmergency) {
            Log.i(this, "can not start outgoing call, have ringing call.");
            return false;
        }
        /// @}

        /// M: For block certain ViLTE @{
        if (shouldBlockForCertainViLTE(call)) {
            Log.i(this, "makeRoomForOutgoingCall: Block certain ViLTE!");
            return false;
        }
        /// @}

        if (hasMaximumLiveCalls()) {
            // NOTE: If the amount of live calls changes beyond 1, this logic will probably
            // have to change.
            ///M: google original code @{
            // we should skip the call itself
            // or the active can not be hold
            //Call liveCall = getFirstCallWithState(LIVE_CALL_STATES);
            Call liveCall = getFirstCallWithState(call, LIVE_CALL_STATES);
            /// @}
            Log.i(this, "makeRoomForOutgoingCall call = " + call + " livecall = " +
                   liveCall);

            /// M: If exist only one live call, and it is the new outgoing call itself,
            // the liveCall calculated above will be null. @{
            if (liveCall == null || call == liveCall) {
            /// @}
                // If the call is already the foreground call, then we are golden.
                // This can happen after the user selects an account in the SELECT_PHONE_ACCOUNT
                // state since the call was already populated into the list.
                return true;
            }

            /// M: Should skip the new outgoing call itself. outgoingCall
            // should include the dialing call with fake active state.@{
            Call outgoingCall = getFirstCallWithState(call, OUTGOING_CALL_STATES);
            if (outgoingCall == null) {
                outgoingCall = getDialingCdmaCall();
            }

            if (outgoingCall != null) {
            /// @}
                if (isEmergency && !outgoingCall.isEmergencyCall()) {
                    // Disconnect the current outgoing call if it's not an emergency call. If the
                    // user tries to make two outgoing calls to different emergency call numbers,
                    // we will try to connect the first outgoing call.
                    call.getAnalytics().setCallIsAdditional(true);
                    outgoingCall.getAnalytics().setCallIsInterrupted(true);
                    outgoingCall.disconnect();
                    return true;
                }
                if (outgoingCall.getState() == CallState.SELECT_PHONE_ACCOUNT) {
                    // If there is an orphaned call in the {@link CallState#SELECT_PHONE_ACCOUNT}
                    // state, just disconnect it since the user has explicitly started a new call.
                    call.getAnalytics().setCallIsAdditional(true);
                    outgoingCall.getAnalytics().setCallIsInterrupted(true);
                    outgoingCall.disconnect();
                    return true;
                }
                return false;
            }

            if (hasMaximumHoldingCalls()) {
                // There is no more room for any more calls, unless it's an emergency.
                if (isEmergency) {
                    // Kill the current active call, this is easier then trying to disconnect a
                    // holding call and hold an active call.
                    call.getAnalytics().setCallIsAdditional(true);
                    liveCall.getAnalytics().setCallIsInterrupted(true);
                    liveCall.disconnect();
                    return true;
                }
                return false;  // No more room!
            }

            /// M: ALPS02445100. We should not dial another call when there exists
            /// a dialing cdma call. @{
            if (getDialingCdmaCall() != null) {
                return false;
            }
            /// @}

            // We have room for at least one more holding call at this point.

            // TODO: Remove once b/23035408 has been corrected.
            // If the live call is a conference, it will not have a target phone account set.  This
            // means the check to see if the live call has the same target phone account as the new
            // call will not cause us to bail early.  As a result, we'll end up holding the
            // ongoing conference call.  However, the ConnectionService is already doing that.  This
            // has caused problems with some carriers.  As a workaround until b/23035408 is
            // corrected, we will try and get the target phone account for one of the conference's
            // children and use that instead.
            PhoneAccountHandle liveCallPhoneAccount = liveCall.getTargetPhoneAccount();
            if (liveCallPhoneAccount == null && liveCall.isConference() &&
                    !liveCall.getChildCalls().isEmpty()) {
                liveCallPhoneAccount = getFirstChildPhoneAccount(liveCall);
                Log.i(this, "makeRoomForOutgoingCall: using child call PhoneAccount = " +
                        liveCallPhoneAccount);
            }

            // First thing, if we are trying to make a call with the same phone account as the live
            // call, then allow it so that the connection service can make its own decision about
            // how to handle the new call relative to the current one.
            if (Objects.equals(liveCallPhoneAccount, call.getTargetPhoneAccount())) {
                Log.i(this, "makeRoomForOutgoingCall: phoneAccount matches.");
                call.getAnalytics().setCallIsAdditional(true);
                liveCall.getAnalytics().setCallIsInterrupted(true);
                return true;
            } else if (call.getTargetPhoneAccount() == null) {
                // Without a phone account, we can't say reliably that the call will fail.
                // If the user chooses the same phone account as the live call, then it's
                // still possible that the call can be made (like with CDMA calls not supporting
                // hold but they still support adding a call by going immediately into conference
                // mode). Return true here and we'll run this code again after user chooses an
                // account.
                return true;
            }

            // Try to hold the live call before attempting the new outgoing call.
            if (liveCall.can(Connection.CAPABILITY_HOLD)) {
                Log.i(this, "makeRoomForOutgoingCall: holding live call.");
                call.getAnalytics().setCallIsAdditional(true);
                liveCall.getAnalytics().setCallIsInterrupted(true);
                liveCall.hold();
                return true;
            }

            // The live call cannot be held so we're out of luck here.  There's no room.
            return false;
        }
        return true;
    }

    /**
     * Given a call, find the first non-null phone account handle of its children.
     *
     * @param parentCall The parent call.
     * @return The first non-null phone account handle of the children, or {@code null} if none.
     */
    private PhoneAccountHandle getFirstChildPhoneAccount(Call parentCall) {
        for (Call childCall : parentCall.getChildCalls()) {
            PhoneAccountHandle childPhoneAccount = childCall.getTargetPhoneAccount();
            if (childPhoneAccount != null) {
                return childPhoneAccount;
            }
        }
        return null;
    }

    /**
     * Checks to see if the call should be on speakerphone and if so, set it.
     */
    private void maybeMoveToSpeakerPhone(Call call) {
        if (call.getStartWithSpeakerphoneOn()) {
            setAudioRoute(CallAudioState.ROUTE_SPEAKER);
            call.setStartWithSpeakerphoneOn(false);
        }
    }

    /**
     * Creates a new call for an existing connection.
     *
     * @param callId The id of the new call.
     * @param connection The connection information.
     * @return The new call.
     */
    Call createCallForExistingConnection(String callId, ParcelableConnection connection) {
        boolean isDowngradedConference = (connection.getConnectionProperties()
                & Connection.PROPERTY_IS_DOWNGRADED_CONFERENCE) != 0;
        Call call = new Call(
                callId,
                mContext,
                this,
                mLock,
                mConnectionServiceRepository,
                mContactsAsyncHelper,
                mCallerInfoAsyncQueryFactory,
                mPhoneNumberUtilsAdapter,
                connection.getHandle() /* handle */,
                null /* gatewayInfo */,
                null /* connectionManagerPhoneAccount */,
                connection.getPhoneAccount(), /* targetPhoneAccountHandle */
                Call.CALL_DIRECTION_UNDEFINED /* callDirection */,
                false /* forceAttachToExistingConnection */,
                isDowngradedConference /* isConference */,
                connection.getConnectTimeMillis() /* connectTimeMillis */);

        call.initAnalytics();
        call.getAnalytics().setCreatedFromExistingConnection(true);

        setCallState(call, Call.getStateFromConnectionState(connection.getState()),
                "existing connection");
        call.setConnectionCapabilities(connection.getConnectionCapabilities());
        call.setConnectionProperties(connection.getConnectionProperties());
        call.setCallerDisplayName(connection.getCallerDisplayName(),
                connection.getCallerDisplayNamePresentation());
        call.addListener(this);

        // In case this connection was added via a ConnectionManager, keep track of the original
        // Connection ID as created by the originating ConnectionService.
        Bundle extras = connection.getExtras();
        if (extras != null && extras.containsKey(Connection.EXTRA_ORIGINAL_CONNECTION_ID)) {
            call.setOriginalConnectionId(extras.getString(Connection.EXTRA_ORIGINAL_CONNECTION_ID));
        }

        ///M: For ECC retry @{
        // TeleService will do auto retry for ECC, it creates a new connection for Telecom.
        // If the ECC retry happened during MD reset, then we should do a audio mode reset
        // by speech driver request.
        if (call.isEmergencyCall() &&
                (getForegroundCall() != null && getForegroundCall().isEmergencyCall())) {
            mCallAudioManager.resetAudioMode();
        }
        /// @}

        addCall(call);

        return call;
    }

    /**
     * Determines whether Telecom already knows about a Connection added via the
     * {@link android.telecom.ConnectionService#addExistingConnection(PhoneAccountHandle,
     * Connection)} API via a ConnectionManager.
     *
     * See {@link Connection#EXTRA_ORIGINAL_CONNECTION_ID}.
     * @param originalConnectionId The new connection ID to check.
     * @return {@code true} if this connection is already known by Telecom.
     */
    Call getAlreadyAddedConnection(String originalConnectionId) {
        Optional<Call> existingCall = mCalls.stream()
                .filter(call -> originalConnectionId.equals(call.getOriginalConnectionId()) ||
                            originalConnectionId.equals(call.getId()))
                .findFirst();

        if (existingCall.isPresent()) {
            Log.i(this, "isExistingConnectionAlreadyAdded - call %s already added with id %s",
                    originalConnectionId, existingCall.get().getId());
            return existingCall.get();
        }

        return null;
    }

    /**
     * @return A new unique telecom call Id.
     */
    private String getNextCallId() {
        synchronized(mLock) {
            return TELECOM_CALL_ID_PREFIX + (++mCallId);
        }
    }

    /**
     * Callback when foreground user is switched. We will reload missed call in all profiles
     * including the user itself. There may be chances that profiles are not started yet.
     */
    void onUserSwitch(UserHandle userHandle) {
        mCurrentUserHandle = userHandle;
        mMissedCallNotifier.setCurrentUserHandle(userHandle);
        final UserManager userManager = UserManager.get(mContext);
        List<UserInfo> profiles = userManager.getEnabledProfiles(userHandle.getIdentifier());
        for (UserInfo profile : profiles) {
            reloadMissedCallsOfUser(profile.getUserHandle());
        }
    }

    /**
     * Because there may be chances that profiles are not started yet though its parent user is
     * switched, we reload missed calls of profile that are just started here.
     */
    void onUserStarting(UserHandle userHandle) {
        if (UserUtil.isProfile(mContext, userHandle)) {
            reloadMissedCallsOfUser(userHandle);
        }
    }

    public TelecomSystem.SyncRoot getLock() {
        return mLock;
    }

    /// M: Change to public for calling by BootCompletedBroadcastListener
    public void reloadMissedCallsOfUser(UserHandle userHandle) {
        mMissedCallNotifier.reloadFromDatabase(
                mLock, this, mContactsAsyncHelper, mCallerInfoAsyncQueryFactory, userHandle);
    }

    /**
     * Dumps the state of the {@link CallsManager}.
     *
     * @param pw The {@code IndentingPrintWriter} to write the state to.
     */
    public void dump(IndentingPrintWriter pw) {
        mContext.enforceCallingOrSelfPermission(android.Manifest.permission.DUMP, TAG);
        if (mCalls != null) {
            pw.println("mCalls: ");
            pw.increaseIndent();
            for (Call call : mCalls) {
                pw.println(call);
            }
            pw.decreaseIndent();
        }

        if (mCallAudioManager != null) {
            pw.println("mCallAudioManager:");
            pw.increaseIndent();
            mCallAudioManager.dump(pw);
            pw.decreaseIndent();
        }

        if (mTtyManager != null) {
            pw.println("mTtyManager:");
            pw.increaseIndent();
            mTtyManager.dump(pw);
            pw.decreaseIndent();
        }

        if (mInCallController != null) {
            pw.println("mInCallController:");
            pw.increaseIndent();
            mInCallController.dump(pw);
            pw.decreaseIndent();
        }

        if (mConnectionServiceRepository != null) {
            pw.println("mConnectionServiceRepository:");
            pw.increaseIndent();
            mConnectionServiceRepository.dump(pw);
            pw.decreaseIndent();
        }
    }

    /**
    * For some disconnected causes, we show a dialog when it's a mmi code or potential mmi code.
    *
    * @param call The call.
    */
    private void maybeShowErrorDialogOnDisconnect(Call call) {
        if (call.getState() == CallState.DISCONNECTED && (isPotentialMMICode(call.getHandle())
                || isPotentialInCallMMICode(call.getHandle()))) {
            DisconnectCause disconnectCause = call.getDisconnectCause();
            if (!TextUtils.isEmpty(disconnectCause.getDescription()) && (disconnectCause.getCode()
                    == DisconnectCause.ERROR)) {
                Intent errorIntent = new Intent(mContext, ErrorDialogActivity.class);
                errorIntent.putExtra(ErrorDialogActivity.ERROR_MESSAGE_STRING_EXTRA,
                        disconnectCause.getDescription());
                errorIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivityAsUser(errorIntent, UserHandle.CURRENT);
                // M: fix CR:ALPS03482479,when in dsda exist Active call ,add mmi,
                // mmi call(select_phone_account) will send to ui,if disconnectCause
                // is error or restricted,and description is not null,ui will show alert
                // dialog,and disconnectCause is error,telecom will show alert dialog also.
                // so add logic,if telecom show dialog,will set disconnectCause description
                // is null,so ensure not show in ui again.
                Log.i(this, "set description is null to ensure not show dialog in ui again");
                call.setDisconnectCause(new DisconnectCause(disconnectCause.getCode(),
                        disconnectCause.getLabel(), null, disconnectCause.getReason(),
                        disconnectCause.getTone()));
            }
        }
    }

    /**
     * Separate one command to two actions. After process the first action, according to the
     * result, continue to handle or cancel the secondary action.
     *
     * @param call: the first action related call.
     */
    public void handleActionProcessComplete(Call call) {
        Log.d(this, "have pending call actions: %s", mPendingCallActions.containsKey(call));
        if (mPendingCallActions.containsKey(call) && (call.getState() == CallState.ON_HOLD
                || call.getState() == CallState.DISCONNECTED)) {
            PendingCallAction pendingAction = removePendingCallAction(call);

            pendingAction.handleActionProcessSuccessful();
        }
    }

    /**
     * Add a new pending call action.
     * @param firstActionCall The first call action will operate on this call
     * @param pendingCall The second call action will operate on this call
     * @param pendingAction
     * @param videoState Only will be used when pending action is answer.
     */
    private void addPendingCallAction(Call firstActionCall, Call pendingCall, String pendingAction,
            int videoState) {
        PendingCallAction pendingCallAction = new PendingCallAction(
                pendingCall,
                pendingAction,
                videoState);
        mPendingCallActions.put(firstActionCall, pendingCallAction);
    }

    /**
     * Remove pending call action from hash map.
     * @param firstActionCall: key for hash map.
     * @return
     */
    private PendingCallAction removePendingCallAction(Call firstActionCall) {
        return (PendingCallAction) mPendingCallActions.remove(firstActionCall);
    }

    /**
     * Keep the info of the secondary pending action of a command.
     */
    private class PendingCallAction {
        public static final String PENDING_ACTION_ANSWER = TelecomManagerEx.OPERATION_ANSWER_CALL;

        private Call mPendingCall;
        private String mPendingAction;
        private int mVideoState;

        public PendingCallAction(Call call, String action, int videoState) {
            mPendingCall = call;
            mPendingAction = action;
            mVideoState = videoState;
        }

        /**
         * To handle the pending call action after action finished successfully.
         */
        public void handleActionProcessSuccessful() {
            Log.d(this, "pending action = %s, call= %s", mPendingAction, mPendingCall);

            if (mPendingAction.equals(PENDING_ACTION_ANSWER)) {
                for (CallsManagerListener listener : mListeners) {
                    listener.onIncomingCallAnswered(mPendingCall);
                }

                // We do not update the UI until we get confirmation of the answer() through
                // {@link #markCallAsActive}.
                if (mPendingCall.getState() == CallState.RINGING) {

                    // After first action finished, do answer.
                    mPendingCall.answer(mVideoState);
                    if (VideoProfile.isVideo(mVideoState) &&
                        !mWiredHeadsetManager.isPluggedIn() &&
                        !mBluetoothManager.isBluetoothAvailable() &&
                        isSpeakerEnabledForVideoCalls()) {
                        mPendingCall.setStartWithSpeakerphoneOn(true);
                    }
                }
            }
        }

        public void handleActionProcessFailed() {
            Log.d(this, "handleActionProcessFailed, call= %s", mPendingCall);
        }
    }

    /**
     * M: The all incoming calls will be sorted according to user's action,
     * since there are more than 1 incoming call exist user may touch to switch
     * any incoming call to the primary screen, the sequence of the incoming call
     * will be changed.
     */
    void setSortedIncomingCallList(List<Call> list) {
        if (list != null) {
            mSortedInComingCallList.clear();
            for (Call call: list) {
                mSortedInComingCallList.add(call);
            }
        }
        for (CallsManagerListener listener : mListeners) {
            listener.onInComingCallListChanged(list);
        }
    }
    /// @}

    /**
     * Broadcast the connection lost of the call.
     *
     * @param call: the related call.
     */
    void notifyConnectionLost(Call call) {
        Log.d(this, "notifyConnectionLost, call:%s", call);
        for (CallsManagerListener listener : mListeners) {
            listener.onConnectionLost(call);
        }
    }

    /**
     * Clear the pending call action if the first action failed.
     *
     * @param call: the related call.
     */
    void notifyActionFailed(Call call, int action) {
        Log.d(this, "notifyActionFailed, call:%s", call);
        if (mPendingCallActions.containsKey(call)) {
            Log.i(this, "notifyActionFailed, remove pending action");
            PendingCallAction pendingAction = mPendingCallActions.remove(call);
            pendingAction.handleActionProcessFailed();
        }
        SuppMessageHelper suppMessageHelper = new SuppMessageHelper();
        String msg = mContext.getResources()
                .getString(suppMessageHelper.getActionFailedMessageId(action));
        showToastInfomation(msg);
    }

    /**
     * show SS notification.
     *
     * @param call: the related call.
     */
    void notifySSNotificationToast(Call call, int notiType, int type, int code, String number, int index) {
        Log.d(this, "notifySSNotificationToast, call:%s", call);
        String msg = "";
        SuppMessageHelper suppMessageHelper = new SuppMessageHelper();
        if (notiType == 0) {
            msg = suppMessageHelper.getSuppServiceMOString(code, index, number);
        } else if (notiType == 1) {
            String str = "";
            msg = suppMessageHelper.getSuppServiceMTString(code, index);
            if (type == 0x91) {
                if (number != null && number.length() != 0) {
                    str = " +" + number;
                }
            }
            msg = msg + str;
        }
        showToastInfomation(msg);
    }

    /**
     * show SS notification.
     *
     * @param call: the related call.
     */
    void notifyNumberUpdate(Call call, String number) {
        Log.d(this, "notifyNumberUpdate, call:%s", call);
        if (number != null && number.length() != 0) {
            Uri handle = Uri.fromParts(PhoneNumberUtils.isUriNumber(number) ?
                    PhoneAccount.SCHEME_SIP : PhoneAccount.SCHEME_TEL, number, null);
            call.setHandle(handle);
        }
    }

    /**
     * update incoming call info..
     *
     * @param call: the related call.
     */
    void notifyIncomingInfoUpdate(Call call, int type, String alphaid, int cli_validity) {
        Log.d(this, "notifyIncomingInfoUpdate, call:%s", call);
        // The definition of "0 / 1 / 2" is in SuppCrssNotification.java
        int handlePresentation = -1;
        switch (cli_validity) {
            case 0:
                handlePresentation = TelecomManager.PRESENTATION_ALLOWED;
                break;
            case 1:
                handlePresentation = TelecomManager.PRESENTATION_RESTRICTED;
                break;
            case 2:
                handlePresentation = TelecomManager.PRESENTATION_UNKNOWN;
                break;
            default:
                break;
        }
        // TODO: For I'm not sure what is stand for handle, SuppCrssNotification.number, or SuppCrssNotification.alphaid?
        // So I do not update handle here. Need confirm with framework, and re-check this part.
        if (handlePresentation != -1 && call != null) {
            call.setHandle(call.getHandle(), handlePresentation);
        }
    }

    /// M: CC: to notify CDMA MO call is accepted from the remote @{
    void notifyCdmaCallAccepted(Call call) {
        call.setConnectTimeMillis(System.currentTimeMillis());
        Log.d(this, "notifyCdmaCallAccepted, call:%s", call);

        for (CallsManagerListener listener : mListeners) {
            listener.onCdmaCallAccepted(call);
        }

        call.setIsAcceptedCdmaMoCall(true);
        updateCanAddCall();
    }
    /// @}

    /// M: CC: For 3G VT only @{
    /**
     * notify CallAudioManager of the VT status info to set to AudioManager
     *
     * @param call: the related call.
     * @param status: vt status.
     *     - 0: active
     *     - 1: disconnected
     */
    void notifyVtStatusInfo(Call call, int status) {
        Log.d(this, "notifyVtStatusInfo, call:%s", call);
        for (CallsManagerListener listener : mListeners) {
            listener.onVtStatusInfoChanged(call, status);
        }
    }
    /// @}

    public class SuppMessageHelper {
        //action code
        private static final int ACTION_UNKNOWN = 0;
        private static final int ACTION_SWITCH = 1;
        private static final int ACTION_SEPARATE = 2;
        private static final int ACTION_TRANSFER = 3;
        private static final int ACTION_REJECT = 4;
        private static final int ACTION_HANGUP = 5;

        //MO code
        private static final int MO_CODE_UNCONDITIONAL_CF_ACTIVE = 0;
        private static final int MO_CODE_SOME_CF_ACTIVE = 1;
        private static final int MO_CODE_CALL_FORWARDED = 2;
        private static final int MO_CODE_CALL_IS_WAITING = 3;
        private static final int MO_CODE_CUG_CALL = 4;
        private static final int MO_CODE_OUTGOING_CALLS_BARRED = 5;
        private static final int MO_CODE_INCOMING_CALLS_BARRED = 6;
        private static final int MO_CODE_CLIR_SUPPRESSION_REJECTED = 7;
        private static final int MO_CODE_CALL_DEFLECTED = 8;
        private static final int MO_CODE_CALL_FORWARDED_TO = 9;

        //MT code
        private static final int MT_CODE_FORWARDED_CALL = 0;
        private static final int MT_CODE_CUG_CALL = 1;
        private static final int MT_CODE_CALL_ON_HOLD = 2;
        private static final int MT_CODE_CALL_RETRIEVED = 3;
        private static final int MT_CODE_MULTI_PARTY_CALL = 4;
        private static final int MT_CODE_ON_HOLD_CALL_RELEASED = 5;
        private static final int MT_CODE_FORWARD_CHECK_RECEIVED = 6;
        private static final int MT_CODE_CALL_CONNECTING_ECT = 7;
        private static final int MT_CODE_CALL_CONNECTED_ECT = 8;
        private static final int MT_CODE_DEFLECTED_CALL = 9;
        private static final int MT_CODE_ADDITIONAL_CALL_FORWARDED = 10;
        private static final int MT_CODE_FORWARDED_CF = 11;
        private static final int MT_CODE_FORWARDED_CF_UNCOND = 12;
        private static final int MT_CODE_FORWARDED_CF_COND = 13;
        private static final int MT_CODE_FORWARDED_CF_BUSY = 14;
        private static final int MT_CODE_FORWARDED_CF_NO_REPLY = 15;
        private static final int MT_CODE_FORWARDED_CF_NOT_REACHABLE = 16;

        public int getActionFailedMessageId(int action) {
            int errMsgId = -1;
            switch (action) {
            case ACTION_SWITCH:
                errMsgId = R.string.incall_error_supp_service_switch;
                break;
            case ACTION_SEPARATE:
                errMsgId = R.string.incall_error_supp_service_separate;
                break;
            case ACTION_TRANSFER:
                errMsgId = R.string.incall_error_supp_service_transfer;
                break;
            case ACTION_REJECT:
                errMsgId = R.string.incall_error_supp_service_reject;
                break;
            case ACTION_HANGUP:
                errMsgId = R.string.incall_error_supp_service_hangup;
                break;
            case ACTION_UNKNOWN:
            default:
                errMsgId = R.string.incall_error_supp_service_unknown;
                break;
            }
            return errMsgId;
        }

        public String getSuppServiceMOString(int code, int index, String number) {
            String moStr = "";
            switch (code) {
            case MO_CODE_UNCONDITIONAL_CF_ACTIVE:
                moStr = mContext.getResources()
                        .getString(R.string.mo_code_unconditional_cf_active);
                break;
            case MO_CODE_SOME_CF_ACTIVE:
                moStr = mContext.getResources().getString(R.string.mo_code_some_cf_active);
                break;
            case MO_CODE_CALL_FORWARDED:
                moStr = mContext.getResources().getString(R.string.mo_code_call_forwarded);
                break;
            case MO_CODE_CALL_IS_WAITING:
                moStr = mContext.getResources().getString(R.string.call_waiting_indication);
                break;
            case MO_CODE_CUG_CALL:
                moStr = mContext.getResources().getString(R.string.mo_code_cug_call);
                moStr = moStr + " " + index;
                break;
            case MO_CODE_OUTGOING_CALLS_BARRED:
                moStr = mContext.getResources().getString(R.string.mo_code_outgoing_calls_barred);
                break;
            case MO_CODE_INCOMING_CALLS_BARRED:
                moStr = mContext.getResources().getString(R.string.mo_code_incoming_calls_barred);
                break;
            case MO_CODE_CLIR_SUPPRESSION_REJECTED:
                moStr = mContext.getResources().getString(
                        R.string.mo_code_clir_suppression_rejected);
                break;
            case MO_CODE_CALL_DEFLECTED:
                moStr = mContext.getResources().getString(R.string.mo_code_call_deflected);
                break;
            case MO_CODE_CALL_FORWARDED_TO:
                // here we just show "call forwarding...",
                // and number will be updated via pau later if needed.
                moStr = mContext.getResources().getString(R.string.mo_code_call_forwarding);
                break;
            default:
                // Attempt to use a service we don't recognize or support
                // ("Unsupported service" or "Selected service failed")
                moStr = mContext.getResources().getString(
                        R.string.incall_error_supp_service_unknown);
                break;
            }
            return moStr;
        }

        public String getSuppServiceMTString(int code, int index) {
            String mtStr = "";
            switch (code) {
            case MT_CODE_FORWARDED_CALL:
                mtStr = mContext.getResources().getString(R.string.mt_code_forwarded_call);
                break;
            case MT_CODE_CUG_CALL:
                mtStr = mContext.getResources().getString(R.string.mt_code_cug_call);
                mtStr = mtStr + " " + index;
                break;
            case MT_CODE_CALL_ON_HOLD:
                mtStr = mContext.getResources().getString(R.string.mt_code_call_on_hold);
                break;
            case MT_CODE_CALL_RETRIEVED:
                mtStr = mContext.getResources().getString(R.string.mt_code_call_retrieved);
                break;
            case MT_CODE_MULTI_PARTY_CALL:
                mtStr = mContext.getResources().getString(R.string.mt_code_multi_party_call);
                break;
            case MT_CODE_ON_HOLD_CALL_RELEASED:
                mtStr = mContext.getResources().getString(R.string.mt_code_on_hold_call_released);
                break;
            case MT_CODE_FORWARD_CHECK_RECEIVED:
                mtStr = mContext.getResources().getString(R.string.mt_code_forward_check_received);
                break;
            case MT_CODE_CALL_CONNECTING_ECT:
                mtStr = mContext.getResources().getString(R.string.mt_code_call_connecting_ect);
                break;
            case MT_CODE_CALL_CONNECTED_ECT:
                mtStr = mContext.getResources().getString(R.string.mt_code_call_connected_ect);
                break;
            case MT_CODE_DEFLECTED_CALL:
                mtStr = mContext.getResources().getString(R.string.mt_code_deflected_call);
                break;
            case MT_CODE_ADDITIONAL_CALL_FORWARDED:
                mtStr = mContext.getResources().getString(
                        R.string.mt_code_additional_call_forwarded);
                break;
            case MT_CODE_FORWARDED_CF:
                mtStr = mContext.getResources().getString(R.string.mt_code_forwarded_call) + "("
                        + mContext.getResources().getString(R.string.mt_code_forwarded_cf) + ")";
                break;
            case MT_CODE_FORWARDED_CF_UNCOND:
                mtStr = mContext.getResources().getString(R.string.mt_code_forwarded_call) + "("
                        + mContext.getResources().getString(R.string.mt_code_forwarded_cf_uncond)
                        + ")";
                break;
            case MT_CODE_FORWARDED_CF_COND:
                mtStr = mContext.getResources().getString(R.string.mt_code_forwarded_call) + "("
                        + mContext.getResources().getString(R.string.mt_code_forwarded_cf_cond)
                        + ")";
                break;
            case MT_CODE_FORWARDED_CF_BUSY:
                mtStr = mContext.getResources().getString(R.string.mt_code_forwarded_call) + "("
                        + mContext.getResources().getString(R.string.mt_code_forwarded_cf_busy)
                        + ")";
                break;
            case MT_CODE_FORWARDED_CF_NO_REPLY:
                mtStr = mContext.getResources().getString(R.string.mt_code_forwarded_call) + "("
                        + mContext.getResources().getString(R.string.mt_code_forwarded_cf_no_reply)
                        + ")";
                break;
            case MT_CODE_FORWARDED_CF_NOT_REACHABLE:
                mtStr = mContext.getResources().getString(R.string.mt_code_forwarded_call)
                        + "("
                        + mContext.getResources().getString(
                                R.string.mt_code_forwarded_cf_not_reachable) + ")";
                break;
            default:
                // Attempt to use a service we don't recognize or support
                // ("Unsupported service" or "Selected service failed")
                mtStr = mContext.getResources().getString(
                        R.string.incall_error_supp_service_unknown);
                break;
            }
            return mtStr;
        }
    }

    /**
     * M: Post to main thread to show toast.
     */
    private void showToastInfomation(String msg) {
        mToastInformation = msg;
        Runnable toastAlert = new Runnable("Toast.info", mLock) {
            @Override
            public void loggedRun() {
                Toast.makeText(mContext, mToastInformation, Toast.LENGTH_SHORT).show();
            }
        };
        mHandler.post(toastAlert.prepare());
    }

    /**
     * M: Power on/off device when connecting to smart book
     */
    void updatePowerForSmartBook(boolean onOff) {
        PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
        Log.d(TAG, "SmartBook power onOff: " + onOff);
        /*TODO  mark smartbook code to let build pass first.
        if (onOff) {
            pm.wakeUpByReason(SystemClock.uptimeMillis(), PowerManager.WAKE_UP_REASON_SMARTBOOK);
        } else {
            pm.goToSleep(SystemClock.uptimeMillis(), PowerManager.GO_TO_SLEEP_REASON_SMARTBOOK, 0);
        }
        */
    }

    boolean neededForceSpeakerOn() {
        boolean result = false;
        Log.i(TAG, "neededForceSpeakerOn");
        if (android.os.SystemProperties.get("ro.mtk_tb_call_speaker_on").equals("1")) {
            Log.i(TAG, "neededForceSpeakerOn, ro.mtk_tb_call_speaker_on == 1");
            if (!mWiredHeadsetManager.isPluggedIn()
                    && !mBluetoothManager.isBluetoothAvailable()) {
                Log.i(TAG, "neededForceSpeakerOn, ro.mtk_tb_call_speaker_on == 1 && no bt!");
                if (mCallAudioManager.getCallAudioState().getRoute()
                                      != CallAudioState.ROUTE_SPEAKER) {
                    result = true;
                    Log.i(TAG, "neededForceSpeakerOn, set route to speaker");
                }
            }
        }
        return result;
    }

    /**
     * M: Handle explicit call transfer.
     */
    void explicitCallTransfer(Call call) {
        if (call != null) {
            final ConnectionServiceWrapper service = call.getConnectionService();
            service.explicitCallTransfer(call);
        } else {
            Log.w(this, "explicitCallTransfer failed, call is null");
        }
    }

    /**
     * M: Handle blind/assured explicit call transfer.
     */
    void explicitCallTransfer(Call call, String number, int type) {
        if (call != null) {
            final ConnectionServiceWrapper service = call.getConnectionService();
            service.explicitCallTransfer(call, number, type);
        } else {
            Log.w(this, "explicitCallTransfer failed, call is null");
        }
    }

    /**
     * M: Instructs Telecom to hang up all calls.
     */
    public void hangupAll() {
        Log.v(this, "hangupAll");

        for (Call call : mCalls) {
            if (call.getParentCall() != null) {
                continue;
            }
            call.hangupAll();
        }
    }

    /**
     * M: Instructs Telecom to disconnect all ON_HOLD calls.
     */
    public void hangupAllHoldCalls() {
        Log.v(this, "hangupAllHoldCalls");

        for (Call call : mCalls) {
            if (call.getParentCall() != null) {
                continue;
            }
            if (call.getState() == CallState.ON_HOLD) {
                /// M: [log optimize]
                Log.disconnectEvent(call, "CallsManager.hangupAllHoldCalls");
                disconnectCall(call);
            }
        }
    }

    /**
     * M: Instructs Telecom to disconnect active call and answer waiting call.
     */
    public void hangupActiveAndAnswerWaiting() {
        Log.v(this, "hangupActiveAndAnswerWaiting");
        Call ringingCall;
        if (mSortedInComingCallList.size() > 1) {
            ringingCall = mSortedInComingCallList.get(0);
        } else {
            ringingCall = getRingingCall();
        }
        if (!mCalls.contains(ringingCall)) {
            Log.d(this, "Request to answer a non-existent call %s", ringingCall);
            return;
        }
        Call foregroundCall = getForegroundCall();
        if (foregroundCall != null && foregroundCall.isActive()) {
            mPendingCallActions.put(foregroundCall, new PendingCallAction(ringingCall,
                    PendingCallAction.PENDING_ACTION_ANSWER, ringingCall.getVideoState()));

            foregroundCall.disconnect(PendingCallAction.PENDING_ACTION_ANSWER);
        }
    }

    /**
     * M: [ALPS01798317]: judge whether all calls are ringing call
     * @return true: all calls are ringing.
     */
    public boolean isAllCallRinging() {
        for (Call call : mCalls) {
            if (call.getState() != CallState.RINGING) {
                return false;
            }
        }

        return true;
    }

    // expose API of isPotentialInCallMMICode() and isPotentialInCallMMICode() to other package.
    public boolean isPotentialMMIOrInCallMMI(Uri handle) {
        return isPotentialMMICode(handle) || isPotentialInCallMMICode(handle);
    }
    /// @}

    /// M: CC: For 3G VT only @{
    /**
     * Here judge whether can accept a new call(MO / MT) based on logic of that
     * video call can not co-exist with any other call. Eg, 3G VT.
     * @param isVideoRequest
     * @return
     */
    public boolean shouldBlockFor3GVT(boolean isVideoRequest) {
        boolean result = false;
        // The logic is only for 3G VT.
        if (TelecomUtils.isSupport3GVT()) {
            if (isVideoRequest) {
                // if new call is video call, check if exist any valid call.
                for (Call call : mCalls) {
                    if (call.getState() != CallState.SELECT_PHONE_ACCOUNT) {
                        result = true;
                        break;
                    }
                }
            } else {
                // if new call is voice call, check if exist any valid video call.
                for (Call call : mCalls) {
                    if (call.getState() != CallState.SELECT_PHONE_ACCOUNT
                            && VideoProfile.isVideo(call.getVideoState())) {
                        result = true;
                        break;
                    }
                }
            }
        }
        return result;
    }
    /// @}

    /**
     * Check whether we should block the ViLTE request(MO/MT),
     * if exist any video call, block any request on same SIM;
     * if exist any non-video call, block any video request on same SIM.
     * TODO: should combine with 3G VT check?
     * @param newCall
     * @return
     */
    public boolean shouldBlockForCertainViLTE(Call newCall) {
        boolean result = false;
        PhoneAccountHandle accountHandle = newCall.getTargetPhoneAccount();
        boolean singletonVilte = newCall.allowsSingletonViLTE();
        Log.d(this, "newCall / accountHandle / singletonVilte = %s / %s / %s",
                newCall, accountHandle, singletonVilte);
        if (singletonVilte && accountHandle != null) {
            if (newCall.isVideoCall()) {
                // if new call is video call, check if exist any valid call on same SIM.
                for (Call call : mCalls) {
                    if (call.getState() != CallState.SELECT_PHONE_ACCOUNT
                            && Objects.equals(accountHandle, call.getTargetPhoneAccount())) {
                        result = true;
                        break;
                    }
                }
            } else {
                // if new call is voice call, check if exist any valid video call on same SIM.
                for (Call call : mCalls) {
                    if (call.getState() != CallState.SELECT_PHONE_ACCOUNT
                            && Objects.equals(accountHandle, call.getTargetPhoneAccount())
                            && VideoProfile.isVideo(call.getVideoState())) {
                        result = true;
                        break;
                    }
                }
            }
        }
        Log.d(this, "shouldBlockForCertainViLTE()...result = %s", result);
        return result;
    }

    /**
     * M: Prevent dialing from another SIM based account if already exist a call on
     * SIM account for Dsds project.
     *
     * @param call
     * @return
     */
    private boolean preventCallFromOtherSimBasedAccountForDsds(Call call) {
        if (!TelecomUtils.isInDsdaMode() && call.getTargetPhoneAccount() != null
                && TelephonyUtil.isPstnComponentName(call.getTargetPhoneAccount()
                .getComponentName())) {

            for (Call otherCall : mCalls) {
                if (Objects.equals(otherCall, call)) {
                    continue;
                }
                PhoneAccountHandle otherAccount = otherCall.getTargetPhoneAccount();
                if (otherAccount != null
                        && TelephonyUtil.isPstnComponentName(otherAccount.getComponentName())
                        && !Objects.equals(otherAccount, call.getTargetPhoneAccount())) {
                    Log.d(this, "Need to stop dialing a second call from other sim.");
                    return true;
                }
            }
        }
        return false;
    }

    /// M: ALPS02302619 @{
    /**
     * Get the Ecc call
     * @return Ecc call if exists, or null
     */
    Call getEmergencyCall() {
        for (Call call : mCalls) {
            if (call.isEmergencyCall()) {
                Log.d(this, "get Emergency call: " + call);
                return call;
            }
        }
        return null;
    }
    /// @}

    /// M: ALPS02512168 @{
    /**
     * has voip call
     * @return true: voip call exist, or false
     */
    boolean hasVoipCall() {
        for (Call call: mCalls) {
            if (call.getIsVoipAudioMode()) {
                Log.i(this, "has voip call: " + call);
                return true;
            }
        }
        return false;
    }
    /// @}

    /**
     * M: Returns all the calls that it finds with the given states.
     *
     * @param states
     * @return Call List
     */
    public List<Call> getCallsWithStates(int... states) {
        List<Call> callList = new ArrayList<Call>();
        for (int currentState : states) {
            for (Call call : mCalls) {
                if (currentState == call.getState()) {
                    // Only find the top-level calls, skip a conference child call.
                    if (call.getParentCall() == null) {
                        callList.add(call);
                    }
                }
            }
        }
        return callList;
    }

    /// M: Get sorted Incoming and Holding call lists
    List<Call> getSortedInComingCallList() {
        return mSortedInComingCallList;
    }

    /**
     * M: Check if two calls are in a same phone account.
     * @param firstCall
     * @param secondCall
     * @return
     */
    public boolean isInSamePhoneAccount(Call firstCall, Call secondCall) {
        if (firstCall == null || secondCall == null) {
            return false;
        }
        return Objects.equals(firstCall.getTargetPhoneAccount(),
                secondCall.getTargetPhoneAccount());
    }

    /**
     * M: To check whether have 1A1W in another phone account: 1A1W(Account1) + 1I(Account2).
     * In this case, answer the ringing call in account2, need to hang up the active call.
     * Because if hold the active call also will answer the waiting call in account1.
     * @return
     */
    private boolean has1A1WInAnotherPhoneAccount(Call ringCall) {
        Call activeCall = getActiveCall();
        if (activeCall != null && !isInSamePhoneAccount(activeCall, ringCall)) {
            List<Call> ringCallList = getCallsWithStates(CallState.RINGING);
            for (Call otherRingCall : ringCallList) {
                if (otherRingCall != ringCall && isInSamePhoneAccount(activeCall, otherRingCall)) {
                    Log.d(this, "has1A1WInAnotherPhoneAccount for current ringCall: %s", ringCall);
                    return true;
                }
            }
        }
        return false;
    }

    private void setIntentExtrasAndStartTime(Call call, Bundle extras) {
      // Create our own instance to modify (since extras may be Bundle.EMPTY)
      extras = new Bundle(extras);

      // Specifies the time telecom began routing the call. This is used by the dialer for
      // analytics.
      extras.putLong(TelecomManager.EXTRA_CALL_TELECOM_ROUTING_START_TIME_MILLIS,
              SystemClock.elapsedRealtime());

      call.setIntentExtras(extras);
    }

    ///M: to get the dialing CDMA call exists, there should only be one at most
    private Call getDialingCdmaCall() {
        //Dialing CDMA CALL always have a fake active state
        for (Call call : getCallsWithStates(CallState.ACTIVE)) {
            if(call.isCdmaDialingCall()){
                Log.d(this, "getDialingCdmaCall: %s", call);
                return call;
            }
        }
        return null;
    }
    ///@}

    ///M: [EccDisconnectAll] to check whether have pending Ecc. @{
    public boolean hasPendingEcc() {
        return mPendingEccCall != null;
    }
    ///@}

    ///M: judge whether there has active call @{
    public boolean hasActiveCall() {
        for (Call call : mCalls) {
            if (call.getState() == CallState.ACTIVE) {
                return true;
            }
        }
        return false;
    }
    /// @}
}
