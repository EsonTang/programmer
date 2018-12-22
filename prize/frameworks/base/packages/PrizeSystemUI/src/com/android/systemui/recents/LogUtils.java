package com.android.systemui.recents;

import android.util.Log;

/**
 * Created by prize on 2017/11/10.
 */

public class LogUtils {

    public static final boolean DEBUG = false;

    public static void d(String tag, String msg) {
        if (DEBUG) {
            Log.d("xiarui-" + tag, msg);
        }
    }
}
