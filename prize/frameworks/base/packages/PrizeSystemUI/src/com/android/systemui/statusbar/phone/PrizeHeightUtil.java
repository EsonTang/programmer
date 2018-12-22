package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.view.WindowManager;

/**
 * Created by Administrator on 2017/6/26.
 */

public class PrizeHeightUtil {

    public static int getStatusBarHeight(Context context){
        Resources mResources = context.getResources();

        int mStatusBarHeight = 0;
        int resourceId = mResources.getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            //根据资源ID获取响应的尺寸值
            mStatusBarHeight = mResources.getDimensionPixelSize(resourceId);
        }
        return mStatusBarHeight;
    }

    public static int getWindowWidth(Context context){
        Resources mResources = context.getResources();
        DisplayMetrics metric = mResources.getDisplayMetrics();
        int mWindowWidth = metric.widthPixels;     // 屏幕宽度（像素）

        return mWindowWidth;
    }

    public static int getWindowRealHeight(Context context){
        DisplayMetrics metric = new DisplayMetrics();
        WindowManager mWManager=(WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        mWManager.getDefaultDisplay().getRealMetrics(metric);
        int mWindowHeight = metric.heightPixels;

        return mWindowHeight;
    }

    public static int getWindowDisplayHeight(Context context){
        DisplayMetrics metric = new DisplayMetrics();
        WindowManager mWManager=(WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        mWManager.getDefaultDisplay().getMetrics(metric);
        int mWindowDisplayHeight = metric.heightPixels;

        return mWindowDisplayHeight;
    }
}
