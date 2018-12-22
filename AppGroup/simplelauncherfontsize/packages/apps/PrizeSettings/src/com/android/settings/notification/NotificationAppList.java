/*
 * Copyright (C) 2014 The Android Open Source Project
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

package com.android.settings.notification;

import static com.android.settings.notification.AppNotificationSettings.EXTRA_HAS_SETTINGS_INTENT;
import static com.android.settings.notification.AppNotificationSettings.EXTRA_SETTINGS_INTENT;
import android.animation.LayoutTransition;
import android.app.INotificationManager;
import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.LauncherActivityInfo;
import android.content.pm.LauncherApps;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.Signature;
import android.content.pm.PackageInfo;//add by liup
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings;
import android.service.notification.NotificationListenerService;
import android.util.ArrayMap;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.CheckBox;

import com.android.settings.PinnedHeaderListFragment;
import com.android.settings.R;
import com.android.settings.Settings.NotificationAppListActivity;
import com.android.settings.UserSpinnerAdapter;
import com.android.settings.Utils;
import com.mediatek.common.prizeoption.PrizeOption;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.os.Message;//huangdianjun-20151103

/**Prize-notification-huangdianjun-2015.4.28-begin*/
import android.widget.Switch;
/**Prize-notification-huangdianjun-2015.4.28-end*/

import android.os.SystemProperties;

/** Just a sectioned list of installed applications, nothing else to index **/
public class NotificationAppList extends PinnedHeaderListFragment
        implements OnItemSelectedListener{
    private static final String TAG = "NotificationAppList";
    private static final boolean DEBUG = Log.isLoggable(TAG, Log.DEBUG);

    private static final String EMPTY_SUBTITLE = "";
    private static final String SECTION_BEFORE_A = "*";
    private static final String SECTION_AFTER_Z = "**";
    private static final Intent APP_NOTIFICATION_PREFS_CATEGORY_INTENT
            = new Intent(Intent.ACTION_MAIN)
                .addCategory(Notification.INTENT_CATEGORY_NOTIFICATION_PREFERENCES);

    private final Handler mHandler = new Handler();
    private final ArrayMap<String, AppRow> mRows = new ArrayMap<String, AppRow>();
    private final ArrayList<AppRow> mSortedRows = new ArrayList<AppRow>();
    private final ArrayList<String> mSections = new ArrayList<String>();

    
    private LayoutInflater mInflater;
    private NotificationAppAdapter mAdapter;
    private Signature[] mSystemSignature;
    private Parcelable mListViewState;
    private Backend mBackend = new Backend();
    private UserSpinnerAdapter mProfileSpinnerAdapter;
    private Spinner mSpinner;

    private PackageManager mPM;
    private UserManager mUM;
    private LauncherApps mLauncherApps;
    //prize-add-huangdianjun-20151102-start
	private TextView countNotAppTv;
	private static Context mContext;
	private static int countNotApp;
	private static final int OVER = 1;
	private Handler handler;
    //prize-add-huangdianjun-20151102-end
	


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();//add-by-huangdianjun-20151102
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mAdapter = new NotificationAppAdapter(mContext);
        mUM = UserManager.get(mContext);
        mPM = mContext.getPackageManager();
        mLauncherApps = (LauncherApps) mContext.getSystemService(Context.LAUNCHER_APPS_SERVICE);
        getActivity().setTitle(R.string.app_notifications_title);
        //prize-huangdianjun-20151102-start
        if(handler == null){
        	handler = new Handler() {
        		
        		
        		public void handleMessage(Message msg) {
        			switch (msg.what) {
        			case OVER:
        				countNotAppTv.setVisibility(View.VISIBLE);
        				setHeaderCount(countNotApp);
        				break;
        			default:
        				break;
        			}
        			super.handleMessage(msg);
        		}
        	};
        }
        //prize-huangdianjun-20151102-end
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
    	//prize-huangdianjun-add-20151102-start
    	View view = inflater.inflate(R.layout.notification_app_list, container, false);
    	countNotAppTv = (TextView)view.findViewById(R.id.notification_count_txt);
        return view;
        //prize-huangdianjun-add-20151102-end
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mProfileSpinnerAdapter = Utils.createUserSpinnerAdapter(mUM, mContext);
        if (mProfileSpinnerAdapter != null) {
            mSpinner = (Spinner) getActivity().getLayoutInflater().inflate(
                    R.layout.spinner_view, null);
            mSpinner.setAdapter(mProfileSpinnerAdapter);
            mSpinner.setOnItemSelectedListener(this);
            setPinnedHeaderView(mSpinner);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        repositionScrollbar();
        getListView().setAdapter(mAdapter);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (DEBUG) Log.d(TAG, "Saving listView state");
        mListViewState = getListView().onSaveInstanceState();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mListViewState = null;  // you're dead to me
    }

    @Override
    public void onResume() {
        super.onResume();
        loadAppsList();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        UserHandle selectedUser = mProfileSpinnerAdapter.getUserHandle(position);
        if (selectedUser.getIdentifier() != UserHandle.myUserId()) {
            Intent intent = new Intent(getActivity(), NotificationAppListActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            mContext.startActivityAsUser(intent, selectedUser);
            // Go back to default selection, which is the first one; this makes sure that pressing
            // the back button takes you into a consistent state
            mSpinner.setSelection(0);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    public void setBackend(Backend backend) {
        mBackend = backend;
    }

    private void loadAppsList() {
        AsyncTask.execute(mCollectAppsRunnable);
    }

    private String getSection(CharSequence label) {
        if (label == null || label.length() == 0) return SECTION_BEFORE_A;
        final char c = Character.toUpperCase(label.charAt(0));
        if (c < 'A') return SECTION_BEFORE_A;
        if (c > 'Z') return SECTION_AFTER_Z;
        return Character.toString(c);
    }

    private void repositionScrollbar() {
        final int sbWidthPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                getListView().getScrollBarSize(),
                getResources().getDisplayMetrics());
        final View parent = (View)getView().getParent();
        final int eat = Math.min(sbWidthPx, parent.getPaddingEnd());
        if (eat <= 0) return;
        if (DEBUG) Log.d(TAG, String.format("Eating %dpx into %dpx padding for %dpx scroll, ld=%d",
                eat, parent.getPaddingEnd(), sbWidthPx, getListView().getLayoutDirection()));
        parent.setPaddingRelative(parent.getPaddingStart(), parent.getPaddingTop(),
                parent.getPaddingEnd() - eat, parent.getPaddingBottom());
    }

    private static class ViewHolder {
        ViewGroup row;
        ImageView icon;
        TextView title;
        TextView subtitle;
        /**Prize-notification-huangdianjun-2015.5.28-begin*/
        TextView summary;
        //View rowDivider;
    		Switch notiSwitch;
    		/**Prize-notification-huangdianjun-2015.5.28-end*/
    }

    private class NotificationAppAdapter extends ArrayAdapter<Row> implements SectionIndexer {
        public NotificationAppAdapter(Context context) {
            super(context, 0, 0);
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }

        @Override
        public int getItemViewType(int position) {
            Row r = getItem(position);
            return r instanceof AppRow ? 1 : 0;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            Row r = getItem(position);
            View v;
            if (convertView == null) {
                v = newView(parent, r);
            } else {
                v = convertView;
            }
            bindView(v, r, false /*animate*/);
            return v;
        }

        public View newView(ViewGroup parent, Row r) {
            if (!(r instanceof AppRow)) {
                return mInflater.inflate(R.layout.notification_app_section, parent, false);
            }
            final View v = mInflater.inflate(R.layout.notification_app, parent, false);
            final ViewHolder vh = new ViewHolder();
            vh.row = (ViewGroup) v;
            vh.row.setLayoutTransition(new LayoutTransition());
            vh.row.setLayoutTransition(new LayoutTransition());
            vh.icon = (ImageView) v.findViewById(android.R.id.icon);
            vh.title = (TextView) v.findViewById(android.R.id.title);
            vh.subtitle = (TextView) v.findViewById(android.R.id.text1);
            /**Prize-notification-huangdianjun-2015.4.28-begin*/
            vh.summary = (TextView)v.findViewById(R.id.summary);
			
			//add for simpleLuancher by liup 20160112 start
			if(SystemProperties.get("persist.sys.simpleLuancher").equals("1")){
				vh.summary.setVisibility(View.GONE);
			}
			//add for simpleLuancher by liup 20160112 end
			
            //vh.rowDivider = v.findViewById(R.id.row_divider);
            if(!PrizeOption.PRIZE_SETTINGS_NOTIFICATION){
            		vh.notiSwitch = (Switch)v.findViewById(R.id.noti_switch);
            		vh.notiSwitch.setVisibility(View.VISIBLE);
            }
            /**Prize-notification-huangdianjun-2015.4.28-end*/
            v.setTag(vh);
            return v;
        }

        private void enableLayoutTransitions(ViewGroup vg, boolean enabled) {
            if (enabled) {
                vg.getLayoutTransition().enableTransitionType(LayoutTransition.APPEARING);
                vg.getLayoutTransition().enableTransitionType(LayoutTransition.DISAPPEARING);
            } else {
                vg.getLayoutTransition().disableTransitionType(LayoutTransition.APPEARING);
                vg.getLayoutTransition().disableTransitionType(LayoutTransition.DISAPPEARING);
            }
        }

        public void bindView(final View view, Row r, boolean animate) {
            if (!(r instanceof AppRow)) {
                // it's a section row
                final TextView tv = (TextView)view.findViewById(android.R.id.title);
                tv.setText(r.section);
                return;
            }

            final AppRow row = (AppRow)r;
            final ViewHolder vh = (ViewHolder) view.getTag();
            enableLayoutTransitions(vh.row, animate);
            /**Prize-notification-huangdianjun-2015.4.28-begin*/
            if(!PrizeOption.PRIZE_SETTINGS_NOTIFICATION){
            		vh.notiSwitch.setChecked(!row.banned);
            }
            //vh.rowDivider.setVisibility(row.first ? View.GONE : View.VISIBLE);
            /**Prize-notification-huangdianjun-2015.4.28-end*/
            vh.row.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                	/**Prize-notification-hekeyi-2015.4.8-begin*/
                	if(PrizeOption.PRIZE_SETTINGS_NOTIFICATION){
                			mContext.startActivity(new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                      .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                      .putExtra(Settings.EXTRA_APP_PACKAGE, row.pkg)
                      .putExtra(Settings.EXTRA_APP_UID, row.uid)
                      .putExtra(EXTRA_HAS_SETTINGS_INTENT, row.settingsIntent != null)
                      .putExtra(EXTRA_SETTINGS_INTENT, row.settingsIntent));
                	}else{
                		if(vh.notiSwitch.isChecked()){
                				vh.notiSwitch.setChecked(false);
//                				row.banned = false;
                				row.banned = true;
                				//prize-huangdj-20151106-start
                				countNotApp ++;
                				vh.summary.setText(mContext.getString(R.string.prize_noticentre_centre_blocked_summary));
                				setHeaderCount(countNotApp);
                		
                		}else{
                				vh.notiSwitch.setChecked(true);
//                				row.banned = true;
                				row.banned = false;
                				vh.summary.setText(mContext.getString(R.string.prize_noticentre_centre_allowed_summary));
                				countNotApp --;
                				setHeaderCount(countNotApp);
                				//prize-huangdj-20151106-end
                		}
                		mBackend.setNotificationsBanned(row.pkg, row.uid, row.banned);
                	}
                	/**Prize-notification-hekeyi-2015.4.8-end*/
                }
            });
            enableLayoutTransitions(vh.row, animate);
            vh.icon.setImageDrawable(row.icon);
            vh.title.setText(row.label);
			//prize-add-huangdianjun-20151118-start
            if(row.banned){
            	
            	vh.summary.setText(mContext.getString(R.string.prize_noticentre_centre_blocked_summary));
            }
            else{
            	vh.summary.setText(mContext.getString(R.string.prize_noticentre_centre_allowed_summary));
            }
			//prize-add-huangdianjun-20151118-end
            final String sub = getSubtitle(row);
            vh.subtitle.setText(sub);
            /** Prize-notification-hekeyi-2015-4-7-begin*/      			 
      			 if(!PrizeOption.PRIZE_SETTINGS_NOTIFICATION){
      			 			vh.subtitle.setVisibility(View.GONE);
      			 }else{
      			 			vh.subtitle.setVisibility(!sub.isEmpty() ? View.VISIBLE : View.GONE);
      			 }
     			 /** Prize-notification-hekeyi-2015-4-7-end*/
        }

        private String getSubtitle(AppRow row) {
            if (row.banned) {
                return mContext.getString(R.string.app_notification_row_banned);
            }
            if (!row.priority && !row.sensitive) {
                return EMPTY_SUBTITLE;
            }
            final String priString = mContext.getString(R.string.app_notification_row_priority);
            final String senString = mContext.getString(R.string.app_notification_row_sensitive);
            if (row.priority != row.sensitive) {
                return row.priority ? priString : senString;
            }
            return priString + mContext.getString(R.string.summary_divider_text) + senString;
        }

        @Override
        public Object[] getSections() {
            return mSections.toArray(new Object[mSections.size()]);
        }

        @Override
        public int getPositionForSection(int sectionIndex) {
            final String section = mSections.get(sectionIndex);
            final int n = getCount();
            for (int i = 0; i < n; i++) {
                final Row r = getItem(i);
                if (r.section.equals(section)) {
                    return i;
                }
            }
            return 0;
        }

        @Override
        public int getSectionForPosition(int position) {
            Row row = getItem(position);
            return mSections.indexOf(row.section);
        }
    }

    private static class Row {
        public String section;
    }

    public static class AppRow extends Row {
        public String pkg;
        public int uid;
        public Drawable icon;
        public CharSequence label;
        public Intent settingsIntent;
        public boolean banned;
        public boolean priority;
        public boolean sensitive;
        public boolean first;  // first app in section
        /**Prize-notification-hekeyi-2015.4.8-begin*/
        public boolean notiBoolean;
        /**Prize-notification-hekeyi-2015.4.8-end*/
    }

    private static final Comparator<AppRow> mRowComparator = new Comparator<AppRow>() {
        private final Collator sCollator = Collator.getInstance();
        @Override
        public int compare(AppRow lhs, AppRow rhs) {
            return sCollator.compare(lhs.label, rhs.label);
        }
    };

  //prize-huangdianjun-20151204-bugid9272-start
    public static AppRow loadAppRow(PackageManager pm, ApplicationInfo app,
            Backend backend,Context context) {
        final AppRow row = new AppRow();
        row.pkg = app.packageName;
        row.uid = app.uid;
        try {
            row.label = app.loadLabel(pm);
        } catch (Throwable t) {
            Log.e(TAG, "Error loading application label for " + row.pkg, t);
            row.label = row.pkg;
        }
        row.icon = loadAppIcon(app,pm,context);//add-by-huangdianjun-20151102
        row.banned = backend.getNotificationsBanned(row.pkg, row.uid);
        row.priority = backend.getHighPriority(row.pkg, row.uid);
        row.sensitive = backend.getSensitive(row.pkg, row.uid);
        row.notiBoolean = backend.getNotificationsBanned(row.pkg, row.uid);
        return row;
    }
  //prize-huangdianjun-20151204-bugid9272-end
    public static AppRow loadAppRow(PackageManager pm, ApplicationInfo app,
            Backend backend) {
        final AppRow row = new AppRow();
        row.pkg = app.packageName;
        row.uid = app.uid;
        try {
            row.label = app.loadLabel(pm);
        } catch (Throwable t) {
            Log.e(TAG, "Error loading application label for " + row.pkg, t);
            row.label = row.pkg;
        }
        row.icon = app.loadIcon(pm);
        row.banned = backend.getNotificationsBanned(row.pkg, row.uid);
        row.priority = backend.getHighPriority(row.pkg, row.uid);
        row.sensitive = backend.getSensitive(row.pkg, row.uid);
        row.notiBoolean = backend.getNotificationsBanned(row.pkg, row.uid);
        return row;
    }

    public static List<ResolveInfo> queryNotificationConfigActivities(PackageManager pm) {
        if (DEBUG) Log.d(TAG, "APP_NOTIFICATION_PREFS_CATEGORY_INTENT is "
                + APP_NOTIFICATION_PREFS_CATEGORY_INTENT);
        final List<ResolveInfo> resolveInfos = pm.queryIntentActivities(
                APP_NOTIFICATION_PREFS_CATEGORY_INTENT,
                0 //PackageManager.MATCH_DEFAULT_ONLY
        );
        return resolveInfos;
    }
    public static void collectConfigActivities(PackageManager pm, ArrayMap<String, AppRow> rows) {
        final List<ResolveInfo> resolveInfos = queryNotificationConfigActivities(pm);
        applyConfigActivities(pm, rows, resolveInfos);
    }

    public static void applyConfigActivities(PackageManager pm, ArrayMap<String, AppRow> rows,
            List<ResolveInfo> resolveInfos) {
        if (DEBUG) Log.d(TAG, "Found " + resolveInfos.size() + " preference activities"
                + (resolveInfos.size() == 0 ? " ;_;" : ""));
        for (ResolveInfo ri : resolveInfos) {
            final ActivityInfo activityInfo = ri.activityInfo;
            final ApplicationInfo appInfo = activityInfo.applicationInfo;
            final AppRow row = rows.get(appInfo.packageName);
            if (row == null) {
                Log.v(TAG, "Ignoring notification preference activity ("
                        + activityInfo.name + ") for unknown package "
                        + activityInfo.packageName);
                continue;
            }
            if (row.settingsIntent != null) {
                Log.v(TAG, "Ignoring duplicate notification preference activity ("
                        + activityInfo.name + ") for package "
                        + activityInfo.packageName);
                continue;
            }
            row.settingsIntent = new Intent(APP_NOTIFICATION_PREFS_CATEGORY_INTENT)
                    .setClassName(activityInfo.packageName, activityInfo.name);
        }
    }

	private boolean isSystemDefaultApp(String packageName) {
		if(packageName.equals("com.android.dialer") || packageName.equals("com.android.contacts") || packageName.equals("com.android.mms") ||
			packageName.equals("com.android.settings") || packageName.equals("com.android.calendar") || packageName.equals("com.android.deskclock") ||
			packageName.equals("com.android.email") || packageName.equals("com.android.gallery3d") || packageName.equals("com.android.music") ||
			packageName.equals("com.goodix.fpsetting") || packageName.equals("com.android.fileexplorer") || packageName.equals("com.android.soundrecorder") ||
			packageName.equals("com.mediatek.fmradio") || packageName.equals("com.prize.flash") || packageName.equals("com.prize.music") ||
			packageName.equals("com.android.stk") || packageName.equals("com.android.calculator2") || packageName.equals("com.android.development") ||
			packageName.equals("com.android.providers.downloads.ui")){
			return true;
		}
		return false;
	}

	
    private final Runnable mCollectAppsRunnable = new Runnable() {
        @Override
        public void run() {
            synchronized (mRows) {
                final long start = SystemClock.uptimeMillis();
                if (DEBUG) Log.d(TAG, "Collecting apps...");
                mRows.clear();
                mSortedRows.clear();

                // collect all launchable apps, plus any packages that have notification settings
                final List<ApplicationInfo> appInfos = new ArrayList<ApplicationInfo>();
				
				//add by liup add sogou control notification start
				final List<PackageInfo> packageInfos = mPM.getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES);
				for (PackageInfo info : packageInfos) {
					ApplicationInfo appInfo = info.applicationInfo;
					String packageName = appInfo.packageName;
					if(packageName.equals("com.sohu.inputmethod.sogou")){
						appInfos.add(appInfo);
					}
					
				}
				//add by liup add sogou control notification end
				
                final List<LauncherActivityInfo> lais
                        = mLauncherApps.getActivityList(null /* all */,
                            UserHandle.getCallingUserHandle());
                if (DEBUG) Log.d(TAG, "  launchable activities:");
                for (LauncherActivityInfo lai : lais) {
                    if (DEBUG) Log.d(TAG, "    " + lai.getComponentName().toString());
					if(!isSystemDefaultApp(lai.getApplicationInfo().packageName)){//add by liup remove system default control 20150820
						appInfos.add(lai.getApplicationInfo());
					}
                }

                final List<ResolveInfo> resolvedConfigActivities
                        = queryNotificationConfigActivities(mPM);
                if (DEBUG) Log.d(TAG, "  config activities:");
                for (ResolveInfo ri : resolvedConfigActivities) {
                    if (DEBUG) Log.d(TAG, "    "
                            + ri.activityInfo.packageName + "/" + ri.activityInfo.name);
                    appInfos.add(ri.activityInfo.applicationInfo);
                }

                for (ApplicationInfo info : appInfos) {
                    final String key = info.packageName;
                    if (mRows.containsKey(key)) {
                        // we already have this app, thanks
                        continue;
                    }
                    final AppRow row = loadAppRow(mPM, info, mBackend,mContext);//prize-huangdianjun-20151203-bugid9272
                    mRows.put(key, row);
                    
                }

                // add config activities to the list
                applyConfigActivities(mPM, mRows, resolvedConfigActivities);
                // sort rows
                mSortedRows.addAll(mRows.values());
                Collections.sort(mSortedRows, mRowComparator);
                // compute sections
                /**  Prize-notification clear section-hekeyi-2015.4.8-begin*/
                if(PrizeOption.PRIZE_SETTINGS_NOTIFICATION){
                		mSections.clear();
                		String section = null;
                		for (AppRow r : mSortedRows) {
                    		r.section = getSection(r.label);
                    		if (!r.section.equals(section)) {
                        		section = r.section;
                        		mSections.add(section);
                    		}
                		}
                }                
                /**  Prize-notification clear section-hekeyi-2015.4.8-end*/
                mHandler.post(mRefreshAppsListRunnable);
                final long elapsed = SystemClock.uptimeMillis() - start;
                if (DEBUG) Log.d(TAG, "Collected " + mRows.size() + " apps in " + elapsed + "ms");
                //add-by-huangdianjun-20151102-start
				countNotApp=0;
                for (int i = 0; i < mRows.size(); i++) {
                	String key = mRows.keyAt(i);
                	AppRow row = mRows.get(key);
                	boolean blockAppNoti = mBackend.getNotificationsBanned(key, row.uid);
                	if(blockAppNoti){
                		countNotApp++;
                	}
				}
                handler.sendEmptyMessage(OVER);
              //add-by-huangdianjun-20151102
              
            }
        }
    };

    private void refreshDisplayedItems() {
        if (DEBUG) Log.d(TAG, "Refreshing apps...");
        mAdapter.clear();
        synchronized (mSortedRows) {
            String section = null;
            if (mSortedRows == null) {
                return;
            }
            final int N = mSortedRows.size();
            boolean first = true;
            for (int i = 0; i < N; i++) {
                if (mSortedRows.size() < i) {
                    return;
                }

                final AppRow row = mSortedRows.get(i);
                /**Prize-notification-hekeyi-2015.4.8-begin*/
                if(PrizeOption.PRIZE_SETTINGS_NOTIFICATION){
                		if (!row.section.equals(section)) {
                    section = row.section;
                    Row r = new Row();
                    r.section = section;
                    mAdapter.add(r);
                    first = true;
                	}
                }
                row.first = first;
                mAdapter.add(row);
                first = false;
            }
        }
        if (mListViewState != null) {
            if (DEBUG) Log.d(TAG, "Restoring listView state");
            getListView().onRestoreInstanceState(mListViewState);
            mListViewState = null;
        }
        if (DEBUG) Log.d(TAG, "Refreshed " + mSortedRows.size() + " displayed items");
    }

    private final Runnable mRefreshAppsListRunnable = new Runnable() {
        @Override
        public void run() {
            refreshDisplayedItems();
        }
    };

    public static class Backend {
        static INotificationManager sINM = INotificationManager.Stub.asInterface(
                ServiceManager.getService(Context.NOTIFICATION_SERVICE));

        public boolean setNotificationsBanned(String pkg, int uid, boolean banned) {
            try {
                sINM.setNotificationsEnabledForPackage(pkg, uid, !banned);
                return true;
            } catch (Exception e) {
               Log.w(TAG, "Error calling NoMan", e);
               return false;
            }
        }

        public boolean getNotificationsBanned(String pkg, int uid) {
            try {
                final boolean enabled = sINM.areNotificationsEnabledForPackage(pkg, uid);
                return !enabled;
            } catch (Exception e) {
                Log.w(TAG, "Error calling NoMan", e);
                return false;
            }
        }

        public boolean getHighPriority(String pkg, int uid) {
            try {
                return sINM.getPackagePriority(pkg, uid) == Notification.PRIORITY_MAX;
            } catch (Exception e) {
                Log.w(TAG, "Error calling NoMan", e);
                return false;
            }
        }

        public boolean setHighPriority(String pkg, int uid, boolean highPriority) {
            try {
                sINM.setPackagePriority(pkg, uid,
                        highPriority ? Notification.PRIORITY_MAX : Notification.PRIORITY_DEFAULT);
                return true;
            } catch (Exception e) {
                Log.w(TAG, "Error calling NoMan", e);
                return false;
            }
        }

        public boolean getSensitive(String pkg, int uid) {
            try {
                return sINM.getPackageVisibilityOverride(pkg, uid) == Notification.VISIBILITY_PRIVATE;
            } catch (Exception e) {
                Log.w(TAG, "Error calling NoMan", e);
                return false;
            }
        }

        public boolean setSensitive(String pkg, int uid, boolean sensitive) {
            try {
                sINM.setPackageVisibilityOverride(pkg, uid,
                        sensitive ? Notification.VISIBILITY_PRIVATE
                                : NotificationListenerService.Ranking.VISIBILITY_NO_OVERRIDE);
                return true;
            } catch (Exception e) {
                Log.w(TAG, "Error calling NoMan", e);
                return false;
            }
        }
    }
	//prize-huangdianjun-20151102-start
    public static Drawable loadAppIcon(ApplicationInfo app,PackageManager pm,Context mContext) {
        if (app != null) {
    		if(app.packageName.contains("com.tencen1.mm")){
    			return mContext.getResources().getDrawable(R.drawable.prize_setting_weixin2);
			}else if(app.packageName.contains("com.tencent.mm")){
				return mContext.getResources().getDrawable(R.drawable.prize_setting_weixin);
			}else if(app.packageName.contains("com.qihoo.browser")){//com.baidu.browser.apps_mr
			    return mContext.getResources().getDrawable(R.drawable.prize_setting_browser);
			}else if(app.packageName.contains("com.qihoo360.mobilesafe")){
				return mContext.getResources().getDrawable(R.drawable.prize_setting_safecenter);
			}else if(app.packageName.contains("com.qiyi.video")){
				return mContext.getResources().getDrawable(R.drawable.prize_setting_aiqiyi);
			}else if(app.packageName.contains("com.android.dialer")){
				return mContext.getResources().getDrawable(R.drawable.prize_setting_dail);
			}else if(app.packageName.contains("com.android.calculator2")){
				return mContext.getResources().getDrawable(R.drawable.prize_setting_calc);
			}else if(app.packageName.contains("com.koobee.koobeecenter")){
				return mContext.getResources().getDrawable(R.drawable.prize_setting_koobeecenter);
			}else if(app.packageName.contains("com.android.soundrecorder")){
				return mContext.getResources().getDrawable(R.drawable.prize_setting_soundrecorder);
			}else if(app.packageName.contains("com.iLoong.base.themebox")){
				return mContext.getResources().getDrawable(R.drawable.prize_setting_meihua);
			}else if(app.packageName.contains("com.android.calendar") || app.packageName.contains("com.android.providers.calendar")){
				return mContext.getResources().getDrawable(R.drawable.prize_setting_calendar);
			}else if(app.packageName.contains("com.android.deskclock")){
				return mContext.getResources().getDrawable(R.drawable.prize_setting_clock);
			}else if(app.packageName.contains("com.mediatek.fmradio")){
				return mContext.getResources().getDrawable(R.drawable.prize_setting_fm);
			}else if(app.packageName.contains("com.baidu.searchbox")){
				return mContext.getResources().getDrawable(R.drawable.prize_setting_search);
			}else if(app.packageName.contains("com.sohu.newsclient")){
				return mContext.getResources().getDrawable(R.drawable.prize_setting_sohunews);
			}else if(app.packageName.contains("com.android.notepad")){
				return mContext.getResources().getDrawable(R.drawable.prize_setting_notepad);
			}else if(app.packageName.contains("com.tencent.qqlive")){
				return mContext.getResources().getDrawable(R.drawable.prize_setting_qqlive);
			}else if(app.packageName.contains("com.tencent.news")){
				return mContext.getResources().getDrawable(R.drawable.prize_setting_tencentnews);
			}else if(app.packageName.contains("net.qihoo.launcher.widget.clockweather")){
				return mContext.getResources().getDrawable(R.drawable.prize_setting_weather);
			}else if(app.packageName.contains("com.android.contacts")){
				return mContext.getResources().getDrawable(R.drawable.prize_setting_contact);
			}else if(app.packageName.contains("com.android.gallery3d")){
				return mContext.getResources().getDrawable(R.drawable.prize_setting_gallery);
			}else if(app.packageName.contains("com.android.fileexplorer")){
				return mContext.getResources().getDrawable(R.drawable.prize_setting_filemanager);
			}else if(app.packageName.contains("com.adups.fota")){
				return mContext.getResources().getDrawable(R.drawable.prize_setting_fota);
			}else if(app.packageName.contains("com.android.providers.downloads")){
				return mContext.getResources().getDrawable(R.drawable.prize_setting_download);
			}else if(app.packageName.contains("ctrip.android.view")){
				return mContext.getResources().getDrawable(R.drawable.prize_setting_xiechen);
			}else if(app.packageName.contains("com.android.mms")){
				return mContext.getResources().getDrawable(R.drawable.prize_setting_sms);
			}else if(app.packageName.contains("com.tencent.android.qqdownloader")){
				return mContext.getResources().getDrawable(R.drawable.prize_setting_tencentapp);
			}else if(app.packageName.contains("com.joloplay.gamecenter.prize")){
				return mContext.getResources().getDrawable(R.drawable.prize_setting_gamecenter);
			}else if(app.packageName.contains("com.prize.weather")){
				return mContext.getResources().getDrawable(R.drawable.prize_setting_weather);
			}else if(app.packageName.contains("com.tencent.mobileqq")){
				return mContext.getResources().getDrawable(R.drawable.prize_setting_qq);
			}else if(app.packageName.contains("com.android.settings")){
				return mContext.getResources().getDrawable(R.drawable.prize_setting_settings);
			}else return app.loadIcon(pm);
			
        }
        return null;
    }
    public void setHeaderCount(int count) {

		countNotAppTv.setText(mContext.getResources().getString(
				R.string.prize_notification_app_count_title1)
				+ countNotApp
				+ getResources().getString(
						R.string.prize_notification_app_count_title2));
	}
	//prize-huangdianjun-20151102-end
}
