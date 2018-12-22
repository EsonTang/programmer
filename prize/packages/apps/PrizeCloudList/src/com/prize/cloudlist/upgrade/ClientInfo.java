package com.prize.cloudlist.upgrade;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.os.SystemProperties;
/**
 * 终端信息
 * 
 */
public class ClientInfo {
	//public static final String TAG = "cloudlist";
	public final static int NONET = 0;
	public final static int MOBILE_3G = 1;
	public final static int MOBILE_2G = 2;
	public final static int MOBILE_4G = 4;
	// public final static int CMNET = 1;
	// public final static int CMWAP = 2;
	public final static int WIFI = 3;
	public final static int MOBILE = 5;
	// 中国大陆三大运营商imei
	private static final String CHA_IMSI = "46003";
	private static final String CMCC_IMSI_1 = "46000";
	private static final String CMCC_IMSI_2 = "46002";
	private static final String CHU_IMSI = "46001";

	// 中国大陆三大运营商 provider
	private static final String CMCC = "中国移动";
	private static final String CHU = "中国联通";
	private static final String CHA = "中国电信";

	// 未知内容
	public static final String UNKNOWN = "unknown";

	private static ClientInfo instance;
	
	/**系统版本号：如5.0**/
	public String androidVersion = null;
	public int androidVerCode = 0;
	public String softwareVersion = null;	
	/**系统版本 didos version name**/
	public String systemVersion = null;	
	
	// cpu型号
	public String cpu = null;
	// 厂商
	public String brand = null;
	// 机型
	public String model = null;
	// imei
	public String imei1 = null;
	public String imei2 = null;
	public String imei = null;		
	
	// 屏幕大小
	public String screenSize = null;
	public int screenWidth = 0;
	public int screenHeight = 0;
	// 屏幕的dpi
	public short dpi = 0;	
	// mac地址
	public String mac = null;
	public String language;
	public String country;	
	
	private ClientInfo(Context context) {		

		softwareVersion = android.os.Build.DISPLAY;
		androidVersion = android.os.Build.VERSION.RELEASE + "";
		androidVerCode = Build.VERSION.SDK_INT;		
		try {
			systemVersion =SystemProperties.get("ro.product.system.version", "unknown");
		}catch (Exception e){
			systemVersion = UNKNOWN;
		}

		
		try {			

			cpu = getCpuInfo();

			WifiManager wifiManager = (WifiManager) context
					.getSystemService(Context.WIFI_SERVICE);
			WifiInfo wifiInfo = wifiManager.getConnectionInfo();
			if(wifiInfo != null)
			{
				mac = wifiInfo.getMacAddress();
			}
			Locale locale = context.getResources().getConfiguration().locale;
			language = locale.getLanguage();// 获取语言
			country = locale.getCountry();// 获取国家码
			Log.i("whitelist", "mac="+mac+"--systemVersion="+systemVersion
				+"--androidVersion-"+androidVersion+"-androidVerCode-"+androidVerCode);
			
			TelephonyManager telephonyManager = (TelephonyManager) context
					.getSystemService(Context.TELEPHONY_SERVICE);			
			getImei(context);
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}

		brand = Build.MANUFACTURER;// 手机厂商
		model = Build.MODEL;// 手机型号
		Log.i("whitelist", "brand-->" + brand);		
		
		DisplayMetrics dm = context.getResources().getDisplayMetrics();
		int width = dm.widthPixels;
		int height = dm.heightPixels;
		if (width > height) {
			screenWidth = height;
			screenHeight = width;
		} else {
			screenWidth = width;
			screenHeight = height;

		}
		screenSize = screenWidth + "*" + screenHeight;
		dpi = (short) dm.densityDpi;		
	}
	

	private void getImei(Context mContext) {
		try {			
			imei1 = SystemProperties.get("gsm.mtk.imei1", "");
			imei2 = SystemProperties.get("gsm.mtk.imei2", "");
			
			if (!TextUtils.isEmpty(imei1)) {
				imei = imei1;
			} else if (!TextUtils.isEmpty(imei2)) {
				imei = imei2;
			} else {
				imei = UNKNOWN;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public String toString() {
		return "ClientInfo{" +
				"imei=" + imei +
				", model=" + model +
				", version=" + softwareVersion +"}";
	}

	private static int check2GOr3GNet(Context context) {

		int mobileNetType = NONET;
		if (null == context) {
			return mobileNetType;
		}
		TelephonyManager telMgr = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);

		int netWorkType = telMgr.getNetworkType();
		switch (netWorkType) {
		case TelephonyManager.NETWORK_TYPE_UMTS:
		case TelephonyManager.NETWORK_TYPE_HSDPA:
		case TelephonyManager.NETWORK_TYPE_HSPA:
		case TelephonyManager.NETWORK_TYPE_HSUPA:
		case TelephonyManager.NETWORK_TYPE_EVDO_0:
		case TelephonyManager.NETWORK_TYPE_EVDO_A:
			// case TelephonyManager.NETWORK_TYPE_EVDO_B:
			mobileNetType = MOBILE_3G;
			break;
		case TelephonyManager.NETWORK_TYPE_UNKNOWN:
		case TelephonyManager.NETWORK_TYPE_IDEN:
		case TelephonyManager.NETWORK_TYPE_1xRTT:
		case TelephonyManager.NETWORK_TYPE_GPRS:
		case TelephonyManager.NETWORK_TYPE_EDGE:
		case TelephonyManager.NETWORK_TYPE_CDMA:
			mobileNetType = MOBILE_2G;
			break;
		case TelephonyManager.NETWORK_TYPE_LTE: // api<11 : replace by 13
			mobileNetType = MOBILE_4G;
			break;
		default:
			mobileNetType = MOBILE_3G;
			break;
		}

		return mobileNetType;

	}

	/**
	 * 取cpu 信息
	 * 
	 * @return cpu字符串
	 */
	private String getCpuInfo() {
		String str1 = "/proc/cpuinfo";
		String str2 = "";
		String[] cpuInfo = { "", "" };
		String[] arrayOfString;
		String ret;
		try {
			FileReader fr = new FileReader(str1);
			BufferedReader localBufferedReader = new BufferedReader(fr, 8192);
			str2 = localBufferedReader.readLine();
			if (null != str2) {
				arrayOfString = str2.split("\\s+");
				for (int i = 2; i < arrayOfString.length; i++) {
					cpuInfo[0] = cpuInfo[0] + arrayOfString[i] + " ";
				}
			}

			str2 = localBufferedReader.readLine();
			if (null != str2) {
				arrayOfString = str2.split("\\s+");
				cpuInfo[1] += arrayOfString[2];
			}

			localBufferedReader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		ret = cpuInfo[0];
		return ret;
	}

	public static ClientInfo getInstance(Context context) {
		if (null == instance) {
			instance = new ClientInfo(context);
		}
		return instance;
	}


	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}
}
