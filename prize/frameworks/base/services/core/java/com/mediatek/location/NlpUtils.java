package com.mediatek.location;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.LocalServerSocket;
import android.net.LocalSocket;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.os.HandlerThread;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.util.Log;

import com.android.server.LocationManagerService;
import static com.mediatek.location.DataCoder.getInt;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;


public class NlpUtils {
    private static final boolean DEBUG = LocationManagerService.D;

    // Messages for internal handler
    private final static int NLPS_MSG_GPS_STARTED = 0;
    private final static int NLPS_MSG_GPS_STOPPED = 1;
    private final static int NLPS_MSG_NLP_NIJ_REQ = 2;
    private final static int NLPS_MSG_NLP_NIJ_CANCEL = 3;
    private final static int NLPS_MSG_NLP_UPDATED = 4;
    private final static int NLPS_MSG_CLEAR_LAST_LOC = 5;

    // Commands from Server Instance Socket
    private final static int NLPS_CMD_QUIT = 100;
    private final static int NLPS_CMD_GPS_NIJ_REQ = 101;
    private final static int NLPS_CMD_GPS_NIJ_CANCEL = 102;

    private final static int NLP_CMD_SRC_UNKNOWN = 0;  // for legacy mnld, without request src
    private final static int NLP_CMD_SRC_MNL = (1 << 0);
    private final static int NLP_CMD_SRC_APM = (1 << 1);

    private final static int NLPS_MAX_CLIENTS = 2;
    private final static boolean NIJ_ON_GPS_START_DEFAULT = false;
    private static final int UPDATE_LOCATION = 7;
    private static final int LAST_LOCATION_EXPIRED_TIMEOUT = (10*60*1000); //ms

    protected final static String SOCKET_ADDRESS = "com.mediatek.nlpservice.NlpService";
    private static final String PROP_NLP_ENABLED = "persist.sys.nlp.enabled";
    private LocationManager mLocationManager;
    private NlpsMsgHandler mHandler;
    private Thread mServerThread;
    private volatile boolean mIsStopping = false;
    private AtomicInteger mClientCount = new AtomicInteger();
    private LocalServerSocket mNlpServerSocket = null;
    private Location mLastLocation = null;
    private Context mContext;
    private Handler mGpsHandler;
    private int mNlpRequestSrc = 0;

    public NlpUtils(Context context, Handler gpsHandler) {
        if (DEBUG) log("onCreate");
        mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        mContext = context;
        mGpsHandler = gpsHandler;

        HandlerThread handlerThread = new HandlerThread("[NlpUtils]");
        handlerThread.start();
        mHandler = new NlpsMsgHandler(handlerThread.getLooper());
        mServerThread = new Thread() {
            public void run() {
                if (DEBUG) log("mServerThread.run()");
                doServerTask();
            }
        };
        mServerThread.start();

        IntentFilter intentFilter;
        intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        intentFilter.addAction(LocationManager.PROVIDERS_CHANGED_ACTION);
        mContext.registerReceiver(new BroadcastReceiver() {
            @Override
                public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                    connectivityAction(intent);
                } else if (action.equals(LocationManager.PROVIDERS_CHANGED_ACTION)) {
                    // M: set systemProperty to indicate NLP status for APM feature
                    if (isNlpEnabled()) {
                        SystemProperties.set(PROP_NLP_ENABLED, "1");
                    } else {
                        SystemProperties.set(PROP_NLP_ENABLED, "0");
                    }
                }
            }
        }, intentFilter);

        mLocationManager.requestLocationUpdates(
                LocationManager.PASSIVE_PROVIDER, 0, 0,
                mPassiveLocationListener);
    }

    private void connectivityAction(Intent intent) {
        NetworkInfo info =
                intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
        ConnectivityManager connManager = (ConnectivityManager)
                mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        info = connManager.getNetworkInfo(info.getType());

        int networkState;
        if (intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false) ||
                (info != null && !info.isConnected()) ) {
            log("Connectivity set unConnected");
            clearLastLocation();
        }
    }

    private boolean isNlpEnabled() {
        return mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    private void startNlpQueryLocked(int src) {
        boolean isNlpEnabled = isNlpEnabled();
        log("startNlpQueryLocked isNlpEnabled=" + isNlpEnabled + " src:" + src +
                " mRequestSrc:" + mNlpRequestSrc);
        if ((mNlpRequestSrc & src) != 0) {
            stopNlpQueryLocked(src);
        }

        if (mNlpRequestSrc == 0) {
            mLocationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, 1000, 0, mNetworkLocationListener);
        }
        mNlpRequestSrc |= src;
    }

    private void stopNlpQueryLocked(int src) {
        if (DEBUG) log("stopNlpQueryLocked src:" + src + " mRequestSrc:" + mNlpRequestSrc);
        if ((mNlpRequestSrc & src) != 0) {
            mNlpRequestSrc &= ~(src);
            if ( mNlpRequestSrc == 0) {
                mLocationManager.removeUpdates(mNetworkLocationListener);
            }
        }
    }

    private LocationListener mPassiveLocationListener = new LocationListener() {

        @Override
        public void onLocationChanged(Location location) {
            if (mNlpRequestSrc == 0 ||
                !LocationManager.NETWORK_PROVIDER.equals(location.getProvider())) {
                return;
            }
            synchronized(this) {
                if( mLastLocation == null) {
                    mLastLocation = new Location(location);
                } else {
                    mLastLocation.set(location);
                }
            }
            // reset the timer to clear last location
            mHandler.removeMessages(NLPS_MSG_CLEAR_LAST_LOC);
            sendCommandDelayed(NLPS_MSG_CLEAR_LAST_LOC, LAST_LOCATION_EXPIRED_TIMEOUT);

            sendCommand(NLPS_MSG_NLP_UPDATED, 0);
        }

        @Override
        public void onProviderDisabled(String provider) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

    };

    private LocationListener mNetworkLocationListener = new LocationListener() {

        @Override
        public void onLocationChanged(Location location) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

    };

    public static void log(String msg) {
        Log.d("NlpUtils", msg);
    }

    private static void close(LocalServerSocket lss) {
        try {
            lss.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void close(LocalSocket ls) {
        try {
            ls.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private synchronized void requestNlp(int src) {
        try {
            startNlpQueryLocked(src);
            if (src == NLP_CMD_SRC_MNL) {
                // inject last Location
                if (mLastLocation != null) {
                    if (DEBUG) log("inject NLP location");
                    mGpsHandler.obtainMessage(UPDATE_LOCATION, 0, 0, mLastLocation).sendToTarget();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private synchronized void releaseNlp(int src) {
        try {
            stopNlpQueryLocked(src);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private synchronized void closeServerSocket() {
        if (mNlpServerSocket == null) {
            return;
        }
        close(mNlpServerSocket);
        mNlpServerSocket = null;
    }

    private synchronized void clearLastLocation() {
        if (DEBUG) log("clearLastLocation");
        mLastLocation = null;
    }

    private void doServerTask() {
        try {
            if (DEBUG) log("NlpUtilsSocket+");
            synchronized(this) {
                mNlpServerSocket = new LocalServerSocket(SOCKET_ADDRESS);
                if (DEBUG) log("NlpServerSocket: " + mNlpServerSocket);
            }

            while (mIsStopping != true) {
                if (DEBUG) log("NlpUtilsSocket, wait client");
                LocalSocket instanceSocket = mNlpServerSocket.accept();
                if (DEBUG) log("NlpUtilsSocket, instance: " + instanceSocket);
                if (mIsStopping != true) {
                    if (mClientCount.get() < NLPS_MAX_CLIENTS) {
                        new ServerInstanceThread(instanceSocket).start();
                    } else {
                        log("no resource, client count: " + mClientCount.get());
                        close(instanceSocket);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        closeServerSocket();
        if (DEBUG) log("NlpUtilsSocket-");
    }

    private class ServerInstanceThread extends Thread {
        LocalSocket mSocket;

        public ServerInstanceThread(LocalSocket instanceSocket) {
            mSocket = instanceSocket;
            mClientCount.getAndIncrement();
            if (DEBUG) log("client count+: " + mClientCount.get());
        }

        public void run() {
            try {
                if (DEBUG) log("NlpInstanceSocket+");
                DataInputStream dins = new DataInputStream(mSocket.getInputStream());
                while (mIsStopping != true) {
                    int cmd = getInt(dins);
                    //<< For future use...
                    int data1 = getInt(dins);
                    int data2 = getInt(dins);
                    int data3 = getInt(dins);
                    //>>
                    //log("cmd=" + cmd + "," + Integer.toHexString(data1) + ","
                    //+ Integer.toHexString(data2) + "," + Integer.toHexString(data3));
                    if (cmd == NLPS_CMD_GPS_NIJ_REQ) {
                        log("ClientCmd: NLPS_CMD_GPS_INJECT_REQ");
                        sendCommand(NLPS_MSG_NLP_NIJ_REQ, data1);
                    } else if (cmd == NLPS_CMD_GPS_NIJ_CANCEL) {
                        log("ClientCmd: NLPS_CMD_GPS_NIJ_CANCEL");
                        sendCommand(NLPS_MSG_NLP_NIJ_CANCEL, data1);
                    } else if (cmd == NLPS_CMD_QUIT) {
                        if (DEBUG) log("ClientCmd: QUIT");
                        break;
                    } else {
                        log("ClientCmd, unknown: " + cmd);
                        break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            closeInstanceSocket();
            if (DEBUG) log("NlpInstanceSocket-");
        }

        private void closeInstanceSocket() {
            close(mSocket);
            mSocket = null;
            mClientCount.getAndDecrement();
            if (DEBUG) log("client count-: " + mClientCount.get());
        }
    }

    private void sendCommand(int cmd, int arg1) {
        Message msg = Message.obtain();
        msg.what = cmd;
        msg.arg1 = arg1;
        mHandler.sendMessage(msg);
    }

    private void sendCommandDelayed(int cmd, long delayMs) {
        Message msg = Message.obtain();
        msg.what = cmd;
        mHandler.sendMessageDelayed(msg, delayMs);
    }

    private class NlpsMsgHandler extends Handler {
        public NlpsMsgHandler(Looper looper) {
            super(looper, null, true);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case NLPS_MSG_NLP_NIJ_REQ:
                    if (DEBUG) log("handle NLPS_MSG_NLP_NIJ_REQ arg1: " + msg.arg1);
                    int reqSrc = msg.arg1;
                    if (reqSrc == NLP_CMD_SRC_UNKNOWN) {
                        reqSrc = NLP_CMD_SRC_MNL;
                    }
                    requestNlp(reqSrc);
                    break;
                case NLPS_MSG_NLP_NIJ_CANCEL:
                    if (DEBUG) log("handle NLPS_MSG_NLP_NIJ_CANCEL arg1: " + msg.arg1);
                    releaseNlp(msg.arg1);
                    break;
                case NLPS_MSG_NLP_UPDATED:
                    if (DEBUG) log("handle NLPS_MSG_NLP_UPDATED");
                    releaseNlp(NLP_CMD_SRC_MNL);
                    break;
                case NLPS_MSG_CLEAR_LAST_LOC:
                    if (DEBUG) log("handle NLPS_MSG_CLEAR_LAST_LOC");
                    clearLastLocation();
                    break;
                default:
                    log("Undefined message: " + msg.what);
            }
        }
    }
}
