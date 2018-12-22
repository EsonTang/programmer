package com.prize.music.admanager.statistics.core;

import android.content.Context;

import java.util.Properties;

/**
 * @创建者 longbaoxiu
 * @创建者 2016/12/17.15:12
 * @描述
 */

public interface StaticsManager {
    void onEventParameter(Properties p, Context mContext);
    void onInitEvent(String eventName);
}
