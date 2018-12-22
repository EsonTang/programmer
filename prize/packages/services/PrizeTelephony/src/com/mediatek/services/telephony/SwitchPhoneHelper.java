package com.mediatek.services.telephony;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.telephony.ServiceState;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.android.ims.ImsManager;
import com.android.internal.telephony.IccCardConstants;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.PhoneFactory;
import com.android.internal.telephony.ProxyController;
import com.android.internal.telephony.TelephonyIntents;

import com.android.services.telephony.TelephonyConnectionServiceUtil;

import com.mediatek.internal.telephony.RadioCapabilitySwitchUtil;
import com.mediatek.telephony.TelephonyManagerEx;

import static com.android.internal.telephony.PhoneConstants.PHONE_TYPE_CDMA;
import static com.android.internal.telephony.PhoneConstants.PHONE_TYPE_GSM;
import static com.android.internal.telephony.PhoneConstants.PHONE_TYPE_NONE;

/**
 * Helper class to switch phone for emergency call.
 */
public class SwitchPhoneHelper {
    private static final String TAG = "ECCSwitchPhone";
    /**
     * Receives the result of the SwitchPhoneHelper's attempt to switch phone.
     */
    public interface Callback {
        void onComplete(boolean success);
    }

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_START_SWITCH_PHONE:
                    startSwitchPhoneInternal();
                    break;
                case MSG_SWITCH_PHONE_TIMEOUT:
                    logd("MSG_SWITCH_PHONE_TIMEOUT");
                    finish();
                    break;
                case MSG_MODE_SWITCH_RESULT:
                    logd("MSG_MODE_SWITCH_RESULT");
                    AsyncResult ar = (AsyncResult) msg.obj;
                    if (ar.exception != null) {
                        logd("Fail to switch now, mFailRetryCount:" + mFailRetryCount);
                        if (mFailRetryCount++ < MAX_FAIL_RETRY_COUNT) {
                            mHandler.sendEmptyMessageDelayed(MSG_START_SWITCH_PHONE,
                                    RETRY_SWITCH_PHONE_MILLIS);
                        } else {
                            finish();
                        }
                    } else {
                        logd("Start switch phone!");
                        startSwitchPhoneTimer();
                        if (!mRegisterSwitchPhoneReceiver) {
                            IntentFilter intentFilter = new IntentFilter(
                                    TelephonyIntents.ACTION_RADIO_TECHNOLOGY_CHANGED);
                            intentFilter.addAction(TelephonyIntents.ACTION_SUBINFO_RECORD_UPDATED);
                            mSwitchPhoneReceiver = new SwitchPhoneReceiver();
                            mContext.registerReceiver(mSwitchPhoneReceiver, intentFilter);
                            mRegisterSwitchPhoneReceiver = true;
                        }
                    }
                    break;
                case MSG_WAIT_FOR_INTENT_TIMEOUT:
                    logd("MSG_WAIT_FOR_INTENT_TIMEOUT");
                    if (needToSwitchPhone()) {
                        startSwitchPhone(mCallback);
                    } else {
                        finish();
                    }
                    break;
                case MSG_START_TURN_OFF_VOLTE:
                    logd("Start turn off VoLTE!");
                    exitCtLteOnlyMode();
                    startTurnOffVolteTimer();
                    break;
                case MSG_TURN_OFF_VOLTE_TIMEOUT:
                    logd("MSG_TURN_OFF_VOLTE_TIMEOUT");
                    finish();
                    break;
                default:
                    logd("handleMessage: unexpected message:" + msg.what);
                    break;
            }
        }
    };

    // Handler message codes; see handleMessage()
    private static final int MSG_START_SWITCH_PHONE = 1;
    private static final int MSG_SWITCH_PHONE_TIMEOUT = 2;
    private static final int MSG_MODE_SWITCH_RESULT = 3;
    private static final int MSG_WAIT_FOR_INTENT_TIMEOUT = 4;
    private static final int MSG_START_TURN_OFF_VOLTE = 5;
    private static final int MSG_TURN_OFF_VOLTE_TIMEOUT = 6;

    private static final boolean MTK_C2K_SUPPORT
            = "1".equals(SystemProperties.get("ro.boot.opt_c2k_support"));
    private static final boolean MTK_FLIGHTMODE_POWEROFF_MD_SUPPORT
            = "1".equals(SystemProperties.get("ro.mtk_flight_mode_power_off_md"));
    private static final boolean MTK_CT_VOLTE_SUPPORT
            = "1".equals(SystemProperties.get("persist.mtk_ct_volte_support", "0"));
    private static final int PROJECT_SIM_NUM = TelephonyManager.getDefault().getPhoneCount();
    private static final int RETRY_SWITCH_PHONE_MILLIS = 2000;
    private static final int SWITCH_PHONE_TIMEOUT_MILLIS = 10000;
    private static final int WAIT_FOR_FINISH_TIMEOUT_MILLIS = 30000;
    private static final int TURN_OFF_VOLTE_TIMEOUT_MILLIS = 5000;
    private static final int MODE_GSM = 1;
    private static final int MODE_C2K = 4;
    private static final int MAX_FAIL_RETRY_COUNT = 10;

    private boolean mRegisterSwitchPhoneReceiver = false;
    private boolean mRegisterSimStateReceiver = false;
    private boolean mRegisterSimSwitchReceiver = false;
    private boolean mSkipFirstIntent = false;
    private int mTargetPhoneType = PHONE_TYPE_NONE;
    private int mFailRetryCount = 0;
    private int[] mSimStateCollected = new int[PROJECT_SIM_NUM];
    private int[] mRadioTechCollected = new int[PROJECT_SIM_NUM];
    private String[] mSimState = new String[PROJECT_SIM_NUM];
    private String mNumber;
    private TelephonyManager mTm;
    private SwitchPhoneReceiver mSwitchPhoneReceiver;
    private SimStateReceiver mSimStateReceiver;
    private SimSwitchReceiver mSimSwitchReceiver;
    private final Context mContext;
    private Callback mCallback;  // The callback to notify upon completion.
    private Phone mPhone;  // The phone which will be used to switch.
    private EmergencyNumberUtils mEccNumberUtils;
    private AirplaneModeObserver mAirplaneModeObserver;
    private TelephonyConnectionServiceUtil mTelephonyConnectionServiceUtil =
            TelephonyConnectionServiceUtil.getInstance();

    public SwitchPhoneHelper(Context context, String number) {
        logd("SwitchPhoneHelper constructor");
        mContext = context;
        mNumber = number;
        mEccNumberUtils = new EmergencyNumberUtils(number);
        mAirplaneModeObserver = new AirplaneModeObserver(mHandler);
        mTm = TelephonyManager.getDefault();
    }

    public boolean needToPrepareForDial() {
        if (!MTK_C2K_SUPPORT) {
            return false;
        }
        if (mayTriggerSwitchPhone()) {
            return true;
        }
        if (ProxyController.getInstance().isCapabilitySwitching()) {
            logd("Capability switching");
            return true;
        }
        if (needToSwitchPhone()) {
            return true;
        }
        return false;
    }

    public void prepareForDial(Callback callback) {
        if (mayTriggerSwitchPhone()) {
            startExitAirplaneModeSequence(callback);
        } else if (ProxyController.getInstance().isCapabilitySwitching()) {
            waitForCapabilitySwitchFinish(callback);
        } else if (needToSwitchPhone()) {
            startSwitchPhone(callback);
        }
    }

    private boolean mayTriggerSwitchPhone() {
        boolean inAirplaneMode = Settings.Global.getInt(mContext.getContentResolver(),
                Settings.Global.AIRPLANE_MODE_ON, 0) > 0;
        return MTK_FLIGHTMODE_POWEROFF_MD_SUPPORT && inAirplaneMode;
    }

    private void startSwitchPhone(Callback callback) {
        mPhone = null;
        unregisterReceiver();
        mCallback = callback;
        int mainPhoneId = RadioCapabilitySwitchUtil.getMainCapabilityPhoneId();
        logd("startSwitchPhone, mainPhoneId:" + mainPhoneId);
        if (mTelephonyConnectionServiceUtil.hasPerformEccRetry()) {
            int previousPhoneType = mTelephonyConnectionServiceUtil.getEccPhoneType();
            logd("startSwitchPhone, previousPhoneType:" + previousPhoneType);
            if (previousPhoneType == PHONE_TYPE_CDMA) {
                mTargetPhoneType = PHONE_TYPE_GSM;
                if (!mTm.hasIccCard(mainPhoneId)) {
                    mPhone = PhoneFactory.getPhone(mainPhoneId);
                } else {
                    logd("main phone has card, can't switch!");
                }
            } else {
                mTargetPhoneType = PHONE_TYPE_CDMA;
                int cdmaSlot = SystemProperties.getInt("persist.radio.cdma_slot", -1);
                logd("startSwitchPhone, cdmaSlot:" + cdmaSlot);
                if (cdmaSlot != -1) {
                    if (!mTm.hasIccCard(cdmaSlot - 1)) {
                        // Select no SIM card and CDMA capability slot
                        mPhone = PhoneFactory.getPhone(cdmaSlot - 1);

                        if (MTK_CT_VOLTE_SUPPORT) {
                            // Reset the target phone type for GSM always/only/preferred number
                            if (mEccNumberUtils.isGsmAlwaysNumber()
                                || mEccNumberUtils.isGsmOnlyNumber()
                                || mEccNumberUtils.isGsmPreferredNumber()) {
                                mTargetPhoneType = PHONE_TYPE_GSM;
                            }
                        }
                    } else {
                        if (isInCtLteOnlyMode()) {
                            // Turn off VoLTE to exit LTE only mode
                            mPhone = PhoneFactory.getPhone(cdmaSlot - 1);
                            mTelephonyConnectionServiceUtil.setEccRetryPhoneId(mPhone.getPhoneId());
                            logd("startSwitchPhone, turn off VoLTE for phone" + mPhone.getPhoneId()
                                    + ", phoneType:" + mPhone.getPhoneType()
                                    + ", mTargetPhoneType:" + mTargetPhoneType);
                            mHandler.obtainMessage(MSG_START_TURN_OFF_VOLTE).sendToTarget();
                            return;
                        }

                        for (int i = 0; i < PROJECT_SIM_NUM; i++) {
                            if (!mTm.hasIccCard(i)) {
                                // Select no SIM card slot
                                mPhone = PhoneFactory.getPhone(i);
                            }
                        }
                    }
                } else {
                    for (int i = 0; i < PROJECT_SIM_NUM; i++) {
                        if (!mTm.hasIccCard(i)) {
                            // Select no SIM card slot
                            mPhone = PhoneFactory.getPhone(i);
                        }
                    }
                }
            }
            if (mPhone != null) {
                mTelephonyConnectionServiceUtil.setEccRetryPhoneId(mPhone.getPhoneId());
            }
        } else {
            if (Settings.Global.getInt(mContext.getContentResolver(),
                        Settings.Global.AIRPLANE_MODE_ON, 0) > 0) {
                Settings.Global.putInt(mContext.getContentResolver(),
                        Settings.Global.AIRPLANE_MODE_ON, 0);
                Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
                intent.putExtra("state", false);
                mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
            }
            if (mEccNumberUtils.isCdmaAlwaysNumber()
                    || mEccNumberUtils.isCdmaPreferredNumber()) {
                int cdmaSlot = SystemProperties.getInt("persist.radio.cdma_slot", -1);
                logd("startSwitchPhone, cdmaSlot:" + cdmaSlot);
                if (cdmaSlot != -1 && !mTm.hasIccCard(cdmaSlot - 1)) {
                    // Select no SIM card and CDMA capability slot
                    mPhone = PhoneFactory.getPhone(cdmaSlot - 1);
                } else {
                    for (int i = 0; i < PROJECT_SIM_NUM; i++) {
                        if (!mTm.hasIccCard(i)) {
                            // Select no SIM card slot
                            mPhone = PhoneFactory.getPhone(i);
                        }
                    }
                }
                mTargetPhoneType = PHONE_TYPE_CDMA;
            } else if (mEccNumberUtils.isGsmAlwaysNumber() || mEccNumberUtils.isGsmOnlyNumber()
                    || mEccNumberUtils.isGsmPreferredNumber()) {
                if (!mTm.hasIccCard(mainPhoneId)) {
                    mPhone = PhoneFactory.getPhone(mainPhoneId);
                } else {
                    logd("main phone has card, can't switch!");
                }
                mTargetPhoneType = PHONE_TYPE_GSM;
            }
        }
        if (mPhone == null) {
            logd("startSwitchPhone, no suitable phone selected to switch!");
            finish();
            return;
        }
        logd("startSwitchPhone with phone" + mPhone.getPhoneId() + ", phoneType:"
                + mPhone.getPhoneType() + ", mTargetPhoneType:" + mTargetPhoneType);
        mHandler.obtainMessage(MSG_START_SWITCH_PHONE).sendToTarget();
    }

    private void startSwitchPhoneInternal() {
        if (!mTm.hasIccCard(mPhone.getPhoneId())) {
            if (mTargetPhoneType == PHONE_TYPE_GSM) {
                mPhone.exitEmergencyCallbackMode();
            }
            mPhone.triggerModeSwitchByEcc(mTargetPhoneType ==
                    PHONE_TYPE_CDMA ? MODE_C2K : MODE_GSM,
                    mHandler.obtainMessage(MSG_MODE_SWITCH_RESULT));
        } else {
            logd("startSwitchPhoneInternal, no need to switch phone!");
            finish();
        }
    }

    private void finish() {
        onComplete(true);
        cleanup();
    }

    private void startSwitchPhoneTimer() {
        cancelSwitchPhoneTimer();
        mHandler.sendEmptyMessageDelayed(MSG_SWITCH_PHONE_TIMEOUT, WAIT_FOR_FINISH_TIMEOUT_MILLIS);
    }

    private void cancelSwitchPhoneTimer() {
        mHandler.removeMessages(MSG_SWITCH_PHONE_TIMEOUT);
        mHandler.removeMessages(MSG_START_SWITCH_PHONE);
    }

    public void onDestroy() {
        logd("onDestroy");
        mContext.getContentResolver().unregisterContentObserver(mAirplaneModeObserver);
        cleanup();
        if ("OP09".equals(SystemProperties.get("persist.operator.optr"))
                && "SEGDEFAULT".equals(SystemProperties.get("persist.operator.seg"))) {
            Phone phone = PhoneFactory.getPhone(0);
            logd("Phone0 type:" + phone.getPhoneType() + ", hascard:" + mTm.hasIccCard(0));
            if (phone.getPhoneType() != PHONE_TYPE_CDMA && !mTm.hasIccCard(0)) {
                phone.triggerModeSwitchByEcc(MODE_C2K, null);
            }
        }
    }

    private void startExitAirplaneModeSequence(Callback callback) {
        logd("startExitAirplaneModeSequence");
        cleanup();
        mCallback = callback;
        for (int i = 0; i < PROJECT_SIM_NUM; i++) {
            mSimStateCollected[i] = 0;
            mSimState[i] = null;
        }
        IntentFilter intentFilter = new IntentFilter(
                TelephonyIntents.ACTION_SIM_STATE_CHANGED);
        mSimStateReceiver = new SimStateReceiver();
        mContext.registerReceiver(mSimStateReceiver, intentFilter);
        mRegisterSimStateReceiver = true;
        Settings.Global.putInt(mContext.getContentResolver(),
                Settings.Global.AIRPLANE_MODE_ON, 0);
        Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        intent.putExtra("state", false);
        mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
        mHandler.sendEmptyMessageDelayed(MSG_WAIT_FOR_INTENT_TIMEOUT,
                WAIT_FOR_FINISH_TIMEOUT_MILLIS);
    }

    private boolean isAllSimReady() {
        for (int i = 0; i < PROJECT_SIM_NUM; i++) {
            if (mSimStateCollected[i] != 1 || mSimState[i] == null
                    || mSimState[i].equals(IccCardConstants.INTENT_VALUE_ICC_NOT_READY)) {
                return false;
            }
        }
        return true;
    }

    private boolean isRoaming() {
        TelephonyManagerEx tmEx = TelephonyManagerEx.getDefault();
        int cSlot = SystemProperties.getInt("persist.radio.cdma_slot", -1);
        if (cSlot != -1) {
            int appFamily = tmEx.getIccAppFamily(cSlot - 1);
            Phone phone = PhoneFactory.getPhone(cSlot - 1);
            logd("isRoaming, cdmaSlot:" + cSlot + ", appFamily:" + appFamily
                    + ", phonetype:" + (phone != null ? phone.getPhoneType() : PHONE_TYPE_NONE));
            if ((appFamily >= TelephonyManagerEx.APP_FAM_3GPP2 || tmEx.isCt3gDualMode(cSlot - 1))
                    && phone != null && phone.getPhoneType() == PHONE_TYPE_GSM) {
                logd("Card" + (cSlot - 1) + " is roaming");
                return true;
            }
        } else {
            for (Phone p : PhoneFactory.getPhones()) {
                if (p.getPhoneType() == PHONE_TYPE_CDMA) {
                    logd("isRoaming, phone" + p.getPhoneId() + " is CDMA phone");
                    return false;
                }
            }
            for (int i = 0; i < PROJECT_SIM_NUM; i++) {
                if (tmEx.getIccAppFamily(i) >= TelephonyManagerEx.APP_FAM_3GPP2
                        || tmEx.isCt3gDualMode(i)) {
                    logd("Card" + i + " is roaming");
                    return true;
                }
            }
        }
        return false;
    }

    private boolean hasSuitableCdmaPhone() {
        for (Phone p : PhoneFactory.getPhones()) {
            if (p.getPhoneType() == PHONE_TYPE_CDMA) {
                logd("hasSuitableCdmaPhone, phone" + p.getPhoneId());
                return true;
            }
        }
        return false;
    }

    private boolean hasSuitableGsmPhone() {
        // TODO: if support L+W or L+L, return true
        boolean noSimInserted = true;
        for (int i = 0; i < PROJECT_SIM_NUM; i++) {
            if (mTm.hasIccCard(i)) {
                noSimInserted = false;
                break;
            }
        }
        int mainPhoneId = RadioCapabilitySwitchUtil.getMainCapabilityPhoneId();
        Phone mainPhone = PhoneFactory.getPhone(mainPhoneId);
        logd("hasSuitableGsmPhone, noSimInserted:" + noSimInserted + ", mainPhone type:"
                + (mainPhone != null ? mainPhone.getPhoneType() : PHONE_TYPE_NONE));
        if (noSimInserted && mainPhone != null && mainPhone.getPhoneType() != PHONE_TYPE_GSM) {
            return false;
        } else {
            return true;
        }
    }

    private boolean hasInServiceGsmPhone() {
        for (Phone p : PhoneFactory.getPhones()) {
            if (p.getPhoneType() == PHONE_TYPE_GSM
                    && ServiceState.STATE_IN_SERVICE == p.getServiceState().getState()) {
                logd("Phone" + p.getPhoneId() + " in service");
                return true;
            }
        }
        return false;
    }

    private boolean needToSwitchPhone() {
        if (!MTK_C2K_SUPPORT) {
            return false;
        }
        if (mTelephonyConnectionServiceUtil.hasPerformEccRetry()) {
            int previousPhoneType = mTelephonyConnectionServiceUtil.getEccPhoneType();
            logd("needToSwitchPhone, previousPhoneType:" + previousPhoneType);
            if (!mEccNumberUtils.isGsmAlwaysNumber() && !mEccNumberUtils.isGsmOnlyNumber()
                    && previousPhoneType == PHONE_TYPE_GSM && !hasSuitableCdmaPhone()
                    && (!isRoaming() || isInCtLteOnlyMode())) {
                logd("Need to switch to CDMAPhone");
                return true;
            }
            if (!mEccNumberUtils.isCdmaAlwaysNumber()
                    && ((previousPhoneType == PHONE_TYPE_CDMA && !hasSuitableGsmPhone())
                        || (previousPhoneType == PHONE_TYPE_GSM && isInCtLteOnlyMode()))) {
                logd("Need to switch to GSMPhone");
                return true;
            }
            logd("No need to switch phone");
            return false;
        }
        if ((mEccNumberUtils.isGsmAlwaysNumber() || mEccNumberUtils.isGsmOnlyNumber()
                || mEccNumberUtils.isGsmPreferredNumber())
                && !hasSuitableGsmPhone()) {
            logd("Need to switch to GSMPhone");
            return true;
        }
        if (((mEccNumberUtils.isCdmaAlwaysNumber())
                || (mEccNumberUtils.isCdmaPreferredNumber() && !hasInServiceGsmPhone()))
                && !hasSuitableCdmaPhone() && !isRoaming()) {
            logd("Need to switch to CDMAPhone");
            return true;
        }
        logd("No need to switch phone");
        return false;
    }

    private void waitForCapabilitySwitchFinish(Callback callback) {
        cleanup();
        mCallback = callback;
        IntentFilter intentFilter;
        if (!isAllCdmaCard()) {
            intentFilter = new IntentFilter(TelephonyIntents.ACTION_SET_RADIO_CAPABILITY_DONE);
            intentFilter.addAction(TelephonyIntents.ACTION_SET_RADIO_CAPABILITY_FAILED);
        } else {
            for (int i = 0; i < PROJECT_SIM_NUM; i++) {
                mRadioTechCollected[i] = 0;
            }
            intentFilter = new IntentFilter(TelephonyIntents.ACTION_RADIO_TECHNOLOGY_CHANGED);
        }
        mSimSwitchReceiver = new SimSwitchReceiver();
        mContext.registerReceiver(mSimSwitchReceiver, intentFilter);
        mRegisterSimSwitchReceiver = true;
        mHandler.sendEmptyMessageDelayed(MSG_WAIT_FOR_INTENT_TIMEOUT,
                WAIT_FOR_FINISH_TIMEOUT_MILLIS);
    }

    private void onComplete(boolean success) {
        if (mCallback != null) {
            Callback tempCallback = mCallback;
            mCallback = null;
            tempCallback.onComplete(success);
        }
    }

    private void unregisterReceiver() {
        logd("unregisterReceiver, mRegisterSwitchPhoneReceiver:" + mRegisterSwitchPhoneReceiver
                + ", mRegisterSimStateReceiver:" + mRegisterSimStateReceiver
                + ", mRegisterSimSwitchReceiver:" + mRegisterSimSwitchReceiver);
        if (mRegisterSwitchPhoneReceiver) {
            mContext.unregisterReceiver(mSwitchPhoneReceiver);
            mRegisterSwitchPhoneReceiver = false;
        }
        if (mRegisterSimStateReceiver) {
            mContext.unregisterReceiver(mSimStateReceiver);
            mRegisterSimStateReceiver = false;
        }
        if (mRegisterSimSwitchReceiver) {
            mContext.unregisterReceiver(mSimSwitchReceiver);
            mRegisterSimSwitchReceiver = false;
        }
    }

    private void cleanup() {
        logd("cleanup");
        unregisterReceiver();
        // This will send a failure call back if callback has yet to be invoked.  If the callback
        // was already invoked, it's a no-op.
        onComplete(false);
        mHandler.removeCallbacksAndMessages(null);
        mPhone = null;
    }

    private boolean isAllCdmaCard() {
        int appFamily;
        for (int i = 0; i < PROJECT_SIM_NUM; i++) {
            appFamily = TelephonyManagerEx.getDefault().getIccAppFamily(i);
            if (appFamily == TelephonyManagerEx.APP_FAM_NONE
                    || appFamily == TelephonyManagerEx.APP_FAM_3GPP) {
                logd("appFamily of slot" + i + " is " + appFamily);
                return false;
            }
        }
        return true;
    }

    private boolean isAllPhoneReady() {
        for (int i = 0; i < PROJECT_SIM_NUM; i++) {
            if (mRadioTechCollected[i] != 1) {
                return false;
            }
        }
        return true;
    }

    private void logd(String s) {
        Log.d(TAG, s);
    }

    private class SwitchPhoneReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            logd("Received:" + action);
            if (mPhone != null) {
                logd("Service state:" + mPhone.getServiceState().getState()
                        + ", phoneType:" + mPhone.getPhoneType()
                        + ", mTargetPhoneType:" + mTargetPhoneType
                        + ", phoneId:" + mPhone.getPhoneId()
                        + ", hasIccCard:" + mTm.hasIccCard(mPhone.getPhoneId()));
                if (TelephonyIntents.ACTION_RADIO_TECHNOLOGY_CHANGED.equals(action)) {
                    if (mPhone.getPhoneType() == mTargetPhoneType) {
                        logd("Switch to target phone!");
                        cancelSwitchPhoneTimer();
                        finish();
                    }
                } else if (TelephonyIntents.ACTION_SUBINFO_RECORD_UPDATED.equals(action)) {
                    if (mTm.hasIccCard(mPhone.getPhoneId())) {
                        logd("No need to switch phone anymore!");
                        cancelSwitchPhoneTimer();
                        finish();
                    }
                }
            }
        }
    }

    private class SimStateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            int slotId = intent.getIntExtra(PhoneConstants.PHONE_KEY,
                    SubscriptionManager.INVALID_SIM_SLOT_INDEX);
            String simState = intent.getStringExtra(IccCardConstants.INTENT_KEY_ICC_STATE);
            logd("Received ACTION_SIM_STATE_CHANGED, slotId:" + slotId
                    + ", simState:" + simState + ", mSkipFirstIntent:" + mSkipFirstIntent);
            if (!mSkipFirstIntent) {
                mSkipFirstIntent = true;
                return;
            }
            if (slotId != SubscriptionManager.INVALID_SIM_SLOT_INDEX) {
                mSimStateCollected[slotId] = 1;
                mSimState[slotId] = simState;
            }
            if (isAllSimReady()) {
                mHandler.removeMessages(MSG_WAIT_FOR_INTENT_TIMEOUT);
                if (needToSwitchPhone()) {
                    startSwitchPhone(mCallback);
                } else {
                    finish();
                }
            }
        }
    }

    private class SimSwitchReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (TelephonyIntents.ACTION_RADIO_TECHNOLOGY_CHANGED.equals(action)) {
                int slotId = intent.getIntExtra(PhoneConstants.PHONE_KEY,
                        SubscriptionManager.INVALID_SIM_SLOT_INDEX);
                logd("Received ACTION_RADIO_TECHNOLOGY_CHANGED, slotId:" + slotId
                        + ", mSkipFirstIntent:" + mSkipFirstIntent);
                if (!mSkipFirstIntent) {
                    mSkipFirstIntent = true;
                    return;
                }
                if (slotId != SubscriptionManager.INVALID_SIM_SLOT_INDEX) {
                    mRadioTechCollected[slotId] = 1;
                }
                if (isAllPhoneReady()) {
                    mHandler.removeMessages(MSG_WAIT_FOR_INTENT_TIMEOUT);
                    if (needToSwitchPhone()) {
                        startSwitchPhone(mCallback);
                    } else {
                        finish();
                    }
                }
            } else {
                logd("Received " + action);
                mHandler.removeMessages(MSG_WAIT_FOR_INTENT_TIMEOUT);
                if (needToSwitchPhone()) {
                    startSwitchPhone(mCallback);
                } else {
                    finish();
                }
            }
        }
    }

    private class AirplaneModeObserver extends ContentObserver {
        private Context mMyContext;
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
            logd("onChange, isAirplaneModeOn:" + isAirplaneModeOn);
            if (isAirplaneModeOn) {
                cleanup();
            }
        }
    }

    private boolean isInCtLteOnlyMode() {
        if (!MTK_CT_VOLTE_SUPPORT) {
            return false;
        }

        boolean ctLteOnlyMode = false;
        boolean volteSupport = false;
        boolean volteSetting = false;
        int mainPhoneId = RadioCapabilitySwitchUtil.getMainCapabilityPhoneId();
        int cdmaSlot = SystemProperties.getInt("persist.radio.cdma_slot", -1);
        int appFamily = TelephonyManagerEx.getDefault().getIccAppFamily(cdmaSlot - 1);
        Phone phone = PhoneFactory.getPhone(cdmaSlot - 1);
        if (cdmaSlot != -1 && (mainPhoneId == cdmaSlot - 1)
            && (appFamily == TelephonyManagerEx.APP_FAM_NONE
                || appFamily >= TelephonyManagerEx.APP_FAM_3GPP2)
            && phone != null && phone.getPhoneType() == PHONE_TYPE_GSM) {
            volteSupport = ImsManager.isVolteEnabledByPlatform(phone.getContext());
            volteSetting = ImsManager.isEnhanced4gLteModeSettingEnabledByUser(phone.getContext());
            if (volteSupport && volteSetting) {
                ctLteOnlyMode = true;
            }
        }
        logd("isInCtLteOnlyMode, mainPhoneId=" + mainPhoneId
                + ", cdmaSlot=" + cdmaSlot
                + ", appFamily=" + appFamily
                + ", phoneType=" + (phone != null ? phone.getPhoneType() : PHONE_TYPE_NONE)
                + ", volteSupport=" + volteSupport
                + ", volteSetting=" + volteSetting
                + ", ctLteOnlyMode=" + ctLteOnlyMode);
        return ctLteOnlyMode;
    }

    private void exitCtLteOnlyMode() {
        int cdmaSlot = SystemProperties.getInt("persist.radio.cdma_slot", -1);
        Phone phone = PhoneFactory.getPhone(cdmaSlot - 1);
        if (cdmaSlot != -1 && phone != null) {
            ImsManager.setEnhanced4gLteModeSetting(phone.getContext(), false);
        }
        logd("exitCtLteOnlyMode, cdmaSlot=" + cdmaSlot);
    }

    private void startTurnOffVolteTimer() {
        cancelTurnOffVolteTimer();
        mHandler.sendEmptyMessageDelayed(MSG_TURN_OFF_VOLTE_TIMEOUT, TURN_OFF_VOLTE_TIMEOUT_MILLIS);
    }

    private void cancelTurnOffVolteTimer() {
        mHandler.removeMessages(MSG_TURN_OFF_VOLTE_TIMEOUT);
        mHandler.removeMessages(MSG_START_TURN_OFF_VOLTE);
    }
}
