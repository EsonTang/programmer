package com.prize.app.database.beans;

import android.os.Parcel;
import android.os.Parcelable;

public class HomeRecord  implements Parcelable {
	/** 标题或名称 */
	public String name;
	// /** 类型 */
	public int type;
	/** code */
	public String id;
	/** 图片地址 */
	public String iconUrl;
	/** 简介 */
	public String desc;
	public static final int CONTENT_TYPE_AD = 1;
	public static final int CONTENT_TYPE_NOTICE = 2;
	public static final int CONTENT_TYPE_LIST = 3;
	public static final int CONTENT_TYPE_NAVBLOCKS = 5;
	public static final int CONTENT_TYPE_COMMAND_HOTWORDS = CONTENT_TYPE_NAVBLOCKS + 1;
	/** 游戏包名 */
	public String packageName;
	/** apk大小 */
	public String apkSize;
	/** 下载次数 */
	public String downloadTimes;

	// public String gameActivity;
	// public String gameClass;
	public String downloadUrl;
	public int versionCode;
	public String apkMd5;
	/** 评分 */
	public String rating;
	/** 专题或者app */
	public String adType;
	/** 推荐栏的关键词 */
	public String key;
	public String apkSizeFormat;
	public int content_type;
	public int giftCount;
	public String categoryName;
	public String json;
	public HomeRecord() {
	}
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(json);
	}

	public static final Parcelable.Creator<HomeRecord> CREATOR = new Parcelable.Creator<HomeRecord>() {
		public HomeRecord createFromParcel(Parcel in) {
			return new HomeRecord(in);
		}

		public HomeRecord[] newArray(int size) {
			return new HomeRecord[size];
		}
	};


	public HomeRecord(Parcel arg0) {
		json = arg0.readString();
	}
}
