/*
* Copyright (C) 2011-2014 MediaTek Inc.
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

/// M: for VoLTE enhanced conference call. @{
import android.telecom.Conference;
import android.telecom.ConnectionRequest;
/// @}

import android.telephony.PhoneNumberUtils;
/// M: for VoLTE enhanced conference call. @{
import android.telephony.ServiceState;
/// @}
import android.telephony.TelephonyManager;

import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.SubscriptionController;


import com.android.internal.telephony.gsm.GsmMmiCode;
import com.android.internal.telephony.imsphone.ImsPhone;
import com.android.internal.telephony.imsphone.ImsPhoneMmiCode;
import com.android.internal.telephony.uicc.UiccCardApplication;
import com.android.internal.telephony.uicc.UiccController;
import com.android.internal.telephony.GsmCdmaPhone;

import android.os.SystemProperties;

import java.util.List;
import java.util.ArrayList;

// for cell conn manager
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import android.telephony.SubscriptionManager;

import com.android.internal.telephony.IccCardConstants;
import com.android.internal.telephony.TelephonyIntents;
import com.android.phone.PhoneGlobals;
import com.android.phone.PhoneUtils;
import com.mediatek.internal.telephony.CellConnMgr;
import com.mediatek.phone.SimErrorDialog;

/// M: CC: IMS @{
import com.android.internal.telephony.Call;
import com.android.internal.telephony.CallStateException;
import com.android.internal.telephony.Connection;
import com.mediatek.telephony.TelephonyManagerEx;
/// @}

/// M: CC: check data only add for OP09 Plug in
import com.mediatek.phone.ext.ExtensionManager;

/// M: CC: Proprietary CRSS handling
import com.mediatek.services.telephony.SuppMessageManager;

//for [VoLTE_SS] notify user for volte mmi request while data off
import com.mediatek.settings.TelephonyUtils;
import com.android.phone.prize.PrizePhoneBlockNumberController;//PRIZE-add-yuandailin-2016-4-14

/// M: CC: ECC Retry
import com.mediatek.services.telephony.EmergencyRetryHandler;

/// M: ECC special handle @{
import android.telecom.PhoneAccountHandle;

import com.mediatek.services.telephony.EmergencyRuleHandler;
/// @}

/// M: CC: Vzw/CTVolte ECC @{
import android.telephony.RadioAccessFamily;

import com.android.internal.telephony.TelephonyDevController;
/// @}

/// M: CC: Set ECC in progress @{
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import com.mediatek.internal.telephony.ITelephonyEx;
/// @}
/*prize-add-huangpengfei-2016-10-31-start*/
import com.android.internal.telephony.CallerInfo;
import android.net.Uri;
/*prize-add-huangpengfei-2016-10-31-end*/

/**
 * Service for making GSM and CDMA connections.
 */
public class TelephonyConnectionServiceUtil {

    private static final TelephonyConnectionServiceUtil INSTANCE = new TelephonyConnectionServiceUtil();
    private TelephonyConnectionService mService;

    // for cell conn manager
    private int mCurrentDialSubId;
    private int mCurrentDialSlotId;
    private CellConnMgr mCellConnMgr;
    private int mCellConnMgrCurrentRun;
    private int mCellConnMgrTargetRun;
    private int mCellConnMgrState;
    private ArrayList<String> mCellConnMgrStringArray;
    private Context mContext;
    private SimErrorDialog mSimErrorDialog;
    /// M: CC: Proprietary CRSS handling @{
    private SuppMessageManager mSuppMessageManager;
    /// @}
    /// M: CC: ECC Retry @{
    private EmergencyRetryHandler mEccRetryHandler;
    private int mEccPhoneType = PhoneConstants.PHONE_TYPE_NONE;
    private int mEccRetryPhoneId = -1;
    private boolean mHasPerformEccRetry = false;
    /// @}

    /// M: CC: Vzw/CTVolte ECC @{
    private String mEccNumber;

    TelephonyDevController mTelDevController = TelephonyDevController.getInstance();
    private boolean hasC2kOverImsModem() {
        if (mTelDevController != null && mTelDevController.getModem(0) != null &&
                mTelDevController.getModem(0).hasC2kOverImsModem() == true) {
            return true;
        }
        return false;
    }

    private static final int RAF_C2K = RadioAccessFamily.RAF_IS95A | RadioAccessFamily.RAF_IS95B |
        RadioAccessFamily.RAF_1xRTT | RadioAccessFamily.RAF_EVDO_0 | RadioAccessFamily.RAF_EVDO_A |
        RadioAccessFamily.RAF_EVDO_B | RadioAccessFamily.RAF_EHRPD;
    /// @}

    private static final boolean MTK_CT_VOLTE_SUPPORT
            = "1".equals(SystemProperties.get("persist.mtk_ct_volte_support", "0"));

    // for cell conn manager
    private final BroadcastReceiver mCellConnMgrReceiver = new TcsBroadcastReceiver();
    /// M: CC: PPL (Phone Privacy Lock Service)
    private final BroadcastReceiver mPplReceiver = new TcsBroadcastReceiver();

    TelephonyConnectionServiceUtil() {
        mService = null;
        mContext = null;
        mSimErrorDialog = null;
        /// M: CC: Proprietary CRSS handling
        mSuppMessageManager = null;
        /// M: CC: ECC Retry
        mEccRetryHandler = null;
    }

    public static TelephonyConnectionServiceUtil getInstance() {
        return INSTANCE;
    }

    public void setService(TelephonyConnectionService s) {
        Log.d(this, "setService: " + s);
        mService = s;
        mContext = mService.getApplicationContext();
        /// M: CC: ECC Retry
        mEccRetryHandler = null;
        enableSuppMessage(s);
        /// M: CC: PPL @{
        IntentFilter intentFilter = new IntentFilter("com.mediatek.ppl.NOTIFY_LOCK");
        mContext.registerReceiver(mPplReceiver, intentFilter);
        /// @}
    }

    /**
     * unset TelephonyConnectionService to be bind.
     */
    public void unsetService() {
        Log.d(this, "unSetService: " + mService);
        mService = null;
        /// M: CC: ECC Retry @{
        mEccRetryHandler = null;
        mEccPhoneType = PhoneConstants.PHONE_TYPE_NONE;
        mEccRetryPhoneId = -1;
        mHasPerformEccRetry = false;
        /// @}
        disableSuppMessage();
        /// M: CC: PPL
        mContext.unregisterReceiver(mPplReceiver);
    }

    /// M: CC: ECC Retry @{
    public void setEccPhoneType(int phoneType) {
        mEccPhoneType = phoneType;
        Log.d(this, "setEccPhoneType, phoneType:" + phoneType);
    }

    public int getEccPhoneType() {
        return mEccPhoneType;
    }

    public void setEccRetryPhoneId(int phoneId) {
        mEccRetryPhoneId = phoneId;
        Log.d(this, "setEccRetryPhoneId, phoneId:" + phoneId);
    }

    public int getEccRetryPhoneId() {
        return mEccRetryPhoneId;
    }

    public boolean hasPerformEccRetry() {
        return mHasPerformEccRetry;
    }

    /**
     * Check if ECC Retry is running.
     * @return {@code true} if ECC Retry is running and {@code false} otherwise.
     */
    public boolean isEccRetryOn() {
        boolean bIsOn = (mEccRetryHandler != null);
        if (bIsOn) {
            Log.d(this, "ECC Retry : isEccRetryOn is true");
        }
        return bIsOn;
    }

    /**
     * Save ECC Retry requested paramsRegister once ECC is created.
     * @param request ConnectionRequest
     * @param initPhoneId PhoneId of the initial ECC
     */
    public void setEccRetryParams(ConnectionRequest request, int initPhoneId) {
        //Check UE is set to test mode or not   (CTA =1,FTA =2 , IOT=3 ...)
        // Skip ECC Retry for TC26.9.6.2.2
        if (SystemProperties.getInt("gsm.gcf.testmode", 0) == 2) {
            Log.d(this, "setEccRetryParams, skip for FTA mode");
            return;
        }

        if (TelephonyManager.getDefault().getPhoneCount() <= 1) {
            if (!MTK_CT_VOLTE_SUPPORT) {
                Log.d(this, "setEccRetryParams, skip for SS project");
                return;
            }
        }

        Log.d(this, "setEccRetryParams, request: " + request + " initPhoneId: " + initPhoneId);
        if (mEccRetryHandler == null) {
            mEccRetryHandler = new EmergencyRetryHandler(request, initPhoneId);
        }
    }

    public void clearEccRetryParams() {
        Log.d(this, "clearEccRetryParams");
        mEccRetryHandler = null;
    }

    /**
     * Set original ECC Call Id
     * @param id CallId
     */
    public void setEccRetryCallId(String id) {
        Log.d(this, "ECC Retry : setEccRetryCallId = " + id);
        if (mEccRetryHandler != null) {
            mEccRetryHandler.setCallId(id);
        }
    }

    /**
     * If ECC Retry timeout
     * @return {@code true} if ECC Retry timeout {@code false} otherwise.
     */
    public boolean eccRetryTimeout() {
        boolean bIsTimeout = false;
        if (mEccRetryHandler != null) {
            if (mEccRetryHandler.isTimeout()) {
                mEccRetryHandler = null;
                bIsTimeout = true;
            }
        }
        Log.d(this, "ECC Retry : eccRetryTimeout = " + bIsTimeout);
        return bIsTimeout;
    }

    /**
     * Perform ECC Retry
     */
    public void performEccRetry() {
        Log.d(this, "performEccRetry");
        if (mEccRetryHandler == null || mService == null) {
            return;
        }
        mHasPerformEccRetry = true;
        ConnectionRequest retryRequest = new ConnectionRequest(
                mEccRetryHandler.getNextAccountHandle(),
                mEccRetryHandler.getRequest().getAddress(),
                mEccRetryHandler.getRequest().getExtras(),
                mEccRetryHandler.getRequest().getVideoState());
        mService.createConnectionInternal(mEccRetryHandler.getCallId(), retryRequest);
    }
    /// @}

    /// M: CC: Proprietary CRSS handling @{
    /**
     * Register for Supplementary Messages once TelephonyConnection is created.
     * @param cs TelephonyConnectionService
     * @param conn TelephonyConnection
     */
    private void enableSuppMessage(TelephonyConnectionService cs) {
        //Log.d(this, "enableSuppMessage for " + cs);
        if (mSuppMessageManager == null) {
            mSuppMessageManager = new SuppMessageManager(cs);
            mSuppMessageManager.registerSuppMessageForPhones();
        }
    }

    /**
     * Unregister for Supplementary Messages  once TelephonyConnectionService is destroyed.
     */
    private void disableSuppMessage() {
        //Log.d(this, "disableSuppMessage");
        if (mSuppMessageManager != null) {
            mSuppMessageManager.unregisterSuppMessageForPhones();
            mSuppMessageManager = null;
        }
    }

    /**
     * Force Supplementary Message update once TelephonyConnection is created.
     * @param conn The connection to update supplementary messages.
     */
    public void forceSuppMessageUpdate(TelephonyConnection conn) {
        if (mSuppMessageManager != null) {
            Phone p = conn.getPhone();
            if (p != null) {
                Log.d(this, "forceSuppMessageUpdate for " + conn + ", " + p
                        + " phone " + p.getPhoneId());
                mSuppMessageManager.forceSuppMessageUpdate(conn, p);
            }
        }
    }

    /// @}

    public boolean isECCExists() {
        if (mService == null) {
            // it means that never a call exist
            // so still not register in telephonyConnectionService
            // ECC doesn't exist
            return false;
        }

        if (mService.getFgConnection() == null) {
            return false;
        }
        if (mService.getFgConnection().getCall() == null ||
            mService.getFgConnection().getCall().getEarliestConnection() == null ||
            mService.getFgConnection().getCall().getPhone() == null) {
            return false;
        }

        String activeCallAddress = mService.getFgConnection().getCall().
                getEarliestConnection().getAddress();

        boolean bECCExists;

        bECCExists = (PhoneNumberUtils.isEmergencyNumber(activeCallAddress)
                     && !PhoneNumberUtils.isSpecialEmergencyNumber(
                            mService.getFgConnection().getCall().getPhone().getSubId(),
                            activeCallAddress));

        if (bECCExists) {
            Log.d(this, "ECC call exists.");
        }
        else {
            Log.d(this, "ECC call doesn't exists.");
        }

        return bECCExists;
    }

    /// M: CC: Error message due to CellConnMgr checking @{
    /**
     * register broadcast Receiver.
     */
    private void cellConnMgrRegisterForSubEvent() {
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        intentFilter.addAction(TelephonyIntents.ACTION_SIM_STATE_CHANGED);
        mContext.registerReceiver(mCellConnMgrReceiver, intentFilter);
    }

    /**
     * unregister broadcast Receiver.
     */
    private void cellConnMgrUnregisterForSubEvent() {
        mContext.unregisterReceiver(mCellConnMgrReceiver);
    }

   /**
     For SIM unplugged, PhoneAccountHandle is null, hence TelephonyConnectionService returns OUTGOING_FAILURE,
     without CellConnMgr checking, UI will show "Call not Sent" Google default dialog.
     For SIM plugged, under
     (1) Flight mode on, MTK SimErrorDialog will show FLIGHT MODE string returned by CellConnMgr.
          Only turning off flight mode via notification bar can dismiss the dialog.
     (2) SIM off, MTK SimErrorDialog will show SIM OFF string returned by CellConnMgr.
          Turning on flight mode, or unplugging SIM can dismiss the dialog.
     (3) SIM locked, MTK SimErrorDialog will show SIM LOCKED string returned by CellConnMgr.
          Turning on flight mode, or unplugging SIM can dismiss the dialog.
     */

    /**
     * Listen to intent of Airplane mode and Sim mode.
     * In case of Airplane mode off or Sim Hot Swap, dismiss SimErrorDialog
     */
    private class TcsBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (isInitialStickyBroadcast()) {
                Log.d(this, "Skip initial sticky broadcast");
                return;
            }
            String action = intent.getAction();
            switch (action) {
                /// M: CC: PPL @{
                case "com.mediatek.ppl.NOTIFY_LOCK":
                    Log.d(this, "Receives com.mediatek.ppl.NOTIFY_LOCK");
                    for (android.telecom.Connection conn : mService.getAllConnections()) {
                        if (conn instanceof TelephonyConnection) {
                            conn.onHangupAll();
                            break;
                        }
                    }
                    break;
                /// @}
                case Intent.ACTION_AIRPLANE_MODE_CHANGED:
                    Log.d(this, "SimErrorDialog finish due to ACTION_AIRPLANE_MODE_CHANGED");
                    mSimErrorDialog.dismiss();
                    break;
                case TelephonyIntents.ACTION_SIM_STATE_CHANGED:
                    String simState = intent.getStringExtra(IccCardConstants.INTENT_KEY_ICC_STATE);
                    int slotId = intent.getIntExtra(PhoneConstants.SLOT_KEY,
                            SubscriptionManager.INVALID_SIM_SLOT_INDEX);
                    Log.d(this, "slotId: " + slotId + " simState: " + simState);
                    if ((slotId != SubscriptionManager.INVALID_SIM_SLOT_INDEX) &&
                            (slotId == mCurrentDialSlotId) &&
                            (simState.equals(IccCardConstants.INTENT_VALUE_ICC_ABSENT))) {
                        Log.d(this, "SimErrorDialog finish due hot plug out of SIM " +
                                (slotId + 1));
                        mSimErrorDialog.dismiss();
                    }
                    break;
                default:
                    break;
            }
        }
    };

    public void cellConnMgrSetSimErrorDialogActivity(SimErrorDialog dialog) {
        if (mContext == null) {
            Log.d(this, "cellConnMgrSetSimErrorDialogActivity, mContext is null");
            return;
        }

        if (mSimErrorDialog == dialog) {
            Log.d(this, "cellConnMgrSetSimErrorDialogActivity, skip duplicate");
            return;
        }

        mSimErrorDialog = dialog;
        if (mSimErrorDialog != null) {
            cellConnMgrRegisterForSubEvent();
            Log.d(this, "cellConnMgrRegisterForSubEvent for setSimErrorDialogActivity");
        } else {
            cellConnMgrUnregisterForSubEvent();
            Log.d(this, "cellConnMgrUnregisterForSubEvent for setSimErrorDialogActivity");
        }
    }

    public boolean cellConnMgrShowAlerting(int subId) {
        if (mContext == null) {
            Log.d(this, "cellConnMgrShowAlerting, mContext is null");
            return false;
        }

        /// M: To check if WiFi Calling enabled. @{
        /// If WiFi calling is enabled, return directly.
        if (TelephonyManagerEx.getDefault().isWifiCallingEnabled(subId)) {
            Log.d(this, "WiFi calling is enabled, return directly.");
            return false;
        }
        /// @}

        mCellConnMgr = new CellConnMgr(mContext);
        mCurrentDialSubId = subId;
        mCurrentDialSlotId = SubscriptionController.getInstance().getSlotId(subId);

        //Step1. Query state by indicated request type, the return value are the combination of current states
        mCellConnMgrState = mCellConnMgr.getCurrentState(mCurrentDialSubId, CellConnMgr.STATE_FLIGHT_MODE |
            CellConnMgr.STATE_RADIO_OFF | CellConnMgr.STATE_SIM_LOCKED | CellConnMgr.STATE_ROAMING |
            CellConnMgr.STATE_NOIMSREG_FOR_CTVOLTE);

        // check if need to notify user to do something
        // Since UX might change, check the size of mCellConnMgrStringArray to show dialog.
        if (mCellConnMgrState != CellConnMgr.STATE_READY) {

            //Step2. Query string used to show dialog
            mCellConnMgrStringArray = mCellConnMgr.getStringUsingState(mCurrentDialSubId, mCellConnMgrState);
            mCellConnMgrCurrentRun = 0;
            mCellConnMgrTargetRun = mCellConnMgrStringArray.size() / 4;

            Log.d(this, "cellConnMgrShowAlerting, slotId: " + mCurrentDialSlotId +
                " state: " + mCellConnMgrState + " size: " + mCellConnMgrStringArray.size());

            /// M: For op09 change "SIM" to "UIM" @{
            mCellConnMgrStringArray = ExtensionManager.getTelephonyConnectionServiceExt().
                customizeSimDisplayString(mCellConnMgrStringArray, mCurrentDialSlotId);
            /// @}

            if (mCellConnMgrTargetRun > 0) {
                cellConnMgrShowAlertingInternal();
                return true;
            }
        }
        return false;
    }

    public void cellConnMgrHandleEvent() {

        //Handle the request if user click on positive button
        mCellConnMgr.handleRequest(mCurrentDialSubId, mCellConnMgrState);

        mCellConnMgrCurrentRun++;

        if (mCellConnMgrCurrentRun != mCellConnMgrTargetRun) {
            cellConnMgrShowAlertingInternal();
        } else {
            cellConnMgrShowAlertingFinalize();
        }
    }

    private void cellConnMgrShowAlertingInternal() {

        //Show confirm dialog with returned dialog title, description, negative button and positive button

        ArrayList<String> stringArray = new ArrayList<String>();
        stringArray.add(mCellConnMgrStringArray.get(mCellConnMgrCurrentRun * 4));
        stringArray.add(mCellConnMgrStringArray.get(mCellConnMgrCurrentRun * 4 + 1));
        stringArray.add(mCellConnMgrStringArray.get(mCellConnMgrCurrentRun * 4 + 2));
        stringArray.add(mCellConnMgrStringArray.get(mCellConnMgrCurrentRun * 4 + 3));

        for (int i = 0; i < stringArray.size(); i++) {
            Log.d(this, "cellConnMgrShowAlertingInternal, string(" + i + ")=" + stringArray.get(i));
        }

        // call dialog ...
        Log.d(this, "cellConnMgrShowAlertingInternal");
//        final Intent intent = new Intent(mContext, SimErrorDialogActivity.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
//        intent.putStringArrayListExtra(SimErrorDialogActivity.DIALOG_INFORMATION, stringArray);
//        mContext.startActivity(intent);
        if (stringArray.size() < 4) {
            Log.d(this, "cellConnMgrShowAlertingInternal, stringArray is illegle, do nothing.");
            return;
        }
        if (mSimErrorDialog != null) {
            Log.w(this, "cellConnMgrShowAlertingInternal, There's an existing error dialog: "
                    + mSimErrorDialog + ", ignore displaying the new error.");
            return;
        }
        mSimErrorDialog = new SimErrorDialog(mContext, stringArray);
        Log.d(this, "cellConnMgrShowAlertingInternal, show SimErrorDialog: " + mSimErrorDialog);
        mSimErrorDialog.show();
    }

    public void cellConnMgrShowAlertingFinalize() {
        Log.d(this, "cellConnMgrShowAlertingFinalize");
        mCellConnMgrCurrentRun = -1;
        mCellConnMgrTargetRun = 0;
        mCurrentDialSubId = SubscriptionManager.INVALID_SUBSCRIPTION_ID;
        mCurrentDialSlotId = SubscriptionManager.INVALID_SIM_SLOT_INDEX;
        mCellConnMgrState = -1;
        mCellConnMgr = null;
    }

    public boolean isCellConnMgrAlive() {
        return (mCellConnMgr != null);
    }
    /// @}

    /// M: CC: Error message due to VoLTE SS checking @{
    //--------------[VoLTE_SS] notify user when volte mmi request while data off-------------
    /**
     * This function used to judge whether the dialed mmi needs to be blocked (which needs XCAP)
     * Disallow SS setting/query.
     * @param phone The phone to dial
     * @param number The number to dial
     * @return {@code true} if the number has MMI format to be blocked and {@code false} otherwise.
     */
    private boolean isBlockedMmi(Phone phone, String dialString) {
        boolean isBlockedMmi = false;

        if (PhoneNumberUtils.isUriNumber(dialString)) {
            return false;
        }
        String dialPart = PhoneNumberUtils.extractNetworkPortionAlt(PhoneNumberUtils.
                stripSeparators(dialString));

        ImsPhone imsPhone = (ImsPhone)phone.getImsPhone();
        boolean imsUseEnabled = phone.isImsUseEnabled()
                 && imsPhone != null
                 && imsPhone.isVolteEnabled()
                 && imsPhone.isUtEnabled()
                 && (imsPhone.getServiceState().getState() == ServiceState.STATE_IN_SERVICE);

        if (imsUseEnabled == true) {
            isBlockedMmi = ImsPhoneMmiCode.isUtMmiCode(
                    dialPart, imsPhone);
        } else if (phone.getPhoneType() == PhoneConstants.PHONE_TYPE_GSM) {
            int slot = SubscriptionController.getInstance().getSlotId(phone.getSubId());
            UiccCardApplication cardApp = UiccController.getInstance().
                    getUiccCardApplication(slot, UiccController.APP_FAM_3GPP);
            isBlockedMmi = GsmMmiCode.isUtMmiCode(
                    dialPart, (GsmCdmaPhone) phone, cardApp);
        }
        Log.d(this, "isBlockedMmi = " + isBlockedMmi + ", imsUseEnabled = " + imsUseEnabled);
        return isBlockedMmi;
    }

    /**
     * This function used to check whether we should notify user to open data connection.
     * For now, we judge certain mmi code + "IMS-phoneAccount" + data connection is off.
     * @param number The number to dial
     * @param phone The target phone
     * @return {@code true} if the notification should pop up and {@code false} otherwise.
     */
    public boolean shouldOpenDataConnection(String number,  Phone phone) {
        return (isBlockedMmi(phone, number) &&
                TelephonyUtils.shouldShowOpenMobileDataDialog(mContext, phone.getSubId()));
    }
    /// @}

    /// M: for VoLTE Conference. @{
    boolean isVoLTEConferenceFull(ImsConferenceController imsConfController) {
        if (imsConfController == null) {
            return false;
        }

        // we assume there is only one ImsPhone at the same time.
        // and we dont support two conference at the same time.
        ArrayList<ImsConference> curImsConferences =
            imsConfController.getCurrentConferences();
        if (curImsConferences.size() == 0
                || curImsConferences.get(0).getNumbOfParticipants() < 5) {
            return false;
        } else {
            return true;
        }
    }

    boolean canHoldImsConference(ImsConference conference) {
        if (conference == null) {
            return false;
        }

        Phone phone = conference.getPhone();
        if (phone == null) {
            return false;
        }

        int state = conference.getState();

        if ((state == android.telecom.Connection.STATE_ACTIVE)
                && (phone.getBackgroundCall().isIdle())) {
            Log.d(this, "canHold conference=" + conference);
            return true;
        } else {
            Log.d(this, "canHold"
                    + " state=" + state
                    + " BgCall is Idle = " + phone.getBackgroundCall().isIdle());
            return false;
        }
    }

    boolean canUnHoldImsConference(ImsConference conference) {
        if (conference == null) {
            return false;
        }

        Phone phone = conference.getPhone();
        if (phone == null) {
            return false;
        }

        int state = conference.getState();

        if ((state == android.telecom.Connection.STATE_HOLDING)
                && (phone.getForegroundCall().isIdle())) {
            Log.d(this, "canUnHold conference=" + conference);
            return true;
        } else {
            Log.d(this, "canUnHold"
                + " state=" + state
                + " FgCall is Idle = " + phone.getForegroundCall().isIdle());
            return false;
        }
    }
    /// @}

    /// M: For VoLTE enhanced conference call. @{
    /**
     * Create a conference connection given an incoming request. This is used to attach to existing
     * incoming calls.
     *
     * @param request Details about the incoming call.
     * @return The {@code GsmConnection} object to satisfy this call, or {@code null} to
     *         not handle the call.
     */
    private android.telecom.Connection createIncomingConferenceHostConnection(
            Phone phone, ConnectionRequest request) {
        Log.i(this, "createIncomingConferenceHostConnection, request: " + request);

        if (mService == null || phone == null) {
            return android.telecom.Connection.createFailedConnection(
                    DisconnectCauseUtil.toTelecomDisconnectCause(
                            android.telephony.DisconnectCause.ERROR_UNSPECIFIED));
        }

        Call call = phone.getRingingCall();
        if (!call.getState().isRinging()) {
            Log.i(this, "onCreateIncomingConferenceHostConnection, no ringing call");
            return android.telecom.Connection.createFailedConnection(
                    DisconnectCauseUtil.toTelecomDisconnectCause(
                            android.telephony.DisconnectCause.INCOMING_MISSED,
                            "Found no ringing call"));
        }

        com.android.internal.telephony.Connection originalConnection =
                call.getState() == Call.State.WAITING ?
                    call.getLatestConnection() : call.getEarliestConnection();

        for (android.telecom.Connection connection : mService.getAllConnections()) {
            if (connection instanceof TelephonyConnection) {
                TelephonyConnection telephonyConnection = (TelephonyConnection) connection;
                if (telephonyConnection.getOriginalConnection() == originalConnection) {
                    Log.i(this, "original connection already registered");
                    return android.telecom.Connection.createCanceledConnection();
                }
            }
        }

        GsmCdmaConnection connection = new GsmCdmaConnection(PhoneConstants.PHONE_TYPE_GSM,
                originalConnection, null, null, false, false);
        return connection;
    }

    /**
     * Create a conference connection given an outgoing request. This is used to initiate new
     * outgoing calls.
     *
     * @param request Details about the outgoing call.
     * @return The {@code GsmConnection} object to satisfy this call, or the result of an invocation
     *         of {@link Connection#createFailedConnection(DisconnectCause)} to not handle the call.
     */
    private android.telecom.Connection createOutgoingConferenceHostConnection(
            Phone phone, final ConnectionRequest request, List<String> numbers) {
        Log.i(this, "createOutgoingConferenceHostConnection, request: " + request);

        if (phone == null) {
            Log.d(this, "createOutgoingConferenceHostConnection, phone is null");
            return android.telecom.Connection.createFailedConnection(
                    DisconnectCauseUtil.toTelecomDisconnectCause(
                            android.telephony.DisconnectCause.OUTGOING_FAILURE, "Phone is null"));
        }

        if (TelephonyConnectionServiceUtil.getInstance().
                cellConnMgrShowAlerting(phone.getSubId())) {
            Log.d(this,
                "createOutgoingConferenceHostConnection, cellConnMgrShowAlerting() check fail");
            return android.telecom.Connection.createFailedConnection(
                    DisconnectCauseUtil.toTelecomDisconnectCause(
                            android.telephony.DisconnectCause.OUTGOING_CANCELED_BY_SERVICE,
                            "cellConnMgrShowAlerting() check fail"));
        }

        int state = phone.getServiceState().getState();
        switch (state) {
            case ServiceState.STATE_IN_SERVICE:
            case ServiceState.STATE_EMERGENCY_ONLY:
                break;
            case ServiceState.STATE_OUT_OF_SERVICE:
                return android.telecom.Connection.createFailedConnection(
                        DisconnectCauseUtil.toTelecomDisconnectCause(
                                android.telephony.DisconnectCause.OUT_OF_SERVICE,
                                "ServiceState.STATE_OUT_OF_SERVICE"));
            case ServiceState.STATE_POWER_OFF:
                return android.telecom.Connection.createFailedConnection(
                        DisconnectCauseUtil.toTelecomDisconnectCause(
                                android.telephony.DisconnectCause.POWER_OFF,
                                "ServiceState.STATE_POWER_OFF"));
            default:
                Log.d(this, "onCreateOutgoingConnection, unknown service state: %d", state);
                return android.telecom.Connection.createFailedConnection(
                        DisconnectCauseUtil.toTelecomDisconnectCause(
                                android.telephony.DisconnectCause.OUTGOING_FAILURE,
                                "Unknown service state " + state));
        }

        // Don't call createConnectionFor() because we can't add this connection to
        // GsmConferenceController
        GsmCdmaConnection connection = new GsmCdmaConnection(PhoneConstants.PHONE_TYPE_GSM,
                null, null, null, false, true);
        connection.setInitializing();
        connection.setVideoState(request.getVideoState());

        placeOutgoingConferenceHostConnection(connection, phone, request, numbers);

        return connection;
    }

    private void placeOutgoingConferenceHostConnection(
            TelephonyConnection connection, Phone phone, ConnectionRequest request,
            List<String> numbers) {

        com.android.internal.telephony.Connection originalConnection;
        try {
            originalConnection = phone.dial(numbers, request.getVideoState());
        } catch (CallStateException e) {
            Log.e(this, e, "placeOutgoingConfHostConnection, phone.dial exception: " + e);
            connection.setDisconnected(DisconnectCauseUtil.toTelecomDisconnectCause(
                    android.telephony.DisconnectCause.OUTGOING_FAILURE,
                    e.getMessage()));
            return;
        }

        if (originalConnection == null) {
            int telephonyDisconnectCause = android.telephony.DisconnectCause.OUTGOING_FAILURE;
            Log.d(this, "placeOutgoingConnection, phone.dial returned null");
            connection.setDisconnected(DisconnectCauseUtil.toTelecomDisconnectCause(
                    telephonyDisconnectCause, "Connection is null"));
        } else {
            connection.setOriginalConnection(originalConnection);
        }
    }


    /**
     * This can be used by telecom to either create a new outgoing conference call or attach
     * to an existing incoming conference call.
     */
    Conference createConference(
            ImsConferenceController imsConfController,
            Phone phone,
            final ConnectionRequest request,
            final List<String> numbers,
            boolean isIncoming) {
        if (imsConfController == null) {
            return null;
        }

        android.telecom.Connection connection = isIncoming ?
            createIncomingConferenceHostConnection(phone, request)
                : createOutgoingConferenceHostConnection(phone, request, numbers);
        Log.d(this, "onCreateConference, connection: %s", connection);

        if (connection == null) {
            Log.d(this, "onCreateConference, connection: %s");
            return null;
        } else if (connection.getState() ==
                android.telecom.Connection.STATE_DISCONNECTED) {
            Log.d(this, "the host connection is dicsonnected");
            return createFailedConference(connection.getDisconnectCause());
        } else if (!(connection instanceof GsmCdmaConnection) ||
                ((GsmCdmaConnection) connection).getPhoneType() != PhoneConstants.PHONE_TYPE_GSM) {
            Log.d(this, "abnormal case, the host connection isn't GsmConnection");
            int telephonyDisconnectCause = android.telephony.DisconnectCause.ERROR_UNSPECIFIED;
            connection.setDisconnected(DisconnectCauseUtil.toTelecomDisconnectCause(
                    telephonyDisconnectCause));
            return createFailedConference(telephonyDisconnectCause, "unexpected error");
        }

        return imsConfController.createConference((TelephonyConnection) connection);
    }

    Conference createFailedConference(int disconnectCause, String reason) {
        return createFailedConference(
            DisconnectCauseUtil.toTelecomDisconnectCause(disconnectCause, reason));
    }

    Conference createFailedConference(android.telecom.DisconnectCause disconnectCause) {
        Conference failedConference = new Conference(null) { };
        failedConference.setDisconnected(disconnectCause);
        return failedConference;
    }
    /// @}


    /**
     * M: check data only add for OP09 Plug in. @{
     * @param phone The target phone
     */
    public boolean isDataOnlyMode(Phone phone) {
        if (ExtensionManager.getTelephonyConnectionServiceExt(
                ).isDataOnlyMode(PhoneGlobals.getInstance().
                    getApplicationContext(), phone)) {
            return true;
        }
        return false;
    }
    /** @} */

    /// M: ALPS02072589 @{
    /**
     * Register Supplementary Messages for ImsPhone.
     * @param phone ImsPhone
     */
    public void registerSuppMessageForImsPhone(Phone phone) {
        if (mSuppMessageManager == null) {
            return;
        }
        mSuppMessageManager.registerSuppMessageForPhone(phone);
    }

    /**
     * Unregister Supplementary Messages for ImsPhone.
     * @param phone ImsPhone
     */
    public void unregisterSuppMessageForImsPhone(Phone phone) {
        if (mSuppMessageManager == null) {
            return;
        }
        mSuppMessageManager.unregisterSuppMessageForPhone(phone);
    }
    /// @}

    /// M: CC: ECC Retry @{
    /// M: Add for GSM+CDMA ecc. @{
    /**
     * Checked if the ecc request need to handle by internal rules.
     * @param accountHandle The target PhoneAccountHandle.
     * @param number The ecc number.
     */
    public Phone selectPhoneBySpecialEccRule(
            PhoneAccountHandle accountHandle,
            String number, Phone defaultEccPhone) {
        EmergencyRuleHandler eccRuleHandler = null;
        if (getEccRetryPhoneId() != -1) {
            eccRuleHandler = new EmergencyRuleHandler(
                    PhoneUtils.makePstnPhoneAccountHandle(Integer.toString(getEccRetryPhoneId())),
                    number, true, defaultEccPhone);
        } else {
            eccRuleHandler = new EmergencyRuleHandler(
                    accountHandle,
                    number, isEccRetryOn(), defaultEccPhone);
        }
        return eccRuleHandler.getPreferredPhone();
    }
    /// @}

    /// M: CC: Vzw/CTVolte ECC @{
    public void setEmergencyNumber(String numberToDial) {
        mEccNumber = numberToDial;
    }

    public void enterEmergencyMode(Phone phone, int isAirplane) {
        if (!hasC2kOverImsModem() && !phone.useVzwLogic()) {
            return;
        }

        // Do not enter Emergency Mode for ISO ECC only.
        // isLocalEmergencyNumber() = true, isEmergencyNumber()=false for ISO ECC only.
        // Since FW checks ECC without ISO, emergency mode setting should be consistent with FW.
        // Do not enter Emergency Mode for CTA ECC (110,119,120,122).
        // CTA ECC: shown as ECC but dialed as normal call
        // with SIM: 93(true) -> ATD,   91-legacy(true) -> ATD
        // w/o SIM: 93(false) -> ATDE,   91-legacy(true) -> ATD -> MD(ATDE)
        if (mEccNumber == null ||
                PhoneNumberUtils.isSpecialEmergencyNumber(phone.getSubId(), mEccNumber) ||
                !PhoneNumberUtils.isEmergencyNumber(phone.getSubId(), mEccNumber)) {
            return;
        }

        // TODO: Need to design for DSDS under airplane mode
        // Only set ECM before Radio on to speed ECC network searching for SS
        // Set ECM after Radio On for DSDS
        // Only for C2K-enabled phone
        if (TelephonyManager.getDefault().getPhoneCount() <= 1) {
            Log.d(this, "Enter Emergency Mode(SS), airplane mode:" + isAirplane);
            phone.setCurrentStatus(isAirplane,
                    phone.isImsRegistered() ? 1 : 0,
                    null);
        } else {
            int raf = phone.getRadioAccessFamily();
            if ((raf & RAF_C2K) > 0) {
                Log.d(this, "Enter Emergency Mode(DSDS), airplane mode:" + isAirplane);
                phone.setCurrentStatus(isAirplane,
                       phone.isImsRegistered() ? 1 : 0,
                       null);
            }
        }
        mEccNumber = null;
    }
    /// @}

    /// M: CC: Set ECC in progress @{
    public void setInEcc(boolean inProgress) {
        ITelephonyEx telEx = ITelephonyEx.Stub.asInterface(
                ServiceManager.getService(Context.TELEPHONY_SERVICE_EX));
        if (telEx != null) {
            try {
                if (inProgress) {
                    if (!telEx.isEccInProgress()) {
                        telEx.setEccInProgress(true);
                        Intent intent = new Intent("android.intent.action.ECC_IN_PROGRESS");
                        intent.putExtra("in_progress", inProgress);
                        mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
                    }
                } else {
                    if (telEx.isEccInProgress()) {
                        telEx.setEccInProgress(false);
                        Intent intent = new Intent("android.intent.action.ECC_IN_PROGRESS");
                        intent.putExtra("in_progress", inProgress);
                        mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
                    }
                }
            } catch (RemoteException e) {
                Log.e(this, e, "Exception of setEccInProgress");
            }
        }
    }
    /// @}
    	/*PRIZE-black number list-yuandailin-2016-4-14-start*/
      /*prize-change  PhoneProxy phoneProxy to Phone phone -huangpengfei-2016-11-1 */
    public boolean shouldBlockNumber(/*PhoneProxy phoneProxy, */Phone phone, Call call, Connection connection) {
      PrizePhoneBlockNumberController  prizePhoneBlockNumberController = new PrizePhoneBlockNumberController();
          if (prizePhoneBlockNumberController.shouldBlockNumber(
            PhoneGlobals.getInstance().getApplicationContext(), connection)) {
            if (call != null) {
                hangupRingingCall(call);
                
                    prizePhoneBlockNumberController.addRejectCallLog(
                    getCallerInfoFromConnection(connection),               
                    PhoneUtils.makePstnPhoneAccountHandle(phone));
               
            }	
            Log.d(this, "shouldBlockNumber true call: " + call + " connection: " + connection);
            return true;
        }
        Log.d(this, "shouldBlockNumber false call: " + call + " connection: " + connection);
        return false;
    }
    
    /**
     *  to hang up the current call if it is incoming call.
     * 
     * @param call current call
     */
    private void hangupRingingCall(Call call) {
        Log.d(this, "hangup ringing call");
        Call.State state = call.getState();
        if (state == Call.State.INCOMING) {
            try {
                call.hangup();
                Log.d(this, "hangupRingingCall(): regular incoming call: hangup()");
            } catch (CallStateException ex) {
                Log.d(this, "Call hangup: caught " + ex);
            }
        } else {
            Log.d(this, "hangupRingingCall: no INCOMING or WAITING call");
        }
    }

    /**
     *  Get the caller info
     *
     * @param conn The phone connection.
     * @return The CallerInfo associated with the connection. Maybe null.
     */
    private CallerInfo getCallerInfoFromConnection(Connection conn) {
        CallerInfo ci = null;
        Object o = conn.getUserData();

        if ((o == null) || (o instanceof CallerInfo)) {
            ci = (CallerInfo) o;
        } else if (o instanceof Uri) {
            ci = CallerInfo.getCallerInfo(PhoneGlobals.getInstance().
                getApplicationContext(), (Uri) o);
        } else {
            ci = ((PhoneUtils.CallerInfoToken) o).currentInfo;
        }
        return ci;
    }
	/*PRIZE-black number list-yuandailin-2016-4-14-end*/

}
