package com.prize.app.net.datasource.base;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * 一键装机实体bean
 */
public class AppsKeyInstallingPageItemBean implements Parcelable {
    public String title;
    public String iconUrl;
    public String color;
    public int checkedCnt;
    public ArrayList<AppsItemBean> apps = new ArrayList<AppsItemBean>();

    public ArrayList<AppsItemBean> getApps() {
        return apps;
    }

    public AppsKeyInstallingPageItemBean() {
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
            /*for (AppsItemBean app : apps) {
                sb.append(app.toString());
				sb.append("\n");
			}*/
        sb.append("app size = " + apps.size());
        sb.append("\n");
        sb.append(apps.get(2).toString());
        return sb.toString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel arg0, int i) {
        arg0.writeString(color);
        arg0.writeString(title);
        arg0.writeString(iconUrl);
        arg0.writeList(apps);
        arg0.writeInt(checkedCnt);
    }

    public static final Parcelable.Creator<AppsKeyInstallingPageItemBean> CREATOR = new Parcelable.Creator<AppsKeyInstallingPageItemBean>() {
        public AppsKeyInstallingPageItemBean createFromParcel(Parcel in) {
            return new AppsKeyInstallingPageItemBean(in);
        }

        public AppsKeyInstallingPageItemBean[] newArray(int size) {
            return new AppsKeyInstallingPageItemBean[size];
        }
    };

    public AppsKeyInstallingPageItemBean(Parcel arg0) {
        color = arg0.readString();
        title = arg0.readString();
        iconUrl = arg0.readString();
        apps = arg0.readArrayList(AppsItemBean.class.getClassLoader());
        checkedCnt = arg0.readInt();
    }
}