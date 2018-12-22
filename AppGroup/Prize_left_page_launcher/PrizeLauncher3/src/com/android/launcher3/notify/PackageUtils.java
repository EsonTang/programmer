package com.android.launcher3.notify;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import android.content.Context;
import android.content.res.Resources;

public class PackageUtils {
	 /**
     * 获取可读取Apk文件资源的对象,主要用于读取apk中的icon、Label
     * 
     * @param context
     * @param apkPath
     * @return
     */
    @SuppressWarnings("rawtypes")
    public static Resources createApkResources(Context context, String apkPath) {
        // Resources res;
        // Resources pRes = context.getResources();
        // AssetManager assmgr = new AssetManager();
        // assmgr.addAssetPath(apkPath);
        // res = new Resources(assmgr, pRes.getDisplayMetrics(),
        // pRes.getConfiguration());
        // return res;

        String PATH_AssetManager = "android.content.res.AssetManager";
        try {
            Class assetMagCls = Class.forName(PATH_AssetManager);
            Constructor assetMagCt = assetMagCls.getConstructor((Class[]) null);
            Object assetMag = assetMagCt.newInstance((Object[]) null);
            Class[] typeArgs = new Class[1];
            typeArgs[0] = String.class;
            Method assetMag_addAssetPathMtd = assetMagCls.getDeclaredMethod(
                    "addAssetPath", typeArgs);

            Object[] valueArgs = new Object[1];
            valueArgs[0] = apkPath;
            assetMag_addAssetPathMtd.invoke(assetMag, valueArgs);
            Resources res = context.getResources();

            typeArgs = new Class[3];
            typeArgs[0] = assetMag.getClass();
            typeArgs[1] = res.getDisplayMetrics().getClass();
            typeArgs[2] = res.getConfiguration().getClass();
            Constructor resCt = Resources.class.getConstructor(typeArgs);

            valueArgs = new Object[3];
            valueArgs[0] = assetMag;
            valueArgs[1] = res.getDisplayMetrics();
            valueArgs[2] = res.getConfiguration();
            res = (Resources) resCt.newInstance(valueArgs);
            return res;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
