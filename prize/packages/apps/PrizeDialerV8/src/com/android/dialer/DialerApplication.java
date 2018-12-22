/*
 * Copyright (C) 2013 The Android Open Source Project
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
 * limitations under the License
 */

package com.android.dialer;

import android.app.Application;
import android.content.Context;
import android.os.Trace;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.telecom.TelecomManager;

import com.android.contacts.common.extensions.ExtensionsFactory;
import com.android.contacts.common.testing.NeededForTesting;
import com.android.contacts.commonbind.analytics.AnalyticsUtil;
import com.android.dialer.compat.FilteredNumberCompat;
import com.android.dialer.database.FilteredNumberAsyncQueryHandler;
import com.android.dialer.filterednumber.BlockedNumbersAutoMigrator;
import com.android.internal.annotations.VisibleForTesting;

import com.mediatek.contacts.GlobalEnv;
import com.mediatek.dialer.dialersearch.DialerSearchHelper;
import com.mediatek.dialer.ext.ExtensionManager;
/*--PRIZE-Add-SIMPLE_LAUNCHER_TTS-hpf-2017_11_25-start--*/
import com.prize.tts.client.ServiceUtil;
import android.text.TextUtils;
import android.content.Context;
import android.media.AudioManager;
import com.android.dialer.util.VoiceUtils;
import android.os.Handler;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import android.util.Log;
/*--PRIZE-Add-SIMPLE_LAUNCHER_TTS-hpf-2017_11_25-end--*/
/*PRIZE-Add-TouchPalSDK_Call_Mark-zhoushuanghua-2018_5_29-start*/
import com.cootek.smartdialer_oem_module.sdk.CooTekPhoneService;
import android.telephony.TelephonyManager;
import com.mediatek.common.prizeoption.PrizeOption;
/*PRIZE-Add-TouchPalSDK_Call_Mark-zhoushuanghua-2018_5_29-end*/

public class DialerApplication extends Application {

    private static final String TAG = "DialerApplication";

    private static Context sContext;
    /*--PRIZE-Add-SIMPLE_LAUNCHER_TTS-hpf-2017_11_25-start--*/
    public static ServiceUtil mUtil;
    public static boolean isInComing = false;
    private static AudioManager audioManager;
 	/*--PRIZE-Add-SIMPLE_LAUNCHER_TTS-hpf-2017_11_25-end--*/
    /*PRIZE-Add-TouchPalSDK_Call_Mark-zhoushuanghua-2018_5_29-start*/
    private static String sNum1, sNum2, sVoipPkgName;
    /*PRIZE-Add-TouchPalSDK_Call_Mark-zhoushuanghua-2018_5_29-end*/

    @Override
    public void onCreate() {
        sContext = this;
        Trace.beginSection(TAG + " onCreate");
        super.onCreate();
        Trace.beginSection(TAG + " ExtensionsFactory initialization");
        ExtensionsFactory.init(getApplicationContext());
        Trace.endSection();
        /*--PRIZE-Add-SIMPLE_LAUNCHER_TTS-hpf-2017_11_25-start--*/
        mUtil = ServiceUtil.getInstance(getApplicationContext());
        audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        /*--PRIZE-Add-SIMPLE_LAUNCHER_TTS-hpf-2017_11_25-end--*/
        new BlockedNumbersAutoMigrator(PreferenceManager.getDefaultSharedPreferences(this),
                new FilteredNumberAsyncQueryHandler(getContentResolver())).autoMigrate();
        Trace.beginSection(TAG + " Analytics initialization");
        AnalyticsUtil.initialize(this);
        Trace.endSection();
        /// M: for ALPS01907201, init GlobalEnv for mediatek ContactsCommon
        GlobalEnv.setApplicationContext(getApplicationContext());
        /// M: [MTK Dialer Search] fix ALPS01762713 @{
        /*PRIZE-Change-PrizeInDialer_N-wangzhong-2016_10_24-start*/
        /*DialerSearchHelper.initContactsPreferences(getApplicationContext());*/
        DialerSearchHelper.initContactsPreferences(getContext());
        /*PRIZE-Change-PrizeInDialer_N-wangzhong-2016_10_24-end*/
        /// @}
        /// M: For plug-in @{
        ExtensionManager.getInstance().init(this);
        com.mediatek.contacts.ExtensionManager.registerApplicationContext(this);
        /// @}
        ///M:Add for Aas
        GlobalEnv.setAasExtension();

        /*PRIZE-Add-TMSDK_Call_Mark-wangzhong-2017_5_5-start*/
        if (com.android.dialer.prize.tmsdkcallmark.CallMarkManager.getInstance().isSuportTMSDKCallMark()) {
            try {
                tmsdk.common.TMSDKContext.init(this, com.android.dialer.prize.tmsdkcallmark.CallMarkSecureService.class, new tmsdk.common.ITMSApplicaionConfig() {
                    @Override
                    public java.util.HashMap<String, String> config(java.util.Map<String, String> src) {
                        java.util.HashMap<String, String> ret = new java.util.HashMap<String, String>(src);
                        return ret;
                    }
                });
            } catch (RuntimeException e) {
                Log.d("TMSDK_Call_Mark", "TMSDK_Call_Mark  TMSDKContext.init fail!");
            }
        }
        /*PRIZE-Add-TMSDK_Call_Mark-wangzhong-2017_5_5-end*/

        /*PRIZE-Add-TouchPalSDK_Call_Mark-zhoushuanghua-2018_5_29-start*/
        if(PrizeOption.PRIZE_COOTEK_SDK){
            android.util.Log.i("CooTek","CooTekPhoneService  initialize  start ");
            sNum1 = null;
            sNum2 = null;
            try {
                TelephonyManager telephonyManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
                sNum1 = telephonyManager.getLine1Number();
            } catch (SecurityException e) {
                e.printStackTrace();
            }
            //sVoipPkgName = null;
            //CooTekSDKUtils.initSDK(this, sNum1, sNum2, null);
            CooTekPhoneService sdk = CooTekPhoneService.initialize(getApplicationContext(), sNum1, null);
            android.util.Log.i("CooTek","CooTekPhoneService  initialize  end ");
        }
        /*PRIZE-Add-TouchPalSDK_Call_Mark-zhoushuanghua-2018_5_29-end*/
        Trace.endSection();
    }

    @Nullable
    public static Context getContext() {
        return sContext;
    }

    @NeededForTesting
    public static void setContextForTest(Context context) {
        sContext = context;
    }


    /// M: use to override system real service start @{
    private TelecomManager mTelecomManager;

    @Override
    public Object getSystemService(String name) {
        if (Context.TELECOM_SERVICE.equals(name) && mTelecomManager != null) {
            return mTelecomManager;
        }
        return super.getSystemService(name);
    }

    @VisibleForTesting
    public void setTelecomManager(TelecomManager telecom) {
        mTelecomManager = telecom;
    };
    /// M: end @}

    /*--PRIZE-Add-SIMPLE_LAUNCHER_TTS-hpf-2017_11_25-start--*/
    @Override
    public void onTerminate() {
        super.onTerminate();
        /*PRIZE-Add-TouchPalSDK_Call_Mark-zhoushuanghua-2018_5_29-start*/
        if (PrizeOption.PRIZE_COOTEK_SDK && CooTekPhoneService.isInitialized()) {
            CooTekPhoneService.getInstance().deinitialize();
        }
        /*PRIZE-Add-TouchPalSDK_Call_Mark-zhoushuanghua-2018_5_29-end*/
        mUtil.unbindservice();
        mUtil = null;
        audioManager = null;
        sContext = null;
    }

    /**Methods below are use to control weather to broadcast**/
    /**Dialpad broadcast**/
    public static void dialpadSpeak(int rsID){
        if(VoiceUtils.getKey(VoiceUtils.PRIZE_VOICE_KEY, sContext) != 1
                || VoiceUtils.getKey(VoiceUtils.PRIZE_VOICE_DIALER_KEY, sContext) != 1){
            return;
        }
        if(!TextUtils.isEmpty(sContext.getString(rsID))){
            int speak = mUtil.speakTextByMode(sContext.getString(rsID), 3);
        }
    }

    /**InCallUI dialpad broadcast**/
    public static void inCallUIDialPadSpeak(int rsID){
        Log.d(TAG,"[inCallUIDialPadSpeak]  VoiceUtils.getKey(VoiceUtils.PRIZE_VOICE_KEY, sContext) = "+VoiceUtils.getKey(VoiceUtils.PRIZE_VOICE_KEY, sContext)
                +"  VoiceUtils.getKey(VoiceUtils.PRIZE_VOICE_CALL_KEY, sContext) = "+VoiceUtils.getKey(VoiceUtils.PRIZE_VOICE_CALL_KEY, sContext));
        if(VoiceUtils.getKey(VoiceUtils.PRIZE_VOICE_KEY, sContext) != 1
                || VoiceUtils.getKey(VoiceUtils.PRIZE_VOICE_CALL_KEY, sContext) != 1){
            Log.d(TAG,"[inCallUIDialPadSpeak]  return``");
            return;
        }
        if(!TextUtils.isEmpty(sContext.getString(rsID))){
            Log.d(TAG,"[inCallUIDialPadSpeak]  speakTextByMode");
            int speak = mUtil.speakTextByMode(sContext.getString(rsID), 3);
        }
    }

    /**Incoming Calls broadcast**/
    public static void inComingSpeak(final String content){
        if(VoiceUtils.getKey(VoiceUtils.PRIZE_VOICE_KEY, sContext) != 1
                || VoiceUtils.getKey(VoiceUtils.PRIZE_VOICE_CALL_KEY, sContext) != 1){
            return;
        }

        if(!TextUtils.isEmpty(content) && isInComing){
            //mUtil.speak(content);
            new Handler().postDelayed(new Runnable(){
                public void run()
                {
                    int i = 0;
                    for(i = 0; i < 100; i++){
                        int speak = mUtil.speakTextByMode(preparePhoneNum(content) + sContext.getString(R.string.incoming_call), 1);
                    }
                }
            }, 100);
            //audioManager.adjustStreamVolume(AudioManager.STREAM_RING, AudioManager.ADJUST_UNMUTE, AudioManager.FLAG_VIBRATE);
            isInComing = false;
        }
    }

    public static void stopSpeaking(){
        mUtil.stop();
    }

    private static String preparePhoneNum (String content){
		Pattern pattern = Pattern.compile("[0-9]*");
		Matcher isNum = pattern.matcher(content);
		if( !isNum.matches() ){
		    return content;
		}
        if(Locale.getDefault().getLanguage().contains("zh")){
            return content;
        }

        StringBuffer temp = new StringBuffer();
        int a = 0;
        for(a = 0; a < content.length(); a++){
            if(Character.isSpace(content.charAt(a))){
            }else{
                temp = temp.append("" + content.charAt(a));
            }
        }

        StringBuffer cache = new StringBuffer();
        int i = 0;
        for(i = 0; i < temp.toString().length(); i++){
            switch(Integer.parseInt(("" + temp.toString().charAt(i)))){
                case 1:
                    cache.append(sContext.getString(R.string.dialpad_one) + " ");
                    break;
                case 2:
                    cache.append(sContext.getString(R.string.dialpad_two) + " ");
                    break;
                case 3:
                    cache.append(sContext.getString(R.string.dialpad_three) + " ");
                    break;
                case 4:
                    cache.append(sContext.getString(R.string.dialpad_four) + " ");
                    break;
                case 5:
                    cache.append(sContext.getString(R.string.dialpad_five) + " ");
                    break;
                case 6:
                    cache.append(sContext.getString(R.string.dialpad_six) + " ");
                    break;
                case 7:
                    cache.append(sContext.getString(R.string.dialpad_seven) + " ");
                    break;
                case 8:
                    cache.append(sContext.getString(R.string.dialpad_eight) + " ");
                    break;
                case 9:
                    cache.append(sContext.getString(R.string.dialpad_night) + " ");
                    break;
                case 0:
                    cache.append(sContext.getString(R.string.dialpad_zero) + " ");
                    break;
                default:
                    break;
            }
        }
        return cache.toString();
    }
    /*--PRIZE-Add-SIMPLE_LAUNCHER_TTS-hpf-2017_11_25-end--*/

}
