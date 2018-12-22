package com.prize.app.net.datasource.base;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 差分包字段
 * Created by longbaoxiu on 2016/8/11.
 */
public class AppPatch implements Parcelable {
    public int appId;
    public String packageName;
    public int fromVersion;
    public int toVersion;
    public String fromApkMd5;
    public String toApkMd5;
    /**下载地址***/
    public String patchUrl;
    public long patchSize;
    public String createTime;
    public int id;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel arg0, int flags) {
        arg0.writeInt(appId);
        arg0.writeString(packageName);
        arg0.writeInt(fromVersion);
        arg0.writeInt(toVersion);

        arg0.writeString(fromApkMd5);
        arg0.writeString(toApkMd5);
        arg0.writeString(patchUrl);
        arg0.writeLong(patchSize);

        arg0.writeString(createTime);
        arg0.writeInt(id);

    }
    public AppPatch(){

    }
    public AppPatch(Parcel arg0){
        appId=arg0.readInt();
        packageName=arg0.readString();
        fromVersion=arg0.readInt();
        toVersion=arg0.readInt();

        fromApkMd5=arg0.readString();
        toApkMd5=arg0.readString();
        patchUrl=arg0.readString();
        patchSize=arg0.readLong();


        createTime=arg0.readString();
        id=arg0.readInt();
    }
    public static final Parcelable.Creator<AppPatch> CREATOR = new Parcelable.Creator<AppPatch>() {
        public AppPatch createFromParcel(Parcel in) {
            return new AppPatch(in);
        }

        public AppPatch[] newArray(int size) {
            return new AppPatch[size];
        }
    };
}
