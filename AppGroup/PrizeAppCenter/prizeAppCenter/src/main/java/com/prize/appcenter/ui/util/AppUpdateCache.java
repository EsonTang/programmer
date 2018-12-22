package com.prize.appcenter.ui.util;

import com.prize.app.net.datasource.base.AppsItemBean;
import com.prize.app.util.JLog;

import java.util.ArrayList;
import java.util.List;

/**
 * 应用更新的缓存数据
 * longbaoxiu
 * 2017/6/26.16:48
 */

public class AppUpdateCache {
    private volatile static AppUpdateCache instance;
    private List<AppsItemBean> data = new ArrayList<>();
    private AppUpdateCache() {
    }
    public void saveCache(List<AppsItemBean> data) {
        if(data ==null)
            return;
        this.data.clear();
        this.data.addAll(data);
        if (JLog.isDebug) {
            JLog.i("AppUpdateActivity","saveCache");
        }
    }

    public List<AppsItemBean> getCache() {
        return this.data;
    }

    public void clearCache() {
        if (JLog.isDebug) {
            JLog.i("AppUpdateActivity","clearCache");
        }
        if (data != null) {
            data.clear();
        }
    }
    /** Returns singleton class instance */
    public static AppUpdateCache getInstance() {
        if (instance == null) {
            synchronized (AppUpdateCache.class) {
                if (instance == null) {
                    instance = new AppUpdateCache();
                }
            }
        }
        return instance;
    }
}
