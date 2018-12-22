/*
 * Copyright (C) 2011 The Android Open Source Project
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

package com.mediatek.settings.deviceinfo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbManager;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceScreen;
import android.util.Log;
import android.support.v7.preference.CheckBoxPreference;
import com.android.settings.R;
import com.android.settings.deviceinfo.UsbSettings;

import java.util.ArrayList;
import java.util.List;
import com.mediatek.settings.FeatureOption;
import com.mediatek.common.prizeoption.PrizeOption;

public class UsbSettingsExts {

    private static final String TAG = "UsbSettings";
    private static final String EXTRA_USB_HW_DISCONNECTED = "USB_HW_DISCONNECTED";
    private static final String EXTRA_USB_IS_PC_KNOW_ME = "USB_IS_PC_KNOW_ME";
    private static final String EXTRA_PLUGGED_TYPE = "plugged";
    private static final String FUNCTION_CHARGING = "charging";
    private static final String PROPERTY_USB_TYPE = "ro.sys.usb.storage.type";
    private static final String DEFAULT_USB_TYPE = "mtp";
    private static final String PROPERTY_USB_CHARGE_ONLY = "ro.sys.usb.charging.only";
    private static final String PROPERTY_USB_BICR = "ro.sys.usb.bicr";
    private static final String FUNCTION_SUPPORT = "yes";
    private static final String FUNCTION_NOT_SUPPORT = "no";
    private static final String PROPERTY_USB_CONFIG = "sys.usb.config";
    private static final String FUNCTION_NONE = "none";
    private static final int DEFAULT_PLUGGED_TYPE = 0;

    private static final String KEY_MTP = "usb_mtp";
    private static final String KEY_PTP = "usb_ptp";
	private static final String KEY_CHARGE = "usb_charge";
    private static final String KEY_USB_CATEGORY = "usb_category";
    private static final int ORDER_UMS = -1;
    private static final int USB_CHARGING_PLUGIN = 2;
    private static final int DUAL_INPUT_CHARGER = 3;
	
    private RadioButtonPreference mMtp;
    private RadioButtonPreference mPtp;
    private RadioButtonPreference mUms;
    private RadioButtonPreference mCharge;
    private RadioButtonPreference mBicr;
	private CheckBoxPreference mDev;
    private String mCurrentFunction = "";
    private List<RadioButtonPreference> mRadioButtonPreferenceList = new ArrayList<RadioButtonPreference>();

    private boolean mNeedUpdate = true;
    private boolean mNeedExit = false;

    public PreferenceScreen addUsbSettingsItem(UsbSettings usbSettings) {
        PreferenceScreen root = usbSettings.getPreferenceScreen();
		Log.e("liup","addUsbSettingsItem root = " + root);
        if (root == null) return null;

        mMtp = (RadioButtonPreference) root.findPreference(KEY_MTP); 
        if(mMtp != null){
            mMtp.setOnPreferenceChangeListener(usbSettings);
            mRadioButtonPreferenceList.add(mMtp);
        }

        mPtp = (RadioButtonPreference) root.findPreference(KEY_PTP);
        if(mPtp != null){
           mPtp.setOnPreferenceChangeListener(usbSettings); 
           mRadioButtonPreferenceList.add(mPtp);
        }

        Context context = usbSettings.getActivity();
		
			mCharge = (RadioButtonPreference) root.findPreference(KEY_CHARGE);
        	mCharge.setOnPreferenceChangeListener(usbSettings);
        	mRadioButtonPreferenceList.add(mCharge);
        return root;
    }

    public void updateEnableStatus(boolean enabled) {
        for (RadioButtonPreference preference : mRadioButtonPreferenceList) {
            preference.setEnabled(enabled);
        }
    }

    public void updateCheckedStatus(String function) {
        RadioButtonPreference currentUsb;
        if (UsbManager.USB_FUNCTION_MTP.equals(function)) {
            currentUsb = mMtp;
        } else if (UsbManager.USB_FUNCTION_PTP.equals(function)) {
            currentUsb = mPtp;
        } else if (UsbManager.USB_FUNCTION_MASS_STORAGE.equals(function)) {
            currentUsb = mUms;
        } else if (UsbManager.USB_FUNCTION_NONE.equals(function)) {
            currentUsb = mCharge;
        } else if (UsbManager.USB_FUNCTION_BICR.equals(function)) {
            currentUsb = mBicr;
        } else {
            currentUsb = null;
        }
        for (RadioButtonPreference usb : mRadioButtonPreferenceList) {
            usb.setChecked(usb.equals(currentUsb));
        }
    }

    public void setCurrentFunction(String function) {
        mCurrentFunction = function;
    }

    public String getFunction(Preference preference) {
        String function = FUNCTION_NONE;
        if (preference == mMtp && mMtp.isChecked()) {
            function = UsbManager.USB_FUNCTION_MTP;
        } else if (preference == mPtp && mPtp.isChecked()) {
            function = UsbManager.USB_FUNCTION_PTP;
        } else if (preference == mUms && mUms.isChecked()) {
            function = UsbManager.USB_FUNCTION_MASS_STORAGE;
        } else if (preference == mCharge && mCharge.isChecked()) {
            function = UsbManager.USB_FUNCTION_NONE;
        } else if (preference == mBicr && mBicr.isChecked()) {
            function = UsbManager.USB_FUNCTION_BICR;
        }
        return function;
    }

    /**
     * After you plug out the usb and plug in again, it will restore to
     * previous function if false, and current function if true.
     *
     * @param preference
     *            the current RadioButtonPreference.
     * @return  Whether need to set current function as default function.
     */
    public boolean isMakeDefault(Preference preference) {
        return !(preference == mBicr);
    }

    public String getCurrentFunction() {
        String functions = android.os.SystemProperties.get(PROPERTY_USB_CONFIG, FUNCTION_NONE);
        Log.d(TAG, "current function: " + functions);
        int commandIndex = functions.indexOf(',');
        return (commandIndex > 0) ? functions.substring(0, commandIndex)
                : functions;
    }

    public IntentFilter getIntentFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbManager.ACTION_USB_STATE);
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        return filter;
    }

    public void dealWithBroadcastEvent(Intent intent) {
        String action = intent.getAction();
        String currentFunction = getCurrentFunction();
        if (UsbManager.ACTION_USB_STATE.equals(action)) {
            boolean isHwUsbConnected = !intent.getBooleanExtra(EXTRA_USB_HW_DISCONNECTED, false);
            if (isHwUsbConnected) {
               
            } else {
                mNeedExit = true;
            }
        } else if (Intent.ACTION_BATTERY_CHANGED.equals(action)) {
            int plugType = intent.getIntExtra(EXTRA_PLUGGED_TYPE, DEFAULT_PLUGGED_TYPE);
            if (plugType == USB_CHARGING_PLUGIN) {
               
            } else {
                mNeedExit = true;
            }
        }
    }

    public boolean isNeedUpdate() {
        return mNeedUpdate;
    }

    public void setNeedUpdate(boolean isNeed) {
        mNeedUpdate = isNeed;
    }

    public boolean isNeedExit() {
        return mNeedExit;
    }
    /*PRIZE-USB-xiaxuefeng-2015-5-22-start*/
	public Preference getCharge() {
		return mCharge;
	}
	/*PRIZE-USB-xiaxuefeng-2015-5-22-end*/
}