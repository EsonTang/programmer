
package com.android.server.connectivity.tethering;

import android.net.ConnectivityManager;
import android.net.InterfaceConfiguration;
import android.net.LinkAddress;
import android.net.NetworkUtils;
import android.os.INetworkManagementService;
import android.os.SystemProperties;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.net.InetAddress;
import java.util.Arrays;

/**
 * @hide
 * Modem direct tethering is a new function.
 * It enables IP packets are send/receive between laptop & modem directly.
 *
 */

public class MdDirectTethering {
    private static final String TAG = MdDirectTethering.class.getSimpleName();

    private static final String SYSTEM_PROPERTY_MDT_ENABLE =
            "sys.mtk_md_direct_tether_enable";
    private static final String SYSTEM_PROPERTY_MDT_SUPPORT =
            "ro.mtk_md_direct_tethering";
    private static final String SYSTEM_PROPERTY_MDT_MODE_CHANG =
            "sys.usb.rndis.direct";
    private static final String SYSTEM_PROPERTY_MDT_BRIDGE_NAME =
            "ro.tethering.bridge.interface";

    private static final String MDT_IFACE_BR_SUB1 = "rndis0";
    private static final String MDT_IFACE_BR_SUB2 = "ccmni-lan";
    private static final String BR_SUB_IFACE_ADDR      = "0.0.0.0";
    private static final int BR_PREFIX_LENGTH        = 24;

    private boolean mMdtEnable;
    private static final boolean sMdtSupport = SystemProperties.getBoolean(
                    SYSTEM_PROPERTY_MDT_ENABLE, false);
    private String[] mTetherableMdtUsbRegexs;

    private final INetworkManagementService mNMService;

    public MdDirectTethering(INetworkManagementService nmService) {
        mNMService = nmService;

        mMdtEnable = SystemProperties.getBoolean(
                    SYSTEM_PROPERTY_MDT_ENABLE, false);
        Log.d(TAG, "MDT support: " + mMdtEnable);

        mTetherableMdtUsbRegexs = new String[1];
        mTetherableMdtUsbRegexs[0] = SystemProperties.get(
            SYSTEM_PROPERTY_MDT_BRIDGE_NAME, "mdbr0");
        Log.d(TAG, "UsbRegexs:" + Arrays.toString(mTetherableMdtUsbRegexs));
    }

    public boolean isMdtEnable() {
        mMdtEnable = SystemProperties.getBoolean(
                    SYSTEM_PROPERTY_MDT_ENABLE, false);
        return sMdtSupport && mMdtEnable;
    }

    public boolean isMdtEnable(int ifaceType) {
        mMdtEnable = SystemProperties.getBoolean(
                    SYSTEM_PROPERTY_MDT_ENABLE, false);
        return sMdtSupport && mMdtEnable &&
                ifaceType == ConnectivityManager.TETHERING_USB;
    }

    private void enableMdtFunction(boolean enabled) {
        SystemProperties.set(SYSTEM_PROPERTY_MDT_MODE_CHANG,
                     String.valueOf(enabled));
    }

    public String[] getMdtTetherableUsbRegexs() {
        return mTetherableMdtUsbRegexs;
    }

    public boolean clearBridgeMac(String iface) {
        try {
            mNMService.clearBridgeMac(iface);
        } catch (Exception e) {
            Log.e(TAG, "Error clearBridgeMac: " + e);
            return false;
        }
        return true;
    }

    public boolean configureMdtIface(String iface, boolean enabled) {
        Log.d(TAG, "configureMdtIface:" + enabled);

        try {
            InterfaceConfiguration ifcg = new InterfaceConfiguration();
            InetAddress addr = NetworkUtils.numericToInetAddress(BR_SUB_IFACE_ADDR);
            ifcg.setLinkAddress(new LinkAddress(addr, BR_PREFIX_LENGTH));
            if (enabled) {
                ifcg.setInterfaceUp();
                mNMService.addBridgeInterface(iface, MDT_IFACE_BR_SUB1);
                mNMService.addBridgeInterface(iface, MDT_IFACE_BR_SUB2);
            } else {
                ifcg.setInterfaceDown();
                // Bridge port interface will be cleared while deleteBridge(),
                // so ignore deleteBridgeInterface
            }
            mNMService.setInterfaceConfig(MDT_IFACE_BR_SUB2, ifcg);
            mNMService.setInterfaceConfig(MDT_IFACE_BR_SUB1, ifcg);
        } catch (Exception e) {
            Log.e(TAG, "Error configureMdtIface: " + e);
            return false;
        }
        return true;
    }

    public void addBridgeInterface(String iface, boolean isAdded) {
        Log.d(TAG, "addBridgeInterface:" + iface + ":" + isAdded);
        try {
            enableMdtFunction(isAdded);
            if (isAdded) {
                mNMService.addBridgeInterface(iface, MDT_IFACE_BR_SUB2);
                InterfaceConfiguration ifcg = new InterfaceConfiguration();
                InetAddress addr = NetworkUtils.numericToInetAddress(BR_SUB_IFACE_ADDR);
                ifcg.setLinkAddress(new LinkAddress(addr, BR_PREFIX_LENGTH));
                ifcg.setInterfaceUp();
                mNMService.setInterfaceConfig(MDT_IFACE_BR_SUB2, ifcg);
            } else {
                mNMService.deleteBridgeInterface(iface, MDT_IFACE_BR_SUB2);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error addBridgeInterface: " + e);
        }
    }

    //set down sub bridge interface which is not controlled by usb when hardware disconnect
    public boolean resetMdtInterface() {
        Log.d(TAG, "resetMdtInterface");

        enableMdtFunction(false);

        //bring down interface not controlled by usb driver
        InterfaceConfiguration ifcg = new InterfaceConfiguration();
        InetAddress addr = NetworkUtils.numericToInetAddress(BR_SUB_IFACE_ADDR);
        ifcg.setLinkAddress(new LinkAddress(addr, BR_PREFIX_LENGTH));
        ifcg.setInterfaceDown();
        try {
            mNMService.setInterfaceConfig(MDT_IFACE_BR_SUB2, ifcg);
        } catch (Exception e) {
            Log.e(TAG, "Error resetMdtInterface: " + e);
            return false;
        }
        return true;
    }

    public boolean isMobileUpstream(int upType) {
        if (ConnectivityManager.TYPE_MOBILE == upType ||
            ConnectivityManager.TYPE_MOBILE_DUN == upType ||
            ConnectivityManager.TYPE_MOBILE_HIPRI == upType
            ) {
            Log.d(TAG, "isMobileUpstream: true");
            return true;
        } else {
            Log.d(TAG, "isMobileUpstream: false");
            return false;
        }
    }

    public boolean shouldUseMdt(int radioNetworkType) {
        if (isGsm(radioNetworkType)) {
                return true;
        }
        return false;
    }

    private boolean isGsm(int radioNetworkType) {
        Log.d(TAG, "isGsm :" + radioNetworkType);
        return radioNetworkType == TelephonyManager.NETWORK_TYPE_GPRS
                || radioNetworkType == TelephonyManager.NETWORK_TYPE_EDGE
                || radioNetworkType == TelephonyManager.NETWORK_TYPE_UMTS
                || radioNetworkType == TelephonyManager.NETWORK_TYPE_HSDPA
                || radioNetworkType == TelephonyManager.NETWORK_TYPE_HSUPA
                || radioNetworkType == TelephonyManager.NETWORK_TYPE_HSPA
                || radioNetworkType == TelephonyManager.NETWORK_TYPE_LTE
                || radioNetworkType == TelephonyManager.NETWORK_TYPE_HSPAP
                || radioNetworkType == TelephonyManager.NETWORK_TYPE_GSM
                || radioNetworkType == TelephonyManager.NETWORK_TYPE_TD_SCDMA;
    }

}

