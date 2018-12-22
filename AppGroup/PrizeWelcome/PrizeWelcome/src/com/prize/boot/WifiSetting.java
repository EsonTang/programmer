package com.prize.boot;

import java.util.List;

import com.prize.boot.util.Utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

public class WifiSetting {
	private final WifiManager mWifiManager;

	/* security type */
	public static final int SECURITY_NONE = 0;
	public static final int SECURITY_WEP = 1;
	public static final int SECURITY_PSK = 2;
	public static final int SECURITY_WPA_PSK = 3;
	public static final int SECURITY_WPA2_PSK = 4;
	public static final int SECURITY_EAP = 5;
	public static final int SECURITY_WAPI_PSK = 6;
	public static final int SECURITY_WAPI_CERT = 7;
	
	
	public static final int WIFICIPHER_NOPASS = 1;
	public static final int WIFICIPHER_WEP = 2;
	public static final int WIFICIPHER_WPA = 3;
	
	public static final int CONNECTED = 1;
	public static final int CONNECTING = 2;
	public static final int UNKNOWN = 0;

	private static WifiSetting instance = null;
	
	private static Context mContext;

	public static WifiSetting getInstance(Context context) {
		if (instance == null) {
			instance = new WifiSetting(context);
		}
		return instance;
	}

	public WifiSetting(Context context) {
		mContext = context;
		mWifiManager = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);
	}

	public boolean setWifiEnable(boolean enable) {
		Log.d(Utils.TAG, "onCheckedChanged, setWifiEnabled = " + enable);
		return mWifiManager.setWifiEnabled(enable);
	}

	/**
	 * 打开WIFI
	 */
	private void openWifi() {
		Log.d(Utils.TAG, "open wifi");
		if (!mWifiManager.isWifiEnabled()) {
			mWifiManager.setWifiEnabled(true);
		}
	}

	// 连接状态
	public boolean getConnState(ScanResult result) {
		WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
		if (wifiInfo == null) {
			return false;
		}
		if (wifiInfo.getSSID().equals("\"" + result.SSID + "\"")) {
			return true;
		}
		return false;
	}
	
	//是否连接WIFI
    public int isWifiConnected()
    {
        ConnectivityManager connectivityManager = (ConnectivityManager)mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiNetworkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        State state = wifiNetworkInfo.getState();
        Log.i("prize","----Wifi-State ----> " + state.toString());
        if(wifiNetworkInfo.isConnected()) {
        	return CONNECTED;
        }
        if(state == State.CONNECTED){
        	return CONNECTED;
        }
        if(state == State.CONNECTING || state == State.DISCONNECTED || state == State.DISCONNECTING){
        	return CONNECTING;
        }
    
        return UNKNOWN ;
    }

	// 加密类型
	public static int getSecurity(ScanResult result) {
		Log.v("prize", "---wifi--ScanResult SSID -->"+result.SSID+", capabilities---->"+result.capabilities);
		if (result.capabilities.contains("WAPI-PSK")) {
			return SECURITY_WAPI_PSK;
		} else if (result.capabilities.contains("WAPI-CERT")) {
			return SECURITY_WAPI_CERT;
		} else if (result.capabilities.contains("WEP")) {
			return SECURITY_WEP;
		} else if (result.capabilities.contains("WPA")) {
			return SECURITY_WPA_PSK;
		} else if (result.capabilities.contains("PSK")) {
			return SECURITY_PSK;
		} else if (result.capabilities.contains("EAP")) {
			return SECURITY_EAP;
		}
		return SECURITY_NONE;
	}


	// 连接指定Id的WIFI
	public boolean ConnectWifi(List<WifiConfiguration> wifiConfigList,int wifiId) {
		for (int i = 0; i < wifiConfigList.size(); i++) {
			WifiConfiguration wifi = wifiConfigList.get(i);
			if (wifi.networkId == wifiId) {
				while (!(mWifiManager.enableNetwork(wifiId, true))) {// 激活该Id，建立连接
					// status:0--已经连接，1--不可连接，2--可以连接
					Log.i("ConnectWifi",
							String.valueOf(wifiConfigList.get(wifiId).status));
				}
				return true;
			}
		}
		return false;
	}

	// 得到Wifi配置好的信息
	public List<WifiConfiguration> getConfiguration() {
		List<WifiConfiguration> wifiConfigList = mWifiManager.getConfiguredNetworks();// 得到配置好的网络信息
		if (wifiConfigList == null) {
			return null;
		}
		for (int i = 0; i < wifiConfigList.size(); i++) {
			Log.i(Utils.TAG, wifiConfigList.get(i).SSID);
			Log.i(Utils.TAG, String.valueOf(wifiConfigList.get(i).networkId));
		}
		return wifiConfigList;
	}

	// 判定指定WIFI是否已经配置好,依据WIFI的地址BSSID,返回NetId
	public int IsConfiguration(List<WifiConfiguration> wifiConfigList, String SSID) {
		Log.i("prize", "IsConfiguration--->" + String.valueOf(wifiConfigList.size()) + "----SSID = "+SSID);
		for (int i = 0; i < wifiConfigList.size(); i++) {
			Log.i("prize",wifiConfigList.get(i).SSID+"------"+
					String.valueOf(wifiConfigList.get(i).networkId));
			if (wifiConfigList.get(i).SSID.equals("\"" + SSID + "\"")) {// 地址相同
				return wifiConfigList.get(i).networkId;
			}
		}
		return -1;
	}

	// 添加一个网络并连接
	public boolean addNetwork(WifiConfiguration wcg) {
		int wcgID = mWifiManager.addNetwork(wcg);
		return mWifiManager.enableNetwork(wcgID, true);
	}
	
	/*** 
     * 配置要连接的WIFI热点信息     
     * @param SSID 
     * @param password 
     * @param type  加密类型 
     * @return 
     */       
	public WifiConfiguration CreateWifiInfo(String SSID, String Password,
			int Type) {
		WifiConfiguration config = new WifiConfiguration();
		config.allowedAuthAlgorithms.clear();
		config.allowedGroupCiphers.clear();
		config.allowedKeyManagement.clear();
		config.allowedPairwiseCiphers.clear();
		config.allowedProtocols.clear();
		config.SSID = "\"" + SSID + "\"";

		WifiConfiguration tempConfig = this.IsExsits(SSID);
		if (tempConfig != null) {
			mWifiManager.removeNetwork(tempConfig.networkId);
		}

		if (Type == WIFICIPHER_NOPASS) { // WIFICIPHER_NOPASS
		//	config.wepKeys[0] = "";
			config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
		//	config.wepTxKeyIndex = 0;
		}
		if (Type == WIFICIPHER_WEP) { // WIFICIPHER_WEP
			config.hiddenSSID = true;
			config.wepKeys[0] = "\"" + Password + "\"";
			config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
			config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
			config.wepTxKeyIndex = 0;
		}
		if (Type == WIFICIPHER_WPA) { // WIFICIPHER_WPA
			config.preSharedKey = "\"" + Password + "\"";
			config.hiddenSSID = true;
			config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
			config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
			config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
			config.allowedPairwiseCiphers .set(WifiConfiguration.PairwiseCipher.CCMP);
			config.status = WifiConfiguration.Status.ENABLED;   
		}
		return config;
	}
	//判断WIFI热点信息是否存在
	private WifiConfiguration IsExsits(String SSID) {
		List<WifiConfiguration> existingConfigs = mWifiManager
				.getConfiguredNetworks();
		for (WifiConfiguration existingConfig : existingConfigs) {
			if (existingConfig.SSID.equals("\"" + SSID + "\"")) {
				return existingConfig;
			}
		}
		return null;
	}

}
