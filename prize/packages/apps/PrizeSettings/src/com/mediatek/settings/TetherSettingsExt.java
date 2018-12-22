/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein
 * is confidential and proprietary to MediaTek Inc. and/or its licensors.
 * Without the prior written permission of MediaTek inc. and/or its licensors,
 * any reproduction, modification, use or disclosure of MediaTek Software,
 * and information contained herein, in whole or in part, shall be strictly prohibited.
 *
 * MediaTek Inc. (C) 2010. All rights reserved.
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
 */

package com.mediatek.settings;

import android.app.Activity;
import android.bluetooth.BluetoothDun;
import android.bluetooth.BluetoothPan;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.storage.IMountService;
import android.provider.Settings.System;
import android.support.v14.preference.PreferenceFragment;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.PreferenceGroup;
import android.util.Log;

import com.android.settings.R;
import com.android.settings.Utils;
import com.mediatek.settings.ext.IApnSettingsExt;

import java.util.concurrent.atomic.AtomicReference;

public class TetherSettingsExt implements OnPreferenceChangeListener {
    private static final String TAG = "TetherSettingsExt";

    private static final String KEY_USB_TETHER_TYPE = "usb_tethering_type";
    private static final String KEY_TETHERED_IPV6 = "tethered_ipv6";
    private static final String USB_TETHER_SETTINGS = "usb_tether_settings";
    private static final String USB_DATA_STATE = "mediatek.intent.action.USB_DATA_STATE";

    private static final int BASE_ORDER = -100;

    private IMountService mMountService = null;
    private ConnectivityManager mConnectService;
    private Context mContext;
    private Resources mResources;
    private PreferenceScreen mPrfscreen;
    public ListPreference mUsbTetherType;
    private ListPreference mTetherIpv6;
    public static final String ACTION_WIFI_TETHERED_SWITCH = "action.wifi.tethered_switch";

    private int mBtErrorIpv4;
    private int mBtErrorIpv6;

    private int mUsbErrorIpv4 = ConnectivityManager.TETHER_ERROR_NO_ERROR;
    private int mUsbErrorIpv6 = ConnectivityManager.TETHER_ERROR_IPV6_NO_ERROR;

    private String[] mBluetoothRegexs;

    IApnSettingsExt mExt;

    private BluetoothDun mBluetoothDunProxy;
    private AtomicReference<BluetoothDun> mBluetoothDun = new AtomicReference<BluetoothDun>();

    public TetherSettingsExt(Context context) {
        Log.d(TAG, "TetherSettingsExt");
        mContext = context;
        initServices();
    }

    public void onCreate(PreferenceScreen screen) {
        Log.d(TAG, "onCreate");
        mPrfscreen = screen;
        initPreference(screen);
        /// get plugin
        mExt = UtilsExt.getApnSettingsPlugin(mContext);
        /// add tether apn settings
        mExt.customizeTetherApnSettings(screen);

        mBluetoothDunProxy = new BluetoothDun(mContext, mDunServiceListener);
    }

    public void onStart(Activity activity, BroadcastReceiver receiver) {
        // add the receiver intent filter
        IntentFilter filter = getIntentFilter();
        activity.registerReceiver(receiver, filter);

        // set USB Tether Type attribute
        if (mUsbTetherType != null) {
            mUsbTetherType.setOnPreferenceChangeListener(this);
            int value = System.getInt(activity.getContentResolver(), System.USB_TETHERING_TYPE,
                    System.USB_TETHERING_TYPE_DEFAULT);
            mUsbTetherType.setValue(String.valueOf(value));
            mUsbTetherType.setSummary(activity.getResources().getStringArray(
                    R.array.usb_tether_type_entries)[value]);
        }
    }

    public void onDestroy() {
        BluetoothDun dun = mBluetoothDun.get();
        if (dun != null) {
            dun.close();
            mBluetoothDun.set(null);
            dun = null;
        }
        if (mBluetoothDunProxy != null) {
            mBluetoothDunProxy.close();
            mBluetoothDunProxy = null;
        }
    }

    public boolean onPreferenceChange(Preference preference, Object value) {
        String key = preference.getKey();
        Log.d(TAG, "onPreferenceChange key=" + key);
        if (KEY_USB_TETHER_TYPE.equals(key)) {
            int index = Integer.parseInt(((String) value));
            System.putInt(mContext.getContentResolver(), System.USB_TETHERING_TYPE, index);
            mUsbTetherType
                    .setSummary(mResources.getStringArray(R.array.usb_tether_type_entries)[index]);

            Log.d(TAG, "onPreferenceChange USB_TETHERING_TYPE value = " + index);
        } else if (KEY_TETHERED_IPV6.equals(key)) {
            // save value to provider
            int ipv6Value = Integer.parseInt(String.valueOf(value));
            if (mConnectService != null) {
                mConnectService.setTetheringIpv6Enable(ipv6Value == 1);
            }
            mTetherIpv6.setValueIndex(ipv6Value);
            mTetherIpv6
                .setSummary(mResources.getStringArray(R.array.tethered_ipv6_entries)[ipv6Value]);
        }
        return true;
    }

    private void initPreference(PreferenceScreen screen) {
        // create USB tethering type preference
        if (FeatureOption.MTK_TETHERING_EEM_SUPPORT) {
            mUsbTetherType = (ListPreference) new ListPreference(mContext);
            mUsbTetherType.setKey(KEY_USB_TETHER_TYPE);
            mUsbTetherType.setTitle(R.string.usb_tether_type_title);
            screen.addPreference(mUsbTetherType);
            mUsbTetherType.setEntries(R.array.usb_tether_type_entries);
            mUsbTetherType.setEntryValues(R.array.usb_tether_type_values);
            mUsbTetherType.setPersistent(false);

            int order = BASE_ORDER + 1;
            Preference usbTetherSettings = screen.findPreference(USB_TETHER_SETTINGS);
            if (usbTetherSettings != null) {
                order = usbTetherSettings.getOrder() + 1;
            }

            mUsbTetherType.setOrder(order);
        }

        String[] usbRegexs = mConnectService.getTetherableUsbRegexs();

        // the condition must keep the same with mUsbTether removed
        // TetherSettings.java
        boolean usbAvailable = usbRegexs.length != 0;
        if (!usbAvailable || Utils.isMonkeyRunning()) {
            if (mUsbTetherType != null) {
                Log.d(TAG, "remove mUsbTetherType");
                screen.removePreference(mUsbTetherType);
            }
        }

        // creat IPV4/IPV6 tethering list preference
        if (FeatureOption.MTK_TETHERINGIPV6_SUPPORT) {
            mTetherIpv6 = (ListPreference) new ListPreference(mContext);
            mTetherIpv6.setKey(KEY_TETHERED_IPV6);
            mTetherIpv6.setTitle(R.string.tethered_ipv6_title);
            screen.addPreference(mTetherIpv6);
            mTetherIpv6.setEntries(R.array.tethered_ipv6_entries);
            mTetherIpv6.setEntryValues(R.array.tethered_ipv6_values);
            mTetherIpv6.setPersistent(false);
            mTetherIpv6.setOnPreferenceChangeListener(this);
        }

    }

    public IntentFilter getIntentFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(USB_DATA_STATE);
        filter.addAction(BluetoothPan.ACTION_CONNECTION_STATE_CHANGED);
        filter.addAction(BluetoothDun.STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.WIFI_AP_STATE_CHANGED_ACTION);
        filter.addAction(ACTION_WIFI_TETHERED_SWITCH);
        return filter;
    }

    private synchronized void initServices() {
        // get mount service
        if (mMountService == null) {
            IBinder service = ServiceManager.getService("mount");
            if (service != null) {
                mMountService = IMountService.Stub.asInterface(service);
            } else {
                Log.e(TAG, "Can't get mount service");
            }
        }
        // get connectivity service
        mConnectService = (ConnectivityManager) mContext
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        // get Resource
        mResources = mContext.getResources();
        mBluetoothRegexs = mConnectService.getTetherableBluetoothRegexs();
    }

    public boolean isUMSEnabled() {
        if (mMountService == null) {
            Log.d(TAG, " mMountService is null, return");
            return false;
        }
        try {
            return mMountService.isUsbMassStorageEnabled();
        } catch (RemoteException e) {
            Log.e(TAG, "Util:RemoteException when isUsbMassStorageEnabled: " + e);
            return false;
        } catch (UnsupportedOperationException ex) {
            Log.e(TAG, "this device doesn't support UMS");
            return false;
        }
    }

    /*
     * update ipv4&ipv6 setting preference
     */
    public void updateIpv6Preference(SwitchPreference usbTether, SwitchPreference bluetoothTether,
            WifiManager wifiManager) {
        if (FeatureOption.MTK_TETHERINGIPV6_SUPPORT) {
            if (mTetherIpv6 != null) {
                mTetherIpv6.setEnabled(!usbTether.isChecked() && !bluetoothTether.isChecked()
                        && !wifiManager.isWifiApEnabled());
                if (mConnectService != null) {
                    int ipv6Value = mConnectService.getTetheringIpv6Enable() ? 1 : 0;
                    mTetherIpv6.setValueIndex(ipv6Value);
                    mTetherIpv6.setSummary(mResources.getStringArray(
                            R.array.tethered_ipv6_entries)[ipv6Value]);
                }
            }
        }
    }

    public void updateBTPrfSummary(Preference pref, String originSummary) {
        if (FeatureOption.MTK_TETHERINGIPV6_SUPPORT) {
            pref.setSummary(originSummary + getIPV6String(mBtErrorIpv4, mBtErrorIpv6));
        } else {
            pref.setSummary(originSummary);
        }
    }

    public void updateUSBPrfSummary(Preference pref, String originSummary, boolean usbTethered,
            boolean usbAvailable) {
        if (usbTethered) {
            if (FeatureOption.MTK_TETHERINGIPV6_SUPPORT) {
                pref.setSummary(originSummary + getIPV6String(mUsbErrorIpv4, mUsbErrorIpv6));
            } else {
                pref.setSummary(originSummary);
            }
        }

        if (usbAvailable) {
            if (FeatureOption.MTK_TETHERINGIPV6_SUPPORT) {
                if (mUsbErrorIpv4 == ConnectivityManager.TETHER_ERROR_NO_ERROR
                        || mUsbErrorIpv6 == ConnectivityManager.TETHER_ERROR_IPV6_NO_ERROR) {
                    pref.setSummary(R.string.usb_tethering_available_subtext);
                } else {
                    pref.setSummary(R.string.usb_tethering_errored_subtext);
                }
            }
        }
    }

    public void updateUsbTypeListState(boolean state) {
        if (mUsbTetherType != null) {
            Log.d(TAG, "set USB Tether Type state = " + state);
            mUsbTetherType.setEnabled(state);
        }
    }

    public void getBTErrorCode(String[] available) {
        if (FeatureOption.MTK_TETHERINGIPV6_SUPPORT) {
            mBtErrorIpv4 = ConnectivityManager.TETHER_ERROR_NO_ERROR;
            mBtErrorIpv6 = ConnectivityManager.TETHER_ERROR_IPV6_NO_ERROR;
            for (String s : available) {
                for (String regex : mBluetoothRegexs) {
                    if (s.matches(regex) && mConnectService != null) {
                        if (mBtErrorIpv4 == ConnectivityManager.TETHER_ERROR_NO_ERROR) {
                            mBtErrorIpv4 = (mConnectService.getLastTetherError(s) & 0x0f);
                        }
                        if (mBtErrorIpv6 == ConnectivityManager.TETHER_ERROR_IPV6_NO_ERROR) {
                            mBtErrorIpv6 = (mConnectService.getLastTetherError(s) & 0xf0);
                        }
                    }
                }
            }
        }
    }

    /*
     * get ipv6 string
     */
    public String getIPV6String(int errorIpv4, int errorIpv6) {
        String text = "";
        if (mTetherIpv6 != null && "1".equals(mTetherIpv6.getValue())) {
            Log.d(TAG, "[errorIpv4 =" + errorIpv4 + "];" + "[errorIpv6 =" + errorIpv6 + "];");
            if (errorIpv4 == ConnectivityManager.TETHER_ERROR_NO_ERROR
                    && errorIpv6 == ConnectivityManager.TETHER_ERROR_IPV6_AVAIABLE) {
                text = mResources.getString(R.string.tethered_ipv4v6);
            } else if (errorIpv4 == ConnectivityManager.TETHER_ERROR_NO_ERROR) {
                text = mResources.getString(R.string.tethered_ipv4);
            } else if (errorIpv6 == ConnectivityManager.TETHER_ERROR_IPV6_AVAIABLE) {
                text = mResources.getString(R.string.tethered_ipv6);
            }
        }
        return text;
    }

    public int getUSBErrorCode(String[] available, String[] tethered, String[] usbRegexs) {
        int usbError = ConnectivityManager.TETHER_ERROR_NO_ERROR;
        if (FeatureOption.MTK_TETHERINGIPV6_SUPPORT) {
            mUsbErrorIpv4 = ConnectivityManager.TETHER_ERROR_NO_ERROR;
            mUsbErrorIpv6 = ConnectivityManager.TETHER_ERROR_IPV6_NO_ERROR;
        }
        for (String s : available) {
            for (String regex : usbRegexs) {
                if (s.matches(regex) && mConnectService != null) {
                    if (FeatureOption.MTK_TETHERINGIPV6_SUPPORT) {
                        if (mUsbErrorIpv4 == ConnectivityManager.TETHER_ERROR_NO_ERROR) {
                            mUsbErrorIpv4 = (mConnectService.getLastTetherError(s) & 0x0f);
                        }
                        if (mUsbErrorIpv6 == ConnectivityManager.TETHER_ERROR_IPV6_NO_ERROR) {
                            mUsbErrorIpv6 = (mConnectService.getLastTetherError(s) & 0xf0);
                        }
                    } else {
                        if (usbError == ConnectivityManager.TETHER_ERROR_NO_ERROR) {
                            usbError = mConnectService.getLastTetherError(s);
                        }
                    }
                }
            }
        }

        if (FeatureOption.MTK_TETHERINGIPV6_SUPPORT) {
            for (String s : tethered) {
                for (String regex : usbRegexs) {
                    if (s.matches(regex)) {
                        if (mConnectService != null) {
                            if (mUsbErrorIpv6 == ConnectivityManager.TETHER_ERROR_IPV6_NO_ERROR) {
                                mUsbErrorIpv6 = (mConnectService.getLastTetherError(s) & 0xf0);
                            }
                        }
                    }
                }
            }
        }

        return usbError;
    }

    public void updateBtTetherState(SwitchPreference btPrf) {
        BluetoothDun dun = BluetoothDunGetProxy();
        if (dun != null && dun.isTetheringOn() && btPrf != null) {
            btPrf.setChecked(true);
        } else {
            btPrf.setChecked(false);
        }
    }

    public void updateBtDunTether(boolean state) {
        BluetoothDun bluetoothDun = BluetoothDunGetProxy();
        if (bluetoothDun != null) {
            bluetoothDun.setBluetoothTethering(state);
        }
    }

    private BluetoothDun.ServiceListener mDunServiceListener = new BluetoothDun.ServiceListener() {
        public void onServiceConnected(BluetoothDun proxy) {
            Log.d(TAG, "BluetoothDun service connected");
            mBluetoothDun.set((BluetoothDun) proxy);
        }

        public void onServiceDisconnected() {
            Log.d(TAG, "BluetoothDun service disconnected");
            BluetoothDun dun = mBluetoothDun.get();
            if (dun != null) {
                dun.close();
                dun = null;
            }
            mBluetoothDun.set(null);
            mBluetoothDunProxy = null;
        }
    };

    public BluetoothDun BluetoothDunGetProxy() {
        BluetoothDun Dun = mBluetoothDun.get();
        if (Dun == null) {
            if (mBluetoothDunProxy != null) {
                mBluetoothDun.set(mBluetoothDunProxy);
            } else {
                mBluetoothDunProxy = new BluetoothDun(mContext, mDunServiceListener);
            }
            return mBluetoothDunProxy;
        } else {
            return Dun;
        }
    }
}
