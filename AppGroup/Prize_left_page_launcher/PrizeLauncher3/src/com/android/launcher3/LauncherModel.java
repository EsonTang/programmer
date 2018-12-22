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

import java.lang.ref.SoftReference;
import java.net.URISyntaxException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;

import android.app.SearchManager;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentProviderClient;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.Intent.ShortcutIconResource;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Parcelable;
import android.os.Process;
import android.os.RemoteException;
import android.os.SystemClock;
import android.provider.BaseColumns;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.util.Pair;

import com.android.download.DownLoadService;
import com.android.download.DownLoadTaskInfo;
import com.android.gallery3d.util.LogUtils;
import com.android.launcher3.InstallWidgetReceiver.WidgetMimeTypeHandlerData;
import com.android.launcher3.LauncherSettings.Favorites;
import com.android.launcher3.dao.FirstInstallTable;
import com.android.launcher3.lq.DefaultConfig;
import com.lqsoft.lqtheme.LqShredPreferences;
import com.mediatek.launcher3.ext.AllApps;
import com.mediatek.launcher3.ext.LauncherLog;
import com.prize.left.page.model.FolderModel;

/**
 * Maintains in-memory state of the Launcher. It is expected that there should be only one
 * LauncherModel object held in a static. Also provide APIs for updating the database state
 * for the Launcher.
 */
public class LauncherModel extends BroadcastReceiver {
    static final boolean DEBUG_LOADERS = true;//LauncherLog.DEBUG_LOADERS;
    static final boolean DEBUG_MODEL = true;
    static final String TAG = "Launcher.Model";

    // true = use a "More Apps" folder for non-workspace apps on upgrade
    // false = strew non-workspace apps across the workspace on upgrade
    public static final boolean UPGRADE_USE_MORE_APPS_FOLDER = false;

    private static final int ITEMS_CHUNK = 6; // batch size for the workspace icons
    private final boolean mAppsCanBeOnRemoveableStorage;

    private final LauncherAppState mApp;
    private final Object mLock = new Object();
    private DeferredHandler mHandler = new DeferredHandler();
    private LoaderTask mLoaderTask;
    private boolean mIsLoaderTaskRunning;
    private volatile boolean mFlushingWorkerThread;
	private TelephonyManager mTelephonyManager;//add by zhouerlong

    // Specific runnable types that are run on the main thread deferred handler, this allows us to
    // clear all queued binding runnables when the Launcher activity is destroyed.
    private static final int MAIN_THREAD_NORMAL_RUNNABLE = 0;
    private static final int MAIN_THREAD_BINDING_RUNNABLE = 1;
    
    private Launcher mlauncher;


    public void setLauncher(Launcher mlauncher) {
		this.mlauncher = mlauncher;
	}
	private static final HandlerThread sWorkerThread = new HandlerThread("launcher-loader");
    static {
        sWorkerThread.start();
    }
    private static final Handler sWorker = new Handler(sWorkerThread.getLooper());

    // We start off with everything not loaded.  After that, we assume that
    // our monitoring of the package manager provides all updates and we never
    // need to do a requery.  These are only ever touched from the loader thread.
    private boolean mWorkspaceLoaded;
    private boolean mAllAppsLoaded;

    // When we are loading pages synchronously, we can't just post the binding of items on the side
    // pages as this delays the rotation process.  Instead, we wait for a callback from the first
    // draw (in Workspace) to initiate the binding of the remaining side pages.  Any time we start
    // a normal load, we also clear this set of Runnables.
    static final ArrayList<Runnable> mDeferredBindRunnables = new ArrayList<Runnable>();

    private SoftReference<Callbacks> mCallbacks;

	private DownLoadService mDownLoadService;

    public DownLoadService getDownLoadService() {
		return mDownLoadService;
	}

	public void setDownLoadService(DownLoadService mDownLoadService) {
		this.mDownLoadService = mDownLoadService;
	}
	// < only access in worker thread >
    AllAppsList mBgAllAppsList;

    // The lock that must be acquired before referencing any static bg data structures.  Unlike
    // other locks, this one can generally be held long-term because we never expect any of these
    // static data structures to be referenced outside of the worker thread except on the first
    // load after configuration change.
    static final Object sBgLock = new Object();

    // sBgItemsIdMap maps *all* the ItemInfos (shortcuts, folders, and widgets) created by
    // LauncherModel to their ids
    static final HashMap<Long, ItemInfo> sBgItemsIdMap = new HashMap<Long, ItemInfo>();

    // sBgWorkspaceItems is passed to bindItems, which expects a list of all folders and shortcuts
    //       created by LauncherModel that are directly on the home screen (however, no widgets or
    //       shortcuts within folders).
    static final ArrayList<ItemInfo> sBgWorkspaceItems = new ArrayList<ItemInfo>();
    static final ArrayList<FirstInstallItemBean> sFirstInstallItems = new ArrayList<FirstInstallItemBean>();

    // sBgAppWidgets is all LauncherAppWidgetInfo created by LauncherModel. Passed to bindAppWidget()
    static final ArrayList<LauncherAppWidgetInfo> sBgAppWidgets =
        new ArrayList<LauncherAppWidgetInfo>();
    

    // sBgFolders is all FolderInfos created by LauncherModel. Passed to bindFolders()
    static final HashMap<Long, FolderInfo> sBgFolders = new HashMap<Long, FolderInfo>();

    // sBgDbIconCache is the set of ItemInfos that need to have their icons updated in the database
    static final HashMap<Object, byte[]> sBgDbIconCache = new HashMap<Object, byte[]>();

    // sBgWorkspaceScreens is the ordered set of workspace screens.
    static final ArrayList<Long> sBgWorkspaceScreens = new ArrayList<Long>();

    // </ only access in worker thread >

    private IconCache mIconCache;
    private Bitmap mDefaultIcon;

    protected int mPreviousConfigMcc;
    /// M: record pervious mnc and orientation config.
    protected int mPreviousConfigMnc;
    protected int mPreviousOrientation;

    /// M: Flag to record whether we need to flush icon cache and label cache.
    private boolean mForceFlushCache;

    /**
     * The number of apps page, add for OP09.
     */
    public static int sMaxAppsPageIndex = 0;
    public int mCurrentPosInMaxPage = 0;
    public SoftReference<Callbacks> getCallBacks() {
    	return this.mCallbacks;
    }
	//????????
    public interface Callbacks {
        public boolean setLoadOnResume();
        public int getCurrentWorkspaceScreen(); //获取屏幕序号
        public void startBinding();//startBinding 还是绑定
        public void bindItems(ArrayList<ItemInfo> shortcuts, int start, int end,
                              boolean forceAnimateIcons);
        public void bindScreens(ArrayList<Long> orderedScreenIds);
        public void bindAddScreens(ArrayList<Long> orderedScreenIds);
        public void bindFolders(HashMap<Long,FolderInfo> folders);
        public void finishBindingItems(boolean upgradePath);//数据加载完成。
        public void bindAppWidget(LauncherAppWidgetInfo info);
        public void bindDownProgress(DownLoadTaskInfo info);
        public void bindAllApplications(ArrayList<AppInfo> apps);
        public void setCustomTheme(String destPath);//??????
        public void suggestWallpaperDimension(boolean isoffset);//??????
        public void dochangeDataIcon(int date, int week,String bg);//add by zhouerlong 0728
        public void screenShotWorkspace();
        public void bindAppsAdded(ArrayList<Long> newScreens,
                                  ArrayList<ItemInfo> addNotAnimated,
                                  ArrayList<ItemInfo> addAnimated,
                                  ArrayList<AppInfo> addedApps);//bindAppsAdded增加新的一批应用
        public void bindAppsUpdated(ArrayList<AppInfo> apps,boolean fromappStore);//bindAppsUpdated 通知一个app 更新重新覆盖
        public void bindAppUpdated(AppInfo apps);//bindAppsUpdated 通知一个app 更新重新覆盖
        /// M: [ALPS01273634] Do not remove shortcut from workspace when receiving ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE.
        public void bindComponentsRemoved(ArrayList<String> packageNames,
                        ArrayList<AppInfo> appInfos,
                        boolean matchPackageNamesOnly,
                        boolean permanent);
        public void bindPackagesUpdated(ArrayList<Object> widgetsAndShortcuts);//bindPackagesUpdated多个应用被更新
        public void bindSearchablesChanged();
        public void bindAppsFinish();
        public boolean isAllAppsButtonRank(int rank);
        public void onPageBoundSynchronously(int page);
        public void dumpLogsToLocalData();
		public void bindComponentsRemovedFromAppStore(
				ArrayList<String> removedPackageNames,
				ArrayList<AppInfo> removedApps, boolean packageRemoved,
				boolean permanent);
    }

    public interface ItemInfoFilter {
        public boolean filterItem(ItemInfo parent, ItemInfo info, ComponentName cn);
    }

    LauncherModel(LauncherAppState app, IconCache iconCache) {
        final Context context = app.getContext();

        mAppsCanBeOnRemoveableStorage = Environment.isExternalStorageRemovable();
        mApp = app;
        mBgAllAppsList = new AllAppsList(iconCache);
        mIconCache = iconCache;

        mDefaultIcon = Utilities.createIconBitmap(
                mIconCache.getFullResDefaultActivityIcon(), context);

        final Resources res = context.getResources();
        Configuration config = res.getConfiguration();
        mPreviousConfigMcc = config.mcc;
        //add by zhouerlong
        mTelephonyManager = (TelephonyManager)mApp.getContext().getSystemService(
                Context.TELEPHONY_SERVICE);
        //add by zhouerlong
    }
    
    private static LauncherModel mlauModel; 
    public static LauncherModel getInstace(LauncherAppState l,Context c) {
    	if(mlauModel ==null) {
    		mlauModel = new LauncherModel(l,IconCache.getInstace(c));
    	}
    	return mlauModel;
    }

    /** Runs the specified runnable immediately if called from the main thread, otherwise it is
     * posted on the main thread handler. */
    /// M: revised to public
    public void runOnMainThread(Runnable r) {
        runOnMainThread(r, 0);
    }
    private void runOnMainThread(Runnable r, int type) {
        if (sWorkerThread.getThreadId() == Process.myTid()) {
            // If we are on the worker thread, post onto the main handler
            mHandler.post(r);
        } else {
            r.run();
        }
    }

    /** Runs the specified runnable immediately if called from the worker thread, otherwise it is
     * posted on the worker thread handler. */
    /// M: revised to public
    public static void runOnWorkerThread(Runnable r) {
        if (sWorkerThread.getThreadId() == Process.myTid()) {
            r.run();
        } else {
            // If we are not on the worker thread, then post to the worker handler
            sWorker.post(r);
        }
    }
    public void OnFolderItemsChange() {
    	
    		try {
    			synchronized (sBgLock) {
       	    	 for (long id : sBgFolders.keySet()) {

     	      		sBgFolders.get(id).itemsChanged();
     	     	 }
				}
			} catch (Exception e) {
				// TODO: handle exception
			}
    	
    }
    
    public class DownloadGameTable {

    	public static final String TABLE_DOWNLOAD_PATH = "content://com.prize.appcenter.provider.appstore.download/"
    			+ DownloadGameTable.TABLE_NAME_GAME;
    	/* download game info 表名 */
    	public static final String TABLE_NAME_GAME = "table_game";
    	public static final String GAME_PACKAGE = "pkg_name";
    	public static final String GAME_NAME = "game_name";
    	public static final String GAME_ICON_URL = "icon_url"; // 游戏显示小图标
    	public static final String GAME_VERSION_CODE = "version_code";
    	public static final String GAME_DOWNLOAD_STATE = "download_state";
    	public static final String GAME_APK_SIZE = "apk_size";
    	public static final String GAME_LOADED_SIZE = "loaded_size"; // 已经下载的大小
    }
    
    
    private void deleteItemsFromAppStore(Context context) {

        final Uri contentUri = LauncherSettings.Favorites.CONTENT_URI;
        if (DEBUG_LOADERS) Log.d(TAG, "loading model from " + contentUri);
        String where = LauncherSettings.Favorites.FROM_APPSTORE+ "=?";
        int count = context.getContentResolver().delete(contentUri, where, new String[]{"1"});
    }
    private List<DownLoadTaskInfo> loadStoreApps(Context context) {
		List<DownLoadTaskInfo> downloads = new ArrayList<>();
		Cursor cursor = context.getContentResolver().query(
				Uri.parse(DownloadGameTable.TABLE_DOWNLOAD_PATH), null, null, null, null);
		while (cursor != null && cursor.moveToNext()) {
			DownLoadTaskInfo downLoadTask = new DownLoadTaskInfo();
			downLoadTask.pkgName=cursor.getString(cursor
					.getColumnIndex(DownloadGameTable.GAME_PACKAGE));
			downLoadTask.title=cursor.getString(cursor
					.getColumnIndex(DownloadGameTable.GAME_NAME));
			downLoadTask.state =cursor.getInt(cursor
					.getColumnIndex(DownloadGameTable.GAME_DOWNLOAD_STATE));
			downLoadTask.iconUrl =cursor.getString(cursor
					.getColumnIndex(DownloadGameTable.GAME_ICON_URL));
			int totalSize=cursor.getInt(cursor
					.getColumnIndex(DownloadGameTable.GAME_APK_SIZE));
			int loadSize=cursor.getInt(cursor
					.getColumnIndex(DownloadGameTable.GAME_LOADED_SIZE));
			if(totalSize!=0) {
				downLoadTask.progress = (int)((float)loadSize * 100 / totalSize);
			}
			if(downLoadTask.state!= DownLoadService.STATE_DOWNLOAD_SUCESS) {
				if (downLoadTask.title != null && downLoadTask.iconUrl != null
						&& downLoadTask.pkgName != null) {
					
					if(!isContains(downloads,downLoadTask.pkgName)) {
						LogUtils.i("zhouerlong", "下载任务信息---"+downLoadTask.toString());
						downloads.add(downLoadTask);
					}
				}
			}
		}
		try {
			if (cursor != null) {
				cursor.close();
			}
		} catch (Exception e) {
		}

		return downloads;
	}
    
    boolean isContains(List<DownLoadTaskInfo> dls,String pkg) {
    	List<String > pkgs = new ArrayList<>();
    	for(DownLoadTaskInfo d : dls) {
    		if(!pkgs.contains(d.pkgName)) {
    			pkgs.add(d.pkgName);
    		}
    	}
    	
    	if(pkgs.contains(pkg)) {
    		return true;
    	}
    	return false;
    	
    }
    
	public boolean updateDownLoadDb(Context c) {
		boolean result = false;
		List<DownLoadTaskInfo> dls = loadStoreApps(c);

		List<String> pkgs = new ArrayList<>();
		List<String> titles = new ArrayList<>();
		for (DownLoadTaskInfo d : dls) {
			pkgs.add(d.pkgName);
			titles.add(d.title);
		}

		StringBuffer select = new StringBuffer();
		StringBuffer pkgStr = new StringBuffer();

		for (int i = 0; i < pkgs.size(); i++) {
			if (i > 0) {
				pkgStr.append(",");
			}
			pkgStr.append("'"+pkgs.get(i)+"'");
		}
		StringBuffer titleStr = new StringBuffer();
		for (int i = 0; i < titles.size(); i++) {
			if (i > 0) {
				titleStr.append(",");
			}
			titleStr.append("'"+titles.get(i)+"'");
		}
		select.append("select _id from favorites where "
				+ LauncherSettings.Favorites.PACKAGE_NAME + " not in ("
				+ pkgStr + " ) and title not in (" + titleStr + " ) and "
				+ LauncherSettings.Favorites.FROM_APPSTORE + " =1 ");
		

	/*	select.append("select _id from favorites where "
				+ LauncherSettings.Favorites.PACKAGE_NAME + " not in ("
				+ pkgStr + " )  and "
				+ LauncherSettings.Favorites.FROM_APPSTORE + " =1 ");*/
		// delete from t_cardType where code in
		StringBuffer deletesql = new StringBuffer(
				"delete from favorites where _id in(" + select + ")");
		LogUtils.i("zhouerlong", "执行数据库操作："+deletesql);

		SQLiteDatabase db = LauncherProvider.mOpenHelper.getWritableDatabase();
		try {
			db.beginTransaction();
			db.execSQL(deletesql.toString());
			db.setTransactionSuccessful();
			return true;
		} catch (Exception e) {
			// TODO: handle exception
		} finally {

			db.endTransaction();
		}

		return false;

	}
    
    static boolean findNextAvailableIconSpaceInScreen(ArrayList<ItemInfo> items, int[] xy,
                                 long screen) {
        LauncherAppState app = LauncherAppState.getInstance();
        if(app.getDynamicGrid()==null) {
        	return false;
        }
        DeviceProfile grid = app.getDynamicGrid().getDeviceProfile();
        final int xCount = (int) grid.numColumns;
        final int yCount = (int) grid.numRows;
        boolean[][] occupied = new boolean[xCount][yCount];

        int cellX, cellY, spanX, spanY;
        for (int i = 0; i < items.size(); ++i) {
            final ItemInfo item = items.get(i);
            if (item.container == LauncherSettings.Favorites.CONTAINER_DESKTOP) {
                if (item.screenId == screen) {
                    cellX = item.cellX;
                    cellY = item.cellY;
                    spanX = item.spanX;
                    spanY = item.spanY;
                    for (int x = cellX; 0 <= x && x < cellX + spanX && x < xCount; x++) {
                        for (int y = cellY; 0 <= y && y < cellY + spanY && y < yCount; y++) {
                            occupied[x][y] = true;
                        }
                    }
                }
            }
        }

        return CellLayout.findVacantCell(xy, 1, 1, xCount, yCount, occupied);
    }
    static Pair<Long, int[]> findNextAvailableIconSpace(Context context, String name,
                                                        Intent launchIntent,
                                                        int firstScreenIndex,
                                                        ArrayList<Long> workspaceScreens) {
        // Lock on the app so that we don't try and get the items while apps are being added
        LauncherAppState app = LauncherAppState.getInstance();
        LauncherModel model = app.getModel();
        boolean found = false;
        synchronized (app) {
            if (sWorkerThread.getThreadId() != Process.myTid()) {
                // Flush the LauncherModel worker thread, so that if we just did another
                // processInstallShortcut, we give it time for its shortcut to get added to the
                // database (getItemsInLocalCoordinates reads the database)
                model.flushWorkerThread();
            }
            final ArrayList<ItemInfo> items = LauncherModel.getItemsInLocalCoordinates(context);

            // Try adding to the workspace screens incrementally, starting at the default or center
            // screen and alternating between +1, -1, +2, -2, etc. (using ~ ceil(i/2f)*(-1)^(i-1))
            firstScreenIndex = Math.min(firstScreenIndex, workspaceScreens.size());
            int count = workspaceScreens.size();
            for (int screen = firstScreenIndex; screen < count && !found; screen++) {
                int[] tmpCoordinates = new int[2];
                if (findNextAvailableIconSpaceInScreen(items, tmpCoordinates,
                        workspaceScreens.get(screen))) {
                    // Update the Launcher db
                    return new Pair<Long, int[]>(workspaceScreens.get(screen), tmpCoordinates);
                }
            }
        }
        return null;
    }

    public void addAndBindAddedApps(final Context context, final ArrayList<ItemInfo> workspaceApps,
                                    final ArrayList<AppInfo> allAppsApps) {
        Callbacks cb = mCallbacks != null ? mCallbacks.get() : null;
        addAndBindAddedApps(context, workspaceApps, cb, allAppsApps);
    }
    public void addItemDataBaseWithFirstInstall(ArrayList<AppInfo> addapps) {
    	for(AppInfo info:addapps) {
    		info.firstInstall=1;
    	}
    }
    		
    
    public void addAppStoreItems(DownLoadTaskInfo downloadInfo) {
    	synchronized (sBgLock) {
    		if(installedExistsFromAppStore(mApp.getContext(), downloadInfo.pkgName)) {
    			return;
    		}
    		ShortcutInfo info = new ShortcutInfo();
    		ArrayList<ItemInfo> list = new ArrayList<>();
    		info.title = downloadInfo.title;
    		info.iconUri = downloadInfo.iconUrl;
    		info.itemType = LauncherSettings.Favorites.ITEM_TYPE_APPLICATION;
    		info.container = LauncherSettings.Favorites.CONTAINER_DESKTOP;
    		info.packageName = downloadInfo.pkgName;
    		info.fromAppStore=1;
    		info.spanX=1;
    		info.spanY =1;
    		list.add(info);
    		LogUtils.i("zhouerlong", "downloadInfo:::::"+info.title);

            Callbacks cb = mCallbacks != null ? mCallbacks.get() : null;
            if(list.size()>0) {
            addAndBindAddedAppsFromAppStore(mApp.getContext(), list, cb, null);
            }
		}
	}
    
    
    public void addAppStoreItemToFolder(DownLoadTaskInfo downloadInfo) {
    	synchronized (sBgLock) {
    		ShortcutInfo info = new ShortcutInfo();
    		ArrayList<ItemInfo> list = new ArrayList<>();
    		info.title = downloadInfo.title;
    		info.iconUri = downloadInfo.iconUrl;
    		info.itemType = LauncherSettings.Favorites.ITEM_TYPE_APPLICATION;
    		info.container = LauncherSettings.Favorites.CONTAINER_DESKTOP;
    		info.packageName = downloadInfo.pkgName;
    		info.fromAppStore=1;
    		info.spanX=1;
    		info.spanY =1;
    		info.container=ItemInfo.NO_ID;
    		

    		FolderModel.getInstance(mApp.getContext()).doGet();
            // Item is in a user folder
            FolderInfo folderInfo =
                    findOrMakeFolder(sBgFolders, downloadInfo.container);
            folderInfo.add(info);

            
		}
	}
    
    
    public void addAppStoreItems(List<DownLoadTaskInfo> downloadInfos) {
    	synchronized (sBgLock) {
    		ArrayList<ItemInfo> list = new ArrayList<>();
    		for(DownLoadTaskInfo downloadInfo:downloadInfos) {
        		ShortcutInfo info = new ShortcutInfo();
        		info.title = downloadInfo.title;
        		info.iconUri = downloadInfo.iconUrl;
        		info.itemType = LauncherSettings.Favorites.ITEM_TYPE_APPLICATION;
        		info.container = LauncherSettings.Favorites.CONTAINER_DESKTOP;
        		info.packageName = downloadInfo.pkgName;
        		info.fromAppStore=1;
        		info.spanX=1;
        		info.spanY =1;
        		if(downloadExists(mApp.getContext(), downloadInfo.title, downloadInfo.pkgName)) {
        			continue;
        		}
        		list.add(info);
    		}
            Callbacks cb = mCallbacks != null ? mCallbacks.get() : null;
            if(list.size()>0) {
            addAndBindAddedAppsFromAppStore(mApp.getContext(), list, cb, null);
            }
		}
	}
    
    

    
    public void addAndBindAddedAppsFromAppStore(final Context context, final ArrayList<ItemInfo> workspaceApps,
                                    final Callbacks callbacks, final ArrayList<AppInfo> allAppsApps) {
        /// M: allAppsApps may be null
        if (workspaceApps.isEmpty() && (allAppsApps != null && allAppsApps.isEmpty())) {
            return;
        }
        // Process the newly added applications and add them to the database first
        Runnable r = new Runnable() {
            public void run() {
                final ArrayList<ItemInfo> addedShortcutsFinal = new ArrayList<ItemInfo>();
                final ArrayList<Long> addedWorkspaceScreensFinal = new ArrayList<Long>();

                // Get the list of workspace screens.  We need to append to this list and
                // can not use sBgWorkspaceScreens because loadWorkspace() may not have been
                // called.
                ArrayList<Long> workspaceScreens = new ArrayList<Long>();
                TreeMap<Integer, Long> orderedScreens = loadWorkspaceScreensDb(context);
                for (Integer i : orderedScreens.keySet()) {
                    long screenId = orderedScreens.get(i);
                    workspaceScreens.add(screenId);
                }

                synchronized(sBgLock) {
                    Iterator<ItemInfo> iter = workspaceApps.iterator();
                    while (iter.hasNext()) {
                    
                    try {

                        final ItemInfo a = iter.next();
                        final String name = a.title.toString();
                        final int fromAppstore = 1;
                        final Intent launchIntent = a.getIntent();

                        // Short-circuit this logic if the icon exists somewhere on the workspace
                        if ((LauncherModel.shortcutExists(context, name, launchIntent)|| LauncherModel.shortcutExistsFromAppStore(context,name,fromAppstore,a.packageName))) {
                        	if (a != null) {
                                final ItemInfo  modified = a;
                                Intent intent = modified.getIntent();
                                if(intent !=null) {
                                    ArrayList<ItemInfo> infos =
                                            getItemInfoForComponentName(intent.getComponent());
                                    for (ItemInfo i : infos) {
                                        if (isShortcutInfoUpdateable(i)) {
                                            ShortcutInfo info = (ShortcutInfo) i;
                                            info.title = a.title.toString();
                                            updateItemInDatabase(context, info);
                                        }
                                    }
                                }
                                }
                                mHandler.post(new Runnable() {
                                    public void run() {
                                        Callbacks cb = mCallbacks != null ? mCallbacks.get() : null;
                                        if (callbacks == cb && cb != null) {
                                        	if(a instanceof AppInfo) {
                                                callbacks.bindAppUpdated((AppInfo)a);
                                        	}
                                        }
                                    }
                                });
                            } else {

                        // Add this icon to the db, creating a new page if necessary.  If there
                        // is only the empty page then we just add items to the first page.
                        // Otherwise, we add them to the next pages.
                        int startSearchPageIndex = workspaceScreens.isEmpty() ? 0 : 2;
                        Pair<Long, int[]> coords = LauncherModel.findNextAvailableIconSpace(context,
                                name, launchIntent, startSearchPageIndex, workspaceScreens);
                        if (coords == null) {
                            LauncherProvider lp = LauncherAppState.getLauncherProvider();

                            // If we can't find a valid position, then just add a new screen.
                            // This takes time so we need to re-queue the add until the new
                            // page is added.  Create as many screens as necessary to satisfy
                            // the startSearchPageIndex.
                            int numPagesToAdd = Math.max(1, startSearchPageIndex + 1 -
                                    workspaceScreens.size());
                            while (numPagesToAdd > 0) {
                                long screenId = lp.generateNewScreenId();
                                // Save the screen id for binding in the workspace
                                workspaceScreens.add(screenId);
                                addedWorkspaceScreensFinal.add(screenId);
                                numPagesToAdd--;
                            }

                            // Find the coordinate again
                            coords = LauncherModel.findNextAvailableIconSpace(context,
                                    name, launchIntent, startSearchPageIndex, workspaceScreens);
                        }
                        if (coords == null) {
                            throw new RuntimeException("Coordinates should not be null");
                        }

                        ShortcutInfo shortcutInfo;
                        if (a instanceof ShortcutInfo) {
                            shortcutInfo = (ShortcutInfo) a;
                        } else if (a instanceof AppInfo) {
                            shortcutInfo = ((AppInfo) a).makeShortcut();
                        } else {
                            throw new RuntimeException("Unexpected info type");
                        }

                        // Add the shortcut to the db
                        addItemToDatabase(context, shortcutInfo,
                                LauncherSettings.Favorites.CONTAINER_DESKTOP,
                                coords.first, coords.second[0], coords.second[1], false);
//                        String packageName = shortcutInfo.getIntent().getComponent().getPackageName();

                        // Save the ShortcutInfo for binding in the workspace
                        addedShortcutsFinal.add(shortcutInfo);
                    }
					} catch (Exception e) {
						// TODO: handle exception
					}
                    }
                }

                // Update the workspace screens
                updateWorkspaceScreenOrder(context, workspaceScreens);

                /// M: allAppsApps may be null
                if (!addedShortcutsFinal.isEmpty() || (allAppsApps != null && !allAppsApps.isEmpty())) {
                    runOnMainThread(new Runnable() {
                        public void run() {
                            Callbacks cb = mCallbacks != null ? mCallbacks.get() : null;
                            if (callbacks == cb && cb != null) {
                                final ArrayList<ItemInfo> addAnimated = new ArrayList<ItemInfo>();
                                final ArrayList<ItemInfo> addNotAnimated = new ArrayList<ItemInfo>();
                                if (!addedShortcutsFinal.isEmpty()) {
                                    ItemInfo info = addedShortcutsFinal.get(addedShortcutsFinal.size() - 1);
                                    long lastScreenId = info.screenId;
                                    for (ItemInfo i : addedShortcutsFinal) {
                                        if (i.screenId == lastScreenId) {
                                            addAnimated.add(i);
                                        } else {
                                            addNotAnimated.add(i);
                                        }
                                    }
                                }
                                callbacks.bindAppsAdded(addedWorkspaceScreensFinal,
                                        addNotAnimated, addAnimated, allAppsApps);
                                
                            }
                        }
                    });
                }
            }
        };
        runOnWorkerThread(r);
    }


    
    
    
    public void addAndBindAddedApps(final Context context, final ArrayList<ItemInfo> workspaceApps,
                                    final Callbacks callbacks, final ArrayList<AppInfo> allAppsApps) {
        /// M: allAppsApps may be null
        if (workspaceApps.isEmpty() && (allAppsApps != null && allAppsApps.isEmpty())) {
            return;
        }
        // Process the newly added applications and add them to the database first
        Runnable r = new Runnable() {
            public void run() {
                final ArrayList<ItemInfo> addedShortcutsFinal = new ArrayList<ItemInfo>();
                final ArrayList<Long> addedWorkspaceScreensFinal = new ArrayList<Long>();

                // Get the list of workspace screens.  We need to append to this list and
                // can not use sBgWorkspaceScreens because loadWorkspace() may not have been
                // called.
                ArrayList<Long> workspaceScreens = new ArrayList<Long>();
                TreeMap<Integer, Long> orderedScreens = loadWorkspaceScreensDb(context);
                for (Integer i : orderedScreens.keySet()) {
                    long screenId = orderedScreens.get(i);
                    workspaceScreens.add(screenId);
                }

                synchronized(sBgLock) {
                    Iterator<ItemInfo> iter = workspaceApps.iterator();
                	SharedPreferences sp = mApp.getContext().getSharedPreferences(
            				"load_default_res", Context.MODE_PRIVATE);
                    while (iter.hasNext()) {
                        ItemInfo a = iter.next();
                        final String name = a.title.toString();
                        final Intent launchIntent = a.getIntent();

                        // Short-circuit this logic if the icon exists somewhere on the workspace
                        if (LauncherModel.shortcutExists(context, name, launchIntent)||name.equals("指纹识别")||name.equals("指紋識別")||name.equals("FingerPrint")) {
                            continue;
                        }
                        

                   	/* if (shortcutExists(context,name)) {
                            continue;
                        }*/

                        // Add this icon to the db, creating a new page if necessary.  If there
                        // is only the empty page then we just add items to the first page.
                        // Otherwise, we add them to the next pages.
                        int page = Utilities.getInstallPosPage();
                		boolean isloaded = sp.getBoolean("load_default_res_loadeds", false);
                		int startSearchPageIndex=workspaceScreens.isEmpty() ? 0 : 1;
//                		if(!isloaded) {
                             startSearchPageIndex = workspaceScreens.isEmpty() ? 0 : page;
//                		}
                        Pair<Long, int[]> coords = LauncherModel.findNextAvailableIconSpace(context,
                                name, launchIntent, startSearchPageIndex, workspaceScreens);
                        if (coords == null) {
                            LauncherProvider lp = LauncherAppState.getLauncherProvider();

                            // If we can't find a valid position, then just add a new screen.
                            // This takes time so we need to re-queue the add until the new
                            // page is added.  Create as many screens as necessary to satisfy
                            // the startSearchPageIndex.
                            int numPagesToAdd = Math.max(1, startSearchPageIndex + 1 -
                                    workspaceScreens.size());
                            while (numPagesToAdd > 0) {
                                long screenId = lp.generateNewScreenId();
                                // Save the screen id for binding in the workspace
                                workspaceScreens.add(screenId);
                                addedWorkspaceScreensFinal.add(screenId);
                                numPagesToAdd--;
                            }

                            // Find the coordinate again
                            coords = LauncherModel.findNextAvailableIconSpace(context,
                                    name, launchIntent, startSearchPageIndex, workspaceScreens);
                        }
                        if (coords == null) {
                            throw new RuntimeException("Coordinates should not be null");
                        }

                        ShortcutInfo shortcutInfo;
                        if (a instanceof ShortcutInfo) {
                            shortcutInfo = (ShortcutInfo) a;
                        } else if (a instanceof AppInfo) {
                            shortcutInfo = ((AppInfo) a).makeShortcut();
                        } else {
                            throw new RuntimeException("Unexpected info type");
                        }
                        if(shortcutInfo.getIntent()!=null&&shortcutInfo.getIntent().getComponent()!=null) {
                        	shortcutInfo.packageName=	shortcutInfo.getIntent().getComponent().getPackageName();
                        }
                        // Add the shortcut to the db
                        addItemToDatabase(context, shortcutInfo,
                                LauncherSettings.Favorites.CONTAINER_DESKTOP,
                                coords.first, coords.second[0], coords.second[1], false);
//                        String packageName = shortcutInfo.getIntent().getComponent().getPackageName();

                        // Save the ShortcutInfo for binding in the workspace
                        addedShortcutsFinal.add(shortcutInfo);
                    }

         			sp.edit().putBoolean("load_default_res_loadeds", true).commit();
                }

                // Update the workspace screens
                updateWorkspaceScreenOrder(context, workspaceScreens);
                final StackTraceElement[] stackTrace = new Throwable().getStackTrace();

                /// M: allAppsApps may be null
                if (!addedShortcutsFinal.isEmpty() || (allAppsApps != null && !allAppsApps.isEmpty())) {
                    runOnMainThread(new Runnable() {
                        public void run() {
                            Callbacks cb = mCallbacks != null ? mCallbacks.get() : null;
                            if (callbacks == cb && cb != null) {
                                final ArrayList<ItemInfo> addAnimated = new ArrayList<ItemInfo>();
                                final ArrayList<ItemInfo> addNotAnimated = new ArrayList<ItemInfo>();
                                if (!addedShortcutsFinal.isEmpty()) {
                                    ItemInfo info = addedShortcutsFinal.get(addedShortcutsFinal.size() - 1);
                                    long lastScreenId = info.screenId;
                                    for (ItemInfo i : addedShortcutsFinal) {
                                        if (i.screenId == lastScreenId) {
                                            addAnimated.add(i);

                                            RuntimeException e = new RuntimeException("title"+i.title);
                                            if(stackTrace!=null) {
                                            	e.setStackTrace(stackTrace);
                                            }
                                        } else {
                                            addNotAnimated.add(i);
                                        }
                                    }
                                }


                                callbacks.bindAppsAdded(addedWorkspaceScreensFinal,
                                        addNotAnimated, addAnimated, allAppsApps);
                            }
                        }
                    });
                }
            }
        };
        runOnWorkerThread(r);
    }

    public Bitmap getFallbackIcon() {
        return Bitmap.createBitmap(mDefaultIcon);
    }

    public void unbindItemInfosAndClearQueuedBindRunnables() {
        if (sWorkerThread.getThreadId() == Process.myTid()) {
            throw new RuntimeException("Expected unbindLauncherItemInfos() to be called from the " +
                    "main thread");
        }

        // Clear any deferred bind runnables
        mDeferredBindRunnables.clear();
        // Remove any queued bind runnables
        mHandler.cancelAllRunnablesOfType(MAIN_THREAD_BINDING_RUNNABLE);
        // Unbind all the workspace items
        unbindWorkspaceItemsOnMainThread();
    }

    /** Unbinds all the sBgWorkspaceItems and sBgAppWidgets on the main thread */
    void unbindWorkspaceItemsOnMainThread() {
        // Ensure that we don't use the same workspace items data structure on the main thread
        // by making a copy of workspace items first.
        final ArrayList<ItemInfo> tmpWorkspaceItems = new ArrayList<ItemInfo>();
        final ArrayList<ItemInfo> tmpAppWidgets = new ArrayList<ItemInfo>();
        synchronized (sBgLock) {
            tmpWorkspaceItems.addAll(sBgWorkspaceItems);
            tmpAppWidgets.addAll(sBgAppWidgets);
        }
        Runnable r = new Runnable() {
                @Override
                public void run() {
                   for (ItemInfo item : tmpWorkspaceItems) {
                       item.unbind();
                   }
                   for (ItemInfo item : tmpAppWidgets) {
                       item.unbind();
                   }
                }
            };
        runOnMainThread(r);
    }

    /**
     * Adds an item to the DB if it was not created previously, or move it to a new
     * <container, screen, cellX, cellY>
     */
    static void addOrMoveItemInDatabase(Context context, ItemInfo item, long container,
            long screenId, int cellX, int cellY) {
        if (item.container == ItemInfo.NO_ID) {
            // From all apps
            addItemToDatabase(context, item, container, screenId, cellX, cellY, false);
        } else {
            // From somewhere else
            moveItemInDatabase(context, item, container, screenId, cellX, cellY);
        }
    }

    static void checkItemInfoLocked(
            final long itemId, final ItemInfo item, StackTraceElement[] stackTrace) {
        ItemInfo modelItem = sBgItemsIdMap.get(itemId);
        if (modelItem != null && item != modelItem) {
            // check all the data is consistent
            if (modelItem instanceof ShortcutInfo && item instanceof ShortcutInfo) {
            	try {
                    ShortcutInfo modelShortcut = (ShortcutInfo) modelItem;
                    ShortcutInfo shortcut = (ShortcutInfo) item;
                    if (modelShortcut.title.toString().equals(shortcut.title.toString()) &&
                            modelShortcut.intent.filterEquals(shortcut.intent) &&
                            modelShortcut.id == shortcut.id &&
                            modelShortcut.itemType == shortcut.itemType &&
                            modelShortcut.container == shortcut.container &&
                            modelShortcut.screenId == shortcut.screenId &&
                            modelShortcut.cellX == shortcut.cellX &&
                            modelShortcut.cellY == shortcut.cellY &&
                            modelShortcut.spanX == shortcut.spanX &&
                            modelShortcut.spanY == shortcut.spanY &&
                            ((modelShortcut.dropPos == null && shortcut.dropPos == null) ||
                            (modelShortcut.dropPos != null &&
                                    shortcut.dropPos != null &&
                                    modelShortcut.dropPos[0] == shortcut.dropPos[0] &&
                            modelShortcut.dropPos[1] == shortcut.dropPos[1]))) {
                        // For all intents and purposes, this is the same object
                        return;
                    }
				} catch (Exception e) {
					// TODO: handle exception
				}
            }

            // the modelItem needs to match up perfectly with item if our model is
            // to be consistent with the database-- for now, just require
            // modelItem == item or the equality check above
            String msg = "item: " + ((item != null) ? item.toString() : "null") +
                    "modelItem: " +
                    ((modelItem != null) ? modelItem.toString() : "null") +
                    "Error: ItemInfo passed to checkItemInfo doesn't match original";
            RuntimeException e = new RuntimeException(msg);
            if (stackTrace != null) {
                e.setStackTrace(stackTrace);
            }
            // TODO: something breaks this in the upgrade path
            //throw e;
        }
    }

    static void checkItemInfo(final ItemInfo item) {
        final StackTraceElement[] stackTrace = new Throwable().getStackTrace();
        final long itemId = item.id;
        Runnable r = new Runnable() {
            public void run() {
                synchronized (sBgLock) {
                    checkItemInfoLocked(itemId, item, stackTrace);
                }
            }
        };
        runOnWorkerThread(r);
    }

	static void updateItemInDatabaseHelper(Context context,
			final ContentValues values, final int itemId,
			final String callingFunction) {
		final Uri uri = LauncherSettings.Favorites.getContentUri(itemId, false);
		final ContentResolver cr = context.getContentResolver();
	/*	int t = db.update(AllApps.TABLE_FAVORITES, values,
				Favorites._ID + "=?", new String[] { String.valueOf(itemId) });*/
        Runnable r = new Runnable() {
            public void run() {
                int t = cr.update(uri, values, null, null);
            }
        };
        runOnWorkerThread(r);
		

	}

    static void updateItemInDatabaseHelper(Context context, final ContentValues values,
            final ItemInfo item, final String callingFunction) {
        final long itemId = item.id;
        final Uri uri = LauncherSettings.Favorites.getContentUri(itemId, false);
        final ContentResolver cr = context.getContentResolver();

        if (DEBUG_MODEL) {
            LauncherLog.d(TAG, "updateItemInDatabaseHelper values = " + values + ", item = " + item);
        }

        final StackTraceElement[] stackTrace = new Throwable().getStackTrace();
        Runnable r = new Runnable() {
            public void run() {
                int t = cr.update(uri, values, null, null);

                Log.d("zhouerlong", "updateItemInDatabaseHelper values = " + values + ", item = " + item+"    t:"+t);
                updateItemArrays(item, itemId, stackTrace);
            }
        };
        runOnWorkerThread(r);
    }
    
    
    static void updateItemInDatabaseHelperLanguage(Context context, final ContentValues values,
            final ItemInfo item, final String callingFunction) {
        final long itemId = item.id;
        final Uri uri = LauncherSettings.Favorites.getContentUri(itemId, false);
        final ContentResolver cr = context.getContentResolver();
                Runnable r = new Runnable() {
                    public void run() {
                        int t = cr.update(uri, values, null, null);
                    }
                };
                runOnWorkerThread(r);
    }

    static void updateItemsInDatabaseHelper(Context context, final ArrayList<ContentValues> valuesList,
            final ArrayList<ItemInfo> items, final String callingFunction) {
        final ContentResolver cr = context.getContentResolver();

        final StackTraceElement[] stackTrace = new Throwable().getStackTrace();
        Runnable r = new Runnable() {
            public void run() {
                ArrayList<ContentProviderOperation> ops =
                        new ArrayList<ContentProviderOperation>();
                int count = items.size();
                for (int i = 0; i < count; i++) {
                    ItemInfo item = items.get(i);
                    final long itemId = item.id;
                    final Uri uri = LauncherSettings.Favorites.getContentUri(itemId, false);
                    ContentValues values = valuesList.get(i);

                    ops.add(ContentProviderOperation.newUpdate(uri).withValues(values).build());
                    updateItemArrays(item, itemId, stackTrace);

                }
                try {
                    cr.applyBatch(LauncherProvider.AUTHORITY, ops);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        runOnWorkerThread(r);
    }

    static void updateItemArrays(ItemInfo item, long itemId, StackTraceElement[] stackTrace) {
        // Lock on mBgLock *after* the db operation
        synchronized (sBgLock) {
            checkItemInfoLocked(itemId, item, stackTrace);

            if (item.container != LauncherSettings.Favorites.CONTAINER_DESKTOP &&
                    item.container != LauncherSettings.Favorites.CONTAINER_HOTSEAT) {
                // Item is in a folder, make sure this folder exists
                if (!sBgFolders.containsKey(item.container)) {
                    // An items container is being set to a that of an item which is not in
                    // the list of Folders.
                    String msg = "item: " + item + " container being set to: " +
                            item.container + ", not in the list of folders";
                    Log.e(TAG, msg);
                    Launcher.dumpDebugLogsToConsole();
                }
            }

            // Items are added/removed from the corresponding FolderInfo elsewhere, such
            // as in Workspace.onDrop. Here, we just add/remove them from the list of items
            // that are on the desktop, as appropriate
            ItemInfo modelItem = sBgItemsIdMap.get(itemId);
            /// M: [ALPS01464916] Check if modelItem is null before using it.
            if (modelItem != null &&
                    (modelItem.container == LauncherSettings.Favorites.CONTAINER_DESKTOP ||
                    modelItem.container == LauncherSettings.Favorites.CONTAINER_HOTSEAT)) {
                switch (modelItem.itemType) {
                    case LauncherSettings.Favorites.ITEM_TYPE_APPLICATION:
                    case LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT:
                    case LauncherSettings.Favorites.ITEM_TYPE_FOLDER:
                        if (!sBgWorkspaceItems.contains(modelItem)) {
                            sBgWorkspaceItems.add(modelItem);
                        }
                        break;
                    default:
                        break;
                }
            } else {
                sBgWorkspaceItems.remove(modelItem);
            }
        }
    }

    public void flushWorkerThread() {
        mFlushingWorkerThread = true;
        Runnable waiter = new Runnable() {
                public void run() {
                    synchronized (this) {
                        notifyAll();
                        mFlushingWorkerThread = false;
                    }
                }
            };

        synchronized(waiter) {
            runOnWorkerThread(waiter);
            if (mLoaderTask != null) {
                synchronized(mLoaderTask) {
                    mLoaderTask.notify();
                }
            }
            boolean success = false;
            while (!success) {
                try {
                    waiter.wait();
                    success = true;
                } catch (InterruptedException e) {
                }
            }
        }
    }

    /**
     * Move an item in the DB to a new <container, screen, cellX, cellY>
     */
    static void moveItemInDatabase(Context context, final ItemInfo item, final long container,
            final long screenId, final int cellX, final int cellY) {
        String transaction = "DbDebug    Modify item (" + item.title + ") in db, id: " + item.id +
                " (" + item.container + ", " + item.screenId + ", " + item.cellX + ", " + item.cellY +
                ") --> " + "(" + container + ", " + screenId + ", " + cellX + ", " + cellY + ")";
        Launcher.sDumpLogs.add(transaction);
        Log.d(TAG, transaction);

        if (DEBUG_MODEL) {
            LauncherLog.d(TAG, "moveItemInDatabase: item = " + item + ", container = " + container + ", screenId = " + screenId
                    + ", cellX = " + cellX + ", cellY = " + cellY + ", context = " + context);
        }

        item.container = container;
        item.cellX = cellX;
        item.cellY = cellY;

        // We store hotseat items in canonical form which is this orientation invariant position
        // in the hotseat
        if (context instanceof Launcher && screenId < 0 &&
                container == LauncherSettings.Favorites.CONTAINER_HOTSEAT) {
            item.screenId = ((Launcher) context).getHotseat().getOrderInHotseat(cellX, cellY);
        } else {
            item.screenId = screenId;
        }

        final ContentValues values = new ContentValues();
        values.put(LauncherSettings.Favorites.CONTAINER, item.container);
        values.put(LauncherSettings.Favorites.CELLX, item.cellX);
        values.put(LauncherSettings.Favorites.CELLY, item.cellY);
        values.put(LauncherSettings.Favorites.SCREEN, item.screenId);
        values.put(LauncherSettings.Favorites.FIRST_INSTALL, item.firstInstall);
        values.put(LauncherSettings.Favorites.RECOMMD_DIS, item.recommd_dis);
        values.put(LauncherSettings.Favorites.CONFIG_NAME, item.configName);
        values.put(LauncherSettings.Favorites.ICON_URI, item.iconUri);
        if(item instanceof ShortcutInfo) {
        	ShortcutInfo info = (ShortcutInfo) item;
        	if(info.intent !=null&&info.intent.getComponent()!=null)
            values.put(LauncherSettings.Favorites.PACKAGE_NAME, info.intent.getComponent().getPackageName());
        }
        values.put(LauncherSettings.Favorites.DOWN_PROGRESS, item.progress);
        values.put(LauncherSettings.Favorites.FROM_APPSTORE, item.fromAppStore);
        values.put(LauncherSettings.Favorites.DOWN_STATE, item.down_state);
        values.put(LauncherSettings.Favorites.HIDE, item.hide);

        updateItemInDatabaseHelper(context, values, item, "moveItemInDatabase");
    }
    
    
    /*static void queryItemsInDatabaseByconfigName() {
        final ContentResolver cr = context.getContentResolver();
        boolean result = false;
        if (intent !=null && title !=null) {
        Cursor c = cr.query(LauncherSettings.Favorites.CONTENT_URI,
            new String[] { BaseColumns._ID }, "name=? and intent=?",
            new String[] { title, intent.toUri(0) }, null);
        try {
            result = c.moveToFirst();
        } finally {
            c.close();
        }
        }
        return result;
    
    	
    }*/

    /**
     * Move items in the DB to a new <container, screen, cellX, cellY>. We assume that the
     * cellX, cellY have already been updated on the ItemInfos.
     */
    static void moveItemsInDatabase(Context context, final ArrayList<ItemInfo> items,
            final long container, final int screen) {

        ArrayList<ContentValues> contentValues = new ArrayList<ContentValues>();
        int count = items.size();

        for (int i = 0; i < count; i++) {
            ItemInfo item = items.get(i);
            item.container = container;

            // We store hotseat items in canonical form which is this orientation invariant position
            // in the hotseat
            if (context instanceof Launcher && screen < 0 &&
                    container == LauncherSettings.Favorites.CONTAINER_HOTSEAT) {
                item.screenId = ((Launcher) context).getHotseat().getOrderInHotseat(item.cellX,
                        item.cellY);
            } else {
                item.screenId = screen;
            }

            final ContentValues values = new ContentValues();
            values.put(LauncherSettings.Favorites.CONTAINER, item.container);
            values.put(LauncherSettings.Favorites.CELLX, item.cellX);
            values.put(LauncherSettings.Favorites.CELLY, item.cellY);
            values.put(LauncherSettings.Favorites.SCREEN, item.screenId);

            contentValues.add(values);
        }
        updateItemsInDatabaseHelper(context, contentValues, items, "moveItemInDatabase");
    }

    /**
     * Move and/or resize item in the DB to a new <container, screen, cellX, cellY, spanX, spanY>
     */
    static void modifyItemInDatabase(Context context, final ItemInfo item, final long container,
            final long screenId, final int cellX, final int cellY, final int spanX, final int spanY) {
        String transaction = "DbDebug    Modify item (" + item.title + ") in db, id: " + item.id +
                " (" + item.container + ", " + item.screenId + ", " + item.cellX + ", " + item.cellY +
                ") --> " + "(" + container + ", " + screenId + ", " + cellX + ", " + cellY + ")";
        Launcher.sDumpLogs.add(transaction);
        Log.d(TAG, transaction);

        if (DEBUG_MODEL) {
            LauncherLog.d(TAG, "modifyItemInDatabase: item = " + item + ", container = " + container + ", screenId = " + screenId
                    + ", cellX = " + cellX + ", cellY = " + cellY + ", spanX = " + spanX + ", spanY = " + spanY);
        }
        if (context instanceof Launcher) {
        	Launcher l = (Launcher) context;
        	l.updateChildContent((int)screenId);
        }
        item.container = container;
        item.cellX = cellX;
        item.cellY = cellY;
        item.spanX = spanX;
        item.spanY = spanY;

        // We store hotseat items in canonical form which is this orientation invariant position
        // in the hotseat
        if (context instanceof Launcher && screenId < 0 &&
                container == LauncherSettings.Favorites.CONTAINER_HOTSEAT) {

        	if(cellX>(((Launcher) context).getHotseat().getLayout().getCountX()-1)) {
        		return ;
        	}
            item.screenId = ((Launcher) context).getHotseat().getOrderInHotseat(cellX, cellY);
        } else {
            item.screenId = screenId;
        }

        final ContentValues values = new ContentValues();
        values.put(LauncherSettings.Favorites.CONTAINER, item.container);
        values.put(LauncherSettings.Favorites.CELLX, item.cellX);
        values.put(LauncherSettings.Favorites.CELLY, item.cellY);
        values.put(LauncherSettings.Favorites.SPANX, item.spanX);
        values.put(LauncherSettings.Favorites.SPANY, item.spanY);
        values.put(LauncherSettings.Favorites.SCREEN, item.screenId);

        updateItemInDatabaseHelper(context, values, item, "modifyItemInDatabase");
    }
    
    
    /**
     * Update an item to the database in a specified container.
     */
    static void modifyItemInDatabaseByFirstInstall(Context context, final ItemInfo item) {
        if (DEBUG_MODEL) {
            LauncherLog.d(TAG, "updateItemInDatabase: item = " + item);
        }

        final ContentValues values = new ContentValues();
        item.updateValuesWithFirstInstall(values, item.progress);
        updateItemInDatabaseHelper(context, values, item, "updateItemInDatabase");
    }
    
    
    
    /**
     * Update an item to the database in a specified container.
     */
   public static void modifyItemInDatabaseByRecommdDis(Context context, final ItemInfo item) {
        if (DEBUG_MODEL) {
            LauncherLog.d(TAG, "updateItemInDatabase: item = " + item);
        }

        final ContentValues values = new ContentValues();
        item.updateValuesRecommdDis(values, item.recommd_dis);
        updateItemInDatabaseHelper(context, values, item, "updateItemInDatabase");
    }
    /**
     * Update an item to the database in a specified container.
     */
    static void modifyItemInDatabaseByLanguage(Context context, final ItemInfo item) {
        if (DEBUG_MODEL) {
            LauncherLog.d(TAG, "updateItemInDatabase: item = " + item);
        }

        final ContentValues values = new ContentValues();
        values.put(LauncherSettings.Favorites.TITLE, item.title.toString());
        updateItemInDatabaseHelperLanguage(context, values, item, "updateItemInDatabase");
    }
    
    /**
     * Update an item to the database in a specified container.
     */
    static void modifyItemInDatabaseByHide(Context context, final ItemInfo item) {
        if (DEBUG_MODEL) {
            LauncherLog.d(TAG, "updateItemInDatabase: item = " + item);
        }

        final ContentValues values = new ContentValues();
        values.put(LauncherSettings.Favorites.HIDE, item.hide);
        updateItemInDatabaseHelper(context, values, item, "updateItemInDatabase");
    }
    
    /**
     * Update an item to the database in a specified container.
     */
    static void modifyItemInDatabaseByDownLoadDate(Context context, final ItemInfo item) {
        if (DEBUG_MODEL) {
            LauncherLog.d(TAG, "updateItemInDatabase: item = " + item);
        }

        final ContentValues values = new ContentValues();

        values.put(LauncherSettings.Favorites.DOWN_PROGRESS, item.progress);
//        values.put(LauncherSettings.Favorites.TITLE, (String) item.title);
        values.put(LauncherSettings.Favorites.DOWN_STATE, item.down_state);
        updateItemInDatabaseHelper(context, values, item, "updateItemInDatabase");
    }

    /**
     * Update an item to the database in a specified container.
     */
    static void updateItemInDatabase(Context context, final ItemInfo item) {
        if (DEBUG_MODEL) {
            LauncherLog.d(TAG, "updateItemInDatabase: item = " + item);
        }

        final ContentValues values = new ContentValues();
        item.onAddToDatabase(values);
        item.updateValuesWithCoordinates(values, item.cellX, item.cellY);
        updateItemInDatabaseHelper(context, values, item, "updateItemInDatabase");
    }
    
    /**
     * Returns true if the shortcuts already exists in the database.
     * we identify a shortcut by its title and intent.
     */
    static boolean shortcutExists1(Context context, String title, Intent intent) {
        final ContentResolver cr = context.getContentResolver();
        final Intent intentWithPkg, intentWithoutPkg;

        if (intent.getComponent() != null) {
            // If component is not null, an intent with null package will produce
            // the same result and should also be a match.
            if (intent.getPackage() != null) {
                intentWithPkg = intent;
                intentWithoutPkg = new Intent(intent).setPackage(null);
            } else {
                intentWithPkg = new Intent(intent).setPackage(
                        intent.getComponent().getPackageName());
                intentWithoutPkg = intent;
            }
        } else {
            intentWithPkg = intent;
            intentWithoutPkg = intent;
        }
        

    	String projection [] = new String[] {};
    	String selection;
    	String selectionArg [] = new String[] {};
		projection = new String[] { "title", "intent" };
		selection = "title=?  and (intent=? or intent=?)";
		selectionArg = new String[] { title, intentWithPkg.toUri(0), intentWithoutPkg.toUri(0) };
		
		
        if (Launcher.isSupportClone&&intent.getAppInstanceIndex() == 1) {
			projection = new String[] { "title", "intent" };
			selection = "title=? and intent=?";
			selectionArg = new String[] { title, intent.toUri(0) };
			
			
		}
    Cursor c = cr.query(LauncherSettings.Favorites.CONTENT_URI,
    		projection, selection,
    		selectionArg, null);
        boolean result = false;
        try {
            result = c.moveToFirst();
        } finally {
            c.close();
        }
        return result;
    }

    /**
     * Returns true if the shortcuts already exists in the database.
     * we identify a shortcut by its title and intent.
     */
    static boolean shortcutExists(Context context, String title, Intent intent) {
        final ContentResolver cr = context.getContentResolver();
        boolean result = false;
        if (intent !=null && title !=null) {
        	String projection [] = new String[] {};
        	String selection;
        	String selectionArg [] = new String[] {};
        	if(intent.getComponent()!=null) {
            	 projection = new String[] {"componentname"};
            	 selection ="componentname=?";
            	 selectionArg = new String[] {intent.getComponent().toString()};
        	}else {
				projection = new String[] { "title", "intent" };
				selection = "title=? and intent=?";
				selectionArg = new String[] { title, intent.toUri(0) };
        	}
			if (Launcher.isSupportClone&&intent.getAppInstanceIndex() == 1) {
				projection = new String[] { "title", "intent" };
				selection = "title=? and intent=?";
				selectionArg = new String[] { title, intent.toUri(0) };
				
				
			}
        Cursor c = cr.query(LauncherSettings.Favorites.CONTENT_URI,
        		projection, selection,
        		selectionArg, null);
        try {
            result = c.moveToFirst();
            if(result) {
            	LogUtils.i("zhouerlong", "title:"+title);
            }
        } finally {
        	try {
                c.close();
			} catch (Exception e) {
				// TODO: handle exception
				}
        }
        }
        return result;
    }
    
    
    /**
     * Returns true if the shortcuts already exists in the database.
     * we identify a shortcut by its title and intent.
     */
	public static String loadAllAppPkgs(Context context) {
		final ContentResolver cr = context.getContentResolver();
		Cursor cursor = null;

		StringBuffer sql = new StringBuffer();
		sql.append("select package_name from favorites");
		StringBuffer pkgs = new StringBuffer();
		try {
			
			cursor = cr.query(LauncherSettings.Favorites.CONTENT_URI, new
			  String[]{"package_name"}, null, null, null);
			 
		/*	Cursor cursor = LauncherApplication.getDbManager().execQuery(
					sql.toString());*/

			while (cursor.moveToNext()) {
				String pkg = cursor.getString(0);
				if (pkg != null) {
					pkgs.append(pkg);
					pkgs.append(",");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			// TODO: handle exception
		} finally {
			if (cursor != null)
				cursor.close();
		}
		return pkgs.toString();
	}
    
    
    /**
     * Returns true if the shortcuts already exists in the database.
     * we identify a shortcut by its title and intent.
     */
	static int getHotseats(Context context) {
		final ContentResolver cr = context.getContentResolver();
		int result = 0;
		String selection = "container = ?";
		String[] selectionArg = new String[] { String
				.valueOf(LauncherSettings.Favorites.CONTAINER_HOTSEAT) };
		Cursor c = cr.query(LauncherSettings.Favorites.CONTENT_URI, null,
				selection, selectionArg, null);
		try {
			result = c.getCount();
		} finally {
        	try {
                c.close();
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
		return result;
	}
    
    
    /**
     * Returns true if the shortcuts already exists in the database.
     * we identify a shortcut by its title and intent.
     */
    static boolean shortcutExists(Context context,String title) {
        final ContentResolver cr = context.getContentResolver();
        boolean result = false;
        if (title !=null ) {
        	Cursor c = cr.query(LauncherSettings.Favorites.CONTENT_URI,
                    new String[] {  "title" }, " title=?",
                    new String[] {  title }, null);
        try {
            result = c.moveToFirst();//c.moveToFirst();
            
        } finally {
            c.close();
        }
        }
        return result;
    }
    
    
    /**
     * Returns true if the shortcuts already exists in the database.
     * we identify a shortcut by its title and intent.
     */
	public static int findRecommdDisDb(Context context, String id) {
		final ContentResolver cr = context.getContentResolver();
		boolean result = false;
		Cursor c = cr.query(LauncherSettings.Favorites.CONTENT_URI,
				new String[] { "recommd_dis" }, " _id=?", new String[] { id },
				null);
		try {
			result = c.moveToFirst();// c.moveToFirst();
			if (result) {
				return c.getInt(0);
			}

		} finally {
			try {
				c.close();
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
		return -1;
	}
    
    
    /**
     * Returns true if the shortcuts already exists in the database.
     * we identify a shortcut by its title and intent.
     */
    static boolean shortcutExistsWithPkg(Context context,String pkg) {
        final ContentResolver cr = context.getContentResolver();
        boolean result = false;
        if (pkg !=null ) {
        	Cursor c = cr.query(LauncherSettings.Favorites.CONTENT_URI,
                    new String[] {  "package_name" }, " package_name=?",
                    new String[] {  pkg }, null);
        try {
            result = c.moveToFirst();//c.moveToFirst();
            
        } finally {
        	try {
                c.close();
			} catch (Exception e) {
				// TODO: handle exception
			}
        }
        }
        return result;
    }
    
    
    /**
     * Returns true if the shortcuts already exists in the database.
     * we identify a shortcut by its title and intent.
     */
	static boolean downloadExists(Context context, String title, String pkg) {
		final ContentResolver cr = context.getContentResolver();
		boolean result = false;
		if (title != null && pkg != null) {

			String selection = " title=? and package_name=? and from_appstore=?";

//			String selection = "  package_name=? and from_appstore=?";
//			String[] selectionArg = new String[] { title, pkg, "1" };
			String[] selectionArg = new String[] { title, pkg, "1" };
			Cursor c = cr.query(LauncherSettings.Favorites.CONTENT_URI,
					new String[] { "title" }, selection, selectionArg, null);
			try {
				result = c.moveToFirst();// c.moveToFirst();

			} catch (Exception e) {

			} finally {
				if (c != null)
					c.close();
			}
		}
		return result;
	}
    
	/**
	 * Returns true if the shortcuts already exists in the database. we identify
	 * a shortcut by its title and intent.
	 */
	static boolean shortcutExistsFromAppStore(Context context, String title,int fromAppstore,String pkg) {
		final ContentResolver cr = context.getContentResolver();
		boolean result = false;
		if(title ==null) {
			return result;
		}
		if(pkg==null) {
			return result;
		}
		if(Utilities.checkApkExist(context, pkg)) {
			return true;
		}
		
		Log.i("zhouerlong", "title+shortcutExistsFromAppStore"+title);
		Cursor c = cr.query(LauncherSettings.Favorites.CONTENT_URI,
				new String[] {"from_appstore","title" }, "from_appstore=? and title=?", new String[] { String.valueOf(fromAppstore),title },
				null);
		try {
			result = c.moveToFirst();
		} finally {
        	try {
                c.close();
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
		return result;
	}
	
	/**
	 * Returns true if the shortcuts already exists in the database. we identify
	 * a shortcut by its title and intent.
	 */
	public  boolean installedExistsFromAppStore(Context context, String pkageName) {
		final ContentResolver cr = context.getContentResolver();
		boolean result = false;
		if(pkageName ==null) {
			return result;
		}
		Cursor c = cr.query(LauncherSettings.Favorites.CONTENT_URI,
				new String[] {"package_name" }, "package_name=?", new String[] { pkageName },
				null);
		try {
			result = c.moveToFirst();
		} finally {
			c.close();
		}
		return result;
	}	/**
	 * Returns true if the shortcuts already exists in the database. we identify
	 * a shortcut by its title and intent.
	 */
	static boolean installExistsFromAppStore(Context context, String pkageName,int fromAppstore) {
		final ContentResolver cr = context.getContentResolver();
		boolean result = false;
		if(pkageName ==null) {
			return result;
		}
		Cursor c = cr.query(LauncherSettings.Favorites.CONTENT_URI,
				new String[] {"from_appstore","package_name" }, "from_appstore=? and package_name=?", new String[] { String.valueOf(fromAppstore),pkageName },
				null);
		try {
			result = c.moveToFirst();
		} finally {
			c.close();
		}
		return result;
	}

    /**
     * Returns an ItemInfo array containing all the items in the LauncherModel.
     * The ItemInfo.id is not set through this function.
     */
    static ArrayList<ItemInfo> getItemsInLocalCoordinates(Context context) {
        ArrayList<ItemInfo> items = new ArrayList<ItemInfo>();
        final ContentResolver cr = context.getContentResolver();
        Cursor c = cr.query(LauncherSettings.Favorites.CONTENT_URI, new String[] {
                LauncherSettings.Favorites.ITEM_TYPE, LauncherSettings.Favorites.CONTAINER,
                LauncherSettings.Favorites.SCREEN, LauncherSettings.Favorites.CELLX, LauncherSettings.Favorites.CELLY,
                LauncherSettings.Favorites.SPANX, LauncherSettings.Favorites.SPANY }, null, null, null);

        final int itemTypeIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.ITEM_TYPE);
        final int containerIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.CONTAINER);
        final int screenIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.SCREEN);
        final int cellXIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.CELLX);
        final int cellYIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.CELLY);
        final int spanXIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.SPANX);
        final int spanYIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.SPANY);

        try {
            while (c.moveToNext()) {
                ItemInfo item = new ItemInfo();
                item.cellX = c.getInt(cellXIndex);
                item.cellY = c.getInt(cellYIndex);
                item.spanX = Math.max(1, c.getInt(spanXIndex));
                item.spanY = Math.max(1, c.getInt(spanYIndex));
                item.container = c.getInt(containerIndex);
                item.itemType = c.getInt(itemTypeIndex);
                item.screenId = c.getInt(screenIndex);

                items.add(item);
            }
        } catch (Exception e) {
            items.clear();
        } finally {
            c.close();
        }

        return items;
    }

    /**
     * Find a folder in the db, creating the FolderInfo if necessary, and adding it to folderList.
     */
    FolderInfo getFolderById(Context context, HashMap<Long,FolderInfo> folderList, long id) {
        final ContentResolver cr = context.getContentResolver();
        Cursor c = cr.query(LauncherSettings.Favorites.CONTENT_URI, null,
                "_id=? and (itemType=? or itemType=?)",
                new String[] { String.valueOf(id),
                        String.valueOf(LauncherSettings.Favorites.ITEM_TYPE_FOLDER)}, null);

        try {
            if (c.moveToFirst()) {
                final int itemTypeIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.ITEM_TYPE);
                final int titleIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.TITLE);
                final int containerIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.CONTAINER);
                final int screenIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.SCREEN);
                final int cellXIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.CELLX);
                final int cellYIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.CELLY);

                FolderInfo folderInfo = null;
                switch (c.getInt(itemTypeIndex)) {
                    case LauncherSettings.Favorites.ITEM_TYPE_FOLDER:
                        folderInfo = findOrMakeFolder(folderList, id);
                        break;
                }

                folderInfo.title = c.getString(titleIndex);
                folderInfo.id = id;
                folderInfo.container = c.getInt(containerIndex);
                folderInfo.screenId = c.getInt(screenIndex);
                folderInfo.cellX = c.getInt(cellXIndex);
                folderInfo.cellY = c.getInt(cellYIndex);

                return folderInfo;
            }
        } finally {
        	try {
                c.close();
			} catch (Exception e) {
				// TODO: handle exception
			}
        }

        return null;
    }

    /**
     * Add an item to the database in a specified container. Sets the container, screen, cellX and
     * cellY fields of the item. Also assigns an ID to the item.
     */
    static void addItemToDatabase(Context context, final ItemInfo item, final long container,
            final long screenId, final int cellX, final int cellY, final boolean notify) {
        if (DEBUG_MODEL) {
            LauncherLog.d(TAG, "addItemToDatabase item = " + item + ", container = " + container + ", screenId = " + screenId
                    + ", cellX " + cellX + ", cellY = " + cellY + ", notify = " + notify);
        }

        item.container = container;
        item.cellX = cellX;
        item.cellY = cellY;
        // We store hotseat items in canonical form which is this orientation invariant position
        // in the hotseat
        if (context instanceof Launcher && screenId < 0 &&
                container == LauncherSettings.Favorites.CONTAINER_HOTSEAT) {
            item.screenId = ((Launcher) context).getHotseat().getOrderInHotseat(cellX, cellY);
        } else {
            item.screenId = screenId;
        }
        if(item instanceof ShortcutInfo) {
        	ShortcutInfo s = (ShortcutInfo) item;
        	LogUtils.i("zhouerlong", "info:ss"+s.toString());
        }
        final ContentValues values = new ContentValues();
        final ContentResolver cr = context.getContentResolver();
        item.onAddToDatabase(values);

        item.id = LauncherAppState.getLauncherProvider().generateNewItemId();
        values.put(LauncherSettings.Favorites._ID, item.id);
        item.updateValuesWithCoordinates(values, item.cellX, item.cellY);

        final StackTraceElement[] stackTrace = new Throwable().getStackTrace();

        Runnable r = new Runnable() {
            public void run() {
                String transaction = "DbDebug    Add item (" + item.title + ") to db, id: "
                        + item.id + " (" + container + ", " + screenId + ", " + cellX + ", "
                        + cellY + ")";
                Launcher.sDumpLogs.add(transaction);
                Log.d(TAG, transaction);

              Uri url = cr.insert(notify ? LauncherSettings.Favorites.CONTENT_URI :
                        LauncherSettings.Favorites.CONTENT_URI_NO_NOTIFICATION, values);
              LogUtils.i("zhouerlong", "transaction:::::"+transaction+"    url---zzz:"+url);

                // Lock on mBgLock *after* the db operation
                synchronized (sBgLock) {
                    checkItemInfoLocked(item.id, item, null);
                    sBgItemsIdMap.put(item.id, item);
                    if (DEBUG_MODEL) {
                        LauncherLog.d(TAG, "addItemToDatabase sBgItemsIdMap.put = " + item.id + ", item = " + item);
                    }
                    switch (item.itemType) {
                        case LauncherSettings.Favorites.ITEM_TYPE_FOLDER:
                            sBgFolders.put(item.id, (FolderInfo) item);
                            // Fall through
                        case LauncherSettings.Favorites.ITEM_TYPE_APPLICATION:
                        case LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT:
                            if (item.container == LauncherSettings.Favorites.CONTAINER_DESKTOP ||
                                    item.container == LauncherSettings.Favorites.CONTAINER_HOTSEAT) {
                                sBgWorkspaceItems.add(item);
                            } else {
                                if (!sBgFolders.containsKey(item.container)) {
                                    // Adding an item to a folder that doesn't exist.
                                    String msg = "adding item: " + item + " to a folder that " +
                                            " doesn't exist";
                                    Log.e(TAG, msg);
                                    Launcher.dumpDebugLogsToConsole();
                                }
                            }
                            break;
                        case LauncherSettings.Favorites.ITEM_TYPE_APPWIDGET:
                            sBgAppWidgets.add((LauncherAppWidgetInfo) item);
                            if (DEBUG_MODEL) {
                                LauncherLog.d(TAG, "addItemToDatabase sAppWidgets.add = " + item);
                            }
                            break;
                    }
                }
            }
        };
        runOnWorkerThread(r);
    }
    
    /**
     * Add an item to the database in a specified container. Sets the container, screen, cellX and
     * cellY fields of the item. Also assigns an ID to the item.
     */
    static void addItemToWorkspace(Context context, final ItemInfo item, final long container,
            final long screenId, final int cellX, final int cellY, final boolean notify) {
        if (DEBUG_MODEL) {
            LauncherLog.d(TAG, "addItemToDatabase item = " + item + ", container = " + container + ", screenId = " + screenId
                    + ", cellX " + cellX + ", cellY = " + cellY + ", notify = " + notify);
        }

        item.container = container;
        item.cellX = cellX;
        item.cellY = cellY;
        // We store hotseat items in canonical form which is this orientation invariant position
        // in the hotseat
        if (context instanceof Launcher && screenId < 0 &&
                container == LauncherSettings.Favorites.CONTAINER_HOTSEAT) {
            item.screenId = ((Launcher) context).getHotseat().getOrderInHotseat(cellX, cellY);
        } else {
            item.screenId = screenId;
        }

        final ContentValues values = new ContentValues();
        final ContentResolver cr = context.getContentResolver();
        item.onAddToDatabase(values);

        item.id = LauncherAppState.getLauncherProvider().generateNewItemId();
        values.put(LauncherSettings.Favorites._ID, item.id);
        item.updateValuesWithCoordinates(values, item.cellX, item.cellY);

        final StackTraceElement[] stackTrace = new Throwable().getStackTrace();

        Runnable r = new Runnable() {
            public void run() {
                String transaction = "DbDebug    Add item (" + item.title + ") to db, id: "
                        + item.id + " (" + container + ", " + screenId + ", " + cellX + ", "
                        + cellY + ")";
                Launcher.sDumpLogs.add(transaction);
                Log.d(TAG, transaction);

//                cr.insert(notify ? LauncherSettings.Favorites.CONTENT_URI :
//                        LauncherSettings.Favorites.CONTENT_URI_NO_NOTIFICATION, values);

                // Lock on mBgLock *after* the db operation
                synchronized (sBgLock) {
                    checkItemInfoLocked(item.id, item, null);
                    sBgItemsIdMap.put(item.id, item);
                    if (DEBUG_MODEL) {
                        LauncherLog.d(TAG, "addItemToDatabase sBgItemsIdMap.put = " + item.id + ", item = " + item);
                    }
                    switch (item.itemType) {
                        case LauncherSettings.Favorites.ITEM_TYPE_FOLDER:
                            sBgFolders.put(item.id, (FolderInfo) item);
                            // Fall through
                        case LauncherSettings.Favorites.ITEM_TYPE_APPLICATION:
                        case LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT:
                            if (item.container == LauncherSettings.Favorites.CONTAINER_DESKTOP ||
                                    item.container == LauncherSettings.Favorites.CONTAINER_HOTSEAT) {
                                sBgWorkspaceItems.add(item);
                            } else {
                                if (!sBgFolders.containsKey(item.container)) {
                                    // Adding an item to a folder that doesn't exist.
                                    String msg = "adding item: " + item + " to a folder that " +
                                            " doesn't exist";
                                    Log.e(TAG, msg);
                                    Launcher.dumpDebugLogsToConsole();
                                }
                            }
                            break;
                        case LauncherSettings.Favorites.ITEM_TYPE_APPWIDGET:
                            sBgAppWidgets.add((LauncherAppWidgetInfo) item);
                            if (DEBUG_MODEL) {
                                LauncherLog.d(TAG, "addItemToDatabase sAppWidgets.add = " + item);
                            }
                            break;
                    }
                }
            }
        };
        runOnWorkerThread(r);
    }

    /**
     * Creates a new unique child id, for a given cell span across all layouts.
     */
    static int getCellLayoutChildId(
            long container, long screen, int localCellX, int localCellY, int spanX, int spanY) {
        return (((int) container & 0xFF) << 24)
                | ((int) screen & 0xFF) << 16 | (localCellX & 0xFF) << 8 | (localCellY & 0xFF);
    }
    static void deleteDatabase(Context context, final Uri uri) {
    	final ContentResolver cr = context.getContentResolver();
    	Runnable r = new Runnable() {
			
			@Override
			public void run() {
		    	cr.delete(uri, null, null);
			}
		};
		
		runOnWorkerThread(r);
    }
    /**
     * Removes the specified item from the database
     * @param context
     * @param item
     */
    static void deleteItemFromDatabase(Context context, final ItemInfo item) {
        if (DEBUG_MODEL) {
            LauncherLog.d(TAG, "deleteItemFromDatabase item = " + item);
        }

        final ContentResolver cr = context.getContentResolver();
        final Uri uriToDelete = LauncherSettings.Favorites.getContentUri(item.id, false);

        Runnable r = new Runnable() {
            public void run() {
                String transaction = "DbDebug    Delete item (" + item.title + ") from db, id: "
                        + item.id + " (" + item.container + ", " + item.screenId + ", " + item.cellX +
                        ", " + item.cellY + ")";
                Launcher.sDumpLogs.add(transaction);
                Log.d(TAG, transaction);

                cr.delete(uriToDelete, null, null);

                // Lock on mBgLock *after* the db operation
                synchronized (sBgLock) {
                    switch (item.itemType) {
                        case LauncherSettings.Favorites.ITEM_TYPE_FOLDER:
                            sBgFolders.remove(item.id);
                            for (ItemInfo info: sBgItemsIdMap.values()) {
                                if (info.container == item.id) {
                                    // We are deleting a folder which still contains items that
                                    // think they are contained by that folder.
                                    String msg = "deleting a folder (" + item + ") which still " +
                                            "contains items (" + info + ")";
                                    Log.e(TAG, msg);
                                    Launcher.dumpDebugLogsToConsole();
                                }
                            }
                            sBgWorkspaceItems.remove(item);
                            break;
                        case LauncherSettings.Favorites.ITEM_TYPE_APPLICATION:
                        case LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT:
                            sBgWorkspaceItems.remove(item);
                            break;
                        case LauncherSettings.Favorites.ITEM_TYPE_APPWIDGET:
                            sBgAppWidgets.remove((LauncherAppWidgetInfo) item);
                            break;
                    }
                    sBgItemsIdMap.remove(item.id);
                    sBgDbIconCache.remove(item);
                }
                if (DEBUG_MODEL) {
                    LauncherLog.d(TAG, "deleteItemFromDatabase sAppWidgets.remove = " + item
                            + ", sItemsIdMap.remove = " + item.id);
                }
            }
        };
        runOnWorkerThread(r);
    }

    
    
	/**
	 * Removes the specified item from the database
	 * 
	 * @param context
	 * @param item
	 */
	public void deleteItemFromDatabase(final Context context,
			final Intent intent, final String name, final boolean duplicate) {
		Runnable r = new Runnable() {
			public void run() {
				long id = 0;
				if (intent != null && name != null) {
					final ContentResolver cr = context.getContentResolver();
					Cursor c = cr.query(LauncherSettings.Favorites.CONTENT_URI,
							new String[] { LauncherSettings.Favorites._ID,
									LauncherSettings.Favorites.INTENT },
							LauncherSettings.Favorites.TITLE + "=?",
							new String[] { name }, null);
					final int intentIndex = c
							.getColumnIndexOrThrow(LauncherSettings.Favorites.INTENT);
					final int idIndex = c
							.getColumnIndexOrThrow(LauncherSettings.Favorites._ID);

					boolean changed = false;
					try {
						while (c.moveToNext()) {
							try {
								if (intent.filterEquals(Intent.parseUri(
										c.getString(intentIndex), 0))) {
									id = c.getLong(idIndex);
									final Uri uri = LauncherSettings.Favorites
											.getContentUri(id, false);
									cr.delete(uri, null, null);
									changed = true;
									if (!duplicate) {
										break;
									}
								}
							} catch (URISyntaxException e) {
								// Ignore
								LauncherLog
										.w(TAG,
												"URISyntaxException happened when removeShortcut.");
							}
						}
					} finally {
			        	try {
			                c.close();
						} catch (Exception e) {
							// TODO: handle exception
						}
					}

					ItemInfo item = sBgItemsIdMap.get(id);
					if (item == null) {
						return;
					}

						if(mlauncher !=null) {
							mlauncher.bindItemInforemove(item);
						}else if(context instanceof Launcher) {
							Launcher l = (Launcher) context; 
							l.bindItemInforemove(item);
						} 
					synchronized (sBgLock) {
						switch (item.itemType) {
						case LauncherSettings.Favorites.ITEM_TYPE_FOLDER:
							sBgFolders.remove(item.id);
							for (ItemInfo info : sBgItemsIdMap.values()) {
								if (info.container == item.id) {
									// We are deleting a folder which still
									// contains items that
									// think they are contained by that folder.
									String msg = "deleting a folder (" + item
											+ ") which still "
											+ "contains items (" + info + ")";
									Log.e(TAG, msg);
									Launcher.dumpDebugLogsToConsole();
								}
							}
							sBgWorkspaceItems.remove(item);
							break;
						case LauncherSettings.Favorites.ITEM_TYPE_APPLICATION:
						case LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT:
							sBgWorkspaceItems.remove(item);
							break;
						case LauncherSettings.Favorites.ITEM_TYPE_APPWIDGET:
							sBgAppWidgets.remove((LauncherAppWidgetInfo) item);
							break;
						}
						sBgItemsIdMap.remove(item.id);
						sBgDbIconCache.remove(item);
					}
				}
			}
		};
		runOnWorkerThread(r);
	}
    /**
     * Update the order of the workspace screens in the database. The array list contains
     * a list of screen ids in the order that they should appear.
     */
    void updateWorkspaceScreenOrder(Context context, final ArrayList<Long> screens) {
        final ArrayList<Long> screensCopy = new ArrayList<Long>(screens);
        final ContentResolver cr = context.getContentResolver();
        final Uri uri = LauncherSettings.WorkspaceScreens.CONTENT_URI;

        // Remove any negative screen ids -- these aren't persisted
        Iterator<Long> iter = screensCopy.iterator();
        while (iter.hasNext()) {
            long id = iter.next();
            if (id < 0) {
                iter.remove();
            }
        }

        Runnable r = new Runnable() {
            @Override
            public void run() {
                // Clear the table
            	try {
                cr.delete(uri, null, null);
                int count = screensCopy.size();
                ContentValues[] values = new ContentValues[count];
                for (int i = 0; i < count; i++) {
                    ContentValues v = new ContentValues();
                    long screenId = screensCopy.get(i);
                    v.put(LauncherSettings.WorkspaceScreens._ID, screenId);
                    v.put(LauncherSettings.WorkspaceScreens.SCREEN_RANK, i);
                    values[i] = v;
                }
                cr.bulkInsert(uri, values);
				
				} catch (Exception e) {
					e.printStackTrace();
				}

                synchronized (sBgLock) {
                    sBgWorkspaceScreens.clear();
                    sBgWorkspaceScreens.addAll(screensCopy);
                }
            }
        };
        runOnWorkerThread(r);
    }

    /**
     * Remove the contents of the specified folder from the database
     */
    static void deleteFolderContentsFromDatabase(Context context, final FolderInfo info) {
        if (DEBUG_MODEL) {
            LauncherLog.d(TAG, "deleteFolderContentsFromDatabase info = " + info);
        }

        final ContentResolver cr = context.getContentResolver();

        Runnable r = new Runnable() {
            public void run() {
                cr.delete(LauncherSettings.Favorites.getContentUri(info.id, false), null, null);
                // Lock on mBgLock *after* the db operation
                synchronized (sBgLock) {
                    sBgItemsIdMap.remove(info.id);
                    sBgFolders.remove(info.id);
                    sBgDbIconCache.remove(info);
                    sBgWorkspaceItems.remove(info);
                    if (DEBUG_MODEL) {
                        LauncherLog.d(TAG, "deleteFolderContentsFromDatabase sBgItemsIdMap.remove = " + info.id);
                    }
                }

                cr.delete(LauncherSettings.Favorites.CONTENT_URI_NO_NOTIFICATION,
                        LauncherSettings.Favorites.CONTAINER + "=" + info.id, null);
                // Lock on mBgLock *after* the db operation
                synchronized (sBgLock) {
                    for (ItemInfo childInfo : info.contents) {
                        sBgItemsIdMap.remove(childInfo.id);
                        sBgDbIconCache.remove(childInfo);
                        if (DEBUG_MODEL) {
                            LauncherLog.d(TAG, "deleteFolderContentsFromDatabase sItemsIdMap.remove = " + childInfo.id);
                        }
                    }
                }
            }
        };
        runOnWorkerThread(r);
    }

    /**
     * Set this as the current Launcher activity object for the loader.
     */
    public void initialize(Callbacks callbacks) {
        synchronized (mLock) {
            mCallbacks = new SoftReference<Callbacks>(callbacks);
        }
    }

    /**
     * Call from the handler for ACTION_PACKAGE_ADDED, ACTION_PACKAGE_REMOVED and
     * ACTION_PACKAGE_CHANGED.
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        if (DEBUG_LOADERS) Log.d(TAG, "onReceive intent=" + intent);

        final String action = intent.getAction();

        if (Intent.ACTION_PACKAGE_CHANGED.equals(action)
                || Intent.ACTION_PACKAGE_REMOVED.equals(action)
                || Intent.ACTION_PACKAGE_ADDED.equals(action)) {
            final String packageName = intent.getData().getSchemeSpecificPart();
            final boolean replacing = intent.getBooleanExtra(Intent.EXTRA_REPLACING, false);

            int op = PackageUpdatedTask.OP_NONE;

            if (packageName == null || packageName.length() == 0) {
                // they sent us a bad intent
                return;
            }
            if (packageName.equals(AppsCustomizePagedView.STK_PACKAGE_NAME)) {
    			return ;
            }
            if (Intent.ACTION_PACKAGE_CHANGED.equals(action)) {
                op = PackageUpdatedTask.OP_UPDATE;
            } else if (Intent.ACTION_PACKAGE_REMOVED.equals(action)) {
                if (!replacing) {
                    op = PackageUpdatedTask.OP_REMOVE;
                }
                // else, we are replacing the package, so a PACKAGE_ADDED will be sent
                // later, we will update the package at this time
            } else if (Intent.ACTION_PACKAGE_ADDED.equals(action)) {
                if (!replacing) {
                    op = PackageUpdatedTask.OP_ADD;
                } else {
                    op = PackageUpdatedTask.OP_UPDATE;
                }
            }

            if (op != PackageUpdatedTask.OP_NONE) {
                enqueuePackageUpdated(new PackageUpdatedTask(op, new String[] { packageName }));
            }

        } else if (Intent.ACTION_EXTERNAL_APPLICATIONS_AVAILABLE.equals(action)) {
			final boolean replacing = intent.getBooleanExtra(Intent.EXTRA_REPLACING, false);
			String[] packages = intent.getStringArrayExtra(Intent.EXTRA_CHANGED_PACKAGE_LIST);
			if (!replacing) {
				enqueuePackageUpdated(new PackageUpdatedTask(PackageUpdatedTask.OP_ADD, packages));
				if (mAppsCanBeOnRemoveableStorage) {
					// Only rebind if we support removable storage.  It catches the case where
					// apps on the external sd card need to be reloaded
					startLoaderFromBackground();
				}
			} else {
				// If we are replacing then just update the packages in the list
				enqueuePackageUpdated(new PackageUpdatedTask(PackageUpdatedTask.OP_UPDATE,
					packages));
			}
        } else if (Intent.ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE.equals(action)) {
            final boolean replacing = intent.getBooleanExtra(Intent.EXTRA_REPLACING, false);
			if (!replacing) {
				String[] packages = intent.getStringArrayExtra(Intent.EXTRA_CHANGED_PACKAGE_LIST);
				enqueuePackageUpdated(new PackageUpdatedTask(
					PackageUpdatedTask.OP_UNAVAILABLE, packages));
			}
			// else, we are replacing the packages, so ignore this event and wait for
			// EXTERNAL_APPLICATIONS_AVAILABLE to update the packages at that time
        } else if (Intent.ACTION_LOCALE_CHANGED.equals(action)) {
            // If we have changed locale we need to clear out the labels in all apps/workspace.
            LauncherLog.d(TAG, "LOCALE_CHANGED: config = " + context.getResources().getConfiguration());
            forceReload();
        } else if (Intent.ACTION_CONFIGURATION_CHANGED.equals(action)) {
             // Check if configuration change was an mcc/mnc change which would affect app resources
             // and we would need to clear out the labels in all apps/workspace. Same handling as
             // above for ACTION_LOCALE_CHANGED
             Configuration currentConfig = context.getResources().getConfiguration();
             if (mPreviousConfigMcc != currentConfig.mcc) {
                   Log.d(TAG, "Reload apps on config change. curr_mcc:"
                       + currentConfig.mcc + " prevmcc:" + mPreviousConfigMcc);
                   forceReload();
             }
             // Update previousConfig
             mPreviousConfigMcc = currentConfig.mcc;

//    		 android.os.Process.killProcess(android.os.Process.myPid());
        } else if (SearchManager.INTENT_GLOBAL_SEARCH_ACTIVITY_CHANGED.equals(action) ||
                   SearchManager.INTENT_ACTION_SEARCHABLES_CHANGED.equals(action)) {
            if (mCallbacks != null) {
                Callbacks callbacks = mCallbacks.get();
                if (callbacks != null) {
//                    callbacks.bindSearchablesChanged();
                }
            }
        }
    }
    
    public void removeAppFromAppStore(String pakcageName) {

        int  op = PackageUpdatedTask.OP_REMOVE;
        if (op != PackageUpdatedTask.OP_NONE) {
        	enqueuePackageUpdatedFromAppStroe(new PackageUpdatedTaskFromAppStore(op, new String[] { pakcageName }));
        }
    }
    
    
    public void updateDownLoadState(int state,String packageName) {
    	
    }
    
    public void updateProgressFromAppStore(DownLoadTaskInfo info) {
    	
        sWorker.post(new DownloadProgressTaskFromAppstore(info));
    }

    public void forceReload() {
        resetLoadedState(true, true);
        if (DEBUG_LOADERS) {
            Log.d(TAG, "forceReload: mLoaderTask =" + mLoaderTask + ", mAllAppsLoaded = "
                    + mAllAppsLoaded + ", mWorkspaceLoaded = " + mWorkspaceLoaded + ", this = " + this);
        }

        // Do this here because if the launcher activity is running it will be restarted.
        // If it's not running startLoaderFromBackground will merely tell it that it needs
        // to reload.
        startLoaderFromBackground();
    }

    public void resetLoadedState(boolean resetAllAppsLoaded, boolean resetWorkspaceLoaded) {
        synchronized (mLock) {
            if (LauncherLog.DEBUG_LOADER) {
                LauncherLog.d(TAG, "resetLoadedState: mLoaderTask =" + mLoaderTask
                        + ", this = " + this);
            }
            // Stop any existing loaders first, so they don't set mAllAppsLoaded or
            // mWorkspaceLoaded to true later
            stopLoaderLocked();
            if (resetAllAppsLoaded) mAllAppsLoaded = false;
            if (resetWorkspaceLoaded) mWorkspaceLoaded = false;
        }
    }

    /**
     * When the launcher is in the background, it's possible for it to miss paired
     * configuration changes.  So whenever we trigger the loader from the background
     * tell the launcher that it needs to re-run the loader when it comes back instead
     * of doing it now.
     */
    public void startLoaderFromBackground() {
        if (DEBUG_MODEL) {
            LauncherLog.d(TAG, "startLoaderFromBackground: mCallbacks = " + mCallbacks + ", this = " + this);
        }

        boolean runLoader = false;
        if (mCallbacks != null) {
            Callbacks callbacks = mCallbacks.get();
            if (DEBUG_MODEL) {
                LauncherLog.d(TAG, "startLoaderFromBackground: callbacks = " + callbacks + ", this = " + this);
            }
            if (callbacks != null) {
                if (DEBUG_MODEL) {
                    LauncherLog.d(TAG, "startLoaderFromBackground: callbacks.setLoadOnResume() = "
                            + callbacks.setLoadOnResume() + ", this = " + this);
                }
                // Only actually run the loader if they're not paused.
                if (!callbacks.setLoadOnResume()) {
                    runLoader = true;
                }
            }
        }
        if (runLoader) {
            startLoader(false, -1);
        }
    }

    // If there is already a loader task running, tell it to stop.
    // returns true if isLaunching() was true on the old task
    private boolean stopLoaderLocked() {
        boolean isLaunching = false;
        LoaderTask oldTask = mLoaderTask;
        if (oldTask != null) {
            if (oldTask.isLaunching()) {
                isLaunching = true;
            }
            oldTask.stopLocked();
        }
        if (DEBUG_LOADERS) {
            LauncherLog.d(TAG, "stopLoaderLocked: mLoaderTask =" + mLoaderTask + ", isLaunching = "
                    + isLaunching + ", this = " + this);
        }
        return isLaunching;
    }

    /**
     * @param isLaunching 是否在启动
     * @param synchronousBindPage当前页数 异步加载单页 synchronous bindPage synchronous 
     */
    public void startLoader(boolean isLaunching, int synchronousBindPage) { //这里是加载数据的入口，  启动了一个子线程
        synchronized (mLock) {
            if (DEBUG_LOADERS) {
                LauncherLog.d(TAG, "startLoader: isLaunching=" + isLaunching + ", mCallbacks = " + mCallbacks);
            }

            // Clear any deferred bind-runnables from the synchronized load process
            // We must do this before any loading/binding is scheduled below.
            mDeferredBindRunnables.clear();

            // Don't bother to start the thread if we know it's not going to do anything
            if (mCallbacks != null && mCallbacks.get() != null) {
                // If there is already one running, tell it to stop.
                // also, don't downgrade isLaunching if we're already running
                isLaunching = isLaunching || stopLoaderLocked();
                /// M: added for top package feature, load top packages from a xml file.
                AllAppsList.loadTopPackage(mApp.getContext());
                mLoaderTask = new LoaderTask(mApp.getContext(), isLaunching);
                if (DEBUG_MODEL) {
                    LauncherLog.d(TAG, "startLoader: mAllAppsLoaded = " + mAllAppsLoaded
                            + ",mWorkspaceLoaded = " + mWorkspaceLoaded + ",synchronousBindPage = "
                            + synchronousBindPage + ",mIsLoaderTaskRunning = "
                            + mIsLoaderTaskRunning + ",mLoaderTask = " + mLoaderTask,
                            new Throwable("startLoader"));
                }

                if (synchronousBindPage > -1 && mAllAppsLoaded && mWorkspaceLoaded) {
                    mLoaderTask.runBindSynchronousPage(synchronousBindPage);  //先加载预加页 先加载完当前的页面 增加流畅度 必须 apps, workspace 都加载过一遍
                } else {
                    sWorkerThread.setPriority(Thread.NORM_PRIORITY);
                    sWorker.post(mLoaderTask);
                }
            }
        }
    }

    void bindRemainingSynchronousPages() {
        // Post the remaining side pages to be loaded
        if (!mDeferredBindRunnables.isEmpty()) {
            for (final Runnable r : mDeferredBindRunnables) {
                mHandler.post(r, MAIN_THREAD_BINDING_RUNNABLE);
            }
            mDeferredBindRunnables.clear();
        }
    }



    public boolean isCurrentCallbacks(Callbacks callbacks) {
        return (mCallbacks != null && mCallbacks.get() == callbacks);
    }
    
    public void stopLoader() {
        synchronized (mLock) {
            if (mLoaderTask != null) {
                if (DEBUG_MODEL) {
                    LauncherLog.d(TAG, "stopLoader: mLoaderTask = " + mLoaderTask
                            + ",mIsLoaderTaskRunning = " + mIsLoaderTaskRunning);
                }
                mLoaderTask.stopLocked();
            }
        }
    }

    /** Loads the workspace screens db into a map of Rank -> ScreenId */
    private static TreeMap<Integer, Long> loadWorkspaceScreensDb(Context context) {
        final ContentResolver contentResolver = context.getContentResolver();
        final Uri screensUri = LauncherSettings.WorkspaceScreens.CONTENT_URI;
        final Cursor sc = contentResolver.query(screensUri, null, null, null, null);
        TreeMap<Integer, Long> orderedScreens = new TreeMap<Integer, Long>();

        try {
            final int idIndex = sc.getColumnIndexOrThrow(
                    LauncherSettings.WorkspaceScreens._ID);
            final int rankIndex = sc.getColumnIndexOrThrow(
                    LauncherSettings.WorkspaceScreens.SCREEN_RANK);
            while (sc.moveToNext()) {
                try {
                    long screenId = sc.getLong(idIndex);
                    int rank = sc.getInt(rankIndex);
                    orderedScreens.put(rank, screenId);
                } catch (Exception e) {
                	e.printStackTrace();
                    Launcher.addDumpLog(TAG, "Desktop items loading interrupted - invalid screens: " + e, true);
                }
            }
        } finally {
        	try {
        		sc.close();
			} catch (Exception e) {
				// TODO: handle exception
			}
        }
        return orderedScreens;
    }

    public boolean isAllAppsLoaded() {
        return mAllAppsLoaded;
    }

    boolean isLoadingWorkspace() {
        synchronized (mLock) {
            if (mLoaderTask != null) {
                return mLoaderTask.isLoadingWorkspace();
            }
        }
        return false;
    }

    /**
     * Runnable for the thread that loads the contents of the launcher:
     *   - workspace icons
     *   - widgets
     *   - all apps icons
     */
    private class LoaderTask implements Runnable {
        private Context mContext;
        private boolean mIsLaunching;
        private boolean mIsLoadingAndBindingWorkspace;
        private boolean mStopped;
        private boolean mLoadAndBindStepFinished;

        private HashMap<Object, CharSequence> mLabelCache;

        LoaderTask(Context context, boolean isLaunching) {
            mContext = context;
            mIsLaunching = isLaunching;
            mLabelCache = new HashMap<Object, CharSequence>();
            if (DEBUG_LOADERS) {
                LauncherLog.d(TAG, "LoaderTask construct: mLabelCache = " + mLabelCache +
                        ", mIsLaunching = " + mIsLaunching + ", this = " + this);
            }
        }

        boolean isLaunching() {
            return mIsLaunching;
        }

        boolean isLoadingWorkspace() {
            return mIsLoadingAndBindingWorkspace;
        }

        /** Returns whether this is an upgrade path */
        private boolean loadAndBindWorkspace() {
            mIsLoadingAndBindingWorkspace = true;

            // Load the workspace
            if (DEBUG_LOADERS) {
                Log.d(TAG, "loadAndBindWorkspace mWorkspaceLoaded=" + mWorkspaceLoaded);
            }
            final Callbacks oldCallbacks = mCallbacks.get();
            if (oldCallbacks != null &&oldCallbacks instanceof Launcher) {
            	Launcher context = (Launcher) oldCallbacks;
//            	context.postCopyThemePageckage();
            }
            boolean isUpgradePath = false;
            if (!mWorkspaceLoaded) {
                isUpgradePath = loadWorkspace();
//                loadFirstInstallInfo();f
                synchronized (LoaderTask.this) {
                    if (mStopped) {
                        LauncherLog.d(TAG, "loadAndBindWorkspace returned by stop flag.");
                        return isUpgradePath;
                    }
                    mWorkspaceLoaded = true;
                }
            }

            // Bind the workspace
            bindWorkspace(-1, isUpgradePath);
            return isUpgradePath;
        }
        
		/*private void loadFirstInstallInfo() {
			Runnable r = new Runnable() {

				@Override
				public void run() {
					Cursor cursor = LauncherAppState.getLauncherProvider()
							.query(FirstInstallTable.TABLE_NAME, null,
									null, null, null, null, null);
					if (cursor == null) {
						return;
					}
					while (cursor.moveToNext()) {
						FirstInstallItemBean item = new FirstInstallItemBean();
						item.id = cursor.getInt(cursor
								.getColumnIndex(FirstInstallTable.ID));
						item.className = cursor.getString(cursor
								.getColumnIndex(FirstInstallTable.CLASS_NAME));
						item.packageName = cursor
								.getString(cursor
										.getColumnIndex(FirstInstallTable.PACKAGE_NAME));
						item.first_install = cursor.getInt(cursor.getColumnIndex(FirstInstallTable.FIRST_INSTALL));
						
						sFirstInstallItems.add(item);
					}

					cursor.close();

				}
			};
			
			sWorker.post(r);
		}*/

        private void waitForIdle() {
            // Wait until the either we're stopped or the other threads are done.
            // This way we don't start loading all apps until the workspace has settled
            // down.
            synchronized (LoaderTask.this) {
                final long workspaceWaitTime = DEBUG_LOADERS ? SystemClock.uptimeMillis() : 0;
                if (DEBUG_LOADERS) {
                    Log.d(TAG, "waitForIdle start, workspaceWaitTime : " + workspaceWaitTime + "ms, Thread priority :"
                            + Thread.currentThread().getPriority() + ", this = " + this);
                }

                mHandler.postIdle(new Runnable() {
                        public void run() {
                            synchronized (LoaderTask.this) {
                                mLoadAndBindStepFinished = true;
                                if (DEBUG_LOADERS) {
                                    Log.d(TAG, "done with previous binding step");
                                }
                                LoaderTask.this.notify();
                            }
                        }
                    });

                while (!mStopped && !mLoadAndBindStepFinished && !mFlushingWorkerThread) {
                    try {
                        // Just in case mFlushingWorkerThread changes but we aren't woken up,
                        // wait no longer than 1sec at a time
                        this.wait(1000);
                    } catch (InterruptedException ex) {
                        // Ignore
                    }
                }
                if (DEBUG_LOADERS) {
                    Log.d(TAG, "waited " + (SystemClock.uptimeMillis() - workspaceWaitTime)
                            + "ms for previous step to finish binding, mStopped = " + mStopped
                            + ",mLoadAndBindStepFinished = " + mLoadAndBindStepFinished);
                }
            }
        }

        /**
         * @param synchronousBindPage 异步加载预页
         */
        void runBindSynchronousPage(int synchronousBindPage) {
            if (DEBUG_MODEL) {
                LauncherLog.d(TAG, "runBindSynchronousPage: mAllAppsLoaded = " + mAllAppsLoaded
                        + ",mWorkspaceLoaded = " + mWorkspaceLoaded + ",synchronousBindPage = "
                        + synchronousBindPage + ",mIsLoaderTaskRunning = " + mIsLoaderTaskRunning
                        + ",mStopped = " + mStopped + ",this = " + this);
            }

            if (synchronousBindPage < 0) {
                // Ensure that we have a valid page index to load synchronously
                throw new RuntimeException("Should not call runBindSynchronousPage() without " +
                        "valid page index");
            }
            if (!mAllAppsLoaded || !mWorkspaceLoaded) {
                // Ensure that we don't try and bind a specified page when the pages have not been
                // loaded already (we should load everything asynchronously in that case)
                throw new RuntimeException("Expecting AllApps and Workspace to be loaded");
            }
            synchronized (mLock) {
                if (mIsLoaderTaskRunning) {
                    // Ensure that we are never running the background loading at this point since
                    // we also touch the background collections
                    throw new RuntimeException("Error! Background loading is already running");
                }
            }

            // XXX: Throw an exception if we are already loading (since we touch the worker thread
            //      data structures, we can't allow any other thread to touch that data, but because
            //      this call is synchronous, we can get away with not locking).

            // The LauncherModel is static in the LauncherAppState and mHandler may have queued
            // operations from the previous activity.  We need to ensure that all queued operations
            // are executed before any synchronous binding work is done.
            mHandler.flush();

            // Divide the set of loaded items into those that we are binding synchronously, and
            // everything else that is to be bound normally (asynchronously).
            bindWorkspace(synchronousBindPage, false);
            // XXX: For now, continue posting the binding of AllApps as there are other issues that
            //      arise from that.
            onlyBindAllApps();
        }

        public void run() {
            boolean isUpgrade = false;

            synchronized (mLock) {
                if (DEBUG_LOADERS) {
                    LauncherLog.d(TAG, "Set load task running flag >>>>, mIsLaunching = " +
                            mIsLaunching + ",this = " + this);
                }
                mIsLoaderTaskRunning = true;
            }
            // Optimize for end-user experience: if the Launcher is up and // running with the
            // All Apps interface in the foreground, load All Apps first. Otherwise, load the
            // workspace first (default).
            keep_running: {
                // Elevate priority when Home launches for the first time to avoid
                // starving at boot time. Staring at a blank home is not cool.
                synchronized (mLock) {
                    if (DEBUG_LOADERS) Log.d(TAG, "Setting thread priority to " +
                            (mIsLaunching ? "DEFAULT" : "BACKGROUND"));
                    android.os.Process.setThreadPriority(mIsLaunching
                            ? Process.THREAD_PRIORITY_DEFAULT : Process.THREAD_PRIORITY_BACKGROUND);
                }
                if (DEBUG_LOADERS) Log.d(TAG, "step 1: loading workspace");  //第一步先加载workspace 
                isUpgrade = loadAndBindWorkspace();

                if (mStopped) {
                    LauncherLog.i(TAG, "LoadTask break in the middle, this = " + this);
                    break keep_running;
                }

                // Whew! Hard work done.  Slow us down, and wait until the UI thread has
                // settled down.
                synchronized (mLock) {
                    if (mIsLaunching) {
                        if (DEBUG_LOADERS) Log.d(TAG, "Setting thread priority to BACKGROUND");
                        android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                    }
                }
                waitForIdle();
                Callbacks callbacks = mCallbacks.get();
                if (callbacks != null) {
                    addAppStoreItems(loadStoreApps(mApp.getContext()));
                    callbacks.bindAppsFinish();
                    isFinish=true;
                }
                // second step
                if (DEBUG_LOADERS) Log.d(TAG, "step 2: loading all apps");
                loadAndBindAllApps();

                // Restore the default thread priority after we are done loading items
                synchronized (mLock) {
                    android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_DEFAULT);
                }
            }

            // Update the saved icons if necessary
            if (DEBUG_LOADERS) Log.d(TAG, "Comparing loaded icons to database icons");
            synchronized (sBgLock) {
                for (Object key : sBgDbIconCache.keySet()) {
//                    updateSavedIcon(mContext, (ShortcutInfo) key, sBgDbIconCache.get(key));
                }
                sBgDbIconCache.clear();
            }

            if (AppsCustomizePagedView.DISABLE_ALL_APPS) {
                // Ensure that all the applications that are in the system are
                // represented on the home screen.
                if (!UPGRADE_USE_MORE_APPS_FOLDER || !isUpgrade) {
                    verifyApplications();
                }
            }
            Context context = mContext; //add by caosw
            // Clear out this reference, otherwise we end up holding it until all of the
            // callback runnables are done.
            mContext = null;

            synchronized (mLock) {
                // If we are still the last one to be scheduled, remove ourselves.
                if (mLoaderTask == this) {
                    mLoaderTask = null;
                }
                if (DEBUG_LOADERS) {
                    LauncherLog.d(TAG, "Reset load task running flag <<<<, this = " + this);
                }
                mIsLoaderTaskRunning = false;

				// add by caosw
			/*	if (context != null) {
					if (Settings.System.getInt(context.getContentResolver(),
							"isSwitchThemes", -1) == 1) {
						Log.i("caosw", "putInt 0 ----- Theme Settings");
						Settings.System.putInt(context.getContentResolver(),
								"isSwitchThemes", 0);
					}
					context = null;
				}*/
            }
        }

        public void stopLocked() {
            synchronized (LoaderTask.this) {
                mStopped = true;
                this.notify();
            }
            if (DEBUG_LOADERS) {
                LauncherLog.d(TAG, "stopLocked completed, this = " + LoaderTask.this
                        + ", mLoaderTask = " + mLoaderTask + ",mIsLoaderTaskRunning = "
                        + mIsLoaderTaskRunning);
            }
        }

        /**
         * Gets the callbacks object.  If we've been stopped, or if the launcher object
         * has somehow been garbage collected, return null instead.  Pass in the Callbacks
         * object that was around when the deferred message was scheduled, and if there's
         * a new Callbacks object around then also return null.  This will save us from
         * calling onto it with data that will be ignored.
         */
        Callbacks tryGetCallbacks(Callbacks oldCallbacks) {
            synchronized (mLock) {
                if (mStopped) {
                    LauncherLog.i(TAG, "tryGetCallbacks returned null by stop flag.");
                    return null;
                }

                if (mCallbacks == null) {
                    return null;
                }

                final Callbacks callbacks = mCallbacks.get();
                if (callbacks != oldCallbacks) {
                    return null;
                }
                if (callbacks == null) {
                    Log.w(TAG, "no mCallbacks");
                    return null;
                }

                return callbacks;
            }
        }

        private void verifyApplications() {
            final Context context = mApp.getContext();

            // Cross reference all the applications in our apps list with items in the workspace
            ArrayList<ItemInfo> tmpInfos;
            ArrayList<ItemInfo> added = new ArrayList<ItemInfo>();
            synchronized (sBgLock) {
                for (AppInfo app : mBgAllAppsList.data) {
                    tmpInfos = getItemInfoForComponentName(app.componentName);
                    if (tmpInfos.isEmpty()) {
                        // We are missing an application icon, so add this to the workspace
                        added.add(app);
                        // This is a rare event, so lets log it
                        Log.e(TAG, "Missing Application on load: " + app);
                    }
                }
            }
            if (!added.isEmpty()) {
                Callbacks cb = mCallbacks != null ? mCallbacks.get() : null;
                addAndBindAddedApps(context, added, cb, null);
            }
            if(added.size()<=0) {
                	SharedPreferences sp = mApp.getContext().getSharedPreferences(
            				"load_default_res", Context.MODE_PRIVATE);
         			sp.edit().putBoolean("load_default_res_loadeds", true).commit();
            }
        }

        private boolean checkItemDimensions(ItemInfo info) {
            LauncherAppState app = LauncherAppState.getInstance();
            DeviceProfile grid = app.getDynamicGrid().getDeviceProfile();
            return (info.cellX + info.spanX) > (int) grid.numColumns ||
                    (info.cellY + info.spanY) > (int) grid.numRows;
        }

        // check & update map of what's occupied; used to discard overlapping/invalid items
        private boolean checkItemPlacement(HashMap<Long, ItemInfo[][]> occupied, ItemInfo item,
                                           AtomicBoolean deleteOnItemOverlap) {
            LauncherAppState app = LauncherAppState.getInstance();
            DeviceProfile grid = app.getDynamicGrid().getDeviceProfile();
            int countX = (int) grid.numColumns;
            int countY = (int) grid.numRows;

            long containerIndex = item.screenId;
            if (item.container == LauncherSettings.Favorites.CONTAINER_HOTSEAT) {
                // Return early if we detect that an item is under the hotseat button
                if (mCallbacks == null ||
                        mCallbacks.get().isAllAppsButtonRank((int) item.screenId)) {
                    deleteOnItemOverlap.set(true);
                    return false;
                }

                if (occupied.containsKey(LauncherSettings.Favorites.CONTAINER_HOTSEAT)) {
                    if (occupied.get(LauncherSettings.Favorites.CONTAINER_HOTSEAT)
                            [(int) item.screenId][0] != null) {
                        Log.e(TAG, "Error loading shortcut into hotseat " + item
                                + " into position (" + item.screenId + ":" + item.cellX + ","
                                + item.cellY + ") occupied by "
                                + occupied.get(LauncherSettings.Favorites.CONTAINER_HOTSEAT)
                                [(int) item.screenId][0]);
                            return false;
                    }
                } else {
                    ItemInfo[][] items = new ItemInfo[countX + 1][countY + 1];
                    if(item.screenId>=countX) {
                    	return false;
                    }
                    items[(int) item.screenId][0] = item;
                    occupied.put((long) LauncherSettings.Favorites.CONTAINER_HOTSEAT, items);
                    return true;
                }
            } else if (item.container != LauncherSettings.Favorites.CONTAINER_DESKTOP) {
                // Skip further checking if it is not the hotseat or workspace container
                return true;
            }

            if (!occupied.containsKey(item.screenId)) {
                ItemInfo[][] items = new ItemInfo[countX + 1][countY + 1];
                occupied.put(item.screenId, items);
            }

            ItemInfo[][] screens = occupied.get(item.screenId);
            // Check if any workspace icons overlap with each other
            for (int x = item.cellX; x < (item.cellX+item.spanX); x++) {
                for (int y = item.cellY; y < (item.cellY+item.spanY); y++) {
                    if (screens[x][y] != null) {
                        Log.e(TAG, "Error loading shortcut " + item
                            + " into cell (" + containerIndex + "-" + item.screenId + ":"
                            + x + "," + y
                            + ") occupied by "
                            + screens[x][y]);
                        return false;
                    }
                }
            }
            for (int x = item.cellX; x < (item.cellX+item.spanX); x++) {
                for (int y = item.cellY; y < (item.cellY+item.spanY); y++) {
                    screens[x][y] = item;
                }
            }

            return true;
        }

        /** Clears all the sBg data structures */
        private void clearSBgDataStructures() {
            synchronized (sBgLock) {
                sBgWorkspaceItems.clear();
                sBgAppWidgets.clear();
                sBgFolders.clear();
                sBgItemsIdMap.clear();
                sBgDbIconCache.clear();
                sBgWorkspaceScreens.clear();
            }
        }

        /** Returns whether this is an upgradge path */
        private boolean loadWorkspace() {
            final long t = DEBUG_LOADERS ? SystemClock.uptimeMillis() : 0;

            final Context context = mContext;
            final ContentResolver contentResolver = context.getContentResolver();
            final PackageManager manager = context.getPackageManager();
            final AppWidgetManager widgets = AppWidgetManager.getInstance(context);
            final boolean isSafeMode = manager.isSafeMode();

            LauncherAppState app = LauncherAppState.getInstance();
            DeviceProfile grid = app.getDynamicGrid().getDeviceProfile();
            int countX = (int) grid.numColumns;
            int countY = (int) grid.numRows;

            // Make sure the default workspace is loaded, if needed
            LauncherAppState.getLauncherProvider().loadDefaultFavoritesIfNecessary(0);//装在默认的favorites如果有必要的话 如果是第一次
//            deleteItemsFromAppStore(mContext);
            updateDownLoadDb(mContext);
            // Check if we need to do any upgrade-path logic
            boolean loadedOldDb = true;

            synchronized (sBgLock) {
            	loadedOldDb = LauncherAppState.getLauncherProvider().justLoadedOldDb();
                clearSBgDataStructures();

                final ArrayList<Long> itemsToRemove = new ArrayList<Long>();
                final Uri contentUri = LauncherSettings.Favorites.CONTENT_URI;
                if (DEBUG_LOADERS) Log.d(TAG, "loading model from " + contentUri);
                final Cursor c = contentResolver.query(contentUri, null, null, null, null);

                // +1 for the hotseat (it can be larger than the workspace)
                // Load workspace in reverse order to ensure that latest items are loaded first (and
                // before any earlier duplicates)
                final HashMap<Long, ItemInfo[][]> occupied = new HashMap<Long, ItemInfo[][]>();

                try {
                    final int idIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites._ID);
                    final int intentIndex = c.getColumnIndexOrThrow
                            (LauncherSettings.Favorites.INTENT);
                    final int titleIndex = c.getColumnIndexOrThrow
                            (LauncherSettings.Favorites.TITLE);
                    final int titleIdIndex = c.getColumnIndexOrThrow
                            (LauncherSettings.Favorites.TITLE_ID);
                    final int componentNameIdIndex = c.getColumnIndexOrThrow
                            (LauncherSettings.Favorites.COMPONENTNAME);
                    final int iconTypeIndex = c.getColumnIndexOrThrow(
                            LauncherSettings.Favorites.ICON_TYPE);
                    final int iconIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.ICON);
                    final int iconPackageIndex = c.getColumnIndexOrThrow(
                            LauncherSettings.Favorites.ICON_PACKAGE);
                    final int iconResourceIndex = c.getColumnIndexOrThrow(
                            LauncherSettings.Favorites.ICON_RESOURCE);
                    final int containerIndex = c.getColumnIndexOrThrow(
                            LauncherSettings.Favorites.CONTAINER);
                    final int itemTypeIndex = c.getColumnIndexOrThrow(
                            LauncherSettings.Favorites.ITEM_TYPE);
                    final int appWidgetIdIndex = c.getColumnIndexOrThrow(
                            LauncherSettings.Favorites.APPWIDGET_ID);
                    final int appWidgetProviderIndex = c.getColumnIndexOrThrow(
                            LauncherSettings.Favorites.APPWIDGET_PROVIDER);
                    final int screenIndex = c.getColumnIndexOrThrow(
                            LauncherSettings.Favorites.SCREEN);
                    final int cellXIndex = c.getColumnIndexOrThrow
                            (LauncherSettings.Favorites.CELLX);
                    final int cellYIndex = c.getColumnIndexOrThrow
                            (LauncherSettings.Favorites.CELLY);
                    final int spanXIndex = c.getColumnIndexOrThrow
                            (LauncherSettings.Favorites.SPANX);
                    final int spanYIndex = c.getColumnIndexOrThrow(
                            LauncherSettings.Favorites.SPANY);
                    final int firstInstall = c.getColumnIndexOrThrow(
                            LauncherSettings.Favorites.FIRST_INSTALL);
                    final int recommd_dis = c.getColumnIndexOrThrow(
                            LauncherSettings.Favorites.RECOMMD_DIS);
                    final int packageName = c.getColumnIndexOrThrow(
                            LauncherSettings.Favorites.PACKAGE_NAME);
                    final int down_progress = c.getColumnIndexOrThrow(
                            LauncherSettings.Favorites.DOWN_PROGRESS);
                    final int fromAppStore = c.getColumnIndexOrThrow(
                            LauncherSettings.Favorites.FROM_APPSTORE);
                    final int downState = c.getColumnIndexOrThrow(
                            LauncherSettings.Favorites.DOWN_STATE);
                    final int iconUri = c.getColumnIndexOrThrow(
                            LauncherSettings.Favorites.ICON_URI);
                    final int hide = c.getColumnIndexOrThrow(
                            LauncherSettings.Favorites.HIDE);
                    //final int uriIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.URI);
                    //final int displayModeIndex = c.getColumnIndexOrThrow(
                    //        LauncherSettings.Favorites.DISPLAY_MODE);

                    ShortcutInfo info;
                    String intentDescription;
                    LauncherAppWidgetInfo appWidgetInfo;
                    int container;
                    long id = 0;
                    Intent intent = null;

                    while (!mStopped && c.moveToNext()) {
                        AtomicBoolean deleteOnItemOverlap = new AtomicBoolean(false);
                        try {
                            int itemType = c.getInt(itemTypeIndex);

                            switch (itemType) {
                            case LauncherSettings.Favorites.ITEM_TYPE_APPLICATION:
                            case LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT:
                                id = c.getLong(idIndex);
                                intentDescription = c.getString(intentIndex);
                                int fromdownAppStore = c.getInt(fromAppStore);
									if(fromdownAppStore!=1) {
		                                try {
		                                    intent = Intent.parseUri(intentDescription, 0);
		                                    ComponentName cn = intent.getComponent();

		                                    if (cn != null && !isValidPackageComponent(manager, cn)) {
		                                        if (!mAppsCanBeOnRemoveableStorage) {
		                                            // Log the invalid package, and remove it from the db
		                                            Launcher.addDumpLog(TAG, "Invalid package removed: " + cn, true);
		                                            itemsToRemove.add(id);
		                                        } else {
		                                            // If apps can be on external storage, then we just
		                                            // leave them for the user to remove (maybe add
		                                            // visual treatment to it)
		                                            Launcher.addDumpLog(TAG, "Invalid package found: " + cn, true);
		                                        }
		                                        continue;
		                                    }
		                                } catch (URISyntaxException e) {
		                                    Launcher.addDumpLog(TAG, "Invalid uri: " + intentDescription, true);
		                                    continue;
		                                }
									}else {
										LogUtils.i("zhouerlong", "这是第三方下载图标");
										intent=null;
									}

                                if (itemType == LauncherSettings.Favorites.ITEM_TYPE_APPLICATION&&fromdownAppStore!=1) {
                                    info = getShortcutInfo(manager, intent, context, c, iconIndex,
                                            titleIndex, mLabelCache);
                                } else {
                                    info = getShortcutInfo(c,intent, context, iconTypeIndex,
                                            iconPackageIndex, iconResourceIndex, iconIndex,
                                            titleIndex);

                                    // App shortcuts that used to be automatically added to Launcher
                                    // didn't always have the correct intent flags set, so do that
                                    // here
                                    if (fromdownAppStore!=1&&intent.getAction() != null &&
                                        intent.getCategories() != null &&
                                        intent.getAction().equals(Intent.ACTION_MAIN) &&
                                        intent.getCategories().contains(Intent.CATEGORY_LAUNCHER)) {
                                        intent.addFlags(
                                            Intent.FLAG_ACTIVITY_NEW_TASK |
                                            Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                                    }
                                }

                                if (info != null) {
                                	info.packageName="";
                                    info.id = id;
                                    info.intent = intent;
                                    if(intent!=null)
                                    intent.setSourceBounds(null);
                                    container = c.getInt(containerIndex);
                                    info.container = container;
                                    info.screenId = c.getInt(screenIndex);
                                    info.cellX = c.getInt(cellXIndex);
                                    info.cellY = c.getInt(cellYIndex);
                                    info.firstInstall = c.getInt(firstInstall);
                                    info.packageName = c.getString(packageName);
                                    info.progress = c.getInt(down_progress);
                                    info.iconUri = c.getString(iconUri);
                                    info.fromAppStore = c.getInt(fromAppStore);
                                    info.hide = c.getInt(hide);
                                    info.down_state = c.getInt(downState);
                                    info.spanX = 1;
                                    info.spanY = 1;
                                    info.mComponentName = c.getString(componentNameIdIndex);
                                    
                                    
                                    
                                    
                                    LogUtils.i("zhouerlong", "info:ss"+info.toString());
                                    // Skip loading items that are out of bounds
                                    if (container == LauncherSettings.Favorites.CONTAINER_DESKTOP) {
                                        if (checkItemDimensions(info)) {
                                            Launcher.addDumpLog(TAG, "Skipped loading out of bounds shortcut: "
                                                    + info + ", " + grid.numColumns + "x" + grid.numRows, true);
                                            continue;
                                        }
                                    }
                                    // check & update map of what's occupied
                                    deleteOnItemOverlap.set(false);
                                    if (!checkItemPlacement(occupied, info, deleteOnItemOverlap)) {
//                                        if (deleteOnItemOverlap.get()) {
                                            itemsToRemove.add(id);
//                                        }
                                        break;
                                    }

                                    switch (container) {
                                    case LauncherSettings.Favorites.CONTAINER_DESKTOP:
                                    case LauncherSettings.Favorites.CONTAINER_HOTSEAT:
                                    	 /*SharedPreferences sp = mContext.getSharedPreferences("test_hot", Context.MODE_PRIVATE);
                                    	 if(!sp.getBoolean("test_hot", false)) {
                                    		 if(info.intent.getComponent().getClassName().equals("com.cmread.bplusc.bookshelf.LocalMainActivity")) {
                                    			 info.cellY=0;
                                    			 info.cellX=3;
                                    			 info.screenId=3;
                                    			 info.container=LauncherSettings.Favorites.CONTAINER_HOTSEAT;
                                    			 moveItemInDatabase(context, info, info.container, info.screenId, info.cellX, info.cellY);
                                        		 sp.edit().putBoolean("test_hot", true).commit();
                                    		 }
                                    	 }*/
                                        sBgWorkspaceItems.add(info);
                                        break;
                                    default:
                                        // Item is in a user folder
                                        FolderInfo folderInfo =
                                                findOrMakeFolder(sBgFolders, container);
                                        folderInfo.add(info);
                                        
                                        break;
                                    }
                                    sBgItemsIdMap.put(info.id, info);

                                    // now that we've loaded everthing re-save it with the
                                    // icon in case it disappears somehow.
//                                    queueIconToBeChecked(sBgDbIconCache, info, c, iconIndex);
                                } else {
                                    throw new RuntimeException("Unexpected null ShortcutInfo");
                                }
                                break;

                            case LauncherSettings.Favorites.ITEM_TYPE_FOLDER:
                                id = c.getLong(idIndex);
                                FolderInfo folderInfo = findOrMakeFolder(sBgFolders, id);

                                folderInfo.title = c.getString(titleIndex);
                                folderInfo.id = id;
                                container = c.getInt(containerIndex);
                                folderInfo.container = container;
                                folderInfo.screenId = c.getInt(screenIndex);
                                folderInfo.recommd_dis = c.getInt(recommd_dis);
                                folderInfo.cellX = c.getInt(cellXIndex);
                                folderInfo.cellY = c.getInt(cellYIndex);
                                folderInfo.title_id = c.getInt(titleIdIndex);//add by zhouerlong
                                folderInfo.spanX = 1;
                                folderInfo.spanY = 1;

                                // Skip loading items that are out of bounds
                                if (container == LauncherSettings.Favorites.CONTAINER_DESKTOP) {
                                    if (checkItemDimensions(folderInfo)) {
                                        Log.d(TAG, "Skipped loading out of bounds folder");
                                        continue;
                                    }
                                }
                                // check & update map of what's occupied
                                deleteOnItemOverlap.set(false);
                                if (!checkItemPlacement(occupied, folderInfo,
                                        deleteOnItemOverlap)) {
//                                    if (deleteOnItemOverlap.get()) {
                                        itemsToRemove.add(id);
//                                    }
                                    break;
                                }

                                switch (container) {
                                    case LauncherSettings.Favorites.CONTAINER_DESKTOP:
                                    case LauncherSettings.Favorites.CONTAINER_HOTSEAT:
                                        sBgWorkspaceItems.add(folderInfo);
                                        break;
                                }

                                sBgItemsIdMap.put(folderInfo.id, folderInfo);
                                sBgFolders.put(folderInfo.id, folderInfo);
                                if (DEBUG_MODEL) {
                                    LauncherLog.d(TAG, "loadWorkspace sBgItemsIdMap.put = " + folderInfo);
                                }
                                break;

                            case LauncherSettings.Favorites.ITEM_TYPE_APPWIDGET:
                                // Read all Launcher-specific widget details
                                int appWidgetId = c.getInt(appWidgetIdIndex);
                                String savedProvider = c.getString(appWidgetProviderIndex);

                                id = c.getLong(idIndex);

                                final AppWidgetProviderInfo provider =
                                        widgets.getAppWidgetInfo(appWidgetId);

                                if (!isSafeMode && (provider == null || provider.provider == null ||
                                        provider.provider.getPackageName() == null)) {
                                    String log = "Deleting widget that isn't installed anymore: id="
                                        + id + " appWidgetId=" + appWidgetId;
                                    Log.e(TAG, log);
                                    Launcher.addDumpLog(TAG, log, false);
                                    itemsToRemove.add(id);
                                } else {
                                    appWidgetInfo = new LauncherAppWidgetInfo(appWidgetId,
                                            provider.provider);
                                    appWidgetInfo.id = id;
                                    appWidgetInfo.screenId = c.getInt(screenIndex);
                                    appWidgetInfo.cellX = c.getInt(cellXIndex);
                                    appWidgetInfo.cellY = c.getInt(cellYIndex);
                                    appWidgetInfo.spanX = c.getInt(spanXIndex);
                                    appWidgetInfo.spanY = c.getInt(spanYIndex);
                                    int[] minSpan = Launcher.getMinSpanForWidget(context, provider);
                                    appWidgetInfo.minSpanX = minSpan[0];
                                    appWidgetInfo.minSpanY = minSpan[1];

                                    container = c.getInt(containerIndex);
                                    if (container != LauncherSettings.Favorites.CONTAINER_DESKTOP &&
                                        container != LauncherSettings.Favorites.CONTAINER_HOTSEAT) {
                                        Log.e(TAG, "Widget found where container != " +
                                            "CONTAINER_DESKTOP nor CONTAINER_HOTSEAT - ignoring!");
                                        continue;
                                    }

                                    appWidgetInfo.container = c.getInt(containerIndex);
                                    // Skip loading items that are out of bounds
                                    if (container == LauncherSettings.Favorites.CONTAINER_DESKTOP) {
                                        if (checkItemDimensions(appWidgetInfo)) {
                                            Log.d(TAG, "Skipped loading out of bounds app widget");
                                            continue;
                                        }
                                    }
                                    // check & update map of what's occupied
                                    deleteOnItemOverlap.set(false);
                                    if (!checkItemPlacement(occupied, appWidgetInfo,
                                            deleteOnItemOverlap)) {
//                                        if (deleteOnItemOverlap.get()) {
                                            itemsToRemove.add(id);
//                                        }
                                        break;
                                    }
                                    String providerName = provider.provider.flattenToString();
                                    if (!providerName.equals(savedProvider)) {
                                        ContentValues values = new ContentValues();
                                        values.put(LauncherSettings.Favorites.APPWIDGET_PROVIDER,
                                                providerName);
                                        String where = BaseColumns._ID + "= ?";
                                        String[] args = {Integer.toString(c.getInt(idIndex))};
                                        contentResolver.update(contentUri, values, where, args);
                                    }
                                    sBgItemsIdMap.put(appWidgetInfo.id, appWidgetInfo);
                                    sBgAppWidgets.add(appWidgetInfo);
                                }
                                break;
                            }
                        } catch (Exception e) {
                        	itemsToRemove.add(id);
                            Launcher.addDumpLog(TAG, "Desktop items loading interrupted: " + e, true);
                        }
                    }
                } finally {
                    if (c != null) {
                        c.close();
                    }
                }

                // Break early if we've stopped loading
                
                if (itemsToRemove.size() > 0) {
                    ContentProviderClient client = contentResolver.acquireContentProviderClient(
                            LauncherSettings.Favorites.CONTENT_URI);
                    // Remove dead items
                    for (long id : itemsToRemove) {
                        if (DEBUG_LOADERS) {
                            Log.d(TAG, "Removed id = " + id);
                        }
                        // Don't notify content observers
                        try {
                            client.delete(LauncherSettings.Favorites.getContentUri(id, false),
                                    null, null);
                        } catch (RemoteException e) {
                            Log.w(TAG, "Could not remove id = " + id);
                        }
                    }
                }

                if (loadedOldDb) {
                	initScreens(context);
                } else {
                    TreeMap<Integer, Long> orderedScreens = loadWorkspaceScreensDb(mContext);
                    if(orderedScreens.size()<=0) {
                    	initScreens(context);
                    }else {
                    for (Integer i : orderedScreens.keySet()) {
                        sBgWorkspaceScreens.add(orderedScreens.get(i));
                    }

                    // Remove any empty screens
                    ArrayList<Long> unusedScreens = new ArrayList<Long>(sBgWorkspaceScreens);
                    for (ItemInfo item: sBgItemsIdMap.values()) {
                        long screenId = item.screenId;
                        if (item.container == LauncherSettings.Favorites.CONTAINER_DESKTOP &&
                                unusedScreens.contains(screenId)) {
                            unusedScreens.remove(screenId);
                        }
                    }

                    // If there are any empty screens remove them, and update.
                    if (unusedScreens.size() != 0) {
                        //sBgWorkspaceScreens.removeAll(unusedScreens); //del by zhouerlong
//add by zhouerlong
                        updateWorkspaceScreenOrder(context, sBgWorkspaceScreens);
                    }
                }
                }
                if (mStopped) {
//                	LauncherAppState.getLauncherProvider().resetOldDb();
                    clearSBgDataStructures();
                    return false;
                }

                if (DEBUG_LOADERS) {
                    Log.d(TAG, "loaded workspace in " + (SystemClock.uptimeMillis()-t) + "ms");
                    Log.d(TAG, "workspace layout: ");
                    int nScreens = occupied.size();
                    for (int y = 0; y < countY; y++) {
                        String line = "";

                        Iterator<Long> iter = occupied.keySet().iterator();
                        while (iter.hasNext()) {
                            long screenId = iter.next();
                            if (screenId > 0) {
                                line += " | ";
                            }
                            for (int x = 0; x < countX; x++) {
                                line += ((occupied.get(screenId)[x][y] != null) ? "#" : ".");
                            }
                        }
                        Log.d(TAG, "[ " + line + " ]");
                    }
                }
            }
            return loadedOldDb;
        }
        
        
        private void initScreens(Context context) {

            long maxScreenId = 0;
            // If we're importing we use the old screen order.
            
           LauncherProvider.resetOldDb(LauncherApplication.getInstance().getApplicationContext());

			String pageCount = Utilities.getDefaultpageCount();
			int page = 3;
			try {
				page = pageCount != null ? Integer.valueOf(pageCount)
						: 3;
			} catch (Exception e) {
				e.printStackTrace();
			}
            for(long id=0;id<page;id++) {

                sBgWorkspaceScreens.add(id);
                if (id >maxScreenId) {
                    maxScreenId = id;
                	
                }
            }
            for (ItemInfo item: sBgItemsIdMap.values()) {
                long screenId = item.screenId;
                if (item.container == LauncherSettings.Favorites.CONTAINER_DESKTOP &&
                        !sBgWorkspaceScreens.contains(screenId)) {
                    sBgWorkspaceScreens.add(screenId);
                    if (screenId > maxScreenId) {
                        maxScreenId = screenId;
                    }
                }
            }
            Collections.sort(sBgWorkspaceScreens);

            LauncherAppState.getLauncherProvider().updateMaxScreenId(maxScreenId);
            updateWorkspaceScreenOrder(context, sBgWorkspaceScreens);

            // Update the max item id after we load an old db
            long maxItemId = 0;
            // If we're importing we use the old screen order.
            for (ItemInfo item: sBgItemsIdMap.values()) {
                maxItemId = Math.max(maxItemId, item.id);
            }
//            LauncherAppState.getLauncherProvider().updateMaxItemId(maxItemId);
        
        }

        /** Filters the set of items who are directly or indirectly (via another container) on the
         * specified screen. */
        private void filterCurrentWorkspaceItems(int currentScreen,
                ArrayList<ItemInfo> allWorkspaceItems,
                ArrayList<ItemInfo> currentScreenItems,
                ArrayList<ItemInfo> otherScreenItems) {
            // Purge any null ItemInfos
            Iterator<ItemInfo> iter = allWorkspaceItems.iterator();
            while (iter.hasNext()) {
                ItemInfo i = iter.next();
                if (i == null) {
                    iter.remove();
                }
            }

            // If we aren't filtering on a screen, then the set of items to load is the full set of
            // items given.
            if (currentScreen < 0) {
                currentScreenItems.addAll(allWorkspaceItems);
            }

            // Order the set of items by their containers first, this allows use to walk through the
            // list sequentially, build up a list of containers that are in the specified screen,
            // as well as all items in those containers.
            Set<Long> itemsOnScreen = new HashSet<Long>();
            Collections.sort(allWorkspaceItems, new Comparator<ItemInfo>() {
                @Override
                public int compare(ItemInfo lhs, ItemInfo rhs) {
                    return (int) (lhs.container - rhs.container);
                }
            });
            for (ItemInfo info : allWorkspaceItems) {
                if (info.container == LauncherSettings.Favorites.CONTAINER_DESKTOP) {
                    if (info.screenId == currentScreen) {
                        currentScreenItems.add(info);
                        itemsOnScreen.add(info.id);
                    } else {
                        otherScreenItems.add(info);
                    }
                } else if (info.container == LauncherSettings.Favorites.CONTAINER_HOTSEAT) {
                    currentScreenItems.add(info);
                    itemsOnScreen.add(info.id);
                } else {
                    if (itemsOnScreen.contains(info.container)) {
                        currentScreenItems.add(info);
                        itemsOnScreen.add(info.id);
                    } else {
                        otherScreenItems.add(info);
                    }
                }
            }
        }

        /** Filters the set of widgets which are on the specified screen. */
        private void filterCurrentAppWidgets(int currentScreen,
                ArrayList<LauncherAppWidgetInfo> appWidgets,
                ArrayList<LauncherAppWidgetInfo> currentScreenWidgets,
                ArrayList<LauncherAppWidgetInfo> otherScreenWidgets) {
            // If we aren't filtering on a screen, then the set of items to load is the full set of
            // widgets given.
            if (currentScreen < 0) {
                currentScreenWidgets.addAll(appWidgets);
            }

            for (LauncherAppWidgetInfo widget : appWidgets) {
                if (widget == null) continue;
                if (widget.container == LauncherSettings.Favorites.CONTAINER_DESKTOP &&
                        widget.screenId == currentScreen) {
                    currentScreenWidgets.add(widget);
                } else {
                    otherScreenWidgets.add(widget);
                }
            }
        }

        /** Filters the set of folders which are on the specified screen. */
        private void filterCurrentFolders(int currentScreen,
                HashMap<Long, ItemInfo> itemsIdMap,
                HashMap<Long, FolderInfo> folders,
                HashMap<Long, FolderInfo> currentScreenFolders,
                HashMap<Long, FolderInfo> otherScreenFolders) {
            // If we aren't filtering on a screen, then the set of items to load is the full set of
            // widgets given.
            if (currentScreen < 0) {
                currentScreenFolders.putAll(folders);
            }

            for (long id : folders.keySet()) {
                ItemInfo info = itemsIdMap.get(id);
                FolderInfo folder = folders.get(id);
                if (info == null || folder == null) continue;
                if (info.container == LauncherSettings.Favorites.CONTAINER_DESKTOP &&
                        info.screenId == currentScreen) {
                    currentScreenFolders.put(id, folder);
                } else {
                    otherScreenFolders.put(id, folder);
                }
            }
        }

        /** Sorts the set of items by hotseat, workspace (spatially from top to bottom, left to
         * right) */
        private void sortWorkspaceItemsSpatially(ArrayList<ItemInfo> workspaceItems) {
            final LauncherAppState app = LauncherAppState.getInstance();
            final DeviceProfile grid = app.getDynamicGrid().getDeviceProfile();
            // XXX: review this
            Collections.sort(workspaceItems, new Comparator<ItemInfo>() {
                @Override
                public int compare(ItemInfo lhs, ItemInfo rhs) {
                    int cellCountX = (int) grid.numColumns;
                    int cellCountY = (int) grid.numRows;
                    int screenOffset = cellCountX * cellCountY;
                    int containerOffset = screenOffset * (Launcher.SCREEN_COUNT + 1); // +1 hotseat
                    long lr = (lhs.container * containerOffset + lhs.screenId * screenOffset +
                            lhs.cellY * cellCountX + lhs.cellX);
                    long rr = (rhs.container * containerOffset + rhs.screenId * screenOffset +
                            rhs.cellY * cellCountX + rhs.cellX);
                    return (int) (lr - rr);
                }
            });
        }

        private void bindWorkspaceScreens(final Callbacks oldCallbacks,
                final ArrayList<Long> orderedScreens) {
            final Runnable r = new Runnable() {
                @Override
                public void run() {
                    Callbacks callbacks = tryGetCallbacks(oldCallbacks);
                    if (callbacks != null) {
                        callbacks.bindScreens(orderedScreens);
                    }
                }
            };
            runOnMainThread(r, MAIN_THREAD_BINDING_RUNNABLE);
        }

        private void bindWorkspaceItems(final Callbacks oldCallbacks,
                final ArrayList<ItemInfo> workspaceItems,
                final ArrayList<LauncherAppWidgetInfo> appWidgets,
                final HashMap<Long, FolderInfo> folders,
                ArrayList<Runnable> deferredBindRunnables) {

            final boolean postOnMainThread = (deferredBindRunnables != null);

            // Bind the workspace items
            int N = workspaceItems.size();
            for (int i = 0; i < N; i += ITEMS_CHUNK) {
                final int start = i;
                final int chunkSize = (i+ITEMS_CHUNK <= N) ? ITEMS_CHUNK : (N-i);
                final Runnable r = new Runnable() {
                    @Override
                    public void run() {
                        Callbacks callbacks = tryGetCallbacks(oldCallbacks);
                        if (callbacks != null) {
                            callbacks.bindItems(workspaceItems, start, start+chunkSize,
                                    false);
                        }
                    }
                };
                if (postOnMainThread) {
                    deferredBindRunnables.add(r);
                } else {
                    runOnMainThread(r, MAIN_THREAD_BINDING_RUNNABLE);
                }
            }

            // Bind the folders
            if (!folders.isEmpty()) {
                final Runnable r = new Runnable() {
                    public void run() {
                        Callbacks callbacks = tryGetCallbacks(oldCallbacks);
                        if (callbacks != null) {
                            callbacks.bindFolders(folders);
                        }
                    }
                };
                if (postOnMainThread) {
                    deferredBindRunnables.add(r);
                } else {
                    runOnMainThread(r, MAIN_THREAD_BINDING_RUNNABLE);
                }
            }

            // Bind the widgets, one at a time
            N = appWidgets.size();

            for (int i = 0; i < N; i++) {
                final LauncherAppWidgetInfo widget = appWidgets.get(i);
                final Runnable r = new Runnable() {
                    public void run() {
                        Callbacks callbacks = tryGetCallbacks(oldCallbacks);
                        if (callbacks != null) {
                            callbacks.bindAppWidget(widget);
                        }
                    }
                };
                if (postOnMainThread) {
                    deferredBindRunnables.add(r);
                } else {
                    runOnMainThread(r, MAIN_THREAD_BINDING_RUNNABLE);
                }
            }
        }

        /**
         * Binds all loaded data to actual views on the main thread.
         */
        private void bindWorkspace(int synchronizeBindPage, final boolean isUpgradePath) {
            final long t = SystemClock.uptimeMillis();
            Runnable r;

            // Don't use these two variables in any of the callback runnables.
            // Otherwise we hold a reference to them.
            final Callbacks oldCallbacks = mCallbacks.get();
            if (oldCallbacks == null) {
                // This launcher has exited and nobody bothered to tell us.  Just bail.
                Log.w(TAG, "LoaderTask running with no launcher");
                return;
            }

            final boolean isLoadingSynchronously = (synchronizeBindPage > -1);
            final int currentScreen = isLoadingSynchronously ? synchronizeBindPage :
                oldCallbacks.getCurrentWorkspaceScreen();

            // Load all the items that are on the current page first (and in the process, unbind
            // all the existing workspace items before we call startBinding() below.
            unbindWorkspaceItemsOnMainThread();
            ArrayList<ItemInfo> workspaceItems = new ArrayList<ItemInfo>();
            ArrayList<LauncherAppWidgetInfo> appWidgets =
                    new ArrayList<LauncherAppWidgetInfo>();
            HashMap<Long, FolderInfo> folders = new HashMap<Long, FolderInfo>();
            HashMap<Long, ItemInfo> itemsIdMap = new HashMap<Long, ItemInfo>();
            ArrayList<Long> orderedScreenIds = new ArrayList<Long>();
            synchronized (sBgLock) {
                workspaceItems.addAll(sBgWorkspaceItems);
                appWidgets.addAll(sBgAppWidgets);
                folders.putAll(sBgFolders);
                itemsIdMap.putAll(sBgItemsIdMap);
                orderedScreenIds.addAll(sBgWorkspaceScreens);
            }

            ArrayList<ItemInfo> currentWorkspaceItems = new ArrayList<ItemInfo>();
            ArrayList<ItemInfo> otherWorkspaceItems = new ArrayList<ItemInfo>();
            ArrayList<LauncherAppWidgetInfo> currentAppWidgets =
                    new ArrayList<LauncherAppWidgetInfo>();
            ArrayList<LauncherAppWidgetInfo> otherAppWidgets =
                    new ArrayList<LauncherAppWidgetInfo>();
            HashMap<Long, FolderInfo> currentFolders = new HashMap<Long, FolderInfo>();
            HashMap<Long, FolderInfo> otherFolders = new HashMap<Long, FolderInfo>();

            // Separate the items that are on the current screen, and all the other remaining items
            filterCurrentWorkspaceItems(currentScreen, workspaceItems, currentWorkspaceItems,
                    otherWorkspaceItems);
            filterCurrentAppWidgets(currentScreen, appWidgets, currentAppWidgets,
                    otherAppWidgets);
            filterCurrentFolders(currentScreen, itemsIdMap, folders, currentFolders,
                    otherFolders);
            sortWorkspaceItemsSpatially(currentWorkspaceItems);
            sortWorkspaceItemsSpatially(otherWorkspaceItems);

            // Tell the workspace that we're about to start binding items
            r = new Runnable() {
                public void run() {
                    Callbacks callbacks = tryGetCallbacks(oldCallbacks);
                    if (callbacks != null) {
                        callbacks.startBinding();
                    }
                }
            };
            runOnMainThread(r, MAIN_THREAD_BINDING_RUNNABLE);
            bindWorkspaceScreens(oldCallbacks, orderedScreenIds);

            // Load items on the current page
            bindWorkspaceItems(oldCallbacks, currentWorkspaceItems, currentAppWidgets,
                    currentFolders, null);
            if (isLoadingSynchronously) {
                r = new Runnable() {
                    public void run() {
                        Callbacks callbacks = tryGetCallbacks(oldCallbacks);
                        if (callbacks != null) {
                            callbacks.onPageBoundSynchronously(currentScreen);
                        }
                    }
                };
                runOnMainThread(r, MAIN_THREAD_BINDING_RUNNABLE);
            }

            // Load all the remaining pages (if we are loading synchronously, we want to defer this
            // work until after the first render)
            mDeferredBindRunnables.clear();
            bindWorkspaceItems(oldCallbacks, otherWorkspaceItems, otherAppWidgets, otherFolders,
                    (isLoadingSynchronously ? mDeferredBindRunnables : null));

            // Tell the workspace that we're done binding items
            r = new Runnable() {
                public void run() {
                    Callbacks callbacks = tryGetCallbacks(oldCallbacks);
                    if (callbacks != null) {
                        callbacks.finishBindingItems(isUpgradePath);
                    }

                    // If we're profiling, ensure this is the last thing in the queue.
                    if (DEBUG_LOADERS) {
                        Log.d(TAG, "bound workspace in "
                            + (SystemClock.uptimeMillis()-t) + "ms");
                    }

                    mIsLoadingAndBindingWorkspace = false;
                }
            };
            if (isLoadingSynchronously) {
                mDeferredBindRunnables.add(r);
            } else {
                runOnMainThread(r, MAIN_THREAD_BINDING_RUNNABLE);
            }
        }

        private void loadAndBindAllApps() {
            if (LauncherLog.DEBUG_LOADER) {
                LauncherLog.d(TAG, "loadAndBindAllApps: mAllAppsLoaded =" + mAllAppsLoaded
                        + ", mStopped = " + mStopped + ", this = " + this);
            }
            if (!mAllAppsLoaded) {/// M: Add for op09 Edit and Hide app icons.
                if (false/*LauncherExtPlugin.getInstance().getOperatorCheckerExt(mContext).supportEditAndHideApps()*/) {    /// M: For Edit AllAppsList for op09.
                    loadAndBindAllAppsList();
                } else {
                    loadAllApps();
                }
                synchronized (LoaderTask.this) {
                    if (mStopped) {
                        LauncherLog.d(TAG, "loadAndBindAllApps returned by stop flag.");
                        return;
                    }
                    mAllAppsLoaded = true;
                }
            } else {
                onlyBindAllApps();
            }
        }

        private void onlyBindAllApps() {
            final Callbacks oldCallbacks = mCallbacks.get();
            if (oldCallbacks == null) {
                // This launcher has exited and nobody bothered to tell us.  Just bail.
                Log.w(TAG, "LoaderTask running with no launcher (onlyBindAllApps)");
                return;
            }

            if (DEBUG_LOADERS) {
                LauncherLog.d(TAG, "onlyBindAllApps: oldCallbacks =" + oldCallbacks + ", this = " + this);
            }

            // shallow copy
            @SuppressWarnings("unchecked")
            final ArrayList<AppInfo> list
                    = (ArrayList<AppInfo>) mBgAllAppsList.data.clone();
            Runnable r = new Runnable() {
                public void run() {
                    final long t = SystemClock.uptimeMillis();
                    final Callbacks callbacks = tryGetCallbacks(oldCallbacks);
                    if (callbacks != null) {
                        callbacks.bindAllApplications(list);
                    }
                    if (DEBUG_LOADERS) {
                        Log.d(TAG, "bound all " + list.size() + " apps from cache in "
                                + (SystemClock.uptimeMillis()-t) + "ms");
                    }
                }
            };
            boolean isRunningOnMainThread = !(sWorkerThread.getThreadId() == Process.myTid());
            if (isRunningOnMainThread) {
                r.run();
            } else {
                mHandler.post(r);
            }
        }

        private void loadAllApps() {
            final long loadTime = DEBUG_LOADERS ? SystemClock.uptimeMillis() : 0;

            final Callbacks oldCallbacks = mCallbacks.get();
            if (oldCallbacks == null) {
                // This launcher has exited and nobody bothered to tell us.  Just bail.
                Log.w(TAG, "LoaderTask running with no launcher (loadAllApps)");
                return;
            }

            final PackageManager packageManager = mContext.getPackageManager();
            final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
            mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

            // Clear the list of apps
            mBgAllAppsList.clear();

            // Query for the set of apps
            final long qiaTime = DEBUG_LOADERS ? SystemClock.uptimeMillis() : 0;
            List<ResolveInfo> apps = packageManager.queryIntentActivities(mainIntent, 0);
            if (DEBUG_LOADERS) {
                Log.d(TAG, "queryIntentActivities took "
                        + (SystemClock.uptimeMillis()-qiaTime) + "ms");
                Log.d(TAG, "queryIntentActivities got " + apps.size() + " apps");
            }
            // Fail if we don't have any apps
            if (apps == null || apps.isEmpty()) {
                return;
            }
            // Sort the applications by name
            final long sortTime = DEBUG_LOADERS ? SystemClock.uptimeMillis() : 0;
            Collections.sort(apps,
                    new LauncherModel.ShortcutNameComparator(packageManager, mLabelCache));
            if (DEBUG_LOADERS) {
                Log.d(TAG, "sort took "
                        + (SystemClock.uptimeMillis()-sortTime) + "ms");
            }

            // Create the ApplicationInfos
            for (int i = 0; i < apps.size(); i++) {
                ResolveInfo app = apps.get(i);
                // This builds the icon bitmaps.
                mBgAllAppsList.add(new AppInfo(packageManager, app,
                        mIconCache, mLabelCache));
            }
            
            //add  by zhouerlong 修改SIM 在小米syle模式下空缺问题 
       if (AppsCustomizePagedView.DISABLE_ALL_APPS) {

//      		mBgAllAppsList.removePackage("com.android.inputmethod.latin");
//      		mBgAllAppsList.removePackage("com.google.android.inputmethod.pinyin");
//	if(TelephonyManager.SIM_STATE_READY != mTelephonyManager.getSimState()){
				
//				mBgAllAppsList.removeSTKActivity();
//			}
//    	   mBgAllAppsList.removePackage("com.android.launcher3");
//    	   mBgAllAppsList.removePackage("com.android.music");
       }
			//add by zhouerlong
            mBgAllAppsList.reorderApplist();

            // Huh? Shouldn't this be inside the Runnable below?
            final ArrayList<AppInfo> added = mBgAllAppsList.added;
            mBgAllAppsList.added = new ArrayList<AppInfo>();

            // Post callback on main thread
            mHandler.post(new Runnable() {
                public void run() {
                    final long bindTime = SystemClock.uptimeMillis();
                    final Callbacks callbacks = tryGetCallbacks(oldCallbacks);
                    if (callbacks != null) {
                        callbacks.bindAllApplications(added);
                        if (DEBUG_LOADERS) {
                            Log.d(TAG, "bound " + added.size() + " apps in "
                                + (SystemClock.uptimeMillis() - bindTime) + "ms");
                        }
                    } else {
                        Log.i(TAG, "not binding apps: no Launcher activity");
                    }
                }
            });

            if (DEBUG_LOADERS) {
                Log.d(TAG, "Icons processed in "
                        + (SystemClock.uptimeMillis() - loadTime) + "ms");
            }
        }

        /// M: Add for op09 start. @{

        /**
         * M: Load and bind all apps list, add for OP09.
         */
        private void loadAndBindAllAppsList() {
            final long t = DEBUG_LOADERS ? SystemClock.uptimeMillis() : 0;

            // Don't use these two variables in any of the callback runnables.
            // Otherwise we hold a reference to them.
            final Callbacks oldCallbacks = mCallbacks.get();
            if (oldCallbacks == null) {
                // This launcher has exited and nobody bothered to tell us. Just
                // bail.
                Log.w(TAG, "LoaderTask running with no launcher (loadAndBindAllAppsList)");
                return;
            }

            mBgAllAppsList.clear();
            ArrayList<AppInfo> apps = loadAllAppsExtend();
            final int appSize = apps.size();
            for (int i = 0; i < appSize; i++) {
                mBgAllAppsList.add(apps.get(i));
            }
            final Callbacks callbacks = tryGetCallbacks(oldCallbacks);
            final ArrayList<AppInfo> added = mBgAllAppsList.added;
            mBgAllAppsList.added = new ArrayList<AppInfo>();

            mHandler.post(new Runnable() {
                public void run() {
                    final long t = SystemClock.uptimeMillis();
                    if (callbacks != null) {
                        callbacks.bindAllApplications(added);
                        if (DEBUG_LOADERS) {
                            LauncherLog.d(TAG, "bound " + added.size() + " apps in "
                                    + (SystemClock.uptimeMillis() - t) + "ms");
                        }
                    } else {
                        LauncherLog.i(TAG, "not binding apps: no Launcher activity");
                    }
                }
            });
        }

        private class ItemPosition {
            int screen;
            int pos;

            public ItemPosition(int screen, int pos) {
                this.screen = screen;
                this.pos = pos;
            }
        }

        /**
         * M: Only load all apps list, we need to do this by two steps, first
         * load from the default all apps list(database), then load all remains
         * by querying package manager service, add for OP09.
         *
         * @return
         */
        private ArrayList<AppInfo> loadAllAppsExtend() {
            final long t = DEBUG_LOADERS ? SystemClock.uptimeMillis() : 0;

            final ArrayList<AppInfo> added = mBgAllAppsList.added;
            mBgAllAppsList.added = new ArrayList<AppInfo>();

            ArrayList<AppInfo> allApps = new ArrayList<AppInfo>();
            sMaxAppsPageIndex = 0;
            mCurrentPosInMaxPage = 0;

            final Context context = mContext;
            final ContentResolver contentResolver = context.getContentResolver();
            final PackageManager packageManager = context.getPackageManager();
            final AppWidgetManager widgets = AppWidgetManager.getInstance(context);

            // Make sure the default app list is loaded.
           /* final boolean loadDefault = LauncherExtPlugin.getInstance().getLoadDataExt(mContext)
                    .loadDefaultAllAppsIfNecessary(LauncherProvider.getSQLiteDatabase());*/

            ArrayList<ItemPosition> itemsRemoved = new ArrayList<ItemPosition>();
            boolean bNeedReorder = false;

            final Cursor c = contentResolver.query(AllApps.CONTENT_URI, null,
                    null, null, null);
            try {
                final int idIndex = c.getColumnIndexOrThrow(AllApps._ID);
                final int intentIndex = c.getColumnIndexOrThrow(AllApps.INTENT);
                final int titleIndex = c.getColumnIndexOrThrow(AllApps.TITLE);
                final int itemTypeIndex = c
                        .getColumnIndexOrThrow(AllApps.ITEM_TYPE);
                final int screenIndex = c.getColumnIndexOrThrow(AllApps.SCREEN);
                final int cellXIndex = c.getColumnIndexOrThrow(AllApps.CELLX);
                final int cellYIndex = c.getColumnIndexOrThrow(AllApps.CELLY);
                final int visibleIndex = c
                        .getColumnIndexOrThrow(AllApps.VISIBLE_FLAG);

                AppInfo info;
                String intentDescription;
                long id;
                Intent intent;
                int visible;
                int itemType;

                while (!mStopped && c.moveToNext()) {
                    itemType = c.getInt(itemTypeIndex);
                    intentDescription = c.getString(intentIndex);
                    try {
                        intent = Intent.parseUri(intentDescription, 0);
                    } catch (URISyntaxException e) {
                        LauncherLog.w(TAG, "loadAllApps, parse Intent Uri error: " + intentDescription);
                        continue;
                    }

                    info = getApplicationInfo(packageManager, intent, context, c, titleIndex);
                    visible = c.getInt(visibleIndex);

                    if (info != null) {
                        info.intent = intent;
                        info.id = c.getLong(idIndex);
                        info.screenId = c.getInt(screenIndex);
                        info.cellX = c.getInt(cellXIndex);
                        info.cellY = c.getInt(cellYIndex);
                        info.pos = info.cellY * AllApps.sAppsCellCountX + info.cellX;
                        info.isVisible = (visible == 1);
                        if (info.screenId > sMaxAppsPageIndex) {
                            sMaxAppsPageIndex = (int) info.screenId;
                            mCurrentPosInMaxPage = info.pos;
                        }

                        if (info.screenId == sMaxAppsPageIndex && info.pos > mCurrentPosInMaxPage) {
                            mCurrentPosInMaxPage = info.pos;
                        }

                        final ResolveInfo resolveInfo = packageManager.resolveActivity(intent, 0);

                        /// M: Remove the item whose resolve info is null
                        if (resolveInfo == null || resolveInfo.activityInfo.packageName == null) {
                            itemsRemoved.add(new ItemPosition((int) info.screenId, info.pos));
                            id = c.getLong(idIndex);
                            contentResolver.delete(AllApps.getContentUri(id, false), null, null);
                            bNeedReorder = true;
                            LauncherLog.w(TAG, "loadAllApps: Error getting application info " + id + ", removing it");
                        } else {
                            mIconCache.getTitleAndIcon(info, resolveInfo, mLabelCache);
                            allApps.add(info);
                        }
                    } else {
                        // Failed to load the shortcut, probably because the activity manager couldn't resolve it
                        // (maybe the app was uninstalled), or the db row was somehow screwed up.
                        // Delete it.
                        final int pos = c.getInt(cellYIndex) * AllApps.sAppsCellCountX + c.getInt(cellXIndex);
                        itemsRemoved.add(new ItemPosition(c.getInt(screenIndex), pos));
                        id = c.getLong(idIndex);
                        contentResolver.delete(AllApps.getContentUri(id, false), null, null);
                        bNeedReorder = true;
                        LauncherLog.w(TAG, "loadAllApps: Error getting application info " + id + ", removing it");
                    }
                }
            } finally {
                c.close();
            }

            /// M: Judge the LauncherApplication start flag to decide check PMS or not @{
            LauncherApplication app = (LauncherApplication) context.getApplicationContext();
            if (app.isTotalStart()) {
                if (DEBUG_MODEL) {
                    LauncherLog.d(TAG, "loadAllApps, LauncherApplication is started from beginning! " +
                            "So check the apps left in PMS. " + allApps.size() + "apps is already loaded before it.");
                }
                checkAndAddAppsInPackageManagerButNotInDB(allApps);
                app.resetTotalStartFlag();
            }
            /// M: }@

            if (bNeedReorder) {
                reorderAllApps(allApps, itemsRemoved);
            }

            if (DEBUG_MODEL) {
//                LauncherLog.d(TAG, "loadAllApps: loadDefault = " + loadDefault);
                for (AppInfo info : allApps) {
                    LauncherLog.d(TAG, "loadAllApps: load " + info);
                }
            }

            return allApps;
        }

        /**
         * M: To dispose the APKs which are in db but can't query from
         * PackageManager, so after remove them, the left ones need reorder.
         */
        private void reorderAllApps(ArrayList<AppInfo> allApps,
                ArrayList<ItemPosition> itemsRemoved) {
            ArrayList<AppInfo> itemsInTheSameScreenButAfterPosition = null;
            ArrayList<AppInfo> itemsInTheAfterScreen = null;
            LauncherLog.d(TAG, "reorderAllApps: There are " + itemsRemoved.size()
                    + " removed items.");

            for (ItemPosition removedItemPosition : itemsRemoved) {
                LauncherLog.d(TAG, "reorderAllApps: The removed items is at screen="
                        + removedItemPosition.screen + ", pos=" + removedItemPosition.pos);

                boolean bOnlyOneItemInTheScreen = true;

                for (AppInfo appInfo : allApps) {
                    if (appInfo.screenId == removedItemPosition.screen
                            && appInfo.pos > removedItemPosition.pos) {
                        if (itemsInTheSameScreenButAfterPosition == null) {
                            itemsInTheSameScreenButAfterPosition = new ArrayList<AppInfo>();
                        }
                        LauncherLog.d(TAG, "Add one item which are in the same screen "
                                + "with removed item and at cellX=" + appInfo.cellX + ", cellY="
                                + appInfo.cellY);
                        itemsInTheSameScreenButAfterPosition.add(appInfo);
                    }

                    if (bOnlyOneItemInTheScreen && appInfo.screenId == removedItemPosition.screen) {
                        bOnlyOneItemInTheScreen = false;
                    }
                }

                if (bOnlyOneItemInTheScreen) {
                    for (AppInfo appInfo : allApps) {
                        if (appInfo.screenId > removedItemPosition.screen) {
                            if (itemsInTheAfterScreen == null) {
                                itemsInTheAfterScreen = new ArrayList<AppInfo>();
                            }
                            itemsInTheAfterScreen.add(appInfo);
                        }
                    }
                }

                if (itemsInTheSameScreenButAfterPosition != null
                        && itemsInTheSameScreenButAfterPosition.size() > 0) {
                    LauncherLog.d(TAG, "reorderAllApps: itemsInTheSameScreenAndAfterRemovedOne number is "
                                    + itemsInTheSameScreenButAfterPosition.size());
                    int newX = -1;
                    int newY = -1;
                    for (AppInfo appInfo : itemsInTheSameScreenButAfterPosition) {
                        appInfo.pos -= 1;

                        newX = appInfo.pos % AllApps.sAppsCellCountX;
                        newY = appInfo.pos / AllApps.sAppsCellCountX;
                        LauncherLog.d(TAG, "reorderAllApps: move item from (" + appInfo.cellX + ","
                                + appInfo.cellY + ") to (" + newX + "," + newY + ").");

                        moveAllAppsItemInDatabase(mContext, appInfo, (int) appInfo.screenId, newX, newY);
                    }
                } else {
                    if (itemsInTheAfterScreen != null && itemsInTheAfterScreen.size() > 0) {
                        LauncherLog.d(TAG, "reorderAllApps: itemsInBiggerScreen number is "
                                + itemsInTheAfterScreen.size());
                        for (AppInfo appInfo : itemsInTheAfterScreen) {
                            LauncherLog.d(TAG, "reorderAllApps: move item (" + appInfo.cellX + ","
                                    + appInfo.cellY + "). from screen " + appInfo.screenId
                                    + " to the forward one.");
                            moveAllAppsItemInDatabase(mContext, appInfo, (int) (appInfo.screenId - 1),
                                    appInfo.cellX, appInfo.cellY);
                        }
                    }
                }
            }
        }

        /**
         * M: Check and add the apps which are in PMS but not in Launcher.db.
         */
        private void checkAndAddAppsInPackageManagerButNotInDB(ArrayList<AppInfo> allApps) {
            Intent intent = new Intent(Intent.ACTION_MAIN, null);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            final PackageManager packageManager = mContext.getPackageManager();
            List<ResolveInfo> resolveInfos = null;

            resolveInfos = packageManager.queryIntentActivities(intent, 0);
            if (resolveInfos == null || resolveInfos.size() == 0) {
                LauncherLog.e(TAG, "queryIntentActivities got null or zero!");
                return;
            }
            List<ComponentName> componentNames = new ArrayList<ComponentName>();
            for (ResolveInfo resolveInfo : resolveInfos) {
                final String packageName = resolveInfo.activityInfo.applicationInfo.packageName;
                componentNames.add(new ComponentName(packageName, resolveInfo.activityInfo.name));
            }

            if (DEBUG_MODEL) {
                LauncherLog.d(TAG, "checkPackageManagerForAppsNotInDB, query PMS got "
                        + componentNames.size() + " apps");
            }

            int index = -1;
            for (AppInfo appInfo : allApps) {
                index = componentNames.indexOf(appInfo.componentName);
                if (index != -1) {
                    componentNames.remove(index);
                    resolveInfos.remove(index);
                }
            }
            addAppsInPMButNotInDB(allApps, resolveInfos, componentNames, packageManager);

            return;
        }

        /**
         * M: Add apps in PM not in DB.
         */
        private void addAppsInPMButNotInDB(ArrayList<AppInfo> allApps, List<ResolveInfo> resolveInfos,
                List<ComponentName> componentNames, PackageManager packageManager) {
            int onePageAppsNumber = AllApps.sAppsCellCountX
                    * AllApps.sAppsCellCountY;
            AppInfo appInfo = null;
            ResolveInfo resolveInfo = null;
            ComponentName cmpName = null;
            Intent intent = null;
            int leftAppNumber = componentNames.size();
            if (DEBUG_MODEL) {
                LauncherLog.d(TAG, "checkPackageManagerForAppsNotInDB, there are " + leftAppNumber
                        + " apps left in PMS.");
            }

            for (int i = 0; i < leftAppNumber; ++i) {
                cmpName = componentNames.get(i);
                resolveInfo = resolveInfos.get(i);

                if (DEBUG_MODEL) {
                    LauncherLog.d(TAG, "checkPackageManagerForAppsNotInDB, dipose " + cmpName);
                }

                appInfo = new AppInfo(packageManager, resolveInfo, mIconCache, mLabelCache);
                appInfo.setFlagAndInstallTime(packageManager);
                appInfo.title = resolveInfo.loadLabel(packageManager).toString();
                if (appInfo.title == null) {
                    appInfo.title = cmpName.getClassName();
                }

                intent = new Intent(Intent.ACTION_MAIN, null);
                intent.addCategory(Intent.CATEGORY_LAUNCHER);
                intent.setComponent(cmpName);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                appInfo.intent = intent;
                appInfo.componentName = cmpName;
                appInfo.itemType = LauncherSettings.Favorites.ITEM_TYPE_APPLICATION;

                if (onePageAppsNumber >= mCurrentPosInMaxPage) {
                    sMaxAppsPageIndex += 1;
                    mCurrentPosInMaxPage = 0;
                } else {
                    mCurrentPosInMaxPage += 1;
                }

                if (DEBUG_MODEL) {
                    LauncherLog.d(TAG, "checkPackageManagerForAppsNotInDB, Max page is "
                            + sMaxAppsPageIndex + ", current pos in max page is " + mCurrentPosInMaxPage);
                }

                appInfo.screenId = sMaxAppsPageIndex;
                appInfo.pos = mCurrentPosInMaxPage;
                appInfo.cellX = appInfo.pos % AllApps.sAppsCellCountX;
                appInfo.cellY = appInfo.pos / AllApps.sAppsCellCountX;
                appInfo.isVisible = true;

                if (DEBUG_MODEL) {
                    LauncherLog.d(TAG, "checkPackageManagerForAppsNotInDB, insert " + cmpName
                            + " into " + " page=" + appInfo.screenId + ", cellX=" + appInfo.cellX
                            + ", cellY=" + appInfo.cellY + ", pos=" + appInfo.pos);
                }

                allApps.add(appInfo);
                addAllAppsItemToDatabase(mContext, appInfo, (int) appInfo.screenId, appInfo.cellX,
                        appInfo.cellY, false);
            }
        }

        /// M: Add for op09 end. }@

        public void dumpState() {
            synchronized (sBgLock) {
                Log.d(TAG, "mLoaderTask.mContext=" + mContext);
                Log.d(TAG, "mLoaderTask.mIsLaunching=" + mIsLaunching);
                Log.d(TAG, "mLoaderTask.mStopped=" + mStopped);
                Log.d(TAG, "mLoaderTask.mLoadAndBindStepFinished=" + mLoadAndBindStepFinished);
                Log.d(TAG, "mItems size=" + sBgWorkspaceItems.size());
            }
        }
    }

    void enqueuePackageUpdated(PackageUpdatedTask task) {
        sWorker.post(task);
    }

	class DownloadProgressTaskFromAppstore implements Runnable {
		DownLoadTaskInfo mInfo;
		public DownloadProgressTaskFromAppstore(DownLoadTaskInfo mInfo) {
			super();
			this.mInfo = mInfo;
		}
		@Override
		public void run() {
//			modifyItemInDatabaseByDownLoadProgress(context, item);

            mHandler.post(new Runnable() {
                public void run() {
                    Callbacks cb = mCallbacks != null ? mCallbacks.get() : null;

                    ItemInfo info = getItemInfoForPackageNameFromAppStore(mInfo.pkgName);
                    if( info !=null && cb !=null && mInfo != null) {
                        info.progress = mInfo.progress;
                        if(info.down_state != mInfo.state) {
                            info.down_state = mInfo.state;
                        }
                        
//                       Context c = LauncherApplication.getInstance().getApplicationContext();
//                       String title =info.title.toString();
                     /*  if(info.down_state == DownLoadService.APP_STATE_INSTALLING) {
//                          	Log.i("zhouerlong", "安装中。。。。。。"+info.down_state+"   进度::::"+info.progress);
                       	 title = c.getString(R.string.installing);
                       	
                       }else if(info.down_state == DownLoadService.STATE_DOWNLOAD_WAIT) {
                       	title = c.getString(R.string.waiting);
                       	
                       }else if(info.down_state == DownLoadService.STATE_DOWNLOAD_START_LOADING){
                          	title = c.getString(R.string.downloading);
//                       	Log.i("zhouerlong", "下载中。。。。。。"+info.down_state+"   进度::::"+info.progress);
                       	info.title=title;
                       }else if(info.down_state == DownLoadService.STATE_DOWNLOAD_PAUSE){
//                       	Log.i("zhouerlong", "暂停。。。。。。"+info.down_state+"   进度::::"+info.progress);
                       	title = "暂停";
                      	title = c.getString(R.string.pause);
                       }*/
//                        info.title = title;
                        modifyItemInDatabaseByDownLoadDate(mApp.getContext(),info);
                        try {
                            cb.bindDownProgress(mInfo);
						} catch (Exception e) {
							if(mlauncher!=null) {
								mlauncher.bindDownProgress(mInfo);
							}
							e.printStackTrace();
						}
                    }
                }
            });
		}

	}

    void enqueuePackageUpdatedFromAppStroe(PackageUpdatedTaskFromAppStore task) {
        sWorker.post(task);
    }

	private boolean isFinish;
    private class PackageUpdatedTask implements Runnable {
        int mOp;
        String[] mPackages;

        public static final int OP_NONE = 0;
        public static final int OP_ADD = 1;
        public static final int OP_UPDATE = 2;
        public static final int OP_REMOVE = 3; // uninstlled
        public static final int OP_UNAVAILABLE = 4; // external media unmounted


        public PackageUpdatedTask(int op, String[] packages) {
            mOp = op;
            mPackages = packages;
        }

        public void run() {
            final Context context = mApp.getContext();

            final String[] packages = mPackages;
            final int N = packages.length;
            boolean isFromAppStores = false;
            if(mPackages.length==1) {
            	String pkg = packages[0];

                final int fromAppstore = 1;
                if(installExistsFromAppStore(context,pkg,fromAppstore)) {
                	isFromAppStores = true;
                	mOp=OP_UPDATE;
                }
            }
            
            switch (mOp) {
                case OP_ADD:
                    for (int i=0; i<N; i++) {
                        if (DEBUG_LOADERS) Log.d(TAG, "mAllAppsList.addPackage " + packages[i]);
                        mBgAllAppsList.addPackage(context, packages[i]);
                    }
                    break;
                case OP_UPDATE:
                    for (int i=0; i<N; i++) {
                        if (DEBUG_LOADERS) Log.d(TAG, "mAllAppsList.updatePackage " + packages[i]);
            
			            if(!isFinish) {
			            	return;
			            }
                        mBgAllAppsList.updatePackage(context, packages[i],isFromAppStores);
                        WidgetPreviewLoader.removePackageFromDb(
                                mApp.getWidgetPreviewCacheDb(), packages[i]);
                    }
                    break;
                case OP_REMOVE:
                case OP_UNAVAILABLE:
                    for (int i=0; i<N; i++) {
                        if (DEBUG_LOADERS) Log.d(TAG, "mAllAppsList.removePackage " + packages[i]);
                        mBgAllAppsList.removePackage(packages[i]);
                        WidgetPreviewLoader.removePackageFromDb(
                                mApp.getWidgetPreviewCacheDb(), packages[i]);
                    }
                    break;
            }

            ArrayList<AppInfo> added = null;
            ArrayList<AppInfo> modified = null;
            final ArrayList<AppInfo> removedApps = new ArrayList<AppInfo>();

            if (mBgAllAppsList.added.size() > 0) {
                added = new ArrayList<AppInfo>(mBgAllAppsList.added);
                mBgAllAppsList.added.clear();
            }
            if (mBgAllAppsList.modified.size() > 0) {
                modified = new ArrayList<AppInfo>(mBgAllAppsList.modified);
                mBgAllAppsList.modified.clear();
            }
            if (mBgAllAppsList.removed.size() > 0) {
                removedApps.addAll(mBgAllAppsList.removed);
                mBgAllAppsList.removed.clear();
            }

            final Callbacks callbacks = mCallbacks != null ? mCallbacks.get() : null;
            if (callbacks == null) {
                Log.w(TAG, "Nobody to tell about the new app.  Launcher is probably loading.");
                return;
            }

            if (DEBUG_MODEL) {
                LauncherLog.d(TAG, "PackageUpdatedTask: added = " + added + ",modified = "
                        + modified + ",removedApps = " + removedApps);
            }

            if (added != null) {
                // Ensure that we add all the workspace applications to the db
                Callbacks cb = mCallbacks != null ? mCallbacks.get() : null;
                if (!AppsCustomizePagedView.DISABLE_ALL_APPS) {
                    addAndBindAddedApps(context, new ArrayList<ItemInfo>(), cb, added);
                } else {
                    final ArrayList<ItemInfo> addedInfos = new ArrayList<ItemInfo>(added);
                    addItemDataBaseWithFirstInstall(added);
                    addAndBindAddedApps(context, addedInfos, cb, added);
                }
            }
            if (modified != null) {
                final ArrayList<AppInfo> modifiedFinal = modified;
                
                if(isFromAppStores) {
                		AppInfo app = modified.get(0);
                		app.packageName = app.componentName.getPackageName();
                        ItemInfo info = getItemInfoForPackageNameFromAppStore(app.packageName);
                        if( info != null && info instanceof ShortcutInfo) {
//                        	mDownLoadService.pikerRemoveCurrentTask(app.packageName);
                        	ShortcutInfo shortcut = (ShortcutInfo) info;
                        	shortcut.makeModifyShortcutInfo(app);
                        	info.fromAppStore=0;
                        	info.firstInstall=1;
                        	info.itemType = LauncherSettings.Favorites.ITEM_TYPE_APPLICATION;
                        	
                        }
                        updateItemInDatabase(context, info);
                }
                // Update the launcher db to reflect the changes
                for (AppInfo a : modifiedFinal) {
                    ArrayList<ItemInfo> infos =
                            getItemInfoForComponentName(a.componentName);
                    for (ItemInfo i : infos) {
                        if (isShortcutInfoUpdateable(i)) {
                            ShortcutInfo info = (ShortcutInfo) i;
                            info.title = a.title.toString();
                            updateItemInDatabase(context, info);
                        }
                    }
                }

                addItemDataBaseWithFirstInstall(modifiedFinal);
                final boolean fromAppStore = isFromAppStores;
                mHandler.post(new Runnable() {
                    public void run() {
                        Callbacks cb = mCallbacks != null ? mCallbacks.get() : null;
                        if (callbacks == cb && cb != null) {
                            callbacks.bindAppsUpdated(modifiedFinal,fromAppStore);
                        }
                    }
                });
            }
            // If a package has been removed, or an app has been removed as a result of
            // an update (for example), make the removed callback.
            if (mOp == OP_REMOVE || !removedApps.isEmpty()) {
                /// M: [ALPS01273634] Do not remove shortcut from workspace when receiving ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE.
                final boolean permanent = mOp != OP_UNAVAILABLE;
                final boolean packageRemoved = (mOp == OP_REMOVE);
                final ArrayList<String> removedPackageNames =
                        new ArrayList<String>(Arrays.asList(packages));

                /// M: [ALPS01273634] Do not remove shortcut from workspace when receiving ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE.
                if (permanent) {
                    // Update the launcher db to reflect the removal of apps
                    if (packageRemoved) {
                        for (String pn : removedPackageNames) {
                            ArrayList<ItemInfo> infos = getItemInfoForPackageName(pn);
                            for (ItemInfo i : infos) {
                                deleteItemFromDatabase(context, i);
                            }
                        }

                        // Remove any queued items from the install queue
                        String spKey = LauncherAppState.getSharedPreferencesKey();
                        SharedPreferences sp =
                                context.getSharedPreferences(spKey, Context.MODE_PRIVATE);
                        InstallShortcutReceiver.removeFromInstallQueue(sp, removedPackageNames);
                    } else {
                        for (AppInfo a : removedApps) {
                            ArrayList<ItemInfo> infos =
                                    getItemInfoForComponentName(a.componentName);
                            for (ItemInfo i : infos) {
                                deleteItemFromDatabase(context, i);
                            }
                        }
                    }
                }

                mHandler.post(new Runnable() {
                    public void run() {
                        Callbacks cb = mCallbacks != null ? mCallbacks.get() : null;
                        if (callbacks == cb && cb != null) {
                            /// M: [ALPS01273634] Do not remove shortcut from workspace when receiving ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE.
                            callbacks.bindComponentsRemoved(removedPackageNames,
                                    removedApps, packageRemoved, permanent);
                        }
                    }
                });
            }

            final ArrayList<Object> widgetsAndShortcuts =
                getSortedWidgetsAndShortcuts(context);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    Callbacks cb = mCallbacks != null ? mCallbacks.get() : null;
                    if (callbacks == cb && cb != null) {
                        callbacks.bindPackagesUpdated(widgetsAndShortcuts);
                    }
                }
            });

            // Write all the logs to disk
            mHandler.post(new Runnable() {
                public void run() {
                    Callbacks cb = mCallbacks != null ? mCallbacks.get() : null;
                    if (callbacks == cb && cb != null) {
                        callbacks.dumpLogsToLocalData();
                    }
                }
            });
        }
    }

    private class PackageUpdatedTaskFromAppStore implements Runnable {
        int mOp;
        String[] mPackages;

        public static final int OP_NONE = 0;
        public static final int OP_ADD = 1;
        public static final int OP_UPDATE = 2;
        public static final int OP_REMOVE = 3; // uninstlled
        public static final int OP_UNAVAILABLE = 4; // external media unmounted


        public PackageUpdatedTaskFromAppStore(int op, String[] packages) {
            mOp = op;
            mPackages = packages;
        }

        public void run() {
            final Context context = mApp.getContext();

            final String[] packages = mPackages;
            final int N = packages.length;
                    for (int i=0; i<N; i++) {
                        if (DEBUG_LOADERS) Log.d(TAG, "mAllAppsList.removePackage " + packages[i]);
                        mBgAllAppsList.removePackage(packages[i]);
                        WidgetPreviewLoader.removePackageFromDb(
                                mApp.getWidgetPreviewCacheDb(), packages[i]);
                    }
            final ArrayList<AppInfo> removedApps = new ArrayList<AppInfo>();
            if (mBgAllAppsList.removed.size() > 0) {
                removedApps.addAll(mBgAllAppsList.removed);
                mBgAllAppsList.removed.clear();
            }

            final Callbacks callbacks = mCallbacks != null ? mCallbacks.get() : null;
            if (callbacks == null) {
                Log.w(TAG, "Nobody to tell about the new app.  Launcher is probably loading.");
                return;
            }
            // If a package has been removed, or an app has been removed as a result of
            // an update (for example), make the removed callback.
            if (mOp == OP_REMOVE || !removedApps.isEmpty()) {
                /// M: [ALPS01273634] Do not remove shortcut from workspace when receiving ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE.
                final boolean permanent = mOp != OP_UNAVAILABLE;
                final boolean packageRemoved = (mOp == OP_REMOVE);
                final ArrayList<String> removedPackageNames =
                        new ArrayList<String>(Arrays.asList(packages));

                /// M: [ALPS01273634] Do not remove shortcut from workspace when receiving ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE.
                if (permanent) {
                    // Update the launcher db to reflect the removal of apps
                    if (packageRemoved) {
                        for (String pn : removedPackageNames) {
                            ItemInfo info = getItemInfoForPackageNameFromAppStore(pn);
                            if (info !=null)
                                deleteItemFromDatabase(context, info);
                        }

                        // Remove any queued items from the install queue
                        String spKey = LauncherAppState.getSharedPreferencesKey();
                        SharedPreferences sp =
                                context.getSharedPreferences(spKey, Context.MODE_PRIVATE);
                        InstallShortcutReceiver.removeFromInstallQueue(sp, removedPackageNames);
                    } else {
                        for (AppInfo a : removedApps) {
                            ArrayList<ItemInfo> infos =
                                    getItemInfoForComponentName(a.componentName);
                            for (ItemInfo i : infos) {
                                deleteItemFromDatabase(context, i);
                            }
                        }
                    }
                }

                mHandler.post(new Runnable() {
                    public void run() {
                        Callbacks cb = mCallbacks != null ? mCallbacks.get() : null;
                        if (callbacks == cb && cb != null) {
                            /// M: [ALPS01273634] Do not remove shortcut from workspace when receiving ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE.
                            callbacks.bindComponentsRemovedFromAppStore(removedPackageNames,
                                    removedApps, packageRemoved, permanent);
                        }
                    }
                });
            }

            final ArrayList<Object> widgetsAndShortcuts =
                getSortedWidgetsAndShortcuts(context);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    Callbacks cb = mCallbacks != null ? mCallbacks.get() : null;
                    if (callbacks == cb && cb != null) {
                        callbacks.bindPackagesUpdated(widgetsAndShortcuts);
                    }
                }
            });

            // Write all the logs to disk
            mHandler.post(new Runnable() {
                public void run() {
                    Callbacks cb = mCallbacks != null ? mCallbacks.get() : null;
                    if (callbacks == cb && cb != null) {
                        callbacks.dumpLogsToLocalData();
                    }
                }
            });
        }
    }

    // Returns a list of ResolveInfos/AppWindowInfos in sorted order
    public static ArrayList<Object> getSortedWidgetsAndShortcuts(Context context) {
        PackageManager packageManager = context.getPackageManager();
        final ArrayList<Object> widgetsAndShortcuts = new ArrayList<Object>();
        widgetsAndShortcuts.addAll(AppWidgetManager.getInstance(context).getInstalledProviders());
        Intent shortcutsIntent = new Intent(Intent.ACTION_CREATE_SHORTCUT);
        widgetsAndShortcuts.addAll(packageManager.queryIntentActivities(shortcutsIntent, 0));
        Collections.sort(widgetsAndShortcuts,
            new LauncherModel.WidgetAndShortcutNameComparator(packageManager));
        return widgetsAndShortcuts;
    }

    private boolean isValidPackageComponent(PackageManager pm, ComponentName cn) {
        if (cn == null) {
            return false;
        }

        try {
            // Skip if the application is disabled
            PackageInfo pi = pm.getPackageInfo(cn.getPackageName(), 0);
            if (!pi.applicationInfo.enabled) {
                return false;
            }

            // Check the activity
            return (pm.getActivityInfo(cn, 0) != null);
        } catch (NameNotFoundException e) {
            return false;
        }
    }

    /**
     * This is called from the code that adds shortcuts from the intent receiver.  This
     * doesn't have a Cursor, but
     */
    public ShortcutInfo getShortcutInfo(PackageManager manager, Intent intent, Context context) {
        return getShortcutInfo(manager, intent, context, null, -1, -1, null);
    }

    /**
     * Make an ShortcutInfo object for a shortcut that is an application.
     *
     * If c is not null, then it will be used to fill in missing data like the title and icon.
     */
    public ShortcutInfo getShortcutInfo(PackageManager manager, Intent intent, Context context,
            Cursor c, int iconIndex, int titleIndex, HashMap<Object, CharSequence> labelCache) {
        ComponentName componentName = intent.getComponent();
        final ShortcutInfo info = new ShortcutInfo();
        if (componentName != null && !isValidPackageComponent(manager, componentName)) {
            Log.d(TAG, "Invalid package found in getShortcutInfo: " + componentName);
            return null;
        } else {
            try {
                PackageInfo pi = manager.getPackageInfo(componentName.getPackageName(), 0);
                info.initFlagsAndFirstInstallTime(pi);
            } catch (NameNotFoundException e) {
                Log.d(TAG, "getPackInfo failed for package " +
                        componentName.getPackageName());
            }
        }

        // TODO: See if the PackageManager knows about this case.  If it doesn't
        // then return null & delete this.

        // the resource -- This may implicitly give us back the fallback icon,
        // but don't worry about that.  All we're doing with usingFallbackIcon is
        // to avoid saving lots of copies of that in the database, and most apps
        // have icons anyway.

        // Attempt to use queryIntentActivities to get the ResolveInfo (with IntentFilter info) and
        // if that fails, or is ambiguious, fallback to the standard way of getting the resolve info
        // via resolveActivity().
        Bitmap icon = null;
        ResolveInfo resolveInfo = null;
        ComponentName oldComponent = intent.getComponent();
        Intent newIntent = new Intent(intent.getAction(), null);
        newIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        newIntent.setPackage(oldComponent.getPackageName());
        List<ResolveInfo> infos = manager.queryIntentActivities(newIntent, 0);
        for (ResolveInfo i : infos) {
            ComponentName cn = new ComponentName(i.activityInfo.packageName,
                    i.activityInfo.name);
            if (cn.equals(oldComponent)) {
                resolveInfo = i;
            }
        }
        if (resolveInfo == null) {
            resolveInfo = manager.resolveActivity(intent, 0);
        }
        if (resolveInfo != null) {
            icon = mIconCache.getIcon(componentName, resolveInfo, labelCache);
        }
        // the db
        if (icon == null) {
            if (c != null) {
                icon = getIconFromCursor(c, iconIndex, context);
            }
        }
        // the fallback icon
        if (icon == null) {
            icon = getFallbackIcon();
            info.usingFallbackIcon = true;
        }
        info.setIcon(icon);

        final int config_name = c.getColumnIndexOrThrow(
                LauncherSettings.Favorites.CONFIG_NAME);
        info.configName = c.getString(config_name);

        // from the resource
        if (resolveInfo != null) {
            ComponentName key = LauncherModel.getComponentNameFromResolveInfo(resolveInfo);
            if (labelCache != null && labelCache.containsKey(key)) {
                info.title = labelCache.get(key);
            } else {
                info.title = resolveInfo.activityInfo.loadLabel(manager);
                if (labelCache != null) {
                    labelCache.put(key, info.title);
                }
            }
        }
        
        if(info.configName!=null) {
        	info.title = info.configName;
        }
       info.id = c.getLong(c.getColumnIndex(Favorites._ID));
        if(info.title!=null) {
//        	modifyItemInDatabaseByLanguage(mApp.getContext(),info);
        }
        // from the db
        if (info.title == null) {
            if (c != null) {
                info.title =  c.getString(titleIndex);
            }
        }
        // fall back to the class name of the activity
        if (info.title == null) {
            info.title = componentName.getClassName();
        }
        info.itemType = LauncherSettings.Favorites.ITEM_TYPE_APPLICATION;
        return info;
    }

    static ArrayList<ItemInfo> filterItemInfos(Collection<ItemInfo> infos,
            ItemInfoFilter f) {
        HashSet<ItemInfo> filtered = new HashSet<ItemInfo>();
        for (ItemInfo i : infos) {
            if (i instanceof ShortcutInfo) {
                ShortcutInfo info = (ShortcutInfo) i;
                if (info.intent !=null) {
                    ComponentName cn = info.intent.getComponent();
                    if (cn != null && f.filterItem(null, info, cn)) {
                        filtered.add(info);
                    }
                }
            } else if (i instanceof FolderInfo) {
                FolderInfo info = (FolderInfo) i;
                for (ShortcutInfo s : info.contents) {
                	if(s.intent !=null) {
                        ComponentName cn = s.intent.getComponent();
                        if (cn != null && f.filterItem(info, s, cn)) {
                            filtered.add(s);
                        }
                	}
                }
            } else if (i instanceof LauncherAppWidgetInfo) {
                LauncherAppWidgetInfo info = (LauncherAppWidgetInfo) i;
                ComponentName cn = info.providerName;
                if (cn != null && f.filterItem(null, info, cn)) {
                    filtered.add(info);
                }
            }
        }
        return new ArrayList<ItemInfo>(filtered);
    }
    
	private ItemInfo getItemInfoForPackageNameFromAppStore(final String pn) {
		Iterator<Long> iterator = sBgItemsIdMap.keySet().iterator();
		while (iterator.hasNext()) {
			Long key = (Long) iterator.next();
			ItemInfo info = sBgItemsIdMap.get(key);
			if (info.packageName!=null&&info.packageName.equals(pn)) {
				return info;
			}

		}
		return null;
	}

    private ArrayList<ItemInfo> getItemInfoForPackageName(final String pn) {
        ItemInfoFilter filter  = new ItemInfoFilter() {
            @Override
            public boolean filterItem(ItemInfo parent, ItemInfo info, ComponentName cn) {
                return cn.getPackageName().equals(pn);
            }
        };
        return filterItemInfos(sBgItemsIdMap.values(), filter);
    }
    
    

    private ArrayList<ItemInfo> getItemInfoForComponentName(final ComponentName cname) {
        ItemInfoFilter filter  = new ItemInfoFilter() {
            @Override
            public boolean filterItem(ItemInfo parent, ItemInfo info, ComponentName cn) {
                return cn.equals(cname);
            }
        };
        return filterItemInfos(sBgItemsIdMap.values(), filter);
    }

    public static boolean isShortcutInfoUpdateable(ItemInfo i) {
        if (i instanceof ShortcutInfo) {
            ShortcutInfo info = (ShortcutInfo) i;
            // We need to check for ACTION_MAIN otherwise getComponent() might
            // return null for some shortcuts (for instance, for shortcuts to
            // web pages.)
            Intent intent = info.intent;
            if(intent ==null) {
            	return false;
            }
            ComponentName name = intent.getComponent();
            if (info.itemType == LauncherSettings.Favorites.ITEM_TYPE_APPLICATION &&
                    Intent.ACTION_MAIN.equals(intent.getAction()) && name != null) {
                return true;
            }
        }
        return false;
    }

    /**
     * Make an ShortcutInfo object for a shortcut that isn't an application.
     */
    private ShortcutInfo getShortcutInfo(Cursor c, Intent intent,Context context,
            int iconTypeIndex, int iconPackageIndex, int iconResourceIndex, int iconIndex,
            int titleIndex) {

        Bitmap icon = null;
        final ShortcutInfo info = new ShortcutInfo();
        info.itemType = LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT;

        // TODO: If there's an explicit component and we can't install that, delete it.

        info.title = c.getString(titleIndex);

        int iconType = c.getInt(iconTypeIndex);
        switch (iconType) {
        case LauncherSettings.Favorites.ICON_TYPE_RESOURCE:
            String packageName = c.getString(iconPackageIndex);
            String resourceName = c.getString(iconResourceIndex);
            PackageManager packageManager = context.getPackageManager();
            info.customIcon = false;
            // the resource
            try {
                Resources resources = packageManager.getResourcesForApplication(packageName);
                if (resources != null) {
                    final int id = resources.getIdentifier(resourceName, null, null);
                    icon = Utilities.createIconBitmap(
                            mIconCache.getFullResIcon(resources, id), context);
                }
            } catch (Exception e) {
                // drop this.  we have other places to look for icons
            }
            // the db
            if (icon == null) {
                icon = getIconFromCursor(c, iconIndex, context);
            }
            if(icon == null) {
            	
            }
            // the fallback icon
            if (icon == null) {
                icon = getFallbackIcon();
                info.usingFallbackIcon = true;
            }
            break;
        case LauncherSettings.Favorites.ICON_TYPE_BITMAP:
            icon = getIconFromCursor(c, iconIndex, context);
            if (icon == null) {
                icon = getFallbackIcon();
                info.customIcon = false;
                info.usingFallbackIcon = true;
            } else {
                info.customIcon = true;
            }
            break;
        default:
            icon = getFallbackIcon();
            info.usingFallbackIcon = true;
            info.customIcon = false;
            break;
        }

        if(icon!=null) {
        	if(Launcher.isSupportClone&&intent!=null&&intent.getAppInstanceIndex()==1) {
    			ComponentName comName = new ComponentName(intent.getComponent().getPackageName(),intent.getComponent().getClassName()+"s");

//            	icon = IconCache.getLqIcon(intent.getComponent(), icon, true, "");
            	icon = IconCache.getThemeIcon(comName, icon, true, "",context);

        	}else {

            	icon = IconCache.getLqIcon(null, icon, true, "");
        	}
        	if(icon.getHeight()!=Utilities.sIconTextureHeight) {
        		icon=ImageUtils.resizeIcon(icon, Utilities.sIconTextureHeight, Utilities.sIconTextureWidth);
        	}
        }
        info.setIcon(icon);
        return info;
    }

    Bitmap getIconFromCursor(Cursor c, int iconIndex, Context context) {
        @SuppressWarnings("all") // suppress dead code warning
        final boolean debug = false;
        if (debug) {
            Log.d(TAG, "getIconFromCursor app="
                    + c.getString(c.getColumnIndexOrThrow(LauncherSettings.Favorites.TITLE)));
        }
        byte[] data = c.getBlob(iconIndex);
        try {
            return Utilities.createIconBitmap(
                    BitmapFactory.decodeByteArray(data, 0, data.length), context);
        } catch (Exception e) {
            return null;
        }
    }

    ShortcutInfo addShortcut(Context context, Intent data, long container, int screen,
            int cellX, int cellY, boolean notify) {
        final ShortcutInfo info = infoFromShortcutIntent(context, data, null);
        if (info == null) {
            return null;
        }
        addItemToDatabase(context, info, container, screen, cellX, cellY, notify);

        return info;
    }

    /**
     * Attempts to find an AppWidgetProviderInfo that matches the given component.
     */
    AppWidgetProviderInfo findAppWidgetProviderInfoWithComponent(Context context,
            ComponentName component) {
        List<AppWidgetProviderInfo> widgets =
            AppWidgetManager.getInstance(context).getInstalledProviders();
        for (AppWidgetProviderInfo info : widgets) {
            if (info.provider.equals(component)) {
                return info;
            }
        }
        return null;
    }

    /**
     * Returns a list of all the widgets that can handle configuration with a particular mimeType.
     */
    List<WidgetMimeTypeHandlerData> resolveWidgetsForMimeType(Context context, String mimeType) {
        final PackageManager packageManager = context.getPackageManager();
        final List<WidgetMimeTypeHandlerData> supportedConfigurationActivities =
            new ArrayList<WidgetMimeTypeHandlerData>();

        final Intent supportsIntent =
            new Intent(InstallWidgetReceiver.ACTION_SUPPORTS_CLIPDATA_MIMETYPE);
        supportsIntent.setType(mimeType);

        // Create a set of widget configuration components that we can test against
        final List<AppWidgetProviderInfo> widgets =
            AppWidgetManager.getInstance(context).getInstalledProviders();
        final HashMap<ComponentName, AppWidgetProviderInfo> configurationComponentToWidget =
            new HashMap<ComponentName, AppWidgetProviderInfo>();
        for (AppWidgetProviderInfo info : widgets) {
            configurationComponentToWidget.put(info.configure, info);
        }

        // Run through each of the intents that can handle this type of clip data, and cross
        // reference them with the components that are actual configuration components
        final List<ResolveInfo> activities = packageManager.queryIntentActivities(supportsIntent,
                PackageManager.MATCH_DEFAULT_ONLY);
        for (ResolveInfo info : activities) {
            final ActivityInfo activityInfo = info.activityInfo;
            final ComponentName infoComponent = new ComponentName(activityInfo.packageName,
                    activityInfo.name);
            if (configurationComponentToWidget.containsKey(infoComponent)) {
                supportedConfigurationActivities.add(
                        new InstallWidgetReceiver.WidgetMimeTypeHandlerData(info,
                                configurationComponentToWidget.get(infoComponent)));
            }
        }
        return supportedConfigurationActivities;
    }

    ShortcutInfo infoFromShortcutIntent(Context context, Intent data, Bitmap fallbackIcon) {
        Intent intent = data.getParcelableExtra(Intent.EXTRA_SHORTCUT_INTENT);
        String name = data.getStringExtra(Intent.EXTRA_SHORTCUT_NAME);
        Parcelable bitmap = data.getParcelableExtra(Intent.EXTRA_SHORTCUT_ICON);

        if (intent == null) {
            // If the intent is null, we can't construct a valid ShortcutInfo, so we return null
            Log.e(TAG, "Can't construct ShorcutInfo with null intent");
            return null;
        }

        Bitmap icon = null;
        boolean customIcon = false;
        ShortcutIconResource iconResource = null;

        if (bitmap != null && bitmap instanceof Bitmap) {

            if(LqShredPreferences.isLqtheme(context)){
            		if(Launcher.isSupportClone&&data.getAppInstanceIndex()==1) {
            			ComponentName comName = new ComponentName(data.getComponent().getPackageName(),data.getComponent().getClassName()+"s");

                      	 bitmap = IconCache.getThemeIcon(comName, (Bitmap)bitmap, true, "",context);
            		}else {
                     	 bitmap = IconCache.getLqIcon(data.getComponent(), (Bitmap)bitmap, true, "");
            		}
            }
//            if(Launcher.isSupportIconSize) {
//            	
//	                icon = (Bitmap)bitmap;
//            }else {
                icon = Utilities.createIconBitmap(new FastBitmapDrawable((Bitmap)bitmap), context);
//            }
            customIcon = true;
        } else {
            Parcelable extra = data.getParcelableExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE);
            if (extra != null && extra instanceof ShortcutIconResource) {
                try {
                    iconResource = (ShortcutIconResource) extra;
                    final PackageManager packageManager = context.getPackageManager();
                    Resources resources = packageManager.getResourcesForApplication(
                            iconResource.packageName);
                    final int id = resources.getIdentifier(iconResource.resourceName, null, null);
                    
                    

					if (Launcher.isSupportIconSize) {
						icon = ImageUtils.drawableToBitmap1(mIconCache
								.getFullResIcon(resources, id));
						icon = IconCache.getLqIcon(null, (Bitmap)icon, true, "");
						if(icon!=null&&icon.getHeight()!=Utilities.sIconTextureHeight) {
							icon=ImageUtils.resizeIcon(icon, Utilities.sIconTextureHeight, Utilities.sIconTextureWidth);
						}

					} else {
						icon = Utilities.createIconBitmap(
								mIconCache.getFullResIcon(resources, id),
								context);
					}
                } catch (Exception e) {
                    Log.w(TAG, "Could not load shortcut icon: " + extra);
                }
            }
        }

        final ShortcutInfo info = new ShortcutInfo();

        if (icon == null) {
            if (fallbackIcon != null) {
                icon = fallbackIcon;
            } else {
                icon = getFallbackIcon();
                info.usingFallbackIcon = true;
            }
        }
		/*if(icon !=null) {
            if(LqShredPreferences.isLqtheme(context)){

        		if(Launcher.isSupportClone&&data.getAppInstanceIndex()==1) {
        			ComponentName comName = new ComponentName(data.getComponent().getPackageName(),data.getComponent().getClassName()+"s");

        			icon = IconCache.getLqIcon(comName, icon, true, "");
        		}else {

        			icon = IconCache.getLqIcon(data.getComponent(), icon, true, "");
        		}
            }
		}*/
        info.setIcon(icon);

        info.title = name;
        info.intent = intent;
        info.customIcon = customIcon;
//        info.iconResource = iconResource;

        return info;//add by prize_zhouerlong of modify
    }

    boolean queueIconToBeChecked(HashMap<Object, byte[]> cache, ShortcutInfo info, Cursor c,
            int iconIndex) {
        // If apps can't be on SD, don't even bother.
        if (!mAppsCanBeOnRemoveableStorage) {
            return false;
        }
        // If this icon doesn't have a custom icon, check to see
        // what's stored in the DB, and if it doesn't match what
        // we're going to show, store what we are going to show back
        // into the DB.  We do this so when we're loading, if the
        // package manager can't find an icon (for example because
        // the app is on SD) then we can use that instead.
        if (!info.customIcon && !info.usingFallbackIcon) {
            cache.put(info, c.getBlob(iconIndex));
            return true;
        }
        return false;
    }
    void updateSavedIcon(Context context, ShortcutInfo info, byte[] data) {
        boolean needSave = false;
        try {
            if (data != null) {
                Bitmap saved = BitmapFactory.decodeByteArray(data, 0, data.length);
                Bitmap loaded = info.getIcon(mIconCache);
                needSave = !saved.sameAs(loaded);
            } else {
                needSave = true;
            }
        } catch (Exception e) {
            needSave = true;
        }
        if (needSave) {
            Log.d(TAG, "going to save icon bitmap for info=" + info);
            // This is slower than is ideal, but this only happens once
            // or when the app is updated with a new icon.
            updateItemInDatabase(context, info);
        }
    }

    /**
     * Return an existing FolderInfo object if we have encountered this ID previously,
     * or make a new one.
     */
   public static FolderInfo findOrMakeFolder(HashMap<Long, FolderInfo> folders, long id) {
        // See if a placeholder was created for us already
        FolderInfo folderInfo = folders.get(id);
        if (folderInfo == null) {
            // No placeholder -- create a new instance
            folderInfo = new FolderInfo();
            folders.put(id, folderInfo);
        }
        return folderInfo;
    }

    public static final Comparator<AppInfo> getAppNameComparator() {
        final Collator collator = Collator.getInstance();
        return new Comparator<AppInfo>() {
            public final int compare(AppInfo a, AppInfo b) {
                int result = collator.compare(a.title.toString().trim(),
                        b.title.toString().trim());
                if (result == 0) {
                    result = a.componentName.compareTo(b.componentName);
                }
                return result;
            }
        };
    }
    public static final Comparator<AppInfo> APP_INSTALL_TIME_COMPARATOR
            = new Comparator<AppInfo>() {
        public final int compare(AppInfo a, AppInfo b) {
            if (a.firstInstallTime < b.firstInstallTime) return 1;
            if (a.firstInstallTime > b.firstInstallTime) return -1;
            return 0;
        }
    };
    public static final Comparator<AppWidgetProviderInfo> getWidgetNameComparator() {
        final Collator collator = Collator.getInstance();
        return new Comparator<AppWidgetProviderInfo>() {
            public final int compare(AppWidgetProviderInfo a, AppWidgetProviderInfo b) {
                return collator.compare(a.label.toString().trim(), b.label.toString().trim());
            }
        };
    }
    static ComponentName getComponentNameFromResolveInfo(ResolveInfo info) {
        if (info.activityInfo != null) {
            return new ComponentName(info.activityInfo.packageName, info.activityInfo.name);
        } else {
            return new ComponentName(info.serviceInfo.packageName, info.serviceInfo.name);
        }
    }
    public static class ShortcutNameComparator implements Comparator<ResolveInfo> {
        private Collator mCollator;
        private PackageManager mPackageManager;
        private HashMap<Object, CharSequence> mLabelCache;
        ShortcutNameComparator(PackageManager pm) {
            mPackageManager = pm;
            mLabelCache = new HashMap<Object, CharSequence>();
            mCollator = Collator.getInstance();
        }
        ShortcutNameComparator(PackageManager pm, HashMap<Object, CharSequence> labelCache) {
            mPackageManager = pm;
            mLabelCache = labelCache;
            mCollator = Collator.getInstance();
        }
        public final int compare(ResolveInfo a, ResolveInfo b) {
            CharSequence labelA, labelB;
            ComponentName keyA = LauncherModel.getComponentNameFromResolveInfo(a);
            ComponentName keyB = LauncherModel.getComponentNameFromResolveInfo(b);
            if (mLabelCache.containsKey(keyA)) {
                labelA = mLabelCache.get(keyA);
            } else {
                labelA = a.loadLabel(mPackageManager).toString().trim();

                mLabelCache.put(keyA, labelA);
            }
            if (mLabelCache.containsKey(keyB)) {
                labelB = mLabelCache.get(keyB);
            } else {
                labelB = b.loadLabel(mPackageManager).toString().trim();

                mLabelCache.put(keyB, labelB);
            }
            return mCollator.compare(labelA.toString(), labelB.toString());
        }
    };
    public static class WidgetAndShortcutNameComparator implements Comparator<Object> {
        private Collator mCollator;
        private PackageManager mPackageManager;
        private HashMap<Object, String> mLabelCache;
        WidgetAndShortcutNameComparator(PackageManager pm) {
            mPackageManager = pm;
            mLabelCache = new HashMap<Object, String>();
            mCollator = Collator.getInstance();
        }
        public final int compare(Object a, Object b) {
            String labelA, labelB;
            if (mLabelCache.containsKey(a)) {
                labelA = mLabelCache.get(a);
            } else {
                labelA = (a instanceof AppWidgetProviderInfo) ?
                    ((AppWidgetProviderInfo) a).label :
                    ((ResolveInfo) a).loadLabel(mPackageManager).toString().trim();
                mLabelCache.put(a, labelA);
            }
            if (mLabelCache.containsKey(b)) {
                labelB = mLabelCache.get(b);
            } else {
                labelB = (b instanceof AppWidgetProviderInfo) ?
                    ((AppWidgetProviderInfo) b).label :
                    ((ResolveInfo) b).loadLabel(mPackageManager).toString().trim();
                mLabelCache.put(b, labelB);
            }
            return mCollator.compare(labelA.toString(), labelB.toString());
        }
    };

    public void dumpState() {
        Log.d(TAG, "mCallbacks=" + mCallbacks);
        AppInfo.dumpApplicationInfoList(TAG, "mAllAppsList.data", mBgAllAppsList.data);
        AppInfo.dumpApplicationInfoList(TAG, "mAllAppsList.added", mBgAllAppsList.added);
        AppInfo.dumpApplicationInfoList(TAG, "mAllAppsList.removed", mBgAllAppsList.removed);
        AppInfo.dumpApplicationInfoList(TAG, "mAllAppsList.modified", mBgAllAppsList.modified);
        if (mLoaderTask != null) {
            mLoaderTask.dumpState();
        } else {
            Log.d(TAG, "mLoaderTask=null");
        }
    }

    /**
     * M: Set flush cache.
     */
    synchronized void setFlushCache() {
        LauncherLog.d(TAG, "Set flush cache flag for locale changed.");
        mForceFlushCache = true;
    }

    /**
     * M: Flush icon cache and label cache if locale has been changed.
     *
     * @param labelCache label cache.
     */
    synchronized void flushCacheIfNeeded(HashMap<Object, CharSequence> labelCache) {
        if (DEBUG_MODEL) {
            LauncherLog.d(TAG, "flushCacheIfNeeded: sForceFlushCache = " + mForceFlushCache
                    + ", mLoaderTask = " + mLoaderTask + ", labelCache = " + labelCache);
        }
        if (mForceFlushCache) {
            labelCache.clear();
            mIconCache.flush();
            mForceFlushCache = false;
        }
    }

    /**
     * M: Get all app list.
     */
    public AllAppsList getAllAppsList() {
        return mBgAllAppsList;
    }

    /// M: Add for op09 start. @{

    /**
     * M: Add an item to the database. Sets the screen, cellX and cellY fields of
     * the item. Also assigns an ID to the item, add for OP09 start.
     */
    void addAllAppsItemToDatabase(Context context, final AppInfo item, final int screen,
            final int cellX, final int cellY, final boolean notify) {
        if (DEBUG_MODEL) {
            LauncherLog.d(TAG, "addAllAppsItemToDatabase item = " + item + ", screen = " + screen
                    + ", cellX " + cellX + ", cellY = " + cellY + ", notify = " + notify);
        }

        item.cellX = cellX;
        item.cellY = cellY;
        item.screenId = screen;

        int visible = item.isVisible ? 1 : 0;

        final ContentValues values = new ContentValues();
        final ContentResolver cr = context.getContentResolver();
        item.onAddToDatabase(values);

        LauncherApplication app = (LauncherApplication) context.getApplicationContext();
        item.id = -1;//LauncherExtPlugin.getInstance().getLoadDataExt(context).generateNewIdForAllAppsList();
        values.put(AllApps._ID, item.id);
        values.put(AllApps.SCREEN, screen);
        values.put(AllApps.CELLX, cellX);
        values.put(AllApps.CELLY, cellY);
        values.put(AllApps.VISIBLE_FLAG, visible);

        Runnable r = new Runnable() {
            public void run() {
                cr.insert(notify ? AllApps.CONTENT_URI
                        : AllApps.CONTENT_URI_NO_NOTIFICATION, values);
            }
        };
        runOnWorkerThread(r);
    }

    /**
     * M: Update an item to the database. Sets the screen, cellX and cellY fields of
     * the item. Also assigns an ID to the item, add for OP09 start.
     */
    void updateAllAppsItemInDatabaseHelper(Context context, final ContentValues values,
            final AppInfo item, final String callingFunction) {
        final long itemId = item.id;
        final Uri uri = AllApps.getContentUri(itemId, false);
        final ContentResolver cr = context.getContentResolver();
        if (DEBUG_MODEL) {
            LauncherLog.d(TAG, "updateAllAppsItemInDatabaseHelper: values = " + values
                    + ", item = " + item + ",itemId = " + itemId + ", uri = " + uri.toString());
        }

        Runnable r = new Runnable() {
            public void run() {
                if (DEBUG_MODEL) {
                    LauncherLog.d(TAG, "updateAllAppsItemInDatabaseHelper in run: values = " + values + ", item = " + item
                            + ", uri = " + uri.toString());
                }
                cr.update(uri, values, null, null);
            }
        };
        runOnWorkerThread(r);
    }

    /**
     * M: Move an item in the DB to a new <screen, cellX, cellY>
     */
    void moveAllAppsItemInDatabase(Context context, final AppInfo item, final int screen,
            final int cellX, final int cellY) {
        if (DEBUG_MODEL) {
            LauncherLog.d(TAG, "moveAllAppsItemInDatabase item = " + item + ", screen = " + screen
                    + ", cellX = " + cellX + ", cellY = " + cellY);
        }

        item.cellX = cellX;
        item.cellY = cellY;
        item.screenId = screen;

        int visible = item.isVisible ? 1 : 0;

        final ContentValues values = new ContentValues();
        values.put(AllApps.CELLX, item.cellX);
        values.put(AllApps.CELLY, item.cellY);
        values.put(AllApps.SCREEN, (int) item.screenId);
        values.put(AllApps.VISIBLE_FLAG, visible);

        updateAllAppsItemInDatabaseHelper(context, values, item, "moveAllAppsItemInDatabase");
    }

    /**
     * M:Removes the specified item from the database
     *
     * @param context
     * @param item
     */
    void deleteAllAppsItemFromDatabase(Context context, final AppInfo item) {
        if (DEBUG_MODEL) {
            LauncherLog.d(TAG, "deleteAllAppsItemFromDatabase: item = " + item);
        }

        final ContentResolver cr = context.getContentResolver();
        final Uri uriToDelete = AllApps.getContentUri(item.id, false);
        Runnable r = new Runnable() {
            public void run() {
                cr.delete(uriToDelete, null, null);
                if (DEBUG_MODEL) {
                    LauncherLog.d(TAG, "deleteAllAppsItemFromDatabase remove id : " + item.id);
                }
            }
        };
        runOnWorkerThread(r);
    }

    /**
     * M: Get application info, add for OP09.
     *
     * @param manager
     * @param intent
     * @param context
     * @param c
     * @param titleIndex
     * @return
     */
    public AppInfo getApplicationInfo(PackageManager manager, Intent intent, Context context,
            Cursor c, int titleIndex) {
        final AppInfo info = new AppInfo();

        ComponentName componentName = intent.getComponent();
        if (componentName == null) {
            return null;
        }

        try {
            final String packageName = componentName.getPackageName();
            PackageInfo pi = manager.getPackageInfo(packageName, 0);
            if (!pi.applicationInfo.enabled) {
                // If we return null here, the corresponding item will be removed from the launcher
                // db and will not appear in the workspace.
                return null;
            }

            final int appFlags = manager.getApplicationInfo(packageName, 0).flags;
            if ((appFlags & android.content.pm.ApplicationInfo.FLAG_SYSTEM) == 0) {
                info.flags |= AppInfo.DOWNLOADED_FLAG;

                if ((appFlags & android.content.pm.ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) {
                    info.flags |= AppInfo.UPDATED_SYSTEM_APP_FLAG;
                }
            }
            info.firstInstallTime = manager.getPackageInfo(packageName, 0).firstInstallTime;
        } catch (NameNotFoundException e) {
            Log.d(TAG, "getPackInfo failed for componentName " + componentName);
            return null;
        }

        // from the db
        if (info.title == null) {
            if (c != null) {
                info.title =  c.getString(titleIndex);
            }
        }
        // fall back to the class name of the activity
        if (info.title == null) {
            info.title = componentName.getClassName();
        }
        info.componentName = componentName;
        info.itemType = LauncherSettings.Favorites.ITEM_TYPE_APPLICATION;
        return info;
    }
    
    /**
     * M: restore the application info to data list in all apps list.
     *
     * @param info
     */
    void restoreAppInAllAppsList(final AppInfo info) {
        mBgAllAppsList.add(info);
    }
    

	
	public Bitmap getShortcutCustomIcon(ShortcutInfo item){
		final long itemId = item.id;
        final Uri contentUri = LauncherSettings.Favorites.getContentUri(itemId, false);
        final ContentResolver contentResolver = mApp.getContext().getContentResolver();
        final Cursor c = contentResolver.query(contentUri, null, null, null, null);
        
        final int iconIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.ICON);
        Bitmap bp=null;
        try {
        	 if(c!=null&&c.moveToNext()){
        		 bp=getIconFromCursor(c, iconIndex, mApp.getContext());
             }
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			if(c!=null){
				c.close();
			}
		}
       
        return bp;
    }

    /// M: Add for op09 end. }@
}
