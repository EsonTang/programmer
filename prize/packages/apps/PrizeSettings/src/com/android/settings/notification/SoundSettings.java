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
 * limitations under the License.
 */

package com.android.settings.notification;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.Vibrator;
import android.preference.SeekBarVolumizer;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.provider.SearchIndexableResource;
import android.provider.Settings;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.TwoStatePreference;
import android.support.v14.preference.SwitchPreference;
import android.text.TextUtils;
import android.util.Log;

import com.android.internal.logging.MetricsProto.MetricsEvent;
import com.android.settings.R;
import com.android.settings.RingtonePreference;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settings.dashboard.SummaryLoader;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.RestrictedPreference;
import com.mediatek.settings.UtilsExt;
import com.mediatek.settings.ext.IAudioProfileExt;
import com.mediatek.settings.sim.SimHotSwapHandler;
import com.mediatek.settings.sim.SimHotSwapHandler.OnSimHotSwapListener;

import android.telephony.SubscriptionManager;
import android.telephony.SubscriptionInfo;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static com.android.settingslib.RestrictedLockUtils.EnforcedAdmin;

import com.mediatek.common.prizeoption.PrizeOption;//prize add by xiarui 2018-06-28

public class SoundSettings extends SettingsPreferenceFragment implements Indexable {
    private static final String TAG = "SoundSettings";

    private static final String KEY_MEDIA_VOLUME = "media_volume";
    private static final String KEY_ALARM_VOLUME = "alarm_volume";
    private static final String KEY_RING_VOLUME = "ring_volume";
    private static final String KEY_NOTIFICATION_VOLUME = "notification_volume";
    private static final String KEY_PHONE_RINGTONE = "ringtone";
    private static final String KEY_NOTIFICATION_RINGTONE = "notification_ringtone";

	/*--prize add by liangchangwei for dualcard ringtone--*/
    private static final String KEY_DUALCARD_PHONE_RINGTONE1 = "dualcard_ringtone1";
    private static final String KEY_DUALCARD_NOTIFICATION1_RINGTONE = "dualcard_notification1_ringtone";
    private static final String KEY_DUALCARD_PHONE_RINGTONE2 = "dualcard_ringtone2";
    private static final String KEY_DUALCARD_NOTIFICATION2_RINGTONE = "dualcard_notification2_ringtone";
	/*--prize add by liangchangwei for dualcard ringtone--*/

    private static final String KEY_ALARM_RINGTONE = "alarm_ringtone";
    private static final String KEY_VIBRATE_WHEN_RINGING = "vibrate_when_ringing";
    private static final String KEY_WIFI_DISPLAY = "wifi_display";
    private static final String KEY_ZEN_MODE = "zen_mode";
    private static final String KEY_CELL_BROADCAST_SETTINGS = "cell_broadcast_settings";

    private static final String SELECTED_PREFERENCE_KEY = "selected_preference";
    /* prize-add-by-lijimeng-for vibrate when silent-20180604-start*/
    private static final String KEY_VIBRATE_WHEN_SILENT = "vibrate_when_silent";
    /* prize-add-by-lijimeng-for vibrate when silent-20180604-end*/
    private static final int REQUEST_CODE = 200;

    private static final String[] RESTRICTED_KEYS = {
        KEY_MEDIA_VOLUME,
        KEY_ALARM_VOLUME,
        KEY_RING_VOLUME,
        KEY_NOTIFICATION_VOLUME,
        /* Modify by zhudaopeng at 2016-12-05 Start */
        // KEY_ZEN_MODE,
        /* Modify by zhudaopeng at 2016-12-05 End */
    };

    private static final int SAMPLE_CUTOFF = 2000;  // manually cap sample playback at 2 seconds

    private final VolumePreferenceCallback mVolumeCallback = new VolumePreferenceCallback();
    private final H mHandler = new H();
    private final SettingsObserver mSettingsObserver = new SettingsObserver();
    private final Receiver mReceiver = new Receiver();
    private final ArrayList<VolumeSeekBarPreference> mVolumePrefs = new ArrayList<>();

    private Context mContext;
    private boolean mVoiceCapable;
    private Vibrator mVibrator;
    private AudioManager mAudioManager;
    private VolumeSeekBarPreference mRingOrNotificationPreference;

    private Preference mPhoneRingtonePreference;
    private Preference mNotificationRingtonePreference;
	/*--prize add by liangchangwei for dualcard ringtone--*/
    private Preference mDualCard1RingtonePreference;
    private Preference mDualCard1NotificationRingtonePreference;
    private Preference mDualCard2RingtonePreference;
    private Preference mDualCard2NotificationRingtonePreference;
	private boolean isDualCardInsert = false;
	/*--prize add by liangchangwei for dualcard ringtone--*/
    private Preference mAlarmRingtonePreference;
    private TwoStatePreference mVibrateWhenRinging;
    private ComponentName mSuppressor;
    private int mRingerMode = -1;

    private PackageManager mPm;
    private UserManager mUserManager;
    private RingtonePreference mRequestPreference;

    private IAudioProfileExt mExt;
    private SimHotSwapHandler mSimHotSwapHandler;
    /* prize-add-by-lijimeng-for vibrate when silent-20180604-start*/
    private SwitchPreference mVibrateWhenSilent;
    /* prize-add-by-lijimeng-for vibrate when silent-20180604-end*/

    @Override
    protected int getMetricsCategory() {
        return MetricsEvent.SOUND;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
        mExt = UtilsExt.getAudioProfilePlugin(mContext);
        mPm = getPackageManager();
        mUserManager = UserManager.get(getContext());
        mVoiceCapable = Utils.isVoiceCapable(mContext);

        mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        mVibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
        if (mVibrator != null && !mVibrator.hasVibrator()) {
            mVibrator = null;
        }

        addPreferencesFromResource(R.xml.sound_settings);
        mExt.addCustomizedPreference(this.getPreferenceScreen());
        initVolumePreference(KEY_MEDIA_VOLUME, AudioManager.STREAM_MUSIC,
                com.android.internal.R.drawable.ic_audio_media_mute);
        initVolumePreference(KEY_ALARM_VOLUME, AudioManager.STREAM_ALARM,
                com.android.internal.R.drawable.ic_audio_alarm_mute);
        if (mVoiceCapable) {
            mRingOrNotificationPreference =
                    initVolumePreference(KEY_RING_VOLUME, AudioManager.STREAM_RING,
                            com.android.internal.R.drawable.ic_audio_ring_notif_mute);
            removePreference(KEY_NOTIFICATION_VOLUME);
        } else {
            mRingOrNotificationPreference =
                    initVolumePreference(KEY_NOTIFICATION_VOLUME, AudioManager.STREAM_NOTIFICATION,
                            com.android.internal.R.drawable.ic_audio_ring_notif_mute);
            removePreference(KEY_RING_VOLUME);
        }

        // Enable link to CMAS app settings depending on the value in config.xml.
        boolean isCellBroadcastAppLinkEnabled = this.getResources().getBoolean(
                com.android.internal.R.bool.config_cellBroadcastAppLinks);
        try {
            if (isCellBroadcastAppLinkEnabled) {
                if (mPm.getApplicationEnabledSetting("com.android.cellbroadcastreceiver")
                        == PackageManager.COMPONENT_ENABLED_STATE_DISABLED) {
                    isCellBroadcastAppLinkEnabled = false;  // CMAS app disabled
                }
            }
        } catch (IllegalArgumentException ignored) {
            isCellBroadcastAppLinkEnabled = false;  // CMAS app not installed
        }
        if (!mUserManager.isAdminUser() || !isCellBroadcastAppLinkEnabled ||
                RestrictedLockUtils.hasBaseUserRestriction(mContext,
                        UserManager.DISALLOW_CONFIG_CELL_BROADCASTS, UserHandle.myUserId())) {
            removePreference(KEY_CELL_BROADCAST_SETTINGS);
        }
        initRingtones();
        initVibrateWhenRinging();
        updateRingerMode();
        updateEffectsSuppressor();
        initForSimStateChange();

        if (savedInstanceState != null) {
            String selectedPreference = savedInstanceState.getString(SELECTED_PREFERENCE_KEY, null);
            if (!TextUtils.isEmpty(selectedPreference)) {
                mRequestPreference = (RingtonePreference) findPreference(selectedPreference);
            }
        }
        /* prize-add-by-lijimeng-for vibrate when silent-20180604-start*/
        initVibrateWhenSilent();
        /* prize-add-by-lijimeng-for vibrate when silent-20180604-end*/
    }

    /**
     * init for sim state change, including SIM hot swap, airplane mode, etc.
     */
    private void initForSimStateChange() {
        /// M: for [SIM Hot Swap] @{
        mSimHotSwapHandler = new SimHotSwapHandler(getActivity().getApplicationContext());
        mSimHotSwapHandler.registerOnSimHotSwap(new OnSimHotSwapListener() {
            @Override
            public void onSimHotSwap() {
                if (getActivity() != null) {
                    Log.w(TAG, "onSimHotSwap, finish Activity~~");
                    getActivity().finish();
                }
            }
        });
   }

    @Override
    public void onResume() {
        super.onResume();
        lookupRingtoneNames();
        mSettingsObserver.register(true);
        mReceiver.register(true);
        updateRingOrNotificationPreference();
        updateEffectsSuppressor();
        ///M: ALPS02723215 update the switch status when resume.
        updateVibrateWhenRinging();
        for (VolumeSeekBarPreference volumePref : mVolumePrefs) {
            volumePref.onActivityResume();
        }

        final EnforcedAdmin admin = RestrictedLockUtils.checkIfRestrictionEnforced(mContext,
                UserManager.DISALLOW_ADJUST_VOLUME, UserHandle.myUserId());
        final boolean hasBaseRestriction = RestrictedLockUtils.hasBaseUserRestriction(mContext,
                UserManager.DISALLOW_ADJUST_VOLUME, UserHandle.myUserId());
        for (String key : RESTRICTED_KEYS) {
            Preference pref = findPreference(key);
            if (pref != null) {
                pref.setEnabled(!hasBaseRestriction);
            }
            if (pref instanceof RestrictedPreference && !hasBaseRestriction) {
                ((RestrictedPreference) pref).setDisabledByAdmin(admin);
            }
        }
        RestrictedPreference broadcastSettingsPref = (RestrictedPreference) findPreference(
                KEY_CELL_BROADCAST_SETTINGS);
        if (broadcastSettingsPref != null) {
            broadcastSettingsPref.checkRestrictionAndSetDisabled(
                    UserManager.DISALLOW_CONFIG_CELL_BROADCASTS);
        }
        mExt.onAudioProfileSettingResumed(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        for (VolumeSeekBarPreference volumePref : mVolumePrefs) {
            volumePref.onActivityPause();
        }
        mVolumeCallback.stopSample();
        mSettingsObserver.register(false);
        mReceiver.register(false);
        mExt.onAudioProfileSettingPaused(this);

    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference instanceof RingtonePreference) {
            mRequestPreference = (RingtonePreference) preference;
            mRequestPreference.onPrepareRingtonePickerIntent(mRequestPreference.getIntent());
            startActivityForResult(preference.getIntent(), REQUEST_CODE);
            return true;
        }
        if (mExt.onPreferenceTreeClick(preference)) {
            return true;
        }
        return super.onPreferenceTreeClick(preference);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (mRequestPreference != null) {
            mRequestPreference.onActivityResult(requestCode, resultCode, data);
            mRequestPreference = null;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mRequestPreference != null) {
            outState.putString(SELECTED_PREFERENCE_KEY, mRequestPreference.getKey());
        }
    }

    // === Volumes ===

    private VolumeSeekBarPreference initVolumePreference(String key, int stream, int muteIcon) {
        final VolumeSeekBarPreference volumePref = (VolumeSeekBarPreference) findPreference(key);
        volumePref.setCallback(mVolumeCallback);
        volumePref.setStream(stream);
        mVolumePrefs.add(volumePref);
        volumePref.setMuteIcon(muteIcon);
        return volumePref;
    }

    private void updateRingOrNotificationPreference() {
        mRingOrNotificationPreference.showIcon(mSuppressor != null
                ? com.android.internal.R.drawable.ic_audio_ring_notif_mute
                : mRingerMode == AudioManager.RINGER_MODE_VIBRATE || wasRingerModeVibrate()
                ? com.android.internal.R.drawable.ic_audio_ring_notif_vibrate
                : com.android.internal.R.drawable.ic_audio_ring_notif);
    }

    private boolean wasRingerModeVibrate() {
        return mVibrator != null && mRingerMode == AudioManager.RINGER_MODE_SILENT
                && mAudioManager.getLastAudibleStreamVolume(AudioManager.STREAM_RING) == 0;
    }

    private void updateRingerMode() {
        final int ringerMode = mAudioManager.getRingerModeInternal();
        if (mRingerMode == ringerMode) return;
        mRingerMode = ringerMode;
        updateRingOrNotificationPreference();
    }

    private void updateEffectsSuppressor() {
        final ComponentName suppressor = NotificationManager.from(mContext).getEffectsSuppressor();
        if (Objects.equals(suppressor, mSuppressor)) return;
        mSuppressor = suppressor;
        if (mRingOrNotificationPreference != null) {
            final String text = suppressor != null ?
                    mContext.getString(com.android.internal.R.string.muted_by,
                            getSuppressorCaption(suppressor)) : null;
            mRingOrNotificationPreference.setSuppressionText(text);
        }
        updateRingOrNotificationPreference();
    }

    private String getSuppressorCaption(ComponentName suppressor) {
        final PackageManager pm = mContext.getPackageManager();
        try {
            final ServiceInfo info = pm.getServiceInfo(suppressor, 0);
            if (info != null) {
                final CharSequence seq = info.loadLabel(pm);
                if (seq != null) {
                    final String str = seq.toString().trim();
                    if (str.length() > 0) {
                        return str;
                    }
                }
            }
        } catch (Throwable e) {
            Log.w(TAG, "Error loading suppressor caption", e);
        }
        return suppressor.getPackageName();
    }

    private final class VolumePreferenceCallback implements VolumeSeekBarPreference.Callback {
        private SeekBarVolumizer mCurrent;

        @Override
        public void onSampleStarting(SeekBarVolumizer sbv) {
            if (mCurrent != null && mCurrent != sbv) {
                mCurrent.stopSample();
            }
            mCurrent = sbv;
            if (mCurrent != null) {
                mHandler.removeMessages(H.STOP_SAMPLE);
                mHandler.sendEmptyMessageDelayed(H.STOP_SAMPLE, SAMPLE_CUTOFF);
            }
        }

        @Override
        public void onStreamValueChanged(int stream, int progress) {
            // noop
        }

        public void stopSample() {
            if (mCurrent != null) {
                mCurrent.stopSample();
            }
        }
    };


    // === Phone & notification ringtone ===

    private void initRingtones() {

	    List<SubscriptionInfo> mSubInfoList = SubscriptionManager.from(mContext).getActiveSubscriptionInfoList();
		int simCount = 0;
        if (mSubInfoList != null) {
            simCount = mSubInfoList.size();
        }
		Log.w(TAG,"initRingtones simCount = " + simCount);
        mPhoneRingtonePreference = getPreferenceScreen().findPreference(KEY_PHONE_RINGTONE);
        if (mPhoneRingtonePreference != null && !mVoiceCapable) {
            getPreferenceScreen().removePreference(mPhoneRingtonePreference);
            mPhoneRingtonePreference = null;
        }
        mNotificationRingtonePreference =
                getPreferenceScreen().findPreference(KEY_NOTIFICATION_RINGTONE);
		
		/*--prize add by liangchangwei for dualcard ringtone--*/
        mDualCard1RingtonePreference = getPreferenceScreen().findPreference(KEY_DUALCARD_PHONE_RINGTONE1);
        if (mDualCard1RingtonePreference != null && !mVoiceCapable) {
            getPreferenceScreen().removePreference(mDualCard1RingtonePreference);
            mDualCard1RingtonePreference = null;
        }
        mDualCard1NotificationRingtonePreference =
                getPreferenceScreen().findPreference(KEY_DUALCARD_NOTIFICATION1_RINGTONE);
		
        mDualCard2RingtonePreference = getPreferenceScreen().findPreference(KEY_DUALCARD_PHONE_RINGTONE2);
        if (mDualCard2RingtonePreference != null && !mVoiceCapable) {
            getPreferenceScreen().removePreference(mDualCard2RingtonePreference);
            mDualCard2RingtonePreference = null;
        }
        mDualCard2NotificationRingtonePreference =
                getPreferenceScreen().findPreference(KEY_DUALCARD_NOTIFICATION2_RINGTONE);

		if(simCount == 2){
			isDualCardInsert = true;
            getPreferenceScreen().removePreference(mPhoneRingtonePreference);
            getPreferenceScreen().removePreference(mNotificationRingtonePreference);
		    for(SubscriptionInfo SubInfo: mSubInfoList){
				if(SubInfo.getSimSlotIndex() == 0){
					mDualCard1RingtonePreference.setTitle(SubInfo.getDisplayName() + String.valueOf(SubInfo.getSimSlotIndex() + 1) +  mContext.getResources().getString(R.string.ringtone_title));
					mDualCard1NotificationRingtonePreference.setTitle(SubInfo.getDisplayName() + String.valueOf(SubInfo.getSimSlotIndex() + 1) +  mContext.getResources().getString(R.string.notification_ringtone_title));
				}else if(SubInfo.getSimSlotIndex() == 1){
					mDualCard2RingtonePreference.setTitle(SubInfo.getDisplayName() + String.valueOf(SubInfo.getSimSlotIndex() + 1) +  mContext.getResources().getString(R.string.ringtone_title));
					mDualCard2NotificationRingtonePreference.setTitle(SubInfo.getDisplayName() + String.valueOf(SubInfo.getSimSlotIndex() + 1) +  mContext.getResources().getString(R.string.notification_ringtone_title));
				}
		    }
		}else{
		    isDualCardInsert = false;
            getPreferenceScreen().removePreference(mDualCard1RingtonePreference);
            getPreferenceScreen().removePreference(mDualCard1NotificationRingtonePreference);
            getPreferenceScreen().removePreference(mDualCard2RingtonePreference);
            getPreferenceScreen().removePreference(mDualCard2NotificationRingtonePreference);
		}
		/*--prize add by liangchangwei for dualcard ringtone--*/
		
        mAlarmRingtonePreference = getPreferenceScreen().findPreference(KEY_ALARM_RINGTONE);
		getPreferenceScreen().removePreference(mAlarmRingtonePreference);
    }

    private void lookupRingtoneNames() {
        AsyncTask.execute(mLookupRingtoneNames);
    }

    private final Runnable mLookupRingtoneNames = new Runnable() {
        @Override
        public void run() {
            if(isDualCardInsert){
				if (mDualCard1RingtonePreference != null) {
					final CharSequence summary = updateRingtoneName(
							mContext, RingtoneManager.TYPE_RINGTONE);
					if (summary != null) {
						mHandler.obtainMessage(H.UPDATE_PHONE_RINGTONE, summary).sendToTarget();
					}
				}
				if (mDualCard1NotificationRingtonePreference != null) {
					final CharSequence summary = updateRingtoneName(
							mContext, RingtoneManager.TYPE_NOTIFICATION);
					if (summary != null) {
						mHandler.obtainMessage(H.UPDATE_NOTIFICATION_RINGTONE, summary).sendToTarget();
					}
				}
				if (mDualCard2RingtonePreference != null) {
					final CharSequence summary = updateRingtoneName(
							mContext, RingtoneManager.TYPE_RINGTONE2);
					if (summary != null) {
						mHandler.obtainMessage(H.UPDATE_PHONE_RINGTONE2, summary).sendToTarget();
					}
				}
				if (mDualCard2NotificationRingtonePreference != null) {
					final CharSequence summary = updateRingtoneName(
							mContext, RingtoneManager.TYPE_NOTIFICATION2);
					if (summary != null) {
						mHandler.obtainMessage(H.UPDATE_NOTIFICATION2_RINGTONE, summary).sendToTarget();
					}
				}
            }else{
				if (mPhoneRingtonePreference != null) {
					final CharSequence summary = updateRingtoneName(
							mContext, RingtoneManager.TYPE_RINGTONE);
					if (summary != null) {
						mHandler.obtainMessage(H.UPDATE_PHONE_RINGTONE, summary).sendToTarget();
					}
				}
				if (mNotificationRingtonePreference != null) {
					final CharSequence summary = updateRingtoneName(
							mContext, RingtoneManager.TYPE_NOTIFICATION);
					if (summary != null) {
						mHandler.obtainMessage(H.UPDATE_NOTIFICATION_RINGTONE, summary).sendToTarget();
					}
				}
            }
            if (mAlarmRingtonePreference != null) {
                final CharSequence summary =
                        updateRingtoneName(mContext, RingtoneManager.TYPE_ALARM);
                if (summary != null) {
                    mHandler.obtainMessage(H.UPDATE_ALARM_RINGTONE, summary).sendToTarget();
                }
            }
        }
    };

    private static CharSequence updateRingtoneName(Context context, int type) {
        if (context == null) {
            Log.e(TAG, "Unable to update ringtone name, no context provided");
            return null;
        }
        Uri ringtoneUri = RingtoneManager.getActualDefaultRingtoneUri(context, type);
        CharSequence summary = context.getString(com.android.internal.R.string.ringtone_unknown);
        // Is it a silent ringtone?
        if (ringtoneUri == null) {
            summary = context.getString(com.android.internal.R.string.ringtone_silent);
        } else {
            Cursor cursor = null;
            try {
                if (MediaStore.AUTHORITY.equals(ringtoneUri.getAuthority())) {
                    // Fetch the ringtone title from the media provider
                    cursor = context.getContentResolver().query(ringtoneUri,
                            new String[] { MediaStore.Audio.Media.TITLE }, null, null, null);
                } else if (ContentResolver.SCHEME_CONTENT.equals(ringtoneUri.getScheme())) {
                    cursor = context.getContentResolver().query(ringtoneUri,
                            new String[] { OpenableColumns.DISPLAY_NAME }, null, null, null);
                }
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        summary = cursor.getString(0);
                    }
                }
            } catch (SQLiteException sqle) {
                // Unknown title for the ringtone
            } catch (IllegalArgumentException iae) {
                // Some other error retrieving the column from the provider
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        return summary;
    }

    // === Vibrate when ringing ===

    private void initVibrateWhenRinging() {
        mVibrateWhenRinging =
                (TwoStatePreference) getPreferenceScreen().findPreference(KEY_VIBRATE_WHEN_RINGING);
        if (mVibrateWhenRinging == null) {
            Log.i(TAG, "Preference not found: " + KEY_VIBRATE_WHEN_RINGING);
            return;
        }
        if (!mVoiceCapable) {
            getPreferenceScreen().removePreference(mVibrateWhenRinging);
            mVibrateWhenRinging = null;
            return;
        }
        mVibrateWhenRinging.setPersistent(false);
        updateVibrateWhenRinging();
        mVibrateWhenRinging.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                final boolean val = (Boolean) newValue;
                return Settings.System.putInt(getContentResolver(),
                        Settings.System.VIBRATE_WHEN_RINGING,
                        val ? 1 : 0);
            }
        });
        //prize add by xiarui 2018-06-28 start @{
        if (PrizeOption.PRIZE_VIBRATE_CONTROL) {
            mVibrateWhenRinging.setTitle(R.string.prize_vibrate_when_ringing_title);
            mVibrateWhenRinging.setOrder(0);
        }
        //@}
    }

    private void updateVibrateWhenRinging() {
        if (mVibrateWhenRinging == null) return;
        mVibrateWhenRinging.setChecked(Settings.System.getInt(getContentResolver(),
                Settings.System.VIBRATE_WHEN_RINGING, 0) != 0);
    }

    // === Callbacks ===

    private final class SettingsObserver extends ContentObserver {
        private final Uri VIBRATE_WHEN_RINGING_URI =
                Settings.System.getUriFor(Settings.System.VIBRATE_WHEN_RINGING);

        public SettingsObserver() {
            super(mHandler);
        }

        public void register(boolean register) {
            final ContentResolver cr = getContentResolver();
            if (register) {
                cr.registerContentObserver(VIBRATE_WHEN_RINGING_URI, false, this);
            } else {
                cr.unregisterContentObserver(this);
            }
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);
            if (VIBRATE_WHEN_RINGING_URI.equals(uri)) {
                updateVibrateWhenRinging();
            }
        }
    }

    private final class H extends Handler {
        private static final int UPDATE_PHONE_RINGTONE = 1;
        private static final int UPDATE_NOTIFICATION_RINGTONE = 2;
        private static final int STOP_SAMPLE = 3;
        private static final int UPDATE_EFFECTS_SUPPRESSOR = 4;
        private static final int UPDATE_RINGER_MODE = 5;
        private static final int UPDATE_ALARM_RINGTONE = 6;
        private static final int UPDATE_PHONE_RINGTONE2 = 7;
        private static final int UPDATE_NOTIFICATION2_RINGTONE = 8;

        private H() {
            super(Looper.getMainLooper());
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_PHONE_RINGTONE:
					if(isDualCardInsert){
						mDualCard1RingtonePreference.setSummary((CharSequence) msg.obj);
					}else{
						mPhoneRingtonePreference.setSummary((CharSequence) msg.obj);
					}
                    Settings.System.putString(getContentResolver(), Settings.System.PRIZE_RINGTONE_NAME, (String) msg.obj);
                    break;
				case UPDATE_PHONE_RINGTONE2:
					mDualCard2RingtonePreference.setSummary((CharSequence) msg.obj);
					break;
                case UPDATE_NOTIFICATION_RINGTONE:
					if(isDualCardInsert){
						mDualCard1NotificationRingtonePreference.setSummary((CharSequence) msg.obj);
					}else{
						mNotificationRingtonePreference.setSummary((CharSequence) msg.obj);
					}
                    break;
				case UPDATE_NOTIFICATION2_RINGTONE:
					mDualCard2NotificationRingtonePreference.setSummary((CharSequence) msg.obj);
					break;
                case STOP_SAMPLE:
                    mVolumeCallback.stopSample();
                    break;
                case UPDATE_EFFECTS_SUPPRESSOR:
                    updateEffectsSuppressor();
                    break;
                case UPDATE_RINGER_MODE:
                    updateRingerMode();
                    break;
                case UPDATE_ALARM_RINGTONE:
                    mAlarmRingtonePreference.setSummary((CharSequence) msg.obj);
                    break;
            }
        }
    }

    private class Receiver extends BroadcastReceiver {
        private boolean mRegistered;

        public void register(boolean register) {
            if (mRegistered == register) return;
            if (register) {
                final IntentFilter filter = new IntentFilter();
                filter.addAction(NotificationManager.ACTION_EFFECTS_SUPPRESSOR_CHANGED);
                filter.addAction(AudioManager.INTERNAL_RINGER_MODE_CHANGED_ACTION);
                mContext.registerReceiver(this, filter);
            } else {
                mContext.unregisterReceiver(this);
            }
            mRegistered = register;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (NotificationManager.ACTION_EFFECTS_SUPPRESSOR_CHANGED.equals(action)) {
                mHandler.sendEmptyMessage(H.UPDATE_EFFECTS_SUPPRESSOR);
            } else if (AudioManager.INTERNAL_RINGER_MODE_CHANGED_ACTION.equals(action)) {
                mHandler.sendEmptyMessage(H.UPDATE_RINGER_MODE);
            }
        }
    }

    // === Summary ===

    private static class SummaryProvider extends BroadcastReceiver
            implements SummaryLoader.SummaryProvider {

        private final Context mContext;
        private final AudioManager mAudioManager;
        private final SummaryLoader mSummaryLoader;
        private final int maxVolume;

        public SummaryProvider(Context context, SummaryLoader summaryLoader) {
            mContext = context;
            mSummaryLoader = summaryLoader;
            mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
            maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_RING);
        }

        @Override
        public void setListening(boolean listening) {
            if (listening) {
                IntentFilter filter = new IntentFilter();
                filter.addAction(AudioManager.VOLUME_CHANGED_ACTION);
                filter.addAction(AudioManager.STREAM_DEVICES_CHANGED_ACTION);
                filter.addAction(AudioManager.RINGER_MODE_CHANGED_ACTION);
                filter.addAction(AudioManager.INTERNAL_RINGER_MODE_CHANGED_ACTION);
                filter.addAction(AudioManager.STREAM_MUTE_CHANGED_ACTION);
                filter.addAction(NotificationManager.ACTION_EFFECTS_SUPPRESSOR_CHANGED);
                mContext.registerReceiver(this, filter);
            } else {
                mContext.unregisterReceiver(this);
            }
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String percent =  NumberFormat.getPercentInstance().format(
                    (double) mAudioManager.getStreamVolume(AudioManager.STREAM_RING) / maxVolume);
            mSummaryLoader.setSummary(this,
                    mContext.getString(R.string.sound_settings_summary, percent));
        }
    }

    public static final SummaryLoader.SummaryProviderFactory SUMMARY_PROVIDER_FACTORY
            = new SummaryLoader.SummaryProviderFactory() {
        @Override
        public SummaryLoader.SummaryProvider createSummaryProvider(Activity activity,
                SummaryLoader summaryLoader) {
            return new SummaryProvider(activity, summaryLoader);
        }
    };

    // === Indexing ===

    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {

        public List<SearchIndexableResource> getXmlResourcesToIndex(
                Context context, boolean enabled) {
            final SearchIndexableResource sir = new SearchIndexableResource(context);
            sir.xmlResId = R.xml.sound_settings;
            return Arrays.asList(sir);
        }

        public List<String> getNonIndexableKeys(Context context) {
            final ArrayList<String> rt = new ArrayList<String>();
            if (Utils.isVoiceCapable(context)) {
                rt.add(KEY_NOTIFICATION_VOLUME);
            } else {
                rt.add(KEY_RING_VOLUME);
                rt.add(KEY_PHONE_RINGTONE);
               // rt.add(KEY_WIFI_DISPLAY);
                rt.add(KEY_VIBRATE_WHEN_RINGING);
            }

            final PackageManager pm = context.getPackageManager();
            final UserManager um = (UserManager) context.getSystemService(Context.USER_SERVICE);

            // Enable link to CMAS app settings depending on the value in config.xml.
            boolean isCellBroadcastAppLinkEnabled = context.getResources().getBoolean(
                    com.android.internal.R.bool.config_cellBroadcastAppLinks);
            try {
                if (isCellBroadcastAppLinkEnabled) {
                    if (pm.getApplicationEnabledSetting("com.android.cellbroadcastreceiver")
                            == PackageManager.COMPONENT_ENABLED_STATE_DISABLED) {
                        isCellBroadcastAppLinkEnabled = false;  // CMAS app disabled
                    }
                }
            } catch (IllegalArgumentException ignored) {
                isCellBroadcastAppLinkEnabled = false;  // CMAS app not installed
            }
            if (!um.isAdminUser() || !isCellBroadcastAppLinkEnabled) {
                rt.add(KEY_CELL_BROADCAST_SETTINGS);
            }
             rt.add("other_sounds");
             rt.add("sound_enhancement");
            return rt;
        }
    };
    /* prize-add-by-lijimeng-for vibrate when silent-20180604-start*/
    private void initVibrateWhenSilent(){
        mVibrateWhenSilent = (SwitchPreference) getPreferenceScreen().findPreference(KEY_VIBRATE_WHEN_SILENT);
        if (mVibrateWhenSilent == null) {
            Log.i(TAG, "Preference not found: " + KEY_VIBRATE_WHEN_SILENT);
            return;
        }
        if (!mVoiceCapable) {
            getPreferenceScreen().removePreference(mVibrateWhenSilent);
            mVibrateWhenSilent = null;
            return;
        }
        if (mVibrateWhenSilent == null) {
            return;
        }
        mVibrateWhenSilent.setChecked(Settings.System.getInt(getContentResolver(),
                Settings.System.PRIZE_VIBRATE_IN_SILENT, 0) != 0);
        mVibrateWhenSilent.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                final boolean val = (Boolean) newValue;
                return Settings.System.putInt(getContentResolver(),
                        Settings.System.PRIZE_VIBRATE_IN_SILENT,
                        val ? 1 : 0);
            }
        });

        //prize add by xiarui 2018-06-28 start @{
        if (!PrizeOption.PRIZE_VIBRATE_CONTROL) {
            getPreferenceScreen().removePreference(mVibrateWhenSilent);
        }
        //@}
    }
     /* prize-add-by-lijimeng-for vibrate when silent-20180604-end*/
}
