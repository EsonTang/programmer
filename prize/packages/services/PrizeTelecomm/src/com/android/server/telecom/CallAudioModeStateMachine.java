/*
 * Copyright (C) 2015 The Android Open Source Project
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

package com.android.server.telecom;

import java.io.FileWriter;

import android.content.Context;
import android.media.AudioManager;
import android.os.Message;
import android.os.SystemProperties;
import android.telecom.PhoneAccountHandle;
import android.telecom.PhoneAccount;
import android.telecom.TelecomManager;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.SparseArray;

import com.android.internal.util.IState;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;

import com.android.internal.telephony.PhoneConstants;

import com.mediatek.telecom.TelecomUtils;

public class CallAudioModeStateMachine extends StateMachine {
    public static class MessageArgs {
        public boolean hasActiveOrDialingCalls;
        public boolean hasRingingCalls;
        public boolean hasHoldingCalls;
        public boolean isTonePlaying;
        public boolean foregroundCallIsVoip;
        public Session session;

        public MessageArgs(boolean hasActiveOrDialingCalls, boolean hasRingingCalls,
                boolean hasHoldingCalls, boolean isTonePlaying, boolean foregroundCallIsVoip,
                Session session) {
            this.hasActiveOrDialingCalls = hasActiveOrDialingCalls;
            this.hasRingingCalls = hasRingingCalls;
            this.hasHoldingCalls = hasHoldingCalls;
            this.isTonePlaying = isTonePlaying;
            this.foregroundCallIsVoip = foregroundCallIsVoip;
            this.session = session;
        }

        public MessageArgs() {
            this.session = Log.createSubsession();
        }

        @Override
        public String toString() {
            return "MessageArgs{" +
                    "hasActiveCalls=" + hasActiveOrDialingCalls +
                    ", hasRingingCalls=" + hasRingingCalls +
                    ", hasHoldingCalls=" + hasHoldingCalls +
                    ", isTonePlaying=" + isTonePlaying +
                    ", foregroundCallIsVoip=" + foregroundCallIsVoip +
                    ", session=" + session +
                    '}';
        }
    }

    public static final int INITIALIZE = 1;
    // These ENTER_*_FOCUS commands are for testing.
    public static final int ENTER_CALL_FOCUS_FOR_TESTING = 2;
    public static final int ENTER_COMMS_FOCUS_FOR_TESTING = 3;
    public static final int ENTER_RING_FOCUS_FOR_TESTING = 4;
    public static final int ENTER_TONE_OR_HOLD_FOCUS_FOR_TESTING = 5;
    public static final int ABANDON_FOCUS_FOR_TESTING = 6;

    public static final int NO_MORE_ACTIVE_OR_DIALING_CALLS = 1001;
    public static final int NO_MORE_RINGING_CALLS = 1002;
    public static final int NO_MORE_HOLDING_CALLS = 1003;

    public static final int NEW_ACTIVE_OR_DIALING_CALL = 2001;
    public static final int NEW_RINGING_CALL = 2002;
    public static final int NEW_HOLDING_CALL = 2003;
    public static final int MT_AUDIO_SPEEDUP_FOR_RINGING_CALL = 2004;

    public static final int TONE_STARTED_PLAYING = 3001;
    public static final int TONE_STOPPED_PLAYING = 3002;

    public static final int FOREGROUND_VOIP_MODE_CHANGE = 4001;
    /// M: For 2A -> 1A @{
    public static final int FOREGROUND_MODE_CHANGE = 4501;
    /// @}

    public static final int RUN_RUNNABLE = 9001;

    private static final SparseArray<String> MESSAGE_CODE_TO_NAME = new SparseArray<String>() {{
        put(ENTER_CALL_FOCUS_FOR_TESTING, "ENTER_CALL_FOCUS_FOR_TESTING");
        put(ENTER_COMMS_FOCUS_FOR_TESTING, "ENTER_COMMS_FOCUS_FOR_TESTING");
        put(ENTER_RING_FOCUS_FOR_TESTING, "ENTER_RING_FOCUS_FOR_TESTING");
        put(ENTER_TONE_OR_HOLD_FOCUS_FOR_TESTING, "ENTER_TONE_OR_HOLD_FOCUS_FOR_TESTING");
        put(ABANDON_FOCUS_FOR_TESTING, "ABANDON_FOCUS_FOR_TESTING");
        put(NO_MORE_ACTIVE_OR_DIALING_CALLS, "NO_MORE_ACTIVE_OR_DIALING_CALLS");
        put(NO_MORE_RINGING_CALLS, "NO_MORE_RINGING_CALLS");
        put(NO_MORE_HOLDING_CALLS, "NO_MORE_HOLDING_CALLS");
        put(NEW_ACTIVE_OR_DIALING_CALL, "NEW_ACTIVE_OR_DIALING_CALL");
        put(NEW_RINGING_CALL, "NEW_RINGING_CALL");
        put(NEW_HOLDING_CALL, "NEW_HOLDING_CALL");
        put(MT_AUDIO_SPEEDUP_FOR_RINGING_CALL, "MT_AUDIO_SPEEDUP_FOR_RINGING_CALL");
        put(TONE_STARTED_PLAYING, "TONE_STARTED_PLAYING");
        put(TONE_STOPPED_PLAYING, "TONE_STOPPED_PLAYING");
        put(FOREGROUND_VOIP_MODE_CHANGE, "FOREGROUND_VOIP_MODE_CHANGE");
        /// M: Abandon focus state for ECC retry @{
        put(ABANDON_FOCUS, "ABANDON_FOCUS");
        /// @}

        put(RUN_RUNNABLE, "RUN_RUNNABLE");
    }};

    public static final String TONE_HOLD_STATE_NAME = OtherFocusState.class.getSimpleName();
    public static final String UNFOCUSED_STATE_NAME = UnfocusedState.class.getSimpleName();
    public static final String CALL_STATE_NAME = SimCallFocusState.class.getSimpleName();
    public static final String RING_STATE_NAME = RingingFocusState.class.getSimpleName();
    public static final String COMMS_STATE_NAME = VoipCallFocusState.class.getSimpleName();

    private class BaseState extends State {
        @Override
        public boolean processMessage(Message msg) {
            switch (msg.what) {
                case ENTER_CALL_FOCUS_FOR_TESTING:
                    transitionTo(mSimCallFocusState);
                    return HANDLED;
                case ENTER_COMMS_FOCUS_FOR_TESTING:
                    transitionTo(mVoipCallFocusState);
                    return HANDLED;
                case ENTER_RING_FOCUS_FOR_TESTING:
                    transitionTo(mRingingFocusState);
                    return HANDLED;
                case ENTER_TONE_OR_HOLD_FOCUS_FOR_TESTING:
                    transitionTo(mOtherFocusState);
                    return HANDLED;
                case ABANDON_FOCUS_FOR_TESTING:
                ///M: ALPS02797621 Ecc retry, need to back to normal mode @{
                case ABANDON_FOCUS:
                /// @}
                    transitionTo(mUnfocusedState);
                    return HANDLED;
                case INITIALIZE:
                    mIsInitialized = true;
                    return HANDLED;
                case RUN_RUNNABLE:
                    java.lang.Runnable r = (java.lang.Runnable) msg.obj;
                    r.run();
                    return HANDLED;
                default:
                    return NOT_HANDLED;
            }
        }
    }

    private class UnfocusedState extends BaseState {
        @Override
        public void enter() {
            if (mIsInitialized) {
                Log.i(LOG_TAG, "Abandoning audio focus: now UNFOCUSED");

                mAudioManager.abandonAudioFocusForCall();
                mAudioManager.setMode(AudioManager.MODE_NORMAL);

                mMostRecentMode = AudioManager.MODE_NORMAL;
                ///M write call state to driver file @{
                updateAudioModeToKpdDriver(mMostRecentMode);
                /// @}

                ///M: ALPS02797621 @{
                // this change is to avoid reset audio path when ecc retry.
                ///M: ALPS03016322 @{
                // this change is to reset audio path when normal call is not null.
                Call foregroundCall = mCallAudioManager.getForegroundCall();
                Log.i(LOG_TAG, "foregroundCall = " + foregroundCall);
                if ((foregroundCall == null)/// @}
                        || (foregroundCall != null && !foregroundCall.isEmergencyCall())) {/// @}
                    mCallAudioManager.
                    setCallAudioRouteFocusState(CallAudioRouteStateMachine.NO_FOCUS);
                }
            }
        }

        @Override
        public boolean processMessage(Message msg) {
            if (super.processMessage(msg) == HANDLED) {
                return HANDLED;
            }
            MessageArgs args = (MessageArgs) msg.obj;
            switch (msg.what) {
                case NO_MORE_ACTIVE_OR_DIALING_CALLS:
                    // Do nothing.
                    return HANDLED;
                case NO_MORE_RINGING_CALLS:
                    // Do nothing.
                    return HANDLED;
                case NO_MORE_HOLDING_CALLS:
                    // Do nothing.
                    return HANDLED;
                case NEW_ACTIVE_OR_DIALING_CALL:
                    transitionTo(args.foregroundCallIsVoip
                            ? mVoipCallFocusState : mSimCallFocusState);
                    return HANDLED;
                case NEW_RINGING_CALL:
                    transitionTo(mRingingFocusState);
                    return HANDLED;
                case NEW_HOLDING_CALL:
                    // This really shouldn't happen, but transition to the focused state anyway.
                    Log.w(LOG_TAG, "Call was surprisingly put into hold from an unknown state." +
                            " Args are: \n" + args.toString());
                    transitionTo(mOtherFocusState);
                    return HANDLED;
                case TONE_STARTED_PLAYING:
                    // This shouldn't happen either, but perform the action anyway.
                    Log.w(LOG_TAG, "Tone started playing unexpectedly. Args are: \n"
                            + args.toString());
                    return HANDLED;
                default:
                    // The forced focus switch commands are handled by BaseState.
                    return NOT_HANDLED;
            }
        }
    }

    private class RingingFocusState extends BaseState {
        @Override
        public void enter() {
            Log.i(LOG_TAG, "Audio focus entering RINGING state");
            if (mCallAudioManager.startRinging()) {
                mAudioManager.requestAudioFocusForCall(AudioManager.STREAM_RING,
                        AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
                if (mMostRecentMode == AudioManager.MODE_IN_CALL) {
                    // Preserving behavior from the old CallAudioManager.
                    Log.i(LOG_TAG, "Transition from IN_CALL -> RINGTONE."
                            + "  Resetting to NORMAL first.");
                    mAudioManager.setMode(AudioManager.MODE_NORMAL);
                }
                mAudioManager.setMode(AudioManager.MODE_RINGTONE);
                mCallAudioManager.setCallAudioRouteFocusState(CallAudioRouteStateMachine.RINGING_FOCUS);
            } else {
                Log.i(LOG_TAG, "Entering RINGING but not acquiring focus -- silent ringtone");
                // prize add by xiarui, PRIZE_LED_BLINK, for flash light control when incoming call 2018-08-03 start
                if (mCallFlashLightManager != null) {
                    Log.i(LOG_TAG, "start blink -- silent ringtone");
                    mCallFlashLightManager.startBlink();
                }
                // prize add by xiarui, PRIZE_LED_BLINK, for flash light control when incoming call 2018-08-03 end
            }

            mCallAudioManager.stopCallWaiting();

            ///M write call state to driver file @{
            updateAudioModeToKpdDriver(mMostRecentMode);
            /// @}
        }

        @Override
        public void exit() {
            // Audio mode and audio stream will be set by the next state.
            mCallAudioManager.stopRinging();
            // prize add by xiarui, PRIZE_LED_BLINK, for flash light control when incoming call 2018-08-03 start
            if (mCallFlashLightManager != null) {
                Log.i(LOG_TAG, "stop blink -- silent ringtone");
                mCallFlashLightManager.stopBlink();
            }
            // prize add by xiarui, PRIZE_LED_BLINK, for flash light control when incoming call 2018-08-03 end
        }

        @Override
        public boolean processMessage(Message msg) {
            if (super.processMessage(msg) == HANDLED) {
                return HANDLED;
            }
            MessageArgs args = (MessageArgs) msg.obj;
            switch (msg.what) {
                case NO_MORE_ACTIVE_OR_DIALING_CALLS:
                    // Do nothing. Loss of an active call should not impact ringer.
                    return HANDLED;
                case NO_MORE_HOLDING_CALLS:
                    // Do nothing and keep ringing.
                    return HANDLED;
                case NO_MORE_RINGING_CALLS:
                    // If there are active or holding calls, switch to the appropriate focus.
                    // Otherwise abandon focus.
                    if (args.hasActiveOrDialingCalls) {
                        if (args.foregroundCallIsVoip) {
                            transitionTo(mVoipCallFocusState);
                        } else {
                            transitionTo(mSimCallFocusState);
                        }
                    } else if (args.hasHoldingCalls || args.isTonePlaying) {
                        transitionTo(mOtherFocusState);
                    } else {
                        transitionTo(mUnfocusedState);
                    }
                    return HANDLED;
                case NEW_ACTIVE_OR_DIALING_CALL:
                    // If a call becomes active suddenly, give it priority over ringing.
                    transitionTo(args.foregroundCallIsVoip
                            ? mVoipCallFocusState : mSimCallFocusState);
                    return HANDLED;
                case NEW_RINGING_CALL:
                    Log.w(LOG_TAG, "Unexpected behavior! New ringing call appeared while in " +
                            "ringing state.");
                    return HANDLED;
                case NEW_HOLDING_CALL:
                    // This really shouldn't happen, but transition to the focused state anyway.
                    Log.w(LOG_TAG, "Call was surprisingly put into hold while ringing." +
                            " Args are: " + args.toString());
                    transitionTo(mOtherFocusState);
                    return HANDLED;
                case MT_AUDIO_SPEEDUP_FOR_RINGING_CALL:
                    // This happens when an IMS call is answered by the in-call UI. Special case
                    // that we have to deal with for some reason.

                    // VOIP calls should never invoke this mechanism, so transition directly to
                    // the sim call focus state.
                    transitionTo(mSimCallFocusState);
                    return HANDLED;
                default:
                    // The forced focus switch commands are handled by BaseState.
                    return NOT_HANDLED;
            }
        }
    }

    private class SimCallFocusState extends BaseState {
        @Override
        public void enter() {
            Log.i(LOG_TAG, "Audio focus entering SIM CALL state");
            mAudioManager.requestAudioFocusForCall(AudioManager.STREAM_VOICE_CALL,
                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);

            Log.d(LOG_TAG, "start to set has_foucs");
            mCallAudioManager.setCallAudioRouteFocusState(CallAudioRouteStateMachine.ACTIVE_FOCUS);
            Log.d(LOG_TAG, "finish to set has_focus");

            /// M: support 3G VT @{
            if (TelecomUtils.isSupport3GVT()) {
                Call fgCall = mCallAudioManager.getForegroundCall();
                if(fgCall != null && fgCall.is3GVideoCall()) {
                    if (isInCallMode(mAudioManager.getMode())) {
                        // handle rare case like ALPS02864007.
                        // change to MODE_NORMAL first to make SetVTSpeechCall = 1 effective.
                        Log.d(LOG_TAG, "SimCallFocusState rare case for 3G VT.");
                        mAudioManager.setMode(AudioManager.MODE_NORMAL);
                    }
                    set3GVTSpeechFlag(true);
                }
            }
            ///@}

            /// M: @{
            // support SVLTE audio mode
            // phone1 --> MODE_IN_CALL, phone2 --> MODE_IN_CALL_2
            if (TelecomUtils.isInDsdaMode()) {
                int oldMode = mAudioManager.getMode();
                int newMode = updateAudioModeWithPhoneAccount(AudioManager.MODE_IN_CALL);
                if (oldMode != newMode) {
                    if ((oldMode == AudioManager.MODE_IN_CALL
                            && newMode == AudioManager.MODE_IN_CALL_2)
                            || (oldMode == AudioManager.MODE_IN_CALL_2
                                    && newMode == AudioManager.MODE_IN_CALL)) {
                        mAudioManager.setMode(AudioManager.MODE_NORMAL);
                    }
                    mAudioManager.setMode(newMode);
                    mMostRecentMode = newMode;
                    ///M: @{
                    // background: mute will auto cancel when set audio mode as normal_mode
                    // scenario: (1) only cdma call exist, hold call-->unhold call
                    // (2) Gsm/Cdma call swap
                    if (isInCallMode(newMode)
                            && mCallAudioManager.getCallAudioState().isMuted()) {
                        Log.d(LOG_TAG, "restore mute state after set audio mode to in call!");
                        mCallAudioManager.restoreMuteOnWhenInCallMode();
                    }
                    /// @}
                }
            /// @}
            } else {
                Log.d(LOG_TAG, "start to set mode");
                mAudioManager.setMode(AudioManager.MODE_IN_CALL);
                Log.d(LOG_TAG, "finish to set mode");
                mMostRecentMode = AudioManager.MODE_IN_CALL;
                ///M: ALPS03328944 @{
                // scenario: answer waiting call, and hangup current call
                // this operation will cause audio mode state change from
                // SimCallFocusState to RingingFocusState
                // and AOSP design "Transition from IN_CALL -> RINGTONE.
                // Resetting to NORMAL first".
                // Mute will auto cancel when set audio mode as normal_mode
                if (mCallAudioManager.getCallAudioState() != null
                        && mCallAudioManager.getCallAudioState().isMuted()) {
                    Log.d(LOG_TAG, "restore mute state after set audio mode to in call!");
                    mCallAudioManager.restoreMuteOnWhenInCallMode();
                }
                /// @}
            }

            ///M write call state to driver file @{
            updateAudioModeToKpdDriver(mMostRecentMode);
            /// @}
        }

        @Override
        public boolean processMessage(Message msg) {
            if (super.processMessage(msg) == HANDLED) {
                return HANDLED;
            }
            MessageArgs args = (MessageArgs) msg.obj;
            switch (msg.what) {
                case NO_MORE_ACTIVE_OR_DIALING_CALLS:
                    // Switch to either ringing, holding, or inactive
                    transitionTo(destinationStateAfterNoMoreActiveCalls(args));
                    return HANDLED;
                case NO_MORE_RINGING_CALLS:
                    // Don't transition state, but stop any call-waiting tones that may have been
                    // playing.
                    if (args.isTonePlaying) {
                        mCallAudioManager.stopCallWaiting();
                    }
                    // If a MT-audio-speedup call gets disconnected by the connection service
                    // concurrently with the user answering it, we may get this message
                    // indicating that a ringing call has disconnected while this state machine
                    // is in the SimCallFocusState.
                    if (!args.hasActiveOrDialingCalls) {
                        transitionTo(destinationStateAfterNoMoreActiveCalls(args));
                    }
                    return HANDLED;
                case NO_MORE_HOLDING_CALLS:
                    // Do nothing.
                    return HANDLED;
                case NEW_ACTIVE_OR_DIALING_CALL:
                    // Do nothing. Already active.
                    return HANDLED;
                case NEW_RINGING_CALL:
                    // Don't make a call ring over an active call, but do play a call waiting tone.
                    mCallAudioManager.startCallWaiting();
                    return HANDLED;
                case NEW_HOLDING_CALL:
                    // Don't do anything now. Putting an active call on hold will be handled when
                    // NO_MORE_ACTIVE_CALLS is processed.
                    return HANDLED;
                case FOREGROUND_VOIP_MODE_CHANGE:
                    if (args.foregroundCallIsVoip) {
                        transitionTo(mVoipCallFocusState);
                    }
                    return HANDLED;
                /// M: For 2A -> 1A @{
                case FOREGROUND_MODE_CHANGE:
                    transitionTo(mSimCallFocusState);
                    return HANDLED;
                /// @}
                default:
                    // The forced focus switch commands are handled by BaseState.
                    return NOT_HANDLED;
            }
        }
    }

    private class VoipCallFocusState extends BaseState {
        @Override
        public void enter() {
            Log.i(LOG_TAG, "Audio focus entering VOIP CALL state");
            mAudioManager.requestAudioFocusForCall(AudioManager.STREAM_VOICE_CALL,
                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
            mAudioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
            mMostRecentMode = AudioManager.MODE_IN_COMMUNICATION;

            mCallAudioManager.setCallAudioRouteFocusState(CallAudioRouteStateMachine.ACTIVE_FOCUS);

            ///M write call state to driver file @{
            updateAudioModeToKpdDriver(mMostRecentMode);
            /// @}

            ///M: ALPS03573205 @{
            // scenario: answer waiting call, and hangup current call
            // this operation will cause audio mode state change from
            // SimCallFocusState to RingingFocusState
            // and AOSP design "Transition from IN_CALL -> RINGTONE.
            // Resetting to NORMAL first".
            // Mute reset to off by AudioManager when set audio mode as normal_mode
            if (mAudioManager.isMicrophoneMute()) {
                Log.d(LOG_TAG, "restore mute state after set audio mode to in call!");
                mCallAudioManager.restoreMute();
            }
            /// @}
        }

        @Override
        public boolean processMessage(Message msg) {
            if (super.processMessage(msg) == HANDLED) {
                return HANDLED;
            }
            MessageArgs args = (MessageArgs) msg.obj;
            switch (msg.what) {
                case NO_MORE_ACTIVE_OR_DIALING_CALLS:
                    // Switch to either ringing, holding, or inactive
                    transitionTo(destinationStateAfterNoMoreActiveCalls(args));
                    return HANDLED;
                case NO_MORE_RINGING_CALLS:
                    // Don't transition state, but stop any call-waiting tones that may have been
                    // playing.
                    if (args.isTonePlaying) {
                        mCallAudioManager.stopCallWaiting();
                    }
                    return HANDLED;
                case NO_MORE_HOLDING_CALLS:
                    // Do nothing.
                    return HANDLED;
                case NEW_ACTIVE_OR_DIALING_CALL:
                    // Do nothing. Already active.
                    return HANDLED;
                case NEW_RINGING_CALL:
                    // Don't make a call ring over an active call, but do play a call waiting tone.
                    mCallAudioManager.startCallWaiting();
                    return HANDLED;
                case NEW_HOLDING_CALL:
                    // Don't do anything now. Putting an active call on hold will be handled when
                    // NO_MORE_ACTIVE_CALLS is processed.
                    return HANDLED;
                case FOREGROUND_VOIP_MODE_CHANGE:
                    if (!args.foregroundCallIsVoip) {
                        transitionTo(mSimCallFocusState);
                    }
                    return HANDLED;
                default:
                    // The forced focus switch commands are handled by BaseState.
                    return NOT_HANDLED;
            }
        }
    }

    /**
     * This class is used for calls on hold and end-of-call tones.
     */
    private class OtherFocusState extends BaseState {
        @Override
        public void enter() {
            Log.i(LOG_TAG, "Audio focus entering TONE/HOLDING state");
            mAudioManager.requestAudioFocusForCall(AudioManager.STREAM_VOICE_CALL,
                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);

            /// M: hold c2k call, need to set audio as normal mode @{
            if (TelecomUtils.isInDsdaMode()) {
                Call foregroundCall = mCallAudioManager.getPossiblyHeldForegroundCall();
                if (foregroundCall != null && foregroundCall.getState() == CallState.ON_HOLD
                        && getPhoneType(getPhoneId(foregroundCall.getTargetPhoneAccount()))
                        == PhoneConstants.PHONE_TYPE_CDMA) {
                    mMostRecentMode = AudioManager.MODE_NORMAL;
                }
            }
            /// @}
            mAudioManager.setMode(mMostRecentMode);
            mCallAudioManager.setCallAudioRouteFocusState(CallAudioRouteStateMachine.ACTIVE_FOCUS);

            ///M write call state to driver file @{
            updateAudioModeToKpdDriver(mMostRecentMode);
            /// @}
        }

        @Override
        public boolean processMessage(Message msg) {
            if (super.processMessage(msg) == HANDLED) {
                return HANDLED;
            }
            MessageArgs args = (MessageArgs) msg.obj;
            switch (msg.what) {
                case NO_MORE_HOLDING_CALLS:
                    if (args.hasActiveOrDialingCalls) {
                        transitionTo(args.foregroundCallIsVoip
                                ? mVoipCallFocusState : mSimCallFocusState);
                    } else if (args.hasRingingCalls) {
                        transitionTo(mRingingFocusState);
                    } else if (!args.isTonePlaying) {
                        transitionTo(mUnfocusedState);
                    }
                    // Do nothing if a tone is playing.
                    return HANDLED;
                case NEW_ACTIVE_OR_DIALING_CALL:
                    transitionTo(args.foregroundCallIsVoip
                            ? mVoipCallFocusState : mSimCallFocusState);
                    return HANDLED;
                case NEW_RINGING_CALL:
                    // Apparently this is current behavior. Should this be the case?
                    transitionTo(mRingingFocusState);
                    return HANDLED;
                case NEW_HOLDING_CALL:
                    // Do nothing.
                    return HANDLED;
                case NO_MORE_RINGING_CALLS:
                    // If there are no more ringing calls in this state, then stop any call-waiting
                    // tones that may be playing.
                    mCallAudioManager.stopCallWaiting();
                    return HANDLED;
                case TONE_STOPPED_PLAYING:
                    transitionTo(destinationStateAfterNoMoreActiveCalls(args));
                default:
                    return NOT_HANDLED;
            }
        }
    }

    private static final String LOG_TAG = CallAudioModeStateMachine.class.getSimpleName();

    private final BaseState mUnfocusedState = new UnfocusedState();
    private final BaseState mRingingFocusState = new RingingFocusState();
    private final BaseState mSimCallFocusState = new SimCallFocusState();
    private final BaseState mVoipCallFocusState = new VoipCallFocusState();
    private final BaseState mOtherFocusState = new OtherFocusState();

    private final AudioManager mAudioManager;
    private CallAudioManager mCallAudioManager;
    // prize add by xiarui, PRIZE_LED_BLINK, for flash light control when incoming call 2018-08-03 start
    private PrizeCallFlashLightManager mCallFlashLightManager;
    // prize add by xiarui, PRIZE_LED_BLINK, for flash light control when incoming call 2018-08-03 end

    private int mMostRecentMode;
    private boolean mIsInitialized = false;

    public CallAudioModeStateMachine(AudioManager audioManager) {
        super(CallAudioModeStateMachine.class.getSimpleName());
        mAudioManager = audioManager;
        mMostRecentMode = AudioManager.MODE_NORMAL;

        addState(mUnfocusedState);
        addState(mRingingFocusState);
        addState(mSimCallFocusState);
        addState(mVoipCallFocusState);
        addState(mOtherFocusState);
        setInitialState(mUnfocusedState);
        start();
        sendMessage(INITIALIZE, new MessageArgs());
    }

    public void setCallAudioManager(CallAudioManager callAudioManager) {
        mCallAudioManager = callAudioManager;
    }

    // prize add by xiarui, PRIZE_LED_BLINK, for flash light control when incoming call 2018-08-03 start
    public void setFlashLightManager(PrizeCallFlashLightManager callFlashLightManager) {
        mCallFlashLightManager = callFlashLightManager;
    }
    // prize add by xiarui, PRIZE_LED_BLINK, for flash light control when incoming call 2018-08-03 end

    public String getCurrentStateName() {
        IState currentState = getCurrentState();
        return currentState == null ? "no state" : currentState.getName();
    }

    public void sendMessageWithArgs(int messageCode, MessageArgs args) {
        sendMessage(messageCode, args);
    }

    @Override
    protected void onPreHandleMessage(Message msg) {
        if (msg.obj != null && msg.obj instanceof MessageArgs) {
            Log.continueSession(((MessageArgs) msg.obj).session, "CAMSM.pM_" + msg.what);
            Log.i(LOG_TAG, "Message received: %s.", MESSAGE_CODE_TO_NAME.get(msg.what));
        } else if (msg.what == RUN_RUNNABLE && msg.obj instanceof Runnable) {
            Log.i(LOG_TAG, "Running runnable for testing");
        } else {
                Log.w(LOG_TAG, "Message sent must be of type nonnull MessageArgs, but got " +
                        (msg.obj == null ? "null" : msg.obj.getClass().getSimpleName()));
                Log.w(LOG_TAG, "The message was of code %d = %s",
                        msg.what, MESSAGE_CODE_TO_NAME.get(msg.what));
        }
    }

    @Override
    protected void onPostHandleMessage(Message msg) {
        Log.endSession();
    }

    private BaseState destinationStateAfterNoMoreActiveCalls(MessageArgs args) {
        if (args.hasHoldingCalls) {
            return mOtherFocusState;
        } else if (args.hasRingingCalls) {
            return mRingingFocusState;
        } else if (args.isTonePlaying) {
            return mOtherFocusState;
        } else {
            return mUnfocusedState;
        }
    }

    // ============================================================================================
    // MTK Audio Mode Enhancement Start
    // ============================================================================================
    public static final int CONNECTION_LOST = 10001;
    public static final int ABANDON_FOCUS = 10002;

    private TelephonyManager mTelephonyManager = TelephonyManager.getDefault();

    private boolean isInCallMode(int mode) {
        return mode == AudioManager.MODE_IN_CALL
        || mode == AudioManager.MODE_IN_CALL_2;
    }

    /**
     * M: support 3G VT @{
     * Set VT speech flag info to AudioManager.
     * SetVTSpeechCall = 1/0 will be set at SimCallFocusState.enter() & onVtStatusInfoChanged().
     * it will not take effect until audio mode is set => SetVTSpeechCall = 1 before MODE_IN_CALL.
     * Flow should be : SetVTSpeechCall = 1 -> MODE_IN_CALL -> SetVTSpeechCall = 0 -> MODE_NORMAL;
     * @param isUp true-> setVTSpeech flag up. false-> down.
     */
    void set3GVTSpeechFlag(Boolean isUp) {
        if (!TelecomUtils.isSupport3GVT()) {
            return;
        }
        if (isUp) {
            Log.d(this, "[setAudioMode]SetVTSpeechCall=1 under 3G VT");
            mAudioManager.setParameters("SetVTSpeechCall=1");
        } else {
            Log.d(this, "[setAudioMode]SetVTSpeechCall=0 under 3G VT");
            mAudioManager.setParameters("SetVTSpeechCall=0");
        }
    }

    /**
     * M: Write audio mode to kpd drive file @{
     * so that kpd can wak up System by Vol. key when phone suspend when talking
     * @param mode: audio mode.
     */
    private void updateAudioModeToKpdDriver(int mode) {
        // Notify driver that call state changed
        // they may need to do something
        final int value = (mode > AudioManager.MODE_RINGTONE) ? 2 : mode;

        new Thread(new Runnable("CAMSM.uAMTKD", null) {
            @Override
            public void loggedRun() {
                // Owner : yucong Xiong
                // Set kpd as wake up source
                // so that kpd can wak up System by Vol. key when phone suspend when talking
                String callStateFilePath2 =
                    String.format("/sys/bus/platform/mtk-kpd/driver/kpd_call_state");
                try {
                    String state2 = String.valueOf(value);
                    FileWriter fw2 = new FileWriter(callStateFilePath2);
                    fw2.write(state2);
                    fw2.close();
                    Log.v(this, "Call state for kpd is  %s" + state2);
                } catch (Exception e) {
                    Log.v(this, "" , e);
                }
            }
        }.prepare()).start();
    }
    /** @}   */

    /**
     * M: Get phone type @{
     * @param phoneId
     * @return phone type
     */
    private int getPhoneType(int phoneId) {
        int subId = SubscriptionManager.getSubIdUsingPhoneId(phoneId);
        int type = mTelephonyManager.getCurrentPhoneType(subId);
        Log.d(this, "getPhoneType, phoneId:" + phoneId + ", subId:" + subId
                + ", phone type:" + type);
        return type;
    }
    /** @}   */

    /**
     * M: Since the getId() of PhoneAccountHandle not stands for
     * subId any more in m0, we need to calculate the subId based
     * on the PhoneAccountHandle instance. @{
     * @param handle
     * @return
     */
    private int getPhoneId(PhoneAccountHandle handle) {
        Log.d(this, "getPhoneId, handle:" + handle);
        if (handle == null) {
            return SubscriptionManager.INVALID_PHONE_INDEX;
        }
        ///M: when no sim cards inserted, we will pass the phoneId
        // in mId. @{
        if (TextUtils.isDigitsOnly(handle.getId())
                && handle.getId().length() < 2) {
            return Integer.parseInt(handle.getId());
        }
        /// @}
        int subId = TelecomUtils.getSubId(TelecomSystem.getInstance().getContext(), handle);
        return SubscriptionManager.getPhoneId(subId);
    }
    /** @}   */

    /**
     * M: Update audio mode according to
     * phone account under DSDA/C2K project @{
     * @param mode: audio mode.
     * @return the right audio mode
     */
    private int updateAudioModeWithPhoneAccount(int mode) {
        Log.d(this, "enter updateAudioModeWithPhoneAccount, mode:" + mode +
                ", isDsda: " + TelecomUtils.isInDsdaMode());
        int phoneId = SubscriptionManager.INVALID_PHONE_INDEX;
        if (TelecomUtils.isInDsdaMode()) {
            Call call = mCallAudioManager.getForegroundCall();
            /* Do not need to switch in call mode if the foreground call is not alive */
            Log.d(this, "getForegroundCall, call:" + call);
            if (call != null
                    && (call.isAlive()
                        || call.can(android.telecom.Call.Details.CAPABILITY_SPEED_UP_MT_AUDIO))
                    && mode == AudioManager.MODE_IN_CALL) {
                phoneId = getPhoneId(call.getTargetPhoneAccount());
                if (phoneId == SubscriptionManager.INVALID_PHONE_INDEX) {
                    Log.d(this, "Can't get the phone id now.");
                    return AudioManager.MODE_INVALID;
                }
                Log.d(this, "foreground call's phone id is " + phoneId);
                if (phoneId == PhoneConstants.SIM_ID_2) {
                    mode = AudioManager.MODE_IN_CALL_2;
                }
            }
        }
        Log.d(this, "leave updateAudioModeWithPhoneAccount, mode: " + mode);
        return mode;
    }
    /** @}   */
    // ============================================================================================
    // MTK Audio Mode Enhancement End
    // ============================================================================================
}