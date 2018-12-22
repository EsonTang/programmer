package com.prize.app.util;

import android.app.PackageInstallObserver;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageDeleteObserver;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

import com.prize.app.util.ShellUtils.CommandResult;

import java.io.File;

/**
 * PackageUtils
 * <ul>
 * <strong>Install package</strong>
 * <li>{@link PackageUtils#installNormal(Context, String)}</li>
 * </ul>
 * <ul>
 * <strong>Uninstall package</strong>
 * <li>{@link PackageUtils#uninstallNormal(Context, String)}</li>
 * <li>{@link PackageUtils#uninstallSilent(Context, String)}</li>
 * </ul>
 * <ul>
 * <strong>Is system application</strong>
 * <li>{@link PackageUtils#isSystemApplication(Context)}</li>
 * <li>{@link PackageUtils#isSystemApplication(Context, String)}</li>
 * <li>{@link PackageUtils#isSystemApplication(PackageManager, String)}</li>
 * </ul>
 *
 * @author prize
 */
public class PackageUtils {

    public static final String TAG = "PackageUtils";
//    public static boolean isRoot = false;
//    public static final String PREFIX = "com.android.packageinstaller.";
//    public static final String INTENT_ATTR_APPLICATION_INFO = PREFIX
//            + "applicationInfo";
//    static final String EXTRA_INSTALL_FLOW_ANALYTICS = "com.android.packageinstaller.extras.install_flow_analytics";
//
//    /**
//     * 获取签名HASHCODE
//     *
//     * @param ctx
//     * @param gamePkgName
//     * @return
//     */
//    public static int getPackageSignatureHashcode(Context ctx,
//                                                  String gamePkgName) {
//        int signatureHashCode = 0;
//        PackageInfo pkgInfo;
//        PackageManager pManager = ctx.getPackageManager();
//
//        try {
//            pkgInfo = pManager.getPackageInfo(gamePkgName,
//                    PackageManager.GET_SIGNATURES);
//            signatureHashCode = pkgInfo.signatures[0].hashCode();
//        } catch (NameNotFoundException e) {
//            e.printStackTrace();
//        }
//        return signatureHashCode;
//    }

    /**
     * @param context  Context
     * @param filePath 文件位置
     */
    public static synchronized void installSys(Context context,
                                               String filePath, InstallResultCallBack callBack, String intallPkg) {
        installSilentFRSys(context, filePath, intallPkg, callBack);
    }

    /*
     * install according conditions
     * <ul>
     * <li>if system application or rooted, see
     * {@link #installSilent(Context, String)}</li>
     * <li>else see {@link #installNormal(Context, String)}</li>
     * </ul>
     *
     * @param context
     * @param filePath
     * @return
     */
//    public static final synchronized int install(Context context,
//                                                 String filePath) {
//        return installSilent(context, filePath);
//    }

    /**
     * install package normal by system intent
     *
     * @param context  Context
     * @param filePath file path of package
     * @return whether apk exist
     */
    public static synchronized boolean installNormal(Context context,
                                                     String filePath) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        File file = new File(filePath);
        JLog.i(TAG, "filePath=" + filePath);
        if (file != null && file.length() > 0 && file.exists() && file.isFile()) {
            i.setDataAndType(Uri.parse("file://" + filePath), "application/vnd.android.package-archive");
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            try {
                context.startActivity(i);
            } catch (ActivityNotFoundException e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }
        return false;
    }

    /*
     * install package silent by root
     * <ul>
     * <strong>Attentions：</strong>
     * <li>Don't call this on the ui thread, it costs some times.</li>
     * <li>You should add <strong>android.permission.INSTALL_PACKAGES</strong>
     * in manifest, so no need to request root permission, if you are system
     * app.</li>
     * </ul>
     *
     * @param context  file path of package
     * @param filePath file path of package
     * @return {@link PackageUtils#INSTALL_SUCCEEDED} means install success,
     * other means failed. details see
     * {@link PackageUtils#INSTALL_FAILED}
     */
//    private static synchronized int installSilent(Context context,
//                                                  String filePath) {
//        if (filePath == null || filePath.length() == 0) {
//            return INSTALL_FAILED_INVALID_URI;
//        }
//        File file = new File(filePath);
//        if (file == null || file.length() <= 0 || !file.exists()
//                || !file.isFile()) {
//            return INSTALL_FAILED_INVALID_URI;
//        }
//
//
//        /**
//         * if context is system app, don,t need root permission, but should add
//         * <uses-permission android:name="android.permission.INSTALL_PACKAGES"
//         * /> in mainfest
//         **/
//        StringBuilder command = new StringBuilder().append("pm install -r ")
//                .append(filePath.replace(" ", "\\ "));
//        Log.i(TAG, "command.toString()=" + command.toString());
//        CommandResult commandResult = ShellUtils.execCommand(
//                command.toString(), !isSystemApplication(context), true);
//        if (commandResult.successMsg != null
//                && (commandResult.successMsg.contains("Success") || commandResult.successMsg
//                .contains("success"))) {
//            return INSTALL_SUCCEEDED;
//        } else {
//            Log.e(TAG,
//                    new StringBuilder().append("successMsg:")
//                            .append(commandResult.successMsg)
//                            .append(", ErrorMsg:")
//                            .append(commandResult.errorMsg).toString());
//            Log.e(TAG, "retry execCommand!");
//            commandResult = ShellUtils.execCommand(command.toString(),
//                    isSystemApplication(context), true);
//            if (commandResult.successMsg != null
//                    && (commandResult.successMsg.contains("Success") || commandResult.successMsg
//                    .contains("success"))) {
//                return INSTALL_SUCCEEDED;
//            }
//        }
//
//        Log.e(TAG,
//                new StringBuilder().append("successMsg:")
//                        .append(commandResult.successMsg).append(", ErrorMsg:")
//                        .append(commandResult.errorMsg).toString()
//                        + "---->>filePath=" + filePath);
//        if (commandResult.errorMsg != null) {
//            if (commandResult.errorMsg
//                    .contains("INSTALL_FAILED_ALREADY_EXISTS")) {
//                return INSTALL_FAILED_ALREADY_EXISTS;
//            } else if (commandResult.errorMsg
//                    .contains("INSTALL_FAILED_INVALID_APK")) {
//                return INSTALL_FAILED_INVALID_APK;
//            } else if (commandResult.errorMsg
//                    .contains("INSTALL_FAILED_INVALID_URI")) {
//                return INSTALL_FAILED_INVALID_URI;
//            } else if (commandResult.errorMsg
//                    .contains("INSTALL_FAILED_INSUFFICIENT_STORAGE")) {
//                return INSTALL_FAILED_INSUFFICIENT_STORAGE;
//            } else if (commandResult.errorMsg
//                    .contains("INSTALL_FAILED_DUPLICATE_PACKAGE")) {
//                return INSTALL_FAILED_DUPLICATE_PACKAGE;
//            } else if (commandResult.errorMsg
//                    .contains("INSTALL_FAILED_NO_SHARED_USER")) {
//                return INSTALL_FAILED_NO_SHARED_USER;
//            } else if (commandResult.errorMsg
//                    .contains("INSTALL_FAILED_UPDATE_INCOMPATIBLE")) {
//                return INSTALL_FAILED_UPDATE_INCOMPATIBLE;
//            } else if (commandResult.errorMsg
//                    .contains("INSTALL_FAILED_SHARED_USER_INCOMPATIBLE")) {
//                return INSTALL_FAILED_SHARED_USER_INCOMPATIBLE;
//            } else if (commandResult.errorMsg
//                    .contains("INSTALL_FAILED_MISSING_SHARED_LIBRARY")) {
//                return INSTALL_FAILED_MISSING_SHARED_LIBRARY;
//            } else if (commandResult.errorMsg
//                    .contains("INSTALL_FAILED_REPLACE_COULDNT_DELETE")) {
//                return INSTALL_FAILED_REPLACE_COULDNT_DELETE;
//            } else if (commandResult.errorMsg.contains("INSTALL_FAILED_DEXOPT")) {
//                return INSTALL_FAILED_DEXOPT;
//            } else if (commandResult.errorMsg
//                    .contains("INSTALL_FAILED_OLDER_SDK")) {
//                return INSTALL_FAILED_OLDER_SDK;
//            } else if (commandResult.errorMsg
//                    .contains("INSTALL_FAILED_CONFLICTING_PROVIDER")) {
//                return INSTALL_FAILED_CONFLICTING_PROVIDER;
//            } else if (commandResult.errorMsg
//                    .contains("INSTALL_FAILED_NEWER_SDK")) {
//                return INSTALL_FAILED_NEWER_SDK;
//            } else if (commandResult.errorMsg
//                    .contains("INSTALL_FAILED_TEST_ONLY")) {
//                return INSTALL_FAILED_TEST_ONLY;
//            } else if (commandResult.errorMsg
//                    .contains("INSTALL_FAILED_CPU_ABI_INCOMPATIBLE")) {
//                return INSTALL_FAILED_CPU_ABI_INCOMPATIBLE;
//            } else if (commandResult.errorMsg
//                    .contains("INSTALL_FAILED_MISSING_FEATURE")) {
//                return INSTALL_FAILED_MISSING_FEATURE;
//            } else if (commandResult.errorMsg
//                    .contains("INSTALL_FAILED_CONTAINER_ERROR")) {
//                return INSTALL_FAILED_CONTAINER_ERROR;
//            } else if (commandResult.errorMsg
//                    .contains("INSTALL_FAILED_INVALID_INSTALL_LOCATION")) {
//                return INSTALL_FAILED_INVALID_INSTALL_LOCATION;
//            } else if (commandResult.errorMsg
//                    .contains("INSTALL_FAILED_MEDIA_UNAVAILABLE")) {
//                return INSTALL_FAILED_MEDIA_UNAVAILABLE;
//            } else if (commandResult.errorMsg
//                    .contains("INSTALL_FAILED_VERIFICATION_TIMEOUT")) {
//                return INSTALL_FAILED_VERIFICATION_TIMEOUT;
//            } else if (commandResult.errorMsg
//                    .contains("INSTALL_FAILED_VERIFICATION_FAILURE")) {
//                return INSTALL_FAILED_VERIFICATION_FAILURE;
//            } else if (commandResult.errorMsg
//                    .contains("INSTALL_FAILED_PACKAGE_CHANGED")) {
//                return INSTALL_FAILED_PACKAGE_CHANGED;
//            } else if (commandResult.errorMsg
//                    .contains("INSTALL_FAILED_UID_CHANGED")) {
//                return INSTALL_FAILED_UID_CHANGED;
//            } else if (commandResult.errorMsg.contains("Segmentation fault")) {
//                return INSTALL_FAILED_SEGMENTATION_FAULT;
//            }
//        }
//        return INSTALL_FAILED_OTHER;
//    }

//    /**
//     * uninstall according conditions
//     * <ul>
//     * <li>if system application or rooted, see
//     * {@link #uninstallSilent(Context, String)}</li>
//     * <li>else see {@link #uninstallNormal(Context, String)}</li>
//     * </ul>
//     *
//     * @param context
//     * @param packageName package name of app
//     * @return
//     */
//    public static final synchronized int uninstall(Context context,
//                                                   String packageName) {
//        int result = 0;
//        if (!isRoot) {
//            isRoot = ShellUtils.checkRootPermission();
//        }
//
//        if (PackageUtils.isSystemApplication(context) || isRoot) {
//            result = uninstallSilent(context, packageName);
//        }
//        if (DELETE_SUCCEEDED == result) {
//            return DELETE_SUCCEEDED;
//        }
//        // 删除失败，调用默认卸载
//        return uninstallNormal(context, packageName) ? DELETE_SUCCEEDED
//                : DELETE_FAILED_INVALID_PACKAGE;
//    }

    /*
     * uninstall package normal by system intent
     *
     * @param context     Context
     * @param packageName package name of app
     * @return whether package name is empty
     */
    public static synchronized boolean uninstallNormal(Context context,
                                                       String packageName) {
        if (packageName == null || packageName.length() == 0) {
            return false;
        }
        try {
            Intent i = new Intent(Intent.ACTION_DELETE,
                    Uri.parse(new StringBuilder(64).append("package:")
                            .append(packageName).toString()));
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
            return true;
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
            return false;
        }

    }

    /**
     * uninstall package and clear data of app silent by root
     *
     * @param context     Context
     * @param packageName package name of app
     * @return int
     * @see {@link #uninstallSilent(Context, String, boolean)}
     */
    public static synchronized int uninstallSilent(Context context,
                                                   String packageName) {
        uninstallSilentFRSys(context, packageName);
        return 0;
    }

//    /**
//     * 清除app数据
//     *
//     * @param context
//     * @param packageName
//     */
//    public static void clearApplicationUserData(final Context context,
//                                                final String packageName) {
//        final ActivityManager am = (ActivityManager)
//                context.getSystemService(Context.ACTIVITY_SERVICE);
//        JLog.i(TAG, "clearApplicationUserData--packagename=" + packageName
//                + "----context=" + context + "---am=" + am);
//        if (am != null) {
//            am.clearApplicationUserData(packageName, new IPackageDataObserver.Stub() {
//                @Override
//                public void onRemoveCompleted(String packageName, boolean b) throws RemoteException {
//                    JLog.i(TAG, "clearApplicationUserData--packagename=" + packageName + "---b=" + b);
////                    uninstallSilentFRSys(context,packageName);
//                }
//            });
//        }
//    }
//    private static HashSet<String> staticInstallPkg = new HashSet<String>();

    /**
     * 调用系统底层的安装方法
     *
     * @param context        上下文
     * @param filePath       安装包路径
     * @param appPackageName 安装包包名
     * @param callBack       回调InstallResultCallBack
     */
    public static synchronized void installSilentFRSys(Context context,
                                                       String filePath, final String appPackageName,
                                                       final InstallResultCallBack callBack) {
        Log.i(TAG, "installSilentFRSys-filePath=" + filePath + "--context.getPackageName()=" + context.getPackageName());
//        staticInstallPkg.add(appPackageName);
        try {
            PackageManager pm = context.getPackageManager();
            pm.installPackage(Uri.fromFile(new File(filePath)),
                    new PackageInstallObserver() {
                        @Override
                        public void onPackageInstalled(String basePackageName,
                                                       int returnCode, String msg, Bundle extras) {
                            Log.i(TAG, "installSilentFRSys-msg=" + msg + "--basePackageName=" + basePackageName + "--extras=" + extras + "--returnCode=" + returnCode);
                            if (callBack == null)
                                return;
                            callBack.back(returnCode);
                        }
                    }, PackageManager.INSTALL_REPLACE_EXISTING, context.getPackageName());

        } catch (Exception e) {
            JLog.i(TAG, "installSilentFRSys-Exceptione=" + e + "--appPackageName=" + appPackageName + "--filePath=" + filePath);
            int errCode = INSTALL_FAILED_OTHER;
            if (e != null && !TextUtils.isEmpty(e.getMessage())) {
                String errorMsg = e.getMessage();
                if (errorMsg
                        .contains("INSTALL_FAILED_ALREADY_EXISTS")) {
                    errCode = INSTALL_FAILED_ALREADY_EXISTS;
                } else if (errorMsg
                        .contains("INSTALL_FAILED_INVALID_APK")) {
                    errCode = INSTALL_FAILED_INVALID_APK;
                } else if (errorMsg
                        .contains("INSTALL_FAILED_INVALID_URI")) {
                    errCode = INSTALL_FAILED_INVALID_URI;
                } else if (errorMsg
                        .contains("INSTALL_FAILED_INSUFFICIENT_STORAGE")) {
                    errCode = INSTALL_FAILED_INSUFFICIENT_STORAGE;
                } else if (errorMsg
                        .contains("INSTALL_FAILED_DUPLICATE_PACKAGE")) {
                    errCode = INSTALL_FAILED_DUPLICATE_PACKAGE;
                } else if (errorMsg
                        .contains("INSTALL_FAILED_NO_SHARED_USER")) {
                    errCode = INSTALL_FAILED_NO_SHARED_USER;
                } else if (errorMsg
                        .contains("INSTALL_FAILED_UPDATE_INCOMPATIBLE")) {
                    errCode = INSTALL_FAILED_UPDATE_INCOMPATIBLE;
                } else if (errorMsg
                        .contains("INSTALL_FAILED_SHARED_USER_INCOMPATIBLE")) {
                    errCode = INSTALL_FAILED_SHARED_USER_INCOMPATIBLE;
                } else if (errorMsg
                        .contains("INSTALL_FAILED_MISSING_SHARED_LIBRARY")) {
                    errCode = INSTALL_FAILED_MISSING_SHARED_LIBRARY;
                } else if (errorMsg
                        .contains("INSTALL_FAILED_REPLACE_COULDNT_DELETE")) {
                    errCode = INSTALL_FAILED_REPLACE_COULDNT_DELETE;
                } else if (errorMsg.contains("INSTALL_FAILED_DEXOPT")) {
                    errCode = INSTALL_FAILED_DEXOPT;
                } else if (errorMsg
                        .contains("INSTALL_FAILED_OLDER_SDK")) {
                    errCode = INSTALL_FAILED_OLDER_SDK;
                } else if (errorMsg
                        .contains("INSTALL_FAILED_CONFLICTING_PROVIDER")) {
                    errCode = INSTALL_FAILED_CONFLICTING_PROVIDER;
                } else if (errorMsg
                        .contains("INSTALL_FAILED_NEWER_SDK")) {
                    errCode = INSTALL_FAILED_NEWER_SDK;
                } else if (errorMsg
                        .contains("INSTALL_FAILED_TEST_ONLY")) {
                    errCode = INSTALL_FAILED_TEST_ONLY;
                } else if (errorMsg
                        .contains("INSTALL_FAILED_CPU_ABI_INCOMPATIBLE")) {
                    errCode = INSTALL_FAILED_CPU_ABI_INCOMPATIBLE;
                } else if (errorMsg
                        .contains("INSTALL_FAILED_MISSING_FEATURE")) {
                    errCode = INSTALL_FAILED_MISSING_FEATURE;
                } else if (errorMsg
                        .contains("INSTALL_FAILED_CONTAINER_ERROR")) {
                    errCode = INSTALL_FAILED_CONTAINER_ERROR;
                } else if (errorMsg
                        .contains("INSTALL_FAILED_INVALID_INSTALL_LOCATION")) {
                    errCode = INSTALL_FAILED_INVALID_INSTALL_LOCATION;
                } else if (errorMsg
                        .contains("INSTALL_FAILED_MEDIA_UNAVAILABLE")) {
                    errCode = INSTALL_FAILED_MEDIA_UNAVAILABLE;
                } else if (errorMsg
                        .contains("INSTALL_FAILED_VERIFICATION_TIMEOUT")) {
                    errCode = INSTALL_FAILED_VERIFICATION_TIMEOUT;
                } else if (errorMsg
                        .contains("INSTALL_FAILED_VERIFICATION_FAILURE")) {
                    errCode = INSTALL_FAILED_VERIFICATION_FAILURE;
                } else if (errorMsg
                        .contains("INSTALL_FAILED_PACKAGE_CHANGED")) {
                    errCode = INSTALL_FAILED_PACKAGE_CHANGED;
                } else if (errorMsg
                        .contains("INSTALL_FAILED_UID_CHANGED")) {
                    errCode = INSTALL_FAILED_UID_CHANGED;
                } else if (errorMsg.contains("Segmentation fault")) {
                    errCode = INSTALL_FAILED_SEGMENTATION_FAULT;
                }
            }
            if (callBack == null)
                return;
            callBack.back(errCode);
        }
    }

    /***
     * 卸载（调用系统PackageManager.deletePackage方法）
     * @param context  Context
     * @param packageName  需要卸载的包名
     */
    public static synchronized void uninstallSilentFRSys(Context context,
                                                         String packageName) {
        try {
            PackageManager pm = context.getPackageManager();
            pm.deletePackage(packageName, new IPackageDeleteObserver.Stub() {

                @Override
                public void packageDeleted(String arg0, int arg1)
                        throws RemoteException {
                    JLog.i(TAG, "uninstallSilentFRSys--arg0=" + arg0
                            + "----arg1=" + arg1);

                }

            }, 0);

        } catch (Exception e) {
            e.printStackTrace();
            JLog.i(TAG, "uninstallSilentFRSys--e=" + e + "----e.getMessage()="
                    + e.getMessage());
        }
    }

    /**
     * uninstall package silent by root
     * <ul>
     * <strong>Attentions：</strong>
     * <li>Don't call this on the ui thread, it may costs some times.</li>
     * <li>You should add <strong>android.permission.DELETE_PACKAGES</strong> in
     * manifest, so no need to request root permission, if you are system app.</li>
     * </ul>
     *
     * @param context     file path of package
     * @param packageName package name of app
     * @param isKeepData  whether keep the data and cache directories around after
     *                    package removal
     * @return <ul>
     * <li>{@link #DELETE_SUCCEEDED} means uninstall success</li>
     * <li>{@link #DELETE_FAILED_INTERNAL_ERROR} means internal error</li>
     * <li>{@link #DELETE_FAILED_INVALID_PACKAGE} means package name
     * error</li>
     * <li>{@link #DELETE_FAILED_PERMISSION_DENIED} means permission
     * denied</li>
     */
    public static synchronized int uninstallSilent(Context context,
                                                   String packageName, boolean isKeepData) {
        if (packageName == null || packageName.length() == 0) {
            return DELETE_FAILED_INVALID_PACKAGE;
        }

        /*
         * if context is system app, don't need root permission, but should add
         * <uses-permission android:name="android.permission.DELETE_PACKAGES" />
         * in mainfest
         **/
        StringBuilder command = new StringBuilder().append("pm uninstall")
                // .append("LD_LIBRARY_PATH=/vendor/lib:/system/lib pm uninstall")
                .append(isKeepData ? " -k " : " ")
                .append(packageName.replace(" ", "\\ "));
        JLog.i(TAG, "uninstallSilent-command.toString()=" + command.toString());
        CommandResult commandResult = ShellUtils.execCommand(
                command.toString(), !isSystemApplication(context), true);
        if (commandResult.successMsg != null
                && (commandResult.successMsg.contains("Success") || commandResult.successMsg
                .contains("success"))) {
            return DELETE_SUCCEEDED;
        }
        JLog.e(TAG,
                new StringBuilder().append("uninstallSilent successMsg:")
                        .append(commandResult.successMsg).append(", ErrorMsg:")
                        .append(commandResult.errorMsg).toString());
        if (commandResult.errorMsg == null) {
            return DELETE_FAILED_INTERNAL_ERROR;
        }
        if (commandResult.errorMsg.contains("Permission denied")) {
            return DELETE_FAILED_PERMISSION_DENIED;
        }
        return DELETE_FAILED_INTERNAL_ERROR;
    }

    /**
     * whether context is system application
     *
     * @param context
     * @return
     */
    public static boolean isSystemApplication(Context context) {
        if (context == null) {
            return false;
        }

        return isSystemApplication(context, context.getPackageName());
    }

    /**
     * whether packageName is system application
     *
     * @param context     Context
     * @param packageName 包名
     * @return boolean
     */
    private static boolean isSystemApplication(Context context,
                                               String packageName) {
        return context != null && isSystemApplication(context.getPackageManager(), packageName);
    }
//    /**
//     *是否是在真正的安装
//     *
//     * @param packageName
//     * @return
//     */
//    public static boolean isRealInstall(String packageName) {
//        if (packageName == null) {
//            return false;
//        }
//        JLog.i("0000", "cancelInstallTask-packageName=" + packageName + "--staticInstallPkg.size()=" + staticInstallPkg.size());
//        return staticInstallPkg.contains(packageName);
//    }

    /**
     * whether packageName is system application
     *
     * @param packageManager PackageManager
     * @param packageName    包名
     * @return <ul>
     * <li>if packageManager is null, return false</li>
     * <li>if package name is null or is empty, return false</li>
     * <li>if package name not exit, return false</li>
     * <li>if package name exit, but not system app, return false</li>
     * <li>else return true</li>
     * </ul>
     */
    public static boolean isSystemApplication(PackageManager packageManager,
                                              String packageName) {
        if (packageManager == null || packageName == null
                || packageName.length() == 0) {
            return false;
        }

        try {
            ApplicationInfo app = packageManager.getApplicationInfo(
                    packageName, 0);
            return (app != null && (app.flags & ApplicationInfo.FLAG_SYSTEM) > 0);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Installation return code<br/>
     * install success.
     */
    public static final int INSTALL_SUCCEEDED = 1;
    /**
     * Installation return code<br/>
     * the package is already installed.
     */
    private static final int INSTALL_FAILED_ALREADY_EXISTS = -1;

    /**
     * Installation return code<br/>
     * the package archive file is invalid.
     */
    private static final int INSTALL_FAILED_INVALID_APK = -2;

    /**
     * Installation return code<br/>
     * the URI passed in is invalid.
     */
    private static final int INSTALL_FAILED_INVALID_URI = -3;

    /**
     * Installation return code<br/>
     * the package manager service found that the device didn't have enough
     * storage space to install the app.
     */
    private static final int INSTALL_FAILED_INSUFFICIENT_STORAGE = -4;

    /**
     * Installation return code<br/>
     * a package is already installed with the same name.
     */
    private static final int INSTALL_FAILED_DUPLICATE_PACKAGE = -5;

    /**
     * Installation return code<br/>
     * the requested shared user does not exist.
     */
    private static final int INSTALL_FAILED_NO_SHARED_USER = -6;

    /**
     * Installation return code<br/>
     * a previously installed package of the same name has a different signature
     * than the new package (and the old package's data was not removed).
     */
    private static final int INSTALL_FAILED_UPDATE_INCOMPATIBLE = -7;

    /**
     * Installation return code<br/>
     * the new package is requested a shared user which is already installed on
     * the device and does not have matching signature.
     */
    private static final int INSTALL_FAILED_SHARED_USER_INCOMPATIBLE = -8;

    /**
     * Installation return code<br/>
     * the new package uses a shared library that is not available.
     */
    public static final int INSTALL_FAILED_MISSING_SHARED_LIBRARY = -9;

    /**
     * Installation return code<br/>
     * the new package uses a shared library that is not available.
     */
    private static final int INSTALL_FAILED_REPLACE_COULDNT_DELETE = -10;

    /**
     * Installation return code<br/>
     * the new package failed while optimizing and validating its dex files,
     * either because there was not enough storage or the validation failed.
     */
    private static final int INSTALL_FAILED_DEXOPT = -11;

    /**
     * Installation return code<br/>
     * the new package failed because the current SDK version is older than that
     * required by the package.
     */
    private static final int INSTALL_FAILED_OLDER_SDK = -12;

    /**
     * Installation return code<br/>
     * the new package failed because it contains a content provider with the
     * same authority as a provider already installed in the system.
     */
    private static final int INSTALL_FAILED_CONFLICTING_PROVIDER = -13;

    /**
     * Installation return code<br/>
     * the new package failed because the current SDK version is newer than that
     * required by the package.
     */
    private static final int INSTALL_FAILED_NEWER_SDK = -14;

    /**
     * Installation return code<br/>
     * the new package failed because it has specified that it is a test-only
     * package and the caller has not supplied the {@link #}
     * flag.
     */
    private static final int INSTALL_FAILED_TEST_ONLY = -15;

    /**
     * Installation return code<br/>
     * the package being installed contains native code, but none that is
     * compatible with the the device's CPU_ABI.
     */
    private static final int INSTALL_FAILED_CPU_ABI_INCOMPATIBLE = -16;

    /**
     * Installation return code<br/>
     * the new package uses a feature that is not available.
     */
    private static final int INSTALL_FAILED_MISSING_FEATURE = -17;

    /**
     * Installation return code<br/>
     * a secure container mount point couldn't be accessed on external media.
     */
    private static final int INSTALL_FAILED_CONTAINER_ERROR = -18;

    /**
     * Installation return code<br/>
     * the new package couldn't be installed in the specified install location.
     */
    private static final int INSTALL_FAILED_INVALID_INSTALL_LOCATION = -19;

    /**
     * Installation return code<br/>
     * the new package couldn't be installed in the specified install location
     * because the media is not available.
     */
    private static final int INSTALL_FAILED_MEDIA_UNAVAILABLE = -20;

    /**
     * Installation return code<br/>
     * the new package couldn't be installed because the verification timed out.
     */
    private static final int INSTALL_FAILED_VERIFICATION_TIMEOUT = -21;

    /**
     * Installation return code<br/>
     * the new package couldn't be installed because the verification did not
     * succeed.
     */
    private static final int INSTALL_FAILED_VERIFICATION_FAILURE = -22;

    /**
     * Installation return code<br/>
     * the package changed from what the calling program expected.
     */
    private static final int INSTALL_FAILED_PACKAGE_CHANGED = -23;

    /**
     * Installation return code<br/>
     * the new package is assigned a different UID than it previously held.
     */
    private static final int INSTALL_FAILED_UID_CHANGED = -24;
    /**
     * Installation return code<br/>
     * Install faile reason:segmentation_fault
     */
    private static final int INSTALL_FAILED_SEGMENTATION_FAULT = -25;

    /**
     * Installation return code<br/>
     * other reason
     */
    private static final int INSTALL_FAILED_OTHER = -1000000;
    /**
     * Uninstall return code<br/>
     * uninstall success.
     */
    private static final int DELETE_SUCCEEDED = 1;

    /**
     * Uninstall return code<br/>
     * uninstall fail if the system failed to delete the package for an
     * unspecified reason.
     */
    private static final int DELETE_FAILED_INTERNAL_ERROR = -1;
    /**
     * Uninstall return code<br/>
     * uninstall fail if the system failed to delete the package because it is
     * the active DevicePolicy manager.
     */
    public static final int DELETE_FAILED_DEVICE_POLICY_MANAGER = -2;

    /**
     * Uninstall return code<br/>
     * uninstall fail if pcakge name is invalid
     */
    private static final int DELETE_FAILED_INVALID_PACKAGE = -3;

    /**
     * Uninstall return code<br/>
     * uninstall fail if permission denied
     */
    private static final int DELETE_FAILED_PERMISSION_DENIED = -4;

}
