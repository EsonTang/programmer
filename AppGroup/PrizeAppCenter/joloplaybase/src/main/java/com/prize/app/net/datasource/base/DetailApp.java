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

public class DetailApp implements Serializable {
	public String name;
	public String packageName;
	public String minVersion;
	public String categoryName;
	public String developer;
	public String iconUrl;
	public String largeIcon;
	public String screenshotsUrl;
	public float rating;
	public String versionName;
	public int versionCode;
	public long apkSize;
	public int boxLabel;
	public String priceInfo;
	public String tag;
	public long downloadTimes;
	public String downloadUrl;
	public String rDownloadUrl;
	public String apkMd5;
	public String brief;
	public String description;
	public String updateInfo;
	public String appPermission;
	public String createTime;
	public String updateTime;
	public int totalComments;
	public String language;
	public String signatureMd5;
	public String downloadTimesFormat;
	public String apkSizeFormat;
	public int isAd;
	public String id;
	private static final long serialVersionUID = 1L;
	//1.2version  add by huanglingjun 2015-12-28	
	public int activates;
	public int appTypeId;
	public int catId;
	public String downloadUrl360;
	public String downloadUrlCdn;
	public String downloadUrlOss;
	public String bannerUrl;
	public String vedioUrl;
	public int giftCount;
	public int hotValue;
	public int rank;
	/**1.9版本增加，用于请求相关推荐及大家喜欢***/
	public int subCatId;
	public String ourTag;
	/**3.2版本**/
	public int sourceType;

	@Override
	public String toString() {
		return "DetailApp [name=" + name + ", packageName=" + packageName
				+ ", minVersion=" + minVersion + ", categoryName="
				+ categoryName + ", developer=" + developer + ", iconUrl="
				+ iconUrl + ", largeIcon=" + largeIcon + ", screenshotsUrl="
				+ screenshotsUrl + ", rating=" + rating + ", versionName="
				+ versionName + ", versionCode=" + versionCode + ", apkSize="
				+ apkSize + ", boxLabel=" + boxLabel + ", priceInfo="
				+ priceInfo + ", tag=" + tag + ", downloadTimes="
				+ downloadTimes + ", downloadUrl=" + downloadUrl
				+ ", rDownloadUrl=" + rDownloadUrl + ", apkMd5=" + apkMd5
				+ ", brief=" + brief + ", description=" + description
				+ ", updateInfo=" + updateInfo + ", appPermission="
				+ appPermission + ", createTime=" + createTime
				+ ", updateTime=" + updateTime + ", totalComments="
				+ totalComments + ", language=" + language + ", signatureMd5="
				+ signatureMd5 + ", downloadTimesFormat=" + downloadTimesFormat
				+ ", apkSizeFormat=" + apkSizeFormat + ", id=" + id + "]";
	}

}
