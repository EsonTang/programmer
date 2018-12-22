package com.prize.qihoo.cleandroid.sdk;


public class SDKEnv {

    public static final boolean DEBUG = false;// BuildConfig.DEBUG;

    /**
     * 清理SDK 版本号
     */
    public static final String CLEAR_SDK_VERSION_NAME = "4.7.2.1013";

    /**
     * 国际化标志，海外版本请将变量设置为true
     */
    public static boolean sIsMultilang;

    /**
     * 清理SDK 更新权限，由于Android5.0 版本中新增特性，不允许签名不同的应用使用申请同名Permission
     * 请务必修改后缀名，并与AndroidManifest.xml 中保持一直 <br>
     * <permission
     * android:name="com.qihoo.antivirus.update.permission.clear_sdk" />
     * <uses-permission
     * android:name="com.qihoo.antivirus.update.permission.clear_sdk" /> <br>
     * <br>
     * 修改样例 UPDATE_PERMISSION =
     * "com.qihoo.antivirus.update.permission.clear_sdk_厂商名"
     */
    public static final String UPDATE_PERMISSION = "com.qihoo.antivirus.update.permission.clear_sdk_7000098";
}
