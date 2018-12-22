package com.cundong.utils;

/**
 * Created by Administrator on 2016/8/12.
 */
public class PatchUtils {
    /**
     * native方法 使用路径为oldApkPath的apk与路径为patchPath的补丁包，合成新的apk，并存储于newApkPath
     *
     * 返回：0，说明操作成功
     *
     * @param oldApkPath 示例:/sdcard/old.apk
     * @param newApkPath 示例:/sdcard/new.apk
     * @param patchPath  示例:/sdcard/xx.patch
     * @return int
     */
    public static native int patch(String oldApkPath, String newApkPath, String patchPath);
}
