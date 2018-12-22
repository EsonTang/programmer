/*
 * Copyright (C) 2015 The Android Open Source Project
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
package com.android.settings.deviceinfo;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.usb.UsbManager;
import android.hardware.usb.UsbPort;
import android.hardware.usb.UsbPortStatus;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;

public class UsbBackend {

    private static final int MODE_POWER_MASK  = 0x01;
    public static final int MODE_POWER_SINK   = 0x00;
    public static final int MODE_POWER_SOURCE = 0x01;

    /// M: Update MODE_DATA_MASK to add new type
    private static final int MODE_DATA_MASK  = 0x07 << 1;
    public static final int MODE_DATA_NONE   = 0x00 << 1;
    public static final int MODE_DATA_MTP    = 0x01 << 1;
    public static final int MODE_DATA_PTP    = 0x02 << 1;
    public static final int MODE_DATA_MIDI   = 0x03 << 1;
    /// M: Add for Built-in CD-ROM and USB Mass Storage
    public static final int MODE_DATA_MASS_STORAGE = 0x04 << 1;
    public static final int MODE_DATA_BICR   = 0x05 << 1;

    /// M: Add for Built-in CD-ROM and USB Mass Storage
    private static final String PROPERTY_USB_BICR = "ro.sys.usb.bicr";
    private static final String FUNCTION_SUPPORT = "yes";
    private static final String FUNCTION_NOT_SUPPORT = "no";
    private static boolean sBicrSupport = FUNCTION_SUPPORT
            .equals(SystemProperties.get(PROPERTY_USB_BICR,
                    FUNCTION_NOT_SUPPORT));
    private static final String PROPERTY_USB_TYPE = "ro.sys.usb.storage.type";
    private static final String DEFAULT_USB_TYPE = "mtp";
    private static boolean sUmsSupport = SystemProperties.get(
            PROPERTY_USB_TYPE, DEFAULT_USB_TYPE).equals(
            UsbManager.USB_FUNCTION_MTP + ","
                    + UsbManager.USB_FUNCTION_MASS_STORAGE);

    private final boolean mRestricted;
    private final boolean mRestrictedBySystem;
    private final boolean mMidi;

    private UserManager mUserManager;
    private UsbManager mUsbManager;
    private UsbPort mPort;
    private UsbPortStatus mPortStatus;

    private boolean mIsUnlocked;

    public UsbBackend(Context context) {
        Intent intent = context.registerReceiver(null,
                new IntentFilter(UsbManager.ACTION_USB_STATE));
        mIsUnlocked = intent == null ?
                false : intent.getBooleanExtra(UsbManager.USB_DATA_UNLOCKED, false);

        mUserManager = UserManager.get(context);
        mUsbManager = context.getSystemService(UsbManager.class);

        mRestricted = mUserManager.hasUserRestriction(UserManager.DISALLOW_USB_FILE_TRANSFER);
        mRestrictedBySystem = mUserManager.hasBaseUserRestriction(
                UserManager.DISALLOW_USB_FILE_TRANSFER, UserHandle.of(UserHandle.myUserId()));
        mMidi = context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_MIDI);

        UsbPort[] ports = mUsbManager.getPorts();
        // For now look for a connected port, in the future we should identify port in the
        // notification and pick based on that.
        final int N = ports.length;
        for (int i = 0; i < N; i++) {
            UsbPortStatus status = mUsbManager.getPortStatus(ports[i]);
            if (status.isConnected()) {
                mPort = ports[i];
                mPortStatus = status;
                break;
            }
        }
    }

    public int getCurrentMode() {
        if (mPort != null) {
            int power = mPortStatus.getCurrentPowerRole() == UsbPort.POWER_ROLE_SOURCE
                    ? MODE_POWER_SOURCE : MODE_POWER_SINK;
            return power | getUsbDataMode();
        }
        return MODE_POWER_SINK | getUsbDataMode();
    }

    public int getUsbDataMode() {
			mIsUnlocked = mUsbManager.isUsbDataUnlocked();//add liup 20171218 45893
        if (!mIsUnlocked) {
            return MODE_DATA_NONE;
        } else if (mUsbManager.isFunctionEnabled(UsbManager.USB_FUNCTION_MTP)) {
            return MODE_DATA_MTP;
        } else if (mUsbManager.isFunctionEnabled(UsbManager.USB_FUNCTION_PTP)) {
            return MODE_DATA_PTP;
        } else if (mUsbManager.isFunctionEnabled(UsbManager.USB_FUNCTION_MIDI)) {
            return MODE_DATA_MIDI;
        /// M: Add for Built-in CD-ROM and USB Mass Storage @{
        } else if (mUsbManager.isFunctionEnabled(UsbManager.USB_FUNCTION_MASS_STORAGE)) {
            return MODE_DATA_MASS_STORAGE;
        } else if (mUsbManager.isFunctionEnabled(UsbManager.USB_FUNCTION_BICR)) {
            return MODE_DATA_BICR;
        /// M: @}
        }
        return MODE_DATA_NONE; // ...
    }

    private void setUsbFunction(int mode) {
        switch (mode) {
            case MODE_DATA_MTP:
                mUsbManager.setCurrentFunction(UsbManager.USB_FUNCTION_MTP);
                mUsbManager.setUsbDataUnlocked(true);
                break;
            case MODE_DATA_PTP:
                mUsbManager.setCurrentFunction(UsbManager.USB_FUNCTION_PTP);
                mUsbManager.setUsbDataUnlocked(true);
                break;
            case MODE_DATA_MIDI:
                mUsbManager.setCurrentFunction(UsbManager.USB_FUNCTION_MIDI);
                mUsbManager.setUsbDataUnlocked(true);
                break;
            /// M: Add for Built-in CD-ROM and USB Mass Storage @{
            case MODE_DATA_MASS_STORAGE:
                mUsbManager.setCurrentFunction(UsbManager.USB_FUNCTION_MASS_STORAGE);
                mUsbManager.setUsbDataUnlocked(true);
                break;
            case MODE_DATA_BICR:
                mUsbManager.setCurrentFunction(UsbManager.USB_FUNCTION_BICR);
                mUsbManager.setUsbDataUnlocked(true);
                break;
            /// M: @}
            default:
                mUsbManager.setCurrentFunction(null);
                mUsbManager.setUsbDataUnlocked(false);
                break;
        }
    }

    public void setMode(int mode) {
        if (mPort != null) {
            int powerRole = modeToPower(mode);
            // If we aren't using any data modes and we support host mode, then go to host mode
            // so maybe? the other device can provide data if it wants, otherwise go into device
            // mode because we have no choice.
            int dataRole = (mode & MODE_DATA_MASK) == MODE_DATA_NONE
                    && mPortStatus.isRoleCombinationSupported(powerRole, UsbPort.DATA_ROLE_HOST)
                    ? UsbPort.DATA_ROLE_HOST : UsbPort.DATA_ROLE_DEVICE;
            mUsbManager.setPortRoles(mPort, powerRole, dataRole);
        }
        setUsbFunction(mode & MODE_DATA_MASK);
    }

    private int modeToPower(int mode) {
        return (mode & MODE_POWER_MASK) == MODE_POWER_SOURCE
                    ? UsbPort.POWER_ROLE_SOURCE : UsbPort.POWER_ROLE_SINK;
    }

    public boolean isModeDisallowed(int mode) {
        if (mRestricted && (mode & MODE_DATA_MASK) != MODE_DATA_NONE
                && (mode & MODE_DATA_MASK) != MODE_DATA_MIDI) {
            // No USB data modes are supported.
            return true;
        }
        return false;
    }

    public boolean isModeDisallowedBySystem(int mode) {
        if (mRestrictedBySystem && (mode & MODE_DATA_MASK) != MODE_DATA_NONE
                && (mode & MODE_DATA_MASK) != MODE_DATA_MIDI) {
            // No USB data modes are supported.
            return true;
        }
        return false;
    }

    public boolean isModeSupported(int mode) {
        if (!mMidi && (mode & MODE_DATA_MASK) == MODE_DATA_MIDI) {
            return false;
        }

        if (mPort != null) {
            int power = modeToPower(mode);
            if ((mode & MODE_DATA_MASK) != 0) {
                // We have a port and data, need to be in device mode.
                return mPortStatus.isRoleCombinationSupported(power,
                        UsbPort.DATA_ROLE_DEVICE);
            } else {
                // No data needed, we can do this power mode in either device or host.
                return mPortStatus.isRoleCombinationSupported(power, UsbPort.DATA_ROLE_DEVICE)
                        || mPortStatus.isRoleCombinationSupported(power, UsbPort.DATA_ROLE_HOST);
            }
        }
        /// M: Add for Built-in CD-ROM and USB Mass Storage @{
        boolean added = true;
        switch (mode & MODE_DATA_MASK) {
            case MODE_DATA_MASS_STORAGE:
                added = sUmsSupport;
                break;
            case MODE_DATA_BICR:
                added = sBicrSupport;
                break;
            default:
                break;
        }
        /// M: @}
        // No port, support sink modes only.
        return added && ((mode & MODE_POWER_MASK) != MODE_POWER_SOURCE);
    }
}
