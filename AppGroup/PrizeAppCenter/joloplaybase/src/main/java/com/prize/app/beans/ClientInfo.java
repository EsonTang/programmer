/*
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：
 *当前版本：
 *作	者：
 *完成日期：
 *修改记录：
 *修改日期：
 *版 本 号：
 *修 改 人：
 *修改内容：
...
 *修改记录：
 *修改日期：
 *版 本 号：
 *修 改 人：
 *修改内容：
 *********************************************/
package com.prize.app.beans;

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
import android.os.SystemProperties;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;

import com.google.gson.Gson;
import com.prize.app.BaseApplication;
import com.prize.app.util.DataStoreUtils;
import com.prize.app.util.JLog;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Locale;

/**
 * 终端信息
 * 
 */
public class ClientInfo {
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

	// 包名
	public String packageName = null;
	/**系统版本号：如5.0**/
	public String androidVersion = null;
	/**系统版本号：如M7Q.V1.00**/
	public String androidVerName = null;
	/**系统版本号：如5.0系统对应21**/
	public int androidVerCode;
	/**系统版本**/
	public String systemVersion = null;
	// apkVerName
	public String appVersionCode = null;
	// 本包apk包
	public int appVersion = 1;
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
	public long clientStartTime;
	// imsi
	public String imsi = null;
	// 运营提供商
	public String operator = null;
	// 网络状态， 网络状态会不停变化，故设置成static，需实时更新
	public static byte networkType;
	// 渠道code
	public String channel = "";
	// ram大小
	public int ramSize = 0;
	// rom大小
	public long romSize = 0;
	// 屏幕大小
	public String screenSize = null;
	public int screenWidth = 0;
	public int screenHeight = 0;
	// 屏幕的dpi
	public short dpi = 0;
	// mac地址
	public String mac = null;
	// sd卡大小
	public String sdCardSize = null;

	public String language;
	public String userId;

	public String country;
	// 纬度
	public Double latitude = 0.0;
	// 经度
	public Double longitude = 0.0;
	/**地理位置：eg：中国广东省深圳市南山区科苑路11号*/
	public String location;
	// 地区经纬度
	public String position = null;
	public String sn = null;
	public String mobile = null;
	public int netStatus;

	public String apkSign = null;
	/** 移动设备标识 2.3add*/
	public String androidId = null;
	/** 移动设备序列号 2.3add*/
	public String serialNo = null;
	/**
	 * 获取手机唯一标识
	 */
	public String tid;
	/**
	 *
	 */
	public String pageInfo;
	/**
	 *
	 */
	public String ua;
	/**
	 * 获取手机的信息
	 */
	private ClientInfo() {
		Context context = BaseApplication.curContext;

		packageName = context.getPackageName();

		androidVerName = android.os.Build.DISPLAY;
		androidVersion = android.os.Build.VERSION.RELEASE + "";
		androidVerCode = Build.VERSION.SDK_INT;
		androidId= Settings.System.getString(context.getContentResolver(),Settings.Secure.ANDROID_ID);
		serialNo= Build.SERIAL;
		try {
			systemVersion =SystemProperties.get("ro.product.system.version", "unknown");
		}catch (Exception e){
			systemVersion = UNKNOWN;
		}


		// 获取packagemanager的实例
		PackageManager packageManager = context.getPackageManager();
		//
		channel = getAppMetaData(packageManager);
		// getPackageName()是你当前类的包名，0代表是获取版本信息
		PackageInfo packageInfo;
		try {
			packageInfo = packageManager.getPackageInfo(packageName, 0);
			appVersionCode = packageInfo.versionName;
			appVersion = packageInfo.versionCode;

			cpu = getCpuInfo();

			ramSize = getTotalMemory(context);
			romSize = getTotalInternalMemorySize();

			WifiManager wifiManager = (WifiManager) context
					.getSystemService(Context.WIFI_SERVICE);
			WifiInfo wifiInfo = wifiManager.getConnectionInfo();
			mac = wifiInfo.getMacAddress();
			Locale locale = context.getResources().getConfiguration().locale;
			language = locale.getLanguage();// 获取语言
			country = locale.getCountry();// 获取国家码
			if (JLog.isDebug) {
				JLog.i("long", "channelCode-->" + channel+"--mac="+mac+"--systemVersion="+systemVersion+"--appVersionCode="+appVersionCode
				+"--androidVersion-"+androidVersion+"-androidVerName-"+androidVerName+"-androidVerCode-"+androidVerCode);
			}
			TelephonyManager telephonyManager = (TelephonyManager) context
					.getSystemService(Context.TELEPHONY_SERVICE);
			imsi = telephonyManager.getSubscriberId();
			if (TextUtils.isEmpty(imsi)) {
				imsi = UNKNOWN;
			}
			sn = telephonyManager.getSimSerialNumber();
			if (TextUtils.isEmpty(sn)) {
				sn = UNKNOWN;
			}
			mobile = telephonyManager.getLine1Number();
			getImei(context);
			formateLocation();
		} catch (Exception e) {
			e.printStackTrace();
		}

		brand = Build.MANUFACTURER;// 手机厂商
		model = Build.MODEL;// 手机型号
		if (JLog.isDebug) {
			JLog.i("long", "brand-->" + brand);
		}
		operator = getProvider(imsi);

		networkType = (byte) getAPNType(context);
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
		sdCardSize = getSDCardMemory();
	}

	private void formateLocation() {
		String location = DataStoreUtils.readLocalInfo("locationKey");
		if (!TextUtils.isEmpty(location)) {
			LocationInfo info = new Gson().fromJson(location, LocationInfo.class);
			latitude = info.latitude;
			longitude = info.lontitude;
			this.location = info.address;
		}
	}

	/**
	 * 获取application中指定的meta-data(切勿改变getString("UMENG_CHANNEL");中的key值) 其与清单文件
	 * meta-data android:name="UMENG_CHANNEL"
	 * 
	 * @return 如果没有获取成功(没有对应值,或者异常)，则返回值为空
	 */
	private String getAppMetaData(PackageManager packageManager) {
		String resultData = null;
		try {
			if (packageManager != null) {
				ApplicationInfo applicationInfo = packageManager
						.getApplicationInfo(packageName,
								PackageManager.GET_META_DATA);
				if (applicationInfo != null) {
					if (applicationInfo.metaData != null) {
						resultData = applicationInfo.metaData
								.getString("UMENG_CHANNEL");
					}
				}

			}
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}

		return resultData;
	}

	private void getImei(Context mContext) {
		try {
			CTelephoneInfo telephonyInfo = CTelephoneInfo.getInstance(mContext);
			telephonyInfo.setCTelephoneInfo(channel);
			imei1 = telephonyInfo.getImeiSIM1();
			imei2 = telephonyInfo.getImeiSIM2();
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
				"latitude=" + latitude +
				", longitude=" + longitude +
				", location='" + location + '\'' +
				", tid='" + tid + '\'' +
				'}';
	}

	/**
	 * 会报这个StatFs sf = new StatFs(sdcardDir.getPath());<br>
	 * java.lang.IllegalArgumentException异常
	 */
	@SuppressLint("NewApi")
	private String getSDCardMemory() {
		String ret = "0";
		long[] sdCardInfo = new long[2];
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			File sdcardDir = Environment.getExternalStorageDirectory();
			StatFs sf = new StatFs(sdcardDir.getPath());
			// modify by huanglingjun 2015-12-2 第三版和系统版控制，别覆盖
			long bSize = 0;
			long bCount = 0;
			if (BaseApplication.isThird) {
				bSize = sf.getBlockSize();
				bCount = sf.getBlockCount();
			} else {
				bSize = sf.getBlockSizeLong();
				bCount = sf.getBlockCountLong();
			}

			// long availBlocks = sf.getAvailableBlocks();

			sdCardInfo[0] = bSize * bCount;// 总大小
			// sdCardInfo[1] = bSize * availBlocks;// 可用大小
			ret = sdCardInfo[0] + "";
			// ret = String.valueOf((sdCardInfo[0] / 1024) / 1024 / 1024);
		}
		return ret;
	}

	/**
	 * 取Rom Size
	 * 
	 */
	private long getTotalInternalMemorySize() {
		File path = Environment.getDataDirectory();
		StatFs stat = new StatFs(path.getPath());
		long blockSize = stat.getBlockSize();
		long totalBlocks = stat.getBlockCount();
		return totalBlocks * blockSize;
	}

	// 获取IMSI号的供应商
	private String getProvider(String imsi) {

		String provider = UNKNOWN; // 当前sim卡运营商 //3为未知的 或者没有sim卡的比如平板
		if (imsi != null) {
			if (imsi.startsWith(CMCC_IMSI_1) || imsi.startsWith(CMCC_IMSI_2)) {// 中国移动
				provider = CMCC;
			} else if (imsi.startsWith(CHU_IMSI)) {// 中国联通
				provider = CHU;
			} else if (imsi.startsWith(CHA_IMSI)) {// 中国电信
				provider = CHA;
			}
		}
		JLog.i("ClientInfo","provider is " + provider);
		return provider;
	}

	// 获取手机总内存
	private int getTotalMemory(Context context) {
		String str1 = "/proc/meminfo";
		String str2;
		String[] arrayOfString;
		int initial_memory = 0;
		try {
			FileReader localFileReader = new FileReader(str1);
			BufferedReader localBufferedReader = new BufferedReader(
					localFileReader, 8192);
			if (localBufferedReader != null) {
				str2 = localBufferedReader.readLine();
				if (str2 != null) {
					arrayOfString = str2.split("\\s+");
					initial_memory = Integer.valueOf(arrayOfString[1])
							.intValue();// KB
					localBufferedReader.close();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return initial_memory; // G
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

	// 获取当前网络状态
	public static int getAPNType(Context context) {
		int netType = NONET;
		networkType = NONET;

		if (null == context) {
			return netType;
		}
		ConnectivityManager connMgr = (ConnectivityManager) context.getApplicationContext()
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		if (networkInfo == null || (networkInfo.getState() != State.CONNECTED)) {
			return netType;
		}
		int nType = networkInfo.getType();
		if (nType == ConnectivityManager.TYPE_MOBILE) {
			netType = check2GOr3GNet(context);
		} else if (nType == ConnectivityManager.TYPE_WIFI) {
			netType = WIFI;
		} else {
			boolean b = ConnectivityManager.isNetworkTypeValid(nType);
			if (b) {
				netType = MOBILE_3G; // 联通3G就跑这里
			}
		}
		networkType = (byte) netType;
		return netType;
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

	public static ClientInfo getInstance() {
		if (null == instance) {
			instance = new ClientInfo();
		}
		return instance;
	}

	/**
	 * 获取手机的信息（型号。厂商。分辨率等）
	 * 
	 */
	public static void initClientInfo() {
		getInstance();
	}



	public void setClientStartTime(long clientStartTime) {
		this.clientStartTime = clientStartTime;
	}




	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}


	public void setNetStatus(int netStatus) {
		this.netStatus = netStatus;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public void setApkSign(String apkSign) {
		this.apkSign = apkSign;
	}

}
