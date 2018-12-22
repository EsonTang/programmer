package com.prize.prizeappoutad.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.Log;
import android.view.WindowManager;

public class BaseUtils {
	public final static int NONET = 0;
	public final static int MOBILE_3G = 1;
	public final static int MOBILE_2G = 2;
	public final static int MOBILE_4G = 4;
	// public final static int CMNET = 1;
	// public final static int CMWAP = 2;
	public final static int WIFI = 3;

    public static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }
    public static boolean isEmptyList(List list) {
        return list == null || list.size() ==0;
    }

    public static boolean existSDcard() {
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()))
            return true;
        return false;
    }
    /**
     * network connect ok
     *
     * @param context
     * @return
     */
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) {
            return false;
        }

        NetworkInfo[] info = null;
        try{
            info = cm.getAllNetworkInfo();
        } catch (SecurityException e) {

        }
        if (info != null) {
            for (int i = 0; i < info.length; i++) {
                if (info[i] != null) {
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    /**
     * md5加密方法
     * @param password
     * @return
     */
    public static String md5Password(String password) {

        try {
            // 得到一个信息摘要器
            MessageDigest digest = MessageDigest.getInstance("md5");
            byte[] result = digest.digest(password.getBytes());
            StringBuffer buffer = new StringBuffer();
            // 把没一个byte 做一个与运算 0xff;
            for (byte b : result) {
                // 与运算
                int number = b & 0xff;// 加盐
                String str = Integer.toHexString(number);
                if (str.length() == 1) {
                    buffer.append("0");
                }
                buffer.append(str);
            }

            // 标准的md5加密后的结果
            return buffer.toString();
        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return "";
        }

    }
/*
    public static DisplayImageOptions getAdmobOptions() {

        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.yy_logo) //设置图片在下载期间显示的图片
                .showImageForEmptyUri(R.drawable.yy_logo)//设置图片Uri为空或是错误的时候显示的图片
                .showImageOnFail(R.drawable.yy_logo)  //设置图片加载/解码过程中错误时候显示的图片
                .cacheInMemory()//设置下载的图片是否缓存在内存中
                .cacheOnDisk(true)//设置下载的图片是否缓存在SD卡中
//                .considerExifParams(true)  //是否考虑JPEG图像EXIF参数（旋转，翻转）
                .imageScaleType(ImageScaleType.IN_SAMPLE_INT)//设置图片以如何的编码方式显示
                .bitmapConfig(Bitmap.Config.RGB_565)//设置图片的解码类型
                        //.decodingOptions(BitmapFactory.Options decodingOptions)//设置图片的解码配置
                .delayBeforeLoading(0)//int delayInMillis为你设置的下载前的延迟时间
                        //设置图片加入缓存前，对bitmap进行设置
                        //.preProcessor(BitmapProcessor preProcessor)
                .resetViewBeforeLoading()//设置图片在下载前是否重置，复位
                .displayer(new FadeInBitmapDisplayer(1000))//是否图片加载好后渐入的动画时间，可能会出现闪动
                .build();//构建完成

        return options;
    }*/
    /**
     * 判断两个日期是否为同一天
     *
     * @param curDate
     * @param lastDate
     * @return
     */
    public  static boolean isSameDay(Date curDate, Date lastDate) {
        String DATE_FORMAT = "yyyy-MM-dd";
        String curDateStr = "";
        String lastDateStr = "";
        SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT);
        curDateStr = format.format(curDate);
        lastDateStr = format.format(lastDate);
        return curDateStr.equals(lastDateStr);
    }
    
    
    public static int getAppIconId(Context context)
    {
    	String packageName = context.getPackageName();
      try
      {
        if (packageName == null)
          return 0;
        Intent localIntent = new Intent("android.intent.action.MAIN");
        localIntent.addCategory("android.intent.category.LAUNCHER");
        PackageManager localPackageManager = context.getPackageManager();
        List localList = localPackageManager.queryIntentActivities(localIntent, 1);
        if (localList != null)
          for (int i = 0; i < localList.size(); i++)
            try
            {
              ResolveInfo localResolveInfo = (ResolveInfo)localList.get(i);
              if (localResolveInfo != null)
              {
                if (!localResolveInfo.activityInfo.packageName.equals(packageName))
                  continue;
                int iconId = localResolveInfo.activityInfo.applicationInfo.icon;
                return iconId;
              }
            }
            catch (Throwable localThrowable2)
            {
            }
      }
      catch (Throwable localThrowable1)
      {
      }
      return 0;
    }
    
    /**  
     * 图片转成string  
     *   
     * @param bitmap  
     * @return  
     */  
    public static String bitmapToString(Bitmap bitmap)  
    {  
        ByteArrayOutputStream baos = new ByteArrayOutputStream();// outputstream  
        bitmap.compress(CompressFormat.PNG, 100, baos);  
        byte[] appicon = baos.toByteArray();// 转为byte数组  
        return Base64.encodeToString(appicon, Base64.DEFAULT);
  
    }  
    
    public static String byte2hex(byte[] b) // 二进制转字符串  
    {  
       StringBuffer sb = new StringBuffer();  
       String stmp = "";  
       for (int n = 0; n < b.length; n++) {  
        stmp = Integer.toHexString(b[n] & 0XFF);  
        if (stmp.length() == 1){  
            sb.append("0" + stmp);  
        }else{  
            sb.append(stmp);  
        }  
          
       }  
       return sb.toString();  
    } 
  
    /**  
     * string转成bitmap  
     *   
     * @param st  
     */ 
  public static synchronized Bitmap StringToBitmap(String paramString)
  {
    try
    {
      if (paramString == null)
        return null;
      byte[] arrayOfByte = Base64.decode(paramString, 0);
      if (arrayOfByte == null)
        return null;
      return BitmapFactory.decodeByteArray(arrayOfByte, 0, arrayOfByte.length);
    }
    catch (Throwable localThrowable)
    {
    }
    return null;
  }
  
    public static int dip2px(Context context, float dipValue) {
	    float scale = context.getResources().getDisplayMetrics().density;
	    return (int) (dipValue * scale + 0.5f);
    }


	public static int px2dip(Context context, float pxValue) {
	    float scale = context.getResources().getDisplayMetrics().density;
	    return (int) (pxValue / scale + 0.5f);
	}
	
	 private Bitmap Bytes2Bimap(byte[] b){
         if(b.length!=0){
             return BitmapFactory.decodeByteArray(b, 0, b.length);
         }
         else {
             return null;
         }

	 }
	 
	 public static void writeToFile(String out, String fileName){
		 out = out.replace("\n", "\"+\n\"\\n");
		 File file = new File(Environment.getExternalStorageDirectory().getAbsoluteFile(), fileName);
		 if(!file.exists()){
			 try {
				file.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		 }
		 
		 ByteArrayInputStream bai = new ByteArrayInputStream(out.getBytes());
		 
		 try {    
	            FileOutputStream fos = new FileOutputStream(file);  
	            Writer os = new OutputStreamWriter(fos, "GBK");  
	            os.write(out);  
	            os.flush();  
	            fos.close();  
	        } catch (FileNotFoundException e) {    
	            // TODO Auto-generated catch block     
	            e.printStackTrace();    
	        } catch (IOException e) {    
	            // TODO Auto-generated catch block     
	            e.printStackTrace();    
	        }    
	 }
	 
	 public static int getDisplayWidth(Context context){
		 WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

		  int width = wm.getDefaultDisplay().getWidth();
		  int height = wm.getDefaultDisplay().getHeight();
		  
		  return width;
	 }
	 
	 /***
	 *  获取当前网络类型
	 * @param context
	 * @return
	 */
	public static int getAPNType(Context context) {
		int netType = NONET;

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
		return netType;
	}
	
	private static int check2GOr3GNet(Context context) {

		int mobileNetType = NONET;
		if (null == context) {
			return mobileNetType;
		}
		TelephonyManager telMgr = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);

		int netWorkType = telMgr.getNetworkType();
		Log.e("NLG0606","netWorkType = "+netWorkType);
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
		 case TelephonyManager.NETWORK_TYPE_LTE:    //api<11 : replace by 13
			 mobileNetType = MOBILE_4G;
             break;
		default:
			mobileNetType = MOBILE_3G;
			break;
		}

		return mobileNetType;

	}
	
	public static String[] getYoumiIDAndKey(Context context){
    	String[] result = new String[2];
    	ApplicationInfo appInfo;
		try {
			appInfo = context.getPackageManager()
                    .getApplicationInfo(context.getPackageName(),
                    		PackageManager.GET_META_DATA);
	    	result[0] =appInfo.metaData.getString("YOUMI_ACCESS_ID");
	    	result[1] =appInfo.metaData.getString("YOUMI_ACCESS_KEY");
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
    }

	/*public static void initLocation(LocationClient locationClient){
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationMode.Hight_Accuracy);//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setCoorType("bd09ll");//可选，默认gcj02，设置返回的定位结果坐标系
        option.setOpenGps(true);//可选，默认false,设置是否使用gps
        option.setIsNeedAddress(true);//可选，设置是否需要地址信息，默认不需要
        int span=1000;
        option.setScanSpan(span);//可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        option.setLocationNotify(true);//可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
        option.setIsNeedLocationDescribe(true);//可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
        option.setIsNeedLocationPoiList(true);//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        option.setIgnoreKillProcess(false);//可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死  
        option.SetIgnoreCacheException(false);//可选，默认false，设置是否收集CRASH信息，默认收集
        option.setEnableSimulateGps(false);//可选，默认false，设置是否需要过滤gps仿真结果，默认需要
                         
        locationClient.setLocOption(option);
    }*/
	//TODO
	
	public static String md5(String paramString)
	  {
	    if (paramString != null)
	      try
	      {
	        MessageDigest localMessageDigest = MessageDigest.getInstance("MD5");
	        localMessageDigest.update(paramString.getBytes());
	        String str;
	        for (paramString = new BigInteger(1, localMessageDigest.digest()).toString(16); ; paramString = str)
	        {
	          if (paramString.length() >= 32)
	            return paramString;
	          str = "0" + paramString;
	        }
	      }
	      catch (Exception localException)
	      {
	       Log.e("RSplashActivity", "md5加密出错" + localException.getMessage(), null);
	      }
	    return paramString;
	  }

	  public static String md5(byte[] paramArrayOfByte)
	  {
	    Object localObject = "";
	    if (paramArrayOfByte != null)
	      try
	      {
	        MessageDigest localMessageDigest = MessageDigest.getInstance("MD5");
	        localMessageDigest.update(paramArrayOfByte);
	        String str;
	        for (localObject = new BigInteger(1, localMessageDigest.digest()).toString(16); ; localObject = str)
	        {
	          if (((String)localObject).length() >= 32){
	            return (String) localObject;
	          }
	          str = "0" + (String)localObject;
	        }
	      }
	      catch (Exception localException)
	      {
	       Log.e("RSplashActivity", "md5加密出错" + localException.getMessage());
	      }
	    return (String) localObject;
	  }
		
}
