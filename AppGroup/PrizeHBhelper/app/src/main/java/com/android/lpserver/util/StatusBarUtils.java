package com.android.lpserver.util;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.android.lpserver.R;

import java.lang.reflect.Field;

/**
 * Created by prize on 2017/9/12.
 */

public class StatusBarUtils {

    public static void setStatusBar(Window window, Context context) {
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                //| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            window.setStatusBarColor(context.getResources().getColor(R.color.prize_actionbar_bg_color_v8));      // prize modify zhaojian 8.0 2017803
        }else {
            window.setStatusBarColor(Color.TRANSPARENT);
        }

        WindowManager.LayoutParams lp = window.getAttributes();
//        lp.statusBarInverse = StatusBarManager.STATUS_BAR_INVERSE_GRAY;
//        getWindow().setAttributes(lp);
        try {
            Class statusBarManagerClazz = Class.forName("android.app.StatusBarManager");
            Field grayField = statusBarManagerClazz.getDeclaredField("STATUS_BAR_INVERSE_GRAY");
            Object gray = grayField.get(statusBarManagerClazz);
            Class windowManagerLpClazz = lp.getClass();
            Field statusBarInverseField = windowManagerLpClazz.getDeclaredField("statusBarInverse");
            statusBarInverseField.set(lp,gray);
            window.setAttributes(lp);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
