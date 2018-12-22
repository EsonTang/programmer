/**
 * 
 */
package com.android.prize.salesstatis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import com.google.gson.Gson;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StatFs;
import android.os.SystemProperties;
import android.telephony.TelephonyManager;
import android.text.format.Formatter;
import android.util.DisplayMetrics;
import android.util.Log;


/**
 * 终端信息
 * 
 */
public class ClientInfo {
	private static final String TAG = "PrizeSalesStatis";
	public final static int NONET = 0;
	public final static int MOBILE_3G = 1;
	public final static int MOBILE_2G = 2;
//	public final static int CMNET = 1;
//	public final static int CMWAP = 2;
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

	// 渠道号文件名，我们将渠道号写到assert目录下该文件内
	private static final String CHANNELCODE_FILENAME = "channelcode";

	private static ClientInfo instance;

	// 安卓系统版本号
	public String androidVer = null;      
	// cpu型号
	public String cpu = null;     
	// 厂商
	public String fact = null;  
	// 机型
	public String model = null;  
	//手机版本号
	public String mver = null;  
	// imei
	public String imei = null;  
	// imsi
	public String imsi = null;   
	// 运营提供商 
	public String provider = null;  
	// 网络状态， 网络状态会不停变化，故设置成static，需实时更新
	public static byte networkType;    
	// 渠道code
	public String channelCode = "jolo";  
	// ram大小
	public int ramSize = 0;    
	// rom大小
	public int romSize = 0;  
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
	//语言
	public String localLanguage=null;   
	//国家码
	public String country=null;  
	//地区经纬度
	public String position=null;  
	//项目
	public String project=null;    
	//手机号码，可能获取不到
	public String mobile=null; 
	
	//纬度
	public Double  latitude = 0.0;
	//经度
	public Double  longitude = 0.0;
	
	//SN
	public String sn=null; 
	
	public Context mContext;
	
	public ClientInfo(Context context) {
		mContext = context;
		//androidVer = Build.SYSTEM_VERSION;
        androidVer = SystemProperties.get("ro.prize_customer");
        if(androidVer == null){
            androidVer = "Prize";
        }
		// 获取packagemanager的实例
		PackageManager packageManager = context.getPackageManager();
		try {	
			project=getProjectName();
			cpu = getCpuInfo();
			
			ramSize = getTotalMemory(context);
			romSize = (int) ((getTotalInternalMemorySize() / 1024) / 1024);
			
			WifiManager wifiManager = (WifiManager) context
					.getSystemService(Context.WIFI_SERVICE);
			WifiInfo wifiInfo = wifiManager.getConnectionInfo();
			mac = wifiInfo.getMacAddress();
			Locale locale = context.getResources().getConfiguration().locale;
			localLanguage = locale.getLanguage();//获取语言
			country = locale.getCountry();//获取国家码
			
			fact = Build.MANUFACTURER;// 手机厂商
			model = Build.MODEL;// 手机型号
			mver = Build.DISPLAY;//手机版本号
			sn = Build.SERIAL;//SN
			
			//channelCode = getchannelCode(context, CHANNELCODE_FILENAME);
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
			sdCardSize = getSDCardMemory(context);
			
			TelephonyManager telephonyManager = (TelephonyManager) context
					.getSystemService(Context.TELEPHONY_SERVICE);
			imsi = telephonyManager.getSubscriberId();
			imei = getImei();//telephonyManager.getDeviceId();
			mobile = telephonyManager.getLine1Number();
			provider = getProvider(imsi);
			
			networkType = (byte) getAPNType(context);

		} catch (Exception e) {
		}finally{
			checkInfo();
		}	
	}
	
	private String getProjectName(){
		String systemVersion = Build.DISPLAY;
		Log.v(TAG, "[ClientInfo]--Version-----> " + systemVersion);
		if(systemVersion != null && !systemVersion.equals("")){
			String[] trim = systemVersion.split("\\.");
			String ret = trim[0];
			return ret;
		}
		return null;
	}
	
	public String getImei(){
		String curImei = "";
		CTelephoneInfo telephonyInfo = CTelephoneInfo.getInstance(mContext);
		Log.v(TAG, "------>start");
		telephonyInfo.setPhoneInfo();
		Log.v(TAG, "------>end");
		String imeiSIM1 = telephonyInfo.getImeiSIM1();
		String imeiSIM2 = telephonyInfo.getImeiSIM2();
		String meidSIM1 = telephonyInfo.getMeidSIM1();
		String meidSIM2 = telephonyInfo.getMeidSIM2();
		if (imeiSIM1 != null) {
			curImei = imeiSIM1;
			if(meidSIM2!=null){
				curImei = curImei + "," + meidSIM2;
			}else if(imeiSIM2!=null){
				curImei = curImei + "," + imeiSIM2;
			}
		}else if(meidSIM1!=null){
			curImei = meidSIM1;
			if(imeiSIM2!=null){
				curImei = curImei + "," + imeiSIM2;
			}else if(meidSIM2!=null){
				curImei = curImei + "," + meidSIM2;
			}
		}
		Log.v(TAG, "[ClientInfo]--curImei----> " + curImei);
		return curImei;
	}
	


	private String getchannelCode(Context context, String fileName) {
		String result = "joloplay";
		InputStreamReader inputReader = null;
		BufferedReader bufReader = null;
		try {
			inputReader = new InputStreamReader(context.getResources()
					.getAssets().open(fileName));
			bufReader = new BufferedReader(inputReader);
			String line = "";
			String Result = "";
			while ((line = bufReader.readLine()) != null) {
				Result += line;
			}
			result = Result;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (null != inputReader) {
				try {
					inputReader.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			if (null != bufReader) {
				try {
					bufReader.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return result;
	}

	/**会报这个StatFs sf = new StatFs(sdcardDir.getPath());<br>
	 * java.lang.IllegalArgumentException异常*/
	private String getSDCardMemory(Context context) {
		String ret = "0";
		try {
			String state = Environment.getExternalStorageState();
			if (Environment.MEDIA_MOUNTED.equals(state)) {
				 File path = Environment.getExternalStorageDirectory();  
			     StatFs stat = new StatFs(path.getPath());  
			     long blockSize = stat.getBlockSize();  
			     long totalBlocks = stat.getBlockCount();  
			     long availBlocks = stat.getAvailableBlocks();
			     ret= Formatter.formatFileSize(context, blockSize * totalBlocks);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		  
		return ret;
	}

	/**
	 * 取Rom Size
	 * 
	 * @return
	 */
	private long getTotalInternalMemorySize() {
		long romSize=0;
		long blockSize=0;
		long totalBlocks=0;
		try {
			File path = Environment.getDataDirectory();
			StatFs stat = new StatFs(path.getPath());
			blockSize = stat.getBlockSize();
			totalBlocks = stat.getBlockCount();
			romSize=totalBlocks * blockSize;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return romSize;
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
		return initial_memory / 1024; // MB
	}

	private static int check2GOr3GNet(Context context){

		int mobileNetType = NONET;
		if (null == context) {
			return mobileNetType;
		}
		TelephonyManager telMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE); 
		
		int netWorkType = telMgr.getNetworkType();
		//JLog.i("net", "net is "+netWorkType);
		switch (netWorkType) {		
		case TelephonyManager.NETWORK_TYPE_UMTS:
		case TelephonyManager.NETWORK_TYPE_HSDPA:
		case TelephonyManager.NETWORK_TYPE_HSPA:
		case TelephonyManager.NETWORK_TYPE_HSUPA:
		case TelephonyManager.NETWORK_TYPE_EVDO_0:
		case TelephonyManager.NETWORK_TYPE_EVDO_A:
//		case TelephonyManager.NETWORK_TYPE_EVDO_B:
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
		networkType = (byte) netType;
		//JLog.info("networkType is "+networkType);
		return netType;
	}

	/**
	 * 取cpu 信息
	 * 
	 * @return
	 */
	private String getCpuInfo() {
		String str1 = "/proc/cpuinfo";
		String str2 = "";
		String[] arrayOfString;
		String ret = UNKNOWN;
		BufferedReader localBufferedReader = null;
		try {
			FileReader fr = new FileReader(str1);
			localBufferedReader = new BufferedReader(fr, 8192);
			List<String> mList = new ArrayList<String>();
			while((str2=localBufferedReader.readLine()) != null){
				mList.add(str2);
			}
			str2=mList.get(mList.size()-1);
			if (null != str2) {
				arrayOfString = str2.split("\\s+");
				if(arrayOfString[2] != null){
					ret = arrayOfString[2];
				}		
			}		
		} catch (Exception e) {
		}finally{
			try {
				if(localBufferedReader != null){
					localBufferedReader.close();
				}	
			} catch (IOException e) {
				e.printStackTrace();
				
			}
		}
		return ret;
	}
	
	public void setPosition(String position) {
		this.position = position;
	}
	
	public String getPosition() {
		return position;
	}
	
	public Double getLatitude() {
		return latitude;
	}

	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}

	public Double getLongitude() {
		return longitude;
	}

	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}

	public static ClientInfo getInstance(Context context) {
		if (null == instance) {
			instance = new ClientInfo(context);
		}
		return instance;
	}

	/**
	 * 初始化
	 */
	public static void initClientInfo(Context context){
		getInstance(context);
	}

	@Override
	public String toString() {
		return "ClientInfo [ androidVer="
				+ androidVer + ",mver=" + mver  + ", cpu=" + cpu + ", fact=" + fact
				+ ", model=" + model + ", imei=" + imei + ", imsi=" + imsi
				+ ", provider=" + provider + ", channelCode=" + channelCode
				+ ", ramSize=" + ramSize + ", romSize=" + romSize
				+ ", screenSize=" + screenSize + ", screenWidth=" + screenWidth
				+ ", screenHeight=" + screenHeight + ", dpi=" + dpi + ", mac="
				+ mac + ", sdCardSize=" + sdCardSize + ", localLanguage="
				+ localLanguage + ", country=" + country + ", position="+ position+
				", project=" +project+", mobile="+ mobile+", sn="+ sn +
				", longitude="+ longitude +", latitude="+ latitude +"]";
	}
	
	public String getJson(){
		Map<String,Object> map=new TreeMap<String,Object>();
		map.put("type", 2);
		map.put("cpu",cpu);  //cpu
		map.put("fact", fact);   //厂商
		map.put("model", model);   //机型
		map.put("mver", mver);   //手机版本号
		/*if(imei==null||imei.equals("")){
			return null;
		}*/
		map.put("imei", imei);
		map.put("imsi", imsi);
		map.put("ver", androidVer);   //android版本号
		map.put("net", provider);   //运营供应商
		map.put("status", networkType);  //网络状态
		map.put("rom", romSize);   
		map.put("ram", ramSize);
		map.put("sd", sdCardSize);   //sd卡		
		map.put("screen", screenSize);   //屏幕
		map.put("mac", mac);
		map.put("language", localLanguage);  //语言
		map.put("country", country);     
		map.put("position", position);   //位置
		map.put("project", project);    //项目
		map.put("mobile", mobile);   //手机号码
		map.put("sn", sn);   //sn
		map.put("longitude", longitude);   //sn
		map.put("latitude", latitude);   //sn
		Gson gson=new Gson();
        String json=gson.toJson(map);	
		return json;
	}
	/**
	 * 检查所有信息
	 */
	public void checkInfo(){
		if (null == imei) {
			imei = UNKNOWN;
		}
		if (null == imsi) {
			imsi = UNKNOWN;
		}
		if("".equals(mobile)){
			mobile = UNKNOWN;
		}
		if(null == sdCardSize){
			sdCardSize = UNKNOWN;
		}
		if(null == fact){
			fact = UNKNOWN;
		}
		if(null == model){
			model = UNKNOWN;
		}
		if(null == mver){
			mver = UNKNOWN;
		}
		if(null == androidVer){
			androidVer = UNKNOWN;
		}
		if(null == localLanguage){
			localLanguage = UNKNOWN;
		}
		if(null == country){
			country = UNKNOWN;
		}
		if(null == position){
			position = UNKNOWN;
		}
		if(null == project){
			project = UNKNOWN;
		}
		if(null == screenSize){
			screenSize = UNKNOWN;
		}
		if(null == cpu){
			cpu = UNKNOWN;
		}
		if(null == provider){
			provider = UNKNOWN;
		}
		if (mac == null) {
			mac = UNKNOWN;
		}
		if (sn == null) {
			sn = UNKNOWN;
		}
	}
}
