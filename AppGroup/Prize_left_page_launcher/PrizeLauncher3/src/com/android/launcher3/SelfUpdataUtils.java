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

package com.android.launcher3;

import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RecentTaskInfo;
import android.content.Context;

/**
 * 
 ** 自升级、得到包名工具类
 * 
 * @author huangchangguo
 * @version V1.0
 */
public class SelfUpdataUtils {
	private static final String TAG = "huang-SelfUpdataUtils";
	static long downloadId = 0;

	/**
	 * @return The lists of the RunningApps
	 */
	@SuppressWarnings("deprecation")
	public static Boolean isRecentRunningApp(Context context,
			String NativePcgName) {
		ActivityManager am = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		List<RecentTaskInfo> recentTasks = am.getRecentTasks(25,
				ActivityManager.RECENT_IGNORE_UNAVAILABLE);
		for (RecentTaskInfo recentTaskInfo : recentTasks) {
			try {
				String packageName = recentTaskInfo.baseIntent.getComponent()
						.getPackageName();
				if (packageName.equals(NativePcgName)) {
					return true;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return false;
	}

}
