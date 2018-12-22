package com.prize.music.admanager.presenter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.xutils.x;
import org.xutils.common.Callback.CommonCallback;
import org.xutils.http.RequestParams;

import com.prize.app.beans.ClientInfo;
import com.prize.app.constants.Constants;
import com.prize.app.net.datasource.base.AppsItemBean;
import com.prize.music.admanager.Configs;
import com.prize.music.admanager.view.WebViewActivity;
import com.prize.music.helpers.utils.PreferencesUtils;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.FileCallBack;
import com.zhy.http.okhttp.callback.StringCallback;

import android.app.ActivityManager;
import android.app.ActivityManager.RecentTaskInfo;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.DownloadManager.Request;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.os.SystemProperties;
import android.util.Log;
import okhttp3.Call;
import okhttp3.MediaType;

/**
 * * 升级、得到包名工具类
 *
 * @author huangchangguo
 * @version V1.0
 */
public class AdNetManager {
	private static final String TAG = "huang-AdNetManager";
	private AdNetManager mInstance;
	private static int i = 0;

	public AdNetManager getInstance() {
		if (mInstance == null) {
			synchronized (AdNetManager.class) {
				if (mInstance == null) {
					mInstance = new AdNetManager();
				}
			}
		}
		return mInstance;
	}

	/**
	 * 判断是否要更新版本(getPackageArchiveInfo) 根据versionCode来判断
	 *
	 * @param packageName
	 * @param versionCode
	 * @return
	 */

	public static boolean appIsNeedUpate(String packageName, int versionCode, Context context) {

		try {
			ApplicationInfo applicationInfo = context.getPackageManager().getApplicationInfo(packageName, 0);
			if (!isNewMethod()) {
				JLog.i(TAG, "this.versionCode: " + applicationInfo.versionCode + "  versionCode: " + versionCode);
				return applicationInfo.versionCode < versionCode;
			}
			PackageInfo packageInfo = context.getPackageManager().getPackageArchiveInfo(applicationInfo.publicSourceDir,
					0);
			if (packageInfo != null) {
				JLog.i(TAG, "this.versionCode: " + packageInfo.versionCode + "  versionCode: " + versionCode);
				return packageInfo.versionCode < versionCode;
			}
		} catch (NameNotFoundException e) {
			return false;
		}
		return false;
	}

	public static boolean isNewMethod() {
		JLog.i("huang-SelfUpdataUtils", "isNewMethod-=" + SystemProperties.get("ro.fota.version", "0"));
		JLog.i("huang-SelfUpdataUtils",
				"isNewMethod-prize_=" + SystemProperties.get("ro.prize_app_update_appcenter", "0"));
		// return "1".equals(SystemProperties.get("ro.fota.version", "0"));
		return "1".equals(SystemProperties.get("ro.prize_app_update_appcenter", "0"));
	}

	public static boolean isWiFiActive(Context inContext) {
		Context context = inContext.getApplicationContext();
		ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivity != null) {
			NetworkInfo[] info = connectivity.getAllNetworkInfo();
			if (info != null) {
				for (int i = 0; i < info.length; i++) {
					if (info[i].getTypeName().equals("WIFI") && info[i].isConnected()) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public interface setOnDownloadedLinstener {
		void downloadedLinstener(String path);
	}

	public static synchronized void downloadApk(Context context, String downloadurl, String apkName,
			setOnDownloadedLinstener linstener) {
		if (downloadurl == null || ClientInfo.networkType == ClientInfo.NONET) {
			JLog.i(TAG, "downloadApk-downloadurl:" + downloadurl);
			return;
		}
		// 下载APK
		String apkpath = Configs.APK_FILE_PATH;
		String downloadFileName = getApkName(apkName);
		downloadApk(downloadurl, apkpath, downloadFileName, linstener);
	}

	public static void downloadApk(String downloadurl, String destFileDir, String destFileName,
			final setOnDownloadedLinstener linstener) {
		if (downloadurl == null && destFileDir == null && destFileName == null) {
			return;
		}
		OkHttpUtils.get().url(downloadurl).build().execute(new FileCallBack(destFileDir, destFileName) {

			@Override
			public void onResponse(File arg0, int arg1) {
				String absolutePath = arg0.getAbsolutePath();
				JLog.i(TAG, "OkHttpUtils-onResponse" + absolutePath);
				linstener.downloadedLinstener(absolutePath);
				// installSilence(absolutePath);
			}

			@Override
			public void onError(Call arg0, Exception arg1, int arg2) {
				JLog.i(TAG, "OkHttpUtils-onError" + arg1.getMessage());
				linstener.downloadedLinstener(null);
			}

		});
	}

	/**
	 * @return The lists of the RunningApps
	 */
	@SuppressWarnings("deprecation")
	public Boolean isRecentRunningApp(Context context, String NativePcgName) {

		// final PackageManager pm = context.getPackageManager();
		ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		// runningProcess = am.getRunningAppProcesses();
		// List<RunningTaskInfo> runningTasks = am.getRunningTasks(50);
		List<RecentTaskInfo> recentTasks = am.getRecentTasks(25, ActivityManager.RECENT_IGNORE_UNAVAILABLE);
		JLog.i(TAG, "recentTasks:" + recentTasks.size());
		for (RecentTaskInfo recentTaskInfo : recentTasks) {
			try {
				String name = recentTaskInfo.description.getClass().getPackage().getName();

				String packageName = recentTaskInfo.baseIntent.getComponent().getPackageName();
				JLog.i(TAG, "recentTasks:-origActivity:" + " packageName:" + packageName);
				if (packageName.equals(NativePcgName)) {
					// return true;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return false;
	}

	/**
	 * @return The lists of the RunningApps
	 */
	@SuppressWarnings("deprecation")
	public static void isRecentTasks(Context context, String NativePcgName) {

		// final PackageManager pm = context.getPackageManager();
		ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningAppProcessInfo> runningAP = am.getRunningAppProcesses();
		List<RunningTaskInfo> runningTasks = am.getRunningTasks(0);
		JLog.i(TAG, "recentTasks:" + runningAP.size());
		for (RunningAppProcessInfo recentTaskInfo : runningAP) {
			try {
				String name = recentTaskInfo.processName;
				JLog.i(TAG, "processName: " + name);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	// 获得apk的路径
	public static String getFilePath(String apkName) {
		String apkName_ = apkName + Configs.ANDROID_APP_SUFFIX;
		if (apkName == null)
			return apkName;
		return new StringBuilder(Environment.getExternalStorageDirectory().getAbsolutePath()).append(File.separator)
				.append(Configs.DOWNLOAD_FOLDER_NAME).append(File.separator).append(Configs.PRIZE_MUSIC)
				.append(File.separator).append(apkName_).toString();
	}

	// 获得apk的名字
	public static String getApkName(String apkName) {
		if (apkName == null)
			return i++ + "";
		String apkName_ = apkName + Constants.ANDROID_APP_SUFFIX;
		return apkName_;
	}

	// 检查下载的APK是否存在
	public static Boolean checkApkIsExist(Context ctx, String pckgName, String apkName) {
		try {
			String filePath = AdNetManager.getFilePath(apkName);

			File file = new File(filePath);
			if (file != null && file.length() > 0 && file.exists() && file.isFile()) {
				PackageInfo localPackageInfo = ctx.getPackageManager().getPackageArchiveInfo(filePath,
						PackageManager.GET_ACTIVITIES);

				if (localPackageInfo == null && localPackageInfo.packageName == null) {
					JLog.i(TAG, "APK不存在:" + " apkName:" + apkName);
					return false;
				}
				if (localPackageInfo.packageName.equals(pckgName))
					JLog.i(TAG, "APK存在:" + " apkName:" + apkName);
				return true;
			}
		} catch (Exception e) {
			JLog.i(TAG, "checkApkIsExist:" + e.toString());
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * 跳转到系统浏览器(QQ浏览器)
	 *
	 * @param context
	 * @param uri
	 */
	public static void onJumpAdWebView(Context context, String uri) {

		if (context == null || uri == null) {
			Log.e("TAG", "onJumpAdWebView:context=null || uri:" + uri);
			return;
		}
		Log.i("TAG", "onJumpAdWebView:start");
		Intent intent = new Intent();
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setAction("android.intent.action.VIEW");
		Uri content_url = Uri.parse(uri);
		intent.setData(content_url);
		intent.setClassName("com.android.browser", "com.tencent.mtt.MainActivity");
		// intent.setComponent(new ComponentName("com.android.browser",
		// "com.tencent.mtt.MainActivity"));
		context.startActivity(intent);

	}

	/**
	 * 跳转到自定义webView
	 *
	 * @param context
	 * @param uri
	 */
	public static void onJumpAdWebActivity(Context context, String uri) {
		try {
			Intent it = new Intent(context, WebViewActivity.class);
			it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			it.setAction("android.intent.action.VIEW");
			it.putExtra(WebViewActivity.P_URL, uri);
			context.startActivity(it);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 跳转到三方浏览器
	 *
	 * @param context
	 * @param uri
	 */
	public static void onJumpOtherExplorer(Context context, String uri) {

		if (context == null || uri == null) {
			Log.e("TAG", "onJumpAdWebView:context=null || uri:" + uri);
			return;
		}
		Log.i("TAG", "onJumpAdWebView");
		Intent intent = new Intent();
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setAction("android.intent.action.VIEW");
		Uri content_url = Uri.parse(uri);
		intent.setData(content_url);
		intent.setClassName("com.UCMobile", "com.UCMobile.main.UCMobile");
		// intent.setComponent(new ComponentName("com.android.browser",
		// "com.tencent.mtt.MainActivity"));
		context.startActivity(intent);

	}

	/**
	 * @param context
	 *            context
	 * @param appItem
	 *            下载应用的信息,加载到应用市场下载队列
	 * @param isbackground
	 *            deful:false
	 */
	public static void onJumpAdAppDownloadService(Context context, AppsItemBean appItem, Boolean isbackground) {
		if (context == null || appItem == null) {
			Log.e("TAG", "startAdAppDownloadService:getcontext || getappItem==null");
			return;
		}
		Log.e("TAG", "startAdAppDownloadService");
		Intent intent = new Intent();
		intent.setComponent(
				new ComponentName("com.prize.appcenter", "com.prize.appcenter.service.PrizeAppCenterService"));
		intent.putExtra("action", 4);
		intent.putExtra("optType", 2);
		intent.putExtra("isbackground", isbackground);
		Bundle bundle = new Bundle();
		bundle.putParcelable("bean", (Parcelable) appItem);
		intent.putExtras(bundle);
		context.startService(intent);
	}

	/**
	 * 回传地址 如果失败，再请求一次
	 * 
	 * @param callBackurl
	 */
	public static void callBackUrl(final ArrayList<String> callBackurl, Context context) {
		if (callBackurl == null)
			return;
		String ua = PreferencesUtils.getString(context, Configs.UA);
		try {		
			for (String url : callBackurl) {
				OkHttpUtils.get()
						   .url(url)
						   .addHeader("User-Agent", ua)
						   .build()
						   .execute(new StringCallback() {
	
					@Override
					public void onResponse(String arg0, int arg1) {
						// isCallError=false;
						JLog.i(TAG, "callBackUrl-onResponse：" + arg0.toString());
					}
	
					@Override
					public void onError(Call arg0, Exception arg1, int arg2) {
						// if (!isCallError) {
						// isCallError=true;
						// callBackUrl(callBackurl);
						// }
					}
				});
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	// OkHttpUtils.get().url(callBackurl).build().execute(new StringCallback() {
	//
	// @Override
	// public void onResponse(String arg0, int arg1) {
	// // isCallError=false;
	// JLog.i(TAG, "callBackUrl-onResponse："+arg0.toString());
	// }
	//
	// @Override
	// public void onError(Call arg0, Exception arg1, int arg2) {
	//// if (!isCallError) {
	//// isCallError=true;
	//// callBackUrl(callBackurl);
	//// }
	//
	// }
	// });
	// }
	// static Boolean isCallError=false;

}
