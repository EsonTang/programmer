package com.prize.left.page.util;

import java.io.File;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.IPackageDeleteObserver;
import android.content.pm.IPackageInstallObserver;
import android.content.pm.IPackageMoveObserver;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.RemoteException;
import android.util.Log;

public class ApkOperateManager {
	public static String TAG = "ApkOperateManager";

	/**
	 * 静默安装
	 * */
	public static void installApkDefaul(Context context, String fileName,
			String packageName) {
		File file = new File(fileName);
		int installFlags = 0;
		if (!file.exists())
			return;
		installFlags |= PackageManager.INSTALL_REPLACE_EXISTING;
		PackageManager pm = context.getPackageManager();
		try {
			IPackageInstallObserver observer = new MyPakcageInstallObserver();
			pm.installPackage(Uri.fromFile(file), observer, installFlags,
					packageName);
		} catch (Exception e) {

		}

	}

	
	/**
     * 静默安装
     * */
    public static void autoInstallApk(Context context, String fileName,
            String packageName, String APPName) {
        Log.d(TAG, "jing mo an zhuang:" + packageName + ",fileName:" + fileName);
        File file = new File(fileName);
        int installFlags = 0;
        if (!file.exists())
            return;
        installFlags |= PackageManager.INSTALL_REPLACE_EXISTING;
        PackageManager pm = context.getPackageManager();
        try {
            IPackageInstallObserver observer = new MyPakcageInstallObserver();
            Log.i(TAG, "########installFlags:" + installFlags+"packagename:"+packageName);
            pm.installPackage(Uri.fromFile(file), observer, installFlags,
                    packageName);
        } catch (Exception e) {
             
        }
 
    }
	
	/* 静默安装回调 */
	private static class MyPakcageInstallObserver extends
			IPackageInstallObserver.Stub {

		public MyPakcageInstallObserver() {
		}

		@Override
		public void packageInstalled(String packageName, int returnCode) {
		}
	}
}