/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.launcher3;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.PackageInfo;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Represents an app in AllAppsView.
 */
public class AppInfo extends ItemInfo {
    private static final String TAG = "Launcher3.AppInfo";

    /**
     * The intent used to start the application.
     */
    Intent intent;

    /**
     * A bitmap version of the application icon.
     */
    Bitmap iconBitmap;

    /**
     * The time at which the app was first installed.
     */
    long firstInstallTime;

    public ComponentName componentName;

    /**
     * M: The application icon is show or hide for CT.
     */
    public boolean isVisible = true;
    boolean stateChanged;

    static final int DOWNLOADED_FLAG = 1;
    static final int UPDATED_SYSTEM_APP_FLAG = 2;

    int flags = 0;

    AppInfo() {
        itemType = LauncherSettings.BaseLauncherColumns.ITEM_TYPE_SHORTCUT;
    }

    protected Intent getIntent() {
        return intent;
    }

    /**
     * Must not hold the Context.
     */
    public AppInfo(PackageManager pm, ResolveInfo info, IconCache iconCache,
            HashMap<Object, CharSequence> labelCache) {
        final String packageName = info.activityInfo.applicationInfo.packageName;

        this.componentName = new ComponentName(packageName, info.activityInfo.name);
        this.container = ItemInfo.NO_ID;
        this.setActivity(componentName,
                Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

        this.setFlagAndInstallTime(pm, packageName);
        iconCache.getTitleAndIcon(this, info, labelCache);
    }

    public static int initFlags(PackageInfo pi) {
        int appFlags = pi.applicationInfo.flags;
        int flags = 0;
        if ((appFlags & android.content.pm.ApplicationInfo.FLAG_SYSTEM) == 0) {
            flags |= DOWNLOADED_FLAG;

            if ((appFlags & android.content.pm.ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) {
                flags |= UPDATED_SYSTEM_APP_FLAG;
            }
        }
        return flags;
    }

    public static long initFirstInstallTime(PackageInfo pi) {
        return pi.firstInstallTime;
    }

    public AppInfo(AppInfo info) {
        super(info);
        componentName = info.componentName;
        title = info.title.toString();
        intent = new Intent(info.intent);
        flags = info.flags;
        firstInstallTime = info.firstInstallTime;

        /// M: copy value from source applcation info.
        pos = info.pos;
        isVisible = info.isVisible;
        stateChanged = info.stateChanged;
    }

    /**
     * Creates the application intent based on a component name and various launch flags.
     * Sets {@link #itemType} to {@link LauncherSettings.BaseLauncherColumns#ITEM_TYPE_APPLICATION}.
     *
     * @param className the class name of the component representing the intent
     * @param launchFlags the launch flags
     */
    final void setActivity(ComponentName className, int launchFlags) {
        intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setComponent(className);
        intent.setFlags(launchFlags);
        itemType = LauncherSettings.BaseLauncherColumns.ITEM_TYPE_APPLICATION;
    }

    @Override
    public String toString() {
        return "ApplicationInfo(title=" + title.toString() + " id=" + this.id
                + " type=" + this.itemType + " container=" + this.container
                + " screen=" + screenId + " cellX=" + cellX + " cellY=" + cellY
                + " spanX=" + spanX + " spanY=" + spanY + " dropPos=" + dropPos
                + " pos = " + pos + " isVisible = " + isVisible + ")";
    }

    public static void dumpApplicationInfoList(String tag, String label, ArrayList<AppInfo> list) {
        Log.d(tag, label + " size=" + list.size());
        for (AppInfo info: list) {
            Log.d(tag, "   title=\"" + info.title + "\" iconBitmap="
                    + info.iconBitmap + " firstInstallTime="
                    + info.firstInstallTime);
        }
    }

    public ShortcutInfo makeShortcut() {
        return new ShortcutInfo(this);
    }

    /**
     * M: set app flag and record first install time.
     *
     * @param pm
     * @param packageName
     */
    public void setFlagAndInstallTime(final PackageManager pm, String packageName) {
        try {
            PackageInfo pi = pm.getPackageInfo(packageName, 0);
            flags = initFlags(pi);
            firstInstallTime = initFirstInstallTime(pi);
        } catch (NameNotFoundException e) {
            Log.d(TAG, "PackageManager.getApplicationInfo failed for " + packageName);
        }
    }

    /**
     * M: set app flag and record first install time.
     *
     * @param pm
     */
    public void setFlagAndInstallTime(final PackageManager pm) {
        String packageName = getPackageName();
        setFlagAndInstallTime(pm, packageName);
    }

    /**
     * M: Returns the package name that the shortcut's intent will resolve to, or
     * an empty string if none exists.
     * 
     * @return the package name.
     */
    String getPackageName() {
        String packageName = "";
        if (intent != null) {
            packageName = intent.getPackage();
            if (packageName == null && intent.getComponent() != null) {
                packageName = intent.getComponent().getPackageName();
            }        
        }
        return packageName;
    }
    


    //begin add by lqsoft for lqtheme
    public Bitmap updateIcon(IconCache cache) {
        iconBitmap = cache.getIcon(intent);
        return iconBitmap;
    }
    //end add by lqsoft for lqtheme
}
