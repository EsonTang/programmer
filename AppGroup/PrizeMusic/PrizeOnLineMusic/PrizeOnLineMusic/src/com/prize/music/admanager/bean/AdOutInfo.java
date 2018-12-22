package com.prize.music.admanager.bean;

/**
 * 启动页广告bean
 * 
 * @author huangchangguo 2017.5.20
 * 
 */
public class AdOutInfo {
	// id
	public String id = "0";
	// "typeId": 1(广告类型：1是横幅的广告，2是插屏的广告)
	public String typeId;
	// "tag": "这是备用的字段一，默认值为null",这个广告二期是传弹广告的次数
	public String tag;
	// "subTag": "这是两个备用的字段二，默认值为null",
	public String subTag;
	// "adSource": own
	public String adSource;
	// "adBanner":
	// "http://img.szprize.cn/appstore/cat/休闲益智_1467085483546_5.png（adbannner:图片地址，始终不为空)",
	public String adBanner;
	// 广告详情
	public AdDetailsInfo adDetails;
	@Override
	public String toString() {
		return "AdOutInfo [id=" + id + ", typeId=" + typeId + ", tag=" + tag + ", subTag=" + subTag + ", adSource="
				+ adSource + ", adBanner=" + adBanner + ", adDetails=" + adDetails + "]";
	}
	
	

}
