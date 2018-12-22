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
 * author:huangdianjun-floatwindow_manager-20151118
 */
package com.android.settings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.AppOpsManager;
import android.app.ListFragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.webkit.WebView.FindListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.android.prize.AppInfo;
import android.os.Parcelable;
import android.widget.ListView;
import android.content.pm.PackageInfo;

public class PrizeFloatWindowManager extends ListFragment {
	/* prize-add-by-lijimeng-20171219-start*/
	private static int left;
	private static int top;
	private static int right;
	private static int bottom;
	private static int topminHeight;
	private static int minHeight;
	/* prize-add-by-lijimeng-20171219-end*/
	private ArrayList<AppInfo> mlistAppInfo;
	private WindowManager wm;
	private static final int OVER = 1;
	private FloatWindowManagerAdapter floatAdapter;
	private TextView countTv;
	private int count = 0;
	private Handler handler = null;
	private boolean floatBln;
	/*add by liuweiquan 20160714 start*/
	private Handler mHandler = null;
	private Context mContext;
	/*add by liuweiquan 20160714 end*/
	
	private Parcelable mListViewState;
	private ListView mListView;
	private ExecutorService mExecutorService;
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		mContext = getActivity();
		prizeInitDimens(mContext);
		mExecutorService = Executors.newFixedThreadPool(2);
		wm = (WindowManager) mContext.getSystemService(
				Context.WINDOW_SERVICE);
		mlistAppInfo = new ArrayList<AppInfo>();
		/*add by liuweiquan 20160714 start*/
		
		mHandlerThread.start();
		mHandler = new Handler(mHandlerThread.getLooper());
		mHandler.post(mRunnable);
		/*add by liuweiquan 20160714 end*/
		if (handler == null) {
			handler = new Handler() {
				public void handleMessage(Message msg) {
					switch (msg.what) {
					case OVER:
						refresh();
						break;
					default:
						break;
					}
					super.handleMessage(msg);
				}
			};
		}
	}
	/*add by liuweiquan 20160714 start*/
	Runnable mRunnable = new Runnable() {
		public void run() {
			queryAppInfo();
			if(mContext==null)
				return;
			handler.sendEmptyMessage(OVER);
		}
	};
	HandlerThread mHandlerThread = new HandlerThread ("PrizeFloatWindowManager");
	
	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		mHandler.removeCallbacks(mRunnable);
		mHandlerThread.quitSafely();
		
		super.onPause();
	}
	/*add by liuweiquan 20160714 end*/
	
	public void onDestroy() {
		super.onDestroy();
	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.floatwindow_manager_app_list,
				container, false);
		countTv = (TextView) view.findViewById(R.id.floatwindow_app_count_txt);
		mListView = (ListView)view.findViewById(android.R.id.list);
		//mListView.setNestedScrollingEnabled(false);
		return view;
	}

	public void refresh() {
		//countTv.setVisibility(View.VISIBLE);
		floatAdapter = new FloatWindowManagerAdapter(mContext,
				mlistAppInfo);
		floatAdapter.notifyDataSetChanged();
		setListAdapter(floatAdapter);
		try{
			getListView().setOnItemClickListener(new MyOnItemClickListener());
			setHeaderCount(count);
		}catch(Throwable t){
			
		}
		
	}

	private boolean checkPemission(String packageName) {
		if(null == mContext.getPackageManager()){
			return false;
		}
		PackageManager pm = mContext.getPackageManager();
		if(null != pm){
			/*boolean permission = (PackageManager.PERMISSION_GRANTED == pm
					.checkPermission("android.permission.SYSTEM_ALERT_WINDOW",
							packageName));
			try {
				int uid = pm.getPackageUid(packageName, UserHandle.myUserId());
				mode = mAppOpsManager.checkOp(
						AppOpsManager.OP_SYSTEM_ALERT_WINDOW, uid, packageName);
			} catch (NameNotFoundException e) {
			} catch (Exception exp) {
			}
			return permission || mode == AppOpsManager.MODE_ALLOWED;*/
			/*-prize-add by lihuangyuan,for change check system_alert_window permission,2017-08-23-start*/
			try {
				PackageInfo info = pm.getPackageInfo(packageName,PackageManager.GET_PERMISSIONS);
				if(info != null && info.requestedPermissions != null)
				{
					for(int i =0;i<info.requestedPermissions.length;i++)
					{
						if("android.permission.SYSTEM_ALERT_WINDOW".equals(info.requestedPermissions[i]))
						{
							return true;
						}
					}
				}
			} catch (Exception exp) {
			}
			/*-prize-add by lihuangyuan,for change check system_alert_window permission,2017-08-23-end*/
		}
		return false;
	}

	public void queryAppInfo() {
		PackageManager pm = mContext.getPackageManager();
		Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
		mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		List<ResolveInfo> resolveInfos = pm.queryIntentActivities(mainIntent,
				PackageManager.GET_ACTIVITIES);
		Collections.sort(resolveInfos,
				new ResolveInfo.DisplayNameComparator(pm));
		if (mlistAppInfo != null) {
			mlistAppInfo.clear();
			for (ResolveInfo reInfo : resolveInfos) {
				if (!reInfo.activityInfo.packageName
						.equals("com.android.music") && !reInfo.activityInfo.packageName
						.equals("com.prize.factorytest") && checkPemission(reInfo.activityInfo.packageName)) {
					String activityName = reInfo.activityInfo.name;
					String pkgName = reInfo.activityInfo.packageName;
                                  //filter some apk
                                    if(pkgName.equals("com.android.dialer"))
                                    {            
                                        continue;
                                    }
					/* prize-modify-by-lijimeng-for bugid 52076-20180307-start*/
					//String appLabel = (String) reInfo.loadLabel(pm);
					String appLabel = (String) reInfo.activityInfo.applicationInfo.loadLabel(pm);
					/* prize-modify-by-lijimeng-for bugid 52076-20180307-end*/
					Drawable icon = reInfo.loadIcon(pm);
					Intent launchIntent = new Intent();
					launchIntent.setComponent(new ComponentName(pkgName,
							activityName));
					AppInfo appInfo = new AppInfo();
					//if (pkgName.equals("com.android.gallery3d")) {
						//floatBln = wm.getFloatEnable("com.prize.videoc");
					//} else if (pkgName.equals("com.prize.videoc")) {
						//floatBln = wm.getFloatEnable("com.android.gallery3d");
					//} else {
						floatBln = wm.getFloatEnable(pkgName);
					//}
					if (floatBln) {
						count++;
					}
					appInfo.setFloatBln(floatBln);
					appInfo.setPkgName(pkgName);
					appInfo.setAppLabel(appLabel);
					appInfo.setAppIcon(icon);
					appInfo.setIntent(launchIntent);
					mlistAppInfo.add(appInfo);
				}

			}
		}
	}

	public class MyOnItemClickListener implements OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			final AppInfo appInfo = mlistAppInfo.get(position);

			Switch mSwitch = (Switch) view.findViewById(R.id.float_switch);
			if (mSwitch.isChecked()) {
				mSwitch.setChecked(false);
				//if (appInfo.getPkgName().equals("com.android.gallery3d")) {
					//wm.setFloatEnable("com.prize.videoc", false);
				//} else if (appInfo.getPkgName().equals("com.prize.videoc")) {
					//wm.setFloatEnable("com.android.gallery3d", false);
				//} else {
				if(mExecutorService != null){
					Runnable runnable = new Runnable() {
						@Override
						public void run() {
							wm.setFloatEnable(appInfo.getPkgName(), false);
							appInfo.setFloatBln(false);
						}
					};
					mExecutorService.execute(runnable);
				}
				//}
				//floatAdapter.notifyDataSetChanged();
				count--;
				setHeaderCount(count);
			} else {
				mSwitch.setChecked(true);
				//if (appInfo.getPkgName().equals("com.android.gallery3d")) {
					//wm.setFloatEnable("com.prize.videoc", true);
				//} else if (appInfo.getPkgName().equals("com.prize.videoc")) {
					//wm.setFloatEnable("com.android.gallery3d", true);
				//} else {
				if(mExecutorService != null){
					Runnable runnable = new Runnable() {
						@Override
						public void run() {
							wm.setFloatEnable(appInfo.getPkgName(), true);
							appInfo.setFloatBln(true);
						}
					};
					mExecutorService.execute(runnable);
				}
				//}

				//floatAdapter.notifyDataSetChanged();
				count++;
				setHeaderCount(count);
			}
		}

	}

	public void setHeaderCount(int count) {

		countTv.setText(getResources().getString(
				R.string.prize_floatwindow_app_count_title1)
				+ count
				+ getResources().getString(
						R.string.prize_floatwindow_app_count_title2));
	}

	public class FloatWindowManagerAdapter extends BaseAdapter {

		private List<AppInfo> mlistAppInfo = null;

		LayoutInflater infater = null;

		public FloatWindowManagerAdapter(Context context, List<AppInfo> apps) {
			infater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			mlistAppInfo = apps;
		}

		@Override
		public int getCount() {
			return mlistAppInfo.size();
		}

		@Override
		public Object getItem(int position) {
			return mlistAppInfo.get(position);
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertview, ViewGroup arg2) {
			System.out.println("getView at " + position);
			View view = null;
			ViewHolder holder = null;
			if (convertview == null || convertview.getTag() == null) {
				view = infater.inflate(R.layout.floatwindow_manager_app_item,
						null);
				holder = new ViewHolder(view);
				view.setTag(holder);
			} else {
				view = convertview;
				holder = (ViewHolder) convertview.getTag();
			}
			/* prize-add-by-lijimeng-20171219-start*/
			if(getCount() > 1){
				if(position == 0){
					holder.dividerLines.setVisibility(View.VISIBLE);
					view.setMinimumHeight(topminHeight);
					view.setPadding(left,top,right,0);
					view.setBackgroundResource(R.drawable.list_preferencecategory_selector);
				}else if(position == getCount()-1){
					holder.dividerLines.setVisibility(View.GONE);
					view.setMinimumHeight(topminHeight);
					view.setPadding(left,0,right,bottom);
					view.setBackgroundResource(R.drawable.list_topmorepreferencecategory_selector);
				}else{
					holder.dividerLines.setVisibility(View.VISIBLE);
					view.setMinimumHeight(minHeight);
					view.setPadding(left,0,right,0);
					view.setBackgroundResource(R.drawable.npreferencecategory_selector);
				}
			}else{

				view.setPadding(left,top,right,0);
				view.setBackgroundResource(R.drawable.list_toponelistpreferencecategory_selector);
			}
			/* prize-add-by-lijimeng-20171219-end*/

			AppInfo appInfo = (AppInfo) getItem(position);
			holder.appIcon.setImageDrawable(appInfo.getAppIcon());
			holder.tvAppLabel.setText(appInfo.getAppLabel());
			holder.floatSwitch.setChecked(appInfo.isFloatBln());
			if (appInfo.isFloatBln()) {
				holder.summary.setText(mContext.getString(
						R.string.prize_noticentre_centre_allowed_summary));
			} else {
				holder.summary.setText(mContext.getString(
						R.string.prize_noticentre_centre_blocked_summary));
			}
			return view;
		}

		class ViewHolder {
			ImageView appIcon;
			TextView tvAppLabel;
			TextView summary;
			Switch floatSwitch;
			View dividerLines;
			public ViewHolder(View view) {
				this.appIcon = (ImageView) view.findViewById(android.R.id.icon);
				this.dividerLines =  view.findViewById(R.id.divider_lines);
				this.tvAppLabel = (TextView) view
						.findViewById(android.R.id.title);
				this.summary = (TextView) view.findViewById(R.id.summary);
				this.floatSwitch = (Switch) view
						.findViewById(R.id.float_switch);
			}
		}
	}
	/* prize-add-by-lijimeng-20171219-start*/
	private void prizeInitDimens(Context context){
		left = context.getResources().getDimensionPixelSize(R.dimen.prize_preferencefragment_padding_start);
		top = context.getResources().getDimensionPixelSize(R.dimen.prize_preferencefragment_card_magintop);
		right = context.getResources().getDimensionPixelSize(R.dimen.prize_preferencefragment_pading_end);
		bottom = context.getResources().getDimensionPixelSize(R.dimen.prize_preferencefragment_card_maginbottom);
		topminHeight = context.getResources().getDimensionPixelSize(R.dimen.prize_notification_item_minheight);
		minHeight = context.getResources().getDimensionPixelSize(R.dimen.prize_list_icon_item_minhight);
	}
	/* prize-add-by-lijimeng-20171219-end*/
}
