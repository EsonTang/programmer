package com.prize.prizeappoutad.utils;

import java.util.Properties;

import com.prize.prizeappoutad.activity.RSplashActivity;
import com.prize.prizeappoutad.constants.Constants;
import com.tencent.stat.StatService;

import android.content.Context;

/**
 * MTA 统计 工具类
 * 
 * @author huangchangguo 2016.11.14
 * 
 */
public class MTAUtils {

	private static final String MTA_ONADDOWNLOAD = "onAdClickDownload";
	private static final String MTA_ONADFAILD = "onAdFaild";
	private static final String MTA_ONADSTART = "onAdStart";
	private static final String MTA_LOCATION = "onLocation";
	private static final String MTA_JUMP_BANNER = "onJumpBanner";
	private static final String MTA_JUMP_SLOT = "onJumpSlot";
	private static final String MTA_CLICK_BANNER = "onClickBanner";
	private static final String MTA_CLICK_SLOT = "onClickSlot";
	private static final String MTA_SERVICE_CREATE = "onServiceCreate";
	private static final String MTA_SERVICE_COMMAND = "onServiceCommand";
	private static final String MTA_UPDATE_TIMES = "onUpdateTimes";

	/** 百度开屏广告展示失败次数统计 */
	public static void splashAdFailed(Context context) {
		Properties prop = new Properties();
		prop.setProperty("onAdFaild", "onAdFaild");
		StatService.trackCustomKVEvent(context, MTA_ONADFAILD, prop);
	}

	/** 统计服务重启的次数 */
	public static void onServiceCreate(Context context) {
		Properties prop = new Properties();
		prop.setProperty("onServiceCreate", "onServiceCreate");
		StatService.trackCustomKVEvent(context, MTA_SERVICE_CREATE, prop);
	}

	/** 统计服务调用的次数 */
	public static void onServiceCommand(Context context) {
		Properties prop = new Properties();
		prop.setProperty("onServiceCommand", "onServiceCommand");
		StatService.trackCustomKVEvent(context, MTA_SERVICE_COMMAND, prop);
	}

	/** 百度开屏广告展示成功次数统计 */
	public static void splashAdPresent(Context context) {
		Properties prop = new Properties();
		prop.setProperty("onAdStart", "onAdStart");
		StatService.trackCustomKVEvent(context, MTA_ONADSTART, prop);
	}

	/** 百度开屏广告被点击次数统计 */
	public static void splashAdDownload(Context context) {
		Properties prop = new Properties();
		prop.setProperty("onAdClick", "onAdDownload");
		StatService.trackCustomKVEvent(context, MTA_ONADDOWNLOAD, prop);
	}

	/** 百度定位 次数统计 */
	public static void BDLocation(Context context) {
		Properties prop = new Properties();
		prop.setProperty("onLocation", "onLocation");
		StatService.trackCustomKVEvent(context, MTA_LOCATION, prop);
	}

	/** banner广告跳转次数 */
	public static void onJumpBannerAd(Context context) {
		Properties prop = new Properties();
		prop.setProperty("onJumpBannerAd", "onJumpBannerAd");
		StatService.trackCustomKVEvent(context, MTA_JUMP_BANNER, prop);
	}

	/** banner广告点击次数 */
	public static void onClickBannerAd(Context context) {
		Properties prop = new Properties();
		prop.setProperty("onClickBannerAd", "onClickBannerAd");
		StatService.trackCustomKVEvent(context, MTA_CLICK_BANNER, prop);
	}

	/** 插屏广告跳转次数 */
	public static void onJumpSlotAd(Context context) {
		Properties prop = new Properties();
		prop.setProperty("onJumpSlotAd", "onJumpSlotAd");
		StatService.trackCustomKVEvent(context, MTA_JUMP_SLOT, prop);
	}

	/** 插屏广告点击次数 */
	public static void onClickSlotAd(Context context) {
		Properties prop = new Properties();
		prop.setProperty("onClickSlotAd", "onClickSlotAd");
		StatService.trackCustomKVEvent(context, MTA_CLICK_SLOT, prop);
	}

	/** 更新的次数 */
	public static void onUpdateTimes(Context context) {
		Properties prop = new Properties();
		prop.setProperty("onUpdateTimes", "onUpdateTimes");
		StatService.trackCustomKVEvent(context, MTA_UPDATE_TIMES, prop);
	}

}
