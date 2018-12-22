/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein
 * is confidential and proprietary to MediaTek Inc. and/or its licensors.
 * Without the prior written permission of MediaTek inc. and/or its licensors,
 * any reproduction, modification, use or disclosure of MediaTek Software,
 * and information contained herein, in whole or in part, shall be strictly prohibited.
 */
/* MediaTek Inc. (C) 2010. All rights reserved.
 *
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER ON
 * AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL WARRANTIES,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NONINFRINGEMENT.
 * NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH RESPECT TO THE
 * SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY, INCORPORATED IN, OR
 * SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES TO LOOK ONLY TO SUCH
 * THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO. RECEIVER EXPRESSLY ACKNOWLEDGES
 * THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES
 * CONTAINED IN MEDIATEK SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK
 * SOFTWARE RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S ENTIRE AND
 * CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE RELEASED HEREUNDER WILL BE,
 * AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE MEDIATEK SOFTWARE AT ISSUE,
 * OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE CHARGE PAID BY RECEIVER TO
 * MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek Software")
 * have been modified by MediaTek Inc. All revisions are subject to any receiver's
 * applicable license agreements with MediaTek Inc.
 */

/*
 * Copyright (C) 2006 The Android Open Source Project
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

package com.mediatek.phone;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

import android.net.LinkProperties;

import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.os.AsyncResult;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.telecom.PhoneAccount;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;
import android.telephony.CellInfo;
import android.telephony.CellLocation;
import android.telephony.PhoneNumberUtils.EccEntry;
import android.telephony.NeighboringCellInfo;
import android.telephony.RadioAccessFamily;
import android.telephony.ServiceState;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.telephony.cdma.CdmaCellLocation;
import android.text.TextUtils;
import android.util.Log;


import com.android.phone.PhoneGlobals;

import com.android.internal.telecom.ITelecomService;
import com.android.internal.telephony.CommandException;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.IccCard;
import com.android.internal.telephony.IccCardConstants;
import com.android.internal.telephony.MccTable;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.GsmCdmaPhone;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.PhoneFactory;
import com.android.internal.telephony.ProxyController;
import com.android.internal.telephony.RILConstants;
import com.android.internal.telephony.SubscriptionController;
import com.android.internal.telephony.TelephonyIntents;
import com.android.internal.telephony.TelephonyProperties;
import com.android.internal.telephony.uicc.IccIoResult;
import com.android.internal.telephony.uicc.IccRecords;
import com.android.internal.telephony.uicc.IccUtils;
import com.android.internal.telephony.uicc.UiccCard;
import com.android.internal.telephony.uicc.UiccController;

import com.android.ims.ImsManager;

import java.util.List;

import com.mediatek.internal.telephony.ITelephonyEx;
import com.mediatek.internal.telephony.ISetDefaultSubResultCallback;
import com.mediatek.internal.telephony.MmsConfigInfo;
import com.mediatek.internal.telephony.MmsIcpInfo;

import com.mediatek.internal.telephony.RadioCapabilitySwitchUtil;
import com.android.internal.telephony.dataconnection.DcTracker;

import com.android.internal.telephony.dataconnection.DcFailCause;

import com.mediatek.internal.telephony.uicc.CsimPhbStorageInfo;
import com.mediatek.internal.telephony.uicc.IccFileAdapter;

import com.mediatek.internal.telephony.BtSimapOperResponse;
import com.mediatek.internal.telephony.PseudoBSRecord;
import java.util.ArrayList;
import java.util.Iterator;

import com.mediatek.telephony.ExternalSimManager;
import com.mediatek.telephony.TelephonyManagerEx;

//import com.android.internal.telephony.cdma.CDMAPhone;


/**
 * Implementation of the ITelephony interface.
 */
public class PhoneInterfaceManagerEx extends ITelephonyEx.Stub {

    private static final String LOG_TAG = "PhoneInterfaceManagerEx";
    private static final boolean DBG = true;
    private static final boolean DBG_LOC = false;

    /** The singleton instance. */
    private static PhoneInterfaceManagerEx sInstance;

    PhoneGlobals mApp;
    Phone mPhone;
    private AppOpsManager mAppOps;
    private SubscriptionController mSubscriptionController;

    MainThreadHandler mMainThreadHandler;

    // Query SIM phonebook Adn stroage info thread
    private QueryAdnInfoThread mAdnInfoThread = null;

    // SIM authenthication thread
    private SimAuth mSimAuthThread = null;
    //Icc file adapter
    private static IccFileAdapter[] sIccFileAdapter = null;
    private String[] mOmhOperators = null;

    private static final int CMD_HANDLE_NEIGHBORING_CELL = 2;
    private static final int EVENT_NEIGHBORING_CELL_DONE = 3;
    private static final int EVENT_GET_PSEUDO_BS_RECORDS = 4;
    private static final int EVENT_UNSOL_PSEUDO_BS_INFO_LIST = 5;

    /* SMS Center Address start*/
    private static final int CMD_HANDLE_GET_SCA = 11;
    private static final int CMD_GET_SCA_DONE = 12;
    private static final int CMD_HANDLE_SET_SCA = 13;
    private static final int CMD_SET_SCA_DONE = 14;
    /* SMS Center Address end*/

    // M: [LTE][Low Power][UL traffic shaping] Start
    private static final int CMD_SET_LTE_ACCESS_STRATUM_STATE =35;
    private static final int EVENT_SET_LTE_ACCESS_STRATUM_STATE_DONE =36;
    private static final int CMD_SET_LTE_UPLINK_DATA_TRANSFER_STATE=37;
    private static final int EVENT_SET_LTE_UPLINK_DATA_TRANSFER_STATE_DONE =38;
    // M: [LTE][Low Power][UL traffic shaping] End

    // Provide getMeid(int slotId) for APP. in case, there is no CDMAPhone.
    private static final int EVENT_RADIO_AVAILABLE = 40;
    private static final int EVENT_GET_DEVICE_IDENTITY_DONE = 41;

    // RX Test
    private static final int EVENT_SET_RX_TEST_CONFIG = 42;
    private static final int EVENT_GET_RX_TEST_RESULT = 43;

    private static final String[] PROPERTY_RIL_TEST_SIM = {
        "gsm.sim.ril.testsim",
        "gsm.sim.ril.testsim.2",
        "gsm.sim.ril.testsim.3",
        "gsm.sim.ril.testsim.4",
    };

    private static final String[] ICCRECORD_PROPERTY_ICCID = {
        "ril.iccid.sim1",
        "ril.iccid.sim2",
        "ril.iccid.sim3",
        "ril.iccid.sim4",
    };

   /**
    * The property is used to get supported card type of each SIM card in the slot.
    * @hide
    */
    private static final String[] PROPERTY_RIL_FULL_UICC_TYPE  = {
        "gsm.ril.fulluicctype",
        "gsm.ril.fulluicctype.2",
        "gsm.ril.fulluicctype.3",
        "gsm.ril.fulluicctype.4",
    };

   /**
    * The property shows uim that is only for cdma card.
    * @hide
    */
   private static final String[] PROPERTY_UIM_SUBSCRIBER_ID = {
       "ril.uim.subscriberid.1",
       "ril.uim.subscriberid.2",
       "ril.uim.subscriberid.3",
       "ril.uim.subscriberid.4",
   };

   /**
    * The property is used to check if the card is cdma 3G dual mode card in the slot.
    * @hide
    */
    private static final String[] PROPERTY_RIL_CT3G = {
        "gsm.ril.ct3g",
        "gsm.ril.ct3g.2",
        "gsm.ril.ct3g.3",
        "gsm.ril.ct3g.4",
    };

   /**
    * The property is used to control the log level of TAGs which used for M log.
    * @hide
    */
    private static final String[] PROPERTY_M_LOG_TAG = {
        "persist.log.tag.DCT",
        "persist.log.tag.RIL-DATA",
        "persist.log.tag.C2K_RIL-DATA",
        "persist.log.tag.GsmCdmaPhone",
        "persist.log.tag.SSDecisonMaker",
        "persist.log.tag.GsmMmiCode",
        "persist.log.tag.RpSsController",
        "persist.log.tag.RIL-SS",
        "persist.log.tag.RILMD2-SS",
        "persist.log.tag.DataSubSelector",
        "persist.log.tag.DcFcMgr",
        "persist.log.tag.DC-1",
        "persist.log.tag.DC-2",
        "persist.log.tag.RetryManager",
        "persist.log.tag.IccProvider",
        "persist.log.tag.IccPhoneBookIM",
        "persist.log.tag.AdnRecordCache",
        "persist.log.tag.AdnRecordLoader",
        "persist.log.tag.AdnRecord",
        "persist.log.tag.RIL-PHB",
        "persist.log.tag.RpPhbController",
        "persist.log.tag.RmcPhbReq",
        "persist.log.tag.RmcPhbUrc",
        "persist.log.tag.RtcPhb",
        "persist.log.tag.RIL-SMS",
        "persist.log.tag.DupSmsFilterExt",
        "persist.log.tag.VT",
        "persist.log.tag.ImsVTProvider",
        "persist.log.tag.IccCardProxy",
        "persist.log.tag.IsimFileHandler",
        "persist.log.tag.IsimRecords",
        "persist.log.tag.SIMRecords",
        "persist.log.tag.SpnOverride",
        "persist.log.tag.UiccCard",
        "persist.log.tag.UiccController",
        "persist.log.tag.RIL-SIM",
        "persist.log.tag.MtkAgpsHelper",
        "persist.log.tag.CountryDetector",
        "persist.log.tag.DataDispatcher",
        "persist.log.tag.ImsService",
        "persist.log.tag.IMS_RILA",
        "persist.log.tag.IMSRILRequest",
        "persist.log.tag.ImsManager",
        "persist.log.tag.ImsApp",
        "persist.log.tag.ImsBaseCommands",
        "persist.log.tag.ImsCall",
        "persist.log.tag.ImsPhone",
        "persist.log.tag.ImsPhoneCall",
        "persist.log.tag.ImsPhoneBase",
        "persist.log.tag.ImsCallSession",
        "persist.log.tag.ImsCallProfile",
        "persist.log.tag.ImsEcbm",
        "persist.log.tag.ImsEcbmProxy",
        "persist.log.tag.OperatorUtils",
        "persist.log.tag.WfoApp",
        "persist.log.tag.GbaApp",
        "persist.log.tag.GbaBsfProcedure",
        "persist.log.tag.GbaBsfResponse",
        "persist.log.tag.GbaDebugParam",
        "persist.log.tag.GbaService",
        "persist.log.tag.SresResponse",
        "persist.log.tag.ImsUtService",
        "persist.log.tag.SimservType",
        "persist.log.tag.SimservsTest",
        "persist.log.tag.ImsUt",
        "persist.log.tag.SSDecisonMaker",
        "persist.log.tag.SuppSrvConfig",
        "persist.log.tag.ECCCallHelper",
        "persist.log.tag.GsmConnection",
        "persist.log.tag.TelephonyConf",
        "persist.log.tag.TeleConfCtrler",
        "persist.log.tag.TelephonyConn",
        "persist.log.tag.TeleConnService",
        "persist.log.tag.ECCRetryHandler",
        "persist.log.tag.ECCNumUtils",
        "persist.log.tag.ECCRuleHandler",
        "persist.log.tag.SuppMsgMgr",
        "persist.log.tag.ECCSwitchPhone",
        "persist.log.tag.GsmCdmaConn",
        "persist.log.tag.GsmCdmaPhone",
        "persist.log.tag.Phone",
        "persist.log.tag.RIL-CC",
        "persist.log.tag.RpCallControl",
        "persist.log.tag.RpAudioControl",
        "persist.log.tag.RadioManager",
        "persist.log.tag.RIL_Mux",
        "persist.log.tag.RIL-OEM",
        "persist.log.tag.RIL",
        "persist.log.tag.RIL_UIM_SOCKET",
        "persist.log.tag.RILD",
        "persist.log.tag.RIL-RP",
        "persist.log.tag.RfxMessage",
        "persist.log.tag.RfxDebugInfo",
        "persist.log.tag.RfxTimer",
        "persist.log.tag.RfxObject",
        "persist.log.tag.SlotQueueEntry",
        "persist.log.tag.RfxAction",
        "persist.log.tag.RFX",
        "persist.log.tag.RpRadioMessage",
        "persist.log.tag.RpModemMessage",
        "persist.log.tag.PhoneFactory",
        "persist.log.tag.ProxyController",
        "persist.log.tag.SpnOverride",
        "persist.log.tag.SmsPlusCode",
        "persist.log.tag.AutoRegSmsFwk",
        "persist.log.tag.AirplaneHandler",
        "persist.log.tag.RfxDefDestUtils",
        "persist.log.tag.RfxSM",
        "persist.log.tag.RfxSocketSM",
        "persist.log.tag.RfxDT",
        "persist.log.tag.RpCdmaOemCtrl",
        "persist.log.tag.RpRadioCtrl",
        "persist.log.tag.RpMDCtrl",
        "persist.log.tag.RpCdmaRadioCtrl",
        "persist.log.tag.RpFOUtils",
        "persist.log.tag.C2K_RIL-SIM"
    };

    private static final String[] PROPERTY_V_LOG_TAG = {
        "persist.log.tag.NetworkStats",
        "persist.log.tag.NetworkPolicy"
    };

    private static final String[] PROPERTY_M_LOG_TAG_COMMON_RIL = {
        "persist.log.tag.AT",
        "persist.log.tag.RILMUXD",
        "persist.log.tag.RILC-MTK",
        "persist.log.tag.RILC",
        "persist.log.tag.RfxMainThread",
        "persist.log.tag.RfxRoot",
        "persist.log.tag.RfxRilAdapter",
        "persist.log.tag.RfxController",
        "persist.log.tag.RILC-RP",
        "persist.log.tag.RfxHandlerMgr"
    };

    // MTK-END

    private static final int APC_TIMER_MAX_VALUE = 65535;

    // Key used for storing PseudoBS feature on off, 1 to on and 0 to off
    public static final String APC_ENABLE_KEY = "apc_enable_key";

    // Key used for storing PseudoBS URC report on off, 1 to on and 0 to off
    public static final String APC_REPORT_KEY = "apc_report_key";

    // Key used for storing PseudoBS report timer in seconds, value is in 1 ~ 65535
    public static final String APC_TIMER_KEY = "apc_timer_key";

    private ArrayList<EccEntry> mUserCustomizedEccList = new ArrayList<EccEntry>();

    private ISetDefaultSubResultCallback mCallback = null;

    private boolean mIsEccInProgress = false;

    // Provide getMeid(int slotId) for APP. in case, there is no CDMAPhone.
    private static Phone[] sPhones = null;
    private static CommandsInterface[] sCi = null;
    private String[] mMeids = null;
    private DeviceIdQueryHandler[] mDeviceIdQueryHandler;

    public static final boolean DEBUG = "eng".equals(Build.TYPE);

    /**
     * Initialize the singleton PhoneInterfaceManagerEx instance.
     * This is only done once, at startup, from PhoneGlobals.onCreate().
     */
    /* package */
    public static PhoneInterfaceManagerEx init(PhoneGlobals app, Phone phone) {
        synchronized (PhoneInterfaceManagerEx.class) {
            if (sInstance == null) {
                sInstance = new PhoneInterfaceManagerEx(app, phone);
            } else {
                Log.wtf(LOG_TAG, "init() called multiple times!  sInstance = " + sInstance);
            }
            return sInstance;
        }
    }

    /** Private constructor; @see init() */
    private PhoneInterfaceManagerEx(PhoneGlobals app, Phone phone) {
        mApp = app;
        mPhone = phone;
        mMainThreadHandler = new MainThreadHandler();
        updateUserCustomizedEccList(getUserCustomizedEccList());
        mAppOps = (AppOpsManager)app.getSystemService(Context.APP_OPS_SERVICE);
        mSubscriptionController = SubscriptionController.getInstance();
        publish();
        //OMH related init.
        omhInit();
        IntentFilter intentFilter = new IntentFilter(
                TelephonyIntents.ACTION_SET_RADIO_CAPABILITY_DONE);
        intentFilter.addAction(TelephonyIntents.ACTION_SET_RADIO_CAPABILITY_FAILED);
        mApp.registerReceiver(mReceiver, intentFilter);
        deviceIdHandlerInit();
    }

    private void publish() {
        if (DBG) log("publish: " + this);

        ServiceManager.addService("phoneEx", this);

        if (SystemProperties.getInt("ro.mtk_external_sim_support", 0) == 1) {
            ExternalSimManager.getDefault(mPhone.getContext());
        }
    }

    private static void log(String msg) {
        Log.d(LOG_TAG, "[PhoneIntfMgrEx] " + msg);
    }

    private static void loge(String msg) {
        Log.e(LOG_TAG, "[PhoneIntfMgrEx] " + msg);
    }

    private static void logD(String msg) {
        if (DEBUG) {
            Log.d(LOG_TAG, "[PhoneIntfMgrEx] " + msg);
        }
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            log("Receiver action: " + action);
            if (mCallback != null) {
                try {
                    if (TelephonyIntents.ACTION_SET_RADIO_CAPABILITY_DONE.equals(action)) {
                        mCallback.onComplete(true);
                    } else if (TelephonyIntents.ACTION_SET_RADIO_CAPABILITY_FAILED.equals(action)) {
                        mCallback.onComplete(false);
                    }
                } catch (RemoteException e) {
                    log("onComplete fail, exception:" + e);
                }
                mCallback = null;
            } else {
                log("mCallback is null");
            }
        }
    };

    /**
     * A request object for use with {@link MainThreadHandler}. Requesters should wait() on the
     * request after sending. The main thread will notify the request when it is complete.
     */
    private static final class MainThreadRequest {
        /** The argument to use for the request */
        public Object argument;
        /** The result of the request that is run on the main thread */
        public Object result;
        public Object argument2;

        public MainThreadRequest(Object argument) {
            this.argument = argument;
        }

        public MainThreadRequest(Object argument, Object argument2) {
            this.argument = argument;
            this.argument2 = argument2;
        }
    }

    /**
     * A handler that processes messages on the main thread in the phone process. Since many
     * of the Phone calls are not thread safe this is needed to shuttle the requests from the
     * inbound binder threads to the main thread in the phone process.  The Binder thread
     * may provide a {@link MainThreadRequest} object in the msg.obj field that they are waiting
     * on, which will be notified when the operation completes and will contain the result of the
     * request.
     *
     * <p>If a MainThreadRequest object is provided in the msg.obj field,
     * note that request.result must be set to something non-null for the calling thread to
     * unblock.
     */
    private final class MainThreadHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            MainThreadRequest request;
            Message onCompleted;
            AsyncResult ar;
            int subId;
            int phoneId;

            switch (msg.what) {
                case CMD_HANDLE_NEIGHBORING_CELL:
                    request = (MainThreadRequest) msg.obj;
                    onCompleted = obtainMessage(EVENT_NEIGHBORING_CELL_DONE, request);

                    final Phone phone = (Phone) request.argument;
                    phone.getNeighboringCids(onCompleted);

                    break;

                case EVENT_NEIGHBORING_CELL_DONE:
                    ar = (AsyncResult) msg.obj;
                    request = (MainThreadRequest) ar.userObj;
                    if (ar.exception == null && ar.result != null) {
                        request.result = ar.result;
                    } else {
                        // create an empty list to notify the waiting thread
                        request.result = new ArrayList<NeighboringCellInfo>(0);
                    }
                    // Wake up the requesting thread
                    synchronized (request) {
                        request.notifyAll();
                    }
                    break;
                case CMD_HANDLE_GET_SCA:
                    request = (MainThreadRequest) msg.obj;
                    onCompleted = obtainMessage(CMD_GET_SCA_DONE, request);

                    if (request.argument == null) {
                        // no argument, ignore
                        log("[sca get sc address but no argument");
                    } else {
                        subId = (Integer) request.argument;
                        getPhone(subId).getSmscAddress(onCompleted);
                    }
                    break;

                case CMD_GET_SCA_DONE:
                    ar = (AsyncResult) msg.obj;
                    request = (MainThreadRequest) ar.userObj;

                    Bundle result = new Bundle();
                    if (ar.exception == null && ar.result != null) {
                        log("[sca get result" + ar.result);
                        result.putByte(TelephonyManagerEx.GET_SC_ADDRESS_KEY_RESULT,
                                TelephonyManagerEx.ERROR_CODE_NO_ERROR);
                        result.putCharSequence(TelephonyManagerEx.GET_SC_ADDRESS_KEY_ADDRESS,
                                (String) ar.result);
                    } else {
                        byte error = TelephonyManagerEx.ERROR_CODE_GENERIC_ERROR;
                        if (ar.exception instanceof CommandException) {
                            CommandException ce = (CommandException) ar.exception;
                            if (ce.getCommandError() ==
                                    CommandException.Error.REQUEST_NOT_SUPPORTED) {
                                error = TelephonyManagerEx.ERROR_CODE_NO_SUPPORT_SC_ADDR;
                            }
                        }
                        log("[sca Fail to get sc address, error = " + error);
                        result.putByte(TelephonyManagerEx.GET_SC_ADDRESS_KEY_RESULT, error);
                        result.putCharSequence(TelephonyManagerEx.GET_SC_ADDRESS_KEY_ADDRESS, "");
                    }
                    request.result = result;

                    synchronized (request) {
                        log("[sca notify sleep thread");
                        request.notifyAll();
                    }
                    break;

                case CMD_HANDLE_SET_SCA:
                    request = (MainThreadRequest) msg.obj;
                    onCompleted = obtainMessage(CMD_SET_SCA_DONE, request);

                    ScAddress sca = (ScAddress) request.argument;
                    if (sca.mSubId == SubscriptionManager.INVALID_SUBSCRIPTION_ID) {
                        // invalid subscription ignore
                        log("[sca invalid subscription");
                    } else {
                        getPhone(sca.mSubId).setSmscAddress(sca.mAddress, onCompleted);
                    }
                    break;

                case CMD_SET_SCA_DONE:
                    ar = (AsyncResult) msg.obj;
                    request = (MainThreadRequest) ar.userObj;
                    if (ar.exception != null) {
                        Log.d(LOG_TAG, "[sca Fail: set sc address");
                        request.result = new Boolean(false);
                    } else {
                        Log.d(LOG_TAG, "[sca Done: set sc address");
                        request.result = new Boolean(true);
                    }

                    synchronized (request) {
                        request.notifyAll();
                    }
                    break;

                // M: [LTE][Low Power][UL traffic shaping] Start
                case CMD_SET_LTE_ACCESS_STRATUM_STATE:
                    request = (MainThreadRequest) msg.obj;
                    boolean enabled = ((Boolean) request.argument).booleanValue();
                    phoneId = ((Integer) request.argument2).intValue();
                    if (DBG) {
                        log("CMD_SET_LTE_ACCESS_STRATUM_STATE: enabled " + enabled
                                + "phoneId " + phoneId);
                    }
                    mPhone = PhoneFactory.getPhone(phoneId);
                    if (mPhone == null) {
                        loge("setLteAccessStratumReport: No MainPhone");
                        request.result = new Boolean(false);
                        synchronized (request) {
                            request.notifyAll();
                        }
                    } else {
                        DcTracker dcTracker = mPhone.mDcTracker;
                        onCompleted = obtainMessage(EVENT_SET_LTE_ACCESS_STRATUM_STATE_DONE,
                                request);
                        dcTracker.onSetLteAccessStratumReport((Boolean) enabled, onCompleted);
                    }
                    break;

                case EVENT_SET_LTE_ACCESS_STRATUM_STATE_DONE:
                    if (DBG) log("EVENT_SET_LTE_ACCESS_STRATUM_STATE_DONE");
                    handleNullReturnEvent(msg, "setLteAccessStratumReport");
                    break;

                case CMD_SET_LTE_UPLINK_DATA_TRANSFER_STATE:
                    request = (MainThreadRequest) msg.obj;
                    int state = ((Integer) request.argument).intValue();
                    phoneId = ((Integer) request.argument2).intValue();
                    if (DBG) {
                        log("CMD_SET_LTE_UPLINK_DATA_TRANSFER_STATE: state " + state
                                + "phoneId " + phoneId);
                    }
                    mPhone = PhoneFactory.getPhone(phoneId);
                    if (mPhone == null) {
                        loge("setLteUplinkDataTransfer: No MainPhone");
                        request.result = new Boolean(false);
                        synchronized (request) {
                            request.notifyAll();
                        }
                    } else {
                        DcTracker dcTracker = mPhone.mDcTracker;
                        onCompleted = obtainMessage(EVENT_SET_LTE_UPLINK_DATA_TRANSFER_STATE_DONE,
                                request);
                        dcTracker.onSetLteUplinkDataTransfer((Integer) state, onCompleted);
                    }
                    break;

                case EVENT_SET_LTE_UPLINK_DATA_TRANSFER_STATE_DONE:
                    if (DBG) log("EVENT_SET_LTE_UPLINK_DATA_TRANSFER_STATE_DONE");
                    handleNullReturnEvent(msg, "setLteUplinkDataTransfer");
                    break;
                // M: [LTE][Low Power][UL traffic shaping] End

                case EVENT_GET_PSEUDO_BS_RECORDS:
                    if (DBG) log("handle EVENT_GET_PSEUDO_BS_RECORDS");
                    ar = (AsyncResult) msg.obj;
                    PseudoBsInfoResult bsIfno = (PseudoBsInfoResult) ar.userObj;
                    synchronized(bsIfno.lockObj) {
                        if (ar.exception != null) {
                            log("EVENT_GET_PSEUDO_BS_RECORDS: error ret null, e=" + ar.exception);
                            bsIfno.list = null;
                        } else {
                            int[] msgs = (int[]) ar.result;
                            bsIfno.list = new ArrayList<PseudoBSRecord>();
                            bsIfno.list = parsePseudoRecords(msgs);
                        }
                        bsIfno.lockObj.notify();
                        if (DBG) log("EVENT_GET_PSEUDO_BS_RECORDS notify result");
                    }
                    break;

                case EVENT_SET_RX_TEST_CONFIG:
                case EVENT_GET_RX_TEST_RESULT:
                    if (DBG) log("handle RX_TEST");
                    ar = (AsyncResult) msg.obj;
                    RxTestObject rt = (RxTestObject) ar.userObj;
                    synchronized(rt.lockObj) {
                        if (ar.exception != null) {
                            log("RX_TEST: error ret null, e=" + ar.exception);
                            rt.result = null;
                        } else {
                            rt.result = (int[]) ar.result;
                        }
                        rt.lockObj.notify();
                        if (DBG) log("RX_TEST notify result");
                    }
                    break;
                default:
                    break;
            }
        }

        private void handleNullReturnEvent(Message msg, String command) {
            AsyncResult ar = (AsyncResult) msg.obj;
            MainThreadRequest request = (MainThreadRequest) ar.userObj;
            if (ar.exception == null) {
                request.result = new Boolean(true);
            } else {
                request.result = new Boolean(false);
                if (ar.exception instanceof CommandException) {
                    loge(command + ": CommandException: " + ar.exception);
                } else {
                    loge(command + ": Unknown exception");
                }
            }
            synchronized (request) {
                request.notifyAll();
            }
        }
    }

    /**
     * Posts the specified command to be executed on the main thread,
     * waits for the request to complete, and returns the result.
     * @see sendRequestAsync
     */
    private Object sendRequest(int command, Object argument) {
        if (Looper.myLooper() == mMainThreadHandler.getLooper()) {
            throw new RuntimeException("This method will deadlock if called from the main thread.");
        }

        MainThreadRequest request = new MainThreadRequest(argument);
        Message msg = mMainThreadHandler.obtainMessage(command, request);
        msg.sendToTarget();

        // Wait for the request to complete
        synchronized (request) {
            while (request.result == null) {
                try {
                    request.wait();
                } catch (InterruptedException e) {
                    // Do nothing, go back and wait until the request is complete
                }
            }
        }
        return request.result;
    }

   /**
     * Posts the specified command to be executed on the main thread,
     * waits for the request to complete, and returns the result.
     * @see #sendRequestAsync
     */
    private Object sendRequest(int command, Object argument, Object argument2) {
        if (Looper.myLooper() == mMainThreadHandler.getLooper()) {
            throw new RuntimeException("This method will deadlock if called from the main thread.");
        }

        MainThreadRequest request = new MainThreadRequest(argument, argument2);
        Message msg = mMainThreadHandler.obtainMessage(command, request);
        msg.sendToTarget();

        // Wait for the request to complete
        synchronized (request) {
            while (request.result == null) {
                try {
                    request.wait();
                } catch (InterruptedException e) {
                    // Do nothing, go back and wait until the request is complete
                }
            }
        }
        return request.result;
    }

    private static Phone getPhone(int subId) {
        // FIXME: getPhone by subId
        int phoneId = SubscriptionController.getInstance().getPhoneId(subId);
        return PhoneFactory.getPhone(
                ((phoneId < 0) ? SubscriptionManager.DEFAULT_PHONE_INDEX : phoneId));
    }

    private static Phone getPhoneUsingPhoneId(int phoneId) {
        return PhoneFactory.getPhone(phoneId);
    }

    public int getSubIdBySlot(int slot) {
        int [] subIds = SubscriptionManager.getSubId(slot);
        int subId = ((subIds == null) ? SubscriptionManager.getDefaultSubId() : subIds[0]);
        if (DBG) log("getSubIdBySlot, simId " + slot + "subId " + subId);
        return subId;
    }

    private class UnlockSim extends Thread {

        /* Query network lock start */

        // Verify network lock result.
        public static final int VERIFY_RESULT_PASS = 0;
        public static final int VERIFY_INCORRECT_PASSWORD = 1;
        public static final int VERIFY_RESULT_EXCEPTION = 2;

        // Total network lock count.
        public static final int NETWORK_LOCK_TOTAL_COUNT = 5;
        public static final String QUERY_SIMME_LOCK_RESULT = "com.mediatek.phone.QUERY_SIMME_LOCK_RESULT";
        public static final String SIMME_LOCK_LEFT_COUNT = "com.mediatek.phone.SIMME_LOCK_LEFT_COUNT";

        /* Query network lock end */


        private final IccCard mSimCard;

        private boolean mDone = false;
        private boolean mResult = false;

        // For replies from SimCard interface
        private Handler mHandler;

        private static final int QUERY_NETWORK_STATUS_COMPLETE = 100;
        private static final int SET_NETWORK_LOCK_COMPLETE = 101;

        private int mVerifyResult = -1;
        private int mSIMMELockRetryCount = -1;

        public UnlockSim(IccCard simCard) {
            mSimCard = simCard;
        }

        @Override
        public void run() {
            Looper.prepare();
            synchronized (UnlockSim.this) {
                mHandler = new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        AsyncResult ar = (AsyncResult) msg.obj;
                        switch (msg.what) {
                            case QUERY_NETWORK_STATUS_COMPLETE:
                                synchronized (UnlockSim.this) {
                                    int [] LockState = (int []) ar.result;
                                    if (ar.exception != null) { //Query exception occurs
                                        log("Query network lock fail");
                                        mResult = false;
                                    } else {
                                        mSIMMELockRetryCount = LockState[2];
                                        log("[SIMQUERY] Category = " + LockState[0]
                                            + " ,Network status =" + LockState[1]
                                            + " ,Retry count = " + LockState[2]);
                                        mResult = true;
                                    }
                                    mDone = true;
                                    UnlockSim.this.notifyAll();
                                }
                                break;
                            case SET_NETWORK_LOCK_COMPLETE:
                                log("SUPPLY_NETWORK_LOCK_COMPLETE");
                                synchronized (UnlockSim.this) {
                                    if ((ar.exception != null) &&
                                           (ar.exception instanceof CommandException)) {
                                        log("ar.exception " + ar.exception);
                                        if (((CommandException) ar.exception).getCommandError()
                                            == CommandException.Error.PASSWORD_INCORRECT) {
                                            mVerifyResult = VERIFY_INCORRECT_PASSWORD;
                                       } else {
                                            mVerifyResult = VERIFY_RESULT_EXCEPTION;
                                       }
                                    } else {
                                        mVerifyResult = VERIFY_RESULT_PASS;
                                    }
                                    mDone = true;
                                    UnlockSim.this.notifyAll();
                                }
                                break;
                        }
                    }
                };
                UnlockSim.this.notifyAll();
            }
            Looper.loop();
        }

        synchronized Bundle queryNetworkLock(int category) {

            while (mHandler == null) {
                try {
                    log("wait for mHandler");
                    wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            log("Enter queryNetworkLock");
            Message callback = Message.obtain(mHandler, QUERY_NETWORK_STATUS_COMPLETE);
            mSimCard.queryIccNetworkLock(category, callback);

            while (!mDone) {
                try {
                    log("wait for done");
                    wait();
                } catch (InterruptedException e) {
                    // Restore the interrupted status
                    Thread.currentThread().interrupt();
                }
            }
            try {
                mHandler.getLooper().quit();
                if (mHandler.getLooper().getThread() != null) {
                    mHandler.getLooper().getThread().interrupt();
                }
            } catch (NullPointerException ne) {
                loge("queryNetworkLock Null looper");
                ne.printStackTrace();
            }
            Bundle bundle = new Bundle();
            bundle.putBoolean(QUERY_SIMME_LOCK_RESULT, mResult);
            bundle.putInt(SIMME_LOCK_LEFT_COUNT, mSIMMELockRetryCount);

            log("done");
            return bundle;
        }

        synchronized int supplyNetworkLock(String strPasswd) {

            while (mHandler == null) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            log("Enter supplyNetworkLock");
            Message callback = Message.obtain(mHandler, SET_NETWORK_LOCK_COMPLETE);
            mSimCard.supplyNetworkDepersonalization(strPasswd, callback);

            while (!mDone) {
                try {
                    log("wait for done");
                    wait();
                } catch (InterruptedException e) {
                    // Restore the interrupted status
                    Thread.currentThread().interrupt();
                }
            }
            try {
                mHandler.getLooper().quit();
                if (mHandler.getLooper().getThread() != null) {
                    mHandler.getLooper().getThread().interrupt();
                }
            } catch (NullPointerException ne) {
                loge("supplyNetworkLock Null looper");
                ne.printStackTrace();
            }
            log("done");
            return mVerifyResult;
        }
    }

    public Bundle queryNetworkLock(int subId, int category) {
        final UnlockSim queryNetworkLockState;

        log("queryNetworkLock");

        queryNetworkLockState = new UnlockSim(getPhone(subId).getIccCard());
        queryNetworkLockState.start();

        return queryNetworkLockState.queryNetworkLock(category);
    }

    public int supplyNetworkDepersonalization(int subId, String strPasswd) {
        final UnlockSim supplyNetworkLock;

        log("supplyNetworkDepersonalization");

        supplyNetworkLock = new UnlockSim(getPhone(subId).getIccCard());
        supplyNetworkLock.start();

        return supplyNetworkLock.supplyNetworkLock(strPasswd);
    }

    /**
     * Modem SML change feature.
     * This function will query the SIM state of the given slot. And broadcast
     * ACTION_UNLOCK_SIM_LOCK if the SIM state is in network lock.
     *
     * @param subId: Indicate which sub to query
     * @param needIntent: The caller can deside to broadcast ACTION_UNLOCK_SIM_LOCK or not
     *                    in this time, because some APs will receive this intent (eg. Keyguard).
     *                    That can avoid this intent to effect other AP.
     */
    public void repollIccStateForNetworkLock(int subId, boolean needIntent) {
        if (TelephonyManager.getDefault().getPhoneCount() > 1) {
            getPhone(subId).getIccCard().repollIccStateForModemSmlChangeFeatrue(needIntent);
        } else {
            log("Not Support in Single SIM.");
        }
    }

    private static class SetMsisdn extends Thread {
        private int mSubId;
        private Phone myPhone;
        private boolean mDone = false;
        private int mResult = 0;
        private Handler mHandler;

        private static final String DEFAULT_ALPHATAG = "Default Tag";
        private static final int CMD_SET_MSISDN_COMPLETE = 100;


        public SetMsisdn(Phone myP, int subId) {
            mSubId = subId;
            myPhone = myP;
        }


        @Override
        public void run() {
            Looper.prepare();
            synchronized (SetMsisdn.this) {
                mHandler = new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        AsyncResult ar = (AsyncResult) msg.obj;
                        switch (msg.what) {
                            case CMD_SET_MSISDN_COMPLETE:
                                synchronized (SetMsisdn.this) {
                                    if (ar.exception != null) { //Query exception occurs
                                        Log.e(LOG_TAG, "Set msisdn fail");
                                        mDone = true;
                                        mResult = 0;
                                    } else {
                                        Log.d(LOG_TAG, "Set msisdn success");
                                        mDone = true;
                                        mResult = 1;
                                    }
                                    SetMsisdn.this.notifyAll();
                                }
                                break;
                        }
                    }
                };
                SetMsisdn.this.notifyAll();
            }
            Looper.loop();
        }

        synchronized int setLine1Number(String alphaTag, String number) {

            while (mHandler == null) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            Log.d(LOG_TAG, "Enter setLine1Number");
            Message callback = Message.obtain(mHandler, CMD_SET_MSISDN_COMPLETE);
            String myTag = alphaTag;

            myTag = myPhone.getLine1AlphaTag();

            if (myTag == null || myTag.equals("")) {
                myTag = DEFAULT_ALPHATAG;
            }

            Log.d(LOG_TAG, "sub = " + mSubId + ", Tag = " + myTag + " ,number = " + number);

            myPhone.setLine1Number(myTag, number, callback);


            while (!mDone) {
                try {
                    Log.d(LOG_TAG, "wait for done");
                    wait();
                } catch (InterruptedException e) {
                    // Restore the interrupted status
                    Thread.currentThread().interrupt();
                }
            }

            Log.d(LOG_TAG, "done");
            return mResult;
        }
    }

    //@Override
    public int setLine1Number(int subId, String alphaTag, String number) {
        if (DBG) log("setLine1NumberUsingSubId, subId " + subId);
        if (number == null) {
            loge("number = null");
            return 0;
        }
        if (subId <= 0) {
            loge("Error subId: " + subId);
            return 0;
        }

        final SetMsisdn setMsisdn;

        setMsisdn = new SetMsisdn(getPhone(subId), subId);
        setMsisdn.start();

        return setMsisdn.setLine1Number(alphaTag, number);
    }

    /**
    * Return true if the FDN of the ICC card is enabled
    */
    //@Override
    public boolean isFdnEnabled(int subId) {
        log("isFdnEnabled  subId=" + subId);

        if (subId <= 0) {
            loge("Error subId: " + subId);
            return false;
        }

        /* We will rollback the temporary solution after SubscriptionManager merge to L1 */
        Phone phone = getPhone(subId);
        if (phone != null) {
            return phone.getIccCard().getIccFdnAvailable() && phone.getIccCard().getIccFdnEnabled();
        } else {
            return false;
        }
    }

    //@Override
    public String getIccCardType(int subId) {
        if (DBG) log("getIccCardType  subId=" + subId);

        Phone phone = getPhone(subId);
        if (phone == null) {
            if (DBG) log("getIccCardType(): phone is null");
            return "";
        }

        return phone.getIccCard().getIccCardType();
    }

    //@Override
    public boolean isAppTypeSupported(int slotId, int appType) {
        if (DBG) log("isAppTypeSupported  slotId=" + slotId);

        UiccCard uiccCard = UiccController.getInstance().getUiccCard(slotId);
        if (uiccCard == null) {
            if (DBG) log("isAppTypeSupported(): uiccCard is null");
            return false;
        }

        return ((uiccCard.getApplicationByType(appType) == null) ?  false : true);
    }

    //@Override
    public boolean isTestIccCard(int slotId) {
        String mTestCard = null;

        mTestCard = SystemProperties.get(PROPERTY_RIL_TEST_SIM[slotId], "");
        if (DBG) log("isTestIccCard(): slot id =" + slotId + ", iccType = " + mTestCard);
        return (mTestCard != null && mTestCard.equals("1"));
    }

    /**
     * Gemini
     * Returns the alphabetic name of current registered operator.
     * <p>
     * Availability: Only when user is registered to a network. Result may be
     * unreliable on CDMA networks (use {@link #getPhoneType()} to determine if
     * on a CDMA network).
     */
    @Deprecated
    public String getNetworkOperatorNameGemini(int slotId) {
        int subId = getSubIdBySlot(slotId);
        if (DBG) log("Deprecated! getNetworkOperatorNameGemini simId = " + slotId + " ,sub = " + subId);
        return getNetworkOperatorNameUsingSub(subId);
    }

    public String getNetworkOperatorNameUsingSub(int subId) {
        int phoneId = SubscriptionManager.getPhoneId(subId);
        String prop = TelephonyManager.getTelephonyProperty(phoneId, TelephonyProperties.PROPERTY_OPERATOR_ALPHA, "");
        if (DBG) log("getNetworkOperatorNameUsingSub sub = " + subId + " ,prop = " + prop);
        return prop;
    }

    /**
     * Gemini
     * Returns the numeric name (MCC+MNC) of current registered operator.
     * <p>
     * Availability: Only when user is registered to a network. Result may be
     * unreliable on CDMA networks (use {@link #getPhoneType()} to determine if
     * on a CDMA network).
     */
    @Deprecated
    public String getNetworkOperatorGemini(int slotId) {
        int subId = getSubIdBySlot(slotId);
        if (DBG) log("Deprecated! getNetworkOperatorGemini simId = " + slotId + " ,sub = " + subId);
        return getNetworkOperatorUsingSub(subId);
    }

    public String getNetworkOperatorUsingSub(int subId) {
        int phoneId = SubscriptionManager.getPhoneId(subId);
        String prop = TelephonyManager.getTelephonyProperty(phoneId, TelephonyProperties.PROPERTY_OPERATOR_NUMERIC, "");
        if (DBG) log("getNetworkOperatorUsingSub sub = " + subId + " ,prop = " + prop);
        return prop;
    }

    // MVNO-API START
    public String getMvnoMatchType(int subId) {
        String type = getPhone(subId).getMvnoMatchType();
        if (DBG) log("getMvnoMatchTypeUsingSub sub = " + subId + " ,vMailAlphaTag = " + type);
        return type;
    }

    public String getMvnoPattern(int subId, String type) {
        String pattern = getPhone(subId).getMvnoPattern(type);
        if (DBG) log("getMvnoPatternUsingSub sub = " + subId + " ,vMailAlphaTag = " + pattern);
        return pattern;
    }
    // MVNO-API END

    /**
     * Make sure the caller has the READ_PRIVILEGED_PHONE_STATE permission.
     *
     * @throws SecurityException if the caller does not have the required permission
     */
    private void enforcePrivilegedPhoneStatePermission() {
        mApp.enforceCallingOrSelfPermission(android.Manifest.permission.READ_PRIVILEGED_PHONE_STATE,
                null);
    }

    /**
     * Request to run AKA authenitcation on UICC card by indicated family.
     *
     * @param slotId indicated sim id
     * @param family indiacted family category
     *        UiccController.APP_FAM_3GPP =  1; //SIM/USIM
     *        UiccController.APP_FAM_3GPP2 = 2; //RUIM/CSIM
     *        UiccController.APP_FAM_IMS   = 3; //ISIM
     * @param byteRand random challenge in byte array
     * @param byteAutn authenication token in byte array
     *
     * @return reponse paramenters/data from UICC
     *
     */
    public byte[] simAkaAuthentication(int slotId, int family, byte[] byteRand, byte[] byteAutn) {
        enforcePrivilegedPhoneStatePermission();

        String strRand = "";
        String strAutn = "";
        log("simAkaAuthentication session is " + family + " simId " + slotId);

        if (byteRand != null && byteRand.length > 0) {
            strRand = IccUtils.bytesToHexString(byteRand).substring(0, byteRand.length * 2);
        }

        if (byteAutn != null && byteAutn.length > 0) {
            strAutn = IccUtils.bytesToHexString(byteAutn).substring(0, byteAutn.length * 2);
        }
        log("simAkaAuthentication Randlen " + strRand.length() + " strRand is "
                + strRand + ", AutnLen " + strAutn.length() + " strAutn " + strAutn);
        String akaData = Integer.toHexString(strRand.length()) + strRand +
                Integer.toHexString(strAutn.length()) + strAutn;
        if (DBG) {
            log("akaData: " + akaData);
        }


        int subId = getSubIdBySlot(slotId);
        int appType = PhoneConstants.APPTYPE_UNKNOWN;
        switch (family) {
            case 1:
                appType = PhoneConstants.APPTYPE_USIM;
                break;
            case 2:
                appType = PhoneConstants.APPTYPE_CSIM;
                break;
            case 3:
                appType = PhoneConstants.APPTYPE_ISIM;
                break;
        }
        if (appType == PhoneConstants.APPTYPE_UNKNOWN) {
            return null;
        } else {
            Context context = mPhone.getContext();
            String responseData = TelephonyManager.from(context).getIccAuthentication(
                       subId, appType, TelephonyManager.AUTHTYPE_EAP_SIM, akaData);
            return IccUtils.hexStringToBytes(responseData);
        }
    }

    /**
     * Request to run GBA authenitcation (Bootstrapping Mode)on UICC card
     * by indicated family.
     *
     * @param slotId indicated sim id
     * @param family indiacted family category
     *        UiccController.APP_FAM_3GPP =  1; //SIM/USIM
     *        UiccController.APP_FAM_3GPP2 = 2; //RUIM/CSIM
     *        UiccController.APP_FAM_IMS   = 3; //ISIM
     * @param byteRand random challenge in byte array
     * @param byteAutn authenication token in byte array
     *
     * @return reponse paramenters/data from UICC
     *
     */
    public byte[] simGbaAuthBootStrapMode(int slotId, int family, byte[] byteRand, byte[] byteAutn) {
        enforcePrivilegedPhoneStatePermission();

        if (mSimAuthThread == null) {
            log("simGbaAuthBootStrapMode new thread");
            mSimAuthThread = new SimAuth(mPhone);
            mSimAuthThread.start();
        } else {
            log("simGbaAuthBootStrapMode thread has been created.");
        }

        String strRand = "";
        String strAutn = "";
        log("simGbaAuthBootStrapMode session is " + family + " simId " + slotId);

        if (byteRand != null && byteRand.length > 0) {
            strRand = IccUtils.bytesToHexString(byteRand).substring(0, byteRand.length * 2);
        }

        if (byteAutn != null && byteAutn.length > 0) {
            strAutn = IccUtils.bytesToHexString(byteAutn).substring(0, byteAutn.length * 2);
        }
        log("simGbaAuthBootStrapMode strRand is " + strRand + " strAutn " + strAutn);

        return mSimAuthThread.doGeneralSimAuth(slotId, family, 1, 0xDD, strRand, strAutn);
    }

    /**
     * Request to run GBA authenitcation (NAF Derivation Mode)on UICC card
     * by indicated family.
     *
     * @param slotId indicated sim id
     * @param family indiacted family category
     *        UiccController.APP_FAM_3GPP =  1; //SIM/USIM
     *        UiccController.APP_FAM_3GPP2 = 2; //RUIM/CSIM
     *        UiccController.APP_FAM_IMS   = 3; //ISIM
     * @param byteNafId network application function id in byte array
     * @param byteImpi IMS private user identity in byte array
     *
     * @return reponse paramenters/data from UICC
     *
     */
    public byte[] simGbaAuthNafMode(int slotId, int family, byte[] byteNafId, byte[] byteImpi) {
        enforcePrivilegedPhoneStatePermission();

        if (mSimAuthThread == null) {
            log("simGbaAuthNafMode new thread");
            mSimAuthThread = new SimAuth(mPhone);
            mSimAuthThread.start();
        } else {
            log("simGbaAuthNafMode thread has been created.");
        }

        String strNafId = "";
        String strImpi = "";
        log("simGbaAuthNafMode session is " + family + " simId " + slotId);

        if (byteNafId != null && byteNafId.length > 0) {
            strNafId = IccUtils.bytesToHexString(byteNafId).substring(0, byteNafId.length * 2);
        }

        /* ISIM GBA NAF mode parameter should be NAF_ID.
         * USIM GAB NAF mode parameter should be NAF_ID + IMPI
         * If getIccApplicationChannel got 0, mean that ISIM not support */
        if (UiccController.getInstance().getIccApplicationChannel(slotId, family) == 0) {
            log("simGbaAuthNafMode ISIM not support.");
            if (byteImpi != null && byteImpi.length > 0) {
                strImpi = IccUtils.bytesToHexString(byteImpi).substring(0, byteImpi.length * 2);
            }
        }
        log("simGbaAuthNafMode NAF ID is " + strNafId + " IMPI " + strImpi);

        return mSimAuthThread.doGeneralSimAuth(slotId, family, 1, 0xDE, strNafId, strImpi);
    }

    /**
     * Since MTK keyguard has dismiss feature, we need to retrigger unlock event
     * when user try to access the SIM card.
     *
     * @param subId inidicated subscription
     *
     * @return true represent broadcast a unlock intent to notify keyguard
     *         false represent current state is not LOCKED state. No need to retrigger.
     *
     */
    public boolean broadcastIccUnlockIntent(int subId) {
        int state = TelephonyManager.getDefault().getSimState(SubscriptionManager.getSlotId(subId));

        log("[broadcastIccUnlockIntent] subId:" + subId + " state: " + state);

        String lockedReasion = "";

        switch (state) {
            case TelephonyManager.SIM_STATE_PIN_REQUIRED:
                lockedReasion = IccCardConstants.INTENT_VALUE_LOCKED_ON_PIN;
                break;
            case TelephonyManager.SIM_STATE_PUK_REQUIRED:
                lockedReasion = IccCardConstants.INTENT_VALUE_LOCKED_ON_PUK;
                break;
            case TelephonyManager.SIM_STATE_NETWORK_LOCKED:
                switch (getPhone(subId).getIccCard().getNetworkPersoType()) {
                    case PERSOSUBSTATE_SIM_NETWORK:
                        lockedReasion = IccCardConstants.INTENT_VALUE_LOCKED_NETWORK;
                        break;
                    case PERSOSUBSTATE_SIM_NETWORK_SUBSET:
                        lockedReasion = IccCardConstants.INTENT_VALUE_LOCKED_NETWORK_SUBSET;
                        break;
                    case PERSOSUBSTATE_SIM_CORPORATE:
                        lockedReasion = IccCardConstants.INTENT_VALUE_LOCKED_CORPORATE;
                        break;
                    case PERSOSUBSTATE_SIM_SERVICE_PROVIDER:
                        lockedReasion = IccCardConstants.INTENT_VALUE_LOCKED_SERVICE_PROVIDER;
                        break;
                    case PERSOSUBSTATE_SIM_SIM:
                        lockedReasion = IccCardConstants.INTENT_VALUE_LOCKED_SIM;
                        break;
                    default:
                        lockedReasion = IccCardConstants.INTENT_VALUE_ICC_UNKNOWN;
                }
                break;
            default:
                return false;
        }

        Intent intent = new Intent(TelephonyIntents.ACTION_UNLOCK_SIM_LOCK);

        intent.putExtra(IccCardConstants.INTENT_KEY_ICC_STATE,
                         IccCardConstants.INTENT_VALUE_ICC_LOCKED);
        intent.putExtra(IccCardConstants.INTENT_KEY_LOCKED_REASON, lockedReasion);
        SubscriptionManager.putPhoneIdAndSubIdExtra(intent, SubscriptionManager.getPhoneId(subId));
        log("[broadcastIccUnlockIntent] Broadcasting intent ACTION_UNLOCK_SIM_LOCK "
            + " reason " + state + " for slotId : " + SubscriptionManager.getSlotId(subId));

        mApp.sendBroadcastAsUser(intent, UserHandle.ALL);

        return true;
    }

    /**
     * Query if the radio is turned off by user.
     *
     * @param subId inidicated subscription
     *
     * @return true radio is turned off by user.
     *         false radio isn't turned off by user.
     *
     */
    public boolean isRadioOffBySimManagement(int subId) {
        boolean result = true;
        try {
            Context otherAppsContext = mApp.createPackageContext(
                    "com.android.phone", Context.CONTEXT_IGNORE_SECURITY);
            SharedPreferences mIccidPreference =
                    otherAppsContext.getSharedPreferences("RADIO_STATUS", 0);

            if (SubscriptionController.getInstance() != null) {
                int mSlotId = SubscriptionController.getInstance().getPhoneId(subId);

                if (mSlotId < 0 || mSlotId >= TelephonyManager.getDefault().getPhoneCount()) {
                    log("[isRadioOffBySimManagement]mSlotId: " + mSlotId);

                    return false;
                }

                String mIccId = SystemProperties.get(ICCRECORD_PROPERTY_ICCID[mSlotId], "");
                if ((mIccId != null) && (mIccidPreference != null)) {
                    log("[isRadioOffBySimManagement]SharedPreferences: "
                            + mIccidPreference.getAll().size() + ", IccId: " + mIccId);
                    result = mIccidPreference.contains(mIccId);
                }
            }

            log("[isRadioOffBySimManagement]result: " + result);
        } catch (NameNotFoundException e) {
            log("Fail to create com.android.phone createPackageContext");
        }
        return result;
    }

    // SIM switch
    /**
     * Get current phone capability
     *
     * @return the capability of phone. (@see PhoneConstants)
     */
    public int getPhoneCapability(int phoneId) {
        //return PhoneConstants.CAPABILITY_34G;
        return 0;
    }

    /**
     * Set capability to phones
     *
     * @param phoneId phones want to change capability
     * @param capability new capability for each phone
     */
    public void setPhoneCapability(int[] phoneId, int[] capability) {

    }

    /**
     * To config SIM swap mode(for dsda).
     *
     * @return true if config SIM Swap mode successful, or return false
     */
    public boolean configSimSwap(boolean toSwapped) {
        return true;
    }

    /**
     * To check SIM is swapped or not(for dsda).
     *
     * @return true if swapped, or return false
     */
    public boolean isSimSwapped() {
        return false;
    }

    /**
     * To Check if Capability Switch Manual Control Mode Enabled.
     *
     * @return true if Capability Switch manual control mode is enabled, else false;
     */
    public boolean isCapSwitchManualEnabled() {
        return true;
    }

    /**
     * Get item list that will be displayed on manual switch setting
     *
     * @return String[] contains items
     */
    public String[] getCapSwitchManualList() {
        return null;
    }


  /**
     * To get located PLMN from sepcified SIM modem  protocol
     * Returns current located PLMN string(ex: "46000") or null if not availble (ex: in flight mode or no signal area or this SIM is turned off)
     * @param subId Indicate which SIM subscription to query
     */
    public String getLocatedPlmn(int subId) {
        return getPhone(subId).getLocatedPlmn();
    }

   /**
     * Check if phone is hiding network temporary out of service state.
     * @param subId Indicate which SIM subscription to query
     * @return if phone is hiding network temporary out of service state.
    */
    public int getNetworkHideState(int subId) {
        return getPhone(subId).getNetworkHideState();
    }

   /**
     * Get the network service state for specified SIM.
     * @param subId Indicate which SIM subscription to query
     * @return service state.
     */
    public Bundle getServiceState(int subId) {
        Phone phone = getPhone(subId);
        if (phone != null) {
            Bundle data = new Bundle();
            phone.getServiceState().fillInNotifierBundle(data);
            return data;
        } else {
            log("Can't not get phone");
            return null;
        }
    }

    /**
     * Helper thread to turn async call to {@link #SimAuthentication} into
     * a synchronous one.
     */
    private static class SimAuth extends Thread {
        private Phone mTargetPhone;
        private boolean mDone = false;
        private IccIoResult mResponse = null;

        // For replies from SimCard interface
        private Handler mHandler;

        // For async handler to identify request type
        private static final int SIM_AUTH_GENERAL_COMPLETE = 300;

        public SimAuth(Phone phone) {
            mTargetPhone = phone;
        }

        @Override
        public void run() {
            Looper.prepare();
            synchronized (SimAuth.this) {
                mHandler = new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        AsyncResult ar = (AsyncResult) msg.obj;
                        switch (msg.what) {
                            case SIM_AUTH_GENERAL_COMPLETE:
                                log("SIM_AUTH_GENERAL_COMPLETE");
                                synchronized (SimAuth.this) {
                                    if (ar.exception != null) {
                                        log("SIM Auth Fail");
                                        mResponse = (IccIoResult) (ar.result);
                                    } else {
                                        mResponse = (IccIoResult) (ar.result);
                                    }
                                    log("SIM_AUTH_GENERAL_COMPLETE result is " + mResponse);
                                    mDone = true;
                                    SimAuth.this.notifyAll();
                                }
                                break;
                        }
                    }
                };
                SimAuth.this.notifyAll();
            }
            Looper.loop();
        }

        byte[] doGeneralSimAuth(int slotId, int family, int mode, int tag,
                String strRand, String strAutn) {
           synchronized (SimAuth.this) {
                while (mHandler == null) {
                    try {
                        SimAuth.this.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }

                mDone = false;
                mResponse = null;

                Message callback = Message.obtain(mHandler, SIM_AUTH_GENERAL_COMPLETE);

                int sessionId = UiccController.getInstance().getIccApplicationChannel(slotId, family);
                log("family = " + family + ", sessionId = " + sessionId);

                int[] subId = SubscriptionManager.getSubId(slotId);
                if (subId == null) {
                    log("slotId = " + slotId + ", subId is invalid.");
                    return null;
                } else {
                    getPhone(subId[0]).doGeneralSimAuthentication
                            (sessionId, mode, tag, strRand, strAutn, callback);
                }

                while (!mDone) {
                    try {
                        log("wait for done");
                        SimAuth.this.wait();
                    } catch (InterruptedException e) {
                        // Restore the interrupted status
                        Thread.currentThread().interrupt();
                    }
                }
                int len = 0;
                byte[] result = null;

                if (mResponse != null) {
                    // 2 bytes for sw1 and sw2
                    len = 2 + ((mResponse.payload == null) ? 0 : mResponse.payload.length);
                    result = new byte[len];

                    if (mResponse.payload != null) {
                        System.arraycopy(mResponse.payload, 0, result, 0, mResponse.payload.length);
                    }

                    result[len - 1] = (byte) mResponse.sw2;
                    result[len - 2] = (byte) mResponse.sw1;

                    // TODO: Should use IccUtils.bytesToHexString to print log info.
                    //for (int i = 0; i < len ; i++) {
                    //    log("Result = " + result[i]);
                    //}
                    //log("Result = " + new String(result));
                } else {
                    log("mResponse is null.");
                }

                log("done");
                return result;
            }
        }
    }

   /**
    * This function is used to get SIM phonebook storage information
    * by sim id.
    *
    * @param simId Indicate which sim(slot) to query
    * @return int[] which incated the storage info
    *         int[0]; // # of remaining entries
    *         int[1]; // # of total entries
    *         int[2]; // # max length of number
    *         int[3]; // # max length of alpha id
    *
    */
    public int[] getAdnStorageInfo(int subId) {
        Log.d(LOG_TAG, "getAdnStorageInfo " + subId);

        if (SubscriptionManager.isValidSubscriptionId(subId) == true) {
            if (mAdnInfoThread == null) {
                Log.d(LOG_TAG, "getAdnStorageInfo new thread ");
                mAdnInfoThread  = new QueryAdnInfoThread(subId);
                mAdnInfoThread.start();
            } else {
                mAdnInfoThread.setSubId(subId);
                Log.d(LOG_TAG, "getAdnStorageInfo old thread ");
            }
            return mAdnInfoThread.GetAdnStorageInfo();
        } else {
            Log.d(LOG_TAG, "getAdnStorageInfo subId is invalid.");
            int[] recordSize;
            recordSize = new int[4];
            recordSize[0] = 0; // # of remaining entries
            recordSize[1] = 0; // # of total entries
            recordSize[2] = 0; // # max length of number
            recordSize[3] = 0; // # max length of alpha id
            return recordSize;
        }
    }

    private static class QueryAdnInfoThread extends Thread {

        private int mSubId;
        private boolean mDone = false;
        private int[] recordSize;

        private Handler mHandler;

        // For async handler to identify request type
        private static final int EVENT_QUERY_PHB_ADN_INFO = 100;

        public QueryAdnInfoThread(int subId) {
            mSubId = subId;
        }
        public void setSubId(int subId) {
            synchronized (QueryAdnInfoThread.this) {
                mSubId = subId;
                mDone = false;
            }
        }

        @Override
        public void run() {
            Looper.prepare();
            synchronized (QueryAdnInfoThread.this) {
                mHandler = new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        AsyncResult ar = (AsyncResult) msg.obj;

                        switch (msg.what) {
                            case EVENT_QUERY_PHB_ADN_INFO:
                                Log.d(LOG_TAG, "EVENT_QUERY_PHB_ADN_INFO");
                                synchronized (QueryAdnInfoThread.this) {
                                    mDone = true;
                                    int[] info = (int[]) (ar.result);
                                    if (info != null) {
                                        recordSize = new int[4];
                                        recordSize[0] = info[0]; // # of remaining entries
                                        recordSize[1] = info[1]; // # of total entries
                                        recordSize[2] = info[2]; // # max length of number
                                        recordSize[3] = info[3]; // # max length of alpha id
                                        Log.d(LOG_TAG, "recordSize[0]=" + recordSize[0] +
                                                ",recordSize[1]=" + recordSize[1] +
                                                "recordSize[2]=" + recordSize[2] +
                                                ",recordSize[3]=" + recordSize[3]);
                                    }
                                    else {
                                        recordSize = new int[4];
                                        recordSize[0] = 0; // # of remaining entries
                                        recordSize[1] = 0; // # of total entries
                                        recordSize[2] = 0; // # max length of number
                                        recordSize[3] = 0; // # max length of alpha id
                                    }
                                    QueryAdnInfoThread.this.notifyAll();

                                }
                                break;
                            }
                      }
                };
                QueryAdnInfoThread.this.notifyAll();
            }
            Looper.loop();
        }

        public int[] GetAdnStorageInfo() {
            synchronized (QueryAdnInfoThread.this) {
                while (mHandler == null) {
                    try {
                        QueryAdnInfoThread.this.wait();

                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
                Message response = Message.obtain(mHandler, EVENT_QUERY_PHB_ADN_INFO);

                getPhone(mSubId).queryPhbStorageInfo(RILConstants.PHB_ADN, response);

                while (!mDone) {
                    try {
                        Log.d(LOG_TAG, "wait for done");
                        QueryAdnInfoThread.this.wait();
                    } catch (InterruptedException e) {
                        // Restore the interrupted status
                        Thread.currentThread().interrupt();
                    }
                }
                Log.d(LOG_TAG, "done");
                return recordSize;
            }
        }
    }

   /**
    * This function is used to check if the SIM phonebook is ready
    * by sim id.
    *
    * @param simId Indicate which sim(slot) to query
    * @return true if phone book is ready.
    *
    */
    public boolean isPhbReady(int subId) {
        String strPhbReady = "false";
        String strAllSimState = "";
        String strCurSimState = "";
        boolean isSimLocked = false;
        int phoneId = SubscriptionManager.getPhoneId(subId);
        int slotId = SubscriptionManager.getSlotId(subId);

        if (SubscriptionManager.isValidSlotId(slotId) == true) {
            strAllSimState = SystemProperties.get(TelephonyProperties.PROPERTY_SIM_STATE);

            if ((strAllSimState != null) && (strAllSimState.length() > 0)) {
                String values[] = strAllSimState.split(",");
                if ((phoneId >= 0) && (phoneId < values.length) && (values[phoneId] != null)) {
                    strCurSimState = values[phoneId];
                }
            }

            isSimLocked = (strCurSimState.equals("NETWORK_LOCKED") ||
                    strCurSimState.equals("PIN_REQUIRED"));
                    //In PUK_REQUIRED state, phb can be accessed.

            // M: PHB Revise
            // Use different system properties to indicate the GSM/C2K PHB
            // ready state
            Phone phone = PhoneFactory.getPhone(phoneId);
            if (phone != null) {
                if ((phone.getPhoneType() == PhoneConstants.PHONE_TYPE_GSM) ||
                        CsimPhbStorageInfo.isUsingGsmPhbReady(
                            phone.getIccFileHandler())) {
                    if (PhoneConstants.SIM_ID_1 == slotId) {
                        strPhbReady = SystemProperties.get("gsm.sim.ril.phbready", "false");
                    } else {
                        strPhbReady = SystemProperties.get("gsm.sim.ril.phbready."
                                                           + (slotId + 1), "false");
                    }
                } else {
                    if (PhoneConstants.SIM_ID_1 == slotId) {
                        strPhbReady = SystemProperties.get("cdma.sim.ril.phbready", "false");
                    } else {
                        strPhbReady = SystemProperties.get("cdma.sim.ril.phbready."
                                                           + (slotId + 1), "false");
                    }
                }
                log("[isPhbReady] phoneType is: " + phone.getPhoneType());
            }
            else {
                log("[isPhbReady] phone is not create, return false");
                return false;
            }
        }

        log("[isPhbReady] subId:" + subId + ", slotId: " + slotId + ", isPhbReady: "
            + strPhbReady + ",strSimState: " + strAllSimState);

        return (strPhbReady.equals("true") && !isSimLocked);
    }

    public boolean isAirplanemodeAvailableNow() {
        return mApp.isAllowAirplaneModeChange();
    }

    // SMS parts
    private class ScAddress {
        public String mAddress;
        public int mSubId = SubscriptionManager.INVALID_SUBSCRIPTION_ID;

        public ScAddress(int subId, String addr) {
            mAddress = addr;
            mSubId = subId;
        }
    }

    /**
     * Get service center address
     *
     * @param subId subscription identity
     *
     * @return service message center address
     */
    public Bundle getScAddressUsingSubId(int subId) {
        log("getScAddressUsingSubId, subId: " + subId);

        int phoneId = SubscriptionManager.getPhoneId(subId);
        if (phoneId == SubscriptionManager.INVALID_SUBSCRIPTION_ID) {
            log("no corresponding phone id");
            return null;
        }

        Bundle result = (Bundle) sendRequest(CMD_HANDLE_GET_SCA, subId);

        log("getScAddressUsingSubId: exit with " + result.toString());

        return result;
    }

    /**
     * Set service message center address
     *
     * @param subId subscription identity
     * @param address service message center addressto be set
     *
     * @return true for success, false for failure
     */
    public boolean setScAddressUsingSubId(int subId, String address) {
        log("setScAddressUsingSubId, subId: " + subId);

        int phoneId = SubscriptionManager.getPhoneId(subId);
        if (phoneId == SubscriptionManager.INVALID_SUBSCRIPTION_ID) {
            log("no corresponding phone id");
            return false;
        }

        ScAddress scAddress = new ScAddress(subId, address);

        Boolean result = (Boolean) sendRequest(CMD_HANDLE_SET_SCA, scAddress);

        log("setScAddressUsingSubId: exit with " + result.booleanValue());
        return result.booleanValue();
    }
    // SMS part end

    /**
     * This function will get DcFailCause with int format.
     *
     * @param apnType for geting which last error of apnType
     * @param phoneId for getting the current using phone
     * @return int: return int failCause value
     */
    public int getLastDataConnectionFailCause(String apnType, int phoneId) {
        // N migration, API not ready
        /*
        DcFailCause failCause = PhoneFactory.getPhone(phoneId).
                                    getLastDataConnectionFailCause(apnType);
        return failCause.getErrorCode();
        */
        return 0;
    }

    /**
     * This function will get link properties of input apn type.
     *
     * @param apnType input apn type for geting link properties
     * @param phoneId for getting the current using phone
     * @return LinkProperties: return correspondent link properties with input apn type
     */
    public LinkProperties getLinkProperties(String apnType, int phoneId) {
        return PhoneFactory.getPhone(phoneId).getLinkProperties(apnType);
    }

    /**
     * Set phone radio type and access technology.
     *
     * @param rafs an RadioAccessFamily array to indicate all phone's
     *        new radio access family. The length of RadioAccessFamily
     *        must equal to phone count.
     * @return true if start setPhoneRat successfully.
     */
    @Override
    public boolean setRadioCapability(RadioAccessFamily[] rafs) {
        boolean ret = true;
        try {
            ProxyController.getInstance().setRadioCapability(rafs);
        } catch (RuntimeException e) {
            Log.w(LOG_TAG, "setRadioCapability: Runtime Exception");
            e.printStackTrace();
            ret = false;
        }
        return ret;
    }

    /**
     * Set ViLTE settngs by given sub-id.
     * @param enable The state for ViLTE settings.
     * @param subId The subId for query
     * @hide
     */
    public void enableVideoCalling(boolean enable, int subId) {
        mApp.enforceCallingOrSelfPermission(android.Manifest.permission.MODIFY_PHONE_STATE, null);

        int phoneId = SubscriptionController.getInstance().getPhoneId(subId);
        log("enableVideoCalling subId=(" + subId + "), phoneId("+ phoneId + ") = " + enable);

        ImsManager.setVtSetting(mPhone.getContext(), enable, phoneId);
    }

    /**
     * Get ViLTE registration state by given sub-id.
     * @param callingPackage The calling package
     * @param subId The subId for query
     * @return true if viLTE is registered, or false
     * @hide
     */
    public boolean isVideoCallingEnabled(String callingPackage, int subId) {
        if (!canReadPhoneState(callingPackage, "isVideoCallingEnabled")) {
            return false;
        }

        int phoneId = SubscriptionController.getInstance().getPhoneId(subId);

        // Check the user preference and the  system-level IMS setting. Even if the user has
        // enabled video calling, if IMS is disabled we aren't able to support video calling.
        // In the long run, we may instead need to check if there exists a connection service
        // which can support video calling.
        boolean enable = ImsManager.isVtEnabledByPlatform(mPhone.getContext(), phoneId)
                && ImsManager.isEnhanced4gLteModeSettingEnabledByUser(mPhone.getContext(), phoneId)
                && ImsManager.isVtEnabledByUser(mPhone.getContext(), phoneId);
        log("isVideoCallingEnabled subId=(" + subId + "), phoneId("+ phoneId + ") = " + enable);

        return enable;
    }


    /**
     * Check read phone state permission by calling package.
     * @param callingPackage The calling package need to be checked.
     * @param message
     * @return true if can read phone state, or false
     * @hide
     */
    private boolean canReadPhoneState(String callingPackage, String message) {
        try {
            mApp.enforceCallingOrSelfPermission(
                    android.Manifest.permission.READ_PRIVILEGED_PHONE_STATE, message);

            // SKIP checking for run-time permission since caller or self has PRIVILEDGED permission
            return true;
        } catch (SecurityException e) {
            mApp.enforceCallingOrSelfPermission(android.Manifest.permission.READ_PHONE_STATE,
                    message);
        }

        if (mAppOps.noteOp(AppOpsManager.OP_READ_PHONE_STATE, Binder.getCallingUid(),
                callingPackage) != AppOpsManager.MODE_ALLOWED) {
            return false;
        }

        return true;
    }

    /**
     * Check if under capability switching.
     *
     * @return true if switching
     */
    public boolean isCapabilitySwitching() {
        return ProxyController.getInstance().isCapabilitySwitching();
    }

    public void setTrmForPhone(int phoneId, int mode) {
        CommandsInterface ci;
        Phone phone = PhoneFactory.getPhone(phoneId);
        if (phone != null) {
            ci = phone.mCi;
            log("setTrmForPhone phoneId: " + phoneId + " mode:" + mode);
            ci.setTrm(mode, null);
        } else {
            log("phone is null");
        }
    }

    /**
     * Get main capability phone id.
     * @return The phone id with highest capability.
     */
    public int getMainCapabilityPhoneId() {
        return RadioCapabilitySwitchUtil.getMainCapabilityPhoneId();
    }

   /**
     * Initialze external SIM service on phone process.
     *
     * @hide
     */
    public void initializeService(String serviceName) {
        // FIXME: need to design a whitelist mechansim, only alloew some service to execute this.
        if ("osi".equals(serviceName)) {
            SystemProperties.set("ctl.start", serviceName);
        }
    }

   /**
     * Finalize external SIM service on phone process.
     *
     * @hide
     */
    public void finalizeService(String serviceName) {
        // FIXME: need to design a whitelist mechansim, only alloew some service to execute this.
        if ("osi".equals(serviceName)) {
            SystemProperties.set("ctl.stop", serviceName);
        }
    }

   /**
     * Return the sim card if in home network.
     *
     * @param subId subscription ID to be queried.
     * @return true if in home network.
     * @hide
     */
    public boolean isInHomeNetwork(int subId) {
        final int phoneId = SubscriptionManager.getPhoneId(subId);
        boolean isInHomeNetwork = false;
        final Phone phone = PhoneFactory.getPhone(phoneId);
        if (phone != null) {
            ServiceState serviceState = phone.getServiceState();
            if (serviceState != null) {
                isInHomeNetwork = inSameCountry(phoneId, serviceState.getVoiceOperatorNumeric());
            }
        }
        log("isInHomeNetwork, subId=" + subId + " ,phoneId=" + phoneId
                + " ,isInHomeNetwork=" + isInHomeNetwork);
        return isInHomeNetwork;
    }

    /**
     * Check ISO country by MCC to see if phone is roaming in same registered country.
     *
     * @param phoneId for which phone inSameCountry is returned
     * @param operatorNumeric registered operator numeric
     * @return true if in same country.
     * @hide
     */
    private static final boolean inSameCountry(int phoneId, String operatorNumeric) {
        if (TextUtils.isEmpty(operatorNumeric) || (operatorNumeric.length() < 5)
                || (!TextUtils.isDigitsOnly(operatorNumeric))) {
            // Not a valid network
            log("inSameCountry, Not a valid network"
                    + ", phoneId=" + phoneId + ", operatorNumeric=" + operatorNumeric);
            return true;
        }

        final String homeNumeric = getHomeOperatorNumeric(phoneId);
        if (TextUtils.isEmpty(homeNumeric) || (homeNumeric.length() < 5)
                || (!TextUtils.isDigitsOnly(homeNumeric))) {
            // Not a valid SIM MCC
            log("inSameCountry, Not a valid SIM MCC"
                    + ", phoneId=" + phoneId + ", homeNumeric=" + homeNumeric);
            return true;
        }

        boolean inSameCountry = true;
        final String networkMCC = operatorNumeric.substring(0, 3);
        final String homeMCC = homeNumeric.substring(0, 3);
        final String networkCountry = MccTable.countryCodeForMcc(Integer.parseInt(networkMCC));
        final String homeCountry = MccTable.countryCodeForMcc(Integer.parseInt(homeMCC));
        log("inSameCountry, phoneId=" + phoneId
                + ", homeMCC=" + homeMCC
                + ", networkMCC=" + networkMCC
                + ", homeCountry=" + homeCountry
                + ", networkCountry=" + networkCountry);
        if (networkCountry.isEmpty() || homeCountry.isEmpty()) {
            // Not a valid country
            return true;
        }
        inSameCountry = homeCountry.equals(networkCountry);
        if (inSameCountry) {
            return inSameCountry;
        }
        // special same country cases
        if ("us".equals(homeCountry) && "vi".equals(networkCountry)) {
            inSameCountry = true;
        } else if ("vi".equals(homeCountry) && "us".equals(networkCountry)) {
            inSameCountry = true;
        } else if ("cn".equals(homeCountry) && "mo".equals(networkCountry)) {
            inSameCountry = true;
        }

        log("inSameCountry, phoneId=" + phoneId + ", inSameCountry=" + inSameCountry);
        return inSameCountry;
    }

    /**
     * Returns the Service Provider Name (SPN).
     *
     * @param phoneId for which HomeOperatorNumeric is returned
     * @return the Service Provider Name (SPN)
     * @hide
     */
    private static final String getHomeOperatorNumeric(int phoneId) {
        String numeric = TelephonyManager.getDefault().getSimOperatorNumericForPhone(phoneId);
        if (TextUtils.isEmpty(numeric)) {
            numeric = SystemProperties.get("ro.cdma.home.operator.numeric", "");
        }
        log("getHomeOperatorNumeric, phoneId=" + phoneId + ", numeric=" + numeric);
        return numeric;
    }

    // M: [LTE][Low Power][UL traffic shaping] Start
    public boolean setLteAccessStratumReport(boolean enabled) {
        int phoneId = RadioCapabilitySwitchUtil.getMainCapabilityPhoneId();
        int dataPhoneId = SubscriptionManager
                .getPhoneId(SubscriptionManager.getDefaultDataSubscriptionId());
        Phone phone = PhoneFactory.getPhone(phoneId);
        if (phone == null || phoneId != dataPhoneId) {
            loge("setLteAccessStratumReport incorrect parameter [getMainPhoneId = "
                    + RadioCapabilitySwitchUtil.getMainCapabilityPhoneId()
                    + ", dataPhoneId = " + dataPhoneId + "]");
            if (phoneId != dataPhoneId) {
                if (DBG) {
                    loge("setLteAccessStratumReport: MainPhoneId and dataPhoneId aren't the same");
                }
            }
            return false;
        }
        if (DBG) log("setLteAccessStratumReport: enabled = " + enabled);
        Boolean success = (Boolean) sendRequest(CMD_SET_LTE_ACCESS_STRATUM_STATE,
                new Boolean(enabled), new Integer(phoneId));
        if (DBG) log("setLteAccessStratumReport: success = " + success);
        return success;

    }

    public boolean setLteUplinkDataTransfer(boolean isOn, int timeMillis) {
        int state = 1;
        int phoneId = RadioCapabilitySwitchUtil.getMainCapabilityPhoneId();
        int dataPhoneId = SubscriptionManager
                .getPhoneId(SubscriptionManager.getDefaultDataSubscriptionId());
        Phone phone = PhoneFactory.getPhone(phoneId);
        if (phone == null || phoneId != dataPhoneId) {
            loge("setLteUplinkDataTransfer incorrect parameter [getMainPhoneId = "
                    + RadioCapabilitySwitchUtil.getMainCapabilityPhoneId()
                    + ", dataPhoneId = " + dataPhoneId + "]");
            if (phoneId != dataPhoneId) {
                if (DBG) {
                    loge("setLteUplinkDataTransfer: MainPhoneId and dataPhoneId aren't the same");
                }
            }
            return false;
        }
        if (DBG) {
            log("setLteUplinkDataTransfer: isOn = " + isOn
                    + ", Tclose timer = " + (timeMillis/1000));
        }
        if (!isOn) state = (timeMillis/1000) << 16 | 0;
        Boolean success = (Boolean) sendRequest(CMD_SET_LTE_UPLINK_DATA_TRANSFER_STATE,
                new Integer(state), new Integer(phoneId));
        if (DBG) log("setLteUplinkDataTransfer: success = " + success);
        return success;
    }

    public String getLteAccessStratumState() {
        int phoneId = RadioCapabilitySwitchUtil.getMainCapabilityPhoneId();
        int dataPhoneId = SubscriptionManager
                .getPhoneId(SubscriptionManager.getDefaultDataSubscriptionId());
        Phone phone = PhoneFactory.getPhone(phoneId);
        String state = PhoneConstants.LTE_ACCESS_STRATUM_STATE_UNKNOWN;
        if (phone == null || phoneId != dataPhoneId) {
            loge("getLteAccessStratumState incorrect parameter [getMainPhoneId = "
                    + RadioCapabilitySwitchUtil.getMainCapabilityPhoneId()
                    + ", dataPhoneId = " + dataPhoneId + "]");
            if (phoneId != dataPhoneId) {
                if (DBG) {
                    loge("getLteAccessStratumState: MainPhoneId and dataPhoneId aren't the same");
                }
            }
        } else {
            DcTracker dcTracker = phone.mDcTracker;
            state = dcTracker.getLteAccessStratumState();
        }
        if (DBG) log("getLteAccessStratumState: " + state);
        return state;
    }

    public boolean isSharedDefaultApn() {
        int phoneId = RadioCapabilitySwitchUtil.getMainCapabilityPhoneId();
        int dataPhoneId = SubscriptionManager
                .getPhoneId(SubscriptionManager.getDefaultDataSubscriptionId());
        Phone phone = PhoneFactory.getPhone(phoneId);
        boolean isSharedDefaultApn = false;
        if (phone == null || phoneId != dataPhoneId) {
            loge("isSharedDefaultApn incorrect parameter [getMainPhoneId = "
                    + RadioCapabilitySwitchUtil.getMainCapabilityPhoneId()
                    + ", dataPhoneId = " + dataPhoneId + "]");
            if (phoneId != dataPhoneId) {
                if (DBG) loge("isSharedDefaultApn: MainPhoneId and dataPhoneId aren't the same");
            }
        } else {
            DcTracker dcTracker = phone.mDcTracker;
            isSharedDefaultApn = dcTracker.isSharedDefaultApn();
        }
        if (DBG) log("isSharedDefaultApn: " + isSharedDefaultApn);
        return isSharedDefaultApn;
    }
    // M: [LTE][Low Power][UL traffic shaping] End


    /**
     * Check if need to enable the OMH feature.
     * @param subId SubId for the card to be checked.
     * @return if the card is enable OMH.
     */
    public boolean isOmhEnable(int subId) {
        if (!SubscriptionManager.isValidSubscriptionId(subId)) {
            return false;
        }
        Phone phone = getPhone(subId);
        if (phone == null || phone.getPhoneType() != PhoneConstants.PHONE_TYPE_CDMA) {
            return false;
        }

        String operator = TelephonyManager.getDefault()
                .getSimOperatorNumeric(subId);
        if (DBG_LOC) {
            log("isOmhEnable: the subId = " + subId + " operator name = " + operator);
        }

        if (operator == null || mOmhOperators == null) {
            return false;
        }

        for (String s : mOmhOperators) {
            if (DBG_LOC) {
                log("isOmhEnable: operator = " + s);
            }
            if (operator.equals(s)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Check if the card is OMH card.
     * @param subId the sub id for check.
     * @return if the card is OMH card.
     */
    public boolean isOmhCard(int subId) {
        if (!isOmhEnable(subId)) {
            return false;
        }
        return getIccFileAdapterBySubId(subId).isOmhCard();
    }

    /**
     * Get the MMS Iusser Connectivity Parameters.
     * @param subId The sub id used to get icp.
     * @return The MmsIcpInfo object.
     * @hide
     */
    public MmsIcpInfo getMmsIcpInfo(int subId) {
        Object object = getIccFileAdapterBySubId(subId)
                .getMmsIcpInfo();
        if (object instanceof MmsIcpInfo) {
            return (MmsIcpInfo) object;
        }
        return null;
    }

    /**
     * Get MMS config information, for example, max MMS size.
     * @param subId used to get the info.
     * @return the MmsConfigInfo object for MMS config.
     * @hide
     */
    public MmsConfigInfo getMmsConfigInfo(int subId) {
        Object object = getIccFileAdapterBySubId(subId)
                .getMmsConfigInfo();
        if (object instanceof MmsConfigInfo) {
            return (MmsConfigInfo) object;
        }
        return null;
    }

    /**
     * Return the user customized ecc list.
     *
     * @return Return the user customized ecc list
     * @hide
     */
    public Bundle getUserCustomizedEccList() {
        Bundle result = null;
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mApp);
        int count = sp.getInt("ecc_count", 0);
        if (count > 0) {
            ArrayList<String> names = new ArrayList<String>();
            ArrayList<String> numbers = new ArrayList<String>();
            for (int i = 0; i < count; i++) {
                names.add(sp.getString("ecc_name" + i, ""));
                numbers.add(sp.getString("ecc_number" + i, ""));
            }
            result = new Bundle();
            result.putStringArrayList("names", names);
            result.putStringArrayList("numbers", numbers);
        }
        return result;
    }

    /**
     * Update the user customized ecc list.
     * @param eccString The ECC number string
     * @return true if succeed, or false
     * @hide
     */
    public boolean updateUserCustomizedEccList(Bundle bundle) {
        mUserCustomizedEccList.clear();
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mApp);
        Editor editor = sp.edit();
        int count = sp.getInt("ecc_count", 0);
        editor.putInt("ecc_count", 0);
        for (int i = 0; i < count; i++) {
            editor.remove("ecc_name" + i);
            editor.remove("ecc_number" + i);
        }
        if (bundle != null) {
            ArrayList<String> names = bundle.getStringArrayList("names");
            ArrayList<String> numbers = bundle.getStringArrayList("numbers");
            if (names != null && numbers != null && names.size() == numbers.size()) {
                editor.putInt("ecc_count", names.size());
                for (int i = 0; i < names.size(); i++) {
                    EccEntry entry = new EccEntry(names.get(i), numbers.get(i));
                    mUserCustomizedEccList.add(entry);
                    editor.putString("ecc_name" + i, names.get(i));
                    editor.putString("ecc_number" + i, numbers.get(i));
                }
            }
        }
        editor.commit();
        log("[updateUserCustomizedEccList] mUserCustomizedEccList: " + mUserCustomizedEccList);
        return true;
    }

    /**
     * Check if the number is user customized ecc.
     * @param number The number need to check
     * @return true if yes, or false
     * @hide
     */
    public boolean isUserCustomizedEcc(String number) {
        if (number == null || mUserCustomizedEccList.size() == 0) {
            log("[isUserCustomizedEcc] No number or no customized number!");
            return false;
        }

        boolean isAirplaneModeOn = Settings.Global.getInt(mApp.getContentResolver(),
                Settings.Global.AIRPLANE_MODE_ON, 0) > 0;
        if ("1".equals(SystemProperties.get("ro.mtk_flight_mode_power_off_md"))
                && isAirplaneModeOn) {
            log("[isUserCustomizedEcc] airplane mode on, return false!");
            return false;
        }

        int subIdCdma = SubscriptionManager.INVALID_SUBSCRIPTION_ID;
        int cdmaSlotId = SubscriptionManager.INVALID_SIM_SLOT_INDEX;
        TelephonyManager tm = TelephonyManager.getDefault();
        int simCount = tm.getSimCount();
        int tmpSubId = SubscriptionManager.INVALID_SUBSCRIPTION_ID;
        boolean isCdmaSimInsert = false;
        for (int i = 0; i < simCount; i++) {
            int[] subIds = SubscriptionManager.getSubId(i);
            if (subIds != null && subIds.length > 0) {
                tmpSubId= subIds[0];
            }
            if (tm.getCurrentPhoneType(tmpSubId) == PhoneConstants.PHONE_TYPE_CDMA) {
                subIdCdma = tmpSubId;
                cdmaSlotId = i;
                break;
            }
        }
        if (subIdCdma != SubscriptionManager.INVALID_SUBSCRIPTION_ID) {
            isCdmaSimInsert = tm.hasIccCard(cdmaSlotId);
        }

        // return false if no OMH card
        if (!isCdmaSimInsert || !isOmhCard(subIdCdma)) {
            log("[isUserCustomizedEcc] no OMH card return false, isCdmaSimInsert: "
                    + isCdmaSimInsert);
            return false;
        }

        log("[isUserCustomizedEcc] mUserCustomizedEccList: " + mUserCustomizedEccList);

        String numberPlus = null;
        String ecc = null;
        for (EccEntry entry : mUserCustomizedEccList) {
            ecc = entry.getEcc();
            numberPlus = ecc + "+";
            if (ecc.equals(number) || numberPlus.equals(number)) {
                return true;
            }
        }
        return false;
    }

    private void omhInit() {
        //Init opeartors list that support OMH.
        Context context = mPhone.getContext();
        mOmhOperators = context.getResources().getStringArray(
                com.mediatek.internal.R.array.operator_support_omh_list);
        //Create for OMH feature
        int numPhones = TelephonyManager.getDefault().getPhoneCount();
        sIccFileAdapter = new IccFileAdapter[numPhones];
        for (int i = 0; i < numPhones; i++) {
            Phone simPhone  =  PhoneFactory.getPhone(i);
            sIccFileAdapter[i] = new IccFileAdapter(mApp, simPhone);
        }
    }

    private IccFileAdapter getIccFileAdapterBySubId(int subId) {
        int phoneId = SubscriptionManager.getPhoneId(subId);
        return sIccFileAdapter[phoneId];
    }

    /**
     * get call forwarding feature code.
     * @param type call forwarding type
     * @param subId sub id
     * @return call forwarding feature code
     * @hide
     */
    public int[] getCallForwardingFc(int type, int subId) {
        int phoneId = SubscriptionManager.getPhoneId(subId);
        if (!SubscriptionManager.isValidPhoneId(phoneId)) {
            return null;
        }
        int[] cf = null;
        switch (type) {
            case 1:
                cf = sIccFileAdapter[phoneId].getFcsForApp(3, 22, subId);
                break;
            case 2:
                cf = sIccFileAdapter[phoneId].getFcsForApp(3, 7, subId);
                break;
            case 3:
                cf = sIccFileAdapter[phoneId].getFcsForApp(8, 12, subId);
                break;
            case 4:
                cf = sIccFileAdapter[phoneId].getFcsForApp(13, 17, subId);
                break;
            case 5:
                cf = sIccFileAdapter[phoneId].getFcsForApp(18, 22, subId);
                break;
            default:
                log("getCallForwardingFc, invalid code.");
                break;
        }
        return cf;
    }

    /**
     * get call waiting feature code.
     * @param subId sub id
     * @return call waiting feature code
     * @hide
     */
    public int[] getCallWaitingFc(int subId) {
        int phoneId = SubscriptionManager.getPhoneId(subId);
        if (!SubscriptionManager.isValidPhoneId(phoneId)) {
            return null;
        }
        int[] cf = sIccFileAdapter[phoneId].getFcsForApp(23, 25, subId);
        return cf;
    }

    /**
     * get do not disturb feature code.
     * @param subId sub id
     * @return do not disturb feature code
     * @hide
     */
    public int[] getDonotDisturbFc(int subId) {
        int phoneId = SubscriptionManager.getPhoneId(subId);
        if (!SubscriptionManager.isValidPhoneId(phoneId)) {
            return null;
        }
        int[] cf = sIccFileAdapter[phoneId].getFcsForApp(30, 31, subId);
        return cf;
    }

    /**
     * get voice message retrieve feature code.
     * @param subId sub id
     * @return voice message retrieve feature code
     * @hide
     */
    public int[] getVMRetrieveFc(int subId) {
        log("getVMRetrieveFC");
        int phoneId = SubscriptionManager.getPhoneId(subId);
        if (!SubscriptionManager.isValidPhoneId(phoneId)) {
            return null;
        }
        int[] cf = sIccFileAdapter[phoneId].getFcsForApp(38, 38, subId);
        return cf;
    }

    /**
     * get cell broadcast priority from RUIM.
     * @param subId sub id
     * @param userCategory service category
     * @param userPriority user priority
     * @return cell broadcast priority
     * @hide
     */
    public int getBcsmsCfgFromRuim(int subId, int userCategory, int userPriority) {
        log("getBcsmsCfgFromRuim");
        int phoneId = SubscriptionManager.getPhoneId(subId);
        if (!SubscriptionManager.isValidPhoneId(phoneId)) {
            return -1;
        }
        int ret = sIccFileAdapter[phoneId].getBcsmsCfgFromRuim(userCategory, userPriority);
        return ret;
    }

    /**
     * get next message ID from RUIM.
     * @param subId sub id
     * @return message ID
     * @hide
     */
    public int getNextMessageId(int subId) {
        log("getNextMessageId");
        int phoneId = SubscriptionManager.getPhoneId(subId);
        if (!SubscriptionManager.isValidPhoneId(phoneId)) {
            return -1;
        }
        int ret = sIccFileAdapter[phoneId].getNextMessageId();
        return ret;
    }

    /**
     * get wap message ID from RUIM.
     * @param subId sub id
     * @return wap message ID
     * @hide
     */
    public int getWapMsgId(int subId) {
        log("getWapMsgId");
        int phoneId = SubscriptionManager.getPhoneId(subId);
        if (!SubscriptionManager.isValidPhoneId(phoneId)) {
            return -1;
        }
        int ret = sIccFileAdapter[phoneId].getWapMsgId();
        return ret;
    }

    @Override
    public Bundle getCellLocationUsingSlotId(int slotId) {
        enforceFineOrCoarseLocationPermission("getCellLocationUsingSlotId");

        if (checkIfCallerIsSelfOrForegroundUser()) {
            if (DBG_LOC) log("getCellLocationUsingSlotId: is active user");
            Bundle data = new Bundle();
            try {
                int subId[] = mSubscriptionController.getSubIdUsingSlotId(slotId);
                final Phone phone = (subId != null) ? getPhone(subId[0]) : null;

                if (subId != null && phone != null) {
                    CellLocation cellLocation = phone.getCellLocation();
                    if (cellLocation != null) {
                        if (phone.getPhoneType() == PhoneConstants.PHONE_TYPE_GSM) {
                            if (!((GsmCellLocation) cellLocation).isEmpty()) {
                                cellLocation.fillInNotifierBundle(data);
                                log("phone.getGsmCellLocation for slotId[" +
                                        slotId + "]:" + data);
                                return data;
                            }
                        } else if (phone.getPhoneType() == PhoneConstants.PHONE_TYPE_CDMA) {
                            if (!((CdmaCellLocation) cellLocation).isEmpty()) {
                                cellLocation.fillInNotifierBundle(data);
                                log("phone.getCdmaCellLocation for slotId[" +
                                       slotId + "]:" + data);
                                return data;
                            }
                        } else {
                            log("phone.getCellLocationUsingSlotId: phone type is abnormal");
                        }
                    }
                    log("phone.getCellLocationUsingSlotId: is null");
                } else {
                    log("phone or subId: is null");
                }
            } catch (IllegalStateException ex) {
                log("IllegalStateException");
                return null;
            }
            return data;
        } else {
            log("getCellLocationUsingSlotId: suppress non-active user");
            return null;
        }
    }

    @Override
    public List<NeighboringCellInfo> getNeighboringCellInfoUsingSlotId(int slotId) {
        enforceFineOrCoarseLocationPermission("getNeighboringCellInfoUsingSlotId");

        if (checkIfCallerIsSelfOrForegroundUser()) {
            if (DBG_LOC) log("getNeighboringCellInfoUsingSlotId: is active user");

            ArrayList<NeighboringCellInfo> cells = null;

            try {
                int subId[] = mSubscriptionController.getSubIdUsingSlotId(slotId);
                final Phone phone = (subId != null) ? getPhone(subId[0]) : null;

                if (subId != null && phone != null) {
                    cells = (ArrayList<NeighboringCellInfo>) sendRequest(
                            CMD_HANDLE_NEIGHBORING_CELL, phone, null);
                } else {
                    log("phone or subId: is null");
                }
            } catch (RuntimeException e) {
                Log.e(LOG_TAG, "getNeighboringCellInfoUsingSlotId " + e);
            }
            return cells;
        } else {
            if (DBG_LOC) log("getNeighboringCellInfoUsingSlotId: suppress non-active user");
            return null;
        }
    }

    private void enforceFineOrCoarseLocationPermission(String message) {
        try {
            mApp.enforceCallingOrSelfPermission(
                    android.Manifest.permission.ACCESS_FINE_LOCATION, null);
        } catch (SecurityException e) {
            // If we have ACCESS_FINE_LOCATION permission, skip the check for ACCESS_COARSE_LOCATION
            // A failure should throw the SecurityException from ACCESS_COARSE_LOCATION since this
            // is the weaker precondition
            mApp.enforceCallingOrSelfPermission(
                    android.Manifest.permission.ACCESS_COARSE_LOCATION, message);
        }
    }

    //
    // Internal helper methods.
    //

    private static boolean checkIfCallerIsSelfOrForegroundUser() {
        boolean ok;

        boolean self = Binder.getCallingUid() == Process.myUid();
        if (!self) {
            // Get the caller's user id then clear the calling identity
            // which will be restored in the finally clause.
            int callingUser = UserHandle.getCallingUserId();
            long ident = Binder.clearCallingIdentity();

            try {
                // With calling identity cleared the current user is the foreground user.
                int foregroundUser = ActivityManager.getCurrentUser();
                ok = (foregroundUser == callingUser);
                if (DBG_LOC) {
                    log("checkIfCallerIsSelfOrForegoundUser: foregroundUser=" + foregroundUser
                            + " callingUser=" + callingUser + " ok=" + ok);
                }
            } catch (Exception ex) {
                if (DBG_LOC) loge("checkIfCallerIsSelfOrForegoundUser: Exception ex=" + ex);
                ok = false;
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        } else {
            if (DBG_LOC) log("checkIfCallerIsSelfOrForegoundUser: is self");
            ok = true;
        }
        if (DBG_LOC) log("checkIfCallerIsSelfOrForegoundUser: ret=" + ok);
        return ok;
    }

    public int getCdmaSubscriptionActStatus(int subId) {
        int actStatus = 0;
        Phone p = getPhone(subId);
        if (p != null){
            if (DBG) {
                log("getCdmaSubscriptionActStatus, phone type " + p.getPhoneType());
            }
            if (p.getPhoneType() == PhoneConstants.PHONE_TYPE_CDMA) {
                actStatus = p.getCdmaSubscriptionActStatus();
            }
        } else {
            log("fail to getCdmaSubscriptionActStatus due to phone is null");
        }
        return actStatus;
    }

    /**
     * Check if can switch default subId.
     * This api is not capable with Google's setDefaultXXXSubId.
     * It is only capable with setDefaultSubIdForAll.
     * @return true if yes, or false in these cases:
     *         1) voice call is in progress
     *         2) airplane mode is on
     *         3) phone's radio is not available
     *         4) radio capability switch is ongoing
     * @hide
     */
    public boolean canSwitchDefaultSubId() {
        log("canSwitchDefaultSubId");
        // check if in call
        if (TelephonyManager.getDefault().getCallState() != TelephonyManager.CALL_STATE_IDLE) {
            log("In call, fail to set RAT for phones");
            return false;
        }
        int airplaneMode = Settings.Global.getInt(
                mApp.getContentResolver(),
                Settings.Global.AIRPLANE_MODE_ON, 0);
        if (airplaneMode > 0) {
            log("Airplane mode is on, fail to set RAT for phones");
            return false;
        }
        // check radio available
        Phone[] phones = PhoneFactory.getPhones();
        for (int i = 0; i < phones.length; i++) {
            if (!phones[i].isRadioAvailable()) {
                log("Phone" + i + " is not available");
                return false;
            }
            if (phones[i].isInEcm()) {
                log("Phone" + i + " is in ECBM");
                return false;
            }
        }
        if (isEccInProgress()) {
            log("ECC in progress");
            return false;
        }
        // check if still switching
        if (isCapabilitySwitching()) {
            log("Is still switching");
            return false;
        }
        if (mCallback != null) {
            log("Last switching is ongoing");
            return false;
        }
        return true;
    }

    /**
     * Set default subId for data, voice, sms.
     * @param type The switch type, indicates which setting type(
     *        {@link TelephonyManagerEx#DEFAULT_DATA}, {@link TelephonyManagerEx#DEFAULT_VOICE}
     *        or {@link TelephonyManagerEx#DEFAULT_SMS}) triggers this synchronous change
     * @param subId The default subId
     * @param callback The callback for notifying application the result
     * @return true if succeed, or return false.
     * @hide
     */
    public boolean setDefaultSubIdForAll(int type, int subId,
            ISetDefaultSubResultCallback callback) {
        log("setDefaultSubIdForAll, type:" + type + ", subId:" + subId);
        if (!canSwitchDefaultSubId()) {
            log("Can't switch now");
            return false;
        }
        mCallback = callback;
        boolean needSwitchData = false;
        boolean needSwitchVoice = false;
        boolean needSwitchSms = false;
        int voiceSubId = getDefaultVoiceSubId();
        int smsSubId = SubscriptionManager.getDefaultSmsSubscriptionId();
        int dataSubId = SubscriptionManager.getDefaultDataSubscriptionId();
        switch (type) {
            case TelephonyManagerEx.DEFAULT_DATA:
                if (dataSubId == subId) {
                    break;
                }
                if (SubscriptionManager.isValidSubscriptionId(voiceSubId)) {
                    needSwitchVoice = true;
                }
                if (SubscriptionManager.isValidSubscriptionId(smsSubId)) {
                    needSwitchSms = true;
                }
                if (SubscriptionManager.isValidSubscriptionId(subId)) {
                    needSwitchData = true;
                }
                break;
            case TelephonyManagerEx.DEFAULT_VOICE:
                if (voiceSubId == subId) {
                    break;
                }
                if (SubscriptionManager.isValidSubscriptionId(smsSubId)
                        && SubscriptionManager.isValidSubscriptionId(subId)) {
                    needSwitchSms = true;
                }
                if (SubscriptionManager.isValidSubscriptionId(subId)) {
                    needSwitchData = true;
                }
                needSwitchVoice = true;
                break;
            case TelephonyManagerEx.DEFAULT_SMS:
                if (smsSubId == subId) {
                    break;
                }
                if (SubscriptionManager.isValidSubscriptionId(voiceSubId)
                        && SubscriptionManager.isValidSubscriptionId(subId)) {
                    needSwitchVoice = true;
                }
                if (SubscriptionManager.isValidSubscriptionId(subId)) {
                    needSwitchData = true;
                }
                needSwitchSms = true;
                break;
            default:
                break;
        }
        log("setDefaultSubIdForAll, voiceSubId:" + voiceSubId + ", smsSubId:" + smsSubId
                + ", dataSubId:" + dataSubId + ", needSwitchData:" + needSwitchData
                + ", needSwitchVoice:" + needSwitchVoice + ", needSwitchSms:" + needSwitchSms);
        // 1. Set default voice
        if (needSwitchVoice) {
            ITelecomService teleSvc = ITelecomService.Stub.asInterface(
                    ServiceManager.getService(Context.TELECOM_SERVICE));
            if (teleSvc != null) {
                PhoneAccountHandle phoneAccountHandle =
                        subscriptionIdToPhoneAccountHandle(subId);
                log("Phone account for default voice:" + phoneAccountHandle);
                try {
                    teleSvc.setUserSelectedOutgoingPhoneAccount(phoneAccountHandle);
                } catch (RemoteException e) {
                    log("Failed to set default voice, error:" + e);
                    if (mCallback != null) {
                        try {
                            mCallback.onComplete(false);
                        } catch (RemoteException ex) {
                            log("onComplete fail, exception:" + ex);
                        }
                        mCallback = null;
                    }
                    return false;
                }
            } else {
                log("Can't get TELECOM_SERVICE");
                if (mCallback != null) {
                    try {
                        mCallback.onComplete(false);
                    } catch (RemoteException ex) {
                        log("onComplete fail, exception:" + ex);
                    }
                    mCallback = null;
                }
                return false;
            }
        }

        // 2. Set defautl sms
        if (needSwitchSms) {
            SubscriptionController.getInstance().setDefaultSmsSubId(subId);
        }

        // 3. Set default data
        if (needSwitchData) {
            if (SubscriptionManager.isValidSubscriptionId(subId)
                    // VSIM feature will try to set a default data sub to non SIM card
                    // inserted slot.
                    || (SystemProperties.getInt("ro.mtk_external_sim_support", 0) == 1)) {
                Phone[] phones = PhoneFactory.getPhones();
                int len = phones.length;
                log("[setDefaultSubIdForAll] num phones=" + len);
                ProxyController proxyController = ProxyController.getInstance();
                RadioAccessFamily[] rafs = new RadioAccessFamily[len];
                boolean atLeastOneMatch = false;
                for (int phoneId = 0; phoneId < len; phoneId++) {
                    Phone phone = phones[phoneId];
                    int raf;
                    int id = phone.getSubId();
                    if (id == subId) {
                        // TODO Handle the general case of N modems and M subscriptions.
                        raf = proxyController.getMaxRafSupported();
                        atLeastOneMatch = true;
                    } else {
                        // TODO Handle the general case of N modems and M subscriptions.
                        raf = proxyController.getMinRafSupported();
                    }
                    log("[setDefaultSubIdForAll] phoneId:" + phoneId
                            + ", subId:" + id + ", RAF:" + raf);
                    rafs[phoneId] = new RadioAccessFamily(phoneId, raf);
                }
                if (atLeastOneMatch) {
                    try {
                        if (!ProxyController.getInstance().setRadioCapability(rafs)) {
                            log("[setDefaultSubIdForAll] setRadioCapability fail");
                            if (mCallback != null) {
                                try {
                                    mCallback.onComplete(false);
                                } catch (RemoteException ex) {
                                    log("onComplete fail, exception:" + ex);
                                }
                                mCallback = null;
                            }
                            return false;
                        }
                    } catch (RuntimeException e) {
                        log("[setDefaultSubIdForAll] setRadioCapability: Runtime Exception");
                        e.printStackTrace();
                        if (mCallback != null) {
                            try {
                                mCallback.onComplete(false);
                            } catch (RemoteException ex) {
                                log("onComplete fail, exception:" + ex);
                            }
                            mCallback = null;
                        }
                        return false;
                    }
                } else {
                    log("[setDefaultSubIdForAll] no valid subId's found - not updating.");
                }
            }
            SubscriptionController.getInstance().setDefaultDataSubIdWithoutCapabilitySwitch(subId);
        }
        if (!needSwitchData && mCallback != null) {
            try {
                mCallback.onComplete(true);
            } catch (RemoteException e) {
                log("onComplete fail, exception:" + e);
            }
            mCallback = null;
        }
        return true;
    }

    private PhoneAccountHandle subscriptionIdToPhoneAccountHandle(int subId) {
        final TelecomManager telecomManager = TelecomManager.from(mApp);
        final TelephonyManager telephonyManager = TelephonyManager.from(mApp);
        final Iterator<PhoneAccountHandle> phoneAccounts =
                telecomManager.getCallCapablePhoneAccounts().listIterator();
        while (phoneAccounts.hasNext()) {
            final PhoneAccountHandle phoneAccountHandle = phoneAccounts.next();
            final PhoneAccount phoneAccount = telecomManager.getPhoneAccount(phoneAccountHandle);
            if (subId == telephonyManager.getSubIdForPhoneAccount(phoneAccount)) {
                return phoneAccountHandle;
            }
        }
        return null;
    }

    private int getDefaultVoiceSubId() {
        int subId = SubscriptionManager.INVALID_SUBSCRIPTION_ID;
        final TelecomManager telecomManager = TelecomManager.from(mApp);
        PhoneAccountHandle handle =
                telecomManager.getUserSelectedOutgoingPhoneAccount();
        if (handle != null) {
            final PhoneAccount phoneAccount = telecomManager.getPhoneAccount(handle);
            subId = TelephonyManager.from(mApp).getSubIdForPhoneAccount(phoneAccount);
        } else {
            log("getDefaultVoiceSubId, handle is null");
        }
        return subId;
    }

    public void setEccInProgress(boolean state) {
        mIsEccInProgress = state;
        log("setEccInProgress, mIsEccInProgress:" + mIsEccInProgress);
    }

    public boolean isEccInProgress() {
        log("isEccInProgress, mIsEccInProgress:" + mIsEccInProgress);
        return mIsEccInProgress;
    }

    /**
     * Get uim imsi by sub id.
     * @param subId subscriber id
     * @return uim imsi
     * @hide
     */
    public String getUimImsiBySubId(int subId) {
        mApp.enforceCallingOrSelfPermission(android.Manifest.permission.READ_PHONE_STATE, null);

        int phoneId = SubscriptionManager.getPhoneId(subId);
        if (phoneId < 0 || phoneId >= PROPERTY_UIM_SUBSCRIBER_ID.length) {
            log("getUimImsiBySubId:invalid phoneId " + phoneId);
            return null;
        }

        return SystemProperties.get(PROPERTY_UIM_SUBSCRIBER_ID[phoneId], "");
    }

    /**
     * Get IMS registration state by given sub-id.
     * @param subId The subId for query
     * @return true if IMS is registered, or false
     * @hide
     */
    public boolean isImsRegistered(int subId) {
        Phone p = getPhone(subId);
        if (p != null){
            if (DBG) {
                log("isImsRegistered(" + subId + ")=" + p.isImsRegistered());
            }
            return p.isImsRegistered();
        }
        return false;
    }

    /**
     * Get Volte registration state by given sub-id.
     * @param subId The subId for query
     * @return true if volte is registered, or false
     * @hide
     */
    public boolean isVolteEnabled(int subId) {
        Phone p = getPhone(subId);
        if (p != null){
            if (DBG) {
                log("isVolteEnabled=(" + subId + ")=" + p.isVolteEnabled());
            }
            return p.isVolteEnabled();
        }
        return false;
    }

    /**
     * Get WFC registration state by given sub-id.
     * @param subId The subId for query
     * @return true if wfc is registered, or false
     * @hide
     */
    public boolean isWifiCallingEnabled(int subId) {
        Phone p = getPhone(subId);
        if (p != null){
            if (DBG) {
                log("isWifiCallingEnabled(" + subId + ")=" + p.isWifiCallingEnabled());
            }
            return p.isWifiCallingEnabled();
        }
        return false;
    }

    /**
     * Get icc app family by slot id.
     * @param slotId slot id
     * @return the family type
     * @hide
     */
    public int getIccAppFamily(int slotId) {
        int iccType = TelephonyManagerEx.APP_FAM_NONE;
        int phoneCount = TelephonyManager.getDefault().getSimCount();
        if (slotId < 0 || slotId >= phoneCount) {
            log("getIccAppFamily, invalid slotId:" + slotId);
            return iccType;
        }

        String uiccType = SystemProperties.get(PROPERTY_RIL_FULL_UICC_TYPE[slotId]);
        String appType[] = uiccType.split(",");
        int fullType = TelephonyManagerEx.CARD_TYPE_NONE;
        for (int i = 0; i < appType.length; i++) {
            if ("USIM".equals(appType[i])) {
                fullType = fullType | TelephonyManagerEx.CARD_TYPE_USIM;
            } else if ("SIM".equals(appType[i])) {
                fullType = fullType | TelephonyManagerEx.CARD_TYPE_SIM;
            } else if ("CSIM".equals(appType[i])) {
                fullType = fullType | TelephonyManagerEx.CARD_TYPE_CSIM;
            } else if ("RUIM".equals(appType[i])) {
                fullType = fullType | TelephonyManagerEx.CARD_TYPE_RUIM;
            }
        }

        if (fullType == TelephonyManagerEx.CARD_TYPE_NONE) {
            iccType = TelephonyManagerEx.APP_FAM_NONE;
        } else if ((fullType & TelephonyManagerEx.CARD_TYPE_CSIM) != 0
                && (fullType & TelephonyManagerEx.CARD_TYPE_USIM) != 0) {
            iccType = TelephonyManagerEx.APP_FAM_3GPP2 | TelephonyManagerEx.APP_FAM_3GPP;
        } else if ((fullType & TelephonyManagerEx.CARD_TYPE_CSIM) != 0
                || (fullType & TelephonyManagerEx.CARD_TYPE_RUIM) != 0) {
            iccType = TelephonyManagerEx.APP_FAM_3GPP2;
        } else {
            iccType = TelephonyManagerEx.APP_FAM_3GPP;

            // Uim dual mode sim, may switch to SIM type for use
            if (fullType == TelephonyManagerEx.CARD_TYPE_SIM) {
                String uimDualMode = SystemProperties.get(PROPERTY_RIL_CT3G[slotId]);
                if ("1".equals(uimDualMode)) {
                    iccType = TelephonyManagerEx.APP_FAM_3GPP2;
                }
            }
        }
        logD("getIccAppFamily, " + "uiccType[" + slotId + "] = "
                    + uiccType + "fullType = " + fullType + " iccType = " + iccType);
        return iccType;
    }

    // Provide getMeid(int slotId) for APP. in case, there is no CDMAPhone.
    private void deviceIdHandlerInit() {
        if (isC2kSupport()) {
            sPhones = PhoneFactory.getPhones();
            int phoneCount = sPhones.length;
            sCi = new CommandsInterface[phoneCount];
            mMeids = new String[phoneCount];
            mDeviceIdQueryHandler = new DeviceIdQueryHandler[phoneCount];
            log("[MEID]deviceIdHandlerInit() length = " + sPhones.length);
            for (int i = 0; i < phoneCount; i++) {
                sCi[i] = sPhones[i].mCi;
                mDeviceIdQueryHandler[i] = new DeviceIdQueryHandler(i);
                sCi[i].registerForAvailable(mDeviceIdQueryHandler[i],
                        EVENT_RADIO_AVAILABLE, null);
            }
        } else {
            log("[MEID]DeviceIdQueryHandler don't init()");
        }
    }

    private boolean isC2kSupport() {
        String isC2kSupport = SystemProperties.get("ro.boot.opt_c2k_support", "0");
        log("[MEID]enableDeviceInformation isC2kSupport=" + isC2kSupport);
        return !"0".equals(isC2kSupport);
    }

    class DeviceIdQueryHandler extends Handler {
        CommandsInterface mCi;
        int mSlotId = -1;

        public DeviceIdQueryHandler(int i) {
            mCi = sCi[i];
            mSlotId = i;
        }

        @Override
        public void handleMessage(Message msg) {
            log("[MEID]DeviceIdQueryHandler handleMessage msg = " + msg.what);
            switch (msg.what) {
                case EVENT_RADIO_AVAILABLE:
                    mCi.getDeviceIdentity(obtainMessage(EVENT_GET_DEVICE_IDENTITY_DONE));
                    break;
                case EVENT_GET_DEVICE_IDENTITY_DONE:
                    AsyncResult ar = (AsyncResult) msg.obj;
                    if (ar.exception != null) {
                        log("[MEID]Null mMeid!! ar.exception = " + ar.exception.toString());
                        break;
                    }
                    String[] respId = (String[]) ar.result;
                    mMeids[mSlotId] = respId[3];
                    if (mMeids[mSlotId] != null) {
                        mMeids[mSlotId] = mMeids[mSlotId].toUpperCase();
                    }
                    log("[MEID]mMeid: " + mMeids[mSlotId] + "; mSlotId=" + mSlotId);
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * Get imei by slotId.
     * @param slotId
     * @return
     * @hide
     */
    public String getImei(int slotId) {
        Phone phone = PhoneFactory.getPhone(slotId);
        if (phone != null) {
            return phone.getImei();
        } else {
            return null;
        }
    }

    /**
     * Get MEID by slotId.
     * @param slotId
     * @return
     * @hide
     */
    public String getMeid(int slotId) {
        if (mMeids != null && slotId < mMeids.length) {
            return mMeids[slotId];
        } else {
            Phone phone = PhoneFactory.getPhone(slotId);
            String meid = phone.getMeid();
            if("0".equals(meid)){
                return null;
            } else {
                return meid;
            }
        }
    }

    /**
     * Check wheter phone type is GSM.
     * @param slotId
     * @return
     * @hide
     */
    public boolean isPhoneTypeGsm(int slotId) {
        Phone phone = PhoneFactory.getPhone(slotId);
        GsmCdmaPhone mPhone = null;
        if (phone instanceof GsmCdmaPhone) {
            mPhone = (GsmCdmaPhone) phone;
            return mPhone.isPhoneTypeGsm();
        } else {
            return false;
        }
    }

    /**
     * Check whether phone type is CDMA.
     * @param slotId
     * @return
     * @hide
     */
    public boolean isPhoneTypeCdma(int slotId) {
        Phone phone = PhoneFactory.getPhone(slotId);
        GsmCdmaPhone mPhone = null;
        if (phone instanceof GsmCdmaPhone) {
            mPhone = (GsmCdmaPhone) phone;
            return mPhone.isPhoneTypeCdma();
        } else {
            return false;
        }
    }

    /**
     * Check whether phone type is CDMA+LTE.
     * @param slotId
     * @return
     * @hide
     */
    public boolean isPhoneTypeCdmaLte(int slotId) {
        Phone phone = PhoneFactory.getPhone(slotId);
        GsmCdmaPhone mPhone = null;
        if (phone instanceof GsmCdmaPhone) {
            mPhone = (GsmCdmaPhone) phone;
            return mPhone.isPhoneTypeCdmaLte();
        } else {
            return false;
        }
    }

    /**
     * Check whether MCC MNC marked as roaming
     * @param slotId
     * @return
     * @hide
     */
    public boolean isMccMncMarkedAsRoaming(int slotId, String mccMnc) {
        Phone phone = PhoneFactory.getPhone(slotId);
        GsmCdmaPhone mPhone = null;
        if (phone instanceof GsmCdmaPhone) {
            mPhone = (GsmCdmaPhone) phone;
            return mPhone.isMccMncMarkedAsRoaming(mccMnc);
        } else {
            return false;
        }
    }

    /**
     * Check whether MCC MNC marked as non-roaming
     * @param slotId
     * @return
     * @hide
     */
    public boolean isMccMncMarkedAsNonRoaming(int slotId, String mccMnc) {
        Phone phone = PhoneFactory.getPhone(slotId);
        GsmCdmaPhone mPhone = null;
        if (phone instanceof GsmCdmaPhone) {
            mPhone = (GsmCdmaPhone) phone;
            return mPhone.isMccMncMarkedAsNonRoaming(mccMnc);
        } else {
            return false;
        }
    }

    public void enablePseudoBSMonitor(int slotId, boolean reportOn, int reportRateInSeconds) {
        if (DBG) {
            log("enablePseudoBSMonitor slotId = " + slotId + ", reportOn = " + reportOn +
                    ", reportRateInSeconds = " + reportRateInSeconds);
        }
        if (reportOn && (reportRateInSeconds < 1 || reportRateInSeconds > APC_TIMER_MAX_VALUE)) {
            return;
        }
        int subId = getSubIdBySlot(slotId);
        int phoneId = SubscriptionManager.getPhoneId(subId);
        Phone phone = PhoneFactory.getPhone(phoneId);
        if (phone != null) {
            if (phone.getPhoneType() == PhoneConstants.PHONE_TYPE_GSM) {
                CommandsInterface ci = phone.mCi;
                ci.enablePseudoBSMonitor(reportOn, reportRateInSeconds, null);
            }
        }
    }

    public void disablePseudoBSMonitor(int slotId) {
        if (DBG) log("disablePseudoBSMonitor slotId = " + slotId);
        int subId = getSubIdBySlot(slotId);
        int phoneId = SubscriptionManager.getPhoneId(subId);
        Phone phone = PhoneFactory.getPhone(phoneId);
        if (phone != null) {
            if (phone.getPhoneType() == PhoneConstants.PHONE_TYPE_GSM) {
                CommandsInterface ci = phone.mCi;
                ci.disablePseudoBSMonitor(null);
            }
        }
    }

    private List<PseudoBSRecord> parsePseudoRecords(int[] msgs) {
        int size = msgs[0];
        if (DBG) log("parsePseudoRecords: size=" + size);
        List<PseudoBSRecord> list = new ArrayList<PseudoBSRecord>();
        int i;
        for (i = 0; i < size; i++) {
            PseudoBSRecord record = new PseudoBSRecord(
                msgs[i*6+1], msgs[i*6+2], msgs[i*6+3],
                msgs[i*6+4], msgs[i*6+5], msgs[i*6+6]);
            list.add(record);
        }
        return list;
    }

    private class PseudoBsInfoResult {
        List<PseudoBSRecord> list = null;
        Object lockObj = new Object();
    }

    public List<PseudoBSRecord> queryPseudoBSRecords(int slotId) {
        if (DBG) log("queryPseudoBSRecords slotId = " + slotId);
        int subId = getSubIdBySlot(slotId);
        int phoneId = SubscriptionManager.getPhoneId(subId);
        Phone phone = PhoneFactory.getPhone(phoneId);
        if (phone != null) {
            if (phone.getPhoneType() == PhoneConstants.PHONE_TYPE_GSM) {
                CommandsInterface ci = phone.mCi;
                PseudoBsInfoResult result = new PseudoBsInfoResult();
                //wait query return and return
                synchronized(result.lockObj) {
                    result.list = null;
                    ci.queryPseudoBSRecords(mMainThreadHandler.obtainMessage(
                        EVENT_GET_PSEUDO_BS_RECORDS, result));
                    try {
                        result.lockObj.wait(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                synchronized(result.lockObj) {
                    if (result.list != null) {
                        if (DBG) log("queryPseudoBSRecords return: list.size=" + result.list.size()
                                + " list=" + result.list);
                        return result.list;
                    } else {
                        if (DBG) log("queryPseudoBSRecords return: list=null");
                        return null;
                    }
                }
            } else {
                if (DBG) log("phone is not GsmPhone, ignore");
                return null;
            }
        } else {
            return null;
        }
    }

    /**
    * Set APC feature mode
    * @param slotId.
    * @param apcMode 0: APC disable, 1: APC enable mode I, 2: APC feature enable mode II
    * @param reportOn true for URC report on and false for URC report off.
    * @param reportRateInSeconds report interval.
    */
    public void setAntiPseudoCellMode(int slotId, int apcMode, boolean reportOn,
                        int reportRateInSeconds) {
        if (DBG) {
            log("setAntiPseudoCellMode slotId = " + slotId + ", apcMode = " + apcMode +
                ", reportOn = " + reportOn + ", reportRateInSeconds = " + reportRateInSeconds);
        }
        if (apcMode != 0 && apcMode != 1 && apcMode != 2) {
            return;
        }
        if (reportOn && (reportRateInSeconds < 1 || reportRateInSeconds > APC_TIMER_MAX_VALUE)) {
            return;
        }
        int subId = getSubIdBySlot(slotId);
        int phoneId = SubscriptionManager.getPhoneId(subId);
        Phone phone = PhoneFactory.getPhone(phoneId);
        if (phone != null) {
            if (phone.getPhoneType() == PhoneConstants.PHONE_TYPE_GSM) {
                CommandsInterface ci = phone.mCi;
                ci.enablePseudoBSMonitor(apcMode, reportOn, reportRateInSeconds, null);
            }
        }
    }

    private class RxTestObject {
        int result[] = null;
        Object lockObj = new Object();
    }

    public int[] setRxTestConfig(int phoneId, int config) {
        Phone phone = PhoneFactory.getPhone(phoneId);
        if (phone != null) {
            CommandsInterface ci = phone.mCi;
            if (ci != null) {
                RxTestObject RxTest = new RxTestObject();
                synchronized(RxTest.lockObj) {
                    ci.setRxTestConfig(config, mMainThreadHandler.obtainMessage(
                            EVENT_SET_RX_TEST_CONFIG, RxTest));
                    try {
                        RxTest.lockObj.wait(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                synchronized(RxTest.lockObj) {
                    if (RxTest.result != null) {
                        if (DBG) log("setRxTestConfig return: " + RxTest.result);
                        return RxTest.result;
                    } else {
                        if (DBG) log("setRxTestConfig return: null");
                        return null;
                    }
                }
            } else {
                if (DBG) log("setRxTestConfig phone.mCi = null");
            }
        } else {
            if (DBG) log("setRxTestConfig phone = null");
        }
        return null;
    }

    public int[] getRxTestResult(int phoneId) {
        Phone phone = PhoneFactory.getPhone(phoneId);
        if (phone != null) {
            CommandsInterface ci = phone.mCi;
            if (ci != null) {
                RxTestObject RxTest = new RxTestObject();
                synchronized(RxTest.lockObj) {
                    ci.getRxTestResult(mMainThreadHandler.obtainMessage(
                            EVENT_GET_RX_TEST_RESULT, RxTest));
                    try {
                        RxTest.lockObj.wait(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                synchronized(RxTest.lockObj) {
                    if (RxTest.result != null) {
                        if (DBG) log("getRxTestResult return: " + RxTest.result);
                        return RxTest.result;
                    } else {
                        if (DBG) log("getRxTestResult return: null");
                        return null;
                    }
                }
            } else {
                if (DBG) log("getRxTestResult phone.mCi = null");
            }
        } else {
            if (DBG) log("getRxTestResult phone = null");
        }
        return null;
    }

    /**
     * Get full IccId by slotId.
     * @param slotId int
     * @return Iccid
     * @hide
     */
    public String getFullIccSerialNumber(int slotId) {
        mApp.enforceCallingOrSelfPermission(
                android.Manifest.permission.READ_PHONE_STATE,
                null);
        Phone phone = getPhoneUsingPhoneId(slotId);
        return phone.getFullIccSerialNumber();
    }

    /**
     * Enable or disable Telephony and connectivity debug log
     * @param enable true: enable log, false: disable log
     */
    public void setTelLog(boolean enable) {
        if (DBG) log("setTelLog enable = " + enable);
        if (SystemProperties.getInt("persist.log.tag.tel_log_ctrl", 0) != 1) {
            return;
        }
        if (enable) {
            for (String telLogTag : PROPERTY_M_LOG_TAG_COMMON_RIL) {
                SystemProperties.set(telLogTag, "D");
            }
            for (String telLogTag : PROPERTY_M_LOG_TAG) {
                SystemProperties.set(telLogTag, "D");
            }
            for (String telLogTag : PROPERTY_V_LOG_TAG) {
                SystemProperties.set(telLogTag, "V");
            }
        } else {
            // Userdebug/user load: allow log level I
            for (String telLogTag : PROPERTY_M_LOG_TAG_COMMON_RIL) {
                SystemProperties.set(telLogTag, "I");
            }
            if (!SystemProperties.get("ro.build.type").equals("eng")) {
                for (String telLogTag : PROPERTY_M_LOG_TAG) {
                    SystemProperties.set(telLogTag, "I");
                }
                for (String telLogTag : PROPERTY_V_LOG_TAG) {
                    SystemProperties.set(telLogTag, "I");
                }
            }
        }
    }

    /*
    * Enable/ disable game modem enhancement
    * param onOff: turn on enhancement if true.
    */
    public void requestGameModeEnhancement(boolean onOff) {
        int phoneId = RadioCapabilitySwitchUtil.getMainCapabilityPhoneId();
        Phone phone = PhoneFactory.getPhone(phoneId);
        String mode = onOff ? "1" : "0";
        if (phone != null) {
            phone.invokeOemRilRequestStrings(new String[]{"AT+EMRO=1,\"lte_game_ho_enhancement\","
                    + mode, ""}, null);
        } else {
            log("requestGameModeEnhancement: phone is  NULL");
        }
    }

    @Override
    public long getVtDataUsage(int subId) {
        mApp.enforceCallingOrSelfPermission(android.Manifest.permission.READ_NETWORK_USAGE_HISTORY,
                null);

        Phone phone = getPhone(subId);
        long vtDataUsage = 0;

        if (phone != null) {
            vtDataUsage = phone.getVtDataUsage();
        }
        log("getVtDataUsage subId: " + subId + " phone: " + phone + " usage: " + vtDataUsage);
        return vtDataUsage;
    }

    @Override
    public List<CellInfo> getAllCellInfo(int slotId, String callingPackage) {
        enforceFineOrCoarseLocationPermission("getAllCellInfo");

        // OP_COARSE_LOCATION controls both fine and coarse location.
        if (mAppOps.noteOp(AppOpsManager.OP_COARSE_LOCATION, Binder.getCallingUid(),
                callingPackage) != AppOpsManager.MODE_ALLOWED) {
            return null;
        }

        final Phone phone = PhoneFactory.getPhone(slotId);
        if (phone == null) {
            return null;
        }

        if (checkIfCallerIsSelfOrForegroundUser()
                || checkCallerInteractAcrossUsersFull(phone.getContext())) {
            if (DBG_LOC) {
                log("getAllCellInfo: is active user");
            }
            return phone.getAllCellInfo();
        } else {
            if (DBG_LOC) {
                log("getAllCellInfo: suppress non-active user");
            }
            return null;
        }
    }

    private final boolean checkCallerInteractAcrossUsersFull(Context context) {
        return context.checkCallingOrSelfPermission(
                android.Manifest.permission.INTERACT_ACROSS_USERS_FULL)
                == PackageManager.PERMISSION_GRANTED;
    }
}
