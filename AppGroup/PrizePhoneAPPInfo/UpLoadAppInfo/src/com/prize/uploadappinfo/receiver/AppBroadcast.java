package com.prize.uploadappinfo.receiver;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;

import com.prize.uploadappinfo.bean.AppRecordInfo;
import com.prize.uploadappinfo.constants.Constant;
import com.prize.uploadappinfo.database.InstalledAppTable;
import com.prize.uploadappinfo.database.PrizeDatabaseHelper;
import com.prize.uploadappinfo.utils.JLog;
import com.prize.uploadappinfo.utils.PollingUtils;

/**
 * 类描述：监听app安装和卸载的广播
 * 
 * @author prize
 * @version 版本
 */
public class AppBroadcast extends BroadcastReceiver {

	private String TAG = "AppBroadcast";

	@Override
	public void onReceive(Context context, Intent intent) {
		String data = intent.getData().toString();
		String pkgName = data.substring(data.indexOf(":") + 1).trim();
		JLog.i(TAG, "data=" + data + "----packageName=" + pkgName + "--intent="
				+ intent);
		int versioncode = 0;
		String versionName = null;
		String appName = null;
		if (Intent.ACTION_PACKAGE_ADDED.equals(intent.getAction())) {
			try {
				PackageManager pm = context.getPackageManager();
				ApplicationInfo packageInfo = pm.getApplicationInfo(pkgName, 0);
				versioncode = packageInfo.versionCode;

				versionName = pm.getPackageInfo(pkgName, 0).versionName;
				appName = packageInfo.loadLabel(pm).toString();
			} catch (Exception e) {
				e.printStackTrace();
			}
			ContentValues value = new ContentValues();
			value.put(InstalledAppTable.PKG_NAME, pkgName);
			value.put(InstalledAppTable.VERSION_CODE, versioncode);
			value.put(InstalledAppTable.VERSIONNAME, versionName);
			value.put(InstalledAppTable.APPNAME, appName);
			PrizeDatabaseHelper.updateInstalledTable(value);
			PollingUtils.startServiceWithtValue(context, appName, pkgName,
					Constant.APP_TYPE_INSTALL, 2);
			return;
		}
		if (Intent.ACTION_PACKAGE_REMOVED.equals(intent.getAction())) {
			appName = queryAppName(pkgName);
			JLog.i(TAG, "appName="+appName);
			PollingUtils.startServiceWithtValue(context, appName, pkgName,
					Constant.APP_TYPE_UNINSTALL, 2);
			StringBuilder builder = new StringBuilder(
					InstalledAppTable.PKG_NAME).append("=?");
			PrizeDatabaseHelper.delete(InstalledAppTable.TABLE_NAME,
					builder.toString(), new String[] { pkgName });
			return;
		}
	}

	/**
	 * 获取应用名称
	 * 
	 * @param pkgName
	 * @return
	 */
	private String queryAppName(String pkgName) {
		StringBuilder builder = new StringBuilder(InstalledAppTable.PKG_NAME)
				.append("=?");
		Cursor cursor = PrizeDatabaseHelper.query(InstalledAppTable.TABLE_NAME,
				null, builder.toString(), new String[] { pkgName }, null, null,
				null);
		String appName = null;
		if (cursor != null) {
			try {
				int appColumnIndex = cursor
						.getColumnIndex(InstalledAppTable.APPNAME);
				if (cursor.moveToNext()) {
					appName = cursor.getString(appColumnIndex);
					JLog.i(TAG,
							"queryAppName-cursor.getCount()="
									+ cursor.getCount() + "--appName="
									+ appName);
				}
				cursor.close();
			} catch (Exception e) {
				// TODO: handle exception
			} finally {
				if (cursor != null) {
					cursor.close();
				}
			}

		}
		return appName;
	}
}
