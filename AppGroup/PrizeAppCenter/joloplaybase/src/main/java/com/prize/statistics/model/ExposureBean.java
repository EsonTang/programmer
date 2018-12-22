package com.prize.statistics.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.prize.app.util.CommonUtils;

/**
 * 曝光统计bean
 * <p>
 * longbaoxiu
 * 2017/5/11.13:49
 */

public class ExposureBean implements Parcelable {
    /**
     * 界面：通知栏（notificationbar）、启动页（startuppage）、推荐页（recommendpage）、应用页（apppage）、游戏页（gamepage）
     */
    public String gui;
    /**
     * 控件：列表（list）、列表->焦点图（list-focus）
     */
    public String widget;
    /**
     * 应用id 3.2add
     */
    public String appId;
    /**
     * 应用名称 3.2add
     */
    public String appName;
    /**
     * 应用包名 3.2add
     */
    public String packageName;
    /**
     * 来源 3.2add
     */
    public int sourceType=-1;
    /**
     * 代理名称即渠道号（下载数据才有）3.2add
     */
    public String agency;
//    /**
//     * 位置：1、2、3、4
//     */
//    public String position;
    /**
     * 应用（app）、焦点图（focus）
     */
    public String type;
    /**
     * 主键（id）
     */
    public String datas;
    /**
     * 标题（应用名、focus标题）
     */
    public String title;
    /**
     * 数据标记
     */
    public String backParams;

//    /**
//     * 所在一级页面位置：1、2、3、4
//     */
//    public String child_position;
    /**
     * 所在一级页面id
     */
    public String parent_datas;
    /**
     * 所在一级页面类型
     */
    public String parent_type;

//    @Override
//    public String toString() {
//        return "ExposureBean{" +
//                "position='" + position + '\'' +
//                ", datas='" + datas + '\'' +
//                ", title='" + title + '\'' +
//                ", parent_datas='" + parent_datas + '\'' +
//                '}';
//    }

    @Override
    public String toString() {
        return "ExposureBean{" +
                "gui='" + gui + '\'' +
                ", widget='" + widget + '\'' +
                ", appName='" + CommonUtils.unicode2String(appName) + '\'' +
                ", title='" + title + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof ExposureBean) {
            ExposureBean st = (ExposureBean) o;
            if (!TextUtils.isEmpty(appId) && !TextUtils.isEmpty(st.packageName)) {
                return (appId.equals(st.appId) && packageName.equals(st.packageName));
            } else {
                return !TextUtils.isEmpty(datas) && !TextUtils.isEmpty(st.title) && (datas.equals(st.datas) && title.equals(st.title));
            }
        } else {
            return false;
        }
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.gui);
        dest.writeString(this.widget);
        dest.writeString(this.appId);
        dest.writeString(this.appName);
        dest.writeString(this.packageName);
        dest.writeInt(this.sourceType);
        dest.writeString(this.agency);
//        dest.writeString(this.position);
        dest.writeString(this.type);
        dest.writeString(this.datas);
        dest.writeString(this.title);
        dest.writeString(this.backParams);
//        dest.writeString(this.child_position);
        dest.writeString(this.parent_datas);
        dest.writeString(this.parent_type);
    }

    public ExposureBean() {
    }

    protected ExposureBean(Parcel in) {
        this.gui = in.readString();
        this.widget = in.readString();
        this.appId = in.readString();
        this.appName = in.readString();
        this.packageName = in.readString();
        this.sourceType = in.readInt();
        this.agency = in.readString();
//        this.position = in.readString();
        this.type = in.readString();
        this.datas = in.readString();
        this.title = in.readString();
        this.backParams = in.readString();
//        this.child_position = in.readString();
        this.parent_datas = in.readString();
        this.parent_type = in.readString();
    }

    public static final Parcelable.Creator<ExposureBean> CREATOR = new Parcelable.Creator<ExposureBean>() {
        @Override
        public ExposureBean createFromParcel(Parcel source) {
            return new ExposureBean(source);
        }

        @Override
        public ExposureBean[] newArray(int size) {
            return new ExposureBean[size];
        }
    };
}

