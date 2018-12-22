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

package com.prize.prizenavigation.bean;

import android.os.Parcel;
import android.os.Parcelable;

/***
 * 
 *app详情实体bean，实现了Parcelable接口
 * 类名称：AppsItemBean
 * 
 * 创建人：longbaoxiu
 * 
 * 修改时间：2016年6月13日 下午2:07:32
 * 
 * @version 1.0.0
 *
 */
public class AppsItemBean implements Parcelable {

	public int appTypeId;
	public int versionCode = -1;
	public String name;
	/****分类名称*****/
	public String categoryName;
	/****包名*****/
	public String packageName;
	/****评分*****/
	public String rating;
	/****版本名称*****/
	public String versionName;
	/****小图标*****/
	public String iconUrl;
	/****apk大小*****/
	public String apkSize;
	public String apkSizeFormat;
	/***1:首发；2：最新；3：热门；4：独家；5：推广；6：活动；7：特权*****/
	public String boxLabel;
	public String downloadTimes;
	/****下载地址*****/
	public String downloadUrl;
	/****更新时间*****/
	public String updateTime;
	/***更新信息*****/
	public String updateInfo;
	/****大图标*****/
	public String largeIcon;
	/****礼包个数*****/
	public int giftCount;
	public String downloadTimesFormat;
	public String apkMd5;
	public String id;
	public int isAd;
	/****广告url地址*****/
	public String bannerUrl;
	/****简介*****/
	public String brief;
	public String tag;
	/****副标题*****/
	public String subTitle;
	public int position;
	public String cardId;
	public int cardPosition;
	
	public String ourTag;
	/****下载地址*****/
	public String downloadUrlCdn;
	
//	public String publishstatus;
//	public String brand;
//	public String packagename;
//	public String iconurl;
//	public String versionname;
//	public int versioncode;
//	public String downloadtimes;
//	public String downloadurlcdn;
//	public String developer;
//	public String downloadurl;
//	public String apkmd5;
//	public String apksize;
//	public String updatetime;
//	public String updateinfo;
	

	public void setPosition(int position) {
		this.position = position;
	}

	public int getPosition() {
		return position;
	}


	public AppsItemBean() {
	}

	@Override
	public String toString() {
		return "AppsItemBean [appTypeId=" + appTypeId + ", versionCode="
				+ versionCode + ", name=" + name + ", categoryName="
				+ categoryName + ", packageName=" + packageName + ", rating="
				+ rating + ", versionName=" + versionName + ", iconUrl="
				+ iconUrl + ", apkSize=" + apkSize + ", apkSizeFormat="
				+ apkSizeFormat + ", boxLabel=" + boxLabel + ", downloadTimes="
				+ downloadTimes + ", downloadUrl=" + downloadUrl
				+ ", updateTime=" + updateTime + ", downloadTimesFormat="
				+ downloadTimesFormat + ", id=" + id + ", headerId=" + "]";
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel arg0, int flags) {
		arg0.writeInt(appTypeId);
		arg0.writeInt(versionCode);
		arg0.writeString(name);
		
		arg0.writeString(categoryName);
		arg0.writeString(packageName);
		arg0.writeString(rating);
		
		arg0.writeString(versionName);
		arg0.writeString(iconUrl);
		arg0.writeString(apkSize);
		
		arg0.writeString(apkSizeFormat);
		arg0.writeString(boxLabel);
		arg0.writeString(downloadTimes);
		
		arg0.writeString(downloadUrl);
		arg0.writeString(updateTime);
		arg0.writeString(updateInfo);
		
		arg0.writeString(largeIcon);
		arg0.writeInt(giftCount);
		arg0.writeString(downloadTimesFormat);
		
		arg0.writeString(apkMd5);
		arg0.writeString(id);
		arg0.writeInt(isAd);
		
		arg0.writeString(bannerUrl);
		arg0.writeString(brief);
		arg0.writeString(tag);
		
		arg0.writeString(subTitle);
		arg0.writeInt(position);
		arg0.writeString(cardId);
		
		arg0.writeInt(cardPosition);
		arg0.writeString(ourTag);

		arg0.writeString(downloadUrlCdn);

	}

	public static final Creator<AppsItemBean> CREATOR = new Creator<AppsItemBean>() {
		public AppsItemBean createFromParcel(Parcel in) {
			return new AppsItemBean(in);
		}

		public AppsItemBean[] newArray(int size) {
			return new AppsItemBean[size];
		}
	};

	public AppsItemBean(Parcel arg0) {
		appTypeId = arg0.readInt();
		versionCode = arg0.readInt();
		name = arg0.readString();
		
		categoryName = arg0.readString();
		packageName = arg0.readString();
		rating = arg0.readString();
		
		versionName = arg0.readString();
		iconUrl = arg0.readString();
		apkSize = arg0.readString();
		
		apkSizeFormat = arg0.readString();
		boxLabel = arg0.readString();
		downloadTimes = arg0.readString();
		
		downloadUrl = arg0.readString();
		updateTime = arg0.readString();
		updateInfo = arg0.readString();
		
		largeIcon = arg0.readString();	
		giftCount = arg0.readInt();
		downloadTimesFormat = arg0.readString();
		
		apkMd5 = arg0.readString();
		id = arg0.readString();
		isAd = arg0.readInt();
		
		bannerUrl = arg0.readString();
		brief = arg0.readString();
		tag = arg0.readString();
		
		subTitle = arg0.readString();
		position = arg0.readInt();
		cardId = arg0.readString();
		
		cardPosition = arg0.readInt();
		ourTag = arg0.readString();
	}
}
