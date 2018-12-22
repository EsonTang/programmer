/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein is
 * confidential and proprietary to MediaTek Inc. and/or its licensors. Without
 * the prior written permission of MediaTek inc. and/or its licensors, any
 * reproduction, modification, use or disclosure of MediaTek Software, and
 * information contained herein, in whole or in part, shall be strictly
 * prohibited.
 *
 * MediaTek Inc. (C) 2010. All rights reserved.
 *
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER
 * ON AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL
 * WARRANTIES, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NONINFRINGEMENT. NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH
 * RESPECT TO THE SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY,
 * INCORPORATED IN, OR SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES
 * TO LOOK ONLY TO SUCH THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO.
 * RECEIVER EXPRESSLY ACKNOWLEDGES THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO
 * OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES CONTAINED IN MEDIATEK
 * SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK SOFTWARE
 * RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S
 * ENTIRE AND CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE
 * RELEASED HEREUNDER WILL BE, AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE
 * MEDIATEK SOFTWARE AT ISSUE, OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE
 * CHARGE PAID BY RECEIVER TO MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek
 * Software") have been modified by MediaTek Inc. All revisions are subject to
 * any receiver's applicable license agreements with MediaTek Inc.
 */

package com.android.launcher3;

import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.SystemProperties;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Xml;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.android.launcher3.op.AllApps;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * M: LauncherExtPlugin class used to Get AllAppsListExt.
 */
public class LauncherExtPlugin {
    private static final String TAG = "LauncherExtPlugin";

    private static LauncherExtPlugin sLauncherExtPluginInstance = new LauncherExtPlugin();

    /**
     * Get a LauncherExtPlugin instance.
     * @return LauncherExtPlugin single object.
     */
    public static LauncherExtPlugin getInstance() {
        return sLauncherExtPluginInstance;
    }

    private static final String sOptr = SystemProperties.get("persist.operator.optr");
    private static final String sSpec = SystemProperties.get("persist.operator.spec");
    private static final String sSeg = SystemProperties.get("persist.operator.seg");
    private static final String OPTR_OP09 = "OP09";
    private static final String SEG_DEFAULT = "SEGDEFAULT";
    private static final boolean sSupportOP09A =
        sOptr != null && sOptr.equals(OPTR_OP09)
        && sSeg != null && sSeg.equals(SEG_DEFAULT);

    private static final String TAG_ALLAPPS = "allapps";
    private static final String TAG_APPLICATION_ITEM = "application_item";

    // To record max ID of AllAppList (include app and folder).
    private long mMaxIdInAllAppsList;
    // To record max screen of AllAppList.
    private int mMaxScreen;
    // To record number of empty screen.
    private int mEmptyScreens;
    // To record cell index of cell.
    private int mCellIndex;
    // To record empty cells of current screen.
    private int mEmptyCells;

    public boolean supportEditAndHideApps() {
        if (sSupportOP09A) {
            return true;
        } else {
            return false;
        }
    }

    public void customizeOverviewPanel(Context context,
            ViewGroup overviewPanel, View[] overviewButtons) {
        if (sSupportOP09A) {
            Log.d(TAG, "customizeOverviewPanel()");

            final View wallpaperButton = overviewButtons[0];
            final View widgetButton = overviewButtons[1];
            final View settingsButton = overviewButtons[2];

            final FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams)
                    overviewPanel.getLayoutParams();
            lp.width = FrameLayout.LayoutParams.MATCH_PARENT;
            lp.height = FrameLayout.LayoutParams.MATCH_PARENT;

            final LinearLayout overviewPanelLayout = (LinearLayout)
                    View.inflate(context, R.layout.launcher_overview_panel, null);
            final View editAppsButton = overviewPanelLayout.findViewById(
                    R.id.edit_app_button);
            final View hideAppsButton = overviewPanelLayout.findViewById(
                    R.id.hide_app_button);

            overviewPanelLayout.removeView(editAppsButton);
            overviewPanelLayout.removeView(hideAppsButton);

            final LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)
                    wallpaperButton.getLayoutParams();
            editAppsButton.setLayoutParams(new LinearLayout.LayoutParams(params));
            hideAppsButton.setLayoutParams(new LinearLayout.LayoutParams(params));

            overviewPanel.addView(editAppsButton);
            overviewPanel.addView(hideAppsButton);

            overviewButtons[3] = editAppsButton;
            overviewButtons[4] = hideAppsButton;
        }
    }

    public void initCellCount(Context context) {
        final Resources res = context.getResources();

        AllApps.sAppsCellCountX = res.getInteger(
            R.integer.config_appsCustomizeCellCountX);
        AllApps.sAppsCellCountY = res.getInteger(
            R.integer.config_appsCustomizeCellCountY);

        Log.d(TAG, "initCellCount: AllApps.sAppsCellCountX="
            + AllApps.sAppsCellCountX + ", AllApps.sAppsCellCountY="
            + AllApps.sAppsCellCountY);
    }

    public void initializeMaxIdForAllAppsList(SQLiteDatabase db) {
        if (sSupportOP09A) {
            final int maxIdIndex = 0;
            long id = -1;
            Cursor c = null;

            try {
                c = db.rawQuery("SELECT MAX(_id) FROM allapps", null);
                if (c != null && c.moveToNext()) {
                    id = c.getLong(maxIdIndex);
                }
            } finally {
                if (c != null) {
                    c.close();
                }
            }

            if (id == -1) {
                throw new RuntimeException("Error: could not query max id.");
            }

            Log.d(TAG, "initializeMaxIdForAllAppsList: id=" + id);

            mMaxIdInAllAppsList = id;
        }
    }

    public int getMaxScreenIndexForAllAppsList(SQLiteDatabase db) {
        if (sSupportOP09A) {
            final int maxScreenIndex = 0;
            long screenIndex = -1;
            Cursor c = null;

            try {
                c = db.rawQuery("SELECT MAX(screen) FROM allapps", null);
                if (c != null && c.moveToNext()) {
                    screenIndex = c.getLong(maxScreenIndex);
                }
            } finally {
                if (c != null) {
                    c.close();
                }
            }

            Log.d(TAG, "getMaxScreenIndexForAllAppsList: screenIndex="
                + screenIndex);

            return (int) screenIndex;
        } else {
            return 0;
        }
    }

    public void createAllAppsTable(SQLiteDatabase db,
                    Context context, long userSerialNumber) {
        if (sSupportOP09A) {
            Log.d(TAG, "createAllAppsTable()");

            mMaxIdInAllAppsList = 1;

            try {
                db.execSQL("CREATE TABLE allapps (" +
                    "_id INTEGER PRIMARY KEY," +
                    "title TEXT," +
                    "intent TEXT," +
                    "container INTEGER," +
                    "screen INTEGER," +
                    "cellX INTEGER," +
                    "cellY INTEGER," +
                    "spanX INTEGER," +
                    "spanY INTEGER," +
                    "itemType INTEGER," +
                    "visible INTEGER NOT NULL DEFAULT 1," +
                    "modified INTEGER NOT NULL DEFAULT 0," +
                    "profileId INTEGER DEFAULT " + userSerialNumber +
                    ");");
            } catch (SQLException e) {
                Log.e(TAG, "createAllAppsTable: SQLException", e);
            }

            SharedPreferences sp = context.getSharedPreferences(
                AllApps.SHARED_PREFERENCE_KEY, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putBoolean(
                AllApps.DB_CREATED_BUT_DEFAULT_ALLAPPS_NOT_LOADED, true);
            editor.commit();
        }
    }

    public long generateNewIdForAllAppsList() {
        if (sSupportOP09A) {
            if (mMaxIdInAllAppsList < 0) {
                throw new RuntimeException("Error: max id was not initialized");
            }

            mMaxIdInAllAppsList += 1;

            Log.d(TAG, "generateNewIdForAllAppsList: mMaxIdMainmenu="
                + mMaxIdInAllAppsList);

            return mMaxIdInAllAppsList;
        } else {
            return -1;
        }
    }

    public boolean loadDefaultAllAppsIfNecessary(
                    SQLiteDatabase db, Context context) {
        if (sSupportOP09A) {
            Log.d(TAG, "loadDefaultAllAppsIfNecessary()");

            final SharedPreferences sp = context.getSharedPreferences(
                AllApps.SHARED_PREFERENCE_KEY, Context.MODE_PRIVATE);
            final boolean loadDefault = sp.getBoolean(
                AllApps.DB_CREATED_BUT_DEFAULT_ALLAPPS_NOT_LOADED, false);

            if (loadDefault) {
                SharedPreferences.Editor editor = sp.edit();
                editor.remove(AllApps.DB_CREATED_BUT_DEFAULT_ALLAPPS_NOT_LOADED);
                loadDefaultAllAppsList(db, context, R.xml.default_allapps);
                editor.commit();
            }

            return loadDefault;
        } else {
            return false;
        }
    }

    private int loadDefaultAllAppsList(SQLiteDatabase db, Context context,
                    final int allAppsListResourceId) {
        Log.d(TAG, "loadDefaultAllAppsList: allAppsListResourceId="
            + allAppsListResourceId);

        mMaxScreen = 0;
        mEmptyScreens = 0;
        mCellIndex = -1;
        mEmptyCells = 0;

        final Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        final PackageManager packageManager = context.getPackageManager();
        List<ResolveInfo> apps = null;

        apps = packageManager.queryIntentActivities(intent, 0);
        if (apps == null || apps.size() == 0) {
            Log.e(TAG, "queryIntentActivities got null or zero!");
            return 0;
        }

        // Sort the apps
        Collections.sort(apps, new ResolveInfo.DisplayNameComparator(packageManager));

        List<ComponentName> componentNames = new ArrayList<ComponentName>();
        for (ResolveInfo info : apps) {
            final String packageName = info.activityInfo.applicationInfo.packageName;
            componentNames.add(new ComponentName(packageName, info.activityInfo.name));
        }

        Log.d(TAG, "loadDefaultAllAppsList: query PMS got " + componentNames.size()
            + " apps, activityInfos=" + componentNames);

        ContentValues values = new ContentValues();
        int i = 0;

        try {
            XmlResourceParser parser = context.getResources().getXml(
                allAppsListResourceId);
            AttributeSet attrs = Xml.asAttributeSet(parser);
            beginDocument(parser, TAG_ALLAPPS);

            final int depth = parser.getDepth();

            int type;
            long id = -1;

            while (((type = parser.next()) != XmlPullParser.END_TAG
                    || parser.getDepth() > depth)
                   && type != XmlPullParser.END_DOCUMENT) {
                if (type != XmlPullParser.START_TAG) {
                    continue;
                }

                final String name = parser.getName();
                if (TAG_APPLICATION_ITEM.equals(name)) {
                    values.clear();
                    TypedArray a = context.obtainStyledAttributes(
                        attrs, R.styleable.AllApps);
                    id = addItemToAllAppsList(db, values, a, packageManager,
                            intent, componentNames);
                    a.recycle();
                }

                if (id >= 0) {
                    i++;
                }
            }
        } catch (XmlPullParserException e) {
            Log.w(TAG, "Got exception parsing favorites.", e);
        } catch (IOException e) {
            Log.w(TAG, "Got exception parsing favorites.", e);
        }

        Log.d(TAG, "loadDefaultAllAppsList: load i=" + i
            + " components from xml file.");
        Log.d(TAG, "loadDefaultAllAppsList: remains componentNames.size="
            + componentNames.size() + ", componentNames=" + componentNames);

        mCellIndex++;
        long id = -1;

        for (ComponentName componentName : componentNames) {
            values.clear();
            id = addItemToAllAppsList(db, values, packageManager, intent,
                    componentName);

            if (id >= 0) {
                i++;
            }
        }

        Log.d(TAG, "loadDefaultAllAppsList: load i=" + i + " apks totally.");

        return i;
    }

    private static void beginDocument(final XmlPullParser parser,
                            String firstElementName)
                            throws XmlPullParserException, IOException {
        int type;

        while ((type = parser.next()) != XmlPullParser.START_TAG
                && type != XmlPullParser.END_DOCUMENT) {
        }

        if (type != XmlPullParser.START_TAG) {
            throw new XmlPullParserException("No start tag found");
        }

        if (!parser.getName().equals(firstElementName)) {
            throw new XmlPullParserException("Unexpected start tag: found="
                        + parser.getName() + ", expected=" + firstElementName);
        }
    }

    private long addItemToAllAppsList(SQLiteDatabase db,
                    ContentValues values, TypedArray a,
                    PackageManager packageManager, Intent intent,
                    List<ComponentName> componentNames) {
        long container = AllApps.CONTAINER_ALLAPP;
        String strContainer = a.getString(R.styleable.AllApps_op_container);
        if (strContainer != null) {
            container = Long.valueOf(strContainer);
        }
        String screen = a.getString(R.styleable.AllApps_op_page);
        String x = a.getString(R.styleable.AllApps_op_cellX);
        String y = a.getString(R.styleable.AllApps_op_cellY);
        int spanX = 1;
        int spanY = 1;

        int screenNumber = Integer.parseInt(screen);
        if (screenNumber > mMaxScreen) {
            mMaxScreen = screenNumber;
            mEmptyCells = 0;
            if (mCellIndex == -1) {
                mEmptyScreens++;
            }
            mCellIndex = -1;
        } else if (screenNumber < mMaxScreen) {
            throw new IllegalArgumentException(
                        "Wrong xml configuration! The screen number must be ascend:"
                        + " screenNumber=" + screenNumber + ", maxScreen=" + mMaxScreen);
        }

        if (mEmptyScreens != 0) {
            screen = (Integer.valueOf(screen) - mEmptyScreens) + "";
        }

        if (mEmptyCells != 0) {
            int index = changePositionToIndex(Integer.valueOf(x), Integer.valueOf(y));
            if (index < mEmptyCells) {
                throw new IllegalArgumentException(
                            "addItemToAllAppsList: Wrong xml configuration!"
                            + " Index is smaller than empty cells:"
                            + " index=" + index + ", emptycells=" + mEmptyCells);
            }
            x = changeIndexToPositionX(index - mEmptyCells) + "";
            y = changeIndexToPositionY(index - mEmptyCells) + "";
        }

        values.put(AllApps.CONTAINER, container);
        values.put(AllApps.SCREEN, screen);
        values.put(AllApps.CELLX, x);
        values.put(AllApps.CELLY, y);
        values.put(AllApps.SPANX, spanX);
        values.put(AllApps.SPANY, spanY);

        long id = -1;
        ActivityInfo info = null;
        String packageName = a.getString(R.styleable.AllApps_op_appPackageName);
        String className = a.getString(R.styleable.AllApps_op_appClassName);
        ComponentName cmpName = null;

        try {
            try {
                cmpName = new ComponentName(packageName, className);
                info = packageManager.getActivityInfo(cmpName, 0);
            } catch (PackageManager.NameNotFoundException nnfe) {
                String[] packages = packageManager.currentToCanonicalPackageNames(
                                        new String[] {packageName});
                cmpName = new ComponentName(packages[0], className);
                info = packageManager.getActivityInfo(cmpName, 0);
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.w(TAG, "Can't find the activity info in PackageManager: "
                + packageName + "/" + className, e);
            mEmptyCells++;
            return -1;
        }

        if (info == null) {
            mEmptyCells++;
            Log.d(TAG, "Can't find the package info, cellIndex=" + mCellIndex);
            return -1;
        }

        final boolean hasComponent = componentNames.remove(cmpName);

        Log.d(TAG, "Load app item: screen=" + screen + ", x=" + x + ", y=" + y
            + ", intent=" + intent + ", id=" + id + ", pkgName=" + packageName
            + ", clsName=" + className + ", cmpName=" + cmpName
            + ", hasComponent=" + hasComponent + ", info=" + info);

        if (!hasComponent) {
            // There is no such component in system, return -1 directly.
            mEmptyCells++;
            Log.d(TAG, "No such component, cellIndex=" + mCellIndex);
            return -1;
        }

        id = generateNewIdForAllAppsList();
        values.put(AllApps._ID, id);
        intent.setComponent(cmpName);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
            | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        values.put(AllApps.INTENT, intent.toUri(0));
        values.put(AllApps.TITLE, info.loadLabel(packageManager).toString());
        values.put(AllApps.ITEM_TYPE, AllApps.ITEM_TYPE_APPLICATION);

        int cellIndex = changePositionToIndex(Integer.valueOf(x), Integer.valueOf(y));
        if (cellIndex >= getNumberOfIconsInOnePage()) {
            throw new IllegalArgumentException(
                        "Wrong xml configuration! The cell number must be smaller than "
                        + getNumberOfIconsInOnePage() + ", cellIndex=" + cellIndex);
        }
        if (cellIndex != mCellIndex + 1) {
            throw new IllegalArgumentException(
                        "Wrong xml configuration! The cellX and cellY must be "
                        + "ascend and continous: cellIndex=" + cellIndex
                        + ", lastIndex=" + mCellIndex);
        }

        if (dbInsertAndCheck(db, AllApps.TABLE_ALLAPPS, null, values) < 0) {
            mEmptyCells++;
            Log.w(TAG, "Insert app item (" + values + ") to database failed.");
            return -1;
        }

        mCellIndex++;

        return id;
    }

    private long addItemToAllAppsList(SQLiteDatabase db, ContentValues values,
                    final PackageManager packageManager, final Intent intent,
                    final ComponentName componentName) {
        ActivityInfo activityInfo = null;

        try {
            activityInfo = packageManager.getActivityInfo(componentName, 0);
        } catch (PackageManager.NameNotFoundException nnfe) {
            Log.w(TAG, "Can not add such application: " + componentName);
        }

        if (activityInfo == null) {
            return -1;
        }

        final long container = AllApps.CONTAINER_ALLAPP;
        final String screen = String.valueOf(mMaxScreen - mEmptyScreens);
        final String x = String.valueOf(changeIndexToPositionX(mCellIndex));
        final String y = String.valueOf(changeIndexToPositionY(mCellIndex));
        final int spanX = 1;
        final int spanY = 1;

        values.put(AllApps.CONTAINER, container);
        values.put(AllApps.SCREEN, screen);
        values.put(AllApps.CELLX, x);
        values.put(AllApps.CELLY, y);
        values.put(AllApps.SPANX, spanX);
        values.put(AllApps.SPANY, spanY);

        long id = -1;

        id = generateNewIdForAllAppsList();
        values.put(AllApps._ID, id);
        intent.setComponent(componentName);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
            | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        values.put(AllApps.INTENT, intent.toUri(0));
        values.put(AllApps.TITLE,
            activityInfo.loadLabel(packageManager).toString());
        values.put(AllApps.ITEM_TYPE, AllApps.ITEM_TYPE_APPLICATION);

        Log.d(TAG, "Load app item: screen=" + screen + ", x=" + x + ", y=" + y
            + ", intent=" + intent + ", id=" + id
            + ", componentName=" + componentName);

        if (dbInsertAndCheck(db, AllApps.TABLE_ALLAPPS, null, values) < 0) {
            Log.w(TAG, "Insert app item (" + values + ") to database failed.");
            return -1;
        }

        if (mCellIndex >= getNumberOfIconsInOnePage() - 1) {
            mCellIndex = 0;
            mMaxScreen++;
        } else {
            mCellIndex++;
        }

        return id;
    }

    private int changeIndexToPositionX(int index) {
        return index % AllApps.sAppsCellCountX;
    }

    private int changeIndexToPositionY(int index) {
        return index / AllApps.sAppsCellCountX;
    }

    private int changePositionToIndex(int x, int y) {
        return y * AllApps.sAppsCellCountX + x;
    }

    private int getNumberOfIconsInOnePage() {
        return AllApps.sAppsCellCountX * AllApps.sAppsCellCountY;
    }

    private long dbInsertAndCheck(SQLiteDatabase db, String table,
                    String nullColumnHack, ContentValues values) {
        if (!values.containsKey(AllApps._ID)) {
            throw new RuntimeException(
                        "Error: attempting to add item without specifying an id");
        }
        return db.insert(table, nullColumnHack, values);
    }
}
