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

package android.net.dhcp;

import com.android.internal.util.HexDump;
import com.android.internal.util.Protocol;
import com.android.internal.util.State;
import com.android.internal.util.MessageUtils;
import com.android.internal.util.StateMachine;
import com.android.internal.util.WakeupMessage;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.arp.ArpPeer;
import android.net.DhcpResults;
import android.net.InterfaceConfiguration;
import android.net.LinkAddress;
import android.net.NetworkUtils;
import android.net.metrics.IpConnectivityLog;
import android.net.metrics.DhcpClientEvent;
import android.net.metrics.DhcpErrorEvent;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConfiguration.KeyMgmt;
import android.os.IBinder;
import android.os.INetworkManagementService;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.system.ErrnoException;
import android.system.Os;
import android.system.PacketSocketAddress;
import android.util.EventLog;
import android.util.Log;
import android.util.SparseArray;
import android.util.TimeUtils;

import java.io.BufferedReader;  /// M: cache lease implementation
import java.io.BufferedWriter;  /// M: cache lease implementation
import java.io.File;  /// M: cache lease implementation
import java.io.FileDescriptor;
import java.io.FileReader;  /// M: cache lease implementation
import java.io.FileWriter;  /// M: cache lease implementation
import java.io.IOException;
import java.lang.Thread;
import java.net.InetAddress;
import java.net.Inet4Address;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Random;

import libcore.io.IoBridge;

import static android.system.OsConstants.*;
import static android.net.dhcp.DhcpPacket.*;

/**
 * A DHCPv4 client.
 *
 * Written to behave similarly to the DhcpStateMachine + dhcpcd 5.5.6 combination used in Android
 * 5.1 and below, as configured on Nexus 6. The interface is the same as DhcpStateMachine.
 *
 * TODO:
 *
 * - Exponential backoff when receiving NAKs (not specified by the RFC, but current behaviour).
 * - Support persisting lease state and support INIT-REBOOT. Android 5.1 does this, but it does not
 *   do so correctly: instead of requesting the lease last obtained on a particular network (e.g., a
 *   given SSID), it requests the last-leased IP address on the same interface, causing a delay if
 *   the server NAKs or a timeout if it doesn't.
 *
 * Known differences from current behaviour:
 *
 * - Does not request the "static routes" option.
 * - Does not support BOOTP servers. DHCP has been around since 1993, should be everywhere now.
 * - Requests the "broadcast" option, but does nothing with it.
 * - Rejects invalid subnet masks such as 255.255.255.1 (current code treats that as 255.255.255.0).
 *
 * @hide
 */
public class DhcpClient extends StateMachine {

    private static final String TAG = "DhcpClient";
    private static final boolean DBG = true;
    private static final boolean STATE_DBG = true;
    private static final boolean MSG_DBG = true;
    private static final boolean PACKET_DBG = true;

    // Timers and timeouts.
    private static final int SECONDS = 1000;
    ///M: ALPS02979853: shorten DHCP discovery 1st retry  @{
    //private static final int FIRST_TIMEOUT_MS   =   2 * SECONDS;
    private static final int FIRST_TIMEOUT_MS   =   500;
    /// @}
    private static final int MAX_TIMEOUT_MS     = 128 * SECONDS;

    // This is not strictly needed, since the client is asynchronous and implements exponential
    // backoff. It's maintained for backwards compatibility with the previous DHCP code, which was
    // a blocking operation with a 30-second timeout. We pick 36 seconds so we can send packets at
    // t=0, t=2, t=6, t=14, t=30, allowing for 10% jitter.
    private static final int DHCP_TIMEOUT_MS    =  36 * SECONDS;

    private static final int PUBLIC_BASE = Protocol.BASE_DHCP;

    /* Commands from controller to start/stop DHCP */
    public static final int CMD_START_DHCP                  = PUBLIC_BASE + 1;
    public static final int CMD_STOP_DHCP                   = PUBLIC_BASE + 2;

    /* Notification from DHCP state machine prior to DHCP discovery/renewal */
    public static final int CMD_PRE_DHCP_ACTION             = PUBLIC_BASE + 3;
    /* Notification from DHCP state machine post DHCP discovery/renewal. Indicates
     * success/failure */
    public static final int CMD_POST_DHCP_ACTION            = PUBLIC_BASE + 4;
    /* Notification from DHCP state machine before quitting */
    public static final int CMD_ON_QUIT                     = PUBLIC_BASE + 5;

    /* Command from controller to indicate DHCP discovery/renewal can continue
     * after pre DHCP action is complete */
    public static final int CMD_PRE_DHCP_ACTION_COMPLETE    = PUBLIC_BASE + 6;

    /* Command and event notification to/from IpManager requesting the setting
     * (or clearing) of an IPv4 LinkAddress.
     */
    public static final int CMD_CLEAR_LINKADDRESS           = PUBLIC_BASE + 7;
    public static final int CMD_CONFIGURE_LINKADDRESS       = PUBLIC_BASE + 8;
    public static final int EVENT_LINKADDRESS_CONFIGURED    = PUBLIC_BASE + 9;

    /* Message.arg1 arguments to CMD_POST_DHCP_ACTION notification */
    public static final int DHCP_SUCCESS = 1;
    public static final int DHCP_FAILURE = 2;

    // Internal messages.
    private static final int PRIVATE_BASE         = Protocol.BASE_DHCP + 100;
    private static final int CMD_KICK             = PRIVATE_BASE + 1;
    private static final int CMD_RECEIVED_PACKET  = PRIVATE_BASE + 2;
    private static final int CMD_TIMEOUT          = PRIVATE_BASE + 3;
    private static final int CMD_RENEW_DHCP       = PRIVATE_BASE + 4;
    private static final int CMD_REBIND_DHCP      = PRIVATE_BASE + 5;
    private static final int CMD_EXPIRE_DHCP      = PRIVATE_BASE + 6;
    /// M: ALPS03448409: ip recover detect self-IP and gateway-IP by arp  @{
    private static final int CMD_IP_RECOVER       = PRIVATE_BASE + 31;
    /// @}

    // For message logging.
    private static final Class[] sMessageClasses = { DhcpClient.class };
    private static final SparseArray<String> sMessageNames =
            MessageUtils.findMessageNames(sMessageClasses);

    // DHCP parameters that we request.
    /* package */ static final byte[] REQUESTED_PARAMS = new byte[] {
        DHCP_SUBNET_MASK,
        //M: Add static route due to IOT issue
        DHCP_STATIC_ROUTE,
        DHCP_ROUTER,
        DHCP_DNS_SERVER,
        DHCP_DOMAIN_NAME,
        //M: disable MTU due to IOT issue
        //DHCP_MTU,
        DHCP_BROADCAST_ADDRESS,  // TODO: currently ignored.
        DHCP_LEASE_TIME,
        DHCP_RENEWAL_TIME,
        DHCP_REBINDING_TIME,
        DHCP_VENDOR_INFO,
    };

    // DHCP flag that means "yes, we support unicast."
    private static final boolean DO_UNICAST   = false;

    // System services / libraries we use.
    private final Context mContext;
    private final Random mRandom;
    private final IpConnectivityLog mMetricsLog = new IpConnectivityLog();

    // Sockets.
    // - We use a packet socket to receive, because servers send us packets bound for IP addresses
    //   which we have not yet configured, and the kernel protocol stack drops these.
    // - We use a UDP socket to send, so the kernel handles ARP and routing for us (DHCP servers can
    //   be off-link as well as on-link).
    private FileDescriptor mPacketSock;
    private FileDescriptor mUdpSock;
    private ReceiveThread mReceiveThread;

    // State variables.
    private final StateMachine mController;
    private final WakeupMessage mKickAlarm;
    private final WakeupMessage mTimeoutAlarm;
    private final WakeupMessage mRenewAlarm;
    private final WakeupMessage mRebindAlarm;
    private final WakeupMessage mExpiryAlarm;
    private final String mIfaceName;

    private boolean mRegisteredForPreDhcpNotification;
    private NetworkInterface mIface;
    private byte[] mHwAddr;
    private PacketSocketAddress mInterfaceBroadcastAddr;
    private int mTransactionId;
    private long mTransactionStartMillis;
    private DhcpResults mDhcpLease;
    private long mDhcpLeaseExpiry;
    private DhcpResults mOffer;
    private boolean mIsAutoIpEnabled = false;
    ///M: for IP recover @{
    private DhcpResults mPastDhcpLease;
    private boolean mIsIpRecoverEnabled = true;
    // M: ALPS03448409: detect self-IP and gateway-IP by arp
    private int mDADResult = 0;
        // 1: detected, ip conflict
        // 2: not detected, ip reuse
    private int mGWDResult = 0;
        // 1: detected, gateway exist
        // 2: not detected, gateway gone
    /// @}
    /// M: cache lease implementation  @{
    private static final String DHCP_LEASE_FILE = "/data/misc/dhcp/dhcp_lease.conf";
    /// @}

    // Milliseconds SystemClock timestamps used to record transition times to DhcpBoundState.
    private long mLastInitEnterTime;
    private long mLastBoundExitTime;

    // States.
    private State mStoppedState = new StoppedState();
    private State mDhcpState = new DhcpState();
    private State mDhcpInitState = new DhcpInitState();
    private State mDhcpSelectingState = new DhcpSelectingState();
    private State mDhcpRequestingState = new DhcpRequestingState();
    private State mDhcpHaveLeaseState = new DhcpHaveLeaseState();
    private State mConfiguringInterfaceState = new ConfiguringInterfaceState();
    private State mDhcpBoundState = new DhcpBoundState();
    private State mDhcpRenewingState = new DhcpRenewingState();
    private State mDhcpRebindingState = new DhcpRebindingState();
    private State mDhcpInitRebootState = new DhcpInitRebootState();
    private State mDhcpRebootingState = new DhcpRebootingState();
    private State mWaitBeforeStartState = new WaitBeforeStartState(mDhcpInitState);
    private State mWaitBeforeRenewalState = new WaitBeforeRenewalState(mDhcpRenewingState);

    private WakeupMessage makeWakeupMessage(String cmdName, int cmd) {
        cmdName = DhcpClient.class.getSimpleName() + "." + mIfaceName + "." + cmdName;
        return new WakeupMessage(mContext, getHandler(), cmdName, cmd);
    }

    private DhcpClient(Context context, StateMachine controller, String iface) {
        super(TAG);

        mContext = context;
        mController = controller;
        mIfaceName = iface;

        addState(mStoppedState);
        addState(mDhcpState);
            addState(mDhcpInitState, mDhcpState);
            addState(mWaitBeforeStartState, mDhcpState);
            addState(mDhcpSelectingState, mDhcpState);
            addState(mDhcpRequestingState, mDhcpState);
            addState(mDhcpHaveLeaseState, mDhcpState);
                addState(mConfiguringInterfaceState, mDhcpHaveLeaseState);
                addState(mDhcpBoundState, mDhcpHaveLeaseState);
                addState(mWaitBeforeRenewalState, mDhcpHaveLeaseState);
                addState(mDhcpRenewingState, mDhcpHaveLeaseState);
                addState(mDhcpRebindingState, mDhcpHaveLeaseState);
            addState(mDhcpInitRebootState, mDhcpState);
            addState(mDhcpRebootingState, mDhcpState);

        setInitialState(mStoppedState);

        mRandom = new Random();

        // Used to schedule packet retransmissions.
        mKickAlarm = makeWakeupMessage("KICK", CMD_KICK);
        // Used to time out PacketRetransmittingStates.
        mTimeoutAlarm = makeWakeupMessage("TIMEOUT", CMD_TIMEOUT);
        // Used to schedule DHCP reacquisition.
        mRenewAlarm = makeWakeupMessage("RENEW", CMD_RENEW_DHCP);
        mRebindAlarm = makeWakeupMessage("REBIND", CMD_REBIND_DHCP);
        mExpiryAlarm = makeWakeupMessage("EXPIRY", CMD_EXPIRE_DHCP);
    }

    public void registerForPreDhcpNotification() {
        mRegisteredForPreDhcpNotification = true;
    }

    public static DhcpClient makeDhcpClient(
            Context context, StateMachine controller, String intf) {
        DhcpClient client = new DhcpClient(context, controller, intf);
        client.start();
        return client;
    }

    private boolean initInterface() {
        try {
            mIface = NetworkInterface.getByName(mIfaceName);
            mHwAddr = mIface.getHardwareAddress();
            mInterfaceBroadcastAddr = new PacketSocketAddress(mIface.getIndex(),
                    DhcpPacket.ETHER_BROADCAST);
            //M: set proto to 0x0800 for readable in Wireshark
            mInterfaceBroadcastAddr.sll_protocol = 0x0800;
            return true;
        } catch(SocketException | NullPointerException e) {
            Log.e(TAG, "Can't determine ifindex or MAC address for " + mIfaceName, e);
            Log.e(TAG, "mIface = " + mIface);
            return false;
        }
    }

    private void startNewTransaction() {
        mTransactionId = mRandom.nextInt();
        mTransactionStartMillis = SystemClock.elapsedRealtime();
    }

    private boolean initSockets() {
        return initPacketSocket() && initUdpSocket();
    }

    private boolean initPacketSocket() {
        try {
            mPacketSock = Os.socket(AF_PACKET, SOCK_RAW, ETH_P_IP);
            PacketSocketAddress addr = new PacketSocketAddress((short) ETH_P_IP, mIface.getIndex());
            Os.bind(mPacketSock, addr);
            NetworkUtils.attachDhcpFilter(mPacketSock);
        } catch(SocketException|ErrnoException e) {
            Log.e(TAG, "Error creating packet socket", e);
            return false;
        }
        return true;
    }

    private boolean initUdpSocket() {
        try {
            mUdpSock = Os.socket(AF_INET, SOCK_DGRAM, IPPROTO_UDP);
            Os.setsockoptInt(mUdpSock, SOL_SOCKET, SO_REUSEADDR, 1);
            Os.setsockoptIfreq(mUdpSock, SOL_SOCKET, SO_BINDTODEVICE, mIfaceName);
            Os.setsockoptInt(mUdpSock, SOL_SOCKET, SO_BROADCAST, 1);
            Os.setsockoptInt(mUdpSock, SOL_SOCKET, SO_RCVBUF, 0);
            Os.bind(mUdpSock, Inet4Address.ANY, DhcpPacket.DHCP_CLIENT);
            NetworkUtils.protectFromVpn(mUdpSock);
        } catch(SocketException|ErrnoException e) {
            Log.e(TAG, "Error creating UDP socket", e);
            return false;
        }
        return true;
    }

    private boolean connectUdpSock(Inet4Address to) {
        try {
            Os.connect(mUdpSock, to, DhcpPacket.DHCP_SERVER);
            return true;
        } catch (SocketException|ErrnoException e) {
            Log.e(TAG, "Error connecting UDP socket", e);
            return false;
        }
    }

    private static void closeQuietly(FileDescriptor fd) {
        try {
            IoBridge.closeAndSignalBlockedThreads(fd);
        } catch (IOException ignored) {}
    }

    private void closeSockets() {
        closeQuietly(mUdpSock);
        closeQuietly(mPacketSock);
    }

    class ReceiveThread extends Thread {

        private final byte[] mPacket = new byte[DhcpPacket.MAX_LENGTH];
        private volatile boolean mStopped = false;

        public void halt() {
            mStopped = true;
            closeSockets();  // Interrupts the read() call the thread is blocked in.
        }

        @Override
        public void run() {
            if (DBG) Log.d(TAG, "Receive thread started");
            while (!mStopped) {
                int length = 0;  // Or compiler can't tell it's initialized if a parse error occurs.
                try {
                    length = Os.read(mPacketSock, mPacket, 0, mPacket.length);
                    DhcpPacket packet = null;
                    packet = DhcpPacket.decodeFullPacket(mPacket, length, DhcpPacket.ENCAP_L2);
                    if (DBG) Log.d(TAG, "Received packet: " + packet);
                    sendMessage(CMD_RECEIVED_PACKET, packet);
                } catch (IOException|ErrnoException e) {
                    if (!mStopped) {
                        Log.e(TAG, "Read error", e);
                        logError(DhcpErrorEvent.RECEIVE_ERROR);
                    }
                } catch (DhcpPacket.ParseException e) {
                    Log.e(TAG, "Can't parse packet: " + e.getMessage());
                    if (PACKET_DBG) {
                        Log.d(TAG, HexDump.dumpHexString(mPacket, 0, length));
                    }
                    if (e.errorCode == DhcpErrorEvent.DHCP_NO_COOKIE) {
                        int snetTagId = 0x534e4554;
                        String bugId = "31850211";
                        int uid = -1;
                        String data = DhcpPacket.ParseException.class.getName();
                        EventLog.writeEvent(snetTagId, bugId, uid, data);
                    }
                    logError(e.errorCode);
                }
            }
            if (DBG) Log.d(TAG, "Receive thread stopped");
        }
    }

    private short getSecs() {
        return (short) ((SystemClock.elapsedRealtime() - mTransactionStartMillis) / 1000);
    }

    private boolean transmitPacket(ByteBuffer buf, String description, int encap, Inet4Address to) {
        try {
            if (encap == DhcpPacket.ENCAP_L2) {
                if (DBG) Log.d(TAG, "Broadcasting " + description);
                Os.sendto(mPacketSock, buf.array(), 0, buf.limit(), 0, mInterfaceBroadcastAddr);
            } else if (encap == DhcpPacket.ENCAP_BOOTP && to.equals(INADDR_BROADCAST)) {
                if (DBG) Log.d(TAG, "Broadcasting " + description);
                // We only send L3-encapped broadcasts in DhcpRebindingState,
                // where we have an IP address and an unconnected UDP socket.
                //
                // N.B.: We only need this codepath because DhcpRequestPacket
                // hardcodes the source IP address to 0.0.0.0. We could reuse
                // the packet socket if this ever changes.
                Os.sendto(mUdpSock, buf, 0, to, DhcpPacket.DHCP_SERVER);
            } else {
                // It's safe to call getpeername here, because we only send unicast packets if we
                // have an IP address, and we connect the UDP socket in DhcpBoundState#enter.
                if (DBG) Log.d(TAG, String.format("Unicasting %s to %s",
                        description, Os.getpeername(mUdpSock)));
                Os.write(mUdpSock, buf);
            }
        } catch(ErrnoException|IOException e) {
            Log.e(TAG, "Can't send packet: ", e);
            return false;
        }
        return true;
    }

    private boolean sendDiscoverPacket() {
        ByteBuffer packet = DhcpPacket.buildDiscoverPacket(
                DhcpPacket.ENCAP_L2, mTransactionId, getSecs(), mHwAddr,
                DO_UNICAST, REQUESTED_PARAMS);
        return transmitPacket(packet, "DHCPDISCOVER", DhcpPacket.ENCAP_L2, INADDR_BROADCAST);
    }

    private boolean sendRequestPacket(
            Inet4Address clientAddress, Inet4Address requestedAddress,
            Inet4Address serverAddress, Inet4Address to) {
        // TODO: should we use the transaction ID from the server?
        final int encap = INADDR_ANY.equals(clientAddress)
                ? DhcpPacket.ENCAP_L2 : DhcpPacket.ENCAP_BOOTP;

        ByteBuffer packet = DhcpPacket.buildRequestPacket(
                encap, mTransactionId, getSecs(), clientAddress,
                DO_UNICAST, mHwAddr, requestedAddress,
                serverAddress, REQUESTED_PARAMS, null);
        String serverStr = (serverAddress != null) ? serverAddress.getHostAddress() : null;
        String description = "DHCPREQUEST ciaddr=" + clientAddress.getHostAddress() +
                             " request=" + requestedAddress.getHostAddress() +
                             " serverid=" + serverStr;
        return transmitPacket(packet, description, encap, to);
    }

    private void scheduleLeaseTimers() {
        if (mDhcpLeaseExpiry == 0) {
            Log.d(TAG, "Infinite lease, no timer scheduling needed");
            return;
        }

        final long now = SystemClock.elapsedRealtime();

        // TODO: consider getting the renew and rebind timers from T1 and T2.
        // See also:
        //     https://tools.ietf.org/html/rfc2131#section-4.4.5
        //     https://tools.ietf.org/html/rfc1533#section-9.9
        //     https://tools.ietf.org/html/rfc1533#section-9.10
        final long remainingDelay = mDhcpLeaseExpiry - now;
        final long renewDelay = remainingDelay / 2;
        final long rebindDelay = remainingDelay * 7 / 8;
        mRenewAlarm.schedule(now + renewDelay);
        mRebindAlarm.schedule(now + rebindDelay);
        mExpiryAlarm.schedule(now + remainingDelay);
        Log.d(TAG, "Scheduling renewal in " + (renewDelay / 1000) + "s");
        Log.d(TAG, "Scheduling rebind in " + (rebindDelay / 1000) + "s");
        Log.d(TAG, "Scheduling expiry in " + (remainingDelay / 1000) + "s");
    }

    private void notifySuccess() {
        mController.sendMessage(
                CMD_POST_DHCP_ACTION, DHCP_SUCCESS, 0, new DhcpResults(mDhcpLease));
    }

    private void notifyFailure() {
        mController.sendMessage(CMD_POST_DHCP_ACTION, DHCP_FAILURE, 0, null);
    }

    private void acceptDhcpResults(DhcpResults results, String msg) {
        ///M: for IP recover @{
        results.setSystemExpiredTime(mDhcpLeaseExpiry);
        /// @}
        mDhcpLease = results;
        mOffer = null;
        Log.d(TAG, msg + " lease: " + mDhcpLease);
        notifySuccess();
    }

    private void clearDhcpState() {
        mDhcpLease = null;
        mDhcpLeaseExpiry = 0;
        mOffer = null;
    }

    /**
     * Quit the DhcpStateMachine.
     *
     * @hide
     */
    public void doQuit() {
        Log.d(TAG, "doQuit");
        quit();
    }

    @Override
    protected void onQuitting() {
        Log.d(TAG, "onQuitting");
        mController.sendMessage(CMD_ON_QUIT);
    }

    abstract class LoggingState extends State {
        private long mEnterTimeMs;

        @Override
        public void enter() {
            if (STATE_DBG) Log.d(TAG, "Entering state " + getName());
            mEnterTimeMs = SystemClock.elapsedRealtime();
        }

        @Override
        public void exit() {
            long durationMs = SystemClock.elapsedRealtime() - mEnterTimeMs;
            logState(getName(), (int) durationMs);
        }

        private String messageName(int what) {
            return sMessageNames.get(what, Integer.toString(what));
        }

        private String messageToString(Message message) {
            long now = SystemClock.uptimeMillis();
            StringBuilder b = new StringBuilder(" ");
            TimeUtils.formatDuration(message.getWhen() - now, b);
            b.append(" ").append(messageName(message.what))
                    .append(" ").append(message.arg1)
                    .append(" ").append(message.arg2)
                    .append(" ").append(message.obj);
            return b.toString();
        }

        @Override
        public boolean processMessage(Message message) {
            if (MSG_DBG) {
                Log.d(TAG, getName() + messageToString(message));
            }
            return NOT_HANDLED;
        }

        @Override
        public String getName() {
            // All DhcpClient's states are inner classes with a well defined name.
            // Use getSimpleName() and avoid super's getName() creating new String instances.
            return getClass().getSimpleName();
        }
    }

    // Sends CMD_PRE_DHCP_ACTION to the controller, waits for the controller to respond with
    // CMD_PRE_DHCP_ACTION_COMPLETE, and then transitions to mOtherState.
    abstract class WaitBeforeOtherState extends LoggingState {
        protected State mOtherState;

        @Override
        public void enter() {
            super.enter();
            mController.sendMessage(CMD_PRE_DHCP_ACTION);
        }

        @Override
        public boolean processMessage(Message message) {
            super.processMessage(message);
            switch (message.what) {
                case CMD_PRE_DHCP_ACTION_COMPLETE:
                    /// M: ALPS03051043: no ip conver if AP authentication is WEP OPEN  @{
                    mIsIpRecoverEnabled = true;
                    if (message.obj != null) {
                        WifiConfiguration wifiConfig = (WifiConfiguration) message.obj;

                        // is current wifi ap used WEP?
                        if (wifiConfig.allowedKeyManagement.get(KeyMgmt.NONE) &&
                                wifiConfig.wepTxKeyIndex >= 0 &&
                                wifiConfig.wepTxKeyIndex < wifiConfig.wepKeys.length &&
                                wifiConfig.wepKeys[wifiConfig.wepTxKeyIndex] != null) {
                            mIsIpRecoverEnabled = false;
                        }
                    }
                    Log.d(TAG, "IP recover: mIsIpRecoverEnabled@CMD_PRE_DHCP_ACTION_COMPLETE = "
                        + mIsIpRecoverEnabled);
                    ///@}
                    transitionTo(mOtherState);
                    return HANDLED;
                default:
                    return NOT_HANDLED;
            }
        }
    }

    class StoppedState extends State {
        /// M: cache lease implementation  @{
        String reqIP = null;
        String reqGW = null;
        String reqDNS = null;
        String reqDomain = null;
        String srvIP = null;
        /// @}

        @Override
        public boolean processMessage(Message message) {
            switch (message.what) {
                case CMD_START_DHCP:
                    /// M: for IP recover and cache lease  @{
                    mPastDhcpLease = (DhcpResults)message.obj;
                    mIsIpRecoverEnabled = true;
                    Log.d(TAG, "IP recover: past lease from caller:\n\t" + mPastDhcpLease +
                        ", DhcpClient Protocol.BASE_DHCP = " + Protocol.BASE_DHCP);
                    checkPastLease();
                    Log.d(TAG, "IP recover: past lease after check:\n\t" + mPastDhcpLease);
                    /// @}
                    if (mRegisteredForPreDhcpNotification) {
                        transitionTo(mWaitBeforeStartState);
                    } else {
                        transitionTo(mDhcpInitState);
                    }
                    return HANDLED;
                default:
                    return NOT_HANDLED;
            }
        }

        /// M: cache lease implementation  @{
        private void checkPastLease() {
            if (mPastDhcpLease == null) {
                getLeaseFromFile();

                if (reqIP == null ||
                        reqGW == null ||
                        reqDNS == null ||
                        srvIP == null) {
                    Log.e(TAG, "checkPastLease(): past dhcp lease was not valid" +
                        ", request IP = " + reqIP +
                        ", request Gateway = " + reqGW +
                        ", request DNS = " + reqDNS +
                        ", server IP = " + srvIP);

                } else {
                    mPastDhcpLease = new DhcpResults();
                    try {
                        int prefixLength = NetworkUtils.getImplicitNetmask(
                            (Inet4Address) InetAddress.getByName(reqIP));
                        mPastDhcpLease.ipAddress = new LinkAddress(
                            (Inet4Address) InetAddress.getByName(reqIP),
                            prefixLength);
                        mPastDhcpLease.gateway = InetAddress.getByName(reqGW);
                        mPastDhcpLease.dnsServers.add(
                            (Inet4Address) InetAddress.getByName(reqDNS));
                        mPastDhcpLease.domains = reqDomain;
                    } catch (UnknownHostException e) {
                        mPastDhcpLease = null;
                        Log.e(TAG, "checkPastLease():" +
                            " past dhcp lease some IP was not valid, " + e);

                    }
                }
            } //if (mPastDhcpLease == null)
        }

        private void getLeaseFromFile() {
            File file = new File(DHCP_LEASE_FILE);
            if (!file.exists()) {
                Log.e(TAG, "getLeaseFromFile(): file not existed");
            } else {
                BufferedReader reader = null;
                String[] nameValue = null;
                try {
                    reader = new BufferedReader(new FileReader(DHCP_LEASE_FILE));
                    for (String line = reader.readLine();
                            line != null; line = reader.readLine()) {
                        if (line.startsWith("IP")) {
                            nameValue = line.split("=");
                            reqIP = (nameValue.length != 2) ? null : nameValue[1];
                        } else if (line.startsWith("Gateway")) {
                            nameValue = line.split("=");
                            reqGW = (nameValue.length != 2) ? null : nameValue[1];
                        } else if (line.startsWith("DNS")) {
                            nameValue = line.split("=");
                            reqDNS = (nameValue.length != 2) ? null : nameValue[1];
                        } else if (line.startsWith("Domain")) {
                            nameValue = line.split("=");
                            reqDomain = (nameValue.length != 2) ? null : nameValue[1];
                        } else if (line.startsWith("Server")) {
                            nameValue = line.split("=");
                            srvIP = (nameValue.length != 2) ? null : nameValue[1];
                        }
                    } //for()
                } catch (IOException e) {
                    Log.e(TAG, "getLeaseFromFile()-01: " + e);
                } finally {
                    try {
                        if (reader != null) {
                            reader.close();
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "getLeaseFromFile()-02: " + e);
                    }
                }

                // Log.e(TAG, "getLeaseFromFile() req ip = " + reqIP);
                // Log.e(TAG, "getLeaseFromFile() req gateway = " + reqGW);
                // Log.e(TAG, "getLeaseFromFile() req DNS = " + reqDNS);
                // Log.e(TAG, "getLeaseFromFile() req domain = " + reqDomain);
                // Log.e(TAG, "getLeaseFromFile() srv ip = " + srvIP);
            }
        }
        /// @}
    }

    class WaitBeforeStartState extends WaitBeforeOtherState {
        public WaitBeforeStartState(State otherState) {
            super();
            mOtherState = otherState;
        }
    }

    class WaitBeforeRenewalState extends WaitBeforeOtherState {
        public WaitBeforeRenewalState(State otherState) {
            super();
            mOtherState = otherState;
        }
    }

    class DhcpState extends State {
        @Override
        public void enter() {
            clearDhcpState();
            if (initInterface() && initSockets()) {
                mReceiveThread = new ReceiveThread();
                mReceiveThread.start();
            } else {
                notifyFailure();
                transitionTo(mStoppedState);
            }
        }

        @Override
        public void exit() {
            if (mReceiveThread != null) {
                mReceiveThread.halt();  // Also closes sockets.
                mReceiveThread = null;
            }
            clearDhcpState();
        }

        @Override
        public boolean processMessage(Message message) {
            super.processMessage(message);
            switch (message.what) {
                case CMD_STOP_DHCP:
                    transitionTo(mStoppedState);
                    return HANDLED;
                default:
                    return NOT_HANDLED;
            }
        }
    }

    public boolean isValidPacket(DhcpPacket packet) {
        // TODO: check checksum.
        int xid = packet.getTransactionId();
        if (xid != mTransactionId) {
            Log.d(TAG, "Unexpected transaction ID " + xid + ", expected " + mTransactionId);
            return false;
        }
        if (!Arrays.equals(packet.getClientMac(), mHwAddr)) {
            Log.d(TAG, "MAC addr mismatch: got " +
                    HexDump.toHexString(packet.getClientMac()) + ", expected " +
                    HexDump.toHexString(mHwAddr));
            return false;
        }
        return true;
    }

    public void setDhcpLeaseExpiry(DhcpPacket packet) {
        long leaseTimeMillis = packet.getLeaseTimeMillis();
        mDhcpLeaseExpiry =
                (leaseTimeMillis > 0) ? SystemClock.elapsedRealtime() + leaseTimeMillis : 0;
    }

    /**
     * Retransmits packets using jittered exponential backoff with an optional timeout. Packet
     * transmission is triggered by CMD_KICK, which is sent by an AlarmManager alarm. If a subclass
     * sets mTimeout to a positive value, then timeout() is called by an AlarmManager alarm mTimeout
     * milliseconds after entering the state. Kicks and timeouts are cancelled when leaving the
     * state.
     *
     * Concrete subclasses must implement sendPacket, which is called when the alarm fires and a
     * packet needs to be transmitted, and receivePacket, which is triggered by CMD_RECEIVED_PACKET
     * sent by the receive thread. They may also set mTimeout and implement timeout.
     */
    abstract class PacketRetransmittingState extends LoggingState {

        ///M:
        //private int mTimer;
        protected int mTimer;
        /// @}
        protected int mTimeout = 0;

        @Override
        public void enter() {
            super.enter();
            initTimer();
            maybeInitTimeout();
            sendMessage(CMD_KICK);
        }

        @Override
        public boolean processMessage(Message message) {
            super.processMessage(message);
            switch (message.what) {
                case CMD_KICK:
                    sendPacket();
                    scheduleKick();
                    return HANDLED;
                case CMD_RECEIVED_PACKET:
                    receivePacket((DhcpPacket) message.obj);
                    return HANDLED;
                case CMD_TIMEOUT:
                    timeout();
                    return HANDLED;
                default:
                    return NOT_HANDLED;
            }
        }

        @Override
        public void exit() {
            super.exit();
            mKickAlarm.cancel();
            mTimeoutAlarm.cancel();
        }

        abstract protected boolean sendPacket();
        abstract protected void receivePacket(DhcpPacket packet);
        protected void timeout() {}

        protected void initTimer() {
            mTimer = FIRST_TIMEOUT_MS;
        }

        protected int jitterTimer(int baseTimer) {
            int maxJitter = baseTimer / 10;
            int jitter = mRandom.nextInt(2 * maxJitter) - maxJitter;
            return baseTimer + jitter;
        }

        protected void scheduleKick() {
            long now = SystemClock.elapsedRealtime();
            long timeout = jitterTimer(mTimer);
            long alarmTime = now + timeout;
            mKickAlarm.schedule(alarmTime);
            mTimer *= 2;
            if (mTimer > MAX_TIMEOUT_MS) {
                mTimer = MAX_TIMEOUT_MS;
            }
        }

        protected void maybeInitTimeout() {
            if (mTimeout > 0) {
                long alarmTime = SystemClock.elapsedRealtime() + mTimeout;
                mTimeoutAlarm.schedule(alarmTime);
            }
        }
    }

    class DhcpInitState extends PacketRetransmittingState {
        public DhcpInitState() {
            super();
            /// M: for IP recover, if no OFFER in 12 sec. @{
            mTimeout = DHCP_TIMEOUT_MS / 3;
            /// @}
        }

        @Override
        public void enter() {
            super.enter();
            startNewTransaction();
            mLastInitEnterTime = SystemClock.elapsedRealtime();
        }

        protected boolean sendPacket() {
            return sendDiscoverPacket();
        }

        protected void receivePacket(DhcpPacket packet) {
            if (!isValidPacket(packet)) return;
            if (!(packet instanceof DhcpOfferPacket)) return;
            mOffer = packet.toDhcpResults();
            if (mOffer != null) {
                Log.d(TAG, "Got pending lease: " + mOffer);
                transitionTo(mDhcpRequestingState);
            }
        }

        ///M: for IP recover @{
        @Override
        protected void timeout() {
            if(doIpRecover()) {
                sendMessageDelayed(CMD_IP_RECOVER, 1700);
            }
        }

        // M: ALPS03448409: detect self-IP and gateway-IP by arp
        @Override
        public boolean processMessage(Message message) {
            super.processMessage(message);
            switch (message.what) {
                case CMD_IP_RECOVER:
                    if (mDADResult == 2 && mGWDResult == 1) {
                        Log.d(TAG, "ip recover: good result!");
                        acceptDhcpResults(mPastDhcpLease, "Confirmed");
                        transitionTo(mConfiguringInterfaceState);
                    } else if (mDADResult == 1 || mGWDResult == 2) {
                        Log.d(TAG, "ip recover: bad result!");
                    } else {
                        Log.d(TAG, "ip recover: no full result yet");
                        sendMessageDelayed(CMD_IP_RECOVER, 1700);
                    }
                    return HANDLED;
                default:
                    return NOT_HANDLED;
            }
        }
        /// @}

        ///M: ALPS02979853: send next discovery packet on time  @{
        protected void scheduleKick() {
            long timeout = jitterTimer(mTimer);
            Log.d(TAG, "scheduleKick()@DhcpInitState timeout=" + timeout);
            sendMessageDelayed(CMD_KICK, timeout);
            mTimer *= 2;
            if (mTimer > MAX_TIMEOUT_MS) {
                mTimer = MAX_TIMEOUT_MS;
            }
        }
        /// @}
    }

    // Not implemented. We request the first offer we receive.
    class DhcpSelectingState extends LoggingState {
    }

    class DhcpRequestingState extends PacketRetransmittingState {
        public DhcpRequestingState() {
            mTimeout = DHCP_TIMEOUT_MS / 2;
        }

        protected boolean sendPacket() {
            return sendRequestPacket(
                    INADDR_ANY,                                    // ciaddr
                    (Inet4Address) mOffer.ipAddress.getAddress(),  // DHCP_REQUESTED_IP
                    (Inet4Address) mOffer.serverAddress,           // DHCP_SERVER_IDENTIFIER
                    INADDR_BROADCAST);                             // packet destination address
        }

        protected void receivePacket(DhcpPacket packet) {
            if (!isValidPacket(packet)) return;
            if ((packet instanceof DhcpAckPacket)) {
                DhcpResults results = packet.toDhcpResults();
                if (results != null) {
                    setDhcpLeaseExpiry(packet);
                    acceptDhcpResults(results, "Confirmed");
                    transitionTo(mConfiguringInterfaceState);
                }
            } else if (packet instanceof DhcpNakPacket) {
                // TODO: Wait a while before returning into INIT state.
                Log.d(TAG, "Received NAK, returning to INIT");
                mOffer = null;
                transitionTo(mDhcpInitState);
            }
        }

        @Override
        protected void timeout() {
            if (mIsAutoIpEnabled && performAutoIP()) {
                mOffer = null;
                notifySuccess();
                transitionTo(mConfiguringInterfaceState);
            } else {
                // After sending REQUESTs unsuccessfully for a while, go back to init.
                transitionTo(mDhcpInitState);
            }
        }
    }

    class DhcpHaveLeaseState extends State {
        @Override
        public void enter() {
            super.enter();
            /// M: cache lease implementation  @{
            putLeaseToFile();
            /// @}
        }

        /// M: cache lease implementation  @{
        private void putLeaseToFile() {
            BufferedWriter writer = null;
            try {
                writer = new BufferedWriter(new FileWriter(DHCP_LEASE_FILE));
                if (mDhcpLease.ipAddress != null &&
                        mDhcpLease.ipAddress.getAddress() != null) {
                    writer.write("IP=" + mDhcpLease.ipAddress.getAddress().getHostAddress() + "\n");
                    // Log.e(TAG, "putLeaseToFile() req ip = " +
                    //     mDhcpLease.ipAddress.getAddress().getHostAddress());
                }
                if (mDhcpLease.gateway != null &&
                        mDhcpLease.gateway.getAddress() != null) {
                    writer.write("Gateway=" + mDhcpLease.gateway.getHostAddress() + "\n");
                    // Log.e(TAG, "putLeaseToFile() req gateway = " +
                    //     mDhcpLease.gateway.getHostAddress());
                }
                if (mDhcpLease.dnsServers != null) {
                    for (InetAddress dns : mDhcpLease.dnsServers) {
                        writer.write("DNS=" + dns.getHostAddress() + "\n");
                        // Log.e(TAG, "putLeaseToFile() req DNS = " +
                        //     dns.getHostAddress());
                        break;
                    }// for()
                }
                if (mDhcpLease.domains != null) {
                    writer.write("Domain=" + mDhcpLease.domains + "\n");
                    // Log.e(TAG, "putLeaseToFile() req domain = " +
                    //     mDhcpLease.domains);
                }
                if (mDhcpLease.serverAddress != null) {
                    writer.write("Server=" + mDhcpLease.serverAddress.getHostAddress() + "\n");
                    // Log.e(TAG, "putLeaseToFile() srv ip = " +
                    //     mDhcpLease.serverAddress.getHostAddress());
                }
            } catch (IOException e) {
                Log.e(TAG, "putLeaseToFile()-01: " + e);
            } finally {
                try {
                    if (writer != null) {
                        writer.close();
                    }
                } catch (IOException e) {
                    Log.e(TAG, "putLeaseToFile()-02: " + e);
                }
            }
        }
        /// @}

        @Override
        public boolean processMessage(Message message) {
            switch (message.what) {
                case CMD_EXPIRE_DHCP:
                    Log.d(TAG, "Lease expired!");
                    notifyFailure();
                    transitionTo(mDhcpInitState);
                    return HANDLED;
                default:
                    return NOT_HANDLED;
            }
        }

        @Override
        public void exit() {
            // Clear any extant alarms.
            mRenewAlarm.cancel();
            mRebindAlarm.cancel();
            mExpiryAlarm.cancel();
            clearDhcpState();
            // Tell IpManager to clear the IPv4 address. There is no need to
            // wait for confirmation since any subsequent packets are sent from
            // INADDR_ANY anyway (DISCOVER, REQUEST).
            mController.sendMessage(CMD_CLEAR_LINKADDRESS);
        }
    }

    class ConfiguringInterfaceState extends LoggingState {
        @Override
        public void enter() {
            super.enter();
            mController.sendMessage(CMD_CONFIGURE_LINKADDRESS, mDhcpLease.ipAddress);
        }

        @Override
        public boolean processMessage(Message message) {
            super.processMessage(message);
            switch (message.what) {
                case EVENT_LINKADDRESS_CONFIGURED:
                    transitionTo(mDhcpBoundState);
                    return HANDLED;
                default:
                    return NOT_HANDLED;
            }
        }
    }

    class DhcpBoundState extends LoggingState {
        @Override
        public void enter() {
            super.enter();
            if (mDhcpLease.serverAddress != null && !connectUdpSock(mDhcpLease.serverAddress)) {
                // There's likely no point in going into DhcpInitState here, we'll probably
                // just repeat the transaction, get the same IP address as before, and fail.
                //
                // NOTE: It is observed that connectUdpSock() basically never fails, due to
                // SO_BINDTODEVICE. Examining the local socket address shows it will happily
                // return an IPv4 address from another interface, or even return "0.0.0.0".
                //
                // TODO: Consider deleting this check, following testing on several kernels.
                notifyFailure();
                transitionTo(mStoppedState);
            }

            scheduleLeaseTimers();
            logTimeToBoundState();
        }

        @Override
        public void exit() {
            super.exit();
            mLastBoundExitTime = SystemClock.elapsedRealtime();
        }

        @Override
        public boolean processMessage(Message message) {
            super.processMessage(message);
            switch (message.what) {
                case CMD_RENEW_DHCP:
                    if (mRegisteredForPreDhcpNotification) {
                        transitionTo(mWaitBeforeRenewalState);
                    } else {
                        transitionTo(mDhcpRenewingState);
                    }
                    return HANDLED;
                default:
                    return NOT_HANDLED;
            }
        }

        private void logTimeToBoundState() {
            long now = SystemClock.elapsedRealtime();
            if (mLastBoundExitTime > mLastInitEnterTime) {
                logState(DhcpClientEvent.RENEWING_BOUND, (int)(now - mLastBoundExitTime));
            } else {
                logState(DhcpClientEvent.INITIAL_BOUND, (int)(now - mLastInitEnterTime));
            }
        }
    }

    abstract class DhcpReacquiringState extends PacketRetransmittingState {
        protected String mLeaseMsg;

        @Override
        public void enter() {
            super.enter();
            startNewTransaction();
        }

        abstract protected Inet4Address packetDestination();

        protected boolean sendPacket() {
            return sendRequestPacket(
                    (Inet4Address) mDhcpLease.ipAddress.getAddress(),  // ciaddr
                    INADDR_ANY,                                        // DHCP_REQUESTED_IP
                    null,                                              // DHCP_SERVER_IDENTIFIER
                    packetDestination());                              // packet destination address
        }

        protected void receivePacket(DhcpPacket packet) {
            if (!isValidPacket(packet)) return;
            if ((packet instanceof DhcpAckPacket)) {
                final DhcpResults results = packet.toDhcpResults();
                if (results != null) {
                    if (!mDhcpLease.ipAddress.equals(results.ipAddress)) {
                        Log.d(TAG, "Renewed lease not for our current IP address!");
                        notifyFailure();
                        transitionTo(mDhcpInitState);
                    }
                    setDhcpLeaseExpiry(packet);
                    // Updating our notion of DhcpResults here only causes the
                    // DNS servers and routes to be updated in LinkProperties
                    // in IpManager and by any overridden relevant handlers of
                    // the registered IpManager.Callback.  IP address changes
                    // are not supported here.
                    acceptDhcpResults(results, mLeaseMsg);
                    transitionTo(mDhcpBoundState);
                }
            } else if (packet instanceof DhcpNakPacket) {
                Log.d(TAG, "Received NAK, returning to INIT");
                notifyFailure();
                transitionTo(mDhcpInitState);
            }
        }
    }

    class DhcpRenewingState extends DhcpReacquiringState {
        public DhcpRenewingState() {
            mLeaseMsg = "Renewed";
        }

        @Override
        public boolean processMessage(Message message) {
            if (super.processMessage(message) == HANDLED) {
                return HANDLED;
            }

            switch (message.what) {
                case CMD_REBIND_DHCP:
                    transitionTo(mDhcpRebindingState);
                    return HANDLED;
                default:
                    return NOT_HANDLED;
            }
        }

        @Override
        protected Inet4Address packetDestination() {
            // Not specifying a SERVER_IDENTIFIER option is a violation of RFC 2131, but...
            // http://b/25343517 . Try to make things work anyway by using broadcast renews.
            return (mDhcpLease.serverAddress != null) ?
                    mDhcpLease.serverAddress : INADDR_BROADCAST;
        }
    }

    class DhcpRebindingState extends DhcpReacquiringState {
        public DhcpRebindingState() {
            mLeaseMsg = "Rebound";
        }

        @Override
        public void enter() {
            super.enter();

            // We need to broadcast and possibly reconnect the socket to a
            // completely different server.
            closeQuietly(mUdpSock);
            if (!initUdpSocket()) {
                Log.e(TAG, "Failed to recreate UDP socket");
                transitionTo(mDhcpInitState);
            }
        }

        @Override
        protected Inet4Address packetDestination() {
            return INADDR_BROADCAST;
        }
    }

    class DhcpInitRebootState extends LoggingState {
    }

    class DhcpRebootingState extends LoggingState {
    }

    private void logError(int errorCode) {
        mMetricsLog.log(new DhcpErrorEvent(mIfaceName, errorCode));
    }

    private void logState(String name, int durationMs) {
        mMetricsLog.log(new DhcpClientEvent(mIfaceName, name, durationMs));
    }

    /** Self-configured IP address.
     ** The IP address range is 169.254.0.1 through 169.254.255.254 and
     ** default class B subnet mask of 255.255.0.0
     ** Only support AutoIp in the scenario:
     ** - new connection with first time DHCP failed
     ** Others are not supported in current stage:
     ** - Had previously IP and Lease Expired and no DHCP Server
     ** - AutoIp directly
     **/
    private boolean performAutoIP() {
        Random random = new Random();
        int INFINITE_LEASE = (int) 0xffffffff;
        byte autoIp[] = new byte[] { (byte)0xa9, (byte)0xfe, 10, 10 };
        byte arpResult[] = null;
        /** The total time to run AutoIp = DHCP_TIMEOUT_MS + arpResponseTimeout
         ** If enable AutoIp need to set WifiStateMachine.OBTAINING_IP_ADDRESS_GUARD_TIMER_MSEC
         ** to larger value, to avoid WifiStateMachine triger dhcp timeout and disconnect event.
         **/
        int arpResponseTimeout = 5000;
        int autoIpMaxRetryCount = 5;
        ArpPeer ap = null;
        boolean retVal = false;

        for (int i = 0; i < autoIpMaxRetryCount; i++) {
            autoIp[2] = new Integer(random.nextInt(256)).byteValue();
            autoIp[3] = new Integer(random.nextInt(254) + 1).byteValue();

            try {
                InetAddress ipAddress = InetAddress.getByAddress(autoIp);
                Log.d(TAG, "performAutoIP(" + i + ") = " +
                    "oooKxxxK" +
                    logDumpIpv4(2, ipAddress.getHostAddress()));
                ap = new ArpPeer(mIfaceName, Inet4Address.ANY, ipAddress);
                // doArp will blocking several seconds, to create thread if needed
                arpResult = ap.doArp(arpResponseTimeout);
                if (arpResult == null) {
                    mDhcpLease = new DhcpResults();
                    mDhcpLease.ipAddress = new LinkAddress(ipAddress, 16);
                    /*mDhcpLease.gateway = mGateway;
                    mDhcpLease.dnsServers.addAll(mDnsServers);
                    mDhcpLease.domains = mDomainName;
                    mDhcpLease.serverAddress = mServerIdentifier;
                    mDhcpLease.vendorInfo = mVendorInfo;*/
                    mDhcpLease.leaseDuration = INFINITE_LEASE;
                    setIpAddress(mDhcpLease.ipAddress);
                    Log.d(TAG, "performAutoIP done");
                    retVal = true;
                } else {
                    Log.d(TAG, "DAD detected!!");
                }
            } catch (UnknownHostException ue) {
                // autoIp is numeric, so this should not be triggered
                Log.d(TAG, "err :" + ue);
            } catch (ErrnoException ee) {
                Log.d(TAG, "err :" + ee);
            } catch (IllegalArgumentException ie) {
                Log.d(TAG, "err :" + ie);
            } catch (SocketException se) {
                Log.d(TAG, "err :" + se);
            } finally {
                if (ap != null)    ap.close();
            }
        }
        return retVal;
    }

    private boolean setIpAddress(LinkAddress address) {
        InterfaceConfiguration ifcg = new InterfaceConfiguration();
        ifcg.setLinkAddress(address);
        try {
            IBinder b = ServiceManager.getService(Context.NETWORKMANAGEMENT_SERVICE);
            INetworkManagementService service = INetworkManagementService.Stub.asInterface(b);
            service.setInterfaceConfig(mIfaceName, ifcg);
        } catch (RemoteException | IllegalStateException e) {
            Log.e(TAG, "Error configuring IP address " + address + ": ", e);
            return false;
        }
        return true;
    }

    ///M: for IP recover @{
    private boolean doIpRecover() {
        if (mIsIpRecoverEnabled == false) {
            Log.d(TAG, "IP recover: it was disabled");
            return false;
        }
        if (mPastDhcpLease == null) {
            Log.d(TAG, "IP recover: mPastDhcpLease is empty");
            return false;
        }
        Log.d(TAG, "IP recover: mPastDhcpLease = " + mPastDhcpLease);

        long reCaculatedLeaseMillis =
            mPastDhcpLease.systemExpiredTime - SystemClock.elapsedRealtime();
        Log.d(TAG, "IP recover: reCaculatedLeaseMillis = " + reCaculatedLeaseMillis);

        if (reCaculatedLeaseMillis < 0) {
            // configure infinite lease
            mDhcpLeaseExpiry = 0;
            Log.e(TAG, "IP recover: lease had been expired! configure to infinite lease");
        } else {
            mDhcpLeaseExpiry = SystemClock.elapsedRealtime() + reCaculatedLeaseMillis;
            Log.d(TAG, "IP recover: mDhcpLeaseExpiry = " + mDhcpLeaseExpiry);
        }

        /// M: ALPS03448409: detect self-IP and gateway-IP by arp  @{
        Thread tDAD = new Thread() {
            public void run() {
                byte arpResult[] = null;
                ArpPeer ap = null;
                try {
                    InetAddress ipAddress = mPastDhcpLease.ipAddress.getAddress();
                    Log.d(TAG, "IP recover: DAD arp address = " +
                        "#$%K" +
                        logDumpIpv4(3, ipAddress.getHostAddress()));
                    mDADResult = 0;
                    ap = new ArpPeer(mIfaceName, Inet4Address.ANY, ipAddress);
                    arpResult = ap.doArp(5000);
                } catch (ErrnoException|IllegalArgumentException|SocketException e) {
                    Log.e(TAG, "IP recover: DAD err :" + e);
                } finally {
                    if (ap != null)    ap.close();
                    if (arpResult == null) {
                        mDADResult = 2;
                    } else {
                        mDADResult = 1;
                    }
                    Log.d(TAG, "IP recover: DAD result = " + mDADResult);
                }
            }//run()
        };
        tDAD.start();

        Thread tGWD = new Thread() {
            public void run() {
                byte arpResult[] = null;
                ArpPeer ap = null;
                try {
                    InetAddress ipAddress = mPastDhcpLease.gateway;
                    Log.d(TAG, "IP recover: GWD arp address = " +
                        "#$%K" +
                        logDumpIpv4(3, ipAddress.getHostAddress()));
                    mGWDResult = 0;
                    ap = new ArpPeer(mIfaceName, Inet4Address.ANY, ipAddress);
                    arpResult = ap.doArp(5000);
                } catch (ErrnoException|IllegalArgumentException|SocketException e) {
                    Log.e(TAG, "IP recover: GWD err :" + e);
                } finally {
                    if (ap != null)    ap.close();
                    if (arpResult == null) {
                        mGWDResult = 2;
                    } else {
                        mGWDResult = 1;
                    }
                    Log.d(TAG, "IP recover: GWD result = " + mGWDResult);
                }
            }//run()
        };
        tGWD.start();
        /// @}

        return true;
    }
    /// @}

    ///M: @{
    private String logDumpIpv4(int postAmount, String ipv4) {
        if (ipv4 == null) return null;
        String[] octets = ipv4.split("\\.");
        if (octets.length != 4) return ipv4;
        StringBuilder builder = new StringBuilder(16);
        String result = null;
        for (int i = (4-postAmount); i < 4; i++) {
            try {
                if (octets[i].length() > 3) return ipv4;
                builder.append(Integer.parseInt(octets[i]));
            } catch (NumberFormatException e) {
                return ipv4;
            }
            if (i < 3) builder.append('K');
        }
        result = builder.toString();
        return result;
    }
    /// @}
}
