
package com.prize.qihoo.cleandroid.sdk;

import android.os.Parcel;
import android.util.Log;

import com.qihoo360.mobilesafe.opti.env.clear.ProcessClearEnv;
import com.qihoo360.mobilesafe.opti.i.processclear.AppPackageInfo;

/*定义一个辅助类用来帮助把逻辑数据转换成界面显示用到的数据类型*/
public class ProcessInfoUtils {
    private static final boolean DEBUG = SDKEnv.DEBUG;

    private static final String TAG = DEBUG ? "ProcessInfoUtils" : ProcessInfoUtils.class.getSimpleName();

    // 建议选中清理
    public static final String UI_FLAG_SELECT = "ui_select";

    // 有可清理的进程项
    public static final String UI_FLAG_HAS_CLEAR_PROCESS = "ui_proc";

    // 系统进程
    public static final String UI_FLAG_SYSTEM = "ui_system";

    public static class ProcessUiItem {
        boolean mSystemApp;// 系统应用(没有加白名单操作)

        boolean mDeaultChoosen;// 默认是否选中状态

        boolean mHasClearProc;// 是否有可清理的部份进程项（分可清理的缓存和谨慎清理项）

        boolean mDefaultHiden;// 默认是否建议隐藏（建议不显示）
    }

    /* 从逻辑数据返回界面需要的标记 */
    public static ProcessUiItem getUiFlagsFromData(AppPackageInfo processInfo) {
        if (processInfo != null) {
            ProcessUiItem item = new ProcessUiItem();
            if (item != null) {
                /* 当前结点下有可清理的进程项 */
                if (processInfo.clearablePids != null && processInfo.clearablePids.length > 0) {
                    item.mHasClearProc = true;
                }
                item.mDeaultChoosen = isDefaultChoosen((processInfo.isDefaultChoosen == 1), processInfo.flag);

                item.mSystemApp = isSystemApp(processInfo.flag, processInfo.type);

                item.mDefaultHiden = (processInfo.flag == ProcessClearEnv.FLAG_HIDE);

                if (DEBUG) {
                    Log.i(TAG, "getUiFlagsFromData item=" + processInfo.packageName + "(system=" + item.mSystemApp + " choosen=" + item.mDeaultChoosen + " hasClearProc=" + item.mHasClearProc + ")");
                }
                return item;
            }
        }
        return null;
    }

    private static boolean isDefaultChoosen(boolean defaultChoosen, int flag) {
        if (defaultChoosen == true && flag != ProcessClearEnv.FLAG_HIDE) {
            return true;
        }
        return false;
    }

    private static boolean isSystemApp(int flag, int type) {
        if (flag == ProcessClearEnv.FLAG_HIDE || type == ProcessClearEnv.TYPE_SYSTEM_CORE_HAS_KILL_PROC) {
            return true;
        }
        return false;
    }

    static public AppPackageInfo cloneAppPackageInfo(AppPackageInfo info) {
        if (info != null) {
            // 暂时先要有用的值，后面把结构数据做优化整理
            AppPackageInfo clone = new AppPackageInfo();
            if (clone != null) {
                try {
                    clone.packageName = info.packageName;
                    clone.appName = info.appName;
                    clone.flag = info.flag;
                    clone.isDefaultChoosen = info.isDefaultChoosen;
                    clone.userSelection = info.userSelection;
                    if (info.pids != null) {
                        clone.pids = new int[info.pids.length];
                        int index = 0;
                        for(int i:info.pids){
                            clone.pids[index++] = i;
                        }
                    }
                    if(info.clearablePids != null){
                        clone.clearablePids = new int[info.clearablePids.length];
                        int index = 0;
                        for(int i:info.clearablePids){
                            clone.clearablePids[index++] = i;
                        }
                    }
                    clone.uid = info.uid;
                    clone.type = info.type;
                    clone.usedMemory = info.usedMemory;
                    clone.clearMemory = info.clearMemory;
                    if (clone.bundle != null && info.bundle != null) {
                        clone.bundle.putAll(info.bundle);
                    }
                } catch (Exception e) {
                    if (DEBUG) {
                        Log.e(TAG, "error" + e.getMessage(), e);
                    }
                }
                if (DEBUG) {
                    Log.i(TAG, "origin item name=" + info.packageName + " flag=" + info.flag + " choosen=" + info.isDefaultChoosen);
                    Log.i(TAG, "clone item name=" + clone.packageName + " flag=" + clone.flag + " choosen=" + clone.isDefaultChoosen);
                }
                return clone;
            }
        }
        return null;
    }
}
