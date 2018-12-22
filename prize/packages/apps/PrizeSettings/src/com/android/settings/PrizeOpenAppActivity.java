package com.android.settings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class PrizeOpenAppActivity extends Activity implements OnItemClickListener {

	private ListView listview = null;

	public static List<AppInfo> mlistAppInfo;
	String string;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.prize_broswe_app_list);

		listview = (ListView) findViewById(R.id.listviewApp);
		mlistAppInfo = new ArrayList<AppInfo>();
		string = getIntent().getExtras().getString("gesture");
		
		queryAppInfo();
		BrowseApplicationInfoAdapter browseAppAdapter = new BrowseApplicationInfoAdapter(
				this, mlistAppInfo);
		listview.setAdapter(browseAppAdapter);
		listview.setOnItemClickListener(this);
	}

	public void onItemClick(AdapterView<?> arg0, View view, int position,
			long arg3) {
		// TODO Auto-generated method stub	
		Intent intent = new Intent();
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.putExtra("postion", position);
		intent.putExtra("gesture", string);
		PrizeSleepGestureActivity.sAppBool = true;
        intent.setClass(this,PrizeSleepGestureActivity.class);
        startActivity(intent);
	}

	public void queryAppInfo() {
		PackageManager pm = this.getPackageManager();

		Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
		mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		List<ResolveInfo> resolveInfos = pm
				.queryIntentActivities(mainIntent, 0);

		Collections.sort(resolveInfos,
				new ResolveInfo.DisplayNameComparator(pm));
		if (mlistAppInfo != null) {
			mlistAppInfo.clear();
			for (ResolveInfo reInfo : resolveInfos) {
				String activityName = reInfo.activityInfo.name;
				String pkgName = reInfo.activityInfo.packageName;
				String appLabel = (String) reInfo.loadLabel(pm);
				Drawable icon = reInfo.loadIcon(pm);

				Intent launchIntent = new Intent();
				launchIntent.setComponent(new ComponentName(pkgName,
						activityName));

				AppInfo appInfo = new AppInfo();
				appInfo.setAppLabel(appLabel);
				appInfo.setAppIcon(icon);
				appInfo.setIntent(launchIntent);
				appInfo.setPkgName(pkgName);
				appInfo.setActivityName(activityName);
				mlistAppInfo.add(appInfo);
			}
		}
	}

	public class BrowseApplicationInfoAdapter extends BaseAdapter {

		private List<AppInfo> mlistAppInfo = null;

		LayoutInflater infater = null;

		public BrowseApplicationInfoAdapter(Context context, List<AppInfo> apps) {
			infater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			mlistAppInfo = apps;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			System.out.println("size" + mlistAppInfo.size());
			return mlistAppInfo.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return mlistAppInfo.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(int position, View convertview, ViewGroup arg2) {
			View view = null;
			ViewHolder holder = null;
			if (convertview == null || convertview.getTag() == null) {
				view = infater.inflate(R.layout.prize_browse_app_item, null);
				holder = new ViewHolder(view);
				view.setTag(holder);
			} else {
				view = convertview;
				holder = (ViewHolder) convertview.getTag();
			}
			AppInfo appInfo = (AppInfo) getItem(position);
			holder.appIcon.setImageDrawable(appInfo.getAppIcon());
			holder.tvAppLabel.setText(appInfo.getAppLabel());
			return view;
		}

		class ViewHolder {
			ImageView appIcon;
			TextView tvAppLabel;

			public ViewHolder(View view) {
				this.appIcon = (ImageView) view.findViewById(R.id.imgApp);
				this.tvAppLabel = (TextView) view.findViewById(R.id.tvAppLabel);
			}
		}
	}

	public class AppInfo {

		private String appLabel;
		private Drawable appIcon;
		private Intent intent;
		private String pkgName;
		private String activityName;

		public AppInfo() {
		}

		public String getAppLabel() {
			return appLabel;
		}

		public void setAppLabel(String appName) {
			this.appLabel = appName;
		}

		public Drawable getAppIcon() {
			return appIcon;
		}

		public void setAppIcon(Drawable appIcon) {
			this.appIcon = appIcon;
		}

		public Intent getIntent() {
			return intent;
		}

		public void setIntent(Intent intent) {
			this.intent = intent;
		}

		public String getPkgName() {
			return pkgName;
		}

		public void setPkgName(String pkgName) {
			this.pkgName = pkgName;
		}
		public String getActivityName() {
			return activityName;
		}

		public void setActivityName(String activityName) {
			this.activityName = activityName;
		}
		
	}
}