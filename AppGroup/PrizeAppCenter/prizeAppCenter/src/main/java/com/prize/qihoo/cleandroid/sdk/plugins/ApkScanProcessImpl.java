
package com.prize.qihoo.cleandroid.sdk.plugins;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.DeadObjectException;
import android.os.IBinder;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;

import com.prize.qihoo.cleandroid.sdk.ApkScanService;
import com.prize.qihoo.cleandroid.sdk.SDKEnv;
import com.prize.qihoo.cleandroid.sdk.SharedPrefUtils;
import com.prize.qihoo.cleandroid.sdk.TrashClearUtils;
import com.qihoo360.mobilesafe.opti.env.clear.TrashClearEnv;
import com.qihoo360.mobilesafe.opti.i.plugins.ApkInfo;
import com.qihoo360.mobilesafe.opti.i.plugins.IApkScanProcess;
import com.qihoo360.mobilesafe.opti.i.plugins.IApkScanService;

public class ApkScanProcessImpl implements IApkScanProcess {
    private static final boolean DEBUG = SDKEnv.DEBUG;

    private static final String TAG = DEBUG ? "ApkScanProcessImpl" : ApkScanProcessImpl.class.getSimpleName();

    private final Context mContext;

    private IApkScanService mClearService;

    private static final String CLEAR_DAMAGE_FILE = "o_c_s_h_df";

    private static final String CLEAR_APK_PARSED_FILE = "o_c_s_h_pf";// apk扫描缓存文件

    private final Map<String, CrashAPKWithDate> mCrashDamageMap = new HashMap<String, CrashAPKWithDate>();// 会崩溃的破损包

    private final Map<String, ApkInfo> mApkParsedMap = new HashMap<String, ApkInfo>();

    private long mTotalTime = 0;// 打日志时间

    private boolean mIsApkParseChanged = false;

    public ApkScanProcessImpl(Context context) {
        mContext = context;
        readDamageFile();
        readApkParsedFile();
    }

    /**
     * 读取记录破损包文件
     *
     * @see 首次扫描记录，下次扫描跳过此安装包
     */
    private void readDamageFile() {
        String path = mContext.getFilesDir().getAbsolutePath() + File.separator + CLEAR_DAMAGE_FILE;
        parseFile(path);

        // 识别记录的上次破损包
        String apkFilter = SharedPrefUtils.getString(mContext, SharedPrefUtils.KEY_CLEAR_APKPATH_FILTER, "null");
        if (!apkFilter.equals("null")) {
            File file = new File(apkFilter);
            if (file.exists()) {
                CrashAPKWithDate crashAPKWithDate = mCrashDamageMap.get(apkFilter);
                if (crashAPKWithDate != null) {
                    CrashAPKWithDate temp = new CrashAPKWithDate();
                    temp.mPath = apkFilter;
                    temp.mDate = file.lastModified();
                    mCrashDamageMap.put(apkFilter, temp);
                    writeFile(path);
                }
            }
            // 扫描完成，去除记录
            SharedPrefUtils.setString(mContext, SharedPrefUtils.KEY_CLEAR_APKPATH_FILTER, "null");
        }
    }

    /**
     * 添加到apk解析文件
     *
     * @param info
     */
    private void addToApkParseFile(ApkInfo info) {
        if (!TextUtils.isEmpty(info.path)) {// 路径不为空
            ApkInfo temp = new ApkInfo();
            temp.path = info.path;
            temp.desc = info.desc;
            temp.size = info.size;
            temp.dataType = info.dataType;
            temp.apkVersionName = info.apkVersionName;
            temp.apkVersionCode = info.apkVersionCode;
            temp.apkIconID = info.apkIconID;
            temp.packageName = info.packageName;
            temp.modifyTime = info.modifyTime;

            mApkParsedMap.put(info.path, temp);
            mIsApkParseChanged = true;
        }
    }

    /**
     * 读取Apk解析缓存文件
     *
     */
    private void readApkParsedFile() {
        String path = mContext.getFilesDir().getAbsolutePath() + File.separator + CLEAR_APK_PARSED_FILE;
        File file = new File(path);
        if (!file.exists()) {
            return;
        }

        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(file), 1024);
            String line;
            while (!TextUtils.isEmpty(line = br.readLine())) {
                if (DEBUG) {
                    Log.w(TAG, "readApkCacheFile,line:" + line);
                }
                String[] data = line.split("\\|");
                if (data.length == 10 && !TextUtils.isEmpty(data[0])) {// 路径不为空
                    ApkInfo temp = new ApkInfo();
                    if (!TextUtils.isEmpty(data[0])) {
                        temp.path = data[0];
                    } else {
                        continue;
                    }
                    if (!TextUtils.isEmpty(data[1])) {
                        temp.desc = data[1];
                    }
                    temp.size = Long.parseLong(data[2]);
                    temp.dataType = Integer.parseInt(data[3]);
                    if (!TextUtils.isEmpty(data[4])) {
                        temp.apkVersionName = data[4];
                    }
                    temp.apkVersionCode = Integer.parseInt(data[5]);
                    temp.apkIconID = Integer.parseInt(data[6]);
                    if (!TextUtils.isEmpty(data[7])) {
                        temp.packageName = data[7];
                    }
                    temp.modifyTime = Long.parseLong(data[8]);
                    File f = new File(data[0]);
                    if (f.exists()) {
                        mApkParsedMap.put(data[0], temp);
                    }
                }
            }
        } catch (Exception e) {
            if (DEBUG) {
                Log.e(TAG, "readApkCacheFile failed", e);
            }
        } finally {
            try {
                br.close();
            } catch (Exception e) {
            }
        }

        mIsApkParseChanged = false;
    }

    /**
     * 写Apk缓存文件
     *
     * @param
     */
    public void writeApkParsedFile() {
        if (!mIsApkParseChanged || mApkParsedMap == null || mApkParsedMap.size() == 0) {
            return;
        }

        String path = mContext.getFilesDir().getAbsolutePath() + File.separator + CLEAR_APK_PARSED_FILE;

        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(new File(path), false));// false表示非append方式

            Set<Entry<String, ApkInfo>> set = mApkParsedMap.entrySet();
            for (Entry<String, ApkInfo> entry : set) {
                ApkInfo temp = entry.getValue();
                StringBuffer line = new StringBuffer();
                if (!TextUtils.isEmpty(temp.path)) {
                    line.append(temp.path);
                }
                line.append('|');

                if (!TextUtils.isEmpty(temp.desc)) {
                    line.append(temp.desc);
                }
                line.append('|');

                line.append(temp.size);
                line.append('|');

                line.append(temp.dataType);
                line.append('|');

                if (!TextUtils.isEmpty(temp.apkVersionName)) {
                    line.append(temp.apkVersionName);
                }
                line.append('|');

                line.append(temp.apkVersionCode);
                line.append('|');

                line.append(temp.apkIconID);
                line.append('|');

                if (!TextUtils.isEmpty(temp.packageName)) {
                    line.append(temp.packageName);
                }
                line.append('|');

                line.append(temp.modifyTime);
                line.append('|');

                line.append("0").append('\n');// 以0结尾，否则split时，会跳位
                bw.write(line.toString());
                if (DEBUG) {
                    Log.d(TAG, "writeApkParsedFile, line : " + line);
                }
            }
            bw.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (bw != null) {
                try {
                    bw.close();
                } catch (Exception e) {
                }
            }
        }
        mIsApkParseChanged = false;
    }

    @Override
    public int create() {
        if (DEBUG) {
            Log.d(TAG, "create");
        }
        if (mClearService == null) {
            bindService();
        } else {
            try {
                mClearService.create();
            } catch (Exception e) {
                if (DEBUG) {
                    Log.e(TAG, "create", e);
                }
            }
        }
        return 1;
    }

    private void bindService() {
        TrashClearUtils.bindService(mContext, ApkScanService.class, ApkScanService.ACTION_CLEAR_SERVICE, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public int destroy() {
        if (DEBUG) {
            Log.d(TAG, "destroy");
        }
        TrashClearUtils.unbindService(TAG, mContext, mServiceConnection);
        // 有些手机断开时，没有调用onServiceDisconnected,直接赋值为空。
        mClearService = null;

        // 保存apk解析文件
        writeApkParsedFile();

        if (DEBUG) {
            Log.d(TAG, "destroy,mTotalTime:" + mTotalTime);
        }
        return 1;
    }

    /**
     * 判断是否为崩溃包
     *
     * @param path
     * @return
     */
    private boolean isValidAPk(String path) {
        CrashAPKWithDate crashAPKWithDate = mCrashDamageMap.get(path);
        if (crashAPKWithDate != null) {
            File file = new File(path);
            // 最后修改时间相同，才能判断是同一个崩溃包
            if (file.exists() && crashAPKWithDate.mDate == file.lastModified()) {
                return false;
            }
        }
        return true;
    }

    /**
     * 判断此apk是否已解析过
     *
     * @return
     */
    public ApkInfo isApkParsed(String apkPath) {
        ApkInfo info = mApkParsedMap.get(apkPath);
        if (info != null) {
            File file = new File(apkPath);
            if (file.lastModified() == info.modifyTime) {
                return info;
            }
        }

        return null;
    }

    @Override
    public synchronized ApkInfo scanApk(String apkPath) {
        if (DEBUG) {
            Log.d(TAG, "scanApk,apkPath:" + apkPath);
        }
        ApkInfo entry = null;
        if (mClearService == null) {
            checkClearService();
        }

        // 如果存在于缓存文件中，就返回上次解析
        ApkInfo apkInfo = isApkParsed(apkPath);
        if (apkInfo != null) {
            return apkInfo;
        }
        long startTime = System.currentTimeMillis();

        // 扫描前先记录一下
        SharedPrefUtils.setString(mContext, SharedPrefUtils.KEY_CLEAR_APKPATH_FILTER, apkPath);

        if (mClearService != null) {
            // 判断是否为崩溃破损包
            if (isValidAPk(apkPath)) {
                try {
                    entry = mClearService.scanApk(apkPath);
                } catch (DeadObjectException e) {
                    SystemClock.sleep(1000);
                    if (DEBUG) {
                        Log.e(TAG, "scanApk DeadObjectException,apkPath:" + apkPath);
                    }
                    // 添加为崩溃破损包
                    saveCrashApk(apkPath);

                    checkClearService();
                    if (DEBUG) {
                        Log.e(TAG, "scanApk DeadObjectException", e);
                    }
                } catch (Exception e) {
                    if (DEBUG) {
                        Log.e(TAG, "scanApk", e);
                    }
                }
            }
            // todo,二次验证，有时会识别错误，
            // if (entry == null) {
            // if (unzip(apkPath, "AndroidManifest.xml")) {
            // entry = new TrashInfo();
            // entry.filePath = apkPath;
            // TrashClearUtils.loadApkInfo(mContext, entry);
            // // 去除崩溃破损包标识
            // removeCrashApk(apkPath);
            // }
            // }

            if (entry == null) {
                entry = new ApkInfo();
                entry.path = apkPath;
                File apkFile = new File(entry.path);
                entry.size = apkFile.length();
                entry.modifyTime = apkFile.lastModified();
                entry.desc = apkFile.getName();
                entry.dataType = TrashClearEnv.APK_TYPE_DAMAGED;
            }
        } else {
            if (DEBUG) {
                Log.i(TAG, "mClearService == null");
            }
            entry = ApkScanService.getApkInfo(mContext, apkPath);
        }

        if (DEBUG) {
            if (entry.dataType == TrashClearEnv.APK_TYPE_DAMAGED) {
                Log.i(TAG, "scanApk damaged apk :" + entry.path);
            }
        }

        // 扫描完成，去除记录
        SharedPrefUtils.setString(mContext, SharedPrefUtils.KEY_CLEAR_APKPATH_FILTER, "null");

        // 添加到apk解析文件
        addToApkParseFile(entry);

        if (DEBUG) {
            long cost = System.currentTimeMillis() - startTime;
            Log.d(TAG, "scanApk,cost:" + cost);
            mTotalTime += cost;
        }

        return entry;
    }

    private void checkClearService() {
        if (DEBUG) {
            Log.w(TAG, "reBindClearService start");
        }
        bindService();
        int waitTime = 0;
        while (mClearService == null) {
            SystemClock.sleep(100);
            waitTime += 100;
            // 10秒超时
            if (waitTime > 10000) {
                if (DEBUG) {
                    Log.w(TAG, "reBindClearService timeout");
                }
                break;
            }
        }
        if (DEBUG) {
            Log.w(TAG, "reBindClearService end");
        }
    }

    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mClearService = IApkScanService.Stub.asInterface(service);
            if (DEBUG) {
                Log.d(TAG, "onServiceConnected");
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            if (DEBUG) {
                Log.d(TAG, "onServiceDisconnected");
            }
            mClearService = null;
        }
    };

    // /**
    // * 去除破损包
    // *
    // * @param apkPath
    // */
    // private void removeCrashApk(String apkPath) {
    // if (mCrashDamageList.contains(apkPath)) {
    // mCrashDamageList.remove(apkPath);
    // // 保存文件
    // String path = mContext.getFilesDir().getAbsolutePath() + File.separator +
    // CLEAR_DAMAGE_FILE;
    // writeFile(mCrashDamageList, path);
    // }
    // }

    /**
     * 保存破损包
     *
     * @param apkPath
     */
    private void saveCrashApk(String apkPath) {
        File file = new File(apkPath);
        if (file.exists()) {
            CrashAPKWithDate temp = new CrashAPKWithDate();
            temp.mPath = apkPath;
            temp.mDate = file.lastModified();
            mCrashDamageMap.put(apkPath, temp);
            // 保存文件
            String path = mContext.getFilesDir().getAbsolutePath() + File.separator + CLEAR_DAMAGE_FILE;
            writeFile(path);
        }
    }

    /**
     * 写文件
     *
     * @param
     */
    public void writeFile(String path) {
        if (mCrashDamageMap == null || mCrashDamageMap.size() == 0) {
            return;
        }

        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(new File(path), true));// true表示append方式
            Set<Entry<String, CrashAPKWithDate>> set = mCrashDamageMap.entrySet();
            for (Entry<String, CrashAPKWithDate> entry : set) {
                CrashAPKWithDate temp = entry.getValue();
                String line = temp.mPath + "|" + temp.mDate + "\n";
                bw.write(line);
                if (DEBUG) {
                    Log.w(TAG, "printLog, line : " + line);
                }
            }
            bw.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (bw != null) {
                try {
                    bw.close();
                } catch (Exception e) {
                }
            }
        }
    }

    /**
     * 解析文件
     */
    public void parseFile(String path) {
        File file = new File(path);
        if (!file.exists()) {
            return;
        }

        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(file), 1024);
            String line;
            while (!TextUtils.isEmpty(line = br.readLine())) {
                if (DEBUG) {
                    Log.w(TAG, "ClearServiceHelper,parseFile, " + path + " : " + line);
                }
                String[] data = line.split("\\|");
                if (data.length == 2) {
                    CrashAPKWithDate crashAPKWithDate = mCrashDamageMap.get(data[0]);
                    if (crashAPKWithDate == null) {
                        CrashAPKWithDate temp = new CrashAPKWithDate();
                        temp.mPath = data[0];
                        temp.mDate = Long.parseLong(data[1]);
                        mCrashDamageMap.put(data[0], temp);
                    }
                }
            }
        } catch (Exception e) {
            if (DEBUG) {
                Log.e(TAG, "read config file failed", e);
            }
        } finally {
            try {
                br.close();
            } catch (Exception e) {
            }
        }

        // 超过限值，就清一次0,防止影响效率
        if (mCrashDamageMap.size() > 50) {
            mCrashDamageMap.clear();
        }
    }

    /**
     * 崩溃包信息数据结构
     *
     * @author wanglingjun
     */
    private class CrashAPKWithDate {
        public String mPath;// 路径

        public long mDate;// 文件日期
    }
}
