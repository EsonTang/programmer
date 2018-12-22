
/*******************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：经期任务主界面控件
 *当前版本：V1.0
 *作	者：zhouerlong
 *完成日期：2015-8-5
 *********************************************/

package com.android.launcher3.search.data.recent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageParser.Component;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.GridView;
import android.widget.RelativeLayout;

import com.android.launcher3.Launcher;
import com.android.launcher3.R;

public class RecentRelativeLayout extends RelativeLayout {

	private GridView list;
	private List<HashMap<String, Object>> appInfos = new ArrayList<HashMap<String, Object>>();
	private RencentAppAdapter renAdapter;
	private RecentAppCallback r;
	private boolean mAnimationEnd=false;
	private  boolean mResultOK=false;

	public RecentRelativeLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onFinishInflate() {
		// TODO Auto-generated method stub
		super.onFinishInflate();
		this.init();
	}

	public void reload() {
		
		r.cancel();
		appInfos.clear();
		 r = new RecentAppCallback(this.getContext(),
				renAdapter);
		r.excute();
		frushGridView();
	}
	
	public void frushGridView() {

		mResultOK=false;
		mAnimationEnd=false;
	}

	public RecentRelativeLayout(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	private void init() {

		list = (GridView) findViewById(R.id.listView);
		// reloadButtons(this, appInfos, 20);
		renAdapter = new RencentAppAdapter(this.getContext(), appInfos);
		list.setAdapter(renAdapter);
		 r = new RecentAppCallback(this.getContext(),
				renAdapter);
		r.excute();
		frushGridView();
	}

	class GetRecentApp extends
			AsyncTask<Object, HashMap<String, Object>, Object> {
		RecentAppCallback recent;

		@Override
		protected Object doInBackground(Object... arg0) {
			recent.doInBackground(arg0[0]);
			return null;
		}

		public void doPublishProgress(HashMap<String, Object> info) {
			publishProgress(info);
		}

		public GetRecentApp(RecentAppCallback recent) {
			super();
			this.recent = recent;
		}

		@Override
		protected void onProgressUpdate(HashMap<String, Object>... values) {
			// TODO Auto-generated method stub
			super.onProgressUpdate(values);
			recent.onProgressUpdate(values[0]);

		}

		@Override
		protected void onPostExecute(Object result) {
			// TODO Auto-generated method stub

//			renAdapter.updateListView(appInfos);
			recent.onPostExecute(result);
			//add by zhouerlong 
			
			super.onPostExecute(result);
		}

	}

	class RecentAppCallback {
		private GetRecentApp task = new GetRecentApp(this);
		private Context mContext;
		private RencentAppAdapter mGrid;
//		List<HashMap<String, Object>> appInfos = new ArrayList<HashMap<String, Object>>();
	//add by zhouerlong 去掉这里是指 appInfos 引用错误

		Object doInBackground(Object obj) {
			int repeatCount = 10;
			reloadButtons(mContext, repeatCount, task);
			//填写默认值 add by zhouerlong
			return null;
		}

		public RecentAppCallback(Context mContext, RencentAppAdapter grid) {
			super();
			this.mContext = mContext;
			mGrid = grid;
		}

		public void excute() {
			task.execute(1);
		}

		public void cancel() {
			if(!task.isCancelled()) {

				task.cancel(true);
			}
			
		}

		void onProgressUpdate(HashMap<String, Object> values) {
			if (!task.isCancelled()) {
				appInfos.add(values);
				
			}

		}
		
	//add by zhouerlong begin 	
		public void onPostExecute(Object result) {
			// TODO Auto-generated method stub
			
//			renAdapter.updateListView(appInfos);
			updateListViews(true);
			
		}
	}
	
	
	
//	private String mAnimationEnd=null;
//	private  String mResultOK=null;
	
 	public void udateListView() {
		
//		renAdapter.updateListView(appInfos);
		updateListViews(false);
	}
	//此方法是在上拉动画结束以后 调用一次，数据加载完成调用一次  实现同步调用
 	synchronized public void updateListViews(boolean result) {
		if (result) {
			if (!mResultOK) {
				mResultOK = true;
			}
		}else {

			if (!mAnimationEnd) {
				mAnimationEnd = true;
			}
		}

		//如果上滑动画，数据加载都完成了 才实现刷新gridview 不然会出现卡顿现象
		if (mResultOK&&mAnimationEnd) {
			renAdapter.updateListView(appInfos);
		}
		
		//add by zhouerlong end
		
	}

	/**加载经期任务
	 * @param context
	 * @param appNumber
	 * @param recent
	 */
	public void reloadButtons(Context context, int appNumber,
			GetRecentApp recent) {
		int MAX_RECENT_TASKS = appNumber; // allow for some discards
		int repeatCount = appNumber;

		appInfos.removeAll(appInfos);

		// final Context context = activity.getApplication();
		final PackageManager pm = context.getPackageManager();
		final ActivityManager am = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);

		final List<ActivityManager.RecentTaskInfo> recentTasks = am
				.getRecentTasks(MAX_RECENT_TASKS + 1, 0x0002);

		ActivityInfo homeInfo = new Intent(Intent.ACTION_MAIN).addCategory(
				Intent.CATEGORY_HOME).resolveActivityInfo(pm, 0);
		int numTasks = recentTasks.size();
		for (int i = 0; i < numTasks && (i < MAX_RECENT_TASKS); i++) {
			HashMap<String, Object> singleAppInfo = new HashMap<String, Object>();
			final ActivityManager.RecentTaskInfo info = recentTasks.get(i);
			if (info.baseIntent.getComponent().getClassName().equals("com.google.android.velvet.ui.VelvetActivity")
					|| info.baseIntent.getComponent().getClassName().equals("com.google.android.apps.gsa.searchnow.SearchNowActivity")) {
				info.baseIntent.setClassName(info.baseIntent.getComponent().getPackageName(), "com.google.android.googlequicksearchbox.SearchActivity");
			}
			//add by zhouerlong google搜素的时候出现问 具体原因未知
			Intent intent = new Intent(info.baseIntent);
			if (info.origActivity != null) {
				intent.setComponent(info.origActivity);
			}
			if (homeInfo != null) {
				if (homeInfo.packageName.equals(intent.getComponent()
						.getPackageName())
						&& homeInfo.name.equals(intent.getComponent()
								.getClassName())) {
					MAX_RECENT_TASKS = MAX_RECENT_TASKS + 1;
					continue;
				}
			}
			intent.setFlags((intent.getFlags() & ~Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
					| Intent.FLAG_ACTIVITY_NEW_TASK);
			final ResolveInfo resolveInfo = pm.resolveActivity(intent, 0);
			if (resolveInfo != null) {
				final ActivityInfo activityInfo = resolveInfo.activityInfo;
				final String title = activityInfo.loadLabel(pm).toString();
				Drawable icon = null;

				Launcher launcher = (Launcher) mContext;

					 icon = activityInfo.loadIcon(pm);
			
				if (title != null && title.length() > 0 && icon != null) {
					singleAppInfo.put("title", title);
					singleAppInfo.put("icon", icon);
					singleAppInfo.put("tag", intent);
					singleAppInfo.put("packageName", activityInfo.packageName);
					recent.doPublishProgress(singleAppInfo);

					// appInfos.add(singleAppInfo);
				}
			}
		}
		MAX_RECENT_TASKS = repeatCount;
	}

}
