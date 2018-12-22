package com.prize.appcenter.ui.util;

import android.os.Looper;
import android.util.Printer;

/**
 * @创建者 longbaoxiu
 * @创建者 2017/8/15.14:33
 * @描述
 */

public class BlockDetectByPrinter {
    public static void start() {

        Looper.getMainLooper().setMessageLogging(new Printer() {

            private static final String START = ">>>>> Dispatching";
            private static final String END = "<<<<< Finished";

            @Override
            public void println(String x) {
                if (x.startsWith(START)) {
                    LogMonitor.getInstance().startMonitor();
                }
                if (x.startsWith(END)) {
                    LogMonitor.getInstance().removeMonitor();
                }
            }
        });

    }
}
