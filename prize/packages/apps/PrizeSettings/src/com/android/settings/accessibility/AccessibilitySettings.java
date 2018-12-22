/*
 * Copyright (C) 2009 The Android Open Source Project
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

package com.android.settings.accessibility;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.os.UserHandle;
import android.provider.SearchIndexableResource;
import android.provider.Settings;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceScreen;
import android.text.TextUtils;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityManager;

import com.android.internal.content.PackageMonitor;
import com.android.internal.logging.MetricsProto.MetricsEvent;
import com.android.internal.view.RotationPolicy;
import com.android.internal.view.RotationPolicy.RotationPolicyListener;
import com.android.settings.DialogCreatable;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.search.SearchIndexableRaw;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.RestrictedLockUtils.EnforcedAdmin;
import com.android.settingslib.RestrictedPreference;
import com.android.settingslib.accessibility.AccessibilityUtils;

import com.mediatek.settings.FeatureOption;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**prize-add-game-reading-mode-liup-20150420-start*/
import android.view.WindowManager;
import android.content.Intent;
import com.mediatek.common.prizeoption.PrizeOption;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.util.Log;
/**prize-add-game-reading-mode-liup-20150420-end*/
/**prize-add-game-reading-mode-liuweiquan-20160123-start*/
import android.app.NotificationManager;
import android.app.Notification;
import android.app.PendingIntent;
/**prize-add-game-reading-mode-liuweiquan-20160123-end*/
/**
 * Activity with the accessibility settings.
 */
public class AccessibilitySettings extends SettingsPreferenceFragment implements DialogCreatable,
        Preference.OnPreferenceChangeListener, Indexable {

    // Preference categories
    private static final String SERVICES_CATEGORY = "services_category";
    private static final String SYSTEM_CATEGORY = "system_category";
	/**prize-remove-display-category-huangdianjun-20150724-start*/
    private static final String DISPLAY_CATEGORY = "display_category";
    /**prize-remove-display-category-huangdianjun-20150724-end*/

    // Preferences
	/**prize-add-game-reading-mode-liup-20150420-start*/
    private static final String PRIZE_GAME_MODE =
            "prize_game_mode_preference";
	private static final String PRIZE_READING_MODE =
            "prize_reading_mode_preference";
	/**prize-add-game-reading-mode-liup-20150420-end*/
	/**prize-add-game-reading-mode-liuweiquan-20160123-start*/
	public static final int GameModeNotificationID = 3;
	public static final int ReadingModeNotificationID = 2;
	/**prize-add-game-reading-mode-liuweiquan-20160123-end*/
    private static final String TOGGLE_HIGH_TEXT_CONTRAST_PREFERENCE =
            "toggle_high_text_contrast_preference";
    private static final String TOGGLE_INVERSION_PREFERENCE =
            "toggle_inversion_preference";
    private static final String TOGGLE_POWER_BUTTON_ENDS_CALL_PREFERENCE =
            "toggle_power_button_ends_call_preference";
    private static final String TOGGLE_LOCK_SCREEN_ROTATION_PREFERENCE =
            "toggle_lock_screen_rotation_preference";
    private static final String TOGGLE_SPEAK_PASSWORD_PREFERENCE =
            "toggle_speak_password_preference";
    private static final String TOGGLE_LARGE_POINTER_ICON =
            "toggle_large_pointer_icon";
    private static final String TOGGLE_MASTER_MONO =
            "toggle_master_mono";
    private static final String SELECT_LONG_PRESS_TIMEOUT_PREFERENCE =
            "select_long_press_timeout_preference";
    private static final String ENABLE_ACCESSIBILITY_GESTURE_PREFERENCE_SCREEN =
            "enable_global_gesture_preference_screen";
    private static final String CAPTIONING_PREFERENCE_SCREEN =
            "captioning_preference_screen";
    private static final String DISPLAY_MAGNIFICATION_PREFERENCE_SCREEN =
            "screen_magnification_preference_screen";
    private static final String FONT_SIZE_PREFERENCE_SCREEN =
            "font_size_preference_screen";
    private static final String AUTOCLICK_PREFERENCE_SCREEN =
            "autoclick_preference_screen";
    private static final String DISPLAY_DALTONIZER_PREFERENCE_SCREEN =
            "daltonizer_preference_screen";
    /// M: MTK add ipo settings
    private static final String IPO_SETTING_PREFERENCE = "ipo_setting";

    // Extras passed to sub-fragments.
    static final String EXTRA_PREFERENCE_KEY = "preference_key";
    static final String EXTRA_CHECKED = "checked";
    static final String EXTRA_TITLE = "title";
    static final String EXTRA_SUMMARY = "summary";
    static final String EXTRA_SETTINGS_TITLE = "settings_title";
    static final String EXTRA_COMPONENT_NAME = "component_name";
    static final String EXTRA_SETTINGS_COMPONENT_NAME = "settings_component_name";

    // Timeout before we update the services if packages are added/removed
    // since the AccessibilityManagerService has to do that processing first
    // to generate the AccessibilityServiceInfo we need for proper
    // presentation.
    private static final long DELAY_UPDATE_SERVICES_MILLIS = 1000;

    // Auxiliary members.
    static final Set<ComponentName> sInstalledServices = new HashSet<>();

    private final Map<String, String> mLongPressTimeoutValuetoTitleMap = new HashMap<>();

    private final Handler mHandler = new Handler();

    private final Runnable mUpdateRunnable = new Runnable() {
        @Override
        public void run() {
            if (getActivity() != null) {
                updateServicesPreferences();
            }
        }
    };

    private final PackageMonitor mSettingsPackageMonitor = new PackageMonitor() {
        @Override
        public void onPackageAdded(String packageName, int uid) {
            sendUpdate();
        }

        @Override
        public void onPackageAppeared(String packageName, int reason) {
            sendUpdate();
        }

        @Override
        public void onPackageDisappeared(String packageName, int reason) {
            sendUpdate();
        }

        @Override
        public void onPackageRemoved(String packageName, int uid) {
            sendUpdate();
        }

        private void sendUpdate() {
            mHandler.postDelayed(mUpdateRunnable, DELAY_UPDATE_SERVICES_MILLIS);
        }
    };

    private final SettingsContentObserver mSettingsContentObserver =
            new SettingsContentObserver(mHandler) {
                @Override
                public void onChange(boolean selfChange, Uri uri) {
                    updateServicesPreferences();
                    /// M: Also need update system preference(ex: color invertion changed)
                    /// so change to update all preferences
                    updateAllPreferences();
                }
            };

    private final RotationPolicyListener mRotationPolicyListener = new RotationPolicyListener() {
        @Override
        public void onChange() {
            updateLockScreenRotationCheckbox();
        }
    };
	
	/**prize-add-refresh-liup-20150624-start*/
	BroadcastReceiver mFloatWindowReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final boolean bPrizeGameMode = Settings.System.getInt(getContentResolver(),Settings.System.PRIZE_GAME_MODE,0) == 1;
			mPrizeGameMode.setChecked(bPrizeGameMode);
			final boolean bPrizeReadingMode = Settings.System.getInt(getContentResolver(),Settings.System.PRIZE_READING_MODE,0) == 1;
            mPrizeReadingMode.setChecked(bPrizeReadingMode);
		}
    };
	/**prize-add-refresh-liup-20150624-end*/
    // Preference controls.
    private PreferenceCategory mServicesCategory;
    private PreferenceCategory mSystemsCategory;
	/**prize-remove-display-category-huangdianjun-20150724-start*/
    private PreferenceCategory mDisplayCategory;
    /**prize-remove-display-category-huangdianjun-20150724-end*/

	/**prize-add-game-reading-mode-liup-20150420-start*/	
	private SwitchPreference mPrizeGameMode;
	private SwitchPreference mPrizeReadingMode;
	/**prize-add-game-reading-mode-liup-20150420-end*/	
	
    private SwitchPreference mToggleLargeTextPreference;
    private SwitchPreference mToggleHighTextContrastPreference;
    private SwitchPreference mTogglePowerButtonEndsCallPreference;
    private SwitchPreference mToggleLockScreenRotationPreference;
    private SwitchPreference mToggleSpeakPasswordPreference;
    private SwitchPreference mToggleLargePointerIconPreference;
    private SwitchPreference mToggleMasterMonoPreference;
    private ListPreference mSelectLongPressTimeoutPreference;
    private Preference mNoServicesMessagePreference;
    private PreferenceScreen mCaptioningPreferenceScreen;
    private PreferenceScreen mDisplayMagnificationPreferenceScreen;
    private PreferenceScreen mFontSizePreferenceScreen;
    private PreferenceScreen mAutoclickPreferenceScreen;
    private PreferenceScreen mGlobalGesturePreferenceScreen;
    private PreferenceScreen mDisplayDaltonizerPreferenceScreen;
    private SwitchPreference mToggleInversionPreference;
    /// M: IPO preference
    private SwitchPreference mIpoSetting;

    private int mLongPressTimeoutDefault;

    private DevicePolicyManager mDpm;

    @Override
    protected int getMetricsCategory() {
        return MetricsEvent.ACCESSIBILITY;
    }

    @Override
    protected int getHelpResource() {
        return R.string.help_uri_accessibility;
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.accessibility_settings);
        initializeAllPreferences();
        mDpm = (DevicePolicyManager) (getActivity()
                .getSystemService(Context.DEVICE_POLICY_SERVICE));
    }

    @Override
    public void onResume() {
        super.onResume();
        updateAllPreferences();
		/**prize-add-refresh-liup-20150624-start*/
		IntentFilter filter = new IntentFilter();
        filter.addAction("accessibility.prize.onoff");
        getActivity().registerReceiver(mFloatWindowReceiver, filter);
		/**prize-add-refresh-liup-20150624-end*/
        mSettingsPackageMonitor.register(getActivity(), getActivity().getMainLooper(), false);
        mSettingsContentObserver.register(getContentResolver());
        if (RotationPolicy.isRotationSupported(getActivity())) {
            RotationPolicy.registerRotationPolicyListener(getActivity(),
                    mRotationPolicyListener);
        }
    }

    @Override
    public void onPause() {
		getActivity().unregisterReceiver(mFloatWindowReceiver);//prize-add-refresh-liup-20150624
        mSettingsPackageMonitor.unregister();
        mSettingsContentObserver.unregister(getContentResolver());
        if (RotationPolicy.isRotationSupported(getActivity())) {
            RotationPolicy.unregisterRotationPolicyListener(getActivity(),
                    mRotationPolicyListener);
        }
        super.onPause();
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (mSelectLongPressTimeoutPreference == preference) {
            handleLongPressTimeoutPreferenceChange((String) newValue);
            return true;
        } else if (mToggleInversionPreference == preference) {
            handleToggleInversionPreferenceChange((Boolean) newValue);
            return true;
        } else if (mIpoSetting == preference) {
            /** M: mtk add ipo settings @{ */
            Settings.System.putInt(getContentResolver(), Settings.System.IPO_SETTING,
                    (Boolean) newValue ? 1 : 0);
                return true;
             /** @} */
        }
        return false;
    }

    private void handleLongPressTimeoutPreferenceChange(String stringValue) {
        Settings.Secure.putInt(getContentResolver(),
                Settings.Secure.LONG_PRESS_TIMEOUT, Integer.parseInt(stringValue));
        mSelectLongPressTimeoutPreference.setSummary(
                mLongPressTimeoutValuetoTitleMap.get(stringValue));
    }

    private void handleToggleInversionPreferenceChange(boolean checked) {
        Settings.Secure.putInt(getContentResolver(),
                Settings.Secure.ACCESSIBILITY_DISPLAY_INVERSION_ENABLED, (checked ? 1 : 0));
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
		/**prize-add-game-reading-mode-liup-20150420-start*/
        if (mPrizeGameMode == preference) {
            handlePrizeGameModePreferenceClick();
            return true;
        } else if (mPrizeReadingMode == preference) {
            handlePrizeReadingModePreferenceClick();
            return true;
        } 
		else 
		/**prize-add-game-reading-mode-liup-20150420-start*/
        if (mToggleHighTextContrastPreference == preference) {
            handleToggleTextContrastPreferenceClick();
            return true;
        } else if (mTogglePowerButtonEndsCallPreference == preference) {
            handleTogglePowerButtonEndsCallPreferenceClick();
            return true;
        } else if (mToggleLockScreenRotationPreference == preference) {
            handleLockScreenRotationPreferenceClick();
            return true;
        } else if (mToggleSpeakPasswordPreference == preference) {
            handleToggleSpeakPasswordPreferenceClick();
            return true;
        } else if (mToggleLargePointerIconPreference == preference) {
            handleToggleLargePointerIconPreferenceClick();
            return true;
        } else if (mToggleMasterMonoPreference == preference) {
            handleToggleMasterMonoPreferenceClick();
            return true;
        } else if (mGlobalGesturePreferenceScreen == preference) {
            handleToggleEnableAccessibilityGesturePreferenceClick();
            return true;
        } else if (mDisplayMagnificationPreferenceScreen == preference) {
            handleDisplayMagnificationPreferenceScreenClick();
            return true;
        }
        return super.onPreferenceTreeClick(preference);
    }
	/**prize-add-game-reading-mode-liuweiquan-20160123-start*/
	private void addIconToStatusbar(int iconID,int titleID,int summaryID,int notificationID){ 
			NotificationManager notificationManager =(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
      String titleStr = getString(titleID);
      String summaryStr = getString(summaryID);
      Notification notification = new Notification();
      notification.icon = iconID;
      notification.when = 0;
      notification.flags |= Notification.FLAG_ONGOING_EVENT;
			notification.flags |= Notification.FLAG_NO_CLEAR;
			notification.flags |= Notification.FLAG_KEEP_NOTIFICATION_ICON;
			
      notification.tickerText = titleStr;
      notification.defaults = 0; // please be quiet
      notification.sound = null;
      notification.vibrate = null;
      notification.priority = Notification.PRIORITY_DEFAULT;
      //Intent intent = new Intent(getActivity().getBaseContext(),com.android.settings.Settings.class); 
	  Intent intent = new Intent();
      intent.setAction("android.settings.ACCESSIBILITY_SETTINGS");
      PendingIntent pendingIntent = PendingIntent.getActivity(getActivity(), 0, intent, 0);
      notification.setLatestEventInfo(getActivity(), titleStr, summaryStr, pendingIntent);     
      notificationManager.notify(notificationID, notification);
	}

	private void deleteIconToStatusbar(int notificationID){ 
      NotificationManager notifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
      notifyManager.cancel(notificationID);
  } 
  /**prize-add-game-reading-mode-liuweiquan-20160123-end*/ 
	/**prize-add-game-reading-mode-liup-20150420-start*/
	private void handlePrizeGameModePreferenceClick() {
		if(mPrizeGameMode.isChecked()){
			addIconToStatusbar(R.drawable.game_mode,R.string.prize_game_mode_title,R.string.prize_game_mode_notification_summary,GameModeNotificationID);
		}else{
			deleteIconToStatusbar(GameModeNotificationID);
		}
		Settings.System.putInt(getContentResolver(),Settings.System.PRIZE_GAME_MODE,mPrizeGameMode.isChecked() ? 1 : 0);
	}
	private void handlePrizeReadingModePreferenceClick() {
		if(mPrizeReadingMode.isChecked()){
			Log.e("liup","isChecked");
			addIconToStatusbar(R.drawable.reading_mode,R.string.prize_reading_mode_title,R.string.prize_reading_mode_notification_summary,ReadingModeNotificationID);
			getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		}else{
			deleteIconToStatusbar(ReadingModeNotificationID);
			getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		}
		
		Settings.System.putInt(getContentResolver(),Settings.System.PRIZE_READING_MODE,mPrizeReadingMode.isChecked() ? 1 : 0);
	}

    private void handleToggleTextContrastPreferenceClick() {
        Settings.Secure.putInt(getContentResolver(),
                Settings.Secure.ACCESSIBILITY_HIGH_TEXT_CONTRAST_ENABLED,
                (mToggleHighTextContrastPreference.isChecked() ? 1 : 0));
    }

    private void handleTogglePowerButtonEndsCallPreferenceClick() {
        Settings.Secure.putInt(getContentResolver(),
                Settings.Secure.INCALL_POWER_BUTTON_BEHAVIOR,
                (mTogglePowerButtonEndsCallPreference.isChecked()
                        ? Settings.Secure.INCALL_POWER_BUTTON_BEHAVIOR_HANGUP
                        : Settings.Secure.INCALL_POWER_BUTTON_BEHAVIOR_SCREEN_OFF));
    }

    private void handleLockScreenRotationPreferenceClick() {
        RotationPolicy.setRotationLockForAccessibility(getActivity(),
                !mToggleLockScreenRotationPreference.isChecked());
    }

    private void handleToggleSpeakPasswordPreferenceClick() {
        Settings.Secure.putInt(getContentResolver(),
                Settings.Secure.ACCESSIBILITY_SPEAK_PASSWORD,
                mToggleSpeakPasswordPreference.isChecked() ? 1 : 0);
    }

    private void handleToggleLargePointerIconPreferenceClick() {
        Settings.Secure.putInt(getContentResolver(),
                Settings.Secure.ACCESSIBILITY_LARGE_POINTER_ICON,
                mToggleLargePointerIconPreference.isChecked() ? 1 : 0);
    }

    private void handleToggleMasterMonoPreferenceClick() {
        Settings.System.putIntForUser(getContentResolver(), Settings.System.MASTER_MONO,
                mToggleMasterMonoPreference.isChecked() ? 1 : 0, UserHandle.USER_CURRENT);
    }

    private void handleToggleEnableAccessibilityGesturePreferenceClick() {
        Bundle extras = mGlobalGesturePreferenceScreen.getExtras();
        extras.putString(EXTRA_TITLE, getString(
                R.string.accessibility_global_gesture_preference_title));
        extras.putString(EXTRA_SUMMARY, getString(
                R.string.accessibility_global_gesture_preference_description));
        extras.putBoolean(EXTRA_CHECKED, Settings.Global.getInt(getContentResolver(),
                Settings.Global.ENABLE_ACCESSIBILITY_GLOBAL_GESTURE_ENABLED, 0) == 1);
        super.onPreferenceTreeClick(mGlobalGesturePreferenceScreen);
    }

    private void handleDisplayMagnificationPreferenceScreenClick() {
        Bundle extras = mDisplayMagnificationPreferenceScreen.getExtras();
        extras.putString(EXTRA_TITLE, getString(
                R.string.accessibility_screen_magnification_title));
        extras.putCharSequence(EXTRA_SUMMARY, getActivity().getResources().getText(
                R.string.accessibility_screen_magnification_summary));
        extras.putBoolean(EXTRA_CHECKED, Settings.Secure.getInt(getContentResolver(),
                Settings.Secure.ACCESSIBILITY_DISPLAY_MAGNIFICATION_ENABLED, 0) == 1);
        super.onPreferenceTreeClick(mDisplayMagnificationPreferenceScreen);
    }

    private void initializeAllPreferences() {
        mServicesCategory = (PreferenceCategory) findPreference(SERVICES_CATEGORY);
        mSystemsCategory = (PreferenceCategory) findPreference(SYSTEM_CATEGORY);
        /**prize-remove-display-category-huangdianjun-20150724-start*/
        mDisplayCategory = (PreferenceCategory) findPreference(DISPLAY_CATEGORY);
        
        /**prize-remove-display-category-huangdianjun-20150724-end*/

	 /**prize-add-game-reading-mode-liup-20150420-start*/	
	 mPrizeGameMode = (SwitchPreference) findPreference(PRIZE_GAME_MODE);
	 mPrizeReadingMode = (SwitchPreference) findPreference(PRIZE_READING_MODE);
	 if(!PrizeOption.PRIZE_GAME_MODE){
		 mSystemsCategory.removePreference(mPrizeGameMode);	 
	 }
	 if(!PrizeOption.PRIZE_READING_MODE){
		 mSystemsCategory.removePreference(mPrizeReadingMode);	 
	 }
	 /**prize-add-game-reading-mode-liup-20150420-end*/
        // Text contrast.
        mToggleHighTextContrastPreference =
                (SwitchPreference) findPreference(TOGGLE_HIGH_TEXT_CONTRAST_PREFERENCE);

        // Display inversion.
        mToggleInversionPreference = (SwitchPreference) findPreference(TOGGLE_INVERSION_PREFERENCE);
		/**prize-remove_toggle_inversion-huangdianjun-20150724-start*/
        mDisplayCategory.removePreference(mToggleInversionPreference);
        /**prize-remove_toggle_inversion-huangdianjun-20150724-end*/
        mToggleInversionPreference.setOnPreferenceChangeListener(this);

        // Power button ends calls.
        mTogglePowerButtonEndsCallPreference =
                (SwitchPreference) findPreference(TOGGLE_POWER_BUTTON_ENDS_CALL_PREFERENCE);
        if (!KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_POWER)
                || !Utils.isVoiceCapable(getActivity())) {
            mSystemsCategory.removePreference(mTogglePowerButtonEndsCallPreference);
        }

        // Lock screen rotation.
        mToggleLockScreenRotationPreference =
                (SwitchPreference) findPreference(TOGGLE_LOCK_SCREEN_ROTATION_PREFERENCE);
        if (!RotationPolicy.isRotationSupported(getActivity())) {
            mSystemsCategory.removePreference(mToggleLockScreenRotationPreference);
        }else{
			mSystemsCategory.removePreference(mToggleLockScreenRotationPreference);
		}

        // Speak passwords.
        mToggleSpeakPasswordPreference =
                (SwitchPreference) findPreference(TOGGLE_SPEAK_PASSWORD_PREFERENCE);

        // Large pointer icon.
        mToggleLargePointerIconPreference =
                (SwitchPreference) findPreference(TOGGLE_LARGE_POINTER_ICON);

        // Master Mono
        mToggleMasterMonoPreference =
                (SwitchPreference) findPreference(TOGGLE_MASTER_MONO);

        // Long press timeout.
        mSelectLongPressTimeoutPreference =
                (ListPreference) findPreference(SELECT_LONG_PRESS_TIMEOUT_PREFERENCE);
        mSelectLongPressTimeoutPreference.setOnPreferenceChangeListener(this);
        if (mLongPressTimeoutValuetoTitleMap.size() == 0) {
            String[] timeoutValues = getResources().getStringArray(
                    R.array.long_press_timeout_selector_values);
            mLongPressTimeoutDefault = Integer.parseInt(timeoutValues[0]);
            String[] timeoutTitles = getResources().getStringArray(
                    R.array.long_press_timeout_selector_titles);
            final int timeoutValueCount = timeoutValues.length;
            for (int i = 0; i < timeoutValueCount; i++) {
                mLongPressTimeoutValuetoTitleMap.put(timeoutValues[i], timeoutTitles[i]);
            }
        }

        // Captioning.
        mCaptioningPreferenceScreen = (PreferenceScreen) findPreference(
                CAPTIONING_PREFERENCE_SCREEN);

        // Display magnification.
        mDisplayMagnificationPreferenceScreen = (PreferenceScreen) findPreference(
                DISPLAY_MAGNIFICATION_PREFERENCE_SCREEN);

        // Font size.
        mFontSizePreferenceScreen = (PreferenceScreen) findPreference(
                FONT_SIZE_PREFERENCE_SCREEN);
		if(mFontSizePreferenceScreen != null){
			 mSystemsCategory.removePreference(mFontSizePreferenceScreen);	 
		}

        // Autoclick after pointer stops.
        mAutoclickPreferenceScreen = (PreferenceScreen) findPreference(
                AUTOCLICK_PREFERENCE_SCREEN);

        // Display color adjustments.
        mDisplayDaltonizerPreferenceScreen = (PreferenceScreen) findPreference(
                DISPLAY_DALTONIZER_PREFERENCE_SCREEN);

        // Global gesture.
        mGlobalGesturePreferenceScreen =
                (PreferenceScreen) findPreference(ENABLE_ACCESSIBILITY_GESTURE_PREFERENCE_SCREEN);
		/**prize-huangdianjun-20150511-start*/
             mSystemsCategory.removePreference(mGlobalGesturePreferenceScreen);
        /**prize-huangdianjun-20150511-end*/
        final int longPressOnPowerBehavior = getActivity().getResources().getInteger(
                com.android.internal.R.integer.config_longPressOnPowerBehavior);
        final int LONG_PRESS_POWER_GLOBAL_ACTIONS = 1;
        if (!KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_POWER)
                || longPressOnPowerBehavior != LONG_PRESS_POWER_GLOBAL_ACTIONS) {
            // Remove accessibility shortcut if power key is not present
            // nor long press power does not show global actions menu.
            mSystemsCategory.removePreference(mGlobalGesturePreferenceScreen);
        }

        /// M: IPO settings
        mIpoSetting = (SwitchPreference) findPreference(IPO_SETTING_PREFERENCE);
        mIpoSetting.setOnPreferenceChangeListener(this);
		//modify liup 20160606 remove IPO menu start
		mSystemsCategory.removePreference(mIpoSetting);
		/*
        if (!FeatureOption.MTK_IPO_SUPPORT || UserHandle.myUserId() != UserHandle.USER_OWNER) {
            mSystemsCategory.removePreference(mIpoSetting);
        }
		*/
		//modify liup 20160606 remove IPO menu end
    }

    private void updateAllPreferences() {
        updateServicesPreferences();
        updateSystemPreferences();
    }

    private void updateServicesPreferences() {
        // Since services category is auto generated we have to do a pass
        // to generate it since services can come and go and then based on
        // the global accessibility state to decided whether it is enabled.

        // Generate.
        mServicesCategory.removeAll();

        AccessibilityManager accessibilityManager = AccessibilityManager.getInstance(getActivity());

        List<AccessibilityServiceInfo> installedServices =
                accessibilityManager.getInstalledAccessibilityServiceList();
        Set<ComponentName> enabledServices = AccessibilityUtils.getEnabledServicesFromSettings(
                getActivity());
        List<String> permittedServices = mDpm.getPermittedAccessibilityServices(
                UserHandle.myUserId());
        final boolean accessibilityEnabled = Settings.Secure.getInt(getContentResolver(),
                Settings.Secure.ACCESSIBILITY_ENABLED, 0) == 1;

        for (int i = 0, count = installedServices.size(); i < count; ++i) {
            AccessibilityServiceInfo info = installedServices.get(i);

            RestrictedPreference preference = new RestrictedPreference(getActivity());
			preference.setWidgetLayoutResource(R.layout.dynamic_preference_widget_right_arrow);
            String title = info.getResolveInfo().loadLabel(getPackageManager()).toString();

            ServiceInfo serviceInfo = info.getResolveInfo().serviceInfo;
            ComponentName componentName = new ComponentName(serviceInfo.packageName,
                    serviceInfo.name);

            preference.setKey(componentName.flattenToString());

            preference.setTitle(title);
            final boolean serviceEnabled = accessibilityEnabled
                    && enabledServices.contains(componentName);
            String serviceEnabledString;
            if (serviceEnabled) {
                serviceEnabledString = getString(R.string.accessibility_feature_state_on);
            } else {
                serviceEnabledString = getString(R.string.accessibility_feature_state_off);
            }

            // Disable all accessibility services that are not permitted.
            String packageName = serviceInfo.packageName;
            boolean serviceAllowed =
                    permittedServices == null || permittedServices.contains(packageName);
            if (!serviceAllowed && !serviceEnabled) {
                EnforcedAdmin admin = RestrictedLockUtils.checkIfAccessibilityServiceDisallowed(
                        getActivity(), serviceInfo.packageName, UserHandle.myUserId());
                if (admin != null) {
                    preference.setDisabledByAdmin(admin);
                } else {
                    preference.setEnabled(false);
                }
            } else {
                preference.setEnabled(true);
            }

            preference.setSummary(serviceEnabledString);

            preference.setOrder(i);
            preference.setFragment(ToggleAccessibilityServicePreferenceFragment.class.getName());
            preference.setPersistent(true);

            Bundle extras = preference.getExtras();
            extras.putString(EXTRA_PREFERENCE_KEY, preference.getKey());
            extras.putBoolean(EXTRA_CHECKED, serviceEnabled);
            extras.putString(EXTRA_TITLE, title);

            String description = info.loadDescription(getPackageManager());
            if (TextUtils.isEmpty(description)) {
                description = getString(R.string.accessibility_service_default_description);
            }
            extras.putString(EXTRA_SUMMARY, description);

            String settingsClassName = info.getSettingsActivityName();
            if (!TextUtils.isEmpty(settingsClassName)) {
                extras.putString(EXTRA_SETTINGS_TITLE,
                        getString(R.string.accessibility_menu_item_settings));
                extras.putString(EXTRA_SETTINGS_COMPONENT_NAME,
                        new ComponentName(info.getResolveInfo().serviceInfo.packageName,
                                settingsClassName).flattenToString());
            }

            extras.putParcelable(EXTRA_COMPONENT_NAME, componentName);

            mServicesCategory.addPreference(preference);
        }

        if (mServicesCategory.getPreferenceCount() == 0) {
            if (mNoServicesMessagePreference == null) {
                mNoServicesMessagePreference = new Preference(getPrefContext());
                mNoServicesMessagePreference.setPersistent(false);
                mNoServicesMessagePreference.setLayoutResource(
                        R.layout.text_description_preference);
                mNoServicesMessagePreference.setSelectable(false);
                mNoServicesMessagePreference.setSummary(
                        getString(R.string.accessibility_no_services_installed));
            }
            mServicesCategory.addPreference(mNoServicesMessagePreference);
        }
    }

    private void updateSystemPreferences() {
        // Text contrast.
        mToggleHighTextContrastPreference.setChecked(
                Settings.Secure.getInt(getContentResolver(),
                        Settings.Secure.ACCESSIBILITY_HIGH_TEXT_CONTRAST_ENABLED, 0) == 1);
		/**prize-add-game-reading-mode-liup-20150420-start*/	
		final boolean bPrizeGameMode = Settings.System.getInt(getContentResolver(),Settings.System.PRIZE_GAME_MODE,0) == 1;
		mPrizeGameMode.setChecked(bPrizeGameMode);
		final boolean bPrizeReadingMode = Settings.System.getInt(getContentResolver(),Settings.System.PRIZE_READING_MODE,0) == 1;
		mPrizeReadingMode.setChecked(bPrizeReadingMode);
		/**prize-add-game-reading-mode-liup-20150420-start*/	

        // If the quick setting is enabled, the preference MUST be enabled.
        mToggleInversionPreference.setChecked(Settings.Secure.getInt(getContentResolver(),
                Settings.Secure.ACCESSIBILITY_DISPLAY_INVERSION_ENABLED, 0) == 1);

        // Power button ends calls.
        if (KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_POWER)
                && Utils.isVoiceCapable(getActivity())) {
            final int incallPowerBehavior = Settings.Secure.getInt(getContentResolver(),
                    Settings.Secure.INCALL_POWER_BUTTON_BEHAVIOR,
                    Settings.Secure.INCALL_POWER_BUTTON_BEHAVIOR_DEFAULT);
            final boolean powerButtonEndsCall =
                    (incallPowerBehavior == Settings.Secure.INCALL_POWER_BUTTON_BEHAVIOR_HANGUP);
            mTogglePowerButtonEndsCallPreference.setChecked(powerButtonEndsCall);
        }

        // Auto-rotate screen
        updateLockScreenRotationCheckbox();

        // Speak passwords.
        final boolean speakPasswordEnabled = Settings.Secure.getInt(getContentResolver(),
                Settings.Secure.ACCESSIBILITY_SPEAK_PASSWORD, 0) != 0;
        mToggleSpeakPasswordPreference.setChecked(speakPasswordEnabled);

        // Large pointer icon.
        mToggleLargePointerIconPreference.setChecked(Settings.Secure.getInt(getContentResolver(),
                Settings.Secure.ACCESSIBILITY_LARGE_POINTER_ICON, 0) != 0);

        // Master mono
        updateMasterMono();

        // Long press timeout.
        final int longPressTimeout = Settings.Secure.getInt(getContentResolver(),
                Settings.Secure.LONG_PRESS_TIMEOUT, mLongPressTimeoutDefault);
        String value = String.valueOf(longPressTimeout);
        mSelectLongPressTimeoutPreference.setValue(value);
        mSelectLongPressTimeoutPreference.setSummary(mLongPressTimeoutValuetoTitleMap.get(value));

        updateFeatureSummary(Settings.Secure.ACCESSIBILITY_CAPTIONING_ENABLED,
                mCaptioningPreferenceScreen);
        updateFeatureSummary(Settings.Secure.ACCESSIBILITY_DISPLAY_MAGNIFICATION_ENABLED,
                mDisplayMagnificationPreferenceScreen);
        updateFeatureSummary(Settings.Secure.ACCESSIBILITY_DISPLAY_DALTONIZER_ENABLED,
                mDisplayDaltonizerPreferenceScreen);

        updateFontSizeSummary(mFontSizePreferenceScreen);

        updateAutoclickSummary(mAutoclickPreferenceScreen);

        // Global gesture
        final boolean globalGestureEnabled = Settings.Global.getInt(getContentResolver(),
                Settings.Global.ENABLE_ACCESSIBILITY_GLOBAL_GESTURE_ENABLED, 0) == 1;
        if (globalGestureEnabled) {
            mGlobalGesturePreferenceScreen.setSummary(
                    R.string.accessibility_global_gesture_preference_summary_on);
        } else {
            mGlobalGesturePreferenceScreen.setSummary(
                    R.string.accessibility_global_gesture_preference_summary_off);
        }

        /// M: IPO Setting @{
        boolean ipoSettingEnabled = Settings.System.getInt(getContentResolver(),
                Settings.System.IPO_SETTING, 1) == 1;
        if (mIpoSetting != null) {
            mIpoSetting.setChecked(ipoSettingEnabled);
        }
        /// @} end
    }

    private void updateFeatureSummary(String prefKey, Preference pref) {
        final boolean enabled = Settings.Secure.getInt(getContentResolver(), prefKey, 0) == 1;
        pref.setSummary(enabled ? R.string.accessibility_feature_state_on
                : R.string.accessibility_feature_state_off);
    }

    private void updateAutoclickSummary(Preference pref) {
        final boolean enabled = Settings.Secure.getInt(
                getContentResolver(), Settings.Secure.ACCESSIBILITY_AUTOCLICK_ENABLED, 0) == 1;
        if (!enabled) {
            pref.setSummary(R.string.accessibility_feature_state_off);
            return;
        }
        int delay = Settings.Secure.getInt(
                getContentResolver(), Settings.Secure.ACCESSIBILITY_AUTOCLICK_DELAY,
                AccessibilityManager.AUTOCLICK_DELAY_DEFAULT);
        pref.setSummary(ToggleAutoclickPreferenceFragment.getAutoclickPreferenceSummary(
                getResources(), delay));
    }

    private void updateFontSizeSummary(Preference pref) {
        final float currentScale = Settings.System.getFloat(getContext().getContentResolver(),
                Settings.System.FONT_SCALE, 1.0f);
        final Resources res = getContext().getResources();
        final String[] entries = res.getStringArray(R.array.entries_font_size);
        final String[] strEntryValues = res.getStringArray(R.array.entryvalues_font_size);
        final int index = ToggleFontSizePreferenceFragment.fontSizeValueToIndex(currentScale,
                strEntryValues);
        pref.setSummary(entries[index]);
    }

    private void updateLockScreenRotationCheckbox() {
        Context context = getActivity();
        if (context != null) {
            mToggleLockScreenRotationPreference.setChecked(
                    !RotationPolicy.isRotationLocked(context));
        }
    }

    private void updateMasterMono() {
        final boolean masterMono = Settings.System.getIntForUser(
                getContentResolver(), Settings.System.MASTER_MONO,
                0 /* default */, UserHandle.USER_CURRENT) == 1;
        mToggleMasterMonoPreference.setChecked(masterMono);
    }

    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {
        @Override
        public List<SearchIndexableRaw> getRawDataToIndex(Context context, boolean enabled) {
            List<SearchIndexableRaw> indexables = new ArrayList<SearchIndexableRaw>();

            PackageManager packageManager = context.getPackageManager();
            AccessibilityManager accessibilityManager = (AccessibilityManager)
                    context.getSystemService(Context.ACCESSIBILITY_SERVICE);

            String screenTitle = context.getResources().getString(
                    R.string.accessibility_title);

            // Indexing all services, regardless if enabled.
            List<AccessibilityServiceInfo> services = accessibilityManager
                    .getInstalledAccessibilityServiceList();
            final int serviceCount = services.size();
            for (int i = 0; i < serviceCount; i++) {
                AccessibilityServiceInfo service = services.get(i);
                if (service == null || service.getResolveInfo() == null) {
                    continue;
                }

                ServiceInfo serviceInfo = service.getResolveInfo().serviceInfo;
                ComponentName componentName = new ComponentName(serviceInfo.packageName,
                        serviceInfo.name);

                SearchIndexableRaw indexable = new SearchIndexableRaw(context);
                indexable.key = componentName.flattenToString();
                indexable.title = service.getResolveInfo().loadLabel(packageManager).toString();
                indexable.summaryOn = context.getString(R.string.accessibility_feature_state_on);
                indexable.summaryOff = context.getString(R.string.accessibility_feature_state_off);
                indexable.screenTitle = screenTitle;
                indexables.add(indexable);
            }

            return indexables;
        }

        @Override
        public List<SearchIndexableResource> getXmlResourcesToIndex(Context context,
               boolean enabled) {
            List<SearchIndexableResource> indexables = new ArrayList<SearchIndexableResource>();
            SearchIndexableResource indexable = new SearchIndexableResource(context);
            indexable.xmlResId = R.xml.accessibility_settings;
            indexables.add(indexable);
            return indexables;
        }
		/// remove menu to search db liup 20160627 start
		@Override
		public List<String> getNonIndexableKeys(Context context) {
			final List<String> keys = new ArrayList<String>();
			keys.add(DISPLAY_CATEGORY);
			if(!PrizeOption.PRIZE_GAME_MODE){
				keys.add(PRIZE_GAME_MODE);
			}
			if(!PrizeOption.PRIZE_READING_MODE){
				keys.add(PRIZE_READING_MODE); 
			}
			//-prize-delete-yangming-2106-8-12-start-change/
			//if(!PrizeOption.PRIZE_FLOAT_WINDOW){
			//	keys.add(PRIZE_FLOAT_WINDOW);
			//}
			//-prize-delete-yangming-2106-8-12-end-/
			keys.add(IPO_SETTING_PREFERENCE);
			keys.add(TOGGLE_LOCK_SCREEN_ROTATION_PREFERENCE);
			// keys.add(TOGGLE_SPEAK_PASSWORD_PREFERENCE);
			// keys.add(DISPLAY_MAGNIFICATION_PREFERENCE_SCREEN);
			keys.add(ENABLE_ACCESSIBILITY_GESTURE_PREFERENCE_SCREEN);
			return keys;
		}
		/// remove menu to search db liup 20160627 end
    };
}
