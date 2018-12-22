package com.prize.prizeappoutad.bean;

/**
 * 广告跳转详情信息
 * 
 * @author huangchangguo 2016.11.14
 * 
 */
public class AdDetailsInfo {
	// "detailType": 1(1是跳转URL，2是下载),
	public int detailType;
	// "jumpUrl": "http://img.szprize.cn/appstore.html",
	public String jumpUrl;
	// "appInfo":(注：这里是应用的字段。
	// 如果广告是下载应用detailType=2，则jumpUrl为null。
	// 如果是 detailType=1，则跳转html，appInfo为null)
	public AppsItemBean appInfo;

}
