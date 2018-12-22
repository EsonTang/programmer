/*******************************************
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;

import com.prize.app.BaseApplication;
import com.prize.app.util.JLog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Environment;
import android.os.StatFs;
import android.os.SystemProperties;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;

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
	// 安卓系统版本号
	public String androidVersion = null;
	public String androidVerName = null;
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
	// 网络状态， 网络状态会不停变化，故设置成static，需实时更新
	public String operator = null;
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
	// 地区经纬度
	public String position = null;
	public String sn = null;
	public String mobile = null;
	public int netStatus;
	
	public  String ip= null;
	public  String androidId;
	
	public  String apkSign=null;
	
	public String systemVersion="6.0";	
	public int androidVerCode;	
	public String serialNo;
	public String platform;
	

	/**
	 * 获取手机的信息
	 */
	private ClientInfo() {
		Context context = BaseApplication.curContext;

		packageName = context.getPackageName();
		
		apkSign=BaseApplication.getSign();
		
		
		androidVerName = android.os.Build.DISPLAY;
		androidVersion = android.os.Build.VERSION.RELEASE + "";
		
		//modified by huangchangguo 2017.6.27
		try {	
		serialNo=android.os.Build.SERIAL;		
		androidVerCode=android.os.Build.VERSION.SDK_INT;		
		platform = SystemProperties.get("ro.board.platform");
		
		//systemVersion = android.os.Build.SYSTEM_VERSION;
			
		} catch (Exception e) {
			e.printStackTrace();
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
			if (ip==null) 
				getNetIp();			
			ramSize = getTotalMemory(context);
			romSize = getTotalInternalMemorySize();

			WifiManager wifiManager = (WifiManager) context
					.getSystemService(Context.WIFI_SERVICE);
//			WifiInfo wifiInfo = wifiManager.getConnectionInfo();
//			mac = wifiInfo.getMacAddress();
			try {
				mac =getMac();
				//Log.i("huang-ClientInfo:", mac);				
				if (mac.equals("")) {
					mac=getNewMac();
					if (mac==null) {
						mac="02:00:00:00:00:00";//默认地址
					}					
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			androidId = Settings.System.getString(context.getContentResolver(), Settings.System.ANDROID_ID); 	
			Locale locale = context.getResources().getConfiguration().locale;
			language = locale.getLanguage();// 获取语言
			country = locale.getCountry();// 获取国家码

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
		} catch (Exception e) {
		} finally {
			// if (null == imsi) {
			// imsi = UNKNOWN;
			// }
		}

		brand = Build.MANUFACTURER;// 手机厂商
		model = Build.MODEL;// 手机型号
		
		operator = getProvider(imsi);

		networkType = (byte) getAPNType(context);
		DisplayMetrics dm = context.getResources().getDisplayMetrics();
		JLog.i("ClientInfo", "dm="+dm);
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

	/**
	 * 获取application中指定的meta-data(切勿改变getString("UMENG_CHANNEL");中的key值) 其与清单文件
	 * meta-data android:name="UMENG_CHANNEL"
	 * 
	 * @return 如果没有获取成功(没有对应值，或者异常)，则返回值为空
	 */
	public String getAppMetaData(PackageManager packageManager) {
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
		CTelephoneInfo telephonyInfo = CTelephoneInfo.getInstance(mContext);
		telephonyInfo.setCTelephoneInfo();
		imei1 = telephonyInfo.getImeiSIM1();
		imei2 = telephonyInfo.getImeiSIM2();
		//优先取imei2
		if (!TextUtils.isEmpty(imei2)) {
			imei = imei2;
		} else if (!TextUtils.isEmpty(imei1)) {
			imei = imei1;
		} else {
			imei = UNKNOWN;
		}
	}

	@Override
	public String toString() {
		return "ClientInfo [packageName=" + packageName + ", androidVer="
				+ androidVersion + ", androidVerName=" + androidVerName
				+ ", apkVerName=" + appVersionCode + ", apkVerCode="
				+ appVersion + ", cpu=" + cpu + ", hsman=" + brand
				+ ", hstype=" + model + ", imei1=" + imei1 + ", imei2=" + imei2
				+ ", imsi=" + imsi + ", provider=" + operator
				+ ", channelCode=" + channel + ", ramSize=" + ramSize
				+ ", romSize=" + romSize + ", screenSize=" + screenSize
				+ ", screenWidth=" + screenWidth + ", screenHeight="
				+ screenHeight + ", dpi=" + dpi + ", mac=" + mac
				+ ", sdCardSize=" + sdCardSize + ", localLanguage=" + language
				+ ", country=" + country + ", latitude=" + latitude
				+ ", longitude=" + longitude + ", position=" + position
				+ ", sn=" + sn + ", mobile=" + mobile + "]";
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
			// if (BaseApplication.isThird) {
			// bSize = sf.getBlockSize();
			// bCount = sf.getBlockCount();
			// } else {
			bSize = sf.getBlockSizeLong();
			bCount = sf.getBlockCountLong();
			// }

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
	 * @return
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
		JLog.i("net", "net is " + netWorkType);
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
		ConnectivityManager connMgr = (ConnectivityManager) context
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
//		if (ip==null) {
//			getNetIp();
//		}
		networkType = (byte) netType;			
		JLog.info("networkType is " + networkType);
		return netType;
	}
	
	static Boolean isRunning=false;
	/* 获取外网的IP(要访问Url，要放到后台线程里处理) 
     * @return String 
     */  
    public  void getNetIp() {  
    	if (isRunning) 
			return;
    	isRunning=true;
       Thread thread = new Thread(new Runnable() {
		
		@Override
		public void run() {				
			ip = getCommonIP();
			if (ip==null) //如果ip为空，从168里面
				ip = GetNetIp();			
			isRunning=false;		
			JLog.i("huang-ClientInfo", " ip:"+ip);
		}
	}); 
     thread .start();  
    }
    
     private  String getCommonIP(){
	   URL infoUrl = null;  
       InputStream inStream = null;  
       String line = "";  
       try {  
           infoUrl = new URL("http://pv.sohu.com/cityjson?ie=utf-8");  
           URLConnection connection = infoUrl.openConnection();  
           HttpURLConnection httpConnection = (HttpURLConnection) connection;  
           int responseCode = httpConnection.getResponseCode();  
           if (responseCode == HttpURLConnection.HTTP_OK) {  
               inStream = httpConnection.getInputStream();  
               BufferedReader reader = new BufferedReader(new InputStreamReader(inStream, "utf-8"));  
               StringBuilder strber = new StringBuilder();  
               while ((line = reader.readLine()) != null)  
                   strber.append(line + "\n");  
               inStream.close();  
               // 从反馈的结果中提取出IP地址  
               int start = strber.indexOf("{");  
               int end = strber.indexOf("}");  
               String json = strber.substring(start, end + 1);  
               if (json != null) {  
                   try {  
                       JSONObject jsonObject = new JSONObject(json);  
                       line = jsonObject.optString("cip");  
                   } catch (JSONException e) {  
                       e.printStackTrace();  
                   }  
                     
               }  
               return line;  
           }  
       } catch (MalformedURLException e) {  
           e.printStackTrace();  
       } catch (IOException e) {  
           e.printStackTrace();  
       }  
       return line;  
    }

     
     /**
 	 * 获取外网的IP方式二
 	 * 
 	 * @Title: GetNetIp
 	 * @return String
 	 * @throws
 	 */
 	public static String GetNetIp() {
 		URL infoUrl = null;
 		InputStream inStream = null;
 		String ipLine = "";
 		HttpURLConnection httpConnection = null;
 		try {
 			infoUrl = new URL("http://ip168.com/");
 			URLConnection connection = infoUrl.openConnection();
 			httpConnection = (HttpURLConnection) connection;
 			int responseCode = httpConnection.getResponseCode();
 			if (responseCode == HttpURLConnection.HTTP_OK) {
 				inStream = httpConnection.getInputStream();
 				BufferedReader reader = new BufferedReader(
 						new InputStreamReader(inStream, "utf-8"));
 				StringBuilder strber = new StringBuilder();
 				String line = null;
 				while ((line = reader.readLine()) != null)
 					strber.append(line + "\n");

 				Pattern pattern = Pattern
 						.compile("((?:(?:25[0-5]|2[0-4]\\d|((1\\d{2})|([1-9]?\\d)))\\.){3}(?:25[0-5]|2[0-4]\\d|((1\\d{2})|([1-9]?\\d))))");
 				Matcher matcher = pattern.matcher(strber.toString());
 				if (matcher.find()) {
 					ipLine = matcher.group();
 				}
 			}

 		} catch (MalformedURLException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		} finally {
 			try {
 				inStream.close();
 				httpConnection.disconnect();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		return ipLine;
 	}
     
	/**
	 * 取cpu 信息
	 * 
	 * @return
	 */
	private String getCpuInfo() {
		String str1 = "/proc/cpuinfo";
		String str2 = "";
		String[] cpuInfo = { "", "" };
		String[] arrayOfString;
		String ret = null;
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
	 * @return void
	 */
	public static void initClientInfo() {
		getInstance();
	}
	/**
	 * 获取手机的mac地址，6.0以上通用
	 * huangchangguo 2017.6.26 
	 * @return
	 */
	private static String getNewMac() { 
		  try { 
		    List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces()); 
		    for (NetworkInterface nif : all) { 
		      if (!nif.getName().equalsIgnoreCase("wlan0")) continue; 
		  
		      byte[] macBytes = nif.getHardwareAddress(); 
		      if (macBytes == null) { 
		        return null; 
		      } 
		  
		      StringBuilder res1 = new StringBuilder(); 
		      for (byte b : macBytes) { 
		        res1.append(String.format("%02X:", b)); 
		      } 
		  
		      if (res1.length() > 0) { 
		        res1.deleteCharAt(res1.length() - 1); 
		      } 
		      return res1.toString(); 
		    } 
		  } catch (Exception ex) { 
		    ex.printStackTrace(); 
		  } 
		  return null; 
		
		}
	/**
	 * 获取手机的mac地址
	 * liukun 2017.7.13
	 * @return
	 */
	private static String getMac(){
		String macSerial = "";
	    try {
	      Process pp = Runtime.getRuntime().exec(
	          "cat /sys/class/net/wlan0/address");
	      InputStreamReader ir = new InputStreamReader(pp.getInputStream());
	      LineNumberReader input = new LineNumberReader(ir);

	      String line;
	      while ((line = input.readLine()) != null) {
	        macSerial += line.trim();
	      }

	      input.close();
	    } catch (IOException e) {
	      e.printStackTrace();
	    }

	    return macSerial;
	}
	
	public static byte[] getMacAddress() {  
        Enumeration<NetworkInterface> interfaces = null;  
        try {  
            interfaces = NetworkInterface.getNetworkInterfaces();  
        } catch (SocketException e1) {  
            // TODO Auto-generated catch block  
            e1.printStackTrace();  
        }  
        while (interfaces.hasMoreElements()) {  
            final NetworkInterface ni = interfaces.nextElement();  
            try {  
                if (ni.isLoopback() || ni.isPointToPoint() || ni.isVirtual())  
                    continue;  
            } catch (SocketException e) {  
                // TODO Auto-generated catch block  
                e.printStackTrace();  
            }  
            byte[] macAddress = null;  
            try {  
                macAddress = ni.getHardwareAddress();  
            } catch (SocketException e) {  
                // TODO Auto-generated catch block  
                e.printStackTrace();  
            }  
            if (macAddress != null && macAddress.length > 0)  
                return macAddress;  
        }  
        return null;  
    }  
	
	/** 
	 * 通过网络接口取 
	 * @return 
	 */
	public String getAndroidVersion() {
		return androidVersion;
	}
	

	public void setAndroidVersion(String androidVersion) {
		this.androidVersion = androidVersion;
	}

	public String getAppVersionCode() {
		return appVersionCode;
	}

	public void setAppVersionCode(String appVersionCode) {
		this.appVersionCode = appVersionCode;
	}

	public int getAppVersion() {
		return appVersion;
	}

	public void setAppVersion(int appVersion) {
		this.appVersion = appVersion;
	}

	public String getBrand() {
		return brand;
	}

	public void setBrand(String brand) {
		this.brand = brand;
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public String getImei() {
		return imei;
	}

	public void setImei(String imei) {
		this.imei = imei;
	}

	public long getClientStartTime() {
		return clientStartTime;
	}

	public void setClientStartTime(long clientStartTime) {
		this.clientStartTime = clientStartTime;
	}

	public String getImsi() {
		return imsi;
	}

	public void setImsi(String imsi) {
		this.imsi = imsi;
	}

	public String getOperator() {
		return operator;
	}

	public void setOperator(String operator) {
		this.operator = operator;
	}

	public String getChannelNo() {
		return channel;
	}

	public void setChannelNo(String channelNo) {
		this.channel = channelNo;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getSn() {
		return sn;
	}

	public void setSn(String sn) {
		this.sn = sn;
	}

	public int getNetStatus() {
		return netStatus;
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

}
