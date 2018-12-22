package com.prize.left.page.model;

import java.util.ArrayList;
import java.util.List;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;

import com.android.launcher3.LauncherSettings;
import com.android.launcher3.R;
import com.prize.left.page.bean.AppBean;
import com.prize.left.page.bean.AppsCardBean;
import com.prize.left.page.request.AppRequest;
import com.prize.left.page.response.AppResponse;
import com.prize.left.page.view.holder.AppViewHolder;
/***
 * 搜索应用业务类(最近应用)
 * @author fanjunchen
 *
 */
public class AppModel extends BaseModel<AppResponse> {

	private AppRequest reqParam;
	
	private AppResponse response;
	
	private List<AppBean> allQueryData = null;
	
	private AppViewHolder viewHolder = null;
	/**是否展开*/
	private boolean isExpand = false;
	/**默认显示的联系人个数*/
	private final int DEFAULT_NUM = 4;
	/**是否正在运行*/
	private boolean isRunning = false;
	
	public AppModel(Context ctx) {
		mCtx = ctx;
		reqParam = new AppRequest();
	}
	/**
	 * 设置查询串
	 * @param q
	 */
	public void setQuery(String q) {
		reqParam.queryStr = q;
	}
	/***
	 * 是否展开
	 * @param isOpen
	 */
	public void setExpand(boolean isOpen) {
		isExpand = isOpen;
		// 需要刷新页面
		if (isExpand)
			viewHolder.setDatas(allQueryData);
		else if (allQueryData != null)
			viewHolder.setDatas(allQueryData.subList(0, DEFAULT_NUM));
		viewHolder.setExpandText(isExpand);
	}
	
	public void setResIdentity(String res) {
	}

	@Override
	public void doGet() {
		// TODO Auto-generated method stub
		doPost();
	}

	@Override
	public void doPost() {
		// TODO Auto-generated method stub
		if (null == cancelObj || cancelObj.isCancelled()) {
			cancelObj = null;
			newHttpCallback();
			if (!isRunning) {
				isRunning = true;
				new GetAppTask().execute();
			}
		}
	}

	@Override
	public void cancel() {
		// TODO Auto-generated method stub
		if (null != cancelObj)
			cancelObj.cancel();
	}

	@Override
	public void onResponse(AppResponse resp) {
		// TODO Auto-generated method stub
		if (callback != null)
			callback.onResponse(resp);
		else {
			// 可以处理刷新UI的事情
			if (viewHolder == null)
				return;
			if (allQueryData != null && allQueryData.size() > DEFAULT_NUM)
				viewHolder.setExpandVisible(true);
			else
				viewHolder.setExpandVisible(false);
			viewHolder.setDatas(resp.data.items);
		}
	}
	
	private View.OnClickListener mLsn = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
				case R.id.txt_expand:
					setExpand(!isExpand);
					break;
			}
		}
	};
	
	@Override
	public void setViewHolder(RecyclerView.ViewHolder holder) {
		viewHolder = (AppViewHolder) holder;
		
		viewHolder.setExpandClick(mLsn);
	}
	
	@Override
	protected void newHttpCallback() {
		
	}
	/***
	 * 异步任务来查询处理数据
	 * @author fanjunchen
	 *
	 */
	class GetAppTask extends AsyncTask<Void, Void, Void> {
		List<AppBean> data = null;
		@Override
		protected Void doInBackground(Void... params) {
			// TODO 取数据
			if (reqParam == null || TextUtils.isEmpty(reqParam.queryStr))
				data = loadRecentApp(mCtx, 4);
			else {
				data = getApp(reqParam.queryStr);
			}
			return null;
		}
		
		private final String[] projection = new String[] {
				LauncherSettings.Favorites.INTENT
				,LauncherSettings.Favorites.TITLE
				,LauncherSettings.Favorites.ITEM_TYPE
		};
		
		private final String where = LauncherSettings.Favorites.ITEM_TYPE + "=? or " + LauncherSettings.Favorites.ITEM_TYPE + "=?"
				+ " and " + LauncherSettings.Favorites.TITLE + " like ?";
		
		private final String[] args = new String[] {String.valueOf(LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT), 
				String.valueOf(LauncherSettings.Favorites.ITEM_TYPE_APPLICATION)
				, null}; 
		
		private List<AppBean> getApp(String str) {
			 final Uri contentUri = LauncherSettings.Favorites.CONTENT_URI;
			 List<AppBean> rs = null;
             try {
            	 if (!TextUtils.isEmpty(str))
            		 args[2] = "%" + str + "%";
            	 else
            		 args[2] = "%";
            	 final Cursor c = mCtx.getContentResolver().query(contentUri, projection, where, args, null);
	             if (c != null && c.getCount()>0) {
	            	 final int itemIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.ITEM_TYPE);
	                 final int intentIndex = c.getColumnIndexOrThrow
	                         (LauncherSettings.Favorites.INTENT);
	                 final int titleIndex = c.getColumnIndexOrThrow
	                         (LauncherSettings.Favorites.TITLE);
	                 
	                 rs = new ArrayList<AppBean>();
	                 while (c.moveToNext()) {
	                	 AppBean a = new AppBean();
	                	 a.title = c.getString(titleIndex);
	                	 a.it = Intent.parseUri(c.getString(intentIndex), 0);
	                	 a.type = c.getInt(itemIndex);
	                	 if(a.it.getComponent() != null && a.title.contains(str))
	                	 rs.add(a);
	                 }
	                 
	                 c.close();
	             }
	             else if (c != null) {
	            	 c.close();
	             }
             }
             catch (Exception e) {
            	 rs = null;
             }
             return rs;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			// TODO 取完数据后回调
			if (response == null) {
				response = new AppResponse();
				response.data = new AppsCardBean();
			}
			allQueryData = data;
			if (allQueryData != null && allQueryData.size() > DEFAULT_NUM && !isExpand)
				response.data.items = allQueryData.subList(0, DEFAULT_NUM);
			else
				response.data.items = data;
			onResponse(response);
			isRunning = false;
			super.onPostExecute(result);
		}
		
		/**
		 * 加载近期任务
		 * @param context
		 * @param appNumber
		 * @param recent
		 */
		@SuppressWarnings("deprecation")
		public List<AppBean> loadRecentApp(Context context, int appNumber) {
			int MAX_RECENT_TASKS = appNumber; // allow for some discards
			int repeatCount = appNumber;

			List<AppBean> resultList = null;
			
			final PackageManager pm = context.getPackageManager();
			final ActivityManager am = (ActivityManager) context
					.getSystemService(Context.ACTIVITY_SERVICE);

			final List<ActivityManager.RecentTaskInfo> recentTasks = am
					.getRecentTasks(MAX_RECENT_TASKS + 1, ActivityManager.RECENT_IGNORE_HOME_STACK_TASKS);//0x0002
			
			ActivityInfo homeInfo = new Intent(Intent.ACTION_MAIN).addCategory(
					Intent.CATEGORY_HOME).resolveActivityInfo(pm, 0);
			int numTasks = recentTasks.size();
			
			resultList = new ArrayList<AppBean>(appNumber);
			
			for (int i = 0; i < numTasks && (i < MAX_RECENT_TASKS); i++) {
				
				final ActivityManager.RecentTaskInfo info = recentTasks.get(i);
				if (info.baseIntent.getComponent().getClassName().equals("com.google.android.velvet.ui.VelvetActivity")
						|| info.baseIntent.getComponent().getClassName().equals("com.google.android.apps.gsa.searchnow.SearchNowActivity")) {
					info.baseIntent.setClassName(info.baseIntent.getComponent().getPackageName(), "com.google.android.googlequicksearchbox.SearchActivity");
				}
				
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
				
					if (title != null && title.length() > 0) {
						AppBean ab = new AppBean();
						ab.title = title;
						ab.it =  intent;
						ab.type = 0;
						resultList.add(ab);
						ab = null;
					}
				}
			}
			MAX_RECENT_TASKS = repeatCount;
			return resultList;
		}
	}
	
	@Override
	public void doBindImg() {
		if (viewHolder != null) {
			//viewHolder.doBindImg();
		}
	}
}
