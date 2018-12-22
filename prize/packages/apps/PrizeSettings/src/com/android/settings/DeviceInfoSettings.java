/*
 * Copyright (C) 2008 The Android Open Source Project
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

package com.android.settings;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.os.SELinux;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.SearchIndexableResource;
import android.provider.Settings;

import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceGroup;
import android.support.v7.preference.PreferenceScreen;

import android.telephony.CarrierConfigManager;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.android.internal.logging.MetricsProto.MetricsEvent;
import com.android.settings.dashboard.SummaryLoader;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Index;
import com.android.settings.search.Indexable;
import com.android.settingslib.DeviceInfoUtils;
import com.android.settingslib.RestrictedLockUtils;

import com.mediatek.settings.deviceinfo.DeviceInfoSettingsExts;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.android.settingslib.RestrictedLockUtils.EnforcedAdmin;
//fota start
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
//fota end
//add system version. prize-linkh-20150518.
import android.os.Bundle;
import android.view.View;
import com.android.settings.R;
import android.widget.ImageView;
import com.mediatek.common.prizeoption.PrizeOption;
// end.....

// prize-add-by-yanghao-20160303-start
import android.system.Os;
import android.system.StructStatVfs;
import android.system.ErrnoException;
import com.mediatek.settings.FeatureOption;
// prize-add-by-yanghao-20160303-end 
import android.widget.LinearLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.LinearLayoutManager;
public class DeviceInfoSettings extends SettingsPreferenceFragment implements Indexable {

    private static final String LOG_TAG = "DeviceInfoSettings";
    private static final String FILENAME_PROC_VERSION = "/proc/version";
    private static final String FILENAME_MSV = "/sys/board_properties/soc/msv";

    private static final String KEY_CONTAINER = "container";
    private static final String KEY_REGULATORY_INFO = "regulatory_info";
    private static final String KEY_TERMS = "terms";
    private static final String KEY_LICENSE = "license";
    private static final String KEY_COPYRIGHT = "copyright";
    private static final String KEY_WEBVIEW_LICENSE = "webview_license";
    private static final String KEY_SYSTEM_UPDATE_SETTINGS = "system_update_settings";
    private static final String PROPERTY_URL_SAFETYLEGAL = "ro.url.safetylegal";
    private static final String PROPERTY_SELINUX_STATUS = "ro.build.selinux";
    private static final String KEY_KERNEL_VERSION = "kernel_version";
    private static final String KEY_BUILD_NUMBER = "build_number";
    private static final String KEY_DEVICE_MODEL = "device_model";
    private static final String KEY_SELINUX_STATUS = "selinux_status";
    private static final String KEY_BASEBAND_VERSION = "baseband_version";
    private static final String KEY_FIRMWARE_VERSION = "firmware_version";
    private static final String KEY_SECURITY_PATCH = "security_patch";
    private static final String KEY_UPDATE_SETTING = "additional_system_update_settings";
    private static final String KEY_EQUIPMENT_ID = "fcc_equipment_id";
    private static final String PROPERTY_EQUIPMENT_ID = "ro.ril.fccid";
    private static final String KEY_DEVICE_FEEDBACK = "device_feedback";
    private static final String KEY_SAFETY_LEGAL = "safetylegal";
    private static final String KEY_RESOLVING_POWER = "resolving_power";
    //PRIZE-add-system_version-hdj-20150710-start
    private static final String KEY_SYSTEM_VERSION = "system_version";
	//prize-start-pyx 2016-06-30 stop fresh core image
    private static final String KEY_CPU_CORENUMBER = "cpu_core_number";
	//prize-end-pyx 2016-06-30 stop fresh core image
    //PRIZE-add-system_version-hdj-20150710-start
    
    // prize-add-by-yanghao-20160303-start
    private static final String KEY_RAM_INFO = "ram_info";
    private static final String KEY_ROM_INFO = "rom_info";
    private static final int TYPE_RAM = 1;
    private static final int TYPE_ROM = 2;
    
    private final long BYTES_IN_GB = 1*1024*1024*1024;    
	private final long BYTES_IN_1_POINT_5_GB = new Double(1.5*1024*1024*1024).longValue();  // prize-add-by-yanghao-20160503 for k559 volte
    private final long BYTES_IN_2_GB = 2*BYTES_IN_GB;
    private final long BYTES_IN_3_GB = 3*BYTES_IN_GB;
    private final long BYTES_IN_4_GB = 4*BYTES_IN_GB;
	/*prize-add-bugid:55039 add ram_info_size-ganxiayong-20180411-start*/
	private final long BYTES_IN_6_GB = 6*BYTES_IN_GB;
	/*prize-add-bugid:55039 add ram_info_size-ganxiayong-20180411-end*/
    private final long BYTES_IN_8_GB = 8*BYTES_IN_GB;
    private final long BYTES_IN_16_GB = 16*BYTES_IN_GB;
    private final long BYTES_IN_32_GB = 32*BYTES_IN_GB;
    private final long BYTES_IN_64_GB = 64*BYTES_IN_GB;
    private final long BYTES_IN_128_GB = 128*BYTES_IN_GB;
    // prize-add-by-yanghao-20160303-end

    static final int TAPS_TO_BE_A_DEVELOPER = 7;

    long[] mHits = new long[3];
    int mDevHitCountdown;
    Toast mDevHitToast;

    private UserManager mUm;

    private EnforcedAdmin mFunDisallowedAdmin;
    private boolean mFunDisallowedBySystem;
    private EnforcedAdmin mDebuggingFeaturesDisallowedAdmin;
    private boolean mDebuggingFeaturesDisallowedBySystem;

    private DeviceInfoSettingsExts mExts;

    @Override
    protected int getMetricsCategory() {
        return MetricsEvent.DEVICEINFO;
    }

    @Override
    protected int getHelpResource() {
        return R.string.help_uri_about;
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        mUm = UserManager.get(getActivity());
		
        //keep the portrait orientation. prize-linkh-20150523
        getActivity().setRequestedOrientation(android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        addPreferencesFromResource(R.xml.device_info_settings);

        setStringSummary(KEY_FIRMWARE_VERSION, Build.VERSION.RELEASE);
        findPreference(KEY_FIRMWARE_VERSION).setEnabled(true);

        String patch = DeviceInfoUtils.getSecurityPatch();
        if (!TextUtils.isEmpty(patch)) {
            try {
                SimpleDateFormat template = new SimpleDateFormat("yyyy-MM-dd");
                Date patchDate = template.parse(patch);
                String format = DateFormat.getBestDateTimePattern(Locale.getDefault(), "dMMMMyyyy");
                patch = DateFormat.format(format, patchDate).toString();
            } catch (ParseException e) {
                // broken parse; fall through and use the raw string
            }
            setStringSummary(KEY_SECURITY_PATCH, patch);
        } else {
            getPreferenceScreen().removePreference(findPreference(KEY_SECURITY_PATCH));
        }

        setValueSummary(KEY_BASEBAND_VERSION, "gsm.version.baseband");
        setStringSummary(KEY_DEVICE_MODEL, Build.MODEL + DeviceInfoUtils.getMsvSuffix());
        setValueSummary(KEY_EQUIPMENT_ID, PROPERTY_EQUIPMENT_ID);
        setStringSummary(KEY_DEVICE_MODEL, Build.MODEL);
        setStringSummary(KEY_BUILD_NUMBER, Build.DISPLAY);
        //PRIZE-add-system_version-hdj-20150710-start
        setStringSummary(KEY_SYSTEM_VERSION, Build.SYSTEM_VERSION);
        //PRIZE-add-system_version-hdj-20150710-end
        findPreference(KEY_BUILD_NUMBER).setEnabled(true);
        findPreference(KEY_KERNEL_VERSION).setSummary(DeviceInfoUtils.getFormattedKernelVersion());
        WindowManager windowManager = (WindowManager) getPrefContext().getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        if(windowManager != null){
            windowManager.getDefaultDisplay().getRealMetrics(displayMetrics);
            int widthPx = displayMetrics.widthPixels;
            int hightPx = displayMetrics.heightPixels;
            String symbol = getPrefContext().getResources().getString(R.string.prize_split_symbol);
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append(String.valueOf(hightPx)).append(symbol).append(String.valueOf(widthPx));
            findPreference(KEY_RESOLVING_POWER).setSummary(stringBuffer.toString());
        }
        // prize-add-by-yanghao-20160303-start 
        
        setStringSummary(KEY_RAM_INFO, updateStorageSummary(TYPE_RAM));
        setStringSummary(KEY_ROM_INFO, updateStorageSummary(TYPE_ROM));
        // prize-add-by-yanghao-20160303-end 

        if (!SELinux.isSELinuxEnabled()) {
            String status = getResources().getString(R.string.selinux_status_disabled);
            setStringSummary(KEY_SELINUX_STATUS, status);
        } else if (!SELinux.isSELinuxEnforced()) {
            String status = getResources().getString(R.string.selinux_status_permissive);
            setStringSummary(KEY_SELINUX_STATUS, status);
        }

        // Remove selinux information if property is not present
        removePreferenceIfPropertyMissing(getPreferenceScreen(), KEY_SELINUX_STATUS,
                PROPERTY_SELINUX_STATUS);

        // Remove Safety information preference if PROPERTY_URL_SAFETYLEGAL is not set
        removePreferenceIfPropertyMissing(getPreferenceScreen(), KEY_SAFETY_LEGAL,
                PROPERTY_URL_SAFETYLEGAL);

        // Remove Equipment id preference if FCC ID is not set by RIL
        removePreferenceIfPropertyMissing(getPreferenceScreen(), KEY_EQUIPMENT_ID,
                PROPERTY_EQUIPMENT_ID);

        // Remove Baseband version if wifi-only device
        if (Utils.isWifiOnly(getActivity())) {
            getPreferenceScreen().removePreference(findPreference(KEY_BASEBAND_VERSION));
        }

        // Dont show feedback option if there is no reporter.
        if (TextUtils.isEmpty(DeviceInfoUtils.getFeedbackReporterPackage(getActivity()))) {
            getPreferenceScreen().removePreference(findPreference(KEY_DEVICE_FEEDBACK));
        }

        /*
         * Settings is a generic app and should not contain any device-specific
         * info.
         */
        final Activity act = getActivity();
        // These are contained in the "container" preference group
        PreferenceGroup parentPreference = (PreferenceGroup) findPreference(KEY_CONTAINER);
        Utils.updatePreferenceToSpecificActivityOrRemove(act, parentPreference, KEY_TERMS,
                Utils.UPDATE_PREFERENCE_FLAG_SET_TITLE_TO_MATCHING_ACTIVITY);
        Utils.updatePreferenceToSpecificActivityOrRemove(act, parentPreference, KEY_LICENSE,
                Utils.UPDATE_PREFERENCE_FLAG_SET_TITLE_TO_MATCHING_ACTIVITY);
        Utils.updatePreferenceToSpecificActivityOrRemove(act, parentPreference, KEY_COPYRIGHT,
                Utils.UPDATE_PREFERENCE_FLAG_SET_TITLE_TO_MATCHING_ACTIVITY);
        Utils.updatePreferenceToSpecificActivityOrRemove(act, parentPreference, KEY_WEBVIEW_LICENSE,
                Utils.UPDATE_PREFERENCE_FLAG_SET_TITLE_TO_MATCHING_ACTIVITY);

        // These are contained by the root preference screen
         parentPreference = getPreferenceScreen();
        //prize-remove_system_legal_information-hdj-20150708-start
        removePreference(KEY_CONTAINER);
        //prize-remove_system_legal_information-hdj-20150708-end

        if (mUm.isAdminUser()) {
            Utils.updatePreferenceToSpecificActivityOrRemove(act, parentPreference,
                    KEY_SYSTEM_UPDATE_SETTINGS,
                    Utils.UPDATE_PREFERENCE_FLAG_SET_TITLE_TO_MATCHING_ACTIVITY);
        } else {
            // Remove for secondary users
            removePreference(KEY_SYSTEM_UPDATE_SETTINGS);
        }

        // Read platform settings for additional system update setting
        removePreferenceIfBoolFalse(KEY_UPDATE_SETTING,
                R.bool.config_additional_system_update_setting_enable);

        // Remove manual entry if none present.
        // removePreferenceIfBoolFalse(KEY_MANUAL, R.bool.config_show_manual);

        // Remove regulatory information if none present.
        final Intent intent = new Intent(Settings.ACTION_SHOW_REGULATORY_INFO);
        if (getPackageManager().queryIntentActivities(intent, 0).isEmpty()) {
            Preference pref = findPreference(KEY_REGULATORY_INFO);
            if (pref != null) {
                getPreferenceScreen().removePreference(pref);
            }
        }

        ///M:
        mExts = new DeviceInfoSettingsExts(getActivity(), this);
        mExts.initMTKCustomization(getPreferenceScreen());
        //fota start
        /*if(!isApkExist(act, "com.adups.fota")){*/
            if(findPreference("adupsfota_software_update") != null){
        	    getPreferenceScreen().removePreference(findPreference("adupsfota_software_update"));
            }
        /*} else {
		    Preference preference = findPreference("adupsfota_software_update");
			if (preference != null) {
		        preference.setTitle(getAppName(act, "com.adups.fota"));
			}
		}*/
        //fota end

        /**prize-add-User_Uanual-by-zhongweilin-20150808-start*/
        if(!isApkExist(act, "com.freeme.operationManual")){
            if(findPreference("operation_manual") != null){
                getPreferenceScreen().removePreference(findPreference("operation_manual"));
            }
        } else {
            Preference preference = findPreference("operation_manual");
        }
        /**prize-add-User_Uanual-by-zhongweilin-20150808-end*/
        
    	//add system version. prize-linkh-20150518.
    	/*android.app.ActionBar ab = getActivity().getActionBar();
    	if(ab != null) {
    		ab.hide();
    	}*/ //end....
        
        /* Add by zhudaopeng at 2016-12-05 Start */
        getActivity().setTitle(getString(R.string.about_settings));
        /* Add by zhudaopeng at 2016-12-05 End */
        
    }

	//add system version. prize-linkh-20150518.
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
    	super.onViewCreated(view, savedInstanceState);
        ImageView v = (ImageView)view.findViewById(R.id.system_logo);
        View divider = view.findViewById(R.id.system_logo_divider);
        if(v != null) {
            String customer = PrizeOption.PRIZE_CUSTOMER_NAME;
           // v.setImageResource(R.drawable.didoos_logo_prize);
            /*if("koobee".equals(customer)) {
                v.setImageResource(R.drawable.dido_logo_prize);
            } else if("coosea".equals(customer)) {
                v.setImageResource(R.drawable.bingo_logo_prize);
            }*/
           // v.setVisibility(View.VISIBLE);
            if(divider!=null)
            	divider.setVisibility(View.GONE);
        }
    } 
    //end.....


    @Override
    public void onResume() {
        super.onResume();
        mDevHitCountdown = getActivity().getSharedPreferences(DevelopmentSettings.PREF_FILE,
                Context.MODE_PRIVATE).getBoolean(DevelopmentSettings.PREF_SHOW,
                        android.os.Build.TYPE.equals("eng")) ? -1 : TAPS_TO_BE_A_DEVELOPER;
        mDevHitToast = null;
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference.getKey().equals(KEY_FIRMWARE_VERSION)) {
            System.arraycopy(mHits, 1, mHits, 0, mHits.length-1);
            mHits[mHits.length-1] = SystemClock.uptimeMillis();
            if (mHits[0] >= (SystemClock.uptimeMillis()-500)) {
                if (mUm.hasUserRestriction(UserManager.DISALLOW_FUN)) {
                    if (mFunDisallowedAdmin != null && !mFunDisallowedBySystem) {
                        RestrictedLockUtils.sendShowAdminSupportDetailsIntent(getActivity(),
                                mFunDisallowedAdmin);
                    }
                    Log.d(LOG_TAG, "Sorry, no fun for you!");
                    return false;
                }

                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.setClassName("android",
                        com.android.internal.app.PlatLogoActivity.class.getName());
                try {
                    startActivity(intent);
                } catch (Exception e) {
                    Log.e(LOG_TAG, "Unable to start activity " + intent.toString());
                }
            }
        } else if (preference.getKey().equals(KEY_BUILD_NUMBER)) {
            // Don't enable developer options for secondary users.
            if (!mUm.isAdminUser()) return true;

            // Don't enable developer options until device has been provisioned
            if (!Utils.isDeviceProvisioned(getActivity())) {
                return true;
            }

            if (mUm.hasUserRestriction(UserManager.DISALLOW_DEBUGGING_FEATURES)) {
                if (mDebuggingFeaturesDisallowedAdmin != null &&
                        !mDebuggingFeaturesDisallowedBySystem) {
                    RestrictedLockUtils.sendShowAdminSupportDetailsIntent(getActivity(),
                            mDebuggingFeaturesDisallowedAdmin);
                }
                return true;
            }

            if (mDevHitCountdown > 0) {
                mDevHitCountdown--;
                if (mDevHitCountdown == 0) {
                    getActivity().getSharedPreferences(DevelopmentSettings.PREF_FILE,
                            Context.MODE_PRIVATE).edit().putBoolean(
                                    DevelopmentSettings.PREF_SHOW, true).apply();
                    if (mDevHitToast != null) {
                        mDevHitToast.cancel();
                    }
                    mDevHitToast = Toast.makeText(getActivity(), R.string.show_dev_on,
                            Toast.LENGTH_LONG);
                    mDevHitToast.show();
                    // This is good time to index the Developer Options
                    Index.getInstance(
                            getActivity().getApplicationContext()).updateFromClassNameResource(
                                    DevelopmentSettings.class.getName(), true, true);

                } else if (mDevHitCountdown > 0
                        && mDevHitCountdown < (TAPS_TO_BE_A_DEVELOPER-2)) {
                    if (mDevHitToast != null) {
                        mDevHitToast.cancel();
                    }
                    mDevHitToast = Toast.makeText(getActivity(), getResources().getQuantityString(
                            R.plurals.show_dev_countdown, mDevHitCountdown, mDevHitCountdown),
                            Toast.LENGTH_SHORT);
                    mDevHitToast.show();
                }
            } else if (mDevHitCountdown < 0) {
                if (mDevHitToast != null) {
                    mDevHitToast.cancel();
                }
                mDevHitToast = Toast.makeText(getActivity(), R.string.show_dev_already,
                        Toast.LENGTH_LONG);
                mDevHitToast.show();
            }
        } else if (preference.getKey().equals(KEY_DEVICE_FEEDBACK)) {
            sendFeedback();
        } else if(preference.getKey().equals(KEY_SYSTEM_UPDATE_SETTINGS)) {
            CarrierConfigManager configManager =
                    (CarrierConfigManager) getSystemService(Context.CARRIER_CONFIG_SERVICE);
            PersistableBundle b = configManager.getConfig();
            if (b.getBoolean(CarrierConfigManager.KEY_CI_ACTION_ON_SYS_UPDATE_BOOL)) {
                ciActionOnSysUpdate(b);
            }
        }
        /// M:
        mExts.onCustomizedPreferenceTreeClick(preference);
        return super.onPreferenceTreeClick(preference);
    }

    /**
     * Trigger client initiated action (send intent) on system update
     */
    private void ciActionOnSysUpdate(PersistableBundle b) {
        String intentStr = b.getString(CarrierConfigManager.
                KEY_CI_ACTION_ON_SYS_UPDATE_INTENT_STRING);
        if (!TextUtils.isEmpty(intentStr)) {
            String extra = b.getString(CarrierConfigManager.
                    KEY_CI_ACTION_ON_SYS_UPDATE_EXTRA_STRING);
            String extraVal = b.getString(CarrierConfigManager.
                    KEY_CI_ACTION_ON_SYS_UPDATE_EXTRA_VAL_STRING);

            Intent intent = new Intent(intentStr);
            if (!TextUtils.isEmpty(extra)) {
                intent.putExtra(extra, extraVal);
            }
            Log.d(LOG_TAG, "ciActionOnSysUpdate: broadcasting intent " + intentStr +
                    " with extra " + extra + ", " + extraVal);
            getActivity().getApplicationContext().sendBroadcast(intent);
        }
    }

    private void removePreferenceIfPropertyMissing(PreferenceGroup preferenceGroup,
            String preference, String property ) {
        if (SystemProperties.get(property).equals("")) {
            // Property is missing so remove preference from group
            try {
                preferenceGroup.removePreference(findPreference(preference));
            } catch (RuntimeException e) {
                Log.d(LOG_TAG, "Property '" + property + "' missing and no '"
                        + preference + "' preference");
            }
        }
    }

    private void removePreferenceIfBoolFalse(String preference, int resId) {
        if (!getResources().getBoolean(resId)) {
            Preference pref = findPreference(preference);
            if (pref != null) {
                getPreferenceScreen().removePreference(pref);
            }
        }
    }

    private void setStringSummary(String preference, String value) {
        try {
            findPreference(preference).setSummary(value);
        } catch (RuntimeException e) {
            findPreference(preference).setSummary(
                getResources().getString(R.string.device_info_default));
        }
    }

    private void setValueSummary(String preference, String property) {
        try {
            findPreference(preference).setSummary(
                    SystemProperties.get(property,
                            getResources().getString(R.string.device_info_default)));
        } catch (RuntimeException e) {
            // No recovery
        }
    }

    private void sendFeedback() {
        String reporterPackage = DeviceInfoUtils.getFeedbackReporterPackage(getActivity());
        if (TextUtils.isEmpty(reporterPackage)) {
            return;
        }
        Intent intent = new Intent(Intent.ACTION_BUG_REPORT);
        intent.setPackage(reporterPackage);
        startActivityForResult(intent, 0);
    }

    /**
     * Reads a line from the specified file.
     * @param filename the file to read from
     * @return the first line, if any.
     * @throws IOException if the file couldn't be read
     */
    private static String readLine(String filename) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filename), 256);
        try {
            return reader.readLine();
        } finally {
            reader.close();
        }
    }

    public static String getFormattedKernelVersion() {
        try {
            return formatKernelVersion(readLine(FILENAME_PROC_VERSION));

        } catch (IOException e) {
            Log.e(LOG_TAG,
                "IO Exception when getting kernel version for Device Info screen",
                e);

            return "Unavailable";
        }
    }

    public static String formatKernelVersion(String rawKernelVersion) {
        // Example (see tests for more):
        // Linux version 3.0.31-g6fb96c9 (android-build@xxx.xxx.xxx.xxx.com) \
        //     (gcc version 4.6.x-xxx 20120106 (prerelease) (GCC) ) #1 SMP PREEMPT \
        //     Thu Jun 28 11:02:39 PDT 2012

        final String PROC_VERSION_REGEX =
            "Linux version (\\S+) " + /* group 1: "3.0.31-g6fb96c9" */
            "\\((\\S+?)\\) " +        /* group 2: "x@y.com" (kernel builder) */
            "(?:\\(gcc.+? \\)) " +    /* ignore: GCC version information */
            "(#\\d+) " +              /* group 3: "#1" */
            "(?:.*?)?" +              /* ignore: optional SMP, PREEMPT, and any CONFIG_FLAGS */
            "((Sun|Mon|Tue|Wed|Thu|Fri|Sat).+)"; /* group 4: "Thu Jun 28 11:02:39 PDT 2012" */

        Matcher m = Pattern.compile(PROC_VERSION_REGEX).matcher(rawKernelVersion);
        if (!m.matches()) {
            Log.e(LOG_TAG, "Regex did not match on /proc/version: " + rawKernelVersion);
            return "Unavailable";
        } else if (m.groupCount() < 4) {
            Log.e(LOG_TAG, "Regex match on /proc/version only returned " + m.groupCount()
                    + " groups");
            return "Unavailable";
        }
        return m.group(1) + "\n" +                 // 3.0.31-g6fb96c9
            m.group(2) + " " + m.group(3) + "\n" + // x@y.com #1
            m.group(4);                            // Thu Jun 28 11:02:39 PDT 2012
    }

    /**
     * Returns " (ENGINEERING)" if the msv file has a zero value, else returns "".
     * @return a string to append to the model number description.
     */
    private String getMsvSuffix() {
        // Production devices should have a non-zero value. If we can't read it, assume it's a
        // production device so that we don't accidentally show that it's an ENGINEERING device.
        try {
            String msv = readLine(FILENAME_MSV);
            // Parse as a hex number. If it evaluates to a zero, then it's an engineering build.
            if (Long.parseLong(msv, 16) == 0) {
                return " (ENGINEERING)";
            }
        } catch (IOException ioe) {
            // Fail quietly, as the file may not exist on some devices.
        } catch (NumberFormatException nfe) {
            // Fail quietly, returning empty string should be sufficient
        }
        return "";
    }

    private static String getFeedbackReporterPackage(Context context) {
        final String feedbackReporter =
                context.getResources().getString(R.string.oem_preferred_feedback_reporter);
        if (TextUtils.isEmpty(feedbackReporter)) {
            // Reporter not configured. Return.
            return feedbackReporter;
        }
        // Additional checks to ensure the reporter is on system image, and reporter is
        // configured to listen to the intent. Otherwise, dont show the "send feedback" option.
        final Intent intent = new Intent(Intent.ACTION_BUG_REPORT);

        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> resolvedPackages =
                pm.queryIntentActivities(intent, PackageManager.GET_RESOLVED_FILTER);
        for (ResolveInfo info : resolvedPackages) {
            if (info.activityInfo != null) {
                if (!TextUtils.isEmpty(info.activityInfo.packageName)) {
                    try {
                        ApplicationInfo ai = pm.getApplicationInfo(info.activityInfo.packageName, 0);
                        if ((ai.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                            // Package is on the system image
                            if (TextUtils.equals(
                                        info.activityInfo.packageName, feedbackReporter)) {
                                return feedbackReporter;
                            }
                        }
                    } catch (PackageManager.NameNotFoundException e) {
                         // No need to do anything here.
                    }
                }
            }
        }
        return null;
    }

    /**
     * For Search.
     */
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
        new BaseSearchIndexProvider() {

            @Override
            public List<SearchIndexableResource> getXmlResourcesToIndex(
                    Context context, boolean enabled) {
                final SearchIndexableResource sir = new SearchIndexableResource(context);
                sir.xmlResId = R.xml.device_info_settings;
                return Arrays.asList(sir);
            }

            @Override
            public List<String> getNonIndexableKeys(Context context) {
                final List<String> keys = new ArrayList<String>();
                if (isPropertyMissing(PROPERTY_SELINUX_STATUS)) {
                    keys.add(KEY_SELINUX_STATUS);
                }
                if (isPropertyMissing(PROPERTY_URL_SAFETYLEGAL)) {
                    keys.add(KEY_SAFETY_LEGAL);
                }
                if (isPropertyMissing(PROPERTY_EQUIPMENT_ID)) {
                    keys.add(KEY_EQUIPMENT_ID);
                }
                // Remove Baseband version if wifi-only device
                if (Utils.isWifiOnly(context)) {
                    keys.add((KEY_BASEBAND_VERSION));
                }
                // Dont show feedback option if there is no reporter.
                if (TextUtils.isEmpty(DeviceInfoUtils.getFeedbackReporterPackage(context))) {
                    keys.add(KEY_DEVICE_FEEDBACK);
                }
                final UserManager um = UserManager.get(context);
                // TODO: system update needs to be fixed for non-owner user b/22760654
                //if (!um.isAdminUser()) {
                    keys.add(KEY_SYSTEM_UPDATE_SETTINGS);
              //  }
                if (!context.getResources().getBoolean(
                        R.bool.config_additional_system_update_setting_enable)) {
                    keys.add(KEY_UPDATE_SETTING);
                }
                keys.add("adupsfota_software_update");
                return keys;
            }

            private boolean isPropertyMissing(String property) {
                return SystemProperties.get(property).equals("");
            }
        };
	
    //fota start
    private boolean isApkExist(Context ctx, String packageName){
        PackageManager pm = ctx.getPackageManager();
        PackageInfo packageInfo = null;
        String versionName = null;
        try {
            packageInfo = pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            versionName = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("FotaUpdate", "isApkExist not found");
            return false;
        }
		
        if (versionName != null) {
            String[] names = versionName.split("\\.");
            if (names.length >= 4 && "9".equals(names[3])) {
                return false;
            }
        }
        Log.i("FotaUpdate", "isApkExist = true");
        return true;
    }

    public String getAppName(Context ctx, String packageName) {
        PackageManager pm = ctx.getPackageManager();
        ApplicationInfo appInfo = null;
        try {
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            appInfo = pm.getApplicationInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            appInfo = null;
        }
		
        return (String) pm.getApplicationLabel(appInfo);
    }
    //fota end
    // prize-add-by-yanghao-20160303-start
     private String updateStorageSummary(int type) {
        String totalSizeStr;        
        long totalRealSize = 0;
        
        Log.d(LOG_TAG, "type : "+type+" (1 == RAM, 2 == ROM)");
        //calc total size.
        if (type == TYPE_RAM)
        {
        	totalRealSize = calcPhoneRamTotal();
        }else if (type == TYPE_ROM)
        {
        	totalRealSize = calcPhoneRomTotal();
        }
        
        Log.d(LOG_TAG, "totalRealSize : "+totalRealSize);
        if(totalRealSize <= 0) {
            totalSizeStr = "0GB";
        }else if(totalRealSize <= BYTES_IN_GB) {
            totalSizeStr = "1GB";
        } else if(totalRealSize <= BYTES_IN_1_POINT_5_GB)  // prize-add-by-yanghao-20160503 for k559 volte
		{
			totalSizeStr = "1.5GB";
		}else if (totalRealSize <= BYTES_IN_2_GB){
        		totalSizeStr = "2GB";
        } else if (totalRealSize <= BYTES_IN_3_GB){
        		totalSizeStr = "3GB";
        } else if (totalRealSize <= BYTES_IN_4_GB){
        		totalSizeStr = "4GB";
        } else if(totalRealSize <= BYTES_IN_6_GB) {
            totalSizeStr = "6GB";/*prize-add-bugid:55039 add ram_info_size-ganxiayong-20180411*/
        } else if(totalRealSize <= BYTES_IN_8_GB) {
            totalSizeStr = "8GB";
        } else if(totalRealSize <= BYTES_IN_16_GB) {
            totalSizeStr = "16GB";
        } else if(totalRealSize <= BYTES_IN_32_GB) {
            totalSizeStr = "32GB";
        } else if(totalRealSize <= BYTES_IN_64_GB) {
            totalSizeStr = "64GB";
        } else if(totalRealSize <= BYTES_IN_128_GB) {
            totalSizeStr = "128GB";
        } else {
            //keep the orginal format.
            totalSizeStr = "" + totalRealSize;
        }
				Log.d(LOG_TAG, "totalSizeStr : "+totalSizeStr);
				return totalSizeStr;
    }
    
		public long calcPhoneRamTotal() {
				Log.d(LOG_TAG, "calcPhoneRamTotal()");
        String dir = "/proc/meminfo";
        try {
            FileReader fr = new FileReader(dir);
            BufferedReader br = new BufferedReader(fr, 2048);
            String memoryLine = br.readLine();
            String subMemoryLine = memoryLine.substring(memoryLine.indexOf("MemTotal:"));
            br.close();
            return Integer.parseInt(subMemoryLine.replaceAll("\\D+", "")) * 1024l;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }
    
    private long calcPhoneRomTotal() {
        Log.d(LOG_TAG, "calcPhoneRomTotal()");
        long size = 0;
        //cache 
        size += getDirectorySize("/cache");
        //system
        size += getDirectorySize("/system");
        //dev
        size += getDirectorySize("/dev");
        //data
        size += getDirectorySize("/data");

		/*
        if(!FeatureOption.MTK_SHARED_SDCARD) {

        }
		*/
        return size;
    }
    
    
    private long getDirectorySize(String path) {
        
        long totalSize = 0;
        if(path != null) {
            try {
                final StructStatVfs stat = Os.statvfs(path);
                totalSize = stat.f_blocks * stat.f_bsize;
            } catch (ErrnoException e) {
                Log.e(LOG_TAG, "Exception: " + new IllegalStateException(e));
            }
        }
        return totalSize;
    }
    // prize-add-by-yanghao-20160303-end
 //prize-start-pyx 2016-06-30 stop fresh core image
    @Override
    public void onDestroy() {
    	getPreferenceScreen().removePreference(findPreference(KEY_CPU_CORENUMBER));
    	super.onDestroy();
    }
   //prize-end-pyx 2016-06-30 stop fresh core image
    
}
