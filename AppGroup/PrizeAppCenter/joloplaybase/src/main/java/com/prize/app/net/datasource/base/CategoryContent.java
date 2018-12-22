package com.prize.app.net.datasource.base;

import android.os.Parcel;
import android.os.Parcelable;

public class CategoryContent implements Parcelable {

	public CategoryContent() {
		// TODO Auto-generated constructor stub
	}
	/** id */
	public String keyId;
	/** 内容 */
	public String subTag;

	@Override
	public int describeContents() {
		return 0;
	}

	public CategoryContent(String keyId, String subTag) {
		this.keyId = keyId;
		this.subTag = subTag;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(keyId);
		dest.writeString(subTag);
	}

	public static final Parcelable.Creator<CategoryContent> CREATOR = new Parcelable.Creator<CategoryContent>() {
		public CategoryContent createFromParcel(Parcel in) {
			return new CategoryContent(in);
		}

		public CategoryContent[] newArray(int size) {
			return new CategoryContent[size];
		}
	};

	public CategoryContent(Parcel arg0) {
		keyId = arg0.readString();
		subTag= arg0.readString();
	}
}
