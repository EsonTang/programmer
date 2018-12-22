/*
 * Copyright (C) 2015 The Android Open Source Project
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
package com.android.launcher3.allapps;

import android.content.Context;
/// M: Add for OP customization.
import android.content.ComponentName;
import android.util.Log;
/// M: Add for OP customization.
import android.graphics.BitmapFactory;

import com.android.launcher3.AppInfo;
/// M: Add for OP customization.
import com.android.launcher3.FolderInfo;
import com.android.launcher3.ItemInfo;
/// @}
import com.android.launcher3.Launcher;
/// M: Add for OP customization. @{
import com.android.launcher3.LauncherExtPlugin;
import com.android.launcher3.LauncherModelPluginEx;
/// @}
import com.android.launcher3.compat.AlphabeticIndexCompat;
import com.android.launcher3.compat.UserHandleCompat;
import com.android.launcher3.config.ProviderConfig;
import com.android.launcher3.model.AppNameComparator;
/// M: Add for OP customization. @{
import com.android.launcher3.R;
import com.android.launcher3.ShortcutInfo;
import com.android.launcher3.op.AllApps;
import com.android.launcher3.op.LauncherLog;
/// @}
import com.android.launcher3.util.ComponentKey;

import java.util.ArrayList;
import java.util.Collections;
/// M: Add for OP customization.
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

/**
 * The alphabetically sorted list of applications.
 */
public class AlphabeticalAppsList {

    public static final String TAG = "AlphabeticalAppsList";
    private static final boolean DEBUG = false;
    private static final boolean DEBUG_PREDICTIONS = false;

    private static final int FAST_SCROLL_FRACTION_DISTRIBUTE_BY_ROWS_FRACTION = 0;
    private static final int FAST_SCROLL_FRACTION_DISTRIBUTE_BY_NUM_SECTIONS = 1;

    private final int mFastScrollDistributionMode = FAST_SCROLL_FRACTION_DISTRIBUTE_BY_NUM_SECTIONS;

    /**
     * Info about a section in the alphabetic list
     */
    public static class SectionInfo {
        // The number of applications in this section
        public int numApps;
        // The section break AdapterItem for this section
        public AdapterItem sectionBreakItem;
        // The first app AdapterItem for this section
        public AdapterItem firstAppItem;
    }

    /**
     * Info about a fast scroller section, depending if sections are merged, the fast scroller
     * sections will not be the same set as the section headers.
     */
    public static class FastScrollSectionInfo {
        // The section name
        public String sectionName;
        // The AdapterItem to scroll to for this section
        public AdapterItem fastScrollToItem;
        // The touch fraction that should map to this fast scroll section info
        public float touchFraction;

        public FastScrollSectionInfo(String sectionName) {
            this.sectionName = sectionName;
        }
    }

    /**
     * Info about a particular adapter item (can be either section or app)
     */
    public static class AdapterItem {
        /** Common properties */
        // The index of this adapter item in the list
        public int position;
        // The type of this item
        public int viewType;

        /** Section & App properties */
        // The section for this item
        public SectionInfo sectionInfo;

        /** App-only properties */
        // The section name of this app.  Note that there can be multiple items with different
        // sectionNames in the same section
        public String sectionName = null;
        // The index of this app in the section
        public int sectionAppIndex = -1;
        // The row that this item shows up on
        public int rowIndex;
        // The index of this app in the row
        public int rowAppIndex;
        // The associated AppInfo for the app
        public AppInfo appInfo = null;
        // The index of this app not including sections
        public int appIndex = -1;

        /// M: Add for OP customization.
        public FolderInfo folderInfo = null;

        public static AdapterItem asSectionBreak(int pos, SectionInfo section) {
            AdapterItem item = new AdapterItem();
            item.viewType = AllAppsGridAdapter.VIEW_TYPE_SECTION_BREAK;
            item.position = pos;
            item.sectionInfo = section;
            section.sectionBreakItem = item;
            return item;
        }

        public static AdapterItem asPredictedApp(int pos, SectionInfo section, String sectionName,
                int sectionAppIndex, AppInfo appInfo, int appIndex) {
            AdapterItem item = asApp(pos, section, sectionName, sectionAppIndex, appInfo, appIndex);
            item.viewType = AllAppsGridAdapter.VIEW_TYPE_PREDICTION_ICON;
            return item;
        }

        public static AdapterItem asApp(int pos, SectionInfo section, String sectionName,
                int sectionAppIndex, AppInfo appInfo, int appIndex) {
            AdapterItem item = new AdapterItem();
            item.viewType = AllAppsGridAdapter.VIEW_TYPE_ICON;
            item.position = pos;
            item.sectionInfo = section;
            item.sectionName = sectionName;
            item.sectionAppIndex = sectionAppIndex;
            item.appInfo = appInfo;
            item.appIndex = appIndex;
            return item;
        }

        public static AdapterItem asEmptySearch(int pos) {
            AdapterItem item = new AdapterItem();
            item.viewType = AllAppsGridAdapter.VIEW_TYPE_EMPTY_SEARCH;
            item.position = pos;
            return item;
        }

        public static AdapterItem asPredictionDivider(int pos) {
            AdapterItem item = new AdapterItem();
            item.viewType = AllAppsGridAdapter.VIEW_TYPE_PREDICTION_DIVIDER;
            item.position = pos;
            return item;
        }

        public static AdapterItem asSearchDivder(int pos) {
            AdapterItem item = new AdapterItem();
            item.viewType = AllAppsGridAdapter.VIEW_TYPE_SEARCH_DIVIDER;
            item.position = pos;
            return item;
        }

        public static AdapterItem asMarketDivider(int pos) {
            AdapterItem item = new AdapterItem();
            item.viewType = AllAppsGridAdapter.VIEW_TYPE_SEARCH_MARKET_DIVIDER;
            item.position = pos;
            return item;
        }

        public static AdapterItem asMarketSearch(int pos) {
            AdapterItem item = new AdapterItem();
            item.viewType = AllAppsGridAdapter.VIEW_TYPE_SEARCH_MARKET;
            item.position = pos;
            return item;
        }

        /// M: Add for OP customization. @{
        public static AdapterItem asFolder(int pos,
                SectionInfo section, String sectionName, int sectionAppIndex,
                FolderInfo folderInfo, int appIndex) {
            AdapterItem item = new AdapterItem();
            item.viewType = AllAppsGridAdapter.FOLDER_VIEW_TYPE;
            item.position = pos;
            item.sectionInfo = section;
            item.sectionName = sectionName;
            item.sectionAppIndex = sectionAppIndex;
            item.appIndex = appIndex;
            item.folderInfo = folderInfo;
            return item;
        }
        /// @}

        public String toString() {
            String s =
                    "position=" + position
                    + " viewType=" + viewType
                    + " sectionInfo=" + sectionInfo
                    + " sectionName=" + sectionName
                    + " sectionAppIndex=" + sectionAppIndex
                    + " rowIndex=" + rowIndex
                    + " rowAppIndex=" + rowAppIndex
                    + " appIndex=" + appIndex;
            if (appInfo != null) {
                s = s + " appInfo=" + appInfo.toString();
            } else {
                s = s + " appInfo=null";
            }
            return s;
        }
    }

    /**
     * Common interface for different merging strategies.
     */
    public interface MergeAlgorithm {
        boolean continueMerging(SectionInfo section, SectionInfo withSection,
                int sectionAppCount, int numAppsPerRow, int mergeCount);
    }

    private Launcher mLauncher;

    // The set of apps from the system not including predictions
    /// M: Modify for OP customization.
    public static final List<AppInfo> mApps = new ArrayList<>();
    private final HashMap<ComponentKey, AppInfo> mComponentToAppMap = new HashMap<>();

    // The set of filtered apps with the current filter
    private List<AppInfo> mFilteredApps = new ArrayList<>();
    // The current set of adapter items
    private List<AdapterItem> mAdapterItems = new ArrayList<>();
    // The set of sections for the apps with the current filter
    private List<SectionInfo> mSections = new ArrayList<>();
    // The set of sections that we allow fast-scrolling to (includes non-merged sections)
    private List<FastScrollSectionInfo> mFastScrollerSections = new ArrayList<>();
    // The set of predicted app component names
    private List<ComponentKey> mPredictedAppComponents = new ArrayList<>();
    // The set of predicted apps resolved from the component names and the current set of apps
    private List<AppInfo> mPredictedApps = new ArrayList<>();
    // The of ordered component names as a result of a search query
    private ArrayList<ComponentKey> mSearchResults;
    private HashMap<CharSequence, String> mCachedSectionNames = new HashMap<>();
    private AllAppsGridAdapter mAdapter;
    private AlphabeticIndexCompat mIndexer;
    private AppNameComparator mAppNameComparator;
    private MergeAlgorithm mMergeAlgorithm;
    private int mNumAppsPerRow;
    private int mNumPredictedAppsPerRow;
    private int mNumAppRowsInAdapter;

    /// M: Add for OP customization. @{
    private boolean mSupportEditAndHideApps;
    public static ArrayList<AppInfo> sShowAndHideApps = new ArrayList<AppInfo>();

    // Include all apps and folders in page, use for display
    public static final ArrayList<ItemInfo> mItems = new ArrayList<>();
    public static final ArrayList<ItemInfo> mItemsBackup = new ArrayList<>();

    //all apps (include app in folder)
    public static ArrayList<AppInfo> sAllApps = new ArrayList<>();
    //all apps in page(except app in folder)
    //public static ArrayList<AppInfo> sApps = new ArrayList<>();
    //all folder;
    //public static ArrayList<FolderInfo> sFolders = new ArrayList<>();
    private int mMaxAppsCountInOnePage = 0;
    /// @}

    public AlphabeticalAppsList(Context context) {
        mLauncher = Launcher.getLauncher(context);
        mIndexer = new AlphabeticIndexCompat(context);
        mAppNameComparator = new AppNameComparator(context);

        /// M: Add for OP customization. @{
        mSupportEditAndHideApps = LauncherExtPlugin.getInstance()
                .supportEditAndHideApps();
        /// @}
    }

    /**
     * Sets the number of apps per row.
     */
    public void setNumAppsPerRow(int numAppsPerRow, int numPredictedAppsPerRow,
            MergeAlgorithm mergeAlgorithm) {
        mNumAppsPerRow = numAppsPerRow;
        mNumPredictedAppsPerRow = numPredictedAppsPerRow;
        mMergeAlgorithm = mergeAlgorithm;

        updateAdapterItems();
    }

    /**
     * Sets the adapter to notify when this dataset changes.
     */
    public void setAdapter(AllAppsGridAdapter adapter) {
        mAdapter = adapter;
    }

    /**
     * Returns all the apps.
     */
    public List<AppInfo> getApps() {
        return mApps;
    }

    /**
     * Returns sections of all the current filtered applications.
     */
    public List<SectionInfo> getSections() {
        return mSections;
    }

    /**
     * Returns fast scroller sections of all the current filtered applications.
     */
    public List<FastScrollSectionInfo> getFastScrollerSections() {
        return mFastScrollerSections;
    }

    /**
     * Returns the current filtered list of applications broken down into their sections.
     */
    public List<AdapterItem> getAdapterItems() {
        return mAdapterItems;
    }

    /**
     * Returns the number of rows of applications (not including predictions)
     */
    public int getNumAppRows() {
        return mNumAppRowsInAdapter;
    }

    /**
     * Returns the number of applications in this list.
     */
    public int getNumFilteredApps() {
        return mFilteredApps.size();
    }

    /**
     * Returns whether there are is a filter set.
     */
    public boolean hasFilter() {
        return (mSearchResults != null);
    }

    /**
     * Returns whether there are no filtered results.
     */
    public boolean hasNoFilteredResults() {
        return (mSearchResults != null) && mFilteredApps.isEmpty();
    }

    /**
     * Sets the sorted list of filtered components.
     */
    public boolean setOrderedFilter(ArrayList<ComponentKey> f) {
        if (mSearchResults != f) {
            boolean same = mSearchResults != null && mSearchResults.equals(f);
            mSearchResults = f;
            updateAdapterItems();
            return !same;
        }
        return false;
    }

    /**
     * Sets the current set of predicted apps.  Since this can be called before we get the full set
     * of applications, we should merge the results only in onAppsUpdated() which is idempotent.
     */
    public void setPredictedApps(List<ComponentKey> apps) {
        mPredictedAppComponents.clear();
        mPredictedAppComponents.addAll(apps);
        onAppsUpdated();
    }

    /**
     * Sets the current set of apps.
     */
    public void setApps(List<AppInfo> apps) {
        mComponentToAppMap.clear();
        addApps(apps);
    }

    /**
     * Adds new apps to the list.
     */
    public void addApps(List<AppInfo> apps) {
        /// M: Add for OP customization. @{
        if (mSupportEditAndHideApps) {
            final int appsCount = apps.size();
            for (int i = 0; i < appsCount; i++) {
                final AppInfo appInfo = apps.get(i);
                appInfo.screenId = 0;
                mItemsBackup.add(appInfo);
                updateAllItemOrderByPos(mItemsBackup);
                LauncherModelPluginEx.addAllAppsItemToDatabase(mLauncher, appInfo,
                    (int) appInfo.screenId, appInfo.cellX, appInfo.cellY, false);
            }
            refreshView(true);
            return;
        }
        /// @}

        updateApps(apps);
    }

    /**
     * Updates existing apps in the list
     */
    public void updateApps(List<AppInfo> apps) {
        ///M: ALPS02831547. Avoid AppList is null.
        if (apps == null) return;

        for (AppInfo app : apps) {
            mComponentToAppMap.put(app.toComponentKey(), app);
        }
        onAppsUpdated();
    }

    /**
     * Removes some apps from the list.
     */
    public void removeApps(List<AppInfo> apps) {
        /// M: Add for OP customization. @{
        if (mSupportEditAndHideApps) {
            for (AppInfo info : apps) {
                for (ItemInfo iteminfo : mItemsBackup) {
                    if (iteminfo instanceof AppInfo) {
                        ComponentName componentName = ((AppInfo) iteminfo).componentName;
                        if (info.componentName.equals(componentName)) {
                            mItemsBackup.remove(iteminfo);
                            deleteItemInDatabase(iteminfo);
                            break;
                        }
                    }
                }
            }
            hideOrRemoveApps(apps, true);
            refreshView(true);
            return;
        }
        /// @}

        for (AppInfo app : apps) {
            mComponentToAppMap.remove(app.toComponentKey());
        }
        onAppsUpdated();
    }

    /**
     * Updates internals when the set of apps are updated.
     */
    private void onAppsUpdated() {
        // Sort the list of apps
        mApps.clear();
        mApps.addAll(mComponentToAppMap.values());
        Collections.sort(mApps, mAppNameComparator.getAppInfoComparator());

        // As a special case for some languages (currently only Simplified Chinese), we may need to
        // coalesce sections
        Locale curLocale = mLauncher.getResources().getConfiguration().locale;
        TreeMap<String, ArrayList<AppInfo>> sectionMap = null;
        boolean localeRequiresSectionSorting = curLocale.equals(Locale.SIMPLIFIED_CHINESE);
        if (localeRequiresSectionSorting) {
            // Compute the section headers.  We use a TreeMap with the section name comparator to
            // ensure that the sections are ordered when we iterate over it later
            sectionMap = new TreeMap<>(mAppNameComparator.getSectionNameComparator());
            for (AppInfo info : mApps) {
                // Add the section to the cache
                String sectionName = getAndUpdateCachedSectionName(info.title);

                // Add it to the mapping
                ArrayList<AppInfo> sectionApps = sectionMap.get(sectionName);
                if (sectionApps == null) {
                    sectionApps = new ArrayList<>();
                    sectionMap.put(sectionName, sectionApps);
                }
                sectionApps.add(info);
            }

            // Add each of the section apps to the list in order
            List<AppInfo> allApps = new ArrayList<>(mApps.size());
            for (Map.Entry<String, ArrayList<AppInfo>> entry : sectionMap.entrySet()) {
                allApps.addAll(entry.getValue());
            }

            mApps.clear();
            mApps.addAll(allApps);
        } else {
            // Just compute the section headers for use below
            for (AppInfo info : mApps) {
                // Add the section to the cache
                getAndUpdateCachedSectionName(info.title);
            }
        }

        // Recompose the set of adapter items from the current set of apps
        updateAdapterItems();
    }

    /**
     * Updates the set of filtered apps with the current filter.  At this point, we expect
     * mCachedSectionNames to have been calculated for the set of all apps in mApps.
     */
    private void updateAdapterItems() {
        SectionInfo lastSectionInfo = null;
        String lastSectionName = null;
        FastScrollSectionInfo lastFastScrollerSectionInfo = null;
        int position = 0;
        int appIndex = 0;

        // Prepare to update the list of sections, filtered apps, etc.
        mFilteredApps.clear();
        mFastScrollerSections.clear();
        mAdapterItems.clear();
        mSections.clear();

        if (DEBUG_PREDICTIONS) {
            if (mPredictedAppComponents.isEmpty() && !mApps.isEmpty()) {
                mPredictedAppComponents.add(new ComponentKey(mApps.get(0).componentName,
                        UserHandleCompat.myUserHandle()));
                mPredictedAppComponents.add(new ComponentKey(mApps.get(0).componentName,
                        UserHandleCompat.myUserHandle()));
                mPredictedAppComponents.add(new ComponentKey(mApps.get(0).componentName,
                        UserHandleCompat.myUserHandle()));
                mPredictedAppComponents.add(new ComponentKey(mApps.get(0).componentName,
                        UserHandleCompat.myUserHandle()));
            }
        }

        // Add the search divider
        mAdapterItems.add(AdapterItem.asSearchDivder(position++));

        // Process the predicted app components
        mPredictedApps.clear();
        if (mPredictedAppComponents != null && !mPredictedAppComponents.isEmpty() && !hasFilter()) {
            for (ComponentKey ck : mPredictedAppComponents) {
                AppInfo info = mComponentToAppMap.get(ck);
                if (info != null) {
                    mPredictedApps.add(info);
                } else {
                    if (ProviderConfig.IS_DOGFOOD_BUILD) {
                        Log.e(TAG, "Predicted app not found: " + ck);
                    }
                }
                // Stop at the number of predicted apps
                if (mPredictedApps.size() == mNumPredictedAppsPerRow) {
                    break;
                }
            }

            if (!mPredictedApps.isEmpty()) {
                // Add a section for the predictions
                lastSectionInfo = new SectionInfo();
                lastFastScrollerSectionInfo = new FastScrollSectionInfo("");
                AdapterItem sectionItem = AdapterItem.asSectionBreak(position++, lastSectionInfo);
                mSections.add(lastSectionInfo);
                mFastScrollerSections.add(lastFastScrollerSectionInfo);
                mAdapterItems.add(sectionItem);

                // Add the predicted app items
                for (AppInfo info : mPredictedApps) {
                    AdapterItem appItem = AdapterItem.asPredictedApp(position++, lastSectionInfo,
                            "", lastSectionInfo.numApps++, info, appIndex++);
                    if (lastSectionInfo.firstAppItem == null) {
                        lastSectionInfo.firstAppItem = appItem;
                        lastFastScrollerSectionInfo.fastScrollToItem = appItem;
                    }
                    mAdapterItems.add(appItem);
                    mFilteredApps.add(info);
                }

                mAdapterItems.add(AdapterItem.asPredictionDivider(position++));
            }
        }

        // Recreate the filtered and sectioned apps (for convenience for the grid layout) from the
        // ordered set of sections
        /// M: Add for OP customization. @{
        if (mSupportEditAndHideApps) {
            for (ItemInfo info : getFiltersItemInfos()) {
                String sectionName = getAndUpdateCachedSectionName(info.title);

                // Create a new section if the section names do not match
                if (lastSectionInfo == null || !sectionName.equals(lastSectionName)) {
                    lastSectionName = sectionName;
                    lastSectionInfo = new SectionInfo();
                    lastFastScrollerSectionInfo = new FastScrollSectionInfo(sectionName);
                    mSections.add(lastSectionInfo);
                    mFastScrollerSections.add(lastFastScrollerSectionInfo);

                    // Create a new section item to break the flow of items in the list
                    if (!hasFilter()) {
                        AdapterItem sectionItem = AdapterItem.asSectionBreak(
                            position++, lastSectionInfo);
                        mAdapterItems.add(sectionItem);
                    }
                }

                // Create an app item (op09_new: or a folder item)
                AdapterItem appItem = null;
                if (info instanceof AppInfo) {
                    appItem = AdapterItem.asApp(position++, lastSectionInfo, sectionName,
                            lastSectionInfo.numApps++, (AppInfo) info, appIndex++);
                } else if (info instanceof FolderInfo) {
                    appItem = AdapterItem.asFolder(position++, lastSectionInfo, sectionName,
                            lastSectionInfo.numApps++, (FolderInfo) info, appIndex++);
                }

                if (lastSectionInfo.firstAppItem == null) {
                    lastSectionInfo.firstAppItem = appItem;
                    lastFastScrollerSectionInfo.fastScrollToItem = appItem;
                }

                mAdapterItems.add(appItem);
                if (info instanceof AppInfo) {
                    mFilteredApps.add((AppInfo) info);
                }
            }
        } else {
        /// @}
            for (AppInfo info : getFiltersAppInfos()) {
                String sectionName = getAndUpdateCachedSectionName(info.title);

                // Create a new section if the section names do not match
                if (lastSectionInfo == null || !sectionName.equals(lastSectionName)) {
                    lastSectionName = sectionName;
                    lastSectionInfo = new SectionInfo();
                    lastFastScrollerSectionInfo = new FastScrollSectionInfo(sectionName);
                    mSections.add(lastSectionInfo);
                    mFastScrollerSections.add(lastFastScrollerSectionInfo);

                    // Create a new section item to break the flow of items in the list
                    if (!hasFilter()) {
                        AdapterItem sectionItem =
                            AdapterItem.asSectionBreak(position++, lastSectionInfo);
                        mAdapterItems.add(sectionItem);
                    }
                }

                // Create an app item
                AdapterItem appItem = AdapterItem.asApp(position++, lastSectionInfo, sectionName,
                        lastSectionInfo.numApps++, info, appIndex++);
                if (lastSectionInfo.firstAppItem == null) {
                    lastSectionInfo.firstAppItem = appItem;
                    lastFastScrollerSectionInfo.fastScrollToItem = appItem;
                }
                mAdapterItems.add(appItem);
                mFilteredApps.add(info);
            }
        }

        // Append the search market item if we are currently searching
        if (hasFilter()) {
            if (hasNoFilteredResults()) {
                mAdapterItems.add(AdapterItem.asEmptySearch(position++));
            } else {
                mAdapterItems.add(AdapterItem.asMarketDivider(position++));
            }
            mAdapterItems.add(AdapterItem.asMarketSearch(position++));
        }

        // Merge multiple sections together as requested by the merge strategy for this device
        mergeSections();

        if (mNumAppsPerRow != 0) {
            // Update the number of rows in the adapter after we do all the merging (otherwise, we
            // would have to shift the values again)
            int numAppsInSection = 0;
            int numAppsInRow = 0;
            int rowIndex = -1;
            for (AdapterItem item : mAdapterItems) {
                item.rowIndex = 0;
                if (AllAppsGridAdapter.isDividerViewType(item.viewType)) {
                    numAppsInSection = 0;
                } else if (AllAppsGridAdapter.isIconViewType(item.viewType)
                           /// M: Add for OP customization.
                           || item.viewType == AllAppsGridAdapter.FOLDER_VIEW_TYPE) {
                    if (numAppsInSection % mNumAppsPerRow == 0) {
                        numAppsInRow = 0;
                        rowIndex++;
                    }
                    item.rowIndex = rowIndex;
                    item.rowAppIndex = numAppsInRow;
                    numAppsInSection++;
                    numAppsInRow++;
                }
            }
            mNumAppRowsInAdapter = rowIndex + 1;

            // Pre-calculate all the fast scroller fractions
            switch (mFastScrollDistributionMode) {
                case FAST_SCROLL_FRACTION_DISTRIBUTE_BY_ROWS_FRACTION:
                    float rowFraction = 1f / mNumAppRowsInAdapter;
                    for (FastScrollSectionInfo info : mFastScrollerSections) {
                        AdapterItem item = info.fastScrollToItem;
                        if (!AllAppsGridAdapter.isIconViewType(item.viewType)
                            /// M: Add for OP customization.
                            && item.viewType != AllAppsGridAdapter.FOLDER_VIEW_TYPE) {
                            info.touchFraction = 0f;
                            continue;
                        }

                        float subRowFraction = item.rowAppIndex * (rowFraction / mNumAppsPerRow);
                        info.touchFraction = item.rowIndex * rowFraction + subRowFraction;
                    }
                    break;
                case FAST_SCROLL_FRACTION_DISTRIBUTE_BY_NUM_SECTIONS:
                    float perSectionTouchFraction = 1f / mFastScrollerSections.size();
                    float cumulativeTouchFraction = 0f;
                    for (FastScrollSectionInfo info : mFastScrollerSections) {
                        AdapterItem item = info.fastScrollToItem;
                        if (!AllAppsGridAdapter.isIconViewType(item.viewType)
                            /// M: Add for OP customization.
                            && item.viewType != AllAppsGridAdapter.FOLDER_VIEW_TYPE) {
                            info.touchFraction = 0f;
                            continue;
                        }
                        info.touchFraction = cumulativeTouchFraction;
                        cumulativeTouchFraction += perSectionTouchFraction;
                    }
                    break;
            }
        }

        // Refresh the recycler view
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
    }

    private List<AppInfo> getFiltersAppInfos() {
        if (mSearchResults == null) {
            return mApps;
        }

        ArrayList<AppInfo> result = new ArrayList<>();
        for (ComponentKey key : mSearchResults) {
            AppInfo match = mComponentToAppMap.get(key);
            if (match != null) {
                result.add(match);
            }
        }
        return result;
    }

    /**
     * Merges multiple sections to reduce visual raggedness.
     */
    private void mergeSections() {
        // Ignore merging until we have an algorithm and a valid row size
        if (mMergeAlgorithm == null || mNumAppsPerRow == 0) {
            return;
        }

        // Go through each section and try and merge some of the sections
        if (!hasFilter()) {
            int sectionAppCount = 0;
            for (int i = 0; i < mSections.size() - 1; i++) {
                SectionInfo section = mSections.get(i);
                sectionAppCount = section.numApps;
                int mergeCount = 1;

                // Merge rows based on the current strategy
                while (i < (mSections.size() - 1) &&
                        mMergeAlgorithm.continueMerging(section, mSections.get(i + 1),
                                sectionAppCount, mNumAppsPerRow, mergeCount)) {
                    SectionInfo nextSection = mSections.remove(i + 1);

                    // Remove the next section break
                    mAdapterItems.remove(nextSection.sectionBreakItem);
                    int pos = mAdapterItems.indexOf(section.firstAppItem);

                    // Point the section for these new apps to the merged section
                    int nextPos = pos + section.numApps;
                    for (int j = nextPos; j < (nextPos + nextSection.numApps); j++) {
                        AdapterItem item = mAdapterItems.get(j);
                        item.sectionInfo = section;
                        item.sectionAppIndex += section.numApps;
                    }

                    // Update the following adapter items of the removed section item
                    pos = mAdapterItems.indexOf(nextSection.firstAppItem);
                    for (int j = pos; j < mAdapterItems.size(); j++) {
                        AdapterItem item = mAdapterItems.get(j);
                        item.position--;
                    }
                    section.numApps += nextSection.numApps;
                    sectionAppCount += nextSection.numApps;

                    if (DEBUG) {
                        Log.d(TAG, "Merging: " + nextSection.firstAppItem.sectionName +
                                " to " + section.firstAppItem.sectionName +
                                " mergedNumRows: " + (sectionAppCount / mNumAppsPerRow));
                    }
                    mergeCount++;
                }
            }
        }
    }

    /**
     * Returns the cached section name for the given title, recomputing and updating the cache if
     * the title has no cached section name.
     */
    private String getAndUpdateCachedSectionName(CharSequence title) {
        String sectionName = mCachedSectionNames.get(title);
        if (sectionName == null) {
            sectionName = mIndexer.computeSectionName(title);
            mCachedSectionNames.put(title, sectionName);
        }
        return sectionName;
    }

    /// M: Add for OP customization. @{

    // Back from HideAppsActivity, process the state change of apps.
    public void processAppsStateChanged() {
        if (LauncherLog.DEBUG_EDIT) {
            LauncherLog.d(TAG, "processAppsStateChanged");
        }

        // Used to recorder all apps which will be hidden.
        ArrayList<AppInfo> hideApps = new ArrayList<AppInfo>();
        // Used to recorder app apps which will be shown.
        ArrayList<AppInfo> showApp = new ArrayList<AppInfo>();

        int count = sShowAndHideApps.size();
        for (int i = 0; i < count; i++) {
            AppInfo appInfo = sShowAndHideApps.get(i);

            appInfo = checkShowAndHideApp(appInfo);

            if (appInfo != null) {
                if (appInfo.isVisible) {
                    showApp.add(appInfo);
                } else {
                    hideApps.add(appInfo);
                }
            }
        }

        if (hideApps.isEmpty() && showApp.isEmpty()) {
            LauncherLog.e(TAG, "processAppsStateChanged: no valid app.");
        }

        // Hide apps.
        if (hideApps.size() > 0) {
            hideOrRemoveApps(hideApps, false);
        }

        // Show apps.
        if (showApp.size() > 0) {
            showApps(showApp);
        }

        sShowAndHideApps.clear();

        refreshView(true);

        for (ItemInfo itemInfo : mItems) {
            updateItemInDatabase(itemInfo);
        }

        // If the apps are hidden, the corresponding shortcuts in the homescreen
        // will be removed.
        if (hideApps.size() > 0) {
           mLauncher.getWorkspace().removeItemsByAppInfo(hideApps);
        }
    }

    private AppInfo checkShowAndHideApp(AppInfo appInfo) {
        ComponentName componentName = appInfo.componentName;
        if (componentName == null && appInfo.intent != null
            && appInfo.intent.getComponent() != null) {
            componentName = appInfo.intent.getComponent();
        }

        if (componentName == null) {
            LauncherLog.e(TAG, "checkShowAndHideApp: app componentName is null"
                + ", app=" + appInfo);
            return appInfo;
        }

        // Find item in mItemsBackup
        AppInfo newAppInfo = null;
        FolderInfo folderInfo = null;
        for (ItemInfo item : mItemsBackup) {
            if (item instanceof AppInfo) {
                AppInfo appItem = (AppInfo) item;
                if (componentName.equals(appItem.componentName)) {
                    newAppInfo = appItem;
                    if (appInfo != newAppInfo) {
                        LauncherLog.e(TAG, "checkShowAndHideApp: app not matched"
                            + ", old=" + appInfo + ", new=" + newAppInfo);
                        newAppInfo.isVisible = appInfo.isVisible;
                    }
                    break;
                }
            } else if ((item instanceof FolderInfo) && !appInfo.isVisible) {
                folderInfo = (FolderInfo) item;
                for (ShortcutInfo scItem : folderInfo.contents) {
                    if (componentName.equals(scItem.mComponentName)) {
                        if (appInfo.container != folderInfo.id) {
                            LauncherLog.e(TAG, "checkShowAndHideApp: app not matched"
                                + ", old=" + appInfo + ", new=" + scItem);
                        }
                        newAppInfo = scItem.makeAppInfo();
                        newAppInfo.isVisible = appInfo.isVisible;
                        break;
                    }
                }
            }
        }

        if (newAppInfo == null) {
            LauncherLog.e(TAG, "checkShowAndHideApp: app not found, app=" + appInfo);
        }

        return newAppInfo;
    }

    // Hide or remove some apps.
    private void hideOrRemoveApps(List<AppInfo> apps, boolean isRemoved) {
        if (LauncherLog.DEBUG_EDIT) {
            LauncherLog.d(TAG, "hideOrRemoveApps: apps = " + apps + ",isRemoved = " + isRemoved);
        }

        // Used to recorder all pages which apps state changed.
        final int hideAppsCount = apps.size();

        for (int i = 0; i < hideAppsCount; i++) {
            final AppInfo appInfo = apps.get(i);
            // The root cause is STK enable/disable components, that makes the
            // appInfo is not added to a real page before it removed, so the
            // screenId is invalid and JE happens. We need check the screenId.
            if (appInfo.screenId == -1) {
                LauncherLog.i(TAG, "hideOrRemoveApps: appInfo.screenId == -1 -> appInfo is "
                        + appInfo);
                continue;
            }

            long page = appInfo.screenId;
            if (appInfo.container != AllApps.CONTAINER_ALLAPP) {
                for (ItemInfo itemInfo : mItemsBackup) {
                    if (itemInfo instanceof FolderInfo) {
                        boolean result = removeFromFolder(appInfo, (FolderInfo) itemInfo);
                        if (result) {
                            break;
                        }
                    }
                }
                appInfo.container = AllApps.CONTAINER_ALLAPP;
                appInfo.screenId = 0;
                if (isRemoved) {
                    deleteItemInDatabase(appInfo);
                } else {
                    mItemsBackup.add(appInfo);
                    updateItemInDatabase(appInfo);
                }
            } else {
                mItemsBackup.remove(appInfo);

                if (isRemoved) {
                    deleteItemInDatabase(appInfo);
                } else {
                    mItemsBackup.add(appInfo);
                    updateItemInDatabase(appInfo);
                }
            }
        }

        AppInfo testinfo = null;
         for (int i = mItemsBackup.size() - 1; i >= 0; i--) {
            ItemInfo itemInfo = mItemsBackup.get(i);
            if (itemInfo instanceof FolderInfo) {
                if (((FolderInfo) itemInfo).contents.size() == 0) {
                    mItemsBackup.remove(itemInfo);
                    deleteItemInDatabase(itemInfo);
                } else if (((FolderInfo) itemInfo).contents.size() == 1) {
                    AppInfo appInfo = ((FolderInfo) itemInfo).contents.get(0).makeAppInfo();
                    appInfo.container = AllApps.CONTAINER_ALLAPP;
                    appInfo.screenId = 0;

                    int index = mItemsBackup.indexOf(itemInfo);
                    mItemsBackup.set(index, appInfo);

                    testinfo = appInfo;

                    updateItemInDatabase(appInfo);
                    deleteItemInDatabase(itemInfo);
                }
            }
        }

        updateAllItemOrderByPos(mItemsBackup);

        for (ItemInfo itemInfo : mItemsBackup) {
            updateItemInDatabase(itemInfo);
        }
    }

    // Show apps which state changed from hide to show.
    private void showApps(final List<AppInfo> showAppsList) {
        for (AppInfo info : showAppsList) {
            mItemsBackup.remove(info);
            mItemsBackup.add(info);
        }

        updateAllItemOrderByPos(mItemsBackup);

        for (ItemInfo itemInfo : mItemsBackup) {
            //updateItemInDatabase(itemInfo);
        }
    }

    public void updateAllApps() {
        mItems.clear();
        for (ItemInfo info : mItemsBackup) {
            if (info instanceof AppInfo) {
                if (((AppInfo) info).isVisible) {
                    mItems.add(info);
                }
            } else if (info instanceof FolderInfo) {
                mItems.add((FolderInfo) info);
            }
        }

        if (Launcher.isInEditMode() && mItems.size() > 0) {
            AppInfo padingInfo = null;
            ItemInfo info = mItemsBackup.get(0);
            if (info instanceof FolderInfo) {
                AppInfo tempinfo = ((FolderInfo) info).contents.get(0).makeAppInfo();
                padingInfo = new AppInfo(tempinfo);
            } else {
                padingInfo = new AppInfo((AppInfo) info);
            }
            padingInfo.isForPadding = AppInfo.PADDING_APP;
            padingInfo.id = AppInfo.NO_ID;
            padingInfo.isVisible = true;
            padingInfo.title = "Padding";
            ComponentName cn = new ComponentName("com.op09.launcher", "com.op09.padding");
            padingInfo.intent.setComponent(cn);
            padingInfo.componentName = cn;
            padingInfo.iconBitmap = BitmapFactory.decodeResource(mLauncher.getResources(),
                        R.drawable.padding_app_icon);

            int num = mItems.size();

            /*
             * Calculate the total number of items (included the pading items).
             * Add additional row with pading items in the end.
             * This row is used to keep the last page can be top align.
             */
            num = (num + mMaxAppsCountInOnePage - 1) /
                mMaxAppsCountInOnePage * mMaxAppsCountInOnePage + mNumAppsPerRow;

            for (int i = mItems.size(); i < num; i++) {
                mItems.add(padingInfo);
            }
        }
    }

    // Delete an item from database.
    public void deleteItemInDatabase(final ItemInfo info) {
        if (LauncherLog.DEBUG_EDIT && LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "deleteItemInDatabase: info = " + info);
        }
        if (info != null) {
            LauncherModelPluginEx.deleteAllAppsItemFromDatabase(mLauncher, info);
        }
    }

    // Update the app info in database.
    public void updateItemInDatabase(final ItemInfo info) {
        if (LauncherLog.DEBUG_EDIT && LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "updateItemInDatabase: info = " + info);
        }

        if (info != null) {
            LauncherModelPluginEx.moveAllAppsItemInDatabase(
                    mLauncher, info, (int) info.screenId, info.cellX, info.cellY);
        }
    }

    public void setItems(ArrayList<ItemInfo> allItems) {
        mItemsBackup.clear();
        mItemsBackup.addAll(allItems);

        Collections.sort(mItemsBackup, mAllItemPositionComparator);

        refreshView(true);
    }

    public void setMaxAppNumInPage(int maxAppsCountInOnePage) {
        mMaxAppsCountInOnePage = maxAppsCountInOnePage;
    }

    // Update the sequence of item by pos.
    public void updateAllItemOrderByPos(ArrayList<ItemInfo> allItems) {
        ItemInfo info = null;
        int i = 0;
        for (int j = 0; j < allItems.size(); j++) {
            info = allItems.get(j);
            if ((info instanceof FolderInfo) ||
                    (info instanceof AppInfo &&
                    ((AppInfo) info).isForPadding != AppInfo.PADDING_APP
                    && ((AppInfo) info).isVisible)) {
                info.mPos = i;
                info.cellX = i;
                i++;
            }
        }
    }

    public boolean removeFromFolder(final AppInfo appInfo, final FolderInfo folderInfo) {
        final ComponentName componentName = appInfo.componentName;
        // Find item.
        ShortcutInfo info = null;
        for (ShortcutInfo item : folderInfo.contents) {
            if (componentName.equals(item.mComponentName)) {
                info = item;
                break;
            }
        }
        // Remove item.
        if (info != null) {
            if (LauncherLog.DEBUG_EDIT) {
                LauncherLog.d(TAG, "removeFromFolder start, appInfo = " + appInfo
                        + ", folderInfo=" + folderInfo);
            }
            folderInfo.remove(info, false);

            if (LauncherLog.DEBUG_EDIT) {
                LauncherLog.d(TAG, "removeFromFolder end");
            }
            return true;
        } else {
            return false;
        }
    }

    public void refreshView(boolean isUpdataData) {
        if (isUpdataData) {
            Collections.sort(mItemsBackup, mAllItemPositionComparator);
            updateAllApps();
        }

        mApps.clear();
        for (ItemInfo info : mItems) {
            if (info instanceof AppInfo) {
                mApps.add((AppInfo) info);
            }
        }
        updateAdapterItems();
    }

    private List<ItemInfo> getFiltersItemInfos() {
        if (mSearchResults == null) {  // || Launcher.isInEditMode()
            return mItems;
        }

        mComponentToAppMap.clear();
        for (ItemInfo info : mItems) {
            if (info instanceof AppInfo) {
                mComponentToAppMap.put(((AppInfo) info).toComponentKey(), (AppInfo) info);
            }
        }

        ArrayList<ItemInfo> result = new ArrayList<>();
        for (ComponentKey key : mSearchResults) {
            AppInfo match = mComponentToAppMap.get(key);
            if (match != null) {
                result.add(match);
            }
        }
        return result;
    }

    // PageInfo Items Position Comparator.
    private final Comparator<ItemInfo> mAllItemPositionComparator
                                        = new Comparator<ItemInfo>() {
        @Override
        public int compare(ItemInfo item1, ItemInfo item2) {
            final int pos1 = item1.cellX;
            final int pos2 = item2.cellX;

            // special case for dragging padding app,
            // its cellx,celly may be the same with a real app.
            // so, in this case, we should make sure dragging
            // padding app always at the front of the real app.
            if (pos1 == pos2) {
                String str1 = item1.title.toString();
                String str2 = item2.title.toString();
                if (str1.equals("DraggingPadding")) {
                    return -1;
                } else if (str2.equals("DraggingPadding")) {
                    return 1;
                }
            }
            return pos1 - pos2;
        }
    };

    /// @}
}
