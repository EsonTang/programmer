/*
* Copyright (C) 2014 MediaTek Inc.
* Modification based on code covered by the mentioned copyright
* and/or permission notice(s).
*/
/*
 * Copyright (C) 2014 The Android Open Source Project
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

package com.android.dialer.settings;

import android.app.Activity;
import android.content.Context;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.provider.Settings;

import com.android.dialer.R;
import com.android.phone.common.util.SettingsUtil;
import com.mediatek.dialer.util.DialerFeatureOptions;
//import com.mediatek.audioprofile.AudioProfileManager;
//import com.mediatek.common.audioprofile.AudioProfileListener;

import java.lang.Boolean;
import java.lang.CharSequence;
import java.lang.Object;
import java.lang.Override;
import java.lang.Runnable;
import java.lang.String;
import java.lang.Thread;
import android.util.Log;
/*PRIZE-remove the padding of left and right-yuandailin-2015-9-16-start*/
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;
/*PRIZE-remove the padding of left and right-yuandailin-2015-9-16-end*/
import android.content.Intent;
import android.content.ComponentName;

import com.mediatek.common.prizeoption.PrizeOption; //prize add by xiarui for bug61629 2018-06-11

public class PrizeGeneralSettingsFragment extends PreferenceFragment
        implements Preference.OnPreferenceChangeListener,Preference.OnPreferenceClickListener {//PRIZE-add -yuandailin-2016-7-29
    private static final String CATEGORY_SOUNDS_KEY    = "dialer_general_sounds_category_key";
    //private static final String BUTTON_RINGTONE_KEY    = "button_ringtone_key";//prize-remove-Ringtone-hpf-2017-10-17
    private static final String BUTTON_VIBRATE_ON_RING = "button_vibrate_on_ring";
    private static final String BUTTON_PLAY_DTMF_TONE  = "button_play_dtmf_tone";
    private static final String BUTTON_RESPOND_VIA_SMS_KEY = "button_respond_via_sms_key";
    private static final String BUTTON_REJECT_BUTTON_KEY = "prize_call_reject_button_key";

    private static final int MSG_UPDATE_RINGTONE_SUMMARY = 1;
	/*PRIZE-add-yuandailin-2016-6-2-start*/
	private static final int NO_VIBRATION_FOR_CALLS = 0;
    private static final int DO_VIBRATION_FOR_CALLS = 1;
    /*PRIZE-add-yuandailin-2016-6-2-end*/
    private Context mContext;

    //private Preference mRingtonePreference;//prize-remove-Ringtone-hpf-2017-10-17
    private CheckBoxPreference mVibrateWhenRinging;
    private CheckBoxPreference mPlayDtmfTone;
    private Preference mRespondViaSms;
    private Preference prizeRejectKey;//PRIZE-add -yuandailin-2016-7-29

    /*PRIZE-Add-DialerV8-wangzhong-2017_7_19-start*/
    private View mView;
    /*PRIZE-Add-DialerV8-wangzhong-2017_7_19-end*/
    /* prize-remove-Ringtone-hpf-2017-10-17-start*/
    /*private Runnable mRingtoneLookupRunnable;
    private final Handler mRingtoneLookupComplete = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_UPDATE_RINGTONE_SUMMARY:
                    mRingtonePreference.setSummary((CharSequence) msg.obj);
                    break;
            }
        }
    };*/
    /* prize-remove-Ringtone-hpf-2017-10-17-end*/
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = getActivity().getApplicationContext();

        addPreferencesFromResource(R.xml.prize_general_settings);

        //mRingtonePreference = findPreference(BUTTON_RINGTONE_KEY);//prize-remove-Ringtone-hpf-2017-10-17
        mVibrateWhenRinging = (CheckBoxPreference) findPreference(BUTTON_VIBRATE_ON_RING);
        mPlayDtmfTone = (CheckBoxPreference) findPreference(BUTTON_PLAY_DTMF_TONE);
        mRespondViaSms = findPreference(BUTTON_RESPOND_VIA_SMS_KEY);
        prizeRejectKey= findPreference(BUTTON_REJECT_BUTTON_KEY);//PRIZE-add -yuandailin-2016-7-29
        prizeRejectKey.setOnPreferenceClickListener(this);//PRIZE-add -yuandailin-2016-7-29

        PreferenceCategory soundCategory = (PreferenceCategory) findPreference(CATEGORY_SOUNDS_KEY);
        if (mVibrateWhenRinging != null) {
            Vibrator vibrator = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
            if (vibrator != null && vibrator.hasVibrator() && !PrizeOption.PRIZE_VIBRATE_CONTROL) { //prize modify by xiarui for bug61629 2018-06-11
                mVibrateWhenRinging.setOnPreferenceChangeListener(this);
                /** M: for ALPS01927352, add Listener for audio profile changed @{ */
                /*if (DialerFeatureOptions.isMtkAudioProfileEnabled()) {
                    final AudioProfileManager audioProfileMgr = (AudioProfileManager) mContext
                            .getSystemService(Context.AUDIO_PROFILE_SERVICE);
                    mAudioProfileListener = new AudioProfileListener() {
                        @Override
                        public void onProfileChanged(String profileKey) {
                            boolean vbrEnabled = audioProfileMgr
                                    .isVibrationEnabled(profileKey);
                            String checkKey = audioProfileMgr.getActiveProfileKey();
                            if (vbrEnabled != mVibrateWhenRinging.isChecked()) {
                                mVibrateWhenRinging.setChecked(vbrEnabled);
                            }
                        }
                    };
                    audioProfileMgr.listenAudioProfie(mAudioProfileListener,
                            AudioProfileListener.LISTEN_PROFILE_CHANGE);
                } else {*/
                    mVbOnRingObsrv = new ContentObserver(new Handler()) {
                        @Override
                        public void onChange(boolean selfChange) {
                            boolean vbrEnabled = Settings.System.getInt(
                                    mContext.getContentResolver(),
                                    Settings.System.VIBRATE_WHEN_RINGING, 1) == 1;
                            if (mVibrateWhenRinging.isChecked() != vbrEnabled) {
                                mVibrateWhenRinging.setChecked(vbrEnabled);
                            }
                        }
                    };
                    mContext.getContentResolver().registerContentObserver(
                            Settings.System.getUriFor(
                                    Settings.System.VIBRATE_WHEN_RINGING), false, mVbOnRingObsrv);
                /*}*/
                /** @} */
            } else {
                soundCategory.removePreference(mVibrateWhenRinging);
                mVibrateWhenRinging = null;
            }
        }

        if (mPlayDtmfTone != null) {
            mPlayDtmfTone.setOnPreferenceChangeListener(this);
            /// M: [ALPS01841736] add listener for DMTF change
            getActivity().getContentResolver().registerContentObserver(Settings.System.getUriFor(
                    Settings.System.DTMF_TONE_WHEN_DIALING), false, mDTMFObserver);
            mPlayDtmfTone.setChecked(Settings.System.getInt(mContext.getContentResolver(),
                    Settings.System.DTMF_TONE_WHEN_DIALING, 1) != 0);
        }
        /* prize-remove-Ringtone-hpf-2017-10-17-start*/
        /*mRingtoneLookupRunnable = new Runnable() {
            @Override
            public void run() {
                if (mRingtonePreference != null) {
                    SettingsUtil.updateRingtoneName(
                            mContext,
                            mRingtoneLookupComplete,
                            RingtoneManager.TYPE_RINGTONE,
                            mRingtonePreference.getKey(),
                            MSG_UPDATE_RINGTONE_SUMMARY);
                }
            }
        };*/
        /* prize-remove-Ringtone-hpf-2017-10-17-end*/
    }

    /*PRIZE-remove the padding of left and right-yuandailin-2015-9-16-start*/
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
            
        View view = super.onCreateView(inflater, container, savedInstanceState);

        /*PRIZE-Add-DialerV8-wangzhong-2017_7_19-start*/
        mView = view;
        if (null != getActivity()) {
            getActivity().getActionBar().setElevation(mContext.getResources().getDimensionPixelOffset(R.dimen.prize_elevation_top));
        }
        /*PRIZE-Add-DialerV8-wangzhong-2017_7_19-end*/
        return view;
	}
    /*PRIZE-remove the padding of left and right-yuandailin-2015-9-16-end*/
    /**
     * Supports onPreferenceChangeListener to look for preference changes.
     *
     * @param preference The preference to be changed
     * @param objValue The value of the selection, NOT its localized display value.
     */
    @Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {
        if (preference == mVibrateWhenRinging) {
            boolean doVibrate = (Boolean) objValue;
            /// M: [ALPS01791893] phone not vibrate, when the MT call
            setVibrateOnRing(doVibrate);
        }
        return true;
    }

    /**
     * Click listener for toggle events.
     */
    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mPlayDtmfTone) {
            /** M: [ALPS01846179] add MTK AudioProfile support @{ */
            /*if (DialerFeatureOptions.isMtkAudioProfileEnabled()) {
                AudioProfileManager audioProfileMgr = (AudioProfileManager) mContext
                        .getSystemService(Context.AUDIO_PROFILE_SERVICE);
                String profileKey = audioProfileMgr.getActiveProfileKey();
                audioProfileMgr.setDtmfToneEnabled(profileKey, mPlayDtmfTone.isChecked() ? true
                        : false);
            } else {*/
                Settings.System.putInt(mContext.getContentResolver(),
                        Settings.System.DTMF_TONE_WHEN_DIALING, mPlayDtmfTone.isChecked() ? 1 : 0);
            /*}*/
            /** @} */
        } else if (preference == mRespondViaSms) {
            // Needs to return false for the intent to launch.
            return false;
        }
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mVibrateWhenRinging != null) {
            /** M: [ALPS01791893] check whether to vibrate @{ */
            mVibrateWhenRinging.setChecked(shouldVibrate());
            /** @} */
        }

        /*PRIZE-Add-DialerV8-wangzhong-2017_7_19-start*/
        View listView = mView.findViewById(android.R.id.list);
        if (null != listView && listView instanceof android.widget.ListView) {
            listView.setBackgroundColor(mContext.getResources().getColor(R.color.prize_preferences_lowest_bg));
            ((android.widget.ListView) listView).setDivider(null);
            android.graphics.drawable.ColorDrawable drawable = new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT);
            ((android.widget.ListView) listView).setSelector(drawable);
        }
        /*PRIZE-Add-DialerV8-wangzhong-2017_7_19-end*/

        // Lookup the ringtone name asynchronously.
        //new Thread(mRingtoneLookupRunnable).start();//prize-remove-Ringtone-hpf-2017-10-17
    }

    // -----------------------------------------------MTK------------------------------------------------------------
    /// M: [ALPS01791893] get vibrate on ring setting (refer to Ringer)
    private boolean shouldVibrate() {
        /*if (DialerFeatureOptions.isMtkAudioProfileEnabled()) {
            AudioProfileManager audioProfileMgr = (AudioProfileManager) mContext
                    .getSystemService(Context.AUDIO_PROFILE_SERVICE);
            String profileKey = audioProfileMgr.getActiveProfileKey();
            return audioProfileMgr.isVibrationEnabled(profileKey);
        } else {*/
            /*PRIZE-change-yuandailin-2016-6-2-start*/      
            //return SettingsUtil.getVibrateWhenRingingSetting(mContext);
            int vibrateWhenRingingSetting = Settings.System.getInt(getActivity()
                    .getContentResolver(), Settings.System.VIBRATE_WHEN_RINGING,
                    NO_VIBRATION_FOR_CALLS);
            return hasVibrator() && (vibrateWhenRingingSetting == DO_VIBRATION_FOR_CALLS);
            /*PRIZE-change-yuandailin-2016-6-2-end*/		
        /*}*/
    }

    /// M: [ALPS01791893] set vibrate on ring setting
    private void setVibrateOnRing(boolean vibrate) {
        /*if (DialerFeatureOptions.isMtkAudioProfileEnabled()) {
            AudioProfileManager audioProfileMgr = (AudioProfileManager) mContext
                    .getSystemService(Context.AUDIO_PROFILE_SERVICE);
            String profileKey = audioProfileMgr.getActiveProfileKey();
            audioProfileMgr.setVibrationEnabled(profileKey, vibrate);
        } else {*/
            Settings.System.putInt(mContext.getContentResolver(),
                    Settings.System.VIBRATE_WHEN_RINGING, vibrate ? 1 : 0);
        /*}*/
    }

    /** M: [ALPS01841736] add listener for DTMF change @{ */
    private DTMFObserver mDTMFObserver = new DTMFObserver();

    private class DTMFObserver extends ContentObserver {

        public DTMFObserver() {
            super(new Handler());
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            Activity activity = getActivity();
            if (activity != null) {
                boolean dtmfToneEnabled = Settings.System.getInt(
                        activity.getContentResolver(),
                        Settings.System.DTMF_TONE_WHEN_DIALING, 1) == 1;
                if(mPlayDtmfTone.isChecked() != dtmfToneEnabled) {
                    mPlayDtmfTone.setChecked(dtmfToneEnabled);
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        if (mDTMFObserver != null) {
            mContext.getContentResolver().unregisterContentObserver(mDTMFObserver);
        }
        if (mVbOnRingObsrv != null) {
            mContext.getContentResolver().unregisterContentObserver(mVbOnRingObsrv);
            mVbOnRingObsrv = null;
        }
        /*if(mAudioProfileListener != null) {
            ((AudioProfileManager) mContext
                    .getSystemService(Context.AUDIO_PROFILE_SERVICE)).listenAudioProfie(
                    mAudioProfileListener, AudioProfileListener.STOP_LISTEN);
            mAudioProfileListener = null;
        }*/
        super.onDestroy();
    }
    /** @} */

	/*PRIZE-add-yuandailin-2016-6-2-start*/
	/**
     * Whether the device hardware has a vibrator.
     */
    private boolean hasVibrator() {
        Vibrator vibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
        return vibrator != null && vibrator.hasVibrator();
    }
	/*PRIZE-add-yuandailin-2016-6-2-end*/

    /** M: for ALPS01927352, add listener for profile changed @{ */
    //AudioProfileListener mAudioProfileListener;
    ContentObserver mVbOnRingObsrv;
    /** @} */
    
    /*PRIZE-add -yuandailin-2016-7-29-start*/
	@Override
	public boolean onPreferenceClick(Preference preference) {	
		if (preference == prizeRejectKey){
			Intent intent = new Intent(Intent.ACTION_MAIN);
			ComponentName cn = new ComponentName("com.android.phone", "com.android.phone.prize.CallRejectActivity");
			intent.setComponent(cn);
        	getActivity().startActivity(intent);
        	return true;
        }
		return false;
	}
	/*PRIZE-add -yuandailin-2016-7-29-end*/
}
