package com.prize.left.page.model;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;
import android.view.View;

import com.android.launcher3.LauncherSettings;
import com.android.launcher3.R;
import com.baidu.android.pushservice.PushLightapp;
import com.prize.left.page.bean.AppBean;
import com.prize.left.page.bean.ContactPerson;
import com.prize.left.page.bean.UsedCardBean;
import com.prize.left.page.response.UsedResponse;
import com.prize.left.page.ui.PushViewLinearLayout;
import com.prize.left.page.util.DBUtils;
import com.prize.left.page.view.holder.UsedViewHolder;
/***
 * 最近使用业务类(最近联系人及使用应用)
 * @author fanjunchen
 *
 */
public class UsedModel extends BaseModel<UsedResponse> {

	private UsedResponse response;
	
	private List<ContactPerson> personData = null;
	
	private List<AppBean> appData = null;
	
	private UsedViewHolder viewHolder = null;
	/**是否展开*/
	private boolean isExpand = false;
	/**默认显示的联系人个数*/
	private final int DEFAULT_NUM = 4;
	/**是否正在运行*/
	private boolean isRunning = false;
	
	public UsedModel(Context ctx) {
		mCtx = ctx;
		mUsageStatsManager = (UsageStatsManager) mCtx.getSystemService(Context.USAGE_STATS_SERVICE);
	}
	/***
	 * 是否展开
	 * @param isOpen
	 */
	public void setExpand(boolean isOpen) {
		isExpand = isOpen;
		// 需要刷新页面
		if (isExpand)
			viewHolder.setContactDatas(personData);
		else if (personData != null)
			viewHolder.setContactDatas(personData.subList(0, DEFAULT_NUM));
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
	public void doRefresh() {
		if (!isRunning) {
			isRunning = true;
			new GetUsedTask().execute();
			viewHolder.doRefresh();
		}
	}

	@Override
	public void doPost() {
		// TODO Auto-generated method stub
		if (null == cancelObj || cancelObj.isCancelled()) {
			cancelObj = null;
			newHttpCallback();
			if (!isRunning) {
				isRunning = true;
				new GetUsedTask().execute();
				viewHolder.doPost();
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
	public void onResume() {
		// TODO Auto-generated method stub
		doPost();
	}

	@Override
	public void onResponse(UsedResponse resp) {
		// TODO Auto-generated method stub
		if (callback != null)
			callback.onResponse(resp);
		else {
			// 可以处理刷新UI的事情
			if (viewHolder == null)
				return;
			if (personData != null && personData.size() > DEFAULT_NUM)
				viewHolder.setExpandVisible(true);
			else
				viewHolder.setExpandVisible(false);
			
			if ((resp.data.items == null ||
					resp.data.items.size()<1)
					&& (resp.data.appDatas == null || resp.data.appDatas.size()<1)) { //response == resp && 
				
				//viewHolder.itemView.setVisibility(View.GONE);
				if (mICdNotify != null)
					mICdNotify.notifyUpdate(mCdType, false);
				else
					viewHolder.itemView.setVisibility(View.GONE);
				return;
			}
			
			if (mICdNotify != null)
				mICdNotify.addUpdate(mCdType);
			else
				viewHolder.itemView.setVisibility(View.VISIBLE);
			response.data.items = resp.data.items;
			response.data.appDatas = resp.data.appDatas;

			viewHolder.setAppDatas(resp.data.appDatas);
			viewHolder.setContactDatas(resp.data.items);
			/*else
				viewHolder.itemView.setVisibility(View.VISIBLE);*/
			
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
		viewHolder = (UsedViewHolder) holder;
		
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
	class GetUsedTask extends AsyncTask<Void, Void, Void> {
		List<ContactPerson> data = null;
		
		List<AppBean> appDt = null;
		@Override
		protected Void doInBackground(Void... params) {
			// TODO 取数据
			data = getContacts();
			appDt = loadRecentApp(mCtx, DEFAULT_NUM);
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			// TODO 取完数据后回调
			UsedResponse r = new UsedResponse();
			r.data = new UsedCardBean();
			if (response == null) {
				response = r;
			}
			
			personData = data;
			if (personData != null && personData.size() > DEFAULT_NUM && !isExpand)
				r.data.items = personData.subList(0, DEFAULT_NUM);
			else
				r.data.items = data;
			
			if (appData != null && appData.size() > DEFAULT_NUM && !isExpand)
				r.data.appDatas = appData.subList(0, DEFAULT_NUM);
			else
				r.data.appDatas = appDt;
			onResponse(r);
			isRunning = false;
			//super.onPostExecute(result);
		}
	}
	
	public List<ContactPerson> getContacts() {
		List<ContactPerson> rs = null;
		rs = DBUtils.getCallRecord(mCtx, DEFAULT_NUM);
		if (null == rs || rs.size() < 1) {
			rs = null;
			rs = DBUtils.getPhoneContacts(mCtx, DEFAULT_NUM, null);
		}
		else {
			int s = rs.size();
			if (s > 0) {
				List<ContactPerson> bb = DBUtils.getPhoneContacts(mCtx, DEFAULT_NUM - s, rs);
				rs.addAll(bb);
			}
		}
		ContactPerson temp = null;
		ContactPerson repeatPhoneContacts = null;
		list = new ArrayList<ContactPerson>();
		list.addAll(rs);
        for (int i = 0; i < rs.size() - 1; i++)
        {
            temp = rs.get(i);
            
            for (int j = i + 1; j < rs.size(); j++)
            {
              if(temp.phoneNum.equals(rs.get(j).phoneNum)){
            	  if(TextUtils.isEmpty(temp.phoneNum)){
            		  repeatPhoneContacts = temp;
            	  }else{
            		  repeatPhoneContacts = rs.get(j);
            	  }
            	  if(repeatPhoneContacts != null){
                  	list.remove(repeatPhoneContacts);
              }
            }
        }
        
        }
        
		return list;
	}
	
	public int getUsedNum() {
		int rs = 0;
		rs = DBUtils.getCallRecordNum(mCtx, DEFAULT_NUM);
		if (rs < 1) {
			rs = DBUtils.getPhoneContactsNum(mCtx, DEFAULT_NUM);
		}
		return rs;
	}
	/**
	 * 加载近期任务
	 * @param context
	 * @param appNumber
	 * @param recent
	 */
	public List<AppBean> loadRecentApp(Context context, int appNumber) {
		List<AppBean> resultList = null;
		/*int MAX_RECENT_TASKS = appNumber; // allow for some discards
		int repeatCount = appNumber;

		final PackageManager pm = context.getPackageManager();
		final ActivityManager am = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);

		final List<ActivityManager.RecentTaskInfo> recentTasks = am
				.getRecentTasks(MAX_RECENT_TASKS + 2, ActivityManager.RECENT_IGNORE_HOME_STACK_TASKS);//0x0002
		
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
				else if ("com.android.settings".equals(intent.getComponent()
							.getPackageName())
							&& "com.android.settings.UsbSettings".equals(intent.getComponent()
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
		MAX_RECENT_TASKS = repeatCount;*/
		resultList = getBestApps(appNumber);
		return resultList;
	}
	
	@Override
	public void doBindImg() {
		if (viewHolder != null) {
			//viewHolder.doBindImg();
		}
	}

	private static final int _DISPLAY_ORDER_USAGE_TIME = 0;
	private static final int _DISPLAY_ORDER_LAST_TIME_USED = 1;

	private int mDisplayOrder = _DISPLAY_ORDER_USAGE_TIME;
	//private LastTimeUsedComparator mLastTimeUsedComparator = new LastTimeUsedComparator();
	private UsageTimeComparator mUsageTimeComparator = new UsageTimeComparator();
	private final ArrayList<UsageStats> mPackageStats = new ArrayList<>();
	private UsageStatsManager mUsageStatsManager = null;
	private List<ContactPerson> list;

	private List<AppBean> getBestApps(final int max) {

		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DAY_OF_YEAR, -5);

		final List<UsageStats> stats = mUsageStatsManager.queryUsageStats(
				UsageStatsManager.INTERVAL_BEST, cal.getTimeInMillis(),
				System.currentTimeMillis());
		if (stats == null) {
			return null;
		}
		mPackageStats.clear();
		Log.e("UsageStats", "=UsageStats2222==size=" + stats.size());

		ArrayMap<String, UsageStats> map = new ArrayMap<>();
		final int statCount = stats.size();
		for (int i = 0; i < statCount; i++) {
			final android.app.usage.UsageStats pkgStats = stats.get(i);
			try {
				UsageStats existingStats = map.get(pkgStats.getPackageName());
				if (existingStats == null) {
					map.put(pkgStats.getPackageName(), pkgStats);
				} else {
					existingStats.add(pkgStats);
				}

			} catch (Exception e) {
				// This package may be gone.
			}
		}
		mPackageStats.addAll(map.values());
		// Sort list
		sortList();
		//查询出前四个数据是否在launcher的数据库中存在
		int count = 0;
		List<AppBean> resultList = null;
		List<String> pkgs = new ArrayList<String>(12);
		for (UsageStats a : mPackageStats) {
			String pkg = a.getPackageName();
			try{
			AppBean ab = getApp(mCtx, pkg);
			if (null == ab)
				continue;
			
			if (pkgs.contains(pkg))
				continue;
			pkgs.add(pkg);
			count ++;
			if (null == resultList)
				resultList = new ArrayList<AppBean>(4);
//			if(null != LauncherProvider.getRenameAppName(pkg))
//				ab.title = LauncherProvider.getRenameAppName(pkg);
			resultList.add(ab);
			if (count >= max)
				break;
			}catch(Exception e){
		}
		}
		return resultList;
	}
	/***
	 * 通过包名获取应用
	 * @param context
	 * @param pkgName
	 * @return
	 */
	private AppBean getApp(Context context, String pkgName) {
        final ContentResolver cr = context.getContentResolver();
        PackageManager pm=context.getPackageManager();
        AppBean ab = null;
        if (pkgName != null) {
	        Cursor c = cr.query(LauncherSettings.Favorites.CONTENT_URI,
	            new String[] { "title", "intent" }, "package_name =?",
	            new String[] { pkgName }, null);
	        
	        try {
	            if (c.moveToFirst()) {
	            	ab = new AppBean();
//	            	ab.title = c.getString(0);
					ab.it = Intent.parseUri(c.getString(1), 0);
					ab.title=(String) pm.getApplicationLabel(pm.getApplicationInfo(pkgName,PackageManager.GET_META_DATA));
	            }
	        }
	        catch (Exception e) {
				e.printStackTrace();
				ab = null;
			}
	        finally {
	            c.close();
	        }
        }
        return ab;
    }
	
	/***
	 * 排序
	 */
	private void sortList() {
		if (mDisplayOrder == _DISPLAY_ORDER_USAGE_TIME) {
			Collections.sort(mPackageStats, mUsageTimeComparator);
		} 
		/*else if (mDisplayOrder == _DISPLAY_ORDER_LAST_TIME_USED) {
			Collections.sort(mPackageStats, mLastTimeUsedComparator);
		} */
	}

	/*public static class LastTimeUsedComparator implements
			Comparator<UsageStats> {
		@Override
		public final int compare(UsageStats a, UsageStats b) {
			// return by descending order
			return (int) (b.getLastTimeUsed() - a.getLastTimeUsed());
		}
	}*/

	public static class UsageTimeComparator implements Comparator<UsageStats> {
		@Override
		public final int compare(UsageStats a, UsageStats b) {
			return (int) (b.getTotalTimeInForeground() - a
					.getTotalTimeInForeground());
		}
	}
}
