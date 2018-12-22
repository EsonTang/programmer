package com.mediatek.services.telephony;

import android.os.SystemProperties;
import android.telecom.PhoneAccountHandle;
import android.telephony.ServiceState;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneFactory;
/// M: CC: Vzw/CTVolte ECC
import android.telephony.RadioAccessFamily;
import com.android.internal.telephony.TelephonyDevController;
import com.android.phone.PhoneUtils;

import com.mediatek.internal.telephony.RadioCapabilitySwitchUtil;

import static com.android.internal.telephony.PhoneConstants.PHONE_TYPE_NONE;
import static com.android.internal.telephony.PhoneConstants.PHONE_TYPE_CDMA;
import static com.android.internal.telephony.PhoneConstants.PHONE_TYPE_GSM;

import java.util.ArrayList;
import java.util.List;

/**
 * The emergency call handler.
 * Selected the proper Phone for setting up the ecc call.
 */
public class EmergencyRuleHandler {
    private static final String TAG = "ECCRuleHandler";
    private static final boolean DBG = true;

    private Phone mGsmPhone = null;
    private Phone mCdmaPhone = null;
    private Phone mEccRetryPhone = null;
    private Phone mDefaultEccPhone = null;
    private Phone mMainPhone = null;
    private int mMainPhoneId = 0;

    private boolean mIsEccRetry;
    private List<GCRuleHandler> mGCRuleList;
    private EmergencyNumberUtils mEccNumberUtils;
    private TelephonyManager mTm;

    private static final boolean MTK_C2K_SUPPORT =
            "1".equals(SystemProperties.get("ro.boot.opt_c2k_support"));
    private static final int PROJECT_SIM_NUM = TelephonyManager.getDefault().getPhoneCount();

    /// M: CC: Vzw/CTVolte ECC @{
    TelephonyDevController mTelDevController = TelephonyDevController.getInstance();
    private boolean hasC2kOverImsModem() {
        if (mTelDevController != null && mTelDevController.getModem(0) != null &&
                mTelDevController.getModem(0).hasC2kOverImsModem() == true) {
            return true;
        }
        return false;
    }

    private static final int RAT_GSM_ONLY = 1;
    private static final int RAT_CDMA_ONLY = 2;
    private static final int RAT_GSM_PREF = 3;
    private static final int RAT_CDMA_PREF = 4;
    int mPrefRat = 0;
    /// @}

    /**
     * The common interface for ECC rule.
     */
    public interface GCRuleHandler {
        /**
         * Handle the ecc reqeust.
         * @param number The Ecc number will be dialed.
         * @return Phone The Phone object used for ecc.
         */
        public Phone handleRequest();
    }

    /**
     * Init the EmergencyRuleHandler.
     * @param accountHandle The target PhoneAccountHandle.
     * @param number The Ecc number.
     * @param isEccRetry whether this is ECC Retry.
     */
    public EmergencyRuleHandler(
            PhoneAccountHandle accountHandle,
            String number,
            boolean isEccRetry,
            Phone defaultEccPhone) {
        for (Phone p : PhoneFactory.getPhones()) {
            log("Phone" + p.getPhoneId() + ":" + (p.getPhoneType() == PHONE_TYPE_CDMA ?
                    "CDMA" : (p.getPhoneType() == PHONE_TYPE_GSM ? "GSM" : "NONE"))
                    + ", service state:" + serviceStateToString(p.getServiceState().getState()));
        }
        mTm = TelephonyManager.getDefault();
        mEccNumberUtils = new EmergencyNumberUtils(number);
        mIsEccRetry = isEccRetry;

        /* default phone is
        1. target phone if SIM inserted, or
        2. TeleService::getFirstPhoneForEmergencyCall()
        */
        mDefaultEccPhone = defaultEccPhone;
        mMainPhoneId = RadioCapabilitySwitchUtil.getMainCapabilityPhoneId();
        mMainPhone = PhoneFactory.getPhone(mMainPhoneId);
        initPhones(accountHandle);
    }

    /**
     * Check if both CDMA and GSM phone exist.
     * @return true if both CDMA and GSM phone exist, otherwise false.
     */
    public static boolean isDualPhoneCdmaExist() {
        if (MTK_C2K_SUPPORT) {
            if (PROJECT_SIM_NUM >= 2) {
                for (Phone p : PhoneFactory.getPhones()) {
                    if (p.getPhoneType() == PHONE_TYPE_CDMA) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void initPhones(PhoneAccountHandle accountHandle) {
        mGsmPhone = getProperPhone(PHONE_TYPE_GSM);
        mCdmaPhone = getProperPhone(PHONE_TYPE_CDMA);

        if (mGsmPhone != null) {
            log("GSM Network State == " +
                    serviceStateToString(mGsmPhone.getServiceState().getState()));
        } else {
            log("No GSM Phone exist.");
        }
        if (mCdmaPhone != null) {
            log("CDMA Network State == " +
                    serviceStateToString(mCdmaPhone.getServiceState().getState()));
        } else {
            log("No CDMA Phone exist.");
        }

        int phoneId = SubscriptionManager.INVALID_PHONE_INDEX;
        if (mIsEccRetry) {
            phoneId = Integer.parseInt(accountHandle.getId());
            mEccRetryPhone = PhoneFactory.getPhone(phoneId);
            log("EccRetry phoneId:" + phoneId);
        }
    }

    private Phone getProperPhone(int phoneType) {
        Phone phone = null;
        if (phoneType == PHONE_TYPE_GSM) {
            // 1. In service 3/4G phone
            if (mMainPhone != null && mMainPhone.getPhoneType() == PHONE_TYPE_GSM
                    && ServiceState.STATE_IN_SERVICE == mMainPhone.getServiceState().getState()) {
                log("getProperPhone(G) : in service, main phone, phoneId:" + mMainPhoneId);
                return mMainPhone;
            }
            // 2. In service phone
            for (int i = 0; i < PROJECT_SIM_NUM; i++) {
                if (i == mMainPhoneId) {
                    continue;
                }
                phone = PhoneFactory.getPhone(i);
                if (phone.getPhoneType() == PHONE_TYPE_GSM
                        && ServiceState.STATE_IN_SERVICE == phone.getServiceState().getState()) {
                    log("getProperPhone(G) : in service, non-main phone, slotid:" + i);
                    return phone;
                }
            }
            // 3. Radio on and SIM card inserted 3/4G phone
            if (mMainPhone != null && mMainPhone.getPhoneType() == PHONE_TYPE_GSM
                    && ServiceState.STATE_POWER_OFF != mMainPhone.getServiceState().getState()
                    && mTm.hasIccCard(mMainPhoneId)) {
                log("getProperPhone(G) : radio on, with SIM, main phone, phoneId:" + mMainPhoneId);
                return mMainPhone;
            }
            // 4. Radio on and SIM card inserted phone
            for (int i = 0; i < PROJECT_SIM_NUM; i++) {
                if (i == mMainPhoneId) {
                    continue;
                }
                phone = PhoneFactory.getPhone(i);
                if (phone.getPhoneType() == PHONE_TYPE_GSM
                        && ServiceState.STATE_POWER_OFF != phone.getServiceState().getState()
                        && mTm.hasIccCard(i)) {
                    log("getProperPhone(G) : radio on + with SIM + non-main slot:" + i);
                    return phone;
                }
            }
            // 5. Radio on 3/4G phone
            if (mMainPhone != null && mMainPhone.getPhoneType() == PHONE_TYPE_GSM
                    && ServiceState.STATE_POWER_OFF != mMainPhone.getServiceState().getState()) {
                log("getProperPhone(G) : radio on + noSIM + main slot:" + mMainPhoneId);
                return mMainPhone;
            }
            // 6. Radio on phone
            for (int i = 0; i < PROJECT_SIM_NUM; i++) {
                if (i == mMainPhoneId) {
                    continue;
                }
                phone = PhoneFactory.getPhone(i);
                if (phone.getPhoneType() == PHONE_TYPE_GSM
                        && ServiceState.STATE_POWER_OFF != phone.getServiceState().getState()) {
                    log("getProperPhone(G) : radio on + noSIM + non-main slot:" + i);
                    return phone;
                }
            }
            // 7. SIM card inserted 3/4G phone
            if (mMainPhone != null && mMainPhone.getPhoneType() == PHONE_TYPE_GSM
                    && mTm.hasIccCard(mMainPhoneId)) {
                log("getProperPhone(G) : radio off + with SIM + main slot:" + mMainPhoneId);
                return mMainPhone;
            }
            // 8. SIM card inserted phone
            for (int i = 0; i < PROJECT_SIM_NUM; i++) {
                if (i == mMainPhoneId) {
                    continue;
                }
                phone = PhoneFactory.getPhone(i);
                if (phone.getPhoneType() == PHONE_TYPE_GSM && mTm.hasIccCard(i)) {
                    log("getProperPhone(G) : radio off + with SIM + non-main slot:" + i);
                    return phone;
                }
            }
            // 9. 3/4G phone
            if (mMainPhone != null && mMainPhone.getPhoneType() == PHONE_TYPE_GSM) {
                log("getProperPhone(G) : radio off + noSIM + main slot:" + mMainPhoneId);
                return mMainPhone;
            }
            // 10. other phone
            for (int i = 0; i < PROJECT_SIM_NUM; i++) {
                if (i == mMainPhoneId) {
                    continue;
                }
                phone = PhoneFactory.getPhone(i);
                if (phone.getPhoneType() == PHONE_TYPE_GSM) {
                    log("getProperPhone(G) : radio off + noSIM + non-main slot:" + i);
                    return phone;
                }
            }
        } else if (phoneType == PHONE_TYPE_CDMA) {
            for (int i = 0; i < PROJECT_SIM_NUM; i++) {
                phone = PhoneFactory.getPhone(i);
                if (phone.getPhoneType() == PHONE_TYPE_CDMA) {
                    log("getProperPhone(C) : slot:" + i);
                    return phone;
                }
            }
        }
        return null;
    }

    /**
     * Check if gsm has registered to network.
     * @return indicates the register status.
     */
    private boolean isGsmNetworkReady() {
        if (mGsmPhone != null) {
            return ServiceState.STATE_IN_SERVICE
                    == mGsmPhone.getServiceState().getState();
        }
        return false;
    }

    /**
     * Check if cdma has registered to network.
     * @return indicates the register status.
     */
    private boolean isCdmaNetworkReady() {
        if (mCdmaPhone != null) {
            return ServiceState.STATE_IN_SERVICE
                    == mCdmaPhone.getServiceState().getState();
        }
        return false;
    }

    String serviceStateToString(int state) {
        String s = null;
        switch (state) {
            case ServiceState.STATE_IN_SERVICE:
                s = "STATE_IN_SERVICE";
                break;
            case ServiceState.STATE_OUT_OF_SERVICE:
                s = "STATE_OUT_OF_SERVICE";
                break;
            case ServiceState.STATE_EMERGENCY_ONLY:
                s = "STATE_EMERGENCY_ONLY";
                break;
            case ServiceState.STATE_POWER_OFF:
                s = "STATE_POWER_OFF";
                break;
            default:
                log("serviceStateToString, invalid state:" + state);
                s = "UNKNOWN_STATE";
                break;
        }
        return s;
    }

    /**
     * Get the proper Phone for ecc dial.
     * @return A object for Phone that used for setup call.
     */
   /*
   isDualPhoneCdmaExist = true:
       1. G + C
       2. No(G) + C
       3. G + (No)C
       4. No(G) + No(C)
   allSimInserted = true:
       5. G + G
       6. G
       7. C
   allSimInserted = false:
       8. G + No(G)
       9. No(G) + No(G)
       10. No(G)
       11. No(C)
   */
    public Phone getPreferredPhone() {
        if (!MTK_C2K_SUPPORT) {
            if (mIsEccRetry) {
                log("for non-c2k project, return eccRetry phone:" + mEccRetryPhone);
                return mEccRetryPhone;
            } else {
                log("for non-c2k project, return default phone:" + mDefaultEccPhone);
                return mDefaultEccPhone;
            }
        } else {
            Phone prefPhone = null;

            boolean allSimInserted = true;
            for (int i = 0; i < PROJECT_SIM_NUM; i++) {
                if (!mTm.hasIccCard(i)) {
                    allSimInserted = false;
                    break;
                }
            }

            log("getPreferredPhone, allSimInserted:" + allSimInserted);

            if (isDualPhoneCdmaExist()) {
                // case 1~4
                generateGCRuleList();
                prefPhone = getPhoneFromGCRuleList();
                if (prefPhone != null) {
                    log("for G+C project with G+C phone, return " + prefPhone + " rat:" + mPrefRat);
                } else {
                    log("for G+C project with G+C phone, return default phone:" + mDefaultEccPhone);
                    return mDefaultEccPhone;
                }
            } else {
                if (mIsEccRetry) {
                    log("for G+C project w/o G+C phone, return eccRetry phone:" + mEccRetryPhone);
                    return mEccRetryPhone;
                } else {
                    // case 5~7
                    // Cannot switch C capability when SIM inserted, no matter in service or not
                    if (allSimInserted) {
                        /* [ALPS03582877] Still set ECC preferred RAT for GSM only and GSM prefer
                           ECC number to avoid modem making ECC via CDMA network first. */
                        if (mEccNumberUtils.isGsmOnlyNumber() ||
                            mEccNumberUtils.isGsmPreferredNumber()) {
                            prefPhone = mDefaultEccPhone;
                            mPrefRat = RAT_GSM_PREF;
                        } else {
                            log("for G+C project w/o G+C phone,allSimInserted,return default phone:"
                                    + mDefaultEccPhone);
                            return mDefaultEccPhone;
                        }
                    } else {
                        // old design: 90/91/92 no need to switch phone here
                        if (!hasC2kOverImsModem()) {
                            return mDefaultEccPhone;
                        }

                        // new design: 93
                        // For numbers(999) as CdmaAlways & GsmPreferred, Always has higher prority
                        if (mEccNumberUtils.isCdmaAlwaysNumber()) {
                            prefPhone = mCdmaPhone;
                            mPrefRat = RAT_CDMA_ONLY;
                        } else if (mEccNumberUtils.isGsmAlwaysNumber() ||
                                mEccNumberUtils.isGsmOnlyNumber()) {
                            prefPhone = mGsmPhone;
                            mPrefRat = RAT_GSM_ONLY;
                        } else if (mEccNumberUtils.isCdmaPreferredNumber()) {
                            prefPhone = mCdmaPhone;
                            mPrefRat = RAT_CDMA_PREF;
                        } else if (mEccNumberUtils.isGsmPreferredNumber()) {
                            prefPhone = mGsmPhone;
                            mPrefRat = RAT_GSM_PREF;
                        } else {
                            log("for G+C project w/o G+C phone, in Service with SIM,"
                                + " return default phone:" + mDefaultEccPhone);
                            return mDefaultEccPhone;
                        }

                        // Error handling: if ECC number has no rule
                        if (prefPhone == null) {
                            prefPhone = getGsmPhoneAndSwitchToCdmaIfNecessary();
                        }
                    }/* End of allSimInserted */
                }/* End of mIsEccRetry */
            }/* End of isDualPhoneCdmaExist() */

            /// M: CC: Vzw/CTVolte ECC @{
            // 93 modem support MD retry ECC from IMS -> CS(C2K/GSM)
            if (hasC2kOverImsModem() && mPrefRat != 0 && prefPhone != null) {
                prefPhone.setEccPreferredRat(mPrefRat, null);
            }
            /// @}
            return prefPhone;
        }
    }

    /// M: CC: Vzw/CTVolte ECC @{
    private static final int MODE_GSM = 1;
    private static final int MODE_C2K = 4;
    private static final int RAF_C2K = RadioAccessFamily.RAF_IS95A | RadioAccessFamily.RAF_IS95B |
        RadioAccessFamily.RAF_1xRTT | RadioAccessFamily.RAF_EVDO_0 | RadioAccessFamily.RAF_EVDO_A |
        RadioAccessFamily.RAF_EVDO_B | RadioAccessFamily.RAF_EHRPD;

    // If called when allSimInserted = false
    private Phone getGsmPhoneAndSwitchToCdmaIfNecessary() {
        if (mPrefRat == RAT_CDMA_PREF || mPrefRat == RAT_CDMA_ONLY) {
            if (!mTm.hasIccCard(mDefaultEccPhone.getPhoneId())) {
                int raf = mDefaultEccPhone.getRadioAccessFamily();
                if ((raf & RAF_C2K) == 0) {
                    log("defaulEccPhone is not c2k-enabled, trigger switch");
                    mDefaultEccPhone.triggerModeSwitchByEcc(MODE_C2K, null);
                }
            }
        }
        return mDefaultEccPhone;
    }
    /// @}

    private void generateGCRuleList() {
        if (mGCRuleList != null) {
            mGCRuleList.clear();
        }
        mGCRuleList = new ArrayList<GCRuleHandler>();

        // 0. Select main phone for GSM w/o SIM
        mGCRuleList.add(new MainPhoneNoSimGsmRule());
        // 1. Select phone based on always number rule
        mGCRuleList.add(new AlwaysNumberRule());
        // 2. Select phone based on only number rule
        mGCRuleList.add(new OnlyNumberRule());
        // 3. Select ECC retry phone
        mGCRuleList.add(new EccRetryRule());
        // 4. Select phone based on GSM/CDMA service state
        mGCRuleList.add(new GCReadyRule());
        mGCRuleList.add(new GsmReadyOnlyRule());
        mGCRuleList.add(new CdmaReadyOnlyRule());
        mGCRuleList.add(new GCUnReadyRule());
    }

    private Phone getPhoneFromGCRuleList() {
        for (GCRuleHandler rule : mGCRuleList) {
            Phone phone = rule.handleRequest();
            if (phone != null) {
                //log("getPhoneFromGCRuleList, preferred phone" + phone.getPhoneId());
                return phone;
            }
        }
        return null;
    }

    /**
     * MainPhoneNoSimGsmRule
     */
    class MainPhoneNoSimGsmRule implements GCRuleHandler {
        public Phone handleRequest() {
            log("MainPhoneNoSimGsmRule: handleRequest...");

            boolean noSimInserted = true;
            for (int i = 0; i < PROJECT_SIM_NUM; i++) {
                if (mTm.hasIccCard(i)) {
                    noSimInserted = false;
                    break;
                }
            }

            // Select main capability phone for CMCC lab test
            if (!mIsEccRetry && noSimInserted && mMainPhone != null) {
                if (mEccNumberUtils.isGsmAlwaysNumber() || mEccNumberUtils.isGsmOnlyNumber()) {
                    mPrefRat = RAT_GSM_ONLY;
                    return mMainPhone;
                } else if (mEccNumberUtils.isGsmPreferredNumber()) {
                    mPrefRat = RAT_GSM_PREF;
                    return mMainPhone;
                }
            }
            return null;
        }
    }


    /**
     * AlwaysNumberRule
     */
    class AlwaysNumberRule implements GCRuleHandler {
        public Phone handleRequest() {
            log("AlwaysNumberRule: handleRequest...");
            if (mEccNumberUtils.isGsmAlwaysNumber()) {
                mPrefRat = RAT_GSM_ONLY;
                return mGsmPhone;
            }
            if (mEccNumberUtils.isCdmaAlwaysNumber()) {
                mPrefRat = RAT_CDMA_ONLY;
                return mCdmaPhone;
            }
            return null;
        }
    }

    /**
     * OnlyNumberRule
     */
    class OnlyNumberRule implements GCRuleHandler {
        public Phone handleRequest() {
            log("OnlyNumberRule: handleRequest...");
            if (mEccNumberUtils.isGsmOnlyNumber()) {
                mPrefRat = RAT_GSM_ONLY;
                return mGsmPhone;
            }
            return null;
        }
    }

    /**
     * ECC Retry rule
     */
    class EccRetryRule implements GCRuleHandler {
        public Phone handleRequest() {
            if (mIsEccRetry) {
                log("EccRetryRule: handleRequest...");
                // do not specific mPrefRat for EccRetryPhone
                return mEccRetryPhone;
            }
            return null;
        }
    }

    /**
     * Only GSM register to network.
     */
    class GsmReadyOnlyRule implements GCRuleHandler {
        public Phone handleRequest() {
            log("GsmReadyOnlyRule: handleRequest...");
            if (mEccNumberUtils.isGsmAlwaysNumber() || mEccNumberUtils.isCdmaAlwaysNumber()) {
                return null;
            }
            if (mEccNumberUtils.isGsmOnlyNumber()) {
                return null;
            }
            if (isGsmNetworkReady() && !isCdmaNetworkReady()) {
                // do not specific mPrefRat without number rule
                return mGsmPhone;
            }
            return null;
        }
    }

    /**
     * Only CDMA register to network.
     */
    class CdmaReadyOnlyRule implements GCRuleHandler {
        public Phone handleRequest() {
            log("CdmaReadyOnlyRule: handleRequest...");
            if (mEccNumberUtils.isGsmAlwaysNumber() || mEccNumberUtils.isCdmaAlwaysNumber()) {
                return null;
            }
            if (mEccNumberUtils.isGsmOnlyNumber()) {
                return null;
            }
            if (isCdmaNetworkReady() && !isGsmNetworkReady()) {
                // do not specific mPrefRat without number rule
                return mCdmaPhone;
            }
            return null;
        }
    }


    /**
     * CDMA and GSM register to network.
     */
    class GCReadyRule implements GCRuleHandler {
        public Phone handleRequest() {
            log("GCReadyRule: handleRequest...");
            if (mEccNumberUtils.isGsmAlwaysNumber() || mEccNumberUtils.isCdmaAlwaysNumber()) {
                return null;
            }
            if (mEccNumberUtils.isGsmOnlyNumber()) {
                return null;
            }
            if (isCdmaNetworkReady() && isGsmNetworkReady()) {
                if (mEccNumberUtils.isGsmPreferredNumber()) {
                    mPrefRat = RAT_GSM_PREF;
                    return mGsmPhone;
                }
                if (mEccNumberUtils.isCdmaPreferredNumber()) {
                    mPrefRat = RAT_CDMA_PREF;
                    return mCdmaPhone;
                }
            }
            return null;
        }
    }

    /**
     * Both CDMA and GSM are not ready.
     */
    class GCUnReadyRule implements GCRuleHandler {
        public Phone handleRequest() {
            log("GCUnReadyRule: handleRequest...");
            if (mEccNumberUtils.isGsmAlwaysNumber() || mEccNumberUtils.isCdmaAlwaysNumber()) {
                return null;
            }
            if (mEccNumberUtils.isGsmOnlyNumber()) {
                return null;
            }
            if (!isCdmaNetworkReady() && !isGsmNetworkReady()) {
                if (mEccNumberUtils.isGsmPreferredNumber()) {
                    mPrefRat = RAT_GSM_PREF;
                    return mGsmPhone;
                }
                if (mEccNumberUtils.isCdmaPreferredNumber()) {
                    mPrefRat = RAT_CDMA_PREF;
                    return mCdmaPhone;
                }
            }
            return null;
        }
    }

    /**
     * Handle ECC default case.
     */
    class DefaultHandler implements GCRuleHandler {
        public Phone handleRequest() {
            log("Can't got here! something is wrong!");
            return mGsmPhone;
        }
    }

    private void log(String s) {
        Log.d(TAG, s);
    }
}
