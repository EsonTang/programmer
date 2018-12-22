package com.prize.qihoo.cleandroid.sdk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

import android.util.Log;

import com.qihoo360.mobilesafe.opti.env.clear.TrashClearEnv;
import com.qihoo360.mobilesafe.opti.i.trashclear.TrashClearCategory;
import com.qihoo360.mobilesafe.opti.i.trashclear.TrashInfo;

/**
 * 大文件归类整理 按照路径描述及来源进行整理
 */
public class BigFileSort {
    private static final boolean DEBUG = SDKEnv.DEBUG;

    private static final String TAG = DEBUG ? "BigFileSort" : BigFileSort.class.getSimpleName();

    private static final int BIG_FILE_SIZE = 10 * 1024 * 1024;

    public static void sort(TrashClearCategory trashClearCategory) {
        if (trashClearCategory.trashInfoList == null) {
            return;
        }

        ArrayList<TrashInfo> list = trashClearCategory.trashInfoList;
        HashMap<String, TrashInfo> hashMap = new HashMap<String, TrashInfo>();
        for (TrashInfo trashInfo : list) {
            if (DEBUG) {
                Log.e(TAG, "sort:" + trashInfo);
            }
            String src = trashInfo.bundle.getString(TrashClearEnv.src);
            if (src == null) {
                if (DEBUG) {
                    Log.e(TAG, "sort error data:" + trashInfo);
                }
                continue;
            }
            String comeFormPath = trashInfo.bundle.getString(TrashClearEnv.comeFormPath);
            if (comeFormPath == null) {
                comeFormPath = TrashClearEnv.BIGFILE_OTHER;
            }

            // comeFormPathDesc == null 表示是其他大文件
            String pathDesc = trashInfo.bundle.getString(TrashClearEnv.comeFormPathDesc);
            if (pathDesc == null) {
                comeFormPath = TrashClearEnv.BIGFILE_OTHER;
                pathDesc = TrashClearEnv.BIGFILE_OTHER;
                trashInfo.bundle.putBoolean(TrashClearEnv.isOtherBigFile, true);
            }

            TrashInfo rootInfo = hashMap.get(comeFormPath);
            if (rootInfo == null) {
                rootInfo = new TrashInfo();
                rootInfo.type = TrashClearEnv.CATE_BIGFILE;
                rootInfo.desc = pathDesc;
                rootInfo.packageName = trashInfo.packageName;
                if (!pathDesc.equals(TrashClearEnv.BIGFILE_OTHER)) {
                    rootInfo.bundle.putString(TrashClearEnv.src, src);
                }
                hashMap.put(comeFormPath, rootInfo);
            }

            ArrayList<TrashInfo> subList = rootInfo.bundle.getParcelableArrayList(TrashClearEnv.subList);
            if (subList == null) {
                subList = new ArrayList<TrashInfo>(8);
                rootInfo.bundle.putParcelableArrayList(TrashClearEnv.subList, subList);
            }
            subList.add(trashInfo);

            rootInfo.size += trashInfo.size;
            rootInfo.count++;

        }

        Set<Entry<String, TrashInfo>> set = hashMap.entrySet();

        TrashInfo otherBigfile = null;
        ArrayList<TrashInfo> resultList = new ArrayList<TrashInfo>();

        for (Entry<String, TrashInfo> entry : set) {
            TrashInfo trashInfo = entry.getValue();
            // 排序小类别
            TrashClearUtils.sort(trashInfo);

            if (trashInfo.desc.equals(TrashClearEnv.BIGFILE_OTHER)) {
                otherBigfile = trashInfo;
            } else {
                resultList.add(trashInfo);
            }
        }

        if (resultList != null) {
            for (TrashInfo alias : resultList) {
                aliasMerge(alias);
            }
        }
        // 排序列表
        TrashClearUtils.sort(resultList);

        // 其他大文件添加在列表后面
        if (otherBigfile != null) {
            aliasMerge(otherBigfile);
            resultList.add(otherBigfile);
        }

        trashClearCategory.trashInfoList = resultList;
    }

    public static void aliasMerge(TrashInfo info) {
        if (info == null || info.bundle == null) {
            return;
        }
        ArrayList<TrashInfo> subList = info.bundle.getParcelableArrayList(TrashClearEnv.subList);
        if (subList == null) {
            return;
        }
        HashMap<String, TrashInfo> hashMap = new HashMap<String, TrashInfo>();
        ArrayList<TrashInfo> newSubList = new ArrayList<TrashInfo>();
        for (int index = 0; index < subList.size(); index++) {
            TrashInfo subInfo = subList.get(index);
            if (subInfo == null || subInfo.bundle == null) {
                newSubList.add(subInfo);
                continue;
            }
            String mergedAlias = subInfo.bundle.getString(TrashClearEnv.mergedAliasBigFileFolder);
            if (mergedAlias == null) {
                newSubList.add(subInfo);
                continue;
            }
            TrashInfo rootItem = hashMap.get(mergedAlias);
            if (rootItem == null) {
                rootItem = subInfo;
                rootItem.path = mergedAlias;
                ArrayList<String> multiPathList = new ArrayList<String>();
                multiPathList.add(subInfo.path);
                rootItem.bundle.putStringArrayList(TrashClearEnv.multiPathList, multiPathList);
                hashMap.put(mergedAlias, rootItem);
            } else {
                ArrayList<String> multiPathList = rootItem.bundle.getStringArrayList(TrashClearEnv.multiPathList);
                multiPathList.add(subInfo.path);
                rootItem.size += subInfo.size;
            }
        }
        Set<Entry<String, TrashInfo>> set = hashMap.entrySet();
        for (Entry<String, TrashInfo> entry : set) {
            TrashInfo trashInfo = entry.getValue();
            if (trashInfo.size > BIG_FILE_SIZE) {
                newSubList.add(trashInfo);
            }
        }
        info.bundle.putParcelableArrayList(TrashClearEnv.subList, newSubList);
        TrashClearUtils.sort(info);
    }

}
