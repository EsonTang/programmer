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

import android.content.Context;
import android.content.SharedPreferences;

public class Utils {
	public static final String TAG = "prize";
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
}
