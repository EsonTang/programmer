
package com.prize.qihoo.cleandroid.sdk;

import android.text.TextUtils;
import android.util.Log;

import com.qihoo360.mobilesafe.opti.env.clear.TrashClearEnv;
import com.qihoo360.mobilesafe.opti.i.trashclear.TrashInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 安装包排序
 */
public class ApkListSort {

    private static final boolean DEBUG = com.prize.qihoo.cleandroid.sdk.SDKEnv.DEBUG;

    private static final String TAG = DEBUG ? "ApkListSort" : ApkListSort.class.getSimpleName();

    private final ArrayList<TrashInfo> mCheckedList = new ArrayList<TrashInfo>();

    private final ArrayList<TrashInfo> mOtherList = new ArrayList<TrashInfo>();

    public ArrayList<TrashInfo> sort(List<TrashInfo> sortList) {
        for (TrashInfo trashInfo : sortList) {
            // 已勾选，备份包，破损包就不需要运用勾选策略,直接进入mCheckedList列表
            if (trashInfo.clearType == TrashClearEnv.CLEAR_TYPE_ONEKEY || TrashClearEnv.APK_TYPE_DAMAGED == trashInfo.dataType || TrashClearEnv.APK_TYPE_BACKUPED == trashInfo.dataType) {
                mCheckedList.add(trashInfo);
            } else {
                if (trashInfo.dataType == TrashClearEnv.APK_TYPE_INSTALLED || trashInfo.dataType == TrashClearEnv.APK_TYPE_OLDER) {
                    // 已安装,旧版本先勾选
                    trashInfo.clearType = TrashClearEnv.CLEAR_TYPE_ONEKEY;
                }
                // 其它情况进入勾选策略列表
                mOtherList.add(trashInfo);
            }

        }
        // 判断重复包
        checkRepeat(mOtherList);
        // 判断旧版本
        checkOld(mOtherList);
        // 判断是否3天内下载
        checkDate(mOtherList);

        mCheckedList.addAll(mOtherList);
        mOtherList.clear();

        // 排序
        sortApk();

        for (TrashInfo trashInfo : mCheckedList) {
            com.prize.qihoo.cleandroid.sdk.TrashClearUtils.setTrashInfoSelected(trashInfo);
            // if (DEBUG) {
            // String oneApkVersionName =
            // trashInfo.bundle.getString(TrashClearEnv.apkVersionName);
            // int oneApkVersionCode =
            // trashInfo.bundle.getInt(TrashClearEnv.apkVersionCode);
            // Log.i(TAG, "oneApkVersionName:" + oneApkVersionName +
            // ",oneApkVersionCode:" + oneApkVersionCode + ",path:" +
            // trashInfo.path);
            // }
        }

        return mCheckedList;
    }

    /**
     * @param sortList
     */
    private void checkDate(List<TrashInfo> sortList) {
        int nCount = sortList.size();
        if (nCount == 0) {
            return;
        }

        for (int i = 0; i < nCount; i++) {
            TrashInfo info = sortList.get(i);
            // 非缓存未安装，升级包，添加时间判断
            if (info.dataType == TrashClearEnv.APK_TYPE_UNINSTALLED || info.dataType == TrashClearEnv.APK_TYPE_UPDATE) {
                if (!TextUtils.isEmpty(info.path) && checkApkDownloadTime(info.path)) {
                    // 标识为时间限制包
                    info.dataType = TrashClearEnv.APK_TYPE_DATELINE;
                } else {
                    info.clearType = TrashClearEnv.CLEAR_TYPE_ONEKEY;
                }
            }
        }
    }

    /**
     * 检测安装包是否在3天及以内
     *
     * @param path
     * @return
     */
    private boolean checkApkDownloadTime(String path) {
        boolean ret = false;

        File file = new File(path);
        if (file.exists()) {
            ret = Math.abs(System.currentTimeMillis() - file.lastModified()) <= (86400000l * 1);
        }

        return ret;
    }

    // 排序
    private void sortApk() {
        // 先排大小,越来越大
        try {
            Collections.sort(mCheckedList, mComparatorLength);
        } catch (Exception e) {
            if (DEBUG) {
                Log.d(TAG, "sort apklist error ", e);
            }
        }

        // 再按包名进行归集
        combineWithPkg();
    }

    // 通过包名归集
    private void combineWithPkg() {
        int nCheckedCount = mCheckedList.size();
        if (nCheckedCount == 0) {
            return;
        }
        mOtherList.clear();
        // 从最大的开始
        for (int i = 0; i < nCheckedCount; i++) {
            TrashInfo one = mCheckedList.get(i);
            int nOtherCount = mOtherList.size();
            boolean isAdd = false;

            for (int j = nOtherCount - 1; j >= 0; j--) {
                // 如果包名相同，就添加到此包名组的前面
                if (checkPkg(one, mOtherList.get(j))) {
                    mOtherList.add(j + 1, one);
                    isAdd = true;
                    break;
                }
            }
            // 如果没有添加，直接添加到头部
            if (!isAdd) {
                mOtherList.add(one);
            }
        }

        mCheckedList.clear();
        mCheckedList.addAll(mOtherList);
        mOtherList.clear();
    }

    // 检测旧版本
    private void checkOld(List<TrashInfo> sortList) {
        int nCount = sortList.size();
        if (nCount == 0) {
            return;
        }

        for (int i = 0; i < nCount; i++) {
            for (int j = i; j < nCount; j++) {
                if (i != j) {
                    if (checkSubOld(sortList.get(i), sortList.get(j))) {

                    }
                }
            }
        }
    }

    // 检测重复包,冒泡排序方法查找重复包
    private void checkRepeat(List<TrashInfo> sortList) {
        int nCount = sortList.size();
        if (nCount == 0) {
            return;
        }

        for (int i = 0; i < nCount; i++) {
            for (int j = i; j < nCount; j++) {
                if (i != j) {
                    if (checkEquals(sortList.get(i), sortList.get(j))) {
                        // 将j位置标识为重复包，这样，保持第一个不会是重复包
                        sortList.get(j).dataType = TrashClearEnv.APK_TYPE_REPEAT;
                        // 重复包,勾选
                        sortList.get(j).clearType = TrashClearEnv.CLEAR_TYPE_ONEKEY;
                    }
                }
            }
        }
    }

    /**
     * 检查重复包 说明：argStr1=versionName;argInt1=versionCode;argStr2=packageName;
     * argInt2 = applicationInfo.icon;
     */
    private boolean checkEquals(TrashInfo one, TrashInfo another) {

        if (TextUtils.isEmpty(one.packageName) || TextUtils.isEmpty(another.packageName)) {
            return false;
        }
        String oneApkVersionName = one.bundle.getString(TrashClearEnv.apkVersionName);
        int oneApkVersionCode = one.bundle.getInt(TrashClearEnv.apkVersionCode);
        String anotherApkVersionName = another.bundle.getString(TrashClearEnv.apkVersionName);
        int anotherApkVersionCode = another.bundle.getInt(TrashClearEnv.apkVersionCode);

        if (one.packageName.equals(another.packageName) && oneApkVersionCode == anotherApkVersionCode) {
            if (oneApkVersionName != null && oneApkVersionName.equals(anotherApkVersionName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 检测包名是否相同 说明：argStr1=versionName;argInt1=versionCode;argStr2=packageName;
     * argInt2 = applicationInfo.icon;
     */
    private boolean checkPkg(TrashInfo one, TrashInfo another) {

        if (TextUtils.isEmpty(one.packageName) || TextUtils.isEmpty(another.packageName)) {
            return false;
        }

        if (one.packageName.equals(another.packageName)) {
            return true;
        }

        return false;
    }

    // 检查旧版本
    private boolean checkSubOld(TrashInfo one, TrashInfo another) {
        if (TextUtils.isEmpty(one.packageName) || TextUtils.isEmpty(another.packageName)) {
            return false;
        }

        if (!one.packageName.equals(another.packageName)) {
            return false;
        }
        String oneApkVersionName = one.bundle.getString(TrashClearEnv.apkVersionName);
        int oneApkVersionCode = one.bundle.getInt(TrashClearEnv.apkVersionCode);
        String anotherApkVersionName = another.bundle.getString(TrashClearEnv.apkVersionName);
        int anotherApkVersionCode = another.bundle.getInt(TrashClearEnv.apkVersionCode);

        TrashInfo changedInfo = null;
        if (oneApkVersionCode < anotherApkVersionCode) {
            changedInfo = one;
        } else if (oneApkVersionCode > anotherApkVersionCode) {
            changedInfo = another;
        } else if (oneApkVersionCode == anotherApkVersionCode) {

            if (null == oneApkVersionName || null == anotherApkVersionName) {

            } else if (oneApkVersionName.equals(anotherApkVersionName)) {

            } else {
                // 处理versionCode一样，但是versionName不同的安装包
                try {
                    String oneArray[] = oneApkVersionName.split("\\.");
                    String anotherArray[] = anotherApkVersionName.split("\\.");
                    if (null != oneArray && null != anotherArray) {
                        int oneLen = oneArray.length;
                        int anotherLen = anotherArray.length;
                        int len = oneLen;
                        if (oneLen > anotherLen) {
                            len = anotherLen;
                        }
                        for (int i = 0; i < len; i++) {
                            int oneVersion = Integer.valueOf(oneArray[i]);
                            int anotherVersion = Integer.valueOf(anotherArray[i]);
                            if (oneVersion > anotherVersion) {
                                break;
                            }
                            if (oneVersion < anotherVersion) {
                                changedInfo = one;
                                break;
                            }
                        }
                    }
                } catch (Exception e) {
                    if (DEBUG) {
                        Log.e(TAG, "compare versionName error", e);
                    }
                }
            }
        }

        // 如果已安装，就不修改为旧版本了
        if (changedInfo != null && changedInfo.dataType != TrashClearEnv.APK_TYPE_INSTALLED) {
            changedInfo.dataType = TrashClearEnv.APK_TYPE_OLDER;
            changedInfo.clearType = TrashClearEnv.CLEAR_TYPE_ONEKEY;
        }

        return false;
    }

    // 降序，越来越小
    private final Comparator<TrashInfo> mComparatorLength = new Comparator<TrashInfo>() {
        @Override
        public int compare(TrashInfo one, TrashInfo another) {

            if (one.size > another.size) {
                return -1;
            }
            if (one.size < another.size) {
                return 1;
            }

            return 0;
        }
    };
}
