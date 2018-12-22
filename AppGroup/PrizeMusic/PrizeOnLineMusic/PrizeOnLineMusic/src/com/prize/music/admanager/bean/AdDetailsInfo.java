package com.prize.music.admanager.bean;

import java.util.ArrayList;

import com.prize.app.net.datasource.base.AppsItemBean;

/**
 * 启动页广告跳转详情信息
 *
 * @author huangchangguo 2017.5.20
 */
public class AdDetailsInfo {

    //APK名
    public String apkName;
    //包名
    public String packageName;
    // "detailType": 1(1是跳转URL，2是下载),三方广告添加：3，下载APK的地址
    public int detailType = -1;
    // "jumpUrl": "http://img.szprize.cn/appstore.html",
    public String       jumpUrl;
    // "appInfo":(注：这里是应用的字段。
    // 如果广告是下载应用detailType=2，则jumpUrl为null。
    // 如果是 detailType=1，则跳转html，appInfo为null)
    public AppsItemBean appInfo;
    
    public String appId;
    //验证是不是deeplink的
    public String apkMd5;
    /**曝光回传地址*/
    public ArrayList<String> impr_url;
    /**点击回传地址*/
    public ArrayList<String> click_url;
    /**下载开始回传地址*/
    public ArrayList<String>  inst_downstart_url;
    /**下载成功回传地址 */
    public ArrayList<String>  inst_downsucc_url;
    /**APK安装回传地址 */
    public ArrayList<String>  inst_installstart_url;
    /**APK安装成功回传地址 */
    public ArrayList<String>  inst_installsucc_url;
	@Override
	public String toString() {
		return "AdDetailsInfo [apkName=" + apkName + ", packageName=" + packageName + ", detailType=" + detailType
				+ ", jumpUrl=" + jumpUrl + ", appInfo=" + appInfo + ", appId=" + appId + ", apkMd5=" + apkMd5
				+ ", impr_url=" + impr_url + ", click_url=" + click_url + ", inst_downstart_url=" + inst_downstart_url
				+ ", inst_downsucc_url=" + inst_downsucc_url + ", inst_installstart_url=" + inst_installstart_url
				+ ", inst_installsucc_url=" + inst_installsucc_url + "]";
	}

    
    
    
}
