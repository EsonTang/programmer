package com.prize.app.beans;

/**
 * 百度定位位置信息bean
 * @创建者 longbaoxiu
 * @创建者 2017/7/13.15:30
 * @描述
 */

public class LocationInfo {

    public  Double  latitude;// 纬度
    public  Double  lontitude;// 经度
    public  String  address;//位置信息

    @Override
    public String toString() {
        return "LocationInfo{" +
                "latitude=" + latitude +
                ", lontitude=" + lontitude +
                ", location='" + address + '\'' +
                '}';
    }
}
