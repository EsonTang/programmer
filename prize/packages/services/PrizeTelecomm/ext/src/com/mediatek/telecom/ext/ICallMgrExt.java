package com.mediatek.telecom.ext;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.UserHandle;
import android.net.Uri;
import android.telecom.PhoneAccountHandle;

import android.telecom.PhoneAccountHandle;

public interface ICallMgrExt {

    /**
     * should build call capabilities.
     *
     * @param smsCapability can response via sms.
     *
     * @return capalilities of the call.
     *
     * @internal
     */
    int buildCallCapabilities(boolean smsCapability);

    /**
     * get call features
     *
     * @param PhoneAccountHandle callPhoneAccountHandle
     *
     * @return int call features.
     */
    int getCallFeatures(PhoneAccountHandle callPhoneAccountHandle,
              int callFeatures);

    /**
     * Should disconnect call calls when dial an ECC out.
     * Only for CMCC Volte PCT test
     *
     * @return whether to disconnect or not
     *
     * @internal
     */
     boolean shouldDisconnectCallsWhenEcc();

    /**
     * should prevent outgoing video call if battery low
     *
     * @param context
     *
     * @param intent
     *
     * @return true/false
     */

    boolean shouldPreventVideoCallIfLowBattery(Context context, Intent intent);
    /**
     * should popup when emergency call tried on roaming
     *
     * @param handle uri for getting accounthandle.
     *
     * @param phoneAccountHandle phoneaccounthandle.
     *
     * @param extras Bundle.
     *
     * @return status of dialog shown or not
     */

    boolean blockOutgoingCall(Uri handle, PhoneAccountHandle phoneAccountHandle, Bundle extras);

    /**
     * should resume hold call.
     *
     * @return whether resume hold call or not.
     *
     */
    boolean shouldResumeHoldCall();
}