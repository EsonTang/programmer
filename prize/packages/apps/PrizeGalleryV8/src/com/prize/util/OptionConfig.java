/***
 * function:watermark
 * author: wanzhijuan
 * date:   2016-1-21
 */
package com.prize.util;

import android.content.Context;
import com.android.gallery3d.R;

public class OptionConfig {
	
	public static boolean SHOW_WATERMARK;
	
	public static boolean SHOW_VIDEO_FLOAT;
	
	public static boolean getBool(Context context, int resId) {
		return context.getResources().getBoolean(resId);
	}
	
	public static int getInt(Context context, int resId) {
		return context.getResources().getInteger(resId);
	}
	
	public static String getString(Context context, int resId) {
		return context.getResources().getString(resId);
	}
	
	public static void configV6Point3(Context context) {
		SHOW_WATERMARK = getBool(context, R.bool.config_show_watermark);
		SHOW_VIDEO_FLOAT = getBool(context, R.bool.config_video_float);
	}
}
