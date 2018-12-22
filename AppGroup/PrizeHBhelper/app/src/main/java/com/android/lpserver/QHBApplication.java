package com.android.lpserver;

import android.app.Activity;
import android.app.Application;
import android.content.Context;

public class QHBApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        //prize-add-zhaojian for pcba:switch should be off by default -2017824 start
//        Config.getConfig(this).initHBSwith();
        //prize-add-zhaojian for pcba:switch should be off by default -2017824 end
    }

   /* public static void showShare(final Activity activity) {

    }

    *//** Display share*//*
    public static void showShare(final Activity activity, final String shareUrl) {
    }

    *//** Check for updates*//*
    public static void checkUpdate(Activity activity) {

    }*/

    /** First activity start call*/
    public static void activityStartMain(Activity activity) {

    }

    /** onCreate*/
    public static void activityCreateStatistics(Activity activity) {

    }

    /** onResume*/
    public static void activityResumeStatistics(Activity activity) {

    }

    /** onPause*/
    public static void activityPauseStatistics(Activity activity) {

    }

    /** Event statistics*/
    public static void eventStatistics(Context context, String event) {

    }

    /** Event statistics*/
    public static void eventStatistics(Context context, String event, String tag) {

    }
}
