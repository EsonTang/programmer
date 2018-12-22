package com.android.settings.face.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Administrator on 2017/11/3.
 */

public class ThreadUtil {
    private static ExecutorService mCacheThreadExecutor = Executors.newCachedThreadPool();

    public static ExecutorService getmCacheThreadExecutor() {
        return mCacheThreadExecutor;
    }
}
