package com.prize.smartcleaner.utils;

import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.INotificationManager.Stub;
import android.content.Context;
import android.os.ServiceManager;

import com.prize.smartcleaner.PrizeClearFilterManager;

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

        if (needKillList.size() > 0) {
            for (int i = 0; i < needKillList.size(); i++) {
                LogUtils.i(TAG, "needKillAudioList[" + i + "] = " + needKillList.get(i));
            }
        } else {
            LogUtils.i(TAG, "no audio lock app need killed");
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
                        LogUtils.i(TAG, "add noClearPkg: " + noClearPkg);
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

    public static ArrayList<String> getNeedKillMusicList(Context context, ArrayList<String> musicActiveList) {
        ArrayList<String> needKillList = new ArrayList();
        ArrayList<String> audioList = new ArrayList();
        ArrayList<String> aliveMusicList = new ArrayList<>();
        ArrayList<String> musicList = PrizeClearFilterManager.getInstance().getFilterListFromSP(context, PrizeClearUtil.TYPE_MUSIC);
        ArrayList<RunningAppProcessInfo> processes = PrizeClearUtil.getRunningAppProcesses(context);
        int[] audioPids = PrizeClearUtil.getActiveAudioPids(context);

        for (int j = 0; j < processes.size(); j++) {
            RunningAppProcessInfo runningAppProcessInfo = (RunningAppProcessInfo) processes.get(j);
            String processName = runningAppProcessInfo.processName;
            int pid = runningAppProcessInfo.pid;
            String[] pkgList = runningAppProcessInfo.pkgList;

            if (PrizeClearUtil.isInList(pkgList, musicList)) {
                for (String pkg : runningAppProcessInfo.pkgList) {
                    if (aliveMusicList != null && !aliveMusicList.contains(pkg)) {
                        aliveMusicList.add(pkg);
                        LogUtils.d(TAG, "getNeedKillMusicList: add alive music list: " + pkg);
                    }
                }
            }

            if (audioPids != null) {
                for (int i = 0; i < audioPids.length; i++) {
                    if (!processName.equals("system") && audioPids[i] == pid) {
                        for (String pkg : pkgList) {
                            if (audioList != null && !audioList.contains(pkg)) {
                                audioList.add(pkg);
                                LogUtils.d(TAG, "add get audio locker list: " + pkg);
                            }
                        }
                    }
                }
            }
        }

        for (String pkg : aliveMusicList) {
            if (needKillList != null && !needKillList.contains(pkg) && !audioList.contains(pkg) && !"com.android.musicfx".equals(pkg)) {
                needKillList.add(pkg);
            }
            if (musicActiveList != null && !musicActiveList.contains(pkg) && audioList.contains(pkg)) {
                musicActiveList.add(pkg);
                LogUtils.i(TAG, "musicActiveList = " + pkg);
            }
        }

        if (needKillList.size() > 0) {
            for (int i = 0; i < needKillList.size(); i++) {
                LogUtils.i(TAG, "needKillInactiveMusicList[" + i + "] = " + needKillList.get(i));
            }
        } else {
            LogUtils.i(TAG, "no inactive music app need killed");
        }

        return needKillList;
    }

    public static ArrayList<String> getNeedKillLocationList(Context context, ArrayList<String> locationActiveList) {
        ArrayList<String> needKillList = new ArrayList();
        ArrayList<String> aliveLocationList = new ArrayList<>();
        ArrayList<String> locationList = PrizeClearUtil.getInUseLocationPkgList(context);
        ArrayList<String> mapList = PrizeClearFilterManager.getInstance().getFilterListFromSP(context, PrizeClearUtil.TYPE_MAP);
        ArrayList<RunningAppProcessInfo> processes = PrizeClearUtil.getRunningAppProcesses(context);

        for (int j = 0; j < processes.size(); j++) {
            RunningAppProcessInfo runningAppProcessInfo = (RunningAppProcessInfo) processes.get(j);
            String processName = runningAppProcessInfo.processName;
            String[] pkgList = runningAppProcessInfo.pkgList;

            //location map
            if (PrizeClearUtil.isInList(pkgList, mapList)) {
                for (String pkg : runningAppProcessInfo.pkgList) {
                    if (aliveLocationList != null && !aliveLocationList.contains(pkg)) {
                        aliveLocationList.add(pkg);
                        LogUtils.d(TAG, "getNeedKillLocationList: add alive location list: " + pkg);
                    }
                }
            }
        }

        //add inactive location map app ; get active list map list
        for (String pkg : aliveLocationList) {
            if (needKillList != null && !needKillList.contains(pkg) && !locationList.contains(pkg)) {
                needKillList.add(pkg);
            }
            if (locationActiveList != null && !locationActiveList.contains(pkg) && locationList.contains(pkg)) {
                locationActiveList.add(pkg);
                LogUtils.i(TAG, "locationActiveList = " + pkg);
            }
        }

        if (needKillList.size() > 0) {
            for (int i = 0; i < needKillList.size(); i++) {
                LogUtils.i(TAG, "needKillInactiveLocationList[" + i + "] = " + needKillList.get(i));
            }
        } else {
            LogUtils.i(TAG, "no inactive location app need killed");
        }

        //add use location app which not in mapList
        for (String pkg : locationList) {
            if (needKillList != null && !needKillList.contains(pkg) && !mapList.contains(pkg)) {
                needKillList.add(pkg);
                LogUtils.i(TAG, "[ " + pkg + " ] use location which not in mapList, need be killed");
            }
        }

        return needKillList;
    }
}
