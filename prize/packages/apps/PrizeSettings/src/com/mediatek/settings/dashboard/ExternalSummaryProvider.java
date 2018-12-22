package com.mediatek.settings.dashboard;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import com.android.settings.dashboard.SummaryLoader;
import com.android.settings.dashboard.SummaryLoader.SummaryProviderFactory;
import com.android.settingslib.drawer.Tile;

import dalvik.system.PathClassLoader;

import java.lang.reflect.Field;
import java.util.HashMap;

public class ExternalSummaryProvider {

    private static final String TAG = ExternalSummaryProvider.class.getSimpleName();
    public static final String META_DATA_KEY_EXTERNAL_SUMMARY =
        "com.mediatek.settings.summary";
    private static HashMap<String, PathClassLoader> sLoaderMap = new HashMap<>();

    public static SummaryLoader.SummaryProvider createExternalSummaryProvider(
            Activity activity, SummaryLoader summaryLoader, Tile tile) {
        Bundle metaData = tile.metaData;
        Log.d(TAG, "createExternalSummaryProvider for: " + tile.intent.getComponent());
        if (metaData == null) {
            Log.d(TAG, "No metadata specified for " + tile.intent.getComponent());
            return null;
        }
        String clsName = metaData.getString(META_DATA_KEY_EXTERNAL_SUMMARY);
        if (clsName == null) {
            Log.d(TAG, "No summary provider specified for " + tile.intent.getComponent());
            return null;
        }
        ComponentName cn = tile.intent.getComponent();
        String pkgName = cn.getPackageName();
        try {
            PathClassLoader newLoader = sLoaderMap.get(pkgName);
            if (newLoader == null) {
                String sourceDir = activity.getPackageManager().getApplicationInfo(
                        pkgName, 0).sourceDir;
                Log.d(TAG, "clsName: " + clsName + " sourceDir: " + sourceDir);
                newLoader = new PathClassLoader(sourceDir, activity.getClassLoader());
                sLoaderMap.put(pkgName, newLoader);
            }
            Class<?> cls = newLoader.loadClass(clsName);
            Field field = cls.getField(SummaryLoader.SUMMARY_PROVIDER_FACTORY);
            SummaryProviderFactory factory = (SummaryProviderFactory) field.get(null);
            Context desContext = activity;
            desContext = activity.createPackageContext(cn.getPackageName(),
                    Context.CONTEXT_INCLUDE_CODE
                            | Context.CONTEXT_IGNORE_SECURITY);
            return factory.createSummaryProvider(activity, summaryLoader);
        } catch (PackageManager.NameNotFoundException e) {
            Log.d(TAG, "Couldn't find package " + cn.getPackageName(), e);
        } catch (ClassNotFoundException e) {
            Log.d(TAG, "Couldn't find " + clsName, e);
        } catch (NoSuchFieldException e) {
            Log.d(TAG, "Couldn't find " + SummaryLoader.SUMMARY_PROVIDER_FACTORY, e);
        } catch (ClassCastException e) {
            Log.d(TAG, "Couldn't cast " + SummaryLoader.SUMMARY_PROVIDER_FACTORY, e);
        } catch (IllegalAccessException e) {
            Log.d(TAG, "Couldn't get " + SummaryLoader.SUMMARY_PROVIDER_FACTORY, e);
        }
        return null;
    }
}
