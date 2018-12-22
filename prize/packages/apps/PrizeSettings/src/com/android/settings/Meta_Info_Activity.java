package com.android.settings;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.provider.Settings.Global;
import android.content.ContentResolver;
import java.util.Map;
import android.content.IntentFilter;
import android.net.wifi.ScanSettings;
import android.net.wifi.WifiScanner;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import android.os.ServiceManager;
import android.os.IBinder;
import android.os.RemoteException;
import java.util.Collection;
import android.os.Parcel;
import android.os.Parcelable;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConfiguration.AuthAlgorithm;
import android.net.wifi.WifiConfiguration.KeyMgmt;
import android.net.wifi.SupplicantState;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.net.wifi.WifiInfo;
import android.net.IpConfiguration;
import android.net.IpConfiguration.IpAssignment;
import java.net.InetAddress;
import android.net.StaticIpConfiguration;
import java.net.Inet4Address;
import android.net.NetworkUtils;
import android.net.LinkAddress;
import android.os.SystemProperties;
import java.lang.InterruptedException;
import android.net.wifi.WifiConfiguration.KeyMgmt;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;


public class Meta_Info_Activity extends Activity implements View.OnClickListener {

    //default config
    private String default_target_ssid = "oppometa";
    private String default_target_on_rssi = "-45";
    private String default_target_off_rssi = "-55";
    private String default_target_channel = "6";
    private String default_target_ip_address = "192.168.43.108";
    private String default_target_network_dns = "192.168.43.1";
    private String default_target_network_gateway = "192.168.43.1";
    private String default_target_auto_connect_delay = "1";
    private String default_target_auto_disconnect_delay = "2";
    private	String default_target_network_keyMgmt = "NONE";
    private	String default_target_network_password = null;
    private String default_als_threshold = "5";
    private String default_ps_threshold = "0";
    private String default_use_dhcp = "0";
    private String default_check_alsps = "0";

    private static final String META_CONNECT_TYPE = "persist.meta.connecttype";
    private static final String ATM_IP_ADDRESS = "persist.atm.ipaddress";
    private static final String ATM_MD_MODE = "persist.atm.mdmode";
    private static final String PATH = "/sdcard/wifi_socket_config.ini";
    private static final String SECTION_NAME = "config_info";
    private static final String TAG = "Meta_Info_Activity";
    private boolean loaded = false;

    private int connectThreshold = 0;
    private int disconnectThreshold = 0;
    int targetConnectRssi = 0;
    int targetDisonnectRssi = 0;
    String target_network_keygmt = "NONE";
    String target_network_password = null;

    boolean startScanThread = false;

    String connectBSSID = null;
    boolean isFirstTimeRSSI = true;

    private WifiManager mWifiManager;
    NetworkInfo.DetailedState lastState = NetworkInfo.DetailedState.IDLE;

    private int use_dhcp = -1;

    private SensorManager sensorManager;
    private int alsData = -1;
    private int psData = -1;
    private int alsThreshold = -1;
    private int psThreshold = -1;
    private int check_alsps = 0;
    boolean isDutInBox = false;

    //save ini
    Map<String,String> config_ini_map;

    Handler handler;
    private static final int UPDATE_CONNECT_TYPE = 0x01;
    private static final int UPDATE_ALSPS_STATUS = 0x02;
    private static final String ACTION_AUTO_EXIT_ACTIVITY = "Exit_Meta_Info_Activity";

    WifiConfiguration config = new WifiConfiguration();

    Handler mHandler;

    //main ui compontent
    TextView ALS_Value;
    TextView PS_Value;
    TextView Target_SSID;
    TextView Auto_Connect_RSSI;
    TextView Auto_Disconnect_RSSI;
    TextView Target_Channel;
    TextView Target_IP;
    TextView Target_Gateway;
    TextView Target_DNS;
    TextView Auto_Connect_Retry;
    TextView Auto_Disconnect_Retry;
    TextView WIFI_Status;
    TextView Current_RSSI;
    TextView MAC_Address;
    TextView Connect_Type;
    Button exit_Button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.meta_info_activity);
        ALS_Value = (TextView) findViewById(R.id.ALS_Value_value);
        PS_Value = (TextView) findViewById(R.id.PS_Value_value);
        Target_SSID = (TextView) findViewById(R.id.Target_SSID_value);
        Auto_Connect_RSSI = (TextView) findViewById(R.id.Auto_Connect_RSSI_value);
        Auto_Disconnect_RSSI = (TextView) findViewById(R.id.Auto_Disconnect_RSSI_value);
        Target_Channel = (TextView) findViewById(R.id.Target_Channel_value);
        Target_IP = (TextView) findViewById(R.id.Target_IP_value);
        Target_Gateway = (TextView) findViewById(R.id.Target_Gateway_value);
        Target_DNS = (TextView) findViewById(R.id.Target_DNS_value);
        Auto_Connect_Retry = (TextView) findViewById(R.id.Auto_Connect_Retry_value);
        Auto_Disconnect_Retry = (TextView) findViewById(R.id.Auto_Disconnect_Retry_value);
        WIFI_Status = (TextView) findViewById(R.id.WIFI_Status_value);
        Current_RSSI = (TextView) findViewById(R.id.Current_RSSI_value);
        MAC_Address = (TextView) findViewById(R.id.MAC_Address_value);
        Connect_Type = (TextView) findViewById(R.id.Connect_Type_value);
        exit_Button = (Button) findViewById(R.id.Exit_Button);
        exit_Button.setOnClickListener(this);
        mWifiManager = (WifiManager)getSystemService(WIFI_SERVICE);

        //register WifiStateReceiver
        new WifiStateReceiver().register(this) ;
        
        //register als and ps sensor listener
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor als_sensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        Sensor ps_sensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        sensorManager.registerListener(alsListener, als_sensor, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(psListener, ps_sensor, SensorManager.SENSOR_DELAY_NORMAL);
        
        //register auto exit broadcast receiver
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_AUTO_EXIT_ACTIVITY);
        registerReceiver(AutoExitReceiver, intentFilter);

        mHandler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case UPDATE_CONNECT_TYPE:
                        if (Connect_Type != null) {
                            Log.d(TAG,"update connect type:" + (String)msg.obj);
                            Connect_Type.setText((String)msg.obj);
                        }
                        break;
                    case UPDATE_ALSPS_STATUS:
                        if (ALS_Value != null && PS_Value != null) {
                            ALS_Value.setText((String)msg.obj);
                            PS_Value.setText((String)msg.obj);
                        }
                    default:
                        break;
                }
            }
        };

        Log.d(TAG,"load config_ini_map");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    config_ini_map = new ConfigReader(PATH).getSingleMap(SECTION_NAME);
                    Target_SSID.setText(config_ini_map.getOrDefault(new String("target_ssid"),default_target_ssid ));
                    Auto_Connect_RSSI.setText(config_ini_map.getOrDefault(new String("target_on_rssi"),default_target_on_rssi));
                    Auto_Disconnect_RSSI.setText(config_ini_map.getOrDefault(new String("target_off_rssi"),default_target_off_rssi));
                    Target_Channel.setText(config_ini_map.getOrDefault(new String("target_channel"),default_target_channel));
                    Target_IP.setText(config_ini_map.getOrDefault(new String("target_ip_address"),default_target_ip_address));
                    Target_Gateway.setText(config_ini_map.getOrDefault(new String("target_network_gateway"),default_target_network_gateway));
                    Target_DNS.setText(config_ini_map.getOrDefault(new String("target_network_dns"),default_target_network_dns));
                    Auto_Connect_Retry.setText(config_ini_map.getOrDefault(new String("target_auto_connect_delay"),default_target_auto_connect_delay));
                    Auto_Disconnect_Retry.setText(config_ini_map.getOrDefault(new String("target_auto_disconnect_delay"),default_target_auto_disconnect_delay));						
                    target_network_keygmt = config_ini_map.getOrDefault(new String("target_network_keyMgmt"),default_target_network_keyMgmt);
                    target_network_password = config_ini_map.getOrDefault(new String("target_network_password"),default_target_network_password);
                    alsThreshold = Integer.parseInt(config_ini_map.getOrDefault(new String("als_data_threshold"),default_als_threshold));
                    psThreshold = Integer.parseInt(config_ini_map.getOrDefault(new String("ps_data_threshold"),default_ps_threshold));
                    use_dhcp = Integer.parseInt(config_ini_map.getOrDefault(new String("use_dhcp"),default_use_dhcp));
                    check_alsps = Integer.parseInt(config_ini_map.getOrDefault(new String("check_alsps"),default_check_alsps));
                    Log.d(TAG,"ini load completed");
                    loaded = true;
                } catch (Exception e) {
                    Log.d(TAG,"file not find or io exception,loaded the default value");
                    Target_SSID.setText(default_target_ssid );
                    Auto_Connect_RSSI.setText(default_target_on_rssi);
                    Auto_Disconnect_RSSI.setText(default_target_off_rssi);
                    Target_Channel.setText(default_target_channel);
                    Target_IP.setText(default_target_ip_address);
                    Target_Gateway.setText(default_target_network_gateway);
                    Target_DNS.setText(default_target_network_dns);
                    Auto_Connect_Retry.setText(default_target_auto_connect_delay);
                    Auto_Disconnect_Retry.setText(default_target_auto_disconnect_delay);
                    target_network_keygmt = default_target_network_keyMgmt;
                    target_network_password = default_target_network_password;
                    alsThreshold = Integer.parseInt(default_als_threshold);
                    psThreshold = Integer.parseInt(default_ps_threshold);
                    use_dhcp = Integer.parseInt(default_use_dhcp);
                    check_alsps = Integer.parseInt(default_check_alsps);
                    loaded = true;
                    e.printStackTrace();
                }
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                //wait for loaded
                while(!loaded);
                connectThreshold = Integer.parseInt(Auto_Connect_Retry.getText().toString());
                disconnectThreshold = Integer.parseInt(Auto_Disconnect_Retry.getText().toString());
                targetConnectRssi =Integer.parseInt(Auto_Connect_RSSI.getText().toString());
                targetDisonnectRssi =Integer.parseInt(Auto_Disconnect_RSSI.getText().toString());
                //saveTargetNetwork();
            }
        }).start();

        //show connect type
        new Thread(new Runnable() {
            @Override
            public void run() {
                String pre_connect_type = "";
                String cur_connect_type="";
                Message msg = null;
                while(true) {
                    try{
                        Thread.sleep(50);
                        cur_connect_type = SystemProperties.get(META_CONNECT_TYPE);
                        if (!cur_connect_type.equals(pre_connect_type)) {
                            Log.d(TAG,"connect type changed from " + pre_connect_type + " to " + cur_connect_type);
                            msg = Message.obtain(mHandler,UPDATE_CONNECT_TYPE,cur_connect_type);
                            msg.sendToTarget();
                            pre_connect_type = cur_connect_type;
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();

        //new thread to read ALSPS status
        new Thread(new Runnable() {
            @Override
            public void run() {
                String alsps_status="";
                Message msg = null;
                if(0 != check_alsps) {
                    int retry_cnt = 0;
                    while(false == isDutInBox) {
                        try {
                            Log.d(TAG, "checkAlsPsData: als_data =" + alsData + ", ps_data = " + psData);
                            if(alsData < alsThreshold && psData > psThreshold) {
                                retry_cnt++;
                                Log.d(TAG, "checkAlsPsData: DUT inside shielding box for " + retry_cnt);
                                if(retry_cnt == 5) {
                                    Log.d(TAG, "checkAlsPsData: confirm DUT inside shielding box");
                                    isDutInBox = true;
                                    alsps_status = "PASS";
                                    msg = Message.obtain(mHandler, UPDATE_ALSPS_STATUS, alsps_status);
                                    msg.sendToTarget();
                                    break;
                                }
                                Thread.sleep(200);
                            } else {
                                retry_cnt = 0;
                                Log.d(TAG, "checkAlsPsData: DUT outside of shielding box");
                                Thread.sleep(200);
                                continue;
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                else {
                    isDutInBox = true;
                    alsps_status = "NO CHECK";
                    msg = Message.obtain(mHandler, UPDATE_ALSPS_STATUS, alsps_status);
                    msg.sendToTarget();
                }
            }
        }).start();

        //new thread to control scan
        final Thread scanThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(startScanThread) {
                    startScanFreq();
                }
            }
        });

        //scanThread.start();
        //open wifi and set scanalways to false
        turnOnWifi();
        removeAllConfigNetworks();
        setTargetNetworkConfig();
        startScanFreq();
    }

    protected void onDestroy() {
        super.onDestroy();
        if(sensorManager != null) {
            sensorManager.unregisterListener(alsListener);
            sensorManager.unregisterListener(psListener);
        }
        unregisterReceiver(AutoExitReceiver);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.Exit_Button) {
            Log.d(TAG,"exit meta_info_activity");
            SystemProperties.set(META_CONNECT_TYPE,"usb");
            CheckModemMode();
            mWifiManager.setWifiEnabled(false);
            Settings.Global.putInt(getContentResolver(), Settings.Global.WIFI_SCAN_ALWAYS_AVAILABLE,1);
            removeAllConfigNetworks();
            finish();
        }
    }

    @Override
    public void onBackPressed() {
            SystemProperties.set(META_CONNECT_TYPE,"usb");
            CheckModemMode();
            mWifiManager.setWifiEnabled(false);
            Settings.Global.putInt(getContentResolver(), Settings.Global.WIFI_SCAN_ALWAYS_AVAILABLE,1);
            removeAllConfigNetworks();
            finish();
    }

    private final BroadcastReceiver AutoExitReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG,"get broadcast action: " + action);
            if(ACTION_AUTO_EXIT_ACTIVITY.equals(action)) {
                Log.d(TAG,"exit meta_info_activity");
                SystemProperties.set(META_CONNECT_TYPE,"usb");
                CheckModemMode();
                mWifiManager.setWifiEnabled(false);
                Settings.Global.putInt(getContentResolver(), Settings.Global.WIFI_SCAN_ALWAYS_AVAILABLE,1);
                removeAllConfigNetworks();
                finish();
            }
        }
    };

    private class WifiStateReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
           String action = intent.getAction();
            if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)) {
                Log.d(TAG,"WIFI_STATE_CHANGED_ACTION");
            } else if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(action)) {
                Log.d(TAG,"SCAN_RESULTS_AVAILABLE_ACTION");
                String targetBssid = getScanResults();
                if (targetBssid != null) {
                    connectThreshold = Integer.parseInt(Auto_Connect_Retry.getText().toString());
                    handleConnectNetwork(targetBssid);
                }
            } else if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)) {
                NetworkInfo info = (NetworkInfo) intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                Log.d(TAG,"NETWORK_STATE_CHANGED_ACTION is " + info.getDetailedState());
                handleNetworkStateChange(info);
            } else if (WifiManager.RSSI_CHANGED_ACTION.equals(action)) {
                Log.d(TAG,"RSSI_CHANGED_ACTION");
                int newRssi = intent.getIntExtra(WifiManager.EXTRA_NEW_RSSI, 0);
                handleRssiChange(newRssi);
            }
        }

        void register(Context context) {
            IntentFilter mIntentFilter = new IntentFilter();
            mIntentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
            mIntentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
            mIntentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
            mIntentFilter.addAction(WifiManager.RSSI_CHANGED_ACTION);
            context.registerReceiver(this, mIntentFilter);
        }
    }

    private String getScanResults() {
        if(false == isDutInBox) {
            startScanFreq();
            return null;
        }
        String targetSSID = Target_SSID.getText().toString();

        int countApOverRssi = 0;
        int showRSSI = -999;

        //Log.d(TAG,"targetSSID is "+targetSSID +"targetConnectRssi is "+targetConnectRssi);
        List<ScanResult> newResults = mWifiManager.getScanResults();
        for (ScanResult newResult : newResults) {
            if (newResult.SSID == null || newResult.SSID.isEmpty()) {
                continue;
            }
            String scanSSID = newResult.SSID;
            int scanRSSI = newResult.level;
            String scanBSSID = newResult.BSSID;
            if ((scanRSSI > showRSSI) && (scanSSID.equals(targetSSID))) {
                showRSSI = scanRSSI;
                Log.d(TAG, "update showRSSI :" + showRSSI);
            }
            //only one SSID's rssi is more than connect threshold
            if ((scanRSSI > targetConnectRssi) && (scanSSID.equals(targetSSID))) {
                //Scanned RSSI above threshold
                countApOverRssi++;
                Log.d(TAG,"scaned scanRSSI > targetConnectRssi");
                connectThreshold--;
                Log.d(TAG,"connectThreshold is" + connectThreshold);

                if (connectThreshold < 1) {
                    Log.d(TAG,"targetSSID is "+scanSSID +"targetConnectRssi is "+scanRSSI);
                    connectBSSID = newResult.BSSID;
                    Log.d(TAG,"connectBSSID is "+connectBSSID);
                    connectThreshold = Integer.parseInt(Auto_Connect_Retry.getText().toString());
                } else {
                    startScanFreq();
                }
                break;
            } else if((connectBSSID != null)&&(scanBSSID.equals(connectBSSID))) {
                //Scanned RSSI below disconnect threshold
                Log.d(TAG,"scaned connectedSSID" );
                if (scanRSSI < targetDisonnectRssi) {
                    disconnectThreshold--;
                    Log.d(TAG,"disconnectThreshold is" + disconnectThreshold);
                }
                if (disconnectThreshold <1) {
                    Log.d(TAG,"disconnect "+scanBSSID);
                    mWifiManager.disconnect();
                    isFirstTimeRSSI = true;
                    removeAllConfigNetworks();
                    disconnectThreshold = Integer.parseInt(Auto_Disconnect_Retry.getText().toString());
                }
                break;
            }else
                continue;
        }
        if (countApOverRssi == 0) {
            //Restart scan when no RSSI meets request
            startScanFreq();
        }
        Current_RSSI.setText(String.valueOf(showRSSI));
        return connectBSSID;
    }

    private void handleConnectNetwork(String bssid) {
        config.BSSID = bssid;
        Log.d(TAG,"connect network mac is " + bssid);

        mWifiManager.connect(config,null);
    }

    private void removeAllConfigNetworks() {
        Log.d(TAG,"removeAllConfigNetworks");
        List<WifiConfiguration> networks = mWifiManager.getConfiguredNetworks();
        if (networks != null) {
        //Log.d(TAG,"networks = " + networks );
            int length = networks.size();
            for (int i = 0; i < length; i++) {
                mWifiManager.forget(networks.get(i).networkId ,null);
        }
        Log.d(TAG,"forget all saved network");
       }
    }

    private static String convertToQuotedString(String string) {
       return "\"" + string + "\"";
    }

    private static String quotedString(String s) {
        return String.format("\"%s\"", s);
    }

    private WifiConfiguration setTargetNetworkConfig() {
        //need add a password and kgt
        //set config ssid
        config.SSID = convertToQuotedString(Target_SSID.getText().toString());
        //config.channel = Integer.parseInt(Target_Channel.getText().toString());
        //int key = 0;
        for (int i=0;i<8;i++) {
            if (KeyMgmt.strings[i].equals(target_network_keygmt)) {
                config.allowedKeyManagement.set(i);
                break;
            }
        }
        if (target_network_password != null) {
            config.preSharedKey = quotedString(target_network_password);
        }
        //set ip config
        if(use_dhcp == 0) {
            StaticIpConfiguration staticIpConfig = new StaticIpConfiguration();
            //get ipaddress
            String ipAddr = Target_IP.getText().toString();
            SystemProperties.set(ATM_IP_ADDRESS,ipAddr);
            Inet4Address inetAddr = getIPv4Address(ipAddr);
            //get networkPrefixLength default is 24
            int networkPrefixLength = 24;
            staticIpConfig.ipAddress = new LinkAddress(inetAddr, networkPrefixLength);

            //set gateway
            String gateway = Target_Gateway.getText().toString();
            InetAddress gatewayAddr = getIPv4Address(gateway);
            staticIpConfig.gateway = gatewayAddr;

            //set dnsserver
            String dns = Target_DNS.getText().toString();
            InetAddress dnsAddr = getIPv4Address(dns);
            staticIpConfig.dnsServers.add(dnsAddr);
            staticIpConfig.dnsServers.add(dnsAddr);

            config.setIpAssignment(IpAssignment.STATIC);
            config.setStaticIpConfiguration(staticIpConfig);
        }

        Log.d(TAG,"save the target ap  " + config);

        return config;
    }

    private Inet4Address getIPv4Address(String text) {
        try {
            text = text.split("/")[0];
            Log.d(TAG,"Ip address: " + text);
            return (Inet4Address) NetworkUtils.numericToInetAddress(text);
        } catch (IllegalArgumentException | ClassCastException e) {
            return null;
        }
    }

    private void saveTargetNetwork() {
        WifiConfiguration saveConfig = setTargetNetworkConfig();
        //saveConfig.BSSID = bssid;
        mWifiManager.save(saveConfig,null);
        //mWifiManager.disableEphemeralNetwork(saveConfig.SSID);
    }

    private void startScanFreq() {
        Log.d(TAG,"startScanFreq");
        mWifiManager.startScan();
    }

    private void turnOnWifi() {
        //stop reconnect scan
        if(mWifiManager.setWifiEnabled(true)) {
            Log.d(TAG,"setwifienable true");
            final ContentResolver cr = getContentResolver();
            final int scanAlways = Settings.Global.getInt(cr,Settings.Global.WIFI_SCAN_ALWAYS_AVAILABLE, 0);
            Log.d("TAG","scanAlways is "+ scanAlways);
            if (scanAlways != 0) {
                Global.putInt(getContentResolver(),Global.WIFI_SCAN_ALWAYS_AVAILABLE,0);
                Log.d(TAG,"set scanalways to 0");
            }
            mWifiManager.stopReconnectAndScan(true,36000,true); //Disable auto connect for 1 hour
        }
    }
    private void handleRssiChange(int rssi) {
        Log.d(TAG, "newRssi is " + rssi);
        Current_RSSI.setText(String.valueOf(rssi));

        if (isFirstTimeRSSI&&rssi < targetDisonnectRssi) {
            if (disconnectThreshold>0) {
                //disconnectThreshold--;
                Log.d(TAG, "newRssi below targetDisconnectRSSI ");
                isFirstTimeRSSI = false;
                startScanFreq();
            }
        }
    }

    private void handleNetworkStateChange(NetworkInfo mNetworkInfo) {
        NetworkInfo.DetailedState currentdState = mNetworkInfo.getDetailedState();
        if (currentdState != lastState) {
            lastState = currentdState;
            // reset & clear notification on a network connect & disconnect
            switch (currentdState) {
                case CONNECTED:
                    SystemProperties.set(META_CONNECT_TYPE,"wifi");
                    Log.d(TAG,"set persist.meta.connecttype to wifi");
                    WIFI_Status.setText(R.string.Wifi_Connected);
                    final WifiInfo wifiInfo = mWifiManager.getConnectionInfo();

                    if (wifiInfo != null) {
                        int connectedFrequency = wifiInfo.getFrequency();
                        int connectedChannel = checkConnectedChannel(connectedFrequency);
                        Log.d(TAG,"get connected channel: " + connectedChannel);
                        Target_Channel.setText(String.valueOf(connectedChannel));

                        if(use_dhcp == 1) {
                            InetAddress connectedIP = NetworkUtils.intToInetAddress(wifiInfo.getIpAddress());
                            String connectedIpAddress = checkConnectedIp(connectedIP);
                            Target_IP.setText(connectedIpAddress);
                            Log.d(TAG,"get dhcp ip address: " + connectedIpAddress);
                            SystemProperties.set(ATM_IP_ADDRESS,connectedIpAddress);
                        }
                        MAC_Address.setText(wifiInfo.getBSSID());
                    } else {
                        Log.d(TAG,"get wifiInfo fail");
                    }
                    break;
                case DISCONNECTED:
                    WIFI_Status.setText(R.string.Wifi_Disconnected);
                    break;
                case OBTAINING_IPADDR:
                    WIFI_Status.setText(R.string.Wifi_OBTAINING_IPADDR);
                    break;
                case SCANNING:
                    WIFI_Status.setText(R.string.Wifi_Scaning);
                    break;
                case CONNECTING:
                    WIFI_Status.setText(R.string.Wifi_Connecting);
                    break;
                case AUTHENTICATING:
                    WIFI_Status.setText(R.string.Wifi_AUTHENTICATING);
                    break;
                default:
                    break;
            }
        }
    }

    private SensorEventListener alsListener = new SensorEventListener() {
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            if(check_alsps != 0 && isDutInBox == false)
            {
                alsData = (int) event.values[0];
                ALS_Value.setText(String.valueOf(alsData));
            }
        }
    };

    private SensorEventListener psListener = new SensorEventListener() {
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            if(check_alsps != 0 && isDutInBox == false)
            {
                psData = (int) event.values[0];
                PS_Value.setText(String.valueOf(psData));
            }
        }
    };

    private void CheckModemMode() {
        //Double check modem mode before exit, if tool failed to switch it
        String modemMode = SystemProperties.get(ATM_MD_MODE);
        if(modemMode.equals("meta")) {
            Log.d(TAG,"change modem mode to normal before exit");
            SystemProperties.set(ATM_MD_MODE, "normal");
        }
    }

    private String checkConnectedIp(InetAddress connectedIP) {
        Collection<InetAddress> ipAddress = new ArrayList<InetAddress>();
        ipAddress.add(connectedIP);
        String[] connectedIpAddress = new String[1];
        connectedIpAddress = NetworkUtils.makeStrings(ipAddress);
        return connectedIpAddress[0];
    }

    private int checkConnectedChannel(int connectedFrequency) {
        int channel = 0;
        int frequency = connectedFrequency;

        switch(frequency) {
            case 2412:
                channel = 1;
                break;
            case 2417:
                channel = 2;
                break;
            case 2422:
                channel = 3;
                break;
            case 2427:
                channel = 4;
                break;
            case 2432:
                channel = 5;
                break;
            case 2437:
                channel = 6;
                break;
            case 2442:
                channel = 7;
                break;
            case 2447:
                channel = 8;
                break;
            case 2452:
                channel = 9;
                break;
            case 2457:
                channel = 10;
                break;
            case 2462:
                channel = 11;
                break;
            case 2467:
                channel = 12;
                break;
            case 2472:
                channel = 13;
                break;
            case 2484:
                channel = 14;
                break;
            case 5180:
                channel = 36;
                break;
            case 5200:
                channel = 40;
                break;
            default:
                channel = 0;
        }
        return channel;
    }

}
