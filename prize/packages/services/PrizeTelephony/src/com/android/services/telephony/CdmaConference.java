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

import android.content.Context;
import android.os.PersistableBundle;
import android.telecom.Conference;
import android.telecom.Connection;
import android.telecom.PhoneAccountHandle;
import android.telephony.CarrierConfigManager;

import com.android.internal.telephony.Call;
import com.android.internal.telephony.CallStateException;
import com.android.phone.PhoneGlobals;
import com.android.phone.common.R;
/// M: CC: Vzw/CTVolte ECC @{
import com.android.internal.telephony.PhoneConstants;

import java.util.List;

/**
 * CDMA-based conference call.
 */
public class CdmaConference extends Conference {
    private int mCapabilities;
    private int mProperties;

    public CdmaConference(PhoneAccountHandle phoneAccount) {
        super(phoneAccount);
        setActive();

        mProperties = Connection.PROPERTY_GENERIC_CONFERENCE;
        setConnectionProperties(mProperties);
    }

    public void updateCapabilities(int capabilities) {
        capabilities |= Connection.CAPABILITY_MUTE;
        /// M: Not allow mute in ECBM and update after exit ECBM @{
        mCapabilities |= capabilities;
        /// @}
        setConnectionCapabilities(buildConnectionCapabilities());
    }

    /**
     * Invoked when the Conference and all it's {@link Connection}s should be disconnected.
     */
    @Override
    public void onDisconnect() {
        Call call = getOriginalCall();
        if (call != null) {
            Log.d(this, "Found multiparty call to hangup for conference.");
            try {
                call.hangup();
            } catch (CallStateException e) {
                Log.e(this, e, "Exception thrown trying to hangup conference");
            }
        }
    }

    @Override
    public void onSeparate(Connection connection) {
        Log.e(this, new Exception(), "Separate not supported for CDMA conference call.");
    }

    @Override
    public void onHold() {
        /// M: CDMA call fake hold handling. @{
        //Log.e(this, new Exception(), "Hold not supported for CDMA conference call.");
        Log.d(this, "onHold, just set the hold status.");
        mHandler.sendMessageDelayed(
                android.os.Message.obtain(mHandler, MSG_CDMA_CALL_SWITCH, FAKE_HOLD, 0),
                MSG_CDMA_CALL_SWITCH_DELAY);
        /// @}
    }

    /**
     * Invoked when the conference should be moved from hold to active.
     */
    @Override
    public void onUnhold() {
        /// M: CDMA call fake hold handling. @{
        //Log.e(this, new Exception(), "Unhold not supported for CDMA conference call.");
        Log.d(this, "onUnhold, just set the unhold status.");
        mHandler.sendMessageDelayed(
                android.os.Message.obtain(mHandler, MSG_CDMA_CALL_SWITCH, FAKE_UNHOLD, 0),
                MSG_CDMA_CALL_SWITCH_DELAY);
        /// @}
    }

    @Override
    public void onMerge() {
        Log.i(this, "Merging CDMA conference call.");
        // Can only merge once
        mCapabilities &= ~Connection.CAPABILITY_MERGE_CONFERENCE;
        // Once merged, swap is enabled.
        if (isSwapSupportedAfterMerge()){
            mCapabilities |= Connection.CAPABILITY_SWAP_CONFERENCE;
        }
        updateCapabilities(mCapabilities);
        sendFlash();

        /// M: ALPS02142143 @{
        // If the call in HOLDING status before merging, then unhold
        // it, avoid the call keep holding status and no "unhold" button
        // to recover it.
        if (getState() == Connection.STATE_HOLDING) {
            onUnhold();
        }
        /// @}
    }

    @Override
    public void onPlayDtmfTone(char c) {
        /// M: CC: Vzw/CTVolte ECC @{
        //final CdmaConnection connection = getFirstConnection();
        final GsmCdmaConnection connection = getFirstConnection();
        /// @}
        if (connection != null) {
            connection.onPlayDtmfTone(c);
        } else {
            Log.w(this, "No CDMA connection found while trying to play dtmf tone.");
        }
    }

    @Override
    public void onStopDtmfTone() {
        /// M: CC: Vzw/CTVolte ECC @{
        //final CdmaConnection connection = getFirstConnection();
        final GsmCdmaConnection connection = getFirstConnection();
        /// @}
        if (connection != null) {
            connection.onStopDtmfTone();
        } else {
            Log.w(this, "No CDMA connection found while trying to stop dtmf tone.");
        }
    }

    @Override
    public void onSwap() {
        Log.i(this, "Swapping CDMA conference call.");
        sendFlash();
    }

    private void sendFlash() {
        Call call = getOriginalCall();
        if (call != null) {
            try {
                // For CDMA calls, this just sends a flash command.
                call.getPhone().switchHoldingAndActive();
            } catch (CallStateException e) {
                Log.e(this, e, "Error while trying to send flash command.");
            }
        }
    }

    private Call getMultipartyCallForConnection(Connection connection) {
        com.android.internal.telephony.Connection radioConnection =
                getOriginalConnection(connection);
        if (radioConnection != null) {
            Call call = radioConnection.getCall();
            if (call != null && call.isMultiparty()) {
                return call;
            }
        }
        return null;
    }

    private Call getOriginalCall() {
        List<Connection> connections = getConnections();
        if (!connections.isEmpty()) {
            com.android.internal.telephony.Connection originalConnection =
                    getOriginalConnection(connections.get(0));
            if (originalConnection != null) {
                return originalConnection.getCall();
            }
        }
        return null;
    }

    /**
     * Return whether network support swap after merge conference call.
     *
     * @return true to support, false not support.
     */
    private final boolean isSwapSupportedAfterMerge()
    {
        boolean supportSwapAfterMerge = true;
        Context context = PhoneGlobals.getInstance();

        if (context != null) {
            CarrierConfigManager configManager = (CarrierConfigManager) context.getSystemService(
                    Context.CARRIER_CONFIG_SERVICE);
            /// M: CC: Get the config of the current phone @{
            //PersistableBundle b = configManager.getConfig();
            PersistableBundle b = null;
            Call call = getOriginalCall();
            if (call != null && call.getPhone() != null) {
                b = configManager.getConfigForSubId(call.getPhone().getSubId());
            }
            /// @}
            if (b != null) {
                supportSwapAfterMerge =
                        b.getBoolean(CarrierConfigManager.KEY_SUPPORT_SWAP_AFTER_MERGE_BOOL);
                Log.d(this, "Current network support swap after call merged capability is "
                        + supportSwapAfterMerge);
            }
        }
        return supportSwapAfterMerge;
    }

    private com.android.internal.telephony.Connection getOriginalConnection(Connection connection) {
        /// M: CC: Vzw/CTVolte ECC @{
        //if (connection instanceof CdmaConnection) {
        //    return ((CdmaConnection) connection).getOriginalConnection();
        if (connection instanceof GsmCdmaConnection &&
            ((GsmCdmaConnection) connection).getPhoneType() == PhoneConstants.PHONE_TYPE_CDMA) {
            return ((GsmCdmaConnection) connection).getOriginalConnection();
        //// @}
        } else {
            Log.e(this, null, "Non CDMA connection found in a CDMA conference");
            return null;
        }
    }

    /// M: CC: Vzw/CTVolte ECC @{
    //private CdmaConnection getFirstConnection() {
    private GsmCdmaConnection getFirstConnection() {
        final List<Connection> connections = getConnections();
        if (connections.isEmpty()) {
            return null;
        }
        /// M: CC: Vzw/CTVolte ECC @{
        //return (CdmaConnection) connections.get(0);
        return (GsmCdmaConnection) connections.get(0);
    }

    /// M: CDMA call fake hold handling. @{
    private static final int MSG_CDMA_CALL_SWITCH = 3;
    private static final int MSG_CDMA_CALL_SWITCH_DELAY = 200;
    private static final int FAKE_HOLD = 1;
    private static final int FAKE_UNHOLD = 0;
    private static final boolean MTK_SVLTE_SUPPORT =
            "1".equals(android.os.SystemProperties.get("ro.boot.opt_c2k_lte_mode"));

    private final android.os.Handler mHandler = new android.os.Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case MSG_CDMA_CALL_SWITCH:
                    handleFakeHold(msg.arg1);
                    break;
                default:
                    break;
            }
        }
    };

    private void handleFakeHold(int fakeOp) {
        Log.d(this, "handleFakeHold with operation %s", fakeOp);
        if (FAKE_HOLD == fakeOp) {
            setOnHold();
        } else if (FAKE_UNHOLD == fakeOp) {
            setActive();
        }
        resetConnectionState();
        updateCapabilities(mCapabilities);
    }

    public void resetConnectionState() {
        int state = getState();
        if (state != Connection.STATE_ACTIVE && state != Connection.STATE_HOLDING) {
            return;
        }

        List<Connection> conns = getConnections();
        for (Connection c : conns) {
            if (c.getState() != state) {
                if (state == Connection.STATE_ACTIVE) {
                    c.setActive();
                } else {
                    c.setOnHold();
                }
                /// M: CC: Vzw/CTVolte ECC @{
                //if (c instanceof CdmaConnection) {
                //    CdmaConnection cc = (CdmaConnection) c;
                if ((c instanceof GsmCdmaConnection) &&
                        ((GsmCdmaConnection) c).getPhoneType() == PhoneConstants.PHONE_TYPE_CDMA) {
                    GsmCdmaConnection cc = (GsmCdmaConnection) c;
                    /// M: The access control is changed to protected
                    cc.fireOnCallState();
                }
            }
        }
    }
    /// @}

    /// M: Not allow mute in ECBM and update after exit ECBM @{
    @Override
    protected int buildConnectionCapabilities() {
        Log.d(this, "buildConnectionCapabilities");

        /*if (getConnections() == null || getConnections().size() == 0) {
            Log.d(this, "No connection exist, update capability to 0.");
            return 0;
        }*/

        boolean inEcm = false;
        Call call = getOriginalCall();
        if (call != null) {
            com.android.internal.telephony.Phone phone = call.getPhone();
            if (phone != null) {
                inEcm = phone.isInEcm();
            }
        }
        if (!inEcm) {
            mCapabilities |= Connection.CAPABILITY_MUTE;
        } else {
            mCapabilities &= ~Connection.CAPABILITY_MUTE;
        }

        /// M: CDMA call fake hold handling. @{
        if (MTK_SVLTE_SUPPORT) {
            mCapabilities |= Connection.CAPABILITY_SUPPORT_HOLD;
            if (getState() == Connection.STATE_ACTIVE || getState() == Connection.STATE_HOLDING) {
                mCapabilities |= Connection.CAPABILITY_HOLD;
            }
        }
        /// @}

        Log.d(this, Connection.capabilitiesToString(mCapabilities));
        return mCapabilities;
    }
    /// @}

    /// M: CC: HangupAll for FTA 31.4.4.2 @{
    /**
     * Hangup all connections in the conference.
     * CDMA hangup all is different to GSM, no CHLD=6 to hang up all calls in a phone
     */
    @Override
    public void onHangupAll() {
        Log.d(this, "onHangupAll");
        if (getFirstConnection() != null) {
            try {
                Call call = getFirstConnection().getOriginalConnection().getCall();
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
    /// @}

    /// M: For CDMA conference @{
    public void removeCapabilities(int capabilities) {
        mCapabilities &= ~capabilities;
        setConnectionCapabilities(buildConnectionCapabilities());
    }
    /// @}

    /// M: CC: For DSDS/DSDA Two-action operation @{
    /**
     * Invoked when the Conference and all it's {@link Connection}s should be disconnected,
     * with pending call action, answer?
     * @param pendingCallAction The pending call action.
     */
    @Override
    public void onDisconnect(String pendingCallAction) {
        onDisconnect();
    }
    /// @}
}
