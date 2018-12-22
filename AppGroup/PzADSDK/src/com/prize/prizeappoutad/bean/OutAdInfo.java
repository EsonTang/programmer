package com.prize.prizeappoutad.bean;

/**
 * 应用外广告bean
 * 
 * @author huangchangguo 2016.11.14
 * 
 */
public class OutAdInfo {
	// "typeId": 1(广告类型：1是横幅的广告，2是插屏的广告)
	public int typeId;
	// "tag": "这是备用的字段一，默认值为null",
	public String tag;
	// "subTag": "这是两个备用的字段二，默认值为null",
	public String subTag;
	// "adSource": "广告商",
	public String adSource;
	// "adBanner":
	// "http://img.szprize.cn/appstore/cat/休闲益智_1467085483546_5.png（adbannner:图片地址，始终不为空)",
	public String adBanner;
	// 广告详情
	public AdDetailsInfo adDetails;

}
