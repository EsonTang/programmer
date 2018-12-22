
package com.prize.qihoo.cleandroid.sdk;

import android.content.Context;
import android.util.Log;

import com.prize.qihoo.cleandroid.sdk.ProcessInfoUtils.ProcessUiItem;
import com.prize.qihoo.cleandroid.sdk.plugins.PtManagerImpl;
import com.qihoo.cleandroid.sdk.utils.ClearModuleUtils;
import com.qihoo360.mobilesafe.opti.env.clear.ProcessClearEnv;
import com.qihoo360.mobilesafe.opti.i.processclear.AppPackageInfo;
import com.qihoo360.mobilesafe.opti.i.processclear.ICallbackClear;
import com.qihoo360.mobilesafe.opti.i.processclear.ICallbackScan;
import com.qihoo360.mobilesafe.opti.i.processclear.IProcessCleaner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 手机加速 辅助类，执行扫描、清理操作，方便界面调用； <br>
 * 注：ProcessClearCaller 用完后，记得调用destory ，否则影响资源释放
 */
public class ProcessClearCaller extends BaseOptiTask {

    public static final boolean DEBUG = SDKEnv.DEBUG;

    private static final String TAG = DEBUG ? "ProcessClearCaller" : ProcessClearCaller.class.getSimpleName();

    // 内存清理SDK 接口
    private final IProcessCleaner mProcessCleaner;

    // 可以清理进程列表
    private List<AppPackageInfo> mAppPackageInfoList;

    public ProcessClearCaller(Context context) {
        super(context);
        mProcessCleaner = ClearModuleUtils.getProcessCleanerImpl(context);
        mProcessCleaner.init(mContext, false);
    }

    /**
     * 扫描可以清理的内存
     *
     */
    @Override
    public void scan() {
        mProcessCleaner.scan(ProcessClearEnv.PROCESS_CLEAR_TYPE_NORMAL, mScanCallbackSDK);
    }

    /**
     * 清理内存，手机加速
     */
    @Override
    public void clear() {
        if (mAppPackageInfoList != null) {

            ArrayList<AppPackageInfo> appList = new ArrayList<AppPackageInfo>(mAppPackageInfoList);
            ArrayList<AppPackageInfo> unClearList = new ArrayList<AppPackageInfo>();
            ArrayList<String> pidClearItems = new ArrayList<String>();
            ArrayList<String> packageClearItems = new ArrayList<String>();

            for (AppPackageInfo info : appList) {
                boolean selected = (info.bundle != null) ? info.bundle.getBoolean(ProcessInfoUtils.UI_FLAG_SELECT) : false;
                if (!selected) {
                    unClearList.add(info);
                    continue;
                }

                if (PtManagerImpl.isRootOk() && (info.clearablePids != null && info.clearablePids.length > 0)) {
                    // root功能启动时，可以根据根据内存状态，清理指定的进程
                    for (int i : info.clearablePids) {
                        pidClearItems.add(Integer.toString(i));
                    }
                } else {
                    // 以包名方式清理
                    packageClearItems.add(info.packageName);
                }
            }

            if (pidClearItems != null && pidClearItems.size() > 0) {
                if (DEBUG) {
                    Log.d(TAG, "clearByPid:" + pidClearItems);
                }
                mProcessCleaner.clearByPid(pidClearItems, ProcessClearEnv.PROCESS_CLEAR_TYPE_NORMAL, mClearCallbackSDK);
            }
            if (packageClearItems.size() > 0) {
                if (DEBUG) {
                    Log.d(TAG, "clearByPkg:" + packageClearItems);
                }
                mProcessCleaner.clearByPkg(packageClearItems, ProcessClearEnv.PROCESS_CLEAR_TYPE_NORMAL, mClearCallbackSDK);
            }

            mAppPackageInfoList = unClearList;
        }
    }

    /**
     * 释放资源;
     */
    @Override
    public void onDestroy() {
        mProcessCleaner.destroy();
    }

    /**
     * 获取手机加速列表
     */
    public List<AppPackageInfo> getList() {
        return mAppPackageInfoList;
    }

    /**
     * 获取扫描结果
     */
    public ResultSummaryInfo getResultSummaryInfo() {
        ResultSummaryInfo resultSummaryInfo = new ResultSummaryInfo();
        if (mAppPackageInfoList != null) {
            for (AppPackageInfo appPackageInfo : mAppPackageInfoList) {
                if (appPackageInfo.shouldSelect()) {
                    resultSummaryInfo.selectedCount++;
                    resultSummaryInfo.selectedSize += appPackageInfo.usedMemory;
                }
                resultSummaryInfo.count++;
                resultSummaryInfo.size += appPackageInfo.usedMemory;
            }
        }
        return resultSummaryInfo;
    }

    // 扫描回调接口
    private final ICallbackScan mScanCallbackSDK = new ICallbackScan() {

        private List<AppPackageInfo> selectedList = null;

        private List<AppPackageInfo> unSelectedList = null;

        @Override
        public void onStart() {
            if (DEBUG) {
                Log.i(TAG, "onStart");
            }
            // 扫描开始，可以做一些初始化的操作
            selectedList = new ArrayList<AppPackageInfo>();
            unSelectedList = new ArrayList<AppPackageInfo>();
            if (mScanCallback != null) {
                mScanCallback.onStart();
            }
        }

        @Override
        public void onProgress(int scanned, int total, String scanningItem) {
            // 这里可以得到扫描进度
            if (DEBUG) {
                Log.v(TAG, "onProgress " + scanned + " " + total + " " + scanningItem);
            }
            if (mScanCallback != null) {
                mScanCallback.onProgressUpdate(scanned, total);
            }
        }

        @Override
        public void onFoundItem(AppPackageInfo processInfo) {
            // 获得扫描到的数据，这些数据可以用于界面显示，以及清理进程时的应用。
            if (DEBUG) {
                Log.v(TAG, "onFoundItem " + processInfo.packageName + " " + processInfo.isDefaultChoosen + " usedMemory:" + processInfo.usedMemory);
            }
            ProcessUiItem item = ProcessInfoUtils.getUiFlagsFromData(processInfo);
            // 建议隐藏的项先不加到列表里
            if (item.mDefaultHiden) {
                if (item.mHasClearProc) {
                    AppPackageInfo procInfo = ProcessInfoUtils.cloneAppPackageInfo(processInfo);
                    //更新可清理部份的大小
                    procInfo.usedMemory = processInfo.clearMemory;
                    if (DEBUG) {
                        Log.i(TAG, "add item hiden has clear proc name=" + processInfo.packageName);
                    }
                    if (procInfo != null) {
                        add(procInfo, true, true);
                    }
                }
            } else {
                if (item.mHasClearProc) {
                    if (DEBUG) {
                        Log.i(TAG, "add item normal proc name=" + processInfo.packageName);
                    }
                    AppPackageInfo procInfo = ProcessInfoUtils.cloneAppPackageInfo(processInfo);
                    //更新可清理部份的大小
                    procInfo.usedMemory = processInfo.clearMemory;
                    processInfo.usedMemory = processInfo.usedMemory - procInfo.usedMemory;
                    add(procInfo, true, true);
                }
                if (DEBUG) {
                    Log.i(TAG, "add item normal package name=" + processInfo.packageName);
                }
                add(processInfo, item.mDeaultChoosen, false);
            }
        }

        private void add(AppPackageInfo processInfo, boolean select, boolean procclear) {
            processInfo.usedMemory = processInfo.usedMemory * 1024;
            processInfo.clearMemory = processInfo.clearMemory * 1024;
            // 界面 扩展数据都放到Bundle里
            if (processInfo.bundle != null) {
                processInfo.bundle.putBoolean(ProcessInfoUtils.UI_FLAG_SELECT, select);
            }
            if(procclear){
                if (processInfo.bundle != null) {
                    processInfo.bundle.putBoolean(ProcessInfoUtils.UI_FLAG_HAS_CLEAR_PROCESS, procclear);
                }
            }
            if (select) {
                selectedList.add(processInfo);
            } else {
                unSelectedList.add(processInfo);
            }
        }

        @Override
        public void onFinished(int resultCode) {
            // 表示扫描完成
            if (DEBUG) {
                Log.i(TAG, "onFinished resultCode: " + resultCode);
            }

            sortList(selectedList);
            sortList(unSelectedList);

            ArrayList<AppPackageInfo> list = new ArrayList<AppPackageInfo>(selectedList.size() + unSelectedList.size());
            list.addAll(selectedList);
            list.addAll(unSelectedList);
            mAppPackageInfoList = list;

            selectedList.clear();
            selectedList = null;
            unSelectedList.clear();
            unSelectedList = null;

            if (mScanCallback != null) {
                mScanCallback.onFinish(false);
            }
        }
    };

    // 按内存大小排序
    private void sortList(List<AppPackageInfo> list) {
        Collections.sort(list, new Comparator<AppPackageInfo>() {

            @Override
            public int compare(AppPackageInfo object1, AppPackageInfo object2) {
                if (object1.usedMemory > object2.usedMemory) {
                    return -1;
                } else if (object1.usedMemory < object2.usedMemory) {
                    return 1;
                }
                return 0;
            }
        });
    }

    // 清理回调接口
    private final ICallbackClear mClearCallbackSDK = new ICallbackClear() {

        @Override
        public void onStart() {
            if (DEBUG) {
                Log.i(TAG, "Clear onStart");
            }
            if (mClearCallback != null) {
                mClearCallback.onStart();
            }
        }

        @Override
        public void onProgress(int cleared, int total, String clearingItem, int itemMemory) {
            if (DEBUG) {
                Log.v(TAG, "Clear onProgress " + cleared + " " + total + " " + clearingItem + " " + itemMemory);
            }
            if (mClearCallback != null) {
                mClearCallback.onProgressUpdate(cleared, total);
            }
        }

        @Override
        public void onFinished(int result) {
            if (DEBUG) {
                Log.i(TAG, "Clear onFinished " + result);
            }
            if (mClearCallback != null) {
                mClearCallback.onFinish(false);
            }
        }
    };
}
