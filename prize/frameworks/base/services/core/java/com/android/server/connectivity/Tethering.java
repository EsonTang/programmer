/*
* Copyright (C) 2014 MediaTek Inc.
* Modification based on code covered by the mentioned copyright
* and/or permission notice(s).
*/
/*
 * Copyright (C) 2010 The Android Open Source Project
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

package com.android.server.connectivity;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothPan;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothProfile.ServiceListener;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.hardware.usb.UsbManager;
import android.net.ConnectivityManager;
import android.net.ConnectivityManager.NetworkCallback;
import android.net.INetworkPolicyManager;
import android.net.INetworkStatsService;
import android.net.LinkAddress;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.net.NetworkState;
import android.net.NetworkUtils;
import android.net.RouteInfo;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.INetworkManagementService;
import android.os.Looper;
import android.os.Message;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.telephony.CarrierConfigManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;
import android.util.SparseArray;

import com.android.internal.telephony.IccCardConstants;
import com.android.internal.telephony.TelephonyIntents;
import com.android.internal.util.IndentingPrintWriter;
import com.android.internal.util.MessageUtils;
import com.android.internal.util.Protocol;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;
import com.android.server.IoThread;
import com.android.server.connectivity.tethering.IControlsTethering;
import com.android.server.connectivity.tethering.IPv6TetheringCoordinator;
import com.android.server.connectivity.tethering.MdDirectTethering;
import com.android.server.connectivity.tethering.TetherInterfaceStateMachine;
import com.android.server.net.BaseNetworkObserver;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

/** M: MediaTek imports @{ */
import android.content.res.Configuration;
import android.os.Build;
import android.os.SystemProperties;
import android.telephony.SubscriptionManager;
import android.widget.Toast;
/// @}

/**
 * @hide
 *
 * This class holds much of the business logic to allow Android devices
 * to act as IP gateways via USB, BT, and WiFi interfaces.
 */
public class Tethering extends BaseNetworkObserver implements IControlsTethering {

    private final Context mContext;
    private final static String TAG = "Tethering";
    /// M: Reduce log on user/userdebug load@ {
    private static final String PROP_FORCE_DEBUG_KEY = "persist.log.tag.tel_dbg";
    private static final boolean IS_USER_BUILD =
            "user".equals(Build.TYPE) || "userdebug".equals(Build.TYPE);
    private final static boolean DBG = IS_USER_BUILD ? Log.isLoggable(TAG, Log.DEBUG)
                        || (SystemProperties.getInt(PROP_FORCE_DEBUG_KEY, 0) == 1) : true;
    private final static boolean VDBG = IS_USER_BUILD ? Log.isLoggable(TAG, Log.DEBUG)
                        || (SystemProperties.getInt(PROP_FORCE_DEBUG_KEY, 0) == 1) : true;
    /// }

    private static final Class[] messageClasses = {
            Tethering.class, TetherMasterSM.class, TetherInterfaceStateMachine.class
    };
    private static final SparseArray<String> sMagicDecoderRing =
            MessageUtils.findMessageNames(messageClasses);

    // TODO - remove both of these - should be part of interface inspection/selection stuff
    private String[] mTetherableUsbRegexs;
    private String[] mTetherableWifiRegexs;
    private String[] mTetherableBluetoothRegexs;
    private Collection<Integer> mUpstreamIfaceTypes;

    // used to synchronize public access to members
    private final Object mPublicSync;

    private static final Integer MOBILE_TYPE = new Integer(ConnectivityManager.TYPE_MOBILE);
    private static final Integer HIPRI_TYPE = new Integer(ConnectivityManager.TYPE_MOBILE_HIPRI);
    private static final Integer DUN_TYPE = new Integer(ConnectivityManager.TYPE_MOBILE_DUN);

    // if we have to connect to mobile, what APN type should we use?  Calculated by examining the
    // upstream type list and the DUN_REQUIRED secure-setting
    private int mPreferredUpstreamMobileApn = ConnectivityManager.TYPE_NONE;

    private final INetworkManagementService mNMService;
    private final INetworkStatsService mStatsService;
    private final INetworkPolicyManager mPolicyManager;
    private final Looper mLooper;

    private static class TetherState {
        public final TetherInterfaceStateMachine mStateMachine;
        public int mLastState;
        public int mLastError;
        public TetherState(TetherInterfaceStateMachine sm) {
            mStateMachine = sm;
            // Assume all state machines start out available and with no errors.
            mLastState = IControlsTethering.STATE_AVAILABLE;
            mLastError = ConnectivityManager.TETHER_ERROR_NO_ERROR;
        }
    }
    private final ArrayMap<String, TetherState> mTetherStates;

    private final BroadcastReceiver mStateReceiver;

    // {@link ComponentName} of the Service used to run tether provisioning.
    private static final ComponentName TETHER_SERVICE = ComponentName.unflattenFromString(Resources
            .getSystem().getString(com.android.internal.R.string.config_wifi_tether_enable));

    // USB is  192.168.42.1 and 255.255.255.0
    // Wifi is 192.168.43.1 and 255.255.255.0
    // BT is limited to max default of 5 connections. 192.168.44.1 to 192.168.48.1
    // with 255.255.255.0
    // P2P is 192.168.49.1 and 255.255.255.0

    private String[] mDhcpRange;
    private static final String[] DHCP_DEFAULT_RANGE = {
        "192.168.42.2", "192.168.42.254", "192.168.43.2", "192.168.43.254",
        "192.168.44.2", "192.168.44.254", "192.168.45.2", "192.168.45.254",
        "192.168.46.2", "192.168.46.254", "192.168.47.2", "192.168.47.254",
        "192.168.48.2", "192.168.48.254", "192.168.49.2", "192.168.49.254",
    };

    private String[] mDefaultDnsServers;
    private static final String DNS_DEFAULT_SERVER1 = "8.8.8.8";
    private static final String DNS_DEFAULT_SERVER2 = "8.8.4.4";

    private final StateMachine mTetherMasterSM;
    private final UpstreamNetworkMonitor mUpstreamNetworkMonitor;
    private String mCurrentUpstreamIface;

    private Notification.Builder mTetheredNotificationBuilder;
    private int mLastNotificationId;

    private boolean mRndisEnabled;       // track the RNDIS function enabled state
    private boolean mUsbTetherRequested; // true if USB tethering should be started
                                         // when RNDIS is enabled
    private boolean mIsTetheringChangeDone = false;

    ///M: ALPS00433208 JE due to race condition
    private Object  mNotificationSync;

    /** M: ipv6 tethering @{ */
    private boolean mIpv6FeatureEnable = false;
    /** @} */

    /// M: For automatic NS-IOT test
    public static final String ACTION_ENABLE_NSIOT_TESTING =
            "android.intent.action.ACTION_ENABLE_NSIOT_TESTING";
    public static final String EXTRA_NSIOT_ENABLED =
            "nsiot_enabled";
    public static final String EXTRA_NSIOT_IP_ADDR =
            "nsiot_ip_addr";
    public static final String SYSTEM_PROPERTY_NSIOT_PENDING =
            "net.nsiot_pending";

    /// M: Direct Tethering support
    MdDirectTethering mMdDirectTethering;

    // True iff WiFi tethering should be started when soft AP is ready.
    private boolean mWifiTetherRequested;

    public Tethering(Context context, INetworkManagementService nmService,
            INetworkStatsService statsService, INetworkPolicyManager policyManager) {
        mContext = context;
        mNMService = nmService;
        mStatsService = statsService;
        mPolicyManager = policyManager;

        mPublicSync = new Object();

        mTetherStates = new ArrayMap<>();

        // make our own thread so we don't anr the system
        mLooper = IoThread.get().getLooper();
        mTetherMasterSM = new TetherMasterSM("TetherMaster", mLooper);
        mTetherMasterSM.start();

        mUpstreamNetworkMonitor = new UpstreamNetworkMonitor();

        mStateReceiver = new StateReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbManager.ACTION_USB_STATE);
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        filter.addAction(WifiManager.WIFI_AP_STATE_CHANGED_ACTION);
        filter.addAction(Intent.ACTION_CONFIGURATION_CHANGED);
        /// M: For automatic NS-IOT test
        filter.addAction(ACTION_ENABLE_NSIOT_TESTING);
        mContext.registerReceiver(mStateReceiver, filter);
        mNotificationSync = new Object();

        filter = new IntentFilter();
        filter.addAction(Intent.ACTION_MEDIA_SHARED);
        filter.addAction(Intent.ACTION_MEDIA_UNSHARED);
        filter.addDataScheme("file");
        mContext.registerReceiver(mStateReceiver, filter);

        mDhcpRange = context.getResources().getStringArray(
                com.android.internal.R.array.config_tether_dhcp_range);
        if ((mDhcpRange.length == 0) || (mDhcpRange.length % 2 ==1)) {
            mDhcpRange = DHCP_DEFAULT_RANGE;
        }

        /// M: Modem direct tethering.
        mMdDirectTethering = new MdDirectTethering(mNMService);

        // load device config info
        updateConfiguration();

        // TODO - remove and rely on real notifications of the current iface
        mDefaultDnsServers = new String[2];
        mDefaultDnsServers[0] = DNS_DEFAULT_SERVER1;
        mDefaultDnsServers[1] = DNS_DEFAULT_SERVER2;

        /// M: For automatic NS-IOT test
        SystemProperties.set(SYSTEM_PROPERTY_NSIOT_PENDING, "false");
    }

    // We can't do this once in the Tethering() constructor and cache the value, because the
    // CONNECTIVITY_SERVICE is registered only after the Tethering() constructor has completed.
    private ConnectivityManager getConnectivityManager() {
        return (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    void updateConfiguration() {
        String[] tetherableUsbRegexs = mContext.getResources().getStringArray(
                com.android.internal.R.array.config_tether_usb_regexs);
        String[] tetherableWifiRegexs = mContext.getResources().getStringArray(
                com.android.internal.R.array.config_tether_wifi_regexs);
        String[] tetherableBluetoothRegexs = mContext.getResources().getStringArray(
                com.android.internal.R.array.config_tether_bluetooth_regexs);

        int ifaceTypes[] = null;

        try {
            final TelephonyManager mTelephonyManager = TelephonyManager.getDefault();
            final int subId = SubscriptionManager.getDefaultDataSubscriptionId();
            String sMccMnc = null;
            String sMcc;
            String sMnc;

            if (SubscriptionManager.isValidSubscriptionId(subId)) {
                sMccMnc = mTelephonyManager.getSimOperator(subId);
            }

            if (sMccMnc != null && sMccMnc.length() >= 5) {
                sMcc = sMccMnc.substring(0, 3);
                sMnc = sMccMnc.substring(3, sMccMnc.length());
                int mcc = Integer.parseInt(sMcc);
                int mnc = Integer.parseInt(sMnc);

                Resources res = getResourcesUsingMccMnc(mContext, mcc, mnc);
                if (res != null) {
                    ifaceTypes = res.getIntArray(
                    com.android.internal.R.array.config_tether_upstream_types);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (ifaceTypes == null) {
            ifaceTypes = mContext.getResources().getIntArray(
                com.android.internal.R.array.config_tether_upstream_types);
        }

        Collection<Integer> upstreamIfaceTypes = new ArrayList<>();
        for (int i : ifaceTypes) {
            upstreamIfaceTypes.add(new Integer(i));
        }

        synchronized (mPublicSync) {
            /// M: MDT feature @{
            if (mMdDirectTethering != null && mMdDirectTethering.isMdtEnable()) {
                mTetherableUsbRegexs = mMdDirectTethering.getMdtTetherableUsbRegexs();
            } else {
                mTetherableUsbRegexs = tetherableUsbRegexs;
            }
            /// @}
            mTetherableWifiRegexs = tetherableWifiRegexs;
            mTetherableBluetoothRegexs = tetherableBluetoothRegexs;
            mUpstreamIfaceTypes = upstreamIfaceTypes;
        }

        // check if the upstream type list needs to be modified due to secure-settings
        checkDunRequired();
    }

    @Override
    public void interfaceStatusChanged(String iface, boolean up) {
        // Never called directly: only called from interfaceLinkStateChanged.
        // See NetlinkHandler.cpp:71.
        if (VDBG) Log.d(TAG, "interfaceStatusChanged " + iface + ", " + up);
        synchronized (mPublicSync) {
            int interfaceType = ifaceNameToType(iface);
            if (interfaceType == ConnectivityManager.TETHERING_INVALID) {
                return;
            }

            TetherState tetherState = mTetherStates.get(iface);
            if (up) {
                if (tetherState == null) {
                    trackNewTetherableInterface(iface, interfaceType);
                }
            } else {

                if (interfaceType == ConnectivityManager.TETHERING_BLUETOOTH) {
                    Log.d(TAG, "interfaceLinkStatusChanged, only" +
                                " interfaceType == TETHERING_BLUETOOTH " +
                                "sendMessage:CMD_INTERFACE_DOWN");
                    tetherState.mStateMachine.sendMessage(
                            TetherInterfaceStateMachine.CMD_INTERFACE_DOWN);
                    mTetherStates.remove(iface);
                } else {
                    // Ignore usb0 down after enabling RNDIS.
                    // We will handle disconnect in interfaceRemoved.
                    // Similarly, ignore interface down for WiFi.  We monitor WiFi AP status
                    // through the WifiManager.WIFI_AP_STATE_CHANGED_ACTION intent.
                    if (VDBG) Log.d(TAG, "ignore interface down for " + iface);
                }
            }
        }
    }

    @Override
    public void interfaceLinkStateChanged(String iface, boolean up) {
        if (VDBG) Log.d(TAG, "interfaceLinkStateChanged " + iface + ", " + up);
        interfaceStatusChanged(iface, up);
    }

    private boolean isUsb(String iface) {
        synchronized (mPublicSync) {
            for (String regex : mTetherableUsbRegexs) {
                if (iface.matches(regex)) return true;
            }
            return false;
        }
    }

    private boolean isWifi(String iface) {
        synchronized (mPublicSync) {
            for (String regex : mTetherableWifiRegexs) {
                if (iface.matches(regex)) return true;
            }
            return false;
        }
    }

    private boolean isBluetooth(String iface) {
        synchronized (mPublicSync) {
            for (String regex : mTetherableBluetoothRegexs) {
                if (iface.matches(regex)) return true;
            }
            return false;
        }
    }

    private int ifaceNameToType(String iface) {
        if (isWifi(iface)) {
            return ConnectivityManager.TETHERING_WIFI;
        } else if (isUsb(iface)) {
            return ConnectivityManager.TETHERING_USB;
        } else if (isBluetooth(iface)) {
            return ConnectivityManager.TETHERING_BLUETOOTH;
        }
        return ConnectivityManager.TETHERING_INVALID;
    }

    @Override
    public void interfaceAdded(String iface) {
        if (VDBG) Log.d(TAG, "interfaceAdded " + iface);
        synchronized (mPublicSync) {
            int interfaceType = ifaceNameToType(iface);
            if (interfaceType == ConnectivityManager.TETHERING_INVALID) {
                if (VDBG) Log.d(TAG, iface + " is not a tetherable iface, ignoring");
                return;
            }

            TetherState tetherState = mTetherStates.get(iface);
            if (tetherState == null) {
                trackNewTetherableInterface(iface, interfaceType);
            } else {
                if (VDBG) Log.d(TAG, "active iface (" + iface + ") reported as added, ignoring");
            }
        }
    }

    @Override
    public void interfaceRemoved(String iface) {
        if (VDBG) Log.d(TAG, "interfaceRemoved " + iface);
        synchronized (mPublicSync) {
            TetherState tetherState = mTetherStates.get(iface);
            if (tetherState == null) {
                if (VDBG) {
                    Log.e(TAG, "attempting to remove unknown iface (" + iface + "), ignoring");
                }
                return;
            }
            Log.d(TAG, "interfaceRemoved, iface=" + iface + ", sendMessage:CMD_INTERFACE_DOWN");
            tetherState.mStateMachine.sendMessage(TetherInterfaceStateMachine.CMD_INTERFACE_DOWN);
            mTetherStates.remove(iface);
        }
    }

    public void startTethering(int type, ResultReceiver receiver,
            boolean showProvisioningUi) {
        Log.d(TAG, "startTethering:" + type);
        if (!isTetherProvisioningRequired()) {
            Log.d(TAG, "Not TetherProvisioningRequired");
            enableTetheringInternal(type, true, receiver);
            return;
        }
        Log.d(TAG, "TetherProvisioningRequired");
        if (showProvisioningUi) {
            runUiTetherProvisioningAndEnable(type, receiver);
        } else {
            runSilentTetherProvisioningAndEnable(type, receiver);
        }
    }

    public void stopTethering(int type) {
        Log.d(TAG, "stopTethering:" + type);
        enableTetheringInternal(type, false, null);
        if (isTetherProvisioningRequired()) {
            cancelTetherProvisioningRechecks(type);
        }
    }

    /**
     * Check if the device requires a provisioning check in order to enable tethering.
     *
     * @return a boolean - {@code true} indicating tether provisioning is required by the carrier.
     */
    private boolean isTetherProvisioningRequired() {
        String[] provisionApp = mContext.getResources().getStringArray(
                com.android.internal.R.array.config_mobile_hotspot_provision_app);
        if (SystemProperties.getBoolean("net.tethering.noprovisioning", false)
                || provisionApp == null) {
            return false;
        }

        // Check carrier config for entitlement checks
        final CarrierConfigManager configManager = (CarrierConfigManager) mContext
             .getSystemService(Context.CARRIER_CONFIG_SERVICE);
        /// M: check the null config @{
        if (configManager == null || configManager.getConfig() == null) {

            if (VDBG) { Log.i(TAG, "configManager is null ,provisionApp.length "
                            + provisionApp.length); }
            //return false;
        }
        /// @}

        if (configManager != null && configManager.getConfig() != null) {
            // we do have a CarrierConfigManager and it has a config.
            boolean isEntitlementCheckRequired = configManager.getConfig().getBoolean(
                    CarrierConfigManager.KEY_REQUIRE_ENTITLEMENT_CHECKS_BOOL);
            if (!isEntitlementCheckRequired) {

                if (VDBG) { Log.i(TAG, "isEntitlementCheckRequired = false ,return false"); }
                return false;
            }
        }
        return (provisionApp.length == 2);
    }

    /**
     * Enables or disables tethering for the given type. This should only be called once
     * provisioning has succeeded or is not necessary. It will also schedule provisioning rechecks
     * for the specified interface.
     */
    private void enableTetheringInternal(int type, boolean enable, ResultReceiver receiver) {
        Log.d(TAG, "enableTetheringInternal type:" + type + ", enable:" + enable);
        boolean isProvisioningRequired = enable && isTetherProvisioningRequired();
        int result;
        switch (type) {
            case ConnectivityManager.TETHERING_WIFI:
                result = setWifiTethering(enable);
                if (isProvisioningRequired && result == ConnectivityManager.TETHER_ERROR_NO_ERROR) {
                    scheduleProvisioningRechecks(type);
                }
                sendTetherResult(receiver, result);
                break;
            case ConnectivityManager.TETHERING_USB:
                result = setUsbTethering(enable);
                if (isProvisioningRequired && result == ConnectivityManager.TETHER_ERROR_NO_ERROR) {
                    scheduleProvisioningRechecks(type);
                }
                sendTetherResult(receiver, result);
                break;
            case ConnectivityManager.TETHERING_BLUETOOTH:
                setBluetoothTethering(enable, receiver);
                break;
            default:
                Log.w(TAG, "Invalid tether type.");
                sendTetherResult(receiver, ConnectivityManager.TETHER_ERROR_UNKNOWN_IFACE);
        }
    }

    private void sendTetherResult(ResultReceiver receiver, int result) {
        if (receiver != null) {
            receiver.send(result, null);
        }
    }

    private int setWifiTethering(final boolean enable) {
        synchronized (mPublicSync) {
            if (VDBG) Log.d(TAG, "setWifiTethering:" + enable);
            mWifiTetherRequested = enable;
            final WifiManager wifiManager =
                    (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
            if (wifiManager.setWifiApEnabled(null /* use existing wifi config */, enable)) {
                return ConnectivityManager.TETHER_ERROR_NO_ERROR;
            }
            return ConnectivityManager.TETHER_ERROR_MASTER_ERROR;
        }
    }

    private void setBluetoothTethering(final boolean enable, final ResultReceiver receiver) {
        final BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null || !adapter.isEnabled()) {
            Log.w(TAG, "Tried to enable bluetooth tethering with null or disabled adapter. null: " +
                    (adapter == null));
            sendTetherResult(receiver, ConnectivityManager.TETHER_ERROR_SERVICE_UNAVAIL);
            return;
        }

        adapter.getProfileProxy(mContext, new ServiceListener() {
            @Override
            public void onServiceDisconnected(int profile) { }

            @Override
            public void onServiceConnected(int profile, BluetoothProfile proxy) {
                ((BluetoothPan) proxy).setBluetoothTethering(enable);
                // TODO: Enabling bluetooth tethering can fail asynchronously here.
                // We should figure out a way to bubble up that failure instead of sending success.
                int result = ((BluetoothPan) proxy).isTetheringOn() == enable ?
                        ConnectivityManager.TETHER_ERROR_NO_ERROR :
                        ConnectivityManager.TETHER_ERROR_MASTER_ERROR;
                sendTetherResult(receiver, result);
                if (enable && isTetherProvisioningRequired()) {
                    scheduleProvisioningRechecks(ConnectivityManager.TETHERING_BLUETOOTH);
                }
                adapter.closeProfileProxy(BluetoothProfile.PAN, proxy);
            }
        }, BluetoothProfile.PAN);
    }

    private void runUiTetherProvisioningAndEnable(int type, ResultReceiver receiver) {
        ResultReceiver proxyReceiver = getProxyReceiver(type, receiver);
        sendUiTetherProvisionIntent(type, proxyReceiver);
    }

    private void sendUiTetherProvisionIntent(int type, ResultReceiver receiver) {
        Intent intent = new Intent(Settings.ACTION_TETHER_PROVISIONING);
        intent.putExtra(ConnectivityManager.EXTRA_ADD_TETHER_TYPE, type);
        intent.putExtra(ConnectivityManager.EXTRA_PROVISION_CALLBACK, receiver);
        final long ident = Binder.clearCallingIdentity();
        try {
            mContext.startActivityAsUser(intent, UserHandle.CURRENT);
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    /**
     * Creates a proxy {@link ResultReceiver} which enables tethering if the provsioning result is
     * successful before firing back up to the wrapped receiver.
     *
     * @param type The type of tethering being enabled.
     * @param receiver A ResultReceiver which will be called back with an int resultCode.
     * @return The proxy receiver.
     */
    private ResultReceiver getProxyReceiver(final int type, final ResultReceiver receiver) {
        ResultReceiver rr = new ResultReceiver(null) {
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                // If provisioning is successful, enable tethering, otherwise just send the error.
                if (resultCode == ConnectivityManager.TETHER_ERROR_NO_ERROR) {
                    enableTetheringInternal(type, true, receiver);
                } else {
                    sendTetherResult(receiver, resultCode);
                }
            }
        };

        // The following is necessary to avoid unmarshalling issues when sending the receiver
        // across processes.
        Parcel parcel = Parcel.obtain();
        rr.writeToParcel(parcel,0);
        parcel.setDataPosition(0);
        ResultReceiver receiverForSending = ResultReceiver.CREATOR.createFromParcel(parcel);
        parcel.recycle();
        return receiverForSending;
    }

    private void scheduleProvisioningRechecks(int type) {
        Log.d(TAG, "scheduleProvisioningRechecks type:" + type);
        Intent intent = new Intent();
        intent.putExtra(ConnectivityManager.EXTRA_ADD_TETHER_TYPE, type);
        intent.putExtra(ConnectivityManager.EXTRA_SET_ALARM, true);
        intent.setComponent(TETHER_SERVICE);
        final long ident = Binder.clearCallingIdentity();
        try {
            mContext.startServiceAsUser(intent, UserHandle.CURRENT);
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    private void runSilentTetherProvisioningAndEnable(int type, ResultReceiver receiver) {
        ResultReceiver proxyReceiver = getProxyReceiver(type, receiver);
        sendSilentTetherProvisionIntent(type, proxyReceiver);
    }

    private void sendSilentTetherProvisionIntent(int type, ResultReceiver receiver) {
        Intent intent = new Intent();
        intent.putExtra(ConnectivityManager.EXTRA_ADD_TETHER_TYPE, type);
        intent.putExtra(ConnectivityManager.EXTRA_RUN_PROVISION, true);
        intent.putExtra(ConnectivityManager.EXTRA_PROVISION_CALLBACK, receiver);
        intent.setComponent(TETHER_SERVICE);
        final long ident = Binder.clearCallingIdentity();
        try {
            mContext.startServiceAsUser(intent, UserHandle.CURRENT);
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    private void cancelTetherProvisioningRechecks(int type) {
        if (getConnectivityManager().isTetheringSupported()) {
            Intent intent = new Intent();
            intent.putExtra(ConnectivityManager.EXTRA_REM_TETHER_TYPE, type);
            intent.setComponent(TETHER_SERVICE);
            final long ident = Binder.clearCallingIdentity();
            try {
                mContext.startServiceAsUser(intent, UserHandle.CURRENT);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }
    }

    public int tether(String iface) {
        if (DBG) Log.d(TAG, "Tethering " + iface);
        synchronized (mPublicSync) {
            TetherState tetherState = mTetherStates.get(iface);
            if (tetherState == null) {
                Log.e(TAG, "Tried to Tether an unknown iface: " + iface + ", ignoring");
                return ConnectivityManager.TETHER_ERROR_UNKNOWN_IFACE;
            }
            // Ignore the error status of the interface.  If the interface is available,
            // the errors are referring to past tethering attempts anyway.
            if (tetherState.mLastState != IControlsTethering.STATE_AVAILABLE) {
                Log.e(TAG, "Tried to Tether an unavailable iface: " + iface + ", ignoring");
                return ConnectivityManager.TETHER_ERROR_UNAVAIL_IFACE;
            }
            tetherState.mStateMachine.sendMessage(TetherInterfaceStateMachine.CMD_TETHER_REQUESTED);
            return ConnectivityManager.TETHER_ERROR_NO_ERROR;
        }
    }

    public int untether(String iface) {
        if (DBG) Log.d(TAG, "Untethering " + iface);
        synchronized (mPublicSync) {
            TetherState tetherState = mTetherStates.get(iface);
            if (tetherState == null) {
                Log.e(TAG, "Tried to Untether an unknown iface :" + iface + ", ignoring");
                return ConnectivityManager.TETHER_ERROR_UNKNOWN_IFACE;
            }
            if (tetherState.mLastState != IControlsTethering.STATE_TETHERED) {
                Log.e(TAG, "Tried to untether an untethered iface :" + iface + ", ignoring");
                return ConnectivityManager.TETHER_ERROR_UNAVAIL_IFACE;
            }
            tetherState.mStateMachine.sendMessage(
                    TetherInterfaceStateMachine.CMD_TETHER_UNREQUESTED);
            return ConnectivityManager.TETHER_ERROR_NO_ERROR;
        }
    }

    public void untetherAll() {
        stopTethering(ConnectivityManager.TETHERING_WIFI);
        stopTethering(ConnectivityManager.TETHERING_USB);
        stopTethering(ConnectivityManager.TETHERING_BLUETOOTH);
    }

    public int getLastTetherError(String iface) {
        synchronized (mPublicSync) {
            TetherState tetherState = mTetherStates.get(iface);
            if (tetherState == null) {
                Log.e(TAG, "Tried to getLastTetherError on an unknown iface :" + iface +
                        ", ignoring");
                return ConnectivityManager.TETHER_ERROR_UNKNOWN_IFACE;
            }
            return tetherState.mLastError; //sm.getLastError();

        }
    }

    private void sendTetherStateChangedBroadcast() {
        if (!getConnectivityManager().isTetheringSupported()) return;

        Log.d(TAG, "sendTetherStateChangedBroadcast");

        ArrayList<String> availableList = new ArrayList<String>();
        ArrayList<String> activeList = new ArrayList<String>();
        ArrayList<String> erroredList = new ArrayList<String>();

        boolean wifiTethered = false;
        boolean usbTethered = false;
        boolean bluetoothTethered = false;

        synchronized (mPublicSync) {

            for (int i = 0; i < mTetherStates.size(); i++) {
                TetherState tetherState = mTetherStates.valueAt(i);
                String iface = mTetherStates.keyAt(i);
                if (tetherState.mLastError != ConnectivityManager.TETHER_ERROR_NO_ERROR) {
                    Log.d(TAG, "add err");
                    erroredList.add(iface);
                } else if (tetherState.mLastState == IControlsTethering.STATE_AVAILABLE) {
                    Log.d(TAG, "add avai");
                    availableList.add(iface);
                } else if (tetherState.mLastState == IControlsTethering.STATE_TETHERED) {
                    if (isUsb(iface)) {
                        Log.d(TAG, "usb isTethered");
                        usbTethered = true;
                    } else if (isWifi(iface)) {
                        Log.d(TAG, "wifi isTethered");
                        wifiTethered = true;
                    } else if (isBluetooth(iface)) {
                        Log.d(TAG, "bt isTethered");
                        bluetoothTethered = true;
                    }
                    activeList.add(iface);
                }
            }
        }
        Intent broadcast = new Intent(ConnectivityManager.ACTION_TETHER_STATE_CHANGED);
        broadcast.addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING |
                Intent.FLAG_RECEIVER_REGISTERED_ONLY_BEFORE_BOOT);
        broadcast.putStringArrayListExtra(ConnectivityManager.EXTRA_AVAILABLE_TETHER,
                availableList);
        broadcast.putStringArrayListExtra(ConnectivityManager.EXTRA_ACTIVE_TETHER, activeList);
        broadcast.putStringArrayListExtra(ConnectivityManager.EXTRA_ERRORED_TETHER,
                erroredList);

        mContext.sendStickyBroadcastAsUser(broadcast, UserHandle.ALL);
        if (DBG) {
            Log.d(TAG, String.format(
                    "sendTetherStateChangedBroadcast avail=[%s] active=[%s] error=[%s]",
                    TextUtils.join(",", availableList),
                    TextUtils.join(",", activeList),
                    TextUtils.join(",", erroredList)));
        }

        if (usbTethered) {
            if (wifiTethered || bluetoothTethered) {
                showTetheredNotification(com.android.internal.R.drawable.stat_sys_tether_general);
            } else {
                showTetheredNotification(com.android.internal.R.drawable.stat_sys_tether_usb);
            }
        } else if (wifiTethered) {
            if (bluetoothTethered) {
                showTetheredNotification(com.android.internal.R.drawable.stat_sys_tether_general);
            } else {
                /* We now have a status bar icon for WifiTethering, so drop the notification */
                clearTetheredNotification();
            }
        } else if (bluetoothTethered) {
            showTetheredNotification(com.android.internal.R.drawable.stat_sys_tether_bluetooth);
        } else {
            clearTetheredNotification();
        }
    }

    private void showTetheredNotification(int icon) {
        Log.i(TAG, "showTetheredNotification icon:" + icon);
        synchronized (Tethering.this.mNotificationSync) {
             NotificationManager notificationManager =
                (NotificationManager)mContext.getSystemService(Context.NOTIFICATION_SERVICE);
             if (notificationManager == null) {
                 return;
             }

             if (mLastNotificationId != 0) {
                 if (mLastNotificationId == icon) {
                     return;
                 }
                 notificationManager.cancelAsUser(null, mLastNotificationId,
                         UserHandle.ALL);
                 mLastNotificationId = 0;
             }

             Intent intent = new Intent();
        intent.setClassName("com.android.settings", "com.android.settings.TetherSettings");
             intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

        PendingIntent pi = PendingIntent.getActivityAsUser(mContext, 0, intent, 0,
                null, UserHandle.CURRENT);

        Resources r = Resources.getSystem();
        CharSequence title = r.getText(com.android.internal.R.string.tethered_notification_title);
        CharSequence message = r.getText(com.android.internal.R.string.
                tethered_notification_message);

        if (mTetheredNotificationBuilder == null) {
            mTetheredNotificationBuilder = new Notification.Builder(mContext);
            mTetheredNotificationBuilder.setWhen(0)
                    .setOngoing(true)
                    .setColor(mContext.getColor(
                            com.android.internal.R.color.system_notification_accent_color))
                    .setVisibility(Notification.VISIBILITY_PUBLIC)
                    .setCategory(Notification.CATEGORY_STATUS);
        }
        mTetheredNotificationBuilder.setSmallIcon(icon)
                .setContentTitle(title)
                .setContentText(message)
                .setContentIntent(pi);
        mLastNotificationId = icon;

        notificationManager.notifyAsUser(null, mLastNotificationId,
                mTetheredNotificationBuilder.build(), UserHandle.ALL);
        }
    }

    private void clearTetheredNotification() {
        Log.i(TAG, "clearTetheredNotification");
        synchronized (Tethering.this.mNotificationSync) {
            NotificationManager notificationManager =
                (NotificationManager)mContext.getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null && mLastNotificationId != 0) {
                notificationManager.cancelAsUser(null, mLastNotificationId,
                        UserHandle.ALL);
                mLastNotificationId = 0;
            }
        }
    }

    private class StateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context content, Intent intent) {
            String action = intent.getAction();
            Log.i(TAG, "StateReceiver onReceive action:" + action);
            if (action == null) { return; }
            if (action.equals(UsbManager.ACTION_USB_STATE)) {
                synchronized (Tethering.this.mPublicSync) {
                    boolean usbConnected = intent.getBooleanExtra(UsbManager.USB_CONNECTED, false);


                    mRndisEnabled = intent.getBooleanExtra(UsbManager.USB_FUNCTION_RNDIS, false);

                    // start tethering if we have a request pending
                    if (usbConnected && mRndisEnabled && mUsbTetherRequested) {
                        Log.i(TAG, "StateReceiver onReceive action synchronized: usbConnected && mRndisEnabled && mUsbTetherRequested, tetherUsb!! ");
                        tetherMatchingInterfaces(true, ConnectivityManager.TETHERING_USB);
                    }
                    mUsbTetherRequested = false;
                }
            } else if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                NetworkInfo networkInfo = (NetworkInfo)intent.getParcelableExtra(
                        ConnectivityManager.EXTRA_NETWORK_INFO);
                if (VDBG) Log.i(TAG, "Tethering got CONNECTIVITY_ACTION, networkInfo:" + networkInfo);
                if (networkInfo != null &&
                        networkInfo.getDetailedState() != NetworkInfo.DetailedState.FAILED) {

                   /// M: ALPS03258912 performance optimization @{
                   /// Remove duplicated TetherMasterSM.CMD_UPSTREAM_CHANGED events
                   if (!mTetherMasterSM.getHandler().hasMessages(
                                TetherMasterSM.CMD_UPSTREAM_CHANGED)) {
                        mTetherMasterSM.sendMessage(TetherMasterSM.CMD_UPSTREAM_CHANGED);
                   }
                   /// @}
                }
            } else if (action.equals(WifiManager.WIFI_AP_STATE_CHANGED_ACTION)) {
                synchronized (Tethering.this.mPublicSync) {
                    int curState =  intent.getIntExtra(WifiManager.EXTRA_WIFI_AP_STATE,
                            WifiManager.WIFI_AP_STATE_DISABLED);
                    if (VDBG) Log.d(TAG, "curState:" + curState);
                    switch (curState) {
                        case WifiManager.WIFI_AP_STATE_ENABLING:
                            // We can see this state on the way to both enabled and failure states.
                            break;
                        case WifiManager.WIFI_AP_STATE_ENABLED:
                            // When the AP comes up and we've been requested to tether it, do so.
                            if (mWifiTetherRequested) {
                                tetherMatchingInterfaces(true, ConnectivityManager.TETHERING_WIFI);
                            }
                            break;
                        case WifiManager.WIFI_AP_STATE_DISABLED:
                        case WifiManager.WIFI_AP_STATE_DISABLING:
                        case WifiManager.WIFI_AP_STATE_FAILED:
                        default:
                            if (DBG) {
                                Log.d(TAG, "Canceling WiFi tethering request - AP_STATE=" +
                                    curState);
                            }
                            // Tell appropriate interface state machines that they should tear
                            // themselves down.
                            for (int i = 0; i < mTetherStates.size(); i++) {
                                TetherInterfaceStateMachine tism =
                                        mTetherStates.valueAt(i).mStateMachine;
                                if (tism.interfaceType() == ConnectivityManager.TETHERING_WIFI) {
                                    tism.sendMessage(
                                            TetherInterfaceStateMachine.CMD_TETHER_UNREQUESTED);
                                    break;  // There should be at most one of these.
                                }
                            }
                            // Regardless of whether we requested this transition, the AP has gone
                            // down.  Don't try to tether again unless we're requested to do so.
                            mWifiTetherRequested = false;
                            break;
                    }
                }
            } else if (action.equals(Intent.ACTION_CONFIGURATION_CHANGED)) {
                updateConfiguration();
            } else if (action.equals(ACTION_ENABLE_NSIOT_TESTING)) {
                /// M: For automatic NS-IOT test
                boolean enabled = intent.getBooleanExtra(EXTRA_NSIOT_ENABLED, false);
                String ipAddr = intent.getStringExtra(EXTRA_NSIOT_IP_ADDR);
                Log.e(TAG, "[NS-IOT]Receieve ACTION_ENABLE_NSIOT_TESTING:" + EXTRA_NSIOT_ENABLED
                    + " = " + enabled
                    + "," + EXTRA_NSIOT_IP_ADDR + " = " + ipAddr);
                SystemProperties.set(SYSTEM_PROPERTY_NSIOT_PENDING, "true");
                enableUdpForwardingForUsb(enabled, ipAddr);
            }
        }
    }

    private void tetherMatchingInterfaces(boolean enable, int interfaceType) {
        if (VDBG) Log.d(TAG, "tetherMatchingInterfaces(" + enable + ", " + interfaceType + ")");

        String[] ifaces = null;
        try {
            ifaces = mNMService.listInterfaces();
        } catch (Exception e) {
            Log.e(TAG, "Error listing Interfaces", e);
            return;
        }

        String chosenIface = null;
        if (ifaces != null) {
            for (String iface : ifaces) {
                if (ifaceNameToType(iface) == interfaceType) {
                    chosenIface = iface;
                    break;
                }
            }
        }

        if (chosenIface == null) {
            Log.e(TAG, "could not find iface of type " + interfaceType);
            return;
        }

        int result = (enable ? tether(chosenIface) : untether(chosenIface));
        if (result != ConnectivityManager.TETHER_ERROR_NO_ERROR) {
            Log.e(TAG, "unable start or stop tethering on iface " + chosenIface);

            return;
        }
    }

    // TODO - return copies so people can't tamper
    public String[] getTetherableUsbRegexs() {
        return mTetherableUsbRegexs;
    }

    public String[] getTetherableWifiRegexs() {
        return mTetherableWifiRegexs;
    }

    public String[] getTetherableBluetoothRegexs() {
        return mTetherableBluetoothRegexs;
    }

    public int setUsbTethering(boolean enable) {
        if (VDBG) Log.d(TAG, "setUsbTethering(" + enable + ")");
         UsbManager usbManager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);

        synchronized (mPublicSync) {
            if (enable) {
                if (mRndisEnabled) {
                    final long ident = Binder.clearCallingIdentity();
                    try {
                        tetherMatchingInterfaces(true, ConnectivityManager.TETHERING_USB);
                    } finally {
                        Binder.restoreCallingIdentity(ident);
                    }
                } else {
                    mUsbTetherRequested = true;
                    Log.d(TAG, "The MTK_TETHERING_RNDIS only");
                    usbManager.setCurrentFunction(UsbManager.USB_FUNCTION_RNDIS);
                }
            } else {
                final long ident = Binder.clearCallingIdentity();
                try {
                    tetherMatchingInterfaces(false, ConnectivityManager.TETHERING_USB);
                } finally {
                    Binder.restoreCallingIdentity(ident);
                }
                if (mRndisEnabled) {
                    usbManager.setCurrentFunction(null);
                }
                mUsbTetherRequested = false;
            }
        }
        return ConnectivityManager.TETHER_ERROR_NO_ERROR;
    }

    public int[] getUpstreamIfaceTypes() {
        int values[];
        synchronized (mPublicSync) {
            updateConfiguration();  // TODO - remove?
            values = new int[mUpstreamIfaceTypes.size()];
            Iterator<Integer> iterator = mUpstreamIfaceTypes.iterator();
            for (int i=0; i < mUpstreamIfaceTypes.size(); i++) {
                values[i] = iterator.next();
            }
        }
        return values;
    }

    private void checkDunRequired() {
        int secureSetting = 2;
        TelephonyManager tm = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        if (tm != null) {
            secureSetting = tm.getTetherApnRequired();
        }
        synchronized (mPublicSync) {
            // 2 = not set, 0 = DUN not required, 1 = DUN required
            if (VDBG) Log.i(TAG, "checkDunRequired:" + secureSetting);
            if (secureSetting != 2) {
                int requiredApn = (secureSetting == 1 ?
                        ConnectivityManager.TYPE_MOBILE_DUN :
                        ConnectivityManager.TYPE_MOBILE_HIPRI);
                if (requiredApn == ConnectivityManager.TYPE_MOBILE_DUN) {
                    while (mUpstreamIfaceTypes.contains(MOBILE_TYPE)) {
                        mUpstreamIfaceTypes.remove(MOBILE_TYPE);
                    }
                    while (mUpstreamIfaceTypes.contains(HIPRI_TYPE)) {
                        mUpstreamIfaceTypes.remove(HIPRI_TYPE);
                    }
                    if (mUpstreamIfaceTypes.contains(DUN_TYPE) == false) {
                        mUpstreamIfaceTypes.add(DUN_TYPE);
                    }
                } else {
                    while (mUpstreamIfaceTypes.contains(DUN_TYPE)) {
                        mUpstreamIfaceTypes.remove(DUN_TYPE);
                    }
                    if (mUpstreamIfaceTypes.contains(MOBILE_TYPE) == false) {
                        mUpstreamIfaceTypes.add(MOBILE_TYPE);
                    }
                    if (mUpstreamIfaceTypes.contains(HIPRI_TYPE) == false) {
                        mUpstreamIfaceTypes.add(HIPRI_TYPE);
                    }
                }
            }
            if (mUpstreamIfaceTypes.contains(DUN_TYPE)) {
                mPreferredUpstreamMobileApn = ConnectivityManager.TYPE_MOBILE_DUN;
            } else {
                mPreferredUpstreamMobileApn = ConnectivityManager.TYPE_MOBILE_HIPRI;
            }
            Log.d(TAG, "mPreferredUpstreamMobileApn = " + mPreferredUpstreamMobileApn);
        }
    }

    // TODO review API - maybe return ArrayList<String> here and below?
    public String[] getTetheredIfaces() {
        ArrayList<String> list = new ArrayList<String>();
        synchronized (mPublicSync) {
            for (int i = 0; i < mTetherStates.size(); i++) {
                TetherState tetherState = mTetherStates.valueAt(i);
                if (tetherState.mLastState == IControlsTethering.STATE_TETHERED) {
                    list.add(mTetherStates.keyAt(i));
                }
            }
        }
        return list.toArray(new String[list.size()]);
    }

    /// M: For automatic NS-IOT test
    public String[] getTetheredIfacePairs() {
        final ArrayList<String> list = new ArrayList<String>();
        synchronized (mPublicSync) {
            for (int i = 0; i < mTetherStates.size(); i++) {
                TetherState tetherState = mTetherStates.valueAt(i);
                if (tetherState.mLastState == IControlsTethering.STATE_TETHERED) {
                    String upStreamIface = tetherState.mStateMachine.upstreamIfaceName();
                    list.add(upStreamIface);
                    list.add(mTetherStates.keyAt(i));
                        Log.i(TAG, "getTetheredIfacePairs:" + upStreamIface
                            + ", " + mTetherStates.keyAt(i));
                }
            }
        }
        return list.toArray(new String[list.size()]);
    }

    public String[] getTetherableIfaces() {
        ArrayList<String> list = new ArrayList<String>();
        synchronized (mPublicSync) {
            for (int i = 0; i < mTetherStates.size(); i++) {
                TetherState tetherState = mTetherStates.valueAt(i);
                if (tetherState.mLastState == IControlsTethering.STATE_AVAILABLE) {
                    list.add(mTetherStates.keyAt(i));
                }
            }
        }
        return list.toArray(new String[list.size()]);
    }

    public String[] getTetheredDhcpRanges() {
        return mDhcpRange;
    }

    public String[] getErroredIfaces() {
        ArrayList<String> list = new ArrayList<String>();
        synchronized (mPublicSync) {
            for (int i = 0; i < mTetherStates.size(); i++) {
                TetherState tetherState = mTetherStates.valueAt(i);
                if (tetherState.mLastError != ConnectivityManager.TETHER_ERROR_NO_ERROR) {
                    list.add(mTetherStates.keyAt(i));
                }
            }
        }
        return list.toArray(new String[list.size()]);
    }

    private void maybeLogMessage(State state, int what) {
        if (DBG) {
            Log.d(TAG, state.getName() + " got " +
                    sMagicDecoderRing.get(what, Integer.toString(what)));
        }
    }

    public boolean getIpv6FeatureEnable() {
        return mIpv6FeatureEnable;
    }

    public void setIpv6FeatureEnable(boolean enable) {
        Log.d(TAG, "setIpv6FeatureEnable:" + enable + " old:" + mIpv6FeatureEnable);
        int value = (enable ? 1 : 0);
        if (mIpv6FeatureEnable != enable) {
            mIpv6FeatureEnable = enable;
            Settings.System.putInt(mContext.getContentResolver()
                , Settings.System.TETHER_IPV6_FEATURE, value);
        }
    }


    /**
     * A NetworkCallback class that relays information of interest to the
     * tethering master state machine thread for subsequent processing.
     */
    class UpstreamNetworkCallback extends NetworkCallback {
        @Override
        public void onAvailable(Network network) {
            mTetherMasterSM.sendMessage(TetherMasterSM.EVENT_UPSTREAM_CALLBACK,
                    UpstreamNetworkMonitor.EVENT_ON_AVAILABLE, 0, network);
        }

        @Override
        public void onCapabilitiesChanged(Network network, NetworkCapabilities newNc) {
            mTetherMasterSM.sendMessage(TetherMasterSM.EVENT_UPSTREAM_CALLBACK,
                    UpstreamNetworkMonitor.EVENT_ON_CAPABILITIES, 0,
                    new NetworkState(null, null, newNc, network, null, null));
        }

        @Override
        public void onLinkPropertiesChanged(Network network, LinkProperties newLp) {
            if (VDBG) { Log.i(TAG, "onLinkPropertiesChanged LinkProperties:" + newLp); }
            mTetherMasterSM.sendMessage(TetherMasterSM.EVENT_UPSTREAM_CALLBACK,
                    UpstreamNetworkMonitor.EVENT_ON_LINKPROPERTIES, 0,
                    new NetworkState(null, newLp, null, network, null, null));
        }

        @Override
        public void onLost(Network network) {
            mTetherMasterSM.sendMessage(TetherMasterSM.EVENT_UPSTREAM_CALLBACK,
                    UpstreamNetworkMonitor.EVENT_ON_LOST, 0, network);
        }
    }

    /**
     * A class to centralize all the network and link properties information
     * pertaining to the current and any potential upstream network.
     *
     * Calling #start() registers two callbacks: one to track the system default
     * network and a second to specifically observe TYPE_MOBILE_DUN networks.
     *
     * The methods and data members of this class are only to be accessed and
     * modified from the tethering master state machine thread. Any other
     * access semantics would necessitate the addition of locking.
     *
     * TODO: Investigate whether more "upstream-specific" logic/functionality
     * could/should be moved here.
     */
    class UpstreamNetworkMonitor {
        static final int EVENT_ON_AVAILABLE      = 1;
        static final int EVENT_ON_CAPABILITIES   = 2;
        static final int EVENT_ON_LINKPROPERTIES = 3;
        static final int EVENT_ON_LOST           = 4;

        final HashMap<Network, NetworkState> mNetworkMap = new HashMap<>();
        NetworkCallback mDefaultNetworkCallback;
        NetworkCallback mDunTetheringCallback;

        void start() {
            stop();

            mDefaultNetworkCallback = new UpstreamNetworkCallback();
            getConnectivityManager().registerDefaultNetworkCallback(mDefaultNetworkCallback);

            final NetworkRequest dunTetheringRequest = new NetworkRequest.Builder()
                    .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                    .removeCapability(NetworkCapabilities.NET_CAPABILITY_NOT_RESTRICTED)
                    .addCapability(NetworkCapabilities.NET_CAPABILITY_DUN)
                    .build();
            mDunTetheringCallback = new UpstreamNetworkCallback();
            getConnectivityManager().registerNetworkCallback(
                    dunTetheringRequest, mDunTetheringCallback);
        }

        void stop() {
            if (mDefaultNetworkCallback != null) {
                getConnectivityManager().unregisterNetworkCallback(mDefaultNetworkCallback);
                mDefaultNetworkCallback = null;
            }

            if (mDunTetheringCallback != null) {
                getConnectivityManager().unregisterNetworkCallback(mDunTetheringCallback);
                mDunTetheringCallback = null;
            }

            mNetworkMap.clear();
        }

        NetworkState lookup(Network network) {
            return (network != null) ? mNetworkMap.get(network) : null;
        }

        NetworkState processCallback(int arg1, Object obj) {
            switch (arg1) {
                case EVENT_ON_AVAILABLE: {
                    final Network network = (Network) obj;
                    if (VDBG) {
                        Log.d(TAG, "EVENT_ON_AVAILABLE for " + network);
                    }
                    if (!mNetworkMap.containsKey(network)) {
                        mNetworkMap.put(network,
                                new NetworkState(null, null, null, network, null, null));
                    }

                    final ConnectivityManager cm = getConnectivityManager();

                    if (mDefaultNetworkCallback != null) {
                        cm.requestNetworkCapabilities(mDefaultNetworkCallback);
                        cm.requestLinkProperties(mDefaultNetworkCallback);
                    }

                    // Requesting updates for mDunTetheringCallback is not
                    // necessary. Because it's a listen, it will already have
                    // heard all NetworkCapabilities and LinkProperties updates
                    // since UpstreamNetworkMonitor was started. Because we
                    // start UpstreamNetworkMonitor before chooseUpstreamType()
                    // is ever invoked (it can register a DUN request) this is
                    // mostly safe. However, if a DUN network is already up for
                    // some reason (unlikely, because DUN is restricted and,
                    // unless the DUN network is shared with another APN, only
                    // the system can request it and this is the only part of
                    // the system that requests it) we won't know its
                    // LinkProperties or NetworkCapabilities.

                    return mNetworkMap.get(network);
                }
                case EVENT_ON_CAPABILITIES: {
                    final NetworkState ns = (NetworkState) obj;
                    if (!mNetworkMap.containsKey(ns.network)) {
                        // Ignore updates for networks for which we have not yet
                        // received onAvailable() - which should never happen -
                        // or for which we have already received onLost().
                        return null;
                    }
                    if (VDBG) {
                        Log.d(TAG, String.format("EVENT_ON_CAPABILITIES for %s: %s",
                                ns.network, ns.networkCapabilities));
                    }

                    final NetworkState prev = mNetworkMap.get(ns.network);
                    mNetworkMap.put(ns.network,
                            new NetworkState(null, prev.linkProperties, ns.networkCapabilities,
                                             ns.network, null, null));
                    return mNetworkMap.get(ns.network);
                }
                case EVENT_ON_LINKPROPERTIES: {
                    final NetworkState ns = (NetworkState) obj;
                    if (!mNetworkMap.containsKey(ns.network)) {
                        // Ignore updates for networks for which we have not yet
                        // received onAvailable() - which should never happen -
                        // or for which we have already received onLost().
                        return null;
                    }
                    if (VDBG) {
                        Log.d(TAG, String.format("EVENT_ON_LINKPROPERTIES for %s: %s",
                                ns.network, ns.linkProperties));
                    }

                    final NetworkState prev = mNetworkMap.get(ns.network);
                    mNetworkMap.put(ns.network,
                            new NetworkState(null, ns.linkProperties, prev.networkCapabilities,
                                             ns.network, null, null));
                    return mNetworkMap.get(ns.network);
                }
                case EVENT_ON_LOST: {
                    final Network network = (Network) obj;
                    if (VDBG) {
                        Log.d(TAG, "EVENT_ON_LOST for " + network);
                    }
                    return mNetworkMap.remove(network);
                }
                default:
                    return null;
            }
        }
    }

    // Needed because the canonical source of upstream truth is just the
    // upstream interface name, |mCurrentUpstreamIface|.  This is ripe for
    // future simplification, once the upstream Network is canonical.
    boolean pertainsToCurrentUpstream(NetworkState ns) {
        if (ns != null && ns.linkProperties != null && mCurrentUpstreamIface != null) {
            for (String ifname : ns.linkProperties.getAllInterfaceNames()) {
                if (mCurrentUpstreamIface.equals(ifname)) {
                    return true;
                }
            }
        }
        return false;
    }

    class TetherMasterSM extends StateMachine {
        private static final int BASE_MASTER                    = Protocol.BASE_TETHERING;
        // an interface SM has requested Tethering
        static final int CMD_TETHER_MODE_REQUESTED              = BASE_MASTER + 1;
        // an interface SM has unrequested Tethering
        static final int CMD_TETHER_MODE_UNREQUESTED            = BASE_MASTER + 2;
        // upstream connection change - do the right thing
        static final int CMD_UPSTREAM_CHANGED                   = BASE_MASTER + 3;
        // we don't have a valid upstream conn, check again after a delay
        static final int CMD_RETRY_UPSTREAM                     = BASE_MASTER + 4;
        // Events from NetworkCallbacks that we process on the master state
        // machine thread on behalf of the UpstreamNetworkMonitor.
        static final int EVENT_UPSTREAM_CALLBACK                = BASE_MASTER + 5;
        // we treated the error and want now to clear it
        static final int CMD_CLEAR_ERROR                        = BASE_MASTER + 6;

        private State mInitialState;
        private State mTetherModeAliveState;

        private State mSetIpForwardingEnabledErrorState;
        private State mSetIpForwardingDisabledErrorState;
        private State mStartTetheringErrorState;
        private State mStopTetheringErrorState;
        private State mSetDnsForwardersErrorState;

        // This list is a little subtle.  It contains all the interfaces that currently are
        // requesting tethering, regardless of whether these interfaces are still members of
        // mTetherStates.  This allows us to maintain the following predicates:
        //
        // 1) mTetherStates contains the set of all currently existing, tetherable, link state up
        //    interfaces.
        // 2) mNotifyList contains all state machines that may have outstanding tethering state
        //    that needs to be torn down.
        //
        // Because we excise interfaces immediately from mTetherStates, we must maintain mNotifyList
        // so that the garbage collector does not clean up the state machine before it has a chance
        // to tear itself down.
        private final ArrayList<TetherInterfaceStateMachine> mNotifyList;
        private final IPv6TetheringCoordinator mIPv6TetheringCoordinator;

        private int mMobileApnReserved = ConnectivityManager.TYPE_NONE;
        private NetworkCallback mMobileUpstreamCallback;

        private static final int UPSTREAM_SETTLE_TIME_MS     = 10000;
        /** M: ipv6 tethering */
        private String mName;

        TetherMasterSM(String name, Looper looper) {
            super(name, looper);
            /** M: ipv6 tethering */
            mName = name;

            //Add states
            mInitialState = new InitialState();
            addState(mInitialState);
            mTetherModeAliveState = new TetherModeAliveState();
            addState(mTetherModeAliveState);

            mSetIpForwardingEnabledErrorState = new SetIpForwardingEnabledErrorState();
            addState(mSetIpForwardingEnabledErrorState);
            mSetIpForwardingDisabledErrorState = new SetIpForwardingDisabledErrorState();
            addState(mSetIpForwardingDisabledErrorState);
            mStartTetheringErrorState = new StartTetheringErrorState();
            addState(mStartTetheringErrorState);
            mStopTetheringErrorState = new StopTetheringErrorState();
            addState(mStopTetheringErrorState);
            mSetDnsForwardersErrorState = new SetDnsForwardersErrorState();
            addState(mSetDnsForwardersErrorState);

            mNotifyList = new ArrayList<>();
            mIPv6TetheringCoordinator = new IPv6TetheringCoordinator(mNotifyList);
            setInitialState(mInitialState);
        }

        class TetherMasterUtilState extends State {
            @Override
            public boolean processMessage(Message m) {
                return false;
            }

            protected boolean turnOnUpstreamMobileConnection(int apnType) {
                if (apnType == ConnectivityManager.TYPE_NONE) { return false; }

                if (apnType != mMobileApnReserved) {
                    // Unregister any previous mobile upstream callback because
                    // this request, if any, will be different.
                    turnOffUpstreamMobileConnection();
                }

                if (mMobileUpstreamCallback != null) {
                    // Looks like we already filed a request for this apnType.
                    return true;
                }

                switch (apnType) {
                    case ConnectivityManager.TYPE_MOBILE_DUN:
                    case ConnectivityManager.TYPE_MOBILE:
                    case ConnectivityManager.TYPE_MOBILE_HIPRI:
                        boolean isCcpMode = SystemProperties.getBoolean(
                            "persist.op12.ccp.mode", false);
                        if (isCcpMode) {
                            Log.i(TAG ,"isCcpMode enabled, don't enable mobile");
                            return false;
                        }
                        mMobileApnReserved = apnType;
                        break;
                    default:
                        return false;
                }
                Log.i(TAG, "[MSM_TetherModeAlive][" + mName + "] mMobileApnReserved:" + mMobileApnReserved);
                final NetworkRequest.Builder builder = new NetworkRequest.Builder()
                        .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR);
                if (apnType == ConnectivityManager.TYPE_MOBILE_DUN) {
                    builder.removeCapability(NetworkCapabilities.NET_CAPABILITY_NOT_RESTRICTED)
                           .addCapability(NetworkCapabilities.NET_CAPABILITY_DUN);
                } else {
                    builder.addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
                }
                final NetworkRequest mobileUpstreamRequest = builder.build();

                // The UpstreamNetworkMonitor's callback will be notified.
                // Therefore, to avoid duplicate notifications, we only register a no-op.
                mMobileUpstreamCallback = new NetworkCallback();

                // TODO: Change the timeout from 0 (no onUnavailable callback) to use some
                // moderate callback time (once timeout callbacks are implemented). This might
                // be useful for updating some UI. Additionally, we should definitely log a
                // message to aid in any subsequent debugging
                if (DBG) Log.d(TAG, "requesting mobile upstream network: " + mobileUpstreamRequest);
                getConnectivityManager().requestNetwork(
                        mobileUpstreamRequest, mMobileUpstreamCallback, 0, apnType);
                return true;
            }

            protected void turnOffUpstreamMobileConnection() {
                if (mMobileUpstreamCallback != null) {
                    getConnectivityManager().unregisterNetworkCallback(mMobileUpstreamCallback);
                    mMobileUpstreamCallback = null;
                }
                mMobileApnReserved = ConnectivityManager.TYPE_NONE;
            }

            protected boolean turnOnMasterTetherSettings() {
                try {
                        mNMService.setIpForwardingEnabled(true);
                } catch (Exception e) {
                    transitionTo(mSetIpForwardingEnabledErrorState);
                    return false;
                }
                try {
                    mNMService.startTethering(mDhcpRange);
                } catch (Exception e) {
                    try {
                        mNMService.stopTethering();
                        mNMService.startTethering(mDhcpRange);
                    } catch (Exception ee) {
                        transitionTo(mStartTetheringErrorState);
                        return false;
                    }
                }
                return true;
            }

            protected boolean turnOffMasterTetherSettings() {
                try {
                    mNMService.stopTethering();
                } catch (Exception e) {
                    transitionTo(mStopTetheringErrorState);
                    return false;
                }
                try {
                        mNMService.setIpForwardingEnabled(false);
                } catch (Exception e) {
                    transitionTo(mSetIpForwardingDisabledErrorState);
                    return false;
                }
                transitionTo(mInitialState);
                return true;
            }

            /** M: dedicated apn feature @{ */
            private boolean checkDataEnabled(int networkType) {
                TelephonyManager tm = TelephonyManager.getDefault();
                boolean dataEnabled = false;

                dataEnabled = tm.getDataEnabled();
                Log.i(TAG, "checkDataEnabled:" + dataEnabled);

                return dataEnabled;
            }
            /** @} */

            protected void chooseUpstreamType(boolean tryCell) {
                final ConnectivityManager cm = getConnectivityManager();
                int upType = ConnectivityManager.TYPE_NONE;
                String iface = null;
                String ifacePD = null;  //need run runDhcpv6PDSequence()
                /// M: MDT feature
                int radioNetworkType = TelephonyManager.NETWORK_TYPE_UNKNOWN;

                updateConfiguration(); // TODO - remove?

                synchronized (mPublicSync) {
                    if (VDBG) {
                        String result="";
                        Iterator<Integer> iterator = mUpstreamIfaceTypes.iterator();
                        for (int i=0; i < mUpstreamIfaceTypes.size(); i++) {
                            result = result + " " +iterator.next();
                        }

                        Log.d(TAG, "[" + mName + "]chooseUpstreamType has upstream iface types: "
                                                                                    + result);
                    }

                    for (Integer netType : mUpstreamIfaceTypes) {
                        NetworkInfo info = cm.getNetworkInfo(netType.intValue());
                        // TODO: if the network is suspended we should consider
                        // that to be the same as connected here.
                        if ((info != null) && info.isConnected()) {
                            upType = netType.intValue();
                            /// M: MDT feature @{
                            if (mMdDirectTethering != null &&
                                    mMdDirectTethering.isMobileUpstream(upType)) {
                                final TelephonyManager tm = TelephonyManager.getDefault();
                                final int subId =
                                        SubscriptionManager.getDefaultDataSubscriptionId();
                                if (SubscriptionManager.isValidSubscriptionId(subId)) {
                                    radioNetworkType = tm.getDataNetworkType(subId);
                                }
                                if (DBG) {
                                    Log.d(TAG, "radioNetworkType:" + radioNetworkType);
                                }
                            }
                            /// @}
                            break;
                        }
                    }
                }

                if (DBG) {
                    Log.d(TAG, "[" + mName + "]chooseUpstreamType(" + tryCell + "),"
                            + " preferredApn="
                            + ConnectivityManager.getNetworkTypeName(mPreferredUpstreamMobileApn)
                            + ", got type="
                            + ConnectivityManager.getNetworkTypeName(upType));
                }
                Log.d(TAG, "pre-checkDataEnabled + " + checkDataEnabled(upType) );
                switch (upType) {
                    case ConnectivityManager.TYPE_MOBILE_DUN:
                    case ConnectivityManager.TYPE_MOBILE_HIPRI:
                        // If we're on DUN, put our own grab on it.
                        //if (checkDataEnabled(upType)) {
                            turnOnUpstreamMobileConnection(upType);
                        //}
                        break;
                    case ConnectivityManager.TYPE_NONE:
                        if (tryCell &&
                                turnOnUpstreamMobileConnection(mPreferredUpstreamMobileApn)) {
                            // We think mobile should be coming up; don't set a retry.
                        } else {
                            sendMessageDelayed(CMD_RETRY_UPSTREAM, UPSTREAM_SETTLE_TIME_MS);
                        }
                        break;
                    default:
                        /* If we've found an active upstream connection that's not DUN/HIPRI
                         * we should stop any outstanding DUN/HIPRI start requests.
                         *
                         * If we found NONE we don't want to do this as we want any previous
                         * requests to keep trying to bring up something we can use.
                         */
                        turnOffUpstreamMobileConnection();
                        break;
                }

                Network network = null;
                if (upType != ConnectivityManager.TYPE_NONE) {
                    LinkProperties linkProperties = cm.getLinkProperties(upType);
                    if (linkProperties != null) {
                            // Find the interface with the default IPv4 route. It may be the
                            // interface described by linkProperties, or one of the interfaces
                            // stacked on top of it.
                            Log.i(TAG, "Finding IPv4 upstream interface on: " + linkProperties);
                            RouteInfo ipv4Default = RouteInfo.selectBestRoute(
                                linkProperties.getAllRoutes(), Inet4Address.ANY);
                            if (ipv4Default != null) {
                                iface = ipv4Default.getInterface();
                                Log.i(TAG, "Found interface " + ipv4Default.getInterface());
                            } else {
                                /// M: Support mobile IPv6 only for machine testing
                                if (ConnectivityManager.isNetworkTypeMobile(upType)
                                        && linkProperties.hasIPv6DefaultRoute()) {
                                    iface = linkProperties.getInterfaceName();
                                    Log.i(TAG, "Found v6 upstream: " + iface);
                                } else {
                                    Log.i(TAG, "No IPv4 upstream interface, giving up.");
                                }
                            }
                        }

                    if (iface != null) {
                        network = cm.getNetworkForType(upType);
                        if (network == null) {
                            Log.e(TAG, "No Network for upstream type " + upType + "!");
                        }
                        setDnsForwarders(network, linkProperties);
                    }
                }

                /// M: MDT feature @{
                String dIface = getDownstreamIface();
                if (dIface != null && mMdDirectTethering != null &&
                        mMdDirectTethering.isMdtEnable() && isUsb(dIface)) {
                    boolean useMdt = mMdDirectTethering.shouldUseMdt(radioNetworkType);
                    mMdDirectTethering.addBridgeInterface(dIface, useMdt);
                }
                /// @}
                notifyTetheredOfNewUpstreamIface(iface);
                NetworkState ns = mUpstreamNetworkMonitor.lookup(network);
                if (ns != null && pertainsToCurrentUpstream(ns)) {
                    // If we already have NetworkState for this network examine
                    // it immediately, because there likely will be no second
                    // EVENT_ON_AVAILABLE (it was already received).
                    handleNewUpstreamNetworkState(ns);
                } else if (mCurrentUpstreamIface == null) {
                    // There are no available upstream networks, or none that
                    // have an IPv4 default route (current metric for success).
                    handleNewUpstreamNetworkState(null);
                }
            }

            protected void setDnsForwarders(final Network network, final LinkProperties lp) {
                String[] dnsServers = mDefaultDnsServers;
                Collection<InetAddress> dnses = lp.getDnsServers();
                // TODO: Properly support the absence of DNS servers.
                if (dnses != null && !dnses.isEmpty()) {
                    //M: support tethering over clatd
                    //M: sort v6 dns first for tethering over clatd
                    Collection<InetAddress> sortedDnses = new ArrayList<InetAddress>();
                    for (InetAddress ia : dnses) {
                        if (ia instanceof Inet6Address) {
                            sortedDnses.add(ia);
                        }
                    }
                    for (InetAddress ia : dnses) {
                        if (ia instanceof Inet4Address) {
                            sortedDnses.add(ia);
                        }
                    }
                    dnses = sortedDnses;
                    // TODO: remove this invocation of NetworkUtils.makeStrings().
                    dnsServers = NetworkUtils.makeStrings(dnses);
                }
                if (VDBG) {
                    Log.d(TAG, "Setting DNS forwarders: Network=" + network +
                           ", dnsServers=" + Arrays.toString(dnsServers));
                }
                try {
                    mNMService.setDnsForwarders(network, dnsServers);
                } catch (Exception e) {
                    // TODO: Investigate how this can fail and what exactly
                    // happens if/when such failures occur.
                    Log.e(TAG, "Setting DNS forwarders failed!");
                    transitionTo(mSetDnsForwardersErrorState);
                }
            }

            protected void notifyTetheredOfNewUpstreamIface(String ifaceName) {
                if (DBG) Log.i(TAG, "[MSM_TetherModeAlive][" + mName +
                    "] Notifying tethered with upstream =" + ifaceName);
                mCurrentUpstreamIface = ifaceName;

                for (TetherInterfaceStateMachine sm : mNotifyList) {
                    sm.sendMessage(TetherInterfaceStateMachine.CMD_TETHER_CONNECTION_CHANGED,
                            ifaceName);
                }

                /// M: NS-IOT testing
                if (ifaceName != null) {
                    if (SystemProperties.getBoolean(SYSTEM_PROPERTY_NSIOT_PENDING, false)) {
                        enableUdpForwardingForUsb(true, null);
                    }
                }
            }


            protected void handleNewUpstreamNetworkState(NetworkState ns) {
                mIPv6TetheringCoordinator.updateUpstreamNetworkState(ns);
            }
        }

        private final AtomicInteger mSimBcastGenerationNumber = new AtomicInteger(0);
        private SimChangeBroadcastReceiver mBroadcastReceiver = null;

        private void startListeningForSimChanges() {
            if (DBG) Log.d(TAG, "startListeningForSimChanges");
            if (mBroadcastReceiver == null) {
                mBroadcastReceiver = new SimChangeBroadcastReceiver(
                        mSimBcastGenerationNumber.incrementAndGet());
                final IntentFilter filter = new IntentFilter();
                filter.addAction(TelephonyIntents.ACTION_SIM_STATE_CHANGED);

                mContext.registerReceiver(mBroadcastReceiver, filter);
            }
        }

        private void stopListeningForSimChanges() {
            if (DBG) Log.d(TAG, "stopListeningForSimChanges");
            if (mBroadcastReceiver != null) {
                mSimBcastGenerationNumber.incrementAndGet();
                mContext.unregisterReceiver(mBroadcastReceiver);
                mBroadcastReceiver = null;
            }
        }

        class SimChangeBroadcastReceiver extends BroadcastReceiver {
            // used to verify this receiver is still current
            final private int mGenerationNumber;

            // used to check the sim state transition from non-loaded to loaded
            private boolean mSimNotLoadedSeen = false;

            public SimChangeBroadcastReceiver(int generationNumber) {
                super();
                mGenerationNumber = generationNumber;
            }

            @Override
            public void onReceive(Context context, Intent intent) {
                if (DBG) {
                    Log.d(TAG, "simchange mGenerationNumber=" + mGenerationNumber +
                            ", current generationNumber=" + mSimBcastGenerationNumber.get());
                }
                if (mGenerationNumber != mSimBcastGenerationNumber.get()) return;

                final String state =
                        intent.getStringExtra(IccCardConstants.INTENT_KEY_ICC_STATE);

                Log.d(TAG, "got Sim changed to state " + state + ", mSimNotLoadedSeen=" +
                        mSimNotLoadedSeen);
                if (!mSimNotLoadedSeen && !IccCardConstants.INTENT_VALUE_ICC_LOADED.equals(state)) {
                    mSimNotLoadedSeen = true;
                }

                if (mSimNotLoadedSeen && IccCardConstants.INTENT_VALUE_ICC_LOADED.equals(state)) {
                    mSimNotLoadedSeen = false;
                    try {
                        if (mContext.getResources().getString(com.android.internal.R.string.
                                config_mobile_hotspot_provision_app_no_ui).isEmpty() == false) {
                            ArrayList<Integer> tethered = new ArrayList<Integer>();
                            synchronized (mPublicSync) {
                                for (int i = 0; i < mTetherStates.size(); i++) {
                                    TetherState tetherState = mTetherStates.valueAt(i);
                                    if (tetherState.mLastState !=
                                            IControlsTethering.STATE_TETHERED) {
                                        continue;  // Skip interfaces that aren't tethered.
                                    }
                                    String iface = mTetherStates.keyAt(i);
                                    int interfaceType = ifaceNameToType(iface);
                                    if (interfaceType != ConnectivityManager.TETHERING_INVALID) {
                                        tethered.add(new Integer(interfaceType));
                                    }
                                }
                            }
                            for (int tetherType : tethered) {
                                Intent startProvIntent = new Intent();
                                startProvIntent.putExtra(
                                        ConnectivityManager.EXTRA_ADD_TETHER_TYPE, tetherType);
                                startProvIntent.putExtra(
                                        ConnectivityManager.EXTRA_RUN_PROVISION, true);
                                startProvIntent.setComponent(TETHER_SERVICE);
                                mContext.startServiceAsUser(startProvIntent, UserHandle.CURRENT);
                            }
                            Log.d(TAG, "re-evaluate provisioning");
                        } else {
                            Log.d(TAG, "no prov-check needed for new SIM");
                        }
                    } catch (Resources.NotFoundException e) {
                        Log.d(TAG, "no prov-check needed for new SIM");
                        // not defined, do nothing
                    }
                }
            }
        }

        class InitialState extends TetherMasterUtilState {
            @Override
            public void enter() {
                Log.i(TAG, "[MSM_Initial][" + mName + "] enter");
            }
            @Override
            public boolean processMessage(Message message) {
                maybeLogMessage(this, message.what);
                if (DBG) Log.d(TAG, "[MSM_Initial][" + mName
                    + "] processMessage what=" + message.what);
                boolean retValue = true;
                switch (message.what) {
                    case CMD_TETHER_MODE_REQUESTED:
                        TetherInterfaceStateMachine who = (TetherInterfaceStateMachine) message.obj;
                        if (VDBG) Log.d(TAG, "[MSM_Initial][" + mName
                            + "] Tether Mode requested by " + who);
                        if (mNotifyList.indexOf(who) < 0) {
                            mNotifyList.add(who);
                            mIPv6TetheringCoordinator.addActiveDownstream(who);
                        }
                        transitionTo(mTetherModeAliveState);
                        break;
                    case CMD_TETHER_MODE_UNREQUESTED:
                        who = (TetherInterfaceStateMachine) message.obj;
                        Log.d(TAG, "[MSM_Initial] CMD_TETHER_MODE_UNREQUESTED ===========>");
                        if (VDBG) Log.d(TAG, "[MSM_Initial][" + mName
                            + "]Tether Mode unrequested by " + who);
                        mNotifyList.remove(who);
                        mIPv6TetheringCoordinator.removeActiveDownstream(who);

                        Log.i(TAG, "[MSM_Initial] CMD_TETHER_MODE_UNREQUESTED <===========");

                        break;
                    default:
                        retValue = false;
                        break;
                }
                return retValue;
            }
        }

        class TetherModeAliveState extends TetherMasterUtilState {
            boolean mTryCell = true;
            @Override
            public void enter() {
                Log.i(TAG, "[MSM_TetherModeAlive][" + mName + "] enter");
                // TODO: examine if we should check the return value.
                turnOnMasterTetherSettings(); // may transition us out
                startListeningForSimChanges();
                mUpstreamNetworkMonitor.start();
                mTryCell = true;  // better try something first pass or crazy tests cases will fail
                chooseUpstreamType(mTryCell);
                mTryCell = !mTryCell;
            }

            @Override
            public void exit() {
                // TODO: examine if we should check the return value.

                Log.i(TAG, "[MSM_TetherModeAlive][" + mName + "] exit");
                turnOffUpstreamMobileConnection();
                mUpstreamNetworkMonitor.stop();
                stopListeningForSimChanges();
                notifyTetheredOfNewUpstreamIface(null);
                handleNewUpstreamNetworkState(null);
            }

            @Override
            public boolean processMessage(Message message) {
                maybeLogMessage(this, message.what);
                if (DBG) Log.d(TAG, "[MSM_TetherModeAlive][" + mName + "] processMessage what="
                    + message.what);
                boolean retValue = true;
                switch (message.what) {
                    case CMD_TETHER_MODE_REQUESTED: {
                        TetherInterfaceStateMachine who = (TetherInterfaceStateMachine)message.obj;
                        if (VDBG) Log.d(TAG, "Tether Mode requested by " + who);
                        if (mNotifyList.indexOf(who) < 0) {
                            mNotifyList.add(who);
                            mIPv6TetheringCoordinator.addActiveDownstream(who);
                        }
                        who.sendMessage(TetherInterfaceStateMachine.CMD_TETHER_CONNECTION_CHANGED,
                                mCurrentUpstreamIface);
                        break;
                    }
                    case CMD_TETHER_MODE_UNREQUESTED: {
                        TetherInterfaceStateMachine who = (TetherInterfaceStateMachine)message.obj;
                        if (VDBG) Log.d(TAG, "Tether Mode unrequested by " + who);

                        if (mNotifyList.remove(who)) {
                            if (DBG) Log.d(TAG, "TetherModeAlive removing notifyee " + who);
                            if (mNotifyList.isEmpty()) {
                                Log.i(TAG, "[MSM_TetherModeAlive] CMD_TETHER_MODE_UNREQUESTED is empty");
                                turnOffMasterTetherSettings(); // transitions appropriately
                            } else {
                                if (DBG) {
                                    Log.d(TAG, "TetherModeAlive still has " + mNotifyList.size() +
                                            " live requests:");

                                    for (TetherInterfaceStateMachine o : mNotifyList) {
                                        Log.d(TAG, "  " + o);
                                    }
                                }
                            }
                        } else {
                           Log.e(TAG, "TetherModeAliveState UNREQUESTED has unknown who: " + who);
                        }


                        mIPv6TetheringCoordinator.removeActiveDownstream(who);
                        Log.i(TAG, "[MSM_TetherModeAlive] CMD_TETHER_MODE_UNREQUESTED <==========");

                        break;
                    }
                    case CMD_UPSTREAM_CHANGED:
                        // need to try DUN immediately if Wifi goes down
                        mTryCell = true;
                        chooseUpstreamType(mTryCell);
                        mTryCell = !mTryCell;
                        break;
                    case CMD_RETRY_UPSTREAM:
                        chooseUpstreamType(mTryCell);
                        mTryCell = !mTryCell;
                        break;
                    case EVENT_UPSTREAM_CALLBACK: {
                        // First: always update local state about every network.
                        final NetworkState ns = mUpstreamNetworkMonitor.processCallback(
                                message.arg1, message.obj);

                        if (ns == null || !pertainsToCurrentUpstream(ns)) {
                            // TODO: In future, this is where upstream evaluation and selection
                            // could be handled for notifications which include sufficient data.
                            // For example, after CONNECTIVITY_ACTION listening is removed, here
                            // is where we could observe a Wi-Fi network becoming available and
                            // passing validation.
                            if (mCurrentUpstreamIface == null) {
                                // If we have no upstream interface, try to run through upstream
                                // selection again.  If, for example, IPv4 connectivity has shown up
                                // after IPv6 (e.g., 464xlat became available) we want the chance to
                                // notice and act accordingly.
                                chooseUpstreamType(false);
                            }
                            break;
                        }

                        switch (message.arg1) {
                            case UpstreamNetworkMonitor.EVENT_ON_AVAILABLE:
                                // The default network changed, or DUN connected
                                // before this callback was processed. Updates
                                // for the current NetworkCapabilities and
                                // LinkProperties have been requested (default
                                // request) or are being sent shortly (DUN). Do
                                // nothing until they arrive; if no updates
                                // arrive there's nothing to do.
                                break;
                            case UpstreamNetworkMonitor.EVENT_ON_CAPABILITIES:
                                handleNewUpstreamNetworkState(ns);
                                break;
                            case UpstreamNetworkMonitor.EVENT_ON_LINKPROPERTIES:
                                setDnsForwarders(ns.network, ns.linkProperties);
                                handleNewUpstreamNetworkState(ns);
                                break;
                            case UpstreamNetworkMonitor.EVENT_ON_LOST:
                                // TODO: Re-evaluate possible upstreams. Currently upstream
                                // reevaluation is triggered via received CONNECTIVITY_ACTION
                                // broadcasts that result in being passed a
                                // TetherMasterSM.CMD_UPSTREAM_CHANGED.
                                handleNewUpstreamNetworkState(null);
                                break;
                            default:
                                break;
                        }
                        break;
                    }
                    default:
                        retValue = false;
                        break;
                }
                return retValue;
            }
        }

        class ErrorState extends State {
            int mErrorNotification;
            @Override
            public boolean processMessage(Message message) {
                Log.i(TAG, "[MSM_Error][" + mName + "] processMessage what=" + message.what);
                boolean retValue = true;
                switch (message.what) {
                    case CMD_TETHER_MODE_REQUESTED:
                        TetherInterfaceStateMachine who = (TetherInterfaceStateMachine)message.obj;
                        who.sendMessage(mErrorNotification);
                        break;
                    case CMD_CLEAR_ERROR:
                        mErrorNotification = ConnectivityManager.TETHER_ERROR_NO_ERROR;
                        transitionTo(mInitialState);
                        break;
                    default:
                       retValue = false;
                }
                return retValue;
            }
            void notify(int msgType) {
                mErrorNotification = msgType;
                for (TetherInterfaceStateMachine sm : mNotifyList) {
                    sm.sendMessage(msgType);
                }
            }

        }
        class SetIpForwardingEnabledErrorState extends ErrorState {
            @Override
            public void enter() {
                Log.e(TAG, "[MSM_Error][" + mName + "] setIpForwardingEnabled");
                notify(TetherInterfaceStateMachine.CMD_IP_FORWARDING_ENABLE_ERROR);
                /** M: for bug solving */
                //transitionTo(mInitialState);
            }
        }

        class SetIpForwardingDisabledErrorState extends ErrorState {
            @Override
            public void enter() {
                Log.e(TAG, "[MSM_Error][" + mName + "] setIpForwardingDisabled");
                notify(TetherInterfaceStateMachine.CMD_IP_FORWARDING_DISABLE_ERROR);
                /** M: for bug solving */
                //transitionTo(mInitialState);
            }
        }

        class StartTetheringErrorState extends ErrorState {
            @Override
            public void enter() {
                Log.e(TAG, "[MSM_Error][" + mName + "] startTethering");
                notify(TetherInterfaceStateMachine.CMD_START_TETHERING_ERROR);
                try {
                        mNMService.setIpForwardingEnabled(false);
                } catch (Exception e) {}
                /** M: for bug solving */
                //transitionTo(mInitialState);
            }
        }

        class StopTetheringErrorState extends ErrorState {
            @Override
            public void enter() {
                Log.e(TAG, "[MSM_Error][" + mName + "] stopTethering");
                notify(TetherInterfaceStateMachine.CMD_STOP_TETHERING_ERROR);
                try {
                        mNMService.setIpForwardingEnabled(false);
                } catch (Exception e) {}
                /** M: for bug solving */
                //transitionTo(mInitialState);
            }
        }

        class SetDnsForwardersErrorState extends ErrorState {
            @Override
            public void enter() {
                Log.e(TAG, "[MSM_Error][" + mName + "] setDnsForwarders");
                notify(TetherInterfaceStateMachine.CMD_SET_DNS_FORWARDERS_ERROR);
                try {
                    mNMService.stopTethering();
                } catch (Exception e) {}
                try {
                        mNMService.setIpForwardingEnabled(false);
                } catch (Exception e) {}
                /** M: for bug solving */
                //transitionTo(mInitialState);
            }
        }
    }

    @Override
    public void dump(FileDescriptor fd, PrintWriter writer, String[] args) {
        // Binder.java closes the resource for us.
        @SuppressWarnings("resource")
        final IndentingPrintWriter pw = new IndentingPrintWriter(writer, "  ");
        if (mContext.checkCallingOrSelfPermission(
                android.Manifest.permission.DUMP) != PackageManager.PERMISSION_GRANTED) {
            pw.println("Permission Denial: can't dump ConnectivityService.Tether " +
                    "from from pid=" + Binder.getCallingPid() + ", uid=" +
                    Binder.getCallingUid());
                    return;
        }

        pw.println("Tethering:");
        pw.increaseIndent();
        pw.print("mUpstreamIfaceTypes:");
        synchronized (mPublicSync) {
            for (Integer netType : mUpstreamIfaceTypes) {
                pw.print(" " + ConnectivityManager.getNetworkTypeName(netType));
            }
            pw.println();

            pw.println("Tether state:");
            pw.increaseIndent();

            for (int i = 0; i < mTetherStates.size(); i++) {
                final String iface = mTetherStates.keyAt(i);
                final TetherState tetherState = mTetherStates.valueAt(i);
                pw.print(iface + " - ");

                switch (tetherState.mLastState) {
                    case IControlsTethering.STATE_UNAVAILABLE:
                        pw.print("UnavailableState");
                        break;
                    case IControlsTethering.STATE_AVAILABLE:
                        pw.print("AvailableState");
                        break;
                    case IControlsTethering.STATE_TETHERED:
                        pw.print("TetheredState");
                        break;
                    default:
                        pw.print("UnknownState");
                        break;
                }
                pw.println(" - lastError = " + tetherState.mLastError);
                //M: Modified for quick debug using dumpsys
                //pw.println("  mMyUpstreamIfaceName: " + o.mMyUpstreamIfaceName);

            }
            pw.decreaseIndent();
        }
        pw.decreaseIndent();
    }

    @Override
    public void notifyInterfaceStateChange(String iface, TetherInterfaceStateMachine who,
                                           int state, int error) {
        synchronized (mPublicSync) {
            TetherState tetherState = mTetherStates.get(iface);
            if (tetherState != null && tetherState.mStateMachine.equals(who)) {
                tetherState.mLastState = state;
                tetherState.mLastError = error;
            } else {
                if (DBG) Log.d(TAG, "got notification from stale iface " + iface);
            }
        }

        if (DBG) {
            Log.d(TAG, "iface " + iface + " notified that it was in state " + state +
                    " with error " + error);
        }

        try {
            // Notify that we're tethering (or not) this interface.
            // This is how data saver for instance knows if the user explicitly
            // turned on tethering (thus keeping us from being in data saver mode).
            mPolicyManager.onTetheringChanged(iface, state == IControlsTethering.STATE_TETHERED);
        } catch (RemoteException e) {
            // Not really very much we can do here.
        }

        // If TetherMasterSM is in ErrorState, TetherMasterSM stays there.
        // Thus we give a chance for TetherMasterSM to recover to InitialState
        // by sending CMD_CLEAR_ERROR
        if (error == ConnectivityManager.TETHER_ERROR_MASTER_ERROR) {
            mTetherMasterSM.sendMessage(TetherMasterSM.CMD_CLEAR_ERROR, who);
        }
        switch (state) {
            case IControlsTethering.STATE_UNAVAILABLE:
            case IControlsTethering.STATE_AVAILABLE:
                mTetherMasterSM.sendMessage(TetherMasterSM.CMD_TETHER_MODE_UNREQUESTED, who);
                break;
            case IControlsTethering.STATE_TETHERED:
                mTetherMasterSM.sendMessage(TetherMasterSM.CMD_TETHER_MODE_REQUESTED, who);
                break;
        }
        sendTetherStateChangedBroadcast();
    }

    private void trackNewTetherableInterface(String iface, int interfaceType) {
        TetherState tetherState;
        /// M: MDT feature @{
        tetherState = new TetherState(new TetherInterfaceStateMachine(iface, mLooper,
                interfaceType, mNMService, mStatsService, this, mMdDirectTethering));
        /// @}
        mTetherStates.put(iface, tetherState);
        tetherState.mStateMachine.start();
    }

    /** M: dedicated apn feature for OP03APNSettingExt
     * @hide
     */
    public boolean isTetheringChangeDone() {

        Log.d(TAG, "TODO: mIsTetheringChangeDone need implement");
        return mIsTetheringChangeDone;
    }

    /// M: MDT feature @{
    private String getDownstreamIface() {
        synchronized (mPublicSync) {
            for (int i = 0; i < mTetherStates.size(); i++) {
                TetherState tetherState = mTetherStates.valueAt(i);
                String iface = mTetherStates.keyAt(i);
                if (tetherState.mLastState == IControlsTethering.STATE_TETHERED) {
                    Log.d(TAG, "getDownstreamIface:" + iface);
                    return iface;
                }
            }
        }
        return null;
    }
    /// @}

    /// M: For automatic NS-IOT test
    private boolean enableUdpForwardingForUsb(boolean enabled, String ipAddr) {
        Toast mToast;
        mToast = Toast.makeText(mContext, null, Toast.LENGTH_SHORT);
        Log.d(TAG, "enableUdpForwardingForUsb:" + enabled + ":" + ipAddr);

        String[] tetherInterfaces = getTetheredIfacePairs();
        if (mCurrentUpstreamIface != null) {
            tetherInterfaces[0] = mCurrentUpstreamIface;
        }

        if (tetherInterfaces.length != 2) {
           Log.e(TAG, "[NS-IOT]Wrong tethering state:" + tetherInterfaces.length);
           mToast.setText("Please only enable one tethering, now:" + tetherInterfaces.length / 2);
           mToast.show();
           return false;
        } else if (tetherInterfaces[0] == null) {
           Log.e(TAG, "[NS-IOT]Upstream is null");
           mToast.setText("[NS-IOT]Upstream is null" + tetherInterfaces.length / 2);
           mToast.show();
           return false;
        }

        String extInterface = tetherInterfaces[0];
        String inInterface = tetherInterfaces[1];

        if (ipAddr == null || ipAddr.length() == 0 || "unknown".equals(ipAddr)) {
            try {
                Log.e(TAG, "[NS-IOT]getUsbClient(" + inInterface);
                mNMService.getUsbClient(inInterface);
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "[NS-IOT]getUsbClient failed!");
            }
            String propertyName = "net.rndis.client";
            ipAddr = SystemProperties.get(propertyName);
            if (enabled && (ipAddr == null || ipAddr.length() == 0)) {
                Log.d(TAG, "[NS-IOT]There is no HostPC address!");
                mToast.setText("There is no HostPC address");
                mToast.show();
                return false;
            } else {
                Log.d(TAG, "[NS-IOT]Disable or There is HostPC prefix: " + ipAddr);
            }
        }

        mToast.setText("enableUdpForwarding(" + enabled + "," + inInterface + ","
            + extInterface + "," + ipAddr);
        mToast.show();
        try {
            Log.d(TAG, "[NS-IOT]enableUdpForwarding(" + enabled + "," + inInterface
                + "," + extInterface + "," + ipAddr);
            mNMService.enableUdpForwarding(enabled, inInterface, extInterface, ipAddr);
            mNMService.setMtu(extInterface, 1500);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "[NS-IOT]enableUdpForwarding failed!");
            mToast.setText("enableUdpForwarding failed!");
            mToast.show();
            return false;
        }
    }

    private Resources getResourcesUsingMccMnc(Context context, int mcc, int mnc) {
        try {
            if (DBG) Log.i(TAG, "getResourcesUsingMccMnc: mcc = " + mcc + ", mnc = " + mnc);
            Configuration configuration = new Configuration();
            configuration.mcc = mcc;
            configuration.mnc = mnc;
            Context resc = context.createConfigurationContext(configuration);
            return resc.getResources();

        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.i(TAG, "getResourcesUsingMccMnc fail, return null");
        return null;
    }
}
