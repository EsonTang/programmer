package com.prize.lockscreen.utils;

import java.util.Random;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.view.Display;
import android.view.WindowManager;

import com.prize.lockscreen.application.LockScreenApplication;

public class DisplayUtil {

	private static Point SCR_POINT = null;
	
	public static int getScreenWidthPixels() {
		if (null == SCR_POINT) {
			SCR_POINT = new Point();
			// DisplayMetrics dm = new DisplayMetrics();
			Display ds = ((WindowManager) LockScreenApplication.getContext().getSystemService(
					Context.WINDOW_SERVICE)).getDefaultDisplay();
			ds.getRealSize(SCR_POINT);
		}
		return SCR_POINT.x;// > SCR_POINT.y ? SCR_POINT.y : SCR_POINT.x;
	}

	public static int getScreenHeightPixels() {
		if (null == SCR_POINT) {
			SCR_POINT = new Point();
			//DisplayMetrics dm = new DisplayMetrics();
			Display ds = ((WindowManager) LockScreenApplication.getContext().getSystemService(
					Context.WINDOW_SERVICE)).getDefaultDisplay();
			ds.getRealSize(SCR_POINT);
		}
		return SCR_POINT.y;// > SCR_POINT.x ? SCR_POINT.y : SCR_POINT.x;
	}

	/**
	 * 0 <= nextInt(n) < n
	 */
	public static int getDirection() {
//		return (int) (Math.random() * 4) + 1;
		Random rand = new Random();
		return rand.nextInt(360);
	}

	/**
	 * 状态栏的高度
	 * @param res
	 * @return
	 */
	public static int getStatusBarHeight(Context context) {
		Resources res = context.getResources();
		int result = 0;
		int resourceId = res.getIdentifier("status_bar_height", "dimen",
				"android");
		if (resourceId > 0) {
			result = res.getDimensionPixelSize(resourceId);
		}
		return result;
	}
}
