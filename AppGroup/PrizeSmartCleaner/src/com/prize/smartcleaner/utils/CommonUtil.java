package com.prize.smartcleaner.utils;

import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.INotificationManager.Stub;
import android.content.Context;
import android.os.ServiceManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created by xiarui on 2018/2/25.
 */

public class CommonUtil {

    private static final String TAG = "CommonUtil";

    public static ArrayList<String> getNeedKillAudioList(Context context) {
        ArrayList<String> needKillList = new ArrayList();
        ArrayList<String> audioList = new ArrayList();
        ArrayList<RunningAppProcessInfo> processes = PrizeClearUtil.getRunningAppProcesses(context);
        int[] audioPids = PrizeClearUtil.getActiveAudioPids(context);
        if (audioPids != null) {
            int length = audioPids.length;
            if (length > 0) {
                ArrayList<String> noClearNotification = new ArrayList();
                boolean haveNoClearNotify = getNoClearPkg(context, noClearNotification);
                for (int i = 0; i < length; i++) {
                    for (int j = 0; j < processes.size(); j++) {
                        RunningAppProcessInfo runningAppProcessInfo = (RunningAppProcessInfo) processes.get(j);
                        if (!runningAppProcessInfo.processName.equals("system") && audioPids[i] == runningAppProcessInfo.pid) {
                            for (String pkg : runningAppProcessInfo.pkgList) {
                                if (audioList != null && !audioList.contains(pkg)) {
                                    audioList.add(pkg);
                                    LogUtils.d(TAG, "getAudioList: add audio list: " + pkg);
                                }
                            }
                        }
                    }

                    if (haveNoClearNotify) {
                        for (String pkg : audioList) {
                            if (!noClearNotification.contains(pkg) && !needKillList.contains(pkg)) {
                                needKillList.add(pkg);
                            }
                        }
                    }
                }
            }
        }

        for (int i = 0; i < needKillList.size(); i++) {
            LogUtils.d(TAG, "needKillList[" + i + "] = " + needKillList.get(i));
        }

        return needKillList;

    }

    public static boolean getNoClearPkg(Context context, ArrayList<String> noClearList) {
        Process exec = null;
        BufferedReader bufferedReader = null;
        boolean haveNoClearNotification = false;
        try {
            exec = Runtime.getRuntime().exec("dumpsys notification noClear");
            bufferedReader = new BufferedReader(new InputStreamReader(exec.getInputStream(), "UTF-8"));
            while (true) {
                String readLine = bufferedReader.readLine();
                if (readLine == null) {
                    break;
                } else if (readLine.contains("NoClearNotification:")) {
                    String noClearPkg = readLine.substring(readLine.indexOf("NoClearNotification:") + "NoClearNotification:".length());
                    if (!(noClearPkg == null || "".equals(noClearPkg))) {
                        noClearList.add(noClearPkg);
                    }
                } else if (readLine.contains("mNotificationNoClear:")) {
                    haveNoClearNotification = true;
                    LogUtils.d(TAG, "getNoClearNotificationList: useAllNoClear!");
                }
            }
        } catch (IOException e) {
            LogUtils.d(TAG, "failed parsing dumpsys notification  " + e);
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    LogUtils.d(TAG, "failed closing reader  " + e);
                }
            }
            if (exec != null) {
                try {
                    exec.waitFor();
                } catch (InterruptedException e) {
                    LogUtils.d(TAG, "failed process waitfor " + e);
                }
                exec.destroy();
            }
        }
        LogUtils.d(TAG, "haveNoClearNotification = " + haveNoClearNotification);
        return haveNoClearNotification;
    }
}
