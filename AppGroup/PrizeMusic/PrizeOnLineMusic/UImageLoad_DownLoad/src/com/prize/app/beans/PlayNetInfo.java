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

package com.prize.app.beans;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.telephony.TelephonyManager;

import com.prize.app.BaseApplication;
import com.prize.app.util.JLog;

/**
 * 
 **
 * 
 * @author longbaoxiu
 * @version V1.0
 */
public class PlayNetInfo {
	public final static int NONET = 0;
	public final static int MOBILE_3G = 1;
	public final static int MOBILE_2G = 2;
	public final static int MOBILE_4G = 4;
	public final static int WIFI = 3;
	public static byte networkType;

	public PlayNetInfo() {
		Context context = BaseApplication.curContext;
		networkType = (byte) getAPNType(context);

	}

	public static PlayNetInfo getInstance() {
		if (null == instance) {
			instance = new PlayNetInfo();
		}
		return instance;
	}

	private static PlayNetInfo instance;

	// 获取当前网络状态
	public static int getAPNType(Context context) {
		int netType = NONET;
		networkType = NONET;

		if (null == context) {
			return netType;
		}
		ConnectivityManager connMgr = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		if (networkInfo == null || (networkInfo.getState() != State.CONNECTED)) {
			return netType;
		}
		int nType = networkInfo.getType();
		if (nType == ConnectivityManager.TYPE_MOBILE) {
			netType = check2GOr3GNet(context);
		} else if (nType == ConnectivityManager.TYPE_WIFI) {
			netType = WIFI;
		} else {
			boolean b = ConnectivityManager.isNetworkTypeValid(nType);
			if (b) {
				netType = MOBILE_3G; // 联通3G就跑这里
			}
		}
		networkType = (byte) netType;
		JLog.info("networkType is " + networkType);
		return netType;
	}

	private static int check2GOr3GNet(Context context) {

		int mobileNetType = NONET;
		if (null == context) {
			return mobileNetType;
		}
		TelephonyManager telMgr = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);

		int netWorkType = telMgr.getNetworkType();
		JLog.i("net", "net is " + netWorkType);
		switch (netWorkType) {
		case TelephonyManager.NETWORK_TYPE_UMTS:
		case TelephonyManager.NETWORK_TYPE_HSDPA:
		case TelephonyManager.NETWORK_TYPE_HSPA:
		case TelephonyManager.NETWORK_TYPE_HSUPA:
		case TelephonyManager.NETWORK_TYPE_EVDO_0:
		case TelephonyManager.NETWORK_TYPE_EVDO_A:
			// case TelephonyManager.NETWORK_TYPE_EVDO_B:
			mobileNetType = MOBILE_3G;
			break;
		case TelephonyManager.NETWORK_TYPE_UNKNOWN:
		case TelephonyManager.NETWORK_TYPE_IDEN:
		case TelephonyManager.NETWORK_TYPE_1xRTT:
		case TelephonyManager.NETWORK_TYPE_GPRS:
		case TelephonyManager.NETWORK_TYPE_EDGE:
		case TelephonyManager.NETWORK_TYPE_CDMA:
			mobileNetType = MOBILE_2G;
			break;
		case TelephonyManager.NETWORK_TYPE_LTE: // api<11 : replace by 13
			mobileNetType = MOBILE_4G;
			break;
		default:
			mobileNetType = MOBILE_3G;
			break;
		}

		return mobileNetType;

	}
}
