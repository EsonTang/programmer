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

package com.prize.app.net.datasource.base;

import java.io.Serializable;

public class AppsItemBean implements Serializable {

	/** 用一句话描述这个变量表示什么 */
	private static final long serialVersionUID = 1L;

	public int appTypeId;
	public int versionCode = -1;
	public String name;
	public String categoryName;
	public String packageName;
	public String rating;
	public String versionName;
	public String iconUrl;
	public String apkSize;
	public String apkSizeFormat;
	public String boxLabel;
	public String downloadTimes;
	public String downloadUrl;
	public String updateTime;
	public String updateInfo;
	public String largeIcon;
	public int giftCount;
	public String downloadTimesFormat;
	public String apkMd5;
	public String id;
	public int isAd;
	public String bannerUrl;
	public String brief;
	public String tag;
	public String subTitle;
	public int position;
    private String channel;
    private String brand;
    private int publishStatus;
    private String developer;
    private String downloadUrlCdn;
    
//	  "data": {
//    "app": {
//      "channel": "",
//      "publishStatus": 1,
//      "brand": "",


//      "developer": "铂睿智恒",
//      "downloadUrlCdn": "http://img.szprize.cn/music/apps/com.prize.music/1459824867868.apk",
//    }
//  }
    
	@Override
	public String toString() {
		return "AppsItemBean [appTypeId=" + appTypeId + ", versionCode="
				+ versionCode + ", name=" + name + ", categoryName="
				+ categoryName + ", packageName=" + packageName + ", rating="
				+ rating + ", versionName=" + versionName + ", iconUrl="
				+ iconUrl + ", apkSize=" + apkSize + ", apkSizeFormat="
				+ apkSizeFormat + ", boxLabel=" + boxLabel + ", downloadTimes="
				+ downloadTimes + ", downloadUrl=" + downloadUrl
				+ ", updateTime=" + updateTime + ", updateInfo=" + updateInfo
				+ ", largeIcon=" + largeIcon + ", giftCount=" + giftCount
				+ ", downloadTimesFormat=" + downloadTimesFormat + ", apkMd5="
				+ apkMd5 + ", id=" + id + ", isAd=" + isAd + ", bannerUrl="
				+ bannerUrl + ", brief=" + brief + ", tag=" + tag
				+ ", subTitle=" + subTitle + ", position=" + position
				+ ", channel=" + channel + ", brand=" + brand
				+ ", publishStatus=" + publishStatus + ", developer="
				+ developer + ", downloadUrlCdn=" + downloadUrlCdn + "]";
	}
	
	/**
	 * 每个Item对应的HeaderId
	 */

}
