/*
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

package com.prize.app.net.datasource.base;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

/**
 * app详情实体bean，实现了Parcelable接口
 * 类名称：AppsItemBean
 * <p>
 * 创建人：longbaoxiu
 * <p>
 * 修改时间：2016年6月13日 下午2:07:32
 *
 * @version 1.0.0
 */
public class AppsItemBean implements Parcelable {

    public int appTypeId;
    public int versionCode = -1;
    public String name;
    /**
     * 分类名称
     *****/
    public String categoryName;
    /**
     * 包名
     *****/
    public String packageName;
    /**
     * 评分
     *****/
    public String rating;
    /**
     * 版本名称
     *****/
    public String versionName;
    /**
     * 小图标
     *****/
    public String iconUrl;
    /**
     * apk大小
     *****/
    public String apkSize;
    public String apkSizeFormat;
    /**
     * 1:首发；2：最新；3：热门；4：独家；5：推广；6：活动；7：特权
     *****/
    public String boxLabel;
    public String downloadTimes;
    /**
     * 下载地址
     *****/
    public String downloadUrl;
    /**
     * 更新时间
     *****/
    public String updateTime;
    /**
     * 更新信息
     *****/
    public String updateInfo;
    /**
     * 大图标
     *****/
    public String largeIcon;
    /**
     * 礼包个数
     *****/
    public int giftCount;
    public String downloadTimesFormat;
    public String apkMd5;
    public String id;
    public int isAd;
    /**
     * 广告url地址
     *****/
    public String bannerUrl;
    /**
     * 简介
     *****/
    public String brief;
    public String tag;
    /**
     * 副标题
     *****/
    public String subTitle;
    public int position;
    public String cardId;
    public int cardPosition;
    /**
     * 首發，独家标签
     *****/
    public String ourTag;

    /**
     * 2.0版本新增差分包
     ****/
    public AppPatch appPatch;
    /**
     * 2.0版本新增积分字段
     ****/
    public int points;
    /**
     * 2.0版本新增积分字段-已经领取
     ****/
    public int timesCount;
    /**
     * 是否被选中，同步恢复应用字段,默认选中
     */
    public boolean isCheck = true;

    /**
     * 2.2版本增加字段，表示页面标题
     */
    public String pageTitle;
    /**
     * 2.5版本增加字段，下载完成时间
     */
    public String dowloadedStamp;
    /**
     * 2.5版本增加字段，表示是安装还是更新
     */
    public String installType;
    /**
     * 2.7版本增加字段，所属界面信息
     */
    public String pageInfo;
    /**
     * 2.7版本增加字段，自定义标签
     */
    public String customTags;
    /**
     * 2.7版本增加字段，打点信息字段
     */
    public String backParams;


    /**
     * 2.4版本增加字段，0:应用不存在，1：应用存在；2：更新状态
     */
    public int istatus = 0;

    public void setPosition(int position) {
        this.position = position;
    }

    public int getPosition() {
        return position;
    }

    /**
     * 表示是否是推广应用 2.7 add
     */
    public boolean isAdvertise = false;
    /**
     * 表示是否显示推广图标1：是
     */
    public int adType;
    /**
     * 3.1 add;
     * 0 - 默认；
     * 1 - 不做静默更新；
     * 2 - 忽略激活，强制更新；
     * 3 - 忽略任何情况，强制更新；
     */
    public int silentStatus;
    /**
     * 3.1 add;
     * 0 - 未激活； 1 - 已激活
     */
    public int isActive;
    /**
     * 3.2 add;
     * 来源
     */
    public int sourceType;

    public AppsItemBean() {
    }


    @Override
    public String toString() {
        return "AppsItemBean{" +
                "versionCode=" + versionCode +
                ", name='" + name + '\'' +
                ", packageName='" + packageName + '\'' +
                ", id='" + id + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
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
        arg0.writeParcelable(appPatch, flags);

        arg0.writeString(dowloadedStamp);
        arg0.writeString(installType);
        arg0.writeString(pageInfo);
        arg0.writeString(backParams);
        arg0.writeString(customTags);

        arg0.writeInt(silentStatus);
        arg0.writeInt(isActive);
        arg0.writeInt(sourceType);

    }

    public static final Parcelable.Creator<AppsItemBean> CREATOR = new Parcelable.Creator<AppsItemBean>() {
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

        appPatch = arg0.readParcelable(AppPatch.class.getClassLoader());
        dowloadedStamp = arg0.readString();
        installType = arg0.readString();
        pageInfo = arg0.readString();
        backParams = arg0.readString();
        customTags = arg0.readString();

        silentStatus = arg0.readInt();
        isActive = arg0.readInt();
        sourceType = arg0.readInt();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null)
            return false;
        if (o instanceof AppsItemBean) {
            AppsItemBean st = (AppsItemBean) o;

            return !(TextUtils.isEmpty(packageName) || TextUtils.isEmpty(st.packageName)) && (packageName.equals(st.packageName));

//            if (TextUtils.isEmpty(packageName) || TextUtils.isEmpty(st.packageName))
//                return false;
//            return (packageName.equals(st.packageName));
        } else {
            return super.equals(o);
        }
    }
}
