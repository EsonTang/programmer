package com.prize.left.page.util;

import java.io.File;
import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.DownloadManager;
import android.app.StatusBarManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.webkit.MimeTypeMap;

import com.android.launcher3.R;
import com.baidu.location.BDLocation;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.prize.left.page.bean.AddrBean;
import com.prize.left.page.bean.LocBean;
/***
 * 公用工具类
 * @author fanjunchen
 *
 */
public class CommonUtils {
	/***
	 * 改变状态栏反色
	 * @param mWindow
	 */
	public static void changeStatus(Window mWindow) {
		WindowManager.LayoutParams lp = mWindow.getAttributes();
		Field[] fields = LayoutParams.class.getDeclaredFields();
		boolean b = false;
		for (int i = 0; i < fields.length; i++) {// 暂时如此处理，防止在不同的手机crash
			if (fields[i].getName().equals("statusBarInverse")) {
				b = true;
				break;
			}
		}
		
		if (b) {
			lp.statusBarInverse = StatusBarManager.STATUS_BAR_INVERSE_GRAY;
			mWindow.setAttributes(lp);
		}
	}
	
	public static void changeStatusWhite(Window mWindow) {
		WindowManager.LayoutParams lp = mWindow.getAttributes();
		Field[] fields = LayoutParams.class.getDeclaredFields();
		boolean b = false;
		for (int i = 0; i < fields.length; i++) {// 暂时如此处理，防止在不同的手机crash
			if (fields[i].getName().equals("statusBarInverse")) {
				b = true;
				break;
			}
		}
		
		if (b) {
			lp.statusBarInverse = StatusBarManager.STATUS_BAR_INVERSE_WHITE;
			mWindow.setAttributes(lp);
		}
	}
	/***
	 * json to object&lt;T&gt;
	 * @param jsonString
	 * @param cls
	 * @return
	 */
	public static <T> T getObject(String jsonString, Class<T> cls) {
		T t = null;
		try {
			Gson gson = new Gson();
			t = gson.fromJson(jsonString, cls);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return t;
	}
	
	

	public static <T> String toGson(T cls) {
		String t = null;
		try {
			Gson gson = new Gson();
			t = gson.toJson(cls);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return t;
	}
	/***
	 * json to List&lt;T&gt;
	 * @param jsonString
	 * @param cls
	 * @return
	 */
	public static <T> List<T> getObjects(String jsonString, Class<T[]> cls) {
		List<T> list = null;
		try {
			/*Gson gson = new Gson();
			list = gson.fromJson(jsonString, new TypeToken<List<T>>() {}.getType());*/
			list = stringToArray(jsonString, cls);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}
	
	public static <T> List<T> stringToArray(String s, Class<T[]> clazz) {
        T[] arr = new Gson().fromJson(s, clazz);
        return Arrays.asList(arr);
    }
	/***
	 * json to List<Map<String, Object>>
	 * @param jsonString
	 * @return
	 */
	public static List<Map<String, Object>> listKeyMaps(String jsonString) {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		try {
			Gson gson = new Gson();
			list = gson.fromJson(jsonString,
					new TypeToken<List<Map<String, Object>>>() {
					}.getType());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}
	/***
	 * 获取某资源ID
	 * @param ctx
	 * @param type 资源类型 eg: string, drawable, layout
	 * @param strId
	 * @return
	 */
	public static int getResourceId(Context ctx, String type, String strId) {
		try {
			return ctx.getResources().getIdentifier(strId, type,
					ctx.getPackageName());
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
	}
	/***
	 * 获取loc的查询串
	 * @param loc
	 * @return
	 */
	public static String getLocQueryStr(BDLocation loc) {
		if (null == loc)
			return null;
		
		StringBuilder sb = new StringBuilder(64);
		sb.append(loc.getLatitude()).append(',').append(loc.getLongitude());
		
		String str = sb.toString();
		sb = null;
		
		return str;
	}
	/***
	 * 百度位置信息转换为addrBean
	 * @param loc
	 * @return
	 */
	public static AddrBean locToAddrBean(BDLocation loc) {
		AddrBean ad = null;
		if (loc != null) {
			ad = new AddrBean();
			ad.name = loc.getAddrStr();
			ad.district = loc.getDistrict();
			ad.city = loc.getCity();
			ad.cityid = loc.getCityCode();
			ad.location = new LocBean();
			ad.location.lat = loc.getLatitude();
			ad.location.lng = loc.getLongitude();
		}
		return ad;
	}
	/***
	 * 跳转致发短信界面
	 * @param ctx
	 * @param num
	 */
	public static void jumpToSms(Context ctx, String num) {
		Intent intent = new Intent();
		//系统默认的action，用来打开默认的短信界面
		intent.setAction(Intent.ACTION_SENDTO);
		//需要发短息的号码
		intent.setData(Uri.parse("smsto:" + num));
		ctx.startActivity(intent);
	}
	
	/***
	 * 跳转致拨号界面
	 * @param ctx
	 * @param num
	 */
	public static void jumpToPhone(Context ctx, String num) {
		Intent intent = new Intent();
		//系统默认的action，用来打开默认的短信界面
		intent.setAction(Intent.ACTION_DIAL);// ACTION_CALL
		//需要发短息的号码
		intent.setData(Uri.parse("tel:" + num));
		ctx.startActivity(intent);
	}
	
	/***
	 * 跳转致发短信界面
	 * @param ctx
	 * @param num
	 */
	public static void jumpToSms(Activity ctx, String num, int enterAnim) {
		Intent intent = new Intent();
		//系统默认的action，用来打开默认的短信界面
		intent.setAction(Intent.ACTION_SENDTO);
		//需要发短息的号码
		intent.setData(Uri.parse("smsto:" + num));
		ctx.startActivity(intent);
		ctx.overridePendingTransition(enterAnim, 0);
	}
	
	/***
	 * 跳转致打电话界面
	 * @param ctx
	 * @param num
	 */
	public static void jumpToCallPhone(Activity ctx, String num, int enterAnim) {
		Intent intent = new Intent();
		//系统默认的action，用来打开默认的短信界面
		intent.setAction(Intent.ACTION_CALL);// ACTION_CALL
		//需要发短息的号码
		intent.setData(Uri.parse("tel:" + num));
		ctx.startActivity(intent);
		ctx.overridePendingTransition(enterAnim, 0);
	}
	
	/***
	 * 跳转致某个联系人界面
	 * @param ctx
	 * @param num
	 */
	public static void jumpToPeople(Activity ctx, String num, int enterAnim) {
		Intent intent = new Intent();
		//系统默认的action，用来打开默认的短信界面
		intent.setAction(Intent.ACTION_VIEW);// ACTION_CALL
		//需要发短息的号码
		intent.setData(Uri.parse("tel:" + num));
		ctx.startActivity(intent);
		ctx.overridePendingTransition(enterAnim, 0);
	}
	
	private static long mDownloadId = 0;
	/**
	 * 应用下载
	 * @param downloadManager
	 * @param url
	 * @param context
	 */
	public static void downloadApk(DownloadManager downloadManager,
			String url, Context context) {
		File folder = Environment
				.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
		if (!folder.exists() || !folder.isDirectory()) {
			folder.mkdirs();
		}
		if (downloadManager == null) {
			downloadManager = (DownloadManager) context
					.getSystemService(Context.DOWNLOAD_SERVICE);
		}
		long saveId = PreferencesUtils.getLong(context,
				IConstants.KEY_NAME_DOWNLOAD_ID);
		if (mDownloadId == saveId) {
			return;
		}
		DownloadManager.Request request = new DownloadManager.Request(
				Uri.parse(url));
		request.setDestinationInExternalPublicDir(
				Environment.DIRECTORY_DOWNLOADS, IConstants.APK_FILE_NAME);
		// 设置文件类型
		MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
		String mimeString = mimeTypeMap.getMimeTypeFromExtension(MimeTypeMap
				.getFileExtensionFromUrl(url));
		request.setMimeType(mimeString);
		// 在通知栏中是否显示
		request.setShowRunningNotification(false);
		
		request.setVisibleInDownloadsUi(true);
		mDownloadId = downloadManager.enqueue(request);
		/** save download id to preferences **/
		PreferencesUtils.putLong(context, IConstants.KEY_NAME_DOWNLOAD_ID,
				mDownloadId);
	}
	
	/**
	 * 应用下载
	 * @param downloadManager
	 * @param url
	 * @param context
	 * @param verCode
	 */
	public static void downloadApk(DownloadManager downloadManager,
			String url, Context context, int code) {
		
		int tCode = PreferencesUtils.getInt(context,
				IConstants.KEY_DOWNLOAD_CODE);
		
		if (tCode != code) {
			mDownloadId = 0;
		}
		int localVersion = ClientInfo.getInstance(context).appVersion;
		if(localVersion==code) {
			return;
		}
		
		File folder = Environment
				.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
		if (!folder.exists() || !folder.isDirectory()) {
			folder.mkdirs();
		}
		if (downloadManager == null) {
			downloadManager = (DownloadManager) context
					.getSystemService(Context.DOWNLOAD_SERVICE);
		}
		long saveId = PreferencesUtils.getLong(context,
				IConstants.KEY_NAME_DOWNLOAD_ID);
		if (mDownloadId == saveId&&mDownloadId!=0) {
			return;
		}
		
		DownloadManager.Request request = new DownloadManager.Request(
				Uri.parse(url));
		request.setDestinationInExternalPublicDir(
				Environment.DIRECTORY_DOWNLOADS, IConstants.APK_FILE_NAME);
		// 设置文件类型
		MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
		String mimeString = mimeTypeMap.getMimeTypeFromExtension(MimeTypeMap
				.getFileExtensionFromUrl(url));
		request.setMimeType(mimeString);
		// 在通知栏中是否显示
		request.setShowRunningNotification(false);
		
		request.setVisibleInDownloadsUi(true);
		mDownloadId = downloadManager.enqueue(request);
		/** save download id to preferences **/
		PreferencesUtils.putLong(context, IConstants.KEY_NAME_DOWNLOAD_ID,
				mDownloadId);
		
		PreferencesUtils.putInt(context, IConstants.KEY_DOWNLOAD_CODE,
				code);
	}
	
	public static final int MB_2_BYTE = 1024 * 1024;
	
	public static final int KB_2_BYTE = 1024;
	
	static final DecimalFormat DOUBLE_DECIMAL_FORMAT = new DecimalFormat("0.##");
	/**
	 * @param size
	 * @return
	 */
	public static CharSequence getAppSize(long size) {
		if (size <= 0) {
			return "0M";
		}

		if (size >= MB_2_BYTE) {
			return new StringBuilder(16).append(
					DOUBLE_DECIMAL_FORMAT.format((double) size / MB_2_BYTE))
					.append("M");
		} else if (size >= KB_2_BYTE) {
			return new StringBuilder(16).append(
					DOUBLE_DECIMAL_FORMAT.format((double) size / KB_2_BYTE))
					.append("K");
		} else {
			return size + "B";
		}
	}

	public static String getNotiPercent(long progress, long max) {
		int rate = 0;
		if (progress <= 0 || max <= 0) {
			rate = 0;
		} else if (progress > max) {
			rate = 100;
		} else {
			rate = (int) ((double) progress / max * 100);
		}
		return new StringBuilder(16).append(rate).append("%").toString();
	}

	public static boolean isDownloading(int status) {
		return status == DownloadManager.STATUS_RUNNING
				|| status == DownloadManager.STATUS_PAUSED
				|| status == DownloadManager.STATUS_PENDING;
	}
	
	/**
	 * 全角转化为半角的方法
	 * @param input
	 * @return
	 */
	public static String ToDBC(String input) {
		char[] c = input.toCharArray();
		for (int i = 0; i < c.length; i++) {
			if (isChinese(c[i])) {
				if (c[i] == 12288) {
					c[i] = (char) 32;
					continue;
				}
				if (c[i] > 65280 && c[i] < 65375)
					c[i] = (char) (c[i] - 65248);
			}
		}
		return new String(c);
	}
	/***
	 * 判断一个字符是否为中文
	 * @param c
	 * @return
	 */
	public static boolean isChinese(char c) {
		Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
		if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
				|| ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
				|| ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
				|| ub == Character.UnicodeBlock.GENERAL_PUNCTUATION
				|| ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
				|| ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS) {
			return true;
		}
		return false;
	}
	/**格式化价格*/
	private static DecimalFormat priceFormat = new DecimalFormat("￥#.0");
	/***
	 * 格式化价格串
	 * @param f
	 * @return
	 */
	public static String formatPrice(float f) {
		return priceFormat.format(f/100);
	}
	/**格式化评分*/
	private static DecimalFormat gradFormat = new DecimalFormat("#.#");
	/***
	 * 格式化评分
	 * @param f
	 * @return
	 */
	public static String formatGrad(float f) {
		return gradFormat.format(f);
	}
	/**格式化评分*/
	private static DecimalFormat distanceFormat = new DecimalFormat("#.##");
	/***
	 * 
	 * @param f
	 * @return
	 */
	public static String formatDistance(float f) {
		if (f > 1000) {
			return distanceFormat.format(f/1000) + "km";
		}
		else if (f > 0){
			return f + "m";
		}
		else 
			return "";
	}
}
