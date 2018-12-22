package com.koobee.koobeecenter.utils;

/*******************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：
 *当前版本：
 *作	者：
 *完成日期：
 *修改记录：
 *修改日期：
 *版 本 号：
 *修 改 人：
 *修改内容：
...
 *修改记录：
 *修改日期：
 *版 本 号：
 *修 改 人：
 *修改内容：
 *********************************************/


import android.app.StatusBarManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;


import java.lang.reflect.Field;

import com.koobee.koobeecenter02.R;

//import android.app.StatusBarManager;

public class WindowMangerUtils {

	/**
	 * 
	 * 反转status状态栏的图标 文字颜色 只能在setContentView之后调用
	 *
	 * @param mWindow  Window
	 * @return void
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

	/**
	 * 
	 * 反转status状态栏的图标 文字颜色 只能在setContentView之后调用
	 * 
	 * @param mWindow
	 *            Window
	 * @return void
	 */
	public static void changeStatus2White(Window mWindow) {
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

	/**
	 * 方法描述：沉浸式状态栏
	 */
	public static void initStateBar(Window window, Context mCtx) {
		window.requestFeature(Window.FEATURE_NO_TITLE);
		if (VERSION.SDK_INT >= VERSION_CODES.L) {
			window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
					| WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
			window.getDecorView().setSystemUiVisibility(
				View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
							| View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
			window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
			window.setStatusBarColor(mCtx.getResources().getColor(R.color.color_00000000));
			
		} else if (VERSION.SDK_INT >= VERSION_CODES.KITKAT) {
			//透明状态栏  
			window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);  
			//透明导航栏  
			window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
		}
	}

	/**
	 * 方法描述：沉浸式状态栏
	 */
	public static void initStateBar(Window window, Context mCtx, int color) {
		window.requestFeature(Window.FEATURE_NO_TITLE);
		if (VERSION.SDK_INT >= VERSION_CODES.KITKAT) {
			window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
					| WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
			window.getDecorView().setSystemUiVisibility(
					View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
							| View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
			window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
			window.setStatusBarColor(mCtx.getResources().getColor(color));
		}
	}

	public static int getStatusBarHeight(Context context) {
		int result = 0;
		int resourceId = context.getResources().getIdentifier(
				"status_bar_height", "dimen", "android");
		if (resourceId > 0) {
			result = context.getResources().getDimensionPixelSize(resourceId);
		}
		return result;
	}
}
