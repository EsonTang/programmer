
package com.prize.qihoo.cleandroid.sdk;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.prize.qihoo.cleandroid.sdk.plugins.PtManagerImpl;
import com.qihoo360.mobilesafe.opti.env.clear.TrashClearEnv;
import com.qihoo360.mobilesafe.opti.i.trashclear.TrashClearCategory;
import com.qihoo360.mobilesafe.opti.i.trashclear.TrashInfo;

/**
 * 系统缓存勾选策略类
 *
 * @see 目前只用作系统缓存勾选
 */
public class AppCacheCheck {
    private static final boolean DEBUG = SDKEnv.DEBUG;

    private static final String TAG = DEBUG ? "AppCacheCheck" : AppCacheCheck.class.getSimpleName();

    /**
     * 检测系统缓存勾选状态
     *
     * @param list
     */
    public static void check(ArrayList<TrashInfo> list, Context context) {
        if (list.size() == 0) {
            return;
        }

        List<String> pkgList = load(context);

        boolean isAllSelected = true;
        // 更新为上次保存的状态
        for (TrashInfo trashInfo : list) {
            if (pkgList.contains(trashInfo.packageName)) {
                if (DEBUG) {
                    Log.i(TAG, "check,is contains,pkg: " + trashInfo.packageName);
                }
                trashInfo.isSelected = false;
            }
            if (!trashInfo.isSelected) {
                isAllSelected = false;
            }
        }

        boolean isRoot = PtManagerImpl.isRootOk();
        // 非root下，只要有一个未勾选，就全不勾选
        if (!isRoot && !isAllSelected) {
            for (TrashInfo trashInfo : list) {
                if (DEBUG) {
                    Log.i(TAG, "check,isAllSelected,pkg: " + trashInfo.packageName);
                }
                trashInfo.isSelected = false;
            }
        }
    }

    /**
     * 加载数据未勾选包名
     */
    private static List<String> load(Context context) {
        List<String> pkgList = new ArrayList<String>();

        String pkg = SharedPrefUtils.getString(context, SharedPrefUtils.KEY_CLEAR_APPCACHE_SELECT, "");
        if (!TextUtils.isEmpty(pkg)) {
            String[] pkgArray = pkg.split("\\|");
            if (pkgArray.length > 0) {
                for (String p : pkgArray) {
                    pkgList.add(p);
                }
            }
        }

        if (DEBUG) {
            Log.i(TAG, "load,pkg: " + pkg);
        }

        return pkgList;
    }

    /**
     * 保存勾选状态
     *
     * @param list
     */
    private static void save(List<TrashInfo> list, Context context) {
        if (list.size() == 0) {
            return;
        }

        StringBuffer data = new StringBuffer();
        for (TrashInfo t : list) {
            if (!t.isSelected) {
                if (data.length() == 0) {
                    data.append(t.packageName);
                } else {
                    data.append("|");
                    data.append(t.packageName);
                }
            }
        }

        SharedPrefUtils.setString(context, SharedPrefUtils.KEY_CLEAR_APPCACHE_SELECT, data.toString());

        if (DEBUG) {
            Log.i(TAG, "save,data: " + data.toString());
        }
    }

    /**
     * 保存系统缓存勾选状态
     */
    public static void saveAppCacheState(TrashClearCategory appCacheItem, Context context) {
        if (appCacheItem == null || appCacheItem.trashInfoList == null || appCacheItem.trashInfoList.size() == 0) {
            return;
        }
        TrashInfo t = appCacheItem.trashInfoList.get(0);
        if (t.type != TrashClearEnv.CATE_APP_SYSTEM_CACHE) {
            return;
        }

        List<TrashInfo> subList = t.bundle.getParcelableArrayList(TrashClearEnv.subList);
        if (subList.size() == 0) {
            return;
        }

        save(subList, context);
    }

    /**
     * 判断系统缓存是否可被清理
     *
     * @param subList
     * @return 返回为true，表示可以清理
     */
    public static boolean checkAppCacheCanClear(ArrayList<TrashInfo> subList) {
        boolean ret = true;
        if (subList == null || subList.size() == 0) {
            return ret;
        }

        boolean isRoot = PtManagerImpl.isRootOk();
        if (!isRoot) {
            for (TrashInfo trashInfo : subList) {
                // 非root,只要有一个没被勾选，就不可清理
                if (!trashInfo.isSelected) {
                    ret = false;
                    break;
                }
            }
        }

        if (DEBUG) {
            Log.i(TAG, "checkAppCacheCanClear,isRoot: " + isRoot + ",ret: " + ret);
        }

        return ret;
    }
}
