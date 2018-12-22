package com.prize.boot.util;

import java.util.List;

/**
 * Created by Administrator on 2016/6/27.
 */

public class OneKeyInstallData {

    private String code;
    private String msg;

    private DataEntity data;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public DataEntity getData() {
        return data;
    }

    public void setData(DataEntity data) {
        this.data = data;
    }

    public static class DataEntity {
        private int pageCount;
        private int pageIndex;
        private int pageSize;
        private int pageItemCount;

        private List<AppsEntity> apps;

        public int getPageCount() {
            return pageCount;
        }

        public void setPageCount(int pageCount) {
            this.pageCount = pageCount;
        }

        public int getPageIndex() {
            return pageIndex;
        }

        public void setPageIndex(int pageIndex) {
            this.pageIndex = pageIndex;
        }

        public int getPageSize() {
            return pageSize;
        }

        public void setPageSize(int pageSize) {
            this.pageSize = pageSize;
        }

        public int getPageItemCount() {
            return pageItemCount;
        }

        public void setPageItemCount(int pageItemCount) {
            this.pageItemCount = pageItemCount;
        }

        public List<AppsEntity> getApps() {
            return apps;
        }

        public void setApps(List<AppsEntity> apps) {
            this.apps = apps;
        }

        public static class AppsEntity {
            private int appTypeId;
            private String name;
            private String packageName;
            private String categoryName;
            private double rating;
            private String tag;
            private String versionName;
            private String iconUrl;
            private String largeIcon;
            private int apkSize;
            private String apkSizeFormat;
            private int boxLabel;
            private int downloadTimes;
            private String downloadTimesFormat;
            private String downloadUrl;
            private int versionCode;
            private String updateInfo;
            private String apkMd5;
            private String brief;
            private String updateTime;
            private int giftCount;
            private int isAd;
            private Object bannerUrl;
            private int statusType;
            private int sourceType;
            private Object subTitle;
            private int sourceId;
            private Object searchDownloadPecent;
            private Object ourTag;
            private int weight;
            private int id;

            public int getAppTypeId() {
                return appTypeId;
            }

            public void setAppTypeId(int appTypeId) {
                this.appTypeId = appTypeId;
            }

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public String getPackageName() {
                return packageName;
            }

            public void setPackageName(String packageName) {
                this.packageName = packageName;
            }

            public String getCategoryName() {
                return categoryName;
            }

            public void setCategoryName(String categoryName) {
                this.categoryName = categoryName;
            }

            public double getRating() {
                return rating;
            }

            public void setRating(double rating) {
                this.rating = rating;
            }

            public String getTag() {
                return tag;
            }

            public void setTag(String tag) {
                this.tag = tag;
            }

            public String getVersionName() {
                return versionName;
            }

            public void setVersionName(String versionName) {
                this.versionName = versionName;
            }

            public String getIconUrl() {
                return iconUrl;
            }

            public void setIconUrl(String iconUrl) {
                this.iconUrl = iconUrl;
            }

            public String getLargeIcon() {
                return largeIcon;
            }

            public void setLargeIcon(String largeIcon) {
                this.largeIcon = largeIcon;
            }

            public int getApkSize() {
                return apkSize;
            }

            public void setApkSize(int apkSize) {
                this.apkSize = apkSize;
            }

            public String getApkSizeFormat() {
                return apkSizeFormat;
            }

            public void setApkSizeFormat(String apkSizeFormat) {
                this.apkSizeFormat = apkSizeFormat;
            }

            public int getBoxLabel() {
                return boxLabel;
            }

            public void setBoxLabel(int boxLabel) {
                this.boxLabel = boxLabel;
            }

            public int getDownloadTimes() {
                return downloadTimes;
            }

            public void setDownloadTimes(int downloadTimes) {
                this.downloadTimes = downloadTimes;
            }

            public String getDownloadTimesFormat() {
                return downloadTimesFormat;
            }

            public void setDownloadTimesFormat(String downloadTimesFormat) {
                this.downloadTimesFormat = downloadTimesFormat;
            }

            public String getDownloadUrl() {
                return downloadUrl;
            }

            public void setDownloadUrl(String downloadUrl) {
                this.downloadUrl = downloadUrl;
            }

            public int getVersionCode() {
                return versionCode;
            }

            public void setVersionCode(int versionCode) {
                this.versionCode = versionCode;
            }

            public String getUpdateInfo() {
                return updateInfo;
            }

            public void setUpdateInfo(String updateInfo) {
                this.updateInfo = updateInfo;
            }

            public String getApkMd5() {
                return apkMd5;
            }

            public void setApkMd5(String apkMd5) {
                this.apkMd5 = apkMd5;
            }

            public String getBrief() {
                return brief;
            }

            public void setBrief(String brief) {
                this.brief = brief;
            }

            public String getUpdateTime() {
                return updateTime;
            }

            public void setUpdateTime(String updateTime) {
                this.updateTime = updateTime;
            }

            public int getGiftCount() {
                return giftCount;
            }

            public void setGiftCount(int giftCount) {
                this.giftCount = giftCount;
            }

            public int getIsAd() {
                return isAd;
            }

            public void setIsAd(int isAd) {
                this.isAd = isAd;
            }

            public Object getBannerUrl() {
                return bannerUrl;
            }

            public void setBannerUrl(Object bannerUrl) {
                this.bannerUrl = bannerUrl;
            }

            public int getStatusType() {
                return statusType;
            }

            public void setStatusType(int statusType) {
                this.statusType = statusType;
            }

            public int getSourceType() {
                return sourceType;
            }

            public void setSourceType(int sourceType) {
                this.sourceType = sourceType;
            }

            public Object getSubTitle() {
                return subTitle;
            }

            public void setSubTitle(Object subTitle) {
                this.subTitle = subTitle;
            }

            public int getSourceId() {
                return sourceId;
            }

            public void setSourceId(int sourceId) {
                this.sourceId = sourceId;
            }

            public Object getSearchDownloadPecent() {
                return searchDownloadPecent;
            }

            public void setSearchDownloadPecent(Object searchDownloadPecent) {
                this.searchDownloadPecent = searchDownloadPecent;
            }

            public Object getOurTag() {
                return ourTag;
            }

            public void setOurTag(Object ourTag) {
                this.ourTag = ourTag;
            }

            public int getWeight() {
                return weight;
            }

            public void setWeight(int weight) {
                this.weight = weight;
            }

            public int getId() {
                return id;
            }

            public void setId(int id) {
                this.id = id;
            }

            @Override
            public String toString() {
                return "AppsEntity{" +
                        "appTypeId=" + appTypeId +
                        ", name='" + name + '\'' +
                        ", packageName='" + packageName + '\'' +
                        ", categoryName='" + categoryName + '\'' +
                        ", rating=" + rating +
                        ", tag='" + tag + '\'' +
                        ", versionName='" + versionName + '\'' +
                        ", iconUrl='" + iconUrl + '\'' +
                        ", largeIcon='" + largeIcon + '\'' +
                        ", apkSize=" + apkSize +
                        ", apkSizeFormat='" + apkSizeFormat + '\'' +
                        ", boxLabel=" + boxLabel +
                        ", downloadTimes=" + downloadTimes +
                        ", downloadTimesFormat='" + downloadTimesFormat + '\'' +
                        ", downloadUrl='" + downloadUrl + '\'' +
                        ", versionCode=" + versionCode +
                        ", updateInfo='" + updateInfo + '\'' +
                        ", apkMd5='" + apkMd5 + '\'' +
                        ", brief='" + brief + '\'' +
                        ", updateTime='" + updateTime + '\'' +
                        ", giftCount=" + giftCount +
                        ", isAd=" + isAd +
                        ", bannerUrl=" + bannerUrl +
                        ", statusType=" + statusType +
                        ", sourceType=" + sourceType +
                        ", subTitle=" + subTitle +
                        ", sourceId=" + sourceId +
                        ", searchDownloadPecent=" + searchDownloadPecent +
                        ", ourTag=" + ourTag +
                        ", weight=" + weight +
                        ", id=" + id +
                        '}';
            }
        }
    }
}
