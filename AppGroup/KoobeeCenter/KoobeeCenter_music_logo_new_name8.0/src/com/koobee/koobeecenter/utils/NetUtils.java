package com.koobee.koobeecenter.utils;

import com.koobee.koobeecenter02.R;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetUtils {

	/**
	 * 网络是否连接
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isNetConnected(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (cm == null)
			return false;
		NetworkInfo netinfo = cm.getActiveNetworkInfo();
		if (netinfo == null) {
			return false;
		}
		return netinfo.isConnected();
	}

	/**
	 * 显示无网络提示对话框
	 * 
	 * @param context
	 * @param title
	 * @param message
	 */
	public static void openNet(final Context context, String title,
			String message) {
		// 让用户检查网络
		AlertDialog.Builder adb = new AlertDialog.Builder(context);
		final AlertDialog ad = adb.create();
		ad.setTitle(title);
		ad.setCancelable(false);
		ad.setMessage(message);
		ad.setButton(context.getString(R.string.sure),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (ad != null && ad.isShowing()) {
							ad.dismiss();
						}
						/*// 由于4.0以上把原来的设置方式舍弃了所以上面的代码舍去
						if (android.os.Build.VERSION.SDK_INT > 13) {
							// 3.2以上打开设置界面，也可以直接用ACTION_WIRELESS_SETTINGS打开到wifi界面
							context.startActivity(new Intent(
									android.provider.Settings.ACTION_SETTINGS));
						} else {
							context.startActivity(new Intent(
									android.provider.Settings.ACTION_WIRELESS_SETTINGS));
						}
*/
					}
				});
		/*ad.setButton2(context.getString(R.string.cancel),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (ad != null && ad.isShowing()) {
							ad.dismiss();
						}
					}
				});*/
		ad.show();
	}

	public static boolean isWifiConnected(Context context) {
		if (context != null) {
			ConnectivityManager mConnectivityManager = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo mWiFiNetworkInfo = mConnectivityManager
					.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
			if (mWiFiNetworkInfo != null) {
				return mWiFiNetworkInfo.isAvailable();
			}
		}
		return false;
	}

	public static boolean isMobileConnected(Context context) {
		if (context != null) {
			ConnectivityManager mConnectivityManager = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo mMobileNetworkInfo = mConnectivityManager
					.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
			if (mMobileNetworkInfo != null) {
				return mMobileNetworkInfo.isAvailable();
			}
		}
		return false;
	}
}
