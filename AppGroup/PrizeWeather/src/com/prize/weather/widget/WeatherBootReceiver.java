
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

package com.prize.weather.widget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 **
 * 类描述：
 * @author 作者
 * @version 版本
 */
public class WeatherBootReceiver extends BroadcastReceiver{
	
	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		Log.d("receive_hky","action = "+action);
		if((Intent.ACTION_BOOT_COMPLETED.equals(action)) //|| (Intent.ACTION_USER_PRESENT).equals(action)
		        /*PRIZE-Add-PrizeWeather-wangzhong-2016_8_10-start*/
		        || (("com.cooee.weather.Weather.action.REQUEST_REFRESH_DATA_FOR_3TH").equals(action))
		        /*PRIZE-Add-PrizeWeather-wangzhong-2016_8_10-end*/
				|| (("android.net.conn.CONNECTIVITY_CHANGE").equals(action))) {
//		if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
			context.startService(new Intent(context, WidgetService.class));
		}
	}

}
