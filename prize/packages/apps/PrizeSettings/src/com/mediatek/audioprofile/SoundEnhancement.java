/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein
 * is confidential and proprietary to MediaTek Inc. and/or its licensors.
 * Without the prior written permission of MediaTek inc. and/or its licensors,
 * any reproduction, modification, use or disclosure of MediaTek Software,
 * and information contained herein, in whole or in part, shall be strictly prohibited.
 *
 * MediaTek Inc. (C) 2014. All rights reserved.
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

package com.mediatek.audioprofile;

import android.content.Context;
import android.content.res.Resources;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.UserHandle;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.util.Log;

import com.android.settings.InstrumentedFragment;
import com.android.settings.R;
import com.android.settings.SettingsActivity;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.search.SearchIndexableRaw;

import com.mediatek.settings.FeatureOption;

import java.util.ArrayList;
import java.util.List;

public class SoundEnhancement extends SettingsPreferenceFragment implements Indexable {

    private static final String TAG = "SoundEnhancement";

    private Context mContext;
    private AudioManager mAudioManager = null;

    // Sound enhancement
    private static final String KEY_MUSIC_PLUS = "music_plus";
    private static final String KEY_SOUND_ENAHCNE = "sound_enhance";
    private static final String KEY_BESLOUDNESS = "bes_loudness";
    private static final String KEY_BESSURROUND = "bes_surround";
    private static final String KEY_ANC = "anc_switch";

    // Audio enhance preference
    private SwitchPreference mMusicPlusPrf;
    //BesLoudness checkbox preference
    private SwitchPreference mBesLoudnessPref;
    private Preference mBesSurroundPref;
    // ANC switch preference
    private SwitchPreference mAncPref;

    // the keys about set/get the status of driver
    private static final String GET_MUSIC_PLUS_STATUS = "GetMusicPlusStatus";
    private static final String GET_MUSIC_PLUS_STATUS_ENABLED = "GetMusicPlusStatus=1";
    private static final String SET_MUSIC_PLUS_ENABLED = "SetMusicPlusStatus=1";
    private static final String SET_MUSIC_PLUS_DISABLED = "SetMusicPlusStatus=0";

    // the params when set/get the status of besloudness
    private static final String GET_BESLOUDNESS_STATUS = "GetBesLoudnessStatus";
    private static final String GET_BESLOUDNESS_STATUS_ENABLED = "GetBesLoudnessStatus=1";
    private static final String SET_BESLOUDNESS_ENABLED = "SetBesLoudnessStatus=1";
    private static final String SET_BESLOUDNESS_DISABLED = "SetBesLoudnessStatus=0";

    // Sound enhance category has no preference
    private static final int SOUND_PREFERENCE_NULL_COUNT = 0;

    private static final String MTK_AUDENH_SUPPORT_State = "MTK_AUDENH_SUPPORT";
    private static final String MTK_AUDENH_SUPPORT_on = "MTK_AUDENH_SUPPORT=true";
    private static final String MTK_AUDENH_SUPPORT_off = "MTK_AUDENH_SUPPORT=false";

    // ANC
    public static final String ANC_UI_STATUS_DISABLED = "ANC_UI=off";
    public static final String ANC_UI_STATUS_ENABLED = "ANC_UI=on";
    public static final String GET_ANC_UI_STATUS = "ANC_UI";

    // BESSURROUND
    protected static final String BESSURROUND_ON = "BesSurround_OnOff=1";
    protected static final String BESSURROUND_OFF = "BesSurround_OnOff=0";
    protected static final String BESSURROUND_MOVIE = "BesSurround_Mode=0";
    protected static final String BESSURROUND_MUSIC = "BesSurround_Mode=1";
    protected static final int BESSURROUND_MODE_MOVIE = 0;
    protected static final int BESSURROUND_MODE_MUSIC = 1;
    protected static final String GET_BESSURROUND_STATE = "BesSurround_OnOff";
    protected static final String GET_BESSURROUND_MODE = "BesSurround_Mode";

    private String mAudenhState  = null;

    /**
     * called to do the initial creation of a fragment.
     *
     * @param icicle
     */
    public void onCreate(Bundle icicle) {
        Log.d("@M_" + TAG, "onCreate");
        super.onCreate(icicle);
        mContext = getActivity();

        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        //Query the audenh state
        mAudenhState = mAudioManager.getParameters(MTK_AUDENH_SUPPORT_State);
        Log.d("@M_" + TAG, "AudENH state: " + mAudenhState);
        PreferenceScreen root = getPreferenceScreen();
        if (root != null) {
            root.removeAll();
        }
        addPreferencesFromResource(R.xml.audioprofile_sound_enhancement);

        // get the music plus preference
        mMusicPlusPrf = (SwitchPreference) findPreference(KEY_MUSIC_PLUS);
        mBesLoudnessPref = (SwitchPreference) findPreference(KEY_BESLOUDNESS);
        mBesSurroundPref = (Preference) findPreference(KEY_BESSURROUND);
        mAncPref = (SwitchPreference) findPreference(KEY_ANC);

        if (!mAudenhState.equalsIgnoreCase(MTK_AUDENH_SUPPORT_on)) {
            Log.d("@M_" + TAG, "remove audio enhance preference " + mMusicPlusPrf);
            getPreferenceScreen().removePreference(mMusicPlusPrf);
        }
        if (!FeatureOption.MTK_BESLOUDNESS_SUPPORT) {
            Log.d("@M_" + TAG, "feature option is off, remove BesLoudness preference");
            getPreferenceScreen().removePreference(mBesLoudnessPref);
        }
        if (!FeatureOption.MTK_BESSURROUND_SUPPORT) {
            Log.d("@M_" + TAG, "remove BesSurround preference " + mBesSurroundPref);
            getPreferenceScreen().removePreference(mBesSurroundPref);
        }
        if (!FeatureOption.MTK_ANC_SUPPORT) {
            Log.d("@M_" + TAG, "feature option is off, remove ANC preference");
            getPreferenceScreen().removePreference(mAncPref);
        }
        setHasOptionsMenu(false);
    }

    private void updatePreferenceHierarchy() {
        // update music plus state
        if (mAudenhState.equalsIgnoreCase(MTK_AUDENH_SUPPORT_on)) {
            String state = mAudioManager.getParameters(GET_MUSIC_PLUS_STATUS);
            Log.d("@M_" + TAG, "get the state: " + state);
            boolean isChecked = false;
            if (state != null) {
                isChecked = state.equals(GET_MUSIC_PLUS_STATUS_ENABLED) ? true
                        : false;
            }
            mMusicPlusPrf.setChecked(isChecked);
        }

        //update Besloudness preference state
        if (FeatureOption.MTK_BESLOUDNESS_SUPPORT) {
            String state = mAudioManager.getParameters(GET_BESLOUDNESS_STATUS);
            Log.d("@M_" + TAG, "get besloudness state: " + state);
            mBesLoudnessPref.setChecked(GET_BESLOUDNESS_STATUS_ENABLED.equals(state));
        }

        if (FeatureOption.MTK_ANC_SUPPORT) {
            String state = mAudioManager.getParameters(GET_ANC_UI_STATUS);
            Log.d("@M_" + TAG, "ANC state: " + state);
            boolean checkedStatus = ANC_UI_STATUS_ENABLED.equals(state);
            mAncPref.setChecked(checkedStatus);
        }
    }

    /**
     * called when the fragment is visible to the user Need to update summary
     * and active profile, register for the profile change.
     */
    public void onResume() {
        Log.d("@M_" + TAG, "onResume");
        super.onResume();
        updatePreferenceHierarchy();
    }

    /**
     * Click the preference and enter into the EditProfile.
     *
     * @param preference
     *            the clicked preference
     * @return set success or fail
     */
    public boolean onPreferenceTreeClick(Preference preference) {

        // click the music plus checkbox
        if (mAudenhState.equalsIgnoreCase(MTK_AUDENH_SUPPORT_on)) {
            if (mMusicPlusPrf == preference) {
                boolean enabled = ((SwitchPreference) preference).isChecked();
                String cmdStr = enabled ? SET_MUSIC_PLUS_ENABLED : SET_MUSIC_PLUS_DISABLED;
                Log.d("@M_" + TAG, " set command about music plus: " + cmdStr);
                mAudioManager.setParameters(cmdStr);
            }
        }

        if (FeatureOption.MTK_BESLOUDNESS_SUPPORT) {
            if (mBesLoudnessPref == preference) {
                boolean enabled = ((SwitchPreference) preference).isChecked();
                String cmdStr = enabled ? SET_BESLOUDNESS_ENABLED : SET_BESLOUDNESS_DISABLED;
                Log.d("@M_" + TAG, " set command about besloudness: " + cmdStr);
                mAudioManager.setParameters(cmdStr);
            }
        }

        if (mBesSurroundPref == null) {
            Log.d("@M_" + TAG, " mBesSurroundPref = null");
        } else if (mBesSurroundPref.getKey() == null) {
            Log.d("@M_" + TAG, " mBesSurroundPref.getKey() == null)");
        }

        if (mBesSurroundPref == preference) {
            Log.d("@M_" + TAG, " mBesSurroundPref onPreferenceTreeClick");
            ((SettingsActivity) getActivity())
                    .startPreferencePanel(BesSurroundSettings.class.getName(),
                            null, -1, mContext.getText(R.string.audio_profile_bes_surround_title),
                            null, 0);
        }

        if (FeatureOption.MTK_ANC_SUPPORT) {
            if (mAncPref == preference) {
                boolean enabled = ((SwitchPreference) preference).isChecked();
                String cmdStr = enabled ? ANC_UI_STATUS_ENABLED : ANC_UI_STATUS_DISABLED;
                Log.d("@M_" + TAG, " set command about besloudness: " + cmdStr);
                mAudioManager.setParameters(cmdStr);
            }

        }

        return super.onPreferenceTreeClick(preference);
    }

    protected static boolean getBesSurroundState(AudioManager audioManager) {
        String state = audioManager.getParameters(GET_BESSURROUND_STATE);
        Log.d("@M_" + TAG, "getBesSurroundState: " + state);
        boolean besState = BESSURROUND_ON.equals(state);
        return besState;
    }

    protected static void setBesSurroundState(AudioManager audioManager, boolean state) {
        audioManager.setParameters(state ? BESSURROUND_ON : BESSURROUND_OFF);
    }

    protected static int getBesSurroundMode(AudioManager audioManager) {
        String state = audioManager.getParameters(GET_BESSURROUND_MODE);
        Log.d("@M_" + TAG, "getBesSurroundMode: " + state);
        boolean modeMovie = BESSURROUND_MOVIE.equals(state);
        return modeMovie ? BESSURROUND_MODE_MOVIE : BESSURROUND_MODE_MUSIC;
    }

    protected static void setBesSurroundMode(AudioManager audioManager, int mode) {
        audioManager.setParameters(mode == BESSURROUND_MODE_MOVIE ? BESSURROUND_MOVIE
                : BESSURROUND_MUSIC);
    }

    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
    new BaseSearchIndexProvider() {
        @Override
        public List<SearchIndexableRaw> getRawDataToIndex(Context context, boolean enabled) {
            final List<SearchIndexableRaw> result = new ArrayList<SearchIndexableRaw>();
            final Resources res = context.getResources();

            SearchIndexableRaw data = new SearchIndexableRaw(context);
            data.title = res.getString(R.string.sound_enhancement_title);
            data.screenTitle = res.getString(R.string.sound_enhancement_title);
            data.keywords = res.getString(R.string.sound_enhancement_title);
            result.add(data);

            return result;
        }
    };

    @Override
    protected int getMetricsCategory() {
        return InstrumentedFragment.METRICS_SOUNDENHANCEMENT;
    }
}

