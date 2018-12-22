package com.prize.statistics.model;

import com.prize.app.constants.Constants;

/**
 * @创建者 longbaoxiu
 * @创建者 2016/12/19.16:39
 * @描述
 */

public class StatisConstant {
    /**
     * 正式环境URL
     */
    private static final String GIS_URL_DEVICE = Constants.GIS_URL_DEVICE;
    private static final String GIS_URL_ICSVIP_DEVICE = Constants.GIS_ICSVIP_URL;

//    /****接口安全校验，先请求改接口来获取一个pid*****/
//    public static final String PID_URL = GIS_URL_DEVICE + "/collect/setting";
//    /****此接口需要与pid接口联用*****/
//    public static final String UUID_URL = GIS_URL_DEVICE + "/api/uuid";
    /**
     * 请求服务器时间
     *****/
    public static final String SERVER_TIME_URL = GIS_URL_DEVICE + "/collect/setting";
    /**
     * 上传日志URl
     *****/
    public static final String SERVER_LOGS_URL = GIS_URL_DEVICE + "/collect/logs";

    /**
     * 专门上传下载数据重要数据URl
     */
    public static final String SERVER_ICSVIP_URL = GIS_URL_ICSVIP_DEVICE + "/collect/logs";

    public static int STATICS_DBVERSIONCODE = 3;//3.2变更3
    public static String PRIZE_STAT_DB_NAME = "prize_stat_db";

    public static final String NEW_STATICS = Constants.NEW_BASE_URL + "tid=%s&source=market&type=%d&sdkv=%s&model=%s&channel=%s&brand=%s";
}
