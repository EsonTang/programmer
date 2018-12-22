package com.mediatek.providers.contacts;

import android.content.Context;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.provider.ContactsContract.Aas;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;

import com.android.internal.telephony.IIccPhoneBook;
import com.mediatek.internal.telephony.ITelephonyEx;

/**
 * Add this class for SIM support.
 */
public class SimCardUtils {

    public static final String TAG = "ProviderSimCardUtils";
    private static final String ACCOUNT_TYPE_POSTFIX = " Account";
    public static TelephonyManager sTelephonyManager;

    /**
     * M: Structure function.
     * @param context context
     */
    public SimCardUtils(Context context) {
        sTelephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
    }

    /**
     * M: add for mark SIM type.
     */
    public interface SimType {
        String SIM_TYPE_SIM_TAG = "SIM";
        int SIM_TYPE_SIM = 0;

        String SIM_TYPE_USIM_TAG = "USIM";
        int SIM_TYPE_USIM = 1;

        // UIM
        int SIM_TYPE_UIM = 2;
        int SIM_TYPE_CSIM = 3;
        String SIM_TYPE_UIM_TAG = "RUIM";
        // UIM
        // UICC TYPE
        String SIM_TYPE_CSIM_TAG = "CSIM";
        // UICC TYPE
    }

    /**
     * M: [Gemini+] all possible icc card type are put in this array. it's a map
     * of SIM_TYPE => SIM_TYPE_TAG like SIM_TYPE_SIM => "SIM"
     */
    private static final SparseArray<String> SIM_TYPE_ARRAY = new SparseArray<String>();
    static {
        SIM_TYPE_ARRAY.put(SimType.SIM_TYPE_SIM, SimType.SIM_TYPE_SIM_TAG);
        SIM_TYPE_ARRAY.put(SimType.SIM_TYPE_USIM, SimType.SIM_TYPE_USIM_TAG);
        SIM_TYPE_ARRAY.put(SimType.SIM_TYPE_UIM, SimType.SIM_TYPE_UIM_TAG);
        SIM_TYPE_ARRAY.put(SimType.SIM_TYPE_CSIM, SimType.SIM_TYPE_CSIM_TAG);
    }

    /**
     * M: check whether the SIM is inserted.
     * @param slotId the slot id of SIM.
     * @return whether the SIM is inserted.
     */
    public static boolean isSimInserted(int slotId) {
        boolean isSimInsert = false;

        if (sTelephonyManager != null) {
            if (ContactsProviderUtils.isGeminiSupport()) {
                isSimInsert = sTelephonyManager.hasIccCard(slotId);
            } else {
                isSimInsert = sTelephonyManager.hasIccCard(0);
            }
        }

        return isSimInsert;
    }

    /**
     * M: [Gemini+] get the icc card type by slotId.
     *
     * @param slotId slotId
     * @return the integer type
     */
    public static int getSimTypeBySlot(int slotId) {
        final ITelephonyEx iTel = ITelephonyEx.Stub.asInterface(ServiceManager
                .getService(Context.TELEPHONY_SERVICE_EX));
        String iccCardType = null;
        try {
            /// For ALPS01399514,ALPS01399519,there may be null pointer
            if (iTel != null) {
                if (ContactsProviderUtils.isGeminiSupport()) {
                    iccCardType = iTel.getIccCardType(slotId);
                } else {
                    iccCardType = iTel.getIccCardType(0);
                }
            }
        } catch (RemoteException e) {
            Log.e(TAG, "catched exception.");
            e.printStackTrace();
        }
        if (TextUtils.isEmpty(iccCardType)) {
            Log.w(TAG, "failed to get iccCardType");
            return -1;
        }
        /*
         * M: add for UICC card type start
         */
//        if (SimType.SIM_TYPE_CSIM_TAG.equals(iccCardType)) {
//            iccCardType = SimType.SIM_TYPE_USIM_TAG;
//        }
        /*
         * M: add for UICC card type end
         */
        for (int i = 0; i < SIM_TYPE_ARRAY.size(); i++) {
            if (TextUtils.equals(SIM_TYPE_ARRAY.valueAt(i), iccCardType)) {
                return SIM_TYPE_ARRAY.keyAt(i);
            }
        }
        Log.w(TAG, "iccCardType " + iccCardType + " is not valid");
        return -1;
    }

    /**
     * M: [Gemini+] get the readable sim account type, like "SIM Account".
     *
     * @param simType
     * the integer sim type
     * @return the string like "SIM Account"
     */
    public static String getSimAccountType(int simType) {
        return SIM_TYPE_ARRAY.get(simType) + ACCOUNT_TYPE_POSTFIX;
    }

    /**
     * M: [Gemini+]SIM account type is a string like "USIM Account".
     *
     * @param accountType the account type
     * @return whether the account is sim account
     */
    public static boolean isSimAccount(String accountType) {
        for (int i = 0; i < SIM_TYPE_ARRAY.size(); i++) {
            int simType = SIM_TYPE_ARRAY.keyAt(i);
            if (TextUtils.equals(getSimAccountType(simType), accountType)) {
                return true;
            }
        }
        Log.d(TAG, "account " + accountType + " is not SIM account");
        return false;
    }


    /// M: Add for AAS @{
    private static final String SIMPHONEBOOK_SERVICE = "simphonebook";

    private static IIccPhoneBook getIIccPhoneBook() {
        LogUtils.d(TAG, "[getIIccPhoneBook]");
        String serviceName = SIMPHONEBOOK_SERVICE;
        final IIccPhoneBook iIccPhb = IIccPhoneBook.Stub.asInterface(ServiceManager
                .getService(serviceName));
        return iIccPhb;
    }

    /**
     * The function to get AAS by sub id and the index in SIM.
     */
    public static String getAASLabel(String indicator) {
        final String DECODE_SYMBOL = Aas.ENCODE_SYMBOL;
        if (!indicator.contains(DECODE_SYMBOL) || indicator.indexOf(DECODE_SYMBOL) == 0
                || indicator.indexOf(DECODE_SYMBOL) == (indicator.length() - 1)) {
            LogUtils.w(TAG, "[getAASLabel] return;");
            return "";
        }
        String aas = "";
        String keys[] = indicator.split(DECODE_SYMBOL);
        int subId = Integer.valueOf(keys[0]);
        int index = Integer.valueOf(keys[1]);
        LogUtils.d(TAG, "[getAASLabel] subId: " + subId + ",index: " + index);
        if (subId > 0 && index > 0) {//if have the aas label.the index value must > 0.
            try {
                final IIccPhoneBook iIccPhb = getIIccPhoneBook();
                if (iIccPhb != null) {
                    if (iIccPhb.isPhbReady(subId)) {
                        aas = iIccPhb.getUsimAasById(subId, index);
                    } else {
                        LogUtils.e(TAG, "[getAASLabel]IccPhb not ready! subid=" + subId);
                    }
                }
            } catch (RemoteException e) {
                LogUtils.e(TAG, "[getAASLabel] catched exception.");
            }
        }
        if (aas == null) {
            aas = "";
        }
        LogUtils.d(TAG, "[getAASLabel] aas=" + aas);
        return aas;
    }

    /// @}
}

