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

//GSM+CDMA
import com.android.internal.telephony.Connection;
import com.android.internal.telephony.imsphone.ImsPhone;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneConstants;


//GSM
/// M: for VoLTE @{
import com.android.internal.telephony.Phone.FeatureType;
/// @}


//CDMA
import android.os.Handler;
import android.os.Message;

import android.provider.Settings;
import android.telephony.DisconnectCause;
import android.telephony.PhoneNumberUtils;

import com.android.internal.telephony.Call;
import com.android.internal.telephony.CallStateException;
import com.android.internal.telephony.imsphone.ImsPhoneConnection;
import com.android.phone.settings.SettingsConstants;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Manages a single phone call handled by GSM.
 */
final class GsmCdmaConnection extends TelephonyConnection {
    private static final String TAG = "GsmCdmaConnection";

    // GSM+CDMA
    private int mPhoneType;


    //GSM


    //CDMA
    private static final int MSG_CALL_WAITING_MISSED = 1;
    private static final int MSG_DTMF_SEND_CONFIRMATION = 2;
    private static final int TIMEOUT_CALL_WAITING_MILLIS = 20 * 1000;
    /// M: CDMA call fake hold handling. @{
    private static final int MSG_CDMA_CALL_SWITCH = 3;
    private static final int MSG_CDMA_CALL_SWITCH_DELAY = 200;
    private static final int FAKE_HOLD = 1;
    private static final int FAKE_UNHOLD = 0;
    /// @}

    private final Handler mHandler = new Handler() {

        /** ${inheritDoc} */
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_CALL_WAITING_MISSED:
                    hangupCallWaiting(DisconnectCause.INCOMING_MISSED);
                    break;
                case MSG_DTMF_SEND_CONFIRMATION:
                    handleBurstDtmfConfirmation();
                    break;
                /// M: CDMA call fake hold handling. @{
                case MSG_CDMA_CALL_SWITCH:
                    handleFakeHold(msg.arg1);
                    break;
                /// @}
                default:
                    break;
            }
        }

    };

    /**
     * {@code True} if the CDMA connection should allow mute.
     */
    private boolean mAllowMute;
    private final boolean mIsOutgoing;
    // Queue of pending short-DTMF characters.
    private final Queue<Character> mDtmfQueue = new LinkedList<>();
    private final EmergencyTonePlayer mEmergencyTonePlayer;

    // Indicates that the DTMF confirmation from telephony is pending.
    private boolean mDtmfBurstConfirmationPending = false;
    private boolean mIsCallWaiting;

    /// M: Add flag to indicate if the CDMA call is fake dialing @{
    // For CDMA third part call, if the second call is MO call,
    // the state will changed to ACTIVE during force dialing,
    // so need to check if need to update the ACTIVE to telecom.
    private boolean mIsForceDialing = false;
    /// @}

    /// M: CDMA call fake hold handling. @{
    private static final boolean MTK_SVLTE_SUPPORT =
            "1".equals(android.os.SystemProperties.get("ro.boot.opt_c2k_lte_mode"));
    /// @}

    GsmCdmaConnection(
            int phoneType,
            Connection connection,
            String telecomCallId,
            EmergencyTonePlayer emergencyTonePlayer,
            boolean allowMute,
            boolean isOutgoing) {

        // GSM+CDMA
        super(connection, telecomCallId);
        mPhoneType = phoneType;
        Log.d(this, "GsmCdmaConnection constructor mPhoneType = " + mPhoneType);

        // CDMA
        mEmergencyTonePlayer = emergencyTonePlayer;
        mAllowMute = allowMute;
        mIsOutgoing = isOutgoing;
        mIsCallWaiting = connection != null && connection.getState() == Call.State.WAITING;
        boolean isImsCall = getOriginalConnection() instanceof ImsPhoneConnection;
        // Start call waiting timer for CDMA waiting call.
        if (mPhoneType == PhoneConstants.PHONE_TYPE_CDMA) {
            if (mIsCallWaiting && !isImsCall) {
                startCallWaitingTimer();
            }
        }
    }


    // GSM+CDMA
    public int getPhoneType() {
        return mPhoneType;
    }

    // GSM+CDMA
    @Override
    void setOriginalConnection(Connection originalConnection) {
        int oldPhoneType = mPhoneType;
        int origPhoneType = originalConnection.getPhoneType();
        if (origPhoneType == PhoneConstants.PHONE_TYPE_IMS) {
            Phone origPhone = originalConnection.getCall().getPhone();
            mPhoneType = (((ImsPhone) origPhone).getDefaultPhone()).getPhoneType();
        } else {
            mPhoneType = origPhoneType;
        }

        Log.d(this, "setOriginalConnection origPhoneType: " + origPhoneType
                + "mPhoneType: " + oldPhoneType + " -> " + mPhoneType);

        super.setOriginalConnection(originalConnection);
        // CDMA
        mIsCallWaiting = originalConnection != null &&
                originalConnection.getState() == Call.State.WAITING;
        boolean isImsCall = getOriginalConnection() instanceof ImsPhoneConnection;
        // Start call waiting timer for CDMA waiting call.
        if (mPhoneType == PhoneConstants.PHONE_TYPE_CDMA) {
            if (mIsCallWaiting && mHandler != null && !isImsCall) {
                startCallWaitingTimer();
            }
        }
    }

    // GSM+CDMA
    /**
     * Clones the current {@link GsmConnection}.
     * <p>
     * Listeners are not copied to the new instance.
     *
     * @return The cloned connection.
     */
    @Override
    public TelephonyConnection cloneConnection() {
        GsmCdmaConnection gsmCdmaConnection = new GsmCdmaConnection(
                mPhoneType,
                getOriginalConnection(),
                getTelecomCallId(),
                mEmergencyTonePlayer, mAllowMute, mIsOutgoing);
        return gsmCdmaConnection;
    }

    // GSM+CDMA
    /** {@inheritDoc} */
    @Override
    public void onPlayDtmfTone(char digit) {
        if (mPhoneType == PhoneConstants.PHONE_TYPE_GSM) {
            if (getPhone() != null) {
                getPhone().startDtmf(digit);
                /// M: CC: DTMF request special handling @{
                // Stop DTMF when TelephonyConnection is disconnected
                mDtmfRequestIsStarted = true;
                /// @}
            }
        } else if (mPhoneType == PhoneConstants.PHONE_TYPE_CDMA) {
            /// M: CC: error handling @{
            if (getPhone() == null) {
                return;
            }
            /// @}
            if (useBurstDtmf()) {
                Log.i(this, "sending dtmf digit as burst");
                sendShortDtmfToNetwork(digit);
            } else {
                Log.i(this, "sending dtmf digit directly");
                getPhone().startDtmf(digit);
            }
        }
    }

    // GSM+CDMA
    /** {@inheritDoc} */
    @Override
    public void onStopDtmfTone() {
        if (mPhoneType == PhoneConstants.PHONE_TYPE_GSM) {
            if (getPhone() != null) {
                getPhone().stopDtmf();
                /// M: CC: DTMF request special handling @{
                // Stop DTMF when TelephonyConnection is disconnected
                mDtmfRequestIsStarted = false;
                /// @}
            }
        } else if (mPhoneType == PhoneConstants.PHONE_TYPE_CDMA) {
            /// M: CC: error handling @{
            if (getPhone() == null) {
                return;
            }
            /// @}
            if (!useBurstDtmf()) {
                getPhone().stopDtmf();
            }
        }
    }

    // GSM
    @Override
    protected int buildConnectionProperties() {
        int properties = super.buildConnectionProperties();
        // PROPERTY_IS_DOWNGRADED_CONFERENCE is permanent on GSM connections -- once it is set, it
        // should be retained.
        if (mPhoneType == PhoneConstants.PHONE_TYPE_GSM) {
            if ((getConnectionProperties() & PROPERTY_IS_DOWNGRADED_CONFERENCE) != 0) {
                properties |= PROPERTY_IS_DOWNGRADED_CONFERENCE;
            }
        }
        return properties;
    }

    // GSM+CDMA
    @Override
    protected int buildConnectionCapabilities() {
        int capabilities = super.buildConnectionCapabilities();
        if (mPhoneType == PhoneConstants.PHONE_TYPE_GSM) {
            capabilities |= CAPABILITY_MUTE;
        } else if (mPhoneType == PhoneConstants.PHONE_TYPE_CDMA) {
            if (mAllowMute) {
                capabilities |= CAPABILITY_MUTE;
            }
        }

        // Overwrites TelephonyConnection.buildConnectionCapabilities() and resets the hold options
        // because all GSM calls should hold, even if the carrier config option is set to not show
        // hold for IMS calls.
        if (mPhoneType == PhoneConstants.PHONE_TYPE_GSM) {
            if (!shouldTreatAsEmergencyCall()) {
                capabilities |= CAPABILITY_SUPPORT_HOLD;
                if (getState() == STATE_ACTIVE || getState() == STATE_HOLDING) {
                    capabilities |= CAPABILITY_HOLD;
                }
            }
        } else if (mPhoneType == PhoneConstants.PHONE_TYPE_CDMA) {
            /// M: CDMA call fake hold handling. @{
            // AOSP doesn't support hold (no HOLD capability & UI) for pure CDMA call,
            // but for IMS CDMA call, HOLD capability & UI is supported
            if (MTK_SVLTE_SUPPORT && !isImsConnection()) {
                boolean isRealConnected = false;
                Connection origConn = getOriginalConnection();
                if ((origConn instanceof com.android.internal.telephony.GsmCdmaConnection)
                        && (com.android.internal.telephony.PhoneConstants.PHONE_TYPE_CDMA
                        == origConn.getPhoneType())) {
                    com.android.internal.telephony.GsmCdmaConnection conn =
                            (com.android.internal.telephony.GsmCdmaConnection) origConn;
                    isRealConnected = conn.isRealConnected();
                } else {
                    isRealConnected = (getState() == STATE_ACTIVE);
                }
                Log.d(this, "buildConnectionCapabilities, origConn:" + origConn
                        + ", isRealConnected:" + isRealConnected);
                if (!shouldTreatAsEmergencyCall()) {
                    capabilities |= CAPABILITY_SUPPORT_HOLD;
                    if ((getState() == STATE_ACTIVE
                            && ((mIsOutgoing && isRealConnected) || !mIsOutgoing))
                            || getState() == STATE_HOLDING) {
                        capabilities |= CAPABILITY_HOLD;
                    }
                }
            }
            /// @}
        }

        if (mPhoneType == PhoneConstants.PHONE_TYPE_GSM) {
            // For GSM connections, CAPABILITY_CONFERENCE_HAS_NO_CHILDREN should be applied whenever
            // PROPERTY_IS_DOWNGRADED_CONFERENCE is true.
            if ((getConnectionProperties() & PROPERTY_IS_DOWNGRADED_CONFERENCE) != 0) {
                capabilities |= CAPABILITY_CONFERENCE_HAS_NO_CHILDREN;
            }

            /// M: CC: Interface for ECT @{
            if (getConnectionService() != null) {
                if (getConnectionService().canTransfer(this)) {
                    capabilities |= CAPABILITY_ECT;
                }

                if (getConnectionService().canBlindAssuredTransfer(this)
                        && getPhone().isFeatureSupported(FeatureType.VOLTE_ECT)) {
                    if (getState() == STATE_ACTIVE && !shouldTreatAsEmergencyCall()) {
                        capabilities |= CAPABILITY_BLIND_ASSURED_ECT;
                    }
                }
            }
            /// @}

            /// M: For VoLTE @{
            //if (SystemProperties.get("persist.mtk_volte_support").equals("1")) {
            //    if (getPhone() != null) {
            //        int curPhoneType = getPhone().getPhoneType();
            //        if (curPhoneType == PhoneConstants.PHONE_TYPE_IMS) {
            //            capabilities |= CAPABILITY_VOLTE;
            //        }
            //    }
            //}
        }
        log("buildConnectionCapabilities: " + capabilitiesToString(capabilities));

        return capabilities;
    }

    // GSM?
    @Override
    void onRemovedFromCallService() {
        super.onRemovedFromCallService();
    }

    // GSM?
    private void log(String s) {
        Log.d(TAG, s);
    }

    // CDMA
    @Override
    public void onReject() {
        if (mPhoneType == PhoneConstants.PHONE_TYPE_GSM) {
            super.onReject();
        } else if (mPhoneType == PhoneConstants.PHONE_TYPE_CDMA) {
            Connection connection = getOriginalConnection();
            if (connection != null) {
                switch (connection.getState()) {
                    case INCOMING:
                        // Normal ringing calls are handled the generic way.
                        super.onReject();
                        break;
                    case WAITING:
                        hangupCallWaiting(DisconnectCause.INCOMING_REJECTED);
                        break;
                    default:
                        Log.e(this, new Exception(), "Rejecting a non-ringing call");
                        // might as well hang this up, too.
                        super.onReject();
                        break;
                }
            }
        }
    }

    // CDMA
    @Override
    public void onAnswer() {
        if (mPhoneType == PhoneConstants.PHONE_TYPE_CDMA) {
            mHandler.removeMessages(MSG_CALL_WAITING_MISSED);
        }
        super.onAnswer();
    }

    // CDMA
    @Override
    public void onStateChanged(int state) {
        if (mPhoneType == PhoneConstants.PHONE_TYPE_CDMA) {
            Connection originalConnection = getOriginalConnection();
            mIsCallWaiting = originalConnection != null &&
                    originalConnection.getState() == Call.State.WAITING;

            if (mEmergencyTonePlayer != null) {
                if (state == android.telecom.Connection.STATE_DIALING) {
                    if (isEmergency()) {
                        mEmergencyTonePlayer.start();
                    }
                } else {
                    // No need to check if it is an emergency call, since it is a no-op if it
                    // isn't started.
                    mEmergencyTonePlayer.stop();
                }
            }
        }
        super.onStateChanged(state);
    }

    // CDMA
    @Override
    public void performConference(android.telecom.Connection otherConnection) {
        if (mPhoneType == PhoneConstants.PHONE_TYPE_GSM) {
            super.performConference(otherConnection);
        } else if (mPhoneType == PhoneConstants.PHONE_TYPE_CDMA) {
            if (isImsConnection()) {
                super.performConference(otherConnection);
            } else {
                Log.w(this, "Non-IMS CDMA Connection attempted to call performConference.");
            }
        }
    }

    // CDMA
    void forceAsDialing(boolean isDialing) {
        if (isDialing) {
            setStateOverride(Call.State.DIALING);
            /// M: Add flag to indicate if the CDMA call is fake dialing @{
            mIsForceDialing = true;
            /// @}
        } else {
            resetStateOverride();
            /// M: Add flag to indicate if the CDMA call is fake dialing @{
            mIsForceDialing = false;
            /// @}
        }
    }

    // CDMA
    boolean isOutgoing() {
        return mIsOutgoing;
    }

    // CDMA
    boolean isCallWaiting() {
        return mIsCallWaiting;
    }

    // CDMA
    /**
     * We do not get much in the way of confirmation for Cdma call waiting calls. There is no
     * indication that a rejected call succeeded, a call waiting call has stopped. Instead we
     * simulate this for the user. We allow TIMEOUT_CALL_WAITING_MILLIS milliseconds before we
     * assume that the call was missed and reject it ourselves. reject the call automatically.
     */
    private void startCallWaitingTimer() {
        /// M: CC: Vzw/CTVolte ECC @{
        mHandler.removeMessages(MSG_CALL_WAITING_MISSED);
        //// @}
        mHandler.sendEmptyMessageDelayed(MSG_CALL_WAITING_MISSED, TIMEOUT_CALL_WAITING_MILLIS);
    }

    // CDMA
    private void hangupCallWaiting(int telephonyDisconnectCause) {
        Connection originalConnection = getOriginalConnection();
        if (originalConnection != null) {
            try {
                originalConnection.hangup();
            } catch (CallStateException e) {
                Log.e(this, e, "Failed to hangup call waiting call");
            }
            setDisconnected(DisconnectCauseUtil.toTelecomDisconnectCause(telephonyDisconnectCause));
        }
    }

    // CDMA
    /**
     * Read the settings to determine which type of DTMF method this CDMA phone calls.
     */
    private boolean useBurstDtmf() {
        if (isImsConnection()) {
            Log.d(this, "in ims call, return false");
            return false;
        }
        int dtmfTypeSetting = Settings.System.getInt(
                getPhone().getContext().getContentResolver(),
                Settings.System.DTMF_TONE_TYPE_WHEN_DIALING,
                SettingsConstants.DTMF_TONE_TYPE_NORMAL);
        return dtmfTypeSetting == SettingsConstants.DTMF_TONE_TYPE_NORMAL;
    }

    // CDMA
    private void sendShortDtmfToNetwork(char digit) {
        synchronized (mDtmfQueue) {
            if (mDtmfBurstConfirmationPending) {
                mDtmfQueue.add(new Character(digit));
            } else {
                sendBurstDtmfStringLocked(Character.toString(digit));
            }
        }
    }

    // CDMA
    private void sendBurstDtmfStringLocked(String dtmfString) {
        /// M: Add null check to avoid timing issue. @{
        Phone phone = getPhone();
        if (phone != null) {
            phone.sendBurstDtmf(
                    dtmfString, 0, 0, mHandler.obtainMessage(MSG_DTMF_SEND_CONFIRMATION));
            mDtmfBurstConfirmationPending = true;
        }
        /// @}
    }

    // CDMA
    private void handleBurstDtmfConfirmation() {
        String dtmfDigits = null;
        synchronized (mDtmfQueue) {
            mDtmfBurstConfirmationPending = false;
            if (!mDtmfQueue.isEmpty()) {
                StringBuilder builder = new StringBuilder(mDtmfQueue.size());
                while (!mDtmfQueue.isEmpty()) {
                    builder.append(mDtmfQueue.poll());
                }
                dtmfDigits = builder.toString();

                // It would be nice to log the digit, but since DTMF digits can be passwords
                // to things, or other secure account numbers, we want to keep it away from
                // the logs.
                Log.i(this, "%d dtmf character[s] removed from the queue", dtmfDigits.length());
            }
            if (dtmfDigits != null) {
                sendBurstDtmfStringLocked(dtmfDigits);
            }
        }
    }

    // CDMA
    private boolean isEmergency() {
        Phone phone = getPhone();
        return phone != null &&
                PhoneNumberUtils.isLocalEmergencyNumber(
                    phone.getContext(), getAddress().getSchemeSpecificPart());
    }

    // CDMA
    /**
     * Called when ECM mode is exited; set the connection to allow mute and update the connection
     * capabilities.
     */
    @Override
    protected void handleExitedEcmMode() {
        // We allow mute upon existing ECM mode and rebuild the capabilities.
        mAllowMute = true;
        super.handleExitedEcmMode();
    }

    // CDMA
    /// M: CC: HangupAll for FTA 31.4.4.2 @{
    /**
     * CDMA hangup all is different to GSM, no CHLD=6 to hang up all calls in a phone
     */
    @Override
    public void onHangupAll() {
        if (mPhoneType == PhoneConstants.PHONE_TYPE_GSM) {
            super.onHangupAll();
        } else if (mPhoneType == PhoneConstants.PHONE_TYPE_CDMA) {
            Log.d(this, "onHangupAll");
            if (getOriginalConnection() != null) {
                try {
                    Call call = getOriginalConnection().getCall();
                    if (call != null) {
                        call.hangup();
                    } else {
                        Log.w(this, "call is null.");
                    }
                } catch (CallStateException e) {
                    Log.e(this, e, "Failed to hangup the call.");
                }
            }
        }
    }
    /// @}

    // CDMA
    /// M: CDMA call fake hold handling. @{
    @Override
    public void performHold() {
        if (mPhoneType == PhoneConstants.PHONE_TYPE_GSM) {
            super.performHold();
        } else if (mPhoneType == PhoneConstants.PHONE_TYPE_CDMA) {
            /// M: CDMA call fake hold handling. @{
            // AOSP doesn't support hold (no HOLD capability & UI) for pure CDMA call,
            // but for IMS CDMA call, HOLD capability & UI is supported
            if (MTK_SVLTE_SUPPORT && !isImsConnection()) {
                Log.d(this, "performHold, just set the hold status.");
                mHandler.sendMessageDelayed(
                        Message.obtain(mHandler, MSG_CDMA_CALL_SWITCH, FAKE_HOLD, 0),
                        MSG_CDMA_CALL_SWITCH_DELAY);
            } else {
                super.performHold();
            }
            /// @}
        }
    }

    // CDMA
    @Override
    public void performUnhold() {
        if (mPhoneType == PhoneConstants.PHONE_TYPE_GSM) {
            super.performUnhold();
        } else if (mPhoneType == PhoneConstants.PHONE_TYPE_CDMA) {
            /// M: CDMA call fake hold handling. @{
            // AOSP doesn't support hold (no HOLD capability & UI) for pure CDMA call,
            // but for IMS CDMA call, HOLD capability & UI is supported
            if (MTK_SVLTE_SUPPORT && !isImsConnection()) {
                Log.d(this, "performUnhold, just set the active status.");
                mHandler.sendMessageDelayed(
                        Message.obtain(mHandler, MSG_CDMA_CALL_SWITCH, FAKE_UNHOLD, 0),
                        MSG_CDMA_CALL_SWITCH_DELAY);
            } else {
                super.performUnhold();
            }
            /// @}
        }
    }

    // CDMA
    private void handleFakeHold(int fakeOp) {
        Log.d(this, "handleFakeHold with operation %s", fakeOp);
        if (FAKE_HOLD == fakeOp) {
            setOnHold();
        } else if (FAKE_UNHOLD == fakeOp) {
            setActive();
        }
        fireOnCallState();
    }
    /// @}

    // CDMA
    /// M: Add flag to indicate if the CDMA call is fake dialing @{
    @Override
    boolean isForceDialing() {
        return mIsForceDialing;
    }
    /// @}
}
