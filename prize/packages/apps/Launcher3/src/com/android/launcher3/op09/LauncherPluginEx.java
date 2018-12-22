/*
* Copyright (C) 2014 MediaTek Inc.
* Modification based on code covered by the mentioned copyright
* and/or permission notice(s).
*/
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

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.launcher3.ItemInfo;
import com.android.launcher3.op.LauncherLog;
import com.android.launcher3.util.PendingRequestArgs;

import java.util.ArrayList;

public class LauncherPluginEx {
    static final String TAG = "LauncherPluginEx";

    /// M: [OP09]request to hide application. @{
    public static final int REQUEST_HIDE_APPS = 12;
    private static final String HIDE_PACKAGE_NAME = "com.android.launcher3";
    private static final String HIDE_ACTIVITY_NAME = "com.android.launcher3.HideAppsActivity";

    /// M: whether the apps customize pane is in edit mode, add for OP09.
    public static boolean sIsInEditMode = false;  //OP09 private
    //M:[OP09] }@

    //M:[OP09] Market icon start @{
    private Intent mAppMarketIntent = null;
    private static final boolean DISABLE_MARKET_BUTTON = true;
    //M:[OP09] end }@

    private Launcher mLauncher;

    public LauncherPluginEx(Launcher launcher) {
        mLauncher = launcher;
    }

    //M:[OP09]Add for edit/hide apps. Start@{
    /**
     * M: Make the apps customize pane enter edit mode, user can rearrange the
     * application icons in this mode, add for OP09 start.
     */
    private void enterEditMode() {
        if (LauncherLog.DEBUG_EDIT) {
            LauncherLog.d(TAG, "enterEditMode: mIsInEditMode = " + sIsInEditMode);
        }

        sIsInEditMode = true;

        //op09 mLauncher.mAppsCustomizeTabHost.enterEditMode();
        mLauncher.getAppsView().setEditModeFlag();
    }

    /**
     * M: Make the apps customize pane exit edit mode.
     */
    public void exitEditMode() {
        if (LauncherLog.DEBUG_EDIT) {
            LauncherLog.d(TAG, "exitEditMode: mIsInEditMode = " + sIsInEditMode);
        }

        sIsInEditMode = false;
        RecyclerView.Adapter adapter = mLauncher.getAppsView().mAdapter;
        adapter.notifyDataSetChanged();
        mLauncher.getAppsView().invalidate();

        //updateAppMarketIcon();
        //op09 mLauncher.mAppsCustomizeTabHost.exitEditMode();
        mLauncher.getAppsView().exitEditMode();
    }

    public void setHiddenAndEditButton(ViewGroup overviewPanel, View[] overviewPanelButtons) {
        LauncherExtPlugin.getInstance().customizeOverviewPanel(
            mLauncher.getApplicationContext(),
            overviewPanel, overviewPanelButtons);
        final View editAppsButton = overviewPanelButtons[3];
        final View hideAppsButton = overviewPanelButtons[4];

        TextView temp = (TextView) overviewPanelButtons[0];

        ((TextView) editAppsButton).setTextColor(temp.getCurrentTextColor());
        ((TextView) hideAppsButton).setTextColor(temp.getCurrentTextColor());

        editAppsButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                LauncherLog.d(TAG, "onClick:  v = " + v);
                /// OP09 edit mode, to-do.
                enterEditMode();
                //op09 mLauncher.showAllApps(false,
                //     AppsCustomizePagedView.ContentType.Applications, true);
                mLauncher.showAppsView(true /* animated */,
                   true /* updatePredictedApps */, false /* focusSearchBar */);
            }
        });
        editAppsButton.setOnTouchListener(mLauncher.getHapticFeedbackTouchListener());

        hideAppsButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                LauncherLog.d(TAG, "onClick:  arg0 = " + arg0);
                startHideAppsActivity();
            }
        });
        hideAppsButton.setOnTouchListener(mLauncher.getHapticFeedbackTouchListener());
    }

    /**
     * M: Start hideAppsActivity.
     */
    public void startHideAppsActivity() { //op09 private
        mLauncher.setWaitingForResult(new PendingRequestArgs(new ItemInfo())); //add in N1
        Intent intent = new Intent();
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setClassName(HIDE_PACKAGE_NAME, HIDE_ACTIVITY_NAME);
        startActivityForResultSafely(intent, REQUEST_HIDE_APPS);
    }

    private void updateAppMarketIcon(Drawable.ConstantState d) {
        if (!DISABLE_MARKET_BUTTON) {
            // Ensure that the new drawable we are creating has the approprate toolbar icon bounds
            Resources r = mLauncher.getResources();
            Drawable marketIconDrawable = d.newDrawable(r);
            int w = r.getDimensionPixelSize(R.dimen.toolbar_external_icon_width);
            int h = r.getDimensionPixelSize(R.dimen.toolbar_external_icon_height);
            marketIconDrawable.setBounds(0, 0, w, h);

            //updateTextButtonWithDrawable(R.id.market_button, marketIconDrawable);
        }
    }

    /**
     * M: Start Activity For Result Safely.
     */
    private void startActivityForResultSafely(Intent intent, int requestCode) {
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "startActivityForResultSafely: intent = " + intent
                    + ", requestCode = " + requestCode);
        }

        try {
            mLauncher.startActivityForResult(intent, requestCode);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(mLauncher, R.string.activity_not_found, Toast.LENGTH_SHORT).show();
        } catch (SecurityException e) {
            Toast.makeText(mLauncher, R.string.activity_not_found, Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Launcher does not have the permission to launch " + intent +
                    ". Make sure to create a MAIN intent-filter for the corresponding activity " +
                    "or use the exported attribute for this activity.", e);
        }
    }

    //override
    public void bindAllItems(ArrayList<AppInfo> allApps, ArrayList<AppInfo> apps,
            ArrayList<FolderInfo> folders) {
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "bindAllItems:"
                + " allApps.size=" + (allApps != null ? allApps.size() : 0)
                + ", apps.size=" + (apps != null ? apps.size() : 0)
                + ", folders.size=" + (folders != null ? folders.size() : 0));
            if (allApps != null && allApps.size() > 0) {
                for (int i = 0; i < allApps.size(); i++) {
                    LauncherLog.d(TAG, "bindAllItems: allApps[" + i + "]=" + allApps.get(i));
                }
            }
            if (apps != null && apps.size() > 0) {
                for (int i = 0; i < apps.size(); i++) {
                    LauncherLog.d(TAG, "bindAllItems: apps[" + i + "]=" + apps.get(i));
                }
            }
            if (folders != null && folders.size() > 0) {
                for (int i = 0; i < folders.size(); i++) {
                    LauncherLog.d(TAG, "bindAllItems: folders[" + i + "]=" + folders.get(i));
                }
            }
        }

        if (sIsInEditMode) {
            exitEditMode();
        }

        ArrayList<ItemInfo> allItems =  new ArrayList<>();
        allItems.addAll(apps);
        allItems.addAll(folders);
        mLauncher.getAppsView().setItems(allItems);
    }

}
