package com.mediatek.services.telephony;

import android.os.Build;
import android.os.SystemProperties;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.util.Log;

import static com.android.internal.telephony.PhoneConstants.PHONE_TYPE_CDMA;
import static com.android.internal.telephony.PhoneConstants.PHONE_TYPE_GSM;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

public class EmergencyNumberUtils {
    private static final String TAG = "ECCNumUtils";
    private static final boolean DBG = "eng".equals(Build.TYPE);

    private String mNumber;
    private boolean mIsGsmAlwaysNumber;
    private boolean mIsCdmaAlwaysNumber;
    private boolean mIsGsmOnlyNumber;
    private boolean mIsGsmPreferredNumber;
    private boolean mIsCdmaPreferredNumber;

    private static final String ECC_LIST_PREFERENCE_PATH
            = "/system/vendor/etc/ecc_list_preference.xml";
    private static final String OPERATOR_ATTR = "Operator";
    private static final String ECC_LIST_ATTR  = "EccList";
    private static final String ECC_GSM_ONLY_TAG       = "GsmOnly";
    private static final String ECC_GSM_PREFERRED_TAG  = "GsmPref";
    private static final String ECC_CDMA_PREFERRED_TAG = "CdmaPref";

    private static HashMap<String, String> sGsmOnlyEccMap       = new HashMap<String, String>();
    private static HashMap<String, String> sGsmPreferredEccMap  = new HashMap<String, String>();
    private static HashMap<String, String> sCdmaPreferredEccMap = new HashMap<String, String>();
    private static String sOp;

    static {
        parseEccListPreference();
        sOp = SystemProperties.get("persist.operator.optr", "OM");
    }

    public EmergencyNumberUtils(String number) {
        mNumber = number;
        log("Number:" + number + ", operator:" + sOp);
        mIsGsmAlwaysNumber = isGsmAlwaysNumber(mNumber);
        mIsCdmaAlwaysNumber = isCdmaAlwaysNumber(mNumber);
        mIsGsmOnlyNumber = isGsmOnlyNumber(mNumber);
        mIsGsmPreferredNumber = isGsmPreferredNumber(mNumber);
        mIsCdmaPreferredNumber = isCdmaPreferredNumber(mNumber);
    }

/*
<?xml version="1.0" encoding="utf-8"?>
<EccPrefTable>
    <!--
        The attribute definition for tag GsmOnly, GsmPref, CdmaPref:
        - Operator: OM or OPXX
        - EccList: the preferred ECC list
    -->
    <GsmOnly Operator="OM" EccList="112,000,08,118" />
    <GsmOnly Operator="OP09" EccList="112,000,08,118" />
    <GsmOnly Operator="OP01" EccList="112" />
    <GsmPref Operator="OM" EccList="911,999" />
    <GsmPref Operator="OP09" EccList="911,999" />
    <GsmPref Operator="OP01" EccList="000,08,118,911,999" />
    <CdmaPref Operator="OM" EccList="110,119,120,122" />
    <CdmaPref Operator="OP09" EccList="110,119,120,122" />
    <CdmaPref Operator="OP01" EccList="110,119,120,122" />
</EccPrefTable>
*/
    /**
     * Parse Ecc List Preference From XML File
     */
    private static void parseEccListPreference() {
        sGsmOnlyEccMap.clear();
        sGsmPreferredEccMap.clear();
        sCdmaPreferredEccMap.clear();

        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = factory.newPullParser();
            if (parser == null) {
                return;
            }
            FileReader fileReader = new FileReader(ECC_LIST_PREFERENCE_PATH);
            parser.setInput(fileReader);
            int eventType = parser.getEventType();

            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        String eccTag = parser.getName();
                        String op = null;
                        String eccList = null;
                        int attrNum = parser.getAttributeCount();
                        for (int i = 0; i < attrNum; ++i) {
                            String name = parser.getAttributeName(i);
                            String value = parser.getAttributeValue(i);
                            if (name.equals(OPERATOR_ATTR)) {
                                op = value;
                            } else if (name.equals(ECC_LIST_ATTR)) {
                                eccList = value;
                            }
                        }
                        if (op != null && eccList != null) {
                            if (eccTag.equals(ECC_GSM_ONLY_TAG)) {
                                sGsmOnlyEccMap.put(op, eccList);
                            } else if (eccTag.equals(ECC_GSM_PREFERRED_TAG)) {
                                sGsmPreferredEccMap.put(op, eccList);
                            } else if (eccTag.equals(ECC_CDMA_PREFERRED_TAG)) {
                                sCdmaPreferredEccMap.put(op, eccList);
                            }
                        }
                        break;
                    default:
                        break;
                }
                eventType = parser.next();
            }
            fileReader.close();
        } catch (FileNotFoundException e) {
            Log.e(TAG, "Ecc List Preference file not found");
            sGsmOnlyEccMap.put("OM", "112,000,08,118");
            sGsmOnlyEccMap.put("OP09", "112,000,08,118");
            sGsmOnlyEccMap.put("OP01", "112");
            sGsmPreferredEccMap.put("OM", "911,999");
            sGsmPreferredEccMap.put("OP09", "911,999");
            sGsmPreferredEccMap.put("OP01", "000,08,118,911,999");
            sCdmaPreferredEccMap.put("OM", "110,119,120,122");
            sCdmaPreferredEccMap.put("OP09", "110,119,120,122");
            sCdmaPreferredEccMap.put("OP01", "110,119,120,122");
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static boolean isNumberMatched(String number, String[] eccList) {
        for (String eccNumber : eccList) {
            if (number.equals(eccNumber)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isGsmAlwaysNumber(String number) {
        boolean result = PhoneNumberUtils.isEmergencyNumberExt(number, PHONE_TYPE_GSM)
                && !PhoneNumberUtils.isEmergencyNumberExt(number, PHONE_TYPE_CDMA);
        log("isGsmAlwaysNumber = " + result);
        return result;
    }

    public static boolean isCdmaAlwaysNumber(String number) {
        boolean result = !PhoneNumberUtils.isEmergencyNumberExt(number, PHONE_TYPE_GSM)
                && PhoneNumberUtils.isEmergencyNumberExt(number, PHONE_TYPE_CDMA);
        log("isCdmaAlwaysNumber = " + result);
        return result;
    }

    public static boolean isGsmOnlyNumber(String number) {
        String eccList = "";
        if (sGsmOnlyEccMap.containsKey(sOp)) {
            eccList = sGsmOnlyEccMap.get(sOp);
        } else {
            eccList = sGsmOnlyEccMap.get("OM");
        }
        boolean bMatched = TextUtils.isEmpty(eccList) ?
                false : isNumberMatched(number, eccList.split(","));
        log("isGsmOnlyNumber = " + bMatched + ", eccList = " + eccList);
        return bMatched;
    }

    public static boolean isGsmPreferredNumber(String number) {
        String eccList = "";
        if (sGsmPreferredEccMap.containsKey(sOp)) {
            eccList = sGsmPreferredEccMap.get(sOp);
        } else {
            eccList = sGsmPreferredEccMap.get("OM");
        }
        boolean bMatched = TextUtils.isEmpty(eccList) ?
                false : isNumberMatched(number, eccList.split(","));
        log("isGsmPreferredNumber = " + bMatched + ", eccList = " + eccList);
        return bMatched;
    }

    public static boolean isCdmaPreferredNumber(String number) {
        String eccList = "";
        if (sCdmaPreferredEccMap.containsKey(sOp)) {
            eccList = sCdmaPreferredEccMap.get(sOp);
        } else {
            eccList = sCdmaPreferredEccMap.get("OM");
        }
        boolean bMatched = TextUtils.isEmpty(eccList) ?
                false : isNumberMatched(number, eccList.split(","));
        log("isCdmaPreferredNumber = " + bMatched + ", eccList = " + eccList);
        return bMatched;
    }

    public boolean isGsmAlwaysNumber() {
        //log("isGsmAlwaysNumber:" + mIsGsmAlwaysNumber);
        return mIsGsmAlwaysNumber;
    }

    public boolean isCdmaAlwaysNumber() {
        //log("isCdmaAlwaysNumber:" + mIsCdmaAlwaysNumber);
        return mIsCdmaAlwaysNumber;
    }

    public boolean isGsmOnlyNumber() {
        //log("isGsmOnlyNumber:" + mIsGsmOnlyNumber);
        return mIsGsmOnlyNumber;
    }

    public boolean isGsmPreferredNumber() {
        //log("isGsmPreferredNumber:" + mIsGsmPreferredNumber);
        return mIsGsmPreferredNumber;
    }

    public boolean isCdmaPreferredNumber() {
        //log("isCdmaPreferredNumber:" + mIsCdmaPreferredNumber);
        return mIsCdmaPreferredNumber;
    }

    private static void log(String s) {
        if (DBG) {
            Log.d(TAG, s);
        }
    }
}
