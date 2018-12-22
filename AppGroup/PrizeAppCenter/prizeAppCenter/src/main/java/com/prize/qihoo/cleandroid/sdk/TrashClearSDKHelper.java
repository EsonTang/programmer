
package com.prize.qihoo.cleandroid.sdk;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.qihoo.cleandroid.sdk.utils.ClearModuleUtils;
import com.qihoo360.mobilesafe.opti.env.clear.ClearOptionEnv;
import com.qihoo360.mobilesafe.opti.env.clear.ClearSharePrefEnv;
import com.qihoo360.mobilesafe.opti.env.clear.TrashClearEnv;
import com.qihoo360.mobilesafe.opti.env.clear.WhiteListEnv;
import com.qihoo360.mobilesafe.opti.i.trashclear.ICallbackTrashClear;
import com.qihoo360.mobilesafe.opti.i.trashclear.ICallbackTrashScan;
import com.qihoo360.mobilesafe.opti.i.trashclear.ITrashClear;
import com.qihoo360.mobilesafe.opti.i.trashclear.TrashClearCategory;
import com.qihoo360.mobilesafe.opti.i.trashclear.TrashInfo;
import com.qihoo360.mobilesafe.opti.i.whitelist.IUserBWList;
import com.qihoo360.mobilesafe.opti.i.whitelist.UserBWRecord;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 垃圾清理SDK调用接口
 */
public class TrashClearSDKHelper extends BaseOptiTask {

    protected static final boolean DEBUG = SDKEnv.DEBUG;

    protected static final String TAG = DEBUG ? "TrashClearSDKHelper" : TrashClearSDKHelper.class.getSimpleName();

    private final ITrashClear mTrashClear;

    // 结果概要信息
    private ResultSummaryInfo mResultSummaryInfo = new ResultSummaryInfo();

    // 线程同步锁
    private final static Object mLockObj = new Object();

    // 系统盘垃圾
    private TrashClearCategory mSystemItem;

    // 缓存垃圾
    private TrashClearCategory mAppCacheItem;

    // 缓存垃圾 保存在data/data/包名/cache 的缓存垃圾
    private TrashClearCategory mAppSystemCacheItem;

    // 缓存垃圾 缓存专项清理（针对缓存文件进行清理，而不是缓存文件夹）
    private TrashClearCategory mAppFileCacheItem;

    // 广告插件
    private TrashClearCategory mAdpluginItem;

    // 卸载残留
    private TrashClearCategory mUninstalledItem;

    // 安装包(未安装)
    private TrashClearCategory mApkItemUnInstalled;

    // 安装包(已安装)
    private TrashClearCategory mApkItemInstalled;

    // 所有安装包
    private TrashClearCategory mApkItemAll;

    // 大文件
    private TrashClearCategory mBigfileItem;

    // 用户设置白名单操作类
    private IUserBWList mUserBWListImpl;

    private TrashInfo WixinTrashInfo;

    private TrashInfo QQTrashInfo;

    private static TrashClearSDKHelper instance;

    /** 是否去除重叠数据 */
    private boolean isRemoveOverlapData = false;

    protected TrashClearSDKHelper(Context context) {
        super(context);
        mTrashClear = ClearModuleUtils.getTrashClearImpl(context);
    }

    public static TrashClearSDKHelper getInstance(Context context){
        if(instance == null){
            synchronized (mLockObj) {
                if(instance == null){
                    instance = new TrashClearSDKHelper(context);
                }
            }
        }
        return instance;
    }


    /**
     * 扫描接口会耗时，请在线程中调用
     */
    @Override
    public void scan() {
        if (DEBUG) {
            Log.i(TAG, "scan");
        }
        scanStart();
        init();
        try {
            ClearModuleUtils.getClearModulel(mContext).setOption(ClearOptionEnv.SCAN_ALIAS_BIGFILE, "1");
        } catch (Throwable e) {
            if (DEBUG) {
                Log.e(TAG, "getClearModulel", e);
            }
        }
        mTrashClear.setOption(TrashClearEnv.itcOptionBigFolderFastScan, "1");
        mTrashClear.scan(mType, mTrashTypes, mCallbackTrashScan);
    }

    /**
     * 清理所有类别的垃圾项，垃圾清理是阻塞的，请在线程中调用
     */
    @Override
    public void clear() {
        clear(TrashClearEnv.TYPE_ALL_ITEMS);
    }

    /**
     * 根据类型清理指定类别的垃圾
     *
     * @param type 垃圾类别 <br>
     *            TrashClearEnv.TYPE_ALL_ITEMS 清理所有项；<br>
     *            TrashClearEnv.TYPE_ONEKEY_CLEAR_ITEMS 只清理 放心清理项
     */
    public void clear(int type) {
        if (DEBUG) {
            Log.i(TAG, "clear " + type);
        }
        clearStart();

        ResultSummaryInfo resultSummaryInfo = getResultInfo();
        long clearTotalSize = 0;
        if (resultSummaryInfo != null) {
            clearTotalSize = resultSummaryInfo.selectedSize;
        }
        // 保存系统缓存勾选状态
        AppCacheCheck.saveAppCacheState(mAppCacheItem, mContext);

        // 清理前先剔除要清理的数据
        ArrayList<TrashInfo> clearList = new ArrayList<TrashInfo>();
        List<TrashClearCategory> cateList = getAllTrashClearCategoryList(); //nlg_add

        if (cateList != null) {
            for (TrashClearCategory trashClearCategory : cateList) {

                if (trashClearCategory.trashInfoList == null) {
                    continue;
                }

                TrashClearCategory newTrashClearCategory;
                try {
                    newTrashClearCategory = trashClearCategory.clone();
                } catch (CloneNotSupportedException e) {
                    if (DEBUG) {
                        e.printStackTrace();
                    }
                    continue;
                }
                ArrayList<TrashInfo> unClearList = new ArrayList<TrashInfo>();

                for (TrashInfo trashInfo : newTrashClearCategory.trashInfoList) {

                    ArrayList<TrashInfo> subList = trashInfo.bundle.getParcelableArrayList(TrashClearEnv.subList);

                    // 判断系统缓存是否可被清理
                    if (trashInfo.type == TrashClearEnv.CATE_APP_SYSTEM_CACHE) {
                        if (!AppCacheCheck.checkAppCacheCanClear(subList)) {
                            if (DEBUG) {
                                Log.i(TAG, "clear,ignore AppCache!");
                            }
                            unClearList.add(trashInfo);
                            continue;
                        }
                    }

                    if (subList == null || subList.size() < 1) {

                        if (trashInfo.isInWhiteList) {
                            unClearList.add(trashInfo);
                            continue;
                        }
                        if (!trashInfo.isSelected) {
                            unClearList.add(trashInfo);
                            continue;
                        }

                        if (TrashClearEnv.TYPE_ONEKEY_CLEAR_ITEMS == type) {
                            if (trashInfo.bundle.getBoolean(TrashClearEnv.ONEKEY_CLEAR_FLAG, false)) {
                                // 只清理放心清理项
                                clearList.add(trashInfo);
                            } else {
                                unClearList.add(trashInfo);
                                continue;
                            }
                        } else {
                            clearList.add(trashInfo);
                        }
                    } else {
                        // 有子路径信息时
                        ArrayList<TrashInfo> subInfoUnClearList = new ArrayList<TrashInfo>(subList.size());
                        for (TrashInfo subInfo : subList) {

                            if (subInfo.isInWhiteList) {
                                subInfoUnClearList.add(subInfo);
                                continue;
                            }
                            if (!subInfo.isSelected) {
                                subInfoUnClearList.add(subInfo);
                                continue;
                            }
                            if (TrashClearEnv.TYPE_ONEKEY_CLEAR_ITEMS == type) {
                                if (subInfo.bundle.getBoolean(TrashClearEnv.ONEKEY_CLEAR_FLAG, false)) {
                                    // 只清理放心清理项
                                    clearList.add(subInfo);
                                } else {
                                    subInfoUnClearList.add(subInfo);
                                    continue;
                                }
                            } else {
                                clearList.add(subInfo);
                            }
                        }
                        if (subInfoUnClearList.size() > 0) {
                            TrashInfo cloneInfo;
                            try {
                                cloneInfo = trashInfo.clone();
                            } catch (CloneNotSupportedException e) {
                                if (DEBUG) {
                                    e.printStackTrace();
                                }
                                continue;
                            }
                            cloneInfo.bundle.putParcelableArrayList(TrashClearEnv.subList, subInfoUnClearList);
                            unClearList.add(cloneInfo);
                        }
                    }
                }

                newTrashClearCategory.trashInfoList = unClearList;
                TrashClearUtils.refresh(newTrashClearCategory);

                switch (newTrashClearCategory.type) {
                    case TrashClearEnv.CATE_CACHE:
                        mAppCacheItem = newTrashClearCategory;
                        break;
                    case TrashClearEnv.CATE_UNINSTALLED:
                        mUninstalledItem = newTrashClearCategory;
                        break;
                    case TrashClearEnv.CATE_ADPLUGIN:
                        mAdpluginItem = newTrashClearCategory;
                        break;
                    case TrashClearEnv.CATE_BIGFILE:
                        mBigfileItem = newTrashClearCategory;
                        break;
                    case TrashClearEnv.CATE_APK:
                        mApkItemAll = newTrashClearCategory;
                        // 安装包单独排序(未安装)
                        if (mApkItemAll != null) {
                            divideApkItem();
                        }
                        break;
                    case TrashClearEnv.CATE_SYSTEM:
                        mSystemItem = newTrashClearCategory;
                        break;

                    default:
                        break;
                }

            }
        }

        if(WixinTrashInfo != null){
            refreshTrshInfo(type, WixinTrashInfo, clearList);
            refreshSummaryInfo(mResultSummaryInfo);
        }

        if(QQTrashInfo != null){
            refreshTrshInfo(type, QQTrashInfo, clearList);
            refreshSummaryInfo(mResultSummaryInfo);
        }
        //refreshWeixinQQInfo(); //nlg_add

        calResultSummaryInfo(null, false, true);

        // 保存清理记录
        long historyClear = SharedPrefUtils.getLong(mContext, SharedPrefUtils.SYSCLEAR_TRASH_HISTORY, 0);
        SharedPrefUtils.setLong(mContext, SharedPrefUtils.SYSCLEAR_TRASH_HISTORY, clearTotalSize + historyClear);
        if (DEBUG) {
            Log.d(TAG, "saveClearHistory clearTotalSize:" + TrashClearUtils.getHumanReadableSizeMore(clearTotalSize) + " historyClear:" + TrashClearUtils.getHumanReadableSizeMore(historyClear));
        }

        // 执行清理操作
        mTrashClear.setOption(TrashClearEnv.itcOptionMoveClear, "1");
        //修复友盟：java.lang.NullPointerException: Attempt to invoke virtual method 'char[] java.lang.String.toCharArray()' on a null object reference
        try {
            mTrashClear.clearByTrashInfo(clearList, mCallbackTrashClear);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void refreshTrshInfo(int type, TrashInfo trashInfo, ArrayList<TrashInfo> clearList){

        ArrayList<TrashInfo> subList = trashInfo.bundle.getParcelableArrayList(TrashClearEnv.subList);
        if (subList != null && subList.size() > 0){
            // 有子路径信息时
            ArrayList<TrashInfo> subInfoUnClearList = new ArrayList<TrashInfo>(subList.size());
            for (TrashInfo subInfo : subList) {

                if (subInfo.isInWhiteList) {
                    subInfoUnClearList.add(subInfo);
                    continue;
                }
                if (!subInfo.isSelected) {
                    subInfoUnClearList.add(subInfo);
                    continue;
                }
                if (TrashClearEnv.TYPE_ONEKEY_CLEAR_ITEMS == type) {
                    if (subInfo.bundle.getBoolean(TrashClearEnv.ONEKEY_CLEAR_FLAG, false)) {
                        // 只清理放心清理项
                        clearList.add(subInfo);
                    } else {
                        subInfoUnClearList.add(subInfo);
                        continue;
                    }
                } else {
                    clearList.add(subInfo);
                }
            }
            trashInfo.bundle.putParcelableArrayList(TrashClearEnv.subList, subInfoUnClearList);
        }
    }

    /**
     * 取消扫描
     */
    @Override
    public void cancelScan() {
        if (DEBUG) {
            Log.d(TAG, "cancelScan");
        }
        synchronized (mLockObj) {
            mTrashClear.cancelScan();
            super.cancelScan();
            dealScanFinished();
        }
    }

    /**
     * 取消清理
     */
    @Override
    public void cancelClear() {
        if (DEBUG) {
            Log.d(TAG, "cancelClear");
        }
        synchronized (mLockObj) {
            mTrashClear.cancelClear();
            super.cancelClear();
            dealClearFinished();
        }
    }

    private void dealScanFinished() {
        dealResult();
        calResultSummaryInfo(null, true, true);
        scanFinish();
        if (DEBUG) {
            Log.d(TAG, "dealScanFinished");
        }
    }

    private void dealClearFinished() {
        if (DEBUG) {
            Log.d(TAG, "dealScanFinished");
        }
        clearFinish();
    }

    /**
     * 获取结果信息
     *
     * @return
     */
    public ResultSummaryInfo getResultInfo() {
        synchronized (mLockObj) {
            return mResultSummaryInfo;
        }
    }

    private void calResultSummaryInfo(ArrayList<String> linkageApkCheckedPath, boolean isLinkageAllBigfileChecked, boolean isReSpitData) {
        long start = System.currentTimeMillis();
        synchronized (mLockObj) {
            List<TrashClearCategory> allList = getAllTrashClearCategoryList(); //nld_add
            mResultSummaryInfo = filterOverlapData(allList, linkageApkCheckedPath, isLinkageAllBigfileChecked);
            if (mExpandCallback != null) {
                if (isReSpitData) {
                    mExpandCallback.reSpitData();
                } else {
                    mExpandCallback.refreshData();
                }
            }
        }
        if (DEBUG) {
            Log.d(TAG, "calResulSummaryInfo spend time:" + (System.currentTimeMillis() - start));
        }
    }

    /**
     * 因缓存目录和安装包、大文件存在包含关系,因此引入联动策略。单项联动，避免误删 和删除不彻底的问题<br>
     * 一：扫描结束时，做一次遍历，若缓存目录勾选，其内的大文件也默认勾选。其内的安装包不联动(不勾选)。<br>
     * 二：用户手动勾选缓存目录时，其内的大文件、安装包也联动勾选。 <br>
     * 三：用户点击大文件、安装包时，其父节点缓存目录不联动(单向联动)。 <br>
     * 四：清理缓存目录时，不删除其内的安装包、大文件；安装包、大文件是否删除删除以来自身的勾选状态；
     */
    private ResultSummaryInfo filterOverlapData(List<TrashClearCategory> allList, ArrayList<String> linkageCheckedPathList, boolean isLinkageAllBigfileChecked) {
        if (DEBUG) {
            Log.d(TAG, "filterOverlapData start: isLinkageAllBigfileChecked:" + linkageCheckedPathList + " isLinkageAllBigfileChecked:" + isLinkageAllBigfileChecked);
        }

        ResultSummaryInfo resultSummaryInfo = new ResultSummaryInfo();

        // 1、提取重叠的数据
        HashMap<String, TrashInfo> trashInfoMapSrc = new HashMap<String, TrashInfo>();
        HashMap<String, ArrayList<TrashInfo>> overlapTrashInfoMap = new HashMap<String, ArrayList<TrashInfo>>();
        for (TrashClearCategory trashClearCategory : allList) {
            switch (trashClearCategory.type) {
                case TrashClearEnv.CATE_CACHE:
                case TrashClearEnv.CATE_UNINSTALLED:
                case TrashClearEnv.CATE_ADPLUGIN:
                case TrashClearEnv.CATE_SYSTEM:
                    List<TrashInfo> trashList = trashClearCategory.trashInfoList;
                    for (TrashInfo trashInfo : trashList) {
                        // 可能会与大文件、安装包存在重叠数据的类型
                        if (TrashClearEnv.CATE_APP_SD_CACHE == trashInfo.type || TrashClearEnv.CATE_UNINSTALLED == trashInfo.type || TrashClearEnv.CATE_ADPLUGIN == trashInfo.type
                                || TrashClearEnv.CATE_SYSTEM_TRASH == trashInfo.type || TrashClearEnv.CATE_FILE_CACHE == trashInfo.type) {
                            List<TrashInfo> subList = trashInfo.bundle.getParcelableArrayList(TrashClearEnv.subList);
                            if (subList == null) {
                                trashInfoMapSrc.put(trashInfo.path, trashInfo);
                            } else {
                                for (TrashInfo trashInfo2 : subList) {
                                    trashInfoMapSrc.put(trashInfo2.path, trashInfo2);
                                }
                            }
                        }
                    }

                    break;
                case TrashClearEnv.CATE_APK:
                case TrashClearEnv.CATE_BIGFILE:
                    trashList = trashClearCategory.trashInfoList;
                    for (TrashInfo trashInfo : trashList) {
                        List<TrashInfo> subList = trashInfo.bundle.getParcelableArrayList(TrashClearEnv.subList);
                        if (subList == null) {
                            setOverlapList(overlapTrashInfoMap, trashInfo);
                        } else {
                            for (TrashInfo subInfo : subList) {
                                setOverlapList(overlapTrashInfoMap, subInfo);
                            }
                        }
                    }
                    break;

                default:
                    break;
            }
        }

        // 2、同步重叠数据选中状态并计算重叠数据大小
        long overlapTotalSize = 0;
        long overlapCheckedSize = 0;
        HashSet<Integer> overlapTypeSet = new HashSet<Integer>();
        Set<String> pathListSrc = trashInfoMapSrc.keySet();
        for (String overlapPathSrc : pathListSrc) {

            ArrayList<TrashInfo> overlapList = overlapTrashInfoMap.get(overlapPathSrc);
            if (overlapList == null) {
                continue;
            }
            TrashInfo trashInfo1 = trashInfoMapSrc.get(overlapPathSrc);
            for (TrashInfo overlapTrashInfo : overlapList) {

                if (TrashClearEnv.CATE_APK == overlapTrashInfo.type && (linkageCheckedPathList != null && linkageCheckedPathList.contains(overlapPathSrc))) {
                    // 同步重叠数据勾选状态
                    overlapTrashInfo.isSelected = trashInfo1.isSelected && !trashInfo1.isInWhiteList;

                    if (DEBUG) {
                        Log.v(TAG, "overlap setSelected apk " + overlapPathSrc + overlapTrashInfo);
                    }

                } else if (TrashClearEnv.CATE_BIGFILE == overlapTrashInfo.type && (isLinkageAllBigfileChecked || (linkageCheckedPathList != null && linkageCheckedPathList.contains(overlapPathSrc)))) {
                    // 同步重叠数据勾选状态
                    overlapTrashInfo.isSelected = trashInfo1.isSelected && !trashInfo1.isInWhiteList;
                    overlapTrashInfo.isInWhiteList = trashInfo1.isInWhiteList;
                    if (DEBUG) {
                        Log.v(TAG, "overlap setSelected bigfile " + overlapPathSrc + overlapTrashInfo);
                    }
                }

                overlapTotalSize += overlapTrashInfo.size;
                // 双方都选中时才算重叠选中
                if (trashInfo1.isSelected && !trashInfo1.isInWhiteList && overlapTrashInfo.isSelected && !overlapTrashInfo.isInWhiteList) {
                    overlapCheckedSize += overlapTrashInfo.size;
                    if (DEBUG) {
                        Log.v(TAG, "overlap selected 3:" + overlapTrashInfo);
                    }
                }
                overlapTypeSet.add(overlapTrashInfo.type);
            }
        }

        // 3、重新计算选中状态变化的数据
        for (Integer type : overlapTypeSet) {
            TrashClearUtils.refresh(getCateItem(type));
        }

        for (TrashClearCategory trashClearCategory : allList) {
            resultSummaryInfo.selectedCount += trashClearCategory.selectedCount;
            resultSummaryInfo.selectedSize += trashClearCategory.selectedSize;
            resultSummaryInfo.count += trashClearCategory.count;
            resultSummaryInfo.size += trashClearCategory.size;
        }

        if (DEBUG) {
            Log.d(TAG, "filterOverlapData before overlap:" + resultSummaryInfo);
        }
        // 4、计算、去除重叠数据
        if (isRemoveOverlapData) {
            resultSummaryInfo.size -= overlapTotalSize;
            resultSummaryInfo.selectedSize -= overlapCheckedSize;
        }
        if (DEBUG) {
            Log.d(TAG, "filterOverlapData after overlap:" + resultSummaryInfo);
        }
        refreshSummaryInfo(resultSummaryInfo);
        return resultSummaryInfo;
    }

    public void refreshSummaryInfo(ResultSummaryInfo resultSummaryInfo){

        long weixinSize = 0, qqSize = 0, weixinSelectedSize = 0, qqSelectedSize = 0, weixinSizeChange = 0, qqSizeChange = 0;
        if(WixinTrashInfo != null) {
            List<TrashInfo> weixinSubList = WixinTrashInfo.bundle.getParcelableArrayList(TrashClearEnv.subList);
            if(weixinSubList==null)
                return;
            for (TrashInfo info : weixinSubList) {
                weixinSize += info.size;
                weixinSelectedSize += info.isSelected ? info.size : 0;
            }
            weixinSizeChange = weixinSize - resultSummaryInfo.weixinSize;
            WixinTrashInfo.size = weixinSize;
        }

        if(QQTrashInfo != null) {
            List<TrashInfo> qqSubList = QQTrashInfo.bundle.getParcelableArrayList(TrashClearEnv.subList);
            if(qqSubList==null)
                return;
            for (TrashInfo info : qqSubList) {
                qqSize += info.size;
                qqSelectedSize += info.isSelected ? info.size : 0;
            }
            qqSizeChange = qqSize - resultSummaryInfo.qqSize;
            QQTrashInfo.size = qqSize;
        }

        long qqSelectedChange = qqSelectedSize - resultSummaryInfo.qqSelectedSize;
        long weixinSelectedChange = weixinSelectedSize - resultSummaryInfo.weixinSelectedSize;

        resultSummaryInfo.weixinSize = weixinSize;
        resultSummaryInfo.qqSize = qqSize;
        resultSummaryInfo.weixinSelectedSize = weixinSelectedSize;
        resultSummaryInfo.qqSelectedSize = qqSelectedSize;
        resultSummaryInfo.selectedSize += (qqSelectedChange+weixinSelectedChange);
        resultSummaryInfo.size += (weixinSizeChange + qqSizeChange);
    }

    public long getSuggestSize(){
        long suggestSize = 0;
        List<TrashClearCategory> list1 = getTrashClearCategoryList();
        List<TrashClearCategory> list2 = getSafeTrashClearCategoryList();
        for (TrashClearCategory category : list1) {
            if(category.type == TrashClearEnv.CATE_CACHE){
                suggestSize += category.size;
            }
        }
        for (TrashClearCategory category :
                list2) {
            suggestSize += category.size;
        }
        return suggestSize;
    }

    /** 设置计算扫描结果时是否去除重复数据，避免结果异常，如（缓存垃圾中可能包含大文件，统计总大小是是否去除重复的部分） */
    public void setRemoveOverlapData(boolean isRemoveOverlapData) {
        this.isRemoveOverlapData = isRemoveOverlapData;
    }

    private void setOverlapList(HashMap<String, ArrayList<TrashInfo>> overlapTrashInfoMap, TrashInfo trashInfo) {
        String overlapPath = trashInfo.bundle.getString(TrashClearEnv.overlapPath);
        if (overlapPath != null) {
            ArrayList<TrashInfo> list = overlapTrashInfoMap.get(overlapPath);
            if (list == null) {
                list = new ArrayList<TrashInfo>(3);
            }
            list.add(trashInfo);
            overlapTrashInfoMap.put(overlapPath, list);
        }
    }

    /**
     * 获取所有扫描到的垃圾
     */
    public List<TrashClearCategory> getAllTrashClearCategoryList() {
        if (DEBUG) {
            Log.d(TAG, "getTrashClearCategoryList");
        }
        ArrayList<TrashClearCategory> list = new ArrayList<TrashClearCategory>(6);
        if (mSystemItem != null && mSystemItem.count > 0) {
            list.add(mSystemItem);
        }
        if (mAdpluginItem != null && mAdpluginItem.count > 0) {
            list.add(mAdpluginItem);
        }
        if (mUninstalledItem != null && mUninstalledItem.count > 0) {
            list.add(mUninstalledItem);
        }
        if (mAppCacheItem != null && mAppCacheItem.count > 0) {
            list.add(mAppCacheItem);
        }
        if (mApkItemAll != null && mApkItemAll.count > 0) {
            list.add(mApkItemAll);
        }
        if (mBigfileItem != null && mBigfileItem.count > 0) {
            list.add(mBigfileItem);
        }
        return list;
    }

    /**
     * 获取非安全清理的垃圾
     */
    public List<TrashClearCategory> getTrashClearCategoryList() {
        if (DEBUG) {
            Log.d(TAG, "getTrashClearCategoryList");
        }
        ArrayList<TrashClearCategory> list = new ArrayList<TrashClearCategory>(5);
       /* if (mSystemItem != null && mSystemItem.count > 0) {
            list.add(mSystemItem);
        }
        if (mAdpluginItem != null && mAdpluginItem.count > 0) {
            list.add(mAdpluginItem);
        }*/
        if (mAppCacheItem != null && mAppCacheItem.count > 0) {
            list.add(mAppCacheItem);
        }
        if (mApkItemUnInstalled != null && mApkItemUnInstalled.count > 0) {
            list.add(mApkItemUnInstalled);
        }
        if (mBigfileItem != null && mBigfileItem.count > 0) {
            list.add(mBigfileItem);
        }
        return list;
    }

    /**
     * 获取可安全清理的垃圾 nlg_add
     */
    public List<TrashClearCategory> getSafeTrashClearCategoryList() {
        if (DEBUG) {
            Log.d(TAG, "getTrashClearCategoryList");
        }
        List<TrashClearCategory> list = new ArrayList<TrashClearCategory>(4);
        if (mSystemItem != null && mSystemItem.count > 0) {
            list.add(mSystemItem);
        }
        /*if (mAppCacheItem != null && mAppCacheItem.count > 0) {
            list.add(mAppCacheItem);
        }*/
        if (mAdpluginItem != null && mAdpluginItem.count > 0) {
            list.add(mAdpluginItem);
        }
        if (mUninstalledItem != null && mUninstalledItem.count > 0) {
            if(mUninstalledItem.size > 0) {
                list.add(mUninstalledItem);
            }
        }
        if (mApkItemInstalled != null && mApkItemInstalled.count > 0) {
            list.add(mApkItemInstalled);
        }
        return list;
    }

    /**
     * 根据类别获取指定的垃圾
     *
     * @param cateType 垃圾类别
     * @return 垃圾
     */
    public TrashClearCategory getTrashClearCategory(int cateType) {
        TrashClearCategory trashClearCategory = null;
        switch (cateType) {
            case TrashClearEnv.CATE_CACHE:
                trashClearCategory = mAppCacheItem;
                break;
            case TrashClearEnv.CATE_UNINSTALLED:
                trashClearCategory = mUninstalledItem;
                break;
            case TrashClearEnv.CATE_APK:
                trashClearCategory = mApkItemAll;
                break;
            case TrashClearEnv.CATE_BIGFILE:
                trashClearCategory = mBigfileItem;
                break;
            case TrashClearEnv.CATE_ADPLUGIN:
                trashClearCategory = mAdpluginItem;
                break;
            case TrashClearEnv.CATE_SYSTEM:
                trashClearCategory = mSystemItem;
                break;

            default:
                break;
        }

        return trashClearCategory == null ? new TrashClearCategory(cateType) : trashClearCategory;
    }

    /**
     * 获取要清理的垃圾列表
     *
     * @param type 垃圾类别： TYPE_ONEKEY_CLEAR_ITEMS 放心清理的垃圾 或者 用户勾选的所有垃圾
     * @return
     */
    public List<TrashInfo> getClearList(int type) {
        if (DEBUG) {
            Log.d(TAG, "getClearList");
        }

        ArrayList<TrashInfo> clearList = new ArrayList<TrashInfo>();
        List<TrashClearCategory> cateList = getAllTrashClearCategoryList(); //nld_add
        if (cateList != null) {
            for (TrashClearCategory trashClearCategory : cateList) {
                if (trashClearCategory.trashInfoList == null) {
                    continue;
                }
                for (TrashInfo trashInfo : trashClearCategory.trashInfoList) {
                    ArrayList<TrashInfo> subList = trashInfo.bundle.getParcelableArrayList(TrashClearEnv.subList);
                    if (subList == null || subList.size() == 0) {

                        if (!trashInfo.isSelected) {
                            continue;
                        }
                        if (trashInfo.isInWhiteList) {
                            continue;
                        }

                        if (TrashClearEnv.TYPE_ONEKEY_CLEAR_ITEMS == type) {
                            if (trashInfo.bundle.getBoolean(TrashClearEnv.ONEKEY_CLEAR_FLAG, false)) {
                                // 只清理放心清理项
                                clearList.add(trashInfo);
                            }
                        } else {
                            clearList.add(trashInfo);
                        }
                    } else {
                        for (TrashInfo subInfo : subList) {
                            if (!subInfo.isSelected) {
                                continue;
                            }
                            if (subInfo.isInWhiteList) {
                                continue;
                            }
                            if (TrashClearEnv.TYPE_ONEKEY_CLEAR_ITEMS == type) {
                                if (subInfo.bundle.getBoolean(TrashClearEnv.ONEKEY_CLEAR_FLAG, false)) {
                                    // 只清理放心清理项
                                    clearList.add(subInfo);
                                }
                            } else {
                                clearList.add(subInfo);
                            }
                        }
                    }

                }
            }
        }
        return clearList;
    }

    /**
     * 垃圾类别勾选状态改变，刷新子项勾选状态和统计结果
     */
    public void onTrashClearCategorySelectedChanged(TrashClearCategory trashClearCategory) {
        TrashClearUtils.onTrashClearCategorySelectedChanged(trashClearCategory);
        refreshCategory(trashClearCategory);
    }

    /**
     * 垃圾类别全选，刷新子项勾选状态和统计结果
     */
    public void trashClearCategorySelectedAll(TrashClearCategory trashClearCategory) {
        TrashClearUtils.trashClearCategorySelectedAll(trashClearCategory);
        refreshCategory(trashClearCategory);
    }

    /**
     * 垃圾类别全不选，刷新子项勾选状态和统计结果
     */
    public void trashClearCategoryDeSelectedAll(TrashClearCategory trashClearCategory) {
        TrashClearUtils.trashClearCategoryDeSelectedAll(trashClearCategory);
        refreshCategory(trashClearCategory);
    }

    /**
     * 刷新垃圾统计结果
     */
    public void refreshCategory(TrashClearCategory trashClearCategory) {
        TrashClearUtils.refresh(trashClearCategory);
		//刷新所有安装包
        if(trashClearCategory.type == TrashClearEnv.CATE_APK){
            TrashClearUtils.refresh(mApkItemAll);
        }
        calResultSummaryInfo(isNeedLinkageChecked(trashClearCategory.type) ? getLinkedCheckedPathList(trashClearCategory) : null, isNeedLinkageChecked(trashClearCategory.type), false);
    }

    /**
     * 单项垃圾勾选状态改变，刷新统计结果
     */
    public void onTrashInfoSelectedChanged(TrashInfo trashInfo) {

        if(TrashClearEnv.CATE_APK == trashInfo.type){
            for (TrashInfo info : mApkItemAll.trashInfoList) {
                if(info.packageName == trashInfo.packageName){
                    TrashClearUtils.onTrashInfoSelectedChanged(info, !trashInfo.isSelected);
                }
            }
            TrashClearUtils.refresh(getCateItem(trashInfo.type));

            if(trashInfo.dataType == TrashClearEnv.APK_TYPE_INSTALLED){
                TrashClearUtils.refresh(mApkItemInstalled);
            }else {
                TrashClearUtils.refresh(mApkItemUnInstalled);
            }
        }else {
            TrashClearUtils.onTrashInfoSelectedChanged(trashInfo, !trashInfo.isSelected);
            TrashClearUtils.refresh(getCateItem(trashInfo.type));
        }
        calResultSummaryInfo(isNeedLinkageChecked(trashInfo.type) ? getLinkedCheckedPathList(trashInfo) : null, false, false);
    }

    private ArrayList<String> getLinkedCheckedPathList(TrashClearCategory trashClearCategory) {
        ArrayList<String> linkageCheckedPathList = new ArrayList<String>();
        for (TrashInfo trashInfo : trashClearCategory.trashInfoList) {
            switch (trashInfo.type) {
                case TrashClearEnv.CATE_APP_SD_CACHE:
                case TrashClearEnv.CATE_ADPLUGIN:
                case TrashClearEnv.CATE_UNINSTALLED:
                    linkageCheckedPathList.addAll(getLinkedCheckedPathList(trashInfo));
                    break;

                default:
                    break;
            }
        }
        return linkageCheckedPathList;
    }

    private ArrayList<String> getLinkedCheckedPathList(TrashInfo trashInfo) {
        ArrayList<String> linkageCheckedPathList = new ArrayList<String>(1);
        List<TrashInfo> trashInfoList = trashInfo.bundle.getParcelableArrayList(TrashClearEnv.subList);
        if (trashInfoList != null) {
            for (TrashInfo subInfo : trashInfoList) {
                linkageCheckedPathList.add(subInfo.path);
            }
        } else {
            linkageCheckedPathList.add(trashInfo.path);
        }
        return linkageCheckedPathList;
    }

    private boolean isNeedLinkageChecked(int type) {
        return !(TrashClearEnv.CATE_APK == type || TrashClearEnv.CATE_BIGFILE == type);
    }

    /**
     * 白名单状态改变
     */
    public void onWhiteListStatusChanged(TrashInfo trashInfo) {

        trashInfo.isInWhiteList = !trashInfo.isInWhiteList;

        // 安装包加白时，需要刷新所有的目录中安装包
        if (TrashClearEnv.CATE_APK == trashInfo.type) {
            ArrayList<TrashInfo> apkList = mApkItemAll.trashInfoList;
            String apkDirPath = trashInfo.bundle.getString(TrashClearEnv.dirPath);
            for (TrashInfo apkInfo : apkList) {
                if (apkInfo.bundle.getString(TrashClearEnv.dirPath).startsWith(apkDirPath)) {
                    apkInfo.isInWhiteList = trashInfo.isInWhiteList;
                }
            }
        }

        TrashClearUtils.refresh(getCateItem(trashInfo.type));

        calResultSummaryInfo(isNeedLinkageChecked(trashInfo.type) ? getLinkedCheckedPathList(trashInfo) : null, false, false);

        if (mUserBWListImpl == null) {
            mUserBWListImpl = ClearModuleUtils.getUserBWListImpl(mContext, WhiteListEnv.TYPE_USER_CACHE);
        }

        if (trashInfo.isInWhiteList) {
            mUserBWListImpl.insert(TrashClearUtils.tranUserBWRecord(trashInfo));
        } else {
            mUserBWListImpl.remove(TrashClearUtils.tranUserBWRecord(trashInfo));
        }
        mUserBWListImpl.save();
    }

    /**
     * 白名单状态改变,刷新数据
     */
    public void onWhiteListStatusChanged(List<UserBWRecord> userBWRecordList) {
        if (userBWRecordList == null) {
            return;
        }

        // 先收集所有的TrashInfo List
        List<TrashClearCategory> allTrashClearCategorieList = getAllTrashClearCategoryList();//nld_add
        if (allTrashClearCategorieList == null) {
            return;
        }
        ArrayList<TrashInfo> allTrashInfoList = new ArrayList<TrashInfo>();
        for (TrashClearCategory trashClearCategory : allTrashClearCategorieList) {
            if (trashClearCategory.trashInfoList == null) {
                continue;
            }
            for (TrashInfo trashInfo : trashClearCategory.trashInfoList) {
                ArrayList<TrashInfo> subList = trashInfo.bundle.getParcelableArrayList(TrashClearEnv.subList);
                if (subList == null) {
                    allTrashInfoList.add(trashInfo);
                } else {
                    allTrashInfoList.addAll(subList);
                }
            }
        }

        HashSet<Integer> typeSet = new HashSet<Integer>(1);

        // 匹配白名单
        for (UserBWRecord userBWRecord : userBWRecordList) {

            String whiteListPath = userBWRecord.value;
            if (TextUtils.isEmpty(whiteListPath)) {
                continue;
            }

            // 添加刷新类型
            typeSet.add(userBWRecord.type);

            boolean isInWhiteList = (userBWRecord.flag == WhiteListEnv.USER_SELECTION_NOT_KILL);

            for (TrashInfo trashInfo : allTrashInfoList) {

                if (TrashClearEnv.CATE_APK == userBWRecord.type) {

                    if (TrashClearEnv.CATE_APK == trashInfo.type) {
                        if (trashInfo.bundle.getString(TrashClearEnv.dirPath).startsWith(whiteListPath)) {
                            trashInfo.isInWhiteList = isInWhiteList;
                        }
                    }

                } else if (TrashClearEnv.CATE_FILE_CACHE == userBWRecord.type || TrashClearEnv.CATE_CACHE == userBWRecord.type || TrashClearEnv.CATE_APP_SD_CACHE == userBWRecord.type
                        || TrashClearEnv.CATE_UNINSTALLED == userBWRecord.type) {

                    if (TrashClearEnv.CATE_FILE_CACHE == trashInfo.type || TrashClearEnv.CATE_CACHE == trashInfo.type || TrashClearEnv.CATE_APP_SD_CACHE == trashInfo.type
                            || TrashClearEnv.CATE_UNINSTALLED == trashInfo.type) {
                        if (whiteListPath.equals(trashInfo.path)) {
                            trashInfo.isInWhiteList = isInWhiteList;
                        }
                    }

                }
            }
        }

        for (Integer type : typeSet) {
            switch (type) {
                case TrashClearEnv.CATE_CACHE:
                case TrashClearEnv.CATE_APP_SD_CACHE:
                case TrashClearEnv.CATE_FILE_CACHE:
                    TrashClearUtils.refresh(getCateItem(TrashClearEnv.CATE_APP_SD_CACHE));
                    break;
                case TrashClearEnv.CATE_APK:
                    TrashClearUtils.refresh(getCateItem(TrashClearEnv.CATE_APK));
                    break;
                case TrashClearEnv.CATE_UNINSTALLED:
                    TrashClearUtils.refresh(getCateItem(TrashClearEnv.CATE_UNINSTALLED));

                    break;

                default:
                    break;
            }
        }

        calResultSummaryInfo(null, false, false);

    }

    /**
     * 获取历史清理记录
     */
    public long getClearHistory() {
        long trashClearHistory = SharedPrefUtils.getLong(mContext, ClearSharePrefEnv.SYSCLEAR_TRASH_HISTORY, 0);
        if (DEBUG) {
            Log.d(TAG, "getClearHistory trashClearHistory:" + TrashClearUtils.getHumanReadableSizeMore(trashClearHistory));
        }

        return trashClearHistory;
    }

    /**
     * 释放资源
     */
    @Override
    public void onDestroy() {
        if (DEBUG) {
            Log.i(TAG, "onDestroy");
        }
    }

    // 垃圾扫描回调
    private final ICallbackTrashScan mCallbackTrashScan = new ICallbackTrashScan() {

        long selectedSize = 0;

        long totalSize = 0;

        @Override
        public void onStart() {
            if (DEBUG) {
                Log.i(TAG, "onStart");
            }
            selectedSize = 0;
            totalSize = 0;
        }

        @Override
        public void onProgress(int scanned, int total, String scaningItem) {
            if (DEBUG) {
                Log.v(TAG, "onProgress scanned:" + scanned + " total:" + total + " scaningItem:" + scaningItem);
            }
            synchronized (mLockObj) {
                if (isScanCancelled()) {
                    return;
                }

                if (mScanCallback != null) {
                    mScanCallback.onProgressUpdate(scanned, total);
                }
            }
        }

        @Override
        public void onFoundItem(TrashInfo trashInfo) {
            synchronized (mLockObj) {
                if (isScanCancelled()) {
                    return;
                }
                foundItem(trashInfo);

                switch (trashInfo.type) {
                    case TrashClearEnv.CATE_APK:
                    case TrashClearEnv.CATE_BIGFILE:
                        // 重叠数据，不进行扫描数据进度更新
                        if (trashInfo.bundle.getString(TrashClearEnv.overlapPath) != null) {
                            return;
                        }

                        break;

                    default:
                        break;
                }
                // 扫描数据进度更新
                if (trashInfo.isSelected) {
                    selectedSize += trashInfo.size;
                }
                totalSize += trashInfo.size;

                if (mScanCallback != null) {
                    mScanCallback.onDataUpdate(totalSize, selectedSize, trashInfo);
                }
            }
        }

        @Override
        public void onFinished(int resultCode) {
            if (DEBUG) {
                Log.i(TAG, "onFinished resultCode:" + resultCode);
            }
            synchronized (mLockObj) {
                if (isScanCancelled()) {
                    return;
                }
                dealScanFinished();
            }
        }
    };

    // 垃圾清理回调
    private final ICallbackTrashClear mCallbackTrashClear = new ICallbackTrashClear() {

        @Override
        public void onStart() {
            if (DEBUG) {
                Log.i(TAG, "onStart");
            }
        }

        @Override
        public void onProgress(int cleared, int total, TrashInfo trashInfo) {
            synchronized (mLockObj) {
                if (isClearCancelled()) {
                    return;
                }
            }
            if (DEBUG) {
                Log.v(TAG, "onProgress cleared:" + cleared + " total:" + total + " clearingItem:" + trashInfo);
            }
        }

        @Override
        public void onFinished(int resultCode) {
            if (DEBUG) {
                Log.i(TAG, "onFinished resultCode:" + resultCode);
            }
            synchronized (mLockObj) {
                if (isClearCancelled()) {
                    return;
                }
                dealClearFinished();
            }
        }
    };

    protected void foundItem(TrashInfo trashInfo) {
        if (DEBUG) {
            Log.v(TAG, "foundItem:" + trashInfo);
        }
        if (trashInfo == null) {
            return;
        }

        TrashClearCategory trashClearCategory = null;

        // 系统缓存mAppSystemCacheItem 会被合并到 mAppCacheItem 中，需要特殊处理
        if (TrashClearEnv.CATE_APP_SYSTEM_CACHE == trashInfo.type) {
            trashClearCategory = mAppSystemCacheItem;
        } else if (TrashClearEnv.CATE_FILE_CACHE == trashInfo.type) {// 缓存文件专清
            trashClearCategory = mAppFileCacheItem;
        } else {
            trashClearCategory = getCateItem(trashInfo.type);
        }

        if (trashClearCategory != null) {
            trashClearCategory.trashInfoList.add(trashInfo);
        }
    }

    private TrashClearCategory getCateItem(int type) {
        TrashClearCategory item = null;
        switch (type) {
            case TrashClearEnv.CATE_APP_SD_CACHE:
            case TrashClearEnv.CATE_APP_SYSTEM_CACHE:
            case TrashClearEnv.CATE_FILE_CACHE:// 缓存文件专清
                item = mAppCacheItem;
                break;
            case TrashClearEnv.CATE_UNINSTALLED:
                item = mUninstalledItem;
                break;
            case TrashClearEnv.CATE_ADPLUGIN:
                item = mAdpluginItem;
                break;
            case TrashClearEnv.CATE_BIGFILE:
                item = mBigfileItem;
                break;
            case TrashClearEnv.CATE_APK:
                item = mApkItemAll;
                break;
            case TrashClearEnv.CATE_SYSTEM_TEMP:
            case TrashClearEnv.CATE_SYSTEM_THUMBNAIL:
            case TrashClearEnv.CATE_SYSTEM_INVALID_THUMBNAIL:
            case TrashClearEnv.CATE_SYSTEM_LOG:
            case TrashClearEnv.CATE_SYSTEM_LOSTDIR:
            case TrashClearEnv.CATE_SYSTEM_EMPTYDIR:
            case TrashClearEnv.CATE_SYSTEM_TRASH:
            case TrashClearEnv.CATE_SYSTEM_RT_TRASH:
            case TrashClearEnv.CATE_SYSTEM_BAK:
                item = mSystemItem;
                break;

            default:
                break;
        }
        return item;
    }

    // 扫描开始 初始化
    private void init() {
        mAppCacheItem = new TrashClearCategory(TrashClearEnv.CATE_CACHE);
        mAppFileCacheItem = new TrashClearCategory(TrashClearEnv.CATE_FILE_CACHE);// 缓存专项清理
        mAppSystemCacheItem = new TrashClearCategory(TrashClearEnv.CATE_APP_SYSTEM_CACHE);
        mAdpluginItem = new TrashClearCategory(TrashClearEnv.CATE_ADPLUGIN);
        mUninstalledItem = new TrashClearCategory(TrashClearEnv.CATE_UNINSTALLED);
        mApkItemUnInstalled = new TrashClearCategory(TrashClearEnv.CATE_APK);
        mApkItemInstalled = new TrashClearCategory(TrashClearEnv.CATE_APK);
        mApkItemAll = new TrashClearCategory(TrashClearEnv.CATE_APK);
        mSystemItem = new TrashClearCategory(TrashClearEnv.CATE_SYSTEM);
        mBigfileItem = new TrashClearCategory(TrashClearEnv.CATE_BIGFILE);
        mResultSummaryInfo = new ResultSummaryInfo();
    }

    /**
     * 将专项清理数据合并到垃圾缓存
     */
    private void fileCacheAddToAppCache() {
        long startTime = System.currentTimeMillis();
        if (mAppFileCacheItem==null||mAppCacheItem==null||mAppFileCacheItem.trashInfoList == null || mAppFileCacheItem.trashInfoList.size() == 0 || mAppCacheItem.trashInfoList == null || mAppCacheItem.trashInfoList.size() == 0) {
            return;
        }

        TrashInfo parentInfo = null;
        for (TrashInfo fileInfo : mAppFileCacheItem.trashInfoList) {
            parentInfo = null;
            for (TrashInfo appInfo : mAppCacheItem.trashInfoList) {
                if (fileInfo.packageName.equals(appInfo.packageName)) {
                    parentInfo = appInfo;
                    break;
                }
            }

            if (parentInfo != null) {
                ArrayList<TrashInfo> subList = parentInfo.bundle.getParcelableArrayList(TrashClearEnv.subList);
                // 1,没有子节点情况,创建一个父节点
                if (subList == null) {
                    TrashInfo tempInfo;
                    try {
                        tempInfo = parentInfo.clone();
                    } catch (CloneNotSupportedException e) {
                        if (DEBUG) {
                            e.printStackTrace();
                        }
                        continue;
                    }
                    // 创建子节点
                    subList = new ArrayList<TrashInfo>(3);
                    parentInfo.bundle.putParcelableArrayList(TrashClearEnv.subList, subList);
                    // 旧根路径描述统一为数据
                    if (tempInfo.desc != null) {
                        tempInfo.desc = TrashClearUtils.getResStringById(mContext, "sysclear_trash_cache", "数据");
                    }
                    // 将本身添加进父节点
                    subList.add(tempInfo);
                }

                subList.add(fileInfo);
            } else {
                // 没找到,就自己创建父节点，然后添加进去
                parentInfo = new TrashInfo();
                parentInfo.desc = TrashClearUtils.getAppName(fileInfo.packageName, mContext.getPackageManager());// 显示软件名称
                if (TextUtils.isEmpty(parentInfo.desc)) {
                    parentInfo.desc = fileInfo.desc;
                }
                parentInfo.packageName = fileInfo.packageName;
                parentInfo.type = TrashClearEnv.CATE_APP_SD_CACHE;
                Bundle bundle = new Bundle();
                ArrayList<TrashInfo> subList = new ArrayList<TrashInfo>(3);
                bundle.putParcelableArrayList(TrashClearEnv.subList, subList);
                parentInfo.bundle = bundle;
                mAppCacheItem.trashInfoList.add(parentInfo);
                subList.add(fileInfo);
            }
        }

        if (DEBUG) {
            Log.i(TAG, "fileCacheAddToAppCache,time:" + (System.currentTimeMillis() - startTime));
        }
    }

    // 移除微信，QQ nlg_add
    private void removeWixinQQCache(){
        if(mAppCacheItem.trashInfoList == null || mAppCacheItem.trashInfoList.size() == 0){
            return;
        }
        boolean flag = false;
        int count = mAppCacheItem.trashInfoList.size();
        for (int i=0; i<count; i++) {
            TrashInfo appInfo = mAppCacheItem.trashInfoList.get(i);

            if(appInfo.packageName != null){
                if ("com.tencent.mm".equals(appInfo.packageName)) {
                    WixinTrashInfo = appInfo;
                    mAppCacheItem.trashInfoList.remove(appInfo);
                    count--;
                    i--;
                    if(flag){
                        break;
                    }else{
                        flag = true;
                    }
                }
                else if ("com.tencent.mobileqq".equals(appInfo.packageName)) {
                    QQTrashInfo = appInfo;
                    mAppCacheItem.trashInfoList.remove(appInfo);
                    count--;
                    i--;
                    if(flag){
                        break;
                    }else{
                        flag = true;
                    }
                }
            }
        }
    }

    //nlg_add
    public void setWixinTrashInfo(TrashInfo info){
        WixinTrashInfo = info;
    }

    public TrashInfo getWixinTrashInfo(){
        return WixinTrashInfo;
    }

    public void setQQTrashInfo(TrashInfo info){
        QQTrashInfo = info;
    }

    public TrashInfo getQQTrashInfo(){
        return QQTrashInfo;
    }

    // 扫描结束时刷新数据
    private void dealResult() {
        if (DEBUG) {
            Log.d(TAG, "dealResult start");
        }
        if (mSystemItem != null) {
            //TrashClearUtils.refresh(mSystemItem);
            trashClearCategorySelectedAll(mSystemItem); //勾选整个分类
            TrashClearUtils.sort(mSystemItem.trashInfoList);
        }

        if (mAppCacheItem != null) {
            // 合并专项清理数据
            fileCacheAddToAppCache();
            TrashClearUtils.refresh(mAppCacheItem);// 先刷新一下大小，再排序才能正确
            TrashClearUtils.sort(mAppCacheItem.trashInfoList);
            removeWixinQQCache();

            // 缓存专项清理
            // if (mAppFileCacheItem != null && mAppFileCacheItem.trashInfoList
            // != null && mAppFileCacheItem.trashInfoList.size() > 0) {
            //
            // TrashClearUtils.refresh(mAppFileCacheItem);
            // TrashClearUtils.sort(mAppFileCacheItem.trashInfoList);
            //
            // TrashInfo trashInfo = new TrashInfo();
            // trashInfo.desc = "缓存专项清理";
            // trashInfo.type = TrashClearEnv.CATE_FILE_CACHE;
            // Bundle bundle = new Bundle();
            // bundle.putParcelableArrayList(TrashClearEnv.subList,
            // mAppFileCacheItem.trashInfoList);
            // trashInfo.bundle = bundle;
            // // 缓存专项清理需要添加到第二个位置,它前面是系统缓存
            // mAppCacheItem.trashInfoList.add(0, trashInfo);
            // }
            if (mAppSystemCacheItem != null && mAppSystemCacheItem.trashInfoList != null && mAppSystemCacheItem.trashInfoList.size() > 0) {
                // 检测系统缓存勾选状态
                AppCacheCheck.check(mAppSystemCacheItem.trashInfoList, mContext);

                TrashClearUtils.refresh(mAppSystemCacheItem);
                TrashClearUtils.sort(mAppSystemCacheItem.trashInfoList);

                TrashInfo trashInfo = new TrashInfo();
                trashInfo.desc = TrashClearUtils.getResStringById(mContext, "sysclear_trash_system_app_cache", "系统缓存");
                trashInfo.type = TrashClearEnv.CATE_APP_SYSTEM_CACHE;
                Bundle bundle = new Bundle();
                bundle.putParcelableArrayList(TrashClearEnv.subList, mAppSystemCacheItem.trashInfoList);
                trashInfo.bundle = bundle;
                // 系统缓存需要添加到第一个位置
                mAppCacheItem.trashInfoList.add(0, trashInfo);
            }
            TrashClearUtils.refresh(mAppCacheItem);
        }

        if (mAdpluginItem != null) {
            //TrashClearUtils.refresh(mAdpluginItem);
            trashClearCategorySelectedAll(mAdpluginItem); //勾选整个分类
            TrashClearUtils.sort(mAdpluginItem.trashInfoList);
        }

        if (mUninstalledItem != null) {
            //TrashClearUtils.refresh(mUninstalledItem);
            trashClearCategorySelectedAll(mUninstalledItem); //勾选整个分类
            TrashClearUtils.sort(mUninstalledItem.trashInfoList);
        }

        if (mApkItemAll != null) {
            mApkItemAll.trashInfoList = new ApkListSort().sort(mApkItemAll.trashInfoList);
            TrashClearUtils.refresh(mApkItemAll);
            divideApkItem();
            trashClearCategorySelectedAll(mApkItemInstalled);
            trashClearCategoryDeSelectedAll(mApkItemUnInstalled);
        }

        // 大文件特殊处理
        if (mBigfileItem != null) {
            BigFileSort.sort(mBigfileItem);
            TrashClearUtils.refresh(mBigfileItem);
        }

        if (DEBUG) {
            Log.d(TAG, "dealResult end");
        }
    }

    /**
     * 将安装包分为已安装和未安装的 nlg_add
     * */
    private void divideApkItem(){
        mApkItemInstalled.trashInfoList.clear();
        mApkItemUnInstalled.trashInfoList.clear();
        if(mApkItemAll!=null && mApkItemAll.trashInfoList.size()>0) {
            for (TrashInfo trashInfo : mApkItemAll.trashInfoList) {
                if (trashInfo.dataType == TrashClearEnv.APK_TYPE_INSTALLED) {
                    mApkItemInstalled.trashInfoList.add(trashInfo);
                }else {
                    mApkItemUnInstalled.trashInfoList.add(trashInfo);
                }
            }
        }

      


        // 安装包单独排序(已安装) nlg_add
        if (mApkItemInstalled != null) {
            TrashClearUtils.refresh(mApkItemInstalled);
        }

        // 安装包单独排序(未安装) nlg_add
        if (mApkItemUnInstalled != null) {
            TrashClearUtils.refresh(mApkItemUnInstalled);
        }
    }

    private ExpandCallback mExpandCallback;

    public void setExpandCallback(ExpandCallback expandCallback) {
        mExpandCallback = expandCallback;
    }

    // 如果产品需求与SDK的不一直时，可以通过重写这个方法，满足需求
    public static interface ExpandCallback {
        // 数据有增减时，重新拆分数据
        void reSpitData();

        // 刷新数据，勾选状态改变时重新刷新状态
        void refreshData();
    }
}