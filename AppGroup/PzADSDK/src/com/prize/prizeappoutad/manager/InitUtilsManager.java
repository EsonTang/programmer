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

package com.prize.prizeappoutad.manager;

import android.content.Context;

import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.tencent.stat.MtaSDkException;
import com.tencent.stat.StatService;

/**
 * 初始化任务的工具类
 * 
 * @author huangchangguo
 * @version V1.1
 */
public class InitUtilsManager {

	public static void initLocation(LocationClient locationClient) {
		LocationClientOption option = new LocationClientOption();
		option.setLocationMode(LocationMode.Battery_Saving);// 可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
		option.setCoorType("bd09ll");// 可选，默认gcj02，设置返回的定位结果坐标系
		option.setOpenGps(false);// 可选，默认false,设置是否使用gps
		option.setIsNeedAddress(true);// 可选，设置是否需要地址信息，默认不需要
		/*
		 * int span=1000;
		 * option.setScanSpan(span);//可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
		 * option
		 * .setLocationNotify(true);//可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
		 * option
		 * .setIsNeedLocationDescribe(true);//可选，默认false，设置是否需要位置语义化结果，可以在BDLocation
		 * .getLocationDescribe里得到，结果类似于“在北京天安门附近”
		 * option.setIsNeedLocationPoiList
		 * (true);//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
		 * option.setIgnoreKillProcess
		 * (false);//可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程
		 * ，设置是否在stop的时候杀死这个进程，默认不杀死
		 * option.SetIgnoreCacheException(false);//可选，默认false，设置是否收集CRASH信息，默认收集
		 * option.setEnableSimulateGps(false);//可选，默认false，设置是否需要过滤gps仿真结果，默认需要
		 */
		locationClient.setLocOption(option);
	}

	/**
	 * 初始化MTA统计平台
	 */
	public static void initTengXunAccount(Context context) {
		// androidManifest.xml指定本activity最先启动
		// 因此，MTA的初始化工作需要在本onCreate中进行
		// 在startStatService之前调用StatConfig配置类接口，使得MTA配置及时生效
		// initMTAConfig(true);
		String appkey = "A5K8MPK16WTQ";
		// 初始化并启动MTA
		// 第三方SDK必须按以下代码初始化MTA，其中appkey为规定的格式或MTA分配的代码。
		// 其它普通的app可自行选择是否调用
		try {
			// 第三个参数必须为：com.tencent.stat.common.StatConstants.VERSION
			StatService.startStatService(context, appkey,
					com.tencent.stat.common.StatConstants.VERSION);
		} catch (MtaSDkException e) {
			// MTA初始化失败
			e.printStackTrace();
		}
	}

}
