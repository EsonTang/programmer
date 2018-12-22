package com.prize.prizeappoutad.receiver;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.gson.Gson;
import com.prize.prizeappoutad.bean.NetCommonInfo;
import com.prize.prizeappoutad.constants.Constants;
import com.prize.prizeappoutad.utils.JLog;
import com.prize.prizeappoutad.utils.PreferencesUtils;

/**
 * 根据包名接受广播，启动广告界面
 * 
 * @author huangchangguo 2016.10.17
 * (功能整合，暂时废止)
 * 
 */
public class StartAdReceiver extends BroadcastReceiver {

	private static final String TAG = "huang-StartAdReceiver";
	private ArrayList<String> names;

	@Override
	public void onReceive(Context context, Intent intent) {
		// 接受app启动的包名

		String packageName = intent.getStringExtra("packagename");

		Log.i(TAG, "context in service = " + context);
		Log.i(TAG, "packageName = " + packageName);
		if (packageName == null) {
			return;
		}
		// 从sp中获取需要广告的包名
		String result = PreferencesUtils.getString(context,
				Constants.PACKAGENAMES, null);
		JLog.i(TAG, "StartAdReceiver-result" + result);
		try {
			JSONObject obj = new JSONObject(result);
			NetCommonInfo packageNamesInfo = new Gson().fromJson(
					result, NetCommonInfo.class);
			 names = packageNamesInfo.data.packageName;
	
//			if (obj.getInt("code") == 0) {
//
//				JSONObject o = new JSONObject(obj.getString("data"));
//
//				PackageNamesInfo packageNamesInfo = new Gson().fromJson(
//						o.getString("packageName"), PackageNamesInfo.class);
//				names = packageNamesInfo.packageName;
//			}
		} catch (JSONException e) {
			JLog.i(TAG, "StartAdReceiver-JSONException");
			e.printStackTrace();

		}

		Log.i(TAG, "NetNames:" + names.toString() + "    packageName:"
				+ packageName);
		if (names != null && names.contains(packageName)) {
			Log.i(TAG, "    startAdActivity    !");
			Intent it = new Intent(Intent.ACTION_MAIN);
			it.addCategory(Intent.CATEGORY_LAUNCHER);
			it.setClassName("com.prize.prizeappoutad",
					"com.prize.prizeappoutad.activity.RSplashActivity");
			it.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
					| Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(it);
		}
		// PackageManager packageManager = context.getPackageManager();
		// PackageInfo packageInfo;
		// String appName;
		// try {
		// packageInfo = packageManager.getPackageInfo(packageName, 0);
		// appName = packageInfo.applicationInfo.loadLabel(
		// context.getPackageManager()).toString();
		// } catch (NameNotFoundException e) {
		// e.printStackTrace();
		// }
		//
		// if (intent.getAction() == Constants.INTENT_ACTION_APP_LAUNCH) {
		// switch (packageName) {
		// case Constants.SINA_WEIBO_PACKAGE_NAME:
		// case Constants.TEST_PACKAGE_NAME:
		//
		// break;
		// }
		// } else if (intent.getAction() == Constants.INTENT_ACTION_APP_EXIT) {
		//
		// }
	}

}
