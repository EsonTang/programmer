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

import android.content.Context;
import android.net.Uri;
import android.os.AsyncResult;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.telecom.PhoneAccount;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;
import android.telephony.Rlog;
import android.text.TextUtils;

import com.android.internal.telephony.Call;
import com.android.internal.telephony.CallStateException;
import com.android.internal.telephony.Connection;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.imsphone.ImsExternalCallTracker;
import com.android.internal.telephony.imsphone.ImsExternalConnection;
import com.android.phone.PhoneUtils;

import com.google.common.base.Preconditions;

import java.util.Objects;


/**
 * Listens to incoming-call events from the associated phone object and notifies Telecom upon each
 * occurence. One instance of these exists for each of the telephony-based call services.
 */
final class PstnNoAccountUnknownCallNotifier {

    // Sensitive log task
    private static final String PROP_FORCE_DEBUG_KEY = "persist.log.tag.tel_dbg";

    private static final int EVENT_UNKNOWN_CONNECTION = 102;

    /** The phone object to listen to. */
    private final Phone mPhone;

    /**
     * Used to listen to events from {@link #mPhone}.
     */
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case EVENT_UNKNOWN_CONNECTION:
                    handleNewUnknownConnection((AsyncResult) msg.obj);
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * Persists the specified parameters and starts listening to phone events.
     *
     * @param phone The phone object for listening to incoming calls.
     */
    PstnNoAccountUnknownCallNotifier(Phone phone) {
        Preconditions.checkNotNull(phone);

        mPhone = phone;

        registerForNotifications();
    }

    void teardown() {
        unregisterForNotifications();
    }

    /**
     * Register for notifications from the base phone.
     */
    private void registerForNotifications() {
        if (mPhone != null) {
            Log.i(this, "Registering: %s", mPhone);
            mPhone.registerForUnknownConnection(mHandler, EVENT_UNKNOWN_CONNECTION, null);
        }
    }

    private void unregisterForNotifications() {
        if (mPhone != null) {
            Log.i(this, "Unregistering: %s", mPhone);
            mPhone.unregisterForUnknownConnection(mHandler);
        }
    }

    private void handleNewUnknownConnection(AsyncResult asyncResult) {
        Log.i(this, "handleNewUnknownConnection");
        if (!(asyncResult.result instanceof Connection)) {
            Log.w(this, "handleNewUnknownConnection called with non-Connection object");
            return;
        }
        Connection connection = (Connection) asyncResult.result;
        if (connection != null) {
            // Because there is a handler between telephony and here, it causes this action to be
            // asynchronous which means that the call can switch to DISCONNECTED by the time it gets
            // to this code. Check here to ensure we are not adding a disconnected or IDLE call.
            Call.State state = connection.getState();
            if (state == Call.State.DISCONNECTED || state == Call.State.IDLE) {
                Log.i(this, "Skipping new unknown connection because it is idle. " + connection);
                return;
            }

            Call call = connection.getCall();
            if (call != null && call.getState().isAlive()) {
                addNewUnknownCall(connection);
            }
        }
    }

    private void addNewUnknownCall(Connection connection) {
        Log.i(this, "addNewUnknownCall, connection is: %s", connection);

        if (!maybeSwapAnyWithUnknownConnection(connection)) {
            Log.i(this, "For no account, no need to create new connection %s", connection);
        } else {
            Log.i(this, "swapped an old connection, new one is: %s", connection);
        }
    }

    /**
     * Define cait.Connection := com.android.internal.telephony.Connection
     *
     * Given a previously unknown cait.Connection, check to see if it's likely a replacement for
     * another cait.Connnection we already know about. If it is, then we silently swap it out
     * underneath within the relevant {@link TelephonyConnection}, using
     * {@link TelephonyConnection#setOriginalConnection(Connection)}, and return {@code true}.
     * Otherwise, we return {@code false}.
     */
    private boolean maybeSwapAnyWithUnknownConnection(Connection unknown) {
        if (!unknown.isIncoming()) {
            TelecomAccountRegistry registry = TelecomAccountRegistry.getInstance(null);
            if (registry != null) {
                TelephonyConnectionService service = registry.getTelephonyConnectionService();
                if (service != null) {
                    for (android.telecom.Connection telephonyConnection : service
                            .getAllConnections()) {
                        if (telephonyConnection instanceof TelephonyConnection) {
                            if (maybeSwapWithUnknownConnection(
                                    (TelephonyConnection) telephonyConnection,
                                    unknown)) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    private boolean maybeSwapWithUnknownConnection(
            TelephonyConnection telephonyConnection,
            Connection unknown) {
        Connection original = telephonyConnection.getOriginalConnection();
        if (original != null && !original.isIncoming()
                && Objects.equals(original.getAddress(), unknown.getAddress())) {
            // If the new unknown connection is an external connection, don't swap one with an
            // actual connection.  This means a call got pulled away.  We want the actual connection
            // to disconnect.
            if (unknown instanceof ImsExternalConnection &&
                    !(telephonyConnection
                            .getOriginalConnection() instanceof ImsExternalConnection)) {
                Log.v(this, "maybeSwapWithUnknownConnection - not swapping regular connection " +
                        "with external connection.");
                return false;
            }

            telephonyConnection.setOriginalConnection(unknown);
            return true;
        }
        return false;
    }
}
