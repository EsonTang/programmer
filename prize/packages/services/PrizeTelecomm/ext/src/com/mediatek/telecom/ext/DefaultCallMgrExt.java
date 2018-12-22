package com.mediatek.telecom.ext;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.UserHandle;
import android.net.Uri;
import android.util.Log;

import android.telecom.PhoneAccountHandle;

public class DefaultCallMgrExt implements ICallMgrExt {

    /**
     * should build call capabilities.
     *
     * @param smsCapability can response via sms.
     *
     * @return capalilities of the call.
     */
    @Override
    public int buildCallCapabilities(boolean smsCapability) {
        return 0;
    }

   /**
     * get call features
     *
     * @param PhoneAccountHandle callPhoneAccountHandle
     *
     * @return int call features.
     */
    @Override
    public int getCallFeatures(PhoneAccountHandle callPhoneAccountHandle,
                     int callFeatures) {
        return callFeatures;
    }

    /**
     * Should disconnect call calls when dial an ECC out.
     * Only for CMCC Volte PCT test
     *
     * @return whether to disconnect or not
     *
     */
    @Override
    public boolean shouldDisconnectCallsWhenEcc() {
        return true;
    }

    /**
     * should prevent video call based on battery status
     *
     * @param intent can response via sms.
     *
     * @return boolean true/false
     */
    @Override
    public boolean shouldPreventVideoCallIfLowBattery(Context context, Intent intent) {
        return false;
    }
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
    @Override
    public boolean blockOutgoingCall(Uri handle, PhoneAccountHandle phoneAccountHandle,
            Bundle extras){
        Log.d("DefaultCallMgrExt", "blockOutgoingCall" );
        return false;
    }

     /**
     * should resume hold call.
     *
     * @return whether resume hold call or not.
     *
     */
    @Override
    public boolean shouldResumeHoldCall() {
        //default do not resume hold calls when disconnect remote
        return false;
    }
}