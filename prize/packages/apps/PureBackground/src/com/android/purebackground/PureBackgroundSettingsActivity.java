/************************************************************************
 * Copyright (C) 2016, Shenzhen Prize co., LTD
 * Name:     PureBackgroundSettingsActivity.java
 * Function：Pure background main activity
 * Version:  1.0
 * Author：  wangxianzhen
 * Data:     2016-04-11
 ************************************************************************/
package com.android.purebackground;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.android.purebackground.ui.GroupListAdapter;
import com.android.purebackground.ui.CustomComparator;
import com.android.purebackground.ui.PureBackgroundListAdapter;
import com.android.purebackground.ui.GroupListAdapter.ListenerEx;
import com.android.purebackground.unit.PackagesInfo;
import com.android.purebackground.util.PureBackgroundUtils;
import com.android.purebackground.util.StateBarUtils;
import com.android.purebackground.R;

import android.app.ActionBar;
import android.app.Activity;
import android.app.StatusBarManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.content.pm.LauncherApps;
import android.content.pm.LauncherActivityInfo;
import android.support.v4.widget.NestedScrollView;
import android.widget.RelativeLayout;
/*-prize add by lihuangyuan,for whitelist -2017-05-06-start-*/
import android.os.WhiteListManager;
import android.provider.WhiteListColumns;
/*-prize add by lihuangyuan,for whitelist -2017-05-06-end-*/
public class PureBackgroundSettingsActivity extends Activity implements
		OnCheckedChangeListener, ListenerEx, OnClickListener {
	private static final String TAG = "PureBackground";
	// Handle Type
	public static final int REFRESH_LIST_VIEW = 0;

	// Query Filter AppInfo Type
	public static final int FILTER_ALL_APP = 0;
	public static final int FILTER_SYSTEM_APP = 1;
	public static final int FILTER_THIRD_APP = 2;
	public static final int FILTER_SDCARD_APP = 3;

	// ListMode Type
	public static final int LIST_MODE_APP_DISABLE_RUNNING = 1; // prohibit the background
	public static final int LIST_MODE_APP_ENALBE_RUNNING = 0; // allow the background
	public static final int LIST_MODE_ALL_APP = 2;

	// Data storage
	public static final String PURE_BG_ACTION_BAR_SWITCH_VALUE = "pureBgActionBarSwitchValue";
	public static final String PURE_BG_DISABLE_APP_LIST = "pureBgDisableAppList";
	public static final String PURE_BG_ENABLE_APP_LIST = "pureBgEnableAppList";
	public static final String PURE_BG_STATUS_OPEN_VALUE = "pureBgStatusOpenValue";

	// Application list group head string resources ID
	public final static int[] GroupListTitleIds = new int[] {
			R.string.enable_running, R.string.disable_running};

	// Loading allows the background information application list
	public static List<PackagesInfo> mAppEnableRunningList = new ArrayList<PackagesInfo>();
	// Loading list application background information is prohibited
	public static List<PackagesInfo> mAppDisableRunningList = new ArrayList<PackagesInfo>();
	// Loading system all installed three application list information
	private List<PackagesInfo> mAppTempAllList = new ArrayList<PackagesInfo>();
    //Prohibit third-party applications market background regardless of whether the download.
    //And don't allow in pure background list configuration.
    private String[] mThirdAppMarketList = null;
	private ListView mListView;
	private PureBackgroundListAdapter mListViewAdapter;
	private GroupListAdapter mAppEnalbeListAdapter;
	private GroupListAdapter mAppDisalbeListAdapter;

	private TextView mEmpty;
	private View mLayoutContainerLoading;
	private View mHeadView;
	private Switch mActionBarSwitch;
	private PackageManager mPackageManager;
	private CustomComparator mCustomComparator;
	private Switch prize_pure_background;
	private ImageButton prize_return;
	private TextView prize_status_checked;

	public static boolean mActionBarSwitchIsChecked;
	public static boolean mPureBgIsOpen;// get the status of checked
	private boolean isInitFinished = false; // Whether the initialization has
											// been completed
	private HomeKeyClick homeKeyReceiver;
	private LauncherApps mLauncherApps;
	private LinearLayout closeLinearLayout;
	private NestedScrollView openLinearLayout;
	/*-prize add by lihuangyuan,for whitelist -2017-05-06-start-*/
	WhiteListManager whiteListMgr;
	/*-prize add by lihuangyuan,for whitelist -2017-05-06-end-*/
	/**
	 * Listening to the third application install and uninstall
	 */
	private BroadcastReceiver mInstalledReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent == null) {
				return;
			}

			String action = intent.getAction();
			String packageName = intent.getDataString().replace("package:", "");
			if (action.equals(PureBackgroundUtils.INTENT_ACTION_PACKAGE_ADDED)) {
				// install
				Log.i(TAG, "PureBackgroundSettingsActivity onReceive-->install:" + packageName);
				String appName = PureBackgroundUtils.getApplicationName(
						PureBackgroundSettingsActivity.this, packageName);
				PackagesInfo pkInfo = new PackagesInfo();
				pkInfo.setAppName(appName);
				pkInfo.setPackageName(packageName);
				try {
					pkInfo.setAppIcon(mPackageManager
							.getApplicationIcon(packageName));
				} catch (NameNotFoundException e) {
					e.printStackTrace();
				}

                              /*prize add by lihuangyuan,for whitelist -2017-05-06-start*/
		                //if(PureBackgroundUtils.checkAllowBkDefault(packageName))
		                String [] defList = whiteListMgr.getPurebackgroundDefList();
		                if(PureBackgroundUtils.isInList(packageName,defList))
				  {
				  /*prize add by lihuangyuan,for whitelist -2017-05-06-end*/
				    Log.i(TAG, "PureBackgroundSettingsActivity install " + packageName + " default enable");
				    if (mAppEnableRunningList == null) {
					    mAppEnableRunningList = new ArrayList<PackagesInfo>();
				    }
				    mAppEnableRunningList.add(pkInfo);
				    Collections.sort(mAppEnableRunningList, mCustomComparator);
		                }
				  else
				  {
				    Log.i(TAG, "PureBackgroundSettingsActivity install " + packageName + " default disable");
				    if (mAppDisableRunningList == null) {
					    mAppDisableRunningList = new ArrayList<PackagesInfo>();
				    }
				    mAppDisableRunningList.add(pkInfo);
				    Collections.sort(mAppDisableRunningList, mCustomComparator);
                }
				setListDataForListMode(LIST_MODE_ALL_APP);
				refreshListViewSections();
			} else if (action.equals(PureBackgroundUtils.INTENT_ACTION_PACKAGE_REMOVED)) {
				// unInstall
				Log.i(TAG, "PureBackgroundSettingsActivity onReceive-->unInstall:" + packageName);
				boolean isRemoved = false;
				/*prize add by lihuangyuan,for whitelist -2017-05-06-start*/
				for (int i=0;i<mAppDisableRunningList.size();i++) 
				{
					PackagesInfo mApp = mAppDisableRunningList.get(i);
					if (mApp.getPackageName().equals(packageName)) 
					{
						isRemoved = true;
						mAppDisableRunningList.remove(i);
						break;
					}
				}
				for (int i=0;i<mAppEnableRunningList.size();i++) 
				{
					PackagesInfo mApp = mAppEnableRunningList.get(i);
					if (mApp.getPackageName().equals(packageName)) 
					{
						isRemoved = true;
						mAppEnableRunningList.remove(i);
						break;
					}
				}
				/*prize add by lihuangyuan,for whitelist -2017-05-06-end*/
				setListDataForListMode(LIST_MODE_ALL_APP);
				refreshListViewSections();
				//mTempList = null;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		StateBarUtils.initSateBar(this);
		setContentView(R.layout.purebackgroud_activity);
		RelativeLayout mRelativeLayout = (RelativeLayout)findViewById(R.id.parent);
		View lines = findViewById(R.id.header);
		mRelativeLayout.bringChildToFront(lines);
		/*-prize add by lihuangyuan,for whitelist -2017-05-06-start-*/
		whiteListMgr = (WhiteListManager)getSystemService(Context.WHITELIST_SERVICE);
		/*-prize add by lihuangyuan,for whitelist -2017-05-06-end-*/
		WindowManager.LayoutParams lp= getWindow().getAttributes();
		lp.statusBarInverse = StatusBarManager.STATUS_BAR_INVERSE_GRAY;
		getWindow().setAttributes(lp);
		mLauncherApps = (LauncherApps) getSystemService(Context.LAUNCHER_APPS_SERVICE);
		prize_pure_background = (Switch) findViewById(R.id.prize_pure_background);
		prize_return = (ImageButton) findViewById(R.id.prize_return);
		prize_status_checked = (TextView) findViewById(R.id.prize_status_checked);

		prize_return.setOnClickListener(this);
		mActionBarSwitchIsChecked = PureBackgroundUtils
				.isActionBarSwitchChecked(this);
		mPureBgIsOpen = PureBackgroundUtils.isOpenPureBackgroud(this);
		mCustomComparator = new CustomComparator();
		mPackageManager = getPackageManager();
		prize_pure_background.setOnCheckedChangeListener(this);

		closeLinearLayout = (LinearLayout)findViewById(R.id.close_purebackground);
		openLinearLayout = (NestedScrollView)findViewById(R.id.open_purebackground);
		registerInstalledReceiver();
		homeKeyReceiver = new HomeKeyClick();
		registerReceiver(homeKeyReceiver, new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
		initializeAdapter();
		initListView();

		startQueryFilterAppInfo();
        Log.i(TAG, "onCreate mPureBgIsOpen = " + mPureBgIsOpen);
        if (mPureBgIsOpen) {
            prize_status_checked.setText(getResources().getString(R.string.text_click_description_close_information));
            closeLinearLayout.setVisibility(View.GONE);
            openLinearLayout.setVisibility(View.VISIBLE);
        } else {
            prize_status_checked.setText(getResources().getString(R.string.text_click_description_open_information));
            closeLinearLayout.setVisibility(View.VISIBLE);
            openLinearLayout.setVisibility(View.GONE);
        }

        mThirdAppMarketList = getResources().getStringArray(com.prize.internal.R.array.third_app_market);
	}

	@Override
	protected void onResume() {
		super.onResume();
		prize_pure_background.setChecked(mPureBgIsOpen);
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if (buttonView.getId() == R.id.prize_pure_background) {
			Log.i(TAG, "onCheckedChanged:->isChecked = " + isChecked);
			mPureBgIsOpen = isChecked;
			if (mAppDisableRunningList.size() == 0
					&& mAppEnableRunningList.size() == 0) {
				return;
			}
			if (isChecked) {
				prize_status_checked.setText(getResources().getString(
						R.string.text_click_description_close_information));
				closeLinearLayout.setVisibility(View.GONE);
				openLinearLayout.setVisibility(View.VISIBLE);
			} else {
				prize_status_checked.setText(getResources().getString(
						R.string.text_click_description_open_information));
				closeLinearLayout.setVisibility(View.VISIBLE);
				openLinearLayout.setVisibility(View.GONE);

			}
			setListDataForListMode(LIST_MODE_ALL_APP);
			refreshListViewSections();
		}
	}

	/**
	 * get checkbox status value
	 * 
	 * @return
	 */
	private int getCheckStatusValue() {
		if (prize_pure_background.isChecked()) {
			return 1;
		} else {
			return 0;
		}
	}

	/**
	 * init ListViewAdapter
	 */
	private void initializeAdapter() {
		mListViewAdapter = new PureBackgroundListAdapter(this);

		int length = GroupListTitleIds.length;
		for (int index = 0; index < length; index++) {
			setListAdapterForListMode(index);
		}
	}

	/**
	 * init ListView
	 */
	private void initListView() {
		mEmpty = (TextView) findViewById(R.id.empty);
		mLayoutContainerLoading = (View) findViewById(R.id.loading_container);
		mLayoutContainerLoading.setVisibility(View.VISIBLE);
		mListView = (ListView) findViewById(R.id.list_view);
		mListView.setAdapter(mListViewAdapter);
		mListView.setNestedScrollingEnabled(false);
		mListView.setFocusable(false);
		refreshListViewSections();
	}

	/**
	 * According to the data packet set different adapters
	 * 
	 * @param mode
	 */
	private void setListAdapterForListMode(int lListMode) {
		switch (lListMode) {
		case LIST_MODE_APP_DISABLE_RUNNING:
			mAppDisalbeListAdapter = new GroupListAdapter(this,
					LIST_MODE_APP_DISABLE_RUNNING);
			mAppDisalbeListAdapter.setListener(this);
			mListViewAdapter.addSection(
					getResources().getString(GroupListTitleIds[lListMode]),
					mAppDisalbeListAdapter);
			setListDataForListMode(LIST_MODE_APP_DISABLE_RUNNING);
			break;
		case LIST_MODE_APP_ENALBE_RUNNING:
			mAppEnalbeListAdapter = new GroupListAdapter(this,
					LIST_MODE_APP_ENALBE_RUNNING);
			mAppEnalbeListAdapter.setListener(this);
			mListViewAdapter.addSection(
					getResources().getString(GroupListTitleIds[lListMode]),
					mAppEnalbeListAdapter);
			setListDataForListMode(LIST_MODE_APP_ENALBE_RUNNING);
			break;
		default:
			break;
		}
	}

	/**
	 * According to the grouping List set different data
	 * 
	 * @param modeIndex
	 */
	private void setListDataForListMode(int modeIndex) {
		switch (modeIndex) {
		case LIST_MODE_APP_DISABLE_RUNNING:
			mAppDisalbeListAdapter.setListData(mAppDisableRunningList);
			break;
		case LIST_MODE_APP_ENALBE_RUNNING:
			mAppEnalbeListAdapter.setListData(mAppEnableRunningList);
			break;
		case LIST_MODE_ALL_APP:
			mAppEnalbeListAdapter.setListData(mAppEnableRunningList);
			mAppDisalbeListAdapter.setListData(mAppDisableRunningList);
			break;
		default:
			break;
		}
	}

	/**
	 * refresh list
	 */
	private void refreshListViewSections() {
		if (mAppEnableRunningList.size() == 0
				&& mAppDisableRunningList.size() == 0) {
			mListViewAdapter.removeSections(LIST_MODE_APP_DISABLE_RUNNING);
			mListViewAdapter.removeSections(LIST_MODE_APP_ENALBE_RUNNING);
			if (isInitFinished) {
				mEmpty.setVisibility(View.VISIBLE);
			}
			prize_pure_background.setChecked(false);
			prize_pure_background.setEnabled(false);
		} else {
			mEmpty.setVisibility(View.GONE);
			prize_pure_background.setEnabled(true);
			if (mAppEnableRunningList.size() == 0
					&& mAppDisableRunningList.size() > 0) {
				mListViewAdapter.removeSections(LIST_MODE_APP_ENALBE_RUNNING);
				mListViewAdapter.addSections(LIST_MODE_APP_DISABLE_RUNNING,
						mAppDisalbeListAdapter);
			} else if (mAppDisableRunningList.size() == 0
					&& mAppEnableRunningList.size() > 0) {
				mListViewAdapter.removeSections(LIST_MODE_APP_DISABLE_RUNNING);
				mListViewAdapter.addSections(LIST_MODE_APP_ENALBE_RUNNING,
						mAppEnalbeListAdapter);
			} else {
				mListViewAdapter.addSections(LIST_MODE_APP_DISABLE_RUNNING,
						mAppDisalbeListAdapter);
				mListViewAdapter.addSections(LIST_MODE_APP_ENALBE_RUNNING,
						mAppEnalbeListAdapter);
			}
		}
		mListViewAdapter.notifyDataSetChanged();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * Open the thread information query three applications
	 * 
	 * @param queryType
	 */
	private void startQueryFilterAppInfo() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				isInitFinished = false;
				queryFilterAppInfo(FILTER_THIRD_APP);

				Message message = new Message();
				message.what = REFRESH_LIST_VIEW;
				mHandler.sendMessageDelayed(message, 300);
			}
		}).start();
	}

	/**
	 * According to the query conditions, query specific ApplicationInfo
	 * 
	 * @param filter
	 * @return
	 */
	public void queryFilterAppInfo(int filter) {

		// clear data
		mAppTempAllList.clear();
		// Check all installed applications
		/*-prize add by lihuangyuan,for whitelist -2017-05-06-start-*/		 
		 String [] hideList = whiteListMgr.getPurebackgroundHideList();
		 /*-prize add by lihuangyuan,for whitelist -2017-05-06-end-*/
        final List<LauncherActivityInfo> lais= mLauncherApps.getActivityList(null,UserHandle.getCallingUserHandle());
            for (LauncherActivityInfo lai : lais) {
                ApplicationInfo app = lai.getApplicationInfo();
                String packageName = app.loadLabel(mPackageManager).toString();
		  /*prize add by lihuangyuan,for whitelist -2017-05-06-start*/
                /*if((app != null) && (PureBackgroundUtils.hideAppIcon(app.packageName)))*/
		  if((app != null) && PureBackgroundUtils.isInList(app.packageName,hideList))
		  {
		  /*prize add by lihuangyuan,for whitelist -2017-05-06-end*/
                    Log.i(TAG, "hide packageName="  + packageName + ", app.packageName=" + app.packageName);
                    continue;
                }
                PackagesInfo pkInfo = new PackagesInfo();
                pkInfo.setAppIcon(app.loadIcon(mPackageManager));
                pkInfo.setAppName(packageName);
                pkInfo.setPackageName(app.packageName);
                Log.i(TAG, "add packageName="  + packageName + ", app.packageName=" + app.packageName);
                mAppTempAllList.add(pkInfo);
        }

        List<ApplicationInfo> listAppcations = mPackageManager.getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES);
        for (ApplicationInfo app : listAppcations) {
            if ((app.prizeFlags & ApplicationInfo.FLAG_IS_PREBUILT_THIRD_APPS) != 0) {
                String packageName = app.loadLabel(mPackageManager).toString();
                Log.i(TAG, "packageName=" + packageName + ", app.packageName=" + app.packageName);
                if(app.packageName.equals("com.ekesoo.font")) {
                    Log.i(TAG, "add packageName=" + packageName + ", app.packageName=" + app.packageName);
                    PackagesInfo pkInfo = new PackagesInfo();
                    pkInfo.setAppIcon(app.loadIcon(mPackageManager));
                    pkInfo.setAppName(packageName);
                    pkInfo.setPackageName(app.packageName);
                    mAppTempAllList.add(pkInfo);
                }
            }
        }
		// sort
		Collections.sort(mAppTempAllList, mCustomComparator);
	}

    Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case REFRESH_LIST_VIEW:
                // clear data
                mAppEnableRunningList.clear();
                mAppDisableRunningList.clear();
                /*-prize add by lihuangyuan,for whitelist -2017-05-06-start-*/
		  String[] enablelist = whiteListMgr.getPurbackgroundEnableList();
		  String[] disablelist = whiteListMgr.getPuregackgroundDisableList();
		  //if(enablelist != null)
		  {
		  	for (PackagesInfo mApp : mAppTempAllList) 
			{
                        if (PureBackgroundUtils.isInList(mApp.getPackageName(), disablelist)) 
			   {
                            mAppDisableRunningList.add(mApp);
                            Log.i(TAG, "mHandler disable = " + mApp.getPackageName());
                        }
			   else if(PureBackgroundUtils.isInList(mApp.getPackageName(), enablelist)) 
			   {
                            mAppEnableRunningList.add(mApp);
                            Log.i(TAG, "mHandler enable = " + mApp.getPackageName());
                        }
			    else
			    {
			        mAppDisableRunningList.add(mApp);
				 PureBackgroundUtils.insertToOrUpdateEnableDb(PureBackgroundSettingsActivity.this, mApp.getPackageName(), false);
                            Log.i(TAG, "mHandler insert record disable = " + mApp.getPackageName());
			    }
                    }
		  }
		  /*-prize add by lihuangyuan,for whitelist -2017-05-06-end-*/

                    isInitFinished = true;
                    mLayoutContainerLoading.setVisibility(View.GONE);
                    setListDataForListMode(LIST_MODE_ALL_APP);
                    refreshListViewSections();
                //}
                break;
            }
            super.handleMessage(msg);
        }
    };

	private void registerInstalledReceiver() {
		final IntentFilter filter = new IntentFilter();
		filter.addAction(PureBackgroundUtils.INTENT_ACTION_PACKAGE_ADDED);
		filter.addAction(PureBackgroundUtils.INTENT_ACTION_PACKAGE_REMOVED);
		filter.addDataScheme(PureBackgroundUtils.INTENT_DATA_SCHEME);
		registerReceiver(mInstalledReceiver, filter);
	}

	private void unregisterInstalledReceiver() {
		unregisterReceiver(mInstalledReceiver);
	}

	@Override
	protected void onPause() {
		super.onPause();
		if(isInitFinished) pureBKgroundDataStorage();
	}

	/**
	 * backup data when exit
	 */
	private void pureBKgroundDataStorage() {
		// Backup pure background master switch value
		Settings.System.putLong(getContentResolver(),
				PURE_BG_STATUS_OPEN_VALUE, getCheckStatusValue());
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterInstalledReceiver();
		unregisterReceiver(homeKeyReceiver);
	}

	/**
	 * According to the packet switch is triggered after regrouping, refresh the
	 * list
	 */
	@Override
	public synchronized void onRegrouping(int lListMode, int lPos) {
		Log.i(TAG, "onRegrouping:->lListMode = " + lListMode + " lPos = "
				+ lPos);
		PackagesInfo pkInfo = null;
		switch (lListMode) {
		case LIST_MODE_APP_DISABLE_RUNNING:
			// Removed from the list of banned the background
			Log.i(TAG,
					"Debug-->>onRegrouping:LIST_MODE_APP_DISABLE_RUNNING->lPos = "
							+ lPos + " size = " + mAppDisableRunningList.size());
			pkInfo = mAppDisableRunningList.get(lPos);
			/*prize add by lihuangyuan,for whitelist -2017-05-06-start*/
			mAppDisableRunningList.remove(lPos);
			mAppEnableRunningList.add(pkInfo);
			PureBackgroundUtils.updateToDb(this, pkInfo.getPackageName(), false,true);
			/*prize add by lihuangyuan,for whitelist -2017-05-06-end*/
			// sort
			Collections.sort(mAppEnableRunningList, mCustomComparator);
			break;
		case LIST_MODE_APP_ENALBE_RUNNING:
			Log.i(TAG,
					"Debug-->>onRegrouping:LIST_MODE_APP_ENALBE_RUNNING->lPos = "
							+ lPos + " size = " + mAppDisableRunningList.size());
			// Remove from allowing the background to the list
			pkInfo = mAppEnableRunningList.get(lPos);
			pkInfo = mAppEnableRunningList.get(lPos);
			/*prize add by lihuangyuan,for whitelist -2017-05-06-start*/
			mAppEnableRunningList.remove(lPos);
			mAppDisableRunningList.add(pkInfo);
			PureBackgroundUtils.updateToDb(this, pkInfo.getPackageName(), true,false);
			/*prize add by lihuangyuan,for whitelist -2017-05-06-end*/
			// sort
			Collections.sort(mAppDisableRunningList, mCustomComparator);
			break;
		default:
			break;
		}
		setListDataForListMode(LIST_MODE_ALL_APP);
		refreshListViewSections();
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.prize_return:
			PureBackgroundSettingsActivity.this.finish();
			break;
		}
	}

    class HomeKeyClick extends BroadcastReceiver {
        static final String SYSTEM_REASON = "reason";
        static final String SYSTEM_HOME_KEY = "homekey";// home key
        static final String SYSTEM_RECENT_APPS = "recentapps";// long home key

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.i(TAG, "HomeKeyClick onReceive " + intent);
            /*if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
                String reason = intent.getStringExtra(SYSTEM_REASON);
                Log.i(TAG, "HomeKeyClick reason " + reason);
                if (reason != null) {
                    if (reason.equals(SYSTEM_HOME_KEY)) {
                        PureBackgroundSettingsActivity.this.finish();
                    }
                }
            }*/
        }
    }
}
