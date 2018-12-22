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
import android.os.SystemProperties;

public class PrizeFloatWindowManager extends ListFragment {

	private ArrayList<AppInfo> mlistAppInfo;
	private WindowManager wm;
	private static final int OVER = 1;
	private FloatWindowManagerAdapter floatAdapter;
	private TextView countTv;
	private int count = 0;
	private Handler handler = null;
	private boolean floatBln;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		wm = (WindowManager) getActivity().getSystemService(
				Context.WINDOW_SERVICE);
		mlistAppInfo = new ArrayList<AppInfo>();
		new Thread(new Runnable() {
			public void run() {
				queryAppInfo();
				handler.sendEmptyMessage(OVER);

			}
		}).start();
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

	public void onDestroy() {
		super.onDestroy();
	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.floatwindow_manager_app_list,
				container, false);
		countTv = (TextView) view.findViewById(R.id.floatwindow_app_count_txt);
		return view;
	}

	public void refresh() {
		countTv.setVisibility(View.VISIBLE);
		floatAdapter = new FloatWindowManagerAdapter(getActivity(),
				mlistAppInfo);
		floatAdapter.notifyDataSetChanged();
		setListAdapter(floatAdapter);
		getListView().setOnItemClickListener(new MyOnItemClickListener());
		setHeaderCount(count);
	}

	private boolean checkPemission(String packageName) {
		PackageManager pm = getActivity().getPackageManager();
		boolean permission = (PackageManager.PERMISSION_GRANTED == pm
				.checkPermission("android.permission.SYSTEM_ALERT_WINDOW",
						packageName));
		return permission;
	}

	public void queryAppInfo() {
		PackageManager pm = getActivity().getPackageManager();
		Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
		mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		List<ResolveInfo> resolveInfos = pm.queryIntentActivities(mainIntent,
				PackageManager.GET_UNINSTALLED_PACKAGES);
		Collections.sort(resolveInfos,
				new ResolveInfo.DisplayNameComparator(pm));
		if (mlistAppInfo != null) {
			mlistAppInfo.clear();
			for (ResolveInfo reInfo : resolveInfos) {
				if (!reInfo.activityInfo.packageName
						.equals("com.android.music") && checkPemission(reInfo.activityInfo.packageName)) {
					String activityName = reInfo.activityInfo.name;
					String pkgName = reInfo.activityInfo.packageName;
					String appLabel = (String) reInfo.loadLabel(pm);
					Drawable icon = loadAppIcon(reInfo, pm);
					Intent launchIntent = new Intent();
					launchIntent.setComponent(new ComponentName(pkgName,
							activityName));
					AppInfo appInfo = new AppInfo();
					if (pkgName.equals("com.android.gallery3d")) {
						floatBln = wm.getFloatEnable("com.prize.videoc");
					} else if (pkgName.equals("com.prize.videoc")) {
						floatBln = wm.getFloatEnable("com.android.gallery3d");
					} else {
						floatBln = wm.getFloatEnable(pkgName);
					}
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
			AppInfo appInfo = mlistAppInfo.get(position);

			Switch mSwitch = (Switch) view.findViewById(R.id.float_switch);
			if (mSwitch.isChecked()) {
				mSwitch.setChecked(false);
				if (appInfo.getPkgName().equals("com.android.gallery3d")) {
					wm.setFloatEnable("com.prize.videoc", false);
				} else if (appInfo.getPkgName().equals("com.prize.videoc")) {
					wm.setFloatEnable("com.android.gallery3d", false);
				} else {
					wm.setFloatEnable(appInfo.getPkgName(), false);
				}
				appInfo.setFloatBln(false);
				floatAdapter.notifyDataSetChanged();
				count--;
				setHeaderCount(count);
			} else {
				mSwitch.setChecked(true);
				if (appInfo.getPkgName().equals("com.android.gallery3d")) {
					wm.setFloatEnable("com.prize.videoc", true);
				} else if (appInfo.getPkgName().equals("com.prize.videoc")) {
					wm.setFloatEnable("com.android.gallery3d", true);
				} else {
					wm.setFloatEnable(appInfo.getPkgName(), true);
				}
				appInfo.setFloatBln(true);
				floatAdapter.notifyDataSetChanged();
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
			AppInfo appInfo = (AppInfo) getItem(position);
			holder.appIcon.setImageDrawable(appInfo.getAppIcon());
			holder.tvAppLabel.setText(appInfo.getAppLabel());
			holder.floatSwitch.setChecked(appInfo.isFloatBln());
			if (appInfo.isFloatBln()) {
				holder.summary.setText(getActivity().getString(
						R.string.prize_noticentre_centre_allowed_summary));
			} else {
				holder.summary.setText(getActivity().getString(
						R.string.prize_noticentre_centre_blocked_summary));
			}
			return view;
		}

		class ViewHolder {
			ImageView appIcon;
			TextView tvAppLabel;
			TextView summary;
			Switch floatSwitch;

			public ViewHolder(View view) {
				this.appIcon = (ImageView) view.findViewById(android.R.id.icon);
				this.tvAppLabel = (TextView) view
						.findViewById(android.R.id.title);
				this.summary = (TextView) view.findViewById(R.id.summary);
				
				//add for simpleLuancher by liup 20160112 start
				if(SystemProperties.get("persist.sys.simpleLuancher").equals("1")){
					this.summary.setVisibility(View.GONE);
				}
				//add for simpleLuancher by liup 20160112 end
			
				this.floatSwitch = (Switch) view
						.findViewById(R.id.float_switch);
			}
		}
	}

	public Drawable loadAppIcon(ResolveInfo reInfo, PackageManager pm) {
		if (reInfo != null) {
			if (reInfo.toString().contains("com.tencen1.mm")) {
				return getActivity().getResources().getDrawable(
						R.drawable.prize_setting_weixin2);
			} else if (reInfo.toString().contains("com.tencent.mm")) {
				return getActivity().getResources().getDrawable(
						R.drawable.prize_setting_weixin);
			} else if (reInfo.toString().contains("com.qihoo.browser")) {
				return getActivity().getResources().getDrawable(
						R.drawable.prize_setting_browser);
			} else if (reInfo.toString().contains("com.qihoo360.mobilesafe")) {
				return getActivity().getResources().getDrawable(
						R.drawable.prize_setting_safecenter);
			} else if (reInfo.toString().contains("com.qiyi.video")) {
				return getActivity().getResources().getDrawable(
						R.drawable.prize_setting_aiqiyi);
			} else if (reInfo.toString().contains("com.android.dialer")) {
				return getActivity().getResources().getDrawable(
						R.drawable.prize_setting_dail);
			} else if (reInfo.toString().contains("com.android.calculator2")) {
				return getActivity().getResources().getDrawable(
						R.drawable.prize_setting_calc);
			} else if (reInfo.toString().contains("com.koobee.koobeecenter")) {
				return getActivity().getResources().getDrawable(
						R.drawable.prize_setting_koobeecenter);
			} else if (reInfo.toString().contains("com.android.soundrecorder")) {
				return getActivity().getResources().getDrawable(
						R.drawable.prize_setting_soundrecorder);
			} else if (reInfo.toString().contains("com.iLoong.base.themebox")) {
				return getActivity().getResources().getDrawable(
						R.drawable.prize_setting_meihua);
			} else if (reInfo.toString().contains("com.android.calendar")
					|| reInfo.toString().contains(
							"com.android.providers.calendar")) {
				return getActivity().getResources().getDrawable(
						R.drawable.prize_setting_calendar);
			} else if (reInfo.toString().contains("com.android.deskclock")) {
				return getActivity().getResources().getDrawable(
						R.drawable.prize_setting_clock);
			} else if (reInfo.toString().contains("com.mediatek.fmradio")) {
				return getActivity().getResources().getDrawable(
						R.drawable.prize_setting_fm);
			} else if (reInfo.toString().contains("com.baidu.searchbox")) {
				return getActivity().getResources().getDrawable(
						R.drawable.prize_setting_search);
			} else if (reInfo.toString().contains("com.sohu.newsclient")) {
				return getActivity().getResources().getDrawable(
						R.drawable.prize_setting_sohunews);
			} else if (reInfo.toString().contains("com.android.notepad")) {
				return getActivity().getResources().getDrawable(
						R.drawable.prize_setting_notepad);
			} else if (reInfo.toString().contains("com.tencent.qqlive")) {
				return getActivity().getResources().getDrawable(
						R.drawable.prize_setting_qqlive);
			} else if (reInfo.toString().contains("com.tencent.news")) {
				return getActivity().getResources().getDrawable(
						R.drawable.prize_setting_tencentnews);
			} else if (reInfo.toString().contains(
					"net.qihoo.launcher.widget.clockweather")) {
				return getActivity().getResources().getDrawable(
						R.drawable.prize_setting_weather);
			} else if (reInfo.toString().contains("com.android.contacts")) {
				return getActivity().getResources().getDrawable(
						R.drawable.prize_setting_contact);
			} else if (reInfo.activityInfo.name.contains("com.android.camera.CameraLauncher")) {
				return getActivity().getResources().getDrawable(
						R.drawable.prize_setting_camera);
			} else if (reInfo.activityInfo.name.contains("com.android.gallery3d.app.GalleryActivity")) {
				return getActivity().getResources().getDrawable(
						R.drawable.prize_setting_gallery);
			} 
			else if (reInfo.toString().contains("com.android.fileexplorer")) {
				return getActivity().getResources().getDrawable(
						R.drawable.prize_setting_filemanager);
			} else if (reInfo.toString().contains("com.adups.fota")) {
				return getActivity().getResources().getDrawable(
						R.drawable.prize_setting_fota);
			} else if (reInfo.toString().contains(
					"com.android.providers.downloads")) {
				return getActivity().getResources().getDrawable(
						R.drawable.prize_setting_download);
			} else if (reInfo.toString().contains("ctrip.android.view")) {
				return getActivity().getResources().getDrawable(
						R.drawable.prize_setting_xiechen);
			} else if (reInfo.toString().contains("com.android.mms")) {
				return getActivity().getResources().getDrawable(
						R.drawable.prize_setting_sms);
			} else if (reInfo.toString().contains(
					"com.tencent.android.qqdownloader")) {
				return getActivity().getResources().getDrawable(
						R.drawable.prize_setting_tencentapp);
			} else if (reInfo.toString().contains(
					"com.joloplay.gamecenter.prize")) {
				return getActivity().getResources().getDrawable(
						R.drawable.prize_setting_gamecenter);
			} else if (reInfo.toString().contains("com.prize.weather")) {
				return getActivity().getResources().getDrawable(
						R.drawable.prize_setting_weather);
			} else if (reInfo.toString().contains("com.tencent.mobileqq")) {
				return getActivity().getResources().getDrawable(
						R.drawable.prize_setting_qq);
			} else if (reInfo.toString().contains("com.android.settings")) {
				return getActivity().getResources().getDrawable(
						R.drawable.prize_setting_settings);
			} else
				return reInfo.loadIcon(pm);
		}
		return null;
	}

}
