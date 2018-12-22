package com.android.systemui.recents;

import android.content.Context;
import android.content.res.Resources;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Created by prize xiarui on 2017/11/10.
 */

public class DisplayUtils {

    private static final String TAG = "DisplayUtils";

    /**
     * get status bar height
     * @return
     */
    public static int getStatusBarHeight(Context context) {
        Object obj = null;
        Field field = null;
        int x = 0, sbar = 0;
        try {
            Class<?> c = Class.forName("com.android.internal.R$dimen");
            obj = c.newInstance();
            field = c.getField("status_bar_height");
            x = Integer.parseInt(field.get(obj).toString());
            sbar = context.getResources().getDimensionPixelSize(x);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        LogUtils.d(TAG, "StatusBar Height:" + sbar);
        return sbar;
    }

    /**
     * check device is show navigation bar
     * @return
     */
    public static boolean checkDeviceHasNavigationBar(Context context) {
        boolean hasNavigationBar = false;
        Resources rs = context.getResources();
        int id = rs.getIdentifier("config_showNavigationBar", "bool", "android");
        if (id > 0) {
            hasNavigationBar = rs.getBoolean(id);
        }
        try {
            Class systemPropertiesClass = Class.forName("android.os.SystemProperties");
            Method m = systemPropertiesClass.getMethod("get", String.class);
            String navBarOverride = (String) m.invoke(systemPropertiesClass, "qemu.hw.mainkeys");
            if ("1".equals(navBarOverride)) {
                hasNavigationBar = false;
            } else if ("0".equals(navBarOverride)) {
                hasNavigationBar = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        LogUtils.d(TAG, "is show navigation bar : " + hasNavigationBar);
        return hasNavigationBar;
    }

    /**
     * get navigation bar height
     * @return
     */
    public static int getNavigationBarHeight(Context context) {
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height","dimen", "android");
        int height = resources.getDimensionPixelSize(resourceId);
        LogUtils.d(TAG, "NavigationBar Height:" + height);
        return height;
    }
}
