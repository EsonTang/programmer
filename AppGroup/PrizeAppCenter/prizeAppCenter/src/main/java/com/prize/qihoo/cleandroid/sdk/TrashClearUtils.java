
package com.prize.qihoo.cleandroid.sdk;

import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.qihoo360.mobilesafe.opti.env.clear.TrashClearEnv;
import com.qihoo360.mobilesafe.opti.env.clear.WhiteListEnv;
import com.qihoo360.mobilesafe.opti.i.trashclear.TrashClearCategory;
import com.qihoo360.mobilesafe.opti.i.trashclear.TrashInfo;
import com.qihoo360.mobilesafe.opti.i.whitelist.UserBWRecord;

import java.io.File;
import java.lang.reflect.Method;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class TrashClearUtils {
    private static final boolean DEBUG = SDKEnv.DEBUG;

    private static final String TAG = DEBUG ? "TrashClearUtils" : TrashClearUtils.class.getSimpleName();

    private static NumberFormat mSizeFormat;

    static {
        mSizeFormat = NumberFormat.getInstance();
        mSizeFormat.setMaximumFractionDigits(2);
    }

    /**
     * return a string in human readable format
     *
     * @param bytes 单位 B
     */
    public static String getHumanReadableSizeMore(long bytes) {
        if (bytes == 0) {
            return "0M";
        } else if (bytes < 1024) {
            return bytes + "B";
        } else if (bytes < 1048576) {
            return mSizeFormat.format(bytes / 1024f) + "K";
        } else if (bytes < 1048576 * 1024) {
            return mSizeFormat.format(bytes / 1024f / 1024f) + "M";
        } else {
            return mSizeFormat.format(bytes / 1024f / 1024f / 1024f) + "G";
        }
    }

    /**
     * return a string in human readable format
     *
     * @param bytes 单位 B
     */
    public static String getHumanReadableSize(long bytes) {
        if (bytes < 1024) {
            return bytes + "";
        } else if (bytes < 1048576) {
            return mSizeFormat.format(bytes / 1024f);
        } else if (bytes < 1048576 * 1024) {
            return mSizeFormat.format(bytes / 1024f / 1024f);
        } else {
            return mSizeFormat.format(bytes / 1024f / 1024f / 1024f);
        }
    }

//    /**
//     * 返回MB为单位的大小
//     *
//     * @param bytes 单位 B
//     */
//    public static String getMBSize(long bytes) {
//        return mSizeFormat.format(bytes / 1024f / 1024f) + "M";
//    }

    /**
     * return a string in human readable size unit
     *
     * @param bytes 单位 B
     */
    public static String getHumanReadableSizeUnit(long bytes) {
        if (bytes < 1024) {
            return "B";
        } else if (bytes < 1048576) {
            return "K";
        } else if (bytes < 1048576 * 1024) {
            return "M";
        } else {
            return "G";
        }
    }

    private static Comparator<TrashInfo> mComparator = new Comparator<TrashInfo>() {
        @Override
        public int compare(TrashInfo object1, TrashInfo object2) {
            int result = 0;
            if (object1 == null || object2 == null) {
                return result;
            }
            if (object1.size > object2.size) {
                result = -1;
            } else if (object1.size < object2.size) {
                result = 1;
            } else {
                result = 0;
            }
            return result;
        }
    };

    /**
     * 绑定指定的服务
     * 由于android的bug(http://code.google.com/p/android/issues/detail?id=2483
     * )，在Tab页中绑定需要使用ApplicationContext，
     * 所以在该函数统一调用ApplicationContext，防止使用错误的Context
     */
    public static void bindService(Context context, Class<?> serviceClazz, String action, ServiceConnection conn, int flags) {
        try {
            Intent serviceIntent = new Intent(context, serviceClazz).setAction(action);
            context.getApplicationContext().bindService(serviceIntent, conn, flags);
        } catch (Exception e) {
            if (DEBUG) {
                Log.e("TrashClearUtils", "bindService", e);
            }
        }
    }

    public static void unbindService(String tag, Context context, ServiceConnection conn) {
        try {
            context.getApplicationContext().unbindService(conn);
        } catch (Exception e) {
            if (DEBUG) {
                Log.e(tag, "unbindService", e);
            }
        }
    }

    static void sort(List<TrashInfo> list) {
        if (list == null) {
            return;
        }
        for (TrashInfo trashInfo : list) {
            sort(trashInfo);
        }

        Collections.sort(list, mComparator);
    }

    static void sort(TrashInfo trashInfo) {
        ArrayList<TrashInfo> subList = trashInfo.bundle.getParcelableArrayList(TrashClearEnv.subList);
        if (subList != null) {
            sort(subList);
        }
    }


    static void setTrashInfoSelected(TrashInfo trashInfo) {
        trashInfo.isSelected = (trashInfo.clearType == TrashClearEnv.CLEAR_TYPE_ONEKEY && !trashInfo.isInWhiteList);
    }
//    /**
//     * 将垃圾数据类型转成成白名单数据类型
//     */
//    public static List<UserBWRecord> tranUserBWRecord(List<TrashInfo> tranList) {
//        if (tranList == null) {
//            return null;
//        }
//        ArrayList<UserBWRecord> list = new ArrayList<UserBWRecord>();
//        for (TrashInfo TrashInfo : tranList) {
//            list.add(tranUserBWRecord(TrashInfo));
//        }
//        return list;
//    }

    /**
     * 将垃圾数据类型转成成白名单数据类型
     */
    static UserBWRecord tranUserBWRecord(TrashInfo tranInfo) {
        UserBWRecord userBWRecord = new UserBWRecord();

        if (TrashClearEnv.CATE_APK == tranInfo.type) {
            // 安装包保存时相对路径
            userBWRecord.value = tranInfo.bundle.getString(TrashClearEnv.dirPath);
        } else {
            userBWRecord.value = tranInfo.path;
        }
        userBWRecord.type = tranInfo.type;
        userBWRecord.flag = tranInfo.isInWhiteList ? WhiteListEnv.USER_SELECTION_NOT_KILL : WhiteListEnv.USER_SELECTION_NONE;
        userBWRecord.desc = tranInfo.desc;
        userBWRecord.packageName = tranInfo.packageName;
        if (TextUtils.isEmpty(userBWRecord.packageName)) {
            userBWRecord.packageName = "";
        }
        ArrayList<String> pkgList = tranInfo.bundle.getStringArrayList(TrashClearEnv.pkgList);
        if (pkgList != null) {
            Bundle bundle = new Bundle();
            bundle.putStringArrayList(WhiteListEnv.pkgList, pkgList);
            userBWRecord.bundle = bundle;
        }
        boolean isOther = tranInfo.bundle.getBoolean(TrashClearEnv.isUninstalledOtherItem, false);
        if (isOther) {
            if (userBWRecord.bundle == null) {
                userBWRecord.bundle = new Bundle();
            }
            userBWRecord.bundle.putBoolean(WhiteListEnv.isOther, isOther);
        }
        String uninstalledAppDesc = tranInfo.bundle.getString(TrashClearEnv.uninstalledAppDesc);
        if (!TextUtils.isEmpty(uninstalledAppDesc)) {
            if (userBWRecord.bundle == null) {
                userBWRecord.bundle = new Bundle();
            }
            userBWRecord.bundle.putString(WhiteListEnv.uninstalledAppDesc, uninstalledAppDesc);
        }
        return userBWRecord;
    }

    /**
     * 刷新数据，计算子项大小
     */
    public static void refresh(TrashClearCategory trashClearCategory) {
        if (trashClearCategory == null || trashClearCategory.trashInfoList == null) {
            return;
        }

        long totalNum = 0;
        long totalLength = 0;
        // 不包含白名单选中的项目
        long totalCheckedCountUnContainWhiteList = 0;
        long totalCheckedSizeUnContainWhiteList = 0;
        // 包含白名单选中的项目,用来计算是否全选
        long totalCheckedCountContainWhiteList = 0;

        ArrayList<TrashInfo> trashInfoList = new ArrayList<TrashInfo>(trashClearCategory.trashInfoList);
        for (TrashInfo trashInfo : trashInfoList) {
            if (trashInfo == null) {
                continue;
            }

            ArrayList<TrashInfo> subList = trashInfo.bundle.getParcelableArrayList(TrashClearEnv.subList);
            if (subList != null && subList.size() > 0) {
                // 计算子项大小
                ResultSummaryInfo fileInfo = calSubList(trashInfo);
                totalCheckedCountUnContainWhiteList += fileInfo.selectedCount;
                totalCheckedCountContainWhiteList += fileInfo.argLong1;
                totalCheckedSizeUnContainWhiteList += fileInfo.selectedSize;

                trashInfo.count = fileInfo.count;
                trashInfo.size = fileInfo.size;

                if (TrashClearEnv.CATE_APP_SD_CACHE == trashInfo.type ||
                        TrashClearEnv.CATE_APP_SYSTEM_CACHE == trashInfo.type || TrashClearEnv.CATE_FILE_CACHE == trashInfo.type
                        || TrashClearEnv.CATE_UNINSTALLED == trashInfo.type) {
                    Bundle bundle = trashInfo.bundle;
                    bundle.putInt(TrashClearEnv.cautiousClearCount, fileInfo.cautiousClearCount);
                }
                // 处理全选
                if (trashInfo.count > 0 && trashInfo.count == fileInfo.argLong1) {
                    trashInfo.isSelected = true;
                } else {
                    trashInfo.isSelected = false;
                }

                totalNum += subList.size();
            } else {

                ResultSummaryInfo fileInfo = calTrashInfo(trashInfo);
                totalCheckedCountUnContainWhiteList += fileInfo.selectedCount;
                totalCheckedCountContainWhiteList += fileInfo.argLong1;
                totalCheckedSizeUnContainWhiteList += fileInfo.selectedSize;

                totalNum++;
            }

            totalLength += trashInfo.size;
        }

        trashClearCategory.count = totalNum;
        trashClearCategory.size = totalLength;
        trashClearCategory.selectedCount = totalCheckedCountUnContainWhiteList;
        trashClearCategory.selectedSize = totalCheckedSizeUnContainWhiteList;

        if (totalNum > 0 && (totalCheckedCountContainWhiteList == totalNum)) {
            trashClearCategory.isSelectedAll = true;
        } else {
            trashClearCategory.isSelectedAll = false;
        }
    }

    private static ResultSummaryInfo calSubList(TrashInfo rootInfo) {
        ResultSummaryInfo totalInfo = new ResultSummaryInfo();
        ArrayList<TrashInfo> subList = rootInfo.bundle.getParcelableArrayList(TrashClearEnv.subList);
        if (subList == null) {
            return null;
        }
        for (TrashInfo trashInfo : subList) {
            if (trashInfo == null) {
                continue;
            }

            ResultSummaryInfo info = calTrashInfo(trashInfo);

            // 带子缓存的需要计算里面包含的敏感项
            info.argInt1++;
            if (TrashClearEnv.CLEAR_TYPE_KEEP == trashInfo.clearType) {
                info.cautiousClearCount++;
            }

            totalInfo.count += info.count;
            totalInfo.size += info.size;
            totalInfo.selectedCount += info.selectedCount;
            totalInfo.selectedSize += info.selectedSize;
            totalInfo.argInt1 += info.argInt1;
            totalInfo.cautiousClearCount += info.cautiousClearCount;
            totalInfo.argLong1 += info.argLong1;
        }
        return totalInfo;
    }

    private static  ResultSummaryInfo calTrashInfo(TrashInfo trashInfo) {
        ResultSummaryInfo fileInfo = new ResultSummaryInfo();
        if (trashInfo.isSelected) {
            fileInfo.argLong1++;
            if (TrashClearEnv.CATE_PROCESS == trashInfo.type) {
                fileInfo.selectedCount++;
                fileInfo.selectedSize += trashInfo.size;
            } else {
                if (!trashInfo.isInWhiteList) {
                    fileInfo.selectedCount++;
                    fileInfo.selectedSize += trashInfo.size;
                }
            }
        }
        fileInfo.count++;
        fileInfo.size += trashInfo.size;

        return fileInfo;
    }

    /**
     * TrashInfo 选中状态改变 刷新数据
     *
     * @param trashInfo  TrashInfo
     * @param isSelected  是否被选中
     */
    public static void onTrashInfoSelectedChanged(TrashInfo trashInfo, boolean isSelected) {
        trashInfo.isSelected = isSelected;

        List<TrashInfo> trashInfoList = trashInfo.bundle.getParcelableArrayList(TrashClearEnv.subList);
        if (trashInfoList == null) {
            return;
        }

        for (TrashInfo subInfo : trashInfoList) {
            onTrashInfoSelectedChanged(subInfo, isSelected);
        }
    }

    /**
     * TrashClearCategory 选中状态改变 刷新数据
     *
     * @param trashClearCategory  TrashClearCategory
     */
    public static void onTrashClearCategorySelectedChanged(TrashClearCategory trashClearCategory) {
        trashClearCategory.isSelectedAll = !trashClearCategory.isSelectedAll;
        if (trashClearCategory.trashInfoList == null) {
            return;
        }

        for (TrashInfo trashInfo : trashClearCategory.trashInfoList) {
            TrashClearUtils.onTrashInfoSelectedChanged(trashInfo, trashClearCategory.isSelectedAll);
        }
    }

    /**
     * TrashClearCategory 垃圾类别全选
     *
     * @param trashClearCategory  TrashClearCategory
     * @author nlg_add
     */
    public static void trashClearCategorySelectedAll(TrashClearCategory trashClearCategory) {
        trashClearCategory.isSelectedAll = true;
        if (trashClearCategory.trashInfoList == null) {
            return;
        }

        for (TrashInfo trashInfo : trashClearCategory.trashInfoList) {
            TrashClearUtils.onTrashInfoSelectedChanged(trashInfo, true);
        }
    }

    /**
     * TrashClearCategory 垃圾类别全不选
     *
     * @param trashClearCategory  TrashClearCategory
     * @author nlg_add
     */
    static void trashClearCategoryDeSelectedAll(TrashClearCategory trashClearCategory) {
        trashClearCategory.isSelectedAll = false;
        if (trashClearCategory.trashInfoList == null) {
            return;
        }

        for (TrashInfo trashInfo : trashClearCategory.trashInfoList) {
            TrashClearUtils.onTrashInfoSelectedChanged(trashInfo, false);
        }
    }

    // 获取清理加速扫描列表，由存储卡根路径和应用名称组成
    public static List<String> getScanList(Context context) {
        ArrayList<String> scanList = new ArrayList<String>();
        List<String> appList = getInstalledAppList(context);// getLauncherAppList(context);
        if (appList != null) {
            scanList.addAll(appList);
        }
        List<String> rootPathList = getRootPathList(context);
        if (rootPathList != null) {
            scanList.addAll(rootPathList);
        }
        return scanList;
    }

    // 获取默认存储卡的根路径中文件名列表
    private static List<String> getRootPathList(Context context) {
        File sdPath = Environment.getExternalStorageDirectory();
        if (!sdPath.exists()) {
            return null;
        }
        String[] files = sdPath.list();

        if (files == null) {
            return null;
        }

        ArrayList<String> rootFileList = new ArrayList<String>();
        for (String file : files) {
            rootFileList.add(file);
        }
        return rootFileList;
    }

    /**
     * 获取所有安装软件的包名
     */
    private static List<String> getInstalledAppList(Context context) {
        List<ApplicationInfo> installedApps = null;
        List<String> installedPkgList = null;
        try {
            installedApps = context.getPackageManager().getInstalledApplications(0);
        } catch (Exception e) {
            if (DEBUG) {
                Log.d(TAG, "getInstalledApplications", e);
            }
        }

        if (installedPkgList == null) {
            if (installedApps == null) {
                installedApps = new ArrayList<ApplicationInfo>(1);
            }
            int installedCount = installedApps.size();
            installedPkgList = new ArrayList<String>(installedCount);
            for (int i = 0; i < installedCount; i++) {
                installedPkgList.add(installedApps.get(i).packageName);
            }
        }
        return installedPkgList;
    }

    public static  String getAppName(String packageName, PackageManager packageManager) {
        String appName = packageName;
        ApplicationInfo info = null;
        try {
            info = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA);
            if (info != null) {
                appName = info.loadLabel(packageManager).toString();
            }
        } catch (Throwable e) {
            if (DEBUG) {
                Log.d(TAG, "getAppName", e);
            }
        }
        if (appName == null) {
            appName = "";
        }
        return appName;
    }

    public static Drawable getApplicationIcon(String packageName, PackageManager packageManager) {

        try {
            return packageManager.getApplicationIcon(packageName);
        } catch (NameNotFoundException e) {
            return packageManager.getDefaultActivityIcon();
        } catch (OutOfMemoryError e) {
            System.gc();
            return packageManager.getDefaultActivityIcon();
        } catch (Throwable e) {
            return packageManager.getDefaultActivityIcon();
        }
    }

    /**
     * 通过反射加载安装包Icon
     */
    public static Drawable loadApkIcon(Context context, int iconID, String apkFilePath) {
        if (0 == iconID) {
            return null;
        }

        Drawable icon = null;
        Resources res = getApkResByRefrect(context, apkFilePath);
        if (res != null) {
            try {
                icon = res.getDrawable(iconID);
            } catch (Throwable e) {
                if (DEBUG) {
                    Log.e(TAG, "getDrawable error", e);
                }
            }
        }
        return icon;
    }

    /**
     * 通过反射加载安装包Resources
     */
    private static Resources getApkResByRefrect(Context context, String apkFilePath) {
        Method addAssetPathMethod = null;
        Object instance = null;
        Class<?> clazz = null;
        Resources apkRes = null;
        try {
            clazz = Class.forName("android.content.res.AssetManager");
            instance = clazz.newInstance();
            addAssetPathMethod = clazz.getMethod("addAssetPath", String.class);
            addAssetPathMethod.invoke(instance, apkFilePath);
            Resources res = context.getResources();
            apkRes = new Resources((AssetManager) instance, res.getDisplayMetrics(), res.getConfiguration());
        } catch (Throwable e) {
            if (DEBUG) {
                Log.e(TAG, "Class.forName(\"android.content.res.AssetManager\") error", e);
            }
        }
        return apkRes;
    }

//    /**
//     * 正则表达式匹配路径
//     *
//     * @param rootPath    正则路径前缀 如：/sdcard/adb
//     * @param regularPath 正则路径后缀 如：[0-9]{4}/adb
//     * @return 返回匹配的路径列表
//     */
//    public static List<String> getRegularPathList(String rootPath, String regularPath) {
//        int scanDepth = regularPath.split(File.separator).length;
//        List<String> matchList = getRegularPathList(rootPath, rootPath + File.separator + regularPath, scanDepth, 0, new ArrayList<String>(3));
//        if (DEBUG) {
//            Log.v(TAG, "getRegularPathList regularPath:" + rootPath + File.separator + regularPath + " " + matchList);
//        }
//        return matchList;
//    }

//    /**
//     * 正则表达式匹配路径
//     *
//     * @param rootPath    正则路径的前缀 如：/sdcard/adb
//     * @param regularPath 带正则表达式的全路径 如：/sdcard/adb/[0-9]{4}
//     * @param maxDepth    匹配深度
//     * @param curDepth    当前深度
//     * @param list
//     * @return
//     */
//    public static List<String> getRegularPathList(String rootPath, String regularPath, int maxDepth, int curDepth, ArrayList<String> list) {
//
//        ArrayList<String> pathList = list;
//
//        if (DEBUG) {
//            Log.v(TAG, "getRegularPathList maxDepth:" + maxDepth + " curDepth:" + curDepth + " rootPath:" + rootPath + " " + regularPath + " " + pathList);
//        }
//
//        if (curDepth >= maxDepth) {
//            if (pathList == null) {
//                pathList = new ArrayList<String>(3);
//            }
//            Pattern pat = Pattern.compile(regularPath);
//            Matcher mat = pat.matcher(rootPath.toLowerCase(Locale.US));
//            if (mat.matches()) {
//                pathList.add(rootPath);
//            }
//            return pathList;
//        }
//
//        File file = new File(rootPath);
//        if (!file.isDirectory()) {
//            return pathList;
//        }
//        String[] fileNames = null;
//        try {
//            fileNames = file.list();
//        } catch (Throwable e) {
//            if (DEBUG) {
//                Log.e(TAG, "list", e);
//            }
//        }
//        if (fileNames == null) {
//            return pathList;
//        }
//
//        curDepth++;
//        for (String fileName : fileNames) {
//            File file2 = new File(rootPath + File.separator + fileName);
//            if (!file2.isDirectory()) {
//                continue;
//            }
//            getRegularPathList(file2.getAbsolutePath(), regularPath, maxDepth, curDepth, pathList);
//        }
//        return pathList;
//    }

//    // 获取在桌面显示的应用名称列表
//    public static List<String> getLauncherAppList(Context context) {
//
//        ArrayList<String> appNameList = new ArrayList<String>();
//        PackageManager pm = context.getPackageManager();
//        Intent intent = new Intent(Intent.ACTION_MAIN);
//        intent.addCategory(Intent.CATEGORY_LAUNCHER);
//        List<ResolveInfo> resolveInfoList = pm.queryIntentActivities(intent, 0);
//        if (resolveInfoList == null) {
//            return null;
//        }
//        for (ResolveInfo resolveInfo : resolveInfoList) {
//            if (resolveInfo == null) {
//                continue;
//            }
//            CharSequence label = resolveInfo.loadLabel(pm);
//            if (label == null) {
//                continue;
//            }
//            appNameList.add(label.toString());
//        }
//        return appNameList;
//    }
//
//    // 获取在桌面显示的应用包名列表
//    public static List<String> getLauncherAppPkgList(Context context) {
//
//        ArrayList<String> appPkgList = new ArrayList<String>();
//        PackageManager pm = context.getPackageManager();
//        Intent intent = new Intent(Intent.ACTION_MAIN);
//        intent.addCategory(Intent.CATEGORY_LAUNCHER);
//        List<ResolveInfo> resolveInfoList = null;
//        try {
//            resolveInfoList = pm.queryIntentActivities(intent, 0);
//        } catch (Exception e) {
//            if (DEBUG) {
//                Log.d(TAG, "getLauncherAppPkgList", e);
//            }
//        }
//        if (resolveInfoList == null) {
//            return null;
//        }
//
//        for (ResolveInfo resolveInfo : resolveInfoList) {
//            if (resolveInfo == null) {
//                continue;
//            }
//            ActivityInfo activityInfo = resolveInfo.activityInfo;
//            if (activityInfo == null) {
//                continue;
//            }
//            ApplicationInfo applicationInfo = activityInfo.applicationInfo;
//            if (applicationInfo == null) {
//                continue;
//            }
//            String packageName = applicationInfo.packageName;
//            if (packageName == null) {
//                continue;
//            }
//            if (!appPkgList.contains(packageName)) {
//                appPkgList.add(packageName);
//            }
//        }
//        return appPkgList;
//    }

    static String getResStringById(Context context, String strId, String defaultValue) {
        try {
            Resources res = context.getResources();
            return res.getString(res.getIdentifier(strId, "string", context.getPackageName()));
        } catch (Exception e) {
            if (DEBUG) {
                Log.w(TAG, "getResStringById" + e.getMessage());
            }
            return defaultValue;
        }
    }

//    public static final boolean isAppCache(TrashInfo trashInfo) {
//        return trashInfo.bundle.getInt(TrashClearEnv.suggestion) == TrashClearEnv.SUGGESTION_RELOAD;
//    }
}
