/*******************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：悬浮音乐窗口管理
 *当前版本：V1.0
 *作	者：朱道鹏
 *完成日期：2015-05-08
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
package com.prize.music;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;

import com.prize.music.R;

/**
 **
 * 类描述：悬浮音乐窗口管理类
 * @author 朱道鹏
 * @version V1.0
 */
public class MusicWindowManager {

	/**
	 * 悬浮窗View的实例
	 */
	private static MusicFloatView musicFloatView;

	/**
	 * 悬浮窗View的参数
	 */
	private static LayoutParams musicViewWindowParams;

	/**
	 * 用于控制在屏幕上添加或移除悬浮窗
	 */
	private static WindowManager mWindowManager;

	/**
	 * 用于获取手机可用内存
	 */
	private static ActivityManager mActivityManager;

	/**
	 * 创建一个悬浮窗。位置为屏幕正中间。
	 */
	public static void createBigWindow(Context context) {
		if (context == null) {
			return;
		}
		WindowManager windowManager = getWindowManager(context);
		int screenWidth = windowManager.getDefaultDisplay().getWidth();
		int screenHeight = windowManager.getDefaultDisplay().getHeight();
		int windowViewWidth = context.getResources().getDimensionPixelOffset(R.dimen.float_view_width);
		int windowViewHeight = context.getResources().getDimensionPixelOffset(R.dimen.float_view_height);
		if (musicFloatView == null) {
			musicFloatView = new MusicFloatView(context);
			
			musicViewWindowParams = new LayoutParams();
			musicViewWindowParams.x = screenWidth / 2 - windowViewWidth / 2;
			musicViewWindowParams.y = screenHeight / 2 - windowViewHeight / 2;
			musicViewWindowParams.type = LayoutParams.TYPE_PHONE;
			musicViewWindowParams.format = PixelFormat.RGBA_8888;
			musicViewWindowParams.gravity = Gravity.LEFT | Gravity.TOP;
			musicViewWindowParams.width = windowViewWidth;
			musicViewWindowParams.height = windowViewHeight;
			musicViewWindowParams.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL | LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH | 
					LayoutParams.FLAG_NOT_FOCUSABLE;
			musicFloatView.setParams(musicViewWindowParams);
			try {
				windowManager.addView(musicFloatView, musicViewWindowParams);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 将大悬浮窗从屏幕上移除。
	 */
	public static void removeBigWindow(Context context) {
		if (musicFloatView != null) {
			WindowManager windowManager = getWindowManager(context);
			windowManager.removeView(musicFloatView);
			musicFloatView = null;
		}
	}

	/**
	 * 如果WindowManager还未创建，则创建一个新的WindowManager返回。否则返回当前已创建的WindowManager。
	 * 
	 * @param context
	 *            必须为应用程序的Context.
	 * @return WindowManager的实例，用于控制在屏幕上添加或移除悬浮窗。
	 */
	private static WindowManager getWindowManager(Context context) {
		if (mWindowManager == null) {
			mWindowManager = (WindowManager) context
					.getSystemService(Context.WINDOW_SERVICE);
		}
		return mWindowManager;
	}

	/**
	 * 是否有悬浮窗(包括小悬浮窗和大悬浮窗)显示在屏幕上。
	 * 
	 * @return 有悬浮窗显示在桌面上返回true，没有的话返回false。
	 */
	public static boolean isWindowShowing() {
		return musicFloatView != null;
	}

	/**
	 * 如果ActivityManager还未创建，则创建一个新的ActivityManager返回。否则返回当前已创建的ActivityManager。
	 * 
	 * @param context
	 *            可传入应用程序上下文。
	 * @return ActivityManager的实例，用于获取手机可用内存。
	 */
	private static ActivityManager getActivityManager(Context context) {
		if (mActivityManager == null) {
			mActivityManager = (ActivityManager) context
					.getSystemService(Context.ACTIVITY_SERVICE);
		}
		return mActivityManager;
	}

	/**
	 * 计算已使用内存的百分比，并返回。
	 * 
	 * @param context
	 *            可传入应用程序上下文。
	 * @return 已使用内存的百分比，以字符串形式返回。
	 */
	public static String getUsedPercentValue(Context context) {
		String dir = "/proc/meminfo";
		try {
			FileReader fr = new FileReader(dir);
			BufferedReader br = new BufferedReader(fr, 2048);
			String memoryLine = br.readLine();
			String subMemoryLine = memoryLine.substring(memoryLine
					.indexOf("MemTotal:"));
			br.close();
			long totalMemorySize = Integer.parseInt(subMemoryLine.replaceAll(
					"\\D+", ""));
			long availableSize = getAvailableMemory(context) / 1024;
			int percent = (int) ((totalMemorySize - availableSize)
					/ (float) totalMemorySize * 100);
			return percent + "%";
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "悬浮窗";
	}

	/**
	 * 获取当前可用内存，返回数据以字节为单位。
	 * 
	 * @param context
	 *            可传入应用程序上下文。
	 * @return 当前可用内存。
	 */
	private static long getAvailableMemory(Context context) {
		ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
		getActivityManager(context).getMemoryInfo(mi);
		return mi.availMem;
	}

}
