package com.prize.appcenter.bean;

import android.os.Parcel;
import android.os.Parcelable;

import com.prize.app.net.datasource.base.AppsItemBean;

import java.util.ArrayList;
import java.util.List;

/**
 * 推送通知返回的数据
 *  longbaoxiu
 *  2016/11/7.10:47
 *
 */

public class PushDataBean  implements Parcelable {
    /**AppsItemBean**/
    public AppsItemBean app;
    public PushValueBean qiho;
    public List<AppsItemBean> apps=new ArrayList<>();
    /**爱奇艺的视频字段**/
    public String aid;
    /**爱奇艺的视频字段**/
    public String tid;
    /**内置QQ浏览器和今日头条跳转uir字段**/
    public String uri;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel arg0, int flags) {
        arg0.writeParcelable(app,flags);
        arg0.writeList(apps);
        arg0.writeString(aid);
        arg0.writeString(tid);
        arg0.writeString(uri);
        arg0.writeParcelable(qiho,flags);
    }

    public static final Parcelable.Creator<PushDataBean> CREATOR = new Parcelable.Creator<PushDataBean>() {
        public PushDataBean createFromParcel(Parcel in) {
            return new PushDataBean(in);
        }

        public PushDataBean[] newArray(int size) {
            return new PushDataBean[size];
        }
    };

    public PushDataBean(Parcel arg0) {

        app=arg0.readParcelable(AppsItemBean.class.getClassLoader());
        apps=arg0.readArrayList(AppsItemBean.class.getClassLoader());
        aid=arg0.readString();
        tid=arg0.readString();
        uri=arg0.readString();
        qiho=arg0.readParcelable(PushValueBean.class.getClassLoader());
    }
    public PushDataBean() {
    }

    @Override
    public String toString() {
        return "PushDataBean{" +
                "app=" + app +
                ", apps=" + apps +
                ", aid='" + aid + '\'' +
                ", tid='" + tid + '\'' +
                ", uri='" + uri + '\'' +
                '}';
    }
}
