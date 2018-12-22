package com.mediatek.settings.vtss;

import android.app.ActionBar;
import android.content.Intent;
import android.content.res.Resources;
import android.preference.PreferenceScreen;

import com.android.internal.telephony.CommandsInterface;
import com.android.phone.CallForwardEditPreference;
import com.android.phone.R;
import com.mediatek.settings.CallBarringBasePreference;

public class GsmUmtsVTUtils {
    public static final int VIDEO_SERVICE_CLASS = CommandsInterface.SERVICE_CLASS_VIDEO;
    public static final int VOICE_SERVICE_CLASS = CommandsInterface.SERVICE_CLASS_VOICE;
    public static final String SERVICE_CLASS = "service_class";
    public static final int CF_TYPE = 0;
    public static final int CB_TYPE = 1;

    private static final String BUTTON_CFU_KEY   = "button_cfu_key";
    private static final String BUTTON_CFB_KEY   = "button_cfb_key";
    private static final String BUTTON_CFNRY_KEY = "button_cfnry_key";
    private static final String BUTTON_CFNRC_KEY = "button_cfnrc_key";

    private static final String BUTTON_CALL_BARRING_KEY = "all_outing_key";
    private static final String BUTTON_ALL_OUTING_KEY = "all_outing_international_key";
    private static final String BUTTON_OUT_INTERNATIONAL_EXCEPT = "all_outing_except_key";
    private static final String BUTTON_ALL_INCOMING_KEY = "all_incoming_key";
    private static final String BUTTON_ALL_INCOMING_EXCEPT = "all_incoming_except_key";

    public static void setServiceClass(Intent intent, int serviceClass) {
        intent.putExtra(SERVICE_CLASS, serviceClass);
    }

    public static void setCFServiceClass(PreferenceScreen prefSet, int serviceClass) {
        CallForwardEditPreference buttonCFU;
        buttonCFU = (CallForwardEditPreference) prefSet.findPreference(BUTTON_CFU_KEY);
        buttonCFU.setServiceClass(serviceClass);

        CallForwardEditPreference buttonCFB;
        buttonCFB = (CallForwardEditPreference) prefSet.findPreference(BUTTON_CFB_KEY);
        buttonCFB.setServiceClass(serviceClass);

        CallForwardEditPreference buttonCFNRy;
        buttonCFNRy = (CallForwardEditPreference) prefSet.findPreference(BUTTON_CFNRY_KEY);
        buttonCFNRy.setServiceClass(serviceClass);

        CallForwardEditPreference buttonCFNRc;
        buttonCFNRc = (CallForwardEditPreference) prefSet.findPreference(BUTTON_CFNRC_KEY);
        buttonCFNRc.setServiceClass(serviceClass);
    }

    public static void setCBServiceClass(PreferenceScreen prefSet, int serviceClass) {
        CallBarringBasePreference callAllOutButton;
        callAllOutButton = (CallBarringBasePreference) prefSet
        .findPreference(BUTTON_CALL_BARRING_KEY);
        callAllOutButton.setServiceClass(serviceClass);

        CallBarringBasePreference callInternationalOutButton;
        callInternationalOutButton = (CallBarringBasePreference) prefSet
        .findPreference(BUTTON_ALL_OUTING_KEY);
        callInternationalOutButton.setServiceClass(serviceClass);

        CallBarringBasePreference callInternationalOutButton2;
        callInternationalOutButton2 = (CallBarringBasePreference) prefSet
        .findPreference(BUTTON_OUT_INTERNATIONAL_EXCEPT);
        callInternationalOutButton2.setServiceClass(serviceClass);

        CallBarringBasePreference callInButton;
        callInButton = (CallBarringBasePreference) prefSet
        .findPreference(BUTTON_ALL_INCOMING_KEY);
        callInButton.setServiceClass(serviceClass);

        CallBarringBasePreference callInButton2;
        callInButton2 = (CallBarringBasePreference) prefSet
        .findPreference(BUTTON_ALL_INCOMING_EXCEPT);
        callInButton2.setServiceClass(serviceClass);
    }

    public static int getActionBarResId(int serviceClass, int type) {

        int resId = R.string.actionBarCFVoice;

        if (type == CF_TYPE) {
            resId = R.string.actionBarCFVoice;
            if (serviceClass == VIDEO_SERVICE_CLASS) {
                resId = R.string.actionBarCFVideo;
            }
        } else {
            resId = R.string.actionBarCBVoice;
            if (serviceClass == VIDEO_SERVICE_CLASS) {
                resId = R.string.actionBarCBVideo;
            }
        }
        return resId;
    }
}