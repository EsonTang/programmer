package com.prize.music.admanager.statistics.model;

/**
 * @创建者 longbaoxiu
 * @创建者 2016/12/19.16:39
 * @描述
 */

public class StatisConstant {
    /** 正式环境URL */
    public static final String GIS_URL_DEVICE = "http://ics.szprize.cn/ics";
//  public static final String GIS_URL_DEVICE = "http://192.168.1.187:8080/ics";

    /****接口安全校验，先请求改接口来获取一个pid*****/
    public static final String PID_URL = GIS_URL_DEVICE+"/collect/setting";
    /****此接口需要与pid接口联用*****/
    public static final String UUID_URL = GIS_URL_DEVICE+"/api/uuid";
    /**请求服务器时间*****/
    public static final String  SERVER_TIME_URL = GIS_URL_DEVICE+"/collect/setting";
    /**上传日志URl*****/
    public static final String  SERVER_LOGS_URL = GIS_URL_DEVICE+"/collect/logs";
}
