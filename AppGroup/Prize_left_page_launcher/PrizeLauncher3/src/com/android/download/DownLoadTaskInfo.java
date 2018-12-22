package com.android.download;

import android.os.Parcel;
import android.os.Parcelable;

public class DownLoadTaskInfo implements Parcelable {
	public int progress;
	public int state;
	public String iconUrl;
	public String title;
	public String pkgName;
	public int container=-2;

	/**
	 * seems meaningless return 0;
	 */
	@Override
	public int describeContents() {
		return 0;
	}

	public DownLoadTaskInfo() {
		super();
	}

	/**
	 * <默认构造函数>
	 */
	public DownLoadTaskInfo(Parcel in) {
		// 注意顺序
		progress = in.readInt();
		state = in.readInt();
		iconUrl = in.readString();
		title = in.readString();
		pkgName = in.readString();
		container = in.readInt();
	}

	/**
	 * 将对象序列化为一个Parcel对象 可以将Parcel看成是一个流，通过writeToParcel把对象写到流里面,
	 * 再通过createFromParcel从流里读取对象 注意:写的顺序和读的顺序必须一致。
	 */
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(progress);
		dest.writeInt(state);
		dest.writeString(iconUrl);
		dest.writeString(title);
		dest.writeString(pkgName);
		dest.writeInt(container);
	}

	@Override
	public String toString() {
		return "DownLoadTaskInfo [progress=" + progress + ", state=" + state
				+ ", iconUrl=" + iconUrl + ", title=" + title + ", pkgName="
				+ pkgName + ", container=" + container + "]";
	}

	/**
	 * 实例化静态内部对象CREATOR实现接口Parcelable.Creator public static
	 * final一个都不能少，内部对象CREATOR的名称也不能改变，必须全部大写
	 */
	public static final Parcelable.Creator<DownLoadTaskInfo> CREATOR = new Creator<DownLoadTaskInfo>() {

		// 将Parcel对象反序列化为HarlanInfo
		@Override
		public DownLoadTaskInfo createFromParcel(Parcel source) {
			DownLoadTaskInfo info = new DownLoadTaskInfo(source);
			return info;
		}

		@Override
		public DownLoadTaskInfo[] newArray(int size) {
			return new DownLoadTaskInfo[size];
		}

	};
}
