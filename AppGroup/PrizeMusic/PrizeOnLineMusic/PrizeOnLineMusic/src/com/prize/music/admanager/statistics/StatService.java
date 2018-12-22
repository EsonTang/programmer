package com.prize.music.admanager.statistics;

import java.util.Properties;

import com.prize.music.admanager.presenter.JLog;
import com.prize.music.admanager.statistics.core.StatSdk;
import com.prize.music.admanager.statistics.db.utils.StaticsAgent;

import android.content.Context;


/**
 * 统计管理类
 * @创建者 longbaoxiu
 * @创建者 2016/12/17.15:08
 * @描述
 */

public class StatService {


    /**
     * 初始化数据库，服务器时间等，消息头等
     * @param var0
     */
    public static void init(Context var0) {
        JLog.i("PRIZE2016", "PrizeStatService-init");
        StaticsAgent.init(var0);
        StatSdk.getInstance(var0).getServeTime(var0);

}

    public static void trackCustomKVEvent(Context var0, String var1, Properties var2) {
        StatSdk.getInstance(var0.getApplicationContext()).setEventParameter(var1,var2);
    }
}
