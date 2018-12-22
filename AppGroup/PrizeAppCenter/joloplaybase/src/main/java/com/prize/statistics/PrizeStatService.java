package com.prize.statistics;

import android.content.Context;

import com.prize.app.util.JLog;
import com.prize.statistics.core.PrizeStatSdk;
import com.prize.statistics.db.utils.StaticsAgent;
import com.prize.statistics.model.ExposureBean;

import java.util.List;
import java.util.Properties;

/**
 * 统计管理类
 * longbaoxiu
 * 2016/12/17.15:08
 *
 */

public class PrizeStatService {


    /**
     * 初始化数据库，服务器时间等，消息头等
     * @param var0  Context
     */
    public static void init(Context var0) {
        JLog.i("PRIZE2016", "PrizeStatService-init");
        StaticsAgent.init(var0);
        PrizeStatSdk.getInstance(var0).getServeTime();

}

    public static void trackCustomKVEvent(Context var0, String var1, Properties var2,boolean upLoadNow) {
        PrizeStatSdk.getInstance(var0.getApplicationContext()).setEventParameter(var1,var2,upLoadNow);
    }
    public static void trackExposureEvent(Context var0, String var1, List<ExposureBean> var2,boolean upLoadNow) {
        PrizeStatSdk.getInstance(var0.getApplicationContext()).setExposurearameter(var1,var2,upLoadNow);
    }
    public static void trackNewDown(Context var0, String var1, ExposureBean bean) {
        PrizeStatSdk.getInstance(var0.getApplicationContext()).trackNewDown(var1,bean);
    }

    public static void trackDownEvent(Context var0, String var1, Properties var2) {
        PrizeStatSdk.getInstance(var0.getApplicationContext()).setDownEventParameter(var1,var2);
    }
}
