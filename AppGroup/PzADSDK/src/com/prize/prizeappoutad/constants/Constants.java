package com.prize.prizeappoutad.constants;

import java.io.File;

import org.json.JSONObject;

import android.os.Environment;

public class Constants {

	/** JLog 开关，打开/关闭 */
	public static final boolean JLog = false;

	/** 正式环境URL */
	// 正式环境自升级
	public static final String AD_SOURCE_URL = "http://newapi.szprize.cn/Ad/UpgradeInfo/check";
	// 正式环境过滤包名
	public static final String AD_FILTER_URL = "http://newapi.szprize.cn/Ad/AppInfo/App";
	/** 测试环境URL */
	// 测试环境自升级
	// public static final String AD_SOURCE_URL =
	// "http://192.168.1.158:8084/Ad/UpgradeInfo/check";
	// 测试环境过滤包名
	// public static final String AD_FILTER_URL =
	// "http://192.168.1.158:8084/Ad/AppInfo/App";

	// --------------------------------------------勿需改变的常量值-------------------------------------------//
	public static final String PREFERENCE_AD_SOURCE = "prize.adSource";
	// 下载的id，自升级
	public static final String KEY_NAME_DOWNLOAD_ID = "downloadAdId";

	public static final String APP_MD5 = "appMD5";
	public static final String PACKAGENAMES = "packagenames";
	// 插屏广告信息
	public static final String OUTADINFO = "outAdInfo";

	/** 百度广告 */
	public static final String AD_SOURCE_BAIDU = "baidu";
	/** 百度PUSH初始化成功 */
	public static final String BD_INIT_SUCESS = "BD_INIT_SUCESS";
	/** 有米广告 */
	public static final String AD_SOURCE_YOUMI = "youmi";

	/** tid key */
	public static final String KEY_TID = "persist.sys.tid";

	public static final String GIS_URL_DEVICE = "http://ics.szprize.cn";
	/**** 接口安全校验，先请求改接口来获取一个pid *****/
	public static final String PID_URL = GIS_URL_DEVICE + "/ics/api/pid";
	/**** 此接口需要与pid接口联用 *****/
	public static final String UUID_URL = GIS_URL_DEVICE + "/ics/api/uuid";
	/**** 广告服务的报名 *****/
	public static final String AD_SERVICE_NAME = "com.prize.prizeappoutad.service.ThirdAdService";

	public static final String UPDATE_TID = GIS_URL_DEVICE
			+ "/ics/api/updateTid";

	// 检测更新文件存储的路径
	public static final String ANDROID_APP_SUFFIX = ".apk";
	public static final String PRIZEAPPCENTER = "PrizeAppOutAd";
	public static String DOWNLOAD_FOLDER_NAME = "download";
	public static final String DOWNLOAD_FILE_NAME = PRIZEAPPCENTER
			+ ANDROID_APP_SUFFIX;
	public static final String APKPATH = new StringBuilder(Environment
			.getExternalStorageDirectory().getAbsolutePath())
			.append(File.separator).append(Constants.PRIZEAPPCENTER)
			.append(File.separator).append(Constants.DOWNLOAD_FOLDER_NAME)
			.toString();
	public static final String APKFILEPATH = new StringBuilder(Environment
			.getExternalStorageDirectory().getAbsolutePath())
			.append(File.separator).append(Constants.PRIZEAPPCENTER)
			.append(File.separator).append(Constants.DOWNLOAD_FOLDER_NAME)
			.append(File.separator).append(Constants.DOWNLOAD_FILE_NAME)
			.toString();

	/** actions */
	public static final String INTENT_ACTION_APP_LAUNCH = "action.prize.app.lauch";
	public static final String INTENT_ACTION_APP_EXIT = "action.prize.app.exit";
	public static final String INTENT_ACTION_360_SHOUJIZHUSHOU_LAUNCH = "intent.action.360.shoujizhushou.lauch";
	public static final String INTENT_ACTION_WANDOUJIA_LAUNCH = "intent.action.wandoujia.lauch";
	public static final String INTENT_ACTION_SINA_WEIBO_LAUNCH = "intent.action.sina.weibo.lauch";
	public static final String INTENT_ACTION_SHOW_FLOAT_SPOT = "intent.action.show.float.spot";
	public static final String INTENT_ACTION_SHOW_FLOAT_BANNER = "intent.action.show.float.banner";
	public static final String ACTION_BOOT_COMPLETED = "intent.action.start.service";

	public static final String ACTION_PRIZE_ALARM = "action.prize.alarm";

	public static final String SINA_WEIBO_PACKAGE_NAME = "com.sina.weibo";
	public static final String TEST_PACKAGE_NAME = "adtest.nlg.com.adtest";
	// baidu push key
	public static final String PUSH_API_KEY = "XpcPGl1cVh0IdsQNUb6uzs1B";

	// test ad string
	public static final String TEST_AD_STRING = "{\"typeId\": 2,\"tag\": \"这是备用的字段一，默认值为null\",\"subTag\": "
			+ "\"这是两个备用的字段二，默认值为null\",\"adSource\": \"广告商\",\"adBanner\":"
			+ "\"http://ad-client.oss-cn-beijing.aliyuncs.com/ad-szprize/AdImg/2016-11-21/1479715439.png\",\"adDetails\": "
			+ "{\"detailType\": 1 ,"
			+ "\"jumpUrl\": \"http://msoftdl.360.cn/mobilesafe/shouji360/360safe/500192/360MobileSafe.apk\","
			+ "\"appInfo\": {"
			+ "\"appTypeId\":2,"
			+ "\"catId\":22,"
			+ "\"name\":\"神手麻将\","
			+ "\"packageName\":\"com.jiami.quickmj.godhand\","
			+ "\"categoryName\":\"棋牌游戏\","
			+ "\"rating\":4.9,"
			+ "\"tag\":\"单机 麻将 休闲 血战 四川麻将 二人麻将\","
			+ "\"versionName\":\"1.8.3\","
			+ "\"iconUrl\":\"http://cdnimages.oss-cn-hangzhou.aliyuncs.com/appstore/appinfo/com.jiami.quickmj.godhand/1473842631424.res/drawable/icon.png\","
			+ "\"largeIcon\":\"http://img.szprize.cn/appstore/appinfo/largeIcon/1461217612403.png\","
			+ "\"apkSize\":29590181,"
			+ "\"apkSizeFormat\":\"28.22MB\","
			+ "\"boxLabel\":0,"
			+ "\"downloadTimes\":76559459,"
			+ "\"downloadTimesFormat\":\"7656万次\","
			+ "\"downloadUrl\":\"http://msoftdl.360.cn/mobilesafe/shouji360/360safe/500192/360MobileSafe.apk\","
			+ "\"versionCode\":803,"
			+ "\"updateInfo\":\"1、增加了新的兑换码，关注微信公众号和官方微博即可免费获得；\","
			+ "\"apkMd5\":\"ab3ec747590011a7329e401012cf2829\","
			+ "\"brief\":\"胡牌胡到手抽筋，赢钱赢到你开心!\","
			+ "\"updateTime\":\"2016-09-30 15:29:37\","
			+ "\"giftCount\":0,"
			+ "\"isAd\":0,"
			+ "\"bannerUrl\":null,"
			+ "\"statusType\":0,"
			+ "\"sourceType\":0,"
			+ "\"subTitle\":null,"
			+ "\"sourceId\":1132232,"
			+ "\"searchDownloadPecent\":null,"
			+ "\"ourTag\":\"\","
			+ "\"appPatch\":null,"
			+ "\"weight\":0,"
			+ "\"id\":1132232}}}";

}
