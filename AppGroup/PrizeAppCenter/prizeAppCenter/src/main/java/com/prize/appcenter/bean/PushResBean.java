package com.prize.appcenter.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 推送通知返回的数据
 * longbaoxiu
 * 2016/11/7.10:47
 */

public class PushResBean implements Parcelable {
    /**展示UI类型**/
    public String uiType;
    /**展示小图**/
    public String iconUrl;
    /**展示Banner **/
    public String bannerUrl;
    /**展示标题**/
    public String title;
    /**展示内容**/
    public String content;
    /**类型**/
    public String type;
    /**展示内容**/
    public String value;
    /**通知是否允许滑动删除 0:不允许 1：允许**/
    public int allowDelete;
    /**是否允许Toast 0:不允许 1：允许**/
    public int allowToast;
    /**客户端的push开关控制 1忽略 0不**/
    public int allowHowever;
    public int id;

    /**Toast消息**/
    public String toast;

    /**服务端返回html格式的标题，含有字体颜色大小粗细等  --2.5版本 add*/
    public String titleHtml;

    /**服务端返回是否显示退推送时间  1：显示，其余都不显示 --2.5版本 add*/
    public int allowTime;

    /**是否允许Toast 0:不允许 1：允许 -2.5版本 add*/
    public int allowLayer;
    public PushDataBean data = new PushDataBean();

    @Override
    public String toString() {
        return "PushResBean{" +
                "uiType='" + uiType + '\'' +
                ", iconUrl='" + iconUrl + '\'' +
                ", bannerUrl='" + bannerUrl + '\'' +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", type='" + type + '\'' +
                ", value='" + value + '\'' +
                ", allowToast=" + allowToast +
                ", id=" + id +
                ", allowDelete=" + allowDelete +
                ", toast='" + toast + '\'' +
                ", data=" + data +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel arg0, int flags) {

        arg0.writeString(uiType);
        arg0.writeString(iconUrl);
        arg0.writeString(bannerUrl);
        arg0.writeString(title);
        arg0.writeString(content);
        arg0.writeString(type);
        arg0.writeString(value);
        arg0.writeInt(allowToast);
        arg0.writeInt(allowDelete);
        arg0.writeString(toast);
        arg0.writeParcelable(data,flags);

        arg0.writeInt(id);
        arg0.writeInt(allowTime);
        arg0.writeString(titleHtml);
        arg0.writeInt(allowLayer);

    }
    public static final Parcelable.Creator<PushResBean> CREATOR = new Parcelable.Creator<PushResBean>() {
        public PushResBean createFromParcel(Parcel in) {
            return new PushResBean(in);
        }

        public PushResBean[] newArray(int size) {
            return new PushResBean[size];
        }
    };
    public PushResBean(){

    }
    public PushResBean(Parcel arg0) {
        uiType = arg0.readString();
        iconUrl = arg0.readString();
        bannerUrl = arg0.readString();
        title = arg0.readString();
        content = arg0.readString();
        type = arg0.readString();
        value = arg0.readString();
        allowToast = arg0.readInt();
        allowDelete = arg0.readInt();
        toast = arg0.readString();
        data=arg0.readParcelable(PushDataBean.class.getClassLoader());

        id=arg0.readInt();
        allowTime=arg0.readInt();
        titleHtml = arg0.readString();
        allowLayer=arg0.readInt();

    }
}
