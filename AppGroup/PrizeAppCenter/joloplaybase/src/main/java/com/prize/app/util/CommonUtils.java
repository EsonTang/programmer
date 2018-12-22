package com.prize.app.util;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.KeyguardManager;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.provider.Settings;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.prize.app.BaseApplication;
import com.prize.app.beans.ClientInfo;
import com.prize.app.beans.DrawerData;
import com.prize.app.beans.HotKeyBean;
import com.prize.app.beans.Person;
import com.prize.app.beans.WelfareBean;
import com.prize.app.constants.Constants;
import com.prize.app.database.InstalledAppTable;
import com.prize.app.database.PrizeDatabaseHelper;
import com.prize.app.download.AppManagerCenter;
import com.prize.app.net.datasource.base.AppsItemBean;
import com.prize.app.net.datasource.base.PrizeAppsCardData;
import com.prize.app.net.datasource.home.CarParentBean;
import com.prize.statistics.model.ExposureBean;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

/**
 * @author longbaoxiu
 * @version V1.0
 */
public class CommonUtils {

    /**
     * 转换long类型为string类型，用于apk大小换算
     *
     * @param fileS   大小
     * @param formate 保留小数点位数："#.00"格式
     * @return String
     */
    public static String formatSize(long fileS, String formate) {
        DecimalFormat df = new DecimalFormat(formate);
        String fileSizeString = "";
        if (fileS < 1024) {
            fileSizeString = df.format((double) fileS) + "B";
        } else if (fileS < 1048576) {
            fileSizeString = df.format((double) fileS / 1024) + "KB";
        } else if (fileS < 1073741824) {
            fileSizeString = df.format((double) fileS / 1048576) + "MB";
        } else {
            fileSizeString = df.format((double) fileS / 1073741824) + "GB";
        }
        return fileSizeString;
    }

    /**
     * 转byte为M
     *
     * @param size long
     * @return String
     */
    public static String paresAppSize(long size) {
        return String.format("%1$.2f", size / (1024 * 1024f));
    }

    /**
     * 保留一位小数
     *
     * @param size double
     * @return String  百分比 eg：3.3%
     */
    public static String paresDownLoadPercent(double size) {
        java.text.DecimalFormat df = new java.text.DecimalFormat("#0.0");
        return df.format(size) + "%";
    }

//    /**
//     * 程序是否在前台运行
//     *
//     * @return  boolean
//     */
//    public static boolean isAppOnForeground(Context contex) {
//        ActivityManager activityManager = (ActivityManager) contex
//                .getApplicationContext().getSystemService(
//                        Context.ACTIVITY_SERVICE);
//        String packageName = contex.getApplicationContext().getPackageName();
//
//        List<RunningAppProcessInfo> appProcesses = activityManager
//                .getRunningAppProcesses();
//        if (appProcesses == null)
//            return false;
//
//        for (RunningAppProcessInfo appProcess : appProcesses) {
//            // The name of the process that this object is associated with.
//            if (appProcess.processName.equals(packageName)
//                    && appProcess.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
//                return true;
//            }
//        }
//
//        return false;
//    }

//    /**
//     * 方法描述：获得所有第三方应用的包名,以,号拼接的字符串
//     */
//    public static String getPackgeNames() {
//        // 获得系统所有应用的安装包信息
//        ArrayList<PackageInfo> appPackage = getPackageInfoList();
//        if (appPackage == null || appPackage.size() <= 0) {
//            return null;
//        }
//        StringBuilder stringBuilder = new StringBuilder();
//        for (PackageInfo packageInfo : appPackage) {
//            stringBuilder.append(packageInfo.packageName).append(",");
//        }
//        String packgeNames = stringBuilder.toString().trim();
//        if (packgeNames.length() >= 0 && packgeNames.endsWith(",")) {
//            packgeNames = packgeNames.substring(0, packgeNames.length() - 1);
//        }
//        return packgeNames;
//    }

    // /**
    // * 方法描述：获得所有第三方应用的包名versionCode,以,号拼接的字符串(eg:
    // * com.geili.koudai#1321,com.geili.koudai#132)
    // *
    // */
    // public static String getPackgeInfo(Context contex) {
    // String packgeNames = null;
    //
    // try {
    //
    // List<PackageInfo> appPackage = contex.getApplicationContext()
    // .getPackageManager()
    // .getInstalledPackages(PackageManager.GET_ACTIVITIES);
    // StringBuilder stringBuilder = new StringBuilder();
    // for (int i = 0; i < appPackage.size(); i++) {
    // PackageInfo packageInfo = appPackage.get(i);
    // //
    // //
    // //
    // 获取第三方应用packageInfo.applicationInfo.flags=0，系统应用packageInfo.applicationInfo.flags=1
    // // if ((packageInfo.applicationInfo.flags &
    // // android.content.pm.ApplicationInfo.FLAG_SYSTEM) == 0) {
    // stringBuilder.append(packageInfo.packageName).append("#")
    // .append(packageInfo.versionCode).append(",");
    // }
    // packgeNames = stringBuilder.toString().trim();
    // if (packgeNames.length() > 0 && packgeNames.endsWith(",")) {
    // packgeNames = packgeNames
    // .substring(0, packgeNames.length() - 1);
    // }
    // } catch (Exception e) {
    // return packgeNames;
    // }
    // return packgeNames;
    //
    // }

//    public static List<ApplicationInfo> getPackgeInfoByGetInstalledAp() {
//        List<ApplicationInfo> appPackage = null;
//        try {
//
//            appPackage = BaseApplication.curContext.getPackageManager()
//                    .getInstalledApplications(
//                            PackageManager.GET_UNINSTALLED_PACKAGES);
//            // StringBuilder stringBuilder = new StringBuilder();
//            // for (int i = 0; i < appPackage.size(); i++) {
//            // ApplicationInfo Info = appPackage.get(i);
//            // stringBuilder.append(Info.packageName).append("#")
//            // .append(Info.versionCode).append(",");
//            // }
//            // packgeNames = stringBuilder.toString().trim();
//            // if (packgeNames.length() > 0 && packgeNames.endsWith(",")) {
//            // packgeNames = packgeNames
//            // .substring(0, packgeNames.length() - 1);
//            // }
//        } catch (Exception e) {
//            return appPackage;
//        }
//        return appPackage;
//
//    }

    /**
     * 扫描手机应用，并且保存到数据库
     *
     * @param contex Context
     * @param sysApp （需要过滤的app）
     * @return boolean
     */
    public static boolean inert2DB(Context contex, String sysApp) {
        try {
            PackageManager pm = contex.getPackageManager();
            List<ApplicationInfo> appPackage = pm
                    .getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES);
            List<ContentValues> datas = new ArrayList<ContentValues>();
            int size = appPackage.size();
            for (int i = 0; i < size; i++) {
                ApplicationInfo Info = appPackage.get(i);
                if (sysApp.contains(Info.packageName + ","))
                    continue;
                ContentValues value = new ContentValues();
                value.put(InstalledAppTable.PKG_NAME, Info.packageName);
                try {
                    if (AppManagerCenter.isNewMethod()) {
                        PackageInfo packageInfo = pm.getPackageArchiveInfo(
                                Info.publicSourceDir, 0);
                        value.put(InstalledAppTable.VERSION_CODE,
                                packageInfo.versionCode);

                    } else {
                        if (BaseApplication.isThird) {
                            value.put(InstalledAppTable.VERSION_CODE,
                                    AppManagerCenter.getAppVersionCode(Info.packageName));
                        } else {

                            value.put(InstalledAppTable.VERSION_CODE,
                                    Info.versionCode);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                // }
                datas.add(value);
            }
            int result = PrizeDatabaseHelper.batchInsert(datas);
            return result == 1;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /***
     * @return app updated request string from DataBase
     *fanjunchen added
     */
    public static String getPackgeInfoStrFormDB() {
        String packgeNames = null;
        try {
            Cursor cur = PrizeDatabaseHelper.query(
                    InstalledAppTable.TABLE_NAME, new String[]{
                            InstalledAppTable.PKG_NAME,
                            InstalledAppTable.VERSION_CODE}, null, null, null,
                    null, null);

            if (null == cur)
                return null;

            int count = cur.getCount();
            if (count < 1) {
                cur.close();
                return null;
            }

            StringBuilder stringBuilder = new StringBuilder(2048);
            int i = 0;
            while (cur.moveToNext()) {
                i++;
                stringBuilder.append(cur.getString(0)).append("#")
                        .append(cur.getString(1));
                if (i != count) {
                    stringBuilder.append(",");
                }
            }
            cur.close();
            packgeNames = stringBuilder.toString();
            stringBuilder = null;
        } catch (Exception e) {
            return null;
        }
        return packgeNames;

    }

    /***
     * @return true haveData; false no data.
     * fanjunchen added
     */
    public static boolean isInitIntalledAppOk() {
        try {
            Cursor cur = PrizeDatabaseHelper.query(
                    InstalledAppTable.TABLE_NAME,
                    new String[]{InstalledAppTable.PKG_NAME}, null, null,
                    null, null, null);
            if (null == cur)
                return false;

            boolean rs = cur.getCount() > 0;
            cur.close();
            return rs;
        } catch (Exception e) {
            return false;
        }
    }

    // @prize }
    // /**
    // * 方法描述：获得所有第三方应用的包名versionCode,以,号拼接的字符串(eg:
    // * com.geili.koudai#1321,com.geili.koudai#132)
    // *
    // */
    // public static String getPackgeInfoByQueryIntent() {
    // String packgeNames = null;
    // List<ResolveInfo> lists = getResolveInfoList();
    // if (lists == null || lists.size() <= 0) {
    // return packgeNames;
    // }
    //
    // int size = lists.size();
    // PackageManager pm = BaseApplication.curContext.getPackageManager();
    // StringBuilder stringBuilder = new StringBuilder();
    // try {
    // ResolveInfo resolveInfo;
    // for (int i = 0; i < size; i++) {
    // resolveInfo = lists.get(i);
    // String pkgName = resolveInfo.activityInfo.packageName;
    // int versionCode = pm.getPackageInfo(pkgName, 0).versionCode;
    // String param = new StringBuilder(pkgName).append("#")
    // .append(versionCode).append(",").toString();
    // if (stringBuilder != null
    // && !stringBuilder.toString().contains(param)) {
    // stringBuilder.append(param);
    // // stringBuilder.append(pkgName).append("#")
    // // .append(versionCode).append(",");
    //
    // }
    // }
    // packgeNames = stringBuilder.toString().trim();
    // if (packgeNames.length() > 0 && packgeNames.endsWith(",")) {
    // packgeNames = packgeNames
    // .substring(0, packgeNames.length() - 1);
    // }
    //
    // return packgeNames;
    // } catch (NameNotFoundException e) {
    // return packgeNames;
    // }
    //
    // }

//    public static List<ResolveInfo> getResolveInfoList() {
//        List<ResolveInfo> lists = null;
//        try {
//            PackageManager pm = BaseApplication.curContext.getPackageManager();
//            Intent intent = new Intent(Intent.ACTION_MAIN, null);
//            // intent.addCategory(Intent.CATEGORY_DEFAULT);
//            lists = pm.queryIntentActivities(intent,
//                    PackageManager.GET_ACTIVITIES);
//        } catch (Exception e) {
//            e.printStackTrace();
//            return null;
//        }
//        if (lists == null) {
//            return null;
//        }
//        return lists;
//    }

//    public static ArrayList<PackageInfo> getPackageInfoList() {
//        ArrayList<PackageInfo> appPackage = null;
//        try {
//            appPackage = (ArrayList<PackageInfo>) BaseApplication.curContext
//                    .getPackageManager().getInstalledPackages(0);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        if (appPackage == null) {
//            return null;
//        }
//        ArrayList<PackageInfo> mPackages = new ArrayList<PackageInfo>();
//        for (int i = 0; i < appPackage.size(); i++) {
//            PackageInfo packageInfo = appPackage.get(i);
//            // 获取第三方应用packageInfo.applicationInfo.flags=0，系统应用packageInfo.applicationInfo.flags=1
//            // if ((packageInfo.applicationInfo.flags &
//            // android.content.pm.ApplicationInfo.FLAG_SYSTEM) == 0) {
//            mPackages.add(packageInfo);
//            // }
//        }
//        return mPackages;
//    }

    public static ArrayList<PackageInfo> getThirdPackageInfoList() {
        ArrayList<PackageInfo> appPackage = null;
        try {
            appPackage = (ArrayList<PackageInfo>) BaseApplication.curContext
                    .getPackageManager().getInstalledPackages(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (appPackage == null) {
            return null;
        }
        ArrayList<PackageInfo> mPackages = new ArrayList<PackageInfo>();
        for (int i = 0; i < appPackage.size(); i++) {
            PackageInfo packageInfo = appPackage.get(i);
            // 获取第三方应用packageInfo.applicationInfo.flags=0，系统应用packageInfo.applicationInfo.flags=1
            if ((packageInfo.applicationInfo.flags & android.content.pm.ApplicationInfo.FLAG_SYSTEM) == 0) {
                mPackages.add(packageInfo);
            }
        }
        return mPackages;
    }

//    /**
//     * @param activity  Activity
//     * @return > 0 success; <= 0 fail
//     */
//    public static int getStatusHeight(Activity activity) {
//        int statusHeight = 0;
//        Rect localRect = new Rect();
//        activity.getWindow().getDecorView()
//                .getWindowVisibleDisplayFrame(localRect);
//        statusHeight = localRect.top;
//        if (0 == statusHeight) {
//            Class<?> localClass;
//            try {
//                localClass = Class.forName("com.android.internal.R$dimen");
//                Object localObject = localClass.newInstance();
//                int i5 = Integer.parseInt(localClass
//                        .getField("status_bar_height").get(localObject)
//                        .toString());
//                statusHeight = activity.getResources()
//                        .getDimensionPixelSize(i5);
//            } catch (ClassNotFoundException e) {
//                e.printStackTrace();
//            } catch (IllegalAccessException e) {
//                e.printStackTrace();
//            } catch (InstantiationException e) {
//                e.printStackTrace();
//            } catch (NumberFormatException e) {
//                e.printStackTrace();
//            } catch (IllegalArgumentException e) {
//                e.printStackTrace();
//            } catch (SecurityException e) {
//                e.printStackTrace();
//            } catch (NoSuchFieldException e) {
//                e.printStackTrace();
//            }
//        }
//        return statusHeight;
//    }

    public static void copyText(TextView tv, Context context) {
        ClipboardManager cmb = (ClipboardManager) context
                .getSystemService(Context.CLIPBOARD_SERVICE);
        cmb.setText(tv.getText());

    }

    /**
     * 方法描述：查询是否登录云账号 返回userId
     *
     * @return void 返回userId 或者unkouwn
     */
    public static String queryUserId() {
        ContentResolver resolver = BaseApplication.curContext
                .getContentResolver();
        Uri uri = Uri.parse("content://com.prize.appcenter.provider.appstore/table_person");
        String userId = null;
        try {
            Cursor cs = resolver.query(uri, null, null, null, null);
            if (cs != null && cs.moveToFirst()) {
                userId = cs.getString(cs.getColumnIndex("userId"));
            }
            if (cs != null) {
                cs.close();
            }
            if (TextUtils.isEmpty(userId))
                return null;
        } catch (Exception e) {
            return null;
        }
        return userId;
    }

    /**
     * 查询云账号信息
     *
     * @param context Context
     * @return Person  Person
     */
    public static Person queryUserPerson(Context context) {
        if (context == null) {
            return null;
        }
        ContentResolver resolver = context.getContentResolver();
        Uri uri = Uri.parse("content://com.prize.appcenter.provider.appstore/table_person");
        Person person = new Person();
        String userId = null;
        String realName = null;
        String imgPath = null;
        String phone = null;
        try {
            Cursor cs = resolver.query(uri, null, null, null, null);
            if (cs != null && cs.moveToFirst()) {
                userId = cs.getString(cs.getColumnIndex("userId"));
                realName = cs.getString(cs.getColumnIndex("realName"));
                imgPath = cs.getString(cs.getColumnIndex("avatar"));
                phone = cs.getString(cs.getColumnIndex("phone"));

            }
            if (cs != null) {
                cs.close();
            }
            if (TextUtils.isEmpty(userId)) {
                return null;
            } else {
                if (!TextUtils.isEmpty(phone)) {
                    person.setPhone(phone);
                } else {
                    person.setPhone("");
                }
                if (!TextUtils.isEmpty(imgPath)) {
                    person.setAvatar(imgPath);
                } else {
                    person.setAvatar("");
                }
                if (!TextUtils.isEmpty(realName)) {
                    person.setRealName(realName);
                } else {
                    person.setRealName("");
                }
                person.setUserId(userId);
                return person;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static long lastClickTime;

    /**
     * @return boolean   [如果是连续点击则返回true
     */
    public static boolean isFastDoubleClick() {
        long time = System.currentTimeMillis();
        long timeD = time - lastClickTime;
        if (0 < timeD && timeD < 800) {
            return true;
        }
        lastClickTime = time;
        return false;
    }


    /**
     * 过滤已安装的应用（第一页如若返回的个数少于5个 则拼凑为5个）
     *
     * @param listData    List<AppsItemBean>
     * @param isFirstpage 是否是第一页
     * @param needSize    需要的个数
     * @return ArrayList<AppsItemBean>
     */
    public static ArrayList<AppsItemBean> filterResData(
            List<AppsItemBean> listData, boolean isFirstpage, int needSize) {
        ArrayList<AppsItemBean> listFilter = new ArrayList<AppsItemBean>();
        ArrayList<AppsItemBean> list = new ArrayList<AppsItemBean>();
        int size = listData.size();
        for (int i = 0; i < size; i++) {
            AppsItemBean item = listData.get(i);
            if (AppManagerCenter.isAppExist(item.packageName)) {
                listFilter.add(item);
                continue;
            }
            list.add(item);
        }
        if (isFirstpage) {
            int a = 0;
            while (list.size() < needSize && a < listFilter.size()
                    && listFilter.get(a) != null) {
                list.add(a, listFilter.get(a));
                a++;
            }

        }
        return list;
    }

    /**
     * 过滤已安装的应用（个数少于4个 则拼凑为4个）,并且过滤传入的包名
     *
     * @param packageName 包名
     * @param listData    ArrayList<AppsItemBean>
     * @return ArrayList<AppsItemBean>
     */
    public static ArrayList<AppsItemBean> filterDetailData(String packageName,
                                                           ArrayList<AppsItemBean> listData) {
        ArrayList<AppsItemBean> listFilter = new ArrayList<AppsItemBean>();
        ArrayList<AppsItemBean> list = new ArrayList<AppsItemBean>();
        int size = listData.size();
        for (int i = 0; i < size; i++) {
            AppsItemBean item = listData.get(i);
            if (packageName.equals(item.packageName)) {
                continue;
            }
            if (AppManagerCenter.isAppExist(item.packageName)) {
                listFilter.add(item);
                continue;
            }
            list.add(item);
            if (list.size() == 4)
                return list;
        }
        int a = 0;
        while (list.size() < 5 && a < listFilter.size()
                && listFilter.get(a) != null) {
            list.add(a, listFilter.get(a));
            a++;
        }

        return list;
    }

    /**
     * 过滤已安装(2.4增加，js交互，返回应用状态的字符串)
     *
     * @param jsonString json字符串
     * @return String
     */
    public static String jsCallInitStatusApps(
            String jsonString) {
        List<AppsItemBean> list = new ArrayList();
        try {
            Gson gson = new Gson();
            list = gson.fromJson(jsonString, new TypeToken<List<AppsItemBean>>() {
            }.getType());
            int size = list.size();
            for (int i = 0; i < size; i++) {
                AppsItemBean itemBean = list.get(i);
                if (AppManagerCenter.isAppExist(itemBean.packageName)) {
                    if (AppManagerCenter.appIsNeedUpate(itemBean.packageName, itemBean.versionCode)) {
                        itemBean.istatus = 2;
                    } else {
                        itemBean.istatus = 1;
                    }
                }
            }
        } catch (Exception e) {
            return "";
        }
        return new Gson().toJson(list);

    }

    /***
     * 获取更新的列表
     *
     * @param listData    List<AppsItemBean>
     * @return List<AppsItemBean>
     */
    public static List<AppsItemBean> getUpdateApps(
            List<AppsItemBean> listData) {
        ArrayList<AppsItemBean> listFilter = new ArrayList<AppsItemBean>();
        int size = listData.size();
        for (int i = 0; i < size; i++) {
            AppsItemBean item = listData.get(i);
            if (!TextUtils.isEmpty(item.packageName) && AppManagerCenter.isAppExist(item.packageName) && AppManagerCenter.appIsNeedUpate(item.packageName, item.versionCode)) {
                listFilter.add(item);
            }
        }
        return listFilter;
    }


    /**
     * 过滤掉未安装的应用，返回已安装应用
     */
    public static ArrayList<AppsItemBean> filterUnInstalled(
            ArrayList<AppsItemBean> listData) {
        ArrayList<AppsItemBean> listFilter = new ArrayList<AppsItemBean>();
        int size = listData.size();
        for (int i = 0; i < size; i++) {
            AppsItemBean item = listData.get(i);
            if (!TextUtils.isEmpty(item.packageName) && AppManagerCenter.isAppExist(item.packageName)) {
                listFilter.add(item);
            }
        }
        return listFilter;
    }

    /**
     * 过滤掉已安装的应用，返回未安装应用
     */
    public static ArrayList<AppsItemBean> filterInstalled(
            List<AppsItemBean> listData) {
        ArrayList<AppsItemBean> listFilter = new ArrayList<AppsItemBean>();
        int size = listData.size();
        for (int i = 0; i < size; i++) {
            AppsItemBean item = listData.get(i);
            if (!TextUtils.isEmpty(item.packageName) && !AppManagerCenter.isAppExist(item.packageName)) {
                listFilter.add(item);
            }
        }
        return listFilter;
    }

    /**
     * 过滤掉已安装的应用，返回未安装应用,达到需要的个数后，立即返回
     */
    public static ArrayList<AppsItemBean> filterInstalledNeedSize(
            List<AppsItemBean> listData, int needSize) {
        ArrayList<AppsItemBean> listFilter = new ArrayList<AppsItemBean>();
        int size = listData.size();
        for (int i = 0; i < size; i++) {
            AppsItemBean item = listData.get(i);
            if (!TextUtils.isEmpty(item.packageName) && !AppManagerCenter.isAppExist(item.packageName)) {
                listFilter.add(item);
                if (listFilter.size() >= needSize) {
                    return listFilter;
                }
            }
        }
        return listFilter;
    }

    /**
     * 返回需要更新的数据
     */
    public static ArrayList<AppsItemBean> getUpdateNeedSize(
            List<AppsItemBean> listData, int needSize) {
        ArrayList<AppsItemBean> listFilter = new ArrayList<AppsItemBean>();
        int size = listData.size();
        for (int i = 0; i < size; i++) {
            AppsItemBean item = listData.get(i);
            if (!TextUtils.isEmpty(item.packageName) && AppManagerCenter.isAppExist(item.packageName) && AppManagerCenter.appIsNeedUpate(item.packageName, item.versionCode)) {
                listFilter.add(item);
                if (listFilter.size() >= needSize) {
                    return listFilter;
                }
            }
        }
        return listFilter;
    }

    /**
     * 过滤掉礼包个数为0的应用
     */
    public static void filterNoGift(ArrayList<AppsItemBean> listData) {
        ArrayList<AppsItemBean> listFilter = new ArrayList<AppsItemBean>();
        for (AppsItemBean item : listData) {
            if (item.giftCount == 0) {
                listFilter.add(item);
            }
        }
        listData.removeAll(listFilter);
    }

    /**
     * 过滤掉已安装的应用，从列表中移除, 过滤完如果数目大于6，保留前6个
     */
    public static void filterAndRemoveInstalled(
            List<AppsItemBean> listData) {
        ArrayList<AppsItemBean> listFilter = new ArrayList<AppsItemBean>();
        for (int i = 0; i < listData.size(); i++) {
            AppsItemBean item = listData.get(i);
            if (!TextUtils.isEmpty(item.packageName) && AppManagerCenter.isAppExist(item.packageName)) {
                listFilter.add(item);
            }
        }
        listData.removeAll(listFilter);

        listFilter.clear();

        if (listData.size() > 6) {
            for (int i = 6; i < listData.size(); i++) {
                listFilter.add(listData.get(i));
            }

            listData.removeAll(listFilter);
        }
    }

    /***
     * 过滤已安装,并且转换为CarParentBean类型(2.2版本新增加)
     *
     * @param listData   ArrayList<AppsItemBean>
     * @param isHome  true:需要提出前三条
     * @return ArrayList<CarParentBean>
     */
    public static ArrayList<CarParentBean> filterInstalledAnd2CarParentBean(
            ArrayList<AppsItemBean> listData, boolean isHome) {
//        if (isHome) {
//            listData.removeAll(filterInstalledAndGetTopThree(listData)); //移除前三个应用
//        }
        ArrayList<CarParentBean> listFilter = new ArrayList<CarParentBean>();
        int size = listData.size();
        for (int i = 0; i < size; i++) {
            AppsItemBean item = listData.get(i);
            if (!TextUtils.isEmpty(item.packageName) && AppManagerCenter.isAppExist(item.packageName)) {
                continue;
            }
            listFilter.add(new CarParentBean("apps", null, item));
        }
        return listFilter;
    }

    /***
     * 过滤已安装,并且过滤精彩游戏前三个，并且转换为CarParentBean类型(2.2版本新增加,游戏页专用)
     *
     * @param listData  AppsItemBean的数据List
     * @param pkgList 包名List
     * @return ArrayList<CarParentBean>
     */
    public static ArrayList<CarParentBean> filterInstalledAnd2CarParentBean(
            ArrayList<AppsItemBean> listData, List<String> pkgList) {
        ArrayList<CarParentBean> listFilter = new ArrayList<CarParentBean>();
        int size = listData.size();
        for (int i = 0; i < size; i++) {
            AppsItemBean item = listData.get(i);
            if (!TextUtils.isEmpty(item.packageName)) {
                if (AppManagerCenter.isAppExist(item.packageName) || pkgList.contains(item.packageName)) {
                    continue;
                }
            }
            listFilter.add(new CarParentBean("apps", null, item));
        }
        return listFilter;
    }

    /**
     * 过滤已安装,并且获取前三个应用
     *
     * @param listData ArrayList<AppsItemBean>
     * @return ArrayList<AppsItemBean>
     */
    public static ArrayList<AppsItemBean> filterInstalledAndGetTopThree(
            ArrayList<AppsItemBean> listData) {
        ArrayList<AppsItemBean> listFilter = new ArrayList<AppsItemBean>();
        int size = listData.size();
        for (int i = 0; i < size; i++) {
            AppsItemBean item = listData.get(i);
            if (!TextUtils.isEmpty(item.packageName) && AppManagerCenter.isAppExist(item.packageName)) {
                continue;
            }
            if (listFilter.size() < 3)
                listFilter.add(item);
            else
                break;
        }
        return listFilter;
    }


    /***
     * 过滤暂时支持的布局类型(2.2版本新增加)
     *
     * @param listData   ArrayList<PrizeAppsCardData.FocusBean>
     * @return ArrayList<CarParentBean>
     */
    public static ArrayList<CarParentBean> filterFocus(
            ArrayList<PrizeAppsCardData.FocusBean> listData) {
        ArrayList<CarParentBean> listFilter = new ArrayList<CarParentBean>();
        int size = listData.size();
        for (int i = 0; i < size; i++) {
            PrizeAppsCardData.FocusBean item = listData.get(i);
            if (item == null || TextUtils.isEmpty(item.type)) {
                continue;
            }
            if (item.type.equals(Constants.WEB) || item.type.equals(Constants.TOPIC) ||
                    item.type.equals(Constants.APPSIN) || item.type.equals(Constants.NOTOPIC) ||
                    item.type.equals(Constants.APP) || item.type.equals(Constants.CATSIN) ||
                    item.type.equals(Constants.MATTS) || item.type.equals(Constants.HOTTEST) || item.type.equals(Constants.VIDEO)) {
//                if (item.type.equals(Constants.RANK)) {
//                    item.apps = filterFocusRankData(item.apps);
                if (item.type.equals(Constants.NOTOPIC)) {
                    item.apps = filterInstalled(item.apps);
                    if (item.apps == null || item.apps.size() < 5)
                        continue;
                }
                item.positon = i + 1;
                listFilter.add(new CarParentBean("focus", item, null));
            }
        }
        return listFilter;
    }

    /***
     * 过滤已经安装的应用，但是当小于needSize 时，自动补充一些已安装数据到列表。直到其size到达needSize
     *
     * @param listData 需要处理的list
     * @param needSize 控制最多显示个数
     * @return 过滤后的list
     */
    public static ArrayList<AppsItemBean> filterInstalled(
            List<AppsItemBean> listData, int needSize) {
        ArrayList<AppsItemBean> listFilter = new ArrayList<AppsItemBean>();
        ArrayList<AppsItemBean> installedList = new ArrayList<AppsItemBean>();
        int size = listData.size();
        for (int i = 0; i < size; i++) {
            AppsItemBean item = listData.get(i);
            if (!TextUtils.isEmpty(item.packageName) && AppManagerCenter.isAppExist(item.packageName)) {
                installedList.add(item);
                continue;
            }
            listFilter.add(item);
            if (listFilter.size() >= needSize) {
                return listFilter;
            }
        }

        int a = 0;
        while (listFilter.size() < needSize && a < installedList.size()
                && installedList.get(a) != null) {
            listFilter.add(a, installedList.get(a));
            a++;
        }
        return listFilter;
    }


    /***
     * 过滤已经安装的应用，但是当小于needSize 时，自动补充一些已安装数据到列表。直到其size到达needSize
     *
     * @param listData 需要处理的list
     * @param needSize 最多显示个数
     * @return 过滤后的list
     */
    public static List<HotKeyBean> filterSearchHotInstalled(List<HotKeyBean> listData, int needSize) {
        ArrayList<HotKeyBean> listFilter = new ArrayList<HotKeyBean>();
        ArrayList<HotKeyBean> installedList = new ArrayList<HotKeyBean>();
        int size = listData.size();
        for (int i = 0; i < size; i++) {
            HotKeyBean item = listData.get(i);
            if (!TextUtils.isEmpty(item.ikey) && "app".equals(item.ikey) && item.data != null && item.data.app != null && !TextUtils.isEmpty(item.data.app.packageName) && AppManagerCenter.isAppExist(item.data.app.packageName)) {
                installedList.add(item);
                continue;
            }
            listFilter.add(item);
        }
        if (listFilter.size() >= needSize) {
            Collections.shuffle(listFilter);
            return listFilter.subList(0, needSize);
        }
        Collections.shuffle(listFilter);
        int a = 0;
        while (listFilter.size() < needSize && a < installedList.size()
                && installedList.get(a) != null) {
            listFilter.add(a, installedList.get(a));
            a++;
        }
        return listFilter;
    }

    /***
     * 过滤已经安装的应用，但是当小于needSize 时，自动补充一些已安装数据到列表。直到其size到达needSize
     *
     * @param listData 需要处理的list
     * @param needSize 最少显示个数
     * @return 过滤后的list
     */
    public static ArrayList<AppsItemBean> filterSearchInstalled(
            List<AppsItemBean> listData, int needSize) {
        ArrayList<AppsItemBean> listFilter = new ArrayList<AppsItemBean>();
        ArrayList<AppsItemBean> installedList = new ArrayList<AppsItemBean>();
        int size = listData.size();
        for (int i = 0; i < size; i++) {
            AppsItemBean item = listData.get(i);
            if (!TextUtils.isEmpty(item.packageName) && AppManagerCenter.isAppExist(item.packageName)) {
                installedList.add(item);
                continue;
            }
            listFilter.add(item);
        }

        int a = 0;
        while (listFilter.size() < needSize && a < installedList.size()
                && installedList.get(a) != null) {
            listFilter.add(installedList.get(a));
            a++;
        }
        return listFilter;
    }

    /**
     * 当天是否已经领取积分了
     *
     * @param context Context
     * @param key     key
     * @return 是否已经领取积分
     */
    public static boolean IsGotWelfare(Context context, String key) {
        if (context == null || TextUtils.isEmpty(key))
            return false;
        Long timeSecond;
        try {
            timeSecond = PreferencesUtils.getLong(context, key, 0);
        } catch (Exception e) {
            timeSecond = System.currentTimeMillis();
            PreferencesUtils.putLong(context, key, timeSecond);
        }
        return timeSecond != 0 && isToday(timeSecond);
    }

    /**
     * 当天是否已经领取礼包了
     *
     * @param context Context
     * @param key     key
     * @return boolean
     */
    public static boolean IsGotWelfareGift(Context context, String key) {
        return !(context == null || TextUtils.isEmpty(key)) && PreferencesUtils.getBoolean(context, key, false);
    }

    /**
     * 判断给定字符串时间是否为今日
     *
     * @param sdate String
     * @return boolean
     */
    public static boolean isToday(String sdate) {
        if (TextUtils.isEmpty(sdate)) {
            return false;
        }
        Date d = new Date(Long.parseLong(sdate));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        String dateString = formatter.format(d);
        Date today = new Date();
        if (dateString != null) {
            String nowDate = formatter.format(today);
            return nowDate.equals(dateString);
        }
        return false;
    }

    /**
     * 判断给定字符串时间是否为今日
     *
     * @param sdate 时间long格式
     * @return boolean  boolean
     */
    private static boolean isToday(long sdate) {
        Date d = new Date(sdate);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        String dateString = formatter.format(d);
        Date today = new Date();
        if (dateString != null) {
            String nowDate = formatter.format(today);
            return nowDate.equals(dateString);
        }
        return false;
    }

    /***
     * @param filterList      list 数据
     * @param filterListfocus focus数据
     * @return ArrayList<CarParentBean>
     */
    public static ArrayList<CarParentBean> change2CarParentBean2(List<CarParentBean> filterList, List<CarParentBean> filterListfocus, int lastFootListCount) {
        ArrayList<CarParentBean> data = new ArrayList<CarParentBean>();
        int fSize = filterListfocus.size();
        int lSize = filterList.size();
        for (int i = 0; i <= fSize; i++) {
            int k = 0;
            for (int j = 5 * i; j < lSize; j++) {
                k++;
                data.add(filterList.get(j));
                if (i == 0) {
                    if ((5 - lastFootListCount) == k) {
                        break;
                    }
                }
                if (5 == k && i != fSize) {
                    break;
                }
            }
            if (i == fSize)
                return data;
            data.add(filterListfocus.get(i));
        }

        return data;
    }

    /**
     * 过滤Focus榜单数据（当未安装个数大于等于8个时，取8个，当少于5个个时，拼接已安装为5个）
     *
     * @param beans List<AppsItemBean>
     * @return List<AppsItemBean>
     */
    private static List<AppsItemBean> filterFocusRankData(List<AppsItemBean> beans) {
        if (beans == null)
            return null;
        List<AppsItemBean> listFilter = new ArrayList<AppsItemBean>();
        ArrayList<AppsItemBean> installedList = new ArrayList<AppsItemBean>();
        int size = beans.size();
        for (int i = 0; i < size; i++) {
            AppsItemBean bean = beans.get(i);
            if (AppManagerCenter.isAppExist(bean.packageName)) {
                installedList.add(bean);
                continue;
            }
            listFilter.add(bean);

        }
        if (listFilter.size() >= 8) {
            listFilter = listFilter.subList(0, 8);
        } else {
            if (listFilter.size() < 5) {
                int a = 0;
                while (listFilter.size() < 5 && a < installedList.size()
                        && installedList.get(a) != null) {
                    listFilter.add(a, installedList.get(a));
                    a++;
                }
            }

        }
        return listFilter;
    }

    /**
     * 过滤游戏页精彩游戏数据（当少于5个时，拼接已安装为5个）
     *
     * @param beans List<AppsItemBean>
     * @return List<AppsItemBean>
     */
    public static List<AppsItemBean> filterWonderfulData(List<AppsItemBean> beans) {
        if (beans == null)
            return null;
        List<AppsItemBean> listFilter = new ArrayList<AppsItemBean>();
        ArrayList<AppsItemBean> installedList = new ArrayList<AppsItemBean>();
        int size = beans.size();
        for (int i = 0; i < size; i++) {
            AppsItemBean bean = beans.get(i);
            if (AppManagerCenter.isAppExist(bean.packageName)) {
                installedList.add(bean);
                continue;
            }
            listFilter.add(bean);

        }

        if (listFilter.size() < 5) {
            int a = 0;
            while (listFilter.size() < 5 && a < installedList.size()
                    && installedList.get(a) != null) {
                listFilter.add(installedList.get(a));
                a++;
            }
        }
        return listFilter;
    }


    /**
     * 过滤游戏页玩家福利数据（按照如下权重排列7（未领取的已安装的礼包应用）;6(未领取的已安装的积分应用)；5（未领取的未安装的礼包应用）；
     * 4（未领取的未安装的积分应用）；3（已领取的已安装礼包应用）；2（已领取的已安装积分应用）；1（已领取的未安装礼包应用）；
     * 0（已领取的的未安装的积分应用））
     *
     * @param beans 按规则排序后的前三位数据list
     * @return List<WelfareBean>
     */
    public static List<WelfareBean> filterWelfareData2(Context context, List<WelfareBean> beans) {
        if (beans == null || beans.size() <= 0)
            return null;
        for (WelfareBean welfareBean : beans) {
            if (AppManagerCenter.isAppExist(welfareBean.app.packageName)) {//遍历已安装的礼包应用
                if ("gift".equals(welfareBean.type)) {
                    boolean isGotWelfareGift = CommonUtils.IsGotWelfareGift(context, Constants.KEY_WELFARE_GOT_GIFT + welfareBean.app.packageName);
                    if (!isGotWelfareGift) {
                        welfareBean.weight = 7;
                    } else {
                        welfareBean.weight = 3;
                    }
                } else {
                    boolean isGotWelfarePoint = CommonUtils.IsGotWelfare(context, Constants.KEY_WELFARE_GOT + welfareBean.app.packageName);
                    if (!isGotWelfarePoint) {
                        welfareBean.weight = 6;
                    } else {
                        welfareBean.weight = 2;
                    }
                }
            } else {
                if ("gift".equals(welfareBean.type)) {
                    boolean isGotWelfareGift = CommonUtils.IsGotWelfareGift(context, Constants.KEY_WELFARE_GOT_GIFT + welfareBean.app.packageName);
                    if (!isGotWelfareGift) {
                        welfareBean.weight = 5;
                    } else {
                        welfareBean.weight = 1;
                    }
                } else {
                    boolean isGotWelfarePoint = CommonUtils.IsGotWelfare(context, Constants.KEY_WELFARE_GOT + welfareBean.app.packageName);
                    if (!isGotWelfarePoint) {
                        welfareBean.weight = 4;
                    } else {
                        welfareBean.weight = 0;
                    }
                }
            }
        }
        Collections.sort(beans, new Comparator<WelfareBean>() {
            @Override
            public int compare(WelfareBean o1, WelfareBean o2) {
                if (o1.weight < o2.weight) {
                    return 1;
                } else if (o1.weight > o2.weight) {
                    return -1;
                } else {
                    return 0;
                }
            }
        });
        if (JLog.isDebug) {
            JLog.i(TAG, "beans=" + beans);
        }
        return beans.subList(0, 3);
    }

    /**
     * 获取抽屉中app列表 只需要3个
     */
    public static List<AppsItemBean> getDrawerData(DrawerData data) {
        List<AppsItemBean> drawerData = filterInstalledNeedSize(data.firstAppInfos, 2);
        drawerData.addAll(filterInstalledNeedSize(data.secondAppInfos, 3 - drawerData.size()));
        return drawerData;
    }


    /***
     * @param filterList      list 数据
     * @param filterListfocus focus数据  实际需要的Focus数据
     * @return ArrayList<CarParentBean>
     */
    public static ArrayList<CarParentBean> change2CarParentBean(List<CarParentBean> filterList, List<CarParentBean> filterListfocus, int lastFootListCount) {
        ArrayList<CarParentBean> data = new ArrayList<CarParentBean>();
        int fSize = filterListfocus.size();
        int lSize = filterList.size();
        for (int i = 0; i <= fSize; i++) {
            if (lastFootListCount == 5 && i == 0) {
                data.add(filterListfocus.get(i));
                continue;
            }
            int k = 0;
            for (int j = 5 * i; j < lSize; j++) {
                data.add(filterList.get(j));
                k++;
                if (i == 0) {
                    if ((5 - lastFootListCount) == k) {
                        break;
                    }
                }
                if (5 == k) {
                    break;
                }

            }
            if (i == fSize) {
                return data;

            }
            data.add(filterListfocus.get(i));

        }//此时

        return data;
    }

    /**
     * xx月xx日；
     *
     * @param appPackage 包名
     * @return 安装时间
     */
    public static String getInstallTime(String appPackage) {
        long installTime = System.currentTimeMillis();
        if (BaseApplication.curContext == null)
            return getFormateTime(installTime);

        try {
            ApplicationInfo applicationInfo = BaseApplication.curContext.getPackageManager().getApplicationInfo(
                    appPackage, 0);
            if (applicationInfo == null)
                return String.valueOf(System.currentTimeMillis());
            String dir = applicationInfo.publicSourceDir;
            installTime = new File(dir).lastModified();
            return getFormateTime(installTime);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return getFormateTime(installTime);
        }
    }

    /**
     * 获取格式化时间戳（MM月dd日）
     *
     * @param installtime 时间的long格式
     * @return String
     */
    public static String getFormateTime(long installtime) {
//        long currentTime = (System.currentTimeMillis() - installtime) / 1000;
//        if (currentTime < 60 * 10) //10分钟
//            return "刚刚";
//        if (isToday(installtime + ""))
//            return "今天";
//        if (currentTime <= 60 * 60*48) //10分钟
//            return "昨天";
//        int year = isCurrentYear(installtime + "");
//        if (year == 0) {
        SimpleDateFormat formatter = new SimpleDateFormat("MM月dd日");
        return formatter.format(new Date(installtime));
//        } else {
//            return year + "年前";
//        }

    }

//    /**
//     * 判断给定字符串时间返回的年差
//     *
//     * @param sdate
//     * @return int
//     */
//
//    public static int isCurrentYear(String sdate) {
//        if (TextUtils.isEmpty(sdate)) {
//            return 0;
//        }
//        Date d = new Date(Long.parseLong(sdate));
//        SimpleDateFormat formatter = new SimpleDateFormat("yyyy");
//        String dateString = formatter.format(d);
//        Date today = new Date();
//        if (dateString != null) {
//            String nowDate = formatter.format(today);
//            JLog.i("HomePagerListAdapter", "----给定年份=" + dateString + "----当前的年份nowDate=" + nowDate);
//            return Integer.parseInt(nowDate) - Integer.parseInt(dateString);
//        }
//        return 0;
//    }

//    /**
//     * 判断给定字符串时间是否为昨天
//     *
//     * @param sdate
//     * @return boolean
//     */
//    public static boolean isYestoday(String sdate) {
//        if (TextUtils.isEmpty(sdate)) {
//            return false;
//        }
//        Date d = new Date(Long.parseLong(sdate));
//        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
//        String dateString = formatter.format(d);
//        Date today = new Date();
//        if (dateString != null) {
//            String nowDate = formatter.format(today);
//            return nowDate.equals(dateString);
//        }
//        return false;
//    }

    /**
     * 发送桌面提醒
     *
     * @param context Context
     * @param count   个数
     */
    public static void sendCautionBroadcast(Context context, int count) {
        if (context == null)
            return;
        Intent intent = new Intent();//酷宇的launcher
        intent.setAction(Constants.ACTION_UNREAD_CHANGED);
        intent.putExtra("package", context.getApplicationInfo().packageName);
        intent.putExtra(Constants.EXTRA_UNREAD_NUMBER, count);
        intent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        context.sendStickyBroadcast(intent);

        if (BaseApplication.isThird) {
            return;
        }
        Intent intent2 = new Intent();//我们自己的launcher
        ComponentName componentName = new ComponentName(context.getApplicationInfo().packageName, "com.prize.appcenter.PlayPlusClientActivity");
        intent2.setAction(Intent.ACTION_UNREAD_CHANGED);
        intent2.putExtra(Intent.EXTRA_UNREAD_COMPONENT, componentName);
        intent2.putExtra(Intent.EXTRA_UNREAD_NUMBER, count);
        intent2.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        context.sendStickyBroadcast(intent2);

        try {
            final ContentResolver cr = context.getContentResolver();
            android.provider.Settings.System.putInt(cr, "com_android_appstore_mtk_unread", count);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取不超过长度的字符串
     *
     * @param content 字符串
     * @return 字符串
     */
    public static String getMaxLenStr(String content) {
        if (!TextUtils.isEmpty(content) && content.length() >= 20) {
            content = content.substring(0, 20);
        }
        return content;
    }

    /**
     * 判断是否黑屏
     *
     * @param c Context
     * @return boolean
     */
    public static boolean isScreenLocked(Context c) {
        android.app.KeyguardManager mKeyguardManager = (KeyguardManager) c.getSystemService(Context.KEYGUARD_SERVICE);
        return mKeyguardManager.inKeyguardRestrictedInputMode();

    }


    /**
     * 返回有差分包的情况下，可以节省流量的大小
     *
     * @param updateApps List<AppsItemBean>
     * @return 返回有差分包的情况下，可以节省流量的大小
     */
    public static String calTatolSize(List<AppsItemBean> updateApps) {
        long total = 0;
        long realTotal = 0;
        if (updateApps == null)
            return "";
        int size = updateApps.size();
        if (size <= 0)
            return "";
        for (int i = 0; i < size; i++) {
            AppsItemBean bean = updateApps.get(i);
            if (bean.apkSize == null) {
                continue;
            }
            long apkSize = Long.parseLong(bean.apkSize);
            total += apkSize;
            if (bean.appPatch != null) {
                String oldApkSource = ApkUtils.getSourceApkPath(BaseApplication.curContext, bean.packageName);
                if (!TextUtils.isEmpty(oldApkSource) && !TextUtils.isEmpty(bean.appPatch.fromApkMd5)) {
                    // 校验一下本地安装APK的MD5是不是和真实的MD5一致
                    if (SignUtils.checkMd5(oldApkSource, bean.appPatch.fromApkMd5)) {
                        realTotal += bean.appPatch.patchSize;
                    } else {
                        realTotal += apkSize;
                    }
                } else {
                    realTotal += apkSize;
                }

            } else {
                realTotal += apkSize;
            }
        }
        if (total == realTotal) {
            return "";
        }
        return CommonUtils.formatSize(total - realTotal, "#.0");
    }

//    private static Set<String> fileItemList = new HashSet<String>();

//    static {
//        fileItemList.add("META-INF/CERT.RSA");
//        fileItemList.add("META-INF/CERT.SF");
//        fileItemList.add("META-INF/MANIFEST.MF");
//        fileItemList.add("AndroidManifest.xml");
//        fileItemList.add("resources.arsc");
//        fileItemList.add("classes.dex");
//    }

    private static final String TAG = "CommonUtils";

//    /**
//     * 是否是合法应用
//     *
//     * @param apkPath 文件路径
//     * @return true：合法。否则非法
//     */
//    public static boolean isLegalApk(String apkPath) {
//        boolean result = true;
//        long start = System.currentTimeMillis();
//        ZipEntry entry = null;
//        ZipFile zipFile = null;
//        ZipInputStream zipInput = null;
//        try {
//            File file = new File(apkPath);
//            zipFile = new ZipFile(file);
//            zipInput = new ZipInputStream(new FileInputStream(file));
//            InputStream input = null;
//            while ((entry = zipInput.getNextEntry()) != null) {
//                if (fileItemList.contains(entry.getName())) {
//                    input = zipFile.getInputStream(entry);
//                    input.skip(1024 * 10000);
//                    input.close();
//                }
//            }
//        } catch (Exception e) {
//            Log.e(TAG, "check apk file error: " + entry.getName() + "--apkPath=" + apkPath);
//            result = false;
//            e.printStackTrace();
//        } finally {
//            if (zipInput != null) {
//                try {
//                    zipInput.close();
//                } catch (IOException e) {
//                    result = false;
//                    e.printStackTrace();
//                }
//            }
//            if (zipFile != null) {
//                try {
//                    zipFile.close();
//                } catch (IOException e) {
//                    result = false;
//                    e.printStackTrace();
//                }
//            }
//            Log.i(TAG, "check used time:" + (System.currentTimeMillis() - start) + "ms.");
//        }
//        return result;
//    }

    /**
     * 形成html标签形式的语句（系统屏蔽了设置颜色这个对空格会过滤）
     *
     * @param color   颜色值 例如：#ff0000
     * @param content 需要变化颜色的字体
     * @return String
     */
    public static String formHtml(String color, String content) {
        StringBuilder builder = new StringBuilder("<font color='");
        return builder.append(color).append("'>").append(content).append("</font>").toString();
    }

    /**
     * 形成SpannableString的语句（系统屏蔽了设置颜色这个对内容空格也会起作用）
     *
     * @param color   颜色值 例如：#ff0000
     * @param content 需要变化颜色的字体
     * @return SpannableString
     */
    public static SpannableString formCustomTextColor(String color, String content) {
        SpannableString spannableString = new SpannableString(content);
        spannableString.setSpan(new ForegroundColorSpan(Color.parseColor(color)), 0, spannableString.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        return spannableString;
    }


//    /**
//     * @param mExposureBeans List<ExposureBean>
//     * @param guiname        界面名称
//     * @param carParentBean  CarParentBean
//     * @param position       位置：1、2、3、4  list所在位置
//     * @param viewType       item类型
//     * @return List<ExposureBean>
//     */
//    public static List<ExposureBean> formatAppPagerExposure(List<ExposureBean> mExposureBeans, String guiname,
//                                                            CarParentBean carParentBean, int position, int viewType) {
//        String data = null;
//        String title = null;
//        String backParams = null;
//        boolean isList = false;
//        switch (viewType) {
//            case Constants.TYPE_FOCUS_RANK:
//                return mExposureBeans;
//            case Constants.TYPE_APP_LIST:
//                data = carParentBean.mAppItemBean.id;
//                title = carParentBean.mAppItemBean.name;
//                if (!TextUtils.isEmpty(carParentBean.mAppItemBean.backParams)) {
//                    backParams = carParentBean.mAppItemBean.backParams;
//                }
//                isList = true;
//                break;
//            case Constants.TYPE_FOCUS_WEB:
//            case Constants.TYPE_FOCUS_APPSIN:
//            case Constants.TYPE_FOCUS_CATSIN:
//            case Constants.TYPE_FOCUS_MATTS:
//                data = String.valueOf(carParentBean.focus.id);
//                title = carParentBean.focus.title;
//                break;
//            case Constants.TYPE_FOCUS_APP:
//            case Constants.TYPE_FOCUS_HOTTEST:
//            case Constants.TYPE_FOCUS_VIDEO:
//                for (int i = 0; i < 2; i++) {
//                    ExposureBean bean = new ExposureBean();
//                    bean.gui = guiname;
//                    bean.widget = Constants.LIST;
//                    if (i == 0) {
//                        bean.type = "focus";
//                        bean.datas = String.valueOf(carParentBean.focus.id);
//                        bean.title = carParentBean.focus.title;
//                    } else {
//                        bean.type = "app";
//                        bean.datas = carParentBean.focus.app.id;
//                        bean.title = carParentBean.focus.app.name;
//                        bean.child_position = String.valueOf(i);
//                        bean.parent_type = "focus";
//                        if (!TextUtils.isEmpty(carParentBean.focus.app.backParams)) {
//                            bean.backParams = carParentBean.focus.app.backParams;
//                        }
//                        bean.parent_datas = String.valueOf(carParentBean.focus.id);
//                    }
//                    bean.position = String.valueOf(position);
//                    if (!mExposureBeans.contains(bean)) {
//                        mExposureBeans.add(bean);
//                    }
//
//                }
//                return mExposureBeans;
//            case Constants.TYPE_FOCUS_TOPIC:
//                int size = carParentBean.focus.apps.size();
//                if (carParentBean.focus.apps.size() > 4) {
//                    size = 4;
//                }
//                ExposureBean bean = new ExposureBean();
//                bean.gui = guiname;
//                bean.widget = Constants.LIST;
//                bean.position = String.valueOf(position);
//                bean.type = "focus";
//                bean.datas = String.valueOf(carParentBean.focus.id);
//                bean.title = carParentBean.focus.title;
//                if (!mExposureBeans.contains(bean)) {
//                    mExposureBeans.add(bean);
//                }
//                for (int i = 0; i < size; i++) {
//                    ExposureBean beans = new ExposureBean();
//                    beans.gui = guiname;
//                    beans.widget = Constants.LIST;
//                    AppsItemBean itemBean = carParentBean.focus.apps.get(i);
//                    beans.type = "app";
//                    beans.datas = itemBean.id;
//                    beans.title = itemBean.name;
//                    if (!TextUtils.isEmpty(itemBean.backParams)) {
//                        beans.backParams = itemBean.backParams;
//                    }
//                    beans.child_position = String.valueOf(i);
//                    beans.position = String.valueOf(position);
//                    beans.parent_type = "focus";
//                    beans.parent_datas = String.valueOf(carParentBean.focus.id);
//                    if (!mExposureBeans.contains(beans)) {
//                        mExposureBeans.add(beans);
//                    }
//                }
//                return mExposureBeans;
//            case Constants.TYPE_FOCUS_NOTOPIC:
//                int size1 = carParentBean.focus.apps.size();
//                if (carParentBean.focus.apps.size() > 5) {
//                    size1 = 5;
//                }
//                ExposureBean bean1 = new ExposureBean();
//                bean1.gui = guiname;
//                bean1.widget = Constants.LIST;
//                bean1.position = String.valueOf(position);
//                bean1.type = "focus";
//                bean1.datas = String.valueOf(carParentBean.focus.id);
//                bean1.title = carParentBean.focus.title;
//                if (!mExposureBeans.contains(bean1)) {
//                    mExposureBeans.add(bean1);
//                }
//                for (int i = 0; i < size1; i++) {
//                    ExposureBean beans = new ExposureBean();
//                    beans.gui = guiname;
//                    beans.widget = Constants.LIST;
//                    AppsItemBean itemBean = carParentBean.focus.apps.get(i);
//                    beans.type = "app";
//                    beans.datas = itemBean.id;
//                    beans.title = itemBean.name;
//                    if (!TextUtils.isEmpty(itemBean.backParams)) {
//                        beans.backParams = itemBean.backParams;
//                    }
//                    beans.child_position = String.valueOf(i);
//                    beans.position = String.valueOf(position);
//                    beans.parent_type = "focus";
//                    beans.parent_datas = String.valueOf(carParentBean.focus.id);
//                    if (!mExposureBeans.contains(beans)) {
//                        mExposureBeans.add(beans);
//                    }
//                }
//                return mExposureBeans;
//        }
//        ExposureBean bean = new ExposureBean();
//        bean.gui = guiname;
//        bean.widget = Constants.LIST;
//        if (isList) {
//            bean.type = "app";
//        } else {
//            bean.type = "focus";
//        }
//        bean.position = String.valueOf(position);
//        bean.datas = data;
//        bean.title = title;
//        if (!TextUtils.isEmpty(backParams)) {
//            bean.backParams = backParams;
//        }
//        if (!mExposureBeans.contains(bean)) {
//            mExposureBeans.add(bean);
//        }
//        return mExposureBeans;
//    }

    /**
     * 添加曝光list  3.2add
     *
     * @param mExposureBeans List<ExposureBean>
     * @param guiname        页面名称
     * @param carParentBean  CarParentBean
     * @param viewType       int
     * @return List<ExposureBean>
     */
    public static List<ExposureBean> formNewPagerExposure(List<ExposureBean> mExposureBeans, String guiname,
                                                          CarParentBean carParentBean, int viewType) {
        if (viewType != Constants.TYPE_APP_LIST) return mExposureBeans;
        ExposureBean bean = new ExposureBean();
        bean.gui = guiname;
        bean.widget = Constants.LIST;
        bean.appId = carParentBean.mAppItemBean.id;
//        bean.appName =carParentBean.mAppItemBean.name;
        bean.appName = string2Unicode(carParentBean.mAppItemBean.name);
//        if (JLog.isDebug) {
//            JLog.i(TAG,"formNewPagerExposure--bean.appName="+bean.appName);
//        }
        bean.packageName = carParentBean.mAppItemBean.packageName;
        bean.sourceType = carParentBean.mAppItemBean.sourceType;
        if (!mExposureBeans.contains(bean)) {
            mExposureBeans.add(bean);
        }
        return mExposureBeans;
    }

    /**
     * 设置类似广告360的
     *
     * @param mExposureBeans List<ExposureBean>
     * @param guiname        页面名称
     * @param carParentBean  CarParentBean
     * @param viewType       int
     * @return List<ExposureBean>
     */
    public static List<ExposureBean> form360PagerExposure(List<ExposureBean> mExposureBeans, String guiname,
                                                          CarParentBean carParentBean, int viewType) {
        if (viewType != Constants.TYPE_APP_LIST) return mExposureBeans;
        if (TextUtils.isEmpty(carParentBean.mAppItemBean.backParams)) return mExposureBeans;
        ExposureBean bean = new ExposureBean();
        bean.gui = guiname;
        bean.widget = Constants.LIST;
        bean.datas = carParentBean.mAppItemBean.id;
        bean.title = carParentBean.mAppItemBean.name;
        bean.backParams = carParentBean.mAppItemBean.backParams;
        if (!mExposureBeans.contains(bean)) {
            mExposureBeans.add(bean);
        }
        return mExposureBeans;
    }

    /**
     * 字符串转换unicode
     */
    public static String string2Unicode(String string) {

        try {
            if (TextUtils.isEmpty(string)) return string;
            return URLEncoder.encode(string, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
//        return string;
//        StringBuffer unicode = new StringBuffer();
//        for (int i = 0; i < string.length(); i++) {
//            // 取出每一个字符
//            char c = string.charAt(i);
//            // 转换为unicode
//            unicode.append("\\u" + Integer.toHexString(c));
//        }
//        return unicode.toString();
    }

    /**
     * unicode 转字符串
     */
    public static String unicode2String(String unicode) {
        try {
            if (TextUtils.isEmpty(unicode)) return unicode;
            return URLDecoder.decode(unicode, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
//        if(TextUtils.isEmpty(unicode)||!unicode.contains("\\u"))return unicode;
//        StringBuffer string = new StringBuffer();
//
//        String[] hex = unicode.split("\\\\u");
//
//        for (int i = 1; i < hex.length; i++) {
//
//            // 转换出每一个代码点
//            int data = Integer.parseInt(hex[i], 16);
//
//            // 追加成string
//            string.append((char) data);
//        }
//
//        return string.toString();
    }

    /**
     * 添加曝光bean  3.2add
     *
     * @param guiname 页面名称
     * @param widget  二级页面名称
     * @return ExposureBean
     */
    public static ExposureBean formNewPagerExposure(AppsItemBean appBean, String guiname, String widget) {
        ExposureBean bean = new ExposureBean();
        bean.gui = guiname;
        bean.widget = widget;
        bean.appId = appBean.id;
        bean.appName = appBean.name;
        bean.appName = string2Unicode(appBean.name);
        bean.packageName = appBean.packageName;
        bean.sourceType = appBean.sourceType;
        return bean;
    }

    /**
     * 新下载完成应用信息上传（3.2add）
     *
     * @param appId       appId
     * @param packageName 包名
     * @param appName     名称
     * @param pageInfo    页面信息
     * @return ExposureBean
     */
    public static ExposureBean formNewPagerExposure(String appId, String packageName, String appName, String pageInfo) {
        ExposureBean bean = null;
        if (!TextUtils.isEmpty(pageInfo)) {
            bean = GsonParseUtils.parseSingleBean(pageInfo, ExposureBean.class);
        }
        if (bean == null) {
            bean = new ExposureBean();

        }
        bean.appId = appId;
        bean.appName = string2Unicode(appName);
        bean.packageName = packageName;
        return bean;
    }

//    /**
//     * 转换合成 ExposureBean
//     *
//     * @param guiname  页面名字
//     * @param isList   是否是列表
//     * @param position 位置：1、2、3、4  list所在位置
//     * @param datas    list item类型
//     * @param title    应用或者标题名字
//     * @return ExposureBean
//     */
//    public static ExposureBean formatAppPagerExposure(String guiname, boolean isList, int position, String datas, String title, String backParams) {
//        ExposureBean bean = new ExposureBean();
//        bean.gui = guiname;
//        bean.widget = Constants.LIST;
//        if (isList) {
//            bean.type = "app";
//        } else {
//            bean.type = "focus";
//        }
//        bean.position = String.valueOf(position);
//        bean.datas = datas;
//        bean.title = title;
//        if (!TextUtils.isEmpty(backParams)) {
//            bean.backParams = backParams;
//        }
//        return bean;
//    }


//    /**
//     * 合成应用页的头布局信息
//     *
//     * @param position 位置
//     * @param data     主键id
//     * @param title    标题
//     * @param adType   类型（eg:topic）
//     * @return ExposureBean
//     */
//    public static ExposureBean formatAppPagerHeadExposure(int position, String data, String title, String adType) {
//        ExposureBean bean = new ExposureBean();
//        bean.gui = "apppage";
//        bean.widget = "head";
//        bean.type = adType;
//        bean.position = String.valueOf(position);
//        bean.datas = data;
//        bean.title = title;
//        return bean;
//    }

//    /**
//     * 应用页专用 转换合成 ExposureBean
//     *
//     * @param gui        所在界面
//     * @param widget     界面所在布局控件等
//     * @param type       对应数据类型
//     * @param position   位置：1、2、3、4
//     * @param data       主键（id）
//     * @param title      标题（应用名、focus标题）
//     * @param backParams 360打点
//     * @return ExposureBean
//     */
//    public static ExposureBean formatGameHeadExposure(String gui, String widget, String type, int position, String data, String title, String backParams) {
//        ExposureBean bean = new ExposureBean();
//        bean.gui = gui;
//        bean.widget = widget;
//        bean.position = String.valueOf(position);
//        bean.type = type;
//        bean.datas = data;
//        bean.title = title;
//        if (!TextUtils.isEmpty(backParams)) {
//            bean.backParams = backParams;
//        }
//        return bean;
//    }

    /**
     * 主要根据竖直方向是否可见(游戏页专用 含有tab栏的遮挡)
     *
     * @param view View
     * @return 是否可见
     */
    public static boolean isVisibleGamepageView(View view) {
        if (view == null)
            return false;
        int[] location = new int[2];
        view.getLocationInWindow(location);
        if (location[1] > 0) {
            if (location[1] < DisplayUtil.forMatSpAndDp(TypedValue.COMPLEX_UNIT_DIP, 101)) {//tab栏高度
                if (JLog.isDebug) {
                    JLog.i(TAG, "isVisibleGamePageView-=" + (location[1] > (DisplayUtil.forMatSpAndDp(TypedValue.COMPLEX_UNIT_DIP, 101) - view.getHeight())) + "--View高度：" + view.getHeight()
                            + "-forMatSpAndDp=" + DisplayUtil.forMatSpAndDp(TypedValue.COMPLEX_UNIT_DIP, 101) + "--location[1]=" + location[1] + "--view=" + view);
                }
                return location[1] > (DisplayUtil.forMatSpAndDp(TypedValue.COMPLEX_UNIT_DIP, 101) - view.getHeight());
            } else {
                if (JLog.isDebug) {
                    JLog.i(TAG, "isVisibleGamePageView-=" + ((location[1] + DisplayUtil.forMatSpAndDp(TypedValue.COMPLEX_UNIT_DIP, 49)) < ClientInfo.getInstance().screenHeight) + "--location[1] =" + location[1] + "--view=" + view);
                }
                return (location[1] + DisplayUtil.forMatSpAndDp(TypedValue.COMPLEX_UNIT_DIP, 49)) < ClientInfo.getInstance().screenHeight;//底部tab栏高度
            }
        } else {
            return false;
        }
    }

    /**
     * View是否在可视范围内（主要根据竖直方向是否可见）
     *
     * @param view View
     * @return 是否可见
     */
    public static boolean isViewVisible(View view) {
        if (view == null)
            return false;
        int[] location = new int[2];
        view.getLocationInWindow(location);
        if (location[1] < 0) {
//            if (JLog.isDebug) {
//                JLog.i(TAG, "isViewVisible-=" + ((location[1] + view.getHeight()) > 0) + "---location[1]=" + location[1] + "--view.getHeight()=" + view.getHeight() + "--view=" + view);
//            }
            return (location[1] + view.getHeight()) > 0;
        }
//        if (JLog.isDebug) {
//            JLog.i(TAG, "isViewVisible-=" + (location[1] < ClientInfo.getInstance().screenHeight && (location[1] > 0)) + "---location[1]=" + location[1] + "--view.getHeight()=" + view.getHeight() + "--view=" + view);
//        }
        return location[1] < ClientInfo.getInstance().screenHeight && (location[1] > 0);
    }

    /**
     * 详情View是否在可视范围内（主要根据竖直方向是否可见）
     *
     * @param view View
     * @return 是否可见
     */
    public static boolean isViewVisibleForDetail(View view) {
        if (view == null)
            return false;
        int[] location = new int[2];
        view.getLocationInWindow(location);
        if (location[1] < 0) {
            if (JLog.isDebug) {
                JLog.i(TAG, "isViewVisible-=" + ((location[1] + view.getHeight()) > 0) + "---location[1]=" + location[1] + "--view.getHeight()=" + view.getHeight() + "--view=" + view);
            }
            return (location[1] + view.getHeight()) > 0;
        }
        if (JLog.isDebug) {
            JLog.i(TAG, "isViewVisible-=" + (location[1] < (ClientInfo.getInstance().screenHeight - DisplayUtil.forMatSpAndDp(TypedValue.COMPLEX_UNIT_DIP, 57)) && (location[1] > 0)) + "---location[1]=" + location[1] + "--view.getHeight()=" + view.getHeight() + "--view=" + view);
        }
        //DisplayUtil.forMatSpAndDp(TypedValue.COMPLEX_UNIT_DIP, 57) 底部下载按钮的高度
        return location[1] < (ClientInfo.getInstance().screenHeight - DisplayUtil.forMatSpAndDp(TypedValue.COMPLEX_UNIT_DIP, 57)) && (location[1] > 0);
    }

    /**
     * @param view View
     * @return 是否可见
     */
    public static boolean getHorizontalScrollViewVisible(View view) {
        if (view == null)
            return false;
        int[] location = new int[2];
        view.getLocationInWindow(location);
//        if (JLog.isDebug) {
//            JLog.i(TAG, "getHorizontalScrollViewVisible-location[1]< ClientInfo.getInstance().screenWidth=" + (location[0] < ClientInfo.getInstance().screenWidth && location[0] > 0) + "---location[0]:" + location[0]);
//        }
        if (location[0] < 0) {
            return location[0] < ClientInfo.getInstance().screenWidth && Math.abs(location[0]) < view.getWidth();
        } else {
            return location[0] < ClientInfo.getInstance().screenWidth;

        }
    }

    /**
     * 添加应用的页面信息 下载用 必须要带souceType 否则
     *
     * @param appBean  AppsItemBean
     * @param gui      app所在页面
     * @param widget   app所在页面控件
     * @param position pp所在页面控件的位置
     * @return AppsItemBean
     */
    public static AppsItemBean formatAppPageInfo(AppsItemBean appBean, String gui, String widget, int position) {
        if (!TextUtils.isEmpty(appBean.pageInfo))
            return appBean;
        ExposureBean bean = new ExposureBean();
        bean.gui = gui;
        bean.widget = widget;
        bean.sourceType = appBean.sourceType;
        appBean.pageInfo = new Gson().toJson(bean);
        if (JLog.isDebug) {
            JLog.i(TAG, "formatAppPageInfo-AppsItemBean.pageInfo=" + appBean.pageInfo + "--appBean.name=" + appBean.name);
        }
        return appBean;
    }

    /**
     * 添加应用的页面信息(只要是请求下载时使用) 3.2add
     *
     * @param appBean  AppsItemBean
     * @param pageInfo pp所在页面控件的位置
     * @return AppsItemBean
     */
    public static AppsItemBean formatNewAppPageInfo(AppsItemBean appBean, String pageInfo) {
//        if (!TextUtils.isEmpty(appBean.pageInfo))
//            return appBean;
        if (JLog.isDebug) {
            JLog.i(TAG, "转换前formatNewAppPageInfo-AppsItemBean.pageInfo=" + pageInfo);
        }
        if (TextUtils.isEmpty(pageInfo))
            return appBean;
        ExposureBean eBean = GsonParseUtils.parseSingleBean(pageInfo, ExposureBean.class);
        if (eBean.sourceType != -1) {//传进来就有souceType
            appBean.pageInfo = pageInfo;
            return appBean;
        }
        eBean.sourceType = appBean.sourceType;
        appBean.pageInfo = new Gson().toJson(eBean);
//        }
        return appBean;
    }


    /**
     * 转换合成 ExposureBean(旧版本)
     *
     * @param guiname 页面名字
     * @param widge   控件
     * @param datas   list item类型
     * @param title   应用或者标题名字
     * @return ExposureBean
     */
    public static ExposureBean formatSearchHeadExposure(String guiname, String widge, String datas, String title, String backParams) {
        if (TextUtils.isEmpty(backParams)) return null;
        ExposureBean bean = new ExposureBean();
        bean.gui = guiname;
        bean.widget = widge;
        bean.datas = datas;
        bean.title = title;
        bean.backParams = backParams;
        JLog.i(TAG, "formatSearchHeadExposure-bean=" + bean);
        return bean;
    }

    private static void forceStopProgress(int pid) {
        try {

            android.os.Process.killProcess(pid);
            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * kill后台进程
     *
     * @param cxt           Context
     * @param isMainProcess 是否只kill主进程
     */
    public static void killProcessByPId(Context cxt, boolean isMainProcess) {
        ActivityManager am = (ActivityManager) cxt
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningAppProcessInfo> runningApps = am.getRunningAppProcesses();
        if (runningApps == null) {
            return;
        }
        if (JLog.isDebug) {
            JLog.i(TAG, "killProcessByPId=");
        }
        String proceeName = cxt.getPackageName();
        String remoteProceeName = proceeName + ":remote";
        int mainPid = 0;
        for (RunningAppProcessInfo procInfo : runningApps) {
            if (JLog.isDebug) {
                JLog.i(TAG, "procInfo.processName=" + procInfo.processName + "--procInfo.pid=" + procInfo.pid);
            }
            if (isMainProcess) {
                if (proceeName.equals(procInfo.processName)) {//kill进程
                    forceStopProgress(procInfo.pid);
                    return;
                } else {
                    continue;
                }
            }
            if (proceeName.equals(procInfo.processName)) {//先kill后台进程
                mainPid = procInfo.pid;
                continue;
            }
            if (remoteProceeName.equals(procInfo.processName)) {//先kill后台进程
                if (JLog.isDebug) {
                    JLog.i(TAG, "kill远程进程");
                }
                forceStopProgress(procInfo.pid);
            }
        }
        if (JLog.isDebug) {
            JLog.i(TAG, "kill主进程");
        }
        forceStopProgress(mainPid);
    }

    /**
     * app是否被激活（是否使用过流量）
     *
     * @param packageName 包名
     * @return 是否被激活
     */
    public static boolean isAppActivated(String packageName) {
        PackageManager pm = BaseApplication.curContext.getPackageManager();
        try {
            PackageInfo info = pm.getPackageInfo(packageName, PackageManager.GET_UNINSTALLED_PACKAGES);
            //获取每个应用程序在操作系统内的进程id
            int localUid = info.applicationInfo.uid;

            File dir = new File("/proc/uid_stat/");
            String[] children = dir.list();
            if (children == null) return false;
            StringBuffer stringBuffer = new StringBuffer();
            if (JLog.isDebug) {
                JLog.i(TAG, "isAppActivated children.length=" + children.length);
            }
            for (int i = 0; i < children.length; i++) {
                stringBuffer.append(children[i]);
                stringBuffer.append("   ");
            }
            if (!Arrays.asList(children).contains(String.valueOf(localUid))) {
                return false;
            }
            File uidFileDir = new File("/proc/uid_stat/" + String.valueOf(localUid));
            File uidActualFileReceived = new File(uidFileDir, "tcp_rcv");
            File uidActualFileSent = new File(uidFileDir, "tcp_snd");
            String textReceived = "0";
            String textSent = "0";
            BufferedReader brReceived = new BufferedReader(new FileReader(uidActualFileReceived));
            BufferedReader brSent = new BufferedReader(new FileReader(uidActualFileSent));
            String receivedLine;
            String sentLine;

            if ((receivedLine = brReceived.readLine()) != null) {
                textReceived = receivedLine;
            }
            if ((sentLine = brSent.readLine()) != null) {
                textSent = sentLine;
            }
            if (JLog.isDebug) {
                JLog.i(TAG, "isAppActivated rx=" + textReceived + "--tx=" + textSent + "--packageName=" + packageName + "--localUid=" + localUid);
            }
            return (Long.valueOf(textReceived) + Long.valueOf(textSent)) > 0;

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }


//    /**
//     * 跳转市场详情界面
//     *
//     * @param context Context 上下文
//     * @param appId   应用id
//     */
//    private void startInstallFromAppcenter(Context context, String appId) {
//        Intent intent = new Intent();
//        ComponentName cn = new ComponentName("com.prize.appcenter", "com.prize.appcenter.activity.AppDetailActivity");
//        intent.setComponent(cn);
//        Bundle b = new Bundle();
//        b.putString("appid", appId);
//        intent.putExtra("bundle", b);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        context.startActivity(intent);
//    }


    /**
     * 过滤掉360已安装的应用
     */
    public static ArrayList<AppsItemBean> filter360Data(List<AppsItemBean> listData) {
        ArrayList<AppsItemBean> listFilter = new ArrayList<AppsItemBean>();
        for (int i = 0; i < listData.size(); i++) {
            AppsItemBean item = listData.get(i);
            if (!TextUtils.isEmpty(item.packageName) && !TextUtils.isEmpty(item.backParams) && (item.adType == 1) && AppManagerCenter.isAppExist(item.packageName))
                continue;
            listFilter.add(item);
        }
        return listFilter;
    }

    /**
     * 过滤掉已安装的应用，从列表中移除, 过滤完如果数目大于6，保留前6个
     */
    public static ArrayList<AppsItemBean> filterSearchMatch(
            ArrayList<AppsItemBean> listData, AppsItemBean bean) {
        ArrayList<AppsItemBean> targetList = new ArrayList<>();
        if (bean != null) {
            targetList.add(bean);
        }
        if (listData == null) return targetList;
        AppsItemBean item;
        for (int i = 0; i < listData.size(); i++) {
            item = listData.get(i);
            if (!TextUtils.isEmpty(item.packageName) && !AppManagerCenter.isAppExist(item.packageName)) {
                item.isAdvertise = true;
                targetList.add(item);
                return targetList;
            }
        }
        return targetList;
    }


    /**
     * 判断是否打开了通知监听权限
     *
     * @param context Context
     * @return boolean
     */
    public static boolean isEnabled(Context context) {
        String pkgName = context.getPackageName();
        final String flat = Settings.Secure.getString(context.getContentResolver(), "enabled_notification_listeners");
        if (JLog.isDebug) {
            JLog.i(TAG, "flat=" + flat);
        }
        if (!TextUtils.isEmpty(flat)) {
            final String[] names = flat.split(":");
            for (int i = 0; i < names.length; i++) {
                final ComponentName cn = ComponentName.unflattenFromString(names[i]);
                if (cn != null) {
                    if (JLog.isDebug) {
                        JLog.i(TAG, "cn=" + cn + "--names[i]=" + names[i] + "--cn.getPackageName()=" + cn.getPackageName());
                    }
                    if (TextUtils.equals(pkgName, cn.getPackageName())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static void saveEnabledServices(Context context, HashSet<ComponentName> mEnabledServices) {
        mEnabledServices.add(new ComponentName(context.getPackageName(), "com.prize.appcenter.service.NotificationMonitorService"));
        StringBuilder sb = null;
        for (ComponentName cn : mEnabledServices) {
            if (sb == null) {
                sb = new StringBuilder();
            } else {
                sb.append(':');
            }
            sb.append(cn.flattenToString());
        }
        Settings.Secure.putString(context.getContentResolver(), Settings.Secure.ENABLED_NOTIFICATION_LISTENERS,
                sb != null ? sb.toString() : "");
    }

    private static void writePushPermissionSetting(Context context) {
        if (!CommonUtils.isGetPermission(context, "android.permission.WRITE_SECURE_SETTINGS"))
            return;
        HashSet<ComponentName> mEnabledServices = new HashSet<ComponentName>();
        mEnabledServices.clear();
        final String flat = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ENABLED_NOTIFICATION_LISTENERS);
//        final String flat = Settings.Secure.getString(context.getContentResolver(),"enabled_notification_listeners");
        if (flat != null && !"".equals(flat)) {
            final String[] names = flat.split(":");
            for (int i = 0; i < names.length; i++) {
                final ComponentName cn = ComponentName.unflattenFromString(names[i]);
                if (cn != null) {
                    mEnabledServices.add(cn);
                }
            }
        }
        saveEnabledServices(context, mEnabledServices);
    }

    /**
     * 判断市场是否具有通知权限，没有的话 强制写入
     */
    public static void writePushPermissionSetting() {
        if (ClientInfo.getInstance().androidVerCode >= 23) return;
        if (isEnabled(BaseApplication.curContext)) return;
        writePushPermissionSetting(BaseApplication.curContext);
    }

    public static boolean isGetPermission(Context context, String permName) {
        PackageManager pm = context.getPackageManager();
        return (PackageManager.PERMISSION_GRANTED == pm.checkPermission(permName, context.getPackageName()));
    }

//    private static final String XOR_KEY = "ddsad5464kior4423dsa";
//
//    /**
//     * data异或
//     * @param bytes byte[]
//     * @return byte[]
//     */
//    public static byte[] encrypt(byte[] bytes) {
//        if (bytes == null) {
//            return null;
//        }
//        int len = bytes.length;
//        int keyLen = XOR_KEY.length();
//
//        for (int i = 0; i < len; i++) {
//            bytes[i] ^= XOR_KEY.charAt(i%keyLen);
//        }
//        return bytes;
//    }

    public static String getNewTid() {
        Uri uri = Uri.parse("content://com.prize.globaldata.provider.GlobalDataProvider");
//        ContentValues mContentValues = new ContentValues();
//        BaseApplication.curContext.getContentResolver().insert(uri, mContentValues);
        Cursor mCursor = BaseApplication.curContext.getContentResolver().query(uri,
                new String[]{"tid", "describe"}, null, null, null);
        if (mCursor != null) {
            mCursor.moveToNext();
            String tid = mCursor.getString(0);
            if (JLog.isDebug) {
                JLog.i(TAG, "getNewTid-tid=" + tid);
            }
            mCursor.close();
            return tid;
        }
        return null;
    }

}
