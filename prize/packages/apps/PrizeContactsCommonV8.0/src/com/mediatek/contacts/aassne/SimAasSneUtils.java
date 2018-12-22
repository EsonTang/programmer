package com.mediatek.contacts.aassne;

import android.content.Context;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.telephony.SubscriptionManager;
import android.text.TextUtils;

import com.android.internal.telephony.IIccPhoneBook;
import com.android.internal.telephony.ITelephony;
import com.android.internal.telephony.EncodeException;
import com.android.internal.telephony.GsmAlphabet;

import com.mediatek.contacts.simcontact.SimCardUtils;
import com.mediatek.contacts.simcontact.SlotUtils;
import com.mediatek.contacts.util.AccountTypeUtils;
import com.mediatek.contacts.util.Log;
import com.mediatek.internal.telephony.ITelephonyEx;
import com.mediatek.internal.telephony.uicc.AlphaTag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class SimAasSneUtils {
    private static final String TAG = "SimAasSneUtils";
    public static final String KEY_SUB_ID = "subId";
    private static final int SLOT_ID1 = com.android.internal.telephony.PhoneConstants.SIM_ID_1;
    private static final String SIMPHONEBOOK_SERVICE = "simphonebook";

    public static final String IS_ADDITIONAL_NUMBER = "1";

    private static HashMap<Integer, List<AlphaTag>> sAasMap =
                new HashMap<Integer, List<AlphaTag>>(2);

    private static final int ERROR = -1;

    private static String sCurrentAccount = null;
    private static int sCurSubId = -1;

    public static void setCurrentSubId(int subId) {
        sCurSubId = subId;
        sCurrentAccount = getAccountTypeBySub(subId);
        Log.d(TAG, "[setCurrentSubId] sCurSubId=" + sCurSubId
                + " sCurrentAccount=" + sCurrentAccount);
    }

    public static String getCurAccount() {
        Log.d(TAG, "[getCurAccount] sCurrentAccount=" + sCurrentAccount);
        return sCurrentAccount;
    }

    public static int getCurSubId() {
        Log.d(TAG, "[getCurSlotId] sCurSubId=" + sCurSubId);
        return sCurSubId;
    }

    public static String getAccountTypeBySub(int subId) {
        int slotId = SubscriptionManager.getSlotId(subId);
        if (slotId < SLOT_ID1) {
            Log.e(TAG, "[getAccountTypeBySub]Error slotid:" + slotId);
            return null;
        }
        String simAccountType;
        if (isSimInserted(slotId)) {
            simAccountType = getSimAccountTypeBySub(subId);
        } else {
            Log.e(TAG, "[getAccountTypeBySub]Error slotId:" + slotId + " no sim inserted!");
            simAccountType = null;
        }
        Log.d(TAG, "[getAccountTypeBySub] accountType:" + simAccountType);
        return simAccountType;
    }

    private static boolean isSimInserted(int slotId) {
        final ITelephony iTel = ITelephony.Stub.asInterface(ServiceManager
                .getService(Context.TELEPHONY_SERVICE));
        boolean isSimInsert = false;
        try {
            if (iTel != null) {
                isSimInsert = iTel.hasIccCardUsingSlotId(slotId);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
            isSimInsert = false;
        }
        return isSimInsert;
    }

    private static String getSimAccountTypeBySub(int subId) {
        final ITelephonyEx iTel = ITelephonyEx.Stub.asInterface(ServiceManager
                .getService(Context.TELEPHONY_SERVICE_EX));
        String simAccountType = AccountTypeUtils.ACCOUNT_TYPE_SIM;
        try {
            if (SimCardUtils.SimType.SIM_TYPE_USIM_TAG.equals(iTel.getIccCardType(subId))) {
                simAccountType = AccountTypeUtils.ACCOUNT_TYPE_USIM;
            } else if (SimCardUtils.SimType.SIM_TYPE_RUIM_TAG.equals(iTel.getIccCardType(subId))) {
                simAccountType = AccountTypeUtils.ACCOUNT_TYPE_RUIM;
            } else if (SimCardUtils.SimType.SIM_TYPE_CSIM_TAG.equals(iTel.getIccCardType(subId))) {
                simAccountType = AccountTypeUtils.ACCOUNT_TYPE_CSIM;
            }
        } catch (RemoteException e) {
            Log.e(TAG, "[getSimAccountTypeBySub] catched exception.");
            e.printStackTrace();
        }
        return simAccountType;
    }

    /**
     * refresh local aas list. after you change the USim card aas info, please refresh local info.
     * @param slot
     * @return
     */
    public static boolean refreshAASList(int subId) {
        int slot = SubscriptionManager.getSlotId(subId);
        if (slot < SLOT_ID1) {
            Log.d(TAG, "[refreshAASList] slot=" + slot);
            return false;
        }

        try {
            final IIccPhoneBook iIccPhb = getIIccPhoneBook();
            if (iIccPhb != null) {
                Log.d(TAG, "[refreshAASList] subId =" + subId);
                List<AlphaTag> atList = iIccPhb.getUsimAasList(subId);
                Log.d(TAG, "[refreshAASList] atList =" + atList);
                if (atList != null) {
                    Iterator<AlphaTag> iter = atList.iterator();
                    Log.d(TAG, "[refreshAASList] success");
                    while (iter.hasNext()) {
                        AlphaTag entry = iter.next();
                        String tag = entry.getAlphaTag();
                        if (TextUtils.isEmpty(tag)) {
                            iter.remove();
                        }
                        Log.d(TAG, "[refreshAASList] tag=" + tag);
                    }
                }
                sAasMap.put(slot, atList);
            }
        } catch (RemoteException e) {
            Log.e(TAG, "[refreshAASList] catched exception.");
            sAasMap.put(slot, null);
        }

        return true;
    }

    /**
     * get USim card aas info without null tag. It will return all aas info that can be used in
     * application.
     * @param slot
     * @return
     */
    public static List<AlphaTag> getAAS(int subId) {
        List<AlphaTag> atList = new ArrayList<AlphaTag>();
        int slot = SubscriptionManager.getSlotId(subId);
        if (slot < SLOT_ID1) {
            Log.e(TAG, "[getAAS] slot=" + slot);
            return atList;
        }
        // Here, force to refresh the list.
        Log.d(TAG, "[getAAS] refreshAASList");
        refreshAASList(subId);

        List<AlphaTag> list = sAasMap.get(slot);

        return list != null ? list : atList;
    }

    public static String getAASById(int subId, int index) {
        int slotId = SubscriptionManager.getSlotId(subId);
        if (slotId < SLOT_ID1 || index < 1) {
            return "";
        }
        String aas = "";
        try {
            final IIccPhoneBook iIccPhb = getIIccPhoneBook();
            if (iIccPhb != null) {
                aas = iIccPhb.getUsimAasById(subId, index);
            }
        } catch (RemoteException e) {
            Log.e(TAG, "[getUSIMAASById] catched exception.");
        }
        if (aas == null) {
            aas = "";
        }
        Log.d(TAG, "[getUSIMAASById] aas=" + aas);
        return aas;
    }

    public static int getAasIndexByName(String aas, int subId) {
        int slotId = SubscriptionManager.getSlotId(subId);
        if (slotId < SLOT_ID1 || TextUtils.isEmpty(aas)) {
            Log.e(TAG, "[getAasIndexByName] error slotId=" + slotId + ",aas=" + aas);
            return ERROR;
        }
        // here, it only can compare type name
        Log.d(TAG, "[getAasIndexByName] aas=" + aas);
        List<AlphaTag> atList = getAAS(subId);
        Iterator<AlphaTag> iter = atList.iterator();
        while (iter.hasNext()) {
            AlphaTag entry = iter.next();
            String tag = entry.getAlphaTag();
            if (aas.equalsIgnoreCase(tag)) {
                Log.d(TAG, "[getAasIndexByName] tag=" + tag);
                return entry.getRecordIndex();
            }
        }
        return ERROR;
    }

    public static int insertUSIMAAS(int subId, String aasName) {
        int slotId = SubscriptionManager.getSlotId(subId);
        if (slotId < SLOT_ID1 || TextUtils.isEmpty(aasName)) {
            return ERROR;
        }
        int result = ERROR;
        try {
            final IIccPhoneBook iIccPhb = getIIccPhoneBook();
            if (iIccPhb != null) {
                result = iIccPhb.insertUsimAas(subId, aasName);
            }
        } catch (RemoteException e) {
            Log.e(TAG, "[insertUSIMAAS] catched exception.");
        }

        return result;
    }

    public static boolean updateUSIMAAS(int subId, int index, int pbrIndex, String aasName) {
        boolean result = false;
        try {
            final IIccPhoneBook iIccPhb = getIIccPhoneBook();
            if (iIccPhb != null) {
                result = iIccPhb.updateUsimAas(subId, index, pbrIndex, aasName);
            }
        } catch (RemoteException e) {
            Log.e(TAG, "[updateUSIMAAS] catched exception.");
        }
        Log.d(TAG, "[updateUSIMAAS] refreshAASList");
        refreshAASList(subId);

        return result;
    }

    public static boolean removeUSIMAASById(int subId, int index, int pbrIndex) {
        boolean result = false;
        try {
            final IIccPhoneBook iIccPhb = getIIccPhoneBook();
            if (iIccPhb != null) {
                result = iIccPhb.removeUsimAasById(subId, index, pbrIndex);
            }
        } catch (RemoteException e) {
            Log.e(TAG, "[removeUSIMAASById] catched exception.");
        }
        Log.d(TAG, "[removeUSIMAASById] refreshAASList");
        refreshAASList(subId);

        return result;
    }

    public static boolean isAasTextValid(String text, int subId) {
        if (TextUtils.isEmpty(text)) {
            return false;
        }
        final int MAX = SlotUtils.getUsimAasMaxNameLength(subId);
        try {
            GsmAlphabet.stringToGsm7BitPacked(text);
            if (text.length() > MAX) {
                return false;
            }
        } catch (EncodeException e) {
            if (text.length() > ((MAX - 1) >> 1)) {
                return false;
            }
        }
        return true;
    }

    private static IIccPhoneBook getIIccPhoneBook() {
        Log.d(TAG, "[getIIccPhoneBook]");
        String serviceName = SIMPHONEBOOK_SERVICE;
        final IIccPhoneBook iIccPhb = IIccPhoneBook.Stub.asInterface(ServiceManager
                .getService(serviceName));
        return iIccPhb;
    }

    public static boolean isUsim(String accountType) {
        return AccountTypeUtils.ACCOUNT_TYPE_USIM.equals(accountType);
    }

    public static boolean isSim(String accountType) {
        return AccountTypeUtils.ACCOUNT_TYPE_SIM.equals(accountType);
    }

    public static boolean isCsim(String accountType) {
        return AccountTypeUtils.ACCOUNT_TYPE_CSIM.equals(accountType);
    }

    public static boolean isRuim(String accountType) {
        return AccountTypeUtils.ACCOUNT_TYPE_RUIM.equals(accountType);
    }

    public static boolean isUsimOrCsim(String accountType) {
        return isUsim(accountType) || isCsim(accountType);
    }

    public static boolean isSimOrRuim(String accountType) {
        return isSim(accountType) || isRuim(accountType);
    }

    public static boolean isPhoneNumType(String mimeType) {
        return Phone.CONTENT_ITEM_TYPE.equals(mimeType);
    }

    public static boolean isAasPhoneType(int type) {
        return (Anr.TYPE_AAS == type);
    }

    public static String getSuffix(int count) {
        if (count <= 0) {
            return "";
        } else {
            return String.valueOf(count);
        }
    }

}
