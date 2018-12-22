package com.prize.music.admanager.presenter;

import com.prize.app.net.datasource.base.AppsItemBean;
import com.prize.music.activities.MainActivity;
import com.prize.music.admanager.view.WebViewActivity;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
	
/**
 * 跳转App的管理类,针对固定的几个推荐App
 * 
 * @author huangchangguo
 * 
 *         2017.1.12
 */
public class AdJumpManager {

	private static final String TAG = "huang-OpenAppManager";

	/**
	 * type 1 Open App
	 * 
	 * @param ctx
	 * @param appPcgName
	 *            detailUrl
	 * @param DetailPath
	 *            detail
	 * @param appid
	 * 
	 */
	public static void openAppHome(Context ctx, String appPcgName, String appDetail, String appid) {
		JLog.i(TAG, " |appPcgName:" + appPcgName + " |DetailUrl:" + " |appid:" + appid);
		if (ctx == null || appPcgName == null || appid == null)
			return;
		if (isPkgInstalled(appPcgName, ctx)) {
			Intent intent = new Intent();
			try {
				intent = ctx.getPackageManager().getLaunchIntentForPackage(appPcgName);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				ctx.startActivity(intent);
			} catch (Exception e) {
				JLog.i(TAG, "Launcher Special App Error!" + appPcgName);
			}

		} else {
			gotoAppCenter(ctx, appid);
		}
	}

	/**
	 * openAppDetail for system installed
	 * 
	 * @param ctx
	 * @param appPcgName
	 * @param appDetail
	 * @param appid
	 */
	public static void openAppDetail(Context ctx, String appPcgName, String appDetail, String appid) {
		if (ctx == null || appPcgName == null || appid == null)
			return;
		if (isPkgInstalled(appPcgName, ctx)) {
			swichApps(ctx, appPcgName, appDetail, appid);
		} else {
			JLog.i(TAG, "openAppDetail-gotoAppCenter");
			gotoAppCenter(ctx, appid);
		}
	}

	private static void swichApps(Context ctx, String appPcgName, String appDetail, String appid) {

		if (appPcgName.contains("com.qiyi.video")) {
			wakeUpAQY(ctx, appDetail, appid);			
		} else if (appPcgName.contains("com.ss.android.article.news") || appPcgName.contains("com.andreader.prein")) {
			// 头条
			Intent intent = new Intent();
			intent.setData(Uri.parse(appDetail));
			intent.setAction(Intent.ACTION_VIEW);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			try {
				ctx.startActivity(intent);
			} catch (Exception e) {
			}

		} else if (appPcgName.contains("com.sankuai.meituan") || appPcgName.contains("com.tencent.news")
				|| appPcgName.contains("com.tencent.reading")) {
			// 美团
			Intent intent = new Intent();
			intent.setData(Uri.parse(appDetail));
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			try {
				ctx.startActivity(intent);
			} catch (Exception e) {
			}
		} else if (appPcgName.contains("com.qihoo.browser")) {
			// 奇虎
			Intent intent = new Intent();
			intent.setAction(Intent.ACTION_VIEW);
			intent.setData(Uri.parse(appDetail));
			intent.putExtra("from", "kb");
			intent.setClassName("com.qihoo.browser", "com.qihoo.browser.activity.SplashActivity");
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			try {
				ctx.startActivity(intent);
			} catch (Exception e) {
			}

			// } else if (appPcgName.contains("com.tencent.news")) {
			// Intent news = new Intent();
			// news.setData(Uri.parse(appDetail));
			// news.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			// try {
			// ctx.startActivity(news);
			// } catch (Exception e) {
			// }
			// } else if (appPcgName.contains("com.tencent.reading")) {
			// Intent intent = new Intent();
			// intent.setData(Uri.parse(appDetail));
			// intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			// try {
			// ctx.startActivity(intent);
			// } catch (Exception e) {
			// }
			// } else if (appPcgName.contains("com.andreader.prein")) {
			// // 咪咕
			// Intent intent = new Intent();
			// intent.setData(Uri.parse(appDetail));
			// intent.setAction(Intent.ACTION_VIEW);
			// intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			// try {
			// ctx.startActivity(intent);
			// } catch (Exception e) {
			// }
		}else {//不在推荐列表中的App，直接打开主页
			Intent intent = new Intent();
			try {
				intent = ctx.getPackageManager().getLaunchIntentForPackage(appPcgName);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				ctx.startActivity(intent);
			} catch (Exception e) {
				JLog.i(TAG, "Launcher Special App Error!" + appPcgName);
			}

		}

	}

	/**
	 * 特殊处理爱奇艺启动，兼容它的特定版本
	 * 
	 * @param ctx
	 * @param appDetail
	 * @param appid
	 */
	private static void wakeUpAQY(Context ctx, String appDetail, String appid) {
		int versionCode = 0;
		try {
			PackageManager pm = ctx.getPackageManager();
			ApplicationInfo applicationInfo = pm.getApplicationInfo("com.qiyi.video", 0);
			PackageInfo packageInfo = pm.getPackageArchiveInfo(applicationInfo.publicSourceDir, 0);
			if (packageInfo != null) {
				versionCode = packageInfo.versionCode;
			}
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}

		if (versionCode == 80730) { // v7.3
			Intent intent = new Intent();
			intent.setData(Uri.parse(appDetail));
			intent.setAction("android.intent.action.qiyivideo.player");
			intent.setPackage("com.qiyi.video");
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			try {
				ctx.startActivity(intent);
			} catch (ActivityNotFoundException e) {
				gotoAppCenter(ctx, appid);
			}
		} else if (versionCode >= 80770) { // v7.7
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setData(Uri.parse(appDetail));
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			try {
				ctx.startActivity(intent);
			} catch (ActivityNotFoundException e) {
				gotoAppCenter(ctx, appid);
			}
		}
	}

	private static void gotoAppCenter(Context ctx, String appid) {
		Toast.makeText(ctx, "未检测到应用，请先下载安装", Toast.LENGTH_SHORT).show();
		try {
			Intent intent = new Intent();
			intent.setClassName("com.prize.appcenter", "com.prize.appcenter.activity.AppDetailActivity");
			Bundle bundle = new Bundle();
			bundle.putParcelable("AppsItemBean", null);
			bundle.putString("appid", appid);
			intent.putExtra("bundle", bundle);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			ctx.startActivity(intent);
		} catch (Exception e) {
			e.getMessage();
		}
	}
	
	/**
	 * 判断系统是否存在此应用
	 * @param pkgName
	 * @param ctx
	 * @return
	 */
	public static boolean isPkgInstalled(String pkgName,Context ctx) {
		PackageInfo packageInfo = null;
		try {
		  packageInfo = ctx.getPackageManager().getPackageInfo(pkgName, 0);
		} catch (NameNotFoundException e) {
		  packageInfo = null;
		  e.printStackTrace();
		}
		if (packageInfo == null) {
		  return false;
		} else {
		  return true;
		}
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
        	Log.i("huang",e.toString());
            e.printStackTrace();
        }
    }
    
    /**
     * 跳转到APK主页
     *
     * @param context
     * @param uri
     */
    public static void onJumpMainActivity(Context context) {
        try {
            Intent it = new Intent(context, MainActivity.class);
            it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);         
            context.startActivity(it);
        } catch (Exception e) {
        	Log.i("huang",e.toString());
            e.printStackTrace();
        }
    }
    
	/**
	 * 
	 * @param context
	 *            applicationContext
	 * @param appItem
	 *            下载应用的信息
	 * @param isbackground
	 *            deful:false
	 */
	public static void onJumpAdAppDownloadService(Context context,
			AppsItemBean appItem, Boolean isbackground) {
		if (context == null || appItem == null) {
			Log.e("TAG",
					"startAdAppDownloadService:getcontext || getappItem==null");
			return;
		}
		Log.e("TAG", "startAdAppDownloadService");
		Intent intent = new Intent();
		intent.setComponent(new ComponentName("com.prize.appcenter",
				"com.prize.appcenter.service.PrizeAppCenterService"));
		intent.putExtra("action", 4);
		intent.putExtra("optType", 2);
		intent.putExtra("isbackground", isbackground);
		Bundle bundle = new Bundle();
		bundle.putSerializable("bean", appItem);
		intent.putExtras(bundle);
		context.startService(intent);
	}
	 public static int dip2px(Context context, float dpValue) {
	        final float scale = context.getResources().getDisplayMetrics().density;
	        return (int) (dpValue * scale + 0.5f);
	    }
}
