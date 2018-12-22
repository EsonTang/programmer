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

import static com.android.incallui.CallButtonFragment.Buttons.*;
import static com.android.incallui.CallButtonFragment.ButtonTexts.*;/*PRIZE-add-yuandailin-2016-3-15-start*/

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.UserHandle;
import android.telecom.CallAudioState;
import android.telecom.InCallService.VideoCall;
import android.telecom.VideoProfile;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;

import com.android.contacts.common.compat.CallSdkCompat;
import com.android.contacts.common.compat.SdkVersionOverride;
import com.android.dialer.compat.UserManagerCompat;
import com.android.incallui.AudioModeProvider.AudioModeListener;
import com.android.incallui.InCallCameraManager.Listener;
import com.android.incallui.InCallPresenter.CanAddCallListener;
import com.android.incallui.InCallPresenter.InCallDetailsListener;
import com.android.incallui.InCallPresenter.InCallState;
import com.android.incallui.InCallPresenter.InCallStateListener;
import com.android.incallui.InCallPresenter.IncomingCallListener;
import com.android.incallui.InCallPresenter.InCallDetailsListener;
/// M: add for phone record. @{
import com.android.incallui.InCallPresenter.PhoneRecorderListener;
/// @}
/// M: DMLock @{
import com.mediatek.incallui.InCallUtils;
/// @}
/// M: add for plug in. @{
import com.mediatek.incallui.ext.ExtensionManager;
/// @}
import com.mediatek.incallui.wrapper.FeatureOptionWrapper;

/**
 * Logic for call buttons.
 */
public class CallButtonPresenter extends Presenter<CallButtonPresenter.CallButtonUi>
        implements InCallStateListener, AudioModeListener, IncomingCallListener,
        InCallDetailsListener, CanAddCallListener, Listener, PhoneRecorderListener,
        CallList.CallUpdateListener {

    private static final String KEY_AUTOMATICALLY_MUTED = "incall_key_automatically_muted";
    private static final String KEY_PREVIOUS_MUTE_STATE = "incall_key_previous_mute_state";

    private Call mCall;
    private boolean mAutomaticallyMuted = false;
    private boolean mPreviousMuteState = false;

    public CallButtonPresenter() {
    }

    @Override
    public void onUiReady(CallButtonUi ui) {
        super.onUiReady(ui);

        AudioModeProvider.getInstance().addListener(this);

        // register for call state changes last
        final InCallPresenter inCallPresenter = InCallPresenter.getInstance();
        inCallPresenter.addListener(this);
        inCallPresenter.addIncomingCallListener(this);
        inCallPresenter.addDetailsListener(this);
        inCallPresenter.addCanAddCallListener(this);

        /*PRIZE-Delete-Optimize_Dialer-wangzhong-2018_3_5-start*/
        /*inCallPresenter.getInCallCameraManager().addCameraSelectionListener(this);*/
        /*PRIZE-Delete-Optimize_Dialer-wangzhong-2018_3_5-end*/

        /// M: [Voice Record] add Phone Record listener
        InCallPresenter.getInstance().addPhoneRecorderListener(this);

        // Update the buttons state immediately for the current call
        onStateChange(InCallState.NO_CALLS, inCallPresenter.getInCallState(),
                CallList.getInstance());
    }

    @Override
    public void onUiUnready(CallButtonUi ui) {
        super.onUiUnready(ui);

        InCallPresenter.getInstance().removeListener(this);
        AudioModeProvider.getInstance().removeListener(this);
        InCallPresenter.getInstance().removeIncomingCallListener(this);
        InCallPresenter.getInstance().removeDetailsListener(this);
        InCallPresenter.getInstance().getInCallCameraManager().removeCameraSelectionListener(this);
        InCallPresenter.getInstance().removeCanAddCallListener(this);
        /// M: [Voice Record]remove Phone Record listener
        InCallPresenter.getInstance().removePhoneRecorderListener(this);
        /// M: [Video call] when UI unready, must remove the listener.
        /// otherwise, if rotation happened, the Listener would not change to
        /// the new CallButtonPresenter instance @{
        if (mCall != null) {
            CallList.getInstance().removeCallUpdateListener(mCall.getId(), this);
        }
        /// @}
        ExtensionManager.getVilteAutoTestHelperExt().unregisterReceiverForUpgradeAndDowngrade();
    }

    @Override
    public void onStateChange(InCallState oldState, InCallState newState, CallList callList) {
        CallButtonUi ui = getUi();

        /// M: [Video Call] for tracking the CallUpdateListener.
        Call previousCall = mCall;

        if (newState == InCallState.OUTGOING) {
            mCall = callList.getOutgoingCall();
            /// M: For ALPS01940714, force set mute false if emergency call. @{
            if (isEmergencyCall(mCall)) {
                muteClicked(false);
            }
            /// @}
        } else if (newState == InCallState.INCALL) {
            mCall = callList.getActiveOrBackgroundCall();

            // When connected to voice mail, automatically shows the dialpad.
            // (On previous releases we showed it when in-call shows up, before waiting for
            // OUTGOING.  We may want to do that once we start showing "Voice mail" label on
            // the dialpad too.)
            if (ui != null) {
                if (oldState == InCallState.OUTGOING && mCall != null) {
                    if (CallerInfoUtils.isVoiceMailNumber(ui.getContext(), mCall)) {
                        /*prize-delete-huangliemin-2016-6-8-start*/
                        //ui.displayDialpad(true /* show */, true /* animate */);
                        /*prize-delete-huangliemin-2016-6-8-end*/
                    }
                }
            }
        } else if (newState == InCallState.INCOMING) {
            if (ui != null) {
                ui.displayDialpad(false /* show */, true /* animate */);
            }
            mCall = callList.getIncomingCall();
        } else {
            mCall = null;
        }

        /// [VideoCall] for all "primary call"(disconnected not included)
        /// add callUpdateListener for the session event. @{
        if (!Call.areSame(previousCall, mCall)) {
            if (previousCall != null) {
                CallList.getInstance().removeCallUpdateListener(previousCall.getId(), this);
            }
            if (mCall != null) {
                CallList.getInstance().addCallUpdateListener(mCall.getId(), this);
            }
        }
        /// @}
        updateUi(newState, mCall);

        /// M: Plug-in. @{
        ExtensionManager.getRCSeCallButtonExt().onStateChange(mCall != null ?
                        mCall.getTelecomCall() : null,
                callList.getCallMap());
        /// @}

        ///M: you can refer the method to InCallVideoCallCallback onSessionModifyRequestReceived
        updateVideoCallSessionState(mCall);
    }

    /**
     * Updates the user interface in response to a change in the details of a call.
     * Currently handles changes to the call buttons in response to a change in the details for a
     * call.  This is important to ensure changes to the active call are reflected in the available
     * buttons.
     *
     * @param call The active call.
     * @param details The call details.
     */
    @Override
    public void onDetailsChanged(Call call, android.telecom.Call.Details details) {
        // Only update if the changes are for the currently active call
        if (getUi() != null && call != null && call.equals(mCall)) {
            updateButtonsState(call);
        }
    }

    @Override
    public void onIncomingCall(InCallState oldState, InCallState newState, Call call) {

        onStateChange(oldState, newState, CallList.getInstance());
    }

    @Override
    public void onCanAddCallChanged(boolean canAddCall) {
        if (getUi() != null && mCall != null) {
            updateButtonsState(mCall);
        }
    }

    @Override
    public void onAudioMode(int mode) {
        if (getUi() != null) {
            getUi().setAudio(mode);
        }
    }

    @Override
    public void onSupportedAudioMode(int mask) {
        if (getUi() != null) {
            getUi().setSupportedAudio(mask);
        }
    }

    @Override
    public void onMute(boolean muted) {
        if (getUi() != null && !mAutomaticallyMuted) {
            getUi().setMute(muted);
        }
    }

    public int getAudioMode() {
        return AudioModeProvider.getInstance().getAudioMode();
    }

    public int getSupportedAudio() {
        return AudioModeProvider.getInstance().getSupportedModes();
    }

    public void setAudioMode(int mode) {

        // TODO: Set a intermediate state in this presenter until we get
        // an update for onAudioMode().  This will make UI response immediate
        // if it turns out to be slow

        Log.d(this, "Sending new Audio Mode: " + CallAudioState.audioRouteToString(mode));
        TelecomAdapter.getInstance().setAudioRoute(mode);
    }

    /**
     * Function assumes that bluetooth is not supported.
     */
    public void toggleSpeakerphone() {
        // this function should not be called if bluetooth is available
        if (0 != (CallAudioState.ROUTE_BLUETOOTH & getSupportedAudio())) {

            // It's clear the UI is wrong, so update the supported mode once again.
            Log.e(this, "toggling speakerphone not allowed when bluetooth supported.");
            getUi().setSupportedAudio(getSupportedAudio());
            return;
        }

        int newMode = CallAudioState.ROUTE_SPEAKER;

        // if speakerphone is already on, change to wired/earpiece
        if (getAudioMode() == CallAudioState.ROUTE_SPEAKER) {
            newMode = CallAudioState.ROUTE_WIRED_OR_EARPIECE;
        }

        setAudioMode(newMode);
    }

    public void muteClicked(boolean checked) {
        Log.d(this, "turning on mute: " + checked);
        TelecomAdapter.getInstance().mute(checked);
    }

    public void holdClicked(boolean checked) {
        if (mCall == null) {
            return;
        }
        if (checked) {
            Log.i(this, "Putting the call on hold: " + mCall);
            /// M: [log optimize]
            Log.op(mCall, Log.CcOpAction.HOLD, "hold button clicked.");
            TelecomAdapter.getInstance().holdCall(mCall.getId());
        } else {
            Log.i(this, "Removing the call from hold: " + mCall);
            /// M: [log optimize]
            Log.op(mCall, Log.CcOpAction.UNHOLD, "unhold button clicked.");
            TelecomAdapter.getInstance().unholdCall(mCall.getId());
        }
    }

    public void swapClicked() {
        if (mCall == null) {
            return;
        }

        Log.i(this, "Swapping the call: " + mCall);
        /// M: [log optimize]
        Log.op(mCall, Log.CcOpAction.SWAP, "swap key clicked.");
        TelecomAdapter.getInstance().swap(mCall.getId());
    }

    public void mergeClicked() {
        /// M: [log optimize]
        Log.op(mCall, Log.CcOpAction.MERGE, "Merge to be a conference");
        /// M: fix NPE issue ALPS02509623. @{
        if (mCall == null) {
            return;
        }
        /// @}
        TelecomAdapter.getInstance().merge(mCall.getId());
    }

    public void addCallClicked() {
        // Automatically mute the current call
        mAutomaticallyMuted = true;
        mPreviousMuteState = AudioModeProvider.getInstance().getMute();
        // Simulate a click on the mute button
        muteClicked(true);
        TelecomAdapter.getInstance().addCall();
    }

    public void changeToVoiceClicked() {
        // M: fix CR:ALPS02550278,NullPointerException.
        if (mCall == null) {
            return;
        }
        VideoCall videoCall = mCall.getVideoCall();
        if (videoCall == null) {
            return;
        }

        VideoProfile videoProfile = new VideoProfile(
                VideoProfile.STATE_AUDIO_ONLY, VideoProfile.QUALITY_DEFAULT);
        videoCall.sendSessionModifyRequest(videoProfile);
        ///M: add new state for downgrade and updateVideoBtnUi @{
        mCall.setSessionModificationState(Call.SessionModificationState.
                WAITING_FOR_DOWNGRADE_RESPONSE);
        //show message for downgrade
        InCallPresenter.getInstance().showMessage(R.string.video_call_downgrade_request);

        //@}
    }

    public void showDialpadClicked(boolean checked) {
        Log.v(this, "Show dialpad " + String.valueOf(checked));
        if (getUi() != null)//PRIZE-add-yuandailin-2016-6-15
        getUi().displayDialpad(checked /* show */, true /* animate */);
    }

    public void changeToVideoClicked() {
        // M: fix CR:ALPS02550278,NullPointerException.
        if (mCall == null) {
            return;
        }
        VideoCall videoCall = mCall.getVideoCall();
        if (videoCall == null) {
            return;
        }
        /// M: fix CR:ALPS02499779,can not use upgrade anymore.
        /// [Video call] when receive upgrade to video request
        /// and click video call button, no response. @{
        if (mCall.getSessionModificationState() ==
            Call.SessionModificationState.RECEIVED_UPGRADE_TO_VIDEO_REQUEST) {
            return;
        }
        /// @}
        int currVideoState = mCall.getVideoState();
        int currUnpausedVideoState = VideoUtils.getUnPausedVideoState(currVideoState);
        currUnpausedVideoState |= VideoProfile.STATE_BIDIRECTIONAL;

        VideoProfile videoProfile = new VideoProfile(currUnpausedVideoState);
        videoCall.sendSessionModifyRequest(videoProfile);
        mCall.setSessionModificationState(Call.SessionModificationState.
                WAITING_FOR_UPGRADE_RESPONSE);
    }

    /**
     * Switches the camera between the front-facing and back-facing camera.
     * @param useFrontFacingCamera True if we should switch to using the front-facing camera, or
     *     false if we should switch to using the back-facing camera.
     */
    public void switchCameraClicked(boolean useFrontFacingCamera) {
        InCallCameraManager cameraManager = InCallPresenter.getInstance().getInCallCameraManager();
        cameraManager.setUseFrontFacingCamera(useFrontFacingCamera);
        // M: fix CR:ALPS02550278,NullPointerException.
        if (mCall == null) {
            return;
        }
        VideoCall videoCall = mCall.getVideoCall();
        if (videoCall == null) {
            return;
        }

        String cameraId = cameraManager.getActiveCameraId();
        if (cameraId != null) {
            final int cameraDir = cameraManager.isUsingFrontFacingCamera()
                    ? Call.VideoSettings.CAMERA_DIRECTION_FRONT_FACING
                    : Call.VideoSettings.CAMERA_DIRECTION_BACK_FACING;
            mCall.getVideoSettings().setCameraDir(cameraDir);
            videoCall.setCamera(cameraId);
            videoCall.requestCameraCapabilities();
        }
    }


    /**
     * Stop or start client's video transmission.
     * @param pause True if pausing the local user's video, or false if starting the local user's
     *    video.
     */
    public void pauseVideoClicked(boolean pause) {
        // M: fix CR:ALPS02550278,NullPointerException.
        if (mCall == null) {
            return;
        }
        VideoCall videoCall = mCall.getVideoCall();
        if (videoCall == null) {
            return;
        }

        if (pause) {
            videoCall.setCamera(null);
            VideoProfile videoProfile = new VideoProfile(
                    mCall.getVideoState() & ~VideoProfile.STATE_TX_ENABLED);
            videoCall.sendSessionModifyRequest(videoProfile);
            /// M: when close camera set the session state @{
            mCall.setSessionModificationState(Call.SessionModificationState.
                    WAITING_FOR_PAUSE_VIDEO_RESPONSE);
            /// @}
        } else {
            InCallCameraManager cameraManager = InCallPresenter.getInstance().
                    getInCallCameraManager();
            videoCall.setCamera(cameraManager.getActiveCameraId());
            VideoProfile videoProfile = new VideoProfile(
                    mCall.getVideoState() | VideoProfile.STATE_TX_ENABLED);
            videoCall.sendSessionModifyRequest(videoProfile);
            mCall.setSessionModificationState(Call.SessionModificationState.
                    WAITING_FOR_UPGRADE_RESPONSE);
        }
    }

    private void updateUi(InCallState state, Call call) {
        Log.d(this, "Updating call UI for call: ", call);
        /// M: DMLock @{
        if (InCallUtils.isDMLocked()) {
            updateInCallControlsDuringDMLocked(call);
            return;
        }
        /// @}

        final CallButtonUi ui = getUi();
        if (ui == null) {
            return;
        }

        final boolean isEnabled =
                state.isConnectingOrConnected() &&!state.isIncoming() && call != null;
        ui.setEnabled(isEnabled);

        /// M: for ALPS01945830. Redraw callbuttons. @{
        //ui.updateColors();//PRIZE-remove-yuandailin-2016-3-9
        /// @}

        /*Prize-change-huangliemin-2016-5-26-start*/
        /*if (call == null) {
            return;
        }*/
        if (!isEnabled) {
            return;
        }
        /*Prize-change-huangliemin-2016-5-26-end*/

        /*PRIZE-Delete-Optimize_Dialer-wangzhong-2018_3_5-start*/
        /// M: fix CR:ALPS02259658,"hang up active,answer waiting call"not display in 1A+1W @{
        /*if (call.getState() == Call.State.INCOMING
                && CallList.getInstance().getActiveAndHoldCallsCount() != 0) {
            ui.enableOverflowButton();
        }*/
        /// @}
        /*PRIZE-Delete-Optimize_Dialer-wangzhong-2018_3_5-end*/

        updateButtonsState(call);
        /*PRIZE-add-yuandailin-2016-4-14-start*/
        if (isEnabled) {
            boolean isCanMute = call.can(android.telecom.Call.Details.CAPABILITY_MUTE);
            ui.showButton(BUTTON_MUTE, isCanMute);
            ui.enableButton(BUTTON_MUTE, isCanMute);
            ui.enableButtonText(BUTTON_MUTE_TEXT, isCanMute);
        }
        /*PRIZE-add-yuandailin-2016-4-14-end*/

        /*PRIZE-Delete-Optimize_Dialer-wangzhong-2018_3_5-start*/
        //M: for ALPS02501750. update hide button for rotation.
        /*if (mCall != null && mCall.isHidePreview()) {
            ui.updateHideButtonStatus(true);
        } else {
            ui.updateHideButtonStatus(false);
        }*/
        /*PRIZE-Delete-Optimize_Dialer-wangzhong-2018_3_5-end*/
    }

    /**
     * Updates the buttons applicable for the UI.
     *
     * @param call The active call.
     */
    private void updateButtonsState(Call call) {
        Log.v(this, "updateButtonsState");
        final CallButtonUi ui = getUi();

        final boolean isVideo = VideoUtils.isVideoCall(call);

        /*PRIZE-add-yuandailin-2016-4-19-start*/
        int callState = call.getState();
        boolean isCallActive = callState == Call.State.ACTIVE;
        /*PRIZE-add-yuandailin-2016-4-19-end*/

        // Common functionality (audio, hold, etc).
        // Show either HOLD or SWAP, but not both. If neither HOLD or SWAP is available:
        //     (1) If the device normally can hold, show HOLD in a disabled state.
        //     (2) If the device doesn't have the concept of hold/swap, remove the button.
        final boolean showSwap = call.can(
                android.telecom.Call.Details.CAPABILITY_SWAP_CONFERENCE);
        /// M: [Video Call]move show hold controller to updateVideoButtonUI  @{
        boolean showHold = !showSwap
                && call.can(android.telecom.Call.Details.CAPABILITY_SUPPORT_HOLD)
                && call.can(android.telecom.Call.Details.CAPABILITY_HOLD);
        if (isVideo) {
            showHold &= call.getVideoFeatures().supportsHold();
        }
        ///@}
        final boolean isCallOnHold = call.getState() == Call.State.ONHOLD;

        final boolean showAddCall = TelecomAdapter.getInstance().canAddCall()
                && UserManagerCompat.isUserUnlocked(ui.getContext());
        final boolean showMerge = call.can(
                android.telecom.Call.Details.CAPABILITY_MERGE_CONFERENCE);
        /* google code,move to updateVideoButtonUI.
        final boolean showUpgradeToVideo = !isVideo && hasVideoCallCapabilities(call);
        final boolean showDowngradeToAudio = isVideo && isDowngradeToAudioSupported(call);
        */
        final boolean showMute = call.can(android.telecom.Call.Details.CAPABILITY_MUTE);
        /// M: add other feature. @{
        final boolean canSetEct = InCallUtils.canSetEct();
        final boolean canHangupAllCalls = InCallUtils.canHangupAllCalls();
        final boolean canHangupAllHoldCalls = InCallUtils.canHangupAllHoldCalls();
        final boolean canHangupActiveAndAnswerWaiting = InCallUtils
                .canHangupActiveAndAnswerWaiting();
        Log.d(this, "[updateButtonsState] Swap:" + showSwap + " OnHold:"
                + isCallOnHold + " AddCall:" + showAddCall + " Merge:"
                + showMerge + " Mute:" + showMute + " SetEct:" + canSetEct
                + " Hangup[AllCalls:" + canHangupAllCalls + " AllHoldCalls:"
                + canHangupAllHoldCalls + " ActiveAndAnswerWaiting:"
                + canHangupActiveAndAnswerWaiting + "]");
        /// @}
        /*PRIZE-change-yuandailin-2016-4-6-start*/
        ui.showButton(BUTTON_AUDIO, true);
        ui.showButtonText(BUTTON_AUDIO_TEXT, true);

        /*PRIZE-Delete-Optimize_Dialer-wangzhong-2018_3_5-start*/
        /*ui.showButton(BUTTON_SWAP, showSwap);*/
        /*PRIZE-Delete-Optimize_Dialer-wangzhong-2018_3_5-end*/

        /*PRIZE-change -yuandailin-2016-7-14-start*/
        ui.showButton(BUTTON_HOLD, true);
        ui.showButtonText(BUTTON_HOLD_TEXT, true);
        ui.showButton(PRIZE_BUTTON_CONTACTS, true);
        ui.showButtonText(PRIZE_BUTTON_CONTACTS_TEXT, true);
        /*PRIZE-change -yuandailin-2016-7-14-end*/
        ui.enableButton(BUTTON_HOLD, showHold);
        ui.enableButtonText(BUTTON_HOLD_TEXT, showHold);
        ui.setHold(isCallOnHold);
        ui.showButton(BUTTON_MUTE, true);
        ui.showButtonText(BUTTON_MUTE_TEXT, true);
        /*PRIZE-change-yuandailin-2016-4-6-end*/

      	/*PRIZE-change-yuandailin-2016-3-14-start*/
        //ui.showButton(BUTTON_ADD_CALL, showAddCall);
        ui.showButton(BUTTON_ADD_CALL, true);
        ui.showButtonText(BUTTON_ADD_CALL_TEXT, true);
        /*PRIZE-add -yuandailin-2016-8-12-start*/
        ui.enableButton(BUTTON_ADD_CALL, showAddCall);
        ui.enableButtonText(BUTTON_ADD_CALL_TEXT, showAddCall);
        /*PRIZE-add -yuandailin-2016-8-12-end*/
        /* google code,move to updateVideoButtonUI.
        ui.showButton(BUTTON_UPGRADE_TO_VIDEO, showUpgradeToVideo);
        ui.showButton(BUTTON_DOWNGRADE_TO_AUDIO, showDowngradeToAudio);
        ui.showButton(BUTTON_SWITCH_CAMERA, isVideo);
        ui.showButton(BUTTON_PAUSE_VIDEO, isVideo);
        */
        /*PRIZE-change-yuandailin-2016-4-11-start*/
        //ui.showButton(BUTTON_DIALPAD, true);
        ui.showButton(BUTTON_VOICE_RECORD, true);
        ui.enableButton(BUTTON_VOICE_RECORD, isCallActive);
        ui.showButtonText(BUTTON_VOICE_RECORD_TEXT, true);
        ui.enableButtonText(BUTTON_VOICE_RECORD_TEXT, isCallActive);
        /*PRIZE-Delete-Optimize_Dialer-wangzhong-2018_3_5-start*/
        /*ui.showButton(BUTTON_DIALPAD, false);
        ui.showButton(BUTTON_TIP, true);
        ui.enableButton(BUTTON_TIP, true);
        ui.showButtonText(BUTTON_TIP_TEXT, true);
        ui.showButton(BUTTON_MERGE, showMerge);*/
        /*PRIZE-Delete-Optimize_Dialer-wangzhong-2018_3_5-end*/
        /*PRIZE-change-yuandailin-2016-4-11-end*/

        /// M: add other feature. @{
        /*PRIZE-change-yuandailin-2016-4-11-start*/
        /*ui.showButton(BUTTON_SET_ECT, canSetEct);
        ui.enableButton(BUTTON_SET_ECT,canSetEct);
        ui.showButton(BUTTON_HANGUP_ALL_CALLS, canHangupAllCalls);
        ui.enableButton(BUTTON_HANGUP_ALL_CALLS, canHangupAllCalls);
        ui.showButton(BUTTON_HANGUP_ALL_HOLD_CALLS, canHangupAllHoldCalls);
        ui.enableButton(BUTTON_HANGUP_ALL_HOLD_CALLS, canHangupAllHoldCalls);*/
        /*PRIZE-Delete-Optimize_Dialer-wangzhong-2018_3_5-start*/
        /*ui.showButton(BUTTON_SET_ECT, false);
        ui.enableButton(BUTTON_SET_ECT, false);
        ui.showButton(BUTTON_HANGUP_ALL_CALLS, false);
        ui.enableButton(BUTTON_HANGUP_ALL_CALLS, false);
        ui.showButton(BUTTON_HANGUP_ALL_HOLD_CALLS, false);
        ui.enableButton(BUTTON_HANGUP_ALL_HOLD_CALLS, false);*/
        /*PRIZE-change-yuandailin-2016-4-11-end*/
        /*ui.showButton(BUTTON_HANGUP_ACTIVE_AND_ANSWER_WAITING, canHangupActiveAndAnswerWaiting);
        ui.enableButton(BUTTON_HANGUP_ACTIVE_AND_ANSWER_WAITING, canHangupActiveAndAnswerWaiting);*/
        /*PRIZE-Delete-Optimize_Dialer-wangzhong-2018_3_5-end*/
        /// @}
        updateVideoButtonUI(call, isVideo);//PRIZE-add for video call-yuandailin-2016-7-6
        ui.updateButtonStates();

    }

    private boolean hasVideoCallCapabilities(Call call) {
        if (SdkVersionOverride.getSdkVersion(Build.VERSION_CODES.M) >= Build.VERSION_CODES.M) {
            return call.can(android.telecom.Call.Details.CAPABILITY_SUPPORTS_VT_LOCAL_TX)
                    && call.can(android.telecom.Call.Details.CAPABILITY_SUPPORTS_VT_REMOTE_RX);
        }
        // In L, this single flag represents both video transmitting and receiving capabilities
        return call.can(android.telecom.Call.Details.CAPABILITY_SUPPORTS_VT_LOCAL_TX);
    }

    /**
     * Determines if downgrading from a video call to an audio-only call is supported.  In order to
     * support downgrade to audio, the SDK version must be >= N and the call should NOT have the
     * {@link android.telecom.Call.Details#CAPABILITY_CANNOT_DOWNGRADE_VIDEO_TO_AUDIO}.
     * @param call The call.
     * @return {@code true} if downgrading to an audio-only call from a video call is supported.
     */
    private boolean isDowngradeToAudioSupported(Call call) {
        return !call.can(CallSdkCompat.Details.CAPABILITY_CANNOT_DOWNGRADE_VIDEO_TO_AUDIO);
    }

    public void refreshMuteState() {
        // Restore the previous mute state
        if (mAutomaticallyMuted &&
                AudioModeProvider.getInstance().getMute() != mPreviousMuteState) {
            if (getUi() == null) {
                return;
            }
            muteClicked(mPreviousMuteState);
        }
        mAutomaticallyMuted = false;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_AUTOMATICALLY_MUTED, mAutomaticallyMuted);
        outState.putBoolean(KEY_PREVIOUS_MUTE_STATE, mPreviousMuteState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        mAutomaticallyMuted =
                savedInstanceState.getBoolean(KEY_AUTOMATICALLY_MUTED, mAutomaticallyMuted);
        mPreviousMuteState =
                savedInstanceState.getBoolean(KEY_PREVIOUS_MUTE_STATE, mPreviousMuteState);
        super.onRestoreInstanceState(savedInstanceState);
    }

    public interface CallButtonUi extends Ui {
        void showButton(int buttonId, boolean show);
        void showButtonText(int buttonTextId, boolean show);//PRIZE-add-yuandailin-2016-4-6
        void enableButton(int buttonId, boolean enable);
        void enableButtonText(int buttonTextId, boolean enable);//PRIZE-add-yuandailin-2016-4-6
        void setEnabled(boolean on);
        void setMute(boolean on);
        void setHold(boolean on);
        /*PRIZE-Delete-Optimize_Dialer-wangzhong-2018_3_5-start*/
        /*void setCameraSwitched(boolean isBackFacingCamera);
        void setVideoPaused(boolean isPaused);*/
        /*PRIZE-Delete-Optimize_Dialer-wangzhong-2018_3_5-end*/
        void setAudio(int mode);
        void setSupportedAudio(int mask);
        void displayDialpad(boolean on, boolean animate);
        boolean isDialpadVisible();

        /**
         * Once showButton() has been called on each of the individual buttons in the UI, call
         * this to configure the overflow menu appropriately.
         */
        void updateButtonStates();
        Context getContext();

        /*PRIZE-Delete-Optimize_Dialer-wangzhong-2018_3_5-start*/
        /// M: for ALPS01945830. Redraw callbuttons. @{
        /*void updateColors();*/
        /// @}
        /*PRIZE-Delete-Optimize_Dialer-wangzhong-2018_3_5-end*/

        /// M: Voice recording
        void configRecordingButton();

        /*PRIZE-Delete-Optimize_Dialer-wangzhong-2018_3_5-start*/
        /// M: fix CR:ALPS02259658,"hang up active,answer waiting call"not display in 1A+1W @{
        /*void enableOverflowButton();*/
        /// @}

        /*void updateHideButtonStatus(boolean hide);*/
        /*PRIZE-Delete-Optimize_Dialer-wangzhong-2018_3_5-end*/
    }

    @Override
    public void onActiveCameraSelectionChanged(boolean isUsingFrontFacingCamera) {
        /*PRIZE-Delete-Optimize_Dialer-wangzhong-2018_3_5-start*/
        /*if (getUi() == null) {
            return;
        }
        getUi().setCameraSwitched(!isUsingFrontFacingCamera);*/
        /*PRIZE-Delete-Optimize_Dialer-wangzhong-2018_3_5-end*/
    }

    //---------------------------------------Mediatek-----------------------------------
    /**
     * M: [DM Lock] update incall UI button states when DM Lock enabled.
     * @param call used to set call button states.
     */
    void updateInCallControlsDuringDMLocked(Call call) {
        final CallButtonUi ui = getUi();
        if (ui == null) {
            Log.d(this, "just return ui:" + ui);
            return;
        }
        Context context = ui.getContext();
        if (context == null) {
            Log.d(this, "just return context:" + context);
            return;
        }
        if (call == null) {
            Log.d(this, "just return call:" + call);
            return;
        }
        ui.setEnabled(false);
        /*PRIZE-Delete-Optimize_Dialer-wangzhong-2018_3_5-start*/
        /*ui.showButton(BUTTON_MERGE, false);
        ui.showButton(BUTTON_SWITCH_VOICE_RECORD, false);*/
        /*PRIZE-Delete-Optimize_Dialer-wangzhong-2018_3_5-end*/
        ui.showButton(BUTTON_ADD_CALL, true);
        ui.enableButton(BUTTON_ADD_CALL, false);
        final boolean canHold = call.can(android.telecom.Call.Details.CAPABILITY_HOLD);
        ui.displayDialpad(getUi().isDialpadVisible(), true);
        ui.showButton(BUTTON_HOLD, canHold);
    }

    /**
     * Instructs Telecom to disconnect all the calls.
     */
    public void hangupAllClicked() {
        Log.d(this, "Hangup all calls");
        TelecomAdapter.getInstance().hangupAll();
    }

    /**
     * Instructs Telecom to disconnect all the HOLDING calls.
     */
    public void hangupAllHoldCallsClicked() {
        Log.d(this, "Hangup all hold calls");
        TelecomAdapter.getInstance().hangupAllHoldCalls();
    }

    /**
     * Instructs Telecom to disconnect active call and answer waiting call.
     */
    public void hangupActiveAndAnswerWaitingClicked() {
        Log.d(this, "Hangup active and answer waiting");
        TelecomAdapter.getInstance().hangupActiveAndAnswerWaiting();
    }

    /**
     * M: Check whether the call is ECC.
     * @param call current call
     * @return true if is ECC call
     */
    private boolean isEmergencyCall(Call call) {
        if (call != null) {
            Uri handle = call.getHandle();
            if (handle != null) {
                String number = handle.getSchemeSpecificPart();
                if (!TextUtils.isEmpty(number)) {
                    return PhoneNumberUtils.isEmergencyNumber(number);
                }
            }
        }
        return false;
    }

    /**
     * M: get the ECT capable call.
     * For ECT, we just check the hold call.
     */
    private Call getTheCallWithEctCapable() {
        final Call call = CallList.getInstance().getBackgroundCall();
        if (call != null && call.can(android.telecom.Call.Details.CAPABILITY_ECT)) {
            return call;
        }
        return null;
    }

    /**
     * M: Instructs Telecom to select Ect Menu.
     */
    public void onEctMenuSelected() {
        final Call call = getTheCallWithEctCapable();
        if (call != null) {
            TelecomAdapter.getInstance().explicitCallTransfer(call.getTelecomCall().getCallId());
        }
    }

    /** M: [Voice Record] switch the state of voice recording @{ */
    public void voiceRecordClicked() {
        TelecomAdapter.getInstance().startVoiceRecording();
    }

    public void stopRecordClicked() {
        TelecomAdapter.getInstance().stopVoiceRecording();
    }

    @Override
    public void onUpdateRecordState(int state, int customValue) {
        if (FeatureOptionWrapper.isSupportPhoneVoiceRecording()) {
            final CallButtonUi ui = getUi();
            if (ui != null) {
                ui.configRecordingButton();
            }
        }
    }
    /** @} */

    /**
     * M: update VideoButtonUi
     *
     * @param call
     * @param isVideo indicate current call is video call or not.
     */
    public void updateVideoButtonUI(Call call, boolean isVideo) {
        final CallButtonUi ui = getUi();
        //only call is active, can show video button can operate camera.
        int callState = call.getState();
        boolean isCallActive = callState == Call.State.ACTIVE;

        // find whether it's held state, when held state can't do video operation
        boolean currentHeldState = false;
        android.telecom.Call.Details details = call.getDetails();
        if (details == null) {
            currentHeldState = false;
        }
        currentHeldState = call.isHeld();

        final boolean showUpgradeToVideoEx = (!isVideo
                && ExtensionManager.getInCallButtonExt().isVideoCallCapable(call.getNumber()));
        /// FIXME: bug here!!! should not use "||" to combine these conditions.
        /// Plugin should be able to no show even if Host said yet.
        final boolean showUpgradeToVideo = showUpgradeToVideoEx
                || (!isVideo
                    && hasVideoCallCapabilities(call)
                    && call.getVideoFeatures().canUpgradeToVideoCall());

        Log.d(this, "[updateVideoButtonUI] showUpgradeToVideo:" + showUpgradeToVideo
                + "callState:" + callState);

        final boolean showUpgradeBtn = (showUpgradeToVideo
                || call.getVideoFeatures().forceEnableVideo())
                && isCallActive && !currentHeldState;
        final boolean showSwitchBtn = isVideo && isCallActive && !currentHeldState;
        final boolean showPauseVideoBtn = isVideo
                && call.getVideoFeatures().supportsPauseVideo()
                && isCallActive && !currentHeldState
                /// M: we should not show pause button if no peer video from remote.
                && VideoProfile.isReceptionEnabled(call.getVideoState());
        final boolean showHideLocalVideoBtn = isVideo
                && call.getVideoFeatures().supportsHidePreview()
                && isCallActive && !currentHeldState;
          final boolean showDowngradBtn = isVideo && isDowngradeToAudioSupported(call)
                && call.getVideoFeatures().supportsDowngrade()
                && isCallActive && !currentHeldState;
        final boolean showManageVideoConBtn = isVideo
                && call.can(android.telecom.Call.Details.CAPABILITY_MANAGE_CONFERENCE)
                && isCallActive;
        //we will set VideoBtn Enable except the instantaneous state
        final boolean canEnableVideoBtn =
                call.getSessionModificationState()
                != Call.SessionModificationState.WAITING_FOR_UPGRADE_RESPONSE
            && call.getSessionModificationState()
                != Call.SessionModificationState.RECEIVED_UPGRADE_TO_VIDEO_REQUEST
            && call.getSessionModificationState()
                != Call.SessionModificationState.WAITING_FOR_DOWNGRADE_RESPONSE
            && call.getSessionModificationState()
                != Call.SessionModificationState.WAITING_FOR_PAUSE_VIDEO_RESPONSE
            && call.getSessionModificationState()
                != Call.SessionModificationState.RECEIVED_UPGRADE_TO_VIDEO_REQUEST_ONE_WAY;
        // when pause video make one way  and only can receive video, we can't show
        // switch camera button
        final boolean isCameraOff = call.getVideoState() == VideoProfile.STATE_RX_ENABLED;
        /// M: fix CR:ALPS02847667 to support show both hold and swap
        boolean showHold = call.can(android.telecom.Call.Details.CAPABILITY_SUPPORT_HOLD)
              && call.can(android.telecom.Call.Details.CAPABILITY_HOLD);
        if (isVideo) {
            showHold &= call.getVideoFeatures().supportsHold();
        }

        // M: fix CR:ALPS02698355,don't show voice record when receive upgrade request.
        // [Voice Record] check if should display record
        final boolean canRecordVoice = call
                .can(android.telecom.Call.Details.CAPABILITY_VOICE_RECORD)
                && !InCallUtils.isDMLocked()
                && !VideoUtils.isVideoCall(call)
                && UserManagerCompat.isUserUnlocked(ui.getContext());

        //add canEnableVideoBtn flag to control showing hold button is avoid this case:
        //when there was a volte call, we can do some video action, during this action,
        //we can't show hold button. by another way if there was a voice call, it's
        //SessionModificationState  always is no_request, so meet the requestment.
        /*ui.showButton(BUTTON_HOLD, showHold && canEnableVideoBtn);*/
        ui.showButton(BUTTON_HOLD, true);
        /*PRIZE-Delete-Optimize_Dialer-wangzhong-2018_3_5-start*/
        /*ui.showButton(BUTTON_UPGRADE_TO_VIDEO, showUpgradeBtn && canEnableVideoBtn);
        ui.showButton(BUTTON_SWITCH_CAMERA, showSwitchBtn && canEnableVideoBtn && !isCameraOff);
        ui.showButton(BUTTON_PAUSE_VIDEO, showPauseVideoBtn && canEnableVideoBtn);
        /// M:add hide Local preview button and downgrade button
        ui.showButton(BUTTON_HIDE_LOCAL_VIDEO, showHideLocalVideoBtn && canEnableVideoBtn);
        ui.showButton(BUTTON_DOWNGRADE_TO_AUDIO, showDowngradBtn && canEnableVideoBtn);
        /// end add local preview and downgrade button @}

        /// M:add show Video manage conference button
        ui.showButton(BUTTON_MANAGE_VIDEO_CONFERENCE, showManageVideoConBtn && canEnableVideoBtn);*/
        /// end add manage conference button @}
        /*PRIZE-Delete-Optimize_Dialer-wangzhong-2018_3_5-end*/
        /// end only call state is active  can show video button @}
        if (!canEnableVideoBtn) {
            /*PRIZE-Change-InCallUI_VideoCall-wangzhong-2016_12_26-start*/
            /*ui.showButton(BUTTON_ADD_CALL, false);*/
            ui.showButton(BUTTON_ADD_CALL, true);
            ui.enableButton(BUTTON_ADD_CALL, false);
            /*PRIZE-Change-InCallUI_VideoCall-wangzhong-2016_12_26-end*/
        }

        /*PRIZE-Delete-Optimize_Dialer-wangzhong-2018_3_5-start*/
        /*if (showPauseVideoBtn && canEnableVideoBtn) {
            /// Rx only: already set to paused state
            /// Bidirectional: normal case, didn't press pause
            /// Tx only / voice: the pause button would hide, no need update
            ui.setVideoPaused(!VideoProfile.isBidirectional(call.getVideoState()));
        }*/
        /*PRIZE-Delete-Optimize_Dialer-wangzhong-2018_3_5-end*/
        /** M: fix CR:ALPS02698355,don't show voice record when receive upgrade request.
         * [Voice Record] support voice recording @{ */
        final boolean showRecording = UserHandle.myUserId()
                == UserHandle.USER_OWNER && canRecordVoice && canEnableVideoBtn;
        /*PRIZE-Delete-Optimize_Dialer-wangzhong-2018_3_5-start*/
        /*ui.showButton(BUTTON_SWITCH_VOICE_RECORD, showRecording);*/
        /*PRIZE-Delete-Optimize_Dialer-wangzhong-2018_3_5-end*/
        if (showRecording) {
            ui.configRecordingButton();
        }
        /** @} */
    }

    @Override
    public void onCallChanged(Call call) {
        // no op
    }

    @Override
    public void onSessionModificationStateChange(int sessionModificationState) {
        if(mCall == null) {
            return;
        }
        updateButtonsState(mCall);
    }
    /// end update VideoButtonUi @}

    /**
     * M: when ony way remote side set SessionModificationState,
     * we change to default according to videostate
     *
     * @param call
     */
    private void updateVideoCallSessionState(Call call) {
        if (call == null) {
            return;
        }
        if (call.getSessionModificationState()
                == Call.SessionModificationState.WAITING_FOR_DOWNGRADE_RESPONSE) {
            if (call.getVideoState() == VideoProfile.STATE_AUDIO_ONLY) {
                call.setSessionModificationState(Call.SessionModificationState.NO_REQUEST);
            }
        } else if (call.getSessionModificationState()
                == Call.SessionModificationState.WAITING_FOR_PAUSE_VIDEO_RESPONSE) {
            if (call.getVideoState() == VideoProfile.STATE_TX_ENABLED) {
                call.setSessionModificationState(Call.SessionModificationState.NO_REQUEST);
            }
        } else if (call.getSessionModificationState()
                == Call.SessionModificationState.RECEIVED_UPGRADE_TO_VIDEO_REQUEST_ONE_WAY) {
            if (call.getVideoState() == VideoProfile.STATE_BIDIRECTIONAL) {
                call.setSessionModificationState(Call.SessionModificationState.NO_REQUEST);
            }
        }
    }

    @Override
    public void onLastForwardedNumberChange() {
        // no-op
    }

    @Override
    public void onChildNumberChange() {
        // no-op
    }

}
