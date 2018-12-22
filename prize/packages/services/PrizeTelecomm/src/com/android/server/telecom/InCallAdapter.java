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

package com.android.server.telecom;

import android.os.Binder;
import android.os.Bundle;
import android.telecom.PhoneAccountHandle;
import com.android.internal.telecom.IInCallAdapter;
import com.mediatek.telecom.LogUtils;
import com.mediatek.telecom.PerformanceTracker;
import com.mediatek.telecom.TelecomUtils;
import com.mediatek.telecom.recording.PhoneRecorderHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * Receives call commands and updates from in-call app and passes them through to CallsManager.
 * {@link InCallController} creates an instance of this class and passes it to the in-call app after
 * binding to it. This adapter can receive commands and updates until the in-call app is unbound.
 */
class InCallAdapter extends IInCallAdapter.Stub {

    private final CallsManager mCallsManager;
    private final CallIdMapper mCallIdMapper;
    private final TelecomSystem.SyncRoot mLock;
    private final String mOwnerComponentName;

    /** Persists the specified parameters. */
    public InCallAdapter(CallsManager callsManager, CallIdMapper callIdMapper,
            TelecomSystem.SyncRoot lock, String ownerComponentName) {
        mCallsManager = callsManager;
        mCallIdMapper = callIdMapper;
        mLock = lock;
        mOwnerComponentName = ownerComponentName;
    }

    @Override
    public void answerCall(String callId, int videoState) {
        try {

            Log.startSession(Log.Sessions.ICA_ANSWER_CALL, mOwnerComponentName);
            ///M: [Performance Track] @{
            Call c = mCallIdMapper.getCall(callId);
            if (c != null && !c.isConference()) {
                PerformanceTracker.getInstance().trackAnswerCall(
                        c.getId(), PerformanceTracker.MT_RECEIVE_ANSWER_OPERATION);
            }
            ///@}

            long token = Binder.clearCallingIdentity();
            try {
                synchronized (mLock) {
                    Log.d(this, "answerCall(%s,%d)", callId, videoState);
                    Call call = mCallIdMapper.getCall(callId);
                    /// M: for log parser @{
                    LogUtils.logCcOp(call, LogUtils.OP_ACTION_ANSWER, callId, "");
                    /// @}
                    if (call != null) {
                        mCallsManager.answerCall(call, videoState);
                    } else {
                        Log.w(this, "answerCall, unknown call id: %s", callId);
                    }
                }
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        } finally {
            Log.endSession();
        }
    }

    @Override
    public void rejectCall(String callId, boolean rejectWithMessage, String textMessage) {
        try {
            Log.startSession(Log.Sessions.ICA_REJECT_CALL, mOwnerComponentName);
            long token = Binder.clearCallingIdentity();
            try {
                synchronized (mLock) {
                    Log.d(this, "rejectCall(%s,%b,%s)", callId, rejectWithMessage, textMessage);
                    Call call = mCallIdMapper.getCall(callId);
                    /// M: for log parser @{
                    LogUtils.logCcOp(call, LogUtils.OP_ACTION_REJECT, callId, "");
                    /// @}
                    if (call != null) {
                        mCallsManager.rejectCall(call, rejectWithMessage, textMessage);
                    } else {
                        Log.w(this, "setRingback, unknown call id: %s", callId);
                    }
                }
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        } finally {
            Log.endSession();
        }
    }

    @Override
    public void playDtmfTone(String callId, char digit) {
        try {
            Log.startSession("ICA.pDT", mOwnerComponentName);
            long token = Binder.clearCallingIdentity();
            try {
                synchronized (mLock) {
                    Log.d(this, "playDtmfTone(%s,%c)", callId, digit);
                    Call call = mCallIdMapper.getCall(callId);
                    if (call != null) {
                        mCallsManager.playDtmfTone(call, digit);
                    } else {
                        Log.w(this, "playDtmfTone, unknown call id: %s", callId);
                    }
                }
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        } finally {
            Log.endSession();
        }
    }

    @Override
    public void stopDtmfTone(String callId) {
        try {
            Log.startSession("ICA.sDT", mOwnerComponentName);
            long token = Binder.clearCallingIdentity();
            try {
                synchronized (mLock) {
                    Log.d(this, "stopDtmfTone(%s)", callId);
                    Call call = mCallIdMapper.getCall(callId);
                    if (call != null) {
                        mCallsManager.stopDtmfTone(call);
                    } else {
                        Log.w(this, "stopDtmfTone, unknown call id: %s", callId);
                    }
                }
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        } finally {
            Log.endSession();
        }
    }

    @Override
    public void postDialContinue(String callId, boolean proceed) {
        try {
            Log.startSession("ICA.pDC", mOwnerComponentName);
            long token = Binder.clearCallingIdentity();
            try {
                synchronized (mLock) {
                    Log.d(this, "postDialContinue(%s)", callId);
                    Call call = mCallIdMapper.getCall(callId);
                    if (call != null) {
                        mCallsManager.postDialContinue(call, proceed);
                    } else {
                        Log.w(this, "postDialContinue, unknown call id: %s", callId);
                    }
                }
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        } finally {
            Log.endSession();
        }
    }

    @Override
    public void disconnectCall(String callId) {
        try {

            Log.startSession(Log.Sessions.ICA_DISCONNECT_CALL, mOwnerComponentName);
            ///M: [Performance Track] @{
            Call c = mCallIdMapper.getCall(callId);
            if (c != null && !c.isConference()) {
                PerformanceTracker.getInstance().trackEndCall(
                        c.getId(), PerformanceTracker.END_CALL_OPERATION_RECEIVED);
            }
            ///@}

            long token = Binder.clearCallingIdentity();
            try {
                synchronized (mLock) {
                    Log.v(this, "disconnectCall: %s", callId);
                    Call call = mCallIdMapper.getCall(callId);
                    /// M: for log parser @{
                    LogUtils.logCcOp(call, LogUtils.OP_ACTION_HANGUP, callId, "");
                    /// @}
                    if (call != null) {
                        /// M: [log optimize]
                        Log.disconnectEvent(call, "InCallAdapter.disconnectCall: UI hangup");
                        /// M: fix CR:ALPS03026582 & ALPS03346549
                        // CR:ALPS03026582: hang up call happen system API Dump.
                        // ALPS03346549 do not stop recording when disconnect conference member. @{
                        if (call.getParentCall() == null) {
                            PhoneRecorderHandler.getInstance().stopVoiceRecord();
                        }
                        /// @}
                        mCallsManager.disconnectCall(call);
                    } else {
                        Log.w(this, "disconnectCall, unknown call id: %s", callId);
                    }
                }
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        } finally {
            Log.endSession();
        }
    }

    @Override
    public void holdCall(String callId) {
        try {
            Log.startSession(Log.Sessions.ICA_HOLD_CALL, mOwnerComponentName);
            long token = Binder.clearCallingIdentity();
            try {
                synchronized (mLock) {
                    Call call = mCallIdMapper.getCall(callId);
                    /// M: for log parser @{
                    LogUtils.logCcOp(call, LogUtils.OP_ACTION_HOLD, callId, "");
                    /// @}
                    if (call != null) {
                        mCallsManager.holdCall(call);
                    } else {
                        Log.w(this, "holdCall, unknown call id: %s", callId);
                    }
                }
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        } finally {
            Log.endSession();
        }
    }

    @Override
    public void unholdCall(String callId) {
        try {
            Log.startSession(Log.Sessions.ICA_UNHOLD_CALL, mOwnerComponentName);
            long token = Binder.clearCallingIdentity();
            try {
                synchronized (mLock) {
                    Call call = mCallIdMapper.getCall(callId);
                    /// M: for log parser @{
                    LogUtils.logCcOp(call, LogUtils.OP_ACTION_UNHOLD, callId, "");
                    /// @}
                    if (call != null) {
                        mCallsManager.unholdCall(call);
                    } else {
                        Log.w(this, "unholdCall, unknown call id: %s", callId);
                    }
                }
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        } finally {
            Log.endSession();
        }
    }

    @Override
    public void phoneAccountSelected(String callId, PhoneAccountHandle accountHandle,
            boolean setDefault) {
        try {
            Log.startSession("ICA.pAS", mOwnerComponentName);
            long token = Binder.clearCallingIdentity();
            try {
                synchronized (mLock) {
                    Call call = mCallIdMapper.getCall(callId);
                    if (call != null) {
                        mCallsManager.phoneAccountSelected(call, accountHandle, setDefault);
                    } else {
                        Log.w(this, "phoneAccountSelected, unknown call id: %s", callId);
                    }
                }
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        } finally {
            Log.endSession();
        }
    }

    @Override
    public void mute(boolean shouldMute) {
        try {
            Log.startSession(Log.Sessions.ICA_MUTE, mOwnerComponentName);
            long token = Binder.clearCallingIdentity();
            try {
                synchronized (mLock) {
                    mCallsManager.mute(shouldMute);
                }
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        } finally {
            Log.endSession();
        }
    }

    @Override
    public void setAudioRoute(int route) {
        try {
            Log.startSession(Log.Sessions.ICA_SET_AUDIO_ROUTE, mOwnerComponentName);
            long token = Binder.clearCallingIdentity();
            try {
                synchronized (mLock) {
                    mCallsManager.setAudioRoute(route);
                }
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        } finally {
            Log.endSession();
        }
    }

    @Override
    public void conference(String callId, String otherCallId) {
        try {

            Log.startSession(Log.Sessions.ICA_CONFERENCE, mOwnerComponentName);
            /// M: [CTA] checking conference permission. @{
            if (!TelecomUtils.checkCallingCtaPermission(
                    com.mediatek.Manifest.permission.CTA_CONFERENCE_CALL,
                    mOwnerComponentName, "Merge conference")) {
                Log.w(this, "[conference]Failed to Merge conference, no permission");
                TelecomUtils.showToast(com.mediatek.R.string.denied_required_permission);
                return;
            }
            /// @}
            long token = Binder.clearCallingIdentity();
            try {
                synchronized (mLock) {
                    Call call = mCallIdMapper.getCall(callId);
                    Call otherCall = mCallIdMapper.getCall(otherCallId);
                    if (call != null && otherCall != null) {
                        /// M: for log parser @{
                        // use extraMsg to record information of otherCall
                        LogUtils.logCcOp(call, LogUtils.OP_ACTION_CONFERENCE, callId,
                                otherCall.toString());
                        /// @}
                        mCallsManager.conference(call, otherCall);
                    } else {
                        Log.w(this, "conference, unknown call id: %s or %s", callId, otherCallId);
                    }
                }
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        } finally {
            Log.endSession();
        }
    }

    @Override
    public void splitFromConference(String callId) {
        try {
            Log.startSession("ICA.sFC", mOwnerComponentName);
            long token = Binder.clearCallingIdentity();
            try {
                synchronized (mLock) {
                    Call call = mCallIdMapper.getCall(callId);
                    /// M: for log parser @{
                    LogUtils.logCcOp(call, LogUtils.OP_ACTION_SPLIT, callId, "");
                    /// @}
                    if (call != null) {
                        call.splitFromConference();
                    } else {
                        Log.w(this, "splitFromConference, unknown call id: %s", callId);
                    }
                }
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        } finally {
            Log.endSession();
        }
    }

    @Override
    public void mergeConference(String callId) {
        try {
            Log.startSession("ICA.mC", mOwnerComponentName);
            /// M: [CTA] checking conference permission. @{
            if (!TelecomUtils.checkCallingCtaPermission(
                    com.mediatek.Manifest.permission.CTA_CONFERENCE_CALL,
                    mOwnerComponentName, "Merge CDMA(?) conference")) {
                Log.w(this, "[mergeConference]Failed to Merge CDMA conference, no permission");
                TelecomUtils.showToast(com.mediatek.R.string.denied_required_permission);
                return;
            }
            /// @}
            long token = Binder.clearCallingIdentity();
            try {
                synchronized (mLock) {
                    Call call = mCallIdMapper.getCall(callId);
                    if (call != null) {
                        call.mergeConference();
                    } else {
                        Log.w(this, "mergeConference, unknown call id: %s", callId);
                    }
                }
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        } finally {
            Log.endSession();
        }
    }

    @Override
    public void swapConference(String callId) {
        try {
            Log.startSession("ICA.sC", mOwnerComponentName);
            long token = Binder.clearCallingIdentity();
            try {
                synchronized (mLock) {
                    Call call = mCallIdMapper.getCall(callId);
                    if (call != null) {
                        call.swapConference();
                    } else {
                        Log.w(this, "swapConference, unknown call id: %s", callId);
                    }
                }
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        } finally {
            Log.endSession();
        }
    }

    @Override
    public void pullExternalCall(String callId) {
        try {
            Log.startSession("ICA.pEC", mOwnerComponentName);
            long token = Binder.clearCallingIdentity();
            try {
                synchronized (mLock) {
                    Call call = mCallIdMapper.getCall(callId);
                    if (call != null) {
                        call.pullExternalCall();
                    } else {
                        Log.w(this, "pullExternalCall, unknown call id: %s", callId);
                    }
                }
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        } finally {
            Log.endSession();
        }
    }

    @Override
    public void sendCallEvent(String callId, String event, Bundle extras) {
        try {
            Log.startSession("ICA.sCE", mOwnerComponentName);
            long token = Binder.clearCallingIdentity();
            try {
                synchronized (mLock) {
                    Call call = mCallIdMapper.getCall(callId);
                    if (call != null) {
                        call.sendCallEvent(event, extras);
                    } else {
                        Log.w(this, "sendCallEvent, unknown call id: %s", callId);
                    }
                }
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        } finally {
            Log.endSession();
        }
    }

    @Override
    public void putExtras(String callId, Bundle extras) {
        try {
            Log.startSession("ICA.pE", mOwnerComponentName);
            long token = Binder.clearCallingIdentity();
            try {
                synchronized (mLock) {
                    Call call = mCallIdMapper.getCall(callId);
                    if (call != null) {
                        call.putExtras(Call.SOURCE_INCALL_SERVICE, extras);
                    } else {
                        Log.w(this, "putExtras, unknown call id: %s", callId);
                    }
                }
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        } finally {
            Log.endSession();
        }
    }

    @Override
    public void removeExtras(String callId, List<String> keys) {
        try {
            Log.startSession("ICA.rE", mOwnerComponentName);
            long token = Binder.clearCallingIdentity();
            try {
                synchronized (mLock) {
                    Call call = mCallIdMapper.getCall(callId);
                    if (call != null) {
                        call.removeExtras(Call.SOURCE_INCALL_SERVICE, keys);
                    } else {
                        Log.w(this, "removeExtra, unknown call id: %s", callId);
                    }
                }
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        } finally {
            Log.endSession();
        }
    }

    @Override
    public void turnOnProximitySensor() {
        try {
            Log.startSession("ICA.tOnPS", mOwnerComponentName);
            long token = Binder.clearCallingIdentity();
            try {
                synchronized (mLock) {
                    mCallsManager.turnOnProximitySensor();
                }
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        } finally {
            Log.endSession();
        }
    }

    @Override
    public void turnOffProximitySensor(boolean screenOnImmediately) {
        try {
            Log.startSession("ICA.tOffPS", mOwnerComponentName);
            long token = Binder.clearCallingIdentity();
            try {
                synchronized (mLock) {
                    mCallsManager.turnOffProximitySensor(screenOnImmediately);
                }
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        } finally {
             Log.endSession();
        }
    }

    /**
     * M: Start voice recording
     */
    @Override
    public void startVoiceRecording() {
        /// M: checking runtime permission set @{
        if (!TelecomUtils.checkCallingPermission(TelecomSystem.getInstance().getContext(),
                android.Manifest.permission.RECORD_AUDIO,
                mOwnerComponentName, "Start voice recording")) {
            Log.w(this, "[startVoiceRecording]Failed to record, no permission");
            TelecomUtils.showToast(com.mediatek.R.string.denied_required_permission);
            return;
        }
        /// @}
        long token = Binder.clearCallingIdentity();
        try {
            synchronized (mLock) {
                Call activeCall = mCallsManager.getActiveCall();
                if (activeCall == null) {
                    Log.w(this, "Cannot startVoiceRecording when no active call exists.");
                } else {
                    if (activeCall.getParentCall() != null) {
                        activeCall = activeCall.getParentCall();
                    }
                    PhoneRecorderHandler.getInstance().startVoiceRecord(activeCall,
                            PhoneRecorderHandler.PHONE_RECORDING_VOICE_CALL_CUSTOM_VALUE);
                }
            }
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    /**
     * M: Stop voice recording
     */
    @Override
    public void stopVoiceRecording() {
        long token = Binder.clearCallingIdentity();
        try {
            synchronized (mLock) {
                PhoneRecorderHandler.getInstance().stopVoiceRecord();
            }
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    @Override
    public void updatePowerForSmartBook(boolean onOff) {
        long token = Binder.clearCallingIdentity();
        try {
            synchronized (mLock) {
                mCallsManager.updatePowerForSmartBook(onOff);
            }
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    /**
     * M: Add for OP09 2W request.
     * @return
     */
    @Override
    public void setSortedIncomingCallList(List<String> list) {
        long token = Binder.clearCallingIdentity();
        try {
            synchronized (mLock) {
                @SuppressWarnings("unchecked")
                final List<Call> callList = getCallListById(list);

                mCallsManager.setSortedIncomingCallList(callList);
            }
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    @Override
    public void explicitCallTransfer(String callId) {
        long token = Binder.clearCallingIdentity();
        try {
            synchronized (mLock) {
                Call call = mCallIdMapper.getCall(callId);
                if (call != null) {
                    mCallsManager.explicitCallTransfer(call);
                }
            }
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    /**
     * M: blind/assured explicit call transfer.
     */
    @Override
    public void blindAssuredEct(String callId, String number, int type) {
        Log.v(this, "blindAssuredEct");
        long token = Binder.clearCallingIdentity();
        try {
            synchronized (mLock) {
                Call call = mCallIdMapper.getCall(callId);
                if (call != null) {
                    mCallsManager.explicitCallTransfer(call, number, type);
                } else {
                    Log.v(this, "can not get call, call id = " + callId);
                }
            }
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    @Override
    public void hangupAll() {
        Log.v(this, "hangupAll()");
        long token = Binder.clearCallingIdentity();
        try {
            synchronized (mLock) {
                mCallsManager.hangupAll();
            }
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    @Override
    public void hangupAllHoldCalls() {
        Log.v(this, "hangupAllHoldCalls()");
        long token = Binder.clearCallingIdentity();
        try {
            synchronized (mLock) {
                mCallsManager.hangupAllHoldCalls();
            }
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    @Override
    public void hangupActiveAndAnswerWaiting() {
        Log.v(this, "hangupActiveAndAnswerWaiting()");
        long token = Binder.clearCallingIdentity();
        try {
            synchronized (mLock) {
                mCallsManager.hangupActiveAndAnswerWaiting();
            }
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    /// M: For VoLTE @{
    @Override
    public void inviteConferenceParticipants(String conferenceCallId, List<String> numbers) {
        Log.v(this, "inviteConferenceParticipants()..." + conferenceCallId + " / " + numbers);
        /// M: [CTA] checking conference permission. @{
        if (!TelecomUtils.checkCallingCtaPermission(
                com.mediatek.Manifest.permission.CTA_CONFERENCE_CALL,
                mOwnerComponentName, "Invite participants to conference")) {
            Log.w(this, "[inviteConferenceParticipants]Failed to Invite participants," +
                    " no permission");
            TelecomUtils.showToast(com.mediatek.R.string.denied_required_permission);
            return;
        }
        /// @}
        long token = Binder.clearCallingIdentity();
        try {
            synchronized (mLock) {
                if (numbers == null || numbers.size() <= 0) {
                    Log.w(this, "inviteConferenceParticipants()...unexpected parameters, abandon");
                    return;
                }
                Call call = mCallIdMapper.getCall(conferenceCallId);
                /// M: for log parser @{
                LogUtils.logCcOp(call, LogUtils.OP_ACTION_ADD_MEMBER,
                        conferenceCallId, LogUtils.numbersToString(numbers));
                /// @}
                if (conferenceCallId != null && call != null) {
                    call.inviteConferenceParticipants(numbers);
                } else {
                    Log.w(this, "inviteConferenceParticipants, unknown conference id: %s",
                            conferenceCallId);
                }
            }
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }
    /// @}

    private List<Call> getCallListById(List<String> list) {
        final List<Call> callList = new ArrayList<Call>(list.size());
        for (String id : list) {
            callList.add(mCallIdMapper.getCall(id));
        }

        return callList;
    }
}
