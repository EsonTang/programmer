/*******************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：
 *当前版本：1.0.0
 *作	者：yiyi
 *完成日期：2015年8月4日
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
package com.prize.boot.util;

import android.R.string;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

public class Utils {
	public static final String TAG = "pengcancan";
	public static int defaultLanguage = -1;
	public static void saveBootStatus(Context ctx, boolean value) {
		SharedPreferences settings = ctx.getSharedPreferences("boot_prefs", Context.MODE_WORLD_READABLE);
		SharedPreferences.Editor editor = settings.edit();
		editor.putBoolean("boot", value);
		editor.commit();
	}
	
	public static final int RESULT_CODE_NEXT = 20;
    public static final int RESULT_CODE_BACK = 21;
    public static final int RESULT_CODE_FINISH = 22;
    
    
    private static String APPCENTER_PKG = "com.prize.appcenter";
	public static boolean isAppCenterExist(Context context) {
		try {
			context.getPackageManager().getApplicationInfo(APPCENTER_PKG, 0);
			return true;
		} catch (Exception e) {
		}
		return false;
	}
	
	/**
	 * 获取版本号
	 * @return AppCenter的版本号
	 */
	public static boolean isAppCenterVersionValid(Context context) {
		Float ver = 1.0f;
		if (isAppCenterExist(context)) {
			try {
				PackageManager manager = context.getPackageManager();
				PackageInfo info = manager.getPackageInfo(APPCENTER_PKG, 0);
				String version = info.versionName;
				ver = Float.parseFloat(version.substring(0, 3));
				Log.i("pengcancan", "[isAppCenterVersionValid] version: "+version+", ver: "+ver);
			} catch (Exception e) {
				e.printStackTrace();
				ver = 1.0f;
			}
		}
		return ver >= 1.2f;
	}
}
