package com.prize.appcenter.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 推送通知返回的数据
 *  longbaoxiu
 *  2016/11/7.10:47
 *
 */

public class PushValueBean implements Parcelable {
    /**
     * 搜索关键字 -3.1版本 add
     */
    public String word;
    /**
     * 是否下载  3.1add
     */
    public boolean download = false;
    /**
     * 应用id  3.1 add
     */
    public String appId;
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel arg0, int flags) {
        arg0.writeString(word);
        arg0.writeString(appId);
        arg0.writeByte((byte) (download ? 1 : 0));
    }

    public static final Creator<PushValueBean> CREATOR = new Creator<PushValueBean>() {
        public PushValueBean createFromParcel(Parcel in) {
            return new PushValueBean(in);
        }

        public PushValueBean[] newArray(int size) {
            return new PushValueBean[size];
        }
    };

    public PushValueBean(Parcel arg0) {
        word = arg0.readString();
        appId = arg0.readString();
        download = arg0.readByte() != 0;
    }
    public PushValueBean() {
    }

}
