

/*******************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：读取应用类
 *异步读取Apps
 *当前版本：V1.0
 *作	者：zhouerlong
 *完成日期：2015-8-5
 *********************************************/
package com.android.launcher3.search.data;

import java.util.HashMap;
import java.util.List;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.launcher3.ImageUtils;
import com.android.launcher3.Launcher;
import com.android.launcher3.R;
import com.android.launcher3.search.GroupMemberBean;
import com.android.launcher3.search.MyExpandableAdapter;

public class GetAppsResponse extends RSTResponse {

	private PackageManager mPackageManager;

	public GetAppsResponse(Context mContext,
			List<GroupMemberBean> mGroupBeanList,
			HashMap<Integer, List<GroupMemberBean>> mGroupChildList,
			String groupTitle, MyExpandableAdapter adpter, Runnable r,
			HashMap<String, AsyncTaskCallback> groupClass, List<String> groups) {
		super(mContext, mGroupBeanList, mGroupChildList, groupTitle, adpter, r,
				groupClass, groups);
		// TODO Auto-generated constructor stub
	}

	@Override
	public List<GroupMemberBean> run() {
		ActivityManager activityManager = (ActivityManager) mContext
				.getSystemService(Context.ACTIVITY_SERVICE);
		mIconDpi = activityManager.getLauncherLargeIconDensity();

		final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
		mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

		List<ResolveInfo> apps = null;
		mPackageManager = mContext.getPackageManager();
		apps = mPackageManager.queryIntentActivities(mainIntent, 0);
		for (ResolveInfo app : apps) {
			String title = (String) app.loadLabel(mPackageManager);
			Drawable icon = null;
			if (mContext instanceof Launcher) {
				Launcher launcher = (Launcher) mContext;
					icon =this.getFullResIcon(app);
			}
			
			
			GroupMemberBean childBean = this.fillData(title);
			childBean.setApps(new AppsBean(app));
			if (icon != null) {
				childBean.setIcon(icon);
			}
			mTask.doPublishProgress(childBean);
			// mApps.add(childBean);
		}
		// mGroupChildBeanList.add(mApps);

		return mApps;
	}

	@Override
	public void onProgressUpdate(GroupMemberBean... values) {

		super.onProgressUpdate(values);
	}

	public Drawable getFullResIcon(ActivityInfo info) {

		mPackageManager = mContext.getPackageManager();
		Resources resources;
		try {
			resources = mPackageManager
					.getResourcesForApplication(info.applicationInfo);
		} catch (PackageManager.NameNotFoundException e) {
			resources = null;
		}
		if (resources != null) {
			int iconId = info.getIconResource();
			if (iconId != 0) {
				return getFullResIcon(resources, iconId);
			}
		}
		return null;
	}
	
	

	public Drawable getFullResIcon(ResolveInfo info) {
		return getFullResIcon(info.activityInfo);
	}

	public Drawable getFullResIcon(Resources resources, int iconId) {
		Drawable d;
		try {
			d = resources.getDrawableForDensity(iconId, mIconDpi);
		} catch (Resources.NotFoundException e) {
			d = null;
		}

		return (d != null) ? d : mContext.getResources().getDrawable(
				R.drawable.ic_launcher);
	}

	@Override
	public View getchildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent,
			List<GroupMemberBean> group,
			HashMap<Integer, List<GroupMemberBean>> child) {
		// TODO Auto-generated method stub
		return super.getchildView(groupPosition, childPosition, isLastChild,
				convertView, parent, group, child);
	}
	
	@Override
	public void onClick(GroupMemberBean item) {
		
		// TODO Auto-generated method stub
		Intent intent =item.getApps().getIntent();
		boolean isCanSetup = ((Launcher)mContext).isCanSetup(intent);
		if (!isCanSetup) {
			return;
		}
		mContext.startActivity(intent);
	}

}
