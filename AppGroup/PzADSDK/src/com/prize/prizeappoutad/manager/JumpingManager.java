package com.prize.prizeappoutad.manager;

import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.prize.prizeappoutad.activity.WebviewActivity;
import com.prize.prizeappoutad.bean.AppSelfUpdateBean;
import com.prize.prizeappoutad.bean.AppsItemBean;

/**
 * 广告平台 跳转控制类 1：跳转到Web界面 2：跳转到应用市场下载队列
 * 
 * @author huangchangguo 2016.11.14
 * 
 */
public class JumpingManager {
	private static final String TAG = "huang-JumpingManager";

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
		bundle.putParcelable("bean", appItem);
		intent.putExtras(bundle);
		context.startService(intent);
	}

	/**
	 * 跳转到系统浏览器
	 * 
	 * @param context
	 * @param uri
	 * @param string
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
		intent.setClassName("com.android.browser",
				"com.tencent.mtt.MainActivity");
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
	public static void onJumpAdWebView2(Context context, String uri,
			String webviewTitle) {

		if (context == null || uri == null) {
			Log.e("TAG", "onJumpAdWebView:context=null || uri:" + uri);
			return;
		}
		Log.i("TAG", "onJumpAdWebView:start");
		Intent intent = new Intent(context, WebviewActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra(WebviewActivity.EXTRA_URL, uri);
		intent.putExtra(WebviewActivity.EXTRA_TITLE, webviewTitle);
		context.startActivity(intent);

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
		intent.setClassName("com.UCMobile",
				"com.UCMobile.main.UCMobile");
		// intent.setComponent(new ComponentName("com.android.browser",
		// "com.tencent.mtt.MainActivity"));
		context.startActivity(intent);

	}

}
