/*
 * Copyright (C) 2016 The Android Open Source Project
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
import android.database.ContentObserver;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;

import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.os.SomeArgs;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.SubscriptionController;

/// M: CC: ECC phone selection rule @{
//Use RadioManager for powering on radio during ECC [ALPS01785370]
import com.mediatek.internal.telephony.RadioManager;
/// @}

/// M: CC: Vzw/CTVolte ECC @{
import com.android.internal.telephony.TelephonyDevController;

import com.mediatek.internal.telephony.IRadioPower;
/// @}

import com.mediatek.telephony.TelephonyManagerEx;

/**
 * Helper class that listens to a Phone's radio state and sends a callback when the radio state of
 * that Phone is either "in service" or "emergency calls only."
 */
public class EmergencyCallStateListener {

    /**
     * Receives the result of the EmergencyCallStateListener's attempt to turn on the radio.
     */
    interface Callback {
        void onComplete(EmergencyCallStateListener listener, boolean isRadioReady);
    }

    // Number of times to retry the call, and time between retry attempts.
    private static int MAX_NUM_RETRIES = 5;
    private static long TIME_BETWEEN_RETRIES_MILLIS = 5000;  // msec

    // Handler message codes; see handleMessage()
    @VisibleForTesting
    public static final int MSG_START_SEQUENCE = 1;
    @VisibleForTesting
    public static final int MSG_SERVICE_STATE_CHANGED = 2;
    @VisibleForTesting
    public static final int MSG_RETRY_TIMEOUT = 3;

    private final Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_START_SEQUENCE:
                    SomeArgs args = (SomeArgs) msg.obj;
                    try {
                        Phone phone = (Phone) args.arg1;
                        EmergencyCallStateListener.Callback callback =
                                (EmergencyCallStateListener.Callback) args.arg2;
                        startSequenceInternal(phone, callback);
                    } finally {
                        args.recycle();
                    }
                    break;
                case MSG_SERVICE_STATE_CHANGED:
                    onServiceStateChanged((ServiceState) ((AsyncResult) msg.obj).result);
                    break;
                case MSG_RETRY_TIMEOUT:
                    onRetryTimeout();
                    break;
                default:
                    Log.wtf(this, "handleMessage: unexpected message: %d.", msg.what);
                    break;
            }
        }
    };


    private Callback mCallback;  // The callback to notify upon completion.
    private Phone mPhone;  // The phone that will attempt to place the call.
    private int mNumRetriesSoFar;

    /// M: For ECC change feature @{
    private TelephonyManager mTm;
    private AirplaneModeObserver mAirplaneModeObserver;
    private Context mContext;
    /// @}

    /// M: CC: Vzw/CTVolte ECC @{
    TelephonyDevController telDevController = TelephonyDevController.getInstance();
    private boolean hasC2kOverImsModem() {
        if (telDevController != null && telDevController.getModem(0) != null &&
                telDevController.getModem(0).hasC2kOverImsModem() == true) {
            return true;
        }
        return false;
    }

    private RadioPowerInterface mRadioPowerIf;
    class RadioPowerInterface implements IRadioPower {
        public void notifyRadioPowerChange(boolean power, int phoneId) {
            Log.d(this, "notifyRadioPowerChange, power:" + power + " phoneId:" + phoneId);
            if (mPhone == null) {
                Log.d(this, "notifyRadioPowerChange, return since mPhone is null");
                return;
            }

            if ((mPhone.getPhoneId() == phoneId) && (power == true)) {
                if (TelephonyManager.getDefault().getPhoneCount() <= 1) {
                    TelephonyConnectionServiceUtil.getInstance()
                            .enterEmergencyMode(mPhone, 1/*airplane*/);
                }
            }
        }
    }
    /// @}

    /**
     * Starts the "wait for radio" sequence. This is the (single) external API of the
     * EmergencyCallStateListener class.
     *
     * This method kicks off the following sequence:
     * - Listen for the service state change event telling us the radio has come up.
     * - Retry if we've gone {@link #TIME_BETWEEN_RETRIES_MILLIS} without any response from the
     *   radio.
     * - Finally, clean up any leftover state.
     *
     * This method is safe to call from any thread, since it simply posts a message to the
     * EmergencyCallStateListener's handler (thus ensuring that the rest of the sequence is entirely
     * serialized, and runs only on the handler thread.)
     */
    public void waitForRadioOn(Phone phone, Callback callback) {
        Log.d(this, "waitForRadioOn: Phone " + phone.getPhoneId());

        if (mPhone != null) {
            // If there already is an ongoing request, ignore the new one!
            return;
        }

        /// M: For ECC change feature @{
        mTm = TelephonyManager.getDefault();
        if (!hasC2kOverImsModem()) {
            mAirplaneModeObserver = new AirplaneModeObserver(mHandler);
        }
        /// @}

        /// M: CC: Vzw/CTVolte ECC @{
        mPhone = phone;
        mRadioPowerIf = new RadioPowerInterface();
        RadioManager.registerForRadioPowerChange("EmergencyCallHelper", mRadioPowerIf);
        /// @}

        SomeArgs args = SomeArgs.obtain();
        args.arg1 = phone;
        args.arg2 = callback;
        mHandler.obtainMessage(MSG_START_SEQUENCE, args).sendToTarget();
    }

    /**
     * Actual implementation of waitForRadioOn(), guaranteed to run on the handler thread.
     *
     * @see #waitForRadioOn
     */
    private void startSequenceInternal(Phone phone, Callback callback) {
        Log.d(this, "startSequenceInternal: Phone " + phone.getPhoneId());

        // First of all, clean up any state left over from a prior emergency call sequence. This
        // ensures that we'll behave sanely if another startTurnOnRadioSequence() comes in while
        // we're already in the middle of the sequence.
        cleanup();

        mPhone = phone;
        mCallback = callback;

        registerForServiceStateChanged();
        // Next step: when the SERVICE_STATE_CHANGED event comes in, we'll retry the call; see
        // onServiceStateChanged(). But also, just in case, start a timer to make sure we'll retry
        // the call even if the SERVICE_STATE_CHANGED event never comes in for some reason.
        startRetryTimer();
    }

    /**
     * Handles the SERVICE_STATE_CHANGED event. Normally this event tells us that the radio has
     * finally come up. In that case, it's now safe to actually place the emergency call.
     */
    private void onServiceStateChanged(ServiceState state) {
        Log.d(this, "onServiceStateChanged(), new state = %s, Phone = %s", state,
                mPhone.getPhoneId());

        /// M: For ECC change feature @{
        Log.d(this, "onServiceStateChanged(), isEmergencyOnly:" + state.isEmergencyOnly()
                + ", phonetype:" + mPhone.getPhoneType()
                + ", hasCard:" + mTm.hasIccCard(mPhone.getPhoneId()));
        /// @}

        // Possible service states:
        // - STATE_IN_SERVICE        // Normal operation
        // - STATE_OUT_OF_SERVICE    // Still searching for an operator to register to,
        //                           // or no radio signal
        // - STATE_EMERGENCY_ONLY    // Phone is locked; only emergency numbers are allowed
        // - STATE_POWER_OFF         // Radio is explicitly powered off (airplane mode)

        if (isOkToCall(state.getState()) || state.isEmergencyOnly()) {
            // Woo hoo!  It's OK to actually place the call.
            Log.d(this, "onServiceStateChanged: ok to call!");

            onComplete(true);
            cleanup();
        } else {
            // The service state changed, but we're still not ready to call yet.
            Log.d(this, "onServiceStateChanged: not ready to call yet, keep waiting.");
        }
    }

    /**
     * We currently only look to make sure that the radio is on before dialing. We should be able to
     * make emergency calls at any time after the radio has been powered on and isn't in the
     * UNAVAILABLE state, even if it is reporting the OUT_OF_SERVICE state.
     */
    private boolean isOkToCall(int serviceState) {
        // Once we reach either STATE_IN_SERVICE or STATE_EMERGENCY_ONLY, it's finally OK to place
        // the emergency call.
        /// M: Vzw ECC @{
        if (mPhone.useVzwLogic()) {
            return (mPhone.getState() == PhoneConstants.State.OFFHOOK) ||
                (serviceState != ServiceState.STATE_POWER_OFF) ||
                TelephonyManager.from(mContext).isWifiCallingAvailable();
        }
        /// @}

        return ((mPhone.getState() == PhoneConstants.State.OFFHOOK) ||
                /// M: CC: check service state instead of radio state @{
                //mPhone.getServiceStateTracker().isRadioOn() ||
                (serviceState == ServiceState.STATE_IN_SERVICE) ||
                (serviceState == ServiceState.STATE_EMERGENCY_ONLY)) ||
                // Allow STATE_OUT_OF_SERVICE if we are at the max number of retries.
                (mNumRetriesSoFar == MAX_NUM_RETRIES &&
                serviceState == ServiceState.STATE_OUT_OF_SERVICE) ||
                /// @}
                /// M: [ALPS02185470] Only retry once for WFC ECC. @{
                TelephonyManagerEx.getDefault().isWifiCallingEnabled(mPhone.getSubId());
                /// @}
    }

    /**
     * Handles the retry timer expiring.
     */
    private void onRetryTimeout() {
        int serviceState = mPhone.getServiceState().getState();
        Log.d(this, "onRetryTimeout():  phone state = %s, service state = %d, retries = %d.",
                mPhone.getState(), serviceState, mNumRetriesSoFar);

        /// M: For ECC change feature @{
        Log.d(this, "onRetryTimeout(), emergencyOnly:" + mPhone.getServiceState().isEmergencyOnly()
                + ", phonetype:" + mPhone.getPhoneType()
                + ", hasCard:" + mTm.hasIccCard(mPhone.getPhoneId()));
        /// @}

        // - If we're actually in a call, we've succeeded.
        // - Otherwise, if the radio is now on, that means we successfully got out of airplane mode
        //   but somehow didn't get the service state change event.  In that case, try to place the
        //   call.
        // - If the radio is still powered off, try powering it on again.

        if (isOkToCall(serviceState) || mPhone.getServiceState().isEmergencyOnly()) {
            Log.d(this, "onRetryTimeout: Radio is on. Cleaning up.");

            // Woo hoo -- we successfully got out of airplane mode.
            onComplete(true);
            cleanup();
        } else {
            // Uh oh; we've waited the full TIME_BETWEEN_RETRIES_MILLIS and the radio is still not
            // powered-on.  Try again.

            mNumRetriesSoFar++;
            Log.d(this, "mNumRetriesSoFar is now " + mNumRetriesSoFar);

            if (mNumRetriesSoFar > MAX_NUM_RETRIES) {
                Log.w(this, "Hit MAX_NUM_RETRIES; giving up.");
                cleanup();
            } else {
                Log.d(this, "Trying (again) to turn on the radio.");
                /// M: CC: ECC phone selection rule @{
                // Use RadioManager for powering on radio during ECC [ALPS01785370]
                if (RadioManager.isMSimModeSupport()) {
                    //RadioManager will help to turn on radio even this iccid is off by sim mgr
                    Log.d(this, "isMSimModeSupport true, use RadioManager forceSetRadioPower");
                    RadioManager.getInstance().forceSetRadioPower(true, mPhone.getPhoneId());
                } else {
                    //android's default action
                    Log.d(this, "isMSimModeSupport false, use default setRadioPower");
                    mPhone.setRadioPower(true);
                }
                /// @}
                startRetryTimer();
            }
        }
    }

    /**
     * Clean up when done with the whole sequence: either after successfully turning on the radio,
     * or after bailing out because of too many failures.
     *
     * The exact cleanup steps are:
     * - Notify callback if we still hadn't sent it a response.
     * - Double-check that we're not still registered for any telephony events
     * - Clean up any extraneous handler messages (like retry timeouts) still in the queue
     *
     * Basically this method guarantees that there will be no more activity from the
     * EmergencyCallStateListener until someone kicks off the whole sequence again with another call
     * to {@link #waitForRadioOn}
     *
     * TODO: Do the work for the comment below:
     * Note we don't call this method simply after a successful call to placeCall(), since it's
     * still possible the call will disconnect very quickly with an OUT_OF_SERVICE error.
     */
    // M: CC: change to public method to invoke from EmergencyCallHelper.
    public void cleanup() {
        Log.d(this, "cleanup()");

        // This will send a failure call back if callback has yet to be invoked.  If the callback
        // was already invoked, it's a no-op.
        onComplete(false);

        unregisterForServiceStateChanged();
        cancelRetryTimer();

        // M: CC: Clean up any pending MSG_START_SEQUENCE message.
        mHandler.removeMessages(MSG_START_SEQUENCE);

        // Used for unregisterForServiceStateChanged() so we null it out here instead.
        mPhone = null;
        mNumRetriesSoFar = 0;
    }

    private void startRetryTimer() {
        cancelRetryTimer();
        mHandler.sendEmptyMessageDelayed(MSG_RETRY_TIMEOUT, TIME_BETWEEN_RETRIES_MILLIS);
    }

    private void cancelRetryTimer() {
        mHandler.removeMessages(MSG_RETRY_TIMEOUT);
    }

    private void registerForServiceStateChanged() {
        // Unregister first, just to make sure we never register ourselves twice.  (We need this
        // because Phone.registerForServiceStateChanged() does not prevent multiple registration of
        // the same handler.)
        unregisterForServiceStateChanged();
        mPhone.registerForServiceStateChanged(mHandler, MSG_SERVICE_STATE_CHANGED, null);
    }

    private void unregisterForServiceStateChanged() {
        // This method is safe to call even if we haven't set mPhone yet.
        if (mPhone != null) {
            mPhone.unregisterForServiceStateChanged(mHandler);  // Safe even if unnecessary
        }
        mHandler.removeMessages(MSG_SERVICE_STATE_CHANGED);  // Clean up any pending messages too
    }

    private void onComplete(boolean isRadioReady) {
        if (mCallback != null) {
            Callback tempCallback = mCallback;
            mCallback = null;
            tempCallback.onComplete(this, isRadioReady);
            RadioManager.unregisterForRadioPowerChange(mRadioPowerIf);
            /// M: For ECC change feature @{
            if (!hasC2kOverImsModem()) {
                mContext.getContentResolver().unregisterContentObserver(mAirplaneModeObserver);
            }
            /// @}
        }
    }

    @VisibleForTesting
    public Handler getHandler() {
        return mHandler;
    }

    @VisibleForTesting
    public void setMaxNumRetries(int retries) {
        MAX_NUM_RETRIES = retries;
    }

    @VisibleForTesting
    public void setTimeBetweenRetriesMillis(long timeMs) {
        TIME_BETWEEN_RETRIES_MILLIS = timeMs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !getClass().equals(o.getClass())) return false;

        EmergencyCallStateListener that = (EmergencyCallStateListener) o;

        if (mNumRetriesSoFar != that.mNumRetriesSoFar) {
            return false;
        }
        if (mCallback != null ? !mCallback.equals(that.mCallback) : that.mCallback != null) {
            return false;
        }
        return mPhone != null ? mPhone.equals(that.mPhone) : that.mPhone == null;

    }

    public void setContext(Context context) {
        mContext = context;
    }

    /// M: For ECC change feature @{
    private class AirplaneModeObserver extends ContentObserver {
        public AirplaneModeObserver(Handler handler) {
            super(handler);
            mContext.getContentResolver().registerContentObserver(
                    Settings.Global.getUriFor(Settings.Global.AIRPLANE_MODE_ON), false, this);
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            boolean isAirplaneModeOn = Settings.Global.getInt(
                    mContext.getContentResolver(),
                    Settings.Global.AIRPLANE_MODE_ON, 0) > 0;
            Log.d(this, "onChange, isAirplaneModeOn:" + isAirplaneModeOn);
            if (isAirplaneModeOn) {
                cleanup();
            }
        }
    }
    /// @}
}
