package com.prize.app.util;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Parcelable;

/**
 * 快捷方式的工具方法
 * 
 * @author prize
 */
public class ShortcutUtil {
	private static final String[] PROJECTION = { "title", "iconResource" };

	/**
	 * 
	 * @param act
	 *            ： 当前的activity , not null
	 * @param iconResId
	 *            : shortcut 的 ICON , not null
	 * @param shortcutName
	 *            ： , not null
	 * @param cls
	 *            ： 启动主入口 , not null
	 * @param intent
	 *            : 需要传参数的时候，可以传 null
	 * 
	 */
	public static void createShortCut(Context appContext, int iconResId,
			String shortcutName, Class<?> cls, Intent intent) {

		if (null == intent) {
			intent = new Intent();
		}
		intent.setFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
		intent.addFlags(Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); //添加这一项，确保只创建一次
		
		intent.setAction(Intent.ACTION_MAIN);// Intent.ACTION_MAIN为了在卸载应用的时候同时删除桌面快捷方式
		intent.addCategory(Intent.CATEGORY_LAUNCHER);// Intent.CATEGORY_LAUNCHER为了在卸载应用的时候同时删除桌面快捷方式
		intent.setClass(appContext, cls);

		// 创建快捷的intent
		Intent shortcutintent = new Intent(
				"com.android.launcher.action.INSTALL_SHORTCUT");

		// 不允许重复创建
		shortcutintent.putExtra("duplicate", false);
		// 需要现实的名称
		shortcutintent.putExtra(Intent.EXTRA_SHORTCUT_NAME, shortcutName);
		// 快捷图片
		Parcelable icon = Intent.ShortcutIconResource.fromContext(appContext,
				iconResId);
		shortcutintent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, icon);
		// 点击快捷图片，运行的程序主入口
		shortcutintent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, intent);
		// 发送广播
		appContext.sendBroadcast(shortcutintent);
	}

	public static boolean hasShortcut(Context appContext, String shortcutName) {
		final ContentResolver cr = appContext.getContentResolver();
		int sdkVersion = android.os.Build.VERSION.SDK_INT;
		String AUTHORITY = "com.android.launcher2.settings";

		if (sdkVersion < 8) {
			AUTHORITY = "com.android.launcher.settings";
		} else {
			AUTHORITY = "com.android.launcher2.settings";
		}

		Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
				+ "/favorites?notify=true");
		try {
			Cursor c = cr.query(CONTENT_URI, PROJECTION, "title=?",
					new String[] { shortcutName }, null);
			if (c != null && c.moveToFirst()) {
				c.close();
				return true;
			}
		} catch (Exception e) {
		}
		
		return false;
	}

	public static void delShortCut(Context appContext,String shortcutName, Class<?> cls){
		//快捷方式的名称  
		Intent shortcut = new Intent("com.android.launcher.action.UNINSTALL_SHORTCUT");  
		shortcut.putExtra(Intent.EXTRA_SHORTCUT_NAME, shortcutName);

		Intent intent = new Intent();
		intent.setFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
		intent.addFlags(Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY);
		intent.setAction(Intent.ACTION_MAIN);// Intent.ACTION_MAIN为了在卸载应用的时候同时删除桌面快捷方式
		intent.addCategory(Intent.CATEGORY_LAUNCHER);// Intent.CATEGORY_LAUNCHER为了在卸载应用的时候同时删除桌面快捷方式
		intent.setClass(appContext, cls);
		
		shortcut.putExtra(Intent.EXTRA_SHORTCUT_INTENT, intent);  
		appContext.sendBroadcast(shortcut);  
	}
}
