package com.android.prize.salesstatis;

import android.content.Context;
import android.telephony.TelephonyManager;

public class SalesStatisUtil {
	
	 /**
	 * 方法描述：获取手机imei号
	 * @param Context context
	 * @return String 返回imei号
	 */
	public static String getIMEI(Context context) {

		return ((TelephonyManager) context.getSystemService(

		Context.TELEPHONY_SERVICE)).getDeviceId();

	}
}
