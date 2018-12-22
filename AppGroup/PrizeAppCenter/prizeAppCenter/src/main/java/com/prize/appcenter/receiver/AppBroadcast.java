package com.prize.appcenter.receiver;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;

import com.prize.app.BaseApplication;
import com.prize.app.constants.Constants;
import com.prize.app.database.InstalledAppTable;
import com.prize.app.database.PrizeDatabaseHelper;
import com.prize.app.download.AppManagerCenter;
import com.prize.app.net.datasource.base.AppsItemBean;
import com.prize.app.util.DataStoreUtils;
import com.prize.app.util.MTAUtil;
import com.prize.app.util.PrizeStatUtil;
import com.prize.appcenter.service.PrizeAppCenterService;
import com.prize.appcenter.ui.util.AIDLUtils;
import com.prize.appcenter.ui.util.UpdateDataUtils;

import java.io.File;


/**
 * 类描述：监听app安装和卸载的广播
 *
 * @author huanglingjun
 * @version 版本
 */
public class AppBroadcast extends BroadcastReceiver {

    private String TAG = "AppBroadcast";

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent==null||intent.getData()==null)return;
        String data = intent.getData().toString();
        String packageName = data.substring(data.indexOf(":") + 1).trim();
        Log.i(TAG, "packageName=" + packageName + "--intent.getAction()=" + intent.getAction());
        if (packageName.equals(context.getPackageName())) {
            if (Intent.ACTION_PACKAGE_REPLACED.equals(intent.getAction())) {
                delAppCenterApk();
            }
            return;
        }
//        isNeedUpload(context);
        int versioncode = 0;
        if (Intent.ACTION_PACKAGE_ADDED.equals(intent.getAction())) {
            String installType = AIDLUtils.getisUpdate_install(packageName);
            Log.i(TAG, "onReceive-installType=" + installType + "---packageName=" + packageName);
            try {
                versioncode = AppManagerCenter.getAppVersionCode(packageName,
                        context);
            } catch (Exception e) {
                e.printStackTrace();
            }
            ContentValues value = new ContentValues();
            value.put(InstalledAppTable.PKG_NAME, packageName);
            value.put(InstalledAppTable.VERSION_CODE, versioncode);
            AIDLUtils.updateInstalledTable(value);
            installedGame(packageName, context, Intent.ACTION_PACKAGE_ADDED,
                    installType);

        } else if (Intent.ACTION_PACKAGE_REMOVED.equals(intent.getAction())) {
            try {
                PackageInfo p = context.getPackageManager().getPackageInfo(

                        packageName, 0);
                if (p != null
                        && (p.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {// 系统app
                    versioncode = AppManagerCenter.getAppVersionCode(
                            packageName, context);
                    ContentValues value = new ContentValues();
                    value.put(InstalledAppTable.PKG_NAME, packageName);
                    value.put(InstalledAppTable.VERSION_CODE, versioncode);
                    AIDLUtils.updateInstalledTable(value);
                } else {
                    StringBuilder builder = new StringBuilder(
                            InstalledAppTable.PKG_NAME).append("=?");
                    PrizeDatabaseHelper.delete(InstalledAppTable.TABLE_NAME,
                            builder.toString(), new String[]{packageName});
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (!TextUtils.isEmpty(packageName)) {
                    StringBuilder builder = new StringBuilder(
                            InstalledAppTable.PKG_NAME).append("=?");
                    PrizeDatabaseHelper.delete(InstalledAppTable.TABLE_NAME,
                            builder.toString(), new String[]{packageName});
                    AIDLUtils.deleteSingle(packageName);
                    AIDLUtils.removeTask(packageName);
                    UpdateDataUtils.getUpdateInstance().removeInstalledApk(
                            BaseApplication.curContext, packageName);
                }
            }
            installedGame(packageName, context, Intent.ACTION_PACKAGE_REMOVED,
                    null);
        } else if (Intent.ACTION_PACKAGE_REPLACED.equals(intent.getAction())) {
            try {
                versioncode = AppManagerCenter.getAppVersionCode(packageName,
                        context);
            } catch (Exception e) {
                e.printStackTrace();
            }
            ContentValues value = new ContentValues();
            value.put(InstalledAppTable.PKG_NAME, packageName);
            value.put(InstalledAppTable.VERSION_CODE, versioncode);
            AIDLUtils.updateInstalledTable(value);
            installedGame(packageName, context, Intent.ACTION_PACKAGE_REPLACED,
                    null);

        }

    }

    /**
     * 安装完成的处理
     *
     * @param packageName 包名
     * @param context     Context
     * @param action      包名
     * @param installType 包名
     */
    private void installedGame(String packageName, Context context,
                               String action, String installType) {
        AppsItemBean game = AIDLUtils.getDownloadGameByPkgname(packageName);
        Intent intent = new Intent(context, PrizeAppCenterService.class);
        intent.putExtra(PrizeAppCenterService.OPT_TYPE, 3);
        if (!TextUtils.isEmpty(installType)) {
            if (installType.equals("download_install") && game != null) {
                MTAUtil.onInstallSucess(game.name, packageName);
                String backParam = AIDLUtils.getBackParam(packageName);
                if (!TextUtils.isEmpty(backParam)) {
                    PrizeStatUtil.onBackParamsAppInstalled(backParam);
                }
            }

        }
        if (Intent.ACTION_PACKAGE_ADDED.equals(action)) {
            AIDLUtils.deletePushData(packageName);
        }

        if (game != null && !Intent.ACTION_PACKAGE_REMOVED.equals(action)) {
            AIDLUtils.installedGame(packageName);
            if (BaseApplication.isThird) {
                String valDeletePkg = DataStoreUtils
                        .readLocalInfo(DataStoreUtils.AUTO_DEL_PKG);
                if (DataStoreUtils.CHECK_ON.equals(valDeletePkg)) {
                    // 删除安装包
                    UpdateDataUtils.getUpdateInstance().removeInstalledApk(
                            context, packageName);
                }
            } else {
                UpdateDataUtils.getUpdateInstance().removeInstalledApk(context,
                        packageName);
            }
            MTAUtil.onDownloadSuccess(game.name, packageName);
        }
        if (intent != null) {
            context.startService(intent);
        }
    }

    /**
     * 删除自升级后的apk
     */
    private void delAppCenterApk() {
        if (BaseApplication.curContext == null)
            return;


//        File oldFile = new File(Constants.APK_OlD_PATH);
//        if (oldFile.exists()) {
//            oldFile.delete();
//            BaseApplication.curContext.getContentResolver().delete(
//                    MediaStore.Files.getContentUri("external"),
//                    "_DATA=?",
//                    new String[]{Constants.APK_OlD_PATH});
//        }
        File file = new File(Constants.APKFILEPATH);
        if (!file.exists()) {
            return;
        }
        PackageManager pm = BaseApplication.curContext.getPackageManager();
        if (pm == null)
            return;
        PackageInfo pathPackageInfo = pm.getPackageArchiveInfo(Constants.APKFILEPATH,
                PackageManager.GET_ACTIVITIES);
        if (pathPackageInfo != null && !TextUtils.isEmpty(pathPackageInfo.packageName)) {
            try {
                PackageInfo appPackageInfo = pm.getPackageInfo(BaseApplication.curContext.getPackageName(), 0);
                if (appPackageInfo != null && BaseApplication.curContext.getPackageName().equals(pathPackageInfo.packageName) && appPackageInfo.versionCode >= pathPackageInfo.versionCode) {
                    file.delete();
                    BaseApplication.curContext.getContentResolver().delete(
                            MediaStore.Files.getContentUri("external"),
                            "_DATA=?",
                            new String[]{Constants.APKFILEPATH});

                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
